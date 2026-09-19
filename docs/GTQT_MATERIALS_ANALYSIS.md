# GTQT Materials Analysis — for the Forge 1.20.1 Pollution port

Factual audit of the 1.12.2 GTQT material registries and the Pollution materials the
1.20.1 port substituted. No build, no commit, no source edits were made — this file is
the only artifact.

**Repos audited (clones as found):**

| Path | Remote / branch | What it actually is | Version |
|---|---|---|---|
| `H:\MinecraftMods\GT-QuantumTransition` | `GTQT/GT-QuantumTransition` | **modpack** (CurseForge manifest), `overrides/` only | pack 1.8.8, MC 1.12.2 |
| `H:\MinecraftMods\GTQT-GregTech` | `GTQT/GregTech` `master-gtqt` | the GT base fork (`modId gregtech`, jar `gregtech-gtqt`) | **1.9.0** |
| `H:\MinecraftMods\GTQT-Core` | `GTQT/GTQT-Core` `master` | the **older** GTQT-Core addon (`root_package keqing.gtqtcore`) | **1.8.9** |
| `H:\MinecraftMods\Pollution` | `GTQT/Pollution` | upstream Pollution (1.12.2) | 414 commits |
| `H:\MinecraftMods\Pollution-Unofficial-1.20.1` | `MineRealms/Pollution-Unofficial-1.20.1` | the port (GTCEu 7.5.3) | — |

**Critical repo fact:** upstream Pollution's `build.gradle:116-117` pins
`libs/gtqtcore-1.9.0.jar` and `libs/gregtech-gtqt-1.12.2-1.9.0.jar`. The GregTech half is
exactly the `GTQT-GregTech` clone (v1.9.0). The addon half is a **`meowmel.gtqtcore` 1.9.0
fork whose source is not in this workspace** — the `GTQT-Core` clone is the older
`keqing.gtqtcore` 1.8.9 codebase (different root package, different material set). All
Pollution imports (`import meowmel.gtqtcore.api.unification.material.GTQTMaterials;`,
21 files) resolve against that missing fork. Consequences are called out explicitly below.

---

## 1. Material registry layout and counts

### 1.1 `GT-QuantumTransition` — 0 material registries

The repo is the GTQT modpack. It contains no Java sources and no jars:

```
overrides/config, overrides/groovy, overrides/resources, overrides/scripts
```

Material-relevant content is only `overrides/config/gtqtcore.cfg` (machine switches) and
the GroovyScript/ZenScript recipe files. There is nothing to port from this repo.

### 1.2 `GTQT-GregTech` — the GT base fork, 752 materials

Layout (GTCEu-style registry, not the old GTCE 1.12 one):

| File under `src/main/java/gregtech/api/unification/material/` | Builders | Lines |
|---|---:|---:|
| `materials/ElementMaterials.java` | 174 | 1344 |
| `materials/FirstDegreeMaterials.java` | 212 | 1694 |
| `materials/HigherDegreeMaterials.java` | 28 | 249 |
| `materials/OrganicChemistryMaterials.java` | 76 | 574 |
| `materials/SecondDegreeMaterials.java` | 93 | 802 |
| `materials/UnknownCompositionMaterials.java` | 169 | 933 |
| `materials/MaterialFlagAddition.java` (flags only) | 0 | 409 |
| `materials/SoftToolAddition.java` (tool flags only) | 0 | 39 |
| `Materials.java` (field declarations) | **752 fields** | — |

Builder idiom: `X = Material.builder(id, gregtechId("x")) ... .build();`
(e.g. `SecondDegreeMaterials.java:31`). There are **no material builders anywhere else**
in the repo — the only other hit is `integration/groovy/GroovyExpansions.java:29`, a
runtime bridge.

Verdict: this is a stock GTCEu-1.12-style base. It contains **none** of the Pollution
magic materials (grep for `Thaumium|Mana|Sunnarium|Manasteel|Mansus|ErichAura|VoidMetal`
returns nothing in the material package). Its only "new" entries vs. the older
`gregtech-MeowmelMuku-2.9.0` jar are GTCEu-era chemistry: nuclear isotopes
(`uranium_235/238`, `plutonium_236..244`, `curium_242..250`, …), battery chemistry
(`zinc_manganese_electrolyte`, `lithium_bisoxalatoborate`, `lfp_cathode_powder`, …,
`UnknownCompositionMaterials.java:693-918`), hydrocracking/steamcracking fluids and
`corium`/`honeycomb_extract` (`UnknownCompositionMaterials.java:910-923`).

### 1.3 `GTQT-Core` (older clone) — the actual GTQT custom registry, 1473 materials

Layout under `src/main/java/keqing/gtqtcore/api/unification/`:

