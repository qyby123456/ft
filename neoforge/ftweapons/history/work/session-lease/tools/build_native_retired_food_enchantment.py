import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1]
base=work/'tools/build_native_trident_pickup_proof.py'
frozen=json.loads((work/'reports/native-repair-ranged-use-candidate-529ba4c9.json').read_text(encoding='utf-8'))
assert hashlib.sha256(base.read_bytes()).hexdigest()==frozen['inputHashes'][str(base)]
preparation=work/'reports/native-retired-food-enchantment-preparation.json'
prepared=json.loads(preparation.read_text(encoding='utf-8'))
for name,wanted in prepared['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
for row in prepared['sourceChanges']+prepared['resourceChanges']:
    assert hashlib.sha256(Path(row['staged']).read_bytes()).hexdigest()==row['stagedSha256'],row['staged']
result=subprocess.run([sys.executable,'-B',str(base)],cwd=work.parents[1]);assert result.returncode==0
candidate=work/'reports/native-repair-ranged-use-candidate.json';receipt=json.loads(candidate.read_text(encoding='utf-8'));attempt=Path(receipt['attempt']);resources={}
for row in prepared['resourceChanges']:
    source=Path(row['staged']);relative=source.relative_to(work/'proposal-resources');target=attempt/'retired-resources'/relative;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target)
    resources[relative.as_posix()]=str(target)
    for path in [source,target]:receipt['inputHashes'][str(path)]=hashlib.sha256(path.read_bytes()).hexdigest()
receipt['additionalProductionResources']=resources
receipt['retiredFoodEnchantmentPreparation']=str(preparation)
receipt['retiredFoodEnchantmentRuntimeAccepted']=False
for path in [Path(__file__),preparation]:receipt['inputHashes'][str(path)]=hashlib.sha256(path.read_bytes()).hexdigest()
candidate.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'retirementResourcesFrozen':len(resources),'runtimeAccepted':False,'installable':False}))
