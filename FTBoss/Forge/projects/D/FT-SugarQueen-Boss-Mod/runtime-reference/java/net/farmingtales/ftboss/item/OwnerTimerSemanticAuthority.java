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
 *  org.spongepowered.asm.mixin.transformer.meta.MixinMerged
 */
package net.farmingtales.ftboss.item;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
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
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public final class OwnerTimerSemanticAuthority {
    private static final ThreadLocal<Origin> COPYING = new ThreadLocal();
    private static final ThreadLocal<LeafObservation> LEAF = new ThreadLocal();

    private OwnerTimerSemanticAuthority() {
    }

    private static boolean caller(Class<?> clazz, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != OwnerTimerSemanticAuthority.class).findFirst().map(stackFrame -> {
            String string2;
            String string3;
            if (stackFrame.getDeclaringClass() != clazz) {
                return false;
            }
            switch (string) {
                case "ftboss$ownerLocalDataCopy": 
                case "ftboss$ownerLocalBaselineCopy": {
                    if (clazz != FoodWeaponEvolution.class) {
                        return false;
                    }
                    string3 = "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin";
                    string2 = "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;";
                    break;
                }
                case "ftboss$ownerMaterialGuard": {
                    if (clazz != FoodWeaponRanged.class) {
                        return false;
                    }
                    string3 = "net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin";
                    string2 = "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)Z";
                    break;
                }
                case "ftboss$ownerSemanticLeaf": {
                    if (clazz != FoodWeaponHitPlan.class) {
                        return false;
                    }
                    string3 = "net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin";
                    string2 = "(Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Ljava/lang/Object;Ljava/lang/Object;)V";
                    break;
                }
                default: {
                    return stackFrame.getMethodName().equals(string);
                }
            }
            String string4 = stackFrame.getMethodName();
            String string5 = "wrapOperation\\$(?:zz[a-p]|z[b-p][a-p]|[b-p][a-p]{2,6}|[b-h][a-p]{7})(?:[0-9a-f]{3}|[1-9a-f][0-9a-f]{3,7})\\$" + Pattern.quote(string);
            if (!stackFrame.getDescriptor().equals(string2) || !string4.matches(string5)) {
                return false;
            }
            try {
                Method method = clazz.getDeclaredMethod(string4, stackFrame.getMethodType().parameterArray());
                MixinMerged mixinMerged = method.getDeclaredAnnotation(MixinMerged.class);
                return mixinMerged != null && string3.equals(mixinMerged.mixin());
            }
            catch (LinkageError | ReflectiveOperationException | SecurityException throwable) {
                return false;
            }
        }).orElse(false));
    }

    private static void graph(Tag tag, IdentityHashMap<Object, Boolean> identityHashMap, int n) {
        boolean bl;
        if (tag == null || n > 64) {
            throw new IllegalArgumentException("Local copy depth");
        }
        boolean bl2 = bl = tag.getClass() == CompoundTag.class || tag.getClass() == ListTag.class || tag.getClass() == ByteArrayTag.class || tag.getClass() == IntArrayTag.class || tag.getClass() == LongArrayTag.class;
        if (!bl) {
            if (tag.getClass() != ByteTag.class && tag.getClass() != ShortTag.class && tag.getClass() != IntTag.class && tag.getClass() != LongTag.class && tag.getClass() != FloatTag.class && tag.getClass() != DoubleTag.class && tag.getClass() != StringTag.class && tag.getClass() != EndTag.class) {
                throw new IllegalArgumentException("Local tag implementation");
            }
            return;
        }
        if (!OwnerFullRepairGuard.detachedLocalNode(tag) || identityHashMap.put(tag, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Local copy is attached or aliased");
        }
        if (tag instanceof NativeExpectedFieldViews.Compound) {
            NativeExpectedFieldViews.Compound compound = (NativeExpectedFieldViews.Compound)tag;
            Map<String, Tag> map = compound.ftboss$rawCompoundFields();
            if (identityHashMap.put(map, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("Local map alias");
            }
            for (Tag tag2 : map.values()) {
                OwnerTimerSemanticAuthority.graph(tag2, identityHashMap, n + 1);
            }
        } else if (tag instanceof NativeExpectedFieldViews.Sequence) {
            NativeExpectedFieldViews.Sequence sequence = (NativeExpectedFieldViews.Sequence)tag;
            List<Tag> list = sequence.ftboss$rawListFields();
            if (identityHashMap.put(list, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("Local list alias");
            }
            for (Tag tag3 : list) {
                OwnerTimerSemanticAuthority.graph(tag3, identityHashMap, n + 1);
            }
        } else {
            byte[] byArray;
            if (tag instanceof NativeExpectedFieldViews.Bytes) {
                NativeExpectedFieldViews.Bytes bytes = (NativeExpectedFieldViews.Bytes)tag;
                v1 = bytes.ftboss$rawBytes();
            } else if (tag instanceof NativeExpectedFieldViews.Ints) {
                NativeExpectedFieldViews.Ints ints = (NativeExpectedFieldViews.Ints)tag;
                v1 = ints.ftboss$rawInts();
            } else if (tag instanceof NativeExpectedFieldViews.Longs) {
                NativeExpectedFieldViews.Longs longs = (NativeExpectedFieldViews.Longs)tag;
                v1 = longs.ftboss$rawLongs();
            } else {
                v1 = byArray = null;
            }
            if (byArray == null || identityHashMap.put(byArray, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("Local array alias");
            }
        }
    }

    private static boolean detachedCopy(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (compoundTag == null || compoundTag2 == null || compoundTag == compoundTag2 || compoundTag.getClass() != CompoundTag.class || compoundTag2.getClass() != CompoundTag.class) {
            return false;
        }
        IdentityHashMap<Object, Boolean> identityHashMap = new IdentityHashMap<Object, Boolean>();
        IdentityHashMap<Object, Boolean> identityHashMap2 = new IdentityHashMap<Object, Boolean>();
        OwnerTimerSemanticAuthority.graph((Tag)compoundTag, identityHashMap, 0);
        OwnerTimerSemanticAuthority.graph((Tag)compoundTag2, identityHashMap2, 0);
        for (Object object : identityHashMap2.keySet()) {
            if (!identityHashMap.containsKey(object)) continue;
            return false;
        }
        return FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe((Tag)compoundTag), NativeExpectedRawFields.observe((Tag)compoundTag2));
    }

    public static void localDataCopied(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (!OwnerTimerSemanticAuthority.caller(FoodWeaponEvolution.class, "ftboss$ownerLocalDataCopy")) {
            return;
        }
        COPYING.remove();
        if (OwnerTimerSemanticAuthority.detachedCopy(compoundTag, compoundTag2)) {
            COPYING.set(new Origin(compoundTag2));
        }
    }

    public static void localBaselineCopied(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (!OwnerTimerSemanticAuthority.caller(FoodWeaponEvolution.class, "ftboss$ownerLocalBaselineCopy")) {
            return;
        }
        Origin origin = COPYING.get();
        COPYING.remove();
        if (origin != null && origin.thread == Thread.currentThread() && origin.data == compoundTag && OwnerTimerSemanticAuthority.detachedCopy(compoundTag, compoundTag2)) {
            origin.baseline = compoundTag2;
            COPYING.set(origin);
        }
    }

    static Origin claim(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (!OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "timerQuoted")) {
            return null;
        }
        Origin origin = COPYING.get();
        COPYING.remove();
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponEvolution.class && stackFrame.getMethodName().equals("quoteFinalWeaponHit") && stackFrame.getDescriptor().equals("(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;")));
        if (!bl || origin == null || origin.claimed || origin.thread != Thread.currentThread() || origin.data != compoundTag2 || origin.baseline != compoundTag || !OwnerFullRepairGuard.detachedLocalNode(compoundTag2) || !OwnerFullRepairGuard.detachedLocalNode(compoundTag)) {
            return null;
        }
        origin.claimed = true;
        return origin;
    }

    private static boolean savedLeaf(LeafObservation leafObservation) {
        if (leafObservation == null || leafObservation.closed || leafObservation.thread != Thread.currentThread() || LEAF.get() != leafObservation) {
            return false;
        }
        Book book = leafObservation.session.timerSemantics;
        return book != null && book.session == leafObservation.session && book.prepared.timerSemantics == book && book.prepared.lineage == leafObservation.session.lineage && book.groups.get(leafObservation.leaf) == leafObservation.group && FoodWeaponHitPlan.originalSequence(leafObservation.commit) == leafObservation.session && leafObservation.session.original.atLeaf(leafObservation.leaf);
    }

    static LeafObservation enter(FoodWeaponOriginalSequence.Session session, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, Object object) {
        if (!OwnerTimerSemanticAuthority.caller(FoodWeaponSourceLedger.class, "ownerTimerLeafEntered") || session == null || session.timerSemantics == null || !session.original.atLeaf(object)) {
            return null;
        }
        Group group = session.timerSemantics.groups.get(object);
        if (group == null) {
            return null;
        }
        LeafObservation leafObservation = new LeafObservation(session, finalEffectsCommit, object, group, LEAF.get());
        LEAF.set(leafObservation);
        return leafObservation;
    }

    public static void rangedGuard(Object object, Object object2, CompoundTag compoundTag, boolean bl) {
        if (!OwnerTimerSemanticAuthority.caller(FoodWeaponRanged.class, "ftboss$ownerMaterialGuard")) {
            return;
        }
        LeafObservation leafObservation = LEAF.get();
        if (!OwnerTimerSemanticAuthority.savedLeaf(leafObservation)) {
            return;
        }
        Group group = leafObservation.group;
        FoodWeaponHitPlan.TimerSemanticProjection timerSemanticProjection = FoodWeaponHitPlan.timerSemanticAtRanged(leafObservation.commit, leafObservation.leaf, (FoodWeaponSourceLedger.OwnerWriterPublication)leafObservation.session.lineage.issuer);
        if (object != group.program.player() || object2 != group.program.source() || compoundTag == null || group.guardObserved || !group.accepts(timerSemanticProjection)) {
            leafObservation.session.observationFailed(null, 0, null);
            return;
        }
        group.guardObserved = true;
        group.guardEmpty = bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public static void leave(LeafObservation leafObservation, Throwable throwable) {
        block32: {
            Object object;
            block31: {
                block30: {
                    if (leafObservation == null || leafObservation.closed || leafObservation.thread != Thread.currentThread() || LEAF.get() != leafObservation) {
                        return;
                    }
                    if (OwnerTimerSemanticAuthority.caller(FoodWeaponHitPlan.class, "ftboss$ownerSemanticLeaf") && OwnerTimerSemanticAuthority.savedLeaf(leafObservation) && throwable == null) break block30;
                    leafObservation.session.observationFailed(null, 3, throwable);
                    if (LEAF.get() == leafObservation && leafObservation.thread == Thread.currentThread()) {
                        leafObservation.closed = true;
                        LeafObservation leafObservation2 = leafObservation.previous;
                        while (leafObservation2 != null && leafObservation2.closed) {
                            leafObservation2 = leafObservation2.previous;
                        }
                        if (leafObservation2 == null) {
                            LEAF.remove();
                        } else {
                            LEAF.set(leafObservation2);
                        }
                    }
                    return;
                }
                object = null;
                for (FoodWeaponHitPlan.TimerSemanticResult object2 : FoodWeaponHitPlan.timerSemanticResults(leafObservation.commit)) {
                    if (object2.leaf() != leafObservation.leaf) continue;
                    if (object != null) {
                        throw new IllegalStateException("Duplicate semantic result");
                    }
                    object = object2;
                }
                Group group = leafObservation.group;
                if (group.projection == null && group.emptyProjection != null && object != null && ((FoodWeaponHitPlan.TimerSemanticResult)object).admittedKeys() == 0 && ((FoodWeaponHitPlan.TimerSemanticResult)object).returnedHelpers() == 0 && !((FoodWeaponHitPlan.TimerSemanticResult)object).rangedEntered() && group.timers.isEmpty() && group.ranged.isEmpty() && !group.guardObserved && !group.acquired && !group.rooted) {
                    group.projection = group.emptyProjection;
                }
                if (object != null && ((FoodWeaponHitPlan.TimerSemanticResult)object).originalFailure() == null && ((FoodWeaponHitPlan.TimerSemanticResult)object).observationFailure() == null && ((FoodWeaponHitPlan.TimerSemanticResult)object).timerEntered() && ((FoodWeaponHitPlan.TimerSemanticResult)object).timerReturned() && ((FoodWeaponHitPlan.TimerSemanticResult)object).resolvedInstructions() == group.selectedInstructions && ((FoodWeaponHitPlan.TimerSemanticResult)object).admittedKeys() == group.timers.size() && ((FoodWeaponHitPlan.TimerSemanticResult)object).returnedHelpers() == group.timers.size() && group.projection != null && group.timers.size() == group.projection.writes().size() && (group.timers.isEmpty() || ((FoodWeaponHitPlan.TimerSemanticResult)object).rangedEntered() && group.guardObserved && (group.guardEmpty || group.acquired && group.rooted && group.ranged.size() == group.projection.rangedLongs().size()))) break block31;
                leafObservation.session.observationFailed(null, 3, null);
                if (LEAF.get() == leafObservation && leafObservation.thread == Thread.currentThread()) {
                    void var4_9;
                    leafObservation.closed = true;
                    LeafObservation leafObservation2 = leafObservation.previous;
                    while (var4_9 != null && var4_9.closed) {
                        LeafObservation leafObservation3 = var4_9.previous;
                    }
                    if (var4_9 == null) {
                        LEAF.remove();
                    } else {
                        LEAF.set((LeafObservation)var4_9);
                    }
                }
                return;
            }
            try {
                group.done = true;
            }
            catch (Throwable throwable2) {
                try {
                    leafObservation.session.observationFailed(null, 3, throwable2);
                }
                catch (Throwable throwable3) {
                    if (LEAF.get() == leafObservation && leafObservation.thread == Thread.currentThread()) {
                        leafObservation.closed = true;
                        LeafObservation leafObservation4 = leafObservation.previous;
                        while (leafObservation4 != null && leafObservation4.closed) {
                            leafObservation4 = leafObservation4.previous;
                        }
                        if (leafObservation4 == null) {
                            LEAF.remove();
                        } else {
                            LEAF.set(leafObservation4);
                        }
                    }
                    throw throwable3;
                }
                if (LEAF.get() == leafObservation && leafObservation.thread == Thread.currentThread()) {
                    leafObservation.closed = true;
                    LeafObservation leafObservation4 = leafObservation.previous;
                    while (leafObservation4 != null && leafObservation4.closed) {
                        leafObservation4 = leafObservation4.previous;
                    }
                    if (leafObservation4 == null) {
                        LEAF.remove();
                    } else {
                        LEAF.set(leafObservation4);
                    }
                }
                break block32;
            }
            if (LEAF.get() == leafObservation && leafObservation.thread == Thread.currentThread()) {
                leafObservation.closed = true;
                object = leafObservation.previous;
                while (object != null && ((LeafObservation)object).closed) {
                    object = ((LeafObservation)object).previous;
                }
                if (object == null) {
                    LEAF.remove();
                } else {
                    LEAF.set((LeafObservation)object);
                }
            }
        }
    }

    static Tag materialize(FoodWeaponExpectedTrace.Value value) {
        if (value == null) {
            return null;
        }
        return switch (value.kind) {
            case 1 -> ByteTag.m_128266_((byte)((byte)value.bits));
            case 2 -> ShortTag.m_129258_((short)((short)value.bits));
            case 3 -> IntTag.m_128679_((int)((int)value.bits));
            case 4 -> LongTag.m_128882_((long)value.bits);
            case 5 -> FloatTag.m_128566_((float)Float.intBitsToFloat((int)value.bits));
            case 6 -> DoubleTag.m_128500_((double)Double.longBitsToDouble(value.bits));
            case 8 -> StringTag.m_129297_((String)value.string);
            case 10 -> {
                CompoundTag var1_1 = new CompoundTag();
                for (String var5_5 : FoodWeaponExpectedTrace.compoundKeys(value)) {
                    Tag var6_6 = OwnerTimerSemanticAuthority.materialize(FoodWeaponExpectedTrace.field(value, var5_5));
                    if (var6_6 == null) {
                        throw new IllegalArgumentException("Unsupported quoted timer value");
                    }
                    var1_1.m_128365_(var5_5, var6_6);
                }
                yield var1_1;
            }
            default -> null;
        };
    }

    static final class Origin {
        private final Thread thread = Thread.currentThread();
        private final CompoundTag data;
        private CompoundTag baseline;
        private boolean claimed;

        private Origin(CompoundTag compoundTag) {
            this.data = compoundTag;
        }
    }

    public static final class LeafObservation {
        private final FoodWeaponOriginalSequence.Session session;
        private final FoodWeaponHitPlan.FinalEffectsCommit commit;
        private final Object leaf;
        private final Group group;
        private final Thread thread = Thread.currentThread();
        private final LeafObservation previous;
        private boolean closed;

        private LeafObservation(FoodWeaponOriginalSequence.Session session, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, Object object, Group group, LeafObservation leafObservation) {
            this.session = session;
            this.commit = finalEffectsCommit;
            this.leaf = object;
            this.group = group;
            this.previous = leafObservation;
        }
    }

    static final class Book {
        final OwnerFieldExpectedAuthority.Prepared prepared;
        private final IdentityHashMap<Object, Group> groups = new IdentityHashMap();
        private final IdentityHashMap<Object, Boolean> localLeaves = new IdentityHashMap();
        private final IdentityHashMap<Object, OwnerNativeSemanticAuthority.TargetWrite> targetWrites = new IdentityHashMap();
        private final Set<Object> targetReturned = Collections.newSetFromMap(new IdentityHashMap());
        private final IdentityHashMap<FoodWeaponExpectedTrace.Operation, OwnerNativeSemanticAuthority.PutBinding> materialized = new IdentityHashMap();
        private final Set<Object> nativeReturned = Collections.newSetFromMap(new IdentityHashMap());
        private FoodWeaponOriginalSequence.Session session;
        private boolean frozen;
        private final Object rangedRoot;
        private final Object rangedBuffer;

        Book(OwnerFieldExpectedAuthority.Prepared prepared, Object object, Object object2) {
            if (!OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "quoteTimers") || prepared == null) {
                throw new SecurityException("Original quote required");
            }
            this.prepared = prepared;
            this.rangedRoot = object;
            this.rangedBuffer = object2;
        }

        void quote(FoodWeaponHitPlan foodWeaponHitPlan, IdentityHashMap<Object, Boolean> identityHashMap, Object object, Object object2, Object object3, Function<Object, OwnerFieldExpectedAuthority.TimerQuote> function) {
            if (this.frozen || !OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "quoteTimers")) {
                throw new SecurityException("Original quote phase");
            }
            for (Map.Entry<Object, Boolean> entry : identityHashMap.entrySet()) {
                OwnerNativeSemanticAuthority.TargetWrite object4;
                if (!Boolean.TRUE.equals(entry.getValue()) || (object4 = OwnerNativeSemanticAuthority.reserveTarget(this.prepared, foodWeaponHitPlan, entry.getKey(), object2, object)) == null || this.targetWrites.put(entry.getKey(), object4) == null) continue;
                throw new IllegalArgumentException("Repeated original target leaf");
            }
            FoodWeaponHitPlan.TimerSemanticCatalogue timerSemanticCatalogue = FoodWeaponHitPlan.timerSemanticCatalogue(foodWeaponHitPlan);
            if (timerSemanticCatalogue == null || timerSemanticCatalogue.unsupported() != null) {
                throw new IllegalArgumentException("Genuine semantic catalogue");
            }
            for (FoodWeaponHitPlan.TimerSemanticProgram timerSemanticProgram : timerSemanticCatalogue.programs()) {
                OwnerFieldExpectedAuthority.TimerQuote timerQuote = function.apply(timerSemanticProgram.leaf());
                if (!(Boolean.TRUE.equals(identityHashMap.get(timerSemanticProgram.leaf())) && timerQuote != null && timerQuote.leaf == timerSemanticProgram.leaf() && timerQuote.weapon == object && timerQuote.player == object2 && timerQuote.source == object3 && timerQuote.origin != null && timerSemanticProgram.weapon() == object && timerSemanticProgram.player() == object2 && timerSemanticProgram.source() == object3 && timerSemanticProgram.unsupported() == null && timerQuote.profile.equals(FoodWeaponEvolution.planHitTimerProfile((FoodWeaponEvolution.Profile)((Object)timerSemanticProgram.profile()))) && FoodWeaponExpectedTrace.same(timerQuote.baseline, timerSemanticProgram.baseline()) && FoodWeaponExpectedTrace.same(timerQuote.data, timerSemanticProgram.initialData()) && !this.groups.containsKey(timerSemanticProgram.leaf()))) {
                    throw new IllegalArgumentException("Original detached timer quote required");
                }
                Group group = new Group(timerSemanticProgram, timerQuote.origin, timerQuote.profile, identityHashMap);
                this.groups.put(timerSemanticProgram.leaf(), group);
                for (FoodWeaponHitPlan.TimerSemanticInstruction timerSemanticInstruction : timerSemanticProgram.instructions()) {
                    this.localLeaves.put(timerSemanticInstruction.leaf(), Boolean.TRUE);
                }
            }
        }

        boolean local(Object object) {
            return this.localLeaves.containsKey(object) || this.targetWrites.containsKey(object);
        }

        boolean targetMatches(FoodWeaponOriginalSequence.Session session, Object object, OwnerNativeSemanticAuthority.TargetWrite targetWrite) {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("targetLive")).orElse(false));
            return bl && this.frozen && this.session == session && session.timerSemantics == this && this.prepared.timerSemantics == this && session.lineage == this.prepared.lineage && session.original.atLeaf(object) && this.targetWrites.get(object) == targetWrite && !this.targetReturned.contains(object) && OwnerFieldExpectedAuthority.unchanged(this.prepared);
        }

        void targetReturned(FoodWeaponOriginalSequence.Session session, Object object, OwnerNativeSemanticAuthority.TargetWrite targetWrite) {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class && stackFrame.getMethodName().equals("targetClosed")).orElse(false));
            if (!(bl && this.frozen && session == this.session && session.timerSemantics == this && this.prepared.timerSemantics == this && session.lineage == this.prepared.lineage && session.original.atLeaf(object) && this.targetWrites.get(object) == targetWrite && OwnerFieldExpectedAuthority.unchanged(this.prepared) && this.targetReturned.add(object))) {
                throw new IllegalStateException("Original target facts do not match saved selection");
            }
        }

        void bind(FoodWeaponOriginalSequence.Session session) {
            if (!OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "seal") || this.frozen || session == null || session.lineage != this.prepared.lineage) {
                throw new SecurityException("Original Prepared Session required");
            }
            this.session = session;
            this.frozen = true;
        }

        FoodWeaponExpectedTrace.Operation resolve(FoodWeaponOriginalSequence.Session session, Object object, FoodWeaponExpectedTrace.Kind kind, String string, String string2, FoodWeaponExpectedTrace.State state) {
            if (!(OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "originalBegins") && session == this.session && this.frozen && session.timerSemantics == this && this.prepared.timerSemantics == this && session.original.atLeaf(object) && this.prepared.lineage == session.lineage)) {
                return null;
            }
            FoodWeaponExpectedTrace.Builder builder = new FoodWeaponExpectedTrace.Builder(state);
            if (kind == FoodWeaponExpectedTrace.Kind.NATIVE_PUT) {
                OwnerNativeSemanticAuthority.PutBinding putBinding = this.prepared.nativePuts.get(object);
                if (putBinding == null || this.nativeReturned.contains(object)) {
                    return null;
                }
                FoodWeaponExpectedTrace.Operation operation = OwnerNativeSemanticAuthority.semanticOperation(putBinding, string, state, builder);
                if (operation != null) {
                    this.materialized.put(operation, putBinding);
                }
                return operation;
            }
            Group group = this.groups.get(object);
            if (group == null || group.done) {
                return null;
            }
            if (kind == FoodWeaponExpectedTrace.Kind.TIMER) {
                FoodWeaponHitPlan.TimerSemanticStep timerSemanticStep = FoodWeaponHitPlan.timerSemanticAtSessionOriginal(session, object, string);
                if (!(timerSemanticStep != null && group.accepts(timerSemanticStep.projection()) && group.profile.equals(string2) && string.equals(timerSemanticStep.key()) && timerSemanticStep.originalStepOrdinal() == group.timers.size() && !group.timers.contains(string) && FoodWeaponExpectedTrace.same(timerSemanticStep.value(), group.projection.writes().get(string)))) {
                    return null;
                }
                return builder.timer(group.profile, string, timerSemanticStep.value());
            }
            FoodWeaponHitPlan.TimerSemanticProjection timerSemanticProjection = FoodWeaponHitPlan.timerSemanticAtSessionRanged(session, object);
            if (!group.accepts(timerSemanticProjection) || group.timers.size() != timerSemanticProjection.writes().size() || !group.guardObserved || group.guardEmpty) {
                return null;
            }
            if (kind == FoodWeaponExpectedTrace.Kind.RANGED_ACQUIRE) {
                if (group.acquired || !"FTBossRangedProcTimers".equals(string)) {
                    return null;
                }
                if (!(this.rangedBuffer instanceof CompoundTag) || this.rangedRoot == null) {
                    return null;
                }
                return builder.rangedAcquire(this.rangedRoot, this.rangedBuffer);
            }
            if (!group.acquired || group.rooted) {
                return null;
            }
            if (kind == FoodWeaponExpectedTrace.Kind.RANGED_TIMER) {
                Long l = timerSemanticProjection.rangedLongs().get(string);
                return l == null || group.ranged.contains(string) ? null : builder.rangedLong(string, l);
            }
            if (kind == FoodWeaponExpectedTrace.Kind.RANGED_ROOT && "FTBossRangedProcTimers".equals(string) && group.ranged.size() == timerSemanticProjection.rangedLongs().size()) {
                return builder.rangedRoot();
            }
            return null;
        }

        boolean writerStep(FoodWeaponOriginalSequence.Session session, Object object, FoodWeaponHitPlan.TimerSemanticStep timerSemanticStep) {
            if (!OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "timerWriterExpected") || session != this.session || this.prepared.timerSemantics != this || session.timerSemantics != this || timerSemanticStep == null || timerSemanticStep != FoodWeaponHitPlan.timerSemanticAtSessionOriginal(session, object, timerSemanticStep.key())) {
                return false;
            }
            Group group = this.groups.get(object);
            return group != null && !group.done && group.accepts(timerSemanticStep.projection()) && FoodWeaponExpectedTrace.same(timerSemanticStep.value(), timerSemanticStep.projection().writes().get(timerSemanticStep.key()));
        }

        boolean nativeMatches(FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.Step step, OwnerNativeSemanticAuthority.PutBinding putBinding) {
            return OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "semanticNativeBinding") && session == this.session && session.timerSemantics == this && this.prepared.timerSemantics == this && step != null && step.session == session && this.materialized.get(step.expected) == putBinding;
        }

        void returned(FoodWeaponOriginalSequence.Step step, Object object, boolean bl, Throwable throwable) {
            if (!OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "close") || step.session != this.session || !bl || throwable != null) {
                return;
            }
            OwnerNativeSemanticAuthority.PutBinding putBinding = this.materialized.get(step.expected);
            if (putBinding != null) {
                this.nativeReturned.add(object);
                return;
            }
            Group group = this.groups.get(object);
            if (group == null) {
                return;
            }
            switch (step.expected.kind) {
                case TIMER: {
                    group.timers.add(step.expected.key);
                    break;
                }
                case RANGED_ACQUIRE: {
                    group.acquired = true;
                    break;
                }
                case RANGED_TIMER: {
                    group.ranged.add(step.expected.key);
                    break;
                }
                case RANGED_ROOT: {
                    group.rooted = true;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unquoted semantic kind");
                }
            }
        }

        boolean complete() {
            if (!OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "traceComplete") || !this.frozen || this.session == null || this.session.timerSemantics != this || this.prepared.timerSemantics != this || this.nativeReturned.size() != this.prepared.nativePuts.size() || this.targetReturned.size() != this.targetWrites.size()) {
                return false;
            }
            for (Group group : this.groups.values()) {
                if (group.done) continue;
                return false;
            }
            return true;
        }
    }

    static final class Group {
        private final FoodWeaponHitPlan.TimerSemanticProgram program;
        private final Origin origin;
        private final String profile;
        private final int selectedInstructions;
        private final FoodWeaponHitPlan.TimerSemanticProjection emptyProjection;
        private final Map<String, List<FoodWeaponExpectedTrace.Value>> alternatives;
        private final Set<String> timers = new HashSet<String>();
        private final Set<String> ranged = new HashSet<String>();
        private FoodWeaponHitPlan.TimerSemanticProjection projection;
        private boolean acquired;
        private boolean rooted;
        private boolean guardObserved;
        private boolean guardEmpty;
        private boolean done;

        private Group(FoodWeaponHitPlan.TimerSemanticProgram timerSemanticProgram, Origin origin, String string2, IdentityHashMap<Object, Boolean> identityHashMap) {
            boolean bl;
            Object object2;
            this.program = timerSemanticProgram;
            this.origin = origin;
            this.profile = string2;
            HashMap<String, List> hashMap = new HashMap<String, List>();
            for (String arrayList2 : FoodWeaponExpectedTrace.compoundKeys(timerSemanticProgram.initialData())) {
                hashMap.put(arrayList2, new ArrayList<FoodWeaponExpectedTrace.Value>(List.of(FoodWeaponExpectedTrace.field(timerSemanticProgram.initialData(), arrayList2))));
            }
            int n = 0;
            ArrayList<FoodWeaponHitPlan.TimerSemanticChoice> arrayList3 = new ArrayList<FoodWeaponHitPlan.TimerSemanticChoice>();
            int n2 = -1;
            for (FoodWeaponHitPlan.TimerSemanticInstruction timerSemanticInstruction : timerSemanticProgram.instructions()) {
                if (timerSemanticInstruction.planOrdinal() <= n2 || !identityHashMap.containsKey(timerSemanticInstruction.leaf())) {
                    throw new IllegalArgumentException("Original instruction identity/order");
                }
                n2 = timerSemanticInstruction.planOrdinal();
                boolean bl2 = Boolean.TRUE.equals(identityHashMap.get(timerSemanticInstruction.leaf()));
                arrayList3.add(new FoodWeaponHitPlan.TimerSemanticChoice(timerSemanticInstruction, bl2, bl2));
                if (!bl2) continue;
                ++n;
                object2 = hashMap.computeIfAbsent(timerSemanticInstruction.key(), string -> {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    arrayList.add(null);
                    return arrayList;
                });
                if (!timerSemanticInstruction.conditional()) {
                    object2.clear();
                }
                if (!object2.stream().noneMatch(value -> FoodWeaponExpectedTrace.same(value, timerSemanticInstruction.value()))) continue;
                object2.add(timerSemanticInstruction.value());
            }
            FoodWeaponHitPlan.timerSemanticQuoteProjection(timerSemanticProgram, List.copyOf(arrayList3));
            ArrayList<FoodWeaponHitPlan.TimerSemanticChoice> arrayList = new ArrayList<FoodWeaponHitPlan.TimerSemanticChoice>();
            for (FoodWeaponHitPlan.TimerSemanticChoice timerSemanticChoice : arrayList3) {
                arrayList.add(new FoodWeaponHitPlan.TimerSemanticChoice(timerSemanticChoice.instruction(), timerSemanticChoice.selected(), timerSemanticChoice.selected() && !timerSemanticChoice.instruction().conditional()));
            }
            boolean bl3 = true;
            for (Object object2 : hashMap.keySet()) {
                FoodWeaponHitPlan.TimerSemanticChoice timerSemanticChoice;
                int n3;
                FoodWeaponExpectedTrace.Value value2 = FoodWeaponExpectedTrace.field(timerSemanticProgram.initialData(), (String)object2);
                FoodWeaponExpectedTrace.Value value3 = FoodWeaponExpectedTrace.field(timerSemanticProgram.baseline(), (String)object2);
                int n4 = -1;
                for (n3 = 0; n3 < arrayList.size(); ++n3) {
                    timerSemanticChoice = (FoodWeaponHitPlan.TimerSemanticChoice)arrayList.get(n3);
                    if (!timerSemanticChoice.selected() || timerSemanticChoice.instruction().conditional() || !((String)object2).equals(timerSemanticChoice.instruction().key())) continue;
                    value2 = timerSemanticChoice.instruction().value();
                    n4 = n3;
                }
                if (FoodWeaponExpectedTrace.same(value2, value3)) continue;
                n3 = -1;
                for (int i = n4 + 1; i < arrayList.size(); ++i) {
                    FoodWeaponHitPlan.TimerSemanticChoice timerSemanticChoice2 = (FoodWeaponHitPlan.TimerSemanticChoice)arrayList.get(i);
                    if (!timerSemanticChoice2.selected() || !timerSemanticChoice2.instruction().conditional() || !((String)object2).equals(timerSemanticChoice2.instruction().key()) || !FoodWeaponExpectedTrace.same(timerSemanticChoice2.instruction().value(), value3)) continue;
                    n3 = i;
                }
                if (n3 < 0) {
                    bl = false;
                    break;
                }
                timerSemanticChoice = (FoodWeaponHitPlan.TimerSemanticChoice)arrayList.get(n3);
                arrayList.set(n3, new FoodWeaponHitPlan.TimerSemanticChoice(timerSemanticChoice.instruction(), true, true));
            }
            Iterator iterator = bl ? FoodWeaponHitPlan.timerSemanticQuoteProjection(timerSemanticProgram, List.copyOf(arrayList)) : null;
            this.emptyProjection = iterator != null && ((FoodWeaponHitPlan.TimerSemanticProjection)((Object)iterator)).writes().isEmpty() ? iterator : null;
            object2 = new HashMap();
            hashMap.forEach((arg_0, arg_1) -> Group.lambda$new$2((Map)object2, arg_0, arg_1));
            this.alternatives = Map.copyOf(object2);
            this.selectedInstructions = n;
        }

        private boolean accepts(FoodWeaponHitPlan.TimerSemanticProjection timerSemanticProjection) {
            if (!(timerSemanticProjection != null && timerSemanticProjection.program() == this.program && timerSemanticProjection.localData() != null && this.origin.thread == Thread.currentThread() && this.origin.claimed && OwnerFullRepairGuard.detachedLocalNode(this.origin.data) && OwnerFullRepairGuard.detachedLocalNode(this.origin.baseline))) {
                return false;
            }
            HashSet<String> hashSet = new HashSet<String>(this.alternatives.keySet());
            for (String object : FoodWeaponExpectedTrace.compoundKeys(timerSemanticProjection.localData())) {
                hashSet.add(object);
            }
            HashMap hashMap = new HashMap();
            HashMap<String, Long> hashMap2 = new HashMap<String, Long>();
            for (String string : hashSet) {
                FoodWeaponExpectedTrace.Value value = FoodWeaponExpectedTrace.field(timerSemanticProjection.localData(), string);
                List<FoodWeaponExpectedTrace.Value> list = this.alternatives.get(string);
                if (list == null || list.stream().noneMatch(value2 -> FoodWeaponExpectedTrace.same(value2, value))) {
                    return false;
                }
                if (value == null) continue;
                if (!FoodWeaponExpectedTrace.same(value, FoodWeaponExpectedTrace.field(this.program.baseline(), string))) {
                    hashMap.put(string, value);
                }
                if (!string.startsWith("Last") || value.kind != 4) continue;
                hashMap2.put(string, value.bits);
            }
            if (!hashMap2.equals(timerSemanticProjection.rangedLongs()) || hashMap.size() != timerSemanticProjection.writes().size()) {
                return false;
            }
            for (Map.Entry entry : hashMap.entrySet()) {
                if (FoodWeaponExpectedTrace.same((FoodWeaponExpectedTrace.Value)entry.getValue(), timerSemanticProjection.writes().get(entry.getKey()))) continue;
                return false;
            }
            if (this.projection != null && this.projection != timerSemanticProjection) {
                return false;
            }
            this.projection = timerSemanticProjection;
            return true;
        }

        private static /* synthetic */ void lambda$new$2(Map map, String string, List list) {
            map.put(string, Collections.unmodifiableList(new ArrayList(list)));
        }
    }
}

