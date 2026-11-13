from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-05'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def cut(direction, point):
    east, up, south = point
    roof = up >= 8
    near = {'north': south <= 2, 'south': south >= 38, 'west': east <= 3, 'east': east >= 11}[direction]
    return roof or up >= 2 and near


def main():
    source = TARGET / 'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-05-corridor.png')
    connections = [{'facing': side, 'position': point, 'width': 5, 'height': 4, 'outward_normal': normal} for side, point, normal in [('north', [7,2,0], [0,0,-1]), ('south', [7,2,40], [0,0,1])]]
    targets = [{'name': label, 'position': point} for label, point in [('near-threshold',[7,2,1]), ('rib-one',[7,2,2]), ('lamp-one',[7,2,6]), ('rib-two',[7,2,10]), ('middle-front',[7,2,14]), ('lamp-two',[7,2,19]), ('rib-four',[7,2,26]), ('lamp-three',[7,2,32]), ('rib-last',[7,2,38]), ('far-threshold',[7,2,39]), ('west-pot-access',[5,2,5]), ('east-pot-access',[9,2,13]), ('west-sand-access',[5,2,23]), ('east-sand-access',[9,2,33])]]
    skylights = [(east,south) for east in range(3,5) for south in range(5,8)] + [(east,south) for east in range(10,12) for south in range(29,32)]
    metadata = {'id':'G2B-05', 'name':'砂岩纹带积沙长廊', 'size':list(structure.size), 'entrance':[7,2,0], 'interior':[7,2,6], 'facing':'north', 'connections':connections, 'route_targets':targets, 'reference':str(reference), 'reference_sha256':digest(reference), 'sha256':sha, 'coordinate_system':'Local XYZ feet, east +X, north -Z', 'hall_feet_y':2, 'roof':{'type':'flat sandstone with transverse rib beams', 'ceiling_y':10, 'intentional_skylight_xz':skylights, 'skylight_count':2, 'open_cells':12, 'not_fully_sealed':True}, 'landmark':{'type':'straight low-ceiling ribbed corridor, three centerline hanging lamps, orange segmented pavement and wall bands', 'clear_center_strip_inclusive':[[6,2,0],[8,5,40]], 'no_platform_pool_or_columns':True}, 'registration':'PENDING_INDEPENDENT_ACCEPTANCE', 'runtime_slot':None, 'next_image':'S2-06 LOCKED_NOT_STARTED', 'world_placement':'NOT_RUN', 'deviations':['Block-scale proportions estimated, not pixel-identical','Sand slopes approximated with supported sand/sandstone/slabs','Urns approximated by terracotta and flower pots','Wall symbols approximated by chiseled red sandstone','Two real skylight breaches, not fully weather-sealed; no invented side exits']}
    save(TARGET/'room.json', metadata)
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert not materials['minecraft:water'] and not materials['minecraft:lava'] and not materials['minecraft:chest']
    assert not structure.entities and not structure.block_entities
    fragile, lights, sand_support = [], [], []
    for point,state in structure.blocks.items():
        east,up,south = point
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush'}:
            below = structure.get((east,up-1,south))
            assert below != AIR
            if state.name == 'minecraft:dead_bush':
                assert below.name == 'minecraft:sand'
            fragile.append(list(point))
        if state.name == 'minecraft:sand':
            below = structure.get((east,up-1,south))
            assert below.name in {'minecraft:sandstone','minecraft:smooth_sandstone','minecraft:cut_sandstone'}
            sand_support.append({'position':list(point),'support':below.name})
        if state.name == 'minecraft:lantern':
            assert up == 7 and structure.get((east,10,south)).name == 'minecraft:cut_sandstone'
            assert all(structure.get((east,height,south)).name == 'minecraft:chain' for height in [8,9])
            lights.append({'position':list(point),'chain_y':[8,9],'roof_y':10})
    assert len(fragile) == 8 and len(lights) == 3 and len(sand_support) == 12
    for connector in connections:
        east,up,south = connector['position']
        for offset in range(-2,3):
            assert structure.get((east+offset,up-1,south)) != AIR
            for height in range(4):
                assert structure.get((east+offset,up+height,south)) == AIR
    assert all(structure.get((east,up,south)) == AIR for east in range(6,9) for up in range(2,6) for south in range(41))
    roof_holes = [(east,south) for east in range(15) for south in range(41) if structure.get((east,10,south)) == AIR]
    assert set(roof_holes) == set(skylights)
    solids = {point for point,state in structure.blocks.items() if state != AIR}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east,up,south = pending.popleft()
        for neighbor in [(east-1,up,south),(east+1,up,south),(east,up-1,south),(east,up+1,south),(east,up,south-1),(east,up,south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids-reached, sorted(solids-reached)[:20]
    save(TARGET/'materials.json',dict(materials))
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'supported_pots_and_bushes':fragile,'lantern_roof_connections':lights,'supported_sand':sand_support,'six_face_solids':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'scope':'Static topology/support checks, not physical simulation','liquids':0,'containers':0,'two_connectors_full_width_height_clear':True,'center_strip_3wide_4high_all_41_positions_clear':True,'roof_holes_xz':roof_holes,'world_ticks':'NOT_RUN'})
    save(ROOT/'assembly.json',{'single_template':True,'origin':[0,0,0],'world_origin':None,'bounds_inclusive':[[0,0,0],[14,11,40]],'order':[{'step':1,'path':str(source),'offset':[0,0,0]}],'connections':connections,'hall_feet_y':2,'warning':'Multi-chunk full-volume occupancy required. Both ports Y2, no elevation difference. Two roof breaches expose weather/light/spawning to integration conditions. World ticks and deployment NOT_RUN.'})
    derivatives = []
    for direction in ['north','south','west','east']:
        preview = Structure.read(source)
        removed = []
        for point,state in list(preview.blocks.items()):
            if cut(direction,point) and state != AIR:
                preview.set(point,AIR)
                removed.append(point)
        path = ROOT/'preview-only'/f'{direction}.nbt'
        preview.write(path)
        check = Structure.read(path)
        changed = set(removed)
        assert all(check.get(point) == state for point,state in structure.blocks.items() if point not in changed)
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'deployable':False,'roof_and_upper_rib_cut':'y>=8','near_wall_rule':{'north':'y>=2,z<=2','south':'y>=2,z>=38','west':'y>=2,x<=3','east':'y>=2,x>=11'}[direction],'only_declared_air_replacements':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    save(ROOT/'qa/route-input.json',[{'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]}])
    print('S2-05 PREPARED '+sha)


if __name__ == '__main__':
    main()
