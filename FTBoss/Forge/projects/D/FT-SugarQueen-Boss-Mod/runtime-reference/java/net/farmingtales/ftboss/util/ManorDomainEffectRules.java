/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorDomainRules;

public final class ManorDomainEffectRules {
    public static final int PULSE_TICKS = 100;
    public static final int LEECH_COOLDOWN = 20;
    public static final double LEECH_FRACTION = 0.08;
    public static final float LEECH_CAP = 2.0f;

    private ManorDomainEffectRules() {
    }

    public static String kind(int n) {
        return switch (n) {
            case 0 -> "Boss\u53d7\u76ca";
            case 1, 2, 4, 5, 7, 11 -> "\u53cc\u65b9\u5171\u4eab";
            case 3, 6, 8, 10 -> "\u73a9\u5bb6\u53d7\u5bb3";
            default -> "\u65e0\u9886\u57df";
        };
    }

    public static String name(int n) {
        return switch (n) {
            case 0 -> "\u743c\u534e\u7167\u9619";
            case 1 -> "\u957f\u591c\u60ac\u706f";
            case 2 -> "\u53c2\u5546\u6e21\u661f";
            case 3 -> "\u65e0\u95f4\u7384\u5ead";
            case 4 -> "\u6276\u6851\u7167\u91ce";
            case 5 -> "\u6ca7\u6e9f\u5f52\u589f";
            case 6 -> "\u7487\u7391\u592a\u865a";
            case 7 -> "\u5e7d\u7bc1\u7167\u5f71";
            case 8 -> "\u6e9f\u6d77\u6d6e\u69ce";
            case 10 -> "\u971c\u534e\u505c\u4e91";
            case 11 -> "\u7d2b\u5fae\u68a6\u9619";
            default -> "\u65e0\u9886\u57df";
        };
    }

    public static String description(int n) {
        return switch (n) {
            case 0 -> "Boss\u62a4\u7532+4\u3001\u627f\u53d7\u4f24\u5bb3-10%\u3002";
            case 1 -> "\u53cc\u65b9\u547d\u4e2d\u5438\u88408%\uff0c\u6bcf\u79d2\u6700\u591a\u89e6\u53d1\u4e00\u6b21\u3001\u6700\u591a\u6062\u590d2\u70b9\uff1bBoss\u8fdc\u7a0b\u4f24\u5bb3+18%\uff1b\u73a9\u5bb6\u4ec5\u5bf9\u672c\u573aBoss\u9635\u8425\u751f\u6548\u3002";
            case 2 -> "\u53cc\u65b9\u8fdb\u884c6\u683c\u53ca\u4ee5\u4e0a\u7684\u8fdc\u7a0b\u547d\u4e2d\uff0c\u4f24\u5bb3+22%\uff1b\u73a9\u5bb6\u4ec5\u5bf9\u672c\u573aBoss\u9635\u8425\u751f\u6548\u3002";
            case 3 -> "\u73a9\u5bb6\u83b7\u5f97\u7684\u6cbb\u7597\u964d\u4f4e25%\uff1bBoss\u5bf9\u751f\u547d\u4f4e\u4e8e60%\u7684\u76ee\u6807\u8fd1\u6218\u4f24\u5bb3+25%\u3002";
            case 4 -> "\u53cc\u65b9\u653b\u51fb\u5c5e\u6027+12%\u3002";
            case 5 -> "\u53cc\u65b9\u627f\u53d7\u4f24\u5bb3-10%\u3001\u79fb\u52a8\u901f\u5ea6-10%\uff1bBoss\u8fdc\u7a0b\u4f24\u5bb3+18%\u3002";
            case 6 -> "\u73a9\u5bb6\u79fb\u52a8\u901f\u5ea6-15%\uff1bBoss\u51fb\u9000\u6297\u6027+0.35\u3001\u8fdc\u7a0b\u4f24\u5bb3+20%\u3002";
            case 7 -> "\u53cc\u65b9\u4ece\u76ee\u6807\u80cc\u540e\u8fd1\u6218\u547d\u4e2d\uff0c\u4f24\u5bb3+20%\uff1bBoss\u79fb\u52a8\u901f\u5ea6+12%\u3002";
            case 8 -> "\u73a9\u5bb6\u62a4\u7532-3\uff1bBoss\u62a4\u7532+2\u3002";
            case 10 -> "\u73a9\u5bb6\u79fb\u52a8\u901f\u5ea6-20%\uff1bBoss\u62a4\u7532+3\u3001\u51fb\u9000\u6297\u6027+0.20\u3002";
            case 11 -> "\u53cc\u65b9\u751f\u547d\u4f4e\u4e8e70%\u65f6\uff0c\u6bcf5\u79d2\u6062\u590d\u6700\u5927\u751f\u547d\u503c1%\uff1b\u73a9\u5bb6\u6bcf\u6b21\u6700\u591a\u6062\u590d1\u70b9\uff1bBoss\u6bcf\u7b2c\u4e09\u6b21\u8fdc\u7a0b\u547d\u4e2d\u4f24\u5bb3+35%\u3002";
            default -> "";
        };
    }

    public static double playerArmor(int n) {
        return n == 8 ? -3.0 : 0.0;
    }

    public static double playerSpeed(int n) {
        return switch (n) {
            case 5 -> -0.1;
            case 6 -> -0.15;
            case 10 -> -0.2;
            default -> 0.0;
        };
    }

    public static double bossSpeed(int n) {
        return n == 5 ? -0.1 : ManorDomainRules.speed(n);
    }

    public static double playerAttack(int n) {
        return n == 4 ? 0.12 : 0.0;
    }

    public static double incoming(int n) {
        return n == 0 || n == 5 ? 0.9 : 1.0;
    }

    public static double healing(int n) {
        return n == 3 ? 0.75 : 1.0;
    }

    public static double playerOutgoing(int n, boolean bl, double d, boolean bl2) {
        if (n == 2 && bl && Double.isFinite(d) && d >= 6.0) {
            return 1.22;
        }
        return n == 7 && !bl && bl2 ? 1.2 : 1.0;
    }

    public static float leech(float f, float f2) {
        if (!Float.isFinite(f) || !Float.isFinite(f2) || f <= 0.0f || f2 <= 0.0f) {
            return 0.0f;
        }
        return (float)Math.min(2.0, (double)Math.min(f, f2) * 0.08);
    }

    public static float prayer(float f, float f2, boolean bl) {
        if (!Float.isFinite(f) || !Float.isFinite(f2) || f2 <= 0.0f || f <= 0.0f || f >= f2 * 0.7f) {
            return 0.0f;
        }
        return bl ? Math.min(1.0f, f2 * 0.01f) : f2 * 0.01f;
    }
}

