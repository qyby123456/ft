/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Objects;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponConsumerSequence;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponBossPassive {
    private static final int PROC_COOLDOWN_TICKS = 60;
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final String TIMER_ROOT = "FTBossIntrinsicPassiveTimers";
    private static final String PROJECTILE_PROFILE = "FTBossIntrinsicProjectileProfile";

    private FoodWeaponBossPassive() {
    }

    public static String profile(ItemStack itemStack) {
        return FoodWeaponEvolution.isNativeWeapon(itemStack) ? FoodWeaponEvolution.firstProfileId(itemStack) : "";
    }

    public static FoodWeaponEvolution.WeaponDetailView detail(ItemStack itemStack) {
        String string = FoodWeaponBossPassive.profile(itemStack);
        return string.isEmpty() ? null : new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.skills.boss_passive"), (Component)Component.m_237115_((String)("passive.ftboss.boss_weapon." + string)), (Component)Component.m_237115_((String)("passive.ftboss.boss_weapon." + string + ".description")));
    }

    private static boolean ready(ServerPlayer serverPlayer, String string) {
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_(TIMER_ROOT);
        long l2 = compoundTag.m_128454_(string);
        if (compoundTag.m_128441_(string) && l >= l2 && l - l2 < 60L) {
            return false;
        }
        compoundTag.m_128356_(string, l);
        serverPlayer.getPersistentData().m_128365_(TIMER_ROOT, (Tag)compoundTag);
        return true;
    }

    @SubscribeEvent
    public static void projectile(EntityJoinLevelEvent entityJoinLevelEvent) {
        ServerPlayer serverPlayer;
        AbstractArrow abstractArrow;
        block3: {
            block2: {
                Entity entity;
                if (entityJoinLevelEvent.getLevel().f_46443_ || !((entity = entityJoinLevelEvent.getEntity()) instanceof AbstractArrow) || !((entity = (abstractArrow = (AbstractArrow)entity).m_19749_()) instanceof ServerPlayer)) break block2;
                serverPlayer = (ServerPlayer)entity;
                if (!abstractArrow.getPersistentData().m_128441_(PROJECTILE_PROFILE)) break block3;
            }
            return;
        }
        abstractArrow.getPersistentData().m_128359_(PROJECTILE_PROFILE, FoodWeaponBossPassive.profile(serverPlayer.m_21205_()));
    }

    private static String attackProfile(ServerPlayer serverPlayer, DamageSource damageSource) {
        AbstractArrow abstractArrow;
        if (damageSource.m_7640_() == serverPlayer) {
            return FoodWeaponBossPassive.profile(serverPlayer.m_21205_());
        }
        Entity entity = damageSource.m_7640_();
        if (entity instanceof AbstractArrow && (abstractArrow = (AbstractArrow)entity).m_19749_() == serverPlayer) {
            return abstractArrow.getPersistentData().m_128461_(PROJECTILE_PROFILE);
        }
        return "";
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void attack(LivingHurtEvent livingHurtEvent) {
        Monster monster;
        ServerPlayer serverPlayer;
        Object object;
        if (livingHurtEvent.getAmount() <= 0.0f || !((object = livingHurtEvent.getSource().m_7639_()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)object).m_6084_() || serverPlayer.m_5833_() || !((object = livingHurtEvent.getEntity()) instanceof Monster) || (monster = (Monster)object).m_7307_((Entity)serverPlayer) || livingHurtEvent.getSource().m_7640_() != serverPlayer && !(livingHurtEvent.getSource().m_7640_() instanceof AbstractArrow)) {
            return;
        }
        object = FoodWeaponBossPassive.attackProfile(serverPlayer, livingHurtEvent.getSource());
        if (((String)object).equals("benger") && monster.m_21223_() <= monster.m_21233_() * 0.3f) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 1.1f);
            return;
        }
        if (!(((String)object).equals("sugar") || ((String)object).equals("butcher") || ((String)object).equals("saya") || ((String)object).equals("wine") || ((String)object).equals("defect"))) {
            return;
        }
        if (!FoodWeaponBossPassive.ready(serverPlayer, (String)object)) {
            return;
        }
        switch (object) {
            case "sugar": {
                monster.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 40, 0), (Entity)serverPlayer);
                break;
            }
            case "butcher": {
                serverPlayer.m_5634_(1.0f);
                break;
            }
            case "saya": {
                monster.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 40, 0), (Entity)serverPlayer);
                break;
            }
            case "wine": {
                monster.m_147207_(new MobEffectInstance(MobEffects.f_19613_, 40, 0), (Entity)serverPlayer);
                break;
            }
            case "defect": {
                livingHurtEvent.setAmount(livingHurtEvent.getAmount() + 1.0f);
                break;
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void defend(LivingHurtEvent livingHurtEvent) {
        ServerPlayer serverPlayer;
        Object object;
        if (livingHurtEvent.getAmount() <= 0.0f || !((object = livingHurtEvent.getEntity()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)object).m_6084_() || serverPlayer.m_5833_()) {
            return;
        }
        object = FoodWeaponBossPassive.profile(serverPlayer.m_21205_());
        if (((String)object).equals("compost")) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 0.92f);
        }
        if (((String)object).equals("tide") && livingHurtEvent.getSource().m_7640_() instanceof AbstractArrow) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 0.9f);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void kill(LivingDeathEvent livingDeathEvent) {
        ServerPlayer serverPlayer;
        Object object;
        block7: {
            block6: {
                object = livingDeathEvent.getEntity();
                if (!(object instanceof Monster)) break block6;
                Monster monster = (Monster)object;
                object = livingDeathEvent.getSource().m_7639_();
                if (object instanceof ServerPlayer && (serverPlayer = (ServerPlayer)object).m_6084_() && !serverPlayer.m_5833_() && !monster.m_7307_((Entity)serverPlayer) && !LemonSummonRewards.suppressed((Entity)monster)) break block7;
            }
            return;
        }
        object = FoodWeaponBossPassive.attackProfile(serverPlayer, livingDeathEvent.getSource());
        if (!((String)object).equals("rat") && !((String)object).equals("apple") || !FoodWeaponBossPassive.ready(serverPlayer, (String)object)) {
            return;
        }
        if (((String)object).equals("rat")) {
            serverPlayer.m_36324_().m_38707_(1, 0.0f);
        } else {
            serverPlayer.m_5634_(1.0f);
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        FoodWeaponEvolution.WeaponDetailView weaponDetailView = FoodWeaponBossPassive.detail(itemTooltipEvent.getItemStack());
        if (weaponDetailView == null) {
            return;
        }
        itemTooltipEvent.getToolTip().add(weaponDetailView.category().m_6881_().m_7220_((Component)Component.m_237113_((String)": ")).m_7220_(weaponDetailView.title()).m_130940_(ChatFormatting.GOLD));
        itemTooltipEvent.getToolTip().add(weaponDetailView.description().m_6881_().m_130940_(ChatFormatting.GRAY));
    }

    public static void applyPassiveHit(HitCommit hitCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (hitCommit == null || hitCommit.executed || Thread.currentThread() != hitCommit.thread || hitCommit.server != hitCommit.plan.context.player().m_284548_().m_7654_() || !hitCommit.server.m_18695_()) {
            return;
        }
        FoodWeaponConsumerSequence.Accepted accepted = hitCommit.prepared.originalSequence;
        if (accepted != null && !accepted.enter(hitCommit, childHurtBridge)) {
            return;
        }
        Throwable throwable = null;
        try {
            hitCommit.executed = true;
            if (!FoodWeaponBossPassive.publishedPassiveHit(hitCommit)) {
                return;
            }
            if (!(accepted == null ? FoodWeaponBondHits.matchesContext(hitCommit.plan.context) : accepted.ordinaryContext()) || !FoodWeaponBossPassive.matchesPassiveHitReceipt(hitCommit.prepared, hitCommit.amount, hitCommit.receipt)) {
                FoodWeaponBossPassive.restoreHitTimer(hitCommit, childHurtBridge);
                return;
            }
            if (!FoodWeaponBossPassive.passiveHitLeafLive(hitCommit, childHurtBridge)) {
                return;
            }
            FoodWeaponBossPassive.applyHit(hitCommit, childHurtBridge);
            if (!FoodWeaponBossPassive.passiveHitLeafLive(hitCommit, childHurtBridge)) {
                return;
            }
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.finish(hitCommit, childHurtBridge, throwable);
            }
        }
    }

    static boolean bindOriginalSequence(PreparedHit preparedHit, FoodWeaponOriginalSequence.Session session, Object object, int n, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        if (preparedHit == null || preparedHit.publishAttempted || preparedHit.originalSequence != null || !FoodWeaponFinalDamage.matchesPassiveHitPublishing(preparedHit, offenseReceipt, publishingCleanup) || !FoodWeaponBossPassive.matchesPassiveHitReceipt(preparedHit, f, offenseReceipt)) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedHit.binding;
        FoodWeaponConsumerSequence.Accepted accepted = FoodWeaponConsumerSequence.bind(session, object, n, preparedHit, preparedHit.plan, offenseReceipt, publishingCleanup, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets, preparedHit.plan.context.player(), preparedHit.plan.context.target(), preparedHit.plan.context.source(), preparedHit.plan.context.actualWeapon(), preparedHit.plan.context.originalHand(), preparedHit.plan.context.quotedAt(), f, "PassiveProducer");
        if (accepted == null) {
            return false;
        }
        preparedHit.originalSequence = accepted;
        return true;
    }

    static HitCommit commitPassiveHit(PreparedHit preparedHit, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedHit == null ? null : preparedHit.originalSequence;
        int n = accepted == null ? 0 : accepted.publicationCallEntered(preparedHit);
        Throwable throwable = null;
        try {
            if (preparedHit == null || preparedHit.publishAttempted || preparedHit.publishingCleanup != null || !FoodWeaponFinalDamage.matchesPassiveHitPublishing(preparedHit, offenseReceipt, publishingCleanup)) {
                HitCommit hitCommit = null;
                return hitCommit;
            }
            preparedHit.publishingCleanup = publishingCleanup;
            HitCommit hitCommit = FoodWeaponBossPassive.commitPassiveHit(preparedHit, f, offenseReceipt);
            return hitCommit;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.publicationExited(preparedHit, n, throwable);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void finishPassiveHitPublication(PreparedHit preparedHit, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedHit == null ? null : preparedHit.originalSequence;
        Throwable throwable = null;
        try {
            if (!(FoodWeaponBossPassive.preparedCleanupCaller() && preparedHit != null && preparedHit.publishingCleanup == publishingCleanup && (preparedHit.originalSequence != null && preparedHit.originalSequence.cleanup(preparedHit, publishingCleanup) || FoodWeaponFinalDamage.matchesPassiveHitPublicationCleanup(preparedHit, publishingCleanup)) && preparedHit.publishAttempted && preparedHit.publishExited && preparedHit.publicationReceipt != null)) {
                return;
            }
            CompleteBatchBinding completeBatchBinding = preparedHit.binding;
            if (!FoodWeaponBossPassive.matchesPreparedPassiveHit(preparedHit, preparedHit.plan, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets)) {
                return;
            }
            preparedHit.finalizingCleanup = publishingCleanup;
            try {
                FoodWeaponBossPassive.retireHitPublication(preparedHit, FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch));
            }
            finally {
                if (preparedHit.originalSequence != null) {
                    preparedHit.originalSequence.retired(preparedHit);
                }
            }
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.publicationCleanupExited(preparedHit, publishingCleanup, throwable);
            }
        }
    }

    static boolean matchesPassiveHitCommit(HitCommit hitCommit, PreparedHit preparedHit, HitPlan hitPlan, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return hitCommit != null && preparedHit != null && hitCommit.prepared == preparedHit && hitCommit.plan == hitPlan && hitPlan != null && hitPlan.committed && offenseReceipt != null && hitCommit.receipt == offenseReceipt && Float.isFinite(f) && f > 0.0f && Float.floatToRawIntBits(hitCommit.amount) == Float.floatToRawIntBits(f) && FoodWeaponBossPassive.matchesPreparedPassiveHit(preparedHit, hitPlan, attemptBatchQuote, attemptBatchAdmission, list, list2);
    }

    static boolean matchesPreparedPassiveHit(PreparedHit preparedHit, HitPlan hitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return preparedHit != null && hitPlan != null && preparedHit.plan == hitPlan && attemptBatchQuote != null && attemptBatchAdmission != null && list != null && list2 != null && preparedHit.binding.batch == attemptBatchQuote && preparedHit.binding.admission == attemptBatchAdmission && preparedHit.binding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote) && preparedHit.binding.originalBinding != null && preparedHit.binding.requests.equals(list) && preparedHit.binding.targets.equals(list2);
    }

    public static PreparedHit preparePassiveHit(HitPlan hitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        if (!FoodWeaponBossPassive.matchesPassiveHitBatch(hitPlan, attemptBatchQuote, attemptBatchAdmission, list, list2)) {
            return null;
        }
        CompleteBatchBinding completeBatchBinding = new CompleteBatchBinding(attemptBatchQuote, attemptBatchAdmission, list, list2);
        return completeBatchBinding.originalBinding == null ? null : new PreparedHit(hitPlan, completeBatchBinding);
    }

    private static boolean publishedPassiveHit(HitCommit hitCommit) {
        HitPlan hitPlan = hitCommit.plan;
        return hitPlan.context.player().getPersistentData().m_128423_(TIMER_ROOT) == hitCommit.publishedTimers && hitCommit.publishedTimers.m_128425_(hitPlan.profile, 4) && hitCommit.publishedTimers.m_128454_(hitPlan.profile) == hitPlan.context.quotedAt() && Objects.equals(hitCommit.publishedTimers.m_128423_(hitPlan.profile), hitCommit.publishedTimer);
    }

    private static boolean matchesPassiveHitReceipt(PreparedHit preparedHit, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (preparedHit != null && preparedHit.originalSequence != null) {
            return preparedHit.originalSequence.matchesReceipt(preparedHit, offenseReceipt, f);
        }
        if (preparedHit == null || offenseReceipt == null || !Float.isFinite(f) || f <= 0.0f) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedHit.binding;
        FoodWeaponBondHits.Context context = preparedHit.plan.context;
        return offenseReceipt.matchesBatch(completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets) && offenseReceipt.matches(preparedHit.plan, context.player(), context.source(), context.quotedAt(), f) && offenseReceipt.attempt(FoodWeaponSourceLedger.AttemptDomain.PASSIVE_HIT).filter(attemptResult -> attemptResult.domain() == FoodWeaponSourceLedger.AttemptDomain.PASSIVE_HIT && attemptResult.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS).isPresent();
    }

    private static void restoreHitTimer(HitCommit hitCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (!FoodWeaponBossPassive.ownsHitTimerCleanup(hitCommit, childHurtBridge)) {
            return;
        }
        hitCommit.cleanupCompleted = true;
        FoodWeaponBossPassive.retireHitPublication(hitCommit.prepared, FoodWeaponSourceLedger.originalInteractionBinding(hitCommit.prepared.binding.batch));
    }

    private static boolean passiveHitLeafLive(HitCommit hitCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (hitCommit != null && hitCommit.prepared.originalSequence != null) {
            return hitCommit.executed && FoodWeaponBossPassive.publishedPassiveHit(hitCommit) && hitCommit.prepared.originalSequence.continuing(hitCommit, childHurtBridge);
        }
        return hitCommit != null && hitCommit.executed && FoodWeaponBossPassive.publishedPassiveHit(hitCommit) && FoodWeaponBossPassive.matchesPassiveHitReceipt(hitCommit.prepared, hitCommit.amount, hitCommit.receipt) && FoodWeaponFinalDamage.matchesPassiveHitInvocation(hitCommit, childHurtBridge);
    }

    private static void applyHit(HitCommit hitCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        HitPlan hitPlan = hitCommit.plan;
        ServerPlayer serverPlayer = hitPlan.context.player();
        LivingEntity livingEntity = hitPlan.context.target();
        if (!FoodWeaponBossPassive.passiveHitLeafLive(hitCommit, childHurtBridge)) {
            return;
        }
        switch (hitPlan.profile) {
            case "sugar": {
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 40, 0), (Entity)serverPlayer);
                break;
            }
            case "butcher": {
                serverPlayer.m_5634_(1.0f);
                break;
            }
            case "saya": {
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 40, 0), (Entity)serverPlayer);
                break;
            }
            case "wine": {
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19613_, 40, 0), (Entity)serverPlayer);
                break;
            }
        }
        if (!FoodWeaponBossPassive.passiveHitLeafLive(hitCommit, childHurtBridge)) {
            return;
        }
    }

    public static HitCommit commitPassiveHit(PreparedHit preparedHit, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedHit == null ? null : preparedHit.originalSequence;
        boolean bl = false;
        int n = accepted == null ? 0 : accepted.publicationCallEntered(preparedHit);
        Throwable throwable = null;
        try {
            Object object;
            CompoundTag compoundTag;
            HitPublication hitPublication;
            if (preparedHit == null || preparedHit.publishAttempted || preparedHit.publishingCleanup != null && !FoodWeaponFinalDamage.matchesPassiveHitPublishing(preparedHit, offenseReceipt, preparedHit.publishingCleanup)) {
                HitCommit hitCommit = null;
                return hitCommit;
            }
            HitPlan hitPlan = preparedHit.plan;
            if (!FoodWeaponBossPassive.hitContext(hitPlan) || !FoodWeaponBossPassive.matchesPassiveHitReceipt(preparedHit, f, offenseReceipt)) {
                HitCommit hitCommit = null;
                return hitCommit;
            }
            preparedHit.publishAttempted = true;
            preparedHit.publicationReceipt = offenseReceipt;
            preparedHit.publicationAmountBits = Float.floatToRawIntBits(f);
            bl = true;
            CompoundTag compoundTag2 = hitPlan.context.player().getPersistentData();
            CompoundTag compoundTag3 = compoundTag2.m_128469_(TIMER_ROOT).m_6426_();
            compoundTag3.m_128356_(hitPlan.profile, hitPlan.context.quotedAt());
            hitPlan.committed = true;
            preparedHit.publication = hitPublication = new HitPublication(preparedHit, compoundTag2, compoundTag3);
            hitPublication.persistentLookupEntered = true;
            hitPublication.persistent = compoundTag = hitPlan.context.player().getPersistentData();
            hitPublication.persistentLookupCompleted = true;
            hitPublication.preimageReadEntered = true;
            hitPublication.previousRoot = compoundTag.m_128423_(TIMER_ROOT);
            for (String object22 : hitPublication.keys) {
                Tag tag;
                Tag tag2;
                Tag tag3 = hitPublication.previousRoot;
                if (tag3 instanceof CompoundTag) {
                    CompoundTag compoundTag4 = (CompoundTag)tag3;
                    tag2 = compoundTag4.m_128423_(object22);
                } else {
                    tag2 = null;
                }
                if ((tag = tag2) == null) continue;
                hitPublication.previous.m_128365_(object22, tag.m_6426_());
            }
            hitPublication.preimageReadCompleted = true;
            hitPublication.rootWriteEntered = true;
            compoundTag.m_128365_(TIMER_ROOT, (Tag)compoundTag3);
            hitPublication.rootWriteCompleted = true;
            if (!FoodWeaponBossPassive.readBackHitPublication(hitPublication)) {
                object = null;
                return object;
            }
            object = new HitCommit(preparedHit, offenseReceipt, f, compoundTag3);
            preparedHit.commitConstructed = true;
            if (preparedHit.originalSequence != null) {
                preparedHit.originalSequence.published(preparedHit, object);
            }
            Object object2 = object;
            return object2;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            if (bl && (throwable2 instanceof RuntimeException || throwable2 instanceof Error)) {
                preparedHit.publishFailure = throwable2;
                if (preparedHit.originalSequence != null) {
                    preparedHit.originalSequence.originalThrown(throwable2);
                }
            }
            throw throwable2;
        }
        finally {
            if (bl) {
                preparedHit.publishExited = true;
            }
            if (accepted != null) {
                accepted.publicationExited(preparedHit, n, throwable);
            }
        }
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponBossPassive.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("finalizeFailedPublication") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")).orElse(false)) != false;
    }

    private static void retireHitPublication(PreparedHit preparedHit, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
        HitPublication hitPublication = preparedHit.publication;
        if (hitPublication == null || hitPublication.cleanupStarted) {
            return;
        }
        hitPublication.cleanupStarted = true;
        try {
            if (!FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(preparedHit.binding.originalBinding, originalInteractionBinding, hitPublication.player)) {
                hitPublication.displaced = true;
                return;
            }
            if (!hitPublication.rootWriteEntered) {
                hitPublication.cleanupCompleted = true;
                return;
            }
            for (int i = 0; i < hitPublication.keys.size(); ++i) {
                Tag tag;
                if (!FoodWeaponBossPassive.ownsHitPublication(preparedHit, originalInteractionBinding)) {
                    hitPublication.displaced = true;
                    return;
                }
                String string = hitPublication.keys.get(i);
                Tag tag2 = hitPublication.previous.m_128423_(string);
                Tag tag3 = tag = tag2 == null ? null : tag2.m_6426_();
                if (!FoodWeaponBossPassive.ownsHitPublication(preparedHit, originalInteractionBinding)) {
                    hitPublication.displaced = true;
                    return;
                }
                hitPublication.restoreProgress[i] = 1;
                if (tag == null) {
                    hitPublication.target.m_128473_(string);
                } else {
                    hitPublication.target.m_128365_(string, tag);
                }
                hitPublication.restoreProgress[i] = 2;
            }
            hitPublication.cleanupCompleted = FoodWeaponBossPassive.ownsHitPublication(preparedHit, originalInteractionBinding);
            hitPublication.displaced = !hitPublication.cleanupCompleted;
        }
        catch (Error | RuntimeException throwable) {
            hitPublication.cleanupFailure = throwable;
            throw throwable;
        }
    }

    public static boolean matchesPassiveHitBatch(HitPlan hitPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return FoodWeaponBossPassive.hitContext(hitPlan) && FoodWeaponBondHits.matchesBatch(hitPlan.context, FoodWeaponSourceLedger.EffectDomain.PASSIVE_HIT, attemptBatchQuote, attemptBatchAdmission, list, list2);
    }

    private static boolean ownsHitTimerCleanup(HitCommit hitCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (hitCommit != null && hitCommit.prepared.originalSequence != null) {
            return hitCommit.executed && !hitCommit.cleanupCompleted && hitCommit.prepared.plan == hitCommit.plan && hitCommit.plan.committed && FoodWeaponBossPassive.publishedPassiveHit(hitCommit) && hitCommit.prepared.originalSequence.continuing(hitCommit, childHurtBridge);
        }
        if (!(hitCommit != null && hitCommit.executed && !hitCommit.cleanupCompleted && hitCommit.prepared.plan == hitCommit.plan && hitCommit.plan.committed && FoodWeaponBossPassive.publishedPassiveHit(hitCommit) && FoodWeaponFinalDamage.matchesPassiveHitCleanupInvocation(hitCommit, childHurtBridge))) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = hitCommit.prepared.binding;
        return completeBatchBinding.originalBinding != null && completeBatchBinding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch), hitCommit.plan.context.player());
    }

    private static boolean hitContext(HitPlan hitPlan) {
        return hitPlan != null && !hitPlan.committed && FoodWeaponBondHits.matchesContext(hitPlan.context) && FoodWeaponBossPassive.contextProfile(hitPlan.context).equals(hitPlan.profile) && FoodWeaponBossPassive.readyView(hitPlan.context, hitPlan.profile) && Objects.equals(hitPlan.timers, hitPlan.context.player().getPersistentData().m_128423_(TIMER_ROOT));
    }

    private static boolean readBackHitPublication(HitPublication hitPublication) {
        boolean bl = hitPublication.rootReadbackMatched = hitPublication.persistent.m_128423_(TIMER_ROOT) == hitPublication.target;
        if (!hitPublication.rootReadbackMatched) {
            return false;
        }
        boolean bl2 = true;
        for (int i = 0; i < hitPublication.keys.size(); ++i) {
            String string = hitPublication.keys.get(i);
            hitPublication.readbackProgress[i] = 1;
            Tag tag = hitPublication.target.m_128423_(string);
            if (tag != null) {
                hitPublication.observed.m_128365_(string, tag.m_6426_());
            }
            hitPublication.readbackProgress[i] = 2;
            bl2 &= Objects.equals(hitPublication.observed.m_128423_(string), hitPublication.expected.m_128423_(string));
        }
        hitPublication.rootReadbackCompleted = true;
        hitPublication.ownPostimageMatched = bl2 && hitPublication.persistent.m_128423_(TIMER_ROOT) == hitPublication.target;
        return hitPublication.ownPostimageMatched;
    }

    private static boolean ownsHitPublication(PreparedHit preparedHit, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
        HitPublication hitPublication = preparedHit.publication;
        CompleteBatchBinding completeBatchBinding = preparedHit.binding;
        if (!(FoodWeaponBossPassive.hitCleanupPhase(preparedHit) && hitPublication != null && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == hitPublication.thread && hitPublication.server == hitPublication.player.m_284548_().m_7654_() && hitPublication.server.m_18695_() && completeBatchBinding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, originalInteractionBinding, hitPublication.player))) {
            return false;
        }
        if (hitPublication.player.getPersistentData() != hitPublication.persistent || hitPublication.persistent.m_128423_(TIMER_ROOT) != hitPublication.target) {
            return false;
        }
        for (int i = 0; i < hitPublication.keys.size(); ++i) {
            Tag tag;
            String string = hitPublication.keys.get(i);
            Tag tag2 = tag = hitPublication.restoreProgress[i] == 2 ? hitPublication.previous.m_128423_(string) : hitPublication.expected.m_128423_(string);
            if (Objects.equals(hitPublication.target.m_128423_(string), tag)) continue;
            return false;
        }
        return FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, originalInteractionBinding, hitPublication.player) && FoodWeaponBossPassive.hitCleanupPhase(preparedHit);
    }

    private static String contextProfile(FoodWeaponBondHits.Context context) {
        if (context == null) {
            return "";
        }
        ItemStack itemStack = context.weapon();
        FoodWeaponFamilyRules.Kind kind = FoodWeaponFamilyBuild.kind(itemStack);
        if (!(FoodWeaponEvolution.isNativeWeapon(itemStack) || kind == FoodWeaponFamilyRules.Kind.BOW || kind == FoodWeaponFamilyRules.Kind.STAFF || itemStack.m_41720_() instanceof AxeItem || FoodWeaponSourceLedger.isTrident(itemStack))) {
            return "";
        }
        return FoodWeaponEvolution.firstProfileId(itemStack);
    }

    private static boolean readyView(FoodWeaponBondHits.Context context, String string) {
        Tag tag = context.player().getPersistentData().m_128423_(TIMER_ROOT);
        if (tag != null && !(tag instanceof CompoundTag)) {
            return false;
        }
        CompoundTag compoundTag = context.player().getPersistentData().m_128469_(TIMER_ROOT);
        if (compoundTag.m_128441_(string) && !compoundTag.m_128425_(string, 4)) {
            return false;
        }
        long l = compoundTag.m_128454_(string);
        return !compoundTag.m_128441_(string) || context.quotedAt() < l || context.quotedAt() - l >= 60L;
    }

    private static boolean hitCleanupPhase(PreparedHit preparedHit) {
        if (preparedHit.originalSequence != null && preparedHit.originalSequence.cleanup(preparedHit, preparedHit.publishingCleanup)) {
            return true;
        }
        return preparedHit.finalizingCleanup == null || preparedHit.publishingCleanup == preparedHit.finalizingCleanup && FoodWeaponFinalDamage.matchesPassiveHitPublicationCleanup(preparedHit, preparedHit.finalizingCleanup);
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

        private HitPublication(PreparedHit preparedHit, CompoundTag compoundTag, CompoundTag compoundTag2) {
            this.player = preparedHit.plan.context.player();
            this.weapon = preparedHit.plan.context.actualWeapon();
            this.readPersistent = compoundTag;
            this.beforeFirstWriteRoot = compoundTag.m_128423_(FoodWeaponBossPassive.TIMER_ROOT);
            this.beforeFirstWrite = new CompoundTag();
            this.target = compoundTag2;
            this.expected = new CompoundTag();
            this.previous = new CompoundTag();
            this.observed = new CompoundTag();
            this.keys = List.of(preparedHit.plan.profile);
            this.readbackProgress = new byte[this.keys.size()];
            this.restoreProgress = new byte[this.keys.size()];
            for (String string : this.keys) {
                Tag tag;
                Tag tag2;
                Tag tag3;
                Tag tag4 = compoundTag2.m_128423_(string);
                if (tag4 != null) {
                    this.expected.m_128365_(string, tag4.m_6426_());
                }
                if ((tag3 = this.beforeFirstWriteRoot) instanceof CompoundTag) {
                    CompoundTag compoundTag3 = (CompoundTag)tag3;
                    tag2 = compoundTag3.m_128423_(string);
                } else {
                    tag2 = null;
                }
                if ((tag = tag2) == null) continue;
                this.beforeFirstWrite.m_128365_(string, tag.m_6426_());
            }
            this.thread = Thread.currentThread();
            this.server = this.player.m_284548_().m_7654_();
        }
    }

    public static final class PreparedHit {
        private final HitPlan plan;
        private final CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private HitPublication publication;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodWeaponFinalDamage.OffenseReceipt publicationReceipt;
        private int publicationAmountBits;
        private boolean publishAttempted;
        private boolean publishExited;
        private boolean commitConstructed;
        private Throwable publishFailure;

        private PreparedHit(HitPlan hitPlan, CompleteBatchBinding completeBatchBinding) {
            this.plan = hitPlan;
            this.binding = completeBatchBinding;
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

        private HitPlan(FoodWeaponBondHits.Context context, String string, float f, float f2, FoodWeaponSourceLedger.AttemptRequest attemptRequest) {
            this.context = context;
            this.profile = string;
            this.input = f;
            this.amount = f2;
            Tag tag = context.player().getPersistentData().m_128423_(FoodWeaponBossPassive.TIMER_ROOT);
            this.timers = tag == null ? null : tag.m_6426_();
            this.attempts = List.of(attemptRequest);
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

    public static final class HitCommit {
        private final HitPlan plan;
        private final PreparedHit prepared;
        private final CompoundTag publishedTimers;
        private final Tag publishedTimer;
        private final Tag previousTimer;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final Thread thread;
        private final MinecraftServer server;
        private boolean executed;
        private boolean cleanupCompleted;

        private HitCommit(PreparedHit preparedHit, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, CompoundTag compoundTag) {
            Tag tag;
            this.plan = preparedHit.plan;
            this.prepared = preparedHit;
            this.publishedTimers = compoundTag;
            this.publishedTimer = compoundTag.m_128423_(preparedHit.plan.profile).m_6426_();
            Tag tag2 = preparedHit.plan.timers;
            if (tag2 instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag)tag2;
                tag = compoundTag2.m_128423_(preparedHit.plan.profile);
            } else {
                tag = null;
            }
            Tag tag3 = tag;
            this.previousTimer = tag3 == null ? null : tag3.m_6426_();
            this.receipt = offenseReceipt;
            this.amount = f;
            this.thread = Thread.currentThread();
            this.server = preparedHit.plan.context.player().m_284548_().m_7654_();
        }
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
            this.batch = attemptBatchQuote;
            this.admission = attemptBatchAdmission;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
            this.requests = List.copyOf(list);
            this.targets = List.copyOf(list2);
        }
    }
}

