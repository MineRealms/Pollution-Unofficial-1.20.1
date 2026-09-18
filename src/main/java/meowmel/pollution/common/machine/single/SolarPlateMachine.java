package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Solar plate: generates EU from light with six themed kinds and a per-kind
 * boost condition.
 *
 * <p>Semantics match upstream {@code MetaTileEntitySolarPlate}: output is
 * {@code VA[tier (+1 when boosted)] / 3}; kinds 2 and 4 work regardless of the
 * sun, kind 3 needs daylight without a sky check, the other kinds need daylight
 * and sky access. The upstream 7x7 sky scan is replaced by a sky check directly
 * above the machine to avoid per-tick multi-position scans.</p>
 */
public class SolarPlateMachine extends PollutionEnergyMachine {

    public static final int KIND_AIR = 1;
    public static final int KIND_ENTROPY = 2;
    public static final int KIND_EARTH = 3;
    public static final int KIND_FIRE = 4;
    public static final int KIND_ORDER = 5;
    public static final int KIND_WATER = 6;

    private final int kind;

    public SolarPlateMachine(IMachineBlockEntity info, int tier, int kind) {
        super(info, tier);
        this.kind = kind;
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!isProducing(level)) {
            return;
        }
        int boost = hasBoost(level) ? 1 : 0;
        energyContainer.addEnergy(GTValues.VA[getTier() + boost] / 3);
    }

    private boolean isProducing(ServerLevel level) {
        return switch (kind) {
            case KIND_ENTROPY, KIND_FIRE -> true;
            case KIND_EARTH -> level.isDay();
            default -> level.isDay() && level.canSeeSky(getPos().above());
        };
    }

    private boolean hasBoost(ServerLevel level) {
        return switch (kind) {
            case KIND_AIR -> getPos().getY() > 160;
            case KIND_ENTROPY -> level.isNight();
            case KIND_EARTH -> getPos().getY() < 10;
            case KIND_FIRE -> level.dimension() == Level.NETHER;
            case KIND_ORDER -> level.isDay();
            case KIND_WATER -> level.getBlockState(getPos().below()).is(Blocks.WATER);
            default -> false;
        };
    }
}
