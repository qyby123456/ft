package net.farmingtales.ftboss.turret;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ZhuniPairCombat;
import net.farmingtales.ftboss.boss.ai.ZhuniPairRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ZhuniTurretDrops {
    private final Map<UUID, Long> nextDrops = new HashMap<>();
    private long firstDrop;
    private Vec3 placement;
    private UUID placingFor;
    private long deadline;
    private long returnUntil;
    private int building;

    public void revealedAt(long now) {
        firstDrop = now + ManorExpansionRules.TURRET_COOLDOWN;
        nextDrops.clear();
        placement = null;
        placingFor = null;
        building = 0;
        returnUntil = now;
    }

    public Vec3 preferred(EasterBossEntity boss, ZhuniEntity bird, ServerLevel level, Vec3 home) {
        long now = level.getGameTime();
        var players = ManorBalanceHooks.players(boss, level);
        var present = new HashSet<UUID>();
        for (ServerPlayer player : players) present.add(player.getUUID());
        nextDrops.keySet().retainAll(present);
        if (placement != null) {
            if (!present.contains(placingFor) || now >= deadline) {
                nextDrops.put(placingFor, now + 20);
                placement = null;
                returnUntil = now + ZhuniPairRules.RETURN_TICKS;
                return home;
            }
            Vec3 destination = placement.add(0, 0.5, 0);
            if (bird.distanceToSqr(destination) <= 0.75 * 0.75 && ++building >= ZhuniPairRules.BUILD_TICKS) {
                int count = ZhuniPairRules.summons(ZhuniPairCombat.xuanlanDefeated(boss), 1);
                boolean dropped = false;
                for (int index = 0; index < count; index++) {
                    Vec3 position = index == 0 ? placement : location(boss, level, placement, boss.getYRot());
                    if (position != null) dropped |= place(boss, level, position);
                }
                nextDrops.put(placingFor, now + (dropped ? ManorExpansionRules.TURRET_COOLDOWN : 20));
                placement = null;
                returnUntil = now + ZhuniPairRules.RETURN_TICKS;
                return home;
            }
            return destination;
        }
        if (now < firstDrop || now < returnUntil) return home;
        for (ServerPlayer player : players) {
            if (now < nextDrops.getOrDefault(player.getUUID(), firstDrop)) continue;
            placement = location(boss, level, player.position(), player.getYRot());
            if (placement == null) {
                nextDrops.put(player.getUUID(), now + 20);
                continue;
            }
            placingFor = player.getUUID();
            building = 0;
            deadline = now + ZhuniPairRules.TRAVEL_TIMEOUT;
            return placement.add(0, 0.5, 0);
        }
        return home;
    }

    private static Vec3 location(EasterBossEntity boss, ServerLevel level, Vec3 center, float rotation) {
        for (int ring = 0; ring < 3; ring++) {
            double radius = 3.0 + ring * 1.5;
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = attempt * Math.PI / 4.0 + rotation * Math.PI / 180.0;
                BlockPos floor = null;
                for (int down = 0; down < 48; down++) {
                    BlockPos candidate = BlockPos.containing(center.x + Math.cos(angle) * radius,
                            center.y - 1 - down, center.z + Math.sin(angle) * radius);
                    if (!level.hasChunkAt(candidate)) break;
                    if (level.getBlockState(candidate).isSolidRender(level, candidate)) {
                        floor = candidate;
                        break;
                    }
                }
                if (floor == null) continue;
                Vec3 position = new Vec3(floor.getX() + 0.5, floor.getY() + 1.0, floor.getZ() + 0.5);
                XuanlanTurretEntity turret = candidate(boss, level, position);
                if (valid(level, turret)) return position;
            }
        }
        return null;
    }

    private static XuanlanTurretEntity candidate(EasterBossEntity boss, ServerLevel level, Vec3 position) {
        XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, level);
        turret.setOwner(boss);
        turret.moveTo(position.x, position.y, position.z, boss.getYRot(), 0);
        return turret;
    }

    private static boolean valid(ServerLevel level, XuanlanTurretEntity turret) {
        return level.getWorldBorder().isWithinBounds(turret.blockPosition()) && level.noCollision(turret)
                && level.getEntitiesOfClass(XuanlanTurretEntity.class, turret.getBoundingBox().inflate(0.5)).isEmpty();
    }

    private static boolean place(EasterBossEntity boss, ServerLevel level, Vec3 position) {
        XuanlanTurretEntity turret = candidate(boss, level, position);
        if (!valid(level, turret) || !inEncounter(boss, turret)) return false;
        ManorFightMembers.inherit(boss, turret);
        return level.addFreshEntity(turret);
    }

    public static void reinforce(EasterBossEntity boss, ServerLevel level) {
        var existing = new java.util.ArrayList<XuanlanTurretEntity>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof XuanlanTurretEntity turret && turret.isAlive() && !turret.isRemoved()
                    && boss.getUUID().equals(ManorFightMembers.ownerId(turret))) existing.add(turret);
        }
        for (XuanlanTurretEntity turret : existing) {
            Vec3 position = location(boss, level, turret.position(), turret.getYRot());
            if (position != null) place(boss, level, position);
        }
    }

    public static boolean inEncounter(EasterBossEntity boss, Entity turret) {
        UUID fight = ManorFightMembers.fightId(boss);
        if (fight != null && !ManorFight.hasActive(fight)) return false;
        Vec3 anchor = fight == null ? null : ManorFight.anchorOf(fight);
        return anchor == null ? boss.distanceToSqr(turret) <= 96.0 * 96.0 : turret.distanceToSqr(anchor) <= 160.0 * 160.0;
    }
}
