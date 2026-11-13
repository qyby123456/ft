import copy
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, AIR, state, plain

root = Path(__file__).resolve().parent
index_path = Path(r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\FINAL-80-SOURCE-INDEX.json')
index = json.loads(index_path.read_text(encoding='utf-8'))
origins = {1: (30, 2, 2), 2: (64, 2, 14), 3: (30, 2, 99), 4: (60, 2, 89), 5: (68, 2, 44), 6: (0, 2, 119), 7: (63, 0, 144), 8: (110, 2, 100), 9: (106, 2, 56)}
links = [
    ('古树厅—书房', (1, 'east'), (2, 'north'), [(54,14),(59,14),(59,6),(75,6),(75,14)], 4),
    ('书房—藤蔓长廊', (2, 'south'), (5, 'north'), [(75,34),(75,44)], 4),
    ('长廊—主厅', (5, 'south'), (4, 'north'), [(75,80),(75,89)], 4),
    ('主厅—水景小室', (4, 'west'), (3, 'east'), [(60,109),(54,109),(54,105),(48,105)], 4),
    ('小室—折角廊', (3, 'west'), (6, 'north'), [(30,109),(19,109),(19,119)], 4),
    ('主厅—空中花园', (4, 'east'), (8, 'west'), [(90,109),(100,109),(100,126),(110,126)], 4),
    ('花园—双守卫宝库', (8, 'north'), (9, 'south'), [(120,100),(120,84)], 4),
    ('入口—主厅', (7, 'north'), (4, 'south'), [(75,144),(75,123)], 4),
]
used_ports = {(number, side) for _, first, last, _, _ in links for number, side in (first, last)} | {(7, 'south')}
size = (145, 24, 173)
result = Structure(size)
modules = []
targets = []
door_changes = []
caps = []
occupied = {}
for item in index['items']:
    number = item['image']
    if item['group'] != 1 or number not in origins:
        continue
    path = Path(item['candidate'])
    assert hashlib.sha256(path.read_bytes()).hexdigest() == item['sha256']
    module = Structure.read(path)
    metadata = json.loads(path.with_suffix('.json').read_text(encoding='utf-8'))
    origin = origins[number]
    assert not module.entities
    def translated(position):
        return tuple(position[axis] + origin[axis] for axis in range(3))
    for position, block_state in module.blocks.items():
        destination = translated(position)
        assert destination not in occupied, (number, destination)
        occupied[destination] = number
        entity = copy.deepcopy(module.block_entities.get(position))
        if entity is not None:
            for axis, coordinate in zip(('x', 'y', 'z'), destination):
                if axis in entity.value:
                    entity.value[axis].value = coordinate
        if block_state.name.endswith('_door') and block_state.get('open') == 'false':
            properties = dict(block_state.properties)
            properties['open'] = 'true'
            door_changes.append({'module': number, 'local': position, 'position': destination, 'before': dict(block_state.properties), 'after': properties})
            block_state = state(block_state.name, **properties)
        result.set(destination, block_state, entity)
    for port in metadata['ports']:
        if (number, port['side']) not in used_ports:
            east, height, south = translated(port['feet'])
            cap_positions = []
            for across in range(-(port['width']//2), port['width']//2 + 1):
                for rise in range(port['clearHeight']):
                    position = (east + across, height + rise, south) if port['side'] in ('north', 'south') else (east, height + rise, south + across)
                    assert position not in result.block_entities
                    result.set(position, state('mossy_stone_bricks'))
                    cap_positions.append(position)
            caps.append({'module': number, 'side': port['side'], 'positions': cap_positions})
    route = metadata.get('body_route', metadata.get('route', []))
    for route_index, position in enumerate(route):
        targets.append({'label': f'S1-{number:02}-body-{route_index}', 'position': translated(position)})
    targets.append({'label': f'S1-{number:02}-interior', 'position': translated(metadata['interior'])})
    modules.append({'image': number, 'name': metadata.get('name', item['id']), 'path': str(path), 'sha256': item['sha256'], 'origin': origin, 'size': module.size, 'ports': metadata['ports'], 'source_body_targets': len(route)})

def expand(points):
    positions = [points[0]]
    for first, last in zip(points, points[1:]):
        assert first[0] == last[0] or first[1] == last[1]
        length = abs(first[0]-last[0]) + abs(first[1]-last[1])
        for step in range(1, length + 1):
            positions.append((first[0] + (last[0]-first[0])*step//length, first[1] + (last[1]-first[1])*step//length))
    return positions

corridor_reports = []
for label, first_port, last_port, points, feet in links:
    centers = expand(points)
    def port_world(key):
        module = next(module for module in modules if module['image'] == key[0])
        port = next(port for port in module['ports'] if port['side'] == key[1])
        return tuple(port['feet'][axis] + module['origin'][axis] for axis in range(3))
    assert (points[0][0], feet, points[0][1]) == port_world(first_port)
    assert (points[-1][0], feet, points[-1][1]) == port_world(last_port)
    outer = {(east+across,south+forward) for east,south in centers for across in range(-3,4) for forward in range(-3,4)}
    inner = {(east+across,south+forward) for east,south in centers for across in range(-2,3) for forward in range(-2,3)}
    for east, south in outer:
        if any((east, height, south) in occupied for height in range(size[1])):
            continue
        for height in range(feet-1):
            result.set((east,height,south), state('stone_bricks'))
        for height in range(feet-1, feet+6):
            if height == feet-1:
                block_state = state('mossy_stone_bricks' if (east+south)%4 else 'chiseled_stone_bricks')
            elif height == feet+5:
                block_state = state('dark_oak_log', axis='x')
            elif (east,south) in inner:
                block_state = AIR
            else:
                block_state = state('mossy_stone_bricks' if (east+height+south)%3 else 'cracked_stone_bricks')
            result.set((east,height,south), block_state)
    lamps = []
    for east, south in centers[4:-4:9]:
        if (east,feet+4,south) not in occupied:
            result.set((east,feet+4,south), state('lantern', hanging='true', waterlogged='false'))
            lamps.append((east,feet+4,south))
    targets.extend({'label':f'link-{label}-{target_index}', 'position':[east,feet,south]} for target_index,(east,south) in enumerate(centers))
    corridor_reports.append({'name': label, 'from': first_port, 'to': last_port, 'waypoints_xz': points, 'feet': feet, 'inner_width': 5, 'clear_height': 4, 'lanterns': lamps})

for east in range(size[0]):
    for south in range(size[2]):
        for height in range(size[1]):
            position = (east,height,south)
            if position not in result.blocks:
                result.set(position, AIR)
candidate = root / 'mossy-forest-connected.nbt'
result.write(candidate)
source_hash = hashlib.sha256(candidate.read_bytes()).hexdigest()
start = [75,2,172]
cap_cells = {tuple(position) for cap in caps for position in cap['positions']}
excluded_targets = [target for target in targets if tuple(target['position']) in cap_cells]
targets = [target for target in targets if tuple(target['position']) not in cap_cells]
(root/'capped-port-targets.json').write_text(json.dumps({'reason':'These former external-port standing cells are now deliberately solid caps, not interior destinations. All other module route targets are retained.', 'targets':excluded_targets},ensure_ascii=False,indent=2),encoding='utf-8')
metadata = {'name':'苔藓古林·九域连通地牢', 'size':size, 'sha256':source_hash, 'entrance':start, 'modules': modules, 'links':corridor_reports, 'unused_ports_capped':caps, 'entry_doors_opened':door_changes, 'scope':'New assembled artifact, not automatically inheriting module acceptance. Sources unmodified. Door states are open in this export; empty containers remain empty. Rotation zero; world ticks/physics/GUI/deployment NOT_RUN.'}
(root/'layout.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2),encoding='utf-8')
qa = root/'qa'
qa.mkdir(exist_ok=True)
spec = [{'group':1,'candidate':str(candidate),'sha256':source_hash,'size':size,'start':start,'targets':targets,'swimming':False}]
(qa/'route-input.json').write_text(json.dumps(spec,ensure_ascii=False,indent=2),encoding='utf-8')
tag_source = index_path.parent/'vanilla-block-tags.json'
(qa/'vanilla-block-tags.json').write_bytes(tag_source.read_bytes())
parts = []
combined = {}
for origin_east in range(0,size[0],48):
    for origin_south in range(0,size[2],48):
        origin = (origin_east,0,origin_south)
        part_size = (min(48,size[0]-origin_east),size[1],min(48,size[2]-origin_south))
        part = Structure(part_size)
        for east in range(part_size[0]):
            for height in range(part_size[1]):
                for south in range(part_size[2]):
                    local=(east,height,south)
                    position=(east+origin_east,height,south+origin_south)
                    entity=copy.deepcopy(result.block_entities.get(position))
                    if entity is not None:
                        for axis,value in zip(('x','y','z'),local):
                            if axis in entity.value:
                                entity.value[axis].value=value
                    part.set(local,result.blocks[position],entity)
                    assert position not in combined
                    combined[position]=result.blocks[position]
        part_path = root/'parts'/f'part-{len(parts)+1:02}.nbt'
        part.write(part_path)
        loaded=Structure.read(part_path)
        assert loaded.blocks==part.blocks and {key:plain(value) for key,value in loaded.block_entities.items()}=={key:plain(value) for key,value in part.block_entities.items()}
        parts.append({'order':len(parts)+1,'path':str(part_path.relative_to(root)),'origin':origin,'size':part_size,'sha256':hashlib.sha256(part_path.read_bytes()).hexdigest(),'explicit_records':len(part.blocks),'block_entities':len(part.block_entities)})
assert combined == result.blocks
(root/'assembly.json').write_text(json.dumps({'source_sha256':source_hash,'parts':parts,'policy':'All explicit cells including AIR overwrite existing terrain. Fixed common unrotated source origin. Do not apply parts with independent rotations. Runtime assembly not tested.'},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'sha256':source_hash,'size':size,'body_targets':len(targets),'modules':len(modules),'links':len(links),'parts':len(parts),'entities':len(result.block_entities),'door_halves_opened':len(door_changes),'materials':Counter(value.name for value in result.blocks.values())},ensure_ascii=False))
