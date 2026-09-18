# Materials Audit — upstream unported material files

Audit of the four upstream material files that had not been ported:

| Upstream file | Materials | Previously ported | Ported by this audit | Still unported |
|---|---|---:|---:|---:|---:|
| `api/unification/materials/SecondDegreeMaterials.java` (19 KB) | 62 | 9 | 8 | 45 |
| `api/unification/materials/HigherDegreeMaterials.java` | 4 | 0 | 0 | 4 |
| `api/unification/materials/MagicIntegrationMaterials.java` | 7 | 0 | 1 | 6 |
| `api/unification/materials/MaterialFlagAddition.java` | 0 (empty `init()`) | 0 | 0 | 0 |
| **Total** | **73** | **9** | **9** | **55** |

Method: every `PollutionMaterials.<Field>` declared in the four files was searched
(case-sensitive, field name and `pollution:<id>` string) across the whole port
(`src/main/java/**`, resources, docs) and across upstream `src/main/java/**`
(including the 36 files that statically import `PollutionMaterials.*`, so bare
field references are covered). Ported recipe maps and machine structures were
checked for real consumers, not only mentions.

## 1. Consumer result

**The port has no code consumers of any of the 73 materials.** Every hit in
`meowmel/pollution/**` is a javadoc/comment recording a substitution or a skip:

| Port file | Lines | Kind |
|---|---|---|
| `loaders/recipes/MagicChemicalRecipes.java` | 34-35, 57, 60-61, 70, 73-75 | substitution/skip list |
| `loaders/recipes/MagicIntegrationRecipes.java` | 49, 52, 55 | skip list |
| `loaders/recipes/ForgeAlchemyRecipes.java` | 41-43 | skip list |
| `loaders/recipes/BotaniaRecipes.java` | 44-47, 170 | substitution table |
| `loaders/recipes/NodeFusionRecipes.java` | 27, 29-32 | substitution table |
| `loaders/recipes/MagicGCYMRecipes.java` | 38-39, 73, 80, 313, 430 | substitution/skip notes |
| `common/machine/multiblock/MagicStructureElements.java` | 39, 42 | substitution note |
| `common/machine/multiblock/botania/*`, `.../magic/MagicAssemblerMachine.java` | javadoc | substitution note |
| `dimension/worldgen/PollutionOreVeins.java`, `POWorldgenTodos.java` | 83, 270 / 32, 101, 137 | skipped deposit notes |

The only non-comment matches are recipe **ids** named after upstream materials
(`node_fusion/hyperdimensional_silver`, `node_fusion/dimensional_transforming_agent`,
`node_fusion/starmetal_alloy`, `mana_turbine/hyperdimensional_silver`), which are
not material references.

Upstream bare-name consumers live only in recipe files that the port has already
ported with substitutions (`MagicGCYMRecipes`, `NodeFusionRecipes`) or not ported
at all (`BloodCircuit.java` -> `BloodPlasma`; `MaterialsLine.java` ->
`EnergyCrystal`, `StarmetalAlloy`).

## 2. Still-skipped upstream recipes that need these materials

The table lists every skipped recipe group that references a material from the
four audited files, and whether the material is the *only* missing dependency
(port has the recipe map, items and all other fluids).

