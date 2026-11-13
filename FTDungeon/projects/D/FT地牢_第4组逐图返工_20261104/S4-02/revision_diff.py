import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def read(path):
    data = plain(read_nbt(path)[1])
    return {tuple(record['pos']): {'state': data['palette'][record['state']], 'nbt': record.get('nbt')} for record in data['blocks']}


def main():
    old_path = ROOT / 'history/rejected-9a101d56/incoming/S4-02/room.nbt'
    new_path = ROOT / 'incoming/S4-02/room.nbt'
    previous = read(old_path)
    current = read(new_path)
    changes = []
    for position in sorted(previous.keys() | current.keys()):
        before = previous.get(position)
        after = current.get(position)
        if before != after:
            changes.append({'position': position, 'before': before, 'after': after})
    assert all(change['position'][1] <= 5 for change in changes)
    protected_names = {'minecraft:chest', 'minecraft:bookshelf', 'minecraft:dark_oak_fence', 'minecraft:dark_oak_slab', 'minecraft:potted_fern', 'minecraft:flower_pot', 'minecraft:ochre_froglight', 'minecraft:chain', 'minecraft:vine', 'minecraft:iron_bars'}
    for position, block in previous.items():
        if block['state']['Name'] in protected_names:
            assert current.get(position) == block, position
    old_surface = sum(previous.get((east, 3, south), {}).get('state', {}).get('Name') == 'minecraft:water' for east in range(4, 25) for south in range(4, 23))
    new_surface = sum(current.get((east, 3, south), {}).get('state', {}).get('Name') == 'minecraft:water' for east in range(4, 25) for south in range(4, 23))
    report = {'status': 'PASS', 'previous_sha256': hashlib.sha256(old_path.read_bytes()).hexdigest(), 'sha256': hashlib.sha256(new_path.read_bytes()).hexdigest(), 'changed_coordinate_count': len(changes), 'all_changes_y_at_most': 5, 'protected_furniture_lamps_vines_grille': 'UNCHANGED', 'exposed_water_interior_before': old_surface, 'exposed_water_interior_after': new_surface, 'changes': changes}
    (ROOT / 'revision-diff.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({key: value for key, value in report.items() if key != 'changes'}, indent=2))


if __name__ == '__main__':
    main()
