/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;

final class FoodWeaponOriginalSequence {
    private FoodWeaponOriginalSequence() {
    }

    static CoverageReceipt issueOwnerCoverage(OwnerFieldExpectedAuthority.Prepared prepared, Object object) {
        if (!OwnerFieldExpectedAuthority.authorizesIssue(prepared, object)) {
            return null;
        }
        return new CoverageReceipt(prepared, object);
    }

    static Lineage ownerLineage(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, long l) {
        if (!OwnerFieldExpectedAuthority.ownerCaller()) {
            throw new SecurityException("Owner issuer required");
        }
        return new Lineage(object, object2, object3, object4, object5, object6, l);
    }

    static Session prepare(Lineage lineage, FoodWeaponExpectedTrace.Plan plan) {
        return new Session(lineage, plan);
    }

    private static boolean sameText(String string, String string2) {
        return string == string2 || string != null && string.equals(string2);
    }

    static final class CoverageReceipt {
        final Lineage lineage;
        final FoodWeaponExpectedTrace.Plan expected;
        private final OwnerFieldExpectedAuthority.Prepared issuerProof;
        private final Object catalogueGeneration;

        private CoverageReceipt(OwnerFieldExpectedAuthority.Prepared prepared, Object object) {
            this.issuerProof = prepared;
            this.catalogueGeneration = object;
            this.lineage = prepared.lineage;
            this.expected = OwnerFieldExpectedAuthority.expected(prepared);
        }
    }

    static final class Lineage {
        final Object ownerRoot;
        final Object ownerSelection;
        final Object issuer;
        final Object acceptedCallback;
        final Object childScope;
        final Object publication;
        final long generation;

        private Lineage(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, long l) {
            if (object == null || object2 == null || object3 == null || object4 == null || object6 == null) {
                throw new IllegalArgumentException("Actual accepted Owner lineage required");
            }
            this.ownerRoot = object;
            this.ownerSelection = object2;
            this.issuer = object3;
            this.acceptedCallback = object4;
            this.childScope = object5;
            this.publication = object6;
            this.generation = l;
        }
    }

    static final class Session {
        final Lineage lineage;
        final OriginalRun original;
        final FoodWeaponExpectedTrace.Plan expected;
        private final Step[] steps;
        OwnerTimerSemanticAuthority.Book timerSemantics;
        private FoodWeaponExpectedTrace.State expectedCursor;
        private int next;
        private Step active;
        private boolean traceRevoked;
        private boolean coverageInstalled;
        private Throwable firstObservationFailure;
        private Step firstFailedStep;

        private Session(Lineage lineage, FoodWeaponExpectedTrace.Plan plan) {
            this(lineage, plan, null);
        }

        private Session(Lineage lineage, FoodWeaponExpectedTrace.Plan plan, OriginalRun originalRun) {
            if (lineage == null || plan == null) {
                throw new IllegalArgumentException("Independent trace and real lineage required");
            }
            this.lineage = lineage;
            this.expected = plan;
            if (originalRun != null && originalRun.lineage != lineage) {
                throw new IllegalArgumentException("Original branch lineage mismatch");
            }
            this.original = originalRun == null ? new OriginalRun(lineage) : originalRun;
            this.steps = new Step[plan.size()];
            for (int i = 0; i < this.steps.length; ++i) {
                this.steps[i] = new Step(this, plan.operation(i));
            }
            this.expectedCursor = this.steps.length == 0 ? plan.expectedAfter : this.steps[0].expected.before;
        }

