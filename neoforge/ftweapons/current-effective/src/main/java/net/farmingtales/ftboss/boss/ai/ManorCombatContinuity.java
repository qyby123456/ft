package net.farmingtales.ftboss.boss.ai;

import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyi_basic.BasicServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ManorCombatContinuity {
    private ManorCombatContinuity() {
    }

    public static boolean present(Entity var0, LivingEntity var1) {
        return ManorLivingTargets.valid(var0, var1) && (!(var0.level() instanceof ServerLevel var2) || var2.getEntity(var1.getUUID()) == var1);
    }

    private static boolean combatBoss(EasterBossEntity var0) {
        return var0 != null && !var0.isMini() && !var0.isGiant() && EasterBossEntity.isManorVariant(var0.getVariant());
    }

    private static boolean target(EasterBossEntity var0, LivingEntity var1) {
        return present(var0, var1)
            && (ManorMultiplayerBalance.isManagedManorBoss(var0) ? ManorMultiplayerBalance.validManorTarget(var0, var1) : var0.distanceToSqr(var1) <= 4096.0);
    }

    public static boolean canTeleport(EasterBossEntity var0, LivingEntity var1) {
        if (!(var1 instanceof Player)) {
            return ManorLivingTargets.valid(var0, var1);
        } else {
            if (!(var1 instanceof ServerPlayer var2) || !present(var0, var1)) {
                return false;
            }

            return var2.getUUID().equals(var0.getManorSummonerId())
                ? true
                : combatBoss(var0)
                    && target(var0, var1)
                    && (var1 == var0.getTarget() || var1 == BossAggro.highest(var0) || ManorMultiplayerBalance.validManorTarget(var0, var1));
        }
    }

    public static boolean canAttack(EasterBossEntity var0, Entity var1) {
        if (var1 instanceof LivingEntity var2 && present(var0, var2)) {
            return true;
        }

        return false;
    }

    private static LivingEntity choose(EasterBossEntity var0, List<ServerPlayer> var1) {
        LivingEntity var2 = BossAggro.highest(var0);
        if (target(var0, var2)) {
            return var2;
        } else {
            LivingEntity var3 = var0.getTarget();
            return target(var0, var3)
                ? var3
                : var1.stream()
                    .filter(var1x -> target(var0, var1x))
                    .min(
                        Comparator.<ServerPlayer>comparingInt(var1x -> var0.hasLineOfSight(var1x) ? 0 : 1)
                            .thenComparingDouble(var1x -> var0.distanceToSqr(var1x))
                            .thenComparing(var0x -> var0x.getUUID().toString())
                    )
                    .orElse(null);
        }
    }

    private static void change(EasterBossEntity var0, LivingEntity var1) {
        if (var1 != var0.getTarget()) {
            var0.setTarget(var1);
            var0.getNavigation().stop();
            BasicServer.cancel(var0);
            net.farmingtales.ftboss.xueli_basic.BasicServer.cancel(var0, true);
        }
    }

    public static void repair(EasterBossEntity var0) {
        if (combatBoss(var0)
            && var0.level() instanceof ServerLevel var1
            && var0.isAlive()
            && !var0.isDeadOrDying()
            && !var0.isNoAi()
            && !var0.isInvulnerable()
            && !var0.isManorIntroHidden()
            && !HiddenBossIntro.isActing(var0.getUUID())
            && !ManorFinaleCombat.manages(var0)) {
            change(var0, choose(var0, var1.players()));
        }
    }

    public static void update(EasterBossEntity var0, List<ServerPlayer> var1) {
        if (ManorFinaleCombat.manages(var0)) {
            ManorFinaleCombat.updateAggro(var0, var1);
        } else if (!var0.isNoAi()) {
            change(var0, choose(var0, var1));
        }
    }

    public static ServerPlayer fightPlayer(EasterBossEntity var0) {
        if (var0.level() instanceof ServerLevel var1) {
            if (var0.getTarget() instanceof ServerPlayer var3 && target(var0, var3)) {
                return var3;
            }

            if (BossAggro.highest(var0) instanceof ServerPlayer var4 && target(var0, var4)) {
                return var4;
            }

            return var1.players()
                .stream()
                .filter(var1x -> target(var0, var1x))
                .min(Comparator.<ServerPlayer>comparingDouble(var1x -> var0.distanceToSqr(var1x)).thenComparing(var0x -> var0x.getUUID().toString()))
                .orElse(null);
        } else {
            return null;
        }
    }
}
