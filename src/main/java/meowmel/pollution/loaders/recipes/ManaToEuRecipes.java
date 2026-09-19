package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Mana-to-EU fuel recipes, port of upstream {@code ManaToEuRecipes}.
 *
 * <p>Upstream registered seven fuels on {@code MANA_TO_EU}, all at
 * {@code EUt(8192)} (the port emits the generator-style negative EUt), with the
 * duration computed from the (removed) {@code POConfig.MachineSettingSwitch}
 * defaults: {@code (int)(100 * EuPerMb / 8192)} for 100 mB of each mana fluid.</p>
 *
 * <p><b>Material substitutions:</b> {@code GTQTMaterials.Mana} maps to
 * {@link PollutionMaterials#InfusedAura}, the mapping already used by
 * {@code AERecipes} and {@code NodeFusionRecipes}. The other six upstream mana
 * fluids (Impuremana, WhiteMansus, BlackMansus, Starrymansus, RichAura,
 * ErichAura) are real port materials now, so all 7/7 upstream fuels are
 * registered.</p>
 *
 * <p><b>Config inlining:</b> the upstream natural-mana default was
 * 8192 EU per mB, giving {@code (int)(100 * 8192 / 8192) = 100} ticks, and the
 * Impuremana/WhiteMansus default 256 EU per mB gives 3 ticks. The remaining
 * defaults were 512/1024/2048/32768 EU per mB for
 * BlackMansus/Starrymansus/RichAura/ErichAura (6/12/25/400 ticks).</p>
 */
public final class ManaToEuRecipes {

    /** Upstream default burn time of 100 mB natural mana at 8192 EU/t. */
    private static final int MANA_FUEL_DURATION = 100;
    /** EuPerMbKqMagicRub = 256 EU/mB -> {@code (int)(100 * 256 / 8192)} = 3 ticks. */
    private static final int IMPURE_MANA_FUEL_DURATION = 3;
    /** EuPerMbKqMagicGas = 256 EU/mB -> {@code (int)(100 * 256 / 8192)} = 3 ticks. */
    private static final int WHITE_MANSUS_FUEL_DURATION = 3;
    /** EuPerMbKqMagicFas = 512 EU/mB -> {@code (int)(100 * 512 / 8192)} = 6 ticks. */
    private static final int BLACK_MANSUS_FUEL_DURATION = 6;
    /** EuPerMbKqMagicDas = 1024 EU/mB -> {@code (int)(100 * 1024 / 8192)} = 12 ticks. */
    private static final int STARRY_MANSUS_FUEL_DURATION = 12;
    /** EuPerMbKqMagicAas = 2048 EU/mB -> {@code (int)(100 * 2048 / 8192)} = 25 ticks. */
    private static final int RICH_AURA_FUEL_DURATION = 25;
    /** EuPerMbRichMagicKq = 32768 EU/mB -> {@code (int)(100 * 32768 / 8192)} = 400 ticks. */
    private static final int ERICH_AURA_FUEL_DURATION = 400;

    private ManaToEuRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("infused_aura"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .duration(MANA_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("impuremana"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.Impuremana.getFluid(100))
                .duration(IMPURE_MANA_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("whitemansus"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.WhiteMansus.getFluid(100))
                .duration(WHITE_MANSUS_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("blackmansus"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.BlackMansus.getFluid(100))
                .duration(BLACK_MANSUS_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("starrymansus"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.Starrymansus.getFluid(100))
                .duration(STARRY_MANSUS_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("rich_aura"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.RichAura.getFluid(100))
                .duration(RICH_AURA_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        GTRecipeBuilder.of(id("erich_aura"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.ErichAura.getFluid(100))
                .duration(ERICH_AURA_FUEL_DURATION)
                .EUt(-8192)
                .save(provider);

        Pollution.LOGGER.info("[botania] MANA_TO_EU: 7/7 upstream fuels ported "
                + "(InfusedAura/Impuremana/WhiteMansus/BlackMansus/Starrymansus/RichAura/ErichAura)");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "mana_to_eu/" + path);
    }
}
