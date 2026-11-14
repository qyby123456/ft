package net.farmingtales.ftboss.client.manorrun;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import net.farmingtales.ftboss.client.render.GongdiHumanoidTimeline;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import org.joml.Quaterniond;

public final class GongdiRunFrame {
    private static final class Library {
        private static final GongdiBasicAttackSampler.Frame IDLE = GongdiBasicAttackSampler.sample("held_idle",0);
    }
    public static GongdiBasicAttackSampler.Frame apply(GongdiBasicAttackSampler.Frame original, EasterBossEntity entity, float age) {
        float partial = (float) RunCycle.clamp(age - entity.tickCount, 0, 1);
        RunCycle.State state = ManorRunAnimation.prepare(entity, age);
        if (state == null || ManorRunAnimation.blocked(entity, partial)) return original;
        double weight = state.previousWeight + (state.weight - state.previousWeight) * partial;
        double phase = state.previousPhase + (state.phase - state.previousPhase) * partial;
        if (weight < 0.0001) return original;
        return transform(original, state.speed, phase, weight);
    }

    public static GongdiBasicAttackSampler.Frame locomotionBase(GongdiBasicAttackSampler.Frame original,double speed,double phase) {
        double walkPhase = phase / (Math.PI * 2);
        walkPhase -= Math.floor(walkPhase);
        var walk = GongdiBasicAttackSampler.sample("weighted_walk",walkPhase);
        double walking = RunCycle.clamp(speed / RunCycle.RUN_EXIT,0,1);
        var base = GongdiHumanoidTimeline.blend(Library.IDLE,walk,walking);
        var bones = new LinkedHashMap<>(base.bones());
        var head = bones.get("head");
        bones.put("head",new GongdiBasicAttackSampler.Bone(head.position(),original.bones().get("head").quaternion()));
        var grip = new GongdiBasicAttackSampler.Bone(new double[]{-0.35,0.73,0.17},bones.get("weapon").quaternion());
        bones.put("weapon",grip);
        bones.put("right_wrist",grip);
        bones.put("left_wrist",new GongdiBasicAttackSampler.Bone(new double[]{0.34,0.71,0.03 - 0.12 * walking * Math.sin(phase - 0.20)},bones.get("left_wrist").quaternion()));
        return GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(base.time(),Map.copyOf(bones)));
    }

    public static GongdiBasicAttackSampler.Frame transform(GongdiBasicAttackSampler.Frame original, double speed, double phase, double weight) {
        var idle = Library.IDLE;
        var bones = new LinkedHashMap<>(idle.bones());
        bones.put("head",new GongdiBasicAttackSampler.Bone(idle.bones().get("head").position(),original.bones().get("head").quaternion()));
        double intensity = RunCycle.clamp((speed - RunCycle.RUN_EXIT) / (RunCycle.SPRINT - RunCycle.RUN_EXIT), 0, 1);
        double lag = Math.sin(phase - 0.20);
        var baseRotation = idle.bones().get("weapon").quaternion();
        var rotation = new Quaterniond(baseRotation[0],baseRotation[1],baseRotation[2],baseRotation[3]).rotateX(0.025 * lag).normalize();
        double[] quaternion = {rotation.x,rotation.y,rotation.z,rotation.w};
        var grip = new GongdiBasicAttackSampler.Bone(new double[]{-0.35,0.73 + 0.012 * lag,0.17},quaternion);
        bones.put("weapon",grip);
        bones.put("right_wrist",grip);
        bones.put("left_wrist",new GongdiBasicAttackSampler.Bone(new double[]{0.34,0.71,0.03 - 0.12 * lag},idle.bones().get("left_wrist").quaternion()));
        for (String side : new String[]{"right","left"}) {
            double swing = Math.sin(phase + (side.equals("right") ? 0 : Math.PI));
            double lift = Math.max(0,swing) * (0.11 + 0.04 * intensity);
            double travel = swing * (0.12 + 0.035 * intensity);
            bones.put(side + "_ankle",new GongdiBasicAttackSampler.Bone(new double[]{side.equals("right") ? -0.145 : 0.145,0.125 + lift,travel},new double[]{0,0,0,1}));
        }
        var target = GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(original.time(),Map.copyOf(bones)));
        return GongdiHumanoidTimeline.blend(original,target,weight);
    }

    private GongdiRunFrame() { }
}
