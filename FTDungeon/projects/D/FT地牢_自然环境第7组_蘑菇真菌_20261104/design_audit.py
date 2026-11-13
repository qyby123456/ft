import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'D:/ft工程文件/结构参考_20261103')
from analyze_structures import NbtReader

PASSABLE = {'air', 'cave_air', 'void_air', 'glow_lichen', 'red_mushroom', 'brown_mushroom', 'stone_pressure_plate', 'torch', 'wall_torch', 'soul_torch', 'soul_wall_torch'}


def geometry(path):
    reader = NbtReader(path.read_bytes())
    assert reader.number('>B') == 10
    reader.string()
    data = reader.value(10)
    palette = data.get('palette') or data['palettes'][0]
    solids = {tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'].split(':')[-1] not in PASSABLE}
    signatures = []
    for rotation in range(4):
        points = []
        for block_x, block_y, block_z in solids:
            if rotation == 0:
                points.append((block_x, block_y, block_z))
            elif rotation == 1:
                points.append((-block_z, block_y, block_x))
            elif rotation == 2:
                points.append((-block_x, block_y, -block_z))
            else:
                points.append((block_z, block_y, -block_x))
        minimum = tuple(min(point[axis] for point in points) for axis in range(3))
        normalized = sorted(tuple(point[axis] - minimum[axis] for axis in range(3)) for point in points)
        signatures.append(hashlib.sha256(json.dumps(normalized, separators=(',', ':')).encode()).hexdigest())
    return min(signatures)


def main():
    references = [Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104/recipe.mjs'),
                  Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104/layout-reference.json'),
                  Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104/recipes/frost-group.mjs'),
                  Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104/README.md')]
    external = []
    for folder in [references[0].parent, references[2].parents[1]]:
        external.extend(folder.glob('incoming/*/room.nbt'))
    own = {path.parent.name: geometry(path) for path in ROOT.glob('incoming/*/room.nbt')}
    matches = [{'room': identity, 'other': str(path)} for path in external for identity, signature in own.items() if signature == geometry(path)]
    assert not matches, matches
    edges = [
        ['NAT7-07', 'south', 'NAT7-05', 'north'], ['NAT7-05', 'south', 'NAT7-01', 'north'],
        ['NAT7-01', 'west', 'NAT7-02', 'north'], ['NAT7-02', 'south', 'NAT7-08', 'west'],
        ['NAT7-01', 'east', 'NAT7-04', 'north'], ['NAT7-04', 'south', 'NAT7-08', 'east'],
        ['NAT7-08', 'south', 'NAT7-06', 'north'], ['NAT7-06', 'east', 'NAT7-09', 'north'],
        ['NAT7-09', 'west', 'NAT7-03', 'north'], ['NAT7-03', 'east', 'NAT7-01', 'south']]
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    rows = {entry['id']: entry for entry in manifest['rooms']}
    assert len(rows) == 9
    used = set()
    for first, first_port, second, second_port in edges:
        for identity, port in [(first, first_port), (second, second_port)]:
            assert port in {entry['direction'] for entry in rows[identity]['connections']}
            assert (identity, port) not in used
            used.add((identity, port))
    design = {'group': 'S7 fungal grove', 'revision': 'differentiated-v2', 'nodes': list(rows), 'logical_edges': edges,
              'cycle_rank': len(edges) - len(rows) + 1, 'assembled_in_world': False,
              'signature': 'two-loop mycelial graph with entrance tail; dual-level ring hub, forced S baffles, straight ramp-spine, paired footstep notes',
              'vertical': {identity: room.get('reachable_feet_levels', []) for identity, room in rows.items()},
              'mechanism': {'room': 'NAT7-03', 'components': 'two stone pressure plates adjacent to two note blocks', 'notes': [8, 15], 'actual_blocks_written': True, 'world_trigger_test': 'NOT RUN'},
              'combat': {'implemented': False, 'spawners': False, 'intent': 'compression, elevated flanking choice, S-turn concealment, open courtyard release, bent transition, stepped treasury focus'}}
    (ROOT / 'DESIGN_IDENTITY.json').write_text(json.dumps(design, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    audit = {'status': 'PARTIAL_COMPARISON_NO_EXACT_GEOMETRY_REUSE', 'own_signatures': own, 'external_templates_compared': len(external),
             'exact_matches': matches, 'method': '90-degree-rotation normalized solid-voxel mask; excludes listed passable decoration; independent of material names',
             'limitations': 'Exact geometry check is not a topology equivalence proof. Full frozen all-group repetition matrix pending. Snapshots of other groups may change.',
             'read_only_snapshots': [{'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in references]}
    (ROOT / 'qa/design-audit.json').write_text(json.dumps(audit, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'cycle_rank': design['cycle_rank'], 'external_templates': len(external), 'exact_geometry_matches': len(matches)}))


if __name__ == '__main__':
    main()
