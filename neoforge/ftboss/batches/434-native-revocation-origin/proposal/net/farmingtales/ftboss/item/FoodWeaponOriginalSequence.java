package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Domain;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Operation;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Plan;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;

final class FoodWeaponOriginalSequence {
    private FoodWeaponOriginalSequence() {
    }

    static FoodWeaponOriginalSequence.CoverageReceipt issueOwnerCoverage(OwnerFieldExpectedAuthority.Prepared var0, Object var1) {
        return !OwnerFieldExpectedAuthority.authorizesIssue(var0, var1) ? null : new FoodWeaponOriginalSequence.CoverageReceipt(var0, var1);
    }

    static FoodWeaponOriginalSequence.Lineage ownerLineage(Object var0, Object var1, Object var2, Object var3, Object var4, Object var5, long var6) {
        if (!OwnerFieldExpectedAuthority.ownerCaller()) {
            throw new SecurityException("Owner issuer required");
        } else {
            return new FoodWeaponOriginalSequence.Lineage(var0, var1, var2, var3, var4, var5, var6);
        }
    }

    static FoodWeaponOriginalSequence.Session prepare(FoodWeaponOriginalSequence.Lineage var0, Plan var1) {
        return new FoodWeaponOriginalSequence.Session(var0, var1);
    }

    private static boolean sameText(String var0, String var1) {
        return var0 == var1 || var0 != null && var0.equals(var1);
    }

    static final class CoverageReceipt {
        final FoodWeaponOriginalSequence.Lineage lineage;
        final Plan expected;
        private final OwnerFieldExpectedAuthority.Prepared issuerProof;
        private final Object catalogueGeneration;

        private CoverageReceipt(OwnerFieldExpectedAuthority.Prepared var1, Object var2) {
            this.issuerProof = var1;
            this.catalogueGeneration = var2;
            this.lineage = var1.lineage;
            this.expected = OwnerFieldExpectedAuthority.expected(var1);
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

        private Lineage(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, long var7) {
            if (var1 != null && var2 != null && var3 != null && var4 != null && var6 != null) {
                this.ownerRoot = var1;
                this.ownerSelection = var2;
                this.issuer = var3;
                this.acceptedCallback = var4;
                this.childScope = var5;
                this.publication = var6;
                this.generation = var7;
            } else {
                throw new IllegalArgumentException("Actual accepted Owner lineage required");
            }
        }
    }

    static final class OriginalOutcomeChoice {
        private final FoodWeaponOriginalSequence.OriginalRun original;
        private final Object producer;
        private final Object acceptedCallback;
        private final int[] outcomes;
        private final FoodWeaponOriginalSequence.Session[] alternatives;
        private boolean selected;

        OriginalOutcomeChoice(FoodWeaponOriginalSequence.Lineage var1, Object var2, Object var3, int[] var4, Plan[] var5) {
            if (var1 != null && var2 != null && var3 == var1.acceptedCallback && var4 != null && var5 != null && var4.length != 0 && var4.length == var5.length
                )
             {
                this.original = new FoodWeaponOriginalSequence.OriginalRun(var1);
                this.producer = var2;
                this.acceptedCallback = var3;
                this.outcomes = (int[])var4.clone();
                this.alternatives = new FoodWeaponOriginalSequence.Session[this.outcomes.length];

                for (int var6 = 0; var6 < this.outcomes.length; var6++) {
                    for (int var7 = 0; var7 < var6; var7++) {
                        if (this.outcomes[var6] == this.outcomes[var7]) {
                            throw new IllegalArgumentException("Duplicate original outcome alternative");
                        }
                    }

                    this.alternatives[var6] = new FoodWeaponOriginalSequence.Session(var1, var5[var6], this.original);
                }
            } else {
                throw new IllegalArgumentException("Independent original outcome alternatives required");
            }
        }

        FoodWeaponOriginalSequence.OriginalRun originalRun() {
            return this.original;
        }

