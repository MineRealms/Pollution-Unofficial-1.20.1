package meowmel.pollution.common.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import meowmel.pollution.api.amplification.AstralAmplifierSnapshot;
import meowmel.pollution.api.amplification.MagicAmplificationEngine;
import meowmel.pollution.api.amplification.MagicAmplificationResult;
import meowmel.pollution.api.amplification.MagicJeiHintResolver;
import meowmel.pollution.api.astral.AstralNbtHelper;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** The upstream live constellation panel, hosted in a GT page with server-owned values. */
public final class AstralConstellationPage implements IFancyUIProvider {
    private final MagicMultiblockController machine;

    public AstralConstellationPage(MagicMultiblockController machine) { this.machine = machine; }

    @Override
    public Component getTitle() { return Component.translatable("pollution.ui.astral.title"); }

    @Override
    public boolean hasPlayerInventory() { return false; }

    @Override
    public IGuiTexture getTabIcon() { return new ItemStackTexture(PollutionItems.CONSTELLATION_DATA_WAFER.asStack()); }

    @Override
    public Widget createMainPage(FancyMachineUIWidget parent) {
        return new WidgetGroup(0, 0, 190, 184).addWidget(new Panel(machine)).setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    private static final class Panel extends Widget {
        private final MagicMultiblockController machine;
        private CompoundTag snapshot = new CompoundTag();

        private Panel(MagicMultiblockController machine) { super(4, 4, 182, 176); this.machine = machine; }

        private CompoundTag sample() {
            var lens = AstralAmplifierSnapshot.from(machine.getAstralLensHatch());
            var logic = machine.getRecipeLogic();
            boolean running = logic.isActive();
            MagicAmplificationResult effects = running && logic instanceof MagicRecipeLogic magic
                    ? magic.getActiveAmplification()
                    : MagicAmplificationEngine.calculate(machine.getMagicProcessTags(null), 200, lens,
                            machine.getTarotHatch(), 0, true);
            CompoundTag tag = effects.serializeSnapshot();
            tag.putString("Focus", lens.getConstellation());
            tag.putBoolean("Wafer", lens.hasDataWafer());
            tag.putBoolean("Sky", lens.isSkyMatched());
            tag.putBoolean("Running", running);
            tag.putInt("Base", percent(lens.getBaseStrength() - lens.getOpticalCrystalStrengthBonus()));
            tag.putInt("Optics", percent(lens.getOpticalCrystalStrengthBonus()));
            tag.putInt("Quality", lens.getOpticalCrystalQuality());
            tag.putString("Card", machine.getTarotHatch() == null ? "" : machine.getTarotHatch().getActiveTarot());
            return tag;
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            snapshot = sample();
            buffer.writeNbt(snapshot);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            CompoundTag data = buffer.readNbt();
            snapshot = data == null ? new CompoundTag() : data;
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            CompoundTag current = sample();
            if (!current.equals(snapshot)) {
                snapshot = current;
                writeUpdateInfo(1, buffer -> buffer.writeNbt(snapshot));
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 1) {
                CompoundTag data = buffer.readNbt();
                snapshot = data == null ? new CompoundTag() : data;
            } else super.readUpdateInfo(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int x = getPosition().x, y = getPosition().y;
            graphics.fill(x, y, x + 182, y + 176, 0xFF0A1522);
            var font = Minecraft.getInstance().font;
            graphics.drawString(font, Component.translatable("pollution.ui.astral.title"), x + 6, y + 5, 0x8FDFFF, false);
            if (!snapshot.getBoolean("Wafer")) {
                int lineY = y + 28;
                for (var line : font.split(Component.translatable("pollution.ui.astral.no_wafer"), 166)) {
                    graphics.drawString(font, line, x + 6, lineY, 0xB5BEC8, false);
                    lineY += 11;
                }
                return;
            }
            var constellation = AstralNbtHelper.findConstellation(snapshot.getString("Focus"));
            if (constellation != null) {
                graphics.drawString(font, constellation.getConstellationName(), x + 88, y + 24, 0x8FDFFF, false);
                var stars = constellation.getStars();
                int max = stars.stream().mapToInt(star -> Math.max(star.x, star.y)).max().orElse(32);
                double scale = 64.0 / Math.max(1, max);
                for (var connection : constellation.getStarConnections()) {
                    line(graphics, x + 8 + (int)(connection.from.x * scale), y + 24 + (int)(connection.from.y * scale),
                            x + 8 + (int)(connection.to.x * scale), y + 24 + (int)(connection.to.y * scale),
                            snapshot.getBoolean("Sky") ? 0xFF7EEFFF : 0xFF617C92);
                }
                for (var star : stars) {
                    int sx = x + 8 + (int)(star.x * scale), sy = y + 24 + (int)(star.y * scale);
                    graphics.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFFE7F7FF);
                }
            }
            graphics.drawString(font, Component.translatable("pollution.ui.astral.base", snapshot.getInt("Base")), x + 88, y + 40, 0xCFDDE8, false);
            graphics.drawString(font, Component.translatable("pollution.ui.astral.sky",
                    Component.translatable(snapshot.getBoolean("Sky") ? "gui.yes" : "gui.no")), x + 88, y + 54, 0xCFDDE8, false);
            var effects = MagicAmplificationResult.deserializeSnapshot(snapshot);
            java.util.List<Component> lines = java.util.List.of(
                    Component.translatable("pollution.ui.astral.optics", snapshot.getInt("Optics"), snapshot.getInt("Quality")),
                    Component.translatable("pollution.machine.tarot_hatch.active", MagicJeiHintResolver.tarotDisplayName(snapshot.getString("Card"))),
                    Component.translatable(snapshot.getBoolean("Running") ? "pollution.ui.astral.running" : "pollution.ui.astral.preview"),
                    Component.translatable("pollution.ui.astral.speed", percent(effects.getDurationReduction()), percent(effects.getEutReduction())),
                    Component.translatable("pollution.ui.astral.magic", percent(effects.getMagicCostReduction()), effects.getExtraParallel()),
                    Component.translatable("pollution.ui.astral.output", percent(effects.getOutputBonus()), percent(effects.getChanceExtraRoll())),
                    Component.translatable("pollution.ui.astral.catalyst", percent(effects.getCatalystSaveChance()), effects.getFurnaceTemperatureBonus()));
            int lineY = y + 95;
            for (Component line : lines) {
                // Scale long localized lines instead of drawing across the panel edge.
                float scale = Math.min(1.0F, 168.0F / Math.max(1, font.width(line)));
                graphics.pose().pushPose();
                graphics.pose().translate(x + 6, lineY, 0);
                graphics.pose().scale(scale, scale, 1);
                graphics.drawString(font, line, 0, 0, 0xCFDDE8, false);
                graphics.pose().popPose();
                lineY += 11;
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void line(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
            int dx = Math.abs(x1-x0), dy = Math.abs(y1-y0), sx = x0<x1 ? 1:-1, sy = y0<y1 ? 1:-1, error = dx-dy;
            while (true) {
                graphics.fill(x0, y0, x0+1, y0+1, color);
                if (x0==x1 && y0==y1) break;
                int twice = error*2;
                if (twice > -dy) { error -= dy; x0 += sx; }
                if (twice < dx) { error += dx; y0 += sy; }
            }
        }

        private static int percent(double value) { return (int)Math.round(value * 100); }
    }
}
