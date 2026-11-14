package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

final class OwnerMaterialProcAuthority {
    private static final ThreadLocal<Leaf> CURRENT = new ThreadLocal<>();
    private static final List<Material> ORDER = List.of(Material.LAPIS, Material.REDSTONE, Material.AMETHYST,
        Material.WITHERITE, Material.IGNITIUM, Material.CURSIUM, Material.ANCIENT_METAL, Material.BOSS_FROST,
        Material.BOSS_BUTCHER, Material.BOSS_TEA, Material.BOSS_WINE, Material.BOSS_SHEPHERD, Material.BOSS_TIDE);

    private OwnerMaterialProcAuthority() {}

    private static boolean caller(Class<?> type, String method) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames
            .filter(frame -> frame.getDeclaringClass().getNestHost() != OwnerMaterialProcAuthority.class)
            .findFirst().map(frame -> frame.getDeclaringClass() == type && frame.getMethodName().equals(method)).orElse(false));
    }

    private static long tick(ServerPlayer player) {
        return player.serverLevel().getServer().overworld().getGameTime();
    }

    static final class Program {
        final ServerPlayer player;
        final BeforeMitigation event;
        final DamageSource source;
        final List<Material> materials;
        final Map<Material, Integer> food;
        final long tick;
        final Thread thread;

        private Program(ServerPlayer player, BeforeMitigation event, List<Material> materials) {
            this.player = player;
            this.event = event;
            this.source = event.getSource();
            this.materials = List.copyOf(materials);
            this.tick = tick(player);
            this.thread = Thread.currentThread();
            EnumMap<Material, Integer> food = new EnumMap<>(Material.class);
            for (Material material : ORDER) {
                if (materials.contains(material)) food.put(material, WeaponMaterialPowers.quoteClosedMaterialFood(player, this.source, material));
            }
            this.food = Map.copyOf(food);
        }
    }

    static Program capture(ServerPlayer player, BeforeMitigation event, List<Material> materials) {
        boolean authentic = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L)
            .findFirst().map(frame -> frame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                && frame.getDeclaringClass().getSimpleName().equals("MaterialPowersLeaf") && frame.getMethodName().equals("<init>")).orElse(false));
        if (!authentic || player == null || event == null || materials == null || !player.serverLevel().getServer().isSameThread())
            throw new SecurityException("Actual closed material leaf construction required");
        return new Program(player, event, materials);
    }

    static final class Projection {
        final Map<String, Value> writes;
        final Map<String, Value> rangedLongs = Map.of();
        private Projection(Map<String, Value> writes) { this.writes = Map.copyOf(writes); }
    }

    static final class Group {
        final OwnerTimerSemanticAuthority.Book book;
        final Object leaf;
        final Program program;
        final String profile = "material-proc";
        final Set<String> timers = new HashSet<>();
        final Set<String> ranged = Set.of();
        final boolean acquired = false;
        final boolean rooted = false;
        Projection projection = new Projection(Map.of());
        boolean done;
        boolean entered;
        int lastOrdinal = -1;
        private Group(OwnerTimerSemanticAuthority.Book book, Object leaf, Program program) {
            this.book = book;
            this.leaf = leaf;
            this.program = program;
        }
    }

    static Group reserve(OwnerTimerSemanticAuthority.Book book, Object leaf, Program program, Object player, Object source) {
        if (!caller(OwnerTimerSemanticAuthority.Book.class, "quoteMaterial") || book == null || program == null
            || program.player != player || program.source != source || program.thread != Thread.currentThread()
            || tick(program.player) != program.tick || !FoodWeaponHitPlan.materialPowersLeaf(leaf))
            throw new SecurityException("Frozen original material programme required");
        return new Group(book, leaf, program);
    }

    static final class Leaf {
        final FoodWeaponOriginalSequence.Session session;
        final FoodWeaponHitPlan.FinalEffectsCommit commit;
        final FoodWeaponFinalDamage.ChildHurtBridge bridge;
        final Group group;
        final Thread thread = Thread.currentThread();
        final Leaf previous;
        Proc proc;
        boolean closed;
        private Leaf(FoodWeaponOriginalSequence.Session session, FoodWeaponHitPlan.FinalEffectsCommit commit,
            FoodWeaponFinalDamage.ChildHurtBridge bridge, Group group, Leaf previous) {
            this.session = session;
            this.commit = commit;
            this.bridge = bridge;
            this.group = group;
            this.previous = previous;
        }
    }

    private static boolean live(Leaf leaf) {
        return leaf != null && CURRENT.get() == leaf && leaf.thread == Thread.currentThread() && !leaf.closed
            && leaf.session.extraAuthorityRetained() && leaf.session.timerSemantics == leaf.group.book
            && leaf.group.book.prepared.timerSemantics == leaf.group.book
            && leaf.group.book.prepared.lineage == leaf.session.lineage
            && leaf.group.book.materialGroups.get(leaf.group.leaf) == leaf.group
            && leaf.session.original.atLeaf(leaf.group.leaf)
            && FoodWeaponHitPlan.originalSequence(leaf.commit) == leaf.session
            && FoodWeaponHitPlan.leafLive(leaf.commit, leaf.bridge) && !leaf.group.done;
    }

    static Leaf enter(FoodWeaponOriginalSequence.Session session, FoodWeaponHitPlan.FinalEffectsCommit commit,
        FoodWeaponFinalDamage.ChildHurtBridge bridge, Object originalLeaf) {
        if (!caller(FoodWeaponSourceLedger.class, "ownerMaterialLeafEntered") || session == null || session.timerSemantics == null)
            return null;
        Group group = session.timerSemantics.materialGroups.get(originalLeaf);
        if (group == null) return null;
        Leaf leaf = new Leaf(session, commit, bridge, group, CURRENT.get());
        CURRENT.set(leaf);
        if (group.entered || !live(leaf)) {
            session.observationFailed(null, 0, new IllegalStateException("Original material leaf identity/liveness"));
        } else group.entered = true;
        return leaf;
    }

    static void leave(Leaf leaf, Throwable failure) {
        if (leaf == null) return;
        try {
            if (!StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames.filter(frame -> frame.getDeclaringClass().getNestHost() != OwnerMaterialProcAuthority.class)
                    .findFirst().map(frame -> frame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                        && frame.getDeclaringClass().getSimpleName().equals("MaterialPowersLeaf") && frame.getMethodName().equals("apply")).orElse(false)))
                throw new SecurityException("Actual original material leaf return required");
            if (failure != null || !live(leaf) || leaf.proc != null || !leaf.group.entered
                || !leaf.group.timers.equals(leaf.group.projection.writes.keySet()))
                leaf.session.observationFailed(null, 3, failure);
            else leaf.group.done = true;
        } catch (Throwable observationFailure) {
            leaf.session.observationFailed(null, 3, observationFailure);
        } finally {
            leaf.closed = true;
            if (CURRENT.get() == leaf && leaf.thread == Thread.currentThread()) {
                if (leaf.previous == null) CURRENT.remove(); else CURRENT.set(leaf.previous);
            }
        }
    }

    static final class Proc {
        final Leaf leaf;
        final MaterialProcExpectedPlan.Plan plan;
        final Value initialRoot;
        final Value initialTimer;
        CompoundTag playerRoot;
        CompoundTag root;
        CompoundTag timer;
        int index;
        int notices;
        boolean acquired;
        boolean finished;
        boolean writing;
        FoodWeaponOriginalSequence.Step step;
        Receipt receipt;
        private Proc(Leaf leaf, MaterialProcExpectedPlan.Plan plan) {
            this.leaf = leaf;
            this.plan = plan;
            Value root = FoodWeaponExpectedTrace.field(plan.playerBefore(), MaterialProcExpectedPlan.ROOT);
            this.initialRoot = root != null && root.kind == 10 ? root : FoodWeaponExpectedTrace.emptyCompound();
            Value timer = FoodWeaponExpectedTrace.field(this.initialRoot, plan.material().id());
            this.initialTimer = timer != null && timer.kind == 10 ? timer : FoodWeaponExpectedTrace.emptyCompound();
        }
    }

    private static boolean originalProcStack() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> {
            List<StackWalker.StackFrame> path = frames.filter(frame -> frame.getDeclaringClass().getNestHost() != OwnerMaterialProcAuthority.class).limit(3L).toList();
            return path.size() == 3 && path.get(0).getDeclaringClass() == WeaponMaterialPowers.class
                && path.get(0).getMethodName().equals("closedProc") && path.get(1).getDeclaringClass() == WeaponMaterialPowers.class
                && path.get(1).getMethodName().equals("applyClosedHitSteps")
                && path.get(2).getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                && path.get(2).getDeclaringClass().getSimpleName().equals("MaterialPowersLeaf") && path.get(2).getMethodName().equals("apply");
        });
    }

    static Proc begin(FoodWeaponHitPlan.FinalEffectsCommit commit, FoodWeaponFinalDamage.ChildHurtBridge bridge,
        ServerPlayer player, Material material, int food) {
        Leaf leaf = CURRENT.get();
        if (leaf == null) return null;
        try {
            Program program = leaf.group.program;
            int ordinal = ORDER.indexOf(material);
            if (!originalProcStack() || !live(leaf) || leaf.proc != null || leaf.commit != commit || leaf.bridge != bridge
                || program.player != player || program.thread != Thread.currentThread() || ordinal < 0 || ordinal <= leaf.group.lastOrdinal
                || !program.materials.contains(material) || !Integer.valueOf(food).equals(program.food.get(material))
                || tick(player) != program.tick || WeaponMaterialPowers.quoteClosedMaterialFood(player, program.source, material) != food)
                throw new IllegalStateException("Original closed material choice/order/food/tick mismatch");
            State cursor = leaf.session.materialProcCursor();
            MaterialProcExpectedPlan.Plan plan = MaterialProcExpectedPlan.quote(cursor.player, material, program.tick, program.food.get(material));
            Proc proc = new Proc(leaf, plan);
            leaf.proc = proc;
            leaf.group.lastOrdinal = ordinal;
            Map<String, Value> writes = new LinkedHashMap<>(leaf.group.projection.writes);
            for (int index = 0; index < plan.writes().size(); index++) {
                if (writes.put(key(proc, index), plan.writes().get(index).argument()) != null)
                    throw new IllegalStateException("Repeated material write quote");
            }
            leaf.group.projection = new Projection(writes);
            return proc;
        } catch (Throwable failure) {
            leaf.session.observationFailed(null, 0, failure);
            return null;
        }
    }

    private static String key(Proc proc, int index) {
        return proc.plan.material().id() + "/" + index + "/" + proc.plan.writes().get(index).key();
    }

    static void acquired(ServerPlayer player, Material material, int food, long tick, CompoundTag root, CompoundTag timer) {
        Leaf leaf = CURRENT.get();
        if (leaf == null || leaf.proc == null) return;
        Proc proc = leaf.proc;
        try {
            if (!caller(WeaponMaterialPowers.class, "proc") || !live(leaf) || proc.acquired || proc.plan.material() != material
                || proc.plan.foodCount() != food || proc.plan.tick() != tick || leaf.group.program.player != player)
                throw new IllegalStateException("Original material buffer acquisition mismatch");
            proc.playerRoot = NativeExpectedRawFields.materialPersistent(player);
            proc.root = root;
            proc.timer = timer;
            if (proc.playerRoot == null || root == null || timer == null || proc.playerRoot == root || proc.playerRoot == timer || root == timer
                || !FoodWeaponExpectedTrace.same(proc.plan.playerBefore(), NativeExpectedRawFields.observe(proc.playerRoot))
                || !FoodWeaponExpectedTrace.same(proc.initialRoot, NativeExpectedRawFields.observe(root))
                || !FoodWeaponExpectedTrace.same(proc.initialTimer, NativeExpectedRawFields.observe(timer)))
                throw new IllegalStateException("Original material immutable buffer before images");
            Tag storedRoot = NativeExpectedRawFields.materialChild(proc.playerRoot, MaterialProcExpectedPlan.ROOT);
            Tag storedTimer = NativeExpectedRawFields.materialChild(root, material.id());
            if (storedRoot instanceof CompoundTag ? storedRoot != root : storedRoot == root)
                throw new IllegalStateException("Material root attachment identity");
            if (storedTimer instanceof CompoundTag ? storedTimer != timer : storedTimer == timer)
                throw new IllegalStateException("Material timer attachment identity");
            proc.acquired = true;
        } catch (Throwable failure) { leaf.session.observationFailed(null, 0, failure); }
    }

    static FoodWeaponExpectedTrace.Operation resolve(OwnerTimerSemanticAuthority.Book book, FoodWeaponOriginalSequence.Session session,
        Object leafIdentity, String key, String profile, State cursor) {
        Leaf leaf = CURRENT.get();
        Proc proc = leaf == null ? null : leaf.proc;
        if (!caller(OwnerTimerSemanticAuthority.Book.class, "resolve") || !live(leaf) || proc == null || !proc.writing
            || proc.step != null || leaf.session != session || leaf.group.book != book || leaf.group.leaf != leafIdentity
            || !"material-proc".equals(profile) || !key(proc, proc.index).equals(key)
            || !FoodWeaponExpectedTrace.same(cursor.player, proc.plan.writes().get(proc.index).playerBefore())) return null;
        return new FoodWeaponExpectedTrace.Builder(cursor).materialProcWrite(key, proc.plan.writes().get(proc.index));
    }

    private static Proc writeBegins(CompoundTag receiver, String key, Value argument) {
        Leaf leaf = CURRENT.get();
        Proc proc = leaf == null ? null : leaf.proc;
        if (proc == null) return null;
        try {
            if (!caller(WeaponMaterialPowers.class, "proc") || !live(leaf) || !proc.acquired || proc.finished || proc.writing
                || proc.index >= proc.plan.writes().size()) throw new IllegalStateException("Unexpected material write");
            MaterialProcExpectedPlan.Write expected = proc.plan.writes().get(proc.index);
            CompoundTag expectedReceiver = proc.index < 2 ? proc.timer : proc.index == 2 ? proc.root : proc.playerRoot;
            if (receiver != expectedReceiver || !expected.key().equals(key) || !FoodWeaponExpectedTrace.same(expected.argument(), argument)
                || !buffersMatch(proc, proc.index)) throw new IllegalStateException("Material write receiver/key/value/before mismatch");
            proc.writing = true;
            proc.notices = 0;
            proc.step = leaf.session.originalBegins(leaf.group.leaf, Kind.MATERIAL_PROC_WRITE, key(proc, proc.index), "material-proc");
            if (proc.step == null) throw new IllegalStateException("Material Session entry rejected");
            return proc;
        } catch (Throwable failure) { leaf.session.observationFailed(proc.step, 0, failure); return null; }
    }

    private static boolean buffersMatch(Proc proc, int completed) {
        Value timer = completed == 0 ? proc.initialTimer : completed == 1
            ? FoodWeaponExpectedTrace.put(proc.initialTimer, "Stamp", proc.plan.writes().get(0).argument()) : proc.plan.writes().get(2).argument();
        boolean childAttached = FoodWeaponExpectedTrace.field(proc.initialRoot, proc.plan.material().id()) != null
            && FoodWeaponExpectedTrace.field(proc.initialRoot, proc.plan.material().id()).kind == 10;
        Value root = completed >= 3 || childAttached ? FoodWeaponExpectedTrace.put(proc.initialRoot, proc.plan.material().id(), timer) : proc.initialRoot;
        Value player = completed == 0 ? proc.plan.playerBefore() : proc.plan.writes().get(completed - 1).playerAfter();
        return proc.playerRoot == NativeExpectedRawFields.materialPersistent(proc.leaf.group.program.player)
            && FoodWeaponExpectedTrace.same(player, NativeExpectedRawFields.observe(proc.playerRoot))
            && FoodWeaponExpectedTrace.same(root, NativeExpectedRawFields.observe(proc.root))
            && FoodWeaponExpectedTrace.same(timer, NativeExpectedRawFields.observe(proc.timer));
    }

    private static boolean originalWritePath(List<StackWalker.StackFrame> frames, Proc proc) throws ReflectiveOperationException {
        String writer = proc.index == 0 ? "putStamp" : proc.index == 1 ? "putDelay" : "putCompound";
        int boundary = -1;
        for (int index = 3; index < frames.size(); index++) {
            if (frames.get(index).getDeclaringClass() == OwnerMaterialProcAuthority.class) {
                boundary = index;
                break;
            }
        }
        if (boundary < 3 || boundary + 4 >= frames.size() || !frames.get(boundary).getMethodName().equals(writer)) return false;
        if (proc.index != 0 && boundary != 3) return false;
        String originalName = frames.get(2).getMethodName();
        for (int index = 3; index < boundary; index++) {
            StackWalker.StackFrame frame = frames.get(index);
            if (frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("nativeFxCooldown")
                && frame.getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;JLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V")) continue;
            if (frame.getDeclaringClass() != CompoundTag.class) return false;
            if (frame.getMethodName().equals("putLong") && frame.getDescriptor().equals("(Ljava/lang/String;J)V")) continue;
            if (frame.getMethodName().matches(java.util.regex.Pattern.quote("mixinextras$bridge$" + originalName + "$") + "[0-9]+")
                && frame.getDescriptor().equals("([Ljava/lang/Object;)Ljava/lang/Void;")) continue;
            if (!frame.getMethodName().endsWith("$ftboss$nativeFxCooldown")
                || !frame.getDescriptor().equals("(Ljava/lang/String;JLcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V")) return false;
            MixinMerged merged = CompoundTag.class.getDeclaredMethod(frame.getMethodName(), frame.getMethodType().parameterArray()).getAnnotation(MixinMerged.class);
            if (merged == null || !merged.mixin().equals("net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin")) return false;
        }
        StackWalker.StackFrame original = frames.get(boundary + 1);
        StackWalker.StackFrame closed = frames.get(boundary + 2);
        StackWalker.StackFrame effects = frames.get(boundary + 3);
        StackWalker.StackFrame leaf = frames.get(boundary + 4);
        return original.getDeclaringClass() == WeaponMaterialPowers.class && original.getMethodName().equals("proc")
            && original.getDescriptor().equals("(Lnet/minecraft/server/level/ServerPlayer;Lnet/farmingtales/ftboss/util/FoodWeaponBuildRules$Material;I)Z")
            && closed.getDeclaringClass() == WeaponMaterialPowers.class && closed.getMethodName().equals("closedProc")
            && effects.getDeclaringClass() == WeaponMaterialPowers.class && effects.getMethodName().equals("applyClosedHitSteps")
            && leaf.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
            && leaf.getDeclaringClass().getSimpleName().equals("MaterialPowersLeaf") && leaf.getMethodName().equals("apply");
    }

    static boolean plannedMutation(Object receiver, int reason) {
        Leaf leaf = CURRENT.get();
        Proc proc = leaf == null ? null : leaf.proc;
        if (!caller(OwnerFullRepairGuard.class, "changed") || proc == null || !live(leaf) || !proc.writing || proc.step == null
            || proc.notices != 0 || reason != (proc.index < 2 ? 20 : 21)
            || receiver != (proc.index < 2 ? proc.timer : proc.index == 2 ? proc.root : proc.playerRoot)) return false;
        try {
            List<StackWalker.StackFrame> frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> stream.skip(1L).limit(40L).toList());
            if (frames.size() < 7 || frames.get(0).getDeclaringClass() != OwnerFullRepairGuard.class
                || !frames.get(0).getMethodName().equals("changed") || !frames.get(0).getDescriptor().equals("(Ljava/lang/Object;I)V")
                || frames.get(1).getDeclaringClass() != CompoundTag.class || frames.get(2).getDeclaringClass() != CompoundTag.class) return false;
            StackWalker.StackFrame callback = frames.get(1);
            String callbackName = proc.index < 2 ? "ftboss$fullRepairVoidMutation" : "ftboss$fullRepairReturnedMutation";
            if (!callback.getMethodName().endsWith("$" + callbackName)) return false;
            MixinMerged merged = CompoundTag.class.getDeclaredMethod(callback.getMethodName(), callback.getMethodType().parameterArray()).getAnnotation(MixinMerged.class);
            if (merged == null || !merged.mixin().equals("net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin")) return false;
            StackWalker.StackFrame original = frames.get(2);
            boolean originalMethod = proc.index == 0 ? NativeFreshFactoryTrust298.rangedTimerMutationFrame(original)
                : proc.index == 1 ? original.getMethodName().equals("putInt") && original.getDescriptor().equals("(Ljava/lang/String;I)V")
                : original.getMethodName().equals("put") && original.getDescriptor().equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;");
            if (!originalMethod || !originalWritePath(frames, proc) || !buffersMatch(proc, proc.index)) return false;
            proc.notices++;
            return true;
        } catch (Throwable failure) { leaf.session.observationFailed(proc.step, 0, failure); return false; }
    }

    static final class Receipt {
        final Proc proc;
        final FoodWeaponOriginalSequence.Step step;
        boolean consumed;
        private Receipt(Proc proc) { this.proc = proc; this.step = proc.step; }
        boolean accepts(FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step) {
            if (!caller(FoodWeaponOriginalSequence.Session.class, "materialProcReturned") || consumed || !live(proc.leaf)
                || proc.leaf.proc != proc || proc.receipt != this || proc.step != this.step || this.step != step
                || session != proc.leaf.session || !proc.writing || proc.notices != 1 || !buffersMatch(proc, proc.index + 1)) return false;
            consumed = true;
            return true;
        }
    }

    private static void writeReturned(Proc proc, Throwable failure) {
        if (proc == null) return;
        try {
            if (failure != null || !live(proc.leaf) || !proc.writing || proc.notices != 1 || !buffersMatch(proc, proc.index + 1))
                throw new IllegalStateException("Incomplete original material write", failure);
            proc.receipt = new Receipt(proc);
            proc.leaf.session.materialProcReturned(proc.step, proc.receipt);
            proc.leaf.session.close(proc.step);
            if (!proc.leaf.session.extraAuthorityRetained()) throw new IllegalStateException("Material expected image advance rejected");
            proc.index++;
            proc.step = null;
            proc.receipt = null;
            proc.writing = false;
        } catch (Throwable observationFailure) { proc.leaf.session.observationFailed(proc.step, 3, observationFailure); }
    }

    static void returned(OwnerTimerSemanticAuthority.Book book, FoodWeaponOriginalSequence.Step step, Object leafIdentity) {
        Leaf leaf = CURRENT.get();
        Proc proc = leaf == null ? null : leaf.proc;
        if (!caller(OwnerTimerSemanticAuthority.Book.class, "returned") || !live(leaf) || proc == null || proc.step != step
            || proc.receipt == null || !proc.receipt.consumed || leaf.group.book != book || leaf.group.leaf != leafIdentity
            || !key(proc, proc.index).equals(step.expected.key) || !leaf.group.timers.add(step.expected.key))
            throw new IllegalStateException("Exact material Session receipt required");
    }

    static void putStamp(CompoundTag receiver, String key, long value) {
        Proc proc = writeBegins(receiver, key, FoodWeaponExpectedTrace.number(4, value));
        try { receiver.putLong(key, value); }
        catch (Throwable failure) { writeReturned(proc, failure); throw failure; }
        writeReturned(proc, null);
    }

    static void putDelay(CompoundTag receiver, String key, int value) {
        Proc proc = writeBegins(receiver, key, FoodWeaponExpectedTrace.number(3, value));
        try { receiver.putInt(key, value); }
        catch (Throwable failure) { writeReturned(proc, failure); throw failure; }
        writeReturned(proc, null);
    }

    static Tag putCompound(CompoundTag receiver, String key, Tag value) {
        Leaf leaf = CURRENT.get();
        if (leaf == null || leaf.proc == null) return receiver.put(key, value);
        Value argument;
        try { argument = NativeExpectedRawFields.observe(value); }
        catch (Throwable observationFailure) {
            leaf.session.observationFailed(null, 0, observationFailure);
            return receiver.put(key, value);
        }
        Proc proc = writeBegins(receiver, key, argument);
        Tag returned;
        try { returned = receiver.put(key, value); }
        catch (Throwable failure) { writeReturned(proc, failure); throw failure; }
        writeReturned(proc, null);
        return returned;
    }

    static void finished(Proc proc, boolean ready, Throwable failure) {
        if (proc == null) return;
        try {
            if (!caller(WeaponMaterialPowers.class, "closedProc") || !live(proc.leaf) || proc.leaf.proc != proc || proc.finished
                || failure != null || !proc.acquired || proc.writing || ready != proc.plan.fired()
                || proc.index != proc.plan.writes().size() || !ready && !buffersMatch(proc, 0) || !FoodWeaponExpectedTrace.same(proc.plan.playerAfter(), NativeExpectedRawFields.observe(proc.playerRoot)))
                throw new IllegalStateException("Original material return/ready/write coverage mismatch", failure);
            proc.finished = true;
        } catch (Throwable observationFailure) { proc.leaf.session.observationFailed(proc.step, 3, observationFailure); }
        finally { if (proc.leaf.proc == proc) proc.leaf.proc = null; }
    }
}
