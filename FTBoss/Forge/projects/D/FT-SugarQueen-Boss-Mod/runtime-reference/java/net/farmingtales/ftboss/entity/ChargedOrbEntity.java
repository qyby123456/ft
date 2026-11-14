/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
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

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.OrbLinkMath;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChargedOrbEntity
extends Monster
implements GeoAnimatable {
    public static final int LIFE = 400;
    private static final RawAnimation IDLE_A = RawAnimation.begin().thenLoop("animation.orb.idle");
    private static final RawAnimation BURST_A = RawAnimation.begin().thenPlay("animation.orb.burst");
    private static final RawAnimation SUMMON_A = RawAnimation.begin().thenPlay("animation.orb.summon");
    private static final RawAnimation FOCUS_A = RawAnimation.begin().thenLoop("animation.orb.focus");
    private static final RawAnimation DASH_A = RawAnimation.begin().thenLoop("animation.orb.dash");
    private static final RawAnimation RETURN_A = RawAnimation.begin().thenLoop("animation.orb.return");
    private static final RawAnimation BEAM_A = RawAnimation.begin().thenLoop("animation.orb.beam");
    private static final RawAnimation DRAIN_A = RawAnimation.begin().thenPlay("animation.orb.drain");
    private static final int BIRTH_TICKS = 10;
    private static final int FADE_TICKS = 6;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.m_135353_(ChargedOrbEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135030_);
    private UUID ownerId;
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.m_135353_(ChargedOrbEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DATA_CHARGE = SynchedEntityData.m_135353_(ChargedOrbEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<CompoundTag> DATA_VISUAL = SynchedEntityData.m_135353_(ChargedOrbEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private boolean reservedForCast;
    private boolean spentProjectile;
    private int focusDuration;
    private int focusTicks;
    private int fadeTicks;
    private Vec3 transferStart = Vec3.f_82478_;
    private int formationIndex;
    private int formationCount = 5;
    private int renderedVisualSequence = -1;
    private boolean maturedRequested;
    private double orbitPhase = 0.0;
    private int life = 400;
    private int charge = 0;
    private boolean gone = false;
    private static final double DASH_SPEED = 1.35;
    private static final float SLAM_DMG_BASE = 12.0f;
    private static final float SLAM_DMG_PER_PHASE = 6.0f;
    private static final int SLAM_CD = 100;
    private Move move = Move.ORBIT;
    private int moveTicks = 60;
    private int slamCd = 40;
    private int dashRepeat = 0;
    private double orbitR = 2.9;
    private double bobA = 0.35;
    private int spin = 1;
    private double dashX;
    private double dashY;
    private double dashZ;
    private int dashLeft = 0;
    private boolean dashHit = false;
    private int dashTargetId = -1;
    private float slamBonus = 0.0f;
    private float damageMul = 1.0f;
    private boolean blackHole = false;

    public boolean isOrbAvailable() {
        return !this.gone && this.m_6084_();
    }

    public boolean isReservedForCast() {
        return this.reservedForCast;
    }

    public boolean isSpentProjectile() {
        return this.spentProjectile;
    }

    public void setFormationSlot(int index, int count) {
        this.formationIndex = index;
        this.formationCount = Math.max(1, count);
        this.orbitPhase = (double)index * Math.PI * 2.0 / (double)this.formationCount;
        this.spin = 1;
    }

    public int formationSlot() {
        return this.formationIndex;
    }

    public void reserveForCast() {
        this.reservedForCast = true;
        this.maturedRequested = true;
    }

    public void focusForCast(int duration) {
        this.reserveForCast();
        this.focusDuration = duration;
        this.focusTicks = 0;
        this.transferStart = this.m_20182_();
        this.setVisualState("focus", duration);
    }

    public void releaseDark(int chargeTicks, float damageMultiplier, LivingEntity target) {
        this.reservedForCast = false;
        this.spentProjectile = true;
        this.maturedRequested = true;
        this.focusDuration = 0;
        this.charge = chargeTicks;
        this.f_19804_.m_135381_(DATA_CHARGE, (Object)chargeTicks);
        this.setSlamBonus(2.0f * ((float)chargeTicks / 20.0f));
        this.setDamageMul(damageMultiplier);
        this.setBlackHoleChance(true);
        this.setLifeTicks(200);
        this.beginDash(target);
        this.setVisualState("dash", 28);
    }

    public String visualState() {
        return ((CompoundTag)this.f_19804_.m_135370_(DATA_VISUAL)).m_128461_("state");
    }

    public float chargeFraction() {
        return Math.min(1.0f, (float)((Integer)this.f_19804_.m_135370_(DATA_CHARGE)).intValue() / 640.0f);
    }

    private void setVisualState(String state, int duration) {
        if (this.m_9236_().f_46443_ || state.equals(this.visualState())) {
            return;
        }
        CompoundTag previous = (CompoundTag)this.f_19804_.m_135370_(DATA_VISUAL);
        CompoundTag next = new CompoundTag();
        next.m_128359_("state", state);
        next.m_128356_("start", this.m_9236_().m_46467_());
        next.m_128405_("duration", duration);
        next.m_128405_("sequence", previous.m_128451_("sequence") + 1);
        this.f_19804_.m_135381_(DATA_VISUAL, (Object)next);
    }

    public ChargedOrbEntity(EntityType<? extends ChargedOrbEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_20242_(true);
        this.m_21530_();
        this.m_20225_(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 20.0).m_22268_(Attributes.f_22281_, 1.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22277_, 48.0);
    }

    protected void m_8099_() {
    }

    public OrbType getOrbType() {
        return OrbType.byId((String)this.f_19804_.m_135370_(DATA_TYPE));
    }

    public void setOrbType(OrbType t) {
        this.f_19804_.m_135381_(DATA_TYPE, (Object)t.id);
    }

    public void setOwnerBoss(DefectCoreEntity boss) {
        this.ownerId = boss.m_20148_();
        this.f_19804_.m_135381_(DATA_OWNER, (Object)boss.m_19879_());
        this.orbitPhase = this.f_19796_.m_188500_() * Math.PI * 2.0;
        this.orbitR = 2.3 + this.f_19796_.m_188500_() * 1.2;
        this.bobA = 0.2 + this.f_19796_.m_188500_() * 0.5;
        this.spin = this.f_19796_.m_188499_() ? 1 : -1;
        this.slamCd = 20 + this.f_19796_.m_188503_(60);
        this.moveTicks = 20 + this.f_19796_.m_188503_(60);
    }

    public int getChargeTicks() {
        return this.charge;
    }

    public void setLifeTicks(int ticks) {
        this.life = ticks;
    }

    public void setSlamBonus(float bonus) {
        this.slamBonus = Math.max(0.0f, bonus);
    }

    public int getLifeTicks() {
        return this.life <= 0 ? 1 : this.life;
    }

    public boolean isOwnedBy(DefectCoreEntity boss) {
        return this.ownerId != null && this.ownerId.equals(boss.m_20148_());
    }

    public boolean isOwnedByUuid(UUID owner) {
        return this.ownerId != null && this.ownerId.equals(owner);
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_TYPE, (Object)OrbType.GLASS.id);
        this.f_19804_.m_135372_(DATA_OWNER, (Object)-1);
        this.f_19804_.m_135372_(DATA_CHARGE, (Object)0);
        this.f_19804_.m_135372_(DATA_VISUAL, (Object)new CompoundTag());
    }

    public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_9236_().f_46443_ || this.gone) {
            return false;
        }
        DefectCoreEntity boss = this.ownerBoss();
        if (boss != null) {
            boss.orbs().onOrbDestroyed(this);
        }
        this.vanish(false, false);
        return true;
    }

    public void m_6667_(DamageSource source) {
        this.vanish(false, false);
    }

    public void absorbed() {
        this.vanish(true, false);
    }

    public void setDamageMul(float m) {
        this.damageMul = m <= 0.0f ? 1.0f : m;
    }

    public void setBlackHoleChance(boolean v) {
        this.blackHole = v;
    }

    public boolean m_6087_() {
        return !this.gone;
    }

    public void beginAssault() {
        if (this.gone || this.reservedForCast) {
            return;
        }
        this.move = Move.WINDUP;
        this.moveTicks = 10;
        this.slamCd = 0;
        this.dashRepeat = 0;
    }

    private void vanish(boolean absorbed, boolean matured) {
        if (this.gone) {
            return;
        }
        this.gone = true;
        this.reservedForCast = false;
        this.fadeTicks = 6;
        this.setVisualState(absorbed ? "drain" : "burst", 6);
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            DefectCoreEntity boss;
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 16, (ParticleOptions)(absorbed ? ParticleTypes.f_123789_ : this.particlesFor()), 0.35, 0.06);
            if (absorbed && (boss = this.ownerBoss()) != null) {
                for (int i = 0; i < 6; ++i) {
                    double t = (double)i / 6.0;
                    Fx.particles((Level)sl, this.m_20185_() + (boss.m_20185_() - this.m_20185_()) * t, this.m_20186_() + (boss.m_20186_() + 1.6 - this.m_20186_()) * t, this.m_20189_() + (boss.m_20189_() - this.m_20189_()) * t, 3, (ParticleOptions)ParticleTypes.f_123810_, 0.05, 0.01);
                }
            }
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_144242_, SoundSource.HOSTILE, 1.0f, absorbed ? 0.7f : 1.4f);
        }
    }

    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128359_("orb_type", this.getOrbType().id);
        if (this.ownerId != null) {
            tag.m_128362_("orb_owner", this.ownerId);
        }
        tag.m_128405_("orb_charge", this.charge);
        tag.m_128405_("orb_life", this.gone ? 0 : this.life);
        tag.m_128405_("orb_slot", this.formationIndex);
        tag.m_128405_("orb_slots", this.formationCount);
        tag.m_128379_("orb_spent", this.spentProjectile || this.gone);
        tag.m_128350_("orb_damage_mul", this.damageMul);
    }

    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.setOrbType(OrbType.byId(tag.m_128461_("orb_type")));
        if (tag.m_128403_("orb_owner")) {
            this.ownerId = tag.m_128342_("orb_owner");
        }
        this.charge = Math.max(0, tag.m_128451_("orb_charge"));
        this.f_19804_.m_135381_(DATA_CHARGE, (Object)this.charge);
        this.life = tag.m_128441_("orb_life") ? tag.m_128451_("orb_life") : 400;
        this.setFormationSlot(tag.m_128451_("orb_slot"), Math.max(1, tag.m_128451_("orb_slots")));
        this.maturedRequested = this.spentProjectile = tag.m_128471_("orb_spent");
        if (tag.m_128441_("orb_damage_mul")) {
            this.setDamageMul(tag.m_128457_("orb_damage_mul"));
        }
        if (this.spentProjectile) {
            this.setSlamBonus(2.0f * ((float)this.charge / 20.0f));
            this.setBlackHoleChance(true);
        }
    }

    private SimpleParticleType particlesFor() {
        return switch (this.getOrbType()) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS -> ParticleTypes.f_123810_;
            case OrbType.LIGHTNING -> ParticleTypes.f_175830_;
            case OrbType.DARK -> ParticleTypes.f_235898_;
            case OrbType.FROST -> ParticleTypes.f_175821_;
            case OrbType.ION -> ParticleTypes.f_123785_;
        };
    }

    public DefectCoreEntity ownerBoss() {
        DefectCoreEntity boss;
        Level level;
        Entity entity;
        if (this.m_9236_().f_46443_) {
            entity = this.m_9236_().m_6815_(((Integer)this.f_19804_.m_135370_(DATA_OWNER)).intValue());
        } else if (this.ownerId != null && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            entity = server.m_8791_(this.ownerId);
        } else {
            entity = null;
        }
        Entity owner = entity;
        return owner instanceof DefectCoreEntity ? (boss = (DefectCoreEntity)owner) : null;
    }

    public void m_8119_() {
        Level level;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        DefectCoreEntity boss = this.ownerBoss();
        if (!(this.ownerId == null || boss != null && boss.m_6084_())) {
            if (boss != null || this.f_19797_ > 40) {
                this.m_146870_();
            }
            return;
        }
        if (boss != null && ((Integer)this.f_19804_.m_135370_(DATA_OWNER)).intValue() != boss.m_19879_()) {
            this.f_19804_.m_135381_(DATA_OWNER, (Object)boss.m_19879_());
        }
        if (this.gone) {
            if ("drain".equals(this.visualState()) && boss != null) {
                Vec3 destination = boss.orbAnchor(false);
                this.m_146884_(this.m_20182_().m_165921_(destination, 0.45));
            }
            if (--this.fadeTicks <= 0) {
                this.m_146870_();
            }
            return;
        }
        if (this.reservedForCast) {
            if (this.focusDuration > 0 && boss != null) {
                Vec3 destination = boss.orbAnchor(true);
                double progress = OrbLinkMath.progress(++this.focusTicks, this.focusDuration);
                this.m_6034_(OrbLinkMath.arc(this.transferStart.f_82479_, destination.f_82479_, progress, 0.0), OrbLinkMath.arc(this.transferStart.f_82480_, destination.f_82480_, progress, 0.4), OrbLinkMath.arc(this.transferStart.f_82481_, destination.f_82481_, progress, 0.0));
            } else if (boss != null) {
                if (boss.orbLinkStage().startsWith("beam")) {
                    this.tickMovement(boss);
                    this.setVisualState("beam", 0);
                } else {
                    this.anchorOrbit(boss, 1.0, 0.0);
                    this.setVisualState("idle", 0);
                }
            }
            return;
        }
        ++this.charge;
        --this.life;
        if (this.f_19797_ % 10 == 0) {
            this.f_19804_.m_135381_(DATA_CHARGE, (Object)this.charge);
        }
        if (boss != null && boss.m_6084_()) {
            String string;
            if (this.f_19797_ <= 10 && !this.spentProjectile) {
                Vec3 start = boss.orbAnchor(true);
                double angle = (double)this.formationIndex * Math.PI * 2.0 / (double)this.formationCount;
                Vec3 end = boss.m_20182_().m_82520_(Math.cos(angle) * this.orbitR, 2.2, Math.sin(angle) * this.orbitR);
                double progress = OrbLinkMath.progress(this.f_19797_, 10.0);
                this.m_6034_(OrbLinkMath.arc(start.f_82479_, end.f_82479_, progress, 0.0), OrbLinkMath.arc(start.f_82480_, end.f_82480_, progress, 0.6), OrbLinkMath.arc(start.f_82481_, end.f_82481_, progress, 0.0));
                this.setVisualState("summon", 10);
                return;
            }
            this.tickMovement(boss);
            if (this.gone) {
                return;
            }
            if (boss.orbLinkStage().startsWith("beam") && !this.spentProjectile && this.move != Move.DASH) {
                string = "beam";
            } else {
                switch (this.move) {
                    case WINDUP: {
                        string = "focus";
                        break;
                    }
                    case DASH: {
                        string = "dash";
                        break;
                    }
                    case RETURN: {
                        string = "return";
                        break;
                    }
                    default: {
                        string = boss.orbLinkStage().startsWith("beam") ? "beam" : "idle";
                    }
                }
            }
            String state = string;
            this.setVisualState(state, 0);
        } else if (this.ownerId != null && boss == null) {
            this.m_146870_();
            return;
        }
        if (this.life <= 0) {
            if (this.spentProjectile) {
                this.vanish(false, true);
            } else if (boss != null && !this.maturedRequested) {
                boss.orbs().onOrbMatured(this);
            } else if (boss == null) {
                this.vanish(false, true);
            }
            return;
        }
        if (this.move != Move.DASH && this.f_19797_ % 4 == 0 && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl2 = (ServerLevel)level;
            Fx.particles((Level)sl2, this.m_20185_(), this.m_20186_(), this.m_20189_(), 2, (ParticleOptions)this.particlesFor(), 0.15, 0.01);
        }
    }

    private void tickMovement(DefectCoreEntity boss) {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (boss.orbLinkStage().startsWith("beam") && !this.spentProjectile && this.move != Move.DASH) {
            double angle = (double)this.formationIndex * Math.PI * 2.0 / (double)this.formationCount + (double)boss.f_19797_ * 0.018;
            double radius = "beam_fire".equals(boss.orbLinkStage()) ? 1.9 : 2.7;
            this.flyTo(boss.m_20185_() + Math.cos(angle) * radius, boss.m_20186_() + 2.65 + Math.sin(angle * 2.0) * 0.25, boss.m_20189_() + Math.sin(angle) * radius, 0.45);
            return;
        }
        if (this.slamCd > 0) {
            --this.slamCd;
        }
        LivingEntity target = this.currentTarget();
        switch (this.move) {
            case ORBIT: {
                this.anchorOrbit(boss, 1.0, 0.0);
                if (--this.moveTicks > 0) break;
                this.pickNextMode(target);
                break;
            }
            case SPIRAL: {
                this.anchorOrbit(boss, 1.35, 0.8);
                if (--this.moveTicks > 0) break;
                this.pickNextMode(target);
                break;
            }
            case WINDUP: {
                this.pullBack(boss, target);
                if (--this.moveTicks > 0) break;
                this.beginDash(target);
                break;
            }
            case DASH: {
                this.tickDash(sl, target);
                break;
            }
            case RETURN: {
                boolean home = this.flyTo(boss.m_20185_(), boss.m_20186_() + 1.6, boss.m_20189_(), 0.55);
                if (!home && --this.moveTicks > 0) break;
                this.move = Move.ORBIT;
                this.moveTicks = 40 + this.f_19796_.m_188503_(60);
            }
        }
    }

    private void anchorOrbit(DefectCoreEntity boss, double speedMul, double pulse) {
        this.orbitPhase += 0.045 * speedMul * (double)this.spin;
        double r = this.orbitR * (pulse > 0.0 ? 1.0 - pulse * (0.5 + 0.5 * Math.sin(this.orbitPhase * 1.7)) : 1.0);
        double hx = boss.m_20185_() + Math.cos(this.orbitPhase) * r;
        double hz = boss.m_20189_() + Math.sin(this.orbitPhase) * r;
        double hy = boss.m_20186_() + 2.2 + Math.sin(this.orbitPhase * 2.0) * this.bobA;
        this.flyTo(hx, hy, hz, 0.38);
    }

    private void pullBack(DefectCoreEntity boss, LivingEntity target) {
        double ax = target != null ? target.m_20185_() - boss.m_20185_() : 0.0;
        double az = target != null ? target.m_20189_() - boss.m_20189_() : 1.0;
        double len = Math.max(0.001, Math.sqrt(ax * ax + az * az));
        double px = boss.m_20185_() - ax / len * 3.2;
        double pz = boss.m_20189_() - az / len * 3.2;
        this.flyTo(px, boss.m_20186_() + 2.3, pz, 0.85);
    }

    private void pickNextMode(LivingEntity target) {
        boolean canSlam;
        boolean bl = canSlam = target != null && this.slamCd <= 0 && (double)this.m_20270_((Entity)target) < 26.0;
        if (canSlam && this.f_19796_.m_188501_() < this.slamChance()) {
            this.move = Move.WINDUP;
            this.moveTicks = 12;
            DefectCoreEntity boss = this.ownerBoss();
            this.dashRepeat = boss != null && boss.orbs().isEchoForm() ? 1 : 0;
            Level level = this.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11932_, SoundSource.HOSTILE, 0.9f, 1.6f);
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 10, (ParticleOptions)this.particlesFor(), 0.3, 0.05);
            }
        } else {
            this.move = this.f_19796_.m_188501_() < 0.5f ? Move.ORBIT : Move.SPIRAL;
            this.moveTicks = 60 + this.f_19796_.m_188503_(80);
        }
    }

    private float slamChance() {
        return switch (this.getOrbType()) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS, OrbType.LIGHTNING, OrbType.DARK -> 0.5f;
            case OrbType.FROST, OrbType.ION -> 0.16f;
        };
    }

    private void beginDash(LivingEntity target) {
        if (target == null) {
            this.toOrbit();
            return;
        }
        this.dashTargetId = target.m_19879_();
        double tx = target.m_20185_() + target.m_20184_().f_82479_ * 6.0;
        double ty = target.m_20186_() + 1.0 + target.m_20184_().f_82480_ * 6.0;
        double tz = target.m_20189_() + target.m_20184_().f_82481_ * 6.0;
        double dx = tx - this.m_20185_();
        double dy = ty - this.m_20186_();
        double dz = tz - this.m_20189_();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        this.dashX = dx / len;
        this.dashY = dy / len;
        this.dashZ = dz / len;
        this.dashLeft = (int)Math.min(28.0, len + 6.0);
        this.dashHit = false;
        this.move = Move.DASH;
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12232_, SoundSource.HOSTILE, 1.3f, 1.2f);
        }
    }

    private void tickDash(ServerLevel sl, LivingEntity target) {
        if (this.getOrbType() == OrbType.LIGHTNING && target != null && target.m_6084_()) {
            double tx = target.m_20185_() - this.m_20185_();
            double ty = target.m_20186_() + (double)target.m_20206_() * 0.5 - this.m_20186_();
            double tz = target.m_20189_() - this.m_20189_();
            double tl = Math.max(0.001, Math.sqrt(tx * tx + ty * ty + tz * tz));
            double k = 0.5;
            double mx = this.dashX + (tx / tl - this.dashX) * k;
            double my = this.dashY + (ty / tl - this.dashY) * k;
            double mz = this.dashZ + (tz / tl - this.dashZ) * k;
            double ml = Math.max(0.001, Math.sqrt(mx * mx + my * my + mz * mz));
            this.dashX = mx / ml;
            this.dashY = my / ml;
            this.dashZ = mz / ml;
        }
        double nx = this.m_20185_() + this.dashX * 1.35;
        double ny = this.m_20186_() + this.dashY * 1.35;
        double nz = this.m_20189_() + this.dashZ * 1.35;
        BlockHitResult hit = this.m_9236_().m_45547_(new ClipContext(this.m_20182_(), new Vec3(nx, ny, nz), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        if (hit.m_6662_() != HitResult.Type.MISS) {
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12346_, SoundSource.HOSTILE, 1.0f, 0.7f);
            Fx.particles((Level)sl, this.m_20185_(), this.m_20186_(), this.m_20189_(), 12, (ParticleOptions)this.particlesFor(), 0.3, 0.08);
            this.toOrbit();
            return;
        }
        this.m_6021_(nx, ny, nz);
        Fx.particles((Level)sl, nx, ny, nz, 6, (ParticleOptions)this.particlesFor(), 0.12, 0.02);
        Fx.particles((Level)sl, nx, ny, nz, 2, (ParticleOptions)ParticleTypes.f_123810_, 0.05, 0.01);
        if (!this.dashHit && target != null && target.m_6084_() && target.m_20191_().m_82400_(this.getOrbType() == OrbType.LIGHTNING ? 1.6 : 1.0).m_82390_(this.m_20182_())) {
            this.slamHit(sl, target);
        }
        if (--this.dashLeft <= 0) {
            if (this.dashRepeat > 0) {
                --this.dashRepeat;
                this.move = Move.WINDUP;
                this.moveTicks = 8;
            } else {
                this.toOrbit();
            }
        }
    }

    private void slamHit(ServerLevel sl, LivingEntity target) {
        int phase;
        this.dashHit = true;
        DefectCoreEntity boss = this.ownerBoss();
        int n = phase = boss != null ? boss.getPhase() : 1;
        if (this.getOrbType() == OrbType.DARK && this.blackHole && boss != null && boss.m_217043_().m_188501_() < 0.05f) {
            WizardSummon.castByName(sl, (LivingEntity)boss, target, "irons_spellbooks:black_hole", 4);
        }
        float dmg = BossScaling.dmg((LivingEntity)boss, (12.0f + (float)phase * 6.0f + this.slamBonus) * this.damageMul);
        target.f_19802_ = 0;
        target.m_6469_(sl.m_269111_().m_269333_((LivingEntity)boss), dmg * 0.25f);
        target.f_19802_ = 0;
        target.m_6469_(sl.m_269111_().m_269104_((Entity)boss, (Entity)boss), dmg * 0.25f);
        BossKnockback.knockback(target, 1.5, this.dashX, this.dashZ);
        if (this.getOrbType() == OrbType.GLASS) {
            for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, target.m_20191_().m_82400_(4.0))) {
                if (p == target || !p.m_6084_()) continue;
                p.f_19802_ = 0;
                p.m_6469_(sl.m_269111_().m_269333_((LivingEntity)boss), dmg * 0.15f);
                p.f_19802_ = 0;
                p.m_6469_(sl.m_269111_().m_269104_((Entity)boss, (Entity)boss), dmg * 0.15f);
                BossKnockback.knockback((LivingEntity)p, 0.9, this.dashX, this.dashZ);
            }
            Fx.ring((Level)sl, target.m_20185_(), target.m_20186_() + 0.4, target.m_20189_(), 3.4, 24, (ParticleOptions)ParticleTypes.f_123797_, 0.04);
        }
        Fx.particles((Level)sl, target.m_20185_(), target.m_20188_(), target.m_20189_(), 22, (ParticleOptions)this.particlesFor(), 0.45, 0.12);
        SoundEvent sfx = switch (this.getOrbType()) {
            default -> throw new IncompatibleClassChangeError();
            case OrbType.GLASS -> SoundEvents.f_11983_;
            case OrbType.LIGHTNING -> SoundEvents.f_12089_;
            case OrbType.DARK -> SoundEvents.f_215750_;
            case OrbType.FROST -> SoundEvents.f_11983_;
            case OrbType.ION -> SoundEvents.f_144243_;
        };
        sl.m_6263_(null, target.m_20185_(), target.m_20188_(), target.m_20189_(), sfx, SoundSource.HOSTILE, 1.2f, 1.0f);
        sl.m_6263_(null, target.m_20185_(), target.m_20188_(), target.m_20189_(), SoundEvents.f_12313_, SoundSource.HOSTILE, 1.0f, 0.8f);
        if (target instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)target;
        }
        FTBossMod.LOGGER.info("[FTBoss][\u5145\u80fd\u7403] \u649e\u51fb -> {} ({} \u4f24\u5bb3, \u9636\u6bb5 {})", new Object[]{target.m_7755_().getString(), String.format("%.1f", Float.valueOf(dmg)), phase});
    }

    private void toOrbit() {
        this.move = Move.RETURN;
        this.moveTicks = 40 + this.f_19796_.m_188503_(60);
        this.slamCd = 100;
        this.dashTargetId = -1;
        this.dashHit = false;
        this.dashRepeat = 0;
    }

    private boolean flyTo(double x, double y, double z, double step) {
        double dz;
        double dy;
        double dx = x - this.m_20185_();
        double dl = Math.sqrt(dx * dx + (dy = y - this.m_20186_()) * dy + (dz = z - this.m_20189_()) * dz);
        if (dl <= 0.35) {
            return true;
        }
        double s = Math.min(step, dl);
        this.m_6021_(this.m_20185_() + dx / dl * s, this.m_20186_() + dy / dl * s, this.m_20189_() + dz / dl * s);
        return false;
    }

    private LivingEntity currentTarget() {
        LivingEntity le;
        Entity e;
        if (this.dashTargetId >= 0 && (e = this.m_9236_().m_6815_(this.dashTargetId)) instanceof LivingEntity && (le = (LivingEntity)e).m_6084_()) {
            return le;
        }
        DefectCoreEntity boss = this.ownerBoss();
        LivingEntity t = boss != null ? boss.m_5448_() : null;
        return t != null && t.m_6084_() ? t : null;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "orb_ctrl", 2, this::predicate)});
    }

    private PlayState predicate(AnimationState<ChargedOrbEntity> state) {
        int sequence = ((CompoundTag)this.f_19804_.m_135370_(DATA_VISUAL)).m_128451_("sequence");
        if (sequence != this.renderedVisualSequence) {
            this.renderedVisualSequence = sequence;
            state.getController().forceAnimationReset();
        }
        state.getController().setTransitionLength(0);
        state.setAnimation(switch (this.visualState()) {
            case "summon" -> SUMMON_A;
            case "focus" -> FOCUS_A;
            case "dash" -> DASH_A;
            case "return" -> RETURN_A;
            case "beam" -> BEAM_A;
            case "drain" -> DRAIN_A;
            case "burst" -> BURST_A;
            default -> IDLE_A;
        });
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    public String describe() {
        return this.getOrbType().id + "(" + this.charge + "t/" + this.move + ")";
    }

    public static enum Move {
        ORBIT,
        SPIRAL,
        WINDUP,
        DASH,
        RETURN;

    }
}

