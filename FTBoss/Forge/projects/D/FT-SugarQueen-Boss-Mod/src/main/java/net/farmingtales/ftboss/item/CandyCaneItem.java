package net.farmingtales.ftboss.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/**
 * Candy Cane (2026-09-12): weapon drop of the Gingerbread King boss.
 * Diamond-tier base with slightly better reach-friendly speed; visually a striped candy cane.
 */
public class CandyCaneItem extends SwordItem {

    public CandyCaneItem(Properties props) {
        super(Tiers.DIAMOND, 3, -2.2f, props);
    }
}