| Skipped upstream recipe group | Materials needed from the audited files | Other missing dependencies | Verdict |
|---|---|---|---|
| LLP chain, `MagicChemicalRecipes` (9 recipes: ethyl silicate, lotus dust, rough LLP, LLP, 4x oil+LLP mixing, LLP centrifuge) | `EthylSilicate`, `LotusDust`, `RoughLlp`, `Llp`, `OilWithLlp` | recipe 1 only: GTQT `SiliconTetrachloride` (GTCEu 7.5.3 has none) | **Ported** (5 materials). 8 recipes fully unblocked, recipe 1 needs one substitution |
| Valonite / Octine / Syrmorite philosopher-stone transmutations, `MagicChemicalRecipes` (3) | `DimensionalTransformingAgent` | none | **Ported** (1 material) |
| Super-sticky-tar pair, `MagicChemicalRecipes` (2) and upstream `TarChain` (5: CoalTar+Redstone fermentation + 4 cracking) | `PureTar`, `SuperStickyTar` | none (TarChain is plain GTCEu chemistry) | **Ported** (2 materials) |
| Arcane ink capsule, `MagicIntegrationRecipes` (1) | `ArcaneInk` | ink production needs Astral Sorcery (source gap only, the capsule itself is portable) | **Ported** (1 material) |
| Evolution core + 3 ultimate-catalyst plastics, `MagicChemicalRecipes` (4) | `DimensionalTransformingAgent` | `BlackMansus`, GTQT `Mana`, `Polystyrene`, `Zylon`, `Polyetheretherketone`, `Kevlar`, `KaptonE/K` (absent from GTCEu 7.5.3) | Not unblocked |
| kqt chain, `MagicChemicalRecipes` (31) | `MagicalSulfoPlumbicSalt`, `AlchemicalResidue1-6`, `AlchemicalVapor1-6`, `MagicalTinSolution`, `MagicalStannousSulfateSolution`, `HighmanaStannousSulfate`, `ImpureMercuricSaltSolution`, `MercuricSaltSolution`, `MagicActivated*`, `PurifiedActivated*`, `SyrmoriteDopedMagicWaterSolution`, `Unformed/EmbryoMagicWater`, `Unstable/Impure/HyperdimensionalSilver`, `FerrousChloride` | GTQT `SiliconTetrachloride`, `CarbonTetrachloride`, `Impuremana`, `BlackMansus`, `WhiteMansus`, `Manasteel`, `KQGold`, `Mana`; TC items (`causalityCollapser`, `voidSeed`) | Not unblocked |
| superconductor chain (5) | `HyperdimensionalSilver`, `StarmetalAlloy` | GTQT `CrudeLk99`, `Mansussteel`, `KQGold`, `Thaumium`; FirstDegree superconductors unported | Not unblocked |
| battery chain (20) | `BasicBatteryHullAlloy`, `AdvancedBatteryHullAlloy`, `BasicBatteryContent`, `AdvancedBatteryContent`, `HyperdimensionalSilver` | GTQT `Mansussteel`, `KQGold`, `Thaumium`; superconductor materials unported | Not unblocked |
| filth chain (6) | `Filth`, `FilthWater`, `VoidWater`, `VoidMaterial` | GTQT `Mana`, `VoidMetal`; `ErichAura`; `ItemsTC.voidSeed` | Not unblocked |
| hachimi chain (5) | `HydrazoicAcid`, `SodiumAzide`, `SodiumCyclopentadienide`, `HafnoceneDichloride`, `uOxoBisHafnoceneAzide` | GTQT `SodiumNitrate`, `Acetylene`; `HafniumTetrachloride` (absent from GTCEu 7.5.3) | Not unblocked |
| blood chain (6) | `BloodPlasma` | Blood Magic life essence; `PurifiedBlood`, `InfusedPurifiedBlood`, `ArcaneGelidFluid`, `CryogenicSyntheticBlood`, `ArcaneComputationalSubstrate`, `SyntheticComputationalBlood` (FirstDegree unported); GTQT `GelidCryotheum` | Not unblocked |
| Astral group + flesh circuits, `MagicIntegrationRecipes` (65+) | `OpticalGradeAquamarine`, `StarlightPollen`, `MoonlightResin`, `AstralBloodPlasma`, `CelestialBiologicalMedium`, `StarryArcaneAlloy`, `ArcaneInk` | Astral Sorcery is not a port dependency (`BlocksAS`/`ItemsAS`, liquid starlight); Blood Magic API; `BasicSubstrate` has no ingot form; TC vis resonator | Not unblocked |
| `ForgeAlchemyRecipes` skips | `AethericDarkSteel`, `BloodOfAvernus`, `IizunamaruElectrum`, `HyperdimensionalSilver`, `DimensionalTransformingAgent` | `Black/White/Starrymansus`, `Sentient/BindingMetal`, `Existing/FadingNexus`, GTQT `Mana`/`Thaumium`/`VoidMetal`, Blood Magic life essence, Botania runes | Not unblocked |
| `MagicGCYMRecipes` skips | `DimensionalTransformingAgent`, `AethericDarkSteel`, `BloodOfAvernus`, `IizunamaruElectrum` | Thaumcraft smelter blocks, GTQT materials | Not unblocked |
| upstream `MaterialsLine` (2: 4 primal dusts -> `EnergyCrystal`; `EnergyCrystal` + entropy/order -> `StarmetalAlloy`) | `EnergyCrystal`, `StarmetalAlloy` | upstream outputs `dust, StarmetalAlloy` although the material has no dust form; the port already substitutes `StarmetalAlloy` -> `NaquadahAlloy` in `NodeFusionRecipes` | Not counted as unblocked (recipe is not portable 1:1) |

