package net.farmingtales.ftboss.util;

/** 食武损耗与修复纯规则；每四分之一耐久触发一次，2026-10-13 新增且不依赖注册或事件。 */
public final class FoodWeaponLifecycleRules {
    public static final int WEAR_STEPS = 4;
    public static final int REPAIR_TASKS = 3;

    private FoodWeaponLifecycleRules() {}

    public static int wearStep(int damage, int maximum) {
        if (maximum <= 0) return 0;
        long clamped = Math.max(0L, Math.min((long) maximum, damage));
        return (int) (clamped * WEAR_STEPS / maximum);
    }

    public static int pendingLosses(int previous, int damage, int maximum) {
        return Math.max(0, wearStep(damage, maximum) - Math.max(0, Math.min(WEAR_STEPS, previous)));
    }

    public static int preserveWear(int damage, int oldMaximum, int newMaximum) {
        if (oldMaximum <= 0 || newMaximum <= 0) return Math.max(0, damage);
        long clamped = Math.max(0L, Math.min((long) oldMaximum, damage));
        int limit = clamped == oldMaximum ? newMaximum : newMaximum - 1;
        return (int) Math.min(limit, (clamped * newMaximum + oldMaximum - 1L) / oldMaximum);
    }

    public static int repairRequired(int level, int task) {
        int stage = Math.max(0, Math.min(8, level));
        return switch (task) {
            case 0 -> 2 + stage;
            case 1 -> 2 + (stage + 1) / 2;
            case 2 -> 1 + stage;
            default -> 0;
        };
    }

    public static boolean ready(int[] progress, int level) {
        if (progress == null || progress.length != REPAIR_TASKS) return false;
        for (int task = 0; task < REPAIR_TASKS; task++) {
            if (progress[task] != repairRequired(level, task)) return false;
        }
        return true;
    }
}
