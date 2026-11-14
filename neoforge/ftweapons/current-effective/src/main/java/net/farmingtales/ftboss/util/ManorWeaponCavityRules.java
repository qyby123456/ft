package net.farmingtales.ftboss.util;

import java.util.Set;

public final class ManorWeaponCavityRules {
    public static final int MAX_LEVEL = 5;
    public static final Set<String> WEAPONS = Set.of(
        "ftboss:dandan_scythe",
        "ftboss:black_cleaver",
        "ftboss:lemoncat_skillet",
        "ftboss:lemoncat_spatula",
        "tridents_n_stuff:stormseeker",
        "tridents_n_stuff:the_desecrator",
        "tridents_n_stuff:echo_of_the_deep",
        "tridents_n_stuff:stellaris",
        "tridents_n_stuff:sanguine_bond",
        "ftboss:qingyi_confluence_trident",
        "ftboss:shadow_sword",
        "ftboss:puffer_bat",
        "ftboss:xueli_skillet",
        "ftboss:xueli_rolling_pin",
        "ftboss:qizai_prayer_staff"
    );

    private ManorWeaponCavityRules() {
    }

    public static int unlockLevel(int var0) {
        if (var0 < 0 || var0 >= 27) {
            return -1;
        } else if (var0 == GourmetBladeCavityRules.CORE) {
            return 0;
        } else {
            int var1 = var0 < GourmetBladeCavityRules.CORE ? var0 + 1 : var0;
            return (var1 * 5 + 26 - 1) / 26;
        }
    }

    public static boolean unlocked(int var0, int var1) {
        int var2 = unlockLevel(var0);
        return var2 >= 0 && var2 <= Math.max(0, Math.min(5, var1));
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