        FoodWeaponOriginalSequence.Session chooseOriginalReturn(Object var1, Object var2, int var3) {
            if (!this.selected && this.producer == var1 && this.acceptedCallback == var2 && this.original.atProducer(this.producer)) {
                this.selected = true;

                for (int var4 = 0; var4 < this.outcomes.length; var4++) {
                    if (this.outcomes[var4] == var3) {
                        return this.alternatives[var4];
                    }
                }

                return null;
            } else {
                return null;
            }
        }
    }

    static final class OriginalRun {
        final FoodWeaponOriginalSequence.Lineage lineage;
        private boolean terminal;
        private Throwable originalFailure;
        private Object activeProducer;
        private Object activeLeaf;
        private Object acceptedCredential;
        private Thread acceptedThread;
        private int producerOrdinal = -1;
        private int leafOrdinal = -1;

        private OriginalRun(FoodWeaponOriginalSequence.Lineage var1) {
            this.lineage = var1;
        }

        boolean bindAcceptedPublication(Object var1, Object var2) {
            if (!this.terminal
                && this.acceptedCredential == null
                && var1 == this.lineage.publication
                && var2 != null
                && FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, var1, var2)) {
                this.acceptedCredential = var2;
                this.acceptedThread = Thread.currentThread();
                return true;
            } else {
                return false;
            }
        }

        boolean continuing(Object var1) {
            return !this.terminal
                && var1 == this.lineage.publication
                && this.acceptedCredential != null
                && this.acceptedThread == Thread.currentThread()
                && FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, var1, this.acceptedCredential);
        }

        boolean enterProducer(Object var1, Object var2, int var3) {
            if (this.continuing(var1)
                && var2 != null
                && this.activeProducer == null
                && this.activeLeaf == null
                && var3 == this.producerOrdinal + 1
                && FoodWeaponFinalDamage.matchesOriginalProducerAdmission(this, var1, this.acceptedCredential, var2, var3)) {
                this.activeProducer = var2;
                this.producerOrdinal = var3;
                this.leafOrdinal = -1;
                return true;
            } else {
                return false;
            }
        }

        boolean enterLeaf(Object var1, Object var2, int var3) {
            if (this.continuing(this.lineage.publication)
                && var1 != null
                && this.activeProducer != null
                && this.activeProducer == var1
                && this.activeLeaf == null
                && var2 != null
                && this.producerOrdinal >= 0
                && var3 == this.leafOrdinal + 1
                && FoodWeaponFinalDamage.matchesOriginalLeafAdmission(this, this.lineage.publication, this.acceptedCredential, var1, var2, var3)) {
                this.activeLeaf = var2;
                this.leafOrdinal = var3;
                return true;
            } else {
                return false;
            }
        }

        boolean atLeaf(Object var1) {
            return this.continuing(this.lineage.publication) && this.activeProducer != null && var1 != null && this.activeLeaf == var1;
        }

        boolean atProducer(Object var1) {
            return this.continuing(this.lineage.publication) && var1 != null && this.activeProducer == var1;
        }

        boolean enterCurrentLeaf(Object var1, int var2) {
            return this.activeProducer != null && this.enterLeaf(this.activeProducer, var1, var2);
        }

        int publishingLeafState(Object var1, int var2, Object var3) {
            if (var1 != null
                && var3 != null
                && this.acceptedCredential != null
                && this.acceptedThread == Thread.currentThread()
                && this.activeProducer == var1
                && this.producerOrdinal == var2
                && this.leafOrdinal == 0
                && FoodWeaponFinalDamage.matchesOriginalRunAdmission(this, this.lineage.publication, this.acceptedCredential)) {
                return this.activeLeaf == var3 ? 1 : (this.activeLeaf == null ? 0 : -1);
            } else {
                return -1;
            }
        }

        void leaveLeaf(Object var1) {
            if (this.activeLeaf == var1) {
                this.activeLeaf = null;
            }
        }

        void leaveProducer(Object var1) {
            if (this.activeProducer == var1 && this.activeLeaf == null) {
                this.activeProducer = null;
            }
        }

        void originalThrown(Throwable var1) {
            if (this.originalFailure == null) {
                this.originalFailure = var1;
            }

            this.terminal = true;
        }

