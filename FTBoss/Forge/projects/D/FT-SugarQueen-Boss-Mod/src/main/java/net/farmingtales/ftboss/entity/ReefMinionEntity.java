package net.farmingtales.ftboss.entity;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.util.WaterBlessing;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ReefMinionEntity extends Monster implements GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int life = 600;
    private int burstCooldown = 80;

    public ReefMinionEntity(EntityType<? extends ReefMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ATTACK_DAMAGE, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.FOLLOW_RANGE, 40);
    }

    public boolean isPuffer() { return getType() == FTEntities.BRINE_PUFFER.get(); }
    public UUID ownerId() { return this.owner; }
    public void setOwnerId(UUID owner) { this.owner = owner; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, false));
    }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof TideFishBossEntity || source.getEntity() instanceof ReefMinionEntity) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living) || !BossTargets.valid(living)) return false;
        boolean hit = super.doHurtTarget(target);
        if (hit) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        WaterBlessing.tick(this);
        if (--this.life <= 0) { discard(); return; }
        if (this.owner != null && tickCount % 20 == 0) {
            Entity ownerEntity = server.getEntity(this.owner);
            if (!(ownerEntity instanceof TideFishBossEntity boss) || !boss.isAlive()) { discard(); return; }
        }
        if (getTarget() == null || !getTarget().isAlive() || !BossTargets.valid(getTarget())) {
            setTarget(BossTargets.nearest(server, this, 36));
        }
        if (getTarget() != null && onGround() && tickCount % 10 == 0) {
            setDeltaMovement(getDeltaMovement().add(0, 0.34, 0));
        }
        if (isPuffer() && --this.burstCooldown <= 0 && getTarget() != null && distanceToSqr(getTarget()) < 9) {
            this.burstCooldown = 100;
            server.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.5, getZ(), 36, 1.5, 0.5, 1.5, 0.02);
            for (ServerPlayer target : server.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(2.5))) {
                if (BossTargets.valid(target) && distanceToSqr(target) <= 6.25 && hasLineOfSight(target)
                        && target.hurt(damageSources().mobAttack(this), 14)) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) tag.putUUID("TideOwner", this.owner);
        tag.putInt("TideLife", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.owner = tag.hasUUID("TideOwner") ? tag.getUUID("TideOwner") : null;
        this.life = tag.contains("TideLife") ? Math.max(1, Math.min(600, tag.getInt("TideLife"))) : 600;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "reef", 3,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.reef_minion.swim"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object object) { return this.tickCount; }
}
