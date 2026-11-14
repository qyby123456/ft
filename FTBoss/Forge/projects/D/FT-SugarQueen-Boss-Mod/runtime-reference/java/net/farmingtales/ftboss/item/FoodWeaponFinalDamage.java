/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.common.ForgeHooks
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.Event
 *  net.minecraftforge.eventbus.api.IEventBus
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodSocketAttributePowers;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponBossPassive;
import net.farmingtales.ftboss.item.FoodWeaponConsumerSequence;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponGuardKernel;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

public final class FoodWeaponFinalDamage {
    private static final StackWalker ORIGINAL_CALLER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final ThreadLocal<ArrayDeque<EventCapture>> EVENT_CAPTURES = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<PreparedListedAction>> GUARD_WAITERS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final int MAX_CAPTURE_DEPTH = 32;

    private FoodWeaponFinalDamage() {
    }

    public static boolean effectsReady() {
        return false;
    }

    private static boolean originalForgeCaller(String string, String string2, String string3) {
        return ORIGINAL_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponFinalDamage.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getDeclaringClass() == ForgeHooks.class && list.get(0).getMethodName().endsWith(string2) && list.get(0).getDescriptor().equals(string3) && list.get(1).getDeclaringClass() == ForgeHooks.class && list.get(1).getMethodName().equals(string) && list.get(1).getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F");
        });
    }

    private static boolean producerCaller(Class<?> clazz, String string) {
        return ORIGINAL_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponFinalDamage.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && (string == null || stackFrame.getMethodName().equals(string)) && stackFrame.getDescriptor().equals("(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V")).orElse(false));
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
    public static boolean dispatchOriginalHurtEvent(IEventBus iEventBus, Event event) {
        Object object;
        PreparedListedAction preparedListedAction2;
        FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding;
        CompoundTag compoundTag;
        Throwable throwable;
        EventCapture eventCapture;
        ServerPlayer serverPlayer;
        Object object2;
        LivingHurtEvent livingHurtEvent;
        block58: {
            block57: {
                block56: {
                    if (iEventBus != MinecraftForge.EVENT_BUS) return iEventBus.post(event);
                    if (!(event instanceof LivingHurtEvent)) return iEventBus.post(event);
                    livingHurtEvent = (LivingHurtEvent)event;
                    if (!FoodWeaponFinalDamage.originalForgeCaller("onLivingHurt", "ftboss$paidHurtDispatch", "(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;)Z")) return iEventBus.post(event);
                    object2 = livingHurtEvent.getSource().m_7639_();
                    if (!(object2 instanceof ServerPlayer)) return iEventBus.post(event);
                    serverPlayer = (ServerPlayer)object2;
                    object2 = EVENT_CAPTURES.get();
                    eventCapture = null;
                    throwable = null;
                    compoundTag = FoodWeaponSourceLedger.resolveReadOnly(livingHurtEvent.getSource(), serverPlayer);
                    if (!compoundTag.m_128456_() && FoodWeaponFinalDamage.sourceMatchesTarget(compoundTag, serverPlayer, livingHurtEvent.getSource(), livingHurtEvent.getEntity())) break block56;
                    boolean bl = iEventBus.post(event);
                    try {
                        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                        // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                        if (eventCapture != null) {
                            eventCapture.closed = true;
                            if (((ArrayDeque)object2).peekLast() == eventCapture) {
                                ((ArrayDeque)object2).removeLast();
                            } else {
                                ((ArrayDeque)object2).remove(eventCapture);
                            }
                        }
                        if (((ArrayDeque)object2).isEmpty()) {
                            EVENT_CAPTURES.remove();
                        }
                        // MONITOREXIT : clazz
                        return bl;
                    }
                    catch (Throwable throwable2) {
                        if (throwable == null) {
                            throw throwable2;
                        }
                        FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable2);
                    }
                    return bl;
                }
                if (((ArrayDeque)object2).size() < 32) break block57;
                boolean bl = iEventBus.post(event);
                try {
                    Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                    // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                    if (eventCapture != null) {
                        eventCapture.closed = true;
                        if (((ArrayDeque)object2).peekLast() == eventCapture) {
                            ((ArrayDeque)object2).removeLast();
                        } else {
                            ((ArrayDeque)object2).remove(eventCapture);
                        }
                    }
                    if (((ArrayDeque)object2).isEmpty()) {
                        EVENT_CAPTURES.remove();
                    }
                    // MONITOREXIT : clazz
                    return bl;
                }
                catch (Throwable throwable3) {
                    if (throwable == null) {
                        throw throwable3;
                    }
                    FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable3);
                }
                return bl;
            }
            originalInteractionBinding = FoodWeaponSourceLedger.capturePrimaryInteractionBinding(compoundTag, serverPlayer, livingHurtEvent.getSource(), (Entity)livingHurtEvent.getEntity());
            if (originalInteractionBinding != null) break block58;
            boolean bl = iEventBus.post(event);
            try {
                Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                if (eventCapture != null) {
                    eventCapture.closed = true;
                    if (((ArrayDeque)object2).peekLast() == eventCapture) {
                        ((ArrayDeque)object2).removeLast();
                    } else {
                        ((ArrayDeque)object2).remove(eventCapture);
                    }
                }
                if (((ArrayDeque)object2).isEmpty()) {
                    EVENT_CAPTURES.remove();
                }
                // MONITOREXIT : clazz
                return bl;
            }
            catch (Throwable throwable4) {
                if (throwable == null) {
                    throw throwable4;
                }
                FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable4);
            }
            return bl;
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
        eventCapture = new EventCapture(livingHurtEvent, serverPlayer, compoundTag, originalInteractionBinding);
        ((ArrayDeque)object2).addLast(eventCapture);
        // MONITOREXIT : clazz
        boolean bl = iEventBus.post(event);
        if (!bl && eventCapture.live() && (preparedListedAction2 = FoodWeaponFinalDamage.sealCapturedProducers(eventCapture)) != null) {
            object = GUARD_WAITERS.get();
            ((ArrayDeque)object).removeIf(preparedListedAction -> preparedListedAction.capture.thread != Thread.currentThread() || FoodWeaponFinalDamage.clock(preparedListedAction.prepared.player) != preparedListedAction.prepared.at || !preparedListedAction.prepared.stable());
            if (((ArrayDeque)object).size() < 32) {
                ((ArrayDeque)object).addLast(preparedListedAction2);
            }
        }
        boolean bl2 = bl;
        try {
            object = FoodWeaponSourceLedger.class;
            // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
            if (eventCapture != null) {
                eventCapture.closed = true;
                if (((ArrayDeque)object2).peekLast() == eventCapture) {
                    ((ArrayDeque)object2).removeLast();
                } else {
                    ((ArrayDeque)object2).remove(eventCapture);
                }
            }
            if (((ArrayDeque)object2).isEmpty()) {
                EVENT_CAPTURES.remove();
            }
            // MONITOREXIT : object
            return bl2;
        }
        catch (Throwable throwable5) {
            if (throwable == null) {
                throw throwable5;
            }
            FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable5);
        }
        return bl2;
        catch (Throwable throwable6) {
            try {
                throwable = throwable6;
                throw throwable6;
            }
            catch (Throwable throwable7) {
                try {
                    Class<FoodWeaponSourceLedger> clazz2 = FoodWeaponSourceLedger.class;
                    // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                    if (eventCapture != null) {
                        eventCapture.closed = true;
                        if (((ArrayDeque)object2).peekLast() == eventCapture) {
                            ((ArrayDeque)object2).removeLast();
                        } else {
                            ((ArrayDeque)object2).remove(eventCapture);
                        }
                    }
                    if (((ArrayDeque)object2).isEmpty()) {
                        EVENT_CAPTURES.remove();
                    }
                    // MONITOREXIT : clazz2
                    throw throwable7;
                }
                catch (Throwable throwable8) {
                    if (throwable == null) {
                        throw throwable8;
                    }
                    FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable8);
                }
                throw throwable7;
            }
        }
    }

    private static EventCapture currentCapture(LivingHurtEvent livingHurtEvent) {
        EventCapture eventCapture = EVENT_CAPTURES.get().peekLast();
        return eventCapture != null && eventCapture.event == livingHurtEvent && eventCapture.live() ? eventCapture : null;
    }

    public static void captureEvolution(LivingHurtEvent livingHurtEvent) {
        ItemStack itemStack;
        if (!FoodWeaponFinalDamage.producerCaller(FoodWeaponEvolution.class, "weaponHit")) {
            return;
        }
        EventCapture eventCapture = FoodWeaponFinalDamage.currentCapture(livingHurtEvent);
        if (eventCapture == null) {
            return;
        }
        float f = livingHurtEvent.getAmount();
        FoodWeaponBondHits.Hit hit = FoodWeaponBondHits.resolve(eventCapture.source, eventCapture.player);
        ItemStack itemStack2 = itemStack = hit == null ? ItemStack.f_41583_ : FoodWeaponBondHits.equipped(eventCapture.player, hit);
        if (hit == null || itemStack.m_41619_()) {
            eventCapture.failed = true;
            return;
        }
        FoodWeaponHitPlan foodWeaponHitPlan = FoodWeaponEvolution.quoteFinalWeaponHit(livingHurtEvent, hit, itemStack, (attemptDomain, d) -> FoodWeaponSourceLedger.quoteAttempt(eventCapture.snapshot, eventCapture.player, attemptDomain, d));
        if (eventCapture.append(ProducerFamily.EVOLUTION, foodWeaponHitPlan, f, foodWeaponHitPlan.amount())) {
            livingHurtEvent.setAmount(foodWeaponHitPlan.amount());
        }
    }

    public static void captureMaterial(LivingHurtEvent livingHurtEvent) {
        if (!FoodWeaponFinalDamage.producerCaller(WeaponMaterialBuild.class, "hit")) {
            return;
        }
        EventCapture eventCapture = FoodWeaponFinalDamage.currentCapture(livingHurtEvent);
        if (eventCapture == null) {
            return;
        }
        float f = livingHurtEvent.getAmount();
        FoodWeaponBondHits.Hit hit = FoodWeaponBondHits.resolve(eventCapture.source, eventCapture.player);
        if (hit == null) {
            eventCapture.failed = true;
            return;
        }
        FoodWeaponHitPlan foodWeaponHitPlan = WeaponMaterialBuild.quoteFinalHit(livingHurtEvent, hit);
        if (eventCapture.append(ProducerFamily.MATERIAL, foodWeaponHitPlan, f, foodWeaponHitPlan.amount())) {
            livingHurtEvent.setAmount(foodWeaponHitPlan.amount());
        }
    }

    public static void captureAttribute(LivingHurtEvent livingHurtEvent, FoodSocketAttributePowers.OffensePlan offensePlan) {
        if (!FoodWeaponFinalDamage.producerCaller(FoodSocketAttributePowers.class, null)) {
            return;
        }
        EventCapture eventCapture = FoodWeaponFinalDamage.currentCapture(livingHurtEvent);
        if (eventCapture == null) {
            return;
        }
        float f = livingHurtEvent.getAmount();
        if (!(offensePlan == null || Float.floatToRawIntBits(offensePlan.amount()) == Float.floatToRawIntBits(f) && offensePlan.snapshot().equals((Object)eventCapture.snapshot))) {
            eventCapture.failed = true;
            return;
        }
        eventCapture.append(ProducerFamily.ATTRIBUTE, offensePlan, f, f);
    }

    public static void capturePack(LivingHurtEvent livingHurtEvent, FoodSocketPowers.PackPlan packPlan) {
        if (!FoodWeaponFinalDamage.producerCaller(FoodSocketPowers.class, null)) {
            return;
        }
        EventCapture eventCapture = FoodWeaponFinalDamage.currentCapture(livingHurtEvent);
        if (eventCapture == null) {
            return;
        }
        if (packPlan != null && !packPlan.snapshot().equals((Object)eventCapture.snapshot)) {
            eventCapture.failed = true;
            return;
        }
        eventCapture.append(ProducerFamily.WOLF, packPlan, livingHurtEvent.getAmount(), livingHurtEvent.getAmount());
    }

    public static void capturePassiveHit(LivingHurtEvent livingHurtEvent, FoodWeaponBossPassive.HitPlan hitPlan) {
        float f;
        if (!FoodWeaponFinalDamage.producerCaller(FoodWeaponBossPassive.class, null)) {
            return;
        }
        EventCapture eventCapture = FoodWeaponFinalDamage.currentCapture(livingHurtEvent);
        if (eventCapture == null) {
            return;
        }
        float f2 = livingHurtEvent.getAmount();
        float f3 = f = hitPlan == null ? f2 : hitPlan.amount();
        if (!(hitPlan == null || Float.floatToRawIntBits(hitPlan.input()) == Float.floatToRawIntBits(f2) && hitPlan.context().snapshot().equals((Object)eventCapture.snapshot))) {
            eventCapture.failed = true;
            return;
        }
        if (eventCapture.append(ProducerFamily.PASSIVE, hitPlan, f2, f)) {
            livingHurtEvent.setAmount(f);
        }
    }

    private static PreparedListedAction sealCapturedProducers(EventCapture eventCapture) {
        Object object;
        Object object2;
        Object object3;
        if (!eventCapture.live() || eventCapture.producers.size() != ProducerFamily.values().length) {
            return null;
        }
        ArrayList<FoodWeaponSourceLedger.AttemptRequest> arrayList = new ArrayList<FoodWeaponSourceLedger.AttemptRequest>();
        ArrayList<FoodWeaponSourceLedger.TargetRequest> arrayList2 = new ArrayList<FoodWeaponSourceLedger.TargetRequest>();
        FoodWeaponHitPlan foodWeaponHitPlan = null;
        FoodWeaponHitPlan object5 = null;
        FoodSocketAttributePowers.OffensePlan offensePlan = null;
        FoodSocketPowers.PackPlan packPlan = null;
        FoodWeaponBossPassive.HitPlan hitPlan = null;
        for (CapturedProducer object42 : eventCapture.producers) {
            object3 = object42.plan();
            if (object3 instanceof FoodWeaponHitPlan) {
                FoodWeaponHitPlan foodWeaponHitPlan2 = (FoodWeaponHitPlan)object3;
                for (FoodWeaponHitPlan.Attempt attempt : foodWeaponHitPlan2.attempts()) {
                    arrayList.add(new FoodWeaponSourceLedger.AttemptRequest(attempt.effect(), attempt.chance()));
                }
                arrayList2.addAll(foodWeaponHitPlan2.targets());
                if (object42.family() == ProducerFamily.MATERIAL) {
                    foodWeaponHitPlan = foodWeaponHitPlan2;
                    continue;
                }
                if (object42.family() == ProducerFamily.EVOLUTION) {
                    object5 = foodWeaponHitPlan2;
                    continue;
                }
                return null;
            }
            object3 = object42.plan();
            if (object3 instanceof FoodSocketAttributePowers.OffensePlan) {
                object2 = (FoodSocketAttributePowers.OffensePlan)object3;
                offensePlan = object2;
                arrayList.addAll(((FoodSocketAttributePowers.OffensePlan)object2).attempts());
                continue;
            }
            object3 = object42.plan();
            if (object3 instanceof FoodSocketPowers.PackPlan) {
                object = (FoodSocketPowers.PackPlan)object3;
                packPlan = object;
                arrayList.addAll(((FoodSocketPowers.PackPlan)object).attempts());
                arrayList2.addAll(((FoodSocketPowers.PackPlan)object).targets());
                continue;
            }
            object3 = object42.plan();
            if (object3 instanceof FoodWeaponBossPassive.HitPlan) {
                FoodWeaponBossPassive.HitPlan hitPlan2;
                hitPlan = hitPlan2 = (FoodWeaponBossPassive.HitPlan)object3;
                arrayList.addAll(hitPlan2.attempts());
                arrayList2.addAll(hitPlan2.targets());
                continue;
            }
            if (object42.plan() == null) continue;
            return null;
        }
        if (arrayList.isEmpty() && arrayList2.isEmpty()) {
            return null;
        }
        PreparedBatch preparedBatch = FoodWeaponFinalDamage.admitTargetBatch(eventCapture.snapshot, eventCapture.player, eventCapture.source, eventCapture.target, arrayList, arrayList2);
        if (preparedBatch == null || preparedBatch.originalBinding != eventCapture.originalBinding) {
            return null;
        }
        for (CapturedProducer capturedProducer : eventCapture.producers) {
            object = capturedProducer.plan();
            if (!(object instanceof FoodWeaponHitPlan)) continue;
            object2 = (FoodWeaponHitPlan)object;
            for (FoodWeaponHitPlan.Attempt attempt : ((FoodWeaponHitPlan)object2).attempts()) {
                object3 = preparedBatch.batch.entries().stream().filter(attemptQuote -> attemptQuote.domain() == attempt.effect()).findFirst().orElse(null);
                FoodWeaponSourceLedger.AttemptQuote attemptQuote2 = attempt.outcome();
                if (object3 != null && ((FoodWeaponSourceLedger.AttemptQuote)object3).status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE && attemptQuote2.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE && Double.doubleToRawLongBits(((FoodWeaponSourceLedger.AttemptQuote)object3).chance()) == Double.doubleToRawLongBits(attemptQuote2.chance()) && Double.doubleToRawLongBits(((FoodWeaponSourceLedger.AttemptQuote)object3).sample()) == Double.doubleToRawLongBits(attemptQuote2.sample()) && ((FoodWeaponSourceLedger.AttemptQuote)object3).success() == attemptQuote2.success()) continue;
                return null;
            }
        }
        PreparedListedAction preparedListedAction = FoodWeaponFinalDamage.collectListedProducers(preparedBatch, foodWeaponHitPlan, object5, offensePlan, packPlan, hitPlan, eventCapture.producers.stream().map(CapturedProducer::family).toList());
        if (preparedListedAction != null) {
            preparedListedAction.capture = eventCapture;
        }
        return preparedListedAction;
    }

    private static PreparedListedAction takeGuardWaiter(LivingEntity livingEntity, DamageSource damageSource) {
        ArrayDeque<PreparedListedAction> arrayDeque = GUARD_WAITERS.get();
        Iterator<PreparedListedAction> iterator = arrayDeque.descendingIterator();
        PreparedListedAction preparedListedAction = null;
        while (iterator.hasNext()) {
            PreparedListedAction preparedListedAction2 = iterator.next();
            if (!preparedListedAction2.prepared.stable()) {
                iterator.remove();
                continue;
            }
            if (preparedListedAction2.prepared.target != livingEntity || preparedListedAction2.prepared.source != damageSource) continue;
            iterator.remove();
            preparedListedAction = preparedListedAction2;
            break;
        }
        if (arrayDeque.isEmpty()) {
            GUARD_WAITERS.remove();
        }
        return preparedListedAction;
    }

    static void retireOriginalCapture(FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
        if (originalInteractionBinding == null || !Thread.holdsLock(FoodWeaponSourceLedger.class) || !ORIGINAL_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponFinalDamage.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false)).booleanValue()) {
            return;
        }
        ArrayDeque<PreparedListedAction> arrayDeque = GUARD_WAITERS.get();
        arrayDeque.removeIf(preparedListedAction -> {
            if (preparedListedAction.prepared.originalBinding != originalInteractionBinding) {
                return false;
            }
            if (preparedListedAction.publication != null) {
                preparedListedAction.publication.closed = true;
            }
            return true;
        });
        if (arrayDeque.isEmpty()) {
            GUARD_WAITERS.remove();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean receiptLive(Publication publication) {
        if (publication != null && publication.originalContinuation != null) {
            return FoodWeaponFinalDamage.originalReceiptLive(publication);
        }
        if (publication == null || publication.publishingCleanup != null && publication.publishingCleanup.phase != PublishingPhase.PUBLISHING) {
            return false;
        }
        if (!FoodWeaponFinalDamage.publicationLive(publication)) {
            return false;
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (publication.closed || publication.publishingCleanup != null && publication.publishingCleanup.phase != PublishingPhase.PUBLISHING || !publication.readiness.matches(publication)) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return false;
            }
            if (publication.currentStateProducer != null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return publication.currentInvocation == null && publication.producers.contains(publication.currentStateProducer);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
            return listedProducerInvocation != null && !listedProducerInvocation.closed && listedProducerInvocation.childCall == null && listedProducerInvocation.publication == publication && listedProducerInvocation.ordinal >= 0 && listedProducerInvocation.ordinal < publication.producers.size() && publication.producers.get(listedProducerInvocation.ordinal) == listedProducerInvocation.producer;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean publicationLive(Publication publication) {
        if (publication != null && publication.originalContinuation != null) {
            return FoodWeaponFinalDamage.originalPublicationLive(publication);
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (publication == null || publication.closed || !publication.readiness.matches(publication)) {
                // ** MonitorExit[var6_1] (shouldn't be in output)
                return false;
            }
            PreparedBatch preparedBatch = publication.prepared;
            FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication = publication.writerPublication;
            ReservationReadiness reservationReadiness = publication.readiness;
            FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit = publication.result;
            List<ListedProducer> list = publication.producers;
            if (!(preparedBatch.committed && preparedBatch.originalBinding != null && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == preparedBatch.thread && publication.server == preparedBatch.player.m_284548_().m_7654_() && publication.server.m_18695_() && FoodWeaponFinalDamage.clock(preparedBatch.player) == preparedBatch.at && Float.isFinite(publication.amount) && !(publication.amount <= 0.0f) && preparedBatch.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY && attemptBatchCommit.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED)) {
                // ** MonitorExit[var6_1] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var6_1] (shouldn't be in output)
            if (!(FoodWeaponSourceLedger.matchesOriginalInteraction(preparedBatch.originalBinding, preparedBatch.snapshot, preparedBatch.player) && FoodWeaponSourceLedger.resolveReadOnly(preparedBatch.source, preparedBatch.player).equals((Object)preparedBatch.snapshot) && FoodWeaponFinalDamage.sourceMatchesTarget(preparedBatch.snapshot, preparedBatch.player, preparedBatch.source, preparedBatch.target) && FoodWeaponSourceLedger.matchesAttemptRequests(preparedBatch.batch, preparedBatch.requests, preparedBatch.targets) && FoodWeaponSourceLedger.matchesAttemptBatch(preparedBatch.snapshot, preparedBatch.player, preparedBatch.batch, attemptBatchCommit, preparedBatch.at) && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(ownerWriterPublication) && FoodWeaponSourceLedger.matchesOriginalInteraction(preparedBatch.originalBinding, preparedBatch.snapshot, preparedBatch.player))) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                // ** MonitorExit[var6_1] (shouldn't be in output)
                return publication.prepared == preparedBatch && publication.writerPublication == ownerWriterPublication && publication.readiness == reservationReadiness && publication.result == attemptBatchCommit && publication.producers == list && !publication.closed && reservationReadiness.matches(publication) && preparedBatch.committed && preparedBatch.originalBinding != null && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == preparedBatch.thread && publication.server == preparedBatch.player.m_284548_().m_7654_() && publication.server.m_18695_() && FoodWeaponFinalDamage.clock(preparedBatch.player) == preparedBatch.at && Float.isFinite(publication.amount) && publication.amount > 0.0f && preparedBatch.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY && attemptBatchCommit.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED;
            }
        }
    }

    private static boolean producerPublished(ListedProducer listedProducer, Publication publication) {
        if (listedProducer instanceof AttributeProducer) {
            AttributeProducer attributeProducer = (AttributeProducer)listedProducer;
            return FoodSocketAttributePowers.matchesOffenseCommit(attributeProducer.consumerCommit, attributeProducer.consumerPrepared, attributeProducer.plan, publication.receipt, publication.amount, listedProducer.prepared.batch, listedProducer.prepared.admission, listedProducer.prepared.requests, listedProducer.prepared.targets);
        }
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            return materialProducer.consumerCommit != null;
        }
        if (listedProducer instanceof EvolutionProducer) {
            EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
            return evolutionProducer.consumerCommit != null;
        }
        if (listedProducer instanceof WolfProducer) {
            WolfProducer wolfProducer = (WolfProducer)listedProducer;
            return FoodSocketPowers.matchesPackCommit(wolfProducer.consumerCommit, wolfProducer.consumerPrepared, wolfProducer.plan, publication.receipt, publication.amount, listedProducer.prepared.batch, listedProducer.prepared.admission, listedProducer.prepared.requests, listedProducer.prepared.targets);
        }
        if (listedProducer instanceof PassiveProducer) {
            PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
            return FoodWeaponBossPassive.matchesPassiveHitCommit(passiveProducer.consumerCommit, passiveProducer.consumerPrepared, passiveProducer.plan, publication.receipt, publication.amount, listedProducer.prepared.batch, listedProducer.prepared.admission, listedProducer.prepared.requests, listedProducer.prepared.targets);
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean localPublishingIdentity(PublishingCleanup publishingCleanup, PublishingPhase publishingPhase) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (publishingCleanup == null || publishingCleanup.phase != publishingPhase || !Thread.holdsLock(FoodWeaponSourceLedger.class) || Thread.currentThread() != publishingCleanup.thread) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            Publication publication = publishingCleanup.publication;
            PreparedBatch preparedBatch = publishingCleanup.prepared;
            GuardResult guardResult = publishingCleanup.guard;
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return !publication.closed && publication.publishingCleanup == publishingCleanup && publication.prepared == preparedBatch && preparedBatch.committed && preparedBatch.thread == publishingCleanup.thread && preparedBatch.at == publishingCleanup.at && publication.server == publishingCleanup.server && publication.receipt == publishingCleanup.receipt && publication.result == publishingCleanup.result && publication.guardResult == guardResult && publication.currentStateProducer == publishingCleanup.producer && publication.currentInvocation == null && publishingCleanup.producer.prepared() == preparedBatch && publishingCleanup.producer.publishAttempted && publishingCleanup.producer.preparedConsumer() == publishingCleanup.consumerPrepared && publishingCleanup.consumerPrepared != null && publishingCleanup.ordinal >= 0 && publishingCleanup.ordinal < publication.producers.size() && publication.producers.get(publishingCleanup.ordinal) == publishingCleanup.producer && publication.readiness.publication == publication && publication.readiness.prepared == preparedBatch && publication.readiness.result == publishingCleanup.result && publication.readiness.producers == publication.producers && guardResult.action.prepared == preparedBatch && guardResult.action.publication == publication && guardResult.action.producers == publication.producers && guardResult.action.guardResult == guardResult && guardResult.action.capture != null && guardResult.action.capture.originalBinding == publishingCleanup.capturedBinding && preparedBatch.originalBinding == publishingCleanup.originalBinding && guardResult.originalBinding == publishingCleanup.originalBinding && guardResult.player == preparedBatch.player && guardResult.source == preparedBatch.source && guardResult.target == preparedBatch.target && guardResult.thread == publishingCleanup.thread && guardResult.at == publishingCleanup.at && guardResult.acceptedBits == Float.floatToRawIntBits(publication.amount);
        }
    }

    private static boolean publishingIdentity(PublishingCleanup publishingCleanup, PublishingPhase publishingPhase) {
        if (!FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, publishingPhase)) {
            return false;
        }
        if (publishingCleanup.publication.originalContinuation != null) {
            Publication.OriginalContinuation originalContinuation = publishingCleanup.publication.originalContinuation;
            if (publishingPhase == PublishingPhase.FINALIZING) {
                return FoodWeaponFinalDamage.matchesOriginalRunAdmission(originalContinuation.sequence.original, publishingCleanup.publication, originalContinuation);
            }
            return FoodWeaponFinalDamage.originalPublicationLive(publishingCleanup.publication) && publishingCleanup.publication.originalSequence.original.atProducer(publishingCleanup.producer);
        }
        PreparedBatch preparedBatch = publishingCleanup.prepared;
        if (!FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(publishingCleanup.capturedBinding, FoodWeaponSourceLedger.originalInteractionBinding(preparedBatch.batch), preparedBatch.player)) {
            return false;
        }
        return FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, publishingPhase);
    }

    static boolean matchesOffensePublishing(FoodSocketAttributePowers.PreparedOffense preparedOffense, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof AttributeProducer && publishingCleanup.consumerPrepared == preparedOffense && publishingCleanup.receipt == offenseReceipt && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING);
    }

    static boolean matchesPackPublishing(FoodSocketPowers.PreparedPack preparedPack, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof WolfProducer && publishingCleanup.consumerPrepared == preparedPack && publishingCleanup.receipt == offenseReceipt && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING);
    }

    static boolean matchesPassiveHitPublishing(FoodWeaponBossPassive.PreparedHit preparedHit, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof PassiveProducer && publishingCleanup.consumerPrepared == preparedHit && publishingCleanup.receipt == offenseReceipt && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING);
    }

    static boolean matchesOffensePublicationCleanup(FoodSocketAttributePowers.PreparedOffense preparedOffense, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof AttributeProducer && publishingCleanup.consumerPrepared == preparedOffense && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.FINALIZING);
    }

    static boolean matchesPackPublicationCleanup(FoodSocketPowers.PreparedPack preparedPack, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof WolfProducer && publishingCleanup.consumerPrepared == preparedPack && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.FINALIZING);
    }

    static boolean matchesPassiveHitPublicationCleanup(FoodWeaponBossPassive.PreparedHit preparedHit, PublishingCleanup publishingCleanup) {
        return publishingCleanup != null && publishingCleanup.producer instanceof PassiveProducer && publishingCleanup.consumerPrepared == preparedHit && FoodWeaponFinalDamage.publishingIdentity(publishingCleanup, PublishingPhase.FINALIZING);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean localInvocationReady(ChildHurtBridge childHurtBridge) {
        if (childHurtBridge != null && childHurtBridge.invocation != null && childHurtBridge.invocation.publication.originalContinuation != null) {
            return FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge);
        }
        if (childHurtBridge == null || childHurtBridge.closed) {
            return false;
        }
        ListedProducerInvocation listedProducerInvocation = childHurtBridge.invocation;
        if (listedProducerInvocation == null || listedProducerInvocation.closed) {
            return false;
        }
        Publication publication = listedProducerInvocation.publication;
        if (!FoodWeaponFinalDamage.publicationLive(publication) || !FoodWeaponFinalDamage.producerPublished(listedProducerInvocation.producer, publication)) {
            return false;
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            PreparedBatch preparedBatch = publication.prepared;
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return !childHurtBridge.closed && !publication.closed && publication.readiness.matches(publication) && publication.currentStateProducer == null && publication.currentInvocation == listedProducerInvocation && !listedProducerInvocation.closed && listedProducerInvocation.publication == publication && listedProducerInvocation.ordinal >= 0 && listedProducerInvocation.ordinal < publication.producers.size() && publication.producers.get(listedProducerInvocation.ordinal) == listedProducerInvocation.producer && listedProducerInvocation.producer.prepared == preparedBatch && preparedBatch.committed && preparedBatch.originalBinding != null && publication.result.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED && preparedBatch.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == preparedBatch.thread && publication.server == preparedBatch.player.m_284548_().m_7654_() && publication.server.m_18695_() && FoodWeaponFinalDamage.clock(preparedBatch.player) == preparedBatch.at;
        }
    }

    private static boolean localInvocationLive(ChildHurtBridge childHurtBridge) {
        if (childHurtBridge != null && childHurtBridge.invocation != null && childHurtBridge.invocation.publication.originalContinuation != null) {
            return FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge);
        }
        return FoodWeaponFinalDamage.localInvocationReady(childHurtBridge) && FoodWeaponSourceLedger.ownerWriterContinuation(childHurtBridge.invocation.publication.writerPublication);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesAttributeInvocation(FoodSocketAttributePowers.OffenseCommit offenseCommit, ChildHurtBridge childHurtBridge) {
        if (offenseCommit == null) return false;
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge)) return false;
        if (childHurtBridge.invocation.childCall != null) return false;
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (!(listedProducer instanceof AttributeProducer)) return false;
        AttributeProducer attributeProducer = (AttributeProducer)listedProducer;
        if (attributeProducer.consumerCommit != offenseCommit) return false;
        return true;
    }

    private static boolean originalCleanupIdentity(ChildHurtBridge childHurtBridge) {
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge) || childHurtBridge.invocation.childCall != null) {
            return false;
        }
        if (childHurtBridge.invocation.producer.originalConsumer != null && childHurtBridge.invocation.producer.originalConsumer.bound) {
            return FoodWeaponFinalDamage.originalConsumerApplyLive(childHurtBridge);
        }
        Publication publication = childHurtBridge.invocation.publication;
        GuardResult guardResult = publication.guardResult;
        return guardResult != null && guardResult.action.capture != null && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(guardResult.action.capture.originalBinding, publication.prepared.originalBinding, publication.prepared.player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesPackCleanupInvocation(FoodSocketPowers.PackCommit packCommit, ChildHurtBridge childHurtBridge) {
        if (packCommit == null) return false;
        if (!FoodWeaponFinalDamage.originalCleanupIdentity(childHurtBridge)) return false;
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (!(listedProducer instanceof WolfProducer)) return false;
        WolfProducer wolfProducer = (WolfProducer)listedProducer;
        if (wolfProducer.consumerCommit != packCommit) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesPassiveHitCleanupInvocation(FoodWeaponBossPassive.HitCommit hitCommit, ChildHurtBridge childHurtBridge) {
        if (hitCommit == null) return false;
        if (!FoodWeaponFinalDamage.originalCleanupIdentity(childHurtBridge)) return false;
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (!(listedProducer instanceof PassiveProducer)) return false;
        PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
        if (passiveProducer.consumerCommit != hitCommit) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesPackInvocation(FoodSocketPowers.PackCommit packCommit, ChildHurtBridge childHurtBridge) {
        if (packCommit == null) return false;
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge)) return false;
        if (childHurtBridge.invocation.childCall != null) return false;
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (!(listedProducer instanceof WolfProducer)) return false;
        WolfProducer wolfProducer = (WolfProducer)listedProducer;
        if (wolfProducer.consumerCommit != packCommit) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesPassiveHitInvocation(FoodWeaponBossPassive.HitCommit hitCommit, ChildHurtBridge childHurtBridge) {
        if (hitCommit == null) return false;
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge)) return false;
        if (childHurtBridge.invocation.childCall != null) return false;
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (!(listedProducer instanceof PassiveProducer)) return false;
        PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
        if (passiveProducer.consumerCommit != hitCommit) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesHitPlanInvocation(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, ChildHurtBridge childHurtBridge) {
        if (finalEffectsCommit == null) return false;
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge)) return false;
        if (childHurtBridge.invocation.childCall != null) {
            return false;
        }
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            if (materialProducer.consumerCommit == finalEffectsCommit) return true;
        }
        if (!(listedProducer instanceof EvolutionProducer)) return false;
        EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
        if (evolutionProducer.consumerCommit != finalEffectsCommit) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesHitPlanSelectionPhase(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, ChildHurtBridge childHurtBridge) {
        if (finalEffectsCommit == null) return false;
        if (!FoodWeaponFinalDamage.localInvocationReady(childHurtBridge)) return false;
        if (childHurtBridge.invocation.childCall != null) {
            return false;
        }
        ListedProducer listedProducer = childHurtBridge.invocation.producer;
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            if (materialProducer.consumerCommit == finalEffectsCommit) return true;
        }
        if (!(listedProducer instanceof EvolutionProducer)) return false;
        EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
        if (evolutionProducer.consumerCommit != finalEffectsCommit) return false;
        return true;
    }

    static boolean matchesOwnerChildCall(ChildHurtCall childHurtCall) {
        return childHurtCall != null && !childHurtCall.closed && childHurtCall.thread == Thread.currentThread() && FoodWeaponFinalDamage.localInvocationLive(childHurtCall.bridge) && childHurtCall.bridge.invocation.childCall == childHurtCall && childHurtCall.originalBinding == childHurtCall.bridge.invocation.publication.prepared.originalBinding && FoodWeaponFinalDamage.matchesOriginalConsumerChild(childHurtCall) && childHurtCall.source != null && childHurtCall.target != null && Float.isFinite(childHurtCall.amount) && childHurtCall.amount > 0.0f;
    }

    static FoodWeaponSourceLedger.OriginalInteractionBinding ownerChildOriginalBinding(ChildHurtCall childHurtCall) {
        return FoodWeaponFinalDamage.matchesOwnerChildCall(childHurtCall) ? childHurtCall.originalBinding : null;
    }

    static DamageSource ownerChildSource(ChildHurtCall childHurtCall) {
        return FoodWeaponFinalDamage.matchesOwnerChildCall(childHurtCall) ? childHurtCall.source : null;
    }

    static LivingEntity ownerChildTarget(ChildHurtCall childHurtCall) {
        return FoodWeaponFinalDamage.matchesOwnerChildCall(childHurtCall) ? childHurtCall.target : null;
    }

    static float ownerChildAmount(ChildHurtCall childHurtCall) {
        return FoodWeaponFinalDamage.matchesOwnerChildCall(childHurtCall) ? childHurtCall.amount : Float.NaN;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean hurtListedChild(ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
        Object object;
        ChildHurtCall childHurtCall;
        if (!FoodWeaponFinalDamage.localInvocationLive(childHurtBridge) || childHurtBridge.invocation.childCall != null || !FoodWeaponFinalDamage.publicationLive(childHurtBridge.invocation.publication) || livingEntity == null || damageSource == null || !Float.isFinite(f) || f <= 0.0f) {
            return false;
        }
        ListedProducerInvocation listedProducerInvocation = childHurtBridge.invocation;
        listedProducerInvocation.childCall = childHurtCall = new ChildHurtCall(childHurtBridge, livingEntity, damageSource, f);
        if (FoodWeaponFinalDamage.hasBoundOriginalAttributeConsumer(childHurtBridge)) {
            try {
                object = (AttributeProducer)listedProducerInvocation.producer;
                childHurtCall.originalConsumerChild = FoodSocketAttributePowers.originalAttributeChild(((AttributeProducer)object).consumerCommit, childHurtBridge, livingEntity, damageSource, f);
            }
            catch (Throwable throwable) {
                childHurtCall.originalConsumerChildFailure = throwable;
                FoodWeaponFinalDamage.revokeExtraAuthority(listedProducerInvocation.publication, throwable);
            }
        }
        object = null;
        try {
            object = FoodWeaponSourceLedger.enterFinalListedChild(childHurtCall);
            if (object == null) {
                boolean bl = false;
                return bl;
            }
            childHurtCall.hurtEntered = true;
            boolean bl = FoodWeaponSourceLedger.hurtFinalListedChild((FoodWeaponSourceLedger.FinalListedChildScope)object);
            return bl;
        }
        finally {
            try {
                if (object != null) {
                    FoodWeaponSourceLedger.leaveFinalListedChild((FoodWeaponSourceLedger.FinalListedChildScope)object);
                }
            }
            finally {
                childHurtCall.closed = true;
                if (listedProducerInvocation.childCall == childHurtCall) {
                    listedProducerInvocation.childCall = null;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean reserveListedAction(PreparedListedAction preparedListedAction, GuardResult guardResult) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Publication publication;
            if (!Thread.holdsLock(FoodWeaponSourceLedger.class) || preparedListedAction == null || guardResult == null || !guardResult.matches(preparedListedAction) || preparedListedAction.publication != null) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            float f = Float.intBitsToFloat(guardResult.acceptedBits);
            preparedListedAction.publication = publication = new Publication(preparedListedAction, preparedListedAction.prepared.commit(f), f);
            PreparedBatch preparedBatch = publication.prepared;
            try {
                publication.ownerReserveSelectionOpen = true;
                publication.writerPublication = FoodWeaponSourceLedger.openOwnerWriterPublication(preparedBatch.originalBinding, preparedBatch.batch, preparedBatch.admission, publication.result, publication.receipt);
            }
            catch (Throwable throwable) {
                FoodWeaponFinalDamage.retainWriterObservationFailure(publication, null, 0, throwable);
                FoodWeaponFinalDamage.closePublication(publication, null);
                boolean bl = false;
                return bl;
            }
            finally {
                publication.ownerReserveSelectionOpen = false;
                publication.ownerReserveSelectionQuerying = false;
            }
            if (FoodWeaponFinalDamage.publicationLive(publication) && publication.readiness.matches(publication)) {
                return true;
            }
            FoodWeaponFinalDamage.closePublication(publication, null);
            return false;
        }
    }

    private static void finalizeFailedPublication(PublishingCleanup publishingCleanup) {
        if (!FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.FINALIZING)) {
            return;
        }
        ListedProducer listedProducer = publishingCleanup.producer;
        if (listedProducer instanceof AttributeProducer) {
            AttributeProducer attributeProducer = (AttributeProducer)listedProducer;
            FoodSocketAttributePowers.finishOffensePublication(attributeProducer.consumerPrepared, publishingCleanup);
        } else {
            listedProducer = publishingCleanup.producer;
            if (listedProducer instanceof WolfProducer) {
                WolfProducer wolfProducer = (WolfProducer)listedProducer;
                FoodSocketPowers.finishPackPublication(wolfProducer.consumerPrepared, publishingCleanup);
            } else {
                listedProducer = publishingCleanup.producer;
                if (listedProducer instanceof PassiveProducer) {
                    PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
                    FoodWeaponBossPassive.finishPassiveHitPublication(passiveProducer.consumerPrepared, publishingCleanup);
                }
            }
        }
    }

    private static void retainCleanupFailure(Throwable throwable, Throwable throwable2) {
        if (throwable == throwable2) {
            return;
        }
        try {
            throwable.addSuppressed(throwable2);
        }
        catch (Throwable throwable3) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean localOwnerWriterPublicationIdentity(OffenseReceipt offenseReceipt, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (offenseReceipt == null || originalInteractionBinding == null || attemptBatchQuote == null || attemptBatchAdmission == null || attemptBatchCommit == null || !Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return false;
            }
            Publication publication = offenseReceipt.publication;
            if (publication == null || publication.closed || publication.receipt != offenseReceipt) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return false;
            }
            PreparedBatch preparedBatch = publication.prepared;
            GuardResult guardResult = publication.guardResult;
            ReservationReadiness reservationReadiness = publication.readiness;
            if (preparedBatch == null || guardResult == null || reservationReadiness == null || guardResult.action == null) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return false;
            }
            PreparedListedAction preparedListedAction = guardResult.action;
            EventCapture eventCapture = preparedListedAction.capture;
            if (!preparedBatch.committed || preparedBatch.originalBinding != originalInteractionBinding || preparedBatch.batch != attemptBatchQuote || preparedBatch.admission != attemptBatchAdmission || publication.result != attemptBatchCommit || publication.server == null || preparedBatch.player == null || preparedBatch.source == null || preparedBatch.target == null || preparedBatch.thread != Thread.currentThread() || guardResult.thread != preparedBatch.thread || guardResult.at != preparedBatch.at || guardResult.originalBinding != originalInteractionBinding || guardResult.player != preparedBatch.player || guardResult.source != preparedBatch.source || guardResult.target != preparedBatch.target || preparedListedAction.publication != publication || preparedListedAction.prepared != preparedBatch || preparedListedAction.guardResult != guardResult || preparedListedAction.producers != publication.producers || eventCapture == null || eventCapture.failed || eventCapture.originalBinding != originalInteractionBinding || eventCapture.player != preparedBatch.player || eventCapture.source != preparedBatch.source || eventCapture.target != preparedBatch.target || eventCapture.thread != preparedBatch.thread || eventCapture.at != preparedBatch.at || reservationReadiness.publication != publication || reservationReadiness.prepared != preparedBatch || reservationReadiness.result != attemptBatchCommit || reservationReadiness.producers != publication.producers || !Float.isFinite(publication.amount) || publication.amount <= 0.0f || guardResult.acceptedBits != Float.floatToRawIntBits(publication.amount) || attemptBatchAdmission.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY || attemptBatchCommit.status() != FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return false;
            }
            if (publication.publishingCleanup != null) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return FoodWeaponFinalDamage.localPublishingIdentity(publication.publishingCleanup, PublishingPhase.PUBLISHING);
            }
            if (publication.currentStateProducer != null) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var5_5] (shouldn't be in output)
            ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
            return listedProducerInvocation == null || !listedProducerInvocation.closed && listedProducerInvocation.childCall == null && listedProducerInvocation.publication == publication && listedProducerInvocation.ordinal >= 0 && listedProducerInvocation.ordinal < publication.producers.size() && publication.producers.get(listedProducerInvocation.ordinal) == listedProducerInvocation.producer && listedProducerInvocation.producer.prepared() == preparedBatch;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean localOwnerReserveSelection(Publication publication, OwnerFieldExpectedAuthority.Prepared prepared, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (publication == null || prepared == null || ownerWriterPublication == null || prepared.lineage == null || publication.prepared == null || !Thread.holdsLock(FoodWeaponSourceLedger.class) || publication.prepared.thread != Thread.currentThread() || !publication.ownerReserveSelectionOpen || publication.ownerReserveSelectionInvalid || publication.closed || publication.writerPublication != ownerWriterPublication || prepared.issuer != ownerWriterPublication || prepared.lineage.issuer != ownerWriterPublication || prepared.lineage.publication != publication || prepared.lineage.acceptedCallback != publication.receipt || publication.originalContinuation != null || publication.originalSequence != null || publication.currentStateProducer != null || publication.currentInvocation != null || publication.publishingCleanup != null || publication.incompletePublishingCleanup != null || publication.ownerReservePreparation != null && publication.ownerReservePreparation != prepared || FoodWeaponSourceLedger.ownerFieldPrepared(ownerWriterPublication) != prepared || OwnerFieldExpectedAuthority.session(prepared) != null) {
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return false;
            }
            PreparedBatch preparedBatch = publication.prepared;
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return FoodWeaponFinalDamage.localOwnerWriterPublicationIdentity(publication.receipt, preparedBatch.originalBinding, preparedBatch.batch, preparedBatch.admission, publication.result);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static boolean acceptsOwnerReserveLeaf(OwnerFieldExpectedAuthority.Prepared prepared, Object object, Object object2, Object object3) {
        if (prepared == null) return false;
        if (prepared.lineage == null) return false;
        Object object4 = prepared.lineage.publication;
        if (!(object4 instanceof Publication)) return false;
        Publication publication = (Publication)object4;
        object4 = prepared.issuer;
        if (!(object4 instanceof FoodWeaponSourceLedger.OwnerWriterPublication)) return false;
        FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication = (FoodWeaponSourceLedger.OwnerWriterPublication)object4;
        try {
            boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
                List list = stream.skip(1L).limit(5L).toList();
                return list.size() == 5 && ((StackWalker.StackFrame)list.get(0)).getDeclaringClass() == FoodWeaponSourceLedger.class && ((StackWalker.StackFrame)list.get(0)).getMethodName().equals("ownerNativeLeafAccepted") && ((StackWalker.StackFrame)list.get(1)).getDeclaringClass() == OwnerFieldExpectedAuthority.class && ((StackWalker.StackFrame)list.get(1)).getMethodName().equals("quoteTimers") && ((StackWalker.StackFrame)list.get(2)).getDeclaringClass() == FoodWeaponSourceLedger.class && ((StackWalker.StackFrame)list.get(2)).getMethodName().equals("ownerOriginalPublicationAssigned") && ((StackWalker.StackFrame)list.get(3)).getDeclaringClass() == FoodWeaponFinalDamage.class && ((StackWalker.StackFrame)list.get(3)).getMethodName().contains("ftboss$ownerOriginalPublicationAssigned") && ((StackWalker.StackFrame)list.get(4)).getDeclaringClass() == FoodWeaponFinalDamage.class && ((StackWalker.StackFrame)list.get(4)).getMethodName().equals("reserveListedAction");
            });
            if (!bl) return false;
            if (!FoodWeaponFinalDamage.localOwnerReserveSelection(publication, prepared, ownerWriterPublication)) {
                return false;
            }
            if (publication.ownerReserveSelectionQuerying) {
                publication.ownerReserveSelectionInvalid = true;
                return false;
            }
            publication.ownerReserveSelectionQuerying = true;
            try {
                FoodWeaponHitPlan foodWeaponHitPlan;
                EvolutionProducer evolutionProducer;
                block24: {
                    block23: {
                        if (!FoodWeaponSourceLedger.ownerWriterPublicationContinuation(ownerWriterPublication) || !(object instanceof EvolutionProducer)) break block23;
                        evolutionProducer = (EvolutionProducer)object;
                        if (object2 instanceof FoodWeaponHitPlan && evolutionProducer.plan == (foodWeaponHitPlan = (FoodWeaponHitPlan)object2) && evolutionProducer.prepared() == publication.prepared && evolutionProducer.consumerPrepared != null && evolutionProducer.consumerCommit == null && !evolutionProducer.publishAttempted) break block24;
                    }
                    boolean bl2 = false;
                    return bl2;
                }
                int n = 0;
                for (ListedProducer listedProducer : publication.producers) {
                    if (listedProducer == evolutionProducer) {
                        ++n;
                        continue;
                    }
                    if (listedProducer.plan() != foodWeaponHitPlan) continue;
                    boolean bl3 = false;
                    return bl3;
                }
                Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (n != 1 || !FoodWeaponHitPlan.ownerReserveLeafSelected(evolutionProducer.consumerPrepared, foodWeaponHitPlan, publication.prepared.batch, publication.prepared.admission, publication.result, object3, publication.prepared.player, publication.prepared.source, publication.prepared.at, publication.prepared.requests, publication.prepared.targets)) {
                        boolean bl4 = false;
                        // ** MonitorExit[var10_14] (shouldn't be in output)
                        return bl4;
                    }
                    // ** MonitorExit[var10_14] (shouldn't be in output)
                    if (!(FoodWeaponFinalDamage.localOwnerReserveSelection(publication, prepared, ownerWriterPublication) && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(ownerWriterPublication) && FoodWeaponFinalDamage.localOwnerReserveSelection(publication, prepared, ownerWriterPublication))) {
                        boolean bl5 = false;
                        return bl5;
                    }
                    publication.ownerReservePreparation = prepared;
                    boolean bl6 = true;
                    return bl6;
                }
            }
            finally {
                publication.ownerReserveSelectionQuerying = false;
            }
        }
        catch (Throwable throwable) {
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                if (!publication.ownerReserveSelectionOpen) return false;
                if (publication.prepared == null) return false;
                if (publication.prepared.thread != Thread.currentThread()) return false;
                if (!Thread.holdsLock(FoodWeaponSourceLedger.class)) return false;
                publication.ownerReserveSelectionInvalid = true;
                if (publication.ownerReserveSelectionFailure != null) return false;
                publication.ownerReserveSelectionFailure = throwable;
                // ** MonitorExit[var7_10] (shouldn't be in output)
                return false;
            }
        }
    }

    static boolean matchesOwnerWriterPublication(OffenseReceipt offenseReceipt, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit) {
        if (offenseReceipt == null || !FoodWeaponFinalDamage.extraOwnerAuthorityLive(offenseReceipt.publication)) {
            return false;
        }
        if (!FoodWeaponFinalDamage.localOwnerWriterPublicationIdentity(offenseReceipt, originalInteractionBinding, attemptBatchQuote, attemptBatchAdmission, attemptBatchCommit)) {
            return false;
        }
        Publication publication = offenseReceipt.publication;
        PreparedBatch preparedBatch = publication.prepared;
        ListedProducer listedProducer = publication.currentStateProducer;
        ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
        PublishingCleanup publishingCleanup = publication.publishingCleanup;
        return publication.server == preparedBatch.player.m_284548_().m_7654_() && publication.server.m_18695_() && FoodWeaponFinalDamage.clock(preparedBatch.player) == preparedBatch.at && FoodWeaponFinalDamage.localOwnerWriterPublicationIdentity(offenseReceipt, originalInteractionBinding, attemptBatchQuote, attemptBatchAdmission, attemptBatchCommit) && publication.currentStateProducer == listedProducer && publication.currentInvocation == listedProducerInvocation && publication.publishingCleanup == publishingCleanup;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean localOwnerWriterSelection(OffenseReceipt offenseReceipt, Object object, int n, Object object2, Object object3, int n2) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (offenseReceipt == null || object == null || object2 == null || object3 == null || n < 0 || n2 < 0 || !Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return false;
            }
            Publication publication = offenseReceipt.publication;
            if (publication == null || publication.closed || publication.receipt != offenseReceipt || publication.writerPublication == null || n >= publication.producers.size() || publication.producers.get(n) != object) {
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return false;
            }
            ListedProducer listedProducer = publication.producers.get(n);
            if (listedProducer.prepared() != publication.prepared) {
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return false;
            }
            if (publication.currentStateProducer != null) {
                PublishingCleanup publishingCleanup = publication.publishingCleanup;
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return publication.currentStateProducer == listedProducer && publication.currentInvocation == null && publishingCleanup != null && publishingCleanup.producer == listedProducer && publishingCleanup.ordinal == n && n2 == 0 && object2 == listedProducer.preparedConsumer() && object3 == object2 && publishingCleanup.consumerPrepared == object2 && publishingCleanup.receipt == offenseReceipt && FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING);
            }
            ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
            if (publication.publishingCleanup != null || listedProducerInvocation == null || listedProducerInvocation.closed || listedProducerInvocation.childCall != null || listedProducerInvocation.publication != publication || listedProducerInvocation.producer != listedProducer || listedProducerInvocation.ordinal != n) {
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return false;
            }
            if (listedProducer instanceof MaterialProducer) {
                MaterialProducer materialProducer = (MaterialProducer)listedProducer;
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return object2 == materialProducer.consumerCommit && FoodWeaponHitPlan.matchesOwnerWriterSelectedLeaf(materialProducer.consumerCommit, materialProducer.consumerPrepared, offenseReceipt, object3, n2);
            }
            if (listedProducer instanceof EvolutionProducer) {
                EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return object2 == evolutionProducer.consumerCommit && FoodWeaponHitPlan.matchesOwnerWriterSelectedLeaf(evolutionProducer.consumerCommit, evolutionProducer.consumerPrepared, offenseReceipt, object3, n2);
            }
            // ** MonitorExit[var6_6] (shouldn't be in output)
            Object object4 = FoodWeaponFinalDamage.stateProducerCommit(listedProducer);
            return object4 != null && object2 == object4 && object3 == object4 && n2 == 1;
        }
    }

    static boolean matchesOwnerWriterSelection(OffenseReceipt offenseReceipt, Object object, int n, Object object2, Object object3, int n2) {
        if (FoodWeaponFinalDamage.matchesOriginalConsumerBindingSelection(offenseReceipt, object, n, object2, object3, n2)) {
            return true;
        }
        if (offenseReceipt == null || !FoodWeaponFinalDamage.extraOwnerAuthorityLive(offenseReceipt.publication)) {
            return false;
        }
        if (!FoodWeaponFinalDamage.localOwnerWriterSelection(offenseReceipt, object, n, object2, object3, n2)) {
            return false;
        }
        Publication publication = offenseReceipt.publication;
        PreparedBatch preparedBatch = publication.prepared;
        ListedProducer listedProducer = publication.currentStateProducer;
        ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
        PublishingCleanup publishingCleanup = publication.publishingCleanup;
        return FoodWeaponFinalDamage.matchesOwnerWriterPublication(offenseReceipt, preparedBatch.originalBinding, preparedBatch.batch, preparedBatch.admission, publication.result) && publication.currentStateProducer == listedProducer && publication.currentInvocation == listedProducerInvocation && publication.publishingCleanup == publishingCleanup && FoodWeaponFinalDamage.localOwnerWriterSelection(offenseReceipt, object, n, object2, object3, n2);
    }

    private static boolean selectPublishingWriter(PublishingCleanup publishingCleanup) {
        if (!FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING) || !FoodWeaponSourceLedger.ownerWriterPublicationContinuation(publishingCleanup.publication.writerPublication)) {
            return false;
        }
        if (publishingCleanup.producer instanceof MaterialProducer || publishingCleanup.producer instanceof EvolutionProducer) {
            return true;
        }
        return FoodWeaponSourceLedger.selectOwnerWriterLeaf(publishingCleanup.publication.writerPublication, publishingCleanup.producer, publishingCleanup.ordinal, publishingCleanup.consumerPrepared, publishingCleanup.consumerPrepared, 0);
    }

    private static Object stateProducerCommit(ListedProducer listedProducer) {
        if (listedProducer instanceof AttributeProducer) {
            AttributeProducer attributeProducer = (AttributeProducer)listedProducer;
            return attributeProducer.consumerCommit;
        }
        if (listedProducer instanceof WolfProducer) {
            WolfProducer wolfProducer = (WolfProducer)listedProducer;
            return wolfProducer.consumerCommit;
        }
        if (listedProducer instanceof PassiveProducer) {
            PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
            return passiveProducer.consumerCommit;
        }
        return null;
    }

    private static boolean selectStateInvocationWriter(ListedProducerInvocation listedProducerInvocation) {
        Publication publication = listedProducerInvocation.publication;
        ListedProducer listedProducer = listedProducerInvocation.producer;
        if (FoodWeaponFinalDamage.originalPublicationLive(publication) && publication.originalSequence.original.atProducer(listedProducer)) {
            if (!FoodWeaponFinalDamage.extraOwnerAuthorityLive(publication)) {
                return true;
            }
            try {
                Object object;
                if (!(listedProducer instanceof MaterialProducer || listedProducer instanceof EvolutionProducer || (object = FoodWeaponFinalDamage.stateProducerCommit(listedProducer)) != null && FoodWeaponSourceLedger.selectOwnerWriterLeaf(publication.writerPublication, listedProducer, listedProducerInvocation.ordinal, object, object, 1))) {
                    FoodWeaponFinalDamage.revokeExtraAuthority(publication, null);
                }
            }
            catch (Throwable throwable) {
                FoodWeaponFinalDamage.retainWriterObservationFailure(publication, publication.writerPublication, 0, throwable);
            }
            return true;
        }
        if (listedProducer instanceof MaterialProducer || listedProducer instanceof EvolutionProducer) {
            return true;
        }
        Object object = FoodWeaponFinalDamage.stateProducerCommit(listedProducer);
        if (object == null || listedProducerInvocation.closed || listedProducerInvocation.childCall != null || publication.currentInvocation != listedProducerInvocation || publication.currentStateProducer != null || publication.publishingCleanup != null || !FoodWeaponFinalDamage.publicationLive(publication)) {
            return false;
        }
        return FoodWeaponSourceLedger.selectOwnerWriterLeaf(publication.writerPublication, listedProducer, listedProducerInvocation.ordinal, object, object, 1) && FoodWeaponSourceLedger.ownerWriterContinuation(publication.writerPublication);
    }

    private static boolean producerObservationComplete(ListedProducer listedProducer) {
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            return FoodWeaponHitPlan.timerObservationNotKnownIncomplete(materialProducer.consumerCommit);
        }
        if (listedProducer instanceof EvolutionProducer) {
            EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
            return FoodWeaponHitPlan.timerObservationNotKnownIncomplete(evolutionProducer.consumerCommit);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean bindOwnerOriginalSequence(OffenseReceipt offenseReceipt, FoodWeaponOriginalSequence.Session session) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (offenseReceipt == null || session == null) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            Publication publication = offenseReceipt.publication;
            if (publication.closed || publication.originalSequence != null || publication.originalContinuation != null || publication.currentInvocation != null || publication.currentStateProducer != null || publication.publishingCleanup != null || publication.writerPublication == null || publication.guardResult == null || publication.guardResult.action.capture == null || session.lineage.publication != publication || session.lineage.issuer != publication.writerPublication || session.lineage.acceptedCallback != offenseReceipt || !Thread.holdsLock(FoodWeaponSourceLedger.class) || Thread.currentThread() != publication.prepared.thread || publication.ownerReservePreparation != null && (session.lineage != publication.ownerReservePreparation.lineage || OwnerFieldExpectedAuthority.session(publication.ownerReservePreparation) != session || FoodWeaponSourceLedger.ownerFieldPrepared(publication.writerPublication) != publication.ownerReservePreparation) || !publication.readiness.matches(publication) || !publication.prepared.committed || publication.prepared.originalBinding == null || publication.guardResult.originalBinding != publication.prepared.originalBinding) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            Publication.OriginalContinuation originalContinuation = new Publication.OriginalContinuation(publication, session);
            publication.originalSequence = session;
            publication.originalContinuation = originalContinuation;
            if (!session.original.bindAcceptedPublication(publication, originalContinuation)) {
                publication.originalSequence = null;
                publication.originalContinuation = null;
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean matchesOriginalRunAdmission(FoodWeaponOriginalSequence.OriginalRun originalRun, Object object, Object object2) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Publication publication;
            block6: {
                block5: {
                    if (!(object instanceof Publication)) break block5;
                    publication = (Publication)object;
                    if (object2 instanceof Publication.OriginalContinuation) break block6;
                }
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return false;
            }
            Publication.OriginalContinuation originalContinuation = (Publication.OriginalContinuation)object2;
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return originalRun != null && publication.originalContinuation == originalContinuation && originalContinuation.publication == publication && originalContinuation.sequence == publication.originalSequence && originalContinuation.sequence.original == originalRun && originalRun.lineage == originalContinuation.sequence.lineage && originalRun.lineage.publication == publication && originalRun.lineage.issuer == publication.writerPublication && originalRun.lineage.acceptedCallback == publication.receipt && !originalContinuation.terminal && !publication.closed && originalContinuation.thread == Thread.currentThread() && Thread.holdsLock(FoodWeaponSourceLedger.class) && originalContinuation.prepared == publication.prepared && originalContinuation.receipt == publication.receipt && originalContinuation.result == publication.result && originalContinuation.producers == publication.producers && publication.prepared.committed && publication.readiness.matches(publication) && publication.guardResult != null && publication.guardResult.action.publication == publication && publication.guardResult.action.prepared == publication.prepared && publication.guardResult.action.producers == publication.producers && publication.guardResult.action.guardResult == publication.guardResult && publication.guardResult.acceptedBits == originalContinuation.acceptedBits && Float.floatToRawIntBits(publication.amount) == originalContinuation.acceptedBits && publication.guardResult.originalBinding == publication.prepared.originalBinding && publication.prepared.originalBinding != null;
        }
    }

    static boolean matchesOriginalProducerAdmission(FoodWeaponOriginalSequence.OriginalRun originalRun, Object object, Object object2, Object object3, int n) {
        if (!FoodWeaponFinalDamage.matchesOriginalRunAdmission(originalRun, object, object2)) {
            return false;
        }
        Publication publication = (Publication)object;
        return publication.originalContinuation.applying && publication.currentInvocation == null && publication.currentStateProducer == null && publication.publishingCleanup == null && publication.originalContinuation.admittingProducer == object3 && publication.originalContinuation.admittingOrdinal == n && object3 != null && n >= 0 && n < publication.producers.size() && publication.producers.get(n) == object3;
    }

    static boolean matchesOriginalLeafAdmission(FoodWeaponOriginalSequence.OriginalRun originalRun, Object object, Object object2, Object object3, Object object4, int n) {
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit;
        if (!FoodWeaponFinalDamage.matchesOriginalRunAdmission(originalRun, object, object2)) {
            return false;
        }
        Publication publication = (Publication)object;
        if (object3 instanceof AttributeProducer || object3 instanceof WolfProducer || object3 instanceof PassiveProducer) {
            return FoodWeaponFinalDamage.admitOriginalConsumerLeaf(publication, (ListedProducer)object3, object4, n);
        }
        ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
        if (!publication.originalContinuation.applying || listedProducerInvocation == null || listedProducerInvocation.closed || listedProducerInvocation.publication != publication || listedProducerInvocation.producer != object3 || listedProducerInvocation.childCall != null || publication.currentStateProducer != null || publication.publishingCleanup != null) {
            return false;
        }
        if (object3 instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)object3;
            finalEffectsCommit = materialProducer.consumerCommit;
        } else if (object3 instanceof EvolutionProducer) {
            EvolutionProducer evolutionProducer = (EvolutionProducer)object3;
            finalEffectsCommit = evolutionProducer.consumerCommit;
        } else {
            finalEffectsCommit = null;
        }
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit2 = finalEffectsCommit;
        return FoodWeaponHitPlan.matchesOriginalLeafAdmission(finalEffectsCommit2, publication.originalSequence, object4, n);
    }

    private static boolean bindOriginalProducerCommit(Publication publication, ListedProducer listedProducer) {
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit;
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            finalEffectsCommit = materialProducer.consumerCommit;
        } else if (listedProducer instanceof EvolutionProducer) {
            EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
            finalEffectsCommit = evolutionProducer.consumerCommit;
        } else {
            finalEffectsCommit = null;
        }
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit2 = finalEffectsCommit;
        return finalEffectsCommit2 == null || FoodWeaponHitPlan.bindOriginalSequence(finalEffectsCommit2, publication.originalSequence, publication.receipt);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean enterOriginalProducer(Publication publication, ListedProducer listedProducer, int n) {
        Publication.OriginalContinuation originalContinuation = publication.originalContinuation;
        if (originalContinuation == null || !originalContinuation.applying || originalContinuation.admittingProducer != null) {
            return false;
        }
        originalContinuation.admittingProducer = listedProducer;
        originalContinuation.admittingOrdinal = n;
        try {
            boolean bl = publication.originalSequence.original.enterProducer(publication, listedProducer, n);
            return bl;
        }
        finally {
            originalContinuation.admittingProducer = null;
            originalContinuation.admittingOrdinal = -1;
        }
    }

    private static OriginalConsumerBinding beginOriginalConsumerBinding(PublishingCleanup publishingCleanup) {
        OriginalConsumerBinding originalConsumerBinding;
        if (!(FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING) && FoodWeaponFinalDamage.originalPublicationLive(publishingCleanup.publication) && publishingCleanup.publication.originalSequence.original.atProducer(publishingCleanup.producer) && publishingCleanup.producer.originalConsumer == null)) {
            return null;
        }
        publishingCleanup.producer.originalConsumer = originalConsumerBinding = new OriginalConsumerBinding(publishingCleanup);
        return originalConsumerBinding;
    }

    private static boolean originalConsumerBindingLive(OriginalConsumerBinding originalConsumerBinding) {
        if (originalConsumerBinding == null || originalConsumerBinding.retired) {
            return false;
        }
        PublishingCleanup publishingCleanup = originalConsumerBinding.cleanup;
        Publication publication = publishingCleanup.publication;
        return FoodWeaponFinalDamage.originalPublicationLive(publication) && publishingCleanup.producer.originalConsumer == originalConsumerBinding && originalConsumerBinding.sequence == publication.originalSequence && publishingCleanup.receipt == publication.receipt && publishingCleanup.prepared == publication.prepared && publishingCleanup.consumerPrepared == publishingCleanup.producer.preparedConsumer() && publishingCleanup.ordinal >= 0 && publishingCleanup.ordinal < publication.producers.size() && publication.producers.get(publishingCleanup.ordinal) == publishingCleanup.producer && originalConsumerBinding.sequence.original.atProducer(publishingCleanup.producer);
    }

    private static boolean calledFromG8(Class<?> clazz, String string) {
        try {
            return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass().getNestHost() != FoodWeaponFinalDamage.class && stackFrame.getDeclaringClass().getNestHost() != FoodWeaponOriginalSequence.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && stackFrame.getMethodName().equals(string)).orElse(false));
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static void originalConsumerBindingFailed(PublishingCleanup publishingCleanup, Throwable throwable) {
        OriginalConsumerBinding originalConsumerBinding = publishingCleanup.producer.originalConsumer;
        if (originalConsumerBinding != null && originalConsumerBinding.cleanup == publishingCleanup && originalConsumerBinding.observationFailure == null) {
            originalConsumerBinding.observationFailure = throwable;
        }
        FoodWeaponFinalDamage.revokeExtraAuthority(publishingCleanup.publication, throwable);
    }

    private static void beginOriginalConsumerApply(ListedProducerInvocation listedProducerInvocation, ChildHurtBridge childHurtBridge, Object object) {
        OriginalConsumerBinding originalConsumerBinding = listedProducerInvocation.producer.originalConsumer;
        if (!FoodWeaponFinalDamage.originalConsumerBindingLive(originalConsumerBinding) || !originalConsumerBinding.bound || !originalConsumerBinding.publishExited || !originalConsumerBinding.publishMatched || originalConsumerBinding.consumerRetired || originalConsumerBinding.applying || originalConsumerBinding.binding || originalConsumerBinding.commit != object || object == null || !FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge) || childHurtBridge.invocation != listedProducerInvocation || listedProducerInvocation.childCall != null) {
            return;
        }
        originalConsumerBinding.bridge = childHurtBridge;
        originalConsumerBinding.applying = true;
    }

    private static void finishOriginalConsumerApply(ListedProducerInvocation listedProducerInvocation, ChildHurtBridge childHurtBridge) {
        OriginalConsumerBinding originalConsumerBinding = listedProducerInvocation.producer.originalConsumer;
        if (originalConsumerBinding == null || originalConsumerBinding.bridge != childHurtBridge || originalConsumerBinding.cleanup.publication != listedProducerInvocation.publication) {
            return;
        }
        originalConsumerBinding.applying = false;
        originalConsumerBinding.bridge = null;
    }

    private static boolean originalConsumerApplyLive(ChildHurtBridge childHurtBridge) {
        if (!FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge)) {
            return false;
        }
        OriginalConsumerBinding originalConsumerBinding = childHurtBridge.invocation.producer.originalConsumer;
        return FoodWeaponFinalDamage.originalConsumerBindingLive(originalConsumerBinding) && originalConsumerBinding.bound && originalConsumerBinding.applying && originalConsumerBinding.bridge == childHurtBridge && originalConsumerBinding.commit == FoodWeaponFinalDamage.stateProducerCommit(childHurtBridge.invocation.producer) && originalConsumerBinding.sequence.original.atLeaf(originalConsumerBinding.leaf);
    }

    private static boolean matchesOriginalConsumerBindingSelection(OffenseReceipt offenseReceipt, Object object, int n, Object object2, Object object3, int n2) {
        if (offenseReceipt == null || !(object instanceof ListedProducer)) {
            return false;
        }
        ListedProducer listedProducer = (ListedProducer)object;
        OriginalConsumerBinding originalConsumerBinding = listedProducer.originalConsumer;
        if (!FoodWeaponFinalDamage.originalConsumerBindingLive(originalConsumerBinding) || !originalConsumerBinding.binding || originalConsumerBinding.bound || originalConsumerBinding.cleanup.receipt != offenseReceipt || originalConsumerBinding.cleanup.ordinal != n || object2 != originalConsumerBinding.cleanup.consumerPrepared || object3 != object2 || n2 != 0 || !FoodWeaponFinalDamage.localPublishingIdentity(originalConsumerBinding.cleanup, PublishingPhase.PUBLISHING)) {
            return false;
        }
        return FoodWeaponFinalDamage.calledFromG8(FoodWeaponConsumerSequence.class, "bind");
    }

    private static boolean admitOriginalConsumerLeaf(Publication publication, ListedProducer listedProducer, Object object, int n) {
        OriginalConsumerBinding originalConsumerBinding = listedProducer.originalConsumer;
        if (!FoodWeaponFinalDamage.originalConsumerBindingLive(originalConsumerBinding) || originalConsumerBinding.cleanup.publication != publication || !(object instanceof FoodWeaponConsumerSequence.Accepted)) {
            return false;
        }
        if (n == 0) {
            if (!originalConsumerBinding.binding || originalConsumerBinding.bound || originalConsumerBinding.leaf != null || !FoodWeaponFinalDamage.localPublishingIdentity(originalConsumerBinding.cleanup, PublishingPhase.PUBLISHING) || !FoodWeaponFinalDamage.calledFromG8(FoodWeaponConsumerSequence.class, "bind")) {
                return false;
            }
            originalConsumerBinding.leaf = object;
            return true;
        }
        ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
        return n == 1 && originalConsumerBinding.bound && originalConsumerBinding.publishExited && originalConsumerBinding.publishMatched && !originalConsumerBinding.consumerRetired && !originalConsumerBinding.binding && originalConsumerBinding.applying && originalConsumerBinding.leaf == object && listedProducerInvocation != null && !listedProducerInvocation.closed && listedProducerInvocation.childCall == null && listedProducerInvocation.publication == publication && listedProducerInvocation.producer == listedProducer && listedProducerInvocation.ordinal == originalConsumerBinding.cleanup.ordinal && originalConsumerBinding.bridge != null && !originalConsumerBinding.bridge.closed && originalConsumerBinding.bridge.invocation == listedProducerInvocation && originalConsumerBinding.commit != null && originalConsumerBinding.commit == FoodWeaponFinalDamage.stateProducerCommit(listedProducer) && publication.currentStateProducer == null && publication.publishingCleanup == null && FoodWeaponFinalDamage.calledFromG8(FoodWeaponConsumerSequence.Accepted.class, "enter");
    }

    private static boolean hasBoundOriginalAttributeConsumer(ChildHurtBridge childHurtBridge) {
        return childHurtBridge != null && childHurtBridge.invocation != null && childHurtBridge.invocation.producer instanceof AttributeProducer && childHurtBridge.invocation.producer.originalConsumer != null && childHurtBridge.invocation.producer.originalConsumer.bound;
    }

    private static boolean matchesOriginalConsumerChild(ChildHurtCall childHurtCall) {
        if (!FoodWeaponFinalDamage.hasBoundOriginalAttributeConsumer(childHurtCall.bridge)) {
            return childHurtCall.originalConsumerChild == null;
        }
        OriginalConsumerBinding originalConsumerBinding = childHurtCall.bridge.invocation.producer.originalConsumer;
        return childHurtCall.originalConsumerChild != null && childHurtCall.originalConsumerChildFailure == null && FoodWeaponFinalDamage.originalConsumerApplyLive(childHurtCall.bridge) && childHurtCall.originalBinding == originalConsumerBinding.cleanup.originalBinding;
    }

    static FoodWeaponConsumerSequence.OriginalChild originalConsumerChild(ChildHurtCall childHurtCall) {
        return FoodWeaponFinalDamage.matchesAcceptedOriginalChild(childHurtCall) ? childHurtCall.originalConsumerChild : null;
    }

    private static boolean originalPublishingExitIdentity(OriginalConsumerBinding originalConsumerBinding, PublishingPhase publishingPhase) {
        if (originalConsumerBinding == null || originalConsumerBinding.consumerRetired || originalConsumerBinding.leaf == null) {
            return false;
        }
        PublishingCleanup publishingCleanup = originalConsumerBinding.cleanup;
        Publication publication = publishingCleanup.publication;
        Publication.OriginalContinuation originalContinuation = publication.originalContinuation;
        return originalContinuation != null && publishingCleanup.producer.originalConsumer == originalConsumerBinding && originalConsumerBinding.sequence == originalContinuation.sequence && !originalConsumerBinding.binding && !originalConsumerBinding.applying && originalConsumerBinding.bridge == null && FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, publishingPhase) && FoodWeaponFinalDamage.matchesOriginalRunAdmission(originalConsumerBinding.sequence.original, publication, originalContinuation);
    }

    private static void finishOriginalConsumerPublication(OriginalConsumerBinding originalConsumerBinding, Object object, boolean bl, Throwable throwable) {
        if (originalConsumerBinding == null) {
            return;
        }
        originalConsumerBinding.commit = object;
        originalConsumerBinding.publishExited = true;
        originalConsumerBinding.publishMatched = bl;
        if (originalConsumerBinding.publicationOriginalFailure == null) {
            originalConsumerBinding.publicationOriginalFailure = throwable;
        }
        if (!FoodWeaponFinalDamage.originalPublishingExitIdentity(originalConsumerBinding, PublishingPhase.PUBLISHING)) {
            return;
        }
        if (originalConsumerBinding.sequence.original.publishingLeafState(originalConsumerBinding.cleanup.producer, originalConsumerBinding.cleanup.ordinal, originalConsumerBinding.leaf) != 1) {
            return;
        }
        try {
            ((FoodWeaponConsumerSequence.Accepted)originalConsumerBinding.leaf).published(originalConsumerBinding.cleanup.consumerPrepared, object);
        }
        catch (Throwable throwable2) {
            originalConsumerBinding.publicationCloseFailure = throwable2;
            FoodWeaponFinalDamage.originalConsumerBindingFailed(originalConsumerBinding.cleanup, throwable2);
        }
    }

    private static void retireOriginalConsumerPublication(PublishingCleanup publishingCleanup) {
        OriginalConsumerBinding originalConsumerBinding = publishingCleanup.producer.originalConsumer;
        if (originalConsumerBinding == null || originalConsumerBinding.cleanup != publishingCleanup) {
            return;
        }
        try {
            if (!FoodWeaponFinalDamage.originalPublishingExitIdentity(originalConsumerBinding, PublishingPhase.FINALIZING)) {
                return;
            }
            if (originalConsumerBinding.sequence.original.publishingLeafState(publishingCleanup.producer, publishingCleanup.ordinal, originalConsumerBinding.leaf) < 0) {
                return;
            }
            ((FoodWeaponConsumerSequence.Accepted)originalConsumerBinding.leaf).retired(publishingCleanup.consumerPrepared);
            originalConsumerBinding.consumerRetired = true;
        }
        catch (Throwable throwable) {
            originalConsumerBinding.publicationCloseFailure = throwable;
            FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, throwable);
        }
        finally {
            originalConsumerBinding.retired = true;
        }
    }

    private static boolean originalPublicationLive(Publication publication) {
        return publication != null && publication.originalContinuation != null && publication.originalContinuation.live();
    }

    private static boolean extraOwnerAuthorityLive(Publication publication) {
        return !(publication == null || publication.extraAuthorityRevoked || publication.originalContinuation != null && !publication.originalContinuation.live() || publication.originalSequence != null && !publication.originalSequence.extraAuthorityRetained());
    }

    private static boolean originalReceiptLive(Publication publication) {
        if (!FoodWeaponFinalDamage.originalPublicationLive(publication) || !publication.originalContinuation.applying) {
            return false;
        }
        if (publication.currentStateProducer != null) {
            PublishingCleanup publishingCleanup = publication.publishingCleanup;
            return publishingCleanup != null && publishingCleanup.phase == PublishingPhase.PUBLISHING && publication.originalSequence.original.atProducer(publication.currentStateProducer) && FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING);
        }
        ListedProducerInvocation listedProducerInvocation = publication.currentInvocation;
        return listedProducerInvocation != null && !listedProducerInvocation.closed && listedProducerInvocation.childCall == null && listedProducerInvocation.publication == publication && listedProducerInvocation.producer.prepared == publication.prepared && listedProducerInvocation.ordinal >= 0 && listedProducerInvocation.ordinal < publication.producers.size() && publication.producers.get(listedProducerInvocation.ordinal) == listedProducerInvocation.producer && publication.originalSequence.original.atProducer(listedProducerInvocation.producer);
    }

    private static void revokeExtraAuthority(Publication publication, Throwable throwable) {
        if (publication == null) {
            return;
        }
        publication.extraAuthorityRevoked = true;
        FoodWeaponOriginalSequence.Session session = publication.originalSequence;
        if (session != null) {
            session.interferenceObserved(session.lineage.ownerSelection, session.lineage.generation, throwable);
        }
    }

    private static boolean originalInvocationLive(ChildHurtBridge childHurtBridge) {
        if (childHurtBridge == null || childHurtBridge.closed || childHurtBridge.invocation == null || childHurtBridge.invocation.closed) {
            return false;
        }
        ListedProducerInvocation listedProducerInvocation = childHurtBridge.invocation;
        Publication publication = listedProducerInvocation.publication;
        return FoodWeaponFinalDamage.originalPublicationLive(publication) && publication.originalContinuation.applying && publication.currentInvocation == listedProducerInvocation && publication.currentStateProducer == null && publication.publishingCleanup == null && listedProducerInvocation.ordinal >= 0 && listedProducerInvocation.ordinal < publication.producers.size() && publication.producers.get(listedProducerInvocation.ordinal) == listedProducerInvocation.producer && listedProducerInvocation.producer.prepared == publication.prepared && publication.originalSequence.original.atProducer(listedProducerInvocation.producer) && FoodWeaponFinalDamage.producerPublished(listedProducerInvocation.producer, publication);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesOriginalHitPlanLeaf(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, ChildHurtBridge childHurtBridge, Object object, int n) {
        block5: {
            ListedProducer listedProducer;
            block4: {
                if (finalEffectsCommit == null) return false;
                if (!FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge)) return false;
                if (childHurtBridge.invocation.childCall != null) return false;
                if (!childHurtBridge.invocation.publication.originalSequence.original.atLeaf(object)) {
                    return false;
                }
                listedProducer = childHurtBridge.invocation.producer;
                if (!(listedProducer instanceof MaterialProducer)) break block4;
                MaterialProducer materialProducer = (MaterialProducer)listedProducer;
                if (materialProducer.consumerCommit == finalEffectsCommit) break block5;
            }
            if (!(listedProducer instanceof EvolutionProducer)) return false;
            EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
            if (evolutionProducer.consumerCommit != finalEffectsCommit) return false;
        }
        boolean bl = true;
        boolean bl2 = bl;
        if (!bl2) return false;
        if (!FoodWeaponHitPlan.matchesOriginalSelectedFinalLeaf(finalEffectsCommit, object, n)) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesOriginalFinalPreparation(FoodWeaponHitPlan.PreparedFinalEffects preparedFinalEffects, OffenseReceipt offenseReceipt) {
        if (preparedFinalEffects == null) return false;
        if (offenseReceipt == null) return false;
        if (!FoodWeaponFinalDamage.originalReceiptLive(offenseReceipt.publication)) {
            return false;
        }
        ListedProducer listedProducer = offenseReceipt.publication.currentStateProducer;
        if (listedProducer instanceof MaterialProducer) {
            MaterialProducer materialProducer = (MaterialProducer)listedProducer;
            if (materialProducer.consumerPrepared == preparedFinalEffects) return true;
        }
        if (!(listedProducer instanceof EvolutionProducer)) return false;
        EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
        if (evolutionProducer.consumerPrepared != preparedFinalEffects) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static FoodWeaponOriginalSequence.Session originalHitPlanSequence(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, ChildHurtBridge childHurtBridge) {
        block5: {
            ListedProducer listedProducer;
            block4: {
                if (finalEffectsCommit == null) return null;
                if (!FoodWeaponFinalDamage.originalInvocationLive(childHurtBridge)) return null;
                if (childHurtBridge.invocation.childCall != null) {
                    return null;
                }
                listedProducer = childHurtBridge.invocation.producer;
                if (!(listedProducer instanceof MaterialProducer)) break block4;
                MaterialProducer materialProducer = (MaterialProducer)listedProducer;
                if (materialProducer.consumerCommit == finalEffectsCommit) break block5;
            }
            if (!(listedProducer instanceof EvolutionProducer)) return null;
            EvolutionProducer evolutionProducer = (EvolutionProducer)listedProducer;
            if (evolutionProducer.consumerCommit != finalEffectsCommit) return null;
        }
        boolean bl = true;
        boolean bl2 = bl;
        if (!bl2) return null;
        FoodWeaponOriginalSequence.Session session = childHurtBridge.invocation.publication.originalSequence;
        return session;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesAcceptedOriginalOffense(FoodSocketAttributePowers.PreparedOffense preparedOffense, OffenseReceipt offenseReceipt) {
        if (offenseReceipt == null) return false;
        if (!FoodWeaponFinalDamage.originalReceiptLive(offenseReceipt.publication)) return false;
        ListedProducer listedProducer = offenseReceipt.publication.currentStateProducer;
        if (!(listedProducer instanceof AttributeProducer)) return false;
        AttributeProducer attributeProducer = (AttributeProducer)listedProducer;
        if (attributeProducer.consumerPrepared != preparedOffense) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesAcceptedOriginalPack(FoodSocketPowers.PreparedPack preparedPack, OffenseReceipt offenseReceipt) {
        if (offenseReceipt == null) return false;
        if (!FoodWeaponFinalDamage.originalReceiptLive(offenseReceipt.publication)) return false;
        ListedProducer listedProducer = offenseReceipt.publication.currentStateProducer;
        if (!(listedProducer instanceof WolfProducer)) return false;
        WolfProducer wolfProducer = (WolfProducer)listedProducer;
        if (wolfProducer.consumerPrepared != preparedPack) return false;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static boolean matchesAcceptedOriginalPassive(FoodWeaponBossPassive.PreparedHit preparedHit, OffenseReceipt offenseReceipt) {
        if (offenseReceipt == null) return false;
        if (!FoodWeaponFinalDamage.originalReceiptLive(offenseReceipt.publication)) return false;
        ListedProducer listedProducer = offenseReceipt.publication.currentStateProducer;
        if (!(listedProducer instanceof PassiveProducer)) return false;
        PassiveProducer passiveProducer = (PassiveProducer)listedProducer;
        if (passiveProducer.consumerPrepared != preparedHit) return false;
        return true;
    }

    static boolean matchesAcceptedOriginalChild(ChildHurtCall childHurtCall) {
        return childHurtCall != null && !childHurtCall.closed && childHurtCall.thread == Thread.currentThread() && FoodWeaponFinalDamage.originalInvocationLive(childHurtCall.bridge) && childHurtCall.bridge.invocation.childCall == childHurtCall && childHurtCall.originalBinding == childHurtCall.bridge.invocation.publication.prepared.originalBinding && FoodWeaponFinalDamage.matchesOriginalConsumerChild(childHurtCall) && childHurtCall.source != null && childHurtCall.target != null && Float.isFinite(childHurtCall.amount) && childHurtCall.amount > 0.0f;
    }

    static FoodWeaponSourceLedger.OwnerWriterPublication packPublishingWriter(FoodSocketPowers.PreparedPack preparedPack, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
        if (!(FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, publishingCleanup) && FoodWeaponFinalDamage.receiptLive(publishingCleanup.publication) && FoodWeaponSourceLedger.ownerWriterContinuation(publishingCleanup.publication.writerPublication))) {
            return null;
        }
        return FoodWeaponFinalDamage.localPublishingIdentity(publishingCleanup, PublishingPhase.PUBLISHING) ? publishingCleanup.publication.writerPublication : null;
    }

    static FoodWeaponSourceLedger.OwnerWriterPublication selectHitPlanWriterLeaf(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, ChildHurtBridge childHurtBridge, Object object, int n) {
        if (FoodWeaponFinalDamage.matchesOriginalHitPlanLeaf(finalEffectsCommit, childHurtBridge, object, n)) {
            Publication publication = childHurtBridge.invocation.publication;
            if (FoodWeaponFinalDamage.extraOwnerAuthorityLive(publication)) {
                try {
                    if (!FoodWeaponSourceLedger.selectOwnerWriterLeaf(publication.writerPublication, childHurtBridge.invocation.producer, childHurtBridge.invocation.ordinal, finalEffectsCommit, object, n)) {
                        FoodWeaponFinalDamage.revokeExtraAuthority(publication, null);
                    }
                }
                catch (Throwable throwable) {
                    FoodWeaponFinalDamage.retainWriterObservationFailure(publication, publication.writerPublication, 0, throwable);
                }
            }
            return publication.writerPublication;
        }
        if (!FoodWeaponFinalDamage.matchesHitPlanSelectionPhase(finalEffectsCommit, childHurtBridge) || !FoodWeaponHitPlan.matchesSelectedFinalLeaf(finalEffectsCommit, object, n)) {
            return null;
        }
        ListedProducerInvocation listedProducerInvocation = childHurtBridge.invocation;
        Publication publication = listedProducerInvocation.publication;
        if (!FoodWeaponFinalDamage.publicationLive(publication) || !FoodWeaponSourceLedger.selectOwnerWriterLeaf(publication.writerPublication, listedProducerInvocation.producer, listedProducerInvocation.ordinal, finalEffectsCommit, object, n)) {
            return null;
        }
        return FoodWeaponFinalDamage.matchesHitPlanInvocation(finalEffectsCommit, childHurtBridge) && FoodWeaponHitPlan.matchesSelectedFinalLeaf(finalEffectsCommit, object, n) ? publication.writerPublication : null;
    }

    private static void retainWriterObservationFailure(Publication publication, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, int n, Throwable throwable) {
        block13: {
            if (publication == null) {
                return;
            }
            if (FoodWeaponFinalDamage.originalPublicationLive(publication)) {
                FoodWeaponFinalDamage.revokeExtraAuthority(publication, throwable);
            } else {
                publication.closed = true;
            }
            if (ownerWriterPublication != null && publication.incompleteWriterPublication == null) {
                publication.incompleteWriterPublication = ownerWriterPublication;
            }
            switch (n) {
                case 0: {
                    if (publication.writerPlanningObservationFailure != null) break;
                    publication.writerPlanningObservationFailure = throwable;
                    break;
                }
                case 1: {
                    if (publication.writerNormalObservationFailure != null) break;
                    publication.writerNormalObservationFailure = throwable;
                    break;
                }
                case 2: {
                    if (publication.writerExceptionObservationFailure != null) break;
                    publication.writerExceptionObservationFailure = throwable;
                    break;
                }
                case 3: {
                    if (publication.writerCloseObservationFailure != null) break;
                    publication.writerCloseObservationFailure = throwable;
                }
            }
            if (ownerWriterPublication == null) {
                return;
            }
            try {
                FoodWeaponSourceLedger.ownerPublicationObservationFailed(ownerWriterPublication, n, throwable);
            }
            catch (Throwable throwable2) {
                if (publication.writerFailureRecordingFailure != null) break block13;
                publication.writerFailureRecordingFailure = throwable2;
            }
        }
    }

    private static void closePublication(Publication publication, Throwable throwable) {
        if (publication == null) {
            return;
        }
        for (ListedProducer listedProducer : publication.producers) {
            OriginalConsumerBinding originalConsumerBinding = listedProducer.originalConsumer;
            if (originalConsumerBinding == null || originalConsumerBinding.cleanup.publication != publication) continue;
            originalConsumerBinding.retired = true;
        }
        FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication = publication.writerPublication;
        publication.writerPublication = null;
        publication.closed = true;
        if (throwable != null && publication.writerOriginalFailure == null) {
            publication.writerOriginalFailure = throwable;
        }
        if (ownerWriterPublication == null) {
            return;
        }
        if (throwable != null) {
            try {
                FoodWeaponSourceLedger.ownerPublicationOriginalFailed(ownerWriterPublication, throwable);
            }
            catch (Throwable throwable2) {
                FoodWeaponFinalDamage.retainWriterObservationFailure(publication, ownerWriterPublication, 2, throwable2);
            }
        }
        try {
            FoodWeaponSourceLedger.closeOwnerWriterPublication(ownerWriterPublication);
        }
        catch (Throwable throwable3) {
            publication.writerCleanupFailure = throwable3;
            FoodWeaponFinalDamage.retainWriterObservationFailure(publication, ownerWriterPublication, 3, throwable3);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean publishListedProducer(Publication publication, ListedProducer listedProducer, int n) {
        PublishingCleanup publishingCleanup;
        if (publication == null || listedProducer == null || publication.closed || listedProducer.publishAttempted || publication.publishingCleanup != null || publication.currentStateProducer != null || publication.currentInvocation != null || listedProducer.prepared() != publication.prepared || n < 0 || n >= publication.producers.size() || publication.producers.get(n) != listedProducer || publication.guardResult == null || publication.guardResult.action.capture == null) {
            return false;
        }
        listedProducer.publishAttempted = true;
        publication.publishingCleanup = publishingCleanup = new PublishingCleanup(publication, listedProducer, n);
        publication.currentStateProducer = listedProducer;
        boolean bl = false;
        Throwable throwable = null;
        try {
            boolean bl2 = FoodWeaponFinalDamage.originalPublicationLive(publication);
            if (bl2) {
                try {
                    if (FoodWeaponFinalDamage.extraOwnerAuthorityLive(publication) && !FoodWeaponFinalDamage.selectPublishingWriter(publishingCleanup)) {
                        FoodWeaponFinalDamage.revokeExtraAuthority(publication, null);
                    }
                }
                catch (Throwable throwable2) {
                    FoodWeaponFinalDamage.retainWriterObservationFailure(publication, publication.writerPublication, 0, throwable2);
                }
            } else if (!FoodWeaponFinalDamage.selectPublishingWriter(publishingCleanup)) {
                boolean bl3 = false;
                return bl3;
            }
            boolean bl4 = bl = listedProducer.publish(publication.amount, publication.receipt, publishingCleanup) && FoodWeaponFinalDamage.producerPublished(listedProducer, publication);
            return bl4;
        }
        catch (Throwable throwable3) {
            throwable = throwable3;
            throw throwable3;
        }
        finally {
            try {
                if (!bl) {
                    publishingCleanup.phase = PublishingPhase.FINALIZING;
                    try {
                        FoodWeaponFinalDamage.finalizeFailedPublication(publishingCleanup);
                    }
                    catch (Throwable throwable4) {
                        publishingCleanup.cleanupFailure = throwable4;
                        publication.incompletePublishingCleanup = publishingCleanup;
                        if (throwable != null) {
                            FoodWeaponFinalDamage.retainCleanupFailure(throwable, throwable4);
                        }
                    }
                }
            }
            finally {
                if (!bl) {
                    FoodWeaponFinalDamage.retireOriginalConsumerPublication(publishingCleanup);
                }
                publishingCleanup.phase = PublishingPhase.CLOSED;
                if (publication.publishingCleanup == publishingCleanup) {
                    publication.publishingCleanup = null;
                }
                if (publication.currentStateProducer == listedProducer) {
                    publication.currentStateProducer = null;
                }
            }
        }
    }

    /*
     * Exception decompiling
     */
    private static void applyListedEffects(PreparedListedAction var0, float var1_1) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [1[TRYBLOCK]], but top level block is 48[MONITOR]
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

    private static PreparedListedAction collectListedProducers(PreparedBatch preparedBatch, FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponHitPlan foodWeaponHitPlan2, FoodSocketAttributePowers.OffensePlan offensePlan, FoodSocketPowers.PackPlan packPlan, FoodWeaponBossPassive.HitPlan hitPlan, List<ProducerFamily> list) {
        if (preparedBatch == null || !preparedBatch.stable() || list == null || list.size() != ProducerFamily.values().length || Set.copyOf(list).size() != ProducerFamily.values().length) {
            return null;
        }
        ArrayList<ListedProducer> arrayList = new ArrayList<ListedProducer>();
        ArrayList<FoodWeaponSourceLedger.AttemptRequest> arrayList2 = new ArrayList<FoodWeaponSourceLedger.AttemptRequest>();
        ArrayList<FoodWeaponSourceLedger.TargetRequest> arrayList3 = new ArrayList<FoodWeaponSourceLedger.TargetRequest>();
        for (ProducerFamily producerFamily : list) {
            switch (producerFamily) {
                case MATERIAL: {
                    if (foodWeaponHitPlan == null) break;
                    Object object = FoodWeaponHitPlan.prepareFinalEffects(foodWeaponHitPlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets, preparedBatch.player, preparedBatch.source);
                    if (object == null) {
                        return null;
                    }
                    arrayList.add(new MaterialProducer(preparedBatch, foodWeaponHitPlan, (FoodWeaponHitPlan.PreparedFinalEffects)object));
                    for (FoodWeaponHitPlan.Attempt attempt : foodWeaponHitPlan.attempts()) {
                        arrayList2.add(new FoodWeaponSourceLedger.AttemptRequest(attempt.effect(), attempt.chance()));
                    }
                    arrayList3.addAll(foodWeaponHitPlan.targets());
                    break;
                }
                case EVOLUTION: {
                    if (foodWeaponHitPlan2 == null) break;
                    Object object = FoodWeaponHitPlan.prepareFinalEffects(foodWeaponHitPlan2, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets, preparedBatch.player, preparedBatch.source);
                    if (object == null) {
                        return null;
                    }
                    arrayList.add(new EvolutionProducer(preparedBatch, foodWeaponHitPlan2, (FoodWeaponHitPlan.PreparedFinalEffects)object));
                    for (FoodWeaponHitPlan.Attempt attempt : foodWeaponHitPlan2.attempts()) {
                        arrayList2.add(new FoodWeaponSourceLedger.AttemptRequest(attempt.effect(), attempt.chance()));
                    }
                    arrayList3.addAll(foodWeaponHitPlan2.targets());
                    break;
                }
                case ATTRIBUTE: {
                    if (offensePlan == null) break;
                    Object object = FoodSocketAttributePowers.prepareOffense(offensePlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                    if (object == null) {
                        return null;
                    }
                    arrayList.add(new AttributeProducer(preparedBatch, offensePlan, (FoodSocketAttributePowers.PreparedOffense)object));
                    arrayList2.addAll(offensePlan.attempts());
                    break;
                }
                case WOLF: {
                    if (packPlan == null) break;
                    Object object = FoodSocketPowers.preparePack(packPlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                    if (!FoodSocketPowers.matchesPreparedPack((FoodSocketPowers.PreparedPack)object, packPlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets)) {
                        return null;
                    }
                    arrayList.add(new WolfProducer(preparedBatch, packPlan, (FoodSocketPowers.PreparedPack)object));
                    arrayList2.addAll(packPlan.attempts());
                    arrayList3.addAll(packPlan.targets());
                    break;
                }
                case PASSIVE: {
                    if (hitPlan == null) break;
                    Object object = FoodWeaponBossPassive.preparePassiveHit(hitPlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                    if (!FoodWeaponBossPassive.matchesPreparedPassiveHit((FoodWeaponBossPassive.PreparedHit)object, hitPlan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets)) {
                        return null;
                    }
                    arrayList.add(new PassiveProducer(preparedBatch, hitPlan, (FoodWeaponBossPassive.PreparedHit)object));
                    arrayList2.addAll(hitPlan.attempts());
                    arrayList3.addAll(hitPlan.targets());
                }
            }
        }
        if (!preparedBatch.requests.equals(arrayList2) || !preparedBatch.targets.equals(arrayList3)) {
            return null;
        }
        return new PreparedListedAction(preparedBatch, arrayList);
    }

    private static PreparedBatch admitBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, DamageSource damageSource, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return FoodWeaponFinalDamage.admitTargetBatch(compoundTag, serverPlayer, damageSource, null, list, list2);
    }

    private static boolean sourceMatchesTarget(CompoundTag compoundTag, ServerPlayer serverPlayer, DamageSource damageSource, LivingEntity livingEntity) {
        if (compoundTag == null || serverPlayer == null || damageSource == null) {
            return false;
        }
        if (compoundTag.m_128403_("NativeHurtId")) {
            return livingEntity != null && FoodWeaponSourceLedger.matchesNativeHurt(damageSource, serverPlayer, (Entity)livingEntity, compoundTag);
        }
        return damageSource.getClass() == DamageSource.class;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static PreparedBatch admitTargetBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, DamageSource damageSource, LivingEntity livingEntity, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        Object object = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            long l;
            if (!Thread.holdsLock(FoodWeaponSourceLedger.class)) {
                // ** MonitorExit[var6_6] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var6_6] (shouldn't be in output)
            if (!(damageSource != null && serverPlayer != null && serverPlayer.getClass() == ServerPlayer.class && serverPlayer.m_284548_().m_7654_().m_18695_() && FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer) && damageSource.m_7639_() == serverPlayer && FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer).equals((Object)compoundTag) && FoodWeaponFinalDamage.sourceMatchesTarget(compoundTag, serverPlayer, damageSource, livingEntity))) {
                return null;
            }
            object = FoodWeaponSourceLedger.quoteAttemptBatch(compoundTag, serverPlayer, list, list2);
            FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission = FoodWeaponSourceLedger.admitAttemptBatch(compoundTag, serverPlayer, (FoodWeaponSourceLedger.AttemptBatchQuote)object, list, list2, l = FoodWeaponFinalDamage.clock(serverPlayer));
            if (attemptBatchAdmission.status() != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY) {
                return null;
            }
            PreparedBatch preparedBatch = new PreparedBatch(compoundTag, serverPlayer, damageSource, livingEntity, (FoodWeaponSourceLedger.AttemptBatchQuote)object, attemptBatchAdmission, list, list2);
            return preparedBatch.stable() ? preparedBatch : null;
        }
    }

    private static boolean defenseReceiverReady() {
        return false;
    }

    private static long clock(ServerPlayer serverPlayer) {
        return serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
    }

    private static String foundationFingerprint(ServerPlayer serverPlayer, ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? new FoundationApprovedRules().fingerprint(serverPlayer, itemStack) : "LEGACY";
    }

    private static PreparedDefense prepareDefense(ServerPlayer serverPlayer, DamageSource damageSource, float f) {
        InteractionHand interactionHand;
        if (serverPlayer.getClass() != ServerPlayer.class || !serverPlayer.m_284548_().m_7654_().m_18695_() || serverPlayer.m_9236_().m_5776_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || !FoodWeaponFinalDamage.incomingSourceAllowed(serverPlayer, damageSource) || FoodWeaponBondHits.secondary(damageSource) || damageSource.m_269533_(DamageTypeTags.f_268738_)) {
            return null;
        }
        FoodSocketAttributePowers.DefenseQuote defenseQuote = FoodSocketAttributePowers.quoteDefense(serverPlayer, damageSource, f);
        FoodSocketAttributePowers.DefensePlan defensePlan = defenseQuote.plan();
        if (defenseQuote.rejection() != FoodSocketAttributePowers.DefenseRejection.NONE || defensePlan == null || defensePlan.input() != f || !Float.isFinite(defensePlan.amount()) || defensePlan.amount() < 0.0f || !Float.isFinite(defensePlan.blocked()) || defensePlan.blocked() <= 0.0f || defensePlan.blocked() > f || f - defensePlan.blocked() != defensePlan.amount() || defenseQuote.amount() != defensePlan.amount()) {
            return null;
        }
        ItemStack itemStack = defensePlan.actualWeapon();
        if (!defensePlan.matchesContext(serverPlayer, damageSource, itemStack, interactionHand = defensePlan.originalHand(), defensePlan.quotedAt())) {
            return null;
        }
        String string = FoodWeaponFinalDamage.foundationFingerprint(serverPlayer, itemStack);
        if (string == null || string.isBlank()) {
            return null;
        }
        PreparedDefense preparedDefense = new PreparedDefense(defensePlan, serverPlayer, damageSource, itemStack, interactionHand, string);
        return FoodWeaponFinalDamage.incomingSourceMatches(preparedDefense, serverPlayer, damageSource) ? preparedDefense : null;
    }

    private static boolean incomingSourceAllowed(ServerPlayer serverPlayer, DamageSource damageSource) {
        ServerPlayer serverPlayer2;
        if (serverPlayer == null || damageSource == null) {
            return false;
        }
        Entity entity = damageSource.m_7639_();
        if (entity instanceof ServerPlayer && (entity = FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer2 = (ServerPlayer)entity)).m_128403_("NativeHurtId")) {
            return FoodWeaponSourceLedger.matchesNativeHurt(damageSource, serverPlayer2, (Entity)serverPlayer, (CompoundTag)entity);
        }
        return damageSource.getClass() == DamageSource.class;
    }

    private static boolean incomingSourceMatches(PreparedDefense preparedDefense, ServerPlayer serverPlayer, DamageSource damageSource) {
        if (preparedDefense == null || preparedDefense.defender != serverPlayer || preparedDefense.source != damageSource) {
            return false;
        }
        if (preparedDefense.incomingNativeSnapshot.m_128456_()) {
            return FoodWeaponFinalDamage.incomingSourceAllowed(serverPlayer, damageSource) && damageSource.getClass() == DamageSource.class;
        }
        return preparedDefense.incomingAttacker != null && damageSource.m_7639_() == preparedDefense.incomingAttacker && FoodWeaponSourceLedger.resolveReadOnly(damageSource, preparedDefense.incomingAttacker).equals((Object)preparedDefense.incomingNativeSnapshot) && FoodWeaponSourceLedger.matchesNativeHurt(damageSource, preparedDefense.incomingAttacker, (Entity)serverPlayer, preparedDefense.incomingNativeSnapshot);
    }

    private static void commitDefense(PreparedDefense preparedDefense, float f) {
        DefenseReceipt defenseReceipt = preparedDefense.receipt;
        if (defenseReceipt == null || !defenseReceipt.matches(preparedDefense.plan, preparedDefense.defender, preparedDefense.source, preparedDefense.weapon, preparedDefense.hand, preparedDefense.at, f)) {
            return;
        }
        FoodSocketAttributePowers.commitDefense(preparedDefense.plan, f, defenseReceipt);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static float finish(LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (!FoodWeaponFinalDamage.originalForgeCaller("onLivingDamage", "ftboss$finalDamageGuard", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;FLorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")) {
            return FoodWeaponGuardKernel.finish(livingEntity, damageSource, f);
        }
        PreparedListedAction preparedListedAction = null;
        Throwable throwable = null;
        try {
            preparedListedAction = FoodWeaponFinalDamage.takeGuardWaiter(livingEntity, damageSource);
            float f2 = FoodWeaponGuardKernel.finish(livingEntity, damageSource, f);
            boolean bl = preparedListedAction != null && Float.isFinite(f2) && f2 > 0.0f && preparedListedAction.prepared.stable();
            boolean bl2 = false;
            if (bl) {
                Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                if (preparedListedAction.prepared.stableIdentity()) {
                    GuardResult guardResult;
                    preparedListedAction.guardResult = guardResult = new GuardResult(preparedListedAction, livingEntity, damageSource, f, f2);
                    bl2 = FoodWeaponFinalDamage.effectsReady();
                }
                // MONITOREXIT : clazz
            }
            if (bl2) {
                bl2 = FoodWeaponFinalDamage.reserveListedAction(preparedListedAction, preparedListedAction.guardResult);
            }
            if (bl2) {
                FoodWeaponFinalDamage.applyListedEffects(preparedListedAction, f2);
            }
            float f3 = f2;
            return f3;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (preparedListedAction != null) {
                FoodWeaponFinalDamage.closePublication(preparedListedAction.publication, throwable);
            }
        }
    }

    private static final class EventCapture {
        private final LivingHurtEvent event;
        private final ServerPlayer player;
        private final DamageSource source;
        private final LivingEntity target;
        private final CompoundTag snapshot;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final Thread thread;
        private final long at;
        private final ArrayList<CapturedProducer> producers = new ArrayList();
        private boolean failed;
        private boolean closed;

        private EventCapture(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, CompoundTag compoundTag, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
            this.event = livingHurtEvent;
            this.player = serverPlayer;
            this.source = livingHurtEvent.getSource();
            this.target = livingHurtEvent.getEntity();
            this.snapshot = compoundTag.m_6426_();
            this.originalBinding = originalInteractionBinding;
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(serverPlayer);
        }

        private boolean live() {
            if (this.closed || this.failed || this.thread != Thread.currentThread() || !this.player.m_284548_().m_7654_().m_18695_() || FoodWeaponFinalDamage.clock(this.player) != this.at || this.source != this.event.getSource() || this.target != this.event.getEntity() || this.source.m_7639_() != this.player || !this.player.m_6084_() || this.player.m_5833_() || this.event.isCanceled() || !Float.isFinite(this.event.getAmount()) || this.event.getAmount() <= 0.0f) {
                return false;
            }
            if (!FoodWeaponSourceLedger.matchesOriginalInteraction(this.originalBinding, this.snapshot, this.player)) {
                return false;
            }
            return FoodWeaponSourceLedger.resolveReadOnly(this.source, this.player).equals((Object)this.snapshot) && FoodWeaponFinalDamage.sourceMatchesTarget(this.snapshot, this.player, this.source, this.target);
        }

        private boolean append(ProducerFamily producerFamily, Object object, float f, float f2) {
            if (!this.live() || this.producers.stream().anyMatch(capturedProducer -> capturedProducer.family() == producerFamily)) {
                this.failed = true;
                return false;
            }
            this.producers.add(new CapturedProducer(producerFamily, object, this.producers.size(), Float.floatToRawIntBits(f), Float.floatToRawIntBits(f2)));
            if (!Float.isFinite(f2) || f2 <= 0.0f) {
                this.failed = true;
            }
            return true;
        }
    }

    private static final class PreparedListedAction {
        private final PreparedBatch prepared;
        private final List<ListedProducer> producers;
        private Publication publication;
        private EventCapture capture;
        private GuardResult guardResult;

        private PreparedListedAction(PreparedBatch preparedBatch, List<ListedProducer> list) {
            this.prepared = preparedBatch;
            this.producers = List.copyOf(list);
        }
    }

    private static enum ProducerFamily {
        MATERIAL,
        EVOLUTION,
        ATTRIBUTE,
        WOLF,
        PASSIVE;

    }

    private record CapturedProducer(ProducerFamily family, Object plan, int ordinal, int inputBits, int outputBits) {
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

        private PreparedBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, DamageSource damageSource, LivingEntity livingEntity, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
            this.snapshot = compoundTag.m_6426_();
            this.player = serverPlayer;
            this.source = damageSource;
            this.target = livingEntity;
            this.batch = attemptBatchQuote;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
            this.admission = attemptBatchAdmission;
            this.requests = List.copyOf(list);
            this.targets = List.copyOf(list2);
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(serverPlayer);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private boolean stable() {
            if (this.committed || this.originalBinding == null || Thread.currentThread() != this.thread || this.player.getClass() != ServerPlayer.class || !this.player.m_284548_().m_7654_().m_18695_() || FoodWeaponFinalDamage.clock(this.player) != this.at || !FoodWeaponFinalDamage.sourceMatchesTarget(this.snapshot, this.player, this.source, this.target) || !FoodWeaponSourceLedger.matchesAttemptRequests(this.batch, this.requests, this.targets)) {
                return false;
            }
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                if (!Thread.holdsLock(FoodWeaponSourceLedger.class) || this.committed || this.originalBinding == null || Thread.currentThread() != this.thread) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return false;
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                if (!FoodWeaponSourceLedger.matchesOriginalInteraction(this.originalBinding, this.snapshot, this.player)) {
                    return false;
                }
                if (!FoodWeaponSourceLedger.resolveReadOnly(this.source, this.player).equals((Object)this.snapshot)) {
                    return false;
                }
                return FoodWeaponSourceLedger.matchesAttemptAdmission(this.snapshot, this.player, this.batch, this.admission, this.at);
            }
        }

        private boolean stableIdentity() {
            return !this.committed && this.originalBinding != null && Thread.currentThread() == this.thread && this.player.getClass() == ServerPlayer.class && this.player.m_284548_().m_7654_().m_18695_() && FoodWeaponFinalDamage.clock(this.player) == this.at && FoodWeaponSourceLedger.matchesAttemptRequests(this.batch, this.requests, this.targets) && this.admission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY;
        }

        private FoodWeaponSourceLedger.AttemptBatchCommit commit(float f) {
            if (!this.stable() || !Float.isFinite(f) || f <= 0.0f) {
                throw new IllegalStateException("Stable genuine Owner admission changed before positive publication");
            }
            this.committed = true;
            FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit = FoodWeaponSourceLedger.commitAdmittedAttemptBatch(this.snapshot, this.player, this.batch, this.admission, f, false, false, this.at);
            if (attemptBatchCommit.status() != FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED || !FoodWeaponSourceLedger.matchesAttemptBatch(this.snapshot, this.player, this.batch, attemptBatchCommit, this.at)) {
                throw new IllegalStateException("Owner result does not belong to the original complete action batch");
            }
            return attemptBatchCommit;
        }
    }

    private static final class Publication {
        private OriginalContinuation originalContinuation;
        private boolean extraAuthorityRevoked;
        private FoodWeaponOriginalSequence.Session originalSequence;
        private final PreparedBatch prepared;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final float amount;
        private final List<ListedProducer> producers;
        private final MinecraftServer server;
        private final OffenseReceipt receipt;
        private ListedProducer currentStateProducer;
        private ListedProducerInvocation currentInvocation;
        private PublishingCleanup publishingCleanup;
        private PublishingCleanup incompletePublishingCleanup;
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
        private final ReservationReadiness readiness;
        private final GuardResult guardResult;
        private boolean closed;

        private Publication(PreparedListedAction preparedListedAction, FoodWeaponSourceLedger.AttemptBatchCommit attemptBatchCommit, float f) {
            this.prepared = preparedListedAction.prepared;
            this.result = attemptBatchCommit;
            this.amount = f;
            this.producers = preparedListedAction.producers;
            this.server = this.prepared.player.m_284548_().m_7654_();
            this.receipt = new OffenseReceipt(this);
            this.guardResult = preparedListedAction.guardResult;
            this.readiness = new ReservationReadiness(this);
        }

        private static final class OriginalContinuation {
            private final Publication publication;
            private final PreparedBatch prepared;
            private final OffenseReceipt receipt;
            private final FoodWeaponSourceLedger.AttemptBatchCommit result;
            private final List<ListedProducer> producers;
            private final FoodWeaponOriginalSequence.Session sequence;
            private final Thread thread;
            private final int acceptedBits;
            private boolean applying;
            private Object admittingProducer;
            private int admittingOrdinal = -1;
            private boolean terminal;

            private OriginalContinuation(Publication publication, FoodWeaponOriginalSequence.Session session) {
                this.publication = publication;
                this.prepared = publication.prepared;
                this.receipt = publication.receipt;
                this.result = publication.result;
                this.producers = publication.producers;
                this.sequence = session;
                this.thread = this.prepared.thread;
                this.acceptedBits = Float.floatToRawIntBits(publication.amount);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            private boolean live() {
                Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
                synchronized (FoodWeaponSourceLedger.class) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return !this.terminal && this.publication.originalContinuation == this && this.publication.originalSequence == this.sequence && !this.publication.closed && Thread.currentThread() == this.thread && Thread.holdsLock(FoodWeaponSourceLedger.class) && this.publication.prepared == this.prepared && this.publication.receipt == this.receipt && this.publication.result == this.result && this.publication.producers == this.producers && this.prepared.committed && this.prepared.originalBinding != null && this.publication.readiness.publication == this.publication && this.publication.readiness.prepared == this.prepared && this.publication.readiness.result == this.result && this.publication.readiness.producers == this.producers && this.publication.guardResult != null && this.publication.guardResult.action.publication == this.publication && this.publication.guardResult.action.prepared == this.prepared && this.publication.guardResult.action.producers == this.producers && this.publication.guardResult.action.guardResult == this.publication.guardResult && this.publication.guardResult.originalBinding == this.prepared.originalBinding && this.publication.guardResult.acceptedBits == this.acceptedBits && Float.floatToRawIntBits(this.publication.amount) == this.acceptedBits && this.sequence.lineage.publication == this.publication && this.sequence.lineage.acceptedCallback == this.receipt && this.sequence.original.continuing(this.publication);
                }
            }
        }
    }

    static final class PublishingCleanup {
        private final Publication publication;
        private final PreparedBatch prepared;
        private final ListedProducer producer;
        private final Object consumerPrepared;
        private final OffenseReceipt receipt;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final GuardResult guard;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding capturedBinding;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final Thread thread;
        private final MinecraftServer server;
        private final long at;
        private final int ordinal;
        private PublishingPhase phase = PublishingPhase.PUBLISHING;
        private Throwable cleanupFailure;

        private PublishingCleanup(Publication publication, ListedProducer listedProducer, int n) {
            this.publication = publication;
            this.prepared = publication.prepared;
            this.producer = listedProducer;
            this.consumerPrepared = listedProducer.preparedConsumer();
            this.receipt = publication.receipt;
            this.result = publication.result;
            this.guard = publication.guardResult;
            this.capturedBinding = this.guard.action.capture.originalBinding;
            this.originalBinding = this.prepared.originalBinding;
            this.thread = this.prepared.thread;
            this.server = publication.server;
            this.at = this.prepared.at;
            this.ordinal = n;
        }
    }

    private static enum PublishingPhase {
        PUBLISHING,
        FINALIZING,
        CLOSED;

    }

    private static final class ReservationReadiness {
        private final Publication publication;
        private final PreparedBatch prepared;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final List<ListedProducer> producers;

        private ReservationReadiness(Publication publication) {
            this.publication = publication;
            this.prepared = publication.prepared;
            this.result = publication.result;
            this.producers = publication.producers;
        }

        private boolean matches(Publication publication) {
            return publication == this.publication && this.prepared == publication.prepared && this.result == publication.result && this.producers == publication.producers && publication.guardResult != null && publication.guardResult.matches(publication.guardResult.action) && publication.guardResult.action.publication == publication && publication.guardResult.acceptedBits == Float.floatToRawIntBits(publication.amount) && this.result.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED;
        }
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    private static abstract class ListedProducer {
        private final PreparedBatch prepared;
        private boolean publishAttempted;
        private OriginalConsumerBinding originalConsumer;

        private ListedProducer(PreparedBatch preparedBatch) {
            this.prepared = preparedBatch;
        }

        final PreparedBatch prepared() {
            return this.prepared;
        }

        abstract Object plan();

        abstract Object preparedConsumer();

        abstract boolean publish(float var1, OffenseReceipt var2, PublishingCleanup var3);

        abstract void apply(ListedProducerInvocation var1);
    }

    private static final class ListedProducerInvocation {
        private final Publication publication;
        private final ListedProducer producer;
        private final int ordinal;
        private ChildHurtCall childCall;
        private boolean closed;

        private ListedProducerInvocation(Publication publication, ListedProducer listedProducer, int n) {
            this.publication = publication;
            this.producer = listedProducer;
            this.ordinal = n;
        }
    }

    static final class ChildHurtCall {
        private final ChildHurtBridge bridge;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final LivingEntity target;
        private final DamageSource source;
        private final float amount;
        private final Thread thread;
        private boolean hurtEntered;
        private boolean closed;
        private FoodWeaponConsumerSequence.OriginalChild originalConsumerChild;
        private Throwable originalConsumerChildFailure;

        private ChildHurtCall(ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
            this.bridge = childHurtBridge;
            this.originalBinding = childHurtBridge.invocation.publication.prepared.originalBinding;
            this.target = livingEntity;
            this.source = damageSource;
            this.amount = f;
            this.thread = Thread.currentThread();
        }
    }

    private static final class AttributeProducer
    extends ListedProducer {
        private final FoodSocketAttributePowers.OffensePlan plan;
        private final FoodSocketAttributePowers.PreparedOffense consumerPrepared;
        private FoodSocketAttributePowers.OffenseCommit consumerCommit;

        private AttributeProducer(PreparedBatch preparedBatch, FoodSocketAttributePowers.OffensePlan offensePlan, FoodSocketAttributePowers.PreparedOffense preparedOffense) {
            super(preparedBatch);
            this.plan = offensePlan;
            this.consumerPrepared = preparedOffense;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean publish(float f, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
            PreparedBatch preparedBatch = this.prepared();
            if (!FoodSocketAttributePowers.matchesPreparedOffense(this.consumerPrepared, this.plan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets)) {
                return false;
            }
            OriginalConsumerBinding originalConsumerBinding = null;
            try {
                originalConsumerBinding = FoodWeaponFinalDamage.beginOriginalConsumerBinding(publishingCleanup);
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.bound = FoodSocketAttributePowers.bindOriginalSequence(this.consumerPrepared, originalConsumerBinding.sequence, this, publishingCleanup.ordinal, offenseReceipt, f, publishingCleanup);
                    if (!originalConsumerBinding.bound) {
                        FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, null);
                    }
                }
            }
            catch (Throwable throwable) {
                FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, throwable);
            }
            finally {
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.binding = false;
                }
            }
            Throwable throwable = null;
            boolean bl = false;
            try {
                this.consumerCommit = FoodSocketAttributePowers.commitPreparedOffense(this.consumerPrepared, f, offenseReceipt, publishingCleanup);
                boolean bl2 = bl = FoodSocketAttributePowers.matchesOffenseCommit(this.consumerCommit, this.consumerPrepared, this.plan, offenseReceipt, f, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                return bl2;
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerPublication(originalConsumerBinding, this.consumerCommit, bl, throwable);
            }
        }

        @Override
        void apply(ListedProducerInvocation listedProducerInvocation) {
            ChildHurtBridge childHurtBridge = new ChildHurtBridge(listedProducerInvocation);
            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(listedProducerInvocation, childHurtBridge, this.consumerCommit);
                FoodSocketAttributePowers.applyOffense(this.consumerCommit, childHurtBridge);
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(listedProducerInvocation, childHurtBridge);
                childHurtBridge.closed = true;
            }
        }
    }

    public static final class OffenseReceipt {
        private final Publication publication;

        private OffenseReceipt(Publication publication) {
            this.publication = publication;
        }

        public boolean matches(Object object, ServerPlayer serverPlayer, DamageSource damageSource, long l, float f) {
            PreparedBatch preparedBatch = this.publication.prepared;
            ListedProducer listedProducer = this.publication.currentStateProducer != null ? this.publication.currentStateProducer : (this.publication.currentInvocation == null ? null : this.publication.currentInvocation.producer);
            return FoodWeaponFinalDamage.receiptLive(this.publication) && listedProducer != null && listedProducer.plan() == object && serverPlayer == preparedBatch.player && damageSource == preparedBatch.source && l == preparedBatch.at && Float.compare(f, this.publication.amount) == 0;
        }

        public boolean matchesBatch(FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
            PreparedBatch preparedBatch = this.publication.prepared;
            return FoodWeaponFinalDamage.receiptLive(this.publication) && attemptBatchQuote == preparedBatch.batch && attemptBatchAdmission == preparedBatch.admission && list != null && list2 != null && preparedBatch.requests.equals(list) && preparedBatch.targets.equals(list2);
        }

        public Optional<FoodWeaponSourceLedger.AttemptResult> attempt(FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
            return FoodWeaponFinalDamage.receiptLive(this.publication) ? this.publication.result.attempt(attemptDomain) : Optional.empty();
        }

        public Optional<FoodWeaponSourceLedger.TargetResult> target(UUID uUID, FoodWeaponSourceLedger.TargetEffectDomain targetEffectDomain) {
            return FoodWeaponFinalDamage.receiptLive(this.publication) ? this.publication.result.target(uUID, targetEffectDomain) : Optional.empty();
        }
    }

    private static final class MaterialProducer
    extends ListedProducer {
        private final FoodWeaponHitPlan plan;
        private final FoodWeaponHitPlan.PreparedFinalEffects consumerPrepared;
        private FoodWeaponHitPlan.FinalEffectsCommit consumerCommit;

        private MaterialProducer(PreparedBatch preparedBatch, FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponHitPlan.PreparedFinalEffects preparedFinalEffects) {
            super(preparedBatch);
            this.plan = foodWeaponHitPlan;
            this.consumerPrepared = preparedFinalEffects;
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
        boolean publish(float f, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
            this.consumerCommit = FoodWeaponHitPlan.commitFinalState(this.consumerPrepared, f, offenseReceipt);
            return this.consumerCommit != null;
        }

        @Override
        void apply(ListedProducerInvocation listedProducerInvocation) {
            ChildHurtBridge childHurtBridge = new ChildHurtBridge(listedProducerInvocation);
            try {
                FoodWeaponHitPlan.applyFinalEffects(this.consumerCommit, childHurtBridge);
            }
            finally {
                childHurtBridge.closed = true;
            }
        }
    }

    private static final class EvolutionProducer
    extends ListedProducer {
        private final FoodWeaponHitPlan plan;
        private final FoodWeaponHitPlan.PreparedFinalEffects consumerPrepared;
        private FoodWeaponHitPlan.FinalEffectsCommit consumerCommit;

        private EvolutionProducer(PreparedBatch preparedBatch, FoodWeaponHitPlan foodWeaponHitPlan, FoodWeaponHitPlan.PreparedFinalEffects preparedFinalEffects) {
            super(preparedBatch);
            this.plan = foodWeaponHitPlan;
            this.consumerPrepared = preparedFinalEffects;
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
        boolean publish(float f, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
            this.consumerCommit = FoodWeaponHitPlan.commitFinalState(this.consumerPrepared, f, offenseReceipt);
            return this.consumerCommit != null;
        }

        @Override
        void apply(ListedProducerInvocation listedProducerInvocation) {
            ChildHurtBridge childHurtBridge = new ChildHurtBridge(listedProducerInvocation);
            try {
                FoodWeaponHitPlan.applyFinalEffects(this.consumerCommit, childHurtBridge);
            }
            finally {
                childHurtBridge.closed = true;
            }
        }
    }

    private static final class WolfProducer
    extends ListedProducer {
        private final FoodSocketPowers.PackPlan plan;
        private final FoodSocketPowers.PreparedPack consumerPrepared;
        private FoodSocketPowers.PackCommit consumerCommit;

        private WolfProducer(PreparedBatch preparedBatch, FoodSocketPowers.PackPlan packPlan, FoodSocketPowers.PreparedPack preparedPack) {
            super(preparedBatch);
            this.plan = packPlan;
            this.consumerPrepared = preparedPack;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean publish(float f, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
            PreparedBatch preparedBatch = this.prepared();
            if (!FoodSocketPowers.matchesPreparedPack(this.consumerPrepared, this.plan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets)) {
                return false;
            }
            OriginalConsumerBinding originalConsumerBinding = null;
            try {
                originalConsumerBinding = FoodWeaponFinalDamage.beginOriginalConsumerBinding(publishingCleanup);
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.bound = FoodSocketPowers.bindOriginalSequence(this.consumerPrepared, originalConsumerBinding.sequence, this, publishingCleanup.ordinal, offenseReceipt, f, publishingCleanup);
                    if (!originalConsumerBinding.bound) {
                        FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, null);
                    }
                }
            }
            catch (Throwable throwable) {
                FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, throwable);
            }
            finally {
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.binding = false;
                }
            }
            Throwable throwable = null;
            boolean bl = false;
            try {
                this.consumerCommit = FoodSocketPowers.commitPack(this.consumerPrepared, f, offenseReceipt, publishingCleanup);
                boolean bl2 = bl = FoodSocketPowers.matchesPackCommit(this.consumerCommit, this.consumerPrepared, this.plan, offenseReceipt, f, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                return bl2;
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerPublication(originalConsumerBinding, this.consumerCommit, bl, throwable);
            }
        }

        @Override
        void apply(ListedProducerInvocation listedProducerInvocation) {
            ChildHurtBridge childHurtBridge = new ChildHurtBridge(listedProducerInvocation);
            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(listedProducerInvocation, childHurtBridge, this.consumerCommit);
                FoodSocketPowers.applyPack(this.consumerCommit, childHurtBridge);
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(listedProducerInvocation, childHurtBridge);
                childHurtBridge.closed = true;
            }
        }
    }

    private static final class PassiveProducer
    extends ListedProducer {
        private final FoodWeaponBossPassive.HitPlan plan;
        private final FoodWeaponBossPassive.PreparedHit consumerPrepared;
        private FoodWeaponBossPassive.HitCommit consumerCommit;

        private PassiveProducer(PreparedBatch preparedBatch, FoodWeaponBossPassive.HitPlan hitPlan, FoodWeaponBossPassive.PreparedHit preparedHit) {
            super(preparedBatch);
            this.plan = hitPlan;
            this.consumerPrepared = preparedHit;
        }

        @Override
        Object plan() {
            return this.plan;
        }

        @Override
        Object preparedConsumer() {
            return this.consumerPrepared;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean publish(float f, OffenseReceipt offenseReceipt, PublishingCleanup publishingCleanup) {
            PreparedBatch preparedBatch = this.prepared();
            if (!FoodWeaponBossPassive.matchesPreparedPassiveHit(this.consumerPrepared, this.plan, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets)) {
                return false;
            }
            OriginalConsumerBinding originalConsumerBinding = null;
            try {
                originalConsumerBinding = FoodWeaponFinalDamage.beginOriginalConsumerBinding(publishingCleanup);
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.bound = FoodWeaponBossPassive.bindOriginalSequence(this.consumerPrepared, originalConsumerBinding.sequence, this, publishingCleanup.ordinal, offenseReceipt, f, publishingCleanup);
                    if (!originalConsumerBinding.bound) {
                        FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, null);
                    }
                }
            }
            catch (Throwable throwable) {
                FoodWeaponFinalDamage.originalConsumerBindingFailed(publishingCleanup, throwable);
            }
            finally {
                if (originalConsumerBinding != null) {
                    originalConsumerBinding.binding = false;
                }
            }
            Throwable throwable = null;
            boolean bl = false;
            try {
                this.consumerCommit = FoodWeaponBossPassive.commitPassiveHit(this.consumerPrepared, f, offenseReceipt, publishingCleanup);
                boolean bl2 = bl = FoodWeaponBossPassive.matchesPassiveHitCommit(this.consumerCommit, this.consumerPrepared, this.plan, offenseReceipt, f, preparedBatch.batch, preparedBatch.admission, preparedBatch.requests, preparedBatch.targets);
                return bl2;
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerPublication(originalConsumerBinding, this.consumerCommit, bl, throwable);
            }
        }

        @Override
        void apply(ListedProducerInvocation listedProducerInvocation) {
            ChildHurtBridge childHurtBridge = new ChildHurtBridge(listedProducerInvocation);
            try {
                FoodWeaponFinalDamage.beginOriginalConsumerApply(listedProducerInvocation, childHurtBridge, this.consumerCommit);
                FoodWeaponBossPassive.applyPassiveHit(this.consumerCommit, childHurtBridge);
            }
            finally {
                FoodWeaponFinalDamage.finishOriginalConsumerApply(listedProducerInvocation, childHurtBridge);
                childHurtBridge.closed = true;
            }
        }
    }

    private static final class GuardResult {
        private final PreparedListedAction action;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final ServerPlayer player;
        private final DamageSource source;
        private final LivingEntity target;
        private final int inputBits;
        private final int acceptedBits;
        private final Thread thread;
        private final long at;

        private GuardResult(PreparedListedAction preparedListedAction, LivingEntity livingEntity, DamageSource damageSource, float f, float f2) {
            this.action = preparedListedAction;
            this.originalBinding = preparedListedAction.prepared.originalBinding;
            this.player = preparedListedAction.prepared.player;
            this.source = damageSource;
            this.target = livingEntity;
            this.inputBits = Float.floatToRawIntBits(f);
            this.acceptedBits = Float.floatToRawIntBits(f2);
            this.thread = Thread.currentThread();
            this.at = FoodWeaponFinalDamage.clock(this.player);
        }

        private boolean matches(PreparedListedAction preparedListedAction) {
            return this.action == preparedListedAction && preparedListedAction.guardResult == this && this.originalBinding == preparedListedAction.prepared.originalBinding && this.player == preparedListedAction.prepared.player && this.source == preparedListedAction.prepared.source && this.target == preparedListedAction.prepared.target && this.thread == Thread.currentThread() && this.at == preparedListedAction.prepared.at && FoodWeaponFinalDamage.clock(this.player) == this.at && Float.isFinite(Float.intBitsToFloat(this.acceptedBits)) && Float.intBitsToFloat(this.acceptedBits) > 0.0f;
        }
    }

    static final class ChildHurtBridge {
        private final ListedProducerInvocation invocation;
        private boolean closed;

        private ChildHurtBridge(ListedProducerInvocation listedProducerInvocation) {
            this.invocation = listedProducerInvocation;
        }
    }

    private static final class OriginalConsumerBinding {
        private final PublishingCleanup cleanup;
        private final FoodWeaponOriginalSequence.Session sequence;
        private Object leaf;
        private Object commit;
        private ChildHurtBridge bridge;
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

        private OriginalConsumerBinding(PublishingCleanup publishingCleanup) {
            this.cleanup = publishingCleanup;
            this.sequence = publishingCleanup.publication.originalSequence;
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
        private final FoodSocketAttributeRules.Resolution recipe;
        private DefenseReceipt receipt;

        private PreparedDefense(FoodSocketAttributePowers.DefensePlan defensePlan, ServerPlayer serverPlayer, DamageSource damageSource, ItemStack itemStack, InteractionHand interactionHand, String string) {
            ServerPlayer serverPlayer2;
            Entity entity = damageSource.m_7639_();
            this.incomingAttacker = entity instanceof ServerPlayer ? (serverPlayer2 = (ServerPlayer)entity) : null;
            serverPlayer2 = this.incomingAttacker == null ? new CompoundTag() : FoodWeaponSourceLedger.resolveReadOnly(damageSource, this.incomingAttacker);
            this.incomingNativeSnapshot = serverPlayer2.m_128403_("NativeHurtId") ? serverPlayer2.m_6426_() : new CompoundTag();
            this.plan = defensePlan;
            this.defender = serverPlayer;
            this.source = damageSource;
            this.weapon = itemStack;
            this.hand = interactionHand;
            this.at = defensePlan.quotedAt();
            this.dimension = serverPlayer.m_9236_().m_46472_();
            this.thread = Thread.currentThread();
            this.weaponState = itemStack.m_41739_(new CompoundTag());
            this.materials = FoodWeaponSourceLedger.materialFingerprint(itemStack);
            this.foundation = string;
            this.recipe = FoodSocketAttributePowers.inspect(itemStack);
        }
    }

    public static final class DefenseReceipt {
        private final PreparedDefense prepared;
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
        private final FoodSocketAttributeRules.Resolution recipe;
        private final float amount;
        private final DefenseReason reason;

        private DefenseReceipt(PreparedDefense preparedDefense, float f, DefenseReason defenseReason) {
            this.prepared = preparedDefense;
            this.plan = preparedDefense.plan;
            this.defender = preparedDefense.defender;
            this.source = preparedDefense.source;
            this.weapon = preparedDefense.weapon;
            this.hand = preparedDefense.hand;
            this.at = preparedDefense.at;
            this.dimension = preparedDefense.dimension;
            this.thread = preparedDefense.thread;
            this.weaponState = preparedDefense.weaponState.m_6426_();
            this.materials = preparedDefense.materials;
            this.foundation = preparedDefense.foundation;
            this.recipe = preparedDefense.recipe;
            this.amount = f;
            this.reason = defenseReason;
        }

        public DefenseReason reason() {
            return this.reason;
        }

        public float amount() {
            return this.amount;
        }

        public boolean matches(FoodSocketAttributePowers.DefensePlan defensePlan, ServerPlayer serverPlayer, DamageSource damageSource, ItemStack itemStack, InteractionHand interactionHand, long l, float f) {
            if (defensePlan != this.plan || serverPlayer != this.defender || damageSource != this.source || itemStack != this.weapon || interactionHand != this.hand || l != this.at || Thread.currentThread() != this.thread || !Float.isFinite(f) || Float.compare(f, this.amount) != 0 || !this.defender.m_284548_().m_7654_().m_18695_() || FoodWeaponFinalDamage.clock(this.defender) != this.at || !this.defender.m_9236_().m_46472_().equals(this.dimension) || !this.defender.m_6084_() || this.defender.m_5833_() || this.defender.m_21120_(this.hand) != this.weapon || this.weapon.m_41619_() || this.weapon.m_41613_() != 1 || FoodWeaponLifecycle.isBroken(this.weapon) || this.source.m_269533_(DamageTypeTags.f_268738_) || FoodWeaponBondHits.secondary(this.source) || !FoodWeaponFinalDamage.incomingSourceMatches(this.prepared, this.defender, this.source) || !this.plan.matchesContext(serverPlayer, damageSource, itemStack, interactionHand, l)) {
                return false;
            }
            if (!(this.weapon.m_41739_(new CompoundTag()).equals((Object)this.weaponState) && FoodWeaponSourceLedger.materialFingerprint(this.weapon).equals(this.materials) && Objects.equals(FoodWeaponFinalDamage.foundationFingerprint(this.defender, this.weapon), this.foundation) && FoodSocketAttributePowers.inspect(this.weapon).equals(this.recipe))) {
                return false;
            }
            return this.reason == DefenseReason.SHIELD_ONLY_ZERO ? this.amount == 0.0f && this.plan.amount() == 0.0f && this.plan.blocked() > 0.0f && this.plan.blocked() == this.plan.input() : this.amount > 0.0f && this.plan.amount() > 0.0f && this.amount <= this.plan.amount() && this.plan.blocked() > 0.0f;
        }
    }

    public static enum DefenseReason {
        POSITIVE_DEFENSE,
        SHIELD_ONLY_ZERO;

    }
}

