/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
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
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
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
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.BossText;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.DefectOrbs;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.client.defectzero.DefectSkillAnimation;
import net.farmingtales.ftboss.client.defectzero.DefectSkillHost;
import net.farmingtales.ftboss.client.defectzero.DefectSkillSync;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossCollision;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DefectCoreEntity
extends Monster
implements GeoAnimatable,
RegularBossCastHost,
DefectSkillHost {
    public static final float PHASE2_AT = 0.7f;
    public static final float PHASE3_AT = 0.35f;
    public static final float OVERLOAD_POWER = 4.0f;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.defect_core.idle");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.defect_core.cast");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.defect_core.hurt");
    private static final RawAnimation PHASE = RawAnimation.begin().thenPlay("animation.defect_core.phase_shift");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.defect_core.orb_summon");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.defect_core.death_overload");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.defect_core.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.defect_core.signature_self_cast");
    private static final RawAnimation CAST_GLASS = RawAnimation.begin().thenPlay("animation.defect_core.cast_glass");
    private static final RawAnimation CAST_LIGHTNING = RawAnimation.begin().thenPlay("animation.defect_core.cast_lightning");
    private static final RawAnimation CAST_FROST = RawAnimation.begin().thenPlay("animation.defect_core.cast_frost");
    private static final RawAnimation CAST_ION = RawAnimation.begin().thenPlay("animation.defect_core.cast_ion");
    private static final RawAnimation CAST_DARK = RawAnimation.begin().thenPlay("animation.defect_core.cast_dark");
    private static final RawAnimation FLOAT_MOVE = RawAnimation.begin().thenLoop("animation.defect_core.float_move");
    private static final RawAnimation ECHO = RawAnimation.begin().thenPlay("animation.defect_core.echo");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.defect_core.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.defect_core.turn_r");
    private static final RawAnimation LANDING_WEAK = RawAnimation.begin().thenLoop("animation.defect_core.exhausted");
    private static final RawAnimation BEAM = RawAnimation.begin().thenPlay("animation.defect_core.beam");
    private static final double T_BEAM = 285.0;
    private static final double T_CAST = 20.0;
    private static final double T_HURT = 10.0;
    private static final double T_PHASE = 52.0;
    private static final double T_SUMMON = 17.0;
    private static final double T_ECHO = 28.0;
    public static final int EXHAUST_TICKS = 120;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final ServerBossEvent bossBar;
    private final DefectOrbs orbs = new DefectOrbs(this);
    private int phase = 1;
    private double hoverPhase = 0.0;
    private int basicAttackCd = 60;
    private int orbsCastCooldown = 0;
    private int castTicks = 0;
    private RawAnimation castAnim = CAST;
    private int castAnimTicks = 20;
    private int summonTicks = 0;
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(DefectCoreEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(DefectCoreEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState signatureCast = new BossActionState((LivingEntity)this, DATA_SIGNATURE_CAST);
    private String castAction = "cast";
    private String lastVisualAction = "";
    private int lastVisualTicks;
    private LivingEntity pendingMelee;
    private long meleeReleaseAt;
    private boolean pendingBasicMagic;
    private static final EntityDataAccessor<CompoundTag> DATA_ORB_CUE = SynchedEntityData.m_135353_(DefectCoreEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private static final EntityDataAccessor<String> DATA_CORE_TINT = SynchedEntityData.m_135353_(DefectCoreEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135030_);
    private int phaseAnimTicks = 0;
    private int echoAnimTicks = 0;
    private int beamAnimTicks = 0;
    private boolean beamCharging = false;
    private int exhaustedTicks = 0;
    private float exhaustHealTotal = 0.0f;
    private int turnTicks = 0;
    private boolean turningLeft = false;
    private float lastYaw = 0.0f;

    public boolean m_6051_() {
        return false;
    }

    public void showOrbCue(String stage, OrbType type, int orbId, int duration) {
        if (this.m_9236_().f_46443_) {
            return;
        }
        CompoundTag cue = new CompoundTag();
        cue.m_128359_("stage", stage);
        cue.m_128359_("type", type.id);
        cue.m_128405_("orb", orbId);
        cue.m_128405_("duration", Math.max(1, duration));
        cue.m_128356_("start", this.m_9236_().m_46467_());
        this.f_19804_.m_135381_(DATA_ORB_CUE, (Object)cue);
    }

    public CompoundTag orbCue() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_ORB_CUE);
    }

    public String orbLinkStage() {
        CompoundTag cue = this.orbCue();
        long age = this.m_9236_().m_46467_() - cue.m_128454_("start");
        return age >= 0L && age < (long)cue.m_128451_("duration") ? cue.m_128461_("stage") : "";
    }

    public Vec3 orbAnchor(boolean palm) {
        double yaw = Math.toRadians(this.m_146908_());
        double forward = palm ? 1.25 : 0.34;
        return this.m_20182_().m_82520_(-Math.sin(yaw) * forward, palm ? 2.05 : 2.1, Math.cos(yaw) * forward);
    }

    public void cancelOrbCastVisual() {
        this.castTicks = 0;
    }

    public boolean isActionLocked() {
        return this.exhaustedTicks > 0 || this.beamAnimTicks > 0 || this.phaseAnimTicks > 0 || this.echoAnimTicks > 0 || this.castTicks > 0 || this.summonTicks > 0;
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_ORB_CUE, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_CORE_TINT, (Object)OrbType.GLASS.id);
    }

    @Override
    public void startSignatureCast(BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(DefectSkillSync.action(kind, castTicks), RegularBossCastHost.visualTicks(castTicks));
    }

    public void setBeamCharging(boolean charging) {
        this.beamCharging = charging;
    }

    public boolean m_6673_(DamageSource source) {
        LivingEntity le;
        if (source.m_7640_() instanceof ChargedOrbEntity) {
            return true;
        }
        Entity entity = source.m_7639_();
        if (entity instanceof LivingEntity && BossAggro.isBossSide((Entity)(le = (LivingEntity)entity))) {
            return true;
        }
        return super.m_6673_(source);
    }

    public DefectCoreEntity(EntityType<? extends DefectCoreEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 120;
        this.m_20242_(true);
        this.m_21530_();
        this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.defect_core"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public void m_142687_(Entity.RemovalReason reason) {
        if (!this.m_9236_().f_46443_) {
            BossBars.untrack((Entity)this, this.bossBar);
            Level level = this.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel rsl = (ServerLevel)level;
                BossMusic.release(rsl, (LivingEntity)this);
            }
        }
        super.m_142687_(reason);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 404.0).m_22268_(Attributes.f_22284_, 2.0).m_22268_(Attributes.f_22281_, 9.0).m_22268_(Attributes.f_22283_, 1.0).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22277_, 40.0).m_22268_(Attributes.f_22278_, 0.85);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 32.0f));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public int getPhase() {
        return this.phase;
    }

    public DefectOrbs orbs() {
        return this.orbs;
    }

    public void playCastAnim(OrbType type) {
        if (this.castTicks > 0 && this.castAction.equals("cast_" + type.id)) {
            return;
        }
        this.castAction = "cast_" + type.id;
        this.castAnim = switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS -> CAST_GLASS;
            case OrbType.LIGHTNING -> CAST_LIGHTNING;
            case OrbType.FROST -> CAST_FROST;
            case OrbType.ION -> CAST_ION;
            case OrbType.DARK -> CAST_DARK;
        };
        this.castTicks = this.castAnimTicks = (switch (type) {
            case OrbType.GLASS -> 23;
            case OrbType.DARK -> 26;
            default -> 20;
        });
        this.orbsCastCooldown = 50;
    }

    public void playEchoAnim() {
        this.echoAnimTicks = 28;
        this.showOrbCue("echo", OrbType.ION, -1, 28);
    }

    public void playBeamAnim() {
        this.beamAnimTicks = 285;
    }

    public void enterExhausted(int ticks) {
        this.enterExhausted(ticks, 0.0f);
    }

    public void enterExhausted(int ticks, float regenFraction) {
        this.exhaustedTicks = Math.max(this.exhaustedTicks, ticks);
        this.showOrbCue("", this.coreTint(), -1, 1);
        this.exhaustHealTotal = regenFraction > 0.0f ? this.m_21233_() * regenFraction : 80.0f;
        for (MobEffect fx : new MobEffect[]{MobEffects.f_19606_, MobEffects.f_19617_, MobEffects.f_19607_, MobEffects.f_19616_}) {
            this.m_21195_(fx);
        }
    }

    public boolean isExhausted() {
        return this.exhaustedTicks > 0;
    }

    public void playPhaseAnim() {
        this.phaseAnimTicks = 52;
        this.showOrbCue("phase", this.coreTint(), -1, 52);
    }

    public void playSummonAnim() {
        this.summonTicks = 17;
    }

    public OrbType coreTint() {
        return OrbType.byId((String)this.f_19804_.m_135370_(DATA_CORE_TINT));
    }

    public void setCoreTint(OrbType type) {
        this.f_19804_.m_135381_(DATA_CORE_TINT, (Object)type.id);
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128359_("core_tint", this.coreTint().id);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("core_tint")) {
            this.setCoreTint(OrbType.byId(tag.m_128461_("core_tint")));
        }
    }

    public boolean m_6469_(DamageSource source, float amount) {
        float gated = BossDamageGate.gate((LivingEntity)this, source, amount);
        if (gated <= 0.0f) {
            return false;
        }
        boolean hit = super.m_6469_(source, gated);
        if (hit) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        return hit;
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (!this.m_6084_()) {
            this.pendingMelee = null;
            return;
        }
        BossDamageGate.tick((LivingEntity)this);
        TrapBreaker.tick((LivingEntity)this);
        BossRage.tickBoss((LivingEntity)this);
        if (this.f_19797_ % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
                if (inst.m_19544_().m_19486_()) continue;
                this.m_21195_(inst.m_19544_());
            }
        }
        this.tickBossBar();
        this.tickPhases();
        this.tickFaceAndTarget();
        this.tickTurnAnim();
        this.tickHover();
        this.tickThrusterFx();
        this.tickBasicAttack();
        if (this.castTicks > 0) {
            --this.castTicks;
        }
        if (this.phaseAnimTicks > 0) {
            --this.phaseAnimTicks;
        }
        if (this.summonTicks > 0) {
            --this.summonTicks;
        }
        if (this.echoAnimTicks > 0) {
            --this.echoAnimTicks;
        }
        if (this.beamAnimTicks > 0) {
            --this.beamAnimTicks;
        }
        if (this.exhaustedTicks > 0) {
            Level level;
            MobEffectInstance inst;
            --this.exhaustedTicks;
            inst = this.m_9236_();
            if (inst instanceof ServerLevel) {
                ServerLevel gsl = (ServerLevel)inst;
                BlockPos gp = gsl.m_5452_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.m_20183_());
                double groundY = gp.m_123342_();
                double cy = this.m_20186_();
                this.m_20334_(0.0, 0.0, 0.0);
                this.f_19789_ = 0.0f;
                if (cy > groundY + 0.02) {
                    BossCollision.move((Entity)this, new Vec3(0.0, Math.max(groundY, cy - 0.9) - this.m_20186_(), 0.0));
                } else if (cy < groundY - 0.05) {
                    BossCollision.move((Entity)this, new Vec3(0.0, groundY - this.m_20186_(), 0.0));
                }
            }
            if (this.f_19797_ % 5 == 0) {
                this.m_5634_(this.exhaustHealTotal / 24.0f);
            }
            if (this.exhaustedTicks == 0 && (level = this.m_9236_()) instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.6f, 1.2f);
                this.playPhaseAnim();
                BossText.flavor("\u6545\u969c\u673a\u5668\u4eba", "\u91cd\u65b0\u5347\u8d77 \u2014\u2014 \u8870\u5f31\u7ed3\u675f");
                FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u843d\u5730\u8870\u5f31\u7ed3\u675f, \u91cd\u65b0\u5347\u7a7a");
            }
        }
        if (this.orbsCastCooldown > 0) {
            --this.orbsCastCooldown;
        }
        BossMusic.request((ServerLevel)this.m_9236_(), (LivingEntity)this);
        this.orbs.tick();
        this.syncAction();
    }

    private void syncAction() {
        String visualAction = "";
        int remaining = 0;
        if (this.exhaustedTicks > 0) {
            visualAction = "exhausted";
            remaining = this.exhaustedTicks;
        } else if (this.beamAnimTicks > 0) {
            visualAction = "beam";
            remaining = this.beamAnimTicks;
        } else if (this.phaseAnimTicks > 0) {
            visualAction = "phase_shift";
            remaining = this.phaseAnimTicks;
        } else if (this.echoAnimTicks > 0) {
            visualAction = "echo";
            remaining = this.echoAnimTicks;
        } else if (this.castTicks > 0) {
            visualAction = this.castAction;
            remaining = this.castTicks;
        } else if (this.summonTicks > 0) {
            visualAction = "orb_summon";
            remaining = this.summonTicks;
        }
        if (!visualAction.equals(this.lastVisualAction) || remaining > this.lastVisualTicks) {
            this.actionState.start(visualAction, remaining);
        }
        this.lastVisualAction = visualAction;
        this.lastVisualTicks = remaining;
    }

    private void tickFaceAndTarget() {
        double dz;
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        LivingEntity t = this.m_5448_();
        if (t == null || !t.m_6084_()) {
            ServerPlayer best = null;
            double bd = 2304.0;
            for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
                double d;
                if (!p.m_6084_() || p.m_7500_() || p.m_5833_() || !((d = this.m_20280_((Entity)p)) < bd)) continue;
                bd = d;
                best = p;
            }
            if (best != null) {
                this.m_6710_((LivingEntity)best);
            }
            t = best;
        }
        if (t == null) {
            return;
        }
        double dx = t.m_20185_() - this.m_20185_();
        if (dx * dx + (dz = t.m_20189_() - this.m_20189_()) * dz > 0.001) {
            float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            this.f_19859_ = this.m_146908_();
            this.m_146922_(yaw);
            this.f_20884_ = this.f_20883_;
            this.f_20883_ = yaw;
            this.m_5616_(yaw);
        }
        double dy = t.m_20188_() - this.m_20188_();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        this.f_19860_ = this.m_146909_();
        this.m_146926_((float)(-Math.toDegrees(Math.atan2(dy, Math.max(horiz, 0.01)))));
    }

    private void tickTurnAnim() {
        float dYaw = Mth.m_14177_((float)(this.m_146908_() - this.lastYaw));
        this.lastYaw = this.m_146908_();
        if (Math.abs(dYaw) > 1.2f) {
            this.turnTicks = 6;
            this.turningLeft = dYaw < 0.0f;
        } else if (this.turnTicks > 0) {
            --this.turnTicks;
        }
    }

    private void tickBasicAttack() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (this.pendingMelee != null) {
            if (this.exhaustedTicks > 0 || this.beamCharging || this.phaseAnimTicks > 0) {
                this.pendingMelee = null;
            } else if (sl.m_46467_() >= this.meleeReleaseAt) {
                double reach;
                LivingEntity victim = this.pendingMelee;
                this.pendingMelee = null;
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), this.pendingBasicMagic ? SoundEvents.f_11862_ : SoundEvents.f_12057_, SoundSource.HOSTILE, 1.2f, 1.1f);
                double d = reach = this.pendingBasicMagic ? 26.0 : 3.4;
                if (victim.m_6084_() && victim.m_9236_() == sl && this.m_20280_((Entity)victim) < reach * reach && this.m_142582_((Entity)victim) && BossTargets.valid(victim)) {
                    if (this.pendingBasicMagic) {
                        victim.m_6469_(sl.m_269111_().m_269104_((Entity)this, (Entity)this), 4.5f + (float)this.phase * 1.5f);
                    } else {
                        this.m_7327_((Entity)victim);
                    }
                }
            }
            return;
        }
        if (this.isActionLocked()) {
            return;
        }
        if (this.exhaustedTicks > 0) {
            return;
        }
        if (this.beamCharging) {
            return;
        }
        LivingEntity t = this.m_5448_();
        if (t == null || !t.m_6084_()) {
            return;
        }
        if (this.orbsCastCooldown > 0 || this.phaseAnimTicks > 0) {
            return;
        }
        if (--this.basicAttackCd > 0) {
            return;
        }
        this.basicAttackCd = this.phase >= 3 ? 36 : (this.phase == 2 ? 48 : 60);
        double dist = this.m_20270_((Entity)t);
        if (dist < 3.4) {
            this.playCastAnim(OrbType.ION);
            this.pendingMelee = t;
            this.pendingBasicMagic = false;
            this.meleeReleaseAt = sl.m_46467_() + 6L;
        } else if (dist < 26.0 && !this.orbs.castBasicSpell(t)) {
            float dmg = 4.5f + (float)this.phase * 1.5f;
            this.playCastAnim(OrbType.LIGHTNING);
            this.pendingMelee = t;
            this.pendingBasicMagic = true;
            this.meleeReleaseAt = sl.m_46467_() + 9L;
            FTBossMod.LOGGER.info("[FTBoss][\u6545\u969c\u673a\u5668\u4eba] \u57fa\u7840\u653b\u51fb(\u56de\u9000) -> {} ({} \u4f24\u5bb3, \u9636\u6bb5 {})", new Object[]{t.m_7755_().getString(), String.format("%.1f", Float.valueOf(dmg)), this.phase});
        }
    }

    private void tickBossBar() {
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            return;
        }
        this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / this.m_21233_())));
        MutableComponent want = Component.m_237115_((String)"boss.ftboss.defect_core").m_7220_((Component)Component.m_237113_((String)(" \u00a77[\u9636\u6bb5 " + DefectCoreEntity.roman(this.phase) + "]")));
        if (!want.getString().equals(this.bossBar.m_18861_().getString())) {
            this.bossBar.m_6456_((Component)want);
        }
        for (ServerPlayer p : new ArrayList(this.bossBar.m_8324_())) {
            boolean keep = p.m_6084_() && !p.m_213877_() && p.m_9236_() == this.m_9236_() && p.m_20280_((Entity)this) <= 2304.0;
            if (keep) continue;
            this.bossBar.m_6539_(p);
        }
        boolean visible = false;
        for (ServerPlayer p : ((ServerLevel)this.m_9236_()).m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (!p.m_6084_()) continue;
            BossBarVisibility.addPlayer(this.bossBar, p);
            visible = true;
        }
        if (this.bossBar.m_8323_() != visible) {
            BossBarVisibility.visible(this.bossBar, visible);
        }
    }

    private static String roman(int n) {
        return switch (n) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    private void tickPhases() {
        int want;
        float ratio = this.m_21223_() / this.m_21233_();
        int n = ratio < 0.35f ? 3 : (want = ratio < 0.7f ? 2 : 1);
        if (want <= this.phase) {
            return;
        }
        this.phase = want;
        this.orbs.unlockPhase(want);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 2.0f, want == 3 ? 0.6f : 0.9f);
            for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
                p.m_5661_((Component)Component.m_237113_((String)("\u00a75\u6545\u969c\u673a\u5668\u4eba \u00a77\u8fdb\u5165 \u00a7d\u9636\u6bb5 " + DefectCoreEntity.roman(want) + "\u00a77\uff01")), true);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u8fdb\u5165\u9636\u6bb5 {} (\u8840\u91cf {}/{})", new Object[]{this.phase, (int)this.m_21223_(), (int)this.m_21233_()});
    }

    private void tickHover() {
        double dz;
        double dy;
        if (!(this.m_9236_() instanceof ServerLevel)) {
            return;
        }
        if (this.exhaustedTicks > 0) {
            BlockPos p = this.m_9236_().m_5452_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.m_20183_());
            double ty = (double)p.m_123342_() + 0.05;
            double dy2 = ty - this.m_20186_();
            if (Math.abs(dy2) > 0.05) {
                BossCollision.move((Entity)this, new Vec3(0.0, Math.max(-0.4, Math.min(0.4, dy2)), 0.0));
            }
            return;
        }
        if (this.beamCharging) {
            return;
        }
        LivingEntity target = this.m_5448_();
        if (target == null || !target.m_6084_()) {
            return;
        }
        this.hoverPhase += 0.03;
        double radius = 3.5;
        double hx = target.m_20185_() + Math.cos(this.hoverPhase) * radius;
        double hz = target.m_20189_() + Math.sin(this.hoverPhase * 2.0) * radius;
        double hy = target.m_20186_() + 1.3 + Math.sin(this.hoverPhase * 1.5) * 0.2;
        double dx = hx - this.m_20185_();
        double dl = Math.sqrt(dx * dx + (dy = hy - this.m_20186_()) * dy + (dz = hz - this.m_20189_()) * dz);
        if (dl > 0.6) {
            double step = Math.min(0.21585, dl);
            BossCollision.move((Entity)this, new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
        }
        this.m_21563_().m_24960_((Entity)target, 30.0f, 30.0f);
    }

    private void tickThrusterFx() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double mdx = this.m_20185_() - this.f_19854_;
        double mdz = this.m_20189_() - this.f_19856_;
        boolean moving = Math.abs(mdx) + Math.abs(mdz) > 0.055;
        if (this.f_19797_ % (moving ? 3 : 5) == 0) {
            float yaw = (float)Math.toRadians(this.m_146908_());
            double rx = Math.cos(yaw);
            double rz = Math.sin(yaw);
            double y = this.m_20186_() + 0.12;
            for (int s = -1; s <= 1; s += 2) {
                Fx.particles((Level)sl, this.m_20185_() + rx * 0.45 * (double)s, y, this.m_20189_() + rz * 0.45 * (double)s, 1, (ParticleOptions)ParticleTypes.f_175830_, 0.06, 0.02);
            }
            Fx.particles((Level)sl, this.m_20185_(), y - 0.1, this.m_20189_(), moving ? 1 : 0, (ParticleOptions)ParticleTypes.f_123810_, 0.12, 0.01);
        }
        if (this.f_19797_ % 20 == 0) {
            Fx.ring((Level)sl, this.m_20185_(), this.m_20186_() + 0.08, this.m_20189_(), moving ? 1.1 : 0.8, 8, (ParticleOptions)ParticleTypes.f_123796_, 0.01);
        }
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        this.orbs.reset();
        SummonSweep.clear((LivingEntity)this, 80.0, MaggotMinionEntity.class);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel bsl = (ServerLevel)level;
            BossMusic.release(bsl, (LivingEntity)this);
        }
        if (!((level = this.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        BossBars.untrack((Entity)this, this.bossBar);
        sl.m_254849_((Entity)this, this.m_20185_(), this.m_20186_() + 0.8, this.m_20189_(), 4.0f, Level.ExplosionInteraction.NONE);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 3.0f, 0.6f);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11738_, SoundSource.HOSTILE, 2.0f, 0.5f);
        FTBossMod.LOGGER.info("[FTBoss] \u6545\u969c\u673a\u5668\u4eba\u8d85\u8f7d\u7206\u70b8 @({}, {}, {}) \u5a01\u529b={}", new Object[]{String.format("%.1f", this.m_20185_()), String.format("%.1f", this.m_20186_()), String.format("%.1f", this.m_20189_()), Float.valueOf(4.0f)});
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "defect_ctrl", 8, this::predicate)});
    }

    private PlayState predicate(AnimationState<DefectCoreEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        state.getController().setAnimationSpeed(1.0);
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if ("beam".equals(this.actionState.name())) {
            double age = this.actionState.elapsed(0.0f);
            double animationAge = age <= 160.0 ? age * 10.5 / 8.0 : 210.0 + (age - 160.0) * 2.5 / 3.0;
            state.getController().setAnimationSpeed(age > 0.0 ? animationAge / age : 1.3125);
            state.setAnimation(BEAM);
            return PlayState.CONTINUE;
        }
        if ("exhausted".equals(this.actionState.name())) {
            state.setAnimation(LANDING_WEAK);
            return PlayState.CONTINUE;
        }
        if (this.signatureCast.active()) {
            DefectSkillAnimation.apply(state, "signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST, this.signatureCast);
            return PlayState.CONTINUE;
        }
        if (this.actionState.name().startsWith("cast")) {
            state.setAnimation(switch (this.actionState.name()) {
                case "cast_glass" -> CAST_GLASS;
                case "cast_lightning" -> CAST_LIGHTNING;
                case "cast_frost" -> CAST_FROST;
                case "cast_ion" -> CAST_ION;
                case "cast_dark" -> CAST_DARK;
                default -> CAST;
            });
            return PlayState.CONTINUE;
        }
        if ("phase_shift".equals(this.actionState.name())) {
            state.setAnimation(PHASE);
            return PlayState.CONTINUE;
        }
        if ("orb_summon".equals(this.actionState.name())) {
            state.setAnimation(SUMMON);
            return PlayState.CONTINUE;
        }
        if ("echo".equals(this.actionState.name())) {
            state.setAnimation(ECHO);
            return PlayState.CONTINUE;
        }
        float turn = Mth.m_14177_((float)(this.m_146908_() - this.f_19859_));
        if (Math.abs(turn) > 1.2f) {
            state.setAnimation(turn > 0.0f ? TURN_L : TURN_R);
            return PlayState.CONTINUE;
        }
        if (this.f_20916_ > 0 || DefectCoreEntity.playing(state, HURT, 10.0)) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        state.setAnimation(this.isDrifting() ? FLOAT_MOVE : IDLE);
        return PlayState.CONTINUE;
    }

    private boolean isDrifting() {
        return Math.abs(this.m_20185_() - this.f_19854_) + Math.abs(this.m_20189_() - this.f_19856_) > 0.055;
    }

    private static boolean playing(AnimationState<DefectCoreEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    @Override
    public BossActionState ftDefectSkillState() {
        return this.signatureCast;
    }

    @Override
    public BossActionState ftDefectAmbientState() {
        return this.actionState;
    }
}

