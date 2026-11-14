package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Collection;
import java.util.List;

/**
 * 「召唤物继承低级增益」: Boss 会把自身增益的<b>降级版（最高 I 级）</b>分享给自己旗下的召唤物。
 *
 * <p>对应需求：削弱 Boss 本体强度(少召唤/少回血)后, 用"召唤物能吃到 Boss 部分低级 buff"来保留战场层次。
 * 覆盖对象：沙耶的茶芽小妖/守护茶芽、鼠群小老鼠、屠夫的屠夫学徒波次、糖霜女皇的糖结晶与铁魔法侍从。</p>
 */
public final class MinionBlessing {

    /** 召唤物标记(在生成处调用 {@link #mark}) */
    private static final String TAG = "ftboss_summon";
    /** v112: 召唤者 UUID 的键 —— 清场时要认主人, 别再"看到带标记的就清"(会误伤别人的小弟) */
    private static final String K_OWNER = "ftboss_summon_owner";
    /** 下一次祝福倒计时 */
    private static final String K_CD = "ftboss_bless_cd";

    private static final int PERIOD = 60;        // 每 3 秒祝福一次
    private static final int MAX_MINIONS = 8;    // 单次最多祝福数量
    private static final int MAX_EFFECTS = 3;    // 每个召唤物最多继承 3 种
    private static final int DURATION = 140;     // 增益持续时间(Boss 持续供给)

    private MinionBlessing() {}

    /** 在召唤物生成处标记(屠夫学徒/糖结晶/铁魔法侍从等)。不带主人 = 只参与"继承增益", 不会被按主人清场 */
    public static void mark(Entity e) {
        mark(e, null);
    }

    /** v112: 带主人的标记 —— {@link SummonSweep#clearMarked} 只清"主人对得上"的, 避免女皇退场清掉沙耶的茶芽 */
    public static void mark(Entity e, java.util.UUID ownerId) {
        e.getPersistentData().putBoolean(TAG, true);
        if (ownerId != null) e.getPersistentData().putUUID(K_OWNER, ownerId);
    }

    /** v112: 这个召唤物记的主人(没记过/不是带主人标记的 -> null) */
    public static java.util.UUID ownerId(Entity e) {
        try {
            return e.getPersistentData().hasUUID(K_OWNER) ? e.getPersistentData().getUUID(K_OWNER) : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /** 是否为"自己人": 沙耶茶芽(含守护茶芽) / 鼠群小老鼠 / 已标记的召唤物 */
    public static boolean isSummon(Entity e) {
        if (e instanceof TeaMinionEntity) return true;
        if (e instanceof RatBossEntity r && r.isMinion()) return true;
        return e.getPersistentData().getBoolean(TAG);
    }

    /** 每 tick 调用(服务端): 周期性把自身有益增益降级分享给召唤物 */
    public static void tick(LivingEntity boss, double range) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()) return;
        var tag = boss.getPersistentData();
        int cd = tag.getInt(K_CD);
        if (cd > 0) {
            tag.putInt(K_CD, cd - 1);
            return;
        }
        tag.putInt(K_CD, PERIOD);

        Collection<MobEffectInstance> own = boss.getActiveEffects();
        if (own.isEmpty()) return;

        List<Mob> nearby = boss.level().getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(range));
        int n = 0;
        for (Mob m : nearby) {
            if (n >= MAX_MINIONS) break;
            if (!m.isAlive() || m == boss || !isSummon(m)) continue;
            share(boss, m, own);
            n++;
        }
    }

    /** 降级分享: 等级取 min(1, Boss等级-1)，即 Boss II/III 级 → 召唤物 I 级 */
    private static void share(LivingEntity boss, LivingEntity minion, Collection<MobEffectInstance> own) {
        int applied = 0;
        for (MobEffectInstance inst : own) {
            if (inst.getEffect() == null || !inst.getEffect().isBeneficial()) continue;
            // 2026-09-10 按用户要求: 鼠群的小老鼠**不继承回复类增益**(生命恢复/伤害吸收/生命提升/饱和)
            if (minion instanceof RatBossEntity rm && rm.isMinion() && isHealing(inst.getEffect())) continue;
            int amp = Math.min(1, Math.max(0, inst.getAmplifier() - 1));
            minion.addEffect(new MobEffectInstance(inst.getEffect(), DURATION, amp, false, false, true));
            if (++applied >= MAX_EFFECTS) break;
        }
        if (applied > 0 && boss.level() instanceof ServerLevel sl) {
            Fx.particles(sl, minion.getX(), minion.getY() + 1.0, minion.getZ(), 6,
                    ParticleTypes.HAPPY_VILLAGER, 0.3, 0.03);
        }
    }

    /** 回复类增益(不给小老鼠的那几种) */
    private static boolean isHealing(MobEffect e) {
        return e == MobEffects.REGENERATION || e == MobEffects.ABSORPTION
                || e == MobEffects.HEALTH_BOOST || e == MobEffects.SATURATION;
    }
}
