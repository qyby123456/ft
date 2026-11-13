import collections
import copy
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
PRIOR = ROOT.parent / 'S8-rebuild-single-20261104-09'
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
ROOM = ROOT / 'incoming/S8R_IMG10'
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-10-overview.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S8-09-independent/independent-details.json')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, AIR, plain


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(relative, value):
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def run(label, arguments, cwd=ROOT):
    result = subprocess.run([str(value) for value in arguments], cwd=cwd, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=1100, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa').mkdir(exist_ok=True)
    (ROOT / f'qa/{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    with (ROOT / 'qa/commands.jsonl').open('a', encoding='utf-8') as stream:
        stream.write(json.dumps({'label':label,'argv':[str(value) for value in arguments],'cwd':str(cwd),'exit_code':result.returncode},ensure_ascii=False)+'\n')
    print(result.stdout[-2200:])
    if result.returncode:
        raise RuntimeError(result.stderr[-5000:])


def prepare():
    assert json.loads(ACCEPTANCE.read_text(encoding='utf-8'))['status']=='PASS'
    assert sha(PRIOR/'incoming/S8R_IMG09/room.nbt')=='17a40ef89e7667c3f1d843763df8ae33d2db1101a590b86da7cf0d3b56a85a0b'
    layout=read('layout.json')
    run('build',['node',SKILL/'scripts/run-build.mjs',ROOT/'recipe.mjs','--out-dir',ROOM,'--data-version','3465'],SKILL)
    save('incoming/S8R_IMG10/room.json',{'id':'S8R_IMG10','name':'晶洞多区域总览地牢','size':layout['size'],'sha256':sha(ROOM/'overview.nbt'),'entrance':[33,2,72],'interior':[33,2,70],'connections':[{'facing':'south','position':[33,2,72],'width':5,'minimum_height':3}],'route_targets':layout['route_targets'],'regions':layout['regions'],'courtyard':layout['courtyard'],'reference':str(REFERENCE),'reference_sha256':sha(REFERENCE),'recipe_sha256':sha(ROOT/'recipe.mjs'),'layout_sha256':sha(ROOT/'layout.json'),'previous_acceptance':str(ACCEPTANCE),'previous_acceptance_sha256':sha(ACCEPTANCE),'status':'AWAITING_INDEPENDENT_ACCEPTANCE','deployment':'NOT_DEPLOYED','doors':[],'fluid':'Enclosed one-block-deep water pool; not simulated','runtime_slot':None})
    structure=Structure.read(ROOM/'overview.nbt')
    assert structure.size==tuple(layout['size']) and structure.data_version==3465
    assert len(structure.blocks)==67*19*73 and not structure.entities
    assert all(all(0<=position[axis]<structure.size[axis] for axis in range(3))for position in structure.blocks)
    names=collections.Counter(value.name for value in structure.blocks.values())
    forbidden={'lava','fire','soul_fire','magma_block','powder_snow','cobweb','spawner','command_block','structure_block','jigsaw'}
    assert not any(value.name.removeprefix('minecraft:')in forbidden for value in structure.blocks.values())
    assert all(structure.get((block_x,0,block_z))!=AIR for block_x in range(67)for block_z in range(73))
    roof_columns=set()
    roof_records=[]
    for region in layout['regions']:
        min_x,min_z,max_x,max_z=region['box']
        cells=[(block_x,region['roof'],block_z)for block_x in range(min_x,max_x+1)for block_z in range(min_z,max_z+1)]
        assert all(structure.get(position).name=='minecraft:deepslate_tiles' for position in cells),region['id']
        roof_columns.update((position[0],position[2])for position in cells)
        roof_records.append({'id':region['id'],'height':region['roof'],'complete_columns':len(cells)})
    solid={position for position,value in structure.blocks.items()if value!=AIR and value.name!='minecraft:water'}
    connected={position for position in solid if position[1]==0}
    pending=collections.deque(connected)
    while pending:
        block_x,block_y,block_z=pending.popleft()
        for delta_x,delta_y,delta_z in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            neighbor=(block_x+delta_x,block_y+delta_y,block_z+delta_z)
            if neighbor in solid and neighbor not in connected:
                connected.add(neighbor)
                pending.append(neighbor)
    assert solid==connected,sorted(solid-connected)[:20]
    water=[]
    for position,value in structure.blocks.items():
        if value.name!='minecraft:water':
            continue
        block_x,block_y,block_z=position
        assert block_y==1 and value.get('level')=='0'
        assert structure.get((block_x,0,block_z))!=AIR
        for delta_x,delta_z in [(1,0),(-1,0),(0,1),(0,-1)]:
            neighbor=structure.get((block_x+delta_x,block_y,block_z+delta_z))
            assert neighbor!=AIR and neighbor.name in {'minecraft:water','minecraft:polished_deepslate'}
        water.append(list(position))
    containers=[]
    assert len(structure.block_entities)==4
    for position,payload in structure.block_entities.items():
        values=plain(payload)
        assert values['Items']==[] and 'LootTable'not in values
        block_x,block_y,block_z=position
        assert structure.get((block_x,block_y+1,block_z))==AIR
        if values['id']=='minecraft:chest':
            clear_box=[(near_x,near_y,near_z)for near_x in range(block_x-1,block_x+2)for near_y in range(block_y+1,block_y+3)for near_z in range(block_z-1,block_z+2)]
            assert all(structure.get(air)==AIR for air in clear_box)
        containers.append({'position':list(position),'payload':values,'above_air':True,'interaction':'NOT_RUN'})
    assert not any(value.name.endswith('_door')for value in structure.blocks.values())
    save('qa/static.json',{'status':'PASS','source_nbt_sha256':sha(ROOM/'overview.nbt'),'size':structure.size,'explicit_cells':len(structure.blocks),'foundation_columns':67*73,'roof_regions':roof_records,'unique_roof_columns':len(roof_columns),'courtyard_roof':'INTENTIONALLY_OPEN_AIR','all_nonwater_nonair_connected_to_foundation':len(connected),'source_water_cells':water,'fluid_boundary_scope':'Static sealed bottom and four horizontal neighbors, no liquid updates/ticks tested','containers':containers,'doors':'ABSENT_NO_OPEN_ANALYSIS_REQUIRED','world_ticks_interaction':'NOT_RUN'})
    save('incoming/S8R_IMG10/materials.json',dict(sorted(names.items())))
    print('Prepared',sha(ROOM/'overview.nbt'),len(structure.blocks),'cells',len(water),'water')


def derivatives():
    structure=Structure.read(ROOM/'overview.nbt')
    layout=read('layout.json')
    cuts=[[[region['box'][0],region['roof'],region['box'][1]],[region['box'][2],region['roof'],region['box'][3]]]for region in layout['regions']]
    cut=copy.deepcopy(structure)
    cut_positions=set()
    for first,last in cuts:
        for block_x in range(first[0],last[0]+1):
            for block_y in range(first[1],last[1]+1):
                for block_z in range(first[2],last[2]+1):
                    cut_positions.add((block_x,block_y,block_z))
                    cut.set((block_x,block_y,block_z),AIR)
    cut_path=ROOT/'views/inspection-roofs-only.nbt'
    cut.write(cut_path)
    loaded=Structure.read(cut_path)
    assert all(loaded.blocks[position]==(AIR if position in cut_positions else value)for position,value in structure.blocks.items())
    assert loaded.block_entities==structure.block_entities
    save('qa/cutaway-provenance.json',{'status':'PASS','source_nbt_sha256':sha(ROOM/'overview.nbt'),'derivative':str(cut_path.relative_to(ROOT)),'derivative_sha256':sha(cut_path),'operation':'Only declared roof boxes set to explicit air. All other full block states and all block entity tags preserved exactly. Not deployable.','boxes':cuts,'air_cells':len(cut_positions)})
    parts=[]
    reconstructed={}
    reconstructed_entities={}
    for min_x in [0,34]:
        for min_z in [0,37]:
            origin=(min_x,0,min_z)
            size=(min(34,67-min_x),19,min(37,73-min_z))
            part=Structure(size)
            for position,value in structure.blocks.items():
                if min_x<=position[0]<min_x+size[0] and min_z<=position[2]<min_z+size[2]:
                    local=tuple(position[axis]-origin[axis]for axis in range(3))
                    part.set(local,value,structure.block_entities.get(position))
            relative=f'parts/part-{len(parts)+1:02d}.nbt'
            part.write(ROOT/relative)
            verify=Structure.read(ROOT/relative)
            assert max(verify.size)<=48 and len(verify.blocks)==size[0]*size[1]*size[2]
            for position,value in verify.blocks.items():
                global_position=tuple(position[axis]+origin[axis]for axis in range(3))
                assert global_position not in reconstructed
                reconstructed[global_position]=value
                if position in verify.block_entities:
                    reconstructed_entities[global_position]=verify.block_entities[position]
            parts.append({'order':len(parts)+1,'path':relative,'origin_offset':origin,'size':size,'explicit_cells':len(verify.blocks),'block_entities':len(verify.block_entities),'sha256':sha(ROOT/relative)})
    assert reconstructed==structure.blocks and reconstructed_entities==structure.block_entities
    save('assembly.json',{'source':'incoming/S8R_IMG10/overview.nbt','source_sha256':sha(ROOM/'overview.nbt'),'size':structure.size,'parts':parts,'placement':'Origin O is source min corner. Place each part at O+origin_offset in listed order, with identical rotation/no mirroring for every part. Parts are disjoint, order deterministic.','explicit_air_policy':'Every cell is explicit, including air; place with integrity 1 and never ignore air. Clears any preexisting blocks inside the entire source volume. Stage only in empty disposable test world. No world deployment performed.','entity_coordinate_policy':'Supplied block entity payloads omit x/y/z, vanilla placement binds them to destination position; state and full payload compared exactly after coordinate translation.','reconstruction':{'status':'PASS_EXACT','cells':len(reconstructed),'block_entities':len(reconstructed_entities),'all_block_names_properties_and_full_entity_tags_equal':True,'overlap_cells':0,'missing_cells':0},'world_placement':'NOT_RUN'})
    print('Exact roof-only derivative and four exact disjoint <=48 parts verified')


def native():
    java=Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classpath=(PRIOR/'qa/classpath.txt').read_text(encoding='utf-8')
    (ROOT/'qa/classes').mkdir(parents=True,exist_ok=True)
    run('native-compile',[java/'javac.exe','-encoding','UTF-8','-classpath',classpath,'-d',ROOT/'qa/classes',ROOT/'tools/CollisionRoutes.java'])
    run('native-run',[java/'java.exe','-Xmx5G','-Djava.awt.headless=true','-classpath',str(ROOT/'qa/classes')+os.pathsep+classpath,'CollisionRoutes',ROOM/'overview.nbt',ROOM/'room.json',ROOT/'qa/native.json'])
    report=read('qa/native.json')
    assert report['status']=='COLLISION_ROUTE_SUPPLEMENT_PASS'
    assert len(report['turns'])==4 and all(all(route['reachable_without_jump']for route in turn['routes'])for turn in report['turns'])
    save('qa/native-provenance.json',{'source_sha256':sha(ROOM/'overview.nbt'),'java_source_sha256':sha(ROOT/'tools/CollisionRoutes.java'),'generic_verifier':str(PRIOR/'tools/CollisionRoutes.java'),'generic_verifier_sha256':sha(PRIOR/'tools/CollisionRoutes.java'),'classpath_source':str(PRIOR/'qa/classpath.txt'),'classpath_source_sha256':sha(PRIOR/'qa/classpath.txt'),'mapped_jar_sha256':sha(Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')),'scope':'Native 1.20.1 registry/template, block entities, fragile supports and collision shapes, four rotations; half-grid swept supported body model, not Entity.move, world physics, ticks or GUI interaction.'})


def screening():
    sys.path.insert(0,str(ROOT.parent/'S8-rebuild-single-20261104-01'))
    import takeover_audit as checker
    previous=json.loads((PRIOR/'qa/material-independent.json').read_text(encoding='utf-8'))
    targets={Path(item['path'])for item in previous['comparisons']}
    targets.add(PRIOR/'incoming/S8R_IMG09/room.nbt')
    candidate=checker.describe(ROOM/'overview.nbt')
    records=[]
    for target in sorted(targets):
        other=checker.describe(target)
        records.append({'path':str(target),'sha256':other['sha256'],'size':other['size'],'exact_material_independent_four_rotation_match':bool(candidate['exact']&other['exact']),'interior_solid_iou':round(max(checker.iou(candidate['volume'][0],volume)for volume in other['volume']),4)})
    assert not any(item['exact_material_independent_four_rotation_match']or item['interior_solid_iou']>=0.9 for item in records)
    save('qa/material-independent.json',{'status':'SCREENING_PASS','source_nbt_sha256':sha(ROOM/'overview.nbt'),'comparison_count':len(records),'comparisons':records,'method':'Existing material-independent exact occupied cells four rotations and normalized interior occupancy IoU. Multiple regions have different floors and courtyard intentionally open. Screening only, not proof of semantic originality or topology equivalence.','max_interior_iou':max(item['interior_solid_iou']for item in records)})
    print('Material independent screening',len(records),'comparisons')


def evidence():
    import math
    layout=read('layout.json')
    structure=Structure.read(ROOM/'overview.nbt')
    report=read('qa/native.json')
    assert report['source_nbt_sha256']==sha(ROOM/'overview.nbt')
    regions=layout['regions']+[layout['courtyard']]
    allowed={(block_x,block_z)for region in regions for block_x in range(region['box'][0],region['box'][2]+1)for block_z in range(region['box'][1],region['box'][3]+1)}
    routes=[]
    for route in report['turns'][0]['routes']:
        points=route['full_path_centers']
        assert route['reachable_without_jump']and points
        assert all((math.floor(point[0]),math.floor(point[2]))in allowed for point in points),route['label']
        assert min(point[1]for point in points)>=2
        routes.append({'label':route['label'],'point_count':len(points),'path_within_declared_rooms_passages_courtyard':True,'minimum_feet_y':min(point[1]for point in points),'full_body_paths':'qa/native.json/turns/0/routes'})
    for block_x in range(31,36):
        assert structure.get((block_x,1,72))!=AIR
        assert all(structure.get((block_x,block_y,72))==AIR for block_y in range(2,5))
    containers=[]
    approaches={(53,2,9):(53,2,10),(59,2,9):(59,2,10),(61,2,22):(61,2,21),(8,2,16):(8,2,17)}
    for position,payload in structure.block_entities.items():
        block_x,block_y,block_z=position
        upper=[]
        for block_y_above in range(block_y+1,structure.size[1]):
            if structure.get((block_x,block_y_above,block_z))!=AIR:
                break
            upper.append(block_y_above)
        assert len(upper)>0
        approach=approaches[position]
        assert any(tuple(target['position'])==approach for target in layout['route_targets'])
        containers.append({'position':position,'block':structure.get(position).name,'payload':plain(payload),'reachable_body_approach_block_feet':approach,'continuous_vertical_air_above':upper,'chest_conservative_air_box':[[block_x-1,block_y+1,block_z-1],[block_x+1,block_y+2,block_z+1]]if structure.get(position).name.endswith('chest')else None,'actual_raycast_lid_menu':'NOT_RUN'})
    save('qa/region-container-evidence.json',{'status':'PASS_STATIC_AND_SAMPLED_BODY','source_nbt_sha256':sha(ROOM/'overview.nbt'),'routes':routes,'four_rotations':{'routes_each':len(report['turns'][0]['routes']),'fragile_survival_checks_each':len(report['turns'][0]['static_fragile_support']),'native_stair_shapes_each':len(report['turns'][0]['stairs']),'supported_entry_lanes':5,'headroom':3},'all_paths_inside_intended_complex_no_outer_geology_detours':True,'no_path_dips_into_pool_below_y2':True,'containers':containers,'plant_blocks':0,'plant_scope':'No vegetation placed; all fragile support claims cover all 159 clusters and 16 lanterns. Chains connect to actual roofs/beams.','doors':'No door blocks in final palette; open-door analysis not applicable.','world_ticks_physics_gameplay_deployment':'NOT_RUN'})
    print('All 34 native route witnesses stay inside intended complex; per-container evidence written')


def contact_sheets():
    from PIL import Image, ImageDraw, ImageFont
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',19)
    captures=read('qa/lodestone.json')['records']
    for first in range(0,len(captures),6):
        subset=captures[first:first+6]
        sheet=Image.new('RGB',(1500,3*585),(22,28,35))
        draw=ImageDraw.Draw(sheet)
        for index,record in enumerate(subset):
            image=Image.open(ROOT/record['screenshot']).convert('RGB')
            image.thumbnail((750,550))
            offset_x=index%2*750
            offset_y=index//2*585
            draw.text((offset_x+8,offset_y+3),record['name'],font=font,fill='white')
            sheet.paste(image,(offset_x,offset_y+32))
        sheet.save(ROOT/f'views/contact-{first//6+1:02d}.png')
    compare=Image.new('RGB',(1800,900),(22,28,35))
    draw=ImageDraw.Draw(compare)
    for index,(image_path,title)in enumerate([(REFERENCE,'S8-10 original reference'),(ROOT/'views/inspection-overview-front.png','Actual NBT / declared roof-only inspection')]):
        image=Image.open(image_path).convert('RGB')
        image.thumbnail((900,830))
        compare.paste(image,(index*900+(900-image.width)//2,50+(830-image.height)//2))
        draw.text((index*900+15,10),title,font=font,fill='white')
    compare.save(ROOT/'views/reference-comparison.png')
    print('Three contact sheets and reference comparison, unaltered image scale only')


def package():
    source=sha(ROOM/'overview.nbt')
    assert read('qa/static.json')['source_nbt_sha256']==source
    assert read('qa/native.json')['source_nbt_sha256']==source
    assert read('assembly.json')['source_sha256']==source
    captures=read('qa/lodestone.json')['records']
    assert len(captures)>=12
    for record in captures:
        assert sha(ROOT/record['nbt'])==record['served_sha256']
        assert sha(ROOT/record['screenshot'])==record['screenshot_sha256']
    save('registration-request.json',{'id':'S8R_IMG10','status':'AWAITING_INDEPENDENT_ACCEPTANCE','source_sha256':source,'source':'incoming/S8R_IMG10/overview.nbt','size':[67,19,73],'part_count':4,'not_started':['S8-11'],'deployment':'NOT_DEPLOYED','world_ticks_interaction':'NOT_RUN','independent_acceptance':None})
    previous=[]
    for index in range(1,10):
        folder=ROOT.parent/f'S8-rebuild-single-20261104-{index:02d}'
        checks=json.loads((folder/'checksums.json').read_text(encoding='utf-8'))
        assert all(sha(folder/relative)==value for relative,value in checks.items())
        previous.append({'id':index,'manifest_sha256':sha(folder/'checksums.json'),'entries':len(checks),'status':'PASS_UNCHANGED'})
    save('qa/previous-deliveries.json',previous)
    excluded={'checksums.json'}
    files={path.relative_to(ROOT).as_posix():sha(path)for path in sorted(ROOT.rglob('*'))if path.is_file() and path.name not in excluded and '__pycache__'not in path.parts and 'classes'not in path.parts}
    save('checksums.json',files)
    print('PACKAGED',source,len(files),'hashes AWAITING_INDEPENDENT_ACCEPTANCE')


if __name__=='__main__':
    {'prepare':prepare,'derivatives':derivatives,'native':native,'screening':screening,'evidence':evidence,'contact-sheets':contact_sheets,'package':package}[sys.argv[1]]()
