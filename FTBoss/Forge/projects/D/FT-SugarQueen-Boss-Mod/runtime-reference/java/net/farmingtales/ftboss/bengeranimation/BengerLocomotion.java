/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 */
package net.farmingtales.ftboss.bengeranimation;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class BengerLocomotion {
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.benger.run");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.benger.walk");
    private static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("animation.benger.sprint");
    public static final double RUN_REFERENCE = 0.11097301136363635;
    public static final double SPRINT_REFERENCE = 0.2790178571428571;
    private static final Map<LivingEntity, Snapshot> STATES = new WeakHashMap<LivingEntity, Snapshot>();

    public static Gait choose(double d, Gait gait) {
        if (!Double.isFinite(d) || d < 0.0) {
            return Gait.WALK;
        }
        if (d >= 0.29 || gait == Gait.SPRINT && d > 0.235) {
            return Gait.SPRINT;
        }
        if (d >= 0.145 || gait != Gait.WALK && d > 0.115) {
            return Gait.RUN;
        }
        return Gait.WALK;
    }

    public static double rate(double d, Gait gait) {
        if (gait == Gait.WALK || !Double.isFinite(d)) {
            return 1.0;
        }
        return Math.max(0.75, Math.min(1.9, d / (gait == Gait.SPRINT ? 0.2790178571428571 : 0.11097301136363635)));
    }

    public static synchronized boolean apply(AnimationState<?> animationState) {
        GeoAnimatable geoAnimatable = animationState.getAnimatable();
        if (!(geoAnimatable instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity2 = (LivingEntity)geoAnimatable;
        if (livingEntity2.m_21224_() || !animationState.isMoving()) {
            STATES.remove(livingEntity2);
            return false;
        }
        double d = Math.sqrt(livingEntity2.m_20184_().m_165925_());
        Snapshot snapshot = STATES.computeIfAbsent(livingEntity2, livingEntity -> new Snapshot());
        if (livingEntity2.f_19797_ != snapshot.tick) {
            boolean bl = snapshot.tick == Integer.MIN_VALUE || livingEntity2.f_19797_ - snapshot.tick > 3;
            snapshot.gait = BengerLocomotion.choose(d, bl ? Gait.WALK : snapshot.gait);
            double d2 = BengerLocomotion.rate(d, snapshot.gait);
            snapshot.playback = bl ? d2 : snapshot.playback + (d2 - snapshot.playback) * 0.22;
            snapshot.tick = livingEntity2.f_19797_;
        }
        animationState.getController().setAnimationSpeed(snapshot.gait == Gait.WALK ? 1.0 : snapshot.playback);
        animationState.setAnimation(snapshot.gait == Gait.SPRINT ? SPRINT : (snapshot.gait == Gait.RUN ? RUN : WALK));
        return true;
    }

    public static enum Gait {
        WALK,
        RUN,
        SPRINT;

    }

    private static final class Snapshot {
        Gait gait = Gait.WALK;
        int tick = Integer.MIN_VALUE;
        double playback = 1.0;

        private Snapshot() {
        }
    }
}

