package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Builder;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Kind;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Operation;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.State;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Bytes;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Ints;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Longs;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Sequence;
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
    private static final ThreadLocal<OwnerTimerSemanticAuthority.Origin> COPYING = new ThreadLocal<>();
    private static final ThreadLocal<OwnerTimerSemanticAuthority.LeafObservation> LEAF = new ThreadLocal<>();

    private OwnerTimerSemanticAuthority() {
    }

    private static boolean caller(Class<?> var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerTimerSemanticAuthority.class)
                        .findFirst()
                        .map(
                            var2x -> {
                                if (var2x.getDeclaringClass() != var0) {
                                    return false;
                                } else {
                                    String var3;
                                    String var4;
                                    switch (var1) {
                                        case "ftboss$ownerLocalDataCopy":
                                        case "ftboss$ownerLocalBaselineCopy":
                                            if (var0 != FoodWeaponEvolution.class) {
                                                return false;
                                            }

                                            var3 = "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin";
                                            var4 = "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/nbt/CompoundTag;";
                                            break;
                                        case "ftboss$ownerMaterialGuard":
                                            if (var0 != FoodWeaponRanged.class) {
                                                return false;
                                            }

                                            var3 = "net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin";
                                            var4 = "(Lnet/minecraft/nbt/CompoundTag;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)Z";
                                            break;
                                        case "ftboss$ownerSemanticLeaf":
                                            if (var0 != FoodWeaponHitPlan.class) {
                                                return false;
                                            }

                                            var3 = "net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin";
                                            var4 = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V";
                                            break;
                                        default:
                                            return var2x.getMethodName().equals(var1);
                                    }

                                    String var5 = var2x.getMethodName();
                                    String var10 = "wrapOperation\\$(?:zz[a-p]|z[b-p][a-p]|[b-p][a-p]{2,6}|[b-h][a-p]{7})(?:[0-9a-f]{3}|[1-9a-f][0-9a-f]{3,7})\\$"
                                        + Pattern.quote(var1);
                                    if (var2x.getDescriptor().equals(var4) && var5.matches(var10)) {
                                        try {
                                            Method var7 = var0.getDeclaredMethod(var5, var2x.getMethodType().parameterArray());
                                            MixinMerged var8 = var7.getDeclaredAnnotation(MixinMerged.class);
                                            return var8 != null && var3.equals(var8.mixin());
                                        } catch (LinkageError | SecurityException | ReflectiveOperationException var9) {
                                            return false;
                                        }
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        )
                        .orElse(false)
            );
    }

    private static void graph(Tag var0, IdentityHashMap<Object, Boolean> var1, int var2) {
        if (var0 != null && var2 <= 64) {
            boolean var3 = var0.getClass() == CompoundTag.class
                || var0.getClass() == ListTag.class
                || var0.getClass() == ByteArrayTag.class
                || var0.getClass() == IntArrayTag.class
                || var0.getClass() == LongArrayTag.class;
            if (!var3) {
                if (var0.getClass() != ByteTag.class
                    && var0.getClass() != ShortTag.class
                    && var0.getClass() != IntTag.class
                    && var0.getClass() != LongTag.class
                    && var0.getClass() != FloatTag.class
                    && var0.getClass() != DoubleTag.class
                    && var0.getClass() != StringTag.class
                    && var0.getClass() != EndTag.class) {
                    throw new IllegalArgumentException("Local tag implementation");
                }
            } else if (OwnerFullRepairGuard.detachedLocalNode(var0) && var1.put(var0, Boolean.TRUE) == null) {
                if (var0 instanceof Compound var4) {
                    Map<String, Tag> var6 = var4.ftboss$rawCompoundFields();
                    if (var1.put(var6, Boolean.TRUE) != null) {
                        throw new IllegalArgumentException("Local map alias");
                    }

                    for (Tag var8 : var6.values()) {
                        graph(var8, var1, var2 + 1);
                    }
                } else if (var0 instanceof Sequence var5) {
                    List<Tag> var10 = var5.ftboss$rawListFields();
                    if (var1.put(var10, Boolean.TRUE) != null) {
                        throw new IllegalArgumentException("Local list alias");
                    }

                    for (Tag var14 : var10) {
                        graph(var14, var1, var2 + 1);
                    }
                } else {
                    Object var11 = var0 instanceof Bytes var9
                        ? var9.ftboss$rawBytes()
                        : (var0 instanceof Ints var15 ? var15.ftboss$rawInts() : (var0 instanceof Longs var13 ? var13.ftboss$rawLongs() : null));
                    if (var11 == null || var1.put(var11, Boolean.TRUE) != null) {
                        throw new IllegalArgumentException("Local array alias");
                    }
                }
            } else {
                throw new IllegalArgumentException("Local copy is attached or aliased");
            }
        } else {
            throw new IllegalArgumentException("Local copy depth");
        }
    }

    private static boolean detachedCopy(CompoundTag var0, CompoundTag var1) {
        if (var0 != null && var1 != null && var0 != var1 && var0.getClass() == CompoundTag.class && var1.getClass() == CompoundTag.class) {
            IdentityHashMap var2 = new IdentityHashMap();
            IdentityHashMap var3 = new IdentityHashMap();
            graph(var0, var2, 0);
            graph(var1, var3, 0);

            for (Object var5 : var3.keySet()) {
                if (var2.containsKey(var5)) {
                    return false;
                }
            }

            return FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe(var0), NativeExpectedRawFields.observe(var1));
        } else {
            return false;
        }
    }

    public static void localDataCopied(CompoundTag var0, CompoundTag var1) {
        if (caller(FoodWeaponEvolution.class, "ftboss$ownerLocalDataCopy")) {
            COPYING.remove();
            if (detachedCopy(var0, var1)) {
                COPYING.set(new OwnerTimerSemanticAuthority.Origin(var1));
            }
        }
    }

    public static void localBaselineCopied(CompoundTag var0, CompoundTag var1) {
        if (caller(FoodWeaponEvolution.class, "ftboss$ownerLocalBaselineCopy")) {
            OwnerTimerSemanticAuthority.Origin var2 = COPYING.get();
            COPYING.remove();
            if (var2 != null && var2.thread == Thread.currentThread() && var2.data == var0 && detachedCopy(var0, var1)) {
                var2.baseline = var1;
                COPYING.set(var2);
            }
        }
    }

    static OwnerTimerSemanticAuthority.Origin claim(CompoundTag var0, CompoundTag var1) {
        if (!caller(OwnerFieldExpectedAuthority.class, "timerQuoted")) {
            return null;
        } else {
            OwnerTimerSemanticAuthority.Origin var2 = COPYING.get();
            COPYING.remove();
            boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> var0x.anyMatch(
                            var0xx -> var0xx.getDeclaringClass() == FoodWeaponEvolution.class
                                    && var0xx.getMethodName().equals("quoteFinalWeaponHit")
                                    && var0xx.getDescriptor()
                                        .equals(
                                            "(Lnet/farmingtales/ftboss/event/NativeWeaponDamageEvent$BeforeMitigation;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;"
                                        )
                        )
                );
            if (var3
                && var2 != null
                && !var2.claimed
                && var2.thread == Thread.currentThread()
                && var2.data == var1
                && var2.baseline == var0
                && OwnerFullRepairGuard.detachedLocalNode(var1)
                && OwnerFullRepairGuard.detachedLocalNode(var0)) {
                var2.claimed = true;
                return var2;
            } else {
                return null;
            }
        }
    }

    private static boolean savedLeaf(OwnerTimerSemanticAuthority.LeafObservation var0) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread() && LEAF.get() == var0) {
            OwnerTimerSemanticAuthority.Book var1 = var0.session.timerSemantics;
            return var1 != null
                && var1.session == var0.session
                && var1.prepared.timerSemantics == var1
                && var1.prepared.lineage == var0.session.lineage
                && var1.groups.get(var0.leaf) == var0.group
                && FoodWeaponHitPlan.originalSequence(var0.commit) == var0.session
                && var0.session.original.atLeaf(var0.leaf);
        } else {
            return false;
        }
    }

    static OwnerTimerSemanticAuthority.LeafObservation enter(FoodWeaponOriginalSequence.Session var0, FoodWeaponHitPlan.FinalEffectsCommit var1, Object var2) {
        if (caller(FoodWeaponSourceLedger.class, "ownerTimerLeafEntered") && var0 != null && var0.timerSemantics != null && var0.original.atLeaf(var2)) {
            OwnerTimerSemanticAuthority.Group var3 = var0.timerSemantics.groups.get(var2);
            if (var3 == null) {
                return null;
            } else {
                OwnerTimerSemanticAuthority.LeafObservation var4 = new OwnerTimerSemanticAuthority.LeafObservation(var0, var1, var2, var3, LEAF.get());
                LEAF.set(var4);
                return var4;
            }
        } else {
            return null;
        }
    }

    public static void rangedGuard(Object var0, Object var1, CompoundTag var2, boolean var3) {
        if (caller(FoodWeaponRanged.class, "ftboss$ownerMaterialGuard")) {
            OwnerTimerSemanticAuthority.LeafObservation var4 = LEAF.get();
            if (savedLeaf(var4)) {
                OwnerTimerSemanticAuthority.Group var5 = var4.group;
                FoodWeaponHitPlan.TimerSemanticProjection var6 = FoodWeaponHitPlan.timerSemanticAtRanged(
                    var4.commit, var4.leaf, (FoodWeaponSourceLedger.OwnerWriterPublication)var4.session.lineage.issuer
                );
                if (var0 == var5.program.player() && var1 == var5.program.source() && var2 != null && !var5.guardObserved && var5.accepts(var6)) {
                    var5.guardObserved = true;
                    var5.guardEmpty = var3;
                } else {
                    var4.session.observationFailed(null, 0, null);
                }
            }
        }
    }

    public static void leave(OwnerTimerSemanticAuthority.LeafObservation var0, Throwable var1) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread() && LEAF.get() == var0) {
            try {
                if (!caller(FoodWeaponHitPlan.class, "ftboss$ownerSemanticLeaf") || !savedLeaf(var0) || var1 != null) {
                    var0.session.observationFailed(null, 3, var1);
                    return;
                }

                FoodWeaponHitPlan.TimerSemanticResult var2 = null;

                for (FoodWeaponHitPlan.TimerSemanticResult var4 : FoodWeaponHitPlan.timerSemanticResults(var0.commit)) {
                    if (var4.leaf() == var0.leaf) {
                        if (var2 != null) {
                            throw new IllegalStateException("Duplicate semantic result");
                        }

                        var2 = var4;
                    }
                }

                OwnerTimerSemanticAuthority.Group var11 = var0.group;
                if (var11.projection == null
                    && var11.emptyProjection != null
                    && var2 != null
                    && var2.admittedKeys() == 0
                    && var2.returnedHelpers() == 0
                    && !var2.rangedEntered()
                    && var11.timers.isEmpty()
                    && var11.ranged.isEmpty()
                    && !var11.guardObserved
                    && !var11.acquired
                    && !var11.rooted) {
                    var11.projection = var11.emptyProjection;
                }

                if (var2 != null
                    && var2.originalFailure() == null
                    && var2.observationFailure() == null
                    && var2.timerEntered()
                    && var2.timerReturned()
                    && var2.resolvedInstructions() == var11.selectedInstructions
                    && var2.admittedKeys() == var11.timers.size()
                    && var2.returnedHelpers() == var11.timers.size()
                    && var11.projection != null
                    && var11.timers.size() == var11.projection.writes().size()
                    && (
                        var11.timers.isEmpty()
                            || var2.rangedEntered()
                                && var11.guardObserved
                                && (var11.guardEmpty || var11.acquired && var11.rooted && var11.ranged.size() == var11.projection.rangedLongs().size())
                    )) {
                    var11.done = true;
                    return;
                }

                var0.session.observationFailed(null, 3, null);
            } catch (Throwable var9) {
                var0.session.observationFailed(null, 3, var9);
                return;
            } finally {
                if (LEAF.get() == var0 && var0.thread == Thread.currentThread()) {
                    var0.closed = true;
                    OwnerTimerSemanticAuthority.LeafObservation var6 = var0.previous;

                    while (var6 != null && var6.closed) {
                        var6 = var6.previous;
                    }

                    if (var6 == null) {
                        LEAF.remove();
                    } else {
                        LEAF.set(var6);
                    }
                }
            }
        }
    }

    static Tag materialize(Value var0) {
        if (var0 == null) {
            return null;
        } else {
            return (Tag)(switch (var0.kind) {
                case 1 -> ByteTag.valueOf((byte)((int)var0.bits));
                case 2 -> ShortTag.valueOf((short)((int)var0.bits));
                case 3 -> IntTag.valueOf((int)var0.bits);
                case 4 -> LongTag.valueOf(var0.bits);
                case 5 -> FloatTag.valueOf(Float.intBitsToFloat((int)var0.bits));
                case 6 -> DoubleTag.valueOf(Double.longBitsToDouble(var0.bits));
                default -> null;
                case 8 -> StringTag.valueOf(var0.string);
                case 10 -> {
                    CompoundTag var1 = new CompoundTag();

                    for (String var5 : FoodWeaponExpectedTrace.compoundKeys(var0)) {
                        Tag var6 = materialize(FoodWeaponExpectedTrace.field(var0, var5));
                        if (var6 == null) {
                            throw new IllegalArgumentException("Unsupported quoted timer value");
                        }

                        var1.put(var5, var6);
                    }

                    yield var1;
                }
            });
        }
    }

    static final class Book {
        final OwnerFieldExpectedAuthority.Prepared prepared;
        private final IdentityHashMap<Object, OwnerTimerSemanticAuthority.Group> groups = new IdentityHashMap<>();
        private final IdentityHashMap<Object, Boolean> localLeaves = new IdentityHashMap<>();
        private final IdentityHashMap<Object, OwnerNativeSemanticAuthority.TargetWrite> targetWrites = new IdentityHashMap<>();
        private final Set<Object> targetReturned = Collections.newSetFromMap(new IdentityHashMap<>());
        private final IdentityHashMap<Operation, OwnerNativeSemanticAuthority.PutBinding> materialized = new IdentityHashMap<>();
        private final Set<Object> nativeReturned = Collections.newSetFromMap(new IdentityHashMap<>());
        private FoodWeaponOriginalSequence.Session session;
        private boolean frozen;
        private final Object rangedRoot;
        private final Object rangedBuffer;
        private Object acquiredRangedBuffer431;

        Book(OwnerFieldExpectedAuthority.Prepared var1, Object var2, Object var3) {
            if (OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "quoteTimers") && var1 != null) {
                this.prepared = var1;
                this.rangedRoot = var2;
                this.rangedBuffer = var3;
            } else {
                throw new SecurityException("Original quote required");
            }
        }

        void quote(
            FoodWeaponHitPlan var1,
            IdentityHashMap<Object, Boolean> var2,
            Object var3,
            Object var4,
            Object var5,
            Function<Object, OwnerFieldExpectedAuthority.TimerQuote> var6
        ) {
            if (!this.frozen && OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "quoteTimers")) {
                for (Entry var8 : var2.entrySet()) {
                    if (Boolean.TRUE.equals(var8.getValue())) {
                        OwnerNativeSemanticAuthority.TargetWrite var9 = OwnerNativeSemanticAuthority.reserveTarget(
                            this.prepared, var1, var8.getKey(), var4, var3
                        );
                        if (var9 != null && this.targetWrites.put(var8.getKey(), var9) != null) {
                            throw new IllegalArgumentException("Repeated original target leaf");
                        }
                    }
                }

                FoodWeaponHitPlan.TimerSemanticCatalogue var14 = FoodWeaponHitPlan.timerSemanticCatalogue(var1);
                if (var14 != null && var14.unsupported() == null) {
                    for (FoodWeaponHitPlan.TimerSemanticProgram var16 : var14.programs()) {
                        OwnerFieldExpectedAuthority.TimerQuote var10 = (OwnerFieldExpectedAuthority.TimerQuote)var6.apply(var16.leaf());
                        if (!Boolean.TRUE.equals(var2.get(var16.leaf()))
                            || var10 == null
                            || var10.leaf != var16.leaf()
                            || var10.weapon != var3
                            || var10.player != var4
                            || var10.source != var5
                            || var10.origin == null
                            || var16.weapon() != var3
                            || var16.player() != var4
                            || var16.source() != var5
                            || var16.unsupported() != null
                            || !var10.profile.equals(FoodWeaponEvolution.planHitTimerProfile((Profile)var16.profile()))
                            || !FoodWeaponExpectedTrace.same(var10.baseline, var16.baseline())
                            || !FoodWeaponExpectedTrace.same(var10.data, var16.initialData())
                            || this.groups.containsKey(var16.leaf())) {
                            throw new IllegalArgumentException("Original detached timer quote required");
                        }

                        OwnerTimerSemanticAuthority.Group var11 = new OwnerTimerSemanticAuthority.Group(var16, var10.origin, var10.profile, var2);
                        this.groups.put(var16.leaf(), var11);

                        for (FoodWeaponHitPlan.TimerSemanticInstruction var13 : var16.instructions()) {
                            this.localLeaves.put(var13.leaf(), Boolean.TRUE);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Genuine semantic catalogue");
                }
            } else {
                throw new SecurityException("Original quote phase");
            }
        }

        boolean local(Object var1) {
            return this.localLeaves.containsKey(var1) || this.targetWrites.containsKey(var1);
        }

        boolean targetMatches(FoodWeaponOriginalSequence.Session var1, Object var2, OwnerNativeSemanticAuthority.TargetWrite var3) {
            boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.skip(1L)
                            .findFirst()
                            .map(var0x -> var0x.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0x.getMethodName().equals("targetLive"))
                            .orElse(false)
                );
            return var4
                && this.frozen
                && this.session == var1
                && var1.timerSemantics == this
                && this.prepared.timerSemantics == this
                && var1.lineage == this.prepared.lineage
                && var1.original.atLeaf(var2)
                && this.targetWrites.get(var2) == var3
                && !this.targetReturned.contains(var2)
                && OwnerFieldExpectedAuthority.unchanged(this.prepared);
        }

        void targetReturned(FoodWeaponOriginalSequence.Session var1, Object var2, OwnerNativeSemanticAuthority.TargetWrite var3) {
            boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.skip(1L)
                            .findFirst()
                            .map(var0x -> var0x.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0x.getMethodName().equals("targetClosed"))
                            .orElse(false)
                );
            if (!var4
                || !this.frozen
                || var1 != this.session
                || var1.timerSemantics != this
                || this.prepared.timerSemantics != this
                || var1.lineage != this.prepared.lineage
                || !var1.original.atLeaf(var2)
                || this.targetWrites.get(var2) != var3
                || !OwnerFieldExpectedAuthority.unchanged(this.prepared)
                || !this.targetReturned.add(var2)) {
                throw new IllegalStateException("Original target facts do not match saved selection");
            }
        }

        void bind(FoodWeaponOriginalSequence.Session var1) {
            if (OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "seal")
                && !this.frozen
                && var1 != null
                && var1.lineage == this.prepared.lineage) {
                this.session = var1;
                this.frozen = true;
            } else {
                throw new SecurityException("Original Prepared Session required");
            }
        }

        Operation resolve(FoodWeaponOriginalSequence.Session var1, Object var2, Kind var3, String var4, String var5, State var6) {
            if (OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "originalBegins")
                && var1 == this.session
                && this.frozen
                && var1.timerSemantics == this
                && this.prepared.timerSemantics == this
                && var1.original.atLeaf(var2)
                && this.prepared.lineage == var1.lineage) {
                Builder var7 = new Builder(var6);
                if (var3 == Kind.NATIVE_PUT) {
                    OwnerNativeSemanticAuthority.PutBinding var11 = this.prepared.nativePuts.get(var2);
                    if (var11 != null && !this.nativeReturned.contains(var2)) {
                        Operation var13 = OwnerNativeSemanticAuthority.semanticOperation(var11, var4, var6, var7);
                        if (var13 != null) {
                            this.materialized.put(var13, var11);
                        }

                        return var13;
                    } else {
                        return null;
                    }
                } else {
                    OwnerTimerSemanticAuthority.Group var8 = this.groups.get(var2);
                    if (var8 == null || var8.done) {
                        return null;
                    } else if (var3 == Kind.TIMER) {
                        FoodWeaponHitPlan.TimerSemanticStep var12 = FoodWeaponHitPlan.timerSemanticAtSessionOriginal(var1, var2, var4);
                        return var12 != null
                                && var8.accepts(var12.projection())
                                && var8.profile.equals(var5)
                                && var4.equals(var12.key())
                                && var12.originalStepOrdinal() == var8.timers.size()
                                && !var8.timers.contains(var4)
                                && FoodWeaponExpectedTrace.same(var12.value(), var8.projection.writes().get(var4))
                            ? var7.nativeTimer(var8.profile, var4, var12.value())
                            : null;
                    } else {
                        FoodWeaponHitPlan.TimerSemanticProjection var9 = FoodWeaponHitPlan.timerSemanticAtSessionRanged(var1, var2);
                        if (!var8.accepts(var9) || var8.timers.size() != var9.writes().size() || !var8.guardObserved || var8.guardEmpty) {
                            return null;
                        } else if (var3 == Kind.RANGED_ACQUIRE) {
                            if (var8.acquired || !"FTBossRangedProcTimers".equals(var4)) {
                                return null;
                            } else {
                                Object plannedBuffer = this.rangedBuffer != null ? this.rangedBuffer : this.acquiredRangedBuffer431;
                                if (plannedBuffer instanceof CompoundTag && this.rangedRoot != null) {
                                    return var7.rangedAcquire(this.rangedRoot, plannedBuffer);
                                }
                                CompoundTag fresh = NativeExpectedFieldObservation.plannedRangedFresh431(var1, var2, this.rangedRoot, var6);
                                if (fresh == null || this.acquiredRangedBuffer431 != null) return null;
                                Operation acquisition = var7.rangedAcquireDetached431(this.rangedRoot, fresh);
                                this.acquiredRangedBuffer431 = fresh;
                                return acquisition;
                            }
                        } else if (!var8.acquired || var8.rooted) {
                            return null;
                        } else if (var3 == Kind.RANGED_TIMER) {
                            Long var10 = var9.rangedLongs().get(var4);
                            return var10 != null && !var8.ranged.contains(var4) ? var7.rangedLong(var4, var10) : null;
                        } else {
                            return var3 == Kind.RANGED_ROOT && "FTBossRangedProcTimers".equals(var4) && var8.ranged.size() == var9.rangedLongs().size()
                                ? var7.rangedRoot()
                                : null;
                        }
                    }
                }
            } else {
                return null;
            }
        }

        boolean writerStep(FoodWeaponOriginalSequence.Session var1, Object var2, FoodWeaponHitPlan.TimerSemanticStep var3) {
            if (OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "timerWriterExpected")
                && var1 == this.session
                && this.prepared.timerSemantics == this
                && var1.timerSemantics == this
                && var3 != null
                && var3 == FoodWeaponHitPlan.timerSemanticAtSessionOriginal(var1, var2, var3.key())) {
                OwnerTimerSemanticAuthority.Group var4 = this.groups.get(var2);
                return var4 != null
                    && !var4.done
                    && var4.accepts(var3.projection())
                    && FoodWeaponExpectedTrace.same(var3.value(), var3.projection().writes().get(var3.key()));
            } else {
                return false;
            }
        }

        boolean nativeMatches(FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.Step var2, OwnerNativeSemanticAuthority.PutBinding var3) {
            return OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "semanticNativeBinding")
                && var1 == this.session
                && var1.timerSemantics == this
                && this.prepared.timerSemantics == this
                && var2 != null
                && var2.session == var1
                && this.materialized.get(var2.expected) == var3;
        }

        void returned(FoodWeaponOriginalSequence.Step var1, Object var2, boolean var3, Throwable var4) {
            if (OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "close") && var1.session == this.session && var3 && var4 == null) {
                OwnerNativeSemanticAuthority.PutBinding var5 = this.materialized.get(var1.expected);
                if (var5 != null) {
                    this.nativeReturned.add(var2);
                } else {
                    OwnerTimerSemanticAuthority.Group var6 = this.groups.get(var2);
                    if (var6 != null) {
                        switch (var1.expected.kind) {
                            case TIMER:
                                var6.timers.add(var1.expected.key);
                                break;
                            case RANGED_ACQUIRE:
                                var6.acquired = true;
                                break;
                            case RANGED_TIMER:
                                var6.ranged.add(var1.expected.key);
                                break;
                            case RANGED_ROOT:
                                var6.rooted = true;
                                break;
                            default:
                                throw new IllegalArgumentException("Unquoted semantic kind");
                        }
                    }
                }
            }
        }

        boolean complete() {
            if (OwnerTimerSemanticAuthority.caller(FoodWeaponOriginalSequence.Session.class, "traceComplete")
                && this.frozen
                && this.session != null
                && this.session.timerSemantics == this
                && this.prepared.timerSemantics == this
                && this.nativeReturned.size() == this.prepared.nativePuts.size()
                && this.targetReturned.size() == this.targetWrites.size()) {
                for (OwnerTimerSemanticAuthority.Group var2 : this.groups.values()) {
                    if (!var2.done) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    static final class Group {
        private final FoodWeaponHitPlan.TimerSemanticProgram program;
        private final OwnerTimerSemanticAuthority.Origin origin;
        private final String profile;
        private final int selectedInstructions;
        private final FoodWeaponHitPlan.TimerSemanticProjection emptyProjection;
        private final Map<String, List<Value>> alternatives;
        private final Set<String> timers = new HashSet<>();
        private final Set<String> ranged = new HashSet<>();
        private FoodWeaponHitPlan.TimerSemanticProjection projection;
        private boolean acquired;
        private boolean rooted;
        private boolean guardObserved;
        private boolean guardEmpty;
        private boolean done;

        private Group(FoodWeaponHitPlan.TimerSemanticProgram var1, OwnerTimerSemanticAuthority.Origin var2, String var3, IdentityHashMap<Object, Boolean> var4) {
            this.program = var1;
            this.origin = var2;
            this.profile = var3;
            HashMap<String, List<Value>> var5 = new HashMap<>();

            for (String var9 : FoodWeaponExpectedTrace.compoundKeys(var1.initialData())) {
                var5.put(var9, new ArrayList<>(List.of(FoodWeaponExpectedTrace.field(var1.initialData(), var9))));
            }

            int var19 = 0;
            ArrayList<FoodWeaponHitPlan.TimerSemanticChoice> var20 = new ArrayList<>();
            int var21 = -1;

            for (FoodWeaponHitPlan.TimerSemanticInstruction var10 : var1.instructions()) {
                if (var10.planOrdinal() <= var21 || !var4.containsKey(var10.leaf())) {
                    throw new IllegalArgumentException("Original instruction identity/order");
                }

                var21 = var10.planOrdinal();
                boolean var11 = Boolean.TRUE.equals(var4.get(var10.leaf()));
                var20.add(new FoodWeaponHitPlan.TimerSemanticChoice(var10, var11, var11));
                if (var11) {
                    var19++;
                    List<Value> var12 = var5.computeIfAbsent(var10.key(), var0 -> {
                        ArrayList<Value> var1x = new ArrayList<>();
                        var1x.add(null);
                        return var1x;
                    });
                    if (!var10.conditional()) {
                        var12.clear();
                    }

                    if (var12.stream().noneMatch(var1x -> FoodWeaponExpectedTrace.same(var1x, var10.value()))) {
                        var12.add(var10.value());
                    }
                }
            }

            FoodWeaponHitPlan.timerSemanticQuoteProjection(var1, List.copyOf(var20));
            ArrayList<FoodWeaponHitPlan.TimerSemanticChoice> var23 = new ArrayList<>();

            for (FoodWeaponHitPlan.TimerSemanticChoice var26 : var20) {
                var23.add(
                    new FoodWeaponHitPlan.TimerSemanticChoice(var26.instruction(), var26.selected(), var26.selected() && !var26.instruction().conditional())
                );
            }

            boolean var25 = true;

            for (String var29 : var5.keySet()) {
                Value var13 = FoodWeaponExpectedTrace.field(var1.initialData(), var29);
                Value var14 = FoodWeaponExpectedTrace.field(var1.baseline(), var29);
                int var15 = -1;

                for (int var16 = 0; var16 < var23.size(); var16++) {
                    FoodWeaponHitPlan.TimerSemanticChoice var17 = (FoodWeaponHitPlan.TimerSemanticChoice)var23.get(var16);
                    if (var17.selected() && !var17.instruction().conditional() && var29.equals(var17.instruction().key())) {
                        var13 = var17.instruction().value();
                        var15 = var16;
                    }
                }

                if (!FoodWeaponExpectedTrace.same(var13, var14)) {
                    int var31 = -1;

                    for (int var32 = var15 + 1; var32 < var23.size(); var32++) {
                        FoodWeaponHitPlan.TimerSemanticChoice var18 = (FoodWeaponHitPlan.TimerSemanticChoice)var23.get(var32);
                        if (var18.selected()
                            && var18.instruction().conditional()
                            && var29.equals(var18.instruction().key())
                            && FoodWeaponExpectedTrace.same(var18.instruction().value(), var14)) {
                            var31 = var32;
                        }
                    }

                    if (var31 < 0) {
                        var25 = false;
                        break;
                    }

                    FoodWeaponHitPlan.TimerSemanticChoice var33 = (FoodWeaponHitPlan.TimerSemanticChoice)var23.get(var31);
                    var23.set(var31, new FoodWeaponHitPlan.TimerSemanticChoice(var33.instruction(), true, true));
                }
            }

            FoodWeaponHitPlan.TimerSemanticProjection var28 = var25 ? FoodWeaponHitPlan.timerSemanticQuoteProjection(var1, List.copyOf(var23)) : null;
            this.emptyProjection = var28 != null && var28.writes().isEmpty() ? var28 : null;
            HashMap<String, List<Value>> var30 = new HashMap<>();
            var5.forEach((var1x, var2x) -> var30.put(var1x, Collections.unmodifiableList(new ArrayList<>(var2x))));
            this.alternatives = Map.copyOf(var30);
            this.selectedInstructions = var19;
        }

        private boolean accepts(FoodWeaponHitPlan.TimerSemanticProjection var1) {
            if (var1 != null
                && var1.program() == this.program
                && var1.localData() != null
                && this.origin.thread == Thread.currentThread()
                && this.origin.claimed
                && OwnerFullRepairGuard.detachedLocalNode(this.origin.data)
                && OwnerFullRepairGuard.detachedLocalNode(this.origin.baseline)) {
                HashSet<String> var2 = new HashSet<>(this.alternatives.keySet());

                for (String var6 : FoodWeaponExpectedTrace.compoundKeys(var1.localData())) {
                    var2.add(var6);
                }

                HashMap<String, Value> var9 = new HashMap<>();
                HashMap<String, Long> var10 = new HashMap<>();

                for (String var13 : var2) {
                    Value var7 = FoodWeaponExpectedTrace.field(var1.localData(), var13);
                    List<Value> var8 = this.alternatives.get(var13);
                    if (var8 == null || var8.stream().noneMatch(var1x -> FoodWeaponExpectedTrace.same(var1x, var7))) {
                        return false;
                    }

                    if (var7 != null) {
                        if (!FoodWeaponExpectedTrace.same(var7, FoodWeaponExpectedTrace.field(this.program.baseline(), var13))) {
                            var9.put(var13, var7);
                        }

                        if (var13.startsWith("Last") && var7.kind == 4) {
                            var10.put(var13, var7.bits);
                        }
                    }
                }

                if (var10.equals(var1.rangedLongs()) && var9.size() == var1.writes().size()) {
                    for (Entry<String, Value> var14 : var9.entrySet()) {
                        if (!FoodWeaponExpectedTrace.same((Value)var14.getValue(), var1.writes().get(var14.getKey()))) {
                            return false;
                        }
                    }

                    if (this.projection != null && this.projection != var1) {
                        return false;
                    } else {
                        this.projection = var1;
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static final class LeafObservation {
        private final FoodWeaponOriginalSequence.Session session;
        private final FoodWeaponHitPlan.FinalEffectsCommit commit;
        private final Object leaf;
        private final OwnerTimerSemanticAuthority.Group group;
        private final Thread thread = Thread.currentThread();
        private final OwnerTimerSemanticAuthority.LeafObservation previous;
        private boolean closed;

        private LeafObservation(
            FoodWeaponOriginalSequence.Session var1,
            FoodWeaponHitPlan.FinalEffectsCommit var2,
            Object var3,
            OwnerTimerSemanticAuthority.Group var4,
            OwnerTimerSemanticAuthority.LeafObservation var5
        ) {
            this.session = var1;
            this.commit = var2;
            this.leaf = var3;
            this.group = var4;
            this.previous = var5;
        }
    }

    static final class Origin {
        private final Thread thread = Thread.currentThread();
        private final CompoundTag data;
        private CompoundTag baseline;
        private boolean claimed;

        private Origin(CompoundTag var1) {
            this.data = var1;
        }
    }
}
