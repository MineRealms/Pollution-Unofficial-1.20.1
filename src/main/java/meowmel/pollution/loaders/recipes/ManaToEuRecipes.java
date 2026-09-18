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
 * {@code EUt(8192)}, with the duration computed from the (removed)
 * {@code POConfig.MachineSettingSwitch} defaults:
 * {@code (int)(100 * EuPerMb / 8192)} for 100 mB of each mana fluid.</p>
 *
 * <p><b>Material substitutions:</b> {@code GTQTMaterials.Mana} maps to
 * {@link PollutionMaterials#InfusedAura}, the mapping already used by
 * {@code AERecipes} and {@code NodeFusionRecipes}. The other six upstream mana
 * fluids (Impuremana, WhiteMansus, BlackMansus, Starrymansus, RichAura,
 * ErichAura) are not ported; registering them all against the single ported
 * mana fluid would collapse into duplicate fuel entries, so they are skipped
 * and documented.</p>
 *
 * <p><b>Config inlining:</b> the upstream natural-mana default was
 * 8192 EU per mB, giving {@code (int)(100 * 8192 / 8192) = 100} ticks. The
 * port keeps that constant; the other defaults were 256/256/512/1024/2048/32768
 * EU per mB (3/3/6/12/25/400 ticks) and belong to the skipped fluids.</p>
 */
public final class ManaToEuRecipes {

    /** Upstream default burn time of 100 mB natural mana at 8192 EU/t. */
    private static final int MANA_FUEL_DURATION = 100;

    private ManaToEuRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("infused_aura"), BotaniaRecipeMaps.MANA_TO_EU)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .duration(MANA_FUEL_DURATION)
                .EUt(8192)
                .save(provider);

        Pollution.LOGGER.info("[botania] MANA_TO_EU: 1/7 upstream fuels ported; 6 skipped "
                + "(Impuremana/WhiteMansus/BlackMansus/Starrymansus/RichAura/ErichAura unported)");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "mana_to_eu/" + path);
    }
}
