package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;

/**
 * Shared structure predicates of the magic multiblocks.
 *
 * <p>Upstream's {@code configureMagicRecipeCasing} made the primary casing
 * character a choice between the casing block and every hatch the recipe map
 * needs (energy, maintenance, muffler, item/fluid hatches) plus the custom vis
 * and infused-fluid hatches. The modern port expresses the same rule with
 * {@link Predicates#autoAbilities(GTRecipeType...)} plus the custom abilities.</p>
 */
public final class MagicStructureElements {

    /**
     * Primary casing of a magic multiblock: accepts the casing block itself or
     * any hatch the supplied recipe types need (plus one vis and one infused
     * fluid hatch).
     */
    public static TraceabilityPredicate magicCasing(net.minecraft.world.level.block.Block casing,
                                                    GTRecipeType... recipeTypes) {
        return Predicates.blocks(casing)
                .or(Predicates.autoAbilities(recipeTypes))
                .or(Predicates.abilities(POMultiblockAbility.VIS_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(POMultiblockAbility.INFUSED_FLUID_HATCH).setMaxGlobalLimited(1));
    }

    /**
     * Frame casing of a GregTech material.
     *
     * <p>Upstream used GTQT materials (HyperdimensionalSilver, KQGold,
     * Mansussteel, Thaumium) for structure frames. Those are not part of the
     * port, so they are substituted with GTCEu materials of comparable
     * progression: HyperdimensionalSilver -&gt; NaquadahAlloy (LuV),
     * KQGold -&gt; TungstenSteel (IV), Mansussteel -&gt; HSSG (IV),
     * Thaumium -&gt; StainlessSteel (HV).</p>
     */
    public static TraceabilityPredicate frame(Material material) {
        return Predicates.blocks(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, material).get());
    }

    private MagicStructureElements() {}
}
