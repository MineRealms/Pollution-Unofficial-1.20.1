# Texture Audit — Pollution Unofficial 1.20.1

Audit of every placeholder/fallback texture in the port against the authoritative
1.12 upstream repository (`H:\MinecraftMods\Pollution\src\main\resources\assets`).
No art was invented: a texture was only changed when an authoritative upstream
texture file exists.

## Summary

- Placeholder texture: `textures/item/heart_fruit.png` (md5 `e15dcd724a1c2702e9fe28a9281e4b37`),
  used as a byte-identical fallback for missing item art.
- Item textures scanned: **1462** PNGs under `textures/item/**`.
- Item textures replaced from upstream: **140**
  (139 were still the heart_fruit placeholder;
  1 was wrong fuzzy-matched content).
- Item textures already byte-identical to upstream: **828**.
- `.png.mcmeta` animation files added alongside: **160**.
- Block textures scanned: **303**, all already byte-identical to upstream (**0 replacements needed**);
  **66** missing `.png.mcmeta` animation files were restored from upstream.
- Block model texture references fixed: **3** (`eldritch_eye`, `heart_fruit`, `tentacle`).
- Items still on the heart_fruit placeholder with no upstream texture: **465**.
  (468 files are byte-identical to the placeholder in total; the other 3 —
  `heart_fruit.png`, `heart_fruit_i.png`, `items_heart_fruit.png` — correctly resolve to
  the upstream `heart_fruit` texture itself.)
- Non-placeholder files with no name-mappable upstream source (previous fuzzy matches): **29**.

## Method

1. **Authoritative item map.** Every JSON under upstream `gregtech/models/item/**` and
   `pollution/models/item/**` was parsed. The model path relative to `models/item`
   (e.g. `metaitems/packaged_aura_node`, `metaitems/battery.lv.magic/1`) is the
   upstream item name; every `layer0`/texture value was resolved to a file under
   `gregtech/textures/items/...` or `pollution/textures/items/...`
   (`gregtech:items/node/node` -> `gregtech/textures/items/node/node.png`).
2. **Full upstream texture index.** In addition, every file under both upstream
   `textures/items/**` trees was indexed by normalised path so textures that no
   model references (e.g. `metaitems/circuits.blood/13.png`,
   `metaitems/circuits.blood/freeze_cooler_blank.png`) are still reachable.
3. **Name matching.** Our file name (relative path under `textures/item`, without
   `.png`) is normalised (lowercase, `[./\-]` -> `_`). Lookups are tried in order:
   exact alias, roman-numeral variants (`filter.i` <-> `filter_1`,
   `battery.iv.magic` <-> `battery_4_magic`), then `metaitems_` / `items_` prefix
   variants, then (item files only) the block index as a fallback for block items.
   Alias priorities: upstream texture path (exact file) > item/block model name >
   blockstate variant name. Ambiguous variant keys such as `normal`/`inventory`
   are skipped.
4. **Copy.** When an authoritative file was found and differed, it was copied over
   our file; its `.png.mcmeta` (if any) was copied to `<ourname>.png.mcmeta`.
5. **Blocks.** Upstream `pollution/blockstates/*.json` and
   `pollution/models/block/*.json` were parsed recursively (both flat and
   `forge_marker` layouts) to build block-variant -> texture; all upstream
   `textures/blocks/**` trees were indexed too. Our `textures/block/**` files were
   compared byte-for-byte.
6. **Block model references.** Non-machine `models/block/*.json` files still
   pointing at vanilla placeholder textures were repointed to the authoritative
   pollution texture (textures only — model structure untouched).

Upstream roots used:

```
H:\MinecraftMods\Pollution\src\main\resources\assets\gregtech\textures\items\**
H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\textures\items\**
H:\MinecraftMods\Pollution\src\main\resources\assets\gregtech\textures\blocks\**
H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\textures\blocks\**
```

## Item replacements

Paths are relative to `assets/pollution/` (ours) and to upstream `assets/` (source).

