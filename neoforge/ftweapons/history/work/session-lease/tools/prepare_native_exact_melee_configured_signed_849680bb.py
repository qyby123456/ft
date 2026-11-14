import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-wooden-configured-signed-book-qualified-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
binding = work / 'reports/native-repair-ranged-use-candidate-849680bb.json'
assert load(binding)['classCount'] == 1381
paid = work / 'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'
result = load(paid)
terminal = Path(result['attempt']) / 'terminal308.json'
end = load(terminal)
assert result['exitCode'] == result['probe']['failures'] == 0 and end['completeSave'] and end['stopSent']
audit = work / 'reports/native-remaining-variant-gap-audit-3320e085.json'
expected = load(audit)['groups']['otherExactOriginalClassesStillNeedActualMatrix']
assert len(expected) == 19 and [row['id'] for row in result['probe']['cases']] == [row['id'] for row in expected]
assert all(row['originalConfiguredWeaponNotBrokenBeforeAnyPaidAuthority'] and row['actualSuccessorOriginalSaveCommitted'] for row in result['probe']['cases'])
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
assert source.count('for (int sample = 0; sample < 1; sample++) {') == 1
source = source.replace('for (int sample = 0; sample < 1; sample++) {', 'for (int sample = 0; sample < 19; sample++) {')
source = source.replace('oneConfiguredWoodenSwordOriginalAttackGenerationTwoFullSignedSession', 'nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions')
anchor = '                if (actual.getMaxDamage() != 59) throw new AssertionError("Original configured wooden sword maximum damage changed");'
assert source.count(anchor) == 1
maximums = ', '.join(str(row['maxDamage']) for row in expected)
classes = ', '.join(json.dumps(row['class']) for row in expected)
source = source.replace(anchor, '                int expectedMaximum = new int[]{' + maximums + '}[sample];\n'
    + '                String expectedClass = List.of(' + classes + ').get(sample);\n'
    + '                if (actual.getMaxDamage() != expectedMaximum || !actual.getItem().getClass().getName().equals(expectedClass))\n'
    + '                    throw new AssertionError("Original configured exact registered implementation/maximum damage drift");\n'
    + '                row.put("originalExactRegisteredClassAndMaximumPreserved", true);')
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-configured-signed-849680bb/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-ranged-use-candidate-41d445cf.json', binding.name)
assert launcher.count('31d7e24e2f1f4fe738214353960e68c5166eec25af3f730bbf70387e9037a6ee') == 1
launcher = launcher.replace('31d7e24e2f1f4fe738214353960e68c5166eec25af3f730bbf70387e9037a6ee', digest(binding))
anchor = 'reports/native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json'
assert launcher.count(anchor) == 1
launcher = launcher.replace(anchor, paid.relative_to(work).as_posix())
launcher = launcher.replace("assert len(seed_result['probe']['cases']) == 1", "assert len(seed_result['probe']['cases']) == 19")
launcher = launcher.replace("assert seed_result['probe']['cases'][0]['originalMaterialInstallationBeforeAnyAuthorityAndFunctionReady']", "assert all(case['originalConfiguredWeaponNotBrokenBeforeAnyPaidAuthority'] for case in seed_result['probe']['cases'])")
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-configured-signed-book-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-configured-signed-849680bb.json')
preparation = work / 'reports/native-exact-melee-configured-signed-preparation-849680bb.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_configured_signed_849680bb.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, binding, paid, terminal, audit, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 19,
    'candidate': str(binding), 'configuredActualPaidSeed': str(paid),
    'completeExactOriginalStaticOrDynamicSessionReturnCoverageRetained': True,
    'originalActualAttackClassRawMaxDamageWearAndGenuineGenerationAssertionsRetained': True,
    'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualConfiguredPaidSeedCases': 19, 'candidate': '849680bb'}))
