package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

final class OwnerRepairContinuation {
    private static final Map<ItemStack, OwnerRepairContinuation.Binding> BINDINGS = new WeakHashMap<>();
    private static final Map<Object, OwnerRepairContinuation.IdentityRead> READS = new IdentityHashMap<>();

    private OwnerRepairContinuation() {
    }

    private static boolean caller(Class<?> var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class)
                        .findFirst()
                        .map(var2x -> var2x.getDeclaringClass() == var0 && var2x.getMethodName().equals(var1))
                        .orElse(false)
            );
    }

    private static OwnerRepairContinuation.Binding binding(ItemStack var0) {
        synchronized (OwnerFullRepairGuard.class) {
            return BINDINGS.get(var0);
        }
    }

    private static boolean valid(OwnerRepairContinuation.Binding var0) {
        ItemStack var1 = var0.actual.get();
        ServerPlayer var2 = var0.player.get();
        return !var0.rejected
            && var0.thread == Thread.currentThread()
            && var0.server != null
            && var0.server.isSameThread()
            && var1 != null
            && var2 != null
            && var2.getServer() == var0.server
            && var2.level() == var0.level
            && var0.owner.equals(var2.getUUID())
            && var2.getInventory() == var0.inventory.get()
            && var2.getInventory().getItem(var0.slot) == var1
            && var1.getCount() == 1
            && (var0.slot == 40 ? var2.getOffhandItem() == var1 : var2.getInventory().selected == var0.slot && var2.getMainHandItem() == var1)
            && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var1)
            && OwnerRepairGenerationAuthority.identityMatches(var1, var0.generation, var0.lease)
            && OwnerRepairGenerationAuthority.repaired(var1, var0.lease)
            && OwnerFullRepairGuard.permits(var1)
            && var0.rules.equals(OwnerStatueTaskPlan.rules(var1))
            && var0.before.equals(NativeFoundationItemData.image(var1));
    }

    private static boolean unique(OwnerRepairContinuation.Binding var0) {
        int var1 = 0;

        for (ServerPlayer var3 : var0.server.getPlayerList().getPlayers()) {
            Inventory var4 = var3.getInventory();

            for (int var5 = 0; var5 < var4.getContainerSize(); var5++) {
                ItemStack var6 = var4.getItem(var5);
                CompoundTag var7 = NativeRepairStackImage.data(NativeFoundationItemData.image(var6)).getCompound("FTBossWeaponLifecycle");
                if (var7.hasUUID("Weapon") && var0.weapon.equals(var7.getUUID("Weapon"))) {
                    if (var6 != var0.actual.get() || var3 != var0.player.get() || ++var1 > 1) {
                        return false;
                    }
                }
            }
        }

        return var1 == 1;
    }

    private static void retire(OwnerRepairContinuation.Binding var0) {
        if (var0 != null) {
            var0.rejected = true;
            if (var0.track != null) {
                var0.track.burn();
            }

            try {
                if (var0.prewriteTaken) {
                    try {
                        OwnerRepairGenerationAuthority.invalidate(var0.actual.get());
                    } finally {
                        OwnerLineageRevocationStore.retirePrewriteLease(var0.lease);
                    }
                } else {
                    OwnerRepairGenerationAuthority.unknownMutation(var0.actual.get());
                }
            } catch (Throwable var5) {
            }
        }
    }

    private static void burnPrewrite(OwnerRepairContinuation.Binding var0) {
        if (var0 != null) {
            var0.prewriteTaken = true;
            retire(var0);
        }
    }

    static boolean bind(
        OwnerStatueTaskOrigin.Publication var0,
        ItemStack var1,
        ServerPlayer var2,
        int var3,
        String var4,
        CompoundTag var5,
        Object var6,
        OwnerStatueTaskOrigin.PublishedPredecessor var7
    ) {
        if (!caller(OwnerStatueTaskOrigin.Publication.class, "bindContinuation")) {
            return false;
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerLineageRevocationStore.Lease var9 = OwnerFullRepairGuard.lease(var1);
                if (var0 != null
                    && var0.candidate(var9)
                    && !BINDINGS.containsKey(var1)
                    && var2 != null
                    && var9.owner.equals(var2.getUUID())
                    && OwnerRepairGenerationAuthority.current(var1) == var6
                    && var0.takePublishedPredecessor(var7, var1, var2, var3, var4, var5, var6, var9)) {
                    OwnerRepairContinuation.Binding var10 = new OwnerRepairContinuation.Binding(var1, var2, var3, var4, var5, var6, var9, var7);
                    if (valid(var10) && unique(var10)) {
                        BINDINGS.put(var1, var10);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private static boolean original(int var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> {
                    java.util.List<java.lang.StackWalker.StackFrame> var3 = var2.filter(
                            var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class
                                    && var0xx.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class
                        )
                        .limit(2L)
                        .toList();
                    return var3.size() == 2
                        && ((StackFrame)var3.get(0)).getDeclaringClass() == ItemStack.class
                        && ((StackFrame)var3.get(0)).getMethodName().equals(var1)
                        && ((StackFrame)var3.get(1)).getDeclaringClass() == ItemStack.class
                        && OwnerStatueWearBoundary.original(var0, ((StackFrame)var3.get(1)).getMethodName(), ((StackFrame)var3.get(1)).getDescriptor());
                }
            );
    }

    static OwnerRepairContinuation.Entry entered(ItemStack var0, Object var1, int var2, int var3) {
        if (!caller(OwnerStatueWearAdmission.class, "entered")) {
            return null;
        } else {
            OwnerRepairContinuation.Binding var4 = binding(var0);
            if (var4 != null && var2 != 13) {
                try {
                    if (var1 instanceof UseOnContext var5) {
                        var1 = var5.getPlayer();
                    }

                    synchronized (OwnerFullRepairGuard.class) {
                        OwnerRepairContinuation.Entry var6 = var4.active;
                        if (original(var2, "ftboss$statueWearEnterGuard")
                            && valid(var4)
                            && unique(var4)
                            && OwnerLineageRevocationStore.liveLease(var4.lease)
                            && var2 >= 1
                            && var2 <= 14
                            && (var6 != null || var2 != 9 && var1 == var4.player.get())
                            && (var6 == null || !var6.closed && var6.head && var6.depth < 128 && (var1 == null || var1 == var4.player.get()))) {
                            OwnerStatueWearPlan var7 = var2 == 8 && var4.predecessor != null
                                ? OwnerStatueWearPlan.prewrite(var0, var4.before, var0.getMaxDamage())
                                : null;
                            OwnerRepairContinuation.Entry var8 = new OwnerRepairContinuation.Entry(var4, var2, var3, var7);
                            var4.active = var8;
                            return var8;
                        } else {
                            if (var2 == 8) {
                                burnPrewrite(var4);
                            } else {
                                retire(var4);
                            }

                            return null;
                        }
                    }
                } catch (Throwable var11) {
                    if (var2 == 8) {
                        burnPrewrite(var4);
                    } else {
                        retire(var4);
                    }

                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static boolean prewriteLocal(OwnerRepairContinuation.Binding var0) {
        if (!Thread.holdsLock(OwnerFullRepairGuard.class)) {
            return false;
        } else {
            ItemStack var1 = var0.actual.get();
            ServerPlayer var2 = var0.player.get();
            return !var0.rejected
                && !var0.prewriteTaken
                && var0.thread == Thread.currentThread()
                && var0.server != null
                && var0.server.isSameThread()
                && var1 != null
                && var2 != null
                && var2.getServer() == var0.server
                && var2.level() == var0.level
                && var0.owner.equals(var2.getUUID())
                && var2.getInventory() == var0.inventory.get()
                && var2.getInventory().getItem(var0.slot) == var1
                && var1.getCount() == 1
                && (var0.slot == 40 ? var2.getOffhandItem() == var1 : var2.getInventory().selected == var0.slot && var2.getMainHandItem() == var1)
                && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var1)
                && OwnerRepairGenerationAuthority.identityMatches(var1, var0.generation, var0.lease)
                && OwnerRepairGenerationAuthority.repaired(var1, var0.lease)
                && OwnerFullRepairGuard.prewriteLocalDenialFree(var1, var0.generation, var0.lease)
                && var0.rules.equals(OwnerStatueTaskPlan.rules(var1))
                && var0.before.equals(NativeFoundationItemData.image(var1));
        }
    }

    static OwnerRepairContinuation.CarryWrite prewrite(ItemStack var0, int var1) {
        OwnerRepairContinuation.Binding var2 = binding(var0);
        if (var2 == null) {
            if ((var0.getItem() == Items.BOW || var0.getItem() == Items.CROSSBOW || var0.getItem() == Items.TRIDENT)
                && !FourWeaponFoundationAdapter.applies(var0)
                && !SwordCavityBuild.applies(var0)) {
                synchronized (OwnerFullRepairGuard.class) {
                    if (OwnerRepairGenerationAuthority.current(var0) == null && OwnerFullRepairGuard.lease(var0) == null) {
                        return null;
                    }

                    OwnerLineageRevocationStore.Lease var19 = OwnerRepairGenerationAuthority.invalidate(var0);
                    OwnerLineageRevocationStore.retirePrewriteLease(var19);
                }

                throw new IllegalStateException("P3 refuses a predecessor-less published object");
            } else {
                return null;
            }
        } else {
            try {
                OwnerRepairContinuation.Track var3;
                synchronized (OwnerFullRepairGuard.class) {
                    OwnerRepairContinuation.Entry var20 = var2.active;
                    boolean var6 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var0x -> var0x.filter(
                                        var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class
                                                && var0xx.getDeclaringClass() != WeaponMaterialBuild.class
                                    )
                                    .findFirst()
                                    .map(var0xx -> OwnerStatueWearBoundary.writer(2, var0xx))
                                    .orElse(false)
                        );
                    if (!caller(WeaponMaterialBuild.class, "durabilityCost")
                        || !var6
                        || var2.prewriteTaken
                        || var2.predecessor == null
                        || var20 == null
                        || var20.closed
                        || var20.kind != 8
                        || var20.requested != var1
                        || var1 <= 0
                        || var20.plan == null
                        || !var20.plan.cursor.equals(var2.before)
                        || !prewriteLocal(var2)
                        || !unique(var2)) {
                        throw new IllegalStateException("P3 lacks an exact original prewrite plan");
                    }

                    var2.prewriteTaken = true;
                    CompoundTag var7 = var2.predecessor
                        .prewriteBinding(var0, var2.player.get(), var2.slot, var2.rules, var2.before, var2.generation, var2.lease);
                    var20.plan.cost(var1);
                    OwnerStatueWearPlan.Step var8 = var20.plan.singleCarryStep();
                    var2.rejected = true;
                    OwnerRepairGenerationAuthority.invalidate(var0);
                    OwnerLineageRevocationStore.retirePrewriteLease(var2.lease);
                    var3 = new OwnerRepairContinuation.Track(var2, var20, var7, var8);
                    var2.track = var3;
                }

                OwnerStatueTaskService.reservePrewriteTrack(var3);
                synchronized (OwnerFullRepairGuard.class) {
                    CompoundTag var21 = NativeOwnerRepairItemData.rawElementForObservation(var0, "FTBossWeaponMaterials");
                    if (var3.phase == OwnerRepairContinuation.TrackPhase.TRACK_RESERVED
                        && var3.unchanged()
                        && unique(var2)
                        && var21 != null
                        && var21.getClass() == CompoundTag.class
                        && var21 instanceof Compound var22) {
                        Map var23 = var22.ftboss$rawCompoundFields();
                        if (var23 != null && var23.getClass() == HashMap.class) {
                            OwnerRepairContinuation.CarryWrite var24 = new OwnerRepairContinuation.CarryWrite(var3, var21, var23);
                            var3.writing = var24;
                            return var24;
                        } else {
                            throw new IllegalStateException("Unknown carry storage");
                        }
                    } else {
                        throw new IllegalStateException("Carry source changed after durable checkpoints");
                    }
                }
            } catch (Throwable var16) {
                synchronized (OwnerFullRepairGuard.class) {
                    burnPrewrite(var2);
                }

                throw new IllegalStateException("Prewrite single carry permanently burned", var16);
            }
        }
    }

    static void claimCarry(OwnerRepairContinuation.CarryWrite var0, ItemStack var1, CompoundTag var2, double var3) {
        if (var0 == null) {
            throw new IllegalStateException("Missing private single carry write");
        } else {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairContinuation.Track var6 = var0.track;

                try {
                    boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var0x -> var0x.filter(
                                        var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class
                                                && var0xx.getDeclaringClass() != OwnerStatueWearWrites.class
                                    )
                                    .findFirst()
                                    .map(var0xx -> var0xx.getDeclaringClass() == WeaponMaterialBuild.class && var0xx.getMethodName().equals("durabilityCost"))
                                    .orElse(false)
                        );
                    if (caller(OwnerStatueWearWrites.class, "carryOnly")
                        && var7
                        && !var0.taken
                        && !var0.closed
                        && var6.writing == var0
                        && var6.phase == OwnerRepairContinuation.TrackPhase.TRACK_RESERVED
                        && var6.unchanged()
                        && unique(var6.binding)
                        && var1 == var6.binding.actual.get()
                        && var2 == var0.receiver
                        && NativeOwnerRepairItemData.rawElementForObservation(var1, "FTBossWeaponMaterials") == var2
                        && var2 instanceof Compound var8
                        && var8.ftboss$rawCompoundFields() == var0.storage
                        && var6.carry.value().equals(DoubleTag.valueOf(var3))) {
                        var0.taken = true;
                        return;
                    }

                    throw new IllegalStateException("Single carry differs from private quote");
                } catch (Throwable var11) {
                    burnPrewrite(var6.binding);
                    throw new IllegalStateException("Single carry permanently burned before write", var11);
                }
            }
        }
    }

    static void carryMutation(Object var0, Object var1, int var2) {
        if (caller(OwnerStatueWearAdmission.class, "mutationNoticed") && var0 instanceof ItemStack var3) {
            OwnerRepairContinuation.Binding var4 = binding(var3);
            if (var4 != null && var4.prewriteTaken) {
                synchronized (OwnerFullRepairGuard.class) {
                    OwnerRepairContinuation.Track var6 = var4.track;
                    OwnerRepairContinuation.CarryWrite var7 = var6 == null ? null : var6.writing;

                    try {
                        if (var7 != null
                            && var7.taken
                            && !var7.closed
                            && var1 == var7.receiver
                            && (var2 == 20 || var2 == 21)
                            && var6.phase == OwnerRepairContinuation.TrackPhase.TRACK_RESERVED
                            && var6.unchanged()
                            && var1 instanceof Compound var8
                            && var8.ftboss$rawCompoundFields() == var7.storage) {
                            boolean var9 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                                .walk(
                                    var1x -> {
                                        java.util.List<java.lang.StackWalker.StackFrame> var2x = var1x.toList();
                                        int var3x = -1;

                                        for (int var4x = 0; var4x < var2x.size(); var4x++) {
                                            StackFrame var5 = (StackFrame)var2x.get(var4x);
                                            if (var5.getDeclaringClass() == OwnerStatueWearWrites.class && var5.getMethodName().equals("carryOnly")) {
                                                var3x = var4x;
                                                break;
                                            }
                                        }

                                        if (var3x >= 1 && var3x + 1 < var2x.size()) {
                                            StackFrame var6x = (StackFrame)var2x.get(var3x + 1);
                                            if (var6x.getDeclaringClass() == WeaponMaterialBuild.class && var6x.getMethodName().equals("durabilityCost")) {
                                                java.util.List<java.lang.StackWalker.StackFrame> var7x = var2x.subList(0, var3x);
                                                return var7x.stream()
                                                        .anyMatch(
                                                            var0xx -> var0xx.getDeclaringClass() == CompoundTag.class
                                                                    && var0xx.getDescriptor().equals("(Ljava/lang/String;D)V")
                                                        )
                                                    && var7x.stream()
                                                        .allMatch(
                                                            var1xx -> var1xx.getDeclaringClass().getNestHost() == OwnerRepairContinuation.class
                                                                    || var1xx.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                                                                    || var1xx.getDeclaringClass() == OwnerStatueWearAdmission.class
                                                                    || var1xx.getDeclaringClass() == CompoundTag.class
                                                                        && (
                                                                            var1xx.getDescriptor().equals("(Ljava/lang/String;D)V")
                                                                                || var1xx.getDescriptor()
                                                                                    .equals(
                                                                                        "(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
                                                                                    )
                                                                                || var1xx.getMethodName()
                                                                                    .contains(
                                                                                        var2 == 20
                                                                                            ? "ftboss$fullRepairVoidMutation"
                                                                                            : "ftboss$fullRepairReturnedMutation"
                                                                                    )
                                                                        )
                                                        );
                                            } else {
                                                return false;
                                            }
                                        } else {
                                            return false;
                                        }
                                    }
                                );
                            int var10 = var2 == 20 ? 1 : 2;
                            if (!var9 || (var7.observations & var10) != 0) {
                                throw new IllegalStateException("Repeated or unknown single carry observer");
                            }

                            var7.observations |= var10;
                            return;
                        }

                        throw new IllegalStateException("Foreign mutation during single carry");
                    } catch (Throwable var12) {
                        burnPrewrite(var4);
                    }
                }
            }
        }
    }

    static void completeCarry(OwnerRepairContinuation.CarryWrite var0, boolean var1) {
        if (var0 != null) {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairContinuation.Track var3 = var0.track;

                try {
                    if (!var1) {
                        return;
                    }

                    ItemStack var4 = var3.binding.actual.get();
                    if (!caller(OwnerStatueWearWrites.class, "carryOnly")
                        || var0.closed
                        || !var0.taken
                        || var3.writing != var0
                        || var3.phase != OwnerRepairContinuation.TrackPhase.TRACK_RESERVED
                        || (var0.observations & 1) == 0
                        || !var3.entry.plan.cursor.equals(var3.binding.before)
                        || !var3.atExpected(var3.carry.after())
                        || !unique(var3.binding)
                        || NativeOwnerRepairItemData.rawElementForObservation(var4, "FTBossWeaponMaterials") != var0.receiver
                        || !(var0.receiver instanceof Compound var5)
                        || var5.ftboss$rawCompoundFields() != var0.storage) {
                        throw new IllegalStateException("Single carry result differs from independent step");
                    }

                    var3.entry.plan.advanced(var3.carry);
                } catch (Throwable var12) {
                    throw new IllegalStateException("Single carry result permanently rejected", var12);
                } finally {
                    var0.closed = true;
                    burnPrewrite(var3.binding);
                }
            }
        }
    }

    static void finishCost(OwnerRepairContinuation.CarryWrite var0, boolean var1) {
        if (var0 != null) {
            synchronized (OwnerFullRepairGuard.class) {
                burnPrewrite(var0.track.binding);
            }

            if (var1) {
                throw new IllegalStateException("Single carry ended; cost return, Damage and normal seal remain forbidden");
            }
        }
    }

    static boolean deferEntry(Object var0, int var1) {
        if (var0 instanceof ItemStack var2 && caller(OwnerFullRepairGuard.class, "changed")) {
            OwnerRepairContinuation.Binding var3 = binding(var2);
            if (var3 == null) {
                return false;
            }

            try {
                synchronized (OwnerFullRepairGuard.class) {
                    OwnerRepairContinuation.Entry var5 = var3.active;
                    if (var5 != null && !var5.closed && !var5.head && (var1 == 20 || var1 == 21) && valid(var3)) {
                        boolean var6 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                            .walk(
                                var2x -> {
                                    java.util.List<java.lang.StackWalker.StackFrame> var3x = var2x.filter(
                                            var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class
                                                    && var0xx.getDeclaringClass() != OwnerFullRepairGuard.class
                                        )
                                        .limit(2L)
                                        .toList();
                                    return var3x.size() == 2
                                        && ((StackFrame)var3x.get(0)).getDeclaringClass() == ItemStack.class
                                        && ((StackFrame)var3x.get(0))
                                            .getMethodName()
                                            .contains(var1 == 20 ? "ftboss$fullRepairVoidMutation" : "ftboss$fullRepairReturnedMutation")
                                        && ((StackFrame)var3x.get(1)).getDeclaringClass() == ItemStack.class
                                        && OwnerStatueWearBoundary.original(
                                            var5.kind, ((StackFrame)var3x.get(1)).getMethodName(), ((StackFrame)var3x.get(1)).getDescriptor()
                                        );
                                }
                            );
                        if (!var6) {
                            return false;
                        }

                        var5.head = true;
                        return true;
                    }

                    return false;
                }
            } catch (Throwable var9) {
                retire(var3);
                return false;
            }
        }

        return false;
    }

    static void exited(OwnerRepairContinuation.Entry var0, Throwable var1) {
        if (var0 != null) {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairContinuation.Binding var3 = var0.binding;

                try {
                    if (!caller(OwnerStatueWearAdmission.class, "exited")
                        || !original(var0.kind, "ftboss$statueWearExitGuard")
                        || var0.closed
                        || var3.active != var0
                        || !var0.head
                        || var1 != null
                        || !valid(var3)) {
                        retire(var3);
                    }
                } catch (Throwable var10) {
                    retire(var3);
                } finally {
                    var0.closed = true;
                    if (var3.active == var0) {
                        var3.active = var0.previous;
                    }
                }
            }
        }
    }

    static OwnerRepairContinuation.IdentityRead identityRead(ItemStack var0, CompoundTag var1) {
        OwnerRepairContinuation.Binding var2 = binding(var0);
        if (var2 == null) {
            return null;
        } else {
            try {
                boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.filter(
                                    var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class
                                            && var0xx.getDeclaringClass() != OwnerStatueTaskWrites.class
                                )
                                .findFirst()
                                .map(
                                    var0xx -> var0xx.getDeclaringClass() == FoodWeaponLifecycle.class
                                            && Set.of("weaponId", "ensureTracked").contains(var0xx.getMethodName())
                                )
                                .orElse(false)
                    );
                synchronized (OwnerFullRepairGuard.class) {
                    if (var3
                        && valid(var2)
                        && NativeSourceLedgerItemData.rawCustomDataForObservation(var0).getCompound("FTBossWeaponLifecycle") == var1
                        && var1.get("Weapon") instanceof IntArrayTag var5
                        && !READS.containsKey(var1)
                        && !READS.containsKey(var5)) {
                        OwnerRepairContinuation.IdentityRead var10 = new OwnerRepairContinuation.IdentityRead(var2, var1, var5);
                        READS.put(var1, var10);
                        READS.put(var5, var10);
                        return var10;
                    } else {
                        throw new IllegalStateException("Detached published identity read");
                    }
                }
            } catch (Throwable var9) {
                retire(var2);
                return null;
            }
        }
    }

    static boolean identityAlias(Object var0, Object var1) {
        if (var1 instanceof int[] var2 && caller(OwnerFullRepairGuard.class, "alias")) {
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairContinuation.IdentityRead var4 = READS.get(var0);
                if (var4 == null) {
                    return false;
                }

                boolean var10000;
                try {
                    boolean var5 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var0x -> var0x.anyMatch(
                                    var0xx -> var0xx.getDeclaringClass() == CompoundTag.class
                                            && (
                                                var0xx.getDescriptor().equals("(Ljava/lang/String;)Ljava/util/UUID;")
                                                    || var0xx.getDescriptor().equals("(Ljava/lang/String;)Z")
                                            )
                                )
                        );
                    if (!var5
                        || !valid(var4.binding)
                        || var4.array != null && var4.array != var2
                        || !Arrays.equals(var2, NativeRepairStackImage.data(var4.binding.before).getCompound("FTBossWeaponLifecycle").getIntArray("Weapon"))) {
                        throw new IllegalStateException("Published identity alias differs");
                    }

                    var4.array = var2;
                    var10000 = true;
                } catch (Throwable var7) {
                    retire(var4.binding);
                    return false;
                }

                return var10000;
            }
        }

        return false;
    }

    static void identityReturned(OwnerRepairContinuation.IdentityRead var0, Throwable var1) {
        if (var0 != null) {
            synchronized (OwnerFullRepairGuard.class) {
                if (READS.get(var0.receiver) == var0) {
                    READS.remove(var0.receiver);
                }

                if (READS.get(var0.storage) == var0) {
                    READS.remove(var0.storage);
                }

                try {
                    if (var1 != null || var0.array == null || !valid(var0.binding)) {
                        retire(var0.binding);
                    }
                } catch (Throwable var5) {
                    retire(var0.binding);
                }
            }
        }
    }

    static void custodyChanged(Object var0) {
        if (caller(OwnerStatueWearAdmission.class, "custodyChanged")) {
            synchronized (OwnerFullRepairGuard.class) {
                for (OwnerRepairContinuation.Binding var3 : BINDINGS.values()) {
                    Inventory var4 = var3.inventory.get();
                    if (var4 == null || var0 == var4.items || var0 == var4.armor || var0 == var4.offhand) {
                        retire(var3);
                    }
                }
            }
        }
    }

    static boolean noBinding(ItemStack var0) {
        synchronized (OwnerFullRepairGuard.class) {
            return BINDINGS.get(var0) == null;
        }
    }

    private static final class Binding {
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final Object level;
        private final UUID owner;
        private final UUID weapon;
        private final int slot;
        private final String rules;
        private final CompoundTag before;
        private final Object generation;
        private final OwnerLineageRevocationStore.Lease lease;
        private final OwnerStatueTaskOrigin.PublishedPredecessor predecessor;
        private final Thread thread = Thread.currentThread();
        private boolean rejected;
        private volatile boolean prewriteTaken;
        private volatile OwnerRepairContinuation.Track track;
        private OwnerRepairContinuation.Entry active;

        private Binding(
            ItemStack var1,
            ServerPlayer var2,
            int var3,
            String var4,
            CompoundTag var5,
            Object var6,
            OwnerLineageRevocationStore.Lease var7,
            OwnerStatueTaskOrigin.PublishedPredecessor var8
        ) {
            this.actual = new WeakReference<>(var1);
            this.player = new WeakReference<>(var2);
            this.inventory = new WeakReference<>(var2.getInventory());
            this.server = var2.getServer();
            this.level = var2.level();
            this.owner = var2.getUUID();
            this.weapon = var7.weapon;
            this.slot = var3;
            this.rules = var4;
            this.before = var5.copy();
            this.generation = var6;
            this.lease = var7;
            this.predecessor = var8;
        }
    }

    static final class CarryWrite {
        private final OwnerRepairContinuation.Track track;
        private final CompoundTag receiver;
        private final Object storage;
        private boolean taken;
        private boolean closed;
        private int observations;

        private CarryWrite(OwnerRepairContinuation.Track var1, CompoundTag var2, Object var3) {
            this.track = var1;
            this.receiver = var2;
            this.storage = var3;
        }
    }

    static final class Entry {
        private final OwnerRepairContinuation.Binding binding;
        private final OwnerRepairContinuation.Entry previous;
        private final int kind;
        private final int depth;
        private final int requested;
        private final OwnerStatueWearPlan plan;
        private boolean head;
        private boolean closed;

        private Entry(OwnerRepairContinuation.Binding var1, int var2, int var3, OwnerStatueWearPlan var4) {
            this.binding = var1;
            this.previous = var1.active;
            this.kind = var2;
            this.requested = var3;
            this.plan = var4;
            this.depth = this.previous == null ? 1 : this.previous.depth + 1;
        }
    }

    static final class IdentityRead {
        private final OwnerRepairContinuation.Binding binding;
        private final CompoundTag receiver;
        private final Object storage;
        private int[] array;

        private IdentityRead(OwnerRepairContinuation.Binding var1, CompoundTag var2, Object var3) {
            this.binding = var1;
            this.receiver = var2;
            this.storage = var3;
        }
    }

    static final class Track {
        private final OwnerRepairContinuation.Binding binding;
        private final OwnerRepairContinuation.Entry entry;
        private final CompoundTag record;
        private volatile OwnerRepairContinuation.TrackPhase phase = OwnerRepairContinuation.TrackPhase.QUOTED;
        private OwnerLineageRevocationStore store;
        private boolean storeTaken;
        private final OwnerStatueWearPlan.Step carry;
        private OwnerRepairContinuation.CarryWrite writing;

        private Track(OwnerRepairContinuation.Binding var1, OwnerRepairContinuation.Entry var2, CompoundTag var3, OwnerStatueWearPlan.Step var4) {
            this.binding = var1;
            this.entry = var2;
            this.carry = var4;
            this.record = var3.copy();
            this.record.putUUID("Track", UUID.randomUUID());
            this.record.putInt("Requested", var2.requested);
            this.record.putInt("Maximum", var2.plan.expectedMaximum());
            this.record.putString("Authority", "NEGATIVE_ONLY_NO_WRITE_LEASE_ROOT_REPAIR_OR_GENERATION");
        }

        private boolean unchanged() {
            return this.atExpected(this.binding.before) && this.entry.plan.cursor.equals(this.binding.before);
        }

        private boolean atExpected(CompoundTag var1) {
            ItemStack var2 = this.binding.actual.get();
            ServerPlayer var3 = this.binding.player.get();
            return this.phase != OwnerRepairContinuation.TrackPhase.BURNED
                && this.binding.track == this
                && this.binding.prewriteTaken
                && this.binding.rejected
                && this.binding.thread == Thread.currentThread()
                && this.binding.server != null
                && this.binding.server.isSameThread()
                && var3 != null
                && var2 != null
                && var3.getServer() == this.binding.server
                && var3.level() == this.binding.level
                && var3.getInventory() == this.binding.inventory.get()
                && this.binding.owner.equals(var3.getUUID())
                && var3.getInventory().getItem(this.binding.slot) == var2
                && var2.getCount() == 1
                && (
                    this.binding.slot == 40
                        ? var3.getOffhandItem() == var2
                        : var3.getInventory().selected == this.binding.slot && var3.getMainHandItem() == var2
                )
                && this.binding.active == this.entry
                && !this.entry.closed
                && this.entry.kind == 8
                && this.entry.plan != null
                && !this.entry.plan.rejected
                && var1.equals(NativeFoundationItemData.image(var2))
                && this.binding.rules.equals(OwnerStatueTaskPlan.rules(var2))
                && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var2)
                && OwnerRepairGenerationAuthority.current(var2) == this.binding.generation
                && OwnerFullRepairGuard.lease(var2) == this.binding.lease
                && !OwnerLineageRevocationStore.locallyLive(this.binding.lease);
        }

        ServerPlayer ownerPlayer() throws IOException {
            if (OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack") && this.unchanged()) {
                return this.binding.player.get();
            } else {
                throw new IOException("Detached prewrite owner");
            }
        }

        CompoundTag ownerRecord() throws IOException {
            if (OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack")
                && this.phase == OwnerRepairContinuation.TrackPhase.QUOTED
                && this.unchanged()) {
                return this.record.copy();
            } else {
                throw new IOException("Detached prewrite record");
            }
        }

        void ownerPrepared(OwnerLineageRevocationStore var1) throws IOException {
            if (OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack")
                && this.phase == OwnerRepairContinuation.TrackPhase.QUOTED
                && var1 != null
                && var1.owns(this.binding.lease)
                && this.unchanged()) {
                this.store = var1;
                this.phase = OwnerRepairContinuation.TrackPhase.PREPARING;
            } else {
                throw new IOException("Prewrite owner preparation changed");
            }
        }

        CompoundTag takeForStore(OwnerLineageRevocationStore var1) throws IOException {
            if (OwnerRepairContinuation.caller(OwnerLineageRevocationStore.class, "consumePrewriteTrack") && !this.storeTaken) {
                this.storeTaken = true;
                if (this.store == var1 && this.phase == OwnerRepairContinuation.TrackPhase.PREPARING && this.unchanged()) {
                    return this.record.copy();
                } else {
                    throw new IOException("Detached prewrite store consumption");
                }
            } else {
                throw new IOException("Prewrite store ticket repeated");
            }
        }

        void reserved() throws IOException {
            if (OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack")
                && this.phase == OwnerRepairContinuation.TrackPhase.PREPARING
                && this.storeTaken
                && this.unchanged()) {
                this.phase = OwnerRepairContinuation.TrackPhase.TRACK_RESERVED;
            } else {
                throw new IOException("Prewrite reservation changed");
            }
        }

        private void burn() {
            this.phase = OwnerRepairContinuation.TrackPhase.BURNED;
        }
    }

    private static enum TrackPhase {
        QUOTED,
        PREPARING,
        TRACK_RESERVED,
        BURNED;
    }
}
