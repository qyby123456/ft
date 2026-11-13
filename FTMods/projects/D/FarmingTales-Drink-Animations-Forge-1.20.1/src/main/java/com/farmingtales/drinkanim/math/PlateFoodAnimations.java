package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;

/**
 * Shareable plates such as Senluo Cookery's rice-cake plate.
 *
 * The plate is a placeable block, so the hotkey must never consume or place it: this
 * family only performs, and the world owns the portions (see {@link PresentationOnly}).
 * The source mod already ships one model per remaining portion, so the bite stages are
 * a straight lookup instead of authored geometry, and the whole family is driven by
 * `plates.json`: a new plate needs that one entry plus its generated stage models.
 */
public final class PlateFoodAnimations {
    /** `id -> stage count`, where stage 0 is the empty plate and the last stage is full. */
    private static final Map<String,Integer> PLATES=load();
    private static final String FOLDER="animation/plates/";
    private static final int DRAW_START_TAIL=41;
    private static final Pose REST=BottleMotion.IDLE;
    /** Positive pitch tips the food side toward the player, which is the whole point of a plate. */
    private static final Pose DISPLAY=new Pose(.28,-.47,-1.03,6,164,-6,.8);
    private static final Pose EAT_HOLD=new Pose(.23,-.35,-.98,34,168,-3,.8);

    private static Map<String,Integer> load() {
        try(var in=PlateFoodAnimations.class.getResourceAsStream("/assets/farmingtales_drink_anim/plates.json")) {
            if(in==null)return Map.of();
            return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),
                new TypeToken<LinkedHashMap<String,Integer>>(){}.getType());
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static boolean owns(DrinkProfile profile) {return profile!=null&&PLATES.containsKey(profile.id());}
    private static int stages(DrinkProfile profile) {return PLATES.get(profile.id());}
    /** Portions on a full plate, which is exactly the number of bites. */
    public static int portions(DrinkProfile profile) {return stages(profile)-1;}
    private static String path(DrinkProfile profile,int level) {return FOLDER+profile.id().split(":")[1]+"/level"+level;}
    /** Every stage model of this plate, ready for `ModelEvent.RegisterAdditional`. */
    public static String[] models(DrinkProfile profile) {
        String[] result=new String[stages(profile)];
        for(int level=0;level<result.length;level++)result[level]=path(profile,level);
        return result;
    }

