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
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorBalanceHooks {
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_balance/ea2b6714-7949-4df1-a9d1-8b4e36f52416");
    private static final Map<EasterBossEntity, Long> ZICAI_TICKS = new WeakHashMap<>();
    private static final Set<ResourceLocation> BROKEN_FOOD = new HashSet<>();
    private static boolean emptyFoodLogged;

    private ManorBalanceHooks() {
    }

    public static boolean mainBoss(EasterBossEntity boss) {
        return !boss.isMini() && !boss.isGiant() && boss.getVariant() >= 0 && boss.getVariant() <= 11;
    }

    public static List<ServerPlayer> players(EasterBossEntity boss, ServerLevel level) {
        List<ServerPlayer> result = new ArrayList<>();

        for (ServerPlayer player : level.players()) {
            if (validPlayer(boss, player) && inArena(boss, player)) {
                result.add(player);
            }
        }

        return result;
    }

    private static boolean inArena(EasterBossEntity boss, ServerPlayer player) {
        UUID fight = ManorFight.fightId(boss);
        UUID playerFight = ManorFightMembers.fightId(player);
        if (fight != null && playerFight != null && !fight.equals(playerFight)) {
            return false;
        } else {
            Vec3 anchor = fight != null && ManorFight.hasActive(fight) ? ManorFight.anchorOf(fight) : null;
            return anchor != null ? player.distanceToSqr(anchor) <= 9216.0 : boss.getBoundingBox().inflate(48.0).contains(player.position());
        }
    }

    private static boolean hitPlayer(EasterBossEntity boss, ServerPlayer player) {
        return !(player instanceof FakePlayer)
            && !player.isRemoved()
            && !player.isCreative()
            && !player.isSpectator()
            && player.level() == boss.level()
            && !boss.isAlliedTo(player)
            && !player.isAlliedTo(boss);
    }

    public static boolean validPlayer(EasterBossEntity boss, ServerPlayer player) {
        return player.isAlive() && hitPlayer(boss, player);
    }

    public static int tridentInterval(int original, EasterBossEntity boss, String channel) {
        return boss.getVariant() == 5 && mainBoss(boss) && boss.level() instanceof ServerLevel level
            ? ManorBalanceRules.tridentInterval(players(boss, level).size())
            : original;
    }

    public static int castTicks(int ticks, LivingEntity caster) {
        if (caster instanceof EasterBossEntity boss && boss.getVariant() == 1 && mainBoss(boss)) {
            return ManorBalanceRules.fasterCast(ticks);
        }

        return ticks;
    }

    public static boolean crossProtected(boolean original, EasterBossEntity boss) {
        return original
            || boss.getVariant() == 11 && mainBoss(boss) && boss.isAlive() && !boss.isRemoved() && !boss.isInvulnerable() && CrossGuard.holding(boss);
    }

    public static int fiveAge(int age, Skill skill) {
        return skill.variant == 1 ? skill.windup - ManorBalanceRules.fasterCast(skill.windup - age) : age;
    }

    public static void zicai(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        ManorBalanceAccess access = (ManorBalanceAccess)boss;
        if (boss.getVariant() == 8 && mainBoss(boss) && target instanceof ServerPlayer player) {
            if (hitPlayer(boss, player)) {
                List<ServerPlayer> participants = players(boss, level);
                boolean dyingMember = !player.isAlive() && inArena(boss, player);
                if (participants.size() + (dyingMember ? 1 : 0) >= 2 && (participants.contains(player) || dyingMember)) {
                    long now = level.getGameTime();
                    Long previous = ZICAI_TICKS.put(boss, now);
                    if (previous == null || previous != now) {
                        for (ServerPlayer participant : participants) {
                            access.ftboss$applyPufferSingle(level, participant);
                        }
                    }
                } else {
                    if (player.isAlive()) {
                        access.ftboss$applyPufferSingle(level, target);
                    }
                }
            }
        } else {
            access.ftboss$applyPufferSingle(level, target);
        }
    }

    public static void zicaiAttack(EasterBossEntity boss, ServerLevel level, LivingEntity target) {
        if (boss.getTarget() == target) {
            zicai(boss, level, target);
        } else if (boss.getVariant() == 8 && mainBoss(boss) && target instanceof ServerPlayer player && hitPlayer(boss, player) && inArena(boss, player)) {
            List<ServerPlayer> participants = players(boss, level);
            if (participants.size() + (player.isAlive() ? 0 : 1) >= 2) {
                zicai(boss, level, target);
            }
        }
    }

    public static void zicaiMelee(EasterBossEntity boss, LivingEntity target) {
        if (boss != null && target instanceof ServerPlayer && boss.getVariant() == 8 && boss.level() instanceof ServerLevel level) {
            zicaiAttack(boss, level, target);
        }
    }

    private static boolean forbidden(EasterBossEntity boss, Holder<MobEffect> effect) {
        ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
        Set<String> banned = boss.isImpossibleChallenge() ? ManorImpossibleRules.BANNED_FOOD_BUFFS : ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS;
        return LemonImmuneBuffRules.isForbidden(effect.value()) || key != null && banned.contains(key.toString());
    }

    public static void food(EasterBossEntity boss) {
        if (boss.getVariant() == 4 && mainBoss(boss) && boss.isAlive() && boss.level() instanceof ServerLevel level) {
            ArrayList var8 = new ArrayList();

            for (Holder<MobEffect> effect : BuiltInRegistries.MOB_EFFECT.holders().toList()) {
                ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
                if (key != null
                    && ManorBalanceRules.foodMod(key.getNamespace())
                    && effect.value().isBeneficial()
                    && !forbidden(boss, effect)
                    && !BROKEN_FOOD.contains(key)) {
                    var8.add(effect);
                }
            }

            if (var8.isEmpty()) {
                if (!emptyFoodLogged) {
                    FTBossMod.LOGGER.warn("[FTBoss][Balance] 柠檬没有可用且未被禁用的食物模组增益，跳过本次施加");
                }

                emptyFoodLogged = true;
            } else {
                Holder<MobEffect> effectx = (Holder<MobEffect>)var8.get(boss.getRandom().nextInt(var8.size()));

                try {
                    MobEffectInstance previous = boss.getEffect(effectx);
                    int amplifier = ManorExpansionRules.nextAmplifier(previous == null ? -1 : previous.getAmplifier());
                    if (effectx == MobEffects.DAMAGE_RESISTANCE && amplifier >= 3) {
                        return;
                    }

                    if (previous != null) {
                        boss.removeEffect(effectx);
                    }

                    if (!boss.addEffect(new MobEffectInstance(effectx, 300, amplifier, false, true, false))) {
                        return;
                    }
                } catch (Throwable var7) {
                    BROKEN_FOOD.add(BuiltInRegistries.MOB_EFFECT.getKey(effectx.value()));

                    try {
                        boss.removeEffect(effectx);
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }

                    FTBossMod.LOGGER.warn("[FTBoss][Balance] 食物增益 {} 已隔离", BuiltInRegistries.MOB_EFFECT.getKey(effectx.value()), var7);
                    return;
                }

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, boss.getX(), boss.getY() + 1.2, boss.getZ(), 16, 0.5, 0.6, 0.5, 0.05);
                level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0F, 1.2F);
            }
        }
    }

    @SubscribeEvent
    public static void applicable(Applicable event) {
        if (event.getEntity() instanceof EasterBossEntity boss
            && boss.getVariant() == 4
            && mainBoss(boss)
            && event.getEffectInstance() != null
            && forbidden(boss, event.getEffectInstance().getEffect())) {
            event.setResult(Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void tick(Pre event) {
        if (event.getEntity() instanceof EasterBossEntity boss && !boss.level().isClientSide) {
            AttributeInstance speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                if (!mainBoss(boss)) {
                    speed.removeModifier(SPEED_ID);
                } else if (speed.getModifier(SPEED_ID) == null) {
                    speed.addTransientModifier(new AttributeModifier(SPEED_ID, 0.5, Operation.ADD_MULTIPLIED_TOTAL));
                }
            }

            if (mainBoss(boss) && boss.isAlive()) {
                if (boss.getVariant() == 4 && boss.tickCount % 10 == 0) {
                    for (MobEffectInstance effect : new ArrayList<>(boss.getActiveEffects())) {
                        if (forbidden(boss, effect.getEffect())) {
                            boss.removeEffect(effect.getEffect());
                        }
                    }
                }

                if (boss.getVariant() == 7
                    && !boss.isNoAi()
                    && !boss.isInvulnerable()
                    && !boss.isInvisible()
                    && !boss.isPerformingSkill()
                    && !boss.isCastingLike()
                    && !HiddenBossIntro.isActing(boss.getUUID())
                    && !ManorFight.isFinaleMember(boss)) {
                    ManorShadowBlink.tick(boss);
                }

                return;
            }

            return;
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
