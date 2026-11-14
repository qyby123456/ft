package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.SwordCavityRules.State;

public final class WeaponAccessoryLinkRules {
    private WeaponAccessoryLinkRules() {
    }

    public static List<Link> links(List<Material> var0, boolean var1) {
        if (!var1) {
            return List.of();
        } else {
            ArrayList<Cell> var2 = new ArrayList<>(Collections.nCopies(27, new Cell(State.EMPTY, Material.ORIGINAL)));
            int var3 = 0;

            for (Material var5 : var0) {
                if (var5 != null && var5 != Material.ORIGINAL && var3 < var2.size()) {
                    var2.set(var3++, new Cell(State.INSTALLED, var5));
                }
            }

            return SwordCavityRules.links(var2);
        }
    }

    public static Build apply(Build var0, List<Material> var1, boolean var2) {
        double var3 = var0.durability();
        double var5 = var0.wear();
        double var7 = var0.damage();
        double var9 = var0.speed();
        double var11 = var0.reach();

        for (Link var14 : links(var1, var2)) {
            var3 += var14.durabilityBonus;
            var5 += var14.wearBonus;
            var7 += var14.damageBonus;
            var9 += var14.speedBonus;
            var11 += var14.reachBonus;
        }

        return new Build(Math.min(3.0, var3), Math.max(0.35, var5), Math.min(2.0, var7), Math.min(1.5, var9), Math.min(0.75, var11));
    }
}
