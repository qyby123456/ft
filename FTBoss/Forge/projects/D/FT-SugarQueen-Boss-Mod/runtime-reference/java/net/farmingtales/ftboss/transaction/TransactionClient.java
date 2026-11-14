/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.transaction;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionWire;

public final class TransactionClient {
    public static final int MENU_INTENT_LIMIT = 128;
    public static final int MENU_WAIT_TICKS = 200;
    private final Predicate<TransactionWire.Command> sender;
    private FourWeaponTransactions.Binding binding;
    private UUID opening;
    private FourWeaponTransactions.Request pending;
    private FourWeaponTransactions.Snapshot authority;
    private String synchronizedFingerprint = "";
    private boolean sourceChanged;
    private boolean deliveryUncertain;
    private TransactionWire.TargetOpen target;
    private final ArrayDeque<MenuIntent> menuIntents = new ArrayDeque();
    private boolean queuedSubmission;
    private int queueContainer = -1;
    private int confirmedContainer = -1;
    private int waitingTicks;
    private UUID acceptedRequest;
    private boolean snapshotConfirmed;
    private int[] confirmedLayout;

    public TransactionClient(Predicate<TransactionWire.Command> predicate) {
        this.sender = Objects.requireNonNull(predicate);
    }

    public boolean open(FourWeaponTransactions.Binding binding) {
        this.close();
        this.binding = Objects.requireNonNull(binding);
        this.opening = UUID.randomUUID();
        this.sourceChanged = false;
        this.deliveryUncertain = false;
        boolean bl = this.sender.test(new TransactionWire.Opening(new FourWeaponTransactions.Open(this.opening, binding)));
        if (!bl) {
            this.deliveryUncertain = true;
        }
        return bl;
    }

    public void observe(FourWeaponTransactions.Binding binding, String string) {
        if (this.binding == null || !this.binding.equals(binding)) {
            this.sourceChanged = true;
            this.clearMenu();
        }
        this.synchronizedFingerprint = Objects.requireNonNull(string);
        this.drainMenu();
    }

    public boolean openTarget(int n, String string, long l, UUID uUID, int n2, String string2) {
        this.close();
        this.opening = UUID.randomUUID();
        this.target = new TransactionWire.TargetOpen(this.opening, n, string, l, uUID, n2, string2);
        this.sourceChanged = false;
        this.deliveryUncertain = false;
        boolean bl = this.sender.test(this.target);
        if (!bl) {
            this.deliveryUncertain = true;
        }
        return bl;
    }

    public boolean canWrite(FourWeaponTransactions.Operation operation) {
        return this.menuIntents.isEmpty() && !this.queuedSubmission && this.ready(operation);
    }

    private boolean ready(FourWeaponTransactions.Operation operation) {
        return this.opening == null && this.pending == null && this.authority != null && !this.sourceChanged && !this.deliveryUncertain && this.authority.allowed().contains((Object)operation) && this.authority.version().fingerprint().equals(this.synchronizedFingerprint);
    }

    public boolean submit(UUID uUID, FourWeaponTransactions.Operation operation, int n, int n2, String string) {
        if (!this.canWrite(operation)) {
            return false;
        }
        return this.sendRequest(uUID, operation, n, n2, string);
    }

    private boolean sendRequest(UUID uUID, FourWeaponTransactions.Operation operation, int n, int n2, String string) {
        boolean bl;
        this.pending = new FourWeaponTransactions.Request(uUID, this.authority.session(), this.authority.credential(), this.binding, this.authority.version(), operation, n, n2, string);
        try {
            bl = this.sender.test(new TransactionWire.Submit(this.pending));
        }
        catch (RuntimeException runtimeException) {
            bl = false;
        }
        if (!bl) {
            this.deliveryUncertain = true;
        }
        return bl;
    }

