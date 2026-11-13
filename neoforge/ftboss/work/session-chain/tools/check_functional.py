import shutil
import subprocess
import sys
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
receipt = WORK / 'reports/functional-compiled.json'
data = load(receipt)
attempt = Path(data['attempt'])
parent = Path(data['parentAttempt'])
arguments = load(parent / 'complete-arguments316.json')
classpath = str(attempt / 'prototype-classes') + ';' + str(parent / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = WORK / 'tests'
tests.mkdir(exist_ok=True)
shared = [HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java', HISTORY / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for source in shared:
    shutil.copyfile(source, tests / source.name)
source = '''package net.farmingtales.ftboss.item;
public final class ContinuationCallers {
    public static void main(String[] args) throws Throwable {
        var method = NativeExpectedFieldObservation.class.getDeclaredMethod(§plannedRangedFieldMutation§, Object.class, int.class);
        method.setAccessible(true);
        var handle = java.lang.invoke.MethodHandles.lookup().unreflect(method);
        int rejected = 0;
        for (int reason : new int[]{20,21,40,42}) {
            Object owner = new net.minecraft.nbt.CompoundTag();
            if (NativeExpectedFieldObservation.plannedRangedFieldMutation(owner, reason)) throw new AssertionError(§Direct grant§);
            rejected++;
            if (Boolean.TRUE.equals(method.invoke(null, owner, reason))) throw new AssertionError(§Reflective grant§);
            rejected++;
            if ((boolean) handle.invokeExact(owner, reason)) throw new AssertionError(§Handle grant§);
            rejected++;
        }
        if (rejected != 12) throw new AssertionError(§Missing rejection§);
        System.out.println(§foreign and out-of-context mutations rejected=§ + rejected);
    }
}
'''.replace('§', chr(34))
(tests / 'ContinuationCallers.java').write_text(source, encoding='utf-8')
checks = attempt / 'checks'
checks.mkdir(exist_ok=True)

def run(command, name):
    save(checks / (name + '-command.json'), command)
    with (checks / (name + '.log')).open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
    save(checks / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(checks / (name + '.log'))

run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(checks), *map(str, tests.glob('*.java'))], 'compile')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(checks) + ';' + classpath]
run(java + ['net.farmingtales.ftboss.item.ContinuationCallers'], 'caller-rejections')
asm = WORK / 'reports/functional-asm.json'
semantic = WORK / 'reports/functional-semantic.json'
manifest = checks / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(parent / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in (attempt / 'prototype-classes').rglob('*.class')])
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(checks / 'diff')], 'delta')
changed = []
added = []
for row in load(semantic)['entries']:
    name = Path(row['name']).stem
    assert row['fieldsEqual'] and row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual']
    assert not row['removedMethods']
    expected = ['changed'] if name == 'OwnerFullRepairGuard' else []
    assert [value.split('(')[0] for value in row['changedMethods']] == expected, row
    assert [value.split('(')[0] for value in row['addedMethods']] == (['plannedRangedFieldMutation'] if name == 'NativeExpectedFieldObservation' else []), row
    changed.extend(row['changedMethods'])
    added.extend(row['addedMethods'])
assert len(changed) == 1 and len(added) == 1
save(WORK / 'reports/functional-checked.json', {
    'failures': 0, 'foreignAndOutOfContextRejections': 12, 'relatedAsmClasses': load(asm)['prototypeClassesChecked'],
    'changedExistingMethods': changed, 'addedMethods': added, 'otherExistingMethodsRetained': True,
    'actualNativePositiveTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [receipt, asm, semantic, Path(__file__), *tests.glob('*.java')]},
})
print({'failures': 0, 'foreignRejections': 12, 'relatedAsmClasses': load(asm)['prototypeClassesChecked'], 'existingMethodsChanged': 1})
