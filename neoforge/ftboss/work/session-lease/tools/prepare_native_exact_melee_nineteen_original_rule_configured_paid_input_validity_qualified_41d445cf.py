import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-nineteen-original-rule-configured-paid-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
failed_path = work / 'reports/native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-41d445cf.json'
failed = load(failed_path)
terminal = Path(failed['attempt']) / 'terminal308.json'
end = load(terminal)
assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1 and end['completeSave'] and end['stopSent']
assert len(failed['probe']['cases']) == 3 and failed['probe']['cases'][-1]['id'] == 'minecraft:stone_sword'
assert 'valid=false' in failed['probe']['failure']
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = '        List<net.minecraft.world.item.Item> materials = new ArrayList<>();'
assert source.count(anchor) == 1
source = source.replace(anchor, '        var validItemId = net.farmingtales.ftboss.util.SwordFunctionRules.class.getDeclaredMethod("validItemId", String.class); validItemId.setAccessible(true);\n' + anchor)
anchor = '        for (var item : materials) definitions.put(item, net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(new ItemStack(item)).orElseThrow());'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + """
        for (var definition : definitions.values()) if (!Boolean.TRUE.equals(validItemId.invoke(null, definition.itemId().toString())))
            throw new AssertionError("Original function rules reject an actual registered replacement item identifier");""")
anchor = '                net.farmingtales.ftboss.util.SwordFunctionRules.Status bestStatus = null;'
assert source.count(anchor) == 1
source = source.replace(anchor, """                java.util.Set<Integer> invalidOriginalInputs = new java.util.HashSet<>();
                for (int inputIndex = 0; inputIndex < originalInputs.size(); inputIndex++) {
                    var input = originalInputs.get(inputIndex);
                    if (input == null || input.present() && (input.material() == null || !Boolean.TRUE.equals(validItemId.invoke(null, input.itemId()))))
                        invalidOriginalInputs.add(inputIndex);
                }
                int invalidOriginalInputCount = invalidOriginalInputs.size();
                row.put("lastActualOriginalInvalidPresentInputCount", invalidOriginalInputCount);
                if (!net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon).valid()) score = -1.0 - invalidOriginalInputCount;
""" + anchor)
anchor = '                        double candidateScore = readinessScore(status);'
assert source.count(anchor) == 1
source = source.replace(anchor, """                        int invalidAfter = invalidOriginalInputCount - (invalidOriginalInputs.contains(slot.ordinal()) ? 1 : 0);
                        double candidateScore = status.valid() ? readinessScore(status) : -1.0 - invalidAfter;""")
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-41d445cf.json', 'native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf.json')
preparation = work / 'reports/native-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_nineteen_original_rule_configured_paid_input_validity_qualified_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, failed_path, terminal, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 19,
    'fixturePlanningOnly': 'If original evaluateItems is invalid, rank strictly fewer invalid actually-present inputs using original caller-independent validItemId read-only; permit original legal intermediate installs without pretending they are function-ready. Actual selected detached/installed status equality and final original readiness remain mandatory.',
    'allOriginalCanReplaceInventoryConsumptionPaymentAndFinalFunctionThresholdAssertionsRetained': True,
    'productChanged': False, 'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'excludedInvalidInitialReferenceMatrix': str(failed_path), 'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'originalInputValidityRulesUnchanged': True}))
