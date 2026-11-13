from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-08'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def cut(direction, point):
    east, up, south = point
    near = {'north': south <= 4, 'south': south >= 33, 'west': east <= 2, 'east': east >= 30}[direction]
    roof = up >= 18 and (east <= 5 or east >= 27 or south >= 29)
    return roof or up >= 3 and near


def main():
    source = TARGET/'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-08-courtyard.png')
    connections = [{'facing':side,'position':point,'width':width,'height':4,'outward_normal':normal} for side,point,width,normal in [('north',[16,1,0],7,[0,0,-1]),('south',[16,3,34],5,[0,0,1]),('west',[0,3,20],5,[-1,0,0]),('east',[32,3,20],5,[1,0,0])]]
    positions = [('north-step-one',[16,2,1]),('north-step-two',[16,3,2]),('north-landing',[16,3,3]),('court-north',[16,3,9]),('pool-west',[10,3,17]),('pool-east',[24,3,17]),('pool-south',[17,3,25]),('tree-planter-access',[11,3,25]),('stair-approach',[5,3,5]),('stair-middle',[5,7,9]),('upper-landing',[5,11,14]),('upper-west',[5,11,20]),('upper-rear-west',[5,11,31]),('upper-rear-center',[16,11,31]),('upper-rear-east',[29,11,31]),('upper-east',[29,11,20]),('upper-east-front',[29,11,8]),('south-threshold',[16,3,33])]
    targets = [{'name':label,'position':point} for label,point in positions]
    metadata = {'id':'G2B-08','name':'砂岩树影跌水庭院','size':list(structure.size),'entrance':[16,1,0],'interior':[16,3,9],'facing':'north','connections':connections,'route_targets':targets,'reference':str(reference),'reference_sha256':digest(reference),'sha256':sha,'coordinate_system':'Local XYZ feet; east +X; north -Z','hall_feet_y':3,'upper_gallery_feet_y':11,'footprint':'Ground x0..32,z4..34 plus north approach x11..21,z0..3; entire 33x24x35 envelope includes explicit air','roof':{'type':'U-shaped portico roof, open central sky','roof_y':19,'gallery_floor_y':10,'tree_canopy_max_y':21,'not_fully_sealed':True},'landmark':{'type':'Central tiered sandstone fountain and broad persistent oak tree behind-west, U-shaped accessible upper galleries','basin_xz':[[12,12],[22,22]],'basin_water_y':3,'fountain_source':[17,8,16],'falling_water_column':[[17,4,17],[17,7,17]],'tree_trunk_xz':[[8,23],[9,24]],'stairs':'x3..5,z6..13; staircase y3..10 southward; feet rises from3 to11'},'registration':'PENDING_INDEPENDENT_ACCEPTANCE','runtime_slot':None,'next_image':'S2-09 LOCKED_NOT_STARTED','world_placement':'NOT_RUN','deviations':['Estimated block proportions, square basin and stepped rectangular columns approximate reference curves','Urns approximated by terracotta and flower pots','Native flowing water block states, not fluid simulation; fluid propagation and generated sources untested','Persistent oak leaves prevent ordinary decay but lighting, fire, neighbor updates and world ticks untested','Open sky courtyard requires weather, lighting and placement review','Entire explicit-air envelope overwrites cells; not approved for integration']}
    save(TARGET/'room.json',metadata)
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert not structure.entities and not structure.block_entities
    assert materials['minecraft:chest'] == materials['minecraft:lava'] == 0
    fragile, lights, sand_support, water, leaves = [],[],[],[],[]
    for point,state in structure.blocks.items():
        east,up,south = point
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush','minecraft:lily_pad','minecraft:vine','minecraft:lantern'}:
            fragile.append({'position':list(point),'block':state.name})
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush','minecraft:lily_pad'}:
            below = structure.get((east,up-1,south))
            assert below != AIR
            if state.name == 'minecraft:dead_bush':
                assert below.name == 'minecraft:sand'
            if state.name == 'minecraft:lily_pad':
                assert below.name == 'minecraft:water' and below.get('level') == '0'
        if state.name == 'minecraft:sand':
            below = structure.get((east,up-1,south))
            assert below.name in {'minecraft:sandstone','minecraft:smooth_sandstone','minecraft:cut_sandstone'}
            sand_support.append({'position':list(point),'support':below.name})
        if state.name == 'minecraft:lantern':
            assert structure.get((east,19,south)).name == 'minecraft:cut_sandstone'
            assert all(structure.get((east,height,south)).name == 'minecraft:chain' for height in [17,18])
            lights.append({'position':list(point),'chain_y':[17,18],'support_y':19})
        if state.name == 'minecraft:water':
            assert 13 <= east <= 21 and 13 <= south <= 21
            if up == 3:
                assert structure.get((east,2,south)).name == 'minecraft:cut_sandstone'
                for delta_east,delta_south in [(1,0),(-1,0),(0,1),(0,-1)]:
                    neighbor = structure.get((east+delta_east,up,south+delta_south))
                    assert neighbor != AIR
            water.append({'position':list(point),'level':state.get('level')})
        if state.name == 'minecraft:oak_leaves':
            assert state.get('persistent') == 'true'
            leaves.append(list(point))
    assert len(fragile) == 35 and len(lights) == 3 and len(sand_support) == 4
    for connector in connections:
        east,up,south = connector['position']
        for offset in range(-(connector['width']//2),connector['width']//2+1):
            horizontal = (east+offset,south) if connector['facing'] in {'north','south'} else (east,south+offset)
            assert structure.get((horizontal[0],up-1,horizontal[1])) != AIR
            assert all(structure.get((horizontal[0],height,horizontal[1])) == AIR for height in range(up,up+4))
    solids = {point for point,state in structure.blocks.items() if state != AIR and state.name not in {'minecraft:water','minecraft:lily_pad'}}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east,up,south = pending.popleft()
        for neighbor in [(east-1,up,south),(east+1,up,south),(east,up-1,south),(east,up+1,south),(east,up,south-1),(east,up,south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids-reached, sorted(solids-reached)[:30]
    save(TARGET/'materials.json',dict(materials))
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'fragile_points':fragile,'lantern_connections':lights,'supported_sand':sand_support,'six_face_solids_excluding_fluids_and_pads':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'water_supported_lily_pads':4,'persistent_leaves':len(leaves),'water_states':water,'fluids_count':len(water),'containers':0,'four_supported_clear_ports':True,'scope':'Static connectivity, support and containment assertions; not world physics or fluid simulation','world_ticks':'NOT_RUN'})
    save(ROOT/'assembly.json',{'single_template':True,'origin':[0,0,0],'world_origin':None,'bounds_inclusive':[[0,0,0],[32,23,34]],'order':[{'step':1,'path':str(source),'offset':[0,0,0]}],'connections':connections,'hall_feet_y':3,'upper_gallery_feet_y':11,'warning':'North interface feetY1; other three feetY3. Full explicit-air volume overwrites cells. Open courtyard, waterfall spread, source generation, leaf update, weather, lighting, rotation and deployment untested.'})
    derivatives = []
    for direction in ['north','south','west','east']:
        preview = Structure.read(source)
        removed = []
        for point,state in list(preview.blocks.items()):
            if cut(direction,point) and state != AIR:
                preview.set(point,AIR)
                removed.append(list(point))
        path = ROOT/'preview-only'/f'{direction}.nbt'
        preview.write(path)
        check = Structure.read(path)
        changed = {tuple(point) for point in removed}
        assert all(check.get(point) == state for point,state in structure.blocks.items() if point not in changed)
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'removed_positions':removed,'deployable':False,'roof_cut':'y>=18 AND (x<=5 OR x>=27 OR z>=29)','near_wall_rule':{'north':'y>=3,z<=4','south':'y>=3,z>=33','west':'y>=3,x<=2','east':'y>=3,x>=30'}[direction],'only_declared_air_replacements':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    save(ROOT/'qa/route-input.json',[{'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]}])
    print('S2-08 PREPARED '+sha)


if __name__ == '__main__':
    main()
