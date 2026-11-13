package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;

/** Yaw turns the label; pitch and roll tilt the whole bottle around the gripping hand. */
public final class BottleMotion {
    public enum Action { IDLE, DRAW, STOW, DRINK, INSPECT, PLACE }
    public record Pose(double x,double y,double z,double pitch,double yaw,double roll,double fill) {
        public Pose mix(Pose b,double t) {return new Pose(lerp(x,b.x,t),lerp(y,b.y,t),lerp(z,b.z,t),lerp(pitch,b.pitch,t),lerp(yaw,b.yaw,t),lerp(roll,b.roll,t),lerp(fill,b.fill,t));}
        public Pose offset(double dx,double dy,double dz,double dp,double dr) {return new Pose(x+dx,y+dy,z+dz,pitch+dp,yaw,roll+dr,fill);}
        public Pose wine(double n) {return new Pose(x,y,z,pitch,yaw,roll,n);}
    }
    public record Camera(double pitch,double yaw,double roll) {
        public Quaternionf rotation() {
            return new Quaternionf().rotationXYZ(
                (float)Math.toRadians(pitch),
                (float)Math.toRadians(yaw),
                (float)Math.toRadians(roll)
            );
        }
        public double magnitude() {return Math.sqrt(pitch*pitch+yaw*yaw+roll*roll);}
    }

    public static final Pose IDLE=new Pose(.48,-.69,-1.08,-3,155,0,.8);
    public static final Pose RUN=new Pose(.52,-.77,-1.03,4,150,6,.8);
    public static final int DRAW_TICKS=19,STOW_TICKS=20;
    public static final int RAISE_TICKS=48,LOWER_TICKS=24,INSPECT_TICKS=104,CORK_RELEASE=27;
    private static final int EMPTY_STOW_TICKS=7,HIDE_ARM_TICKS=6,HIDDEN_TICKS=4;
    public static final int REPLACEMENT_DRAW_TICKS=18;
    private static final double ROOT_UNIT=.096;

    private static final double[] DRAW_TIMES={0,3.2,4.8,7.2,9.6,11.6,14.4,18.4,19};
    private static final Pose[] DRAW_POSES={
        rootOffset(3.8,-20.5,2.0),rootOffset(3.8,-20.5,2.0),rootOffset(3.6,-18.2,1.7),
        rootOffset(2.6,-10.8,1.0),rootOffset(.8,-2.4,-.2),rootOffset(-.18,.7,-.35),
        rootOffset(.07,-.18,.10),IDLE,IDLE
    };
    private static final double[] STOW_TIMES={0,2,4.4,7.2,10.4,19.6,20};
    private static final Pose[] STOW_POSES={
        IDLE,rootOffset(.08,-.04,.20),rootOffset(.75,-2.4,.8),rootOffset(2.7,-10.8,1.8),
        rootOffset(4.1,-20.4,2.4),rootOffset(4.1,-20.4,2.4),rootOffset(4.1,-20.4,2.4)
    };

    private static final double[] INSPECT_TIMES={0,6,16,22,32,42,50,56,66,74,80,84};
    private static final Pose[] INSPECT_POSES={
        IDLE,IDLE.offset(.01,-.01,0,-1,2),
        new Pose(.41,-.56,-1.04,-5,155,-12,.8),new Pose(.40,-.55,-1.035,-5,155,-14,.8),
        new Pose(.405,-.545,-1.025,-4,225,-18,.8),new Pose(.41,-.55,-1.03,-3,325,-16,.8),
        new Pose(.42,-.56,-1.03,2,405,-10,.8),new Pose(.42,-.56,-1.03,3,480,-10,.8),
        new Pose(.415,-.57,-1.045,-4,515,-13,.8),new Pose(.45,-.63,-1.07,-4,515,-5,.8),
        new Pose(.48,-.692,-1.08,-3,515,.2,.8),new Pose(.48,-.69,-1.08,-3,515,0,.8)
    };

