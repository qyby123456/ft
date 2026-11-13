import collections
import hashlib
import json
from pathlib import Path

from authority import NbtReader, prepare

ROOT = Path(__file__).resolve().parent
PEERS = [Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104')]


def save(path, content):
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def decode(path):
    reader = NbtReader(path.read_bytes())
    assert reader.number('>B') == 10
    reader.string()
    template = reader.value(10)
    palette = template.get('palette') or template['palettes'][0]
    return template, {tuple(record['pos']): palette[record['state']]['Name'] for record in template['blocks']}


def signature(path):
    template, blocks = decode(path)
    air = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
    mask = []
    width, height, depth = template['size']
    for gridZ in range(16):
        row = []
        for gridX in range(16):
            blockX = min(width - 1, int((gridX + 0.5) * width / 16))
            blockZ = min(depth - 1, int((gridZ + 0.5) * depth / 16))
            row.append(int(any(blocks.get((blockX, blockY, blockZ), 'minecraft:air') not in air for blockY in (1, 2))))
        mask.append(row)
    variants = []
    for rotation in range(4):
        variants.append(''.join(str(value) for row in mask for value in row))
        mask = [list(row) for row in zip(*mask[::-1])]
    return {'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'size': template['size'], 'rotations': variants, 'canonical_material_blind_ground_mask_sha256': hashlib.sha256(min(variants).encode()).hexdigest()}


def main():
    authority = prepare()
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for index, module in enumerate(manifest['modules']):
        module['authority'] = authority[index]
        module['design_revision'] = 'G2-R2-distinct-sunwell-double-loop'
        module['distinct_design_reference'] = str(ROOT / 'DISTINCT_DESIGN.md')
        save(ROOT / 'incoming' / module['id'] / 'room.json', module)
    modules = {entry['id']: entry for entry in manifest['modules']}
    edges = [
        ('07', 'south', '05', 'north'), ('05', 'south', '01', 'north'),
        ('01', 'east', '06', 'north'), ('06', 'east', '08', 'north'),
        ('08', 'south', '02', 'north'), ('02', 'south', '04', 'south'),
        ('04', 'north', '01', 'south'), ('04', 'east', '08', 'west'),
        ('02', 'east', '09', 'north'), ('08', 'east', '03', 'north'),
    ]
    used = set()
    graph = collections.defaultdict(set)
    for start, start_port, end, end_port in edges:
        for code, port in ((start, start_port), (end, end_port)):
            module_id = 'NAT1-G2-' + code
            assert port in {connector['facing'] for connector in modules[module_id]['connections']}
            assert (code, port) not in used
            used.add((code, port))
        graph[start].add(end)
        graph[end].add(start)
    seen = {'07'}
    queue = collections.deque(seen)
    while queue:
        for neighbor in graph[queue.popleft()] - seen:
            seen.add(neighbor)
            queue.append(neighbor)
    assert len(seen) == 9 and len(edges) - len(seen) + 1 == 2
    plan = {'revision': 'G2-R2-distinct-sunwell-double-loop', 'status': 'CONNECTOR_COMPATIBLE_GRAPH_PLAN_NOT_WORLD_PLACED', 'nodes': sorted(modules), 'edges': [{'from': 'NAT1-G2-' + start, 'from_port': start_port, 'to': 'NAT1-G2-' + end, 'to_port': end_port, 'requires_integration_routing_and_rotation': True} for start, start_port, end, end_port in edges], 'cycles': [['01', '06', '08', '04', '01'], ['04', '08', '02', '04']], 'cycle_rank': 2, 'degrees': {code: len(neighbors) for code, neighbors in sorted(graph.items())}, 'external_entrance': '07 north', 'terminal_branches': ['08 east -> 03 storage', '02 east -> 09 vault'], 'geometry_warning': 'These edges express adjacency, not direct face-to-face world translations. The three-node loop needs routed connectors and collision-free world layout; integration must allocate corridors, rotations and positions. No connectivity in a deployed world is claimed.'}
    save(ROOT / 'distinct-route-plan.json', plan)
    layout = {'concept': manifest['layout']['concept'], 'concept_sha256': manifest['layout']['concept_sha256'], 'purpose': 'Concept appearance reference only; R2 graph supersedes former linear route suggestion', 'placement': 'unassigned; central integration owns world origins, routing and rotations', 'actual_module_graph_plan': str(ROOT / 'distinct-route-plan.json'), 'cycle_rank': 2, 'world_placed': False}
    save(ROOT / 'layout-reference.json', layout)
    manifest['layout'] = layout
    peers = [signature(path) for peer in PEERS for path in sorted((peer / 'incoming').rglob('room.nbt'))]
    matrix = []
    for module_id, module in modules.items():
        own = signature(ROOT / 'incoming' / module_id / 'room.nbt')
        comparisons = []
        for peer in peers:
            similarity = max(sum(first == second for first, second in zip(own['rotations'][0], rotated)) / 256 for rotated in peer['rotations'])
            comparisons.append({'peer': peer['path'], 'peer_sha256': peer['sha256'], 'ground_obstacle_agreement_16x16_rotation_invariant': similarity, 'exact_material_blind_mask': own['canonical_material_blind_ground_mask_sha256'] == peer['canonical_material_blind_ground_mask_sha256']})
        matrix.append({'module': module_id, 'nbt_sha256': own['sha256'], 'material_blind_ground_mask_sha256': own['canonical_material_blind_ground_mask_sha256'], 'comparisons': sorted(comparisons, key=lambda item: -item['ground_obstacle_agreement_16x16_rotation_invariant'])})
    save(ROOT / 'duplicate-audit-input.json', {'revision': plan['revision'], 'scope': 'Read-only snapshots of groups 3 and 7 available locally; not a full all-group uniqueness certification.', 'metric_limits': '16x16 resampled obstacle mask at y=1,2 ignores materials and all rotations, but is not a Minecraft collision simulation. Agreement is not a reuse verdict; primitives and shared authority envelopes can legitimately be similar. Inspect graph, height, landmarks and functional routing together.', 'peer_roots': list(map(str, PEERS)), 'peer_nbt_count': len(peers), 'module_matrix': matrix, 'final_duplicate_matrix_status': 'PENDING_CENTRAL_ALL_GROUP_AUDIT'})
    template, blocks = decode(ROOT / 'incoming/NAT1-G2-01/room.nbt')
    assert blocks[(9, 10, 9)] == 'minecraft:gold_block'
    assert all(blocks[(3, step + 1, step + 4)] == 'minecraft:smooth_sandstone_stairs' for step in range(4))
    assert blocks[(3, 4, 10)] == 'minecraft:cut_sandstone'
    template, blocks = decode(ROOT / 'incoming/NAT1-G2-02/room.nbt')
    assert blocks[(2, 2, 9)] == 'minecraft:lever'
    assert blocks[(2, 1, 9)] == 'minecraft:chiseled_sandstone'
    assert blocks[(3, 1, 9)] == 'minecraft:redstone_lamp'
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS'
    for room in native['rooms']:
        expected = len(modules[room['id']]['connections']) + int(room['id'] == 'NAT1-G2-01')
        assert room['native_reachable_connector_and_gallery_targets'] == expected
    save(ROOT / 'qa/distinct-design-checks.json', {'status': 'STATIC_AND_NATIVE_PATH_PASS', 'graph_cycle_rank': 2, 'graph_edges': len(edges), 'graph_connectors_unique': True, 'obelisk_gold_cap': [9, 10, 9], 'gallery_feet': [3, 5, 10], 'gallery_native_collision_path': 'PASS', 'mechanism': {'lever': [2, 2, 9], 'powered_support': [2, 1, 9], 'adjacent_lamp': [3, 1, 9], 'initial_powered': False, 'status': 'STATIC_CIRCUIT_PRESENT_TICK_TEST_NOT_RUN', 'safety': 'Optional survey lamp; does not gate progress or supply loot'}, 'native_world_placement': 'NOT_RUN', 'all_group_duplicate_audit': 'PENDING'})
    manifest['design_revision'] = plan['revision']
    manifest['distinct_design'] = {'route_plan': str(ROOT / 'distinct-route-plan.json'), 'features': str(ROOT / 'DISTINCT_DESIGN.md'), 'audit_matrix_input': str(ROOT / 'duplicate-audit-input.json'), 'checks': str(ROOT / 'qa/distinct-design-checks.json'), 'all_group_certification': 'PENDING'}
    save(ROOT / 'manifest.json', manifest)
    print(f'DISTINCT DESIGN: two-cycle graph; gallery native path PASS; {len(peers)} peer templates compared without materials')


if __name__ == '__main__':
    main()
