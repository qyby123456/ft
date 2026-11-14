package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultVisuals;
import net.farmingtales.ftboss.util.ManorFinaleRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ManorFinaleCombat {
    private static final String SHOT_DUE = "ftboss_manor_shot_due";
    private static final String SHOT_NEXT = "ftboss_manor_shot_next";
    private static final String SHOT_TARGET = "ftboss_manor_shot_target";
    private static final String SHOT_X = "ftboss_manor_shot_x";
    private static final String SHOT_Y = "ftboss_manor_shot_y";
    private static final String SHOT_Z = "ftboss_manor_shot_z";
    private static final String SHOT_INDEX = "ftboss_manor_finale_shot_index";

    private ManorFinaleCombat() {
    }

    public static boolean manages(LivingEntity var0) {
        if (var0 instanceof EasterBossEntity var1 && var1.isImpossibleChallenge() && !var1.isGiant() && ManorMultiplayerBalance.isManagedManorBoss(var0)) {
            return true;
        }

        return false;
    }

    public static void tick(EasterBossEntity var0) {
        if (manages(var0) && var0.level() instanceof ServerLevel var1) {
            Vec3 var4 = ManorFight.anchorOf(ManorFightMembers.fightId(var0));
            if (var4 != null) {
                List var3 = var1.players().stream().filter(var1x -> validTarget(var0, var1x)).toList();
                updateAggro(var0, var3);
                tickAssault(var1, var0, var3, var4);
            }
        }
    }

    public static boolean keepCommittedTarget(Mob var0) {
        if (manages(var0) && var0 instanceof EasterBossEntity var1 && var1.isPerformingSkill() && validTarget(var1, var1.getTarget())) {
            return true;
        }

        return false;
    }

    private static boolean combatReady(EasterBossEntity var0) {
        return manages(var0)
            && var0.isAlive()
            && !var0.isDeadOrDying()
            && !var0.isNoAi()
            && !var0.isGiant()
            && !var0.isInvulnerable()
            && !HiddenBossIntro.isActing(var0.getUUID());
    }

    public static boolean validTarget(LivingEntity var0, LivingEntity var1) {
        if (manages(var0) && ManorCombatContinuity.present(var0, var1)) {
            Vec3 var2 = ManorFight.anchorOf(ManorFightMembers.fightId(var0));
            return var2 != null && insideArena(var1.position(), var2);
        } else {
            return false;
        }
    }

    private static boolean insideArena(Vec3 var0, Vec3 var1) {
        return ManorFinaleRules.insideArena(var0.x - var1.x, var0.y - var1.y, var0.z - var1.z);
    }

    public static void updateAggro(EasterBossEntity var0, List<ServerPlayer> var1) {
        if (combatReady(var0)) {
            LivingEntity var2 = var0.getTarget();
            if (!var0.isPerformingSkill() || !validTarget(var0, var2)) {
                LivingEntity var3 = BossAggro.highest(var0);
                if (!validTarget(var0, var3)) {
                    var3 = validTarget(var0, var2) ? var2 : null;
                }

                if (var3 == null && validTarget(var0, var0.getLastHurtByMob())) {
                    var3 = var0.getLastHurtByMob();
                }

                if (var3 == null) {
                    var3 = var1.stream()
                        .filter(var1x -> validTarget(var0, var1x))
                        .min(
                            Comparator.<ServerPlayer>comparingInt(var1x -> var0.hasLineOfSight(var1x) ? 0 : 1)
                                .thenComparingDouble(var1x -> var0.distanceToSqr(var1x))
                                .thenComparing(var0x -> var0x.getUUID().toString())
                        )
                        .orElse(null);
                }

                if (var3 != var2) {
                    var0.setTarget(var3);
                    var0.getNavigation().stop();
                }
            }
        }
    }

    public static void huntTarget(EasterBossEntity var0, ServerLevel var1) {
        LivingEntity var2 = var0.getTarget();
        if (combatReady(var0) && validTarget(var0, var2)) {
            var0.getLookControl().setLookAt(var2, 30.0F, 30.0F);
            if (var0.distanceToSqr(var2) > 400.0 && var1.getGameTime() % 10L == 0L) {
                var0.getNavigation().moveTo(var2, 1.6);
            }
        }
    }

    public static void tickAssault(ServerLevel var0, EasterBossEntity var1, List<ServerPlayer> var2, Vec3 var3) {
        CompoundTag var4 = var1.getPersistentData();
        var4.remove("ftboss_manor_edge_target");
        var4.remove("ftboss_manor_edge_since");
        if (!combatReady(var1)) {
            clearDue(var4);
            var4.remove("ftboss_manor_shot_next");
        } else {
            long var5 = var0.getGameTime();
            if (var4.contains("ftboss_manor_shot_due")) {
                LivingEntity var7 = var4.hasUUID("ftboss_manor_shot_target")
                    ? ManorLivingTargets.resolve(var0, var4.getUUID("ftboss_manor_shot_target"))
                    : null;
                Vec3 var8 = storedField(var4);
                if (!validTarget(var1, var7) || !insideArena(var8, var3)) {
                    clearDue(var4);
                } else if (var5 >= var4.getLong("ftboss_manor_shot_due")) {
                    fireAssault(var0, var1, var2, var7, var8);
                    clearDue(var4);
                } else if (var4.getLong("ftboss_manor_shot_due") - var5 == 10L) {
                    warningRing(var0, var8);
                }
            }

            List<LivingEntity> var16 = candidates(var1, var2);
            if (var16.isEmpty()) {
                clearDue(var4);
                var4.remove("ftboss_manor_shot_next");
            } else {
                int var17 = var1.getUUID().hashCode();
                if (!var4.contains("ftboss_manor_shot_next")) {
                    var4.putLong("ftboss_manor_shot_next", var5 + 30L + (long)Math.floorMod(var17, 16));
                }

                if (!var4.contains("ftboss_manor_shot_due") && var5 >= var4.getLong("ftboss_manor_shot_next")) {
                    LivingEntity var9 = var1.getTarget();
                    List<LivingEntity> var10 = var16.stream().filter(var1x -> var1x != var9).toList();
                    List<LivingEntity> var11 = var10.isEmpty() ? var16 : var10;
                    long var12 = var4.getLong("ftboss_manor_finale_shot_index");
                    LivingEntity var14 = (LivingEntity)var11.get(ManorFinaleRules.rotationStart(var17, var12, var11.size()));
                    Vec3 var15 = fieldAt(var0, var1, var14.position(), var3);
                    var4.putUUID("ftboss_manor_shot_target", var14.getUUID());
                    var4.putDouble("ftboss_manor_shot_x", var15.x);
                    var4.putDouble("ftboss_manor_shot_y", var15.y);
                    var4.putDouble("ftboss_manor_shot_z", var15.z);
                    var4.putLong("ftboss_manor_shot_due", var5 + 20L);
                    var4.putLong("ftboss_manor_shot_next", var5 + (long)ManorFinaleRules.cooldown(var17));
                    var4.putLong("ftboss_manor_finale_shot_index", var12 + 1L & Long.MAX_VALUE);
                    ManorAssaultVisuals.show(var0, var1, var15, false);
                    warningRing(var0, var15);
                    var0.sendParticles(ParticleTypes.POOF, var15.x, var15.y + 0.12, var15.z, 18, 1.0, 0.03, 1.0, 0.01);
                    var0.playSound(null, var15.x, var15.y, var15.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.8F, 0.75F);
                    FTBossMod.LOGGER
                        .info(
                            "[FTBoss][ManorAssault] charge boss={} variant={} target={} mode=finale windup={} cooldown={}",
                            var1.getName().getString(),
                            var1.getVariant(),
                            var14.getName().getString(),
                            20,
                            ManorFinaleRules.cooldown(var17)
                        );
                }
            }
        }
    }

    private static List<LivingEntity> candidates(EasterBossEntity var0, List<ServerPlayer> var1) {
        ArrayList<LivingEntity> var2 = new ArrayList<>();

        for (ServerPlayer var4 : var1) {
            addCandidate(var2, var0, var4);
        }

        addCandidate(var2, var0, var0.getTarget());
        addCandidate(var2, var0, var0.getLastHurtByMob());
        addCandidate(var2, var0, BossAggro.highest(var0));
        var2.sort(Comparator.comparing(var0x -> var0x.getUUID().toString()));
        return var2;
    }

    private static void addCandidate(List<LivingEntity> var0, EasterBossEntity var1, LivingEntity var2) {
        if (validTarget(var1, var2) && !var0.contains(var2)) {
            var0.add(var2);
        }
    }

    private static Vec3 fieldAt(ServerLevel var0, EasterBossEntity var1, Vec3 var2, Vec3 var3) {
        BlockHitResult var4 = var0.clip(new ClipContext(var2.add(0.0, 0.05, 0.0), var2.add(0.0, -4.0, 0.0), Block.COLLIDER, Fluid.NONE, var1));
        if (var4.getType() != Type.MISS) {
            Vec3 var5 = var4.getLocation();
            if (var2.y - var5.y <= 3.0 && insideArena(var5, var3)) {
                return var5;
            }
        }

        return var2;
    }

    private static Vec3 storedField(CompoundTag var0) {
        return new Vec3(var0.getDouble("ftboss_manor_shot_x"), var0.getDouble("ftboss_manor_shot_y"), var0.getDouble("ftboss_manor_shot_z"));
    }

    private static void warningRing(ServerLevel var0, Vec3 var1) {
        for (int var2 = 0; var2 < 20; var2++) {
            double var3 = (double)var2 * Math.PI * 2.0 / 20.0;
            var0.sendParticles(ParticleTypes.END_ROD, var1.x + Math.cos(var3) * 3.0, var1.y + 0.12, var1.z + Math.sin(var3) * 3.0, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    private static void clearDue(CompoundTag var0) {
        var0.remove("ftboss_manor_shot_due");
        var0.remove("ftboss_manor_shot_target");
        var0.remove("ftboss_manor_shot_x");
        var0.remove("ftboss_manor_shot_y");
        var0.remove("ftboss_manor_shot_z");
    }

    private static void fireAssault(ServerLevel var0, EasterBossEntity var1, List<ServerPlayer> var2, LivingEntity var3, Vec3 var4) {
        ManorAssaultVisuals.show(var0, var1, var4, true);
        var0.sendParticles(ParticleTypes.POOF, var4.x, var4.y + 0.25, var4.z, 18, 1.2, 0.12, 1.2, 0.04);
        var0.playSound(null, var4.x, var4.y, var4.z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.9F, 0.85F);
        List<LivingEntity> var5 = candidates(var1, var2);
        addCandidate(var5, var1, var3);
        int var6 = 0;

        for (LivingEntity var8 : var5) {
            AABB var9 = var8.getBoundingBox();
            if (ManorFinaleRules.intersectsField(var4.x, var4.y, var4.z, var9.minX, var9.minY, var9.minZ, var9.maxX, var9.maxY, var9.maxZ)) {
                Vec3 var10 = var8.position().add(0.0, (double)var8.getBbHeight() * 0.5, 0.0);
                BlockHitResult var11 = var0.clip(new ClipContext(var4.add(0.0, 0.15, 0.0), var10, Block.COLLIDER, Fluid.NONE, var1));
                if (var11.getType() == Type.MISS && var8.hurt(var1.damageSources().indirectMagic(var1, var1), 5.0F)) {
                    var6++;
                }
            }
        }

        FTBossMod.LOGGER
            .info(
                "[FTBoss][ManorAssault] release boss={} variant={} hits={} mode=finale center=({},{},{})",
                var1.getName().getString(),
                var1.getVariant(),
                var6,
                var4.x,
                var4.y,
                var4.z
            );
    }
}
