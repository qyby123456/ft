import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
preparation = work / 'reports/native-trident-structure-diagnostic-preparation-9d1607e8.json'
frozen = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
for path in [old_source, old_launcher]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == frozen['inputHashes'][str(path)]
binding = work / 'reports/native-repair-ranged-use-candidate-5e024943.json'
candidate = json.loads(binding.read_text(encoding='utf-8'))
new_source = work / 'tests/trident-foundation-gate-5e024943/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_foundation_gate_5e024943.py'
launcher = new_launcher.read_text(encoding='utf-8')
compile(launcher, str(new_launcher), 'exec')
assert binding.name in launcher and hashlib.sha256(binding.read_bytes()).hexdigest() in launcher
assert 'actualTridentWholeInstallationCertified' in new_source.read_text(encoding='utf-8')
assert 'inputs.update({str(proposal / name)' in launcher
resources = [Path(value) for value in candidate['requiredProductionResources'].values()]
for path in resources:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == candidate['inputHashes'][str(path)]
paths = [Path(__file__), preparation, old_source, old_launcher, binding, new_source, new_launcher,
         work / 'tools/prepare_native_trident_certified_copy_runtime.py',
         work / 'tools/recover_native_trident_certified_copy_preparation_5e024943.py',
         work / 'tools/prepare_native_trident_foundation_gate_runtime.py', *resources]
output = work / 'reports/native-trident-foundation-gate-preparation-5e024943.json'
assert not output.exists()
output.write_text(json.dumps({'candidate': str(binding), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'fullLifecycleAccepted': False, 'preparationRecovery': 'First generation reached an existing repeated input-save anchor; recovery wrote the missing launcher but failed while constructing the receipt because origin was not in its globals. Existing candidate and observer were checked and preserved; this finishes only the missing preparation receipt.', 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'preparationFinished': True, 'frozenCandidatePreserved': True}))
