from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-06'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def cut(direction, point):
    east, up, south = point
    near = {'north': south <= 2, 'south': south >= 22, 'west': east <= 3, 'east': east >= 22}[direction]
    return up >= 9 or up >= 2 and near


def main():
    source = TARGET/'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-06-corner.png')
    connections = [{'facing':side,'position':point,'width':5,'height':4,'outward_normal':normal} for side,point,normal in [('north',[7,2,0],[0,0,-1]),('east',[24,2,17],[1,0,0])]]
    targets = [{'name':label,'position':point} for label,point in [('north-threshold',[7,2,1]),('north-rib',[7,2,2]),('north-lamp',[7,2,5]),('north-middle',[7,2,9]),('north-inner',[7,2,13]),('bend-center',[7,2,17]),('bend-south',[7,2,19]),('corner-lamp-approach',[10,2,16]),('east-start',[12,2,17]),('east-middle',[16,2,17]),('east-lamp',[20,2,17]),('east-threshold',[23,2,17]),('pot-approach',[9,2,14]),('east-side-access',[19,2,19])]]
    skylights = [(east,south) for east in range(3,5) for south in range(4,7)]
    metadata = {'id':'G2B-06','name':'砂岩灯龛折角廊','size':list(structure.size),'entrance':[7,2,0],'interior':[7,2,5],'facing':'north','connections':connections,'route_targets':targets,'reference':str(reference),'reference_sha256':digest(reference),'sha256':sha,'coordinate_system':'Local XYZ feet, east +X, north -Z','hall_feet_y':2,'footprint':'1<=x<=24,0<=z<=23 AND (x<=13 OR z>=11); outside is explicit air within full 25x13x25 envelope','roof':{'type':'L-shaped sandstone roof','ceiling_y':11,'intentional_skylight_xz':skylights,'skylight_count':1,'open_cells':6,'not_fully_sealed':True},'landmark':{'type':'real 90-degree L corridor around solid inner corner with recessed hanging lamp','bend_center':[7,2,17],'inner_corner_lamp':[11,6,14],'clear_bent_strip':['x6..8,y2..5,z0..18','x6..24,y2..5,z16..18'],'no_straight_north_south_exit':True},'registration':'PENDING_INDEPENDENT_ACCEPTANCE','runtime_slot':None,'next_image':'S2-07 LOCKED_NOT_STARTED','world_placement':'NOT_RUN','deviations':['Estimated block proportions, not pixel-identical','Urns approximated by terracotta and pots; sand piles by supported blocks/slabs','Dry hanging foliage omitted rather than claiming live vines match','Real 2x3 skylight remains open','Full envelope includes air in northeast missing quadrant; integration must audit all overwritten cells']}
    save(TARGET/'room.json',metadata)
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert not any(materials[name] for name in ['minecraft:water','minecraft:lava','minecraft:chest'])
    assert not structure.entities and not structure.block_entities
    fragile,lights,sand_support = [],[],[]
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
            support_y = 9 if point == (11,6,14) else 11
            assert structure.get((east,support_y,south)).name == 'minecraft:cut_sandstone'
            assert all(structure.get((east,height,south)).name == 'minecraft:chain' for height in range(up+1,support_y))
            lights.append({'position':list(point),'chain_y':list(range(up+1,support_y)),'support_y':support_y,'support':'attached corner cap' if support_y == 9 else 'roof'})
    assert len(fragile) == 8 and len(lights) == 3 and len(sand_support) == 8
    for connector in connections:
        east,up,south = connector['position']
        for offset in range(-2,3):
            horizontal = (east,south+offset) if connector['facing'] == 'east' else (east+offset,south)
            assert structure.get((horizontal[0],up-1,horizontal[1])) != AIR
            for height in range(4):
                assert structure.get((horizontal[0],up+height,horizontal[1])) == AIR
    strips = {(east,up,south) for east in range(6,9) for up in range(2,6) for south in range(19)} | {(east,up,south) for east in range(6,25) for up in range(2,6) for south in range(16,19)}
    assert all(structure.get(point) == AIR for point in strips)
    assert all(structure.get((east,1,south)) != AIR for east,unused_up,south in strips)
    assert all(structure.get((7,up,23)) != AIR for up in range(2,6))
    assert all(structure.get((24,up,7)) == AIR and structure.get((24,1,7)) == AIR for up in range(2,6))
    footprint = {(east,south) for east in range(1,25) for south in range(24) if east<=13 or south>=11}
    roof_holes = [(east,south) for east,south in sorted(footprint) if structure.get((east,11,south)) == AIR]
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
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'supported_pots_and_bushes':fragile,'lantern_connections':lights,'supported_sand':sand_support,'six_face_solids':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'scope':'Static support and topology, not world physics','liquids':0,'containers':0,'two_perpendicular_supported_clear_ports':True,'continuous_bent_strip_3wide_4high_clear':True,'straight_south_continuation_blocked':True,'footprint_columns':len(footprint),'roof_holes_xz':roof_holes,'world_ticks':'NOT_RUN'})
    save(ROOT/'assembly.json',{'single_template':True,'origin':[0,0,0],'world_origin':None,'bounds_inclusive':[[0,0,0],[24,12,24]],'order':[{'step':1,'path':str(source),'offset':[0,0,0]}],'connections':connections,'hall_feet_y':2,'warning':'L footprint in rectangular explicit-air envelope: placement overwrites missing northeast quadrant too. Audit full volume. Both ports Y2, one 90-degree turn. Skylight weather/light/spawning/world ticks NOT_RUN; no deployment permission.'})
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
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'deployable':False,'roof_and_upper_cap_cut':'y>=9','near_wall_rule':{'north':'y>=2,z<=2','south':'y>=2,z>=22','west':'y>=2,x<=3','east':'y>=2,x>=22'}[direction],'only_declared_air_replacements':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    save(ROOT/'qa/route-input.json',[{'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]}])
    print('S2-06 PREPARED '+sha)


if __name__ == '__main__':
    main()
