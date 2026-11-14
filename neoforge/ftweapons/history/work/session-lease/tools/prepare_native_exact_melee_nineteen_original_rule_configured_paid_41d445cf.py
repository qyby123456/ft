import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-wooden-original-rule-planned-paid-qualified-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
configured_proof = work / 'reports/native-exact-melee-wooden-configured-paid-signed-restart-focused-evidence-41d445cf.json'
assert load(configured_proof)['failures'] == 0
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = 'for (int sample = 0; sample < 1; sample++) {'
assert source.count(anchor) == 1
source = source.replace(anchor, 'for (int sample = 0; sample < 19; sample++) {')
source = source.replace('oneWoodenSwordOriginalMaterialInstallationPaidRepairAndSameProcessReload', 'nineteenExactOriginalMeleeVariantsConfiguredPaidRepairAndSameProcessReload')
start = source.index('        if (!net.farmingtales.ftboss.util.SwordCompatibilityRules.upgradeReferenceBudget(')
end = source.index('        row.put("readOnlyOriginalRuleCandidateCount"', start)
source = source[:start] + source[end:]
anchor = '    private static void installOriginalMaterials('
method = """    @SuppressWarnings("unchecked")
    private static void preserveOriginalReturnedSupply(ServerPlayer player, Map<String, Object> row) {
        ItemStack returned = player.getInventory().getItem(2);
        if (returned.isEmpty()) return;
        int destination = -1;
        for (int index = 3; index < 36; index++) if (player.getInventory().getItem(index).isEmpty()) { destination = index; break; }
        if (destination < 0) throw new AssertionError("No fixture inventory space for original returned cavity material");
        String identity = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(returned.getItem()).toString();
        int count = returned.getCount();
        ItemStack moved = player.getInventory().removeItemNoUpdate(2);
        player.getInventory().setItem(destination, moved);
        if (moved != returned || moved.getCount() != count || !player.getInventory().getItem(2).isEmpty())
            throw new AssertionError("Original returned supply identity/count was not preserved");
        List<Object> preserved = (List<Object>)row.computeIfAbsent("originalReturnedMaterialsPreservedInInventory", key -> new ArrayList<>());
        preserved.add(Map.of("item", identity, "count", count, "inventorySlot", destination, "samePhysicalItemAndCountPreserved", true));
    }

"""
assert source.count(anchor) == 1
source = source.replace(anchor, method + anchor)
start = source.index('                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {', source.index('                if (cells.size() != 27)'))
end = source.index('                requireSameOriginalStatus(originalPlannedStatus', start)
source = source[:start] + """                var nativeProfile = net.farmingtales.ftboss.api.SwordCavityExtensions.functionProfile(weapon);
                boolean moddedReferences = !net.farmingtales.ftboss.util.SwordCompatibilityRules.upgradeReferenceBudget(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(), List.of()).isEmpty();
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                    var cell = cells.get(slot.ordinal());
                    ItemStack piece = cell.state() == net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE
                        && nativeParts.get(slot.ordinal()).count() == 1
                        ? net.farmingtales.ftboss.item.SwordNativeMaterials.display(weapon, slot.ordinal(), nativeParts)
                        : net.farmingtales.ftboss.item.SwordCavityBuild.storedItem(weapon, slot);
                    var definition = net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(piece);
                    String fallback = moddedReferences && !piece.isEmpty()
                        ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(piece.getItem()).toString()
                        : (cell.installed() ? "ftboss:legacy_" + cell.material().id() : "");
                    String identity = definition.map(value -> value.itemId().toString()).orElse(fallback);
                    String inputIdentity = identity;
                    if (moddedReferences && cell.state() == net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE && nativeProfile != null) {
                        inputIdentity = "ftboss:native_reference/" + slot.ordinal();
                        var functions = nativeProfile.itemValues().get(identity);
                        if (functions == null) functions = net.farmingtales.ftboss.util.SwordCompatibilityRules.materialFunctions(
                            net.farmingtales.ftboss.util.SwordCompatibilityRules.material(identity));
                        originalValues.put(inputIdentity, functions);
                    } else if (definition.isPresent()) {
                        var value = definition.get();
                        if (net.farmingtales.ftboss.api.SwordMaterialExtensions.registered(value.itemId())
                            || value.categories().contains(net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD))
                            originalValues.put(identity, value.functions());
                    }
                    originalInputs.add(new net.farmingtales.ftboss.util.SwordFunctionRules.Input(inputIdentity,
                        definition.map(value -> value.baseMaterial()).orElse(cell.material()),
                        cell.present() && (cell.state() != net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE || !piece.isEmpty())));
                }
""" + source[end:]
start = source.index('                net.farmingtales.ftboss.util.SwordFunctionRules.Status bestStatus = null;')
end = source.index('                if (bestSlot == null)', start)
source = source[:start] + """                net.farmingtales.ftboss.util.SwordFunctionRules.Status bestStatus = null;
                record PlannedMaterial(net.farmingtales.ftboss.util.SwordCavityRules.Slot slot,
                    net.minecraft.world.item.Item item, net.farmingtales.ftboss.util.SwordFunctionRules.Status status, double score) {}
                List<PlannedMaterial> candidates = new ArrayList<>();
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                    if (!net.farmingtales.ftboss.item.SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) continue;
                    var cell = cells.get(slot.ordinal());
                    for (var item : materials) {
                        var definition = definitions.get(item);
                        var material = definition.baseMaterial();
                        if (!net.farmingtales.ftboss.util.SwordCavityRules.accepts(slot, material)
                            || cell.installed() && cell.material() == material) continue;
                        var proposedInputs = new ArrayList<>(originalInputs);
                        var proposedValues = new java.util.HashMap<>(originalValues);
                        String identity = definition.itemId().toString();
                        proposedInputs.set(slot.ordinal(), new net.farmingtales.ftboss.util.SwordFunctionRules.Input(identity, material, true));
                        if (net.farmingtales.ftboss.api.SwordMaterialExtensions.registered(definition.itemId())
                            || definition.categories().contains(net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD))
                            proposedValues.put(identity, definition.functions());
                        var status = originalPlannedStatus(weapon, proposedInputs, proposedValues);
                        double candidateScore = readinessScore(status);
                        if (candidateScore > score + 0.0000001) candidates.add(new PlannedMaterial(slot, item, status, candidateScore));
                    }
                }
                candidates.sort(java.util.Comparator.comparingDouble(PlannedMaterial::score).reversed());
                Map<net.farmingtales.ftboss.util.SwordCavityRules.Slot, Map<net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material, Boolean>> originalLegality = new java.util.HashMap<>();
                int originalLegalityChecks = 0;
                for (var candidate : candidates) {
                    var material = definitions.get(candidate.item()).baseMaterial();
                    var byMaterial = originalLegality.computeIfAbsent(candidate.slot(), key -> new java.util.HashMap<>());
                    Boolean legal = byMaterial.get(material);
                    if (legal == null) {
                        legal = net.farmingtales.ftboss.item.SwordCavityBuild.canReplace(weapon, candidate.slot(), material);
                        byMaterial.put(material, legal); originalLegalityChecks++;
                    }
                    if (!legal) continue;
                    bestSlot = candidate.slot(); bestMaterial = candidate.item(); bestStatus = candidate.status(); break;
                }
                row.put("lastReadOnlyRuleCandidateCount", candidates.size());
                row.put("lastOriginalCanReplaceChecks", originalLegalityChecks);
""" + source[end:]
anchor = '                player.getInventory().setItem(2, new ItemStack(bestMaterial));'
assert source.count(anchor) == 1
source = source.replace(anchor, '                preserveOriginalReturnedSupply(player, row);\n                ItemStack exactSupply = new ItemStack(bestMaterial);\n                player.getInventory().setItem(2, exactSupply);')
source = source.replace('!accepted || !player.getInventory().getItem(2).isEmpty()', '!accepted || !exactSupply.isEmpty()')
anchor = '            if (net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon)) throw new AssertionError("Original material layout still incomplete");'
assert source.count(anchor) == 1
source = source.replace(anchor, '            preserveOriginalReturnedSupply(player, row);\n' + anchor)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-nineteen-original-rule-configured-paid-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
start = launcher.index("previous = WORK / 'reports/native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json'")
end = launcher.index('attempt = ', start)
launcher = launcher[:start] + "assert load(WORK / 'reports/native-exact-melee-wooden-configured-paid-signed-restart-focused-evidence-41d445cf.json')['failures'] == 0\n" + launcher[end:]
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-original-rule-planned-paid-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-nineteen-original-rule-configured-paid-41d445cf.json')
preparation = work / 'reports/native-exact-melee-nineteen-original-rule-configured-paid-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_nineteen_original_rule_configured_paid_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, configured_proof, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 19,
    'originalVanillaAndModdedNativeReferenceFunctionStatusBranchesReadOnlyPlanned': True,
    'originalCanReplaceDetachedPreviewAndRealInventoryConsumptionStillRequired': True,
    'baselineSelectedPreviewAndActuallyInstalledOriginalStatusMustBeExactlyEqual': True,
    'originalReturnedPiecesPreservedWithoutOverwritingOrDropping': True,
    'originalProfileStatueAndFoodPaymentsAndRawRegisteredClassMaxDamageAssertionsRetained': True,
    'productChanged': False, 'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualConfiguredPaidCasesRequired': 19, 'candidateUnchanged': True}))
