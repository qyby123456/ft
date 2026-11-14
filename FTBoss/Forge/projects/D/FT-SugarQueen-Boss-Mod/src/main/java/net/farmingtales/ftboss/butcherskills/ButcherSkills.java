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
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules.Skill;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ButcherSkills {
    private static final ResourceKey<DamageType> MELEE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("ftboss", "butcher_melee"));
    private static final ResourceKey<DamageType> BLOOD = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("ftboss", "butcher_blood"));
    private static final Map<MeatMountainEntity, State> STATES = new WeakHashMap<>();
    private static final Map<ServerLevel, Pulses> SUMMONS = new WeakHashMap<>();
    private static final Skill[] CHOICES = Skill.values();

    private static final class State {
        final EnumMap<Skill, Long> ready = new EnumMap<>(Skill.class);
        long nextCast;
        long start;
        int rotation;
        Skill skill;
        UUID target;
        Vec3 center;
        Vec3 forward;
        boolean released;
        float facing;
        State(long now) { this.nextCast = now + ButcherSkillRules.FIRST_CAST_DELAY; }
    }

    private record Pulse(Vec3 center, long start, boolean boss, int phase) {}
    private static final class Pulses {
        final List<Pulse> entries = new ArrayList<>();
        long lastTick = Long.MIN_VALUE;
    }

    private ButcherSkills() {}

    public static boolean addWithFx(ServerLevel level, Entity entity) {
        boolean added = level.addFreshEntity(entity);
        if (added && entity instanceof LivingEntity) {
            Pulses pulses = SUMMONS.computeIfAbsent(level, ignored -> new Pulses());
            if (pulses.entries.size() >= ButcherSkillRules.MAX_SUMMON_PULSES) pulses.entries.remove(0);
            int phase = Math.floorMod(entity.getUUID().hashCode(), 4);
            pulses.entries.add(new Pulse(entity.position(), level.getGameTime(), entity instanceof MeatMountainEntity, phase));
            ButcherSkillFx.summon(level, entity.position(), 0, entity instanceof MeatMountainEntity);
        }
        return added;
    }

    private static void tickSummons(ServerLevel level, long now) {
        Pulses pulses = SUMMONS.get(level);
        if (pulses == null || pulses.lastTick == now) return;
        pulses.lastTick = now;
        pulses.entries.removeIf(pulse -> now - pulse.start() > ButcherSkillRules.SUMMON_DURATION);
        for (Pulse pulse : pulses.entries) {
            int elapsed = (int) (now - pulse.start());
            if (elapsed > 0 && elapsed % 4 == pulse.phase()) ButcherSkillFx.summon(level, pulse.center(), elapsed, pulse.boss());
        }
        if (pulses.entries.isEmpty()) SUMMONS.remove(level);
    }

    public static boolean tick(MeatMountainEntity owner, BossActionState action) {
        if (!(owner.level() instanceof ServerLevel level)) return false;
        long now = level.getGameTime();
        tickSummons(level, now);
        if (!owner.isAlive() || owner.isNoAi() || owner.getButcherPhase() != ButcherPhaseRules.ASSAULT) {
            STATES.remove(owner);
            if (action.name().startsWith("butcher_")) action.start("", 0);
            return false;
        }
        State state = STATES.computeIfAbsent(owner, ignored -> new State(now));
        if (state.skill != null) {
            Entity rawTarget = level.getEntity(state.target);
            if (!(rawTarget instanceof LivingEntity target) || !eligible(owner, target)) {
                state.skill = null;
                state.nextCast = now + ButcherSkillRules.GLOBAL_COOLDOWN;
                action.start("", 0);
                return true;
            }
            owner.getNavigation().stop();
            owner.setYRot(state.facing);
            owner.yBodyRot = state.facing;
            owner.yHeadRot = state.facing;
            Vec3 velocity = owner.getDeltaMovement();
            owner.setDeltaMovement(new Vec3(0, velocity.y, 0));
            int elapsed = (int) (now - state.start);
            if (elapsed < state.skill.windup) ButcherSkillFx.telegraph(level, state.skill, state.center, state.forward, elapsed);
            if (!state.released && elapsed >= state.skill.windup) {
                state.released = true;
                release(level, owner, state);
            }
            if (elapsed >= state.skill.duration()) {
                state.skill = null;
                action.start("", 0);
            }
            return true;
        }
        if (now < state.nextCast || action.active()) return false;
        LivingEntity target = BossAggro.highest(owner);
        if (!eligible(owner, target)) target = owner.getTarget();
        if (!eligible(owner, target)) target = BossTargets.nearest(level, owner, 24);
        if (!eligible(owner, target) || !owner.hasLineOfSight(target)) return false;
        double distance = Math.sqrt(owner.distanceToSqr(target));
        Skill selected = null;
        Skill[] choices = CHOICES;
        for (int offset = 0; offset < choices.length; offset++) {
            int index = (state.rotation + offset) % choices.length;
            Skill choice = choices[index];
            if (now >= state.ready.getOrDefault(choice, 0L) && distance <= choice.castRange && Math.abs(target.getY() - owner.getY()) <= 2.5) {
                selected = choice;
                state.rotation = (index + 1) % choices.length;
                break;
            }
        }
        if (selected == null) return false;
        Vec3 origin = owner.position();
        double offsetX = target.getX() - origin.x;
        double offsetZ = target.getZ() - origin.z;
        double horizontal = Math.hypot(offsetX, offsetZ);
        state.forward = horizontal > 0.001 ? new Vec3(offsetX / horizontal, 0, offsetZ / horizontal) : new Vec3(0, 0, 1);
        state.facing = (float) Math.toDegrees(Math.atan2(-state.forward.x, state.forward.z));
        state.center = selected == Skill.BLOOD_BRAND ? target.position() : selected == Skill.BONE_CRUSH
                ? origin.add(state.forward.x * 2, 0, state.forward.z * 2) : origin;
        state.skill = selected;
        state.target = target.getUUID();
        state.start = now;
        state.released = false;
        state.ready.put(selected, now + selected.cooldown);
        state.nextCast = now + selected.duration() + ButcherSkillRules.GLOBAL_COOLDOWN;
        owner.setTarget(target);
        owner.getLookControl().setLookAt(target, 30, 30);
        owner.getNavigation().stop();
        owner.setYRot(state.facing);
        owner.yBodyRot = state.facing;
        owner.yHeadRot = state.facing;
        action.start(selected.action, selected.duration());
        ButcherSkillFx.telegraph(level, selected, state.center, state.forward, 0);
        return true;
    }

    private static boolean eligible(MeatMountainEntity owner, LivingEntity target) {
        return BossTargets.valid(target) && target != owner && target.level() == owner.level()
                && !owner.isAlliedTo(target) && !BossAggro.isBossSide(target) && owner.distanceToSqr(target) <= 2304;
    }

    private static void release(ServerLevel level, MeatMountainEntity owner, State state) {
        Skill skill = state.skill;
        ButcherSkillFx.release(level, skill, state.center, state.forward);
        owner.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, owner.blockPosition(), skill.melee() ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, skill.melee() ? 0.65F : 0.85F);
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(skill.melee() ? MELEE : BLOOD), owner);
        float damage = ButcherSkillRules.damage(skill, owner.getAttributeValue(Attributes.ATTACK_DAMAGE), false)
                * ButcherPhaseRules.damageMultiplier(owner.getButcherPhase());
        double radius = skill.radius;
        AABB area = new AABB(state.center.x - radius, state.center.y - 2.5, state.center.z - radius,
                state.center.x + radius, state.center.y + 2.5, state.center.z + radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!eligible(owner, victim) || !owner.hasLineOfSight(victim)) continue;
            double offsetX = victim.getX() - state.center.x;
            double offsetZ = victim.getZ() - state.center.z;
            if (!ButcherSkillRules.inCircle(offsetX * offsetX + offsetZ * offsetZ, victim.getY() - state.center.y, radius)) continue;
            if ((skill == Skill.CLEAVER_SWEEP || skill == Skill.BLOOD_TIDE) && !ButcherSkillRules.inCone(offsetX, offsetZ,
                    state.forward.x, state.forward.z, radius, skill == Skill.CLEAVER_SWEEP ? 0.5 : Math.sqrt(0.5))) continue;
            float previousHealth = victim.getHealth() + victim.getAbsorptionAmount();
            if (!victim.hurt(source, damage)) continue;
            float dealt = Math.max(0, previousHealth - victim.getHealth() - victim.getAbsorptionAmount());
            float healing = net.farmingtales.ftboss.boss.ai.ButcherBalanceRules.lifesteal(owner.getButcherPhase(), dealt,
                    owner.getMaxHealth() - owner.getHealth());
            if (healing > 0) owner.heal(healing);
            if (skill.melee()) victim.knockback(0.45, owner.getX() - victim.getX(), owner.getZ() - victim.getZ());
        }
    }
}
