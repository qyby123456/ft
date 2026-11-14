import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-stage14-resources-only-v1'

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with path.open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
base = WORK / 'releases/boss-core-stage12-bone-crush-20261112/ftboss-boss-core-1.21.1-stage12.jar'
resources = WORK / 'build/boss-client-resource-completion-v2/ftboss-boss-core-1.21.1-boss-resources2-NOT-INSTALLABLE.jar'
assert sha(base) == '58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
assert sha(resources) == '893dd608646c4ba59ef20e6e52e7694901a0cc5641c1910e0396efba0a914a03'
assert not OUT.exists()
OUT.mkdir()
jar = OUT / 'ftboss-boss-core-1.21.1-stage14-resources-UNVERIFIED.jar'
added = {}
with zipfile.ZipFile(base) as original, zipfile.ZipFile(resources) as source, zipfile.ZipFile(jar, 'x', compression=zipfile.ZIP_DEFLATED) as current:
    original_names = set(original.namelist())
    for info in original.infolist():
        current.writestr(info, original.read(info.filename))
    for name in source.namelist():
        if name not in original_names and not name.endswith('/') and not name.endswith('.class'):
            assert name.startswith('assets/ftboss/') and 'weapon_bonds/' not in name
            payload = source.read(name)
            current.writestr(source.getinfo(name), payload)
            added[name] = {'sha256': hashlib.sha256(payload).hexdigest(), 'bytes': len(payload)}
assert len(added) == 314
with zipfile.ZipFile(base) as original, zipfile.ZipFile(jar) as current:
    assert current.testzip() is None
    assert all(original.read(name) == current.read(name) for name in original.namelist())
    original_classes = {name for name in original.namelist() if name.endswith('.class')}
    assert original_classes == {name for name in current.namelist() if name.endswith('.class')}
    assert not any(name.startswith('net/farmingtales/ftboss/butcherskills/ButcherSummonFx') for name in current.namelist())
    assert 'assets/ftboss/weapon_bonds/guide.json' not in current.namelist()
    for name in added:
        if name.endswith('.json'):
            json.loads(current.read(name))
    obj_models = [name for name in added if name.startswith('assets/ftboss/models/block/statue_') and name.endswith('.json') and json.loads(current.read(name)).get('loader') == 'neoforge:obj']
    assert len(obj_models) == 21
save(OUT / 'added-resource-manifest.json', added)
save(OUT / 'candidate-build-receipt.json', {'jar': str(jar), 'jarSha256': sha(jar), 'baseJar': str(base), 'baseJarSha256': sha(base), 'resourceDonorJar': str(resources), 'resourceDonorJarSha256': sha(resources), 'addedResourceCount': len(added), 'portedObjModels': len(obj_models), 'resourceRestorationOnly': True, 'allOriginalClassBytesIdentical': True, 'allOtherEntriesByteIdentical': True, 'unchangedClassCount': len(original_classes), 'stage13ClassesIncluded': False, 'allDeployedStage12EntriesByteIdentical': True, 'weaponGuideExcluded': True, 'weaponDevelopmentPaused': True, 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'inputHashes': {str(Path(__file__)): sha(__file__), str(base): sha(base), str(resources): sha(resources)}})
print(json.dumps({'jar': str(jar), 'sha256': sha(jar), 'addedBossResources': len(added), 'preservedDeployedClassCount': len(original_classes), 'stage13ClassesIncluded': False}), flush=True)
