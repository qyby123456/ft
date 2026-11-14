import shutil
import subprocess
import sys
import uuid
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/432-native-ranged-fresh-binding/reports/actual-incremental432-experimental.json'
assert digest(binding) == 'a555ebe084a11eff668519a3839ea199e5c12b8a150109f6d57cdce83117f85a'
receipt = load(binding)
for name, expected in receipt['inputHashes'].items():
    assert digest(name) == expected, name
prior = Path(receipt['attempt'])
arguments = load(prior / 'complete-arguments316.json')
original = next(Path(value) for value in arguments if Path(value).name == 'FoodWeaponOriginalSequence.java')
text = original.read_text(encoding='utf-8')
changes = [
    ('        private Throwable firstObservationFailure;', '        private Throwable firstObservationFailure;' + chr(10) + '        private Throwable firstRevocationSite434;'),
    ('        private void revoke(FoodWeaponOriginalSequence.Step var1, int var2, Throwable var3) {' + chr(10) + '            this.traceRevoked = true;',
     '''        private void revoke(FoodWeaponOriginalSequence.Step var1, int var2, Throwable var3) {
            if (!this.traceRevoked) {
                this.firstRevocationSite434 = new IllegalStateException(§Original sequence first revoked at phase § + var2, var3);
            }
            this.traceRevoked = true;'''.replace('§', chr(34))),
]
for before, after in changes:
    assert text.count(before) == 1
    text = text.replace(before, after)
source = BATCH / 'proposal/net/farmingtales/ftboss/item/FoodWeaponOriginalSequence.java'
source.parent.mkdir(parents=True, exist_ok=True)
source.write_text(text, encoding='utf-8')
restored = text
for before, after in reversed(changes):
    assert restored.count(after) == 1
    restored = restored.replace(after, before)
assert restored == original.read_text(encoding='utf-8')
attempt = BATCH / 'build' / ('revocation-origin434-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
javac = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath]

def run(command, name):
    save(attempt / (name + '-command.json'), command)
    with (attempt / (name + '.log')).open('wb') as stream:
        result = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(attempt / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(attempt / (name + '.log'))

run(javac + ['-d', str(attempt / 'prototype-classes'), str(source)], 'compile')
compiled_path = BATCH / 'reports/actual-revocation434-compiled-not-adopted.json'
save(compiled_path, {
    'failures': 0, 'attempt': str(attempt), 'diagnosticOnly': True,
    'sourceRestoredExactly': True, 'rejectionAndGameValuesChanged': False,
    'cumulativeCandidateUpdated': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, Path(__file__), original, source]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')},
})
tests = BATCH / 'tests'
tests.mkdir(exist_ok=True)
shared = [HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java', HISTORY / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for path in shared:
    shutil.copyfile(path, tests / path.name)
run(javac + ['-d', str(attempt / 'checks'), *map(str, tests.glob('*.java'))], 'compile-checks')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(attempt / 'checks') + ';' + str(attempt / 'prototype-classes') + ';' + classpath]
asm = BATCH / 'reports/actual-asm434.json'
semantic = BATCH / 'reports/actual-semantic434.json'
paths = list((attempt / 'prototype-classes').rglob('*.class'))
manifest = attempt / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in paths])
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(attempt / 'diff')], 'semantic')
changed = []
for row in load(semantic)['entries']:
    session = row['name'].endswith('FoodWeaponOriginalSequence$Session.class')
    assert row['fieldsEqual'] == (not session), row
    assert row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual'], row
    assert not row['addedMethods'] and not row['removedMethods'], row
    assert [value.split('(')[0] for value in row['changedMethods']] == (['revoke'] if session else []), row
    if not session:
        assert row['skipDebugClassBytesEqual'], row
    changed.extend(row['changedMethods'])
assert len(changed) == 1
restored_source = attempt / 'restored-source/FoodWeaponOriginalSequence.java'
restored_source.parent.mkdir()
restored_source.write_text(restored, encoding='utf-8')
run(javac + ['-d', str(attempt / 'restored-classes'), str(restored_source)], 'compile-restored')
restoration_manifest = attempt / 'restoration-manifest.json'
save(restoration_manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(attempt / 'restored-classes' / path.relative_to(attempt / 'prototype-classes'))} for path in paths])
restoration = BATCH / 'reports/actual-restoration434.json'
run(java + ['GuardSemantic360', str(restoration_manifest), str(restoration), str(attempt / 'restored-diff')], 'restoration')
assert all(row['skipDebugClassBytesEqual'] for row in load(restoration)['entries'])
save(BATCH / 'reports/actual-revocation434-sealed-not-adopted.json', {
    'failures': 0, 'diagnosticOnly': True, 'asmClasses': len(paths), 'restoredWholeClasses': len(paths),
    'changedExistingMethods': changed, 'allOtherExistingMethodBodiesRetained': True,
    'sourceRestoredExactly': True, 'rejectionAndGameValuesChanged': False,
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [compiled_path, asm, semantic, restoration, Path(__file__), *shared, *tests.glob('*.java')]},
})
print({'failures': 0, 'diagnosticOnly': True, 'changedMethods': 1, 'asmClasses': len(paths), 'restoredWholeClasses': len(paths)})
