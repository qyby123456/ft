import hashlib
import json
from pathlib import Path
from audit_s8_submission import load
workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
destination=workspace/'s8-independent-recipes-20261107-1335'
ready=load(root/'ready-for-review.json')
outputs=[root/room['id']/Path(room['source']['path']).name for room in ready['rooms']]
outputs += [root/'assembly/s8-functional-combined.nbt',*sorted((root/'assembly/parts').glob('*.nbt'))]
assert len(outputs)==31
for original in outputs:
    assert original.read_bytes()==(destination/original.relative_to(root)).read_bytes()
for name in ('submission-snapshot.json','supplemental-snapshot.json'):
    for item in load(root/name)['files']:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT31NBT_RECIPE_OUTPUTS_BINARY_EXACT','room_outputs':10,'merged_and_parts':21,'all31_binary_equal':True,'candidate_nbt_used_as_recipe_input':False,'directory':str(destination),'recovery':'Ten recipes and merged builder completed, initial checker found parts absent because builder needs separate verify.mjs stage. Added and executed frozen verify.mjs only; all31 persisted outputs independently compared.'}
(root/'independent-reproduction.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
