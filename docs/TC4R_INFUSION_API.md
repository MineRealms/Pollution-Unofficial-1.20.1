# TC4R Infusion / Arcane Crafting API

Research-only audit of the Thaumcraft 4R (`dev.tc4port.thaumcraft`, jar
`local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar`)
crafting APIs relevant to the infusion/arcane recipes that were skipped when the
1.12.2 recipe loaders were ported. Every statement was verified with `jar tf`,
`javap -p -c` and by reading the datapack JSONs shipped inside the jar.

## Headline finding

**A usable API exists.** TC4R has no `ThaumcraftApi.addInfusionCraftingRecipe`
equivalent, but it exposes the complete 1.20.1-native route:

- Infusion and arcane crafting are **datapack recipes** with registered
  `RecipeType`/`RecipeSerializer` pairs (`thaumcraft:infusion`,
  `thaumcraft:arcane_shaped`, `thaumcraft:arcane_shapeless`, ...).
- The recipe classes and their serializers are **public**, with public
  constructors and a public `Codec<ItemStack>` for results, so a mod can build
  recipes in Java and inject them as `FinishedRecipe`s into the server's
  dynamic data pack. Pollution already does exactly this through GregTech's
  `GTDynamicDataPack.addRecipe(FinishedRecipe)` (the `Consumer<FinishedRecipe>`
  provider in `PollutionGTAddon#addRecipes`), so no extra plumbing is needed.
- **There is no Java "register recipe" method** (`InfusionInputPatchApi` only
  patches existing infusion inputs; `InfusionCentralTransformationRegistry` only
  registers central transformations).

Pollution's infusion port lives in `loaders/recipes/InfusionRecipes.java` and
uses route 2 below.

---

## 1. Registries and JSON types

`dev.tc4port.thaumcraft.registry.TCRecipes` (public) exposes:

```java
RegistryObject<RecipeType<InfusionRecipe>>                  INFUSION
RegistryObject<RecipeSerializer<InfusionRecipe>>            INFUSION_SERIALIZER
RegistryObject<RecipeType<ArcaneCraftingRecipe>>            ARCANE
RegistryObject<RecipeSerializer<ArcaneRecipe>>              ARCANE_SHAPED_SERIALIZER
RegistryObject<RecipeSerializer<ShapelessArcaneRecipe>>     ARCANE_SHAPELESS_SERIALIZER
RegistryObject<RecipeSerializer<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT_SERIALIZER
RegistryObject<RecipeType<CrucibleRecipe>>                  CRUCIBLE / CRUCIBLE_SERIALIZER
RegistryObject<RecipeSerializer<WandAssemblyRecipe>>        WAND_ASSEMBLY_SERIALIZER
RegistryObject<RecipeSerializer<BannerDyeRecipe>>           BANNER_DYE_SERIALIZER
RegistryObject<RecipeSerializer<JarLabelRecipe>>            JAR_LABEL_SERIALIZER
RegistryObject<RecipeSerializer<CommonMetalCraftingRecipe>> COMMON_METAL_CRAFTING_SERIALIZER
RegistryObject<RecipeSerializer<CommonMetalSmeltingRecipe>> COMMON_METAL_SMELTING_SERIALIZER
```

Datapack `type` strings verified against the 63 infusion / 104+5 arcane / 24
enchantment JSONs shipped in the jar:

| JSON `type` | Serializer | Recipe class |
|---|---|---|
| `thaumcraft:infusion` | `TCRecipes.INFUSION_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.InfusionRecipe` |
| `thaumcraft:arcane_shaped` | `TCRecipes.ARCANE_SHAPED_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.ArcaneRecipe` |
| `thaumcraft:arcane_shapeless` | `TCRecipes.ARCANE_SHAPELESS_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.ShapelessArcaneRecipe` |
| `thaumcraft:infusion_enchantment` | `TCRecipes.INFUSION_ENCHANTMENT_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.InfusionEnchantmentRecipe` |
| `thaumcraft:crucible` | `TCRecipes.CRUCIBLE_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.CrucibleRecipe` |
| `thaumcraft:wand_assembly` | `TCRecipes.WAND_ASSEMBLY_SERIALIZER` | `dev.tc4port.thaumcraft.recipe.WandAssemblyRecipe` |

