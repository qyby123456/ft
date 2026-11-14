/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import net.farmingtales.ftboss.client.render.CleaverMotionData;

public final class CleaverMotionPlayback {
    private final CleaverMotionData data;
    private final CleaverMotionData.Frame fullyOpen;
    private boolean blood;
    private String phase;
    private long ticks;
    private CleaverMotionData.Frame origin;
    private double originAmount;

    public CleaverMotionPlayback(CleaverMotionData data, boolean initialBlood) {
        this.data = data;
        this.fullyOpen = data.sample("blood_idle", 0.0);
        this.blood = initialBlood;
        this.phase = initialBlood ? "blood_idle" : "idle";
    }

    public void target(boolean wanted) {
        if (wanted == this.blood) {
            return;
        }
        this.origin = this.sample(0.0f, false);
        this.originAmount = this.amount(0.0f);
        this.blood = wanted;
        this.phase = this.blood ? "blood_enter" : "blood_exit";
        this.ticks = 0L;
    }

    public void tick() {
        ++this.ticks;
        if ((this.phase.equals("blood_enter") || this.phase.equals("blood_exit")) && (double)this.ticks / 20.0 >= this.data.duration(this.phase) - 1.0E-7) {
            this.phase = this.blood ? "blood_idle" : "idle";
            this.ticks = 0L;
            this.origin = null;
        }
    }

    public double amount(float partialTick) {
        if (this.phase.equals("blood_idle")) {
            return 1.0;
        }
        if (this.phase.equals("idle")) {
            return 0.0;
        }
        double progress = CleaverMotionData.smooth(((double)this.ticks + CleaverMotionData.clamp(partialTick)) / (20.0 * this.data.duration(this.phase)));
        return this.originAmount + ((double)(this.blood ? 1 : 0) - this.originAmount) * progress;
    }

    public CleaverMotionData.Frame sample(float partialTick, boolean suppressPulse) {
        double seconds = ((double)this.ticks + CleaverMotionData.clamp(partialTick)) / 20.0;
        CleaverMotionData.Frame frame = this.data.sample(this.phase, suppressPulse && this.phase.equals("blood_idle") ? 0.0 : seconds);
        if (this.origin == null) {
            return frame;
        }
        double fade = 1.0 - CleaverMotionData.smooth(seconds / this.data.duration(this.phase));
        CleaverMotionData.Frame reference = this.data.sample(this.phase, 0.0);
        LinkedHashMap<String, CleaverMotionData.Pose> bones = new LinkedHashMap<String, CleaverMotionData.Pose>(frame.bones());
        for (String name : CleaverMotionData.BONES) {
            CleaverMotionData.Pose current = frame.bone(name);
            CleaverMotionData.Pose start = this.origin.bone(name);
            CleaverMotionData.Pose expected = reference.bone(name);
            if (CleaverMotionData.WINGS.contains(name)) {
                double openedAngle = this.fullyOpen.bone(name).rotation().axisZ();
                double ratio = CleaverMotionData.clamp(Math.abs(current.rotation().axisZ()) / 11.0);
                double angle = this.blood ? start.rotation().axisZ() + (openedAngle - start.rotation().axisZ()) * ratio : start.rotation().axisZ() * ratio;
                bones.put(name, new CleaverMotionData.Pose(new CleaverMotionData.Vec(0.0, 0.0, angle), CleaverMotionData.ONE));
                continue;
            }
            CleaverMotionData.Vec rotation = current.rotation().offset(start.rotation(), expected.rotation(), fade);
            CleaverMotionData.Vec scale = current.scale().offset(start.scale(), expected.scale(), fade);
            if (name.equals("ember_core")) {
                rotation = new CleaverMotionData.Vec(0.0, 0.0, CleaverMotionPlayback.limit(rotation.axisZ(), -4.5, 4.5));
                scale = new CleaverMotionData.Vec(CleaverMotionPlayback.limit(scale.axisX(), 0.9, 1.13), CleaverMotionPlayback.limit(scale.axisY(), 0.9, 1.13), 1.0);
            } else {
                rotation = new CleaverMotionData.Vec(CleaverMotionPlayback.limit(rotation.axisX(), -1.2, 1.2), CleaverMotionPlayback.limit(rotation.axisY(), -1.2, 1.2), CleaverMotionPlayback.limit(rotation.axisZ(), -1.2, 1.2));
            }
            bones.put(name, new CleaverMotionData.Pose(rotation, scale));
        }
        LinkedHashMap<String, Double> effects = new LinkedHashMap<String, Double>();
        for (String name : CleaverMotionData.EFFECTS) {
            effects.put(name, CleaverMotionData.clamp(frame.effect(name) + (this.origin.effect(name) - reference.effect(name)) * fade));
        }
        return new CleaverMotionData.Frame(bones, effects);
    }

    private static double limit(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

