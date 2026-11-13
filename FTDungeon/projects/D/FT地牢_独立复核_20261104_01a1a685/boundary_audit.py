import collections
import json
import sys
from pathlib import Path
import audit

ROOT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parent
reports = []
directions = [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]
for specification in json.loads((ROOT / 'route-input.json').read_text(encoding='utf-8')):
    template = audit.decode(Path(specification['candidate']))
    size = template['size']
    blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
    solids = {position for position, state in blocks.items() if state['Name'] not in audit.AIR}
    remaining = set(solids)
    components = []
    while remaining:
        seed = remaining.pop()
        queue = collections.deque([seed])
        component = [seed]
        while queue:
            position = queue.popleft()
            for direction in directions:
                neighbor = tuple(position[index] + direction[index] for index in range(3))
                if neighbor in remaining:
                    remaining.remove(neighbor)
                    queue.append(neighbor)
                    component.append(neighbor)
        if not any(position[1] == 0 for position in component):
            components.append({'count': len(component), 'sample': component[:12], 'materials': dict(collections.Counter(blocks[position]['Name'] for position in component))})
    fluid_edges = []
    fluids = collections.Counter()
    for position, state in blocks.items():
        if state['Name'] not in {'minecraft:water', 'minecraft:lava'}:
            continue
        fluids[state['Name']] += 1
        for direction in directions:
            if direction == (0,1,0):
                continue
            neighbor = tuple(position[index] + direction[index] for index in range(3))
            outside = any(neighbor[index] < 0 or neighbor[index] >= size[index] for index in range(3))
            name = blocks.get(neighbor, {'Name': 'minecraft:air'})['Name']
            if outside or name in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}:
                fluid_edges.append({'position': position, 'neighbor': neighbor, 'outside': outside, 'fluid': state['Name']})
    reports.append({'group': specification['group'], 'sha256': specification['sha256'], 'solids_not_connected_to_y0_by_six_face_adjacency': components, 'fluid_counts': dict(fluids), 'fluid_outside_or_air_neighbors_excluding_up_count': len(fluid_edges), 'fluid_edge_samples': fluid_edges[:25], 'scope': 'Static six-face graph, not attachment/gravity proof. Fluids only direct water/lava; does not model permeability, waterlogged flow, fluid levels, updates or external world. Components and fluid edges are review signals, not automatic defects.'})
(ROOT / 'independent-boundaries.json').write_text(json.dumps(reports, ensure_ascii=False, indent=2), encoding='utf-8')
for report in reports:
    print(report['group'], 'unanchored components', len(report['solids_not_connected_to_y0_by_six_face_adjacency']), 'fluid edges', report['fluid_outside_or_air_neighbors_excluding_up_count'])