| File | Builders | Lines |
|---|---:|---:|
| `matreials/ElementMaterials.java` | 45 | 553 |
| `matreials/FirstDegreeMaterials.java` | 747 | 5609 |
| `matreials/HigherDegreeMaterials.java` | 86 | 669 |
| `matreials/MachineCasingMaterials.java` | 55 | 790 |
| `matreials/SecondDegreeMaterials.java` | 323 | 2593 |
| `matreials/UnknownCompositionMaterials.java` | 217 | 1018 |
| `matreials/MaterialFlagAddition.java`, `MaterialPropertyAddition.java`, `SoftToolAddition.java` | 0 | 311 / 52 / 41 |
| `GTQTMaterials.java` (field declarations) | **1466 fields** | 1527 |
| `material/info/GTQTMaterialIconSet.java` | 13 custom icon sets | 22 |

Builder idiom: `GTQTMaterials.X = new Material.Builder(getMaterialsId(), gtqtcoreId("x")) ... .build();`
(start IDs are per-file counters, e.g. `MachineCasingMaterials.java:30-39`).

### 1.4 Pollution 1.12 vs. the 1.20.1 port

| Repo | Registry files | Builders | `PollutionMaterials` fields |
|---|---|---:|---:|
| `Pollution` (upstream) | 9 files under `meowmel/pollution/api/unification/materials/` | **166** | 166 |
| `Pollution-Unofficial-1.20.1` | 10 files under `meowmel/pollution/api/unification/materials/` | **137** | 137 |

Upstream per file: Element 14, FirstDegree 31, HigherDegree 4, Infused 34,
MagicIntegration 7, Ore 14, SecondDegree 62.
Port per file: Element 10, FirstDegree 12, HigherDegree 2, Infused 34, MagicFuel 3,
MagicIntegration 4, Ore 11, SecondDegree 50, Substrate 11.

The port is missing **29 upstream materials** (166 − 137); 18 of them are the focus of
section 3. Port registration goes through
`PollutionMaterialEvents.java:32-48` (`MaterialEvent`), registry creation in
`onMaterialRegistry` (`PollutionMaterialEvents.java:26-30`).

---

## 2. Characterisation of the "messy materials"

The mess is **not** in `GTQT-GregTech` (clean stock base, section 1.2). It is in
`GTQT-Core` — 1473 builders, mostly written one-off style without components, flags,
formulas or icons. The Pollution-specific magic materials that upstream imports are a
third group: they are in the missing `meowmel.gtqtcore` fork and cannot be audited here.

### 2.1 `GTQT-Core` quality statistics (parser over all 1473 builders)

| Metric | Count | Share |
|---|---:|---:|
| no `.components(...)` at all | **676** | 45.9 % |
| no `.flags(...)` | **811** | 55.1 % |
| no explicit `.iconSet(...)` (falls back to DULL) | **894** | 60.7 % |
| no `.color(...)` | 94 | 6.4 % |
| no `.formula(...)` anywhere in the repo (formulas only via post-`.build().setFormula(...)`) | 1473 | 100 % |
| duplicate registry ids | 8 id groups | — |
| duplicate Java field names | 11 groups | — |
| fully empty entries (no components, flags, colour, icon) | 3 | — |

Concrete duplicate / placeholder examples:

| Problem | Material | Location |
|---|---|---|
| id `carbon_disulfide` registered **three times** | `CarbenDisulfide` | `SecondDegreeMaterials.java:221`, `:259`, `:265` |
| id `magic_gas` registered **twice**, different colour and one with formula | `MagicGas` | `SecondDegreeMaterials.java:44` (0x00FFFF, no formula) vs `:87` (0xB2DFEE, `.setFormula("-Ma-")`) |
| id `draconium` twice | `Draconium` | `ElementMaterials.java:37` and `FirstDegreeMaterials.java:3249` |
| id `ammonium_persulfate` twice | `AmmoniumPersulfate` | `FirstDegreeMaterials.java:2622`, `SecondDegreeMaterials.java:434` |
| id `potassium_fluoride` twice | `PotassiumFluoride` | `FirstDegreeMaterials.java:1873`, `SecondDegreeMaterials.java:1665` |
| id `sodium_tungstate` twice | `SodiumTungstate` | `FirstDegreeMaterials.java:2026`, `:2601` |
| id `hydroxylamine_disulfate` twice | `HydroxylamineDisulfate` | `FirstDegreeMaterials.java:2384`, `SecondDegreeMaterials.java:409` |
| id `propargyl_alcohol` twice | `PropargylAlcohol` | `FirstDegreeMaterials.java:4033`, `SecondDegreeMaterials.java:680` |
| placeholder: no components/flags/colour/icon | `IridiumOnCubicZirconia` | `FirstDegreeMaterials.java:3614` |
| placeholder: same | `Cobalt60Iodide` | `FirstDegreeMaterials.java:3706` |
| placeholder: same | `HydroiodicAcid` | `FirstDegreeMaterials.java:3711` |

(The old 1.12 GTCE registry tolerates some duplicate-id re-registration by overwrite; a
1.20.1 port would have to pick one definition or namespace them.)

### 2.2 Material families (id-pattern counts in `GTQT-Core`)

