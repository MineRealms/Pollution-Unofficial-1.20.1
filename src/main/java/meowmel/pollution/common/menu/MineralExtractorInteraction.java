package meowmel.pollution.common.menu;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.tile.MineralExtractorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

/**
 * Opens the mineral extractor screen on right-click.
 *
 * <p>{@code MineralExtractorBlock.use} still carries the pre-GUI stopgap
 * (right-click toggles the machine, sneak-right-click cycles the mode) because
 * the block file is owned by the server-logic batch. Until that call is
 * replaced with {@code NetworkHooks.openScreen}, this Forge-bus handler makes
 * the ported screen reachable without touching the block: a normal
 * right-click opens the menu server-side and denies the block's own use, while
 * sneak-right-click is left alone for the existing mode fallback.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MineralExtractorInteraction {

    private MineralExtractorInteraction() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        if (player.isShiftKeyDown()) {
            return;
        }
        if (!level.getBlockState(event.getPos()).is(PollutionMiscBlocks.MINERAL_EXTRACTOR.get())) {
            return;
        }
        if (!(level.getBlockEntity(event.getPos()) instanceof MineralExtractorBlockEntity extractor)) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) ->
                            new MineralExtractorMenu(containerId, inventory, extractor),
                    Component.literal("矿物提取器")), event.getPos());
        }
        event.setUseBlock(Event.Result.DENY);
    }
}
