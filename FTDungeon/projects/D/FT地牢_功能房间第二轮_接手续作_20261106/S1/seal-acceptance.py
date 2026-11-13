import hashlib
import json
import pathlib
import shutil

ROOT = pathlib.Path(__file__).parent
REVIEW = pathlib.Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S1-submission-20261107-090919-938453')
SEAL = ROOT / 'accepted-seal-20261107'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

acceptance = load(REVIEW/'ACCEPTANCE.json')
assert acceptance['accepted'] and not acceptance['unresolved_required_items']
assert sha(ROOT/'merged/s1-mossy-functional-11-20.nbt') == acceptance['source_sha256']
assert not SEAL.exists(), 'Preserve existing accepted seal; do not overwrite'
old_manifest = load(ROOT/'delivery-file-manifest.json')
assert len(old_manifest['files'])+len(old_manifest['external_references'])+len(old_manifest['external_tools']) == 915
SEAL.mkdir()
shutil.copyfile(ROOT/'delivery-file-manifest.json', SEAL/'preacceptance-915-file-manifest.json')
assets = sorted(ROOT.glob('[12][0-9]-*/*.nbt')) + [ROOT/'scenery/s1-19-forest-depth.nbt', ROOT/'merged/s1-mossy-functional-11-20.nbt'] + sorted((ROOT/'merged/chunks').glob('*.nbt')) + [ROOT/'access-package/s1_access_1_20_1.zip']
assert len(assets) == 46
records = []
for source in assets:
    relative = source.relative_to(ROOT)
    frozen = REVIEW/relative
    assert sha(source) == sha(frozen), f'Accepted frozen asset mismatch: {relative}'
    target = SEAL/'assets'/relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    assert sha(target) == sha(source)
    records.append({'relative':str(relative),'current':str(source),'sealed':str(target),'sha256':sha(target),'bytes':target.stat().st_size})
for name in ['ACCEPTANCE.md','ACCEPTANCE.json']:
    shutil.copyfile(REVIEW/name, SEAL/name)
seal = {'status':'independently_accepted_sealed_not_deployed','accepted':True,'independent_report':str(REVIEW/'ACCEPTANCE.md'),'independent_report_json':str(REVIEW/'ACCEPTANCE.json'),'accepted_at':acceptance['reviewed_at'],'source_sha256':acceptance['source_sha256'],'assets':records,'asset_count':len(records),'limits':acceptance['limits'],'deployment':acceptance['deployment'],'policy':'Preserve accepted geometry and access ZIP. No further construction, retests or expanded certification without a new explicit request.'}
save(SEAL/'seal-manifest.json',seal)
for name in ['ready-for-review.json','handoff-for-independent-review.json','delivery-index.json','progress.json','supplemental-evidence.json']:
    value=load(ROOT/name)
    value.update(status='independently_accepted_sealed_not_deployed',accepted=True,independent_acceptance=True,independent_acceptance_report=str(REVIEW/'ACCEPTANCE.md'),independent_acceptance_json=str(REVIEW/'ACCEPTANCE.json'),accepted_at=acceptance['reviewed_at'],accepted_seal=str(SEAL/'seal-manifest.json'),deployment=acceptance['deployment'],remaining_required_items=[],acceptance_limits=acceptance['limits'])
    if 'limitations' in value:
        value['limitations']=acceptance['limits']
    if name=='progress.json':
        value.update(current='independently_accepted_and_sealed',merge='independently_accepted',remaining_checks=[])
        for room in value['rooms'].values():
            room.update(independent_acceptance=True,visual_review='independently_accepted',next='sealed; no further work required')
    save(ROOT/name,value)
print('Recorded independent acceptance and sealed 45 NBT files plus exact access ZIP; no geometry, world or tests changed.')