| Family | Count | Examples / lines |
|---|---:|---|
| Taranium (fuels, gases, helium-3 processing) | 27 | `heavy/medium/light_taranium_fuel` `FirstDegreeMaterials.java:4883-4918`; `*_taranium_gas`, `cracked_*` `UnknownCompositionMaterials.java:840-874`; `taranium_*_helium_3` `SecondDegreeMaterials.java:1572-1619` |
| Rare-earth "nano resin" extraction chain | 32 | `lanthanum_extracting_nano_resin` … `filled_lutetium_extracting_nano_resin` `HigherDegreeMaterials.java:344-546` |
| "Magic" mana gases/fluids | 8 | `MagicGas/Fas/Das/Aas/Rub` `SecondDegreeMaterials.java:44-127` (formula `-Ma-`), `Magic`, `RichMagic` `:127-135` |
| Rocket fuels | 2 | `rp_1_rocket_fuel` `FirstDegreeMaterials.java:76`, `methylhydrazine_nitrate_rocket_fuel` `:91` |
| Blood | 3 | `blood_cells`, `blood_plasma` `SecondDegreeMaterials.java:581/588`, `dragon_blood` `UnknownCompositionMaterials.java:914` |
| Draconium line | 4 | `draconium`, `awakened_draconium`, `chaotic_draconium` `ElementMaterials.java:37-76` |
| Naquadah/naquadria chemistry | 43 | `naquadriatic_taranium` `FirstDegreeMaterials.java:2233`, enriched solutions `UnknownCompositionMaterials.java` |
| Infinity-tier metals with custom icon sets | 8 | `infinity`, `spacetime`, `eternity`, `hypogen`, `omnium`, `magmatter`, `degenerate_rhenium`, `mhcsm`, `legendarium`, `rhugnor` (ids below) |
| Machine casings | 55 | whole `MachineCasingMaterials.java` |
| Machine-age organic chemistry (acids/oxides/solutions) | 218 | scattered across `FirstDegreeMaterials`/`SecondDegreeMaterials` |

Custom icon sets (`GTQTMaterialIconSet.java:8-21`): `CUSTOM_INFINITY`, `CUSTOM_SPACETIME`,
`CUSTOM_DEGENERATE_RHENIUM`, `CUSTOM_LEGENDARIUM`, `CUSTOM_MHCSM`, `CUSTOM_ETERNITY`,
`CUSTOM_RHUGNOR`, `CUSTOM_HYPOGEN`, `CUSTOM_COSMIC_NEUTRONIUM`, `CUSTOM_MAGNETO_RESONATIC`,
`REAGENT`, `CUSTOM_OMNIUM`, `CUSTOM_MAGMATTER`. Their textures live in
`assets/gtqtcore/textures/items/material_sets/<set>/` (e.g. `infinity/` has 65 files,
`mhcsm/` 108, `magmatter/` 82). The GT base repo/jar only ships the 22 standard sets.

### 2.3 The kqt / hachimi / LLP chains are **Pollution-side**, not GTQT

The three families named in the brief do not exist in either GTQT clone (word-boundary
grep for `KQT`, `hachimi`, `LLP` returns nothing in `GTQT-GregTech` or `GTQT-Core`). They
are recipe chains of upstream Pollution:

| Chain | Upstream location | Materials involved |
|---|---|---|
| kqt | `Pollution/.../loaders/recipes/MagicChemicalRecipes.java:620` (`kqt_chain()`), 31 recipes | `MagicalSulfoPlumbicSalt`, `AlchemicalResidue/Vapor1-6`, `MagicalTinSolution`, `…`, `HyperdimensionalSilver`, `KQGold`, `Impuremana` |
| hachimi | `MagicChemicalRecipes.java:1207` (`hachimi_chain()`), 5 recipes | `HydrazoicAcid`, `SodiumAzide`, `SodiumCyclopentadienide`, `HafnoceneDichloride`, `uOxoBisHafnoceneAzide` |
| LLP | `SecondDegreeMaterials.java:67-79` + `MagicChemicalRecipes` | `RoughLlp`, `Llp`, `OilWithLlp`, `EthylSilicate`, `LotusDust` |

The port already ported all three chains (`docs/MATERIALS_AUDIT.md` §5).

---

## 3. Focused table: materials upstream references that the port substituted

Method: every name in the brief was searched case-sensitively in `Pollution/src/main/java`
(plus the extra `GTQTMaterials.*` references found there), then in `GTQT-GregTech`,
`GTQT-Core`, the GT jar `gregtech-MeowmelMuku-1.12.2-2.9.0-188.jar`, and the port.
Upstream builder data comes from the parsed sources; `icon` is the `MaterialIconSet` of
the builder (blank = default DULL).

### 3.1 GTQT-provided materials (defined outside Pollution)

