package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleRig.Transform;
import com.farmingtales.drinkanim.math.BottleRig.Frame;

/** The carrying hand holds the cup; the other palm owns the edible garnish. */
public final class ServingAnimations {
    public record Serving(String kind,float[] rest,float[] tip,String[] stages) {}
    private static final Map<String,Serving> SERVINGS=load();
    private static final int[] ICE_BITES={14,22,30}, CHOCOLATE_BITES={15,25}, PINEAPPLE_BITES={14};
    public static final int GRASP_TICK=4, CLEAR_TICK=8, RELEASE_TICK=34, SPIT_TICK=37;
    private static final Pose OFFER=new Pose(.30,-.58,-1.16,-5,157,-2,.8);
    private static Map<String,Serving> load() {
        try(var in=ServingAnimations.class.getResourceAsStream("/assets/farmingtales_drink_anim/servings.json")) {
            if(in==null)return Map.of();
            return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),new TypeToken<Map<String,Serving>>(){}.getType());
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static boolean owns(DrinkProfile p) {return SERVINGS.containsKey(p.id());}
    public static boolean popsicle(DrinkProfile p) {return owns(p)&&SERVINGS.get(p.id()).kind().equals("POPSICLE");}
    public static boolean chocolate(DrinkProfile p) {return owns(p)&&SERVINGS.get(p.id()).kind().equals("CHOCOLATE");}
    public static boolean pineapple(DrinkProfile p) {return owns(p)&&SERVINGS.get(p.id()).kind().equals("PINEAPPLE");}
    public static int[] bites(DrinkProfile p) {return (popsicle(p)?ICE_BITES:pineapple(p)?PINEAPPLE_BITES:CHOCOLATE_BITES).clone();}
    private static int lastBite(DrinkProfile p) {int[] b=bites(p);return b[b.length-1];}
    public static String[] models(DrinkProfile p) {return owns(p)?SERVINGS.get(p.id()).stages():new String[]{p.capModel()};}
    public static int propStage(DrinkProfile p,Action action,double t) {
        if(!owns(p)||action!=Action.DRINK||t>=48)return 0;
        int stage=0;for(int bite:bites(p))if(t>=bite)stage++;
        return Math.min(models(p).length-1,stage);
    }
    public static String propModel(DrinkProfile p,Action a,double t) {return models(p)[propStage(p,a,t)];}
    private static double smooth(double t) {return BottleMotion.smooth(t);}
    private static double pulse(double t,double a,double b,double c) {
        return smooth((t-a)/(b-a))*(1-smooth((t-b)/(c-b)));
    }
    private static float rad(double a) {return (float)Math.toRadians(a);}
    private static double end(int duration) {return 48+BottleMotion.sipTicks(duration);}
    private static double biteResponse(DrinkProfile p,double t) {
        double response=0;int i=0;
        for(int bite:bites(p))response+=(1-.12*i++)*(pulse(t,bite-1.4,bite+.2,bite+2.5)-.23*pulse(t,bite+1,bite+2.5,bite+4.5));
        return response;
    }
    private static double withdrawal(DrinkProfile p,double t) {
        if(popsicle(p))return PopsicleMotion.recovery(t);
        int[] bites=bites(p);double result=0;
        for(int i=0;i<bites.length-1;i++)
            result+=(1-.12*i)*pulse(t,bites[i]+.15,bites[i]+2.6,bites[i+1]-.15);
        return result;
    }
    private static double swallow(DrinkProfile p,double t) {
        double result=0;
        for(int bite:bites(p))result+=pulse(t,bite+1.5,bite+3,bite+4.8)
            -.28*pulse(t,bite+3.8,bite+4.9,bite+6);
        return result;
    }
    private static double chew(DrinkProfile p,double t) {
        double result=0;
        for(int bite:bites(p))result+=pulse(t,bite+.1,bite+.65,bite+1.4)
            -.45*pulse(t,bite+1,bite+1.65,bite+2.5);
        return result;
    }
    public static Pose sample(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(chocolate(p)&&action==Action.INSPECT)return ChocolateInspection.cup(t);
        if(popsicle(p)&&action==Action.INSPECT)return PopsicleMotion.inspect(t);
        if(popsicle(p)&&action==Action.DRINK&&t<48)return PopsicleMotion.drink(t);
        if(action!=Action.DRINK||t>=48)return CupAnimations.sample(p,action,t,duration,replacement);
        Pose pose=BottleMotion.IDLE.mix(OFFER,smooth(t/9));
        double active=pulse(t,4,12,43),beat=biteResponse(p,t),take=pulse(t,2,5,12),away=withdrawal(p,t-.65),swallow=swallow(p,t);
        pose=pose.offset(-.004*beat+.010*take,.002*beat-.010*take-.006*away-.003*swallow+.002*Math.sin(t*.43)*active,
            -.006*take,-.2*beat-.8*take+.35*swallow,.35*away);
        pose=pose.mix(CupAnimations.sample(p,Action.DRINK,48,duration,replacement),smooth((t-(pineapple(p)?25:39))/(pineapple(p)?23:9)));
        return pose;
    }
    public static double assistance(DrinkProfile p,Action action,double t,int duration) {
        if(chocolate(p)&&action==Action.INSPECT)return ChocolateInspection.assistance(t);
        if(popsicle(p)&&action==Action.INSPECT)return PopsicleMotion.inspectAssistance(t);
        return action==Action.DRINK?smooth(t/GRASP_TICK)*(1-smooth((t-(popsicle(p)?RELEASE_TICK:lastBite(p)))/7)):0;
    }
    public static boolean propVisible(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        return action!=Action.DRINK||t<(popsicle(p)?48:lastBite(p))||replacement&&t>=end(duration)+41;
    }
    static Vector3f tip(DrinkProfile p) {return new Vector3f(SERVINGS.get(p.id()).tip()).mul(p.renderScale()/16);}
    static Transform seated(DrinkProfile p,Frame rig) {
        float unit=p.renderScale()/16;float[] rest=SERVINGS.get(p.id()).rest();
        return new Transform(rig.bottle().rotation().transform(new Vector3f((rest[0]-8)*unit-(popsicle(p)?.02f:0),(rest[1]-4.8f)*unit,(rest[2]-8)*unit+(popsicle(p)?.02f:0))).add(rig.bottle().position()),new Quaternionf(rig.bottle().rotation()));
    }
    private static Vector3f bezier(Vector3f a,Vector3f b,Vector3f c,Vector3f d,double u) {
        float v=(float)u,w=1-v;
        return new Vector3f(a).mul(w*w*w).fma(3*w*w*v,b).fma(3*w*v*v,c).fma(v*v*v,d);
    }
    private static Vector3f curve(Vector3f a,Vector3f b,Vector3f va,Vector3f vb,double start,double stop,double t) {
        double u=Math.max(0,Math.min(1,(t-start)/(stop-start)));float dt=(float)(stop-start);
        return bezier(a,new Vector3f(a).fma(dt/3,va),new Vector3f(b).fma(-dt/3,vb),b,u);
    }
    private static Transform heldProp(DrinkProfile p,double t,int side,Frame rig) {
        if(pineapple(p))return PineappleMotion.held(p,t,side,rig);
        boolean ice=popsicle(p);
        Transform rest=ice?PopsicleMotion.looseSeat(p,rig,side,smooth(t/GRASP_TICK)):seated(p,rig);float unit=p.renderScale()/16;
        // Lift the popsicle clear of the glass before turning it toward the mouth.
        double first=bites(p)[0],turn=smooth((t-(ice?CLEAR_TICK:5))/(first-(ice?CLEAR_TICK:5))),away=withdrawal(p,t);
        Vector3f clear=new Vector3f(rest.position()).add(-side*(ice?.18f:.11f),ice?.17f:.10f,.025f);
        // First turn the frozen end upward, then aim it at the lower camera edge.
        Quaternionf mouthRotation=ice?PopsicleMotion.upright(side)
            :new Quaternionf().rotationY(rad(90+side*62)).rotateZ(rad(-48)).rotateX(rad(-10));
        double chewing=chew(p,t),swallow=swallow(p,t);
        mouthRotation.rotateXYZ(rad((ice?-4:3)*away+.7*chewing),rad(side*(ice?3:-3)*away),rad(side*(-1.5*away+.4*chewing)));
        if(ice)PopsicleMotion.wrist(mouthRotation,t,side);
        if(ice)mouthRotation=new Quaternionf().rotationX(rad(68-40*Math.min(1,away))).mul(mouthRotation);
        double nibbled=0;for(int b:bites(p))nibbled+=smooth((t-b-.3)/3.8);
        Vector3f tip=new Vector3f(SERVINGS.get(p.id()).tip()).mul(unit*(float)(1-(ice?.21:.38)*nibbled));
        Vector3f lips=new Vector3f(-side*(ice?.21f:.065f),ice?-.15f:-.025f,ice?-.24f:-.49f);
        double beat=biteResponse(p,t);
        // The chocolate has a compact recovery. Ice additionally withdraws into view
        // between bites after its leading end has crossed the lower screen edge.
        double recovery=.85;
        lips.add((float)(-side*(ice?.052:.010)*away*recovery),(float)(-(ice?.048:.055)*away*recovery-.005*chewing-.005*swallow),
            (float)(-(ice?.024:.070)*away*recovery-.004*beat));
        if(ice)lips.add(PopsicleMotion.mouthOffset(t,side));
        if(ice)lips.add((float)(-side*.055*away),(float)(.14*away),(float)(-.30*away));
        Vector3f mouthGrip=new Vector3f(lips).sub(mouthRotation.transform(tip));
        if(ice&&t<=first)return PopsicleMotion.extract(p,t,side,rest,mouthGrip,mouthRotation);
        Vector3f tangent=new Vector3f(-side*.022f,ice?.033f:.018f,.030f),zero=new Vector3f();
        Vector3f position=t<=CLEAR_TICK?curve(rest.position(),clear,zero,tangent,GRASP_TICK,CLEAR_TICK,t)
            :curve(clear,mouthGrip,tangent,zero,CLEAR_TICK,first,t);
        Quaternionf rotation=new Quaternionf(rest.rotation()).slerp(mouthRotation,(float)turn).rotateX(rad(ice?0:.5*beat));
        if(ice&&t>lastBite(p)) {
            Transform last=heldProp(p,lastBite(p),side,rig);
            double transfer=smooth((t-lastBite(p))/(RELEASE_TICK-lastBite(p)));
            // Bring the last exposed end to the lips while the palm still owns
            // the stick. Do not turn it sideways after letting go.
            position=new Vector3f(last.position()).add((float)(side*.035*transfer),
                (float)(.018*transfer),(float)(.13*transfer));
            rotation=new Quaternionf(last.rotation()).rotateX(rad(16*transfer));
            if(t>=SPIT_TICK) {
                double seconds=(t-SPIT_TICK)/20.0*AnimationPlayback.raiseTicks(p)/48.0;
                // The impulse is forward and slightly down. Rotate around the
                // wood's center, so its base does not orbit an invisible hand.
                Vector3f center=new Vector3f(SERVINGS.get(p.id()).tip()).mul(unit*.35f);
                Vector3f worldCenter=new Quaternionf(rotation).transform(new Vector3f(center)).add(position);
                worldCenter.add((float)(side*.12*seconds),(float)(-.45*seconds-4.9*seconds*seconds),(float)(-3.2*seconds));
                rotation.rotateXYZ(rad(seconds*45),rad(side*seconds*10),rad(side*seconds*12));
                position=worldCenter.sub(new Quaternionf(rotation).transform(center));
            }
        }
        return new Transform(position,rotation);
    }
    /** Contact is on the retained edge, not the centre of the chocolate slab. */
    public static Vector3f gripPoint(DrinkProfile p) {
        if(popsicle(p)||pineapple(p))return new Vector3f();
        float margin=.25f*BottleRig.ARM_SCALE/p.renderScale()+.02f;
        return new Vector3f(6.069f+.55f*margin,-1-.83516465f*margin,2.4f).mul(p.renderScale()/16);
    }
    public static Vector3f palmContact(DrinkProfile p,int side) {
        return new Vector3f();
    }
    static Transform helperHand(DrinkProfile p,Transform prop,int side,boolean slim,double t) {
        if(pineapple(p))return PineappleMotion.helper(p,prop,side,t);
        if(popsicle(p))return PopsicleMotion.helper(p,prop,side,slim,t);
        double lift=smooth((t-GRASP_TICK)/(bites(p)[0]-GRASP_TICK)),away=withdrawal(p,t),beat=biteResponse(p,t);
        // Chocolate retains the lower-corner grip and compact elbow follow-through.
        Quaternionf arm=BottleRig.armRotation(new ArmMotion.Pose(24+11*lift-1.0*away+.6*chew(p,t)+.7*beat,
            -5+4*lift-.4*away,0),-side,false);
        Vector3f grip=prop.rotation().transform(gripPoint(p)).add(prop.position());
        Vector3f towardFood=prop.rotation().transform(new Vector3f(-.55f,.83516465f,0));
        Quaternionf face=new Quaternionf().rotationTo(arm.transform(new Vector3f(0,1,0)),towardFood).mul(arm);
        return new Transform(grip,face);
    }
    public static Frame rig(DrinkProfile p,Action action,double t,int duration,int side,boolean slim,Frame rig) {
        if(!owns(p))return rig;
        if(chocolate(p)&&action==Action.INSPECT)return ChocolateInspection.rig(p,t,side,slim,rig);
        if(popsicle(p)&&action==Action.INSPECT)return PopsicleMotion.inspectRig(p,t,side,slim,rig);
        Transform rest=seated(p,rig);
        if(action!=Action.DRINK||t>=48)return new Frame(rig.bottle(),rig.hand(),rig.helper(),rest,0);
        Transform prop=heldProp(p,t,side,rig);
        double assistance=assistance(p,action,t,duration);
        Transform helper=helperHand(p,prop,side,slim,t);
        Vector3f hidden=new Vector3f(-side*.65f,-1.0f,popsicle(p)?-.90f:-.70f);
        double reach=smooth(t/GRASP_TICK);
        Vector3f palm=bezier(hidden,new Vector3f(hidden).add(-side*.08f,.22f,popsicle(p)?.15f:-.05f),
            new Vector3f(helper.position()).add(-side*(popsicle(p)?.28f:.12f),popsicle(p)?.22f:-.09f,popsicle(p)?.65f:-.04f),helper.position(),reach);
        double releaseTime=popsicle(p)?RELEASE_TICK:lastBite(p),retreat=smooth((t-releaseTime)/7);
        if(retreat>0) {
            Transform release=helperHand(p,heldProp(p,releaseTime,side,rig),side,slim,releaseTime);
            palm=bezier(release.position(),new Vector3f(release.position()).add(-side*.055f,-.015f,-.085f),
                new Vector3f(hidden).add(side*.045f,.12f,-.04f),hidden,retreat);
            helper=new Transform(helper.position(),new Quaternionf(release.rotation()).slerp(
                BottleRig.armRotation(new ArmMotion.Pose(8,-12,0),-side,false),(float)retreat));
        }
        return new Frame(rig.bottle(),rig.hand(),new Transform(palm,helper.rotation()),prop,assistance);
    }
    /** Seated garnishes share a bounded spring; extracted food stays attached to the palm. */
    public static Frame sway(DrinkProfile p,Action action,double t,Frame rig,double x,double z) {
        if(!owns(p))return rig;
        double gate=action==Action.DRINK?1-smooth((t-1)/(GRASP_TICK-1)):1;
        if(popsicle(p)&&action==Action.INSPECT)gate=1-PopsicleMotion.inspectLock(t);
        if(chocolate(p)&&action==Action.INSPECT)gate=1-ChocolateInspection.lock(t);
        if(gate<=0)return rig;
        double strength=popsicle(p)?.13:.045;
        Quaternionf local=new Quaternionf().rotateX((float)(Math.tanh(x*2)*strength*gate)).rotateZ((float)(Math.tanh(z*2)*strength*gate));
        Vector3f pivot=popsicle(p)?new Vector3f(SERVINGS.get(p.id()).tip()).mul(p.renderScale()/16*.66f)
            :new Vector3f(1,-1,0).mul(p.renderScale()/16);
        Vector3f shift=new Vector3f(pivot).sub(local.transform(new Vector3f(pivot)));
        if(chocolate(p)&&action==Action.INSPECT) {
            // Keep the lowest chocolate corner resting on the rim as it rocks.
            float minimum=Float.POSITIVE_INFINITY,unit=p.renderScale()/16;
            for(float cx:new float[]{-4.544f,6.070f})for(float cy:new float[]{-1,1})for(float cz:new float[]{-3.149f,5.841f}) {
                Vector3f corner=local.transform(new Vector3f(cx,cy,cz).mul(unit).sub(pivot)).add(pivot);
                minimum=Math.min(minimum,corner.y);
            }
            shift.y+=Math.max(0,-unit-minimum);
        }
        Transform prop=rig.cork();
        return new Frame(rig.bottle(),rig.hand(),rig.helper(),new Transform(new Quaternionf(prop.rotation()).transform(shift).add(prop.position()),new Quaternionf(prop.rotation()).mul(local)),rig.assistance());
    }
    public static Camera camera(DrinkProfile p,Action a,double t,int duration) {
        if(owns(p)&&a==Action.INSPECT)return PopsicleMotion.inspectCamera(t);
        if(a!=Action.DRINK)return new Camera(0,0,0);
        Camera cup=pineapple(p)?CoconutAnimations.camera(a,t,duration):CupAnimations.camera(t,duration);
        double drinking=smooth((t-(pineapple(p)?25:39))/(pineapple(p)?23:9)),beat=biteResponse(p,t-.8),spit=popsicle(p)?pulse(t,SPIT_TICK-1,SPIT_TICK+.6,SPIT_TICK+4):0;
        double follow=withdrawal(p,t-.8),take=pulse(t,3,8,17);
        Camera carry=popsicle(p)?PopsicleMotion.carryCamera(t):new Camera(0,0,0);
        return new Camera(cup.pitch()*drinking+.19*beat+.18*spit-.10*follow-.12*take+.60*swallow(p,t-.35)+.13*chew(p,t-.3)+carry.pitch(),
            cup.yaw()*drinking+.045*beat-.08*follow+carry.yaw(),cup.roll()*drinking+.025*beat+.035*take+carry.roll());
    }
    public static List<AnimationSoundTimeline.Cue> sounds(DrinkProfile p,int duration) {
        var result=new ArrayList<AnimationSoundTimeline.Cue>(AnimationSoundTimeline.drinking(duration));
        int i=0;for(int bite:bites(p))result.add(new AnimationSoundTimeline.Cue(bite,popsicle(p)?"popsicle_bite":pineapple(p)?"pineapple_bite":"chocolate_bite",.72f-.05f*i,1.02f+.04f*i++));
        for(int bite:bites(p))result.add(new AnimationSoundTimeline.Cue(bite+3,"snack_swallow",.30f,1.2f));
        if(popsicle(p))result.add(new AnimationSoundTimeline.Cue(SPIT_TICK,"stick_spit",.38f,1.12f));
        result.sort(Comparator.comparingInt(AnimationSoundTimeline.Cue::tick));return result;
    }
    private ServingAnimations() {}
}
