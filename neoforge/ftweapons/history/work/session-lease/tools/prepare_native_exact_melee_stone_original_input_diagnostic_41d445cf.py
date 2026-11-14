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
failed_path = work / 'reports/native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf.json'
failed = load(failed_path)
terminal = Path(failed['attempt']) / 'terminal308.json'
end = load(terminal)
assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1 and end['completeSave'] and end['stopSent']
assert failed['probe']['cases'][-1]['lastActualOriginalInvalidPresentInputCount'] == 2
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = 'for (int sample = 0; sample < 19; sample++) {'
assert source.count(anchor) == 1
source = source.replace(anchor, 'for (int sample = 2; sample < 3; sample++) {')
source = source.replace('nineteenExactOriginalMeleeVariantsConfiguredPaidRepairAndSameProcessReload', 'oneStoneSwordOriginalInputValidityReadOnlyDiagnostic')
anchor = '                row.put("lastActualOriginalInvalidPresentInputCount", invalidOriginalInputCount);'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + """
                List<Object> invalidDetails = new ArrayList<>(); row.put("actualOriginalInvalidInputDetails", invalidDetails);
                for (int inputIndex : invalidOriginalInputs) {
                    var input = originalInputs.get(inputIndex);
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("index", inputIndex);
                    detail.put("slot", net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()[inputIndex].name());
                    detail.put("unlocked", net.farmingtales.ftboss.item.SwordCavityBuild.cellUnlocked(weapon, inputIndex));
                    detail.put("requiredUnlockLevel", nativeProfile == null ? -1 : nativeProfile.unlockLevels().get(inputIndex));
                    detail.put("originalEvolutionLevel", net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(weapon));
                    detail.put("actualOriginalInputId", input == null ? "<null>" : input.itemId());
                    detail.put("actualOriginalInputPresent", input != null && input.present());
                    detail.put("actualOriginalInputMaterial", input == null ? "<null>" : String.valueOf(input.material()));
                    detail.put("actualOriginalCell", cells.get(inputIndex).toString());
                    detail.put("actualNativePart", nativeParts.get(inputIndex).toString());
                    invalidDetails.add(detail);
                }
                row.put("actualOriginalNativeParts", nativeParts.stream().map(Object::toString).toList());
                row.put("actualOriginalCavityCells", cells.stream().map(Object::toString).toList());""")
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-stone-original-input-diagnostic-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-stone-original-input-diagnostic-41d445cf.json')
preparation = work / 'reports/native-exact-melee-stone-original-input-diagnostic-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_stone_original_input_diagnostic_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, failed_path, terminal, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1, 'originalMatrixIndex': 2,
    'readOnlyOriginalInputCellReferenceUnlockDiagnostic': True, 'allOriginalRefusalsAndFinalReadinessRetained': True,
    'productChanged': False, 'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualReadOnlyDiagnosticCases': 1}))
