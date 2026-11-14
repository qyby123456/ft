import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
receipt = work / 'reports/native-repair-runtime-six-family-census.json'
runtime = json.loads(receipt.read_text(encoding='utf-8'))
attempt = Path(runtime['attempt'])
terminal = json.loads((attempt / 'terminal308.json').read_text(encoding='utf-8'))
assert runtime['exitCode'] == runtime['probe']['failures'] == 0 and terminal['stopSent'] and terminal['completeSave']
probe = runtime['probe']
assert probe['providerObservationAndRawRegistryVersionUnchanged'] and not probe['leaseOrRepairTaskIssued']
assert set(probe['familyCounts']) == {'sword', 'bow', 'crossbow', 'axe', 'trident', 'staff'}
assert sum(probe['familyCounts'].values()) == len(probe['items'])
staff = [item for item in probe['items'] if item['family'] == 'staff']
assert len(staff) == 9
native_staff = [item for item in staff if item['nativeDamageable']]
assert [item['id'] for item in native_staff] == ['ftboss:frost_queen_staff']
assert all(not item['curiosRegistryContainsItem'] and not item['implementsCurioItem']
           and len(item['providers']) == 1 and item['providers'][0]['originalCuriosRegistrationIdentity'] for item in staff)
hashes = dict(runtime['inputHashes'])
for path in [receipt, attempt / 'terminal308.json', attempt / 'runtime308.log', attempt / 'server/actual-storage-view308.json', Path(__file__)]:
    hashes[str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
for name, wanted in hashes.items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
output = work / 'reports/native-six-family-registered-census-evidence.json'
assert not output.exists()
output.write_text(json.dumps({
    'runtimeAttempt': str(attempt), 'familyCounts': probe['familyCounts'],
    'registeredRetainedItemCount': len(probe['items']),
    'staffNativeDamageableIds': [item['id'] for item in native_staff],
    'staffWithoutOriginalNativeDurability': [item['id'] for item in staff if not item['nativeDamageable']],
    'allNineStaffHaveOnlyOriginalInertCuriosRegistrationIdentity': True,
    'defaultStaffEvolutionEligibilityIsNotBoundProfileEligibility': True,
    'actualProviderGetCapabilityInvoked': False, 'genuineLeaseOrRepairTaskIssued': False,
    'censusIsNotSixFamilyRepairOrCombatAcceptance': True,
    'authorityWrittenByReflection': False, 'fullLifecycleAccepted': False, 'installable': False,
    'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'items': len(probe['items']), 'familyCounts': probe['familyCounts'], 'hashes': len(hashes), 'failures': 0}))
