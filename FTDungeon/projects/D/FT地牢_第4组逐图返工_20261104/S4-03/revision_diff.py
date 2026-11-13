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
    old_path = ROOT / 'history/rejected-e10200d3/incoming/S4-03/room.nbt'
    new_path = ROOT / 'incoming/S4-03/room.nbt'
    previous = read(old_path)
    current = read(new_path)
    changes = [{'position': position, 'before': previous.get(position), 'after': current.get(position)} for position in sorted(previous.keys() | current.keys()) if previous.get(position) != current.get(position)]
    assert all(change['after']['state']['Name'] in {'minecraft:dark_oak_log', 'minecraft:mangrove_roots'} for change in changes)
    assert all(current.get(position) == block for position, block in previous.items() if block['state']['Name'] in {'minecraft:chest', 'minecraft:flower_pot', 'minecraft:potted_fern', 'minecraft:ochre_froglight', 'minecraft:chain', 'minecraft:vine', 'minecraft:dark_oak_slab', 'minecraft:dark_oak_fence'})
    assert all(current.get((east, up, south)) == previous.get((east, up, south)) for east in range(9, 15) for up in range(2, 8) for south in range(9, 14))
    roots = {position for position, record in current.items() if record['state']['Name'] in {'minecraft:dark_oak_log', 'minecraft:mangrove_roots'}}
    reached = {(18, 9, 2)}
    queue = list(reached)
    while queue:
        east, up, south = queue.pop()
        for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
            neighbor = (east + delta[0], up + delta[1], south + delta[2])
            if neighbor in roots and neighbor not in reached:
                reached.add(neighbor)
                queue.append(neighbor)
    endpoints = [(16, 4, 5), (19, 4, 6), (16, 4, 9), (19, 4, 10), (16, 4, 11)]
    assert all(point in reached for point in endpoints), endpoints
    report = {'status': 'PASS', 'sha256': hashlib.sha256(new_path.read_bytes()).hexdigest(), 'previous_sha256': hashlib.sha256(old_path.read_bytes()).hexdigest(), 'changed_coordinate_count': len(changes), 'all_changes_are_wood_or_roots': True, 'chest_island_and_furniture_lamps_vines_fences': 'UNCHANGED', 'east_main_trunk_seed': [18, 9, 2], 'face_connected_splayed_endpoints': endpoints, 'east_root_component_blocks': len(reached), 'changes': changes}
    (ROOT / 'revision-diff.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({key: value for key, value in report.items() if key != 'changes'}, indent=2))


if __name__ == '__main__':
    main()
