/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class StatueRepairMutation
implements FourWeaponTransactions.TerminalMutation {
    private static final ThreadLocal<StatueRepairMutation> ACTIVE = new ThreadLocal();
    private final FourWeaponTransactions.Mutation delegate;
    private final ServerPlayer player;
    private final ItemStack weapon;
    private final FourWeaponTransactions.Operation operation;
    private final BlockPos statue;
    private final FourWeaponTransactions.Request request;
    private final boolean nativeRequestOrigin;
    private OwnerStatueTaskService.Prepared taskRecord;
    private final int task;
    private final boolean restorePayments;
    private final int slot;
    private final UUID owner;
    private final UUID weaponId;
    private final MinecraftServer server;
    private final ServerLevel level;
    private final Thread thread;
    private final List<Payment> payments = new ArrayList<Payment>();
    private Payment pending;
    private Phase phase = Phase.PREPARED;

    private StatueRepairMutation(FourWeaponTransactions.Mutation mutation, ServerPlayer serverPlayer, ItemStack itemStack, FourWeaponTransactions.Operation operation, int n, int n2, boolean bl, BlockPos blockPos, FourWeaponTransactions.Request request) {
        this.delegate = Objects.requireNonNull(mutation);
        this.player = Objects.requireNonNull(serverPlayer);
        this.weapon = Objects.requireNonNull(itemStack);
        this.operation = operation;
        this.statue = blockPos.m_7949_();
        this.request = request;
        this.task = n;
        this.restorePayments = bl;
        this.slot = n2;
        this.owner = serverPlayer.m_20148_();
        this.weaponId = Objects.requireNonNull(FoodWeaponLifecycle.weaponId(itemStack));
        this.server = Objects.requireNonNull(serverPlayer.m_20194_());
        this.level = serverPlayer.m_284548_();
        this.thread = Thread.currentThread();
        this.nativeRequestOrigin = !bl && request != null && StatueRepairMutation.originalRequestCaller();
        this.checkSource();
    }

    public static FourWeaponTransactions.Mutation wrap(FourWeaponTransactions.Mutation mutation, ServerPlayer serverPlayer, ItemStack itemStack, FourWeaponTransactions.Operation operation, int n, int n2, BlockPos blockPos, FourWeaponTransactions.Request request) {
        return switch (operation) {
            case FourWeaponTransactions.Operation.START_REPAIR, FourWeaponTransactions.Operation.SUBMIT_REPAIR, FourWeaponTransactions.Operation.CLAIM_REPAIR -> new StatueRepairMutation(mutation, serverPlayer, itemStack, operation, n, n2, false, blockPos, request);
            default -> mutation;
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static StatueRepairMutation wrapLegacy(LegacyCompensation legacyCompensation, ServerPlayer serverPlayer, ItemStack itemStack, FourWeaponTransactions.Operation operation, int n, int n2, BlockPos blockPos) {
        switch (operation) {
            case START_REPAIR: 
            case SUBMIT_REPAIR: 
            case CLAIM_REPAIR: {
                return new StatueRepairMutation(legacyCompensation, serverPlayer, itemStack, operation, n, n2, true, blockPos, null);
            }
            default: {
                throw new IllegalArgumentException("not a legacy repair operation");
            }
        }
    }

    private static boolean originalLegacyCaller(String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != StatueRepairMutation.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == StatueMenuNetwork.class && stackFrame.getMethodName().equals(string)).orElse(false));
    }

    private static boolean taskUiLive() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == StatueMenuNetwork.class && stackFrame.getMethodName().equals("executeRepair")));
    }

    private static boolean originalRequestCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != StatueRepairMutation.class).toList();
            return !list.isEmpty() && list.get(0).getDeclaringClass() == ForgeTransactionHost.class && list.get(0).getMethodName().equals("prepare") && list.stream().anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FourWeaponTransactions.class && stackFrame.getMethodName().equals("submit"));
        });
    }

    private boolean nativeRequestLive(boolean bl) {
        if (!(this.nativeRequestOrigin && !this.restorePayments && this.request != null && this.request.operation() == this.operation && this.request.value() == this.task && this.request.binding().owner().equals(this.owner) && this.request.binding().weapon().equals(this.weaponId) && this.request.binding().inventorySlot() == this.slot && this.request.binding().hand() == (this.slot == 40 ? 1 : 0) && this.request.binding().statue() == this.statue.m_121878_() && this.request.binding().dimension().equals(this.level.m_46472_().m_135782_().toString()) && this.request.binding().item().equals(FourWeaponFoundationAdapter.registryId(this.weapon)))) {
            return false;
        }
        if (!bl && !FourWeaponTransactionRuntime.executing(this.owner, this.operation)) {
            return false;
        }
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List list = stream.toList();
            return list.stream().anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FourWeaponTransactions.class && stackFrame.getMethodName().equals("submit")) && list.stream().anyMatch(stackFrame -> bl ? stackFrame.getDeclaringClass() == StatueRepairMutation.class && stackFrame.getMethodName().equals("terminalCommit") : stackFrame.getDeclaringClass() == ExistingTransactionBoundary.class && stackFrame.getMethodName().equals("execute"));
        });
    }

    static StatueRepairMutation nativeRepairSource(ServerPlayer serverPlayer, ItemStack itemStack) {
        StatueRepairMutation statueRepairMutation = ACTIVE.get();
        if (!(statueRepairMutation != null && statueRepairMutation.player == serverPlayer && statueRepairMutation.weapon == itemStack && statueRepairMutation.phase == Phase.RUNNING && (statueRepairMutation.restorePayments && statueRepairMutation.request == null && StatueRepairMutation.taskUiLive() || statueRepairMutation.nativeRequestLive(false)))) {
            return null;
        }
        statueRepairMutation.checkSource();
        return statueRepairMutation;
    }

    boolean nativeRepairCommitted(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (!(this.player == serverPlayer && this.weapon == itemStack && this.phase == Phase.COMMITTED && this.operation == FourWeaponTransactions.Operation.CLAIM_REPAIR && Thread.currentThread() == this.thread && this.server.m_18695_() && (this.restorePayments && this.request == null && StatueRepairMutation.taskUiLive() || this.nativeRequestLive(true)))) {
            return false;
        }
        this.checkSource();
        return true;
    }

    boolean taskOriginSource(OwnerStatueTaskService.Prepared prepared) {
        return this.taskRecord == prepared && this.phase == Phase.PREPARED && this.restorePayments && this.request == null && StatueRepairMutation.taskUiLive();
    }

    boolean taskOriginRunning(OwnerStatueTaskService.Prepared prepared) {
        return this.taskRecord == prepared && this.phase == Phase.RUNNING && ACTIVE.get() == this && this.restorePayments && this.request == null && StatueRepairMutation.taskUiLive();
    }

    private void checkSource() {
        if (Thread.currentThread() != this.thread || !this.server.m_18695_() || this.player.m_20194_() != this.server || this.player.m_284548_() != this.level || this.player.m_213877_() || !this.owner.equals(this.player.m_20148_()) || !this.weaponId.equals(FoodWeaponLifecycle.weaponId(this.weapon)) || this.slot < 0 || this.slot >= this.player.m_150109_().m_6643_() || this.player.m_150109_().m_8020_(this.slot) != this.weapon || this.slot == 40 && this.player.m_21206_() != this.weapon || this.slot != 40 && (this.player.m_150109_().f_35977_ != this.slot || this.player.m_21205_() != this.weapon)) {
            throw new IllegalStateException("repair mutation source changed");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean commit() {
        if (this.phase != Phase.PREPARED || ACTIVE.get() != null) {
            throw new IllegalStateException("repair mutation already entered");
        }
        this.checkSource();
        boolean bl = this.operation == FourWeaponTransactions.Operation.START_REPAIR && this.restorePayments && this.request == null && StatueRepairMutation.originalLegacyCaller("executeRepair") && OwnerStatueWearAdmission.hasStartCandidate(this.player, this.weapon, this.slot);
        try {
            this.taskRecord = !NativeFoodWeaponWear.eligible(this.weapon) && (this.operation != FourWeaponTransactions.Operation.START_REPAIR || bl) ? OwnerStatueTaskService.prepare(this.player, this.weapon, this.slot, this.statue, this.operation, this.task, this.request) : null;
        }
        catch (IOException iOException) {
            throw OwnerStatueTaskService.unavailable(this.player, iOException);
        }
        if (this.taskRecord != null) {
            this.taskRecord.beforeOriginal(this);
        }
        this.phase = Phase.RUNNING;
        ACTIVE.set(this);
        try {
            boolean bl2 = this.delegate.commit();
            if (this.pending != null) {
                throw new IllegalStateException("original repair payment did not return");
            }
            this.phase = bl2 ? Phase.RETURNED : Phase.REJECTED;
            boolean bl3 = bl2;
            return bl3;
        }
        finally {
            if (this.phase != Phase.RETURNED && this.taskRecord != null) {
                this.taskRecord.originFailed();
            }
            ACTIVE.remove();
        }
    }

    public static void paymentBefore(ServerPlayer serverPlayer, ItemStack itemStack, ItemStack itemStack2, int n, int n2, int n3) {
        StatueRepairMutation statueRepairMutation = ACTIVE.get();
        if (statueRepairMutation == null) {
            return;
        }
        statueRepairMutation.checkSource();
        if (statueRepairMutation.player != serverPlayer || statueRepairMutation.weapon != itemStack || statueRepairMutation.operation != FourWeaponTransactions.Operation.SUBMIT_REPAIR || statueRepairMutation.task != n || statueRepairMutation.phase != Phase.RUNNING || statueRepairMutation.pending != null || n2 < 0 || n2 >= 36 || itemStack2 == itemStack || serverPlayer.m_150109_().m_8020_(n2) != itemStack2 || n3 <= 0 || n3 > itemStack2.m_41613_()) {
            throw new IllegalStateException("repair payment source changed");
        }
        for (Payment payment : statueRepairMutation.payments) {
            if (payment.slot() != n2 && payment.supply() != itemStack2) continue;
            throw new IllegalStateException("repair payment repeated");
        }
        CompoundTag compoundTag = itemStack2.m_41783_();
        statueRepairMutation.pending = new Payment(n2, itemStack2, n3, itemStack2.m_41613_(), -1, compoundTag == null ? null : compoundTag.m_6426_());
        if (statueRepairMutation.taskRecord != null) {
            try {
                statueRepairMutation.taskRecord.beforeShrink(itemStack2, n2, n3);
            }
            catch (IOException iOException) {
                throw OwnerStatueTaskService.unavailable(serverPlayer, iOException);
            }
        }
    }

    public static void paymentAfter(ServerPlayer serverPlayer, ItemStack itemStack, ItemStack itemStack2, int n, int n2, int n3) {
        StatueRepairMutation statueRepairMutation = ACTIVE.get();
        if (statueRepairMutation == null) {
            return;
        }
        Payment payment = statueRepairMutation.pending;
        if (statueRepairMutation.player != serverPlayer || statueRepairMutation.weapon != itemStack || statueRepairMutation.task != n || payment == null || payment.supply() != itemStack2 || payment.slot() != n2 || payment.requested() != n3 || serverPlayer.m_150109_().m_8020_(n2) != itemStack2 || itemStack2.m_41613_() != payment.before() - n3 || !Objects.equals(payment.tag(), itemStack2.m_41783_())) {
            throw new IllegalStateException("original repair payment result changed");
        }
        statueRepairMutation.payments.add(new Payment(n2, itemStack2, n3, payment.before(), itemStack2.m_41613_(), payment.tag()));
        statueRepairMutation.pending = null;
        if (statueRepairMutation.taskRecord != null) {
            try {
                statueRepairMutation.taskRecord.afterShrink(itemStack2, n2, n3);
            }
            catch (IOException iOException) {
                throw OwnerStatueTaskService.unavailable(serverPlayer, iOException);
            }
        }
    }

    private void terminal() {
        if (this.phase != Phase.RETURNED || this.pending != null) {
            throw new IllegalStateException("repair mutation has no successful return");
        }
        this.checkSource();
        if (this.operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR && this.payments.isEmpty()) {
            throw new IllegalStateException("repair submit has no original payment");
        }
        for (Payment payment : this.payments) {
            if (this.player.m_150109_().m_8020_(payment.slot()) == payment.supply() && payment.supply().m_41613_() == payment.after() && Objects.equals(payment.tag(), payment.supply().m_41783_())) continue;
            throw new IllegalStateException("repair payment inventory postcheck failed");
        }
        if (this.taskRecord != null) {
            try {
                this.taskRecord.decideAtOriginalTerminal();
                this.taskRecord.checkpointAfterOriginalSaveOrRollback();
            }
            catch (IOException iOException) {
                throw OwnerStatueTaskService.unavailable(this.player, iOException);
            }
        }
        this.phase = Phase.COMMITTED;
        NativeFoodWeaponWear.committedStatueRepair(this, this.player, this.weapon);
        if (this.taskRecord != null) {
            this.taskRecord.afterOuterCommitted(new Committed(this, this.taskRecord));
        }
    }

    @Override
    public void terminalCommit(FourWeaponTransactions.State state) {
        if (state == null || state.principal() != this.player || state.source() != this.weapon || !state.binding().owner().equals(this.owner) || !state.binding().weapon().equals(this.weaponId) || state.binding().inventorySlot() != this.slot || this.nativeRequestOrigin && !this.request.binding().equals(state.binding())) {
            throw new IllegalStateException("repair terminal source changed");
        }
        this.terminal();
    }

    public void terminalLegacy() {
        if (this.operation == FourWeaponTransactions.Operation.START_REPAIR && this.taskRecord != null && !StatueRepairMutation.originalLegacyCaller("executeRepair")) {
            throw new IllegalStateException("Tracked START requires original statue terminal");
        }
        this.terminal();
    }

    private boolean restorePayments() {
        ArrayList<Payment> arrayList = new ArrayList<Payment>(this.payments);
        if (this.pending != null) {
            arrayList.add(this.pending);
        }
        for (Payment object : arrayList) {
            int payment = object.supply().m_41613_();
            if (this.player.m_150109_().m_8020_(object.slot()) == object.supply() && (payment == object.before() || payment == object.before() - object.requested()) && Objects.equals(object.tag(), object.supply().m_41783_())) continue;
            throw new IllegalStateException("repair payment compensation source changed");
        }
        boolean bl = false;
        for (Payment payment : arrayList) {
            if (payment.supply().m_41613_() == payment.before()) continue;
            payment.supply().m_41764_(payment.before());
            bl = true;
        }
        return bl;
    }

    @Override
    public void rollback() {
        if (this.phase == Phase.ABORTED) {
            return;
        }
        if (Thread.currentThread() != this.thread || this.phase == Phase.ABORTING || this.phase == Phase.ABORT_FAILED) {
            throw new IllegalStateException("repair compensation unavailable");
        }
        this.phase = Phase.ABORTING;
        boolean bl = false;
        try {
            FourWeaponTransactions.Mutation mutation;
            boolean bl2;
            if (this.taskRecord != null) {
                this.taskRecord.beforeOriginalRollback();
            }
            boolean bl3 = bl2 = this.restorePayments && this.restorePayments();
            if (this.restorePayments && (mutation = this.delegate) instanceof LegacyCompensation) {
                LegacyCompensation legacyCompensation = (LegacyCompensation)mutation;
                legacyCompensation.rollback(bl2);
            } else {
                this.delegate.rollback();
            }
            if (this.taskRecord != null) {
                this.taskRecord.checkpointAfterOriginalSaveOrRollback();
            }
            bl = true;
            this.pending = null;
            this.payments.clear();
            this.phase = bl ? Phase.ABORTED : Phase.ABORT_FAILED;
        }
        catch (IOException iOException) {
            try {
                throw OwnerStatueTaskService.unavailable(this.player, iOException);
            }
            catch (Throwable throwable) {
                this.pending = null;
                this.payments.clear();
                this.phase = bl ? Phase.ABORTED : Phase.ABORT_FAILED;
                throw throwable;
            }
        }
    }

    private static enum Phase {
        PREPARED,
        RUNNING,
        RETURNED,
        REJECTED,
        COMMITTED,
        ABORTING,
        ABORTED,
        ABORT_FAILED;

    }

    private record Payment(int slot, ItemStack supply, int requested, int before, int after, CompoundTag tag) {
    }

    static final class Committed {
        private final StatueRepairMutation outer;
        private final OwnerStatueTaskService.Prepared prepared;
        private boolean taken;

        private Committed(StatueRepairMutation statueRepairMutation, OwnerStatueTaskService.Prepared prepared) {
            this.outer = statueRepairMutation;
            this.prepared = prepared;
        }

        boolean take(OwnerStatueTaskService.Prepared prepared) {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueTaskOrigin.class && stackFrame.getMethodName().equals("outerCommitted")).orElse(false));
            if (!bl || this.taken || !this.remains(prepared)) {
                return false;
            }
            this.taken = true;
            return true;
        }

        boolean remains(OwnerStatueTaskService.Prepared prepared) {
            return prepared == this.prepared && this.outer.taskRecord == this.prepared && this.outer.phase == Phase.COMMITTED && Thread.currentThread() == this.outer.thread && this.outer.server.m_18695_() && this.outer.restorePayments && this.outer.request == null && StatueRepairMutation.taskUiLive();
        }
    }

    public static interface LegacyCompensation
    extends FourWeaponTransactions.Mutation {
        public void rollback(boolean var1);

        @Override
        default public void rollback() {
            this.rollback(false);
        }
    }
}

