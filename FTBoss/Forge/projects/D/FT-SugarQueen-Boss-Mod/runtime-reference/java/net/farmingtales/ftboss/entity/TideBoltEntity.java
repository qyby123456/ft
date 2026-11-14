/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TideBoltEntity
extends ThrowableItemProjectile {
    private float damage = 8.0f;

    public TideBoltEntity(EntityType<? extends TideBoltEntity> type, Level level) {
        super(type, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    protected Item m_7881_() {
        return Items.f_42695_;
    }

    protected float m_7139_() {
        return 0.0f;
    }

    protected boolean m_5603_(Entity entity) {
        Player player;
        LivingEntity living;
        if (!(super.m_5603_(entity) && entity instanceof LivingEntity && (living = (LivingEntity)entity).m_6084_())) {
            return false;
        }
        Entity entity2 = this.m_19749_();
        if (entity2 instanceof Player) {
            Player other;
            Player player2 = (Player)entity2;
            return !player2.m_7307_(entity) && (!(entity instanceof Player) || BossTargets.valid(other = (Player)entity) && player2.m_7099_(other));
        }
        return entity instanceof Player && BossTargets.valid(player = (Player)entity);
    }

    protected void m_5790_(EntityHitResult result) {
        Entity entity;
        if (!(this.m_9236_() instanceof ServerLevel) || !((entity = result.m_82443_()) instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity)entity;
        Entity owner = this.m_19749_();
        if (target.m_6469_(this.m_269291_().m_269390_((Entity)this, owner), this.damage)) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 0));
        }
    }

    protected void m_6532_(HitResult result) {
        super.m_6532_(result);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            server.m_8767_((ParticleOptions)ParticleTypes.f_123769_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 12, 0.2, 0.2, 0.2, 0.1);
            this.m_146870_();
        }
    }

    public void m_8119_() {
        super.m_8119_();
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            if (this.f_19797_ > 60 || this.m_19749_() == null || !this.m_19749_().m_6084_()) {
                this.m_146870_();
                return;
            }
            if (this.f_19797_ % 2 == 0) {
                server.m_8767_((ParticleOptions)ParticleTypes.f_123769_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 2, 0.05, 0.05, 0.05, 0.0);
            }
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128350_("TideDamage", this.damage);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.damage = tag.m_128441_("TideDamage") ? tag.m_128457_("TideDamage") : 8.0f;
    }
}

