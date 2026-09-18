package meowmel.pollution.common;

import com.tterrag.registrate.util.entry.RegistryEntry;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative inventory entry point for everything this addon registers through
 * its GregTech registrate (plain items, casings, plants and machine items).
 */
public final class PollutionCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Pollution.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pollution.main"))
            .icon(() -> new ItemStack(PollutionItems.VIS_CHECKER.get()))
            .displayItems((parameters, output) -> {
                for (RegistryEntry<Item> entry : PollutionGTAddon.REGISTRATE.getAll(Registries.ITEM)) {
                    output.accept(entry.get());
                }
            })
            .build());

    private PollutionCreativeTabs() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        TABS.register(context.getModEventBus());
    }
}
