# Biology integration test probes

Target: **local `G:/emoney-bio-test` only**. The bridge wrapper reads that directory's token and connects to loopback:48790; it never reads production credentials. Start this test server with its `start-bio-test.cmd` first. Do not run the mutating probes in a production world.

```
python -I tools/bio/run_final_probes.py
```

The server needs the tested TC4R 20759, GTCEu 7.5.3, this Pollution build, the TH ore-pig policy build, KubeJS biological recipes, and the GTNN Botania initialization fix. `G:/emoney-bio-test/BIO_MANIFEST.json` records the exact jars. The minimal suite intentionally omits unrelated client/pack mods.

- `bio_dump`: read-only GT recipe/condition/content dump, six elemental aspect values, ore-pig pool.
- `bio_machine_probe`: two real TC jars; single batch split 64+36, full output retains, partial 6+4, controller NBT restores.
- `bio_gt_output_probe`: two real GT output hatches; 17mB accepted/127mB pending, exact remainder survives NBT and reaches the second hatch.
- `bio_smelting_run`: actual item bus/fuel hatch/energy hatch and both controllers. Two dusts consumed once, pause/unform plus save/load, exactly 640EU and 20mB fire -> 2 essentia or 288mB.
- `bio_recipe_run`: real LuV reactor lookup and RecipeLogic, no/ordinary cleanroom rejected, missing catalyst rejected, two 240t cycles -> 64 cells +1000mB sludge, reusable brain remains.
- `bio_bootstrap_probe`: IV rejected, LuV sterile reactor consumes the complete bootstrap inputs -> one iPS brain.

**Test limitations:** probes allocate temporary blocks in verified-air positions and restore them. Controllers' part lists/formed flags and a cleanroom-provider fixture are injected; no full structure formation is claimed. Processing loops invoke the real logic synchronously with fixture energy, rather than waiting hundreds of world ticks. Per-batch NBT serialization is tested, not crash-atomic persistence between separate chunks. The output dump covers selected GT recipes, not every TH custom recipe registry or the complete pack.

Reports: `G:/emoney-bio-test/bio-reports/{runtime-recipes.json,assertions.json,machine-probes.json,final-probes.txt}`. Run a fresh dump before evaluating assertions; do not count a stale file as the current run.

Production jar mapping: probes use SRG Minecraft names because this Rosetta build does not reliably resolve every inherited named method. Compiler classpath needs extracted GT jar-in-jar dependencies under the local test server's `libraries/bio-debug`; do not also install these as duplicate runtime mods.
