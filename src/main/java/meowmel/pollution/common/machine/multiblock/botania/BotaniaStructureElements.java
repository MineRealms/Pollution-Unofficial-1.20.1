package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared structure predicates of the Botania multiblocks (batch 1).
 *
 * <p>Upstream origin: {@code ManaMultiblockController#configureManaRecipeCasing}
 * (1.12.2) made the primary casing a choice between the casing block and every
 * hatch the recipe map needs plus the mana hatches; that rule lives in
 * {@link #manaCasing(Block, GTRecipeType...)}.</p>
 *
 * <p>Upstream also matched tiered casings through
 * {@code POTieredCasingGroups} (GTQT beam cores / frames) and read the matched
 * tier from the formed structure. The port has no tiered-casing registry, so
 * {@link #beamCores()} and {@link #tieredFrames()} accept the corresponding
 * modern blocks and record the tier into the match context under
 * {@link #BEAM_CORE_TIER_KEY} / {@link #FRAME_TIER_KEY}; controllers read it
 * back with {@link #beamCoreTier(MultiblockControllerMachine)} and
 * {@link #frameTier(MultiblockControllerMachine)}.</p>
 */
public final class BotaniaStructureElements {

    public static final String BEAM_CORE_TIER_KEY = "BotaniaBeamCoreTier";
    public static final String FRAME_TIER_KEY = "BotaniaFrameTier";

    /** Beam core blocks I..V, matching upstream beam-core tiers 1..5. */
    private static final Block[] BEAM_CORES = {
            PollutionMagicBlocks.BEAM_CORE_0.get(),
            PollutionMagicBlocks.BEAM_CORE_1.get(),
            PollutionMagicBlocks.BEAM_CORE_2.get(),
            PollutionMagicBlocks.BEAM_CORE_3.get(),
            PollutionMagicBlocks.BEAM_CORE_4.get(),
    };

    /**
     * GTCEu frames substituting the GTQT frame tiers of upstream
     * {@code POTieredCasingGroups.frames()}. Levels mirror the material
     * progression used by {@code MagicStructureElements#frame}.
     */
    private static final Map<Material, Integer> FRAME_TIERS = new LinkedHashMap<>();

    static {
        FRAME_TIERS.put(GTMaterials.StainlessSteel, 3);
        FRAME_TIERS.put(GTMaterials.Titanium, 4);
        FRAME_TIERS.put(GTMaterials.TungstenSteel, 5);
        FRAME_TIERS.put(GTMaterials.HSSG, 6);
        FRAME_TIERS.put(GTMaterials.NaquadahAlloy, 7);
    }

    /**
     * Primary casing of a Botania mana multiblock: the casing block itself or
     * any hatch the supplied recipe types need, plus the magic hatches (same
     * layout and limits as upstream {@code configureManaRecipeCasing}).
     *
     * <p>Upstream's {@code abilityGroup(MANA_INPUT_HATCH, 1, 2)} over
     * {mana input hatch, input energy} becomes one shared predicate so a
     * structure may run on two mana hatches instead of a mandatory energy
     * hatch; maintenance and muffler are {@code 1..1} and the mana pool hatch
     * is {@code 0..1}.</p>
     */
    public static TraceabilityPredicate manaCasing(Block casing, GTRecipeType... recipeTypes) {
        return Predicates.blocks(casing)
                .or(Predicates.autoAbilities(recipeTypes, false, false, true, true, true, true))
                .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_HATCH, PartAbility.INPUT_ENERGY)
                        .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setMaxGlobalLimited(1))
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1));
    }

    /** Tiered beam cores I..V; records the matched tier into the match context. */
    public static TraceabilityPredicate beamCores() {
        return Predicates.custom(state -> {
            Block block = state.getBlockState().getBlock();
            for (int tier = 0; tier < BEAM_CORES.length; tier++) {
                if (BEAM_CORES[tier] == block) {
                    state.getMatchContext().set(BEAM_CORE_TIER_KEY, tier + 1);
                    return true;
                }
            }
            return false;
        }, () -> Arrays.stream(BEAM_CORES).map(BlockInfo::fromBlock).toArray(BlockInfo[]::new));
    }

    /** Tiered GT frames; records the matched tier into the match context. */
    public static TraceabilityPredicate tieredFrames() {
        return Predicates.custom(state -> {
            for (Map.Entry<Material, Integer> entry : FRAME_TIERS.entrySet()) {
                if (frameBlock(entry.getKey()) == state.getBlockState().getBlock()) {
                    state.getMatchContext().set(FRAME_TIER_KEY, entry.getValue());
                    return true;
                }
            }
            return false;
        }, () -> FRAME_TIERS.keySet().stream()
                .map(BotaniaStructureElements::frameBlock)
                .map(BlockInfo::fromBlock)
                .toArray(BlockInfo[]::new));
    }

    /** Matched beam-core tier (1..5), or 0 when no tiered core is present. */
    public static int beamCoreTier(MultiblockControllerMachine machine) {
        Integer tier = machine.getMultiblockState().getMatchContext().get(BEAM_CORE_TIER_KEY);
        return tier == null ? 0 : tier;
    }

    /** Matched frame tier, or 0 when no tiered frame is present. */
    public static int frameTier(MultiblockControllerMachine machine) {
        Integer tier = machine.getMultiblockState().getMatchContext().get(FRAME_TIER_KEY);
        return tier == null ? 0 : tier;
    }

    private static Block frameBlock(Material material) {
        return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, material).get();
    }

    private BotaniaStructureElements() {}
}
