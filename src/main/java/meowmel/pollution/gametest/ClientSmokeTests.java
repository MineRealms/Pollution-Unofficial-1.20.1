package meowmel.pollution.gametest;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.PollutionClientState;
import meowmel.pollution.client.screen.MineralExtractorScreen;
import meowmel.pollution.dimension.PollutionDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;

/** Opt-in local development check. Never runs in a distributed build or on a remote server. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class ClientSmokeTests {
    public static Object jeiRuntime;
    private static int ticks, phase, at;
    private static boolean finished;
    private static boolean oldEnabled;
    private static final BlockPos EXTRACTOR = new BlockPos(200, -60, 0);
    private ClientSmokeTests() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (FMLEnvironment.production || !Boolean.getBoolean("pollution.clientSmokeTest") || finished
                || event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (++ticks == 1) {
            GLFW.glfwSetWindowPos(mc.getWindow().getWindow(), -10000, -10000);
            mc.options.pauseOnLostFocus = false;
            mc.options.framerateLimit().set(30);
            mc.options.guiScale().set(2);
        }
        try {
            check(ticks < 12000, "client smoke check timed out at phase " + phase);
            if (ticks % 200 == 0) Pollution.LOGGER.info("[ClientSmoke] phase={}, screen={}, player={}, JEI={}", phase,
                    mc.screen == null ? "none" : mc.screen.getClass().getName(), mc.player != null, jeiRuntime != null);
            if (mc.screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen) {
                shot("00-connection-failure.png");
                throw new IllegalStateException("Disconnected: " + mc.screen.getNarrationMessage().getString());
            }
            if (mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen death) {
                mc.player.respawn();
                at = ticks;
                phase = 0;
                return;
            }
            if (mc.player == null || mc.level == null || !mc.player.hasPermissions(2) || jeiRuntime == null) return;
            check(mc.getCurrentServer() != null && mc.getCurrentServer().ip.equals("127.0.0.1:25599"), "local test server required");
            if (ticks - at < 80) return;
            switch (phase) {
                case 0 -> {
                    command("gamemode creative");
                    command("fill 196 -61 -4 204 -61 5 minecraft:quartz_block");
                    command("tp @s 200.5 -60 3.5 facing 200.5 -59 0.5");
                    command("setblock 200 -60 0 pollution:mineral_extractor");
                    command("item replace entity @s armor.head with pollution:nano_goggles");
                    command("item replace entity @s armor.chest with pollution:quantum_wings");
                    command("item replace entity @s hotbar.0 with pollution:mineral_extractor");
                    command("time set 6000");
                    command("weather clear");
                    command("pollution set " + Math.max(1, PollutionClientState.threshold() * 1.5));
                    next();
                }
                case 1 -> {
                    check(PollutionClientState.isCurrent() && PollutionClientState.amount() > PollutionClientState.threshold(),
                            "server pollution did not reach the client HUD");
                    for (String stone : meowmel.pollution.common.block.PollutionStoneBlocks.STONES.keySet()) {
                        var model = mc.getModelManager().getModel(new net.minecraft.client.resources.model.ModelResourceLocation(
                                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, stone), "inventory"));
                        check(model != mc.getModelManager().getMissingModel(), "missing stone item model: " + stone);
                    }
                    shot("01-pollution-hud.png");
                    mc.player.getInventory().selected = 0;
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(EXTRACTOR), Direction.SOUTH, EXTRACTOR, false));
                    next();
                }
                case 2 -> {
                    check(mc.screen instanceof MineralExtractorScreen, "extractor interaction did not open its screen");
                    var screen = (MineralExtractorScreen) mc.screen;
                    oldEnabled = screen.getMenu().isEnabled();
                    shot("02-mineral-extractor.png");
                    mc.gameMode.handleInventoryButtonClick(screen.getMenu().containerId, 1);
                    next();
                }
                case 3 -> {
                    check(mc.screen instanceof MineralExtractorScreen screen && screen.getMenu().isEnabled() != oldEnabled,
                            "extractor power button did not round-trip through the server");
                    mc.player.closeContainer();
                    mc.setScreen(new InventoryScreen(mc.player));
                    next();
                }
                case 4 -> {
                    shot("03-armor-and-item.png");
                    var runtime = (mezz.jei.api.runtime.IJeiRuntime) jeiRuntime;
                    var type = runtime.getRecipeManager().getRecipeType(ResourceLocation.fromNamespaceAndPath("gtceu", "macerator"))
                            .orElseThrow(() -> new IllegalStateException("JEI has no GT macerator category"));
                    runtime.getRecipesGui().showTypes(java.util.List.of(type));
                    next();
                }
                case 5 -> {
                    check(mc.screen != null && mc.screen.getClass().getName().startsWith("mezz.jei."), "JEI recipe screen did not open");
                    shot("04-jei-gt-recipes.png");
                    mc.setScreen(null);
                    command("execute in pollution:alfheim run tp @s 1000 200 1000 0 -30");
                    next();
                }
                case 6 -> {
                    check(mc.level.dimension().equals(PollutionDimensions.ALFHEIM), "Alfheim teleport failed");
                    check(mc.level.effects().getCloudHeight() == 164, "Alfheim effects were not registered");
                    shot("05-alfheim-sky.png");
                    Pollution.LOGGER.info("[ClientSmoke] PASS: HUD network, stone models, extractor menu/button, armor, JEI, Alfheim sky");
                    finished = true;
                    java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("client-smoke-result.txt"), "PASS\n");
                    mc.stop();
                }
                default -> throw new IllegalStateException("unknown test phase");
            }
        } catch (Throwable failure) {
            finished = true;
            Pollution.LOGGER.error("[ClientSmoke] FAIL at phase " + phase, failure);
            try { java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("client-smoke-result.txt"), "FAIL: " + failure); }
            catch (java.io.IOException ignored) {}
            mc.stop();
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static void command(String command) { Minecraft.getInstance().player.connection.sendUnsignedCommand(command); }
    private static void next() { phase++; at = ticks; }
    private static void shot(String filename) {
        var mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, filename, mc.getMainRenderTarget(), message -> Pollution.LOGGER.info("[ClientSmoke] {}", message.getString()));
    }
}
