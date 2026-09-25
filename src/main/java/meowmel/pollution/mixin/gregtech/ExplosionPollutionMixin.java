package meowmel.pollution.mixin.gregtech;

import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import meowmel.pollution.api.pollution.MachinePollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.gregtechceu.gtceu.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;

/** The GT hook retains the machine position and actual explosion strength after block removal. */
@Mixin(value = {TieredEnergyMachine.class, SteamBoilerMachine.class, LargeBoilerMachine.class,
        ActiveTransformerMachine.class, EnergyHatchPartMachine.class}, remap = false)
public abstract class ExplosionPollutionMixin implements IExplosionMachine {
    @Override
    public void doExplosion(BlockPos pos, float strength) {
        var machine = self();
        if (machine.getLevel() instanceof ServerLevel level) {
            MachinePollution.addExplosionPollution(level, pos, strength);
        }
        // Mirror the three statements of GT's default implementation. Calling
        // Interface.super here is rewritten incorrectly by Mixin 0.8.5 when
        // this implementation is merged into a concrete target class.
        var level = machine.getLevel();
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, strength,
                ConfigHolder.INSTANCE.machines.doesExplosionDamagesTerrain
                        ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }
}