### Infusion JSON schema (verified)

```json
{
  "type": "thaumcraft:infusion",
  "research": "RUNICARMOR",              // optional; ResearchKey string, plain = thaumcraft namespace
  "central": { "item": "thaumcraft:mundane_ring" },   // Ingredient, required
  "components": [ { "item": "..." }, { "tag": "..." } ], // Ingredient list, optional
  "result": { "item": "thaumcraft:runic_ring", "count": 1 }, // optional (see input_transformation)
  "instability": 3,                      // optional, default 0
  "aspects": { "tutamen": 10, "praecantatio": 25 },  // optional
  "input_patch": { "goggles": 1 },       // optional, max 4 entries
  "input_transformation": "namespace:path" // optional, replaces result when central must transform
}
```

- Field names confirmed from the `InfusionRecipe$Serializer` bytecode
  (`research`, `central`, `components`, `result`, `input_patch`,
  `input_transformation`, `instability`, `aspects`).
- `result` is decoded with the public
  `dev.tc4port.thaumcraft.recipe.InfusionRecipe$Serializer.RESULT_CODEC`
  (`= dev.tc4port.thaumcraft.nativeimpl.RecipeValuePlatform.ITEM_CODEC =
  dev.tc4port.thaumcraft.nativeimpl.ForgeRecipeItemCodec.CODEC`). The accepted
  keys are `id` / `item`, `count`, `nbt`, `components` (the shipped files use
  `item`).
- Ingredients use the standard `Ingredient` JSON (`item`/`tag`); the codec is
  `RecipeValuePlatform.INGREDIENT_NONEMPTY` (`Ingredient.fromJson`).
- Aspects are strings parsed by `AspectId.parse`; plain names default to the
  `thaumcraft` namespace. The 48 core aspects shipped in
  `data/thaumcraft/thaumcraft/aspects/default.json` are:
  `aer, terra, ignis, aqua, ordo, perditio, vacuos, lux, tempestas, motus,
  gelum, vitreus, victus, venenum, potentia, permutatio, metallum, mortuus,
  volatus, tenebrae, spiritus, sano, iter, alienis, praecantatio, auram,
  vitium, limus, herba, arbor, bestia, corpus, exanimis, cognitio, sensus,
  humanus, messis, perfodio, instrumentum, meto, telum, tutamen, fames,
  lucrum, fabrico, pannus, machina, vinculum`.
- Limits: `ThaumcraftApiCommon.MAX_INFUSION_INSTABILITY = 499`,
  `MAX_INFUSION_COMPONENTS = 288`, `InfusionRecipe.MAX_INPUT_PATCH_ENTRIES = 4`.

### Arcane JSON schema (verified)

```json
{
  "type": "thaumcraft:arcane_shaped",
  "research": "ADVALCHEMYFURNACE",
  "pattern": [ "VAV", "APA", "VAV" ],
  "key": { "A": { "item": "..." } },
  "result": { "count": 4, "item": "..." },
  "vis": { "aqua": 10, "ordo": 30 },
  "retain": [ 4 ]                        // optional slot indices whose items are kept
}
```

`thaumcraft:arcane_shapeless` uses `ingredients` instead of `pattern`/`key`.
Arcane vis is keyed by `VisChannel` names (the six primal aspects).

---

## 2. Public Java classes (code registration path)

```java
// Recipe class, public constructors:
public InfusionRecipe(ResearchRequirement, Ingredient central, List<Ingredient> components,
                      ItemStack result, Map<String,Integer> inputPatch, int instability,
                      Map<AspectId,Integer> aspects);
public InfusionRecipe(ResearchRequirement, Ingredient central, List<Ingredient> components,
                      ResourceLocation inputTransformation, int instability,
                      Map<AspectId,Integer> aspects);
public static final Codec<ItemStack> InfusionRecipe.Serializer.RESULT_CODEC;

public ArcaneRecipe(ResearchRequirement, RecipePattern, ItemStack, Map<VisChannel,Integer>, List<Integer>);
public ShapelessArcaneRecipe(ResearchRequirement, List<Ingredient>, ItemStack, Map<VisChannel,Integer>);

// Research gating:
public final class ResearchRequirement {           // record
    public static ResearchRequirement none();
    public static ResearchRequirement required(ResearchKey);
}
public ResearchKey ResearchKey.parse(String);      // "INFUSION" -> thaumcraft:INFUSION
```

