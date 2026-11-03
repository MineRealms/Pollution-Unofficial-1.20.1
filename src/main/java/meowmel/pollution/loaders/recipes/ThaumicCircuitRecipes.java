package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import dev.tc4port.thaumcraft.registry.TCRecipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 奥术电路（{@code blood_circuit.*}，血魔法遗产的神秘系改造）。
 *
 * <p><b>Design (phase 3, 2026-11).</b> The 17 legacy blood-magic item ids are
 * kept (old worlds keep their items) but re-branded and re-purposed:</p>
 * <ul>
 *   <li>Templates 1..4 ({@code blood_circuit}, {@code _advanced}, {@code _ultimate},
 *       {@code _supreme}) are TC altar products: template 1 is an arcane
 *       worktable recipe, templates 2..4 are TC infusions (replacing the dead
 *       blood-magic assembler recipe in {@code MagicIntegrationRecipes}).</li>
 *   <li>{@code blood_circuit.0..6} are the mystic circuits MV..UV, crafted in
 *       the plain GT circuit assembler, and added to the
 *       {@code gtceu:circuits/mv..uv} item tags via datapack JSON
 *       ({@code data/gtceu/tags/items/circuits/}).</li>
 *   <li>{@code blood_circuit.7..12} (UHV..MAX) are disabled: no recipes, no
 *       tags, hidden from JEI (ids survive for old worlds).</li>
 * </ul>
 *
 * <p><b>Strict tier parity.</b> Every mystic circuit mirrors one reference GT
 * circuit-assembler recipe (dumped live from the pack's recipe manager,
 * {@code SunlitIndustryCompat/tools/phase3/}): the reference board, wires,
 * frames and solder are kept verbatim; the chip/SMD stack is replaced by the
 * tier template plus the tier mystic metal. Voltage, duration and output count
 * match the reference (>= parity on cost, <= parity on yield) and the
 * cleanroom condition matches the reference recipe.</p>
 *
 * <p>Mystic metal ladder (availability-checked against the pack jars):
 * thaumium (MV), crystal essence (HV), void ingot (EV), mithminite (IV),
 * adaminite (LuV), mithrillium (ZPM), ichorium (UV).</p>
 */
public final class ThaumicCircuitRecipes {

