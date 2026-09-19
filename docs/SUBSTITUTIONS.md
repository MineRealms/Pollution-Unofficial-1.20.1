# Substitution audit: `// 上游: X -> 本移植版: Y`

Single audit table for every `上游 -> 本移植版` annotation found in
`src/main/java/meowmel/pollution/loaders/recipes/`. The comments were collected
by scanning the recipe sources; line references are to the current revision of
each file. Grouped by file, then a consolidated list of upstream content that
is still unmapped or skipped.

Legend: `->` separates the upstream material/item from the ported replacement.

## Summary

| File | Substitution entries |
| --- | ---: |
| `AERecipes.java` | 1 |
| `BotaniaRecipes.java` | 3 |
| `ForgeAlchemyRecipes.java` | 9 |
| `InfusionRecipes.java` | 26 |
| `MagicChemicalRecipes.java` | 38 |
| `MagicGCYMRecipes.java` | 30 |
| `MagicIntegrationRecipes.java` | 11 |
| `NodeFusionRecipes.java` | 2 |
| `StarstreamNexusRecipes.java` | 2 |
| `ThaumcraftRecipes.java` | 2 |
| **Total** | **124** |

Files in the package without any `上游 -> 本移植版` annotation:
`BotaniaNativeRecipes`, `CoilRecipes`, `CompoundAspectRecipes`,
`DandelifeonRecipe`, `GCYRRocketFuels`, `GTNNRocketFuels`,
`InfusedProcessingRecipes`, `MagicFuelRecipes`, `ManaToEuRecipes`,
`PollutionRecipes`, `SafeItems`.

## AERecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `nae2:material` 19-22 / 24-27 (256k/1M/4M/16M item/fluid cell components) | AE2 `cell_component_256k` (AE2 15 ships max 256k; 1M/4M/16M output 4x/16x/64x) | 71, 371 |

## BotaniaRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `block BloodOfAvernus` | TungstenSteel block | 120 |
| `block VoidMetal` | TungstenSteel block | 121 |
| `wireGtSingle ManaSteel` x8 | ManaSteel ingot x4 (GTNN ManaSteel has ingot/fluid only) | 374 |

## ForgeAlchemyRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| GTQT Mana | InfusedAura | 43 |
| BlackMansus / WhiteMansus / Starrymansus | InfusedAura | 44 |
| Terrasteel | GTNN TerraSteel | 45 |
| ElvenElementium | GTNN Elementium | 46 |
| Manasteel | GTNN ManaSteel | 47 |
| GTQT Thaumium | StainlessSteel | 48 |
| Mansussteel | HSSG | 49 |
| BloodOfAvernus | TungstenSteel | 50 |
| GTQT VoidMetal | TC4R `thaumcraft:void_ingot` | 51 |

## InfusionRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `ItemsTC.visResonator` | TC4R `thaumcraft:resonator` (`ESSENTIA_RESONATOR`) | 93, 153 |
| `ItemsTC.morphicResonator` | TC4R `thaumcraft:node_transducer` (`NODE_TRANSDUCER`) | 94, 153 |
| `ItemsAS.skyResonator` | TC4R `NODE_TRANSDUCER` | 154 |
| `ItemsTC.causalityCollapser` | TC4R `thaumcraft:primordial_pearl` (`PRIMORDIAL_PEARL`) | 95 |
| `BlocksTC.visBattery` | TC4R `thaumcraft:vis_charge_relay` (`VIS_CHARGE_RELAY`) | 96 |
| `BlocksTC.infusionMatrix` / `matrixCost` / `matrixSpeed` | TC4R `thaumcraft:runic_matrix` (`RUNIC_MATRIX`) | 97, 1016 |
| `ItemsTC.voidSeed` | TC4R `thaumcraft:eldritch_object` (`ELDRITCH_OBJECT`) | 99, 703 |
| `ItemsTC.creativeFluxSponge` | TC4R `thaumcraft:sanity_soap` (`SANITY_SOAP`) | 100, 811 |
| `PollutionMetaBlocks.FUSION_REACTOR` `FRAME_*` | GTCEu fusion casing (`GTBlocks.FUSION_CASING`) | 101, 946 |
| `PollutionMetaBlocks.FUSION_REACTOR` `COMPOSE_*` | GTCEu fusion coil (`GTBlocks.FUSION_COIL`) | 102, 946 |
| `plateMansussteel` / `frameGtMansussteel` | HSSG plate / frame | 104, 155 |
| `plateManasteel` | GTNN ManaSteel ingot (GTNN carries ingot/fluid only) | 105, 1082 |
| `plateIgnissteel` | IgnisSteel ingot (alloy carries ingot/fluid) | 107, 905 |
| `plateOctine` | Octine dust (port material carries dust/fluid) | 109, 679, 1005 |
| `dustSunnarium` | Titanium dust | 110, 641 |
| `blockTerrasteel` | 9x GTNN TerraSteel ingot (no block form) | 111, 759 |
| `blockValonite` / `blockSubstrate` | 9x gem / dust (no block form) | 111, 663, 757 |
| `blockKeqinggold` | TungstenSteel block | 113 |
| `blockHyperdimensionalSilver` | NaquadahAlloy block | 113 |
| `blockUranium235` | Uranium235 block | 114 |
| `FILTER_MKIII` | port item `FILTER_III` | 155 |
| ritual crystal (`POConstellationCrystal`, unported) | TC4R order crystal cluster | 216 |
| liquid starlight | InfusedAura | 217 |
| `blockVoid` | 9x TC4R `void_ingot` | 1046 |
| `AURA_GENERATORS[4]` | port `VIS_GENERATOR[4]` | 1060 |
| upstream vis hatch tiers ULV..EV | port tiers LV..IV | 920 |

## MagicChemicalRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| GTQT Mana | InfusedAura | 63, 292, 332, 955, 1443, 1600 |
| BlackMansus | InfusedAura | 63, 538, 955, 1405, 1443, 1600 |
| WhiteMansus | InfusedAura | 63, 538, 955, 1336, 1443, 1600 |
| Starrymansus | InfusedAura | 553 |
| ErichAura | InfusedAura | 294, 351 |
| GTQT Polystyrene | StyreneButadieneRubber | 64 |
| GTQT Zylon | Polybenzimidazole | 65 |
| GTQT Polyetheretherketone | Epoxy | 66 |
| GTQT Kevlar | Polybenzimidazole | 66 |
| GTQT KaptonE | Epoxy | 66 |
| GTQT KaptonK | PolyphenyleneSulfide | 67 |
| SiliconTetrachloride | Silicon dust + Chlorine (GTCEu 7.5.3 has no SiCl4) | 68, 1678 |
| `ItemsTC.causalityCollapser` | `PRIMORDIAL_PEARL` | 70, 1299, 1773 |
| ZirconiumTetrachloride | TitaniumTetrachloride | 73, 1795 |
| ZirconiumTetrachloride | Zirconium dust (sugar -> HMF chain) | 1774 |
| Crotonaldehyde | Butyraldehyde | 74, 1797 |
| MethylFormate | MethylAcetate | 75, 1812 |
| `ItemsTC.brain` | TC4R `ZOMBIE_BRAIN` | 76, 1844 |
| `BlocksTC.logGreatwood` | TC4R `GREATWOOD_LOG` | 78, 1914 |
| `BlocksTC.logSilverwood` | TC4R `SILVERWOOD_LOG` | 78, 1914 |
| Mansussteel | HSSG | 82, 126, 1482, 1548 |
| GTQT Thaumium | StainlessSteel | 82, 127, 1483, 1548 |
| `dust Ordolead` | `ingot Ordolead` (alloy has ingot/fluid only) | 134 |
| `dust Valonite` | `gem Valonite` (Valonite has gem/fluid only) | 144 |
| `cableGtSingle Osmiridium` | `cableGtSingle Trinium` | 224 |
| GTQT VoidMetal | TC4R `void_ingot` | 292, 371 |
| `ItemsTC.voidSeed` | `ELDRITCH_OBJECT` | 293, 372, 393, 958, 1407 |
| GTQT SodiumNitrate | GTNN SodiumNitrate | 415 |
| GTQT Acetylene | Ethylene | 416, 458 |
| GTQT HafniumTetrachloride | TitaniumTetrachloride | 416, 476 |
| CarbonTetrachloride | Chloroform (removed from GTCEu 7.5.3) | 954, 1013, 1046 |
| Manasteel | GTNN ManaSteel ingot (GTNN has no dust) | 956, 1218 |
| `dust EnderEye` | `gem EnderEye` (no dust form in 7.5.3) | 957, 1406 |
| `block IizunamaruElectrum` | real material (ported) | 538 |
| `block SentientMetal` | real material (ported) | 553 |
| `block ExistingNexus` | real material (ported) | 569 |
| upstream advanced battery content recipe outputs `BasicBatteryContent` | corrected to `AdvancedBatteryContent` | 165 |
| upstream stone-4 duplication outputs the tier-3 stone | corrected to the tier-4 stone | 570 |

## MagicGCYMRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `BlocksTC.crystalAir/Fire/Water/Earth/Order/Entropy` | `thaumcraft:*_crystal_cluster` | 63, 592 |
| `plate` / `frameGt` of the six aspect alloys | same alloy ingot + HSSG frame (alloys carry ingot/fluid only) | 66, 630 |
| `ItemsTC.morphicResonator` | `NODE_TRANSDUCER` | 75, 880, 997 |
| IizunamaruElectrum | Electrum (header mapping; advanced components now use the real ported material) | 79 |
| AethericDarkSteel | HSSG (header mapping; advanced components now use the real ported material) | 79 |
| BloodOfAvernus | TungstenSteel | 83, 532, 914 |
| GTQT VoidMetal | TC4R `void_ingot` | 84, 914 |
| `ItemsTC.causalityCollapser` | `PRIMORDIAL_PEARL` | 85, 914 |
| Starrymansus / BlackMansus / WhiteMansus | InfusedAura | 86, 914 |
| ErichAura | InfusedAura | 87, 1058 |
| `plateMansussteel` | HSSG plate | 89, 1091 |
| `plateThaumium` | StainlessSteel plate | 90, 1091 |
| `ItemsTC.visResonator` | `ESSENTIA_RESONATOR` | 90, 998 |
| `BlocksTC.visBattery` | `VIS_CHARGE_RELAY` | 92, 999 |
| Botania `altGrass` (removed in 1.20.1) | vanilla grass block input + Botania `enchanted_soil` output | 93, 130 |
| `dust Terrasteel` | GTNN TerraSteel ingot | 95 |
| `dust <alloy>` | `ingot <alloy>` (six element alloys carry ingot/fluid only) | 176 |
| Manasteel | GTNN ManaSteel | 311 |
| Mansussteel | HSSG | 345 |
| parallel hatch IV..UV | GCYMMachines `PARALLEL_HATCH` LuV..UHV (GTCEu 7.5.3 window) | 54, 412 |
| ElvenElementium | NaquadahAlloy (Terra circuit assembler casing) | 532 |
| GTQT Mana | InfusedAura | 646, 675 |
| Botania rune meta 1/3/2/4/6 | named runes (`rune_water`/`rune_fire`/...) | 646, 675 |
| `BlocksTC.smelterThaumium` | TC4R `ALCHEMICAL_FURNACE` | 813 |
| `frameGtTerrasteel` | TungstenSteel frame | 813 |
| `gear HyperdimensionalSilver` | NaquadahAlloy gear | 813 |
| GTQT `CHEMICAL_PLANT` | GTCEu large chemical reactor | 834 |
| upstream `LARGE_GAS_COLLECTOR` (not in 7.5.3) | single-block `GTMachines.GAS_COLLECTOR` | 59, 509 |
| tiered `circuit` items | modern `circuitMeta` configuration input | 60 |
| 84 tiered assembly-line circuits | single `circuitMeta(4)` | 466 |

## MagicIntegrationRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `ItemsTC.salisMundus` | Salisundus dust | 42, 215 |
| `BasicSubstrate ingot` | `BasicSubstrate` fluid (port material is fluid-only) | 44, 323 |
| `plate Manasteel` | GTNN ManaSteel ingot | 46, 324 |
| GTQT Mana | InfusedAura | 48, 323 |
| `ItemsTC.visResonator` | `ESSENTIA_RESONATOR` | 49, 368 |
| liquid starlight | InfusedAura | 50 |
| CelestialBiologicalMedium | InfusedAura (Blood Magic absent) | 53, 392 |
| `frameGtMansussteel` | HSSG frame | 97 |
| `ItemsTC.morphicResonator` | `NODE_TRANSDUCER` | 100 |
| Botania rune meta 3 | `botania:rune_air` | 148 |
| ArcaneInk | real registered fluid (no longer substituted) | 200 |

## NodeFusionRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `screw BloodOfAvernus` | TungstenSteel screw | 51 |
| StarmetalAlloy (unported) | NaquadahAlloy | 154 |

## StarstreamNexusRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| liquid starlight | InfusedAura | 31, 88 |
| Astral Sorcery marble bricks | GTCEu marble | 62 |

## ThaumcraftRecipes.java

| Upstream | Port | Source |
| --- | --- | --- |
| `ItemsTC.ingots` (Thaumcraft ingot meta) | TC4R `thaumcraft:thaumium_ingot` | 38, 62 |
| Botania `ModItems.manaResource` Manasteel maceration | not registered (GTNN ships `gtceu:macerator/macerate_manasteel_ingot`; duplicate rejected by the lookup DB) | 58 |

## Still unmapped / skipped upstream content

### Missing mods (not in the pack)

| Upstream content | Where | Note |
| --- | --- | --- |
| Astral Sorcery: foundational materials, optics, starmetal alchemy, rock-crystal catalysis, attuned wafers, advanced astral components, three celestial machines, ZPM/UV/UHV+ constellation circuit boards, liquid starlight | `MagicIntegrationRecipes` 58-72, `StarstreamNexusRecipes` 38-49, `InfusionRecipes` 121-123 | whole groups skipped; the two recipes that only needed starlight substitute InfusedAura |
| Blood Magic: blood altar slate, blood culture line (living biofilm, ultimate/supreme circuit boards), life essence, BloodPlasma, CelestialBiologicalMedium, InfusedPurifiedBlood, BloodOfAvernus transmutation | `MagicIntegrationRecipes` 66-71, `MagicChemicalRecipes` 92, `ForgeAlchemyRecipes` 65-66, 459 | 6-recipe blood chain + bridge skipped |
| GTFO: industrial banana easter egg; magic greenhouse main-block infusion | `MagicChemicalRecipes` 91, `InfusionRecipes` 119, `MagicGCYMRecipes` 100 | greenhouse recipes themselves are ported |

### Unported Pollution content

