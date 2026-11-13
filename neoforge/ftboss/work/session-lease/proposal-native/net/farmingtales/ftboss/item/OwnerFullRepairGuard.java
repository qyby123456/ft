package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Bytes;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Ints;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Longs;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Sequence;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Stack;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.TextValue;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;

public final class OwnerFullRepairGuard {
    private static final int ALIAS_LIMIT = 65536;

    private static boolean tickCaller() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> var0.filter(var0x -> var0x.getDeclaringClass().getNestHost() != OwnerFullRepairGuard.class)
                        .findFirst()
                        .map(var0x -> var0x.getDeclaringClass() == ItemStack.class && var0x.getMethodName().contains("ftboss$ownerInventoryTickCallback"))
                        .orElse(false)
            );
    }

    public static OwnerFullRepairGuard.TickCut inventoryTickEntered(Object var0, Object var1) {
        OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();
        Throwable var3 = null;

        Object var8;
        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (!tickCaller()) {
                    return null;
                }

                OwnerFullRepairGuard.Node var5 = node(var0, false);
                if (var5 == null || var5.actual == null || var5.actual.denied) {
                    return null;
                }

                OwnerFullRepairGuard.Record var6 = var5.actual;

                try {
                    OwnerFullRepairGuard.StorageView var7 = new OwnerFullRepairGuard.StorageView(var0, true);
                    if (var7.item != var1) {
                        deny(var2, var6, 42);
                        return null;
                    }

                    for (Object var9 : var7.targets) {
                        associate(var2, var6, var9, false);
                    }

                    if (var6.denied || var5.actual != var6) {
                        return null;
                    }

                    return new OwnerFullRepairGuard.TickCut(var0, var5, var7);
                } catch (Throwable var25) {
                    deny(var2, var6, 42);
                    var8 = null;
                }
            }
        } catch (Error | RuntimeException var27) {
            var3 = var27;
            throw var27;
        } finally {
            try {
                var2.flush();
            } catch (Error | RuntimeException var24) {
                if (var3 == null) {
                    throw var24;
                }
            }
        }

        return (OwnerFullRepairGuard.TickCut)var8;
    }

    public static void inventoryTickReturned(OwnerFullRepairGuard.TickCut var0, Throwable var1) {
        OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();
        Throwable var3 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (tickCaller() && var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
                    var0.closed = true;
                    var0.originalFailure = var1;
                    OwnerFullRepairGuard.Node var5 = node(var0.actual, false);
                    if (var5 != null && var5.actual == var0.record && var5.generation == var0.generation && var5.lease == var0.lease) {
                        if (!var0.record.denied && var0.record.events == var0.events) {
                            try {
                                if (!var0.before.same(new OwnerFullRepairGuard.StorageView(var0.actual, true))) {
                                    deny(var2, var0.record, 42);
                                    return;
                                }
                            } catch (Throwable var19) {
                                var0.observerFailure = var19;
                                deny(var2, var0.record, 42);
                            }
                        }
                    }
                }
            }
        } catch (Error | RuntimeException var22) {
            var3 = var22;
            throw var22;
        } finally {
            try {
                var2.flush();
            } catch (Error | RuntimeException var20) {
                if (var3 == null) {
                    throw var20;
                }
            }
        }
    }

    private OwnerFullRepairGuard() {
    }

    static boolean detachedLocalNode(Object var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L).findFirst().map(var0xx -> var0xx.getDeclaringClass().getNestHost() == OwnerTimerSemanticAuthority.class).orElse(false)
            );
        if (var1 && var0 instanceof OwnerRevocationNodeAccess) {
            OwnerFullRepairGuard.Node var2 = node(var0, false);
            if (var2 == null) {
                return true;
            } else if (var2.actual == null && var2.fieldDomain == null && !var2.retired) {
                for (WeakReference var6 : var2.owners) {
                    if (var6 != null && var6.get() != null) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static OwnerFullRepairGuard.Node node(Object var0, boolean var1) {
        if (var0 instanceof OwnerRevocationNodeAccess var2) {
            OwnerFullRepairGuard.Node var3 = var2.ftboss$ownerRevocationNode();
            if (var3 == null && var1) {
                OwnerFullRepairGuard.Node var4 = new OwnerFullRepairGuard.Node();
                var2.ftboss$ownerInstallRevocationNode(var4);
                var3 = var2.ftboss$ownerRevocationNode();
                if (var3 != var4) {
                    return null;
                }
            }

            return var3;
        } else {
            return null;
        }
    }

    private static OwnerFullRepairGuard.Record record(Object var0, boolean var1) {
        OwnerFullRepairGuard.Node var2 = node(var0, var1);
        if (var2 == null) {
            return null;
        } else {
            if (var2.actual == null && var1) {
                var2.actual = new OwnerFullRepairGuard.Record(var0);
            }

            return var2.actual;
        }
    }

    static boolean watchTaskOrigin(ItemStack var0, OwnerStatueTaskOrigin.Session var1) {
        if (!OwnerStatueTaskOrigin.permitsWatch(var1, var0)) {
            return false;
        } else {
            OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();

            boolean var18;
            try {
                synchronized (OwnerFullRepairGuard.class) {
                    OwnerFullRepairGuard.Record var16 = record(var0, false);
                    if (var16 == null || lease(var0) != null || generation(var0) != null) {
                        return false;
                    }

                    OwnerFullRepairGuard.StorageView var5 = new OwnerFullRepairGuard.StorageView(var0, false);

                    for (Object var7 : var5.targets) {
                        associate(var2, var16, var7, false);
                    }

                    var18 = record(var0, false) == var16 && OwnerStatueTaskOrigin.permitsWatch(var1, var0);
                }
            } catch (Throwable var14) {
                return false;
            } finally {
                var2.flush();
            }

            return var18;
        }
    }

    static boolean watchWearPrefix(ItemStack var0) {
        if (!OwnerStatueWearAdmission.watching(var0)) {
            return false;
        } else {
            OwnerFullRepairGuard.Mutation var1 = new OwnerFullRepairGuard.Mutation();

            boolean var4;
            try {
                synchronized (OwnerFullRepairGuard.class) {
                    OwnerFullRepairGuard.Record var15 = record(var0, false);
                    if (var15 != null) {
                        OwnerFullRepairGuard.StorageView var16 = new OwnerFullRepairGuard.StorageView(var0, false);

                        for (Object var6 : var16.targets) {
                            associate(var1, var15, var6, false);
                        }

                        return record(var0, false) == var15 && OwnerStatueWearAdmission.watching(var0);
                    }

                    var4 = false;
                }
            } catch (Throwable var13) {
                return false;
            } finally {
                var1.flush();
            }

            return var4;
        }
    }

    static boolean watchLoadedWear(Object var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0xx.getMethodName().equals("wearInputLoaded"))
                        .orElse(false)
            );
        OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var1
                    && var0 instanceof OwnerRepairStackFields var4
                    && !var4.ftboss$ownerRepairWasRetired()
                    && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var0)) {
                    OwnerFullRepairGuard.Node var18 = node(var0, true);
                    if (var18 == null
                        || var18.retired
                        || var18.untrackedEscape
                        || var18.actual != null
                        || var18.generation != null
                        || var18.lease != null
                        || var18.intent != null) {
                        return false;
                    }

                    OwnerFullRepairGuard.Record var6 = record(var0, true);
                    if (var6 == null) {
                        return false;
                    }

                    try {
                        OwnerFullRepairGuard.StorageView var7 = new OwnerFullRepairGuard.StorageView(var0, false);

                        for (Object var9 : var7.targets) {
                            associate(var2, var6, var9, false);
                        }

                        return !var6.denied && var18.actual == var6;
                    } catch (Throwable var15) {
                        deny(var2, var6, 43);
                        return false;
                    }
                }

                return false;
            }
        } finally {
            var2.flush();
        }
    }

    static synchronized Object generation(Object var0) {
        OwnerFullRepairGuard.Node var1 = node(var0, false);
        return var1 == null ? null : var1.generation;
    }

    static synchronized void generation(Object var0, Object var1) {
        OwnerFullRepairGuard.Node var2 = node(var0, true);
        if (var2 != null) {
            var2.generation = var1;
        }
    }

    static synchronized OwnerRepairGenerationAuthority.Intent intent(Object var0) {
        OwnerFullRepairGuard.Node var1 = node(var0, false);
        return var1 == null ? null : var1.intent;
    }

    static synchronized void intent(Object var0, OwnerRepairGenerationAuthority.Intent var1) {
        OwnerFullRepairGuard.Node var2 = node(var0, true);
        if (var2 != null) {
            var2.intent = var1;
        }
    }

    static synchronized OwnerLineageRevocationStore.Lease lease(Object var0) {
        OwnerFullRepairGuard.Node var1 = node(var0, false);
        return var1 == null ? null : var1.lease;
    }

    static synchronized void lease(Object var0, OwnerLineageRevocationStore.Lease var1) {
        OwnerFullRepairGuard.Node var2 = node(var0, true);
        if (var2 != null && (var2.lease == null || var2.lease == var1)) {
            var2.lease = var1;
        }
    }

    static synchronized OwnerFieldExpectedAuthority.DomainBefore fieldDomain(Object var0) {
        OwnerFullRepairGuard.Node var1 = node(var0, false);
        return var1 == null ? null : var1.fieldDomain;
    }

    static synchronized void fieldDomain(Object var0, OwnerFieldExpectedAuthority.DomainBefore var1) {
        OwnerFullRepairGuard.Node var2 = node(var0, true);
        if (var2 != null) {
            if (var2.fieldDomain != null && var2.fieldDomain != var1) {
                OwnerFieldExpectedAuthority.changed(var0);
            }

            var2.fieldDomain = var1;
        }
    }

    static synchronized void freshGeneration(Object var0, OwnerLineageRevocationStore.Lease var1, Object var2) {
        OwnerFullRepairGuard.Node var3 = node(var0, true);
        if (var3 != null && var1 != null && var2 != null) {
            try {
                OwnerFullRepairGuard.StorageView var4 = new OwnerFullRepairGuard.StorageView(var0, true);
                OwnerFullRepairGuard.Record var5 = new OwnerFullRepairGuard.Record(var0);
                ArrayList<OwnerFullRepairGuard.AliasHandoff> var6 = new ArrayList<>();

                for (Object var8 : var4.targets) {
                    OwnerFullRepairGuard.Node var9 = node(var8, true);
                    if (var9 == null || var5.aliases.size() >= 65536) {
                        return;
                    }

                    int ownerSlot = availableOwnerSlot(var9, var0);
                    if (ownerSlot < 0) return;
                    var5.aliases.add(new WeakReference<>(var8));
                    var6.add(new OwnerFullRepairGuard.AliasHandoff(var9, ownerSlot));
                }

                WeakReference var14 = new WeakReference<>(var5);

                for (OwnerFullRepairGuard.AliasHandoff var16 : var6) {
                    var16.node.owners[var16.slot] = var14;
                }

                if (var3.actual != null) {
                    var3.actual.denied = true;
                }

                var3.actual = var5;
                var3.lease = var1;
                var3.generation = var2;
            } catch (Throwable var13) {
                net.farmingtales.ftboss.FTBossMod.LOGGER.error("Original repair physical generation capture failed", var13);
            }
        }
    }

    static synchronized boolean nativeOperationReady(ItemStack stack) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeOperationEntered")) return false;
        Record record = record(stack, false);
        if (record == null || record.denied) return false;
        try {
            new StorageView(stack, false);
            return record(stack, false) == record && !record.denied;
        } catch (RuntimeException | LinkageError rejected) {
            return false;
        }
    }

    static synchronized boolean refreshNativeOperation(ItemStack stack, OwnerLineageRevocationStore.Lease lease, Object generation) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class
            || !(caller.getMethodName().equals("nativeOperationReturned") || caller.getMethodName().equals("nativeCrossbowComponentReturned")
                && OwnerNativeSemanticAuthority.nativeCrossbowComponentReady(stack, lease, generation)
                || caller.getMethodName().equals("nativeTridentFactoryReturned") && OwnerNativeSemanticAuthority.nativeTridentFactoryReady(stack, lease, generation))
            || !OwnerRepairGenerationAuthority.identityMatches(stack, generation, lease)) return false;
        Record before = record(stack, false);
        freshGeneration(stack, lease, generation);
        return record(stack, false) != before && record(stack, false) != null && generation(stack) == generation && lease(stack) == lease;
    }

    static synchronized boolean refreshNativeSemantic(ItemStack stack, OwnerLineageRevocationStore.Lease lease, Object generation) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        Record before = record(stack, false);
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("advance")
            || before == null || before.denied || !OwnerRepairGenerationAuthority.matches(stack, generation)
            || !OwnerRepairGenerationAuthority.identityMatches(stack, generation, lease)) return false;
        freshGeneration(stack, lease, generation);
        return record(stack, false) != before && record(stack, false) != null && !record(stack, false).denied
            && generation(stack) == generation && lease(stack) == lease;
    }

    static synchronized boolean refreshNativeMelee(ItemStack stack, OwnerLineageRevocationStore.Lease lease, Object generation) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeMeleeItemReturned")
            || !OwnerNativeSemanticAuthority.nativeMeleeTerminalReady(stack, lease, generation)
            || !OwnerRepairGenerationAuthority.identityMatches(stack, generation, lease)) return false;
        Record before = record(stack, false);
        if (before == null || before.denied) return false;
        freshGeneration(stack, lease, generation);
        return record(stack, false) != before && record(stack, false) != null && !record(stack, false).denied
            && generation(stack) == generation && lease(stack) == lease;
    }

    static synchronized boolean nativeMeleeOperationReady(ItemStack stack) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeMeleeItemBefore")) return false;
        Record before = record(stack, false);
        if (before == null || before.denied) return false;
        try {
            new StorageView(stack, false);
            return record(stack, false) == before && !before.denied;
        } catch (RuntimeException | LinkageError rejected) {
            return false;
        }
    }

    private static void deny(OwnerFullRepairGuard.Mutation var0, OwnerFullRepairGuard.Record var1, int var2) {
        if (var1 != null) {
            if (var2 != 20 && var2 != 21) {
                try {
                    OwnerStatueWearAdmission.mutationNoticed(var1.actual.get(), null, var2);
                } catch (Throwable var4) {
                }
            }

            var1.denied = true;
            if (record(var1.actual.get(), false) == var1) {
                OwnerNativeSemanticAuthority.changed(var1.actual.get());
            }

            if (var1.events != Long.MAX_VALUE) {
                var1.events++;
            }

            var1.lastKind = var2;
            if (var2 >= 8 && record(var1.actual.get(), false) == var1) {
                var0.add(OwnerRepairGenerationAuthority.invalidate(var1.actual.get()));
            }
        }
    }

    static boolean prewriteLocalDenialFree(Object var0, Object var1, OwnerLineageRevocationStore.Lease var2) {
        if (!Thread.holdsLock(OwnerFullRepairGuard.class)) {
            return false;
        } else {
            boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> var0x.skip(1L)
                            .findFirst()
                            .map(var0xx -> var0xx.getDeclaringClass() == OwnerRepairContinuation.class && var0xx.getMethodName().equals("prewriteLocal"))
                            .orElse(false)
                );
            if (!var3) {
                return false;
            } else {
                OwnerFullRepairGuard.Node var4 = node(var0, false);
                return var4 != null
                    && var1 != null
                    && var2 != null
                    && var4.generation == var1
                    && var4.lease == var2
                    && var4.actual != null
                    && var4.actual.actual.get() == var0
                    && !var4.actual.denied;
            }
        }
    }

    public static boolean permits(Object var0) {
        if (!(var0 instanceof OwnerRepairStackFields var1)) {
            return false;
        } else if (OwnerRepairGenerationAuthority.fresh(var0)) {
            return true;
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerFullRepairGuard.Record var3 = record(var0, false);
                OwnerFullRepairGuard.Node var4 = node(var0, false);
                return !var1.ftboss$ownerRepairWasRetired() && (var4 == null || !var4.retired) && (var3 == null || !var3.denied);
            }
        }
    }

    public static boolean permits(UUID var0) {
        return OwnerRepairGenerationAuthority.currentSnapshotWeapon(var0);
    }

    public static boolean permits(UUID var0, CompoundTag var1, ServerPlayer var2) {
        return OwnerRepairGenerationAuthority.currentSnapshotWeapon(var0, var1, var2);
    }

    public static boolean bind(Object var0, UUID var1) {
        OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();
        Throwable var3 = null;

        boolean var9;
        try {
            if (var0 == null || var1 == null || !permits(var0)) {
                return false;
            }

            OwnerLineageRevocationStore.Lease var4;
            OwnerFullRepairGuard.Record var5;
            synchronized (OwnerFullRepairGuard.class) {
                var4 = lease(var0);
                var5 = record(var0, true);
                if (var5 == null || var4 == null || !var4.weapon.equals(var1)) {
                    return false;
                }
            }

            if (!OwnerLineageRevocationStore.liveLease(var4)) {
                return false;
            }

            synchronized (OwnerFullRepairGuard.class) {
                OwnerFullRepairGuard.Node var7 = node(var0, false);
                if (record(var0, false) != var5
                    || lease(var0) != var4
                    || var5.denied
                    || !OwnerRepairGenerationAuthority.repaired(var0, var4)
                        && (!(var0 instanceof OwnerRepairStackFields var8) || var8.ftboss$ownerRepairWasRetired() || var7 == null || var7.retired)) {
                    return false;
                }

                try {
                    OwnerFullRepairGuard.StorageView var36 = new OwnerFullRepairGuard.StorageView(var0, false);

                    for (Object var10 : var36.targets) {
                        associate(var2, var5, var10, false);
                    }

                    return record(var0, false) == var5 && !var5.denied;
                } catch (Throwable var28) {
                    deny(var2, var5, 43);
                    var9 = false;
                }
            }
        } catch (Error | RuntimeException var30) {
            var3 = var30;
            throw var30;
        } finally {
            try {
                var2.flush();
            } catch (Error | RuntimeException var26) {
                if (var3 == null) {
                    throw var26;
                }
            }
        }

        return var9;
    }

    public static OwnerFullRepairGuard.Cut enter(Object var0, UUID var1, int var2) {
        OwnerFullRepairGuard.Mutation var3 = new OwnerFullRepairGuard.Mutation();
        Throwable var4 = null;

        Object var11;
        try {
            OwnerRepairGenerationAuthority.Intent var5;
            OwnerFullRepairGuard.Node var6;
            OwnerFullRepairGuard.Record var7;
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 instanceof OwnerRepairStackFields var9) {
                    var9.ftboss$ownerRepairRetire();
                }

                var5 = intent(var0);
                var6 = node(var0, false);
                var7 = record(var0, false);
            }

            if (var5 != null) {
                OwnerRepairGenerationAuthority.entered(var5, var0);
            } else {
                var3.add(OwnerRepairGenerationAuthority.invalidate(var0));
            }

            synchronized (OwnerFullRepairGuard.class) {
                if (node(var0, false) != var6 || record(var0, false) != var7) {
                    if (var5 != null) {
                        OwnerRepairGenerationAuthority.failed(var5, null);
                    }

                    return null;
                }

                OwnerFullRepairGuard.Record var33 = null;

                try {
                    var33 = record(var0, true);
                    if (var33 == null) {
                        return null;
                    }

                    deny(var3, var33, 1);
                    OwnerFullRepairGuard.Cut nativeCut = new OwnerFullRepairGuard.Cut(var33, var0, var2);
                    NativePreQueryCapabilityLifetime199.watch(var0,nativeCut);
                    return nativeCut;
                } catch (Throwable var28) {
                    deny(var3, var33, 11);
                    var11 = null;
                }
            }
        } catch (Error | RuntimeException var30) {
            var4 = var30;
            throw var30;
        } finally {
            try {
                var3.flush();
            } catch (Error | RuntimeException var26) {
                if (var4 == null) {
                    throw var26;
                }
            }
        }

        return (OwnerFullRepairGuard.Cut)var11;
    }

    public static synchronized void snapshotCopied(Object var0, Object var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == CompoundTag.class && var0xx.getMethodName().contains("ftboss$ownerSnapshotCopy"))
                        .orElse(false)
            );
        if (var2 && var0 instanceof CompoundTag && var1 instanceof CompoundTag && var0 != var1) {
            OwnerLineageRevocationStore.Lease var3 = lease(var0);
            if (var3 != null) {
                lease(var1, var3);
            }
        }
    }

    private static boolean ledgerCaller() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != OwnerFullRepairGuard.class)
                        .findFirst()
                        .map(var0x -> var0x.getDeclaringClass() == FoodWeaponSourceLedger.class)
                        .orElse(false)
            );
    }

    static synchronized void planned(OwnerFullRepairGuard.Cut var0, OwnerRepairGenerationAuthority.Intent var1) {
        if (ledgerCaller() && var0 != null && !var0.closed && var0.thread == Thread.currentThread() && var0.repairIntent == null && var1 != null) {
            if (intent(var0.actual) == var1) {
                var0.repairIntent = var1;
            }
        }
    }

    static void fieldsObserved(OwnerFullRepairGuard.Cut var0, Object var1, int var2, byte[] var3) {
        OwnerRepairGenerationAuthority.Intent var4;
        synchronized (OwnerFullRepairGuard.class) {
            if (!ledgerCaller()
                || var0 == null
                || var0.closed
                || var0.thread != Thread.currentThread()
                || var0.actual != var1
                || var0.repairIntent == null
                || var2 == 0 && (!var0.readReturned || var0.setterEntered)
                || var2 == 1 && (!var0.setterEntered || var0.setterReturned)
                || var2 < 0
                || var2 > 1) {
                return;
            }

            var4 = var0.repairIntent;
        }

        OwnerRepairGenerationAuthority.observed(var4, var1, var2, var3);
    }

    static void fieldsValueObserved(OwnerFullRepairGuard.Cut var0, Object var1, int var2, Value var3) {
        OwnerRepairGenerationAuthority.Intent var4;
        synchronized (OwnerFullRepairGuard.class) {
            if (!ledgerCaller()
                || var0 == null
                || var0.closed
                || var0.thread != Thread.currentThread()
                || var0.actual != var1
                || var0.repairIntent == null
                || var2 == 0 && (!var0.readReturned || var0.setterEntered)
                || var2 == 1 && (!var0.setterEntered || var0.setterReturned)
                || var2 < 0
                || var2 > 1) {
                return;
            }

            var4 = var0.repairIntent;
        }

        OwnerRepairGenerationAuthority.observedValue(var4, var1, var2, var3);
    }

    public static void readReturned(OwnerFullRepairGuard.Cut var0, Object var1, int var2, int var3) {
        OwnerFullRepairGuard.Mutation var4 = new OwnerFullRepairGuard.Mutation();
        Throwable var5 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
                    deny(var4, var0.record, 2);
                    if (var0.actual == var1 && !var0.readReturned && var3 == Math.max(0, var2 - var0.reduction)) {
                        var0.damage = var2;
                        var0.requested = var3;
                        var0.readReturned = true;
                    } else {
                        var0.closed = true;
                    }
                }
            }
        } catch (Error | RuntimeException var20) {
            var5 = var20;
            throw var20;
        } finally {
            try {
                var4.flush();
            } catch (Error | RuntimeException var18) {
                if (var5 == null) {
                    throw var18;
                }
            }
        }
    }

    public static void setterEntered(OwnerFullRepairGuard.Cut var0, Object var1, int var2) {
        OwnerFullRepairGuard.Mutation var3 = new OwnerFullRepairGuard.Mutation();
        Throwable var4 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
                    deny(var3, var0.record, 3);
                    if (var0.actual == var1 && var0.readReturned && !var0.setterEntered && var0.requested == var2) {
                        var0.setterEntered = true;
                    } else {
                        var0.closed = true;
                    }
                }
            }
        } catch (Error | RuntimeException var19) {
            var4 = var19;
            throw var19;
        } finally {
            try {
                var3.flush();
            } catch (Error | RuntimeException var17) {
                if (var4 == null) {
                    throw var17;
                }
            }
        }
    }

    public static void returned(OwnerFullRepairGuard.Cut var0) {
        OwnerFullRepairGuard.Mutation var1 = new OwnerFullRepairGuard.Mutation();
        Throwable var2 = null;

        try {
            OwnerRepairGenerationAuthority.Intent var3;
            boolean var4;
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 == null || var0.closed || var0.thread != Thread.currentThread()) {
                    return;
                }

                deny(var1, var0.record, 4);
                var0.setterReturned = var0.setterEntered;
                var4 = var0.setterReturned;
                var3 = var0.repairIntent;
            }

            try {
                if (var3 != null) {
                    OwnerRepairGenerationAuthority.completed(var3, var0.actual, var4);
                }
            } finally {
                synchronized (OwnerFullRepairGuard.class) {
                    var0.closed = true;
                }
            }
        } catch (Error | RuntimeException var34) {
            var2 = var34;
            throw var34;
        } finally {
            try {
                var1.flush();
            } catch (Error | RuntimeException var31) {
                if (var2 == null) {
                    throw var31;
                }
            }
        }
    }

    public static void failed(OwnerFullRepairGuard.Cut var0, Throwable var1, boolean var2) {
        OwnerFullRepairGuard.Mutation var3 = new OwnerFullRepairGuard.Mutation();
        Throwable var4 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && var0.thread == Thread.currentThread()) {
                    deny(var3, var0.record, var2 ? 6 : 5);
                    if (var0.repairIntent != null) {
                        OwnerRepairGenerationAuthority.failed(var0.repairIntent, var1);
                    }

                    if (var2) {
                        if (var0.observerFailure == null) {
                            var0.observerFailure = var1;
                        }
                    } else if (var0.originalFailure == null) {
                        var0.originalFailure = var1;
                    }

                    var0.closed = true;
                }
            }
        } catch (Error | RuntimeException var18) {
            var4 = var18;
            throw var18;
        } finally {
            try {
                var3.flush();
            } catch (Error | RuntimeException var16) {
                if (var4 == null) {
                    throw var16;
                }
            }
        }
    }

    public static synchronized Throwable failure(OwnerFullRepairGuard.Cut var0, boolean var1) {
        return var0 != null && var0.thread == Thread.currentThread() ? (var1 ? var0.observerFailure : var0.originalFailure) : null;
    }

    public static void close(OwnerFullRepairGuard.Cut var0) {
        OwnerFullRepairGuard.Mutation var1 = new OwnerFullRepairGuard.Mutation();
        Throwable var2 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && var0.thread == Thread.currentThread()) {
                    if (!var0.closed && var0.repairIntent != null) {
                        OwnerRepairGenerationAuthority.failed(var0.repairIntent, null);
                    }

                    deny(var1, var0.record, 7);
                    var0.closed = true;
                }
            }
        } catch (Error | RuntimeException var16) {
            var2 = var16;
            throw var16;
        } finally {
            try {
                var1.flush();
            } catch (Error | RuntimeException var14) {
                if (var2 == null) {
                    throw var14;
                }
            }
        }
    }

    static boolean immutableValue(Object var0) {
        if (var0 == null) {
            return true;
        } else {
            Class var1 = var0.getClass();
            return var1 == ByteTag.class
                || var1 == ShortTag.class
                || var1 == IntTag.class
                || var1 == LongTag.class
                || var1 == FloatTag.class
                || var1 == DoubleTag.class
                || var1 == StringTag.class
                || var1 == EndTag.class;
        }
    }

    private static int availableOwnerSlot(OwnerFullRepairGuard.Node node, Object actual) {
        if (!Thread.holdsLock(OwnerFullRepairGuard.class)) throw new IllegalStateException("Original repair owner monitor required");
        for (int index = 0; index < node.owners.length; index++) {
            OwnerFullRepairGuard.Record owner = node.owners[index] == null ? null : node.owners[index].get();
            if (owner == null || owner.actual.get() == actual || record(owner.actual.get(), false) != owner) return index;
        }
        int previousSize = node.owners.length;
        if (previousSize >= 65536) return -1;
        node.owners = java.util.Arrays.copyOf(node.owners, Math.min(65536, previousSize * 2));
        return previousSize;
    }

    private static void associate(OwnerFullRepairGuard.Mutation var0, OwnerFullRepairGuard.Record var1, Object var2, boolean var3) {
        if (!immutableValue(var2)) {
            if (var1 != null && var2 != null && record(var1.actual.get(), false) == var1 && !var1.contains(var2)) {
                OwnerFullRepairGuard.Node var4 = node(var2, true);
                if (var4 != null && var1.aliases.size() < 65536) {
                    int var5 = -1;

                    for (int var6 = 0; var6 < var4.owners.length; var6++) {
                        OwnerFullRepairGuard.Record var7 = var4.owners[var6] == null ? null : var4.owners[var6].get();
                        if (var7 == var1) {
                            return;
                        }

                        if ((var7 == null || record(var7.actual.get(), false) != var7) && var5 < 0) {
                            var5 = var6;
                        }
                    }

                    if (var5 < 0) var5 = availableOwnerSlot(var4, var1.actual.get());
                    if (var5 < 0) {
                        deny(var0, var1, 8);
                    } else {
                        WeakReference var8 = new WeakReference<>(var1);
                        WeakReference var9 = new WeakReference<>(var2);
                        var1.aliases.add(var9);
                        var4.owners[var5] = var8;
                    }
                } else {
                    deny(var0, var1, 8);
                }
            }
        }
    }

    public static void alias(OwnerFullRepairGuard.Cut var0, Object var1, Object var2, boolean var3) {
        OwnerFullRepairGuard.Mutation var4 = new OwnerFullRepairGuard.Mutation();
        Throwable var5 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && var0.thread == Thread.currentThread() && var0.record.contains(var1)) {
                    try {
                        associate(var4, var0.record, var2, var3);
                    } catch (Throwable var18) {
                        deny(var4, var0.record, 11);
                    }
                }
            }
        } catch (Error | RuntimeException var21) {
            var5 = var21;
            throw var21;
        } finally {
            try {
                var4.flush();
            } catch (Error | RuntimeException var19) {
                if (var5 == null) {
                    throw var19;
                }
            }
        }
    }

    public static void changed(OwnerFullRepairGuard.Cut var0, Object var1, int var2) {
        OwnerFullRepairGuard.Mutation var3 = new OwnerFullRepairGuard.Mutation();
        Throwable var4 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 != null && var0.thread == Thread.currentThread() && var0.record.contains(var1)) {
                    if (record(var0.actual, false) == var0.record) {
                        var3.add(OwnerRepairGenerationAuthority.invalidate(var0.actual));
                    }

                    try {
                        OwnerStatueWearAdmission.mutationNoticed(var0.actual, var1, var2);
                    } catch (Throwable var17) {
                    }

                    NativeFoodWeaponWear.nativeRepairChanged(var0.actual, var1, var2);
                    deny(var3, var0.record, var2);
                }
            }
        } catch (Error | RuntimeException var20) {
            var4 = var20;
            throw var20;
        } finally {
            try {
                var3.flush();
            } catch (Error | RuntimeException var18) {
                if (var4 == null) {
                    throw var18;
                }
            }
        }
    }

    public static void alias(Object var0, Object var1, boolean var2) {
        OwnerFullRepairGuard.Mutation var3 = new OwnerFullRepairGuard.Mutation();
        Throwable var4 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerFieldExpectedAuthority.alias(var0, var1, var2);
                if (!OwnerStatueTaskOrigin.boundedProgressAlias(var0, var1)) {
                    if (!OwnerRepairContinuation.identityAlias(var0, var1)) {
                        if (var1 != null && !immutableValue(var1)) {
                            OwnerFullRepairGuard.Node var6 = node(var0, true);
                            if (var6 != null) {
                                if (var2 && !(var1 instanceof OwnerRevocationNodeAccess)) {
                                    if (!var6.untrackedEscape && var0 instanceof ItemStack stack && NativeFoodWeaponWear.eligible(stack)) {
                                        net.farmingtales.ftboss.FTBossMod.LOGGER.error("Original native weapon raw alias escaped to {}", var1.getClass().getName(), new IllegalStateException("First native weapon raw alias"));
                                    }
                                    var6.untrackedEscape = true;
                                }

                                if (var1 instanceof OwnerRevocationNodeAccess) {
                                    node(var1, true);
                                }

                                if (var6.actual != null) {
                                    associate(var3, var6.actual, var1, var2);
                                }

                                for (WeakReference var10 : var6.owners) {
                                    OwnerFullRepairGuard.Record var11 = var10 == null ? null : (OwnerFullRepairGuard.Record)var10.get();
                                    if (var11 != null) {
                                        associate(var3, var11, var1, var2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Error | RuntimeException var26) {
            var4 = var26;
            throw var26;
        } finally {
            try {
                var3.flush();
            } catch (Error | RuntimeException var24) {
                if (var4 == null) {
                    throw var24;
                }
            }
        }
    }

    public static void tagReadOrCreateEntered(Object var0, String var1) {
        boolean var2;
        synchronized (OwnerFullRepairGuard.class) {
            if (!(var0 instanceof Stack var4)) {
                var2 = true;
            } else {
                CompoundTag var5 = var4.ftboss$rawStackTag();
                var2 = var5 == null
                    || var1 != null && (!(var5 instanceof Compound var6) || !(var6.ftboss$rawCompoundFields().get(var1) instanceof CompoundTag));
            }
        }

        if (var2) {
            changed(var0, 42);
        }
    }

    public static void changed(Object var0, int var1) {
        OwnerFullRepairGuard.Mutation var2 = new OwnerFullRepairGuard.Mutation();
        Throwable var3 = null;

        try {
            synchronized (OwnerFullRepairGuard.class) {
                if (var0 == null) {
                    return;
                }

                if (OwnerNativeSemanticAuthority.plannedPassiveRootMutation(var0, var1)) {
                    return;
                }

                if (OwnerNativeSemanticAuthority.nativeOperationMutation(var0, var1)) {
                    return;
                }

                if (NativeExpectedFieldObservation.plannedNativeTimerMutation(var0, var1)) {
                    return;
                }

                if (OwnerRepairContinuation.deferEntry(var0, var1)) {
                    return;
                }

                if (!NativeExpectedFieldObservation.plannedRangedFieldMutation(var0, var1)) {
                    OwnerFieldExpectedAuthority.changed(var0);
                }
                OwnerFullRepairGuard.Node var5 = node(var0, var1 == 40);
                if (var5 != null) {
                    if (var1 == 40) {
                        var5.untrackedEscape = true;
                    }

                    if (var5.actual != null) {
                        try {
                            OwnerStatueWearAdmission.mutationNoticed(var5.actual.actual.get(), var0, var1);
                        } catch (Throwable var26) {
                        }

                        NativeFoodWeaponWear.nativeRepairChanged(var5.actual.actual.get(), var0, var1);
                        var2.add(OwnerRepairGenerationAuthority.invalidate(var5.actual.actual.get()));
                        deny(var2, var5.actual, var1);
                    }

                    for (WeakReference var9 : var5.owners) {
                        OwnerFullRepairGuard.Record var10 = var9 == null ? null : (OwnerFullRepairGuard.Record)var9.get();
                        if (var10 != null && record(var10.actual.get(), false) == var10) {
                            try {
                                OwnerStatueWearAdmission.mutationNoticed(var10.actual.get(), var0, var1);
                            } catch (Throwable var25) {
                            }

                            NativeFoodWeaponWear.nativeRepairChanged(var10.actual.get(), var0, var1);
                            var2.add(OwnerRepairGenerationAuthority.invalidate(var10.actual.get()));
                            deny(var2, var10, var1);
                        }
                    }

                    return;
                }
            }
        } catch (Error | RuntimeException var29) {
            var3 = var29;
            throw var29;
        } finally {
            try {
                var2.flush();
            } catch (Error | RuntimeException var27) {
                if (var3 == null) {
                    throw var27;
                }
            }
        }
    }

    public static synchronized boolean nativePreQueryWatchLive(Object actual,OwnerFullRepairGuard.Cut cut) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativePreQueryCapabilityLifetime199.class
            || !(caller.getMethodName().equals("resolved") || caller.getMethodName().equals("compact"))) {
            throw new SecurityException("Actual native lifetime observer required");
        }
        return actual != null && cut != null && cut.actual == actual && !cut.closed && record(actual,false) == cut.record;
    }

    private static record AliasHandoff(OwnerFullRepairGuard.Node node, int slot) {
    }

    public static final class Cut {
        private final OwnerFullRepairGuard.Record record;
        private final Object actual;
        private final Thread thread = Thread.currentThread();
        private final int reduction;
        private int damage;
        private int requested;
        private boolean readReturned;
        private boolean setterEntered;
        private boolean setterReturned;
        private Throwable originalFailure;
        private Throwable observerFailure;
        private boolean closed;
        private OwnerRepairGenerationAuthority.Intent repairIntent;

        private Cut(OwnerFullRepairGuard.Record var1, Object var2, int var3) {
            this.record = var1;
            this.actual = var2;
            this.reduction = var3;
        }
    }

    private static final class Mutation {
        private final ArrayList<OwnerLineageRevocationStore.Lease> leases = new ArrayList<>(5);

        private void add(OwnerLineageRevocationStore.Lease var1) {
            if (var1 != null) {
                for (OwnerLineageRevocationStore.Lease var3 : this.leases) {
                    if (var3 == var1) {
                        return;
                    }
                }

                this.leases.add(var1);
            }
        }

        private void flush() {
            Throwable var1 = null;

            for (OwnerLineageRevocationStore.Lease var3 : this.leases) {
                try {
                    OwnerLineageRevocationStore.retireLease(var3);
                } catch (Error | RuntimeException var5) {
                    if (var1 == null) {
                        var1 = var5;
                    }
                }
            }

            if (var1 instanceof RuntimeException var7) {
                throw var7;
            } else if (var1 instanceof Error var6) {
                throw var6;
            }
        }
    }

    public static final class Node {
        private WeakReference<OwnerFullRepairGuard.Record>[] owners = new WeakReference[4];
        private OwnerFullRepairGuard.Record actual;
        private OwnerFieldExpectedAuthority.DomainBefore fieldDomain;
        private Object generation;
        private OwnerLineageRevocationStore.Lease lease;
        private OwnerRepairGenerationAuthority.Intent intent;
        private boolean retired;
        private boolean untrackedEscape;

        private Node() {
        }
    }

    private static final class Record {
        private final WeakReference<Object> actual;
        private final ArrayList<WeakReference<?>> aliases = new ArrayList<>();
        private boolean denied;
        private long events;
        private int lastKind;

        private Record(Object var1) {
            this.actual = new WeakReference<>(var1);
        }

        private boolean contains(Object var1) {
            if (var1 == null) {
                return false;
            } else if (this.actual.get() == var1) {
                return true;
            } else {
                for (WeakReference var3 : this.aliases) {
                    if (var3.get() == var1) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private static final class StorageView {
        private final Object item;
        private final int count;
        private final NativeItemComponentObservation components;
        private final NativeItemCapabilityObservation capabilities;
        private final ArrayList<Object> targets = new ArrayList<>();
        private final ArrayList<Object> physical = new ArrayList<>();
        private final IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        private final IdentityHashMap<Object, Boolean> active = new IdentityHashMap<>();
        private final int[] budget = new int[]{0};

        private StorageView(Object var1, boolean var2) {
            this(var1, var2, NativeFoundationItemData.registries());
        }

        private StorageView(Object var1, boolean var2, HolderLookup.Provider registries) {
            OwnerFullRepairGuard.Node var3 = OwnerFullRepairGuard.node(var1, false);
            if (var3 != null && var3.untrackedEscape) {
                throw new IllegalArgumentException("Previously untracked raw escape");
            }
            if (!(var1 instanceof ItemStack stack)) {
                throw new IllegalArgumentException("Unsupported native callback stack storage");
            }
            this.capabilities = NativeItemCapabilityObservation.capture();
            this.components = NativeItemComponentObservation.capture(stack, registries);
            this.item = this.components.itemIdentity();
            this.count = this.components.count();
            this.components.observeRepairStorage((storage, root) -> {
                if (!(storage instanceof OwnerRevocationNodeAccess)) {
                    throw new IllegalArgumentException("Native callback storage revocation Mixin is required");
                }
                this.targets.add(storage);
                if (root != null) this.walk(root, 0);
            });
        }

        private void walk(Tag var1, int var2) {
            if (var1 != null) {
                if (var2 <= 64 && ++this.budget[0] <= 65536) {
                    Class var3 = var1.getClass();
                    if (OwnerFullRepairGuard.immutableValue(var1)) {
                        if (var3 == StringTag.class) {
                            String var11 = ((TextValue)var1).ftboss$rawText();
                            if (var11 == null || var11.length() > 1048576) {
                                throw new IllegalArgumentException("Callback text budget");
                            }
                        }
                    } else if (this.active.containsKey(var1)) {
                        throw new IllegalArgumentException("Callback graph cycle");
                    } else if (this.visited.put(var1, Boolean.TRUE) == null) {
                        if (!(var1 instanceof OwnerRevocationNodeAccess)) {
                            throw new IllegalArgumentException("Unsupported mutable callback storage");
                        } else {
                            this.active.put(var1, Boolean.TRUE);
                            OwnerFullRepairGuard.Node var4 = OwnerFullRepairGuard.node(var1, false);
                            if (var4 != null && var4.untrackedEscape) {
                                throw new IllegalArgumentException("Previously untracked storage escape");
                            } else {
                                this.targets.add(var1);

                                try {
                                    if (var3 != CompoundTag.class) {
                                        if (var3 == ListTag.class) {
                                            List<Tag> var15 = ((Sequence)var1).ftboss$rawListFields();
                                            if (var15 == null || var15.getClass() != ArrayList.class || var15.size() > 65536 - this.budget[0]) {
                                                throw new IllegalArgumentException("Unsupported callback list");
                                            }

                                            this.physical.add(var15);

                                            for (Tag var19 : var15) {
                                                if (var19 == null) {
                                                    throw new IllegalArgumentException("Callback null element");
                                                }

                                                this.walk(var19, var2 + 1);
                                            }
                                        } else if (var3 == ByteArrayTag.class) {
                                            byte[] var14 = ((Bytes)var1).ftboss$rawBytes();
                                            if (var14 == null || var14.length > 1048576) {
                                                throw new IllegalArgumentException("Callback byte array budget");
                                            }

                                            this.physical.add(var14);
                                        } else if (var3 == IntArrayTag.class) {
                                            int[] var13 = ((Ints)var1).ftboss$rawInts();
                                            if (var13 == null || var13.length > 1048576) {
                                                throw new IllegalArgumentException("Callback int array budget");
                                            }

                                            this.physical.add(var13);
                                        } else {
                                            if (var3 != LongArrayTag.class) {
                                                throw new IllegalArgumentException("Unknown callback mutable tag class");
                                            }

                                            long[] var12 = ((Longs)var1).ftboss$rawLongs();
                                            if (var12 == null || var12.length > 1048576) {
                                                throw new IllegalArgumentException("Callback long array budget");
                                            }

                                            this.physical.add(var12);
                                        }
                                    } else {
                                        Map<String, Tag> var5 = ((Compound)var1).ftboss$rawCompoundFields();
                                        if (var5 != null && (var5.getClass() == HashMap.class || NativeFreshFactoryTrust298.readableMap(var1, var5)) && var5.size() <= 65536 - this.budget[0]) {
                                            for (String var7 : var5.keySet()) {
                                                if (var7 == null || var7.length() > 1048576) {
                                                    throw new IllegalArgumentException("Callback map key");
                                                }
                                            }

                                            this.physical.add(var5);

                                            for (Entry<String, Tag> var18 : new TreeMap<>(var5).entrySet()) {
                                                if (var18.getValue() == null) {
                                                    throw new IllegalArgumentException("Callback null tag");
                                                }

                                                this.walk((Tag)var18.getValue(), var2 + 1);
                                            }

                                            return;
                                        }

                                        throw new IllegalArgumentException("Unsupported callback map");
                                    }
                                } finally {
                                    this.active.remove(var1);
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Callback graph budget");
                }
            }
        }

        private boolean same(OwnerFullRepairGuard.StorageView var1) {
            if (var1 != null
                && this.item == var1.item
                && this.count == var1.count
                && this.components.same(var1.components)
                && this.capabilities.same(var1.capabilities)
                && this.targets.size() == var1.targets.size()
                && this.physical.size() == var1.physical.size()) {
                for (int var2 = 0; var2 < this.targets.size(); var2++) {
                    if (this.targets.get(var2) != var1.targets.get(var2)) {
                        return false;
                    }
                }

                for (int var3 = 0; var3 < this.physical.size(); var3++) {
                    if (this.physical.get(var3) != var1.physical.get(var3)) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public static final class TickCut {
        private final Object actual;
        private final OwnerFullRepairGuard.Record record;
        private final Object generation;
        private final OwnerLineageRevocationStore.Lease lease;
        private final OwnerFullRepairGuard.StorageView before;
        private final long events;
        private final Thread thread = Thread.currentThread();
        private Throwable originalFailure;
        private Throwable observerFailure;
        private boolean closed;

        private TickCut(Object var1, OwnerFullRepairGuard.Node var2, OwnerFullRepairGuard.StorageView var3) {
            this.actual = var1;
            this.record = var2.actual;
            this.generation = var2.generation;
            this.lease = var2.lease;
            this.before = var3;
            this.events = this.record.events;
        }
    }
}
