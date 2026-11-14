/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueTaskPlan;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.item.OwnerStatueTaskWritePlan;
import net.farmingtales.ftboss.item.OwnerStatueTaskWrites;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.OwnerStatueWearBoundary;
import net.farmingtales.ftboss.item.OwnerStatueWearWrites;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class OwnerStatueTaskOrigin {
    private static final Map<ItemStack, Session> SESSIONS = new WeakHashMap<ItemStack, Session>();
    private static final Object LOCK = new Object();
    private static final Map<Object, ProgressRead> READS = new IdentityHashMap<Object, ProgressRead>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static ProgressRead progressRead(ItemStack itemStack, CompoundTag compoundTag) {
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return null;
        }
        try {
            CompoundTag compoundTag2;
            if (!OwnerStatueTaskOrigin.caller(OwnerStatueTaskWrites.class, "progress") || !OwnerStatueTaskOrigin.physical(session) || session.phase != Phase.IDLE && session.phase != Phase.WRITING) {
                throw new IllegalStateException("Detached task progress read");
            }
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class && stackFrame.getDeclaringClass() != OwnerStatueTaskWrites.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponRepair.class && Set.of("activeQuest", "view", "submit").contains(stackFrame.getMethodName())).orElse(false));
            CompoundTag compoundTag3 = compoundTag2 = session.active == null ? session.expected : session.active.plan.cursor;
            if (!bl || !compoundTag2.equals((Object)itemStack.m_41739_(new CompoundTag())) || itemStack.m_41783_().m_128469_("FTBossWeaponLifecycle").m_128469_("RepairQuest") != compoundTag) {
                throw new IllegalStateException("Progress read differs from private source");
            }
            Tag tag = compoundTag.m_128423_("Progress");
            if (!(tag instanceof IntArrayTag)) {
                throw new IllegalStateException("Progress storage type changed");
            }
            ProgressRead progressRead = new ProgressRead(session, compoundTag, tag, "Progress", compoundTag2.m_6426_());
            Object object = LOCK;
            synchronized (object) {
                if (READS.containsKey(compoundTag) || READS.containsKey(tag)) {
                    throw new IllegalStateException("Reentrant task progress read");
                }
                READS.put(compoundTag, progressRead);
                READS.put(tag, progressRead);
            }
            return progressRead;
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static ProgressRead identityRead(ItemStack itemStack, CompoundTag compoundTag) {
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return null;
        }
        try {
            CompoundTag compoundTag2;
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class && stackFrame.getDeclaringClass() != OwnerStatueTaskWrites.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && Set.of("weaponId", "ensureTracked").contains(stackFrame.getMethodName())).orElse(false));
            CompoundTag compoundTag3 = compoundTag2 = session.active == null ? session.expected : session.active.plan.cursor;
            if (!(bl && OwnerStatueTaskOrigin.physical(session) && compoundTag2.equals((Object)itemStack.m_41739_(new CompoundTag())) && itemStack.m_41783_().m_128469_("FTBossWeaponLifecycle") == compoundTag)) {
                throw new IllegalStateException("Detached bounded weapon identity read");
            }
            Tag tag = compoundTag.m_128423_("Weapon");
            if (!(tag instanceof IntArrayTag)) {
                throw new IllegalStateException("Weapon identity storage changed");
            }
            ProgressRead progressRead = new ProgressRead(session, compoundTag, tag, "Weapon", compoundTag2.m_6426_());
            Object object = LOCK;
            synchronized (object) {
                if (READS.containsKey(compoundTag) || READS.containsKey(tag)) {
                    throw new IllegalStateException("Reentrant identity read");
                }
                READS.put(compoundTag, progressRead);
                READS.put(tag, progressRead);
            }
            return progressRead;
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void identityReturned(ProgressRead progressRead, Throwable throwable) {
        if (progressRead == null) {
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            if (READS.get(progressRead.receiver) == progressRead) {
                READS.remove(progressRead.receiver);
            }
            if (READS.get(progressRead.storage) == progressRead) {
                READS.remove(progressRead.storage);
            }
        }
        if (throwable != null || !OwnerStatueTaskOrigin.physical(progressRead.session) || !progressRead.before.equals((Object)((ItemStack)progressRead.session.actual.get()).m_41739_(new CompoundTag()))) {
            OwnerStatueTaskOrigin.burn(progressRead.session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean boundedProgressAlias(Object object, Object object2) {
        ProgressRead progressRead;
        int[] nArray;
        block9: {
            block8: {
                if (!(object2 instanceof int[])) break block8;
                nArray = (int[])object2;
                if (OwnerStatueTaskOrigin.caller(OwnerFullRepairGuard.class, "alias")) break block9;
            }
            return false;
        }
        Object object3 = LOCK;
        synchronized (object3) {
            progressRead = READS.get(object);
        }
        if (progressRead == null) {
            return false;
        }
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == CompoundTag.class && (progressRead.key.equals("Progress") ? stackFrame.getDescriptor().equals("(Ljava/lang/String;)[I") : stackFrame.getDescriptor().equals("(Ljava/lang/String;)Ljava/util/UUID;") || stackFrame.getDescriptor().equals("(Ljava/lang/String;)Z"))));
        if (!bl || progressRead.array != null && progressRead.array != nArray || !OwnerStatueTaskOrigin.physical(progressRead.session) || !Arrays.equals(nArray, (progressRead.key.equals("Progress") ? progressRead.before.m_128469_("tag").m_128469_("FTBossWeaponLifecycle").m_128469_("RepairQuest") : progressRead.before.m_128469_("tag").m_128469_("FTBossWeaponLifecycle")).m_128465_(progressRead.key))) {
            OwnerStatueTaskOrigin.burn(progressRead.session);
            return false;
        }
        progressRead.array = nArray;
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void progressReturned(ProgressRead progressRead, int[] nArray, Throwable throwable) {
        if (progressRead == null) {
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            if (READS.get(progressRead.receiver) == progressRead) {
                READS.remove(progressRead.receiver);
            }
            if (READS.get(progressRead.storage) == progressRead) {
                READS.remove(progressRead.storage);
            }
        }
        if (throwable != null || progressRead.array != nArray || !OwnerStatueTaskOrigin.physical(progressRead.session) || !progressRead.before.equals((Object)((ItemStack)progressRead.session.actual.get()).m_41739_(new CompoundTag()))) {
            OwnerStatueTaskOrigin.burn(progressRead.session);
        } else {
            progressRead.session.progressArray = nArray;
        }
    }

    static Write beforeArrayWrite(ItemStack itemStack, CompoundTag compoundTag, int[] nArray, int n, int n2) {
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return null;
        }
        try {
            if (!OwnerStatueTaskOrigin.caller(OwnerStatueTaskWrites.class, "advanceProgress") || session.active == null || nArray != session.progressArray || n != session.active.plan.index || n2 != session.active.paid) {
                throw new IllegalStateException("Unquoted original Progress array mutation");
            }
            int[] nArray2 = (int[])nArray.clone();
            nArray2[n] = Math.addExact(nArray2[n], n2);
            return OwnerStatueTaskOrigin.beforeWrite(itemStack, compoundTag, "ProgressArray", (Tag)new IntArrayTag(nArray2), false);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
            return null;
        }
    }

    private OwnerStatueTaskOrigin() {
    }

    private static boolean caller(Class<?> clazz, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && stackFrame.getMethodName().equals(string)).orElse(false));
    }

    private static boolean physical(Session session) {
        ServerPlayer serverPlayer = (ServerPlayer)session.player.get();
        ItemStack itemStack = (ItemStack)session.actual.get();
        return !session.rejected && session.thread == Thread.currentThread() && serverPlayer != null && itemStack != null && session.server != null && session.server.m_18695_() && serverPlayer.m_20194_() == session.server && session.owner.equals(serverPlayer.m_20148_()) && serverPlayer.m_9236_() == session.level && serverPlayer.m_150109_() == session.inventory.get() && serverPlayer.m_150109_().m_8020_(session.slot) == itemStack && itemStack.m_41613_() == 1 && (session.slot == 40 ? serverPlayer.m_21206_() == itemStack : serverPlayer.m_150109_().f_35977_ == session.slot && serverPlayer.m_21205_() == itemStack) && session.statueState.equals(serverPlayer.m_9236_().m_8055_(session.statue));
    }

    private static boolean uniqueInventoryCopy(Session session, ItemStack itemStack) {
        int n = 0;
        for (ServerPlayer serverPlayer : session.server.m_6846_().m_11314_()) {
            Inventory inventory = serverPlayer.m_150109_();
            for (int i = 0; i < inventory.m_6643_(); ++i) {
                ItemStack itemStack2 = inventory.m_8020_(i);
                CompoundTag compoundTag = itemStack2.m_41739_(new CompoundTag()).m_128469_("tag").m_128469_("FTBossWeaponLifecycle");
                if (!compoundTag.m_128403_("Weapon") || !session.weapon.equals(compoundTag.m_128342_("Weapon")) || itemStack2 == itemStack && serverPlayer == session.player.get() && ++n <= 1) continue;
                return false;
            }
        }
        return n == 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Session session(ItemStack itemStack) {
        Object object = LOCK;
        synchronized (object) {
            return SESSIONS.get(itemStack);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Session reserve(OwnerStatueWearAdmission.Accepted accepted, OwnerStatueTaskService.Prepared prepared, OwnerLineageRevocationStore ownerLineageRevocationStore, ServerPlayer serverPlayer, ItemStack itemStack, int n, BlockPos blockPos, int n2, CompoundTag compoundTag) throws IOException {
        if (!OwnerStatueTaskOrigin.caller(OwnerStatueTaskService.class, "prepareStart") || accepted == null || !accepted.transferToTaskOrigin(serverPlayer, itemStack, n)) {
            return null;
        }
        CompoundTag compoundTag2 = accepted.record();
        Session session = new Session(compoundTag2, compoundTag, ownerLineageRevocationStore, serverPlayer, itemStack, n, blockPos, n2);
        Object object = LOCK;
        synchronized (object) {
            if (SESSIONS.containsKey(itemStack)) {
                return null;
            }
            SESSIONS.put(itemStack, session);
        }
        try {
            if (!OwnerStatueTaskOrigin.physical(session) || OwnerFullRepairGuard.lease(itemStack) != null || OwnerFullRepairGuard.generation(itemStack) != null || !OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) || FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack)) {
                throw new IOException("Unsupported task-origin predecessor");
            }
            session.reservation = ownerLineageRevocationStore.reserveTaskOrigin(session);
            if (session.reservation == null || !OwnerStatueTaskOrigin.physical(session)) {
                throw new IOException("Task-origin negative reservation failed");
            }
            session.phase = Phase.IDLE;
            return session;
        }
        catch (IOException | Error | RuntimeException throwable) {
            OwnerStatueTaskOrigin.burn(session);
            throw throwable;
        }
    }

    static Session find(ServerPlayer serverPlayer, ItemStack itemStack, int n, CompoundTag compoundTag) {
        if (!OwnerStatueTaskOrigin.caller(OwnerStatueTaskService.class, "prepare")) {
            return null;
        }
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return null;
        }
        try {
            if (!(OwnerStatueTaskOrigin.physical(session) && session.phase == Phase.IDLE && session.player.get() == serverPlayer && session.slot == n && compoundTag.m_128403_("Task") && session.task.equals(compoundTag.m_128342_("Task")) && compoundTag.m_128454_("Sequence") == session.version && compoundTag.m_128403_("AcceptedWearCall") && session.call.equals(compoundTag.m_128342_("AcceptedWearCall")) && session.expected.equals((Object)compoundTag.m_128469_("Expected")) && session.rules.equals(compoundTag.m_128461_("Rules")))) {
                OwnerStatueTaskOrigin.burn(session);
                return null;
            }
            return session;
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
            return null;
        }
    }

    static void begin(Session session, OwnerStatueTaskService.Prepared prepared, StatueRepairMutation statueRepairMutation, FourWeaponTransactions.Operation operation, int n, CompoundTag compoundTag, UUID uUID) {
        if (session == null) {
            return;
        }
        try {
            if (!(OwnerStatueTaskOrigin.caller(OwnerStatueTaskService.Prepared.class, "beforeOriginal") && statueRepairMutation.taskOriginSource(prepared) && OwnerStatueTaskOrigin.physical(session) && OwnerStatueTaskOrigin.uniqueInventoryCopy(session, (ItemStack)session.actual.get()) && session.phase == Phase.IDLE && session.active == null && compoundTag.m_128454_("Sequence") == session.version + 1L && session.expected.equals((Object)compoundTag.m_128469_("Expected")) && session.expected.equals((Object)((ItemStack)session.actual.get()).m_41739_(new CompoundTag())) && session.rules.equals(OwnerStatueTaskPlan.rules((ItemStack)session.actual.get())))) {
                throw new IllegalStateException("Detached original task call");
            }
            session.active = new Call(session, prepared, statueRepairMutation, operation, n, compoundTag, uUID);
            session.phase = Phase.WRITING;
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
        }
    }

    static void paid(Session session, OwnerStatueTaskService.Prepared prepared, int n, int n2) {
        if (session == null) {
            return;
        }
        try {
            if (!OwnerStatueTaskOrigin.caller(OwnerStatueTaskService.Prepared.class, "afterShrink") || session.phase != Phase.WRITING || !OwnerStatueTaskOrigin.physical(session) || session.active.prepared != prepared || session.active.plan.operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR || n != session.active.plan.index || n2 <= 0) {
                throw new IllegalStateException("Detached task payment commit");
            }
            session.active.paid = Math.addExact(session.active.paid, n2);
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
        }
    }

    private static boolean writer(boolean bl) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class && stackFrame.getDeclaringClass() != OwnerStatueTaskWrites.class && stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).findFirst().map(stackFrame -> bl ? OwnerStatueWearBoundary.writer(1, stackFrame) : stackFrame.getDeclaringClass() == FoodWeaponRepair.class && Set.of("start", "submit", "claim", "advance").contains(stackFrame.getMethodName()) || stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && Set.of("ensureTracked", "restoreAfterQuest").contains(stackFrame.getMethodName())).orElse(false));
    }

    static Write beforeWrite(ItemStack itemStack, Object object, String string, Tag tag, boolean bl) {
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return null;
        }
        try {
            Call call = session.active;
            if (!(OwnerStatueTaskOrigin.writer(bl) && OwnerStatueTaskOrigin.physical(session) && session.phase == Phase.WRITING && call != null && call.writing == null && call.outer.taskOriginRunning(call.prepared) && call.plan.cursor.equals((Object)itemStack.m_41739_(new CompoundTag())))) {
                throw new IllegalStateException("Task writer is not live");
            }
            if (call.plan.operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR && call.plan.steps.isEmpty()) {
                call.plan.submit(call.paid);
            }
            OwnerStatueTaskWritePlan.Step step = call.plan.expected(string, tag);
            CompoundTag compoundTag = itemStack.m_41783_();
            Object object2 = step.path().isEmpty() ? new String[]{} : step.path().split("/");
            int n = ((String[])object2).length;
            for (int i = 0; i < n; ++i) {
                String string2 = object2[i];
                compoundTag = compoundTag.m_128469_(string2);
            }
            if (!bl && compoundTag != object || bl && (!step.path().isEmpty() || !string.equals("Damage"))) {
                throw new IllegalStateException("Task write receiver differs");
            }
            call.writing = object2 = new Write(session, step, bl ? itemStack.m_41783_() : object);
            return object2;
        }
        catch (Throwable throwable) {
            OwnerStatueTaskOrigin.burn(session);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void afterWrite(Write write, Throwable throwable) {
        if (write == null) {
            return;
        }
        Session session = write.session;
        try {
            if (write.closed || !OwnerStatueTaskOrigin.physical(session) || session.phase != Phase.WRITING || session.active.writing != write || throwable != null || !write.step.after().equals((Object)((ItemStack)session.actual.get()).m_41739_(new CompoundTag()))) {
                throw new IllegalStateException("Incomplete original task mutation");
            }
            session.active.plan.advance(write.step);
            if (!OwnerFullRepairGuard.watchTaskOrigin((ItemStack)session.actual.get(), session)) {
                throw new IllegalStateException("Task alias watch incomplete");
            }
        }
        catch (Throwable throwable2) {
            OwnerStatueTaskOrigin.burn(session);
        }
        finally {
            write.closed = true;
            if (session.active != null && session.active.writing == write) {
                session.active.writing = null;
            }
        }
    }

    static boolean permitsWatch(Session session, Object object) {
        return OwnerStatueTaskOrigin.caller(OwnerFullRepairGuard.class, "watchTaskOrigin") && session != null && session.actual.get() == object && session.phase == Phase.WRITING && !session.rejected && session.active != null && session.active.writing != null;
    }

    static void changed(Object object, Object object2, int n) {
        boolean bl;
        if (!(object instanceof ItemStack)) {
            return;
        }
        ItemStack itemStack = (ItemStack)object;
        Session session = OwnerStatueTaskOrigin.session(itemStack);
        if (session == null) {
            return;
        }
        if (session.thread != Thread.currentThread() || session.phase != Phase.WRITING || session.active == null) {
            OwnerStatueTaskOrigin.burn(session);
            return;
        }
        Write write = session.active.writing;
        if (write != null && !write.closed && object2 == write.receiver && (n == 20 || n == 21) && (bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.takeWhile(stackFrame -> stackFrame.getDeclaringClass() != OwnerStatueTaskWrites.class && stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).allMatch(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == OwnerStatueTaskOrigin.class || stackFrame.getDeclaringClass().getNestHost() == OwnerStatueWearAdmission.class || stackFrame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class || stackFrame.getDeclaringClass() == CompoundTag.class || stackFrame.getDeclaringClass() == ItemStack.class || stackFrame.getDeclaringClass() == Item.class)).booleanValue())) {
            return;
        }
        if (object2 == object && session.active.plan.operation == FourWeaponTransactions.Operation.CLAIM_REPAIR && (n == 20 || n == 21) && (bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream2 -> stream2.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && stackFrame.getMethodName().equals("setInternalDamage")) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponRepair.class && stackFrame.getMethodName().equals("claim"))) != false).booleanValue())) {
            return;
        }
        OwnerStatueTaskOrigin.burn(session);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void custodyChanged(Object object) {
        Object object2 = LOCK;
        synchronized (object2) {
            for (Session session : SESSIONS.values()) {
                Inventory inventory = (Inventory)session.inventory.get();
                if (inventory != null && object != inventory.f_35974_ && object != inventory.f_35975_ && object != inventory.f_35976_) continue;
                OwnerStatueTaskOrigin.burn(session);
            }
        }
    }

    static void failedWrite(ItemStack itemStack) {
        OwnerStatueTaskOrigin.burn(OwnerStatueTaskOrigin.session(itemStack));
    }

    static void burn(Session session) {
        if (session != null) {
            session.rejected = true;
            session.phase = Phase.BURNED;
            session.active = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static void outerCommitted(Session session, OwnerStatueTaskService.Prepared prepared, StatueRepairMutation.Committed committed) {
        int n;
        int[] nArray;
        CompoundTag compoundTag;
        Call call;
        boolean bl;
        OwnerLineageRevocationStore.Lease lease;
        block27: {
            if (session == null) {
                return;
            }
            lease = null;
            bl = false;
            if (!(OwnerStatueTaskOrigin.caller(OwnerStatueTaskService.Prepared.class, "afterOuterCommitted") && committed.take(prepared) && OwnerStatueTaskOrigin.physical(session) && session.phase == Phase.WRITING && session.active != null && session.active.prepared == prepared && session.active.writing == null && session.active.plan.complete())) {
                throw new IllegalStateException("No original complete task terminal");
            }
            call = session.active;
            compoundTag = prepared.originOutcome(committed);
            nArray = compoundTag.m_128465_("ObservedCommittedPayments");
            int[] nArray2 = compoundTag.m_128465_("PolicyCredit");
            if (!(nArray.length == 3 && nArray2.length == 3 && session.task.equals(compoundTag.m_128342_("Task")) && session.call.equals(compoundTag.m_128342_("AcceptedWearCall")) && call.version == compoundTag.m_128454_("Sequence") && call.plan.cursor.equals((Object)compoundTag.m_128469_("Expected")) && session.rules.equals(compoundTag.m_128461_("Rules")) && call.plan.cursor.equals((Object)((ItemStack)session.actual.get()).m_41739_(new CompoundTag())))) {
                throw new IllegalStateException("Task terminal differs from private plan");
            }
            for (n = 0; n < 3; ++n) {
                int n2 = session.payments[n] + (call.plan.operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR && call.plan.index == n ? call.paid : 0);
                if (nArray2[n] == 0 && nArray[n] == n2) continue;
                throw new IllegalStateException("Task policy/real payment ancestry mismatch");
            }
            if (call.plan.operation == FourWeaponTransactions.Operation.CLAIM_REPAIR) break block27;
            if (compoundTag.m_128471_("Consumed")) {
                throw new IllegalStateException("Premature task consumption");
            }
            System.arraycopy(nArray, 0, session.payments, 0, 3);
            session.expected = call.plan.cursor.m_6426_();
            session.version = call.version;
            session.active = null;
            session.phase = Phase.IDLE;
            if (bl || lease == null) return;
            try {
                OwnerLineageRevocationStore.retireLease(lease);
                return;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        if (!compoundTag.m_128471_("Consumed") || !OwnerStatueTaskOrigin.uniqueInventoryCopy(session, (ItemStack)session.actual.get())) {
            throw new IllegalStateException("Original claim not consumed or duplicated");
        }
        for (n = 0; n < 3; ++n) {
            if (nArray[n] == FoodWeaponLifecycleRules.repairRequired(session.evolution, n)) continue;
            throw new IllegalStateException("Claim lacks real task payments");
        }
        session.phase = Phase.CONSUMING;
        Publication publication = new Publication(session, call, committed);
        prepared.consumeOriginGeneration(publication, false);
        lease = session.store.consumeTaskOrigin(publication);
        if (lease == null || !publication.live()) {
            throw new IllegalStateException("Task generation store consumption failed");
        }
        prepared.consumeOriginGeneration(publication, true);
        session.phase = Phase.STORE_CONSUMED;
        bl = OwnerRepairGenerationAuthority.publishTaskOrigin(publication, lease);
        if (!bl) {
            throw new IllegalStateException("Task generation alias publication failed");
        }
        session.phase = Phase.PUBLISHED;
        Object object = LOCK;
        synchronized (object) {
            SESSIONS.remove(session.actual.get());
        }
        if (bl || lease == null) return;
        try {
            OwnerLineageRevocationStore.retireLease(lease);
            return;
        }
        catch (Throwable throwable) {}
        return;
        catch (Throwable throwable) {
            try {
                bl = false;
                OwnerStatueTaskOrigin.burn(session);
                return;
            }
            catch (Throwable throwable2) {
                throw throwable2;
            }
            finally {
                if (!bl && lease != null) {
                    try {
                        OwnerLineageRevocationStore.retireLease(lease);
                    }
                    catch (Throwable throwable3) {}
                }
            }
        }
    }

    static final class Session {
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final Object level;
        private final UUID owner;
        private final UUID weapon;
        private final UUID task;
        private final UUID call;
        private final UUID load;
        private final BlockPos statue;
        private final Object statueState;
        private final int slot;
        private final int tint;
        private final int evolution;
        private final int maximum;
        private final String profile;
        private final String rules;
        private final Thread thread = Thread.currentThread();
        private final int[] payments = new int[3];
        private final OwnerLineageRevocationStore store;
        private volatile Phase phase = Phase.RESERVING;
        private volatile boolean rejected;
        private OwnerLineageRevocationStore.TaskReservation reservation;
        private CompoundTag expected;
        private long version;
        private Call active;
        private int[] progressArray;

        private Session(CompoundTag compoundTag, CompoundTag compoundTag2, OwnerLineageRevocationStore ownerLineageRevocationStore, ServerPlayer serverPlayer, ItemStack itemStack, int n, BlockPos blockPos, int n2) {
            this.actual = new WeakReference<ItemStack>(itemStack);
            this.player = new WeakReference<ServerPlayer>(serverPlayer);
            this.inventory = new WeakReference<Inventory>(serverPlayer.m_150109_());
            this.server = serverPlayer.m_20194_();
            this.level = serverPlayer.m_9236_();
            this.owner = serverPlayer.m_20148_();
            this.weapon = compoundTag.m_128342_("Weapon");
            this.task = compoundTag2.m_128342_("Task");
            this.call = compoundTag.m_128342_("Call");
            this.load = compoundTag.m_128342_("OriginalLoad");
            this.statue = blockPos.m_7949_();
            this.statueState = serverPlayer.m_9236_().m_8055_(blockPos);
            this.slot = n;
            this.tint = n2;
            this.evolution = compoundTag.m_128451_("Level");
            this.maximum = compoundTag.m_128451_("Maximum");
            this.profile = compoundTag.m_128461_("Profile");
            this.rules = compoundTag.m_128461_("Rules");
            this.expected = compoundTag.m_128469_("Expected").m_6426_();
            this.store = ownerLineageRevocationStore;
        }

        CompoundTag reservationBinding(OwnerLineageRevocationStore ownerLineageRevocationStore) {
            if (!OwnerStatueTaskOrigin.caller(OwnerLineageRevocationStore.class, "reserveTaskOrigin") || ownerLineageRevocationStore != this.store || this.phase != Phase.RESERVING || !OwnerStatueTaskOrigin.physical(this)) {
                throw new IllegalStateException("Detached task-origin reservation");
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128362_("Owner", this.owner);
            compoundTag.m_128362_("Weapon", this.weapon);
            compoundTag.m_128362_("Task", this.task);
            compoundTag.m_128362_("OriginalCall", this.call);
            compoundTag.m_128362_("OriginalLoad", this.load);
            return compoundTag;
        }
    }

    private static enum Phase {
        RESERVING,
        IDLE,
        WRITING,
        CONSUMING,
        STORE_CONSUMED,
        PUBLISHED,
        BURNED;

    }

    private static final class Call {
        private final OwnerStatueTaskService.Prepared prepared;
        private final StatueRepairMutation outer;
        private final UUID transaction;
        private final long version;
        private final OwnerStatueTaskWritePlan plan;
        private int paid;
        private Write writing;

        private Call(Session session, OwnerStatueTaskService.Prepared prepared, StatueRepairMutation statueRepairMutation, FourWeaponTransactions.Operation operation, int n, CompoundTag compoundTag, UUID uUID) {
            this.prepared = prepared;
            this.outer = statueRepairMutation;
            this.transaction = uUID;
            this.version = compoundTag.m_128454_("Sequence");
            this.plan = new OwnerStatueTaskWritePlan(session.expected, operation, n, session.evolution, session.maximum, session.profile);
        }
    }

    static final class ProgressRead {
        private final Session session;
        private final CompoundTag receiver;
        private final Object storage;
        private final String key;
        private final CompoundTag before;
        private int[] array;

        private ProgressRead(Session session, CompoundTag compoundTag, Object object, String string, CompoundTag compoundTag2) {
            this.session = session;
            this.receiver = compoundTag;
            this.storage = object;
            this.key = string;
            this.before = compoundTag2;
        }
    }

    static final class Write {
        private final Session session;
        private final OwnerStatueTaskWritePlan.Step step;
        private final Object receiver;
        private final CompoundTag before;
        private boolean closed;

        private Write(Session session, OwnerStatueTaskWritePlan.Step step, Object object) {
            this.session = session;
            this.step = step;
            this.receiver = object;
            this.before = session.active.plan.cursor.m_6426_();
        }
    }

    static final class Publication {
        private final Session session;
        private final Call source;
        private final StatueRepairMutation.Committed terminal;
        private boolean storeTaken;
        private boolean continuationAttempted;
        private boolean predecessorTaken;
        private PublishedPredecessor predecessor;

        private Publication(Session session, Call call, StatueRepairMutation.Committed committed) {
            this.session = session;
            this.source = call;
            this.terminal = committed;
        }

        boolean belongs(OwnerStatueTaskService.Prepared prepared) {
            return this.source.prepared == prepared && this.terminal.remains(prepared) && this.live();
        }

        private boolean live() {
            ItemStack itemStack = (ItemStack)this.session.actual.get();
            return !this.session.rejected && OwnerStatueTaskOrigin.physical(this.session) && this.terminal.remains(this.source.prepared) && this.source == this.session.active && this.source.plan.complete() && itemStack != null && this.source.plan.cursor.equals((Object)itemStack.m_41739_(new CompoundTag())) && this.session.rules.equals(OwnerStatueTaskPlan.rules(itemStack));
        }

        CompoundTag takeForStore(OwnerLineageRevocationStore ownerLineageRevocationStore) {
            if (!OwnerStatueTaskOrigin.caller(OwnerLineageRevocationStore.class, "consumeTaskOrigin") || this.storeTaken || ownerLineageRevocationStore != this.session.store || this.session.phase != Phase.CONSUMING || !this.live()) {
                throw new IllegalStateException("Detached task generation consumption");
            }
            this.storeTaken = true;
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128362_("Owner", this.session.owner);
            compoundTag.m_128362_("Weapon", this.session.weapon);
            compoundTag.m_128362_("Task", this.session.task);
            compoundTag.m_128362_("Claim", this.source.transaction);
            return compoundTag;
        }

        boolean bindContinuation(Object object) {
            if (!OwnerStatueTaskOrigin.caller(OwnerRepairGenerationAuthority.class, "publishTaskOrigin") || this.continuationAttempted) {
                return false;
            }
            this.continuationAttempted = true;
            if (!this.storeTaken || this.session.phase != Phase.STORE_CONSUMED || !this.live()) {
                return false;
            }
            ItemStack itemStack = (ItemStack)this.session.actual.get();
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(itemStack);
            if (!this.candidate(lease) || OwnerRepairGenerationAuthority.current(itemStack) != object || !OwnerRepairGenerationAuthority.repaired(itemStack, lease)) {
                return false;
            }
            this.predecessor = new PublishedPredecessor(this, object, lease);
            return OwnerRepairContinuation.bind(this, itemStack, (ServerPlayer)this.session.player.get(), this.session.slot, this.session.rules, this.source.plan.cursor, object, this.predecessor);
        }

        boolean takePublishedPredecessor(PublishedPredecessor publishedPredecessor, ItemStack itemStack, ServerPlayer serverPlayer, int n, String string, CompoundTag compoundTag, Object object, OwnerLineageRevocationStore.Lease lease) {
            if (!OwnerStatueTaskOrigin.caller(OwnerRepairContinuation.class, "bind") || this.predecessorTaken) {
                return false;
            }
            this.predecessorTaken = true;
            return this.continuationAttempted && publishedPredecessor != null && publishedPredecessor == this.predecessor && this.storeTaken && this.session.phase == Phase.STORE_CONSUMED && this.live() && publishedPredecessor.matches(this, itemStack, serverPlayer, n, string, compoundTag, object, lease);
        }

        OwnerLineageRevocationStore.TaskReservation reservation() {
            return this.session.reservation;
        }

        boolean candidate(OwnerLineageRevocationStore.Lease lease) {
            return lease != null && this.session.reservation != null && this.session.owner.equals(lease.owner) && this.session.weapon.equals(lease.weapon) && this.session.reservation.incarnation.equals(lease.incarnation) && lease.generation == 1L;
        }

        ItemStack actualForPublisher() {
            return OwnerStatueTaskOrigin.caller(OwnerRepairGenerationAuthority.class, "publishTaskOrigin") && this.session.phase == Phase.STORE_CONSUMED && this.live() ? (ItemStack)this.session.actual.get() : null;
        }
    }

    static final class PublishedPredecessor {
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final UUID owner;
        private final UUID weapon;
        private final UUID parentTask;
        private final UUID parentClaim;
        private final UUID originalCall;
        private final UUID originalLoad;
        private final long taskSequence;
        private final int slot;
        private final String rules;
        private final CompoundTag expected;
        private final Object generation;
        private final OwnerLineageRevocationStore.Lease lease;
        private final UUID incarnation;
        private final long authorityGeneration;

        private PublishedPredecessor(Publication publication, Object object, OwnerLineageRevocationStore.Lease lease) {
            this.actual = publication.session.actual;
            this.player = publication.session.player;
            this.owner = publication.session.owner;
            this.weapon = publication.session.weapon;
            this.parentTask = publication.session.task;
            this.parentClaim = publication.source.transaction;
            this.originalCall = publication.session.call;
            this.originalLoad = publication.session.load;
            this.taskSequence = publication.source.version;
            this.slot = publication.session.slot;
            this.rules = publication.session.rules;
            this.expected = publication.source.plan.cursor.m_6426_();
            this.generation = object;
            this.lease = lease;
            this.incarnation = lease.incarnation;
            this.authorityGeneration = lease.generation;
        }

        CompoundTag prewriteBinding(ItemStack itemStack, ServerPlayer serverPlayer, int n, String string, CompoundTag compoundTag, Object object, OwnerLineageRevocationStore.Lease lease) {
            if (!(OwnerStatueTaskOrigin.caller(OwnerRepairContinuation.class, "prewrite") && this.actual.get() == itemStack && this.player.get() == serverPlayer && serverPlayer != null && this.owner.equals(serverPlayer.m_20148_()) && this.owner.equals(lease.owner) && this.weapon.equals(lease.weapon) && this.slot == n && this.rules.equals(string) && this.expected.equals((Object)compoundTag) && this.generation == object && this.lease == lease && this.incarnation.equals(lease.incarnation) && this.authorityGeneration == lease.generation && OwnerRepairGenerationAuthority.current(itemStack) == object && OwnerFullRepairGuard.lease(itemStack) == lease && OwnerRepairGenerationAuthority.repaired(itemStack, lease))) {
                throw new IllegalStateException("Detached prewrite predecessor");
            }
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128362_("Owner", this.owner);
            compoundTag2.m_128362_("Weapon", this.weapon);
            compoundTag2.m_128362_("ParentTask", this.parentTask);
            compoundTag2.m_128362_("ParentClaim", this.parentClaim);
            compoundTag2.m_128362_("OriginalCall", this.originalCall);
            compoundTag2.m_128362_("OriginalLoad", this.originalLoad);
            compoundTag2.m_128356_("TaskSequence", this.taskSequence);
            compoundTag2.m_128405_("Slot", n);
            compoundTag2.m_128359_("Rules", string);
            compoundTag2.m_128365_("Expected", (Tag)this.expected.m_6426_());
            compoundTag2.m_128362_("Incarnation", this.incarnation);
            compoundTag2.m_128356_("Generation", this.authorityGeneration);
            return compoundTag2;
        }

        private boolean matches(Publication publication, ItemStack itemStack, ServerPlayer serverPlayer, int n, String string, CompoundTag compoundTag, Object object, OwnerLineageRevocationStore.Lease lease) {
            return this.actual.get() == itemStack && publication.session.actual.get() == itemStack && this.player.get() == serverPlayer && publication.session.player.get() == serverPlayer && this.owner.equals(publication.session.owner) && this.owner.equals(serverPlayer.m_20148_()) && this.owner.equals(lease.owner) && this.weapon.equals(publication.session.weapon) && this.weapon.equals(lease.weapon) && this.parentTask.equals(publication.session.task) && this.parentClaim.equals(publication.source.transaction) && this.originalCall.equals(publication.session.call) && this.originalLoad.equals(publication.session.load) && this.taskSequence == publication.source.version && this.slot == n && publication.session.slot == n && this.rules.equals(string) && this.rules.equals(publication.session.rules) && this.expected.equals((Object)compoundTag) && this.expected.equals((Object)publication.source.plan.cursor) && this.generation == object && OwnerRepairGenerationAuthority.current(itemStack) == object && this.lease == lease && OwnerFullRepairGuard.lease(itemStack) == lease && this.incarnation.equals(lease.incarnation) && this.authorityGeneration == lease.generation && OwnerRepairGenerationAuthority.repaired(itemStack, lease);
        }
    }
}

