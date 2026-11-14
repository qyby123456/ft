/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.balance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.balance.ManorBalanceRules;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.balance.ManorShadowBlink;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorImpossibleRules;
import net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.ManorBalanceAccess;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorBalanceHooks {
    private static final UUID SPEED_ID = UUID.fromString("ea2b6714-7949-4df1-a9d1-8b4e36f52416");
    private static final Map<EasterBossEntity, Long> ZICAI_TICKS = new WeakHashMap<EasterBossEntity, Long>();
    private static final Set<ResourceLocation> BROKEN_FOOD = new HashSet<ResourceLocation>();
    private static boolean emptyFoodLogged;

    private ManorBalanceHooks() {
    }

    public static boolean mainBoss(EasterBossEntity boss) {
        return !boss.isMini() && !boss.isGiant() && boss.getVariant() >= 0 && boss.getVariant() <= 11;
    }

    public static List<ServerPlayer> players(EasterBossEntity boss, ServerLevel level) {
        ArrayList<ServerPlayer> result = new ArrayList<ServerPlayer>();
        for (ServerPlayer player : level.m_6907_()) {
            if (!ManorBalanceHooks.validPlayer(boss, player) || !ManorBalanceHooks.inArena(boss, player)) continue;
            result.add(player);
        }
        return result;
    }

    private static boolean inArena(EasterBossEntity boss, ServerPlayer player) {
        Vec3 anchor;
        UUID fight = ManorFight.fightId((Entity)boss);
        UUID playerFight = ManorFightMembers.fightId((Entity)player);
        if (fight != null && playerFight != null && !fight.equals(playerFight)) {
            return false;
        }
        Vec3 vec3 = anchor = fight != null && ManorFight.hasActive(fight) ? ManorFight.anchorOf(fight) : null;
        return anchor != null ? player.m_20238_(anchor) <= 9216.0 : boss.m_20191_().m_82400_(48.0).m_82390_(player.m_20182_());
    }

    private static boolean hitPlayer(EasterBossEntity boss, ServerPlayer player) {
        return !(player instanceof FakePlayer) && !player.m_213877_() && !player.m_7500_() && !player.m_5833_() && player.m_9236_() == boss.m_9236_() && !boss.m_7307_((Entity)player) && !player.m_7307_((Entity)boss);
    }

    public static boolean validPlayer(EasterBossEntity boss, ServerPlayer player) {
        return player.m_6084_() && ManorBalanceHooks.hitPlayer(boss, player);
    }

    public static int tridentInterval(int original, EasterBossEntity boss, String channel) {
        Level level;
        if (boss.getVariant() != 5 || !ManorBalanceHooks.mainBoss(boss) || !((level = boss.m_9236_()) instanceof ServerLevel)) {
            return original;
        }
        ServerLevel level2 = (ServerLevel)level;
        return ManorBalanceRules.tridentInterval(ManorBalanceHooks.players(boss, level2).size());
    }

    public static int castTicks(int ticks, LivingEntity caster) {
        EasterBossEntity boss;
        return caster instanceof EasterBossEntity && (boss = (EasterBossEntity)caster).getVariant() == 1 && ManorBalanceHooks.mainBoss(boss) ? ManorBalanceRules.fasterCast(ticks) : ticks;
    }

    public static boolean crossProtected(boolean original, EasterBossEntity boss) {
        return original || boss.getVariant() == 11 && ManorBalanceHooks.mainBoss(boss) && boss.m_6084_() && !boss.m_213877_() && !boss.m_20147_() && CrossGuard.holding(boss);
    }

    public static int fiveAge(int age, ManorFiveCombatRules.Skill skill) {
        return skill.variant == 1 ? skill.windup - ManorBalanceRules.fasterCast(skill.windup - age) : age;
    }

    public static void zicai(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        ManorBalanceAccess access = boss;
        if (boss.getVariant() != 8 || !ManorBalanceHooks.mainBoss(boss) || !(target instanceof ServerPlayer)) {
            access.ftboss$applyPufferSingle(level, target);
            return;
        }
        ServerPlayer player = (ServerPlayer)target;
        if (!ManorBalanceHooks.hitPlayer(boss, player)) {
            return;
        }
        List<ServerPlayer> participants = ManorBalanceHooks.players(boss, level);
        boolean dyingMember = !player.m_6084_() && ManorBalanceHooks.inArena(boss, player);
        if (participants.size() + (dyingMember ? 1 : 0) < 2 || !participants.contains(player) && !dyingMember) {
            if (player.m_6084_()) {
                access.ftboss$applyPufferSingle(level, target);
            }
            return;
        }
        long now = level.m_46467_();
        Long previous = ZICAI_TICKS.put(boss, now);
        if (previous != null && previous == now) {
            return;
        }
        for (ServerPlayer participant : participants) {
            access.ftboss$applyPufferSingle(level, (LivingEntity)participant);
        }
    }

    public static void zicaiAttack(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        List<ServerPlayer> participants;
        ServerPlayer player;
        if (boss.m_5448_() == target) {
            ManorBalanceHooks.zicai(boss, level, target);
        } else if (boss.getVariant() == 8 && ManorBalanceHooks.mainBoss(boss) && target instanceof ServerPlayer && ManorBalanceHooks.hitPlayer(boss, player = (ServerPlayer)target) && ManorBalanceHooks.inArena(boss, player) && (participants = ManorBalanceHooks.players(boss, level)).size() + (player.m_6084_() ? 0 : 1) >= 2) {
            ManorBalanceHooks.zicai(boss, level, target);
        }
    }

    public static void zicaiMelee(EasterBossEntity boss, LivingEntity target) {
        Level level;
        if (boss != null && target instanceof ServerPlayer && boss.getVariant() == 8 && (level = boss.m_9236_()) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            ManorBalanceHooks.zicaiAttack(boss, level2, target);
        }
    }

    private static boolean forbidden(EasterBossEntity boss, MobEffect effect) {
        ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect);
        Set<String> banned = boss.isImpossibleChallenge() ? ManorImpossibleRules.BANNED_FOOD_BUFFS : ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS;
        return LemonImmuneBuffRules.isForbidden(effect) || key != null && banned.contains(key.toString());
    }

    public static void food(EasterBossEntity boss) {
        Level level;
        if (!(boss.getVariant() == 4 && ManorBalanceHooks.mainBoss(boss) && boss.m_6084_() && (level = boss.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ArrayList<MobEffect> pool = new ArrayList<MobEffect>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect);
            if (key == null || !ManorBalanceRules.foodMod(key.m_135827_()) || !effect.m_19486_() || ManorBalanceHooks.forbidden(boss, effect) || BROKEN_FOOD.contains(key)) continue;
            pool.add(effect);
        }
        if (pool.isEmpty()) {
            if (!emptyFoodLogged) {
                FTBossMod.LOGGER.warn("[FTBoss][Balance] \u67e0\u6aac\u6ca1\u6709\u53ef\u7528\u4e14\u672a\u88ab\u7981\u7528\u7684\u98df\u7269\u6a21\u7ec4\u589e\u76ca\uff0c\u8df3\u8fc7\u672c\u6b21\u65bd\u52a0");
            }
            emptyFoodLogged = true;
            return;
        }
        MobEffect effect = (MobEffect)pool.get(boss.m_217043_().m_188503_(pool.size()));
        try {
            MobEffectInstance previous = boss.m_21124_(effect);
            int amplifier = ManorExpansionRules.nextAmplifier(previous == null ? -1 : previous.m_19564_());
            if (effect == MobEffects.f_19606_ && amplifier >= 3) {
                return;
            }
            if (previous != null) {
                boss.m_21195_(effect);
            }
            if (!boss.m_7292_(new MobEffectInstance(effect, 300, amplifier, false, true, false))) {
                return;
            }
        }
        catch (Throwable failure) {
            BROKEN_FOOD.add(ForgeRegistries.MOB_EFFECTS.getKey((Object)effect));
            try {
                boss.m_21195_(effect);
            }
            catch (Throwable cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            FTBossMod.LOGGER.warn("[FTBoss][Balance] \u98df\u7269\u589e\u76ca {} \u5df2\u9694\u79bb", (Object)ForgeRegistries.MOB_EFFECTS.getKey((Object)effect), (Object)failure);
            return;
        }
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123748_, boss.m_20185_(), boss.m_20186_() + 1.2, boss.m_20189_(), 16, 0.5, 0.6, 0.5, 0.05);
        level2.m_5594_(null, boss.m_20183_(), SoundEvents.f_11912_, SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    @SubscribeEvent
    public static void applicable(MobEffectEvent.Applicable event) {
        EasterBossEntity boss;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof EasterBossEntity && (boss = (EasterBossEntity)livingEntity).getVariant() == 4 && ManorBalanceHooks.mainBoss(boss) && event.getEffectInstance() != null && ManorBalanceHooks.forbidden(boss, event.getEffectInstance().m_19544_())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        EasterBossEntity boss;
        block12: {
            block11: {
                LivingEntity livingEntity = event.getEntity();
                if (!(livingEntity instanceof EasterBossEntity)) break block11;
                boss = (EasterBossEntity)livingEntity;
                if (!boss.m_9236_().f_46443_) break block12;
            }
            return;
        }
        AttributeInstance speed = boss.m_21051_(Attributes.f_22279_);
        if (speed != null) {
            if (!ManorBalanceHooks.mainBoss(boss)) {
                speed.m_22120_(SPEED_ID);
            } else if (speed.m_22111_(SPEED_ID) == null) {
                speed.m_22118_(new AttributeModifier(SPEED_ID, "ftboss.manor_balance_speed", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (!ManorBalanceHooks.mainBoss(boss) || !boss.m_6084_()) {
            return;
        }
        if (boss.getVariant() == 4 && boss.f_19797_ % 10 == 0) {
            for (MobEffectInstance effect : new ArrayList(boss.m_21220_())) {
                if (!ManorBalanceHooks.forbidden(boss, effect.m_19544_())) continue;
                boss.m_21195_(effect.m_19544_());
            }
        }
        if (!(boss.getVariant() != 7 || boss.m_21525_() || boss.m_20147_() || boss.m_20145_() || boss.isPerformingSkill() || boss.isCastingLike() || HiddenBossIntro.isActing(boss.m_20148_()) || ManorFight.isFinaleMember((LivingEntity)boss))) {
            ManorShadowBlink.tick(boss);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ZICAI_TICKS.clear();
        BROKEN_FOOD.clear();
        emptyFoodLogged = false;
        ManorShadowBlink.clear();
    }
}

