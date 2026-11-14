/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.youzi.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.farmingtales.ftboss.integ.spells.YouziSpellFlightBridge;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.spells.YouziSpellAssets;
import net.farmingtales.ftboss.youzi.spells.YouziSpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
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
import net.minecraftforge.network.NetworkHooks;

public final class YouziStarProjectile
extends Entity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.m_135353_(YouziStarProjectile.class, (EntityDataSerializer)EntityDataSerializers.f_135029_);
    private UUID owner;
    private UUID lockedTarget;
    private YouziHomingFlight flight;
    private float damage;
    private LivingEntity hitTarget;
    private boolean targetLost;
    private List<AABB> nearbyBlocks = List.of();
    private List<Entity> nearbyTargets = List.of();

    public YouziStarProjectile(EntityType<? extends YouziStarProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public YouziStarProjectile(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2, Vec3 vec3, Vec3 vec32, float f, double d) {
        this(YouziSpellRegistry.star(), (Level)serverLevel);
        this.owner = livingEntity.m_20148_();
        this.lockedTarget = livingEntity2 == null ? null : livingEntity2.m_20148_();
        this.damage = f;
        this.f_19804_.m_135381_(SCALE, (Object)Float.valueOf((float)d));
        this.m_6210_();
        this.m_20343_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        Vec3 vec33 = vec32.m_82546_(vec3).m_82541_();
        this.flight = new YouziHomingFlight(YouziSpellAssets.parameters(d), YouziStarProjectile.vector(vec3), YouziStarProjectile.vector(vec33));
        this.m_20256_(vec33.m_82490_(0.6));
    }

    public double scale() {
        return ((Float)this.f_19804_.m_135370_(SCALE)).floatValue();
    }

    public boolean ownedBy(LivingEntity livingEntity) {
        return livingEntity.m_20148_().equals(this.owner);
    }

    public EntityDimensions m_6972_(Pose pose) {
        float f = this.f_19804_ != null && this.f_19804_.m_285897_(SCALE) ? ((Float)this.f_19804_.m_135370_(SCALE)).floatValue() : 1.0f;
        return EntityDimensions.m_20395_((float)(0.2f * f), (float)(0.2f * f));
    }

    public void m_7350_(EntityDataAccessor<?> entityDataAccessor) {
        super.m_7350_(entityDataAccessor);
        if (entityDataAccessor.equals(SCALE)) {
            this.m_6210_();
        }
    }

    protected void m_8097_() {
        this.f_19804_.m_135372_(SCALE, (Object)Float.valueOf(1.0f));
    }

    protected void m_7378_(CompoundTag compoundTag) {
        this.m_146870_();
    }

    protected void m_7380_(CompoundTag compoundTag) {
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    public void m_8119_() {
        LivingEntity livingEntity;
        VoxelShape voxelShape2;
        LivingEntity livingEntity2;
        ServerLevel serverLevel;
        Level level;
        block12: {
            block11: {
                super.m_8119_();
                if (this.m_9236_().f_46443_) {
                    return;
                }
                level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block11;
                serverLevel = (ServerLevel)level;
                if (this.flight != null && this.f_19797_ <= 30 && serverLevel.m_46805_(BlockPos.m_274561_((double)this.m_20185_(), (double)this.m_20186_(), (double)this.m_20189_()))) break block12;
            }
            this.m_146870_();
            return;
        }
        Entity entity2 = level = this.owner == null ? null : serverLevel.m_8791_(this.owner);
        if (!(level instanceof LivingEntity) || !(livingEntity2 = (LivingEntity)level).m_6084_()) {
            this.m_146870_();
            return;
        }
        if (!serverLevel.isAreaLoaded(BlockPos.m_274561_((double)this.m_20185_(), (double)this.m_20186_(), (double)this.m_20189_()), 4)) {
            this.m_146870_();
            return;
        }
        AABB aABB = new AABB(this.m_20182_(), this.m_20182_()).m_82400_(2.7 + 0.1 * this.scale());
        ArrayList<AABB> arrayList = new ArrayList<AABB>();
        for (VoxelShape voxelShape2 : serverLevel.m_186434_((Entity)this, aABB)) {
            arrayList.addAll(voxelShape2.m_83299_());
        }
        this.nearbyBlocks = arrayList;
        this.nearbyTargets = serverLevel.m_6249_((Entity)this, aABB, entity -> {
            LivingEntity livingEntity2;
            return entity instanceof LivingEntity && YouziSpellFlightBridge.validTarget(livingEntity2, livingEntity2 = (LivingEntity)entity);
        });
        Iterator iterator = this.lockedTarget == null || this.targetLost ? null : serverLevel.m_8791_(this.lockedTarget);
        LivingEntity livingEntity3 = voxelShape2 = iterator instanceof LivingEntity && YouziSpellFlightBridge.validTarget(livingEntity2, livingEntity = (LivingEntity)iterator) ? livingEntity : null;
        if (voxelShape2 == null) {
            this.targetLost = true;
        }
        double d2 = this.flight.snapshot().age();
        this.hitTarget = null;
        this.flight.advance(0.05, arg_0 -> YouziStarProjectile.lambda$m_8119_$1((LivingEntity)voxelShape2, d2, arg_0), (vector, vector2, d) -> this.collision(serverLevel, livingEntity2, vector, vector2, d), false);
        YouziHomingFlight.Snapshot snapshot = this.flight.snapshot();
        this.m_20343_(snapshot.position().coordinateX(), snapshot.position().coordinateY(), snapshot.position().coordinateZ());
        this.m_20256_(YouziStarProjectile.vec(snapshot.heading().multiply(snapshot.speed() / 20.0)));
        if (snapshot.endReason() != YouziHomingFlight.EndReason.ACTIVE) {
            if (this.hitTarget != null && snapshot.endReason() == YouziHomingFlight.EndReason.TARGET) {
                YouziSpellFlightBridge.hit(livingEntity2, this.hitTarget, this.damage);
            }
            if (snapshot.endReason() == YouziHomingFlight.EndReason.TARGET || snapshot.endReason() == YouziHomingFlight.EndReason.BLOCK) {
                SpellVisuals.beam(serverLevel, livingEntity2, "effigy_volley", this.m_20182_(), this.m_20182_());
            }
            this.m_146870_();
        }
    }

    private YouziHomingFlight.Impact collision(ServerLevel serverLevel, LivingEntity livingEntity, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, double d) {
        double d2;
        Vec3 vec3 = YouziStarProjectile.vec(vector);
        Vec3 vec32 = YouziStarProjectile.vec(vector2);
        if (!serverLevel.m_46805_(BlockPos.m_274561_((double)vec32.f_82479_, (double)vec32.f_82480_, (double)vec32.f_82481_))) {
            return new YouziHomingFlight.Impact(0.0, YouziHomingFlight.EndReason.BLOCK);
        }
        double d3 = Double.POSITIVE_INFINITY;
        YouziHomingFlight.EndReason endReason = YouziHomingFlight.EndReason.ACTIVE;
        LivingEntity livingEntity2 = null;
        for (AABB aABB : this.nearbyBlocks) {
            d2 = YouziStarProjectile.fraction(aABB.m_82400_(d), vector, vector2);
            if (!(d2 < d3)) continue;
            d3 = d2;
            endReason = YouziHomingFlight.EndReason.BLOCK;
        }
        for (Entity entity : this.nearbyTargets) {
            d2 = YouziStarProjectile.fraction(entity.m_20191_().m_82400_(d), vector, vector2);
            if (!(d2 < d3)) continue;
            d3 = d2;
            endReason = YouziHomingFlight.EndReason.TARGET;
            livingEntity2 = (LivingEntity)entity;
        }
        this.hitTarget = livingEntity2;
        return Double.isFinite(d3) ? new YouziHomingFlight.Impact(d3, endReason) : null;
    }

    private static double fraction(AABB aABB, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2) {
        if (aABB.m_82390_(YouziStarProjectile.vec(vector))) {
            return 0.0;
        }
        Optional optional = aABB.m_82371_(YouziStarProjectile.vec(vector), YouziStarProjectile.vec(vector2));
        return optional.isPresent() ? Math.min(1.0, YouziStarProjectile.vector((Vec3)optional.get()).subtract(vector).length() / Math.max(1.0E-12, vector2.subtract(vector).length())) : Double.POSITIVE_INFINITY;
    }

    private static YouziHomingFlight.Vector vector(Vec3 vec3) {
        return new YouziHomingFlight.Vector(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
    }

    private static Vec3 vec(YouziHomingFlight.Vector vector) {
        return new Vec3(vector.coordinateX(), vector.coordinateY(), vector.coordinateZ());
    }

    private static /* synthetic */ YouziHomingFlight.Target lambda$m_8119_$1(LivingEntity livingEntity, double d, double d2) {
        return livingEntity == null ? null : new YouziHomingFlight.Target(YouziStarProjectile.vector(livingEntity.m_20191_().m_82399_()).add(YouziStarProjectile.vector(livingEntity.m_20184_()).multiply(20.0 * (d2 - d))), YouziStarProjectile.vector(livingEntity.m_20184_()).multiply(20.0), 0.0);
    }
}

