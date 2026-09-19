# Hatch semantics & tooltip audit

Audit of the Pollution part machines (mana / pool / wireless mana, vis,
infused fluid, flux muffler) and of the multiblock patterns that use them,
against the 1.12 upstream in
`H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution\common\metatileentity\**`.

Structure/limit auditing is covered by `docs/HATCH_AUDIT.md`; this document
covers **what each hatch does and whether the tooltip says so**, plus the
pattern-vs-machine-logic mismatches found while checking the same code paths.

---

## 1. What the hatches actually do

| part | implementation | stores / moves | GT energy interface? |
| --- | --- | --- | --- |
| `ManaHatchMachine` (energy-type, `mana_input/output_hatch_*`) | `NotifiableEnergyContainer` + Botania `ManaReceiver` | input: receives Botania bursts/sparks and adjacent output hatches, the multiblock draws it as EU at `V[tier] × A`; output: the multiblock fills it as EU and it pushes up to `V[tier] × A` mana/t to neighbours. Capacity `V×16×A` in / `V×64×A` out. | yes — it **is** the GT energy interface of the magic multiblocks (1 mana = 1 EU internally) |
| `ManaPoolHatchMachine` (pure mana, `mana_pool_*_hatch_*`) | `NotifiableManaContainer` (`IManaHatch`) + Botania `ManaReceiver` | input: supplies `IManaHatch` recipes, receives bursts and adjacent output pools (intake throttled to `V[tier]`/t); output: receives multiblock mana and pushes to neighbours (output throttled to `V[tier]`/t). Capacities: diluted 10 000, normal/mythic 1 000 000. | no — not registered as an EU recipe handler |
| `WirelessManaHatchMachine` / `WirelessManaPoolHatchMachine` | subclasses with no overrides | identical to the non-wireless parts; the 1.12 `WirelessManager` network is **not ported** (documented TODO) | same as their parents |
| `VisHatchMachine` | `IVisHatch` buffer | drains 0.05 Vis/s from the TC4R vis network (channels cycled), stores `tier` Vis per successful drain, capacity `tier × 2000` | no |
| `InfusedFluidHatchMachine` | `NotifiableFluidTank` (`IO.BOTH`), 1 tank | buffers aspect-mapped infused fluids for magic multiblock recipes, capacity `8000 × 2^tier` mB | no |
| `FluxMufflerMachine` | `IMufflerMachine` | recovers machine byproducts with `min((tier-1)×10, 100)%` chance; the machine's industrial pollution is emitted through this hatch | no |

Upstream references:
`MetaTileEntityManaHatch`, `MetaTileEntityManaPoolHatch`,
`MetaTileEntityWirelessManaHatch`, `MetaTileEntityWirelessManaPoolHatch`,
`MetaTileEntityVisHatch`, `MetaTileEntityInfusedFluidHatch`,
`MetaTileEntityFluxMuffler`, plus upstream lang
`pollution.machine.mana_energy_hatch.tooltip`,
`pollution.machine.mana_pool_hatch.tooltip`,
`pollution.machine.mana_pool_input/output_hatch.tooltip`.

## 2. Tooltip mismatches found & fixed

