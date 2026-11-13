import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-client-resource-completion-v1'

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with path.open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
base = WORK / 'build/boss-summon-fx-client-assets-v2/ftboss-boss-core-1.21.1-stage13-client-assets2-NOT-INSTALLABLE.jar'
original = ROOT / 'reference/forge-current.jar'
assert sha(base) == '589ad968f2efe15a98a1438ee9d9b7218343c197b69d616ada7da3681bb2e3fe'
assert sha(original) == 'c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
allowed = {'apple_knight', 'blockstates', 'boss_preview', 'compost_zero', 'defectzero', 'dora_native', 'dora_zero', 'fx', 'geo', 'lemon_ritual', 'lemoncat_v3', 'manor', 'manor_run', 'maque', 'maque_arcane', 'models', 'motion_data', 'qingyi', 'qizai_arcane', 'reap_flat', 'sugarqueen', 'textures', 'wineking', 'xuanlan', 'xuanlan_arcane', 'xuanlan_native', 'xueli_arcane', 'xueli_basic', 'youzi_spells', 'zicai_melee'}
assert not OUT.exists()
OUT.mkdir()
jar = OUT / 'ftboss-boss-core-1.21.1-boss-resources-NOT-INSTALLABLE.jar'
restored = {}
excluded = {}
with zipfile.ZipFile(base) as old, zipfile.ZipFile(original) as reference, zipfile.ZipFile(jar, 'x', compression=zipfile.ZIP_DEFLATED) as target:
    missing = sorted(name for name in reference.namelist() if name.startswith('assets/ftboss/') and not name.endswith('/') and name not in old.namelist())
    assert len(missing) == 295
    resources = [name for name in missing if name.split('/')[2] in allowed]
    omitted = sorted(set(missing) - set(resources))
    assert omitted == ['assets/ftboss/weapon_bonds/guide.json'] and len(resources) == 294
    for info in old.infolist():
        target.writestr(info, old.read(info.filename))
    for name in resources:
        payload = reference.read(name)
        if name.endswith('.json') or name.endswith('.mcmeta'):
            json.loads(payload)
        if name.endswith('.png'):
            assert payload.startswith(bytes.fromhex('89504e470d0a1a0a'))
        target.writestr(reference.getinfo(name), payload)
        destination = OUT / 'restored-resources' / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(payload)
        group = name.split('/')[2]
        reason = 'Original registered Boss statue/base-display resources; no weapon growth implementation' if group in {'blockstates', 'models'} or '/textures/block/' in name or '/textures/item/' in name else 'Original Boss model/texture/action/effect resource used by retained client renderers'
        restored[name] = {'sha256': hashlib.sha256(payload).hexdigest(), 'bytes': len(payload), 'originalJar': str(original), 'byteIdenticalToOriginal': True, 'scopeReason': reason}
    for name in omitted:
        excluded[name] = {'sha256': hashlib.sha256(reference.read(name)).hexdigest(), 'reason': 'Weapon bond guide belongs to paused weapon module; deliberately not restored'}
with zipfile.ZipFile(base) as old, zipfile.ZipFile(jar) as current:
    assert current.testzip() is None
    old_names = set(old.namelist())
    assert set(current.namelist()) - old_names == set(restored)
    assert all(current.read(name) == old.read(name) for name in old_names)
    class_count = sum(name.endswith('.class') for name in old_names)
    assert class_count == 1446
save(OUT / 'restored-assets-manifest.json', restored)
save(OUT / 'paused-weapon-resources-excluded.json', excluded)
save(OUT / 'candidate-build-receipt.json', {'jar': str(jar), 'jarSha256': sha(jar), 'baseJar': str(base), 'baseJarSha256': sha(base), 'originalReferenceJar': str(original), 'originalReferenceSha256': sha(original), 'addedResources': sorted(restored), 'changedEntries': [], 'allOriginalEntriesByteIdentical': True, 'unchangedClassCount': class_count, 'original315ChecksAnd66TicksReusedByExactClassByteIdentity': True, 'resourceRestorationOnly': True, 'pausedWeaponGuideExcluded': True, 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'stage03MaterialChangesIncluded': False, 'clientMatrixAccepted': False, 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'inputHashes': {str(Path(__file__)): sha(__file__), str(base): sha(base), str(original): sha(original)}})
print(json.dumps({'jar': str(jar), 'jarSha256': sha(jar), 'restoredOriginalBossResources': len(restored), 'excludedWeaponResources': len(excluded), 'allClassBytesUnchanged': class_count, 'installable': False}), flush=True)