| Material | Upstream Pollution reference | GTQT status | Port status |
|---|---|---|---|
| `Thaumium` | 19 refs; builder dependency `FirstDegreeMaterials.java:130` (Mansussteel components), property tweaks `MaterialPropertyAddition.java:27-28`, recipes `MagicChemicalRecipes.java:977/1015`, `MagicGCYMRecipes.java:379/1065/1073/1763/2165`, `ThaumcraftRecipes.java:602/661`, `ForgeAlchemyRecipes.java:279`, `MeteorsHelper.java:127`, filter `PollutionMetaItem1.java:154` | **not in either clone.** GTQT-Core 1.8.9 has no `thaum*` id. Defined in the missing `meowmel.gtqtcore` 1.9.0 fork | substituted by **StainlessSteel** (`MagicChemicalRecipes.java:127`, `MagicGCYMRecipes.java:90`) |
| `Mana` | 117 refs; components of Manasteel `FirstDegreeMaterials.java:112`, Salismundus `:123`, Terrasteel `:151`, Orichalcos `:178`; many recipes | **not in either clone.** Closest 1.8.9 analogue is `Magic` (`id magic`, fluid, 0x7B68EE, `SecondDegreeMaterials.java:127`) | substituted by **InfusedAura** (`ForgeAlchemyRecipes.java:43`, `MagicChemicalRecipes.java:63` …) |
| `Sunnarium` | `MagicIntegrationRecipes.java:66/420`, `ThaumcraftRecipes.java:34/53` (dust output) | GTQT-Core has it only as a **meta item** (`GTQTMetaItems.java:357`, `GTQTMetaItem1.java:357`), not a material | substituted by **Titanium dust** (`InfusionRecipes.java:641`, `ThaumcraftRecipes`) |

### 3.2 Pollution-defined materials (the port substituted or dropped them)

