package net.farmingtales.ftboss.util;

public final class GourmetBladeCavityRules {
    public static final int CORE = SwordCavityRules.Slot.CORE.ordinal();
    public static final int MAX_LEVEL = 8;
    public static final int ACCESSORIES = SwordCavityRules.CELL_COUNT - 1;

    private GourmetBladeCavityRules() {}

    public static int unlockLevel(int index) {
        if (index < 0 || index >= SwordCavityRules.CELL_COUNT) return -1;
        if (index == CORE) return 0;
        int rank = index < CORE ? index + 1 : index;
        return (rank * MAX_LEVEL + ACCESSORIES - 1) / ACCESSORIES;
    }

    public static boolean unlocked(int index, int level) {
        int required = unlockLevel(index);
        return required >= 0 && required <= Math.max(0, Math.min(MAX_LEVEL, level));
    }

    public static int capacity(int level) {
        int result = 0;
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) if (unlocked(index, level)) result++;
        return result;
    }
}
