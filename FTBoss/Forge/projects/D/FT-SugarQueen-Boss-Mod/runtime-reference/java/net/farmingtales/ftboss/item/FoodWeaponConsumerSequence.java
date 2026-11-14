/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.item.FoodSocketAttributePowers;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponBossPassive;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

final class FoodWeaponConsumerSequence {
    private FoodWeaponConsumerSequence() {
    }

    static boolean finalCaller(String string, String string2, String string3) {
        return Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponConsumerSequence.class && stackFrame.getDeclaringClass() != Accepted.class && stackFrame.getDeclaringClass() != FoodSocketPowers.class && stackFrame.getDeclaringClass() != FoodSocketAttributePowers.class && stackFrame.getDeclaringClass() != FoodWeaponBossPassive.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == FoodWeaponFinalDamage.class && stackFrame.getDeclaringClass().getName().equals(FoodWeaponFinalDamage.class.getName() + "$" + string) && stackFrame.getMethodName().equals(string2) && stackFrame.getDescriptor().equals(string3)).orElse(false)) != false;
    }

    static Accepted bind(FoodWeaponOriginalSequence.Session session, Object object, int n, Object object2, Object object3, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, ItemStack itemStack, InteractionHand interactionHand, long l, float f, String string) {
        if (!(FoodWeaponConsumerSequence.finalCaller(string, "publish", "(FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)Z") && session != null && object != null && n >= 0 && object2 != null && object3 != null && offenseReceipt != null && publishingCleanup != null && attemptBatchQuote != null && attemptBatchAdmission != null && list != null && list2 != null && serverPlayer != null && livingEntity != null && damageSource != null && itemStack != null && interactionHand != null && Float.isFinite(f) && !(f <= 0.0f) && session.lineage.acceptedCallback == offenseReceipt && session.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication && session.original.continuing(session.lineage.publication) && session.original.atProducer(object) && FoodWeaponFinalDamage.matchesOwnerWriterSelection(offenseReceipt, object, n, object2, object2, 0) && offenseReceipt.matchesBatch(attemptBatchQuote, attemptBatchAdmission, list, list2) && offenseReceipt.matches(object3, serverPlayer, damageSource, l, f) && serverPlayer.m_21120_(interactionHand) == itemStack && itemStack.m_41613_() == 1 && attemptBatchAdmission.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY)) {
            return null;
        }
        FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
        if (originalInteractionBinding == null || !FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(originalInteractionBinding, originalInteractionBinding, serverPlayer)) {
            return null;
        }
        EnumMap<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponSourceLedger.AttemptResult> enumMap = new EnumMap<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponSourceLedger.AttemptResult>(FoodWeaponSourceLedger.AttemptDomain.class);
        for (FoodWeaponSourceLedger.AttemptRequest attemptRequest : list) {
            FoodWeaponSourceLedger.AttemptResult attemptResult = offenseReceipt.attempt(attemptRequest.domain()).orElse(null);
            if (attemptResult != null && attemptResult.domain() == attemptRequest.domain() && enumMap.put(attemptRequest.domain(), attemptResult) == null) continue;
            return null;
        }
        Accepted accepted = new Accepted(session, object, object2, offenseReceipt, publishingCleanup, originalInteractionBinding, serverPlayer, livingEntity, damageSource, itemStack, interactionHand, l, f, string, enumMap);
        return session.original.enterLeaf(object, accepted, 0) ? accepted : null;
    }

    static final class Accepted {
        private final FoodWeaponOriginalSequence.Session session;
        private final Object producer;
        private final Object prepared;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final ItemStack weapon;
        private final InteractionHand hand;
        private final long tick;
        private final int amountBits;
        private final String producerName;
        private final Thread thread;
        private final MinecraftServer server;
        private final EnumMap<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponSourceLedger.AttemptResult> results;
        private Phase phase = Phase.PUBLISHING;
        private Object commit;
        private FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private int observationPhase = -1;
        private int leafOrdinal;
        private boolean leafOpen = true;
        private int publicationCalls;
        private OriginalChild child;

        private Accepted(FoodWeaponOriginalSequence.Session session, Object object, Object object2, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, ItemStack itemStack, InteractionHand interactionHand, long l, float f, String string, EnumMap<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponSourceLedger.AttemptResult> enumMap) {
            this.session = session;
            this.producer = object;
            this.prepared = object2;
            this.receipt = offenseReceipt;
            this.publishingCleanup = publishingCleanup;
            this.binding = originalInteractionBinding;
            this.player = serverPlayer;
            this.target = livingEntity;
            this.source = damageSource;
            this.weapon = itemStack;
            this.hand = interactionHand;
            this.tick = l;
            this.amountBits = Float.floatToRawIntBits(f);
            this.producerName = string;
            this.results = enumMap;
            this.thread = Thread.currentThread();
            this.server = serverPlayer.m_284548_().m_7654_();
        }

        private boolean cleanupLifetime() {
            return this.phase != Phase.RETIRED && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == this.thread && this.server == this.player.m_284548_().m_7654_() && this.server.m_18695_() && FoodWeaponSourceLedger.clock(this.player) == this.tick && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(this.binding, this.binding, this.player);
        }

        private boolean lifetime() {
            return this.originalFailure == null && this.phase != Phase.CANCELLED && this.phase != Phase.FAILED && this.phase != Phase.RETIRED && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == this.thread && this.server == this.player.m_284548_().m_7654_() && this.server.m_18695_() && FoodWeaponSourceLedger.clock(this.player) == this.tick && this.player.m_9236_() == this.target.m_9236_() && this.session.original.continuing(this.session.lineage.publication) && this.session.original.atProducer(this.producer) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(this.binding, this.binding, this.player);
        }

        boolean publishing(Object object, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
            return this.phase == Phase.PUBLISHING && this.prepared == object && this.receipt == offenseReceipt && this.publishingCleanup == publishingCleanup && this.lifetime() && this.session.original.atLeaf(this);
        }

        boolean matchesReceipt(Object object, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f) {
            return this.prepared == object && this.receipt == offenseReceipt && Float.floatToRawIntBits(f) == this.amountBits && (this.phase == Phase.PUBLISHING || this.phase == Phase.APPLYING) && this.lifetime() && this.session.original.atLeaf(this);
        }

        Optional<FoodWeaponSourceLedger.AttemptResult> attempt(FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
            return this.lifetime() && (this.phase == Phase.PUBLISHING || this.phase == Phase.APPLYING) ? Optional.ofNullable(this.results.get((Object)attemptDomain)) : Optional.empty();
        }

        private void leaveLeaf() {
            if (!this.leafOpen) {
                return;
            }
            this.leafOpen = false;
            this.session.original.leaveLeaf(this);
        }

        int publicationCallEntered(Object object) {
            if (this.prepared != object || this.phase != Phase.PUBLISHING || Thread.currentThread() != this.thread) {
                return 0;
            }
            ++this.publicationCalls;
            return this.publicationCalls;
        }

        void publicationExited(Object object, int n, Throwable throwable) {
            if (this.prepared != object || n <= 0 || this.publicationCalls != n || Thread.currentThread() != this.thread) {
                return;
            }
            --this.publicationCalls;
            if (this.publicationCalls != 0 || this.phase != Phase.PUBLISHING) {
                return;
            }
            if (throwable != null) {
                this.originalThrown(throwable);
            }
            this.published(object, null);
        }

        void publicationCleanupExited(Object object, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, Throwable throwable) {
            if (this.prepared != object || this.publishingCleanup != publishingCleanup || this.phase == Phase.RETIRED || this.publicationCalls != 0 || Thread.currentThread() != this.thread) {
                return;
            }
            if (throwable != null && this.originalFailure == null) {
                this.originalFailure = throwable;
            }
            this.retired(object);
        }

        void publicationRetirementExited(Object object, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding, float f, Throwable throwable) {
            if (this.prepared != object || this.receipt != offenseReceipt || this.binding != originalInteractionBinding || Float.floatToRawIntBits(f) != this.amountBits || this.phase == Phase.RETIRED || this.publicationCalls != 0 || Thread.currentThread() != this.thread) {
                return;
            }
            if (throwable != null && this.originalFailure == null) {
                this.originalFailure = throwable;
            }
            this.retired(object);
        }

        void published(Object object, Object object2) {
            if (this.prepared != object || this.phase != Phase.PUBLISHING) {
                return;
            }
            this.leaveLeaf();
            this.commit = object2;
            this.phase = object2 != null ? Phase.PUBLISHED : (this.originalFailure == null ? Phase.CANCELLED : Phase.FAILED);
        }

        boolean enter(Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            if (!(this.phase == Phase.PUBLISHED && this.commit == object && childHurtBridge != null && this.bridge == null && this.lifetime() && FoodWeaponConsumerSequence.finalCaller(this.producerName, "apply", "(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ListedProducerInvocation;)V") && this.session.original.enterLeaf(this.producer, this, this.leafOrdinal + 1))) {
                return false;
            }
            this.leafOpen = true;
            ++this.leafOrdinal;
            this.bridge = childHurtBridge;
            this.phase = Phase.APPLYING;
            return true;
        }

        boolean continuing(Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
            return this.phase == Phase.APPLYING && this.commit == object && this.bridge == childHurtBridge && this.lifetime() && this.session.original.atLeaf(this);
        }

        boolean ordinaryContext() {
            return this.lifetime() && this.player.m_6084_() && !this.player.m_5833_() && !this.player.m_213877_() && this.player.m_21120_(this.hand) == this.weapon && this.weapon.m_41613_() == 1 && !FoodWeaponLifecycle.isBroken(this.weapon) && !FoodWeaponBondHits.secondary(this.source) && FoodWeaponBondHits.hostile(this.player, this.target) && this.target.m_6084_() && !this.target.m_213877_();
        }

        boolean cleanup(Object object, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
            if (this.prepared != object || this.publishingCleanup != publishingCleanup || !this.cleanupLifetime()) {
                return false;
            }
            return (this.phase == Phase.APPLYING || this.phase == Phase.PUBLISHING) && this.session.original.atLeaf(this) || (this.phase == Phase.PUBLISHED || this.phase == Phase.FINISHED || this.phase == Phase.CANCELLED || this.phase == Phase.FAILED) && Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponConsumerSequence.class && stackFrame.getDeclaringClass() != Accepted.class && stackFrame.getDeclaringClass() != FoodSocketPowers.class && stackFrame.getDeclaringClass() != FoodSocketAttributePowers.class && stackFrame.getDeclaringClass() != FoodWeaponBossPassive.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("finalizeFailedPublication") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")).orElse(false)) != false;
        }

        OriginalChild beginChild(Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
            if (!this.continuing(object, childHurtBridge) || this.child != null || livingEntity == null || damageSource == null || !Float.isFinite(f) || f <= 0.0f || livingEntity.m_9236_() != this.player.m_9236_() || !FoodWeaponBondHits.hostile(this.player, livingEntity) || !livingEntity.m_6084_() || livingEntity.m_213877_()) {
                return null;
            }
            this.child = new OriginalChild(this, object, childHurtBridge, livingEntity, damageSource, f);
            return this.child;
        }

        boolean matchesChild(OriginalChild originalChild, Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
            return this.child != null && this.child == originalChild && !this.child.closed && this.child.owner == this && this.child.commit == object && this.child.bridge == childHurtBridge && this.child.target == livingEntity && this.child.source == damageSource && this.child.amountBits == Float.floatToRawIntBits(f) && this.continuing(object, childHurtBridge);
        }

        void closeChild(OriginalChild originalChild) {
            if (this.child != originalChild || originalChild == null) {
                return;
            }
            originalChild.closed = true;
            this.child = null;
        }

        void finish(Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, Throwable throwable) {
            if (this.commit != object || this.bridge != childHurtBridge || this.phase != Phase.APPLYING) {
                return;
            }
            if (this.child != null) {
                this.closeChild(this.child);
            }
            if (throwable != null) {
                this.originalThrown(throwable);
            }
            this.leaveLeaf();
            this.bridge = null;
            this.phase = throwable == null ? Phase.FINISHED : Phase.FAILED;
        }

        void originalThrown(Throwable throwable) {
            if (throwable == null) {
                return;
            }
            if (this.originalFailure == null) {
                this.originalFailure = throwable;
            }
            this.session.original.originalThrown(throwable);
        }

        void observationFailed(int n, Throwable throwable) {
            if (this.observationPhase == -1) {
                this.observationPhase = n;
            }
            if (this.observationFailure == null) {
                this.observationFailure = throwable;
            }
            this.session.observationFailed(null, n, throwable);
        }

        boolean extraAuthorityRetained() {
            return this.lifetime() && this.session.extraAuthorityRetained();
        }

        void retired(Object object) {
            if (this.prepared != object) {
                return;
            }
            this.leaveLeaf();
            this.bridge = null;
            if (this.child != null) {
                this.closeChild(this.child);
            }
            this.phase = Phase.RETIRED;
        }
    }

    static final class OriginalChild {
        private final Accepted owner;
        private final Object commit;
        private final FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private final LivingEntity target;
        private final DamageSource source;
        private final int amountBits;
        private boolean entered;
        private boolean returned;
        private boolean result;
        private boolean closed;
        private Throwable originalFailure;

        private OriginalChild(Accepted accepted, Object object, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, LivingEntity livingEntity, DamageSource damageSource, float f) {
            this.owner = accepted;
            this.commit = object;
            this.bridge = childHurtBridge;
            this.target = livingEntity;
            this.source = damageSource;
            this.amountBits = Float.floatToRawIntBits(f);
        }

        boolean entered() {
            if (this.entered || this.closed || !this.owner.matchesChild(this, this.commit, this.bridge, this.target, this.source, Float.intBitsToFloat(this.amountBits))) {
                return false;
            }
            this.entered = true;
            return true;
        }

        void returned(boolean bl) {
            if (!this.entered || this.returned || this.closed) {
                return;
            }
            this.result = bl;
            this.returned = true;
        }

        void originalThrown(Throwable throwable) {
            if (this.originalFailure == null) {
                this.originalFailure = throwable;
            }
            this.owner.originalThrown(throwable);
        }
    }

    private static enum Phase {
        PUBLISHING,
        PUBLISHED,
        APPLYING,
        FINISHED,
        CANCELLED,
        FAILED,
        RETIRED;

    }
}

