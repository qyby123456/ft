package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Resolution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.farmingtales.ftboss.event.NativeWeaponDamagePhases;
import net.neoforged.neoforge.common.NeoForge;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

public final class FoodWeaponFinalDamage {
    private static final StackWalker ORIGINAL_CALLER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    private static final ThreadLocal<ArrayDeque<FoodWeaponFinalDamage.EventCapture>> EVENT_CAPTURES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<FoodWeaponFinalDamage.PreparedListedAction>> GUARD_WAITERS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final int MAX_CAPTURE_DEPTH = 32;

    private FoodWeaponFinalDamage() {
    }

    public static boolean effectsReady() {
        return false;
    }

    static boolean nativeOriginalInteractionActive(DamageSource source, LivingEntity target) {
        if (source == null || target == null || !(source.getEntity() instanceof ServerPlayer player)) {
            return false;
        }
        CompoundTag snapshot = FoodWeaponSourceLedger.resolveReadOnly(source, player);
        FoodWeaponSourceLedger.OriginalInteractionBinding binding = FoodWeaponSourceLedger.capturePrimaryInteractionBinding(
            snapshot, player, source, target
        );
        return binding != null && FoodWeaponSourceLedger.matchesOriginalInteraction(binding, snapshot, player);
    }

    static void rejectNativeProducer(BeforeMitigation event) {
        if (producerCaller(FoodSocketPowers.class, null) || producerCaller(FoodWeaponBossPassive.class, null)) {
            FoodWeaponFinalDamage.EventCapture capture = EVENT_CAPTURES.get().peekLast();
            if (capture != null && capture.event == event) {
                capture.failed = true;
            }
        }
    }

    static void capturePassiveScalar(BeforeMitigation event, float amount) {
        if (producerCaller(FoodWeaponBossPassive.class, null)) {
            FoodWeaponFinalDamage.EventCapture capture = currentCapture(event);
            if (capture != null && capture.append(FoodWeaponFinalDamage.ProducerFamily.PASSIVE, null, event.getAmount(), amount)) {
                event.setAmount(amount);
            }
        }
    }

