package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.farmingtales.ftboss.client.render.CleaverMotionData.*;

/** 黑切单件武器的血怒时钟；2026-09-16 接入，tick 推进，帧采样无副作用，支持中途反向。 */
public final class CleaverMotionPlayback {
    private final CleaverMotionData data;
    private final Frame fullyOpen;
    private boolean blood;
    private String phase;
    private long ticks;
    private Frame origin;
    private double originAmount;

    public CleaverMotionPlayback(CleaverMotionData data, boolean initialBlood) {
        this.data = data;
        fullyOpen = data.sample("blood_idle", 0);
        blood = initialBlood;
        phase = initialBlood ? "blood_idle" : "idle";
    }

    public void target(boolean wanted) {
        if (wanted == blood) return;
        origin = sample(0, false);
        originAmount = amount(0);
        blood = wanted;
        phase = blood ? "blood_enter" : "blood_exit";
        ticks = 0;
    }

    public void tick() {
        ticks++;
        if ((phase.equals("blood_enter") || phase.equals("blood_exit")) && ticks / 20.0 >= data.duration(phase) - 0.0000001) {
            phase = blood ? "blood_idle" : "idle";
            ticks = 0;
            origin = null;
        }
    }

    public double amount(float partialTick) {
        if (phase.equals("blood_idle")) return 1;
        if (phase.equals("idle")) return 0;
        double progress = smooth((ticks + clamp(partialTick)) / (20.0 * data.duration(phase)));
        return originAmount + ((blood ? 1 : 0) - originAmount) * progress;
    }

    public Frame sample(float partialTick, boolean suppressPulse) {
        double seconds = (ticks + clamp(partialTick)) / 20.0;
        Frame frame = data.sample(phase, suppressPulse && phase.equals("blood_idle") ? 0 : seconds);
        if (origin == null) return frame;
        double fade = 1 - smooth(seconds / data.duration(phase));
        Frame reference = data.sample(phase, 0);
        Map<String, Pose> bones = new LinkedHashMap<>(frame.bones());
        for (String name : BONES) {
            Pose current = frame.bone(name);
            Pose start = origin.bone(name);
            Pose expected = reference.bone(name);
            if (WINGS.contains(name)) {
                double openedAngle = fullyOpen.bone(name).rotation().axisZ();
                double ratio = clamp(Math.abs(current.rotation().axisZ()) / 11);
                double angle = blood ? start.rotation().axisZ() + (openedAngle - start.rotation().axisZ()) * ratio : start.rotation().axisZ() * ratio;
                bones.put(name, new Pose(new Vec(0, 0, angle), ONE));
            } else {
                Vec rotation = current.rotation().offset(start.rotation(), expected.rotation(), fade);
                Vec scale = current.scale().offset(start.scale(), expected.scale(), fade);
                if (name.equals("ember_core")) {
                    rotation = new Vec(0, 0, limit(rotation.axisZ(), -4.5, 4.5));
                    scale = new Vec(limit(scale.axisX(), 0.9, 1.13), limit(scale.axisY(), 0.9, 1.13), 1);
                } else {
                    rotation = new Vec(limit(rotation.axisX(), -1.2, 1.2), limit(rotation.axisY(), -1.2, 1.2), limit(rotation.axisZ(), -1.2, 1.2));
                }
                bones.put(name, new Pose(rotation, scale));
            }
        }
        Map<String, Double> effects = new LinkedHashMap<>();
        for (String name : EFFECTS) effects.put(name, clamp(frame.effect(name) + (origin.effect(name) - reference.effect(name)) * fade));
        return new Frame(bones, effects);
    }

    private static double limit(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
