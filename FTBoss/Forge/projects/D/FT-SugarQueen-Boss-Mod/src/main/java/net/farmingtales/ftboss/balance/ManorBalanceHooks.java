package net.farmingtales.ftboss.balance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorImpossibleRules;
import net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.ManorBalanceAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 统一处理庄园增速、场地效果分发与柠檬安全食物增益。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorBalanceHooks {
    private static final UUID SPEED_ID = UUID.fromString("ea2b6714-7949-4df1-a9d1-8b4e36f52416");
    private static final Map<EasterBossEntity, Long> ZICAI_TICKS = new WeakHashMap<>();
    private static final Set<ResourceLocation> BROKEN_FOOD = new HashSet<>();
    private static boolean emptyFoodLogged;

    private ManorBalanceHooks() {}

    public static boolean mainBoss(EasterBossEntity boss) {
        return !boss.isMini() && !boss.isGiant() && boss.getVariant() >= 0 && boss.getVariant() <= 11;
    }

    public static List<ServerPlayer> players(EasterBossEntity boss, ServerLevel level) {
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (validPlayer(boss, player) && inArena(boss, player)) result.add(player);
        }
        return result;
    }

    private static boolean inArena(EasterBossEntity boss, ServerPlayer player) {
        UUID fight = ManorFight.fightId(boss);
        UUID playerFight = ManorFightMembers.fightId(player);
        if (fight != null && playerFight != null && !fight.equals(playerFight)) return false;
        Vec3 anchor = fight != null && ManorFight.hasActive(fight) ? ManorFight.anchorOf(fight) : null;
        return anchor != null ? player.distanceToSqr(anchor) <= 96.0 * 96.0
                : boss.getBoundingBox().inflate(48.0).contains(player.position());
    }

    private static boolean hitPlayer(EasterBossEntity boss, ServerPlayer player) {
        return !(player instanceof FakePlayer) && !player.isRemoved() && !player.isCreative()
                && !player.isSpectator() && player.level() == boss.level()
                && !boss.isAlliedTo(player) && !player.isAlliedTo(boss);
    }

    public static boolean validPlayer(EasterBossEntity boss, ServerPlayer player) {
        return player.isAlive() && hitPlayer(boss, player);
    }

    public static int tridentInterval(int original, EasterBossEntity boss, String channel) {
        if (boss.getVariant() != 5 || !mainBoss(boss) || !(boss.level() instanceof ServerLevel level)) return original;
        return ManorBalanceRules.tridentInterval(players(boss, level).size());
    }

    public static int castTicks(int ticks, LivingEntity caster) {
        return caster instanceof EasterBossEntity boss && boss.getVariant() == 1 && mainBoss(boss)
                ? ManorBalanceRules.fasterCast(ticks) : ticks;
    }

    public static boolean crossProtected(boolean original, EasterBossEntity boss) {
        return original || boss.getVariant() == 11 && mainBoss(boss) && boss.isAlive() && !boss.isRemoved()
                && !boss.isInvulnerable() && net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard.holding(boss);
    }

    public static int fiveAge(int age, net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill skill) {
        return skill.variant == 1 ? skill.windup - ManorBalanceRules.fasterCast(skill.windup - age) : age;
    }

    public static void zicai(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        ManorBalanceAccess access = (ManorBalanceAccess) boss;
        if (boss.getVariant() != 8 || !mainBoss(boss) || !(target instanceof ServerPlayer player)) {
            access.ftboss$applyPufferSingle(level, target);
            return;
        }
        if (!hitPlayer(boss, player)) return;
        List<ServerPlayer> participants = players(boss, level);
        boolean dyingMember = !player.isAlive() && inArena(boss, player);
        if (participants.size() + (dyingMember ? 1 : 0) < 2 || !participants.contains(player) && !dyingMember) {
            if (player.isAlive()) access.ftboss$applyPufferSingle(level, target);
            return;
        }
        long now = level.getGameTime();
        Long previous = ZICAI_TICKS.put(boss, now);
        if (previous != null && previous == now) return;
        for (ServerPlayer participant : participants) access.ftboss$applyPufferSingle(level, participant);
    }

    public static void zicaiAttack(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        if (boss.getTarget() == target) {
            zicai(boss, level, target);
        } else if (boss.getVariant() == 8 && mainBoss(boss) && target instanceof ServerPlayer player
                && hitPlayer(boss, player) && inArena(boss, player)) {
            List<ServerPlayer> participants = players(boss, level);
            if (participants.size() + (player.isAlive() ? 0 : 1) >= 2) zicai(boss, level, target);
        }
    }

    public static void zicaiMelee(EasterBossEntity boss, LivingEntity target) {
        if (boss != null && target instanceof ServerPlayer && boss.getVariant() == 8
                && boss.level() instanceof ServerLevel level) zicaiAttack(boss, level, target);
    }

    private static boolean forbidden(EasterBossEntity boss, MobEffect effect) {
        ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        Set<String> banned = boss.isImpossibleChallenge() ? ManorImpossibleRules.BANNED_FOOD_BUFFS
                : ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS;
        return LemonImmuneBuffRules.isForbidden(effect) || key != null && banned.contains(key.toString());
    }

    public static void food(EasterBossEntity boss) {
        if (boss.getVariant() != 4 || !mainBoss(boss) || !boss.isAlive()
                || !(boss.level() instanceof ServerLevel level)) return;
        List<MobEffect> pool = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (key != null && ManorBalanceRules.foodMod(key.getNamespace()) && effect.isBeneficial()
                    && !forbidden(boss, effect) && !BROKEN_FOOD.contains(key)) pool.add(effect);
        }
        if (pool.isEmpty()) {
            if (!emptyFoodLogged) FTBossMod.LOGGER.warn("[FTBoss][Balance] 柠檬没有可用且未被禁用的食物模组增益，跳过本次施加");
            emptyFoodLogged = true;
            return;
        }
        MobEffect effect = pool.get(boss.getRandom().nextInt(pool.size()));
        try {
            MobEffectInstance previous = boss.getEffect(effect);
            int amplifier = ManorExpansionRules.nextAmplifier(previous == null ? -1 : previous.getAmplifier());
            if (effect == net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE
                    && amplifier >= LemonImmuneBuffRules.MAX_RESISTANCE_AMPLIFIER) return;
            if (previous != null) boss.removeEffect(effect);
            if (!boss.addEffect(new MobEffectInstance(effect, ManorBalanceRules.FOOD_DURATION_TICKS, amplifier, false, true, false))) return;
        } catch (Throwable failure) {
            BROKEN_FOOD.add(ForgeRegistries.MOB_EFFECTS.getKey(effect));
            try {
                boss.removeEffect(effect);
            } catch (Throwable cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            FTBossMod.LOGGER.warn("[FTBoss][Balance] 食物增益 {} 已隔离", ForgeRegistries.MOB_EFFECTS.getKey(effect), failure);
            return;
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, boss.getX(), boss.getY() + 1.2, boss.getZ(),
                16, 0.5, 0.6, 0.5, 0.05);
        level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    @SubscribeEvent
    public static void applicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof EasterBossEntity boss && boss.getVariant() == 4 && mainBoss(boss)
                && event.getEffectInstance() != null && forbidden(boss, event.getEffectInstance().getEffect())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || boss.level().isClientSide) return;
        var speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (!mainBoss(boss)) speed.removeModifier(SPEED_ID);
            else if (speed.getModifier(SPEED_ID) == null) speed.addTransientModifier(new AttributeModifier(
                    SPEED_ID, "ftboss.manor_balance_speed", ManorBalanceRules.MOVEMENT_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (!mainBoss(boss) || !boss.isAlive()) return;
        if (boss.getVariant() == 4 && boss.tickCount % 10 == 0) {
            for (MobEffectInstance effect : new ArrayList<>(boss.getActiveEffects())) {
                if (forbidden(boss, effect.getEffect())) boss.removeEffect(effect.getEffect());
            }
        }
        if (boss.getVariant() == 7 && !boss.isNoAi() && !boss.isInvulnerable() && !boss.isInvisible()
                && !HiddenBossIntro.isActing(boss.getUUID())
                && !ManorFight.isFinaleMember(boss)) ManorShadowBlink.tick(boss);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ZICAI_TICKS.clear();
        BROKEN_FOOD.clear();
        emptyFoodLogged = false;
        ManorShadowBlink.clear();
    }
}
