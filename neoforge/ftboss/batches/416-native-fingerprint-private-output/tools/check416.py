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
binding = BATCH / 'reports/actual-fingerprint416-compiled-not-adopted.json'
compiled = load(binding)
for name, expected in {**compiled['inputHashes'], **compiled['outputHashes']}.items():
    assert digest(name) == expected, name
attempt = Path(compiled['attempt'])
prior = Path(load(LOCAL / 'batches/414-food-enchantment-binding/reports/actual-incremental414-experimental.json')['attempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(attempt / 'prototype-classes') + ';' + str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = BATCH / 'tests'
sources = [ROOT / 'batches/404-native-foundation-private-output/tests/Agent404.java',
    ROOT / 'batches/382-native-private-read-parity/tests/Asm382.java', ROOT / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for source in sources:
    shutil.copyfile(source, tests / source.name)
(tests / 'Builder416.java').write_text((tests / 'Builder416.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
output = attempt / ('checks416-' + uuid.uuid4().hex[:8])
output.mkdir()

def run(command, name):
    save(output / (name + '-command.json'), command)
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))

run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath,
    '-d', str(output), *map(str, tests.glob('*.java'))], 'compile')
agent = output / 'agent416.jar'
with zipfile.ZipFile(agent, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('META-INF/MANIFEST.MF', chr(10).join(['Manifest-Version: 1.0', 'Premain-Class: Agent404', '', '']))
    for path in output.glob('Agent404*.class'):
        archive.write(path, path.name)
with zipfile.ZipFile(agent) as archive:
    assert archive.testzip() is None
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
builder = BATCH / 'reports/actual-builder416.json'
asm = BATCH / 'reports/actual-asm416.json'
semantic = BATCH / 'reports/actual-semantic416.json'
manifest = output / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(),
    'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)}
    for path in (attempt / 'prototype-classes').rglob('*.class')
    if (prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')).exists()])
run(java[:1] + ['-javaagent:' + str(agent)] + java[1:] + ['Builder416', str(builder)], 'builder')
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
changed = []
for row in load(semantic)['entries']:
    assert row['fieldsEqual'] and row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual'], row['name']
    assert row['removedMethods'] == []
    name = Path(row['name']).stem + '.java'
    expected = compiled['changedExistingMethods'].get(name, [])
    assert sorted(value.split('(')[0] for value in row['changedMethods']) == sorted(expected), row
    if name == 'NativeExpectedFieldObservation.java':
        assert row['addedMethods'] == ['fingerprintCompoundForRead416(Lnet/minecraft/nbt/CompoundTag;Ljava/util/Map;)Lnet/minecraft/nbt/CompoundTag;']
    else:
        assert row['addedMethods'] == [], row
    if not expected and name != 'NativeExpectedFieldObservation.java':
        assert row['skipDebugClassBytesEqual'], row
    changed.extend(row['changedMethods'])
assert len(changed) == 5
assert load(builder)['builderBinaryAndPayloadIdentityCases'] == 640
save(BATCH / 'reports/actual-fingerprint416-sealed-not-adopted.json', {
    'failures': 0, 'builderBinaryAndPayloadIdentityCases': 640, 'canonicalDigestParityCases': 128,
    'foreignCallerRejections': 6, 'changedExistingMethods': compiled['changedExistingMethods'],
    'asmClasses': load(asm)['prototypeClassesChecked'], 'allOtherExistingMethodBodiesRetained': True,
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'fullSignedSessionAccepted': False,
    'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, builder, asm, semantic, agent, Path(__file__), *sources, *tests.glob('*.java'), tests / 'Builder416.java.in']}})
print({'builderCases': 640, 'digestParityCases': 128, 'foreignRejections': 6, 'asmClasses': load(asm)['prototypeClassesChecked'], 'changedExistingMethods': len(changed)}, flush=True)