| our texture | previous state | authoritative upstream source | match basis |
|---|---|---|---|
| `textures/item/aaminated_glass.png` | placeholder | `gregtech/textures/blocks/glass/aaminated_glass.png` | texture path |
| `textures/item/alfheim_dream_leaves.png` | placeholder | `pollution/textures/blocks/alfheim_dream_leaves.png` | texture path |
| `textures/item/alfheim_elven_sand.png` | placeholder | `pollution/textures/blocks/alfheim_elven_sand.png` | texture path |
| `textures/item/alloy_blast_casing.png` | placeholder | `gregtech/textures/blocks/magicblock/alloy_blast_casing.png` | texture path |
| `textures/item/baminated_glass.png` | placeholder | `gregtech/textures/blocks/glass/baminated_glass.png` | texture path |
| `textures/item/beam_core_0.png` | placeholder | `gregtech/textures/blocks/beamcore/beam_core_0.png` | texture path |
| `textures/item/beam_core_1.png` | placeholder | `gregtech/textures/blocks/beamcore/beam_core_1.png` | texture path |
| `textures/item/beam_core_2.png` | placeholder | `gregtech/textures/blocks/beamcore/beam_core_2.png` | texture path |
| `textures/item/beam_core_3.png` | placeholder | `gregtech/textures/blocks/beamcore/beam_core_3.png` | texture path |
| `textures/item/beam_core_4.png` | placeholder | `gregtech/textures/blocks/beamcore/beam_core_4.png` | texture path |
| `textures/item/blood_circuit.0.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/0.png` | item model |
| `textures/item/blood_circuit.1.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/1.png` | item model |
| `textures/item/blood_circuit.10.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/10.png` | item model |
| `textures/item/blood_circuit.11.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/11.png` | item model |
| `textures/item/blood_circuit.12.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/12.png` | item model |
| `textures/item/blood_circuit.2.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/2.png` | item model |
| `textures/item/blood_circuit.3.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/3.png` | item model |
| `textures/item/blood_circuit.4.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/4.png` | item model |
| `textures/item/blood_circuit.5.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/5.png` | item model |
| `textures/item/blood_circuit.6.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/6.png` | item model |
| `textures/item/blood_circuit.7.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/7.png` | item model |
| `textures/item/blood_circuit.8.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/8.png` | item model |
| `textures/item/blood_circuit.9.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/9.png` | item model |
| `textures/item/blood_circuit_0.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/0.png` | item model |
| `textures/item/blood_circuit_1.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/1.png` | item model |
| `textures/item/blood_circuit_10.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/10.png` | item model |
| `textures/item/blood_circuit_11.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/11.png` | item model |
| `textures/item/blood_circuit_12.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/12.png` | item model |
| `textures/item/blood_circuit_2.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/2.png` | item model |
| `textures/item/blood_circuit_3.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/3.png` | item model |
| `textures/item/blood_circuit_4.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/4.png` | item model |
| `textures/item/blood_circuit_5.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/5.png` | item model |
| `textures/item/blood_circuit_6.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/6.png` | item model |
| `textures/item/blood_circuit_7.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/7.png` | item model |
| `textures/item/blood_circuit_8.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/8.png` | item model |
| `textures/item/blood_circuit_9.png` | placeholder | `pollution/textures/items/metaitems/circuits.blood/9.png` | item model |
| `textures/item/bronze_gearbox.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_gearbox_bronze.png` | blockstate variant |
| `textures/item/bronze_pipe.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_pipe_bronze.png` | blockstate variant |
| `textures/item/caminated_glass.png` | placeholder | `gregtech/textures/blocks/glass/caminated_glass.png` | texture path |
| `textures/item/cogito_defibrillator.png` | placeholder | `pollution/textures/items/metaitems/magic.components/cogito_aed.png` | item model |
| `textures/item/constellation_anchor.png` | placeholder | `pollution/textures/blocks/starstream/constellation_anchor.png` | texture path |
| `textures/item/daminated_glass.png` | placeholder | `gregtech/textures/blocks/glass/daminated_glass.png` | texture path |
| `textures/item/flesh_flower.png` | placeholder | `pollution/textures/blocks/flesh_flower.png` | texture path |
| `textures/item/flesh_leaves.png` | placeholder | `pollution/textures/blocks/flesh_leaves.png` | texture path |
| `textures/item/flesh_plant.png` | placeholder | `pollution/textures/blocks/flesh_plant.png` | texture path |
| `textures/item/flesh_sapling.png` | placeholder | `pollution/textures/blocks/flesh_sapling.png` | texture path |
| `textures/item/infused_fluid_hatch.png` | placeholder | `gregtech/textures/blocks/overlay/machine/magic_hatch/infused_fluid_hatch.png` | texture path |
| `textures/item/laminated_glass.png` | placeholder | `gregtech/textures/blocks/glass/laminated_glass.png` | texture path |
| `textures/item/magic_battery.hull.ev.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.ev.png` | item model |
| `textures/item/magic_battery.hull.hv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.hv.png` | item model |
| `textures/item/magic_battery.hull.iv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.iv.png` | item model |
| `textures/item/magic_battery.hull.luv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.luv.png` | item model |
| `textures/item/magic_battery.hull.lv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.lv.png` | item model |
| `textures/item/magic_battery.hull.mv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.mv.png` | item model |
| `textures/item/magic_battery.hull.uv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.uv.png` | item model |
| `textures/item/magic_battery.hull.zpm.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.zpm.png` | item model |
| `textures/item/magic_battery.png` | placeholder | `gregtech/textures/blocks/magicblock/magic_battery.png` | texture path |
| `textures/item/magic_battery_casing.png` | placeholder | `gregtech/textures/blocks/magicblock/magic_battery.png` | known rename |
| `textures/item/magic_battery_hull_ev.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.ev.png` | item model |
| `textures/item/magic_battery_hull_hv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.hv.png` | item model |
| `textures/item/magic_battery_hull_iv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.iv.png` | item model |
| `textures/item/magic_battery_hull_luv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.luv.png` | item model |
| `textures/item/magic_battery_hull_lv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.lv.png` | item model |
| `textures/item/magic_battery_hull_mv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.mv.png` | item model |
| `textures/item/magic_battery_hull_uv.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.uv.png` | item model |
| `textures/item/magic_battery_hull_zpm.png` | placeholder | `pollution/textures/items/metaitems/battery.hull.zpm.png` | item model |
| `textures/item/magic_circuit.ev.png` | placeholder | `pollution/textures/items/metaitems/circuits/ev.png` | item model |
| `textures/item/magic_circuit.hv.png` | placeholder | `pollution/textures/items/metaitems/circuits/hv.png` | item model |
| `textures/item/magic_circuit.iv.png` | placeholder | `pollution/textures/items/metaitems/circuits/iv.png` | item model |
| `textures/item/magic_circuit.luv.png` | placeholder | `pollution/textures/items/metaitems/circuits/luv.png` | item model |
| `textures/item/magic_circuit.lv.png` | placeholder | `pollution/textures/items/metaitems/circuits/lv.png` | item model |
| `textures/item/magic_circuit.max.png` | placeholder | `pollution/textures/items/metaitems/circuits/max.png` | item model |
| `textures/item/magic_circuit.mv.png` | placeholder | `pollution/textures/items/metaitems/circuits/mv.png` | item model |
| `textures/item/magic_circuit.opv.png` | placeholder | `pollution/textures/items/metaitems/circuits/opv.png` | item model |
| `textures/item/magic_circuit.uev.png` | placeholder | `pollution/textures/items/metaitems/circuits/uev.png` | item model |
| `textures/item/magic_circuit.uhv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uhv.png` | item model |
| `textures/item/magic_circuit.uiv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uiv.png` | item model |
| `textures/item/magic_circuit.ulv.png` | placeholder | `pollution/textures/items/metaitems/circuits/ulv.png` | item model |
| `textures/item/magic_circuit.uv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uv.png` | item model |
| `textures/item/magic_circuit.uxv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uxv.png` | item model |
| `textures/item/magic_circuit.zpm.png` | placeholder | `pollution/textures/items/metaitems/circuits/zpm.png` | item model |
| `textures/item/magic_circuit_ev.png` | placeholder | `pollution/textures/items/metaitems/circuits/ev.png` | item model |
| `textures/item/magic_circuit_hv.png` | placeholder | `pollution/textures/items/metaitems/circuits/hv.png` | item model |
| `textures/item/magic_circuit_iv.png` | placeholder | `pollution/textures/items/metaitems/circuits/iv.png` | item model |
| `textures/item/magic_circuit_luv.png` | placeholder | `pollution/textures/items/metaitems/circuits/luv.png` | item model |
| `textures/item/magic_circuit_lv.png` | placeholder | `pollution/textures/items/metaitems/circuits/lv.png` | item model |
| `textures/item/magic_circuit_max.png` | placeholder | `pollution/textures/items/metaitems/circuits/max.png` | item model |
| `textures/item/magic_circuit_mv.png` | placeholder | `pollution/textures/items/metaitems/circuits/mv.png` | item model |
| `textures/item/magic_circuit_opv.png` | placeholder | `pollution/textures/items/metaitems/circuits/opv.png` | item model |
| `textures/item/magic_circuit_uev.png` | placeholder | `pollution/textures/items/metaitems/circuits/uev.png` | item model |
| `textures/item/magic_circuit_uhv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uhv.png` | item model |
| `textures/item/magic_circuit_uiv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uiv.png` | item model |
| `textures/item/magic_circuit_ulv.png` | placeholder | `pollution/textures/items/metaitems/circuits/ulv.png` | item model |
| `textures/item/magic_circuit_uv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uv.png` | item model |
| `textures/item/magic_circuit_uxv.png` | placeholder | `pollution/textures/items/metaitems/circuits/uxv.png` | item model |
| `textures/item/magic_circuit_zpm.png` | placeholder | `pollution/textures/items/metaitems/circuits/zpm.png` | item model |
| `textures/item/mana_1.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_1.png` | texture path |
| `textures/item/mana_2.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_2.png` | texture path |
| `textures/item/mana_3.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_3.png` | texture path |
| `textures/item/mana_4.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_4.png` | texture path |
| `textures/item/mana_5.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_5.png` | texture path |
| `textures/item/mana_basic.png` | placeholder | `gregtech/textures/blocks/magicblock/mana_basic.png` | texture path |
| `textures/item/nano_goggles.png` | placeholder | `gregtech/textures/items/armor/goggles_nano.png` | item model |
| `textures/item/packaged_aura_node.png` | placeholder | `gregtech/textures/items/node/node.png` | item model |
| `textures/item/polytetrafluoroethylene_pipe.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_pipe_polytetrafluoroethylene.png` | blockstate variant |
| `textures/item/quantum_goggles.png` | placeholder | `gregtech/textures/items/armor/goggles_quantum.png` | item model |
| `textures/item/spell_prism.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism.png` | texture path |
| `textures/item/spell_prism_air.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_air.png` | texture path |
| `textures/item/spell_prism_cold.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_cold.png` | texture path |
| `textures/item/spell_prism_earth.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_earth.png` | texture path |
| `textures/item/spell_prism_hot.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_hot.png` | texture path |
| `textures/item/spell_prism_order.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_order.png` | texture path |
| `textures/item/spell_prism_void.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_void.png` | texture path |
| `textures/item/spell_prism_water.png` | placeholder | `gregtech/textures/blocks/magicblock/spell_prism_water.png` | texture path |
| `textures/item/stainless_steel_gearbox.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_gearbox_stainless_steel.png` | blockstate variant |
| `textures/item/starstream_casing.png` | placeholder | `pollution/textures/blocks/starstream/starstream_casing.png` | texture path |
| `textures/item/starstream_runed_casing.png` | placeholder | `pollution/textures/blocks/starstream/starstream_runed_casing.png` | texture path |
| `textures/item/steel_gearbox.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_gearbox_steel.png` | blockstate variant |
| `textures/item/steel_pipe.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_pipe_steel.png` | blockstate variant |
| `textures/item/stone_of_philosopher_1.png` | placeholder | `pollution/textures/items/metaitems/stone_1.png` | item model |
| `textures/item/stone_of_philosopher_2.png` | placeholder | `pollution/textures/items/metaitems/stone_2.png` | item model |
| `textures/item/stone_of_philosopher_3.png` | placeholder | `pollution/textures/items/metaitems/stone_3.png` | item model |
| `textures/item/stone_of_philosopher_4.png` | placeholder | `pollution/textures/items/metaitems/stone_4.png` | item model |
| `textures/item/stone_of_philosopher_final.png` | placeholder | `pollution/textures/items/metaitems/stone_final.png` | item model |
| `textures/item/terra_1_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_1_casing.png` | texture path |
| `textures/item/terra_2_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_2_casing.png` | texture path |
| `textures/item/terra_3_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_3_casing.png` | texture path |
| `textures/item/terra_4_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_4_casing.png` | texture path |
| `textures/item/terra_5_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_5_casing.png` | texture path |
| `textures/item/terra_6_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_6_casing.png` | texture path |
| `textures/item/terra_watertight_casing.png` | placeholder | `gregtech/textures/blocks/botblock/terra_watertight_casing.png` | texture path |
| `textures/item/test.png` | wrong fuzzy content | `pollution/textures/items/metaitems/test.png` | texture path |
| `textures/item/titanium_gearbox.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_gearbox_titanium.png` | blockstate variant |
| `textures/item/titanium_pipe.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_pipe_titanium.png` | blockstate variant |
| `textures/item/tungstensteel_gearbox.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_gearbox_tungstensteel.png` | blockstate variant |
| `textures/item/tungstensteel_pipe.png` | placeholder | `gregtech/textures/blocks/turbine/machine_casing_pipe_tungstensteel.png` | blockstate variant |
| `textures/item/vis_hatch.png` | placeholder | `gregtech/textures/blocks/overlay/machine/magic_hatch/vis_hatch.png` | texture path |
| `textures/item/void_prism.png` | placeholder | `gregtech/textures/blocks/magicblock/void_prism.png` | texture path |
| `textures/item/wing_nano.png` | placeholder | `gregtech/textures/items/armor/wing_nano.png` | texture path |
| `textures/item/wing_quantum.png` | placeholder | `gregtech/textures/items/armor/wing_quantum.png` | texture path |

