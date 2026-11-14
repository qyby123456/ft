/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastSync;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.body.BodySignals;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorKiting;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.dandanmotion.MotionServer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.xuanlannative.NativeSignals;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.zheergenfx.LightHooks;
import net.farmingtales.ftboss.zheergenphantom.CastReadability;
import net.farmingtales.ftboss.zheergenremainingbody.SkillBodyDispatch;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ManorFiveSkills {
    private static final UUID ATTACK_MODIFIER = UUID.fromString("f33bc9ab-89d1-466d-b648-11fd69710517");
    private static final DustParticleOptions GOLD = ManorFiveSkills.dust(1.0f, 0.74f, 0.2f);
    private static final DustParticleOptions VIOLET = ManorFiveSkills.dust(0.74f, 0.4f, 1.0f);
    private static final DustParticleOptions LIME = ManorFiveSkills.dust(0.72f, 1.0f, 0.2f);
    private static final DustParticleOptions CYAN = ManorFiveSkills.dust(0.2f, 0.9f, 1.0f);
    private static final DustParticleOptions SHADOW = ManorFiveSkills.dust(0.42f, 0.18f, 0.76f);
    private final EasterBossEntity boss;
    private ManorFiveCombatRules.Skill skill;
    private int rotation;
    private int age;
    private UUID targetId;
    private Vec3 origin = Vec3.f_82478_;
    private Vec3 direction = new Vec3(0.0, 0.0, 1.0);
    private Vec3 side = new Vec3(1.0, 0.0, 0.0);

    public ManorFiveSkills(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean isCasting() {
        return this.skill != null;
    }

    public void cancel() {
        this.skill = null;
        this.targetId = null;
        this.age = 0;
    }

    public void reset() {
        this.cancel();
        this.rotation = 0;
    }

    public int rotation() {
        return this.rotation;
    }

    public void restoreRotation(int n) {
        this.rotation = Math.floorMod(n, ManorFiveCombatRules.rotationLength(this.boss.getVariant()));
    }

    public void refreshAttackBonus() {
        boolean bl;
        AttributeInstance attributeInstance = this.boss.m_21051_(Attributes.f_22281_);
        if (attributeInstance == null) {
            return;
        }
        boolean bl2 = bl = ManorFiveCombatRules.supports(this.boss.getVariant()) && !this.boss.isMini();
        if (!bl) {
            attributeInstance.m_22120_(ATTACK_MODIFIER);
        } else if (attributeInstance.m_22111_(ATTACK_MODIFIER) == null) {
            attributeInstance.m_22118_(new AttributeModifier(ATTACK_MODIFIER, "ftboss.manor_five_attack", 0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public boolean tryStart(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        if (!ManorFiveCombatRules.hasExpandedSkills(this.boss.getVariant()) || this.boss.isMini() || this.boss.m_21525_() || !this.boss.m_6084_() || this.skill != null || !(livingEntity instanceof LivingEntity) || !ManorFiveSkills.validPlayer(livingEntity2 = livingEntity) || !this.boss.m_142582_((Entity)livingEntity2) || this.boss.m_20280_((Entity)livingEntity2) > 576.0) {
            return false;
        }
        ManorFiveCombatRules.Skill skill = ManorLivingTargets.airborneFive(ManorFiveCombatRules.select(this.boss.getVariant(), this.rotation), livingEntity, this.boss.getVariant());
        this.rotation = (this.rotation + 1) % ManorFiveCombatRules.rotationLength(this.boss.getVariant());
        if (skill == null) {
            return false;
        }
        this.skill = skill;
        this.age = ManorBalanceHooks.fiveAge(CastReadability.fiveAge(skill.windup - this.boss.getChallengeDifficulty().acceleratedTicks(skill.windup), this.skill), this.skill);
        this.targetId = livingEntity2.m_20148_();
        Vec3 vec3 = livingEntity2.m_20182_().m_82546_(this.boss.m_20182_()).m_82542_(1.0, 0.0, 1.0);
        this.direction = vec3.m_82556_() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : vec3.m_82541_();
        this.side = new Vec3(-this.direction.f_82481_, 0.0, this.direction.f_82479_);
        this.origin = skill.shape == ManorFiveCombatRules.Shape.WAVE || skill.shape == ManorFiveCombatRules.Shape.BEAM || skill.shape == ManorFiveCombatRules.Shape.CONE ? this.boss.m_20182_() : livingEntity2.m_20182_();
        boolean bl = skill.shape == ManorFiveCombatRules.Shape.CIRCLE || skill.shape == ManorFiveCombatRules.Shape.WAVE;
        this.boss.beginManorFiveCast(skill.duration() - this.age, skill.windup - this.age, this.origin, skill.radius, bl);
        net.farmingtales.ftboss.qingyilight.LightHooks.start(this.boss, this.skill, this.origin, this.direction, this.age);
        SkillBodyDispatch.start(this.boss, this.skill, this.age);
        LightHooks.start(this.boss, this.skill, this.origin, this.direction, this.age);
        MotionServer.start(this.boss, this.skill, this.age);
        net.farmingtales.ftboss.dandannativelights.LightHooks.start(this.boss, this.skill, this.origin, this.direction, this.age);
        this.boss.m_21563_().m_24960_((Entity)livingEntity2, 60.0f, 60.0f);
        this.boss.onManorFiveCastStart(skill);
        BodySignals.five(this.boss, this.skill, this.direction, this.age);
        this.boss.ftboss$nativeXuanlanCue(NativeSignals.five(this.boss, this.skill, this.origin, this.direction, this.age));
        for (LivingEntity livingEntity3 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            if (!ManorFiveSkills.validPlayer(livingEntity3) || !(livingEntity3.m_20280_((Entity)this.boss) <= 1024.0)) continue;
            Component.m_237113_((String)("\u00a7e" + EasterBossEntity.NAMES[skill.variant] + " \u00a76\u3010" + skill.title + "\u3011\u00a7f \u84c4\u529b\u4e2d\uff0c\u79bb\u5f00\u9884\u8b66\u533a\u57df\uff01"));
        }
        this.sound(serverLevel, SoundEvents.f_12376_, 0.85f);
        this.telegraph(serverLevel);
        FTBossMod.LOGGER.info("[FTBoss][ManorFive] {} \u84c4\u529b: {} tick\uff0c\u65b9\u5411\u4e0e\u843d\u70b9\u5df2\u9501\u5b9a", (Object)skill.title, (Object)(skill.windup - this.age));
        return true;
    }

    public void tick(ServerLevel serverLevel) {
        int n;
        if (this.boss.f_19797_ % 20 == 0) {
            this.refreshAttackBonus();
        }
        if (this.skill == null) {
            return;
        }
        LivingEntity livingEntity = ManorLivingTargets.resolve((Level)serverLevel, this.targetId);
        if (this.boss.isMini() || this.boss.m_21525_() || !this.boss.m_6084_() || this.boss.getVariant() != this.skill.variant || livingEntity == null || !ManorFiveSkills.validPlayer(livingEntity) || livingEntity.m_20280_((Entity)this.boss) > 1600.0 || ManorFight.isFinaleMember((LivingEntity)this.boss)) {
            this.boss.cancelManorFiveCast();
            this.cancel();
            return;
        }
        if (!this.boss.allowsParallelMelee() && !ManorKiting.enabled(this.boss)) {
            this.boss.m_21573_().m_26573_();
            this.boss.m_20256_(this.boss.m_20184_().m_82542_(0.0, 1.0, 0.0));
        }
        ++this.age;
        if (this.age % 4 == 0) {
            this.telegraph(serverLevel);
        }
        if ((n = ManorFiveCombatRules.pulseAt(this.skill, this.age)) >= 0) {
            this.strike(serverLevel, n);
        }
        if (this.skill == null) {
            return;
        }
        if (this.age >= this.skill.duration()) {
            this.boss.finishManorFiveCast(this.origin);
            this.cancel();
        }
    }

    private void strike(ServerLevel serverLevel, int n) {
        SkillBodyDispatch.pulse(this.boss, this.skill, n);
        this.hitShape(serverLevel, this.skill, this.center(n), this.direction, this.side, n, 1.0, false);
        QingyiCastSync.pulseNative(this.boss);
    }

    private int hitShape(ServerLevel serverLevel, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, Vec3 vec33, int n, double d, boolean bl) {
        net.farmingtales.ftboss.qingyilight.LightHooks.pulse(this.boss, skill, vec3, vec32, n);
        LightHooks.pulse(this.boss, skill, vec3, vec32, n);
        MotionServer.pulse(this.boss, skill);
        net.farmingtales.ftboss.dandannativelights.LightHooks.pulse(this.boss, skill, vec3, vec32, n);
        if (skill == null) {
            return 0;
        }
        int n2 = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            boolean bl2;
            Vec3 vec34;
            if (!ManorFiveSkills.validPlayer(livingEntity) || livingEntity.m_20280_((Entity)this.boss) > 1600.0 || !ManorFiveCombatRules.contains(skill, n, (vec34 = livingEntity.m_20182_().m_82546_(vec3)).m_82526_(vec32), vec34.m_82526_(vec33), vec34.f_82480_, (double)livingEntity.m_20205_() * 0.5) || !this.clearLine(serverLevel, vec3.m_82520_(0.0, 1.0, 0.0), livingEntity.m_20191_().m_82399_())) continue;
            livingEntity.f_19802_ = 0;
            float f = (float)((double)this.boss.manorSkillDamage(livingEntity, skill.damage, skill.meleeMultiplier()) * d);
            if (bl) {
                ManorAdaptiveDamage.hurtSkill(livingEntity, (Entity)this.boss, this.boss.m_269291_().m_269341_(), f);
                bl2 = true;
            } else {
                bl2 = livingEntity.m_6469_(this.boss.m_269291_().m_269333_((LivingEntity)this.boss), f);
            }
            if (!this.boss.m_6084_()) {
                return n2;
            }
            if (!bl2) continue;
            ++n2;
            this.applyEffects(livingEntity, skill, vec3, vec32, n);
            this.impact(serverLevel, livingEntity.m_20182_().m_82520_(0.0, 1.0, 0.0), 12, skill);
        }
        ParticleOptions particleOptions = ManorFiveSkills.impactParticle(skill);
        if (skill.shape == ManorFiveCombatRules.Shape.WAVE) {
            this.ring(serverLevel, vec3.m_82520_(0.0, 0.8, 0.0), ManorFiveCombatRules.waveRadius(skill, n), particleOptions, 40);
        } else if (skill.shape == ManorFiveCombatRules.Shape.BEAM) {
            this.line(serverLevel, vec3.m_82520_(0.0, 1.0, 0.0), vec3.m_82549_(vec32.m_82490_(skill.radius)).m_82520_(0.0, 1.0, 0.0), particleOptions, 32);
        } else if (skill.shape == ManorFiveCombatRules.Shape.CROSS) {
            LivingEntity livingEntity;
            livingEntity = vec3.m_82520_(0.0, 0.8, 0.0);
            this.line(serverLevel, livingEntity.m_82546_(vec32.m_82490_(skill.radius)), livingEntity.m_82549_(vec32.m_82490_(skill.radius)), particleOptions, 24);
            this.line(serverLevel, livingEntity.m_82546_(vec33.m_82490_(skill.radius)), livingEntity.m_82549_(vec33.m_82490_(skill.radius)), particleOptions, 24);
        } else if (skill == ManorFiveCombatRules.Skill.DANDAN_STAR) {
            this.line(serverLevel, vec3.m_82520_(0.0, 7.0, 0.0), vec3.m_82520_(0.0, 0.2, 0.0), (ParticleOptions)ParticleTypes.f_123810_, 24);
            this.ring(serverLevel, vec3.m_82520_(0.0, 0.3, 0.0), skill.radius, particleOptions, 28);
        } else if (skill == ManorFiveCombatRules.Skill.QINGYI_THUNDER) {
            this.line(serverLevel, vec3.m_82520_(0.0, 8.0, 0.0), vec3.m_82520_(0.0, 0.2, 0.0), (ParticleOptions)ParticleTypes.f_175830_, 28);
            this.ring(serverLevel, vec3.m_82520_(0.0, 0.4, 0.0), skill.radius, (ParticleOptions)ParticleTypes.f_123810_, 32);
        } else if (skill == ManorFiveCombatRules.Skill.QINGYI_TIDE) {
            for (int i = 0; i <= 24; ++i) {
                double d2 = Math.toRadians(-50.0 + 100.0 * (double)i / 24.0);
                Vec3 vec35 = vec32.m_82490_(Math.cos(d2)).m_82549_(vec33.m_82490_(Math.sin(d2)));
                this.particle(serverLevel, (ParticleOptions)ParticleTypes.f_123769_, vec3.m_82549_(vec35.m_82490_(skill.radius)).m_82520_(0.0, 0.6, 0.0));
                this.particle(serverLevel, (ParticleOptions)ParticleTypes.f_175830_, vec3.m_82549_(vec35.m_82490_(skill.radius * 0.5)).m_82520_(0.0, 0.8, 0.0));
            }
        } else {
            this.impact(serverLevel, vec3.m_82520_(0.0, 0.6, 0.0), 24, skill);
        }
        this.sound(serverLevel, ManorFiveSkills.impactSound(skill), vec3, 0.9f + (float)n * 0.12f);
        FTBossMod.LOGGER.info("[FTBoss][ManorFive] {} \u7b2c {}/{} \u6bb5: \u6210\u529f\u547d\u4e2d {} \u4eba\uff0c\u57fa\u7840\u4e0b\u9650 {}\uff0c\u666e\u653b\u57fa\u51c6\u500d\u7387 {}" + (d == 1.0 ? "" : "\uff08\u6b8b\u5f71\u91cd\u653e \u00d7{}\uff09"), new Object[]{skill.title, n + 1, skill.pulses, n2, Float.valueOf(skill.damage), skill.meleeMultiplier(), String.format("%.2f", d)});
        return n2;
    }

    public int echoCast(ServerLevel serverLevel, ManorFiveCombatRules.Skill skill, Vec3 vec3, LivingEntity livingEntity, double d, boolean bl) {
        Vec3 vec32;
        if (skill == null || vec3 == null) {
            return 0;
        }
        Vec3 vec33 = Vec3.f_82478_;
        if (livingEntity != null && (vec32 = livingEntity.m_20182_().m_82546_(vec3).m_82542_(1.0, 0.0, 1.0)).m_82556_() > 1.0E-6) {
            vec33 = vec32.m_82541_();
        }
        if (vec33.m_82556_() < 1.0E-6) {
            vec33 = new Vec3(this.boss.m_20154_().f_82479_, 0.0, this.boss.m_20154_().f_82481_);
            if (vec33.m_82556_() < 1.0E-6) {
                vec33 = new Vec3(0.0, 0.0, 1.0);
            }
            vec33 = vec33.m_82541_();
        }
        vec32 = new Vec3(-vec33.f_82481_, 0.0, vec33.f_82479_);
        return this.hitShape(serverLevel, skill, vec3, vec33, vec32, 0, d, bl);
    }

    private void applyEffects(LivingEntity livingEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        boolean bl = n == skill.pulses - 1;
        Vec3 vec33 = livingEntity.m_20182_().m_82546_(vec3).m_82542_(1.0, 0.0, 1.0);
        if (vec33.m_82556_() < 1.0E-6) {
            vec33 = vec32;
        }
        vec33 = vec33.m_82541_();
        if (skill == ManorFiveCombatRules.Skill.SHADOW_CAGE && !bl) {
            vec33 = vec33.m_82490_(-1.0);
        }
        double d = bl ? skill.knockback : skill.knockback * 0.55;
        BossKnockback.knockback((Entity)this.boss, livingEntity, d, -vec33.f_82479_, -vec33.f_82481_);
        double d2 = Math.max(0.0, 1.0 - livingEntity.m_21133_(Attributes.f_22278_));
        Vec3 vec34 = livingEntity.m_20184_();
        livingEntity.m_20334_(vec34.f_82479_, Math.max(vec34.f_82480_, BossKnockback.vertical((Entity)this.boss, (Entity)livingEntity, skill.lift * d2)), vec34.f_82481_);
        livingEntity.f_19864_ = true;
        switch (skill) {
            case YOUZI_SEAL: 
            case YOUZI_ECHO: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 70, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19619_, 100, 0));
                break;
            }
            case DANDAN_REAP: 
            case DANDAN_STAR: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 80, 1));
                if (!bl) break;
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 1));
                break;
            }
            case SHADOW_CROSS: 
            case SHADOW_CAGE: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 80, 0));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 1));
                break;
            }
            case XUANLAN_RAIL: 
            case XUANLAN_RING: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19599_, 80, 0));
                break;
            }
            case QINGYI_TIDE: 
            case QINGYI_THUNDER: 
            case QINGYI_STAR: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 80, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 80, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19619_, 100, 0));
                break;
            }
            case LEMON_ACID: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 80, 0));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 80, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 100, 1));
                break;
            }
            case LEMON_FLAMBE: {
                livingEntity.m_20254_(4);
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 100, 1));
            }
        }
    }

    private void telegraph(ServerLevel serverLevel) {
        ParticleOptions particleOptions = this.color(this.skill);
        Vec3 vec3 = this.origin.m_82520_(0.0, 0.12, 0.0);
        switch (this.skill.shape) {
            case CIRCLE: {
                this.ring(serverLevel, vec3, this.skill.radius, particleOptions, 28);
                break;
            }
            case WAVE: {
                int n = Math.min(this.skill.pulses - 1, Math.max(0, (this.age - this.skill.windup) / this.skill.interval));
                this.ring(serverLevel, vec3, ManorFiveCombatRules.waveRadius(this.skill, n), particleOptions, 32);
                break;
            }
            case ECHO: {
                for (int i = 0; i < this.skill.pulses; ++i) {
                    this.ring(serverLevel, this.center(i).m_82520_(0.0, 0.12, 0.0), this.skill.radius, particleOptions, 14);
                }
                break;
            }
            case CROSS: {
                this.line(serverLevel, vec3.m_82546_(this.direction.m_82490_(this.skill.radius)), vec3.m_82549_(this.direction.m_82490_(this.skill.radius)), particleOptions, 20);
                this.line(serverLevel, vec3.m_82546_(this.side.m_82490_(this.skill.radius)), vec3.m_82549_(this.side.m_82490_(this.skill.radius)), particleOptions, 20);
                break;
            }
            case BEAM: {
                Vec3 vec32 = this.origin.m_82520_(0.0, 1.0, 0.0);
                double d = ManorFiveCombatRules.beamHalfWidth(this.skill);
                for (double d2 : new double[]{-d, d}) {
                    Vec3 vec33 = vec32.m_82549_(this.side.m_82490_(d2));
                    this.line(serverLevel, vec33, vec33.m_82549_(this.direction.m_82490_(this.skill.radius)), particleOptions, 20);
                }
                break;
            }
            case CONE: {
                for (int i = 0; i <= 24; ++i) {
                    double d = Math.toRadians(-50.0 + 100.0 * (double)i / 24.0);
                    Vec3 vec34 = this.direction.m_82490_(Math.cos(d)).m_82549_(this.side.m_82490_(Math.sin(d)));
                    Vec3 vec35 = vec3.m_82549_(vec34.m_82490_(this.skill.radius));
                    this.particle(serverLevel, particleOptions, vec35);
                    if (i != 0 && i != 24) continue;
                    this.line(serverLevel, vec3, vec35, particleOptions, 10);
                }
                break;
            }
        }
    }

    private Vec3 center(int n) {
        if (this.skill.shape != ManorFiveCombatRules.Shape.ECHO) {
            return this.origin;
        }
        double d = (double)n * Math.PI * 2.0 / 3.0;
        return this.origin.m_82549_(this.direction.m_82490_(Math.cos(d) * 3.0)).m_82549_(this.side.m_82490_(Math.sin(d) * 3.0));
    }

    private void ring(ServerLevel serverLevel, Vec3 vec3, double d, ParticleOptions particleOptions, int n) {
        for (int i = 0; i < n; ++i) {
            double d2 = (double)i * Math.PI * 2.0 / (double)n;
            this.particle(serverLevel, particleOptions, vec3.m_82520_(Math.cos(d2) * d, 0.0, Math.sin(d2) * d));
        }
    }

    private void line(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, ParticleOptions particleOptions, int n) {
        Vec3 vec33 = serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_();
        for (int i = 0; i <= n; ++i) {
            this.particle(serverLevel, particleOptions, vec3.m_165921_(vec33, (double)i / (double)n));
        }
    }

    private void particle(ServerLevel serverLevel, ParticleOptions particleOptions, Vec3 vec3) {
        serverLevel.m_8767_(particleOptions, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void impact(ServerLevel serverLevel, Vec3 vec3, int n, ManorFiveCombatRules.Skill skill) {
        serverLevel.m_8767_(ManorFiveSkills.impactParticle(skill), vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, n, 0.65, 0.65, 0.65, 0.05);
    }

    private ParticleOptions color(ManorFiveCombatRules.Skill skill) {
        return switch (skill.variant) {
            case 0 -> GOLD;
            case 1 -> VIOLET;
            case 4 -> {
                if (skill == ManorFiveCombatRules.Skill.LEMON_FLAMBE) {
                    yield GOLD;
                }
                yield LIME;
            }
            case 5, 6 -> CYAN;
            default -> SHADOW;
        };
    }

    private static ParticleOptions impactParticle(ManorFiveCombatRules.Skill skill) {
        return switch (skill.variant) {
            case 0 -> ParticleTypes.f_123808_;
            case 1 -> ParticleTypes.f_123789_;
            case 4 -> {
                if (skill == ManorFiveCombatRules.Skill.LEMON_FLAMBE) {
                    yield ParticleTypes.f_123744_;
                }
                yield ParticleTypes.f_175833_;
            }
            case 5 -> {
                if (skill == ManorFiveCombatRules.Skill.QINGYI_STAR) {
                    yield ParticleTypes.f_123810_;
                }
                yield ParticleTypes.f_175830_;
            }
            case 6 -> ParticleTypes.f_175830_;
            default -> ParticleTypes.f_235898_;
        };
    }

    private static SoundEvent impactSound(ManorFiveCombatRules.Skill skill) {
        return switch (skill.variant) {
            case 0 -> SoundEvents.f_12057_;
            case 1 -> SoundEvents.f_11852_;
            case 4 -> SoundEvents.f_11937_;
            case 5 -> {
                if (skill == ManorFiveCombatRules.Skill.QINGYI_TIDE) {
                    yield SoundEvents.f_12519_;
                }
                yield SoundEvents.f_12521_;
            }
            case 6 -> SoundEvents.f_215771_;
            default -> SoundEvents.f_12317_;
        };
    }

    private void sound(ServerLevel serverLevel, SoundEvent soundEvent, Vec3 vec3, float f) {
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, soundEvent, SoundSource.HOSTILE, 1.0f, f);
    }

    private void sound(ServerLevel serverLevel, SoundEvent soundEvent, float f) {
        this.sound(serverLevel, soundEvent, this.origin, f);
    }

    private boolean clearLine(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_6662_() == HitResult.Type.MISS;
    }

    private static boolean validPlayer(LivingEntity livingEntity) {
        return livingEntity.m_6084_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_();
    }

    private static DustParticleOptions dust(float f, float f2, float f3) {
        return new DustParticleOptions(new Vector3f(f, f2, f3), 1.15f);
    }
}

