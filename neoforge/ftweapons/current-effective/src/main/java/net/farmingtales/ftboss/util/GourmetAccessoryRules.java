package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;

public final class GourmetAccessoryRules {
    public static final double STRENGTH = 2.0;

    private GourmetAccessoryRules() {
    }

    public static void scalePowers(double[] var0, boolean var1) {
        if (var1) {
            for (int var2 = 0; var2 < var0.length; var2++) {
                var0[var2] *= 2.0;
            }
        }
    }

    public static Build scale(Build var0) {
        return var0.damage() == 0.0
            ? var0
            : new Build(
                1.0 + (var0.durability() - 1.0) * 2.0,
                1.0 + (var0.wear() - 1.0) * 2.0,
                1.0 + (var0.damage() - 1.0) * 2.0,
                1.0 + (var0.speed() - 1.0) * 2.0,
                var0.reach() * 2.0
            );
    }

    public static Bonus scale(Bonus var0) {
        return new Bonus(var0.spellPower() * 2.0, var0.maxMana() * 2.0, var0.attackDamage() * 2.0, var0.armor() * 2.0, var0.maxHealth() * 2.0);
    }
}
