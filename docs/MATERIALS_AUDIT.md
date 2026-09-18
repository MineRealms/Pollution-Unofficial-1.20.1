# Materials Audit — upstream unported material files

Audit of the four upstream material files that had not been ported, plus the
second unblock pass (kqt / superconductor / battery / filth / hachimi chains,
nexus metals, advanced alloys and the Astral-material trio).

| Upstream file | Materials | Ported before | Ported by audit | Ported by second pass | Still unported |
|---|---|---:|---:|---:|---:|
| `api/unification/materials/SecondDegreeMaterials.java` (19 KB) | 62 | 9 | 8 | 42 | 3 |
| `api/unification/materials/HigherDegreeMaterials.java` | 4 | 0 | 0 | 2 | 2 |
| `api/unification/materials/MagicIntegrationMaterials.java` | 7 | 0 | 1 | 3 | 3 |
| `api/unification/materials/MaterialFlagAddition.java` | 0 (empty `init()`) | 0 | 0 | 0 | 0 |
| **Total** | **73** | **9** | **9** | **47** | **8** |

The second pass additionally ports six FirstDegreeMaterials
(`Impuremana`, `KQGold`, `CrudeLk99`, `MagicalSuperconductiveLiquid`,
`Basic/AdvancedThaumicSuperconductor`) and four ElementMaterials
(`SentientMetal`, `BindingMetal`, `ExistingNexus`, `FadingNexus`), which are
outside the four audited files but were the last missing dependencies of the
same recipe groups (see section 5).

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

> **Status note (second pass):** the "Not unblocked" verdicts below are the
> snapshot of the first audit. The kqt, superconductor, battery, filth and
> hachimi groups were unblocked and completed by the second pass (section 5);
> the evolution-core group was already ported in the first pass. The blood and
> Astral groups and `MaterialsLine` remain skipped.

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

