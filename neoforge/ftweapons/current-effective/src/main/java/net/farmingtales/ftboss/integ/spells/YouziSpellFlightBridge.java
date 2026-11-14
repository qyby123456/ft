package net.farmingtales.ftboss.integ.spells;

import java.util.List;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Vector;
import net.farmingtales.ftboss.youzi.spells.YouziSpellAssets;
import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class YouziSpellFlightBridge {
    public static boolean pulse(ServerLevel var0, LivingEntity var1, Kind var2, Vec3 var3, float var4, int var5) {
        if (var2 == Kind.EFFIGY_VOLLEY && YouziSpellAssets.geometry() != null) {
            if (!ManorSpellRules.pulse(var5, 12, 3)) {
                return true;
            } else {
                double var6 = BossSpellTuning.radiusScale(var1);
                Vec3 var8 = var1.position();
                List<Entity> var9 = var0.getEntities((Entity)null, new AABB(var8, var8).inflate(64.0), var0x -> var0x instanceof YouziStarProjectile);
                if (var5 == 0) {
                    for (Entity var11 : var9) {
                        if (((YouziStarProjectile)var11).ownedBy(var1)) {
                            var11.discard();
                        }
                    }
                }

                if (var9.size() >= 64) {
                    return true;
                } else {
                    Vec3 var30 = var3.subtract(var1.position());
                    double var31 = Math.hypot(var30.x, var30.z);
                    if (var31 < 1.0E-6) {
                        var30 = var1.getLookAngle();
                    }

                    double var13 = Math.atan2(-var30.x, -var30.z);
                    Vector var15 = YouziSpellAssets.geometry().launchOrigin(var5 / 12, var6);
                    double var16 = Math.cos(var13);
                    double var18 = Math.sin(var13);
                    Vec3 var20 = var8.add(
                        var15.coordinateX() * var16 + var15.coordinateZ() * var18,
                        var15.coordinateY(),
                        -var15.coordinateX() * var18 + var15.coordinateZ() * var16
                    );
                    LivingEntity var21 = null;
                    double var22 = Double.POSITIVE_INFINITY;

                    for (LivingEntity var25 : ManorSpellEffects.targets(var0, var1, var1.position(), 24.0)) {
                        Vec3 var26 = var25.getBoundingBox().getCenter().subtract(var20);
                        Vec3 var27 = var3.subtract(var20).normalize();
                        if (!(var26.normalize().dot(var27) < 0.25)) {
                            double var28 = var25.getBoundingBox().getCenter().distanceToSqr(var3);
                            if (var28 < var22) {
                                var22 = var28;
                                var21 = var25;
                            }
                        }
                    }

                    YouziStarProjectile var32 = new YouziStarProjectile(var0, var1, var21, var20, var3, var4, var6);
                    if (var0.addFreshEntity(var32)) {
                        SpellVisuals.show(var0, var1, var2.id, Phase.PULSE, var3);
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean validTarget(LivingEntity var0, LivingEntity var1) {
        return BossSpellEffects.canTarget(var0, var1);
    }

    public static void hit(LivingEntity var0, LivingEntity var1, float var2) {
        ManorSpellEffects.hurt(var0, var1, ManorIronSpells.spell(Kind.EFFIGY_VOLLEY), var2);
    }
}
