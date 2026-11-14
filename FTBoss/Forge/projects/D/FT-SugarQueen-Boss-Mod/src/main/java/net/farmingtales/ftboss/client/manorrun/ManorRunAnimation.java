package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.minecraft.client.model.geom.ModelPart;

public final class ManorRunAnimation {
    private static final WeakHashMap<EasterBossEntity, RunCycle.State> STATES = new WeakHashMap<>();

    public static void apply(EasterBossModel model, EasterBossEntity entity, float age) {
        if (!EasterBossEntity.isManorVariant(entity.getVariant())) return;
        float partial = (float) RunCycle.clamp(age - entity.tickCount, 0, 1);
        RunCycle.State state = prepare(entity, age);
        if (state == null) return;
        boolean blocked = blocked(entity, partial);
        if (blocked) return;
        double weight = state.previousWeight + (state.weight - state.previousWeight) * partial;
        if (weight < 0.0001 || entity.getVariant() == EasterBossEntity.GONGDI_VARIANT) return;
        double phase = state.previousPhase + (state.phase - state.previousPhase) * partial;
        int profile = entity.getVariant() == 6 && XuanlanMoonWeaponItem.isWhite(entity.getMainHandItem()) ? 12 : entity.getVariant();
        RunCycle.Pose pose = RunCycle.pose(profile, state.speed, phase);
        double postureLean = pose.lean() * state.travelSign;
        float lean = (float) (postureLean * weight);
        model.body.xRot += lean;
        model.rightArm.y = model.leftArm.y = 2 * (float) Math.cos(lean);
        model.rightArm.z = model.leftArm.z = 2 * (float) Math.sin(lean);
        model.rightLeg.y = model.leftLeg.y = 12 * (float) Math.cos(lean);
        model.rightLeg.z = model.leftLeg.z = 12 * (float) Math.sin(lean);
        rotate(model.rightLeg, pose.leg() + postureLean, 0, 0, weight);
        rotate(model.leftLeg, -pose.leg() + postureLean, 0, 0, weight);
        rotate(model.rightArm, pose.rightArm() + postureLean, 0, pose.roll(), weight);
        rotate(model.leftArm, pose.leftArm() + postureLean, 0, -pose.roll(), weight);
        float deformation = entity.getVariant() == 11 ? 0.25F : 0;
        float lift = Math.max(0, Math.max(footY(model.rightLeg, deformation), footY(model.leftLeg, deformation)) - 24);
        model.head.y -= lift;
        model.body.y -= lift;
        model.rightArm.y -= lift;
        model.leftArm.y -= lift;
        model.rightLeg.y -= lift;
        model.leftLeg.y -= lift;
        model.hat.copyFrom(model.head);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.jacket.copyFrom(model.body);
    }

    static RunCycle.State current(EasterBossEntity entity) { return STATES.get(entity); }

    static RunCycle.State prepare(EasterBossEntity entity, float age) {
        if (!entity.isAlive() || entity.isRemoved()) {
            STATES.remove(entity);
            return null;
        }
        float partial = (float) RunCycle.clamp(age - entity.tickCount, 0, 1);
        RunCycle.State state = STATES.computeIfAbsent(entity, ignored -> new RunCycle.State());
        double dx = entity.getX() - state.lastX;
        double dz = entity.getZ() - state.lastZ;
        double yaw = Math.toRadians(entity.yBodyRot);
        double forward = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
        state.update(entity.tickCount, entity.getX(), entity.getZ(), blocked(entity, partial), forward);
        return state;
    }

    static boolean blocked(EasterBossEntity entity, float partial) {
        var cue = entity.getSkillCue();
        double cueAge = entity.level().getGameTime() - cue.getLong("start") + partial;
        boolean cueActive = cue.contains("start") && cue.getInt("duration") > 0
                && cueAge >= 0 && cueAge < cue.getInt("duration");
        return !entity.isAlive() || entity.isRemoved() || !entity.onGround()
                || entity.isInWater() || entity.isPassenger() || entity.isNoAi() || entity.isSleeping() || entity.hurtTime > 0
                || entity.isCastingLike() || entity.isPerformingSkill() || entity.getCombatAnimationWeight(partial) > 0.001F
                || entity.isDoraReloading() || entity.getDoraSpearTicks() > 0
                || entity.isManorIntroHidden() || entity.isXuanlanSkyHidden() || cueActive;
    }

    private static float footY(ModelPart part, float deformation) {
        double pitchSin = Math.sin(part.xRot), pitchCos = Math.cos(part.xRot);
        double yawSin = Math.sin(part.yRot), yawCos = Math.cos(part.yRot);
        double rollSin = Math.sin(part.zRot), rollCos = Math.cos(part.zRot);
        double axisX = rollSin * yawCos;
        double axisY = rollSin * yawSin * pitchSin + rollCos * pitchCos;
        double axisZ = rollSin * yawSin * pitchCos - rollCos * pitchSin;
        return (float) (part.y + 6 * axisY + (6 + deformation) * Math.abs(axisY)
                + (2 + deformation) * (Math.abs(axisX) + Math.abs(axisZ)));
    }

    private static void rotate(ModelPart part, double pitch, double yaw, double roll, double weight) {
        part.xRot += (float) ((pitch - part.xRot) * weight);
        part.yRot += (float) ((yaw - part.yRot) * weight);
        part.zRot += (float) ((roll - part.zRot) * weight);
    }

    private ManorRunAnimation() { }
}