* **HigherDegreeMaterials (2 of 4)**: `Kobemetal` (no consumer in upstream or
  port) and `BloodOfAvernus` (// 跳过: 整合包无 Blood Magic).
* **SecondDegreeMaterials (3 of 62)**: `BloodPlasma` (Blood Magic chain),
  `EnergyCrystal` / `StarmetalAlloy` (upstream `MaterialsLine` outputs
  `dust StarmetalAlloy` although the material has no dust form; the port
  already substitutes `StarmetalAlloy` -> `NaquadahAlloy` in
  `NodeFusionRecipes`).
* **MagicIntegrationMaterials (3 of 7)**: `AstralBloodPlasma`,
  `CelestialBiologicalMedium`, `StarryArcaneAlloy` - all consumers need Astral
  Sorcery or Blood Magic, which are not port dependencies
  (// 跳过: 整合包无 Astral Sorcery/Blood Magic). `OpticalGradeAquamarine`,
  `StarlightPollen` and `MoonlightResin` are now ported, but their upstream
  production and consumer recipes stay skipped with the rest of the Astral
  group.
* **ElementMaterials leftovers**: `WhiteMansus`, `BlackMansus`, `Elven`,
  `Starrymansus` stay substituted by `InfusedAura` / GTNN Elementium (the
  established port mappings); their elements (`Wma`/`Bma`/`El`/`St`) are not
  added to `PollutionElements`.
* **FirstDegreeMaterials leftovers**: `Manasteel` -> GTNN `ManaSteel`,
  `Mansussteel` -> `HSSG`, `Terrasteel` -> GTNN `TerraSteel`,
  `ElvenElementium` -> GTNN `Elementium`, `Orichalcos`, `RichAura`/`ErichAura`,
  `TetraethylLead`/`ChlorineTrifluoride`/`hydrazine_sulfate`/`SodiumLeadAlloy`
  (the latter four are already re-routed through GTCEu-native fuel
  intermediates by `MagicFuelMaterials`).

## 5. Second unblock pass (kqt / battery / filth / hachimi)

Ported this pass (57 materials):

| File | Materials |
|---|---|
| `FirstDegreeMaterials` (6) | `Impuremana`, `KQGold` (element `Kqt`), `CrudeLk99`, `MagicalSuperconductiveLiquid`, `BasicThaumicSuperconductor`, `AdvancedThaumicSuperconductor` |
| `ElementMaterials` (4) | `SentientMetal`, `BindingMetal`, `ExistingNexus`, `FadingNexus` (elements `Sen`/`Bin`/`Exn`/`Fan`) |
| `SecondDegreeMaterials` (42) | kqt chain: `MagicalSulfoPlumbicSalt`, `AlchemicalResidue1-6`, `AlchemicalVapor1-6`, `MagicalTinSolution`, `MagicalStannousSulfateSolution`, `HighmanaStannousSulfate`, `ImpureMercuricSaltSolution`, `MercuricSaltSolution`, `MagicActivatedIronChlorideSolution`, `MagicActivatedFerrousChlorideEthanolSolution`, `PurifiedActivatedFerrousChlorideEthanolSolution`, `PurifiedActivatedFerrousChloride`, `SyrmoriteDopedMagicWaterSolution`, `Unformed/EmbryoMagicWater`, `UnstableDimensionalSilver`, `ImpureHyperdimensionalSilver`, `HyperdimensionalSilver`, `FerrousChloride`; filth: `Filth`, `FilthWater`, `VoidWater`, `VoidMaterial`; battery: `Basic/AdvancedBatteryHullAlloy`, `Basic/AdvancedBatteryContent`; hachimi: `HydrazoicAcid`, `SodiumAzide`, `SodiumCyclopentadienide`, `HafnoceneDichloride`, `uOxoBisHafnoceneAzide` |
| `HigherDegreeMaterials` (2) | `AethericDarkSteel`, `IizunamaruElectrum` |
| `MagicIntegrationMaterials` (3) | `OpticalGradeAquamarine`, `StarlightPollen`, `MoonlightResin` |

GTCEu 7.5.3 adaptations:

* `.ingot()` no longer implies a dust form; the materials whose upstream
  recipes consume dust request `.dust()` explicitly (`KQGold`, `CrudeLk99`,
  `HyperdimensionalSilver`, the battery hull alloys, the nexus metals,
  `AethericDarkSteel`, `IizunamaruElectrum`).
* `GTQT Thaumium` -> `StainlessSteel` and `Mansussteel` -> `HSSG` stay as
  documented substitutions in the component lists; `KQGold` and
  `HyperdimensionalSilver` are real and used directly.
* `setTooltips` has no GTCEu 7.5.3 equivalent (`uOxoBisHafnoceneAzide`).
* Tool/rotor stats of `KQGold` are dropped (modern API mismatch, same policy
  as `OreMaterials`); cable and fluid-pipe properties are kept.
* Upstream ids that 1.20.1 `ResourceLocation` rejects were ASCII-ised:
  `Keqinggold` -> `keqinggold`, `μ_oxo_bis_hafnocene_azide` ->
  `u_oxo_bis_hafnocene_azide`.

Recipes unblocked/completed this pass:

* `MagicChemicalRecipes`: the 30 remaining kqt-chain recipes, the 5
  superconductor recipes, the 4 battery mixer recipes (the 16 assembler/canner
  recipes were switched from their plate/dust substitutions to the real
  materials), the 6 filth recipes, the 5 hachimi recipes, the Impuremana
  distillation and the HMF catalyst fix. Philosopher stones 2/3/4 now use
  `IizunamaruElectrum` / `SentientMetal` / `ExistingNexus` blocks.
* `ForgeAlchemyRecipes`: stone upgrades 2/3/4 and the
  `hyperdimensional_silver` / `kq_gold` / `iizunamaru_electrum` /
  `aetheric_dark_steel` transmutations use the real materials.
* `NodeFusionRecipes`: the node-reactor casings and the
  sentient/binding/hyperdimensional/kq-gold/DTA/existing/fading nexus fusion
  fuels use the real materials.
* `MagicGCYMRecipes`: the advanced components use the real
  `IizunamaruElectrum` / `AethericDarkSteel` / `SentientMetal` /
  `BindingMetal` / `HyperdimensionalSilver`; the Impuremana mixers and the
  Manasteel blast consume real `Impuremana`.
* `BotaniaRecipes`: the white and starry rune altars use the real
  `AethericDarkSteel` / `HyperdimensionalSilver` / `IizunamaruElectrum` /
  `KQGold` blocks.
* `ManaToEuRecipes`: `Impuremana` gets its own 3-tick fuel entry.

Documented substitutions still in place (// 上游: X -> 本移植版: Y):

* `GTQT Mana` / `BlackMansus` / `WhiteMansus` / `Starrymansus` /
  `ErichAura` -> `InfusedAura`; `Manasteel` -> GTNN `ManaSteel`;
  `Mansussteel` -> `HSSG`; `GTQT Thaumium` -> `StainlessSteel`;
  `Terrasteel` -> GTNN `TerraSteel`; `ElvenElementium` -> GTNN `Elementium`;
  `GTQT VoidMetal` -> TC4R void ingot; `BloodOfAvernus` -> `TungstenSteel`.
* GTCEu 7.5.3 removed `CarbonTetrachloride`, `Acetylene` and `SodiumNitrate`
  and has no `HafniumTetrachloride`; the kqt/hachimi recipes use
  `Chloroform`, `Ethylene`, GTNN `SodiumNitrate` and
  `TitaniumTetrachloride` instead. `Zirconium`/`Hafnium` have no item form in
  7.5.3, so the HMF catalyst and the hafnocene feedstock use
  `TitaniumTetrachloride`.
* `ItemsTC.voidSeed` -> TC4R `ELDRITCH_OBJECT`,
  `ItemsTC.causalityCollapser` -> TC4R `PRIMORDIAL_PEARL` (established).

Remaining skips and precise reasons:

* Blood Magic groups (blood chain, `BloodOfAvernus`, BMHPCA, blood
  bridge/culture): 整合包无 Blood Magic.
* Astral Sorcery groups (optics, starmetal alchemy, rock-crystal catalysis,
  attuned wafers, celestial machines, ZPM+ boards, the production of
  `OpticalGradeAquamarine` / `StarlightPollen` / `MoonlightResin`):
  整合包无 Astral Sorcery.
* GTFO (banana easter egg, greenhouse infusion): 整合包无 GTFO.
* Starstream network blocks/machines: 星辉网络方块/机器未移植.
* Magic turbines (turbine_1..3 arcane recipes): 魔法涡轮机（小）机器未注册.
* GTQT `Orichalcum` + Muti Dan De Life On assembly line: GTQT Orichalcum
  未移植且机器未注册.
* `ThaumcraftRecipes` vis/morphic resonator + vis battery efficient recipes:
  upstream items absent.
* `MaterialsLine` (`EnergyCrystal` -> `StarmetalAlloy`): output prefix mismatch
  (dust on an ingot/fluid-only material), not portable 1:1.
* `BotaniaRecipes` 16 ORDINARY_ALGAE petal variants (GTQT algae item absent)
  and the UEV+ mana hatches (machines not registered).
* `ManaToEuRecipes` 5 mana fluids (WhiteMansus, BlackMansus, Starrymansus,
  RichAura, ErichAura) unported.

## 6. Verification

* `.\gradlew.bat compileJava --console=plain` -> **BUILD SUCCESSFUL** after the
  material additions and the recipe batches.
* No file outside `api/unification/**`, `loaders/recipes/**` and `docs/**` was
  modified by the audit or the second pass.
