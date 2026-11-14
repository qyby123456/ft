import difflib
import hashlib
import json
import re
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
assert Path.cwd().resolve() == root
candidate = json.loads((work / "reports/native-repair-ranged-use-candidate-d71b9448.json").read_text(encoding="utf-8"))
arguments = json.loads((Path(candidate["attempt"]) / "complete-arguments316.json").read_text(encoding="utf-8"))
sources = {Path(name).name: Path(name) for name in arguments if Path(name).suffix == ".java"}
changes = {}

def source(name):
    original = sources[name]
    package = re.search(r"(?m)^package\s+([\w.]+)\s*;", original.read_text(encoding="utf-8")).group(1)
    target = work / "proposal-native" / Path(*package.split(".")) / name
    if name not in changes:
        changes[name] = [target, target.read_text(encoding="utf-8") if target.exists() else None, original.read_text(encoding="utf-8")]
    return changes[name][2]

def replace(name, before, after):
    text = source(name)
    assert text.count(before) == 1, (name, before[:80], text.count(before))
    changes[name][2] = text.replace(before, after, 1)

replace("FoodWeaponFinalDamage.java", "    private static boolean reserveListedAction(", '''    static FoodWeaponBossPassive.HitPlan originalPassivePlan(Object candidate, Object weapon, Object player, Object source) {
        boolean caller = ORIGINAL_CALLER.walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("quoteTimers"))
            .orElse(false));
        if (!caller || !(candidate instanceof PassiveProducer producer)) return null;
        PreparedBatch prepared = producer.prepared();
        return prepared != null && prepared.committed && prepared.thread == Thread.currentThread()
            && prepared.player == player && prepared.source == source && clock(prepared.player) == prepared.at
            && producer.plan.context().actualWeapon() == weapon
            && FoodWeaponBossPassive.matchesPreparedPassiveHit(producer.consumerPrepared, producer.plan,
                prepared.batch, prepared.admission, prepared.requests, prepared.targets) ? producer.plan : null;
    }

    private static boolean reserveListedAction(''')

replace("OwnerFieldExpectedAuthority.java", "                if (var7 instanceof OwnerEvolutionPlanFields var8) {", '''                FoodWeaponBossPassive.HitPlan passive = FoodWeaponFinalDamage.originalPassivePlan(var7, var2, var3, var4);
                if (passive != null) {
                    OwnerNativeSemanticAuthority.PutBinding binding = FoodWeaponBossPassive.quoteOwnerPublication(var0, var0.builder, passive);
                    if (binding == null || var0.nativePuts.put(passive, binding) != null) var0.unsupported = true;
                    continue;
                }
                if (var7 instanceof OwnerEvolutionPlanFields var8) {''')

replace("FoodWeaponBossPassive.java", "    private static boolean preparedCleanupCaller() {", '''    static OwnerNativeSemanticAuthority.PutBinding quoteOwnerPublication(
        OwnerFieldExpectedAuthority.Prepared prepared, FoodWeaponExpectedTrace.Builder builder, HitPlan plan
    ) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("quoteTimers"))
            .orElse(false));
        return caller && hitContext(plan) ? OwnerNativeSemanticAuthority.quotePassiveRoot(prepared, builder, plan,
            plan.context.player(), plan.context.actualWeapon(), plan.context.source(), plan.profile, plan.context.quotedAt(), plan.timers) : null;
    }

    static HitPlan ownerPublicationPlan(Object prepared, FoodWeaponConsumerSequence.Accepted accepted) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponConsumerSequence.class && frame.getMethodName().equals("nativePublicationKey"))
            .orElse(false));
        return caller && prepared instanceof PreparedHit hit && hit.originalSequence == accepted
            && hit.publishAttempted && !hit.publishExited && !hit.commitConstructed && hit.publishingCleanup != null
            ? hit.plan : null;
    }

    private static boolean preparedCleanupCaller() {''')

replace("FoodWeaponConsumerSequence.java", "    static final class Accepted {", '''    static Object nativePublicationKey(FoodWeaponOriginalSequence.Session session, Object leaf) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class
                    && (frame.getMethodName().equals("resolve") || frame.getMethodName().equals("returned"))
                || frame.getDeclaringClass() == FoodWeaponSourceLedger.class && frame.getMethodName().equals("ownerPassivePublicationEntered"))
            .orElse(false));
        if (!caller) return null;
        if (!(leaf instanceof Accepted accepted)) return leaf;
        return accepted.session == session && accepted.producerName.equals("PassiveProducer")
            && accepted.phase == Phase.PUBLISHING && accepted.lifetime() && session.original.atLeaf(accepted)
            ? FoodWeaponBossPassive.ownerPublicationPlan(accepted.prepared, accepted) : null;
    }

    static final class Accepted {''')

replace("FoodWeaponConsumerSequence.java", "        int publicationCallEntered(Object var1) {", '''        OwnerNativeSemanticAuthority.Admission passiveCopy(CompoundTag root, CompoundTag original) {
            boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
                .map(frame -> frame.getDeclaringClass() == FoodWeaponBossPassive.class && frame.getMethodName().equals("commitPassiveHit")
                    && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$PreparedHit;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$HitCommit;"))
                .orElse(false));
            return caller && this.producerName.equals("PassiveProducer") && this.phase == Phase.PUBLISHING
                && this.lifetime() && this.session.original.atLeaf(this)
                ? FoodWeaponSourceLedger.ownerPassivePublicationEntered(this.session, this, root, original) : null;
        }

        int publicationCallEntered(Object var1) {''')
replace("FoodWeaponConsumerSequence.java", "import net.minecraft.server.MinecraftServer;", "import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.server.MinecraftServer;")

replace("FoodWeaponSourceLedger.java", "    public static void ownerOriginalPublicationAssigned(Object var0, Object var1) {", '''    static OwnerNativeSemanticAuthority.Admission ownerPassivePublicationEntered(
        FoodWeaponOriginalSequence.Session session, FoodWeaponConsumerSequence.Accepted leaf, CompoundTag root, CompoundTag original
    ) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponConsumerSequence.Accepted.class && frame.getMethodName().equals("passiveCopy"))
            .orElse(false));
        if (caller && session != null && session.original.atLeaf(leaf)
            && session.lineage.issuer instanceof OwnerWriterPublication publication
            && publication.fieldSequenceBinding != null && publication.fieldSequenceBinding.sequence == session
            && publication.fieldPreparation != null && publication.fieldPreparation.lineage == session.lineage
            && OwnerFieldExpectedAuthority.unchanged(publication.fieldPreparation)) {
            Object plan = FoodWeaponConsumerSequence.nativePublicationKey(session, leaf);
            return OwnerNativeSemanticAuthority.admitPassive(session, publication.fieldPreparation.nativePuts.get(plan), leaf, root, original);
        }
        return null;
    }

    public static void ownerOriginalPublicationAssigned(Object var0, Object var1) {''')

replace("OwnerTimerSemanticAuthority.java", "                    OwnerNativeSemanticAuthority.PutBinding var11 = this.prepared.nativePuts.get(var2);\n                    if (var11 != null && !this.nativeReturned.contains(var2)) {", '''                    Object key = FoodWeaponConsumerSequence.nativePublicationKey(var1, var2);
                    OwnerNativeSemanticAuthority.PutBinding var11 = this.prepared.nativePuts.get(key);
                    if (key != null && var11 != null && !this.nativeReturned.contains(key)) {''')
replace("OwnerTimerSemanticAuthority.java", "                    this.nativeReturned.add(var2);", '''                    Object key = FoodWeaponConsumerSequence.nativePublicationKey(this.session, var2);
                    if (key == null || this.prepared.nativePuts.get(key) != var5 || !this.nativeReturned.add(key))
                        throw new IllegalArgumentException("Actual original native publication identity required");''')

replace("OwnerFullRepairGuard.java", "                if (OwnerNativeSemanticAuthority.nativeOperationMutation(var0, var1)) {", '''                if (OwnerNativeSemanticAuthority.plannedPassiveRootMutation(var0, var1)) {
                    return;
                }

                if (OwnerNativeSemanticAuthority.nativeOperationMutation(var0, var1)) {''')

