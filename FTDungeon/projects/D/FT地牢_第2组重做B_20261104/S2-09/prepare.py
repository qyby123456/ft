from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT/'incoming/G2B-09'
sys.path.insert(0,'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


def cut(direction,point):
    east,up,south = point
    near = {'north':south<=3,'south':south>=27,'west':east<=2,'east':east>=26}[direction]
    return up>=14 or up>=3 and near


def main():
    source = TARGET/'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-09-treasure.png')
    connections = [{'facing':side,'position':position,'width':width,'height':4,'outward_normal':normal} for side,position,width,normal in [('north',[14,1,0],7,[0,0,-1]),('west',[0,3,12],5,[-1,0,0]),('east',[28,3,12],5,[1,0,0])]]
    positions = [('north-first-step',[14,2,1]),('north-second-step',[14,3,2]),('entry-hall',[14,3,5]),('rug-center',[14,3,12]),('rug-south',[14,3,16]),('dais-first-stair',[14,4,18]),('dais-lower-landing',[14,4,19]),('dais-second-stair',[14,5,20]),('main-chest-front',[14,5,21]),('main-front-left',[13,5,21]),('main-front-right',[15,5,21]),('main-chest-side',[13,5,23]),('west-chest-front',[5,3,20]),('east-chest-front',[23,3,20]),('west-niche-approach',[7,3,21]),('east-niche-approach',[21,3,21]),('west-statue-front',[9,3,21]),('east-statue-front',[19,3,21])]
    targets = [{'name':label,'position':position} for label,position in positions]
    chests = [{'position':[14,5,22],'facing':'north','front_feet':[14,5,21],'role':'main','expected_slots':4,'expected_items':[['gold_ingot',6],['emerald',3],['diamond',1],['bread',4]]},{'position':[4,3,20],'facing':'east','front_feet':[5,3,20],'role':'west-niche','expected_slots':3,'expected_items':[['gold_nugget',12],['iron_ingot',4],['torch',8]]},{'position':[24,3,20],'facing':'west','front_feet':[23,3,20],'role':'east-niche','expected_slots':3,'expected_items':[['gold_nugget',12],['iron_ingot',4],['torch',8]]}]
    roof_columns = [(east,south) for east in range(29) for south in range(3,29) if structure.get((east,14,south)) != AIR]
    sky_columns = [(east,south) for east in range(6,23) for south in range(6,24) if all(structure.get((east,up,south)) == AIR for up in range(14,18))]
    metadata = {'id':'G2B-09','name':'砂日纹饰宝藏室','size':list(structure.size),'entrance':[14,1,0],'interior':[14,3,12],'facing':'north','connections':connections,'route_targets':targets,'chests':chests,'reference':str(reference),'reference_sha256':digest(reference),'sha256':sha,'coordinate_system':'Local XYZ feet; east +X; north -Z','hall_feet_y':3,'dais_feet_y':5,'footprint':'Ground x0..28,z3..28 plus north approach x10..18,z0..2; full 29x18x29 explicit-air envelope','roof':{'type':'Ruined rim roof and front beam, true central open sky','roof_y':14,'solid_columns':len(roof_columns),'open_central_columns':len(sky_columns),'open_central_bounds_xz':[[6,6],[22,23]],'not_fully_sealed':True},'landmark':{'type':'Rear diamond mosaic, two-stage treasure dais, paired block statues, central floor mosaic and two reachable side chest niches','main_chest':[14,5,22],'rear_mural_x_y_z':[[10,5,26],[18,13,26]],'dais_stairs_z':[18,20],'no_rear_exit':True},'registration':'PENDING_INDEPENDENT_ACCEPTANCE','runtime_slot':None,'next_image':'S2-10 LOCKED_NOT_STARTED','world_placement':'NOT_RUN','deviations':['Block-scale proportions estimated, not pixel-identical','Both side chests lowered for accessible standing fronts; original left high shelf not duplicated','Urns use terracotta and flower pots; statues use sandstone not entities','Long hanging flags approximated by solid wall-colored bands, not banner block entities','Lanterns replace fire braziers for dry non-hazardous approach','Roof is genuinely ruined/open; integration needs weather/light/spawn review','Fixed example inventories, no LootTable or balance claim']}
    save(TARGET/'room.json',metadata)
    assert structure.size == (29,18,29) or list(structure.size) == [29,18,29]
    assert not structure.entities and len(structure.block_entities) == 3
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert materials['minecraft:chest'] == 3 and not any(materials[name] for name in ['minecraft:water','minecraft:lava','minecraft:fire','minecraft:trapped_chest'])
    fragile,lights,sand_support = [],[],[]
    for point,state in structure.blocks.items():
        east,up,south = point
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush','minecraft:lantern'}:
            fragile.append({'position':list(point),'block':state.name})
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush'}:
            below = structure.get((east,up-1,south))
            assert below != AIR
            if state.name == 'minecraft:dead_bush':
                assert below.name == 'minecraft:sand'
        if state.name == 'minecraft:sand':
            below = structure.get((east,up-1,south))
            assert below != AIR
            sand_support.append({'position':list(point),'support':below.name})
        if state.name == 'minecraft:lantern':
            hanging = state.get('hanging') == 'true'
            if hanging:
                assert structure.get((east,14,south)).name == 'minecraft:cut_sandstone'
                assert all(structure.get((east,height,south)).name == 'minecraft:chain' for height in [11,12,13])
            else:
                assert structure.get((east,up-1,south)).name == 'minecraft:chiseled_sandstone'
            lights.append({'position':list(point),'hanging':hanging,'support_y':14 if hanging else up-1})
    assert len(fragile) == 20 and len(lights) == 8 and len(sand_support) == 6
    for chest in chests:
        east,up,south = chest['position']
        assert structure.get((east,up,south)).name == 'minecraft:chest'
        assert structure.get((east,up-1,south)) != AIR
        assert all(structure.get((east,height,south)) == AIR for height in [up+1,up+2])
        front = chest['front_feet']
        assert all(structure.get((front[0],front[1]+height,front[2])) == AIR for height in range(3))
    for connector in connections:
        east,up,south = connector['position']
        for offset in range(-(connector['width']//2),connector['width']//2+1):
            horizontal = (east+offset,south) if connector['facing'] == 'north' else (east,south+offset)
            assert structure.get((horizontal[0],up-1,horizontal[1])) != AIR
            assert all(structure.get((horizontal[0],height,horizontal[1])) == AIR for height in range(up,up+4))
    solids = {point for point,state in structure.blocks.items() if state != AIR}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east,up,south = pending.popleft()
        for neighbor in [(east-1,up,south),(east+1,up,south),(east,up-1,south),(east,up+1,south),(east,up,south-1),(east,up,south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids-reached,sorted(solids-reached)[:20]
    save(TARGET/'materials.json',dict(materials))
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'fragile_points':fragile,'lantern_connections':lights,'supported_sand':sand_support,'six_face_solids':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'fluids':0,'containers':3,'chest_static_air_above':2,'chest_front_air_height':3,'roof_solid_columns':len(roof_columns),'true_open_central_columns':len(sky_columns),'three_supported_clear_ports':True,'world_ticks':'NOT_RUN'})
    save(ROOT/'assembly.json',{'single_template':True,'origin':[0,0,0],'world_origin':None,'bounds_inclusive':[[0,0,0],[28,17,28]],'order':[{'step':1,'path':str(source),'offset':[0,0,0]}],'connections':connections,'hall_feet_y':3,'dais_feet_y':5,'warning':'North interface feetY1; two side interfaces feetY3. Full explicit-air envelope overwrites cells. Central ruined roof is open. No deployment authorized.'})
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
        changed = {tuple(point) for point in removed}
        check = Structure.read(path)
        assert all(check.get(point) == state for point,state in structure.blocks.items() if point not in changed)
        assert len(check.block_entities) == 3
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'removed_positions':removed,'deployable':False,'roof_cut':'y>=14','near_wall_rule':{'north':'y>=3,z<=3','south':'y>=3,z>=27','west':'y>=3,x<=2','east':'y>=3,x>=26'}[direction],'only_declared_air_replacements':True,'all_three_chest_entities_preserved':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    save(ROOT/'qa/route-input.json',[{'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]}])
    print('S2-09 PREPARED '+sha)


if __name__ == '__main__':
    main()
