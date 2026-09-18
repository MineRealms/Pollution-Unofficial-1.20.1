package meowmel.pollution.api.recipes.properties;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

/**
 * Recipe metadata understood by the magic multiblock logic layer.
 *
 * <p>Upstream stored these through GTCEu 1.12's {@code RecipeProperty} registry.
 * Modern GTCEu has no such registry: recipes are data-driven and
 * {@link GTRecipe#data} is the supported extension point. The port keeps the
 * upstream keys verbatim as data entries and exposes typed helpers for builders
 * and logic.</p>
 *
 * <p>Scope note (2026-09-18): only the Thaumcraft-facing keys (vis, infused
 * fluids, research) are wired for builder use so far. Keys of deferred systems
 * (Botania mana, Blood Magic life essence, Astral, Tarot) are reserved with the
 * upstream names so ported recipes stay compatible once those systems land.
 * JEI display of these lines is a separate TODO (modern GT recipe info API).</p>
 */
public final class MagicRecipeProperties {

    public static final String INFUSED_FLUID_PER_TICK = "pollution.magic.infused_fluid_per_tick";
    public static final String MANA_PER_TICK = "pollution.magic.mana_per_tick";
    public static final String LIFE_ESSENCE_PER_TICK = "pollution.magic.life_essence_per_tick";
    public static final String VIS_PER_CRAFT = "pollution.magic.vis_per_craft";
    public static final String THAUMCRAFT_RESEARCH = "pollution.magic.thaumcraft_research";
    public static final String ASTRAL_CONDITION = "pollution.magic.astral_condition";
    public static final String TAROT = "pollution.magic.tarot";
    public static final String PROCESS_TAG_MASK = "pollution.magic.process_tag_mask";
    public static final String CONSUMABLE_CATALYST_INPUTS = "pollution.magic.consumable_catalyst_inputs";

    // ////////////////////////////////////
    // ***** Builder helpers *****//
    // ////////////////////////////////////

    /** Infused fluid drained every tick while the recipe runs (mB/t). */
    public static GTRecipeBuilder infusedFluidPerTick(GTRecipeBuilder builder, int amount) {
        return builder.addData(INFUSED_FLUID_PER_TICK, Math.max(0, amount));
    }

    /** Mana drained every tick while the recipe runs (reserved, Botania). */
    public static GTRecipeBuilder manaPerTick(GTRecipeBuilder builder, long amount) {
        return builder.addData(MANA_PER_TICK, Math.max(0L, amount));
    }

    /** Life essence drained every tick while the recipe runs (reserved, Blood Magic). */
    public static GTRecipeBuilder lifeEssencePerTick(GTRecipeBuilder builder, int amount) {
        return builder.addData(LIFE_ESSENCE_PER_TICK, Math.max(0, amount));
    }

    /** Vis paid once per craft, on top of any per-tick draws. */
    public static GTRecipeBuilder visPerCraft(GTRecipeBuilder builder, int amount) {
        return builder.addData(VIS_PER_CRAFT, Math.max(0, amount));
    }

    /** Player research gate (Thaumcraft research key). Empty values are ignored. */
    public static GTRecipeBuilder thaumcraftResearch(GTRecipeBuilder builder, String researchKey) {
        String normalized = researchKey == null ? "" : researchKey.trim();
        if (!normalized.isEmpty()) {
            builder.addData(THAUMCRAFT_RESEARCH, normalized);
        }
        return builder;
    }

    /** Tarot card id (reserved). */
    public static GTRecipeBuilder tarot(GTRecipeBuilder builder, String tarotId) {
        return builder.addData(TAROT, tarotId == null ? "" : tarotId.trim());
    }

    /**
     * Comma-separated item-input indices eligible for the catalyst-protection
     * rule of {@code MagicMultiblockRecipeLogic}.
     */
    public static GTRecipeBuilder consumableCatalystInputs(GTRecipeBuilder builder, int... inputIndices) {
        if (inputIndices == null || inputIndices.length == 0) {
            return builder;
        }
        StringBuilder serialized = new StringBuilder();
        for (int index : inputIndices) {
            if (index < 0) {
                continue;
            }
            if (serialized.length() > 0) {
                serialized.append(',');
            }
            serialized.append(index);
        }
        if (serialized.length() > 0) {
            builder.addData(CONSUMABLE_CATALYST_INPUTS, serialized.toString());
        }
        return builder;
    }

    /** Convenience mirror of the upstream {@code magic(...)} helper (TC subset). */
    public static GTRecipeBuilder magic(GTRecipeBuilder builder, int infusedPerTick, long manaPerTick,
                                        int lifeEssencePerTick, int visPerCraft) {
        infusedFluidPerTick(builder, infusedPerTick);
        manaPerTick(builder, manaPerTick);
        lifeEssencePerTick(builder, lifeEssencePerTick);
        visPerCraft(builder, visPerCraft);
        return builder;
    }

    // ////////////////////////////////////
    // ***** Recipe getters *****//
    // ////////////////////////////////////

    public static int getInfusedFluidPerTick(GTRecipe recipe) {
        return recipe.data.getInt(INFUSED_FLUID_PER_TICK);
    }

    public static long getManaPerTick(GTRecipe recipe) {
        return recipe.data.getLong(MANA_PER_TICK);
    }

    public static int getLifeEssencePerTick(GTRecipe recipe) {
        return recipe.data.getInt(LIFE_ESSENCE_PER_TICK);
    }

    public static int getVisPerCraft(GTRecipe recipe) {
        return recipe.data.getInt(VIS_PER_CRAFT);
    }

    public static boolean hasVisCost(GTRecipe recipe) {
        return getVisPerCraft(recipe) > 0;
    }

    public static String getThaumcraftResearch(GTRecipe recipe) {
        return recipe.data.getString(THAUMCRAFT_RESEARCH);
    }

    public static String getTarot(GTRecipe recipe) {
        return recipe.data.getString(TAROT);
    }

    public static long getProcessTagMask(GTRecipe recipe) {
        return recipe.data.getLong(PROCESS_TAG_MASK);
    }

    public static String getConsumableCatalystInputs(GTRecipe recipe) {
        return recipe.data.getString(CONSUMABLE_CATALYST_INPUTS);
    }

    private MagicRecipeProperties() {}
}
