import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-variants-signed-qualified-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
paid = work / 'reports/native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json'
result = load(paid)
terminal = Path(result['attempt']) / 'terminal308.json'
end = load(terminal)
assert result['exitCode'] == result['probe']['failures'] == 0 and end['completeSave'] and end['stopSent']
assert len(result['probe']['cases']) == 1
row = result['probe']['cases'][0]
assert row['id'] == 'minecraft:wooden_sword' and row['originalMaterialInstallationBeforeAnyAuthorityAndFunctionReady']
assert row['originalConfiguredWeaponNotBrokenBeforeAnyPaidAuthority'] and row['actualSuccessorOriginalSaveCommitted']
evidence = work / 'reports/native-exact-melee-wooden-configured-paid-focused-evidence-41d445cf.json'
assert load(evidence)['failures'] == 0
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = 'for (int sample = 0; sample < 19; sample++) {'
assert source.count(anchor) == 1
source = source.replace(anchor, 'for (int sample = 0; sample < 1; sample++) {')
source = source.replace('nineteenExactOriginalMeleeVariantsGenerationTwoSignedSessions', 'oneConfiguredWoodenSwordOriginalAttackGenerationTwoFullSignedSession')
anchor = '                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + """
                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual)
                    || net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual))
                    throw new AssertionError("Original installed material configuration was not preserved combat-ready on genuine paid reload");
                row.put("actualPaidReloadRetainedOriginalInstalledFunctionReadyConfiguration", true);
                row.put("originalConfiguredMaximumDamage", actual.getMaxDamage());
                if (actual.getMaxDamage() != 59) throw new AssertionError("Original configured wooden sword maximum damage changed");""")
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-wooden-configured-signed-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
anchor = 'reports/native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json'
assert launcher.count(anchor) == 1
launcher = launcher.replace(anchor, paid.relative_to(work).as_posix())
assert launcher.count("assert len(seed_result['probe']['cases']) == 19") == 1
launcher = launcher.replace("assert len(seed_result['probe']['cases']) == 19", "assert len(seed_result['probe']['cases']) == 1\nassert seed_result['probe']['cases'][0]['originalMaterialInstallationBeforeAnyAuthorityAndFunctionReady']")
launcher = launcher.replace('native-repair-runtime-exact-melee-variants-signed-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-wooden-configured-signed-41d445cf.json')
preparation = work / 'reports/native-exact-melee-wooden-configured-signed-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_wooden_configured_signed_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, paid, terminal, evidence, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1,
    'configuredActualPaidSeed': str(paid), 'productChanged': False, 'authorityFieldsWritten': False,
    'originalAttackAndFullSessionAssertionsRetained': True, 'authenticatedMultiplayerAccepted': False,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualConfiguredPaidSeedCases': 1}))
