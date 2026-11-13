package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;

/** Preview of the normal player swing; no pocket/replacement phase on block placement. */
public final class PlacementMotion {
    public static final int TICKS=6, RECOVER_TICK=6;
    public static Pose swing(Pose rest,double progress) {
        if(progress<=0||progress>=1)return rest;
        double attack=Math.sin((1-(1-progress)*(1-progress))*Math.PI);
        return rest.offset(-attack*.18,attack*.06,-attack*.13,0,attack*9);
    }
    public static Pose sample(DrinkProfile p,double t,boolean remaining) {
        Pose rest=DrinkMotion.sample(p,Action.IDLE,0,16,true);
        return swing(rest,t/TICKS);
    }
    public static boolean bottleVisible(double t,boolean remaining) {return remaining;}
    public static ArmMotion.Pose arm(DrinkProfile p,double t,boolean remaining) {
        return ArmMotion.REST;
    }
    public static Camera camera(double t) {
        return new Camera(0,0,0);
    }
    private PlacementMotion() {}
}
