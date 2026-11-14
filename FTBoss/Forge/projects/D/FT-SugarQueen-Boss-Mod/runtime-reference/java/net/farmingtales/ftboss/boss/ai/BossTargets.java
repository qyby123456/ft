/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class BossTargets {
    private BossTargets() {
    }

    public static boolean valid(Player p) {
        return p != null && p.m_6084_() && !p.m_213877_() && !p.m_7500_() && !p.m_5833_();
    }

    public static boolean valid(LivingEntity e) {
        Player p;
        return e != null && e.m_6084_() && !e.m_213877_() && (!(e instanceof Player) || BossTargets.valid(p = (Player)e));
    }

    public static LivingEntity combatTarget(Mob boss, double fallbackRange) {
        ServerPlayer serverPlayer;
        LivingEntity target = BossAggro.highest((LivingEntity)boss);
        if (target != null) {
            return target;
        }
        Level level = boss.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            serverPlayer = BossTargets.nearest(level2, (Entity)boss, fallbackRange);
        } else {
            serverPlayer = null;
        }
        return serverPlayer;
    }

    public static ServerPlayer nearest(ServerLevel sl, double x, double y, double z, double range) {
        if (sl == null) {
            return null;
        }
        ServerPlayer best = null;
        double bd = range * range;
        AABB box = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, box)) {
            double d;
            if (!BossTargets.valid((Player)p) || !((d = p.m_20275_(x, y, z)) < bd)) continue;
            bd = d;
            best = p;
        }
        return best;
    }

    public static ServerPlayer nearest(ServerLevel sl, Entity from, double range) {
        return from == null ? null : BossTargets.nearest(sl, from.m_20185_(), from.m_20186_(), from.m_20189_(), range);
    }

    public static void dropInvalidTarget(LivingEntity boss) {
        if (!(boss instanceof Mob)) {
            return;
        }
        Mob m = (Mob)boss;
        LivingEntity t = m.m_5448_();
        if (!(t == null || BossTargets.valid(t) && t.m_9236_() == boss.m_9236_())) {
            m.m_6710_(null);
            m.m_21573_().m_26573_();
        }
    }
}

