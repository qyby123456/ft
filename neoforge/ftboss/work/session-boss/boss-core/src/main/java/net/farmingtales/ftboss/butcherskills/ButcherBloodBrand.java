package net.farmingtales.ftboss.butcherskills;

import java.util.Map;
import java.util.EnumMap;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ButcherBloodBrand {
    private static final ButcherSkillRules.Skill[] CHOICES={ButcherSkillRules.Skill.BLOOD_BRAND,ButcherSkillRules.Skill.BLOOD_TIDE,ButcherSkillRules.Skill.CLEAVER_SWEEP};
    private static final ResourceKey<DamageType> BLOOD=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("ftboss","butcher_blood"));
    private static final ResourceKey<DamageType> MELEE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("ftboss","butcher_melee"));
    private static final Map<MeatMountainEntity,State> STATES=new WeakHashMap<>();

    private ButcherBloodBrand() {
    }

    private static boolean eligible(MeatMountainEntity entity,LivingEntity target) {
        return BossTargets.valid(target) && target!=entity && target.level()==entity.level() && !entity.isAlliedTo(target) && !BossAggro.isBossSide(target) && entity.distanceToSqr(target)<=2304.0;
    }

    private static void lockFacing(MeatMountainEntity entity,State state) {
        entity.getNavigation().stop();
        entity.setYRot(state.facing);
        entity.yBodyRot=state.facing;
        entity.yHeadRot=state.facing;
        Vec3 movement=entity.getDeltaMovement();
        entity.setDeltaMovement(new Vec3(0.0,movement.y,0.0));
    }

    public static boolean tick(MeatMountainEntity entity,BossActionState action) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        if (!entity.isAlive() || entity.isNoAi() || entity.getButcherPhase()!=2) {
            STATES.remove(entity);
            if (action.name().startsWith("butcher_")) action.start("",0);
            return false;
        }
        long now=level.getGameTime();
        State state=STATES.computeIfAbsent(entity,ignored->new State(now));
        if (state.active) {
            if (!(level.getEntity(state.target) instanceof LivingEntity target) || !eligible(entity,target)) {
                state.active=false;
                state.nextCast=now+ButcherSkillRules.GLOBAL_COOLDOWN;
                action.start("",0);
                return true;
            }
            lockFacing(entity,state);
            int elapsed=(int)(now-state.start);
            if (elapsed<state.skill.windup) ButcherSkillFx.telegraph(level,state.skill,state.center,state.forward,elapsed);
            if (!state.released && elapsed>=state.skill.windup) {
                state.released=true;
                release(level,entity,state);
            }
            if (elapsed>=state.skill.duration()) {
                state.active=false;
                action.start("",0);
            }
            return true;
        }
        if (now<state.nextCast || action.active()) return false;
        LivingEntity target=BossAggro.highest(entity);
        if (!eligible(entity,target)) target=entity.getTarget();
        if (!eligible(entity,target)) target=BossTargets.nearest(level,entity,24.0);
        if (!eligible(entity,target) || !entity.hasLineOfSight(target)) return false;
        double targetDistance=Math.sqrt(entity.distanceToSqr(target));
        ButcherSkillRules.Skill selected=null;
        for (int offset=0;offset<CHOICES.length;offset++) {
            int index=(state.rotation+offset)%CHOICES.length;
            ButcherSkillRules.Skill choice=CHOICES[index];
            if (now>=state.ready.getOrDefault(choice,0L) && targetDistance<=choice.castRange && Math.abs(target.getY()-entity.getY())<=2.5) {
                selected=choice;
                state.rotation=(index+1)%CHOICES.length;
                break;
            }
        }
        if (selected==null) return false;
        state.skill=selected;
        Vec3 origin=entity.position();
        double offsetX=target.getX()-origin.x;
        double offsetZ=target.getZ()-origin.z;
        double distance=Math.hypot(offsetX,offsetZ);
        state.forward=distance>0.001?new Vec3(offsetX/distance,0.0,offsetZ/distance):new Vec3(0.0,0.0,1.0);
        state.facing=(float)Math.toDegrees(Math.atan2(-state.forward.x,state.forward.z));
        state.center=state.skill==ButcherSkillRules.Skill.BLOOD_BRAND?target.position():origin;
        state.target=target.getUUID();
        state.start=now;
        state.released=false;
        state.active=true;
        state.ready.put(state.skill,now+state.skill.cooldown);
        state.nextCast=now+state.skill.duration()+ButcherSkillRules.GLOBAL_COOLDOWN;
        entity.setTarget(target);
        entity.getLookControl().setLookAt(target,30.0F,30.0F);
        lockFacing(entity,state);
        action.start(state.skill.action,state.skill.duration());
        ButcherSkillFx.telegraph(level,state.skill,state.center,state.forward,0);
        return true;
    }

    private static void release(ServerLevel level,MeatMountainEntity entity,State state) {
        ButcherSkillFx.release(level,state.skill,state.center,state.forward);
        entity.swing(InteractionHand.MAIN_HAND);
        level.playSound(null,entity.blockPosition(),state.skill.melee()?SoundEvents.PLAYER_ATTACK_STRONG:SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.HOSTILE,1.0F,state.skill.melee()?0.65F:0.85F);
        DamageSource source=new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(state.skill.melee()?MELEE:BLOOD),entity);
        float amount=ButcherSkillRules.damage(state.skill,entity.getAttributeValue(Attributes.ATTACK_DAMAGE),false)*ButcherPhaseRules.damageMultiplier(entity.getButcherPhase());
        double radius=state.skill.radius;
        AABB area=new AABB(state.center.x-radius,state.center.y-2.5,state.center.z-radius,state.center.x+radius,state.center.y+2.5,state.center.z+radius);
        for (LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,area)) {
            if (!eligible(entity,target) || !entity.hasLineOfSight(target)) continue;
            double offsetX=target.getX()-state.center.x;
            double offsetZ=target.getZ()-state.center.z;
            if (ButcherSkillRules.inCircle(offsetX*offsetX+offsetZ*offsetZ,target.getY()-state.center.y,radius)
                && (state.skill!=ButcherSkillRules.Skill.CLEAVER_SWEEP && state.skill!=ButcherSkillRules.Skill.BLOOD_TIDE || ButcherSkillRules.inCone(offsetX,offsetZ,state.forward.x,state.forward.z,radius,state.skill==ButcherSkillRules.Skill.CLEAVER_SWEEP?0.5:Math.sqrt(0.5)))
                && target.hurt(source,amount) && state.skill.melee()) target.knockback(0.45,entity.getX()-target.getX(),entity.getZ()-target.getZ());
        }
    }

    private static final class State {
        long nextCast;
        final EnumMap<ButcherSkillRules.Skill,Long> ready=new EnumMap<>(ButcherSkillRules.Skill.class);
        int rotation;
        ButcherSkillRules.Skill skill;
        long start;
        UUID target;
        Vec3 center;
        Vec3 forward;
        boolean active;
        boolean released;
        float facing;

        State(long now) {
            nextCast=now+ButcherSkillRules.FIRST_CAST_DELAY;
        }
    }
}
