# Language coverage audit (Pollution Unofficial, Forge 1.20.1)

Audit of every translation key referenced by the port, compared against the
datagen output `src/generated/resources/assets/pollution/lang/en_us.json` and
the hand-maintained `src/main/resources/assets/pollution/lang/zh_cn.json`.

Symptoms this audit addresses:

* JEI tooltips falling back to the mod name only / raw translation keys.
* JEI category titles and info lines rendering as raw keys on English clients.

## 1. Method

Keys were collected from:

* `Component.translatable("...")` and `Component.translatable(variable)` sites
  under `src/main/java/meowmel/pollution/**`;
* string literals matching `"pollution.*"` (dynamic keys such as
  `pollution.astral_data.function.<name>`, `pollution.machine.mana_pool_hatch.type.<name>`,
  `StarstreamLinkerBehavior#modeMessageKey`);
* `provider.add("...", ...)` in `Pollution.java`;
* JEI code (`compat/jei/**`) - `getTranslationKey`/`translatable` usage;
* item/block builders - `.lang(...)` in `common/item/PollutionItems.java`.

Keys from other mods (`gtceu.*`, `gregtech.*`, `metaarmor.*`) are referenced by
our code but shipped by GTCEu; they are excluded from the "missing" counts.

Result: **144 distinct keys referenced by our code** - 133 own keys + 11
GTCEu keys.

| group | count |
| --- | --- |
| `pollution.machine.*` | 56 |
| `pollution.jei.*` | 18 |
| `pollution.astral_data.*` | 10 |
| `pollution.item.*` | 9 |
| `pollution.magic.failure.*` | 9 |
| `pollution.crystal_quality.*` | 6 |
| `pollution.filter.*` | 5 |
| `pollution.flesh_heart.*` | 4 |
| `pollution.armor.*` | 4 |
| `pollution.mineral_extractor.*` | 3 |
| `pollution.command.*` | 3 |
| `pollution.bauble.*` | 2 |
| `pollution.tarot.*`, `pollution.modeChanged.*`, `pollution.effect.*` | 1 each |
| `itemGroup.pollution.main` | 1 |
| GTCEu (`gtceu.*` / `gregtech.*` / `metaarmor.*`) | 11 |

Note: `pollution.magic.<property>` constants in
`api/recipes/properties/MagicRecipeProperties` are NBT/recipe property ids, not
translation keys, and `pollution.warned` is a persistent-data flag; both were
excluded.

## 2. English (`en_us.json`, datagen)

The checked-in generated file has **789 keys**. Before this audit it was
missing **48** keys referenced by code (11 GTCEu + 37 own). Of the 37 own keys:

* 34 were not declared in the LANG provider at all -> **added** to
  `Pollution.java` (provider now has 137 entries);
* 3 (`pollution.machine.mana_hatch.capacity/input_rate/output_rate`) were
  already in the provider but the generated file predates them;
* 7 `material.pollution.*` keys are produced by GT's own datagen, see
  section 4.

Keys added to the provider (34):

```
pollution.item.vis_checker.tooltip
pollution.item.starstream_linker.mode.input
pollution.item.starstream_linker.mode.network
pollution.item.starstream_linker.tooltip.toggle
pollution.item.starstream_linker.tooltip.input
pollution.item.starstream_linker.tooltip.network
pollution.item.starstream_linker.unported
pollution.machine.aspect_tank.tooltip
pollution.machine.aspect_tank.help
pollution.machine.aspect_tank.tooltip.auto_output
pollution.machine.aspect_tank.tooltip.capacity
pollution.machine.aspect_tank.tooltip.locked
pollution.machine.aspect_tank.tooltip.stored
pollution.machine.aspect_tank.tooltip.voiding
pollution.machine.small_node_generator.tooltip
pollution.machine.source_charge.tooltip
pollution.jei.machine_info.title
pollution.jei.magic_hatch.title
pollution.jei.magic_amplification.title
pollution.jei.magic_amplification.tags
pollution.jei.magic_amplification.constellation
pollution.jei.magic_amplification.tarot
pollution.jei.magic_amplification.footer
pollution.jei.recipe.cost
pollution.jei.recipe.cost.vis
pollution.jei.recipe.cost.infused
pollution.jei.recipe.cost.mana
pollution.jei.recipe.cost.life
pollution.jei.recipe.gate
pollution.jei.recipe.gate.research
pollution.jei.recipe.gate.tarot
pollution.jei.recipe.gate.astral
pollution.jei.recipe.gate.catalyst
pollution.jei.recipe.process_tags
```

JEI category titles use the agreed names: **Magic Machine Info**,
**Magic Hatch Info**, **Amplification Info**.

