package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TemporaryTriads;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.skill.qizaiparty.MultiplayerCrystals;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalEntity extends Mob implements TraceableEntity {
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(QizaiCrystalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> CUE = SynchedEntityData.defineId(QizaiCrystalEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Float> WARD = SynchedEntityData.defineId(QizaiCrystalEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerId;
    private int cooldown = 30;
    private int phase;
    private int phaseTicks;
    private int wardTicks;
    private Vec3 aim = Vec3.ZERO;
    private Entity shield;
    private final List<QizaiCrystalMissileEntity> missiles = new ArrayList<>();

    public QizaiCrystalEntity(EntityType<? extends QizaiCrystalEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public QizaiCrystalEntity(ServerLevel level, EasterBossEntity boss, int role) {
        this((EntityType<? extends QizaiCrystalEntity>)FTEntities.QIZAI_CRYSTAL.get(), level);
        this.ownerId = boss.getUUID();
        this.entityData.set(ROLE, role);
        this.setCustomName(Component.translatable("entity.ftboss.qizai_crystal." + role));
        this.cooldown += role * 10;
        ManorFightMembers.inherit(boss, this);
    }

    public static Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.ATTACK_DAMAGE, 0.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 36.0);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder var1) {
        super.defineSynchedData(var1);
        var1.define(ROLE, 0);
        var1.define(CUE, new CompoundTag());
        var1.define(WARD, 0.0F);
    }

    public EasterBossEntity getOwner() {
        return this.ownerId != null && this.level() instanceof ServerLevel level && level.getEntity(this.ownerId) instanceof EasterBossEntity boss
            ? boss
            : null;
    }

    public int role() {
        return Math.max(0, Math.min(2, this.entityData.get(ROLE)));
    }

    public CompoundTag cue() {
        return this.entityData.get(CUE);
    }

    public float ward() {
        return this.entityData.get(WARD);
    }

    public Entity shield() {
        return this.shield;
    }

    @Override
    public void travel(Vec3 input) {
        if (!this.level().isClientSide) {
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

    protected void dropAllDeathLoot(DamageSource source) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this.getOwner()) {
            return false;
        } else {
            if (source.getEntity() instanceof QizaiCrystalEntity crystal && crystal.getOwner() == this.getOwner()) {
                return false;
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            EasterBossEntity var8 = this.getOwner();
            if (this.isAlive() && !this.isNoAi() && QizaiCrystals.validOwner(var8) && QizaiCrystals.current(this)) {
                this.missiles.removeIf(Entity::isRemoved);
                LivingEntity primary = QizaiCrystals.combatTarget(var8);
                LivingEntity target = primary == null ? null : this.selectTarget(level, var8, primary);
                this.orbit(var8, primary);
                if (this.wardTicks > 0 && --this.wardTicks == 0) {
                    this.clearWard();
                }

                if (this.shield != null && !this.shield.isRemoved() && primary != null) {
                    Vec3 direction = primary.position().subtract(var8.position()).multiply(1.0, 0.0, 1.0).normalize();
                    QizaiCrystalBridge.move(this.shield, var8.position().add(direction.scale(2.8)).add(0.0, 1.4, 0.0), yaw(direction));
                }

                if (target == null) {
                    this.phase = 0;
                    this.entityData.set(CUE, new CompoundTag());
                    this.clearWard();
                } else if (this.role() == 0 && this.phase != 0) {
                    this.laser(level, var8, target);
                } else if ((this.cooldown = CrossGuard.arm(this.cooldown, this) - 1) <= 0) {
                    if (this.role() == 0) {
                        this.phase = 1;
                        this.phaseTicks = 16;
                        this.aim = target.getEyePosition();
                        this.syncBeam(level);
                        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.9F, 1.5F);
                    } else if (this.role() == 1) {
                        for (int index = 0; index < 9; index++) {
                            QizaiCrystalMissileEntity missile = new QizaiCrystalMissileEntity(level, this, var8, target);
                            missile.setPos(this.getEyePosition());
                            float spread = ((float)index / 8.0F - 0.5F) * 0.9F;
                            missile.setDeltaMovement(target.getEyePosition().subtract(this.getEyePosition()).normalize().yRot(spread).scale(0.65));
                            if (level.addFreshEntity(missile)) {
                                this.missiles.add(missile);
                            }
                        }

                        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.7F, 1.6F);
                        this.cooldown = 100;
                    } else {
                        this.clearWard();
                        this.wardTicks = 120;
                        this.entityData.set(WARD, 240.0F);
                        Vec3 direction = target.position().subtract(var8.position()).multiply(1.0, 0.0, 1.0).normalize();
                        this.shield = QizaiCrystalBridge.create(level, var8, var8.position().add(direction.scale(2.8)).add(0.0, 1.4, 0.0), yaw(direction));
                        level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.8F, 1.4F);
                        this.cooldown = 200;
                    }
                }
            } else {
                this.discard();
            }
        }
    }

    private LivingEntity selectTarget(ServerLevel level, EasterBossEntity owner, LivingEntity primary) {
        if (this.role() == 2) {
            return primary;
        } else {
            return this.distanceToSqr(primary) <= 1296.0 && this.hasLineOfSight(primary)
                ? primary
                : level.players()
                    .stream()
                    .filter(player -> QizaiCrystals.target(owner, player) && this.distanceToSqr(player) <= 1296.0 && this.hasLineOfSight(player))
                    .min(Comparator.comparingDouble(this::distanceToSqr))
                    .orElse(null);
        }
    }

    private void orbit(EasterBossEntity owner, LivingEntity target) {
        double angle = CrossGuard.angle(
            MultiplayerCrystals.angle(TemporaryTriads.angle((double)this.tickCount * 0.022 + (double)this.role() * Math.PI * 2.0 / 3.0, this), this), this
        );
        Vec3 preferred = owner.position()
            .add(
                Math.cos(angle) * CrossGuard.radius(MultiplayerCrystals.radius(TemporaryTriads.radius(4.2, this), this), this),
                CrossGuard.height(MultiplayerCrystals.height(TemporaryTriads.height(3.2, this), this), this)
                    + Math.sin((double)this.tickCount * 0.06 + (double)this.role())
                        * CrossGuard.bob(MultiplayerCrystals.bob(TemporaryTriads.bob(0.4, this), this), this),
                Math.sin(angle) * CrossGuard.radius(MultiplayerCrystals.radius(TemporaryTriads.radius(4.2, this), this), this)
            );
        Vec3 movement = preferred.subtract(this.position());
        if (this.distanceToSqr(owner) > 2304.0) {
            this.discard();
        } else {
            this.setDeltaMovement(movement.normalize().scale(Math.min(0.32, movement.length() * 0.14)));
            if (target != null) {
                this.getLookControl().setLookAt(target, 90.0F, 90.0F);
            }
        }
    }

    private static float yaw(Vec3 direction) {
        return (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }

    private Vec3 clippedAim() {
        Vec3 from = this.getEyePosition();
        Vec3 direction = this.aim.subtract(from);
        Vec3 end = from.add(direction.normalize().scale(Math.min(36.0, direction.length() + 1.0)));
        return this.level().clip(new ClipContext(from, end, Block.COLLIDER, Fluid.NONE, this)).getLocation();
    }

    private void laser(ServerLevel var1, EasterBossEntity var2, LivingEntity var3) {
        if (this.phase == 1 && this.phaseTicks > 4) {
            this.aim = var3.getEyePosition();
        }

        if (this.phase == 1 && --this.phaseTicks == 0) {
            this.phase = 2;
            this.phaseTicks = 9;
            var1.playSound(null, this.blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 0.8F, 1.6F);
        }

        if (this.phase == 2) {
            if ((9 - this.phaseTicks) % 4 == 0) {
                Vec3 var4 = this.getEyePosition();
                Vec3 var5 = this.clippedAim();
                List<LivingEntity> var6 = var1.getEntitiesOfClass(LivingEntity.class, new AABB(var4, var5).inflate(0.3), var1x -> QizaiCrystals.target(var2, var1x));
                var6.stream()
                    .filter(var2x -> var2x.getBoundingBox().inflate(0.25).clip(var4, var5).isPresent())
                    .min(Comparator.comparingDouble(this::distanceToSqr))
                    .ifPresent(var3x -> {
                        var3x.invulnerableTime = 0;
                        var3x.hurt(var1.damageSources().indirectMagic(this, var2), 32.0F);
                    });
            }

            if (--this.phaseTicks <= 0) {
                this.phase = 0;
                this.cooldown = 80;
                this.entityData.set(CUE, new CompoundTag());
                return;
            }
        }

        this.syncBeam(var1);
    }

    private void syncBeam(ServerLevel level) {
        CompoundTag cue = new CompoundTag();
        Vec3 end = this.clippedAim();
        cue.putInt("phase", this.phase);
        cue.putLong("until", level.getGameTime() + (long)this.phaseTicks + 2L);
        cue.putDouble("x", end.x);
        cue.putDouble("y", end.y);
        cue.putDouble("z", end.z);
        this.entityData.set(CUE, cue);
    }

    public float absorb(float requested) {
        if (this.isAlive() && !this.isNoAi() && this.wardTicks > 0 && Float.isFinite(requested) && !(requested <= 0.0F)) {
            float absorbed = Math.min(requested, this.ward());
            this.entityData.set(WARD, this.ward() - absorbed);
            return absorbed;
        } else {
            return 0.0F;
        }
    }

    private void clearWard() {
        this.entityData.set(WARD, 0.0F);
        this.wardTicks = 0;
        if (this.shield != null) {
            this.shield.discard();
            this.shield = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.clearWard();
        this.missiles.forEach(Entity::discard);
        this.missiles.clear();
        super.remove(reason);
    }

    @Override
    public void die(DamageSource source) {
        this.clearWard();
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 18, 0.4, 0.6, 0.4, 0.08);
        }

        super.die(source);
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerId != null) {
            tag.putUUID("CrystalOwner", this.ownerId);
        }

        tag.putInt("CrystalRole", this.role());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerId = tag.hasUUID("CrystalOwner") ? tag.getUUID("CrystalOwner") : null;
        this.entityData.set(ROLE, Math.max(0, Math.min(2, tag.getInt("CrystalRole"))));
    }
}
