package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

import static meowmel.pollution.loaders.recipes.InfusionRecipes.aspects;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.ing;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.infusion;

/**
 * Obtainable recipes for the multiblock-structure-exclusive blocks that the
 * audit ({@code docs/UNOBTAINABLE_ITEMS.md}) listed as recipe-less: the mineral
 * extractor, the alchemy PTFE vis conduit (previously only a recipe
 * ingredient) and the eldritch eye.
 *
 * <p><b>Complexity rule.</b> The deeper the block sits in the structure
 * hierarchy, the richer the recipe:</p>
 * <ul>
 *   <li><b>PTFE pipe</b> (EV, magic assembler): PTFE plate + HSSG plate + the
 *       GT PTFE pipe casing + Salisundus dust in an InfusedAura bath, mirroring
 *       the {@code MagicGCYMRecipes.pipe} convention with a vis-conduit
 *       accent.</li>
 *   <li><b>Mineral extractor</b> (EV, magic assembler): EV hull + HV circuit +
 *       MV board + EV sensor/emitter + HSSG frame + Valonite + Salisundus +
 *       mana resonance coil, bathed in InfusedEarth/InfusedAura and
 *       lubricated.</li>
 *   <li><b>Eldritch eye</b> (TC4R infusion): eldritch object central,
 *       void-seed/shard/ender components, alienis/praecantatio/tenebrae
 *       aspects.</li>
 * </ul>
 *
 * <p><b>Beam cores</b> already carry recipes; they are enriched in place
 * ({@link InfusionRecipes#beamCores} becomes a tier-scaled five-core line and
 * the {@code MagicChemicalRecipes.beamCore} shortcut scales with its index)
 * instead of being duplicated here.</p>
 *
 * <p><b>Skipped</b>: the flesh family (task skip list); {@code pollution:portal}
 * has no item form. The world-gen plants (alfheim/rainbow) moved to
 * {@link PlantBlockRecipes}.</p>
 */
public final class StructureBlockRecipes {

    private StructureBlockRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        int extractor = mineralExtractor(provider);
        int pipe = ptfePipe(provider);
        int eye = eldritchEye(provider);
        Pollution.LOGGER.info("[structure] registered {} mineral extractor recipe(s)", extractor);
        Pollution.LOGGER.info("[structure] registered {} PTFE pipe recipe(s)", pipe);
        Pollution.LOGGER.info("[structure] registered {} eldritch eye recipe(s)", eye);
    }

    // ////////////////////////////////////
    // ***** mineral extractor *****//
    // ////////////////////////////////////

    /** EV magic-assembler build: hull, control electronics, mining frame and magic catalysis. */
    private static int mineralExtractor(Consumer<FinishedRecipe> provider) {
        ItemStack result = PollutionMiscBlocks.MINERAL_EXTRACTOR.asStack();
        MachineDefinition hull = at(GTMachines.HULL, GTValues.EV);
        ItemStack circuit = SafeItems.of(PollutionItems.MAGIC_CIRCUIT_HV);
        ItemStack board = SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_MV);
        ItemStack sensor = SafeItems.gt("ev_sensor", 2);
        ItemStack emitter = SafeItems.gt("ev_emitter", 2);
        ItemStack frame = ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 4);
        ItemStack valonite = ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 2);
        ItemStack salisundus = ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 4);
        ItemStack coil = SafeItems.of(PollutionItems.MANA_RESONANCE_COIL, 2);
        FluidStack earth = fluid(PollutionMaterials.InfusedEarth, 2000);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
        FluidStack lubricant = fluid(GTMaterials.Lubricant, 1000);
        if (result.isEmpty() || hull == null
                || anyEmpty(circuit, board, sensor, emitter, frame, valonite, salisundus, coil)
                || earth.isEmpty() || aura.isEmpty() || lubricant.isEmpty()) {
            Pollution.LOGGER.warn("[structure] skipping the mineral extractor: "
                    + "a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("mineral_extractor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(hull.asStack())
                .inputItems(circuit)
                .inputItems(board)
                .inputItems(sensor)
                .inputItems(emitter)
                .inputItems(frame)
                .inputItems(valonite)
                .inputItems(salisundus)
                .inputItems(coil)
                .inputFluids(earth)
                .inputFluids(aura)
                .inputFluids(lubricant)
                .outputItems(result)
                .duration(400)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** PTFE pipe *****//
    // ////////////////////////////////////

    /** EV magic-assembler build, mirroring {@code MagicGCYMRecipes.pipe} with a vis-conduit accent. */
    private static int ptfePipe(Consumer<FinishedRecipe> provider) {
        ItemStack result = PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.asStack(3);
        ItemStack plate = ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene, 6);
        ItemStack hssg = ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 2);
        ItemStack casing = new ItemStack(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get());
        ItemStack salisundus = ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
        if (result.isEmpty() || anyEmpty(plate, hssg, casing, salisundus) || aura.isEmpty()) {
            Pollution.LOGGER.warn("[structure] skipping the PTFE pipe: a component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("polytetrafluoroethylene_pipe"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(plate)
                .inputItems(hssg)
                .inputItems(casing)
                .inputItems(salisundus)
                .inputFluids(aura)
                .outputItems(result)
                .duration(200)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** eldritch eye *****//
    // ////////////////////////////////////

    /** TC4R infusion: eldritch object central, void/ender components, alienis aspects. */
    private static int eldritchEye(Consumer<FinishedRecipe> provider) {
        ItemStack result = PollutionPlantBlocks.ELDRITCH_EYE.asStack();
        ItemStack central = SafeItems.byId("thaumcraft", "eldritch_object", 1);
        if (result.isEmpty() || central.isEmpty()) {
            Pollution.LOGGER.warn("[structure] skipping the eldritch eye: the result or the "
                    + "thaumcraft:eldritch_object central is missing");
            return 0;
        }
        return infusion(provider, "eldritch_eye", result, 8, central,
                aspects("alienis", 64, "praecantatio", 32, "tenebrae", 32, "vacuos", 16),
                ing(SafeItems.byId("thaumcraft", "void_seed", 1)),
                // TC4R ships six primal shards plus the balanced shard; there is no plain
                // "thaumcraft:shard" item (the 1.12 meta item was split).
                ing(SafeItems.byId("thaumcraft", "balanced_shard", 4)),
                ing(new ItemStack(Items.ENDER_EYE, 2)),
                ing(new ItemStack(Items.ENDER_PEARL, 4)),
                ing(SafeItems.byId("thaumcraft", "quicksilver", 2)),
                ing(SafeItems.byId("thaumcraft", "void_ingot", 1)),
                ing(SafeItems.byId("thaumcraft", "flux_goo", 2)),
                ing(SafeItems.byId("thaumcraft", "alumentum", 2))) ? 1 : 0;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static boolean anyEmpty(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static MachineDefinition at(MachineDefinition[] family, int tier) {
        return family == null || tier < 0 || tier >= family.length ? null : family[tier];
    }

    /** @return the fluid stack, or an empty stack when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(com.gregtechceu.gtceu.api.data.chemical.material.Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return FluidStack.EMPTY;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "structure/" + path);
    }
}