## 3. Materials ported by this audit (9)

Ported in `api/unification/materials/SecondDegreeMaterials.java` and
`api/unification/materials/MagicIntegrationMaterials.java` with upstream colours,
forms and formula; fields added to `PollutionMaterials`; registered from
`PollutionMaterialEvents.onMaterial` (together with the other material batches).

| Field | id | Form | Unblocks |
|---|---|---|---|
| `LotusDust` | `pollution:lotus_dust` | dust (DULL) | LLP chain |
| `EthylSilicate` | `pollution:ethyl_silicate` | fluid, formula `(C2H5O)4Si` | LLP chain |
| `RoughLlp` | `pollution:rough_llp` | dust (DULL) | LLP chain |
| `Llp` | `pollution:llp` | dust (SHINY) | LLP chain |
| `OilWithLlp` | `pollution:oil_with_llp` | fluid | LLP chain |
| `PureTar` | `pollution:pure_tar` | fluid + block | tar-slime pair, `TarChain` |
| `SuperStickyTar` | `pollution:super_sticky_tar` | fluid | tar-slime pair |
| `DimensionalTransformingAgent` | `pollution:dimensional_transforming_agent` | fluid | Valonite/Octine/Syrmorite transmutations |
| `ArcaneInk` | `pollution:arcane_ink` | fluid (DULL) | arcane ink capsule |

Recipes unblocked (to be ported in a later loader batch; `loaders/**` is out of
scope for this audit): 9 LLP recipes + 3 transmutations + 2 tar-slime + 5 TarChain
+ 1 ink capsule = **20 upstream recipes**, of which the ethyl-silicate precursor
needs one GTQT `SiliconTetrachloride` substitution.

Caveats recorded for the follow-up recipe port:

* `SiliconTetrachloride` (GTQT, not in GTCEu 7.5.3) is the only non-material
  dependency of the first LLP recipe; the port already substitutes other GTQT
  materials, so a substitute must be picked there.
* `DimensionalTransformingAgent` has no in-port source: its upstream production
  is part of the blocked kqt chain. The three transmutations become portable, but
  the fluid can only be obtained once the kqt chain or a substitution exists.
* `PureTar` has no in-port source until `TarChain` is ported (it is plain GTCEu
  chemistry and was skipped only because the material did not exist).
* `ArcaneInk` production needs Astral Sorcery; only the capsule recipe is
  portable.

## 4. Materials deliberately not ported

* **HigherDegreeMaterials (4)**: `AethericDarkSteel`, `BloodOfAvernus`,
  `IizunamaruElectrum` are already replaced by `NaquadahAlloy` / `TungstenSteel` /
  `Electrum` in every ported recipe (`BotaniaRecipes`, `ForgeAlchemyRecipes`,
  `NodeFusionRecipes`, `MagicGCYMRecipes`); porting them would not unblock
  anything. `Kobemetal` has no consumer in upstream or port.
* **SecondDegreeMaterials (45)**: kqt/battery/superconductor/filth/hachimi/blood
  intermediates - their recipes are blocked by GTQT materials (absent from GTCEu
  7.5.3), unported FirstDegree/Element materials, or TC/Blood Magic items, so the
  material definitions alone would not unblock them. `EnergyCrystal` /
  `StarmetalAlloy` additionally suffer the `MaterialsLine` dust-output mismatch
  (section 2). `BloodPlasma` needs the Blood Magic chain.
* **MagicIntegrationMaterials (6)**: all consumers need Astral Sorcery or Blood
  Magic, which are not port dependencies.

## 5. Verification

* `.\gradlew.bat compileJava --console=plain` -> **BUILD SUCCESSFUL** after the
  material additions.
* No file outside `api/unification/**` and `docs/**` was modified for this audit.
