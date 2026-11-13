package com.farmingtales.drinkanim.math;

import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;

/** Authored bamboo-cup and spoon performance for Senluo Cuisine's steamed egg. */
public final class BambooEggAnimations {
    public static final String ID="kaleidoscope_chinesefood:bamboo_steamed_egg";
    public static final int INSPECT_TICKS=104;
    private static final Pose REST=BottleMotion.IDLE;
    private static final Pose DISPLAY=new Pose(.16,-.41,-1.02,-26,194,-4,.8);
    private static final Pose EAT_HOLD=new Pose(.19,-.34,-.98,-11,174,-3,.8);

    public static boolean owns(DrinkProfile profile) {return profile!=null&&ID.equals(profile.id());}

    /** The cup stays below the face; all three mouth contacts are supplied by the spoon. */
    public static Pose sample(Action action,double t,int duration,boolean replacement) {
        if(action==Action.IDLE)return REST;
        if(action==Action.DRAW||action==Action.STOW||action==Action.PLACE)
            return BottleMotion.sample(action,t,duration,replacement);
        if(action==Action.INSPECT)return track(t,
            new double[]{0,8,18,30,42,54,66,78,91,104},
            new Pose[]{REST,REST.offset(.01,-.01,0,-1,-1),
                new Pose(.33,-.57,-1.09,-9,161,-7,.8),
                new Pose(.22,-.47,-1.06,-19,173,-9,.8),DISPLAY,
                new Pose(.17,-.405,-1.01,-30,213,1,.8),
                new Pose(.22,-.46,-1.04,-16,229,7,.8),
                new Pose(.31,-.55,-1.07,-8,184,-5,.8),
                new Pose(.43,-.65,-1.08,-4,160,-2,.8),REST});
        if(t<=48)return track(t,new double[]{0,10,22,36,48},new Pose[]{REST,
            REST.offset(-.02,.01,0,-2,-2),new Pose(.29,-.57,-1.08,-8,160,-7,.8),
            new Pose(.16,-.43,-1.00,-12,170,-5,.8),EAT_HOLD});
        double sipEnd=48+BottleMotion.sipTicks(duration);
        if(t<sipEnd) {
            double fill=.8-.25*smooth((t-61)/3)-.26*smooth((t-72)/3)-.265*smooth((t-83)/3);
            double settle=.006*Math.sin((t-48)*.32)*window(t,48,54,sipEnd-7,sipEnd);
            return EAT_HOLD.offset(0,settle,0,-.8*settle/.006,0).wine(Math.max(.025,fill));
        }
        double tail=t-sipEnd;
        Pose empty=EAT_HOLD.wine(.025),lowered=new Pose(.31,-.64,-1.03,-5,158,9,.025);
        Pose hidden=new Pose(.59,-1.76,-.88,10,145,14,.025);
        if(tail<=12)return empty.mix(lowered,smooth(tail/12));
        if(tail<=24)return lowered.mix(hidden,smooth((tail-12)/12));
        if(tail<=41||!replacement)return hidden;
        Pose hiddenFull=hidden.wine(.8),settle=REST.offset(.018,.035,-.01,1.2,-.7);
        double draw=tail-41;
        if(draw<=11)return hiddenFull.mix(settle,smooth(draw/11));
        return settle.mix(REST,smooth((draw-11)/(BottleMotion.REPLACEMENT_DRAW_TICKS-11)));
    }

    public static double assistance(Action action,double t,int duration) {
        if(action==Action.INSPECT)return window(t,9,16,88,99);
        if(action==Action.DRINK) {
            double end=48+BottleMotion.sipTicks(duration);
            return window(t,7,16,end+5,end+13);
        }
        return 0;
    }