The generated `en_us.json` itself is **stale** (it predates the last material
and mana-hatch changes). It cannot be regenerated in this environment; run
`gradlew runData` to refresh it. Until then the 37 provider keys above and the
7 material keys below are still absent from the shipped file.

## 3. Chinese (`zh_cn.json`, hand-maintained)

Before: **736 keys**. Missing:

* 7 own keys referenced by code (4 `pollution.flesh_heart.*`,
  3 `pollution.mineral_extractor.*`);
* 90 keys that exist in `en_us.json` but not in the Chinese file: 16
  `block.pollution.*` (flux scrubbers per tier, solar plate MK4-6, LuV vis
  generator, portal), 67 `material.pollution.*`, and the 7 tooltip/message
  keys above.

After: **833 keys**, valid JSON, no duplicate keys. `en_us.json` minus
`zh_cn.json` is now **0**; every own key referenced by code is present.

Added (97):

* 16 blocks: `ev/hv/iv/lv/mv_flux_scrubber`,
  `hv/lv/mv_solar_plate_4/5/6`, `luv_vis_generator`, `portal`;
* 74 materials (see section 4);
* 7 tooltips/messages: `pollution.flesh_heart.bound/level/next_growth/other`,
  `pollution.mineral_extractor.enabled/disabled/mode`.

Updated existing JEI titles to the agreed names: `魔导机器信息`,
`魔导仓室信息`, `增幅信息`.

Chinese material names follow the canonical names already used in the
material class comments (e.g. `太虚玄钢`, `既存之枢`, `消逝之枢`,
`蕴魔硫铅盐`) and the names requested for the special fluids
(`白魔素`, `黑魔素`, `星魔素`, `精灵素`, `精灵元素`, `富集灵气`,
`血色灵气`, `贤者之盐`).

## 4. GT material names

144 materials are registered under `pollution:`. GTCEu's datagen emits
`material.pollution.<name>` for every material through
`com.gregtechceu.gtceu.data.lang.MaterialLangGenerator.generate(provider, registry)`,
wired in `CommonProxy` for every `MaterialRegistry` (verified against the
GTCEu 7.5.3 jar).

The checked-in `en_us.json` contains 137 of 144. The 7 missing ones
(`whitemansus`, `blackmansus`, `starrymansus`, `elven`, `elven_elementium`,
`rich_aura`, `erich_aura`) are simply newer than the last `runData`; GT's
datagen will emit them on the next run.

**Do not add them to the LANG provider manually**: Forge's
`LanguageProvider.add` throws `Duplicate translation key` when GT's generator
has already added the same key, so manual entries would break `runData`.

All 74 materials missing from the Chinese file were added to `zh_cn.json`
(67 that were in the English file + the 7 above).

## 5. Items (`PollutionItems`)

152 items are registered. All of them receive a name:

* `register(String name)` always applies `.lang(displayName(name))`;
* `register(name, factory, lang)` passes an explicit display name;
* `magicBattery(...)` and `filter(...)` pass `displayName(name)`;
* `VIS_CHECKER` uses `.lang("Vis Checker")`.

No item is registered without a `.lang(...)` call, and all 152
`item.pollution.*` keys are present in both language files.
**Items fixed: 0.**

## 6. Known leftovers

* The checked-in generated `en_us.json` is stale; run `gradlew runData` to
  refresh it (this is what actually makes the new English keys ship).
* `compat/jei/PollutionJeiPlugin.machineInfoRecipes()` still contains 8
  hardcoded Chinese `Component.literal` lines (vis generator / flux scrubber /
  flux fuel cell / mineral extractor info pages); they cannot be localized.
  Converting them to `Component.translatable` keys is recommended but was
  outside the allowed file set of this audit.
* `en_ud.json` (generated upside-down English) was not audited.
* GTCEu keys referenced by our code (`gtceu.universal.tooltip.*`,
  `metaarmor.message.nightvision.*`, `gregtech.multiblock.*`) ship with GTCEu
  and are intentionally not duplicated here.

## 7. Summary

| metric | before | after |
| --- | --- | --- |
| keys referenced by code | 144 (133 own + 11 GTCEu) | 144 |
| own keys missing from EN provider | 34 | 0 |
| provider entries | 103 | 137 |
| own keys missing from generated `en_us.json` | 37 (34 + 3 stale) | 37 until `runData`; 0 after |
| materials missing from generated `en_us.json` | 7 | 7 until `runData`; 0 after |
| own keys missing from `zh_cn.json` | 7 | 0 |
| `en_us.json` keys missing from `zh_cn.json` | 90 | 0 |
| `zh_cn.json` keys | 736 | 833 |
| items without `.lang(...)` | 0 | 0 |
