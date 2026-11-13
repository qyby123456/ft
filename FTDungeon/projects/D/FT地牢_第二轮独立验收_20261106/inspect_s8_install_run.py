import hashlib
import json
from pathlib import Path
from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-install-supplement-20261107-1125'
pointer=load(root/'independent-run.json');assert pointer['exit_code']==0 and pointer['frozen_files_unchanged']==103
runtime=Path(pointer['runtime'])
clear=load(runtime/'clear-result.json');installed=load(runtime/'installation-result.json');access=load(runtime/'access-result.json')
assert clear['seeded_stone_cells']==clear['checked_cells']==1009200 and clear['remaining_non_air']==0
assert installed['source_sha256']=='4d768bcaa073ba63da090ea9b6f64068c474d8a4c4d627217a81803e0c83fd37'
assert installed['full_volume_checked']==1009200 and installed['implicit_air_verified']==809435
assert installed['barriers']==90282 and installed['block_entity_payloads_expected']==installed['block_entity_payloads_verified']==56 and not installed['failures']
assert access['barriers_after']==access['barriers_before']==90282 and len(access['access'])==4
for item in access['access']:
    assert item['destination_correct'] and item['body_collision_free'] and item['supported_floor'] and item['command_result']==1
assert len(load(runtime/'bed-normalization.json'))==12
source=root/'runtime-install-r5/server/world/datapacks'
pack_files=0
for path in source.rglob('*'):
    if path.is_file():
        assert path.read_bytes()==(runtime/'server/world/datapacks'/path.relative_to(source)).read_bytes();pack_files+=1
for item in load(root/'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT_REAL_FULL_INSTALL_AND_ADMIN_HOSTED_ACCESS_PASS','source_sha256':installed['source_sha256'],'frozen_files_unchanged':103,'full_seed_clear_install_cells':1009200,'implicit_air_verified':809435,'block_entities_preserved':56,'barriers_unchanged':90282,'named_player_access_roundtrips':2,'access_operations':4,'legacy_bed_normalizations':12,'pack_files_exact':pack_files,'runtime':str(runtime),'limits':['Administrator hosted functions for self and guest; not autonomous player trigger system','Real server command dispatch using unconnected transport; not live client or multiplayer deployment','Installation runtime neighbor shape differences57 retained in raw result; source payload unchanged']}
(root/'independent-install-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
