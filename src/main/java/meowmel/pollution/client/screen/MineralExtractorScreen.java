package meowmel.pollution.client.screen;

import meowmel.pollution.common.menu.MineralExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * 1.20.1 port of the 1.12 {@code GuiMineralExtractor}.
 *
 * <p>Like the original, the whole window is drawn from coloured rectangles and
 * text (no GUI texture), so no new assets are required. The two fake-slot
 * buttons of the legacy container are drawn at the same coordinates and send
 * modern menu-button clicks to {@link MineralExtractorMenu#clickMenuButton}.</p>
 */
public class MineralExtractorScreen extends AbstractContainerScreen<MineralExtractorMenu> {

    private static final String[] MODE_NAMES = {"实体矿", "虚拟矿物", "虚拟流体"};

    // ===== colours (same palette as the 1.12 screen) =====
    private static final int C_BORDER = 0xFF373737;
    private static final int C_BG = 0xFFC6C6C6;
    private static final int C_TITLE = 0xFF555555;
    private static final int C_PANEL = 0xFFAAAAAA;
    private static final int C_LABEL = 0xFF333333;
    private static final int C_SLOT_FILL = 0xFF8B8B8B;
    private static final int C_CHAOS = 0xFFAA4455;
    private static final int C_MAGIC = 0xFF6644CC;
    private static final int C_GREEN = 0xFF55AA44;
    private static final int C_RED = 0xFFAA3333;
    private static final int C_ORANGE = 0xFFCC8844;
    private static final int C_BLUE = 0xFF5588CC;

    // ===== button coordinates, identical to the legacy container =====
    private static final int MODE_BTN_X = 132;
    private static final int MODE_BTN_Y = 100;
    private static final int POWER_BTN_X = 154;
    private static final int POWER_BTN_Y = 100;

    public MineralExtractorScreen(MineralExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 202;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // window background (dark frame + grey body)
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, C_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, C_BG);

        // title bar
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + 12, C_TITLE);
        guiGraphics.fill(x + 1, y + 12, x + this.imageWidth - 1, y + 13, 0xFF8B8B8B);
        guiGraphics.drawString(this.font, "矿物提取器", x + 8, y + 4, 0xFFFFFFFF, false);

        // machine panel
        guiGraphics.fill(x + 3, y + 15, x + this.imageWidth - 3, y + 118, C_PANEL);
        guiGraphics.fill(x + 3, y + 15, x + this.imageWidth - 3, y + 16, 0xFFD6D6D6);

        // essentia chambers
        drawSectionLabel(guiGraphics, x + 8, y + 19, "源质仓", C_CHAOS);
        drawAspectBar(guiGraphics, x + 10, y + 27, this.menu.getChaosAmount(), C_CHAOS, "混沌/熵");
        drawAspectBar(guiGraphics, x + 10, y + 47, this.menu.getMagicAmount(), C_MAGIC, "魔法");

        // slot frames (machine row, pending-ore display, player inventory)
        for (int i = 0; i < MineralExtractorMenu.MACHINE_SLOTS; i++) {
            drawSlot(guiGraphics, x + MineralExtractorMenu.GRID_X + i * 18,
                    y + MineralExtractorMenu.MACHINE_Y);
        }
        drawSlot(guiGraphics, x + MineralExtractorMenu.DISPLAY_X, y + MineralExtractorMenu.DISPLAY_Y);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(guiGraphics, x + MineralExtractorMenu.GRID_X + col * 18,
                        y + MineralExtractorMenu.PLAYER_ROW_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(guiGraphics, x + MineralExtractorMenu.GRID_X + col * 18,
                    y + MineralExtractorMenu.HOTBAR_Y);
        }

        drawStatusLine(guiGraphics, x, y);

        // mode / power buttons
        int mode = this.menu.getMode() % 3;
        drawButton(guiGraphics, x + MODE_BTN_X, y + MODE_BTN_Y, modeColor(mode));
        drawModeIcon(guiGraphics, x + MODE_BTN_X, y + MODE_BTN_Y);
        boolean running = this.menu.isEnabled();
        drawButton(guiGraphics, x + POWER_BTN_X, y + POWER_BTN_Y, running ? C_GREEN : C_RED);
        drawPowerIcon(guiGraphics, x + POWER_BTN_X, y + POWER_BTN_Y, running);

        // section separator between panel and inventory
        guiGraphics.fill(x + 4, y + 120, x + this.imageWidth - 4, y + 121, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Every label is drawn in renderBg so it can sit on top of the custom panels.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (inButton(mouseX, mouseY, MODE_BTN_X, MODE_BTN_Y)) {
            guiGraphics.renderTooltip(this.font, List.of(
                    Component.literal("模式切换"),
                    Component.literal("当前: " + MODE_NAMES[this.menu.getMode() % 3]),
                    Component.literal("点击切换 (实体矿/虚拟矿物/虚拟流体)")),
                    Optional.empty(), mouseX, mouseY);
        }
        if (inButton(mouseX, mouseY, POWER_BTN_X, POWER_BTN_Y)) {
            guiGraphics.renderTooltip(this.font, List.of(
                    Component.literal(this.menu.isEnabled() ? "正在运行" : "已停止"),
                    Component.literal("点击切换运行状态")),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inButton(mouseX, mouseY, MODE_BTN_X, MODE_BTN_Y)) {
                clickMenuButton(MineralExtractorMenu.BUTTON_MODE);
                return true;
            }
            if (inButton(mouseX, mouseY, POWER_BTN_X, POWER_BTN_Y)) {
                clickMenuButton(MineralExtractorMenu.BUTTON_POWER);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clickMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private boolean inButton(double mouseX, double mouseY, int buttonX, int buttonY) {
        int x = this.leftPos + buttonX;
        int y = this.topPos + buttonY;
        return mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
    }

    // ////////////////////////////////////
    // ***** drawing helpers *****//
    // ////////////////////////////////////

    private void drawSectionLabel(GuiGraphics guiGraphics, int x, int y, String text, int dotColor) {
        guiGraphics.fill(x, y + 3, x + 3, y + 6, dotColor);
        guiGraphics.drawString(this.font, text, x + 7, y, C_LABEL, false);
    }

    private void drawStatusLine(GuiGraphics guiGraphics, int x, int y) {
        int mode = this.menu.getMode() % 3;
        if (mode == 1) {
            guiGraphics.drawString(this.font, "虚拟矿物: 产草方块", x + 8, y + 100, C_LABEL, false);
        } else if (mode == 2) {
            guiGraphics.drawString(this.font, "虚拟流体: 产石头", x + 8, y + 100, C_LABEL, false);
        } else {
            ItemStack ore = this.menu.getPendingOre();
            if (!ore.isEmpty()) {
                guiGraphics.drawString(this.font, "当前检测到:", x + 8, y + 100, C_LABEL, false);
                guiGraphics.renderItem(ore, x + 10, y + 103);
                guiGraphics.drawString(this.font, ore.getHoverName().getString(), x + 28, y + 106,
                        0xFF202020, false);
            } else {
                guiGraphics.drawString(this.font, "当前检测到: 无", x + 8, y + 100, C_LABEL, false);
            }
        }
    }

    private int modeColor(int mode) {
        return switch (mode) {
            case 0 -> C_ORANGE;
            case 1 -> C_GREEN;
            default -> C_BLUE;
        };
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, C_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, C_SLOT_FILL);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 2, 0xFFD0D0D0);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 17, 0xFFB0B0B0);
        guiGraphics.fill(x + 16, y + 16, x + 17, y + 17, 0xFF5A5A5A);
    }

    private void drawButton(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF2B2B2B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, color);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 2, 0x55FFFFFF);
        guiGraphics.fill(x + 16, y + 16, x + 17, y + 17, 0x55000000);
    }

    private void drawModeIcon(GuiGraphics guiGraphics, int x, int y) {
        int color = 0xFFFFFFFF;
        guiGraphics.fill(x + 4, y + 5, x + 14, y + 6, color);
        guiGraphics.fill(x + 4, y + 8, x + 14, y + 9, color);
        guiGraphics.fill(x + 4, y + 11, x + 14, y + 12, color);
    }

    private void drawPowerIcon(GuiGraphics guiGraphics, int x, int y, boolean running) {
        int color = 0xFFFFFFFF;
        if (running) {
            guiGraphics.fill(x + 6, y + 5, x + 7, y + 13, color);
            guiGraphics.fill(x + 7, y + 6, x + 8, y + 12, color);
            guiGraphics.fill(x + 8, y + 7, x + 10, y + 11, color);
            guiGraphics.fill(x + 10, y + 8, x + 12, y + 10, color);
        } else {
            guiGraphics.fill(x + 6, y + 6, x + 12, y + 12, color);
        }
    }

    private void drawAspectBar(GuiGraphics guiGraphics, int x, int y, int amount, int color, String label) {
        int max = MineralExtractorMenu.getMaxStock();
        int barWidth = 150;
        guiGraphics.drawString(this.font, label, x, y, C_LABEL, false);
        guiGraphics.fill(x, y + 8, x + barWidth, y + 16, 0xFF2B2B2B);
        guiGraphics.fill(x + 1, y + 9, x + barWidth - 1, y + 15, 0xFF000000);
        int filled = (int) ((barWidth - 2) * Math.min(1.0F, (float) amount / Math.max(1, max)));
        if (filled > 0) {
            guiGraphics.fill(x + 1, y + 9, x + 1 + filled, y + 15, color);
        }
        guiGraphics.fill(x + 1, y + 9, x + barWidth - 1, y + 10, 0x55FFFFFF);
        String amountText = amount + "/" + max;
        guiGraphics.drawString(this.font, amountText,
                x + barWidth - this.font.width(amountText) - 2, y + 8, 0xFFFFFFFF, false);
    }
}
