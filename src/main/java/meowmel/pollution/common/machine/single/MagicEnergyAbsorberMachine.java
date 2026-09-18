package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Magic energy absorber: generates EU while a magic pedestal block sits on top.
 *
 * <p>Upstream ({@code MetaTileEntityMagicEnergyAbsorber}) recognized dragon
 * eggs (HV), Botania pylons (HV/EV) and the Gaia head (IV). Botania is a
 * Phase 6 optional integration, so only the vanilla dragon egg pedestal is
 * ported here; the Botania pedestals stay TODO.</p>
 */
public class MagicEnergyAbsorberMachine extends PollutionEnergyMachine {

    private static final int DRAGON_EGG_OUTPUT_TIER = 3;

    public MagicEnergyAbsorberMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
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
        if (level.getBlockState(getBlockPos().above()).is(Blocks.DRAGON_EGG)) {
            energyContainer.addEnergy(GTValues.V[DRAGON_EGG_OUTPUT_TIER]);
        }
    }
}
