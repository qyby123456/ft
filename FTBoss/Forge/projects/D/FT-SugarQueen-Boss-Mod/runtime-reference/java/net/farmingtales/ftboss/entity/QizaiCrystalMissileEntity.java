/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.entity;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiCrystalMissileEntity
extends Projectile {
    private UUID crystalId;
    private UUID targetId;
    private int lifetime = 70;

    public QizaiCrystalMissileEntity(EntityType<? extends QizaiCrystalMissileEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public QizaiCrystalMissileEntity(ServerLevel level, QizaiCrystalEntity crystal, EasterBossEntity owner, LivingEntity target) {
        this((EntityType<? extends QizaiCrystalMissileEntity>)((EntityType)FTEntities.QIZAI_CRYSTAL_MISSILE.get()), (Level)level);
        this.m_5602_((Entity)owner);
        this.crystalId = crystal.m_20148_();
        this.targetId = target.m_20148_();
        ManorFightMembers.inherit((Entity)owner, (Entity)this);
    }

    protected void m_8097_() {
    }

    public void m_8119_() {
        super.m_8119_();
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            Entity target;
            QizaiCrystalEntity crystal;
            EasterBossEntity owner;
            ServerLevel level2 = (ServerLevel)level;
            Entity entity = this.m_19749_();
            if (!(entity instanceof EasterBossEntity && QizaiCrystals.validOwner(owner = (EasterBossEntity)entity) && this.crystalId != null && (entity = level2.m_8791_(this.crystalId)) instanceof QizaiCrystalEntity && (crystal = (QizaiCrystalEntity)entity).m_6084_() && !crystal.m_21525_() && QizaiCrystals.current(crystal) && QizaiCrystals.combatTarget(owner) != null && --this.lifetime > 0)) {
                this.m_146870_();
                return;
            }
            Entity entity2 = target = this.targetId == null ? null : level2.m_8791_(this.targetId);
            if (!QizaiCrystals.target(owner, target)) {
                this.m_146870_();
                return;
            }
            Vec3 desired = target.m_146892_().m_82546_(this.m_20182_()).m_82541_().m_82490_(0.8);
            this.m_20256_(this.m_20184_().m_82490_(0.85).m_82549_(desired.m_82490_(0.15)).m_82541_().m_82490_(0.8));
            HitResult hit = ProjectileUtil.m_278158_((Entity)this, this::m_5603_);
            if (hit.m_6662_() != HitResult.Type.MISS) {
                if (hit instanceof EntityHitResult) {
                    EntityHitResult contact = (EntityHitResult)hit;
                    Entity entity3 = contact.m_82443_();
                    if (entity3 instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)entity3;
                        living.f_19802_ = 0;
                    }
                    contact.m_82443_().m_6469_(level2.m_269111_().m_269104_((Entity)this, (Entity)owner), 24.0f);
                }
                level2.m_8767_((ParticleOptions)ParticleTypes.f_123810_, hit.m_82450_().f_82479_, hit.m_82450_().f_82480_, hit.m_82450_().f_82481_, 8, 0.15, 0.15, 0.15, 0.05);
                this.m_146870_();
                return;
            }
        }
        this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
        this.m_37283_();
    }

    protected boolean m_5603_(Entity entity) {
        EasterBossEntity owner;
        Entity entity2 = this.m_19749_();
        return entity2 instanceof EasterBossEntity && QizaiCrystals.target(owner = (EasterBossEntity)entity2, entity) && super.m_5603_(entity);
    }

    protected void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
    }

    protected void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.lifetime = 0;
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

