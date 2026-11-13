import difflib
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
WORK = Path(__file__).resolve().parents[1]
SOURCES = json.loads((WORK / 'reports/effective-sources.json').read_text(encoding='utf-8-sig'))
PATCHER = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'


def replace(text, before, after):
    assert text.count(before) == 1, before[:100]
    return text.replace(before, after)


def edit(name, transform):
    target = WORK / 'proposal-native/net/farmingtales/ftboss/item' / name
    exists = target.exists()
    before = target.read_text(encoding='utf-8') if exists else ''
    original = before if exists else Path(SOURCES[name]).read_text(encoding='utf-8-sig')
    after = transform(original)
    if exists:
        lines = list(difflib.unified_diff(before.splitlines(), after.splitlines(), n=3))[2:]
        lines = ['@@' if line.startswith('@@') else line for line in lines]
        patch = '*** Begin Patch\n*** Update File: ' + target.relative_to(ROOT).as_posix() + '\n' + '\n'.join(lines) + '\n*** End Patch\n'
        subprocess.run([PATCHER, '--codex-run-as-apply-patch', patch], cwd=ROOT, check=True)
    else:
        completed = []
        remaining = after.splitlines()
        while remaining:
            chunk = []
            size = 0
            while remaining and (not chunk or size + len(remaining[0]) < 10000):
                line = remaining.pop(0)
                chunk.append(line)
                size += len(line) + 3
            if completed:
                patch = '*** Begin Patch\n*** Update File: ' + target.relative_to(ROOT).as_posix() + '\n@@\n'
                patch += ''.join(' ' + line + '\n' for line in completed[-10:])
                patch += ''.join('+' + line + '\n' for line in chunk) + '*** End of File\n*** End Patch\n'
            else:
                patch = '*** Begin Patch\n*** Add File: ' + target.relative_to(ROOT).as_posix() + '\n' + ''.join('+' + line + '\n' for line in chunk) + '*** End Patch\n'
            subprocess.run([PATCHER, '--codex-run-as-apply-patch', patch], cwd=ROOT, check=True)
            completed.extend(chunk)


def plan(text):
    method = '''
    OwnerStatueTaskWritePlan(CompoundTag before, Operation operation, java.util.UUID task, int index, int payment, net.minecraft.core.HolderLookup.Provider registries) {
        this.registries = registries;
        this.cursor = before.copy();
        this.operation = operation;
        this.index = index;
        CompoundTag lifecycle = life(before);
        this.level = lifecycle.getCompound("RepairQuest").getInt("Level");
        this.maximum = 0;
        this.profile = lifecycle.getString("RepairProfile");
        CompoundTag after = OwnerStatueTaskPlan.nativeRepair(before, operation, task, index, payment, registries);
        CompoundTag planned = before.copy();
        if (operation == Operation.START_REPAIR) {
            this.append(planned, "FTBossWeaponLifecycle", "RepairQuest", life(after).getCompound("RepairQuest").copy());
            this.revision(planned);
        } else if (operation == Operation.SUBMIT_REPAIR) {
            this.submit(payment);
        } else if (operation == Operation.CLAIM_REPAIR) {
            this.append(planned, "FTBossWeaponLifecycle", "RepairQuest", null);
            this.revision(planned);
            this.append(planned, "", "Damage", IntTag.valueOf(0));
            this.append(planned, "FTBossWeaponLifecycle", "Broken", null);
        } else throw new IllegalArgumentException("Native task write operation");
        if (this.steps.isEmpty() || !this.steps.get(this.steps.size() - 1).after().equals(after)) throw new IllegalStateException("Native task independent plans disagree");
    }
'''
    return replace(text, '    private static CompoundTag life(', method + '\n    private static CompoundTag life(')


edit('OwnerStatueTaskWritePlan.java', plan)


