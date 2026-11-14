import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = 'e07f785e'
positive_path = work / ('reports/native-repair-runtime-trident-foundation-gate-' + tag + '.json')
positive = json.loads(positive_path.read_text(encoding='utf-8'))
terminal_path = Path(positive['attempt']) / 'terminal308.json'
terminal = json.loads(terminal_path.read_text(encoding='utf-8'))
assert positive['exitCode'] == positive['probe']['failures'] == 0
assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
pickup = positive['probe']['cases'][-1]['actualPhysicalPickup']
assert pickup['originalPostPickupSaveReturned'] and pickup['sameOriginalObject'] and pickup['sameOriginalWholeImage']
assert pickup['sameGenuineLease'] and pickup['sameGenuineGeneration'] and not pickup['originalGenerationRetired']
for name, wanted in positive['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
old_source = work / 'tests/six-family-paid-restart/PositiveWeapon436.java'
source = old_source.read_text(encoding='utf-8')
assert hashlib.sha256(old_source.read_bytes()).hexdigest() == positive['inputHashes'][str(old_source)] if str(old_source) in positive['inputHashes'] else True
source = source.replace('for (int sample = 0; sample < ids.size(); sample++) {', 'for (int sample : new int[]{4}) {')
source = source.replace('ticket.getLong("LeaseGeneration") != 1)', 'ticket.getLong("LeaseGeneration") != 3)')
source = source.replace('longValue() != 1L', 'longValue() != 3L').replace('longValue() != 2L', 'longValue() != 4L')
source = source.replace('newTicket.getLong("LeaseGeneration") != 2L', 'newTicket.getLong("LeaseGeneration") != 4L')
source = source.replace('generation-one save', 'generation-three post-pickup save').replace('same epoch one to two', 'same epoch three to four')
source = source.replace('"sameEpochGenerationOneToTwo"', '"sameEpochGenerationThreeToFour"')
source = source.replace('"actualOriginalGenerationTwoSaveCommitted"', '"actualOriginalGenerationFourSaveCommitted"')
source = source.replace('"sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess"', '"paidTridentPostPickupContinuationAcrossNewProcess"')
anchor = '                Object successor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));'
assert source.count(anchor) == 1
source = source.replace(anchor, '                if (actual.getDamageValue() != 1) throw new AssertionError("Original post-pickup wear was not restored exactly");\n' + anchor)
new_source = work / ('tests/trident-post-pickup-restart-' + tag + '/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(), source)
old_launcher = work / ('tools/run_native_trident_foundation_gate_' + tag + '.py')
launcher = old_launcher.read_text(encoding='utf-8')
anchor = "seed_receipt = WORK / 'reports/native-repair-runtime-six-family-paid-restart.json'"
assert launcher.count(anchor) == 1
launcher = launcher.replace(anchor, "seed_receipt = WORK / 'reports/native-repair-runtime-trident-foundation-gate-" + tag + ".json'")
anchor = "assert seed_result['probe']['sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess']\nassert len(seed_result['probe']['cases']) == 6\nassert all(case['actualOriginalGenerationTwoSaveCommitted'] for case in seed_result['probe']['cases'])"
assert launcher.count(anchor) == 1
launcher = launcher.replace(anchor, "assert seed_result['probe']['cases'][-1]['id'] == 'minecraft:trident'\nassert seed_result['probe']['cases'][-1]['actualPhysicalPickup']['originalPostPickupSaveReturned']\nassert seed_result['probe']['cases'][-1]['actualPhysicalPickup']['sameOriginalWholeImage']\nassert not seed_result['probe']['cases'][-1]['actualPhysicalPickup']['originalGenerationRetired']")
launcher = launcher.replace('tests/trident-foundation-gate-' + tag + '/PositiveWeapon436.java', new_source.relative_to(work).as_posix())
launcher = launcher.replace("output = WORK / 'reports/native-repair-runtime-trident-foundation-gate-" + tag + ".json'", "output = WORK / 'reports/native-repair-runtime-trident-post-pickup-restart-" + tag + ".json'")
anchor = "save(attempt / 'inputs-before308.json', inputs)"
head, tail = launcher.rsplit(anchor, 1)
launcher = head + "inputs[str(WORK / 'reports/native-trident-post-pickup-restart-preparation-e07f785e.json')] = digest(WORK / 'reports/native-trident-post-pickup-restart-preparation-e07f785e.json')\n" + anchor + tail
new_launcher = work / ('tools/run_native_trident_post_pickup_restart_' + tag + '.py')
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), helper, positive_path, terminal_path, old_source, old_launcher, new_source, new_launcher]
output = work / ('reports/native-trident-post-pickup-restart-preparation-' + tag + '.json')
with output.open('x', encoding='utf-8') as stream:
    json.dump({'positiveRuntimeReceipt': str(positive_path), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'originalWorldModified': False, 'authorityFieldsWritten': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({'prepared': output.name, 'requiresNewProcessObservation': True}))
