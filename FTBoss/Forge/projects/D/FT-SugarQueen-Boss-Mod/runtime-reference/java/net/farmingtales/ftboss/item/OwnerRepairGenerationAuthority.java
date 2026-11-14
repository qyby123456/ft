/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.TridentItem
 */
package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.farmingtales.ftboss.item.FoodWeaponExpectedFieldsCodec;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.farmingtales.ftboss.item.OwnerRepairExpectedImage;
import net.farmingtales.ftboss.item.OwnerRepairStackFields;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

final class OwnerRepairGenerationAuthority {
    private OwnerRepairGenerationAuthority() {
    }

    static Object current(Object object) {
        return OwnerFullRepairGuard.generation(object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean publishTaskOrigin(OwnerStatueTaskOrigin.Publication publication, OwnerLineageRevocationStore.Lease lease) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerStatueTaskOrigin.class && stackFrame.getMethodName().equals("outerCommitted")).orElse(false));
        if (!(bl && publication != null && publication.candidate(lease) && OwnerLineageRevocationStore.liveLease(lease))) {
            return false;
        }
        ItemStack itemStack = publication.actualForPublisher();
        if (itemStack == null) {
            return false;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!OwnerRepairGenerationAuthority.knownStackPolicy(itemStack) || OwnerRepairGenerationAuthority.current(itemStack) != null || OwnerFullRepairGuard.lease(itemStack) != null || OwnerFullRepairGuard.intent(itemStack) != null || publication.actualForPublisher() != itemStack) {
                // ** MonitorExit[var4_4] (shouldn't be in output)
                return false;
            }
            Generation generation = new Generation(lease, false);
            OwnerFullRepairGuard.freshGeneration(itemStack, lease, generation);
            if (OwnerRepairGenerationAuthority.current(itemStack) != generation || OwnerFullRepairGuard.lease(itemStack) != lease || publication.actualForPublisher() != itemStack) {
                generation.retired = true;
                // ** MonitorExit[var4_4] (shouldn't be in output)
                return false;
            }
            generation.repaired = true;
            if (!publication.bindContinuation(generation)) {
                generation.retired = true;
                // ** MonitorExit[var4_4] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var4_4] (shouldn't be in output)
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean fresh(Object object) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Object object2 = OwnerRepairGenerationAuthority.current(object);
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(object);
            if (!OwnerRepairGenerationAuthority.repaired(object, lease)) {
                // ** MonitorExit[var3_1] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var3_1] (shouldn't be in output)
            if (!OwnerLineageRevocationStore.liveLease(lease)) {
                return false;
            }
            clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                // ** MonitorExit[var3_1] (shouldn't be in output)
                return OwnerRepairGenerationAuthority.current(object) == object2 && OwnerRepairGenerationAuthority.repaired(object, lease);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Intent preparePlan(Object object, Object object2, OwnerFieldExpectedAuthority.Prepared prepared, OwnerRepairExpectedImage.Plan plan) {
        FoodWeaponExpectedTrace.Plan plan2 = prepared == null ? null : OwnerFieldExpectedAuthority.expected(prepared);
        boolean bl = plan2 != null && plan2.size() != 0;
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Intent intent;
            Object object3;
            Class clazz2 = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                if (!(object != null && OwnerRepairGenerationAuthority.knownStackPolicy(object2) && prepared != null && prepared.issuer == object && plan != null && OwnerFieldExpectedAuthority.covered(prepared) && bl && OwnerFullRepairGuard.intent(object2) == null)) {
                    // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
                    // ** MonitorExit[var7_6] (shouldn't be in output)
                    return null;
                }
                object3 = OwnerFullRepairGuard.lease(object2);
                if (object3 == null) {
                    // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
                    // ** MonitorExit[var7_6] (shouldn't be in output)
                    return null;
                }
                intent = new Intent(object, object2, prepared, plan);
                OwnerFullRepairGuard.intent(object2, intent);
                if (OwnerFullRepairGuard.intent(object2) != intent) {
                    // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
                    // ** MonitorExit[var7_6] (shouldn't be in output)
                    return null;
                }
                // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
            }
            {
                try {
                    boolean bl2 = OwnerLineageRevocationStore.liveLease(intent.priorLease);
                    clazz2 = FoodWeaponSourceLedger.class;
                    synchronized (FoodWeaponSourceLedger.class) {
                        object3 = OwnerFullRepairGuard.class;
                        synchronized (OwnerFullRepairGuard.class) {
                            if (!(bl2 && plan.weapon.equals(intent.priorLease.weapon) && OwnerRepairGenerationAuthority.issued(intent) && OwnerRepairGenerationAuthority.current(object2) == intent.priorGeneration && OwnerFullRepairGuard.lease(object2) == intent.priorLease && OwnerRepairGenerationAuthority.knownStackPolicy(object2) && OwnerFieldExpectedAuthority.covered(prepared) && bl)) {
                                OwnerRepairGenerationAuthority.failed(intent, null);
                                // ** MonitorExit[var9_11] (shouldn't be in output)
                                // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
                                return null;
                            }
                            // ** MonitorExit[var9_11] (shouldn't be in output)
                            // ** MonitorExit[var8_9 /* !! */ ] (shouldn't be in output)
                            return intent;
                        }
                    }
                }
                catch (Error | RuntimeException throwable) {
                    try {
                        OwnerRepairGenerationAuthority.failed(intent, throwable);
                    }
                    catch (Error | RuntimeException throwable2) {
                        // empty catch block
                    }
                    throw throwable;
                }
            }
        }
    }