    private static final double[] OPEN_TIMES={0,7,14,22,26,28,31,36,43,48};
    private static final Pose[] OPEN_POSES={IDLE,
        new Pose(.30,-.73,-1.08,-8,155,7,.8),new Pose(.24,-.71,-1.05,-6,155,7,.8),
        new Pose(.23,-.68,-1.05,-3,155,5,.8),new Pose(.22,-.65,-1.04,-1,155,5,.8),
        new Pose(.27,-.78,-1.10,-16,155,11,.8),new Pose(.24,-.70,-1.06,-9,155,7,.8),
        new Pose(.24,-.70,-1.06,-9,155,7,.8),new Pose(.18,-.40,-1.0,50,164,4,.8),
        new Pose(.10,-.38,-.96,76,168,6,.8)};

    public static Pose sample(Action action,double tick,int useTicks) {return sample(action,tick,useTicks,false);}
    public static Pose sample(Action action,double tick,int useTicks,boolean replacement) {
        if(action==Action.PLACE)return PlacementMotion.sample(DrinkCatalog.ORANGE,tick,replacement);
        if(action==Action.IDLE)return IDLE;
        if(action==Action.DRAW)return track(tick,DRAW_TIMES,DRAW_POSES);
        if(action==Action.STOW)return track(tick,STOW_TIMES,STOW_POSES);
        if(action==Action.INSPECT) {
            Pose p=track(inspectionMotionTime(tick),INSPECT_TIMES,INSPECT_POSES);
            double push=inspectionPush(tick),settle=inspectionRegrip(tick);
            return MotionDetail.facet(new Pose(p.x,p.y-.003*settle,p.z,
                p.pitch,inspectionYaw(tick),p.roll+.3*settle,p.fill),tick,22);
        }
        double sipEnd=RAISE_TICKS+sipTicks(useTicks);
        if(tick<=RAISE_TICKS) {
            Pose pose=track(tick,OPEN_TIMES,OPEN_POSES);
            double effort=ease((tick-16)/6)*(1-ease((tick-CORK_RELEASE)/2));
            double tension=gesturePulse(tick,17,9)*effort,release=MotionDetail.settle(tick,CORK_RELEASE,9);
            return pose.offset(-.004*tension,.006*tension-.014*release,.004*tension,
                .7*tension+2.2*release,0);
        }
        Pose raised=OPEN_POSES[OPEN_POSES.length-1];
        Pose empty=new Pose(.085,-.32,-.91,101,168,8,.025);
        if(tick<sipEnd) {
            double t=ease((tick-RAISE_TICKS)/sipTicks(useTicks));
            double swallow=MotionDetail.swallow(tick,useTicks);
            return raised.mix(empty,t).offset(0,.006*swallow,.003*swallow,1.2*swallow,0);
        }

        double tail=tick-sipEnd;
        Pose lowered=new Pose(.30,-.66,-1.02,-5,155,12,.025);
        Pose hidden=new Pose(.58,-1.76,-.88,10,145,14,.025);
        if(tail<=12)return empty.mix(lowered,ease(tail/12));
        if(tail<=LOWER_TICKS)return lowered.mix(hidden,ease((tail-12)/(LOWER_TICKS-12)));
        if(tail<=LOWER_TICKS+EMPTY_STOW_TICKS+HIDE_ARM_TICKS)return hidden;
        Pose hiddenFull=hidden.wine(replacement?.8:.025);
        if(tail<=drawStartTail())return hiddenFull;
        if(!replacement)return hidden;
        double draw=tail-drawStartTail();
        Pose settle=IDLE.offset(.018,.035,-.01,1.2,-.7);
        if(draw<=11)return hiddenFull.mix(settle,ease(draw/11));
        return settle.mix(IDLE,ease((draw-11)/(REPLACEMENT_DRAW_TICKS-11)));
    }

