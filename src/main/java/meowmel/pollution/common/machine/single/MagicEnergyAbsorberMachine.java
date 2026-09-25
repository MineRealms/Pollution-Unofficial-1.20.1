package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;

/**
 * Magic energy absorber: generates EU while a magic pedestal block sits on top.
 *
 * <p>Upstream ({@code MetaTileEntityMagicEnergyAbsorber}) recognized dragon
 * eggs (HV), Botania mana/gaia pylons (HV/EV) and the Gaia head (IV). The
 * modern port checks Botania's flattened block registrations directly; the
 * old 1.12 pylon metadata values are no longer needed.</p>
 */
public class MagicEnergyAbsorberMachine extends PollutionEnergyMachine {

    private static final int DRAGON_EGG_OUTPUT_TIER = 3;
    private static final int MANA_PYLON_OUTPUT_TIER = 3;
    private static final int GAIA_PYLON_OUTPUT_TIER = 4;
    private static final int GAIA_HEAD_OUTPUT_TIER = 5;

    public MagicEnergyAbsorberMachine(IMachineBlockEntity info, int tier) {
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
        energyContainer.addEnergy(generationVoltage(level.getBlockState(getPos().above())));
    }

    /** Generation is determined by the pedestal, independently of the output hatch voltage. */
    public static long generationVoltage(BlockState state) {
        var block = state.getBlock();
        int outputTier = 0;
        if (block == Blocks.DRAGON_EGG) {
            outputTier = DRAGON_EGG_OUTPUT_TIER;
        } else if (block == BotaniaBlocks.manaPylon) {
            outputTier = MANA_PYLON_OUTPUT_TIER;
        } else if (block == BotaniaBlocks.gaiaPylon) {
            outputTier = GAIA_PYLON_OUTPUT_TIER;
        } else if (block == BotaniaBlocks.gaiaHead) {
            outputTier = GAIA_HEAD_OUTPUT_TIER;
        }
        return outputTier > 0 ? GTValues.V[outputTier] : 0L;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (getLevel() != null) {
            var state = getLevel().getBlockState(getPos().above());
            textList.add(Component.translatable("pollution.machine.magic_energy_absorber.pedestal",
                    state.getBlock().getName(), generationVoltage(state)));
        }
    }
}