    static Intent prepare(Object object, Object object2, OwnerFieldExpectedAuthority.Prepared prepared, byte[] byArray, int n) {
        return OwnerRepairGenerationAuthority.preparePlan(object, object2, prepared, OwnerRepairExpectedImage.statue(byArray, n));
    }

    static Intent prepareReduction(Object object, Object object2, OwnerFieldExpectedAuthority.Prepared prepared, byte[] byArray, int n) {
        return OwnerRepairGenerationAuthority.preparePlan(object, object2, prepared, OwnerRepairExpectedImage.reduction(byArray, n));
    }

    static boolean statueTaskObjectPolicy(Object object) {
        return OwnerRepairGenerationAuthority.knownStackPolicy(object);
    }

    private static boolean knownStackPolicy(Object object) {
        OwnerRepairStackFields ownerRepairStackFields;
        block5: {
            block4: {
                NativeExpectedFieldViews.Provider provider;
                NativeExpectedFieldViews.Stack stack;
                if (object == null || object.getClass() != ItemStack.class || !(object instanceof OwnerRepairStackFields)) break block4;
                ownerRepairStackFields = (OwnerRepairStackFields)object;
                if (!(object instanceof OwnerRepairCapabilityFields)) break block4;
                OwnerRepairCapabilityFields ownerRepairCapabilityFields = (OwnerRepairCapabilityFields)object;
                if (object instanceof NativeExpectedFieldViews.Stack && (stack = (NativeExpectedFieldViews.Stack)object).ftboss$rawStackCount() == 1 && stack.ftboss$rawStackCaps() == null && object instanceof NativeExpectedFieldViews.Provider && (provider = (NativeExpectedFieldViews.Provider)object).ftboss$rawCapsParentSupplier() == null && ownerRepairStackFields.ftboss$ownerRepairRawCapNbtAbsent() && ownerRepairCapabilityFields.ftboss$ownerRepairInitializedNoProviders()) break block5;
            }
            return false;
        }
        Item item = ownerRepairStackFields.ftboss$ownerRepairRawItem();
        if (item == null) {
            return false;
        }
        Class<?> clazz = item.getClass();
        return clazz == SwordItem.class || clazz == BowItem.class || clazz == CrossbowItem.class || clazz == TridentItem.class;
    }

