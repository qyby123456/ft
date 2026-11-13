package com.farmingtales.drinkanim.math;

import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleRig.Transform;

/** Opposed hands brace the jar and pull its tight wooden stopper free. */
public final class JamAnimations {
    private static final Pose REST=BottleMotion.IDLE;
    private static final Pose OPEN=new Pose(.02,-.49,-1.03,-5,155,-6,.8);
    private static final Pose SIP=new Pose(.10,-.34,-.64,101,130,-5,.8);
    private static final Pose EMPTY=new Pose(.09,-.32,-.62,114,130,-5,.055);
    private static final Pose TOSS=REST.offset(-.10,.09,.01,0,0);
    public static final int DRAW_TICKS=14;
    public static final int INSPECT_TICKS=86;
    public static final int INSPECT_SHAKE_OFFSET=2;
    private static final int CAP_FLIGHT_END=58;
    private static final Pose DISPLAY=new Pose(.38,-.59,-1.13,-5,155,-8,.8);
    private static final double YAW=Math.toRadians(-22.5);
    private record Key(double tick,Pose pose) {}
    private static Key k(double t,double x,double y,double z,double pitch,double yaw,double roll) {
        return new Key(t,new Pose(x,y,z,pitch,yaw,roll,.8));
    }
    private static final List<Key> OPENING=List.of(new Key(0,REST),new Key(10,OPEN),new Key(15,OPEN),
        k(22,.025,-.515,-1.035,-6,155,-8),k(27,.03,-.54,-1.04,-6,155,-9),
        k(29,.055,-.565,-1.045,-7,155,-3),k(32,.06,-.54,-1.015,-2,155,-3),
        k(35,.13,-.52,-.98,8,151,-3),k(40,.17,-.44,-.84,48,140,-4),new Key(48,SIP));
    public static Pose sample(Action action,double t,int duration,boolean replacement) {
        if(action==Action.IDLE||action==Action.STOW)return BottleMotion.sample(action,t,duration,replacement);
        if(action==Action.DRAW)return draw(t);
        if(action==Action.INSPECT)return inspect(t);
        double end=48+BottleMotion.sipTicks(duration);
        if(t<=48) {
            double tremor=strain(t);
            return curve(OPENING,t).offset(.0015*tremor,-.003*tremor,0,.32*tremor,0);
        }
        if(t<end) {
            double u=(t-48)/(end-48);
            Pose held=SIP.offset(.01,-.012,-.015,5,0);
            Pose p=curve(List.of(new Key(0,SIP),new Key(.28,held),new Key(.47,held),new Key(.76,EMPTY.wine(.8)),new Key(1,EMPTY.wine(.8))),u);
            double fill=.8-.31*BottleMotion.smooth(u/.24)-.25*BottleMotion.smooth((u-.55)/.15)
                -.065*BottleMotion.smooth((u-.715)/.06)-.060*BottleMotion.smooth((u-.80)/.06)-.060*BottleMotion.smooth((u-.89)/.07);
            return InertialGestures.jamShake(u*40,p.wine(fill));
        }
        double tail=t-end;
        if(tail<=6)return curve(List.of(new Key(0,EMPTY),new Key(2,new Pose(.17,-.44,-.76,65,142,-3,.055)),new Key(6,REST.wine(.055))),tail);
        if(tail<=16)return BottleMotion.sample(Action.STOW,(tail-6)*2,duration,false).wine(.055);
        Pose hidden=BottleMotion.sample(Action.STOW,20,duration,false).wine(.055);
        if(tail<=20||!replacement)return hidden;
        if(tail<=22)return hidden.wine(.8);
        return draw((tail-22)*19/BottleMotion.REPLACEMENT_DRAW_TICKS);
    }

