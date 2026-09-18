# Phase 6C — Thaumcraft 4R Addon API Reference

Research-only audit of the four Thaumcraft 4R addon ports that are now dependencies of
Pollution-Unofficial-1.20.1. Every statement below was verified with `jar tf`, `javap -public`
and/or grep against the actual jars/sources on 2026-09-18. Nothing here is inferred from
upstream documentation.

Verified jars:

| Addon | Jar | modId | Version string |
|---|---|---|---|
| Forbidden Magic | `local-repo/dev/tc4port/forbidden-magic/0.1.0-20711/forbidden-magic-0.1.0-20711.jar` | `forbidden_magic` | `0.574-1.20.1-port.0.1.0-20711` |
| Tainted Magic | `local-repo/dev/tc4port/tainted-magic/0.1.0-20711/tainted-magic-0.1.0-20711.jar` | `tainted_magic` | `8.1.1-1.20.1-port.0.1.0-20711` |
| Thaumic Energistics | `local-repo/dev/tc4port/thaumic-energistics/0.1.0-20711/thaumic-energistics-0.1.0-20711.jar` | `thaumicenergistics` | `1.1.3.0-port.0.1.0-20711` |
| Thaumic Tinkerer | `local-repo/dev/tc4port/thaumic-tinkerer/0.1.0-20711/thaumic-tinkerer-0.1.0-20711.jar` | `thaumictinkerer` | `2.5-1.20.1-port.0.1.0-20711` |

All four are wired in `build.gradle` as `compileOnly` + `runtimeOnly`
(`fg.deobf("dev.tc4port:${addon}:${tc_addons_version}") { transitive = false }`).
The Thaumcraft core jar is `dev.tc4port:thaumcraft-forge:0.1.0-20711`.

`META-INF/mods.toml` dependency declarations (verified):

- `forbidden_magic` requires `thaumcraft`; declares optional `curios`, `thaumictinkerer`,
  `botania`, `bloodmagic`, `twilightforest`.
- `tainted_magic` requires `thaumcraft`; declares optional `curios`.
- `thaumicenergistics` requires `ae2` `[15.4.10,16)` and `thaumcraft`; AE2 is a hard dependency.
- `thaumictinkerer` requires `thaumcraft`; declares optional `curios`, `jade`,
  `computercraft`, `ae2`, `botania`.

---

## 0. Headline finding — public API surface is very small

- **Thaumic Tinkerer is the only addon with a dedicated `api/` package:**
  `dev.tc4port.thaumictinkerer.api.enchantment` (2 classes). Its `OsmoticEnchantmentApi`
  is the only documented cross-mod registration hook found in any of the four jars.
- **Forbidden Magic, Tainted Magic and Thaumic Energistics expose no `api/` package at all.**
  Their integration-facing classes live under `integration/`, `aspect/`, `research/`,
  `registry/` and `vis/`/`storage/` and are mostly ordinary implementation classes that
  happen to be public. Several Thaumic Energistics strategy classes are package-private and
  therefore **not callable from Pollution** (see §3).
- All addon content is registered from the addon's own mod bus; Pollution should not need to
  call addon registration code except for the specific public hooks listed below. Most
  cross-mod extension in TC4R is **datapack-driven** (research JSON, aspects JSON, object
  aspects, focus catalogs, recipes) rather than Java API driven.

---

## 1. Upstream 1.12.2 Pollution reference audit

The requested grep was run over `H:\MinecraftMods\Pollution\src\main\java`:

```
import .*(forbidden|tainted|thaumic|energistics|tinkerer)   (case-insensitive)
```

**Result: zero matching files.** A broader grep for the package/class names
(`spiteful`, `TaintedMagic`, `ThaumicEnergistics`, `ThaumicTinkerer`, `ForbiddenMagic`,
lowercase variants) also returned zero files, and a broad grep for the words
`forbidden|tainted|tinkerer|energistics|infusion` found no addon references (only unrelated
hits such as `MagicProcessTag.INFUSION` and a private `isForbiddenCatalyst` helper).

Reason: the upstream 1.12.2 mod targets **Thaumcraft 6** (`thaumcraft.api.*` imports) and the
four addons audited here are ports of **Thaumcraft 4 / 1.7.10-era** addons. Upstream never
depended on them. Therefore there is no upstream file that "imports" these addons.
The list below is the **56 upstream files that import Thaumcraft at all** (verified with
`import\s+thaumcraft|import\s+.*thaum`, case-insensitive), plus the 3 tracker-named Phase 6C
target files `TCAspects.java`, `ForgeAlchemyRecipes.java` and `CompoundAspectRecipes.java`
(named in `docs/MIGRATION_TRACKER.md` §5.15, but importing neither Thaumcraft nor any addon
today). Together these are the integration surfaces Phase 6C will touch.

