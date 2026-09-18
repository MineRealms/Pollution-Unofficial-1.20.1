from pathlib import Path
TEX = Path(r"H:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\resources\assets\pollution\textures\item")
names = sorted(p.stem for p in TEX.glob("*.png"))
for kw in ("battery", "circuit", "stone", "pesticide", "goggles", "wing", "tarot", "philosopher", "alchemy", "infusion", "lightwell", "matrix", "observation", "defibrillator", "life_on", "aura", "grape", "rune", "slime"):
    hits = [n for n in names if kw in n]
    print(f"{kw}: {hits[:6]}")