| Material | Upstream definition (file:line) | id / forms | Components / formula | Color | Icon | Flags | Port status |
|---|---|---|---|---|---|---|---|
| `Manasteel` | `FirstDegreeMaterials.java:109-118` | `manasteel`, ingot+fluid+ore | Iron 4 + **Mana** 1 | 0x1E90FF | METALLIC | tool/rotor/pipe/cable | **missing** → GTNN `ManaSteel` (`BotaniaRecipes.java:374-376`) |
| `Mansussteel` | `FirstDegreeMaterials.java:127-141` | `mansussteel`, ingot+fluid | Manasteel 3 + **Thaumium** 2 + Salismundus 1, blast 2700 LOW | 0xE6E6FA | METALLIC | DECOMPOSITION_BY_CENTRIFUGING | **missing** → HSSG (`MagicChemicalRecipes.java:126/1482`) |
| `HyperdimensionalSilver` | `SecondDegreeMaterials.java:276-288` | `hyperdimensional_silver`, ingot+fluid+plasma | formula `Ag50(RnMa)m(AeIgAqTerOrdPe)n`, blast 5400 MID | 0xD1FAFF | SHINY | 9 GEN flags + centrifuge | **ported** `SecondDegreeMaterials.java:335` |
| `KQGold` | `FirstDegreeMaterials.java:157-168` | `Keqinggold`, fluid+ingot+plasma | element `Kqt`, blast 3600 MID | 0xFCF770 | SHINY | 8 GEN flags | **ported** `FirstDegreeMaterials.java:133` (id ASCII-ised `keqinggold`) |
| `ErichAura` | `FirstDegreeMaterials.java:189-193` | `erich_aura`, fluid only | — | 0xCD0000 | SHINY | — | **missing** → InfusedAura (24 refs) |
| `WhiteMansus` | `ElementMaterials.java:74-79` | `whitemansus`, fluid only | — | 0xEFF0FF | SHINY | — | **missing** → InfusedAura (30 refs) |
| `BlackMansus` | `ElementMaterials.java:81-86` | `Blackmansus` (uppercase B!), fluid only | — | 0x606060 | SHINY | — | **missing** → InfusedAura (22 refs) |
| `Starrymansus` | `ElementMaterials.java:95-100` | `starrymansus`, fluid only | — | 0xFFF6FF | BRIGHT | — | **missing** → InfusedAura (31 refs). (Brief spelled it `Starymansus`.) |
| `RichAura` | `FirstDegreeMaterials.java:183-187` | `rich_aura`, fluid only | — | 0xCD6600 | SHINY | — | **missing**; `ManaToEuRecipes` skip list |
| `Impuremana` | `FirstDegreeMaterials.java:103-107` | `impuremana`, fluid only | — | 0x008B8B | DULL | — | **ported** `FirstDegreeMaterials.java:126` |
| `BloodOfAvernus` | `HigherDegreeMaterials.java:51-57` | `blood_of_avernus`, ingot+fluid | — | 0x5E0000 | BRIGHT | 11 GEN flags | **missing** → TungstenSteel (Blood Magic absent) |
| `SentientMetal` | `ElementMaterials.java:102-108` | `sentient_metal`, ingot+fluid | — | 0x55FFFA | BRIGHT | 9 GEN flags | **ported** `ElementMaterials.java:92` (+dust) |
| `BindingMetal` | `ElementMaterials.java:110-116` | `binding_metal`, ingot+fluid | — | 0xDA1D0F | SHINY | 9 GEN flags | **ported** `ElementMaterials.java:102` |
| `ExistingNexus` | `ElementMaterials.java:118-124` | `existing_nexus`, ingot+fluid | — | 0xC0C0C0 | BRIGHT | 9 GEN flags | **ported** `ElementMaterials.java:112` |
| `FadingNexus` | `ElementMaterials.java:126-132` | `fading_nexus`, ingot+fluid | — | 0x404040 | SHINY | 9 GEN flags | **ported** `ElementMaterials.java:122` |
| `AethericDarkSteel` | `HigherDegreeMaterials.java:42-48` | `aetheric_dark_steel`, ingot+fluid | — | 0x041B4E | SHINY | 11 GEN flags | **ported** `HigherDegreeMaterials.java:52` |
| `IizunamaruElectrum` | `HigherDegreeMaterials.java:60-66` | `iizunamaru_electrum`, ingot+fluid | — | 0xF2FF2C | SHINY | 11 GEN flags | **ported** `HigherDegreeMaterials.java:64` |
| `OpticalGradeAquamarine` | `MagicIntegrationMaterials.java:43-48` | `optical_grade_aquamarine`, gem only | — | 0x6DE8F2 | GEM_HORIZONTAL | — | **ported** `MagicIntegrationMaterials.java:35` |
| `StarlightPollen` | `MagicIntegrationMaterials.java:50-56` | `starlight_pollen`, dust+fluid | — | 0xBCEBFF | SHINY | — | **ported** `MagicIntegrationMaterials.java:42` |
| `MoonlightResin` | `MagicIntegrationMaterials.java:58-63` | `moonlight_resin`, fluid only | — | 0xA9B5F7 | DULL | — | **ported** `MagicIntegrationMaterials.java:50` |
| `PureTar` | `SecondDegreeMaterials.java:97-101` | `pure_tar`, liquid | — | 0x4F4F4F | — | — | **ported** `SecondDegreeMaterials.java:117` |
| `SuperStickyTar` | `SecondDegreeMaterials.java:85-89` | `super_sticky_tar`, fluid | — | 0x4F4F4F | SHINY | — | **ported** `SecondDegreeMaterials.java:122` |
| `DimensionalTransformingAgent` | `SecondDegreeMaterials.java:285-288` | `dimensional_transforming_agent`, fluid | — | 0xFFC7F7 | — | — | **ported** `SecondDegreeMaterials.java:130` |
| `ArcaneInk` | `MagicIntegrationMaterials.java:79-84` | `arcane_ink`, fluid | — | 0x241035 | DULL | — | **ported** `MagicIntegrationMaterials.java:56` |
| `BasicSubstrate` | `SecondDegreeMaterials.java:111-114` | `basic_substrate`, ingot+fluid | — | 0xFFFFD8 | — | — | **ported** (fluid-only) `SubstrateMaterials.java:46`, colour changed to 0x6F8F6F |
| `AdvancedSubstrate` | `SecondDegreeMaterials.java:116-119` | `advanced_substrate`, ingot+fluid | — | 0xD4FFF0 | — | — | **ported** (fluid-only) `SubstrateMaterials.java:48`, colour changed to 0x4F6FAF |

Related upstream materials found while searching the same files (for completeness):
`Salismundus` (`FirstDegreeMaterials.java:120-125`, dust, Redstone 2 + Mana 1, 0xEE82EE SHINY)
→ ported with no components (`SubstrateMaterials.java:29`, colour 0xE8E8D0);
`Terrasteel` (`:144-154`, Iron 4 + Carbon 4 + EnderPearl 4 + Mana 3, 0x58FF0B)
→ GTNN `TerraSteel`; `ElvenElementium` (`:195-201`, Iron 4 + `Elven` 1, 0xEE6AA7)
→ GTNN `Elementium`; `Elven` (`ElementMaterials.java:88-93`, fluid, 0xEE30A7 SHINY) unported;
`Orichalcos` (`:171-181`, same components as Terrasteel, 0xFF00FF) unported (its only
consumer, the Muti Dan De Life On line, is skipped); `Kobemetal`
(`HigherDegreeMaterials.java:31-37`, He+Li+Co+Pt+Er, 0xFFD700) unported, no consumer;
`EnergyCrystal` (`SecondDegreeMaterials.java:424-430`, dust+fluid, 4 primal aspects)
and `StarmetalAlloy` (`:434-439`, ingot+fluid, no components) unported (`MaterialsLine`
is not portable 1:1); `BloodPlasma` (`:104-108` and Astral `:65-70`) unported (Blood Magic /
Astral Sorcery absent).

### 3.3 Other `GTQTMaterials.*` references found in upstream Pollution (14)

