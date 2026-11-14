import subprocess
import sys
import uuid
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = ROOT / 'batches/435-native-revocation-origin-binding/reports/actual-incremental435-experimental.json'
assert digest(binding) == 'cc1fc4082a363c3a62e1c9d1b4144ca4e300a6144ad8b55d84ec921d347de6cb'
parent = Path(load(binding)['attempt'])
arguments = load(parent / 'complete-arguments316.json')
sources = [Path(value) for value in arguments if value.endswith('.java')]
changes = []
targets = []
helper = '''    static boolean plannedRangedFieldMutation(Object owner, int reason) {
        Scope scope = current();
        Fact fact = scope == null ? null : scope.active;
        if (fact == null || reason != 20 && reason != 21 || !eligible(scope)
            || fact.thread != Thread.currentThread() || !fact.attempted || fact.returned || fact.originalFailure != null
            || !parent(fact, §RANGED_SAVE§) || fact.originalStep == null || fact.originalStep != scope.step
            || fact.bindingBefore == null || fact.directMapChildren != 0 || scope.lastBinding != fact.bindingBefore
            || scope.acquisitionRoot == null || scope.acquisitionRoot != scope.persistentRoot
            || scope.acquisitionRoot != ((Actor) scope.player).ftboss$rawPersistentData()) return false;
        boolean timer = reason == 20 && §RANGED_LONG§.equals(fact.site);
        boolean root = reason == 21 && §RANGED_ROOT§.equals(fact.site);
        if (!timer && !root || owner != fact.receiver || !Objects.equals(fact.originalStep.expected.key, fact.key)) return false;
        var frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(NativeExpectedFieldObservation::rangedFrames431);
        if (frames.size() < 3 || frames.get(0).getDeclaringClass() != OwnerFullRepairGuard.class
            || !frames.get(0).getMethodName().equals(§changed§) || !frames.get(0).getDescriptor().equals(§(Ljava/lang/Object;I)V§)
            || frames.get(1).getDeclaringClass() != CompoundTag.class || frames.get(2).getDeclaringClass() != CompoundTag.class
            || !frames.get(1).getMethodName().endsWith(timer ? §$ftboss$fullRepairVoidMutation§ : §$ftboss$fullRepairReturnedMutation§)
            || !frames.get(1).getDescriptor().equals(timer ? §(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V§
                : §(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V§)
            || !frames.get(2).getMethodName().equals(timer ? §putLong§ : §put§)
            || !frames.get(2).getDescriptor().equals(timer ? §(Ljava/lang/String;J)V§ : §(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;§)) return false;
        var operation = fact.originalStep.expected;
        State before = operation.before;
        if (!before.rangedAcquired || before.rangedPlayerIdentity != scope.acquisitionRoot
            || before.rangedBufferIdentity != scope.rangedBuffer
            || before.rangedAttached != (fact.bindingBefore.bound == scope.rangedBuffer)
            || !FoodWeaponExpectedTrace.same(before.player, NativeExpectedRawFields.observe(scope.acquisitionRoot))
            || !FoodWeaponExpectedTrace.same(before.rangedBuffer, NativeExpectedRawFields.observe(scope.rangedBuffer))) return false;
        return timer
            ? operation.kind == Kind.RANGED_TIMER && owner == scope.rangedBuffer && fact.argument instanceof Long value
                && operation.argument != null && operation.argument.kind == 4 && operation.argument.bits == value
            : operation.kind == Kind.RANGED_ROOT && owner == scope.acquisitionRoot
                && §FTBossRangedProcTimers§.equals(fact.key) && fact.argument == scope.rangedBuffer
                && FoodWeaponExpectedTrace.same(operation.argument, before.rangedBuffer);
    }

'''.replace('§', chr(34))
for name in ['OwnerFullRepairGuard.java', 'NativeExpectedFieldObservation.java']:
    original = next(path for path in sources if path.name == name)
    text = original.read_text(encoding='utf-8')
    if name == 'OwnerFullRepairGuard.java':
        before = '                OwnerFieldExpectedAuthority.changed(var0);' + chr(10) + '                OwnerFullRepairGuard.Node var5 = node(var0, var1 == 40);'
        after = '''                if (!NativeExpectedFieldObservation.plannedRangedFieldMutation(var0, var1)) {
                    OwnerFieldExpectedAuthority.changed(var0);
                }
                OwnerFullRepairGuard.Node var5 = node(var0, var1 == 40);'''
    else:
        before = '    public static CompoundTag compound(CompoundTag var0, String var1, Operation<CompoundTag> var2) {'
        after = helper + before
    assert text.count(before) == 1, name
    target = WORK / 'proposal/net/farmingtales/ftboss/item' / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text.replace(before, after), encoding='utf-8')
    targets.append(target)
    changes.append({'beforeSource': str(original), 'source': str(target), 'before': before, 'after': after})
attempt = WORK / 'build' / ('continuation-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(parent / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, targets)]
save(attempt / 'compile-command.json', command)
with (attempt / 'compile.log').open('wb') as stream:
    result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal.json', {'exitCode': result.returncode})
assert result.returncode == 0, str(attempt / 'compile.log')
save(WORK / 'reports/functional-compiled.json', {
    'attempt': str(attempt), 'parentAttempt': str(parent), 'failures': 0, 'changes': changes,
    'behavior': 'Preserve player field continuity only during the exact independently planned native ranged putLong/put entry; retain other revocation notifications and existing post-write validation.',
    'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, Path(__file__), *targets, *[Path(change['beforeSource']) for change in changes]]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')},
})
print({'compiled': True, 'attempt': str(attempt), 'functionalSources': 2})