### Notable fixes

- `packaged_aura_node.png` — was the heart_fruit placeholder; now
  `gregtech/textures/items/node/node.png` (referenced by upstream
  `gregtech/models/item/metaitems/packaged_aura_node.json`).
- `nano_goggles.png` / `quantum_goggles.png` — now `armor/goggles_nano` /
  `armor/goggles_quantum` (previous manual table mapped `nano_goggles` to a
  non-existent `goggles`).
- `wing_nano.png` / `wing_quantum.png` — now `armor/wing_nano` / `armor/wing_quantum`.
- `cogito_defibrillator.png` — now `metaitems/magic.components/cogito_aed.png`.
- `stone_of_philosopher_1..4/final.png` — now `metaitems/stone_1..4/final.png`.
- `test.png` — was wrong content (`test_block` art); now `metaitems/test.png`.
- `magic_circuit.*` / `magic_circuit_*` (all 30 files) — now
  `metaitems/circuits/<tier>.png` (the `_4` files are the roman-numeral form of
  tier `iv`).
- `magic_battery_hull_*` / `magic_battery.hull.*` — now `metaitems/battery.hull.*.png`.
- `blood_circuit.*` / `blood_circuit_*` — now `metaitems/circuits.blood/*.png`.
- Block-item textures (`spell_prism*`, `mana_1..5`, `mana_basic`, `terra_*_casing`,
  `beam_core_*`, `*_glass`, `magic_battery`, `magic_battery_casing`,
  `alloy_blast_casing`, `void_prism`, `starstream_*`, `constellation_anchor`) — now
  the upstream block textures they render with.