Upstream imports 17 GTQT materials in total (`Acetylene, Adamantium, Crotonaldehyde,
Cryolite, Fluix, GelidCryotheum, Infinity, Mana, MarsAir, MethylFormate, Orichalcum,
SodiumNitrate, Sunnarium, Thaumium, UnderAir, VoidMetal, Zylon`). The remaining 14 are:

| Material | GTQT-Core 1.8.9 definition (file:line) | Builder summary | Icon set | Port handling |
|---|---|---|---|---|
| `Acetylene` | `FirstDegreeMaterials.java:5130-5134` | fluid, C2H2, 0x959C60 | — | Ethylene (`MagicChemicalRecipes.java:416/458`) |
| `Adamantium` | `ElementMaterials.java:419-429` | ingot(6)+fluid+plasma, element `Ad`, 0xFF0040, blast 5225 | METALLIC | unported; sole consumer `MagicGCYMRecipes.java:2615` skipped |
| `Crotonaldehyde` | `FirstDegreeMaterials.java:376-380` | fluid, C4H8O, 0xd89045 | SHINY | Butyraldehyde |
| `Cryolite` | `HigherDegreeMaterials.java:99-103` | ore+dust, Na3AlF6, 0x98F5FF | — | vein skipped (`PollutionOreVeins.java:180`) |
| `Fluix` | `FirstDegreeMaterials.java:165-170` | gem+ore, SiO2, 0x7D26CD, GENERATE_PLATE+CRYSTALLIZABLE | SHINY | AE2 `fluix_dust`/`fluix_crystal` (`AERecipes.java:101/460`) |
| `GelidCryotheum` | `FirstDegreeMaterials.java:1481-1486` | liquid custom still/flow, Ice 2+Electrotine 1+Water 1, 0x40B8FB, DISABLE_DECOMPOSITION | — | recipe skipped (`MagicChemicalRecipes.java:1281`) |
| `Infinity` | `ElementMaterials.java:143-159` | ingot+liquid, element `Infinity`, CUSTOM_INFINITY, blast 12600 | **CUSTOM_INFINITY** | GTNN `Infinity` (`MagicChemicalRecipes.java:572`, `MagicGCYMRecipes.java:562`) |
| `MarsAir` | `SecondDegreeMaterials.java:75-84` | gas+liquid, CO2 80/Argon 20/O2 10/Radon 10/H2 10/N2 10/MagicGas 10, 0x8B3E2F | — | only a commented-out use (`MetaTileEntityBotGasCollector.java:81`) |
| `MethylFormate` | `FirstDegreeMaterials.java:5084-5088` | fluid, HCO2CH3, 0xFFAAAA, DISABLE_DECOMPOSITION | — | MethylAcetate |
| `Orichalcum` | `ElementMaterials.java:395-405` | ingot(6)+fluid, element `Or`, 0x72A0C1, pipe/blast 9000 | METALLIC | unported; assembly line skipped (`MagicGCYMRecipes.java:98`) |
| `SodiumNitrate` | `FirstDegreeMaterials.java:1994-1999` | dust, NaNO3, 0x846684 | ROUGH | GTNN `SodiumNitrate` (`MagicChemicalRecipes.java:415`) |
| `UnderAir` | `SecondDegreeMaterials.java:51-60` | gas+liquid, CH4 78/H2S 21/Ne 7/Rn 2, 0x2E8B57 | — | only a commented-out use (`MetaTileEntityBotGasCollector.java:82`) |
| `VoidMetal` | `ElementMaterials.java:275-281` | ingot+fluid, element `VoidMetal`, 0x20142C | DULL | TC4R `void_ingot` |
| `Zylon` | `FirstDegreeMaterials.java:506-513` | polymer+fluid, C14H6N2O2, 0xFFE000, 4 flags | SHINY | Polybenzimidazole (`MagicChemicalRecipes.java:65`) |

### 3.4 Texture sets

- All icon sets used by the materials above are **standard GT sets** (`SHINY`, `DULL`,
  `METALLIC`, `BRIGHT`, `ROUGH`, `GEM_HORIZONTAL`) — GTCEu 7.5.3 ships these, and the GT
  jar `gregtech-MeowmelMuku-1.12.2-2.9.0-188.jar` contains all 22 item material-set
  families (`bright, certus, diamond, dull, emerald, fine, flint, gem_horizontal,
  gem_vertical, glass, lapis, lignite, magnetic, metallic, netherstar, opal, powder,
  quartz, rough, ruby, sand, shiny, wood`). **No new PNGs are required for section 3.**