        Step originalBegins(Object object, FoodWeaponExpectedTrace.Kind kind, String string, String string2) {
            if (!this.coverageInstalled && !this.traceRevoked && this.active == null && this.next == 0) {
                try {
                    FoodWeaponSourceLedger.tryInstallOwnerCoverage(this);
                }
                catch (Throwable throwable) {
                    this.revoke(null, 0, throwable);
                }
            }
            if (this.timerSemantics != null) {
                if (this.active != null || object == null || !this.original.atLeaf(object)) {
                    this.revoke(null, 0, null);
                    return null;
                }
                try {
                    FoodWeaponExpectedTrace.Operation operation = this.timerSemantics.resolve(this, object, kind, string, string2, this.expectedCursor);
                    if (operation == null || operation.kind != kind || !FoodWeaponOriginalSequence.sameText(operation.key, string) || !FoodWeaponOriginalSequence.sameText(operation.profile, string2) || operation.before != this.expectedCursor) {
                        this.revoke(null, 0, null);
                        return null;
                    }
                    Step step = new Step(this, operation);
                    step.started = true;
                    step.leaf = object;
                    this.active = step;
                    return step;
                }
                catch (Throwable throwable) {
                    this.revoke(null, 0, throwable);
                    return null;
                }
            }
            if (this.active != null || this.next >= this.steps.length || object == null || !this.original.atLeaf(object)) {
                this.revoke(null, 0, null);
                return null;
            }
            Step step = this.steps[this.next];
            if (step.started || step.expected.kind != kind || !FoodWeaponOriginalSequence.sameText(step.expected.key, string) || !FoodWeaponOriginalSequence.sameText(step.expected.profile, string2) || step.expected.before != this.expectedCursor) {
                this.revoke(step, 0, null);
                return null;
            }
            step.started = true;
            step.leaf = object;
            this.active = step;
            return step;
        }

        void prefixObserved(Step step, FoodWeaponExpectedTrace.Domain domain, String[] stringArray, String string, FoodWeaponExpectedTrace.Value value) {
            if (!this.active(step) || step.phase != 0 || step.observedPrefixes >= step.expected.prefixCount() || !step.expected.prefix(step.observedPrefixes).matches(domain, stringArray, string, value)) {
                this.revoke(step, 0, null);
                return;
            }
            ++step.observedPrefixes;
        }

        void originalCopyBegins(Step step, Object object, Object object2) {
            if (!this.active(step) || step.phase != 0 || object == null || object2 == null || step.observedPrefixes != step.expected.prefixCount()) {
                this.revoke(step, 0, null);
                return;
            }
            step.originalReceiver = object;
            step.originalValue = object2;
            step.phase = 1;
        }

        void originalCopyReturned(Step step, Object object, Object object2, Object object3) {
            if (!this.active(step) || step.phase != 1 || object3 == null || step.originalReceiver != object || step.originalValue != object2 || step.originalArgument != null && step.originalArgument != object3) {
                this.revoke(step, 0, null);
                return;
            }
            step.originalArgument = object3;
            step.phase = 2;
        }

        void entryReturned(Step step, Object object, Object object2, Object object3) {
            if (this.active(step) && step.phase == 0 && (step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_TIMER || step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_ROOT)) {
                Object object4;
                Object object5 = object4 = step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_ROOT ? step.expected.before.rangedPlayerIdentity : step.expected.before.rangedBufferIdentity;
                if (!step.expected.before.rangedAcquired || object != object4 || object3 == null || !step.directFieldsVerified || step.originalArgument != object3 || step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_ROOT && object3 != step.expected.before.rangedBufferIdentity) {
                    this.revoke(step, 0, null);
                    return;
                }
                step.originalReceiver = object;
                step.originalValue = object2;
                step.originalArgument = object3;
                step.entered = true;
                step.phase = 3;
                return;
            }
            if (!this.active(step) || step.phase != 2 || step.originalReceiver != object || step.originalValue != object2 || step.originalArgument != object3 || object3 == null || step.observedPrefixes != step.expected.prefixCount()) {
                this.revoke(step, 0, null);
                return;
            }
            step.entered = true;
            step.phase = 3;
        }

