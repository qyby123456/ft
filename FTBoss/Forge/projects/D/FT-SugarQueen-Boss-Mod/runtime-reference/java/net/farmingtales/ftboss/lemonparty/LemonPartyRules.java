/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.lemonparty;

import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;

public final class LemonPartyRules {
    private LemonPartyRules() {
    }

    public static boolean multiplayer(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 4 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && ManorMultiplayerBalance.isManagedManorBoss((LivingEntity)easterBossEntity) && easterBossEntity.getPersistentData().m_128451_("ftboss_manor_player_count") >= 2;
    }

    public static int absorbMinimum(EasterBossEntity easterBossEntity) {
        return LemonPartyRules.multiplayer(easterBossEntity) ? 3 : 2;
    }

    public static int helperLimit(EasterBossEntity easterBossEntity) {
        return LemonPartyRules.multiplayer(easterBossEntity) ? 5 : 3;
    }

    public static int summonCount(EasterBossEntity easterBossEntity, int n) {
        int n2 = n + (LemonPartyRules.multiplayer(easterBossEntity) ? 1 : 0);
        if (n != 1) {
            return n2;
        }
        int n3 = LemonPartyRules.helperLimit(easterBossEntity) - easterBossEntity.lemonSacrifice().defeatedPostSacrificeHelpers();
        return Math.max(0, Math.min(n2, n3));
    }
}

