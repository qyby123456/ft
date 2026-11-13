import hashlib
import json
import pathlib
import shutil
import sys
import zipfile

ROOT = pathlib.Path(__file__).parent

def read(path):
    return path.read_text(encoding='utf-8-sig')

def load(path):
    return json.loads(read(path))

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

pending = len(sys.argv)>1 and sys.argv[1]=='pending'
if pending:
    previous=ROOT/'supplemental-evidence.json'
    if previous.exists():
        value=load(previous)
        value.update(status='superseded_pending_stricter_natural_run',independent_acceptance=False)
        save(previous,value)
    for name in ['ready-for-review.json','handoff-for-independent-review.json','progress.json']:
        value=load(ROOT/name)
        value.update(status='supplemental_evidence_in_progress_not_accepted',independent_acceptance=False,latest_review_frozen_directory=r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S1-submission-20261107-081456-028610')
        save(ROOT/name,value)
    print('Marked new supplemental evidence in progress; previous frozen geometry retained.')
    raise SystemExit

run = ROOT/(sys.argv[1] if len(sys.argv)>1 else 'worldqa-natural')
interactions = load(ROOT/'worldqa-interactions/interaction-report.json')
natural = load(run/'natural-travel-report.json')
assert interactions['failures']==0 and len(interactions['candidates'])==11
assert natural['status']=='PASS' and len(natural['completed'])==23
assert not natural['watch_chunk_loaded_before_entry'] and natural['natural_loading_hold_ticks']>=600
assert natural['forced_chunks_at_end']==0
assert abs(natural['max_up_step']-.6)<.00001
candidate = ROOT/'merged/s1-mossy-functional-11-20.nbt'
assert natural['candidate_sha256']==sha(candidate)
targets = [entry for candidate_record in interactions['candidates'] for entry in candidate_record['interactions']]
assert len(targets)==76 and all(entry['passed'] and entry['distance']<=4.5 and entry['connected_path_local_feet'] for entry in targets)
inputs={entry['id']:entry for entry in load(ROOT/'worldqa-interactions/inputs.json')}
for candidate_record in interactions['candidates']:
    original=inputs[candidate_record['id']]
    assert candidate_record['source_sha256']==sha(pathlib.Path(original['candidate']))
    design=load(pathlib.Path(original['design']))
    entry=design['start']
    for interaction in candidate_record['interactions']:
        path=interaction['connected_path_local_feet']
        assert path[0]==[entry[0]+.5,entry[1],entry[2]+.5]
        assert path[-1]==interaction['standing_local_feet']
        assert interaction['first_hit_block']==[candidate_record['origin'][axis]+interaction['target'][axis] for axis in range(3)]
        for previous,current in zip(path,path[1:]):
            assert abs(current[0]-previous[0])+abs(current[2]-previous[2])==.5
            assert abs(current[1]-previous[1])<=.5
plan = load(ROOT/'access-package/installation-plan.json')
assert plan['candidate_sha256']==sha(candidate)
assert plan['external_ground_feet']==[9995.5,62,10129.5] and plan['clear_inclusive']['min']==[9992,61,9998]
assert len(plan['chunks'])==33
for chunk in plan['chunks']:
    assert sha(pathlib.Path(chunk['source']))==chunk['sha256']
pack=ROOT/'access-package/s1_access_1_20_1'
with zipfile.ZipFile(ROOT/'access-package/s1_access_1_20_1.zip') as archive:
    expected_files={path.relative_to(pack).as_posix():path for path in pack.rglob('*') if path.is_file()}
    assert set(archive.namelist())==set(expected_files)
    for name,path in expected_files.items():
        assert archive.read(name)==path.read_bytes()
    for chunk in plan['chunks']:
        resource='data/s1/structures/'+chunk['resource'].split(':')[1]+'.nbt'
        assert hashlib.sha256(archive.read(resource)).hexdigest()==chunk['sha256']
source = run/'native-test/src/main/java/net/farmingtales/s1qa/ForestTests.java'
assert 'MoverType' not in read(source) and 'player.move(' not in read(source)
installed=run/'server/world/datapacks/s1_access_1_20_1.zip'
assert sha(installed)==sha(ROOT/'access-package/s1_access_1_20_1.zip')
shutil.copyfile(installed,run/'installed-pack.zip')
evidence_paths=[candidate,ROOT/'interaction-template.java',ROOT/'worldqa-interactions/interaction-report.json',ROOT/'worldqa-interactions/native-test/src/main/java/net/farmingtales/s1qa/ForestTests.java',ROOT/'access-package.py',ROOT/'access-package/s1_access_1_20_1.zip',ROOT/'access-package/installation-plan.json',source,run/'installed-pack.zip',run/'natural-travel-report.json',run/'tests.xml',run/'exit.json']
summary={'status':'maker_supplement_complete_pending_independent_review','independent_acceptance':False,'geometry_unchanged_sha256':sha(candidate),'interaction_candidates':11,'interactions':len(targets),'buttons':sum(entry['kind']=='button' for entry in targets),'containers':sum(entry['kind']=='container' for entry in targets),'maximum_hit_distance':max(entry['distance'] for entry in targets),'natural_waypoints':len(natural['completed']),'elapsed_server_ticks':natural['elapsed_server_ticks'],'natural_run':str(run),'natural_scope':natural['scope'],'source_and_evidence':[{'path':str(path),'sha256':sha(path)} for path in evidence_paths],'prior_collision_move_scope':'Previous worldqa-walk reports explicitly inject vertical displacement and do not prove natural stair/ladder travel. They remain separate historic collision evidence.','interaction_scope':interactions['scope']}
summary.update(max_up_step=natural['max_up_step'],watch_chunk_loaded_before_entry=natural['watch_chunk_loaded_before_entry'],natural_loading_hold_ticks=natural['natural_loading_hold_ticks'],site_forced_chunks_at_end=natural['forced_chunks_at_end'],gametest_spawn_forced_chunks=natural['gametest_spawn_forced_chunks'],external_ground_feet=plan['external_ground_feet'],return_platform_feet=plan['external_pad_feet'],clear_inclusive=plan['clear_inclusive'])
save(ROOT/'supplemental-evidence.json',summary)
for name in ['ready-for-review.json','handoff-for-independent-review.json','progress.json']:
    value=load(ROOT/name)
    value.update(status='ready_for_independent_supplemental_review_not_accepted',independent_acceptance=False,supplemental_evidence=str(ROOT/'supplemental-evidence.json'),installable_access_package=str(ROOT/'access-package/s1_access_1_20_1.zip'),installation_instructions=str(ROOT/'access-package/README.md'),entry_connected_ray_use=str(ROOT/'worldqa-interactions/interaction-report.json'),natural_gravity_travel=str(run/'natural-travel-report.json'))
    if 'limitations' in value:
        value['limitations']=[item for item in value['limitations'] if item!='No ladder climbing or fence jumping physics certification.']
        value['limitations'].append('Natural-gravity proof covers the documented library ladder and watch stair roundtrip only; no human-client, fence-jump or damage certification. Interaction stands have collision-path proof; not every interaction has natural-travel proof.')
    if name=='progress.json':
        value['current']='supplemental_evidence_complete_pending_independent_review'
        value['remaining_checks']=['Independent supplemental review and group acceptance pending.','Human client gameplay, fence jumps, combat, damage and drowning not certified.']
    save(ROOT/name,value)
print('Published supplemental entry, ray/use and natural-gravity evidence; independent acceptance remains false.')
