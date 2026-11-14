/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.monster.MagmaCube
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SheepDungEntity
extends MagmaCube {
    public static final int LIFE = 900;
    private static final float BLAST_RADIUS = 0.95f;
    private static final double HURT_RANGE = 3.0;
    private boolean blasted = false;

    public SheepDungEntity(EntityType<? extends SheepDungEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder m_33000_() {
        return Monster.m_33035_();
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_ && this.f_19797_ > 900) {
            this.m_146870_();
        }
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        this.blast();
    }

    private void blast() {
        if (this.m_9236_().f_46443_ || this.blasted) {
            return;
        }
        this.blasted = true;
        double x = this.m_20185_();
        double y = this.m_20186_() + 0.25;
        double z = this.m_20189_();
        this.m_9236_().m_254849_((Entity)this, x, y, z, 0.95f, Level.ExplosionInteraction.NONE);
        this.m_9236_().m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 0.9f, 1.5f);
        Fx.particles(this.m_9236_(), x, y, z, 18, (ParticleOptions)ParticleTypes.f_123753_, 0.9, 0.12);
        Fx.particles(this.m_9236_(), x, y, z, 8, (ParticleOptions)ParticleTypes.f_123755_, 0.7, 0.06);
        for (Player p : this.m_9236_().m_6907_()) {
            if (!p.m_6084_() || p.m_20275_(x, y, z) > 9.0) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 2.5f);
        }
    }

    public boolean m_6785_(double distance) {
        return false;
    }
}