    public static ArmMotion.Pose arm(Action action,double t,int duration,boolean helper) {
        if(action==Action.IDLE)return ArmMotion.REST;
        if(!helper) {
            double lift=action==Action.INSPECT?26*window(t,5,24,80,103)
                :action==Action.DRINK?24*window(t,7,38,48+BottleMotion.sipTicks(duration),48+BottleMotion.sipTicks(duration)+15):0;
            return new ArmMotion.Pose(lift,sideSway(action,t),0);
        }
        double a=assistance(action,t,duration);
        return new ArmMotion.Pose((action==Action.DRINK?30:20)*a,-5*a,action==Action.DRINK?-10*a:4*a);
    }

    private static double sideSway(Action action,double t) {
        if(action==Action.INSPECT)return 3*window(t,16,32,76,94);
        if(action==Action.DRINK)return -2*window(t,16,36,86,101);
        return 0;
    }

    public static BottleRig.Frame rig(DrinkProfile profile,Pose motion,Action action,double t,int duration,int side,boolean slim) {
        float scale=profile.renderScale(),unit=scale/16;
        Quaternionf cup=BottleRig.rotation(motion,side);
        Vector3f pivot=new Vector3f((float)(side*motion.x()*.75),(float)(motion.y()*.75+.30*scale),(float)(motion.z()*.75));

        // Main palm is rigidly attached to the lower bamboo wall for every cup tilt.
        Vector3f palmLocal=new Vector3f(side*3.42f*unit,(3.15f-4.8f)*unit,-.35f*unit);
        Vector3f palm=cup.transform(new Vector3f(palmLocal)).add(pivot);
        Quaternionf carry=new Quaternionf(cup).mul(new Quaternionf(BottleRig.rotation(REST,side)).conjugate())
            .mul(BottleRig.armRotation(arm(action,t,duration,false),side,false));
        var hand=new BottleRig.Transform(palm,carry);

        BottleRig.Transform spoon=spoon(profile,cup,pivot,action,t,duration,side);
        double help=assistance(action,t,duration);
        Vector3f base=cup.transform(new Vector3f(-side*.45f*unit,-.31f*scale,.45f*unit)).add(pivot);
        Vector3f spoonGrip=new Vector3f(spoon.position()).add(-side*.018f,-.035f,-.015f);
        Vector3f target=spoonGrip;
        if(action==Action.INSPECT) {
            float baseWeight=(float)smooth((t-47)/10);
            target.lerp(base,baseWeight);
        }
        Vector3f hidden=new Vector3f(-side*.75f,-.83f,-.90f);
        Vector3f helper=new Vector3f(hidden).lerp(target,(float)help);
        Quaternionf helperRotation=BottleRig.armRotation(arm(action,t,duration,true),-side,true);
        var helperHand=new BottleRig.Transform(helper,helperRotation);
        return new BottleRig.Frame(new BottleRig.Transform(pivot,cup),hand,helperHand,spoon,help);
    }

    private static BottleRig.Transform spoon(DrinkProfile p,Quaternionf cup,Vector3f pivot,Action action,double t,int duration,int side) {
        // Local values are Blockbench pixels relative to the cup's (8, 4.8, 8) render pivot.
        Vector3f seated=new Vector3f(0,5.7f,1),local=new Vector3f(seated);
        double pitch=0,roll=0;
        if(action==Action.INSPECT) {
            Vector3f dip=new Vector3f(side*.65f,4.15f,.45f),show=new Vector3f(-side*.9f,10.2f,3.1f);
            if(t>=18&&t<27)local=lerp(seated,dip,smooth((t-18)/9));
            else if(t<38&&t>=27)local=lerp(dip,show,smooth((t-27)/11));
            else if(t>=38&&t<49)local=lerp(show,seated,smooth((t-38)/11));
            if(t>=18&&t<49) {pitch=-13*window(t,18,28,39,49);roll=side*9*window(t,24,32,40,48);}
        } else if(action==Action.DRINK) {
            Vector3f dip=new Vector3f(side*.75f,4.0f,.2f),mouth=new Vector3f(side*4.1f,11.8f,5.2f);
            if(t>=45&&t<56)local=lerp(seated,dip,smooth((t-45)/11));
            else if(t>=56&&t<88) {
                double start=t<67?56:t<78?67:78,end=t<67?67:t<78?78:88,u=(t-start)/(end-start);
                if(u<.45)local=lerp(dip,mouth,smooth(u/.45));
                else local=lerp(mouth,dip,smooth((u-.45)/.55));
                double bite=Math.sin(Math.PI*Math.min(1,u));pitch=-18*bite;roll=side*12*bite;
            } else if(t>=88&&t<98)local=lerp(dip,seated,smooth((t-88)/10));
        }
        Vector3f world=cup.transform(local.mul(p.renderScale()/16)).add(pivot);
        Quaternionf rotation=new Quaternionf(cup).rotateX((float)Math.toRadians(pitch)).rotateZ((float)Math.toRadians(roll));
        return new BottleRig.Transform(world,rotation);
    }

