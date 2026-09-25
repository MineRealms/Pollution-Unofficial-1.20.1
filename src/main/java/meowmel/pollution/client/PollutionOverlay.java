package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Compact local pollution indicator; the server remains authoritative. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PollutionOverlay {

    private PollutionOverlay() {}

    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pollution", (gui, graphics, partialTick, width, height) -> render(graphics, width));
    }

    private static void render(GuiGraphics graphics, int width) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || !PollutionClientState.isCurrent()
                || PollutionClientState.amount() <= PollutionClientState.threshold()) {
            return;
        }
        int y = 4;
        double ratio = PollutionClientState.ratio();
        int color = ratio >= 4.0D ? 0xFFFF4444 : ratio >= 2.0D ? 0xFFFFAA33 : 0xFFAA66FF;
        Component text = Component.translatable("pollution.hud.exposure",
                Double.isFinite(ratio) ? String.format(java.util.Locale.ROOT, "%.1f", ratio) : "∞");
        int x = Math.max(4, width - Math.max(120, minecraft.font.width(text)) - 6);
        graphics.drawString(minecraft.font, text, x, y, color, true);
        int fill = Math.min(120, Math.max(2, (int) (120.0D * Math.min(1.0D, ratio / 4.0D))));
        graphics.fill(x, y + 11, x + 120, y + 14, 0x66000000);
        graphics.fill(x, y + 11, x + fill, y + 14, color);
    }
}
