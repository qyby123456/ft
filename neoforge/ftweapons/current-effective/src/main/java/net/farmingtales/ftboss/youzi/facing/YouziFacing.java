package net.farmingtales.ftboss.youzi.facing;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class YouziFacing {
    private YouziFacing() {
    }

    private static boolean supports(String var0) {
        return "effigy_volley".equals(var0) || "effigy_ward".equals(var0) || "effigy_procession".equals(var0);
    }

    private static boolean owner(LivingEntity var0) {
        if (var0 instanceof EasterBossEntity var1 && var1.getVariant() == 0 && !var1.isNoAi() && var1.isAlive() && !var1.isRemoved() && !var1.isInvulnerable()) {
            return true;
        }

        return false;
    }

    public static boolean face(LivingEntity var0, String var1) {
        if (supports(var1) && owner(var0) && var0.level() instanceof ServerLevel) {
            EasterBossEntity var2 = (EasterBossEntity)var0;
            LivingEntity var3 = var2.getTarget();
            if (var3 != null
                && var3 != var0
                && var3.level() == var0.level()
                && var3.isAlive()
                && !var3.isRemoved()
                && !var3.isInvulnerable()
                && BossSpellEffects.canTarget(var0, var3)
                && var0.hasLineOfSight(var3)) {
                Vec3 var4 = var3.getEyePosition().subtract(var0.getEyePosition());
                if (Double.isFinite(var4.x) && Double.isFinite(var4.y) && Double.isFinite(var4.z) && !(var4.lengthSqr() < 1.0E-12)) {
                    float var5 = FacingMath.yaw(var4.x, var4.z, var0.getYRot());
                    float var6 = FacingMath.pitch(var4.x, var4.y, var4.z, var0.getXRot());
                    if (Float.isFinite(var5) && Float.isFinite(var6)) {
                        var2.getLookControl().setLookAt(var3, 180.0F, 180.0F);
                        var0.setYRot(var5);
                        var0.setXRot(var6);
                        var0.setYHeadRot(var5);
                        var0.setYBodyRot(var5);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static Vec3 direction(Vec3 var0, LivingEntity var1, String var2) {
        if ("effigy_ward".equals(var2) && owner(var1)) {
            Vec3 var3 = var1.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
            return Double.isFinite(var3.x) && Double.isFinite(var3.z) && var3.lengthSqr() > 1.0E-12 ? var3 : var0;
        } else {
            return var0;
        }
    }

    public static double modelYaw(double var0, LivingEntity var2, SpellVisualPacket var3, float var4) {
        if (owner(var2) && var3 != null && supports(var3.spell().id)) {
            boolean var5 = var3.spell() == Spell.EFFIGY_WARD || var3.spell() == Spell.EFFIGY_VOLLEY && var3.phase() == Phase.CHARGE;
            return var5 ? FacingMath.modelYaw(var2.yHeadRotO, var2.yHeadRot, var4, var0) : var0;
        } else {
            return var0;
        }
    }
}
