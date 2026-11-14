package net.farmingtales.ftboss.util;

/** 食物跨类组合与临时能力时间边界；时间单位 tick，2026-10-12 新增腾空、友方狼与套装联动。 */
public final class FoodSocketPowerRules {
    public static final int FLIGHT_DURATION_TICKS = 60;
    public static final int FLIGHT_COOLDOWN_TICKS = 600;
    public static final int PACK_DURATION_TICKS = 600;
    public static final int PACK_COOLDOWN_TICKS = 400;
    public static final int PACK_SIZE = 2;

    private FoodSocketPowerRules() {}

    public static boolean flight(int fish, int wine) { return fish >= 2 && wine >= 1; }
    public static boolean pack(int meat, int cheese) { return meat >= 2 && cheese >= 1; }
    public static int setBonus(int setCount, int partnerCount) { return setCount >= 3 && partnerCount >= 1 ? 1 : 0; }
    public static boolean ready(long now, long deadline, int cooldown) {
        return deadline <= now || deadline > now && deadline - now > cooldown;
    }
    public static boolean active(long now, long deadline, int duration) {
        return deadline > now && deadline - now <= duration;
    }
}
