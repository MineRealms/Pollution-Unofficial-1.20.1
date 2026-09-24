# Client rendering parity — 2026-09-24

Reference: upstream `Pollution` commit `098834e`; target baseline `4b279ff`.

## Slime gel

The six aspect slimes already inherit the gel layer through
`PollutionSlimeRenderer extends SlimeRenderer`. The Forge 47.4.23 mapped
Minecraft jar confirms that `SlimeRenderer` installs `SlimeOuterLayer` in its
constructor. That layer resolves the texture through the parent renderer and
uses `RenderType.entityTranslucent`, so it uses the six Pollution textures.
This is the equivalent of upstream `LayerTcSlimeGel`; no second layer is needed.
The missing-gel entry in the initial gap audit was a false positive.

## Mineral extractor item

`MineralExtractorBlockItem.initializeClient` now supplies the lazy
`ItemMineralExtractorRenderer` (Forge `BlockEntityWithoutLevelRenderer`). Its
existing `builtin/entity` item model provides transforms for inventory, hands,
ground and item frames. The item and placed renderer share `renderGeometry`,
which draws the same energy frame, edge flares, orbit rings and crystal without
creating or ticking a fake block entity. The model now also specifies its
particle texture and front GUI lighting. Item datagen explicitly preserves the
hand-authored model.

## Curios goggles and wings

`PollutionCuriosRenderers` registers renderers for nano/quantum goggles and
nano/quantum wings during client setup. The original upstream 128x64 armour
textures are copied unchanged to `pollution:textures/armor/`. Their UV layout
is the regular biped outer armour layout at twice the resolution.

`PollutionCurioArmorRenderer` renders the head parts for goggles and torso/arm
parts for wings, copying the wearer's animated pose and supporting enchanted
glint. Curios controls visibility, cosmetic slots and renderer resource reloads.
Invisible wearers and non-humanoid models do not render the accessory. Upstream
wings used a chest armour texture; this preserves that appearance rather than
introducing an unrelated elytra model.

## Validation

- Checked the installed Curios 5.14.1 API signatures and TC4R's own Curios
  renderer pattern against the local jars/source.
- Checked `BlockEntityWithoutLevelRenderer`, `ItemRenderer.getArmorFoilBuffer`
  and vanilla slime-layer bytecode against Forge 47.4.23.
- Confirmed all four armour textures are byte-identical to upstream and the
  mineral extractor item JSON parses.
- Build and runtime validation are handled with the combined port changes.
  Client smoke check: view all six slime types; render an extractor in JEI,
  inventory, each hand, on the ground and in an item frame; equip each Curios
  accessory, crouch/swim/fly, toggle Curios visibility, test cosmetic slots,
  enchantment glint and F3+T resource reload.
