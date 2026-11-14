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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiSigilEntity
extends Projectile {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.m_135353_(QizaiSigilEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.m_135353_(QizaiSigilEntity.class, (EntityDataSerializer)EntityDataSerializers.f_244073_);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.m_135353_(QizaiSigilEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> RELEASED = SynchedEntityData.m_135353_(QizaiSigilEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private int remaining = 1;

    public QizaiSigilEntity(EntityType<? extends QizaiSigilEntity> type, Level level) {
        super(type, level);
    }

    public QizaiSigilEntity(ServerLevel level, LivingEntity owner, Vec3 position, int kind, int duration) {
        this((EntityType<? extends QizaiSigilEntity>)((EntityType)FTEntities.QIZAI_SIGIL.get()), (Level)level);
        this.m_5602_((Entity)owner);
        this.m_146884_(position);
        this.f_19804_.m_135381_(KIND, (Object)kind);
        this.f_19804_.m_135381_(START, (Object)level.m_46467_());
        this.f_19804_.m_135381_(DURATION, (Object)Math.max(1, Math.min(1200, duration)));
        this.remaining = this.duration() + 20;
        ManorFightMembers.inherit((Entity)owner, (Entity)this);
    }

    protected void m_8097_() {
        this.f_19804_.m_135372_(KIND, (Object)0);
        this.f_19804_.m_135372_(START, (Object)0L);
        this.f_19804_.m_135372_(DURATION, (Object)1);
        this.f_19804_.m_135372_(RELEASED, (Object)false);
    }

    public int kind() {
        return (Integer)this.f_19804_.m_135370_(KIND);
    }

    public int duration() {
        return (Integer)this.f_19804_.m_135370_(DURATION);
    }

    public boolean released() {
        return (Boolean)this.f_19804_.m_135370_(RELEASED);
    }

    public double progress(float partial) {
        return Math.max(0.0f, Math.min(1.0f, ((float)(this.m_9236_().m_46467_() - (Long)this.f_19804_.m_135370_(START)) + partial) / (float)this.duration()));
    }

    public void release() {
        this.f_19804_.m_135381_(RELEASED, (Object)true);
        this.f_19804_.m_135381_(START, (Object)this.m_9236_().m_46467_());
        this.f_19804_.m_135381_(DURATION, (Object)12);
        this.remaining = 12;
    }

    public void m_8119_() {
        Mob mob;
        LivingEntity owner;
        Entity entity;
        super.m_8119_();
        if (!this.m_9236_().f_46443_ && (!((entity = this.m_19749_()) instanceof LivingEntity) || !(owner = (LivingEntity)entity).m_6084_() || owner.m_213877_() || owner.m_5833_() || owner.m_9236_() != this.m_9236_() || owner instanceof Mob && (mob = (Mob)owner).m_21525_() || --this.remaining <= 0)) {
            this.m_146870_();
        }
    }

    protected void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
    }

    protected void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.remaining = 1;
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