    public static int sipTicks(int useTicks) {return Math.max(40,useTicks);}
    public static int totalDrinkTicks(int useTicks) {return RAISE_TICKS+sipTicks(useTicks)+drawStartTail()+REPLACEMENT_DRAW_TICKS;}
    public static double idleBlend(Action action,double tick,int useTicks,boolean replacement) {
        if(action==Action.PLACE)return 0;
        if(action==Action.IDLE)return 1;
        if(action==Action.STOW)return 1-smooth(tick/6);
        double end=action==Action.DRINK?totalDrinkTicks(useTicks):action==Action.INSPECT?INSPECT_TICKS:DRAW_TICKS;
        double start=action==Action.DRAW?0:1-smooth(tick/6);
        return Math.min(1,start+(action!=Action.DRINK||replacement?smooth((tick-end+6)/6):0));
    }
    private static int drawStartTail() {return LOWER_TICKS+EMPTY_STOW_TICKS+HIDE_ARM_TICKS+HIDDEN_TICKS;}
    public static double assistance(Action action,double tick) {
        if(action==Action.DRINK)return openingHand(tick);
        if(action==Action.INSPECT)return inspectionHand(tick);
        return 0;
    }
    public static Camera camera(Action action,double tick,int useTicks) {
        if(action==Action.DRAW)return cameraTrack(tick,
            new double[]{0,3.6,8,11.6,15,18.4,19},
            new Camera[]{new Camera(0,0,0),new Camera(.55,.22,-.18),new Camera(2.40,-.75,.95),
                new Camera(-.85,.42,-.55),new Camera(.40,-.14,.20),new Camera(0,0,0),new Camera(0,0,0)});
        if(action==Action.STOW)return cameraTrack(tick,
            new double[]{0,3.6,8.4,13.6,17.2,19.6,20},
            new Camera[]{new Camera(0,0,0),new Camera(-.35,.16,-.14),new Camera(1.85,-.55,.78),
                new Camera(-1.05,.38,-.52),new Camera(.30,-.12,.20),new Camera(0,0,0),new Camera(0,0,0)});
        if(action==Action.INSPECT)return cameraTrack(inspectionMotionTime(tick),
            new double[]{0,6,16,22,34,44,53,64,74,80,84},
            new Camera[]{new Camera(0,0,0),new Camera(.18,.08,-.06),new Camera(1.20,-.42,.30),
                new Camera(.62,-.18,.08),new Camera(.95,.38,-.26),new Camera(.55,.56,-.18),
                new Camera(.82,-.30,.22),new Camera(.30,-.18,.06),new Camera(-.24,.10,-.10),
                new Camera(.06,-.02,.02),new Camera(0,0,0)});
        if(action==Action.DRINK) {
            double sipEnd=RAISE_TICKS+sipTicks(useTicks);
            Camera c=cameraTrack(tick,
                new double[]{0,14,24,27,30,36,48,sipEnd,sipEnd+12,sipEnd+24,sipEnd+41,totalDrinkTicks(useTicks)},
                new Camera[]{new Camera(0,0,0),new Camera(-.15,.08,0),new Camera(.18,-.05,.08),
                    new Camera(.30,-.06,.08),new Camera(-.42,.06,-.04),new Camera(.10,0,0),
                    new Camera(.95,-.15,.08),new Camera(1.20,-.12,.05),new Camera(.16,0,0),
                    new Camera(-.22,.08,-.04),new Camera(.12,-.03,.02),new Camera(0,0,0)});
            double swallow=.18*MotionDetail.swallow(tick-.8,useTicks);
            return new Camera(c.pitch+swallow,c.yaw,c.roll);
        }
        return new Camera(0,0,0);
    }
    public static double inspectionYaw(double tick) {
        return MotionDetail.turnYaw(tick,22);
    }
    public static double inspectionMotionTime(double tick) {
        if(tick<=22)return tick;
        for(int i=0;i<4;i++) {
            double local=tick-(22+i*16);
            if(local<=7)return 22+i*11+local;
            if(local<=16)return 29+i*11+(local-7)*4/9;
        }
        return tick-20;
    }
    public static double inspectionPush(double tick) {
        double value=0;for(int i=0;i<4;i++)value+=gesturePulse(tick,22+i*16,7);return value;
    }
    public static double inspectionRegrip(double tick) {
        double value=0;for(int i=0;i<3;i++)value+=gesturePulse(tick,31+i*16,6);return value;
    }
    public static double inspectionHand(double tick) {return ease((tick-12)/8)*(1-ease((tick-86)/10));}
    public static double smooth(double t) {t=Math.max(0,Math.min(1,t));return t*t*t*(t*(6*t-15)+10);}
    public static double gesturePulse(double tick,double start,double length) {
        double t=(tick-start)/length;return t<=0||t>=1?0:Math.pow(Math.sin(Math.PI*t),2);
    }
    public static double openingHand(double tick) {return ease((tick-5)/8)*(1-ease((tick-33)/8));}
    public static double corkLift(double tick) {return .025*ease((tick-16)/10)+.34*ease((tick-CORK_RELEASE)/4);}
    public static double corkRock(double tick) {return 0;}
    public static boolean corkVisible(Action action,double tick,int useTicks,boolean replacement) {
        if(action!=Action.DRINK)return true;
        if(tick<41)return true;
        return replacement&&tick>=RAISE_TICKS+sipTicks(useTicks)+drawStartTail();
    }
    public static boolean mouthOpen(Action action,double tick,int useTicks,boolean replacement) {
        return action==Action.DRINK&&tick>16
            &&!(replacement&&tick>=RAISE_TICKS+sipTicks(useTicks)+drawStartTail());
    }
    public static boolean mouthOpen(double tick) {return tick>16;}
    public static double mouthReveal(Action action,double tick,int duration,boolean replacement) {
        if(!mouthOpen(action,tick,duration,replacement))return 0;
        if(tick>=CORK_RELEASE+2)return 1;
        return Math.max(0,Math.min(1,(corkLift(tick)-.004)/.3125));
    }
    public static double ease(double t) {t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    private static double ring(double tick,double onset,double frequency,double damping) {double t=tick-onset;return t<=0?0:Math.sin(t*frequency)*Math.exp(-t*damping);}
    private static Pose track(double tick,double[] times,Pose[] poses) {
        if(tick<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(tick<times[i]) {
            int a=i-1,b=i,prev=Math.max(0,a-1),next=Math.min(poses.length-1,b+1);
            double span=times[b]-times[a],t=(tick-times[a])/span;
            double[] p=values(poses[prev]),x=values(poses[a]),y=values(poses[b]),n=values(poses[next]),r=new double[6];
            for(int c=0;c<6;c++) {
                double m0=a==0||x[c]==y[c]?0:(y[c]-p[c])/(times[b]-times[prev])*span;
                double m1=b==poses.length-1||x[c]==y[c]?0:(n[c]-x[c])/(times[next]-times[a])*span;
                r[c]=(2*t*t*t-3*t*t+1)*x[c]+(t*t*t-2*t*t+t)*m0+(-2*t*t*t+3*t*t)*y[c]+(t*t*t-t*t)*m1;
            }
            return new Pose(r[0],r[1],r[2],r[3],r[4],r[5],lerp(poses[a].fill,poses[b].fill,ease(t)));
        }
        return poses[poses.length-1];
    }
    private static Pose rootOffset(double x,double y,double z) {
        return IDLE.offset(x*ROOT_UNIT,y*ROOT_UNIT,z*ROOT_UNIT,0,0);
    }
    private static Camera cameraTrack(double tick,double[] times,Camera[] values) {
        if(tick<=times[0])return values[0];
        for(int i=1;i<times.length;i++)if(tick<times[i]) {
            double t=ease((tick-times[i-1])/(times[i]-times[i-1]));
            Camera a=values[i-1],b=values[i];
            return new Camera(lerp(a.pitch,b.pitch,t),lerp(a.yaw,b.yaw,t),lerp(a.roll,b.roll,t));
        }
        return values[values.length-1];
    }
    private static double[] values(Pose p) {return new double[]{p.x,p.y,p.z,p.pitch,p.yaw,p.roll};}
    private static double lerp(double a,double b,double t) {return a+(b-a)*t;}
}
