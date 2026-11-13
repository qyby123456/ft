from collections import Counter, deque
from copy import deepcopy
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT/'incoming/G2B-10'
sys.path.insert(0,'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure, plain, integer


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


def cut(direction,point):
    east,up,south = point
    near = {'north':south<=3,'south':south>=64,'west':east<=3,'east':east>=62}[direction]
    return up>=10 or up>=3 and near


def main():
    source = TARGET/'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-10-overview.png')
    regions = [{'id':label,'bounds_inclusive':[[left,0,front],[right,17,back]],'floor_feet':3,'wall_top_y':top,'roof':roof,'center':center} for label,left,front,right,back,top,roof,center in [('entrance',24,3,42,20,13,'rim',[33,3,12]),('front-store',2,3,21,22,12,'rim',[12,3,12]),('west-ritual-hall',2,27,21,51,14,'rim',[12,3,38]),('courtyard',24,24,44,45,12,'rim',[33,3,26]),('rear-grand-hall',23,48,46,64,15,'rim',[34,3,55]),('east-treasure-room',54,3,62,24,12,'rim',[58,3,14]),('east-closed-shrine',54,48,62,64,12,'closed',[58,3,56]),('east-long-gallery',47,3,52,64,11,'closed',[49,3,35])]]
    links = [{'id':label,'regions':pair,'bounds_xz_inclusive':[[left,front],[right,back]],'axis':axis,'clear_width':width,'clear_height':5,'feet_y':3,'center':center} for label,pair,left,front,right,back,axis,width,center in [('entry-store',['entrance','front-store'],21,9,24,15,'x',5,[22,3,12]),('entry-gallery',['entrance','east-long-gallery'],42,9,47,15,'x',5,[44,3,12]),('entry-court',['entrance','courtyard'],29,20,37,24,'z',7,[33,3,22]),('ritual-court',['west-ritual-hall','courtyard'],21,32,24,38,'x',5,[22,3,35]),('court-gallery',['courtyard','east-long-gallery'],44,32,47,38,'x',5,[45,3,35]),('court-grand',['courtyard','rear-grand-hall'],29,45,37,48,'z',7,[33,3,46]),('store-ritual-bend',['front-store','west-ritual-hall'],9,22,15,27,'z',5,[12,3,24]),('ritual-grand-link',['west-ritual-hall','rear-grand-hall'],21,48,23,52,'x',3,[22,3,50]),('gallery-treasure',['east-long-gallery','east-treasure-room'],52,11,54,17,'x',5,[53,3,14]),('gallery-shrine',['east-long-gallery','east-closed-shrine'],52,52,54,58,'x',5,[53,3,55])]]
    positions = [('north-first-step',[33,2,1]),('north-second-step',[33,3,2]),('entrance-back',[33,3,19]),('store-west-chest-front',[6,3,20]),('store-east-chest-front',[17,3,20]),('store-to-bend',[12,3,21]),('ritual-entry',[12,3,28]),('ritual-rear',[12,3,49]),('courtyard-west-walk',[26,3,34]),('courtyard-east-walk',[42,3,34]),('courtyard-rear-walk',[34,3,42]),('court-pool-north',[34,3,28]),('gallery-north',[49,3,5]),('gallery-front-junction',[49,3,12]),('gallery-south',[49,3,62]),('gallery-rear-junction',[49,3,55]),('grand-dais-stair',[33,4,58]),('grand-dais-landing',[33,4,59]),('grand-chest-front',[33,4,60]),('treasure-chest-front',[58,3,20]),('shrine-chest-front',[58,3,60]),('shrine-west-barrel-access',[56,3,60]),('shrine-east-barrel-access',[60,3,60])]
    targets = [{'name':'region-'+item['id'],'position':item['center']} for item in regions]+[{'name':'link-'+item['id'],'position':item['center']} for item in links]+[{'name':label,'position':position} for label,position in positions]
    connections = [{'facing':'north','position':[33,1,0],'width':7,'height':4,'outward_normal':[0,0,-1]}]
    chests = [{'position':position,'facing':facing,'front_feet':front,'role':role,'expected_slots':2,'expected_items':[['gold_ingot',3],['bread',4]]} for position,facing,front,role in [([5,3,20],'east',[6,3,20],'store-west'),([18,3,20],'west',[17,3,20],'store-east'),([33,4,61],'north',[33,4,60],'grand-dais'),([58,3,21],'north',[58,3,20],'treasure'),([58,3,61],'north',[58,3,60],'closed-shrine')]]
    roof = []
    for item in regions:
        left,unused,front = item['bounds_inclusive'][0]
        right,unused,back = item['bounds_inclusive'][1]
        covered = sum(structure.get((east,item['wall_top_y'],south)) != AIR for east in range(left,right+1) for south in range(front,back+1))
        open_sky = sum(all(structure.get((east,up,south)) == AIR for up in range(item['wall_top_y'],18)) for east in range(left+1,right) for south in range(front+1,back))
        roof.append({'region':item['id'],'top_y':item['wall_top_y'],'covered_columns_at_top':covered,'true_open_sky_inner_columns':open_sky,'design':item['roof']})
    metadata = {'id':'G2B-10','name':'砂日多殿地牢总览','size':list(structure.size),'entrance':[33,1,0],'interior':[33,3,12],'facing':'north','connections':connections,'route_targets':targets,'chests':chests,'regions':regions,'internal_links':links,'reference':str(reference),'reference_sha256':digest(reference),'sha256':sha,'coordinate_system':'Local XYZ feet; reference front=NORTH=-Z, left=WEST=-X, rear=SOUTH=+Z','hall_feet_y':3,'dais_feet_y':4,'footprint':'Eight unequal regions and ten enclosed connector tunnels; no blanket ground outside their union; full explicit-air 65x18x67 envelope','roof':roof,'registration':'PENDING_INDEPENDENT_ACCEPTANCE','runtime_slot':None,'next_image':'S2-11 LOCKED_NOT_STARTED','world_placement':'NOT_RUN','deviations':['New independent overview layout, not pasted prior candidates or screenshot assembly','Estimated room proportions; east shrine roof intentionally intact, central court and halls genuinely open with rim roof','No tall tree in overview reference; central dry pillar and still source pool replace detailed courtyard waterfall','Wall banners approximated by solid color bands; small statues and braziers approximated by pillars and lanterns','Fixed example chest inventory is not loot balance certification','Entire explicit-air envelope needs placement coverage review; tiles are mandatory assembly assets, not only previews']}
    save(TARGET/'room.json',metadata)
    save(ROOT/'layout.json',{'source_sha256':sha,'regions':regions,'links':links,'all_region_centers_and_link_centers_are_body_route_targets':True,'graph_scope':'Authored connections; native body reachability checked separately for every declared center','external_connections':connections})
    assert len(structure.blocks) == 65*18*67 and len(structure.block_entities) == 7 and not structure.entities
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert materials['minecraft:chest'] == 5 and materials['minecraft:barrel'] == 2 and materials['minecraft:water'] == 40
    assert not any(materials[name] for name in ['minecraft:lava','minecraft:fire','minecraft:trapped_chest'])
    fragile,lights,sand_support,water = [],[],[],[]
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
            support_y = up-1
            if hanging:
                support_y = up+1
                while structure.get((east,support_y,south)).name == 'minecraft:chain':
                    support_y += 1
            assert structure.get((east,support_y,south)) != AIR,(point,support_y)
            lights.append({'position':list(point),'hanging':hanging,'support_y':support_y})
        if state.name == 'minecraft:water':
            assert up == 3 and state.get('level') == '0' and structure.get((east,2,south)).name == 'minecraft:cut_sandstone'
            assert all(structure.get((east+offset_east,up,south+offset_south)) != AIR for offset_east,offset_south in [(1,0),(-1,0),(0,1),(0,-1)])
            water.append(list(point))
    for chest in chests:
        east,up,south = chest['position']
        assert structure.get((east,up,south)).name == 'minecraft:chest'
        assert structure.get((east,up-1,south)) != AIR
        assert all(structure.get((east,height,south)) == AIR for height in [up+1,up+2])
        east,up,south = chest['front_feet']
        assert all(structure.get((east,up+height,south)) == AIR for height in range(3))
    solids = {point for point,state in structure.blocks.items() if state != AIR and state.name != 'minecraft:water'}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east,up,south = pending.popleft()
        for neighbor in [(east-1,up,south),(east+1,up,south),(east,up-1,south),(east,up+1,south),(east,up,south-1),(east,up,south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids-reached,sorted(solids-reached)[:30]
    save(TARGET/'materials.json',dict(materials))
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'fragile_points':fragile,'fragile_count':len(fragile),'lantern_connections':lights,'supported_sand':sand_support,'six_face_solids_excludes_water':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'water_count':len(water),'water_positions':water,'water_horizontal_air_edges':0,'water_supported_bottom':True,'containers':7,'chests':5,'barrels':2,'roof_by_region':roof,'body_targets':1+len(targets),'world_ticks':'NOT_RUN'})
    derivatives = []
    for direction in ['north','south','west','east']:
        preview = Structure.read(source)
        removed = []
        removed_entities = []
        for point,state in list(preview.blocks.items()):
            if cut(direction,point) and state != AIR:
                if point in preview.block_entities:
                    removed_entities.append(list(point))
                preview.set(point,AIR)
                removed.append(list(point))
        path = ROOT/'preview-only'/f'{direction}.nbt'
        preview.write(path)
        changed = {tuple(point) for point in removed}
        check = Structure.read(path)
        assert all(check.get(point) == state for point,state in structure.blocks.items() if point not in changed)
        assert not removed_entities and len(check.block_entities) == 7
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'removed_positions':removed,'removed_entities':removed_entities,'deployable':False,'roof_cut':'y>=10','near_wall_rule':{'north':'y>=3,z<=3','south':'y>=3,z>=64','west':'y>=3,x<=3','east':'y>=3,x>=62'}[direction],'only_declared_air_replacements':True,'all_seven_container_entities_preserved':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    save(ROOT/'qa/route-input.json',[{'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]}])
    tiles = []
    reconstruction = Structure(structure.size)
    seen = set()
    for tile_index,(left,front,right,back) in enumerate([(0,0,31,33),(32,0,64,33),(0,34,31,66),(32,34,64,66)],start=1):
        offset = (left,0,front)
        tile = Structure((right-left+1,18,back-front+1))
        for east in range(left,right+1):
            for up in range(18):
                for south in range(front,back+1):
                    point = (east,up,south)
                    local = (east-left,up,south-front)
                    entity = deepcopy(structure.block_entities.get(point))
                    if entity is not None:
                        for axis,value in zip(['x','y','z'],local):
                            if axis in entity.value:
                                entity.value[axis] = integer(value)
                    tile.set(local,structure.get(point),entity)
        path = ROOT/'tiles'/f'part-{tile_index:02}.nbt'
        tile.write(path)
        decoded = Structure.read(path)
        assert all(dimension<=48 for dimension in decoded.size)
        for local,state in decoded.blocks.items():
            point = tuple(local[axis]+offset[axis] for axis in range(3))
            assert point not in seen
            seen.add(point)
            entity = deepcopy(decoded.block_entities.get(local))
            if entity is not None:
                for axis,value in zip(['x','y','z'],point):
                    if axis in entity.value:
                        entity.value[axis] = integer(value)
            reconstruction.set(point,state,entity)
        tiles.append({'step':tile_index,'path':str(path),'sha256':digest(path),'offset':list(offset),'size':list(decoded.size),'explicit_cells':len(decoded.blocks),'explicit_air_cells':sum(state==AIR for state in decoded.blocks.values()),'block_entities':len(decoded.block_entities),'deployment_ready':False})
    assert reconstruction.blocks == structure.blocks and seen == set(structure.blocks)
    assert {point:plain(data) for point,data in reconstruction.block_entities.items()} == {point:plain(data) for point,data in structure.block_entities.items()}
    reconstructed_path = ROOT/'qa/reconstructed-from-tiles.nbt'
    reconstruction.write(reconstructed_path)
    check = Structure.read(reconstructed_path)
    assert check.blocks == structure.blocks and {point:plain(data) for point,data in check.block_entities.items()} == {point:plain(data) for point,data in structure.block_entities.items()}
    save(ROOT/'assembly.json',{'source':str(source),'source_sha256':sha,'single_template':False,'full_size':list(structure.size),'world_origin':None,'bounds_inclusive':[[0,0,0],[64,17,66]],'tiles':tiles,'placement_order':[1,2,3,4],'explicit_air_strategy':'Dense nonoverlapping partitions; every cell including air is written once. No structure_void and no skip-air. Placement overwrites entire 65x18x67 envelope. Origins are relative to one shared world origin; no rotations or mirrors. Use no implicit per-piece terrain adjustment.','block_entity_coordinates':'If x/y/z fields exist they are rebased to tile-local and restored for comparison; container Item Slot/Count/name unchanged.','reconstruction':{'status':'PASS','unique_cells':len(seen),'overlap_cells':0,'missing_cells':0,'all_block_states_exact':True,'all_block_entity_payloads_exact':True,'source_block_entities':len(structure.block_entities),'reconstructed':str(reconstructed_path),'reconstructed_sha256':digest(reconstructed_path),'compressed_bytes_not_expected_identical':'Palette ordering/serialization may differ; decoded state and entity equality is required.'},'warning':'Passing reconstruction does not certify placement scheduling, boundaries under world updates or deployment; all NOT_RUN.'})
    print(f'S2-10 PREPARED {sha};{len(targets)+1} routes;{len(fragile)} fragile;4 tiles exact')


if __name__ == '__main__':
    main()
