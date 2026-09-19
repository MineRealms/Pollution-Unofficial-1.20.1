# Machine overlays

Upstream (1.12 GTCE) every machine rendered its own texture: `getBaseTexture()`
(or `renderMetaTileEntity`) returned a `POTextures`/`Textures` renderer that was
drawn as the machine's cube texture, and `getFrontOverlay()` added the GT front
overlay on top. The port's generated models previously used the lava-boiler
front overlay (`gtceu:block/generators/boiler/lava/overlay_front`) everywhere.

This pass restores the upstream textures as the models' overlay faces:

* `tools/generate_machine_models.py` now owns `MACHINE_OVERLAYS` (model key ->
  overlay path) and writes it into `overlay_front`/`back`/`top`/`bottom`/`side`.
* The upstream textures (PNG + `.png.mcmeta` siblings) were copied to
  `src/main/resources/assets/pollution/textures/block/<same relative path>.png`
  (1.12 `textures/blocks/` -> 1.20 `textures/block/`).
* Only the overlay textures changed: the GT generator template
  (`gtceu:block/machine/template/generator_machine`) and the voltage casing
  base textures are untouched.

## POTextures constant -> texture path

All paths are relative to the `pollution` namespace (`pollution:block/<path>`);
upstream they lived in the `gregtech` namespace. Constants in the static
initializer and in `init()` are both listed.

