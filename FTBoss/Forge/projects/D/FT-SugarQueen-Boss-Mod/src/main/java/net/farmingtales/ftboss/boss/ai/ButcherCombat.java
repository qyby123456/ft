package net.farmingtales.ftboss.boss.ai;

import java.util.EnumSet;
import java.util.UUID;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

public final class ButcherCombat extends Goal {
    private static final ResourceKey<DamageType> MELEE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation("ftboss", "butcher_melee"));
    private final MeatMountainEntity owner;
    private final BossActionState action;
    private UUID swingTarget;
    private long nextAttack;
    private final BossApproach approach = new BossApproach();
    private int successfulHits;
    private int swings;
    private boolean resolved;

    public ButcherCombat(MeatMountainEntity owner, BossActionState action) {
        this.owner = owner;
        this.action = action;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.owner.isAlive() && !this.owner.isNoAi()
                && eligible(this.owner.getTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    public void update() {
        if (net.farmingtales.ftboss.butcherskills.ButcherSkills.tick(this.owner, this.action)) return;
        if (!(this.owner.level() instanceof ServerLevel server)) return;
        if (!this.owner.isAlive() || this.owner.isNoAi()) {
            cancelSwing();
            this.approach.reset(this.owner);
            return;
        }
        long now = server.getGameTime();
        LivingEntity target = BossAggro.highest(this.owner);
        if (target == null && eligible(this.owner.getTarget())) target = this.owner.getTarget();
        if (target == null) {
            double nearest = ButcherCombatRules.TARGET_RANGE * ButcherCombatRules.TARGET_RANGE;
            for (ServerPlayer candidate : server.players()) {
                double distance = this.owner.distanceToSqr(candidate);
                if (eligible(candidate) && distance < nearest) {
                    target = candidate;
                    nearest = distance;
                }
            }
            this.owner.setTarget(target);
        }
        if (target == null) {
            cancelSwing();
            this.approach.reset(this.owner);
            return;
        }
        int phase = this.owner.getButcherPhase();
        boolean enraged = phase == ButcherPhaseRules.ASSAULT;
        this.owner.getLookControl().setLookAt(target, 30, 30);
        double reach = Math.sqrt(this.owner.getBbWidth() * this.owner.getBbWidth() * 4 + target.getBbWidth());
        this.approach.tick(this.owner, target, ButcherCombatRules.chaseSpeed(enraged),
                Math.max(0.5, reach - 0.8), Math.max(0.6, reach - 0.3), 0, 0);
        if (this.action.active()) {
            if (!this.resolved && this.action.elapsed(0) >= ButcherMotionTiming.impact(phase, this.action.name())) {
                this.resolved = true;
                if (this.swingTarget != null && server.getEntity(this.swingTarget) instanceof LivingEntity victim
                        && canHit(victim)) hit(server, victim);
            }
            return;
        }
        if (now < this.nextAttack || !canHit(target)) return;
        this.swingTarget = target.getUUID();
        this.resolved = false;
        this.action.start(this.swings++ % 2 == 0 ? "chop" : "backhand", ButcherCombatRules.MELEE_DURATION);
        this.nextAttack = now + ButcherPhaseRules.interval(phase);
        this.owner.swing(InteractionHand.MAIN_HAND);
        server.playSound(null, this.owner.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE, 0.75F, enraged ? 1.3F : 1.1F);
    }

    private boolean eligible(LivingEntity target) {
        return BossTargets.valid(target) && target.level() == this.owner.level() && !this.owner.isAlliedTo(target)
                && !BossAggro.isBossSide(target)
                && this.owner.distanceToSqr(target) < ButcherCombatRules.TARGET_RANGE * ButcherCombatRules.TARGET_RANGE;
    }

    private boolean canHit(LivingEntity target) {
        return eligible(target) && this.owner.hasLineOfSight(target)
                && ButcherCombatRules.withinReach(this.owner.distanceToSqr(target),
                Math.abs(this.owner.getY() - target.getY()), this.owner.getBbWidth(), target.getBbWidth());
    }

    private void hit(ServerLevel server, LivingEntity target) {
        int phase = this.owner.getButcherPhase();
        float damage = ButcherBalanceRules.MELEE_DAMAGE;
        float before = target.getHealth() + target.getAbsorptionAmount();
        DamageSource source = new DamageSource(server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(MELEE_DAMAGE), this.owner);
        if (!target.hurt(source, damage)) return;
        float dealt = Math.max(0, before - target.getHealth() - target.getAbsorptionAmount());
        if (dealt <= 0) return;
        float healing = ButcherBalanceRules.lifesteal(phase, dealt, this.owner.getMaxHealth() - this.owner.getHealth());
        if (healing > 0) this.owner.heal(healing);
        if (phase == ButcherPhaseRules.ASSAULT) {
            ButcherHitEffects.applyTargetDebuffs(this.owner, target, ButcherCombatRules.empowered(this.successfulHits));
        }
        this.successfulHits++;
        server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1,
                target.getZ(), 5, 0.25, 0.3, 0.25, 0.05);
        server.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 0.8F, 0.8F);
    }

    public void reset() {
        cancelSwing();
        this.successfulHits = 0;
        this.nextAttack = this.owner.level().getGameTime() + ButcherPhaseRules.interval(this.owner.getButcherPhase());
    }

    private void cancelSwing() {
        if (this.action.active()) this.action.start("", 0);
        this.swingTarget = null;
        this.resolved = true;
    }
}
