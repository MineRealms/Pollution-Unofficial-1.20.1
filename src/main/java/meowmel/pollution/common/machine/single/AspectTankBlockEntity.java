package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectContainerView;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSource;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Block entity of the aspect tank.
 *
 * <p>TC4R discovers essentia transports through block entities
 * ({@code ThaumcraftApiHelper.getConnectableTransport} and the tube logic cast
 * the neighbour to {@link EssentiaTransport}). A plain GTCEu machine is not a
 * block entity, so the aspect tank registers this subclass through the
 * {@code GTRegistrate.machine(..., blockEntityFactory)} overload and delegates
 * the whole essentia contract to {@link AspectTankMachine}. Without it the tank
 * would be invisible to tubes, golems, mirrors and alembics and could only push
 * essentia actively.</p>
 *
 * <p>All methods are thin delegations; the semantics live in
 * {@link AspectTankMachine}.</p>
 */
public class AspectTankBlockEntity extends MetaMachineBlockEntity
        implements EssentiaTransport, EssentiaSource, AspectContainerView {

    public AspectTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AspectTankMachine tank() {
        return (AspectTankMachine) getMetaMachine();
    }

    // ////////////////////////////////////
    // ***** EssentiaTransport *****//
    // ////////////////////////////////////

    @Override
    public boolean isConnectable(Direction side) {
        return tank().isConnectable(side);
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return tank().canInputFrom(side);
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return tank().canOutputTo(side);
    }

    @Nullable
    @Override
    public AspectId suctionType(Direction side) {
        return tank().suctionType(side);
    }

    @Override
    public int suctionAmount(Direction side) {
        return tank().suctionAmount(side);
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        return tank().takeEssentia(aspect, amount, side, mode);
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        return tank().addEssentia(aspect, amount, side, mode);
    }

    @Nullable
    @Override
    public AspectId essentiaType(Direction side) {
        return tank().essentiaType(side);
    }

    @Override
    public int essentiaAmount(Direction side) {
        return tank().essentiaAmount(side);
    }

    @Nullable
    @Override
    public AspectId extractableAspect(Direction side) {
        return tank().extractableAspect(side);
    }

    @Override
    public int minimumSuction() {
        return tank().minimumSuction();
    }

    @Override
    public boolean renderExtendedTube() {
        return tank().renderExtendedTube();
    }

    // ////////////////////////////////////
    // ***** EssentiaSource *****//
    // ////////////////////////////////////

    @Override
    public int extractEssentia(AspectId aspect, int amount, EssentiaTransferMode mode) {
        return tank().extractEssentia(aspect, amount, mode);
    }

    // ////////////////////////////////////
    // ***** AspectContainerView *****//
    // ////////////////////////////////////

    @Override
    public AspectAmounts visibleAspects() {
        return tank().visibleAspects();
    }

    @Override
    public Set<AspectId> visibleAspectFilters() {
        return tank().visibleAspectFilters();
    }
}