def wear(text):
    methods = '''
    static void beforeNativeRepairWrite(ItemStack actual, Object receiver, String key, net.minecraft.nbt.Tag value) {
        Claim claim = CLAIMS.get(actual);
        if (claim != null) claim.beforeWrite(receiver, key, value);
    }

    static void afterNativeRepairWrite(ItemStack actual, Throwable failure) {
        Claim claim = CLAIMS.get(actual);
        if (claim != null) claim.afterWrite(failure);
    }

    static void nativeRepairChanged(Object actual, Object receiver, int kind) {
        Claim claim = actual instanceof ItemStack stack ? CLAIMS.get(stack) : null;
        if (claim == null) return;
        if (claim.thread != Thread.currentThread() || claim.writing == null || claim.receiver != receiver || (kind != 20 && kind != 21)) {
            claim.rejected = true;
            return;
        }
        boolean allowed = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> {
            java.util.List<StackWalker.StackFrame> path = frames.takeWhile(frame -> frame.getDeclaringClass() != OwnerStatueTaskWrites.class
                && !(frame.getDeclaringClass() == FoodWeaponLifecycle.class && frame.getMethodName().equals("restoreAfterQuest"))).toList();
            return path.stream().allMatch(frame -> frame.getDeclaringClass().getNestHost() == NativeFoodWeaponWear.class
                || frame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                || frame.getDeclaringClass() == net.minecraft.nbt.CompoundTag.class || frame.getDeclaringClass() == ItemStack.class
                || frame.getDeclaringClass().getName().startsWith("net.minecraft.core.component.")
                || frame.getMethodName().startsWith("ftboss$"));
        });
        if (!allowed) claim.rejected = true;
    }
'''
    text = replace(text, '    static void abortedStatueRepair(', methods + '\n    static void abortedStatueRepair(')
    text = replace(text, '        private boolean committed;',
        '        private final OwnerStatueTaskWritePlan plan;\n        private OwnerStatueTaskWritePlan.Step writing;\n        private Object receiver;\n        private boolean rejected;\n        private boolean committed;')
    text = replace(text, '            this.documentOwner = UUID.nameUUIDFromBytes(',
        '            this.plan = new OwnerStatueTaskWritePlan(before, operation, task, index, payment, player.registryAccess());\n            this.documentOwner = UUID.nameUUIDFromBytes(')
    methods = '''
        private void beforeWrite(Object receiver, String key, net.minecraft.nbt.Tag value) {
            boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2L).findFirst()
                .map(frame -> frame.getDeclaringClass() == OwnerStatueTaskWrites.class || frame.getDeclaringClass() == FoodWeaponLifecycle.class).orElse(false));
            if (!caller || this.rejected || this.writing != null || this.thread != Thread.currentThread()
                || !this.plan.cursor.equals(NativeFoundationItemData.image(this.weapon, this.player.registryAccess()))) throw new IllegalStateException("Native task original prefix mismatch");
            OwnerStatueTaskWritePlan.Step step = this.plan.expected(key, value);
            Object expectedReceiver = this.weapon;
            if (!step.path().isEmpty()) {
                CompoundTag compound = NativeSourceLedgerItemData.rawCustomDataForObservation(this.weapon);
                for (String part : step.path().split("/")) compound = compound.getCompound(part);
                expectedReceiver = compound;
            }
            if (receiver != expectedReceiver) throw new IllegalStateException("Native task original receiver mismatch");
            this.writing = step;
            this.receiver = receiver;
        }

        private void afterWrite(Throwable failure) {
            try {
                if (failure != null || this.rejected || this.writing == null
                    || !this.writing.after().equals(NativeFoundationItemData.image(this.weapon, this.player.registryAccess()))) {
                    this.rejected = true;
                    if (failure == null) throw new IllegalStateException("Native task write did not match independent prefix");
                    return;
                }
                this.plan.advance(this.writing);
            } finally {
                this.writing = null;
                this.receiver = null;
            }
        }
'''
    text = replace(text, '        private void persist(', methods + '\n        private void persist(')
    text = replace(text, '            if (!this.terminal(actual) || !this.expected.equals(',
        '            if (this.rejected || this.writing != null || !this.plan.complete() || !this.plan.cursor.equals(this.expected)\n                || !this.terminal(actual) || !this.expected.equals(')
    text = replace(text, '            return CLAIMS.get(actual) == this && this.committed && this.terminal(actual)',
        '            return !this.rejected && this.writing == null && this.plan.complete() && CLAIMS.get(actual) == this && this.committed && this.terminal(actual)')
    return text


edit('NativeFoodWeaponWear.java', wear)


