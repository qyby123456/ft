import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/424-native-socket-recipe-binding/reports/actual-incremental424-experimental.json'
diagnostic = LOCAL / 'batches/425-native-socket-recipe-runtime/reports/actual-first-rejection425-diagnostic.json'
for receipt in [binding, diagnostic]:
    for name, expected in load(receipt)['inputHashes'].items():
        assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
targets = []
updates = []
helper = '''    public static Tag recipeStackForRead426(ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        requireReadCaller(net.farmingtales.ftboss.transaction.NativeRecipeEvidenceData.class, §stack§,
            §(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/nbt/CompoundTag;§);
        return stack.saveOptional(new ReadRegistries(registries));
    }

'''.replace('§', chr(34))
for name in ['NativeRecipeEvidenceData.java', 'NativeExpectedFieldObservation.java']:
    original = next(Path(value) for value in arguments if Path(value).name == name)
    text = original.read_text(encoding='utf-8')
    if name == 'NativeRecipeEvidenceData.java':
        old = 'Tag encoded = stack.saveOptional(registries);'
        new = 'Tag encoded = net.farmingtales.ftboss.item.NativeExpectedFieldObservation.recipeStackForRead426(stack, registries);'
        assert text.count(old) == 1
        text = text.replace(old, new)
    else:
        old = '    private static final class ReadRegistries implements net.minecraft.core.HolderLookup.Provider {'
        new = helper + old
        assert text.count(old) == 1
        text = text.replace(old, new)
    package = text.split('package ', 1)[1].split(';', 1)[0]
    target = BATCH / 'proposal' / package.replace('.', '/') / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    targets.append(target)
    updates.append({'beforeSource': str(original), 'source': str(target), 'beforeText': old, 'afterText': new})
attempt = BATCH / 'build' / ('recipe-stack426-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, targets)]
save(attempt / 'compile-command426.json', command)
with (attempt / 'compile426.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal426.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile426.log')
save(BATCH / 'reports/actual-recipe-stack426-compiled-not-adopted.json', {
    'attempt': str(attempt), 'failures': 0, 'updates': updates,
    'changedExistingMethods': {'NativeRecipeEvidenceData.java': ['stack'], 'NativeExpectedFieldObservation.java': []},
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, diagnostic, Path(__file__), *[Path(row['beforeSource']) for row in updates], *targets]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'originalSaveOptionalPreserved': True}, flush=True)
