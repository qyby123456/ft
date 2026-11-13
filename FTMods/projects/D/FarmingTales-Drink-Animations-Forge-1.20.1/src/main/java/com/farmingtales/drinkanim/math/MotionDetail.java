package com.farmingtales.drinkanim.math;

/** Small, event-driven follow-through; units are authored ticks, not render frames. */
public final class MotionDetail {
    public static double turnProgress(double u) {
        u=Math.max(0,Math.min(1,u));
        // Apply torque early and leave more time to brake into the next face.
        return BottleMotion.smooth(u+.30*Math.sin(Math.PI*u));
    }
    public static double turnYaw(double t,int first) {
        double yaw=BottleMotion.IDLE.yaw();
        for(int i=0;i<4;i++)yaw+=90*turnProgress((t-first-16*i)/7);
        return yaw;
    }
    public static double settle(double t,double start,double length) {
        double age=t-start;
        if(age<=0||age>=length)return 0;
        return BottleMotion.smooth(age/1.4)*Math.sin(age*.87)*Math.exp(-age*.34)
            *(1-BottleMotion.smooth((age-length+3)/3));
    }
    public static BottleMotion.Pose facet(BottleMotion.Pose p,double t,int first) {
        double drive=0,brake=0;
        for(int i=0;i<4;i++) {
            double start=first+16*i;
            drive+=BottleMotion.gesturePulse(t,start,7);
            brake+=settle(t,start+7,9);
        }
        return new BottleMotion.Pose(p.x()+.011*drive,p.y()+.022*drive-.013*brake,
            p.z()-.015*drive+.008*brake,p.pitch()+2.1*drive-2.4*brake,turnYaw(t,first),
            p.roll()-1.2*drive+1.0*brake,p.fill());
    }
    /** A short load, decisive transfer and damped stop; the complete bottle and gripping hand share it. */
    public static BottleMotion.Pose gripResponse(DrinkProfile profile,BottleMotion.Action action,double t,int duration,BottleMotion.Pose p) {
        double load=0,stop=0;
        if(action==BottleMotion.Action.DRAW) {
            load=BottleMotion.gesturePulse(t,3,5);stop=settle(t,12,7);
        } else if(action==BottleMotion.Action.STOW) {
            load=BottleMotion.gesturePulse(t,1,5);
        } else if(action==BottleMotion.Action.INSPECT) {
            var family=ContainerAnimations.family(profile);
            load=BottleMotion.gesturePulse(t,3,7);
            stop=settle(t,family==ContainerAnimations.Family.BRANDY?25:16,7);
            if(family==ContainerAnimations.Family.SPORTS) {
                stop+=settle(t,32,7)+settle(t,42,7);
            } else if(family==ContainerAnimations.Family.WATER) {
                load+=2.3*BottleMotion.gesturePulse(t,18,12);
                stop+=2.6*settle(t,48,8);
            }
            if(family!=ContainerAnimations.Family.WATER)stop+=.6*settle(t,96,7);
        } else if(action==BottleMotion.Action.DRINK) {
            // Opening effort moves the entire grip, never the cork relative to the neck.
            load=.5*BottleMotion.gesturePulse(t,10,7);
            if(profile.cork())stop=settle(t,31,6);
            else if(!profile.cup()&&!profile.can()&&profile.style()!=DrinkProfile.Style.BEER)stop=.6*settle(t,34,6);
        }
        return p.offset(0,-.018*load-.024*stop,.012*load+.012*stop,-1.2*load-2.1*stop,0);
    }
    public static double gripCamera(DrinkProfile p,BottleMotion.Action action,double t) {
        if(action==BottleMotion.Action.DRAW)return .55*settle(t,12.6,6);
        if(action==BottleMotion.Action.STOW)return .25*settle(t,6,7);
        if(action==BottleMotion.Action.DRINK)return p.cup()?0:.45*settle(t,p.cork()?31.5:34.5,6);
        if(action!=BottleMotion.Action.INSPECT)return 0;
        var family=ContainerAnimations.family(p);double impact=.50*settle(t,family==ContainerAnimations.Family.BRANDY?25.6:16.6,7);
        int[] beats=family==null?new int[]{29,45,61,77}:switch(family) {
            case RUM -> new int[]{30,46,62,78}; case BEER -> new int[]{29,45,61,77};
            case CAN, JUICE -> new int[]{};
            case SPORTS -> new int[]{32,42}; case WATER -> new int[]{48}; case BRANDY -> new int[]{};
        };
        for(int beat:beats)impact+=.55*settle(t,beat+.6,7);
        return impact;
    }
    public static double swallow(double t,int duration) {
        double value=0;
        for(var cue:AnimationSoundTimeline.drinking(duration))
            value+=settle(t,cue.tick()+1,8);
        return value;
    }
    private MotionDetail() {}
}
