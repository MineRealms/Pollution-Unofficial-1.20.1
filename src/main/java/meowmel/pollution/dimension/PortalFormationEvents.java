package meowmel.pollution.dimension;

import meowmel.pollution.Pollution;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Source diamond-in-water ritual, restricted to nearby loaded item entities. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
public final class PortalFormationEvents {
    private PortalFormationEvents() {}
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide
                && event.player.tickCount % (PollutionConfig.CHECK_PORTAL_DESTINATION.get() ? 100 : 20) == 0) {
            checkForPortalCreation(event.player);
        }
    }
    public static void checkForPortalCreation(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(PollutionDimensions.UNDERGROUND)
                && !level.dimension().equals(PollutionTeleporter.originDimension())
                && !PollutionConfig.ALLOW_PORTALS_IN_OTHER_DIMENSIONS.get()) return;
        var portal = PollutionMiscBlocks.PORTAL.get();
        for (var item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32),
                item -> item.isAlive() && item.getItem().is(Items.DIAMOND))) {
            if (!portal.canFormPortal(level.getBlockState(item.blockPosition()))) continue;
            level.sendParticles(ParticleTypes.WITCH, item.getX(), item.getY() + .2, item.getZ(), 2, .02, .02, .02, .02);
            if (portal.tryToCreatePortal(level, item.blockPosition(), item.getItem(), player)) {
                if (item.getItem().isEmpty()) item.discard();
                else item.setItem(item.getItem().copy());
            }
        }
    }
}