def writes(text):
    text = replace(text, '        OwnerStatueTaskOrigin.Write var5 = null;',
        '        NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var1, var2, var3.get());\n        OwnerStatueTaskOrigin.Write var5 = null;')
    text = replace(text, '                OwnerStatueTaskOrigin.afterWrite(var5, var6);',
        '                NativeFoodWeaponWear.afterNativeRepairWrite(var0, var6);\n                OwnerStatueTaskOrigin.afterWrite(var5, var6);')
    return text


def writes(text):
    start = text.index('    private static void write(')
    end = text.index('    static boolean hasWeaponIdentity', start)
    section = text[start:end]
    section = replace(section, '        OwnerStatueTaskOrigin.Write var5 = null;',
        '        NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var1, var2, var3.get());\n        OwnerStatueTaskOrigin.Write var5 = null;')
    section = replace(section, '        } finally {\n            try {',
        '        } finally {\n            NativeFoodWeaponWear.afterNativeRepairWrite(var0, var6);\n            try {')
    text = text[:start] + section + text[end:]
    start = text.index('    static void advanceProgress(')
    end = text.index('    static void put(', start)
    section = text[start:end]
    section = replace(section, '        OwnerStatueTaskOrigin.Write var5 = null;',
        '        int[] planned = var2.clone();\n        planned[var3] = Math.addExact(planned[var3], var4);\n        NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var1, "ProgressArray", new IntArrayTag(planned));\n        OwnerStatueTaskOrigin.Write var5 = null;')
    section = replace(section, '        } finally {\n            try {',
        '        } finally {\n            NativeFoodWeaponWear.afterNativeRepairWrite(var0, var6);\n            try {')
    return text[:start] + section + text[end:]


edit('OwnerStatueTaskWrites.java', writes)
edit('FoodWeaponRepair.java', lambda text: replace(text,
    '        NativeRepairTaskData.ensure(var0).putInt("RepairRevision", revision(var0) == Integer.MAX_VALUE ? 0 : revision(var0) + 1);',
    '        OwnerStatueTaskWrites.putInt(var0, NativeRepairTaskData.ensure(var0), "RepairRevision", revision(var0) == Integer.MAX_VALUE ? 0 : revision(var0) + 1);'))


def lifecycle(text):
    return replace(text, '''            var0.setDamageValue(0);
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            if (var1 != null) {
                FoodWeaponData.edit(var0, "FTBossWeaponLifecycle", current -> current.remove("Broken"));
            }''', '''            NativeFoodWeaponWear.beforeNativeRepairWrite(var0, var0, "Damage", net.minecraft.nbt.IntTag.valueOf(0));
            Throwable failure = null;
            try {
                var0.setDamageValue(0);
            } catch (RuntimeException | Error original) {
                failure = original;
                throw original;
            } finally {
                NativeFoodWeaponWear.afterNativeRepairWrite(var0, failure);
            }
            net.minecraft.world.item.component.CustomData data = var0.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            CompoundTag root = data == null ? null : data.getUnsafe();
            if (root != null && root.contains("FTBossWeaponLifecycle", 10)) {
                OwnerStatueTaskWrites.remove(var0, root.getCompound("FTBossWeaponLifecycle"), "Broken");
            }''')


edit('FoodWeaponLifecycle.java', lifecycle)


def guard(text):
    text = replace(text, '                    deny(var3, var0.record, var2);',
        '                    NativeFoodWeaponWear.nativeRepairChanged(var0.actual, var1, var2);\n                    deny(var3, var0.record, var2);')
    text = replace(text, '                        var2.add(OwnerRepairGenerationAuthority.invalidate(var5.actual.actual.get()));',
        '                        NativeFoodWeaponWear.nativeRepairChanged(var5.actual.actual.get(), var0, var1);\n                        var2.add(OwnerRepairGenerationAuthority.invalidate(var5.actual.actual.get()));')
    text = replace(text, '                            var2.add(OwnerRepairGenerationAuthority.invalidate(var10.actual.get()));',
        '                            NativeFoodWeaponWear.nativeRepairChanged(var10.actual.get(), var0, var1);\n                            var2.add(OwnerRepairGenerationAuthority.invalidate(var10.actual.get()));')
    return text


edit('OwnerFullRepairGuard.java', guard)
