package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTBlocks;
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
 * <p><b>Skipped (10 recipes)</b></p>
 * <ul>
 *   <li>Constellation anchor and obelisk core: TC4R does expose a datapack
 *       infusion serializer usable from code (see
 *       {@code docs/TC4R_INFUSION_API.md}), but both recipes need the unported
 *       constellation/ritual crystal block and the obelisk core block. The four
 *       Botania-style machine infusions (pure daisy, mana infusion, rune altar,
 *       petal apothecary) also stay skipped because they need the unported
 *       Thaumcraft vis/morphic resonators.</li>
 *   <li>Starstream nexus controller: needs the unported
 *       {@code STARSTREAM_NEXUS_OBELISK} machine, the obelisk core block,
 *       liquid starlight and DimensionalTransformingAgent.</li>
 *   <li>Linker and chunk anchor: liquid starlight (Astral Sorcery) is
 *       unported.</li>
 *   <li>Relay, interdimensional relay and operation core: the
 *       {@code STARSTREAM_RELAY} / {@code STARSTREAM_INTERDIMENSIONAL_RELAY} /
 *       {@code STARSTREAM_CHUNK_ANCHOR} / {@code STARSTREAM_OPERATION_CORE}
 *       blocks are unported.</li>
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