- Gearbox/pipe block items (`bronze_gearbox`, `steel_gearbox`, `titanium_gearbox`,
  `tungstensteel_gearbox`, `stainless_steel_gearbox`, `*_pipe`) — now the upstream
  `turbine/machine_casing_*` textures via the `turbine` blockstate variants.

All other item textures (828) were already byte-identical to an authoritative upstream file
(including the whole `textures/item/metaitems/**` tree, which mirrors upstream 1:1).

## No upstream texture found

These 465 files are still byte-identical to the `heart_fruit.png` placeholder after
the replacements above. Upstream has no texture for them in the authoritative
repository, so no art was created.

### Botania functional flora (55)

- `agricarnation.png`
- `agricarnation_chibi.png`
- `bellethorn.png`
- `bellethorn_chibi.png`
- `bergamute.png`
- `bubbell.png`
- `bubbell_chibi.png`
- `clayconia.png`
- `clayconia_chibi.png`
- `daffomill.png`
- `dandelifeon.png`
- `daybloom_motif.png`
- `diluted.png`
- `dreadthorn.png`
- `endoflame.png`
- `endoflame_array.png`
- `entropinnyum.png`
- `exoflame.png`
- `fallen_kanade.png`
- `gourmaryllis.png`
- `heisei_dream.png`
- `hopperhock.png`
- `hopperhock_chibi.png`
- `hyacidus.png`
- `hydroangeas.png`
- `hydroangeas_motif.png`
- `jaded_amaranthus.png`
- `jiyuulia.png`
- `jiyuulia_chibi.png`
- `kekimurus.png`
- `labellia.png`
- `loonium.png`
- `marimorphosis.png`
- `marimorphosis_chibi.png`
- `medumone.png`
- `munchdew.png`
- `narslimmus.png`
- `nightshade_motif.png`
- `orechid.png`
- `orechid_ignem.png`
- `pollidisiac.png`
- `pure_daisy.png`
- `rafflowsia.png`
- `rannuncarpus.png`
- `rannuncarpus_chibi.png`
- `rosa_arcana.png`
- `shulk_me_not.png`
- `solegnolia.png`
- `solegnolia_chibi.png`
- `spectranthemum.png`
- `spectrolus.png`
- `tangleberrie.png`
- `tangleberrie_chibi.png`
- `thermalily.png`
- `vinculotus.png`

### Botania material / GUI icons (34)

- `kq_gold.png`
- `livingrock.png`
- `livingwood.png`
- `mana_bottle.png`
- `mana_cookie.png`
- `mana_diamond.png`
- `mana_diamond_block.png`
- `mana_generator.png`
- `mana_glass.png`
- `mana_infusion.png`
- `mana_infusion_reactor.png`
- `mana_input_hatch_16a.png`
- `mana_input_hatch_1a.png`
- `mana_input_hatch_4a.png`
- `mana_input_hatch_64a.png`
- `mana_output_hatch_16a.png`
- `mana_output_hatch_1a.png`
- `mana_output_hatch_4a.png`
- `mana_output_hatch_64a.png`
- `mana_pearl.png`
- `mana_petal_apothecary.png`
- `mana_plate.png`
- `mana_powder_dust.png`
- `mana_powder_dye.png`
- `mana_quartz.png`
- `mana_rune_altar.png`
- `mana_string.png`
- `manastar.png`
- `manasteel.png`
- `manasteel_block.png`
- `petal_apothecary.png`
- `runic_altar.png`
- `stove.png`
- `tigerseye.png`

### Botania recipe / conversion icons (112)