replace("OwnerNativeSemanticAuthority.java", "    private static final ThreadLocal<OwnerNativeSemanticAuthority.ReadFrame> RAW_READ", "    private static final ThreadLocal<Admission> PASSIVE_ROOT = new ThreadLocal<>();\n    private static final ThreadLocal<OwnerNativeSemanticAuthority.ReadFrame> RAW_READ")
replace("OwnerNativeSemanticAuthority.java", "        private boolean copied;\n        private boolean entered;", "        private boolean copied;\n        private Object passiveLeaf;\n        private boolean passiveCopyReturned;\n        private int passiveNotices;\n        private boolean entered;")
replace("OwnerNativeSemanticAuthority.java", "        private final Value argument;\n        private final Thread thread = Thread.currentThread();", '''        private final Value argument;
        private Value passiveBefore;
        private String passiveProfile;
        private long passiveTick;
        private final Thread thread = Thread.currentThread();''')
replace("OwnerNativeSemanticAuthority.java", "        private PutQuote(Object var1, CompoundTag var2, String var3, Tag var4) {", '''        private PutQuote(Object leaf, CompoundTag receiver, Value argument, Value baseline, String profile, long tick) {
            this.leaf = leaf;
            this.receiver = receiver;
            this.key = "FTBossIntrinsicPassiveTimers";
            this.argument = argument;
            this.passiveBefore = baseline;
            this.passiveProfile = profile;
            this.passiveTick = tick;
        }

        private PutQuote(Object var1, CompoundTag var2, String var3, Tag var4) {''')

