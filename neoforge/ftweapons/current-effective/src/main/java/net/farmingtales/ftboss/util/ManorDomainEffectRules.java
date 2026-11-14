package net.farmingtales.ftboss.util;

public final class ManorDomainEffectRules {
    public static final int PULSE_TICKS = 100;
    public static final int LEECH_COOLDOWN = 20;
    public static final double LEECH_FRACTION = 0.08;
    public static final float LEECH_CAP = 2.0F;

    private ManorDomainEffectRules() {
    }

    public static String kind(int var0) {
        return switch (var0) {
            case 0 -> "Boss受益";
            case 1, 2, 4, 5, 7, 11 -> "双方共享";
            case 3, 6, 8, 10 -> "玩家受害";
            default -> "无领域";
        };
    }

    public static String name(int var0) {
        return switch (var0) {
            case 0 -> "琼华照阙";
            case 1 -> "长夜悬灯";
            case 2 -> "参商渡星";
            case 3 -> "无间玄庭";
            case 4 -> "扶桑照野";
            case 5 -> "沧溟归墟";
            case 6 -> "璇玑太虚";
            case 7 -> "幽篁照影";
            case 8 -> "溟海浮槎";
            default -> "无领域";
            case 10 -> "霜华停云";
            case 11 -> "紫微梦阙";
        };
    }

    public static String description(int var0) {
        return switch (var0) {
            case 0 -> "Boss护甲+4、承受伤害-10%。";
            case 1 -> "双方命中吸血8%，每秒最多触发一次、最多恢复2点；Boss远程伤害+18%；玩家仅对本场Boss阵营生效。";
            case 2 -> "双方进行6格及以上的远程命中，伤害+22%；玩家仅对本场Boss阵营生效。";
            case 3 -> "玩家获得的治疗降低25%；Boss对生命低于60%的目标近战伤害+25%。";
            case 4 -> "双方攻击属性+12%。";
            case 5 -> "双方承受伤害-10%、移动速度-10%；Boss远程伤害+18%。";
            case 6 -> "玩家移动速度-15%；Boss击退抗性+0.35、远程伤害+20%。";
            case 7 -> "双方从目标背后近战命中，伤害+20%；Boss移动速度+12%。";
            case 8 -> "玩家护甲-3；Boss护甲+2。";
            default -> "";
            case 10 -> "玩家移动速度-20%；Boss护甲+3、击退抗性+0.20。";
            case 11 -> "双方生命低于70%时，每5秒恢复最大生命值1%；玩家每次最多恢复1点；Boss每第三次远程命中伤害+35%。";
        };
    }

    public static double playerArmor(int var0) {
        return var0 == 8 ? -3.0 : 0.0;
    }

    public static double playerSpeed(int var0) {
        return switch (var0) {
            case 5 -> -0.1;
            case 6 -> -0.15;
            case 10 -> -0.2;
            default -> 0.0;
        };
    }

    public static double bossSpeed(int var0) {
        return var0 == 5 ? -0.1 : ManorDomainRules.speed(var0);
    }

    public static double playerAttack(int var0) {
        return var0 == 4 ? 0.12 : 0.0;
    }

    public static double incoming(int var0) {
        return var0 != 0 && var0 != 5 ? 1.0 : 0.9;
    }

    public static double healing(int var0) {
        return var0 == 3 ? 0.75 : 1.0;
    }

    public static double playerOutgoing(int var0, boolean var1, double var2, boolean var4) {
        if (var0 == 2 && var1 && Double.isFinite(var2) && var2 >= 6.0) {
            return 1.22;
        } else {
            return var0 == 7 && !var1 && var4 ? 1.2 : 1.0;
        }
    }

    public static float leech(float var0, float var1) {
        return Float.isFinite(var0) && Float.isFinite(var1) && !(var0 <= 0.0F) && !(var1 <= 0.0F)
            ? (float)Math.min(2.0, (double)Math.min(var0, var1) * 0.08)
            : 0.0F;
    }

    public static float prayer(float var0, float var1, boolean var2) {
        if (Float.isFinite(var0) && Float.isFinite(var1) && !(var1 <= 0.0F) && !(var0 <= 0.0F) && !(var0 >= var1 * 0.7F)) {
            return var2 ? Math.min(1.0F, var1 * 0.01F) : var1 * 0.01F;
        } else {
            return 0.0F;
        }
    }
}
