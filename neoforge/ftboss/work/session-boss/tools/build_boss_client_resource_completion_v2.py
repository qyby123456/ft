import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-client-resource-completion-v2'

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
base = WORK / 'build/boss-client-resource-completion-v1/ftboss-boss-core-1.21.1-boss-resources-NOT-INSTALLABLE.jar'
assert sha(base) == '0b0216950927f676b1f243d059eead8d7d0b5813871fd8b31abafefdd31d5060'
assert not OUT.exists()
OUT.mkdir()
jar = OUT / 'ftboss-boss-core-1.21.1-boss-resources2-NOT-INSTALLABLE.jar'
changes = {}
with zipfile.ZipFile(base) as original, zipfile.ZipFile(jar, 'x', compression=zipfile.ZIP_DEFLATED) as current:
    for info in original.infolist():
        payload = original.read(info.filename)
        target = payload
        if info.filename.startswith('assets/ftboss/models/block/statue_') and info.filename.endswith('.json'):
            before = json.loads(payload)
            if before.get('loader') == 'forge:obj':
                assert payload.count(b'"forge:obj"') == 1
                target = payload.replace(b'"forge:obj"', b'"neoforge:obj"')
                after = json.loads(target)
                expected = dict(before)
                expected['loader'] = 'neoforge:obj'
                assert after == expected and len(target) == len(payload) + 3
                changes[info.filename] = {'beforeSha256': hashlib.sha256(payload).hexdigest(), 'afterSha256': hashlib.sha256(target).hexdigest(), 'onlyJsonFieldChanged': 'loader', 'beforeLoader': 'forge:obj', 'afterLoader': 'neoforge:obj'}
                path = OUT / 'resource-overrides' / info.filename
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(target)
        current.writestr(info, target)
assert len(changes) == 21
with zipfile.ZipFile(base) as original, zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and original.namelist() == current.namelist()
    assert all(current.read(name) == original.read(name) for name in original.namelist() if name not in changes)
    class_count = sum(name.endswith('.class') for name in original.namelist())
    assert class_count == 1446 and 'assets/ftboss/weapon_bonds/guide.json' not in current.namelist()
save(OUT / 'model-loader-port-manifest.json', changes)
save(OUT / 'candidate-build-receipt.json', {'jar': str(jar), 'jarSha256': sha(jar), 'baseJar': str(base), 'baseJarSha256': sha(base), 'allOriginalClassBytesIdentical': True, 'allOtherEntriesByteIdentical': True, 'unchangedClassCount': class_count, 'changedResources': sorted(changes), 'onlyLoaderNamespacePorted': True, 'resourceRestorationOnly': True, 'allOriginalEntriesByteIdentical': False, 'pausedWeaponGuideExcluded': True, 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'inputHashes': {str(Path(__file__)): sha(__file__), str(base): sha(base)}})
print(json.dumps({'jar': str(jar), 'jarSha256': sha(jar), 'portedObjLoaderModels': len(changes), 'allClassBytesUnchanged': class_count, 'installable': False}), flush=True)