replace("OwnerNativeSemanticAuthority.java", "    static OwnerNativeSemanticAuthority.PutBinding appendPut(", '''    static PutBinding quotePassiveRoot(OwnerFieldExpectedAuthority.Prepared prepared, Builder builder,
        FoodWeaponBossPassive.HitPlan plan, net.minecraft.server.level.ServerPlayer player, ItemStack weapon,
        Object source, String profile, long tick, Tag originalTimers) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponBossPassive.class && frame.getMethodName().equals("quoteOwnerPublication"))
            .orElse(false));
        Before before = prepared == null ? null : prepared.nativeBefore;
        if (!caller || before == null || before.preparation != prepared || before.player != player || before.source != source
            || before.weapon.actual == null || before.weapon.actual.get() != weapon || !(before.playerRoot instanceof CompoundTag root)
            || FoodWeaponSourceLedger.clock(player) != tick || !List.of("sugar", "butcher", "saya", "wine", "defect").contains(profile)) return null;
        ReadContext reading = new ReadContext(before.weapon, weapon, prepared, before, null, null, null, plan, false);
        try {
            Value stored = FoodWeaponExpectedTrace.field(before.expected.player, "FTBossIntrinsicPassiveTimers");
            if (!matchesState(reading, before.expected) || (stored == null ? originalTimers != null
                : originalTimers == null || !FoodWeaponExpectedTrace.same(stored, NativeExpectedRawFields.observe(originalTimers)))) return null;
            Value baseline = stored != null && stored.kind == 10 ? stored : FoodWeaponExpectedTrace.emptyCompound();
            Value after = FoodWeaponExpectedTrace.put(baseline, profile, FoodWeaponExpectedTrace.number(4, tick));
            PutQuote quote = new PutQuote(plan, root, after, baseline, profile, tick);
            return new PutBinding(quote, Domain.PLAYER, new String[0], before,
                builder.nativePut(Domain.PLAYER, new String[0], quote.key, after));
        } catch (Throwable failure) {
            rejectRead(reading, failure);
            return null;
        }
    }

    private static boolean passiveCaller() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames
            .filter(frame -> frame.getDeclaringClass() != OwnerNativeSemanticAuthority.class).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponBossPassive.class && frame.getMethodName().equals("commitPassiveHit")
                && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$PreparedHit;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$HitCommit;"))
            .orElse(false));
    }

    static Admission admitPassive(FoodWeaponOriginalSequence.Session session, PutBinding binding, Object leaf,
        CompoundTag root, CompoundTag original) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponSourceLedger.class && frame.getMethodName().equals("ownerPassivePublicationEntered"))
            .orElse(false));
        if (!caller || session == null || binding == null || binding.quote.passiveBefore == null || original == null
            || binding.quote.receiver != root || binding.quote.thread != Thread.currentThread() || !session.original.atLeaf(leaf)
            || session.lineage != binding.before.preparation.lineage || PASSIVE_ROOT.get() != null) return null;
        FoodWeaponOriginalSequence.Step step = null;
        try {
            if (!FoodWeaponExpectedTrace.same(binding.quote.passiveBefore, NativeExpectedRawFields.observe(original))) return null;
            step = session.originalBegins(leaf, Kind.NATIVE_PUT, binding.quote.key, null);
            if (step == null) return null;
            Before before = binding.before;
            ReadContext reading = new ReadContext(before.weapon, before.weapon.actual.get(), before.preparation, before, session, step, binding, leaf, false);
            if (!matchesSemanticStep(binding, session, step) || !matchesState(reading, step.expected.before)) {
                rejectSaved(before, session, step, null, false);
                return null;
            }
            Admission admission = new Admission(session, step, binding, original);
            admission.passiveLeaf = leaf;
            session.originalCopyBegins(step, root, original);
            return admission;
        } catch (Throwable failure) {
            rejectSaved(binding.before, session, step, failure, false);
            return null;
        }
    }

    static void passiveCopied(Admission admission, CompoundTag original, CompoundTag copy) {
        if (!passiveCaller() || !active(admission)) return;
        try {
            if (admission.binding.quote.passiveBefore == null || admission.passiveCopyReturned || admission.originalValue != original
                || copy == original || !FoodWeaponExpectedTrace.same(admission.binding.quote.passiveBefore, NativeExpectedRawFields.observe(copy))) {
                rejectAdmission(admission, null, false);
                return;
            }
            admission.argument = copy;
            admission.sequence.originalCopyReturned(admission.step, admission.binding.quote.receiver, original, copy);
            admission.passiveCopyReturned = true;
        } catch (Throwable failure) { rejectAdmission(admission, failure, false); }
    }

    static void passivePrepared(Admission admission, CompoundTag copy, String profile, long tick) {
        if (!passiveCaller() || !active(admission)) return;
        try {
            if (!admission.passiveCopyReturned || admission.copied || admission.argument != copy
                || !admission.binding.quote.passiveProfile.equals(profile) || admission.binding.quote.passiveTick != tick) {
                rejectAdmission(admission, null, false);
                return;
            }
            admission.sequence.originalCopyFieldsObserved(admission.step, copy, NativeExpectedRawFields.observe(copy));
            admission.copied = true;
        } catch (Throwable failure) { rejectAdmission(admission, failure, false); }
    }

    static boolean plannedPassiveRootMutation(Object receiver, int reason) {
        Admission admission = PASSIVE_ROOT.get();
        if (!active(admission) || reason != 21 || admission.binding.quote.passiveBefore == null || !admission.entered
            || !admission.copied || admission.passiveNotices != 0 || receiver != admission.binding.quote.receiver) return false;
        try {
            var frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).limit(5L).toList());
            if (frames.size() < 4 || frames.get(0).getDeclaringClass() != OwnerFullRepairGuard.class || !frames.get(0).getMethodName().equals("changed")
                || frames.get(1).getDeclaringClass() != CompoundTag.class || !frames.get(1).getMethodName().endsWith("$ftboss$fullRepairReturnedMutation")
                || !frames.get(1).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
                || frames.get(2).getDeclaringClass() != CompoundTag.class || !frames.get(2).getMethodName().equals("put")
                || !frames.get(2).getDescriptor().equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")
                || frames.get(3).getDeclaringClass() != FoodWeaponBossPassive.class || !frames.get(3).getMethodName().equals("commitPassiveHit")
                || !frames.get(3).getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$PreparedHit;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$HitCommit;")) return false;
            Before before = admission.binding.before;
            ReadContext reading = new ReadContext(before.weapon, before.weapon.actual.get(), before.preparation, before,
                admission.sequence, admission.step, admission.binding, admission.passiveLeaf, false);
            if (!matchesState(reading, admission.step.expected.before)
                || !FoodWeaponExpectedTrace.same(admission.binding.quote.argument, NativeExpectedRawFields.observe((Tag)admission.argument))) return false;
            admission.passiveNotices++;
            return true;
        } catch (Throwable failure) { rejectAdmission(admission, failure, false); return false; }
    }

    static OwnerNativeSemanticAuthority.PutBinding appendPut(''')

