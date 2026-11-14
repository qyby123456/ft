/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
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
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
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
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BleedLink;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BengerPupEntity
extends Monster
implements GeoAnimatable {
    public static final float MAX_HP = 30.0f;
    public static final int LIFE = 900;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.benger.idle");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.benger.run");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.benger.bite");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.benger.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.benger.death");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private int act = 0;
    private int actionTicks = 0;
    private UUID owner;
    private int packAt = -1;
    private int packImpactIn = -1;
    private double packX;
    private double packZ;
    private double packRadius = 3.5;
    private float packDamage = 24.0f;

    public void packPounce(Player target, int delay, double radius, float damage) {
        if (target == null) {
            return;
        }
        this.packAt = this.f_19797_ + delay;
        this.packImpactIn = -1;
        this.packRadius = radius;
        this.packDamage = damage;
        Vec3 d = new Vec3(target.m_20185_() - this.m_20185_(), 0.0, target.m_20189_() - this.m_20189_());
        if (d.m_82556_() < 1.0E-4) {
            d = new Vec3(0.0, 0.0, 1.0);
        }
        d = d.m_82541_();
        this.packX = d.f_82479_;
        this.packZ = d.f_82481_;
        this.m_6710_((LivingEntity)target);
    }

    public BengerPupEntity(EntityType<? extends BengerPupEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 30.0).m_22268_(Attributes.f_22281_, 6.0).m_22268_(Attributes.f_22279_, 0.5).m_22268_(Attributes.f_22277_, 52.0);
    }

    public void setOwner(UUID id) {
        this.owner = id;
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.6, true));
        this.f_21345_.m_25352_(7, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 16.0f));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    protected SoundEvent m_7515_() {
        return SoundEvents.f_12622_;
    }

    protected SoundEvent m_7975_(DamageSource s) {
        return SoundEvents.f_12621_;
    }

    protected SoundEvent m_5592_() {
        return SoundEvents.f_12618_;
    }

    protected float m_6121_() {
        return 0.7f;
    }

    public float m_6100_() {
        return 1.35f;
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.actionTicks > 0 && --this.actionTicks <= 0) {
            this.act = 0;
        }
        if (this.m_21224_()) {
            this.act = 4;
        }
        if (!this.m_9236_().f_46443_ && this.f_19797_ > 900) {
            this.m_146870_();
        }
        if (!this.m_9236_().f_46443_) {
            Level level;
            if (this.packAt > 0 && this.f_19797_ >= this.packAt) {
                this.packAt = -1;
                this.packImpactIn = 8;
                this.m_20334_(this.packX * 1.0, 0.5, this.packZ * 1.0);
                this.f_19812_ = true;
                this.act = 2;
                this.actionTicks = 14;
            }
            if (this.packImpactIn > 0 && --this.packImpactIn <= 0 && (level = this.m_9236_()) instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                this.packImpact(sl);
            }
        }
    }

    private void packImpact(ServerLevel sl) {
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 10, 0.8, 0.15, 0.8, 0.04);
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20280_((Entity)this) > this.packRadius * this.packRadius) continue;
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.packDamage * 0.5f);
            ExtraDebuffs.bleed((LivingEntity)p, 80, 0);
            ++hits;
        }
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][benger] \u5c0f\u72d7\u6251\u51fb\u547d\u4e2d {} \u4eba", (Object)hits);
        }
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (!(source.m_7639_() instanceof Player) && source.m_7639_() != null) {
            return false;
        }
        boolean hit = super.m_6469_(source, amount);
        if (hit && !this.m_9236_().f_46443_ && this.act == 0 && this.m_217043_().m_188501_() < 0.7f) {
            this.act = 3;
            this.actionTicks = 8;
        }
        return hit;
    }

    public boolean m_7327_(Entity target) {
        boolean hit = super.m_7327_(target);
        if (hit && target instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)target;
            ExtraDebuffs.bleed(le, 100, 0);
            if (le instanceof Player) {
                Player pl1 = (Player)le;
                BleedLink.link(pl1, (LivingEntity)this);
            }
            le.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 30, 0));
            BengerPupEntity.debuff(le, "vulnerable", 200, 0);
            BengerPupEntity.debuff(le, "clipped", 200, 0);
            if (!this.m_9236_().f_46443_) {
                this.act = 2;
                this.actionTicks = 10;
            }
        }
        return hit;
    }

    private static void debuff(LivingEntity target, String path, int ticks, int amp) {
        try {
            MobEffect eff = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", path));
            if (eff == null) {
                return;
            }
            if (target.m_21023_(eff)) {
                target.m_21195_(eff);
            }
            target.m_7292_(new MobEffectInstance(eff, ticks, amp, false, false, true));
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][benger] \u5c0f\u72d7\u51cf\u76ca {} \u65bd\u52a0\u5931\u8d25: {}", (Object)path, (Object)t.toString());
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.owner != null) {
            tag.m_128362_("bengerOwner", this.owner);
        }
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128403_("bengerOwner")) {
            this.owner = tag.m_128342_("bengerOwner");
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "pup_ctrl", 3, this::predicate)});
    }

    private PlayState predicate(AnimationState<BengerPupEntity> state) {
        if (this.act == 4 || this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (this.act == 2) {
            state.setAnimation(BITE);
            return PlayState.CONTINUE;
        }
        if (this.act == 3) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? RUN : IDLE);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

