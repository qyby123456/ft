/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RainbowPoopEntity
extends ThrowableItemProjectile {
    private float dmg = 3.0f;
    private boolean rainbow = false;

    public RainbowPoopEntity(EntityType<? extends RainbowPoopEntity> type, Level level) {
        super(type, level);
    }

    public RainbowPoopEntity(Level level, LivingEntity shooter) {
        super((EntityType)FTEntities.RAINBOW_POOP.get(), shooter, level);
    }

    public void setDamage(float v) {
        this.dmg = v * 0.5f;
    }

    public void setRainbow(boolean v) {
        this.rainbow = v;
        this.m_37446_(new ItemStack((ItemLike)RainbowPoopEntity.itemFor(this.m_9236_().m_213780_().m_188503_(3))));
    }

    private static Item itemFor(int i) {
        return switch (i) {
            case 0 -> Items.f_42537_;
            case 1 -> Items.f_42492_;
            default -> Items.f_42539_;
        };
    }

    protected Item m_7881_() {
        return Items.f_42495_;
    }

    protected void m_5790_(EntityHitResult hit) {
        super.m_5790_(hit);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Entity e = hit.m_82443_();
        Entity owner = this.m_19749_();
        if (e instanceof LivingEntity) {
            Entity entity;
            LivingEntity le = (LivingEntity)e;
            le.m_6469_(this.m_269291_().m_269390_((Entity)this, owner), this.dmg);
            if (le instanceof Player) {
                le.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 0));
                le.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 160, 0));
            }
            if (le instanceof Player && (entity = this.m_19749_()) instanceof CompostBossEntity) {
                CompostBossEntity cb = (CompostBossEntity)entity;
                cb.noteHitOnPlayer();
            }
        }
    }

    protected void m_6532_(HitResult result) {
        super.m_6532_(result);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Vec3 pos = result.m_82450_();
            Fx.particles((Level)sl, pos.f_82479_, pos.f_82480_ + 0.2, pos.f_82481_, 14, (ParticleOptions)ParticleTypes.f_123753_, 0.6, 0.08);
            if (this.rainbow) {
                Fx.particles((Level)sl, pos.f_82479_, pos.f_82480_ + 0.3, pos.f_82481_, 10, (ParticleOptions)ParticleTypes.f_123748_, 0.7, 0.06);
            }
        }
        this.m_146870_();
    }

    public void m_8119_() {
        Level level;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.f_19797_ % 3 == 0 && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 1, (ParticleOptions)(this.rainbow ? ParticleTypes.f_123748_ : ParticleTypes.f_123753_), 0.08, 0.01);
        }
    }
}

