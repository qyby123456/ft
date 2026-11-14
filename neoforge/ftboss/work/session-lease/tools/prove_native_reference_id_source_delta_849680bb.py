import hashlib
import json
import re
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding='utf-8'))

old = load(work / 'reports/native-repair-ranged-use-candidate-41d445cf.json')
new = load(work / 'reports/native-repair-ranged-use-candidate-849680bb.json')
assert old['classCount'] == new['classCount'] == 1381 and old['guardCount'] == new['guardCount'] == 208
assert not new['newClasses'] and new['failures'] == 0
old_build = Path(old['attempt'])
new_build = Path(new['attempt'])
old_classes = old_build / 'complete-classes'
new_classes = new_build / 'complete-classes'
old_files = {path.relative_to(old_classes).as_posix(): path for path in old_classes.rglob('*.class')}
new_files = {path.relative_to(new_classes).as_posix(): path for path in new_classes.rglob('*.class')}
assert set(old_files) == set(new_files) and len(new_files) == 1381
changed = [relative for relative in new_files if digest(old_files[relative]) != digest(new_files[relative])]
assert changed == ['net/farmingtales/ftboss/item/SwordCavityBuild.class'], changed
def sources(arguments):
    result = {}
    for value in arguments:
        if isinstance(value, str) and value.endswith('.java'):
            path = Path(value)
            package = re.search(r'^package\s+([\w.]+);', path.read_text(encoding='utf-8-sig'), re.M)
            key = (package.group(1) if package else '<default>') + '/' + path.name
            assert key not in result, key
            result[key] = path
    return result
old_sources = sources(load(old_build / 'complete-arguments316.json'))
new_sources = sources(load(new_build / 'complete-arguments316.json'))
assert set(old_sources) == set(new_sources) and len(new_sources) == 716
changed_sources = [name for name in old_sources if digest(old_sources[name]) != digest(new_sources[name])]
changed_leaves = {name.rsplit('/', 1)[-1] for name in changed_sources}
assert changed_leaves <= {'SwordCavityBuild.java', 'OwnerFieldInstallationProof.java'} and 'SwordCavityBuild.java' in changed_leaves, changed_sources
source_key = 'net.farmingtales.ftboss.item/SwordCavityBuild.java'
old_text = old_sources[source_key].read_text(encoding='utf-8')
new_text = new_sources[source_key].read_text(encoding='utf-8')
anchor = '                String var13 = var12.<String>map(var0x -> var0x.itemId().toString()).orElse(var10.installed() ? "ftboss:legacy_" + var10.material().id() : "");'
replacement = """                ResourceLocation nativeReferenceId = var10.state() == State.NATIVE
                    && ((Piece)var1.get(var9.ordinal())).count() == 1 && !var11.isEmpty()
                        ? BuiltInRegistries.ITEM.getKey(var11.getItem()) : null;
                String var13 = var12.<String>map(var0x -> var0x.itemId().toString())
                    .orElse(nativeReferenceId != null ? nativeReferenceId.toString() : (var10.installed() ? "ftboss:legacy_" + var10.material().id() : ""));"""
assert new_text.replace(replacement, anchor) == old_text and new_text.count(replacement) == 1
outputs = []
for classes in [old_classes, new_classes]:
    result = subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe', '-p', '-s', '-classpath', str(classes), 'net.farmingtales.ftboss.item.SwordCavityBuild'], cwd=root, capture_output=True)
    assert result.returncode == 0
    outputs.append(result.stdout)
assert outputs[0] == outputs[1]
for resource, old_path in old['additionalProductionResources'].items():
    assert digest(old_path) == digest(new['additionalProductionResources'][resource])
assert load(new_build / 'asm.json')['failures'] == 0
for path in [Path(__file__), work / 'reports/native-stone-original-locked-recipe-reference-id-diagnosis-41d445cf.json']:
    digest(path)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
output = work / 'reports/native-native-reference-id-source-delta-849680bb.json'
namespace['add'](output.relative_to(root).as_posix(), json.dumps({
    'failures': 0, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208,
    'changedClassNamesVersus41': changed, 'newProductClasses': 0, 'changedSourceNamesVersus41': changed_sources,
    'originalProductionFieldMethodConstructorDescriptorsIdentical': True,
    'onlyActualOneCountRegisteredNativeReferenceIdentifierFallbackChanged': True,
    'originalProfileMaterialValuesThresholdsLocksReplacementAdmissionAndIndependentEventUnchanged': True,
    'originalNoDefinitionNonNativeReplacementStillDenied': True,
    'retiredFoodEnchantmentSourcesAndBothLanguageResourcesUnchanged': True,
    'runtimeAccepted': False, 'authenticatedMultiplayerAccepted': False, 'installable': False, 'deployed': False,
    'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'report': output.name, 'changedProductClassesVersus41': 1, 'unchangedEffectiveSources': 716 - len(changed_sources), 'newProductClasses': 0, 'hashes': len(hashes)}))
