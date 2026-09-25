package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Magic chemical bath.
 *
 * <p>The pattern records the exact water cells. After formation, one bucket
 * from the input hatches fills one cell every five ticks; recipes wait until
 * all cells contain source water, as in the upstream structure.</p>
 */
public class MagicChemicalBathMachine extends MagicMultiblockController {

    private static final String WATER_CELLS = "pollution.bath.water_cells";
    private List<BlockPos> waterCells = List.of();
    private TickableSubscription waterSubscription;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<BlockPos> matched = getMultiblockState().getMatchContext().get(WATER_CELLS);
        waterCells = matched == null ? List.of() : List.copyOf(matched);
        if (waterSubscription != null) waterSubscription.unsubscribe();
        waterSubscription = subscribeServerTick(this::fillWater);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        clearWaterSubscription();
        waterCells = List.of();
    }

    @Override
    public void onUnload() {
        clearWaterSubscription();
        super.onUnload();
    }

    private void clearWaterSubscription() {
        if (waterSubscription != null) {
            waterSubscription.unsubscribe();
            waterSubscription = null;
        }
    }

    private boolean waterFilled() {
        return !waterCells.isEmpty() && waterCells.stream().allMatch(pos ->
                getLevel().hasChunkAt(pos) && getLevel().getBlockState(pos).is(Blocks.WATER)
                        && getLevel().getFluidState(pos).isSource());
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        return waterFilled() && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return waterFilled() && super.onWorking();
    }

    private void fillWater() {
        if (!isFormed() || getOffsetTimer() % 5 != 0) return;
        for (BlockPos pos : waterCells) {
            if (!getLevel().hasChunkAt(pos)) return;
            var state = getLevel().getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()) continue;
            if (!state.isAir() && !state.is(Blocks.WATER)) return;
            FluidStack bucket = new FluidStack(Fluids.WATER, 1000);
            var tanks = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).stream()
                    .filter(IFluidHandler.class::isInstance).map(IFluidHandler.class::cast).toList();
            int available = 0;
            for (IFluidHandler tank : tanks) {
                available += drainWater(tank, new FluidStack(Fluids.WATER, 1000 - available),
                        IFluidHandler.FluidAction.SIMULATE).getAmount();
                if (available >= 1000) break;
            }
            if (available < 1000) return;
            if (!getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState())) return;
            int remaining = bucket.getAmount();
            for (IFluidHandler tank : tanks) {
                remaining -= drainWater(tank, new FluidStack(Fluids.WATER, remaining),
                        IFluidHandler.FluidAction.EXECUTE).getAmount();
                if (remaining <= 0) break;
            }
            return;
        }
    }

    public MagicChemicalBathMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    private static FluidStack drainWater(IFluidHandler handler, FluidStack requested, IFluidHandler.FluidAction action) {
        // Input hatches prohibit external extraction; their controller uses the internal recipe side.
        return handler instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank tank
                ? tank.drainInternal(requested, action) : handler.drain(requested, action);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XCCCCCX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XXXSXXX", "XXXXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_WATER.get(),
                        GTRecipeTypes.CHEMICAL_BATH_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('A', Predicates.custom(state -> {
                    if (!state.getBlockState().isAir() && !state.getBlockState().is(Blocks.WATER)) return false;
                    List<BlockPos> cells = state.getMatchContext().getOrPut(WATER_CELLS, new ArrayList<BlockPos>());
                    cells.add(state.getPos().immutable());
                    return true;
                }, () -> new BlockInfo[]{BlockInfo.fromBlock(Blocks.WATER)}))
                .build();
    }
}
