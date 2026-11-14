package net.farmingtales.ftboss.youzi;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.YouziMiniRules;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class YouziMiniBalance {
    private YouziMiniBalance() {}

    public static void restore(EasterBossEntity boss) {
        if (!boss.isMini() || boss.getVariant() != 0) return;
        var health = boss.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(YouziMiniRules.HEALTH);
        var armor = boss.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(YouziMiniRules.ARMOR);
        boss.setHealth(Math.min(boss.getHealth(), (float) YouziMiniRules.HEALTH));
    }
}
