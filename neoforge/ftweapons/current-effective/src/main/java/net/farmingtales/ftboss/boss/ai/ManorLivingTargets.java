package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.EasterPearlEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.xueli_melee.MeleePolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ManorLivingTargets {
    private static final String NEXT_PEARL = "ftboss:AirbornePearlAfter";

    private ManorLivingTargets() {
    }

    private static Entity actor(Entity var0) {
        return var0 instanceof Projectile var1 ? var1.getOwner() : var0;
    }

    public static boolean creative(LivingEntity var0) {
        if (var0 instanceof Player var1 && var1.isCreative()) {
            return true;
        }

        return false;
    }

    public static void tell(LivingEntity var0, Component var1, boolean var2) {
        if (var0 instanceof Player var3) {
            ManorSkillTextQuiet.deliver(var3, var1, var2);
        }
    }

    public static boolean valid(Entity var0, LivingEntity var1) {
        Entity var2 = actor(var0);
        return var2 != null
            && BossTargets.valid(var1)
            && !var1.isSpectator()
            && var1 != var2
            && !(var1 instanceof ArmorStand)
            && var1.level() == var2.level()
            && !var2.isAlliedTo(var1)
            && !var1.isAlliedTo(var2)
            && (!BossAggro.isBossSide(var2) || !BossAggro.isBossSide(var1));
    }

    public static boolean canHit(Entity var0, Entity var1) {
        if (var1 instanceof LivingEntity var2 && valid(var0, var2)) {
            return true;
        }

        return false;
    }

    public static List<LivingEntity> nearby(Level var0, Entity var1) {
        Entity var2 = actor(var1);
        return var2 != null && var2.level() == var0
            ? var0.getEntitiesOfClass(
                LivingEntity.class,
                var2.getBoundingBox().inflate(64.0),
                var1x -> valid(var2, var1x)
                        && (
                            var1x instanceof Player
                                || var2 instanceof Mob var3x && (var3x.getTarget() == var1x || var3x.getLastHurtByMob() == var1x)
                                || var1x instanceof Mob var2x && var2x.getTarget() == var2
                        )
            )
            : List.of();
    }

    public static LivingEntity resolve(Level var0, UUID var1) {
        if (var1 == null) {
            return null;
        } else {
            return (var0 instanceof ServerLevel var3 ? var3.getEntity(var1) : var0.getPlayerByUUID(var1)) instanceof LivingEntity var4 ? var4 : null;
        }
    }

    public static LivingEntity flywheelTarget(Entity var0) {
        Entity var1 = actor(var0);
        if (var1 instanceof Mob var2 && valid(var1, var2.getTarget()) && var2.getTarget().distanceToSqr(var1) <= 576.0) {
            return var2.getTarget();
        }

        LivingEntity var9 = null;
        double var3 = 576.0;

        for (LivingEntity var6 : nearby(var0.level(), var0)) {
            double var7 = var6.distanceToSqr(var1);
            if (var7 <= var3) {
                var3 = var7;
                var9 = var6;
            }
        }

        return var9;
    }

    public static boolean airborne(LivingEntity var0) {
        return var0 != null && !var0.onGround() && !var0.isInWater() && !var0.isInLava() && !var0.onClimbable();
    }

    public static boolean hasRanged(int var0) {
        return var0 == 0 || var0 == 1 || var0 == 2 || var0 == 4 || var0 == 5 || var0 == 6 || var0 == 7 || var0 == 10 || var0 == 11;
    }

    public static Skill rangedFive(int var0) {
        return switch (var0) {
            case 0 -> Skill.YOUZI_SEAL;
            case 1 -> Skill.DANDAN_STAR;
            default -> null;
            case 4 -> Skill.LEMON_ACID;
            case 7 -> Skill.SHADOW_CAGE;
        };
    }

    public static Skill airborneFive(Skill var0, LivingEntity var1, int var2) {
        if (!airborne(var1)) {
            return var0;
        } else {
            Skill var3 = rangedFive(var2);
            return var3 == null ? var0 : var3;
        }
    }

    public static boolean preferRanged(boolean var0, EasterBossEntity var1) {
        return ManorTargetPolicy.preferRanged(
            MeleePolicy.preferRanged(var0 || hasRanged(var1.getVariant()) && airborne(var1.getTarget()), var0, var1), var0, var1
        );
    }

    public static int airborneSpell(int var0, LivingEntity var1) {
        return airborne(var1) && var0 != 9 && var0 != 10 ? 1 : var0;
    }

    public static boolean airbornePearl(EasterBossEntity var0) {
        LivingEntity var1 = var0.getTarget();
        if (!airborne(var1)) {
            return false;
        } else {
            if (var0.level() instanceof ServerLevel var2
                && valid(var0, var1)
                && !hasRanged(var0.getVariant())
                && var0.getVariant() != 9
                && !var0.isMini()
                && !var0.isGiant()
                && !var0.isNoAi()
                && var0.isAlive()
                && !var0.isInvulnerable()
                && !var0.isPerformingSkill()
                && !ManorFight.isFinaleMember(var0)
                && !HiddenBossIntro.isActing(var0.getUUID())
                && !(var0.distanceToSqr(var1) <= 4.0)) {
                long var10 = var2.getGameTime();
                if (var10 < var0.getPersistentData().getLong("ftboss:AirbornePearlAfter")) {
                    return true;
                }

                EasterPearlEntity var5 = new EasterPearlEntity(var2, var0);
                Vec3 var6 = var0.getEyePosition().add(0.0, -0.2, 0.0);
                Vec3 var7 = var1.getEyePosition().subtract(var6);
                double var8 = Math.max(0.1, var7.length());
                var5.setPos(var6.x, var6.y, var6.z);
                var5.shoot(var7.x / var8, var7.y / var8 + 0.12, var7.z / var8, 1.5F, 0.0F);
                ManorFightMembers.inherit(var0, var5);
                if (var2.addFreshEntity(var5)) {
                    var0.getPersistentData().putLong("ftboss:AirbornePearlAfter", var10 + (long)var0.getChallengeDifficulty().acceleratedTicks(100));
                    var0.getLookControl().setLookAt(var1, 60.0F, 60.0F);
                }

                return true;
            }

            return true;
        }
    }

    public static List nearby(Level var0, Entity var1, AABB var2) {
        Entity var3 = actor(var1);
        return var3 != null && var3.level() == var0
            ? var0.getEntitiesOfClass(
                LivingEntity.class,
                var2,
                var1x -> valid(var3, var1x)
                        && (
                            var1x instanceof Player
                                || var3 instanceof Mob var3x && (var3x.getTarget() == var1x || var3x.getLastHurtByMob() == var1x)
                                || var1x instanceof Mob var2x && var2x.getTarget() == var3
                        )
            )
            : List.of();
    }
}