| Constant | Renderer | Texture path |
| --- | --- | --- |
| `SOLAR_PLATE_I` | OrientedOverlayRenderer | `machines/solar_i` (`overlay_top`/`overlay_bottom`) |
| `SOLAR_PLATE_II` | OrientedOverlayRenderer | `machines/solar_ii` (`overlay_top`/`overlay_bottom`) |
| `SOLAR_PLATE_III` | OrientedOverlayRenderer | `machines/solar_iii` (`overlay_top`/`overlay_bottom`) |
| `AIR` | SimpleOverlayRenderer | `machines/solars/airside` |
| `DARK` | SimpleOverlayRenderer | `machines/solars/darkside` |
| `EARTH` | SimpleOverlayRenderer | `machines/solars/earthside` |
| `FIRE` | SimpleOverlayRenderer | `machines/solars/fireside` |
| `ORDER` | SimpleOverlayRenderer | `machines/solars/orderside` |
| `WATER` | SimpleOverlayRenderer | `machines/solars/waterside` |
| `FRAME_I` | SimpleOverlayRenderer | `fusion_reactor/frame_ii` |
| `FRAME_II` | SimpleOverlayRenderer | `fusion_reactor/frame_iii` |
| `FRAME_III` | SimpleOverlayRenderer | `fusion_reactor/frame_iv` |
| `FRAME_IV` | SimpleOverlayRenderer | `fusion_reactor/frame_v` |
| `SPELL_PRISM` | SimpleOverlayRenderer | `magicblock/spell_prism` |
| `SPELL_PRISM_COLD` | SimpleOverlayRenderer | `magicblock/spell_prism_cold` |
| `SPELL_PRISM_HOT` | SimpleOverlayRenderer | `magicblock/spell_prism_hot` |
| `SPELL_PRISM_AIR` | SimpleOverlayRenderer | `magicblock/spell_prism_air` |
| `SPELL_PRISM_VOID` | SimpleOverlayRenderer | `magicblock/spell_prism_void` |
| `SPELL_PRISM_WATER` | SimpleOverlayRenderer | `magicblock/spell_prism_water` |
| `SPELL_PRISM_ORDER` | SimpleOverlayRenderer | `magicblock/spell_prism_order` |
| `SPELL_PRISM_EARTH` | SimpleOverlayRenderer | `magicblock/spell_prism_earth` |
| `VOID_PRISM` | SimpleOverlayRenderer | `magicblock/void_prism` |
| `MAGIC_BATTERY` | SimpleOverlayRenderer | `magicblock/magic_battery` |
| `MANA_BASIC` | SimpleOverlayRenderer | `magicblock/mana_basic` |
| `MANA_1`..`MANA_5` | SimpleOverlayRenderer | `magicblock/mana_1`..`mana_5` |
| `TERRA_WATERTIGHT_CASING` | SimpleOverlayRenderer | `botblock/terra_watertight_casing` |
| `TERRA_1_CASING`..`TERRA_6_CASING` | SimpleOverlayRenderer | `botblock/terra_1_casing`..`terra_6_casing` |
| `Livingrock_0` | SimpleOverlayRenderer | `botblock/livingrock0` |
| `HYPER_1`..`HYPER_5` | SimpleOverlayRenderer | `hyper/hyper_1`..`hyper_5` |
| `ASTRAL_MARBLE` | SimpleOverlayRenderer | `astralsorcery:marble_bricks` (not ported) |
| `STARSTREAM_CASING` | SimpleOverlayRenderer | `pollution:starstream/starstream_casing` |
| `QUANTUM_ASPECT_TANK_OVERLAY` | SimpleOverlayRenderer | `overlay/machine/overlay_q_aspect_tank` |
| `PIPE_ASPECT_OUT_OVERLAY` | SimpleOverlayRenderer | `overlay/machine/overlay_pipe_aspect_out` |
| `ASPECT_OUTPUT_OVERLAY` | SimpleOverlayRenderer | `overlay/machine/overlay_aspect_out` |
| `MANA_HATCH_INPUT_1A`/`4A`/`16A`/`64A` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/mana_input_1a`/`4a`/`16a`/`64a` |
| `MANA_HATCH_OUTPUT_1A`/`4A`/`16A`/`64A` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/mana_output_1a`/`4a`/`16a`/`64a` |
| `WIRELESS_MANA_HATCH_INPUT`/`OUTPUT` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/wireless_mana_input`/`wireless_mana_output` |
| `MANA_POOL_HATCH_INPUT`/`OUTPUT` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/mana_pool_input`/`mana_pool_output` |
| `WIRELESS_MANA_POOL_HATCH_INPUT`/`OUTPUT` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/wireless_mana_pool_input`/`wireless_mana_pool_output` |
| `VIS_HATCH` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/vis_hatch` |
| `INFUSED_FLUID_HATCH` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/infused_fluid_hatch` |
| `BLOOD_MAGIC_HATCH` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/blood_magic_hatch` (not ported) |
| `ASTRAL_LENS_HATCH` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/astral_lens_hatch` (not ported) |
| `TAROT_HATCH` | SimpleOverlayRenderer | `overlay/machine/magic_hatch/tarot_hatch` |
| `BMCOMPUTER_CASING` | SidedCubeRenderer | `casings/bm_computer/computer_casing` (not ported) |
| `BMADVANCED_COMPUTER_CASING` | SidedCubeRenderer | `casings/bm_computer/advanced_computer_casing` (not ported) |
| `BMHPCA_*_OVERLAY` | SimpleOverlayRenderer | `overlay/machine/bm_hpca/*` (not ported) |
| `BMHPCA_OVERLAY` | OrientedOverlayRenderer | `multiblock/bm_hpca` (not ported) |

## Upstream machine -> renderer -> overlay

`ported model key` is the model generated by `tools/generate_machine_models.py`;
`-` means the machine is not part of the port yet. "GT standard" means the
upstream renderer was a GregTech `Textures.*` constant, i.e. not a Pollution
texture.

### Single-block machines

| Upstream class | Renderer | Texture path | Ported model key |
| --- | --- | --- | --- |
| `MetaTileEntityVisGenerator` | `Textures.HPCA_OVERLAY` (render call) | GT standard | `vis_generator_<tier>` (fallback) |
| `MetaTileEntityVisProvider` | `Textures.HPCA_BRIDGE_OVERLAY` (render call) | GT standard | `vis_provider_<tier>` (fallback) |
| `MetaTileEntitySmallNodeGenerator` | `Textures.MAGIC_ENERGY_ABSORBER(_ACTIVE)` | GT standard | `small_node_generator_<tier>` (fallback, unwired) |
| `MetaTileEntityMagicEnergyAbsorber` | `Textures.MAGIC_ENERGY_ABSORBER(_ACTIVE)` | GT standard | `magic_energy_absorber_<tier>` (fallback) |
| `MetaTileEntityFluxClear` (single) | `Textures.GAS_COLLECTOR_OVERLAY` | GT standard | `flux_scrubber_<tier>` (fallback) |
| `MetaTileEntityFluxPromotedFuelCell` | `Textures.POWER_SUBSTATION_OVERLAY` | GT standard | `flux_fuel_cell_<tier>` (fallback) |
| `ManaGeneratorTileEntity` | `Textures.COMBUSTION_GENERATOR_OVERLAY` | GT standard | `mana_generator_<tier>` (fallback) |
| `MetaTileEntitySourceCharge` | none (plain `MetaTileEntity`) | GT standard | `source_charge` (fallback) |
| `MetaTileEntitySolarPlate` | `SOLAR_PLATE_I/II/III` + `AIR`..`WATER` sides | `machines/solar_<i/ii/iii>` + `machines/solars/*side` | `solar_plate_<tier>_<kind>` |
| `MetaTileEntityAspectTank` | `QUANTUM_ASPECT_TANK_OVERLAY` | `overlay/machine/overlay_q_aspect_tank` | `aspect_tank_<tier>` (hand-written, already correct) |

### Multiblock parts

| Upstream class | Renderer | Texture path | Ported model key |
| --- | --- | --- | --- |
| `MetaTileEntityVisHatch` | `POTextures.VIS_HATCH` | `overlay/machine/magic_hatch/vis_hatch` | `vis_hatch_<tier>` |
| `MetaTileEntityInfusedFluidHatch` | `POTextures.INFUSED_FLUID_HATCH` | `overlay/machine/magic_hatch/infused_fluid_hatch` | `infused_fluid_hatch_<tier>` |
| `MetaTileEntityFluxMuffler` | `Textures.MUFFLER_OVERLAY` | GT standard | `flux_muffler_<tier>` (fallback) |
| `MetaTileEntityManaHatch` | `MANA_HATCH_INPUT_*A` / `MANA_HATCH_OUTPUT_*A` | `overlay/machine/magic_hatch/mana_(input|output)_<amp>a` | `mana_(input|output)_hatch_<amp>a_<tier>` |
| `MetaTileEntityWirelessManaHatch` | `WIRELESS_MANA_HATCH_INPUT/OUTPUT` | `overlay/machine/magic_hatch/wireless_mana_(input|output)` | `wireless_mana_(input|output)_hatch_<amp>a_<tier>` |
| `MetaTileEntityManaPoolHatch` | `MANA_POOL_HATCH_INPUT/OUTPUT` | `overlay/machine/magic_hatch/mana_pool_(input|output)` | `mana_pool_(input|output)_hatch_<pool>` |
| `MetaTileEntityWirelessManaPoolHatch` | `WIRELESS_MANA_POOL_HATCH_INPUT/OUTPUT` | `overlay/machine/magic_hatch/wireless_mana_pool_(input|output)` | `wireless_mana_pool_(input|output)_hatch_<pool>` |
| `MetaTileEntityAstralLensHatch` | `POTextures.ASTRAL_LENS_HATCH` | `overlay/machine/magic_hatch/astral_lens_hatch` | - |
| `MetaTileEntityBloodMagicHatch` | `POTextures.BLOOD_MAGIC_HATCH` | `overlay/machine/magic_hatch/blood_magic_hatch` | - |
| `MetaTileEntityTarotHatch` | `POTextures.TAROT_HATCH` | `overlay/machine/magic_hatch/tarot_hatch` | `tarot_hatch_lv` |
| `MetaTileEntityMagicItemHatch` | none (GT default) | GT standard | - |
| `MetaTileEntityBMHPCAComponent` and subclasses | `BMHPCA_*_OVERLAY`, `BMCOMPUTER/BMADVANCED_COMPUTER_CASING` | `overlay/machine/bm_hpca/*`, `casings/bm_computer/*` | - |
| `MetaTileEntityBMHPCA` | `BMHPCA_OVERLAY`, `BMCOMPUTER/BMADVANCED_COMPUTER_CASING` | `multiblock/bm_hpca`, `casings/bm_computer/*` | - |

### Multiblocks

| Upstream class | Renderer | Texture path | Ported model key |
| --- | --- | --- | --- |
| `MetaTileEntityMagicMacerator` | `SPELL_PRISM_EARTH` | `magicblock/spell_prism_earth` | `magic_macerator` |
| `MetaTileEntityMagicBender` | `SPELL_PRISM_ORDER` | `magicblock/spell_prism_order` | `magic_bender` |
| `MetaTileEntityMagicCentrifuge` | `SPELL_PRISM_AIR` | `magicblock/spell_prism_air` | `magic_centrifuge` |
| `MetaTileEntityMagicWireMill` | `SPELL_PRISM_WATER` | `magicblock/spell_prism_water` | `magic_wiremill` |
| `MetaTileEntityMagicAutoclave` | `SPELL_PRISM_AIR` | `magicblock/spell_prism_air` | `magic_autoclave` |
| `MetaTileEntityMagicElectrolyzer` | `SPELL_PRISM_ORDER` | `magicblock/spell_prism_order` | `magic_electrolyzer` |
| `MetaTileEntityMagicExtruder` | `SPELL_PRISM_ORDER` | `magicblock/spell_prism_order` | `magic_extruder` |
| `MetaTileEntityMagicMixer` | `SPELL_PRISM_VOID` | `magicblock/spell_prism_void` | `magic_mixer` |
| `MetaTileEntityMagicSifter` | `SPELL_PRISM_EARTH` | `magicblock/spell_prism_earth` | `magic_sifter` |
| `MetaTileEntityMagicSolidifier` | `SPELL_PRISM_ORDER` | `magicblock/spell_prism_order` | `magic_solidifier` |
| `MetaTileEntityMagicBrewery` | `SPELL_PRISM_COLD` | `magicblock/spell_prism_cold` | `magic_brewery` |
| `MetaTileEntityMagicCutter` | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `magic_cutter` |
| `MetaTileEntityMagicGreenHouse` | `SPELL_PRISM_WATER` | `magicblock/spell_prism_water` | `magic_green_house` |
| `MetaTileEntityMagicElectricBlastFurnace` | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `magic_electric_blast_furnace` |
| `MetaTileEntityMagicAlloyBlastSmelter` | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `magic_alloy_blast` |
| `MetaTileEntityMagicChemicalBath` | `SPELL_PRISM_WATER` | `magicblock/spell_prism_water` | `magic_chemical_bath` |
| `MetaTileEntityMagicChemicalReactor` | `SPELL_PRISM_WATER` | `magicblock/spell_prism_water` | `magic_chemical_reactor` |
| `MetaTileEntityMagicDistillery` | `SPELL_PRISM_COLD` | `magicblock/spell_prism_cold` | `magic_distillery` |
| `MetaTileEntityMagicAssembler` | `MANA_BASIC` | `magicblock/mana_basic` | `magic_assembler` |
| `MetaTileEntityInfusedExchange` | `VOID_PRISM` | `magicblock/void_prism` | `infused_exchange` |
| `MetaTileEntityEssenceSmelter` | `SPELL_PRISM_VOID` | `magicblock/spell_prism_void` | `essence_smelter` |
| `MetaTileEntityNodeProducer` | `FRAME_I` | `fusion_reactor/frame_ii` | `node_producer` |
| `MetaTileEntityLargeNodeGenerator` | `FRAME_I` | `fusion_reactor/frame_ii` | `large_node_generator` |
| `MetaTileEntityNodeWasher` | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `node_washer` |
| `MetaTileEntityNodeBlastFurnace` | `MANA_BASIC` | `magicblock/mana_basic` | `node_blast_furnace` |
| `MetaTileEntityNodeFusionReactor` (tier 6/7/8) | `HYPER_1`/`HYPER_2`/`HYPER_3` | `hyper/hyper_1`/`hyper_2`/`hyper_3` | `luv_node_fusion_reactor`/`zpm_node_fusion_reactor`/`uv_node_fusion_reactor` |
| `MetaTileEntityCentralVisTower` | `MANA_BASIC` | `magicblock/mana_basic` | `central_vis_tower` |
| `MetaTileEntityGtEssenceSmelter` | `SPELL_PRISM_VOID` | `magicblock/spell_prism_void` | `gt_essence_smelter` |
| `MetaTileEntityEssenceCollector` | `SPELL_PRISM` | `magicblock/spell_prism` | `essence_collector` |
| `MetaTileEntityIndustrialInfusion` | `SPELL_PRISM_VOID` | `magicblock/spell_prism_void` | `industrial_infusion` |
| `MetaTileEntitySmallChemicalPlant` | `TERRA_WATERTIGHT_CASING` | `botblock/terra_watertight_casing` | `small_chemical_plant` |
| `MetaTileEntityMagicFusionReactor` | `FRAME_I`..`FRAME_IV` by frame tier | `fusion_reactor/frame_ii`..`frame_v` | `magic_fusion_reactor` (`FRAME_I`/`frame_ii`, static default) |
| `MetaTileEntityMagicBattery` | `MAGIC_BATTERY` | `magicblock/magic_battery` | `magic_battery` |
| `MetaTileEntityMagicLargeTurbine` (`large_turbine.magic`) | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `magic_large_turbine` |
| `MetaTileEntityMagicMegaTurbine` (`mega_turbine.magic`) | `SPELL_PRISM_HOT` | `magicblock/spell_prism_hot` | `magic_mega_turbine` |
| `MetaTileEntityManaPlate` | `MANA_BASIC` | `magicblock/mana_basic` | `mana_plate` |
| `MetaTileEntityManaPetalApothecary` | `Livingrock_0` | `botblock/livingrock0` | `mana_petal_apothecary` |
| `MetaTileEntityManaRuneAltar` | `Livingrock_0` | `botblock/livingrock0` | `mana_rune_altar` |
| `MetaTileEntityIndustrialPureDaisy` | `Livingrock_0` | `botblock/livingrock0` | `industrial_pure_daisy` |
| `MetaTileEntityBotDistillery` | `TERRA_WATERTIGHT_CASING` | `botblock/terra_watertight_casing` | `bot_distillery` |
| `MetaTileEntityBotVacuumFreezer` | `MANA_4` | `magicblock/mana_4` | `bot_vacuum_freezer` |
| `MetaTileEntityBotCircuitAssembler` | `MANA_5` | `magicblock/mana_5` | `bot_circuit_assembler` |
| `MetaTileEntityBotGasCollector` | `TERRA_5_CASING` | `botblock/terra_5_casing` | `bot_gas_collector` |
| `MetaTileEntityEndoflameArray` | `TERRA_4_CASING` | `botblock/terra_4_casing` | `endoflame_array` |
| `MetaTileEntityManaInfusionReactor` | `Livingrock_0` | `botblock/livingrock0` | `mana_infusion_reactor` |
| `MetaTileEntityMegaManaTurbine` | `MANA_5` | `magicblock/mana_5` | `mega_mana_turbine` |
| `MetaTileEntityMultiDanDeLifeOn` | `MANA_4` | `magicblock/mana_4` | `pollution_multi_dan_de_life_on` |
| `MetaTileEntityFluxClear` (multiblock) | `GTQTTextures.ROCKET_ENGINE_OVERLAY` | GT standard | - |
| `MetaTileEntityCelestialCalibrationMatrix` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityCelestialCrystalGrowthArray` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityCelestialObservationArray` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityConstellationTower` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityIndustrialLightwell` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityIndustrialStarlightInfuser` | `ASTRAL_MARBLE` | `astralsorcery:marble_bricks` | - |
| `MetaTileEntityStarstreamNexusObelisk` | `STARSTREAM_CASING` | `starstream/starstream_casing` | - |

## Fallback machines

`gtceu:block/machines/overlay_front` does not exist in `gtceu-1.20.1-7.5.3`
(checked inside the jar), so machines whose upstream renderer is a GregTech
standard keep the previous lava-boiler placeholder
`gtceu:block/generators/boiler/lava/overlay_front` (all overlay and emissive
faces, unchanged from before this pass):

| Machine | Model keys | Upstream GT renderer |
| --- | --- | --- |
| Vis Generator | `vis_generator_lv`..`vis_generator_luv` | `Textures.HPCA_OVERLAY` |
| Vis Provider | `vis_provider_lv`..`vis_provider_uhv` | `Textures.HPCA_BRIDGE_OVERLAY` |
| Micro Starlight Node Reactor | `small_node_generator_luv`..`small_node_generator_uhv` (unwired) | `Textures.MAGIC_ENERGY_ABSORBER` |
| Magic Energy Absorber | `magic_energy_absorber_lv`..`magic_energy_absorber_iv` | `Textures.MAGIC_ENERGY_ABSORBER` |
| Flux Scrubber | `flux_scrubber_lv`..`flux_scrubber_iv` | `Textures.GAS_COLLECTOR_OVERLAY` |
| Flux Promoted Fuel Cell | `flux_fuel_cell_lv`..`flux_fuel_cell_iv` | `Textures.POWER_SUBSTATION_OVERLAY` |
| Mana Generator | `mana_generator_lv`..`mana_generator_iv` | `Textures.COMBUSTION_GENERATOR_OVERLAY` |
| Flux Muffler | `flux_muffler_lv`..`flux_muffler_uhv` | `Textures.MUFFLER_OVERLAY` |
| Source Charge | `source_charge` | none (plain `MetaTileEntity`) |

## Notes

* **Emissive layers.** The upstream `SimpleOverlayRenderer` textures are plain
  opaque casings with no emissive pass, so the generated `overlay_*_emissive`
  slots of restored machines are `gtceu:block/void` (GTCEu's fully transparent
  texture). Boiler-fallback models keep the boiler texture in the emissive
  slots exactly as before.
* **Solar plates.** Upstream combined an `OrientedOverlayRenderer`
  (`machines/solar_<i/ii/iii>`, only `overlay_top`/`overlay_bottom` exist) with
  the per-kind element overlay (`AIR`..`WATER` -> `machines/solars/*side`) on
  the four horizontal faces. The static port model maps accordingly:
  `overlay_top`/`overlay_bottom` = tier panel, `overlay_front`/`back`/`side` =
  kind side. The `_active`/`_active_emissive` variants are copied but unused
  (static models cannot switch on the active state).
* **Magic Fusion Reactor.** Upstream switched `FRAME_I`..`FRAME_IV` with the
  frame casing tier; the single static model uses the upstream default
  `FRAME_I` (`fusion_reactor/frame_ii`).
* **Aspect tank.** `aspect_tank_<tier>` models are hand-written and already use
  `pollution:block/overlay/machine/overlay_q_aspect_tank`, the correct upstream
  `QUANTUM_ASPECT_TANK_OVERLAY` path, so they were left untouched.
* **Copied textures.** 135 files (PNGs + 28 `.png.mcmeta`) were copied for the
  50 distinct referenced textures and their `_active`/`_emissive`/`_ctm`
  siblings. The upstream `.mcmeta` files are 1.12 CTM metadata (not animation);
  they are inert in 1.20.1 (unknown metadata sections are ignored).
* **Verification.** After `python tools/generate_machine_models.py`, all 289
  generated models were checked: every `pollution:block/...` texture they
  reference exists in `src/main/resources/assets/pollution/textures/block/`.