### 1.1 Upstream Thaumcraft-facing files (the Phase 6C integration surface)

Core integration / module layer (direct 6C-2 targets):

- `meowmel/pollution/integration/thaumcraft/ThaumcraftModule.java`
- `meowmel/pollution/integration/thaumcraft/TCAspects.java` (contains `// todo addons?` at the
  end of the enum — the obvious place to map addon aspects)
- `meowmel/pollution/api/utils/DummyAspectEventProxy.java`
- `meowmel/pollution/api/utils/POAspectToGtFluidList.java`
- `meowmel/pollution/common/lib/GTEssentiaHandler.java`
- `meowmel/pollution/mixin/thaumcraft/ConfigAspectsMixin.java`
- `meowmel/pollution/mixin/thaumcraft/CommonInternalsMixin.java`
- `meowmel/pollution/mixin/thaumcraft/ThaumcraftApiHelperMixin.java`

Recipe loaders (direct 6C-3 targets):

- `meowmel/pollution/loaders/recipes/ForgeAlchemyRecipes.java` (20,343 bytes; the Phase 6C-3
  "ForgeAlchemyRecipes" file — currently only uses `PORecipeMaps.FORGE_ALCHEMY_RECIPES` and
  GT/Botania/Blood Magic content, **not** any Forbidden Magic class)
- `meowmel/pollution/loaders/recipes/CompoundAspectRecipes.java`
- `meowmel/pollution/loaders/recipes/ThaumcraftRecipes.java`
- `meowmel/pollution/loaders/recipes/MagicChemicalRecipes.java`
- `meowmel/pollution/loaders/recipes/MagicGCYMRecipes.java`
- `meowmel/pollution/loaders/recipes/MagicIntegrationRecipes.java`
- `meowmel/pollution/loaders/recipes/CircuitManager.java`
- `meowmel/pollution/loaders/recipes/ConstellationTowerRecipes.java`
- `meowmel/pollution/loaders/recipes/StarstreamNexusRecipes.java`
- `meowmel/pollution/loaders/recipes/NodeFusionRecipes.java`
- `meowmel/pollution/loaders/recipes/FleshTreeRecipes.java`
- `meowmel/pollution/loaders/recipes/CrystalLine.java`
- `meowmel/pollution/loaders/recipes/BloodAltar.java`
- `meowmel/pollution/api/recipes/builder/IndustrialInfusionBuilder.java`

Machines / multiblocks (6C-4 targets):

- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityIndustrialInfusion.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityInfusedExchange.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityEssenceSmelter.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityGtEssenceSmelter.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityEssenceCollector.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityCentralVisTower.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityLargeNodeGenerator.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityMagicFusionReactor.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityNodeFusionReactor.java`
- `meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityFluxClear.java`
- `meowmel/pollution/common/metatileentity/multiblock/bot/MetaTileEntityManaInfusionReactor.java`
- `meowmel/pollution/common/metatileentity/multiblockpart/MetaTileEntityVisHatch.java`
- `meowmel/pollution/common/metatileentity/single/MetaTileEntityFluxClear.java`
- `meowmel/pollution/common/metatileentity/single/MetaTileEntityFluxPromotedFuelCell.java`
- `meowmel/pollution/common/metatileentity/single/MetaTileEntityVisGenerator.java`
- `meowmel/pollution/common/metatileentity/single/MetaTileEntityVisProvider.java`
- `meowmel/pollution/common/metatileentity/storage/MetaTileEntityAspectTank.java`
- `meowmel/pollution/common/block/tile/TileEntityMineralExtractor.java`
- `meowmel/pollution/api/capability/ipml/POMultiblockCleanVisRecipeLogic.java`
- `meowmel/pollution/mixin/gregtech/MixinMetaTileEntity.java`

Items / world / client:

- `meowmel/pollution/common/items/armor/GogglesNano.java`
- `meowmel/pollution/common/items/armor/GogglesQuantum.java`
- `meowmel/pollution/common/items/behaviors/Tarots.java`
- `meowmel/pollution/common/items/behaviors/VisCheckerBehavior.java`
- `meowmel/pollution/common/warpevent/FluxWarpManager.java`
- `meowmel/pollution/common/block/blocks/BlockFleshFlower.java`
- `meowmel/pollution/common/block/blocks/BlockFleshPlant.java`
- `meowmel/pollution/common/block/rainbow/BlockRainbowLeaves.java`
- `meowmel/pollution/common/block/rainbow/RainbowTreeRegistration.java`
- `meowmel/pollution/client/gui/QuantumAspectTank/AspectImage.java`
- `meowmel/pollution/client/gui/QuantumAspectTank/AspectImageWidget.java`
- `meowmel/pollution/client/gui/QuantumAspectTank/QuantumAspectTankLockWidget.java`
- `meowmel/pollution/client/gui/QuantumAspectTank/QuantumAspectTankMainWidget.java`
- `meowmel/pollution/client/gui/QuantumAspectTank/QuantumAspectTankWidget.java`
- `meowmel/pollution/client/textures/custom/AspectStorageRenderer.java`
- `meowmel/pollution/client/objmodels/ObjModels.java`
- `meowmel/pollution/client/tesr/TesrMagicCircle.java`

### 1.2 Current 1.20.1 project state

A grep of `Pollution-Unofficial-1.20.1/src` for
`dev.tc4port.(forbiddenmagic|taintedmagic|thaumictinkerer)` and `dev.thaumicenergistics`
returned **zero files**. Only the TC core (`dev.tc4port.thaumcraft.*`) is referenced so far.
So none of the four addons is used by the port yet; this document is the starting point for
Phase 6C-2/6C-3/6C-4.

---

## 2. Forbidden Magic (`forbidden_magic`)

### 2.1 Public API packages

**None.** There is no `api/` package. Integration-facing public classes:

| Class (FQN) | What it is |
|---|---|
| `dev.tc4port.forbiddenmagic.integration.TC4AspectIds` | 30 `AspectId` constants for TC4 core aspects (AER, TERRA, …, MACHINA) |
| `dev.tc4port.forbiddenmagic.integration.TC4RegistrationStore` | registration replacement hook |
| `dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects` | 7 sin `AspectId` constants: INFERNUS, IRA, GULA, INVIDIA, SUPERBIA, DESIDIA, LUXURIA |
| `dev.tc4port.forbiddenmagic.aspect.ForbiddenAspectMappings` | `register()` — wires the sin aspects into core mappings |
| `dev.tc4port.forbiddenmagic.research.ForbiddenResearch` | research key constants + availability checks |
| `dev.tc4port.forbiddenmagic.common.ForbiddenRecipeExtensions` | `register()` |
| `dev.tc4port.forbiddenmagic.common.ForbiddenEnchantments` | enchantment math helpers (see 2.3) |
| `dev.tc4port.forbiddenmagic.common.ForbiddenWandBehaviors` | `register()` |
| `dev.tc4port.forbiddenmagic.common.HellfireWandTrigger` | `VisCost COST`, `register()` |
| `dev.tc4port.forbiddenmagic.registry.*` | `FMItems`, `FMBlocks`, `FMEnchantments`, `FMDataComponents`, `FMTags`, `FMConditions`, `FMCreativeTabs` |
| `dev.tc4port.forbiddenmagic.config.FMServerConfig` | Forge config getters |
| `dev.tc4port.forbiddenmagic.ForbiddenMagic` | `MOD_ID`, `initialize(Object, Object)` |

### 2.2 Key verified signatures

```java
// TC4RegistrationStore
public static synchronized void replace(ResourceLocation, Collection<? extends Registration>);
public static synchronized void close(ResourceLocation);
public static synchronized void reinstall(ResourceLocation, Consumer<Collection<Registration>>);
public static synchronized void closeAll();

// ForbiddenAspectMappings
public static synchronized void register();

// ForbiddenResearch
public static final String CATEGORY;                     // plus 37 research/category-key Strings
public static boolean isAvailable(String, FMServerConfig.GluttonyMode);
public static boolean isAvailable(String, FMServerConfig.GluttonyMode, boolean);
// + 3 more overloads with additional booleans (3-, 4- and 7-argument forms)

// ForbiddenEnchantments
public static float wrathBonus(int);
public static int educationalTotalExperience(int, int);
public static ItemStack capitalistReward(LivingEntity, int);
public static boolean isConsumingTarget(ItemStack);
public static int corruptDrops(List<ItemEntity>, RandomSource, boolean);
public static ItemStack sinShardForIndex(int, boolean);