    private static Pose draw(double t) {
        Pose hidden=BottleMotion.sample(Action.STOW,20,16,false);
        if(t<4.5)return curve(List.of(new Key(0,hidden),new Key(2.8,TOSS.offset(0,-.32,0,0,0)),new Key(4.5,TOSS)),t,0,.27*4/7.5);
        if(t<12)return flight(TOSS,TOSS,(t-4.5)/7.5,.27);
        return curve(List.of(new Key(12,TOSS),new Key(13.3,TOSS.offset(-.045,-.068,0,3,-3)),
            new Key(15.4,TOSS.offset(-.018,-.016,0,-1,1)),new Key(19,REST)),t,-.27*4/7.5,0);
    }

    private static Pose flight(Pose a,Pose b,double u,double height) {
        return a.mix(b,u).offset(0,height*4*u*(1-u),0,0,0);
    }
    private static Pose inspect(double t) {
        Pose p;
        if(t<20)p=curve(List.of(new Key(0,REST),k(4,.48,-.71,-1.10,-7,155,-3),
            new Key(12,DISPLAY),new Key(16,DISPLAY.offset(0,-.07,0,-6,0)),new Key(20,DISPLAY)),t,0,.30*4/8);
        else if(t<28) {
            double u=(t-20)/8;
            p=flight(DISPLAY,DISPLAY,u,.30).offset(0,0,0,0,360*u);
        } else p=curve(List.of(new Key(28,DISPLAY),new Key(29.5,DISPLAY.offset(-.025,-.060,0,3,-2)),
            new Key(33,DISPLAY.offset(-.008,-.012,0,-1,1)),new Key(36,DISPLAY),new Key(64,DISPLAY),
            new Key(71,DISPLAY),new Key(76,DISPLAY),new Key(INSPECT_TICKS,REST)),t,-.30*4/8,0).offset(0,0,0,0,360);
        Pose shaken=InertialGestures.apply(ContainerAnimations.Family.CAN,t-INSPECT_SHAKE_OFFSET,p);
        p=p.mix(shaken,.70*(1-BottleMotion.smooth((t-64)/12)));
        double stop=(MotionDetail.settle(t,12,5)+.5*MotionDetail.settle(t,64,6))*(1-BottleMotion.smooth((t-76)/10));
        return p.offset(0,-.008*stop,0,-.6*stop,0);
    }

