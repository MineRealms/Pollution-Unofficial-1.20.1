#!/usr/bin/env python3
"""Runs datagen and auto-heals missing texture errors until it succeeds.

Registrate aborts on the first missing pollution:item/<id> or
pollution:block/<id> texture. This loop reads the crash, creates the texture
(upstream import preferred, placeholder fallback) and reruns - until
BUILD SUCCESSFUL or a non-texture failure appears.
"""

from __future__ import annotations

import re
import shutil
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"
PLACEHOLDER = ASSETS / "textures/item/heart_fruit.png"
LOG = ROOT / "run/datagen-loop.log"
TEXTURE_ERROR = re.compile(r"Texture pollution:(item|block)/([a-zA-Z0-9_.\-]+) does not exist")


def make_texture(kind: str, name: str) -> None:
    folder = ASSETS / "textures" / kind
    folder.mkdir(parents=True, exist_ok=True)
    target = folder / f"{name}.png"
    if target.exists():
        return
    pool = {p.stem: p for p in (ASSETS / "textures/item").glob("*.png") if p != PLACEHOLDER}
    norm = name.replace(".", "_")
    source = pool.get(norm)
    if source is None:
        for up, png in pool.items():
            if up.endswith("_" + norm) or norm.endswith("_" + up) or norm in up:
                source = png
                break
    if source is not None:
        shutil.copyfile(source, target)
        mcmeta = source.with_suffix(".png.mcmeta")
        if mcmeta.exists():
            shutil.copyfile(mcmeta, target.with_suffix(".png.mcmeta"))
    else:
        shutil.copyfile(PLACEHOLDER, target)
    print(f"healed {kind}/{name}.png (source: {source.name if source else 'placeholder'})")


def main() -> int:
    for attempt in range(1, 16):
        with LOG.open("wb") as handle:
            process = subprocess.Popen(
                ["cmd.exe", "/c", "gradlew.bat", "runData", "--console=plain"],
                cwd=ROOT, stdout=handle, stderr=subprocess.STDOUT,
            )
        print(f"[{attempt}] datagen started pid={process.pid}")
        deadline = time.time() + 360
        healed = False
        while time.time() < deadline:
            time.sleep(6)
            if process.poll() is not None:
                break
            if not LOG.exists():
                continue
            text = LOG.read_text(encoding="utf-8", errors="ignore")
            match = TEXTURE_ERROR.search(text)
            if match:
                subprocess.run(["taskkill", "/F", "/T", "/PID", str(process.pid)],
                               capture_output=True)
                make_texture(match.group(1), match.group(2))
                healed = True
                break
        if healed:
            time.sleep(3)
            continue
        text = LOG.read_text(encoding="utf-8", errors="ignore")
        if "BUILD SUCCESSFUL" in text:
            print("datagen succeeded")
            return 0
        print("datagen stopped without success; last lines:")
        for line in text.splitlines()[-12:]:
            print("  " + line[:200])
        return 1
    print("gave up after 15 attempts")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
