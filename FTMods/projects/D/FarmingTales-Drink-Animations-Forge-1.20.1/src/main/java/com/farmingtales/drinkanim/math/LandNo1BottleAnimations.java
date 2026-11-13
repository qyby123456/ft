package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import java.util.List;

/**
 * A restrained two-hand presentation for Paper Kite Manor's four-serving bottle.
 * The reference food clips establish contact before lifting, keep the support palm
 * under the weight, and turn the label in short wrist-led arcs instead of letting
 * the prop spin independently.
 */
public final class LandNo1BottleAnimations {
    public static final String ID="papercraft_magic_decoration:bottle_of_land_no1";
    private static final double[] INSPECT_TIMES={0,7,15,23,34,47,60,72,83,94,104};
    private static final Pose[] INSPECT={
        BottleMotion.IDLE,
        BottleMotion.IDLE.offset(-.006,.008,0,-.8,-1.2),
        new Pose(.405,-.595,-1.055,-7,155,-8,.8),
        new Pose(.375,-.535,-1.035,-5,154,-12,.8),
        new Pose(.382,-.520,-1.025,-3,181,-10,.8),
        new Pose(.390,-.515,-1.020,-1,132,-8,.8),
        new Pose(.388,-.500,-1.012,17,169,-7,.8),
        new Pose(.395,-.525,-1.025,5,151,-10,.8),
        new Pose(.420,-.575,-1.045,-4,156,-8,.8),
        BottleMotion.IDLE.offset(-.012,.018,-.004,-1.2,.8),
        BottleMotion.IDLE
    };
    private static final List<AnimationSoundTimeline.Cue> SOUNDS=List.of(
        new AnimationSoundTimeline.Cue(7,"inspect_lift",.34f,1.00f),
        new AnimationSoundTimeline.Cue(15,"inspect_support",.30f,1.02f),
        new AnimationSoundTimeline.Cue(34,"inspect_turn",.25f,1.04f),
        new AnimationSoundTimeline.Cue(38,"inspect_liquid",.16f,1.03f),
        new AnimationSoundTimeline.Cue(47,"inspect_settle",.10f,.98f),
        new AnimationSoundTimeline.Cue(60,"inspect_turn",.22f,1.02f),
        new AnimationSoundTimeline.Cue(64,"inspect_liquid",.14f,1.05f),
        new AnimationSoundTimeline.Cue(73,"inspect_settle",.09f,1.01f),
        new AnimationSoundTimeline.Cue(91,"inspect_return",.28f,.98f));

    public static boolean owns(DrinkProfile profile) {return profile!=null&&profile.id().equals(ID);}
    /** The native item is a placeable four-serving bottle, not a FoodProperties item. */
    public static boolean presentationOnlyDrink(DrinkProfile profile) {return owns(profile);}

    public static Pose inspect(double t) {
        Pose pose=track(t,INSPECT_TIMES,INSPECT);
        // Tiny compression at each deliberate stop makes the bottle feel weighted.
        double settle=.55*MotionDetail.settle(t,34.8,8)+.45*MotionDetail.settle(t,47.8,8)
            +.50*MotionDetail.settle(t,60.8,8)+.35*MotionDetail.settle(t,73.0,8);
        return pose.offset(0,-.003*settle,.002*settle,.25*settle,0);
    }
    public static double assistance(Action action,double t,int duration) {
        if(action==Action.INSPECT)return window(t,9,17,86,99);
        if(action==Action.DRINK)return Math.max(window(t,6,14,34,42),bodySupport(t,duration));
        return 0;
    }
    /** Transition from the removed cap to a palm under the heavy bottle. */
    public static double bodySupport(double t,int duration) {
        double end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);
        return window(t,42,49,end+4,end+16);
    }
    public static ArmMotion.Pose arm(Action action,double t,int duration,boolean helper) {
        if(action==Action.INSPECT) {
            double a=assistance(action,t,duration);
            double turn=window(t,25,34,72,83);
            return helper?new ArmMotion.Pose((15+5*turn)*a,-5*a,0)
                :new ArmMotion.Pose((22+5*turn)*a,2.5*a,0);
        }
        ArmMotion.Pose ordinary=ArmMotion.sample(action,t,duration,helper);
        if(action!=Action.DRINK||!helper)return ordinary;
        double support=bodySupport(t,duration);
        return ordinary.mix(new ArmMotion.Pose(38,-5,0),support);
    }
    public static Camera camera(double t) {
        Pose now=inspect(t),behind=inspect(Math.max(0,t-1.6));
        double envelope=window(t,4,14,88,101);
        return new Camera((.30+(now.y()-behind.y())*8)*envelope,
            ((now.yaw()-behind.yaw())*.018)*envelope,
            ((now.roll()-behind.roll())*.012)*envelope);
    }
    public static double gestureImpulse(double t) {
        return .020*BottleMotion.gesturePulse(t,15,7)
            +.032*BottleMotion.gesturePulse(t,34,8)
            +.025*BottleMotion.gesturePulse(t,60,8)
            +.018*BottleMotion.gesturePulse(t,83,7);
    }
    public static List<AnimationSoundTimeline.Cue> inspectionSounds() {return SOUNDS;}

    private static double window(double t,double in0,double in1,double out0,double out1) {
        return BottleMotion.smooth((t-in0)/(in1-in0))*(1-BottleMotion.smooth((t-out0)/(out1-out0)));
    }
    private static Pose track(double t,double[] times,Pose[] poses) {
        if(t<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(t<times[i])
            return poses[i-1].mix(poses[i],BottleMotion.smooth((t-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
    private LandNo1BottleAnimations() {}
}
