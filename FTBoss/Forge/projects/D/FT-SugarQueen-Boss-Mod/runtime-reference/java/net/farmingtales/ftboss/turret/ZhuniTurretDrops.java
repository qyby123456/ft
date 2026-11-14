/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.turret;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ZhuniTurretDrops {
    private final Map<UUID, Long> nextDrops = new HashMap<UUID, Long>();
    private long firstDrop;

    public void revealedAt(long now) {
        this.firstDrop = now + 100L;
        this.nextDrops.clear();
    }

    public void tick(EasterBossEntity boss, ServerLevel level) {
        long now = level.m_46467_();
        if (now < this.firstDrop) {
            return;
        }
        List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
        HashSet<UUID> present = new HashSet<UUID>();
        for (ServerPlayer player : players) {
            present.add(player.m_20148_());
        }
        this.nextDrops.keySet().retainAll(present);
        for (ServerPlayer player : players) {
            if (now < this.nextDrops.getOrDefault(player.m_20148_(), this.firstDrop)) continue;
            boolean dropped = players.size() > 1 ? ZhuniTurretDrops.beside(boss, player, level) : ManorExpansionHooks.dropTurret(boss, level);
            this.nextDrops.put(player.m_20148_(), now + (long)(dropped ? 100 : 20));
        }
    }

    private static boolean beside(EasterBossEntity boss, ServerPlayer player, ServerLevel level) {
        for (int ring = 0; ring < 3; ++ring) {
            double radius = 3.0 + (double)ring * 1.5;
            for (int attempt = 0; attempt < 8; ++attempt) {
                BlockPos candidate;
                double angle = (double)attempt * Math.PI / 4.0 + (double)player.m_146908_() * Math.PI / 180.0;
                BlockPos floor = null;
                for (int down = 0; down < 48 && level.m_46805_(candidate = BlockPos.m_274561_((double)(player.m_20185_() + Math.cos(angle) * radius), (double)(player.m_20186_() - 1.0 - (double)down), (double)(player.m_20189_() + Math.sin(angle) * radius))); ++down) {
                    if (!level.m_8055_(candidate).m_60804_((BlockGetter)level, candidate)) continue;
                    floor = candidate;
                    break;
                }
                if (floor == null) continue;
                XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, (Level)level);
                turret.setOwner(boss);
                turret.m_7678_((double)floor.m_123341_() + 0.5, (double)floor.m_123342_() + 1.0, (double)floor.m_123343_() + 0.5, boss.m_146908_(), 0.0f);
                ManorFightMembers.inherit((Entity)boss, (Entity)turret);
                if (!level.m_45786_((Entity)turret) || !level.m_7967_((Entity)turret)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean inEncounter(EasterBossEntity boss, Entity turret) {
        Vec3 anchor;
        UUID fight = ManorFightMembers.fightId((Entity)boss);
        if (fight != null && !ManorFight.hasActive(fight)) {
            return false;
        }
        Vec3 vec3 = anchor = fight == null ? null : ManorFight.anchorOf(fight);
        return anchor == null ? boss.m_20280_(turret) <= 9216.0 : turret.m_20238_(anchor) <= 25600.0;
    }
}

