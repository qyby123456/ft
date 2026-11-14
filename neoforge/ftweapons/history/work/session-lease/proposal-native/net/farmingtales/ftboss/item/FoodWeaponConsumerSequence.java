package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

final class FoodWeaponConsumerSequence {
    private FoodWeaponConsumerSequence() {
    }

    static boolean finalCaller(String var0, String var1, String var2) {
        return Thread.holdsLock(FoodWeaponSourceLedger.class)
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var3 -> var3.filter(
                                var0xx -> var0xx.getDeclaringClass() != FoodWeaponConsumerSequence.class
                                        && var0xx.getDeclaringClass() != FoodWeaponConsumerSequence.Accepted.class
                                        && var0xx.getDeclaringClass() != FoodSocketPowers.class
                                        && var0xx.getDeclaringClass() != FoodSocketAttributePowers.class
                                        && var0xx.getDeclaringClass() != FoodWeaponBossPassive.class
                            )
                            .findFirst()
                            .map(
                                var3x -> var3x.getDeclaringClass().getNestHost() == FoodWeaponFinalDamage.class
                                        && var3x.getDeclaringClass().getName().equals(FoodWeaponFinalDamage.class.getName() + "$" + var0)
                                        && var3x.getMethodName().equals(var1)
                                        && var3x.getDescriptor().equals(var2)
                            )
                            .orElse(false)
                );
    }

    private static boolean weaponContinuous(ServerPlayer player, DamageSource source, ItemStack weapon, InteractionHand hand) {
        if (player.getItemInHand(hand) == weapon) return true;
        if (source.getDirectEntity() == null
            || source.getDirectEntity().getClass() != net.minecraft.world.entity.projectile.ThrownTrident.class
            || source.getEntity() != player || weapon.getCount() != 1) return false;
        FoodWeaponBondHits.Hit hit = FoodWeaponBondHits.resolve(source, player);
        return hit != null && hit.kind() == net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind.TRIDENT
            && "NATIVE_PROJECTILE".equals(hit.snapshot().getString("DamagePurpose"))
            && "TRIDENT".equals(hit.snapshot().getString("NativeEntry"))
            && hand.name().equals(hit.snapshot().getString("OriginalHand"))
            && FoodWeaponBondHits.equipped(player, hit) == weapon;
    }

    static FoodWeaponConsumerSequence.Accepted bind(
        FoodWeaponOriginalSequence.Session var0,
        Object var1,
        int var2,
        Object var3,
        Object var4,
        FoodWeaponFinalDamage.OffenseReceipt var5,
        FoodWeaponFinalDamage.PublishingCleanup var6,
        FoodWeaponSourceLedger.AttemptBatchQuote var7,
        FoodWeaponSourceLedger.AttemptBatchAdmission var8,
        List<FoodWeaponSourceLedger.AttemptRequest> var9,
        List<FoodWeaponSourceLedger.TargetRequest> var10,
        ServerPlayer var11,
        LivingEntity var12,
        DamageSource var13,
        ItemStack var14,
        InteractionHand var15,
        long var16,
        float var18,
        String var19
    ) {
        if (finalCaller(
                var19,
                "publish",
                "(FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)Z"
            )
            && var0 != null
            && var1 != null
            && var2 >= 0
            && var3 != null
            && var4 != null
            && var5 != null
            && var6 != null
            && var7 != null
            && var8 != null
            && var9 != null
            && var10 != null
            && var11 != null
            && var12 != null
            && var13 != null
            && var14 != null
            && var15 != null
            && Float.isFinite(var18)
            && !(var18 <= 0.0F)
            && var0.lineage.acceptedCallback == var5
            && var0.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication
            && var0.original.continuing(var0.lineage.publication)
            && var0.original.atProducer(var1)
            && FoodWeaponFinalDamage.matchesOwnerWriterSelection(var5, var1, var2, var3, var3, 0)
            && var5.matchesBatch(var7, var8, var9, var10)
            && var5.matches(var4, var11, var13, var16, var18)
            && weaponContinuous(var11, var13, var14, var15)
            && var14.getCount() == 1
            && var8.status() == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var20 = FoodWeaponSourceLedger.originalInteractionBinding(var7);
            if (var20 != null && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var20, var20, var11)) {
                EnumMap var21 = new EnumMap<>(FoodWeaponSourceLedger.AttemptDomain.class);

                for (FoodWeaponSourceLedger.AttemptRequest var23 : var9) {
                    FoodWeaponSourceLedger.AttemptResult var24 = var5.attempt(var23.domain()).orElse(null);
                    if (var24 == null || var24.domain() != var23.domain() || var21.put(var23.domain(), var24) != null) {
                        return null;
                    }
                }

                FoodWeaponConsumerSequence.Accepted var25 = new FoodWeaponConsumerSequence.Accepted(
                    var0, var1, var3, var5, var6, var20, var11, var12, var13, var14, var15, var16, var18, var19, var21
                );
                return var0.original.enterLeaf(var1, var25, 0) ? var25 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static Object nativePublicationKey(FoodWeaponOriginalSequence.Session session, Object leaf) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerTimerSemanticAuthority.Book.class
                    && (frame.getMethodName().equals("resolve") || frame.getMethodName().equals("returned"))
                || frame.getDeclaringClass() == FoodWeaponSourceLedger.class && frame.getMethodName().equals("ownerPassivePublicationEntered"))
            .orElse(false));
        if (!caller) return null;
        if (!(leaf instanceof Accepted accepted)) return leaf;
        return accepted.session == session && accepted.producerName.equals("PassiveProducer")
            && accepted.phase == Phase.PUBLISHING && accepted.lifetime() && session.original.atLeaf(accepted)
            ? FoodWeaponBossPassive.ownerPublicationPlan(accepted.prepared, accepted) : null;
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
        private FoodWeaponConsumerSequence.Phase phase = FoodWeaponConsumerSequence.Phase.PUBLISHING;
        private Object commit;
        private FoodWeaponFinalDamage.ChildHurtBridge bridge;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private int observationPhase = -1;
        private int leafOrdinal;
        private boolean leafOpen = true;
        private int publicationCalls;
        private FoodWeaponConsumerSequence.OriginalChild child;

        private Accepted(
            FoodWeaponOriginalSequence.Session var1,
            Object var2,
            Object var3,
            FoodWeaponFinalDamage.OffenseReceipt var4,
            FoodWeaponFinalDamage.PublishingCleanup var5,
            FoodWeaponSourceLedger.OriginalInteractionBinding var6,
            ServerPlayer var7,
            LivingEntity var8,
            DamageSource var9,
            ItemStack var10,
            InteractionHand var11,
            long var12,
            float var14,
            String var15,
            EnumMap<FoodWeaponSourceLedger.AttemptDomain, FoodWeaponSourceLedger.AttemptResult> var16
        ) {
            this.session = var1;
            this.producer = var2;
            this.prepared = var3;
            this.receipt = var4;
            this.publishingCleanup = var5;
            this.binding = var6;
            this.player = var7;
            this.target = var8;
            this.source = var9;
            this.weapon = var10;
            this.hand = var11;
            this.tick = var12;
            this.amountBits = Float.floatToRawIntBits(var14);
            this.producerName = var15;
            this.results = var16;
            this.thread = Thread.currentThread();
            this.server = var7.serverLevel().getServer();
        }

        private boolean cleanupLifetime() {
            return this.phase != FoodWeaponConsumerSequence.Phase.RETIRED
                && Thread.holdsLock(FoodWeaponSourceLedger.class)
                && Thread.currentThread() == this.thread
                && this.server == this.player.serverLevel().getServer()
                && this.server.isSameThread()
                && FoodWeaponSourceLedger.clock(this.player) == this.tick
                && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(this.binding, this.binding, this.player);
        }

        private boolean lifetime() {
            return this.originalFailure == null
                && this.phase != FoodWeaponConsumerSequence.Phase.CANCELLED
                && this.phase != FoodWeaponConsumerSequence.Phase.FAILED
                && this.phase != FoodWeaponConsumerSequence.Phase.RETIRED
                && Thread.holdsLock(FoodWeaponSourceLedger.class)
                && Thread.currentThread() == this.thread
                && this.server == this.player.serverLevel().getServer()
                && this.server.isSameThread()
                && FoodWeaponSourceLedger.clock(this.player) == this.tick
                && this.player.level() == this.target.level()
                && this.session.original.continuing(this.session.lineage.publication)
                && this.session.original.atProducer(this.producer)
                && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(this.binding, this.binding, this.player);
        }

        boolean publishing(Object var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3) {
            return this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING
                && this.prepared == var1
                && this.receipt == var2
                && this.publishingCleanup == var3
                && this.lifetime()
                && this.session.original.atLeaf(this);
        }

        boolean matchesReceipt(Object var1, FoodWeaponFinalDamage.OffenseReceipt var2, float var3) {
            return this.prepared == var1
                && this.receipt == var2
                && Float.floatToRawIntBits(var3) == this.amountBits
                && (this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING || this.phase == FoodWeaponConsumerSequence.Phase.APPLYING)
                && this.lifetime()
                && this.session.original.atLeaf(this);
        }

        Optional<FoodWeaponSourceLedger.AttemptResult> attempt(FoodWeaponSourceLedger.AttemptDomain var1) {
            return !this.lifetime() || this.phase != FoodWeaponConsumerSequence.Phase.PUBLISHING && this.phase != FoodWeaponConsumerSequence.Phase.APPLYING
                ? Optional.empty()
                : Optional.ofNullable(this.results.get(var1));
        }

        private void leaveLeaf() {
            if (this.leafOpen) {
                this.leafOpen = false;
                this.session.original.leaveLeaf(this);
            }
        }

        OwnerNativeSemanticAuthority.Admission passiveCopy(CompoundTag root, CompoundTag original) {
            boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
                .map(frame -> frame.getDeclaringClass() == FoodWeaponBossPassive.class && frame.getMethodName().equals("commitPassiveHit")
                    && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$PreparedHit;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodWeaponBossPassive$HitCommit;"))
                .orElse(false));
            return caller && this.producerName.equals("PassiveProducer") && this.phase == Phase.PUBLISHING
                && this.lifetime() && this.session.original.atLeaf(this)
                ? FoodWeaponSourceLedger.ownerPassivePublicationEntered(this.session, this, root, original) : null;
        }

        int publicationCallEntered(Object var1) {
            if (this.prepared == var1 && this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING && Thread.currentThread() == this.thread) {
                this.publicationCalls++;
                return this.publicationCalls;
            } else {
                return 0;
            }
        }

        void publicationExited(Object var1, int var2, Throwable var3) {
            if (this.prepared == var1 && var2 > 0 && this.publicationCalls == var2 && Thread.currentThread() == this.thread) {
                this.publicationCalls--;
                if (this.publicationCalls == 0 && this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING) {
                    if (var3 != null) {
                        this.originalThrown(var3);
                    }

                    this.published(var1, null);
                }
            }
        }

        void publicationCleanupExited(Object var1, FoodWeaponFinalDamage.PublishingCleanup var2, Throwable var3) {
            if (this.prepared == var1
                && this.publishingCleanup == var2
                && this.phase != FoodWeaponConsumerSequence.Phase.RETIRED
                && this.publicationCalls == 0
                && Thread.currentThread() == this.thread) {
                if (var3 != null && this.originalFailure == null) {
                    this.originalFailure = var3;
                }

                this.retired(var1);
            }
        }

        void publicationRetirementExited(
            Object var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponSourceLedger.OriginalInteractionBinding var3, float var4, Throwable var5
        ) {
            if (this.prepared == var1
                && this.receipt == var2
                && this.binding == var3
                && Float.floatToRawIntBits(var4) == this.amountBits
                && this.phase != FoodWeaponConsumerSequence.Phase.RETIRED
                && this.publicationCalls == 0
                && Thread.currentThread() == this.thread) {
                if (var5 != null && this.originalFailure == null) {
                    this.originalFailure = var5;
                }

                this.retired(var1);
            }
        }

        void published(Object var1, Object var2) {
            if (this.prepared == var1 && this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING) {
                this.leaveLeaf();
                this.commit = var2;
                this.phase = var2 != null
                    ? FoodWeaponConsumerSequence.Phase.PUBLISHED
                    : (this.originalFailure == null ? FoodWeaponConsumerSequence.Phase.CANCELLED : FoodWeaponConsumerSequence.Phase.FAILED);
            }
        }

        boolean enter(Object var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            if (this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHED
                && this.commit == var1
                && var2 != null
                && this.bridge == null
                && this.lifetime()
                && FoodWeaponConsumerSequence.finalCaller(
                    this.producerName, "apply", "(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ListedProducerInvocation;)V"
                )
                && this.session.original.enterLeaf(this.producer, this, this.leafOrdinal + 1)) {
                this.leafOpen = true;
                this.leafOrdinal++;
                this.bridge = var2;
                this.phase = FoodWeaponConsumerSequence.Phase.APPLYING;
                return true;
            } else {
                return false;
            }
        }

        boolean continuing(Object var1, FoodWeaponFinalDamage.ChildHurtBridge var2) {
            return this.phase == FoodWeaponConsumerSequence.Phase.APPLYING
                && this.commit == var1
                && this.bridge == var2
                && this.lifetime()
                && this.session.original.atLeaf(this);
        }

        boolean ordinaryContext() {
            return this.lifetime()
                && this.player.isAlive()
                && !this.player.isSpectator()
                && !this.player.isRemoved()
                && weaponContinuous(this.player, this.source, this.weapon, this.hand)
                && this.weapon.getCount() == 1
                && !FoodWeaponLifecycle.isBroken(this.weapon)
                && !FoodWeaponBondHits.secondary(this.source)
                && FoodWeaponBondHits.hostile(this.player, this.target)
                && this.target.isAlive()
                && !this.target.isRemoved();
        }

        boolean cleanup(Object var1, FoodWeaponFinalDamage.PublishingCleanup var2) {
            return this.prepared == var1 && this.publishingCleanup == var2 && this.cleanupLifetime()
                ? (this.phase == FoodWeaponConsumerSequence.Phase.APPLYING || this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHING)
                        && this.session.original.atLeaf(this)
                    || (
                            this.phase == FoodWeaponConsumerSequence.Phase.PUBLISHED
                                || this.phase == FoodWeaponConsumerSequence.Phase.FINISHED
                                || this.phase == FoodWeaponConsumerSequence.Phase.CANCELLED
                                || this.phase == FoodWeaponConsumerSequence.Phase.FAILED
                        )
                        && Thread.holdsLock(FoodWeaponSourceLedger.class)
                        && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                            .walk(
                                var0 -> var0.filter(
                                            var0x -> var0x.getDeclaringClass() != FoodWeaponConsumerSequence.class
                                                    && var0x.getDeclaringClass() != FoodWeaponConsumerSequence.Accepted.class
                                                    && var0x.getDeclaringClass() != FoodSocketPowers.class
                                                    && var0x.getDeclaringClass() != FoodSocketAttributePowers.class
                                                    && var0x.getDeclaringClass() != FoodWeaponBossPassive.class
                                        )
                                        .findFirst()
                                        .map(
                                            var0x -> var0x.getDeclaringClass() == FoodWeaponFinalDamage.class
                                                    && var0x.getMethodName().equals("finalizeFailedPublication")
                                                    && var0x.getDescriptor()
                                                        .equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")
                                        )
                                        .orElse(false)
                            )
                : false;
        }

        FoodWeaponConsumerSequence.OriginalChild beginChild(
            Object var1, FoodWeaponFinalDamage.ChildHurtBridge var2, LivingEntity var3, DamageSource var4, float var5
        ) {
            if (this.continuing(var1, var2)
                && this.child == null
                && var3 != null
                && var4 != null
                && Float.isFinite(var5)
                && !(var5 <= 0.0F)
                && var3.level() == this.player.level()
                && FoodWeaponBondHits.hostile(this.player, var3)
                && var3.isAlive()
                && !var3.isRemoved()) {
                this.child = new FoodWeaponConsumerSequence.OriginalChild(this, var1, var2, var3, var4, var5);
                return this.child;
            } else {
                return null;
            }
        }

        boolean matchesChild(
            FoodWeaponConsumerSequence.OriginalChild var1,
            Object var2,
            FoodWeaponFinalDamage.ChildHurtBridge var3,
            LivingEntity var4,
            DamageSource var5,
            float var6
        ) {
            return this.child != null
                && this.child == var1
                && !this.child.closed
                && this.child.owner == this
                && this.child.commit == var2
                && this.child.bridge == var3
                && this.child.target == var4
                && this.child.source == var5
                && this.child.amountBits == Float.floatToRawIntBits(var6)
                && this.continuing(var2, var3);
        }

        void closeChild(FoodWeaponConsumerSequence.OriginalChild var1) {
            if (this.child == var1 && var1 != null) {
                var1.closed = true;
                this.child = null;
            }
        }

        void finish(Object var1, FoodWeaponFinalDamage.ChildHurtBridge var2, Throwable var3) {
            if (this.commit == var1 && this.bridge == var2 && this.phase == FoodWeaponConsumerSequence.Phase.APPLYING) {
                if (this.child != null) {
                    this.closeChild(this.child);
                }

                if (var3 != null) {
                    this.originalThrown(var3);
                }

                this.leaveLeaf();
                this.bridge = null;
                this.phase = var3 == null ? FoodWeaponConsumerSequence.Phase.FINISHED : FoodWeaponConsumerSequence.Phase.FAILED;
            }
        }

        void originalThrown(Throwable var1) {
            if (var1 != null) {
                if (this.originalFailure == null) {
                    this.originalFailure = var1;
                }

                this.session.original.originalThrown(var1);
            }
        }

        void observationFailed(int var1, Throwable var2) {
            if (this.observationPhase == -1) {
                this.observationPhase = var1;
            }

            if (this.observationFailure == null) {
                this.observationFailure = var2;
            }

            this.session.observationFailed(null, var1, var2);
        }

        boolean extraAuthorityRetained() {
            return this.lifetime() && this.session.extraAuthorityRetained();
        }

        void retired(Object var1) {
            if (this.prepared == var1) {
                this.leaveLeaf();
                this.bridge = null;
                if (this.child != null) {
                    this.closeChild(this.child);
                }

                this.phase = FoodWeaponConsumerSequence.Phase.RETIRED;
            }
        }
    }

    static final class OriginalChild {
        private final FoodWeaponConsumerSequence.Accepted owner;
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

        private OriginalChild(
            FoodWeaponConsumerSequence.Accepted var1, Object var2, FoodWeaponFinalDamage.ChildHurtBridge var3, LivingEntity var4, DamageSource var5, float var6
        ) {
            this.owner = var1;
            this.commit = var2;
            this.bridge = var3;
            this.target = var4;
            this.source = var5;
            this.amountBits = Float.floatToRawIntBits(var6);
        }

        boolean entered() {
            if (!this.entered
                && !this.closed
                && this.owner.matchesChild(this, this.commit, this.bridge, this.target, this.source, Float.intBitsToFloat(this.amountBits))) {
                this.entered = true;
                return true;
            } else {
                return false;
            }
        }

        void returned(boolean var1) {
            if (this.entered && !this.returned && !this.closed) {
                this.result = var1;
                this.returned = true;
            }
        }

        void originalThrown(Throwable var1) {
            if (this.originalFailure == null) {
                this.originalFailure = var1;
            }

            this.owner.originalThrown(var1);
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
