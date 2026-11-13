package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;

/** Open cups share the juice swirl, with a direct lift and a lower drinking angle. */
public final class CupAnimations {
    public static boolean coffeePreview(DrinkProfile p) {
        return switch(p.id()) {
            case "papercraft_magic_decoration:nocturnal_cat_coffee",
                 "papercraft_magic_decoration:white_rabbit_mocha",
                 "papercraft_magic_decoration:gold_medal_coffee" -> true;
            default -> false;
        };
    }
    private static final Pose REST=BottleMotion.IDLE;
    private static final Pose SIP=new Pose(.08,-.48,-.89,48,163,2,.8);
    private static final Pose EMPTY=new Pose(.06,-.45,-.87,69,163,3,.025);
    public static Pose sample(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(CoconutAnimations.owns(p)&&(action==Action.DRINK||action==Action.INSPECT))
            return CoconutAnimations.sample(p,action,t,duration,replacement);
        return sampleGeneric(p,action,t,duration,replacement);
    }
    static Pose sampleGeneric(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(action==Action.INSPECT&&coffeePreview(p))return coffeeInspect(t);
        if(action!=Action.DRINK)return ContainerAnimations.sample(p,action,t,duration,replacement);
        double end=48+BottleMotion.sipTicks(duration);
        if(t<=48) {
            double u=BottleMotion.smooth(t/48);
            Pose a=REST.offset(-.13,.10,-.06,3,0);
            Pose b=new Pose(.10,-.52,-.94,24,162,1,.8);
            // One Bezier arc, one acceleration envelope: the elbow keeps moving
            // through the middle of the lift instead of stopping at every key.
            return REST.mix(a,u).mix(a.mix(b,u),u).mix(a.mix(b,u).mix(b.mix(SIP,u),u),u);
        }
        if(t<end) {
            Pose sip=SIP.mix(EMPTY,BottleMotion.smooth((t-48)/(end-48)));
            double swallow=MotionDetail.swallow(t,duration);
            return sip.offset(0,.0015*swallow,0,.25*swallow,0);
        }
        double tail=t-end;
        if(tail<=12)return EMPTY.mix(REST.wine(.025),BottleMotion.smooth(tail/12));
        if(tail<=24)return ContainerAnimations.sample(p,Action.STOW,(tail-12)*15/12,duration,false).wine(.025);
        Pose hidden=ContainerAnimations.sample(p,Action.STOW,20,duration,false).wine(.025);
        if(tail<=37||!replacement)return hidden;
        if(tail<=41)return hidden.wine(.8);
        return ContainerAnimations.sample(p,Action.DRAW,(tail-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS,duration,true);
    }
    private static Pose coffeeInspect(double t) {
        // Present the coffee surface with one hand, then trace one restrained
        // wrist circle. The elbow carries the mug through the return, with no regrip.
        double hold=BottleMotion.smooth(t/22)*(1-BottleMotion.smooth((t-78)/26));
        double u=BottleMotion.smooth((t-15)/65),phase=u*2*Math.PI;
        double circle=Math.sin(phase),lookSide=Math.sin(Math.PI*u);
        Pose look=new Pose(.22+.038*circle,-.47+.012*(1-Math.cos(phase)),-1.03-.025*lookSide,
            12+4*(1-Math.cos(phase)),155+22*lookSide,5*circle,.8);
        return REST.mix(look,hold);
    }
    public static double coffeeSupport(double t) {
        return 0;
    }
    public static BottleRig.Frame coffeeRig(DrinkProfile p,Action action,double t,int side,boolean slim,BottleRig.Frame rig) {
        return rig;
    }
    public static Camera camera(double t,int duration) {
        double end=48+BottleMotion.sipTicks(duration);
        double raise=BottleMotion.smooth((t-25)/23)*(1-BottleMotion.smooth((t-end)/15));
        if(t>end+41)return ContainerAnimations.camera(DrinkCatalog.find("farmingtales:sweet_orange_juice"),
            Action.DRAW,(t-end-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS,duration);
        return new Camera(.50*raise,-.045*raise,.018*raise);
    }
    public static Camera coffeeInspectCamera(double t) {
        double a=BottleMotion.smooth(t/12)*(1-BottleMotion.smooth((t-94)/10));
        double yaw=.06*Math.sin(BottleMotion.smooth((t-15)/65)*2*Math.PI);
        double sway=.012*Math.sin(t*.34)*a;
        return new Camera(.025*a,yaw+sway,.012*Math.sin(t*.25)*a);
    }
    private CupAnimations() {}
}

