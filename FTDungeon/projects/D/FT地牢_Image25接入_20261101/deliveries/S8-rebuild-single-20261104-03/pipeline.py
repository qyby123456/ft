import collections
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
BASE = ROOT.parent
ROOM = ROOT / 'incoming/S8R_IMG03'
SECOND = BASE / 'S8-rebuild-single-20261104-02'
FIRST = BASE / 'S8-rebuild-single-20261104-01'
AUDIT = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-03-small-room.png')
sys.path.insert(0, str(FIRST))
from deliver import load

CUTS = {
    'north': [([0,11,0],[16,11,16]),([0,2,0],[16,10,1]),([1,10,1],[15,10,1])],
    'east': [([0,11,0],[16,11,16]),([15,2,0],[16,10,16]),([15,10,1],[15,10,15])],
    'south': [([0,11,0],[16,11,16]),([0,2,15],[16,10,16]),([1,10,15],[15,10,15])],
    'west': [([0,11,0],[16,11,16]),([0,2,0],[1,10,16]),([1,10,1],[1,10,15])],
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def save(relative, value):
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def run(label, argv, cwd=ROOT, env=None):
    result = subprocess.run([str(value) for value in argv], cwd=cwd, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=240, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa').mkdir(exist_ok=True)
    (ROOT / f'qa/{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    with (ROOT / 'qa/commands.jsonl').open('a', encoding='utf-8') as stream:
        stream.write(json.dumps({'label': label, 'argv': [str(value) for value in argv], 'cwd': str(cwd), 'exit_code': result.returncode, 'inspection_direction': None if env is None else env.get('S8_INSPECTION_DIRECTION')}, ensure_ascii=False) + '\n')
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    print(result.stdout[-900:])


def prepare():
    acceptance = json.loads((AUDIT / 'S8-02-ACCEPTANCE.json').read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and acceptance['next_allowed'] == 'S8-03-small-room.png only'
    assert acceptance['nbt_sha256'] == digest(SECOND / 'incoming/S8R_IMG02/room.nbt')
    run('build', ['node', SKILL / 'scripts/run-build.mjs', ROOT / 'recipe.mjs', '--out-dir', ROOM, '--data-version', '3465'], SKILL)
    connections = [{'role': 'entrance', 'facing': 'north', 'position': [8,2,0], 'width': 3, 'height': 3}, {'role': 'exit', 'facing': 'east', 'position': [16,2,7], 'width': 3, 'height': 3}]
    targets = [{'label': label, 'position': position} for label, position in [('chest-front', [8,2,7]), ('west-niche-front', [3,2,5]), ('west-rear-niche-front', [3,2,11]), ('south-left-niche-front', [5,2,13]), ('south-right-niche-front', [11,2,13]), ('right-passage', [13,2,9]), ('north', [8,2,0]), ('east', [16,2,7])]]
    save('incoming/S8R_IMG03/room.json', {'id': 'S8R_IMG03', 'name': '单箱壁龛小晶室', 'size': [17,12,17], 'entrance': [8,2,0], 'interior': [8,2,3], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'chest': {'position': [8,3,9], 'explicit_block_entity': True, 'Items': [], 'LootTable': None, 'interaction': 'NOT_RUN'}, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'recipe': str(ROOT / 'recipe.mjs'), 'recipe_sha256': digest(ROOT / 'recipe.mjs'), 'sha256': digest(ROOM / 'room.nbt'), 'previous_acceptance': str(AUDIT / 'S8-02-ACCEPTANCE.json'), 'mechanisms': [], 'runtime_slot': None, 'status': 'AWAITING_INDEPENDENT_ACCEPTANCE', 'deployment': 'NOT_DEPLOYED'})
    data = load(ROOM / 'room.nbt')
    states = {tuple(block['pos']): data['palette'][block['state']] for block in data['blocks']}
    assert data['size'] == [17,12,17] and data['DataVersion'] == 3465
    assert len(states) == len(data['blocks']) == 3468 and not data.get('entities')
    assert all(0 <= position[0] < 17 and 0 <= position[1] < 12 and 0 <= position[2] < 17 for position in states)
    assert all(states[block_x,11,block_z]['Name'] == 'minecraft:deepslate_tiles' and states[block_x,0,block_z]['Name'] != 'minecraft:air' for block_x in range(17) for block_z in range(17))
    forbidden = {'lava','water','fire','soul_fire','magma_block','cobweb','powder_snow','spawner','command_block','structure_block','jigsaw'}
    assert all(state['Name'].removeprefix('minecraft:') not in forbidden for state in states.values())
    openings = []
    for position, state in states.items():
        block_x, block_y, block_z = position
        if state['Name'] == 'minecraft:air' and block_y >= 2 and (block_x in (0,16) or block_z in (0,16)):
            allowed = block_z == 0 and (abs(block_x - 8) <= 1 and block_y <= 5 or block_x == 8 and block_y == 6) or block_x == 16 and 6 <= block_z <= 8 and block_y <= 4
            assert allowed, 'Undeclared perimeter opening ' + str(position)
            openings.append(position)
    for port in connections:
        block_x, feet_y, block_z = port['position']
        for offset in (-1,0,1):
            lane_x, lane_z = (block_x, block_z + offset) if port['facing'] == 'east' else (block_x + offset, block_z)
            assert states[lane_x,feet_y-1,lane_z]['Name'] != 'minecraft:air'
            assert all(states[lane_x,level,lane_z]['Name'] == 'minecraft:air' for level in range(feet_y,feet_y+3))
    occupied = {position for position, state in states.items() if state['Name'] != 'minecraft:air'}
    reached = {position for position in occupied if position[1] == 0}
    pending = list(reached)
    while pending:
        position = pending.pop()
        for delta in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = tuple(position[axis] + delta[axis] for axis in range(3))
            if adjacent in occupied and adjacent not in reached:
                reached.add(adjacent)
                pending.append(adjacent)
    assert reached == occupied
    explicit = [block for block in data['blocks'] if 'nbt' in block]
    assert len(explicit) == 1 and explicit[0]['pos'] == [8,3,9]
    assert explicit[0]['nbt']['id'] == 'minecraft:chest' and explicit[0]['nbt'].get('Items') == [] and 'LootTable' not in explicit[0]['nbt']
    assert all(states[8,level,9]['Name'] == 'minecraft:air' for level in range(4,11))
    save('incoming/S8R_IMG03/materials.json', dict(sorted(collections.Counter(state['Name'] for state in states.values() if state['Name'] != 'minecraft:air').items())))
    save('qa/static.json', {'status': 'PASS', 'nbt_sha256': digest(ROOM / 'room.nbt'), 'volume_unique_in_bounds': True, 'roof_columns': 289, 'foundation_complete': True, 'all_solid_six_face_connected_to_foundation': True, 'all_perimeter_air_declared': openings, 'ports_3x3_supported': True, 'explicit_chest_payloads': 1, 'explicit_empty_items_no_loot': True, 'chest_top_clear': True, 'world_ticks': 'NOT_RUN'})
    save('qa/cut-rules.json', CUTS)


def native():
    template = SECOND / 'tools/CollisionRoutes.java'
    source = template.read_text(encoding='utf-8')
    source = source.replace('50 - node.halfZ', '34 - node.halfZ').replace('> 49', '> 33').replace('> 25', '> 19').replace('24-position.getZ()', '16-position.getZ()')
    source = source.replace('List.of(25,15,25)', 'List.of(17,12,17)').replace('return 15;', 'return 12;').replace('S8-02', 'S8-03')
    marker = '        Map<String, Node> targets = new LinkedHashMap<>();'
    payload = '''        List<Object> suppliedBlockEntities = new ArrayList<>();
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            if (!block.contains("nbt", Tag.TAG_COMPOUND)) continue;
            ListTag position = block.getList("pos", Tag.TAG_INT);
            BlockPos location = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            CompoundTag payload = block.getCompound("nbt").copy();
            payload.putInt("x", location.getX());
            payload.putInt("y", location.getY());
            payload.putInt("z", location.getZ());
            var entity = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(location, states.get(location), payload);
            if (!(entity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) || !chest.isEmpty() || payload.contains("LootTable")) throw new IllegalStateException("Supplied chest entity not empty");
            suppliedBlockEntities.add(Map.of("position", List.of(location.getX(),location.getY(),location.getZ()), "class", entity.getClass().getName(), "isEmpty", chest.isEmpty(), "scope", "Actual supplied NBT BlockEntity.loadStatic; no world open/animation test"));
        }
        if (suppliedBlockEntities.size() != 1) throw new IllegalStateException("Expected one explicit chest entity");
'''
    assert marker in source
    source = source.replace(marker, payload + marker)
    source = source.replace('        report.put("source_nbt_sha256", hash);', '        report.put("source_nbt_sha256", hash);\n        report.put("supplied_block_entities", suppliedBlockEntities);')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/CollisionRoutes.java').write_text(source, encoding='utf-8')
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(mapped), *dependencies])
    (ROOT / 'qa/classes').mkdir(parents=True, exist_ok=True)
    (ROOT / 'qa/classpath.txt').write_text(classpath, encoding='utf-8')
    run('native-compile', [java / 'javac.exe','-encoding','UTF-8','-classpath',classpath,'-d',ROOT / 'qa/classes',ROOT / 'tools/CollisionRoutes.java'])
    run('native-run', [java / 'java.exe','-Xmx2G','-Djava.awt.headless=true','-classpath',str(ROOT / 'qa/classes')+os.pathsep+classpath,'CollisionRoutes',ROOM / 'room.nbt',ROOM / 'room.json',ROOT / 'qa/native.json'])
    report = read('qa/native.json')
    assert report['status'] == 'COLLISION_ROUTE_SUPPLEMENT_PASS'
    save('qa/native-provenance.json', {'source_nbt_sha256': digest(ROOM / 'room.nbt'), 'java_source_sha256': digest(ROOT / 'tools/CollisionRoutes.java'), 'generic_verifier_source': str(template), 'generic_verifier_sha256': digest(template), 'mapped_jar_sha256': digest(mapped), 'classpath_source_sha256': digest(support), 'exact_commands': 'qa/commands.jsonl', 'classpath': 'qa/classpath.txt', 'state_rotation': '[x,y,z]->[16-z,y,x]; body center [x,y,z]->[17-z,y,x]', 'world_placement_400ticks_gameplay': 'NOT_RUN'})


def previews():
    original = load(ROOM / 'room.nbt')
    original_states = {tuple(block['pos']): (original['palette'][block['state']],block.get('nbt')) for block in original['blocks']}
    records = []
    for direction, boxes in CUTS.items():
        run('cutaway-'+direction, ['node',SKILL / 'scripts/run-build.mjs',ROOT / 'cutaway.mjs','--out-dir',ROOT / 'views' / direction,'--data-version','3465','--no-block-state-updates'], SKILL, dict(os.environ,S8_INSPECTION_DIRECTION=direction))
        path = ROOT / 'views' / direction / f'inspection-only-{direction}.nbt'
        derived = load(path)
        actual = {tuple(block['pos']): (derived['palette'][block['state']],block.get('nbt')) for block in derived['blocks']}
        expected = dict(original_states)
        for first, last in boxes:
            for block_x in range(first[0],last[0]+1):
                for block_y in range(first[1],last[1]+1):
                    for block_z in range(first[2],last[2]+1):
                        expected[block_x,block_y,block_z] = ({'Name':'minecraft:air'},None)
        assert expected == actual
        records.append({'direction': direction,'source_nbt_sha256': digest(ROOM / 'room.nbt'),'derived_nbt': str(path),'derived_sha256': digest(path),'inclusive_air_cut_boxes':boxes,'outside_cuts_all_states_and_nbt_payload_unchanged':True,'changed_positions':sum(actual[position]!=original_states[position] for position in actual),'candidate_eligible':False})
    save('qa/cutaway-provenance.json',{'status':'PASS','records':records})
    run('lodestone-capture',['node',ROOT / 'capture.cjs'])
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',19)
    for mode in ('interior','exterior'):
        sheet = Image.new('RGB',(1400,1140),'#18212b')
        ImageDraw.Draw(sheet).text((12,10),'S8-03 / '+mode+' / 实际Lodestone；各向分别剖切、最终外壳完整',font=font,fill='white')
        for index,direction in enumerate(CUTS):
            with Image.open(ROOT / 'views' / direction / f'{mode}-{direction}.png') as image:
                sheet.paste(image.resize((700,540)),(index%2*700,45+index//2*545))
        sheet.save(ROOT / f'views/{mode}-four-views.png')
    shutil.copyfile(ROOT / 'views/north/interior-north.png',ROOM / 'preview.png')


def duplicates():
    import takeover_audit as checker
    previous_report = json.loads((SECOND / 'qa/material-independent.json').read_text(encoding='utf-8'))
    targets = {Path(item['path']) for item in previous_report['comparisons']}
    targets.add(SECOND / 'incoming/S8R_IMG02/room.nbt')
    candidate = checker.describe(ROOM / 'room.nbt')
    records = []
    errors = []
    for target in sorted(targets):
        try:
            other = checker.describe(target)
            record = {'path':str(target),'sha256':other['sha256'],'size':other['size'],'exact_material_independent_four_rotation_match':bool(candidate['exact']&other['exact']),'interior_solid_iou':round(max(checker.iou(candidate['volume'][0],volume) for volume in other['volume']),4),'dominant_floor_clearance_iou':round(max(checker.iou(candidate['ground'][0],mask) for mask in other['ground']),4),'topology':other['topology']}
            records.append(record)
        except Exception as error:
            errors.append({'path':str(target),'error':str(error)})
    records.sort(key=lambda item:-item['interior_solid_iou'])
    exact = [item for item in records if item['exact_material_independent_four_rotation_match']]
    high = [item for item in records if item['interior_solid_iou']>=0.9]
    direct = [item for item in records if Path(item['path']) in (FIRST / 'incoming/S8R_IMG01/room.nbt',SECOND / 'incoming/S8R_IMG02/room.nbt')]
    save('qa/material-independent.json',{'status':'SCREENING_PASS' if not exact and not high and not errors else 'NEEDS_REVIEW','source_nbt_sha256':candidate['sha256'],'comparison_count':len(records),'candidate_topology':candidate['topology'],'previous_two_direct_comparisons':direct,'exact_matches':exact,'interior_iou_over_0_9':high,'errors':errors,'comparisons':records,'method':'Vanilla names/states discarded, four rotations exact full solid coordinates, independent dominant floor,31x31 horizontal clearance,23x15x23 interior solid normalized excluding boundary/foundation/roof. No global graph/semantic uniqueness guarantee. Previous failed outputs are read-only failure comparisons, never candidates.','independent_acceptance':None})
    print('Compared',len(records),'highest interior IoU',records[0]['interior_solid_iou'],'previous two',[(item['size'],item['interior_solid_iou']) for item in direct])
    assert not exact and not high and not errors


def package():
    metadata = read('incoming/S8R_IMG03/room.json')
    native_report = read('qa/native.json')
    static = read('qa/static.json')
    duplicate = read('qa/material-independent.json')
    browser = read('qa/lodestone.json')
    cutaways = read('qa/cutaway-provenance.json')
    visual = read('qa/visual-review.json')
    actual = digest(ROOM / 'room.nbt')
    assert actual == metadata['sha256'] == native_report['source_nbt_sha256'] == static['nbt_sha256'] == duplicate['source_nbt_sha256'] == visual['source_nbt_sha256']
    assert metadata['recipe_sha256']==digest(ROOT / 'recipe.mjs') and metadata['reference_sha256']==digest(REFERENCE)
    assert native_report['status']=='COLLISION_ROUTE_SUPPLEMENT_PASS' and duplicate['status']=='SCREENING_PASS'
    assert static['status']=='PASS' and visual['status']=='PRODUCER_VISUAL_REVIEW_COMPLETE_AWAITING_INDEPENDENT_ACCEPTANCE'
    assert len(native_report['supplied_block_entities'])==1 and native_report['supplied_block_entities'][0]['isEmpty']
    assert len(native_report['turns'])==4 and all(len(turn['routes'])==9 and all(route['reachable_without_jump'] for route in turn['routes']) and all(port['three_lanes_supported'] and port['three_block_headroom'] for port in turn['anchors']) for turn in native_report['turns'])
    assert len(browser['records'])==8 and len(cutaways['records'])==4
    for item in browser['records']:
        assert item['served_sha256']==digest(ROOT / item['nbt']) and item['screenshot_sha256']==digest(ROOT / item['screenshot']) and not item['page_errors']
        with Image.open(ROOT / item['screenshot']) as image:
            image.verify()
    save('assembly.json',{'overall_size':[17,12,17],'axis':'+X east,+Y up,+Z south','modules':[{'id':'S8R_IMG03','nbt':str(ROOM / 'room.nbt'),'sha256':actual,'size':[17,12,17],'offset':[0,0,0],'local_origin':[0,0,0],'rotation_pivot':[8,0,8],'placement_order':1,'dependencies':[],'connection_anchors':metadata['connections']}],'split':False,'rotation':'clockwise [x,y,z]->[16-z,y,x]; states and anchors together','world_origin':None,'bounds_policy':'Check full17x12x17 explicit-air volume, entities, fluids and borders before placement; reject occupied volume, never clear it','world_placement_400ticks':'NOT_RUN','deployment':'NOT_DEPLOYED'})
    save('incoming/S8R_IMG03/qa.json',{'status':'PRODUCER_CHECKS_PASS_AWAITING_INDEPENDENT_ACCEPTANCE','sha256':actual,'native_report':str(ROOT / 'qa/native.json'),'static_report':str(ROOT / 'qa/static.json'),'four_rotations_nine_targets_each':True,'native_supplied_empty_chest_entity':'PASS_BlockEntity.loadStatic_NOT_WORLD_INTERACTION','native_fragile_checks_per_rotation':[len(turn['static_fragile_support']) for turn in native_report['turns']],'lodestone_views':8,'duplicate_comparison_count':duplicate['comparison_count'],'world_placement_400ticks_neighbor_updates_chunk_reload_forge_gameplay':'NOT_RUN','independent_acceptance':None,'next_image_started':False,'deployment':'NOT_DEPLOYED'})
    save('registration-request.json',{'status':'AWAITING_INDEPENDENT_ACCEPTANCE','producer_thread':'01a1a64f-3e44-7c72-ab6d-8d3efa9147a3','group':8,'reference_index':3,'reference':str(REFERENCE),'reference_sha256':digest(REFERENCE),'nbt':str(ROOM / 'room.nbt'),'sha256':actual,'size':[17,12,17],'metadata':str(ROOM / 'room.json'),'qa':str(ROOM / 'qa.json'),'preview':str(ROOM / 'preview.png'),'native':str(ROOT / 'qa/native.json'),'views':str(ROOT / 'views'),'duplicate':str(ROOT / 'qa/material-independent.json'),'next_image':'S8-04_NOT_AUTHORIZED_NOT_STARTED','world_placement_400ticks':'NOT_RUN','independent_verdict':None,'deployment':'NOT_DEPLOYED'})
    save('checksums.json',{target.relative_to(ROOT).as_posix():digest(target) for target in sorted(ROOT.rglob('*')) if target.is_file() and target.name!='checksums.json' and '__pycache__' not in target.parts})
    print('PACKAGED S8-03',actual,'AWAITING_INDEPENDENT_ACCEPTANCE')


if __name__=='__main__':
    {'prepare':prepare,'native':native,'previews':previews,'duplicates':duplicates,'package':package}[sys.argv[1]]()
