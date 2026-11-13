import collections
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
BASE = ROOT.parent
PRIOR = BASE / 'S8-rebuild-single-20261104-05'
FIRST = BASE / 'S8-rebuild-single-20261104-01'
ROOM = ROOT / 'incoming/S8R_IMG06'
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-06-corner.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S8-05-ACCEPTANCE.json')
SIZE = [27, 13, 27]
CUTS = {
    'north': [([0,11,0],[26,12,26]),([0,2,0],[26,10,1])],
    'east': [([0,11,0],[26,12,26]),([25,2,0],[26,10,26])],
    'south': [([0,11,0],[26,12,26]),([0,2,25],[26,10,26])],
    'west': [([0,11,0],[26,12,26]),([0,2,0],[1,10,26])],
}
sys.path.insert(0, str(FIRST))
from deliver import load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def save(relative, value):
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def run(label, argv, cwd=ROOT, env=None):
    result = subprocess.run([str(value) for value in argv], cwd=cwd, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=360, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa').mkdir(exist_ok=True)
    (ROOT / f'qa/{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    with (ROOT / 'qa/commands.jsonl').open('a', encoding='utf-8') as stream:
        stream.write(json.dumps({'label':label,'argv':[str(value) for value in argv],'cwd':str(cwd),'exit_code':result.returncode,'inspection_direction':None if env is None else env.get('S8_INSPECTION_DIRECTION')},ensure_ascii=False)+'\n')
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    print(result.stdout[-1000:])


def prepare():
    acceptance = json.loads(ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status']=='ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and Path(acceptance['next_allowed'])==REFERENCE
    assert acceptance['nbt_sha256']==digest(PRIOR / 'incoming/S8R_IMG05/room.nbt')
    run('build',['node',SKILL / 'scripts/run-build.mjs',ROOT / 'recipe.mjs','--out-dir',ROOM,'--data-version','3465'],SKILL)
    connections = [{'role':role,'facing':facing,'position':position,'width':5,'minimum_height':3} for role,facing,position in [('entrance','north',[6,2,0]),('exit','east',[26,2,20])]]
    targets = [{'label':label,'position':position} for label,position in [('north',[6,2,0]),('approach',[6,2,6]),('approach-mid',[6,2,12]),('before-turn',[6,2,16]),('elbow',[6,2,20]),('after-turn',[12,2,20]),('east-mid',[18,2,20]),('east',[26,2,20]),('outer-elbow',[4,2,22]),('inner-elbow',[9,2,18]),('niche-front',[10,2,16]),('west-edge',[4,2,11]),('east-edge',[8,2,11]),('north-edge',[17,2,18]),('south-edge',[17,2,22])]]
    save('incoming/S8R_IMG06/room.json',{'id':'S8R_IMG06','name':'内角灯龛晶脉弯廊','size':SIZE,'entrance':[6,2,0],'interior':[6,2,3],'connections':connections,'route_targets':targets,'reference':str(REFERENCE),'reference_sha256':digest(REFERENCE),'recipe_sha256':digest(ROOT / 'recipe.mjs'),'sha256':digest(ROOM / 'room.nbt'),'previous_acceptance':str(ACCEPTANCE),'mechanisms':[],'loot':None,'runtime_slot':None,'status':'AWAITING_INDEPENDENT_ACCEPTANCE','deployment':'NOT_DEPLOYED'})
    data = load(ROOM / 'room.nbt')
    states = {tuple(block['pos']):data['palette'][block['state']] for block in data['blocks']}
    assert data['size']==SIZE and data['DataVersion']==3465 and not data.get('entities')
    assert len(states)==len(data['blocks'])==27*13*27
    assert all(all(0<=position[axis]<SIZE[axis] for axis in range(3)) for position in states)
    assert all(states[block_x,12,block_z]['Name']=='minecraft:deepslate_tiles' and states[block_x,0,block_z]['Name']!='minecraft:air' for block_x in range(27) for block_z in range(27))
    forbidden = {'water','lava','fire','soul_fire','magma_block','cobweb','powder_snow','spawner','command_block','structure_block','jigsaw','chest'}
    assert all(state['Name'].startswith('minecraft:') and state['Name'].split(':')[1] not in forbidden for state in states.values())
    openings = []
    for position,state in states.items():
        block_x,block_y,block_z = position
        if state['Name']=='minecraft:air' and block_y>=2 and (block_x in (0,26) or block_z in (0,26)):
            assert (block_z==0 and abs(block_x-6)<=2 and block_y<8-max(0,abs(block_x-6)-1)) or (block_x==26 and abs(block_z-20)<=2 and block_y<8-max(0,abs(block_z-20)-1)),position
            openings.append(position)
    clear_cells = {(block_x,block_z) for block_x in range(4,9) for block_z in range(0,23)} | {(block_x,block_z) for block_x in range(4,27) for block_z in range(18,23)}
    for block_x,block_z in clear_cells:
        assert states[block_x,1,block_z]['Name']!='minecraft:air'
        assert all(states[block_x,level,block_z]['Name']=='minecraft:air' for level in range(2,5))
    assert all(states[block_x,level,block_z]['Name']!='minecraft:air' for block_x in range(14,25) for block_z in range(2,14) for level in range(2,11))
    occupied = {position for position,state in states.items() if state['Name']!='minecraft:air'}
    reached = {position for position in occupied if position[1]==0}
    pending = list(reached)
    while pending:
        position = pending.pop()
        for delta in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = tuple(position[axis]+delta[axis] for axis in range(3))
            if adjacent in occupied and adjacent not in reached:
                reached.add(adjacent)
                pending.append(adjacent)
    assert reached==occupied,sorted(occupied-reached)[:20]
    assert not any('nbt' in block for block in data['blocks'])
    save('incoming/S8R_IMG06/materials.json',dict(sorted(collections.Counter(state['Name'] for state in states.values() if state['Name']!='minecraft:air').items())))
    save('qa/static.json',{'status':'PASS','nbt_sha256':digest(ROOM / 'room.nbt'),'size':SIZE,'explicit_volume_cells':len(states),'roof_columns':729,'foundation_complete':True,'all_solid_six_face_connected_to_foundation':True,'declared_perimeter_air':openings,'two_supported_five_wide_three_high_ports':True,'L_center_swept_five_wide_three_high_clear':True,'turns':1,'branches_stairs_block_entities_entities_fluids':0,'world_ticks':'NOT_RUN'})
    save('qa/cut-rules.json',CUTS)


def native():
    template = BASE / 'S8-rebuild-single-20261104-02/tools/CollisionRoutes.java'
    source = template.read_text(encoding='utf-8')
    source = source.replace('static Map<BlockPos, List<AABB>> boxes;','static Map<BlockPos, List<AABB>> boxes;\n    static int rotationWidth = 27;\n    static int rotationDepth = 27;')
    source = source.replace('50 - node.halfZ','rotationDepth * 2 - node.halfZ').replace('next.halfX > 49','next.halfX > rotationWidth * 2 - 1').replace('next.halfZ > 49','next.halfZ > rotationDepth * 2 - 1').replace('next.halfY > 25','next.halfY > 19')
    source = source.replace('24-position.getZ()','rotationDepth-1-position.getZ()').replace('List.of(25,15,25)','List.of(27,13,27)').replace('return 15;','return 13;').replace('S8-02','S8-06')
    source = source.replace('targets.replaceAll((label, target) -> rotate(target));','targets.replaceAll((label, target) -> rotate(target));\n            int oldWidth = rotationWidth; rotationWidth = rotationDepth; rotationDepth = oldWidth;')
    source = source.replace('for (int count = 0; count < turn; count++) port = rotate(port);','for (int count = 0; count < turn; count++) port = new Node((count % 2 == 0 ? 27 : 27) * 2 - port.halfZ, port.halfY, port.halfX);')
    marker = '        Map<String, Node> targets = new LinkedHashMap<>();'
    source = source.replace(marker,'        for (int index = 0; index < blocks.size(); index++) if (blocks.getCompound(index).contains("nbt")) throw new IllegalStateException("Unexpected block entity payload");\n'+marker)
    source = source.replace('report.put("source_nbt_sha256", hash);','report.put("source_nbt_sha256", hash);\n        report.put("supplied_block_entities", List.of());')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/CollisionRoutes.java').write_text(source,encoding='utf-8')
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(mapped),*dependencies])
    (ROOT / 'qa/classes').mkdir(parents=True,exist_ok=True)
    (ROOT / 'qa/classpath.txt').write_text(classpath,encoding='utf-8')
    run('native-compile',[java / 'javac.exe','-encoding','UTF-8','-classpath',classpath,'-d',ROOT / 'qa/classes',ROOT / 'tools/CollisionRoutes.java'])
    run('native-run',[java / 'java.exe','-Xmx2G','-Djava.awt.headless=true','-classpath',str(ROOT / 'qa/classes')+os.pathsep+classpath,'CollisionRoutes',ROOM / 'room.nbt',ROOM / 'room.json',ROOT / 'qa/native.json'])
    assert read('qa/native.json')['status']=='COLLISION_ROUTE_SUPPLEMENT_PASS'
    save('qa/native-provenance.json',{'source_nbt_sha256':digest(ROOM / 'room.nbt'),'java_source_sha256':digest(ROOT / 'tools/CollisionRoutes.java'),'generic_verifier_source':str(template),'generic_verifier_sha256':digest(template),'mapped_jar_sha256':digest(mapped),'classpath_source_sha256':digest(support),'exact_commands':'qa/commands.jsonl','rotation':'rectangular dynamic width/depth; blocks [depth-1-z,y,x], centers [depth-z,y,x]; swap width/depth after each turn; original anchors recomputed with alternating depth27/27','world_placement_400ticks':'NOT_RUN'})


def previews():
    original = load(ROOM / 'room.nbt')
    original_states = {tuple(block['pos']):(original['palette'][block['state']],block.get('nbt')) for block in original['blocks']}
    records = []
    for direction,boxes in CUTS.items():
        run('cutaway-'+direction,['node',SKILL / 'scripts/run-build.mjs',ROOT / 'cutaway.mjs','--out-dir',ROOT / 'views' / direction,'--data-version','3465','--no-block-state-updates'],SKILL,dict(os.environ,S8_INSPECTION_DIRECTION=direction))
        path = ROOT / 'views' / direction / f'inspection-only-{direction}.nbt'
        derived = load(path)
        actual = {tuple(block['pos']):(derived['palette'][block['state']],block.get('nbt')) for block in derived['blocks']}
        expected = dict(original_states)
        for first,last in boxes:
            for block_x in range(first[0],last[0]+1):
                for block_y in range(first[1],last[1]+1):
                    for block_z in range(first[2],last[2]+1):
                        expected[block_x,block_y,block_z]=({'Name':'minecraft:air'},None)
        assert expected==actual
        records.append({'direction':direction,'source_nbt_sha256':digest(ROOM / 'room.nbt'),'derived_nbt':str(path),'derived_sha256':digest(path),'inclusive_air_cut_boxes':boxes,'outside_cuts_all_states_and_nbt_payload_unchanged':True,'changed_positions':sum(actual[position]!=original_states[position] for position in actual),'candidate_eligible':False})
    save('qa/cutaway-provenance.json',{'status':'PASS','records':records})
    run('lodestone-capture',['node',ROOT / 'capture.cjs'])
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',19)
    for mode in ('interior','exterior'):
        sheet = Image.new('RGB',(1400,1140),'#18212b')
        ImageDraw.Draw(sheet).text((12,10),'S8-06 / '+mode+' / 实际Lodestone；独立四向剖切，交付外壳完整',font=font,fill='white')
        for index,direction in enumerate(CUTS):
            with Image.open(ROOT / 'views' / direction / f'{mode}-{direction}.png') as image:
                sheet.paste(image.resize((700,540)),(index%2*700,45+index//2*545))
        sheet.save(ROOT / f'views/{mode}-four-views.png')


def duplicates():
    import takeover_audit as checker
    previous = json.loads((PRIOR / 'qa/material-independent.json').read_text(encoding='utf-8'))
    targets = {Path(item['path']) for item in previous['comparisons']}
    targets.add(PRIOR / 'incoming/S8R_IMG05/room.nbt')
    candidate = checker.describe(ROOM / 'room.nbt')
    records,errors = [],[]
    for target in sorted(targets):
        try:
            other = checker.describe(target)
            records.append({'path':str(target),'sha256':other['sha256'],'size':other['size'],'exact_material_independent_four_rotation_match':bool(candidate['exact']&other['exact']),'interior_solid_iou':round(max(checker.iou(candidate['volume'][0],volume) for volume in other['volume']),4),'dominant_floor_clearance_iou':round(max(checker.iou(candidate['ground'][0],mask) for mask in other['ground']),4),'topology':other['topology']})
        except Exception as error:
            errors.append({'path':str(target),'error':str(error)})
    records.sort(key=lambda item:-item['interior_solid_iou'])
    exact = [item for item in records if item['exact_material_independent_four_rotation_match']]
    high = [item for item in records if item['interior_solid_iou']>=0.9]
    save('qa/material-independent.json',{'status':'SCREENING_PASS' if not exact and not high and not errors else 'NEEDS_REVIEW','source_nbt_sha256':candidate['sha256'],'comparison_count':len(records),'candidate_topology':candidate['topology'],'exact_matches':exact,'interior_iou_over_0_9':high,'errors':errors,'comparisons':records,'method':'Names/states discarded; exact solid coordinate four rotations; normalized 23x15x23 interior volume and31x31 dominant floor clearance. Screening only, no global topology/semantic uniqueness guarantee. Prior failed outputs read-only comparisons.','independent_acceptance':None})
    print('Compared',len(records),'max interior IoU',records[0]['interior_solid_iou'])
    assert not exact and not high and not errors


def package():
    actual = digest(ROOM / 'room.nbt')
    metadata,native_report,static,duplicate,browser,cutaways,visual = [read(name) for name in ['incoming/S8R_IMG06/room.json','qa/native.json','qa/static.json','qa/material-independent.json','qa/lodestone.json','qa/cutaway-provenance.json','qa/visual-review.json']]
    assert actual==metadata['sha256']==native_report['source_nbt_sha256']==static['nbt_sha256']==duplicate['source_nbt_sha256']==visual['source_nbt_sha256']
    assert metadata['recipe_sha256']==digest(ROOT / 'recipe.mjs') and metadata['reference_sha256']==digest(REFERENCE)
    assert static['status']=='PASS' and native_report['status']=='COLLISION_ROUTE_SUPPLEMENT_PASS' and duplicate['status']=='SCREENING_PASS'
    assert visual['status']=='PRODUCER_VISUAL_REVIEW_COMPLETE_AWAITING_INDEPENDENT_ACCEPTANCE'
    assert len(native_report['turns'])==4 and all(len(turn['routes'])==16 and all(route['reachable_without_jump'] for route in turn['routes']) and len(turn['anchors'])==2 and all(port['three_lanes_supported'] and port['three_block_headroom'] for port in turn['anchors']) for turn in native_report['turns'])
    assert not native_report['supplied_block_entities'] and len(browser['records'])==11 and len(cutaways['records'])==4
    assert all(control['status']=='PASS' for control in native_report['positive_negative_controls'])
    for turn_index,turn in enumerate(native_report['turns']):
        assert not turn['stairs']
        for connection,anchor in zip(metadata['connections'],turn['anchors']):
            block_x,feet_y,block_z = connection['position']
            width,depth = 27,27
            for rotation_index in range(turn_index):
                block_x,block_z = depth-1-block_z,block_x
                width,depth = depth,width
            assert anchor['local_block_feet']==[block_x,feet_y,block_z] and 0<=block_x<width and 0<=block_z<depth
        axis_route = next(route for route in turn['routes'] if route['label']=='east')
        assert {point[1] for point in axis_route['full_path_centers']}=={2}
    for item in browser['records']:
        assert item['served_sha256']==digest(ROOT / item['nbt']) and item['screenshot_sha256']==digest(ROOT / item['screenshot']) and not item['page_errors']
        with Image.open(ROOT / item['screenshot']) as image:
            image.verify()
    save('assembly.json',{'overall_size':SIZE,'axis':'+X east,+Y up,+Z south','modules':[{'id':'S8R_IMG06','nbt':str(ROOM / 'room.nbt'),'sha256':actual,'size':SIZE,'offset':[0,0,0],'local_origin':[0,0,0],'placement_order':1,'dependencies':[],'connection_anchors':metadata['connections']}],'split':False,'rotation':'rectangular [depth-1-z,y,x], swap dimensions after each90deg, states and anchors rotate together','rotated_sizes':[[27,13,27]]*4,'world_origin':None,'bounds_policy':'Check full explicit-air volume, borders, fluids and entities; reject occupied placement, never clear world','world_placement_400ticks':'NOT_RUN','deployment':'NOT_DEPLOYED'})
    save('incoming/S8R_IMG06/qa.json',{'status':'PRODUCER_CHECKS_PASS_AWAITING_INDEPENDENT_ACCEPTANCE','sha256':actual,'native_report':str(ROOT / 'qa/native.json'),'static_report':str(ROOT / 'qa/static.json'),'four_rotations_sixteen_targets_each':True,'native_fragile_checks_per_rotation':[len(turn['static_fragile_support']) for turn in native_report['turns']],'lodestone_views':11,'full_final_nbt_corner_detail_views':1,'full_final_nbt_eye_level_views':2,'duplicate_comparison_count':duplicate['comparison_count'],'world_placement_400ticks_neighbor_updates_chunk_reload_forge_gameplay':'NOT_RUN','independent_acceptance':None,'next_image_started':False,'deployment':'NOT_DEPLOYED'})
    save('registration-request.json',{'status':'AWAITING_INDEPENDENT_ACCEPTANCE','group':8,'reference_index':6,'reference':str(REFERENCE),'reference_sha256':digest(REFERENCE),'nbt':str(ROOM / 'room.nbt'),'sha256':actual,'size':SIZE,'metadata':str(ROOM / 'room.json'),'qa':str(ROOM / 'qa.json'),'views':str(ROOT / 'views'),'next_image':'S8-07_NOT_AUTHORIZED_NOT_STARTED','world_placement_400ticks':'NOT_RUN','independent_verdict':None,'deployment':'NOT_DEPLOYED'})
    save('checksums.json',{target.relative_to(ROOT).as_posix():digest(target) for target in sorted(ROOT.rglob('*')) if target.is_file() and target.name!='checksums.json' and '__pycache__' not in target.parts})
    print('PACKAGED S8-06',actual,'AWAITING_INDEPENDENT_ACCEPTANCE')


if __name__=='__main__':
    {'prepare':prepare,'native':native,'previews':previews,'duplicates':duplicates,'package':package}[sys.argv[1]]()

