package net.farmingtales.ftboss.boss.ai;

import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class BossMeleeWindup {
    private final Mob owner;
    private final BossActionState action;
    private final int impactTick;
    private final int duration;
    private final double reach;
    private LivingEntity target;

    public BossMeleeWindup(Mob owner, BossActionState action, int impactTick, int duration, double reach) {
        this.owner = owner;
        this.action = action;
        this.impactTick = impactTick;
        this.duration = duration;
        this.reach = reach;
    }

    public void start(Entity candidate) {
        if (this.owner.level().isClientSide || !this.owner.isAlive() || this.action.active()) return;
        if (!(candidate instanceof LivingEntity living) || !valid(living)) return;
        this.target = living;
        this.action.start("melee", this.duration);
        this.owner.getLookControl().setLookAt(living, 30, 30);
    }

    public void tick(Predicate<LivingEntity> hit) {
        if (this.target == null || this.owner.level().isClientSide) return;
        if (!"melee".equals(this.action.name())) {
            this.target = null;
            return;
        }
        if (this.action.elapsed(0) < this.impactTick) return;
        LivingEntity victim = this.target;
        this.target = null;
        if (!(this.owner.level() instanceof ServerLevel server)) return;
        server.playSound(null, this.owner.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE, 0.8f, 0.8f);
        if (!valid(victim) || !hit.test(victim)) return;
        server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5,
                victim.getZ(), 6, 0.18, 0.18, 0.18, 0.08);
    }

    private boolean valid(LivingEntity candidate) {
        return candidate.isAlive() && !candidate.isRemoved() && candidate.level() == this.owner.level()
                && BossTargets.valid(candidate) && this.owner.distanceToSqr(candidate) <= this.reach * this.reach
                && this.owner.hasLineOfSight(candidate);
    }
}
