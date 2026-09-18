package meowmel.pollution.api.pollution;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import meowmel.pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds chunk pollution when a Pollution machine explodes.
 *
 * <p>Modern GregTech has no {@code executeExplosion} hook to mix into and the
 * port's self-destructing machines (the flux promoted fuel cell) call
 * {@code Level#explode} directly. The closest equivalent is Forge's explosion
 * event: an explosion is attributed to a Pollution machine when its center
 * lies on a machine registered under the {@code pollution} namespace. The
 * machine block still exists at {@code ExplosionEvent.Start}, so the lookup is
 * reliable.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
public final class MachinePollutionEvents {

    private MachinePollutionEvents() {}

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Explosion explosion = event.getExplosion();
        Vec3 center = explosion.getPosition();
        BlockPos pos = BlockPos.containing(center.x, center.y, center.z);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine == null || machine.getDefinition() == null
                || !Pollution.MOD_ID.equals(machine.getDefinition().getId().getNamespace())) {
            return;
        }
        MachinePollution.addExplosionPollution(level, pos, MachinePollution.MACHINE_EXPLOSION_POWER);
    }
}
