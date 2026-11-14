import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


base_path = work / 'reports/native-melee-paid-signed-combat-focused-evidence-eef72352.json'
base = load(base_path)
hashes = dict(base['inputHashes'])
assert len(hashes) == 900
receipt_path = work / 'reports/native-repair-runtime-melee-foreign-item-eef72352.json'
result = load(receipt_path)
attempt = Path(result['attempt'])
terminal = load(attempt / 'terminal308.json')
assert result['exitCode'] == result['probe']['failures'] == 0
assert terminal['stopSent'] and terminal['completeSave']
assert [case['id'] for case in result['probe']['cases']] == ['minecraft:iron_sword', 'minecraft:iron_axe']
for index, case in enumerate(result['probe']['cases']):
    assert case['copyControl'] == (index == 0)
    assert all(case[key] for key in ['genuineGenerationFiveBeforeControl', 'originalForeignCallbackExactlyOnce', 'originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation', 'foreignOriginalMutationRetiresGenuineAuthority', 'noPrivatePermissionOrAuthorityReissued'])
preparation = work / 'reports/native-melee-foreign-item-preparation-eef72352.json'
for source in [load(preparation), result]:
    for name, wanted in source['inputHashes'].items():
        assert name not in hashes or hashes[name] == wanted, name
        hashes[name] = wanted
for name, wanted in hashes.items():
    assert digest(name) == wanted, name
paths = [Path(__file__), base_path, receipt_path, preparation]
paths.extend(attempt / name for name in ['terminal308.json', 'runtime308.log', 'live-process308.json', 'server/actual-storage-view308.json', 'restart-world-seed.json', 'compile-command308.json', 'compile308.log'])
world = attempt / 'server/world'
paths.extend([world / 'level.dat', world / 'data/ftboss_native_weapon_wear.dat', *sorted((world / 'ftboss-authority').rglob('*.bin')), *sorted((world / 'playerdata').glob('*.dat'))])
for path in paths:
    hashes[str(path)] = digest(path)
assert not [path for directory in ['ftboss-authority', 'playerdata'] for path in (world / directory).rglob('*') if path.is_file() and path.suffix in ['.tmp', '.pending']]
output = work / 'reports/native-melee-paid-signed-combat-controls-focused-evidence-eef72352.json'
assert not output.exists()
output.write_text(json.dumps({'positiveCombatAndNewProcessEvidence': str(base_path), 'foreignOriginalItemControlReceipt': str(receipt_path), 'foreignCopyCannotDuplicateAuthorityAndRetiresOriginalGenerationFive': True, 'foreignPostCannotBorrowOriginalWearPermissionAndRetiresOriginalGenerationFive': True, 'foreignOriginalExceptionsPreserveIdentityWithoutDataOrAuthorityChange': True, 'foreignCallbacksExecuteExactlyOnceAndCreateNoPrivateScope': True, 'productCandidateUnchanged': True, 'authorityWrittenByReflection': False, 'completeSixFamilySignedCombatAccepted': False, 'fullLifecycleAccepted': False, 'authenticatedMultiplayerAccepted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'hashes': len(hashes), 'focusedFailures': 0, 'output': str(output)}))
