import collections
import copy
import hashlib
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).parent
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, Tag, encode_payload, intlist, state

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def translated_entity(entity, offset):
    result = copy.deepcopy(entity)
    for field in ['pos', 'blockPos']:
        for axis, amount in enumerate(offset):
            result.value[field].value[axis].value += amount
    if 'Pos' in result.value['nbt'].value:
        for axis, amount in enumerate(offset):
            result.value['nbt'].value['Pos'].value[axis].value += amount
    return result

def payload(tag):
    return encode_payload(tag)

def main():
    output = ROOT / 'merged'
    output.mkdir(exist_ok=True)
    rooms = []
    for index, folder in enumerate(sorted(ROOT.glob('[12][0-9]-*'))):
        design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
        candidate = next(folder.glob('*.nbt'))
        checks = json.loads((folder / 'self-check.json').read_text(encoding='utf-8'))
        assert checks['source_sha256'] == sha(candidate), folder
        support = json.loads((folder / 'qa' / 'independent-all-support.json').read_text(encoding='utf-8'))[0]
        assert support['sha256'] == sha(candidate) and not support['failures'] and not support['unsupported']
        offset = (10 + index * 48, 6 - design['start'][1], 14)
        rooms.append({'folder':folder, 'source':Structure.read(candidate), 'candidate':candidate, 'design':design, 'offset':offset})
    assert len(rooms) == 10
    combined = Structure((492, 34, 74))
    protected = set()
    for room in rooms:
        east, up, south = room['offset']
        for local_east in range(room['source'].size[0]):
            for local_south in range(room['source'].size[2]):
                protected.add((east + local_east, south + local_south))
    surfaces = {}
    centerlines = []
    def point(east, feet, south):
        centerlines.append((east,feet,south))
        for delta_east in range(-2,3):
            for delta_south in range(-2,3):
                position = (east+delta_east, south+delta_south)
                if position not in protected:
                    old = surfaces.get(position)
                    if old is None or feet > old:
                        surfaces[position] = feet
    def segment(start, end):
        assert start[1] == end[1]
        if start[0] == end[0]:
            for south in range(min(start[2],end[2]), max(start[2],end[2])+1):
                point(start[0],start[1],south)
        else:
            assert start[2] == end[2]
            for east in range(min(start[0],end[0]),max(start[0],end[0])+1):
                point(east,start[1],start[2])
    segment((5,6,65),(485,6,65))
    for room in rooms:
        east, up, south = room['offset']
        width, height, depth = room['source'].size
        for port in room['design']['ports']:
            local_east, local_up, local_south = port['feet']
            start = (east+local_east, up+local_up, south+local_south)
            if port['side'] in ['west','east']:
                gap = east-5 if port['side']=='west' else east+width+4
                segment(start,(gap,start[1],start[2]))
                segment((gap,6,start[2]),(gap,6,65))
            elif port['side']=='south':
                segment(start,(start[0],6,65))
            else:
                segment(start,(start[0],start[1],8))
                segment((start[0],start[1],8),(east-5,start[1],8))
                if start[1] == 6:
                    segment((east-5,6,8),(east-5,6,65))
                else:
                    assert start[1] == 14
                    for south_step in range(8,66):
                        feet = max(6,14-max(0,south_step-9)*0.5)
                        for delta_east in range(-2,3):
                            position=(east-5+delta_east,south_step)
                            if position not in protected:
                                surfaces[position]=feet
                    segment((east-5,6,30),(east-5,6,65))
    stone = state('mossy_stone_bricks')
    air = state('air')
    for (east,south),feet in surfaces.items():
        roof = int(feet)+5
        for delta_east,delta_south in [(0,0),(1,0),(-1,0),(0,1),(0,-1)]:
            wall_east,wall_south=east+delta_east,south+delta_south
            if (wall_east,wall_south) in protected or (wall_east,wall_south) in surfaces:
                continue
            for up in range(0,roof+1):
                combined.set((wall_east,up,wall_south),stone)
        for up in range(0,int(feet)):
            combined.set((east,up,south),stone)
        if feet % 1:
            combined.set((east,int(feet),south),state('stone_brick_slab',type='bottom',waterlogged=False))
        combined.set((east,roof,south),state('dark_oak_log',axis='x'))
    for (east,south),feet in surfaces.items():
        for up in range(int(feet)+(1 if feet%1 else 0),int(feet)+5):
            combined.set((east,up,south),air)
        if east%12==0 and south==65:
            combined.set((east,int(feet)+4,south),state('lantern',hanging=True,waterlogged=False))
    for room in rooms:
        offset=room['offset']
        for position,block in room['source'].blocks.items():
            target=tuple(coordinate+delta for coordinate,delta in zip(position,offset))
            combined.set(target,block,copy.deepcopy(room['source'].block_entities.get(position)))
        combined.entities.extend(translated_entity(entity,offset) for entity in room['source'].entities)
    barrier=state('barrier')
    for up in range(combined.size[1]):
        for east in range(combined.size[0]):
            for south in [0,combined.size[2]-1]:
                combined.set((east,up,south),barrier)
        for south in range(combined.size[2]):
            for east in [0,combined.size[0]-1]:
                combined.set((east,up,south),barrier)
    for east in range(combined.size[0]):
        for south in range(combined.size[2]):
            for up in [0,combined.size[1]-1]:
                combined.set((east,up,south),barrier)
    candidate=output/'s1-mossy-functional-11-20.nbt'
    combined.write(candidate)
    reread=Structure.read(candidate)
    integrity=[]
    targets=[]
    for room in rooms:
        offset=room['offset']
        for position,block in room['source'].blocks.items():
            target=tuple(coordinate+delta for coordinate,delta in zip(position,offset))
            assert reread.get(target)==block,(room['folder'].name,position)
            source_tag=room['source'].block_entities.get(position)
            target_tag=reread.block_entities.get(target)
            assert (payload(source_tag) if source_tag else None)==(payload(target_tag) if target_tag else None)
        for target in room['design']['targets']:
            targets.append({'label':room['design']['id']+'/'+target['label'],'position':[coordinate+delta for coordinate,delta in zip(target['position'],offset)]})
        for port in room['design']['ports']:
            position=[coordinate+delta for coordinate,delta in zip(port['feet'],offset)]
            targets.append({'label':room['design']['id']+'/'+port['side']+'-port','position':position})
        integrity.append({'id':room['design']['id'],'candidate':str(room['candidate']),'sha256':sha(room['candidate']),'offset':offset,'records_verified':len(room['source'].blocks),'block_entities_verified':len(room['source'].block_entities),'entities':len(room['source'].entities),'exact_source_preserved':True})
    expected_entities=[translated_entity(entity,room['offset']) for room in rooms for entity in room['source'].entities]
    assert collections.Counter(payload(entity) for entity in reread.entities)==collections.Counter(payload(entity) for entity in expected_entities)
    chunks=output/'chunks'
    chunks.mkdir(exist_ok=True)
    pieces={}
    for position,block in reread.blocks.items():
        key=tuple(coordinate//48 for coordinate in position)
        origin=tuple(coordinate*48 for coordinate in key)
        if key not in pieces:
            pieces[key]=Structure(tuple(min(48,limit-start) for limit,start in zip(reread.size,origin)))
        local=tuple(coordinate-start for coordinate,start in zip(position,origin))
        pieces[key].set(local,block,copy.deepcopy(reread.block_entities.get(position)))
    for entity in reread.entities:
        position=[tag.value for tag in entity.value['pos'].value]
        key=tuple(int(coordinate)//48 for coordinate in position)
        pieces[key].entities.append(translated_entity(entity,tuple(-48*coordinate for coordinate in key)))
    rebuilt=Structure(reread.size)
    chunk_index=[]
    for key,piece in sorted(pieces.items()):
        path=chunks/('s1-'+ '-'.join(map(str,key))+'.nbt')
        piece.write(path)
        loaded=Structure.read(path)
        origin=tuple(coordinate*48 for coordinate in key)
        assert max(loaded.size)<=48
        for position,block in loaded.blocks.items():
            target=tuple(coordinate+start for coordinate,start in zip(position,origin))
            assert target not in rebuilt.blocks
            rebuilt.set(target,block,copy.deepcopy(loaded.block_entities.get(position)))
        rebuilt.entities.extend(translated_entity(entity,origin) for entity in loaded.entities)
        chunk_index.append({'file':str(path),'origin':origin,'size':loaded.size,'sha256':sha(path),'records':len(loaded.blocks),'entities':len(loaded.entities)})
    assert rebuilt.blocks==reread.blocks
    assert {position:payload(tag) for position,tag in rebuilt.block_entities.items()}=={position:payload(tag) for position,tag in reread.block_entities.items()}
    assert collections.Counter(payload(entity) for entity in rebuilt.entities)==collections.Counter(payload(entity) for entity in reread.entities)
    save(output/'integrity.json',{'source_sha256':sha(candidate),'rooms':integrity,'chunk_count':len(pieces),'chunk_max_dimension':48,'reassembled_records':len(rebuilt.blocks),'all_block_states_equal':True,'all_block_entity_payloads_equal':True,'all_entity_payloads_equal':True,'outer_barrier_closed_six_faces':True,'sources_not_cut_by_barrier':True,'chunks':chunk_index})
    save(output/'design.json',{'id':'S1-merged','size':reread.size,'start':[5,6,65],'targets':targets,'ports':[],'cutaway':{'removeAbove':17,'frontDepth':73,'sideX':491},'focus_views':[{'id':'connector-watch','position':[408,32,75],'target':[397,11,34],'fov':65}]})
    print('Merged ten final sources:',sha(candidate),'chunks:',len(pieces),'records:',len(rebuilt.blocks))

if __name__=='__main__':
    main()
