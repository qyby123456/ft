package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public final class ButcherMinionMeleeGoal extends MeleeAttackGoal {
    private final int interval;
    private long nextAttack;

    public ButcherMinionMeleeGoal(PathfinderMob owner, int kind) {
        super(owner, 1.3, true);
        this.interval = ButcherCombatRules.minionInterval(kind);
    }

    @Override
    public boolean canUse() {
        return eligible(this.mob.getTarget()) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return eligible(this.mob.getTarget()) && super.canContinueToUse();
    }

    private boolean eligible(LivingEntity target) {
        return target instanceof ServerPlayer player && BossTargets.valid(player)
                && !this.mob.isAlliedTo(player) && this.mob.level() == player.level();
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target, double distanceSquared) {
        long now = this.mob.level().getGameTime();
        if (now < this.nextAttack || !eligible(target) || !this.mob.hasLineOfSight(target)
                || distanceSquared > getAttackReachSqr(target)) return;
        this.nextAttack = now + this.interval;
        this.mob.swing(InteractionHand.MAIN_HAND);
        this.mob.doHurtTarget(target);
    }
}
