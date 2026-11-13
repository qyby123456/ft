package com.farmingtales.drinkanim.math;

import java.util.ArrayList;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;

/**
 * Senluo Cookery's handheld sticky rice cake.
 *
 * Nothing is drawn from a container: the cake itself is the held object, so the body model is
 * swapped for a smaller stage after every bite. The hand does the eating: the whole cake travels
 * up to the lips for every mouthful and settles back into a waiting hold in between, so the three
 * bites read as three separate feeds instead of a cake shrinking in place. The far hand comes up
 * to share the cake while it is eaten and to steady it while the player looks it over.
 */
public final class StickyRiceCakeAnimations {
    public static final String ID="kaleidoscope_cookery:sticky_rice_cake";
    public static final int INSPECT_TICKS=BottleMotion.INSPECT_TICKS;
    private static final String FOLDER="animation/snacks/sticky_rice_cake/";
    private static final String[] STAGES={"whole","bite1","bite2","bite3"};
    /** Tail layout shared with every other container: 12 lower, 12 to the pocket, 17 hidden, then the redraw. */
    private static final int DRAW_START_TAIL=41;
    private static final Pose REST=BottleMotion.IDLE;
    private static final Pose DISPLAY=new Pose(.20,-.44,-1.03,-22,188,-6,.8);
    /** Waiting in the hand between mouthfuls, clearly below the lips. */
    private static final Pose READY=new Pose(.22,-.46,-1.04,-8,168,-5,.8);
    /** At the lips: the far hand has carried the whole cake up and tips it into the mouth.
     *  The lift is real now that the far hand owns the cake: the carrying arm is pinned in the
     *  waiting hold during the mouthfuls, so the shared shoulder framing no longer answers it. */
    private static final Pose LIPS=new Pose(.19,-.01,-.70,20,170,-2,.8);
    /** The mouthful clock of one feed: leave the hold, arrive at the lips, hold, settle back. */
    private static final double FEED_LEAD=6,FEED_ARRIVE=1.2,FEED_HOLD=.4,FEED_SETTLE=3.8;

    public static boolean owns(DrinkProfile profile) {return profile!=null&&ID.equals(profile.id());}
    /** Every stage model, ready for `ModelEvent.RegisterAdditional`. */
    public static String[] models() {
        String[] paths=new String[STAGES.length];
        for(int i=0;i<STAGES.length;i++)paths[i]=FOLDER+STAGES[i];
        return paths;
    }