    private static boolean issued(Intent intent) {
        return intent != null && intent.thread == Thread.currentThread() && !intent.closed && !intent.rejected && OwnerFullRepairGuard.intent(intent.actual) == intent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static void entered(Intent intent, Object object) {
        try {
            Object object2 = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                Object object3;
                Class clazz = OwnerFullRepairGuard.class;
                synchronized (OwnerFullRepairGuard.class) {
                    if (!OwnerRepairGenerationAuthority.issued(intent)) {
                        // ** MonitorExit[var3_4 /* !! */ ] (shouldn't be in output)
                        // ** MonitorExit[var2_2] (shouldn't be in output)
                        return;
                    }
                    if (intent.actual != object || intent.entered || OwnerRepairGenerationAuthority.current(object) != intent.priorGeneration || OwnerFullRepairGuard.lease(object) != intent.priorLease || !OwnerFieldExpectedAuthority.covered(intent.ancestry)) {
                        OwnerRepairGenerationAuthority.failed(intent, null);
                        // ** MonitorExit[var3_4 /* !! */ ] (shouldn't be in output)
                        // ** MonitorExit[var2_2] (shouldn't be in output)
                        return;
                    }
                    intent.entered = true;
                    Object object4 = intent.priorGeneration;
                    if (object4 instanceof Generation) {
                        object3 = (Generation)object4;
                        ((Generation)object3).retired = true;
                    }
                    // ** MonitorExit[var3_4 /* !! */ ] (shouldn't be in output)
                }
                {
                    try {
                        object2 = OwnerLineageRevocationStore.beginLease(intent.priorLease, intent.transaction);
                    }
                    catch (IOException iOException) {
                        OwnerRepairGenerationAuthority.failed(intent, iOException);
                        return;
                    }
                    clazz = FoodWeaponSourceLedger.class;
                    synchronized (FoodWeaponSourceLedger.class) {
                        object3 = OwnerFullRepairGuard.class;
                        synchronized (OwnerFullRepairGuard.class) {
                            if (object2 == null || !OwnerRepairGenerationAuthority.issued(intent) || intent.actual != object || OwnerRepairGenerationAuthority.current(object) != intent.priorGeneration || OwnerFullRepairGuard.lease(object) != intent.priorLease || !OwnerFieldExpectedAuthority.covered(intent.ancestry)) {
                                OwnerRepairGenerationAuthority.failed(intent, null);
                                // ** MonitorExit[var4_8] (shouldn't be in output)
                                // ** MonitorExit[var3_4 /* !! */ ] (shouldn't be in output)
                                return;
                            }
                            intent.advance = object2;
                            // ** MonitorExit[var4_8] (shouldn't be in output)
                            return;
                        }
                    }
                }
            }
        }
        catch (Error | RuntimeException throwable) {
            try {
                OwnerRepairGenerationAuthority.failed(intent, throwable);
                throw throwable;
            }
            catch (Error | RuntimeException throwable2) {
                // empty catch block
            }
            throw throwable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void observed(Intent intent, Object object, int n, byte[] byArray) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Class<OwnerFullRepairGuard> clazz2 = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                if (!OwnerRepairGenerationAuthority.issued(intent)) {
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return;
                }
                if (!(intent.entered && intent.actual == object && n == intent.next && n < intent.plan.prefixes.length && byArray != null && MessageDigest.isEqual(intent.plan.prefixes[n], byArray) && FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(intent.issuer, intent.ancestry))) {
                    OwnerRepairGenerationAuthority.failed(intent, null);
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return;
                }
                ++intent.next;
                // ** MonitorExit[var5_5] (shouldn't be in output)
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void observedValue(Intent intent, Object object, int n, FoodWeaponExpectedTrace.Value value) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Class<OwnerFullRepairGuard> clazz2 = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                if (!OwnerRepairGenerationAuthority.issued(intent)) {
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return;
                }
                if (!(intent.entered && intent.actual == object && n == intent.next && n < intent.plan.prefixes.length && FoodWeaponExpectedTrace.same(FoodWeaponExpectedFieldsCodec.decodeD084(intent.plan.prefixes[n]), value) && FoodWeaponSourceLedger.ownerRepairAcceptedIntentLive(intent.issuer, intent.ancestry))) {
                    OwnerRepairGenerationAuthority.failed(intent, null);
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    // ** MonitorExit[var4_4] (shouldn't be in output)
                    return;
                }
                ++intent.next;
                // ** MonitorExit[var5_5] (shouldn't be in output)
            }
            return;
        }
    }

    /*
     * Exception decompiling
     */
    static boolean completed(Intent var0, Object var1_1, boolean var2_2) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK], 1[TRYBLOCK]], but top level block is 50[MONITOR]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void failed(Intent intent, Throwable throwable) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (intent == null || intent.thread != Thread.currentThread()) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return;
            }
            intent.rejected = true;
            intent.closed = true;
            if (intent.originalFailure == null) {
                intent.originalFailure = throwable;
            }
            if (OwnerFullRepairGuard.intent(intent.actual) == intent) {
                OwnerFullRepairGuard.intent(intent.actual, null);
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matches(Object object, Object object2) {
        Object object3 = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Object object4;
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(object);
            if (OwnerRepairGenerationAuthority.current(object) != object2) return false;
            if (object2 instanceof Generation) {
                object4 = (Generation)object2;
                if (((Generation)object4).retired) {
                    // ** MonitorExit[var3_2] (shouldn't be in output)
                    return false;
                }
            }
            if (lease == null && !(object2 instanceof Generation)) {
                // ** MonitorExit[var3_2] (shouldn't be in output)
                return true;
            }
            // ** MonitorExit[var3_2] (shouldn't be in output)
            if (object2 instanceof Generation) {
                object4 = (Generation)object2;
                v0 = ((Generation)object4).lease;
            } else {
                v0 = object3 = lease;
            }
            if (!OwnerLineageRevocationStore.liveLease((OwnerLineageRevocationStore.Lease)object3)) {
                return false;
            }
            object4 = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                if (!OwnerLineageRevocationStore.locallyLive((OwnerLineageRevocationStore.Lease)object3)) return false;
                if (OwnerRepairGenerationAuthority.current(object) != object2) return false;
                if (OwnerFullRepairGuard.lease(object) != lease) return false;
                if (!(object2 instanceof Generation)) return true;
                Generation generation = (Generation)object2;
                if (generation.retired) return false;
                return true;
            }
        }
    }

    static boolean currentSnapshotWeapon(UUID uUID) {
        return uUID != null && FoodWeaponSourceLedger.ownerRepairScopedWeaponLive(uUID);
    }

    static boolean currentSnapshotWeapon(UUID uUID, CompoundTag compoundTag, ServerPlayer serverPlayer) {
        return uUID != null && FoodWeaponSourceLedger.ownerRepairScopedWeaponLive(uUID, compoundTag, serverPlayer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    static boolean stamp(Object var0, CompoundTag var1_1, OwnerLineageRevocationStore var2_2) {
        var3_3 = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk((Function<Stream, Boolean>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$stamp$3(java.util.stream.Stream ), (Ljava/util/stream/Stream;)Ljava/lang/Boolean;)());
        if (var3_3 == false) return false;
        if (var2_2 == null) return false;
        if (var1_1 == null) return false;
        if (var1_1.m_128403_("Owner") == false) return false;
        if (var1_1.m_128403_("WeaponId") == false) return false;
        if (!OwnerFullRepairGuard.permits(var0)) {
            return false;
        }
        var4_4 = var1_1.m_128342_("Owner");
        var5_5 = var1_1.m_128342_("WeaponId");
        var8_6 = OwnerFullRepairGuard.class;
        // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
        var6_8 = OwnerFullRepairGuard.lease(var0);
        var7_9 = OwnerRepairGenerationAuthority.current(var0);
        // MONITOREXIT : var8_6
        var8_7 = var6_8 == null;
        var9_10 = false;
        var10_11 /* !! */  = null;
        if (var8_7) {
            var6_8 = var2_2.admit(var4_4, var5_5);
            var11_12 = OwnerFullRepairGuard.class;
            // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
            if (OwnerRepairGenerationAuthority.current(var0) != var7_9 || OwnerFullRepairGuard.lease(var0) != null) {
                var12_16 = false;
                // MONITOREXIT : var11_12
                return var12_16;
            }
            OwnerFullRepairGuard.lease(var0, var6_8);
            // MONITOREXIT : var11_12
        }
        if (!(var6_8 != null && var2_2.owns(var6_8) && var6_8.owner.equals(var4_4) && var6_8.weapon.equals(var5_5) && OwnerLineageRevocationStore.liveLease(var6_8))) {
            var11_13 = false;
            return var11_13;
        }
        var11_12 = OwnerFullRepairGuard.class;
        // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
        if (!(OwnerRepairGenerationAuthority.current(var0) == var7_9 && OwnerFullRepairGuard.lease(var0) == var6_8 && var1_1.m_128403_("Owner") && var1_1.m_128403_("WeaponId") && var4_4.equals(var1_1.m_128342_("Owner")) && var5_5.equals(var1_1.m_128342_("WeaponId")))) {
            var12_18 = false;
            // MONITOREXIT : var11_12
            return var12_18;
        }
        ** try [egrp 10[TRYBLOCK] [19, 29, 30 : 432->485)] { 
lbl-1000:
        // 1 sources

        {
            var1_1.m_128362_("OwnerRepairAuthorityEpoch", var6_8.incarnation);
            var1_1.m_128356_("OwnerRepairAuthorityGeneration", var6_8.generation);
            OwnerFullRepairGuard.lease(var1_1, var6_8);
            var12_19 = var9_10 = OwnerFullRepairGuard.lease(var1_1) == var6_8;
            // MONITOREXIT : var11_12
            if (var8_7 == false) return var12_19;
            if (var9_10 != false) return var12_19;
            if (var6_8 == null) return var12_19;
        }
        try {
            OwnerLineageRevocationStore.retireLease(var6_8);
            return var12_19;
        }
        catch (Error | RuntimeException var13_23) {
            if (var10_11 /* !! */  != null) return var12_19;
            throw var13_23;
        }
lbl54:
        // 2 sources

        catch (IOException var11_14) {
            var10_11 /* !! */  = var11_14;
            var12_20 = false;
            return var12_20;
        }
lbl58:
        // 4 sources

        catch (Error | RuntimeException var11_15) {
            var10_11 /* !! */  = var11_15;
            throw var11_15;
        }
        finally {
            block36: {
                if (var8_7 && !var9_10 && var6_8 != null) {
                    try {
                        OwnerLineageRevocationStore.retireLease(var6_8);
                    }
                    catch (Error | RuntimeException var17_26) {
                        if (var10_11 /* !! */  != null) break block36;
                        throw var17_26;
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean snapshotAllowed(CompoundTag compoundTag) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(compoundTag);
            if (!OwnerRepairGenerationAuthority.snapshotFields(compoundTag, lease)) {
                // ** MonitorExit[var2_1] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var2_1] (shouldn't be in output)
            if (!OwnerLineageRevocationStore.liveLease(lease)) {
                return false;
            }
            clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                // ** MonitorExit[var2_1] (shouldn't be in output)
                return OwnerFullRepairGuard.lease(compoundTag) == lease && OwnerRepairGenerationAuthority.snapshotFields(compoundTag, lease) && OwnerLineageRevocationStore.locallyLive(lease);
            }
        }
    }

    static void unknownMutation(Object object) {
        OwnerLineageRevocationStore.retireLease(OwnerRepairGenerationAuthority.invalidate(object));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean repaired(Object object, OwnerLineageRevocationStore.Lease lease) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (lease == null) return false;
            if (!OwnerLineageRevocationStore.locallyLive(lease)) return false;
            Object object2 = OwnerRepairGenerationAuthority.current(object);
            if (!(object2 instanceof Generation)) return false;
            Generation generation = (Generation)object2;
            if (!generation.repaired) return false;
            if (generation.retired) return false;
            if (generation.lease != lease) return false;
            if (OwnerFullRepairGuard.lease(object) != lease) return false;
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OwnerLineageRevocationStore.Lease invalidate(Object object) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Intent intent = OwnerFullRepairGuard.intent(object);
            if (intent != null && intent.committing) {
                intent.rejected = true;
            }
            Object object2 = OwnerRepairGenerationAuthority.current(object);
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(object);
            if (object2 instanceof Generation) {
                Generation generation = (Generation)object2;
                generation.retired = true;
            } else if (lease != null) {
                Generation generation = new Generation(lease, false);
                generation.retired = true;
                OwnerFullRepairGuard.generation(object, generation);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return lease;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean identityMatches(Object object, Object object2, OwnerLineageRevocationStore.Lease lease) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (OwnerRepairGenerationAuthority.current(object) != object2) return false;
            if (OwnerFullRepairGuard.lease(object) != lease) return false;
            if (object2 instanceof Generation) {
                Generation generation = (Generation)object2;
                if (generation.retired) return false;
                if (generation.lease != lease) return false;
            }
            if (lease == null) return true;
            if (!OwnerLineageRevocationStore.locallyLive(lease)) return false;
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean snapshotIdentity(CompoundTag compoundTag, OwnerLineageRevocationStore.Lease lease) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return lease != null && OwnerLineageRevocationStore.locallyLive(lease) && OwnerFullRepairGuard.lease(compoundTag) == lease && OwnerRepairGenerationAuthority.snapshotFields(compoundTag, lease);
        }
    }

    private static boolean snapshotFields(CompoundTag compoundTag, OwnerLineageRevocationStore.Lease lease) {
        return compoundTag != null && lease != null && compoundTag.m_128403_("Owner") && compoundTag.m_128403_("WeaponId") && compoundTag.m_128403_("OwnerRepairAuthorityEpoch") && compoundTag.m_128425_("OwnerRepairAuthorityGeneration", 4) && lease.owner.equals(compoundTag.m_128342_("Owner")) && lease.weapon.equals(compoundTag.m_128342_("WeaponId")) && lease.incarnation.equals(compoundTag.m_128342_("OwnerRepairAuthorityEpoch")) && lease.generation == compoundTag.m_128454_("OwnerRepairAuthorityGeneration");
    }

    private static /* synthetic */ Boolean lambda$stamp$3(Stream stream) {
        return stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false);
    }

    static final class Intent {
        private final Object issuer;
        private final Object actual;
        private final Thread thread = Thread.currentThread();
        private final OwnerFieldExpectedAuthority.Prepared ancestry;
        private final OwnerRepairExpectedImage.Plan plan;
        private final Object priorGeneration;
        private final OwnerLineageRevocationStore.Lease priorLease;
        private final UUID transaction = UUID.randomUUID();
        private OwnerLineageRevocationStore.Advance advance;
        private int next;
        private boolean entered;
        private boolean committing;
        private boolean closed;
        private boolean rejected;
        private Throwable originalFailure;

        private Intent(Object object, Object object2, OwnerFieldExpectedAuthority.Prepared prepared, OwnerRepairExpectedImage.Plan plan) {
            this.issuer = object;
            this.actual = object2;
            this.ancestry = prepared;
            this.plan = plan;
            this.priorGeneration = OwnerRepairGenerationAuthority.current(object2);
            this.priorLease = OwnerFullRepairGuard.lease(object2);
        }
    }

    private static final class Generation {
        private final OwnerLineageRevocationStore.Lease lease;
        private boolean repaired;
        private boolean retired;

        private Generation(OwnerLineageRevocationStore.Lease lease, boolean bl) {
            this.lease = lease;
            this.repaired = bl;
        }
    }
}