// Registries (examples)
FMItems.ITEMS / WRATH_SHARD / ENVY_SHARD / … / TAINTED_COAL / BLINK_FOCUS / TAINTED_WAND_ROD / …
FMBlocks.BLOCKS / BLACK_FLOWER / WRATH_CAGE / TAINTED_LOG / …
FMEnchantments.WRATH / CAPITALIST / CONSUMING / EDUCATIONAL / CORRUPTING / FIERY_CORE / IMPACT / VOIDTOUCHED
FMDataComponents.MOB_IMPRINT / COLLAR_VIS / MORPH_TOOL_STATE / COLLAR_OWNER / BLINK_FOCUS / PROFANE_CONTRACT / CRYSTALWELL_EASY
FMTags.TAINTED_BLOCKS / TAINTED_SHOVEL_EFFECTIVE / PURIFIABLE_FLUX / CORRUPTING_ORES / HELLFIRE_BASES /
        THAUMIUM_INGOTS / CONSUMING_TARGETS / CORRUPTIBLE_SHARDS / WRATH_CAGE_IMPRINTABLE / …
FMTiers.DISTORTION
```

### 2.3 Notes

- **Research** is datapack content, not Java API: `data/forbidden_magic/research/core.json`
  defines categories/entries (keys are the same strings exposed by `ForbiddenResearch`).
  Research entries carry a `"warp"` integer field. `ForbiddenResearch.isAvailable(...)` is
  the only Java-side query hook.
- **Alchemy / recipes** are datapack JSON (`data/forbidden_magic/recipes/…`,
  `data/forbidden_magic/fm_special_mining_results/default.json`,
  `data/forbidden_magic/focus_catalog/…`). `ForbiddenRecipeExtensions.register()` is the only
  Java recipe hook; there is no public "ForgeAlchemy" API class in the jar. Phase 6C-3's
  `ForgeAlchemyRecipes.java` in upstream uses Pollution's own `PORecipeMaps` and does not need
  Forbidden Magic classes unless addon items are chosen as ingredients.
- **Warp**: no warp class. Warp contribution is declared in TC core's data map
  `data/thaumcraft/data_maps/item/crafting_warp.json` shipped by the jar:
  `forbidden_magic:tainted_sapling → 1`, `forbidden_magic:wrath_cage → 5`.
- **Materials**: no material registry; metal tiers are `FMTiers.DISTORTION`; "materials" are
  items (shards, tainted coal/fruit, emerald fragment, shadow ink, etc.).
- **Aspect mapping** is the most likely direct use in Phase 6C-2: reference
  `ForbiddenAspects.*` / `TC4AspectIds.*` when mapping Pollution materials to addon aspects.
- Upstream files that will need this addon: none import it; the intended port targets are
  `integration/thaumcraft/TCAspects.java`, `loaders/recipes/ForgeAlchemyRecipes.java` and
  `loaders/recipes/CompoundAspectRecipes.java` (see §1).

---

## 3. Tainted Magic (`tainted_magic`)

### 3.1 Public API packages

**None.** Integration-facing public classes:

| Class (FQN) | What it is |
|---|---|
| `dev.tc4port.taintedmagic.integration.TMFocusActions` | 7 `FocusUpgradeId` constants + `register()` |
| `dev.tc4port.taintedmagic.integration.TMWandParts` | 3 `WandMaterialId` constants |
| `dev.tc4port.taintedmagic.data.TMFocusData` | record stored on foci |
| `dev.tc4port.taintedmagic.registry.*` | `TMItems`, `TMBlocks`, `TMDataComponents`, `TMEntities`, `TMRecipeSerializers`, `TMDamageTypes`, `TMTags`, `TMSounds` |
| `dev.tc4port.taintedmagic.common.TMEquipmentEvents` | `register()`, `isFortressArmor(ItemStack)`, `sashEnabled(Player)` |
| `dev.tc4port.taintedmagic.network.TMEffects` | server-side legacy FX send helpers |
| `dev.tc4port.taintedmagic.recipe.VoidTouchRecipe` | `RecipeSerializer SERIALIZER` |
| `dev.tc4port.taintedmagic.config.TMServerConfig` | `SPEC`, `warpwoodRechargeBaseTicks()` |
| `dev.tc4port.taintedmagic.TaintedMagic` | `MOD_ID`, `initialize(Object, Object)`, `id(String)` |

### 3.2 Key verified signatures

```java
// TMFocusActions
public static final FocusUpgradeId POTENCY, ENLARGE, SANITY, ANTIBODY, CORROSIVE, PERSISTENT, DIFFUSION;
public static synchronized void register();

// TMWandParts
public static final WandMaterialId WARPWOOD_ROD, WARPWOOD_STAFF, SHADOWMETAL_CAP;

// TMFocusData (record)
public static final TMFocusData EMPTY;
public static final Codec<TMFocusData> CODEC;
public TMFocusData(List<FocusUpgradeId>, Map<ResourceLocation, String>);
public List<FocusUpgradeId> upgradeSlots();
public Map<ResourceLocation, String> properties();