| Upstream content | Where | Note |
| --- | --- | --- |
| GTQT Orichalcum material and its machine (`Muti Dan De Life On` assembly line) | `MagicGCYMRecipes` 98 | skipped |
| Blood Magic HPCA machine group (`BMHPCA`) | `MagicGCYMRecipes` 99 | skipped |
| Magic small turbine machine `turbine_1..3` | `MagicGCYMRecipes` 101 | arcane turbine recipes skipped |
| Starstream network blocks/machines: `CONSTELLATION_CRYSTAL`, `STARSTREAM_RELAY`, `STARSTREAM_INTERDIMENSIONAL_RELAY`, `STARSTREAM_CHUNK_ANCHOR`, `STARSTREAM_OPERATION_CORE`, `STARSTREAM_NEXUS_OBELISK`, `OBELISK_CORE` | `StarstreamNexusRecipes` 40-48, `InfusionRecipes` 121-123 | only the two structural casings + linker are ported |
| `precision_rune_blank` (`PollutionMetaItems`) | `BotaniaRecipes` 67-72 | 3 custom rune-altar recipes wait on the item |
| `ORDINARY_ALGAE` GTQT item | `BotaniaRecipes` 76-77 | 16 petal variants skipped |
| Mana input hatches UEV/UIV/UXV/OpV | `BotaniaRecipes` 78-79 | hatch arrays stop at UHV |
| `ItemsTC.visResonator` / `morphicResonator` / `BlocksTC.visBattery` efficient arcane recipes | `ThaumcraftRecipes` 31-34 | rewritten as GT assembler recipes instead |
| `MANA_RESONANCE_COIL` | `BotaniaRecipes` 73-75, 365-383 | ported conditionally; needs `botania:spark`, skipped with a log line when absent |

### Upstream content absent from GTCEu 7.5.3 / AE2 15 / TC4R

| Upstream content | Replacement / status | Where |
| --- | --- | --- |
| `LARGE_GAS_COLLECTOR` | single-block `GTMachines.GAS_COLLECTOR` | `MagicGCYMRecipes` 59 |
| `MetaTileEntities.PARALLEL_HATCH[0..3]` (IV..UV) | `GCYMMachines.PARALLEL_HATCH` at LuV..UHV | `MagicGCYMRecipes` 54 |
| `MetaItems.*` circuit / SMD / CPU items | GTCEu registry ids (`cpu_chip`, `ram_chip`, `ulpic_chip`, `lpic_chip`, `mpic_chip`, `hpic_chip`, `uhpic_chip`, `nano_cpu_chip`, `resistor`, ...) | `AERecipes`, `MagicIntegrationRecipes`, `MagicGCYMRecipes`, `InfusionRecipes` |
| nae2 1M/4M/16M cell components | AE2 `cell_component_256k` x4/x16/x64 | `AERecipes` 71 |
| AE2 15 fluid interface / fluid bus parts | shared `interface` / `import_bus` / `export_bus`; 2 duplicate fluid-bus recipes skipped | `AERecipes` 60-71 |
| GTQT Mana, ErichAura, Starrymansus, BlackMansus, WhiteMansus | InfusedAura | multiple files (see tables) |
| GTQT VoidMetal, `ItemsTC.causalityCollapser`, `ItemsTC.voidSeed`, `ItemsTC.brain` | TC4R void ingot / primordial pearl / eldritch object / zombie brain | multiple files (see tables) |
| Sunnarium | Titanium | `InfusionRecipes`, `ThaumcraftRecipes` |
| StarmetalAlloy | NaquadahAlloy | `NodeFusionRecipes` 154 |
| CarbonTetrachloride, ZirconiumTetrachloride, HafniumTetrachloride, Crotonaldehyde, MethylFormate | Chloroform, TitaniumTetrachloride, Butyraldehyde, MethylAcetate | `MagicChemicalRecipes` |
| `dust`/`plate`/`block` forms missing on ported GTNN/alloy materials | ingot / gem / dust equivalents (9x for block -> 9 ingots) | `MagicChemicalRecipes`, `MagicGCYMRecipes`, `InfusionRecipes`, `BotaniaRecipes` |
| GTNN `ManaSteel` maceration duplicate | dropped (GTNN ships `gtceu:macerator/macerate_manasteel_ingot`) | `ThaumcraftRecipes` 58 |