- `acacia_leaves_dupe.png`
- `acacia_log_to_dark_oak_log.png`
- `acacia_sapling_to_dark_oak_sapling.png`
- `allium_to_azure_bluet.png`
- `andesite_to_diorite.png`
- `apple_to_sweet_berries.png`
- `azalea_leaves_dupe.png`
- `azure_bluet_to_red_tulip.png`
- `beetroot_seeds_to_melon_seeds.png`
- `birch_leaves_dupe.png`
- `birch_log_to_jungle_log.png`
- `birch_sapling_to_jungle_sapling.png`
- `blaze_quartz_deconstruct.png`
- `blaze_rod_to_nether_wart.png`
- `blue_orchid_to_allium.png`
- `book_to_name_tag.png`
- `brick_deconstruct.png`
- `cactus_to_slime.png`
- `calcite_to_deepslate.png`
- `carrot_to_beetroot_seeds.png`
- `cherry_leaves_dupe.png`
- `cherry_log_to_oak_log.png`
- `cherry_sapling_to_oak_sapling.png`
- `chorus_fruit_to_flower.png`
- `clay_deconstruct.png`
- `coal_dupe.png`
- `cobble_to_sand.png`
- `cocoa_beans_to_wheat_seeds.png`
- `cod_to_salmon.png`
- `cornflower_to_lily_of_the_valley.png`
- `dandelion_to_poppy.png`
- `dark_oak_leaves_dupe.png`
- `dark_oak_log_to_mangrove_log.png`
- `dark_oak_sapling_to_mangrove_propagule.png`
- `dark_quartz_deconstruct.png`
- `dead_bush_to_grass.png`
- `deepslate_to_tuff.png`
- `diorite_to_granite.png`
- `elf_quartz_deconstruct.png`
- `end_stone_to_cobbled_deepslate.png`
- `fern_to_dead_bush.png`
- `flint_to_gunpowder.png`
- `flowering_azalea_leaves_dupe.png`
- `glow_berries_to_apple.png`
- `glowstone_deconstruct.png`
- `glowstone_dupe.png`
- `glowstone_dust_to_redstone.png`
- `granite_to_andesite.png`
- `grass_to_fern.png`
- `gravel_dupe.png`
- `gunpowder_to_flint.png`
- `industrial_infusion_recipes.png`
- `jungle_leaves_dupe.png`
- `jungle_log_to_acacia_log.png`
- `jungle_sapling_to_acacia_sapling.png`
- `lavender_quartz_deconstruct.png`
- `lilac_to_rose_bush.png`
- `lily_of_the_valley_to_sunflower.png`
- `lily_pad_to_vine.png`
- `mana_gen_recipes.png`
- `mana_infusion_recipes.png`
- `mana_petal_recipes.png`
- `mana_quartz_deconstruct.png`
- `mana_rune_altar_recipes.png`
- `mana_to_eu.png`
- `mangrove_leaves_dupe.png`
- `mangrove_log_to_cherry_log.png`
- `mangrove_propagule_to_cherry_sapling.png`
- `melon_seeds_to_pumpkin_seeds.png`
- `netherrack_dupe.png`
- `oak_leaves_dupe.png`
- `oak_log_to_spruce_log.png`
- `oak_sapling_to_spruce_sapling.png`
- `ochre_froglight_to_verdant_froglight.png`
- `orange_tulip_to_white_tulip.png`
- `oxeye_daisy_to_cornflower.png`
- `pearlescent_froglight_to_ochre_froglight.png`
- `peony_to_dandelion.png`
- `pink_tulip_to_oxeye_daisy.png`
- `poppy_to_blue_orchid.png`
- `potato_to_carrot.png`
- `potato_unpoison.png`
- `pufferfish_to_cod.png`
- `pumpkin_seeds_to_cocoa_beans.png`
- `pure_daisy_recipes.png`
- `quartz_deconstruct.png`
- `quartz_dupe.png`
- `red_quartz_deconstruct.png`
- `red_tulip_to_orange_tulip.png`
- `redstone_dupe.png`
- `redstone_to_glowstone_dust.png`
- `rose_bush_to_peony.png`
- `rotten_flesh_to_leather.png`
- `salmon_to_tropical_fish.png`
- `slime_to_cactus.png`
- `snowball_dupe.png`
- `soul_sand_dupe.png`
- `spruce_leaves_dupe.png`
- `spruce_log_to_birch_log.png`
- `spruce_sapling_to_birch_sapling.png`
- `stone_to_andesite.png`
- `sunflower_to_lilac.png`
- `sunny_quartz_deconstruct.png`
- `sweet_berries_to_glow_berries.png`
- `terracotta_to_red_sand.png`
- `tropical_fish_to_pufferfish.png`
- `tuff_to_calcite.png`
- `verdant_froglight_to_pearlescent_froglight.png`
- `vine_to_lily_pad.png`
- `wheat_seeds_to_potato.png`
- `white_tulip_to_pink_tulip.png`
- `wool_deconstruct.png`

### GUI / tooltip icons (`pollution.*`, `gtceu.*`) (83)

