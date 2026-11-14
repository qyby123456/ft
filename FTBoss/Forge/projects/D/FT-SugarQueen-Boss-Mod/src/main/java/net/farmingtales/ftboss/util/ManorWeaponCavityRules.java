package net.farmingtales.ftboss.util;

import java.util.Set;

public final class ManorWeaponCavityRules {
    public static final int MAX_LEVEL = 5;
    public static final Set<String> WEAPONS = Set.of(
            "ftboss:dandan_scythe", "ftboss:black_cleaver",
            "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula",
            "tridents_n_stuff:stormseeker", "tridents_n_stuff:the_desecrator",
            "tridents_n_stuff:echo_of_the_deep", "tridents_n_stuff:stellaris",
            "tridents_n_stuff:sanguine_bond",
            "ftboss:qingyi_confluence_trident", "ftboss:shadow_sword", "ftboss:puffer_bat",
            "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin", "ftboss:qizai_prayer_staff");

    private ManorWeaponCavityRules() {}

    public static int unlockLevel(int index) {
        if (index < 0 || index >= SwordCavityRules.CELL_COUNT) return -1;
        if (index == GourmetBladeCavityRules.CORE) return 0;
        int rank = index < GourmetBladeCavityRules.CORE ? index + 1 : index;
        return (rank * MAX_LEVEL + GourmetBladeCavityRules.ACCESSORIES - 1) / GourmetBladeCavityRules.ACCESSORIES;
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
