from collections import Counter, deque
import copy
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT/'incoming/G2B-07'
sys.path.insert(0,'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure, State


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


def cut(direction,point):
    east,up,south=point
    roof=up>=12 and south>=20
    near={'north':south<=5,'south':south>=20,'west':east<=2,'east':east>=24}[direction]
    return roof or up>=3 and near


def main():
    source=TARGET/'room.nbt'
    structure=Structure.read(source)
    sha=digest(source)
    reference=Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-07-entrance.png')
    connections=[{'facing':side,'position':point,'width':width,'height':height,'outward_normal':normal} for side,point,width,height,normal in [('north',[12,1,0],4,4,[0,0,-1]),('south',[13,3,28],5,6,[0,0,1])]]
    connections[0]['lateral_x_inclusive']=[11,14]
    targets=[{'name':label,'position':point} for label,point in [('step-one',[12,2,1]),('step-two',[12,3,2]),('gate-landing',[12,3,3]),('gate-inside',[12,3,5]),('court-front',[13,3,8]),('court-center',[13,3,12]),('rear-approach',[13,3,17]),('rear-arch',[13,3,21]),('rear-hall',[13,3,26]),('left-alcove',[4,3,18]),('right-alcove',[22,3,18]),('board-approach',[17,3,18]),('brazier-safe-approach',[17,3,15]),('right-court',[21,3,13])]]
    gate_points=[(east,up,4) for east in [12,13] for up in [3,4]]
    for point in gate_points:
        door=structure.get(point)
        props=dict(door.properties)
        assert door.name=='minecraft:dark_oak_door' and props['open']=='false'
        assert props['half']==('lower' if point[1]==3 else 'upper')
        assert props['hinge']==('left' if point[0]==12 else 'right') and props['powered']=='false'
        assert props['facing']=='north'
        assert structure.get((point[0],2,4)).name in {'minecraft:cut_sandstone','minecraft:smooth_sandstone','minecraft:orange_terracotta'}
    metadata={'id':'G2B-07','name':'砂岩双扇门入口庭院','size':list(structure.size),'entrance':[12,1,0],'interior':[13,3,8],'facing':'north','connections':connections,'route_targets':targets,'reference':str(reference),'reference_sha256':digest(reference),'sha256':sha,'coordinate_system':'Local XYZ feet,east+X,north-Z','hall_feet_y':3,'gate_state':'CLOSED','gate':{'positions':[list(point) for point in gate_points],'width':2,'lower_feet_y':3,'axis_z':4,'x_inclusive':[12,13],'north_platform_width':4,'interaction':'NOT_RUN','route_semantics':'Closed blocks through route; open-state derivative analyzed separately'},'roof':{'courtyard':'OPEN_SKY_INTENTIONAL','rear_hall_ceiling_y':12,'covered_xz_inclusive':[[9,20],[17,28]]},'landmark':{'type':'front double wooden doors and two-step rise into courtyard, rear high arch, two sealed storage lamp alcoves, framed plaque and brazier','no_side_exits':True},'registration':'PENDING_INDEPENDENT_ACCEPTANCE','runtime_slot':None,'next_image':'S2-08 LOCKED_NOT_STARTED','world_placement':'NOT_RUN','deviations':['Vanilla double door is two blocks tall; reference tall panel approximation','Crates approximated by six empty barrels','Plaque is dark frame with birch center, no map/text','Lit campfire approximates brazier and remains a hazard','Urns approximated by terracotta and flower pots; supported live vines approximate drape','Exterior dune/cactus omitted; open courtyard weather and gameplay not certified']}
    save(TARGET/'room.json',metadata)
    materials=Counter(state.name for state in structure.blocks.values() if state!=AIR)
    assert not materials['minecraft:water'] and not materials['minecraft:lava']
    assert materials['minecraft:barrel']==6 and materials['minecraft:campfire']==1
    assert not structure.entities and len(structure.block_entities)==7
    for data in structure.block_entities.values():
        assert not data.value.get('Items') and not data.value.get('LootTable')
    fragile,lights,vines=[],[],[]
    for point,state in structure.blocks.items():
        east,up,south=point
        if state.name in {'minecraft:flower_pot','minecraft:dead_bush'}:
            below=structure.get((east,up-1,south))
            assert below!=AIR
            if state.name=='minecraft:dead_bush':
                assert below.name=='minecraft:sand' and structure.get((east,up-2,south))!=AIR
            fragile.append(list(point))
        if state.name=='minecraft:vine':
            assert dict(state.properties)['south']=='true' and structure.get((east,up,south+1))!=AIR
            vines.append(list(point))
        if state.name=='minecraft:lantern':
            cap=9 if east in [5,21] else 12
            assert structure.get((east,cap,south))!=AIR
            assert all(structure.get((east,height,south)).name=='minecraft:chain' for height in range(up+1,cap))
            lights.append({'position':list(point),'support_y':cap,'chain_y':list(range(up+1,cap))})
    assert len(fragile)==8 and len(vines)==8 and len(lights)==3
    for east in range(11,15):
        assert structure.get((east,0,0))!=AIR
        assert all(structure.get((east,height,0))==AIR for height in range(1,5))
    for east in range(11,16):
        assert structure.get((east,2,28))!=AIR
        assert all(structure.get((east,height,28))==AIR for height in range(3,9))
    assert structure.get((19,3,15)).name=='minecraft:cut_sandstone'
    assert structure.get((19,4,15)).name=='minecraft:campfire'
    assert all(structure.get((east,16,south))==AIR for east in range(3,24) for south in range(6,18))
    solids={point for point,state in structure.blocks.items() if state!=AIR}
    reached={point for point in solids if point[1]==0}
    pending=deque(reached)
    while pending:
        east,up,south=pending.popleft()
        for neighbor in [(east-1,up,south),(east+1,up,south),(east,up-1,south),(east,up+1,south),(east,up,south-1),(east,up,south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids-reached,sorted(solids-reached)[:20]
    save(TARGET/'materials.json',dict(materials))
    save(TARGET/'qa.json',{'status':'STATIC_PASS_NATIVE_PENDING','sha256':sha,'supported_pots_and_bushes':fragile,'backed_vines':vines,'lantern_connections':lights,'six_face_solids':len(solids),'six_face_connected_to_y0':len(reached),'detached_solids':[],'scope':'Static topology/support only,not physical simulation','liquids':0,'empty_barrels':6,'supported_lit_campfire':[19,4,15],'gate_closed':True,'gate_pairs_support_and_properties':'PASS','north_platform_clear_x':[11,14],'rear_arch_clear_x':[11,15],'world_ticks':'NOT_RUN'})
    save(ROOT/'assembly.json',{'single_template':True,'origin':[0,0,0],'world_origin':None,'bounds_inclusive':[[0,0,0],[26,16,28]],'order':[{'step':1,'path':str(source),'offset':[0,0,0]}],'connections':connections,'hall_feet_y':3,'gate':metadata['gate'],'warning':'North exterior Y1 climbs two blocks to court/rear Y3. Closed two-wide gate blocks passage; four-wide exterior pad is not the gate aperture. Lit campfire is hazardous. Full explicit-air envelope and open courtyard weather/light/spawning require integration review. No deployment permission.'})
    opened=Structure.read(source)
    for point in gate_points:
        state=opened.get(point)
        props=dict(state.properties)
        props['open']='true'
        opened.set(point,State(state.name,tuple(sorted(props.items()))))
    opened_path=ROOT/'analysis-only/incoming/G2B-07/room.nbt'
    opened.write(opened_path)
    open_meta=copy.deepcopy(metadata)
    open_meta.update({'sha256':digest(opened_path),'gate_state':'OPEN_STATE_ANALYSIS','deployable':False,'source_sha256':sha})
    save(opened_path.with_suffix('.json'),open_meta)
    differences=[point for point,state in structure.blocks.items() if opened.get(point)!=state]
    assert set(differences)==set(gate_points)
    save(ROOT/'gate-state-derivation.json',{'source':str(source),'source_sha256':sha,'derived':str(opened_path),'derived_sha256':digest(opened_path),'deployable':False,'exact_changes':[{'position':list(point),'property':'open','before':'false','after':'true'} for point in gate_points],'scope':'Static door-open state analysis,not actual interaction or world synchronization'})
    derivatives=[]
    for direction in ['north','south','west','east']:
        preview=Structure.read(source)
        removed=[]
        for point,state in list(preview.blocks.items()):
            if cut(direction,point) and state!=AIR:
                preview.set(point,AIR)
                removed.append(point)
        path=ROOT/'preview-only'/f'{direction}.nbt'
        preview.write(path)
        check=Structure.read(path)
        changed=set(removed)
        assert all(check.get(point)==state for point,state in structure.blocks.items() if point not in changed)
        derivatives.append({'direction':direction,'source':str(source),'source_sha256':sha,'derived':str(path),'derived_sha256':digest(path),'removed_nonair':len(removed),'deployable':False,'rear_roof_and_upper_caps_cut':'y>=12,z>=20','near_wall_rule':{'north':'y>=3,z<=5','south':'y>=3,z>=20','west':'y>=3,x<=2','east':'y>=3,x>=24'}[direction],'only_declared_air_replacements':True})
    save(ROOT/'derivation.json',{'source_sha256':sha,'exterior':str(source),'derivatives':derivatives})
    route_targets=[{'label':item['facing'],'position':item['position']} for item in connections]+[{'label':item['name'],'position':item['position']} for item in targets]
    route={'group':2,'candidate':str(source),'sha256':sha,'size':list(structure.size),'start':metadata['entrance'],'swimming':False,'targets':route_targets}
    save(ROOT/'qa/route-input.json',[route])
    route.update({'candidate':str(opened_path),'sha256':digest(opened_path)})
    save(ROOT/'qa/open-gate/route-input.json',[route])
    print('S2-07 CLOSED SOURCE '+sha+'; OPEN ANALYSIS '+digest(opened_path))


if __name__=='__main__':
    main()
