package net.farmingtales.ftboss.util;

public final class ManorDomainEffectRules {
    public static final int PULSE_TICKS = 100;
    public static final int LEECH_COOLDOWN = 20;
    public static final double LEECH_FRACTION = 0.12;
    public static final float LEECH_CAP = 3.0f;

    private ManorDomainEffectRules() {}

    public static String kind(int variant) {
        return switch (variant) {
            case 0 -> "Boss受益";
            case 1, 2, 4, 5, 7, 11 -> "双方共享";
            case 3, 6, 8, 10 -> "玩家受害";
            default -> "无领域";
        };
    }

    public static String name(int variant) {
        return switch (variant) {
            case 0 -> "琼华照阙";
            case 1 -> "长夜悬灯";
            case 2 -> "参商渡星";
            case 3 -> "无间玄庭";
            case 4 -> "扶桑照野";
            case 5 -> "沧溟归墟";
            case 6 -> "璇玑太虚";
            case 7 -> "幽篁照影";
            case 8 -> "溟海浮槎";
            case 10 -> "霜华停云";
            case 11 -> "紫微梦阙";
            default -> "无领域";
        };
    }

    public static String description(int variant) {
        return switch (variant) {
            case 0 -> "Boss护甲+6、承受伤害-15%。";
            case 1 -> "双方命中吸血12%，每秒最多触发一次、最多恢复3点；Boss远程伤害+25%；玩家仅对本场Boss阵营生效。";
            case 2 -> "双方进行6格及以上的远程命中，伤害+30%；玩家仅对本场Boss阵营生效。";
            case 3 -> "玩家获得的治疗降低35%；Boss对生命低于60%的目标近战伤害+35%。";
            case 4 -> "双方攻击属性+18%。";
            case 5 -> "双方承受伤害-15%、移动速度-12%；Boss远程伤害+25%。";
            case 6 -> "玩家移动速度-20%；Boss击退抗性+0.45、远程伤害+30%。";
            case 7 -> "双方从目标背后近战命中，伤害+30%；Boss移动速度+18%。";
            case 8 -> "玩家护甲-5；Boss护甲+4。";
            case 10 -> "玩家移动速度-25%；Boss护甲+5、击退抗性+0.30。";
            case 11 -> "双方生命低于70%时，每5秒恢复最大生命值1.5%；玩家每次最多恢复1.5点；Boss每第三次远程命中伤害+45%。";
            default -> "";
        };
    }

    public static double playerArmor(int variant) {
        return variant == 8 ? -5.0 : 0.0;
    }

    public static double playerSpeed(int variant) {
        return switch (variant) {
            case 5 -> -0.12;
            case 6 -> -0.20;
            case 10 -> -0.25;
            default -> 0.0;
        };
    }

    public static double bossSpeed(int variant) {
        return variant == 5 ? -0.12 : ManorDomainRules.speed(variant);
    }

    public static double playerAttack(int variant) {
        return variant == 4 ? 0.18 : 0.0;
    }

    public static double incoming(int variant) {
        return variant == 0 || variant == 5 ? 0.85 : 1.0;
    }

    public static double healing(int variant) {
        return variant == 3 ? 0.65 : 1.0;
    }

    public static double playerOutgoing(int variant, boolean ranged, double distance, boolean behind) {
        if (variant == 2 && ranged && Double.isFinite(distance) && distance >= 6.0) return 1.30;
        return variant == 7 && !ranged && behind ? 1.30 : 1.0;
    }

    public static float leech(float damage, float availableHealth) {
        if (!Float.isFinite(damage) || !Float.isFinite(availableHealth) || damage <= 0.0f || availableHealth <= 0.0f) return 0.0f;
        return (float) Math.min(LEECH_CAP, Math.min(damage, availableHealth) * LEECH_FRACTION);
    }

    public static float prayer(float health, float maxHealth, boolean player) {
        if (!Float.isFinite(health) || !Float.isFinite(maxHealth) || maxHealth <= 0.0f
                || health <= 0.0f || health >= maxHealth * 0.70f) return 0.0f;
        return player ? Math.min(1.5f, maxHealth * 0.015f) : maxHealth * 0.015f;
    }
}
