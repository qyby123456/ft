package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * 【召唤上限】(2026-09-12 按用户要求新增)。
 *
 * <p>规则: 以 Boss 为中心 {@value #RADIUS} 格内, **同种召唤物**最多 {@value #MAX_PER_TYPE} 只。
 * 到上限后 Boss **不再召唤**, 改为给场上已有的同类**全体上 buff**(速度 II / 力量 I / 抗性 I, 20 秒)
 * —— 场上不会越堆越多, 但压力并不会因此变小, 反而更凶。
 *
 * <p>用法(召唤点三行式):
 * <pre>
 *   if (SummonLimit.atCap(this, FlyMinionEntity.class)) {
 *       // 已满: atCap 内部已经给同类上了 buff + 播了特效, 这里什么都不用做
 *   } else {
 *       int n = Math.min(想要的数量, SummonLimit.room(this, FlyMinionEntity.class));
 *       ...召唤 n 只...
 *   }
 * </pre>
 */
public final class SummonLimit {

    /** 同种召唤物上限 */
    public static final int MAX_PER_TYPE = 10;
    /** 统计半径(Boss 周围) */
    public static final double RADIUS = 64.0;
    /** 强化持续时间(20 秒) */
    public static final int BUFF_TICKS = 400;

    private SummonLimit() {}

    /** 场上同种、存活的数量 */
    public static int count(LivingEntity boss, Class<? extends Entity> type) {
        List<? extends Entity> list = boss.level().getEntitiesOfClass(type,
                boss.getBoundingBox().inflate(RADIUS), Entity::isAlive);
        return list.size();
    }

    /** 还能再召唤几只(0 = 已到上限) */
    public static int room(LivingEntity boss, Class<? extends Entity> type) {
        return Math.max(0, MAX_PER_TYPE - count(boss, type));
    }

    /**
     * 到上限了吗? —— 到上限会**顺便**把场上同类全体强化并播特效, 调用方直接跳过本次召唤即可。
     */
    public static boolean atCap(LivingEntity boss, Class<? extends Mob> type) {
        if (room(boss, type) > 0) return false;
        empower(boss, type);
        return true;
    }

    /** 给场上同类全体上 buff + 粒子/音效/日志 */
    public static void empower(LivingEntity boss, Class<? extends Mob> type) {
        List<? extends Mob> list = boss.level().getEntitiesOfClass(type,
                boss.getBoundingBox().inflate(RADIUS), Entity::isAlive);
        for (Mob m : list) {
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_TICKS, 1, false, false, true));
            m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_TICKS, 0, false, false, true));
            m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF_TICKS, 0, false, false, true));
        }
        if (boss.level() instanceof ServerLevel sl) {
            Fx.particles(sl, boss.getX(), boss.getY() + 1.2, boss.getZ(), 36,
                    ParticleTypes.HAPPY_VILLAGER, 2.2, 0.12);
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.9f, 1.4f);
            for (var p : sl.players()) {
                if (p.distanceToSqr(boss) <= RADIUS * RADIUS) {
                    p.displayClientMessage(Component.literal("§6召唤已达上限(" + MAX_PER_TYPE
                            + ") §7→ 场上同类§c全体强化§7(速度/力量/抗性 " + (BUFF_TICKS / 20) + "s)"), true);
                }
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] {} 的 {} 已到上限 {}, 本次改为全体强化 {} 只",
                boss.getName().getString(), type.getSimpleName(), MAX_PER_TYPE, list.size());
    }
}
