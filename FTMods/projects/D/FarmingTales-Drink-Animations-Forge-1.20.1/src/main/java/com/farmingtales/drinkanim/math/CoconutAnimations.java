package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;

/** Present the coconut; after eating its pineapple, drink directly from the rim. */
public final class CoconutAnimations {
    public static boolean owns(DrinkProfile p) {return p.id().equals("papercraft_magic_decoration:ferry_whiskey");}
    public static Pose sample(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(action==Action.INSPECT) {
            double hold=BottleMotion.smooth(t/23)*(1-BottleMotion.smooth((t-79)/25));
            double turn=Math.sin(Math.PI*BottleMotion.smooth((t-20)/59));
            double look=BottleMotion.smooth((t-43)/17)*(1-BottleMotion.smooth((t-72)/12));
            Pose shown=new Pose(.18-.025*turn,-.49+.016*turn,-.98,
                9+12*look,155+38*turn,-3*turn,.8);
            return BottleMotion.IDLE.mix(shown,hold);
        }
        return CupAnimations.sampleGeneric(p,action,t,duration,replacement);
    }
    public static double support(double t) {
        return BottleMotion.smooth((t-5)/15)*(1-BottleMotion.smooth((t-78)/19));
    }
    public static BottleMotion.Camera camera(Action action,double t,int duration) {
        if(action!=Action.DRINK)return new BottleMotion.Camera(0,0,0);
        double end=48+BottleMotion.sipTicks(duration);
        double hold=BottleMotion.smooth((t-39)/9)*(1-BottleMotion.smooth((t-end)/12));
        return new BottleMotion.Camera((.5+1.1*BottleMotion.smooth((t-48)/(end-48))+.18*MotionDetail.swallow(t,duration))*hold,0,.02*hold);
    }
    private CoconutAnimations() {}
}