    public boolean enqueueMenu(int n, FourWeaponTransactions.Operation operation, int n2, int n3) {
        if (!FourWeaponTransactions.menuOperation(operation) && operation != FourWeaponTransactions.Operation.REMOVE_CELL) {
            return false;
        }
        if (n2 == -999 && operation != FourWeaponTransactions.Operation.MENU_QUICK_CRAFT) {
            return false;
        }
        if (n < 0 || this.opening != null || this.authority == null || this.sourceChanged || this.deliveryUncertain || this.confirmedContainer != n || !this.authority.allowed().contains((Object)operation) || this.pending != null && !this.queuedSubmission || !this.snapshotConfirmed && !this.queuedSubmission) {
            return false;
        }
        try {
            new FourWeaponTransactions.Request(UUID.randomUUID(), this.authority.session(), this.authority.credential(), this.binding, this.authority.version(), operation, n2, n3, "");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
        if (this.queueContainer != -1 && this.queueContainer != n || this.menuIntents.size() + (this.queuedSubmission ? 1 : 0) >= 128) {
            this.abortMenu();
            return false;
        }
        this.queueContainer = n;
        this.menuIntents.addLast(new MenuIntent(operation, n2, n3));
        this.drainMenu();
        return !this.deliveryUncertain;
    }

    public boolean confirmSnapshot(UUID uUID, UUID uUID2, long l, String string, int n) {
        if (this.sourceChanged || this.deliveryUncertain || this.authority == null || this.acceptedRequest == null || !this.acceptedRequest.equals(uUID) || !this.authority.session().equals(uUID2) || this.authority.version().sequence() != l || !this.authority.version().fingerprint().equals(string)) {
            return false;
        }
        if (this.queuedSubmission && this.queueContainer != n) {
            this.abortMenu();
            return false;
        }
        this.snapshotConfirmed = true;
        this.confirmedContainer = n;
        this.drainMenu();
        return true;
    }

    public boolean confirmSnapshot(UUID uUID, UUID uUID2, long l, String string, String string2, int n, int n2, int[] nArray, int n3) {
        if (this.sourceChanged || this.deliveryUncertain || this.authority == null || this.acceptedRequest == null || !this.acceptedRequest.equals(uUID) || !this.authority.session().equals(uUID2) || this.authority.version().sequence() != l || !this.authority.version().fingerprint().equals(string)) {
            return false;
        }
        boolean bl = nArray != null && nArray.length == 27;
        boolean[] blArray = new boolean[27];
        if (bl) {
            for (int n4 : nArray) {
                if (n4 < 0 || n4 >= 27 || blArray[n4]) {
                    bl = false;
                    break;
                }
                blArray[n4] = true;
            }
        }
        if (!bl || !this.authority.version().rulesFingerprint().equals(string2) || this.authority.version().cavity() != n || this.authority.version().evolution() != n2 || n2 < 0 || n2 > 4 || this.queueContainer != -1 && this.confirmedLayout != null && !Arrays.equals(this.confirmedLayout, nArray)) {
            this.abortMenu();
            return false;
        }
        this.confirmedLayout = (int[])nArray.clone();
        return this.confirmSnapshot(uUID, uUID2, l, string, n3);
    }

    public void menuTick(int n) {
        this.observeContainer(n);
        if (this.queueContainer == -1) {
            return;
        }
        this.drainMenu();
        if (this.queueContainer != -1 && ++this.waitingTicks >= 200) {
            this.abortMenu();
        }
    }

    public void observeContainer(int n) {
        if (this.queueContainer != -1 && n != this.queueContainer) {
            this.close();
        }
    }

    public int queuedMenuIntents() {
        return this.menuIntents.size();
    }

    public boolean menuInputActive() {
        return this.queueContainer != -1;
    }

    private void drainMenu() {
        if (this.sourceChanged || this.deliveryUncertain || this.authority == null || this.pending != null) {
            return;
        }
        if (this.queuedSubmission) {
            if (!this.snapshotConfirmed || !this.authority.version().fingerprint().equals(this.synchronizedFingerprint)) {
                return;
            }
            this.queuedSubmission = false;
            this.waitingTicks = 0;
        }
        if (this.menuIntents.isEmpty()) {
            this.queueContainer = -1;
            return;
        }
        MenuIntent menuIntent = this.menuIntents.peekFirst();
        if (!this.snapshotConfirmed || this.confirmedContainer != this.queueContainer || !this.ready(menuIntent.operation())) {
            return;
        }
        this.menuIntents.removeFirst();
        this.queuedSubmission = true;
        this.snapshotConfirmed = false;
        this.waitingTicks = 0;
        if (!this.sendRequest(UUID.randomUUID(), menuIntent.operation(), menuIntent.part(), menuIntent.value(), "")) {
            this.abortMenu();
        }
    }

    private void clearMenu() {
        this.menuIntents.clear();
        this.queuedSubmission = false;
        this.queueContainer = -1;
        this.waitingTicks = 0;
    }

    private void abortMenu() {
        this.deliveryUncertain = true;
        this.close();
    }

    public boolean receive(FourWeaponTransactions.Reply reply) {
        if (reply == null) {
            this.deliveryUncertain = true;
            this.clearMenu();
            return false;
        }
        if (this.opening != null && this.opening.equals(reply.request())) {
            this.opening = null;
            if (reply.code() == FourWeaponTransactions.Code.OPENED && reply.snapshot() != null && (this.binding != null && this.binding.equals(reply.snapshot().binding()) || this.target != null && this.target.hand() == reply.snapshot().binding().hand() && this.target.dimension().equals(reply.snapshot().binding().dimension()) && this.target.statue() == reply.snapshot().binding().statue() && this.target.weapon().equals(reply.snapshot().binding().weapon()) && this.target.revision() == reply.snapshot().version().cavity() && this.target.fingerprint().equals(reply.snapshot().version().fingerprint()))) {
                this.binding = reply.snapshot().binding();
                this.authority = reply.snapshot();
                this.acceptedRequest = reply.request();
                this.snapshotConfirmed = false;
                this.deliveryUncertain = false;
                return true;
            }
            this.deliveryUncertain = true;
            this.clearMenu();
            return false;
        }
        if (this.pending == null || !this.pending.request().equals(reply.request())) {
            return false;
        }
        FourWeaponTransactions.Snapshot snapshot = reply.snapshot();
        if (snapshot != null && (!snapshot.binding().equals(this.binding) || !snapshot.session().equals(this.pending.session()) || !snapshot.version().epoch().equals(this.pending.expected().epoch()) || snapshot.version().sequence() < this.pending.expected().sequence() || reply.accepted() && snapshot.version().sequence() <= this.pending.expected().sequence())) {
            if (this.queuedSubmission) {
                this.abortMenu();
            }
            return false;
        }
        if (reply.accepted() && snapshot == null) {
            if (this.queuedSubmission) {
                this.abortMenu();
            }
            return false;
        }
        this.authority = snapshot;
        this.pending = null;
        this.deliveryUncertain = snapshot == null || this.queuedSubmission && !reply.accepted();
        this.acceptedRequest = reply.accepted() ? reply.request() : null;
        this.snapshotConfirmed = false;
        if (!reply.accepted()) {
            this.clearMenu();
        }
        return true;
    }

    public FourWeaponTransactions.Snapshot authority() {
        return this.sourceChanged || this.deliveryUncertain ? null : this.authority;
    }

    public FourWeaponTransactions.Request pending() {
        return this.pending;
    }

    public void close() {
        if (this.authority != null) {
            try {
                this.sender.test(new TransactionWire.Close(this.authority.session()));
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
        this.opening = null;
        this.pending = null;
        this.authority = null;
        this.binding = null;
        this.synchronizedFingerprint = "";
        this.target = null;
        this.clearMenu();
        this.acceptedRequest = null;
        this.snapshotConfirmed = false;
        this.confirmedContainer = -1;
        this.confirmedLayout = null;
    }

    private record MenuIntent(FourWeaponTransactions.Operation operation, int part, int value) {
    }
}

