# Hatch (仓室) audit — port vs. upstream 1.12

Audit of every ported multiblock pattern against its upstream counterpart in
`H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution\common\metatileentity\multiblock\**`.

* Upstream declares hatches with `Elements.abilities(...)`, `Elements.hatch(...)`,
  `Elements.metaTileEntitiesAsAbility(...)`, `Elements.abilitiesPerLayer(...)`,
  `.hatch('X', ...)`, `.globalAbilityLimit(ability, min, max)` and
  `.abilityGroup(display, min, max, abilities...)`.
* The port declares them with `Predicates.abilities(PartAbility...).setMinGlobalLimited(n)
  / setMaxGlobalLimited(n) / setExactLimit(n)` (and `Predicates.autoAbilities(...)`).
* Translation rule used: `globalAbilityLimit(ability, min, max)` →
  * `min == 0` → no `setMinGlobalLimited`;
  * `min == max == N` → `setExactLimit(N)`;
  * `min > 0` → `setMinGlobalLimited(min)`;
  * `max >= 0` → `setMaxGlobalLimited(max)`;
  * `max == -1` → no max.
  `abilityGroup(a, min, max, a, b)` → one shared predicate
  `Predicates.abilities(a, b).setMinGlobalLimited(min).setMaxGlobalLimited(max)`
  (GTCEu 7.5.3 keeps one `SimplePredicate` per `Predicates.blocks(...)` call, so the
  limits are shared by both abilities exactly like upstream's group).

Abbreviations used below: `E_IN`=INPUT_ENERGY, `E_OUT`=OUTPUT_ENERGY,
`L_IN`=INPUT_LASER, `L_OUT`=OUTPUT_LASER, `I_IN`=IMPORT_ITEMS, `I_OUT`=EXPORT_ITEMS,
`F_IN`=IMPORT_FLUIDS, `F_OUT`=EXPORT_FLUIDS, `MAINT`=MAINTENANCE, `MUFF`=MUFFLER,
`PAR`=PARALLEL_HATCH, `VIS`=VIS_HATCH, `INF`=INFUSED_FLUID_HATCH,
`MP`=MANA_INPUT_POOL, `MH`=MANA_INPUT_HATCH, `MO`=MANA_OUTPUT_HATCH,
`MOP`=MANA_OUTPUT_POOL. `ability[min,max]`, `-` = no constraint.

Result of the two repo checkers after the fixes:

```
python tools/check_pattern_chars.py   -> files with missing predicates: 0
python tools/check_pattern_limits.py  -> files with impossible limits: 0
```

---

## 1. Shared helpers

### 1.1 `MagicStructureElements` (upstream `configureMagicRecipeCasing`)

Upstream casing predicate (`DeclarativePatternBuilder#where(symbol, choice(casing, abilities(...)))`):

| ability | upstream | port before | port after | status |
| --- | --- | --- | --- | --- |
| E_IN + MH | `abilityGroup(MANA_INPUT_HATCH, 1, 2, {MH, E_IN})` | E_IN from `autoAbilities` `[1,2]`, MH absent | shared predicate `{MH, E_IN}[1,2]` | FIXED |
| MAINT | `[1,1]` | absent | `setExactLimit(1)` | FIXED |
| MUFF | `[1,1]` (machines with dedicated muffler char use `[0,1]`/own char) | absent | `setExactLimit(1)`, suppressed when the machine has a dedicated muffler slot | FIXED |
| VIS | `[0,1]` | `setMaxGlobalLimited(1)` | `setMaxGlobalLimited(1)` | OK |
| INF | `[1,1]` | `setMaxGlobalLimited(1)` | `setExactLimit(1)` | FIXED |
| MP | `[0,1]` | absent | `setMaxGlobalLimited(1)` | FIXED |
| I_IN/I_OUT/F_IN/F_OUT | recipe-dependent, no individual limit | `autoAbilities` (energy + items/fluids) | `autoAbilities(recipes, false,false,true,true,true,true)` | FIXED |
| TAROT | `[0,1]` | not registered in the port | `TAROT[-,1]` | FIXED (2026-09-20) |
| BLOOD/ASTRAL | `[0,1]` each | not registered in the port | not accepted | DEVIATION (documented) |
| `Elements.abilities(0, maxHatches, …)` shared slot cap | present | not expressible | not expressible (casing position count caps it) | DEVIATION (documented) |

### 1.2 `BotaniaStructureElements` (upstream `configureManaRecipeCasing`)

| ability | upstream | port before | port after | status |
| --- | --- | --- | --- | --- |
| MH + E_IN | `abilityGroup(MANA_INPUT_HATCH, 1, 2, {MH, E_IN})` | E_IN `[1,2]` from `autoAbilities`, MH `setMaxGlobalLimited(2)` | shared predicate `{MH, E_IN}[1,2]` | FIXED |
| MP | `[0,1]` | `setMaxGlobalLimited(1)` | `setMaxGlobalLimited(1)` | OK |
| MAINT | `[1,1]` | absent | `setExactLimit(1)` | FIXED |
| MUFF | `[1,1]` | absent | `setExactLimit(1)` | FIXED |
| I_IN/I_OUT/F_IN/F_OUT | recipe-dependent, no individual limit | `autoAbilities` | `autoAbilities(recipes, false,false,true,true,true,true)` | FIXED |

Affected machines: `IndustrialPureDaisy`, `ManaInfusionReactor`,
`ManaPetalApothecary`, `ManaRuneAltar`.

---

## 2. Per-machine audit

### 2.1 Magic processing machines (`magic/**`)

All machines below use `MagicStructureElements.magicCasing(...)` (§1.1) except
`MagicMacerator` (was plain casing, now converted) and `MagicFusionReactor`
(explicit predicate, upstream had no `MANA_INPUT_HATCH`).

| machine | char | upstream | port before | port after | status |
| --- | --- | --- | --- | --- | --- |
| MagicAlloyBlastSmelter | X | §1.1 (muffler `[1,1]`, maxHatches 31) | casing + autoAbilities + VIS/INF | §1.1 minus muffler | FIXED |
| MagicAlloyBlastSmelter | M | `MUFFLER_HATCH` hatch, global `[1,1]` | `MUFF` unlimited | `MUFF[1,1]` | FIXED |
| MagicAssembler | A | §1.1 (maxHatches 94) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicAutoclave | X | §1.1 (maxHatches 17) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicBender | X | §1.1 (maxHatches 16) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicBrewery | X | §1.1 (muffler `[1,1]`, maxHatches 24) | casing + autoAbilities + VIS/INF | §1.1 minus muffler | FIXED |
| MagicBrewery | M | `MUFFLER_HATCH` hatch, global `[1,1]` | `MUFF` unlimited | `MUFF[1,1]` | FIXED |
| MagicCentrifuge | X | §1.1 (maxHatches 17) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicChemicalBath | X | §1.1 (maxHatches 31) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicChemicalReactor | A | §1.1 (maxHatches 70) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicCutter | X | §1.1 (maxHatches 18) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicDistillery | Y | inline magic hatches (no slot cap; casing `counted(40,-1)`) | casing + autoAbilities + VIS/INF | §1.1 minus muffler | FIXED |
| MagicDistillery | X | `EXPORT_FLUIDS` per layer `[1,1]` | `F_OUT[-,1]` | `F_OUT[-,1]` | DEVIATION (per-layer → global cap, documented in javadoc) |
| MagicDistillery | C | `MUFFLER_HATCH` hatch, no global limit | `MUFF[1,1]` | `MUFF` unlimited | FIXED |
| MagicElectricBlastFurnace | X | §1.1, `includeMuffler=false` (maxHatches 10) | casing + autoAbilities + VIS/INF | §1.1 minus muffler | FIXED |
| MagicElectricBlastFurnace | M | `MUFFLER_HATCH` hatch, global `[0,1]` | `MUFF` unlimited | `MUFF[-,1]` | FIXED |
| MagicElectrolyzer | X | §1.1 (maxHatches 17) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicExtruder | X | §1.1 (maxHatches 20) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicGreenHouse | C | §1.1 (maxHatches 32) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicMacerator | X | §1.1 (maxHatches 16) | **plain casing, no hatches** | §1.1 | FIXED |
| MagicMacerator | I | `IMPORT_FLUIDS` hatch `[0,1]` | `F_IN` unlimited | `F_IN[-,1]` | FIXED |
| MagicMixer | X | §1.1 (maxHatches 28) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicSifter | X | §1.1 (maxHatches 9) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicSolidifier | X | §1.1 (maxHatches 35) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicWireMill | X | §1.1 (maxHatches 9) | casing + autoAbilities + VIS/INF | §1.1 | FIXED |
| MagicFusionReactor | A | E_IN, I_IN, I_OUT, F_IN, F_OUT, MAINT `[1,1]`, MUFF `[1,1]`, VIS `[0,1]`, INF `[1,1]`, MP `[0,1]`, BLOOD/ASTRAL/TAROT `[0,1]` | frame + `autoAbilities` (E_IN `[1,2]` + items/fluids) | frame + items/fluids + E_IN unlimited + MAINT `[1,1]` + MUFF `[1,1]` + VIS `[-,1]` + INF `[1,1]` + MP `[-,1]` + TAROT `[-,1]` | FIXED (BLOOD/ASTRAL deviation) |
| MagicBattery | A | MAINT `[1,1]`, E_IN `[1,16]`, E_OUT `[1,16]`, ASTRAL/TAROT `[0,1]` | E_IN `[-,16]`, E_OUT `[-,16]`, MAINT `[-,1]` | E_IN `[1,16]`, E_OUT `[1,16]`, MAINT `[1,1]` | FIXED (ASTRAL deviation; TAROT not accepted because the port battery is display-only and has no recipe logic to consume the card) |
| MagicLargeTurbine | R | tiered `ROTOR_HOLDER` `[1,1]` + `MANA_OUTPUT_HATCH` `[1,1]` | `ROTOR_HOLDER[-,2]` | `ROTOR_HOLDER[1,1]` + `MO[1,1]` | FIXED (tier filter dropped, documented) |
| MagicLargeTurbine | H | MAINT `[1,1]`, MUFF `[0,1]`, F_IN `[0,4]`, F_OUT `[0,4]`, ASTRAL/TAROT `[0,1]` | F_IN `[-,2]`, F_OUT `[-,1]`, E_OUT `[-,2]`, MAINT `[-,1]` | F_IN `[-,4]`, F_OUT `[-,4]`, E_OUT `[-,2]` (port EU deviation), MUFF `[-,1]`, MAINT `[1,1]`, TAROT `[-,1]` | FIXED (E_OUT extra, ASTRAL deviation) |
| MagicMegaTurbine | R | `REINFORCED_ROTOR_HOLDER` unlimited | `ROTOR_HOLDER[-,3]` | `ROTOR_HOLDER` unlimited | FIXED (ability mapping documented) |
| MagicMegaTurbine | M | `MUFFLER_HATCH` hatch, no limit | `MUFF[-,1]` | `MUFF` unlimited | FIXED |
| MagicMegaTurbine | A | MAINT `[1,1]`, I_IN `[0,1]`, F_IN `[1,4]`, F_OUT `[1,4]`, `MANA_OUTPUT_HATCH[0,8]`, ASTRAL/TAROT `[0,1]` | F_IN `[-,3]`, F_OUT `[-,1]`, E_OUT `[-,3]`, MAINT `[-,1]` | F_IN `[1,4]`, F_OUT `[1,4]`, I_IN `[-,1]`, `MO[-,8]`, E_OUT `[-,3]` (port EU deviation), MAINT `[1,1]`, TAROT `[-,1]` | FIXED (E_OUT extra, ASTRAL deviation) |
| SmallChemicalPlant | G | E_IN `[1,23]`, MAINT `[1,1]`, MUFF `[1,1]`, I_IN/I_OUT/F_IN/F_OUT `[1,23]` | all max-only (`[-,N]`) | min limits added, MAINT/MUFF exact | FIXED |
| EssenceCollector | A | MAINT `[1,1]`, E_IN `[0,2]` | E_IN `[-,2]`, MAINT `[-,1]` | E_IN `[-,2]`, MAINT `[1,1]` | FIXED |
| EssenceCollector | D | F_OUT `[6,6]` | `F_OUT[6,6]` | unchanged | OK |
| EssenceCollector | O | I_IN `[0,2]` | `I_IN[-,2]` | unchanged | OK |
| EssenceSmelter | B | I_IN `[1,27]`, F_IN `[1,1]`, E_IN `[0,2]`, MAINT `[1,1]` | I_IN `[-,1]`, F_IN `[-,1]`, E_IN `[-,2]`, MAINT `[-,1]` | I_IN `[1,27]`, F_IN `[1,1]`, E_IN `[-,2]`, MAINT `[1,1]` | FIXED |
| GtEssenceSmelter | B | I_IN `[1,27]`, F_IN `[1,1]`, F_OUT `[6,27]`, E_IN `[0,2]`, MAINT `[1,1]` | I_IN `[-,1]`, F_IN `[-,1]`, F_OUT `[-,6]`, E_IN `[-,2]`, MAINT `[-,1]` | I_IN `[1,27]`, F_IN `[1,1]`, F_OUT `[6,27]`, E_IN `[-,2]`, MAINT `[1,1]` | FIXED |
| IndustrialInfusion | B | E_IN, I_IN, I_OUT, F_IN, F_OUT, VIS `[0,1]`, INF `[1,1]`, MP `[0,1]`, BLOOD/ASTRAL/TAROT `[0,1]` | E_IN `[-,16]`, MAINT `[-,1]` | E_IN, I_IN, I_OUT, F_IN, F_OUT, VIS `[-,1]`, INF `[1,1]`, MP `[-,1]`, TAROT `[-,1]` | FIXED (BLOOD/ASTRAL deviation) |
| InfusedExchange | A | F_OUT `[0,1]` | `F_OUT` unlimited | `F_OUT[-,1]` | FIXED |

### 2.2 Node machines (`node/**`)

| machine | char | upstream | port before | port after | status |
| --- | --- | --- | --- | --- | --- |
| NodeBlastFurnace | B | E_IN `[1,91]`, L_IN `[0,3]`, I_IN/I_OUT/F_IN/F_OUT `[0,91]`, MAINT `[1,1]`, MUFF `[1,1]` | E_IN `[-,1]`, L_IN `[-,3]`, items/fluids unlimited, MAINT `[-,1]` | E_IN `[1,91]`, L_IN `[-,3]`, items/fluids unlimited, MAINT `[1,1]` | FIXED (muffler stays on Y, documented) |
| NodeBlastFurnace | Y | `MUFFLER_HATCH` hatch, global `[1,1]` | `MUFF[1,1]` | unchanged | OK |
| NodeFusionReactor | D | E_IN (meta-tile ability) `[1,16]` | `E_IN[-,16]`, MAINT `[-,1]` | `E_IN[1,16]`, MAINT removed (upstream has none) | FIXED |
| NodeFusionReactor | F | F_IN, F_OUT; global F_IN `[5,-1]` | `F_IN[-,5]` | `F_IN[5,-1]` | FIXED |
| NodeFusionReactor | G | E_IN meta-tile ability `[1,16]` | `F_OUT[-,1]` | `F_OUT[1,-1]` | FIXED |
| NodeFusionReactor | H | I_IN meta-tile ability `[1,1]` | `I_IN[1,1]` | unchanged | OK |
| LargeNodeGenerator | C | MAINT `[1,1]`, F_IN `[1,1]`, E_OUT `[0,1]`, L_OUT `[0,1]` | E_OUT `[-,1]`, L_OUT `[-,1]`, MAINT `[-,1]`, F_IN `[-,1]` | MAINT `[1,1]`, F_IN `[1,1]`, E_OUT `[-,1]`, L_OUT `[-,1]` | FIXED |
| LargeNodeGenerator | G | E_OUT, L_OUT (`[0,1]` each) | not used as a hatch char | unchanged | OK (hatches live on C) |
| LargeNodeGenerator | H | 6× ULV `ITEM_IMPORT_BUS` (`metaTileEntitiesAsAbility(I_IN, 6, 6, 6, …)`) | `I_IN[-,1]` | `Predicates.ability(I_IN, ULV)` `[6,6]` | FIXED |
| CentralVisTower | K | MP `[1,1]`, MAINT `[1,1]`, F_OUT `[3,8]` | F_OUT `[3,8]`, F_IN `[1,1]`, E_IN `[-,1]`, MAINT `[-,1]` | F_OUT `[3,8]`, F_IN `[1,1]`, E_IN `[1,1]`, MAINT `[1,1]` | FIXED (MP replaced by EU + InfusedAura, documented) |
| NodeProducer | X | MAINT, E_IN, F_IN; globals `[1,1]` each | E_IN `[-,1]`, F_IN `[-,1]`, I_OUT `[-,1]`, MAINT `[-,1]` | E_IN `[1,1]`, F_IN `[1,1]`, MAINT `[1,1]` (I_OUT removed, upstream keeps it on O) | FIXED |
| NodeProducer | O | I_OUT `[1,1]` | `I_OUT[-,1]` | `I_OUT[1,1]` | FIXED |
| NodeWasher | X | E_IN `[1,1]`, MAINT `[1,1]`, F_IN `[1,1]`, I_IN `[1,1]` | all `[-,1]` | all exact `[1,1]` | FIXED |

### 2.3 Botania machines (`botania/**`)

| machine | char | upstream | port before | port after | status |
| --- | --- | --- | --- | --- | --- |
| BotCircuitAssembler | C | `{MH, E_IN}` group `[1,2]`, MP `[0,1]`, MAINT `[1,1]`, I_IN/I_OUT/F_IN shared `[0,44]` | MH `[-,2]`, MP `[-,1]`, E_IN `[-,44]`, MAINT `[1,1]`, items/fluids `[-,44]` | `{MH, E_IN}[1,2]`, MP `[-,1]`, MAINT `[1,1]`, items/fluids `[-,44]` | FIXED |
| BotDistillery | X | E_IN `[1,2]`, F_IN `[1,1]`, I_OUT `[1,1]`, MH `[1,1]`, MP `[0,1]`, PAR `[1,1]`, MAINT `[1,1]`, MUFF `[1,1]` | E_IN `[-,2]`, others exact, MP `[1,1]` | E_IN `[1,2]`, MP `[-,1]`, others exact | FIXED |
| BotDistillery | M | `EXPORT_FLUIDS` per layer `[1,1]` | per-layer `[1,1]` | unchanged | OK |
| BotGasCollector | A | MP `[1,1]`, MAINT `[1,1]`, F_IN `[2,32]`, F_OUT `[1,32]` | identical | unchanged | OK |
| BotVacuumFreezer | X | `{MH, E_IN}` group `[1,2]`, MP `[0,1]`, MAINT `[1,1]`, items/fluids shared `[0,13]` | MH `[-,2]`, E_IN `[-,2]`, MP `[-,1]`, MAINT `[1,1]`, items/fluids `[-,13]` | `{MH, E_IN}[1,2]`, MP `[-,1]`, MAINT `[1,1]`, items/fluids `[-,13]` | FIXED |
| EndoflameArray | A | I_IN `[1,27]`, MOP `[1,1]`, MAINT `[1,1]` | I_IN `[1,27]`, MOP `[-,1]`, MAINT `[-,1]` | I_IN `[1,27]`, MOP `[1,1]`, MAINT `[1,1]` | FIXED |
| IndustrialPureDaisy | C | §1.2 | casing + autoAbilities + MH/MP max-only | §1.2 | FIXED |
| ManaInfusionReactor | B | §1.2 | casing + autoAbilities + MH/MP max-only | §1.2 | FIXED |
| ManaPetalApothecary | E | §1.2 | casing + autoAbilities + MH/MP max-only | §1.2 | FIXED |
| ManaRuneAltar | A | §1.2 | casing + autoAbilities + MH/MP max-only | §1.2 | FIXED |
| ManaPlate | C | MP `[0,1]`, global `[1,1]` | `MP[1,1]` | unchanged | OK |
| MegaManaTurbine | H | MAINT `[1,1]`, F_IN `[0,4]`, F_OUT `[0,4]`, MUFF `[0,1]` (group cap 9/10) | MAINT `[-,1]`, F_IN `[-,4]`, F_OUT `[-,4]`, MUFF `[-,1]` | MAINT `[1,1]`, rest unchanged | FIXED |
| MegaManaTurbine | J | E_OUT `[0,1]`, L_OUT `[0,1]` | identical | unchanged | OK |
| MultiDanDeLifeOn | G | E_OUT `[0,1]`, L_OUT `[0,1]`, MAINT `[1,1]`, E_IN `[1,-1]`, F_IN `[1,-1]` | E_OUT/L_OUT/MAINT `[-,1]`, E_IN/F_IN `[1,-]`, plus I_IN `[-,1]`, F_OUT `[-,1]` | MAINT `[1,1]`, extra I_IN/F_OUT removed | FIXED |

---

## 3. Remaining deviations (intentional / documented)

1. **BLOOD_MAGIC_HATCH / ASTRAL_LENS_HATCH** — upstream accepted them
   (`[0,1]`) on the magic casing / frame of `MagicAlloyBlastSmelter`,
   `MagicBrewery`, `MagicDistillery`, `MagicElectricBlastFurnace` (via
   `configureMagicRecipeCasing`), `MagicFusionReactor`, `MagicBattery`,
   `MagicLargeTurbine`, `MagicMegaTurbine`, `IndustrialInfusion`. These two
   abilities are not registered in the port yet, so the port cannot accept the
   parts. Noted in the affected machine javadocs.
   `TAROT_HATCH` was ported on 2026-09-20 (`POMultiblockAbility.TAROT_HATCH`,
   `TarotHatchMachine`, `TAROT[-,1]` on the shared magic casing and the explicit
   fusion-reactor / turbine / industrial-infusion patterns). `MagicBattery`
   deliberately does not accept it: the port battery is display-only and has no
   recipe logic that could consume the card.
2. **CentralVisTower `MANA_INPUT_POOL[1,1]` → `INPUT_ENERGY[1,1]` +
   `IMPORT_FLUIDS[1,1]` (InfusedAura)** — the machine was rewritten around EU and
   an InfusedAura upkeep because TC4R has no ambient aura; documented in
   `CentralVisTowerMachine`.
3. **Per-layer export-fluid limits** — `MagicDistillery` X
   (`abilitiesPerLayer(1,1,1, EXPORT_FLUIDS)`) is approximated by a global
   `F_OUT[-,1]`; `BotDistillery` M keeps real per-layer limits
   (`setMinLayerLimited(1).setMaxLayerLimited(1)`).
4. **Shared slot caps** (`Elements.abilities(0, maxHatches, …)`) cannot be
   expressed per-slot in GTCEu 7.5.3; the casing position count caps the
   number of hatches instead. Individual ability limits from
   `globalAbilityLimit` are all applied.
5. **Muffler placement** — where upstream allowed the muffler on both the casing
   and a dedicated slot (`MagicAlloyBlastSmelter`, `MagicBrewery`,
   `NodeBlastFurnace`), the port keeps it on the dedicated slot with
   `setExactLimit(1)` (global limit still 1).
6. **MagicDistillery `C` muffler** has no limit in the port because upstream
   declared no `globalAbilityLimit` for it (unlimited in upstream as well).
7. **MagicLargeTurbine / MagicMegaTurbine** additionally keep the port's
   `OUTPUT_ENERGY` hatches (EU output rewrite); upstream only had
   `MANA_OUTPUT_HATCH`. `MagicMegaTurbine` maps upstream's GTQT
   `REINFORCED_ROTOR_HOLDER` to the standard `ROTOR_HOLDER`.
8. **LargeNodeGenerator H** — upstream required 6 ULV item import buses
   (`metaTileEntitiesAsAbility(IMPORT_ITEMS, 6, 6, 6, ITEM_IMPORT_BUS[ULV])`);
   the port uses `Predicates.ability(IMPORT_ITEMS, GTValues.ULV)` with an exact
   limit of 6.
9. **LargeNodeGenerator H** uses the `setMinGlobalLimited(6, 6)` /
   `setMaxGlobalLimited(6, 6)` two-arg overloads (limit + preview count) for the
   exact-6 requirement; the repo checker's per-layer heuristic cannot represent a
   global minimum larger than the per-layer character count (`H` occurs once per
   layer but six times in the structure).

## 4. Upstream machines not ported (not audited)

`astral/**` (CelestialCalibrationMatrix, CelestialCrystalGrowthArray,
CelestialObservationArray, ConstellationTower, IndustrialLightwell,
IndustrialStarlightInfuser, StarstreamNexusObelisk),
`bloodMagic/MetaTileEntityBMHPCA`, `MetaTileEntityFluxClear`.
Their patterns (including the ASTRAL/BLOOD hatches; the TAROT hatch itself is
ported and wired into the magic casing) have no port counterpart and are out of
scope of this audit.
