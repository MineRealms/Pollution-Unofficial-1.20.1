"""Translate the retained 1.12 CTM metadata to GT Modern / LDLib's 2x2 format."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/pollution/textures'

def main():
    changed = 0
    for path in sorted(ROOT.rglob('*.png.mcmeta')):
        data = json.loads(path.read_text(encoding='utf-8'))
        if 'ctm' not in data:
            continue
        connection = path.with_name(path.name.removesuffix('.png.mcmeta') + '_ctm.png')
        if not connection.exists():
            continue
        from PIL import Image
        with Image.open(connection) as image:
            if image.width != image.height or image.width % 2:
                raise ValueError(f'Not a 2x2 CTM sheet: {connection}')
        data.pop('ctm')
        texture = connection.relative_to(ROOT).with_suffix('').as_posix()
        data['ldlib'] = {'connection': 'pollution:' + texture}
        path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')
        changed += 1
    print(f'Converted {changed} CTM metadata files')

if __name__ == '__main__':
    main()
