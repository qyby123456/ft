/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FoodWeaponLifecycleRules {
    public static final int WEAR_STEPS = 4;
    public static final int REPAIR_TASKS = 3;

    private FoodWeaponLifecycleRules() {
    }

    public static int wearStep(int n, int n2) {
        if (n2 <= 0) {
            return 0;
        }
        long l = Math.max(0L, Math.min((long)n2, (long)n));
        return (int)(l * 4L / (long)n2);
    }

    public static int pendingLosses(int n, int n2, int n3) {
        return Math.max(0, FoodWeaponLifecycleRules.wearStep(n2, n3) - Math.max(0, Math.min(4, n)));
    }

    public static int preserveWear(int n, int n2, int n3) {
        if (n2 <= 0 || n3 <= 0) {
            return Math.max(0, n);
        }
        long l = Math.max(0L, Math.min((long)n2, (long)n));
        int n4 = l == (long)n2 ? n3 : n3 - 1;
        return (int)Math.min((long)n4, (l * (long)n3 + (long)n2 - 1L) / (long)n2);
    }

    public static int repairRequired(int n, int n2) {
        int n3 = Math.max(0, Math.min(8, n));
        return switch (n2) {
            case 0 -> 2 + n3;
            case 1 -> 2 + (n3 + 1) / 2;
            case 2 -> 1 + n3;
            default -> 0;
        };
    }

    public static boolean ready(int[] nArray, int n) {
        if (nArray == null || nArray.length != 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            if (nArray[i] == FoodWeaponLifecycleRules.repairRequired(n, i)) continue;
            return false;
        }
        return true;
    }
}

