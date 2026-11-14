/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class PlayerMoonChakramEntity
extends Projectile {
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.m_135353_(PlayerMoonChakramEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private final Set<UUID> outboundHits = new HashSet<UUID>();
    private final Set<UUID> returnHits = new HashSet<UUID>();
    private boolean reloaded;
    private boolean recovered;
    private Vec3 interpolationTarget;
    private int interpolationTicks;
    private InteractionHand launchHand = InteractionHand.MAIN_HAND;

    public PlayerMoonChakramEntity(EntityType<? extends PlayerMoonChakramEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    protected void m_8097_() {
        this.f_19804_.m_135372_(RETURNING, (Object)false);
    }

    public boolean isReturning() {
        return (Boolean)this.f_19804_.m_135370_(RETURNING);
    }

    public void setLaunchHand(InteractionHand hand) {
        this.launchHand = hand;
    }

    public void m_8119_() {
        Vec3 requested;
        Vec3 endpoint;
        ServerPlayer owner;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            if (this.interpolationTicks > 0) {
                this.m_146884_(this.m_20182_().m_165921_(this.interpolationTarget, 1.0 / (double)this.interpolationTicks));
                --this.interpolationTicks;
            } else {
                this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
            }
            return;
        }
        Entity entity = this.m_19749_();
        if (!(entity instanceof ServerPlayer) || !(owner = (ServerPlayer)entity).m_6084_() || owner.m_5833_() || owner.m_9236_() != this.m_9236_() || this.reloaded || this.f_19797_ > 60 || this.m_20280_((Entity)owner) > 4096.0 || !XuanlanCombat.isTrackedBlade(owner, this)) {
            this.m_146870_();
            return;
        }
        ServerLevel level = owner.m_284548_();
        if (this.f_19797_ > 10) {
            this.f_19804_.m_135381_(RETURNING, (Object)true);
        }
        Vec3 from = this.m_20182_();
        if (this.isReturning()) {
            Vec3 destination = owner.m_146892_().m_82520_(0.0, -0.2, 0.0);
            Vec3 offset = destination.m_82546_(from);
            if (offset.m_82553_() < 0.8) {
                this.recovered = true;
                XuanlanFxPacket.send(level, XuanlanMotionRules.catchKind(XuanlanCombat.bladeSlot(owner, this), this.launchHand == InteractionHand.OFF_HAND), owner.m_19879_(), this.m_20182_(), destination);
                level.m_5594_(null, owner.m_20183_(), SoundEvents.f_12516_, SoundSource.PLAYERS, 0.5f, 1.4f);
                this.m_146870_();
                return;
            }
            this.m_20256_(offset.m_82541_().m_82490_(Math.min(1.6, offset.m_82553_())));
        }
        boolean blocked = (endpoint = XuanlanCombat.clip(level, (Entity)this, from, requested = from.m_82549_(this.m_20184_()))).m_82557_(requested) > 1.0E-4;
        Set<UUID> hits = this.isReturning() ? this.returnHits : this.outboundHits;
        for (LivingEntity target : level.m_6443_(LivingEntity.class, new AABB(from, endpoint).m_82400_(0.25), candidate -> XuanlanCombat.canHit((Player)owner, (Entity)candidate))) {
            if (hits.size() >= 16) break;
            if (hits.contains(target.m_20148_()) || !target.m_20191_().m_82390_(from) && target.m_20191_().m_82371_(from, endpoint).isEmpty()) continue;
            hits.add(target.m_20148_());
            boolean marked = XuanlanCombat.markedBy(target, (Player)owner);
            if (!target.m_6469_(level.m_269111_().m_269299_((Entity)this, (LivingEntity)owner), XuanlanRules.bladeDamage(this.isReturning(), marked))) continue;
            if (marked) {
                XuanlanCombat.consumeMark(target, owner);
            } else {
                XuanlanFxPacket.send(level, 5, target.m_19879_(), target.m_20191_().m_82399_(), endpoint);
            }
            level.m_5594_(null, target.m_20183_(), SoundEvents.f_144245_, SoundSource.PLAYERS, 0.6f, marked ? 1.7f : 1.2f);
        }
        this.m_146884_(endpoint);
        this.m_37283_();
        if (blocked) {
            if (this.isReturning()) {
                this.m_146870_();
            } else {
                this.m_146884_(endpoint.m_82546_(this.m_20184_().m_82541_().m_82490_(0.05)));
                this.f_19804_.m_135381_(RETURNING, (Object)true);
            }
        }
    }

    public void m_142687_(Entity.RemovalReason reason) {
        Level level;
        if (!this.m_213877_() && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerPlayer owner;
            int slot;
            ServerLevel serverLevel = (ServerLevel)level;
            level = this.m_19749_();
            if (level instanceof ServerPlayer && (slot = XuanlanCombat.forgetBlade(owner = (ServerPlayer)level, this)) >= 0 && !this.recovered && owner.m_9236_() == serverLevel) {
                XuanlanFxPacket.send(serverLevel, XuanlanMotionRules.lostKind(slot, this.launchHand == InteractionHand.OFF_HAND), owner.m_19879_(), this.m_20182_(), owner.m_20182_());
            }
        }
        super.m_142687_(reason);
    }

    public void m_6453_(double worldX, double worldY, double worldZ, float yaw, float pitch, int steps, boolean teleport) {
        this.interpolationTarget = new Vec3(worldX, worldY, worldZ);
        this.interpolationTicks = Math.max(1, Math.min(3, steps));
        this.m_146922_(yaw);
        this.m_146926_(pitch);
    }

    protected void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
    }

    protected void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.reloaded = true;
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

