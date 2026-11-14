package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 【Boss 索敌的公共判据】(2026-09-14 v114 按用户反馈)。
 *
 * <p>用户原话：「修正 boss 索敌，比如屠夫会锁定创造模式玩家，和只锁定玩家，文玄蓝的飞轮、青衣的闪电都有类似问题」。
 * 也就是 —— **创造模式 / 旁观模式的玩家不能成为 Boss 的目标**：他们打不动 Boss、也不需要被 Boss 打，
 * 一旦被锁上，Boss 就会整场追着一个无敌的人跑，真正的战斗者反而被无视。
 *
 * <p>这里把判据集中成一个地方，覆盖三类地方：
 * <ol>
 *   <li><b>仇恨回击</b>：{@link BossAggro} 记/按仇恨目标前先过滤(创造玩家打 Boss 也不该拉仇恨)；</li>
 *   <li><b>技能挑人</b>：各处"找最近的玩家"的辅助函数(酒桶王/女皇/飞轮…)，挑不到有效玩家就别放技能；</li>
 *   <li><b>兜底清扫</b>：{@code BossHandler} 每 10t 对所有登记 Boss 跑一次 {@link #dropInvalidTarget} ——
 *       玩家中途切成创造/旁观时，已经锁上的目标会被立刻丢掉(这条盖住所有没改到的路径)。</li>
 * </ol>
 */
public final class BossTargets {

    private BossTargets() {}

    /** 这位玩家能不能当目标: 活着 + 未被移除 + 不是创造 + 不是旁观 */
    public static boolean valid(Player p) {
        return p != null && p.isAlive() && !p.isRemoved() && !p.isCreative() && !p.isSpectator();
    }

    /** 实体版判据(非玩家一律放行, 用于"回击打我的人"那条路) */
    public static boolean valid(LivingEntity e) {
        return e != null && e.isAlive() && !e.isRemoved() && (!(e instanceof Player p) || valid(p));
    }

    public static LivingEntity combatTarget(Mob boss, double fallbackRange) {
        LivingEntity target = BossAggro.highest(boss);
        if (target != null) return target;
        return boss.level() instanceof ServerLevel level ? nearest(level, boss, fallbackRange) : null;
    }

    /** 范围内最近的**有效玩家**(没有就返回 null) */
    public static ServerPlayer nearest(ServerLevel sl, double x, double y, double z, double range) {
        if (sl == null) return null;
        ServerPlayer best = null;
        double bd = range * range;
        AABB box = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (!valid(p)) continue;
            double d = p.distanceToSqr(x, y, z);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    /** 以某个实体为中心找最近的**有效玩家** */
    public static ServerPlayer nearest(ServerLevel sl, Entity from, double range) {
        return from == null ? null : nearest(sl, from.getX(), from.getY(), from.getZ(), range);
    }

    /**
     * 兜底: 目标的"当前目标"若是创造/旁观玩家 -> 立刻清空。
     * <p>覆盖全部路径(包括原版 {@code NearestAttackableTargetGoal} 与 {@link BossAggro} 硬设的目标),
     * 也覆盖"开打之后才切成创造"的情况。
     */
    public static void dropInvalidTarget(LivingEntity boss) {
        if (!(boss instanceof Mob m)) return;
        LivingEntity t = m.getTarget();
        if (t != null && (!valid(t) || t.level() != boss.level())) {
            m.setTarget(null);
            m.getNavigation().stop();
        }
    }
}