    private static boolean originalNativeCaller(String var0, String var1, String var2, String originDescriptor) {
        return ORIGINAL_CALLER.walk(
            var3 -> {
                List var4 = var3.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponFinalDamage.class).limit(2L).toList();
                return var4.size() == 2
                    && ((StackFrame)var4.get(0)).getDeclaringClass() == NativeWeaponDamagePhases.class
                    && ((StackFrame)var4.get(0)).getMethodName().endsWith(var1)
                    && ((StackFrame)var4.get(0)).getDescriptor().equals(var2)
                    && ((StackFrame)var4.get(1)).getDeclaringClass() == NativeWeaponDamagePhases.class
                    && ((StackFrame)var4.get(1)).getMethodName().equals(var0)
                    && ((StackFrame)var4.get(1))
                        .getDescriptor()
                        .equals(originDescriptor);
            }
        );
    }

    private static boolean producerCaller(Class<?> var0, String var1) {
        return ORIGINAL_CALLER.walk(
            var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponFinalDamage.class)
                    .findFirst()
                    .map(
                        var2x -> var2x.getDeclaringClass() == var0
                                && (var1 == null || var2x.getMethodName().equals(var1))
                                && var2x.getDescriptor().equals("(Lnet/farmingtales/ftboss/event/NativeWeaponDamageEvent$BeforeMitigation;)V")
                    )
                    .orElse(false)
        );
    }

    public static boolean dispatchOriginalHurtEvent(IEventBus var0, Event var1) {
        if (var0 == NeoForge.EVENT_BUS
            && var1 instanceof BeforeMitigation var2
            && originalNativeCaller(
                "beforeMitigation", "ftboss$paidHurtDispatch", "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;",
                "(Lnet/minecraft/world/entity/LivingEntity;Lnet/neoforged/neoforge/common/damagesource/DamageContainer;)Z"
            )
            && var2.getSource().getEntity() instanceof ServerPlayer var3) {
            ArrayDeque<FoodWeaponFinalDamage.EventCapture> var36 = EVENT_CAPTURES.get();
            FoodWeaponFinalDamage.EventCapture var5 = null;
            Throwable var6 = null;

            boolean var9;
            try {
                CompoundTag var7 = FoodWeaponSourceLedger.resolveReadOnly(var2.getSource(), var3);
                if (var7.isEmpty() || !sourceMatchesTarget(var7, var3, var2.getSource(), var2.getEntity())) {
                    return NativeDamageEventPosting.post(var0, var1);
                }

                if (var36.size() >= 32) {
                    return NativeDamageEventPosting.post(var0, var1);
                }

                FoodWeaponSourceLedger.OriginalInteractionBinding var8 = FoodWeaponSourceLedger.capturePrimaryInteractionBinding(
                    var7, var3, var2.getSource(), var2.getEntity()
                );
                if (var8 != null) {
                    synchronized (FoodWeaponSourceLedger.class) {
                        var5 = new FoodWeaponFinalDamage.EventCapture(var2, var3, var7, var8);
                        var36.addLast(var5);
                    }

                    var9 = NativeDamageEventPosting.post(var0, var1);
                    if (!var9 && var5.live()) {
                        FoodWeaponFinalDamage.PreparedListedAction var10 = sealCapturedProducers(var5);
                        if (var10 != null) {
                            ArrayDeque<FoodWeaponFinalDamage.PreparedListedAction> var11 = GUARD_WAITERS.get();
                            var11.removeIf(
                                var0x -> var0x.capture.thread != Thread.currentThread()
                                        || clock(var0x.prepared.player) != var0x.prepared.at
                                        || !var0x.prepared.stable()
                            );
                            if (var11.size() < 32) {
                                var11.addLast(var10);
                            }
                        }
                    }

                    return var9;
                }

                var9 = NativeDamageEventPosting.post(var0, var1);
            } catch (Throwable var34) {
                var6 = var34;
                throw var34;
            } finally {
                try {
                    synchronized (FoodWeaponSourceLedger.class) {
                        if (var5 != null) {
                            var5.closed = true;
                            if (var36.peekLast() == var5) {
                                var36.removeLast();
                            } else {
                                var36.remove(var5);
                            }
                        }

                        if (var36.isEmpty()) {
                            EVENT_CAPTURES.remove();
                        }
                    }
                } catch (Throwable var33) {
                    if (var6 == null) {
                        throw var33;
                    }

                    retainCleanupFailure(var6, var33);
                }
            }

            return var9;
        }

        return NativeDamageEventPosting.post(var0, var1);
    }

    private static FoodWeaponFinalDamage.EventCapture currentCapture(BeforeMitigation var0) {
        FoodWeaponFinalDamage.EventCapture var1 = EVENT_CAPTURES.get().peekLast();
        return var1 != null && var1.event == var0 && var1.live() ? var1 : null;
    }

    public static void captureEvolution(BeforeMitigation var0) {
        if (producerCaller(FoodWeaponEvolution.class, "weaponHit")) {
            FoodWeaponFinalDamage.EventCapture var1 = currentCapture(var0);
            if (var1 != null) {
                float var2 = var0.getAmount();
                FoodWeaponBondHits.Hit var3 = FoodWeaponBondHits.resolve(var1.source, var1.player);
                ItemStack var4 = var3 == null ? ItemStack.EMPTY : FoodWeaponBondHits.equipped(var1.player, var3);
                if (var3 != null && !var4.isEmpty()) {
                    FoodWeaponHitPlan var5 = FoodWeaponEvolution.quoteFinalWeaponHit(
                        var0, var3, var4, (var1x, var2x) -> FoodWeaponSourceLedger.quoteAttempt(var1.snapshot, var1.player, var1x, var2x)
                    );
                    if (var1.append(FoodWeaponFinalDamage.ProducerFamily.EVOLUTION, var5, var2, var5.amount())) {
                        var0.setAmount(var5.amount());
                    }
                } else {
                    var1.failed = true;
                }
            }
        }
    }

    public static void captureMaterial(BeforeMitigation var0) {
        if (producerCaller(WeaponMaterialBuild.class, "hit")) {
            FoodWeaponFinalDamage.EventCapture var1 = currentCapture(var0);
            if (var1 != null) {
                float var2 = var0.getAmount();
                FoodWeaponBondHits.Hit var3 = FoodWeaponBondHits.resolve(var1.source, var1.player);
                if (var3 == null) {
                    var1.failed = true;
                } else {
                    FoodWeaponHitPlan var4 = WeaponMaterialBuild.quoteFinalHit(var0, var3);
                    if (var1.append(FoodWeaponFinalDamage.ProducerFamily.MATERIAL, var4, var2, var4.amount())) {
                        var0.setAmount(var4.amount());
                    }
                }
            }
        }
    }

    public static void captureAttribute(BeforeMitigation var0, FoodSocketAttributePowers.OffensePlan var1) {
        if (producerCaller(FoodSocketAttributePowers.class, null)) {
            FoodWeaponFinalDamage.EventCapture var2 = currentCapture(var0);
            if (var2 != null) {
                float var3 = var0.getAmount();
                if (var1 == null || Float.floatToRawIntBits(var1.amount()) == Float.floatToRawIntBits(var3) && var1.snapshot().equals(var2.snapshot)) {
                    var2.append(FoodWeaponFinalDamage.ProducerFamily.ATTRIBUTE, var1, var3, var3);
                } else {
                    var2.failed = true;
                }
            }
        }
    }

    public static void capturePack(BeforeMitigation var0, FoodSocketPowers.PackPlan var1) {
        if (producerCaller(FoodSocketPowers.class, null)) {
            FoodWeaponFinalDamage.EventCapture var2 = currentCapture(var0);
            if (var2 != null) {
                if (var1 != null && !var1.snapshot().equals(var2.snapshot)) {
                    var2.failed = true;
                } else {
                    var2.append(FoodWeaponFinalDamage.ProducerFamily.WOLF, var1, var0.getAmount(), var0.getAmount());
                }
            }
        }
    }

    public static void capturePassiveHit(BeforeMitigation var0, FoodWeaponBossPassive.HitPlan var1) {
        if (producerCaller(FoodWeaponBossPassive.class, null)) {
            FoodWeaponFinalDamage.EventCapture var2 = currentCapture(var0);
            if (var2 != null) {
                float var3 = var0.getAmount();
                float var4 = var1 == null ? var3 : var1.amount();
                if (var1 == null || Float.floatToRawIntBits(var1.input()) == Float.floatToRawIntBits(var3) && var1.context().snapshot().equals(var2.snapshot)) {
                    if (var2.append(FoodWeaponFinalDamage.ProducerFamily.PASSIVE, var1, var3, var4)) {
                        var0.setAmount(var4);
                    }
                } else {
                    var2.failed = true;
                }
            }
        }
    }

    private static FoodWeaponFinalDamage.PreparedListedAction sealCapturedProducers(FoodWeaponFinalDamage.EventCapture var0) {
        if (var0.live() && var0.producers.size() == FoodWeaponFinalDamage.ProducerFamily.values().length) {
            ArrayList var1 = new ArrayList();
            ArrayList var2 = new ArrayList();
            FoodWeaponHitPlan var3 = null;
            FoodWeaponHitPlan var4 = null;
            FoodSocketAttributePowers.OffensePlan var5 = null;
            FoodSocketPowers.PackPlan var6 = null;
            FoodWeaponBossPassive.HitPlan var7 = null;

            for (FoodWeaponFinalDamage.CapturedProducer var9 : var0.producers) {
                Object var14 = var9.plan();
                if (var14 instanceof FoodWeaponHitPlan) {
                    FoodWeaponHitPlan var10 = (FoodWeaponHitPlan)var14;

                    for (FoodWeaponHitPlan.Attempt var15 : var10.attempts()) {
                        var1.add(new FoodWeaponSourceLedger.AttemptRequest(var15.effect(), var15.chance()));
                    }

                    var2.addAll(var10.targets());
                    if (var9.family() == FoodWeaponFinalDamage.ProducerFamily.MATERIAL) {
                        var3 = var10;
                    } else {
                        if (var9.family() != FoodWeaponFinalDamage.ProducerFamily.EVOLUTION) {
                            return null;
                        }

                        var4 = var10;
                    }
                } else if (var9.plan() instanceof FoodSocketAttributePowers.OffensePlan var11) {
                    var5 = var11;
                    var1.addAll(var11.attempts());
                } else if (var9.plan() instanceof FoodSocketPowers.PackPlan var12) {
                    var6 = var12;
                    var1.addAll(var12.attempts());
                    var2.addAll(var12.targets());
                } else if (var9.plan() instanceof FoodWeaponBossPassive.HitPlan var13) {
                    var7 = var13;
                    var1.addAll(var13.attempts());
                    var2.addAll(var13.targets());
                } else if (var9.plan() != null) {
                    return null;
                }
            }

            if (var1.isEmpty() && var2.isEmpty()) {
                return null;
            } else {
                FoodWeaponFinalDamage.PreparedBatch var16 = admitTargetBatch(var0.snapshot, var0.player, var0.source, var0.target, var1, var2);
                if (var16 != null && var16.originalBinding == var0.originalBinding) {
                    for (FoodWeaponFinalDamage.CapturedProducer var19 : var0.producers) {
                        Object var21 = var19.plan();
                        if (var21 instanceof FoodWeaponHitPlan) {
                            FoodWeaponHitPlan var20 = (FoodWeaponHitPlan)var21;

                            for (FoodWeaponHitPlan.Attempt var23 : var20.attempts()) {
                                FoodWeaponSourceLedger.AttemptQuote var28 = var16.batch
                                    .entries()
                                    .stream()
                                    .filter(var1x -> var1x.domain() == var23.effect())
                                    .findFirst()
                                    .orElse(null);
                                FoodWeaponSourceLedger.AttemptQuote var29 = var23.outcome();
                                if (var28 == null
                                    || var28.status() != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                                    || var29.status() != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                                    || Double.doubleToRawLongBits(var28.chance()) != Double.doubleToRawLongBits(var29.chance())
                                    || Double.doubleToRawLongBits(var28.sample()) != Double.doubleToRawLongBits(var29.sample())
                                    || var28.success() != var29.success()) {
                                    return null;
                                }
                            }
                        }
                    }

                    FoodWeaponFinalDamage.PreparedListedAction var18 = collectListedProducers(
                        var16, var3, var4, var5, var6, var7, var0.producers.stream().map(FoodWeaponFinalDamage.CapturedProducer::family).toList()
                    );
                    if (var18 != null) {
                        var18.capture = var0;
                    }

                    return var18;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private static FoodWeaponFinalDamage.PreparedListedAction takeGuardWaiter(LivingEntity var0, DamageSource var1) {
        ArrayDeque<FoodWeaponFinalDamage.PreparedListedAction> var2 = GUARD_WAITERS.get();
        Iterator var3 = var2.descendingIterator();
        FoodWeaponFinalDamage.PreparedListedAction var4 = null;

        while (var3.hasNext()) {
            FoodWeaponFinalDamage.PreparedListedAction var5 = (FoodWeaponFinalDamage.PreparedListedAction)var3.next();
            if (!var5.prepared.stable()) {
                var3.remove();
            } else if (var5.prepared.target == var0 && var5.prepared.source == var1) {
                var3.remove();
                var4 = var5;
                break;
            }
        }

        if (var2.isEmpty()) {
            GUARD_WAITERS.remove();
        }

        return var4;
    }

    static void retireOriginalCapture(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        if (var0 != null
            && Thread.holdsLock(FoodWeaponSourceLedger.class)
            && ORIGINAL_CALLER.walk(
                var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponFinalDamage.class)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponSourceLedger.class)
                        .orElse(false)
            )) {
            ArrayDeque<FoodWeaponFinalDamage.PreparedListedAction> var1 = GUARD_WAITERS.get();
            var1.removeIf(var1x -> {
                if (var1x.prepared.originalBinding != var0) {
                    return false;
                } else {
                    if (var1x.publication != null) {
                        var1x.publication.closed = true;
                    }

                    return true;
                }
            });
            if (var1.isEmpty()) {
                GUARD_WAITERS.remove();
            }
        }
    }

    private static boolean receiptLive(FoodWeaponFinalDamage.Publication var0) {
        if (var0 != null && var0.originalContinuation != null) {
            return originalReceiptLive(var0);
        } else if (var0 != null && (var0.publishingCleanup == null || var0.publishingCleanup.phase == FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)) {
            if (!publicationLive(var0)) {
                return false;
            } else {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (var0.closed
                        || var0.publishingCleanup != null && var0.publishingCleanup.phase != FoodWeaponFinalDamage.PublishingPhase.PUBLISHING
                        || !var0.readiness.matches(var0)) {
                        return false;
                    } else if (var0.currentStateProducer != null) {
                        return var0.currentInvocation == null && var0.producers.contains(var0.currentStateProducer);
                    } else {
                        FoodWeaponFinalDamage.ListedProducerInvocation var2 = var0.currentInvocation;
                        return var2 != null
                            && !var2.closed
                            && var2.childCall == null
                            && var2.publication == var0
                            && var2.ordinal >= 0
                            && var2.ordinal < var0.producers.size()
                            && var0.producers.get(var2.ordinal) == var2.producer;
                    }
                }
            }
        } else {
            return false;
        }
    }

    private static boolean publicationLive(FoodWeaponFinalDamage.Publication var0) {
        if (var0 != null && var0.originalContinuation != null) {
            return originalPublicationLive(var0);
        } else {
            FoodWeaponFinalDamage.PreparedBatch var1;
            FoodWeaponSourceLedger.OwnerWriterPublication var2;
            FoodWeaponFinalDamage.ReservationReadiness var3;
            FoodWeaponSourceLedger.AttemptBatchCommit var4;
            List var5;
            synchronized (FoodWeaponSourceLedger.class) {
                if (var0 == null || var0.closed || !var0.readiness.matches(var0)) {
                    return false;
                }

                var1 = var0.prepared;
                var2 = var0.writerPublication;
                var3 = var0.readiness;
                var4 = var0.result;
                var5 = var0.producers;
                if (!var1.committed
                    || var1.originalBinding == null
                    || !Thread.holdsLock(FoodWeaponSourceLedger.class)
                    || Thread.currentThread() != var1.thread
                    || var0.server != var1.player.serverLevel().getServer()
                    || !var0.server.isSameThread()
                    || clock(var1.player) != var1.at
                    || !Float.isFinite(var0.amount)
                    || var0.amount <= 0.0F
                    || var1.admission.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
                    || var4.status() != FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED) {
                    return false;
                }
            }

            if (FoodWeaponSourceLedger.matchesOriginalInteraction(var1.originalBinding, var1.snapshot, var1.player)
                && FoodWeaponSourceLedger.resolveReadOnly(var1.source, var1.player).equals(var1.snapshot)
                && sourceMatchesTarget(var1.snapshot, var1.player, var1.source, var1.target)
                && FoodWeaponSourceLedger.matchesAttemptRequests(var1.batch, var1.requests, var1.targets)
                && FoodWeaponSourceLedger.matchesAttemptBatch(var1.snapshot, var1.player, var1.batch, var4, var1.at)
                && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(var2)
                && FoodWeaponSourceLedger.matchesOriginalInteraction(var1.originalBinding, var1.snapshot, var1.player)) {
                synchronized (FoodWeaponSourceLedger.class) {
                    return var0.prepared == var1
                        && var0.writerPublication == var2
                        && var0.readiness == var3
                        && var0.result == var4
                        && var0.producers == var5
                        && !var0.closed
                        && var3.matches(var0)
                        && var1.committed
                        && var1.originalBinding != null
                        && Thread.holdsLock(FoodWeaponSourceLedger.class)
                        && Thread.currentThread() == var1.thread
                        && var0.server == var1.player.serverLevel().getServer()
                        && var0.server.isSameThread()
                        && clock(var1.player) == var1.at
                        && Float.isFinite(var0.amount)
                        && var0.amount > 0.0F
                        && var1.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
                        && var4.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED;
                }
            } else {
                return false;
            }
        }
    }

    private static boolean producerPublished(FoodWeaponFinalDamage.ListedProducer var0, FoodWeaponFinalDamage.Publication var1) {
        if (var0 instanceof FoodWeaponFinalDamage.AttributeProducer var6) {
            return FoodSocketAttributePowers.matchesOffenseCommit(
                var6.consumerCommit,
                var6.consumerPrepared,
                var6.plan,
                var1.receipt,
                var1.amount,
                var0.prepared.batch,
                var0.prepared.admission,
                var0.prepared.requests,
                var0.prepared.targets
            );
        } else if (var0 instanceof FoodWeaponFinalDamage.MaterialProducer var5) {
            return var5.consumerCommit != null;
        } else if (var0 instanceof FoodWeaponFinalDamage.EvolutionProducer var4) {
            return var4.consumerCommit != null;
        } else if (var0 instanceof FoodWeaponFinalDamage.WolfProducer var3) {
            return FoodSocketPowers.matchesPackCommit(
                var3.consumerCommit,
                var3.consumerPrepared,
                var3.plan,
                var1.receipt,
                var1.amount,
                var0.prepared.batch,
                var0.prepared.admission,
                var0.prepared.requests,
                var0.prepared.targets
            );
        } else {
            return var0 instanceof FoodWeaponFinalDamage.PassiveProducer var2
                ? FoodWeaponBossPassive.matchesPassiveHitCommit(
                    var2.consumerCommit,
                    var2.consumerPrepared,
                    var2.plan,
                    var1.receipt,
                    var1.amount,
                    var0.prepared.batch,
                    var0.prepared.admission,
                    var0.prepared.requests,
                    var0.prepared.targets
                )
                : false;
        }
    }

    private static boolean localPublishingIdentity(FoodWeaponFinalDamage.PublishingCleanup var0, FoodWeaponFinalDamage.PublishingPhase var1) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 != null && var0.phase == var1 && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == var0.thread) {
                FoodWeaponFinalDamage.Publication var3 = var0.publication;
                FoodWeaponFinalDamage.PreparedBatch var4 = var0.prepared;
                FoodWeaponFinalDamage.GuardResult var5 = var0.guard;
                return !var3.closed
                    && var3.publishingCleanup == var0
                    && var3.prepared == var4
                    && var4.committed
                    && var4.thread == var0.thread
                    && var4.at == var0.at
                    && var3.server == var0.server
                    && var3.receipt == var0.receipt
                    && var3.result == var0.result
                    && var3.guardResult == var5
                    && var3.currentStateProducer == var0.producer
                    && var3.currentInvocation == null
                    && var0.producer.prepared() == var4
                    && var0.producer.publishAttempted
                    && var0.producer.preparedConsumer() == var0.consumerPrepared
                    && var0.consumerPrepared != null
                    && var0.ordinal >= 0
                    && var0.ordinal < var3.producers.size()
                    && var3.producers.get(var0.ordinal) == var0.producer
                    && var3.readiness.publication == var3
                    && var3.readiness.prepared == var4
                    && var3.readiness.result == var0.result
                    && var3.readiness.producers == var3.producers
                    && var5.action.prepared == var4
                    && var5.action.publication == var3
                    && var5.action.producers == var3.producers
                    && var5.action.guardResult == var5
                    && var5.action.capture != null
                    && var5.action.capture.originalBinding == var0.capturedBinding
                    && var4.originalBinding == var0.originalBinding
                    && var5.originalBinding == var0.originalBinding
                    && var5.player == var4.player
                    && var5.source == var4.source
                    && var5.target == var4.target
                    && var5.thread == var0.thread
                    && var5.at == var0.at
                    && var5.acceptedBits == Float.floatToRawIntBits(var3.amount);
            } else {
                return false;
            }
        }
    }

    private static boolean publishingIdentity(FoodWeaponFinalDamage.PublishingCleanup var0, FoodWeaponFinalDamage.PublishingPhase var1) {
        if (!localPublishingIdentity(var0, var1)) {
            return false;
        } else if (var0.publication.originalContinuation != null) {
            FoodWeaponFinalDamage.Publication.OriginalContinuation var3 = var0.publication.originalContinuation;
            return var1 == FoodWeaponFinalDamage.PublishingPhase.FINALIZING
                ? matchesOriginalRunAdmission(var3.sequence.original, var0.publication, var3)
                : originalPublicationLive(var0.publication) && var0.publication.originalSequence.original.atProducer(var0.producer);
        } else {
            FoodWeaponFinalDamage.PreparedBatch var2 = var0.prepared;
            return !FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(
                    var0.capturedBinding, FoodWeaponSourceLedger.originalInteractionBinding(var2.batch), var2.player
                )
                ? false
                : localPublishingIdentity(var0, var1);
        }
    }

    static boolean matchesOffensePublishing(
        FoodSocketAttributePowers.PreparedOffense var0, FoodWeaponFinalDamage.OffenseReceipt var1, FoodWeaponFinalDamage.PublishingCleanup var2
    ) {
        return var2 != null
            && var2.producer instanceof FoodWeaponFinalDamage.AttributeProducer
            && var2.consumerPrepared == var0
            && var2.receipt == var1
            && publishingIdentity(var2, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
    }

    static boolean matchesPackPublishing(
        FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.OffenseReceipt var1, FoodWeaponFinalDamage.PublishingCleanup var2
    ) {
        return var2 != null
            && var2.producer instanceof FoodWeaponFinalDamage.WolfProducer
            && var2.consumerPrepared == var0
            && var2.receipt == var1
            && publishingIdentity(var2, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
    }

    static boolean matchesPassiveHitPublishing(
        FoodWeaponBossPassive.PreparedHit var0, FoodWeaponFinalDamage.OffenseReceipt var1, FoodWeaponFinalDamage.PublishingCleanup var2
    ) {
        return var2 != null
            && var2.producer instanceof FoodWeaponFinalDamage.PassiveProducer
            && var2.consumerPrepared == var0
            && var2.receipt == var1
            && publishingIdentity(var2, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
    }

    static boolean matchesOffensePublicationCleanup(FoodSocketAttributePowers.PreparedOffense var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        return var1 != null
            && var1.producer instanceof FoodWeaponFinalDamage.AttributeProducer
            && var1.consumerPrepared == var0
            && publishingIdentity(var1, FoodWeaponFinalDamage.PublishingPhase.FINALIZING);
    }

    static boolean matchesPackPublicationCleanup(FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        return var1 != null
            && var1.producer instanceof FoodWeaponFinalDamage.WolfProducer
            && var1.consumerPrepared == var0
            && publishingIdentity(var1, FoodWeaponFinalDamage.PublishingPhase.FINALIZING);
    }

    static boolean matchesPassiveHitPublicationCleanup(FoodWeaponBossPassive.PreparedHit var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        return var1 != null
            && var1.producer instanceof FoodWeaponFinalDamage.PassiveProducer
            && var1.consumerPrepared == var0
            && publishingIdentity(var1, FoodWeaponFinalDamage.PublishingPhase.FINALIZING);
    }

    private static boolean localInvocationReady(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        if (var0 != null && var0.invocation != null && var0.invocation.publication.originalContinuation != null) {
            return originalInvocationLive(var0);
        } else if (var0 != null && !var0.closed) {
            FoodWeaponFinalDamage.ListedProducerInvocation var1 = var0.invocation;
            if (var1 != null && !var1.closed) {
                FoodWeaponFinalDamage.Publication var2 = var1.publication;
                if (publicationLive(var2) && producerPublished(var1.producer, var2)) {
                    synchronized (FoodWeaponSourceLedger.class) {
                        FoodWeaponFinalDamage.PreparedBatch var4 = var2.prepared;
                        return !var0.closed
                            && !var2.closed
                            && var2.readiness.matches(var2)
                            && var2.currentStateProducer == null
                            && var2.currentInvocation == var1
                            && !var1.closed
                            && var1.publication == var2
                            && var1.ordinal >= 0
                            && var1.ordinal < var2.producers.size()
                            && var2.producers.get(var1.ordinal) == var1.producer
                            && var1.producer.prepared == var4
                            && var4.committed
                            && var4.originalBinding != null
                            && var2.result.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
                            && var4.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
                            && Thread.holdsLock(FoodWeaponSourceLedger.class)
                            && Thread.currentThread() == var4.thread
                            && var2.server == var4.player.serverLevel().getServer()
                            && var2.server.isSameThread()
                            && clock(var4.player) == var4.at;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean localInvocationLive(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        return var0 != null && var0.invocation != null && var0.invocation.publication.originalContinuation != null
            ? originalInvocationLive(var0)
            : localInvocationReady(var0) && FoodWeaponSourceLedger.ownerWriterContinuation(var0.invocation.publication.writerPublication);
    }

    static boolean matchesAttributeInvocation(FoodSocketAttributePowers.OffenseCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null
            && localInvocationLive(var1)
            && var1.invocation.childCall == null
            && var1.invocation.producer instanceof FoodWeaponFinalDamage.AttributeProducer var2
            && var2.consumerCommit == var0;
    }

    private static boolean originalCleanupIdentity(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        if (!localInvocationLive(var0) || var0.invocation.childCall != null) {
            return false;
        } else if (var0.invocation.producer.originalConsumer != null && var0.invocation.producer.originalConsumer.bound) {
            return originalConsumerApplyLive(var0);
        } else {
            FoodWeaponFinalDamage.Publication var1 = var0.invocation.publication;
            FoodWeaponFinalDamage.GuardResult var2 = var1.guardResult;
            return var2 != null
                && var2.action.capture != null
                && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(
                    var2.action.capture.originalBinding, var1.prepared.originalBinding, var1.prepared.player
                );
        }
    }

    static boolean matchesPackCleanupInvocation(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null
            && originalCleanupIdentity(var1)
            && var1.invocation.producer instanceof FoodWeaponFinalDamage.WolfProducer var2
            && var2.consumerCommit == var0;
    }

    static boolean matchesPassiveHitCleanupInvocation(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null
            && originalCleanupIdentity(var1)
            && var1.invocation.producer instanceof FoodWeaponFinalDamage.PassiveProducer var2
            && var2.consumerCommit == var0;
    }

    static boolean matchesPackInvocation(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null
            && localInvocationLive(var1)
            && var1.invocation.childCall == null
            && var1.invocation.producer instanceof FoodWeaponFinalDamage.WolfProducer var2
            && var2.consumerCommit == var0;
    }

    static boolean matchesPassiveHitInvocation(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null
            && localInvocationLive(var1)
            && var1.invocation.childCall == null
            && var1.invocation.producer instanceof FoodWeaponFinalDamage.PassiveProducer var2
            && var2.consumerCommit == var0;
    }

    static boolean matchesHitPlanInvocation(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && localInvocationLive(var1) && var1.invocation.childCall == null) {
            FoodWeaponFinalDamage.ListedProducer var2 = var1.invocation.producer;
            if (var2 instanceof FoodWeaponFinalDamage.MaterialProducer var4 && var4.consumerCommit == var0
                || var2 instanceof FoodWeaponFinalDamage.EvolutionProducer var3 && var3.consumerCommit == var0) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    static boolean matchesHitPlanSelectionPhase(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && localInvocationReady(var1) && var1.invocation.childCall == null) {
            FoodWeaponFinalDamage.ListedProducer var2 = var1.invocation.producer;
            if (var2 instanceof FoodWeaponFinalDamage.MaterialProducer var4 && var4.consumerCommit == var0
                || var2 instanceof FoodWeaponFinalDamage.EvolutionProducer var3 && var3.consumerCommit == var0) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    static boolean matchesOwnerChildCall(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return var0 != null
            && !var0.closed
            && var0.thread == Thread.currentThread()
            && localInvocationLive(var0.bridge)
            && var0.bridge.invocation.childCall == var0
            && var0.originalBinding == var0.bridge.invocation.publication.prepared.originalBinding
            && matchesOriginalConsumerChild(var0)
            && var0.source != null
            && var0.target != null
            && Float.isFinite(var0.amount)
            && var0.amount > 0.0F;
    }

    static FoodWeaponSourceLedger.OriginalInteractionBinding ownerChildOriginalBinding(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return matchesOwnerChildCall(var0) ? var0.originalBinding : null;
    }

    static DamageSource ownerChildSource(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return matchesOwnerChildCall(var0) ? var0.source : null;
    }

    static LivingEntity ownerChildTarget(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return matchesOwnerChildCall(var0) ? var0.target : null;
    }

    static float ownerChildAmount(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return matchesOwnerChildCall(var0) ? var0.amount : Float.NaN;
    }

    static boolean hurtListedChild(FoodWeaponFinalDamage.ChildHurtBridge var0, LivingEntity var1, DamageSource var2, float var3) {
        if (localInvocationLive(var0)
            && var0.invocation.childCall == null
            && publicationLive(var0.invocation.publication)
            && var1 != null
            && var2 != null
            && Float.isFinite(var3)
            && !(var3 <= 0.0F)) {
            FoodWeaponFinalDamage.ListedProducerInvocation var4 = var0.invocation;
            FoodWeaponFinalDamage.ChildHurtCall var5 = new FoodWeaponFinalDamage.ChildHurtCall(var0, var1, var2, var3);
            var4.childCall = var5;
            if (hasBoundOriginalAttributeConsumer(var0)) {
                try {
                    FoodWeaponFinalDamage.AttributeProducer var6 = (FoodWeaponFinalDamage.AttributeProducer)var4.producer;
                    var5.originalConsumerChild = FoodSocketAttributePowers.originalAttributeChild(var6.consumerCommit, var0, var1, var2, var3);
                } catch (Throwable var32) {
                    var5.originalConsumerChildFailure = var32;
                    revokeExtraAuthority(var4.publication, var32);
                }
            }

            FoodWeaponSourceLedger.FinalListedChildScope var35 = null;

            boolean var7;
            try {
                var35 = FoodWeaponSourceLedger.enterFinalListedChild(var5);
                if (var35 != null) {
                    var5.hurtEntered = true;
                    return FoodWeaponSourceLedger.hurtFinalListedChild(var35);
                }

                var7 = false;
            } finally {
                try {
                    if (var35 != null) {
                        FoodWeaponSourceLedger.leaveFinalListedChild(var35);
                    }
                } finally {
                    var5.closed = true;
                    if (var4.childCall == var5) {
                        var4.childCall = null;
                    }
                }
            }

            return var7;
        } else {
            return false;
        }
    }

    private static boolean nativeReservationReady(PreparedListedAction action, GuardResult guard) {
        if (!Thread.holdsLock(FoodWeaponSourceLedger.class) || action == null || guard == null
            || action.publication != null || !guard.matches(action) || !action.prepared.stableIdentity()) {
            return false;
        }
        EventCapture capture = action.capture;
        PreparedBatch prepared = action.prepared;
        return capture != null && !capture.failed && capture.originalBinding == prepared.originalBinding
            && capture.player == prepared.player && capture.source == prepared.source && capture.target == prepared.target
            && capture.thread == prepared.thread && capture.at == prepared.at
            && capturedNativePlansMatch(capture, action);
    }

    private static boolean capturedNativePlansMatch(EventCapture capture, PreparedListedAction action) {
        int ordinal = 0;
        for (CapturedProducer captured : capture.producers) {
            if (captured.plan() != null) {
                if (ordinal >= action.producers.size()) return false;
                ListedProducer listed = action.producers.get(ordinal++);
                if (listed.prepared() != action.prepared || listed.plan() != captured.plan()) return false;
            }
        }
        return ordinal > 0 && ordinal == action.producers.size();
    }

    static boolean originalAttributeHasNoWrites(Object candidate, Object weapon, Object player, Object source) {
        boolean caller = ORIGINAL_CALLER.walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("quoteTimers"))
            .orElse(false));
        if (!caller || !(candidate instanceof AttributeProducer producer)) return false;
        PreparedBatch prepared = producer.prepared();
        return prepared != null && prepared.committed && prepared.thread == Thread.currentThread()
            && prepared.player == player && prepared.source == source && clock(prepared.player) == prepared.at
            && producer.plan.actualWeapon() == weapon && producer.plan.attempts().isEmpty()
            && FoodSocketAttributePowers.matchesPreparedOffense(producer.consumerPrepared, producer.plan,
                prepared.batch, prepared.admission, prepared.requests, prepared.targets);
    }

    static FoodWeaponBossPassive.HitPlan originalPassivePlan(Object candidate, Object weapon, Object player, Object source) {
        boolean caller = ORIGINAL_CALLER.walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("quoteTimers"))
            .orElse(false));
        if (!caller || !(candidate instanceof PassiveProducer producer)) return null;
        PreparedBatch prepared = producer.prepared();
        return prepared != null && prepared.committed && prepared.thread == Thread.currentThread()
            && prepared.player == player && prepared.source == source && clock(prepared.player) == prepared.at
            && producer.plan.context().actualWeapon() == weapon
            && FoodWeaponBossPassive.matchesPreparedPassiveHit(producer.consumerPrepared, producer.plan,
                prepared.batch, prepared.admission, prepared.requests, prepared.targets) ? producer.plan : null;
    }

    private static boolean reserveListedAction(FoodWeaponFinalDamage.PreparedListedAction var0, FoodWeaponFinalDamage.GuardResult var1) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (!Thread.holdsLock(FoodWeaponSourceLedger.class) || var0 == null || var1 == null || !var1.matches(var0) || var0.publication != null) {
                return false;
            }
        }

        float var14 = Float.intBitsToFloat(var1.acceptedBits);
        FoodWeaponFinalDamage.Publication var3 = new FoodWeaponFinalDamage.Publication(var0, var0.prepared.commit(var14), var14);
        var0.publication = var3;
        FoodWeaponFinalDamage.PreparedBatch var4 = var3.prepared;

        label67: {
            boolean var6;
            try {
                var3.ownerReserveSelectionOpen = true;
                var3.writerPublication = FoodWeaponSourceLedger.openOwnerWriterPublication(
                    var4.originalBinding, var4.batch, var4.admission, var3.result, var3.receipt
                );
                break label67;
            } catch (Throwable var11) {
                retainWriterObservationFailure(var3, null, 0, var11);
                closePublication(var3, null);
                var6 = false;
            } finally {
                var3.ownerReserveSelectionOpen = false;
                var3.ownerReserveSelectionQuerying = false;
            }

            return var6;
        }

        if (publicationLive(var3) && var3.readiness.matches(var3)) {
            return true;
        } else {
            closePublication(var3, null);
            return false;
        }
    }

    private static void finalizeFailedPublication(FoodWeaponFinalDamage.PublishingCleanup var0) {
        if (localPublishingIdentity(var0, FoodWeaponFinalDamage.PublishingPhase.FINALIZING)) {
            if (var0.producer instanceof FoodWeaponFinalDamage.AttributeProducer var1) {
                FoodSocketAttributePowers.finishOffensePublication(var1.consumerPrepared, var0);
            } else if (var0.producer instanceof FoodWeaponFinalDamage.WolfProducer var2) {
                FoodSocketPowers.finishPackPublication(var2.consumerPrepared, var0);
            } else if (var0.producer instanceof FoodWeaponFinalDamage.PassiveProducer var3) {
                FoodWeaponBossPassive.finishPassiveHitPublication(var3.consumerPrepared, var0);
            }
        }
    }

    private static void retainCleanupFailure(Throwable var0, Throwable var1) {
        if (var0 != var1) {
            try {
                var0.addSuppressed(var1);
            } catch (Throwable var3) {
            }
        }
    }

    private static boolean localOwnerWriterPublicationIdentity(
        FoodWeaponFinalDamage.OffenseReceipt var0,
        FoodWeaponSourceLedger.OriginalInteractionBinding var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        FoodWeaponSourceLedger.AttemptBatchCommit var4
    ) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 != null && var1 != null && var2 != null && var3 != null && var4 != null && Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                FoodWeaponFinalDamage.Publication var6 = var0.publication;
                if (var6 != null && !var6.closed && var6.receipt == var0) {
                    FoodWeaponFinalDamage.PreparedBatch var7 = var6.prepared;
                    FoodWeaponFinalDamage.GuardResult var8 = var6.guardResult;
                    FoodWeaponFinalDamage.ReservationReadiness var9 = var6.readiness;
                    if (var7 != null && var8 != null && var9 != null && var8.action != null) {
                        FoodWeaponFinalDamage.PreparedListedAction var10 = var8.action;
                        FoodWeaponFinalDamage.EventCapture var11 = var10.capture;
                        if (!var7.committed
                            || var7.originalBinding != var1
                            || var7.batch != var2
                            || var7.admission != var3
                            || var6.result != var4
                            || var6.server == null
                            || var7.player == null
                            || var7.source == null
                            || var7.target == null
                            || var7.thread != Thread.currentThread()
                            || var8.thread != var7.thread
                            || var8.at != var7.at
                            || var8.originalBinding != var1
                            || var8.player != var7.player
                            || var8.source != var7.source
                            || var8.target != var7.target
                            || var10.publication != var6
                            || var10.prepared != var7
                            || var10.guardResult != var8
                            || var10.producers != var6.producers
                            || var11 == null
                            || var11.failed
                            || var11.originalBinding != var1
                            || var11.player != var7.player
                            || var11.source != var7.source
                            || var11.target != var7.target
                            || var11.thread != var7.thread
                            || var11.at != var7.at
                            || var9.publication != var6
                            || var9.prepared != var7
                            || var9.result != var4
                            || var9.producers != var6.producers
                            || !Float.isFinite(var6.amount)
                            || var6.amount <= 0.0F
                            || var8.acceptedBits != Float.floatToRawIntBits(var6.amount)
                            || var3.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
                            || var4.status() != FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED) {
                            return false;
                        } else if (var6.publishingCleanup != null) {
                            return localPublishingIdentity(var6.publishingCleanup, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
                        } else if (var6.currentStateProducer != null) {
                            return false;
                        } else {
                            FoodWeaponFinalDamage.ListedProducerInvocation var12 = var6.currentInvocation;
                            return var12 == null
                                || !var12.closed
                                    && var12.childCall == null
                                    && var12.publication == var6
                                    && var12.ordinal >= 0
                                    && var12.ordinal < var6.producers.size()
                                    && var6.producers.get(var12.ordinal) == var12.producer
                                    && var12.producer.prepared() == var7;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static boolean localOwnerReserveSelection(
        FoodWeaponFinalDamage.Publication var0, OwnerFieldExpectedAuthority.Prepared var1, FoodWeaponSourceLedger.OwnerWriterPublication var2
    ) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 != null
                && var1 != null
                && var2 != null
                && var1.lineage != null
                && var0.prepared != null
                && Thread.holdsLock(FoodWeaponSourceLedger.class)
                && var0.prepared.thread == Thread.currentThread()
                && var0.ownerReserveSelectionOpen
                && !var0.ownerReserveSelectionInvalid
                && !var0.closed
                && var0.writerPublication == var2
                && var1.issuer == var2
                && var1.lineage.issuer == var2
                && var1.lineage.publication == var0
                && var1.lineage.acceptedCallback == var0.receipt
                && var0.originalContinuation == null
                && var0.originalSequence == null
                && var0.currentStateProducer == null
                && var0.currentInvocation == null
                && var0.publishingCleanup == null
                && var0.incompletePublishingCleanup == null
                && (var0.ownerReservePreparation == null || var0.ownerReservePreparation == var1)
                && FoodWeaponSourceLedger.ownerFieldPrepared(var2) == var1
                && OwnerFieldExpectedAuthority.session(var1) == null) {
                FoodWeaponFinalDamage.PreparedBatch var4 = var0.prepared;
                return localOwnerWriterPublicationIdentity(var0.receipt, var4.originalBinding, var4.batch, var4.admission, var0.result);
            } else {
                return false;
            }
        }
    }

    static boolean acceptsOwnerReserveLeaf(OwnerFieldExpectedAuthority.Prepared var0, Object var1, Object var2, Object var3) {
        if (var0 != null
            && var0.lineage != null
            && var0.lineage.publication instanceof FoodWeaponFinalDamage.Publication var4
            && var0.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var5) {
            try {
                boolean var26 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> {
                            List var1x = var0x.skip(1L).limit(5L).toList();
                            return var1x.size() == 5
                                && ((StackFrame)var1x.get(0)).getDeclaringClass() == FoodWeaponSourceLedger.class
                                && ((StackFrame)var1x.get(0)).getMethodName().equals("ownerNativeLeafAccepted")
                                && ((StackFrame)var1x.get(1)).getDeclaringClass() == OwnerFieldExpectedAuthority.class
                                && ((StackFrame)var1x.get(1)).getMethodName().equals("quoteTimers")
                                && ((StackFrame)var1x.get(2)).getDeclaringClass() == FoodWeaponSourceLedger.class
                                && ((StackFrame)var1x.get(2)).getMethodName().equals("ownerOriginalPublicationAssigned")
                                && ((StackFrame)var1x.get(3)).getDeclaringClass() == FoodWeaponFinalDamage.class
                                && ((StackFrame)var1x.get(3)).getMethodName().contains("ftboss$ownerOriginalPublicationAssigned")
                                && ((StackFrame)var1x.get(4)).getDeclaringClass() == FoodWeaponFinalDamage.class
                                && ((StackFrame)var1x.get(4)).getMethodName().equals("reserveListedAction");
                        }
                    );
                if (!var26 || !localOwnerReserveSelection(var4, var0, var5)) {
                    return false;
                } else if (var4.ownerReserveSelectionQuerying) {
                    var4.ownerReserveSelectionInvalid = true;
                    return false;
                } else {
                    var4.ownerReserveSelectionQuerying = true;

                    try {
                        if (FoodWeaponSourceLedger.ownerWriterPublicationContinuation(var5)
                            && var1 instanceof FoodWeaponFinalDamage.EvolutionProducer var27
                            && var2 instanceof FoodWeaponHitPlan var8
                            && var27.plan == var8
                            && var27.prepared() == var4.prepared
                            && var27.consumerPrepared != null
                            && var27.consumerCommit == null
                            && !((FoodWeaponFinalDamage.ListedProducer)var27).publishAttempted) {
                            int var28 = 0;

                            for (FoodWeaponFinalDamage.ListedProducer var11 : var4.producers) {
                                if (var11 != var27) {
                                    if (var11.plan() == var8) {
                                        return false;
                                    }
                                } else {
                                    var28++;
                                }
                            }

                            synchronized (FoodWeaponSourceLedger.class) {
                                if (var28 != 1
                                    || !FoodWeaponHitPlan.ownerReserveLeafSelected(
                                        var27.consumerPrepared,
                                        var8,
                                        var4.prepared.batch,
                                        var4.prepared.admission,
                                        var4.result,
                                        var3,
                                        var4.prepared.player,
                                        var4.prepared.source,
                                        var4.prepared.at,
                                        var4.prepared.requests,
                                        var4.prepared.targets
                                    )) {
                                    return false;
                                }
                            }

                            if (localOwnerReserveSelection(var4, var0, var5)
                                && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(var5)
                                && localOwnerReserveSelection(var4, var0, var5)) {
                                var4.ownerReservePreparation = var0;
                                return true;
                            }

                            return false;
                        }

                        return false;
                    } finally {
                        var4.ownerReserveSelectionQuerying = false;
                    }
                }
            } catch (Throwable var23) {
                Throwable var25 = var23;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (var4.ownerReserveSelectionOpen
                        && var4.prepared != null
                        && var4.prepared.thread == Thread.currentThread()
                        && Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                        var4.ownerReserveSelectionInvalid = true;
                        if (var4.ownerReserveSelectionFailure == null) {
                            var4.ownerReserveSelectionFailure = var25;
                        }
                    }

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    static boolean matchesOwnerWriterPublication(
        FoodWeaponFinalDamage.OffenseReceipt var0,
        FoodWeaponSourceLedger.OriginalInteractionBinding var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        FoodWeaponSourceLedger.AttemptBatchCommit var4
    ) {
        if (var0 == null || !extraOwnerAuthorityLive(var0.publication)) {
            return false;
        } else if (!localOwnerWriterPublicationIdentity(var0, var1, var2, var3, var4)) {
            return false;
        } else {
            FoodWeaponFinalDamage.Publication var5 = var0.publication;
            FoodWeaponFinalDamage.PreparedBatch var6 = var5.prepared;
            FoodWeaponFinalDamage.ListedProducer var7 = var5.currentStateProducer;
            FoodWeaponFinalDamage.ListedProducerInvocation var8 = var5.currentInvocation;
            FoodWeaponFinalDamage.PublishingCleanup var9 = var5.publishingCleanup;
            return var5.server == var6.player.serverLevel().getServer()
                && var5.server.isSameThread()
                && clock(var6.player) == var6.at
                && localOwnerWriterPublicationIdentity(var0, var1, var2, var3, var4)
                && var5.currentStateProducer == var7
                && var5.currentInvocation == var8
                && var5.publishingCleanup == var9;
        }
    }

    private static boolean localOwnerWriterSelection(FoodWeaponFinalDamage.OffenseReceipt var0, Object var1, int var2, Object var3, Object var4, int var5) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 != null && var1 != null && var3 != null && var4 != null && var2 >= 0 && var5 >= 0 && Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                FoodWeaponFinalDamage.Publication var7 = var0.publication;
                if (var7 != null
                    && !var7.closed
                    && var7.receipt == var0
                    && var7.writerPublication != null
                    && var2 < var7.producers.size()
                    && var7.producers.get(var2) == var1) {
                    FoodWeaponFinalDamage.ListedProducer var8 = var7.producers.get(var2);
                    if (var8.prepared() != var7.prepared) {
                        return false;
                    } else if (var7.currentStateProducer != null) {
                        FoodWeaponFinalDamage.PublishingCleanup var13 = var7.publishingCleanup;
                        return var7.currentStateProducer == var8
                            && var7.currentInvocation == null
                            && var13 != null
                            && var13.producer == var8
                            && var13.ordinal == var2
                            && var5 == 0
                            && var3 == var8.preparedConsumer()
                            && var4 == var3
                            && var13.consumerPrepared == var3
                            && var13.receipt == var0
                            && localPublishingIdentity(var13, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
                    } else {
                        FoodWeaponFinalDamage.ListedProducerInvocation var9 = var7.currentInvocation;
                        if (var7.publishingCleanup != null
                            || var9 == null
                            || var9.closed
                            || var9.childCall != null
                            || var9.publication != var7
                            || var9.producer != var8
                            || var9.ordinal != var2) {
                            return false;
                        } else if (var8 instanceof FoodWeaponFinalDamage.MaterialProducer var15) {
                            return var3 == var15.consumerCommit
                                && FoodWeaponHitPlan.matchesOwnerWriterSelectedLeaf(var15.consumerCommit, var15.consumerPrepared, var0, var4, var5);
                        } else if (var8 instanceof FoodWeaponFinalDamage.EvolutionProducer var14) {
                            return var3 == var14.consumerCommit
                                && FoodWeaponHitPlan.matchesOwnerWriterSelectedLeaf(var14.consumerCommit, var14.consumerPrepared, var0, var4, var5);
                        } else {
                            Object var10 = stateProducerCommit(var8);
                            return var10 != null && var3 == var10 && var4 == var10 && var5 == 1;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    static boolean matchesOwnerWriterSelection(FoodWeaponFinalDamage.OffenseReceipt var0, Object var1, int var2, Object var3, Object var4, int var5) {
        if (matchesOriginalConsumerBindingSelection(var0, var1, var2, var3, var4, var5)) {
            return true;
        } else if (var0 == null || !extraOwnerAuthorityLive(var0.publication)) {
            return false;
        } else if (!localOwnerWriterSelection(var0, var1, var2, var3, var4, var5)) {
            return false;
        } else {
            FoodWeaponFinalDamage.Publication var6 = var0.publication;
            FoodWeaponFinalDamage.PreparedBatch var7 = var6.prepared;
            FoodWeaponFinalDamage.ListedProducer var8 = var6.currentStateProducer;
            FoodWeaponFinalDamage.ListedProducerInvocation var9 = var6.currentInvocation;
            FoodWeaponFinalDamage.PublishingCleanup var10 = var6.publishingCleanup;
            return matchesOwnerWriterPublication(var0, var7.originalBinding, var7.batch, var7.admission, var6.result)
                && var6.currentStateProducer == var8
                && var6.currentInvocation == var9
                && var6.publishingCleanup == var10
                && localOwnerWriterSelection(var0, var1, var2, var3, var4, var5);
        }
    }

    private static boolean selectPublishingWriter(FoodWeaponFinalDamage.PublishingCleanup var0) {
        if (!localPublishingIdentity(var0, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)
            || !FoodWeaponSourceLedger.ownerWriterPublicationContinuation(var0.publication.writerPublication)) {
            return false;
        } else {
            return !(var0.producer instanceof FoodWeaponFinalDamage.MaterialProducer) && !(var0.producer instanceof FoodWeaponFinalDamage.EvolutionProducer)
                ? FoodWeaponSourceLedger.selectOwnerWriterLeaf(
                    var0.publication.writerPublication, var0.producer, var0.ordinal, var0.consumerPrepared, var0.consumerPrepared, 0
                )
                : true;
        }
    }

    private static Object stateProducerCommit(FoodWeaponFinalDamage.ListedProducer var0) {
        if (var0 instanceof FoodWeaponFinalDamage.AttributeProducer var3) {
            return var3.consumerCommit;
        } else if (var0 instanceof FoodWeaponFinalDamage.WolfProducer var2) {
            return var2.consumerCommit;
        } else {
            return var0 instanceof FoodWeaponFinalDamage.PassiveProducer var1 ? var1.consumerCommit : null;
        }
    }

    private static boolean selectStateInvocationWriter(FoodWeaponFinalDamage.ListedProducerInvocation var0) {
        FoodWeaponFinalDamage.Publication var1 = var0.publication;
        FoodWeaponFinalDamage.ListedProducer var2 = var0.producer;
        if (originalPublicationLive(var1) && var1.originalSequence.original.atProducer(var2)) {
            if (!extraOwnerAuthorityLive(var1)) {
                return true;
            } else {
                try {
                    if (!(var2 instanceof FoodWeaponFinalDamage.MaterialProducer) && !(var2 instanceof FoodWeaponFinalDamage.EvolutionProducer)) {
                        Object var5 = stateProducerCommit(var2);
                        if (var5 == null || !FoodWeaponSourceLedger.selectOwnerWriterLeaf(var1.writerPublication, var2, var0.ordinal, var5, var5, 1)) {
                            revokeExtraAuthority(var1, null);
                        }
                    }
                } catch (Throwable var4) {
                    retainWriterObservationFailure(var1, var1.writerPublication, 0, var4);
                }

                return true;
            }
        } else if (!(var2 instanceof FoodWeaponFinalDamage.MaterialProducer) && !(var2 instanceof FoodWeaponFinalDamage.EvolutionProducer)) {
            Object var3 = stateProducerCommit(var2);
            return var3 != null
                    && !var0.closed
                    && var0.childCall == null
                    && var1.currentInvocation == var0
                    && var1.currentStateProducer == null
                    && var1.publishingCleanup == null
                    && publicationLive(var1)
                ? FoodWeaponSourceLedger.selectOwnerWriterLeaf(var1.writerPublication, var2, var0.ordinal, var3, var3, 1)
                    && FoodWeaponSourceLedger.ownerWriterContinuation(var1.writerPublication)
                : false;
        } else {
            return true;
        }
    }

    private static boolean producerObservationComplete(FoodWeaponFinalDamage.ListedProducer var0) {
        if (var0 instanceof FoodWeaponFinalDamage.MaterialProducer var2) {
            return FoodWeaponHitPlan.timerObservationNotKnownIncomplete(var2.consumerCommit);
        } else {
            return var0 instanceof FoodWeaponFinalDamage.EvolutionProducer var1
                ? FoodWeaponHitPlan.timerObservationNotKnownIncomplete(var1.consumerCommit)
                : true;
        }
    }

    static boolean bindOwnerOriginalSequence(FoodWeaponFinalDamage.OffenseReceipt var0, FoodWeaponOriginalSequence.Session var1) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 != null && var1 != null) {
                FoodWeaponFinalDamage.Publication var3 = var0.publication;
                if (!var3.closed
                    && var3.originalSequence == null
                    && var3.originalContinuation == null
                    && var3.currentInvocation == null
                    && var3.currentStateProducer == null
                    && var3.publishingCleanup == null
                    && var3.writerPublication != null
                    && var3.guardResult != null
                    && var3.guardResult.action.capture != null
                    && var1.lineage.publication == var3
                    && var1.lineage.issuer == var3.writerPublication
                    && var1.lineage.acceptedCallback == var0
                    && Thread.holdsLock(FoodWeaponSourceLedger.class)
                    && Thread.currentThread() == var3.prepared.thread
                    && (
                        var3.ownerReservePreparation == null
                            || var1.lineage == var3.ownerReservePreparation.lineage
                                && OwnerFieldExpectedAuthority.session(var3.ownerReservePreparation) == var1
                                && FoodWeaponSourceLedger.ownerFieldPrepared(var3.writerPublication) == var3.ownerReservePreparation
                    )
                    && var3.readiness.matches(var3)
                    && var3.prepared.committed
                    && var3.prepared.originalBinding != null
                    && var3.guardResult.originalBinding == var3.prepared.originalBinding) {
                    FoodWeaponFinalDamage.Publication.OriginalContinuation var4 = new FoodWeaponFinalDamage.Publication.OriginalContinuation(var3, var1);
                    var3.originalSequence = var1;
                    var3.originalContinuation = var4;
                    if (!var1.original.bindAcceptedPublication(var3, var4)) {
                        var3.originalSequence = null;
                        var3.originalContinuation = null;
                        return false;
                    } else {
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

    static boolean matchesOriginalRunAdmission(FoodWeaponOriginalSequence.OriginalRun var0, Object var1, Object var2) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (var1 instanceof FoodWeaponFinalDamage.Publication var4 && var2 instanceof FoodWeaponFinalDamage.Publication.OriginalContinuation var5) {
                return var0 != null
                    && var4.originalContinuation == var5
                    && var5.publication == var4
                    && var5.sequence == var4.originalSequence
                    && var5.sequence.original == var0
                    && var0.lineage == var5.sequence.lineage
                    && var0.lineage.publication == var4
                    && var0.lineage.issuer == var4.writerPublication
                    && var0.lineage.acceptedCallback == var4.receipt
                    && !var5.terminal
                    && !var4.closed
                    && var5.thread == Thread.currentThread()
                    && Thread.holdsLock(FoodWeaponSourceLedger.class)
                    && var5.prepared == var4.prepared
                    && var5.receipt == var4.receipt
                    && var5.result == var4.result
                    && var5.producers == var4.producers
                    && var4.prepared.committed
                    && var4.readiness.matches(var4)
                    && var4.guardResult != null
                    && var4.guardResult.action.publication == var4
                    && var4.guardResult.action.prepared == var4.prepared
                    && var4.guardResult.action.producers == var4.producers
                    && var4.guardResult.action.guardResult == var4.guardResult
                    && var4.guardResult.acceptedBits == var5.acceptedBits
                    && Float.floatToRawIntBits(var4.amount) == var5.acceptedBits
                    && var4.guardResult.originalBinding == var4.prepared.originalBinding
                    && var4.prepared.originalBinding != null;
            }

            return false;
        }
    }

    static boolean matchesOriginalProducerAdmission(FoodWeaponOriginalSequence.OriginalRun var0, Object var1, Object var2, Object var3, int var4) {
        if (!matchesOriginalRunAdmission(var0, var1, var2)) {
            return false;
        } else {
            FoodWeaponFinalDamage.Publication var5 = (FoodWeaponFinalDamage.Publication)var1;
            return var5.originalContinuation.applying
                && var5.currentInvocation == null
                && var5.currentStateProducer == null
                && var5.publishingCleanup == null
                && var5.originalContinuation.admittingProducer == var3
                && var5.originalContinuation.admittingOrdinal == var4
                && var3 != null
                && var4 >= 0
                && var4 < var5.producers.size()
                && var5.producers.get(var4) == var3;
        }
    }

    static boolean matchesOriginalLeafAdmission(FoodWeaponOriginalSequence.OriginalRun var0, Object var1, Object var2, Object var3, Object var4, int var5) {
        if (!matchesOriginalRunAdmission(var0, var1, var2)) {
            return false;
        } else {
            FoodWeaponFinalDamage.Publication var6 = (FoodWeaponFinalDamage.Publication)var1;
            if (!(var3 instanceof FoodWeaponFinalDamage.AttributeProducer)
                && !(var3 instanceof FoodWeaponFinalDamage.WolfProducer)
                && !(var3 instanceof FoodWeaponFinalDamage.PassiveProducer)) {
                FoodWeaponFinalDamage.ListedProducerInvocation var7 = var6.currentInvocation;
                if (var6.originalContinuation.applying
                    && var7 != null
                    && !var7.closed
                    && var7.publication == var6
                    && var7.producer == var3
                    && var7.childCall == null
                    && var6.currentStateProducer == null
                    && var6.publishingCleanup == null) {
                    FoodWeaponHitPlan.FinalEffectsCommit var8 = var3 instanceof FoodWeaponFinalDamage.MaterialProducer var10
                        ? var10.consumerCommit
                        : (var3 instanceof FoodWeaponFinalDamage.EvolutionProducer var9 ? var9.consumerCommit : null);
                    return FoodWeaponHitPlan.matchesOriginalLeafAdmission(var8, var6.originalSequence, var4, var5);
                } else {
                    return false;
                }
            } else {
                return admitOriginalConsumerLeaf(var6, (FoodWeaponFinalDamage.ListedProducer)var3, var4, var5);
            }
        }
    }

    private static boolean bindOriginalProducerCommit(FoodWeaponFinalDamage.Publication var0, FoodWeaponFinalDamage.ListedProducer var1) {
        FoodWeaponHitPlan.FinalEffectsCommit var2 = var1 instanceof FoodWeaponFinalDamage.MaterialProducer var4
            ? var4.consumerCommit
            : (var1 instanceof FoodWeaponFinalDamage.EvolutionProducer var3 ? var3.consumerCommit : null);
        return var2 == null || FoodWeaponHitPlan.bindOriginalSequence(var2, var0.originalSequence, var0.receipt);
    }

    private static boolean enterOriginalProducer(FoodWeaponFinalDamage.Publication var0, FoodWeaponFinalDamage.ListedProducer var1, int var2) {
        FoodWeaponFinalDamage.Publication.OriginalContinuation var3 = var0.originalContinuation;
        if (var3 != null && var3.applying && var3.admittingProducer == null) {
            var3.admittingProducer = var1;
            var3.admittingOrdinal = var2;

            boolean var4;
            try {
                var4 = var0.originalSequence.original.enterProducer(var0, var1, var2);
            } finally {
                var3.admittingProducer = null;
                var3.admittingOrdinal = -1;
            }

            return var4;
        } else {
            return false;
        }
    }

    private static FoodWeaponFinalDamage.OriginalConsumerBinding beginOriginalConsumerBinding(FoodWeaponFinalDamage.PublishingCleanup var0) {
        if (localPublishingIdentity(var0, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)
            && originalPublicationLive(var0.publication)
            && var0.publication.originalSequence.original.atProducer(var0.producer)
            && var0.producer.originalConsumer == null) {
            FoodWeaponFinalDamage.OriginalConsumerBinding var1 = new FoodWeaponFinalDamage.OriginalConsumerBinding(var0);
            var0.producer.originalConsumer = var1;
            return var1;
        } else {
            return null;
        }
    }

    private static boolean originalConsumerBindingLive(FoodWeaponFinalDamage.OriginalConsumerBinding var0) {
        if (var0 != null && !var0.retired) {
            FoodWeaponFinalDamage.PublishingCleanup var1 = var0.cleanup;
            FoodWeaponFinalDamage.Publication var2 = var1.publication;
            return originalPublicationLive(var2)
                && var1.producer.originalConsumer == var0
                && var0.sequence == var2.originalSequence
                && var1.receipt == var2.receipt
                && var1.prepared == var2.prepared
                && var1.consumerPrepared == var1.producer.preparedConsumer()
                && var1.ordinal >= 0
                && var1.ordinal < var2.producers.size()
                && var2.producers.get(var1.ordinal) == var1.producer
                && var0.sequence.original.atProducer(var1.producer);
        } else {
            return false;
        }
    }

    private static boolean calledFromG8(Class<?> var0, String var1) {
        try {
            return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var2 -> var2.filter(
                                var0xx -> var0xx.getDeclaringClass().getNestHost() != FoodWeaponFinalDamage.class
                                        && var0xx.getDeclaringClass().getNestHost() != FoodWeaponOriginalSequence.class
                            )
                            .findFirst()
                            .map(var2x -> var2x.getDeclaringClass() == var0 && var2x.getMethodName().equals(var1))
                            .orElse(false)
                );
        } catch (Throwable var3) {
            return false;
        }
    }

    private static void originalConsumerBindingFailed(FoodWeaponFinalDamage.PublishingCleanup var0, Throwable var1) {
        FoodWeaponFinalDamage.OriginalConsumerBinding var2 = var0.producer.originalConsumer;
        if (var2 != null && var2.cleanup == var0 && var2.observationFailure == null) {
            var2.observationFailure = var1;
        }

        revokeExtraAuthority(var0.publication, var1);
    }

    private static void beginOriginalConsumerApply(FoodWeaponFinalDamage.ListedProducerInvocation var0, FoodWeaponFinalDamage.ChildHurtBridge var1, Object var2) {
        FoodWeaponFinalDamage.OriginalConsumerBinding var3 = var0.producer.originalConsumer;
        if (originalConsumerBindingLive(var3)
            && var3.bound
            && var3.publishExited
            && var3.publishMatched
            && !var3.consumerRetired
            && !var3.applying
            && !var3.binding
            && var3.commit == var2
            && var2 != null
            && originalInvocationLive(var1)
            && var1.invocation == var0
            && var0.childCall == null) {
            var3.bridge = var1;
            var3.applying = true;
        }
    }

    private static void finishOriginalConsumerApply(FoodWeaponFinalDamage.ListedProducerInvocation var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        FoodWeaponFinalDamage.OriginalConsumerBinding var2 = var0.producer.originalConsumer;
        if (var2 != null && var2.bridge == var1 && var2.cleanup.publication == var0.publication) {
            var2.applying = false;
            var2.bridge = null;
        }
    }

    private static boolean originalConsumerApplyLive(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        if (!originalInvocationLive(var0)) {
            return false;
        } else {
            FoodWeaponFinalDamage.OriginalConsumerBinding var1 = var0.invocation.producer.originalConsumer;
            return originalConsumerBindingLive(var1)
                && var1.bound
                && var1.applying
                && var1.bridge == var0
                && var1.commit == stateProducerCommit(var0.invocation.producer)
                && var1.sequence.original.atLeaf(var1.leaf);
        }
    }

    private static boolean matchesOriginalConsumerBindingSelection(
        FoodWeaponFinalDamage.OffenseReceipt var0, Object var1, int var2, Object var3, Object var4, int var5
    ) {
        if (var0 != null && var1 instanceof FoodWeaponFinalDamage.ListedProducer var6) {
            FoodWeaponFinalDamage.OriginalConsumerBinding var7 = var6.originalConsumer;
            return originalConsumerBindingLive(var7)
                    && var7.binding
                    && !var7.bound
                    && var7.cleanup.receipt == var0
                    && var7.cleanup.ordinal == var2
                    && var3 == var7.cleanup.consumerPrepared
                    && var4 == var3
                    && var5 == 0
                    && localPublishingIdentity(var7.cleanup, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)
                ? calledFromG8(FoodWeaponConsumerSequence.class, "bind")
                : false;
        } else {
            return false;
        }
    }

    private static boolean admitOriginalConsumerLeaf(FoodWeaponFinalDamage.Publication var0, FoodWeaponFinalDamage.ListedProducer var1, Object var2, int var3) {
        FoodWeaponFinalDamage.OriginalConsumerBinding var4 = var1.originalConsumer;
        if (!originalConsumerBindingLive(var4) || var4.cleanup.publication != var0 || !(var2 instanceof FoodWeaponConsumerSequence.Accepted)) {
            return false;
        } else if (var3 != 0) {
            FoodWeaponFinalDamage.ListedProducerInvocation var5 = var0.currentInvocation;
            return var3 == 1
                && var4.bound
                && var4.publishExited
                && var4.publishMatched
                && !var4.consumerRetired
                && !var4.binding
                && var4.applying
                && var4.leaf == var2
                && var5 != null
                && !var5.closed
                && var5.childCall == null
                && var5.publication == var0
                && var5.producer == var1
                && var5.ordinal == var4.cleanup.ordinal
                && var4.bridge != null
                && !var4.bridge.closed
                && var4.bridge.invocation == var5
                && var4.commit != null
                && var4.commit == stateProducerCommit(var1)
                && var0.currentStateProducer == null
                && var0.publishingCleanup == null
                && calledFromG8(FoodWeaponConsumerSequence.Accepted.class, "enter");
        } else if (var4.binding
            && !var4.bound
            && var4.leaf == null
            && localPublishingIdentity(var4.cleanup, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)
            && calledFromG8(FoodWeaponConsumerSequence.class, "bind")) {
            var4.leaf = var2;
            return true;
        } else {
            return false;
        }
    }

    private static boolean hasBoundOriginalAttributeConsumer(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        return var0 != null
            && var0.invocation != null
            && var0.invocation.producer instanceof FoodWeaponFinalDamage.AttributeProducer
            && var0.invocation.producer.originalConsumer != null
            && var0.invocation.producer.originalConsumer.bound;
    }

    private static boolean matchesOriginalConsumerChild(FoodWeaponFinalDamage.ChildHurtCall var0) {
        if (!hasBoundOriginalAttributeConsumer(var0.bridge)) {
            return var0.originalConsumerChild == null;
        } else {
            FoodWeaponFinalDamage.OriginalConsumerBinding var1 = var0.bridge.invocation.producer.originalConsumer;
            return var0.originalConsumerChild != null
                && var0.originalConsumerChildFailure == null
                && originalConsumerApplyLive(var0.bridge)
                && var0.originalBinding == var1.cleanup.originalBinding;
        }
    }

    static FoodWeaponConsumerSequence.OriginalChild originalConsumerChild(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return matchesAcceptedOriginalChild(var0) ? var0.originalConsumerChild : null;
    }

    private static boolean originalPublishingExitIdentity(FoodWeaponFinalDamage.OriginalConsumerBinding var0, FoodWeaponFinalDamage.PublishingPhase var1) {
        if (var0 != null && !var0.consumerRetired && var0.leaf != null) {
            FoodWeaponFinalDamage.PublishingCleanup var2 = var0.cleanup;
            FoodWeaponFinalDamage.Publication var3 = var2.publication;
            FoodWeaponFinalDamage.Publication.OriginalContinuation var4 = var3.originalContinuation;
            return var4 != null
                && var2.producer.originalConsumer == var0
                && var0.sequence == var4.sequence
                && !var0.binding
                && !var0.applying
                && var0.bridge == null
                && localPublishingIdentity(var2, var1)
                && matchesOriginalRunAdmission(var0.sequence.original, var3, var4);
        } else {
            return false;
        }
    }

    private static void finishOriginalConsumerPublication(FoodWeaponFinalDamage.OriginalConsumerBinding var0, Object var1, boolean var2, Throwable var3) {
        if (var0 != null) {
            var0.commit = var1;
            var0.publishExited = true;
            var0.publishMatched = var2;
            if (var0.publicationOriginalFailure == null) {
                var0.publicationOriginalFailure = var3;
            }

            if (originalPublishingExitIdentity(var0, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING)) {
                if (var0.sequence.original.publishingLeafState(var0.cleanup.producer, var0.cleanup.ordinal, var0.leaf) == 1) {
                    try {
                        ((FoodWeaponConsumerSequence.Accepted)var0.leaf).published(var0.cleanup.consumerPrepared, var1);
                    } catch (Throwable var5) {
                        var0.publicationCloseFailure = var5;
                        originalConsumerBindingFailed(var0.cleanup, var5);
                    }
                }
            }
        }
    }

    private static void retireOriginalConsumerPublication(FoodWeaponFinalDamage.PublishingCleanup var0) {
        FoodWeaponFinalDamage.OriginalConsumerBinding var1 = var0.producer.originalConsumer;
        if (var1 != null && var1.cleanup == var0) {
            try {
                if (!originalPublishingExitIdentity(var1, FoodWeaponFinalDamage.PublishingPhase.FINALIZING)) {
                    return;
                }

                if (var1.sequence.original.publishingLeafState(var0.producer, var0.ordinal, var1.leaf) >= 0) {
                    ((FoodWeaponConsumerSequence.Accepted)var1.leaf).retired(var0.consumerPrepared);
                    var1.consumerRetired = true;
                    return;
                }
            } catch (Throwable var6) {
                var1.publicationCloseFailure = var6;
                originalConsumerBindingFailed(var0, var6);
                return;
            } finally {
                var1.retired = true;
            }
        }
    }

    private static boolean originalPublicationLive(FoodWeaponFinalDamage.Publication var0) {
        return var0 != null && var0.originalContinuation != null && var0.originalContinuation.live();
    }

    private static boolean extraOwnerAuthorityLive(FoodWeaponFinalDamage.Publication var0) {
        return var0 != null
            && !var0.extraAuthorityRevoked
            && (var0.originalContinuation == null || var0.originalContinuation.live())
            && (var0.originalSequence == null || var0.originalSequence.extraAuthorityRetained());
    }

    private static boolean originalReceiptLive(FoodWeaponFinalDamage.Publication var0) {
        if (!originalPublicationLive(var0) || !var0.originalContinuation.applying) {
            return false;
        } else if (var0.currentStateProducer != null) {
            FoodWeaponFinalDamage.PublishingCleanup var2 = var0.publishingCleanup;
            return var2 != null
                && var2.phase == FoodWeaponFinalDamage.PublishingPhase.PUBLISHING
                && var0.originalSequence.original.atProducer(var0.currentStateProducer)
                && localPublishingIdentity(var2, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING);
        } else {
            FoodWeaponFinalDamage.ListedProducerInvocation var1 = var0.currentInvocation;
            return var1 != null
                && !var1.closed
                && var1.childCall == null
                && var1.publication == var0
                && var1.producer.prepared == var0.prepared
                && var1.ordinal >= 0
                && var1.ordinal < var0.producers.size()
                && var0.producers.get(var1.ordinal) == var1.producer
                && var0.originalSequence.original.atProducer(var1.producer);
        }
    }

    private static void revokeExtraAuthority(FoodWeaponFinalDamage.Publication var0, Throwable var1) {
        if (var0 != null) {
            var0.extraAuthorityRevoked = true;
            FoodWeaponOriginalSequence.Session var2 = var0.originalSequence;
            if (var2 != null) {
                var2.interferenceObserved(var2.lineage.ownerSelection, var2.lineage.generation, var1);
            }
        }
    }

    private static boolean originalInvocationLive(FoodWeaponFinalDamage.ChildHurtBridge var0) {
        if (var0 != null && !var0.closed && var0.invocation != null && !var0.invocation.closed) {
            FoodWeaponFinalDamage.ListedProducerInvocation var1 = var0.invocation;
            FoodWeaponFinalDamage.Publication var2 = var1.publication;
            return originalPublicationLive(var2)
                && var2.originalContinuation.applying
                && var2.currentInvocation == var1
                && var2.currentStateProducer == null
                && var2.publishingCleanup == null
                && var1.ordinal >= 0
                && var1.ordinal < var2.producers.size()
                && var2.producers.get(var1.ordinal) == var1.producer
                && var1.producer.prepared == var2.prepared
                && var2.originalSequence.original.atProducer(var1.producer)
                && producerPublished(var1.producer, var2);
        } else {
            return false;
        }
    }

    static boolean matchesOriginalHitPlanLeaf(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, Object var2, int var3) {
        if (var0 != null
            && originalInvocationLive(var1)
            && var1.invocation.childCall == null
            && var1.invocation.publication.originalSequence.original.atLeaf(var2)) {
            boolean var10000;
            label41: {
                FoodWeaponFinalDamage.ListedProducer var4 = var1.invocation.producer;
                if (var4 instanceof FoodWeaponFinalDamage.MaterialProducer var7 && var7.consumerCommit == var0
                    || var4 instanceof FoodWeaponFinalDamage.EvolutionProducer var6 && var6.consumerCommit == var0) {
                    var10000 = true;
                    break label41;
                }

                var10000 = false;
            }

            boolean var5 = var10000;
            return var5 && FoodWeaponHitPlan.matchesOriginalSelectedFinalLeaf(var0, var2, var3);
        } else {
            return false;
        }
    }

    static boolean matchesOriginalFinalPreparation(FoodWeaponHitPlan.PreparedFinalEffects var0, FoodWeaponFinalDamage.OffenseReceipt var1) {
        if (var0 != null && var1 != null && originalReceiptLive(var1.publication)) {
            FoodWeaponFinalDamage.ListedProducer var2 = var1.publication.currentStateProducer;
            if (var2 instanceof FoodWeaponFinalDamage.MaterialProducer var4 && var4.consumerPrepared == var0
                || var2 instanceof FoodWeaponFinalDamage.EvolutionProducer var3 && var3.consumerPrepared == var0) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    static FoodWeaponOriginalSequence.Session originalHitPlanSequence(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && originalInvocationLive(var1) && var1.invocation.childCall == null) {
            boolean var10000;
            label35: {
                FoodWeaponFinalDamage.ListedProducer var2 = var1.invocation.producer;
                if (var2 instanceof FoodWeaponFinalDamage.MaterialProducer var5 && var5.consumerCommit == var0
                    || var2 instanceof FoodWeaponFinalDamage.EvolutionProducer var4 && var4.consumerCommit == var0) {
                    var10000 = true;
                    break label35;
                }

                var10000 = false;
            }

            boolean var3 = var10000;
            return var3 ? var1.invocation.publication.originalSequence : null;
        } else {
            return null;
        }
    }

    static boolean matchesAcceptedOriginalOffense(FoodSocketAttributePowers.PreparedOffense var0, FoodWeaponFinalDamage.OffenseReceipt var1) {
        return var1 != null
            && originalReceiptLive(var1.publication)
            && var1.publication.currentStateProducer instanceof FoodWeaponFinalDamage.AttributeProducer var2
            && var2.consumerPrepared == var0;
    }

    static boolean matchesAcceptedOriginalPack(FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.OffenseReceipt var1) {
        return var1 != null
            && originalReceiptLive(var1.publication)
            && var1.publication.currentStateProducer instanceof FoodWeaponFinalDamage.WolfProducer var2
            && var2.consumerPrepared == var0;
    }

    static boolean matchesAcceptedOriginalPassive(FoodWeaponBossPassive.PreparedHit var0, FoodWeaponFinalDamage.OffenseReceipt var1) {
        return var1 != null
            && originalReceiptLive(var1.publication)
            && var1.publication.currentStateProducer instanceof FoodWeaponFinalDamage.PassiveProducer var2
            && var2.consumerPrepared == var0;
    }

    static boolean matchesAcceptedOriginalChild(FoodWeaponFinalDamage.ChildHurtCall var0) {
        return var0 != null
            && !var0.closed
            && var0.thread == Thread.currentThread()
            && originalInvocationLive(var0.bridge)
            && var0.bridge.invocation.childCall == var0
            && var0.originalBinding == var0.bridge.invocation.publication.prepared.originalBinding
            && matchesOriginalConsumerChild(var0)
            && var0.source != null
            && var0.target != null
            && Float.isFinite(var0.amount)
            && var0.amount > 0.0F;
    }

    static FoodWeaponSourceLedger.OwnerWriterPublication packPublishingWriter(
        FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.OffenseReceipt var1, FoodWeaponFinalDamage.PublishingCleanup var2
    ) {
        if (matchesPackPublishing(var0, var1, var2)
            && receiptLive(var2.publication)
            && FoodWeaponSourceLedger.ownerWriterContinuation(var2.publication.writerPublication)) {
            return localPublishingIdentity(var2, FoodWeaponFinalDamage.PublishingPhase.PUBLISHING) ? var2.publication.writerPublication : null;
        } else {
            return null;
        }
    }

    static FoodWeaponSourceLedger.OwnerWriterPublication selectHitPlanWriterLeaf(
        FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, Object var2, int var3
    ) {
        if (matchesOriginalHitPlanLeaf(var0, var1, var2, var3)) {
            FoodWeaponFinalDamage.Publication var7 = var1.invocation.publication;
            if (extraOwnerAuthorityLive(var7)) {
                try {
                    if (!FoodWeaponSourceLedger.selectOwnerWriterLeaf(
                        var7.writerPublication, var1.invocation.producer, var1.invocation.ordinal, var0, var2, var3
                    )) {
                        revokeExtraAuthority(var7, null);
                    }
                } catch (Throwable var6) {
                    retainWriterObservationFailure(var7, var7.writerPublication, 0, var6);
                }
            }

            return var7.writerPublication;
        } else if (matchesHitPlanSelectionPhase(var0, var1) && FoodWeaponHitPlan.matchesSelectedFinalLeaf(var0, var2, var3)) {
            FoodWeaponFinalDamage.ListedProducerInvocation var4 = var1.invocation;
            FoodWeaponFinalDamage.Publication var5 = var4.publication;
            if (publicationLive(var5) && FoodWeaponSourceLedger.selectOwnerWriterLeaf(var5.writerPublication, var4.producer, var4.ordinal, var0, var2, var3)) {
                return matchesHitPlanInvocation(var0, var1) && FoodWeaponHitPlan.matchesSelectedFinalLeaf(var0, var2, var3) ? var5.writerPublication : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static void retainWriterObservationFailure(
        FoodWeaponFinalDamage.Publication var0, FoodWeaponSourceLedger.OwnerWriterPublication var1, int var2, Throwable var3
    ) {
        if (var0 != null) {
            if (originalPublicationLive(var0)) {
                revokeExtraAuthority(var0, var3);
            } else {
                var0.closed = true;
            }

            if (var1 != null && var0.incompleteWriterPublication == null) {
                var0.incompleteWriterPublication = var1;
            }

            switch (var2) {
                case 0:
                    if (var0.writerPlanningObservationFailure == null) {
                        var0.writerPlanningObservationFailure = var3;
                    }
                    break;
                case 1:
                    if (var0.writerNormalObservationFailure == null) {
                        var0.writerNormalObservationFailure = var3;
                    }
                    break;
                case 2:
                    if (var0.writerExceptionObservationFailure == null) {
                        var0.writerExceptionObservationFailure = var3;
                    }
                    break;
                case 3:
                    if (var0.writerCloseObservationFailure == null) {
                        var0.writerCloseObservationFailure = var3;
                    }
            }

            if (var1 != null) {
                try {
                    FoodWeaponSourceLedger.ownerPublicationObservationFailed(var1, var2, var3);
                } catch (Throwable var5) {
                    if (var0.writerFailureRecordingFailure == null) {
                        var0.writerFailureRecordingFailure = var5;
                    }
                }
            }
        }
    }

    private static void closePublication(FoodWeaponFinalDamage.Publication var0, Throwable var1) {
        if (var0 != null) {
            for (FoodWeaponFinalDamage.ListedProducer var3 : var0.producers) {
                FoodWeaponFinalDamage.OriginalConsumerBinding var4 = var3.originalConsumer;
                if (var4 != null && var4.cleanup.publication == var0) {
                    var4.retired = true;
                }
            }

            FoodWeaponSourceLedger.OwnerWriterPublication var7 = var0.writerPublication;
            var0.writerPublication = null;
            var0.closed = true;
            if (var1 != null && var0.writerOriginalFailure == null) {
                var0.writerOriginalFailure = var1;
            }

            if (var7 != null) {
                if (var1 != null) {
                    try {
                        FoodWeaponSourceLedger.ownerPublicationOriginalFailed(var7, var1);
                    } catch (Throwable var6) {
                        retainWriterObservationFailure(var0, var7, 2, var6);
                    }
                }

                try {
                    FoodWeaponSourceLedger.closeOwnerWriterPublication(var7);
                } catch (Throwable var5) {
                    var0.writerCleanupFailure = var5;
                    retainWriterObservationFailure(var0, var7, 3, var5);
                }
            }
        }
    }

    private static boolean publishListedProducer(FoodWeaponFinalDamage.Publication var0, FoodWeaponFinalDamage.ListedProducer var1, int var2) {
        if (var0 != null
            && var1 != null
            && !var0.closed
            && !var1.publishAttempted
            && var0.publishingCleanup == null
            && var0.currentStateProducer == null
            && var0.currentInvocation == null
            && var1.prepared() == var0.prepared
            && var2 >= 0
            && var2 < var0.producers.size()
            && var0.producers.get(var2) == var1
            && var0.guardResult != null
            && var0.guardResult.action.capture != null) {
            var1.publishAttempted = true;
            FoodWeaponFinalDamage.PublishingCleanup var3 = new FoodWeaponFinalDamage.PublishingCleanup(var0, var1, var2);
            var0.publishingCleanup = var3;
            var0.currentStateProducer = var1;
            boolean var4 = false;
            Throwable var5 = null;

            try {
                boolean var6 = originalPublicationLive(var0);
                if (var6) {
                    try {
                        if (extraOwnerAuthorityLive(var0) && !selectPublishingWriter(var3)) {
                            revokeExtraAuthority(var0, null);
                        }
                    } catch (Throwable var50) {
                        retainWriterObservationFailure(var0, var0.writerPublication, 0, var50);
                    }
                } else if (!selectPublishingWriter(var3)) {
                    return false;
                }

                return var4 = var1.publish(var0.amount, var0.receipt, var3) && producerPublished(var1, var0);
            } catch (Throwable var53) {
                var5 = var53;
                throw var53;
            } finally {
                try {
                    if (!var4) {
                        var3.phase = FoodWeaponFinalDamage.PublishingPhase.FINALIZING;

                        try {
                            finalizeFailedPublication(var3);
                        } catch (Throwable var51) {
                            var3.cleanupFailure = var51;
                            var0.incompletePublishingCleanup = var3;
                            if (var5 != null) {
                                retainCleanupFailure(var5, var51);
                            }
                        }
                    }
                } finally {
                    if (!var4) {
                        retireOriginalConsumerPublication(var3);
                    }

                    var3.phase = FoodWeaponFinalDamage.PublishingPhase.CLOSED;
                    if (var0.publishingCleanup == var3) {
                        var0.publishingCleanup = null;
                    }

                    if (var0.currentStateProducer == var1) {
                        var0.currentStateProducer = null;
                    }
                }
            }
        } else {
            return false;
        }
    }

    private static void applyListedEffects(FoodWeaponFinalDamage.PreparedListedAction var0, float var1) {
        FoodWeaponFinalDamage.Publication var2;
        FoodWeaponFinalDamage.Publication.OriginalContinuation var3;
        boolean var4;
        synchronized (FoodWeaponSourceLedger.class) {
            var2 = var0 == null ? null : var0.publication;
            if (var2 == null) {
                return;
            }

            var3 = var2.originalContinuation;
            var4 = var3 != null;
            if (var4 && (var3.applying || !var3.live())) {
                return;
            }
        }

        Throwable var119 = null;

        try {
            synchronized (FoodWeaponSourceLedger.class) {
                if (Float.compare(var1, var2.amount) != 0 || !var2.readiness.matches(var2)) {
                    return;
                }

                if (var4) {
                    var3.applying = true;
                }
            }

            for (int var121 = 0; var121 < var2.producers.size(); var121++) {
                boolean var122 = publicationLive(var2);
                FoodWeaponFinalDamage.ListedProducer var8;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (!var122 || var2.closed || !var2.readiness.matches(var2)) {
                        return;
                    }

                    var8 = var2.producers.get(var121);
                    if (var4 && !enterOriginalProducer(var2, var8, var121)) {
                        return;
                    }
                }

                try {
                    if (var8 instanceof AttributeProducer || var8 instanceof WolfProducer || var8 instanceof PassiveProducer) {
                        synchronized (FoodWeaponSourceLedger.class) {
                            if (!publishListedProducer(var2, var8, var121)) {
                                return;
                            }
                        }
                    } else if (!publishListedProducer(var2, var8, var121)) {
                        return;
                    }

                    if (var4 && !bindOriginalProducerCommit(var2, var8)) {
                        return;
                    }

                    if (!publicationLive(var2)
                        && !(var8 instanceof FoodWeaponFinalDamage.WolfProducer)
                        && !(var8 instanceof FoodWeaponFinalDamage.PassiveProducer)) {
                        return;
                    }

                    FoodWeaponFinalDamage.ListedProducerInvocation var123;
                    synchronized (FoodWeaponSourceLedger.class) {
                        var123 = new FoodWeaponFinalDamage.ListedProducerInvocation(var2, var8, var121);
                        var2.currentInvocation = var123;
                    }

                    try {
                        if (!selectStateInvocationWriter(var123)
                            && !(var8 instanceof FoodWeaponFinalDamage.WolfProducer)
                            && !(var8 instanceof FoodWeaponFinalDamage.PassiveProducer)) {
                            return;
                        }

                        if (var8 instanceof AttributeProducer || var8 instanceof WolfProducer || var8 instanceof PassiveProducer) {
                            synchronized (FoodWeaponSourceLedger.class) {
                                var8.apply(var123);
                            }
                        } else {
                            var8.apply(var123);
                        }
                    } finally {
                        synchronized (FoodWeaponSourceLedger.class) {
                            var123.closed = true;
                            if (var2.currentInvocation == var123) {
                                var2.currentInvocation = null;
                            }
                        }
                    }

                    boolean var124 = var4 || publicationLive(var2) && producerPublished(var8, var2);
                    synchronized (FoodWeaponSourceLedger.class) {
                        if (var4) {
                            if (!producerObservationComplete(var8)) {
                                revokeExtraAuthority(var2, null);
                            }
                        } else if (!var124 || !producerObservationComplete(var8)) {
                            return;
                        }
                    }
                } finally {
                    synchronized (FoodWeaponSourceLedger.class) {
                        if (var4) {
                            var2.originalSequence.original.leaveProducer(var8);
                        }
                    }
                }
            }
        } catch (Throwable var116) {
            Throwable var6 = var116;
            var119 = var116;
            synchronized (FoodWeaponSourceLedger.class) {
                if (var4) {
                    var2.originalSequence.original.originalThrown(var6);
                }
            }

            throw var116;
        } finally {
            synchronized (FoodWeaponSourceLedger.class) {
                if (var4) {
                    var3.applying = false;
                    var3.terminal = true;
                    var2.originalSequence.original.terminal();
                }
            }

            closePublication(var2, var119);
        }
    }

    private static FoodWeaponFinalDamage.PreparedListedAction collectListedProducers(
        FoodWeaponFinalDamage.PreparedBatch var0,
        FoodWeaponHitPlan var1,
        FoodWeaponHitPlan var2,
        FoodSocketAttributePowers.OffensePlan var3,
        FoodSocketPowers.PackPlan var4,
        FoodWeaponBossPassive.HitPlan var5,
        List<FoodWeaponFinalDamage.ProducerFamily> var6
    ) {
        if (var0 != null
            && var0.stable()
            && var6 != null
            && var6.size() == FoodWeaponFinalDamage.ProducerFamily.values().length
            && Set.copyOf(var6).size() == FoodWeaponFinalDamage.ProducerFamily.values().length) {
            ArrayList var7 = new ArrayList();
            ArrayList var8 = new ArrayList();
            ArrayList var9 = new ArrayList();

            for (FoodWeaponFinalDamage.ProducerFamily var11 : var6) {
                switch (var11) {
                    case MATERIAL:
                        if (var1 == null) {
                            break;
                        }

                        FoodWeaponHitPlan.PreparedFinalEffects var18 = FoodWeaponHitPlan.prepareFinalEffects(
                            var1, var0.batch, var0.admission, var0.requests, var0.targets, var0.player, var0.source
                        );
                        if (var18 == null) {
                            return null;
                        }

                        var7.add(new FoodWeaponFinalDamage.MaterialProducer(var0, var1, var18));

                        for (FoodWeaponHitPlan.Attempt var20 : var1.attempts()) {
                            var8.add(new FoodWeaponSourceLedger.AttemptRequest(var20.effect(), var20.chance()));
                        }

                        var9.addAll(var1.targets());
                        break;
                    case EVOLUTION:
                        if (var2 == null) {
                            break;
                        }

                        FoodWeaponHitPlan.PreparedFinalEffects var17 = FoodWeaponHitPlan.prepareFinalEffects(
                            var2, var0.batch, var0.admission, var0.requests, var0.targets, var0.player, var0.source
                        );
                        if (var17 == null) {
                            return null;
                        }

                        var7.add(new FoodWeaponFinalDamage.EvolutionProducer(var0, var2, var17));

                        for (FoodWeaponHitPlan.Attempt var14 : var2.attempts()) {
                            var8.add(new FoodWeaponSourceLedger.AttemptRequest(var14.effect(), var14.chance()));
                        }

                        var9.addAll(var2.targets());
                        break;
                    case ATTRIBUTE:
                        if (var3 != null) {
                            FoodSocketAttributePowers.PreparedOffense var16 = FoodSocketAttributePowers.prepareOffense(
                                var3, var0.batch, var0.admission, var0.requests, var0.targets
                            );
                            if (var16 == null) {
                                return null;
                            }

                            var7.add(new FoodWeaponFinalDamage.AttributeProducer(var0, var3, var16));
                            var8.addAll(var3.attempts());
                        }
                        break;
                    case WOLF:
                        if (var4 != null) {
                            FoodSocketPowers.PreparedPack var15 = FoodSocketPowers.preparePack(var4, var0.batch, var0.admission, var0.requests, var0.targets);
                            if (!FoodSocketPowers.matchesPreparedPack(var15, var4, var0.batch, var0.admission, var0.requests, var0.targets)) {
                                return null;
                            }

                            var7.add(new FoodWeaponFinalDamage.WolfProducer(var0, var4, var15));
                            var8.addAll(var4.attempts());
                            var9.addAll(var4.targets());
                        }
                        break;
                    case PASSIVE:
                        if (var5 != null) {
                            FoodWeaponBossPassive.PreparedHit var12 = FoodWeaponBossPassive.preparePassiveHit(
                                var5, var0.batch, var0.admission, var0.requests, var0.targets
                            );
                            if (!FoodWeaponBossPassive.matchesPreparedPassiveHit(var12, var5, var0.batch, var0.admission, var0.requests, var0.targets)) {
                                return null;
                            }

                            var7.add(new FoodWeaponFinalDamage.PassiveProducer(var0, var5, var12));
                            var8.addAll(var5.attempts());
                            var9.addAll(var5.targets());
                        }
                }
            }

            return var0.requests.equals(var8) && var0.targets.equals(var9) ? new FoodWeaponFinalDamage.PreparedListedAction(var0, var7) : null;
        } else {
            return null;
        }
    }

    private static FoodWeaponFinalDamage.PreparedBatch admitBatch(
        CompoundTag var0,
        ServerPlayer var1,
        DamageSource var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        return admitTargetBatch(var0, var1, var2, null, var3, var4);
    }

    private static boolean sourceMatchesTarget(CompoundTag var0, ServerPlayer var1, DamageSource var2, LivingEntity var3) {
        if (var0 == null || var1 == null || var2 == null) {
            return false;
        } else {
            return !var0.hasUUID("NativeHurtId")
                ? var2.getClass() == DamageSource.class
                : var3 != null && FoodWeaponSourceLedger.matchesNativeHurt(var2, var1, var3, var0);
        }
    }

    private static FoodWeaponFinalDamage.PreparedBatch admitTargetBatch(
        CompoundTag var0,
        ServerPlayer var1,
        DamageSource var2,
        LivingEntity var3,
        List<FoodWeaponSourceLedger.AttemptRequest> var4,
        List<FoodWeaponSourceLedger.TargetRequest> var5
    ) {
        synchronized (FoodWeaponSourceLedger.class) {
            if (!Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                return null;
            }
        }

        if (var2 != null
            && var1 != null
            && var1.getClass() == ServerPlayer.class
            && var1.serverLevel().getServer().isSameThread()
            && FoodWeaponSourceLedger.validReadOnly(var0, var1)
            && var2.getEntity() == var1
            && FoodWeaponSourceLedger.resolveReadOnly(var2, var1).equals(var0)
            && sourceMatchesTarget(var0, var1, var2, var3)) {
            FoodWeaponSourceLedger.AttemptBatchQuote var12 = FoodWeaponSourceLedger.quoteAttemptBatch(var0, var1, var4, var5);
            long var7 = clock(var1);
            FoodWeaponSourceLedger.AttemptBatchAdmission var9 = FoodWeaponSourceLedger.admitAttemptBatch(var0, var1, var12, var4, var5, var7);
            if (var9.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY) {
                return null;
            } else {
                FoodWeaponFinalDamage.PreparedBatch var10 = new FoodWeaponFinalDamage.PreparedBatch(var0, var1, var2, var3, var12, var9, var4, var5);
                return var10.stable() ? var10 : null;
            }
        } else {
            return null;
        }
    }

    private static boolean defenseReceiverReady() {
        return false;
    }

    private static long clock(ServerPlayer var0) {
        return var0.serverLevel().getServer().overworld().getGameTime();
    }

    private static String foundationFingerprint(ServerPlayer var0, ItemStack var1) {
        return FourWeaponFoundationAdapter.applies(var1) ? new FoundationApprovedRules().fingerprint(var0, var1) : "LEGACY";
    }

    private static FoodWeaponFinalDamage.PreparedDefense prepareDefense(ServerPlayer var0, DamageSource var1, float var2) {
        if (var0.getClass() == ServerPlayer.class
            && var0.serverLevel().getServer().isSameThread()
            && !var0.level().isClientSide()
            && var0.isAlive()
            && !var0.isSpectator()
            && incomingSourceAllowed(var0, var1)
            && !FoodWeaponBondHits.secondary(var1)
            && !var1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            FoodSocketAttributePowers.DefenseQuote var3 = FoodSocketAttributePowers.quoteDefense(var0, var1, var2);
            FoodSocketAttributePowers.DefensePlan var4 = var3.plan();
            if (var3.rejection() == FoodSocketAttributePowers.DefenseRejection.NONE
                && var4 != null
                && var4.input() == var2
                && Float.isFinite(var4.amount())
                && !(var4.amount() < 0.0F)
                && Float.isFinite(var4.blocked())
                && !(var4.blocked() <= 0.0F)
                && !(var4.blocked() > var2)
                && var2 - var4.blocked() == var4.amount()
                && var3.amount() == var4.amount()) {
                ItemStack var5 = var4.actualWeapon();
                InteractionHand var6 = var4.originalHand();
                if (!var4.matchesContext(var0, var1, var5, var6, var4.quotedAt())) {
                    return null;
                } else {
                    String var7 = foundationFingerprint(var0, var5);
                    if (var7 != null && !var7.isBlank()) {
                        FoodWeaponFinalDamage.PreparedDefense var8 = new FoodWeaponFinalDamage.PreparedDefense(var4, var0, var1, var5, var6, var7);
                        return incomingSourceMatches(var8, var0, var1) ? var8 : null;
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean incomingSourceAllowed(ServerPlayer var0, DamageSource var1) {
        if (var0 != null && var1 != null) {
            if (var1.getEntity() instanceof ServerPlayer var2) {
                CompoundTag var4 = FoodWeaponSourceLedger.resolveReadOnly(var1, var2);
                if (var4.hasUUID("NativeHurtId")) {
                    return FoodWeaponSourceLedger.matchesNativeHurt(var1, var2, var0, var4);
                }
            }

            return var1.getClass() == DamageSource.class;
        } else {
            return false;
        }
    }

    private static boolean incomingSourceMatches(FoodWeaponFinalDamage.PreparedDefense var0, ServerPlayer var1, DamageSource var2) {
        if (var0 == null || var0.defender != var1 || var0.source != var2) {
            return false;
        } else {
            return var0.incomingNativeSnapshot.isEmpty()
                ? incomingSourceAllowed(var1, var2) && var2.getClass() == DamageSource.class
                : var0.incomingAttacker != null
                    && var2.getEntity() == var0.incomingAttacker
                    && FoodWeaponSourceLedger.resolveReadOnly(var2, var0.incomingAttacker).equals(var0.incomingNativeSnapshot)
                    && FoodWeaponSourceLedger.matchesNativeHurt(var2, var0.incomingAttacker, var1, var0.incomingNativeSnapshot);
        }
    }

    private static void commitDefense(FoodWeaponFinalDamage.PreparedDefense var0, float var1) {
        FoodWeaponFinalDamage.DefenseReceipt var2 = var0.receipt;
        if (var2 != null && var2.matches(var0.plan, var0.defender, var0.source, var0.weapon, var0.hand, var0.at, var1)) {
            FoodSocketAttributePowers.commitDefense(var0.plan, var1, var2);
        }
    }

    public static float finish(LivingEntity var0, DamageSource var1, float var2) {
        if (!originalNativeCaller(
            "beforeHealth",
            "ftboss$finalDamageGuard",
            "(Lnet/neoforged/neoforge/common/damagesource/DamageContainer;Lnet/minecraft/world/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V",
            "(Lnet/neoforged/neoforge/common/damagesource/DamageContainer;Lnet/minecraft/world/entity/LivingEntity;)F"
        )) {
            return FoodWeaponGuardKernel.finish(var0, var1, var2);
        } else {
            FoodWeaponFinalDamage.PreparedListedAction var3 = null;
            Throwable var4 = null;

            float var18;
            try {
                var3 = takeGuardWaiter(var0, var1);
                float var5 = FoodWeaponGuardKernel.finish(var0, var1, var2);
                boolean var6 = var3 != null && Float.isFinite(var5) && var5 > 0.0F && var3.prepared.stable();
                boolean var7 = false;
                if (var6) {
                    synchronized (FoodWeaponSourceLedger.class) {
                        if (var3.prepared.stableIdentity()) {
                            FoodWeaponFinalDamage.GuardResult var9 = new FoodWeaponFinalDamage.GuardResult(var3, var0, var1, var2, var5);
                            var3.guardResult = var9;
                            var7 = nativeReservationReady(var3, var9);
                        }
                    }
                }

                if (var7) {
                    var7 = reserveListedAction(var3, var3.guardResult);
                }

                if (var7) {
                    applyListedEffects(var3, var5);
                }

                var18 = var5;
            } catch (Throwable var16) {
                var4 = var16;
                throw var16;
            } finally {
                if (var3 != null) {
                    closePublication(var3.publication, var4);
                }
            }

            return var18;
        }
    }

    private static final class AttributeProducer extends FoodWeaponFinalDamage.ListedProducer {
        private final FoodSocketAttributePowers.OffensePlan plan;
        private final FoodSocketAttributePowers.PreparedOffense consumerPrepared;
        private FoodSocketAttributePowers.OffenseCommit consumerCommit;

        private AttributeProducer(
            FoodWeaponFinalDamage.PreparedBatch var1, FoodSocketAttributePowers.OffensePlan var2, FoodSocketAttributePowers.PreparedOffense var3
        ) {
            super(var1);
            this.plan = var2;
            this.consumerPrepared = var3;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        @Override
        boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            FoodWeaponFinalDamage.PreparedBatch var4 = this.prepared();
            if (!FoodSocketAttributePowers.matchesPreparedOffense(this.consumerPrepared, this.plan, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return false;
            } else {
                FoodWeaponFinalDamage.OriginalConsumerBinding var5 = null;

                try {
                    var5 = FoodWeaponFinalDamage.beginOriginalConsumerBinding(var3);
                    if (var5 != null) {
                        var5.bound = FoodSocketAttributePowers.bindOriginalSequence(this.consumerPrepared, var5.sequence, this, var3.ordinal, var2, var1, var3);
                        if (!var5.bound) {
                            FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, null);
                        }
                    }
                } catch (Throwable var20) {
                    FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, var20);
                } finally {
                    if (var5 != null) {
                        var5.binding = false;
                    }
                }

                Throwable var6 = null;
                boolean var7 = false;

                boolean var8;
                try {
                    this.consumerCommit = FoodSocketAttributePowers.commitPreparedOffense(this.consumerPrepared, var1, var2, var3);
                    var7 = FoodSocketAttributePowers.matchesOffenseCommit(
                        this.consumerCommit, this.consumerPrepared, this.plan, var2, var1, var4.batch, var4.admission, var4.requests, var4.targets
                    );
                    var8 = var7;
                } catch (Throwable var18) {
                    var6 = var18;
                    throw var18;
                } finally {
                    FoodWeaponFinalDamage.finishOriginalConsumerPublication(var5, this.consumerCommit, var7, var6);
                }

                return var8;
            }
        }

        @Override
        void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            FoodWeaponFinalDamage.ChildHurtBridge var2 = new FoodWeaponFinalDamage.ChildHurtBridge(var1);

            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(var1, var2, this.consumerCommit);
                FoodSocketAttributePowers.applyOffense(this.consumerCommit, var2);
            } finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(var1, var2);
                var2.closed = true;
            }
        }
    }

    private static record CapturedProducer(FoodWeaponFinalDamage.ProducerFamily family, Object plan, int ordinal, int inputBits, int outputBits) {
    }

    static final class ChildHurtBridge {
        private final FoodWeaponFinalDamage.ListedProducerInvocation invocation;
        private boolean closed;

        private ChildHurtBridge(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            this.invocation = var1;
        }
    }

    static final class ChildHurtCall {
        private final FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final LivingEntity target;
        private final DamageSource source;
        private final float amount;
        private final Thread thread;
        private boolean hurtEntered;
        private boolean closed;
        private FoodWeaponConsumerSequence.OriginalChild originalConsumerChild;
        private Throwable originalConsumerChildFailure;

        private ChildHurtCall(FoodWeaponFinalDamage.ChildHurtBridge var1, LivingEntity var2, DamageSource var3, float var4) {
            this.bridge = var1;
            this.originalBinding = var1.invocation.publication.prepared.originalBinding;
            this.target = var2;
            this.source = var3;
            this.amount = var4;
            this.thread = Thread.currentThread();
        }
    }

    public static enum DefenseReason {
        POSITIVE_DEFENSE,
        SHIELD_ONLY_ZERO;
    }

    public static final class DefenseReceipt {
        private final FoodWeaponFinalDamage.PreparedDefense prepared;
        private final FoodSocketAttributePowers.DefensePlan plan;
        private final ServerPlayer defender;
        private final DamageSource source;
        private final ItemStack weapon;
        private final InteractionHand hand;
        private final long at;
        private final ResourceKey<Level> dimension;
        private final Thread thread;
        private final CompoundTag weaponState;
        private final String materials;
        private final String foundation;
        private final Resolution recipe;
        private final float amount;
        private final FoodWeaponFinalDamage.DefenseReason reason;

        private DefenseReceipt(FoodWeaponFinalDamage.PreparedDefense var1, float var2, FoodWeaponFinalDamage.DefenseReason var3) {
            this.prepared = var1;
            this.plan = var1.plan;
            this.defender = var1.defender;
            this.source = var1.source;
            this.weapon = var1.weapon;
            this.hand = var1.hand;
            this.at = var1.at;
            this.dimension = var1.dimension;
            this.thread = var1.thread;
            this.weaponState = var1.weaponState.copy();
            this.materials = var1.materials;
            this.foundation = var1.foundation;
            this.recipe = var1.recipe;
            this.amount = var2;
            this.reason = var3;
        }

        public FoodWeaponFinalDamage.DefenseReason reason() {
            return this.reason;
        }

        public float amount() {
            return this.amount;
        }

        public boolean matches(
            FoodSocketAttributePowers.DefensePlan var1, ServerPlayer var2, DamageSource var3, ItemStack var4, InteractionHand var5, long var6, float var8
        ) {
            if (var1 == this.plan
                && var2 == this.defender
                && var3 == this.source
                && var4 == this.weapon
                && var5 == this.hand
                && var6 == this.at
                && Thread.currentThread() == this.thread
                && Float.isFinite(var8)
                && Float.compare(var8, this.amount) == 0
                && this.defender.serverLevel().getServer().isSameThread()
                && FoodWeaponFinalDamage.clock(this.defender) == this.at
                && this.defender.level().dimension().equals(this.dimension)
                && this.defender.isAlive()
                && !this.defender.isSpectator()
                && this.defender.getItemInHand(this.hand) == this.weapon
                && !this.weapon.isEmpty()
                && this.weapon.getCount() == 1
                && !FoodWeaponLifecycle.isBroken(this.weapon)
                && !this.source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !FoodWeaponBondHits.secondary(this.source)
                && FoodWeaponFinalDamage.incomingSourceMatches(this.prepared, this.defender, this.source)
                && this.plan.matchesContext(var2, var3, var4, var5, var6)) {
                if (this.weapon.saveOptional(this.defender.registryAccess()).equals(this.weaponState)
                    && FoodWeaponSourceLedger.materialFingerprint(this.weapon).equals(this.materials)
                    && Objects.equals(FoodWeaponFinalDamage.foundationFingerprint(this.defender, this.weapon), this.foundation)
                    && FoodSocketAttributePowers.inspect(this.weapon).equals(this.recipe)) {
                    return this.reason == FoodWeaponFinalDamage.DefenseReason.SHIELD_ONLY_ZERO
                        ? this.amount == 0.0F && this.plan.amount() == 0.0F && this.plan.blocked() > 0.0F && this.plan.blocked() == this.plan.input()
                        : this.amount > 0.0F && this.plan.amount() > 0.0F && this.amount <= this.plan.amount() && this.plan.blocked() > 0.0F;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static final class EventCapture {
        private final BeforeMitigation event;
        private final ServerPlayer player;
        private final DamageSource source;
        private final LivingEntity target;
        private final CompoundTag snapshot;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final Thread thread;
        private final long at;
        private final ArrayList<FoodWeaponFinalDamage.CapturedProducer> producers = new ArrayList<>();
        private boolean failed;
        private boolean closed;

        private EventCapture(BeforeMitigation var1, ServerPlayer var2, CompoundTag var3, FoodWeaponSourceLedger.OriginalInteractionBinding var4) {
            this.event = var1;
            this.player = var2;
            this.source = var1.getSource();
            this.target = var1.getEntity();
            this.snapshot = var3.copy();
            this.originalBinding = var4;
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(var2);
        }

        private boolean live() {
            if (this.closed
                || this.failed
                || this.thread != Thread.currentThread()
                || !this.player.serverLevel().getServer().isSameThread()
                || FoodWeaponFinalDamage.clock(this.player) != this.at
                || this.source != this.event.getSource()
                || this.target != this.event.getEntity()
                || this.source.getEntity() != this.player
                || !this.player.isAlive()
                || this.player.isSpectator()
                || this.event.isCanceled()
                || !Float.isFinite(this.event.getAmount())
                || this.event.getAmount() <= 0.0F) {
                return false;
            } else {
                return !FoodWeaponSourceLedger.matchesOriginalInteraction(this.originalBinding, this.snapshot, this.player)
                    ? false
                    : FoodWeaponSourceLedger.resolveReadOnly(this.source, this.player).equals(this.snapshot)
                        && FoodWeaponFinalDamage.sourceMatchesTarget(this.snapshot, this.player, this.source, this.target);
            }
        }

        private boolean append(FoodWeaponFinalDamage.ProducerFamily var1, Object var2, float var3, float var4) {
            if (this.live() && !this.producers.stream().anyMatch(var1x -> var1x.family() == var1)) {
                this.producers
                    .add(
                        new FoodWeaponFinalDamage.CapturedProducer(
                            var1, var2, this.producers.size(), Float.floatToRawIntBits(var3), Float.floatToRawIntBits(var4)
                        )
                    );
                if (!Float.isFinite(var4) || var4 <= 0.0F) {
                    this.failed = true;
                }

                return true;
            } else {
                this.failed = true;
                return false;
            }
        }
    }

    private static final class EvolutionProducer extends FoodWeaponFinalDamage.ListedProducer {
        private final FoodWeaponHitPlan plan;
        private final FoodWeaponHitPlan.PreparedFinalEffects consumerPrepared;
        private FoodWeaponHitPlan.FinalEffectsCommit consumerCommit;

        private EvolutionProducer(FoodWeaponFinalDamage.PreparedBatch var1, FoodWeaponHitPlan var2, FoodWeaponHitPlan.PreparedFinalEffects var3) {
            super(var1);
            this.plan = var2;
            this.consumerPrepared = var3;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        @Override
        boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            this.consumerCommit = FoodWeaponHitPlan.commitFinalState(this.consumerPrepared, var1, var2);
            return this.consumerCommit != null;
        }

        @Override
        void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            FoodWeaponFinalDamage.ChildHurtBridge var2 = new FoodWeaponFinalDamage.ChildHurtBridge(var1);

            try {
                FoodWeaponHitPlan.applyFinalEffects(this.consumerCommit, var2);
            } finally {
                var2.closed = true;
            }
        }
    }

    private static final class GuardResult {
        private final FoodWeaponFinalDamage.PreparedListedAction action;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final ServerPlayer player;
        private final DamageSource source;
        private final LivingEntity target;
        private final int inputBits;
        private final int acceptedBits;
        private final Thread thread;
        private final long at;

        private GuardResult(FoodWeaponFinalDamage.PreparedListedAction var1, LivingEntity var2, DamageSource var3, float var4, float var5) {
            this.action = var1;
            this.originalBinding = var1.prepared.originalBinding;
            this.player = var1.prepared.player;
            this.source = var3;
            this.target = var2;
            this.inputBits = Float.floatToRawIntBits(var4);
            this.acceptedBits = Float.floatToRawIntBits(var5);
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(this.player);
        }

        private boolean matches(FoodWeaponFinalDamage.PreparedListedAction var1) {
            return this.action == var1
                && var1.guardResult == this
                && this.originalBinding == var1.prepared.originalBinding
                && this.player == var1.prepared.player
                && this.source == var1.prepared.source
                && this.target == var1.prepared.target
                && this.thread == Thread.currentThread()
                && this.at == var1.prepared.at
                && FoodWeaponFinalDamage.clock(this.player) == this.at
                && Float.isFinite(Float.intBitsToFloat(this.acceptedBits))
                && Float.intBitsToFloat(this.acceptedBits) > 0.0F;
        }
    }

    private abstract static sealed class ListedProducer
        permits FoodWeaponFinalDamage.AttributeProducer,
        FoodWeaponFinalDamage.WolfProducer,
        FoodWeaponFinalDamage.PassiveProducer,
        FoodWeaponFinalDamage.MaterialProducer,
        FoodWeaponFinalDamage.EvolutionProducer {
        private final FoodWeaponFinalDamage.PreparedBatch prepared;
        private boolean publishAttempted;
        private FoodWeaponFinalDamage.OriginalConsumerBinding originalConsumer;

        private ListedProducer(FoodWeaponFinalDamage.PreparedBatch var1) {
            this.prepared = var1;
        }

        final FoodWeaponFinalDamage.PreparedBatch prepared() {
            return this.prepared;
        }

        abstract Object plan();

        abstract Object preparedConsumer();

        abstract boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3);

        abstract void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1);
    }

    private static final class ListedProducerInvocation {
        private final FoodWeaponFinalDamage.Publication publication;
        private final FoodWeaponFinalDamage.ListedProducer producer;
        private final int ordinal;
        private FoodWeaponFinalDamage.ChildHurtCall childCall;
        private boolean closed;

        private ListedProducerInvocation(FoodWeaponFinalDamage.Publication var1, FoodWeaponFinalDamage.ListedProducer var2, int var3) {
            this.publication = var1;
            this.producer = var2;
            this.ordinal = var3;
        }
    }

    private static final class MaterialProducer extends FoodWeaponFinalDamage.ListedProducer {
        private final FoodWeaponHitPlan plan;
        private final FoodWeaponHitPlan.PreparedFinalEffects consumerPrepared;
        private FoodWeaponHitPlan.FinalEffectsCommit consumerCommit;

        private MaterialProducer(FoodWeaponFinalDamage.PreparedBatch var1, FoodWeaponHitPlan var2, FoodWeaponHitPlan.PreparedFinalEffects var3) {
            super(var1);
            this.plan = var2;
            this.consumerPrepared = var3;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        @Override
        boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            this.consumerCommit = FoodWeaponHitPlan.commitFinalState(this.consumerPrepared, var1, var2);
            return this.consumerCommit != null;
        }

        @Override
        void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            FoodWeaponFinalDamage.ChildHurtBridge var2 = new FoodWeaponFinalDamage.ChildHurtBridge(var1);

            try {
                FoodWeaponHitPlan.applyFinalEffects(this.consumerCommit, var2);
            } finally {
                var2.closed = true;
            }
        }
    }

    public static final class OffenseReceipt {
        private final FoodWeaponFinalDamage.Publication publication;

        private OffenseReceipt(FoodWeaponFinalDamage.Publication var1) {
            this.publication = var1;
        }

        public boolean matches(Object var1, ServerPlayer var2, DamageSource var3, long var4, float var6) {
            FoodWeaponFinalDamage.PreparedBatch var7 = this.publication.prepared;
            FoodWeaponFinalDamage.ListedProducer var8 = this.publication.currentStateProducer != null
                ? this.publication.currentStateProducer
                : (this.publication.currentInvocation == null ? null : this.publication.currentInvocation.producer);
            return FoodWeaponFinalDamage.receiptLive(this.publication)
                && var8 != null
                && var8.plan() == var1
                && var2 == var7.player
                && var3 == var7.source
                && var4 == var7.at
                && Float.compare(var6, this.publication.amount) == 0;
        }

        public boolean matchesBatch(
            FoodWeaponSourceLedger.AttemptBatchQuote var1,
            FoodWeaponSourceLedger.AttemptBatchAdmission var2,
            List<FoodWeaponSourceLedger.AttemptRequest> var3,
            List<FoodWeaponSourceLedger.TargetRequest> var4
        ) {
            FoodWeaponFinalDamage.PreparedBatch var5 = this.publication.prepared;
            return FoodWeaponFinalDamage.receiptLive(this.publication)
                && var1 == var5.batch
                && var2 == var5.admission
                && var3 != null
                && var4 != null
                && var5.requests.equals(var3)
                && var5.targets.equals(var4);
        }

        public Optional<FoodWeaponSourceLedger.AttemptResult> attempt(FoodWeaponSourceLedger.AttemptDomain var1) {
            return FoodWeaponFinalDamage.receiptLive(this.publication) ? this.publication.result.attempt(var1) : Optional.empty();
        }

        public Optional<FoodWeaponSourceLedger.TargetResult> target(UUID var1, FoodWeaponSourceLedger.TargetEffectDomain var2) {
            return FoodWeaponFinalDamage.receiptLive(this.publication) ? this.publication.result.target(var1, var2) : Optional.empty();
        }
    }

    private static final class OriginalConsumerBinding {
        private final FoodWeaponFinalDamage.PublishingCleanup cleanup;
        private final FoodWeaponOriginalSequence.Session sequence;
        private Object leaf;
        private Object commit;
        private FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private boolean binding = true;
        private boolean bound;
        private boolean applying;
        private boolean retired;
        private Throwable observationFailure;
        private boolean publishExited;
        private boolean publishMatched;
        private boolean consumerRetired;
        private Throwable publicationOriginalFailure;
        private Throwable publicationCloseFailure;

        private OriginalConsumerBinding(FoodWeaponFinalDamage.PublishingCleanup var1) {
            this.cleanup = var1;
            this.sequence = var1.publication.originalSequence;
        }
    }

    private static final class PassiveProducer extends FoodWeaponFinalDamage.ListedProducer {
        private final FoodWeaponBossPassive.HitPlan plan;
        private final FoodWeaponBossPassive.PreparedHit consumerPrepared;
        private FoodWeaponBossPassive.HitCommit consumerCommit;

        private PassiveProducer(FoodWeaponFinalDamage.PreparedBatch var1, FoodWeaponBossPassive.HitPlan var2, FoodWeaponBossPassive.PreparedHit var3) {
            super(var1);
            this.plan = var2;
            this.consumerPrepared = var3;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        @Override
        boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            FoodWeaponFinalDamage.PreparedBatch var4 = this.prepared();
            if (!FoodWeaponBossPassive.matchesPreparedPassiveHit(this.consumerPrepared, this.plan, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return false;
            } else {
                FoodWeaponFinalDamage.OriginalConsumerBinding var5 = null;

                try {
                    var5 = FoodWeaponFinalDamage.beginOriginalConsumerBinding(var3);
                    if (var5 != null) {
                        var5.bound = FoodWeaponBossPassive.bindOriginalSequence(this.consumerPrepared, var5.sequence, this, var3.ordinal, var2, var1, var3);
                        if (!var5.bound) {
                            FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, null);
                        }
                    }
                } catch (Throwable var20) {
                    FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, var20);
                } finally {
                    if (var5 != null) {
                        var5.binding = false;
                    }
                }

                Throwable var6 = null;
                boolean var7 = false;

                boolean var8;
                try {
                    this.consumerCommit = FoodWeaponBossPassive.commitPassiveHit(this.consumerPrepared, var1, var2, var3);
                    var7 = FoodWeaponBossPassive.matchesPassiveHitCommit(
                        this.consumerCommit, this.consumerPrepared, this.plan, var2, var1, var4.batch, var4.admission, var4.requests, var4.targets
                    );
                    var8 = var7;
                } catch (Throwable var18) {
                    var6 = var18;
                    throw var18;
                } finally {
                    FoodWeaponFinalDamage.finishOriginalConsumerPublication(var5, this.consumerCommit, var7, var6);
                }

                return var8;
            }
        }

        @Override
        void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            FoodWeaponFinalDamage.ChildHurtBridge var2 = new FoodWeaponFinalDamage.ChildHurtBridge(var1);

            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(var1, var2, this.consumerCommit);
                FoodWeaponBossPassive.applyPassiveHit(this.consumerCommit, var2);
            } finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(var1, var2);
                var2.closed = true;
            }
        }
    }

    private static final class PreparedBatch {
        private final CompoundTag snapshot;
        private final ServerPlayer player;
        private final DamageSource source;
        private final LivingEntity target;
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;
        private final Thread thread;
        private final long at;
        private boolean committed;

        private PreparedBatch(
            CompoundTag var1,
            ServerPlayer var2,
            DamageSource var3,
            LivingEntity var4,
            FoodWeaponSourceLedger.AttemptBatchQuote var5,
            FoodWeaponSourceLedger.AttemptBatchAdmission var6,
            List<FoodWeaponSourceLedger.AttemptRequest> var7,
            List<FoodWeaponSourceLedger.TargetRequest> var8
        ) {
            this.snapshot = var1.copy();
            this.player = var2;
            this.source = var3;
            this.target = var4;
            this.batch = var5;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(var5);
            this.admission = var6;
            this.requests = List.copyOf(var7);
            this.targets = List.copyOf(var8);
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(var2);
        }

        private boolean stable() {
            if (!this.committed
                && this.originalBinding != null
                && Thread.currentThread() == this.thread
                && this.player.getClass() == ServerPlayer.class
                && this.player.serverLevel().getServer().isSameThread()
                && FoodWeaponFinalDamage.clock(this.player) == this.at
                && FoodWeaponFinalDamage.sourceMatchesTarget(this.snapshot, this.player, this.source, this.target)
                && FoodWeaponSourceLedger.matchesAttemptRequests(this.batch, this.requests, this.targets)) {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (!Thread.holdsLock(FoodWeaponSourceLedger.class)
                        || this.committed
                        || this.originalBinding == null
                        || Thread.currentThread() != this.thread) {
                        return false;
                    }
                }

                if (!FoodWeaponSourceLedger.matchesOriginalInteraction(this.originalBinding, this.snapshot, this.player)) {
                    return false;
                } else {
                    return !FoodWeaponSourceLedger.resolveReadOnly(this.source, this.player).equals(this.snapshot)
                        ? false
                        : FoodWeaponSourceLedger.matchesAttemptAdmission(this.snapshot, this.player, this.batch, this.admission, this.at);
                }
            } else {
                return false;
            }
        }

        private boolean stableIdentity() {
            return !this.committed
                && this.originalBinding != null
                && Thread.currentThread() == this.thread
                && this.player.getClass() == ServerPlayer.class
                && this.player.serverLevel().getServer().isSameThread()
                && FoodWeaponFinalDamage.clock(this.player) == this.at
                && FoodWeaponSourceLedger.matchesAttemptRequests(this.batch, this.requests, this.targets)
                && this.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY;
        }

        private FoodWeaponSourceLedger.AttemptBatchCommit commit(float var1) {
            if (this.stable() && Float.isFinite(var1) && !(var1 <= 0.0F)) {
                this.committed = true;
                FoodWeaponSourceLedger.AttemptBatchCommit var2 = FoodWeaponSourceLedger.commitAdmittedAttemptBatch(
                    this.snapshot, this.player, this.batch, this.admission, var1, false, false, this.at
                );
                if (var2.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
                    && FoodWeaponSourceLedger.matchesAttemptBatch(this.snapshot, this.player, this.batch, var2, this.at)) {
                    return var2;
                } else {
                    throw new IllegalStateException("Owner result does not belong to the original complete action batch");
                }
            } else {
                throw new IllegalStateException("Stable genuine Owner admission changed before positive publication");
            }
        }
    }

    private static final class PreparedDefense {
        private final ServerPlayer incomingAttacker;
        private final CompoundTag incomingNativeSnapshot;
        private final FoodSocketAttributePowers.DefensePlan plan;
        private final ServerPlayer defender;
        private final DamageSource source;
        private final ItemStack weapon;
        private final InteractionHand hand;
        private final long at;
        private final ResourceKey<Level> dimension;
        private final Thread thread;
        private final CompoundTag weaponState;
        private final String materials;
        private final String foundation;
        private final Resolution recipe;
        private FoodWeaponFinalDamage.DefenseReceipt receipt;

        private PreparedDefense(
            FoodSocketAttributePowers.DefensePlan var1, ServerPlayer var2, DamageSource var3, ItemStack var4, InteractionHand var5, String var6
        ) {
            this.incomingAttacker = var3.getEntity() instanceof ServerPlayer var7 ? var7 : null;
            CompoundTag var9 = this.incomingAttacker == null ? new CompoundTag() : FoodWeaponSourceLedger.resolveReadOnly(var3, this.incomingAttacker);
            this.incomingNativeSnapshot = var9.hasUUID("NativeHurtId") ? var9.copy() : new CompoundTag();
            this.plan = var1;
            this.defender = var2;
            this.source = var3;
            this.weapon = var4;
            this.hand = var5;
            this.at = var1.quotedAt();
            this.dimension = var2.level().dimension();
            this.thread = Thread.currentThread();
            this.weaponState = (CompoundTag)var4.saveOptional(var2.registryAccess());
            this.materials = FoodWeaponSourceLedger.materialFingerprint(var4);
            this.foundation = var6;
            this.recipe = FoodSocketAttributePowers.inspect(var4);
        }
    }

    private static final class PreparedListedAction {
        private final FoodWeaponFinalDamage.PreparedBatch prepared;
        private final List<FoodWeaponFinalDamage.ListedProducer> producers;
        private FoodWeaponFinalDamage.Publication publication;
        private FoodWeaponFinalDamage.EventCapture capture;
        private FoodWeaponFinalDamage.GuardResult guardResult;

        private PreparedListedAction(FoodWeaponFinalDamage.PreparedBatch var1, List<FoodWeaponFinalDamage.ListedProducer> var2) {
            this.prepared = var1;
            this.producers = List.copyOf(var2);
        }
    }

    private static enum ProducerFamily {
        MATERIAL,
        EVOLUTION,
        ATTRIBUTE,
        WOLF,
        PASSIVE;
    }

    private static final class Publication {
        private FoodWeaponFinalDamage.Publication.OriginalContinuation originalContinuation;
        private boolean extraAuthorityRevoked;
        private FoodWeaponOriginalSequence.Session originalSequence;
        private final FoodWeaponFinalDamage.PreparedBatch prepared;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final float amount;
        private final List<FoodWeaponFinalDamage.ListedProducer> producers;
        private final MinecraftServer server;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private FoodWeaponFinalDamage.ListedProducer currentStateProducer;
        private FoodWeaponFinalDamage.ListedProducerInvocation currentInvocation;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup incompletePublishingCleanup;
        private FoodWeaponSourceLedger.OwnerWriterPublication writerPublication;
        private boolean ownerReserveSelectionOpen;
        private boolean ownerReserveSelectionQuerying;
        private boolean ownerReserveSelectionInvalid;
        private Throwable ownerReserveSelectionFailure;
        private OwnerFieldExpectedAuthority.Prepared ownerReservePreparation;
        private FoodWeaponSourceLedger.OwnerWriterPublication incompleteWriterPublication;
        private Throwable writerCleanupFailure;
        private Throwable writerPlanningObservationFailure;
        private Throwable writerNormalObservationFailure;
        private Throwable writerExceptionObservationFailure;
        private Throwable writerCloseObservationFailure;
        private Throwable writerFailureRecordingFailure;
        private Throwable writerOriginalFailure;
        private final FoodWeaponFinalDamage.ReservationReadiness readiness;
        private final FoodWeaponFinalDamage.GuardResult guardResult;
        private boolean closed;

        private Publication(FoodWeaponFinalDamage.PreparedListedAction var1, FoodWeaponSourceLedger.AttemptBatchCommit var2, float var3) {
            this.prepared = var1.prepared;
            this.result = var2;
            this.amount = var3;
            this.producers = var1.producers;
            this.server = this.prepared.player.serverLevel().getServer();
            this.receipt = new FoodWeaponFinalDamage.OffenseReceipt(this);
            this.guardResult = var1.guardResult;
            this.readiness = new FoodWeaponFinalDamage.ReservationReadiness(this);
        }

        private static final class OriginalContinuation {
            private final FoodWeaponFinalDamage.Publication publication;
            private final FoodWeaponFinalDamage.PreparedBatch prepared;
            private final FoodWeaponFinalDamage.OffenseReceipt receipt;
            private final FoodWeaponSourceLedger.AttemptBatchCommit result;
            private final List<FoodWeaponFinalDamage.ListedProducer> producers;
            private final FoodWeaponOriginalSequence.Session sequence;
            private final Thread thread;
            private final int acceptedBits;
            private boolean applying;
            private Object admittingProducer;
            private int admittingOrdinal = -1;
            private boolean terminal;

            private OriginalContinuation(FoodWeaponFinalDamage.Publication var1, FoodWeaponOriginalSequence.Session var2) {
                this.publication = var1;
                this.prepared = var1.prepared;
                this.receipt = var1.receipt;
                this.result = var1.result;
                this.producers = var1.producers;
                this.sequence = var2;
                this.thread = this.prepared.thread;
                this.acceptedBits = Float.floatToRawIntBits(var1.amount);
            }

            private boolean live() {
                synchronized (FoodWeaponSourceLedger.class) {
                    return !this.terminal
                        && this.publication.originalContinuation == this
                        && this.publication.originalSequence == this.sequence
                        && !this.publication.closed
                        && Thread.currentThread() == this.thread
                        && Thread.holdsLock(FoodWeaponSourceLedger.class)
                        && this.publication.prepared == this.prepared
                        && this.publication.receipt == this.receipt
                        && this.publication.result == this.result
                        && this.publication.producers == this.producers
                        && this.prepared.committed
                        && this.prepared.originalBinding != null
                        && this.publication.readiness.publication == this.publication
                        && this.publication.readiness.prepared == this.prepared
                        && this.publication.readiness.result == this.result
                        && this.publication.readiness.producers == this.producers
                        && this.publication.guardResult != null
                        && this.publication.guardResult.action.publication == this.publication
                        && this.publication.guardResult.action.prepared == this.prepared
                        && this.publication.guardResult.action.producers == this.producers
                        && this.publication.guardResult.action.guardResult == this.publication.guardResult
                        && this.publication.guardResult.originalBinding == this.prepared.originalBinding
                        && this.publication.guardResult.acceptedBits == this.acceptedBits
                        && Float.floatToRawIntBits(this.publication.amount) == this.acceptedBits
                        && this.sequence.lineage.publication == this.publication
                        && this.sequence.lineage.acceptedCallback == this.receipt
                        && this.sequence.original.continuing(this.publication);
                }
            }
        }
    }

    static final class PublishingCleanup {
        private final FoodWeaponFinalDamage.Publication publication;
        private final FoodWeaponFinalDamage.PreparedBatch prepared;
        private final FoodWeaponFinalDamage.ListedProducer producer;
        private final Object consumerPrepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final FoodWeaponFinalDamage.GuardResult guard;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding capturedBinding;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final Thread thread;
        private final MinecraftServer server;
        private final long at;
        private final int ordinal;
        private FoodWeaponFinalDamage.PublishingPhase phase = FoodWeaponFinalDamage.PublishingPhase.PUBLISHING;
        private Throwable cleanupFailure;

        private PublishingCleanup(FoodWeaponFinalDamage.Publication var1, FoodWeaponFinalDamage.ListedProducer var2, int var3) {
            this.publication = var1;
            this.prepared = var1.prepared;
            this.producer = var2;
            this.consumerPrepared = var2.preparedConsumer();
            this.receipt = var1.receipt;
            this.result = var1.result;
            this.guard = var1.guardResult;
            this.capturedBinding = this.guard.action.capture.originalBinding;
            this.originalBinding = this.prepared.originalBinding;
            this.thread = this.prepared.thread;
            this.server = var1.server;
            this.at = this.prepared.at;
            this.ordinal = var3;
        }
    }

    private static enum PublishingPhase {
        PUBLISHING,
        FINALIZING,
        CLOSED;
    }

    private static final class ReservationReadiness {
        private final FoodWeaponFinalDamage.Publication publication;
        private final FoodWeaponFinalDamage.PreparedBatch prepared;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final List<FoodWeaponFinalDamage.ListedProducer> producers;

        private ReservationReadiness(FoodWeaponFinalDamage.Publication var1) {
            this.publication = var1;
            this.prepared = var1.prepared;
            this.result = var1.result;
            this.producers = var1.producers;
        }

        private boolean matches(FoodWeaponFinalDamage.Publication var1) {
            return var1 == this.publication
                && this.prepared == var1.prepared
                && this.result == var1.result
                && this.producers == var1.producers
                && var1.guardResult != null
                && var1.guardResult.matches(var1.guardResult.action)
                && var1.guardResult.action.publication == var1
                && var1.guardResult.acceptedBits == Float.floatToRawIntBits(var1.amount)
                && this.result.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED;
        }
    }

    private static final class WolfProducer extends FoodWeaponFinalDamage.ListedProducer {
        private final FoodSocketPowers.PackPlan plan;
        private final FoodSocketPowers.PreparedPack consumerPrepared;
        private FoodSocketPowers.PackCommit consumerCommit;

        private WolfProducer(FoodWeaponFinalDamage.PreparedBatch var1, FoodSocketPowers.PackPlan var2, FoodSocketPowers.PreparedPack var3) {
            super(var1);
            this.plan = var2;
            this.consumerPrepared = var3;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        @Override
        boolean publish(float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            FoodWeaponFinalDamage.PreparedBatch var4 = this.prepared();
            if (!FoodSocketPowers.matchesPreparedPack(this.consumerPrepared, this.plan, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return false;
            } else {
                FoodWeaponFinalDamage.OriginalConsumerBinding var5 = null;

                try {
                    var5 = FoodWeaponFinalDamage.beginOriginalConsumerBinding(var3);
                    if (var5 != null) {
                        var5.bound = FoodSocketPowers.bindOriginalSequence(this.consumerPrepared, var5.sequence, this, var3.ordinal, var2, var1, var3);
                        if (!var5.bound) {
                            FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, null);
                        }
                    }
                } catch (Throwable var20) {
                    FoodWeaponFinalDamage.originalConsumerBindingFailed(var3, var20);
                } finally {
                    if (var5 != null) {
                        var5.binding = false;
                    }
                }

                Throwable var6 = null;
                boolean var7 = false;

                boolean var8;
                try {
                    this.consumerCommit = FoodSocketPowers.commitPack(this.consumerPrepared, var1, var2, var3);
                    var7 = FoodSocketPowers.matchesPackCommit(
                        this.consumerCommit, this.consumerPrepared, this.plan, var2, var1, var4.batch, var4.admission, var4.requests, var4.targets
                    );
                    var8 = var7;
                } catch (Throwable var18) {
                    var6 = var18;
                    throw var18;
                } finally {
                    FoodWeaponFinalDamage.finishOriginalConsumerPublication(var5, this.consumerCommit, var7, var6);
                }

                return var8;
            }
        }

        @Override
        void apply(FoodWeaponFinalDamage.ListedProducerInvocation var1) {
            FoodWeaponFinalDamage.ChildHurtBridge var2 = new FoodWeaponFinalDamage.ChildHurtBridge(var1);

            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(var1, var2, this.consumerCommit);
                FoodSocketPowers.applyPack(this.consumerCommit, var2);
            } finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(var1, var2);
                var2.closed = true;
            }
        }
    }
}
