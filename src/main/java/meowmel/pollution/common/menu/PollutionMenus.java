package meowmel.pollution.common.menu;

import meowmel.pollution.Pollution;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Common-side menu type registration for the ported screens.
 *
 * <p>{@code MenuType} is a common registry, so this lives outside the client
 * package and is safe on a dedicated server. The {@code @Mod.EventBusSubscriber}
 * annotation makes Forge load the class during automatic subscriber injection
 * (after the Pollution mod constructor ran), so the static block can attach the
 * {@link DeferredRegister} to the mod event bus captured by the shared GT
 * registrate. The screen itself is registered client-side in
 * {@code meowmel.pollution.client.PollutionMenuScreens}.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PollutionMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Pollution.MOD_ID);

    public static final RegistryObject<MenuType<MineralExtractorMenu>> MINERAL_EXTRACTOR =
            MENUS.register("mineral_extractor", () -> IForgeMenuType.create(MineralExtractorMenu::new));

    static {
        MENUS.register(PollutionGTAddon.REGISTRATE.getModEventBus());
    }

    private PollutionMenus() {}
}
