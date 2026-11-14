package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultVisuals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ManorMultiplayerBalance {
    public static final double FIGHT_RADIUS = 96.0;
    public static final int MAX_SCALING_PLAYERS = 6;
    public static final double BOSS_HEALTH_PER_EXTRA_PLAYER = 0.1;
    public static final double SPARROW_HEALTH_PER_EXTRA_PLAYER = 0.06;
    public static final int RANGED_WINDUP_TICKS = 20;
    public static final int RANGED_COOLDOWN_TICKS = 140;
    public static final double RANGED_MAX_DISTANCE = 72.0;
    public static final double RANGED_AREA_RADIUS = 3.0;
    public static final double EDGE_PRESSURE_RADIUS = 72.0;
    public static final int EDGE_UNTARGETED_TICKS = 120;
    private static final String BASE_HP = "ftboss_base_hp";
    private static final String BASE_ATTACK = "ftboss_base_atk";
    private static final String DAMAGE_MULTIPLIER = "ftboss_dmg_mul";
    private static final String PLAYER_COUNT = "ftboss_manor_player_count";
    private static final String SHOT_DUE = "ftboss_manor_shot_due";
    private static final String SHOT_NEXT = "ftboss_manor_shot_next";
    private static final String SHOT_TARGET = "ftboss_manor_shot_target";
    private static final String SHOT_X = "ftboss_manor_shot_x";
    private static final String SHOT_Y = "ftboss_manor_shot_y";
    private static final String SHOT_Z = "ftboss_manor_shot_z";
    private static final String EDGE_TARGET = "ftboss_manor_edge_target";
    private static final String EDGE_SINCE = "ftboss_manor_edge_since";

    private ManorMultiplayerBalance() {
    }

    public static boolean tick(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss)) {
            return false;
        } else {
            UUID fightId = activeFightId(boss);
            if (fightId == null) {
                return false;
            } else if (!boss.level().isClientSide && !boss.isDeadOrDying()) {
                Vec3 anchor = ManorFight.anchorOf(fightId);
                if (anchor != null && boss.level() instanceof ServerLevel) {
                    ServerLevel level = (ServerLevel)boss.level();
                    List<ServerPlayer> players = playersInArena(level, anchor);
                    int playerCount = Math.max(1, players.size());
                    int scalingPlayers = Math.min(6, playerCount);
                    boolean sparrow = boss.getVariant() == 9;
                    boolean combatBoss = isCombatBoss(boss);
                    double healthMultiplier = sparrow
                        ? 1.0 + 0.06 * (double)(scalingPlayers - 1)
                        : (combatBoss ? 1.0 + 0.1 * (double)(scalingPlayers - 1) : 1.0);
                    applyHealthAndRestoreDamage(boss, healthMultiplier, playerCount);
                    if (combatBoss) {
                        updateIndependentAggro(boss, players);
                        tickIndependentRangedAttack(level, boss, players, anchor);
                    } else {
                        clearShot(boss.getPersistentData());
                    }

                    return true;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    public static boolean isManagedManorBoss(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss)) {
            return false;
        } else {
            UUID fightId = activeFightId(boss);
            return fightId != null && isCombatBoss(boss);
        }
    }

    public static boolean validManorTarget(LivingEntity entity, LivingEntity target) {
        if (!ManorCombatContinuity.present(entity, target)) {
            return false;
        } else if (ManorFinaleCombat.manages(entity)) {
            return ManorFinaleCombat.validTarget(entity, target);
        } else if (isManagedManorBoss(entity) && target instanceof ServerPlayer player) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.level() == entity.level()) {
                UUID fightId = ManorFightMembers.fightId(entity);
                Vec3 anchor = ManorFight.anchorOf(fightId);
                return anchor != null && player.distanceToSqr(anchor.x, anchor.y, anchor.z) <= 9216.0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static UUID activeFightId(EasterBossEntity boss) {
        int variant = boss.getVariant();
        if (variant != 9 && !EasterBossEntity.isManorVariant(variant)) {
            return null;
        } else {
            UUID fightId = ManorFightMembers.fightId(boss);
            return fightId != null && ManorFight.hasActive(fightId) ? fightId : null;
        }
    }

    private static boolean isCombatBoss(EasterBossEntity boss) {
        return !boss.isMini() && boss.getVariant() != 9 && EasterBossEntity.isManorVariant(boss.getVariant()) && !boss.isManorIntroHidden();
    }

    private static List<ServerPlayer> playersInArena(ServerLevel level, Vec3 anchor) {
        List<ServerPlayer> result = new ArrayList<>();
        double radiusSqr = 9216.0;

        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.distanceToSqr(anchor.x, anchor.y, anchor.z) <= radiusSqr) {
                result.add(player);
            }
        }

        result.sort(Comparator.comparing(playerx -> playerx.getUUID().toString()));
        return result;
    }

    private static void applyHealthAndRestoreDamage(EasterBossEntity boss, double multiplier, int playerCount) {
        CompoundTag tag = boss.getPersistentData();
        AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
        if (!tag.contains("ftboss_base_hp")) {
            tag.putDouble("ftboss_base_hp", health != null ? health.getBaseValue() : (double)boss.getMaxHealth());
        }

        if (!tag.contains("ftboss_base_atk")) {
            AttributeInstance attack = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            tag.putDouble("ftboss_base_atk", attack != null ? attack.getBaseValue() : 0.0);
        }

        double baseHealth = tag.getDouble("ftboss_base_hp");
        if (!Double.isFinite(baseHealth) || baseHealth <= 0.0) {
            baseHealth = health != null ? health.getBaseValue() : (double)boss.getMaxHealth();
            tag.putDouble("ftboss_base_hp", baseHealth);
        }

        double targetHealth = baseHealth * multiplier;
        if (baseHealth <= 1024.0) {
            targetHealth = Math.min(1024.0, targetHealth);
        }

        if (health != null && Math.abs(health.getBaseValue() - targetHealth) > 0.01) {
            float ratio = boss.getMaxHealth() > 0.0F ? boss.getHealth() / boss.getMaxHealth() : 1.0F;
            health.setBaseValue(targetHealth);
            float newMax = boss.getMaxHealth();
            boss.setHealth(Math.max(1.0F, Math.min(newMax, ratio * newMax)));
        }

        tag.putDouble("ftboss_dmg_mul", 1.0);
        tag.putInt("ftboss_manor_player_count", playerCount);
        AttributeInstance attack = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        double baseAttack = tag.getDouble("ftboss_base_atk");
        if (attack != null && Double.isFinite(baseAttack) && baseAttack >= 0.0 && Math.abs(attack.getBaseValue() - baseAttack) > 0.001) {
            attack.setBaseValue(baseAttack);
        }
    }

    private static void updateIndependentAggro(EasterBossEntity var0, List<ServerPlayer> var1) {
        ManorCombatContinuity.update(var0, var1);
    }

    private static void tickIndependentRangedAttack(ServerLevel level, EasterBossEntity boss, List<ServerPlayer> players, Vec3 anchor) {
        if (ManorFinaleCombat.manages(boss)) {
            ManorFinaleCombat.tickAssault(level, boss, players, anchor);
        } else {
            CompoundTag tag = boss.getPersistentData();
            long now = level.getGameTime();
            if (boss.isNoAi()) {
                clearShot(tag);
                clearEdgePressure(tag);
            } else {
                ServerPlayer edgeTarget = trackUntargetedEdgePlayer(boss, players, anchor, tag, now);
                boolean edgePressureReady = edgeTarget != null
                    && tag.contains("ftboss_manor_edge_since")
                    && now - tag.getLong("ftboss_manor_edge_since") >= 120L;
                if (tag.contains("ftboss_manor_shot_due") && now >= tag.getLong("ftboss_manor_shot_due")) {
                    fireStoredShot(level, boss, tag);
                }

                if (players.size() < 2 && !edgePressureReady) {
                    clearDueShot(tag);
                    tag.remove("ftboss_manor_shot_next");
                } else {
                    if (!tag.contains("ftboss_manor_shot_next")) {
                        tag.putLong("ftboss_manor_shot_next", edgePressureReady ? now : now + 60L + (long)Math.floorMod(boss.getUUID().hashCode(), 31));
                    }

                    if (!tag.contains("ftboss_manor_shot_due") && now >= tag.getLong("ftboss_manor_shot_next")) {
                        ServerPlayer target = edgePressureReady ? edgeTarget : chooseRangedTarget(boss, players, now);
                        if (target == null) {
                            tag.putLong("ftboss_manor_shot_next", now + 40L);
                        } else {
                            Vec3 field = groundAt(level, boss, target.position());
                            tag.putUUID("ftboss_manor_shot_target", target.getUUID());
                            tag.putDouble("ftboss_manor_shot_x", field.x);
                            tag.putDouble("ftboss_manor_shot_y", field.y);
                            tag.putDouble("ftboss_manor_shot_z", field.z);
                            tag.putLong("ftboss_manor_shot_due", now + 20L);
                            tag.putLong("ftboss_manor_shot_next", now + 140L + (long)Math.floorMod(boss.getUUID().hashCode(), 21));
                            if (edgePressureReady) {
                                clearEdgePressure(tag);
                            }

                            FTBossMod.LOGGER
                                .info(
                                    "[FTBoss][ManorAssault] charge boss={} variant={} target={} mode={} center=({},{},{})",
                                    boss.getName().getString(),
                                    boss.getVariant(),
                                    target.getScoreboardName(),
                                    edgePressureReady ? "edge" : "multiplayer",
                                    String.format("%.1f", field.x),
                                    String.format("%.1f", field.y),
                                    String.format("%.1f", field.z)
                                );
                            ManorAssaultVisuals.show(level, boss, field, false);
                            ParticleOptions particle = particleFor(boss.getVariant());
                            level.sendParticles(particle, target.getX(), target.getY() + 0.12, target.getZ(), 18, 1.0, 0.03, 1.0, 0.01);
                            level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.8F, 0.75F);
                        }
                    }
                }
            }
        }
    }

    private static ServerPlayer trackUntargetedEdgePlayer(EasterBossEntity boss, List<ServerPlayer> players, Vec3 anchor, CompoundTag tag, long now) {
        double edgeRadiusSqr = 5184.0;
        ServerPlayer tracked = null;
        if (tag.hasUUID("ftboss_manor_edge_target")) {
            UUID trackedId = tag.getUUID("ftboss_manor_edge_target");

            for (ServerPlayer player : players) {
                if (player.getUUID().equals(trackedId) && player.distanceToSqr(anchor.x, anchor.y, anchor.z) >= edgeRadiusSqr) {
                    tracked = player;
                    break;
                }
            }
        }

        if (tracked == null) {
            double farthest = edgeRadiusSqr;

            for (ServerPlayer playerx : players) {
                double distance = playerx.distanceToSqr(anchor.x, anchor.y, anchor.z);
                if (distance >= farthest) {
                    farthest = distance;
                    tracked = playerx;
                }
            }

            clearEdgePressure(tag);
            if (tracked != null) {
                tag.putUUID("ftboss_manor_edge_target", tracked.getUUID());
                tag.putLong("ftboss_manor_edge_since", now);
                FTBossMod.LOGGER
                    .info(
                        "[FTBoss][ManorAssault] edge-watch boss={} target={} anchorDistance={}",
                        boss.getName().getString(),
                        tracked.getScoreboardName(),
                        String.format("%.1f", Math.sqrt(tracked.distanceToSqr(anchor.x, anchor.y, anchor.z)))
                    );
            }
        }

        if (tracked == null) {
            clearEdgePressure(tag);
            return null;
        } else {
            if (!tag.contains("ftboss_manor_edge_since")) {
                tag.putLong("ftboss_manor_edge_since", now);
            }

            return tracked;
        }
    }

    private static void clearEdgePressure(CompoundTag tag) {
        tag.remove("ftboss_manor_edge_target");
        tag.remove("ftboss_manor_edge_since");
    }

    private static Vec3 groundAt(ServerLevel level, EasterBossEntity boss, Vec3 point) {
        BlockHitResult hit = level.clip(new ClipContext(point.add(0.0, 3.0, 0.0), point.add(0.0, -10.0, 0.0), Block.COLLIDER, Fluid.NONE, boss));
        return hit.getType() == Type.MISS ? point : hit.getLocation();
    }

    private static ServerPlayer chooseRangedTarget(EasterBossEntity boss, List<ServerPlayer> players, long now) {
        LivingEntity mainTarget = boss.getTarget();
        int start = Math.floorMod(boss.getUUID().hashCode() + (int)(now / 140L), players.size());
        double maxDistanceSqr = 5184.0;

        for (int pass = 0; pass < 2; pass++) {
            for (int offset = 0; offset < players.size(); offset++) {
                ServerPlayer candidate = players.get((start + offset) % players.size());
                if ((pass != 0 || candidate != mainTarget) && boss.distanceToSqr(candidate) <= maxDistanceSqr && boss.hasLineOfSight(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private static void fireStoredShot(ServerLevel level, EasterBossEntity boss, CompoundTag tag) {
        double x = tag.getDouble("ftboss_manor_shot_x");
        double y = tag.getDouble("ftboss_manor_shot_y");
        double z = tag.getDouble("ftboss_manor_shot_z");
        clearDueShot(tag);
        Vec3 field = new Vec3(x, y, z);
        ManorAssaultVisuals.show(level, boss, field, true);
        ParticleOptions particle = particleFor(boss.getVariant());
        level.sendParticles(particle, x, y + 0.2, z, 28, 1.4, 0.08, 1.4, 0.04);
        level.sendParticles(ParticleTypes.POOF, x, y + 0.25, z, 18, 1.2, 0.12, 1.2, 0.04);
        level.playSound(null, x, y, z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.9F, 0.85F);
        double radiusSqr = 9.0;
        float damage = boss.isImpossibleChallenge() ? 5.0F : 4.0F;
        int hits = 0;

        for (ServerPlayer player : level.players()) {
            if (validManorTarget(boss, player)) {
                double dx = player.getX() - x;
                double dy = player.getY() - y;
                double dz = player.getZ() - z;
                if (dx * dx + dy * dy + dz * dz <= radiusSqr && player.hurt(boss.damageSources().indirectMagic(boss, boss), damage)) {
                    hits++;
                }
            }
        }

        FTBossMod.LOGGER
            .info(
                "[FTBoss][ManorAssault] release boss={} variant={} hits={} center=({},{},{})",
                boss.getName().getString(),
                boss.getVariant(),
                hits,
                String.format("%.1f", x),
                String.format("%.1f", y),
                String.format("%.1f", z)
            );
    }

    private static ParticleOptions particleFor(int variant) {
        switch (variant) {
            case 0:
                return ParticleTypes.FLAME;
            case 1:
                return ParticleTypes.WITCH;
            case 2:
                return ParticleTypes.PORTAL;
            case 3:
                return ParticleTypes.SOUL_FIRE_FLAME;
            case 4:
                return ParticleTypes.COMPOSTER;
            case 5:
                return ParticleTypes.ELECTRIC_SPARK;
            case 6:
                return ParticleTypes.CRIT;
            case 7:
                return ParticleTypes.SMOKE;
            case 8:
                return ParticleTypes.END_ROD;
            case 9:
            default:
                return ParticleTypes.END_ROD;
            case 10:
                return ParticleTypes.SNOWFLAKE;
            case 11:
                return ParticleTypes.DRIPPING_HONEY;
        }
    }

    private static void clearShot(CompoundTag tag) {
        clearDueShot(tag);
        tag.remove("ftboss_manor_shot_next");
    }

    private static void clearDueShot(CompoundTag tag) {
        tag.remove("ftboss_manor_shot_due");
        tag.remove("ftboss_manor_shot_target");
        tag.remove("ftboss_manor_shot_x");
        tag.remove("ftboss_manor_shot_y");
        tag.remove("ftboss_manor_shot_z");
    }
}
