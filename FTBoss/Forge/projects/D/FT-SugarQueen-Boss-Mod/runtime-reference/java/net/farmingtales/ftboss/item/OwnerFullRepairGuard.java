/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.EndTag
 *  net.minecraft.nbt.FloatTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.ShortTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.capabilities.CapabilityDispatcher
 */
package net.farmingtales.ftboss.item;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.farmingtales.ftboss.item.OwnerRepairContinuation;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerRepairStackFields;
import net.farmingtales.ftboss.item.OwnerRevocationNodeAccess;
import net.farmingtales.ftboss.item.OwnerStatueTaskOrigin;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

public final class OwnerFullRepairGuard {
    private static final int ALIAS_LIMIT = 65536;

    private static boolean tickCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerFullRepairGuard.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class && stackFrame.getMethodName().contains("ftboss$ownerInventoryTickCallback")).orElse(false));
    }

    /*
     * Exception decompiling
     */
    public static TickCut inventoryTickEntered(Object var0, Object var1_1) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 17[MONITOR]
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
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static void inventoryTickReturned(TickCut var0, Throwable var1_1) {
        block26: {
            block25: {
                var2_2 = new Mutation();
                var3_3 = null;
                var4_4 = OwnerFullRepairGuard.class;
                // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
                if (!OwnerFullRepairGuard.tickCaller() || var0 == null || var0.closed || var0.thread != Thread.currentThread()) {
                    // MONITOREXIT : var4_4
                    return;
                }
                ** try [egrp 3[TRYBLOCK] [16, 17 : 63->127)] { 
lbl-1000:
                // 1 sources

                {
                    var0.closed = true;
                    var0.originalFailure = var1_1;
                    var5_8 = OwnerFullRepairGuard.node(var0.actual, false);
                    if (var5_8 != null && var5_8.actual == var0.record && var5_8.generation == var0.generation && var5_8.lease == var0.lease) break block25;
                    // MONITOREXIT : var4_4
                }
                try {
                    var2_2.flush();
                    return;
                }
                catch (Error | RuntimeException var6_9) {
                    if (var3_3 != null) return;
                    throw var6_9;
                }
            }
            if (!var0.record.denied && var0.record.events == var0.events) break block26;
            // MONITOREXIT : var4_4
            try {
                var2_2.flush();
                return;
            }
            catch (Error | RuntimeException var6_10) {
                if (var3_3 != null) return;
                throw var6_10;
            }
        }
        try {
            if (!var0.before.same(new StorageView(var0.actual, true))) {
                OwnerFullRepairGuard.deny(var2_2, var0.record, 42);
            }
        }
        catch (Throwable var6_11) {
            var0.observerFailure = var6_11;
            OwnerFullRepairGuard.deny(var2_2, var0.record, 42);
        }
        try {
            var2_2.flush();
            return;
        }
        catch (Error | RuntimeException var4_5) {
            if (var3_3 != null) return;
            throw var4_5;
        }
lbl47:
        // 8 sources

        catch (Error | RuntimeException var4_6) {
            var3_3 = var4_6;
            throw var4_6;
        }
        finally {
            block27: {
                try {
                    var2_2.flush();
                }
                catch (Error | RuntimeException var9_13) {
                    if (var3_3 != null) break block27;
                    throw var9_13;
                }
            }
        }
    }

    private OwnerFullRepairGuard() {
    }

    static boolean detachedLocalNode(Object object) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == OwnerTimerSemanticAuthority.class).orElse(false));
        if (!bl || !(object instanceof OwnerRevocationNodeAccess)) {
            return false;
        }
        Node node = OwnerFullRepairGuard.node(object, false);
        if (node == null) {
            return true;
        }
        if (node.actual != null || node.fieldDomain != null || node.retired) {
            return false;
        }
        for (WeakReference<Record> weakReference : node.owners) {
            if (weakReference == null || weakReference.get() == null) continue;
            return false;
        }
        return true;
    }

    private static Node node(Object object, boolean bl) {
        if (!(object instanceof OwnerRevocationNodeAccess)) {
            return null;
        }
        OwnerRevocationNodeAccess ownerRevocationNodeAccess = (OwnerRevocationNodeAccess)object;
        Node node = ownerRevocationNodeAccess.ftboss$ownerRevocationNode();
        if (node == null && bl) {
            Node node2 = new Node();
            ownerRevocationNodeAccess.ftboss$ownerInstallRevocationNode(node2);
            node = ownerRevocationNodeAccess.ftboss$ownerRevocationNode();
            if (node != node2) {
                return null;
            }
        }
        return node;
    }

    private static Record record(Object object, boolean bl) {
        Node node = OwnerFullRepairGuard.node(object, bl);
        if (node == null) {
            return null;
        }
        if (node.actual == null && bl) {
            node.actual = new Record(object);
        }
        return node.actual;
    }

    /*
     * Exception decompiling
     */
    static boolean watchTaskOrigin(ItemStack var0, OwnerStatueTaskOrigin.Session var1_1) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 5[MONITOR]
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
     * Exception decompiling
     */
    static boolean watchWearPrefix(ItemStack var0) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 5[MONITOR]
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
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static boolean watchLoadedWear(Object object) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("wearInputLoaded")).orElse(false));
        Mutation mutation = new Mutation();
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                OwnerRepairStackFields ownerRepairStackFields;
                if (!bl || !(object instanceof OwnerRepairStackFields) || (ownerRepairStackFields = (OwnerRepairStackFields)object).ftboss$ownerRepairWasRetired() || !OwnerRepairGenerationAuthority.statueTaskObjectPolicy(object)) {
                    boolean bl2 = false;
                    // ** MonitorExit[var3_3] (shouldn't be in output)
                    mutation.flush();
                    return bl2;
                }
            }
        }
        catch (Throwable throwable) {
            mutation.flush();
            throw throwable;
        }
        {
            Node node = OwnerFullRepairGuard.node(object, true);
            if (node == null || node.retired || node.untrackedEscape || node.actual != null || node.generation != null || node.lease != null || node.intent != null) {
                boolean bl3 = false;
                // ** MonitorExit[var3_3] (shouldn't be in output)
                mutation.flush();
                return bl3;
            }
            Record record = OwnerFullRepairGuard.record(object, true);
            if (record == null) {
                boolean bl4 = false;
                // ** MonitorExit[var3_3] (shouldn't be in output)
                mutation.flush();
                return bl4;
            }
            try {
                StorageView storageView = new StorageView(object, false);
                for (Object object2 : storageView.targets) {
                    OwnerFullRepairGuard.associate(mutation, record, object2, false);
                }
                boolean bl5 = !record.denied && node.actual == record;
                // ** MonitorExit[var3_3] (shouldn't be in output)
                mutation.flush();
                return bl5;
            }
            catch (Throwable throwable) {
                OwnerFullRepairGuard.deny(mutation, record, 43);
                boolean bl6 = false;
                // ** MonitorExit[var3_3] (shouldn't be in output)
                mutation.flush();
                return bl6;
            }
        }
    }

    static synchronized Object generation(Object object) {
        Node node = OwnerFullRepairGuard.node(object, false);
        return node == null ? null : node.generation;
    }

    static synchronized void generation(Object object, Object object2) {
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node != null) {
            node.generation = object2;
        }
    }

    static synchronized OwnerRepairGenerationAuthority.Intent intent(Object object) {
        Node node = OwnerFullRepairGuard.node(object, false);
        return node == null ? null : node.intent;
    }

    static synchronized void intent(Object object, OwnerRepairGenerationAuthority.Intent intent) {
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node != null) {
            node.intent = intent;
        }
    }

    static synchronized OwnerLineageRevocationStore.Lease lease(Object object) {
        Node node = OwnerFullRepairGuard.node(object, false);
        return node == null ? null : node.lease;
    }

    static synchronized void lease(Object object, OwnerLineageRevocationStore.Lease lease) {
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node != null && (node.lease == null || node.lease == lease)) {
            node.lease = lease;
        }
    }

    static synchronized OwnerFieldExpectedAuthority.DomainBefore fieldDomain(Object object) {
        Node node = OwnerFullRepairGuard.node(object, false);
        return node == null ? null : node.fieldDomain;
    }

    static synchronized void fieldDomain(Object object, OwnerFieldExpectedAuthority.DomainBefore domainBefore) {
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node == null) {
            return;
        }
        if (node.fieldDomain != null && node.fieldDomain != domainBefore) {
            OwnerFieldExpectedAuthority.changed(object);
        }
        node.fieldDomain = domainBefore;
    }

    static synchronized void freshGeneration(Object object, OwnerLineageRevocationStore.Lease lease, Object object2) {
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node == null || lease == null || object2 == null) {
            return;
        }
        try {
            StorageView storageView = new StorageView(object, true);
            Record record = new Record(object);
            ArrayList<AliasHandoff> arrayList = new ArrayList<AliasHandoff>();
            for (Object iterator : storageView.targets) {
                Node node2 = OwnerFullRepairGuard.node(iterator, true);
                if (node2 == null || record.aliases.size() >= 65536) {
                    return;
                }
                int n = -1;
                for (int i = 0; i < node2.owners.length; ++i) {
                    Record record2;
                    Record record3 = record2 = node2.owners[i] == null ? null : (Record)node2.owners[i].get();
                    if (record2 != null && record2.actual.get() != object && OwnerFullRepairGuard.record(record2.actual.get(), false) == record2) continue;
                    n = i;
                    break;
                }
                if (n < 0) {
                    return;
                }
                record.aliases.add(new WeakReference<Object>(iterator));
                arrayList.add(new AliasHandoff(node2, n));
            }
            WeakReference<Record> weakReference = new WeakReference<Record>(record);
            for (AliasHandoff aliasHandoff : arrayList) {
                aliasHandoff.node.owners[aliasHandoff.slot] = weakReference;
            }
            if (node.actual != null) {
                node.actual.denied = true;
            }
            node.actual = record;
            node.lease = lease;
            node.generation = object2;
        }
        finally {
            return;
        }
    }

    private static void deny(Mutation mutation, Record record, int n) {
        if (record == null) {
            return;
        }
        if (n != 20 && n != 21) {
            try {
                OwnerStatueWearAdmission.mutationNoticed(record.actual.get(), null, n);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        record.denied = true;
        if (OwnerFullRepairGuard.record(record.actual.get(), false) == record) {
            OwnerNativeSemanticAuthority.changed(record.actual.get());
        }
        if (record.events != Long.MAX_VALUE) {
            ++record.events;
        }
        record.lastKind = n;
        if (n >= 8 && OwnerFullRepairGuard.record(record.actual.get(), false) == record) {
            mutation.add(OwnerRepairGenerationAuthority.invalidate(record.actual.get()));
        }
    }

    static boolean prewriteLocalDenialFree(Object object, Object object2, OwnerLineageRevocationStore.Lease lease) {
        if (!Thread.holdsLock(OwnerFullRepairGuard.class)) {
            return false;
        }
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerRepairContinuation.class && stackFrame.getMethodName().equals("prewriteLocal")).orElse(false));
        if (!bl) {
            return false;
        }
        Node node = OwnerFullRepairGuard.node(object, false);
        return node != null && object2 != null && lease != null && node.generation == object2 && node.lease == lease && node.actual != null && node.actual.actual.get() == object && !node.actual.denied;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean permits(Object object) {
        if (!(object instanceof OwnerRepairStackFields)) {
            return false;
        }
        OwnerRepairStackFields ownerRepairStackFields = (OwnerRepairStackFields)object;
        if (OwnerRepairGenerationAuthority.fresh(object)) {
            return true;
        }
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            Record record = OwnerFullRepairGuard.record(object, false);
            Node node = OwnerFullRepairGuard.node(object, false);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return !(ownerRepairStackFields.ftboss$ownerRepairWasRetired() || node != null && node.retired || record != null && record.denied);
        }
    }

    public static boolean permits(UUID uUID) {
        return OwnerRepairGenerationAuthority.currentSnapshotWeapon(uUID);
    }

    public static boolean permits(UUID uUID, CompoundTag compoundTag, ServerPlayer serverPlayer) {
        return OwnerRepairGenerationAuthority.currentSnapshotWeapon(uUID, compoundTag, serverPlayer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static boolean bind(Object object, UUID uUID) {
        Object object2;
        Record record;
        Throwable throwable;
        Mutation mutation;
        block34: {
            OwnerLineageRevocationStore.Lease lease;
            Class<OwnerFullRepairGuard> clazz;
            block33: {
                block32: {
                    block31: {
                        mutation = new Mutation();
                        throwable = null;
                        if (object != null && uUID != null && OwnerFullRepairGuard.permits(object)) break block31;
                        boolean bl = false;
                        try {
                            mutation.flush();
                            return bl;
                        }
                        catch (Error | RuntimeException throwable2) {
                            if (throwable != null) return bl;
                            throw throwable2;
                        }
                    }
                    clazz = OwnerFullRepairGuard.class;
                    // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
                    lease = OwnerFullRepairGuard.lease(object);
                    record = OwnerFullRepairGuard.record(object, true);
                    if (record != null && lease != null && lease.weapon.equals(uUID)) break block32;
                    boolean bl = false;
                    // MONITOREXIT : clazz
                    try {
                        mutation.flush();
                        return bl;
                    }
                    catch (Error | RuntimeException throwable3) {
                        if (throwable != null) return bl;
                        throw throwable3;
                    }
                }
                // MONITOREXIT : clazz
                if (OwnerLineageRevocationStore.liveLease(lease)) break block33;
                boolean bl = false;
                try {
                    mutation.flush();
                    return bl;
                }
                catch (Error | RuntimeException throwable4) {
                    if (throwable != null) return bl;
                    throw throwable4;
                }
            }
            clazz = OwnerFullRepairGuard.class;
            // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
            Node node = OwnerFullRepairGuard.node(object, false);
            if (OwnerFullRepairGuard.record(object, false) == record && OwnerFullRepairGuard.lease(object) == lease && !record.denied && (OwnerRepairGenerationAuthority.repaired(object, lease) || object instanceof OwnerRepairStackFields && !(object2 = (OwnerRepairStackFields)object).ftboss$ownerRepairWasRetired() && node != null && !node.retired)) break block34;
            boolean bl = false;
            // MONITOREXIT : clazz
            try {
                mutation.flush();
                return bl;
            }
            catch (Error | RuntimeException throwable5) {
                if (throwable != null) return bl;
                throw throwable5;
            }
        }
        object2 = new StorageView(object, false);
        for (Object object3 : ((StorageView)object2).targets) {
            OwnerFullRepairGuard.associate(mutation, record, object3, false);
        }
        boolean bl = OwnerFullRepairGuard.record(object, false) == record && !record.denied;
        // MONITOREXIT : clazz
        try {
            mutation.flush();
            return bl;
        }
        catch (Error | RuntimeException throwable6) {
            if (throwable != null) return bl;
            throw throwable6;
        }
        catch (Throwable throwable7) {
            OwnerFullRepairGuard.deny(mutation, record, 43);
            boolean bl2 = false;
            // MONITOREXIT : clazz
            try {
                mutation.flush();
                return bl2;
            }
            catch (Error | RuntimeException throwable8) {
                if (throwable != null) return bl2;
                throw throwable8;
            }
            {
                catch (Error | RuntimeException throwable9) {
                    throwable = throwable9;
                    throw throwable9;
                }
            }
        }
        catch (Throwable throwable10) {
            try {
                mutation.flush();
                throw throwable10;
            }
            catch (Error | RuntimeException throwable11) {
                if (throwable != null) throw throwable10;
                throw throwable11;
            }
        }
    }

    /*
     * Exception decompiling
     */
    public static Cut enter(Object var0, UUID var1_1, int var2_2) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 15[MONITOR]
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

    public static synchronized void snapshotCopied(Object object, Object object2) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == CompoundTag.class && stackFrame.getMethodName().contains("ftboss$ownerSnapshotCopy")).orElse(false));
        if (!(bl && object instanceof CompoundTag && object2 instanceof CompoundTag && object != object2)) {
            return;
        }
        OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(object);
        if (lease != null) {
            OwnerFullRepairGuard.lease(object2, lease);
        }
    }

    private static boolean ledgerCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFullRepairGuard.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false));
    }

    static synchronized void planned(Cut cut, OwnerRepairGenerationAuthority.Intent intent) {
        if (!OwnerFullRepairGuard.ledgerCaller() || cut == null || cut.closed || cut.thread != Thread.currentThread() || cut.repairIntent != null || intent == null) {
            return;
        }
        if (OwnerFullRepairGuard.intent(cut.actual) == intent) {
            cut.repairIntent = intent;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void fieldsObserved(Cut cut, Object object, int n, byte[] byArray) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!OwnerFullRepairGuard.ledgerCaller() || cut == null || cut.closed || cut.thread != Thread.currentThread() || cut.actual != object || cut.repairIntent == null || n == 0 && (!cut.readReturned || cut.setterEntered) || n == 1 && (!cut.setterEntered || cut.setterReturned) || n < 0 || n > 1) {
                // ** MonitorExit[var5_4] (shouldn't be in output)
                return;
            }
            OwnerRepairGenerationAuthority.Intent intent = cut.repairIntent;
            // ** MonitorExit[var5_4] (shouldn't be in output)
            OwnerRepairGenerationAuthority.observed(intent, object, n, byArray);
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void fieldsValueObserved(Cut cut, Object object, int n, FoodWeaponExpectedTrace.Value value) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            if (!OwnerFullRepairGuard.ledgerCaller() || cut == null || cut.closed || cut.thread != Thread.currentThread() || cut.actual != object || cut.repairIntent == null || n == 0 && (!cut.readReturned || cut.setterEntered) || n == 1 && (!cut.setterEntered || cut.setterReturned) || n < 0 || n > 1) {
                // ** MonitorExit[var5_4] (shouldn't be in output)
                return;
            }
            OwnerRepairGenerationAuthority.Intent intent = cut.repairIntent;
            // ** MonitorExit[var5_4] (shouldn't be in output)
            OwnerRepairGenerationAuthority.observedValue(intent, object, n, value);
            return;
        }
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
    public static void readReturned(Cut var0, Object var1_1, int var2_2, int var3_3) {
        block19: {
            var4_4 = new Mutation();
            var5_5 = null;
            var6_6 = OwnerFullRepairGuard.class;
            // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
            if (var0 == null || var0.closed || var0.thread != Thread.currentThread()) {
                // MONITOREXIT : var6_6
                return;
            }
            ** try [egrp 3[TRYBLOCK] [12, 13 : 61->108)] { 
lbl-1000:
            // 1 sources

            {
                OwnerFullRepairGuard.deny(var4_4, var0.record, 2);
                if (var0.actual == var1_1 && !var0.readReturned && var3_3 == Math.max(0, var2_2 - var0.reduction)) break block19;
                var0.closed = true;
                // MONITOREXIT : var6_6
            }
            try {
                var4_4.flush();
                return;
            }
            catch (Error | RuntimeException var7_10) {
                if (var5_5 != null) return;
                throw var7_10;
            }
        }
        var0.damage = var2_2;
        var0.requested = var3_3;
        var0.readReturned = true;
        // MONITOREXIT : var6_6
        try {
            var4_4.flush();
            return;
        }
        catch (Error | RuntimeException var6_7) {
            if (var5_5 != null) return;
            throw var6_7;
        }
lbl32:
        // 6 sources

        catch (Error | RuntimeException var6_8) {
            var5_5 = var6_8;
            throw var6_8;
        }
        finally {
            block20: {
                try {
                    var4_4.flush();
                }
                catch (Error | RuntimeException var10_12) {
                    if (var5_5 != null) break block20;
                    throw var10_12;
                }
            }
        }
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
    public static void setterEntered(Cut var0, Object var1_1, int var2_2) {
        block19: {
            var3_3 = new Mutation();
            var4_4 = null;
            var5_5 = OwnerFullRepairGuard.class;
            // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
            if (var0 == null || var0.closed || var0.thread != Thread.currentThread()) {
                // MONITOREXIT : var5_5
                return;
            }
            ** try [egrp 3[TRYBLOCK] [12, 13 : 59->106)] { 
lbl-1000:
            // 1 sources

            {
                OwnerFullRepairGuard.deny(var3_3, var0.record, 3);
                if (var0.actual == var1_1 && var0.readReturned && !var0.setterEntered && var0.requested == var2_2) break block19;
                var0.closed = true;
                // MONITOREXIT : var5_5
            }
            try {
                var3_3.flush();
                return;
            }
            catch (Error | RuntimeException var6_9) {
                if (var4_4 != null) return;
                throw var6_9;
            }
        }
        var0.setterEntered = true;
        // MONITOREXIT : var5_5
        try {
            var3_3.flush();
            return;
        }
        catch (Error | RuntimeException var5_6) {
            if (var4_4 != null) return;
            throw var5_6;
        }
lbl30:
        // 6 sources

        catch (Error | RuntimeException var5_7) {
            var4_4 = var5_7;
            throw var5_7;
        }
        finally {
            block20: {
                try {
                    var3_3.flush();
                }
                catch (Error | RuntimeException var9_11) {
                    if (var4_4 != null) break block20;
                    throw var9_11;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void returned(Cut cut) {
        Mutation mutation = new Mutation();
        Throwable throwable = null;
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                block26: {
                    if (cut != null && !cut.closed && cut.thread == Thread.currentThread()) break block26;
                    // ** MonitorExit[var5_3] (shouldn't be in output)
                    return;
                }
                OwnerFullRepairGuard.deny(mutation, cut.record, 4);
                boolean bl = cut.setterReturned = cut.setterEntered;
                OwnerRepairGenerationAuthority.Intent intent = cut.repairIntent;
                // ** MonitorExit[var5_3] (shouldn't be in output)
                try {
                    if (intent != null) {
                        OwnerRepairGenerationAuthority.completed(intent, cut.actual, bl);
                    }
                }
                finally {
                    clazz = OwnerFullRepairGuard.class;
                    synchronized (OwnerFullRepairGuard.class) {
                        cut.closed = true;
                        // ** MonitorExit[var5_3] (shouldn't be in output)
                    }
                }
            }
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            block28: {
                try {
                    mutation.flush();
                }
                catch (Error | RuntimeException throwable3) {
                    if (throwable != null) break block28;
                    throw throwable3;
                }
            }
        }
        {
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void failed(Cut cut, Throwable throwable, boolean bl) {
        Mutation mutation = new Mutation();
        Throwable throwable2 = null;
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                block21: {
                    if (cut != null && cut.thread == Thread.currentThread()) break block21;
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    return;
                }
                OwnerFullRepairGuard.deny(mutation, cut.record, bl ? 6 : 5);
                if (cut.repairIntent != null) {
                    OwnerRepairGenerationAuthority.failed(cut.repairIntent, throwable);
                }
                if (bl) {
                    if (cut.observerFailure == null) {
                        cut.observerFailure = throwable;
                    }
                } else if (cut.originalFailure == null) {
                    cut.originalFailure = throwable;
                }
                cut.closed = true;
                // ** MonitorExit[var5_5] (shouldn't be in output)
            }
        }
        catch (Error | RuntimeException throwable3) {
            throwable2 = throwable3;
            throw throwable3;
        }
        finally {
            block23: {
                try {
                    mutation.flush();
                }
                catch (Error | RuntimeException throwable4) {
                    if (throwable2 != null) break block23;
                    throw throwable4;
                }
            }
        }
        {
            return;
        }
    }

    public static synchronized Throwable failure(Cut cut, boolean bl) {
        return cut == null || cut.thread != Thread.currentThread() ? null : (bl ? cut.observerFailure : cut.originalFailure);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void close(Cut cut) {
        Mutation mutation = new Mutation();
        Throwable throwable = null;
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                block17: {
                    if (cut != null && cut.thread == Thread.currentThread()) break block17;
                    // ** MonitorExit[var3_3] (shouldn't be in output)
                    return;
                }
                if (!cut.closed && cut.repairIntent != null) {
                    OwnerRepairGenerationAuthority.failed(cut.repairIntent, null);
                }
                OwnerFullRepairGuard.deny(mutation, cut.record, 7);
                cut.closed = true;
                // ** MonitorExit[var3_3] (shouldn't be in output)
            }
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            block19: {
                try {
                    mutation.flush();
                }
                catch (Error | RuntimeException throwable3) {
                    if (throwable != null) break block19;
                    throw throwable3;
                }
            }
        }
        {
            return;
        }
    }

    static boolean immutableValue(Object object) {
        if (object == null) {
            return true;
        }
        Class<?> clazz = object.getClass();
        return clazz == ByteTag.class || clazz == ShortTag.class || clazz == IntTag.class || clazz == LongTag.class || clazz == FloatTag.class || clazz == DoubleTag.class || clazz == StringTag.class || clazz == EndTag.class;
    }

    private static void associate(Mutation mutation, Record record, Object object, boolean bl) {
        Object object2;
        if (OwnerFullRepairGuard.immutableValue(object)) {
            return;
        }
        if (record == null || object == null || OwnerFullRepairGuard.record(record.actual.get(), false) != record || record.contains(object)) {
            return;
        }
        Node node = OwnerFullRepairGuard.node(object, true);
        if (node == null || record.aliases.size() >= 65536) {
            OwnerFullRepairGuard.deny(mutation, record, 8);
            return;
        }
        int n = -1;
        for (int i = 0; i < node.owners.length; ++i) {
            Object object3 = object2 = node.owners[i] == null ? null : (Record)node.owners[i].get();
            if (object2 == record) {
                return;
            }
            if (object2 != null && OwnerFullRepairGuard.record(((Record)object2).actual.get(), false) == object2 || n >= 0) continue;
            n = i;
        }
        if (n < 0) {
            OwnerFullRepairGuard.deny(mutation, record, 8);
            return;
        }
        WeakReference<Record> weakReference = new WeakReference<Record>(record);
        object2 = new WeakReference<Object>(object);
        record.aliases.add((WeakReference<?>)object2);
        node.owners[n] = weakReference;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void alias(Cut cut, Object object, Object object2, boolean bl) {
        Mutation mutation = new Mutation();
        Throwable throwable = null;
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                block18: {
                    if (cut != null && cut.thread == Thread.currentThread() && cut.record.contains(object)) break block18;
                    // ** MonitorExit[var6_6] (shouldn't be in output)
                    return;
                }
                try {
                    OwnerFullRepairGuard.associate(mutation, cut.record, object2, bl);
                }
                catch (Throwable throwable2) {
                    OwnerFullRepairGuard.deny(mutation, cut.record, 11);
                }
            }
        }
        catch (Error | RuntimeException throwable3) {
            throwable = throwable3;
            throw throwable3;
        }
        finally {
            block20: {
                try {
                    mutation.flush();
                }
                catch (Error | RuntimeException throwable4) {
                    if (throwable != null) break block20;
                    throw throwable4;
                }
            }
        }
        {
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void changed(Cut cut, Object object, int n) {
        Mutation mutation = new Mutation();
        Throwable throwable = null;
        try {
            Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
            synchronized (OwnerFullRepairGuard.class) {
                block19: {
                    if (cut != null && cut.thread == Thread.currentThread() && cut.record.contains(object)) break block19;
                    // ** MonitorExit[var5_5] (shouldn't be in output)
                    return;
                }
                if (OwnerFullRepairGuard.record(cut.actual, false) == cut.record) {
                    mutation.add(OwnerRepairGenerationAuthority.invalidate(cut.actual));
                }
                try {
                    OwnerStatueWearAdmission.mutationNoticed(cut.actual, object, n);
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                OwnerFullRepairGuard.deny(mutation, cut.record, n);
                // ** MonitorExit[var5_5] (shouldn't be in output)
            }
        }
        catch (Error | RuntimeException throwable3) {
            throwable = throwable3;
            throw throwable3;
        }
        finally {
            block21: {
                try {
                    mutation.flush();
                }
                catch (Error | RuntimeException throwable4) {
                    if (throwable != null) break block21;
                    throw throwable4;
                }
            }
        }
        {
            return;
        }
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
    public static void alias(Object var0, Object var1_1, boolean var2_2) {
        block31: {
            block30: {
                block29: {
                    var3_3 = new Mutation();
                    var4_4 = null;
                    var5_5 = OwnerFullRepairGuard.class;
                    // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
                    OwnerFieldExpectedAuthority.alias(var0, var1_1, var2_2);
                    if (OwnerStatueTaskOrigin.boundedProgressAlias(var0, var1_1)) {
                        // MONITOREXIT : var5_5
                        return;
                    }
                    ** try [egrp 3[TRYBLOCK] [18, 19 : 52->63)] { 
lbl-1000:
                    // 1 sources

                    {
                        if (!OwnerRepairContinuation.identityAlias(var0, var1_1)) break block29;
                        // MONITOREXIT : var5_5
                    }
                    try {
                        var3_3.flush();
                        return;
                    }
                    catch (Error | RuntimeException var6_9) {
                        if (var4_4 != null) return;
                        throw var6_9;
                    }
                }
                if (var1_1 != null && !OwnerFullRepairGuard.immutableValue(var1_1)) break block30;
                // MONITOREXIT : var5_5
                try {
                    var3_3.flush();
                    return;
                }
                catch (Error | RuntimeException var6_10) {
                    if (var4_4 != null) return;
                    throw var6_10;
                }
            }
            var6_11 = OwnerFullRepairGuard.node(var0, true);
            if (var6_11 != null) break block31;
            // MONITOREXIT : var5_5
            try {
                var3_3.flush();
                return;
            }
            catch (Error | RuntimeException var7_12) {
                if (var4_4 != null) return;
                throw var7_12;
            }
        }
        if (var2_2 && !(var1_1 instanceof OwnerRevocationNodeAccess)) {
            var6_11.untrackedEscape = true;
        }
        if (var1_1 instanceof OwnerRevocationNodeAccess) {
            OwnerFullRepairGuard.node(var1_1, true);
        }
        if (var6_11.actual != null) {
            OwnerFullRepairGuard.associate(var3_3, var6_11.actual, var1_1, var2_2);
        }
        for (WeakReference<Record> var10_16 : var6_11.owners) {
            v0 = var11_17 = var10_16 == null ? null : (Record)var10_16.get();
            if (var11_17 == null) continue;
            OwnerFullRepairGuard.associate(var3_3, var11_17, var1_1, var2_2);
        }
        // MONITOREXIT : var5_5
        try {
            var3_3.flush();
            return;
        }
        catch (Error | RuntimeException var5_6) {
            if (var4_4 != null) return;
            throw var5_6;
        }
lbl61:
        // 10 sources

        catch (Error | RuntimeException var5_7) {
            var4_4 = var5_7;
            throw var5_7;
        }
        finally {
            block32: {
                try {
                    var3_3.flush();
                }
                catch (Error | RuntimeException var14_19) {
                    if (var4_4 != null) break block32;
                    throw var14_19;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tagReadOrCreateEntered(Object object, String string) {
        Class<OwnerFullRepairGuard> clazz = OwnerFullRepairGuard.class;
        synchronized (OwnerFullRepairGuard.class) {
            boolean bl;
            if (!(object instanceof NativeExpectedFieldViews.Stack)) {
                bl = true;
            } else {
                NativeExpectedFieldViews.Compound compound;
                NativeExpectedFieldViews.Stack stack = (NativeExpectedFieldViews.Stack)object;
                CompoundTag compoundTag = stack.ftboss$rawStackTag();
                bl = compoundTag == null || string != null && (!(compoundTag instanceof NativeExpectedFieldViews.Compound) || !((compound = (NativeExpectedFieldViews.Compound)compoundTag).ftboss$rawCompoundFields().get(string) instanceof CompoundTag));
            }
            // ** MonitorExit[var3_2] (shouldn't be in output)
            if (bl) {
                OwnerFullRepairGuard.changed(object, 42);
            }
            return;
        }
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
    public static void changed(Object var0, int var1_1) {
        block30: {
            block29: {
                var2_2 = new Mutation();
                var3_3 = null;
                var4_4 = OwnerFullRepairGuard.class;
                // MONITORENTER : net.farmingtales.ftboss.item.OwnerFullRepairGuard.class
                if (var0 == null) {
                    // MONITOREXIT : var4_4
                    return;
                }
                ** try [egrp 3[TRYBLOCK] [17, 18 : 40->51)] { 
lbl-1000:
                // 1 sources

                {
                    if (!OwnerRepairContinuation.deferEntry(var0, var1_1)) break block29;
                    // MONITOREXIT : var4_4
                }
                try {
                    var2_2.flush();
                    return;
                }
                catch (Error | RuntimeException var5_8) {
                    if (var3_3 != null) return;
                    throw var5_8;
                }
            }
            OwnerFieldExpectedAuthority.changed(var0);
            var5_9 = OwnerFullRepairGuard.node(var0, var1_1 == 40);
            if (var5_9 != null) break block30;
            // MONITOREXIT : var4_4
            try {
                var2_2.flush();
                return;
            }
            catch (Error | RuntimeException var6_10) {
                if (var3_3 != null) return;
                throw var6_10;
            }
        }
        if (var1_1 == 40) {
            var5_9.untrackedEscape = true;
        }
        if (var5_9.actual != null) {
            try {
                OwnerStatueWearAdmission.mutationNoticed(var5_9.actual.actual.get(), var0, var1_1);
            }
            catch (Throwable var6_11) {
                // empty catch block
            }
            var2_2.add(OwnerRepairGenerationAuthority.invalidate(var5_9.actual.actual.get()));
            OwnerFullRepairGuard.deny(var2_2, var5_9.actual, var1_1);
        }
        for (WeakReference<Record> var9_15 : var5_9.owners) {
            v0 = var10_16 = var9_15 == null ? null : (Record)var9_15.get();
            if (var10_16 == null || OwnerFullRepairGuard.record(var10_16.actual.get(), false) != var10_16) continue;
            try {
                OwnerStatueWearAdmission.mutationNoticed(var10_16.actual.get(), var0, var1_1);
            }
            catch (Throwable var11_17) {
                // empty catch block
            }
            var2_2.add(OwnerRepairGenerationAuthority.invalidate(var10_16.actual.get()));
            OwnerFullRepairGuard.deny(var2_2, var10_16, var1_1);
        }
        // MONITOREXIT : var4_4
        try {
            var2_2.flush();
            return;
        }
        catch (Error | RuntimeException var4_5) {
            if (var3_3 != null) return;
            throw var4_5;
        }
lbl60:
        // 8 sources

        catch (Error | RuntimeException var4_6) {
            var3_3 = var4_6;
            throw var4_6;
        }
        finally {
            block31: {
                try {
                    var2_2.flush();
                }
                catch (Error | RuntimeException var14_19) {
                    if (var3_3 != null) break block31;
                    throw var14_19;
                }
            }
        }
    }

    private static final class Mutation {
        private final ArrayList<OwnerLineageRevocationStore.Lease> leases = new ArrayList(5);

        private Mutation() {
        }

        private void add(OwnerLineageRevocationStore.Lease lease) {
            if (lease == null) {
                return;
            }
            for (OwnerLineageRevocationStore.Lease lease2 : this.leases) {
                if (lease2 != lease) continue;
                return;
            }
            this.leases.add(lease);
        }

        private void flush() {
            Object object;
            Throwable throwable = null;
            for (OwnerLineageRevocationStore.Lease lease : this.leases) {
                try {
                    OwnerLineageRevocationStore.retireLease(lease);
                }
                catch (Error | RuntimeException throwable2) {
                    if (throwable != null) continue;
                    throwable = throwable2;
                }
            }
            if (throwable instanceof RuntimeException) {
                object = (RuntimeException)throwable;
                throw object;
            }
            if (throwable instanceof Error) {
                object = (Error)throwable;
                throw object;
            }
        }
    }

    public static final class Node {
        private final WeakReference<Record>[] owners = new WeakReference[4];
        private Record actual;
        private OwnerFieldExpectedAuthority.DomainBefore fieldDomain;
        private Object generation;
        private OwnerLineageRevocationStore.Lease lease;
        private OwnerRepairGenerationAuthority.Intent intent;
        private boolean retired;
        private boolean untrackedEscape;

        private Node() {
        }
    }

    private static final class Record {
        private final WeakReference<Object> actual;
        private final ArrayList<WeakReference<?>> aliases = new ArrayList();
        private boolean denied;
        private long events;
        private int lastKind;

        private Record(Object object) {
            this.actual = new WeakReference<Object>(object);
        }

        private boolean contains(Object object) {
            if (object == null) {
                return false;
            }
            if (this.actual.get() == object) {
                return true;
            }
            for (WeakReference<?> weakReference : this.aliases) {
                if (weakReference.get() != object) continue;
                return true;
            }
            return false;
        }
    }

    private static final class StorageView {
        private final Object item;
        private final int count;
        private final Object tag;
        private final Object caps;
        private final Object dispatcher;
        private final boolean valid;
        private final boolean initialized;
        private final boolean lazy;
        private final Object parent;
        private final Object lazyData;
        private final ArrayList<Object> targets;
        private final ArrayList<Object> physical;
        private final IdentityHashMap<Object, Boolean> visited;
        private final IdentityHashMap<Object, Boolean> active;
        private final int[] budget;
        private final FoodWeaponExpectedTrace.Value tagFields;

        private StorageView(Object object, boolean bl) {
            NativeExpectedFieldViews.Provider provider;
            NativeExpectedFieldViews.Stack stack;
            block10: {
                block9: {
                    this.targets = new ArrayList();
                    this.physical = new ArrayList();
                    this.visited = new IdentityHashMap();
                    this.active = new IdentityHashMap();
                    this.budget = new int[]{0};
                    Node node = OwnerFullRepairGuard.node(object, false);
                    if (node != null && node.untrackedEscape) {
                        throw new IllegalArgumentException("Previously untracked raw escape");
                    }
                    if (!(object instanceof NativeExpectedFieldViews.Stack)) break block9;
                    stack = (NativeExpectedFieldViews.Stack)object;
                    if (!(object instanceof NativeExpectedFieldViews.Provider)) break block9;
                    provider = (NativeExpectedFieldViews.Provider)object;
                    if (!(object instanceof OwnerRepairCapabilityFields)) break block9;
                    OwnerRepairCapabilityFields ownerRepairCapabilityFields = (OwnerRepairCapabilityFields)object;
                    if (!bl || ownerRepairCapabilityFields.ftboss$ownerRepairInitializedNoProviders()) break block10;
                }
                throw new IllegalArgumentException("Unsupported callback capability policy");
            }
            this.item = stack.ftboss$rawStackItem();
            this.count = stack.ftboss$rawStackCount();
            this.tag = stack.ftboss$rawStackTag();
            this.caps = stack.ftboss$rawStackCaps();
            this.dispatcher = provider.ftboss$rawDispatcher();
            this.valid = provider.ftboss$rawCapsValid();
            this.initialized = provider.ftboss$rawCapsInitialized();
            this.lazy = provider.ftboss$rawCapsLazy();
            this.parent = provider.ftboss$rawCapsParentSupplier();
            this.lazyData = provider.ftboss$rawCapsLazyData();
            if (!(!bl || this.caps == null && this.lazyData == null && this.initialized && this.valid)) {
                throw new IllegalArgumentException("Unquoted callback provider state");
            }
            if (this.dispatcher != null) {
                if (this.dispatcher.getClass() != CapabilityDispatcher.class || !(this.dispatcher instanceof OwnerRevocationNodeAccess)) {
                    throw new IllegalArgumentException("Unsupported dispatcher identity");
                }
                this.targets.add(this.dispatcher);
            }
            this.walk((Tag)this.tag, 0);
            if (this.caps != this.tag) {
                this.walk((Tag)this.caps, 0);
            }
            if (this.lazyData != this.tag && this.lazyData != this.caps) {
                this.walk((Tag)this.lazyData, 0);
            }
            this.tagFields = bl ? NativeExpectedRawFields.observe((Tag)this.tag) : null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void walk(Tag tag, int n) {
            block26: {
                if (tag == null) {
                    return;
                }
                if (n > 64 || (this.budget[0] = this.budget[0] + 1) > 65536) {
                    throw new IllegalArgumentException("Callback graph budget");
                }
                Class<?> clazz = tag.getClass();
                if (OwnerFullRepairGuard.immutableValue(tag)) {
                    String string;
                    if (clazz == StringTag.class && ((string = ((NativeExpectedFieldViews.TextValue)tag).ftboss$rawText()) == null || string.length() > 0x100000)) {
                        throw new IllegalArgumentException("Callback text budget");
                    }
                    return;
                }
                if (this.active.containsKey(tag)) {
                    throw new IllegalArgumentException("Callback graph cycle");
                }
                if (this.visited.put(tag, Boolean.TRUE) != null) {
                    return;
                }
                if (!(tag instanceof OwnerRevocationNodeAccess)) {
                    throw new IllegalArgumentException("Unsupported mutable callback storage");
                }
                this.active.put(tag, Boolean.TRUE);
                Node node = OwnerFullRepairGuard.node(tag, false);
                if (node != null && node.untrackedEscape) {
                    throw new IllegalArgumentException("Previously untracked storage escape");
                }
                this.targets.add(tag);
                try {
                    if (clazz == CompoundTag.class) {
                        Map<String, Tag> map = ((NativeExpectedFieldViews.Compound)tag).ftboss$rawCompoundFields();
                        if (map == null || map.getClass() != HashMap.class || map.size() > 65536 - this.budget[0]) {
                            throw new IllegalArgumentException("Unsupported callback map");
                        }
                        for (String object : map.keySet()) {
                            if (object != null && object.length() <= 0x100000) continue;
                            throw new IllegalArgumentException("Callback map key");
                        }
                        this.physical.add(map);
                        for (Map.Entry entry : new TreeMap<String, Tag>(map).entrySet()) {
                            if (entry.getValue() == null) {
                                throw new IllegalArgumentException("Callback null tag");
                            }
                            this.walk((Tag)entry.getValue(), n + 1);
                        }
                        break block26;
                    }
                    if (clazz == ListTag.class) {
                        List<Tag> list = ((NativeExpectedFieldViews.Sequence)tag).ftboss$rawListFields();
                        if (list == null || list.getClass() != ArrayList.class || list.size() > 65536 - this.budget[0]) {
                            throw new IllegalArgumentException("Unsupported callback list");
                        }
                        this.physical.add(list);
                        for (Tag tag2 : list) {
                            if (tag2 == null) {
                                throw new IllegalArgumentException("Callback null element");
                            }
                            this.walk(tag2, n + 1);
                        }
                        break block26;
                    }
                    if (clazz == ByteArrayTag.class) {
                        byte[] byArray = ((NativeExpectedFieldViews.Bytes)tag).ftboss$rawBytes();
                        if (byArray == null || byArray.length > 0x100000) {
                            throw new IllegalArgumentException("Callback byte array budget");
                        }
                        this.physical.add(byArray);
                        break block26;
                    }
                    if (clazz == IntArrayTag.class) {
                        int[] nArray = ((NativeExpectedFieldViews.Ints)tag).ftboss$rawInts();
                        if (nArray == null || nArray.length > 0x100000) {
                            throw new IllegalArgumentException("Callback int array budget");
                        }
                        this.physical.add(nArray);
                        break block26;
                    }
                    if (clazz == LongArrayTag.class) {
                        long[] lArray = ((NativeExpectedFieldViews.Longs)tag).ftboss$rawLongs();
                        if (lArray == null || lArray.length > 0x100000) {
                            throw new IllegalArgumentException("Callback long array budget");
                        }
                        this.physical.add(lArray);
                        break block26;
                    }
                    throw new IllegalArgumentException("Unknown callback mutable tag class");
                }
                finally {
                    this.active.remove(tag);
                }
            }
        }

        private boolean same(StorageView storageView) {
            int n;
            if (storageView == null || this.item != storageView.item || this.count != storageView.count || this.tag != storageView.tag || this.caps != storageView.caps || this.dispatcher != storageView.dispatcher || this.valid != storageView.valid || this.initialized != storageView.initialized || this.lazy != storageView.lazy || this.parent != storageView.parent || this.lazyData != storageView.lazyData || !FoodWeaponExpectedTrace.same(this.tagFields, storageView.tagFields) || this.targets.size() != storageView.targets.size() || this.physical.size() != storageView.physical.size()) {
                return false;
            }
            for (n = 0; n < this.targets.size(); ++n) {
                if (this.targets.get(n) == storageView.targets.get(n)) continue;
                return false;
            }
            for (n = 0; n < this.physical.size(); ++n) {
                if (this.physical.get(n) == storageView.physical.get(n)) continue;
                return false;
            }
            return true;
        }
    }

    public static final class TickCut {
        private final Object actual;
        private final Record record;
        private final Object generation;
        private final OwnerLineageRevocationStore.Lease lease;
        private final StorageView before;
        private final long events;
        private final Thread thread = Thread.currentThread();
        private Throwable originalFailure;
        private Throwable observerFailure;
        private boolean closed;

        private TickCut(Object object, Node node, StorageView storageView) {
            this.actual = object;
            this.record = node.actual;
            this.generation = node.generation;
            this.lease = node.lease;
            this.before = storageView;
            this.events = this.record.events;
        }
    }

    private record AliasHandoff(Node node, int slot) {
    }

    public static final class Cut {
        private final Record record;
        private final Object actual;
        private final Thread thread = Thread.currentThread();
        private final int reduction;
        private int damage;
        private int requested;
        private boolean readReturned;
        private boolean setterEntered;
        private boolean setterReturned;
        private Throwable originalFailure;
        private Throwable observerFailure;
        private boolean closed;
        private OwnerRepairGenerationAuthority.Intent repairIntent;

        private Cut(Record record, Object object, int n) {
            this.record = record;
            this.actual = object;
            this.reduction = n;
        }
    }
}

