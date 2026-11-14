/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShadowPhantomEntity
extends PathfinderMob {
    public static final int DEFAULT_LIFE = 30;
    public static final float WIDTH = 0.6f;
    public static final float HEIGHT = 1.8f;
    private int life = 30;

    public ShadowPhantomEntity(EntityType<? extends ShadowPhantomEntity> type, Level level) {
        super(type, level);
        this.f_19794_ = true;
        this.m_20242_(true);
        this.m_20225_(true);
        this.m_20331_(true);
        this.f_21364_ = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 1.0).m_22268_(Attributes.f_22281_, 0.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0);
    }

    public void setLife(int ticks) {
        this.life = Math.max(1, ticks);
    }

    public int getLife() {
        return this.life;
    }

    protected void m_8099_() {
    }

    public boolean m_6779_(LivingEntity target) {
        return false;
    }

    public boolean m_7327_(Entity target) {
        return false;
    }

    public boolean m_6094_() {
        return false;
    }

    public boolean m_5829_() {
        return false;
    }

    public void m_7334_(Entity entity) {
    }

    protected void m_7324_(Entity entity) {
    }

    public void m_147240_(double strength, double x, double z) {
    }

    public void m_5997_(double x, double y, double z) {
    }

    public void m_6478_(MoverType type, Vec3 movement) {
        if (this.f_19797_ == 0) {
            super.m_6478_(type, movement);
        }
    }

    public boolean m_6087_() {
        return false;
    }

    public boolean m_6097_() {
        return false;
    }

    public boolean m_6673_(DamageSource source) {
        return true;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    public boolean m_142535_(float distance, float multiplier, DamageSource source) {
        return false;
    }

    protected void m_7472_(DamageSource source, int looting, boolean recentlyHit) {
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (--this.life <= 0) {
            this.m_146870_();
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("PhantomLife", this.life);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("PhantomLife")) {
            this.life = Math.max(1, tag.m_128451_("PhantomLife"));
        }
    }
}

