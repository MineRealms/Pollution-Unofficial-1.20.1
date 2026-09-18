import difflib, re
from pathlib import Path
ROOT = Path(r"H:\MinecraftMods\Pollution-Unofficial-1.20.1")
TEX = ROOT / "src/main/resources/assets/pollution/textures/item"
placeholder = (TEX / "heart_fruit.png").read_bytes()
up_pool = [p.stem for p in TEX.glob("*.png")]
true_ph = [p.stem for p in TEX.glob("*.png") if p.read_bytes() == placeholder]
print(f"true placeholders: {len(true_ph)}")
for name in sorted(true_ph):
    close = difflib.get_close_matches(name, [u for u in up_pool if u != name], n=3, cutoff=0.55)
    print(f"  {name}  <-  {close}")
