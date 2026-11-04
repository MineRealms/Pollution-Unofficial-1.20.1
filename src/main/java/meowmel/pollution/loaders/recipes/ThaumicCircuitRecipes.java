package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
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
 *       worktable recipe, templates 2..4 are TC infusions on the previous
 *       template (replacing the dead blood-magic assembler recipe in
 *       {@code MagicIntegrationRecipes}).</li>
 *   <li>{@code blood_circuit.0..6} are the mystic circuits MV..UV, added to the
 *       {@code gtceu:circuits/mv..uv} item tags via datapack JSON
 *       ({@code data/gtceu/tags/items/circuits/}).</li>
 *   <li>{@code blood_circuit.7..12} (UHV..MAX) are disabled: no recipes, no
 *       tags, hidden from JEI (ids survive for old worlds).</li>
 * </ul>
 *
 * <p><b>Strict tier parity.</b> Every mystic circuit mirrors the wetware-family
 * GT recipe of its tier ({@code CircuitRecipes}, verified against the pack's
 * live recipe dump): recipe type, board, lower-tier circuits, wires, foils,
 * plates, solder, voltage, duration, output count, cleanroom and research are
 * kept verbatim; only the chip / SMD / memory stack is replaced by the tier
 * template plus the tier mystic metal.</p>
 * <ul>
 *   <li>MV..LuV are processor-level (circuit assembler, no lower circuits);
 *       LuV keeps the neuro processing unit as its board, so the bio line stays
 *       mandatory.</li>
 *   <li>ZPM mirrors {@code wetware_processor_assembly_zpm}: wetware board +
 *       2 mystic LuV circuits, yields 2.</li>
 *   <li>UV mirrors {@code wetware_super_computer_uv}: assembly line with
 *       research-station research, 2 mystic ZPM circuits, yields 1.</li>
 * </ul>
 *
 * <p>Mystic metal ladder follows the real crafting order of the donor mods
 * (thaumadds: thaumium &lt; mithrillium &lt; adaminite &lt; mithminite):
 * thaumium (MV), crystal essence (HV), void ingot (EV), mithrillium (IV),
 * ichorium (LuV), adaminite (ZPM), mithminite (UV). Primordial pearls first
 * appear at ZPM (adaminite) and are synthesised industrially late game
 * ({@code ThaumicIndustrialRecipes#primordialPearl}).</p>
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
        // Template 1: arcane worktable — thaumium + quicksilver + LV circuit.
        // The vis map may only name the six primal channels: TC4R rejects any compound
        // aspect with "Arcane recipe cost is not a vis channel" and drops the recipe
        // silently (no build error). auram is a compound, so it is charged as aer.
        arcane(provider, "blood_circuit_template_1", PollutionItems.BLOOD_CIRCUIT.asStack(),
                new String[]{"TQT", " B "},
                Map.of("T", "thaumcraft:thaumium_ingot", "Q", "thaumcraft:quicksilver",
                        "B", "gtceu:basic_integrated_circuit"),
                Map.of("aer", 16, "ordo", 8));

        // Templates 2..4: TC infusions on the previous template. Inputs mirror
        // ThaumicIndustrialRecipes#templates, keep both in sync.
        infusion(provider, "blood_circuit_template_2", PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), 3,
                ing(PollutionItems.BLOOD_CIRCUIT.asStack()),
                Map.of("praecantatio", 16, "metallum", 8, "vacuos", 8),
                ing(item("thaumcraft", "void_ingot")),
                ing(item("thaumcraft", "crystal_essence")),
                ing(item("thaumcraft", "crystal_essence")),
                ing(item("thaumcraft", "quicksilver")));

        infusion(provider, "blood_circuit_template_3", PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), 5,
                ing(PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack()),
                Map.of("praecantatio", 24, "alienis", 16, "metallum", 12),
                ing(item("thaumadds", "mithrillium_ingot")),
                ing(item("tainted_magic", "shadow_metal_ingot")),
                ing(item("isorropia", "ethereal_shard")),
                ing(item("isorropia", "ethereal_shard")));

        // The creation shard already carries one primordial pearl; no second pearl here.
        infusion(provider, "blood_circuit_template_4", PollutionItems.BLOOD_CIRCUIT_SUPREME.asStack(), 8,
                ing(PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack()),
                Map.of("praecantatio", 32, "alienis", 24, "auram", 24),
                ing(item("thaumictinkerer", "ichorium")),
                ing(item("thaumictinkerer", "ichorium")),
                ing(item("tainted_magic", "creation_shard")),
                ing(item("minecraft", "nether_star")));
    }

    // ////////////////////////////////////
    // ***** mystic circuits MV..UV *****//
    // ////////////////////////////////////

    /** One fixed (non template / metal) input of a tier recipe. */
    private record Part(ItemStack stack, int count) {}

    private record TierSpec(String idSuffix, GTRecipeType type, ItemStack template, ItemStack metal,
                            List<Part> parts, int solderMb, int eut, int duration, int count,
                            boolean cleanroom, ItemStack research) {}

    private static int mysticCircuits(Consumer<FinishedRecipe> provider) {
        int added = 0;
        // MV: processor_mv (plastic board, 4 fine red alloy, 60 EU/t, 200 t, x2, no cleanroom).
        added += circuit(provider, new TierSpec("0", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT.asStack(), item("thaumcraft:thaumium_ingot"),
                List.of(part("gtceu:plastic_printed_circuit_board", 1), part("gtceu:fine_red_alloy_wire", 4)),
                72, 60, 200, 2, false, ItemStack.EMPTY));
        // HV: nano_processor_hv (epoxy board, 8 fine electrum, 600 EU/t, 200 t, x2, cleanroom).
        added += circuit(provider, new TierSpec("1", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT.asStack(), item("thaumcraft:crystal_essence"),
                List.of(part("gtceu:epoxy_printed_circuit_board", 1), part("gtceu:fine_electrum_wire", 8)),
                72, 600, 200, 2, true, ItemStack.EMPTY));
        // EV: quantum_processor_ev (fiber board, 12 fine platinum, 2400 EU/t, 200 t, x2, cleanroom).
        added += circuit(provider, new TierSpec("2", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), item("thaumcraft:void_ingot"),
                List.of(part("gtceu:fiber_reinforced_printed_circuit_board", 1),
                        part("gtceu:fine_platinum_wire", 12)),
                72, 2400, 200, 2, true, ItemStack.EMPTY));
        // IV: crystal_processor_iv (multilayer board, 8 fine NbTi, 9600 EU/t, 200 t, x2, cleanroom).
        added += circuit(provider, new TierSpec("3", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(), item("thaumadds:mithrillium_ingot"),
                List.of(part("gtceu:multilayer_fiber_reinforced_printed_circuit_board", 1),
                        part("gtceu:fine_niobium_titanium_wire", 8)),
                72, 9600, 200, 2, true, ItemStack.EMPTY));
        // LuV: wetware_processor_luv (neuro processing unit, 8 fine YBCO, 38400 EU/t, 200 t, x2, cleanroom).
        added += circuit(provider, new TierSpec("4", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), item("thaumictinkerer:ichorium"),
                List.of(part("gtceu:neuro_processing_unit", 1),
                        part("gtceu:fine_yttrium_barium_cuprate_wire", 8)),
                72, 38400, 200, 2, true, ItemStack.EMPTY));
        // ZPM: wetware_processor_assembly_zpm (wetware board, 2 LuV circuits, 16 fine YBCO,
        // 144 solder, 38400 EU/t, 400 t, x2, cleanroom).
        added += circuit(provider, new TierSpec("5", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(), item("thaumadds:adaminite_ingot"),
                List.of(part("gtceu:wetware_printed_circuit_board", 1),
                        new Part(PollutionItems.BLOOD_CIRCUIT_LUV.asStack(), 2),
                        part("gtceu:fine_yttrium_barium_cuprate_wire", 16)),
                144, 38400, 400, 2, true, ItemStack.EMPTY));
        // UV: wetware_super_computer_uv (assembly line, wetware board, 2 ZPM circuits, 24 fine YBCO,
        // 32 PBI foil, 4 europium plates, 1152 solder, 38400 EU/t, 400 t, x1, station research).
        added += circuit(provider, new TierSpec("6", GTRecipeTypes.ASSEMBLY_LINE_RECIPES,
                PollutionItems.BLOOD_CIRCUIT_SUPREME.asStack(), item("thaumadds:mithminite_ingot"),
                List.of(part("gtceu:wetware_printed_circuit_board", 1),
                        new Part(PollutionItems.BLOOD_CIRCUIT_ZPM.asStack(), 2),
                        part("gtceu:fine_yttrium_barium_cuprate_wire", 24),
                        part("gtceu:polybenzimidazole_foil", 32),
                        part("gtceu:europium_plate", 4)),
                1152, 38400, 400, 1, false, PollutionItems.BLOOD_CIRCUIT_ZPM.asStack()));
        return added;
    }

    private static int circuit(Consumer<FinishedRecipe> provider, TierSpec spec) {
        List<ItemStack> required = new ArrayList<>(List.of(spec.template(), spec.metal()));
        spec.parts().forEach(part -> required.add(part.stack()));
        if (anyEmpty(required.toArray(new ItemStack[0]))) {
            Pollution.LOGGER.warn("[thaumic-circuit] skipping tier {}: a required input item is missing", spec.idSuffix());
            return 0;
        }
        ItemStack out = PollutionItems.get("blood_circuit." + spec.idSuffix()).asStack(spec.count());
        if (out.isEmpty()) {
            Pollution.LOGGER.warn("[thaumic-circuit] skipping tier {}: circuit item missing", spec.idSuffix());
            return 0;
        }
        // Assembly-line inputs are ordered: board and lower circuits first, as GT does.
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "thaumic_circuit/" + spec.idSuffix()),
                spec.type());
        for (Part part : spec.parts()) {
            builder.inputItems(part.stack().copyWithCount(1), part.count());
        }
        builder.inputItems(spec.template())
                .inputItems(spec.metal())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(spec.solderMb()))
                .outputItems(out)
                .duration(spec.duration())
                .EUt(spec.eut());
        if (spec.cleanroom()) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        if (!spec.research().isEmpty()) {
            builder.stationResearch(b -> b.researchStack(spec.research()).CWUt(16));
        }
        builder.save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static Part part(String id, int count) {
        return new Part(item(id), count);
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
