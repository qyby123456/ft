/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BarrelCannonballEntity
extends Projectile
implements GeoAnimatable {
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    public static final float EXPLOSION_POWER = 3.0f;
    private float explosionPower = 3.0f;
    private static final float DIRECT_DAMAGE = 4.0f;
    private static final int MAX_LIFE = 80;
    private boolean exploded = false;

    public BarrelCannonballEntity(EntityType<? extends BarrelCannonballEntity> type, Level level) {
        super(type, level);
    }

    public BarrelCannonballEntity(Level level, LivingEntity owner) {
        super((EntityType)FTEntities.BARREL_CANNONBALL.get(), level);
        this.m_5602_((Entity)owner);
        this.m_6034_(owner.m_20185_(), owner.m_20186_() + 1.6, owner.m_20189_());
    }

    protected void m_8097_() {
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    protected void m_7378_(CompoundTag tag) {
        this.exploded = tag.m_128471_("ftboss_exploded");
    }

    protected void m_7380_(CompoundTag tag) {
        tag.m_128379_("ftboss_exploded", this.exploded);
    }

    public boolean m_6087_() {
        return false;
    }

    public boolean m_6060_() {
        return false;
    }

    public void setExplosionPower(float power) {
        if (Float.isFinite(power) && power > 0.0f) {
            this.explosionPower = power;
        }
    }

    protected boolean m_5603_(Entity e) {
        if (e instanceof BarrelCannonballEntity) {
            return false;
        }
        Entity owner = this.m_19749_();
        if (owner != null && e == owner) {
            return false;
        }
        return !(e instanceof WineBarrelBossEntity) || !(owner instanceof WineBarrelBossEntity);
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.exploded) {
            return;
        }
        Vec3 motion = this.m_20184_();
        this.m_20334_(motion.f_82479_, motion.f_82480_ - 0.045, motion.f_82481_);
        Vec3 next = this.m_20182_().m_82549_(this.m_20184_());
        if (!this.m_9236_().f_46443_) {
            BlockHitResult blockHit = this.m_9236_().m_45547_(new ClipContext(this.m_20182_(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
            if (blockHit.m_6662_() != HitResult.Type.MISS) {
                this.m_6034_(blockHit.m_82450_().f_82479_, blockHit.m_82450_().f_82480_, blockHit.m_82450_().f_82481_);
                this.explode();
                return;
            }
            for (Entity e : this.m_9236_().m_45933_((Entity)this, this.m_20191_().m_82369_(this.m_20184_()).m_82400_(0.5))) {
                if (!e.m_6084_() || !this.m_5603_(e)) continue;
                if (e instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity)e;
                    float direct = LemonInheritedAbilityRules.inherited((Entity)this) ? LemonInheritedAbilityRules.damage(4.0f) : 4.0f;
                    le.m_6469_(this.m_269291_().m_269390_((Entity)this, this.m_19749_()), direct);
                }
                this.explode();
                return;
            }
        }
        this.m_6034_(next.f_82479_, next.f_82480_, next.f_82481_);
        if (!this.m_9236_().f_46443_ && (this.f_19797_ > 80 || this.m_20096_())) {
            this.explode();
            return;
        }
        if (this.m_9236_().f_46443_) {
            Fx.particles(this.m_9236_(), this.m_20185_(), this.m_20186_(), this.m_20189_(), 3, (ParticleOptions)ParticleTypes.f_123744_, 0.08, 0.01);
        }
    }

    private void explode() {
        if (this.exploded) {
            return;
        }
        this.exploded = true;
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_254849_((Entity)this, this.m_20185_(), this.m_20186_(), this.m_20189_(), this.explosionPower, Level.ExplosionInteraction.NONE);
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 2.0f, 0.9f);
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 25, (ParticleOptions)ParticleTypes.f_123813_, 1.0, 0.1);
            FTBossMod.LOGGER.info("[FTBoss] \u9152\u6876\u70ae\u5f39\u7206\u70b8 @({}, {}, {}) \u5a01\u529b={}", new Object[]{String.format("%.1f", this.m_20185_()), String.format("%.1f", this.m_20186_()), String.format("%.1f", this.m_20189_()), Float.valueOf(this.explosionPower)});
        }
        this.m_146870_();
    }
}