    private ThaumicCircuitRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        templates(provider);
        int circuits = mysticCircuits(provider);
        Pollution.LOGGER.info("[thaumic-circuit] registered {} mystic circuit recipes (blood_circuit.0..6)", circuits);
    }

    // ////////////////////////////////////
    // ***** templates 1..4 (TC altar) *****//
    // ////////////////////////////////////

    private static void templates(Consumer<FinishedRecipe> provider) {
        // Template 1: arcane worktable — thaumium + quicksilver + LV circuit, auram vis.
        arcane(provider, "blood_circuit_template_1", item("thaumcraft", "thaumium_ingot"),
                new String[]{"TQT", " B "},
                Map.of("T", "thaumcraft:thaumium_ingot", "Q", "thaumcraft:quicksilver",
                        "B", "gtceu:basic_integrated_circuit"),
                Map.of("auram", 16, "ordo", 8));

        // Templates 2..4: TC infusions on the previous template.
        infusion(provider, "blood_circuit_template_2", PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), 3,
                ing(PollutionItems.BLOOD_CIRCUIT.asStack()),
                Map.of("praecantatio", 16, "metallum", 8, "vacuos", 8),
                ing(item("thaumcraft", "void_ingot")),
                ing(item("thaumcraft", "crystal_essence"), item("thaumcraft", "crystal_essence")),
                ing(item("thaumcraft", "quicksilver")));

        infusion(provider, "blood_circuit_template_3", PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), 5,
                ing(PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack()),
                Map.of("praecantatio", 24, "alienis", 16, "metallum", 12),
                ing(item("thaumadds", "mithminite_ingot")),
                ing(item("tainted_magic", "shadow_metal_ingot")),
                ing(item("isorropia", "ethereal_shard"), item("isorropia", "ethereal_shard")));

        infusion(provider, "blood_circuit_template_4", PollutionItems.BLOOD_CIRCUIT_SUPREME.asStack(), 8,
                ing(PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack()),
                Map.of("praecantatio", 32, "alienis", 24, "auram", 24),
                ing(item("thaumadds", "mithrillium_ingot")),
                ing(item("tainted_magic", "creation_shard")),
                ing(item("thaumcraft", "primordial_pearl")));
    }

    // ////////////////////////////////////
    // ***** mystic circuits MV..UV *****//
    // ////////////////////////////////////

    private record TierSpec(int gtTier, String idSuffix, ItemStack template, ItemStack metal,
                            ItemStack board, ItemStack wire, int wireCount, ItemStack frame, int frameCount,
                            ItemStack[] extras, int solderMb, int eut, int duration, int count, boolean cleanroom) {}

    private static int mysticCircuits(Consumer<FinishedRecipe> provider) {
        int added = 0;
        added += circuit(provider, new TierSpec(GTValues.MV, "0",
                PollutionItems.BLOOD_CIRCUIT.asStack(), metal("thaumcraft", "thaumium_ingot"),
                board("gtceu:plastic_printed_circuit_board"), wire("gtceu:fine_red_alloy_wire"), 4,
                null, 0, new ItemStack[0], 72,
                60, 200, 2, false));
        added += circuit(provider, new TierSpec(GTValues.HV, "1",
                PollutionItems.BLOOD_CIRCUIT.asStack(), metal("thaumcraft", "crystal_essence"),
                board("gtceu:epoxy_printed_circuit_board"), wire("gtceu:fine_electrum_wire"), 8,
                null, 0, new ItemStack[0], 72,
                600, 200, 2, true));
        added += circuit(provider, new TierSpec(GTValues.EV, "2",
                PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), metal("thaumcraft", "void_ingot"),
                board("gtceu:fiber_reinforced_printed_circuit_board"), wire("gtceu:fine_platinum_wire"), 12,
                null, 0, new ItemStack[0], 72,
                2400, 200, 2, true));
        added += circuit(provider, new TierSpec(GTValues.IV, "3",
                PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), metal("thaumadds", "mithminite_ingot"),
                board("gtceu:multilayer_fiber_reinforced_printed_circuit_board"),
                wire("gtceu:fine_niobium_titanium_wire"), 8,
                null, 0, new ItemStack[0], 72,
                9600, 200, 2, true));
        added += circuit(provider, new TierSpec(GTValues.LuV, "4",
                PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), metal("thaumadds", "adaminite_ingot"),
                null, wire("gtceu:fine_yttrium_barium_cuprate_wire"), 8,
                null, 0, new ItemStack[0], 72,
                38400, 200, 2, true));
        added += circuit(provider, new TierSpec(GTValues.ZPM, "5",
                PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), metal("thaumadds", "mithrillium_ingot"),
                null, wire("gtceu:annealed_copper_single_wire"), 48,
                board("gtceu:hssg_frame"), 2, new ItemStack[0], 288,
                7680, 800, 1, true));
        added += circuit(provider, new TierSpec(GTValues.UV, "6",
                PollutionItems.BLOOD_CIRCUIT_SUPREME.asStack(), metal("thaumictinkerer", "ichorium"),
                null, wire("gtceu:niobium_titanium_single_wire"), 8,
                board("gtceu:hsse_frame"), 2, new ItemStack[0], 1440,
                30720, 800, 1, false));
        return added;
    }

    private static int circuit(Consumer<FinishedRecipe> provider, TierSpec spec) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(spec.template());
        inputs.add(spec.metal());
        if (spec.board() != null) {
            inputs.add(spec.board());
        }
        if (spec.frame() != null) {
            inputs.add(spec.frame());
        }
        inputs.add(spec.wire());
        for (ItemStack extra : spec.extras()) {
            inputs.add(extra);
        }
        if (anyEmpty(inputs.toArray(new ItemStack[0]))) {
            Pollution.LOGGER.warn("[thaumic-circuit] skipping tier {}: a required input item is missing", spec.idSuffix());
            return 0;
        }
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "thaumic_circuit/" + spec.idSuffix()),
                GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(spec.template())
                .inputItems(spec.metal());
        if (spec.board() != null) {
            builder.inputItems(spec.board());
        }
        if (spec.frame() != null) {
            builder.inputItems(spec.frame(), spec.frameCount());
        }
        builder.inputItems(spec.wire(), spec.wireCount());
        for (ItemStack extra : spec.extras()) {
            builder.inputItems(extra);
        }
        builder.inputFluids(GTMaterials.SolderingAlloy.getFluid(spec.solderMb()));
        ItemStack out = PollutionItems.get("blood_circuit." + spec.idSuffix()).asStack(spec.count());
        if (out.isEmpty()) {
            Pollution.LOGGER.warn("[thaumic-circuit] skipping tier {}: circuit item missing", spec.idSuffix());
            return 0;
        }
        builder.outputItems(out)
                .duration(spec.duration())
                .EUt(spec.eut());
        if (spec.cleanroom()) {
            builder.addCondition(new CleanroomCondition(CleanroomType.CLEANROOM));
        }
        builder.save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack board(String id) {
        return item(id);
    }

    private static ItemStack wire(String id) {
        return item(id);
    }

    private static ItemStack metal(String namespace, String path) {
        return item(namespace, path);
    }

    /** Resolve an item id against the registry; EMPTY when absent (recipe is skipped then). */
    private static ItemStack item(String id) {
        int sep = id.indexOf(':');
        return sep < 0 ? item(Pollution.MOD_ID, id) : item(id.substring(0, sep), id.substring(sep + 1));
    }

    private static ItemStack item(String namespace, String path) {
        Item resolved = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return resolved == null ? ItemStack.EMPTY : new ItemStack(resolved);
    }

    private static net.minecraft.world.item.crafting.Ingredient ing(ItemStack... stacks) {
        return net.minecraft.world.item.crafting.Ingredient.of(stacks);
    }

    private static boolean anyEmpty(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** TC4R arcane worktable recipe (research-free entry point; vis cost applies). */
    private static void arcane(Consumer<FinishedRecipe> provider, String name, ItemStack result,
                               String[] pattern, Map<String, String> key, Map<String, Integer> vis) {
        if (anyEmpty(result)) {
            Pollution.LOGGER.warn("Skipping thaumic_circuit/arcane/{}: result item is missing", name);
            return;
        }
        for (String itemId : key.values()) {
            if (item(itemId).isEmpty()) {
                Pollution.LOGGER.warn("Skipping thaumic_circuit/arcane/{}: key item {} is missing", name, itemId);
                return;
            }
        }
        provider.accept(new ArcaneFinishedRecipe(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "arcane/" + name),
                pattern, key, result, vis));
    }

    private static void infusion(Consumer<FinishedRecipe> provider, String name, ItemStack result, int instability,
                                 net.minecraft.world.item.crafting.Ingredient central, Map<String, Integer> aspects,
                                 net.minecraft.world.item.crafting.Ingredient... components) {
        if (result.isEmpty() || central.isEmpty()) {
            Pollution.LOGGER.warn("Skipping thaumic_circuit/infusion/{}: item missing", name);
            return;
        }
        for (net.minecraft.world.item.crafting.Ingredient component : components) {
            if (component.isEmpty()) {
                Pollution.LOGGER.warn("Skipping thaumic_circuit/infusion/{}: component missing", name);
                return;
            }
        }
        provider.accept(new ThaumicInfusionFinishedRecipe(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "infusion/" + name),
                central, java.util.List.of(components), result, instability, aspects));
    }

    /** Emits the exact {@code thaumcraft:arcane_shaped} JSON schema (research omitted). */
    private record ArcaneFinishedRecipe(ResourceLocation id, String[] pattern, Map<String, String> key,
                                        ItemStack result, Map<String, Integer> vis)
            implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray patternArray = new JsonArray();
            for (String row : pattern) {
                patternArray.add(row);
            }
            json.add("pattern", patternArray);
            JsonObject keyJson = new JsonObject();
            key.forEach((symbol, itemId) -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("item", itemId);
                keyJson.add(symbol, entry);
            });
            json.add("key", keyJson);
            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());
            resultJson.addProperty("count", result.getCount());
            json.add("result", resultJson);
            JsonObject visJson = new JsonObject();
            vis.forEach(visJson::addProperty);
            json.add("vis", visJson);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return TCRecipes.ARCANE_SHAPED_SERIALIZER.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    /** {@code thaumcraft:infusion} JSON with the vanilla TC INFUSION research key. */
    private record ThaumicInfusionFinishedRecipe(ResourceLocation id,
                                                 net.minecraft.world.item.crafting.Ingredient central,
                                                 java.util.List<net.minecraft.world.item.crafting.Ingredient> components,
                                                 ItemStack result, int instability,
                                                 Map<String, Integer> aspects) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("research", "INFUSION");
            json.add("central", central.toJson());
            JsonArray componentArray = new JsonArray();
            for (net.minecraft.world.item.crafting.Ingredient component : components) {
                componentArray.add(component.toJson());
            }
            json.add("components", componentArray);
            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());
            resultJson.addProperty("count", result.getCount());
            json.add("result", resultJson);
            json.addProperty("instability", instability);
            JsonObject aspectJson = new JsonObject();
            aspects.forEach(aspectJson::addProperty);
            json.add("aspects", aspectJson);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return TCRecipes.INFUSION_SERIALIZER.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
