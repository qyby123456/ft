/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.EnumSet;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossApproach;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherHitEffects;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
import net.farmingtales.ftboss.butcherskills.ButcherSkills;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public final class ButcherCombat
extends Goal {
    private static final ResourceKey<DamageType> MELEE_DAMAGE = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "butcher_melee"));
    private final MeatMountainEntity owner;
    private final BossActionState action;
    private UUID swingTarget;
    private long nextAttack;
    private final BossApproach approach = new BossApproach();
    private int successfulHits;
    private int swings;
    private boolean resolved;

    public ButcherCombat(MeatMountainEntity owner, BossActionState action) {
        this.owner = owner;
        this.action = action;
        this.m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean m_8036_() {
        return this.owner.m_6084_() && !this.owner.m_21525_() && this.eligible(this.owner.m_5448_());
    }

    public boolean m_8045_() {
        return this.m_8036_();
    }

    private boolean eligible(LivingEntity target) {
        return BossTargets.valid(target) && target.m_9236_() == this.owner.m_9236_() && !this.owner.m_7307_((Entity)target) && !BossAggro.isBossSide((Entity)target) && this.owner.m_20280_((Entity)target) < 2304.0;
    }

    private boolean canHit(LivingEntity target) {
        return this.eligible(target) && this.owner.m_142582_((Entity)target) && ButcherCombatRules.withinReach(this.owner.m_20280_((Entity)target), Math.abs(this.owner.m_20186_() - target.m_20186_()), this.owner.m_20205_(), target.m_20205_());
    }

    private void cancelSwing() {
        if (this.action.active()) {
            this.action.start("", 0);
        }
        this.swingTarget = null;
        this.resolved = true;
    }

    public void update() {
        if (ButcherSkills.tick(this.owner, this.action)) {
            return;
        }
        Level level = this.owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (!this.owner.m_6084_() || this.owner.m_21525_()) {
            this.cancelSwing();
            this.approach.reset((Mob)this.owner);
            return;
        }
        long l = serverLevel.m_46467_();
        LivingEntity livingEntity = BossAggro.highest((LivingEntity)this.owner);
        if (livingEntity == null && this.eligible(this.owner.m_5448_())) {
            livingEntity = this.owner.m_5448_();
        }
        if (livingEntity == null) {
            double d = 2304.0;
            for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
                double d2 = this.owner.m_20280_((Entity)serverPlayer);
                if (!this.eligible((LivingEntity)serverPlayer) || !(d2 < d)) continue;
                livingEntity = serverPlayer;
                d = d2;
            }
            this.owner.m_6710_(livingEntity);
        }
        if (livingEntity == null) {
            this.cancelSwing();
            this.approach.reset((Mob)this.owner);
            return;
        }
        int n = this.owner.getButcherPhase();
        boolean bl = n == 2;
        this.owner.m_21563_().m_24960_((Entity)livingEntity, 30.0f, 30.0f);
        double d = Math.sqrt(this.owner.m_20205_() * this.owner.m_20205_() * 4.0f + livingEntity.m_20205_());
        this.approach.tick((Mob)this.owner, livingEntity, ButcherCombatRules.chaseSpeed(bl), Math.max(0.5, d - 0.8), Math.max(0.6, d - 0.3), 0.0, 0.0);
        if (this.action.active()) {
            if (!this.resolved && this.action.elapsed(0.0f) >= 1.0f) {
                LivingEntity livingEntity2;
                Entity entity;
                this.resolved = true;
                if (this.swingTarget != null && (entity = serverLevel.m_8791_(this.swingTarget)) instanceof LivingEntity && this.canHit(livingEntity2 = (LivingEntity)entity)) {
                    this.hit(serverLevel, livingEntity2);
                }
            }
            return;
        }
        if (l < this.nextAttack || !this.canHit(livingEntity)) {
            return;
        }
        this.swingTarget = livingEntity.m_20148_();
        this.resolved = false;
        this.action.start(this.swings++ % 2 == 0 ? "chop" : "backhand", 4);
        this.nextAttack = l + (long)ButcherPhaseRules.interval(n);
        this.owner.m_6674_(InteractionHand.MAIN_HAND);
        serverLevel.m_5594_(null, this.owner.m_20183_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 0.75f, bl ? 1.3f : 1.1f);
    }

    private void hit(ServerLevel serverLevel, LivingEntity livingEntity) {
        int n = this.owner.getButcherPhase();
        float f = ButcherCombatRules.damage((float)this.owner.m_21133_(Attributes.f_22281_), this.successfulHits) * ButcherPhaseRules.damageMultiplier(n);
        float f2 = livingEntity.m_21223_() + livingEntity.m_6103_();
        DamageSource damageSource = new DamageSource((Holder)serverLevel.m_9598_().m_175515_(Registries.f_268580_).m_246971_(MELEE_DAMAGE), (Entity)this.owner);
        if (!livingEntity.m_6469_(damageSource, f)) {
            return;
        }
        float f3 = Math.max(0.0f, f2 - livingEntity.m_21223_() - livingEntity.m_6103_());
        if (f3 <= 0.0f) {
            return;
        }
        if (n == 2) {
            ButcherHitEffects.applyTargetDebuffs((Mob)this.owner, livingEntity, ButcherCombatRules.empowered(this.successfulHits));
        }
        ++this.successfulHits;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123798_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 5, 0.25, 0.3, 0.25, 0.05);
        serverLevel.m_5594_(null, livingEntity.m_20183_(), SoundEvents.f_12316_, SoundSource.HOSTILE, 0.8f, 0.8f);
    }

    public void reset() {
        this.cancelSwing();
        this.successfulHits = 0;
        this.nextAttack = this.owner.m_9236_().m_46467_() + (long)ButcherPhaseRules.interval(this.owner.getButcherPhase());
    }
}

