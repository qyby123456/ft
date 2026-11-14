/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
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

import java.util.ArrayList;
import java.util.EnumSet;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.HiddenBossChallenge;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossApproach;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.boss.ai.SummonLimit;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.entity.AppleCloneEntity;
import net.farmingtales.ftboss.entity.AppleMothEntity;
import net.farmingtales.ftboss.entity.AppleWormEntity;
import net.farmingtales.ftboss.entity.CoinManEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.GiantGrubEntity;
import net.farmingtales.ftboss.entity.GreenAppleEntity;
import net.farmingtales.ftboss.entity.RainbowPoopEntity;
import net.farmingtales.ftboss.entity.RottenAppleEntity;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.network.AppleBlastNetwork;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AppleBossEntity
extends Monster
implements GeoAnimatable,
RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(AppleBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(AppleBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.apple_boss.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.apple_boss.walk");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.apple_boss.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.apple_boss.turn_r");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.apple_boss.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.apple_boss.death");
    private static final RawAnimation ROLL = RawAnimation.begin().thenPlay("animation.apple_boss.roll_charge");
    private static final RawAnimation SEED = RawAnimation.begin().thenPlay("animation.apple_boss.seed_shot");
    private static final RawAnimation THROW = RawAnimation.begin().thenPlay("animation.apple_boss.throw_rotten");
    private static final RawAnimation WAX = RawAnimation.begin().thenPlay("animation.apple_boss.wax_shield");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.apple_boss.summon");
    private static final RawAnimation TRANSFORM = RawAnimation.begin().thenPlay("animation.apple_boss.transform");
    private static final RawAnimation ACID = RawAnimation.begin().thenPlay("animation.apple_boss.acid_spit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.apple_boss.attack");
    private static final RawAnimation LID_OPEN = RawAnimation.begin().thenLoop("animation.apple_boss.lid_open");
    private static final RawAnimation ORBIT = RawAnimation.begin().thenPlay("animation.apple_boss.orbit_shot");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.apple_boss.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.apple_boss.signature_self_cast");
    private static final double T_ROLL = 32.0;
    private static final double T_SEED = 32.0;
    private static final double T_THROW = 28.0;
    private static final double T_WAX = 40.0;
    private static final double T_SUMMON = 44.0;
    private static final double T_TRANSFORM = 56.0;
    private static final double T_ACID = 36.0;
    private static final double T_HURT = 9.0;
    private static final double T_ATTACK = 18.0;
    private static final double T_ORBIT = 32.0;
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.m_135353_(AppleBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final ServerBossEvent bossBar;
    private double actionTicks = 0.0;
    private String actionName = "";
    private int fxAt = -1;
    private int seedCd = 90;
    private int acidCd = 200;
    private int waxCd = 320;
    private int summonCd = 160;
    private int chargeState = 0;
    private int chargeT = 0;
    private double chargeX = 0.0;
    private double chargeZ = 0.0;
    private int chargeCd = 10;
    private int throwCd = 140;
    private int juiceCd = 240;
    private int burrowCd = 320;
    private int burrowT = 0;
    private int cloneCd = 120;
    private int orbitCd = 160;
    private int blastWaves = 0;
    private int blastT = 0;
    private double blastX = 0.0;
    private double blastY = 0.0;
    private double blastZ = 0.0;
    private int skyCd = 220;
    private int skyT = 0;
    private double skyX = 0.0;
    private double skyZ = 0.0;
    private int skyWave = 0;
    private int skyWaveT = 0;
    private int grubCd = 200;
    private int metCd = 300;
    private int metT = 0;
    private double metX = 0.0;
    private double metY = 0.0;
    private double metZ = 0.0;
    private static final double T_SKY = 44.0;
    private static final int METEOR_TELEGRAPH = 30;
    private int appleCd = 300;
    private int enchantCd = 900;
    private boolean ascended = false;
    private static final String[] ASCEND_TITLES = new String[]{"farmingtales:sweet_apple_manor", "sweet_apple_manor"};
    private boolean transformed = false;
    private int summonVariant;

    public AppleBossEntity(EntityType<? extends AppleBossEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 140;
        this.bossBar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        BossBars.track((Entity)this, this.bossBar);
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 320.0).m_22268_(Attributes.f_22284_, 2.0).m_22268_(Attributes.f_22281_, 3.5).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 42.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(3, (Goal)new PursueGoal(1.5));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_PHASE, (Object)1);
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
    }

    public int getPhase() {
        return (Integer)this.f_19804_.m_135370_(DATA_PHASE);
    }

    public boolean m_6051_() {
        return false;
    }

    public boolean m_5825_() {
        return true;
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("phase", this.getPhase());
        tag.m_128379_("transformed", this.transformed);
        tag.m_128347_("bossbar_progress", (double)this.bossBar.m_142717_());
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("phase")) {
            this.f_19804_.m_135381_(DATA_PHASE, (Object)tag.m_128451_("phase"));
        }
        this.transformed = tag.m_128471_("transformed");
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(RegularBossCastHost.action(kind), RegularBossCastHost.visualTicks(castTicks));
    }

    public void playAction(String name, double ticks) {
        this.summonVariant = 0;
        this.actionState.start(name, (int)ticks);
        this.actionName = name;
        this.actionTicks = ticks;
        this.fxAt = -1;
    }

    public boolean m_7327_(Entity target) {
        boolean hit = super.m_7327_(target);
        if (hit && target instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)target;
            le.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 100, 0));
            le.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0));
            if (this.actionTicks <= 0.0 && !this.m_21224_()) {
                this.playAction("attack", 18.0);
            }
        }
        return hit;
    }

    public boolean m_6469_(DamageSource source, float amount) {
        boolean ok = super.m_6469_(source, amount);
        if (ok && this.actionTicks <= 0.0 && !this.m_21224_()) {
            this.playAction("hurt", 9.0);
        }
        return ok;
    }

    public void m_8119_() {
        ServerLevel s;
        Level inst2;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.actionTicks > 0.0) {
            this.actionTicks -= 1.0;
        }
        if (this.f_19797_ % 20 == 0) {
            for (Level inst2 : new ArrayList(this.m_21220_())) {
                if (inst2.m_19544_().m_19486_()) continue;
                this.m_21195_(inst2.m_19544_());
            }
        }
        if ((inst2 = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel bsl = (ServerLevel)inst2;
            BossMusic.request(bsl, (LivingEntity)this);
        }
        LivingEntity target = this.m_5448_();
        Level level = this.m_9236_();
        ServerLevel sl = level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
        this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / this.m_21233_())));
        boolean wantBar = false;
        for (ServerPlayer bp : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (bp.m_6084_() && this.m_20280_((Entity)bp) < 2304.0) {
                BossBarVisibility.addPlayer(this.bossBar, bp);
                wantBar = true;
                continue;
            }
            this.bossBar.m_6539_(bp);
        }
        BossBarVisibility.visible(this.bossBar, this.m_6084_() && wantBar);
        if (--this.appleCd <= 0) {
            this.appleCd = 300;
            this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 100, 1, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 2400, 0, false, false, true));
            this.m_5634_(4.0f);
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11912_, SoundSource.HOSTILE, 1.4f, 0.8f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.6, this.m_20189_(), 24, (ParticleOptions)ParticleTypes.f_123748_, 1.3, 0.08);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] golden apple (regen II + absorption I)");
        }
        if (this.getPhase() >= 2 && --this.enchantCd <= 0) {
            this.enchantCd = 900;
            this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 600, 1, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 2400, 3, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 6000, 0, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 6000, 0, false, false, true));
            this.m_5634_(40.0f);
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12275_, SoundSource.HOSTILE, 1.4f, 0.7f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.8, this.m_20189_(), 40, (ParticleOptions)ParticleTypes.f_123767_, 1.6, 0.1);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] ENCHANTED golden apple (regen II / absorption IV / fire res / resistance)");
        }
        if (!this.ascended && this.f_19797_ % 20 == 0 && sl != null) {
            for (Player p : sl.m_6907_()) {
                boolean byContract;
                if (!p.m_6084_() || p.m_20280_((Entity)this) > 2304.0) continue;
                boolean byTitle = BossHandler.hasTitle(p, ASCEND_TITLES);
                String contractHit = BossHandler.matchedContract(p);
                boolean bl = byContract = contractHit != null;
                if (byTitle && byContract) {
                    FTBossMod.LOGGER.info("[FTBoss][Apple] \u89e6\u53d1\u8715\u53d8: \u5934\u8854+\u8840\u8165\u5951\u7ea6 \u5747\u6ee1\u8db3 (title={} contract={}); \u547d\u4e2d\u7269\u54c1={}", new Object[]{byTitle, byContract, contractHit});
                    this.ascendToNextBoss(sl, p);
                    return;
                }
                if (this.f_19797_ % 100 != 0) continue;
                FTBossMod.LOGGER.info("[FTBoss][title-debug] {}", (Object)BossHandler.debugLine(p));
            }
        }
        if (!this.transformed && this.m_21223_() <= this.m_21233_() * 0.5f && this.actionTicks <= 0.0) {
            this.transformed = true;
            this.f_19804_.m_135381_(DATA_PHASE, (Object)2);
            this.playAction("transform", 56.0);
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12563_, SoundSource.HOSTILE, 1.3f, 0.7f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.4, this.m_20189_(), 40, (ParticleOptions)ParticleTypes.f_123753_, 1.8, 0.12);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] phase -> 2 (transform)");
        }
        if (this.actionTicks > 0.0 && "transform".equals(this.actionName)) {
            this.transformFx(sl);
        }
        if (this.skyT > 0) {
            --this.skyT;
            if (sl != null) {
                for (int i = 0; i < 6; ++i) {
                    sl.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.m_20185_(), this.m_20186_() + 2.0 + (double)i * 3.0, this.m_20189_(), 1, 0.15, 0.0, 0.15, 0.0);
                }
                sl.m_8767_((ParticleOptions)ParticleTypes.f_175830_, this.m_20185_(), this.m_20186_() + 4.0, this.m_20189_(), 4, 0.4, 2.0, 0.4, 0.05);
            }
            if (this.skyT == 0) {
                this.skyWave = 10;
                this.skyWaveT = 0;
            }
            return;
        }
        if (this.skyWave > 0) {
            if (--this.skyWaveT <= 0) {
                this.skyWaveT = 10;
                --this.skyWave;
                this.skyBarrage(sl);
            }
            return;
        }
        if (this.metT > 0) {
            --this.metT;
            if (sl != null) {
                double k = 1.0 - (double)this.metT / 30.0;
                double fy = this.metY + (1.0 - k) * 28.0;
                for (int i = 0; i < 5; ++i) {
                    sl.m_8767_((ParticleOptions)ParticleTypes.f_123744_, this.metX, fy + (double)i * 0.5, this.metZ, 1, 0.35, 0.2, 0.35, 0.02);
                }
                sl.m_8767_((ParticleOptions)ParticleTypes.f_123755_, this.metX, fy, this.metZ, 3, 0.5, 0.5, 0.5, 0.02);
            }
            if (this.metT == 0) {
                this.meteorImpact(sl);
            }
            return;
        }
        if (this.blastWaves > 0) {
            if (--this.blastT <= 0) {
                this.blastT = 8;
                int stage = 3 - this.blastWaves + 1;
                --this.blastWaves;
                this.explosionStage(sl, stage);
            }
            return;
        }
        if (this.chargeState >= 1) {
            this.tickCharge(sl);
            return;
        }
        if (this.burrowT > 0) {
            this.tickBurrow(sl, target);
            return;
        }
        if (this.actionTicks > 0.0) {
            this.tickAction(sl, target);
            return;
        }
        if (target == null || !target.m_6084_()) {
            return;
        }
        if (--this.chargeCd <= 0) {
            this.startCharge(sl, target);
            return;
        }
        if (--this.throwCd <= 0 && this.m_20280_((Entity)target) > 49.0 && this.m_20280_((Entity)target) < 676.0) {
            this.throwCd = this.getPhase() >= 2 ? 150 + this.f_19796_.m_188503_(70) : 230 + this.f_19796_.m_188503_(90);
            this.playAction("throw_rotten", 28.0);
            this.fxAt = 0;
            return;
        }
        if (--this.seedCd <= 0 && this.m_20280_((Entity)target) < 400.0) {
            this.seedCd = this.getPhase() >= 2 ? 130 + this.f_19796_.m_188503_(60) : 200 + this.f_19796_.m_188503_(90);
            this.playAction("seed_shot", 32.0);
            this.fxAt = 0;
            return;
        }
        if (this.getPhase() >= 2 && --this.juiceCd <= 0 && this.m_20280_((Entity)target) < 196.0) {
            this.juiceCd = 220 + this.f_19796_.m_188503_(90);
            this.playAction("juice_spray", 36.0);
            this.fxAt = 0;
            return;
        }
        if (--this.acidCd <= 0 && this.m_20280_((Entity)target) < 144.0) {
            this.acidCd = this.getPhase() >= 2 ? 200 + this.f_19796_.m_188503_(90) : 300 + this.f_19796_.m_188503_(120);
            this.playAction("acid_spit", 36.0);
            this.fxAt = 0;
            return;
        }
        if (this.getPhase() >= 2 && --this.burrowCd <= 0 && this.m_20280_((Entity)target) < 900.0) {
            this.burrowCd = 400 + this.f_19796_.m_188503_(160);
            this.burrowT = 26;
            this.m_6842_(true);
            this.m_20331_(true);
            this.m_21573_().m_26573_();
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_215780_, SoundSource.HOSTILE, 1.4f, 1.2f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_(), 40, (ParticleOptions)ParticleTypes.f_123755_, 1.4, 0.1);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] burrow dive start");
            return;
        }
        if (--this.cloneCd <= 0) {
            this.cloneCd = this.getPhase() >= 2 ? 140 + this.f_19796_.m_188503_(80) : 200 + this.f_19796_.m_188503_(100);
            this.playAction("summon", 44.0);
            this.fxAt = 0;
            this.summonVariant = 1;
            return;
        }
        if (--this.grubCd <= 0) {
            this.grubCd = this.getPhase() >= 2 ? 400 + this.f_19796_.m_188503_(160) : 600 + this.f_19796_.m_188503_(240);
            this.playAction("summon", 44.0);
            this.fxAt = 0;
            this.summonVariant = 2;
            return;
        }
        if (--this.orbitCd <= 0) {
            this.orbitCd = this.getPhase() >= 2 ? 150 + this.f_19796_.m_188503_(70) : 240 + this.f_19796_.m_188503_(90);
            this.playAction("orbit_shot", 32.0);
            this.fxAt = 0;
            return;
        }
        if (--this.skyCd <= 0) {
            this.skyCd = this.getPhase() >= 2 ? 380 + this.f_19796_.m_188503_(140) : 560 + this.f_19796_.m_188503_(200);
            this.playAction("sky_beam", 44.0);
            this.fxAt = 0;
            this.skyT = 60;
            this.skyX = target.m_20185_();
            this.skyZ = target.m_20189_();
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.6f, 1.4f);
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11767_, SoundSource.HOSTILE, 1.4f, 0.9f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] sky beam charging at {}, {}", (Object)((int)this.skyX), (Object)((int)this.skyZ));
            return;
        }
        if (--this.metCd <= 0) {
            this.metCd = this.getPhase() >= 2 ? 320 + this.f_19796_.m_188503_(140) : 460 + this.f_19796_.m_188503_(200);
            this.playAction("meteor", 44.0);
            this.fxAt = 0;
            this.metT = 30;
            this.metX = target.m_20185_();
            this.metY = target.m_20186_();
            this.metZ = target.m_20189_();
            if (sl != null) {
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11923_, SoundSource.HOSTILE, 1.8f, 0.6f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] meteor incoming at {}, {}", (Object)((int)this.metX), (Object)((int)this.metZ));
            return;
        }
        if (--this.waxCd <= 0) {
            this.waxCd = 460 + this.f_19796_.m_188503_(160);
            this.playAction("wax_shield", 40.0);
            this.fxAt = 0;
            return;
        }
        if (--this.summonCd <= 0) {
            this.summonCd = this.getPhase() >= 2 ? 180 + this.f_19796_.m_188503_(80) : 260 + this.f_19796_.m_188503_(100);
            this.playAction("summon", 44.0);
            this.fxAt = 0;
            return;
        }
    }

    private void startCharge(ServerLevel sl, LivingEntity target) {
        this.chargeState = 1;
        this.chargeT = 10;
        this.chargeX = target.m_20185_();
        this.chargeZ = target.m_20189_();
        this.playAction("roll_charge", 32.0);
        if (sl != null) {
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.3f, 1.15f);
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 4, (ParticleOptions)ParticleTypes.f_123797_, 1.2, 0.1);
        }
    }

    private void tickCharge(ServerLevel sl) {
        if (this.chargeState == 1) {
            if (--this.chargeT > 0) {
                return;
            }
            this.chargeState = 2;
            this.chargeT = 40;
            LivingEntity t = this.m_5448_();
            if (t != null) {
                this.chargeX = t.m_20185_();
                this.chargeZ = t.m_20189_();
            }
            return;
        }
        if (this.chargeState == 2) {
            double dx = this.chargeX - this.m_20185_();
            double dz = this.chargeZ - this.m_20189_();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            this.m_6034_(this.m_20185_() + dx / dl * 1.2, this.m_20186_(), this.m_20189_() + dz / dl * 1.2);
            if (sl != null) {
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.3, this.m_20189_(), 1, (ParticleOptions)ParticleTypes.f_123810_, 0.9, 0.05);
            }
            boolean contact = false;
            for (Player p : this.m_9236_().m_6907_()) {
                if (!p.m_6084_() || !(p.m_20280_((Entity)this) < 6.25)) continue;
                contact = true;
                break;
            }
            if (contact || --this.chargeT <= 0 || dl < 1.2) {
                this.chargeExplode(sl);
            }
        }
    }

    private void chargeExplode(ServerLevel sl) {
        this.chargeState = 0;
        this.chargeCd = 22 + this.f_19796_.m_188503_(16);
        this.blastX = this.m_20185_();
        this.blastY = this.m_20186_() + 0.8;
        this.blastZ = this.m_20189_();
        this.blastWaves = 2;
        this.blastT = 8;
        this.explosionStage(sl, 1);
    }

    private void explosionStage(ServerLevel sl, int stage) {
        double R;
        if (sl == null) {
            return;
        }
        double d = stage >= 3 ? 7.0 : (R = stage == 2 ? 6.5 : 6.0);
        float base = stage >= 3 ? 60.0f : (stage == 2 ? 52.0f : 45.0f);
        double x = this.blastX;
        double y = this.blastY;
        double z = this.blastZ;
        AppleBlastNetwork.show(sl, x, y, z, R);
        sl.m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.6f + (float)stage * 0.25f, 0.75f - (float)stage * 0.06f);
        for (Player p : sl.m_6907_()) {
            double d2;
            if (!p.m_6084_() || (d2 = Math.sqrt(p.m_20275_(x, y, z))) > R) continue;
            p.m_6469_(this.m_269291_().m_269036_((Entity)this, (Entity)this), (float)((double)base * 0.5 * Math.max(0.25, 1.0 - d2 / R)));
            BossKnockback.knockback((LivingEntity)p, 0.9 + (double)stage * 0.4, x - p.m_20185_(), z - p.m_20189_());
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] explosion stage {} ({} dmg, r={}) at {}, {}, {}", new Object[]{stage, (int)base, R, (int)x, (int)y, (int)z});
    }

    private void summonClones(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        int room = SummonLimit.room((LivingEntity)this, AppleCloneEntity.class);
        if (room <= 0) {
            SummonLimit.atCap((LivingEntity)this, AppleCloneEntity.class);
            return;
        }
        int n = Math.min(5 + this.f_19796_.m_188503_(3) + (this.getPhase() >= 2 ? 2 : 0), room);
        for (int i = 0; i < n; ++i) {
            AppleCloneEntity c = (AppleCloneEntity)((EntityType)FTEntities.APPLE_CLONE.get()).m_20615_((Level)sl);
            if (c == null) continue;
            double a = Math.PI * 2 / (double)Math.max(1, n) * (double)i + this.f_19796_.m_188500_() * 0.5;
            double rr = 4.5 + this.f_19796_.m_188500_() * 1.5;
            c.m_7678_(this.m_20185_() + Math.cos(a) * rr, this.m_20186_(), this.m_20189_() + Math.sin(a) * rr, this.f_19796_.m_188501_() * 360.0f, 0.0f);
            c.m_6710_(this.m_5448_());
            sl.m_7967_((Entity)c);
            Fx.particles((Level)sl, c.m_20185_(), c.m_20186_() + 1.0, c.m_20189_(), 18, (ParticleOptions)ParticleTypes.f_123797_, 1.1, 0.1);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12052_, SoundSource.HOSTILE, 1.4f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} clones", (Object)n);
    }

    private void ascendToNextBoss(ServerLevel sl, Player trigger) {
        this.ascended = true;
        FTBossMod.LOGGER.info("[FTBoss][Apple] \u5934\u8854\u5171\u9e23 by {} -> \u8715\u53d8(\u5360\u4f4d: \u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba)", (Object)trigger.m_7755_().getString());
        if (sl == null) {
            return;
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12563_, SoundSource.HOSTILE, 2.0f, 0.5f);
        Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.5, this.m_20189_(), 120, (ParticleOptions)ParticleTypes.f_123767_, 2.6, 0.16);
        Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 1.5, this.m_20189_(), 80, (ParticleOptions)ParticleTypes.f_123745_, 2.0, 0.12);
        for (Player p : sl.m_6907_()) {
            if (p.m_20280_((Entity)this) > 4096.0) continue;
            p.m_5661_((Component)Component.m_237113_((String)"\u00a76\u3010\u5934\u8854\u5171\u9e23\u3011\u00a77\u68c0\u6d4b\u5230 \u00a7e\u751c\u82f9\u679c\u5e84\u56ed\u00a77 \u7684\u6301\u6709\u8005 \u2014\u2014 \u7235\u58eb\u8715\u53d8\u4e3a \u00a7c\u4e0b\u4e00\u4f4d Boss\u00a77\uff01"), false);
        }
        HiddenBossChallenge.ask(sl, trigger, trigger.m_20182_(), this.m_20182_(), "\u00a76\u54e6\u54e6\uff0c\u6b64\u4e43\u543e\u8f88\u4e4b\u621f\u4e5f\u3002", "\u00a76\u5219\u6c5d\u5f53\u662f\u52c7\u58eb\u4e5f\uff1f", "\u00a76\u527f\u706d\u90aa\u9f99\u4e4b\u4e8b\u53ef\u5f53\u662f\u771f\uff1f", "\u00a7e\u82e5\u5982\u6b64\uff0c\u5219\u543e\u7528\u4ee5\u72e9\u730e\u90aa\u9f99\u4e4b\u5251\u6280", "\u00a7e\u6c5d\u5f53\u8eab\u4ee5\u53d7\u4e4b\uff0c\u52a0\u4ee5\u4e60\u5f97\u5373\u53ef\u2026");
        SummonSweep.clear((LivingEntity)this, 80.0, AppleWormEntity.class, AppleMothEntity.class, RottenAppleEntity.class, GreenAppleEntity.class, AppleCloneEntity.class, CoinManEntity.class, GiantGrubEntity.class);
        this.m_146870_();
    }

    private void summonGiantGrub(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        AABB box = this.m_20191_().m_82400_(64.0);
        for (GiantGrubEntity g : sl.m_45976_(GiantGrubEntity.class, box)) {
            if (!g.m_6084_()) continue;
            FTBossMod.LOGGER.info("[FTBoss][Apple] giant grub already alive, skip");
            return;
        }
        GiantGrubEntity g = (GiantGrubEntity)((EntityType)FTEntities.GIANT_GRUB.get()).m_20615_((Level)sl);
        if (g == null) {
            return;
        }
        double a = this.f_19796_.m_188500_() * Math.PI * 2.0;
        g.m_7678_(this.m_20185_() + Math.cos(a) * 4.0, this.m_20186_(), this.m_20189_() + Math.sin(a) * 4.0, this.f_19796_.m_188501_() * 360.0f, 0.0f);
        g.m_6710_(this.m_5448_());
        sl.m_7967_((Entity)g);
        sl.m_6263_(null, g.m_20185_(), g.m_20186_(), g.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.6f, 0.5f);
        Fx.particles((Level)sl, g.m_20185_(), g.m_20186_() + 1.0, g.m_20189_(), 40, (ParticleOptions)ParticleTypes.f_123753_, 2.0, 0.12);
        FTBossMod.LOGGER.info("[FTBoss][Apple] GIANT GRUB summoned at {}, {}", (Object)((int)g.m_20185_()), (Object)((int)g.m_20189_()));
    }

    public static boolean isAppleSide(Entity e) {
        return e instanceof AppleBossEntity || e instanceof AppleCloneEntity || e instanceof GiantGrubEntity || e instanceof AppleWormEntity || e instanceof AppleMothEntity || e instanceof RottenAppleEntity || e instanceof GreenAppleEntity || e instanceof CoinManEntity;
    }

    private void skyBarrage(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        sl.m_6263_(null, this.skyX, this.m_20186_(), this.skyZ, SoundEvents.f_12090_, SoundSource.HOSTILE, 2.4f, 1.0f);
        int blasts = 16;
        for (int i = 0; i < 16; ++i) {
            double cz;
            double cx;
            double d;
            LivingEntity tgt = this.m_5448_();
            double a = this.f_19796_.m_188500_() * Math.PI * 2.0;
            if (tgt != null && this.f_19796_.m_188500_() < 0.6) {
                d = Math.sqrt(this.f_19796_.m_188500_()) * 6.0;
                cx = tgt.m_20185_();
                cz = tgt.m_20189_();
            } else {
                d = Math.sqrt(this.f_19796_.m_188500_()) * 15.0;
                cx = this.skyX;
                cz = this.skyZ;
            }
            double bx = cx + Math.cos(a) * d;
            double bz = cz + Math.sin(a) * d;
            double by = this.m_20186_();
            if (i == 5 && tgt != null) {
                bx = tgt.m_20185_();
                bz = tgt.m_20189_();
            }
            int kind = i < 5 ? 0 : 2;
            switch (kind) {
                case 0: {
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 26, (ParticleOptions)ParticleTypes.f_123744_, 1.3, 0.14);
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 12, (ParticleOptions)ParticleTypes.f_123756_, 1.0, 0.05);
                    break;
                }
                case 1: {
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 26, (ParticleOptions)ParticleTypes.f_175821_, 1.3, 0.1);
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 14, (ParticleOptions)ParticleTypes.f_123754_, 1.0, 0.12);
                    break;
                }
                case 2: {
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 30, (ParticleOptions)ParticleTypes.f_175830_, 1.4, 0.22);
                    LightningBolt bolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)sl);
                    if (bolt == null) break;
                    bolt.m_6027_(bx, by, bz);
                    sl.m_7967_((Entity)bolt);
                    break;
                }
                default: {
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 24, (ParticleOptions)ParticleTypes.f_123745_, 1.3, 0.1);
                    Fx.particles((Level)sl, bx, by + 0.5, bz, 12, (ParticleOptions)ParticleTypes.f_123765_, 1.0, 0.06);
                }
            }
            sl.m_6263_(null, bx, by, bz, SoundEvents.f_11913_, SoundSource.HOSTILE, 0.9f, 1.2f + this.f_19796_.m_188501_() * 0.3f);
            for (Player p : sl.m_6907_()) {
                double pd;
                if (!p.m_6084_() || (pd = Math.sqrt(p.m_20275_(bx, by, bz))) > 3.5) continue;
                p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), (float)(7.0 * Math.max(0.35, 1.0 - pd / 3.5)));
                switch (kind) {
                    case 0: {
                        p.m_20254_(4);
                        break;
                    }
                    case 1: {
                        p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 2, false, true, true));
                        break;
                    }
                    case 2: {
                        break;
                    }
                    default: {
                        p.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 100, 0, false, true, true));
                    }
                }
                BossKnockback.push((Entity)p, 0.0, 0.4, 0.0);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] sky barrage wave: {} blasts within r=15 around {}, {}", new Object[]{16, (int)this.skyX, (int)this.skyZ});
    }

    private void meteorImpact(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        double x = this.metX;
        double y = this.metY + 0.4;
        double z = this.metZ;
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123812_, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123812_, x, y + 1.4, z, 1, 0.0, 0.0, 0.0, 0.0);
        sl.m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 2.4f, 0.5f);
        sl.m_6263_(null, x, y, z, SoundEvents.f_12089_, SoundSource.HOSTILE, 1.6f, 0.8f);
        Fx.particles((Level)sl, x, y, z, 90, (ParticleOptions)ParticleTypes.f_123744_, 3.6, 0.2);
        Fx.particles((Level)sl, x, y, z, 40, (ParticleOptions)ParticleTypes.f_123756_, 2.6, 0.1);
        Fx.particles((Level)sl, x, y, z, 60, (ParticleOptions)ParticleTypes.f_123755_, 3.2, 0.12);
        double R = 8.0;
        for (Player p : sl.m_6907_()) {
            double d;
            if (!p.m_6084_() || (d = Math.sqrt(p.m_20275_(x, y, z))) > 8.0) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), (float)(30.0 * Math.max(0.4, 1.0 - d / 8.0)));
            p.m_20254_(6);
            BossKnockback.push((Entity)p, 0.0, 1.0, 0.0);
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] meteor impact (60 AoE, r=8) at {}, {}, {}", new Object[]{(int)x, (int)y, (int)z});
    }

    private void throwRotten(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        LivingEntity t = this.m_5448_();
        RainbowPoopEntity proj = new RainbowPoopEntity((Level)sl, (LivingEntity)this);
        proj.setRainbow(false);
        proj.m_37446_(new ItemStack((ItemLike)Items.f_42410_));
        proj.setDamage(12.0f + (float)this.getPhase() * 3.0f);
        double yaw = Math.toRadians(this.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double sx = this.m_20185_() + fx * 1.4;
        double sy = this.m_20186_() + 1.6;
        double sz = this.m_20189_() + fz * 1.4;
        proj.m_6034_(sx, sy, sz);
        if (t != null) {
            double dx = t.m_20185_() - sx;
            double dy = t.m_20188_() - sy;
            double dz = t.m_20189_() - sz;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double lead = Math.min(1.0, len / 24.0);
            double l2 = Math.max(0.1, Math.sqrt((dx += t.m_20184_().f_82479_ * lead * 12.0) * dx + (dy += t.m_20184_().f_82480_ * lead * 6.0) * dy + (dz += t.m_20184_().f_82481_ * lead * 12.0) * dz));
            proj.m_6686_(dx / l2, dy / l2 + 0.1, dz / l2, 1.25f, 0.8f);
        }
        sl.m_7967_((Entity)proj);
        sl.m_6263_(null, sx, sy, sz, SoundEvents.f_12473_, SoundSource.HOSTILE, 1.0f, 0.9f);
        Fx.particles((Level)sl, sx, sy, sz, 10, (ParticleOptions)ParticleTypes.f_123753_, 0.5, 0.06);
        FTBossMod.LOGGER.info("[FTBoss][Apple] throw rotten apple");
    }

    private void throwOrbitApples(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        LivingEntity t = this.m_5448_();
        int count = 3;
        for (int i = 0; i < 3; ++i) {
            RainbowPoopEntity proj = new RainbowPoopEntity((Level)sl, (LivingEntity)this);
            proj.setRainbow(false);
            proj.m_37446_(new ItemStack((ItemLike)Items.f_42410_));
            proj.setDamage(14.0f + (float)this.getPhase() * 3.0f);
            double spread = Math.toRadians((double)(i - 1) * 12.0);
            double yaw = Math.toRadians(this.m_146908_()) + spread;
            double sx = this.m_20185_() - Math.sin(yaw) * 1.2;
            double sy = this.m_20186_() + 2.6;
            double sz = this.m_20189_() + Math.cos(yaw) * 1.2;
            proj.m_6034_(sx, sy, sz);
            if (t != null) {
                double dx = t.m_20185_() - sx;
                double dy = t.m_20188_() - sy;
                double dz = t.m_20189_() - sz;
                double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                double lead = Math.min(1.0, len / 20.0);
                double l2 = Math.max(0.1, Math.sqrt((dx += t.m_20184_().f_82479_ * lead * 10.0) * dx + dy * dy + (dz += t.m_20184_().f_82481_ * lead * 10.0) * dz));
                proj.m_6686_(dx / l2, dy / l2 + 0.42, dz / l2, 1.15f + (float)i * 0.06f, 0.4f);
            }
            sl.m_7967_((Entity)proj);
            Fx.particles((Level)sl, sx, sy, sz, 8, (ParticleOptions)ParticleTypes.f_123797_, 0.4, 0.08);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12473_, SoundSource.HOSTILE, 1.2f, 1.5f);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 0.9f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] orbit apples launched x{}", (Object)3);
    }

    private void juiceSpray(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        double yaw = Math.toRadians(this.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        for (Player p : sl.m_6907_()) {
            double dot;
            double dz;
            double dx;
            double dl;
            if (!p.m_6084_() || (dl = Math.max(0.1, Math.sqrt((dx = p.m_20185_() - this.m_20185_()) * dx + (dz = p.m_20189_() - this.m_20189_()) * dz))) > 8.0 || Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot = dx / dl * fx + dz / dl * fz)))) > 55.0) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 3.0f);
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 140, 2, false, true, true));
            p.m_7292_(new MobEffectInstance(MobEffects.f_19599_, 120, 1, false, true, true));
            p.m_7292_(new MobEffectInstance(MobEffects.f_19604_, 100, 0, false, true, true));
        }
        for (int i = 0; i < 40; ++i) {
            double a = yaw + (this.f_19796_.m_188500_() - 0.5) * Math.toRadians(110.0);
            double d = this.f_19796_.m_188500_() * 8.0;
            Fx.particles((Level)sl, this.m_20185_() - Math.sin(a) * d, this.m_20186_() + 1.2 + this.f_19796_.m_188500_() * 0.8, this.m_20189_() + Math.cos(a) * d, 1, (ParticleOptions)ParticleTypes.f_123753_, 0.35, 0.05);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12098_, SoundSource.HOSTILE, 1.3f, 0.6f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] juice spray");
    }

    private void tickBurrow(ServerLevel sl, LivingEntity target) {
        LivingEntity t;
        --this.burrowT;
        if (sl != null) {
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 4, (ParticleOptions)ParticleTypes.f_123762_, 0.7, 0.03);
        }
        if (this.burrowT > 0) {
            return;
        }
        LivingEntity livingEntity = t = target != null ? target : this.m_5448_();
        if (t != null) {
            this.m_6034_(t.m_20185_(), t.m_20186_(), t.m_20189_());
        }
        this.m_6842_(false);
        this.m_20331_(false);
        this.playAction("summon", 44.0);
        this.fxAt = 0;
        if (sl != null) {
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_215781_, SoundSource.HOSTILE, 1.5f, 1.1f);
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123812_, this.m_20185_(), this.m_20186_() + 0.4, this.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_(), 50, (ParticleOptions)ParticleTypes.f_123755_, 2.0, 0.12);
            for (Player p : sl.m_6907_()) {
                if (!p.m_6084_() || p.m_20280_((Entity)this) > 30.0) continue;
                p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 10.0f + (float)this.getPhase() * 1.5f);
                BossKnockback.push((Entity)p, 0.0, 0.85, 0.0);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] burrow dive emerge");
    }

    private void transformFx(ServerLevel sl) {
        if (this.fxAt < 0) {
            this.fxAt = 0;
        }
        if (this.fxAt == 24) {
            this.f_19804_.m_135381_(DATA_PHASE, (Object)2);
            AttributeInstance spd = this.m_21051_(Attributes.f_22279_);
            if (spd != null) {
                spd.m_22100_(0.1);
            }
            this.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 600, 1, false, false, false));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 600, 1, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 2400, 3, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 6000, 0, false, false, true));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 6000, 0, false, false, true));
            this.m_5634_(40.0f);
            this.appleCd = 300;
            this.enchantCd = 900;
            LivingEntity tt = this.m_5448_();
            double tx = tt != null ? tt.m_20185_() : this.m_20185_();
            double ty = tt != null ? tt.m_20186_() : this.m_20186_();
            double tz = tt != null ? tt.m_20189_() : this.m_20189_();
            this.skyT = 60;
            this.skyX = tx;
            this.skyZ = tz;
            this.metT = 30;
            this.metX = tx;
            this.metY = ty;
            this.metZ = tz;
            this.playAction("sky_beam", 44.0);
            FTBossMod.LOGGER.info("[FTBoss][Apple] transform: enchanted golden apple + sky beam + meteor locked");
            if (sl != null) {
                sl.m_8767_((ParticleOptions)ParticleTypes.f_123812_, this.m_20185_(), this.m_20186_() + 0.4, this.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 50, (ParticleOptions)ParticleTypes.f_123763_, 2.4, 0.14);
                BossMusic.request(sl, (LivingEntity)this);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] phase -> 2 (BGM -> p2)");
        }
        ++this.fxAt;
    }

    private void tickAction(ServerLevel sl, LivingEntity target) {
        if (this.fxAt < 0) {
            return;
        }
        if ("transform".equals(this.actionName)) {
            return;
        }
        ++this.fxAt;
        switch (this.actionName) {
            case "seed_shot": {
                if (this.fxAt != 16) break;
                this.coneAttack(sl, 7.0, 70.0, 9.0f + (float)this.getPhase() * 2.0f, false);
                break;
            }
            case "acid_spit": {
                if (this.fxAt != 15) break;
                this.coneAttack(sl, 6.5, 60.0, 12.0f + (float)this.getPhase() * 3.0f, true);
                break;
            }
            case "throw_rotten": {
                if (this.fxAt != 12) break;
                this.throwRotten(sl);
                break;
            }
            case "juice_spray": {
                if (this.fxAt != 15) break;
                this.juiceSpray(sl);
                break;
            }
            case "orbit_shot": {
                if (this.fxAt != 21) break;
                this.throwOrbitApples(sl);
                break;
            }
            case "wax_shield": {
                if (this.fxAt != 20) break;
                this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 140, 2, false, false, false));
                this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 200, 1, false, false, false));
                this.m_5634_(20.0f);
                break;
            }
            case "summon": {
                if (this.fxAt != 19) break;
                if (this.summonVariant == 1) {
                    this.summonClones(sl);
                } else if (this.summonVariant == 2) {
                    this.summonGiantGrub(sl);
                } else {
                    this.summonSwarm(sl);
                }
                this.summonVariant = 0;
                break;
            }
        }
    }

    private void coneAttack(ServerLevel sl, double range, double halfDeg, float dmg, boolean acid) {
        if (sl == null) {
            return;
        }
        double yaw = Math.toRadians(this.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        for (Player p : sl.m_6907_()) {
            double dot;
            double dz;
            double dx;
            double dl;
            if (!p.m_6084_() || (dl = Math.max(0.1, Math.sqrt((dx = p.m_20185_() - this.m_20185_()) * dx + (dz = p.m_20189_() - this.m_20189_()) * dz))) > range || Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot = dx / dl * fx + dz / dl * fz)))) > halfDeg) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), dmg * 0.5f);
            BossKnockback.knockback((LivingEntity)p, 0.7, this.m_20185_() - p.m_20185_(), this.m_20189_() - p.m_20189_());
            if (!acid) continue;
            p.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 140, 0, false, true, true));
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 1, false, true, true));
        }
        for (int i = 0; i < 26; ++i) {
            double t = (this.f_19796_.m_188500_() - 0.5) * Math.toRadians(halfDeg * 2.0);
            double a = yaw + t;
            double d = this.f_19796_.m_188500_() * range;
            Fx.particles((Level)sl, this.m_20185_() - Math.sin(a) * d, this.m_20186_() + 1.4 + this.f_19796_.m_188500_() * 0.6, this.m_20189_() + Math.cos(a) * d, 1, (ParticleOptions)(acid ? ParticleTypes.f_123763_ : ParticleTypes.f_123797_), 0.25, 0.05);
        }
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), acid ? SoundEvents.f_12098_ : SoundEvents.f_11687_, SoundSource.HOSTILE, 1.2f, acid ? 0.7f : 1.3f);
    }

    private void summonSwarm(ServerLevel sl) {
        if (sl == null) {
            return;
        }
        int pick = this.f_19796_.m_188503_(5);
        int want = 3 + this.f_19796_.m_188503_(3) + (this.getPhase() >= 2 ? 2 : 0);
        if (pick == 0) {
            int room = SummonLimit.room((LivingEntity)this, AppleWormEntity.class);
            if (room <= 0) {
                SummonLimit.atCap((LivingEntity)this, AppleWormEntity.class);
                return;
            }
            int n = Math.min(want, room);
            for (int i = 0; i < n; ++i) {
                AppleWormEntity m = (AppleWormEntity)((EntityType)FTEntities.APPLE_WORM.get()).m_20615_((Level)sl);
                if (m == null) continue;
                m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                m.m_6710_(this.m_5448_());
                sl.m_7967_((Entity)m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} worms", (Object)n);
        } else if (pick == 1) {
            int room = SummonLimit.room((LivingEntity)this, AppleMothEntity.class);
            if (room <= 0) {
                SummonLimit.atCap((LivingEntity)this, AppleMothEntity.class);
                return;
            }
            int n = Math.min(want + 2, room);
            for (int i = 0; i < n; ++i) {
                AppleMothEntity m = (AppleMothEntity)((EntityType)FTEntities.APPLE_MOTH.get()).m_20615_((Level)sl);
                if (m == null) continue;
                m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.0, this.m_20186_() + 1.8 + this.f_19796_.m_188500_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.0, this.f_19796_.m_188501_() * 360.0f, 0.0f);
                m.m_6710_(this.m_5448_());
                sl.m_7967_((Entity)m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} moths", (Object)n);
        } else if (pick == 2) {
            int room = SummonLimit.room((LivingEntity)this, RottenAppleEntity.class);
            if (room <= 0) {
                SummonLimit.atCap((LivingEntity)this, RottenAppleEntity.class);
                return;
            }
            int n = Math.min(want, room);
            for (int i = 0; i < n; ++i) {
                RottenAppleEntity m = (RottenAppleEntity)((EntityType)FTEntities.ROTTEN_APPLE.get()).m_20615_((Level)sl);
                if (m == null) continue;
                m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                m.m_6710_(this.m_5448_());
                sl.m_7967_((Entity)m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} rotten apples", (Object)n);
        } else if (pick == 3) {
            int room = SummonLimit.room((LivingEntity)this, GreenAppleEntity.class);
            if (room <= 0) {
                SummonLimit.atCap((LivingEntity)this, GreenAppleEntity.class);
                return;
            }
            int n = Math.min(want + 3, room);
            for (int i = 0; i < n; ++i) {
                GreenAppleEntity m = (GreenAppleEntity)((EntityType)FTEntities.GREEN_APPLE.get()).m_20615_((Level)sl);
                if (m == null) continue;
                m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                m.m_6710_(this.m_5448_());
                sl.m_7967_((Entity)m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} green apples", (Object)n);
        } else {
            int room = SummonLimit.room((LivingEntity)this, CoinManEntity.class);
            if (room <= 0) {
                SummonLimit.atCap((LivingEntity)this, CoinManEntity.class);
                return;
            }
            int n = Math.min(3 + this.f_19796_.m_188503_(3) + (this.getPhase() >= 2 ? 2 : 0), room);
            for (int i = 0; i < n; ++i) {
                CoinManEntity m = (CoinManEntity)((EntityType)FTEntities.COIN_MAN.get()).m_20615_((Level)sl);
                if (m == null) continue;
                m.m_7678_(this.m_20185_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, this.m_20186_(), this.m_20189_() + (this.f_19796_.m_188500_() - 0.5) * 3.5, 0.0f, 0.0f);
                m.m_6710_(this.m_5448_());
                sl.m_7967_((Entity)m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} coin men", (Object)n);
        }
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        BossBars.untrack((Entity)this, this.bossBar);
        this.bossBar.m_7706_();
        SummonSweep.clear((LivingEntity)this, 80.0, AppleWormEntity.class, AppleMothEntity.class, RottenAppleEntity.class, GreenAppleEntity.class, AppleCloneEntity.class, CoinManEntity.class, GiantGrubEntity.class);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel msl = (ServerLevel)level;
            BossMusic.release(msl, (LivingEntity)this);
        }
        if (this.m_9236_() instanceof ServerLevel) {
            NonManorBossLootRouter.spawnAtBoss((LivingEntity)this, new ItemStack((ItemLike)Items.f_42437_, 1 + this.f_19796_.m_188503_(2)));
            FTBossMod.LOGGER.info("[FTBoss][Apple] defeated (drops: golden apples; apple core removed)");
        }
    }

    public void m_142687_(Entity.RemovalReason reason) {
        BossBars.untrack((Entity)this, this.bossBar);
        super.m_142687_(reason);
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "apple_ctrl", 4, this::predicate)});
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "apple_p2_ctrl", 0, this::phaseTwoPredicate)});
    }

    private PlayState phaseTwoPredicate(AnimationState<AppleBossEntity> state) {
        if (this.getPhase() >= 2 && !this.m_21224_()) {
            state.setAnimation(LID_OPEN);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState predicate(AnimationState<AppleBossEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (this.signatureCast.active()) {
            state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            return PlayState.CONTINUE;
        }
        if (this.actionState.active()) {
            switch (this.actionState.name()) {
                case "attack": {
                    state.setAnimation(ATTACK);
                    return PlayState.CONTINUE;
                }
                case "roll_charge": {
                    state.setAnimation(ROLL);
                    return PlayState.CONTINUE;
                }
                case "seed_shot": {
                    state.setAnimation(SEED);
                    return PlayState.CONTINUE;
                }
                case "throw_rotten": {
                    state.setAnimation(THROW);
                    return PlayState.CONTINUE;
                }
                case "wax_shield": {
                    state.setAnimation(WAX);
                    return PlayState.CONTINUE;
                }
                case "summon": {
                    state.setAnimation(SUMMON);
                    return PlayState.CONTINUE;
                }
                case "transform": {
                    state.setAnimation(TRANSFORM);
                    return PlayState.CONTINUE;
                }
                case "acid_spit": {
                    state.setAnimation(ACID);
                    return PlayState.CONTINUE;
                }
                case "juice_spray": {
                    state.setAnimation(ACID);
                    return PlayState.CONTINUE;
                }
                case "orbit_shot": {
                    state.setAnimation(ORBIT);
                    return PlayState.CONTINUE;
                }
                case "sky_beam": {
                    state.setAnimation(SUMMON);
                    return PlayState.CONTINUE;
                }
                case "meteor": {
                    state.setAnimation(TRANSFORM);
                    return PlayState.CONTINUE;
                }
            }
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        float dYaw = Mth.m_14177_((float)(this.m_146908_() - this.f_19859_));
        if (Math.abs(dYaw) > 1.2f) {
            state.setAnimation(dYaw > 0.0f ? TURN_L : TURN_R);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    private class PursueGoal
    extends Goal {
        private final double speed;
        private final BossApproach approach = new BossApproach();

        PursueGoal(double speed) {
            this.speed = speed;
            this.m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean m_8036_() {
            LivingEntity t = AppleBossEntity.this.m_5448_();
            return t != null && t.m_6084_() && AppleBossEntity.this.chargeState == 0 && AppleBossEntity.this.burrowT == 0;
        }

        public boolean m_8045_() {
            return this.m_8036_();
        }

        public void m_8041_() {
            this.approach.reset((Mob)AppleBossEntity.this);
        }

        public void m_8037_() {
            LivingEntity t = AppleBossEntity.this.m_5448_();
            if (t == null) {
                return;
            }
            AppleBossEntity.this.m_21563_().m_24960_((Entity)t, 30.0f, 30.0f);
            this.approach.tick((Mob)AppleBossEntity.this, t, this.speed, 2.2, 2.5, 0.0, 0.0);
        }
    }
}

