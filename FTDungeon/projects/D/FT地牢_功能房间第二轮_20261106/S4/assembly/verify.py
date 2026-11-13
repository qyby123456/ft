import copy
import hashlib
import json
import sys
from pathlib import Path
sys.dont_write_bytecode = True
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, plain, AIR
ROOT = Path(__file__).resolve().parent
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    layout = json.loads((ROOT / 'layout.json').read_text(encoding='utf-8'))
    source_path = ROOT / 's4-wetland-connected.nbt'
    assert digest(source_path) == layout['sha256']
    source = Structure.read(source_path)
    checked = 0
    entities = 0
    for module in layout['modules']:
        path = Path(module['source'])
        assert digest(path) == module['sha256']
        room = Structure.read(path)
        for east in range(room.size[0]):
            for height in range(room.size[1]):
                for south in range(room.size[2]):
                    local = (east,height,south)
                    position = tuple(value+shift for value,shift in zip(local,module['origin']))
                    assert source.get(position) == room.get(local), (module['id'],local)
                    assert plain(source.block_entities[position]) == plain(room.block_entities[local]) if local in room.block_entities else position not in source.block_entities
                    checked += 1
        entities += len(room.entities)
    assert len(source.entities) == entities == 2
    manifest = json.loads((ROOT / 'parts.json').read_text(encoding='utf-8'))
    covered = set()
    checked_entities = []
    for item in manifest['parts']:
        path = Path(item['path'])
        assert digest(path) == item['sha256']
        part = Structure.read(path)
        assert max(part.size) <= 48
        for local, block in part.blocks.items():
            position = tuple(value+shift for value,shift in zip(local,item['origin']))
            assert position not in covered
            covered.add(position)
            assert source.get(position) == block, position
            assert plain(source.block_entities[position]) == plain(part.block_entities[local]) if local in part.block_entities else position not in source.block_entities
        for entity in part.entities:
            converted = copy.deepcopy(entity)
            for name in ('pos','blockPos'):
                for axis,shift in enumerate(item['origin']):
                    converted.value[name].value[axis].value += shift
            for axis,shift in enumerate(item['origin']):
                converted.value['nbt'].value['Pos'].value[axis].value += shift
            checked_entities.append(plain(converted))
    assert len(covered) == source.size[0]*source.size[1]*source.size[2]
    assert sorted(map(str,checked_entities)) == sorted(str(plain(entity)) for entity in source.entities)
    envelope = set()
    for module in layout['modules']:
        east,height,south = module['origin']
        envelope.update((east+local_east,height+local_height,south+local_south) for local_east in range(module['size'][0]) for local_height in range(module['size'][1]) for local_south in range(module['size'][2]))
    for link in layout['links']:
        for column in link['columns']:
            envelope.update((column['east'],height,south) for height in range(1,int(max(link['from_feet'][1],link['to_feet'][1]))+7) for south in range(26,31))
    boundary = 0
    for position in envelope:
        for direction in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = tuple(value+delta for value,delta in zip(position,direction))
            if adjacent not in envelope:
                assert source.get(adjacent).name == 'minecraft:barrier', adjacent
                boundary += 1
    for position in envelope:
        assert source.get(position).name != 'minecraft:barrier', position
    result = dict(sha256=layout['sha256'],status='PASS',module_cells_compared=checked,partition_cells_compared=len(covered),block_entity_payloads_compared=len(source.block_entities),entities_compared=entities,boundary_faces=boundary,barriers_inside_union=0,scope='Every source-room cell unchanged in assembly; every full-source cell and entity payload compared with <=48 parts. Union boundary checked; routes separately native-tested.')
    (ROOT / 'qa/assembly-check.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result))
if __name__ == '__main__':
    main()