    /**
     * Mouthfuls sit inside the unchanged presentation clock so hosts and sounds never drift apart.
     * They are spread across the whole window: the first lands a beat after the raise is finished
     * and the last leaves the hand enough time to settle before the arm drops to the pocket.
     */
    public static int[] bites(int duration) {
        int count=STAGES.length-1,first=BottleMotion.RAISE_TICKS+9;
        int last=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration)-7;
        int[] ticks=new int[count];
        for(int i=0;i<count;i++)ticks[i]=first+Math.round(i*(last-first)/(float)(count-1));
        return ticks;
    }

    /** How far the hand has carried the cake towards the lips: 0 waiting in the hand, 1 at the mouth. */
    public static double reach(Action action,double t,int duration) {
        if(action!=Action.DRINK)return 0;
        double result=0;
        for(int bite:bites(duration))
            result=Math.max(result,window(t,bite-FEED_LEAD,bite-FEED_ARRIVE,bite+FEED_HOLD,bite+FEED_SETTLE));
        return result;
    }

    /**
     * 1 while the far hand owns the cake. It takes over once the carrying hand has raised the cake
     * into the waiting hold and gives it back when the last mouthful is finished, so every mouthful
     * moves the food and the hand holding it instead of pulling the whole view down with the arm.
     */
    public static double handover(Action action,double t,int duration) {
        if(action!=Action.DRINK)return 0;
        int[] bites=bites(duration);
        return window(t,bites[0]-16,bites[0]-9,bites[bites.length-1]+2,bites[bites.length-1]+6);
    }
    private static double sipEnd(int duration) {return BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);}
    private static int tailEnd(int duration) {return (int)sipEnd(duration)+DRAW_START_TAIL;}

    /** How much of the cake the mouth has already taken. */
    private static int stage(Action action,double t,int duration,boolean replacement) {
        if(action==Action.INSPECT)return 0;
        if(action!=Action.DRINK)return 0;
        // The replacement is a fresh cake, so the pocket hides the last stage and the redraw resets it.
        if(replacement&&t>tailEnd(duration))return 0;
        int index=0;
        for(int bite:bites(duration))if(t>=bite+1)index++;
        return Math.min(STAGES.length-1,index);
    }
    public static String bodyModel(Action action,double t,int duration,boolean replacement) {
        return FOLDER+STAGES[stage(action,t,duration,replacement)];
    }

    public static Pose sample(Action action,double t,int duration,boolean replacement) {
        if(action==Action.IDLE)return REST;
        if(action==Action.DRAW||action==Action.STOW||action==Action.PLACE)
            return BottleMotion.sample(action,t,duration,replacement);
        if(action==Action.INSPECT)return inspect(t);
        return eat(t,duration,replacement);
    }

    /** Held up, turned over twice between thumb and fingers, then returned to the same grip. */
    private static Pose inspect(double t) {
        Pose edge=new Pose(.22,-.44,-1.02,-10,236,-13,.8);
        Pose underside=new Pose(.21,-.45,-1.05,16,306,-8,.8);
        Pose pose=track(t,new double[]{0,8,20,32,44,58,70,82,94,104},
            new Pose[]{REST,REST.offset(.01,-.01,0,-1,-1),DISPLAY,DISPLAY,edge,underside,
                new Pose(.23,-.47,-1.04,-6,340,-9,.8),new Pose(.26,-.52,-1.05,-2,196,-5,.8),
                new Pose(.40,-.62,-1.07,-4,166,-2,.8),REST});
        double effort=window(t,18,30,76,90);
        return pose.offset(.003*Math.sin(t*.31)*effort,.004*Math.sin(t*.43+1.1)*effort,0,
            .9*Math.sin(t*.27)*effort,0);
    }

    /** Raised into the waiting hold, fed to the lips three times, then the empty hand drops to the pocket. */
    private static Pose eat(double t,int duration,boolean replacement) {
        double end=sipEnd(duration);
        if(t<=BottleMotion.RAISE_TICKS) {
            return track(t,new double[]{0,8,18,30,40,BottleMotion.RAISE_TICKS},
                new Pose[]{REST,REST.offset(-.015,.012,0,-2,-1),
                    new Pose(.33,-.59,-1.08,-4,161,-7,.8),
                    new Pose(.24,-.46,-1.02,-8,166,-6,.8),
                    new Pose(.22,-.43,-1.01,-8,167,-5,.8),READY});
        }
        if(t<end) {
            // The hand carries the whole cake in to the lips for every mouthful and brings it back
            // to the waiting hold in between, so the three bites are three separate feeds instead
            // of a cake that quietly shrinks in place while the player watches it.
            double feed=reach(Action.DRINK,t,duration);
            Pose pose=READY.mix(LIPS,feed);
            double fill=.8;
            int[] bites=bites(duration);
            for(int i=0;i<bites.length;i++)
                fill-=(i==0?.25:i==1?.26:.265)*smooth((t-bites[i]-1)/2.5);
            double fraction=BottleMotion.ease((t-BottleMotion.RAISE_TICKS)/BottleMotion.sipTicks(duration));
            double breath=Math.sin((t-BottleMotion.RAISE_TICKS)*Math.PI/9)*Math.pow(Math.sin(Math.PI*fraction),2);
            // A small settle on the way back down keeps the waiting hold alive between feeds.
            return pose.offset(0,.005*breath-.004*feed,0,.6*breath+2.2*feed,0).wine(Math.max(.025,fill));
        }
        double age=t-end;
        Pose empty=READY.wine(.025),lowered=new Pose(.32,-.64,-1.03,-4,158,9,.025);
        Pose hidden=new Pose(.58,-1.76,-.88,10,145,14,.025);
        if(age<=12)return empty.mix(lowered,smooth(age/12));
        if(age<=24)return lowered.mix(hidden,smooth((age-12)/12));
        if(age<=DRAW_START_TAIL||!replacement)return hidden;
        Pose hiddenFull=hidden.wine(.8),settle=REST.offset(.018,.035,-.01,1.2,-.7);
        double draw=age-DRAW_START_TAIL;
        if(draw<=11)return hiddenFull.mix(settle,smooth(draw/11));
        return settle.mix(REST,smooth((draw-11)/(BottleMotion.REPLACEMENT_DRAW_TICKS-11)));
    }

    public static double assistance(Action action,double t,int duration) {
        if(action==Action.INSPECT)return window(t,8,18,84,100);
        // The far hand is not a helper during the meal, it is the hand that holds the food.
        if(action==Action.DRINK)return handover(action,t,duration);
        return 0;
    }

    public static ArmMotion.Pose arm(Action action,double t,int duration,boolean helper) {
        if(action==Action.IDLE)return ArmMotion.REST;
        if(!helper) {
            if(action==Action.DRINK) {
                // The carrying arm only raises the cake and then waits, opening a little as the far
                // hand takes it over: the feeding motion belongs to the other arm from then on.
                double carry=window(t,8,40,sipEnd(duration),sipEnd(duration)+14);
                return new ArmMotion.Pose(12*carry-4*handover(action,t,duration),
                    -2*window(t,16,36,84,100),0);
            }
            return new ArmMotion.Pose(22*window(t,6,24,82,103),3*window(t,18,34,74,94),0);
        }
        // The far arm keeps one carry pose for the whole meal: it is held in cake space, so every
        // mouthful is the arm following the food rather than a separate reach of its own.
        return action==Action.DRINK
            ?new ArmMotion.Pose(26,-9,7)
            :new ArmMotion.Pose(18*assistance(action,t,duration),-6*assistance(action,t,duration),
                5*assistance(action,t,duration));
    }

    public static BottleRig.Frame rig(DrinkProfile profile,Pose motion,Action action,double t,int duration,int side,boolean slim) {
        float scale=profile.renderScale(),unit=scale/16;
        Quaternionf cup=BottleRig.rotation(motion,side);
        Vector3f pivot=pivotOf(motion,side,scale);

        // The shared grip already places the palm centre one bottle half-depth out, which is
        // exactly the cake's own face, so it must not be scaled by width/4 again: doing so
        // pushes the palm off the cake and drags the whole forearm towards the camera.
        Vector3f contact=BottleRig.idleGripOffset(side,slim);
        BottleRig.rotation(REST,side).conjugate().transform(contact);
        // As in the shared rig, an axial inspection turn happens between the fingers: the grip
        // keeps the idle yaw so the forearm never orbits the cake or swings into the frustum.
        Quaternionf grip=action==Action.INSPECT
            ?BottleRig.rotation(new Pose(motion.x(),motion.y(),motion.z(),motion.pitch(),REST.yaw(),motion.roll(),motion.fill()),side)
            :cup;
        // Once the far hand has taken the cake over, the carrying palm stays behind in the waiting
        // hold while the food travels on without it. That is what lets a mouthful reach the lips at
        // all: the shared shoulder framing answers the carrying arm, so an arm that stays put
        // leaves the food free to rise into the mouth.
        double taken=handover(action,t,duration);
        // The released hand also keeps the waiting hold's own frame, so it does not swing around
        // the food the far hand is carrying away from it.
        Quaternionf handGrip=action==Action.INSPECT?grip
            :new Quaternionf(grip).slerp(BottleRig.rotation(READY,side),(float)taken);
        Vector3f palmPivot=new Vector3f(pivot).lerp(pivotOf(READY,side,scale),(float)taken);
        Quaternionf carry=new Quaternionf(handGrip).mul(new Quaternionf(BottleRig.rotation(REST,side)).conjugate())
            .mul(BottleRig.armRotation(arm(action,t,duration,false),side,false));
        BottleRig.Transform hand=new BottleRig.Transform(handGrip.transform(contact).add(palmPivot),carry);

        double help=assistance(action,t,duration);
        // Inspection steadies the underside one cake thickness below the render pivot. While the
        // cake is eaten the far palm takes the carrying grip over, mirrored to the other cheek, so
        // the far hand is holding the food rather than merely touching it: it stays rigid in cake
        // space and every mouthful is the hand and the cake travelling to the lips together.
        Vector3f support=grip.transform(action==Action.DRINK
            ?mirroredGrip(side,slim)
            :new Vector3f(-side*3.4f*unit,-1.9f*unit,1.2f*unit)).add(pivot);
        Vector3f hidden=new Vector3f(-side*.75f,-.83f,-.90f);
        Vector3f helper=new Vector3f(hidden).lerp(support,(float)help);
        Quaternionf helperRotation=new Quaternionf(grip)
            .mul(new Quaternionf(BottleRig.rotation(REST,-side)).conjugate())
            .mul(BottleRig.armRotation(arm(action,t,duration,true),-side,true));
        return new BottleRig.Frame(new BottleRig.Transform(pivot,cup),hand,
            new BottleRig.Transform(helper,helperRotation),new BottleRig.Transform(pivot,cup),help);
    }

    private static Vector3f pivotOf(Pose pose,int side,float scale) {
        return new Vector3f((float)(side*pose.x()*.75),(float)(pose.y()*.75+.30*scale),(float)(pose.z()*.75));
    }

    /** The carrying grip mirrored across the cake's own centre line: the far palm's hold on it. */
    private static Vector3f mirroredGrip(int side,boolean slim) {
        Vector3f carrying=BottleRig.idleGripOffset(side,slim);
        BottleRig.rotation(REST,side).conjugate().transform(carrying);
        return new Vector3f(-carrying.x,carrying.y,carrying.z);
    }

    /** The cake owns no closure, so nothing is ever seated on or lifted off it. */
    public static boolean propVisible(Action action,double t,int duration,boolean replacement) {return false;}
    public static boolean detached(Action action,double t) {return false;}

    public static Camera camera(Action action,double t,int duration) {
        if(action==Action.INSPECT) {
            double a=window(t,4,20,82,103);
            return new Camera(.40*a+.12*window(t,30,42,62,74),-.20*window(t,30,42,70,82),.06*Math.sin(t*.07)*a);
        }
        if(action==Action.DRINK) {
            double end=sipEnd(duration),a=window(t,10,38,end,end+14),beat=0;
            // The eyes dip to meet every mouthful the hand is carrying up to the lips.
            for(int bite:bites(duration))beat+=.20*window(t,bite-2,bite-.5,bite+.6,bite+3.4);
            return new Camera(.24*a+beat,-.07*a,.02*Math.sin(t*.11)*a);
        }
        return new Camera(0,0,0);
    }

    public static List<AnimationSoundTimeline.Cue> inspectionSounds() {return List.of(
        new AnimationSoundTimeline.Cue(7,"inspect_lift",.34f,1.02f),
        new AnimationSoundTimeline.Cue(20,"inspect_support",.22f,1.05f),
        new AnimationSoundTimeline.Cue(32,"inspect_turn",.18f,1.14f),
        new AnimationSoundTimeline.Cue(58,"inspect_turn",.15f,1.19f),
        new AnimationSoundTimeline.Cue(70,"inspect_settle",.10f,1.12f),
        new AnimationSoundTimeline.Cue(94,"inspect_return",.28f,.99f));}

    public static List<AnimationSoundTimeline.Cue> eatingSounds(int duration) {
        var result=new ArrayList<AnimationSoundTimeline.Cue>();
        int i=0;
        for(int bite:bites(duration)) {
            result.add(new AnimationSoundTimeline.Cue(bite,"sticky_rice_cake_bite",.62f-.04f*i,1.03f+.03f*i++));
            result.add(new AnimationSoundTimeline.Cue(bite+3,"snack_swallow",.28f,1.18f));
        }
        return result;
    }

    /** The real cake is a 32 tick food, so the unchanged 40 tick mouthful clock owns every beat. */
    public static double gestureImpulse(Action action,int tick) {
        if(action==Action.DRINK)for(int bite:bites(16))if(tick==bite)return .022;
        if(action==Action.INSPECT&&(tick==32||tick==58))return .026;
        return 0;
    }

    private static Pose track(double t,double[] times,Pose[] poses) {
        if(t<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(t<times[i])return poses[i-1].mix(poses[i],smooth((t-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
    private static double window(double t,double start,double in,double out,double end) {return smooth((t-start)/(in-start))*(1-smooth((t-out)/(end-out)));}
    private static double smooth(double u) {u=Math.max(0,Math.min(1,u));return u*u*u*(u*(u*6-15)+10);}
    private StickyRiceCakeAnimations() {}
}
