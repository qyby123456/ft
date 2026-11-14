package net.farmingtales.ftboss.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class PlayerMoonChakramEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(PlayerMoonChakramEntity.class, EntityDataSerializers.BOOLEAN);
    private final Set<UUID> outboundHits = new HashSet<>();
    private final Set<UUID> returnHits = new HashSet<>();
    private boolean reloaded;
    private boolean recovered;
    private Vec3 interpolationTarget;
    private int interpolationTicks;
    private InteractionHand launchHand = InteractionHand.MAIN_HAND;

    public PlayerMoonChakramEntity(EntityType<? extends PlayerMoonChakramEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() { entityData.define(RETURNING, false); }

    public boolean isReturning() { return entityData.get(RETURNING); }

    public void setLaunchHand(InteractionHand hand) { launchHand = hand; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (interpolationTicks > 0) {
                setPos(position().lerp(interpolationTarget, 1.0 / interpolationTicks));
                interpolationTicks--;
            } else {
                setPos(position().add(getDeltaMovement()));
            }
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.isSpectator()
                || owner.level() != level() || reloaded || tickCount > XuanlanRules.MAX_FLIGHT_TICKS
                || distanceToSqr(owner) > 64 * 64 || !XuanlanCombat.isTrackedBlade(owner, this)) {
            discard();
            return;
        }
        ServerLevel level = owner.serverLevel();
        if (tickCount > XuanlanRules.OUTBOUND_TICKS) entityData.set(RETURNING, true);
        Vec3 from = position();
        if (isReturning()) {
            Vec3 destination = owner.getEyePosition().add(0, -0.2, 0);
            Vec3 offset = destination.subtract(from);
            if (offset.length() < 0.8) {
                recovered = true;
                XuanlanFxPacket.send(level, XuanlanMotionRules.catchKind(XuanlanCombat.bladeSlot(owner, this), launchHand == InteractionHand.OFF_HAND),
                        owner.getId(), position(), destination);
                level.playSound(null, owner.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.5F, 1.4F);
                discard();
                return;
            }
            setDeltaMovement(offset.normalize().scale(Math.min(1.6, offset.length())));
        }
        Vec3 requested = from.add(getDeltaMovement());
        Vec3 endpoint = XuanlanCombat.clip(level, this, from, requested);
        boolean blocked = endpoint.distanceToSqr(requested) > 0.0001;
        Set<UUID> hits = isReturning() ? returnHits : outboundHits;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, endpoint).inflate(0.25),
                candidate -> XuanlanCombat.canHit(owner, candidate))) {
            if (hits.size() >= 16) break;
            if (hits.contains(target.getUUID()) || (!target.getBoundingBox().contains(from)
                    && target.getBoundingBox().clip(from, endpoint).isEmpty())) continue;
            hits.add(target.getUUID());
            boolean marked = XuanlanCombat.markedBy(target, owner);
            int previousInvulnerableTime = target.invulnerableTime;
            boolean damaged;
            target.invulnerableTime = 0;
            try {
                damaged = target.hurt(level.damageSources().mobProjectile(this, owner), XuanlanRules.bladeDamage(isReturning(), marked));
            } finally {
                target.invulnerableTime = Math.max(previousInvulnerableTime, target.invulnerableTime);
            }
            if (damaged) {
                if (marked) XuanlanCombat.consumeMark(target, owner);
                else XuanlanFxPacket.send(level, XuanlanFxPacket.HIT, target.getId(), target.getBoundingBox().getCenter(), endpoint);
                level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.6F, marked ? 1.7F : 1.2F);
            }
        }
        setPos(endpoint);
        updateRotation();
        if (blocked) {
            if (isReturning()) discard();
            else {
                setPos(endpoint.subtract(getDeltaMovement().normalize().scale(0.05)));
                entityData.set(RETURNING, true);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!isRemoved() && level() instanceof ServerLevel serverLevel && getOwner() instanceof ServerPlayer owner) {
            int slot = XuanlanCombat.forgetBlade(owner, this);
            if (slot >= 0 && !recovered && owner.level() == serverLevel) {
                XuanlanFxPacket.send(serverLevel, XuanlanMotionRules.lostKind(slot, launchHand == InteractionHand.OFF_HAND),
                        owner.getId(), position(), owner.position());
            }
        }
        super.remove(reason);
    }

    @Override
    public void lerpTo(double worldX, double worldY, double worldZ, float yaw, float pitch, int steps, boolean teleport) {
        interpolationTarget = new Vec3(worldX, worldY, worldZ);
        interpolationTicks = Math.max(1, Math.min(3, steps));
        setYRot(yaw);
        setXRot(pitch);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        reloaded = true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