- `gtceu.universal.tooltip.amperage_in_till.png`
- `gtceu.universal.tooltip.amperage_out_till.png`
- `gtceu.universal.tooltip.energy_storage_capacity.png`
- `gtceu.universal.tooltip.fluid_storage_capacity.png`
- `gtceu.universal.tooltip.voltage_in.png`
- `gtceu.universal.tooltip.voltage_out.png`
- `pollution.armor.goggles.food.png`
- `pollution.armor.goggles.solar.png`
- `pollution.armor.goggles.vis_discount.png`
- `pollution.armor.goggles.water.png`
- `pollution.astral_data.constellation.png`
- `pollution.astral_data.function.energy.png`
- `pollution.astral_data.function.life.png`
- `pollution.astral_data.function.png`
- `pollution.astral_data.function.processing.png`
- `pollution.astral_data.function.resonance.png`
- `pollution.astral_data.function.stability.png`
- `pollution.astral_data.function.time.png`
- `pollution.astral_data.nbt_preserved.png`
- `pollution.astral_data.unattuned.png`
- `pollution.bauble.material.png`
- `pollution.bauble.source.png`
- `pollution.command.get.png`
- `pollution.command.scrub.png`
- `pollution.command.set.png`
- `pollution.crystal_quality.cultivated.png`
- `pollution.crystal_quality.embryo.png`
- `pollution.crystal_quality.grade.png`
- `pollution.crystal_quality.purity.png`
- `pollution.crystal_quality.stability.png`
- `pollution.crystal_quality.unselected.png`
- `pollution.effect.warning.png`
- `pollution.filter.durability.png`
- `pollution.filter.expected.png`
- `pollution.filter.material.png`
- `pollution.filter.remaining.png`
- `pollution.filter.tier.png`
- `pollution.flesh_heart.bound.png`
- `pollution.flesh_heart.level.png`
- `pollution.flesh_heart.next_growth.png`
- `pollution.flesh_heart.other.png`
- `pollution.item.packaged_aura_node.header.png`
- `pollution.item.vis_checker.result.png`
- `pollution.machine.endoflame_array.display.flowers.png`
- `pollution.machine.endoflame_array.display.fuel_cache.png`
- `pollution.machine.endoflame_array.display.fuel_items.png`
- `pollution.machine.endoflame_array.display.mana_pool.png`
- `pollution.machine.endoflame_array.display.output.png`
- `pollution.machine.flux_fuel_cell.tooltip.png`
- `pollution.machine.flux_muffler.tooltip.png`
- `pollution.machine.flux_muffler.tooltip.recovery.png`
- `pollution.machine.flux_scrubber.tooltip.png`
- `pollution.machine.infused_fluid_hatch.tooltip.png`
- `pollution.machine.magic_energy_absorber.tooltip.png`
- `pollution.machine.mana_plate.speed.png`
- `pollution.machine.mana_plate.tier.png`
- `pollution.machine.mana_pool_hatch.capacity.png`
- `pollution.machine.mana_pool_hatch.transfer.png`
- `pollution.machine.mana_pool_hatch.type.png`
- `pollution.machine.mega_mana_turbine.catalyst.png`
- `pollution.machine.mega_mana_turbine.max_output.png`
- `pollution.machine.mega_mana_turbine.parallel.png`
- `pollution.machine.pollution_multi_dan_de_life_on.buffer.png`
- `pollution.machine.pollution_multi_dan_de_life_on.energy.png`
- `pollution.machine.pollution_multi_dan_de_life_on.mode.png`
- `pollution.machine.solar_plate.tooltip.png`
- `pollution.machine.vis_generator.tooltip.png`
- `pollution.machine.vis_hatch.tooltip.buffer.png`
- `pollution.machine.vis_hatch.tooltip.capacity.png`
- `pollution.machine.vis_hatch.tooltip.drain.png`
- `pollution.machine.vis_provider.tooltip.png`
- `pollution.magic.failure.coil.png`
- `pollution.magic.failure.hatches.png`
- `pollution.magic.failure.infused_fluid.png`
- `pollution.magic.failure.life_essence.png`
- `pollution.magic.failure.mana.png`
- `pollution.magic.failure.research.png`
- `pollution.magic.failure.temperature.png`
- `pollution.magic.failure.vis.png`
- `pollution.mineral_extractor.disabled.png`
- `pollution.mineral_extractor.enabled.png`
- `pollution.mineral_extractor.mode.png`
- `pollution.tarot.the_fool.tooltip.png`

### Other / uncategorised (101)

- `_________.png`
- `advanced_substrate.png`
- `advanced_substrate_syrmorite.png`
- `air.png`
- `alfheim_red_grape_0.png`
- `alfheim_red_grape_1.png`
- `alfheim_red_grape_2.png`
- `alfheim_white_grape.png`
- `amount.png`
- `autumn.png`
- `basalz_bolt.png`
- `basic_substrate.png`
- `basic_substrate_salis.png`
- `binding_metal.png`
- `blind.png`
- `blink.png`
- `blitz_bolt.png`
- `blizz_bolt.png`
- `chance.png`
- `channel.png`
- `dan_de_life_on.png`
- `dripleaf_shrinking.png`
- `duration.png`
- `earth.png`
- `ebf_temp.png`
- `efficiency.png`
- `enabled.png`
- `ender_pearl_from_ghast_tear.png`
- `envy.png`
- `eut.png`
- `existing_nexus.png`
- `fading_nexus.png`
- `fake_explosion.png`
- `fall.png`
- `fire.png`
- `forge_alchemy.png`
- `gluttony.png`
- `greed.png`
- `hyper_substrate.png`
- `hyper_substrate_syrmorite.png`
- `hyperdimensional_silver.png`
- `infused_air.png`
- `infused_earth.png`
- `infused_entropy.png`
- `infused_exchange.png`
- `infused_fire.png`
- `infused_order.png`
- `infused_water.png`
- `jump.png`
- `junk.png`
- `lightning.png`
- `lust.png`
- `luv_node_fusion_reactor.png`
- `main.png`
- `mega_mana_turbine.png`
- `mod.pollution.name.png`
- `mode.png`
- `mushrooms.png`
- `mythic.png`
- `nausea.png`
- `neutronium.png`
- `normal.png`
- `output.png`
- `pesticide.empty.png` — upstream model references missing file `gregtech:items/metaitems/spray.empty`
- `pesticide.full.png` — upstream model references missing file `gregtech:items/metaitems/spray.can.dyes.white`
- `pesticide_empty.png` — upstream model references missing file `gregtech:items/metaitems/spray.empty`
- `pesticide_full.png` — upstream model references missing file `gregtech:items/metaitems/spray.can.dyes.white`
- `poison.png`
- `pollution.png`
- `pollution_multi_dan_de_life_on.png`
- `pride.png`
- `quanta.png`
- `rainbow_leaves.png`
- `rainbow_sapling.png`
- `retention.png`
- `sentient_metal.png`
- `sloth.png`
- `small_chemical_plant.png`
- `spring.png`
- `summer.png`
- `swamp.png`
- `tc_slime_aer.png`
- `tc_slime_aqua.png`
- `tc_slime_ignis.png`
- `tc_slime_ordo.png`
- `tc_slime_perditio.png`
- `tc_slime_terra.png`
- `temperature.png`
- `thickness.png`
- `tiny_potato.png`
- `tritanium.png`
- `underground.png`
- `underground_bridge.png`
- `uv_node_fusion_reactor.png`
- `weakness.png`
- `wind.png`
- `winter.png`
- `wither.png`
- `wrath.png`
- `zombie_siege.png`
- `zpm_node_fusion_reactor.png`

