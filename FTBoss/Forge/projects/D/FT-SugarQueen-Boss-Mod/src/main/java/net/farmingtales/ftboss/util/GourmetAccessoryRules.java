package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;

public final class GourmetAccessoryRules {
    public static final double STRENGTH = 2.0;

    private GourmetAccessoryRules() {}

    public static void scalePowers(double[] powers, boolean gourmet) {
        if (!gourmet) return;
        for (int index = 0; index < powers.length; index++) powers[index] *= STRENGTH;
    }

    public static Build scale(Build build) {
        if (build.damage() == 0) return build;
        return new Build(1 + (build.durability() - 1) * STRENGTH, 1 + (build.wear() - 1) * STRENGTH,
                1 + (build.damage() - 1) * STRENGTH, 1 + (build.speed() - 1) * STRENGTH, build.reach() * STRENGTH);
    }

    public static Bonus scale(Bonus bonus) {
        return new Bonus(bonus.spellPower() * STRENGTH, bonus.maxMana() * STRENGTH,
                bonus.attackDamage() * STRENGTH, bonus.armor() * STRENGTH, bonus.maxHealth() * STRENGTH);
    }
}
