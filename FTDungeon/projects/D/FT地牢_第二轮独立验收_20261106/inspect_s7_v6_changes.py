import hashlib
import json
import sys
from pathlib import Path

from audit_s8_submission import decode


root = Path(sys.argv[1]).resolve()
prior = root.parent / 'S7-submission-20261107-083104-571437'
manifest = json.loads((root / 'ready-for-review.json').read_text(encoding='utf-8'))
snapshot = json.loads((root / 'submission-snapshot.json').read_text(encoding='utf-8'))
paths = {item['source']:Path(item['snapshot']) for item in snapshot['files']}
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
old = json.loads((prior / 'ready-for-review.json').read_text(encoding='utf-8'))
old_paths = {item['source']:Path(item['snapshot']) for item in json.loads((prior / 'submission-snapshot.json').read_text(encoding='utf-8'))['files']}
empty = {'state':{'Name':'minecraft:air'},'nbt':None}
changes = []
for room, previous in zip(manifest['rooms'],old['rooms']):
    assert room['id'] == previous['id']
    source, before = decode(old_paths[previous['candidate']])
    data, after = decode(paths[room['candidate']])
    assert source['size'] == data['size'] and source.get('entities',[]) == data.get('entities',[])
    differing = []
    for position in before.keys() | after.keys():
        first, second = before.get(position,empty),after.get(position,empty)
        if first != second:
            assert first == empty and second['state']['Name'] in ('minecraft:dark_oak_planks','minecraft:dark_oak_slab') and second['nbt'] is None
            assert room['id'] in ('S7-15','S7-16')
            differing.append(dict(position=position,added=second))
    if room['id'] not in ('S7-15','S7-16'):
        assert room['sha256'] == previous['sha256'] and not differing
    changes.append(dict(id=room['id'],changes=differing))
assert [len(item['changes']) for item in changes] == [0,0,0,0,4,8,0,0,0,0]
source, before = decode(old_paths[old['assembly']['candidate']])
data, combined = decode(paths[manifest['assembly']['candidate']])
assert source['size'] == data['size'] == [326,40,151]
assert source.get('entities',[]) == data.get('entities',[]) and len(data.get('entities',[])) == 3
merged_changes = []
for position in before.keys() | combined.keys():
    first,second = before.get(position,empty),combined.get(position,empty)
    if first != second:
        assert first == empty and second['state']['Name'] in ('minecraft:dark_oak_planks','minecraft:dark_oak_slab') and second['nbt'] is None
        merged_changes.append(dict(position=position,added=second))
assert len(merged_changes) == 12 and len(combined) == 201740
assert {point for point,item in before.items() if item['state']['Name']=='minecraft:barrier'} == {point for point,item in combined.items() if item['state']['Name']=='minecraft:barrier'}
rebuilt = {}
entity_count = 0
for part in manifest['assembly']['parts']:
    part_data, cells = decode(paths[part['path']])
    assert all(extent <= 48 for extent in part_data['size'])
    for position,item in cells.items():
        global_position = tuple(value+offset for value,offset in zip(position,part['origin']))
        assert global_position not in rebuilt
        rebuilt[global_position] = item
    entity_count += len(part_data.get('entities',[]))
assert rebuilt == combined and entity_count == 3 and len(manifest['assembly']['parts']) == 15
result = dict(status='PASS_SCOPED',accepted=False,verified_frozen_files=828,source_sha256=manifest['assembly']['sha256'],rooms=changes,merged_changes=merged_changes,all_prior_nonair_payloads_and_entities_retained=True,barrier_set_exactly_unchanged=True,parts_exact_block_payload=True,part_entity_count=entity_count,scope='Independent exact12air-additions and sparse part block/BE reconstruction; full entity positional/equipment reconstruction and current native/world checks still separate')
(root/'independent-changes-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key not in ('rooms','merged_changes')},ensure_ascii=False))
