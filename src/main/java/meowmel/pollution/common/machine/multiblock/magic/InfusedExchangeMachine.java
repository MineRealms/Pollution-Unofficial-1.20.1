package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSearch;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSourceRef;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

/**
 * Infused exchange: drains essentia from a container next to the machine and
 * emits the matching infused fluid through the export hatch
 * (1 essentia unit -&gt; 144 mB, upstream ratio).
 *
 * <p>Upstream read a TC6 jar directly above the controller and pushed into the
 * controller's output tank. The port uses the TC4R essentia API: the six primal
 * aspects are probed around the block above the controller
 * ({@code EssentiaApi.findSource}/{@code extract}) and the fluid goes into the
 * GregTech export fluid hatch tank above.</p>
 */
public class InfusedExchangeMachine extends AbstractDisplayMultiblockMachine {

    private static final int MB_PER_ESSENTIA = 144;
    private static final int SEARCH_RANGE = 3;

    private TickableSubscription tickSubscription;

    public InfusedExchangeMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickExchange);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tickExchange() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        if (getOffsetTimer() % 10 != 0) {
            return;
        }
        FluidHatchPartMachine output = findFluidHatch();
        if (output == null) {
            return;
        }
        for (AspectId aspect : AspectApi.primals()) {
            Optional<EssentiaSourceRef> source = EssentiaApi.findSource(level, getPos().above(), aspect, 1,
                    EssentiaSearch.nearby(SEARCH_RANGE));
            if (source.isEmpty()) {
                continue;
            }
            Material material = PollutionAspectMapping.materialOf(aspect).orElse(null);
            if (material == null || !material.hasFluid()) {
                return;
            }
            FluidStack fluid = material.getFluid(MB_PER_ESSENTIA);
            if (fluid.isEmpty() ||
                    output.tank.fill(fluid, IFluidHandler.FluidAction.SIMULATE) < MB_PER_ESSENTIA) {
                return;
            }
            if (EssentiaApi.extract(level, source.get(), aspect, 1, EssentiaTransferMode.EXECUTE) <= 0) {
                return;
            }
            output.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            return;
        }
    }

    private FluidHatchPartMachine findFluidHatch() {
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch) {
                return hatch;
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("S")
                .aisle("A")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1))
                .build();
    }
}
