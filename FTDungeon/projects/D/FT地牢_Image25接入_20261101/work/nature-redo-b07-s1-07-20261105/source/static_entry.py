import hashlib
import json
import os
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    group = json.loads((ROOT / 'group.json').read_text(encoding='utf-8'))
    folder = ROOT / group['rooms'][0]['id']
    metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
    structure = Structure.read(folder / 'room.nbt')
    fingerprint = hashlib.sha256((folder / 'room.nbt').read_bytes()).hexdigest()
    assert fingerprint == metadata['sha256'] == group['rooms'][0]['sha256']
    assert structure.data_version == 3465 and structure.size == (27, 17, 29)
    assert len(structure.blocks) == 13311 and not structure.entities and not structure.block_entities
    assert hashlib.sha256(Path(metadata['reference']).read_bytes()).hexdigest() == metadata['reference_sha256']
    door_points = {tuple(point) for point in metadata['doors']['positions']}
    found = {point for point, state in structure.blocks.items() if state.name.endswith('_door')}
    assert found == door_points
    for point in found:
        properties = dict(structure.get(point).properties)
        assert properties['open'] == properties['powered'] == 'false'
    for across, feet, forward in metadata['route']:
        for vertical in range(int(feet), int(feet + 1.8) + 1):
            point = (across, vertical, forward)
            state = structure.get(point)
            if point in found:
                continue
            if feet % 1 and vertical == int(feet):
                assert state.name == 'minecraft:stone_brick_slab' and dict(state.properties)['type'] == 'bottom'
            else:
                assert state.name == 'minecraft:air', str((point, state))
    water = []
    for point, state in structure.blocks.items():
        assert state.name.startswith('minecraft:')
        if state.name == 'minecraft:water':
            water.append(point)
            for delta in [(1,0,0),(-1,0,0),(0,-1,0),(0,0,1),(0,0,-1)]:
                neighbor = tuple(value + offset for value, offset in zip(point, delta))
                assert structure.get(neighbor).name != 'minecraft:air'
    assert all(structure.get((across,16,forward)).name != 'minecraft:air' for across in range(27) for forward in range(29))
    report = {'status': 'PASS_STATIC_ONLY', 'source_nbt_sha256': fingerprint, 'records': len(structure.blocks), 'size': structure.size, 'roof_columns': 783, 'water_cells': len(water), 'route_targets': len(metadata['route']), 'doors_closed': True, 'containers': 0, 'palette': dict(Counter(state.name for state in structure.blocks.values())), 'scope': 'Bounds, closed source doors, supported half-step route air/slab/door vocabulary and explicit full roof; native closed/open/body/stair checks required.'}
    save(folder / 'qa/static.json', report)
    save(ROOT / 'qa/static.json', report)
    spec = {'id': metadata['id'], 'sha256': fingerprint, 'size': metadata['size'], 'records': len(structure.blocks), 'feet_y': 2, 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'ports': [port['feet'] for port in metadata['ports']], 'lane': [], 'specialized_route': True, 'water_cells': len(water), 'containers': 0, 'rails': 0, 'rail_networks': [], 'roof_exceptions': [], 'entry_route': metadata['route'], 'doors': metadata['doors']['positions'], 'steps': metadata['steps']}
    save(folder / 'spec.json', spec)
    print(json.dumps(report))


if __name__ == '__main__':
    main()
