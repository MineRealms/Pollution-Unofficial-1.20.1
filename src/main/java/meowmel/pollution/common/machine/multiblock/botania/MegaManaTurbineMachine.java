package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Mega mana turbine: burns mana fluids for EU, with a catalyst boost and a
 * continuous-running parallel ramp.
 *
 * <p>Upstream origin: {@code MetaTileEntityMegaManaTurbine} (1.12.2, an
 * unregistered WIP class next to the registered {@code MetaTileEntityMagicMegaTurbine}).
 * It ran the {@code mana_to_eu} fuel map at ZPM on a fusion-glass/bifrost shell
 * with fusion coils and tiered coil casings, and its recipe logic:</p>
 * <ul>
 *   <li>allowed a maximum output voltage from the highest available catalyst
 *       pair: none -&gt; UV, black+white Mansus -&gt; UHV, KQGold+Hyperdimensional
 *       Silver -&gt; 2A UEV, Sentient+Binding Metal -&gt; UIV;</li>
 *   <li>drained 32 mB of each catalyst fluid every 100 working ticks;</li>
 *   <li>raised its parallel limit from 1 to 32768 over {@code 60000 / coilLevel}
 *       continuous working ticks and scaled fuel use / generation with it;</li>
 *   <li>disabled overclocking.</li>
 * </ul>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>The GTQT catalyst materials (BlackMansus, WhiteMansus, KQGold,
 *       HyperdimensionalSilver, SentientMetal, BindingMetal) are not part of
 *       the port yet. The logic resolves the upstream fluid ids
 *       ({@code pollution:black_mansus}, ...) through {@link ForgeRegistries#FLUIDS};
 *       missing materials simply mean the catalyst level stays 0, so the
 *       machine is fully functional once the materials are ported. The
 *       upstream drain bugs (WhiteMansus drained twice, advanced pairs never
 *       drained at level 1) are fixed by draining the active pair once per
 *       100 ticks.</li>
 *   <li>Modern {@code RecipeLogic} has no {@code getMaxVoltage} /
 *       {@code getParallelLimit} hooks, so the catalyst cap is enforced as a
 *       recipe acceptance check ({@code pollution.magic.failure.catalyst}) and
 *       the parallel ramp is applied per craft by copying the recipe and
 *       setting {@code parallels}; parallel therefore updates when a new craft
 *       starts instead of every tick.</li>
 *   <li>The GTQT tiered coil casings map to standard GregTech heating coils;
 *       the coil level is read from the matched {@code ICoilType}.</li>
 *   <li>{@code isAllowOverclocking = false} has no modern equivalent because
 *       this controller registers no overclocking recipe modifier.</li>
 * </ul>
 */
public class MegaManaTurbineMachine extends ManaMultiblockController implements IDisplayUIMachine {

    /** Upstream base cap: UV without a catalyst. */
    private static final int BASE_CAPACITY_TIER = 8;
    private static final int CATALYST_AMOUNT = 32;
    private static final int MAX_PARALLEL = 32768;
    /** Upstream: 3000 s per coil level. */
    private static final long RAMP_TICKS_PER_COIL_LEVEL = 60000L;

    /**
     * Catalyst pairs ordered by priority (advanced first), exactly like
     * upstream's {@code CatalystLevel} switch. The fourth column is the
     * catalyst level.
     */
    private static final int[][] CATALYST_PAIRS = {
            { 4, 5, 3 }, // sentient_metal + binding_metal
            { 2, 3, 2 }, // kq_gold + hyperdimensional_silver
            { 0, 1, 1 }, // black_mansus + white_mansus
    };

    private static final ResourceLocation[] CATALYST_FLUID_IDS = {
            id("black_mansus"),
            id("white_mansus"),
            id("kq_gold"),
            id("hyperdimensional_silver"),
            id("sentient_metal"),
            id("binding_metal"),
    };

    private int catalystLevel;
    private long catalystAllowedMaxCapacity = GTValues.V[BASE_CAPACITY_TIER];
    private int parallel = 1;

    public MegaManaTurbineMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.MANA_TO_EU);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MegaManaTurbineRecipeLogic(this);
    }

    /** Matched GT heating-coil level, 0 when the shell has no coils. */
    public int coilLevel() {
        return coilType == null ? 0 : coilType.getLevel();
    }

    public int catalystLevel() {
        return catalystLevel;
    }

    public long catalystAllowedMaxCapacity() {
        return catalystAllowedMaxCapacity;
    }

    public int parallel() {
        return parallel;
    }

    /**
     * Recomputes the active catalyst pair and its output cap. Pairs are checked
     * from the highest level down, so an advanced catalyst always wins.
     */
    void updateCatalyst() {
        int level = 0;
        for (int[] pair : CATALYST_PAIRS) {
            FluidStack first = fluid(CATALYST_FLUID_IDS[pair[0]], CATALYST_AMOUNT);
            FluidStack second = fluid(CATALYST_FLUID_IDS[pair[1]], CATALYST_AMOUNT);
            if (!first.isEmpty() && !second.isEmpty() && hasFluid(first) && hasFluid(second)) {
                level = pair[2];
                break;
            }
        }
        catalystLevel = level;
        catalystAllowedMaxCapacity = switch (level) {
            case 1 -> GTValues.V[9];
            case 2 -> 2L * GTValues.V[10];
            case 3 -> GTValues.V[12];
            default -> GTValues.V[BASE_CAPACITY_TIER];
        };
    }

    /** Drains one catalyst dose of the active pair (upstream: every 100 ticks). */
    void drainCatalyst() {
        for (int[] pair : CATALYST_PAIRS) {
            if (pair[2] != catalystLevel) {
                continue;
            }
            drainFluid(fluid(CATALYST_FLUID_IDS[pair[0]], CATALYST_AMOUNT));
            drainFluid(fluid(CATALYST_FLUID_IDS[pair[1]], CATALYST_AMOUNT));
            return;
        }
    }

    /** Upstream {@code parallelLimit()}: 1..32768 ramp over 3000 s per coil level. */
    int parallelLimit(long continuousRunningTime) {
        int coil = Math.max(1, coilLevel());
        long ramp = RAMP_TICKS_PER_COIL_LEVEL / coil;
        if (continuousRunningTime <= ramp) {
            parallel = 1 + Math.round((continuousRunningTime / (float) ramp) * (MAX_PARALLEL - 1));
        } else {
            parallel = MAX_PARALLEL;
        }
        return parallel;
    }

    private List<FluidHatchPartMachine> inputFluidHatches() {
        List<FluidHatchPartMachine> hatches = new java.util.ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && PartAbility.IMPORT_FLUIDS.isApplicable(hatch.getBlockState().getBlock())) {
                hatches.add(hatch);
            }
        }
        return hatches;
    }

    private boolean hasFluid(FluidStack stack) {
        for (FluidHatchPartMachine hatch : inputFluidHatches()) {
            FluidStack stored = hatch.tank.getFluidInTank(0);
            if (!stored.isEmpty() && stored.getFluid() == stack.getFluid()
                    && stored.getAmount() >= stack.getAmount()) {
                return true;
            }
        }
        return false;
    }

    private void drainFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (FluidHatchPartMachine hatch : inputFluidHatches()) {
            FluidStack stored = hatch.tank.getFluidInTank(0);
            if (!stored.isEmpty() && stored.getFluid() == stack.getFluid()
                    && stored.getAmount() >= stack.getAmount()) {
                hatch.tank.drain(stack, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    private static FluidStack fluid(ResourceLocation id, int amount) {
        var fluid = ForgeRegistries.FLUIDS.getValue(id);
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, path);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.mega_mana_turbine.catalyst", catalystLevel));
            textList.add(Component.translatable("pollution.machine.mega_mana_turbine.max_output",
                    MultiblockManaProvider.formatNumbers(catalystAllowedMaxCapacity)));
            textList.add(Component.translatable("pollution.machine.mega_mana_turbine.parallel",
                    parallel, Math.max(1, coilLevel())));
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return MegaManaTurbinePatterns.create(definition);
    }

    /** Fuel logic with the catalyst upkeep and the continuous-running ramp. */
    protected static class MegaManaTurbineRecipeLogic extends MagicRecipeLogic {

        private final MegaManaTurbineMachine turbine;
        private long catalystDrainTimer;

        public MegaManaTurbineRecipeLogic(MegaManaTurbineMachine machine) {
            super(machine);
            this.turbine = machine;
        }

        @Override
        protected ActionResult checkRecipe(GTRecipe recipe) {
            turbine.updateCatalyst();
            ActionResult result = super.checkRecipe(recipe);
            if (!result.isSuccess()) {
                return result;
            }
            EnergyStack output = recipe.getOutputEUt();
            if (output != null && output.voltage() > turbine.catalystAllowedMaxCapacity()) {
                return ActionResult.fail(Component.translatable("pollution.magic.failure.catalyst"), null, null);
            }
            return ActionResult.SUCCESS;
        }

        @Override
        public void setupRecipe(GTRecipe recipe) {
            turbine.updateCatalyst();
            GTRecipe scaled = recipe.copy();
            scaled.parallels = Math.max(1, turbine.parallelLimit(getTotalContinuousRunningTime()));
            super.setupRecipe(scaled);
        }

        @Override
        public ActionResult handleTickRecipe(GTRecipe recipe) {
            turbine.updateCatalyst();
            ActionResult result = super.handleTickRecipe(recipe);
            if (result.isSuccess() && ++catalystDrainTimer >= 100L) {
                catalystDrainTimer = 0L;
                turbine.drainCatalyst();
            }
            return result;
        }
    }
}
