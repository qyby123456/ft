/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class NativeExpectedFieldObservation {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal();
    private static final Map<FoodWeaponOriginalSequence.Session, Domain> DOMAINS = new IdentityHashMap<FoodWeaponOriginalSequence.Session, Domain>();
    private static volatile Throwable infrastructureFailure;

    private NativeExpectedFieldObservation() {
    }

    public static Throwable infrastructureFailure() {
        return infrastructureFailure;
    }

    private static Map<String, Tag> fields(CompoundTag compoundTag) {
        Map<String, Tag> map = ((NativeExpectedFieldViews.Compound)compoundTag).ftboss$rawCompoundFields();
        if (map == null || map.getClass() != HashMap.class) {
            throw new IllegalArgumentException("Unsupported raw backing-map implementation");
        }
        return map;
    }

    private static CompoundTag stackTag(ItemStack itemStack) {
        return ((NativeExpectedFieldViews.Stack)itemStack).ftboss$rawStackTag();
    }

    private static Scope current() {
        try {
            Scope scope = CURRENT.get();
            if (scope != null && (scope.closed || scope.thread != Thread.currentThread())) {
                NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Saved operation scope is closed or on another thread"));
                return null;
            }
            return scope;
        }
        catch (Throwable throwable) {
            infrastructureFailure = throwable;
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void rejectExtra(Scope scope, Throwable throwable) {
        block10: {
            if (scope == null) {
                infrastructureFailure = throwable;
                return;
            }
            scope.retired = true;
            if (scope.observationFailure == null) {
                scope.observationFailure = throwable;
            }
            if (scope.domain != null) {
                Map<FoodWeaponOriginalSequence.Session, Domain> map = DOMAINS;
                synchronized (map) {
                    scope.domain.retired = true;
                    if (scope.domain.failure == null) {
                        scope.domain.failure = throwable;
                    }
                }
            }
            try {
                if (scope.sequence != null) {
                    scope.sequence.observationFailed(scope.step, 0, throwable);
                }
            }
            catch (Throwable throwable2) {
                if (scope.observationFailure != null) break block10;
                scope.observationFailure = throwable2;
            }
        }
    }

    private static void failed(Scope scope, Throwable throwable) {
        NativeExpectedFieldObservation.rejectExtra(scope, throwable);
    }

    private static boolean eligible(Scope scope) {
        if (scope == null || scope.retired || scope.closed || scope.domain == null || scope.domain.retired) {
            return false;
        }
        Domain domain = scope.domain;
        if (domain.sourceA != scope.sourceA || domain.thread != Thread.currentThread() || domain.sequence != scope.sequence || domain.generation != scope.sequence.lineage.generation || domain.finalPublication != scope.finalPublication || domain.finalPublication != scope.sequence.lineage.publication || domain.ownerWriter != scope.writer || domain.ownerWriter != scope.sequence.lineage.issuer || !(scope.writer instanceof FoodWeaponSourceLedger.OwnerWriterPublication) || !scope.sequence.original.atLeaf(scope.leaf)) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Saved source/publication/generation/thread/leaf domain differs"));
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void bind(Scope scope, FoodWeaponOriginalSequence.Session session) {
        scope.sequence = session;
        if (scope.sequence == null) {
            return;
        }
        Map<FoodWeaponOriginalSequence.Session, Domain> map = DOMAINS;
        synchronized (map) {
            scope.domain = DOMAINS.get(scope.sequence);
            if (scope.domain == null) {
                scope.domain = new Domain(scope.sequence, scope.sourceA);
                DOMAINS.put(scope.sequence, scope.domain);
            }
        }
        NativeExpectedFieldObservation.eligible(scope);
    }

    private static Scope open(ItemStack itemStack, Object object, Object object2, Object object3, ServerPlayer serverPlayer) {
        Scope scope = NativeExpectedFieldObservation.current();
        FoodWeaponOriginalSequence.Session session = object == null ? null : FoodWeaponHitPlan.originalSequence((FoodWeaponHitPlan.FinalEffectsCommit)object);
        Scope scope2 = new Scope(itemStack, object, object2, serverPlayer != null && object3 == null && session != null ? session.lineage.issuer : object3, session == null ? null : session.lineage.publication, serverPlayer, scope);
        try {
            CURRENT.set(scope2);
            if (object != null) {
                NativeExpectedFieldObservation.bind(scope2, session);
            }
            Scope scope3 = scope;
            while (scope3 != null) {
                if (scope3.sequence != null && scope3.sequence == scope2.sequence) {
                    NativeExpectedFieldObservation.rejectExtra(scope2, new IllegalStateException("Reentrant use of saved original sequence"));
                }
                scope3 = scope3.previous;
            }
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope2, throwable);
        }
        return scope2;
    }

    private static void close(Scope scope) {
        if (scope == null) {
            return;
        }
        try {
            if (scope.previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(scope.previous);
            }
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        finally {
            scope.closed = true;
        }
    }

    private static Fact fact(Scope scope, String string, Object object, Object object2, Object object3, Operation<?> operation) {
        try {
            if (scope == null) {
                return null;
            }
            if (scope.closed || scope.thread != Thread.currentThread()) {
                throw new IllegalStateException("Foreign event thread");
            }
            Fact fact = new Fact(scope, string, object, object2, object3, operation);
            scope.facts.add(fact);
            return fact;
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
            return null;
        }
    }

    private static void before(Fact fact) {
        if (fact == null) {
            return;
        }
        Scope scope = fact.scope;
        if (scope.active != fact.parent || fact.thread != scope.thread || fact.lifecycleDomain != scope.domain || fact.sourceA != scope.sourceA || fact.operation != scope.operation || fact.lifecycleDomain != null && fact.generation != fact.lifecycleDomain.generation) {
            throw new IllegalStateException("Saved original operation/source/generation/parent entry order differs");
        }
        scope.active = fact;
        if (fact.site.equals("MAP_PUT")) {
            NativeExpectedFieldObservation.beforeMap(fact);
        } else if (fact.site.equals("COPY")) {
            NativeExpectedFieldObservation.beforeCopy(fact);
        } else if (fact.site.equals("TIMER_PUT")) {
            NativeExpectedFieldObservation.beforeTimerPut(fact);
        } else if (fact.site.equals("RANGED_LONG") || fact.site.equals("RANGED_ROOT")) {
            NativeExpectedFieldObservation.beforeRanged(fact);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static <Result> Result invoke(Fact fact, Operation<Result> operation, Object ... objectArray) {
        Object object2;
        try {
            NativeExpectedFieldObservation.before(fact);
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(fact == null ? null : fact.scope, throwable);
        }
        try {
            if (fact != null) {
                fact.attempted = true;
            }
            Object object = operation.call(objectArray);
            if (fact != null) {
                fact.originalReturn = object;
                fact.returned = true;
            }
            try {
                NativeExpectedFieldObservation.after(fact);
            }
            catch (Throwable throwable) {
                NativeExpectedFieldObservation.failed(fact == null ? null : fact.scope, throwable);
            }
            object2 = object;
        }
        catch (Throwable throwable) {
            try {
                if (fact == null) throw throwable;
                fact.originalFailure = throwable;
                fact.scope.originalFailure = throwable;
                throw throwable;
            }
            catch (Throwable throwable2) {
                try {
                    NativeExpectedFieldObservation.finish(fact);
                    throw throwable2;
                }
                catch (Throwable throwable3) {
                    NativeExpectedFieldObservation.failed(fact == null ? null : fact.scope, throwable3);
                }
                throw throwable2;
            }
        }
        try {
            NativeExpectedFieldObservation.finish(fact);
            return (Result)object2;
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(fact == null ? null : fact.scope, throwable);
        }
        return (Result)object2;
    }

    private static void finish(Fact fact) {
        if (fact == null) {
            return;
        }
        Scope scope = fact.scope;
        if (scope.active != fact) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Original child/return/throw order differs"));
        }
        fact.exit = ++scope.clock;
        scope.active = fact.parent;
        if (fact.site.equals("RANGED_LONG") || fact.site.equals("RANGED_ROOT")) {
            NativeExpectedFieldObservation.finishRanged(scope, fact);
        }
    }

    private static boolean parent(Fact fact, String string) {
        return fact.parent != null && fact.parent.scope == fact.scope && fact.parent.site.equals(string) && fact.parent.attempted && !fact.parent.returned && fact.parent.originalFailure == null && fact.parent.exit == 0L;
    }

    private static void require(boolean bl, Scope scope, String string) {
        if (!bl) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException(string));
        }
    }

    private static CompoundTag ownedTimerRoot(Scope scope) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2 = NativeExpectedFieldObservation.stackTag(scope.sourceA);
        if (compoundTag2 == null || compoundTag2 != scope.sourceTag) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Original source tag pointer changed"));
            return null;
        }
        Tag tag = NativeExpectedFieldObservation.fields(compoundTag2).get("FTBossFoodEvolution");
        return tag instanceof CompoundTag ? (compoundTag = (CompoundTag)tag) : null;
    }

    private static Binding binding(Scope scope) {
        if (scope.acquisitionRoot == null || scope.rangedBuffer == null) {
            return null;
        }
        Binding binding = new Binding(scope.acquisitionRoot, scope.acquisitionKey, scope.rangedBuffer);
        if (scope.persistentRoot != binding.root || binding.root != ((NativeExpectedFieldViews.Actor)scope.player).ftboss$rawPersistentData()) {
            scope.unsupportedTopology = true;
        }
        if (!(scope.lastBinding == null || scope.lastBinding.bound == binding.bound || scope.active != null && (scope.active.site.equals("RANGED_ROOT") || scope.active.site.equals("MAP_PUT") && NativeExpectedFieldObservation.parent(scope.active, "RANGED_ROOT")))) {
            scope.unsupportedTopology = true;
        }
        scope.lastBinding = binding;
        if (scope.unsupportedTopology) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Unplanned original root detachment or rebind"));
        }
        return binding;
    }

    private static void beforeCopy(Fact fact) {
        Scope scope = fact.scope;
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "TIMER") && scope.step != null && scope.timerReceiver != null && scope.timerReceiver == NativeExpectedFieldObservation.ownedTimerRoot(scope) && fact.receiver == scope.timerValue && !scope.copyAttempted, scope, "Copy receiver/value/root or parent operation differs");
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        scope.copyAttempted = true;
        scope.copyValue = (Tag)fact.receiver;
    }

    private static void beforeTimerPut(Fact fact) {
        Scope scope = fact.scope;
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "TIMER") && scope.step != null && !scope.putAttempted && scope.copyReturned && scope.timerReceiver == NativeExpectedFieldObservation.ownedTimerRoot(scope) && fact.receiver == scope.timerReceiver && Objects.equals(scope.timerKey, fact.key) && fact.argument == scope.copyResult && scope.copyValue == scope.timerValue && scope.step.expected.kind == FoodWeaponExpectedTrace.Kind.TIMER && Objects.equals(scope.step.expected.key, fact.key), scope, "Timer physical receiver/key/copy-result or planned operation differs");
        scope.putAttempted = true;
    }

    private static void beforeRanged(Fact fact) {
        Scope scope = fact.scope;
        fact.bindingBefore = NativeExpectedFieldObservation.binding(scope);
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        boolean bl = fact.site.equals("RANGED_ROOT");
        NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "RANGED_SAVE") && scope.acquisitionRoot != null && "FTBossRangedProcTimers".equals(scope.acquisitionKey) && (bl ? fact.receiver == scope.acquisitionRoot && fact.argument == scope.rangedBuffer && Objects.equals(fact.key, scope.acquisitionKey) : fact.receiver == scope.rangedBuffer), scope, "Original ranged receiver/key/acquisition or parent differs");
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        scope.kind = bl ? FoodWeaponExpectedTrace.Kind.RANGED_ROOT : FoodWeaponExpectedTrace.Kind.RANGED_TIMER;
        fact.originalStep = scope.step = scope.sequence.originalBegins(scope.leaf, scope.kind, (String)fact.key, null);
        if (scope.step == null) {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("No original preplanned ranged step"));
            return;
        }
        FoodWeaponExpectedTrace.State state = scope.step.expected.before;
        NativeExpectedFieldObservation.require(state.rangedAcquired && state.rangedPlayerIdentity == scope.acquisitionRoot && state.rangedBufferIdentity == scope.rangedBuffer && state.rangedAttached == (fact.bindingBefore.bound == scope.rangedBuffer) && FoodWeaponExpectedTrace.same(state.player, NativeExpectedRawFields.observe((Tag)scope.acquisitionRoot)) && FoodWeaponExpectedTrace.same(state.rangedBuffer, fact.bindingBefore.acquiredFields), scope, "Ranged expected domain identities/path/fields differ");
        if (NativeExpectedFieldObservation.eligible(scope) && bl) {
            scope.sequence.originalCopyFieldsObserved(scope.step, fact.argument, NativeExpectedRawFields.observe((Tag)fact.argument));
            scope.sequence.entryReturned(scope.step, fact.receiver, fact.argument, fact.argument);
        }
    }

    private static void beforeMap(Fact fact) {
        Scope scope = fact.scope;
        fact.bindingBefore = NativeExpectedFieldObservation.binding(scope);
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        NativeExpectedFieldObservation.require(fact.originalStep == scope.step && fact.key instanceof String && fact.argument instanceof Tag && fact.parent != null && fact.parent.scope == scope && fact.parent.exit == 0L && fact.parent.attempted, scope, "Map write lacks exact original parent entry");
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        NativeExpectedFieldObservation.require(fact.physicalOwner != null && NativeExpectedFieldObservation.fields(fact.physicalOwner) == fact.receiver, scope, "Foreign physical backing map");
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        Fact fact2 = fact.parent;
        if (NativeExpectedFieldObservation.parent(fact, "TIMER_PUT") || NativeExpectedFieldObservation.parent(fact, "RANGED_ROOT")) {
            NativeExpectedFieldObservation.require(fact.physicalOwner == fact2.receiver && Objects.equals(fact.key, fact2.key) && fact.argument == fact2.argument && fact2.directMapChildren == 0, scope, "Original put backing-map receiver/key/argument or child count differs");
        } else if (NativeExpectedFieldObservation.parent(fact, "RANGED_LONG")) {
            NativeExpectedFieldObservation.require(fact.physicalOwner == scope.rangedBuffer && fact.physicalOwner == fact2.receiver && Objects.equals(fact.key, fact2.key) && fact2.directMapChildren == 0 && fact.argument.getClass() == LongTag.class && ((NativeExpectedFieldViews.LongValue)fact.argument).ftboss$rawLong() == ((Long)fact2.argument).longValue(), scope, "Foreign nested Map or substituted original LongTag");
            if (NativeExpectedFieldObservation.eligible(scope)) {
                fact.expectedFieldDomain = FoodWeaponExpectedTrace.Domain.RANGED_BUFFER;
                fact.path = new String[0];
                scope.sequence.originalCopyFieldsObserved(scope.step, fact.argument, NativeExpectedRawFields.observe((Tag)fact.argument));
                scope.sequence.entryReturned(scope.step, fact.physicalOwner, null, fact.argument);
            }
        } else if (NativeExpectedFieldObservation.parent(fact, "RECEIVER")) {
            if (fact.physicalOwner == scope.sourceTag && "FTBossFoodEvolution".equals(fact.key)) {
                fact.path = new String[]{"tag"};
            } else if (fact.physicalOwner == NativeExpectedFieldObservation.ownedTimerRoot(scope) && "Profile".equals(fact.key)) {
                fact.path = new String[]{"tag", "FTBossFoodEvolution"};
            }
            NativeExpectedFieldObservation.require(fact.path != null && scope.step != null && scope.prefixes < scope.step.expected.prefixCount(), scope, "Foreign original prefix owner/key/path");
            if (NativeExpectedFieldObservation.eligible(scope)) {
                fact.expectedFieldDomain = FoodWeaponExpectedTrace.Domain.WEAPON;
                NativeExpectedFieldObservation.require(scope.step.expected.prefix(scope.prefixes).matches(fact.expectedFieldDomain, fact.path, (String)fact.key, NativeExpectedRawFields.observe((Tag)fact.argument)), scope, "Original prefix differs from independent expected mutation");
            }
        } else {
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Nested or unbound Map write cannot claim a domain"));
        }
        if (NativeExpectedFieldObservation.eligible(scope)) {
            ++fact2.directMapChildren;
        }
    }

    private static void after(Fact fact) {
        if (fact == null) {
            return;
        }
        Scope scope = fact.scope;
        if (fact.site.equals("PERSISTENT")) {
            NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "RANGED_SAVE") && fact.receiver == scope.player, scope, "Foreign original player getter receiver");
            CompoundTag compoundTag = (CompoundTag)fact.originalReturn;
            NativeExpectedFieldObservation.require(compoundTag == ((NativeExpectedFieldViews.Actor)fact.receiver).ftboss$rawPersistentData() && (scope.persistentRoot == null || scope.persistentRoot == compoundTag), scope, "Original persistent getter returned foreign root");
            if (scope.persistentRoot == null) {
                scope.persistentRoot = compoundTag;
            }
        } else if (fact.site.equals("ACQUIRE")) {
            NativeExpectedFieldObservation.acquire(fact);
        } else if (fact.site.equals("RECEIVER")) {
            NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "TIMER") && fact.receiver == scope.sourceA && scope.timerReceiver == null && fact.originalReturn == NativeExpectedFieldObservation.ownedTimerRoot(scope), scope, "writableData returned foreign source root");
            if (NativeExpectedFieldObservation.eligible(scope)) {
                scope.timerReceiver = (CompoundTag)fact.originalReturn;
            }
        } else if (fact.site.equals("COPY")) {
            if (scope.copyAttempted && !scope.copyReturned) {
                scope.copyResult = (Tag)fact.originalReturn;
                scope.copyReturned = true;
            }
            if (NativeExpectedFieldObservation.eligible(scope) && scope.step != null) {
                fact.rawFields = NativeExpectedRawFields.observe(scope.copyResult);
                scope.sequence.originalCopyFieldsObserved(scope.step, scope.copyResult, fact.rawFields);
            }
        } else if (fact.site.equals("MAP_PUT")) {
            NativeExpectedFieldObservation.afterMap(fact);
        } else if (fact.site.equals("TIMER_PUT")) {
            if (NativeExpectedFieldObservation.eligible(scope)) {
                NativeExpectedFieldObservation.require(fact.directMapChildren == 1 && fact.receiver == NativeExpectedFieldObservation.ownedTimerRoot(scope), scope, "Timer original chain lacks one exact backing-map child");
                if (NativeExpectedFieldObservation.eligible(scope)) {
                    scope.sequence.writeObserved(scope.step, fact.receiver, FoodWeaponExpectedTrace.Domain.WEAPON, new String[]{"tag", "FTBossFoodEvolution"}, (String)fact.key, NativeExpectedRawFields.observe((Tag)fact.argument));
                }
            }
        } else if (fact.site.equals("RANGED_LONG") || fact.site.equals("RANGED_ROOT")) {
            fact.bindingAfter = NativeExpectedFieldObservation.binding(scope);
            if (NativeExpectedFieldObservation.eligible(scope)) {
                NativeExpectedFieldObservation.require(fact.directMapChildren == 1, scope, "Ranged original chain lacks one exact backing-map child");
                if (NativeExpectedFieldObservation.eligible(scope) && fact.site.equals("RANGED_ROOT")) {
                    scope.sequence.writeObserved(scope.step, fact.receiver, FoodWeaponExpectedTrace.Domain.PLAYER, new String[0], (String)fact.key, NativeExpectedRawFields.observe((Tag)fact.argument));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void acquire(Fact fact) {
        FoodWeaponOriginalSequence.Step step;
        Scope scope = fact.scope;
        NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.parent(fact, "RANGED_SAVE") && fact.receiver == scope.persistentRoot && fact.receiver == ((NativeExpectedFieldViews.Actor)scope.player).ftboss$rawPersistentData() && "FTBossRangedProcTimers".equals(fact.key) && scope.acquisitionRoot == null, scope, "Foreign or repeated original compound acquisition");
        if (scope.acquisitionRoot == null) {
            scope.acquisitionRoot = (CompoundTag)fact.receiver;
            scope.acquisitionKey = (String)fact.key;
            scope.rangedBuffer = (CompoundTag)fact.originalReturn;
            fact.bindingAfter = NativeExpectedFieldObservation.binding(scope);
            fact.rawFields = NativeExpectedRawFields.observe((Tag)scope.rangedBuffer);
        }
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        fact.originalStep = step = scope.sequence.originalBegins(scope.leaf, FoodWeaponExpectedTrace.Kind.RANGED_ACQUIRE, (String)fact.key, null);
        try {
            NativeExpectedFieldObservation.require(step != null && step.expected.after.rangedPlayerIdentity == fact.receiver && step.expected.after.rangedBufferIdentity == fact.originalReturn, scope, "Original getter identities differ from independently preplanned acquisition");
            if (NativeExpectedFieldObservation.eligible(scope)) {
                scope.sequence.rangedAcquired(step, fact.receiver, fact.originalReturn);
            }
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        finally {
            try {
                if (step != null) {
                    scope.sequence.close(step);
                }
            }
            catch (Throwable throwable) {
                NativeExpectedFieldObservation.failed(scope, throwable);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void finishRanged(Scope scope, Fact fact) {
        FoodWeaponOriginalSequence.Step step = fact.originalStep;
        if (step == null || scope.kind != FoodWeaponExpectedTrace.Kind.RANGED_TIMER && scope.kind != FoodWeaponExpectedTrace.Kind.RANGED_ROOT) {
            return;
        }
        try {
            if (fact.originalFailure != null) {
                scope.sequence.originalThrown(step, fact.originalFailure);
            } else if (NativeExpectedFieldObservation.eligible(scope)) {
                scope.sequence.originalReturned(step, fact.originalReturn);
            }
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        finally {
            try {
                scope.sequence.close(step);
            }
            catch (Throwable throwable) {
                NativeExpectedFieldObservation.failed(scope, throwable);
            }
            scope.step = null;
            scope.kind = null;
        }
    }

    private static void afterMap(Fact fact) {
        Scope scope = fact.scope;
        fact.bindingAfter = NativeExpectedFieldObservation.binding(scope);
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.fields(fact.physicalOwner) == fact.receiver && NativeExpectedFieldObservation.fields(fact.physicalOwner).get((String)fact.key) == fact.argument, scope, "Original map chain did not publish its exact argument to the bound backing map");
        if (!NativeExpectedFieldObservation.eligible(scope)) {
            return;
        }
        fact.rawFields = NativeExpectedRawFields.observe((Tag)fact.argument);
        if (NativeExpectedFieldObservation.parent(fact, "RECEIVER")) {
            NativeExpectedFieldObservation.require(NativeExpectedFieldObservation.stackTag(scope.sourceA) == scope.sourceTag && (fact.path.length == 1 ? NativeExpectedFieldObservation.ownedTimerRoot(scope) == fact.argument : NativeExpectedFieldObservation.ownedTimerRoot(scope) == fact.physicalOwner), scope, "Original prefix backing map is no longer attached to the saved source path");
            if (!NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            scope.sequence.prefixObserved(scope.step, fact.expectedFieldDomain, fact.path, (String)fact.key, fact.rawFields);
            ++scope.prefixes;
        } else if (NativeExpectedFieldObservation.parent(fact, "RANGED_LONG")) {
            scope.sequence.writeObserved(scope.step, fact.physicalOwner, fact.expectedFieldDomain, fact.path, (String)fact.key, fact.rawFields);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void timer(ItemStack itemStack, FoodWeaponEvolution.Profile profile, String string, Tag tag, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, Object object, int n, Object object2, Object object3, Operation<Void> operation) {
        Scope scope = null;
        try {
            scope = NativeExpectedFieldObservation.open(itemStack, object, object3, object2, null);
            scope.timerKey = string;
            scope.timerValue = tag;
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        try {
            NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(scope, "TIMER", itemStack, string, tag, operation), operation, new Object[]{itemStack, profile, string, tag, ownerWriterStep, object, n, object2, object3});
        }
        finally {
            NativeExpectedFieldObservation.close(scope);
        }
    }

    public static void actualTimerStep(Object object, Object object2, Object object3) {
        Scope scope = NativeExpectedFieldObservation.current();
        try {
            if (scope == null) {
                return;
            }
            NativeExpectedFieldObservation.require(scope.commit == object && scope.leaf == object2 && scope.step == null && scope.active != null && scope.active.site.equals("TIMER"), scope, "Actual TIMER step has foreign caller/parent");
            if (!NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            scope.step = (FoodWeaponOriginalSequence.Step)object3;
            NativeExpectedFieldObservation.require(scope.step != null && scope.step.session == scope.sequence && scope.step.expected.kind == FoodWeaponExpectedTrace.Kind.TIMER && Objects.equals(scope.timerKey, scope.step.expected.key), scope, "Original returned TIMER step identity differs");
            if (!NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            scope.kind = FoodWeaponExpectedTrace.Kind.TIMER;
            scope.sourceTag = NativeExpectedFieldObservation.stackTag(scope.sourceA);
            NativeExpectedFieldObservation.require(FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.field(scope.step.expected.before.weapon, "tag"), NativeExpectedRawFields.observe((Tag)scope.sourceTag)), scope, "Initial source tag differs from independent expected BEFORE");
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
    }

    public static CompoundTag timerReceiver(ItemStack itemStack, FoodWeaponEvolution.Profile profile, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "RECEIVER", itemStack, null, (Object)profile, operation), operation, new Object[]{itemStack, profile});
    }

    public static Tag copiedArgument(Tag tag, Operation<Tag> operation) {
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "COPY", tag, null, null, operation), operation, tag);
    }

    public static Tag timerPut(FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, ItemStack itemStack, CompoundTag compoundTag, String string, Tag tag, Operation<Tag> operation) {
        Scope scope = NativeExpectedFieldObservation.current();
        try {
            if (scope != null) {
                NativeExpectedFieldObservation.require(scope.sourceA == itemStack, scope, "Original TIMER sourceA differs");
            }
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(scope, "TIMER_PUT", compoundTag, string, tag, operation), operation, compoundTag, string, tag);
    }

    public static Object mapPut(CompoundTag compoundTag, Map<Object, Object> map, Object object, Object object2, Operation<Object> operation) {
        Scope scope = NativeExpectedFieldObservation.current();
        Fact fact = NativeExpectedFieldObservation.fact(scope, "MAP_PUT", map, object, object2, operation);
        if (fact != null) {
            fact.physicalOwner = compoundTag;
        }
        return NativeExpectedFieldObservation.invoke(fact, operation, map, object, object2);
    }

    public static CompoundTag persistentData(Entity entity, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "PERSISTENT", entity, null, null, operation), operation, entity);
    }

    public static CompoundTag compound(CompoundTag compoundTag, String string, Operation<CompoundTag> operation) {
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "ACQUIRE", compoundTag, string, null, operation), operation, compoundTag, string);
    }

    public static void rangedLong(CompoundTag compoundTag, String string, long l, Operation<Void> operation) {
        NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "RANGED_LONG", compoundTag, string, l, operation), operation, compoundTag, string, l);
    }

    public static Tag rangedRoot(CompoundTag compoundTag, String string, Tag tag, Operation<Tag> operation) {
        return NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(NativeExpectedFieldObservation.current(), "RANGED_ROOT", compoundTag, string, tag, operation), operation, compoundTag, string, tag);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void rangedSave(ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag, ItemStack itemStack, Object object, Object object2, Operation<Void> operation) {
        Scope scope = null;
        try {
            scope = NativeExpectedFieldObservation.open(itemStack, object, object2, null, serverPlayer);
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        try {
            NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(scope, "RANGED_SAVE", serverPlayer, damageSource, compoundTag, operation), operation, serverPlayer, damageSource, compoundTag);
        }
        finally {
            NativeExpectedFieldObservation.close(scope);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static CompoundTag capture(ServerPlayer serverPlayer, ItemStack itemStack, FoodWeaponFamilyRules.Kind kind, InteractionHand interactionHand, Operation<CompoundTag> operation) {
        Scope scope = null;
        try {
            scope = NativeExpectedFieldObservation.open(itemStack, null, null, null, serverPlayer);
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
        try {
            CompoundTag compoundTag = NativeExpectedFieldObservation.invoke(NativeExpectedFieldObservation.fact(scope, "CAPTURE", itemStack, (Object)kind, interactionHand, operation), operation, new Object[]{serverPlayer, itemStack, kind, interactionHand});
            return compoundTag;
        }
        finally {
            NativeExpectedFieldObservation.close(scope);
        }
    }

    public static void originalTagAssigned(ItemStack itemStack, CompoundTag compoundTag) {
        Scope scope = NativeExpectedFieldObservation.current();
        try {
            Fact fact = NativeExpectedFieldObservation.fact(scope, "SOURCE_TAG_ASSIGNED", itemStack, "tag", compoundTag, null);
            if (fact != null) {
                fact.originalReturn = compoundTag;
                fact.returned = true;
                fact.exit = ++scope.clock;
            }
            if (scope == null || !NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            NativeExpectedFieldObservation.require(scope.active != null && scope.active.site.equals("RECEIVER") && scope.sourceA == itemStack && scope.sourceTag == null && compoundTag == NativeExpectedFieldObservation.stackTag(itemStack) && scope.step != null && scope.prefixes < scope.step.expected.prefixCount(), scope, "Foreign original source tag assignment");
            if (!NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            FoodWeaponExpectedTrace.Value value = NativeExpectedRawFields.observe((Tag)compoundTag);
            NativeExpectedFieldObservation.require(scope.step.expected.prefix(scope.prefixes).matches(FoodWeaponExpectedTrace.Domain.WEAPON, new String[0], "tag", value), scope, "Source tag assignment is not the independent expected prefix");
            if (!NativeExpectedFieldObservation.eligible(scope)) {
                return;
            }
            scope.sourceTag = compoundTag;
            scope.sequence.prefixObserved(scope.step, FoodWeaponExpectedTrace.Domain.WEAPON, new String[0], "tag", value);
            ++scope.prefixes;
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
    }

    public static void aliasReturned(Object object, Object object2, Object object3) {
        Scope scope = NativeExpectedFieldObservation.current();
        Fact fact = NativeExpectedFieldObservation.fact(scope, "ALIAS_RETURN", object, object2, null, null);
        if (fact != null) {
            fact.originalReturn = object3;
            fact.returned = true;
            fact.exit = ++scope.clock;
        }
    }

    public static void providerFields(Object object) {
        Scope scope = NativeExpectedFieldObservation.current();
        try {
            if (scope == null || !(object instanceof NativeExpectedFieldViews.Provider)) {
                return;
            }
            NativeExpectedFieldViews.Provider provider = (NativeExpectedFieldViews.Provider)object;
            Fact fact = NativeExpectedFieldObservation.fact(scope, "PROVIDER_FIELDS", object, null, provider.ftboss$rawDispatcher(), null);
            if (fact != null) {
                fact.originalReturn = new Object[]{provider.ftboss$rawCapsValid(), provider.ftboss$rawCapsLazy(), provider.ftboss$rawCapsInitialized(), provider.ftboss$rawCapsParentSupplier(), provider.ftboss$rawCapsLazyData()};
                fact.returned = true;
                fact.exit = ++scope.clock;
            }
            NativeExpectedFieldObservation.rejectExtra(scope, new IllegalStateException("Provider mutation is outside the selected field catalogue"));
        }
        catch (Throwable throwable) {
            NativeExpectedFieldObservation.failed(scope, throwable);
        }
    }

    static final class Scope {
        final ItemStack sourceA;
        final Object commit;
        final Object leaf;
        final Object writer;
        final Object finalPublication;
        final ServerPlayer player;
        final Object operation = new Object();
        final Scope previous;
        final Thread thread = Thread.currentThread();
        final List<Fact> facts = new ArrayList<Fact>();
        Domain domain;
        FoodWeaponOriginalSequence.Session sequence;
        FoodWeaponOriginalSequence.Step step;
        FoodWeaponExpectedTrace.Kind kind;
        Fact active;
        long clock;
        Throwable observationFailure;
        Throwable originalFailure;
        boolean retired;
        boolean closed;
        CompoundTag sourceTag;
        CompoundTag timerReceiver;
        Tag timerValue;
        Tag copyValue;
        Tag copyResult;
        String timerKey;
        boolean copyAttempted;
        boolean copyReturned;
        boolean putAttempted;
        int prefixes;
        CompoundTag persistentRoot;
        CompoundTag acquisitionRoot;
        String acquisitionKey;
        CompoundTag rangedBuffer;
        Binding lastBinding;
        boolean unsupportedTopology;

        Scope(ItemStack itemStack, Object object, Object object2, Object object3, Object object4, ServerPlayer serverPlayer, Scope scope) {
            this.sourceA = itemStack;
            this.commit = object;
            this.leaf = object2;
            this.writer = object3;
            this.finalPublication = object4;
            this.player = serverPlayer;
            this.previous = scope;
        }
    }

    static final class Domain {
        final FoodWeaponOriginalSequence.Session sequence;
        final ItemStack sourceA;
        final Object finalPublication;
        final Object ownerWriter;
        final long generation;
        final Thread thread = Thread.currentThread();
        volatile boolean retired;
        volatile Throwable failure;

        Domain(FoodWeaponOriginalSequence.Session session, ItemStack itemStack) {
            this.sequence = session;
            this.sourceA = itemStack;
            this.finalPublication = session.lineage.publication;
            this.ownerWriter = session.lineage.issuer;
            this.generation = session.lineage.generation;
        }
    }

    static final class Fact {
        final Scope scope;
        final Domain lifecycleDomain;
        final ItemStack sourceA;
        final Object operation;
        final long generation;
        final Fact parent;
        final String site;
        final Object receiver;
        final Object key;
        final Object argument;
        final Operation<?> continuation;
        final Thread thread = Thread.currentThread();
        final long entry;
        long exit;
        Object originalReturn;
        Throwable originalFailure;
        FoodWeaponExpectedTrace.Value rawFields;
        Binding bindingBefore;
        Binding bindingAfter;
        boolean attempted;
        boolean returned;
        int directMapChildren;
        CompoundTag physicalOwner;
        FoodWeaponOriginalSequence.Step originalStep;
        FoodWeaponExpectedTrace.Domain expectedFieldDomain;
        String[] path;

        Fact(Scope scope, String string, Object object, Object object2, Object object3, Operation<?> operation) {
            this.scope = scope;
            this.lifecycleDomain = scope.domain;
            this.sourceA = scope.sourceA;
            this.operation = scope.operation;
            this.generation = scope.domain == null ? -1L : scope.domain.generation;
            this.originalStep = scope.step;
            this.parent = scope.active;
            this.site = string;
            this.receiver = object;
            this.key = object2;
            this.argument = object3;
            this.continuation = operation;
            this.entry = ++scope.clock;
        }
    }

    static final class Binding {
        final CompoundTag root;
        final String key;
        final CompoundTag acquired;
        final Tag bound;
        final Map<String, Tag> rootMap;
        final Map<String, Tag> acquiredMap;
        final FoodWeaponExpectedTrace.Value acquiredFields;
        final FoodWeaponExpectedTrace.Value boundFields;

        Binding(CompoundTag compoundTag, String string, CompoundTag compoundTag2) {
            this.root = compoundTag;
            this.key = string;
            this.acquired = compoundTag2;
            this.rootMap = NativeExpectedFieldObservation.fields(compoundTag);
            this.acquiredMap = NativeExpectedFieldObservation.fields(compoundTag2);
            this.bound = this.rootMap.get(string);
            this.acquiredFields = NativeExpectedRawFields.observe((Tag)compoundTag2);
            this.boundFields = NativeExpectedRawFields.observe(this.bound);
        }
    }
}

