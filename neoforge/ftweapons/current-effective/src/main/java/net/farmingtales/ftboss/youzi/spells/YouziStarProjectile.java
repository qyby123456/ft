package net.farmingtales.ftboss.youzi.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.farmingtales.ftboss.integ.spells.YouziSpellFlightBridge;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.EndReason;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Impact;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Snapshot;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Target;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Vector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerEntity;

public final class YouziStarProjectile extends Entity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(YouziStarProjectile.class, EntityDataSerializers.FLOAT);
    private UUID owner;
    private UUID lockedTarget;
    private YouziHomingFlight flight;
    private float damage;
    private LivingEntity hitTarget;
    private boolean targetLost;
    private List<AABB> nearbyBlocks = List.of();
    private List<Entity> nearbyTargets = List.of();

    public YouziStarProjectile(EntityType<? extends YouziStarProjectile> var1, Level var2) {
        super(var1, var2);
    }

    public YouziStarProjectile(ServerLevel var1, LivingEntity var2, LivingEntity var3, Vec3 var4, Vec3 var5, float var6, double var7) {
        this(YouziSpellRegistry.star(), var1);
        this.owner = var2.getUUID();
        this.lockedTarget = var3 == null ? null : var3.getUUID();
        this.damage = var6;
        this.entityData.set(SCALE, (float)var7);
        this.refreshDimensions();
        this.setPosRaw(var4.x, var4.y, var4.z);
        Vec3 var9 = var5.subtract(var4).normalize();
        this.flight = new YouziHomingFlight(YouziSpellAssets.parameters(var7), vector(var4), vector(var9));
        this.setDeltaMovement(var9.scale(0.6));
    }

    public double scale() {
        return (double)this.entityData.get(SCALE).floatValue();
    }

    public boolean ownedBy(LivingEntity var1) {
        return var1.getUUID().equals(this.owner);
    }

    @Override
    public EntityDimensions getDimensions(Pose var1) {
        float var2 = this.entityData != null && this.entityData.get(SCALE) != null ? this.entityData.get(SCALE) : 1.0F;
        return EntityDimensions.scalable(0.2F * var2, 0.2F * var2);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> var1) {
        super.onSyncedDataUpdated(var1);
        if (var1.equals(SCALE)) {
            this.refreshDimensions();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SCALE, 1.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag var1) {
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag var1) {
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity tracker) {
        return super.getAddEntityPacket(tracker);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (!(this.level() instanceof ServerLevel var1)
                || this.flight == null
                || this.tickCount > 30
                || !var1.hasChunkAt(BlockPos.containing(this.getX(), this.getY(), this.getZ()))) {
                this.discard();
                return;
            }

            if (!((this.owner == null ? null : var1.getEntity(this.owner)) instanceof LivingEntity var3) || !var3.isAlive()) {
                this.discard();
                return;
            }

            if (!var1.isAreaLoaded(BlockPos.containing(this.getX(), this.getY(), this.getZ()), 4)) {
                this.discard();
            } else {
                AABB var4 = new AABB(this.position(), this.position()).inflate(2.7 + 0.1 * this.scale());
                ArrayList var5 = new ArrayList();

                for (VoxelShape var7 : var1.getBlockCollisions(this, var4)) {
                    var5.addAll(var7.toAabbs());
                }

                LivingEntity var10000;
                label60: {
                    this.nearbyBlocks = var5;
                    this.nearbyTargets = var1.getEntities(this, var4, var1x -> {
                        if (var1x instanceof LivingEntity var2 && YouziSpellFlightBridge.validTarget(var3, var2)) {
                            return true;
                        }

                        return false;
                    });
                    if ((this.lockedTarget != null && !this.targetLost ? var1.getEntity(this.lockedTarget) : null) instanceof LivingEntity var8
                        && YouziSpellFlightBridge.validTarget(var3, var8)) {
                        var10000 = var8;
                        break label60;
                    }

                    var10000 = null;
                }

                LivingEntity var13 = var10000;
                if (var13 == null) {
                    this.targetLost = true;
                }

                double var14 = this.flight.snapshot().age();
                this.hitTarget = null;
                this.flight
                    .advance(
                        0.05,
                        var3x -> var13 == null
                                ? null
                                : new Target(
                                    vector(var13.getBoundingBox().getCenter()).add(vector(var13.getDeltaMovement()).multiply(20.0 * (var3x - var14))),
                                    vector(var13.getDeltaMovement()).multiply(20.0),
                                    0.0
                                ),
                        (var3x, var4x, var5x) -> this.collision(var1, var3, var3x, var4x, var5x),
                        false
                    );
                Snapshot var10 = this.flight.snapshot();
                this.setPosRaw(var10.position().coordinateX(), var10.position().coordinateY(), var10.position().coordinateZ());
                this.setDeltaMovement(vec(var10.heading().multiply(var10.speed() / 20.0)));
                if (var10.endReason() != EndReason.ACTIVE) {
                    if (this.hitTarget != null && var10.endReason() == EndReason.TARGET) {
                        YouziSpellFlightBridge.hit(var3, this.hitTarget, this.damage);
                    }

                    if (var10.endReason() == EndReason.TARGET || var10.endReason() == EndReason.BLOCK) {
                        SpellVisuals.beam(var1, var3, "effigy_volley", this.position(), this.position());
                    }

                    this.discard();
                }
            }
        }
    }

    private Impact collision(ServerLevel var1, LivingEntity var2, Vector var3, Vector var4, double var5) {
        Vec3 var7 = vec(var3);
        Vec3 var8 = vec(var4);
        if (!var1.hasChunkAt(BlockPos.containing(var8.x, var8.y, var8.z))) {
            return new Impact(0.0, EndReason.BLOCK);
        } else {
            double var9 = Double.POSITIVE_INFINITY;
            EndReason var11 = EndReason.ACTIVE;
            LivingEntity var12 = null;

            for (AABB var14 : this.nearbyBlocks) {
                double var15 = fraction(var14.inflate(var5), var3, var4);
                if (var15 < var9) {
                    var9 = var15;
                    var11 = EndReason.BLOCK;
                }
            }

            for (Entity var18 : this.nearbyTargets) {
                double var19 = fraction(var18.getBoundingBox().inflate(var5), var3, var4);
                if (var19 < var9) {
                    var9 = var19;
                    var11 = EndReason.TARGET;
                    var12 = (LivingEntity)var18;
                }
            }

            this.hitTarget = var12;
            return Double.isFinite(var9) ? new Impact(var9, var11) : null;
        }
    }

    private static double fraction(AABB var0, Vector var1, Vector var2) {
        if (var0.contains(vec(var1))) {
            return 0.0;
        } else {
            Optional var3 = var0.clip(vec(var1), vec(var2));
            return var3.isPresent()
                ? Math.min(1.0, vector((Vec3)var3.get()).subtract(var1).length() / Math.max(1.0E-12, var2.subtract(var1).length()))
                : Double.POSITIVE_INFINITY;
        }
    }

    private static Vector vector(Vec3 var0) {
        return new Vector(var0.x, var0.y, var0.z);
    }

    private static Vec3 vec(Vector var0) {
        return new Vec3(var0.coordinateX(), var0.coordinateY(), var0.coordinateZ());
    }
}