        void originalCopyFieldsObserved(Step step, Object object, FoodWeaponExpectedTrace.Value value) {
            if (this.active(step) && (step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_TIMER || step.expected.kind == FoodWeaponExpectedTrace.Kind.RANGED_ROOT)) {
                if (step.phase != 0 || step.directFieldsVerified || object == null || !FoodWeaponExpectedTrace.same(step.expected.argument, value)) {
                    this.revoke(step, 0, null);
                    return;
                }
                step.originalArgument = object;
                step.directFieldsVerified = true;
                return;
            }
            if (!this.active(step) || step.phase < 1 || step.phase > 2 || step.copyVerified || object == null || step.originalArgument != null && step.originalArgument != object || !FoodWeaponExpectedTrace.same(step.expected.argument, value)) {
                this.revoke(step, 0, null);
                return;
            }
            step.originalArgument = object;
            step.copyVerified = true;
        }

        void writeObserved(Step step, Object object, FoodWeaponExpectedTrace.Domain domain, String[] stringArray, String string, FoodWeaponExpectedTrace.Value value) {
            if (!this.active(step) || step.phase != 3 || !step.entered || step.writeObserved || step.originalReceiver != object || step.expected.write == null || !step.expected.write.matches(domain, stringArray, string, value)) {
                this.revoke(step, 1, null);
                return;
            }
            step.writeObserved = true;
            step.phase = 4;
        }

        void originalReturned(Step step, Object object) {
            boolean bl;
            if (!this.active(step) || step.originalReturned || step.phase != (step.expected.write == null ? 3 : 4)) {
                this.revoke(step, 1, null);
                return;
            }
            step.originalReturn = object;
            step.originalReturned = true;
            step.phase = 5;
            boolean bl2 = step.expected.write != null;
            boolean bl3 = bl = step.expected.kind == FoodWeaponExpectedTrace.Kind.TIMER || step.expected.kind == FoodWeaponExpectedTrace.Kind.NATIVE_PUT;
            if (!step.entered || bl2 && (!step.writeObserved || bl && !step.copyVerified || !bl && !step.directFieldsVerified) || step.observedPrefixes != step.expected.prefixCount()) {
                this.revoke(step, 1, null);
            }
        }

        void originalThrown(Step step, Throwable throwable) {
            if (step != null && step.session == this && step.originalFailure == null) {
                step.originalFailure = throwable;
            }
            this.original.originalThrown(throwable);
            this.revoke(step, 2, null);
        }

        void observationFailed(Step step, int n, Throwable throwable) {
            this.revoke(step, n, throwable);
        }

        void close(Step step) {
            if (step == null || step.session != this || step.closed) {
                return;
            }
            if (this.active != step) {
                this.revoke(step, 3, null);
                return;
            }
            if (!step.originalReturned || step.originalFailure != null) {
                this.revoke(step, 3, null);
            }
            if (step.originalReturned) {
                if (this.timerSemantics != null) {
                    try {
                        this.timerSemantics.returned(step, step.leaf, step.originalReturned, step.originalFailure);
                    }
                    catch (Throwable throwable) {
                        this.revoke(step, 3, throwable);
                    }
                }
                this.expectedCursor = step.expected.after;
            }
            if (!this.coverageInstalled) {
                this.traceRevoked = true;
            }
            step.closed = true;
            step.phase = 6;
            this.active = null;
            ++this.next;
        }

        boolean traceComplete() {
            return this.coverageInstalled && !this.traceRevoked && this.active == null && (this.timerSemantics == null ? this.next == this.steps.length && this.expectedCursor == this.expected.expectedAfter : this.timerSemantics.complete());
        }

        boolean semanticNativeBinding(Step step, OwnerNativeSemanticAuthority.PutBinding putBinding) {
            return this.timerSemantics != null && step != null && step.session == this && this.active == step && this.timerSemantics.nativeMatches(this, step, putBinding);
        }

        boolean extraAuthorityRetained() {
            return this.coverageInstalled && !this.traceRevoked;
        }

