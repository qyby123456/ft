package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Mutation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.State;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.TerminalMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class StatueRepairMutation implements TerminalMutation {
    private static final ThreadLocal<StatueRepairMutation> ACTIVE = new ThreadLocal<>();
    private final Mutation delegate;
    private final ServerPlayer player;
    private final ItemStack weapon;
    private final Operation operation;
    private final BlockPos statue;
    private final Request request;
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
    private final List<StatueRepairMutation.Payment> payments = new ArrayList<>();
    private StatueRepairMutation.Payment pending;
    private StatueRepairMutation.Phase phase = StatueRepairMutation.Phase.PREPARED;

    private StatueRepairMutation(
        Mutation var1, ServerPlayer var2, ItemStack var3, Operation var4, int var5, int var6, boolean var7, BlockPos var8, Request var9
    ) {
        this.delegate = Objects.requireNonNull(var1);
        this.player = Objects.requireNonNull(var2);
        this.weapon = Objects.requireNonNull(var3);
        this.operation = var4;
        this.statue = var8.immutable();
        this.request = var9;
        this.task = var5;
        this.restorePayments = var7;
        this.slot = var6;
        this.owner = var2.getUUID();
        this.weaponId = Objects.requireNonNull(FoodWeaponLifecycle.weaponId(var3));
        this.server = Objects.requireNonNull(var2.getServer());
        this.level = var2.serverLevel();
        this.thread = Thread.currentThread();
        this.nativeRequestOrigin = !var7 && var9 != null && originalRequestCaller();
        this.checkSource();
    }

    public static Mutation wrap(Mutation var0, ServerPlayer var1, ItemStack var2, Operation var3, int var4, int var5, BlockPos var6, Request var7) {
        return (Mutation)(switch (var3) {
            case START_REPAIR, SUBMIT_REPAIR, CLAIM_REPAIR -> new StatueRepairMutation(var0, var1, var2, var3, var4, var5, false, var6, var7);
            default -> var0;
        });
    }

    public static StatueRepairMutation wrapLegacy(
        StatueRepairMutation.LegacyCompensation var0, ServerPlayer var1, ItemStack var2, Operation var3, int var4, int var5, BlockPos var6
    ) {
        switch (var3) {
            case START_REPAIR:
            case SUBMIT_REPAIR:
            case CLAIM_REPAIR:
                return new StatueRepairMutation(var0, var1, var2, var3, var4, var5, true, var6, null);
            default:
                throw new IllegalArgumentException("not a legacy repair operation");
        }
    }

    private static boolean originalLegacyCaller(String var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != StatueRepairMutation.class)
                        .findFirst()
                        .map(var1x -> var1x.getDeclaringClass() == StatueMenuNetwork.class && var1x.getMethodName().equals(var0))
                        .orElse(false)
            );
    }

    private static boolean taskUiLive() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.anyMatch(var0x -> var0x.getDeclaringClass() == StatueMenuNetwork.class && var0x.getMethodName().equals("executeRepair")));
    }

    private static boolean originalRequestCaller() {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0 -> {
                    java.util.List<java.lang.StackWalker.StackFrame> var1 = var0.filter(var0x -> var0x.getDeclaringClass().getNestHost() != StatueRepairMutation.class).toList();
                    return !var1.isEmpty()
                        && ((StackFrame)var1.get(0)).getDeclaringClass() == ForgeTransactionHost.class
                        && ((StackFrame)var1.get(0)).getMethodName().equals("prepare")
                        && var1.stream().anyMatch(var0x -> var0x.getDeclaringClass() == FourWeaponTransactions.class && var0x.getMethodName().equals("submit"));
                }
            );
    }

    private boolean nativeRequestLive(boolean var1) {
        if (!this.nativeRequestOrigin
            || this.restorePayments
            || this.request == null
            || this.request.operation() != this.operation
            || this.request.value() != this.task
            || !this.request.binding().owner().equals(this.owner)
            || !this.request.binding().weapon().equals(this.weaponId)
            || this.request.binding().inventorySlot() != this.slot
            || this.request.binding().hand() != (this.slot == 40 ? 1 : 0)
            || this.request.binding().statue() != this.statue.asLong()
            || !this.request.binding().dimension().equals(this.level.dimension().location().toString())
            || !this.request.binding().item().equals(FourWeaponFoundationAdapter.registryId(this.weapon))) {
            return false;
        } else {
            return !var1 && !FourWeaponTransactionRuntime.executing(this.owner, this.operation)
                ? false
                : StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var1x -> {
                            java.util.List<java.lang.StackWalker.StackFrame> var2 = var1x.toList();
                            return var2.stream()
                                    .anyMatch(var0x -> var0x.getDeclaringClass() == FourWeaponTransactions.class && var0x.getMethodName().equals("submit"))
                                && var2.stream()
                                    .anyMatch(
                                        var1xx -> var1
                                                ? var1xx.getDeclaringClass() == StatueRepairMutation.class && var1xx.getMethodName().equals("terminalCommit")
                                                : var1xx.getDeclaringClass() == ExistingTransactionBoundary.class && var1xx.getMethodName().equals("execute")
                                    );
                        }
                    );
        }
    }

    static StatueRepairMutation nativeRepairSource(ServerPlayer var0, ItemStack var1) {
        StatueRepairMutation var2 = ACTIVE.get();
        if (var2 != null
            && var2.player == var0
            && var2.weapon == var1
            && var2.phase == StatueRepairMutation.Phase.RUNNING
            && (var2.restorePayments && var2.request == null && taskUiLive() || var2.nativeRequestLive(false))) {
            var2.checkSource();
            return var2;
        } else {
            return null;
        }
    }

    boolean nativeRepairCommitted(ServerPlayer var1, ItemStack var2) {
        if (this.player == var1
            && this.weapon == var2
            && this.phase == StatueRepairMutation.Phase.COMMITTED
            && this.operation == Operation.CLAIM_REPAIR
            && Thread.currentThread() == this.thread
            && this.server.isSameThread()
            && (this.restorePayments && this.request == null && taskUiLive() || this.nativeRequestLive(true))) {
            this.checkSource();
            return true;
        } else {
            return false;
        }
    }


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

    boolean taskOriginSource(OwnerStatueTaskService.Prepared var1) {
        return this.taskRecord == var1 && this.phase == StatueRepairMutation.Phase.PREPARED && this.restorePayments && this.request == null && taskUiLive();
    }

    boolean taskOriginRunning(OwnerStatueTaskService.Prepared var1) {
        return this.taskRecord == var1
            && this.phase == StatueRepairMutation.Phase.RUNNING
            && ACTIVE.get() == this
            && this.restorePayments
            && this.request == null
            && taskUiLive();
    }

    private void checkSource() {
        if (Thread.currentThread() != this.thread
            || !this.server.isSameThread()
            || this.player.getServer() != this.server
            || this.player.serverLevel() != this.level
            || this.player.isRemoved()
            || !this.owner.equals(this.player.getUUID())
            || !this.weaponId.equals(FoodWeaponLifecycle.weaponId(this.weapon))
            || this.slot < 0
            || this.slot >= this.player.getInventory().getContainerSize()
            || this.player.getInventory().getItem(this.slot) != this.weapon
            || this.slot == 40 && this.player.getOffhandItem() != this.weapon
            || this.slot != 40 && (this.player.getInventory().selected != this.slot || this.player.getMainHandItem() != this.weapon)) {
            throw new IllegalStateException("repair mutation source changed");
        }
    }

    @Override
    public boolean commit() {
        if (this.phase == StatueRepairMutation.Phase.PREPARED && ACTIVE.get() == null) {
            this.checkSource();
            boolean var1 = this.operation == Operation.START_REPAIR
                && this.restorePayments
                && this.request == null
                && originalLegacyCaller("executeRepair")
                && OwnerStatueWearAdmission.hasStartCandidate(this.player, this.weapon, this.slot);

            try {
                this.taskRecord = NativeFoodWeaponWear.eligible(this.weapon) || this.operation == Operation.START_REPAIR && !var1
                    ? null
                    : OwnerStatueTaskService.prepare(this.player, this.weapon, this.slot, this.statue, this.operation, this.task, this.request);
            } catch (IOException var8) {
                throw OwnerStatueTaskService.unavailable(this.player, var8);
            }

            if (this.taskRecord != null) {
                this.taskRecord.beforeOriginal(this);
            }

            this.phase = StatueRepairMutation.Phase.RUNNING;
            ACTIVE.set(this);

            boolean var3;
            try {
                boolean var2 = this.delegate.commit();
                if (this.pending != null) {
                    throw new IllegalStateException("original repair payment did not return");
                }

                this.phase = var2 ? StatueRepairMutation.Phase.RETURNED : StatueRepairMutation.Phase.REJECTED;
                var3 = var2;
            } catch (RuntimeException | Error failure) {
                net.farmingtales.ftboss.FTBossMod.LOGGER.error("Original repair operation {} failed during {}", this.operation, this.phase, failure);
                throw failure;
            } finally {
                if (this.phase != StatueRepairMutation.Phase.RETURNED && this.taskRecord != null) {
                    this.taskRecord.originFailed();
                }

                ACTIVE.remove();
            }

            return var3;
        } else {
            throw new IllegalStateException("repair mutation already entered");
        }
    }

    public static void paymentBefore(ServerPlayer var0, ItemStack var1, ItemStack var2, int var3, int var4, int var5) {
        StatueRepairMutation var6 = ACTIVE.get();
        if (var6 != null) {
            var6.checkSource();
            if (var6.player == var0
                && var6.weapon == var1
                && var6.operation == Operation.SUBMIT_REPAIR
                && var6.task == var3
                && var6.phase == StatueRepairMutation.Phase.RUNNING
                && var6.pending == null
                && var4 >= 0
                && var4 < 36
                && var2 != var1
                && var0.getInventory().getItem(var4) == var2
                && var5 > 0
                && var5 <= var2.getCount()) {
                for (StatueRepairMutation.Payment var8 : var6.payments) {
                    if (var8.slot() == var4 || var8.supply() == var2) {
                        throw new IllegalStateException("repair payment repeated");
                    }
                }

                NativeOwnerRepairItemData.PaymentComponents var10 = NativeOwnerRepairItemData.paymentComponents(var2);
                var6.pending = new StatueRepairMutation.Payment(var4, var2, var5, var2.getCount(), -1, var10);
                if (var6.taskRecord != null) {
                    try {
                        var6.taskRecord.beforeShrink(var2, var4, var5);
                    } catch (IOException var9) {
                        throw OwnerStatueTaskService.unavailable(var0, var9);
                    }
                }
            } else {
                throw new IllegalStateException("repair payment source changed");
            }
        }
    }

    public static void paymentAfter(ServerPlayer var0, ItemStack var1, ItemStack var2, int var3, int var4, int var5) {
        StatueRepairMutation var6 = ACTIVE.get();
        if (var6 != null) {
            StatueRepairMutation.Payment var7 = var6.pending;
            if (var6.player == var0
                && var6.weapon == var1
                && var6.task == var3
                && var7 != null
                && var7.supply() == var2
                && var7.slot() == var4
                && var7.requested() == var5
                && var0.getInventory().getItem(var4) == var2
                && var2.getCount() == var7.before() - var5
                && var7.tag().matches(var2)) {
                var6.payments.add(new StatueRepairMutation.Payment(var4, var2, var5, var7.before(), var2.getCount(), var7.tag()));
                var6.pending = null;
                if (var6.taskRecord != null) {
                    try {
                        var6.taskRecord.afterShrink(var2, var4, var5);
                    } catch (IOException var9) {
                        throw OwnerStatueTaskService.unavailable(var0, var9);
                    }
                }
            } else {
                throw new IllegalStateException("original repair payment result changed");
            }
        }
    }

    private void terminal() {
        if (this.phase == StatueRepairMutation.Phase.RETURNED && this.pending == null) {
            this.checkSource();
            if (this.operation == Operation.SUBMIT_REPAIR && this.payments.isEmpty()) {
                throw new IllegalStateException("repair submit has no original payment");
            } else {
                for (StatueRepairMutation.Payment var2 : this.payments) {
                    if (this.player.getInventory().getItem(var2.slot()) != var2.supply()
                        || var2.supply().getCount() != var2.after()
                        || !var2.tag().matches(var2.supply())) {
                        throw new IllegalStateException("repair payment inventory postcheck failed");
                    }
                }

                if (this.taskRecord != null) {
                    try {
                        this.taskRecord.decideAtOriginalTerminal();
                        this.taskRecord.checkpointAfterOriginalSaveOrRollback();
                    } catch (IOException var3) {
                        throw OwnerStatueTaskService.unavailable(this.player, var3);
                    }
                }

                this.phase = StatueRepairMutation.Phase.COMMITTED;
                NativeFoodWeaponWear.committedStatueRepair(this, this.player, this.weapon);
                if (this.taskRecord != null) {
                    this.taskRecord.afterOuterCommitted(new StatueRepairMutation.Committed(this, this.taskRecord));
                }
            }
        } else {
            throw new IllegalStateException("repair mutation has no successful return");
        }
    }

    @Override
    public void terminalCommit(State var1) {
        if (var1 != null
            && var1.principal() == this.player
            && var1.source() == this.weapon
            && var1.binding().owner().equals(this.owner)
            && var1.binding().weapon().equals(this.weaponId)
            && var1.binding().inventorySlot() == this.slot
            && (!this.nativeRequestOrigin || this.request.binding().equals(var1.binding()))) {
            this.terminal();
        } else {
            throw new IllegalStateException("repair terminal source changed");
        }
    }

    public void terminalLegacy() {
        if (this.operation == Operation.START_REPAIR && this.taskRecord != null && !originalLegacyCaller("executeRepair")) {
            throw new IllegalStateException("Tracked START requires original statue terminal");
        } else {
            this.terminal();
        }
    }

    private boolean restorePayments() {
        ArrayList<StatueRepairMutation.Payment> var1 = new ArrayList<>(this.payments);
        if (this.pending != null) {
            var1.add(this.pending);
        }

        for (StatueRepairMutation.Payment var3 : var1) {
            int var4 = var3.supply().getCount();
            if (this.player.getInventory().getItem(var3.slot()) != var3.supply()
                || var4 != var3.before() && var4 != var3.before() - var3.requested()
                || !var3.tag().matches(var3.supply())) {
                throw new IllegalStateException("repair payment compensation source changed");
            }
        }

        boolean var5 = false;

        for (StatueRepairMutation.Payment var7 : var1) {
            if (var7.supply().getCount() != var7.before()) {
                var7.supply().setCount(var7.before());
                var5 = true;
            }
        }

        return var5;
    }

    @Override
    public void rollback() {
        if (this.phase != StatueRepairMutation.Phase.ABORTED) {
            if (Thread.currentThread() == this.thread
                && this.phase != StatueRepairMutation.Phase.ABORTING
                && this.phase != StatueRepairMutation.Phase.ABORT_FAILED) {
                this.phase = StatueRepairMutation.Phase.ABORTING;
                boolean var1 = false;

                try {
                    if (this.taskRecord != null) {
                        this.taskRecord.beforeOriginalRollback();
                    }

                    boolean var2 = this.restorePayments && this.restorePayments();
                    if (this.restorePayments && this.delegate instanceof StatueRepairMutation.LegacyCompensation var3) {
                        var3.rollback(var2);
                    } else {
                        this.delegate.rollback();
                    }

                    if (this.taskRecord != null) {
                        this.taskRecord.checkpointAfterOriginalSaveOrRollback();
                    }

                    var1 = true;
                } catch (IOException var8) {
                    throw OwnerStatueTaskService.unavailable(this.player, var8);
                } finally {
                    NativeFoodWeaponWear.abortedStatueRepair(this, this.player, this.weapon);
                    this.pending = null;
                    this.payments.clear();
                    this.phase = var1 ? StatueRepairMutation.Phase.ABORTED : StatueRepairMutation.Phase.ABORT_FAILED;
                }
            } else {
                throw new IllegalStateException("repair compensation unavailable");
            }
        }
    }

    static final class Committed {
        private final StatueRepairMutation outer;
        private final OwnerStatueTaskService.Prepared prepared;
        private boolean taken;

        private Committed(StatueRepairMutation var1, OwnerStatueTaskService.Prepared var2) {
            this.outer = var1;
            this.prepared = var2;
        }

        boolean take(OwnerStatueTaskService.Prepared var1) {
            boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.skip(1L)
                            .findFirst()
                            .map(var0x -> var0x.getDeclaringClass() == OwnerStatueTaskOrigin.class && var0x.getMethodName().equals("outerCommitted"))
                            .orElse(false)
                );
            if (var2 && !this.taken && this.remains(var1)) {
                this.taken = true;
                return true;
            } else {
                return false;
            }
        }

        boolean remains(OwnerStatueTaskService.Prepared var1) {
            return var1 == this.prepared
                && this.outer.taskRecord == this.prepared
                && this.outer.phase == StatueRepairMutation.Phase.COMMITTED
                && Thread.currentThread() == this.outer.thread
                && this.outer.server.isSameThread()
                && this.outer.restorePayments
                && this.outer.request == null
                && StatueRepairMutation.taskUiLive();
        }
    }

    public interface LegacyCompensation extends Mutation {
        void rollback(boolean var1);

        @Override
        default void rollback() {
            this.rollback(false);
        }
    }

    private static record Payment(int slot, ItemStack supply, int requested, int before, int after, NativeOwnerRepairItemData.PaymentComponents tag) {
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
}
