import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = root / 'work/session-lease'
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
prior = work / 'reports/native-trident-foundation-gate-preparation-6a38a668.json'
old = json.loads(prior.read_text(encoding='utf-8'))
for name, expected in old['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == expected, name
binding = work / 'reports/native-repair-ranged-use-candidate-af29279a.json'
observer = work / 'tests/trident-foundation-gate-af29279a/PositiveWeapon436.java'
launcher = work / 'tools/run_native_trident_foundation_gate_af29279a.py'
assert all(path.is_file() for path in [binding, observer, launcher])
report = work / 'reports/native-trident-foundation-gate-preparation-af29279a.json'
assert not report.exists()
code = launcher.read_text(encoding='utf-8')
compile(code, str(launcher), 'exec')
assert 'trident-foundation-gate-af29279a/PositiveWeapon436.java' in code
assert hashlib.sha256(binding.read_bytes()).hexdigest() in code
assert 'earlyPaidStateLiveWithObservedSemanticTransition' in observer.read_text(encoding='utf-8')
paths = [Path(__file__), helper, prior, binding, observer, launcher,
         work / 'tools/prepare_native_trident_constructor_cleanup_runtime.py',
         work / 'tools/prepare_native_trident_authoritative_build_runtime.py',
         work / 'tools/prepare_native_trident_certified_copy_runtime.py',
         work / 'tools/prepare_native_trident_foundation_gate_runtime.py',
         work / 'tools/prepare_native_trident_early_observation_6a38a668.py']
result = {'candidate': str(binding), 'newObserver': str(observer), 'newLauncher': str(launcher),
          'preparationFailurePreserved': 'Missing origin in outer inherited namespace after observer/launcher created; existing outputs retained; no JVM had started; generic helper corrected to execute in inherited inner namespace',
          'noExistingOutputOverwritten': True, 'authorityFieldsWritten': False, 'fullLifecycleAccepted': False,
          'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}
namespace['add'](report.relative_to(root).as_posix(), json.dumps(result, indent=2))
print('Recovered preparation receipt without rewriting existing observer or launcher')
