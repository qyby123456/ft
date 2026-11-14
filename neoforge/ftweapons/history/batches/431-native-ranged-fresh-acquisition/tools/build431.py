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
binding = LOCAL / 'batches/427-native-recipe-stack-binding/reports/actual-incremental427-experimental.json'
assert digest(binding) == 'b2d1b76b5fb5228896ccd4e048685ac2f1055641332b73b778f1b31bc5401b3e'
receipt = load(binding)
for name, expected in receipt['inputHashes'].items():
    assert digest(name) == expected, name
prior = Path(receipt['attempt'])
arguments = load(prior / 'complete-arguments316.json')
originals = [Path(value) for value in arguments if value.endswith('.java')]
changes = []
sources = []
inputs = []

def replace(text, before, after):
    assert text.count(before) == 1, before
    changes.append({'before': before, 'after': after})
    return text.replace(before, after)

for name in ['FoodWeaponExpectedTrace', 'OwnerTimerSemanticAuthority', 'NativeFreshFactoryTrust298', 'NativeExpectedFieldObservation']:
    original = next(path for path in originals if path.name == name + '.java')
    inputs.append(original)
    text = original.read_text(encoding='utf-8')
    start = len(changes)
    if name == 'FoodWeaponExpectedTrace':
        marker = '        Operation rangedLong(String string, long longValue) {'
        addition = '''        Operation rangedAcquireDetached431(Object root, Object buffer) {
            this.writable();
            Value prior = FoodWeaponExpectedTrace.field(this.expected.player, §FTBossRangedProcTimers§);
            if (root == null || buffer == null || prior != null && prior.kind == 10
                || this.expected.rangedAcquired
                || this.expected.rangedPlayerIdentity != null && this.expected.rangedPlayerIdentity != root) {
                throw new IllegalArgumentException(§Independent detached ranged allocation intent required§);
            }
            Value empty = FoodWeaponExpectedTrace.emptyCompound();
            State after = new State(this.expected.weapon, this.expected.player, this.expected.carrier, empty, false, root, buffer, true);
            Operation operation = new Operation(this.operations.size(), Kind.RANGED_ACQUIRE, this.expected, this.expected, after, null,
                §FTBossRangedProcTimers§, empty, new Mutation[0], null);
            this.operations.add(operation);
            this.expected = after;
            return operation;
        }

'''.replace('§', chr(34))
        text = replace(text, marker, addition + marker)
    elif name == 'OwnerTimerSemanticAuthority':
        marker = '        private final Object rangedBuffer;'
        text = replace(text, marker, marker + chr(10) + '        private Object acquiredRangedBuffer431;')
        before = '''                                return this.rangedBuffer instanceof CompoundTag && this.rangedRoot != null
                                    ? var7.rangedAcquire(this.rangedRoot, this.rangedBuffer)
                                    : null;'''
        after = '''                                Object plannedBuffer = this.rangedBuffer != null ? this.rangedBuffer : this.acquiredRangedBuffer431;
                                if (plannedBuffer instanceof CompoundTag && this.rangedRoot != null) {
                                    return var7.rangedAcquire(this.rangedRoot, plannedBuffer);
                                }
                                CompoundTag fresh = NativeExpectedFieldObservation.plannedRangedFresh431(var1, var2, this.rangedRoot, var6);
                                if (fresh == null || this.acquiredRangedBuffer431 != null) return null;
                                Operation acquisition = var7.rangedAcquireDetached431(this.rangedRoot, fresh);
                                this.acquiredRangedBuffer431 = fresh;
                                return acquisition;'''
        text = replace(text, before, after)
    elif name == 'NativeFreshFactoryTrust298':
        before = '            if (factory.getDeclaringClass() == CompoundTag.class) return factoryMethods.contains(factory.getMethodName() + factory.getDescriptor());'
        after = '''            if (factory.getDeclaringClass() == CompoundTag.class) {
                boolean trusted = factoryMethods.contains(factory.getMethodName() + factory.getDescriptor());
                if (trusted && factory.getMethodName().equals(§<init>§) && factory.getDescriptor().equals(§()V§)) {
                    NativeExpectedFieldObservation.rangedFreshAllocation431(owner, backing);
                }
                return trusted;
            }'''.replace('§', chr(34))
        text = replace(text, before, after)
    else:
        marker = '        String[] path;'
        text = replace(text, marker, marker + chr(10) + '        CompoundTag rangedFreshOwner431;' + chr(10) + '        boolean rangedFreshRepeated431;')
        marker = '    public static CompoundTag compound(CompoundTag var0, String var1, Operation<CompoundTag> var2) {'
        addition = '''    private static java.util.List<StackWalker.StackFrame> rangedFrames431(java.util.stream.Stream<StackWalker.StackFrame> frames) {
        return frames.filter(NativeExpectedFieldObservation::rangedExternalFrame431).limit(12).toList();
    }

    private static boolean rangedExternalFrame431(StackWalker.StackFrame frame) {
        return frame.getDeclaringClass() != NativeExpectedFieldObservation.class;
    }

    public static void rangedFreshAllocation431(Object owner, Map<?, ?> backing) {
        requireReadCaller(NativeFreshFactoryTrust298.class, §freshMap§, §(Ljava/lang/Object;Ljava/util/Map;)Z§);
        Scope scope = current();
        Fact fact = scope == null ? null : scope.active;
        if (fact == null || !§ACQUIRE§.equals(fact.site) || !parent(fact, §RANGED_SAVE§)
            || !fact.attempted || fact.returned || fact.thread != Thread.currentThread()
            || !§FTBossRangedProcTimers§.equals(fact.key) || fact.receiver != scope.persistentRoot) return;
        java.util.List<StackWalker.StackFrame> frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::rangedFrames431);
        if (frames.size() < 5 || frames.get(0).getDeclaringClass() != NativeFreshFactoryTrust298.class
            || frames.get(1).getDeclaringClass() != CompoundTag.class
            || !frames.get(1).getMethodName().endsWith(§$ftboss$observeConstructorMap280§)
            || !frames.get(1).getDescriptor().equals(§(Ljava/util/Map;)Ljava/util/Map;§)
            || frames.get(2).getDeclaringClass() != CompoundTag.class || !frames.get(2).getMethodName().equals(§<init>§)
            || !frames.get(2).getDescriptor().equals(§(Ljava/util/Map;)V§)
            || frames.get(3).getDeclaringClass() != CompoundTag.class || !frames.get(3).getMethodName().equals(§<init>§)
            || !frames.get(3).getDescriptor().equals(§()V§)
            || frames.get(4).getDeclaringClass() != CompoundTag.class || !frames.get(4).getMethodName().equals(§getCompound§)
            || !frames.get(4).getDescriptor().equals(§(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;§)
            || owner == null || owner.getClass() != CompoundTag.class || backing == null
            || backing.getClass() != HashMap.class || !backing.isEmpty()) return;
        if (fact.rangedFreshOwner431 != null) fact.rangedFreshRepeated431 = true;
        else fact.rangedFreshOwner431 = (CompoundTag) owner;
    }

    static CompoundTag plannedRangedFresh431(FoodWeaponOriginalSequence.Session session, Object leaf, Object root, State expected) {
        requireReadCaller(OwnerTimerSemanticAuthority.Book.class, §resolve§,
            §(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Ljava/lang/Object;Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Kind;Ljava/lang/String;Ljava/lang/String;Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$State;)Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Operation;§);
        Scope scope = current();
        Fact fact = scope == null ? null : scope.active;
        if (fact == null || !eligible(scope) || scope.sequence != session || scope.leaf != leaf
            || scope.thread != Thread.currentThread() || !§ACQUIRE§.equals(fact.site) || !parent(fact, §RANGED_SAVE§)
            || !fact.attempted || !fact.returned || fact.originalFailure != null || fact.rangedFreshRepeated431
            || root == null || root != scope.persistentRoot || root != fact.receiver || root != scope.acquisitionRoot
            || root != ((Actor) scope.player).ftboss$rawPersistentData()
            || !§FTBossRangedProcTimers§.equals(fact.key) || expected == null || expected.rangedAcquired) return null;
        Value prior = FoodWeaponExpectedTrace.field(expected.player, §FTBossRangedProcTimers§);
        if (prior != null && prior.kind == 10 || !FoodWeaponExpectedTrace.same(expected.player, NativeExpectedRawFields.observe((Tag) root))) return null;
        CompoundTag fresh = fact.rangedFreshOwner431;
        if (fresh == null || fresh != fact.originalReturn || fresh != scope.rangedBuffer || fresh.getClass() != CompoundTag.class
            || ((NativeNbtMutationFields) fresh).ftboss$nativeNbtRawEscaped()
            || ((NativeNbtMutationFields) fresh).ftboss$nativeNbtMutationVersion() != 0L
            || !FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.emptyCompound(), NativeExpectedRawFields.observe(fresh))) return null;
        return fresh;
    }

'''.replace('§', chr(34))
        text = replace(text, marker, addition + marker)
    target = BATCH / 'proposal/net/farmingtales/ftboss/item' / (name + '.java')
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    restored = text
    for change in reversed(changes[start:]):
        assert restored.count(change['after']) == 1
        restored = restored.replace(change['after'], change['before'])
    assert restored == original.read_text(encoding='utf-8')
    sources.append(target)
attempt = BATCH / 'build' / ('fresh-acquisition431-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, sources)]
save(attempt / 'compile-command.json', command)
with (attempt / 'compile.log').open('wb') as stream:
    result = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal.json', {'exitCode': result.returncode})
assert result.returncode == 0, str(attempt / 'compile.log')
save(BATCH / 'reports/actual-fresh431-compiled-not-adopted.json', {
    'failures': 0, 'attempt': str(attempt), 'priorAttempt': str(prior), 'changes': changes,
    'sourcesReversedExactly': len(sources), 'cumulativeCandidateUpdated': False,
    'actualRuntimeAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, Path(__file__), *inputs, *sources]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')},
})
print({'compiled': True, 'sourcesReversedExactly': len(sources), 'attempt': str(attempt), 'adopted': False})
