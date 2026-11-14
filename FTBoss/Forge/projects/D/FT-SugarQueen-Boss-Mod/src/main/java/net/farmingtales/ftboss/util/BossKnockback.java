package net.farmingtales.ftboss.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Boss 击退规则（2026-09-19 按用户要求）。
 *
 * <p><b>① 「Boss 受到更少击退」</b>（2026-09-24 用户要求「减少 boss 受到的击退」）：
 * 所有 Boss 本体的抗击退属性统一取 {@link #BOSS_RESISTANCE} —— 0.5（吃一半）→ <b>0.85</b>（只吃 15%）。
 * 依旧是"允许被击退"而不是完全免疫（用户 2026-09-19 定的口径没变）。
 * 唯一的例外是「功底」穿上亚当甲那套装备效果时（{@code EasterBossEntity} 里会把基础值抬到 0.9）——
 * 那是它自己的护甲机制，用户没有点名要去掉。
 *
 * <p><b>② 「Boss 对玩家的击退更少」</b>（2026-09-24 用户要求「boss 对玩家击退减少」）：
 * Boss 代码里所有"推/击退"都走本类，目标若是玩家，水平分量乘 {@link #TO_PLAYER_MULT}（0.25 → <b>0.10</b>），
 * 竖直分量乘 {@link #VERTICAL_MULT}（0.5 → <b>0.25</b>）。
 * 非玩家目标（自家小怪、投射物、Boss 本体）一律原样放行。
 *
 * <p>手感只有一个旋钮：把 {@link #TO_PLAYER_MULT} 调小 = 推得更轻；
 * 想让上挑也一起变矮，把 {@link #SCALE_VERTICAL} 打开即可。
 *
 * <p>普通受击的击退由 BossKnockbackEvents 按伤害来源处理；显式技能调用不会重复缩放。
 */
public final class BossKnockback {
    public static final double XUANLAN_MULTIPLIER = 1.5;

    /** Boss 本体抗击退：0 = 完全被推飞，1 = 完全免疫。
     *  ★ 2026-09-24 用户要求「减少 boss 受到的击退」：0.5 → <b>0.85</b>（只吃 15% 击退）。 */
    public static final double BOSS_RESISTANCE = 0.85;

    /** Boss 打玩家的击退倍率（水平分量）。
     *  ★ 2026-09-24 用户要求「boss 对玩家击退减少」：0.25 → <b>0.10</b>。 */
    public static final double TO_PLAYER_MULT = 0.10;

    /** 是否连竖直分量（上挑/击飞高度）一起削。 */
    public static final boolean SCALE_VERTICAL = true;
    /** ★ 2026-09-24：竖直分量 0.5 → <b>0.25</b>（和水平分量一起再降一档）。 */
    public static final double VERTICAL_MULT = 0.25;
    private static final ThreadLocal<LivingEntity> EXPLICIT_TARGET = new ThreadLocal<>();

    private BossKnockback() {}

    public static double sourceMultiplier(Entity source) {
        for (int depth = 0; source != null && depth < 8; depth++) {
            if (source instanceof Player) return 1.0;
            if (source instanceof net.farmingtales.ftboss.entity.EasterBossEntity boss) {
                return boss.getVariant() == 6 ? XUANLAN_MULTIPLIER : 1.0;
            }
            Entity owner = net.farmingtales.ftboss.boss.ManorFightMembers.owner(source);
            if (owner == source) break;
            source = owner;
        }
        return 1.0;
    }

    public static void push(Entity source, Entity target, double dx, double dy, double dz) {
        double multiplier = sourceMultiplier(source);
        push(target, dx * multiplier, dy * multiplier, dz * multiplier);
    }

    public static void knockback(Entity source, LivingEntity target, double strength, double dx, double dz) {
        knockback(target, strength * sourceMultiplier(source), dx, dz);
    }

    public static double vertical(Entity source, Entity target, double strength) {
        return vertical(target, strength * sourceMultiplier(source));
    }

    /** Boss 技能推人：替代直接写 {@code target.push(...)}。玩家按 {@link #TO_PLAYER_MULT} 削。 */
    public static void push(Entity target, double dx, double dy, double dz) {
        if (target instanceof Player) {
            target.push(dx * TO_PLAYER_MULT,
                    vertical(target, dy),
                    dz * TO_PLAYER_MULT);
        } else {
            target.push(dx, dy, dz);
        }
    }

    /** Boss 技能击退：替代直接写 {@code target.knockback(...)}。玩家按 {@link #TO_PLAYER_MULT} 削。 */
    public static void knockback(LivingEntity target, double strength, double dx, double dz) {
        LivingEntity previous = EXPLICIT_TARGET.get();
        EXPLICIT_TARGET.set(target);
        try {
            target.knockback(target instanceof Player ? strength * TO_PLAYER_MULT : strength, dx, dz);
        } finally {
            if (previous == null) EXPLICIT_TARGET.remove();
            else EXPLICIT_TARGET.set(previous);
        }
    }

    public static boolean isExplicit(LivingEntity target) {
        return EXPLICIT_TARGET.get() == target;
    }

    public static double vertical(Entity target, double strength) {
        return target instanceof Player && SCALE_VERTICAL ? strength * VERTICAL_MULT : strength;
    }
}