- Only `Infinity` (GTQT) uses a non-standard set (`CUSTOM_INFINITY`). Its assets exist
  only in GTQT-Core: `assets/gtqtcore/textures/items/material_sets/infinity/*.png`
  (`ingot.png`, `ingot_double.png`, `ingot_hot.png`, `dust.png`, `dust_small.png`,
  `dust_tiny.png`, `nugget.png`, `plate.png`, `plate_dense.png`, `plate_double.png`,
  `foil.png`, `gear.png`, `gear_small.png`, `ring.png`, `rotor.png`, `round.png`,
  `screw.png`, `spring.png`, `spring_small.png`, `stick.png`, `stick_long.png`,
  `bolt.png`, `lens.png`, `lens_overlay.png`, `singularity.png`, `singularity_overlay.png`,
  `turbine_blade.png`, `wire_fine.png`, `tool_head_*.png`). The same pattern applies to
  `spacetime`, `eternity`, `hypogen`, `omnium`, `magmatter`, `mhcsm`, `legendarium`,
  `degenerate_rhenium`, `cosmic`, `cosmic_neutronium`, `magneto_resonatic`, `reagent`,
  `nuclear`, `enriched`.

---

## 4. Porting plan for the 1.20.1 port

Rules used: GTCEu 7.5.3 builder = `new Material.Builder(id).color(...).form(...).iconSet(...)
.flags(...).components(...).element(...).blast(...).buildAndRegister()`; ids must be
lowercase (`ResourceLocation`); `.ingot()` does not imply dust in 7.5.3; blast takes
`BlastProperty.Builder`. New registrations go into
`meowmel/pollution/api/unification/materials/*`, fields into `PollutionMaterials.java`,
and `register()` into `PollutionMaterialEvents.java:33-48`.

### 4.1 Already ported — no action

`Impuremana` (F:126), `KQGold` (F:133), `SentientMetal` (E:92), `BindingMetal` (E:102),
`ExistingNexus` (E:112), `FadingNexus` (E:122), `AethericDarkSteel` (H:52),
`IizunamaruElectrum` (H:64), `OpticalGradeAquamarine` (M:35), `StarlightPollen` (M:42),
`MoonlightResin` (M:50), `ArcaneInk` (M:56), `PureTar` (S:117), `SuperStickyTar` (S:122),
`DimensionalTransformingAgent` (S:130), `HyperdimensionalSilver` (S:335),
`BasicSubstrate`/`AdvancedSubstrate` (Substrate:46/48).
Cheap follow-up: the port's `SubstrateMaterials.java` uses approximate colours that do not
match upstream — `Substrate` 0x8FAF8F vs upstream 0xCDA5F7, `Valonite` 0x6FD6C4 vs
0xFFCCFF, `Syrmorite` 0x4A5D6A vs 0x2414B3, `Octine` 0xC46A2A vs 0xFFAE33, `Thaummix`
0x7A4FA0 vs 0x5900B3, `Salisundus` 0xE8E8D0 vs upstream 0xEE82EE, `Roughdraft` 0x7A6A5A
vs 0xCDA5F7. Aligning them is a one-line-per-material edit.

### 4.2 Port as-is (definitions available in the clones / upstream)

These are all fluid-only or simple; **no texture assets needed** (standard sets).

| Material | Action | GTCEu 7.5.3 calls |
|---|---|---|
| `WhiteMansus` | add to `ElementMaterials.register()` + field | `.color(0xEFF0FF).fluid().iconSet(MaterialIconSet.SHINY).buildAndRegister()` |
| `BlackMansus` | same; **id must become `blackmansus`** (upstream `Blackmansus` has an uppercase B) | `.color(0x606060).fluid().iconSet(MaterialIconSet.SHINY).buildAndRegister()` |
| `Starrymansus` | same | `.color(0xFFF6FF).fluid().iconSet(MaterialIconSet.BRIGHT).buildAndRegister()` |
| `ErichAura` | same | `.color(0xCD0000).fluid().iconSet(MaterialIconSet.SHINY).buildAndRegister()` |
| `RichAura` | same; unblocks the 5 skipped `ManaToEuRecipes` fuels | `.color(0xCD6600).fluid().iconSet(MaterialIconSet.SHINY).buildAndRegister()` |
| `Elven` | same; prerequisite for `ElvenElementium` (id `elven`, lowercase) | `.color(0xEE30A7).fluid().iconSet(MaterialIconSet.SHINY).buildAndRegister()` |
| `Manasteel` | port only once `Mana` exists (or keep GTNN) | `.color(0x1E90FF).ingot().fluid().iconSet(MaterialIconSet.METALLIC).components(Iron,4,Mana,1)` + `GENERATE_*` flags; drop tool/rotor stats (API mismatch) |
| `Mansussteel` | port once `Manasteel`+`Thaumium` exist | `.color(0xE6E6FA).ingot().fluid().components(Manasteel,3,Thaumium,2,Salismundus,1).flags(DECOMPOSITION_BY_CENTRIFUGING)` + blast 2700 LOW |
| `Terrasteel` | port if GTNN TerraSteel should be dropped | `.color(0x58FF0B).ingot().fluid().components(Iron,4,Carbon,4,EnderPearl,4,Mana,3)` + flags |
| `ElvenElementium` | port once `Elven` exists | `.color(0xEE6AA7).ingot().fluid().components(Iron,4,Elven,1)` + flags |
| `Salismundus` | fix existing definition to upstream components | add `.components(Redstone,2,Mana,1)`, colour 0xEE82EE |
| `EnergyCrystal` / `StarmetalAlloy` | only if `MaterialsLine` is reworked | `EnergyCrystal`: `.dust().fluid().components(InfusedAir,1,InfusedFire,1,InfusedWater,1,InfusedEarth,1)`; `StarmetalAlloy`: `.ingot().fluid()` |

