package net.farmingtales.ftboss.client.render;

/** 庄园切换纯规则：60 tick交接、90 tick寿命与分档预算；2026-10-25独立重做。 */

public final class ManorTransitionRules {
    public static final int ARRIVAL_TICK = 60;
    public static final int DURATION_TICKS = 90;
    public static final int MAX_ACTIVE = 4;
    public static final int FRAME_VERTEX_BUDGET = 16384;
    public static final int FULL_VERTEX_BUDGET = 8192;
    public static final int REDUCED_VERTEX_BUDGET = 4096;
    public static final int MINIMAL_VERTEX_BUDGET = 2048;
    public static final double RADIUS = 13.5;
    public static final double HEIGHT = 11.0;
    public static final double CULL_DISTANCE = 128.0;

    private ManorTransitionRules() {}

    public static boolean valid(int variant, double age) {
        return variant >= -1 && variant < 12 && variant != 9 && Double.isFinite(age) && age >= 0 && age < DURATION_TICKS;
    }

    public static double smooth(double amount) {
        double bounded = Math.max(0, Math.min(1, amount));
        return bounded * bounded * (3 - 2 * bounded);
    }

    public static double opacity(double age) {
        return smooth(age / 8.0) * (1 - smooth((age - 75) / 15.0));
    }

    public static double domain(double age) {
        return 0.6 + 10.8 * smooth(age / 30.0);
    }

    public static int detail(double distanceSquared, int particleSetting) {
        if (particleSetting >= 2 || distanceSquared > 80 * 80) return 2;
        if (particleSetting >= 1 || distanceSquared > 48 * 48) return 1;
        return 0;
    }

    public static int budget(int detail) {
        return detail <= 0 ? FULL_VERTEX_BUDGET : detail == 1 ? REDUCED_VERTEX_BUDGET : MINIMAL_VERTEX_BUDGET;
    }

    public static double correctedAge(double current, int previousPacketAge, int incomingAge) {
        if (incomingAge < previousPacketAge && incomingAge < ARRIVAL_TICK && current >= ARRIVAL_TICK) return incomingAge;
        return Math.max(current, incomingAge);
    }

    public static int accent(int variant) {
        return switch (variant) {
            case 0, 5 -> 0xf2a4b6;
            case 1, 6 -> 0x9edcc4;
            case 2, 7 -> 0x9ccce8;
            case 3, 8 -> 0xcbb5ee;
            case 4, 10 -> 0xf1b879;
            default -> 0xe6d7a4;
        };
    }
}
