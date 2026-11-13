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
receipt = BATCH / 'reports/actual-fresh431-compiled-not-adopted.json'
compiled = load(receipt)
for name, expected in {**compiled['inputHashes'], **compiled['outputHashes']}.items():
    assert digest(name) == expected, name
attempt = Path(compiled['attempt'])
prior = Path(compiled['priorAttempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(attempt / 'prototype-classes') + ';' + str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = BATCH / 'tests'
tests.mkdir(exist_ok=True)
shared = [HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java', HISTORY / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
for source in shared:
    shutil.copyfile(source, tests / source.name)
original = LOCAL / 'batches/430-native-ranged-fallback-proof/tests/RangedFallback430.java.in'
text = original.read_text(encoding='utf-8').replace('RangedFallback430', 'RangedFallback431').replace('§', chr(34))
marker = '            require(rejected, §Current plan unexpectedly supports detached native fallback§);'.replace('§', chr(34))
addition = '''
            FoodWeaponExpectedTrace.Operation acquireFresh = builder.rangedAcquireDetached431(root, first);
            require(acquireFresh.after.player == player && !acquireFresh.after.rangedAttached
                && acquireFresh.after.rangedBufferIdentity == first && acquireFresh.after.rangedPlayerIdentity == root
                && acquireFresh.after.rangedAcquired && FoodWeaponExpectedTrace.same(acquireFresh.after.rangedBuffer, FoodWeaponExpectedTrace.emptyCompound()),
                §Detached plan adopted observed buffer contents or changed root before put§);
            FoodWeaponExpectedTrace.Operation detachedTimer = builder.rangedLong(§LastSkillProc.blood_feast§, 80L);
            require(detachedTimer.after.player == player && !detachedTimer.after.rangedAttached
                && FoodWeaponExpectedTrace.field(detachedTimer.after.rangedBuffer, §LastSkillProc.blood_feast§).bits == 80L,
                §Detached timer plan changed original player image§);
            FoodWeaponExpectedTrace.Operation attach = builder.rangedRoot();
            require(attach.after.rangedAttached && FoodWeaponExpectedTrace.same(
                FoodWeaponExpectedTrace.field(attach.after.player, KEY), detachedTimer.after.rangedBuffer), §Detached publication differs§);
'''.replace('§', chr(34))
assert text.count(marker) == 1
text = text.replace(marker, marker + addition)
marker = '        Map<String, Object> report = new LinkedHashMap<>();'
addition = '''        int foreignRejections = 0;
        try { NativeExpectedFieldObservation.rangedFreshAllocation431(new CompoundTag(), new java.util.HashMap<>()); }
        catch (IllegalStateException expected) { foreignRejections++; }
        try { NativeExpectedFieldObservation.plannedRangedFresh431(null, null, null, null); }
        catch (IllegalStateException expected) { foreignRejections++; }
        for (String methodName : List.of(§rangedFreshAllocation431§, §plannedRangedFresh431§)) {
            java.lang.reflect.Method method = java.util.Arrays.stream(NativeExpectedFieldObservation.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName)).findFirst().orElseThrow();
            method.setAccessible(true);
            try { method.invoke(null, new Object[method.getParameterCount()]); }
            catch (java.lang.reflect.InvocationTargetException expected) {
                require(expected.getCause() instanceof IllegalStateException, §Wrong foreign caller rejection§);
                foreignRejections++;
            }
        }
        require(foreignRejections == 4, §Foreign acquisition helper caller admitted§);
        int invalidIntentRejections = 0;
        FoodWeaponExpectedTrace.State attachedState = new FoodWeaponExpectedTrace.State(FoodWeaponExpectedTrace.emptyCompound(), player, FoodWeaponExpectedTrace.emptyCompound());
        try { new FoodWeaponExpectedTrace.Builder(attachedState).rangedAcquireDetached431(root, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        FoodWeaponExpectedTrace.State emptyState = new FoodWeaponExpectedTrace.State(FoodWeaponExpectedTrace.emptyCompound(), FoodWeaponExpectedTrace.emptyCompound(), FoodWeaponExpectedTrace.emptyCompound());
        try { new FoodWeaponExpectedTrace.Builder(emptyState).rangedAcquireDetached431(null, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        try { new FoodWeaponExpectedTrace.Builder(emptyState).rangedAcquireDetached431(root, null); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        FoodWeaponExpectedTrace.Builder once = new FoodWeaponExpectedTrace.Builder(emptyState);
        once.rangedAcquireDetached431(root, stored);
        try { once.rangedAcquireDetached431(root, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        require(invalidIntentRejections == 4, §Invalid allocation intent admitted§);
'''.replace('§', chr(34))
assert text.count(marker) == 1
text = text.replace(marker, addition + marker)
marker = '        report.put(§failures§, 0);'.replace('§', chr(34))
text = text.replace(marker, marker + chr(10) + '''        report.put(§newDetachedPlanCases§, 12);
        report.put(§foreignCallerRejections§, foreignRejections);
        report.put(§invalidIntentRejections§, invalidIntentRejections);
'''.replace('§', chr(34)))
text = text.replace('currentPlannerSupportsDetachedFallback', 'oldAcquireMethodSupportsDetachedFallback')
(tests / 'RangedFallback431.java').write_text(text, encoding='utf-8')
output = attempt / ('checks431-' + uuid.uuid4().hex[:8])
output.mkdir()

def run(command, name):
    save(output / (name + '-command.json'), command)
    with (output / (name + '.log')).open('wb') as stream:
        result = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(output / (name + '.log'))

run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(output), *map(str, tests.glob('*.java'))], 'compile')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
parity = BATCH / 'reports/actual-detached-plan431.json'
asm = BATCH / 'reports/actual-asm431.json'
semantic = BATCH / 'reports/actual-semantic431.json'
manifest = output / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in (attempt / 'prototype-classes').rglob('*.class')])
run(java + ['net.farmingtales.ftboss.item.RangedFallback431', str(parity)], 'plan')
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
changed = []
added = []
for row in load(semantic)['entries']:
    assert row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual'], row['name']
    assert row['removedMethods'] == [], row
    class_name = Path(row['name']).stem
    expected = {'NativeFreshFactoryTrust298': ['freshMap'], 'OwnerTimerSemanticAuthority$Book': ['resolve']}.get(class_name, [])
    assert sorted(value.split('(')[0] for value in row['changedMethods']) == expected, row
    field_classes = ['NativeExpectedFieldObservation$Fact', 'OwnerTimerSemanticAuthority$Book']
    assert row['fieldsEqual'] == (class_name not in field_classes), row
    allowed_additions = {
        'FoodWeaponExpectedTrace$Builder': ['rangedAcquireDetached431'],
        'NativeExpectedFieldObservation': ['plannedRangedFresh431', 'rangedExternalFrame431', 'rangedFrames431', 'rangedFreshAllocation431'],
    }.get(class_name, [])
    assert sorted(value.split('(')[0] for value in row['addedMethods']) == allowed_additions, row
    if not expected and not allowed_additions and class_name not in field_classes:
        assert row['skipDebugClassBytesEqual'], row
    changed.extend(row['changedMethods'])
    added.extend(row['addedMethods'])
assert len(changed) == 2 and len(added) == 5
checks = load(parity)
assert checks['newDetachedPlanCases'] == 12 and checks['foreignCallerRejections'] == 4 and checks['invalidIntentRejections'] == 4
save(BATCH / 'reports/actual-fresh431-sealed-not-adopted.json', {
    'failures': 0, 'newDetachedPlanCases': 12, 'foreignCallerRejections': 4, 'invalidIntentRejections': 4,
    'asmClasses': load(asm)['prototypeClassesChecked'], 'changedExistingMethods': changed,
    'addedMethods': added, 'allOtherExistingMethodBodiesRetained': True, 'sourcesReversedExactly': 4,
    'nativeConstructorReceiptRuntimeTested': False, 'cumulativeCandidateUpdated': False,
    'fullSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [receipt, parity, asm, semantic, original, Path(__file__), *shared, *tests.glob('*.java')]},
})
print({'newDetachedPlanCases': 12, 'foreignRejections': 4, 'invalidIntents': 4, 'asmClasses': load(asm)['prototypeClassesChecked'], 'existingMethodsChanged': len(changed), 'adopted': False})