### 4.3 Port + texture assets

None of the section-3 materials need new PNGs. Only if the port wants the GTQT
custom-icon materials (`Infinity`, `Adamantium`, `Orichalcum`, `VoidMetal` ported as
GT materials rather than substitutions) would it need the GTQT-Core sets — in practice
only `Infinity` (`CUSTOM_INFINITY`). To do that, copy
`assets/gtqtcore/textures/items/material_sets/infinity/` (28 PNGs + `.mcmeta`, list in
§3.4) and the matching `models/item/material_sets/infinity/*.json` into the port, then
declare a `MaterialIconSet("infinity", ...)`. Recommended only if the `Infinity` dust
recipe (`MagicGCYMRecipes.java:2128`) is ever un-skipped; today the port uses GTNN
`Infinity`, so **keep substitution**.

### 4.4 Keep substitution (with reason)

| Material | Keep | Reason |
|---|---|---|
| `Thaumium` | StainlessSteel | GTQT material; definition only exists in the un-cloned `meowmel.gtqtcore` 1.9.0 fork; 19 refs already work through the substitution |
| `Mana` | InfusedAura | same missing source; GTQT-Core 1.8.9's nearest equivalent (`magic`) is a different material and would not match upstream ratios |
| `Sunnarium` | Titanium dust | the clone has only a meta item, not a material; upstream use is 4 recipe inputs |
| `BloodOfAvernus` | TungstenSteel | Blood Magic is not a port dependency (11 refs all in BM chains) |
| `Orichalcos` / `Orichalcum` (GTQT) | — | consumer machine (Muti Dan De Life On) is not registered; upstream note at `MagicGCYMRecipes.java:98` |
| `Kobemetal` | — | no consumer in upstream or port |
| `BloodPlasma`, `PurifiedBlood`, `InfusedPurifiedBlood`, `ArcaneGelidFluid`, `CryogenicSyntheticBlood`, `ArcaneComputationalSubstrate`, `SyntheticComputationalBlood` | — | Blood Magic chain absent |
| `AstralBloodPlasma`, `CelestialBiologicalMedium`, `StarryArcaneAlloy` | — | Astral Sorcery absent |
| GTQT `Adamantium`, `MarsAir`, `UnderAir`, `GelidCryotheum`, `Cryolite` | — | no live consumers (skipped machines/veins) |
| GTQT `Acetylene`, `MethylFormate`, `Crotonaldehyde`, `Zylon`, `SodiumNitrate`, `Fluix` | Ethylene / MethylAcetate / Butyraldehyde / Polybenzimidazole / GTNN SodiumNitrate / AE2 fluix | GTCEu 7.5.3 removed or lacks these; substitutions already verified by the port's own audit |

### 4.5 Suggested order of work

1. **Cheap wins (no new dependencies):** port the five aura fluids (`WhiteMansus`,
   `BlackMansus`, `Starrymansus`, `ErichAura`, `RichAura`) + `Elven`; fix
   `SubstrateMaterials` colours and `Salismundus` components; re-enable the 5 skipped
   `ManaToEuRecipes` mana fluids and the white/starry rune altar paths that only needed
   `InfusedAura`.
2. **Chain completion:** obtain or reconstruct the `meowmel.gtqtcore` 1.9.0 definitions
   for `Mana`/`Thaumium`/`Sunnarium` (from the actual 1.12.0 jar next to Pollution, or
   from the GTQT 1.9.0 sources); then port `Manasteel` → `Mansussteel` → `Terrasteel` /
   `ElvenElementium` and switch the GTNN/HSSG substitutions only if the recipes are
   re-verified.
3. **Optional:** `EnergyCrystal`/`StarmetalAlloy` with a reworked `MaterialsLine`;
   GTQT custom icon-set materials (needs the GTQT-Core PNG sets listed in §3.4).

---

## 5. Verification notes

- Counts were produced by regex parsing of every builder in the stated files
  (`Material.builder(` for GTQT-GregTech; `new Material.Builder(` for GTQT-Core and
  Pollution). `GTQT-GregTech` has 752 raw builder calls vs 733 matched by the strict
  parser (19 multi-line/odd-format calls); the authoritative count is the 752
  `Materials.java` field declarations.
- `GTQT-Core` 1473 builders vs 1466 declared fields (7 builders not assigned to a field).
- Upstream/port builder counts and `PollutionMaterials` field counts agree (166/166 and
  137/137).
- No file outside this report was modified.
