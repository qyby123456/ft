package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class BossTargets {
    private BossTargets() {
    }

    public static boolean valid(Player var0) {
        return var0 != null && var0.isAlive() && !var0.isRemoved() && !var0.isCreative() && !var0.isSpectator();
    }

    public static boolean valid(LivingEntity var0) {
        return var0 != null && var0.isAlive() && !var0.isRemoved() && (!(var0 instanceof Player var1) || valid(var1));
    }

    public static LivingEntity combatTarget(Mob var0, double var1) {
        LivingEntity var3 = BossAggro.highest(var0);
        if (var3 != null) {
            return var3;
        } else {
            return var0.level() instanceof ServerLevel var4 ? nearest(var4, var0, var1) : null;
        }
    }

    public static ServerPlayer nearest(ServerLevel var0, double var1, double var3, double var5, double var7) {
        if (var0 == null) {
            return null;
        } else {
            ServerPlayer var9 = null;
            double var10 = var7 * var7;
            AABB var12 = new AABB(var1 - var7, var3 - var7, var5 - var7, var1 + var7, var3 + var7, var5 + var7);

            for (ServerPlayer var14 : var0.getEntitiesOfClass(ServerPlayer.class, var12)) {
                if (valid((Player)var14)) {
                    double var15 = var14.distanceToSqr(var1, var3, var5);
                    if (var15 < var10) {
                        var10 = var15;
                        var9 = var14;
                    }
                }
            }

            return var9;
        }
    }

    public static ServerPlayer nearest(ServerLevel var0, Entity var1, double var2) {
        return var1 == null ? null : nearest(var0, var1.getX(), var1.getY(), var1.getZ(), var2);
    }

    public static void dropInvalidTarget(LivingEntity var0) {
        if (var0 instanceof Mob var1) {
            LivingEntity var2 = var1.getTarget();
            if (var2 != null && (!valid(var2) || var2.level() != var0.level())) {
                var1.setTarget(null);
                var1.getNavigation().stop();
            }
        }
    }
}
