package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Continuous view-space chest motion, shared by every foreground attachment. */
public final class BreathingMotion {
    public static final int PERIOD_TICKS=80;

    public static BottleRig.Frame apply(BottleRig.Frame rig,double playerTicks,int side) {
        double phase=2*Math.PI*(playerTicks%PERIOD_TICKS)/PERIOD_TICKS;
        // The second harmonic gives the inhale a soft crest without a stopped hold.
        double rise=Math.sin(phase)+.16*Math.sin(2*phase);
        Vector3f shift=new Vector3f((float)(side*.0035*Math.sin(phase-.7)),
            (float)(.008*rise),(float)(.002*Math.cos(phase+.3)));
        Quaternionf turn=new Quaternionf().rotateXYZ(
            radians(.18*Math.sin(phase+.35)),radians(side*.10*Math.sin(phase-.7)),
            radians(side*.16*Math.sin(phase-.4)));
        // A common camera-relative transform preserves grip, lid seating and the
        // relative ballistic paths of released objects. It never drives a wrist
        // toward a thrown container, and does not rotate the world camera.
        return new BottleRig.Frame(move(rig.bottle(),shift,turn),move(rig.hand(),shift,turn),
            move(rig.helper(),shift,turn),move(rig.cork(),shift,turn),rig.assistance());
    }

    private static BottleRig.Transform move(BottleRig.Transform t,Vector3f shift,Quaternionf turn) {
        Vector3f position=turn.transform(new Vector3f(t.position()).add(0,.45f,-.10f))
            .add(0,-.45f,.10f).add(shift);
        return new BottleRig.Transform(position,new Quaternionf(turn).mul(t.rotation()));
    }
    private static float radians(double degrees) {return (float)Math.toRadians(degrees);}
    private BreathingMotion() {}
}
