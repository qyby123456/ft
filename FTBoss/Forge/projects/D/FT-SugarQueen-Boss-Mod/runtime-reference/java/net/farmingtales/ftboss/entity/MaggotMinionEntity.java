/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import java.util.UUID;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MaggotMinionEntity
extends Monster
implements GeoAnimatable {
    public static final int LIFE = 600;
    public static final int MAX_STACKS = 10;
    public static final int STACK_TICKS = 600;
    private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.maggot.crawl");
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private UUID ownerId;
    private int life = 600;

    public MaggotMinionEntity(EntityType<? extends MaggotMinionEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 12.0).m_22268_(Attributes.f_22281_, 4.0).m_22268_(Attributes.f_22279_, 0.32).m_22268_(Attributes.f_22277_, 32.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.15, false));
        this.f_21345_.m_25352_(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 12.0f));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    protected void m_8097_() {
        super.m_8097_();
    }

    public void setOwnerBoss(DefectCoreEntity boss) {
        this.ownerId = boss.m_20148_();
    }

    public boolean isOwnedByUuid(UUID id) {
        return this.ownerId != null && this.ownerId.equals(id);
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.ownerId != null) {
            tag.m_128362_("owner", this.ownerId);
        }
        tag.m_128405_("life", this.life);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128403_("owner")) {
            this.ownerId = tag.m_128342_("owner");
        }
        this.life = tag.m_128441_("life") ? tag.m_128451_("life") : 600;
    }

    public boolean m_7327_(Entity target) {
        boolean hit = super.m_7327_(target);
        if (hit && target instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)target;
            Level level = this.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                MobEffectInstance cur = le.m_21124_((MobEffect)FTEffects.INFECTION.get());
                int stacks = cur == null ? 0 : cur.m_19564_() + 1;
                int want = Math.min(stacks + 1, 10);
                le.m_7292_(new MobEffectInstance((MobEffect)FTEffects.INFECTION.get(), 600, want - 1, false, true, true));
                sl.m_6263_(null, le.m_20185_(), le.m_20186_(), le.m_20189_(), SoundEvents.f_11968_, SoundSource.HOSTILE, 1.0f, 1.4f);
                Fx.particles((Level)sl, le.m_20185_(), le.m_20186_() + 1.0, le.m_20189_(), 8, (ParticleOptions)ParticleTypes.f_123753_, 0.3, 0.05);
            }
        }
        return hit;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_9236_().f_46443_) {
            return false;
        }
        if (!(source.m_7639_() instanceof ServerPlayer)) {
            return false;
        }
        return super.m_6469_(source, amount);
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public void m_8119_() {
        Level level;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (--this.life <= 0) {
            this.m_146870_();
            return;
        }
        if (this.life % 20 == 0 && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 1, (ParticleOptions)ParticleTypes.f_123753_, 0.15, 0.01);
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "maggot_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<MaggotMinionEntity> state) {
        state.setAnimation(CRAWL);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

