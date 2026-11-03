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
import meowmel.pollution.common.item.PollutionItems;
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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * 配方），在本类中直接按同一规则落成工业注魔配方。</p>
 */
public final class ThaumicIndustrialRecipes {

    /** MB of aspect fluid per essentia point (exchange/smelter rate). */
    private static final int MB_PER_POINT = 144;
    /** MB per vis point for arcane recipes (aura is renewable and cheaper). */
    private static final int MB_PER_VIS_POINT = 36;
    private static final int MAX_ITEMS = 25;
    private static final int MAX_FLUIDS = 8;
    /** Default whitelist: the four templates are handled directly (dynamic recipes). */
    private static final Set<String> DEFAULT_WHITELIST = Set.of(
            "thaumcraft:thaumium_ingot", "thaumcraft:void_ingot", "thaumcraft:quicksilver",
            "thaumcraft:balanced_shard", "thaumcraft:crystal_essence", "thaumcraft:primordial_pearl",
            "thaumcraft:salis_mundus",
            "thaumadds:mithminite_ingot", "thaumadds:adaminite_ingot", "thaumadds:mithrillium_ingot",
            "tainted_magic:shadow_metal_ingot", "tainted_magic:creation_shard",
            "isorropia:ethereal_shard",
            "thaumictinkerer:ichorium", "thaumictinkerer:ichor");

    private ThaumicIndustrialRecipes() {}

    public static void init(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        int converted = 0;
        converted += templates(provider);
        converted += scanModFiles(provider);
        Pollution.LOGGER.info("[thaumic-industrial] registered {} industrial infusion recipes from TC JSONs",
                converted);
    }

    // ////////////////////////////////////
    // ***** the four circuit templates *****//
    // ////////////////////////////////////

    /**
     * Industrial twins of {@code ThaumicCircuitRecipes#templates}: same items,
     * aspects as fluids, no instability, machine-owner research INFUSION.
     */
    private static int templates(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        int added = 0;
        // template 2: void_ingot + crystal_essence x2 + quicksilver, praecantatio 16 / metallum 8 / vacuos 8
        added += template(provider, "template_2", PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack(),
                items("thaumcraft:void_ingot"), pairs("thaumcraft:crystal_essence", 2, "thaumcraft:quicksilver", 1),
                Map.of("praecantatio", 16, "metallum", 8, "vacuos", 8));
        // template 3: mithminite + shadow_metal + ethereal_shard x2, praecantatio 24 / alienis 16 / metallum 12
        added += template(provider, "template_3", PollutionItems.BLOOD_CIRCUIT_ULTIMATE.asStack(),
                items("thaumadds:mithminite_ingot", "tainted_magic:shadow_metal_ingot"),
                pairs("isorropia:ethereal_shard", 2),
                Map.of("praecantatio", 24, "alienis", 16, "metallum", 12));
        // template 4: mithrillium + creation_shard + primordial_pearl, praecantatio 32 / alienis 24 / auram 24
        added += template(provider, "template_4", PollutionItems.BLOOD_CIRCUIT_SUPREME.asStack(),
                items("thaumadds:mithrillium_ingot", "thaumcraft:primordial_pearl"),
                pairs("tainted_magic:creation_shard", 1),
                Map.of("praecantatio", 32, "alienis", 24, "auram", 24));
        return added;
    }

