package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.SwordCavityRules.State;

/** 通用配件羁绊；2026-10-22 四武器沿用剑体组合规则，仅接受调用方已验证的付费解锁材料。 */
public final class WeaponAccessoryLinkRules {
    private WeaponAccessoryLinkRules() {}

    public static List<Link> links(List<Material> paid, boolean ready) {
        if (!ready) return List.of();
        List<Cell> cells = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, new Cell(State.EMPTY, Material.ORIGINAL)));
        int index = 0;
        for (Material material : paid) {
            if (material != null && material != Material.ORIGINAL && index < cells.size()) cells.set(index++, new Cell(State.INSTALLED, material));
        }
        return SwordCavityRules.links(cells);
    }

    public static Build apply(Build base, List<Material> paid, boolean ready) {
        double durability = base.durability();
        double wear = base.wear();
        double damage = base.damage();
        double speed = base.speed();
        double reach = base.reach();
        for (Link link : links(paid, ready)) {
            durability += link.durabilityBonus;
            wear += link.wearBonus;
            damage += link.damageBonus;
            speed += link.speedBonus;
            reach += link.reachBonus;
        }
        return new Build(Math.min(3.0, durability), Math.max(0.35, wear), Math.min(2.0, damage),
                Math.min(1.5, speed), Math.min(0.75, reach));
    }
}