// Items with public static query helpers
KatanaItem.inscription(ItemStack) -> int
KatanaItem.cooldownUntil(ItemStack) -> long
KatanaItem.CHARGE_TICKS, KatanaItem.COOLDOWN_TICKS
KatanaItem.Metal { THAUMIUM, VOID, SHADOW }
TaintedArmorItem.Family { WARPED_GOGGLES, VOIDMETAL_GOGGLES, VOID_FORTRESS, SHADOW_FORTRESS, VOIDWALKER_BOOTS }
VoidwalkerSashItem.enabled(ItemStack) -> boolean
TMTiers.HOLLOW / SHADOW / PRIMAL
TMEffects.send(ServerLevel, LegacyEffectPayload.Kind, Vec3, int)
TMEffects.send(ServerLevel, LegacyEffectPayload.Kind, Vec3, Vec3, double, int)
```

`TMItems` contains the full item registry (shadow metal ingot/nugget, shadow/crimson cloth,
plates, shards, blood phials, magic funguar, nightshade, flyte charm, salis, gate key, warp
fertilizer, warped/voidmetal goggles, void/shadow fortress armor, voidwalker boots/sash, lumos
ring, warpwood wand rod/staff rod, shadowmetal cap, cloth caps, 6 foci, thaumic disassembler,
hollow dagger, shadowmetal tools, primal blade, 3 katanas).

### 3.3 Notes

- **Warp**: no warp-effect class. Warp is contributed via the TC core data map
  `data/thaumcraft/data_maps/item/equipped_warp.json`, verified amounts:
  `warped_goggles 1`, `voidmetal_goggles 5`, `void_fortress_* 3` (helm/chest/legs),
  `shadow_fortress_* 5`, `voidwalker_boots 5`, `voidwalker_sash 2`, `flyte_charm 5`,
  `gate_key 3`, `primal_blade 5`, `voidmetal_katana 3`, `shadowmetal_katana 7`.
- **Taint**: the "taint" content is the Taint Swarm focus / warped shards / warpwood tree,
  exposed only through registry items and datapack `focus_catalog` JSONs. No public taint API.
- **Research** is datapack: `data/tainted_magic/research/tainted_magic.json`.
- `TMEquipmentEvents.isFortressArmor(ItemStack)` and `VoidwalkerSashItem.enabled(ItemStack)`
  are the only public state queries useful for external integration.
- Upstream files that will need this addon: none import it; likely port touch-points are the
  aspect mapping (`TCAspects.java`), warp handling (`common/warpevent/FluxWarpManager.java`)
  and item/armor behavior (`common/items/armor/*`, `common/items/behaviors/*`).

---

## 4. Thaumic Energistics (`thaumicenergistics`)

Requires AE2 `[15.4.10,16)` at runtime. Its public surface is larger than the other addons but
is still **not** packaged as an `api/` module; the meaningful extension points are the AE2
service interfaces it implements.

### 4.1 Public integration classes

| Class (FQN) | Public? | Notes |
|---|---|---|
| `dev.thaumicenergistics.integration.AeAspectIntegration` | yes | `register()` only — wires essentia into AE2 aspect system |
| `dev.thaumicenergistics.integration.EssentiaWorldIntegration` | yes | `register()` only |
| `dev.thaumicenergistics.integration.MEBusFilterSampling` | yes | `supports(AEBaseMenu, Slot)`, `preview(...)`, `handle(...)` |
| `dev.thaumicenergistics.integration.EssentiaContainerItemStrategy` | yes | implements AE2 `ContainerItemStrategy<EssentiaKey, Context>` |
| `dev.thaumicenergistics.integration.EssentiaTransferLimits` | yes | `satisfiesLegacySuction(int, int, int)` |
| `dev.thaumicenergistics.integration.EssentiaTransportStorage` | **package-private** | implements `MEStorage` — not callable from Pollution |
| `dev.thaumicenergistics.integration.EssentiaImportStrategy` | **package-private** | `StackImportStrategy` |
| `dev.thaumicenergistics.integration.EssentiaExportStrategy` | **package-private** | `StackExportStrategy` |
| `dev.thaumicenergistics.integration.EssentiaExternalStorageStrategy` | **package-private** | `ExternalStorageStrategy` |
| `dev.thaumicenergistics.integration.EssentiaTarget` | **package-private** | empty marker class |

### 4.2 Public storage API

```java
// EssentiaKey extends appeng.api.stacks.AEKey
public static final MapCodec<EssentiaKey> MAP_CODEC;
public EssentiaKey(AspectId);
public static EssentiaKey of(String);
public static EssentiaKey of(AspectId);
public AspectId aspect();
public AEKeyType getType();
public CompoundTag toTag();
public static EssentiaKey fromTag(CompoundTag);
public ResourceLocation getId();
public void writeToPacket(FriendlyByteBuf);
public static EssentiaKey fromPacket(FriendlyByteBuf);
public void addDrops(long, List<ItemStack>, Level, BlockPos);

// EssentiaCellHandler implements ICellHandler
public static final EssentiaCellHandler INSTANCE;
public boolean isCell(ItemStack);
public StorageCell getCellInventory(ItemStack, ISaveProvider);
public Optional<TooltipComponent> getTooltipImage(ItemStack);

// EssentiaCellMath
public static long capacityUnits(long, int);
public static long usedBytes(long, int);
public static long insertableAmount(long, long, long, int, int, long, long);
public static long divideRoundUp(long, int);
public static long saturatingAdd(long, long);

// EssentiaStorageCells.register();  EssentiaStorageComponentRules.requireAe2Capacity(long);
// CellAvailability.visible(String, boolean);
```

### 4.3 Public Vis (grid) API

```java
// VisGridService implements IGridService, IGridServiceProvider
public VisGridService(IGrid);
public boolean hasValidSource(VisInterfaceEndpoint, VisLinkData);
public boolean hasValidSource(VisLinkData);
public boolean isRouting(UUID);
public int route(VisInterfaceEndpoint, VisLinkData, VisChannel, int, VisAction);
public int route(UUID, VisLinkData, VisChannel, int, VisAction);

// VisInterfaceEndpoint extends IGridNodeService, VisSource
public abstract UUID interfaceId();
public abstract boolean isOutputMode();
public abstract boolean isInputSource();
public abstract boolean matches(VisLinkData);
public abstract int provideVis(VisChannel, int, VisAction);

// DirectVisSourceAccess
public static boolean hasSupply(BlockEntity);
public static boolean hasOtherSupply(BlockEntity, VisSource);
public static List<VisSource> sources(BlockEntity);

// VisRequestRules
public static final double AE_PER_SUCCESSFUL_REQUEST;
public static boolean hasRequestEnergy(double);
public static boolean mustCharge(boolean, int);
public static boolean sourceNeedsRelay(boolean);
```

### 4.4 Public block entities / parts / registries

- `EssentiaInterfaceBlockEntity` implements AE2 `IInWorldGridNodeHost`, `IActionHost`,
  TC4R `EssentiaTransport`, AE2 `IColorableBlockEntity`; has static `serverTick(...)`.
- `InfusionProviderBlockEntity` implements `EssentiaSource` (public
  `extractEssentia(AspectId, int, EssentiaTransferMode)`).
- `VisInterfaceBlockEntity` and `VisInterfacePart` implement `VisInterfaceEndpoint` and
  expose `configureAsInput()`, `configureAsOutput(VisLinkData)`, `provideVis(...)`,
  `takeVis(...)`, `linkIdentity()`, `networkEndpoint()`.
- `ArcaneAssemblerBlockEntity` implements AE2 `ICraftingProvider`, `MenuProvider`; public
  `storedCentivis(VisChannel)`, `visCostPercent(Player, VisChannel)`, `coreData()`,
  `configureVisLink(VisLinkData)`, `getAvailablePatterns()`, `pushPattern(...)`.
- Registries: `ModItems` (`CELLS`/`PORTABLE_CELLS`/`COMPONENTS` maps by tier string,
  `ESSENTIA_STORAGE_BUS`, `ARCANE_CRAFTING_TERMINAL`, `VIS_INTERFACE`, `ARCANE_ASSEMBLER`,
  `KNOWLEDGE_INSCRIBER`, …), `ModBlockEntities`, `ModMenus`, `ModSlotSemantics`.
- Data: `data/thaumicenergistics/recipes/…` (all cell tiers and machine recipes) and
  `data/thaumicenergistics/object_aspects/applied_energistics.json`.

### 4.5 Notes

- Pollution's own essentia code (`GTEssentiaHandler`, `MetaTileEntityAspectTank`,
  `MetaTileEntityEssentia*`) implements the **TC4R core** `EssentiaTransport` /
  `EssentiaSource` interfaces, not Thaumic Energistics classes. TE already interoperates with
  any block that implements the TC4R essentia interfaces, so no direct TE API calls are
  required for basic essentia I/O.
- If Pollution wants its machines to appear on the AE2 grid as essentia storage/import/export
  targets, the only public hooks are `EssentiaKey`, `EssentiaCellHandler.INSTANCE`, the
  `VisGridService`/`VisInterfaceEndpoint` interfaces and `MEBusFilterSampling`; the actual
  strategies are package-private and cannot be reused.
- Upstream files that will need this addon: none import it. The AE2-facing port work lives in
  `api/utils/ae2Index.java` (AE2 index) and `loaders/recipes/AERecipes`-style content; the
  essentia side is `common/lib/GTEssentiaHandler.java` and
  `common/metatileentity/storage/MetaTileEntityAspectTank.java`.

---

## 5. Thaumic Tinkerer (`thaumictinkerer`)

### 5.1 The only real public API package

`dev.tc4port.thaumictinkerer.api.enchantment`:

```java
// OsmoticEnchantmentApi
public static synchronized void register(OsmoticEnchantmentDefinition);
public static synchronized List<OsmoticEnchantmentDefinition> registeredAddons();

// OsmoticEnchantmentDefinition (record)
public OsmoticEnchantmentDefinition(ResourceKey<Enchantment>, Map<AspectId, Integer>,
                                    Optional<ResearchKey>, ResourceLocation);
public OsmoticEnchantmentDefinition(ResourceKey<Enchantment>, Map<AspectId, Integer>,
                                    Optional<ResearchKey>, ResourceLocation, BooleanSupplier);
public ResourceKey<Enchantment> enchantment();
public Map<AspectId, Integer> baseCost();
public Optional<ResearchKey> research();
public ResourceLocation icon();
public BooleanSupplier enabled();
```

This is the mechanism to make a Pollution (or other mod's) enchantment available in the
Osmotic Enchanter with an aspect cost. Register during mod construction/init after the
enchantment registry exists.

### 5.2 Public integration / catalog classes

```java
// TTInterModComms (Forge InterModComms keys)
public static final String ADD_RESEARCH_BLACKLIST = "AddResearchBlacklist";
public static final String ADD_CC_BLACKLIST = "AddCCBlacklist";
public static void register(Object);
public static boolean isComputerCraftBlacklisted(String);
public static String sanitizeComputerCraftClassName(Object);
public static Set<String> sanitizeResearchPrefixes(Object);

// TTFocusActions
public static void register();

// TTEnchantmentCatalog
public static final int SOURCE_DEFINITION_COUNT;
public static final List<AspectId> PRIMAL_ORDER;
public static volatile List<OsmoticEnchantmentDefinition> DEFINITIONS;
public static void register();
public static OsmoticEnchantmentDefinition get(ResourceKey<Enchantment>);
public static int indexOf(ResourceKey<Enchantment>);
public static Map<AspectId, Integer> cost(OsmoticEnchantmentDefinition, int);
public static Map<AspectId, Integer> primalCost(OsmoticEnchantmentDefinition, int);
public static boolean isValidTool(ItemStack);
public static boolean canSelect(Player, ItemStack, ResourceKey<Enchantment>, Set<ResourceKey<Enchantment>>);
public static boolean areCompatible(ResourceKey<Enchantment>, Holder<Enchantment>, ResourceKey<Enchantment>, Holder<Enchantment>);

// KamiResearchRequirements
public static final ResourceLocation KAMI_LABEL;
public static List<String> requiredResearch();
public static List<String> missingResearch(Player);
public static boolean isKami(ResearchEntrySummary);
public static Set<String> kamiResearchKeys();

// SoulMobCatalog
public static final int SOURCE_RECIPE_COUNT;
public static void register();
public static Optional<SoulMobCatalog.Recipe> recipeFor(Entity);
public static List<SoulMobCatalog.Recipe> recipes();
public static Optional<SoulMobCatalog.Recipe> recipeForAspects(List<SoulAspect>);

// AspectCropLootManager
public static ItemStack roll(AspectId, ServerLevel, RandomSource);
```

### 5.3 Tools / items / interfaces / registries

- `TTItems` (verified registry contents) includes: Smokey Quartz, Spell Cloth, Tome of
  Knowledge Sharing, Infused Inkwell, Talismans (Withhold/Remedium), Cursed Spirit Blade,
  Mob Aspect, Gaseous Illuminae/Tenebrae, Fume Dissipator, Hyperenergetic Nitor, Infused
  Seeds/Grain/Potion, Helmet of Revealing, 10 foci (Flight, Smelt, Deflect, Mending, Ender
  Rift, Dislocation, XP Drain, Celestial Recall, Shadowbeam, Telekinesis), Soul Mould,
  Ichor/Ichorcloth/Ichorium + nugget, Ichorium tools and Awakened versions, Protoclay,
  Ichor Pouch, Feline Amulet, Awakened armor, Black Hole Ring, Worldshaper's Looking Glass,
  Celestial Pearl, Nether/Ender Shards, and the Smokey Quartz blocks / travel paving /
  necromancy tablet / ethereal platform / essentia funnel / thaumic restorer / osmotic
  enchanter / attractors / dynamism tablet / remote placer / locomotive blocks /
  transvector interface + dislocator / celestial gateway / imbued fire blocks.
- `TTEnchantments` (14 `ResourceKey<Enchantment>` constants): ASCENT_BOOST, SLOW_FALL,
  FLAMING_TOUCH, DISINTEGRATE, QUICK_DRAW, VAMPIRISM, FOCUSED_STRIKES, DISPERSED_STRIKES,
  VALIANCE, FINAL_STRIKE, POUNCE, SHATTER, SHOCKWAVE, TUNNEL.
- `TTDataComponents` (22 `ItemField<?>` constants) — e.g. `SHARED_KNOWLEDGE`,
  `ENCHANTMENT_STATE`, `TRANSVECTOR_BINDING`, `FOCUS_DATA`, `SOUL_MOULD_PATTERN`.
- **Transvector Interface** (the "interfaces" focus): `TransvectorInterfaceBlockEntity`
  implements TC4R `EssentiaTransport` and exposes Forge capability proxies
  `itemHandler(Direction)`, `fluidHandler(Direction)`, `energyStorage(Direction)`,
  `linkedBlockEntityForIntegration()`, `isConnectable(Direction)`, `canInputFrom/OutputTo`,
  `takeEssentia/addEssentia`, plus `RANGE` and `maximumDistance()`. This is the class to
  model Pollution's own proxy/interface blocks on.
- `OsmoticEnchanterBlockEntity` has public `serverTick(...)`, `changeSelection(Player, int, int)`,
  `removeSelection(Player, int)`, `start(Player)`, `validWand(ItemStack)`, `catalogSize()`,
  `dataCount()`.
- `MobAspectItem.create(SoulAspect, Tier)`, `MobAspectItem.data(ItemStack)`;
  `InfusedPotionItem.create(String)`, `InfusedPotionItem.aspect(ItemStack)`;
  `SoulMouldItem.pattern(ItemStack)`.

### 5.4 Notes

- Enchantment definitions can also come from data (`data/thaumictinkerer/…`); the public API
  is for mods that want to register programmatically.
- Upstream files that will need this addon: none import it. Relevant port touch-points are
  the tool/enchantment behavior (`common/items/behaviors/*`), warp/vis machines
  (`MetaTileEntityVisProvider`, `MetaTileEntityVisGenerator`) and
  `api/amplification/*` if addon enchantments/tools are exposed to the amplification system.

---

## 6. Practical integration notes (all verified)

1. **No addon is imported by upstream Pollution**, and **no addon is referenced by the
   1.20.1 port yet**. Phase 6C-2/6C-3/6C-4 must introduce every reference.
2. **Only Thaumic Tinkerer has a public API package.** Treat the other three as
   implementation mods: consume their registries (`FMItems`, `TMItems`, `ModItems`,
   `TTItems`), tags (`FMTags`, `TMTags`, `TTTags`) and datapack content, and avoid their
   package-private strategy classes.
3. **Data-driven over Java API:** aspects (`data/*/thaumcraft/aspects/`), object/entity
   aspects, research (`data/*/research/`), focus catalogs, warp contributions
   (`data/thaumcraft/data_maps/item/*_warp.json`), recipes and loot are JSON; Pollution can
   add or override them with its own datapack/resources without touching addon Java code.
4. **TC4R core types appear throughout addon signatures** (`AspectId`, `VisChannel`,
   `VisAction`, `VisCost`, `ResearchKey`, `FocusUpgradeId`, `WandMaterialId`,
   `EssentiaTransport`, `EssentiaSource`, `ItemStateKey`, `Registration`). Pollution's
   existing `compat/tc4r/TC4RBridge.java` and `api/magic/*` already use the core equivalents,
   so addon integration should route through the same bridge style.
5. **Registration replacement hook:** `TC4RegistrationStore.replace/close/reinstall/closeAll`
   (Forbidden Magic) is the only public mechanism found for swapping an addon's TC4R
   registrations — useful if Pollution needs to disable/override addon content.
6. **Cross-addon dependency:** Forbidden Magic's `mods.toml` optionally depends on
   `thaumictinkerer`; the jar contains `compat/ThaumicTinkererCompat` and
   `compat/ThaumicTinkererLoadedCompat`, so enabling both addons changes FM behavior.
7. **Warp is core-driven:** Pollution's warp manager (`common/warpevent/FluxWarpManager.java`
   in upstream, `common/warp/*` in the port) uses TC core `PlayerWarpApi`/`IPlayerWarp`
   equivalents; addon items only contribute warp through the core data maps listed above.
