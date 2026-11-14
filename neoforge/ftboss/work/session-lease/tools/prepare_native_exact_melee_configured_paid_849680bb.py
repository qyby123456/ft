import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
binding = work / 'reports/native-repair-ranged-use-candidate-849680bb.json'
bound = load(binding)
assert bound['classCount'] == 1381 and bound['guardCount'] == 208 and not bound['newClasses']
delta = work / 'reports/native-native-reference-id-source-delta-849680bb.json'
assert load(delta)['failures'] == 0
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = '                    String fallback = moddedReferences && !piece.isEmpty()'
assert source.count(anchor) == 1
source = source.replace(anchor, """                    String fallback = (moddedReferences || cell.state() == net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE
                        && nativeParts.get(slot.ordinal()).count() == 1) && !piece.isEmpty()""")
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-configured-paid-849680bb/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-ranged-use-candidate-41d445cf.json', binding.name)
assert launcher.count('31d7e24e2f1f4fe738214353960e68c5166eec25af3f730bbf70387e9037a6ee') == 1
launcher = launcher.replace('31d7e24e2f1f4fe738214353960e68c5166eec25af3f730bbf70387e9037a6ee', digest(binding))
launcher = launcher.replace('native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-configured-paid-849680bb.json')
preparation = work / 'reports/native-exact-melee-configured-paid-preparation-849680bb.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_configured_paid_849680bb.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, binding, delta, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 19,
    'candidate': str(binding), 'actualOriginalWorldRecipeCaptureRetained': True,
    'noNullWorldDefaultRecipeSubstitutionOrTestMaterialRegistration': True,
    'readOnlyPlannerExactlyMirrorsBoundedNativeIdentityFallback': True,
    'allOriginalInventoryConsumptionThresholdPaymentAndFinalReadinessAssertionsRetained': True,
    'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualConfiguredPaidCasesRequired': 19, 'candidate': '849680bb'}))