    private static Pose curve(List<Key> keys,double t) {
        return curve(keys,t,0,0);
    }
    private static Pose curve(List<Key> keys,double t,double startYVelocity,double endYVelocity) {
        if(t<=keys.get(0).tick())return keys.get(0).pose();
        for(int i=1;i<keys.size();i++)if(t<keys.get(i).tick()) {
            Key a=keys.get(i-1),b=keys.get(i);double h=b.tick()-a.tick(),u=(t-a.tick())/h;
            double[] av=values(a.pose()),bv=values(b.pose()),out=new double[7];
            for(int c=0;c<7;c++) {
                double d=(bv[c]-av[c])/h;
                double m0=i==1?0:tangent((av[c]-values(keys.get(i-2).pose())[c])/(a.tick()-keys.get(i-2).tick()),d);
                double m1=i==keys.size()-1?0:tangent(d,(values(keys.get(i+1).pose())[c]-bv[c])/(keys.get(i+1).tick()-b.tick()));
                if(c==1&&i==1)m0=startYVelocity;
                if(c==1&&i==keys.size()-1)m1=endYVelocity;
                out[c]=(2*u*u*u-3*u*u+1)*av[c]+(u*u*u-2*u*u+u)*h*m0+(-2*u*u*u+3*u*u)*bv[c]+(u*u*u-u*u)*h*m1;
            }
            return new Pose(out[0],out[1],out[2],out[3],out[4],out[5],out[6]);
        }
        return keys.get(keys.size()-1).pose();
    }
    private static double tangent(double a,double b) {return a*b<=0?0:2*a*b/(a+b);}
    private static double[] values(Pose p) {return new double[]{p.x(),p.y(),p.z(),p.pitch(),p.yaw(),p.roll(),p.fill()};}
    public static double assistance(Action action,double t) {
        return assistance(action,t,16);
    }
    public static double assistance(Action action,double t,int duration) {
        if(action==Action.DRINK)return window(t,4,13,34,42);
        return 0;
    }
    private static double window(double t,double a,double b,double c,double d) {
        return BottleMotion.smooth((t-a)/(b-a))*(1-BottleMotion.smooth((t-c)/(d-c)));
    }
    private static double strain(double t) {
        return Math.sin((t-20)*Math.PI*1.35)*window(t,20,23,26,27);
    }
    public static Camera camera(Action action,double t,int duration) {
        if(action==Action.IDLE)return new Camera(0,0,0);
        if(action==Action.DRAW) {
            double catchWeight=MotionDetail.settle(t,12,5);
            return new Camera(-.18*catchWeight,.07*catchWeight,0);
        }
        if(action==Action.STOW)return BottleMotion.camera(action,t,duration);
        if(action==Action.INSPECT) {
            double stop=(MotionDetail.settle(t,12,5)+1.3*MotionDetail.settle(t,28,5)+.5*MotionDetail.settle(t,64,6))*(1-BottleMotion.smooth((t-76)/10));
            return new Camera(-.075*stop+.22*InertialGestures.camera(ContainerAnimations.Family.CAN,t-INSPECT_SHAKE_OFFSET).pitch()*(1-BottleMotion.smooth((t-64)/12)),.035*window(t,21,27,36,45),0);
        }
        double lift=window(t,34,48,48+BottleMotion.sipTicks(duration),64+BottleMotion.sipTicks(duration));
        double span=BottleMotion.sipTicks(duration);
        double shake=t>=48&&t<=48+span?InertialGestures.jamCamera((t-48)*40/span):0;
        return new Camera(.06*lift+.16*MotionDetail.settle(t,27,6)+shake,-.015*lift,0);
    }
    public static List<AnimationSoundTimeline.Cue> inspectionSounds() {
        return List.of(new AnimationSoundTimeline.Cue(6,"inspect_lift",.36f,1),
            new AnimationSoundTimeline.Cue(19,"inspect_turn",.14f,1.02f),
            new AnimationSoundTimeline.Cue(28,"inspect_settle",.18f,1.04f),
            new AnimationSoundTimeline.Cue(32,"inspect_turn",.17f,.99f),
            new AnimationSoundTimeline.Cue(40,"inspect_turn",.17f,1.02f),
            new AnimationSoundTimeline.Cue(48,"inspect_turn",.16f,.98f),
            new AnimationSoundTimeline.Cue(64,"inspect_settle",.10f,1.02f),
            new AnimationSoundTimeline.Cue(78,"inspect_return",.24f,.98f));
    }
    public static List<AnimationSoundTimeline.Cue> eatingSounds(int duration) {
        int span=BottleMotion.sipTicks(duration);
        return List.of(new AnimationSoundTimeline.Cue(48+(int)(span*.10),"jam_eat",.55f,1.06f),
            new AnimationSoundTimeline.Cue(48+(int)(span*.54),"inspect_turn",.12f,1.06f),
            new AnimationSoundTimeline.Cue(48+(int)(span*.72),"inspect_turn",.13f,1.08f),
            new AnimationSoundTimeline.Cue(48+(int)(span*.805),"inspect_turn",.13f,1.04f),
            new AnimationSoundTimeline.Cue(48+(int)(span*.89),"inspect_turn",.11f,1.00f),
            new AnimationSoundTimeline.Cue(48+(int)(span*.95),"jam_eat",.59f,1.0f));
    }
    public static boolean open(Action action,double t,int duration,boolean replacement) {
        return action==Action.DRINK&&t>=28&&(!replacement||t<48+BottleMotion.sipTicks(duration)+20);
    }
    public static boolean detached(Action action,double t) {return action==Action.DRINK&&t>15&&t<CAP_FLIGHT_END;}
    public static boolean visible(Action action,double t,int duration,boolean replacement) {
        return action!=Action.DRINK||t<CAP_FLIGHT_END||!open(action,t,duration,replacement);
    }
    public static BottleRig.Frame rig(DrinkProfile p,Pose motion,Action action,double t,int duration,int side,boolean slim) {
        float scale=p.renderScale(),unit=scale/16;
        Quaternionf rotation=BottleRig.rotation(motion,side);
        Vector3f pivot=new Vector3f((float)(side*motion.x()*.75),(float)(motion.y()*.75+.30*scale),(float)(motion.z()*.75));
        // Use the same carry grip and sleeve orientation as the existing wine rig.
        Vector3f contact=BottleRig.idleGripOffset(side,slim);
        contact.x+=side*.016f;
        BottleRig.rotation(REST,side).conjugate().transform(contact);
        contact.mul(p.width()/4*p.scale(),1,p.depth()/4*p.scale());
        contact.y+=(3.35f-4.8f)*unit;
        Vector3f palm=new Quaternionf(rotation).transform(new Vector3f(contact)).add(pivot);
        double flight=action==Action.DRAW?t:action==Action.DRINK?(t-(48+BottleMotion.sipTicks(duration)+22))*19/BottleMotion.REPLACEMENT_DRAW_TICKS:-1;
        Quaternionf right=new Quaternionf(rotation).mul(BottleRig.rotation(REST,side).conjugate())
            .mul(BottleRig.armRotation(ArmMotion.REST,side,false));
        Transform hand=new Transform(palm,right);
        if(action==Action.DRINK||action==Action.INSPECT) {
            double end=48+BottleMotion.sipTicks(duration);
            float engaged=(float)(action==Action.INSPECT?window(t,0,10,76,INSPECT_TICKS):window(t,0,10,end+5,end+6));
            Vector3f grasp=canContact(p,side,slim);
            Vector3f held=new Quaternionf(rotation).transform(grasp).add(pivot);
            Quaternionf arm=canArm(motion,action,t,duration,side);
            hand=blend(hand,new Transform(held,arm),engaged);
        }
        if(flight>=4.5&&flight<=12) {
            Pose receive=curve(List.of(new Key(4.5,TOSS),new Key(7.2,TOSS.offset(.42,-.13,.10,0,0)),
                new Key(8.6,TOSS.offset(.42,-.13,.10,0,0)),new Key(12,TOSS)),flight,.27*4/7.5,-.27*4/7.5);
            Vector3f received=BottleRig.rotation(receive,side).transform(new Vector3f(contact)).add(pivot(receive,p,side));
            hand=new Transform(received,right);
        }
        if(action==Action.INSPECT&&t>=20&&t<=28) {
            // Follow the release upward, then keep the palm below the airborne jar to receive it.
            Pose receive=curve(List.of(new Key(20,DISPLAY),new Key(22,DISPLAY.offset(.015,.08,-.005,4,0)),
                new Key(25,DISPLAY.offset(.01,.05,0,2,0)),new Key(28,DISPLAY)),t,.30*4/8,-.30*4/8);
            Vector3f received=BottleRig.rotation(receive,side).transform(canContact(p,side,slim)).add(pivot(receive,p,side));
            hand=new Transform(received,canArm(receive,action,t,duration,side));
        }
        float[] cp=p.capPivot();
        Vector3f capLocal=new Vector3f((cp[0]-8)*unit,(cp[1]-4.8f)*unit,(cp[2]-8)*unit);
        boolean carrying=action==Action.DRINK&&t>27&&t<CAP_FLIGHT_END;
        Pose capPose=carrying?sample(Action.DRINK,27,duration,false):motion;
        Quaternionf capBase=BottleRig.rotation(capPose,side),capRotation=new Quaternionf(capBase);
        // Grip the whole plug and draw it straight along the neck; no corner fulcrum or twist.
        if(carrying)capLocal.y+=(float)(.145*BottleMotion.smooth((t-27)/3));
        Vector3f cap=new Quaternionf(capBase).transform(capLocal).add(carrying?pivot(capPose,p,side):pivot);
        if(carrying)cap.z-=(float)(.025*BottleMotion.smooth((t-30)/3));
        // The palm wraps the upper left half of the plug, clear of the holding hand.
        Vector3f pinch=new Quaternionf().rotateY((float)YAW).transform(new Vector3f(.65f,0,-.65f));
        pinch.add(8-cp[0],6.50f-cp[1],8-cp[2]).mul(unit);
        Vector3f helper=new Quaternionf(capRotation).transform(pinch).add(cap).add(-side*.025f,-.020f,.035f);
        if(carrying&&t>33) {
            double flightTime=t-33,exit=BottleMotion.smooth(flightTime/9);
            cap.add(0,(float)(.026*flightTime-.0065*flightTime*flightTime),(float)(-.11*flightTime));
            capRotation.rotateX((float)Math.toRadians(22*flightTime)).rotateZ((float)Math.toRadians(-side*8*flightTime));
            helper.add((float)(-side*.15*exit),(float)(-.65*exit),(float)(.03*exit));
        }
        double assist=assistance(action,t,duration);
        double pull=BottleMotion.smooth((t-27)/3)*(1-BottleMotion.smooth((t-34)/8));
        Transform helperGrip=new Transform(helper,BottleRig.armRotation(new ArmMotion.Pose((4+12*pull)*assist-.8*strain(t),(4+6*pull)*assist,0),-side,true));
        if(!(action==Action.DRINK&&t>=15&&t<44)) {
            helperGrip=new Transform(new Vector3f(helperGrip.position()).lerp(new Vector3f(-side*.8f,-1.05f,-.95f),(float)(1-assist)),helperGrip.rotation());
        }
        return new BottleRig.Frame(new Transform(pivot,rotation),hand,helperGrip,new Transform(cap,capRotation),assist);
    }
    private static Vector3f canContact(DrinkProfile p,int side,boolean slim) {
        // Same rear-side palm anchor as mozhao; lower it to the short jar's centre.
        Vector3f contact=BottleRig.idleGripOffset(side,slim);
        BottleRig.rotation(REST,side).conjugate().transform(contact);
        contact.mul(p.width()/4*p.scale(),1,p.depth()/4*p.scale());
        contact.y+=(3.0f-4.8f)*p.renderScale()/16;
        contact.add(new Quaternionf().rotateY((float)YAW).transform(new Vector3f(-side*(slim?.30f:.40f),0,0).mul(p.renderScale()/16)));
        return contact;
    }
    private static Quaternionf canArm(Pose motion,Action action,double t,int duration,int side) {
        // Mozhao's authored forearm basis, with the jar's own motion driving the elbow.
        double lift=Math.max(-18,Math.min(50,(motion.pitch()-REST.pitch())*.48+(motion.y()-REST.y())*24));
        double sweep=Math.max(-10,Math.min(10,(motion.x()-REST.x())*22));
        if(action==Action.INSPECT)lift+=20*window(t,5,19,67,INSPECT_TICKS);
        else {
            double end=48+BottleMotion.sipTicks(duration);
            double follow=window(t,34,43,end,end+6);
            lift+=(.62*(motion.pitch()-REST.pitch())-lift)*follow;
            lift+=.65*strain(t);
        }
        return BottleRig.armRotation(new ArmMotion.Pose(lift,sweep,0),side,false);
    }
    private static Vector3f pivot(Pose pose,DrinkProfile p,int side) {
        return new Vector3f((float)(side*pose.x()*.75),(float)(pose.y()*.75+.30*p.renderScale()),(float)(pose.z()*.75));
    }
    private static Transform blend(Transform a,Transform b,float weight) {
        return new Transform(new Vector3f(a.position()).lerp(b.position(),weight),new Quaternionf(a.rotation()).slerp(b.rotation(),weight));
    }
    private JamAnimations() {}
}
