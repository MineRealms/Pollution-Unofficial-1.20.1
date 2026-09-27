package meowmel.pollution.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.JsonOps;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.astral.AstralCrystalNbtHelper;
import meowmel.pollution.api.astral.AstralNbtHelper;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeMaps;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeOutputs;
import meowmel.pollution.common.machine.multiblock.astral.ConstellationTowerDefinition;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AstralRecipeGameTests {
    private AstralRecipeGameTests() {}

    @GameTest(template = "platform", batch = "astral_recipes")
    public static void allPollutionRecipeIngredientsExistAndFitTheirRecipeMaps(GameTestHelper helper) {
        List<String> errors = new ArrayList<>();
        List<GTRecipe> recipes = new ArrayList<>();
        for (var entry : helper.getLevel().getRecipeManager().getRecipes()) {
            if (entry instanceof GTRecipe recipe && "pollution".equals(recipe.getId().getNamespace())) recipes.add(recipe);
        }
        for (var type : List.of(AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES, AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES)) {
            type.getProxyRecipes().values().forEach(recipes::addAll);
        }
        for (GTRecipe recipe : recipes) {
            checkContents(recipe, recipe.inputs, "input", recipe.recipeType.maxInputs, errors);
            checkContents(recipe, recipe.outputs, "output", recipe.recipeType.maxOutputs, errors);
            checkContents(recipe, recipe.tickInputs, "tick input", recipe.recipeType.maxInputs, errors);
            checkContents(recipe, recipe.tickOutputs, "tick output", recipe.recipeType.maxOutputs, errors);
        }
        if (!errors.isEmpty()) {
            errors.forEach(error -> Pollution.LOGGER.error("Recipe ingredient audit: {}", error));
            helper.fail("Invalid survival recipes (" + errors.size() + "): " + String.join("; ", errors.subList(0, Math.min(12, errors.size()))));
            return;
        }
        helper.assertTrue(recipes.size() > 100, "recipe audit did not inspect the loaded Pollution recipe set");
        helper.assertTrue(recipes.stream().anyMatch(recipe -> recipe.getId().getPath().endsWith("/seawater_bromine")),
                "seawater_bromine was skipped while loading its iodine byproduct");
        Pollution.LOGGER.info("Recipe ingredient audit passed: {} Pollution and native proxy recipes", recipes.size());
        helper.succeed();
    }

    private static void checkContents(GTRecipe recipe, Map<RecipeCapability<?>, List<Content>> contents,
                                      String direction, Map<RecipeCapability<?>, Integer> limits, List<String> errors) {
        for (var cap : List.of(ItemRecipeCapability.CAP, FluidRecipeCapability.CAP)) {
            var entries = contents.getOrDefault(cap, List.of());
            if (entries.size() > limits.getOrDefault(cap, 0)) {
                errors.add(recipe.getId() + " " + direction + " " + cap.name + " has " + entries.size() + " slots, limit " + limits.getOrDefault(cap, 0));
            }
            for (int i = 0; i < entries.size(); i++) {
                Object content = entries.get(i).content;
                boolean valid;
                if (cap == ItemRecipeCapability.CAP) {
                    var ingredient = ItemRecipeCapability.CAP.of(content);
                    valid = !ingredient.isEmpty() && java.util.Arrays.stream(ingredient.getItems())
                            .anyMatch(stack -> !stack.isEmpty() && stack.getCount() > 0 && ingredient.test(stack));
                } else {
                    var ingredient = FluidRecipeCapability.CAP.of(content);
                    valid = ingredient.getAmount() > 0 && java.util.Arrays.stream(ingredient.getStacks())
                            .anyMatch(stack -> !stack.isEmpty() && stack.getAmount() > 0 && ingredient.test(stack));
                }
                if (!valid) errors.add(recipe.getId() + " " + direction + " " + cap.name + "[" + i + "] is empty/unmatchable: " + content);
            }
        }
    }

    @GameTest(template = "platform", batch = "astral_recipes")
    public static void nativeRecipesAndEveryControllerAreObtainable(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var infusions = AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES.getProxyRecipes().get(RecipeTypesAS.INFUSION.get());
        var wells = AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES.getProxyRecipes().get(RecipeTypesAS.WELL.get());
        helper.assertTrue(infusions != null && !infusions.isEmpty()
                && infusions.size() == manager.getAllRecipesFor(RecipeTypesAS.INFUSION.get()).size(), "native infuser import is incomplete");
        helper.assertTrue(wells != null && !wells.isEmpty()
                && wells.size() == manager.getAllRecipesFor(RecipeTypesAS.WELL.get()).size(), "native lightwell import is incomplete");
        for (GTRecipe well : wells) helper.assertTrue(well.getInputContents(ItemRecipeCapability.CAP).get(0).chance == 0,
                "lightwell consumed its catalyst: " + well.id);
        infusions.forEach(recipe -> assertLookup(helper, recipe));
        wells.forEach(recipe -> assertLookup(helper, recipe));
        for (String name : List.of("astral_lens_hatch", "astral_lens_hatch_advanced", "industrial_starlight_infuser",
                "industrial_lightwell", "celestial_observation_array", "celestial_calibration_matrix", "celestial_crystal_growth_array")) {
            assertLookup(helper, recipe(helper, "components/" + name));
        }
        for (ConstellationTowerDefinition tower : ConstellationTowerDefinition.values()) {
            GTRecipe craft = recipe(helper, "towers/" + tower.getId());
            ItemStack wafer = AstralNbtHelper.createDataWafer(AstralNbtHelper.findConstellation(tower.getId()));
            helper.assertTrue(craft.getInputContents(ItemRecipeCapability.CAP).stream()
                    .anyMatch(c -> ItemRecipeCapability.CAP.of(c.content).test(wafer)), "tower missing its attuned wafer");
            assertLookup(helper, craft);
            assertLookup(helper, recipe(helper, "observation/" + tower.getId()));
            assertLookup(helper, recipe(helper, "calibration/" + tower.getId()));
            assertLookup(helper, recipe(helper, "growth/" + tower.getId()));
        }
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "astral_recipes")
    public static void crystalLineagePreservesSourceAndDoesNotMixQualities(GameTestHelper helper) {
        ItemStack source = rock(1);
        ItemStack different = rock(2);
        GTRecipe seedRecipe = recipe(helper, "crystals/seed");
        GTRecipe selected = AstralRecipeOutputs.transform(seedRecipe, List.of(source, different));
        helper.assertTrue(selected != null, "real native crystal was rejected");
        helper.assertTrue(!ItemRecipeCapability.CAP.of(selected.getInputContents(ItemRecipeCapability.CAP).get(0).content).test(different),
                "parallel input can merge a different crystal's quality");
        ItemStack seed = output(selected);
        helper.assertTrue(AstralCrystalNbtHelper.isCrystalSeed(seed), "seed output lost its lineage");
        helper.assertTrue(!output(seedRecipe).hasTag(), "dynamic processing mutated the global recipe");
        GTRecipe embryo = AstralRecipeOutputs.transform(recipe(helper, "crystals/embryo"), List.of(seed));
        helper.assertTrue(embryo != null && AstralCrystalNbtHelper.isCrystalEmbryo(output(embryo)), "embryo lost its source");
        GTRecipe grown = AstralRecipeOutputs.transform(recipe(helper, "growth/vicio"), List.of(output(embryo)));
        helper.assertTrue(grown != null, "valid embryo did not grow");
        ItemStack crystal = output(grown);
        helper.assertTrue(AstralCrystalNbtHelper.getOpticalQuality(crystal) > 0
                && !AstralCrystalNbtHelper.getCultivatedNativeCrystal(crystal).isEmpty(), "grown crystal lacks usable native attributes");
        helper.assertTrue(crystal.getTag().getCompound("poSourceCrystal").equals(seed.getTag().getCompound("poSourceCrystal"))
                && crystal.getTag().getInt("poCrystalGeneration") == 1, "crystal ancestry was replaced");
        helper.assertTrue(!AstralCrystalNbtHelper.isEligibleRockCrystal(crystal), "cultivated crystal can be selected recursively");
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        var json = GTRecipeSerializer.CODEC.encodeStart(ops, grown).getOrThrow(false, message -> {});
        var restored = GTRecipeSerializer.CODEC.parse(ops, json).getOrThrow(false, message -> {});
        helper.assertTrue(ItemStack.isSameItemSameTags(crystal, output(restored)), "recipe serialization lost dynamic output NBT");
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "astral_recipes")
    public static void blankCrystalsAndWrongConstellationsAreRejected(GameTestHelper helper) {
        helper.assertTrue(AstralRecipeOutputs.transform(recipe(helper, "crystals/embryo"),
                List.of(PollutionItems.ROCK_CRYSTAL_SEED.asStack())) == null, "blank seed was accepted");
        helper.assertTrue(AstralRecipeOutputs.transform(recipe(helper, "growth/vicio"),
                List.of(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO.asStack())) == null, "blank embryo was accepted");
        GTRecipe calibration = recipe(helper, "calibration/vicio");
        helper.assertTrue(AstralRecipeOutputs.transform(calibration,
                List.of(PollutionItems.CULTIVATED_CRYSTAL.asStack())) == null, "quality-zero catalyst was accepted");
        ItemStack right = AstralNbtHelper.createDataWafer(AstralNbtHelper.findConstellation("vicio"));
        ItemStack wrong = AstralNbtHelper.createDataWafer(AstralNbtHelper.findConstellation("armara"));
        var wafer = ItemRecipeCapability.CAP.of(calibration.getInputContents(ItemRecipeCapability.CAP).get(1).content);
        helper.assertTrue(wafer.test(right) && !wafer.test(wrong) && !wafer.test(PollutionItems.CONSTELLATION_DATA_WAFER.asStack()),
                "calibration does not enforce the wafer constellation");
        ItemStack good = AstralCrystalNbtHelper.createCultivatedCrystal(AstralCrystalNbtHelper.createEmbryo(
                AstralCrystalNbtHelper.createSeed(rock(3))), "vicio");
        helper.assertTrue(AstralRecipeOutputs.transform(calibration, List.of(good)) != null,
                "high-quality cultivated crystal was rejected");
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "astral_recipes")
    public static void nativeInfusionCopiesRealInputNbt(GameTestHelper helper) {
        var recipes = AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES.getProxyRecipes().get(RecipeTypesAS.INFUSION.get());
        GTRecipe recipe = recipes.stream().filter(r -> "copy_nbt".equals(r.data.getString(AstralRecipeOutputs.TRANSFORM)))
                .findFirst().orElseThrow(() -> new IllegalStateException("no NBT-preserving native infusion imported"));
        ItemStack input = ItemRecipeCapability.CAP.of(recipe.getInputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0].copy();
        input.getOrCreateTag().putString("astral_recipe_regression", "preserved");
        GTRecipe modified = AstralRecipeOutputs.transform(recipe, List.of(input));
        helper.assertTrue(modified != null && "preserved".equals(output(modified).getOrCreateTag().getString("astral_recipe_regression")),
                "native infusion lost input NBT");
        helper.succeed();
    }

    private static GTRecipe recipe(GameTestHelper helper, String path) {
        String suffix = "/astral/" + path;
        return helper.getLevel().getRecipeManager().getRecipes().stream()
                .filter(GTRecipe.class::isInstance).map(GTRecipe.class::cast)
                .filter(recipe -> "pollution".equals(recipe.getId().getNamespace()) && recipe.getId().getPath().endsWith(suffix))
                .findFirst().orElseThrow(() -> new IllegalStateException("missing survival recipe: pollution:*" + suffix));
    }

    private static void assertLookup(GameTestHelper helper, GTRecipe recipe) {
        Map<RecipeCapability<?>, List<Object>> inputs = new HashMap<>();
        recipe.inputs.forEach((cap, entries) -> inputs.put(cap, entries.stream().map(Content::getContent).toList()));
        helper.assertTrue(recipe.recipeType.db().find(inputs, found -> recipe.getId().equals(found.getId())) != null,
                "recipe exists in JEI data but is missing from the executable lookup: " + recipe.getId());
    }

    private static ItemStack output(GTRecipe recipe) {
        return ItemRecipeCapability.CAP.of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
    }

    private static ItemStack rock(int tier) {
        ItemStack rock = new ItemStack(ItemsAS.ROCK_CRYSTAL.get());
        CrystalAttributes.Builder.newBuilder(false)
                .addProperty(CrystalPropertiesAS.Properties.PROPERTY_SIZE, tier)
                .addProperty(CrystalPropertiesAS.Properties.PROPERTY_PURITY, tier)
                .addProperty(CrystalPropertiesAS.Properties.PROPERTY_SHAPE, tier).build().store(rock);
        return rock;
    }
}
