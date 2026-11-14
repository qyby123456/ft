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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ZhuniTurretDrops {
    private final Map<UUID, Long> nextDrops = new HashMap<>();
    private long firstDrop;

    public void revealedAt(long now) {
        this.firstDrop = now + 100L;
        this.nextDrops.clear();
    }

    public void tick(EasterBossEntity boss, ServerLevel level) {
        long now = level.getGameTime();
        if (now >= this.firstDrop) {
            List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
            HashSet<UUID> present = new HashSet<>();

            for (ServerPlayer player : players) {
                present.add(player.getUUID());
            }

            this.nextDrops.keySet().retainAll(present);

            for (ServerPlayer player : players) {
                if (now >= this.nextDrops.getOrDefault(player.getUUID(), this.firstDrop)) {
                    boolean dropped = players.size() > 1 ? beside(boss, player, level) : ManorExpansionHooks.dropTurret(boss, level);
                    this.nextDrops.put(player.getUUID(), now + (long)(dropped ? 100 : 20));
                }
            }
        }
    }

    private static boolean beside(EasterBossEntity boss, ServerPlayer player, ServerLevel level) {
        for (int ring = 0; ring < 3; ring++) {
            double radius = 3.0 + (double)ring * 1.5;

            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = (double)attempt * Math.PI / 4.0 + (double)player.getYRot() * Math.PI / 180.0;
                BlockPos floor = null;

                for (int down = 0; down < 48; down++) {
                    BlockPos candidate = BlockPos.containing(
                        player.getX() + Math.cos(angle) * radius, player.getY() - 1.0 - (double)down, player.getZ() + Math.sin(angle) * radius
                    );
                    if (!level.hasChunkAt(candidate)) {
                        break;
                    }

                    if (level.getBlockState(candidate).isSolidRender(level, candidate)) {
                        floor = candidate;
                        break;
                    }
                }

                if (floor != null) {
                    XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, level);
                    turret.setOwner(boss);
                    turret.moveTo((double)floor.getX() + 0.5, (double)floor.getY() + 1.0, (double)floor.getZ() + 0.5, boss.getYRot(), 0.0F);
                    ManorFightMembers.inherit(boss, turret);
                    if (level.noCollision(turret) && level.addFreshEntity(turret)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean inEncounter(EasterBossEntity boss, Entity turret) {
        UUID fight = ManorFightMembers.fightId(boss);
        if (fight != null && !ManorFight.hasActive(fight)) {
            return false;
        } else {
            Vec3 anchor = fight == null ? null : ManorFight.anchorOf(fight);
            return anchor == null ? boss.distanceToSqr(turret) <= 9216.0 : turret.distanceToSqr(anchor) <= 25600.0;
        }
    }
}
