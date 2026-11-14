import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

parent = work / 'reports/native-exact-melee-wooden-original-installed-paid-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = '    private static void installOriginalMaterials('
assert source.count(anchor) == 1
method = """    private static net.farmingtales.ftboss.util.SwordFunctionRules.Status originalPlannedStatus(
        ItemStack weapon, List<net.farmingtales.ftboss.util.SwordFunctionRules.Input> inputs,
        Map<String, net.farmingtales.ftboss.util.SwordFunctionRules.Values> values) {
        var profile = net.farmingtales.ftboss.api.SwordCavityExtensions.functionProfile(weapon);
        return net.farmingtales.ftboss.util.SwordFunctionRules.evaluateItems(
            profile == null ? null : profile.withItemValues(values), inputs,
            net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(weapon));
    }

    private static void requireSameOriginalStatus(net.farmingtales.ftboss.util.SwordFunctionRules.Status expected,
        net.farmingtales.ftboss.util.SwordFunctionRules.Status actual) {
        if (!expected.equals(actual)) throw new AssertionError("Read-only original rule plan differs from actual detached/installed status: " + expected + " / " + actual);
    }

"""
source = source.replace(anchor, method + anchor)
anchor = '        materials.sort(java.util.Comparator.comparing(item -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString()));'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + """
        Map<net.minecraft.world.item.Item, net.farmingtales.ftboss.api.SwordMaterialDefinition> definitions = new HashMap<>();
        for (var item : materials) definitions.put(item, net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(new ItemStack(item)).orElseThrow());
        if (!net.farmingtales.ftboss.util.SwordCompatibilityRules.upgradeReferenceBudget(
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(), List.of()).isEmpty())
            throw new AssertionError("This bounded planner does not represent modded functionStatus branches");
        row.put("readOnlyOriginalRuleCandidateCount", materials.size());""")
start = source.index('                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {', source.index('    private static void installOriginalMaterials('))
end = source.index('                if (bestSlot == null)', start)
source = source[:start] + """                List<net.farmingtales.ftboss.util.SwordFunctionRules.Input> originalInputs = new ArrayList<>();
                Map<String, net.farmingtales.ftboss.util.SwordFunctionRules.Values> originalValues = new HashMap<>();
                var nativeParts = net.farmingtales.ftboss.item.SwordNativeMaterials.parts(weapon);
                var cells = net.farmingtales.ftboss.item.SwordCavityBuild.cells(weapon, nativeParts);
                if (cells.size() != 27) throw new AssertionError("Original cavity cell count changed");
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                    var cell = cells.get(slot.ordinal());
                    ItemStack piece = cell.state() == net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE
                        && nativeParts.get(slot.ordinal()).count() == 1
                        ? net.farmingtales.ftboss.item.SwordNativeMaterials.display(weapon, slot.ordinal(), nativeParts)
                        : net.farmingtales.ftboss.item.SwordCavityBuild.storedItem(weapon, slot);
                    var definition = net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(piece);
                    String identity = definition.map(value -> value.itemId().toString()).orElse(cell.installed() ? "ftboss:legacy_" + cell.material().id() : "");
                    definition.ifPresent(value -> {
                        if (net.farmingtales.ftboss.api.SwordMaterialExtensions.registered(value.itemId())
                            || value.categories().contains(net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD))
                            originalValues.put(identity, value.functions());
                    });
                    originalInputs.add(new net.farmingtales.ftboss.util.SwordFunctionRules.Input(identity,
                        definition.map(value -> value.baseMaterial()).orElse(cell.material()),
                        cell.present() && (cell.state() != net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE || !piece.isEmpty())));
                }
                requireSameOriginalStatus(originalPlannedStatus(weapon, originalInputs, originalValues),
                    net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));
                net.farmingtales.ftboss.util.SwordFunctionRules.Status bestStatus = null;
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                    if (!net.farmingtales.ftboss.item.SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) continue;
                    Map<net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material, Boolean> legalMaterials = new HashMap<>();
                    for (var item : materials) {
                        var definition = definitions.get(item);
                        var material = definition.baseMaterial();
                        if (!legalMaterials.computeIfAbsent(material,
                            value -> net.farmingtales.ftboss.item.SwordCavityBuild.canReplace(weapon, slot, value))) continue;
                        var proposedInputs = new ArrayList<>(originalInputs);
                        var proposedValues = new HashMap<>(originalValues);
                        String identity = definition.itemId().toString();
                        proposedInputs.set(slot.ordinal(), new net.farmingtales.ftboss.util.SwordFunctionRules.Input(identity, material, true));
                        if (net.farmingtales.ftboss.api.SwordMaterialExtensions.registered(definition.itemId())
                            || definition.categories().contains(net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD))
                            proposedValues.put(identity, definition.functions());
                        var status = originalPlannedStatus(weapon, proposedInputs, proposedValues);
                        double candidate = readinessScore(status);
                        if (candidate > score + 0.0000001) { score = candidate; bestSlot = slot; bestMaterial = item; bestStatus = status; }
                    }
                }
""" + source[end:]
anchor = '                player.getInventory().setItem(2, new ItemStack(bestMaterial));'
assert source.count(anchor) == 1
source = source.replace(anchor, """                List<ItemStack> selectedContents = new ArrayList<>(net.farmingtales.ftboss.item.SwordCavityBuild.containerItems(weapon));
                selectedContents.set(bestSlot.ordinal(), new ItemStack(bestMaterial));
                ItemStack selectedPreview = (ItemStack)preview.invoke(null, weapon, selectedContents);
                if (selectedPreview.isEmpty()) throw new AssertionError("Original selected detached preview refused");
                requireSameOriginalStatus(bestStatus, net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(selectedPreview));
""" + anchor)
anchor = '                installed.add(Map.of('
assert source.count(anchor) == 1
source = source.replace(anchor, '                requireSameOriginalStatus(bestStatus, net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));\n' + anchor)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-wooden-original-rule-planned-paid-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json', 'native-repair-runtime-exact-melee-wooden-original-rule-planned-paid-41d445cf.json')
preparation = work / 'reports/native-exact-melee-wooden-original-rule-planned-paid-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
anchor = "attempt = "
position = launcher.index(anchor)
launcher = launcher[:position] + """previous = WORK / 'reports/native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json'
previous_report = load(previous)
previous_terminal = load(Path(previous_report['attempt']) / 'terminal308.json')
assert previous_report['exitCode'] == 0 and previous_report['probe']['failures'] != 0
assert previous_terminal['completeSave'] and previous_terminal['stopSent']
""" + launcher[position:]
new_launcher = work / 'tools/run_native_exact_melee_wooden_original_rule_planned_paid_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1,
    'productChanged': False, 'usesOriginalEvaluateItemsReadOnlyForPlanning': True,
    'baselineSelectedDetachedPreviewAndActualInstalledStatusMustBeExactlyEqual': True,
    'originalInventoryReplacementAndActualConsumptionStillRequired': True,
    'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'doesNotStartAnyJvm': True,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'noJvmStarted': True, 'candidateUnchanged': True}))