        boolean bindRegisteredCoverage(CoverageReceipt coverageReceipt) {
            if (this.coverageInstalled || this.active != null || this.next != 0 || this.traceRevoked || coverageReceipt == null || coverageReceipt.lineage != this.lineage || coverageReceipt.expected != this.expected || !OwnerFieldExpectedAuthority.authentic(coverageReceipt.issuerProof, coverageReceipt, coverageReceipt.catalogueGeneration, this)) {
                return false;
            }
            this.coverageInstalled = true;
            return true;
        }

        void interferenceObserved(Object object, long l, Throwable throwable) {
            if (this.lineage.ownerSelection == object && this.lineage.generation == l) {
                this.revoke(this.active, 0, throwable);
            }
        }

        void rangedAcquired(Step step, Object object, Object object2) {
            if (!this.active(step) || step.phase != 0 || step.expected.kind != FoodWeaponExpectedTrace.Kind.RANGED_ACQUIRE || object == null || object2 == null || step.expected.after.rangedPlayerIdentity != object || step.expected.after.rangedBufferIdentity != object2) {
                this.revoke(step, 0, null);
                return;
            }
            step.originalReceiver = object;
            step.originalArgument = object2;
            step.originalReturn = object2;
            step.entered = true;
            step.originalReturned = true;
            step.phase = 5;
        }

        void captureReturned(Step step, Object object) {
            if (!this.active(step) || step.phase != 0 || step.expected.kind != FoodWeaponExpectedTrace.Kind.NATIVE_CAPTURE || step.observedPrefixes != step.expected.prefixCount()) {
                this.revoke(step, 1, null);
                return;
            }
            step.entered = true;
            step.originalReturn = object;
            step.originalReturned = true;
            step.phase = 5;
        }

        private boolean active(Step step) {
            return step != null && step.session == this && this.active == step && !step.closed;
        }

        private void revoke(Step step, int n, Throwable throwable) {
            this.traceRevoked = true;
            if (this.firstFailedStep == null) {
                this.firstFailedStep = step;
            }
            if (this.firstObservationFailure == null) {
                this.firstObservationFailure = throwable;
            }
            if (step != null && step.session == this) {
                if (step.failedPhase == -1) {
                    step.failedPhase = n;
                }
                if (step.observationFailure == null) {
                    step.observationFailure = throwable;
                }
            }
        }
    }

    static final class OriginalOutcomeChoice {
        private final OriginalRun original;
        private final Object producer;
        private final Object acceptedCallback;
        private final int[] outcomes;
        private final Session[] alternatives;
        private boolean selected;

        OriginalOutcomeChoice(Lineage lineage, Object object, Object object2, int[] nArray, FoodWeaponExpectedTrace.Plan[] planArray) {
            if (lineage == null || object == null || object2 != lineage.acceptedCallback || nArray == null || planArray == null || nArray.length == 0 || nArray.length != planArray.length) {
                throw new IllegalArgumentException("Independent original outcome alternatives required");
            }
            this.original = new OriginalRun(lineage);
            this.producer = object;
            this.acceptedCallback = object2;
            this.outcomes = (int[])nArray.clone();
            this.alternatives = new Session[this.outcomes.length];
            for (int i = 0; i < this.outcomes.length; ++i) {
                for (int j = 0; j < i; ++j) {
                    if (this.outcomes[i] != this.outcomes[j]) continue;
                    throw new IllegalArgumentException("Duplicate original outcome alternative");
                }
                this.alternatives[i] = new Session(lineage, planArray[i], this.original);
            }
        }

        OriginalRun originalRun() {
            return this.original;
        }

        Session chooseOriginalReturn(Object object, Object object2, int n) {
            if (this.selected || this.producer != object || this.acceptedCallback != object2 || !this.original.atProducer(this.producer)) {
                return null;
            }
            this.selected = true;
            for (int i = 0; i < this.outcomes.length; ++i) {
                if (this.outcomes[i] != n) continue;
                return this.alternatives[i];
            }
            return null;
        }
    }

