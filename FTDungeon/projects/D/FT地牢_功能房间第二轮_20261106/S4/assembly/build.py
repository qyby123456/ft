import copy
import hashlib
import json
import sys
from pathlib import Path
sys.dont_write_bytecode = True
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import AIR, Structure, state, plain, Tag
ROOT = Path(__file__).resolve().parent
GROUP = ROOT.parent
def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    records = []
    offset = 3
    for folder in sorted(GROUP.glob('[12][0-9]-*')):
        if not folder.is_dir():
            continue
        review = json.loads((folder / 'qa/self-review.json').read_text(encoding='utf-8'))
        assert review['author_self_check'] == 'PASS', folder
        source_path = next(folder.glob('*.nbt'))
        assert digest(source_path) == review['sha256']
        source = Structure.read(source_path)
        inventory = json.loads((folder / 'qa/inventory.json').read_text(encoding='utf-8'))
        ports = inventory['metadata']['ports']
        origin = (offset, 1, 28 - int(ports[0]['feet'][2]))
        records.append(dict(id=review['id'], folder=folder.name, source=source, path=source_path, sha256=review['sha256'], origin=origin, ports=ports, metadata=inventory['metadata']))
        offset += source.size[0] + 13
    assert len(records) == 10
    size = (offset - 10, 24, 53)
    result = Structure(size)
    envelope = set()
    occupied = set()
    targets = []
    def translated(position, origin):
        return tuple(value + shift for value, shift in zip(position, origin))
    for record in records:
        source, origin = record['source'], record['origin']
        for east in range(source.size[0]):
            for height in range(source.size[1]):
                for south in range(source.size[2]):
                    local = (east, height, south)
                    position = translated(local, origin)
                    assert position not in occupied
                    occupied.add(position)
                    envelope.add(position)
                    result.set(position, source.get(local), copy.deepcopy(source.block_entities.get(local)))
        for entity in source.entities:
            converted = copy.deepcopy(entity)
            for name in ('pos', 'blockPos'):
                for axis, shift in enumerate(origin):
                    converted.value[name].value[axis].value += shift
            data = converted.value['nbt'].value
            if 'Pos' in data:
                for axis, shift in enumerate(origin):
                    data['Pos'].value[axis].value += shift
            result.entities.append(converted)
        targets.extend(dict(label=f"{record['id']} {target['label']}", position=translated(target['position'], origin)) for target in record['metadata']['targets'])
    links = []
    for first, second in zip(records, records[1:]):
        source_port = translated(first['ports'][1]['feet'], first['origin'])
        target_port = translated(second['ports'][0]['feet'], second['origin'])
        start, end = int(source_port[0]), int(target_port[0])
        middle = 28
        first_feet, last_feet = source_port[1], target_port[1]
        assert source_port[2] == target_port[2] == 28.5
        columns = []
        for east in range(start, end + 1):
            distance = east - start
            feet = first_feet + (1 if last_feet >= first_feet else -1) * min(abs(last_feet - first_feet), distance * 0.5)
            columns.append(dict(east=east, feet=feet))
            for south in range(middle - 2, middle + 3):
                for height in range(1, int(max(first_feet, last_feet)) + 7):
                    position = (east, height, south)
                    envelope.add(position)
                    if position in occupied:
                        continue
                    if height < int(feet) - 1:
                        block = state('mud_bricks')
                    elif height == int(feet) - 1 and feet == int(feet):
                        block = state('dark_oak_planks')
                    elif height == int(feet) and feet != int(feet):
                        block = state('dark_oak_slab', type='bottom', waterlogged='false')
                    elif height < feet:
                        block = state('dark_oak_planks')
                    elif height >= int(max(first_feet, last_feet)) + 5:
                        block = state('dark_oak_planks')
                    elif abs(south - middle) == 2:
                        block = state('dark_oak_log', axis='y') if east % 5 == 0 else state('green_stained_glass')
                    else:
                        block = AIR
                    result.set(position, block)
        links.append(dict(first=first['id'], second=second['id'], from_feet=source_port, to_feet=target_port, columns=columns, width=3, minimum_clear_height=4))
    neighbor_axes = ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))
    boundary = set()
    for position in envelope:
        for direction in neighbor_axes:
            adjacent = tuple(value + delta for value, delta in zip(position, direction))
            if adjacent not in envelope:
                assert all(0 <= value < limit for value, limit in zip(adjacent, size)), adjacent
                boundary.add(adjacent)
    for position in boundary:
        result.set(position, state('barrier'))
    for east in range(size[0]):
        for height in range(size[1]):
            for south in range(size[2]):
                position = (east, height, south)
                if position not in result.blocks:
                    result.set(position, AIR)
    path = ROOT / 's4-wetland-connected.nbt'
    result.write(path)
    sha256 = digest(path)
    modules = [dict(id=record['id'], folder=record['folder'], source=str(record['path']), sha256=record['sha256'], origin=record['origin'], size=record['source'].size, ports=record['ports']) for record in records]
    start = translated(records[0]['metadata']['start'], records[0]['origin'])
    save(ROOT / 'layout.json', dict(sha256=sha256,size=size,modules=modules,links=links,start=start,targets=targets,boundary_cells=len(boundary),exterior='Barrier on union boundary only; no barrier on module/connector shared faces; full air explicitly stored.'))
    parts = []
    for east in range(0,size[0],48):
        for south in range(0,size[2],48):
            origin = (east,0,south)
            part = Structure((min(48,size[0]-east),size[1],min(48,size[2]-south)))
            for local_east in range(part.size[0]):
                for height in range(part.size[1]):
                    for local_south in range(part.size[2]):
                        local = (local_east,height,local_south)
                        absolute = translated(local,origin)
                        part.set(local,result.get(absolute),copy.deepcopy(result.block_entities.get(absolute)))
            for entity in result.entities:
                position = plain(entity.value['pos'])
                if all(shift <= value < shift + limit for value,shift,limit in zip(position,origin,part.size)):
                    converted = copy.deepcopy(entity)
                    for name in ('pos','blockPos'):
                        for axis,shift in enumerate(origin):
                            converted.value[name].value[axis].value -= shift
                    for axis,shift in enumerate(origin):
                        converted.value['nbt'].value['Pos'].value[axis].value -= shift
                    part.entities.append(converted)
            filename = ROOT / 'parts' / f'part-{len(parts)+1:02}.nbt'
            part.write(filename)
            parts.append(dict(path=str(filename),origin=origin,size=part.size,sha256=digest(filename)))
    save(ROOT / 'parts.json',dict(sha256=sha256,parts=parts))
    qa = ROOT / 'qa'
    native = lambda position:[position[0]-0.5,position[1],position[2]-0.5]
    save(qa / 'route-input.json',[dict(group=4,candidate=str(path),sha256=sha256,start=native(start),targets=[dict(label=target['label'],position=native(target['position'])) for target in targets],swimming=False)])
    (qa / 'vanilla-block-tags.json').write_bytes((GROUP / '11-library/qa/vanilla-block-tags.json').read_bytes())
    print(json.dumps(dict(size=size,sha256=sha256,rooms=10,links=9,targets=len(targets),parts=len(parts),entities=len(result.entities))))
if __name__ == '__main__':
    main()
