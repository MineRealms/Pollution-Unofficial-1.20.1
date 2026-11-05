package meowmel.pollution.dimension;

import meowmel.pollution.Pollution;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The underground portal ritual.
 *
 * <p>Two rituals in this pack use the same "2x2 water pool + dropped item" shape,
 * so they must not share a catalyst: Twilight Forest keeps its own activator
 * (a datapack override of {@code twilightforest:portal/activator}), and this one
 * only reacts to a Thaumcraft thaumium ingot. The decoration requirement is
 * likewise narrowed to Botania's Loonium in {@link
 * meowmel.pollution.common.block.tile.PortalBlock}, so a vanilla flower ring
 * can never produce an underground portal.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
public final class PortalFormationEvents {
    private PortalFormationEvents() {}

    /** Thaumium ingot, resolved by id so this mod does not need a hard TC4R dependency. */
    private static final ResourceLocation CATALYST_ID = new ResourceLocation("thaumcraft", "thaumium_ingot");

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
        // Missing mod -> AIR. A non-empty stack never is(AIR), so the ritual simply stays dormant.
        var catalyst = BuiltInRegistries.ITEM.get(CATALYST_ID);
        var portal = PollutionMiscBlocks.PORTAL.get();
        for (var item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32),
                item -> item.isAlive() && item.getItem().is(catalyst))) {
            if (!portal.canFormPortal(level.getBlockState(item.blockPosition()))) continue;
            level.sendParticles(ParticleTypes.WITCH, item.getX(), item.getY() + .2, item.getZ(), 2, .02, .02, .02, .02);
            if (portal.tryToCreatePortal(level, item.blockPosition(), item.getItem(), player)) {
                if (item.getItem().isEmpty()) item.discard();
                else item.setItem(item.getItem().copy());
            }
        }
    }
}
