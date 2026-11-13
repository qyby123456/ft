package com.farmingtales.drinkanim.math;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

/** First-person crumbs follow the bite clock, so pause, cancellation and repeated render passes are stable. */
public final class ChocolateCrumbs {
    public record Crumb(Vector3f position,float size,float spin,int index) {}
    public static List<Crumb> sample(DrinkProfile p,Action action,double t,int duration,int side,boolean slim,int variant) {
        if(action!=Action.DRINK||!ServingAnimations.chocolate(p))return List.of();
        var result=new ArrayList<Crumb>();int biteIndex=0;
        for(int bite:ServingAnimations.bites(p)) {
            double seconds=(t-bite)*AnimationPlayback.raiseTicks(p)/960.0;
            if(seconds>=0&&seconds<.52) {
                var rig=BottleRig.solve(DrinkMotion.sample(p,action,bite, duration,true),action,bite,duration,side,slim,p);
                rig=EatingMotion.apply(rig,p,action,bite,duration,variant,side);
                var camera=DrinkMotion.camera(p,action,bite,duration,variant).rotation();
                var anchor=rig.cork().rotation().transform(ServingAnimations.tip(p).mul(biteIndex==0?1:.62f)).add(rig.cork().position());
                anchor.add(BottleFraming.offset(rig,action,bite,duration,true,p,camera));camera.transform(anchor);
                for(int i=0;i<8;i++) {
                    double life=.38+.02*(i%7);if(seconds>=life)continue;
                    double spread=(i-3.5)/3.5,vy=.14+.035*(i%3),vz=-.08-.025*(i%4);
                    var point=new Vector3f(anchor).add((float)(side*(spread*.12*seconds+.004*(i%3-1))),
                        (float)(vy*seconds-.95*seconds*seconds),(float)(vz*seconds+.006));
                    float fade=(float)(1-BottleMotion.smooth((seconds-life+.12)/.12));
                    result.add(new Crumb(point,(.007f+.002f*(i%3))*fade,(float)(i*.71+seconds*(3.5+i*.4)),i));
                }
            }
            biteIndex++;
        }
        return result;
    }
    private ChocolateCrumbs() {}
}
