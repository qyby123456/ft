import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description='Recheck six weapon scope against existing hashed runtime evidence.')
parser.add_argument('--latest', action='store_true', help='Verify candidate 427 and runtime 428; preserve the archival 414/415 report.')
options = parser.parse_args()
candidate = 427 if options.latest else 414
runtime = 428 if options.latest else 415
binding_batch = '427-native-recipe-stack-binding' if options.latest else '414-food-enchantment-binding'
runtime_batch = '428-native-recipe-stack-runtime' if options.latest else '415-food-enchantment-runtime'
BINDING = ROOT / f'batches/{binding_batch}/reports/actual-incremental{candidate}-experimental.json'
COVERAGE = ROOT / f'batches/{runtime_batch}/reports/actual-coverage{runtime}-read.json'
SEALED = ROOT / f'batches/{runtime_batch}/reports/actual-nonempty{runtime}-sealed-experimental.json'
EXPECTED_RETAINED = [
    'minecraft:iron_sword', 'minecraft:bow', 'minecraft:crossbow',
    'minecraft:iron_axe', 'minecraft:trident', 'irons_spellbooks:staff_of_the_nines',
]
EXPECTED_REJECTED = [
    'minecraft:wooden_hoe', 'minecraft:stone_hoe', 'minecraft:iron_hoe',
    'minecraft:golden_hoe', 'minecraft:diamond_hoe', 'minecraft:netherite_hoe',
    'minecraft:iron_pickaxe', 'minecraft:iron_shovel', 'minecraft:shears',
    'minecraft:shield', 'minecraft:diamond_chestplate', 'minecraft:stick',
]

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

verified = {}
for receipt in [BINDING, SEALED]:
    data = load(receipt)
    failure_key = 'failures' if receipt == BINDING else 'diagnosticFailures'
    assert data[failure_key] == 0, str(receipt)
    for name, expected in {**data.get('inputHashes', {}), **data.get('outputHashes', {})}.items():
        path = Path(name)
        if name not in verified:
            verified[name] = digest(path)
        actual = verified[name]
        assert actual == expected, name
coverage = load(COVERAGE)
assert coverage['exitCode'] == 0
sealed = load(SEALED)
assert sealed['runtimeClassesVerified'] == load(BINDING)['classCount']
scope = coverage['probe']['sixWeaponScope']
assert scope['failures'] == 0
assert scope['retainedSixFamilies'] == EXPECTED_RETAINED
assert scope['rejectedLegacyDataItems'] == EXPECTED_REJECTED
retired = coverage['probe']['retiredFoodEnchantment']
assert sealed['sixWeaponScope'] == scope
assert sealed['retiredFoodEnchantment'] == retired
assert retired['failures'] == 0 and retired['rejectedRequests'] == 176
for key in ['legacyViewsEmpty', 'noFoodWeaponOrExperienceMutation', 'armorRequestsRejected', 'scratchPlayerNotAuthenticated']:
    assert retired[key] is True, key
result = {
    'failures': 0, 'auditDate': '2026-11-10',
    'retainedCategories': ['sword', 'bow', 'crossbow', 'axe', 'trident', 'staff'],
    'retainedRepresentatives': EXPECTED_RETAINED, 'rejectedLegacyDataItems': EXPECTED_REJECTED,
    'rejectedFoodEnchantmentRequests': retired['rejectedRequests'],
    'referencedFilesHashVerified': len(verified),
    'candidateClassCount': load(BINDING)['classCount'],
    'candidateGuardCount': load(BINDING)['guardCount'],
    'candidateBatch': candidate, 'existingRuntimeBatch': runtime,
    'verificationUsesExistingRuntime415': runtime == 415, 'newRuntimeExecuted': False,
    'allThirdPartyVariantsAccepted': False,
    'protocolAndResourceResidueRemovalComplete': False,
    'fullMigrationAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [BINDING, COVERAGE, SEALED, Path(__file__)]},
}
suffix = '-428' if options.latest else ''
target = ROOT / f'reports/six-weapon-scope-audit-20261110{suffix}.json'
target.write_text(json.dumps(result, ensure_ascii=False, indent=2) + chr(10), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key != 'inputHashes'}, ensure_ascii=False))
