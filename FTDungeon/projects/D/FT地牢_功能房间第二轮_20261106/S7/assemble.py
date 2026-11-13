import copy
import hashlib
import json
import math
import sys
from pathlib import Path

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parent))
from qa import ROOT, Structure, plain, save
from nbtio import AIR, state

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    output = ROOT / 'assembly'
    output.mkdir(exist_ok=True)
    (output / 'parts').mkdir(exist_ok=True)
    modules = []
    sources = {}
    for number in range(11,21):
        room = ROOT / f'S7-{number}'
        summary = json.loads((room / 'summary.json').read_text())
        candidate = Path(summary['nbtPath'])
        check = json.loads((room / 'qa/self-check.json').read_text())
        assert check['status'] == 'SELF_CHECK_PASS' and check['sha256'] == digest(candidate)
        design = json.loads((room / 'design.json').read_text(encoding='utf-8'))
        source = Structure.read(candidate)
        sources[number] = source
        column = number-11 if number<=15 else 20-number
        origin = [12+column*62, 8 if number in [16,17,18] else 2, 12 if number<=15 else 98]
        modules.append({'id': f'S7-{number}', 'number': number, 'origin': origin, 'size': source.size, 'candidate': str(candidate), 'sha256': digest(candidate), 'ports': design['ports'], 'targets': design['targets']})
    size = (326,40,151)
    result = Structure(size)
    occupied = set()
    envelope = set()
    def world(local, origin):
        return tuple(value+shift for value,shift in zip(local,origin))
    for module in modules:
        origin = module['origin']
        source = sources[module['number']]
        for east in range(source.size[0]):
            for up in range(source.size[1]):
                for south in range(source.size[2]):
                    position = world((east,up,south),origin)
                    assert position not in occupied
                    occupied.add(position)
                    envelope.add(position)
        for local, block in source.blocks.items():
            position = world(local,origin)
            result.set(position, block, copy.deepcopy(source.block_entities.get(local)))
        from nbtio import Tag
        for original_entity in source.entities:
            entity=copy.deepcopy(original_entity)
            for name in ['pos','blockPos']:
                for axis in range(3):
                    entity.value[name].value[axis].value+=origin[axis]
            for axis in range(3):
                entity.value['nbt'].value['Pos'].value[axis].value+=origin[axis]
            result.entities.append(entity)
    links = []
    corridor_air = set()
    targets = []
    for module in modules:
        for target in module['targets']:
            targets.append({'label': module['id']+' '+target['label'], 'position': world(target['position'],module['origin'])})
    for index in range(9):
        first, last = modules[index], modules[index+1]
        first_side = 'east' if index<5 else 'west'
        last_side = 'west' if index<4 else 'east'
        begin = world(next(port['foot'] for port in first['ports'] if port['side']==first_side),first['origin'])
        end = world(next(port['foot'] for port in last['ports'] if port['side']==last_side),last['origin'])
        if index==4:
            waypoints = [(begin[0],begin[2]),(310,begin[2]),(310,end[2]),(end[0],end[2])]
        else:
            middle = (begin[0]+end[0])//2
            waypoints = [(begin[0],begin[2]),(middle,begin[2]),(middle,end[2]),(end[0],end[2])]
        path = []
        for start,finish in zip(waypoints,waypoints[1:]):
            distance = abs(start[0]-finish[0])+abs(start[1]-finish[1])
            for step in range(distance+1):
                point = (start[0]+(step if finish[0]>start[0] else -step) if finish[0]!=start[0] else start[0], start[1]+(step if finish[1]>start[1] else -step) if finish[1]!=start[1] else start[1])
                if not path or point!=path[-1]:
                    path.append(point)
        inside = {}
        shell = {}
        for step, (east,south) in enumerate(path):
            fraction = step/max(1,len(path)-1)
            foot = begin[1]+round((end[1]-begin[1])*2*fraction)/2
            for dx in range(-3,4):
                for dz in range(-3,4):
                    horizontal=(east+dx,south+dz)
                    distance=abs(dx)+abs(dz)
                    destination=inside if abs(dx)<=2 and abs(dz)<=2 else shell
                    if horizontal not in destination or distance<destination[horizontal][0]:
                        destination[horizontal]=(distance,foot)
        for (east,south),(_,foot) in {**shell,**inside}.items():
            bottom=math.floor(foot)-1
            top=math.ceil(foot)+4
            for up in range(bottom,top+1):
                position=(east,up,south)
                if position in occupied:
                    continue
                envelope.add(position)
                if (east,south) not in inside:
                    result.set(position,state('purple_terracotta'))
                elif up==bottom:
                    result.set(position,state('dark_oak_planks'))
                elif foot%1 and up==math.floor(foot):
                    result.set(position,state('dark_oak_slab',type='bottom',waterlogged=False))
                elif up==top:
                    result.set(position,state('rooted_dirt'))
                else:
                    result.set(position,AIR)
                    corridor_air.add(position)
        links.append({'from': first['id'], 'to': last['id'], 'start': begin, 'end': end, 'waypoints_xz': waypoints, 'centerline_xz': path, 'clear_width_min': 3, 'clear_height_min': 4, 'half_step_max': 0.5})
        for step in [len(path)//4,len(path)//2,3*len(path)//4]:
            east,south=path[step]
            foot=begin[1]+round((end[1]-begin[1])*2*step/max(1,len(path)-1))/2
            targets.append({'label': f'link-{index+1}-sample-{step}', 'position': [east,foot,south]})
    barriers = set()
    for east,up,south in envelope:
        for dx,dy,dz in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            position=(east+dx,up+dy,south+dz)
            if position not in envelope:
                assert all(0<=value<size[axis] for axis,value in enumerate(position))
                barriers.add(position)
    for position in barriers:
        result.set(position,state('barrier'))
    source_integrity=[]
    for module in modules:
        source=sources[module['number']]
        for local,block in source.blocks.items():
            position=world(local,module['origin'])
            assert result.get(position)==block
            expected=source.block_entities.get(local)
            actual=result.block_entities.get(position)
            assert (plain(expected) if expected else None)==(plain(actual) if actual else None)
        source_integrity.append({'id':module['id'],'sha256':module['sha256'],'block_records_compared':len(source.blocks),'block_entities_compared':len(source.block_entities),'status':'EXACT_TRANSLATION'})
    candidate=output/'s7-fungal-functions-connected.nbt'
    result.write(candidate)
    reread=Structure.read(candidate)
    assert reread.blocks==result.blocks
    parts=[]
    restored={}
    restored_entities={}
    for east in range(0,size[0],48):
        for up in range(0,size[1],48):
            for south in range(0,size[2],48):
                origin=(east,up,south)
                part_size=tuple(min(48,size[axis]-origin[axis]) for axis in range(3))
                part=Structure(part_size)
                for position,block in result.blocks.items():
                    local=tuple(position[axis]-origin[axis] for axis in range(3))
                    if all(0<=local[axis]<part_size[axis] for axis in range(3)):
                        part.set(local,block,copy.deepcopy(result.block_entities.get(position)))
                if not part.blocks:
                    continue
                for original_entity in result.entities:
                    coordinates=plain(original_entity.value['pos'])
                    if all(origin[axis]<=coordinates[axis]<origin[axis]+part_size[axis] for axis in range(3)):
                        entity=copy.deepcopy(original_entity)
                        for name in ['pos','blockPos']:
                            for axis in range(3):
                                entity.value[name].value[axis].value-=origin[axis]
                        for axis in range(3):
                            entity.value['nbt'].value['Pos'].value[axis].value-=origin[axis]
                        part.entities.append(entity)
                path=output/'parts'/f'part-{east}-{up}-{south}.nbt'
                part.write(path)
                loaded=Structure.read(path)
                for local,block in loaded.blocks.items():
                    position=world(local,origin)
                    assert position not in restored
                    restored[position]=block
                    if local in loaded.block_entities:
                        restored_entities[position]=plain(loaded.block_entities[local])
                parts.append({'path':str(path),'sha256':digest(path),'origin':origin,'size':part_size,'records':len(part.blocks)})
    assert restored==result.blocks
    assert restored_entities=={position:plain(entity) for position,entity in result.block_entities.items()}
    assembly={'candidate':str(candidate),'sha256':digest(candidate),'size':size,'modules':modules,'links':links,'barrier_count':len(barriers),'barrier_shell':'six-neighbor exterior of module and corridor union; never inserted inside union','source_integrity':source_integrity,'parts':parts,'partition_comparison':{'status':'EXACT','block_records':len(restored),'block_entities':len(restored_entities)},'exterior_ports':'Both terminal room ports are enclosed by exterior barriers for offline review; no live-world deployment entrance provided.'}
    save(output/'assembly.json',assembly)
    save(output/'summary.json',{'size':size,'nbtPath':str(candidate)})
    start=world([2,3,16],modules[0]['origin'])
    save(output/'design.json',{'id':'S7-assembly','start':start,'targets':targets,'ports':[],'differences':['Two-row folded chain; S7-16 through 18 raised six blocks; actual S7-19 upper/lower ports linked by graded half-step corridor','Complete ten sources preserved cell-for-cell; terminal exterior ports barrier-capped for offline review']})
    print(json.dumps({'candidate':str(candidate),'sha256':digest(candidate),'records':len(result.blocks),'parts':len(parts),'targets':len(targets)}))

if __name__=='__main__':
    main()
