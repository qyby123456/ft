/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.storage.PlayerDataStorage
 */
package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueTaskPlan;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.item.OwnerStatueWearBoundary;
import net.farmingtales.ftboss.item.OwnerStatueWearPlan;
import net.farmingtales.ftboss.item.OwnerStatueWearWrites;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.PlayerDataStorage;

public final class OwnerStatueWearAdmission {
    private static final Map<ItemStack, Accepted> ACCEPTED = new WeakHashMap<ItemStack, Accepted>();
    private static final Object ACCEPTED_LOCK = new Object();
    private static final Map<ItemStack, WeakReference<Root>> LIVE_ROOTS = new WeakHashMap<ItemStack, WeakReference<Root>>();

    private OwnerStatueWearAdmission() {
    }

    private static boolean originalCaller(int n, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class && stackFrame.getDeclaringClass() != OwnerNativeSemanticAuthority.class && stackFrame.getDeclaringClass() != OwnerStatueTaskService.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getDeclaringClass() == (n == 0 ? Inventory.class : ItemStack.class) && list.get(0).getMethodName().equals(string) && list.get(1).getDeclaringClass() == list.get(0).getDeclaringClass() && OwnerStatueWearBoundary.original(n, list.get(1).getMethodName(), list.get(1).getDescriptor());
        });
    }

    public static Load loadEntered(Inventory inventory, ListTag listTag) {
        ServerPlayer serverPlayer;
        Player player;
        if (!(OwnerStatueWearAdmission.originalCaller(0, "ftboss$statueWearLoadGuard") && inventory != null && listTag != null && listTag.getClass() == ListTag.class && (player = inventory.f_35978_) instanceof ServerPlayer && (serverPlayer = (ServerPlayer)player).m_150109_() == inventory && serverPlayer.m_20194_() != null && serverPlayer.m_20194_().m_18695_() && listTag.size() <= 256)) {
            return null;
        }
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class).limit(6L).toList();
            return list.size() == 6 && list.get(0).getDeclaringClass() == Inventory.class && list.get(0).getMethodName().equals("ftboss$statueWearLoadGuard") && list.get(1).getDeclaringClass() == Inventory.class && OwnerStatueWearBoundary.original(0, list.get(1).getMethodName(), list.get(1).getDescriptor()) && list.get(2).getDeclaringClass() == Player.class && list.get(2).getMethodName().equals("readAdditionalSaveData") && list.get(3).getDeclaringClass() == ServerPlayer.class && list.get(3).getMethodName().equals("readAdditionalSaveData") && list.get(4).getDeclaringClass() == Entity.class && list.get(4).getMethodName().equals("load") && list.get(5).getDeclaringClass() == PlayerDataStorage.class && list.get(5).getMethodName().equals("load");
        });
        if (!bl) {
            return null;
        }
        Load load = new Load(inventory, serverPlayer, serverPlayer.m_20194_());
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag;
            Tag tag = listTag.get(i);
            if (!(tag instanceof CompoundTag) || (compoundTag = (CompoundTag)tag).getClass() != CompoundTag.class) {
                return null;
            }
            load.inputs.put(compoundTag, NativeExpectedRawFields.observe((Tag)compoundTag));
        }
        return load;
    }

    public static void loadBound(Load load, CompoundTag compoundTag, ItemStack itemStack) {
        if (!(OwnerStatueWearAdmission.originalCaller(0, "ftboss$statueWearBindGuard") && load != null && !load.closed && load.thread == Thread.currentThread() && load.player.m_150109_() == load.inventory && load.player.m_20194_() == load.server && load.server.m_18695_() && load.owner.equals(load.player.m_20148_()) && load.inputs.containsKey(compoundTag))) {
            return;
        }
        OwnerNativeSemanticAuthority.wearInputLoaded(load, compoundTag, itemStack);
    }

    static Origin quotedOrigin(Load load, CompoundTag compoundTag, ItemStack itemStack, OwnerNativeSemanticAuthority.WeaponInput weaponInput, FoodWeaponExpectedTrace.Value value) {
        if (!StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("wearInputLoaded")).orElse(false)).booleanValue()) {
            return null;
        }
        FoodWeaponExpectedTrace.Value value2 = load.inputs.get(compoundTag);
        FoodWeaponExpectedTrace.Value value3 = FoodWeaponExpectedTrace.field(value2, "Slot");
        if (value3 == null || value3.kind != 1 || !FoodWeaponExpectedTrace.same(value2, NativeExpectedRawFields.observe((Tag)compoundTag))) {
            return null;
        }
        if (FoodWeaponExpectedTrace.field(value2, "tag") == null) {
            return null;
        }
        FoodWeaponExpectedTrace.Value value4 = FoodWeaponExpectedTrace.compound(new String[]{"Count", "id", "tag"}, new FoodWeaponExpectedTrace.Value[]{FoodWeaponExpectedTrace.field(value2, "Count"), FoodWeaponExpectedTrace.field(value2, "id"), FoodWeaponExpectedTrace.field(value2, "tag")});
        if (!FoodWeaponExpectedTrace.same(value4, value)) {
            return null;
        }
        int n = (int)value3.bits & 0xFF;
        if (n == 150) {
            n = 40;
        } else if (n >= 100 && n < 104) {
            n -= 64;
        } else if (n < 0 || n >= 36) {
            return null;
        }
        Origin origin = new Origin(load, itemStack, weaponInput, value, n);
        load.origins.add(origin);
        return origin;
    }

    public static void loadExited(Load load, Throwable throwable) {
        if (!OwnerStatueWearAdmission.originalCaller(0, "ftboss$statueWearLoadExitGuard") || load == null || load.closed || load.thread != Thread.currentThread()) {
            return;
        }
        load.closed = true;
        if (throwable != null || load.player.m_150109_() != load.inventory || load.player.m_20194_() != load.server || !load.server.m_18695_() || !load.owner.equals(load.player.m_20148_())) {
            return;
        }
        for (Origin origin : load.origins) {
            ItemStack itemStack = (ItemStack)origin.actual.get();
            origin.loaded = itemStack != null && load.inventory.m_8020_(origin.slot) == itemStack && OwnerFullRepairGuard.permits(itemStack);
        }
        load.inputs.clear();
        load.origins.clear();
    }

    private static boolean physical(Origin origin, ServerPlayer serverPlayer, ItemStack itemStack) {
        Inventory inventory = (Inventory)origin.inventory.get();
        if (serverPlayer == null || origin.player.get() != serverPlayer || inventory == null || serverPlayer.m_150109_() != inventory || origin.server != serverPlayer.m_20194_() || !origin.server.m_18695_() || origin.thread != Thread.currentThread() || !origin.owner.equals(serverPlayer.m_20148_()) || inventory.m_8020_(origin.slot) != itemStack) {
            return false;
        }
        return origin.slot == 40 ? serverPlayer.m_21206_() == itemStack : inventory.f_35977_ == origin.slot && serverPlayer.m_21205_() == itemStack;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Frame entered(ItemStack itemStack, Object object, int n, int n2) {
        Root root;
        Object object2;
        Object object3;
        if (OwnerRepairContinuation.noBinding(itemStack) && OwnerNativeSemanticAuthority.wearOrigin(itemStack) == null) {
            return null;
        }
        if (NativeFoodWeaponWear.eligible(itemStack)) {
            return null;
        }
        if (!OwnerStatueWearAdmission.originalCaller(n, "ftboss$statueWearEnterGuard")) {
            return null;
        }
        OwnerRepairContinuation.Entry entry = OwnerRepairContinuation.entered(itemStack, object, n, n2);
        if (entry != null) {
            return new Frame(entry);
        }
        Origin origin = OwnerNativeSemanticAuthority.wearOrigin(itemStack);
        if (origin == null || origin.thread != Thread.currentThread()) {
            return null;
        }
        if (object instanceof UseOnContext) {
            object3 = (UseOnContext)object;
            object = object3.m_43723_();
        }
        if ((object3 = origin.active) == null) {
            if (!(n != 9 && !origin.spent && origin.loaded && object instanceof ServerPlayer && OwnerStatueWearAdmission.physical(origin, object2 = (ServerPlayer)object, itemStack) && OwnerRepairGenerationAuthority.statueTaskObjectPolicy(itemStack) && OwnerFullRepairGuard.permits(itemStack))) {
                return null;
            }
            origin.spent = true;
            root = new Root(origin, (ServerPlayer)object2, itemStack);
        } else {
            root = object3.root;
            if (root.phase != Phase.ORIGINAL || root.actual != itemStack || object != null && object != root.player || object3.depth >= 128) {
                root.rejected = true;
                return null;
            }
        }
        object2 = new Frame(root, (Frame)object3, n, n2);
        origin.active = object2;
        boolean bl = false;
        try {
            Object object4;
            if (object3 == null) {
                root.outer = object2;
                object4 = ACCEPTED_LOCK;
                synchronized (object4) {
                    LIVE_ROOTS.put(itemStack, new WeakReference<Root>(root));
                }
                object4 = new Reading(root, (Frame)object2, true);
                CompoundTag compoundTag = OwnerNativeSemanticAuthority.wearImage(origin, itemStack, (Reading)object4);
                if (compoundTag == null || !FoodWeaponExpectedTrace.same(origin.expected, NativeExpectedRawFields.observe((Tag)compoundTag))) {
                    Frame frame = null;
                    return frame;
                }
                root.before = (CompoundTag)FoodWeaponExpectedTrace.materialize(origin.expected);
                root.rules = OwnerStatueTaskPlan.rules(itemStack);
                root.damage = itemStack.m_41773_();
                root.maximum = itemStack.m_41776_();
                root.broken = FoodWeaponLifecycle.isBroken(itemStack);
                UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
                if (uUID == null || !FoodWeaponLifecycle.managed(itemStack) || root.maximum <= 0) {
                    Frame frame = null;
                    return frame;
                }
                try {
                    OwnerStatueTaskService.requireOriginalWearNotRetired(root.player, uUID);
                }
                catch (IOException iOException) {
                    Frame frame = null;
                    if (!bl) {
                        root.rejected = true;
                        origin.active = object3;
                    }
                    return frame;
                }
                int n3 = 0;
                for (int i = 0; i < root.player.m_150109_().m_6643_(); ++i) {
                    if (!uUID.equals(FoodWeaponLifecycle.weaponId(root.player.m_150109_().m_8020_(i)))) continue;
                    ++n3;
                }
                if (n3 != 1 || !OwnerStatueWearAdmission.physical(origin, root.player, itemStack) || OwnerNativeSemanticAuthority.wearImage(origin, itemStack, (Reading)object4) == null) {
                    Frame frame = null;
                    return frame;
                }
                root.plan = OwnerStatueWearPlan.original(itemStack, root.before, root.maximum);
                try {
                    if (root.plan != null && !OwnerStatueTaskService.originalWearPlanUnused(root.player, uUID)) {
                        root.plan = null;
                    }
                }
                catch (IOException iOException) {
                    root.plan = null;
                }
                root.phase = Phase.ORIGINAL;
            }
            if (n >= 7 && n <= 9) {
                ++root.wears;
            }
            OwnerStatueWearAdmission.trace((Frame)object2, true, null);
            bl = true;
            object4 = object2;
            return object4;
        }
        finally {
            if (!bl) {
                root.rejected = true;
                origin.active = object3;
            }
        }
    }

    private static boolean serviceCaller(String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueTaskService.class && stackFrame.getMethodName().equals(string)).orElse(false));
    }

    static Accepted validated(Terminal terminal) {
        if (!OwnerStatueWearAdmission.serviceCaller("recordOriginalWear") || terminal == null || !terminal.handedOff) {
            return null;
        }
        Root root = terminal.root;
        if (root.phase != Phase.TERMINAL || root.origin.active != root.outer || root.rejected || root.writing != null || root.plan == null || !root.plan.taskStartEligible(terminal.primary) || !OwnerStatueWearAdmission.physical(root.origin, root.player, root.actual) || root.player.m_9236_() != root.level || !OwnerStatueWearAdmission.originalCaller(root.outer.kind, "ftboss$statueWearExitGuard")) {
            return null;
        }
        CompoundTag compoundTag = OwnerNativeSemanticAuthority.wearImage(root.origin, root.actual, new Reading(root, root.outer, false));
        if (compoundTag == null || !root.plan.cursor.equals((Object)compoundTag) || !root.rules.equals(OwnerStatueTaskPlan.rules(root.actual))) {
            return null;
        }
        return new Accepted(root, terminal.primary);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void activate(Accepted accepted, Terminal terminal) {
        CompoundTag compoundTag;
        if (!OwnerStatueWearAdmission.serviceCaller("recordOriginalWear") || accepted == null || accepted.spent || terminal == null) {
            throw new IllegalStateException("Detached validated wear publication");
        }
        Root root = terminal.root;
        CompoundTag compoundTag2 = compoundTag = root.phase == Phase.TERMINAL ? OwnerNativeSemanticAuthority.wearImage(root.origin, root.actual, new Reading(root, root.outer, false)) : null;
        if (!(root.origin.active == root.outer && !root.rejected && root.plan != null && root.plan.taskStartEligible(terminal.primary) && root.writing == null && accepted.startEligible() && accepted.call.equals(root.call) && accepted.actual.get() == root.actual && accepted.player.get() == root.player && OwnerStatueWearAdmission.physical(root.origin, root.player, root.actual) && accepted.expected.equals((Object)compoundTag) && OwnerStatueWearAdmission.originalCaller(root.outer.kind, "ftboss$statueWearExitGuard"))) {
            accepted.spent = true;
            return;
        }
        Object object = ACCEPTED_LOCK;
        synchronized (object) {
            if (!root.rejected && !root.plan.rejected) {
                ACCEPTED.put(root.actual, accepted);
            } else {
                accepted.spent = true;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean hasStartCandidate(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == StatueRepairMutation.class && stackFrame.getMethodName().equals("commit")).orElse(false));
        if (!bl || serverPlayer == null || itemStack == null) {
            return false;
        }
        Object object = ACCEPTED_LOCK;
        synchronized (object) {
            Accepted accepted = ACCEPTED.get(itemStack);
            return accepted != null && !accepted.spent && accepted.startEligible() && accepted.actual.get() == itemStack && accepted.player.get() == serverPlayer && accepted.inventory.get() == serverPlayer.m_150109_() && accepted.server == serverPlayer.m_20194_() && accepted.server.m_18695_() && accepted.owner.equals(serverPlayer.m_20148_()) && accepted.slot == n && n >= 0 && n < serverPlayer.m_150109_().m_6643_() && serverPlayer.m_150109_().m_8020_(n) == itemStack && (n == 40 ? serverPlayer.m_21206_() == itemStack : serverPlayer.m_150109_().f_35977_ == n && serverPlayer.m_21205_() == itemStack);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Accepted takeForStart(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        Accepted accepted;
        block9: {
            block8: {
                if (!OwnerStatueWearAdmission.serviceCaller("prepareStart")) {
                    return null;
                }
                Object object = ACCEPTED_LOCK;
                synchronized (object) {
                    accepted = ACCEPTED.remove(itemStack);
                }
                if (accepted == null || accepted.spent) {
                    return null;
                }
                accepted.spent = true;
                if (!accepted.startEligible() || accepted.actual.get() != itemStack || accepted.player.get() != serverPlayer || accepted.inventory.get() != serverPlayer.m_150109_() || accepted.server != serverPlayer.m_20194_() || !accepted.server.m_18695_() || !accepted.owner.equals(serverPlayer.m_20148_()) || accepted.slot != n || serverPlayer.m_150109_().m_8020_(n) != itemStack || (n != 40 ? serverPlayer.m_150109_().f_35977_ != n || serverPlayer.m_21205_() != itemStack : serverPlayer.m_21206_() != itemStack)) break block8;
                if (accepted.expected.equals((Object)itemStack.m_41739_(new CompoundTag())) && accepted.rules.equals(OwnerStatueTaskPlan.rules(itemStack))) break block9;
            }
            return null;
        }
        return accepted;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void custodyChanged(Object object) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == NonNullList.class && stackFrame.getMethodName().contains("ftboss$statueCustody")).orElse(false));
        if (!bl) {
            return;
        }
        OwnerStatueTaskOrigin.custodyChanged(object);
        OwnerRepairContinuation.custodyChanged(object);
        Object object2 = ACCEPTED_LOCK;
        synchronized (object2) {
            LIVE_ROOTS.entrySet().removeIf(entry -> {
                Root root = (Root)((WeakReference)entry.getValue()).get();
                if (root == null || root.phase == Phase.CLOSED) {
                    return true;
                }
                Inventory inventory = (Inventory)root.origin.inventory.get();
                if (inventory == null || object == inventory.f_35974_ || object == inventory.f_35975_ || object == inventory.f_35976_) {
                    root.rejected = true;
                    if (root.plan != null) {
                        root.plan.rejected = true;
                    }
                }
                return false;
            });
            ACCEPTED.entrySet().removeIf(entry -> {
                boolean bl;
                Accepted accepted = (Accepted)entry.getValue();
                Inventory inventory = (Inventory)accepted.inventory.get();
                boolean bl2 = bl = inventory == null || object == inventory.f_35974_ || object == inventory.f_35975_ || object == inventory.f_35976_;
                if (bl) {
                    accepted.spent = true;
                }
                return bl;
            });
        }
    }

    private static Root activeRoot(ItemStack itemStack) {
        Origin origin = OwnerNativeSemanticAuthority.wearOrigin(itemStack);
        return origin == null || origin.active == null || origin.thread != Thread.currentThread() ? null : origin.active.root;
    }

    private static CompoundTag prefix(Root root) {
        Reading reading = new Reading(root, root.origin.active, false);
        reading.prefix = true;
        return OwnerNativeSemanticAuthority.wearImage(root.origin, root.actual, reading);
    }

    private static boolean writer(int n) {
        String string;
        List list = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class && stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).toList());
        if (list.isEmpty()) {
            return false;
        }
        StackWalker.StackFrame stackFrame2 = (StackWalker.StackFrame)list.get(0);
        if (n == 3) {
            if (!OwnerStatueWearBoundary.writer(1, stackFrame2)) {
                return false;
            }
            StackWalker.StackFrame stackFrame3 = list.stream().filter(stackFrame -> stackFrame.getDeclaringClass() != ItemStack.class).findFirst().orElse(null);
            return stackFrame3 != null && (stackFrame3.getDeclaringClass() == FoodWeaponSourceLedger.class && stackFrame3.getMethodName().equals("nativeAppliedDamage") || stackFrame3.getDeclaringClass() == FoodWeaponLifecycle.class && stackFrame3.getMethodName().equals("setInternalDamage") && list.stream().anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && stackFrame.getMethodName().equals("afterDamageChange")));
        }
        switch (n) {
            case 1: {
                String string2 = "durabilityCost";
                break;
            }
            case 2: {
                String string2 = "ensureTracked";
                break;
            }
            case 4: {
                String string2 = "loseSocket";
                break;
            }
            case 5: {
                String string2 = "loseMaterial";
                break;
            }
            case 6: 
            case 7: 
            case 8: {
                String string2 = "afterDamageChange";
                break;
            }
            case 9: {
                String string2 = "finishHurtOriginal";
                break;
            }
            default: {
                String string2 = string = "";
            }
        }
        if (stackFrame2.getDeclaringClass() != (n == 1 ? WeaponMaterialBuild.class : FoodWeaponLifecycle.class) || !stackFrame2.getMethodName().equals(string)) {
            return false;
        }
        StackWalker.StackFrame stackFrame4 = list.stream().filter(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class).findFirst().orElse(null);
        if (stackFrame4 == null) {
            return false;
        }
        int n2 = n == 1 ? 2 : (n == 9 ? 5 : 4);
        return OwnerStatueWearBoundary.writer(n2, stackFrame4) || n == 2 && (OwnerStatueWearBoundary.writer(3, stackFrame4) || OwnerStatueWearBoundary.writer(5, stackFrame4));
    }

    public static Write beforeWrite(ItemStack itemStack, int n, Object object, String string, Tag tag) {
        Root root = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (root == null || root.plan == null) {
            return null;
        }
        try {
            Write write;
            ItemStack itemStack2;
            if (!OwnerStatueWearAdmission.writer(n) || root.phase != Phase.ORIGINAL || root.writing != null || root.rejected || root.plan.rejected || !root.plan.cursor.equals((Object)OwnerStatueWearAdmission.prefix(root))) {
                throw new IllegalStateException("Detached original prefix write");
            }
            OwnerStatueWearPlan.Step step = root.plan.next(n, string, tag);
            CompoundTag compoundTag = itemStack.m_41783_();
            ItemStack itemStack3 = n == 8 ? itemStack : (itemStack2 = step.path().isEmpty() ? compoundTag : compoundTag.m_128469_(step.path()));
            if (n == 4) {
                itemStack2 = ((CompoundTag)itemStack2).m_128437_(step.key(), 10);
            }
            if (n == 3) {
                object = itemStack;
            }
            if (n != 3 && itemStack2 != object) {
                throw new IllegalStateException("Wear writer receiver differs from original storage");
            }
            Object object2 = n == 3 ? compoundTag : object;
            root.writing = write = new Write(root, step, object, object2);
            return write;
        }
        catch (Throwable throwable) {
            root.plan.rejected = true;
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void afterWrite(Write write, Throwable throwable) {
        if (write == null) {
            return;
        }
        Root root = write.root;
        try {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueWearWrites.class).orElse(false));
            if (!bl || write.closed || root.writing != write || root.phase != Phase.ORIGINAL) {
                throw new IllegalStateException("Wear write token replay");
            }
            CompoundTag compoundTag = OwnerStatueWearAdmission.prefix(root);
            if (write.step.after().equals((Object)compoundTag)) {
                root.plan.advanced(write.step);
                if (write.step.site() == 8 && throwable == null) {
                    root.plan.swallowedRollback = true;
                }
            } else if (throwable == null || !write.before.equals((Object)compoundTag)) {
                throw new IllegalStateException("Unplanned partial original mutation");
            }
            if (!OwnerFullRepairGuard.watchWearPrefix(root.actual)) {
                throw new IllegalStateException("Original alias watch incomplete");
            }
        }
        catch (Throwable throwable2) {
            root.plan.rejected = true;
        }
        finally {
            write.closed = true;
            if (root.writing == write) {
                root.writing = null;
            }
        }
    }

    static boolean watching(ItemStack itemStack) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFullRepairGuard.class && stackFrame.getMethodName().equals("watchWearPrefix")).orElse(false));
        Root root = bl ? OwnerStatueWearAdmission.activeRoot(itemStack) : null;
        return root != null && root.phase == Phase.ORIGINAL && root.writing != null && root.plan != null && !root.plan.rejected;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void mutationNoticed(Object object, Object object2, int n) {
        Object object3;
        OwnerStatueTaskOrigin.changed(object, object2, n);
        OwnerRepairContinuation.carryMutation(object, object2, n);
        if (!(object instanceof ItemStack)) {
            return;
        }
        ItemStack itemStack = (ItemStack)object;
        Object object4 = ACCEPTED_LOCK;
        synchronized (object4) {
            object3 = ACCEPTED.remove(itemStack);
            if (object3 != null) {
                ((Accepted)object3).spent = true;
            }
        }
        object4 = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (object4 == null) {
            object3 = ACCEPTED_LOCK;
            synchronized (object3) {
                Root root;
                WeakReference<Root> weakReference = LIVE_ROOTS.get(itemStack);
                Root root2 = root = weakReference == null ? null : (Root)weakReference.get();
                if (root != null) {
                    root.rejected = true;
                }
            }
            return;
        }
        if (((Root)object4).plan == null || ((Root)object4).phase == Phase.CLOSED) {
            return;
        }
        if (((Root)object4).phase != Phase.ORIGINAL) {
            ((Root)object4).plan.rejected = true;
            return;
        }
        object3 = ((Root)object4).writing;
        if (!(object3 == null || ((Write)object3).closed || object2 != ((Write)object3).receiver && object2 != ((Write)object3).storage || n != 20 && n != 21 || !StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.takeWhile(stackFrame -> stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).toList();
            return list.stream().allMatch(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == OwnerStatueWearAdmission.class || stackFrame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class || stackFrame.getDeclaringClass() == ItemStack.class || stackFrame.getDeclaringClass() == Item.class || stackFrame.getDeclaringClass() == CompoundTag.class || stackFrame.getDeclaringClass() == ListTag.class);
        }).booleanValue())) {
            return;
        }
        if (object2 == object && (n == 20 || n == 21)) {
            Frame frame = ((Root)object4).origin.active;
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
                List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class).limit(2L).toList();
                return list.size() == 2 && (OwnerStatueWearBoundary.writer(6, list.get(0)) || OwnerStatueWearBoundary.writer(7, list.get(0))) && OwnerStatueWearBoundary.original(frame.kind, list.get(1).getMethodName(), list.get(1).getDescriptor());
            });
            if (bl) {
                return;
            }
        }
        if (object2 == null && n == 1) {
            return;
        }
        ((Root)object4).plan.rejected = true;
    }

    static void cost(ItemStack itemStack, int n) {
        Root root = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (root == null || root.plan == null) {
            return;
        }
        try {
            if (!OwnerStatueWearAdmission.writer(1) || root.origin.active.kind != 8 || root.origin.active.requested != n || !root.plan.cursor.equals((Object)OwnerStatueWearAdmission.prefix(root))) {
                throw new IllegalStateException("Detached material cost");
            }
            root.plan.cost(n);
        }
        catch (Throwable throwable) {
            root.plan.rejected = true;
        }
    }

    static void damage(ItemStack itemStack, int n, int n2) {
        Root root = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (root == null || root.plan == null) {
            return;
        }
        try {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class && stackFrame.getMethodName().equals("nativeAppliedDamage")).orElse(false));
            if (!bl || root.origin.active.kind != 8 || !root.plan.cursor.equals((Object)OwnerStatueWearAdmission.prefix(root))) {
                throw new IllegalStateException("Detached applied damage");
            }
            root.plan.damage(n, n2);
        }
        catch (Throwable throwable) {
            root.plan.rejected = true;
        }
    }

    static void rollback(ItemStack itemStack, CompoundTag compoundTag, Throwable throwable) {
        Root root = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (root == null || root.plan == null) {
            return;
        }
        try {
            if (!OwnerStatueWearAdmission.writer(8) || !root.plan.cursor.equals((Object)OwnerStatueWearAdmission.prefix(root))) {
                throw new IllegalStateException("Detached swallowed rollback");
            }
            root.plan.rollback(compoundTag, throwable);
        }
        catch (Throwable throwable2) {
            root.plan.rejected = true;
        }
    }

    static void finish(ItemStack itemStack, ServerPlayer serverPlayer, boolean bl) {
        Root root = OwnerStatueWearAdmission.activeRoot(itemStack);
        if (root == null || root.plan == null) {
            return;
        }
        try {
            boolean bl2 = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerStatueWearAdmission.class && stackFrame.getDeclaringClass() != OwnerStatueWearWrites.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponLifecycle.class && stackFrame.getMethodName().equals("finishHurt")).orElse(false));
            boolean bl3 = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class).findFirst().map(stackFrame -> OwnerStatueWearBoundary.writer(5, stackFrame)).orElse(false));
            if (!(bl2 && bl3 && root.origin.active.kind == 8 && serverPlayer == root.player && root.plan.cursor.equals((Object)OwnerStatueWearAdmission.prefix(root)))) {
                throw new IllegalStateException("Detached original hurt finish");
            }
            if (bl) {
                root.plan.finished();
            } else {
                root.plan.finish(true);
            }
        }
        catch (Throwable throwable) {
            root.plan.rejected = true;
        }
    }

    private static void trace(Frame frame, boolean bl, Throwable throwable) {
        Root root = frame.root;
        if (root.trace.size() >= 256) {
            root.rejected = true;
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("Method", frame.kind);
        compoundTag.m_128405_("Requested", frame.requested);
        compoundTag.m_128405_("Depth", frame.depth);
        compoundTag.m_128379_("Entered", bl);
        compoundTag.m_128379_("ReturnedNormally", !bl && throwable == null);
        if (throwable != null) {
            compoundTag.m_128359_("PrimaryClass", throwable.getClass().getName());
        }
        root.trace.add((Object)compoundTag);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void exited(Frame frame, Throwable throwable) {
        if (frame != null && frame.continuation != null) {
            OwnerRepairContinuation.exited(frame.continuation, throwable);
            frame.closed = true;
            return;
        }
        if (frame == null || !OwnerStatueWearAdmission.originalCaller(frame.kind, "ftboss$statueWearExitGuard") || frame.closed || frame.root.origin.thread != Thread.currentThread()) {
            return;
        }
        Root root = frame.root;
        Origin origin = root.origin;
        if (origin.active != frame) {
            root.rejected = true;
            return;
        }
        try {
            OwnerStatueWearAdmission.trace(frame, false, throwable);
            if (frame.previous != null) {
                return;
            }
            if (root.wears == 0) {
                Object object;
                root.phase = Phase.CAPTURING;
                if (throwable == null && !root.rejected && (object = OwnerNativeSemanticAuthority.wearImage(origin, root.actual, new Reading(root, frame, true))) != null && root.before.equals(object)) {
                    origin.spent = false;
                }
                return;
            }
            root.phase = Phase.TERMINAL;
            Object object = new CompoundTag();
            object.m_128405_("Schema", 1);
            object.m_128362_("Call", root.call);
            object.m_128362_("OriginalLoad", origin.load);
            object.m_128362_("Owner", origin.owner);
            object.m_128405_("Slot", origin.slot);
            object.m_128359_("OriginalDimension", root.dimension);
            object.m_128359_("DimensionAtTerminalObservation", root.player.m_9236_().m_46472_().m_135782_().toString());
            object.m_128359_("Authority", "ORIGINAL_LOAD_CUSTODY_BOUND_WEAR_EVIDENCE_ONLY_NOT_TASK_OR_GENERATION_AUTHORITY");
            object.m_128379_("ReturnedNormally", throwable == null);
            if (throwable != null) {
                object.m_128359_("PrimaryClass", throwable.getClass().getName());
            }
            object.m_128379_("CustodyAtTerminal", OwnerStatueWearAdmission.physical(origin, root.player, root.actual) && root.player.m_9236_() == root.level);
            object.m_128379_("RejectedObservation", root.rejected);
            object.m_128405_("WearMethodEntriesNotVerifiedDamage", root.wears);
            object.m_128379_("MutationPlanVerified", false);
            object.m_128379_("CreatesTaskOrGeneration", false);
            object.m_128365_("AcceptedOriginalLoadBefore", (Tag)root.before.m_6426_());
            object.m_128359_("RulesBefore", root.rules);
            object.m_128405_("DamageBefore", root.damage);
            object.m_128405_("MaximumBefore", root.maximum);
            object.m_128379_("BrokenBefore", root.broken);
            object.m_128365_("OriginalCallTrace", (Tag)root.trace.m_6426_());
            try {
                CompoundTag compoundTag = OwnerNativeSemanticAuthority.wearImage(origin, root.actual, new Reading(root, frame, false));
                if (compoundTag == null) {
                    throw new IllegalStateException("Original wear final fields unavailable");
                }
                object.m_128365_("ObservedFinalImageNotExpected", (Tag)compoundTag);
                object.m_128379_("ObservedImageChanged", !root.before.equals((Object)compoundTag));
                object.m_128405_("CountAfter", root.actual.m_41613_());
                object.m_128405_("DamageAfter", root.actual.m_41773_());
                object.m_128405_("MaximumAfter", root.actual.m_41776_());
                object.m_128379_("BrokenAfter", FoodWeaponLifecycle.isBroken(root.actual));
                object.m_128359_("RulesAfterObservation", OwnerStatueTaskPlan.rules(root.actual));
            }
            catch (Throwable throwable2) {
                object.m_128359_("FinalObservationFailureClass", throwable2.getClass().getName());
            }
            try {
                OwnerStatueTaskService.recordOriginalWear(new Terminal(root, throwable, (CompoundTag)object));
            }
            catch (IOException iOException) {
                OwnerStatueTaskService.unavailable(root.player, iOException);
            }
            catch (LinkageError | RuntimeException throwable3) {
                OwnerStatueTaskService.unavailable(root.player, new IOException("Original wear evidence checkpoint failed", throwable3));
            }
        }
        finally {
            frame.closed = true;
            origin.active = frame.previous;
            if (frame.previous == null) {
                root.phase = Phase.CLOSED;
                Object object = ACCEPTED_LOCK;
                synchronized (object) {
                    LIVE_ROOTS.remove(root.actual);
                }
            }
        }
    }

    public static final class Load {
        private final Inventory inventory;
        private final ServerPlayer player;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID identity = UUID.randomUUID();
        private final Thread thread = Thread.currentThread();
        private final IdentityHashMap<CompoundTag, FoodWeaponExpectedTrace.Value> inputs = new IdentityHashMap();
        private final List<Origin> origins = new ArrayList<Origin>();
        private boolean closed;

        private Load(Inventory inventory, ServerPlayer serverPlayer, MinecraftServer minecraftServer) {
            this.inventory = inventory;
            this.player = serverPlayer;
            this.server = minecraftServer;
            this.owner = serverPlayer.m_20148_();
        }
    }

    static final class Origin {
        final OwnerNativeSemanticAuthority.WeaponInput input;
        final FoodWeaponExpectedTrace.Value expected;
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID load;
        private final int slot;
        private final Thread thread = Thread.currentThread();
        private boolean loaded;
        private boolean spent;
        private Frame active;

        private Origin(Load load, ItemStack itemStack, OwnerNativeSemanticAuthority.WeaponInput weaponInput, FoodWeaponExpectedTrace.Value value, int n) {
            this.input = weaponInput;
            this.expected = value;
            this.actual = new WeakReference<ItemStack>(itemStack);
            this.player = new WeakReference<ServerPlayer>(load.player);
            this.inventory = new WeakReference<Inventory>(load.inventory);
            this.server = load.server;
            this.owner = load.owner;
            this.load = load.identity;
            this.slot = n;
        }
    }

    public static final class Frame {
        private final Root root;
        private final Frame previous;
        private final int kind;
        private final int requested;
        private final int depth;
        private final OwnerRepairContinuation.Entry continuation;
        private boolean closed;

        private Frame(OwnerRepairContinuation.Entry entry) {
            this.root = null;
            this.previous = null;
            this.kind = 0;
            this.requested = 0;
            this.depth = 0;
            this.continuation = entry;
        }

        private Frame(Root root, Frame frame, int n, int n2) {
            this.root = root;
            this.previous = frame;
            this.kind = n;
            this.requested = n2;
            this.continuation = null;
            this.depth = frame == null ? 1 : frame.depth + 1;
        }
    }

    private static final class Root {
        private final Origin origin;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final Object level;
        private final String dimension;
        private final UUID call = UUID.randomUUID();
        private final ListTag trace = new ListTag();
        private Frame outer;
        private Phase phase = Phase.CAPTURING;
        private CompoundTag before;
        private String rules;
        private int damage;
        private int maximum;
        private boolean broken;
        private int wears;
        private volatile boolean rejected;
        private OwnerStatueWearPlan plan;
        private Write writing;

        private Root(Origin origin, ServerPlayer serverPlayer, ItemStack itemStack) {
            this.origin = origin;
            this.player = serverPlayer;
            this.actual = itemStack;
            this.level = serverPlayer.m_9236_();
            this.dimension = serverPlayer.m_9236_().m_46472_().m_135782_().toString();
        }
    }

    private static enum Phase {
        CAPTURING,
        ORIGINAL,
        TERMINAL,
        CLOSED;

    }

    static final class Reading {
        private final Root root;
        private final Frame frame;
        private final boolean before;
        private boolean prefix;

        private Reading(Root root, Frame frame, boolean bl) {
            this.root = root;
            this.frame = frame;
            this.before = bl;
        }

        boolean live(OwnerNativeSemanticAuthority.WeaponInput weaponInput, ItemStack itemStack) {
            Origin origin = this.root.origin;
            return origin.input == weaponInput && origin.actual.get() == itemStack && this.root.actual == itemStack && origin.active == this.frame && !this.frame.closed && origin.thread == Thread.currentThread() && origin.server.m_18695_() && this.root.player.m_20194_() == origin.server && origin.owner.equals(this.root.player.m_20148_()) && (this.before ? this.root.phase == Phase.CAPTURING && this.root.player.m_9236_() == this.root.level && OwnerStatueWearAdmission.physical(origin, this.root.player, itemStack) && OwnerFullRepairGuard.permits(itemStack) : (this.prefix ? this.root.phase == Phase.ORIGINAL && this.root.player.m_9236_() == this.root.level && OwnerStatueWearAdmission.physical(origin, this.root.player, itemStack) : this.root.phase == Phase.TERMINAL)) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class && OwnerStatueWearBoundary.original(this.root.outer.kind, stackFrame.getMethodName(), stackFrame.getDescriptor()))) != false;
        }

        boolean before() {
            return this.before;
        }
    }

    static final class Terminal {
        private final Root root;
        private final Throwable primary;
        private final CompoundTag evidence;
        private boolean handedOff;

        private Terminal(Root root, Throwable throwable, CompoundTag compoundTag) {
            this.root = root;
            this.primary = throwable;
            this.evidence = compoundTag;
        }

        ServerPlayer player() {
            return this.root.player;
        }

        UUID identity() {
            return this.root.call;
        }

        CompoundTag take() {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueTaskService.class && stackFrame.getMethodName().equals("recordOriginalWear")).orElse(false));
            if (!(bl && !this.handedOff && this.root.phase == Phase.TERMINAL && this.root.origin.active == this.root.outer && this.root.origin.thread == Thread.currentThread() && this.root.player.m_20194_() == this.root.origin.server && this.root.origin.owner.equals(this.root.player.m_20148_()) && this.root.origin.server.m_18695_() && OwnerStatueWearAdmission.originalCaller(this.root.outer.kind, "ftboss$statueWearExitGuard"))) {
                throw new IllegalStateException("Detached original wear terminal");
            }
            this.handedOff = true;
            if (this.primary == null != this.evidence.m_128471_("ReturnedNormally")) {
                throw new IllegalStateException("Wear primary outcome mismatch");
            }
            return this.evidence.m_6426_();
        }
    }

    public static final class Write {
        private final Root root;
        private final OwnerStatueWearPlan.Step step;
        private final Object receiver;
        private final Object storage;
        private final CompoundTag before;
        private boolean closed;

        private Write(Root root, OwnerStatueWearPlan.Step step, Object object, Object object2) {
            this.root = root;
            this.step = step;
            this.receiver = object;
            this.storage = object2;
            this.before = root.plan.cursor.m_6426_();
        }
    }

    static final class Accepted {
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID call;
        private final UUID load;
        private final UUID weapon;
        private final int slot;
        private final int level;
        private final int maximum;
        private final String profile;
        private final String rules;
        private final CompoundTag expected;
        private final boolean normal;
        private final String primaryClass;
        private final boolean rollback;
        private final boolean rollbackAttempted;
        private volatile boolean spent;
        private boolean taskOriginTransferred;

        private Accepted(Root root, Throwable throwable) {
            this.actual = new WeakReference<ItemStack>(root.actual);
            this.player = new WeakReference<ServerPlayer>(root.player);
            this.inventory = new WeakReference<Inventory>(root.player.m_150109_());
            this.server = root.origin.server;
            this.owner = root.origin.owner;
            this.call = root.call;
            this.load = root.origin.load;
            this.weapon = root.plan.cursor.m_128469_("tag").m_128469_("FTBossWeaponLifecycle").m_128342_("Weapon");
            this.slot = root.origin.slot;
            this.level = root.plan.level;
            this.maximum = root.plan.expectedMaximum();
            this.profile = root.plan.profile;
            this.rules = root.rules;
            this.expected = root.plan.cursor.m_6426_();
            this.normal = throwable == null;
            this.primaryClass = throwable == null ? "" : throwable.getClass().getName();
            this.rollback = root.plan.swallowedRollback;
            this.rollbackAttempted = root.plan.rollbackAttempted;
        }

        boolean transferToTaskOrigin(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueTaskOrigin.class && stackFrame.getMethodName().equals("reserve")).orElse(false));
            if (!(bl && !this.taskOriginTransferred && this.spent && this.startEligible() && this.actual.get() == itemStack && this.player.get() == serverPlayer && this.inventory.get() == serverPlayer.m_150109_() && this.server == serverPlayer.m_20194_() && this.server.m_18695_() && this.owner.equals(serverPlayer.m_20148_()) && this.slot == n && serverPlayer.m_150109_().m_8020_(n) == itemStack && this.expected.equals((Object)itemStack.m_41739_(new CompoundTag())) && this.rules.equals(OwnerStatueTaskPlan.rules(itemStack)))) {
                return false;
            }
            this.taskOriginTransferred = true;
            return true;
        }

        private boolean startEligible() {
            return this.normal && this.primaryClass.isEmpty() && !this.rollbackAttempted && !this.rollback;
        }

        CompoundTag record() {
            if (!this.startEligible()) {
                throw new IllegalStateException("Wear evidence is not normal no-rollback START authority");
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128362_("Call", this.call);
            compoundTag.m_128362_("OriginalLoad", this.load);
            compoundTag.m_128362_("Owner", this.owner);
            compoundTag.m_128362_("Weapon", this.weapon);
            compoundTag.m_128405_("Slot", this.slot);
            compoundTag.m_128405_("Level", this.level);
            compoundTag.m_128405_("Maximum", this.maximum);
            compoundTag.m_128359_("Profile", this.profile);
            compoundTag.m_128359_("Rules", this.rules);
            compoundTag.m_128359_("Status", "AVAILABLE_LIVE_IDENTITY_ONLY");
            compoundTag.m_128359_("Authority", "INDEPENDENT_ORIGINAL_WRITER_PREFIX_NOT_OBSERVATION");
            compoundTag.m_128379_("ReturnedNormally", this.normal);
            compoundTag.m_128359_("PrimaryClass", this.primaryClass);
            compoundTag.m_128379_("OriginalRollbackEntered", this.rollbackAttempted);
            compoundTag.m_128379_("SwallowedOriginalRollbackReturned", this.rollback);
            compoundTag.m_128365_("Expected", (Tag)this.expected.m_6426_());
            return compoundTag;
        }
    }
}

