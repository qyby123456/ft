package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

public final class RunAnimator {
    private static final WeakHashMap<EasterBossEntity, Frame> FRAMES = new WeakHashMap<>();
    private RunAnimator() {}
    private static final class Frame {
        final RunGait.Clock clock = new RunGait.Clock();
        final double[] base = new double[36], pose = new double[36];
        float age;
        double forward, sideways;
        boolean captured;
    }
    private static ModelPart[] parts(EasterBossModel model) {
        return new ModelPart[]{model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
    }
    public static void prepare(EasterBossModel model, EasterBossEntity boss, float limbSwing, float limbAmount, float age) {
        if (EasterBossEntity.isManorVariant(boss.getVariant())) return;
        Frame frame = FRAMES.computeIfAbsent(boss, ignored -> new Frame());
        frame.captured = false;
        if (boss.getVariant() < 0 || boss.getVariant() >= RunGait.count() || !boss.isAlive() || boss.isMini() || boss.isGiant()) {
            frame.clock.update(age, 0, false, 0, boss.getVariant());
            return;
        }
        double deltaX = boss.getX() - boss.xo, deltaZ = boss.getZ() - boss.zo;
        double movement = Math.hypot(deltaX, deltaZ);
        boolean grounded = boss.onGround() && !boss.isInWater() && !boss.isPassenger() && !boss.isFallFlying() && !boss.isNoAi();
        frame.clock.update(age, movement, grounded, limbSwing * .6662, boss.getVariant());
        if (frame.clock.blend <= .001 || !Float.isFinite(limbSwing) || !Float.isFinite(limbAmount)) return;
        double yaw = Math.toRadians(boss.yBodyRot);
        if (movement > .001) {
            frame.forward = (deltaZ * Math.cos(yaw) - deltaX * Math.sin(yaw)) / movement;
            frame.sideways = (deltaX * Math.cos(yaw) + deltaZ * Math.sin(yaw)) / movement;
        }
        if (!Double.isFinite(frame.forward) || !Double.isFinite(frame.sideways)) return;
        ModelPart[] parts = parts(model);
        for (int bone = 0; bone < parts.length; bone++) {
            ModelPart part = parts[bone];
            int offset = bone * 6;
            frame.base[offset] = part.x;
            frame.base[offset + 1] = part.y;
            frame.base[offset + 2] = part.z;
            frame.base[offset + 3] = part.xRot;
            frame.base[offset + 4] = part.yRot;
            frame.base[offset + 5] = part.zRot;
        }
        frame.age = age;
        frame.captured = true;
    }
    private static double allowance(ModelPart part, double[] base, int offset) {
        double rotation = Math.max(Math.abs(part.xRot - base[offset + 3]), Math.max(Math.abs(part.yRot - base[offset + 4]), Math.abs(part.zRot - base[offset + 5])));
        double position = Math.max(Math.abs(part.x - base[offset]), Math.max(Math.abs(part.y - base[offset + 1]), Math.abs(part.z - base[offset + 2])));
        return RunGait.channelAllowance(rotation, position);
    }
    public static void apply(EasterBossModel model, EasterBossEntity boss, float age) {
        if (EasterBossEntity.isManorVariant(boss.getVariant())) return;
        Frame frame = FRAMES.get(boss);
        if (frame == null || !frame.captured || frame.age != age) return;
        frame.captured = false;
        if (!boss.isAlive() || frame.clock.blend <= .001) return;
        boolean left = boss.getMainArm() == HumanoidArm.LEFT;
        boolean held = !boss.getMainHandItem().isEmpty();
        RunGait.sample(boss.getVariant(), frame.clock.phase, frame.clock.speed, left, held, frame.pose);
        RunGait.orient(frame.pose, frame.forward, frame.sideways);
        ModelPart[] parts = parts(model);
        ModelPart[] clothing = {model.hat, model.jacket, model.rightSleeve, model.leftSleeve, model.rightPants, model.leftPants};
        double combat = Math.max(0, Math.min(1, boss.getCombatAnimationWeight(Math.max(0, Math.min(1, age - boss.tickCount)))));
        double upper = (1 - RunGait.smooth(combat / .25)) * allowance(parts[1], frame.base, 6);
        if (boss.isCastingLike()) upper = 0;
        for (int bone = 0; bone < parts.length; bone++) {
            ModelPart part = parts[bone];
            int offset = bone * 6;
            double free = allowance(part, frame.base, offset);
            double positionWeight = frame.clock.blend * (bone < 4 ? upper * RunGait.channelAllowance(0, Math.max(Math.abs(part.y - frame.base[offset + 1]), Math.abs(part.z - frame.base[offset + 2]))) : free);
            double rotationWeight = frame.clock.blend * (bone < 4 ? upper * free : free);
            if (bone > 1 && bone < 4) {
                boolean weaponArm = bone == (left ? 3 : 2);
                boolean steadyGrip = boss.getVariant() == 2 || boss.getVariant() == 6 || boss.getVariant() == 11;
                if (boss.isCastingLike() || combat > .04 || weaponArm && held && steadyGrip || !weaponArm && !boss.getOffhandItem().isEmpty()) rotationWeight = 0;
            }
            if (positionWeight <= .001 && rotationWeight <= .001) continue;
            float originalX = part.x, originalY = part.y, originalZ = part.z;
            float originalPitch = part.xRot, originalYaw = part.yRot, originalRoll = part.zRot;
            part.x += (float)(frame.pose[offset] * positionWeight);
            part.y += (float)(frame.pose[offset + 1] * positionWeight);
            part.z += (float)(frame.pose[offset + 2] * positionWeight);
            part.xRot += (float)((frame.pose[offset + 3] - (bone == 0 ? 0 : frame.base[offset + 3])) * rotationWeight);
            part.yRot += (float)((frame.pose[offset + 4] - (bone == 0 ? 0 : frame.base[offset + 4])) * rotationWeight);
            part.zRot += (float)((frame.pose[offset + 5] - (bone == 0 ? 0 : frame.base[offset + 5])) * rotationWeight);
            if (bone >= 4 && rotationWeight > .001) part.y = Math.min(part.y, (float)(24 - RunGait.sole(part.xRot, part.yRot, part.zRot)));
            clothing[bone].x += part.x - originalX;
            clothing[bone].y += part.y - originalY;
            clothing[bone].z += part.z - originalZ;
            clothing[bone].xRot += part.xRot - originalPitch;
            clothing[bone].yRot += part.yRot - originalYaw;
            clothing[bone].zRot += part.zRot - originalRoll;
        }
    }
}