### Port machine / part item icons (63)

- `bot_circuit_assembler.png`
- `bot_distillery.png`
- `bot_gas_collector.png`
- `bot_vacuum_freezer.png`
- `celestial_calibration_matrix.png`
- `celestial_observation_array.png`
- `central_vis_tower.png`
- `countdown_bomb.png`
- `dimensional_transforming_agent.png`
- `essence_collector.png`
- `essence_smelter.png`
- `flux_fuel_cell.png`
- `flux_muffler.png`
- `flux_scrubber.png`
- `gt_essence_smelter.png`
- `industrial_infusion.png`
- `industrial_lightwell.png`
- `industrial_pure_daisy.png`
- `industrial_starlight_infuser.png`
- `inventory_scramble.png`
- `large_node_generator.png`
- `machine_info.png`
- `magic_alloy_blast.png`
- `magic_assembler.png`
- `magic_autoclave.png`
- `magic_bender.png`
- `magic_blast_smelter.png`
- `magic_brewery.png`
- `magic_centrifuge.png`
- `magic_chemical_bath.png`
- `magic_chemical_reactor.png`
- `magic_cutter.png`
- `magic_distillery.png`
- `magic_electric_blast_furnace.png`
- `magic_electrolyzer.png`
- `magic_energy_absorber.png`
- `magic_extruder.png`
- `magic_fusion_reactor.png`
- `magic_green_house.png`
- `magic_greenhouse.png`
- `magic_large_turbine.png`
- `magic_macerator.png`
- `magic_mega_turbine.png`
- `magic_mixer.png`
- `magic_sifter.png`
- `magic_solidifier.png`
- `magic_turbine.png`
- `magic_wiremill.png`
- `node_blast_furnace.png`
- `node_magic_fusion.png`
- `node_producer.png`
- `node_washer.png`
- `piston_relay.png`
- `vis_generator.png`
- `vis_provider.png`
- `wireless_mana_input_hatch_16a.png`
- `wireless_mana_input_hatch_1a.png`
- `wireless_mana_input_hatch_4a.png`
- `wireless_mana_input_hatch_64a.png`
- `wireless_mana_output_hatch_16a.png`
- `wireless_mana_output_hatch_1a.png`
- `wireless_mana_output_hatch_4a.png`
- `wireless_mana_output_hatch_64a.png`

### Vanilla world block items (17)

- `blue_ice.png`
- `brown_mushroom.png`
- `chiseled_stone_bricks.png`
- `coarse_dirt.png`
- `cobblestone.png`
- `dandelion.png`
- `grass.png`
- `grass_seeds.png`
- `mycel_seeds.png`
- `obsidian.png`
- `packed_ice.png`
- `podzol_seeds.png`
- `poppy.png`
- `red_mushroom.png`
- `sand.png`
- `soul_soil.png`
- `wither_rose.png`

## Wrong fuzzy matches without an authoritative source

These files are not placeholders and do not match any upstream name; their current
content equals some unrelated upstream texture, i.e. they are leftovers of the old
token-subset fuzzy matcher. Upstream has no texture for the intended icon, so they
were left untouched (not reverted, not redrawn).

| our texture | content currently equals |
|---|---|
| `textures/item/alfheim.png` | `pollution/textures/blocks/alfheim_dream_leaves.png` |
| `textures/item/basalz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/battery.ev.magic.png` | `pollution/textures/items/metaitems/battery.ev.magic/1.png` |
| `textures/item/battery.hv.magic.png` | `pollution/textures/items/metaitems/battery.hv.magic/1.png` |
| `textures/item/battery.iv.magic.png` | `pollution/textures/items/metaitems/battery.iv.magic/1.png` |
| `textures/item/battery.luv.magic.png` | `pollution/textures/items/metaitems/battery.luv.magic/1.png` |
| `textures/item/battery.lv.magic.png` | `pollution/textures/items/metaitems/battery.lv.magic/1.png` |
| `textures/item/battery.mv.magic.png` | `pollution/textures/items/metaitems/battery.mv.magic/1.png` |
| `textures/item/battery.uv.magic.png` | `pollution/textures/items/metaitems/battery.uv.magic/1.png` |
| `textures/item/battery.zpm.magic.png` | `pollution/textures/items/metaitems/battery.zpm.magic/1.png` |
| `textures/item/blitz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/blizz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/blood.png` | `pollution/textures/items/metaitems/magic.integration/astral_blood_catalyst.png` |
| `textures/item/catalyst.png` | `pollution/textures/items/metaitems/magic.integration/astral_blood_catalyst.png` |
| `textures/item/constellation.png` | `pollution/textures/items/metaitems/magic.integration/constellation_data_wafer.png` |
| `textures/item/eldritch_eye.png` | `pollution/textures/blocks/eldritch_eye_closed.png` |
| `textures/item/entity.pollution.basalz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/entity.pollution.blitz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/entity.pollution.blizz.png` | _(not present upstream — legacy/entity art)_ |
| `textures/item/ice.png` | `pollution/textures/items/metaitems/magic.components/auto_elenchus_device.png` |
| `textures/item/magic.png` | `pollution/textures/items/metaitems/magic.integration/depleted_magic_core.png` |
| `textures/item/mana.png` | `pollution/textures/items/metaitems/magic.integration/mana_resonance_coil.png` |
| `textures/item/parallel.png` | `pollution/textures/items/metaitems/parallel_enhance.png` |
| `textures/item/portal.png` | `pollution/textures/blocks/portal_barrier.png` |
| `textures/item/rain.png` | `pollution/textures/items/metaitems/circuits.blood/ips_human_brain.png` |
| `textures/item/strength.png` | `pollution/textures/items/metaitems/tarots/the_strength.png` |
| `textures/item/tarot.png` | `pollution/textures/items/metaitems/magic.integration/blank_tarot_card.png` |
| `textures/item/tentacle.png` | `pollution/textures/blocks/tentacle_mid.png` |
| `textures/item/water.png` | `pollution/textures/items/metaitems/baubles/water_ring.png` |

