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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * Starstream obelisk structural blocks, port of the portable subset of
 * upstream {@code meowmel.pollution.loaders.recipes.StarstreamNexusRecipes}
 * (2 of 12 recipes).
 *
 * <p><b>Ported</b>: the two structural casings. The Astral Sorcery marble
 * bricks are replaced by GTCEu marble (Astral Sorcery is not a dependency of
 * the port), the {@code ASTRAL_RESONANCE_COIL} input is replaced by the
 * obtainable {@code MANA_RESONANCE_COIL}, and Starrymansus maps to InfusedAura
 * per the substitution table.</p>
 *
 * <p><b>Newly ported</b></p>
 * <ul>
 *   <li>Starstream linker. // 上游: liquid starlight -> 本移植版: InfusedAura；
 *       ASTRAL_LENS_BASIC -> 本移植版 SILVERED_GLASS_LENS；
 *       ASTRAL_RESONANCE_COIL -> 本移植版 MANA_RESONANCE_COIL
 *       （整合包无 Astral Sorcery）。</li>
 *   <li>Constellation anchor infusion: registered by {@code InfusionRecipes}
 *       (see there for the ritual-crystal and resonance-coil substitutions).</li>
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
 *       that remain (anchor, linker) substitute InfusedAura plus
 *       {@code MANA_RESONANCE_COIL} / {@code SILVERED_GLASS_LENS} for the
 *       astral items, the rest of the Astral chain stays skipped.
 *       // 跳过: 整合包无 Astral Sorcery</li>
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

        // 星轨外壳（上游 Astral Sorcery 大理石砖 -> GTCEu 大理石；
        // 上游: ASTRAL_RESONANCE_COIL x4 -> 本移植版: MANA_RESONANCE_COIL x4，
        // 数量不变：整合包无 Astral Sorcery，魔力谐振线圈同档可量产）
        GTRecipeBuilder.of(id("starstream_casing"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(GTBlocks.MARBLE.asStack(8))
                .inputItems(PollutionItems.STARRY_RUNE.asStack(2))
                .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack(4))
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

        // 星轨链接器 // 上游: liquid starlight -> 本移植版: InfusedAura；
        // 上游: ASTRAL_LENS_BASIC -> 本移植版: SILVERED_GLASS_LENS；
        // 上游: ASTRAL_RESONANCE_COIL x2 -> 本移植版: MANA_RESONANCE_COIL x2
        // （整合包无 Astral Sorcery）
        ItemStack emitter = SafeItems.gt("iv_emitter", 2);
        ItemStack sensor = SafeItems.gt("iv_sensor", 2);
        if (emitter.isEmpty() || sensor.isEmpty()) {
            Pollution.LOGGER.warn("Skipping starstream_linker: a required GT item is missing");
            return;
        }
        GTRecipeBuilder.of(id("starstream_linker"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack())
                .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack(2))
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_IV.asStack(2))
                .inputItems(emitter)
                .inputItems(sensor)
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
