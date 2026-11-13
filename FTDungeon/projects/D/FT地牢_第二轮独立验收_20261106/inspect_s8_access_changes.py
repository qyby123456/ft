import hashlib
import json
from pathlib import Path
from audit_s8_submission import decode, load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
current=root/'access-revision-r4'
prior=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
revision=load(current/'REVISION.json')
ready=load(prior/'ready-for-review.json')
layout=load(prior/'assembly/layout.json')
modules={item['id']:item for item in layout['modules']}
empty={'state':{'Name':'minecraft:air'},'nbt':None}
declared={}
for change in revision['changes']:
    identifier=change['room'];point=tuple(change['pos']);assert (identifier,point) not in declared
    declared[(identifier,point)]=change
assert len(declared)==29
room_changes=[]
for room in ready['rooms']:
    identifier=room['id'];name=Path(room['source']['path']).name
    source=prior/identifier/name
    matches=list((current/identifier).glob('*.nbt'));assert len(matches)==1
    original,before=decode(source);new,after=decode(matches[0]);assert original['size']==new['size'] and original['entities']==new['entities']
    differences=[]
    for point in before.keys()|after.keys():
        first,second=before.get(point,empty),after.get(point,empty)
        if first!=second:
            change=declared[(identifier,point)]
            assert first['state']['Name']=='minecraft:air' and not first['nbt']
            assert second['state']['Name']==change['name'] and second['state'].get('Properties',{})==change['properties'] and not second['nbt']
            differences.append(point)
    assert len(differences)==sum(key[0]==identifier for key in declared)
    if identifier not in ('S8-13','S8-16'):
        assert source.read_bytes()==matches[0].read_bytes()
    room_changes.append({'id':identifier,'added_air_cells':differences,'original_payload_retained':True})
original,before=decode(prior/'assembly/s8-functional-combined.nbt')
new,after=decode(current/'assembly/s8-functional-access-revision.nbt')
assert original['size']==new['size'] and original['entities']==new['entities']
expected={tuple(value+offset for value,offset in zip(point,modules[identifier]['origin'])):change for (identifier,point),change in declared.items()}
actual=[]
for point in before.keys()|after.keys():
    first,second=before.get(point,empty),after.get(point,empty)
    if first!=second:
        assert point in expected and first['state']['Name']=='minecraft:air' and not first['nbt']
        assert second['state']['Name']==expected[point]['name'] and second['state'].get('Properties',{})==expected[point]['properties'] and not second['nbt']
        actual.append(point)
assert len(actual)==29
assert {point for point,item in before.items() if item['state']['Name']=='minecraft:barrier'}=={point for point,item in after.items() if item['state']['Name']=='minecraft:barrier'}
for item in load(root/'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'INDEPENDENT_EXACT29AIR_ADDITIONS_PASS','source_sha256':revision['sha256'],'old_sha256':revision['frozen_sha256'],'rooms':room_changes,'merged_changes':actual,'barrier_set_unchanged':90282,'all_original_nonair_and_be_preserved':True,'remaining':['Newpartpackage installation','New access ladder/bridges necessary natural motion','Adjusted original waypoint explicit acceptance','Visual inspection mechanism and final audit']}
(root/'independent-changes-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in result.items()if key not in ('rooms','merged_changes')},ensure_ascii=False))