| key | problem | fix |
| --- | --- | --- |
| `pollution.machine.mana_hatch.tooltip` | EN said "buffers mana"; nothing said the hatch is the **GT energy interface** | zh + EN now say "energy-type mana hatch … GT energy interface (1 Mana = 1 EU internally)" (upstream `mana_energy_hatch.tooltip`) |
| mana hatch voltage/amperage/capacity lines | built from `gtceu.universal.tooltip.voltage_out/in`, `amperage_out/in_till`, `energy_storage_capacity`, i.e. the item tooltip read "Output Voltage … EU/t", "Energy capacity … EU" although the hatch stores mana | new `pollution.machine.mana_hatch.input_rate / output_rate / capacity` keys describe mana rate (`V×A Mana/t`), tier/amperage and mana buffer; `PollutionMachines.manaHatchTooltips` no longer uses the GT EU keys |
| `pollution.machine.mana_pool_hatch.tooltip` | did not say the pool hatch is **not** an EU interface | zh + EN now match upstream "pure mana pool hatch … not a GT energy interface" |
| `pollution.machine.mana_pool_hatch.capacity/transfer` | units said "魔力" / "Mana" without the pool semantics | now "纯魔力容量：%s Mana" / "最大传输速率：%s Mana/t" (EN mirrors) |
| `pollution.machine.mana_pool_input_hatch.tooltip` | only mentioned receiving; omitted that it **supplies the multiblock** | zh + EN now: supplies the multiblock; receives Botania bursts/sparks and adjacent output hatches (upstream wording) |
| `pollution.machine.mana_pool_output_hatch.tooltip` | only mentioned neighbour output; omitted that it **receives multiblock output** | zh + EN now: receives multiblock mana, outputs to adjacent receivers/input hatches |
| `pollution.machine.wireless_mana_hatch/pool_hatch.tooltip` | EN was a bare name; the wireless network is not ported | EN now states the network is not ported and the part behaves like the normal hatch (zh already did) |
| `pollution.machine.vis_hatch.tooltip.capacity/buffer/drain` | buffer said "units" instead of Vis; drain did not state the real 0.05 Vis/s | now "Vis"; "Stores %s Vis per successful drain"; "Drains 0.05 Vis/s from the Thaumcraft 4R vis network" |
| `pollution.machine.infused_fluid_hatch.tooltip` | "buffers infused fluid" omitted that only aspect-mapped infused fluids are accepted and that the tank feeds recipes | zh + EN now match upstream "stores infused fluids and supplies them to magic multiblock recipes" |
| `pollution.machine.flux_muffler.tooltip` | claimed it "keeps byproducts out of the environment"; the hatch actually **recovers** byproducts and is the machine's pollution vent (`MagicRecipeLogic#emitMufflerPollution`) | zh + EN now: recovers byproducts; industrial pollution is vented here |
| `pollution.machine.pollution_multi_dan_de_life_on.mode0/1` (EN) | EN said "Accelerated growth"/"Slowed growth" while zh and the code say energy/fluid output | EN now "Energy output"/"Fluid output" |

## 3. Pattern-vs-function mismatches found & fixed

1. **`MagicLargeTurbine` / `MagicMegaTurbine` mana output hatch — kept.**
   Upstream `MetaTileEntityMagicLargeTurbine#initializeAbilities` and
   `MetaTileEntityMagicMegaTurbine#initializeAbilities` set the turbine's
   `energyContainer` to `getAbilities(POMultiblockAbility.MANA_OUTPUT_HATCH)`
   and the `R` / `A` slots require one mana output hatch. The turbine burns
   fuel and pushes generated EU into that hatch, which emits it as Botania
   mana. The port keeps the requirement; only the tooltip was wrong (§2). The
   port additionally accepts a standard `OUTPUT_ENERGY` hatch (port-side EU
   deviation, documented in `docs/HATCH_AUDIT.md` §3.7 and in the machine
   javadoc). Upstream's mega turbine variant `MetaTileEntityMegaManaTurbine`
   (botania package) uses standard `OUTPUT_ENERGY`/`OUTPUT_LASER` and matches
   `MegaManaTurbinePatterns` — no change.

2. **`MultiDanDeLifeOn` (Life Activation Garden) — pattern fixed.**
   The machine javadoc and `DandelifeonRecipe` require an item import bus
   (pixie dust) and fluid mode fills an `EXPORT_FLUIDS` hatch
   (`findFluidOutput`), but `MultiDanDeLifeOnPatterns` only allowed
   `IMPORT_FLUIDS`. Upstream had the same bug (its own fairy-dust recipe could
   never run). The `G` predicate now also accepts `IMPORT_ITEMS` and
   `EXPORT_FLUIDS`, matching the machine javadoc's stated intent.

