package net.farmingtales.ftboss.boss.ai;

/** 共享状态数值与时长规则；时间单位为 tick。2026-10-26：供雪黎、紫菜及回归测试共用。 */
public final class CombatStatusRules {
    public static final double ARMOR_BREAK = -0.8;
    public static final double GRIEVOUS_ARMOR = -1.0;
    public static final double BONE_SLOWNESS = -0.45;
    public static final double BONE_WEAKNESS = -12.0;
    public static final float VALOR_HEAL = 5.0F;
    public static final int SECOND = 20;
    public static final int VALOR_DURATION = 5 * SECOND;
    public static final int VALOR_COOLDOWN = 15 * SECOND;

    private CombatStatusRules() {}

    public static int duration(String id, int requested) {
        int limit = switch (id) {
            case "stun", "airborne" -> SECOND;
            case "rooted" -> 2 * SECOND;
            case "ignite" -> 4 * SECOND;
            case "flickering_darkness" -> 5 * SECOND;
            default -> 6 * SECOND;
        };
        return Math.max(1, Math.min(limit, requested));
    }

    public static float darkness(float ticks) {
        return 0.25F + 0.55F * (0.5F + 0.5F * (float) Math.sin(ticks * Math.PI / 20.0));
    }
}
