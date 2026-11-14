/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.UseOnContext
 */
package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueTaskPlan;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.item.OwnerStatueTaskWrites;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.OwnerStatueWearBoundary;
import net.farmingtales.ftboss.item.OwnerStatueWearPlan;
import net.farmingtales.ftboss.item.OwnerStatueWearWrites;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
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
    private static final Map<ItemStack, Binding> BINDINGS = new WeakHashMap<ItemStack, Binding>();
    private static final Map<Object, IdentityRead> READS = new IdentityHashMap<Object, IdentityRead>();

    private OwnerRepairContinuation() {
    }

    private static boolean caller(Class<?> clazz, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && stackFrame.getMethodName().equals(string)).orElse(false));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Binding binding(ItemStack itemStack) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return BINDINGS.get(itemStack);
        }
    }

    private static boolean valid(Binding binding) {
        ItemStack itemStack = (ItemStack)binding.actual.get();
        ServerPlayer serverPlayer = (ServerPlayer)binding.player.get();
        return !binding.rejected && binding.thread == Thread.currentThread() && binding.server != null && binding.server.m_18695_() && itemStack != null && serverPlayer != null && serverPlayer.m_20194_() == binding.server && serverPlayer.m_9236_() == binding.level && binding.owner.equals(serverPlayer.m_20148_()) && serverPlayer.m_150109_() == binding.inventory.get() && serverPlayer.m_150109_().m_8020_(binding.slot) == itemStack && itemStack.m_41613_() == 1 && (binding.slot == 40 ? serverPlayer.m_21206_() == itemStack : serverPlayer.m_150109_().f_35977_ == binding.slot && serverPlayer.m_21205_() == itemStack) && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) && OwnerRepairGenerationAuthority.identityMatches(itemStack, binding.generation, binding.lease) && OwnerRepairGenerationAuthority.repaired(itemStack, binding.lease) && OwnerFullRepairGuard.permits(itemStack) && binding.rules.equals(OwnerStatueTaskPlan.rules(itemStack)) && binding.before.equals((Object)itemStack.m_41739_(new CompoundTag()));
    }

    private static boolean unique(Binding binding) {
        int n = 0;
        for (ServerPlayer serverPlayer : binding.server.m_6846_().m_11314_()) {
            Inventory inventory = serverPlayer.m_150109_();
            for (int i = 0; i < inventory.m_6643_(); ++i) {
                ItemStack itemStack = inventory.m_8020_(i);
                CompoundTag compoundTag = itemStack.m_41739_(new CompoundTag()).m_128469_("tag").m_128469_("FTBossWeaponLifecycle");
                if (!compoundTag.m_128403_("Weapon") || !binding.weapon.equals(compoundTag.m_128342_("Weapon")) || itemStack == binding.actual.get() && serverPlayer == binding.player.get() && ++n <= 1) continue;
                return false;
            }
        }
        return n == 1;
    }

    private static void retire(Binding binding) {
        block8: {
            if (binding == null) {
                return;
            }
            binding.rejected = true;
            if (binding.track != null) {
                binding.track.burn();
            }
            try {
                if (binding.prewriteTaken) {
                    try {
                        OwnerRepairGenerationAuthority.invalidate(binding.actual.get());
                        break block8;
                    }
                    finally {
                        OwnerLineageRevocationStore.retirePrewriteLease(binding.lease);
                    }
                }
                OwnerRepairGenerationAuthority.unknownMutation(binding.actual.get());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private static void burnPrewrite(Binding binding) {
        if (binding == null) {
            return;
        }
        binding.prewriteTaken = true;
        OwnerRepairContinuation.retire(binding);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean bind(OwnerStatueTaskOrigin.Publication publication, ItemStack itemStack, ServerPlayer serverPlayer, int n, String string, CompoundTag compoundTag, Object object, OwnerStatueTaskOrigin.PublishedPredecessor publishedPredecessor) {
        if (!OwnerRepairContinuation.caller(OwnerStatueTaskOrigin.Publication.class, "bindContinuation")) {
            return false;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(itemStack);
            if (publication == null || !publication.candidate(lease) || BINDINGS.containsKey(itemStack) || serverPlayer == null || !lease.owner.equals(serverPlayer.m_20148_()) || OwnerRepairGenerationAuthority.current(itemStack) != object || !publication.takePublishedPredecessor(publishedPredecessor, itemStack, serverPlayer, n, string, compoundTag, object, lease)) {
                // ** MonitorExit[var8_8] (shouldn't be in output)
                return false;
            }
            Binding binding = new Binding(itemStack, serverPlayer, n, string, compoundTag, object, lease, publishedPredecessor);
            if (!OwnerRepairContinuation.valid(binding) || !OwnerRepairContinuation.unique(binding)) {
                // ** MonitorExit[var8_8] (shouldn't be in output)
                return false;
            }
            BINDINGS.put(itemStack, binding);
            // ** MonitorExit[var8_8] (shouldn't be in output)
            return true;
        }
    }

    private static boolean original(int n, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class && stackFrame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getDeclaringClass() == ItemStack.class && list.get(0).getMethodName().equals(string) && list.get(1).getDeclaringClass() == ItemStack.class && OwnerStatueWearBoundary.original(n, list.get(1).getMethodName(), list.get(1).getDescriptor());
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Entry entered(ItemStack itemStack, Object object, int n, int n2) {
        if (!OwnerRepairContinuation.caller(OwnerStatueWearAdmission.class, "entered")) {
            return null;
        }
        Binding binding = OwnerRepairContinuation.binding(itemStack);
        if (binding == null || n == 13) {
            return null;
        }
        try {
            Object object2;
            if (object instanceof UseOnContext) {
                object2 = (UseOnContext)object;
                object = object2.m_43723_();
            }
            object2 = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                Entry entry;
                Entry entry2 = binding.active;
                if (!OwnerRepairContinuation.original(n, "ftboss$statueWearEnterGuard") || !OwnerRepairContinuation.valid(binding) || !OwnerRepairContinuation.unique(binding) || !OwnerLineageRevocationStore.liveLease(binding.lease) || n < 1 || n > 14 || entry2 == null && (n == 9 || object != binding.player.get()) || entry2 != null && (entry2.closed || !entry2.head || entry2.depth >= 128 || object != null && object != binding.player.get())) {
                    if (n == 8) {
                        OwnerRepairContinuation.burnPrewrite(binding);
                    } else {
                        OwnerRepairContinuation.retire(binding);
                    }
                    // ** MonitorExit[var5_5 /* !! */ ] (shouldn't be in output)
                    return null;
                }
                OwnerStatueWearPlan ownerStatueWearPlan = n == 8 && binding.predecessor != null ? OwnerStatueWearPlan.prewrite(itemStack, binding.before, itemStack.m_41776_()) : null;
                binding.active = entry = new Entry(binding, n, n2, ownerStatueWearPlan);
                // ** MonitorExit[var5_5 /* !! */ ] (shouldn't be in output)
                return entry;
            }
        }
        catch (Throwable throwable) {
            if (n == 8) {
                OwnerRepairContinuation.burnPrewrite(binding);
            } else {
                OwnerRepairContinuation.retire(binding);
            }
            return null;
        }
    }

    private static boolean prewriteLocal(Binding binding) {
        if (!Thread.holdsLock(OwnerFullRepairGuard.class)) {
            return false;
        }
        ItemStack itemStack = (ItemStack)binding.actual.get();
        ServerPlayer serverPlayer = (ServerPlayer)binding.player.get();
        return !binding.rejected && !binding.prewriteTaken && binding.thread == Thread.currentThread() && binding.server != null && binding.server.m_18695_() && itemStack != null && serverPlayer != null && serverPlayer.m_20194_() == binding.server && serverPlayer.m_9236_() == binding.level && binding.owner.equals(serverPlayer.m_20148_()) && serverPlayer.m_150109_() == binding.inventory.get() && serverPlayer.m_150109_().m_8020_(binding.slot) == itemStack && itemStack.m_41613_() == 1 && (binding.slot == 40 ? serverPlayer.m_21206_() == itemStack : serverPlayer.m_150109_().f_35977_ == binding.slot && serverPlayer.m_21205_() == itemStack) && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) && OwnerRepairGenerationAuthority.identityMatches(itemStack, binding.generation, binding.lease) && OwnerRepairGenerationAuthority.repaired(itemStack, binding.lease) && OwnerFullRepairGuard.prewriteLocalDenialFree(itemStack, binding.generation, binding.lease) && binding.rules.equals(OwnerStatueTaskPlan.rules(itemStack)) && binding.before.equals((Object)itemStack.m_41739_(new CompoundTag()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static CarryWrite prewrite(ItemStack itemStack, int n) {
        Binding binding = OwnerRepairContinuation.binding(itemStack);
        if (binding == null) {
            if (itemStack.m_41720_() != Items.f_42411_ && itemStack.m_41720_() != Items.f_42717_ && itemStack.m_41720_() != Items.f_42713_ || FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack)) {
                return null;
            }
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                if (OwnerRepairGenerationAuthority.current(itemStack) == null && OwnerFullRepairGuard.lease(itemStack) == null) {
                    // ** MonitorExit[var3_3] (shouldn't be in output)
                    return null;
                }
                OwnerLineageRevocationStore.Lease lease = OwnerRepairGenerationAuthority.invalidate(itemStack);
                OwnerLineageRevocationStore.retirePrewriteLease(lease);
                // ** MonitorExit[var3_3] (shouldn't be in output)
                throw new IllegalStateException("P3 refuses a predecessor-less published object");
            }
        }
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                Track track;
                Entry entry = binding.active;
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class && stackFrame.getDeclaringClass() != WeaponMaterialBuild.class).findFirst().map(stackFrame -> OwnerStatueWearBoundary.writer(2, stackFrame)).orElse(false));
                if (!(OwnerRepairContinuation.caller(WeaponMaterialBuild.class, "durabilityCost") && bl && !binding.prewriteTaken && binding.predecessor != null && entry != null && !entry.closed && entry.kind == 8 && entry.requested == n && n > 0 && entry.plan != null && entry.plan.cursor.equals((Object)binding.before) && OwnerRepairContinuation.prewriteLocal(binding) && OwnerRepairContinuation.unique(binding))) {
                    throw new IllegalStateException("P3 lacks an exact original prewrite plan");
                }
                binding.prewriteTaken = true;
                Object object = binding.predecessor.prewriteBinding(itemStack, (ServerPlayer)binding.player.get(), binding.slot, binding.rules, binding.before, binding.generation, binding.lease);
                entry.plan.cost(n);
                Object object2 = entry.plan.singleCarryStep();
                binding.rejected = true;
                OwnerRepairGenerationAuthority.invalidate(itemStack);
                OwnerLineageRevocationStore.retirePrewriteLease(binding.lease);
                binding.track = track = new Track(binding, entry, (CompoundTag)object, (OwnerStatueWearPlan.Step)object2);
                // ** MonitorExit[var4_6] (shouldn't be in output)
                OwnerStatueTaskService.reservePrewriteTrack(track);
                clazz = OwnerFullRepairGuard.class;
                synchronized (OwnerFullRepairGuard.class) {
                    entry = itemStack.m_41737_("FTBossWeaponMaterials");
                    if (!(track.phase == TrackPhase.TRACK_RESERVED && track.unchanged() && OwnerRepairContinuation.unique(binding) && entry != null && entry.getClass() == CompoundTag.class && (object = entry) instanceof NativeExpectedFieldViews.Compound)) {
                        throw new IllegalStateException("Carry source changed after durable checkpoints");
                    }
                    NativeExpectedFieldViews.Compound compound = (NativeExpectedFieldViews.Compound)object;
                    object = compound.ftboss$rawCompoundFields();
                    if (object == null || object.getClass() != HashMap.class) {
                        throw new IllegalStateException("Unknown carry storage");
                    }
                    track.writing = object2 = new CarryWrite(track, (CompoundTag)entry, object);
                    // ** MonitorExit[var4_6] (shouldn't be in output)
                    return object2;
                }
            }
        }
        catch (Throwable throwable) {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairContinuation.burnPrewrite(binding);
                // ** MonitorExit[var5_10] (shouldn't be in output)
                throw new IllegalStateException("Prewrite single carry permanently burned", throwable);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void claimCarry(CarryWrite carryWrite, ItemStack itemStack, CompoundTag compoundTag, double d) {
        if (carryWrite == null) {
            throw new IllegalStateException("Missing private single carry write");
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Track track = carryWrite.track;
            try {
                NativeExpectedFieldViews.Compound compound;
                CompoundTag compoundTag2;
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class && stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == WeaponMaterialBuild.class && stackFrame.getMethodName().equals("durabilityCost")).orElse(false));
                if (!(OwnerRepairContinuation.caller(OwnerStatueWearWrites.class, "carryOnly") && bl && !carryWrite.taken && !carryWrite.closed && track.writing == carryWrite && track.phase == TrackPhase.TRACK_RESERVED && track.unchanged() && OwnerRepairContinuation.unique(track.binding) && itemStack == track.binding.actual.get() && compoundTag == carryWrite.receiver && itemStack.m_41737_("FTBossWeaponMaterials") == compoundTag && (compoundTag2 = compoundTag) instanceof NativeExpectedFieldViews.Compound && (compound = (NativeExpectedFieldViews.Compound)compoundTag2).ftboss$rawCompoundFields() == carryWrite.storage && track.carry.value().equals(DoubleTag.m_128500_((double)d)))) {
                    throw new IllegalStateException("Single carry differs from private quote");
                }
                carryWrite.taken = true;
            }
            catch (Throwable throwable) {
                OwnerRepairContinuation.burnPrewrite(track.binding);
                throw new IllegalStateException("Single carry permanently burned before write", throwable);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void carryMutation(Object object, Object object2, int n) {
        if (!OwnerRepairContinuation.caller(OwnerStatueWearAdmission.class, "mutationNoticed") || !(object instanceof ItemStack)) {
            return;
        }
        ItemStack itemStack = (ItemStack)object;
        Binding binding = OwnerRepairContinuation.binding(itemStack);
        if (binding == null || !binding.prewriteTaken) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Track track = binding.track;
            CarryWrite carryWrite = track == null ? null : track.writing;
            try {
                int n2;
                NativeExpectedFieldViews.Compound compound;
                Object object3;
                if (carryWrite == null || !carryWrite.taken || carryWrite.closed || object2 != carryWrite.receiver || n != 20 && n != 21 || track.phase != TrackPhase.TRACK_RESERVED || !track.unchanged() || !((object3 = object2) instanceof NativeExpectedFieldViews.Compound) || (compound = (NativeExpectedFieldViews.Compound)object3).ftboss$rawCompoundFields() != carryWrite.storage) {
                    throw new IllegalStateException("Foreign mutation during single carry");
                }
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
                    List list;
                    List list2 = stream.toList();
                    int n2 = -1;
                    for (int i = 0; i < list2.size(); ++i) {
                        list = (StackWalker.StackFrame)list2.get(i);
                        if (list.getDeclaringClass() != OwnerStatueWearWrites.class || !list.getMethodName().equals("carryOnly")) continue;
                        n2 = i;
                        break;
                    }
                    if (n2 < 1 || n2 + 1 >= list2.size()) {
                        return false;
                    }
                    StackWalker.StackFrame stackFrame2 = (StackWalker.StackFrame)list2.get(n2 + 1);
                    if (stackFrame2.getDeclaringClass() != WeaponMaterialBuild.class || !stackFrame2.getMethodName().equals("durabilityCost")) {
                        return false;
                    }
                    list = list2.subList(0, n2);
                    return list.stream().anyMatch(stackFrame -> stackFrame.getDeclaringClass() == CompoundTag.class && stackFrame.getDescriptor().equals("(Ljava/lang/String;D)V")) && list.stream().allMatch(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == OwnerRepairContinuation.class || stackFrame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class || stackFrame.getDeclaringClass() == OwnerStatueWearAdmission.class || stackFrame.getDeclaringClass() == CompoundTag.class && (stackFrame.getDescriptor().equals("(Ljava/lang/String;D)V") || stackFrame.getDescriptor().equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;") || stackFrame.getMethodName().contains(n == 20 ? "ftboss$fullRepairVoidMutation" : "ftboss$fullRepairReturnedMutation")));
                });
                int n3 = n2 = n == 20 ? 1 : 2;
                if (!bl || (carryWrite.observations & n2) != 0) {
                    throw new IllegalStateException("Repeated or unknown single carry observer");
                }
                carryWrite.observations |= n2;
            }
            catch (Throwable throwable) {
                OwnerRepairContinuation.burnPrewrite(binding);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void completeCarry(CarryWrite carryWrite, boolean bl) {
        if (carryWrite == null) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Track track = carryWrite.track;
            try {
                NativeExpectedFieldViews.Compound compound;
                CompoundTag compoundTag;
                if (!bl) {
                    // ** MonitorExit[var2_2] (shouldn't be in output)
                    return;
                }
                ItemStack itemStack = (ItemStack)track.binding.actual.get();
                if (!(OwnerRepairContinuation.caller(OwnerStatueWearWrites.class, "carryOnly") && !carryWrite.closed && carryWrite.taken && track.writing == carryWrite && track.phase == TrackPhase.TRACK_RESERVED && (carryWrite.observations & 1) != 0 && track.entry.plan.cursor.equals((Object)track.binding.before) && track.atExpected(track.carry.after()) && OwnerRepairContinuation.unique(track.binding) && itemStack.m_41737_("FTBossWeaponMaterials") == carryWrite.receiver && (compoundTag = carryWrite.receiver) instanceof NativeExpectedFieldViews.Compound && (compound = (NativeExpectedFieldViews.Compound)compoundTag).ftboss$rawCompoundFields() == carryWrite.storage)) {
                    throw new IllegalStateException("Single carry result differs from independent step");
                }
                track.entry.plan.advanced(track.carry);
            }
            catch (Throwable throwable) {
                throw new IllegalStateException("Single carry result permanently rejected", throwable);
            }
            finally {
                carryWrite.closed = true;
                OwnerRepairContinuation.burnPrewrite(track.binding);
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void finishCost(CarryWrite carryWrite, boolean bl) {
        if (carryWrite == null) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            OwnerRepairContinuation.burnPrewrite(carryWrite.track.binding);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            if (bl) {
                throw new IllegalStateException("Single carry ended; cost return, Damage and normal seal remain forbidden");
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean deferEntry(Object object, int n) {
        ItemStack itemStack;
        block12: {
            block11: {
                if (!(object instanceof ItemStack)) break block11;
                itemStack = (ItemStack)object;
                if (OwnerRepairContinuation.caller(OwnerFullRepairGuard.class, "changed")) break block12;
            }
            return false;
        }
        Binding binding = OwnerRepairContinuation.binding(itemStack);
        if (binding == null) {
            return false;
        }
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                Entry entry = binding.active;
                if (entry == null || entry.closed || entry.head || n != 20 && n != 21 || !OwnerRepairContinuation.valid(binding)) {
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return false;
                }
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
                    List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class && stackFrame.getDeclaringClass() != OwnerFullRepairGuard.class).limit(2L).toList();
                    return list.size() == 2 && list.get(0).getDeclaringClass() == ItemStack.class && list.get(0).getMethodName().contains(n == 20 ? "ftboss$fullRepairVoidMutation" : "ftboss$fullRepairReturnedMutation") && list.get(1).getDeclaringClass() == ItemStack.class && OwnerStatueWearBoundary.original(entry.kind, list.get(1).getMethodName(), list.get(1).getDescriptor());
                });
                if (!bl) {
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return false;
                }
                entry.head = true;
                // ** MonitorExit[var4_4] (shouldn't be in output)
                return true;
            }
        }
        catch (Throwable throwable) {
            OwnerRepairContinuation.retire(binding);
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void exited(Entry entry, Throwable throwable) {
        if (entry == null) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Binding binding = entry.binding;
            try {
                if (!(OwnerRepairContinuation.caller(OwnerStatueWearAdmission.class, "exited") && OwnerRepairContinuation.original(entry.kind, "ftboss$statueWearExitGuard") && !entry.closed && binding.active == entry && entry.head && throwable == null && OwnerRepairContinuation.valid(binding))) {
                    OwnerRepairContinuation.retire(binding);
                }
            }
            catch (Throwable throwable2) {
                OwnerRepairContinuation.retire(binding);
            }
            finally {
                entry.closed = true;
                if (binding.active == entry) {
                    binding.active = entry.previous;
                }
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static IdentityRead identityRead(ItemStack itemStack, CompoundTag compoundTag) {
        Binding binding = OwnerRepairContinuation.binding(itemStack);
        if (binding == null) {
            return null;
        }
        try {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerRepairContinuation.class && stackFrame.getDeclaringClass() != OwnerStatueTaskWrites.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && Set.of("weaponId", "ensureTracked").contains(stackFrame.getMethodName())).orElse(false));
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                IntArrayTag intArrayTag;
                Object object;
                block10: {
                    block9: {
                        if (!bl || !OwnerRepairContinuation.valid(binding) || itemStack.m_41783_().m_128469_("FTBossWeaponLifecycle") != compoundTag || !((object = compoundTag.m_128423_("Weapon")) instanceof IntArrayTag)) break block9;
                        intArrayTag = (IntArrayTag)object;
                        if (!READS.containsKey(compoundTag) && !READS.containsKey(intArrayTag)) break block10;
                    }
                    throw new IllegalStateException("Detached published identity read");
                }
                object = new IdentityRead(binding, compoundTag, intArrayTag);
                READS.put(compoundTag, (IdentityRead)object);
                READS.put(intArrayTag, (IdentityRead)object);
                // ** MonitorExit[var4_5] (shouldn't be in output)
                return object;
            }
        }
        catch (Throwable throwable) {
            OwnerRepairContinuation.retire(binding);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean identityAlias(Object object, Object object2) {
        int[] nArray;
        block11: {
            block10: {
                if (!(object2 instanceof int[])) break block10;
                nArray = (int[])object2;
                if (OwnerRepairContinuation.caller(OwnerFullRepairGuard.class, "alias")) break block11;
            }
            return false;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            IdentityRead identityRead = READS.get(object);
            if (identityRead == null) {
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return false;
            }
            try {
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == CompoundTag.class && (stackFrame.getDescriptor().equals("(Ljava/lang/String;)Ljava/util/UUID;") || stackFrame.getDescriptor().equals("(Ljava/lang/String;)Z"))));
                if (!bl || !OwnerRepairContinuation.valid(identityRead.binding) || identityRead.array != null && identityRead.array != nArray || !Arrays.equals(nArray, identityRead.binding.before.m_128469_("tag").m_128469_("FTBossWeaponLifecycle").m_128465_("Weapon"))) {
                    throw new IllegalStateException("Published identity alias differs");
                }
                identityRead.array = nArray;
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return true;
            }
            catch (Throwable throwable) {
                OwnerRepairContinuation.retire(identityRead.binding);
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void identityReturned(IdentityRead identityRead, Throwable throwable) {
        if (identityRead == null) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (READS.get(identityRead.receiver) == identityRead) {
                READS.remove(identityRead.receiver);
            }
            if (READS.get(identityRead.storage) == identityRead) {
                READS.remove(identityRead.storage);
            }
            try {
                if (throwable != null || identityRead.array == null || !OwnerRepairContinuation.valid(identityRead.binding)) {
                    OwnerRepairContinuation.retire(identityRead.binding);
                }
            }
            catch (Throwable throwable2) {
                OwnerRepairContinuation.retire(identityRead.binding);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void custodyChanged(Object object) {
        if (!OwnerRepairContinuation.caller(OwnerStatueWearAdmission.class, "custodyChanged")) {
            return;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            for (Binding binding : BINDINGS.values()) {
                Inventory inventory = (Inventory)binding.inventory.get();
                if (inventory != null && object != inventory.f_35974_ && object != inventory.f_35975_ && object != inventory.f_35976_) continue;
                OwnerRepairContinuation.retire(binding);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean noBinding(ItemStack itemStack) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return BINDINGS.get(itemStack) == null;
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
        private volatile Track track;
        private Entry active;

        private Binding(ItemStack itemStack, ServerPlayer serverPlayer, int n, String string, CompoundTag compoundTag, Object object, OwnerLineageRevocationStore.Lease lease, OwnerStatueTaskOrigin.PublishedPredecessor publishedPredecessor) {
            this.actual = new WeakReference<ItemStack>(itemStack);
            this.player = new WeakReference<ServerPlayer>(serverPlayer);
            this.inventory = new WeakReference<Inventory>(serverPlayer.m_150109_());
            this.server = serverPlayer.m_20194_();
            this.level = serverPlayer.m_9236_();
            this.owner = serverPlayer.m_20148_();
            this.weapon = lease.weapon;
            this.slot = n;
            this.rules = string;
            this.before = compoundTag.m_6426_();
            this.generation = object;
            this.lease = lease;
            this.predecessor = publishedPredecessor;
        }
    }

    static final class Track {
        private final Binding binding;
        private final Entry entry;
        private final CompoundTag record;
        private volatile TrackPhase phase = TrackPhase.QUOTED;
        private OwnerLineageRevocationStore store;
        private boolean storeTaken;
        private final OwnerStatueWearPlan.Step carry;
        private CarryWrite writing;

        private Track(Binding binding, Entry entry, CompoundTag compoundTag, OwnerStatueWearPlan.Step step) {
            this.binding = binding;
            this.entry = entry;
            this.carry = step;
            this.record = compoundTag.m_6426_();
            this.record.m_128362_("Track", UUID.randomUUID());
            this.record.m_128405_("Requested", entry.requested);
            this.record.m_128405_("Maximum", entry.plan.expectedMaximum());
            this.record.m_128359_("Authority", "NEGATIVE_ONLY_NO_WRITE_LEASE_ROOT_REPAIR_OR_GENERATION");
        }

        private boolean unchanged() {
            return this.atExpected(this.binding.before) && this.entry.plan.cursor.equals((Object)this.binding.before);
        }

        private boolean atExpected(CompoundTag compoundTag) {
            ItemStack itemStack = (ItemStack)this.binding.actual.get();
            ServerPlayer serverPlayer = (ServerPlayer)this.binding.player.get();
            return this.phase != TrackPhase.BURNED && this.binding.track == this && this.binding.prewriteTaken && this.binding.rejected && this.binding.thread == Thread.currentThread() && this.binding.server != null && this.binding.server.m_18695_() && serverPlayer != null && itemStack != null && serverPlayer.m_20194_() == this.binding.server && serverPlayer.m_9236_() == this.binding.level && serverPlayer.m_150109_() == this.binding.inventory.get() && this.binding.owner.equals(serverPlayer.m_20148_()) && serverPlayer.m_150109_().m_8020_(this.binding.slot) == itemStack && itemStack.m_41613_() == 1 && (this.binding.slot == 40 ? serverPlayer.m_21206_() == itemStack : serverPlayer.m_150109_().f_35977_ == this.binding.slot && serverPlayer.m_21205_() == itemStack) && this.binding.active == this.entry && !this.entry.closed && this.entry.kind == 8 && this.entry.plan != null && !this.entry.plan.rejected && compoundTag.equals((Object)itemStack.m_41739_(new CompoundTag())) && this.binding.rules.equals(OwnerStatueTaskPlan.rules(itemStack)) && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) && OwnerRepairGenerationAuthority.current(itemStack) == this.binding.generation && OwnerFullRepairGuard.lease(itemStack) == this.binding.lease && !OwnerLineageRevocationStore.locallyLive(this.binding.lease);
        }

        ServerPlayer ownerPlayer() throws IOException {
            if (!OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack") || !this.unchanged()) {
                throw new IOException("Detached prewrite owner");
            }
            return (ServerPlayer)this.binding.player.get();
        }

        CompoundTag ownerRecord() throws IOException {
            if (!OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack") || this.phase != TrackPhase.QUOTED || !this.unchanged()) {
                throw new IOException("Detached prewrite record");
            }
            return this.record.m_6426_();
        }

        void ownerPrepared(OwnerLineageRevocationStore ownerLineageRevocationStore) throws IOException {
            if (!(OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack") && this.phase == TrackPhase.QUOTED && ownerLineageRevocationStore != null && ownerLineageRevocationStore.owns(this.binding.lease) && this.unchanged())) {
                throw new IOException("Prewrite owner preparation changed");
            }
            this.store = ownerLineageRevocationStore;
            this.phase = TrackPhase.PREPARING;
        }

        CompoundTag takeForStore(OwnerLineageRevocationStore ownerLineageRevocationStore) throws IOException {
            if (!OwnerRepairContinuation.caller(OwnerLineageRevocationStore.class, "consumePrewriteTrack") || this.storeTaken) {
                throw new IOException("Prewrite store ticket repeated");
            }
            this.storeTaken = true;
            if (this.store != ownerLineageRevocationStore || this.phase != TrackPhase.PREPARING || !this.unchanged()) {
                throw new IOException("Detached prewrite store consumption");
            }
            return this.record.m_6426_();
        }

        void reserved() throws IOException {
            if (!(OwnerRepairContinuation.caller(OwnerStatueTaskService.class, "reservePrewriteTrack") && this.phase == TrackPhase.PREPARING && this.storeTaken && this.unchanged())) {
                throw new IOException("Prewrite reservation changed");
            }
            this.phase = TrackPhase.TRACK_RESERVED;
        }

        private void burn() {
            this.phase = TrackPhase.BURNED;
        }
    }

    static final class Entry {
        private final Binding binding;
        private final Entry previous;
        private final int kind;
        private final int depth;
        private final int requested;
        private final OwnerStatueWearPlan plan;
        private boolean head;
        private boolean closed;

        private Entry(Binding binding, int n, int n2, OwnerStatueWearPlan ownerStatueWearPlan) {
            this.binding = binding;
            this.previous = binding.active;
            this.kind = n;
            this.requested = n2;
            this.plan = ownerStatueWearPlan;
            this.depth = this.previous == null ? 1 : this.previous.depth + 1;
        }
    }

    private static enum TrackPhase {
        QUOTED,
        PREPARING,
        TRACK_RESERVED,
        BURNED;

    }

    static final class CarryWrite {
        private final Track track;
        private final CompoundTag receiver;
        private final Object storage;
        private boolean taken;
        private boolean closed;
        private int observations;

        private CarryWrite(Track track, CompoundTag compoundTag, Object object) {
            this.track = track;
            this.receiver = compoundTag;
            this.storage = object;
        }
    }

    static final class IdentityRead {
        private final Binding binding;
        private final CompoundTag receiver;
        private final Object storage;
        private int[] array;

        private IdentityRead(Binding binding, CompoundTag compoundTag, Object object) {
            this.binding = binding;
            this.receiver = compoundTag;
            this.storage = object;
        }
    }
}

