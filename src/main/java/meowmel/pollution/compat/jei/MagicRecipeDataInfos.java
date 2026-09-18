package meowmel.pollution.compat.jei;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import meowmel.pollution.api.amplification.MagicJeiHintResolver;
import meowmel.pollution.api.amplification.MagicProcessTag;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern replacement for upstream's {@code MagicPropertyRecipeUI}.
 *
 * <p>Upstream reserved vertical space on the JEI page and rendered the magic
 * recipe properties (vis, infused fluid, mana, life essence, research, tarot,
 * process tags) as extra lines. GTCEu Modern has no recipe-property registry:
 * extra lines are attached per recipe type through
 * {@link GTRecipeType#addDataInfo}. This class registers one reader per
 * {@link MagicRecipeProperties} key group on every custom magic recipe map, so
 * the lines appear on the normal GT recipe pages without a custom category.</p>
 *
 * <p>Only called from the JEI plugin: on a dedicated server (or without JEI)
 * the reader list is left untouched and no GT class is modified. Each reader
 * returns an empty string when its keys are absent, which is the same
 * convention GregTech itself uses for optional recipe data (for example the
 * EBF coil-tier line).</p>
 */
public final class MagicRecipeDataInfos {

    private static boolean installed;

    private MagicRecipeDataInfos() {}

    /** Registers the magic property readers exactly once. */
    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        for (GTRecipeType recipeType : magicRecipeTypes()) {
            recipeType.addDataInfo(MagicRecipeDataInfos::resourceCostLine);
            recipeType.addDataInfo(MagicRecipeDataInfos::gateLine);
            recipeType.addDataInfo(MagicRecipeDataInfos::processTagLine);
        }
    }

    private static List<GTRecipeType> magicRecipeTypes() {
        return List.of(
                PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES,
                PORecipeMaps.STOVE_RECIPES,
                PORecipeMaps.MAGIC_FUSION_REACTOR,
                PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES,
                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES,
                PORecipeMaps.MAGIC_GREENHOUSE_RECIPES,
                PORecipeMaps.MAGIC_TURBINE_FUELS,
                PORecipeMaps.FORGE_ALCHEMY_RECIPES,
                PORecipeMaps.NODE_MAGIC_FUSION_RECIPES,
                PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES,
                BotaniaRecipeMaps.MANA_PETAL_RECIPES,
                BotaniaRecipeMaps.MANA_RUNE_ALTAR_RECIPES,
                BotaniaRecipeMaps.PURE_DAISY_RECIPES,
                BotaniaRecipeMaps.MANA_INFUSION_RECIPES,
                BotaniaRecipeMaps.MANA_GEN_RECIPES,
                BotaniaRecipeMaps.MANA_TO_EU,
                BotaniaRecipeMaps.DAN_DE_LIFE_ON);
    }

    /** Per-tick / per-craft magic resource costs stored in the recipe data. */
    private static String resourceCostLine(CompoundTag data) {
        List<String> parts = new ArrayList<>();
        int vis = data.getInt(MagicRecipeProperties.VIS_PER_CRAFT);
        if (vis > 0) {
            parts.add(text("pollution.jei.recipe.cost.vis", vis));
        }
        int infused = data.getInt(MagicRecipeProperties.INFUSED_FLUID_PER_TICK);
        if (infused > 0) {
            parts.add(text("pollution.jei.recipe.cost.infused", infused));
        }
        long mana = data.getLong(MagicRecipeProperties.MANA_PER_TICK);
        if (mana > 0L) {
            parts.add(text("pollution.jei.recipe.cost.mana", mana));
        }
        int life = data.getInt(MagicRecipeProperties.LIFE_ESSENCE_PER_TICK);
        if (life > 0) {
            parts.add(text("pollution.jei.recipe.cost.life", life));
        }
        return parts.isEmpty() ? "" : text("pollution.jei.recipe.cost", String.join("；", parts));
    }

    /** Research / tarot / astral gates and catalyst-protection hints. */
    private static String gateLine(CompoundTag data) {
        List<String> parts = new ArrayList<>();
        String research = data.getString(MagicRecipeProperties.THAUMCRAFT_RESEARCH);
        if (!research.isEmpty()) {
            parts.add(text("pollution.jei.recipe.gate.research", research));
        }
        String tarot = data.getString(MagicRecipeProperties.TAROT);
        if (!tarot.isEmpty()) {
            parts.add(text("pollution.jei.recipe.gate.tarot",
                    MagicJeiHintResolver.tarotDisplayName(tarot)));
        }
        if (data.contains(MagicRecipeProperties.ASTRAL_CONDITION)) {
            parts.add(text("pollution.jei.recipe.gate.astral"));
        }
        String catalysts = data.getString(MagicRecipeProperties.CONSUMABLE_CATALYST_INPUTS);
        if (!catalysts.isEmpty()) {
            parts.add(text("pollution.jei.recipe.gate.catalyst", catalysts));
        }
        return parts.isEmpty() ? "" : text("pollution.jei.recipe.gate", String.join("；", parts));
    }

    /** Recipe-domain tags used by the constellation / tarot amplification. */
    private static String processTagLine(CompoundTag data) {
        long mask = data.getLong(MagicRecipeProperties.PROCESS_TAG_MASK);
        if (mask == 0L) {
            return "";
        }
        return text("pollution.jei.recipe.process_tags", MagicProcessTag.describeMask(mask));
    }

    private static String text(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