3. **`NodeFusionReactor` — code fixed.**
   `tickReactor` used `findPart(FluidHatchPartMachine.class)`, i.e. the first
   fluid hatch regardless of direction, while the pattern allows both
   `IMPORT_FLUIDS` and `EXPORT_FLUIDS`. Upstream
   `MetaTileEntityNodeFusionReactor` used `getAbilities(IMPORT_FLUIDS)`. The
   port now looks up the `IMPORT_FLUIDS` hatch for the mansus checks/drains.

4. **`GtEssenceSmelter` — code fixed.**
   The single `findPart(FluidHatchPartMachine.class)` was used both to drain
   InfusedFire (input) and to fill the essence fluids (output). Upstream used
   `getAbilities(IMPORT_FLUIDS)` for the upkeep and `outputFluidInventory`
   (`EXPORT_FLUIDS`) for the products. The port now resolves both hatches
   separately.

5. **`MagicBattery` — code fixed.**
   `tickBattery` treated the first and last energy hatches as source/target
   without checking IO, so with an output hatch first it drained the output
   side. Upstream kept separate `inenergyContainer`
   (`getAbilities(INPUT_ENERGY)`) and `outenergyContainer`
   (`getAbilities(OUTPUT_ENERGY)`). The port now selects one input hatch as
   source and one output hatch as target.

6. **`MultiDanDeLifeOn` energy flush — code fixed.**
   `tickBuffer` used `findPart(EnergyHatchPartMachine.class)`, which can be
   the input hatch; the machine must flush its buffer into an output hatch
   (upstream used `OUTPUT_ENERGY`/`OUTPUT_LASER`). It now looks up an output
   energy hatch for both the flush and the display line.

7. **`ManaReceiverLookup` — code fixed.**
   Output hatches pushed mana into any neighbouring `IManaHatch`, including
   other output hatches (upstream only pushed into `!isExportHatch` /
   `!isExport` parts). Internal transfers now skip export hatches.

## 4. Remaining uncertainties / deviations

* **Wireless mana network**: `WirelessMana*HatchMachine` still behaves like
  the wired parts; the 1.12 `WirelessManager`/`WirelessWorldData` is not
  ported. Tooltips now say so.
* **`ManaMultiblockController` consumption pool**: the controller collects
  every `IManaHatch` part, so a pure-mana recipe may also drain energy-type
  mana hatches (upstream only consumed from `MANA_INPUT_POOL`). All current
  patterns only admit input variants, so this only matters for future
  patterns; not changed.
* **`ManaHandlerList.removeMana`** ignores `consumeMana` failures; amounts are
  pre-checked by `consumeMana(amount, simulate)`, so this is defensive only.
* **`ManaHandlerList.getTier`** returns the lowest hatch tier; the mana plate
  speed therefore follows the weakest installed pool hatch, matching upstream.
* **Mana pool output hatch as a mana recipe input**: the pool hatch's
  `IManaHatch.receiveMana(long)` path is still open (throttled only for
  Botania-capability callers), matching upstream's internal transfer
  behaviour; the `ManaReceiverLookup` guard only prevents output→output
  pushes.
* **Astral/blood hatches** remain unregistered (see
  `docs/HATCH_AUDIT.md` §3.1); machine tooltips that referenced them were not
  re-added. The **tarot hatch** is ported (2026-09-20): `TarotHatchMachine`
  holds one filtered card, `MagicMultiblockController` discovers it through
  `ITarotHatch` and the amplification engine reads it. Because upstream gates
  all amplification behind a calibrated astral wafer, the card's bonuses only
  take effect once the astral lens hatch lands; the recipe gates (`TAROT`
  property and the `EXPERIMENTAL` / `MAGIC_CONVERSION` / `HIDDEN_RITUAL` /
  `RECYCLING` / `THREE_MAGIC_SYSTEMS` process tags) are enforced now.
* **MegaManaTurbine (botania)** resolves its catalyst fluids by id at runtime;
  when the GTQT materials are absent the catalyst level stays 0, so its
  tooltip (`最大输出功率`) shows the UV base cap. This is the documented
  behaviour of the port.
