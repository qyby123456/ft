/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.butcherskills;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
import net.farmingtales.ftboss.butcherskills.ButcherSkillFx;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ButcherSkills {
    private static final ResourceKey<DamageType> MELEE = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "butcher_melee"));
    private static final ResourceKey<DamageType> BLOOD = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "butcher_blood"));
    private static final Map<MeatMountainEntity, State> STATES = new WeakHashMap<MeatMountainEntity, State>();
    private static final Map<ServerLevel, Pulses> SUMMONS = new WeakHashMap<ServerLevel, Pulses>();
    private static final ButcherSkillRules.Skill[] CHOICES = ButcherSkillRules.Skill.values();

    private ButcherSkills() {
    }

    public static boolean addWithFx(ServerLevel serverLevel2, Entity entity) {
        boolean bl = serverLevel2.m_7967_(entity);
        if (bl && entity instanceof LivingEntity) {
            Pulses pulses = SUMMONS.computeIfAbsent(serverLevel2, serverLevel -> new Pulses());
            if (pulses.entries.size() >= 24) {
                pulses.entries.remove(0);
            }
            int n = Math.floorMod(entity.m_20148_().hashCode(), 4);
            pulses.entries.add(new Pulse(entity.m_20182_(), serverLevel2.m_46467_(), entity instanceof MeatMountainEntity, n));
            ButcherSkillFx.summon(serverLevel2, entity.m_20182_(), 0, entity instanceof MeatMountainEntity);
        }
        return bl;
    }

    private static void tickSummons(ServerLevel serverLevel, long l) {
        Pulses pulses = SUMMONS.get(serverLevel);
        if (pulses == null || pulses.lastTick == l) {
            return;
        }
        pulses.lastTick = l;
        pulses.entries.removeIf(pulse -> l - pulse.start() > 32L);
        for (Pulse pulse2 : pulses.entries) {
            int n = (int)(l - pulse2.start());
            if (n <= 0 || n % 4 != pulse2.phase()) continue;
            ButcherSkillFx.summon(serverLevel, pulse2.center(), n, pulse2.boss());
        }
        if (pulses.entries.isEmpty()) {
            SUMMONS.remove(serverLevel);
        }
    }

    private static boolean eligible(MeatMountainEntity meatMountainEntity, LivingEntity livingEntity) {
        return BossTargets.valid(livingEntity) && livingEntity != meatMountainEntity && livingEntity.m_9236_() == meatMountainEntity.m_9236_() && !meatMountainEntity.m_7307_((Entity)livingEntity) && !BossAggro.isBossSide((Entity)livingEntity) && meatMountainEntity.m_20280_((Entity)livingEntity) <= 2304.0;
    }

    public static boolean tick(MeatMountainEntity meatMountainEntity2, BossActionState bossActionState) {
        double d;
        Level level = meatMountainEntity2.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        long l = serverLevel.m_46467_();
        ButcherSkills.tickSummons(serverLevel, l);
        if (!meatMountainEntity2.m_6084_() || meatMountainEntity2.m_21525_() || meatMountainEntity2.getButcherPhase() != 2) {
            STATES.remove((Object)meatMountainEntity2);
            if (bossActionState.name().startsWith("butcher_")) {
                bossActionState.start("", 0);
            }
            return false;
        }
        State state = STATES.computeIfAbsent(meatMountainEntity2, meatMountainEntity -> new State(l));
        if (state.skill != null) {
            LivingEntity livingEntity;
            Entity entity = serverLevel.m_8791_(state.target);
            if (!(entity instanceof LivingEntity) || !ButcherSkills.eligible(meatMountainEntity2, livingEntity = (LivingEntity)entity)) {
                state.skill = null;
                state.nextCast = l + 80L;
                bossActionState.start("", 0);
                return true;
            }
            meatMountainEntity2.m_21573_().m_26573_();
            meatMountainEntity2.m_146922_(state.facing);
            meatMountainEntity2.f_20883_ = state.facing;
            meatMountainEntity2.f_20885_ = state.facing;
            Vec3 vec3 = meatMountainEntity2.m_20184_();
            meatMountainEntity2.m_20256_(new Vec3(0.0, vec3.f_82480_, 0.0));
            int n = (int)(l - state.start);
            if (n < state.skill.windup) {
                ButcherSkillFx.telegraph(serverLevel, state.skill, state.center, state.forward, n);
            }
            if (!state.released && n >= state.skill.windup) {
                state.released = true;
                ButcherSkills.release(serverLevel, meatMountainEntity2, state);
            }
            if (n >= state.skill.duration()) {
                state.skill = null;
                bossActionState.start("", 0);
            }
            return true;
        }
        if (l < state.nextCast || bossActionState.active()) {
            return false;
        }
        LivingEntity livingEntity = BossAggro.highest((LivingEntity)meatMountainEntity2);
        if (!ButcherSkills.eligible(meatMountainEntity2, livingEntity)) {
            livingEntity = meatMountainEntity2.m_5448_();
        }
        if (!ButcherSkills.eligible(meatMountainEntity2, livingEntity)) {
            livingEntity = BossTargets.nearest(serverLevel, (Entity)meatMountainEntity2, 24.0);
        }
        if (!ButcherSkills.eligible(meatMountainEntity2, livingEntity) || !meatMountainEntity2.m_142582_((Entity)livingEntity)) {
            return false;
        }
        double d2 = Math.sqrt(meatMountainEntity2.m_20280_((Entity)livingEntity));
        ButcherSkillRules.Skill skill = null;
        ButcherSkillRules.Skill[] skillArray = CHOICES;
        for (int i = 0; i < skillArray.length; ++i) {
            int n = (state.rotation + i) % skillArray.length;
            ButcherSkillRules.Skill skill2 = skillArray[n];
            if (l < state.ready.getOrDefault((Object)skill2, 0L) || !(d2 <= skill2.castRange) || !(Math.abs(livingEntity.m_20186_() - meatMountainEntity2.m_20186_()) <= 2.5)) continue;
            skill = skill2;
            state.rotation = (n + 1) % skillArray.length;
            break;
        }
        if (skill == null) {
            return false;
        }
        Vec3 vec3 = meatMountainEntity2.m_20182_();
        double d3 = livingEntity.m_20185_() - vec3.f_82479_;
        double d4 = Math.hypot(d3, d = livingEntity.m_20189_() - vec3.f_82481_);
        state.forward = d4 > 0.001 ? new Vec3(d3 / d4, 0.0, d / d4) : new Vec3(0.0, 0.0, 1.0);
        state.facing = (float)Math.toDegrees(Math.atan2(-state.forward.f_82479_, state.forward.f_82481_));
        state.center = skill == ButcherSkillRules.Skill.BLOOD_BRAND ? livingEntity.m_20182_() : (skill == ButcherSkillRules.Skill.BONE_CRUSH ? vec3.m_82520_(state.forward.f_82479_ * 2.0, 0.0, state.forward.f_82481_ * 2.0) : vec3);
        state.skill = skill;
        state.target = livingEntity.m_20148_();
        state.start = l;
        state.released = false;
        state.ready.put(skill, l + (long)skill.cooldown);
        state.nextCast = l + (long)skill.duration() + 80L;
        meatMountainEntity2.m_6710_(livingEntity);
        meatMountainEntity2.m_21563_().m_24960_((Entity)livingEntity, 30.0f, 30.0f);
        meatMountainEntity2.m_21573_().m_26573_();
        meatMountainEntity2.m_146922_(state.facing);
        meatMountainEntity2.f_20883_ = state.facing;
        meatMountainEntity2.f_20885_ = state.facing;
        bossActionState.start(skill.action, skill.duration());
        ButcherSkillFx.telegraph(serverLevel, skill, state.center, state.forward, 0);
        return true;
    }

    private static void release(ServerLevel serverLevel, MeatMountainEntity meatMountainEntity, State state) {
        ButcherSkillRules.Skill skill = state.skill;
        ButcherSkillFx.release(serverLevel, skill, state.center, state.forward);
        meatMountainEntity.m_6674_(InteractionHand.MAIN_HAND);
        serverLevel.m_5594_(null, meatMountainEntity.m_20183_(), skill.melee() ? SoundEvents.f_12316_ : SoundEvents.f_12317_, SoundSource.HOSTILE, 1.0f, skill.melee() ? 0.65f : 0.85f);
        DamageSource damageSource = new DamageSource((Holder)serverLevel.m_9598_().m_175515_(Registries.f_268580_).m_246971_(skill.melee() ? MELEE : BLOOD), (Entity)meatMountainEntity);
        float f = ButcherSkillRules.damage(skill, meatMountainEntity.m_21133_(Attributes.f_22281_), false) * ButcherPhaseRules.damageMultiplier(meatMountainEntity.getButcherPhase());
        double d = skill.radius;
        AABB aABB = new AABB(state.center.f_82479_ - d, state.center.f_82480_ - 2.5, state.center.f_82481_ - d, state.center.f_82479_ + d, state.center.f_82480_ + 2.5, state.center.f_82481_ + d);
        for (LivingEntity livingEntity : serverLevel.m_45976_(LivingEntity.class, aABB)) {
            double d2;
            double d3;
            if (!ButcherSkills.eligible(meatMountainEntity, livingEntity) || !meatMountainEntity.m_142582_((Entity)livingEntity) || !ButcherSkillRules.inCircle((d3 = livingEntity.m_20185_() - state.center.f_82479_) * d3 + (d2 = livingEntity.m_20189_() - state.center.f_82481_) * d2, livingEntity.m_20186_() - state.center.f_82480_, d) || (skill == ButcherSkillRules.Skill.CLEAVER_SWEEP || skill == ButcherSkillRules.Skill.BLOOD_TIDE) && !ButcherSkillRules.inCone(d3, d2, state.forward.f_82479_, state.forward.f_82481_, d, skill == ButcherSkillRules.Skill.CLEAVER_SWEEP ? 0.5 : Math.sqrt(0.5)) || !livingEntity.m_6469_(damageSource, f) || !skill.melee()) continue;
            livingEntity.m_147240_(0.45, meatMountainEntity.m_20185_() - livingEntity.m_20185_(), meatMountainEntity.m_20189_() - livingEntity.m_20189_());
        }
    }

    private static final class Pulses {
        final List<Pulse> entries = new ArrayList<Pulse>();
        long lastTick = Long.MIN_VALUE;

        private Pulses() {
        }
    }

    private record Pulse(Vec3 center, long start, boolean boss, int phase) {
    }

    private static final class State {
        final EnumMap<ButcherSkillRules.Skill, Long> ready = new EnumMap(ButcherSkillRules.Skill.class);
        long nextCast;
        long start;
        int rotation;
        ButcherSkillRules.Skill skill;
        UUID target;
        Vec3 center;
        Vec3 forward;
        boolean released;
        float facing;

        State(long l) {
            this.nextCast = l + 80L;
        }
    }
}

