import shutil
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
binding = BATCH / 'reports/actual-recipe-stack426-compiled-not-adopted.json'
compiled = load(binding)
for name, expected in {**compiled['inputHashes'], **compiled['outputHashes']}.items():
    assert digest(name) == expected, name
attempt = Path(compiled['attempt'])
prior = Path(load(LOCAL / 'batches/424-native-socket-recipe-binding/reports/actual-incremental424-experimental.json')['attempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(attempt / 'prototype-classes') + ';' + str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = BATCH / 'tests'
sources = [ROOT / 'batches/382-native-private-read-parity/tests/Asm382.java', ROOT / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for source in sources:
    shutil.copyfile(source, tests / source.name)
(tests / 'Caller426.java').write_text((tests / 'Caller426.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
output = attempt / ('checks426-' + uuid.uuid4().hex[:8])
output.mkdir()

def run(command, name):
    save(output / (name + '-command.json'), command)
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))

javac = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath]
run(javac + ['-d', str(output), *map(str, tests.glob('*.java'))], 'compile')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
caller = BATCH / 'reports/actual-caller426.json'
asm = BATCH / 'reports/actual-asm426.json'
semantic = BATCH / 'reports/actual-semantic426.json'
manifest = output / 'manifest.json'
paths = sorted((attempt / 'prototype-classes').rglob('*.class'))
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in paths])
run(java + ['Caller426', str(caller)], 'caller')
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
changed = []
for row in load(semantic)['entries']:
    assert row['fieldsEqual'] and row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual'], row
    assert row['removedMethods'] == [], row
    name = Path(row['name']).stem + '.java'
    assert [value.split('(')[0] for value in row['changedMethods']] == compiled['changedExistingMethods'].get(name, []), row
    expected_added = ['recipeStackForRead426(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/nbt/Tag;'] if name == 'NativeExpectedFieldObservation.java' else []
    assert row['addedMethods'] == expected_added, row
    if not row['changedMethods'] and not expected_added:
        assert row['skipDebugClassBytesEqual'], row
    changed.extend(row['changedMethods'])
assert len(changed) == 1 and load(caller)['foreignCallerRejections'] == 3
restored = []
for update in compiled['updates']:
    text = Path(update['source']).read_text(encoding='utf-8')
    assert text.count(update['afterText']) == 1
    text = text.replace(update['afterText'], update['beforeText'])
    assert text == Path(update['beforeSource']).read_text(encoding='utf-8')
    target = output / 'restored-source' / Path(update['source']).name
    target.parent.mkdir(exist_ok=True)
    target.write_text(text, encoding='utf-8')
    restored.append(target)
run(javac + ['-d', str(output / 'restored-classes'), *map(str, restored)], 'compile-restored')
restored_manifest = output / 'restored-manifest.json'
save(restored_manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(output / 'restored-classes' / path.relative_to(attempt / 'prototype-classes'))} for path in paths])
restored_report = BATCH / 'reports/actual-restoration426.json'
run(java + ['GuardSemantic360', str(restored_manifest), str(restored_report), str(output / 'restored-diff')], 'restoration')
assert all(row['skipDebugClassBytesEqual'] for row in load(restored_report)['entries'])
save(BATCH / 'reports/actual-recipe-stack426-sealed-not-adopted.json', {
    'failures': 0, 'foreignCallerRejections': 3, 'changedExistingMethods': compiled['changedExistingMethods'],
    'asmClasses': load(asm)['prototypeClassesChecked'], 'allOtherExistingMethodBodiesRetained': True,
    'sourceRestorationsExact': len(restored), 'restoredClassesSkipDebugEqual': len(paths),
    'originalSaveOptionalAndValidationPreserved': True, 'actualRecipeParityTested': False,
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'fullSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, caller, asm, semantic, restored_report, Path(__file__), *sources, *tests.glob('*.java'), tests / 'Caller426.java.in']}})
print({'asmClasses': load(asm)['prototypeClassesChecked'], 'foreignCallerRejections': 3, 'restoredSources': len(restored), 'restoredClasses': len(paths)}, flush=True)
