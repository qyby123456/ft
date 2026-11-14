package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.util.QizaiCrystalRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
        setNoGravity(true);
        xpReward = 0;
    }

    public QizaiCrystalEntity(ServerLevel level, EasterBossEntity boss, int role) {
        this(FTEntities.QIZAI_CRYSTAL.get(), level);
        ownerId = boss.getUUID();
        entityData.set(ROLE, role);
        setCustomName(net.minecraft.network.chat.Component.translatable("entity.ftboss.qizai_crystal." + role));
        cooldown += role * 10;
        ManorFightMembers.inherit(boss, this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, QizaiCrystalRules.HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.FOLLOW_RANGE, QizaiCrystalRules.RANGE);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ROLE, 0);
        entityData.define(CUE, new CompoundTag());
        entityData.define(WARD, 0F);
    }

    @Override public EasterBossEntity getOwner() {
        return ownerId != null && level() instanceof ServerLevel level && level.getEntity(ownerId) instanceof EasterBossEntity boss ? boss : null;
    }

    public int role() { return Math.max(0, Math.min(2, entityData.get(ROLE))); }
    public CompoundTag cue() { return entityData.get(CUE); }
    public float ward() { return entityData.get(WARD); }
    public Entity shield() { return shield; }

    @Override public void travel(Vec3 input) {
        if (!level().isClientSide) move(MoverType.SELF, getDeltaMovement());
    }

    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed(Player player) { return false; }
    @Override protected void dropAllDeathLoot(DamageSource source) {}

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == getOwner() || source.getEntity() instanceof QizaiCrystalEntity crystal && crystal.getOwner() == getOwner()) return false;
        return super.hurt(source, amount);
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        var owner = getOwner();
        if (!isAlive() || isNoAi() || !QizaiCrystals.validOwner(owner) || !QizaiCrystals.current(this)) { discard(); return; }
        missiles.removeIf(Entity::isRemoved);
        LivingEntity primary = QizaiCrystals.combatTarget(owner);
        LivingEntity target = primary == null ? null : selectTarget(level, owner, primary);
        orbit(owner, primary);
        if (wardTicks > 0 && --wardTicks == 0) clearWard();
        if (shield != null && !shield.isRemoved() && primary != null) {
            Vec3 direction = primary.position().subtract(owner.position()).multiply(1, 0, 1).normalize();
            QizaiCrystalBridge.move(shield, owner.position().add(direction.scale(2.8)).add(0, 1.4, 0), yaw(direction));
        }
        if (target == null) {
            phase = 0;
            entityData.set(CUE, new CompoundTag());
            clearWard();
            return;
        }
        if (role() == QizaiCrystalRules.LASER && phase != 0) { laser(level, owner, target); return; }
        if (--cooldown > 0) return;
        if (role() == QizaiCrystalRules.LASER) {
            phase = 1;
            phaseTicks = QizaiCrystalRules.LASER_WINDUP;
            aim = target.getEyePosition();
            syncBeam(level);
            level.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.9F, 1.5F);
        } else if (role() == QizaiCrystalRules.MISSILE) {
            for (int index = 0; index < QizaiCrystalRules.MISSILE_COUNT; index++) {
                var missile = new QizaiCrystalMissileEntity(level, this, owner, target);
                missile.setPos(getEyePosition());
                float spread = (index / (float) (QizaiCrystalRules.MISSILE_COUNT - 1) - 0.5F) * 0.9F;
                missile.setDeltaMovement(target.getEyePosition().subtract(getEyePosition()).normalize().yRot(spread).scale(0.65));
                if (level.addFreshEntity(missile)) missiles.add(missile);
            }
            level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.7F, 1.6F);
            cooldown = QizaiCrystalRules.MISSILE_COOLDOWN;
        } else {
            clearWard();
            wardTicks = QizaiCrystalRules.WARD_DURATION;
            entityData.set(WARD, QizaiCrystalRules.WARD_CAPACITY);
            Vec3 direction = target.position().subtract(owner.position()).multiply(1, 0, 1).normalize();
            shield = QizaiCrystalBridge.create(level, owner, owner.position().add(direction.scale(2.8)).add(0, 1.4, 0), yaw(direction));
            level.playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.8F, 1.4F);
            cooldown = QizaiCrystalRules.WARD_COOLDOWN;
        }
    }

    private LivingEntity selectTarget(ServerLevel level, EasterBossEntity owner, LivingEntity primary) {
        if (role() == QizaiCrystalRules.GUARD) return primary;
        if (distanceToSqr(primary) <= QizaiCrystalRules.RANGE * QizaiCrystalRules.RANGE && hasLineOfSight(primary)) return primary;
        return level.players().stream().filter(player -> QizaiCrystals.target(owner, player)
                && distanceToSqr(player) <= QizaiCrystalRules.RANGE * QizaiCrystalRules.RANGE && hasLineOfSight(player))
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private void orbit(EasterBossEntity owner, LivingEntity target) {
        double angle = tickCount * 0.022 + role() * Math.PI * 2 / QizaiCrystalRules.COUNT;
        Vec3 preferred = owner.position().add(Math.cos(angle) * 4.2, 3.2 + Math.sin(tickCount * 0.06 + role()) * 0.4, Math.sin(angle) * 4.2);
        Vec3 movement = preferred.subtract(position());
        if (distanceToSqr(owner) > 48 * 48) { discard(); return; }
        setDeltaMovement(movement.normalize().scale(Math.min(0.32, movement.length() * 0.14)));
        if (target != null) getLookControl().setLookAt(target, 90, 90);
    }

    private static float yaw(Vec3 direction) { return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)); }

    private Vec3 clippedAim() {
        Vec3 from = getEyePosition();
        Vec3 direction = aim.subtract(from);
        Vec3 end = from.add(direction.normalize().scale(Math.min(QizaiCrystalRules.RANGE, direction.length() + 1)));
        return level().clip(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getLocation();
    }

    private void laser(ServerLevel level, EasterBossEntity owner, LivingEntity target) {
        if (phase == 1 && phaseTicks > 4) aim = target.getEyePosition();
        if (phase == 1 && --phaseTicks == 0) {
            phase = 2;
            phaseTicks = QizaiCrystalRules.LASER_DURATION;
            level.playSound(null, blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 0.8F, 1.6F);
        }
        if (phase == 2) {
            if ((QizaiCrystalRules.LASER_DURATION - phaseTicks) % 4 == 0) {
                Vec3 from = getEyePosition(), end = clippedAim();
                var victims = level.getEntitiesOfClass(Player.class, new AABB(from, end).inflate(0.3), player -> QizaiCrystals.target(owner, player));
                victims.stream().filter(player -> player.getBoundingBox().inflate(0.25).clip(from, end).isPresent())
                        .min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(player -> {
                            player.invulnerableTime = 0;
                            player.hurt(level.damageSources().indirectMagic(this, owner), QizaiCrystalRules.LASER_DAMAGE);
                        });
            }
            if (--phaseTicks <= 0) {
                phase = 0;
                cooldown = QizaiCrystalRules.LASER_COOLDOWN;
                entityData.set(CUE, new CompoundTag());
                return;
            }
        }
        syncBeam(level);
    }

    private void syncBeam(ServerLevel level) {
        var cue = new CompoundTag();
        Vec3 end = clippedAim();
        cue.putInt("phase", phase);
        cue.putLong("until", level.getGameTime() + phaseTicks + 2);
        cue.putDouble("x", end.x); cue.putDouble("y", end.y); cue.putDouble("z", end.z);
        entityData.set(CUE, cue);
    }

    public float absorb(float requested) {
        if (!isAlive() || isNoAi() || wardTicks <= 0 || !Float.isFinite(requested) || requested <= 0) return 0;
        float absorbed = Math.min(requested, ward());
        entityData.set(WARD, ward() - absorbed);
        return absorbed;
    }

    private void clearWard() {
        entityData.set(WARD, 0F);
        wardTicks = 0;
        if (shield != null) { shield.discard(); shield = null; }
    }

    @Override public void remove(RemovalReason reason) {
        clearWard();
        missiles.forEach(Entity::discard);
        missiles.clear();
        super.remove(reason);
    }

    @Override public void die(DamageSource source) {
        clearWard();
        if (level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 18, 0.4, 0.6, 0.4, 0.08);
        super.die(source);
        discard();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerId != null) tag.putUUID("CrystalOwner", ownerId);
        tag.putInt("CrystalRole", role());
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerId = tag.hasUUID("CrystalOwner") ? tag.getUUID("CrystalOwner") : null;
        entityData.set(ROLE, Math.max(0, Math.min(2, tag.getInt("CrystalRole"))));
    }
}
