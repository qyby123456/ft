"""Generate exact single-bottle aliases from this pack's aging registration lists."""
from pathlib import Path
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parent
SOURCE = ROOT.parents[2] / 'kubejs/startup_scripts/jiqi/aging_barrel.js'
TARGET = ROOT / 'src/main/resources/assets/farmingtales_drink_anim/aged_aliases.json'

def aliases_from_registration():
    text = SOURCE.read_text(encoding='utf-8-sig')
    def entries(name, field):
        body = re.search(r'const ' + name + r' = \[(.*?)^\];', text, re.S | re.M)
        assert body, name
        values = re.findall(r'\b' + field + r': "([^"]+)"', body[1])
        assert values and len(values) == len(set(values)), name
        return values
    stages = entries('FTAGING_STAGE_INFO', 'suffix')
    assert stages == ['_i', '_g', '_d']
    assert 'return "world_liquor_" + itemPath' in text and 'return "tavern_" + itemPath' in text
    assert 'return ftagingId(wine.name + FTAGING_STAGE_INFO[stage].suffix)' in text
    bases = {'farmingtales:' + name: 'farmingtales:' + name for name in entries('FTAGING_WINES', 'name')}
    for base in entries('FTAGING_NATIVE_BREW_LEVEL_WINES', 'id'):
        namespace, name = base.split(':')
        prefix = {'kaleidoscope_tavern': 'tavern', 'kaleidoscope_world_liquor': 'world_liquor'}.get(namespace, namespace)
        bases['farmingtales:' + prefix + '_' + name] = base
    for namespace in ('brewery', 'vinery'):
        for name in entries('FTAGING_' + namespace.upper() + '_WINES', 'id'):
            bases['farmingtales:' + namespace + '_' + name] = namespace + ':' + name
    profiles = {p['id'] for p in json.loads((TARGET.parent / 'drink_profiles.json').read_text(encoding='utf-8'))}
    assert set(bases.values()) <= profiles, sorted(set(bases.values()) - profiles)
    aliases = {name + suffix: base for name, base in bases.items() for suffix in stages}
    assert not (aliases.keys() & profiles), 'An aged item must not override an existing normal profile.'
    return aliases

if __name__ == '__main__':
    aliases = aliases_from_registration()
    TARGET.write_text(json.dumps(aliases, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    # The isolated client uses the same registered item IDs, with original item models in its hotbar.
    for alias, base in aliases.items():
        namespace, name = alias.split(':')
        base_ns, base_name = base.split(':')
        path = ROOT / f'src/smoke/resources/assets/{namespace}/models/item/{name}.json'
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({'parent': f'{base_ns}:item/{base_name}'}) + '\n', encoding='utf-8')
    report = dict(source=str(SOURCE), source_sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
                  normal_drinks=len(set(aliases.values())), aged_items=len(aliases), aliases=aliases)
    (ROOT / 'aged-aliases-source.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(f'Prepared {len(aliases)} aged variants for {len(set(aliases.values()))} normal drinks.')