    /** @return 1 when the template recipe was emitted */
    private static int template(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider, String name,
                                ItemStack result, List<String> itemInputs, List<Pair<String, Integer>> countedInputs,
                                Map<String, Integer> aspects) {
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "industrial_infusion/thaumic/" + name),
                PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES);
        if (result.isEmpty()) {
            Pollution.LOGGER.warn("[thaumic-industrial] skipping template {}: result missing", name);
            return 0;
        }
        for (String id : itemInputs) {
            ItemStack stack = item(id);
            if (stack.isEmpty()) {
                Pollution.LOGGER.warn("[thaumic-industrial] skipping template {}: {} missing", name, id);
                return 0;
            }
            builder.inputItems(stack);
        }
        for (Pair<String, Integer> entry : countedInputs) {
            ItemStack stack = item(entry.getFirst());
            if (stack.isEmpty()) {
                Pollution.LOGGER.warn("[thaumic-industrial] skipping template {}: {} missing",
                        name, entry.getFirst());
                return 0;
            }
            builder.inputItems(stack, entry.getSecond());
        }
        int total = 0;
        for (Map.Entry<String, Integer> aspect : aspects.entrySet()) {
            FluidStack fluid = aspectFluid(aspect.getKey(), aspect.getValue());
            if (fluid.isEmpty()) {
                Pollution.LOGGER.warn("[thaumic-industrial] skipping template {}: unmapped aspect {}",
                        name, aspect.getKey());
                return 0;
            }
            builder.inputFluids(fluid);
            total += aspect.getValue();
        }
        builder.outputItems(result)
                .duration(Math.max(100, total * 5))
                .EUt(GTValues.VA[GTValues.HV]);
        MagicRecipeProperties.thaumcraftResearch(builder, "INFUSION");
        builder.save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** TC recipe JSON scan *****//
    // ////////////////////////////////////

    private static int scanModFiles(Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        Set<String> whitelist = new LinkedHashSet<>(DEFAULT_WHITELIST);
        whitelist.addAll(PollutionConfig.EXTRA_THAUMIC_INDUSTRIAL_WHITELIST.get());

        int converted = 0;
        int skippedUnmapped = 0;
        int skippedFluids = 0;
        int skippedNbt = 0;
        int skippedCaps = 0;
        int scanned = 0;
        for (var modFileInfo : net.minecraftforge.fml.ModList.get().getModFiles()) {
            Path file = modFileInfo.getFile().getFilePath();
            try (ZipFile zip = new ZipFile(file.toFile())) {
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (entry.isDirectory() || !name.startsWith("data/") || !name.endsWith(".json")
                            || !name.contains("/recipes/")) {
                        continue;
                    }
                    JsonObject json;
                    try (InputStream in = zip.getInputStream(entry)) {
                        json = JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                                .getAsJsonObject();
                    } catch (Exception broken) {
                        continue;
                    }
                    String type = json.has("type") && json.get("type").isJsonPrimitive()
                            ? json.get("type").getAsString() : "";
                    if (!type.startsWith("thaumcraft:")) {
                        continue;
                    }
                    String resultId = resultId(json);
                    if (resultId == null || !whitelist.contains(resultId)) {
                        continue;
                    }
                    scanned++;
                    JsonObject report = convert(provider, name, type, json, resultId);
                    if (report == null) {
                        converted++;
                    } else {
                        String why = report.get("why").getAsString();
                        switch (why) {
                            case "unmapped-aspect" -> skippedUnmapped++;
                            case "too-many-fluids" -> skippedFluids++;
                            case "nbt" -> skippedNbt++;
                            default -> skippedCaps++;
                        }
                    }
                }
            } catch (Exception e) {
                Pollution.LOGGER.warn("[thaumic-industrial] failed to scan {}: {}",
                        file.getFileName(), e.toString());
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
        if (type.equals("thaumcraft:infusion")) {
            if (nbt(json.getAsJsonObject("central"))) {
                return report("nbt");
            }
            addIngredient(json.getAsJsonObject("central"), itemInputs, itemCounts);
            for (JsonElement component : json.getAsJsonArray("components")) {
                if (nbt(component.getAsJsonObject())) {
                    return report("nbt");
                }
                addIngredient(component.getAsJsonObject(), itemInputs, itemCounts);
            }
        } else if (type.equals("thaumcraft:crucible")) {
            if (nbt(json.getAsJsonObject("catalyst"))) {
                return report("nbt");
            }
            addIngredient(json.getAsJsonObject("catalyst"), itemInputs, itemCounts);
        } else if (type.equals("thaumcraft:arcane_shapeless")) {
            for (JsonElement ingredient : json.getAsJsonArray("ingredients")) {
                if (nbt(ingredient.getAsJsonObject())) {
                    return report("nbt");
                }
                addIngredient(ingredient.getAsJsonObject(), itemInputs, itemCounts);
            }
        } else { // arcane_shaped: pattern/key
            JsonObject key = json.getAsJsonObject("key");
            Set<String> used = usedSymbols(json.getAsJsonArray("pattern"));
            for (Map.Entry<String, JsonElement> symbol : key.entrySet()) {
                if (!used.contains(symbol.getKey())) {
                    continue;
                }
                JsonObject entry = symbol.getValue().getAsJsonObject();
                if (nbt(entry)) {
                    return report("nbt");
                }
                addIngredient(entry, itemInputs, itemCounts);
            }
        }

        Map<String, Integer> fluids = new LinkedHashMap<>();
        int total = 0;
        boolean arcane = type.startsWith("thaumcraft:arcane");
        JsonObject aspectObject = arcane ? json.getAsJsonObject("vis") : json.getAsJsonObject("aspects");
        if (aspectObject != null) {
            int perPoint = arcane ? MB_PER_VIS_POINT : MB_PER_POINT;
            for (Map.Entry<String, JsonElement> aspect : aspectObject.entrySet()) {
                String id = aspect.getKey();
                int amount = aspect.getValue().getAsInt();
                FluidStack stack = aspectFluid(id, amount * perPoint);
                if (stack.isEmpty()) {
                    return report("unmapped-aspect");
                }
                fluids.merge(String.valueOf(ForgeRegistries.FLUIDS.getKey(stack.getFluid())), stack.getAmount(), Integer::sum);
                total += amount;
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
        String research = json.has("research") && json.get("research").isJsonPrimitive()
                ? json.get("research").getAsString() : "";
        if (!research.isEmpty()) {
            MagicRecipeProperties.thaumcraftResearch(builder, research);
        }
        builder.save(provider);
        return null;
    }

    private static Set<String> usedSymbols(JsonArray pattern) {
        Set<String> used = new LinkedHashSet<>();
        for (JsonElement row : pattern) {
            for (char symbol : row.getAsString().toCharArray()) {
                if (symbol != ' ') {
                    used.add(String.valueOf(symbol));
                }
            }
        }
        return used;
    }

    private static void addIngredient(JsonObject entry, List<Ingredient> inputs, List<Integer> counts) {
        int count = entry.has("count") ? entry.get("count").getAsInt() : 1;
        if (entry.has("tag")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(entry.get("tag").getAsString()));
            if (tag != null) {
                inputs.add(Ingredient.of(tag));
                counts.add(count);
            }
            return;
        }
        ItemStack stack = item(entry.get("item").getAsString(), count);
        if (!stack.isEmpty()) {
            inputs.add(Ingredient.of(stack));
            counts.add(count);
        }
    }

    private static boolean nbt(JsonObject entry) {
        return entry == null || entry.has("nbt");
    }

    private static String resultId(JsonObject json) {
        if (!json.has("result") || !json.getAsJsonObject("result").has("item")) {
            return null;
        }
        return json.getAsJsonObject("result").get("item").getAsString();
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

    private static List<String> items(String... ids) {
        return List.of(ids);
    }

    private static List<Pair<String, Integer>> pairs(Object... idAndCount) {
        List<Pair<String, Integer>> result = new ArrayList<>();
        for (int index = 0; index + 1 < idAndCount.length; index += 2) {
            result.add(Pair.of((String) idAndCount[index], (Integer) idAndCount[index + 1]));
        }
        return result;
    }

    /** @return the aspect fluid stack (144 mB per point basis already applied), or EMPTY when unmapped */
    private static FluidStack aspectFluid(String aspectKey, int points) {
        AspectId aspect;
        try {
            aspect = AspectId.parse(aspectKey);
        } catch (RuntimeException e) {
            return FluidStack.EMPTY;
        }
        return PollutionAspectMapping.materialOf(aspect)
                .filter(material -> material.hasFluid())
                .map(material -> material.getFluid(points * MB_PER_POINT))
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
