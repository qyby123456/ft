import shutil
import subprocess
import sys
import uuid
import zipfile
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = BATCH / 'reports/actual-socket-recipe423-compiled-not-adopted.json'
compiled = load(binding)
for name, expected in {**compiled['inputHashes'], **compiled['outputHashes']}.items():
    assert digest(name) == expected, name
attempt = Path(compiled['attempt'])
prior = Path(load(LOCAL / 'batches/421-native-seed-isolation-binding/reports/actual-incremental421-experimental.json')['attempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(attempt / 'prototype-classes') + ';' + str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = BATCH / 'tests'
sources = [ROOT / 'batches/404-native-foundation-private-output/tests/Agent404.java', ROOT / 'batches/382-native-private-read-parity/tests/Asm382.java', ROOT / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for source in sources:
    shutil.copyfile(source, tests / source.name)
(tests / 'Builder423.java').write_text((tests / 'Builder423.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
output = attempt / ('checks423-' + uuid.uuid4().hex[:8])
output.mkdir()

def run(command, name):
    save(output / (name + '-command.json'), command)
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))

run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(output), *map(str, tests.glob('*.java'))], 'compile')
agent = output / 'agent423.jar'
with zipfile.ZipFile(agent, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('META-INF/MANIFEST.MF', chr(10).join(['Manifest-Version: 1.0', 'Premain-Class: Agent404', '', '']))
    for path in output.glob('Agent404*.class'):
        archive.write(path, path.name)
with zipfile.ZipFile(agent) as archive:
    assert archive.testzip() is None
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
builder = BATCH / 'reports/actual-builder423.json'
asm = BATCH / 'reports/actual-asm423.json'
semantic = BATCH / 'reports/actual-semantic423.json'
manifest = output / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in (attempt / 'prototype-classes').rglob('*.class') if (prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')).exists()])
run(java[:1] + ['-javaagent:' + str(agent)] + java[1:] + ['Builder423', str(builder)], 'builder')
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
added = {
    'NativeExpectedFieldObservation.java': ['socketRecipeCompoundForRead423(Ljava/util/Map;)Lnet/minecraft/nbt/CompoundTag;'],
    'FoundationApprovedRules.java': ['draftCanonical423(Lnet/farmingtales/ftboss/transaction/FoundationApprovedRules$PrivateRead423;)Ljava/lang/String;'],
}
changed = []
for row in load(semantic)['entries']:
    assert row['fieldsEqual'] and row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual'], row['name']
    assert row['removedMethods'] == [], row
    name = Path(row['name']).stem + '.java'
    expected = compiled['changedExistingMethods'].get(name, [])
    assert sorted(value.split('(')[0] for value in row['changedMethods']) == sorted(expected), row
    assert row['addedMethods'] == added.get(name, []), row
    if not expected and name not in added:
        assert row['skipDebugClassBytesEqual'], row
    changed.extend(row['changedMethods'])
assert len(changed) == 4
results = load(builder)
assert results['builderBinaryAndPayloadIdentityCases'] == 256 and results['recipeOrderingAndIndexCases'] == 256 and results['foreignCallerRejections'] == 3
save(BATCH / 'reports/actual-socket-recipe423-sealed-not-adopted.json', {
    'failures': 0, 'builderBinaryAndPayloadIdentityCases': 256, 'recipeOrderingAndIndexCases': 256, 'foreignCallerRejections': 3,
    'changedExistingMethods': compiled['changedExistingMethods'], 'asmClasses': load(asm)['prototypeClassesChecked'],
    'allOtherExistingMethodBodiesRetained': True, 'cumulativeCandidateUpdated': False,
    'actualRecipeAndSocketParityTested': False, 'actualRuntimeTested': False, 'fullSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, builder, asm, semantic, agent, Path(__file__), *sources, *tests.glob('*.java'), tests / 'Builder423.java.in']}})
print({'builderCases': 256, 'orderingCases': 256, 'foreignRejections': 3, 'asmClasses': load(asm)['prototypeClassesChecked'], 'changedExistingMethods': len(changed)}, flush=True)