How to register from code (what Pollution does): build a `FinishedRecipe`
whose `serializeRecipeData` emits the schema above and whose `getType()`
returns `TCRecipes.INFUSION_SERIALIZER.get()`, then hand it to the GT addon
recipe provider. GregTech's `GTDynamicDataPack.addRecipe(FinishedRecipe)`
calls `FinishedRecipe.serializeRecipe()` and writes the JSON into the
server's built-in `data/pollution/recipes/...` virtual pack, where the normal
`RecipeManager` loads it through the TC4R serializer. There is no TC4R-side
"add recipe" call.

The same route works for arcane crafting by using
`TCRecipes.ARCANE_SHAPED_SERIALIZER.get()` / `ARCANE_SHAPELESS_SERIALIZER.get()`
and the arcane schema above.

### Public code hooks that are *not* recipe registration

```java
// Patch the input stacks of an existing infusion recipe (e.g. runic augmentation):
public final class InfusionInputPatchApi {
    public static Registration register(ResourceLocation, InfusionInputPatch);
    public static void validate(ResourceLocation, int, int);
    public static void apply(ResourceLocation, ItemStack, int, HolderLookup.Provider);
}
public interface InfusionInputPatch {
    void validate(int index, int componentCount);
    void apply(ItemStack central, int index, HolderLookup.Provider registries);
}

// Central-transformation recipes (result-less infusions):
public static Registration ThaumcraftApiCommon.registerInfusionCentralTransformation(
        ResourceLocation, InfusionCentralTransformation);
public interface InfusionCentralTransformation {
    default boolean matches(ItemStack);
    ItemStack transform(ItemStack, HolderLookup.Provider);
    default int instability(ItemStack, int);
    default AspectAmounts aspects(ItemStack, AspectAmounts);
}

// Read-only queries:
public static List<ArcaneCraftingRecipe> ArcaneCraftingApi.recipes(Level);
public static Optional<ArcaneCraftingRecipe> ArcaneCraftingApi.byId(Level, ResourceLocation);
public static List<InfusionEnchantmentApi.Entry> InfusionEnchantmentApi.recipes(Level);
```

`dev.tc4port.thaumcraft.api.recipe.NativeRecipeIds.bind/construct` exists but is
an internal helper for binding IDs during construction; it does not register
recipes either.

---

## 3. Practical notes

1. **Research**: `research` is optional. Omitting it makes the recipe always
   available; Pollution's port sets `"INFUSION"` to match the upstream
   `INFUSION@2` gate (`ResearchKey.parse("INFUSION")` resolves to
   `thaumcraft:INFUSION`, which TC4R ships).
2. **Items without a ported 1.12 counterpart**: the TC4 items
   `vis_resonator`, `morphic_resonator`, `causality_collapser` and the blocks
   `vis_battery`, `matrix_cost`, `matrix_speed` were not ported to TC4R, so the
   upstream recipes that need them stay skipped (beam cores, wire coils, vis
   hatches, `industrial_infusion`). TC4R does provide the crystal clusters
   (`ThaumcraftContent.block("air_crystal_cluster")`, ...), `alumentum`,
   `alchemical_furnace`, `alchemical_construct`, `arcane_worktable`,
   `runic_matrix`, `wand_recharge_pedestal`, greatwood/silverwood trees and the
   wand parts, so recipes that only need those are portable.
3. **Result-less recipes**: `runic_augmentation_infusion.json` and the
   `input_patch` fortress-helmet variants omit `result`; they are central
   transformations, not standard infusions. Pollution does not port those.
4. **No code-side conditions**: the dynamic-pack route does not evaluate Forge
   recipe conditions. Recipes referencing optional mod content must be
   registered conditionally in Java instead (Pollution's provider runs when all
   hard dependencies are present).
5. **Consumers**: JEI/Jade read the recipes through the normal
   `RecipeManager`; no extra registration is needed.
