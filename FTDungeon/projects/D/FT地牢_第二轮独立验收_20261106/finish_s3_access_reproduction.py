import hashlib
import json
from pathlib import Path

from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S3-access-revision-20261107-1103'
destination=workspace/'s3-independent-recipes-access-20261107-1120'
prior=Path(__file__).parent/'S3-submission-20261107-072954-274564'
for number in range(11,21):
    identifier=f's3-{number}'
    actual=destination/'rooms'/identifier/(identifier+'.nbt')
    assert actual.read_bytes()==(root/'rooms'/identifier/(identifier+'.nbt')).read_bytes()
    if number!=16:
        assert actual.read_bytes()==(prior/'independent-reproduction/rooms'/identifier/(identifier+'.nbt')).read_bytes()
assert (destination/'rooms/s3-merged/s3-merged.nbt').read_bytes()==(root/'rooms/s3-merged/s3-merged.nbt').read_bytes()
for part in load(root/'parts/manifest.json')['parts']:
    assert (destination/'parts'/Path(part['path']).name).read_bytes()==(root/'parts'/Path(part['path']).name).read_bytes()
verification=load(destination/'rooms/s3-merged/qa/split-source-verification.json')
assert not verification['parts_failures'] and not verification['source_module_failures']
for item in load(root/'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT_CURRENT16_MERGED25PARTS_EXACT_REPRODUCTION','directory':str(destination),'room16_generated_from_recipe':True,'other_nine_rooms_from_prior_parent_recipe_outputs_verified_exact':True,'current16_and_merged_nbt_not_used_as_generation_input':True,'merged_binary_equal':True,'parts_binary_equal':25,'source_sha256':verification['source_sha256'],'recovery':'Initial splitter generated25parts then failed writing missing QA directory; directory created and only splitter rerun. All persisted generated outputs independently compared here.'}
(root/'independent-reproduction.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
