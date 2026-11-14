/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.PlayerDataStorage
 */
package net.farmingtales.ftboss.item;

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedFieldsCodec;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.OwnerEvolutionPlanFields;
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import net.farmingtales.ftboss.item.OwnerFieldEntityAccess;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerNativePutQuoteFields;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerOriginalPlanFields;
import net.farmingtales.ftboss.item.OwnerOriginalPublicationFields;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerRevocationNodeAccess;
import net.farmingtales.ftboss.item.OwnerTimerQuoteFields;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.PlayerDataStorage;

public final class OwnerFieldExpectedAuthority {
    private static final ThreadLocal<Boolean> FOUNDATION_RAW_READ = new ThreadLocal();

    private OwnerFieldExpectedAuthority() {
    }

    static boolean ownerCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class && stackFrame.getDeclaringClass() != FoodWeaponOriginalSequence.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false));
    }

    private static boolean entityHook(String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == Entity.class && stackFrame.getMethodName().contains(string)).orElse(false));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DomainBefore find(Entity entity) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore domainBefore;
            if (entity instanceof OwnerFieldEntityAccess) {
                OwnerFieldEntityAccess ownerFieldEntityAccess = (OwnerFieldEntityAccess)entity;
                domainBefore = ownerFieldEntityAccess.ftboss$ownerFieldDomain();
            } else {
                domainBefore = null;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return domainBefore;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean install(Entity entity, DomainBefore domainBefore) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!(entity instanceof OwnerFieldEntityAccess)) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            OwnerFieldEntityAccess ownerFieldEntityAccess = (OwnerFieldEntityAccess)entity;
            DomainBefore domainBefore2 = ownerFieldEntityAccess.ftboss$ownerFieldDomain();
            if (domainBefore2 != null) {
                domainBefore2.retired = true;
            }
            ownerFieldEntityAccess.ftboss$ownerFieldInstallDomain(domainBefore);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return ownerFieldEntityAccess.ftboss$ownerFieldDomain() == domainBefore;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void constructed(Entity entity, Object object) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!OwnerFieldExpectedAuthority.entityHook("ftboss$ownerFieldConstructed") || entity == null || !(entity instanceof OwnerFieldEntityAccess) || OwnerFieldExpectedAuthority.find(entity) != null) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            OwnerFieldExpectedAuthority.install(entity, new DomainBefore(entity, FoodWeaponExpectedTrace.emptyCompound(), object == null));
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadQuoted(Entity entity, CompoundTag compoundTag) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!OwnerFieldExpectedAuthority.entityHook("ftboss$ownerFieldLoadQuoted") || entity == null || compoundTag == null || !(entity instanceof OwnerFieldEntityAccess)) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == EntityType.class && stackFrame.getMethodName().equals("loadEntityRecursive") || stackFrame.getDeclaringClass() == PlayerDataStorage.class && stackFrame.getMethodName().equals("load")));
            if (!bl) {
                OwnerFieldExpectedAuthority.changed(entity);
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            Tag tag = ((NativeExpectedFieldViews.Compound)compoundTag).ftboss$rawCompoundFields().get("ForgeData");
            if (!(tag instanceof CompoundTag)) {
                OwnerFieldExpectedAuthority.changed(entity);
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            DomainBefore domainBefore = new DomainBefore(entity, NativeExpectedRawFields.observe(tag), true);
            domainBefore.loading = true;
            OwnerFieldExpectedAuthority.install(entity, domainBefore);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAssigned(Entity entity, CompoundTag compoundTag) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            NativeExpectedFieldViews.Actor actor;
            if (!OwnerFieldExpectedAuthority.entityHook("ftboss$ownerFieldLoadAssigned")) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            DomainBefore domainBefore = OwnerFieldExpectedAuthority.find(entity);
            if (domainBefore == null || !domainBefore.loading || domainBefore.retired || !(entity instanceof NativeExpectedFieldViews.Actor) || (actor = (NativeExpectedFieldViews.Actor)entity).ftboss$rawPersistentData() != compoundTag || !FoodWeaponExpectedTrace.same(domainBefore.expected, NativeExpectedRawFields.observe((Tag)compoundTag))) {
                if (domainBefore != null) {
                    domainBefore.retired = true;
                }
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            domainBefore.originalPersistent = compoundTag;
            domainBefore.loading = false;
            OwnerFullRepairGuard.fieldDomain(compoundTag, domainBefore);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void persistentExposed(Entity entity, Object object) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            NativeExpectedFieldViews.Actor actor;
            DomainBefore domainBefore = OwnerFieldExpectedAuthority.find(entity);
            if (domainBefore == null) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            if (domainBefore.originalPersistent == null && object != null && domainBefore.expected == FoodWeaponExpectedTrace.emptyCompound() && entity instanceof NativeExpectedFieldViews.Actor && (actor = (NativeExpectedFieldViews.Actor)entity).ftboss$rawPersistentData() == object && FoodWeaponExpectedTrace.same(domainBefore.expected, NativeExpectedRawFields.observe((Tag)object))) {
                domainBefore.originalPersistent = object;
                OwnerFullRepairGuard.fieldDomain(object, domainBefore);
            }
            if (domainBefore.originalPersistent != object) {
                domainBefore.retired = true;
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] foundationCanonicalRead(Supplier<byte[]> supplier) {
        boolean bl;
        boolean bl2 = bl = supplier != null && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoundationCanonicalDigest.class && stackFrame.getMethodName().equals("canonical")).orElse(false)) != false;
        if (!bl) {
            throw new IllegalStateException("Foundation raw-read scope denied");
        }
        Boolean bl3 = FOUNDATION_RAW_READ.get();
        FOUNDATION_RAW_READ.set(Boolean.TRUE);
        try {
            byte[] byArray = supplier.get();
            return byArray;
        }
        finally {
            if (bl3 == null) {
                FOUNDATION_RAW_READ.remove();
            } else {
                FOUNDATION_RAW_READ.set(bl3);
            }
        }
    }

    public static void rawExposed(Object object, Object object2) {
        boolean bl;
        StackWalker.StackFrame stackFrame2;
        if (Boolean.TRUE.equals(FOUNDATION_RAW_READ.get())) {
            return;
        }
        List list = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).filter(stackFrame -> !stackFrame.getMethodName().contains("ftboss$ownerRawExposed")).limit(4L).toList());
        StackWalker.StackFrame stackFrame3 = list.stream().filter(stackFrame -> !stackFrame.getMethodName().startsWith("ftboss$raw")).findFirst().orElse(null);
        if (stackFrame3 != null && stackFrame3.getDeclaringClass() == OwnerNativeSemanticAuthority.class && (stackFrame2 = (StackWalker.StackFrame)list.stream().filter(stackFrame -> stackFrame.getMethodName().startsWith("ftboss$raw")).findFirst().orElse(null)) != null && OwnerNativeSemanticAuthority.rawReadReturned(object, object2, stackFrame2.getDeclaringClass(), stackFrame2.getMethodName(), stackFrame2.getDescriptor(), stackFrame3.getDeclaringClass(), stackFrame3.getMethodName(), stackFrame3.getDescriptor()) != 0) {
            return;
        }
        boolean bl2 = bl = stackFrame3 != null && (stackFrame3.getDeclaringClass() == OwnerFieldExpectedAuthority.class || stackFrame3.getDeclaringClass().getNestHost() == NativeExpectedRawFields.class || stackFrame3.getDeclaringClass().getNestHost() == FoodWeaponSourceLedger.class || stackFrame3.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class || stackFrame3.getDeclaringClass().getNestHost() == OwnerRepairGenerationAuthority.class || OwnerFieldExpectedAuthority.exactNativeObserver(stackFrame3.getDeclaringClass()));
        if (!bl) {
            OwnerFieldExpectedAuthority.changed(object);
            OwnerFullRepairGuard.changed(object, 40);
        }
    }

    private static boolean exactNativeObserver(Class<?> clazz) {
        try {
            return clazz.getNestHost() == Class.forName("net.farmingtales.ftboss.item.NativeExpectedFieldObservation", false, OwnerFieldExpectedAuthority.class.getClassLoader());
        }
        catch (ClassNotFoundException classNotFoundException) {
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void changed(Object object) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore domainBefore;
            if (object instanceof Entity) {
                Entity entity = (Entity)object;
                v0 = OwnerFieldExpectedAuthority.find(entity);
            } else {
                v0 = domainBefore = OwnerFullRepairGuard.fieldDomain(object);
            }
            if (domainBefore != null) {
                domainBefore.retired = true;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void alias(Object object, Object object2, boolean bl) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            DomainBefore domainBefore = OwnerFullRepairGuard.fieldDomain(object);
            if (domainBefore == null || object2 == null) {
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return;
            }
            if (object2 instanceof OwnerRevocationNodeAccess) {
                OwnerFullRepairGuard.fieldDomain(object2, domainBefore);
            } else if (bl && !OwnerFullRepairGuard.immutableValue(object2)) {
                domainBefore.retired = true;
            }
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean live(DomainBefore domainBefore) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            NativeExpectedFieldViews.Actor actor;
            OwnerFieldEntityAccess ownerFieldEntityAccess;
            if (domainBefore == null || !domainBefore.originated || domainBefore.retired || domainBefore.loading || domainBefore.thread != Thread.currentThread()) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return false;
            }
            Entity entity = (Entity)domainBefore.entity.get();
            boolean bl = entity instanceof OwnerFieldEntityAccess && (ownerFieldEntityAccess = (OwnerFieldEntityAccess)entity).ftboss$ownerFieldRawLevel() == domainBefore.world && ownerFieldEntityAccess.ftboss$ownerFieldDomain() == domainBefore && entity instanceof NativeExpectedFieldViews.Actor && (actor = (NativeExpectedFieldViews.Actor)entity).ftboss$rawPersistentData() == domainBefore.originalPersistent;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return bl;
        }
    }

    public static TimerQuote timerQuoted(Object object, Object object2, Object object3, Object object4, Object object5, CompoundTag compoundTag, CompoundTag compoundTag2) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getName().equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf") && stackFrame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class && stackFrame.getMethodName().contains("ftboss$ownerTimerQuoted")).orElse(false));
        if (!bl || object == null || object3 == null || compoundTag == null || compoundTag2 == null) {
            return null;
        }
        return new TimerQuote(object, object2, object3, object4, object5, compoundTag, compoundTag2, OwnerTimerSemanticAuthority.claim(compoundTag, compoundTag2));
    }

    static Prepared prepare(Object object, Entity entity, Entity entity2, FoodWeaponOriginalSequence.Lineage lineage, byte[] byArray, Object object2, Object object3, MinecraftServer minecraftServer, Object object4, Object object5) {
        DomainBefore domainBefore;
        if (!OwnerFieldExpectedAuthority.ownerCaller() || object == null || lineage == null || lineage.issuer != object || minecraftServer == null || object4 == null || object5 == null || !(entity instanceof OwnerFieldEntityAccess) || entity2 != null && !(entity2 instanceof OwnerFieldEntityAccess)) {
            return null;
        }
        DomainBefore domainBefore2 = OwnerFieldExpectedAuthority.find(entity);
        if (domainBefore2 == null) {
            domainBefore2 = new DomainBefore(entity, FoodWeaponExpectedTrace.emptyCompound(), false);
        }
        DomainBefore domainBefore3 = domainBefore = entity2 == null ? null : OwnerFieldExpectedAuthority.find(entity2);
        if (entity2 != null && domainBefore == null) {
            domainBefore = new DomainBefore(entity2, FoodWeaponExpectedTrace.emptyCompound(), false);
        }
        return new Prepared(object, domainBefore2, domainBefore, lineage, FoodWeaponExpectedFieldsCodec.decodeD084(byArray), object2, object3, minecraftServer, object4, object5);
    }

    static void quoteTimers(Prepared prepared, Object object2, Object object3, Object object4, Object object5) {
        if (!OwnerFieldExpectedAuthority.ownerCaller() || !OwnerFieldExpectedAuthority.issued(prepared) || prepared.sealed || !(object2 instanceof OwnerOriginalPublicationFields)) {
            return;
        }
        OwnerOriginalPublicationFields ownerOriginalPublicationFields = (OwnerOriginalPublicationFields)object2;
        if (prepared.timerSemantics == null) {
            prepared.timerSemantics = new OwnerTimerSemanticAuthority.Book(prepared, prepared.player.originalPersistent, OwnerNativeSemanticAuthority.semanticRangedBuffer(prepared.nativeBefore));
        }
        for (Object obj : ownerOriginalPublicationFields.ftboss$ownerOriginalProducers()) {
            OwnerOriginalPlanFields ownerOriginalPlanFields;
            FoodWeaponHitPlan foodWeaponHitPlan;
            block14: {
                block13: {
                    if (!(obj instanceof OwnerEvolutionPlanFields)) {
                        prepared.unsupported = true;
                        continue;
                    }
                    OwnerEvolutionPlanFields ownerEvolutionPlanFields = (OwnerEvolutionPlanFields)obj;
                    foodWeaponHitPlan = ownerEvolutionPlanFields.ftboss$ownerOriginalPlan();
                    if (!(foodWeaponHitPlan instanceof OwnerOriginalPlanFields)) break block13;
                    ownerOriginalPlanFields = (OwnerOriginalPlanFields)((Object)foodWeaponHitPlan);
                    if (foodWeaponHitPlan instanceof FoodWeaponHitPlan) break block14;
                }
                prepared.unsupported = true;
                continue;
            }
            FoodWeaponHitPlan foodWeaponHitPlan2 = foodWeaponHitPlan;
            IdentityHashMap<Object, Boolean> identityHashMap = new IdentityHashMap<Object, Boolean>();
            for (Object obj2 : ownerOriginalPlanFields.ftboss$ownerOriginalLeaves()) {
                if (obj2 == null || identityHashMap.containsKey(obj2)) {
                    prepared.unsupported = true;
                    continue;
                }
                identityHashMap.put(obj2, FoodWeaponSourceLedger.ownerNativeLeafAccepted(prepared, obj, foodWeaponHitPlan, obj2));
            }
            try {
                prepared.timerSemantics.quote(foodWeaponHitPlan2, identityHashMap, object3, object4, object5, object -> {
                    TimerQuote timerQuote;
                    if (object instanceof OwnerTimerQuoteFields) {
                        OwnerTimerQuoteFields ownerTimerQuoteFields = (OwnerTimerQuoteFields)object;
                        timerQuote = ownerTimerQuoteFields.ftboss$ownerTimerQuote();
                    } else {
                        timerQuote = null;
                    }
                    return timerQuote;
                });
            }
            catch (Throwable throwable) {
                prepared.unsupported = true;
            }
            for (Object obj2 : ownerOriginalPlanFields.ftboss$ownerOriginalLeaves()) {
                if (!Boolean.TRUE.equals(identityHashMap.get(obj2)) || prepared.timerSemantics.local(obj2) || !(obj2 instanceof OwnerNativePutQuoteFields)) continue;
                OwnerNativePutQuoteFields ownerNativePutQuoteFields = (OwnerNativePutQuoteFields)obj2;
                OwnerNativeSemanticAuthority.PutBinding putBinding = OwnerNativeSemanticAuthority.appendPut(prepared.nativeBefore, prepared.builder, obj2, ownerNativePutQuoteFields.ftboss$ownerNativePutQuote());
                if (putBinding == null) {
                    prepared.unsupported = true;
                    continue;
                }
                prepared.nativePuts.put(obj2, putBinding);
            }
        }
    }

    static void unsupported(Prepared prepared) {
        if (OwnerFieldExpectedAuthority.issued(prepared)) {
            prepared.unsupported = true;
        }
    }

    static boolean seal(Prepared prepared) {
        if (!OwnerFieldExpectedAuthority.ownerCaller() || !OwnerFieldExpectedAuthority.issued(prepared) || prepared.sealed) {
            return false;
        }
        FoodWeaponExpectedTrace.Plan plan = prepared.builder.seal();
        prepared.session = FoodWeaponOriginalSequence.prepare(prepared.lineage, plan);
        if (prepared.timerSemantics != null) {
            prepared.timerSemantics.bind(prepared.session);
            prepared.session.timerSemantics = prepared.timerSemantics;
        }
        prepared.sealed = true;
        prepared.catalogueGeneration = !prepared.unsupported && OwnerFieldExpectedAuthority.live(prepared.player) && (prepared.carrier == null || OwnerFieldExpectedAuthority.live(prepared.carrier)) ? OwnerFieldCoverageRegistry.generation(plan) : null;
        prepared.issuing = prepared.catalogueGeneration != null;
        try {
            if (prepared.issuing) {
                prepared.coverage = FoodWeaponOriginalSequence.issueOwnerCoverage(prepared, prepared.catalogueGeneration);
            }
        }
        finally {
            prepared.issuing = false;
        }
        return true;
    }

    static FoodWeaponOriginalSequence.Session session(Prepared prepared) {
        return OwnerFieldExpectedAuthority.issued(prepared) ? prepared.session : null;
    }

    static FoodWeaponOriginalSequence.CoverageReceipt coverage(Prepared prepared) {
        return OwnerFieldExpectedAuthority.issued(prepared) ? prepared.coverage : null;
    }

    static FoodWeaponExpectedTrace.Plan expected(Prepared prepared) {
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.session != null ? prepared.session.expected : null;
    }

    static boolean authorizesIssue(Prepared prepared, Object object) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponOriginalSequence.class && stackFrame.getMethodName().equals("issueOwnerCoverage")).orElse(false));
        return bl && OwnerFieldExpectedAuthority.issued(prepared) && prepared.sealed && prepared.issuing && prepared.coverage == null && FoodWeaponSourceLedger.ownerFieldPreparedEpochs(prepared.issuer, prepared.server, prepared.serverEpoch, prepared.ownerEpoch) && prepared.catalogueGeneration == object && OwnerFieldCoverageRegistry.current(object, prepared.session.expected);
    }

    static boolean authentic(Prepared prepared, FoodWeaponOriginalSequence.CoverageReceipt coverageReceipt, Object object, FoodWeaponOriginalSequence.Session session) {
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.sealed && prepared.session == session && prepared.coverage == coverageReceipt && coverageReceipt != null && !prepared.unsupported && FoodWeaponSourceLedger.ownerFieldPreparedEpochs(prepared.issuer, prepared.server, prepared.serverEpoch, prepared.ownerEpoch) && prepared.catalogueGeneration == object && OwnerFieldCoverageRegistry.current(object, session.expected) && OwnerFieldExpectedAuthority.live(prepared.player) && (prepared.carrier == null || OwnerFieldExpectedAuthority.live(prepared.carrier));
    }

    private static boolean issued(Prepared prepared) {
        return prepared != null && prepared.thread == Thread.currentThread() && prepared.player.thread == Thread.currentThread() && !prepared.retired && FoodWeaponSourceLedger.ownerFieldPrepared(prepared.issuer) == prepared;
    }

    static Prepared matching(Object object, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.CoverageReceipt coverageReceipt) {
        Prepared prepared = FoodWeaponSourceLedger.ownerFieldPrepared(object);
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.session == session && prepared.coverage == coverageReceipt && coverageReceipt != null && prepared.sealed && !prepared.bound && OwnerFieldExpectedAuthority.live(prepared.player) && (prepared.carrier == null || OwnerFieldExpectedAuthority.live(prepared.carrier)) ? prepared : null;
    }

    static boolean pending(Prepared prepared) {
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.sealed && !prepared.unsupported && prepared.coverage == null && OwnerFieldExpectedAuthority.live(prepared.player) && (prepared.carrier == null || OwnerFieldExpectedAuthority.live(prepared.carrier)) && OwnerFieldCoverageRegistry.pending(prepared.session.expected);
    }

    static FoodWeaponOriginalSequence.CoverageReceipt refreshCoverage(Prepared prepared) {
        if (!OwnerFieldExpectedAuthority.ownerCaller() || !OwnerFieldExpectedAuthority.issued(prepared) || !prepared.sealed || prepared.unsupported || prepared.coverage != null || !OwnerFieldExpectedAuthority.live(prepared.player) || prepared.carrier != null && !OwnerFieldExpectedAuthority.live(prepared.carrier)) {
            return prepared == null ? null : prepared.coverage;
        }
        prepared.catalogueGeneration = OwnerFieldCoverageRegistry.generation(prepared.session.expected);
        if (prepared.catalogueGeneration == null) {
            return null;
        }
        prepared.issuing = true;
        try {
            prepared.coverage = FoodWeaponOriginalSequence.issueOwnerCoverage(prepared, prepared.catalogueGeneration);
        }
        finally {
            prepared.issuing = false;
        }
        return prepared.coverage;
    }

    static void bound(Prepared prepared) {
        if (OwnerFieldExpectedAuthority.issued(prepared)) {
            prepared.bound = true;
        }
    }

    static boolean nativeValidationLive(Prepared prepared, OwnerNativeSemanticAuthority.Before before, FoodWeaponOriginalSequence.Session session) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class).orElse(false));
        if (!bl || prepared == null || prepared.thread != Thread.currentThread() || prepared.retired || prepared.unsupported || !FoodWeaponSourceLedger.ownerFieldPreparedEpochs(prepared.issuer, prepared.server, prepared.serverEpoch, prepared.ownerEpoch) || !OwnerFieldExpectedAuthority.live(prepared.player) || prepared.playerGeneration != prepared.player.generation || prepared.carrier != null && (!OwnerFieldExpectedAuthority.live(prepared.carrier) || prepared.carrierGeneration != prepared.carrier.generation)) {
            return false;
        }
        if (before == null) {
            return prepared.nativeBefore == null && !prepared.sealed && !prepared.bound && prepared.session == null && session == null && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == Prepared.class && stackFrame.getMethodName().equals("<init>"))) != false;
        }
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.nativeBefore == before && (session == null ? !prepared.sealed && !prepared.bound : prepared.bound && prepared.session == session && prepared.lineage == session.lineage);
    }

    static void nativeValidationFailed(Prepared prepared, OwnerNativeSemanticAuthority.Before before) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldExpectedAuthority.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class).orElse(false));
        if (bl && prepared != null && prepared.nativeBefore == before) {
            prepared.retired = true;
        }
    }

    static boolean timerWriterExpected(Prepared prepared, FoodWeaponOriginalSequence.Session session, Object object, FoodWeaponHitPlan.TimerSemanticStep timerSemanticStep) {
        return OwnerFieldExpectedAuthority.ownerCaller() && OwnerFieldExpectedAuthority.issued(prepared) && prepared.sealed && prepared.session == session && session != null && session.lineage == prepared.lineage && prepared.timerSemantics != null && prepared.timerSemantics.writerStep(session, object, timerSemanticStep);
    }

    static boolean unchanged(Prepared prepared) {
        return OwnerFieldExpectedAuthority.issued(prepared) && prepared.bound && OwnerFieldExpectedAuthority.live(prepared.player) && prepared.playerGeneration == prepared.player.generation && (prepared.carrier == null || OwnerFieldExpectedAuthority.live(prepared.carrier) && prepared.carrierGeneration == prepared.carrier.generation);
    }

    static boolean covered(Prepared prepared) {
        return OwnerFieldExpectedAuthority.unchanged(prepared) && prepared.coverage != null && prepared.session != null && prepared.session.extraAuthorityRetained();
    }

    static void retire(Prepared prepared) {
        if (prepared != null) {
            prepared.retired = true;
        }
    }

    public static final class DomainBefore {
        private final WeakReference<Entity> entity;
        private final Level world;
        private final Thread thread = Thread.currentThread();
        private final Object generation = new Object();
        private final FoodWeaponExpectedTrace.Value expected;
        private final boolean originated;
        private volatile Object originalPersistent;
        private volatile boolean loading;
        private volatile boolean retired;

        private DomainBefore(Entity entity, FoodWeaponExpectedTrace.Value value, boolean bl) {
            this.entity = new WeakReference<Entity>(entity);
            this.world = ((OwnerFieldEntityAccess)entity).ftboss$ownerFieldRawLevel();
            this.expected = value;
            this.originated = bl;
        }
    }

    public static final class TimerQuote {
        final Object leaf;
        final Object weapon;
        final Object player;
        final Object source;
        final String profile;
        final String[] keys;
        final String[] rangedKeys;
        final FoodWeaponExpectedTrace.Value[] values;
        final FoodWeaponExpectedTrace.Value data;
        final FoodWeaponExpectedTrace.Value baseline;
        final OwnerTimerSemanticAuthority.Origin origin;

        private TimerQuote(Object object, Object object2, Object object3, Object object4, Object object5, CompoundTag compoundTag, CompoundTag compoundTag2, OwnerTimerSemanticAuthority.Origin origin) {
            FoodWeaponExpectedTrace.Value value;
            this.origin = origin;
            this.leaf = object;
            this.weapon = object2;
            this.profile = FoodWeaponEvolution.planHitTimerProfile((FoodWeaponEvolution.Profile)((Object)object3));
            this.player = object4;
            this.source = object5;
            this.data = NativeExpectedRawFields.observe((Tag)compoundTag2);
            this.baseline = value = NativeExpectedRawFields.observe((Tag)compoundTag);
            Map<String, Tag> map = ((NativeExpectedFieldViews.Compound)compoundTag2).ftboss$rawCompoundFields();
            this.keys = (String[])map.keySet().stream().filter(string -> !FoodWeaponExpectedTrace.same(FoodWeaponExpectedTrace.field(value, string), FoodWeaponExpectedTrace.field(this.data, string))).toArray(String[]::new);
            this.rangedKeys = (String[])map.keySet().stream().filter(string -> string.startsWith("Last") && FoodWeaponExpectedTrace.field((FoodWeaponExpectedTrace.Value)this.data, (String)string).kind == 4).toArray(String[]::new);
            this.values = new FoodWeaponExpectedTrace.Value[this.keys.length];
            for (int i = 0; i < this.keys.length; ++i) {
                this.values[i] = FoodWeaponExpectedTrace.field(this.data, this.keys[i]);
            }
        }
    }

    static final class Prepared {
        final Object issuer;
        final DomainBefore player;
        final DomainBefore carrier;
        final Object playerGeneration;
        final Object carrierGeneration;
        final FoodWeaponOriginalSequence.Lineage lineage;
        final OwnerNativeSemanticAuthority.Before nativeBefore;
        volatile OwnerTimerSemanticAuthority.Book timerSemantics;
        final IdentityHashMap<Object, OwnerNativeSemanticAuthority.PutBinding> nativePuts = new IdentityHashMap();
        private final FoodWeaponExpectedTrace.Builder builder;
        private final MinecraftServer server;
        private final Object serverEpoch;
        private final Object ownerEpoch;
        private volatile Object catalogueGeneration;
        private volatile boolean issuing;
        private volatile FoodWeaponOriginalSequence.Session session;
        private volatile FoodWeaponOriginalSequence.CoverageReceipt coverage;
        private volatile boolean unsupported;
        private volatile boolean sealed;
        private volatile boolean bound;
        private volatile boolean retired;
        private final Thread thread = Thread.currentThread();

        private Prepared(Object object, DomainBefore domainBefore, DomainBefore domainBefore2, FoodWeaponOriginalSequence.Lineage lineage, FoodWeaponExpectedTrace.Value value, Object object2, Object object3, MinecraftServer minecraftServer, Object object4, Object object5) {
            this.issuer = object;
            this.server = minecraftServer;
            this.serverEpoch = object4;
            this.ownerEpoch = object5;
            this.player = domainBefore;
            this.carrier = domainBefore2;
            this.playerGeneration = domainBefore.generation;
            this.carrierGeneration = domainBefore2 == null ? null : domainBefore2.generation;
            this.lineage = lineage;
            this.nativeBefore = OwnerFieldExpectedAuthority.live(domainBefore) && (domainBefore2 == null || OwnerFieldExpectedAuthority.live(domainBefore2)) ? OwnerNativeSemanticAuthority.before(this, object2, (Entity)domainBefore.entity.get(), domainBefore2 == null ? null : (Entity)domainBefore2.entity.get(), domainBefore.originalPersistent, domainBefore2 == null ? null : domainBefore2.originalPersistent, object3, value, domainBefore.expected, domainBefore2 == null ? FoodWeaponExpectedTrace.emptyCompound() : domainBefore2.expected) : null;
            FoodWeaponExpectedTrace.Value value2 = OwnerNativeSemanticAuthority.expectedWeapon(this.nativeBefore);
            this.builder = new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(value2 == null ? value : value2, domainBefore.expected, domainBefore2 == null ? FoodWeaponExpectedTrace.emptyCompound() : domainBefore2.expected));
            this.unsupported = !OwnerFieldExpectedAuthority.live(domainBefore) || domainBefore2 != null && !OwnerFieldExpectedAuthority.live(domainBefore2);
        }
    }
}

