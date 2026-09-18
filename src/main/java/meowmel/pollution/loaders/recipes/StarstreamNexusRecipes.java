package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * Starstream obelisk structural blocks, port of the portable subset of
 * upstream {@code meowmel.pollution.loaders.recipes.StarstreamNexusRecipes}
 * (2 of 12 recipes).
 *
 * <p><b>Ported</b>: the two structural casings. The Astral Sorcery marble
 * bricks are replaced by GTCEu marble (Astral Sorcery is not a dependency of
 * the port), and Starrymansus maps to InfusedAura per the substitution
 * table.</p>
 *
 * <p><b>Newly ported</b></p>
 * <ul>
 *   <li>Starstream linker. // 上游: liquid starlight -> 本移植版: InfusedAura
 *       （整合包无 Astral Sorcery）。ASTRAL_LENS_BASIC 物品存在（其自身来源链
 *       属 Astral Sorcery，仍跳过）。</li>
 *   <li>Constellation anchor infusion: registered by {@code InfusionRecipes}
 *       (see there for the ritual-crystal substitution).</li>
 * </ul>
 *
 * <p><b>Skipped</b></p>
 * <ul>
 *   <li>Obelisk core / nexus controller / relay / interdimensional relay /
 *       chunk anchor / operation core: the {@code CONSTELLATION_CRYSTAL},
 *       {@code STARSTREAM_RELAY}, {@code STARSTREAM_INTERDIMENSIONAL_RELAY},
 *       {@code STARSTREAM_CHUNK_ANCHOR}, {@code STARSTREAM_OPERATION_CORE}
 *       blocks and the {@code STARSTREAM_NEXUS_OBELISK} machine are unported.
 *       // 跳过: 星辉网络方块/机器未移植</li>
 *   <li>Liquid starlight inputs are Astral Sorcery content; the two recipes
 *       that remain (anchor, linker) substitute InfusedAura, the rest of the
 *       Astral chain stays skipped. // 跳过: 整合包无 Astral Sorcery</li>
 * </ul>
 */
public final class StarstreamNexusRecipes {

    private StarstreamNexusRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        FluidStack mana = fluid(PollutionMaterials.InfusedAura, 2000);
        if (mana == null) {
            Pollution.LOGGER.warn("Skipping starstream casings: InfusedAura has no fluid");
            return;
        }

        // 星轨外壳（上游 Astral Sorcery 大理石砖 -> GTCEu 大理石）
        GTRecipeBuilder.of(id("starstream_casing"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(GTBlocks.MARBLE.asStack(8))
                .inputItems(PollutionItems.STARRY_RUNE.asStack(2))
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack(4))
                .inputFluids(mana)
                .outputItems(PollutionMagicBlocks.STARSTREAM_CASING.asStack(8))
                .duration(600)
                .EUt(GTValues.VA[GTValues.UV])
                .save(provider);

        FluidStack runedMana = fluid(PollutionMaterials.InfusedAura, 4000);
        if (runedMana == null) {
            Pollution.LOGGER.warn("Skipping starstream_runed_casing: InfusedAura has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("starstream_runed_casing"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionMagicBlocks.STARSTREAM_CASING.asStack(4))
                .inputItems(PollutionItems.STARRY_RUNE.asStack(4))
                .inputItems(PollutionItems.HARMONIZING_RUNE_CORE.asStack(2))
                .inputFluids(runedMana)
                .outputItems(PollutionMagicBlocks.STARSTREAM_RUNED_CASING.asStack(4))
                .duration(900)
                .EUt(GTValues.VA[GTValues.UHV])
                .save(provider);

        // 星轨链接器 // 上游: liquid starlight -> 本移植版: InfusedAura
        GTRecipeBuilder.of(id("starstream_linker"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack(2))
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_IV.asStack(2))
                .inputItems(GTItems.EMITTER_IV.asStack(2))
                .inputItems(GTItems.SENSOR_IV.asStack(2))
                .inputFluids(fluid(PollutionMaterials.InfusedAura, 2000))
                .outputItems(PollutionItems.STARSTREAM_LINKER.asStack())
                .duration(600)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
    }

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "starstream/" + path);
    }
}
