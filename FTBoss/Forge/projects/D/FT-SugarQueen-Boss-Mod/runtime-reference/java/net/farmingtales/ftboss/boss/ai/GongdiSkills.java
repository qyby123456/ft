/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.GongdiCastLifecycle;
import net.farmingtales.ftboss.boss.ai.GongdiGuillotineCooldown;
import net.farmingtales.ftboss.boss.ai.ManorKiting;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class GongdiSkills {
    private static final ResourceKey<DamageType> GUILLOTINE = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "gongdi_guillotine"));
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.8f, 0.06f, 0.12f), 1.1f);
    private static final int ROT_GUILLOTINE = 0;
    private static final int ROT_STRIKE = 1;
    private static final int ROT_PULL = 2;
    private static final int[] ROTATION = new int[]{0, 1, 2};
    private final EasterBossEntity boss;
    private final Map<UUID, BleedingTarget> bleeding = new HashMap<UUID, BleedingTarget>();
    private String skill = "";
    private int windup;
    private int rotation;
    private LivingEntity lockedTarget;
    private Vec3 direction = Vec3.f_82478_;
    private long strikeReadyAt;
    private long pullReadyAt;
    private long executeReadyAt;
    private final GongdiGuillotineCooldown guillotineResetWindow;

    public GongdiSkills(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
        this.guillotineResetWindow = new GongdiGuillotineCooldown();
    }

    public boolean isCasting() {
        return this.windup > 0;
    }

    public void reset() {
        GongdiCastLifecycle.cancel(this.boss);
        this.windup = 0;
        this.skill = "";
        this.lockedTarget = null;
        this.bleeding.clear();
        this.rotation = 0;
        this.strikeReadyAt = 0L;
        this.pullReadyAt = 0L;
        this.executeReadyAt = 0L;
        this.guillotineResetWindow.clear();
    }

    public boolean validTarget(LivingEntity livingEntity, double d) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.boss.m_20182_());
        return livingEntity.m_9236_() == this.boss.m_9236_() && livingEntity.m_6084_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && GongdiCombatRules.inRange(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, d) && this.boss.m_142582_((Entity)livingEntity);
    }

    public void onHit(LivingEntity livingEntity) {
        if (!livingEntity.m_6084_() || ManorLivingTargets.creative(livingEntity) || livingEntity.m_5833_()) {
            return;
        }
        long l = this.boss.m_9236_().m_46467_();
        BleedingTarget bleedingTarget = this.bleeding.get(livingEntity.m_20148_());
        if (bleedingTarget == null || bleedingTarget.player() != livingEntity) {
            bleedingTarget = new BleedingTarget(livingEntity, new GongdiCombatRules.Bleed());
            this.bleeding.put(livingEntity.m_20148_(), bleedingTarget);
        }
        int n = bleedingTarget.bleed().addHit(l, this.boss.isGongdiBloodRageFull());
        Component.m_237113_((String)("\u00a7c\u51fa\u8840 " + n + "/5 \u00a77\u00b7 5 \u79d2\uff1b\u8fdc\u79bb\u65a7\u5203\uff0c\u63d0\u9632\u65ad\u5934\u53f0"));
    }

    public void onAbilityHit(ServerLevel serverLevel, LivingEntity livingEntity, float f) {
        this.boss.onGongdiAbilityHit(serverLevel, livingEntity, f);
        this.onHit(livingEntity);
    }

    public boolean tryStart(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (this.windup > 0 || !GongdiCastLifecycle.canStart(this.boss, serverLevel, livingEntity) || !this.validTarget(livingEntity, 16.875)) {
            return false;
        }
        long l = serverLevel.m_46467_();
        int n = this.stacks(livingEntity, l);
        if (l >= GongdiGuillotineCooldown.eligibleAt(this.executeReadyAt, this.guillotineResetWindow, l) && this.validTarget(livingEntity, 10.125) && (n >= 3 || livingEntity.m_21223_() + livingEntity.m_6103_() <= GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(livingEntity), n))) {
            this.start(serverLevel, livingEntity, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, 10.125);
            this.executeReadyAt = GongdiGuillotineCooldown.triggered(l + 600L, this.guillotineResetWindow);
            return true;
        }
        for (int i = 0; i < ROTATION.length; ++i) {
            int n2 = ROTATION[Math.floorMod(this.rotation, ROTATION.length)];
            this.rotation = (Math.floorMod(this.rotation, ROTATION.length) + 1) % ROTATION.length;
            if (n2 == 0 && l >= GongdiGuillotineCooldown.eligibleAt(this.executeReadyAt, this.guillotineResetWindow, l) && this.validTarget(livingEntity, 10.125)) {
                this.start(serverLevel, livingEntity, "guillotine", GongdiCombatRules.EXECUTE_WINDUP, 10.125);
                this.executeReadyAt = GongdiGuillotineCooldown.triggered(l + 600L, this.guillotineResetWindow);
                return true;
            }
            if (n2 == 1 && l >= this.strikeReadyAt && this.validTarget(livingEntity, 7.65)) {
                this.start(serverLevel, livingEntity, "crippling_strike", GongdiCombatRules.STRIKE_WINDUP, 7.65);
                this.strikeReadyAt = l + 100L;
                return true;
            }
            if (n2 != 2 || l < this.pullReadyAt || !this.validTarget(livingEntity, 15.75)) continue;
            this.start(serverLevel, livingEntity, "apprehend", GongdiCombatRules.PULL_WINDUP, 15.75);
            this.pullReadyAt = l + 160L;
            return true;
        }
        return false;
    }

    private void start(ServerLevel serverLevel, LivingEntity livingEntity, String string, int n, double d) {
        Object object;
        n = this.boss.getChallengeDifficulty().acceleratedTicks(n);
        this.skill = string;
        this.windup = n;
        this.lockedTarget = livingEntity;
        Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.boss.m_20182_());
        this.direction = new Vec3(vec3.f_82479_, 0.0, vec3.f_82481_).m_82541_();
        if (this.direction.m_82556_() < 0.001) {
            object = this.boss.m_20154_();
            this.direction = new Vec3(object.f_82479_, 0.0, object.f_82481_).m_82541_();
            if (this.direction.m_82556_() < 0.001) {
                this.direction = new Vec3(0.0, 0.0, 1.0);
            }
        }
        this.boss.m_146922_((float)Math.toDegrees(Math.atan2(-this.direction.f_82479_, this.direction.f_82481_)));
        this.boss.m_5618_(this.boss.m_146908_());
        this.boss.beginGongdiCast(string, n, d, this.direction);
        object = switch (string) {
            case "apprehend" -> "\u65e0\u60c5\u94c1\u624b \u00b7 \u4fa7\u79fb\u8eb2\u5f00\u62c9\u56de";
            case "crippling_strike" -> "\u81f4\u6b8b\u6253\u51fb \u00b7 \u540e\u64a4\u8eb2\u5f00\u91cd\u51fb";
            default -> "\u8bfa\u514b\u8428\u65af\u65ad\u5934\u53f0 \u00b7 \u62c9\u5f00\u8ddd\u79bb\u6216\u5bfb\u627e\u63a9\u4f53";
        };
        Component.m_237113_((String)("\u00a74\u529f\u5e95\uff1a" + (String)object));
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_12376_, SoundSource.HOSTILE, 1.0f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Gongdi] {} windup={} target={}", new Object[]{string, n, livingEntity.m_6302_()});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void tick(ServerLevel serverLevel) {
        Object object;
        if (this.boss.m_9236_() != serverLevel) {
            return;
        }
        if (!GongdiCastLifecycle.canBegin(this.boss)) {
            this.reset();
            return;
        }
        long l = serverLevel.m_46467_();
        Iterator<BleedingTarget> iterator = this.bleeding.values().iterator();
        while (iterator.hasNext()) {
            object = iterator.next();
            Object object2 = ((BleedingTarget)object).player();
            if (ManorLivingTargets.resolve((Level)serverLevel, object2.m_20148_()) != object2 || !object2.m_6084_() || ManorLivingTargets.creative((LivingEntity)object2) || object2.m_5833_() || object2.m_20280_((Entity)this.boss) > 4096.0) {
                iterator.remove();
                continue;
            }
            float f = ((BleedingTarget)object).bleed().pulse(l);
            if (f > 0.0f) {
                object2.m_6469_(this.boss.m_269291_().m_269333_((LivingEntity)this.boss), f);
                if (!this.boss.m_6084_() || this.boss.m_21525_()) {
                    this.reset();
                    return;
                }
                serverLevel.m_8767_((ParticleOptions)BLOOD, object2.m_20185_(), object2.m_20186_() + 1.1, object2.m_20189_(), ((BleedingTarget)object).bleed().stacks(l) + 2, 0.2, 0.4, 0.2, 0.0);
            }
            if (!((BleedingTarget)object).bleed().expired(l)) continue;
            iterator.remove();
        }
        if (this.windup <= 0) {
            return;
        }
        if (!ManorLivingTargets.valid((Entity)this.boss, this.lockedTarget) || !this.boss.getSkillCue().m_128461_("gongdiSkill").equals(this.skill) || this.boss.getSkillCue().m_128471_("released")) {
            GongdiCastLifecycle.cancel(this.boss);
            this.windup = 0;
            this.skill = "";
            this.lockedTarget = null;
            return;
        }
        if (!ManorKiting.enabled(this.boss)) {
            this.boss.m_21573_().m_26573_();
        }
        this.boss.m_146922_((float)Math.toDegrees(Math.atan2(-this.direction.f_82479_, this.direction.f_82481_)));
        this.boss.m_5618_(this.boss.m_146908_());
        if (this.skill.equals("guillotine") && this.windup % 4 == 0) {
            serverLevel.m_8767_((ParticleOptions)BLOOD, this.lockedTarget.m_20185_(), this.lockedTarget.m_20186_() + 2.2, this.lockedTarget.m_20189_(), 5, 0.18, 0.3, 0.18, 0.0);
        }
        if (--this.windup > 0) {
            return;
        }
        object = this.skill;
        this.skill = "";
        this.boss.finishGongdiCast();
        try {
            switch (object) {
                case "apprehend": {
                    this.pull(serverLevel);
                    return;
                }
                case "crippling_strike": {
                    this.strike(serverLevel);
                    return;
                }
                case "guillotine": {
                    this.execute(serverLevel);
                    return;
                }
                default: {
                    throw new IllegalStateException("Unknown Gongdi skill: " + (String)object);
                }
            }
        }
        finally {
            this.lockedTarget = null;
        }
    }

    private boolean inCone(LivingEntity livingEntity, double d) {
        Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.boss.m_20182_());
        return GongdiCombatRules.inCone(vec3.f_82479_, vec3.f_82481_, this.direction.f_82479_, this.direction.f_82481_, d);
    }

    private void pull(ServerLevel serverLevel) {
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            if (!this.validTarget(livingEntity, 15.75) || !this.inCone(livingEntity, GongdiCombatRules.PULL_HALF_ANGLE)) continue;
            Vec3 vec3 = this.boss.m_20182_().m_82546_(livingEntity.m_20182_());
            double d = Math.hypot(vec3.f_82479_, vec3.f_82481_);
            if (d <= 1.8) continue;
            Vec3 vec32 = new Vec3(vec3.f_82479_, 0.0, vec3.f_82481_).m_82541_().m_82490_(Math.min(1.15, (d - 1.8) * 0.32));
            livingEntity.m_20334_(vec32.f_82479_, 0.18, vec32.f_82481_);
            livingEntity.f_19864_ = true;
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 24, 1));
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11939_, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    private void strike(ServerLevel serverLevel) {
        LivingEntity livingEntity = this.lockedTarget;
        if (livingEntity == null || !this.validTarget(livingEntity, 7.65) || !this.inCone(livingEntity, GongdiCombatRules.STRIKE_HALF_ANGLE)) {
            return;
        }
        float f = livingEntity.m_21223_();
        livingEntity.f_19802_ = 0;
        float f2 = GongdiCombatRules.strikeDamage(this.boss.gongdiMeleeDamage(livingEntity));
        if (!livingEntity.m_6469_(this.boss.m_269291_().m_269333_((LivingEntity)this.boss), f2)) {
            return;
        }
        this.onAbilityHit(serverLevel, livingEntity, Math.max(0.0f, f - livingEntity.m_21223_()));
        if (livingEntity.m_6084_()) {
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 30, 3));
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 16, 0.3, 0.4, 0.3, 0.08);
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_12313_, SoundSource.HOSTILE, 1.3f, 0.65f);
    }

    private void execute(ServerLevel serverLevel) {
        LivingEntity livingEntity = this.lockedTarget;
        if (livingEntity == null || !this.validTarget(livingEntity, 10.125)) {
            return;
        }
        int n = this.stacks(livingEntity, serverLevel.m_46467_());
        DamageSource damageSource = new DamageSource((Holder)serverLevel.m_9598_().m_175515_(Registries.f_268580_).m_246971_(GUILLOTINE), (Entity)this.boss, (Entity)this.boss);
        livingEntity.f_19802_ = 0;
        float f = GongdiCombatRules.executeDamage(this.boss.gongdiMeleeDamage(livingEntity), n);
        if (!livingEntity.m_6469_(damageSource, f)) {
            return;
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123766_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 4, 0.2, 0.5, 0.2, 0.0);
        serverLevel.m_5594_(null, livingEntity.m_20183_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 0.8f, 0.7f);
        if (!livingEntity.m_6084_() && livingEntity instanceof Player) {
            this.guillotineResetWindow.grant(serverLevel.m_46467_());
            this.boss.resetGongdiComboCooldown();
            FTBossMod.LOGGER.info("[FTBoss][Gongdi] guillotine player kill: one recast available for 10 seconds, bleed={}", (Object)n);
        }
    }

    private int stacks(LivingEntity livingEntity, long l) {
        BleedingTarget bleedingTarget = this.bleeding.get(livingEntity.m_20148_());
        return bleedingTarget == null || bleedingTarget.player() != livingEntity ? 0 : bleedingTarget.bleed().stacks(l);
    }

    private record BleedingTarget(LivingEntity player, GongdiCombatRules.Bleed bleed) {
    }
}

