package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ManorCombatLanes {
    private ManorCombatLanes() {
    }

    public static boolean independent(EasterBossEntity var0) {
        if (var0 != null && !var0.isMini() && !var0.isGiant()) {
            int var1 = var0.getVariant();
            return var1 == 0 || var1 == 1 || var1 == 4 || var1 == 5 || var1 == 7 || var1 == 10;
        } else {
            return false;
        }
    }

    public static boolean spellReady(EasterBossEntity var0) {
        return var0.allowsParallelMelee() || independent(var0);
    }

    public static boolean blocksMelee(boolean var0, EasterBossEntity var1, boolean var2) {
        return var0 && (!independent(var1) || var2);
    }

    public static boolean rangedBlocksMelee(boolean var0, EasterBossEntity var1) {
        return !var0 && independent(var1) ? false : ManorLivingTargets.preferRanged(var0, var1);
    }

    public static LivingEntity selectThreat(BossThreatTable var0, long var1, UUID var3, Predicate<UUID> var4, LivingEntity var5) {
        UUID var6 = var0.highest(var1, var3, var4);
        ServerLevel var7 = (ServerLevel)var5.level();
        LivingEntity var8 = var6 != null && var7.getEntity(var6) instanceof LivingEntity var9 ? var9 : null;
        if (ManorMultiplayerBalance.isManagedManorBoss(var5)
            && var5 instanceof EasterBossEntity var18
            && !var18.isGiant()
            && !var18.isMini()
            && !(var8 instanceof ServerPlayer)) {
            ServerPlayer var19 = null;
            double var11 = 0.0;
            LivingEntity var13 = ((Mob)var5).getTarget();
            if (var13 instanceof ServerPlayer var14 && var4.test(var14.getUUID())) {
                var11 = var0.value(var14.getUUID(), var1);
                if (var11 > 0.0) {
                    var19 = var14;
                }
            }

            for (ServerPlayer var15 : var7.players()) {
                if (var4.test(var15.getUUID())) {
                    double var16 = var0.value(var15.getUUID(), var1);
                    if (var16 > var11
                        || var16 > 0.0
                            && var16 == var11
                            && var19 != null
                            && var13 != var19
                            && var15.getUUID().toString().compareTo(var19.getUUID().toString()) < 0) {
                        var19 = var15;
                        var11 = var16;
                    }
                }
            }

            return (LivingEntity)(var19 != null ? var19 : var8);
        }

        return var8;
    }

    public static UUID selectThreatId(BossThreatTable var0, long var1, UUID var3, Predicate<UUID> var4, LivingEntity var5) {
        LivingEntity var6 = selectThreat(var0, var1, var3, var4, var5);
        return var6 == null ? null : var6.getUUID();
    }
}
