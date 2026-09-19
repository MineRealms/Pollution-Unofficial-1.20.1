package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import net.minecraft.world.level.block.Block;

/**
 * Shared structure predicates of the magic multiblocks.
 *
 * <p>Upstream's {@code configureMagicRecipeCasing} made the primary casing
 * character a choice between the casing block and every hatch the recipe map
 * needs (energy, maintenance, muffler, item/fluid hatches) plus the custom vis
 * and infused-fluid hatches. The modern port expresses the same rule with
 * {@link Predicates#autoAbilities(GTRecipeType...)} for the recipe-dependent
 * item/fluid hatches plus the custom abilities registered in the port
 * ({@link POMultiblockAbility#VIS_HATCH},
 * {@link POMultiblockAbility#INFUSED_FLUID_HATCH},
 * {@link POMultiblockAbility#MANA_INPUT_HATCH},
 * {@link POMultiblockAbility#MANA_INPUT_POOL}).</p>
 *
 * <p>Limits mirror upstream's {@code globalAbilityLimit} calls:
 * the mana/energy pair shares the {@code abilityGroup(MANA_INPUT_HATCH, 1, 2)}
 * window, maintenance and infused-fluid are {@code 1..1}, muffler is
 * {@code 1..1} when the machine has no dedicated muffler character (otherwise
 * it stays on that character), vis and mana-pool are {@code 0..1}. Upstream's
 * blood-magic, astral-lens and tarot hatches have no counterpart in the port
 * (documented deviation, see the machine javadocs).</p>
 */
public final class MagicStructureElements {

    /**
     * Primary casing of a magic multiblock: accepts the casing block itself or
     * any hatch the supplied recipe types need (plus the magic hatches).
     */
    public static TraceabilityPredicate magicCasing(Block casing,
                                                    GTRecipeType... recipeTypes) {
        return magicCasing(Predicates.blocks(casing), recipeTypes);
    }

    /** Variant of {@link #magicCasing(Block, GTRecipeType...)} for frame casings. */
    public static TraceabilityPredicate magicCasing(TraceabilityPredicate casing,
                                                    GTRecipeType... recipeTypes) {
        return casing.or(magicHatches(recipeTypes));
    }

    /** Variant of {@link #magicCasing(Block, GTRecipeType...)} without a muffler hatch. */
    public static TraceabilityPredicate magicCasing(Block casing, boolean includeMuffler,
                                                    GTRecipeType... recipeTypes) {
        return Predicates.blocks(casing).or(magicHatches(includeMuffler, recipeTypes));
    }

    /**
     * Hatches accepted on the primary magic casing. Upstream's recipe-dependent
     * item/fluid hatches come from {@code autoAbilities}; energy is part of the
     * shared mana/energy ability group and therefore excluded from the
     * auto-abilities call.
     */
    public static TraceabilityPredicate magicHatches(GTRecipeType... recipeTypes) {
        return magicHatches(true, recipeTypes);
    }

    /** Variant of {@link #magicHatches(GTRecipeType...)} without a muffler hatch. */
    public static TraceabilityPredicate magicHatches(boolean includeMuffler,
                                                     GTRecipeType... recipeTypes) {
        TraceabilityPredicate predicate = Predicates
                .autoAbilities(recipeTypes, false, false, true, true, true, true)
                .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_HATCH, PartAbility.INPUT_ENERGY)
                        .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                .or(Predicates.abilities(POMultiblockAbility.VIS_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(POMultiblockAbility.INFUSED_FLUID_HATCH).setExactLimit(1))
                .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setMaxGlobalLimited(1));
        if (includeMuffler) {
            predicate = predicate.or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1));
        }
        return predicate;
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
