package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.recipes.PORecipeMaps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import meowmel.pollution.Pollution;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 神秘物品工业量产：把 TC 配方 JSON（{@code thaumcraft:infusion / crucible /
 * arcane_shaped / arcane_shapeless}）里<b>白名单产物</b>的配方转写成
 * {@link PORecipeMaps#INDUSTRIAL_INFUSION_RECIPES} 的工业注魔配方。
 *
 * <p><b>Why.</b> 魔法×GT 总原则：前期用神秘手段（坩埚/奥术/注魔祭坛）打基础，
 * 高级阶段解锁工业化大批量生产。工业注魔多方块（LuV 前后解锁）+ GT 并行仓
 * （IV/LuV/ZPM/UV = 4/16/64/256）+ 超频提供量产杠杆；手工祭坛路线保留。</p>
 *
 * <p><b>Conversion rules (phase 3, 2026-11).</b></p>
 * <ul>
 *   <li>只转换白名单产物（默认内置，{@code PollutionConfig#EXTRA_THAUMIC_INDUSTRIAL_WHITELIST}
 *       可按产物 id 追加）；附魔、法杖装配、生物注魔一律不转。</li>
 *   <li>物品输入 = 原配方的中心物/催化剂/组件（{@code {"item":...}} 直取，
 *       {@code {"tag":...}} 转 tag ingredient；带 NBT 的输入或输出跳过）。</li>
 *   <li>要素 → 对应注魔流体，每点 144 mB（{@link PollutionAspectMapping}）；
 *       奥术配方 vis 按每点 36 mB（灵气可再生，比源质便宜）。</li>
 *   <li>未映射要素或流体种类 &gt; 8 的配方跳过并计数。</li>
 *   <li>EUt 固定 VA[HV]；时长 = max(100, 要素总量 × 5) tick；
 *       原研究键写入 {@link MagicRecipeProperties#THAUMCRAFT_RESEARCH}（机主校验）。</li>
 *   <li>确定性配方 id：{@code pollution:industrial_infusion/thaumic/<ns>/<原路径>}。</li>
 * </ul>
 *
 * <p>Pollution 自产的四块奥术电路基底模板不经过 JSON 扫描（它们是运行时动态
 * 配方），在本类中直接按同一规则落成工业注魔配方；每块模板都消耗上一块模板，
 * 与祭坛路线一致。原始珍珠只有 Boss 掉落，本类另给一条 LuV 级工业合成作为后期替代。</p>
 */
public final class ThaumicIndustrialRecipes {

    /** MB of aspect fluid per essentia point (exchange/smelter rate). */
    private static final int MB_PER_POINT = 144;
    /** MB per vis point for arcane recipes (aura is renewable and cheaper). */
    private static final int MB_PER_VIS_POINT = 36;
    private static final int MAX_ITEMS = 25;
    private static final int MAX_FLUIDS = 8;
    /** Default whitelist: the four templates and the pearl are handled directly (dynamic recipes). */
    private static final Set<String> DEFAULT_WHITELIST = Set.of(
            "thaumcraft:thaumium_ingot", "thaumcraft:void_ingot", "thaumcraft:quicksilver",
            "thaumcraft:balanced_shard", "thaumcraft:crystal_essence",
            "thaumcraft:salis_mundus",
            "thaumadds:mithminite_ingot", "thaumadds:adaminite_ingot", "thaumadds:mithrillium_ingot",
            "tainted_magic:shadow_metal_ingot", "tainted_magic:creation_shard",
            "isorropia:ethereal_shard",
            "thaumictinkerer:ichorium", "thaumictinkerer:ichor");

    private ThaumicIndustrialRecipes() {}

    public static void init(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        int converted = 0;
        converted += templates(provider);
        converted += primordialPearl(provider);
        converted += scanModFiles(provider);
        Pollution.LOGGER.info("[thaumic-industrial] registered {} industrial infusion recipes from TC JSONs",
                converted);
    }

    // ////////////////////////////////////
    // ***** the four circuit templates *****//
    // ////////////////////////////////////

    /**
     * Industrial twins of {@code ThaumicCircuitRecipes#templates}: same items
     * (including the previous template), aspects / vis as fluids, no instability.
     */
    private static int templates(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        int added = 0;
        // template 1 (arcane): thaumium x2 + quicksilver + LV circuit, vis aer 16 / ordo 8 (no research)
        added += twin(provider, "template_1", "pollution:blood_circuit",
                pairs("thaumcraft:thaumium_ingot", 2, "thaumcraft:quicksilver", 1,
                        "gtceu:basic_integrated_circuit", 1),
                Map.of("aer", 16, "ordo", 8), MB_PER_VIS_POINT, "");
        // template 2: template 1 + void_ingot + crystal_essence x2 + quicksilver
        added += twin(provider, "template_2", "pollution:blood_circuit_advanced",
                pairs("pollution:blood_circuit", 1, "thaumcraft:void_ingot", 1,
                        "thaumcraft:crystal_essence", 2, "thaumcraft:quicksilver", 1),
                Map.of("praecantatio", 16, "metallum", 8, "vacuos", 8), MB_PER_POINT, "INFUSION");
        // template 3: template 2 + mithrillium + shadow_metal + ethereal_shard x2
        added += twin(provider, "template_3", "pollution:blood_circuit_ultimate",
                pairs("pollution:blood_circuit_advanced", 1, "thaumadds:mithrillium_ingot", 1,
                        "tainted_magic:shadow_metal_ingot", 1, "isorropia:ethereal_shard", 2),
                Map.of("praecantatio", 24, "alienis", 16, "metallum", 12), MB_PER_POINT, "INFUSION");
        // template 4: template 3 + ichorium x2 + creation_shard + nether_star
        added += twin(provider, "template_4", "pollution:blood_circuit_supreme",
                pairs("pollution:blood_circuit_ultimate", 1, "thaumictinkerer:ichorium", 2,
                        "tainted_magic:creation_shard", 1, "minecraft:nether_star", 1),
                Map.of("praecantatio", 32, "alienis", 24, "auram", 24), MB_PER_POINT, "INFUSION");
        return added;
    }

    /**
     * Primordial pearls only drop from eldritch bosses and rare loot bags; no TC recipe makes them.
     * Adaminite, mithminite and the creation shard all eat one, so the late mystic circuits could
     * never scale. This is the late-game industrial substitute: LuV voltage, a nether star and a
     * heavy aspect bill per pearl, gated by the owner's TC {@code PRIMPEARL} research.
     */
    private static int primordialPearl(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        return emit(provider, "primordial_pearl_synthesis", "thaumcraft:primordial_pearl",
                pairs("minecraft:nether_star", 1, "thaumcraft:balanced_shard", 4, "isorropia:ethereal_shard", 2),
                Map.of("praecantatio", 32, "auram", 32, "alienis", 32, "vacuos", 32), MB_PER_POINT,
                "PRIMPEARL", GTValues.VA[GTValues.LuV], 1200);
    }

    private static int twin(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider, String name,
                            String resultId, List<Pair<String, Integer>> inputs, Map<String, Integer> aspects,
                            int mbPerPoint, String research) {
        int total = aspects.values().stream().mapToInt(Integer::intValue).sum();
        return emit(provider, name, resultId, inputs, aspects, mbPerPoint, research,
                GTValues.VA[GTValues.HV], Math.max(100, total * 5));
    }

    /** @return 1 when the recipe was emitted */
    private static int emit(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider, String name,
                            String resultId, List<Pair<String, Integer>> inputs, Map<String, Integer> aspects,
                            int mbPerPoint, String research, int eut, int duration) {
        ItemStack result = item(resultId);
        if (result.isEmpty()) {
            Pollution.LOGGER.warn("[thaumic-industrial] skipping {}: result {} missing", name, resultId);
            return 0;
        }
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "industrial_infusion/thaumic/" + name),
                PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES);
        for (Pair<String, Integer> entry : inputs) {
            ItemStack stack = item(entry.getFirst());
            if (stack.isEmpty()) {
                Pollution.LOGGER.warn("[thaumic-industrial] skipping {}: {} missing", name, entry.getFirst());
                return 0;
            }
            builder.inputItems(stack, entry.getSecond());
        }
        for (Map.Entry<String, Integer> aspect : aspects.entrySet()) {
            FluidStack fluid = aspectFluid(aspect.getKey(), aspect.getValue() * mbPerPoint);
            if (fluid.isEmpty()) {
                Pollution.LOGGER.warn("[thaumic-industrial] skipping {}: unmapped aspect {}", name, aspect.getKey());
                return 0;
            }
            builder.inputFluids(fluid);
        }
        builder.outputItems(result)
                .duration(duration)
                .EUt(eut);
        MagicRecipeProperties.thaumcraftResearch(builder, research);
        builder.save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** TC recipe JSON scan *****//
    // ////////////////////////////////////

    private static int scanModFiles(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        Set<String> whitelist = new LinkedHashSet<>(DEFAULT_WHITELIST);
        whitelist.addAll(PollutionConfig.EXTRA_THAUMIC_INDUSTRIAL_WHITELIST.get());

        Set<String> seen = new HashSet<>();
        int converted = 0;
        int skippedUnmapped = 0;
        int skippedFluids = 0;
        int skippedNbt = 0;
        int skippedCaps = 0;
        int scanned = 0;
        for (var modFileInfo : net.minecraftforge.fml.ModList.get().getModFiles()) {
            // findResource goes through the mod file system, so jar-in-jar libraries resolve too
            // (a plain ZipFile on getFilePath() throws FileNotFoundException for nested jars).
            Path data = modFileInfo.getFile().findResource("data");
            if (!Files.isDirectory(data)) {
                continue;
            }
            List<Path> jsons;
            try (Stream<Path> walk = Files.walk(data)) {
                jsons = walk.filter(path -> path.toString().endsWith(".json")
                        && path.toString().replace('\\', '/').contains("/recipes/")).sorted().toList();
            } catch (Exception e) {
                Pollution.LOGGER.debug("[thaumic-industrial] cannot walk {}: {}",
                        modFileInfo.getFile().getFileName(), e.toString());
                continue;
            }
            for (Path path : jsons) {
                String name = "data/" + data.relativize(path).toString().replace('\\', '/');
                JsonObject json;
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (Exception broken) {
                    continue;
                }
                String type = json.has("type") && json.get("type").isJsonPrimitive()
                        ? json.get("type").getAsString() : "";
                if (!type.startsWith("thaumcraft:")) {
                    continue;
                }
                String resultId = resultId(json);
                if (resultId == null || !whitelist.contains(resultId) || !seen.add(name)) {
                    continue;
                }
                if (json.has("conditions") && !net.minecraftforge.common.crafting.CraftingHelper.processConditions(
                        json, "conditions", net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY)) {
                    continue;
                }
                scanned++;
                JsonObject report;
                try {
                    report = convert(provider, name, type, json, resultId);
                } catch (RuntimeException invalid) {
                    Pollution.LOGGER.warn("[thaumic-industrial] invalid recipe {}: {}", name, invalid.toString());
                    report = report("invalid-data");
                }
                if (report == null) {
                    converted++;
                } else {
                    String why = report.get("why").getAsString();
                    Pollution.LOGGER.info("[thaumic-industrial] skipped {} ({})", name, why);
                    switch (why) {
                        case "unmapped-aspect" -> skippedUnmapped++;
                        case "too-many-fluids" -> skippedFluids++;
                        case "nbt" -> skippedNbt++;
                        default -> skippedCaps++;
                    }
                }
            }
        }
        Pollution.LOGGER.info(
                "[thaumic-industrial] scan: {} whitelisted TC recipes, {} converted, skipped (unmapped aspect {}, "
                        + "fluid cap {}, nbt {}, io cap {})",
                scanned, converted, skippedUnmapped, skippedFluids, skippedNbt, skippedCaps);
        return converted;
    }

    /** @return null on success, or a single-entry report object with the skip reason */
    private static JsonObject convert(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider,
                                      String sourcePath, String type, JsonObject json, String resultId) {
        JsonObject result = json.getAsJsonObject("result");
        if (result.has("nbt") || (result.has("tag") && !result.has("item"))) {
            return report("nbt");
        }
        ItemStack out = item(result.get("item").getAsString(), result.has("count") ? result.get("count").getAsInt() : 1);
        if (out.isEmpty()) {
            return report("nbt");
        }

        List<Ingredient> itemInputs = new ArrayList<>();
        List<Integer> itemCounts = new ArrayList<>();
        // Never silently drop an unresolved ingredient: that would create a cheaper recipe.
        if (json.has("input_transformation") || json.has("retained_slots") || json.has("retainedSlots")
                || json.has("retained_ingredient_indices")) {
            return report("unsupported-transformation");
        }
        for (JsonObject ingredient : ThaumicRecipeData.inputs(type, json)) {
            if (!addIngredient(ingredient, itemInputs, itemCounts)) return report("missing-input");
        }

        Map<String, Integer> fluids = new LinkedHashMap<>();
        int total = 0;
        boolean arcane = type.startsWith("thaumcraft:arcane");
        JsonObject aspectObject = arcane ? json.getAsJsonObject("vis") : json.getAsJsonObject("aspects");
        if (aspectObject != null) {
            for (Map.Entry<String, JsonElement> aspect : aspectObject.entrySet()) {
                String id = aspect.getKey();
                int amount = aspect.getValue().getAsInt();
                if (amount == 0) continue;
                FluidStack stack = aspectFluid(id, ThaumicRecipeData.fluidAmount(amount, arcane));
                if (stack.isEmpty()) {
                    return report("unmapped-aspect");
                }
                fluids.merge(String.valueOf(ForgeRegistries.FLUIDS.getKey(stack.getFluid())), stack.getAmount(), Integer::sum);
                total = Math.addExact(total, amount);
            }
        }
        if (fluids.size() > MAX_FLUIDS) {
            return report("too-many-fluids");
        }
        if (itemInputs.size() > MAX_ITEMS) {
            return report("io-cap");
        }

        String ns = sourcePath.startsWith("data/") && sourcePath.indexOf('/', 5) > 0
                ? sourcePath.substring(5, sourcePath.indexOf('/', 5)) : "unknown";
        String tail = sourcePath.substring(sourcePath.indexOf('/', 5) + 1).replace(".json", "");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID,
                "industrial_infusion/thaumic/" + ns + "/" + tail.toLowerCase(Locale.ROOT));

        GTRecipeBuilder builder = GTRecipeBuilder.of(id, PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES);
        for (int index = 0; index < itemInputs.size(); index++) {
            builder.inputItems(itemInputs.get(index), itemCounts.get(index));
        }
        for (Map.Entry<String, Integer> fluid : fluids.entrySet()) {
            builder.inputFluids(FluidStackHelper.byId(fluid.getKey(), fluid.getValue()));
        }
        builder.outputItems(out)
                .duration(Math.max(100, total * 5))
                .EUt(GTValues.VA[GTValues.HV]);
        if (json.has("research") && !json.get("research").isJsonPrimitive()) return report("unsupported-research");
        String research = json.has("research") ? json.get("research").getAsString() : "";
        if (!research.isEmpty()) {
            MagicRecipeProperties.thaumcraftResearch(builder, research);
        }
        builder.save(provider);
        return null;
    }

    private static boolean addIngredient(JsonObject entry, List<Ingredient> inputs, List<Integer> counts) {
        int count = entry.has("count") ? entry.get("count").getAsInt() : 1;
        if (entry.has("tag")) {
            ResourceLocation id = ResourceLocation.tryParse(entry.get("tag").getAsString());
            if (id == null) return false;
            inputs.add(Ingredient.of(TagKey.create(Registries.ITEM, id)));
            counts.add(count);
            return true;
        }
        ItemStack stack = item(entry.get("item").getAsString());
        if (stack.isEmpty()) return false;
        inputs.add(Ingredient.of(stack));
        counts.add(count);
        return true;
    }

    private static String resultId(JsonObject json) {
        JsonElement result = json.get("result");
        if (result == null || !result.isJsonObject() || !result.getAsJsonObject().has("item")) return null;
        return result.getAsJsonObject().get("item").getAsString();
    }

    private static JsonObject report(String why) {
        JsonObject object = new JsonObject();
        object.addProperty("why", why);
        return object;
    }

    private static ItemStack item(String id) {
        return item(id, 1);
    }

    private static ItemStack item(String id, int count) {
        Item resolved = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
        return resolved == null ? ItemStack.EMPTY : new ItemStack(resolved, count);
    }

    private static List<Pair<String, Integer>> pairs(Object... idAndCount) {
        List<Pair<String, Integer>> result = new ArrayList<>();
        for (int index = 0; index + 1 < idAndCount.length; index += 2) {
            result.add(Pair.of((String) idAndCount[index], (Integer) idAndCount[index + 1]));
        }
        return result;
    }

    /** @return {@code mb} of the aspect's infused fluid, or EMPTY when the aspect has no Pollution fluid */
    private static FluidStack aspectFluid(String aspectKey, int mb) {
        AspectId aspect;
        try {
            aspect = AspectId.parse(aspectKey);
        } catch (RuntimeException e) {
            return FluidStack.EMPTY;
        }
        return PollutionAspectMapping.materialOf(aspect)
                .filter(material -> material.hasFluid())
                .map(material -> material.getFluid(mb))
                .orElse(FluidStack.EMPTY);
    }

    /** Tiny indirection so fluid lookups stay close to Forge APIs without leaking helpers. */
    private static final class FluidStackHelper {
        private FluidStackHelper() {}

        static FluidStack byId(String fluidId, int amount) {
            var fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(fluidId));
            return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
        }
    }
}