    public static Camera camera(Action action,double t,int duration) {
        if(action==Action.INSPECT) {
            double a=window(t,4,20,82,103);
            return new Camera(.48*a+.15*window(t,35,48,66,78),-.24*window(t,35,48,72,84),.08*Math.sin(t*.08)*a);
        }
        if(action==Action.DRINK) {
            double end=48+BottleMotion.sipTicks(duration),a=window(t,12,39,end,end+14);
            return new Camera(.30*a,-.08*a,.03*Math.sin(t*.13)*a);
        }
        return BottleMotion.camera(action,t,duration);
    }

    public static boolean spoonVisible(Action action,double t,int duration,boolean replacement) {return true;}
    public static List<AnimationSoundTimeline.Cue> inspectionSounds() {return List.of(
        new AnimationSoundTimeline.Cue(7,"inspect_lift",.34f,1.02f),
        new AnimationSoundTimeline.Cue(18,"inspect_support",.24f,1.04f),
        new AnimationSoundTimeline.Cue(27,"inspect_turn",.18f,1.13f),
        new AnimationSoundTimeline.Cue(39,"inspect_turn",.15f,1.18f),
        new AnimationSoundTimeline.Cue(55,"inspect_settle",.10f,1.16f),
        new AnimationSoundTimeline.Cue(68,"inspect_turn",.20f,1.05f),
        new AnimationSoundTimeline.Cue(92,"inspect_return",.28f,.99f));}
    public static List<AnimationSoundTimeline.Cue> eatingSounds() {return List.of(
        new AnimationSoundTimeline.Cue(54,"inspect_turn",.12f,1.18f),
        new AnimationSoundTimeline.Cue(61,"custard_eat",.58f,1.06f),
        new AnimationSoundTimeline.Cue(67,"inspect_turn",.11f,1.22f),
        new AnimationSoundTimeline.Cue(72,"custard_eat",.61f,1.03f),
        new AnimationSoundTimeline.Cue(78,"inspect_turn",.10f,1.25f),
        new AnimationSoundTimeline.Cue(83,"custard_eat",.55f,1.09f));}
    public static double gestureImpulse(Action action,int tick) {
        if(action==Action.INSPECT&&(tick==27||tick==39||tick==68))return tick==39?-.025:.03;
        if(action==Action.DRINK&&(tick==61||tick==72||tick==83))return .018;
        return 0;
    }

    private static Pose track(double t,double[] times,Pose[] poses) {
        if(t<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(t<times[i])return poses[i-1].mix(poses[i],smooth((t-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
    private static Vector3f lerp(Vector3f a,Vector3f b,double t) {return new Vector3f(a).lerp(b,(float)t);}
    private static double window(double t,double start,double in,double out,double end) {return smooth((t-start)/(in-start))*(1-smooth((t-out)/(end-out)));}
    private static double smooth(double u) {u=Math.max(0,Math.min(1,u));return u*u*u*(u*(u*6-15)+10);}
    private BambooEggAnimations() {}
}

