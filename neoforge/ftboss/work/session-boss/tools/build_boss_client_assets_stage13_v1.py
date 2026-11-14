import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-summon-fx-client-assets-v1'

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with path.open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
base = WORK / 'build/boss-summon-fx-v2/ftboss-boss-core-1.21.1-stage13-summon-fx-NOT-INSTALLABLE.jar'
original = ROOT / 'reference/forge-current.jar'
assert sha(base) == '60cfcc8fd1ae10e4249b807a8f68c23074d251d091c4e3a804b2be20903d3b6a'
assert sha(original) == 'c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
expected = {'clone_burst', 'clone_fuse', 'crush_lock', 'guard_break', 'guard_hit', 'guard_thread', 'maid_guard', 'pan_charge', 'pan_sweep', 'roll_wake', 'slam_charge', 'slam_impact', 'stun_haze'}
assert not OUT.exists()
OUT.mkdir()
jar = OUT / 'ftboss-boss-core-1.21.1-stage13-client-assets-NOT-INSTALLABLE.jar'
restored = {}
with zipfile.ZipFile(base) as old, zipfile.ZipFile(original) as reference, zipfile.ZipFile(jar, 'x', compression=zipfile.ZIP_DEFLATED) as target:
    resources = sorted(name for name in reference.namelist() if name.startswith('assets/ftboss/xueli_combat/') and not name.endswith('/'))
    assert {Path(name).stem for name in resources if name.endswith('.json')} == expected
    assert len(resources) == 14 and resources[-1].endswith('.json')
    assert sum(name.endswith('.png') for name in resources) == 1
    for info in old.infolist():
        target.writestr(info, old.read(info.filename))
    for name in resources:
        assert name not in old.namelist()
        payload = reference.read(name)
        if name.endswith('.json'):
            value = json.loads(payload)
            assert value['id'] == Path(name).stem
        target.writestr(reference.getinfo(name), payload)
        destination = OUT / 'restored-resources' / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(payload)
        restored[name] = {'sha256': hashlib.sha256(payload).hexdigest(), 'bytes': len(payload), 'originalJar': str(original), 'byteIdenticalToOriginal': True}
with zipfile.ZipFile(base) as old, zipfile.ZipFile(jar) as current:
    assert current.testzip() is None
    old_names = set(old.namelist())
    assert set(current.namelist()) - old_names == set(restored)
    assert all(current.read(name) == old.read(name) for name in old_names)
    class_count = sum(name.endswith('.class') for name in old_names)
save(OUT / 'restored-assets-manifest.json', restored)
save(OUT / 'candidate-build-receipt.json', {'jar': str(jar), 'jarSha256': sha(jar), 'baseJar': str(base), 'baseJarSha256': sha(base), 'originalReferenceJar': str(original), 'originalReferenceSha256': sha(original), 'addedResources': sorted(restored), 'changedEntries': [], 'allOriginalEntriesByteIdentical': True, 'unchangedClassCount': class_count, 'original315ChecksReusedByExactClassByteIdentity': True, 'original315ChecksRerun': False, 'resourceRestorationOnly': True, 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'stage03MaterialChangesIncluded': False, 'installable': False, 'deployed': False, 'inputHashes': {str(Path(__file__)): sha(__file__), str(base): sha(base), str(original): sha(original)}})
print(json.dumps({'jar': str(jar), 'jarSha256': sha(jar), 'restoredOriginalAssets': len(restored), 'allClassBytesUnchanged': class_count, 'installable': False}), flush=True)