native = source("OwnerNativeSemanticAuthority.java")
entering = native[native.index("    public static void entering("):native.index("    public static void returned(")]
passive_entering = entering.replace("public static void entering(", "static void passiveEntering(").replace('leafHook("ftboss$ownerNativePut")', "passiveCaller()")
passive_entering = passive_entering.replace("                if (!var0.copied ||", "                if (var0.binding.quote.passiveBefore == null || PASSIVE_ROOT.get() != null || !var0.sequence.extraAuthorityRetained() || !var0.copied ||")
passive_entering = passive_entering.replace("                var0.entered = true;", "                var0.entered = true;\n                PASSIVE_ROOT.set(var0);")
returned = native[native.index("    public static void returned("):native.index("    private static boolean active(")]
passive_returned = returned.replace("public static void returned(", "static void passiveReturned(").replace('leafHook("ftboss$ownerNativePut")', "passiveCaller()")
passive_returned = passive_returned.replace("                if (!var0.entered ||", "                if (PASSIVE_ROOT.get() != var0 || var0.passiveNotices != 1 || !var0.entered ||")
passive_returned = passive_returned.replace("var0.binding.quote.leaf, false", "var0.passiveLeaf, false")
passive_returned = passive_returned.replace("            } catch (Throwable var7) {\n                rejectAdmission(var0, var7, false);\n            }", "            } catch (Throwable var7) {\n                rejectAdmission(var0, var7, false);\n            } finally {\n                if (PASSIVE_ROOT.get() == var0) PASSIVE_ROOT.remove();\n            }")
replace("OwnerNativeSemanticAuthority.java", "    private static boolean active(", passive_entering + passive_returned + '''    static void passiveFailed(Admission admission, Throwable failure) {
        if (passiveCaller() && active(admission) && admission.binding.quote.passiveBefore != null) {
            if (PASSIVE_ROOT.get() == admission) PASSIVE_ROOT.remove();
            rejectAdmission(admission, failure, true);
        }
    }

    static void passiveClose(Admission admission) {
        if (passiveCaller() && active(admission) && admission.binding.quote.passiveBefore != null) {
            if (PASSIVE_ROOT.get() == admission) PASSIVE_ROOT.remove();
            admission.closed = true;
            try { admission.sequence.close(admission.step); }
            catch (Throwable failure) { rejectSaved(admission.binding.before, admission.sequence, admission.step, failure, false); }
        }
    }

    private static boolean active(''')

replace("OwnerNativeSemanticAuthority.java", "|| !admission.copied || admission.passiveNotices != 0 || receiver != admission.binding.quote.receiver)", "|| !admission.copied || admission.passiveNotices != 0 || !admission.sequence.extraAuthorityRetained() || receiver != admission.binding.quote.receiver)")

boss = source("FoodWeaponBossPassive.java")
start = boss.index("    public static FoodWeaponBossPassive.HitCommit commitPassiveHit(\n")
end = boss.index("    static OwnerNativeSemanticAuthority.PutBinding quoteOwnerPublication(", start)
original = boss[start:end]
modified = original.replace("        Throwable var6 = null;", "        Throwable var6 = null;\n        OwnerNativeSemanticAuthority.Admission passive = null;")
modified = modified.replace('            CompoundTag var9 = var8.getCompound("FTBossIntrinsicPassiveTimers").copy();', '''            CompoundTag originalTimers = var8.getCompound("FTBossIntrinsicPassiveTimers");
            passive = var3 == null ? null : var3.passiveCopy(var8, originalTimers);
            CompoundTag var9 = originalTimers.copy();
            OwnerNativeSemanticAuthority.passiveCopied(passive, originalTimers, var9);''')
