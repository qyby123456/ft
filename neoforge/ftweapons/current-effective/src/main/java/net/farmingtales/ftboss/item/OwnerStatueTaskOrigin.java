package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.OwnerStatueTaskWritePlan.Step;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
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
    private static final Map<ItemStack, OwnerStatueTaskOrigin.Session> SESSIONS = new WeakHashMap<>();
    private static final Object LOCK = new Object();
    private static final Map<Object, OwnerStatueTaskOrigin.ProgressRead> READS = new IdentityHashMap<>();

    static OwnerStatueTaskOrigin.ProgressRead progressRead(ItemStack var0, CompoundTag var1) {
        OwnerStatueTaskOrigin.Session var2 = session(var0);
        if (var2 == null) {
            return null;
        } else {
            try {
                if (caller(OwnerStatueTaskWrites.class, "progress")
                    && physical(var2)
                    && (var2.phase == OwnerStatueTaskOrigin.Phase.IDLE || var2.phase == OwnerStatueTaskOrigin.Phase.WRITING)) {
                    boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var0x -> var0x.filter(
                                        var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class
                                                && var0xx.getDeclaringClass() != OwnerStatueTaskWrites.class
                                    )
                                    .findFirst()
                                    .map(
                                        var0xx -> var0xx.getDeclaringClass() == FoodWeaponRepair.class
                                                && Set.of("activeQuest", "view", "submit").contains(var0xx.getMethodName())
                                    )
                                    .orElse(false)
                        );
                    CompoundTag var4 = var2.active == null ? var2.expected : var2.active.plan.cursor;
                    if (var3
                        && var4.equals(NativeFoundationItemData.image(var0))
                        && NativeSourceLedgerItemData.rawCustomDataForObservation(var0).getCompound("FTBossWeaponLifecycle").getCompound("RepairQuest") == var1) {
                        Tag var5 = var1.get("Progress");
                        if (!(var5 instanceof IntArrayTag)) {
                            throw new IllegalStateException("Progress storage type changed");
                        } else {
                            OwnerStatueTaskOrigin.ProgressRead var6 = new OwnerStatueTaskOrigin.ProgressRead(var2, var1, var5, "Progress", var4.copy());
                            synchronized (LOCK) {
                                if (READS.containsKey(var1) || READS.containsKey(var5)) {
                                    throw new IllegalStateException("Reentrant task progress read");
                                }

                                READS.put(var1, var6);
                                READS.put(var5, var6);
                            }

                            return var6;
                        }
                    } else {
                        throw new IllegalStateException("Progress read differs from private source");
                    }
                } else {
                    throw new IllegalStateException("Detached task progress read");
                }
            } catch (Throwable var10) {
                burn(var2);
                return null;
            }
        }
    }

    static OwnerStatueTaskOrigin.ProgressRead identityRead(ItemStack var0, CompoundTag var1) {
        OwnerStatueTaskOrigin.Session var2 = session(var0);
        if (var2 == null) {
            return null;
        } else {
            try {
                boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.filter(
                                    var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class
                                            && var0xx.getDeclaringClass() != OwnerStatueTaskWrites.class
                                )
                                .findFirst()
                                .map(
                                    var0xx -> var0xx.getDeclaringClass() == FoodWeaponLifecycle.class
                                            && Set.of("weaponId", "ensureTracked").contains(var0xx.getMethodName())
                                )
                                .orElse(false)
                    );
                CompoundTag var4 = var2.active == null ? var2.expected : var2.active.plan.cursor;
                if (var3 && physical(var2) && var4.equals(NativeFoundationItemData.image(var0)) && NativeSourceLedgerItemData.rawCustomDataForObservation(var0).getCompound("FTBossWeaponLifecycle") == var1) {
                    Tag var5 = var1.get("Weapon");
                    if (!(var5 instanceof IntArrayTag)) {
                        throw new IllegalStateException("Weapon identity storage changed");
                    } else {
                        OwnerStatueTaskOrigin.ProgressRead var6 = new OwnerStatueTaskOrigin.ProgressRead(var2, var1, var5, "Weapon", var4.copy());
                        synchronized (LOCK) {
                            if (READS.containsKey(var1) || READS.containsKey(var5)) {
                                throw new IllegalStateException("Reentrant identity read");
                            }

                            READS.put(var1, var6);
                            READS.put(var5, var6);
                        }

                        return var6;
                    }
                } else {
                    throw new IllegalStateException("Detached bounded weapon identity read");
                }
            } catch (Throwable var10) {
                burn(var2);
                return null;
            }
        }
    }

    static void identityReturned(OwnerStatueTaskOrigin.ProgressRead var0, Throwable var1) {
        if (var0 != null) {
            synchronized (LOCK) {
                if (READS.get(var0.receiver) == var0) {
                    READS.remove(var0.receiver);
                }

                if (READS.get(var0.storage) == var0) {
                    READS.remove(var0.storage);
                }
            }

            if (var1 != null || !physical(var0.session) || !var0.before.equals(NativeFoundationItemData.image(var0.session.actual.get()))) {
                burn(var0.session);
            }
        }
    }

    static boolean boundedProgressAlias(Object var0, Object var1) {
        if (var1 instanceof int[] var2 && (caller(OwnerFullRepairGuard.class, "alias")
            || caller(NativeNbtAliasPolicy.class, "trustedTaskProgressArrayRead"))) {
            OwnerStatueTaskOrigin.ProgressRead var3;
            synchronized (LOCK) {
                var3 = READS.get(var0);
            }

            if (var3 == null) {
                return false;
            }

            boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var1x -> var1x.anyMatch(
                            var1xx -> var1xx.getDeclaringClass() == CompoundTag.class
                                    && (
                                        var3.key.equals("Progress")
                                            ? var1xx.getDescriptor().equals("(Ljava/lang/String;)[I")
                                            : var1xx.getDescriptor().equals("(Ljava/lang/String;)Ljava/util/UUID;")
                                                || var1xx.getDescriptor().equals("(Ljava/lang/String;)Z")
                                    )
                        )
                );
            if (var7
                && (var3.array == null || var3.array == var2)
                && physical(var3.session)
                && Arrays.equals(
                    var2,
                    (var3.key.equals("Progress")
                            ? NativeRepairStackImage.data(var3.before).getCompound("FTBossWeaponLifecycle").getCompound("RepairQuest")
                            : NativeRepairStackImage.data(var3.before).getCompound("FTBossWeaponLifecycle"))
                        .getIntArray(var3.key)
                )) {
                if (caller(NativeNbtAliasPolicy.class, "trustedTaskProgressArrayRead") && !var3.key.equals("Progress")) return false;
                if (var3.key.equals("Progress")) {
                    if (var0 != var3.storage || !(var0 instanceof IntArrayTag storage) || !(var0 instanceof NativeTaskProgressArrayFields)) return false;
                    var3.session.progressStorage = storage;
                }
                var3.array = var2;
                return true;
            }

            burn(var3.session);
            return false;
        }

        return false;
    }

    static void progressReturned(OwnerStatueTaskOrigin.ProgressRead var0, int[] var1, Throwable var2) {
        if (var0 != null) {
            synchronized (LOCK) {
                if (READS.get(var0.receiver) == var0) {
                    READS.remove(var0.receiver);
                }

                if (READS.get(var0.storage) == var0) {
                    READS.remove(var0.storage);
                }
            }

            if (var2 == null && var0.array == var1 && physical(var0.session) && var0.before.equals(NativeFoundationItemData.image(var0.session.actual.get()))) {
                var0.session.progressArray = var1;
            } else {
                burn(var0.session);
            }
        }
    }

    static OwnerStatueTaskOrigin.Write beforeArrayWrite(ItemStack var0, CompoundTag var1, int[] var2, int var3, int var4) {
        OwnerStatueTaskOrigin.Session var5 = session(var0);
        if (var5 == null) {
            return null;
        } else {
            try {
                if (caller(OwnerStatueTaskWrites.class, "advanceProgress")
                    && var5.active != null
                    && var2 == var5.progressArray
                    && var3 == var5.active.plan.index
                    && var4 == var5.active.paid) {
                    int[] var6 = (int[])var2.clone();
                    var6[var3] = Math.addExact(var6[var3], var4);
                    OwnerStatueTaskOrigin.Write permit = beforeWrite(var0, var1, "ProgressArray", new IntArrayTag(var6), false);
                    if (permit == null || var5.progressStorage == null || var1.get("Progress") != var5.progressStorage
                        || !((Object)var5.progressStorage instanceof NativeTaskProgressArrayFields fields)) {
                        throw new IllegalStateException("Missing original native Progress array storage");
                    }
                    fields.ftboss$nativeTaskProgressArrayWrite(permit);
                    return permit;
                } else {
                    throw new IllegalStateException("Unquoted original Progress array mutation");
                }
            } catch (Throwable var7) {
                burn(var5);
                return null;
            }
        }
    }

    static boolean permitsNativeProgressArrayWrite(Object storage, Object permit) {
        if (!caller(NativeNbtAliasPolicy.class, "requireTaskProgressArrayWrite") || !(permit instanceof OwnerStatueTaskOrigin.Write write)) return false;
        OwnerStatueTaskOrigin.Session session = write.session;
        if (write.closed || write.arrayVersionAdvanced || !write.step.key().equals("ProgressArray")
            || !physical(session) || session.phase != OwnerStatueTaskOrigin.Phase.WRITING || session.active == null
            || session.active.writing != write || session.progressStorage != storage || session.progressArray == null
            || !(write.receiver instanceof CompoundTag receiver) || receiver.get("Progress") != storage
            || !write.before.equals(session.active.plan.cursor)
            || !write.before.equals(NativeFoundationItemData.image(session.actual.get()))) return false;
        write.arrayVersionAdvanced = true;
        return true;
    }

    static boolean permitsNativeProgressArrayRejection(Object storage, Object source) {
        return caller(NativeNbtAliasPolicy.class, "requireTaskProgressArrayRejection")
            && source instanceof OwnerStatueTaskOrigin.Session session && session.rejected && session.progressStorage == storage;
    }

    private OwnerStatueTaskOrigin() {
    }

    private static boolean caller(Class<?> var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class)
                        .findFirst()
                        .map(var2x -> var2x.getDeclaringClass() == var0 && var2x.getMethodName().equals(var1))
                        .orElse(false)
            );
    }

    private static boolean physical(OwnerStatueTaskOrigin.Session var0) {
        ServerPlayer var1 = var0.player.get();
        ItemStack var2 = var0.actual.get();
        return !var0.rejected
            && var0.thread == Thread.currentThread()
            && var1 != null
            && var2 != null
            && var0.server != null
            && var0.server.isSameThread()
            && var1.getServer() == var0.server
            && var0.owner.equals(var1.getUUID())
            && var1.level() == var0.level
            && var1.getInventory() == var0.inventory.get()
            && var1.getInventory().getItem(var0.slot) == var2
            && var2.getCount() == 1
            && (var0.slot == 40 ? var1.getOffhandItem() == var2 : var1.getInventory().selected == var0.slot && var1.getMainHandItem() == var2)
            && var0.statueState.equals(var1.level().getBlockState(var0.statue));
    }

    private static boolean uniqueInventoryCopy(OwnerStatueTaskOrigin.Session var0, ItemStack var1) {
        int var2 = 0;

        for (ServerPlayer var4 : var0.server.getPlayerList().getPlayers()) {
            Inventory var5 = var4.getInventory();

            for (int var6 = 0; var6 < var5.getContainerSize(); var6++) {
                ItemStack var7 = var5.getItem(var6);
                CompoundTag var8 = NativeRepairStackImage.data(NativeFoundationItemData.image(var7)).getCompound("FTBossWeaponLifecycle");
                if (var8.hasUUID("Weapon") && var0.weapon.equals(var8.getUUID("Weapon"))) {
                    if (var7 != var1 || var4 != var0.player.get() || ++var2 > 1) {
                        return false;
                    }
                }
            }
        }

        return var2 == 1;
    }

    private static OwnerStatueTaskOrigin.Session session(ItemStack var0) {
        synchronized (LOCK) {
            return SESSIONS.get(var0);
        }
    }

    static OwnerStatueTaskOrigin.Session reserve(
        OwnerStatueWearAdmission.Accepted var0,
        OwnerStatueTaskService.Prepared var1,
        OwnerLineageRevocationStore var2,
        ServerPlayer var3,
        ItemStack var4,
        int var5,
        BlockPos var6,
        int var7,
        CompoundTag var8
    ) throws IOException {
        if (caller(OwnerStatueTaskService.class, "prepareStart") && var0 != null && var0.transferToTaskOrigin(var3, var4, var5)) {
            CompoundTag var9 = var0.record();
            OwnerStatueTaskOrigin.Session var10 = new OwnerStatueTaskOrigin.Session(var9, var8, var2, var3, var4, var5, var6, var7);
            synchronized (LOCK) {
                if (SESSIONS.containsKey(var4)) {
                    return null;
                }

                SESSIONS.put(var4, var10);
            }

            try {
                if (physical(var10)
                    && OwnerFullRepairGuard.lease(var4) == null
                    && OwnerFullRepairGuard.generation(var4) == null
                    && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var4)
                    && !FourWeaponFoundationAdapter.applies(var4)
                    && !SwordCavityBuild.applies(var4)) {
                    var10.reservation = var2.reserveTaskOrigin(var10);
                    if (var10.reservation != null && physical(var10)) {
                        var10.phase = OwnerStatueTaskOrigin.Phase.IDLE;
                        return var10;
                    } else {
                        throw new IOException("Task-origin negative reservation failed");
                    }
                } else {
                    throw new IOException("Unsupported task-origin predecessor");
                }
            } catch (RuntimeException | Error | IOException var13) {
                burn(var10);
                throw var13;
            }
        } else {
            return null;
        }
    }

    static OwnerStatueTaskOrigin.Session find(ServerPlayer var0, ItemStack var1, int var2, CompoundTag var3) {
        if (!caller(OwnerStatueTaskService.class, "prepare")) {
            return null;
        } else {
            OwnerStatueTaskOrigin.Session var4 = session(var1);
            if (var4 == null) {
                return null;
            } else {
                try {
                    if (physical(var4)
                        && var4.phase == OwnerStatueTaskOrigin.Phase.IDLE
                        && var4.player.get() == var0
                        && var4.slot == var2
                        && var3.hasUUID("Task")
                        && var4.task.equals(var3.getUUID("Task"))
                        && var3.getLong("Sequence") == var4.version
                        && var3.hasUUID("AcceptedWearCall")
                        && var4.call.equals(var3.getUUID("AcceptedWearCall"))
                        && var4.expected.equals(var3.getCompound("Expected"))
                        && var4.rules.equals(var3.getString("Rules"))) {
                        return var4;
                    } else {
                        burn(var4);
                        return null;
                    }
                } catch (Throwable var6) {
                    burn(var4);
                    return null;
                }
            }
        }
    }

    static void begin(
        OwnerStatueTaskOrigin.Session var0,
        OwnerStatueTaskService.Prepared var1,
        StatueRepairMutation var2,
        Operation var3,
        int var4,
        CompoundTag var5,
        UUID var6
    ) {
        if (var0 != null) {
            try {
                if (!caller(OwnerStatueTaskService.Prepared.class, "beforeOriginal")
                    || !var2.taskOriginSource(var1)
                    || !physical(var0)
                    || !uniqueInventoryCopy(var0, var0.actual.get())
                    || var0.phase != OwnerStatueTaskOrigin.Phase.IDLE
                    || var0.active != null
                    || var5.getLong("Sequence") != var0.version + 1L
                    || !var0.expected.equals(var5.getCompound("Expected"))
                    || !var0.expected.equals(NativeFoundationItemData.image(var0.actual.get()))
                    || !var0.rules.equals(OwnerStatueTaskPlan.rules(var0.actual.get()))) {
                    throw new IllegalStateException("Detached original task call");
                }

                var0.active = new OwnerStatueTaskOrigin.Call(var0, var1, var2, var3, var4, var5, var6);
                var0.phase = OwnerStatueTaskOrigin.Phase.WRITING;
            } catch (Throwable var8) {
                burn(var0);
            }
        }
    }

    static void paid(OwnerStatueTaskOrigin.Session var0, OwnerStatueTaskService.Prepared var1, int var2, int var3) {
        if (var0 != null) {
            try {
                if (!caller(OwnerStatueTaskService.Prepared.class, "afterShrink")
                    || var0.phase != OwnerStatueTaskOrigin.Phase.WRITING
                    || !physical(var0)
                    || var0.active.prepared != var1
                    || var0.active.plan.operation != Operation.SUBMIT_REPAIR
                    || var2 != var0.active.plan.index
                    || var3 <= 0) {
                    throw new IllegalStateException("Detached task payment commit");
                }

                var0.active.paid = Math.addExact(var0.active.paid, var3);
            } catch (Throwable var5) {
                burn(var0);
            }
        }
    }

    private static boolean writer(boolean var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(
                            var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueTaskOrigin.class
                                    && var0xx.getDeclaringClass() != OwnerStatueTaskWrites.class
                                    && var0xx.getDeclaringClass() != OwnerStatueWearWrites.class
                        )
                        .findFirst()
                        .map(
                            var1x -> var0
                                    ? OwnerStatueWearBoundary.writer(1, var1x)
                                    : var1x.getDeclaringClass() == FoodWeaponRepair.class
                                            && Set.of("start", "submit", "claim", "advance").contains(var1x.getMethodName())
                                        || var1x.getDeclaringClass() == FoodWeaponLifecycle.class
                                            && Set.of("ensureTracked", "restoreAfterQuest").contains(var1x.getMethodName())
                        )
                        .orElse(false)
            );
    }

    static OwnerStatueTaskOrigin.Write beforeWrite(ItemStack var0, Object var1, String var2, Tag var3, boolean var4) {
        OwnerStatueTaskOrigin.Session var5 = session(var0);
        if (var5 == null) {
            return null;
        } else {
            try {
                OwnerStatueTaskOrigin.Call var6 = var5.active;
                if (writer(var4)
                    && physical(var5)
                    && var5.phase == OwnerStatueTaskOrigin.Phase.WRITING
                    && var6 != null
                    && var6.writing == null
                    && var6.outer.taskOriginRunning(var6.prepared)
                    && var6.plan.cursor.equals(NativeFoundationItemData.image(var0))) {
                    if (var6.plan.operation == Operation.SUBMIT_REPAIR && var6.plan.steps.isEmpty()) {
                        var6.plan.submit(var6.paid);
                    }

                    Step var7 = var6.plan.expected(var2, var3);
                    CompoundTag var8 = NativeSourceLedgerItemData.rawCustomDataForObservation(var0);

                    for (String var12 : var7.path().isEmpty() ? new String[0] : var7.path().split("/")) {
                        var8 = var8.getCompound(var12);
                    }

                    if ((var4 || var8 == var1) && (!var4 || var7.path().isEmpty() && var2.equals("Damage"))) {
                        OwnerStatueTaskOrigin.Write var14 = new OwnerStatueTaskOrigin.Write(var5, var7, var4 ? NativeSourceLedgerItemData.rawCustomDataForObservation(var0) : var1);
                        var6.writing = var14;
                        return var14;
                    } else {
                        throw new IllegalStateException("Task write receiver differs");
                    }
                } else {
                    throw new IllegalStateException("Task writer is not live");
                }
            } catch (Throwable var13) {
                burn(var5);
                return null;
            }
        }
    }

    static void afterWrite(OwnerStatueTaskOrigin.Write var0, Throwable var1) {
        if (var0 != null) {
            OwnerStatueTaskOrigin.Session var2 = var0.session;

            try {
                if (var0.closed
                    || !physical(var2)
                    || var2.phase != OwnerStatueTaskOrigin.Phase.WRITING
                    || var2.active.writing != var0
                    || var1 != null
                    || var0.step.key().equals("ProgressArray") && !var0.arrayVersionAdvanced
                    || !var0.step.after().equals(NativeFoundationItemData.image(var2.actual.get()))) {
                    throw new IllegalStateException("Incomplete original task mutation");
                }

                var2.active.plan.advance(var0.step);
                if (!OwnerFullRepairGuard.watchTaskOrigin(var2.actual.get(), var2)) {
                    throw new IllegalStateException("Task alias watch incomplete");
                }
            } catch (Throwable var7) {
                burn(var2);
            } finally {
                var0.closed = true;
                if (var2.active != null && var2.active.writing == var0) {
                    var2.active.writing = null;
                }
            }
        }
    }

    static boolean permitsWatch(OwnerStatueTaskOrigin.Session var0, Object var1) {
        return caller(OwnerFullRepairGuard.class, "watchTaskOrigin")
            && var0 != null
            && var0.actual.get() == var1
            && var0.phase == OwnerStatueTaskOrigin.Phase.WRITING
            && !var0.rejected
            && var0.active != null
            && var0.active.writing != null;
    }

    static void changed(Object var0, Object var1, int var2) {
        if (var0 instanceof ItemStack var3) {
            OwnerStatueTaskOrigin.Session var4 = session(var3);
            if (var4 != null) {
                if (var4.thread == Thread.currentThread() && var4.phase == OwnerStatueTaskOrigin.Phase.WRITING && var4.active != null) {
                    OwnerStatueTaskOrigin.Write var5 = var4.active.writing;
                    if (var5 != null && !var5.closed && var1 == var5.receiver && (var2 == 20 || var2 == 21)) {
                        boolean var6 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                            .walk(
                                var0x -> var0x.takeWhile(
                                            var0xx -> var0xx.getDeclaringClass() != OwnerStatueTaskWrites.class
                                                    && var0xx.getDeclaringClass() != OwnerStatueWearWrites.class
                                        )
                                        .allMatch(
                                            var0xx -> var0xx.getDeclaringClass().getNestHost() == OwnerStatueTaskOrigin.class
                                                    || var0xx.getDeclaringClass().getNestHost() == OwnerStatueWearAdmission.class
                                                    || var0xx.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                                                    || var0xx.getDeclaringClass() == CompoundTag.class
                                                    || var0xx.getDeclaringClass() == ItemStack.class
                                                    || var0xx.getDeclaringClass() == Item.class
                                        )
                            );
                        if (var6) {
                            return;
                        }
                    }

                    if (var1 == var0 && var4.active.plan.operation == Operation.CLAIM_REPAIR && (var2 == 20 || var2 == 21)) {
                        boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                            .walk(
                                var0x -> var0x.anyMatch(
                                            var0xx -> var0xx.getDeclaringClass() == FoodWeaponLifecycle.class
                                                    && var0xx.getMethodName().equals("setInternalDamage")
                                        )
                                        && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                                            .walk(
                                                var0xx -> var0xx.anyMatch(
                                                        var0xxx -> var0xxx.getDeclaringClass() == FoodWeaponRepair.class
                                                                && var0xxx.getMethodName().equals("claim")
                                                    )
                                            )
                            );
                        if (var7) {
                            return;
                        }
                    }

                    burn(var4);
                } else {
                    burn(var4);
                }
            }
        }
    }

    static void custodyChanged(Object var0) {
        synchronized (LOCK) {
            for (OwnerStatueTaskOrigin.Session var3 : SESSIONS.values()) {
                Inventory var4 = var3.inventory.get();
                if (var4 == null || var0 == var4.items || var0 == var4.armor || var0 == var4.offhand) {
                    burn(var3);
                }
            }
        }
    }

    static void failedWrite(ItemStack var0) {
        burn(session(var0));
    }

    static void burn(OwnerStatueTaskOrigin.Session var0) {
        if (var0 != null) {
            var0.rejected = true;
            var0.phase = OwnerStatueTaskOrigin.Phase.BURNED;
            var0.active = null;
            try {
                if ((Object)var0.progressStorage instanceof NativeTaskProgressArrayFields fields) {
                    fields.ftboss$nativeTaskProgressArrayRejected(var0);
                }
            } finally {
                var0.progressArray = null;
                var0.progressStorage = null;
            }
        }
    }

    static void outerCommitted(OwnerStatueTaskOrigin.Session var0, OwnerStatueTaskService.Prepared var1, StatueRepairMutation.Committed var2) {
        if (var0 != null) {
            OwnerLineageRevocationStore.Lease var3 = null;
            boolean var4 = false;

            try {
                if (!caller(OwnerStatueTaskService.Prepared.class, "afterOuterCommitted")
                    || !var2.take(var1)
                    || !physical(var0)
                    || var0.phase != OwnerStatueTaskOrigin.Phase.WRITING
                    || var0.active == null
                    || var0.active.prepared != var1
                    || var0.active.writing != null
                    || !var0.active.plan.complete()) {
                    throw new IllegalStateException("No original complete task terminal");
                }

                OwnerStatueTaskOrigin.Call var5 = var0.active;
                CompoundTag var6 = var1.originOutcome(var2);
                int[] var7 = var6.getIntArray("ObservedCommittedPayments");
                int[] var8 = var6.getIntArray("PolicyCredit");
                if (var7.length != 3
                    || var8.length != 3
                    || !var0.task.equals(var6.getUUID("Task"))
                    || !var0.call.equals(var6.getUUID("AcceptedWearCall"))
                    || var5.version != var6.getLong("Sequence")
                    || !var5.plan.cursor.equals(var6.getCompound("Expected"))
                    || !var0.rules.equals(var6.getString("Rules"))
                    || !var5.plan.cursor.equals(NativeFoundationItemData.image(var0.actual.get()))) {
                    throw new IllegalStateException("Task terminal differs from private plan");
                }

                for (int var9 = 0; var9 < 3; var9++) {
                    int var10 = var0.payments[var9] + (var5.plan.operation == Operation.SUBMIT_REPAIR && var5.plan.index == var9 ? var5.paid : 0);
                    if (var8[var9] != 0 || var7[var9] != var10) {
                        throw new IllegalStateException("Task policy/real payment ancestry mismatch");
                    }
                }

                if (var5.plan.operation == Operation.CLAIM_REPAIR) {
                    if (var6.getBoolean("Consumed") && uniqueInventoryCopy(var0, var0.actual.get())) {
                        for (int var25 = 0; var25 < 3; var25++) {
                            if (var7[var25] != FoodWeaponLifecycleRules.repairRequired(var0.evolution, var25)) {
                                throw new IllegalStateException("Claim lacks real task payments");
                            }
                        }

                        var0.phase = OwnerStatueTaskOrigin.Phase.CONSUMING;
                        OwnerStatueTaskOrigin.Publication var26 = new OwnerStatueTaskOrigin.Publication(var0, var5, var2);
                        var1.consumeOriginGeneration(var26, false);
                        var3 = var0.store.consumeTaskOrigin(var26);
                        if (var3 != null && var26.live()) {
                            var1.consumeOriginGeneration(var26, true);
                            var0.phase = OwnerStatueTaskOrigin.Phase.STORE_CONSUMED;
                            var4 = OwnerRepairGenerationAuthority.publishTaskOrigin(var26, var3);
                            if (!var4) {
                                throw new IllegalStateException("Task generation alias publication failed");
                            }

                            var0.phase = OwnerStatueTaskOrigin.Phase.PUBLISHED;
                            synchronized (LOCK) {
                                SESSIONS.remove(var0.actual.get());
                                return;
                            }
                        }

                        throw new IllegalStateException("Task generation store consumption failed");
                    }

                    throw new IllegalStateException("Original claim not consumed or duplicated");
                }

                if (var6.getBoolean("Consumed")) {
                    throw new IllegalStateException("Premature task consumption");
                }

                System.arraycopy(var7, 0, var0.payments, 0, 3);
                var0.expected = var5.plan.cursor.copy();
                var0.version = var5.version;
                var0.active = null;
                var0.phase = OwnerStatueTaskOrigin.Phase.IDLE;
            } catch (Throwable var23) {
                var4 = false;
                burn(var0);
                return;
            } finally {
                if (!var4 && var3 != null) {
                    try {
                        OwnerLineageRevocationStore.retireLease(var3);
                    } catch (Throwable var21) {
                    }
                }
            }
        }
    }

    private static final class Call {
        private final OwnerStatueTaskService.Prepared prepared;
        private final StatueRepairMutation outer;
        private final UUID transaction;
        private final long version;
        private final OwnerStatueTaskWritePlan plan;
        private int paid;
        private OwnerStatueTaskOrigin.Write writing;

        private Call(
            OwnerStatueTaskOrigin.Session var1,
            OwnerStatueTaskService.Prepared var2,
            StatueRepairMutation var3,
            Operation var4,
            int var5,
            CompoundTag var6,
            UUID var7
        ) {
            this.prepared = var2;
            this.outer = var3;
            this.transaction = var7;
            this.version = var6.getLong("Sequence");
            this.plan = new OwnerStatueTaskWritePlan(var1.expected, var4, var5, var1.evolution, var1.maximum, var1.profile);
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

    static final class ProgressRead {
        private final OwnerStatueTaskOrigin.Session session;
        private final CompoundTag receiver;
        private final Object storage;
        private final String key;
        private final CompoundTag before;
        private int[] array;

        private ProgressRead(OwnerStatueTaskOrigin.Session var1, CompoundTag var2, Object var3, String var4, CompoundTag var5) {
            this.session = var1;
            this.receiver = var2;
            this.storage = var3;
            this.key = var4;
            this.before = var5;
        }
    }

    static final class Publication {
        private final OwnerStatueTaskOrigin.Session session;
        private final OwnerStatueTaskOrigin.Call source;
        private final StatueRepairMutation.Committed terminal;
        private boolean storeTaken;
        private boolean continuationAttempted;
        private boolean predecessorTaken;
        private OwnerStatueTaskOrigin.PublishedPredecessor predecessor;

        private Publication(OwnerStatueTaskOrigin.Session var1, OwnerStatueTaskOrigin.Call var2, StatueRepairMutation.Committed var3) {
            this.session = var1;
            this.source = var2;
            this.terminal = var3;
        }

        boolean belongs(OwnerStatueTaskService.Prepared var1) {
            return this.source.prepared == var1 && this.terminal.remains(var1) && this.live();
        }

        private boolean live() {
            ItemStack var1 = this.session.actual.get();
            return !this.session.rejected
                && OwnerStatueTaskOrigin.physical(this.session)
                && this.terminal.remains(this.source.prepared)
                && this.source == this.session.active
                && this.source.plan.complete()
                && var1 != null
                && this.source.plan.cursor.equals(NativeFoundationItemData.image(var1))
                && this.session.rules.equals(OwnerStatueTaskPlan.rules(var1));
        }

        CompoundTag takeForStore(OwnerLineageRevocationStore var1) {
            if (OwnerStatueTaskOrigin.caller(OwnerLineageRevocationStore.class, "consumeTaskOrigin")
                && !this.storeTaken
                && var1 == this.session.store
                && this.session.phase == OwnerStatueTaskOrigin.Phase.CONSUMING
                && this.live()) {
                this.storeTaken = true;
                CompoundTag var2 = new CompoundTag();
                var2.putUUID("Owner", this.session.owner);
                var2.putUUID("Weapon", this.session.weapon);
                var2.putUUID("Task", this.session.task);
                var2.putUUID("Claim", this.source.transaction);
                return var2;
            } else {
                throw new IllegalStateException("Detached task generation consumption");
            }
        }

        boolean bindContinuation(Object var1) {
            if (OwnerStatueTaskOrigin.caller(OwnerRepairGenerationAuthority.class, "publishTaskOrigin") && !this.continuationAttempted) {
                this.continuationAttempted = true;
                if (this.storeTaken && this.session.phase == OwnerStatueTaskOrigin.Phase.STORE_CONSUMED && this.live()) {
                    ItemStack var2 = this.session.actual.get();
                    OwnerLineageRevocationStore.Lease var3 = OwnerFullRepairGuard.lease(var2);
                    if (this.candidate(var3) && OwnerRepairGenerationAuthority.current(var2) == var1 && OwnerRepairGenerationAuthority.repaired(var2, var3)) {
                        this.predecessor = new OwnerStatueTaskOrigin.PublishedPredecessor(this, var1, var3);
                        return OwnerRepairContinuation.bind(
                            this, var2, this.session.player.get(), this.session.slot, this.session.rules, this.source.plan.cursor, var1, this.predecessor
                        );
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        boolean takePublishedPredecessor(
            OwnerStatueTaskOrigin.PublishedPredecessor var1,
            ItemStack var2,
            ServerPlayer var3,
            int var4,
            String var5,
            CompoundTag var6,
            Object var7,
            OwnerLineageRevocationStore.Lease var8
        ) {
            if (OwnerStatueTaskOrigin.caller(OwnerRepairContinuation.class, "bind") && !this.predecessorTaken) {
                this.predecessorTaken = true;
                return this.continuationAttempted
                    && var1 != null
                    && var1 == this.predecessor
                    && this.storeTaken
                    && this.session.phase == OwnerStatueTaskOrigin.Phase.STORE_CONSUMED
                    && this.live()
                    && var1.matches(this, var2, var3, var4, var5, var6, var7, var8);
            } else {
                return false;
            }
        }

        OwnerLineageRevocationStore.TaskReservation reservation() {
            return this.session.reservation;
        }

        boolean candidate(OwnerLineageRevocationStore.Lease var1) {
            return var1 != null
                && this.session.reservation != null
                && this.session.owner.equals(var1.owner)
                && this.session.weapon.equals(var1.weapon)
                && this.session.reservation.incarnation.equals(var1.incarnation)
                && var1.generation == 1L;
        }

        ItemStack actualForPublisher() {
            return OwnerStatueTaskOrigin.caller(OwnerRepairGenerationAuthority.class, "publishTaskOrigin")
                    && this.session.phase == OwnerStatueTaskOrigin.Phase.STORE_CONSUMED
                    && this.live()
                ? this.session.actual.get()
                : null;
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

        private PublishedPredecessor(OwnerStatueTaskOrigin.Publication var1, Object var2, OwnerLineageRevocationStore.Lease var3) {
            this.actual = var1.session.actual;
            this.player = var1.session.player;
            this.owner = var1.session.owner;
            this.weapon = var1.session.weapon;
            this.parentTask = var1.session.task;
            this.parentClaim = var1.source.transaction;
            this.originalCall = var1.session.call;
            this.originalLoad = var1.session.load;
            this.taskSequence = var1.source.version;
            this.slot = var1.session.slot;
            this.rules = var1.session.rules;
            this.expected = var1.source.plan.cursor.copy();
            this.generation = var2;
            this.lease = var3;
            this.incarnation = var3.incarnation;
            this.authorityGeneration = var3.generation;
        }

        CompoundTag prewriteBinding(
            ItemStack var1, ServerPlayer var2, int var3, String var4, CompoundTag var5, Object var6, OwnerLineageRevocationStore.Lease var7
        ) {
            if (OwnerStatueTaskOrigin.caller(OwnerRepairContinuation.class, "prewrite")
                && this.actual.get() == var1
                && this.player.get() == var2
                && var2 != null
                && this.owner.equals(var2.getUUID())
                && this.owner.equals(var7.owner)
                && this.weapon.equals(var7.weapon)
                && this.slot == var3
                && this.rules.equals(var4)
                && this.expected.equals(var5)
                && this.generation == var6
                && this.lease == var7
                && this.incarnation.equals(var7.incarnation)
                && this.authorityGeneration == var7.generation
                && OwnerRepairGenerationAuthority.current(var1) == var6
                && OwnerFullRepairGuard.lease(var1) == var7
                && OwnerRepairGenerationAuthority.repaired(var1, var7)) {
                CompoundTag var8 = new CompoundTag();
                var8.putUUID("Owner", this.owner);
                var8.putUUID("Weapon", this.weapon);
                var8.putUUID("ParentTask", this.parentTask);
                var8.putUUID("ParentClaim", this.parentClaim);
                var8.putUUID("OriginalCall", this.originalCall);
                var8.putUUID("OriginalLoad", this.originalLoad);
                var8.putLong("TaskSequence", this.taskSequence);
                var8.putInt("Slot", var3);
                var8.putString("Rules", var4);
                var8.put("Expected", this.expected.copy());
                var8.putUUID("Incarnation", this.incarnation);
                var8.putLong("Generation", this.authorityGeneration);
                return var8;
            } else {
                throw new IllegalStateException("Detached prewrite predecessor");
            }
        }

        private boolean matches(
            OwnerStatueTaskOrigin.Publication var1,
            ItemStack var2,
            ServerPlayer var3,
            int var4,
            String var5,
            CompoundTag var6,
            Object var7,
            OwnerLineageRevocationStore.Lease var8
        ) {
            return this.actual.get() == var2
                && var1.session.actual.get() == var2
                && this.player.get() == var3
                && var1.session.player.get() == var3
                && this.owner.equals(var1.session.owner)
                && this.owner.equals(var3.getUUID())
                && this.owner.equals(var8.owner)
                && this.weapon.equals(var1.session.weapon)
                && this.weapon.equals(var8.weapon)
                && this.parentTask.equals(var1.session.task)
                && this.parentClaim.equals(var1.source.transaction)
                && this.originalCall.equals(var1.session.call)
                && this.originalLoad.equals(var1.session.load)
                && this.taskSequence == var1.source.version
                && this.slot == var4
                && var1.session.slot == var4
                && this.rules.equals(var5)
                && this.rules.equals(var1.session.rules)
                && this.expected.equals(var6)
                && this.expected.equals(var1.source.plan.cursor)
                && this.generation == var7
                && OwnerRepairGenerationAuthority.current(var2) == var7
                && this.lease == var8
                && OwnerFullRepairGuard.lease(var2) == var8
                && this.incarnation.equals(var8.incarnation)
                && this.authorityGeneration == var8.generation
                && OwnerRepairGenerationAuthority.repaired(var2, var8);
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
        private volatile OwnerStatueTaskOrigin.Phase phase = OwnerStatueTaskOrigin.Phase.RESERVING;
        private volatile boolean rejected;
        private OwnerLineageRevocationStore.TaskReservation reservation;
        private CompoundTag expected;
        private long version;
        private OwnerStatueTaskOrigin.Call active;
        private int[] progressArray;
        private IntArrayTag progressStorage;

        private Session(
            CompoundTag var1, CompoundTag var2, OwnerLineageRevocationStore var3, ServerPlayer var4, ItemStack var5, int var6, BlockPos var7, int var8
        ) {
            this.actual = new WeakReference<>(var5);
            this.player = new WeakReference<>(var4);
            this.inventory = new WeakReference<>(var4.getInventory());
            this.server = var4.getServer();
            this.level = var4.level();
            this.owner = var4.getUUID();
            this.weapon = var1.getUUID("Weapon");
            this.task = var2.getUUID("Task");
            this.call = var1.getUUID("Call");
            this.load = var1.getUUID("OriginalLoad");
            this.statue = var7.immutable();
            this.statueState = var4.level().getBlockState(var7);
            this.slot = var6;
            this.tint = var8;
            this.evolution = var1.getInt("Level");
            this.maximum = var1.getInt("Maximum");
            this.profile = var1.getString("Profile");
            this.rules = var1.getString("Rules");
            this.expected = var1.getCompound("Expected").copy();
            this.store = var3;
        }

        CompoundTag reservationBinding(OwnerLineageRevocationStore var1) {
            if (OwnerStatueTaskOrigin.caller(OwnerLineageRevocationStore.class, "reserveTaskOrigin")
                && var1 == this.store
                && this.phase == OwnerStatueTaskOrigin.Phase.RESERVING
                && OwnerStatueTaskOrigin.physical(this)) {
                CompoundTag var2 = new CompoundTag();
                var2.putUUID("Owner", this.owner);
                var2.putUUID("Weapon", this.weapon);
                var2.putUUID("Task", this.task);
                var2.putUUID("OriginalCall", this.call);
                var2.putUUID("OriginalLoad", this.load);
                return var2;
            } else {
                throw new IllegalStateException("Detached task-origin reservation");
            }
        }
    }

    static final class Write {
        private final OwnerStatueTaskOrigin.Session session;
        private final Step step;
        private final Object receiver;
        private final CompoundTag before;
        private boolean closed;
        private boolean arrayVersionAdvanced;

        private Write(OwnerStatueTaskOrigin.Session var1, Step var2, Object var3) {
            this.session = var1;
            this.step = var2;
            this.receiver = var3;
            this.before = var1.active.plan.cursor.copy();
        }
    }
}
