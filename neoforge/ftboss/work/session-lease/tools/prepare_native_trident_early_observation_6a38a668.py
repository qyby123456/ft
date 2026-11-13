import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = root / 'work/session-lease'
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
prior = work / 'reports/native-trident-foundation-gate-preparation-6a38a668.json'
frozen = json.loads(prior.read_text(encoding='utf-8'))
for name, expected in frozen['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == expected, name
observer = work / 'tests/trident-foundation-gate-6a38a668/PositiveWeapon436.java'
launcher = work / 'tools/run_native_trident_foundation_gate_6a38a668.py'
source = observer.read_text(encoding='utf-8')
anchor = '                if (!earlyUnchanged) throw new AssertionError("Original early Trident release changed paid authority or image");'
assert source.count(anchor) == 1
replacement = """                Object afterEarlyInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                boolean earlyImageSame = beforeEarlyRelease.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual));
                boolean earlyLeaseSame = leaseMethod.invoke(null, actual) == actualLease;
                boolean earlyGenerationSame = generationMethod.invoke(null, actual) == generation;
                boolean earlyGenerationLive = !Boolean.TRUE.equals(read(generation, "retired"));
                boolean earlyHandSame = player.getMainHandItem() == actual;
                boolean earlyCurrentInputLive = afterEarlyInput != null && !Boolean.TRUE.equals(read(afterEarlyInput, "retired"));
                row.put("earlyImageSame", earlyImageSame);
                row.put("earlyLeaseSame", earlyLeaseSame);
                row.put("earlyGenerationSame", earlyGenerationSame);
                row.put("earlyGenerationLive", earlyGenerationLive);
                row.put("earlyHandSame", earlyHandSame);
                row.put("earlyInputSame", beforeEarlyInput == afterEarlyInput);
                row.put("earlyPreviousInputRetired", beforeEarlyInput == null ? null : read(beforeEarlyInput, "retired"));
                row.put("earlyCurrentInputLive", earlyCurrentInputLive);
                boolean earlyPaidStateLive = earlyImageSame && earlyLeaseSame && earlyGenerationSame && earlyGenerationLive && earlyHandSame && earlyCurrentInputLive;
                row.put("earlyPaidStateLiveWithObservedSemanticTransition", earlyPaidStateLive);
                if (!earlyPaidStateLive) throw new AssertionError("Original early Trident release lost paid state");"""
source = source.replace(anchor, replacement)
new_observer = work / 'tests/trident-early-observation-6a38a668/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_early_observation_6a38a668.py'
code = launcher.read_text(encoding='utf-8')
code = code.replace('tests/trident-foundation-gate-6a38a668/PositiveWeapon436.java', 'tests/trident-early-observation-6a38a668/PositiveWeapon436.java')
code = code.replace('reports/native-repair-runtime-trident-foundation-gate-6a38a668.json', 'reports/native-repair-runtime-trident-early-observation-6a38a668.json')
anchor = "save(attempt / 'inputs-before308.json', inputs)"
before, found, after = code.rpartition(anchor)
assert found
before += "inputs[str(WORK / 'tools/prepare_native_trident_early_observation_6a38a668.py')] = digest(WORK / 'tools/prepare_native_trident_early_observation_6a38a668.py')\n"
code = before + found + after
compile(source, str(new_observer), 'exec') if False else None
compile(code, str(new_launcher), 'exec')
namespace['add'](new_observer.relative_to(root).as_posix(), source)
namespace['add'](new_launcher.relative_to(root).as_posix(), code)
paths = [Path(__file__), helper, prior, observer, launcher, new_observer, new_launcher]
report = {'candidate': frozen['candidate'], 'newObserver': str(new_observer), 'newLauncher': str(new_launcher),
          'earlierExactInputIdentityFailurePreserved': True, 'diagnosticEarlySemanticTransitionOnly': True,
          'authorityFieldsWritten': False, 'fullLifecycleAccepted': False,
          'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}
namespace['add']('work/session-lease/reports/native-trident-early-observation-preparation-6a38a668.json', json.dumps(report, indent=2))
print(json.dumps({'prepared': True, 'observer': new_observer.relative_to(root).as_posix(), 'launcher': new_launcher.relative_to(root).as_posix()}))
