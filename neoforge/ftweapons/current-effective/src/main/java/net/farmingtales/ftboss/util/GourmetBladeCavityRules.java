package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SwordCavityRules.Slot;

public final class GourmetBladeCavityRules {
    public static final int CORE = Slot.CORE.ordinal();
    public static final int MAX_LEVEL = 8;
    public static final int ACCESSORIES = 26;

    private GourmetBladeCavityRules() {
    }

    public static int unlockLevel(int var0) {
        if (var0 < 0 || var0 >= 27) {
            return -1;
        } else if (var0 == CORE) {
            return 0;
        } else {
            int var1 = var0 < CORE ? var0 + 1 : var0;
            return (var1 * 8 + 26 - 1) / 26;
        }
    }

    public static boolean unlocked(int var0, int var1) {
        int var2 = unlockLevel(var0);
        return var2 >= 0 && var2 <= Math.max(0, Math.min(8, var1));
    }

    public static int capacity(int var0) {
        int var1 = 0;

        for (int var2 = 0; var2 < 27; var2++) {
            if (unlocked(var2, var0)) {
                var1++;
            }
        }

        return var1;
    }
}
