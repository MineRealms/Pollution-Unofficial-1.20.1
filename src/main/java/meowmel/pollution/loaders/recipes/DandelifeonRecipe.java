package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

/**
 * Dandelifeon generator fuel, port of upstream {@code DandelifeonRecipe}.
 *
 * <p>Upstream consumed {@code ModItems.manaResource} meta 8 and used
 * {@code ItemBlockSpecialFlower.ofType("dandelifeon")} as the non-consumable
 * catalyst. Metadata order verified against the 1.12-final
 * {@code ItemManaResource} ore-dictionary registration: meta 8 is pixie dust,
 * i.e. {@link BotaniaItems#pixieDust}; the flower block is
 * {@link BotaniaFlowerBlocks#dandelifeon}. The numbers (20 ticks, EUt 999) are
 * unchanged.</p>
 */
public final class DandelifeonRecipe {

    private DandelifeonRecipe() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("dandelifeon"), BotaniaRecipeMaps.DAN_DE_LIFE_ON)
                .inputItems(new ItemStack(BotaniaItems.pixieDust))
                .notConsumable(new ItemStack(BotaniaFlowerBlocks.dandelifeon))
                .duration(20)
                .EUt(999)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "dandelifeon/" + path);
    }
}
