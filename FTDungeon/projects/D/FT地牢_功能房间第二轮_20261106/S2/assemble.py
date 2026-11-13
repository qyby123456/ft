import hashlib
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure, State, state, plain


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def rotated(position, size, turns):
    east, up, south = position
    width, height, depth = size
    for turn in range(turns):
        east, south = depth-1-south, east
        width, depth = depth, width
    return east, up, south


def rotate_state(block, turns):
    directions = ['north','east','south','west']
    properties = dict(block.properties)
    for turn in range(turns):
        updated = {}
        for key, value in properties.items():
            if key in directions:
                key = directions[(directions.index(key)+1)%4]
            if key == 'facing' and value in directions:
                value = directions[(directions.index(value)+1)%4]
            if key == 'axis' and value in ('x','z'):
                value = 'z' if value == 'x' else 'x'
            if key == 'rotation':
                value = str((int(value)+4)%16)
            updated[key] = value
        properties = updated
    return State(block.name, tuple(sorted(properties.items())))


def main():
    output = ROOT / 'assembly'
    output.mkdir(exist_ok=True)
    (output / 'qa').mkdir(exist_ok=True)
    (output / 'parts').mkdir(exist_ok=True)
    placements = [
        ('11-library',[8,6,12],0),('12-prison-block',[48,6,10],1),('13-kitchen-mess',[99,6,15],0),('14-armory',[143,6,11],0),('15-training-yard',[185,6,14],0),
        ('16-well-cistern',[185,6,81],2),('17-workshop',[143,6,79],2),('18-crypt-ossuary',[99,6,83],2),('19-watch-balcony',[48,0,77],2),('20-secret-vault',[8,0,80],2)
    ]
    placements = [(name,[origin[0],origin[1]+1,origin[2]],turns) for name,origin,turns in placements]
    merged = Structure((239,25,126))
    envelope = set()
    modules = []
    targets = []
    for name, origin, turns in placements:
        folder = ROOT / name
        design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
        candidate = folder / design['nbt']
        review = json.loads((folder / 'qa/visual-review.json').read_text(encoding='utf-8'))
        assert review['source_sha256'] == digest(candidate)
        source = Structure.read(candidate)
        dimensions = source.size if turns%2 == 0 else (source.size[2],source.size[1],source.size[0])
        def translate(position):
            return tuple(value+shift for value,shift in zip(rotated(position,source.size,turns),origin))
        for position, block in source.blocks.items():
            destination = translate(position)
            assert destination not in merged.blocks, destination
            merged.set(destination,rotate_state(block,turns),source.block_entities.get(position))
            envelope.add(destination)
        ports = []
        for port in design['ports']:
            facing = ['north','east','south','west']
            ports.append({**port,'side':facing[(facing.index(port['side'])+turns)%4],'feet':translate(port['feet'])})
        for target in design['targets']:
            targets.append({'label':design['id']+'/'+target['label'],'position':translate(target['position'])})
        modules.append({'id':design['id'],'folder':str(folder),'candidate':str(candidate),'source_sha256':digest(candidate),'origin':origin,'rotation_clockwise':turns*90,'size':dimensions,'source_size':source.size,'ports':ports})
    ways = [
        [[3,8,21],[8,8,21]],
        [[32,8,30],[40,8,30],[40,8,23],[48,8,23]],
        [[82,8,23],[99,8,23]],
        [[127,8,23],[135,8,23],[135,8,18],[143,8,18]],
        [[169,8,18],[177,8,18],[177,8,22],[185,8,22]],
        [[215,8,22],[230,8,22],[230,8,98],[209,8,98]],
        [[185,8,99],[176,8,99],[176,8,96],[167,8,96]],
        [[143,8,96],[134,8,96],[134,8,100],[125,8,100]],
        [[99,8,100],[90,8,100],[90,8,116],[62,8,116],[62,8,105]],
        [[48,2,82],[40,2,82],[40,2,91],[32,2,91]],
        [[8,2,91],[3,2,91]]
    ]
    ways = [[[point[0],point[1]+1,point[2]] for point in waypoints] for waypoints in ways]
    corridors = []
    module_cells = set(envelope)
    for index, waypoints in enumerate(ways):
        feet = waypoints[0][1]
        centerline = []
        for first,last in zip(waypoints,waypoints[1:]):
            distance = abs(last[0]-first[0])+abs(last[2]-first[2])
            assert first[1] == last[1] and (first[0] == last[0] or first[2] == last[2])
            centerline.extend((first[0]+(last[0]-first[0])*step//distance, first[2]+(last[2]-first[2])*step//distance) for step in range(distance+1))
        outer = {(east+offset_east,south+offset_south) for east,south in centerline for offset_east in range(-2,3) for offset_south in range(-2,3)}
        inner = {(east+offset_east,south+offset_south) for east,south in centerline for offset_east in range(-1,2) for offset_south in range(-1,2)}
        for east,south in outer:
            for up in range(feet-2,feet+6):
                position = east,up,south
                if position in module_cells:
                    continue
                envelope.add(position)
                material = state('smooth_sandstone') if up < feet else state('orange_terracotta') if up == feet+3 else state('cut_sandstone')
                if (east,south) in inner and feet<=up<feet+5:
                    material = AIR
                merged.set(position,material)
        corridors.append({'id':index,'waypoints':waypoints,'feet':feet,'width':3,'clearHeight':5,'outer_footprint':len(outer)})
        targets.extend({'label':f'corridor-{index}-bend-{step}','position':point} for step,point in enumerate(waypoints[1:-1]))
    for module in modules:
        for port in module['ports']:
            east,feet,south=port['feet']
            for across in range(-1,2):
                for rise in range(4):
                    position=(east+across,feet+rise,south) if port['side'] in ('north','south') else (east,feet+rise,south+across)
                    assert merged.get(position).name=='minecraft:air',position
    boundary = set()
    for east,up,south in envelope:
        for offset_east,offset_up,offset_south in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = east+offset_east,up+offset_up,south+offset_south
            if adjacent not in envelope:
                boundary.add(adjacent)
    for position in boundary:
        merged.set(position,state('barrier'))
    candidate = output / 's2-desert-functional-combined.nbt'
    merged.write(candidate)
    sha256 = digest(candidate)
    restored = Structure.read(candidate)
    assert restored.blocks == merged.blocks
    assert {position:plain(payload) for position,payload in restored.block_entities.items()} == {position:plain(payload) for position,payload in merged.block_entities.items()}
    source_matches = []
    for module in modules:
        source = Structure.read(module['candidate'])
        for position,block in source.blocks.items():
            destination=tuple(value+shift for value,shift in zip(rotated(position,source.size,module['rotation_clockwise']//90),module['origin']))
            assert restored.get(destination)==rotate_state(block,module['rotation_clockwise']//90)
            assert plain(restored.block_entities[destination])==plain(source.block_entities[position]) if position in source.block_entities else destination not in restored.block_entities
        source_matches.append({'id':module['id'],'source_sha256':module['source_sha256'],'matched_blocks':len(source.blocks),'matched_block_entities':len(source.block_entities)})
    parts = []
    total = 0
    for east in range(0,merged.size[0],48):
        for south in range(0,merged.size[2],48):
            dimensions=min(48,merged.size[0]-east),merged.size[1],min(48,merged.size[2]-south)
            part=Structure(dimensions)
            for position,block in merged.blocks.items():
                if east<=position[0]<east+dimensions[0] and south<=position[2]<south+dimensions[2]:
                    part.set((position[0]-east,position[1],position[2]-south),block,merged.block_entities.get(position))
            if not part.blocks:
                continue
            filename=output / 'parts' / f's2-x{east:03}-z{south:03}.nbt'
            part.write(filename)
            loaded=Structure.read(filename)
            for local,block in loaded.blocks.items():
                position=local[0]+east,local[1],local[2]+south
                assert restored.blocks[position]==block
                assert plain(restored.block_entities[position])==plain(loaded.block_entities[local]) if local in loaded.block_entities else position not in restored.block_entities
            total+=len(loaded.blocks)
            parts.append({'path':str(filename),'sha256':digest(filename),'origin':[east,0,south],'size':dimensions,'blocks':len(loaded.blocks),'block_entities':len(loaded.block_entities)})
    assert total == len(merged.blocks)
    write(output / 'parts/index.json', {'source_sha256':sha256,'voxel_and_payload_comparison':'PASS','source_records':total,'parts':parts})
    write(output / 'layout.json', {'source_sha256':sha256,'size':merged.size,'modules':modules,'corridors':corridors,'entrance':[3,9,21],'terminal':[3,3,91],'envelope_cells':len(envelope),'boundary_cells':len(boundary),'source_matches':source_matches})
    write(output / 'qa/assembly-integrity.json', {'source_sha256':sha256,'module_source_voxel_and_payload_match':source_matches,'part_voxel_and_payload_match':'PASS','part_records':total,'union_boundary_faces_checked':sum(1 for east,up,south in envelope for offset_east,offset_up,offset_south in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)) if (east+offset_east,up+offset_up,south+offset_south) not in envelope),'barrier_shell_cells':len(boundary),'closed_union_envelope':True,'ports_clear':20,'runtime_deployment':'NOT_RUN'})
    write(output / 'design.json', {'id':'S2-ASSEMBLY','nbt':candidate.name,'size':merged.size,'ports':[{'side':'west','feet':[3,9,21],'width':3,'clearHeight':4}],'targets':targets+[{'label':'terminal','position':[3,3,91]}],'modules':modules,'corridors':corridors,'views':[{'id':'preview-full','position':[-55,180,250],'target':[119,6,63],'fov':52},{'id':'preview-cutaway','position':[-55,180,250],'target':[119,6,63],'cutaway':True,'cutY':18,'fov':52},{'id':'preview-interior','position':[40.5,11.62,27.5],'target':[40,11,21],'fov':85}]})
    print('Assembly',sha256,'modules',len(modules),'records',len(merged.blocks),'parts',len(parts),'targets',len(targets)+1)


if __name__ == '__main__':
    main()
