"""Check the packaged model layout, GT formed skins, translations and LDLib CTM."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / 'src/main/resources/assets/pollution'
GENERATED = ROOT / 'src/generated/resources/assets/pollution'


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def main():
    errors = []
    models = {}
    for base in (MAIN, GENERATED):
        for path in (base / 'models').rglob('*.json'):
            key = 'pollution:' + path.relative_to(base / 'models').with_suffix('').as_posix()
            data = read(path)
            if key in models and (data.get('loader') == 'gtceu:machine' or models[key].get('loader') == 'gtceu:machine'):
                errors.append(f'Model masks generated metadata: {key}')
            # processResources gives authored models precedence over generated item icons.
            models.setdefault(key, data)

    def references(value):
        if isinstance(value, dict):
            for key, child in value.items():
                if key in ('parent', 'model') and isinstance(child, str) and child.startswith('pollution:'):
                    yield child
                else:
                    yield from references(child)
        elif isinstance(value, list):
            for child in value:
                yield from references(child)

    def visit(key, ancestry):
        if key in ancestry:
            errors.append(f'Model cycle: {" -> ".join((*ancestry, key))}')
            return
        if key not in models:
            errors.append(f'Missing model: {key}')
            return
        for ref in references(models[key]):
            visit(ref, (*ancestry, key))

    for key in models:
        visit(key, ())
    parts = 0
    controllers = 0
    for path in (GENERATED / 'models/block/machine').glob('*.json'):
        data = read(path)
        name = path.stem
        if any(part in name for part in ('vis_hatch', 'infused_fluid_hatch', 'mana_input_hatch', 'mana_output_hatch',
                                         'mana_pool_', 'tarot_hatch', 'astral_lens_hatch', 'flux_muffler')):
            parts += 1
            if set(data.get('replaceable_textures', [])) != {'bottom', 'top', 'side'}:
                errors.append(f'Part cannot replace its casing: {name}')
        if 'texture_overrides' in data:
            controllers += 1
    en = read(GENERATED / 'lang/en_us.json')
    zh = read(MAIN / 'lang/zh_cn.json')
    missing = sorted(set(en) - set(zh))
    errors.extend('Missing Chinese key: ' + key for key in missing)
    for source in (ROOT / 'src/main/java').rglob('*.java'):
        for key in re.findall(r'(?:translatable|setHoverTooltips)\("(pollution\.[^"%]+)"', source.read_text(encoding='utf-8')):
            if not key.endswith('.') and (key not in en or key not in zh):
                errors.append(f'Untranslated literal in {source.name}: {key}')
    connections = 0
    for path in (MAIN / 'textures').rglob('*.png.mcmeta'):
        data = read(path)
        connection = data.get('ldlib', {}).get('connection')
        if connection and connection.startswith('pollution:'):
            connections += 1
            texture = MAIN / 'textures' / (connection.split(':', 1)[1] + '.png')
            if not texture.exists():
                errors.append(f'Missing connected texture: {connection}')
    if errors:
        raise SystemExit('\n'.join(errors))
    print(f'PASS: {len(models)} models without masks/cycles; {parts} replaceable parts; '
          f'{controllers} controller skins; {connections} CTM sheets; {len(en)} translated English keys')


if __name__ == '__main__':
    main()
