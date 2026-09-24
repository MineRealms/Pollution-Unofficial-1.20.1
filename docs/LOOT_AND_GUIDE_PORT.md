# Loot and magic guide port

`loaders/loot/GregTechLootTable` listens to Forge's `LootTableLoadEvent` and
adds one named pool (`pollution:gregtech_1_12_port`) to the same structure
whitelist used by the 1.12 loader. It runs only while
`ConfigHolder.INSTANCE.worldgen.addLoot` is enabled. The pool uses one roll,
the upstream weights, and uniform min/max stack counts. This avoids relying on
GregTech's `main` pool name: modern vanilla tables commonly use `pool0`.

The eight `thebetweenlands` IDs are retained as optional targets. Pollution has
no Betweenlands dependency, so absent tables do not produce an event and have
no effect. The 1.12 village blacksmith target is mapped to
`minecraft:chests/village/village_weaponsmith`.

Two 1.12 entries have no direct item in the current GT/Minecraft registry:

* GT 1.12 credits (copper, cupronickel, silver, gold) are omitted because GT
  Modern 7.5.3 does not expose those `MetaItems`.
* `minecraft:piston_extension` is a block state with no item and is omitted.

The remaining pipes, plates, wires, ingots, steam machines/hatches/buses and
the piston use GregTech Modern `ChemicalHelper`, `GTMachines` or vanilla items.

The four upstream static magic handbook pages are exposed through JEI item
information (`compat/jei/MagicGuideJeiInfo`) on the data wafer, all tarot cards,
rock crystal seed and celestial crystal embryo. They are descriptions only;
the Astral celestial machines and recipe maps are not present in this 1.20.1
port. `MagicRecipeProperties.guidePage` and the optional data-info reader keep
the old metadata format available for a future real GT recipe map. Add the
`pollution.magic.guide.*` translations in the Registrate language provider
when changing the text; the JEI class intentionally only owns the stable key
layout.
