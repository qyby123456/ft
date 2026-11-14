package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * **按场上人数缩放**(2026-09-11 按用户要求"根据场上玩家数量来提高 Boss 对应的血量, 每多一个人血量 ×1.25, 伤害增加 15%")。
 *
 * <ul>
 *   <li>场上人数 = Boss 周围 {@value #RADIUS} 格内的存活玩家(创造/旁观不计);</li>
 *   <li>血量倍率 = {@code 1.25^(n-1)}, 伤害倍率 = {@code 1 + 0.15*(n-1)};</li>
 *   <li>每 20t 重算一次(玩家加入/离开自动跟随), **改动最大生命时按当前血量比例缩放**, 不会出现"血量突然跳满";</li>
 *   <li>基准值(首次 tick 时的最大生命/攻击力)存在持久数据里, 反复缩放不会叠加;</li>
 *   <li>自定义伤害(光束/闪电打击/球技能/撞击)统一用 {@link #dmg} 乘倍率, 倍率也写在持久数据里。</li>
 * </ul>
 */
public final class BossScaling {

    /** 统计半径(格) */
    public static final double RADIUS = 48.0;
    /** 血量每多一人的倍率 */
    public static final double HP_PER_PLAYER = 1.25;
    /** 伤害每多一人的加成 */
    public static final double DMG_PER_PLAYER = 0.15;

    private static final String K_BASE_HP = "ftboss_base_hp";
    private static final String K_BASE_ATK = "ftboss_base_atk";
    private static final String K_DMG_MUL = "ftboss_dmg_mul";
    /** 终结最大生命修复迁移标记，避免人数缩放把载入时补齐的生命重新覆盖。 */
    private static final String K_FINALE_HP_FIXED = "ftboss_finale_hp_fixed";

    private BossScaling() {}

    /** 场上参战人数(Boss 周围 RADIUS 格内的存活玩家) */
    public static int playersInFight(LivingEntity boss) {
        if (!(boss.level() instanceof ServerLevel sl)) return 1;
        int n = 0;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(RADIUS))) {
            if (p.isAlive() && !p.isCreative() && !p.isSpectator()) n++;
        }
        return Math.max(1, n);
    }

    /** 每 20t 由 BossHandler 调用 */
    public static void tick(LivingEntity boss) {
        if (DoraPursuitClone.isClone(boss)) return;
        ManorHealthBalance.normalize(boss);
        if (boss == null || boss.level().isClientSide || boss.isDeadOrDying()) return;
        var data = boss.getPersistentData();
        boolean finale = net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.isFinale(boss);
        boolean hadBaseHealth = data.contains(K_BASE_HP);
        if (!hadBaseHealth) {
            // 取属性基础值，避免把其他修饰符已经计算过的最终最大生命再次当作人数缩放基准。
            var hp0 = boss.getAttribute(Attributes.MAX_HEALTH);
            data.putDouble(K_BASE_HP, hp0 != null ? hp0.getBaseValue() : boss.getMaxHealth());
            var atk0 = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            data.putDouble(K_BASE_ATK, atk0 != null ? atk0.getBaseValue() : 0.0);
        }
        if (finale && !data.getBoolean(K_FINALE_HP_FIXED)) {
            /* 旧存档可能已把未翻倍生命写入 K_BASE_HP。NonManorFinaleDifficulty 在实体载入时
               已先补齐属性基础值，这里只在迁移首刻用该值更新人数缩放基准。 */
            var maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
            if (maximumHealth != null) data.putDouble(K_BASE_HP, maximumHealth.getBaseValue());
            data.putBoolean(K_FINALE_HP_FIXED, true);
        }
        int n = playersInFight(boss);
        double hpMul = Math.pow(HP_PER_PLAYER, n - 1);
        double dmgMul = 1.0 + DMG_PER_PLAYER * (n - 1);
        data.putDouble(K_DMG_MUL, dmgMul);

        var maxHp = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            double base = data.getDouble(K_BASE_HP);
            double want = ZhuniPairCombat.maximumHealth(base * hpMul, boss);
            if (Math.abs(maxHp.getBaseValue() - want) > 0.01) {
                float ratio = boss.getMaxHealth() > 0f ? boss.getHealth() / boss.getMaxHealth() : 1f;
                maxHp.setBaseValue(want);
                if (boss instanceof net.farmingtales.ftboss.entity.MeatMountainEntity) {
                    boss.setHealth(ButcherPhaseRules.scaledHealth(ratio, boss.getMaxHealth()));
                } else {
                    boss.setHealth((float) Math.max(1.0, Math.min(want, ratio * want)));
                }
            }
        }
        var atk = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            double base = data.getDouble(K_BASE_ATK);
            double want = base * dmgMul;
            if (Math.abs(atk.getBaseValue() - want) > 0.01) atk.setBaseValue(want);
        }
    }

    /** 自定义伤害倍率(未初始化时按 1× 处理) */
    public static float dmg(LivingEntity boss, float base) {
        if (boss == null) return base;
        double m = boss.getPersistentData().getDouble(K_DMG_MUL);
        return (float) (base * (m <= 0.0 ? 1.0 : m));
    }

    /** 当前倍率(日志/调试) */
    public static double dmgMul(LivingEntity boss) {
        double m = boss.getPersistentData().getDouble(K_DMG_MUL);
        return m <= 0.0 ? 1.0 : m;
    }
}
