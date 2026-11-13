import hashlib
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
catalog = ROOT.parent.parent / 'catalog'
sys.path.insert(0, str(catalog))
from nbtio import Structure, plain

audit_path = catalog / 'expansion-v2/s8-complete-biome-requirements.json'
audit = json.loads(audit_path.read_text(encoding='utf-8'))
fixtures = []
for record in audit['templates']:
    if record['id'] not in {'S8-02', 'S8-03', 'S8-04', 'S8-07', 'S8-09'}:
        continue
    source = Path(record['source'])
    raw = source.read_bytes()
    assert hashlib.sha256(raw).hexdigest() == record['sha256']
    destination = ROOT / 'src/gametest/resources/data/ftbd_payloadtest/structures' / (record['id'].lower().replace('-', '_') + '.nbt')
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, destination)
    fixtures.append({'id': record['id'], 'path': destination.relative_to(ROOT).as_posix(), 'source': str(source), 'sha256': record['sha256'], 'size': record['size'], 'intendedRole': record['intendedRole'], 'blockEntities': record['blockEntities'], 'blockEntityBlocks': record['blockEntityBlocks']})
result = {'scope': 'Original real-room payload capability fixtures, NOT promoted to catalog manifest or complete-biome acceptance', 'auditSha256': hashlib.sha256(audit_path.read_bytes()).hexdigest(), 'fixtures': fixtures}
(ROOT / 'fixtures.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
candidate = catalog / 'expansion-v2/candidate-manifest.json'
candidate_raw = candidate.read_bytes()
candidate_ready = json.loads((catalog / 'expansion-v2/ready.json').read_text(encoding='utf-8'))
assert hashlib.sha256(candidate_raw).hexdigest() == candidate_ready['candidateManifestSha256']
target_root = ROOT / 'src/gametest/resources/data/ftbd_payloadtest'
shutil.copyfile(candidate, target_root / 'candidate-manifest.json')
for item in json.loads(candidate_raw)['templates']:
    assert item['accepted'] is False
    source = catalog / 'expansion-v2/candidate-resources/data/biomedungeon/structures' / (item['id'] + '.nbt')
    assert hashlib.sha256(source.read_bytes()).hexdigest() == item['sha256']
    shutil.copyfile(source, target_root / 'structures' / (item['id'] + '.nbt'))
restart = ROOT / 'src/gametest/resources/data/ftbd_berestart/structures/s8_03.nbt'
restart.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(target_root / 'structures/s8_03.nbt', restart)
print('Frozen', len(fixtures), 'unchanged real architectural fixtures; no manifest promotion')