Note: `battery.ev.magic.png` … `battery.zpm.magic.png` already contain the correct
upstream variant-1 art (`metaitems/battery.<tier>.magic/1.png`); the name simply has
no model alias because upstream stores those textures in a variant directory.

## Block audit

- Our `textures/block/**` contains **303** PNGs;
  every one is byte-identical to an upstream file (0 replacements needed).
  This includes the flat `blocks_*` / `starstream_*` duplicates as well as the
  sub-directory copies (`magicblock/`, `beamcore/`, `botblock/`, `turbine/`,
  `wirecoil/`, `glass/`, `fusion_reactor/`, `hyper/`, `overlays/`, `casings/`,
  `machines/`, `multiblock/`, `starstream/`).
- No block texture is a copy of the heart_fruit placeholder, and none is a generic
  GT voltage casing used as a fallback: every casing texture matches its upstream
  counterpart by relative path.
- 66 upstream `.png.mcmeta` animation descriptors were missing in our
  tree and were copied (beam cores, filters, terra casings, magic voltage casings,
  wire coils, overlays, …). No PNG content was changed.

### Block model texture references fixed

| model | was | now | upstream basis |
|---|---|---|---|
| `models/block/eldritch_eye.json` | `minecraft:block/sculk` | `pollution:block/eldritch_eye_open` | upstream `blockstates/eldritch_eye.json` (`open=true`) and upstream `models/item/eldritch_eye.json` |
| `models/block/heart_fruit.json` | `minecraft:block/red_mushroom` | `pollution:block/heart_fruit_stage3` | upstream `blockstates/heart_fruit.json` (mature stage) and our `models/item/heart_fruit_block.json` |
| `models/block/tentacle.json` | `minecraft:block/slime_block` | `pollution:block/tentacle_mid` | upstream `blockstates/tentacle.json` multipart (`tentacle_center_mid`) |

The blockstate files still map all states to the single generic model, so
`eldritch_eye` always shows the open texture and `heart_fruit` always shows stage 3.
Splitting the blockstates (open/closed, growth stages, tentacle multipart) is out of
scope here because only `models/block/**` and textures may be edited.

### Remaining fallbacks (intentional or upstream-inherited)

- `models/block/mineral_extractor.json` -> `pollution:block/test_block`: the
  **upstream** `pollution/models/block/mineral_extractor.json` also uses
  `pollution:blocks/test_block`; no authoritative art exists. Left as-is.
- `models/block/rainbow_leaves.json` / `rainbow_sapling.json` -> vanilla oak
  leaves/sapling: upstream models reference `minecraft:blocks/leaves_oak` and
  `sapling_oak` too. No pollution texture exists. Left as-is.
- `models/block/mte.json` -> vanilla iron block: same as upstream (`blocks/iron_block`).
- `models/block/portal.json` -> vanilla textures: port-specific model, upstream
  `portal` uses vanilla `blocks/portal`.
- `models/block/constellation_ritual_crystal.json` and
  `constellation_tower_core.json` -> `astralsorcery:*`;
  `models/block/gtessentia_input.json` -> `thaumcraft:blocks/essentia_input`:
  external-mod textures, not present in the authoritative repo.

## Model-level placeholders (outside texture scope)

These item models still point at `minecraft:item/paper` instead of a pollution
texture. Fixing them means editing `models/item/**`, which this task does not allow;
the corresponding textures are now authoritative, so only the model references
remain.

- `models/item/battery.ev.magic.json`
- `models/item/battery.hv.magic.json`
- `models/item/battery.iv.magic.json`
- `models/item/battery.luv.magic.json`
- `models/item/battery.lv.magic.json`
- `models/item/battery.mv.magic.json`
- `models/item/battery.uv.magic.json`
- `models/item/battery.zpm.magic.json`
- `models/item/cogito_defibrillator.json`
- `models/item/nano_goggles.json`
- `models/item/packaged_aura_node.json`
- `models/item/pesticide.empty.json`
- `models/item/pesticide.full.json`
- `models/item/quantum_goggles.json`
- `models/item/wing_nano.json`
- `models/item/wing_quantum.json`

`pesticide.empty` / `pesticide.full` cannot be fixed even with model edits: upstream
models reference `pollution:items/metaitems/pesticide.*`, but those texture files do
not exist upstream either.

## Notes

- `textures/item/heart_fruit.png`, `heart_fruit_i.png` and `items_heart_fruit.png`
  are intentionally identical to the placeholder hash: `heart_fruit` **is** the
  upstream texture, so they are reported as already authoritative rather than
  missing.
- `textures/block/test_block.json` is a stray JSON file inside the texture folder
  (not a texture); left untouched.
- `normal.png` (Botania GUI icon) was briefly matched to the `starstream_relay`
  blockstate variant key `normal` by the block fallback; that alias is now skipped
  and the file was restored to the placeholder.

