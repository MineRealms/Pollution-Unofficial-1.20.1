package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

/**
 * Upstream {@code ThaumcraftRecipes} tail: the eight wire-coil conversions and
 * the thaumium maceration.
 *
 * <ul>
 *   <li>Coil conversions: upstream {@code POCoilBlock.COIL_LEVEL_1..8} arc-furnaced
 *       with 1440 mB InfusedExchange into the matching GTCEu coil block.</li>
 *   <li>Thaumium ingot maceration: {@code forge:ingots/thaumium} tag -&gt;
 *       stainless steel dust (the port's Thaumium substitute).</li>
 * </ul>
 */
public final class CoilRecipes {

    private static final int COIL_EUT = 120;
    private static final int COIL_DURATION = 600;
    private static final int COIL_EXCHANGE = 1440;

    private CoilRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        coil(provider, "cupronickel", PollutionMagicBlocks.WIRE_COIL_CUPRONICKEL.asStack(),
                SafeItems.gt("cupronickel_coil_block", 1));
        coil(provider, "kanthal", PollutionMagicBlocks.WIRE_COIL_KANTHAL.asStack(),
                SafeItems.gt("kanthal_coil_block", 1));
        coil(provider, "nichrome", PollutionMagicBlocks.WIRE_COIL_NICHROME.asStack(),
                SafeItems.gt("nichrome_coil_block", 1));
        coil(provider, "rtm_alloy", PollutionMagicBlocks.WIRE_COIL_RTM_ALLOY.asStack(),
                SafeItems.gt("rtm_alloy_coil_block", 1));
        coil(provider, "hssg", PollutionMagicBlocks.WIRE_COIL_HSSG.asStack(),
                SafeItems.gt("hssg_coil_block", 1));
        coil(provider, "naquadah", PollutionMagicBlocks.WIRE_COIL_NAQUADAH.asStack(),
                SafeItems.gt("naquadah_coil_block", 1));
        coil(provider, "trinium", PollutionMagicBlocks.WIRE_COIL_TRINIUM.asStack(),
                SafeItems.gt("trinium_coil_block", 1));
        coil(provider, "tritanium", PollutionMagicBlocks.WIRE_COIL_TRITANIUM.asStack(),
                SafeItems.gt("tritanium_coil_block", 1));

        // GTNN already provides botania:manasteel_ingot -> GTNN ManaSteel dust
        // (gtceu:macerator/macerate_manasteel_ingot); the duplicate previously
        // registered here was rejected by the GT recipe lookup DB, so it is removed.

        TagKey<Item> thaumiumIngot = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("forge", "ingots/thaumium"));
        GTRecipeBuilder.of(id("thaumium_dust"), GTRecipeTypes.MACERATOR_RECIPES)
                .inputItems(thaumiumIngot)
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.StainlessSteel, 1))
                .duration(10)
                .EUt(2)
                .save(provider);
    }

    private static void coil(Consumer<FinishedRecipe> provider, String name,
                             net.minecraft.world.item.ItemStack input,
                             net.minecraft.world.item.ItemStack output) {
        if (input.isEmpty() || output.isEmpty()) {
            Pollution.LOGGER.warn("Skipping coil/{}: input or output item is missing", name);
            return;
        }
        GTRecipeBuilder.of(id("coil_" + name), GTRecipeTypes.ARC_FURNACE_RECIPES)
                .inputItems(input)
                .inputFluids(PollutionMaterials.InfusedExchange.getFluid(COIL_EXCHANGE))
                .outputItems(output)
                .duration(COIL_DURATION)
                .EUt(COIL_EUT)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "coil/" + path);
    }
}