    /** Mouthfuls sit inside the unchanged presentation clock so hosts and sounds never drift. */
    public static double[] bites(DrinkProfile profile,int duration) {
        int count=portions(profile),span=BottleMotion.sipTicks(duration)-10;
        double[] ticks=new double[count];
        for(int i=0;i<count;i++)ticks[i]=BottleMotion.RAISE_TICKS+10+i*span/(double)count;
        return ticks;
    }
    private static double sipEnd(int duration) {return BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);}

    /** How many portions the mouth has already taken, and so which stage is on the plate. */
    private static int level(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        int portions=portions(profile);
        if(action!=Action.DRINK)return portions;
        // The replacement is a fresh plate, so the pocket hides the empty one and the redraw resets it.
        if(replacement&&t>sipEnd(duration)+DRAW_START_TAIL)return portions;
        int eaten=0;
        for(double bite:bites(profile,duration))if(t>=bite+1)eaten++;
        return Math.max(0,portions-Math.min(portions,eaten));
    }
    public static String bodyModel(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        return path(profile,level(profile,action,t,duration,replacement));
    }

    public static Pose sample(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        if(action==Action.IDLE)return REST;
        if(action==Action.DRAW||action==Action.STOW||action==Action.PLACE)
            return BottleMotion.sample(action,t,duration,replacement);
        if(action==Action.INSPECT)return inspect(t);
        return eat(profile,t,duration,replacement);
    }

    /** Held level so the food on top faces the player, tipped twice to read the rim. */
    private static Pose inspect(double t) {
        Pose tipped=new Pose(.27,-.45,-1.02,30,190,-9,.8);
        Pose turned=new Pose(.29,-.49,-1.06,18,248,-7,.8);
        Pose pose=track(t,new double[]{0,8,20,34,48,62,76,90,104},
            new Pose[]{REST,REST.offset(.01,-.01,0,-1,-1),DISPLAY,DISPLAY,tipped,turned,
                new Pose(.30,-.52,-1.05,10,206,-5,.8),new Pose(.42,-.62,-1.07,4,168,-2,.8),REST});
        double effort=window(t,18,30,76,90);
        return pose.offset(.003*Math.sin(t*.29)*effort,.004*Math.sin(t*.41+1.2)*effort,0,
            .8*Math.sin(t*.25)*effort,0);
    }

    /** Tipped toward the mouth once per portion, then the empty plate drops back to the pocket. */
    private static Pose eat(DrinkProfile profile,double t,int duration,boolean replacement) {
        double end=sipEnd(duration);
        if(t<=BottleMotion.RAISE_TICKS) {
            return track(t,new double[]{0,10,22,34,BottleMotion.RAISE_TICKS},
                new Pose[]{REST,REST.offset(-.015,.012,0,-2,-1),
                    new Pose(.40,-.58,-1.08,4,160,-7,.8),
                    new Pose(.30,-.46,-1.02,14,165,-5,.8),EAT_HOLD});
        }
        if(t<end) {
            double[] bites=bites(profile,duration);
            Pose pose=EAT_HOLD;
            double fill=.8,share=(.8-.025)/bites.length;
            for(int i=0;i<bites.length;i++) {
                double bite=bitePulse(t,bites[i])*(1-.10*i);
                // Every mouthful tips the plate a little further and pulls it to the lips.
                pose=pose.offset(-.004*bite,-.015*bite,.013*bite,7.5*bite,0);
                fill-=share*smooth((t-bites[i]-1)/2.0);
            }
            double fraction=BottleMotion.ease((t-BottleMotion.RAISE_TICKS)/BottleMotion.sipTicks(duration));
            double breath=Math.sin((t-BottleMotion.RAISE_TICKS)*Math.PI/11)*Math.pow(Math.sin(Math.PI*fraction),2);
            return pose.offset(0,.004*breath,0,.5*breath,0).wine(Math.max(.025,fill));
        }
        double age=t-end;
        Pose empty=EAT_HOLD.wine(.025),lowered=new Pose(.32,-.64,-1.03,12,158,9,.025);
        Pose hidden=new Pose(.58,-1.76,-.88,10,145,14,.025);
        if(age<=12)return empty.mix(lowered,smooth(age/12));
        if(age<=24)return lowered.mix(hidden,smooth((age-12)/12));
        if(age<=DRAW_START_TAIL||!replacement)return hidden;
        Pose hiddenFull=hidden.wine(.8),settle=REST.offset(.018,.035,-.01,1.2,-.7);
        double draw=age-DRAW_START_TAIL;
        if(draw<=11)return hiddenFull.mix(settle,smooth(draw/11));
        return settle.mix(REST,smooth((draw-11)/(BottleMotion.REPLACEMENT_DRAW_TICKS-11)));
    }

    /** One quick tip toward the lips, a short hold and a damped return. */
    private static double bitePulse(double t,double bite) {return window(t,bite-.8,bite+.3,bite+1.9,bite+3.9);}

    public static double assistance(Action action,double t,int duration) {
        // The plate is steadied by the far hand while it is turned over for a look.
        if(action==Action.INSPECT)return window(t,8,18,86,100);
        return 0;
    }

    public static ArmMotion.Pose arm(Action action,double t,int duration,boolean helper) {
        if(action==Action.IDLE)return ArmMotion.REST;
        if(!helper) {
            double lift=action==Action.INSPECT?20*window(t,6,24,84,103)
                :action==Action.DRINK?18*window(t,8,40,sipEnd(duration),sipEnd(duration)+14):0;
            double sweep=action==Action.INSPECT?3*window(t,18,34,74,94):-2*window(t,16,36,84,100);
            return new ArmMotion.Pose(lift,sweep,0);
        }
        double a=assistance(action,t,duration);
        return new ArmMotion.Pose(16*a,-7*a,4*a);
    }

    public static BottleRig.Frame rig(DrinkProfile profile,Pose motion,Action action,double t,int duration,int side,boolean slim) {
        float scale=profile.renderScale(),unit=scale/16;
        Quaternionf cup=BottleRig.rotation(motion,side);
        Vector3f pivot=new Vector3f((float)(side*motion.x()*.75),(float)(motion.y()*.75+.30*scale),(float)(motion.z()*.75));

        // As in the shared rig, an axial inspection turn happens between the hands: the grip
        // keeps the idle yaw so the forearm never orbits the plate or swings into the frustum.
        Quaternionf grip=action==Action.INSPECT
            ?BottleRig.rotation(new Pose(motion.x(),motion.y(),motion.z(),motion.pitch(),REST.yaw(),motion.roll(),motion.fill()),side)
            :cup;
        // The palm cups the near rim, one plate thickness below the food line.
        Vector3f palmLocal=new Vector3f(-side*1.7f*unit,-3.05f*unit,6.6f*unit);
        Quaternionf carry=new Quaternionf(grip).mul(new Quaternionf(BottleRig.rotation(REST,side)).conjugate())
            .mul(BottleRig.armRotation(arm(action,t,duration,false),side,false));
        BottleRig.Transform hand=new BottleRig.Transform(grip.transform(palmLocal).add(pivot),carry);

        double help=assistance(action,t,duration);
        // The far palm takes the opposite rim so a wide plate cannot tip out of one hand.
        Vector3f support=grip.transform(new Vector3f(side*3.4f*unit,-3.4f*unit,-3.6f*unit)).add(pivot);
        Vector3f hidden=new Vector3f(-side*.75f,-.83f,-.90f);
        Vector3f helper=new Vector3f(hidden).lerp(support,(float)help);
        Quaternionf helperRotation=BottleRig.armRotation(arm(action,t,duration,true),-side,true);
        return new BottleRig.Frame(new BottleRig.Transform(pivot,cup),hand,
            new BottleRig.Transform(helper,helperRotation),new BottleRig.Transform(pivot,cup),help);
    }

    /** A plate owns no closure, so nothing is ever seated on or lifted off it. */
    public static boolean propVisible(Action action,double t,int duration,boolean replacement) {return false;}
    public static boolean detached(Action action,double t) {return false;}

    public static Camera camera(DrinkProfile profile,Action action,double t,int duration) {
        if(action==Action.INSPECT) {
            double a=window(t,4,20,84,103);
            return new Camera(.38*a+.10*window(t,34,46,62,74),-.18*window(t,34,46,72,84),.05*Math.sin(t*.07)*a);
        }
        if(action==Action.DRINK) {
            double end=sipEnd(duration),a=window(t,10,38,end,end+14),beat=0;
            for(double bite:bites(profile,duration))beat+=.14*window(t,bite-1.0,bite+.4,bite+2.1,bite+4.1);
            return new Camera(.24*a+beat,-.06*a,.02*Math.sin(t*.1)*a);
        }
        return new Camera(0,0,0);
    }

    public static List<AnimationSoundTimeline.Cue> inspectionSounds() {return List.of(
        new AnimationSoundTimeline.Cue(7,"inspect_lift",.34f,1.02f),
        new AnimationSoundTimeline.Cue(20,"inspect_support",.22f,1.05f),
        new AnimationSoundTimeline.Cue(34,"inspect_turn",.18f,1.13f),
        new AnimationSoundTimeline.Cue(62,"inspect_turn",.15f,1.18f),
        new AnimationSoundTimeline.Cue(76,"inspect_settle",.10f,1.12f),
        new AnimationSoundTimeline.Cue(94,"inspect_return",.28f,.99f));}

    public static List<AnimationSoundTimeline.Cue> eatingSounds(DrinkProfile profile,int duration) {
        var result=new ArrayList<AnimationSoundTimeline.Cue>();
        int i=0;
        for(double bite:bites(profile,duration)) {
            int tick=(int)Math.round(bite);
            result.add(new AnimationSoundTimeline.Cue(tick,biteSound(profile),.60f-.04f*i,1.03f+.03f*i++));
            result.add(new AnimationSoundTimeline.Cue(tick+2,"snack_swallow",.26f,1.18f));
        }
        return result;
    }

    private static String biteSound(DrinkProfile profile) {
        return "kaleidoscope_cookery:dark_cuisine".equals(profile.id())
            ? "dark_cuisine_bite" : "sticky_rice_cake_bite";
    }

    /** Every portion is a mouthful on the unchanged 40 tick presentation clock. */
    public static double gestureImpulse(DrinkProfile profile,Action action,int tick) {
        if(action==Action.DRINK)for(double bite:bites(profile,16))if(tick==(int)bite)return .020;
        if(action==Action.INSPECT&&(tick==34||tick==62))return .026;
        return 0;
    }

    private static Pose track(double t,double[] times,Pose[] poses) {
        if(t<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(t<times[i])return poses[i-1].mix(poses[i],smooth((t-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
    private static double window(double t,double start,double in,double out,double end) {return smooth((t-start)/(in-start))*(1-smooth((t-out)/(end-out)));}
    private static double smooth(double u) {u=Math.max(0,Math.min(1,u));return u*u*u*(u*(u*6-15)+10);}
    private PlateFoodAnimations() {}
}