    static final class Step {
        final Session session;
        final FoodWeaponExpectedTrace.Operation expected;
        private Object leaf;
        private Object originalReceiver;
        private Object originalValue;
        private Object originalArgument;
        private Object originalReturn;
        private int observedPrefixes;
        private int phase;
        private boolean started;
        private boolean entered;
        private boolean copyVerified;
        private boolean directFieldsVerified;
        private boolean writeObserved;
        private boolean originalReturned;
        private boolean closed;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private int failedPhase = -1;

        private Step(Session session, FoodWeaponExpectedTrace.Operation operation) {
            this.session = session;
            this.expected = operation;
        }
    }

    static final class OriginalRun {
        final Lineage lineage;
        private boolean terminal;
        private Throwable originalFailure;
        private Object activeProducer;
        private Object activeLeaf;
        private Object acceptedCredential;
        private Thread acceptedThread;
        private int producerOrdinal = -1;
        private int leafOrdinal = -1;

        private OriginalRun(Lineage lineage) {
            this.lineage = lineage;
        }

        boolean bindAcceptedPublication(Object object, Object object2) {
            if (this.terminal || this.acceptedCredential != null || object != this.lineage.publication || object2 == null || !FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, object, object2)) {
                return false;
            }
            this.acceptedCredential = object2;
            this.acceptedThread = Thread.currentThread();
            return true;
        }

        boolean continuing(Object object) {
            return !this.terminal && object == this.lineage.publication && this.acceptedCredential != null && this.acceptedThread == Thread.currentThread() && FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, object, this.acceptedCredential);
        }

        boolean enterProducer(Object object, Object object2, int n) {
            if (!this.continuing(object) || object2 == null || this.activeProducer != null || this.activeLeaf != null || n != this.producerOrdinal + 1 || !FoodWeaponFinalDamage.matchesOriginalProducerAdmission(this, object, this.acceptedCredential, object2, n)) {
                return false;
            }
            this.activeProducer = object2;
            this.producerOrdinal = n;
            this.leafOrdinal = -1;
            return true;
        }

        boolean enterLeaf(Object object, Object object2, int n) {
            if (!this.continuing(this.lineage.publication) || object == null || this.activeProducer == null || this.activeProducer != object || this.activeLeaf != null || object2 == null || this.producerOrdinal < 0 || n != this.leafOrdinal + 1 || !FoodWeaponFinalDamage.matchesOriginalLeafAdmission(this, this.lineage.publication, this.acceptedCredential, object, object2, n)) {
                return false;
            }
            this.activeLeaf = object2;
            this.leafOrdinal = n;
            return true;
        }

        boolean atLeaf(Object object) {
            return this.continuing(this.lineage.publication) && this.activeProducer != null && object != null && this.activeLeaf == object;
        }

        boolean atProducer(Object object) {
            return this.continuing(this.lineage.publication) && object != null && this.activeProducer == object;
        }

        boolean enterCurrentLeaf(Object object, int n) {
            return this.activeProducer != null && this.enterLeaf(this.activeProducer, object, n);
        }

        int publishingLeafState(Object object, int n, Object object2) {
            if (object == null || object2 == null || this.acceptedCredential == null || this.acceptedThread != Thread.currentThread() || this.activeProducer != object || this.producerOrdinal != n || this.leafOrdinal != 0 || !FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, this.lineage.publication, this.acceptedCredential)) {
                return -1;
            }
            return this.activeLeaf == object2 ? 1 : (this.activeLeaf == null ? 0 : -1);
        }

        void leaveLeaf(Object object) {
            if (this.activeLeaf == object) {
                this.activeLeaf = null;
            }
        }

        void leaveProducer(Object object) {
            if (this.activeProducer == object && this.activeLeaf == null) {
                this.activeProducer = null;
            }
        }

        void originalThrown(Throwable throwable) {
            if (this.originalFailure == null) {
                this.originalFailure = throwable;
            }
            this.terminal = true;
        }

        void terminal() {
            this.terminal = true;
        }
    }
}

