/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class FightState {
    public static final double ENGAGE_RANGE = 64.0;

    private FightState() {
    }

    public static boolean inFight(ServerPlayer p) {
        if (p == null || !p.m_6084_()) {
            return false;
        }
        if (ManorFight.isPlayerFighting((Player)p)) {
            return true;
        }
        Level level = p.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        for (LivingEntity le : sl.m_45976_(LivingEntity.class, p.m_20191_().m_82400_(64.0))) {
            Mob m;
            if (!FightState.isBoss((Entity)le) || !(le instanceof Mob) || (m = (Mob)le).m_5448_() != p) continue;
            return true;
        }
        return false;
    }

    public static boolean isEngagedWith(LivingEntity boss, Player p) {
        Mob m;
        return boss instanceof Mob && (m = (Mob)boss).m_5448_() == p && FightState.isBoss((Entity)boss);
    }

    public static boolean isBoss(Entity e) {
        EasterBossEntity eb;
        if (e == null || !e.m_6084_() || e.m_213877_()) {
            return false;
        }
        if (e instanceof EasterBossEntity && ((eb = (EasterBossEntity)e).getVariant() == 9 || eb.isMini())) {
            return false;
        }
        ResourceLocation key = EntityType.m_20613_((EntityType)e.m_6095_());
        return key != null && "ftboss".equals(key.m_135827_());
    }
}