        void terminal() {
            this.terminal = true;
        }
    }

    static final class Session {
        final FoodWeaponOriginalSequence.Lineage lineage;
        final FoodWeaponOriginalSequence.OriginalRun original;
        final Plan expected;
        private final FoodWeaponOriginalSequence.Step[] steps;
        OwnerTimerSemanticAuthority.Book timerSemantics;
        private State expectedCursor;
        private int next;
        private FoodWeaponOriginalSequence.Step active;
        private boolean traceRevoked;
        private boolean coverageInstalled;
        private Throwable firstObservationFailure;
        private Throwable firstRevocationSite434;
        private FoodWeaponOriginalSequence.Step firstFailedStep;

        private Session(FoodWeaponOriginalSequence.Lineage var1, Plan var2) {
            this(var1, var2, null);
        }

        private Session(FoodWeaponOriginalSequence.Lineage var1, Plan var2, FoodWeaponOriginalSequence.OriginalRun var3) {
            if (var1 != null && var2 != null) {
                this.lineage = var1;
                this.expected = var2;
                if (var3 != null && var3.lineage != var1) {
                    throw new IllegalArgumentException("Original branch lineage mismatch");
                } else {
                    this.original = var3 == null ? new FoodWeaponOriginalSequence.OriginalRun(var1) : var3;
                    this.steps = new FoodWeaponOriginalSequence.Step[var2.size()];

                    for (int var4 = 0; var4 < this.steps.length; var4++) {
                        this.steps[var4] = new FoodWeaponOriginalSequence.Step(this, var2.operation(var4));
                    }

                    this.expectedCursor = this.steps.length == 0 ? var2.expectedAfter : this.steps[0].expected.before;
                }
            } else {
                throw new IllegalArgumentException("Independent trace and real lineage required");
            }
        }

        FoodWeaponOriginalSequence.Step originalBegins(Object var1, Kind var2, String var3, String var4) {
            if (!this.coverageInstalled && !this.traceRevoked && this.active == null && this.next == 0) {
                try {
                    FoodWeaponSourceLedger.tryInstallOwnerCoverage(this);
                } catch (Throwable var8) {
                    this.revoke(null, 0, var8);
                }
            }

            if (this.timerSemantics != null) {
                if (this.active == null && var1 != null && this.original.atLeaf(var1)) {
                    try {
                        Operation var9 = this.timerSemantics.resolve(this, var1, var2, var3, var4, this.expectedCursor);
                        if (var9 != null
                            && var9.kind == var2
                            && FoodWeaponOriginalSequence.sameText(var9.key, var3)
                            && FoodWeaponOriginalSequence.sameText(var9.profile, var4)
                            && var9.before == this.expectedCursor) {
                            FoodWeaponOriginalSequence.Step var6 = new FoodWeaponOriginalSequence.Step(this, var9);
                            var6.started = true;
                            var6.leaf = var1;
                            this.active = var6;
                            return var6;
                        } else {
                            this.revoke(null, 0, null);
                            return null;
                        }
                    } catch (Throwable var7) {
                        this.revoke(null, 0, var7);
                        return null;
                    }
                } else {
                    this.revoke(null, 0, null);
                    return null;
                }
            } else if (this.active == null && this.next < this.steps.length && var1 != null && this.original.atLeaf(var1)) {
                FoodWeaponOriginalSequence.Step var5 = this.steps[this.next];
                if (!var5.started
                    && var5.expected.kind == var2
                    && FoodWeaponOriginalSequence.sameText(var5.expected.key, var3)
                    && FoodWeaponOriginalSequence.sameText(var5.expected.profile, var4)
                    && var5.expected.before == this.expectedCursor) {
                    var5.started = true;
                    var5.leaf = var1;
                    this.active = var5;
                    return var5;
                } else {
                    this.revoke(var5, 0, null);
                    return null;
                }
            } else {
                this.revoke(null, 0, null);
                return null;
            }
        }

        void nativeTimerReceiverReturned(FoodWeaponOriginalSequence.Step step,NativeTimerTransitionPlan plan,Object receiver) {
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeExpectedFieldObservation.class) {
                throw new IllegalStateException("Native timer prefix preparation requires the authentic field observer");
            }
            if (this.active(step) && nativeTimerOperation(step.expected) && step.phase == 0 && step.observedPrefixes == 0
                && !step.nativeTimerPrefixesPrepared && !step.nativeTimerComponentAssigned && plan != null
                && plan.sequenceReceiverMatches(this,step,receiver)) {
                step.observedPrefixes = step.expected.prefixCount();
                step.nativeTimerPrefixesPrepared = true;
            } else {
                this.revoke(step,0,null);
            }
        }

        void nativeTimerComponentInstalled(FoodWeaponOriginalSequence.Step step,NativeTimerTransitionPlan plan,Object receiver) {
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeExpectedFieldObservation.class) {
                throw new IllegalStateException("Native timer installation requires the authentic field observer");
            }
            if (this.active(step) && nativeTimerOperation(step.expected) && step.phase == 4 && step.nativeTimerPrefixesPrepared
                && !step.nativeTimerComponentAssigned && step.entered && step.writeObserved && step.copyVerified
                && step.originalReceiver == receiver && step.observedPrefixes == step.expected.prefixCount() && plan != null
                && plan.sequenceInstallationMatches(this,step,receiver)) {
                step.nativeTimerComponentAssigned = true;
            } else {
                this.revoke(step,4,null);
            }
        }

        private static boolean nativeTimerOperation(Operation operation) {
            return operation != null && operation.kind == Kind.TIMER && operation.write != null
                && operation.write.matches(Domain.WEAPON,new String[]{"components","minecraft:custom_data","FTBossFoodEvolution"},operation.key,operation.argument);
        }

        private void nativeTimerReturnedGuard(FoodWeaponOriginalSequence.Step step) {
            if (step != null && nativeTimerOperation(step.expected)
                && (!step.nativeTimerPrefixesPrepared || !step.nativeTimerComponentAssigned)) {
                IllegalStateException failure = new IllegalStateException("Native original TIMER returned before its actual detached preparation and component installation");
                this.revoke(step,1,failure);
                throw failure;
            }
        }

        void prefixObserved(FoodWeaponOriginalSequence.Step var1, Domain var2, String[] var3, String var4, Value var5) {
            if (this.active(var1)
                && var1.phase == 0
                && var1.observedPrefixes < var1.expected.prefixCount()
                && var1.expected.prefix(var1.observedPrefixes).matches(var2, var3, var4, var5)) {
                var1.observedPrefixes++;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void originalCopyBegins(FoodWeaponOriginalSequence.Step var1, Object var2, Object var3) {
            if (this.active(var1) && var1.phase == 0 && var2 != null && var3 != null && var1.observedPrefixes == var1.expected.prefixCount()) {
                var1.originalReceiver = var2;
                var1.originalValue = var3;
                var1.phase = 1;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void originalCopyReturned(FoodWeaponOriginalSequence.Step var1, Object var2, Object var3, Object var4) {
            if (this.active(var1)
                && var1.phase == 1
                && var4 != null
                && var1.originalReceiver == var2
                && var1.originalValue == var3
                && (var1.originalArgument == null || var1.originalArgument == var4)) {
                var1.originalArgument = var4;
                var1.phase = 2;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void entryReturned(FoodWeaponOriginalSequence.Step var1, Object var2, Object var3, Object var4) {
            if (this.active(var1) && var1.phase == 0 && (var1.expected.kind == Kind.RANGED_TIMER || var1.expected.kind == Kind.RANGED_ROOT)) {
                Object var5 = var1.expected.kind == Kind.RANGED_ROOT ? var1.expected.before.rangedPlayerIdentity : var1.expected.before.rangedBufferIdentity;
                if (var1.expected.before.rangedAcquired
                    && var2 == var5
                    && var4 != null
                    && var1.directFieldsVerified
                    && var1.originalArgument == var4
                    && (var1.expected.kind != Kind.RANGED_ROOT || var4 == var1.expected.before.rangedBufferIdentity)) {
                    var1.originalReceiver = var2;
                    var1.originalValue = var3;
                    var1.originalArgument = var4;
                    var1.entered = true;
                    var1.phase = 3;
                } else {
                    this.revoke(var1, 0, null);
                }
            } else if (this.active(var1)
                && var1.phase == 2
                && var1.originalReceiver == var2
                && var1.originalValue == var3
                && var1.originalArgument == var4
                && var4 != null
                && var1.observedPrefixes == var1.expected.prefixCount()) {
                var1.entered = true;
                var1.phase = 3;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void originalCopyFieldsObserved(FoodWeaponOriginalSequence.Step var1, Object var2, Value var3) {
            if (!this.active(var1) || var1.expected.kind != Kind.RANGED_TIMER && var1.expected.kind != Kind.RANGED_ROOT) {
                if (this.active(var1)
                    && var1.phase >= 1
                    && var1.phase <= 2
                    && !var1.copyVerified
                    && var2 != null
                    && (var1.originalArgument == null || var1.originalArgument == var2)
                    && FoodWeaponExpectedTrace.same(var1.expected.argument, var3)) {
                    var1.originalArgument = var2;
                    var1.copyVerified = true;
                } else {
                    this.revoke(var1, 0, null);
                }
            } else if (var1.phase == 0 && !var1.directFieldsVerified && var2 != null && FoodWeaponExpectedTrace.same(var1.expected.argument, var3)) {
                var1.originalArgument = var2;
                var1.directFieldsVerified = true;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void writeObserved(FoodWeaponOriginalSequence.Step var1, Object var2, Domain var3, String[] var4, String var5, Value var6) {
            if (this.active(var1)
                && var1.phase == 3
                && var1.entered
                && !var1.writeObserved
                && var1.originalReceiver == var2
                && var1.expected.write != null
                && var1.expected.write.matches(var3, var4, var5, var6)) {
                var1.writeObserved = true;
                var1.phase = 4;
            } else {
                this.revoke(var1, 1, null);
            }
        }

        void originalReturned(FoodWeaponOriginalSequence.Step var1, Object var2) {
            this.nativeTimerReturnedGuard(var1);
            if (this.active(var1) && !var1.originalReturned && var1.phase == (var1.expected.write == null ? 3 : 4)) {
                var1.originalReturn = var2;
                var1.originalReturned = true;
                var1.phase = 5;
                boolean var3 = var1.expected.write != null;
                boolean var4 = var1.expected.kind == Kind.TIMER || var1.expected.kind == Kind.NATIVE_PUT;
                if (!var1.entered
                    || var3 && (!var1.writeObserved || var4 && !var1.copyVerified || !var4 && !var1.directFieldsVerified)
                    || var1.observedPrefixes != var1.expected.prefixCount()) {
                    this.revoke(var1, 1, null);
                }
            } else {
                this.revoke(var1, 1, null);
            }
        }

        void originalThrown(FoodWeaponOriginalSequence.Step var1, Throwable var2) {
            if (var1 != null && var1.session == this && var1.originalFailure == null) {
                var1.originalFailure = var2;
            }

            this.original.originalThrown(var2);
            this.revoke(var1, 2, null);
        }

        void observationFailed(FoodWeaponOriginalSequence.Step var1, int var2, Throwable var3) {
            this.revoke(var1, var2, var3);
        }

        void close(FoodWeaponOriginalSequence.Step var1) {
            if (var1 != null && var1.session == this && !var1.closed) {
                if (this.active != var1) {
                    this.revoke(var1, 3, null);
                } else {
                    if (!var1.originalReturned || var1.originalFailure != null) {
                        this.revoke(var1, 3, null);
                    }

                    if (var1.originalReturned) {
                        if (this.timerSemantics != null) {
                            try {
                                this.timerSemantics.returned(var1, var1.leaf, var1.originalReturned, var1.originalFailure);
                            } catch (Throwable var3) {
                                this.revoke(var1, 3, var3);
                            }
                        }

                        this.expectedCursor = var1.expected.after;
                    }

                    if (!this.coverageInstalled) {
                        this.traceRevoked = true;
                    }

                    var1.closed = true;
                    var1.phase = 6;
                    this.active = null;
                    this.next++;
                }
            }
        }

        boolean traceComplete() {
            return this.coverageInstalled
                && !this.traceRevoked
                && this.active == null
                && (
                    this.timerSemantics == null
                        ? this.next == this.steps.length && this.expectedCursor == this.expected.expectedAfter
                        : this.timerSemantics.complete()
                );
        }

        boolean semanticNativeBinding(FoodWeaponOriginalSequence.Step var1, OwnerNativeSemanticAuthority.PutBinding var2) {
            return this.timerSemantics != null
                && var1 != null
                && var1.session == this
                && this.active == var1
                && this.timerSemantics.nativeMatches(this, var1, var2);
        }

        boolean extraAuthorityRetained() {
            return this.coverageInstalled && !this.traceRevoked;
        }

        boolean bindRegisteredCoverage(FoodWeaponOriginalSequence.CoverageReceipt var1) {
            if (!this.coverageInstalled
                && this.active == null
                && this.next == 0
                && !this.traceRevoked
                && var1 != null
                && var1.lineage == this.lineage
                && var1.expected == this.expected
                && OwnerFieldExpectedAuthority.authentic(var1.issuerProof, var1, var1.catalogueGeneration, this)) {
                this.coverageInstalled = true;
                return true;
            } else {
                return false;
            }
        }

        void interferenceObserved(Object var1, long var2, Throwable var4) {
            if (this.lineage.ownerSelection == var1 && this.lineage.generation == var2) {
                this.revoke(this.active, 0, var4);
            }
        }

        void rangedAcquired(FoodWeaponOriginalSequence.Step var1, Object var2, Object var3) {
            if (this.active(var1)
                && var1.phase == 0
                && var1.expected.kind == Kind.RANGED_ACQUIRE
                && var2 != null
                && var3 != null
                && var1.expected.after.rangedPlayerIdentity == var2
                && var1.expected.after.rangedBufferIdentity == var3) {
                var1.originalReceiver = var2;
                var1.originalArgument = var3;
                var1.originalReturn = var3;
                var1.entered = true;
                var1.originalReturned = true;
                var1.phase = 5;
            } else {
                this.revoke(var1, 0, null);
            }
        }

        void captureReturned(FoodWeaponOriginalSequence.Step var1, Object var2) {
            if (this.active(var1) && var1.phase == 0 && var1.expected.kind == Kind.NATIVE_CAPTURE && var1.observedPrefixes == var1.expected.prefixCount()) {
                var1.entered = true;
                var1.originalReturn = var2;
                var1.originalReturned = true;
                var1.phase = 5;
            } else {
                this.revoke(var1, 1, null);
            }
        }

        private boolean active(FoodWeaponOriginalSequence.Step var1) {
            return var1 != null && var1.session == this && this.active == var1 && !var1.closed;
        }

        private void revoke(FoodWeaponOriginalSequence.Step var1, int var2, Throwable var3) {
            if (!this.traceRevoked) {
                this.firstRevocationSite434 = new IllegalStateException("Original sequence first revoked at phase " + var2, var3);
            }
            this.traceRevoked = true;
            if (this.firstFailedStep == null) {
                this.firstFailedStep = var1;
            }

            if (this.firstObservationFailure == null) {
                this.firstObservationFailure = var3;
            }

            if (var1 != null && var1.session == this) {
                if (var1.failedPhase == -1) {
                    var1.failedPhase = var2;
                }

                if (var1.observationFailure == null) {
                    var1.observationFailure = var3;
                }
            }
        }
    }

    static final class Step {
        final FoodWeaponOriginalSequence.Session session;
        final Operation expected;
        private Object leaf;
        private Object originalReceiver;
        private Object originalValue;
        private Object originalArgument;
        private Object originalReturn;
        private boolean nativeTimerPrefixesPrepared;
        private boolean nativeTimerComponentAssigned;
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

        private Step(FoodWeaponOriginalSequence.Session var1, Operation var2) {
            this.session = var1;
            this.expected = var2;
        }
    }
}
