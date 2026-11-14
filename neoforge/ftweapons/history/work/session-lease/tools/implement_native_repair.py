import difflib
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
WORK = Path(__file__).resolve().parents[1]
SOURCES = json.loads((WORK / 'reports/effective-sources.json').read_text(encoding='utf-8-sig'))
PATCHER = Path('C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe')
owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
claim = json.loads((ROOT / 'WRITE_OWNER.claim').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId'] == claim['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'


def replace_once(text, before, after):
    assert text.count(before) == 1, before[:100]
    return text.replace(before, after)


def emit(name, text):
    target = WORK / 'proposal-native/net/farmingtales/ftboss/item' / name
    lines = text.splitlines()
    completed = target.read_text(encoding='utf-8').splitlines() if target.exists() else []
    assert lines[:len(completed)] == completed, str(target)
    while len(completed) < len(lines):
        chunk = []
        size = 0
        for line in lines[len(completed):]:
            if chunk and size + len(line) > 12000:
                break
            chunk.append(line)
            size += len(line) + 3
        if completed:
            patch = '*** Begin Patch\n*** Update File: ' + target.relative_to(ROOT).as_posix() + '\n@@\n'
            patch += ''.join(' ' + line + '\n' for line in completed[-10:])
            patch += ''.join('+' + line + '\n' for line in chunk) + '*** End of File\n*** End Patch\n'
        else:
            patch = '*** Begin Patch\n*** Add File: ' + target.relative_to(ROOT).as_posix() + '\n'
            patch += ''.join('+' + line + '\n' for line in chunk) + '*** End Patch\n'
        subprocess.run([str(PATCHER), '--codex-run-as-apply-patch', patch], cwd=ROOT, check=True)
        completed.extend(chunk)


text = Path(SOURCES['OwnerStatueTaskPlan.java']).read_text(encoding='utf-8-sig')
method = '''
    static CompoundTag nativeRepair(CompoundTag before, Operation operation, UUID task, int index, int payment, HolderLookup.Provider registries) {
        if (before.contains("ForgeCaps") || task == null) throw new IllegalArgumentException("Unsupported native repair image");
        CompoundTag after = before.copy();
        CompoundTag data = NativeRepairStackImage.data(after);
        CompoundTag lifecycle = data.getCompound("FTBossWeaponLifecycle");
        ItemStack planned = NativeRepairStackImage.decode(registries, before.copy());
        if (!NativeFoodWeaponWear.eligible(planned) || !lifecycle.hasUUID("Weapon")) throw new IllegalArgumentException("Missing original native wear");
        int level = FoodWeaponEvolution.evolutionLevel(planned);
        if (operation == Operation.START_REPAIR) {
            if (lifecycle.contains("RepairQuest") || NativeRepairStackImage.damage(before) <= 0) throw new IllegalArgumentException("Native repair START state");
            CompoundTag quest = new CompoundTag();
            quest.putInt("Version", 1);
            quest.putUUID("NativeTask", task);
            quest.putInt("Level", level);
            quest.putString("Profile", FoodWeaponLifecycle.repairProfile(planned));
            quest.putIntArray("Progress", new int[3]);
            lifecycle.put("RepairQuest", quest);
        } else {
            CompoundTag quest = lifecycle.getCompound("RepairQuest");
            int[] progress = quest.getIntArray("Progress");
            if (!task.equals(quest.getUUID("NativeTask")) || quest.getInt("Level") != level || progress.length != 3
                || !quest.getString("Profile").equals(FoodWeaponLifecycle.repairProfile(planned))) throw new IllegalArgumentException("Native repair task ancestry");
            for (int taskIndex = 0; taskIndex < 3; taskIndex++) {
                if (progress[taskIndex] < 0 || progress[taskIndex] > FoodWeaponLifecycleRules.repairRequired(level, taskIndex)) throw new IllegalArgumentException("Native repair payment bounds");
            }
            if (operation == Operation.SUBMIT_REPAIR) {
                if (index < 0 || index >= 3 || payment <= 0 || payment > FoodWeaponLifecycleRules.repairRequired(level, index) - progress[index]) throw new IllegalArgumentException("Native repair payment quantity");
                progress[index] += payment;
                quest.putIntArray("Progress", progress);
            } else if (operation == Operation.CLAIM_REPAIR && payment == 0 && FoodWeaponLifecycleRules.ready(progress, level)) {
                lifecycle.remove("RepairQuest");
                lifecycle.remove("Broken");
                NativeRepairStackImage.putDamage(after, 0);
            } else throw new IllegalArgumentException("Native repair operation");
        }
        int revision = Math.max(0, lifecycle.getInt("RepairRevision"));
        lifecycle.putInt("RepairRevision", revision == Integer.MAX_VALUE ? 0 : revision + 1);
        NativeRepairStackImage.normalize(registries, after);
        return after;
    }
'''
text = replace_once(text, 'import java.util.TreeSet;', 'import java.util.TreeSet;\nimport java.util.UUID;')
text = replace_once(text, '    private static String foundationRules(', method + '\n    private static String foundationRules(')
emit('OwnerStatueTaskPlan.java', text)

text = Path(SOURCES['StatueRepairMutation.java']).read_text(encoding='utf-8-sig')
method = '''
    boolean nativeRepairTerminal(ServerPlayer player, ItemStack weapon, Operation operation) {
        if (this.operation != operation || this.player != player || this.weapon != weapon || this.phase != Phase.COMMITTED
            || Thread.currentThread() != this.thread || !this.server.isSameThread()) return false;
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass().getNestHost() == NativeFoodWeaponWear.class).orElse(false));
        if (!caller || !(this.restorePayments && this.request == null && taskUiLive() || this.nativeRequestLive(true))) return false;
        this.checkSource();
        for (Payment payment : this.payments) {
            if (this.player.getInventory().getItem(payment.slot()) != payment.supply() || payment.supply().getCount() != payment.after()
                || !payment.tag().matches(payment.supply())) return false;
        }
        return this.pending == null;
    }

    int nativeRepairPayment(ServerPlayer player, ItemStack weapon, int index) {
        if (nativeRepairSource(player, weapon) != this || this.operation != Operation.SUBMIT_REPAIR || this.task != index || this.pending != null) return -1;
        int amount = 0;
        for (Payment payment : this.payments) {
            if (this.player.getInventory().getItem(payment.slot()) != payment.supply() || payment.supply().getCount() != payment.after()
                || payment.before() - payment.after() != payment.requested() || !payment.tag().matches(payment.supply())) return -1;
            amount = Math.addExact(amount, payment.requested());
        }
        return amount;
    }
'''
text = replace_once(text, '    boolean taskOriginSource(', method + '\n    boolean taskOriginSource(')
text = replace_once(text, '                    this.pending = null;\n                    this.payments.clear();',
    '                    NativeFoodWeaponWear.abortedStatueRepair(this, this.player, this.weapon);\n                    this.pending = null;\n                    this.payments.clear();')
emit('StatueRepairMutation.java', text)

text = Path(SOURCES['OwnerNativeSemanticAuthority.java']).read_text(encoding='utf-8-sig')
method = '''
    static WeaponInput nativeRepairInput(ItemStack stack) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass().getNestHost() != NativeFoodWeaponWear.class || stack == null
            || !((Object)stack instanceof OwnerNativeWeaponInputFields fields)) return null;
        WeaponInput input = fields.ftboss$ownerNativeWeaponInput();
        if (input == null || input.retired || input.thread != Thread.currentThread() || input.actual == null || input.actual.get() != stack
            || !FoodWeaponExpectedTrace.same(input.expected, NativeExpectedRawFields.observe(NativeFoundationItemData.image(stack, input.registries)))) return null;
        return input;
    }

    static CompoundTag nativeRepairImage(WeaponInput input, ItemStack stack) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass().getNestHost() != NativeFoodWeaponWear.class || input == null || nativeRepairInput(stack) != input) return null;
        return ((CompoundTag)FoodWeaponExpectedTrace.materialize(input.expected)).copy();
    }

    static void nativeTransitionReturned(NativeFoodWeaponWear.Claim claim, ItemStack stack) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeFoodWeaponWear.class || !caller.getMethodName().equals("committedStatueRepair")
            || claim == null || !claim.authorizesInput(stack) || !((Object)stack instanceof OwnerNativeWeaponInputFields fields)) throw new IllegalStateException("Uncommitted native repair input");
        WeaponInput input = claim.input();
        if (input == null || input.thread != Thread.currentThread() || input.actual == null || input.actual.get() != stack
            || fields.ftboss$ownerNativeWeaponInput() != input) throw new IllegalStateException("Native repair input custody changed");
        CompoundTag expected = claim.expected();
        NativeItemComponentObservation after = NativeItemComponentObservation.capture(stack, input.registries);
        if (!after.matches(stack) || !expected.equals(NativeFoundationItemData.image(stack, input.registries))) throw new IllegalStateException("Native repair input differs from plan");
        WeaponInput successor = new WeaponInput(input.registries, input.originalInput.get(), NativeExpectedRawFields.observe(expected));
        successor.actual = new WeakReference<>(stack);
        successor.root = (CompoundTag)after.nativeWearStorage(true);
        successor.components = after.nativeWearStorage(false);
        input.retired = true;
        if (!fields.ftboss$ownerAdvanceNativeWeaponInput(input, successor)) {
            successor.retired = true;
            throw new IllegalStateException("Native repair input publication failed");
        }
    }
'''
text = replace_once(text, '    private static final ThreadLocal<NativeWear> NATIVE_WEAR', method + '\n    private static final ThreadLocal<NativeWear> NATIVE_WEAR')
text = replace_once(text, 'if (caller.getDeclaringClass().getNestHost() != NativeFoodWeaponWear.class || input == null || nativeRepairInput(stack) != input) return null;',
    'if (caller.getDeclaringClass().getNestHost() != NativeFoodWeaponWear.class || input == null || input.retired || input.thread != Thread.currentThread()\n            || input.actual == null || input.actual.get() != stack || !((Object)stack instanceof OwnerNativeWeaponInputFields fields)\n            || fields.ftboss$ownerNativeWeaponInput() != input\n            || !FoodWeaponExpectedTrace.same(input.expected, NativeExpectedRawFields.observe(NativeFoundationItemData.image(stack, input.registries)))) return null;')
emit('OwnerNativeSemanticAuthority.java', text)

text = Path(SOURCES['NativeFoodWeaponWear.java']).read_text(encoding='utf-8-sig')
text = replace_once(text, 'import java.io.IOException;', 'import java.io.IOException;\nimport java.nio.charset.StandardCharsets;\nimport net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;')
text = replace_once(text, '                        var8.putIntArray("Paid", new int[3]);',
    '                        Claim prepared = prepareJournal(var0, var1, var7, Operation.START_REPAIR, -1, 0);\n                        var8.putIntArray("Paid", new int[3]);')
text = replace_once(text, '                        var2.putUUID("NativeTask", var7);', '                        var2.putUUID("NativeTask", var7);')
text = replace_once(text, '            var6[var3] = Math.addExact(var6[var3], var4);',
    '            StatueRepairMutation outer = StatueRepairMutation.nativeRepairSource(var0, var1);\n            if (outer == null || outer.nativeRepairPayment(var0, var1, var3) != var4) throw new IllegalStateException("Native repair lacks actual payment");\n            prepareJournal(var0, var1, var2.getUUID("NativeTask"), Operation.SUBMIT_REPAIR, var3, var4);\n            var6[var3] = Math.addExact(var6[var3], var4);')
text = replace_once(text, '                var4.putBoolean("Consumed", true);',
    '                prepareJournal(var0, var1, var2.getUUID("NativeTask"), Operation.CLAIM_REPAIR, -1, 0);\n                var4.putBoolean("Consumed", true);')
text = replace_once(text, '                CLAIMS.put(var1, new NativeFoodWeaponWear.Claim(var3, var0, var7.getUUID("Identity"), var2.getUUID("NativeTask")));', '')
start = text.index('    static void committedStatueRepair(')
end = text.index('    private static final class Authority', start)
text = text[:start] + '''    static void committedStatueRepair(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon) {
        Claim claim = CLAIMS.get(weapon);
        if (claim == null) return;
        OwnerLineageRevocationStore.Lease issued = null;
        boolean completed = false;
        try {
            if (claim.outer != outer || !claim.terminal(weapon)) throw new IllegalStateException("Native repair terminal ancestry changed");
            Authority authority = authority(player.getServer());
            CompoundTag state = authority.weapons.getCompound(claim.nativeIdentity.toString());
            if (claim.operation == Operation.CLAIM_REPAIR) {
                if (!state.getCompound("Tasks").getCompound(claim.task.toString()).getBoolean("Consumed")) throw new IllegalStateException("Native repair claim not consumed");
                ItemStack previous = REPAIR_WRITE.get();
                REPAIR_WRITE.set(weapon);
                try {
                    FoodWeaponLifecycle.restoreAfterQuest(weapon);
                    state.putInt("Damage", 0);
                    state.putInt("Maximum", weapon.getMaxDamage());
                    checkpoint(player.getServer(), authority);
                } finally {
                    if (previous == null) REPAIR_WRITE.remove(); else REPAIR_WRITE.set(previous);
                }
            }
            claim.commitJournal(weapon);
            OwnerNativeSemanticAuthority.nativeTransitionReturned(claim, weapon);
            if (claim.operation == Operation.CLAIM_REPAIR) {
                issued = claim.store.consumeNativeRepair(claim);
                if (!OwnerRepairGenerationAuthority.publishNativeRepair(claim, weapon, issued)) throw new IllegalStateException("Native signed repair generation publication failed");
            }
            completed = true;
        } finally {
            CLAIMS.remove(weapon);
            if (!completed && issued != null) OwnerLineageRevocationStore.retireLease(issued);
        }
    }

    static void abortedStatueRepair(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon) {
        Claim claim = CLAIMS.get(weapon);
        if (claim != null && claim.outer == outer && claim.player == player) CLAIMS.remove(weapon);
    }

    private static Claim prepareJournal(ServerPlayer player, ItemStack weapon, UUID task, Operation operation, int index, int payment) {
        if (CLAIMS.containsKey(weapon)) throw new IllegalStateException("Native repair already pending");
        StatueRepairMutation outer = StatueRepairMutation.nativeRepairSource(player, weapon);
        OwnerNativeSemanticAuthority.WeaponInput input = OwnerNativeSemanticAuthority.nativeRepairInput(weapon);
        CompoundTag before = OwnerNativeSemanticAuthority.nativeRepairImage(input, weapon);
        if (outer == null || before == null || holder(weapon) != player || !OwnerRepairGenerationAuthority.statueTaskObjectPolicy(weapon)) throw new IllegalStateException("Native repair has no independent original input");
        Claim claim = new Claim(outer, player, weapon, task, operation, index, payment, input, before);
        claim.reserveJournal();
        CLAIMS.put(weapon, claim);
        return claim;
    }

''' + text[end:]
start = text.index('    private static record Claim(')
text = text[:start] + '''    static final class Claim {
        private final StatueRepairMutation outer;
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final UUID nativeIdentity;
        private final UUID weaponId;
        private final UUID task;
        private final Operation operation;
        private final int index;
        private final int payment;
        private final OwnerNativeSemanticAuthority.WeaponInput input;
        private final CompoundTag before;
        private final CompoundTag expected;
        private final OwnerLineageRevocationStore store;
        private final UUID documentOwner;
        private final Thread thread = Thread.currentThread();
        private CompoundTag document;
        private boolean committed;
        private boolean taken;
        private OwnerLineageRevocationStore.Lease issued;

        private Claim(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon, UUID task, Operation operation, int index, int payment,
                      OwnerNativeSemanticAuthority.WeaponInput input, CompoundTag before) {
            this.outer = outer;
            this.player = player;
            this.weapon = weapon;
            this.nativeIdentity = FoodWeaponData.read(weapon, "FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
            this.weaponId = FoodWeaponLifecycle.weaponId(weapon);
            this.task = task;
            this.operation = operation;
            this.index = index;
            this.payment = payment;
            this.input = input;
            this.before = before.copy();
            this.expected = OwnerStatueTaskPlan.nativeRepair(before, operation, task, index, payment, player.registryAccess());
            this.documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-journal/v1/" + player.getUUID()).getBytes(StandardCharsets.UTF_8));
            try {
                this.store = OwnerLineageRevocationStore.open(player.getServer());
                this.document = this.store.readRepairDocument(this.documentOwner, false);
            } catch (IOException failure) { throw new IllegalStateException("Native repair signed journal unavailable", failure); }
        }

        private void persist(CompoundTag next) {
            try {
                this.store.writeRepairDocument(this.documentOwner, false, this.document, next);
                this.document = next.copy();
            } catch (IOException failure) { throw new IllegalStateException("Native repair signed journal checkpoint failed", failure); }
        }

        private void reserveJournal() {
            CompoundTag next = this.document.copy();
            if (next.isEmpty()) {
                next.putInt("Schema", 1);
                next.putUUID("Owner", this.player.getUUID());
                next.put("Tasks", new CompoundTag());
            }
            if (next.getInt("Schema") != 1 || !this.player.getUUID().equals(next.getUUID("Owner"))) throw new IllegalStateException("Native repair journal owner mismatch");
            CompoundTag tasks = next.getCompound("Tasks");
            CompoundTag record = tasks.getCompound(this.task.toString());
            if (this.operation == Operation.START_REPAIR) {
                if (!record.isEmpty() || tasks.getAllKeys().size() >= 1024) throw new IllegalStateException("Native repair START replay or budget");
                record.putUUID("Weapon", this.weaponId);
                record.putUUID("NativeIdentity", this.nativeIdentity);
                record.putString("Rules", OwnerStatueTaskPlan.rules(this.weapon));
                record.putIntArray("Paid", new int[3]);
                record.putLong("Sequence", 0L);
            } else {
                if (!"COMMITTED".equals(record.getString("Status")) || !this.weaponId.equals(record.getUUID("Weapon"))
                    || !this.nativeIdentity.equals(record.getUUID("NativeIdentity")) || !this.before.equals(record.getCompound("Expected"))
                    || !OwnerStatueTaskPlan.rules(this.weapon).equals(record.getString("Rules"))) throw new IllegalStateException("Native repair signed predecessor missing or changed");
                int[] paid = record.getIntArray("Paid");
                if (paid.length != 3 || !Arrays.equals(paid, NativeRepairStackImage.data(this.before).getCompound("FTBossWeaponLifecycle").getCompound("RepairQuest").getIntArray("Progress"))) throw new IllegalStateException("Native repair signed payment ancestry");
                if (this.operation == Operation.SUBMIT_REPAIR) paid[this.index] = Math.addExact(paid[this.index], this.payment);
                record.putIntArray("Paid", paid);
            }
            record.putString("Status", "PENDING_ORIGINAL_TERMINAL");
            record.putString("Operation", this.operation.name());
            record.putLong("Sequence", Math.addExact(record.getLong("Sequence"), 1L));
            record.put("Before", this.before.copy());
            record.put("Expected", this.expected.copy());
            tasks.put(this.task.toString(), record);
            this.persist(next);
        }

        private boolean terminal(ItemStack actual) {
            return actual == this.weapon && this.thread == Thread.currentThread() && this.player.getServer().isSameThread()
                && holder(actual) == this.player && eligible(actual) && this.weaponId.equals(FoodWeaponLifecycle.weaponId(actual))
                && this.nativeIdentity.equals(FoodWeaponData.read(actual, "FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity"))
                && this.outer.nativeRepairTerminal(this.player, actual, this.operation);
        }

        private void commitJournal(ItemStack actual) {
            if (!this.terminal(actual) || !this.expected.equals(NativeFoundationItemData.image(actual, this.player.registryAccess()))) throw new IllegalStateException("Native repair original result differs from independent plan");
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            if (!"PENDING_ORIGINAL_TERMINAL".equals(record.getString("Status"))) throw new IllegalStateException("Native repair terminal replay");
            record.putString("Status", this.operation == Operation.CLAIM_REPAIR ? "CONSUMED" : "COMMITTED");
            this.persist(next);
            this.committed = true;
        }

        OwnerNativeSemanticAuthority.WeaponInput input() { return this.input; }
        CompoundTag expected() { return this.expected.copy(); }

        boolean authorizesInput(ItemStack actual) {
            return CLAIMS.get(actual) == this && this.committed && this.terminal(actual) && this.expected.equals(NativeFoundationItemData.image(actual, this.player.registryAccess()));
        }

        UUID owner() { return this.player.getUUID(); }
        UUID weaponId() { return this.weaponId; }

        boolean takeForStore(OwnerLineageRevocationStore store) {
            if (this.store != store || this.taken || this.operation != Operation.CLAIM_REPAIR || !this.authorizesInput(this.weapon)) return false;
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            if (!"CONSUMED".equals(record.getString("Status"))) return false;
            record.putString("Status", "LEASE_RESERVED_NO_REISSUE");
            this.persist(next);
            this.taken = true;
            return true;
        }

        void issued(OwnerLineageRevocationStore.Lease lease) {
            if (!this.taken || this.issued != null || !this.owner().equals(lease.owner) || !this.weaponId.equals(lease.weapon)) throw new IllegalStateException("Native repair lease identity");
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            record.putString("Status", "LEASE_ISSUED");
            record.putUUID("LeaseEpoch", lease.incarnation);
            record.putLong("LeaseGeneration", lease.generation);
            this.persist(next);
            this.issued = lease;
        }

        boolean owns(ItemStack actual, OwnerLineageRevocationStore.Lease lease) {
            return this.issued == lease && lease != null && this.authorizesInput(actual) && OwnerLineageRevocationStore.liveLease(lease);
        }
    }
}
'''
emit('NativeFoodWeaponWear.java', text)

text = Path(SOURCES['OwnerLineageRevocationStore.java']).read_text(encoding='utf-8-sig')
method = '''
    Lease consumeNativeRepair(NativeFoodWeaponWear.Claim claim) throws IOException {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeFoodWeaponWear.class && frame.getMethodName().equals("committedStatueRepair")).orElse(false));
        if (!caller || claim == null || !claim.takeForStore(this)) throw new IOException("Missing original signed native repair claim");
        Lease lease = null;
        boolean completed = false;
        try {
            lease = this.admit(claim.owner(), claim.weaponId());
            claim.issued(lease);
            completed = true;
            return lease;
        } finally {
            if (!completed && lease != null) retireLease(lease);
        }
    }
'''
text = replace_once(text, '    OwnerLineageRevocationStore.Lease admit(', method + '\n    OwnerLineageRevocationStore.Lease admit(')
emit('OwnerLineageRevocationStore.java', text)

text = Path(SOURCES['OwnerRepairGenerationAuthority.java']).read_text(encoding='utf-8-sig')
method = '''
    static boolean publishNativeRepair(NativeFoodWeaponWear.Claim claim, ItemStack actual, OwnerLineageRevocationStore.Lease lease) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == NativeFoodWeaponWear.class && frame.getMethodName().equals("committedStatueRepair")).orElse(false));
        if (!caller || claim == null || lease == null || !knownStackPolicy(actual) || !claim.owns(actual, lease)) return false;
        synchronized (OwnerFullRepairGuard.class) {
            if (OwnerFullRepairGuard.intent(actual) != null || !claim.expected().equals(NativeFoundationItemData.image(actual))) return false;
            Generation generation = new Generation(lease, false);
            OwnerFullRepairGuard.freshGeneration(actual, lease, generation);
            if (current(actual) != generation || OwnerFullRepairGuard.lease(actual) != lease) return false;
            generation.repaired = true;
            return true;
        }
    }
'''
text = replace_once(text, '    static boolean fresh(', method + '\n    static boolean fresh(')
emit('OwnerRepairGenerationAuthority.java', text)