modified = modified.replace("            var9.putLong(var7.profile, var7.context.quotedAt());", "            var9.putLong(var7.profile, var7.context.quotedAt());\n            OwnerNativeSemanticAuthority.passivePrepared(passive, var9, var7.profile, var7.context.quotedAt());")
modified = modified.replace('            var11.put("FTBossIntrinsicPassiveTimers", var9);', '''            OwnerNativeSemanticAuthority.passiveEntering(passive, var11, "FTBossIntrinsicPassiveTimers", var9);
            Tag previousRoot = var11.put("FTBossIntrinsicPassiveTimers", var9);
            OwnerNativeSemanticAuthority.passiveReturned(passive, var11, "FTBossIntrinsicPassiveTimers", var9, previousRoot);
            OwnerNativeSemanticAuthority.passiveClose(passive);''')
modified = modified.replace("        } catch (Throwable var20) {", "        } catch (Throwable var20) {\n            OwnerNativeSemanticAuthority.passiveFailed(passive, var20);")
modified = modified.replace("        } finally {\n            if (var4)", "        } finally {\n            OwnerNativeSemanticAuthority.passiveClose(passive);\n            if (var4)")
assert modified.count("passiveCopied(passive") == modified.count("passivePrepared(passive") == modified.count("Tag previousRoot =") == 1
assert modified.count("passiveClose(passive)") == 2
replace("FoodWeaponBossPassive.java", original, modified)

patches = []
for name, (target, before, after) in changes.items():
    if before is None:
        patches.append("*** Add File: " + target.as_posix() + "\n" + "".join("+" + line + "\n" for line in after.splitlines()))
    else:
        difference = "".join(list(difflib.unified_diff(before.splitlines(True), after.splitlines(True)))[2:])
        difference = re.sub(r"^@@.*@@.*\n", "@@\n", difference, flags=re.M)
        patches.append("*** Update File: " + target.as_posix() + "\n" + difference)
patch = "*** Begin Patch\n" + "".join(patches) + "*** End Patch\n"
command = ["C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe", "--codex-run-as-apply-patch"]
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
helpers = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), helpers)
for name, (target, before, after) in changes.items():
    if before is None:
        helpers["add"](target.relative_to(root).as_posix(), after)
        continue
    while target.read_text(encoding="utf-8") != after:
        current = target.read_text(encoding="utf-8").splitlines()
        desired = after.splitlines()
        edits = [entry for entry in difflib.SequenceMatcher(None, current, desired, autojunk=False).get_opcodes() if entry[0] != "equal"]
        kind, start, end, replacement_start, replacement_end = edits[0]
        added = desired[replacement_start:replacement_end]
        if kind == "insert":
            length = 0
            selected = []
            for line in added:
                if selected and length + len(line) > 4500:
                    break
                selected.append(line)
                length += len(line) + 1
            added = selected
        body = "*** Update File: " + target.as_posix() + "\n@@\n"
        body += "".join(" " + line + "\n" for line in current[max(0, start-4):start])
        body += "".join("-" + line + "\n" for line in current[start:end])
        body += "".join("+" + line + "\n" for line in added)
        body += "".join(" " + line + "\n" for line in current[end:end+4])
        result = subprocess.run([*command, "*** Begin Patch\n" + body + "*** End Patch\n"], cwd=root, capture_output=True, encoding="utf-8")
        assert result.returncode == 0, result.stderr
rows = [{"source": name, "target": str(target), "beforeSha256": hashlib.sha256((before if before is not None else sources[name].read_text(encoding="utf-8")).encode()).hexdigest(),
         "afterSha256": hashlib.sha256(target.read_bytes()).hexdigest()} for name, (target, before, after) in changes.items()]
with (work / "reports/native-passive-root-source-preparation-d71b9448.json").open("x", encoding="utf-8") as stream:
    json.dump({"scope": "real original PASSIVE_HIT persistent-root publication only", "parentCandidate": candidate["attempt"],
               "changedSources": rows, "newProductClasses": 0, "fullNineteenSignedOrRestartAccepted": False,
               "toolSha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(), "installable": False, "deployed": False}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"stagedBehaviorSources": len(rows), "productClassesAdded": 0, "scope": "original_passive_root_publication", "preparationOnly": True}))
