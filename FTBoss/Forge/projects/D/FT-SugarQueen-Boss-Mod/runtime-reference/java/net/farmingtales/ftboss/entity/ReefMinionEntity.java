/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.util.WaterBlessing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ReefMinionEntity
extends Monster
implements GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private UUID owner;
    private int life = 600;
    private int burstCooldown = 80;

    public ReefMinionEntity(EntityType<? extends ReefMinionEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 24.0).m_22268_(Attributes.f_22281_, 12.0).m_22268_(Attributes.f_22279_, 0.34).m_22268_(Attributes.f_22277_, 40.0);
    }

    public boolean isPuffer() {
        return this.m_6095_() == FTEntities.BRINE_PUFFER.get();
    }

    public UUID ownerId() {
        return this.owner;
    }

    public void setOwnerId(UUID owner) {
        this.owner = owner;
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.3, false));
    }

    public boolean m_6040_() {
        return true;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (source.m_7639_() instanceof TideFishBossEntity || source.m_7639_() instanceof ReefMinionEntity) {
            return false;
        }
        return super.m_6469_(source, amount);
    }

    public boolean m_7327_(Entity target) {
        LivingEntity living;
        if (!(target instanceof LivingEntity) || !BossTargets.valid(living = (LivingEntity)target)) {
            return false;
        }
        boolean hit = super.m_7327_(target);
        if (hit) {
            living.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, 0));
        }
        return hit;
    }

    public void m_8119_() {
        TideFishBossEntity boss;
        Entity ownerEntity;
        ServerLevel server;
        block10: {
            block9: {
                super.m_8119_();
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block9;
                server = (ServerLevel)level;
                if (this.m_6084_()) break block10;
            }
            return;
        }
        WaterBlessing.tick((LivingEntity)this);
        if (--this.life <= 0) {
            this.m_146870_();
            return;
        }
        if (!(this.owner == null || this.f_19797_ % 20 != 0 || (ownerEntity = server.m_8791_(this.owner)) instanceof TideFishBossEntity && (boss = (TideFishBossEntity)ownerEntity).m_6084_())) {
            this.m_146870_();
            return;
        }
        if (this.m_5448_() == null || !this.m_5448_().m_6084_() || !BossTargets.valid(this.m_5448_())) {
            this.m_6710_((LivingEntity)BossTargets.nearest(server, (Entity)this, 36.0));
        }
        if (this.m_5448_() != null && this.m_20096_() && this.f_19797_ % 10 == 0) {
            this.m_20256_(this.m_20184_().m_82520_(0.0, 0.34, 0.0));
        }
        if (this.isPuffer() && --this.burstCooldown <= 0 && this.m_5448_() != null && this.m_20280_((Entity)this.m_5448_()) < 9.0) {
            this.burstCooldown = 100;
            server.m_8767_((ParticleOptions)ParticleTypes.f_123772_, this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_(), 36, 1.5, 0.5, 1.5, 0.02);
            for (ServerPlayer target : server.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(2.5))) {
                if (!BossTargets.valid((Player)target) || !(this.m_20280_((Entity)target) <= 6.25) || !this.m_142582_((Entity)target) || !target.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 14.0f)) continue;
                target.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 60, 0));
            }
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.owner != null) {
            tag.m_128362_("TideOwner", this.owner);
        }
        tag.m_128405_("TideLife", this.life);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.owner = tag.m_128403_("TideOwner") ? tag.m_128342_("TideOwner") : null;
        this.life = tag.m_128441_("TideLife") ? Math.max(1, Math.min(600, tag.m_128451_("TideLife"))) : 600;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "reef", 3, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.reef_minion.swim")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object object) {
        return this.f_19797_;
    }
}

