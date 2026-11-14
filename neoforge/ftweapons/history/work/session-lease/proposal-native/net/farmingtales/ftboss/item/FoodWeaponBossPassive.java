package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponBossPassive {
    private static final int PROC_COOLDOWN_TICKS = 60;
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final String TIMER_ROOT = "FTBossIntrinsicPassiveTimers";
    private static final String PROJECTILE_PROFILE = "FTBossIntrinsicProjectileProfile";

    private FoodWeaponBossPassive() {
    }

    public static String profile(ItemStack var0) {
        return FoodWeaponEvolution.isNativeWeapon(var0) ? FoodWeaponEvolution.firstProfileId(var0) : "";
    }

    public static WeaponDetailView detail(ItemStack var0) {
        String var1 = profile(var0);
        return var1.isEmpty()
            ? null
            : new WeaponDetailView(
                Component.translatable("screen.ftboss.skills.boss_passive"),
                Component.translatable("passive.ftboss.boss_weapon." + var1),
                Component.translatable("passive.ftboss.boss_weapon." + var1 + ".description")
            );
    }

    private static boolean ready(ServerPlayer var0, String var1) {
        long var2 = var0.serverLevel().getServer().overworld().getGameTime();
        CompoundTag var4 = var0.getPersistentData().getCompound("FTBossIntrinsicPassiveTimers");
        long var5 = var4.getLong(var1);
        if (var4.contains(var1) && var2 >= var5 && var2 - var5 < 60L) {
            return false;
        } else {
            var4.putLong(var1, var2);
            var0.getPersistentData().put("FTBossIntrinsicPassiveTimers", var4);
            return true;
        }
    }

    @SubscribeEvent
    public static void projectile(EntityJoinLevelEvent var0) {
        if (!var0.getLevel().isClientSide
            && var0.getEntity() instanceof AbstractArrow var1
            && var1.getOwner() instanceof ServerPlayer var2
            && !var1.getPersistentData().contains("FTBossIntrinsicProjectileProfile")) {
            FoodWeaponSourceLedger.nativePassiveProjectileProfile(var1, var2);
        }
    }

    private static String attackProfile(ServerPlayer var0, DamageSource var1) {
        if (var1.getDirectEntity() == var0) {
            return profile(var0.getMainHandItem());
        } else {
            if (var1.getDirectEntity() instanceof AbstractArrow var2 && var2.getOwner() == var0) {
                return var2.getPersistentData().getString("FTBossIntrinsicProjectileProfile");
            }

            return "";
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void attack(BeforeMitigation var0) {
        if (FoodWeaponFinalDamage.nativeOriginalInteractionActive(var0.getSource(), var0.getEntity())) {
            captureNativePassive202(var0);
            return;
        }
        if (!(var0.getAmount() <= 0.0F)
            && var0.getSource().getEntity() instanceof ServerPlayer var1
            && var1.isAlive()
            && !var1.isSpectator()
            && var0.getEntity() instanceof Monster var2
            && !var2.isAlliedTo(var1)
            && (var0.getSource().getDirectEntity() == var1 || var0.getSource().getDirectEntity() instanceof AbstractArrow)) {
            String var7 = attackProfile(var1, var0.getSource());
            if (var7.equals("benger") && var2.getHealth() <= var2.getMaxHealth() * 0.3F) {
                var0.setAmount(var0.getAmount() * 1.1F);
            } else if (var7.equals("sugar") || var7.equals("butcher") || var7.equals("saya") || var7.equals("wine") || var7.equals("defect")) {
                if (ready(var1, var7)) {
                    switch (var7) {
                        case "sugar":
                            var2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), var1);
                            break;
                        case "butcher":
                            var1.heal(1.0F);
                            break;
                        case "saya":
                            var2.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0), var1);
                            break;
                        case "wine":
                            var2.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0), var1);
                            break;
                        case "defect":
                            var0.setAmount(var0.getAmount() + 1.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void defend(BeforeMitigation var0) {
        if (!(var0.getAmount() <= 0.0F) && var0.getEntity() instanceof ServerPlayer var1 && var1.isAlive() && !var1.isSpectator()) {
            String var3 = profile(var1.getMainHandItem());
            if (var3.equals("compost")) {
                var0.setAmount(var0.getAmount() * 0.92F);
            }

            if (var3.equals("tide") && var0.getSource().getDirectEntity() instanceof AbstractArrow) {
                var0.setAmount(var0.getAmount() * 0.9F);
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void kill(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof Monster var1
            && var0.getSource().getEntity() instanceof ServerPlayer var2
            && var2.isAlive()
            && !var2.isSpectator()
            && !var1.isAlliedTo(var2)
            && !LemonSummonRewards.suppressed(var1)) {
            String var5 = attackProfile(var2, var0.getSource());
            if ((var5.equals("rat") || var5.equals("apple")) && ready(var2, var5)) {
                if (var5.equals("rat")) {
                    var2.getFoodData().eat(1, 0.0F);
                } else {
                    var2.heal(1.0F);
                }

                return;
            }

            return;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        WeaponDetailView var1 = detail(var0.getItemStack());
        if (var1 != null) {
            var0.getToolTip().add(var1.category().copy().append(Component.literal(": ")).append(var1.title()).withStyle(ChatFormatting.GOLD));
            var0.getToolTip().add(var1.description().copy().withStyle(ChatFormatting.GRAY));
        }
    }

    public static void applyPassiveHit(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null
            && !var0.executed
            && Thread.currentThread() == var0.thread
            && var0.server == var0.plan.context.player().serverLevel().getServer()
            && var0.server.isSameThread()) {
            FoodWeaponConsumerSequence.Accepted var2 = var0.prepared.originalSequence;
            if (var2 == null || var2.enter(var0, var1)) {
                Throwable var3 = null;

                try {
                    var0.executed = true;
                    if (!publishedPassiveHit(var0)) {
                        return;
                    }

                    if ((var2 == null ? !FoodWeaponBondHits.matchesContext(var0.plan.context) : !var2.ordinaryContext())
                        || !matchesPassiveHitReceipt(var0.prepared, var0.amount, var0.receipt)) {
                        restoreHitTimer(var0, var1);
                        return;
                    }

                    if (!passiveHitLeafLive(var0, var1)) {
                        return;
                    }

                    applyHit(var0, var1);
                    if (passiveHitLeafLive(var0, var1)) {
                        return;
                    }
                } catch (Error | RuntimeException var8) {
                    var3 = var8;
                    throw var8;
                } finally {
                    if (var2 != null) {
                        var2.finish(var0, var1, var3);
                    }
                }
            }
        }
    }

    static boolean bindOriginalSequence(
        FoodWeaponBossPassive.PreparedHit var0,
        FoodWeaponOriginalSequence.Session var1,
        Object var2,
        int var3,
        FoodWeaponFinalDamage.OffenseReceipt var4,
        float var5,
        FoodWeaponFinalDamage.PublishingCleanup var6
    ) {
        if (var0 != null
            && !var0.publishAttempted
            && var0.originalSequence == null
            && FoodWeaponFinalDamage.matchesPassiveHitPublishing(var0, var4, var6)
            && matchesPassiveHitReceipt(var0, var5, var4)) {
            FoodWeaponBossPassive.CompleteBatchBinding var7 = var0.binding;
            FoodWeaponConsumerSequence.Accepted var8 = FoodWeaponConsumerSequence.bind(
                var1,
                var2,
                var3,
                var0,
                var0.plan,
                var4,
                var6,
                var7.batch,
                var7.admission,
                var7.requests,
                var7.targets,
                var0.plan.context.player(),
                var0.plan.context.target(),
                var0.plan.context.source(),
                var0.plan.context.actualWeapon(),
                var0.plan.context.originalHand(),
                var0.plan.context.quotedAt(),
                var5,
                "PassiveProducer"
            );
            if (var8 == null) {
                return false;
            } else {
                var0.originalSequence = var8;
                return true;
            }
        } else {
            return false;
        }
    }

    static FoodWeaponBossPassive.HitCommit commitPassiveHit(
        FoodWeaponBossPassive.PreparedHit var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3
    ) {
        FoodWeaponConsumerSequence.Accepted var4 = var0 == null ? null : var0.originalSequence;
        int var5 = var4 == null ? 0 : var4.publicationCallEntered(var0);
        Throwable var6 = null;

        FoodWeaponBossPassive.HitCommit var7;
        try {
            if (var0 == null || var0.publishAttempted || var0.publishingCleanup != null || !FoodWeaponFinalDamage.matchesPassiveHitPublishing(var0, var2, var3)
                )
             {
                return null;
            }

            var0.publishingCleanup = var3;
            var7 = commitPassiveHit(var0, var1, var2);
        } catch (Throwable var11) {
            var6 = var11;
            throw var11;
        } finally {
            if (var4 != null) {
                var4.publicationExited(var0, var5, var6);
            }
        }

        return var7;
    }

    static void finishPassiveHitPublication(FoodWeaponBossPassive.PreparedHit var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        FoodWeaponConsumerSequence.Accepted var2 = var0 == null ? null : var0.originalSequence;
        Throwable var3 = null;

        try {
            if (!preparedCleanupCaller()
                || var0 == null
                || var0.publishingCleanup != var1
                || (var0.originalSequence == null || !var0.originalSequence.cleanup(var0, var1))
                    && !FoodWeaponFinalDamage.matchesPassiveHitPublicationCleanup(var0, var1)
                || !var0.publishAttempted
                || !var0.publishExited
                || var0.publicationReceipt == null) {
                return;
            }

            FoodWeaponBossPassive.CompleteBatchBinding var4 = var0.binding;
            if (!matchesPreparedPassiveHit(var0, var0.plan, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return;
            }

            var0.finalizingCleanup = var1;

            try {
                retireHitPublication(var0, FoodWeaponSourceLedger.originalInteractionBinding(var4.batch));
            } finally {
                if (var0.originalSequence != null) {
                    var0.originalSequence.retired(var0);
                }
            }
        } catch (Throwable var14) {
            var3 = var14;
            throw var14;
        } finally {
            if (var2 != null) {
                var2.publicationCleanupExited(var0, var1, var3);
            }
        }
    }

    static boolean matchesPassiveHitCommit(
        FoodWeaponBossPassive.HitCommit var0,
        FoodWeaponBossPassive.PreparedHit var1,
        FoodWeaponBossPassive.HitPlan var2,
        FoodWeaponFinalDamage.OffenseReceipt var3,
        float var4,
        FoodWeaponSourceLedger.AttemptBatchQuote var5,
        FoodWeaponSourceLedger.AttemptBatchAdmission var6,
        List<FoodWeaponSourceLedger.AttemptRequest> var7,
        List<FoodWeaponSourceLedger.TargetRequest> var8
    ) {
        return var0 != null
            && var1 != null
            && var0.prepared == var1
            && var0.plan == var2
            && var2 != null
            && var2.committed
            && var3 != null
            && var0.receipt == var3
            && Float.isFinite(var4)
            && var4 > 0.0F
            && Float.floatToRawIntBits(var0.amount) == Float.floatToRawIntBits(var4)
            && matchesPreparedPassiveHit(var1, var2, var5, var6, var7, var8);
    }

    static boolean matchesPreparedPassiveHit(
        FoodWeaponBossPassive.PreparedHit var0,
        FoodWeaponBossPassive.HitPlan var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        List<FoodWeaponSourceLedger.AttemptRequest> var4,
        List<FoodWeaponSourceLedger.TargetRequest> var5
    ) {
        return var0 != null
            && var1 != null
            && var0.plan == var1
            && var2 != null
            && var3 != null
            && var4 != null
            && var5 != null
            && var0.binding.batch == var2
            && var0.binding.admission == var3
            && var0.binding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(var2)
            && var0.binding.originalBinding != null
            && var0.binding.requests.equals(var4)
            && var0.binding.targets.equals(var5);
    }

    public static FoodWeaponBossPassive.PreparedHit preparePassiveHit(
        FoodWeaponBossPassive.HitPlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        if (!matchesPassiveHitBatch(var0, var1, var2, var3, var4)) {
            return null;
        } else {
            FoodWeaponBossPassive.CompleteBatchBinding var5 = new FoodWeaponBossPassive.CompleteBatchBinding(var1, var2, var3, var4);
            return var5.originalBinding == null ? null : new FoodWeaponBossPassive.PreparedHit(var0, var5);
        }
    }

    private static boolean publishedPassiveHit(FoodWeaponBossPassive.HitCommit var0) {
        FoodWeaponBossPassive.HitPlan var1 = var0.plan;
        return var1.context.player().getPersistentData().get("FTBossIntrinsicPassiveTimers") == var0.publishedTimers
            && var0.publishedTimers.contains(var1.profile, 4)
            && var0.publishedTimers.getLong(var1.profile) == var1.context.quotedAt()
            && Objects.equals(var0.publishedTimers.get(var1.profile), var0.publishedTimer);
    }

    private static boolean matchesPassiveHitReceipt(FoodWeaponBossPassive.PreparedHit var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2) {
        if (var0 != null && var0.originalSequence != null) {
            return var0.originalSequence.matchesReceipt(var0, var2, var1);
        } else if (var0 != null && var2 != null && Float.isFinite(var1) && !(var1 <= 0.0F)) {
            FoodWeaponBossPassive.CompleteBatchBinding var3 = var0.binding;
            FoodWeaponBondHits.Context var4 = var0.plan.context;
            return var2.matchesBatch(var3.batch, var3.admission, var3.requests, var3.targets)
                && var2.matches(var0.plan, var4.player(), var4.source(), var4.quotedAt(), var1)
                && var2.attempt(FoodWeaponSourceLedger.AttemptDomain.PASSIVE_HIT)
                    .filter(
                        var0x -> var0x.domain() == FoodWeaponSourceLedger.AttemptDomain.PASSIVE_HIT
                                && var0x.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                    )
                    .isPresent();
        } else {
            return false;
        }
    }

    private static void restoreHitTimer(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (ownsHitTimerCleanup(var0, var1)) {
            var0.cleanupCompleted = true;
            retireHitPublication(var0.prepared, FoodWeaponSourceLedger.originalInteractionBinding(var0.prepared.binding.batch));
        }
    }

    private static boolean passiveHitLeafLive(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null && var0.prepared.originalSequence != null
            ? var0.executed && publishedPassiveHit(var0) && var0.prepared.originalSequence.continuing(var0, var1)
            : var0 != null
                && var0.executed
                && publishedPassiveHit(var0)
                && matchesPassiveHitReceipt(var0.prepared, var0.amount, var0.receipt)
                && FoodWeaponFinalDamage.matchesPassiveHitInvocation(var0, var1);
    }

    private static void applyHit(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        FoodWeaponBossPassive.HitPlan var2 = var0.plan;
        ServerPlayer var3 = var2.context.player();
        LivingEntity var4 = var2.context.target();
        if (passiveHitLeafLive(var0, var1)) {
            String var5 = var2.profile;
            switch (var5) {
                case "sugar":
                    var4.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), var3);
                    break;
                case "butcher":
                    var3.heal(1.0F);
                    break;
                case "saya":
                    var4.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0), var3);
                    break;
                case "wine":
                    var4.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0), var3);
            }

            if (passiveHitLeafLive(var0, var1)) {
                ;
            }
        }
    }

    public static FoodWeaponBossPassive.HitCommit commitPassiveHit(
        FoodWeaponBossPassive.PreparedHit var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2
    ) {
        FoodWeaponConsumerSequence.Accepted var3 = var0 == null ? null : var0.originalSequence;
        boolean var4 = false;
        int var5 = var3 == null ? 0 : var3.publicationCallEntered(var0);
        Throwable var6 = null;
        OwnerNativeSemanticAuthority.Admission passive = null;

        FoodWeaponBossPassive.HitCommit var24;
        try {
            if (var0 == null
                || var0.publishAttempted
                || var0.publishingCleanup != null && !FoodWeaponFinalDamage.matchesPassiveHitPublishing(var0, var2, var0.publishingCleanup)) {
                return null;
            }

            FoodWeaponBossPassive.HitPlan var7 = var0.plan;
            if (!hitContext(var7) || !matchesPassiveHitReceipt(var0, var1, var2)) {
                return null;
            }

            var0.publishAttempted = true;
            var0.publicationReceipt = var2;
            var0.publicationAmountBits = Float.floatToRawIntBits(var1);
            var4 = true;
            CompoundTag var8 = var7.context.player().getPersistentData();
            CompoundTag originalTimers = var8.getCompound("FTBossIntrinsicPassiveTimers");
            passive = var3 == null ? null : var3.passiveCopy(var8, originalTimers);
            CompoundTag var9 = originalTimers.copy();
            OwnerNativeSemanticAuthority.passiveCopied(passive, originalTimers, var9);
            var9.putLong(var7.profile, var7.context.quotedAt());
            OwnerNativeSemanticAuthority.passivePrepared(passive, var9, var7.profile, var7.context.quotedAt());
            var7.committed = true;
            FoodWeaponBossPassive.HitPublication var10 = new FoodWeaponBossPassive.HitPublication(var0, var8, var9);
            var0.publication = var10;
            var10.persistentLookupEntered = true;
            CompoundTag var11 = var7.context.player().getPersistentData();
            var10.persistent = var11;
            var10.persistentLookupCompleted = true;
            var10.preimageReadEntered = true;
            var10.previousRoot = var11.get("FTBossIntrinsicPassiveTimers");

            for (String var13 : var10.keys) {
                Tag var14 = var10.previousRoot instanceof CompoundTag var15 ? var15.get(var13) : null;
                if (var14 != null) {
                    var10.previous.put(var13, var14.copy());
                }
            }

            var10.preimageReadCompleted = true;
            var10.rootWriteEntered = true;
            OwnerNativeSemanticAuthority.passiveEntering(passive, var11, "FTBossIntrinsicPassiveTimers", var9);
            Tag previousRoot = var11.put("FTBossIntrinsicPassiveTimers", var9);
            OwnerNativeSemanticAuthority.passiveReturned(passive, var11, "FTBossIntrinsicPassiveTimers", var9, previousRoot);
            OwnerNativeSemanticAuthority.passiveClose(passive);
            var10.rootWriteCompleted = true;
            if (readBackHitPublication(var10)) {
                var24 = new FoodWeaponBossPassive.HitCommit(var0, var2, var1, var9);
                var0.commitConstructed = true;
                if (var0.originalSequence != null) {
                    var0.originalSequence.published(var0, var24);
                }

                return var24;
            }

            var24 = null;
        } catch (Throwable var20) {
            OwnerNativeSemanticAuthority.passiveFailed(passive, var20);
            var6 = var20;
            if (var4 && (var20 instanceof RuntimeException || var20 instanceof Error)) {
                var0.publishFailure = var20;
                if (var0.originalSequence != null) {
                    var0.originalSequence.originalThrown(var20);
                }
            }

            throw var20;
        } finally {
            OwnerNativeSemanticAuthority.passiveClose(passive);
            if (var4) {
                var0.publishExited = true;
            }

            if (var3 != null) {
                var3.publicationExited(var0, var5, var6);
            }
        }

        return var24;
    }

    static OwnerNativeSemanticAuthority.PutBinding quoteOwnerPublication(
        OwnerFieldExpectedAuthority.Prepared prepared, FoodWeaponExpectedTrace.Builder builder, HitPlan plan
    ) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldExpectedAuthority.class && frame.getMethodName().equals("quoteTimers"))
            .orElse(false));
        return caller && hitContext(plan) ? OwnerNativeSemanticAuthority.quotePassiveRoot(prepared, builder, plan,
            plan.context.player(), plan.context.actualWeapon(), plan.context.source(), plan.profile, plan.context.quotedAt(), plan.timers) : null;
    }

    static HitPlan ownerPublicationPlan(Object prepared, FoodWeaponConsumerSequence.Accepted accepted) {
        boolean caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponConsumerSequence.class && frame.getMethodName().equals("nativePublicationKey"))
            .orElse(false));
        return caller && prepared instanceof PreparedHit hit && hit.originalSequence == accepted
            && hit.publishAttempted && !hit.publishExited && !hit.commitConstructed && hit.publishingCleanup != null
            ? hit.plan : null;
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class)
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponBossPassive.class)
                            .findFirst()
                            .map(
                                var0x -> var0x.getDeclaringClass() == FoodWeaponFinalDamage.class
                                        && var0x.getMethodName().equals("finalizeFailedPublication")
                                        && var0x.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")
                            )
                            .orElse(false)
                );
    }

    private static void retireHitPublication(FoodWeaponBossPassive.PreparedHit var0, FoodWeaponSourceLedger.OriginalInteractionBinding var1) {
        FoodWeaponBossPassive.HitPublication var2 = var0.publication;
        if (var2 != null && !var2.cleanupStarted) {
            var2.cleanupStarted = true;

            try {
                if (!FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var0.binding.originalBinding, var1, var2.player)) {
                    var2.displaced = true;
                } else if (!var2.rootWriteEntered) {
                    var2.cleanupCompleted = true;
                } else {
                    for (int var3 = 0; var3 < var2.keys.size(); var3++) {
                        if (!ownsHitPublication(var0, var1)) {
                            var2.displaced = true;
                            return;
                        }

                        String var4 = var2.keys.get(var3);
                        Tag var5 = var2.previous.get(var4);
                        Tag var6 = var5 == null ? null : var5.copy();
                        if (!ownsHitPublication(var0, var1)) {
                            var2.displaced = true;
                            return;
                        }

                        var2.restoreProgress[var3] = 1;
                        if (var6 == null) {
                            var2.target.remove(var4);
                        } else {
                            var2.target.put(var4, var6);
                        }

                        var2.restoreProgress[var3] = 2;
                    }

                    var2.cleanupCompleted = ownsHitPublication(var0, var1);
                    var2.displaced = !var2.cleanupCompleted;
                }
            } catch (Error | RuntimeException var7) {
                var2.cleanupFailure = var7;
                throw var7;
            }
        }
    }

    public static boolean matchesPassiveHitBatch(
        FoodWeaponBossPassive.HitPlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        return hitContext(var0) && FoodWeaponBondHits.matchesBatch(var0.context, FoodWeaponSourceLedger.EffectDomain.PASSIVE_HIT, var1, var2, var3, var4);
    }

    private static boolean ownsHitTimerCleanup(FoodWeaponBossPassive.HitCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && var0.prepared.originalSequence != null) {
            return var0.executed
                && !var0.cleanupCompleted
                && var0.prepared.plan == var0.plan
                && var0.plan.committed
                && publishedPassiveHit(var0)
                && var0.prepared.originalSequence.continuing(var0, var1);
        } else if (var0 != null
            && var0.executed
            && !var0.cleanupCompleted
            && var0.prepared.plan == var0.plan
            && var0.plan.committed
            && publishedPassiveHit(var0)
            && FoodWeaponFinalDamage.matchesPassiveHitCleanupInvocation(var0, var1)) {
            FoodWeaponBossPassive.CompleteBatchBinding var2 = var0.prepared.binding;
            return var2.originalBinding != null
                && var2.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(var2.batch)
                && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(
                    var2.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(var2.batch), var0.plan.context.player()
                );
        } else {
            return false;
        }
    }

    private static void captureNativePassive202(BeforeMitigation event) {
        FoodWeaponBossPassive.HitPlan plan = null;
        if (!(event.getAmount() <= 0.0F)
            && event.getSource().getEntity() instanceof ServerPlayer player
            && player.isAlive()
            && !player.isSpectator()
            && event.getEntity() instanceof Monster target
            && !target.isAlliedTo(player)
            && (event.getSource().getDirectEntity() == player || event.getSource().getDirectEntity() instanceof AbstractArrow)) {
            String profile = attackProfile(player, event.getSource());
            if (profile.equals("benger") && target.getHealth() <= target.getMaxHealth() * 0.3F) {
                FoodWeaponFinalDamage.capturePassiveScalar(event, event.getAmount() * 1.1F);
                return;
            }
            if (profile.equals("sugar") || profile.equals("butcher") || profile.equals("saya") || profile.equals("wine") || profile.equals("defect")) {
                FoodWeaponBondHits.Context context = FoodWeaponBondHits.quoteHitContext(target, event.getSource());
                if (context == null || !contextProfile(context).equals(profile)) {
                    FoodWeaponFinalDamage.rejectNativeProducer(event);
                    return;
                }
                if (readyView(context, profile)) {
                    float amount = profile.equals("defect") ? event.getAmount() + 1.0F : event.getAmount();
                    plan = new FoodWeaponBossPassive.HitPlan(context, profile, event.getAmount(), amount,
                        new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.PASSIVE_HIT, 1.0));
                    if (!hitContext(plan)) {
                        FoodWeaponFinalDamage.rejectNativeProducer(event);
                        return;
                    }
                }
            }
        }
        FoodWeaponFinalDamage.capturePassiveHit(event, plan);
    }

    private static boolean hitContext(FoodWeaponBossPassive.HitPlan var0) {
        return var0 != null
            && !var0.committed
            && FoodWeaponBondHits.matchesContext(var0.context)
            && contextProfile(var0.context).equals(var0.profile)
            && readyView(var0.context, var0.profile)
            && Objects.equals(var0.timers, var0.context.player().getPersistentData().get("FTBossIntrinsicPassiveTimers"));
    }

    private static boolean readBackHitPublication(FoodWeaponBossPassive.HitPublication var0) {
        var0.rootReadbackMatched = var0.persistent.get("FTBossIntrinsicPassiveTimers") == var0.target;
        if (!var0.rootReadbackMatched) {
            return false;
        } else {
            boolean var1 = true;

            for (int var2 = 0; var2 < var0.keys.size(); var2++) {
                String var3 = var0.keys.get(var2);
                var0.readbackProgress[var2] = 1;
                Tag var4 = var0.target.get(var3);
                if (var4 != null) {
                    var0.observed.put(var3, var4.copy());
                }

                var0.readbackProgress[var2] = 2;
                var1 &= Objects.equals(var0.observed.get(var3), var0.expected.get(var3));
            }

            var0.rootReadbackCompleted = true;
            var0.ownPostimageMatched = var1 && var0.persistent.get("FTBossIntrinsicPassiveTimers") == var0.target;
            return var0.ownPostimageMatched;
        }
    }

    private static boolean ownsHitPublication(FoodWeaponBossPassive.PreparedHit var0, FoodWeaponSourceLedger.OriginalInteractionBinding var1) {
        FoodWeaponBossPassive.HitPublication var2 = var0.publication;
        FoodWeaponBossPassive.CompleteBatchBinding var3 = var0.binding;
        if (!hitCleanupPhase(var0)
            || var2 == null
            || !Thread.holdsLock(FoodWeaponSourceLedger.class)
            || Thread.currentThread() != var2.thread
            || var2.server != var2.player.serverLevel().getServer()
            || !var2.server.isSameThread()
            || var3.originalBinding != FoodWeaponSourceLedger.originalInteractionBinding(var3.batch)
            || !FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var3.originalBinding, var1, var2.player)) {
            return false;
        } else if (var2.player.getPersistentData() == var2.persistent && var2.persistent.get("FTBossIntrinsicPassiveTimers") == var2.target) {
            for (int var4 = 0; var4 < var2.keys.size(); var4++) {
                String var5 = var2.keys.get(var4);
                Tag var6 = var2.restoreProgress[var4] == 2 ? var2.previous.get(var5) : var2.expected.get(var5);
                if (!Objects.equals(var2.target.get(var5), var6)) {
                    return false;
                }
            }

            return FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var3.originalBinding, var1, var2.player) && hitCleanupPhase(var0);
        } else {
            return false;
        }
    }

    private static String contextProfile(FoodWeaponBondHits.Context var0) {
        if (var0 == null) {
            return "";
        } else {
            ItemStack var1 = var0.weapon();
            Kind var2 = FoodWeaponFamilyBuild.kind(var1);
            return !FoodWeaponEvolution.isNativeWeapon(var1)
                    && var2 != Kind.BOW
                    && var2 != Kind.STAFF
                    && !(var1.getItem() instanceof AxeItem)
                    && !FoodWeaponSourceLedger.isTrident(var1)
                ? ""
                : FoodWeaponEvolution.firstProfileId(var1);
        }
    }

    private static boolean readyView(FoodWeaponBondHits.Context var0, String var1) {
        Tag var2 = var0.player().getPersistentData().get("FTBossIntrinsicPassiveTimers");
        if (var2 != null && !(var2 instanceof CompoundTag)) {
            return false;
        } else {
            CompoundTag var3 = var0.player().getPersistentData().getCompound("FTBossIntrinsicPassiveTimers");
            if (var3.contains(var1) && !var3.contains(var1, 4)) {
                return false;
            } else {
                long var4 = var3.getLong(var1);
                return !var3.contains(var1) || var0.quotedAt() < var4 || var0.quotedAt() - var4 >= 60L;
            }
        }
    }

    private static boolean hitCleanupPhase(FoodWeaponBossPassive.PreparedHit var0) {
        return var0.originalSequence != null && var0.originalSequence.cleanup(var0, var0.publishingCleanup)
            ? true
            : var0.finalizingCleanup == null
                || var0.publishingCleanup == var0.finalizingCleanup && FoodWeaponFinalDamage.matchesPassiveHitPublicationCleanup(var0, var0.finalizingCleanup);
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(
            FoodWeaponSourceLedger.AttemptBatchQuote var1,
            FoodWeaponSourceLedger.AttemptBatchAdmission var2,
            List<FoodWeaponSourceLedger.AttemptRequest> var3,
            List<FoodWeaponSourceLedger.TargetRequest> var4
        ) {
            this.batch = var1;
            this.admission = var2;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(var1);
            this.requests = List.copyOf(var3);
            this.targets = List.copyOf(var4);
        }
    }

    public static final class HitCommit {
        private final FoodWeaponBossPassive.HitPlan plan;
        private final FoodWeaponBossPassive.PreparedHit prepared;
        private final CompoundTag publishedTimers;
        private final Tag publishedTimer;
        private final Tag previousTimer;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final Thread thread;
        private final MinecraftServer server;
        private boolean executed;
        private boolean cleanupCompleted;

        private HitCommit(FoodWeaponBossPassive.PreparedHit var1, FoodWeaponFinalDamage.OffenseReceipt var2, float var3, CompoundTag var4) {
            this.plan = var1.plan;
            this.prepared = var1;
            this.publishedTimers = var4;
            this.publishedTimer = var4.get(var1.plan.profile).copy();
            Tag var5 = var1.plan.timers instanceof CompoundTag var6 ? var6.get(var1.plan.profile) : null;
            this.previousTimer = var5 == null ? null : var5.copy();
            this.receipt = var2;
            this.amount = var3;
            this.thread = Thread.currentThread();
            this.server = var1.plan.context.player().serverLevel().getServer();
        }
    }

    public static final class HitPlan {
        private final FoodWeaponBondHits.Context context;
        private final String profile;
        private final Tag timers;
        private final float input;
        private final float amount;
        private final List<FoodWeaponSourceLedger.AttemptRequest> attempts;
        private boolean committed;

        private HitPlan(FoodWeaponBondHits.Context var1, String var2, float var3, float var4, FoodWeaponSourceLedger.AttemptRequest var5) {
            this.context = var1;
            this.profile = var2;
            this.input = var3;
            this.amount = var4;
            Tag var6 = var1.player().getPersistentData().get("FTBossIntrinsicPassiveTimers");
            this.timers = var6 == null ? null : var6.copy();
            this.attempts = List.of(var5);
        }

        public FoodWeaponBondHits.Context context() {
            return this.context;
        }

        public float input() {
            return this.input;
        }

        public float amount() {
            return this.amount;
        }

        public List<FoodWeaponSourceLedger.AttemptRequest> attempts() {
            return this.attempts;
        }

        public List<FoodWeaponSourceLedger.TargetRequest> targets() {
            return List.of();
        }
    }

    private static final class HitPublication {
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final CompoundTag readPersistent;
        private final Tag beforeFirstWriteRoot;
        private final CompoundTag beforeFirstWrite;
        private CompoundTag persistent;
        private final CompoundTag target;
        private final CompoundTag expected;
        private Tag previousRoot;
        private final CompoundTag previous;
        private final CompoundTag observed;
        private final List<String> keys;
        private final byte[] readbackProgress;
        private final byte[] restoreProgress;
        private final Thread thread;
        private final MinecraftServer server;
        private boolean persistentLookupEntered;
        private boolean persistentLookupCompleted;
        private boolean preimageReadEntered;
        private boolean preimageReadCompleted;
        private boolean rootReadbackMatched;
        private boolean rootReadbackCompleted;
        private boolean ownPostimageMatched;
        private boolean rootWriteEntered;
        private boolean rootWriteCompleted;
        private boolean cleanupStarted;
        private boolean cleanupCompleted;
        private boolean displaced;
        private Throwable cleanupFailure;

        private HitPublication(FoodWeaponBossPassive.PreparedHit var1, CompoundTag var2, CompoundTag var3) {
            this.player = var1.plan.context.player();
            this.weapon = var1.plan.context.actualWeapon();
            this.readPersistent = var2;
            this.beforeFirstWriteRoot = var2.get("FTBossIntrinsicPassiveTimers");
            this.beforeFirstWrite = new CompoundTag();
            this.target = var3;
            this.expected = new CompoundTag();
            this.previous = new CompoundTag();
            this.observed = new CompoundTag();
            this.keys = List.of(var1.plan.profile);
            this.readbackProgress = new byte[this.keys.size()];
            this.restoreProgress = new byte[this.keys.size()];

            for (String var5 : this.keys) {
                Tag var6 = var3.get(var5);
                if (var6 != null) {
                    this.expected.put(var5, var6.copy());
                }

                Tag var7 = this.beforeFirstWriteRoot instanceof CompoundTag var8 ? var8.get(var5) : null;
                if (var7 != null) {
                    this.beforeFirstWrite.put(var5, var7.copy());
                }
            }

            this.thread = Thread.currentThread();
            this.server = this.player.serverLevel().getServer();
        }
    }

    public static final class PreparedHit {
        private final FoodWeaponBossPassive.HitPlan plan;
        private final FoodWeaponBossPassive.CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private FoodWeaponBossPassive.HitPublication publication;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodWeaponFinalDamage.OffenseReceipt publicationReceipt;
        private int publicationAmountBits;
        private boolean publishAttempted;
        private boolean publishExited;
        private boolean commitConstructed;
        private Throwable publishFailure;

        private PreparedHit(FoodWeaponBossPassive.HitPlan var1, FoodWeaponBossPassive.CompleteBatchBinding var2) {
            this.plan = var1;
            this.binding = var2;
        }
    }
}
