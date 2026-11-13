package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;

/** Small, contact-preserving performance variations over the container's authored action. */
public final class EatingMotion {
    public static final int VARIANTS=4;
    private record Style(double x,double y,double z,double pitch,double yaw,double roll) {}
    private static final Style[] STYLES={
        new Style(.018,-.004,.006,.8,-1.2,.55),
        new Style(-.015,.008,.014,-.4,1.1,-.35),
        new Style(.006,-.010,.018,.5,.7,-.60),
        new Style(-.007,.010,-.003,-.6,-.8,.40)
    };
    private record Detail(double x,double y,double z,double pitch,double yaw,double roll) {}

    public static BottleRig.Frame apply(BottleRig.Frame rig,DrinkProfile p,Action action,
                                        double t,int duration,int variant,int side) {
        if(action!=Action.DRINK)return rig;
        Detail d=detail(p,t,duration,variant);
        Vector3f shift=new Vector3f((float)(side*d.x),(float)d.y,(float)d.z);
        Quaternionf turn=new Quaternionf().rotateXYZ(radians(d.pitch),radians(side*d.yaw),radians(side*d.roll));
        // Rotate about the holding palm, then move the entire assembly. Both hands,
        // closures and the container retain exactly the approved relative contacts.
        Vector3f pivot=rig.hand().position();
        return new BottleRig.Frame(move(rig.bottle(),pivot,shift,turn),move(rig.hand(),pivot,shift,turn),
            move(rig.helper(),pivot,shift,turn),move(rig.cork(),pivot,shift,turn),rig.assistance());
    }

    public static Camera camera(DrinkProfile p,Action action,double t,int duration,int variant) {
        if(action!=Action.DRINK)return new Camera(0,0,0);
        // The eyes respond after the hands. No random sampling or frame-history
        // dependency here: pausing, rendering twice and exporting give the same pose.
        Detail a=detail(p,t-1.4,duration,variant),b=detail(p,t-2.8,duration,variant);
        double end=48+BottleMotion.sipTicks(duration);
        double fade=1-smooth((t-end-4)/8);
        double lift=smooth((t-(p.cup()?6:36))/14)*(1-smooth((t-end)/12));
        return new Camera(clamp((.17*lift+(a.y*.65+b.y*.35)*7+a.pitch*.045)*fade,.32),
            clamp(((a.x*.65+b.x*.35)*6+a.yaw*.055)*fade,.20),
            clamp((a.roll*.024+b.roll*.014)*fade,.045));
    }

    private static Detail detail(DrinkProfile p,double t,int duration,int variant) {
        Style s=STYLES[Math.floorMod(variant,VARIANTS)];
        double begin=p.cup()?0:34,end=48+BottleMotion.sipTicks(duration);
        double active=smooth((t-begin)/(48-begin))*(1-smooth((t-end)/12));
        double lift=pulse(t,begin,begin+(48-begin)*.55,48);
        double lower=pulse(t,end-2,end+3,end+12);
        double balance=pulse(t,49,49+(end-49)*.42,end);
        double beat=0,sway=0;int i=0;
        for(var cue:AnimationSoundTimeline.drinking(p,duration)) {
            if(!cue.event().equals("gulp")&&!cue.event().equals("jam_eat")&&!cue.event().equals("custard_eat"))continue;
            double sign=((i+variant)%2==0?1:-1),gain=.78+.12*((i+variant)%3);
            // Fast load, slower release and a small opposite recovery. Successive
            // mouthfuls have different directions and emphasis, not a looping sine.
            double response=pulse(t,cue.tick()-2,cue.tick()+.7,cue.tick()+5.5)
                -.24*pulse(t,cue.tick()+3,cue.tick()+5.5,cue.tick()+9);
            beat+=gain*response;sway+=sign*gain*response;i++;
        }
        double family=p.style()==DrinkProfile.Style.BRANDY||ContainerAnimations.family(p)==ContainerAnimations.Family.BRANDY?.65:p.cup()||p.food()?.8:p.jam()?.85:1;
        double gate=smooth((t-46)/3)*(1-smooth((t-end)/10));
        return new Detail(family*(s.x*active+.006*s.yaw*lift-.005*s.yaw*lower+.003*sway*gate),
            family*(s.y*active+.008*lift-.009*lower-.0045*beat*gate),
            family*(s.z*active+.006*balance-.004*beat*gate),
            family*(s.pitch*active+.5*lift-.7*lower+.35*beat*gate),
            family*(s.yaw*active+.45*sway*gate),
            family*(s.roll*active+.22*sway*gate));
    }

    private static double pulse(double t,double start,double crest,double end) {
        return smooth((t-start)/(crest-start))*(1-smooth((t-crest)/(end-crest)));
    }
    private static double smooth(double u) {u=Math.max(0,Math.min(1,u));return u*u*u*(u*(u*6-15)+10);}
    private static double clamp(double x,double limit) {return Math.max(-limit,Math.min(limit,x));}
    private static float radians(double d) {return (float)Math.toRadians(d);}
    private static BottleRig.Transform move(BottleRig.Transform t,Vector3f pivot,Vector3f shift,Quaternionf turn) {
        return new BottleRig.Transform(turn.transform(new Vector3f(t.position()).sub(pivot)).add(pivot).add(shift),
            new Quaternionf(turn).mul(t.rotation()));
    }
    private EatingMotion() {}
}
