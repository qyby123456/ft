/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.joml.Vector3f
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.bengeranimation.BengerLocomotion;
import net.farmingtales.ftboss.bengeranimation.BengerSkillSync;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BleedLink;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.DogPack;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.BengerPupEntity;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BengerEntity
extends Monster
implements GeoAnimatable {
    public static final float MAX_HP = 330.0f;
    public static final int T_BITE = 10;
    public static final int T_POUNCE = 14;
    public static final int T_ROAR = 20;
    public static final int T_HURT = 8;
    public static final int T_DEATH = 24;
    private static final int PACK_CD = 280;
    private static final int PACK_LEAD = 30;
    private static final double PACK_RANGE = 20.0;
    private static final float PACK_DAMAGE = 36.0f;
    private static final double PACK_RADIUS = 3.5;
    private static final int SLAM_CD = 340;
    private static final int SLAM_COUNT = 3;
    private static final int SLAM_GAP = 22;
    private static final int SLAM_HANG = 8;
    private static final float SLAM_DAMAGE = 39.0f;
    private static final double SLAM_RADIUS = 4.0;
    private static final int ROAR_CD = 320;
    private static final double ROAR_RADIUS = 14.0;
    private static final int CLAMP_CD = 320;
    private static final double CLAMP_RANGE = 5.5;
    private static final int CLAMP_TICKS = 60;
    private static final float CLAMP_DPS = 20.0f;
    private static final float CLAMP_HEAL = 16.0f;
    private static final int CHARGE_CD = 380;
    private static final double CHARGE_MAX = 22.0;
    private static final float CHARGE_DAMAGE = 34.0f;
    private static final int CHARGE_STUN = 40;
    private static final int BARK_CD = 400;
    private static final double BARK_RADIUS = 14.0;
    private static final int BARK_STUN = 50;
    private static final int MILK_CD = 560;
    private static final int MILK_TICKS = 400;
    private int phase = 1;
    private static final int SWEEP_CD = 220;
    private static final double SWEEP_RANGE = 5.5;
    private static final double SWEEP_RADIUS = 4.2;
    private static final float SWEEP_DAMAGE = 33.0f;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.benger.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.benger.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.benger.run");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.benger.bite");
    private static final RawAnimation POUNCE = RawAnimation.begin().thenPlay("animation.benger.pounce");
    private static final RawAnimation ROAR = RawAnimation.begin().thenPlay("animation.benger.roar");
    private static final RawAnimation SWEEP = RawAnimation.begin().thenPlay("animation.benger.sweep");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.benger.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.benger.death");
    private static final int ACT_NONE = 0;
    private static final int ACT_BITE = 1;
    private static final int ACT_PACK = 2;
    private static final int ACT_ROAR = 3;
    private static final int ACT_HURT = 4;
    private static final int ACT_DEATH = 5;
    private static final int ACT_SLAM = 6;
    private static final int ACT_LEAP = 7;
    private static final int ACT_SWEEP = 8;
    private static final int ACT_BARK = 9;
    private static final int ACT_MILK = 10;
    public static final int PUP_CAP = 8;
    private static final EntityDataAccessor<Boolean> DATA_RAGED = SynchedEntityData.m_135353_(BengerEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final ServerBossEvent bossBar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
    private int act = 0;
    private int actionTicks = 0;
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(BengerEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private int lastSyncedAction = 0;
    private int lastSyncedTicks;
    private int packCd = 120;
    private int roarCd = 200;
    private int sweepCd = 120;
    private int slamCd = 240;
    private int pounceFired = 0;
    private int roarFired = 0;
    private int sweepFired = 0;
    private int packPounceAt = -1;
    private double jumpX;
    private double jumpZ;
    private int slamLeft = 0;
    private int slamTimer = 0;
    private boolean slamAirborne = false;
    private double slamX;
    private double slamY;
    private double slamZ;
    private int growlCd = 0;
    private static final int INTANG_CD = 500;
    private static final int INTANG_TICKS = 60;
    private int intangCd = 500;
    private int intangLeft = 0;
    private int barkCd = 200;
    private int milkCd = 320;
    private int clampCd = 280;
    private int clampTicks = 0;
    private int clampHealTick = 0;
    private Player clampTarget = null;
    private double clampX;
    private double clampY;
    private double clampZ;
    private int chargeCd = 200;
    private int chargeTicks = 0;
    private int chargeFired = 0;
    private double chargeX;
    private double chargeZ;
    private final Map<UUID, Integer> stunTicks = new HashMap<UUID, Integer>();
    private final Map<UUID, Vec3> stunAnchor = new HashMap<UUID, Vec3>();
    private static final double CHARGE_MIN = 3.5;

    public BengerEntity(EntityType<? extends BengerEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 330.0).m_22268_(Attributes.f_22281_, 30.0).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22284_, 4.0).m_22268_(Attributes.f_22285_, 4.0).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 48.0);
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_RAGED, (Object)false);
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
    }

    public boolean isRaged() {
        return (Boolean)this.f_19804_.m_135370_(DATA_RAGED);
    }

    private void setRaged(boolean v) {
        this.f_19804_.m_135381_(DATA_RAGED, (Object)v);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.25, true));
        this.f_21345_.m_25352_(6, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0));
        this.f_21345_.m_25352_(7, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 24.0f));
        this.f_21345_.m_25352_(8, (Goal)new RandomLookAroundGoal((Mob)this));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    protected SoundEvent m_7515_() {
        return SoundEvents.f_12619_;
    }

    protected SoundEvent m_7975_(DamageSource s) {
        return SoundEvents.f_12621_;
    }

    protected SoundEvent m_5592_() {
        return SoundEvents.f_12618_;
    }

    protected float m_6121_() {
        return 1.3f;
    }

    public float m_6100_() {
        return 0.72f;
    }

    public boolean m_6785_(double dist) {
        return false;
    }

    public void m_8119_() {
        Level level;
        super.m_8119_();
        if (this.actionTicks > 0 && --this.actionTicks <= 0) {
            this.act = 0;
        }
        if (this.m_21224_()) {
            this.act = 5;
        }
        if ((level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossDamageGate.tick((LivingEntity)this);
            BossRage.tickBoss((LivingEntity)this);
            TrapBreaker.tick((LivingEntity)this);
            BossUnstuck.tick((LivingEntity)this);
            BossMusic.request(sl, (LivingEntity)this);
            this.bossBar.m_142711_(Math.max(0.0f, this.m_21223_() / this.m_21233_()));
            this.intangTick(sl);
            BengerSkillSync.beforeClamp(this.actionState, (LivingEntity)this, this.clampTicks, (LivingEntity)this.clampTarget);
            this.clampTick(sl);
            this.chargeTick(sl);
            this.stunTick(sl);
            this.skillTick(sl);
            if (this.act != this.lastSyncedAction || this.actionTicks > this.lastSyncedTicks) {
                Integer.toString(this.act);
            }
            this.lastSyncedAction = this.act;
            this.lastSyncedTicks = this.actionTicks;
        }
        BengerSkillSync.afterTick(this.actionState, (LivingEntity)this, this.act, this.actionTicks, this.clampTicks, this.chargeTicks, this.slamLeft, this.slamTimer, this.slamAirborne, this.packPounceAt, this.m_9236_().f_46443_, this.m_21224_(), this.slamX, this.slamY, this.slamZ);
        this.syncIntangibleState();
    }

    private void actionResolve(ServerLevel sl) {
        switch (this.act) {
            case 2: {
                if (this.pounceFired != 0 || this.actionTicks > 12) break;
                this.pounceFired = 1;
                this.summonPups(sl, this.phase == 2 ? 6 : 3);
                sl.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.6f, 1.4f);
                this.lockPack(sl);
                this.playSoundLocal(sl, SoundEvents.f_12620_, 1.8f, 0.62f);
                FTBossMod.LOGGER.info("[FTBoss][benger] \u72fc\u7fa4\u6251\u51fb\uff1a\u957f\u55e5\u53ec\u72d7 + \u9501\u5b9a\u73a9\u5bb6\uff08{} tick \u540e\u5168\u4f53\u6251\u51fa\uff09", (Object)30);
                break;
            }
            case 9: {
                if (this.roarFired != 0 || this.actionTicks > 12) break;
                this.roarFired = 1;
                this.barkAoe(sl);
                break;
            }
            case 10: {
                if (this.roarFired != 0 || this.actionTicks > 12) break;
                this.roarFired = 1;
                this.drinkMilk(sl);
                break;
            }
            case 7: {
                if (this.pounceFired != 0 || this.actionTicks > 8) break;
                this.pounceFired = 1;
                this.packImpact(sl);
                break;
            }
            case 1: 
            case 8: {
                if (this.sweepFired != 0 || this.actionTicks > 4) break;
                this.sweepFired = 1;
                this.sweepDamage(sl);
                break;
            }
            case 3: {
                if (this.roarFired != 0 || this.actionTicks > 12) break;
                this.roarFired = 1;
                this.roarEffect(sl);
                break;
            }
        }
    }

    private void startPackPounce(ServerLevel sl, LivingEntity target) {
        this.act = 3;
        this.actionTicks = 20;
        this.pounceFired = 0;
        this.packCd = 280;
        this.packPounceAt = this.f_19797_ + 30;
        this.playSoundLocal(sl, SoundEvents.f_12619_, 1.5f, 0.85f);
    }

    private void lockPack(ServerLevel sl) {
        Player p;
        Player target;
        LivingEntity livingEntity = this.m_5448_();
        Player player = target = livingEntity instanceof Player ? (p = (Player)livingEntity) : null;
        if (target == null) {
            return;
        }
        int n = 0;
        this.lockSelf(target);
        if (target instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)target;
            DogPack.launchAll(sl, sp, 30);
        }
        for (BengerPupEntity pup : sl.m_45976_(BengerPupEntity.class, this.m_20191_().m_82400_(48.0))) {
            if (!pup.m_6084_()) continue;
            pup.packPounce(target, 30, 3.5, 36.0f);
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] \u72fc\u7fa4\u9501\u5b9a\uff1abenger + {} \u53ea\u5c0f\u72d7 \u540c\u65f6\u6251\u5411 {}", (Object)n, (Object)target.m_7755_().getString());
    }

    private void lockSelf(Player target) {
        Vec3 d = new Vec3(target.m_20185_() - this.m_20185_(), 0.0, target.m_20189_() - this.m_20189_());
        if (d.m_82556_() < 1.0E-4) {
            d = new Vec3(0.0, 0.0, 1.0);
        }
        d = d.m_82541_();
        this.jumpX = d.f_82479_;
        this.jumpZ = d.f_82481_;
    }

    private void selfPackLeap(ServerLevel sl) {
        if (this.packPounceAt < 0) {
            return;
        }
        this.packPounceAt = -1;
        this.m_20334_(this.jumpX * 1.05, 0.55, this.jumpZ * 1.05);
        this.f_19812_ = true;
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12619_, SoundSource.HOSTILE, 1.4f, 0.9f);
    }

    private void packImpact(ServerLevel sl) {
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 0.3, this.m_20189_(), 20, 1.4, 0.2, 1.4, 0.05);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 1.0f, 1.5f);
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20280_((Entity)this) > 12.25) continue;
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 36.0f);
            this.applyBleedStack((LivingEntity)p, 160);
            BossKnockback.knockback((LivingEntity)p, 1.0, this.jumpX, this.jumpZ);
            ++hits;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] \u96c6\u4f53\u6251\u51fb\u843d\u70b9\u547d\u4e2d {} \u4eba\uff08{} \u4f24\u5bb3 + \u6d41\u8840\uff09", (Object)hits, (Object)36);
    }

    private void startSlam(ServerLevel sl, LivingEntity target) {
        if (target == null) {
            return;
        }
        this.slamLeft = 3;
        this.slamTimer = 8;
        this.slamAirborne = false;
        this.slamX = target.m_20185_();
        this.slamY = target.m_20186_();
        this.slamZ = target.m_20189_();
        this.slamCd = 340;
        this.playSoundLocal(sl, SoundEvents.f_12619_, 1.6f, 1.1f);
        FTBossMod.LOGGER.info("[FTBoss][benger] \u8fde\u7eed\u4e0b\u7838\u8d77\u624b\uff1a\u8fde\u7838 {} \u6b21\uff0c\u9996\u6b21\u843d\u70b9 ({}, {}, {})", new Object[]{3, String.format("%.1f", this.slamX), String.format("%.1f", this.slamY), String.format("%.1f", this.slamZ)});
    }

    private void slamTick(ServerLevel sl) {
        if (--this.slamTimer > 0) {
            if (this.slamAirborne) {
                LivingEntity t = this.m_5448_();
                if (t != null) {
                    this.slamX = t.m_20185_();
                    this.slamY = t.m_20186_();
                    this.slamZ = t.m_20189_();
                }
                double r = 4.0;
                for (int a = 0; a < 24; ++a) {
                    double ang = (double)a / 24.0 * Math.PI * 2.0 + (double)this.f_19797_ * 0.05;
                    sl.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.slamX + Math.cos(ang) * r, this.slamY + 0.15, this.slamZ + Math.sin(ang) * r, 1, 0.0, 0.0, 0.0, 0.0);
                }
                sl.m_8767_((ParticleOptions)ParticleTypes.f_123762_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 3, 0.2, 0.1, 0.2, 0.01);
            }
            return;
        }
        if (!this.slamAirborne) {
            this.slamAirborne = true;
            this.slamTimer = 8;
            this.m_6021_(this.slamX, this.slamY + 9.0, this.slamZ);
            this.m_20334_(0.0, 0.0, 0.0);
            this.f_19789_ = 0.0f;
            this.playSoundLocal(sl, SoundEvents.f_12620_, 1.4f, 1.25f);
        } else {
            this.slamAirborne = false;
            this.m_6021_(this.slamX, this.slamY, this.slamZ);
            this.m_20334_(0.0, 0.0, 0.0);
            this.f_19789_ = 0.0f;
            this.slamImpact(sl);
            BengerSkillSync.landed(this.actionState);
            this.slamTimer = --this.slamLeft > 0 ? 22 : 0;
        }
    }

    private void slamImpact(ServerLevel sl) {
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123812_, this.slamX, this.slamY + 0.3, this.slamZ, 1, 0.0, 0.0, 0.0, 0.0);
        Fx.particles((Level)sl, this.slamX, this.slamY + 0.3, this.slamZ, 40, (ParticleOptions)ParticleTypes.f_123796_, 4.0, 0.15);
        sl.m_6263_(null, this.slamX, this.slamY, this.slamZ, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.6f, 0.6f);
        sl.m_6263_(null, this.slamX, this.slamY, this.slamZ, SoundEvents.f_11668_, SoundSource.HOSTILE, 1.3f, 0.5f);
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20275_(this.slamX, this.slamY, this.slamZ) > 16.0) continue;
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 39.0f);
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 80, 1));
            this.addTremor(p);
            BossKnockback.push((Entity)p, 0.0, 0.55, 0.0);
            ++hits;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] \u4e0b\u7838\u7b2c {} \u51fb\u843d\u70b9\u547d\u4e2d {} \u4eba\uff08{} \u4f24\u5bb3\uff09", new Object[]{3 - this.slamLeft + 1, hits, 39});
    }

    private void sweepDamage(ServerLevel sl) {
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20280_((Entity)this) > 17.64) continue;
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 33.0f);
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 0));
            BossKnockback.knockback((LivingEntity)p, 1.3, p.m_20185_() - this.m_20185_(), p.m_20189_() - this.m_20189_());
            ++hits;
        }
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123766_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 6, 1.6, 0.4, 1.6, 0.02);
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][benger] \u6253\u72d7\u68d2\u6a2a\u626b\u547d\u4e2d {} \u4eba\uff08{} \u4f24\u5bb3 + \u51fb\u98de\uff09", (Object)hits, (Object)33);
        }
    }

    private void roarEffect(ServerLevel sl) {
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12620_, SoundSource.HOSTILE, 2.2f, 0.62f);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123758_, this.m_20185_(), this.m_20186_() + 2.2, this.m_20189_(), 24, 0.9, 0.6, 0.9, 0.2);
        this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 160, 1));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 160, 0));
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20280_((Entity)this) > 196.0) continue;
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 120, 1));
            p.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 120, 0));
            BossKnockback.knockback((LivingEntity)p, 0.7, p.m_20185_() - this.m_20185_(), p.m_20189_() - this.m_20189_());
            ++hits;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] \u957f\u55e5\uff1a\u5f71\u54cd {} \u540d\u73a9\u5bb6", (Object)hits);
    }

    private void summonPups(ServerLevel sl, int n) {
        int made = DogPack.summon(sl, (LivingEntity)this, this.m_20185_(), this.m_20186_(), this.m_20189_(), n);
        if (made > 0) {
            sl.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.5f, 1.5f);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u5524\u6765 {} \u53ea\u6574\u5408\u5305\u91cc\u7684\u72d7\uff08\u573a\u4e0a\u5171 {} \u53ea\uff09", (Object)made, (Object)DogPack.count(sl));
        }
    }

    private void playSoundLocal(ServerLevel sl, SoundEvent ev, float vol, float pitch) {
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), ev, SoundSource.HOSTILE, vol, pitch);
    }

    private void tell(ServerLevel sl, String msg) {
        for (ServerPlayer p : sl.m_6907_()) {
            if (!(p.m_20280_((Entity)this) < 1600.0)) continue;
            p.m_5661_((Component)Component.m_237113_((String)msg), true);
        }
    }

    private void applyBleedStack(LivingEntity le, int ticks) {
        int maxAmp = this.phase == 2 ? 9 : 4;
        MobEffect own = (MobEffect)FTEffects.BLEED.get();
        int cur = own != null && le.m_21023_(own) ? le.m_21124_(own).m_19564_() : -1;
        int amp = Math.min(maxAmp, cur + 1);
        ExtraDebuffs.bleed(le, ticks, amp);
        if (le instanceof Player) {
            Player pl = (Player)le;
            BleedLink.link(pl, (LivingEntity)this);
        }
    }

    private void addTremor(Player p) {
        MobEffect eff = (MobEffect)FTEffects.TREMOR.get();
        if (eff == null) {
            return;
        }
        int cur = p.m_21023_(eff) ? p.m_21124_(eff).m_19564_() : -1;
        int amp = Math.min(9, cur + 1);
        p.m_7292_(new MobEffectInstance(eff, 200, amp, false, true, true));
        FTBossMod.LOGGER.info("[FTBoss][benger] \u4e0b\u7838\u9707\u98a4 -> {} \u53e0\u5230 {} \u5c42", (Object)p.m_7755_().getString(), (Object)(amp + 1));
    }

    private void barkAoe(ServerLevel sl) {
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 2.2f, 0.8f);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12619_, SoundSource.HOSTILE, 2.0f, 0.65f);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_235902_, this.m_20185_(), this.m_20186_() + 1.6, this.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
        Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 60, (ParticleOptions)ParticleTypes.f_123758_, 14.0, 0.6);
        int n = 0;
        for (LivingEntity le : sl.m_45976_(LivingEntity.class, this.m_20191_().m_82400_(14.0))) {
            if (le == this || le instanceof BengerPupEntity || !le.m_6084_()) continue;
            if (le instanceof Player) {
                Player p = (Player)le;
                this.stunPlayer(p, 50);
                p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 8.0f);
            } else {
                le.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 9));
                le.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 50, 2));
                le.m_20334_(0.0, le.m_20184_().f_82480_, 0.0);
            }
            ++n;
        }
        this.summonPups(sl, this.phase == 2 ? 4 : 2);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.6f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][benger] \u5927\u72d7\u53eb\uff01\u547d\u4e2d {} \u4e2a\u751f\u7269\uff08\u7729\u6655 {} \u79d2\uff09+ \u53ec 2 \u53ea\u5c0f\u72d7", (Object)n, (Object)2);
    }

    private void drinkMilk(ServerLevel sl) {
        this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 400, 3));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 600, 1));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 1200, 0));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 400, 1));
        try {
            MobEffect damon = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", "damon_effect"));
            if (damon != null) {
                if (this.m_21023_(damon)) {
                    this.m_21195_(damon);
                }
                this.m_7292_(new MobEffectInstance(damon, 400, 3));
            } else {
                FTBossMod.LOGGER.warn("[FTBoss][benger] \u6ca1\u627e\u5230 armamentarium:damon_effect, \u8840\u738b\u5b50 IV \u8df3\u8fc7");
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][benger] \u8840\u738b\u5b50\u6548\u679c\u65bd\u52a0\u5931\u8d25: {}", (Object)t.toString());
        }
        this.intangLeft = Math.max(this.intangLeft, 60);
        this.m_20331_(true);
        for (Mob m : sl.m_45976_(Mob.class, this.m_20191_().m_82400_(64.0))) {
            if (m == this || m.m_5448_() != this) continue;
            m.m_6710_(null);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11911_, SoundSource.HOSTILE, 1.6f, 0.8f);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123748_, this.m_20185_(), this.m_20186_() + 1.4, this.m_20189_(), 30, 1.0, 1.0, 1.0, 0.2);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123789_, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 30, 1.2, 1.4, 1.2, 0.15);
        FTBossMod.LOGGER.info("[FTBoss][benger] \u91ce\u751f\u72d7\u5976\uff1a\u9644\u9b54\u91d1\u82f9\u679c\uff08\u5438\u6536 IV/\u518d\u751f II/\u6297\u706b\uff09+ \u8840\u738b\u5b50 IV + \u865a\u5f71 {} \u79d2", (Object)3);
    }

    public boolean m_6673_(DamageSource source) {
        return super.m_6673_(source) || this.intangLeft > 0;
    }

    private void startClamp(ServerLevel sl, Player p) {
        this.clampTarget = p;
        this.clampTicks = 60;
        this.clampHealTick = 0;
        this.clampCd = 320;
        this.clampX = p.m_20185_();
        this.clampY = p.m_20186_();
        this.clampZ = p.m_20189_();
        this.act = 1;
        this.actionTicks = 10;
        this.sweepFired = 1;
        this.m_20334_((p.m_20185_() - this.m_20185_()) * 0.12, 0.12, (p.m_20189_() - this.m_20189_()) * 0.12);
        this.f_19812_ = true;
        this.playSoundLocal(sl, SoundEvents.f_12619_, 1.6f, 0.75f);
        FTBossMod.LOGGER.info("[FTBoss][benger] \u6495\u54ac\u94b3\u5236\uff1a\u54ac\u4f4f {}\uff08{} \u79d2\uff0c\u6bcf\u79d2 {} \u4f24\u5bb3 + \u81ea\u8eab\u56de {}\uff09", new Object[]{p.m_7755_().getString(), 3, 20, 16});
    }

    private void clampTick(ServerLevel sl) {
        if (this.clampTicks <= 0 || this.clampTarget == null) {
            return;
        }
        Player p = this.clampTarget;
        if (!p.m_6084_() || p.m_5833_() || (double)this.m_20270_((Entity)p) > 6.0) {
            this.clampTicks = 0;
            this.clampTarget = null;
            return;
        }
        if (p instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)p;
            sp.f_8906_.m_9774_(this.clampX, this.clampY, this.clampZ, p.m_146908_(), p.m_146909_());
        }
        p.f_19864_ = true;
        p.f_19789_ = 0.0f;
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123798_, p.m_20185_(), p.m_20186_() + 1.0, p.m_20189_(), 3, 0.3, 0.3, 0.3, 0.02);
        if (--this.clampTicks % 20 == 0 || this.clampTicks <= 0) {
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 20.0f);
            this.applyBleedStack((LivingEntity)p, 120);
            if (this.m_21223_() < this.m_21233_()) {
                this.m_5634_(16.0f);
            }
            sl.m_6263_(null, p.m_20185_(), p.m_20186_(), p.m_20189_(), SoundEvents.f_11912_, SoundSource.HOSTILE, 1.2f, 0.8f);
        }
        if (this.clampTicks <= 0) {
            FTBossMod.LOGGER.info("[FTBoss][benger] \u6495\u54ac\u94b3\u5236\u7ed3\u675f\uff08{} \u5269\u4f59\u8840\u91cf {}\uff09", (Object)p.m_7755_().getString(), (Object)((int)p.m_21223_()));
            this.clampTarget = null;
        }
    }

    public void stunPlayer(Player p, int ticks) {
        this.stunTicks.put(p.m_20148_(), ticks);
        this.stunAnchor.put(p.m_20148_(), p.m_20182_());
    }

    private void stunTick(ServerLevel sl) {
        if (this.stunTicks.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> it = this.stunTicks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> en = it.next();
            ServerPlayer p = sl.m_7654_().m_6846_().m_11259_(en.getKey());
            int left = en.getValue() - 1;
            if (p == null || !p.m_6084_() || left <= 0) {
                it.remove();
                this.stunAnchor.remove(en.getKey());
                continue;
            }
            en.setValue(left);
            Vec3 a = this.stunAnchor.get(en.getKey());
            if (a != null) {
                p.f_8906_.m_9774_(a.f_82479_, a.f_82480_, a.f_82481_, p.m_146908_(), p.m_146909_());
                p.f_19864_ = true;
            }
            if (left % 5 != 0) continue;
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123797_, p.m_20185_(), p.m_20186_() + 1.9, p.m_20189_(), 4, 0.25, 0.2, 0.25, 0.02);
        }
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if ((amount = BossDamageGate.gate((LivingEntity)this, source, amount)) <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_(source, amount);
        if (hit) {
            BossDamageGate.onHit((LivingEntity)this);
            BossBarVisibility.visible(this.bossBar, true);
            if (!this.m_9236_().f_46443_ && this.act == 0 && this.m_217043_().m_188501_() < 0.6f) {
                this.act = 4;
                this.actionTicks = 8;
            }
        }
        return hit;
    }

    /*
     * Enabled aggressive block sorting
     */
    public boolean m_7327_(Entity target) {
        boolean hit = super.m_7327_(target);
        if (!hit) return hit;
        if (!(target instanceof LivingEntity)) return hit;
        LivingEntity le = (LivingEntity)target;
        this.applyBleedStack(le, 160);
        le.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0));
        BossKnockback.knockback(le, 0.8, le.m_20185_() - this.m_20185_(), le.m_20189_() - this.m_20189_());
        if (this.m_9236_().f_46443_) return hit;
        if (this.act != 0) return hit;
        if (le instanceof Player) {
            Level level;
            Player pl = (Player)le;
            if (this.clampCd <= 0 && (level = this.m_9236_()) instanceof ServerLevel) {
                ServerLevel slc = (ServerLevel)level;
                if (this.m_217043_().m_188501_() < 0.35f) {
                    this.startClamp(slc, pl);
                    return hit;
                }
            }
        }
        this.act = 1;
        this.actionTicks = 10;
        this.sweepFired = 1;
        return hit;
    }

    public void m_6457_(ServerPlayer p) {
        super.m_6457_(p);
        BossBarVisibility.addPlayer(this.bossBar, p);
        BossBars.track((Entity)this, this.bossBar);
    }

    public void m_6452_(ServerPlayer p) {
        super.m_6452_(p);
        this.bossBar.m_6539_(p);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public void m_6667_(DamageSource source) {
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossMusic.release(sl, (LivingEntity)this);
            BossBars.untrack((Entity)this, this.bossBar);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u88ab\u51fb\u8d25\uff08{} \u7684\u4f24\u5bb3\uff09", (Object)source.m_19385_());
        }
        this.act = 5;
        super.m_6667_(source);
    }

    public void m_142687_(Entity.RemovalReason reason) {
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossBars.untrack((Entity)this, this.bossBar);
            BossMusic.release(sl, (LivingEntity)this);
        }
        super.m_142687_(reason);
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128379_("bengerRaged", this.isRaged());
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("bengerRaged")) {
            this.setRaged(tag.m_128471_("bengerRaged"));
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "benger_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<BengerEntity> state) {
        this.actionState.prepare(state);
        if (BengerSkillSync.apply(this.actionState, state)) {
            return PlayState.CONTINUE;
        }
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        int visualAction = this.actionState.active() ? Integer.parseInt(this.actionState.name()) : 0;
        switch (visualAction) {
            case 7: {
                state.setAnimation(POUNCE);
                return PlayState.CONTINUE;
            }
            case 8: {
                state.setAnimation(SWEEP);
                return PlayState.CONTINUE;
            }
            case 2: 
            case 3: 
            case 9: {
                state.setAnimation(ROAR);
                return PlayState.CONTINUE;
            }
            case 10: {
                state.setAnimation(IDLE);
                return PlayState.CONTINUE;
            }
            case 1: {
                state.setAnimation(BITE);
                return PlayState.CONTINUE;
            }
            case 4: {
                state.setAnimation(HURT);
                return PlayState.CONTINUE;
            }
        }
        if (BengerLocomotion.apply(state)) {
            return PlayState.CONTINUE;
        }
        if (state.isMoving()) {
            state.setAnimation(this.m_20184_().m_165925_() > 0.02 ? RUN : WALK);
            return PlayState.CONTINUE;
        }
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    private void chargeTick(ServerLevel serverLevel) {
        if (this.chargeTicks <= 0) {
            return;
        }
        if (!this.m_6084_()) {
            this.finishCharge();
            return;
        }
        this.m_21573_().m_26573_();
        float f = (float)Math.toDegrees(Math.atan2(-this.chargeX, this.chargeZ));
        this.m_146922_(f);
        this.m_5618_(f);
        this.m_5616_(f);
        if (this.chargeTicks > 16) {
            this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
            if (this.chargeTicks % 2 == 0) {
                DustParticleOptions dustParticleOptions = new DustParticleOptions(new Vector3f(1.0f, 0.55f, 0.08f), 1.5f);
                for (int i = 1; i <= 12; ++i) {
                    serverLevel.m_8767_((ParticleOptions)dustParticleOptions, this.m_20185_() + this.chargeX * (double)i, this.m_20186_() + 0.2, this.m_20189_() + this.chargeZ * (double)i, 1, 0.08, 0.02, 0.08, 0.0);
                }
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 8, 0.6, 0.5, 0.6, 0.02);
            }
            --this.chargeTicks;
            return;
        }
        if (this.chargeTicks == 16) {
            this.playSoundLocal(serverLevel, SoundEvents.f_12317_, 2.0f, 0.6f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 24, 0.7, 0.4, 0.7, 0.12);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u8ffd\u730e\u7a81\u51fb\u51b2\u523a\u751f\u6548");
        }
        Vec3 vec3 = new Vec3(this.chargeX * 1.6, 0.0, this.chargeZ * 1.6);
        if (serverLevel.m_186434_((Entity)this, this.m_20191_().m_82369_(vec3)).iterator().hasNext()) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_(), 16, 0.6, 0.3, 0.6, 0.06);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u8ffd\u730e\u7a81\u51fb\u9047\u969c\u788d\u505c\u6b62");
            this.finishCharge();
            return;
        }
        this.m_20334_(vec3.f_82479_, this.m_20184_().f_82480_, vec3.f_82481_);
        this.f_19812_ = true;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_() - this.chargeX, this.m_20186_() + 0.3, this.m_20189_() - this.chargeZ, 6, 0.45, 0.15, 0.45, 0.04);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 4, 0.4, 0.4, 0.4, 0.03);
        AABB aABB = this.m_20191_().m_82369_(vec3).m_82377_(0.65, 0.3, 0.65);
        if (this.chargeFired == 0) {
            for (Player player : serverLevel.m_6907_()) {
                if (!player.m_6084_() || player.m_5833_() || player.m_7500_() || !aABB.m_82381_(player.m_20191_()) || !this.m_21574_().m_148306_((Entity)player)) continue;
                player.f_19802_ = 0;
                if (!player.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 34.0f)) continue;
                BossKnockback.push((Entity)player, this.chargeX * 1.4, 0.45, this.chargeZ * 1.4);
                this.stunPlayer(player, 40);
                this.chargeFired = 1;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123747_, player.m_20185_(), player.m_20186_() + 0.9, player.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, player.m_20185_(), player.m_20186_() + 0.9, player.m_20189_(), 24, 0.6, 0.6, 0.6, 0.15);
                serverLevel.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 1.4f, 0.6f);
                FTBossMod.LOGGER.info("[FTBoss][benger] \u51b2\u649e\u547d\u4e2d {}\uff08{} \u4f24\u5bb3 + \u7729\u6655 {} \u79d2\uff09", new Object[]{player.m_7755_().getString(), 34, 2});
                break;
            }
        }
        if (--this.chargeTicks <= 0) {
            this.finishCharge();
        }
    }

    private void syncIntangibleState() {
        if (this.m_9236_().f_46443_) {
            return;
        }
        boolean bl = this.intangLeft > 0 && this.m_6084_();
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_ACTION);
        if (compoundTag.m_128471_("bengerIntangible") == bl) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        compoundTag2.m_128379_("bengerIntangible", bl);
        this.f_19804_.m_135381_(DATA_ACTION, (Object)compoundTag2);
    }

    private void startCharge(ServerLevel serverLevel, LivingEntity livingEntity) {
        Vec3 vec3 = new Vec3(livingEntity.m_20185_() - this.m_20185_(), 0.0, livingEntity.m_20189_() - this.m_20189_());
        if (vec3.m_82556_() < 1.0E-4) {
            vec3 = new Vec3(0.0, 0.0, 1.0);
        }
        vec3 = vec3.m_82541_();
        this.chargeX = vec3.f_82479_;
        this.chargeZ = vec3.f_82481_;
        this.chargeTicks = 24;
        this.chargeFired = 0;
        this.chargeCd = 380;
        this.act = 7;
        this.actionTicks = 24;
        this.pounceFired = 1;
        this.m_21573_().m_26573_();
        this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
        float f = (float)Math.toDegrees(Math.atan2(-this.chargeX, this.chargeZ));
        this.m_146922_(f);
        this.m_5618_(f);
        this.m_5616_(f);
        this.playSoundLocal(serverLevel, SoundEvents.f_12363_, 1.4f, 1.2f);
        FTBossMod.LOGGER.info("[FTBoss][benger] \u8ffd\u730e\u7a81\u51fb\u8d77\u624b\uff1a8 tick \u65b9\u5411\u9884\u8b66 + 16 tick \u51b2\u523a\uff08{} \u4f24\u5bb3 + \u7729\u6655 {} \u79d2\uff09", (Object)34, (Object)2);
    }

    private void finishCharge() {
        this.chargeTicks = 0;
        this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
        if (this.act == 7) {
            this.act = 0;
            this.actionTicks = 0;
        }
    }

    public boolean isIntangible() {
        return this.m_6084_() && (this.m_9236_().f_46443_ ? ((CompoundTag)this.f_19804_.m_135370_(DATA_ACTION)).m_128471_("bengerIntangible") : this.intangLeft > 0);
    }

    private void intangTick(ServerLevel serverLevel) {
        if (this.intangLeft > 0) {
            --this.intangLeft;
            this.m_20331_(true);
            this.intangibleParticles(serverLevel);
            if (this.intangLeft % 10 == 0 && this.intangLeft > 0) {
                serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 1.6f);
            }
            if (this.intangLeft == 0) {
                this.m_20331_(false);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123747_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123745_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 24, 1.0, 0.8, 1.0, 0.04);
                serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12619_, SoundSource.HOSTILE, 1.4f, 0.7f);
                FTBossMod.LOGGER.info("[FTBoss][benger] \u865a\u5f71\u7ed3\u675f\uff08\u91cd\u65b0\u53ef\u4ee5\u88ab\u9009\u4e2d\uff09");
            }
            return;
        }
        if (--this.intangCd <= 0) {
            this.intangCd = 500;
            this.intangLeft = 60;
            this.m_20331_(true);
            this.m_6710_(null);
            int n = 0;
            for (Mob mob : serverLevel.m_45976_(Mob.class, this.m_20191_().m_82400_(64.0))) {
                if (mob == this || mob.m_5448_() != this) continue;
                mob.m_6710_(null);
                ++n;
            }
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.4f, 1.4f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 40, 1.2, 1.4, 1.2, 0.15);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u8fdb\u5165\u865a\u5f71\u72b6\u6001\uff1a{} \u79d2\u4e0d\u53ef\u9009\u4e2d/\u65e0\u654c\uff08\u89e3\u9664 {} \u4e2a\u7d22\u654c\uff09", (Object)3, (Object)n);
        }
    }

    private void skillTick(ServerLevel serverLevel) {
        AttributeInstance attributeInstance;
        if (this.chargeCd > 0) {
            --this.chargeCd;
        }
        if (this.packCd > 0) {
            --this.packCd;
        }
        if (this.roarCd > 0) {
            --this.roarCd;
        }
        if (this.sweepCd > 0) {
            --this.sweepCd;
        }
        if (this.slamCd > 0) {
            --this.slamCd;
        }
        if (this.growlCd > 0) {
            --this.growlCd;
        }
        if (this.barkCd > 0) {
            --this.barkCd;
        }
        if (this.milkCd > 0) {
            --this.milkCd;
        }
        if (this.phase == 1 && this.m_21223_() < this.m_21233_() * 0.5f) {
            this.phase = 2;
            this.summonPups(serverLevel, 4);
            attributeInstance = this.m_21051_(Attributes.f_22279_);
            if (attributeInstance != null) {
                attributeInstance.m_22118_(new AttributeModifier(UUID.fromString("6f2b1d3c-9a11-4c21-8f30-000000000002"), "ftboss.benger_phase2", 0.05, AttributeModifier.Operation.ADDITION));
            }
            this.tell(serverLevel, "\u00a74\u00a7l\u3010\u4e8c\u9636\u6bb5\u3011\u00a7r\u00a7cbenger \u5524\u6765\u4e86\u6574\u7fa4\u91ce\u72d7\uff01");
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12620_, SoundSource.HOSTILE, 2.4f, 0.55f);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u8fdb\u5165\u4e8c\u9636\u6bb5\uff08\u534a\u8840\uff09\uff1a\u53ec\u5524\u7ffb\u500d / \u6d41\u8840\u53ef\u53e0 10 \u5c42 / \u4e0b\u7838\u9644\u52a0\u9707\u98a4");
        }
        if (!this.isRaged() && this.m_21223_() < this.m_21233_() * 0.4f) {
            this.setRaged(true);
            attributeInstance = this.m_21051_(Attributes.f_22279_);
            if (attributeInstance != null) {
                attributeInstance.m_22118_(new AttributeModifier(UUID.fromString("6f2b1d3c-9a11-4c21-8f30-000000000001"), "ftboss.benger_rage", 0.07, AttributeModifier.Operation.ADDITION));
            }
            this.summonPups(serverLevel, this.phase == 2 ? 6 : 3);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.6f, 1.5f);
            this.playSoundLocal(serverLevel, SoundEvents.f_12620_, 1.6f, 0.6f);
            FTBossMod.LOGGER.info("[FTBoss][benger] \u72c2\u66b4\uff08\u8840\u91cf {}/{}\uff09\uff1a\u79fb\u901f +0.07\uff0c\u518d\u558a 2 \u53ea\u5c0f\u72d7", (Object)((int)this.m_21223_()), (Object)((int)this.m_21233_()));
        }
        if (this.packPounceAt > 0 && this.f_19797_ >= this.packPounceAt) {
            this.selfPackLeap(serverLevel);
            this.act = 7;
            this.actionTicks = 14;
            this.pounceFired = 0;
        }
        if (this.chargeTicks > 0 || this.clampTicks > 0 || this.packPounceAt > 0 && this.act == 0) {
            return;
        }
        if (this.act != 0) {
            this.actionResolve(serverLevel);
            return;
        }
        if (this.slamLeft > 0) {
            this.slamTick(serverLevel);
            return;
        }
        attributeInstance = this.m_5448_();
        if (attributeInstance == null || !attributeInstance.m_6084_()) {
            return;
        }
        double d = this.m_20270_((Entity)attributeInstance);
        boolean bl = this.m_21574_().m_148306_((Entity)attributeInstance);
        if (this.chargeCd <= 0 && d >= 3.5 && d <= 22.0 && bl) {
            this.startCharge(serverLevel, (LivingEntity)attributeInstance);
            return;
        }
        if (this.barkCd <= 0 && d <= 14.0) {
            this.act = 9;
            this.actionTicks = 20;
            this.roarFired = 0;
            this.barkCd = 400;
            FTBossMod.LOGGER.info("[FTBoss][benger] \u5927\u72d7\u53eb\uff01\u8d77\u624b\uff08{} \u683c\u5185\u5168\u90e8\u751f\u7269\u7729\u6655 {} \u79d2 + \u53ec 2 \u53ea\u5c0f\u72d7\uff09", (Object)14, (Object)2);
            return;
        }
        if (this.milkCd <= 0 && (this.m_21223_() < this.m_21233_() * 0.7f || this.m_217043_().m_188501_() < 0.25f)) {
            this.act = 10;
            this.actionTicks = 20;
            this.roarFired = 0;
            this.milkCd = 560;
            return;
        }
        if (this.packCd <= 0 && d <= 20.0 && bl) {
            this.startPackPounce(serverLevel, (LivingEntity)attributeInstance);
            return;
        }
        if (this.slamCd <= 0 && d <= 18.0 && bl) {
            this.startSlam(serverLevel, (LivingEntity)attributeInstance);
            return;
        }
        if (this.clampCd <= 0 && d <= 5.5 && attributeInstance instanceof Player) {
            Player player = (Player)attributeInstance;
            this.startClamp(serverLevel, player);
            return;
        }
        if (this.sweepCd <= 0 && d <= 5.5) {
            this.act = 8;
            this.actionTicks = 10;
            this.sweepFired = 0;
            this.playSoundLocal(serverLevel, SoundEvents.f_12317_, 1.4f, 0.7f);
            this.sweepCd = 220;
            FTBossMod.LOGGER.info("[FTBoss][benger] \u6253\u72d7\u68d2\u6a2a\u626b\uff08\u76ee\u6807\u8ddd\u79bb {} \u683c\uff09", (Object)String.format("%.1f", d));
            return;
        }
        if (this.roarCd <= 0 && d <= 14.0) {
            this.act = 3;
            this.actionTicks = 20;
            this.roarFired = 0;
            this.roarCd = 320;
            return;
        }
        if (this.growlCd <= 0 && this.m_217043_().m_188501_() < 0.02f) {
            this.growlCd = 160;
            this.playSoundLocal(serverLevel, SoundEvents.f_12619_, 1.2f, 0.7f);
        }
    }

    private void intangibleParticles(ServerLevel serverLevel) {
        if (this.intangLeft <= 0) {
            return;
        }
        for (int i = 0; i < 12; ++i) {
            double d = (double)i * Math.PI / 6.0 + (double)this.f_19797_ * 0.18;
            double d2 = 1.35 + 0.12 * Math.sin((double)this.f_19797_ * 0.3);
            double d3 = 0.3 + (double)(i % 3) * 0.65;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123745_, this.m_20185_() + Math.cos(d) * d2, this.m_20186_() + d3, this.m_20189_() + Math.sin(d) * d2, 1, 0.0, 0.0, 0.0, 0.005);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 6, 0.8, 0.8, 0.8, 0.03);
    }
}

