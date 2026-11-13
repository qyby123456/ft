package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;

/** Container-specific gestures, sharing the proven pocket and replacement transitions. */
public final class DrinkMotion {
    private static final Pose REST=BottleMotion.IDLE;
    // Tea bottles retain wine inspection but use the complete water screw-cap performance.
    private static DrinkProfile motionProfile(DrinkProfile p,Action action) {
        return action==Action.DRINK&&BottleClosures.waterCap(p)?DrinkCatalog.find("farmingtales:purified_water"):p;
    }
    public static double idleBlend(DrinkProfile p,Action action,double age,int duration,boolean replacement) {
        if(ServingAnimations.owns(p)&&!ServingAnimations.pineapple(p)&&action==Action.INSPECT)
            return Math.min(1,1-BottleMotion.smooth(age/6)+BottleMotion.smooth((age-PopsicleMotion.INSPECT_TICKS+6)/6));
        if(p.jam()&&action==Action.DRINK)return BottleMotion.idleBlend(action,age+19,duration,replacement);
        if(action==Action.INSPECT&&ContainerAnimations.family(p)==ContainerAnimations.Family.WATER&&age>=72)
            return BottleMotion.smooth((age-72)/10);
        return BottleMotion.idleBlend(action,age,duration,replacement);
    }
    public static Pose blendRunning(DrinkProfile profile,Action action,Pose pose,double weight) {
        Pose target=BottleMotion.RUN;
        if(profile.cup())target=target.offset(0,.12,0,0,0);
        // Keep the completed toss on the same revolution when returning to a running hold.
        if(action==Action.INSPECT&&(ContainerAnimations.family(profile)==ContainerAnimations.Family.WATER||profile.jam())&&pose.roll()>180)
            target=target.offset(0,0,0,0,360);
        return pose.mix(target,weight);
    }
    public static Pose sample(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        profile=motionProfile(profile,action);
        if(action==Action.PLACE)return PlacementMotion.sample(profile,t,replacement);
        if(BambooEggAnimations.owns(profile))return BambooEggAnimations.sample(action,t,duration,replacement);
        if(StickyRiceCakeAnimations.owns(profile))return StickyRiceCakeAnimations.sample(action,t,duration,replacement);
        if(PlateFoodAnimations.owns(profile))return PlateFoodAnimations.sample(profile,action,t,duration,replacement);
        if(profile.jam())return JamAnimations.sample(action,t,duration,replacement);
        Pose pose=MotionDetail.gripResponse(profile,action,t,duration,sampleAuthored(profile,action,t,duration,replacement));
        if(profile.cup())pose=pose.offset(0,.12,0,0,0);
        if(profile.wine()&&action==Action.DRINK) {
            double sip= BottleMotion.smooth((t-35)/13)*(1-BottleMotion.smooth((t-48-BottleMotion.sipTicks(duration))/12));
            pose=pose.offset(0,-.075*sip,0,0,0);
        }
        if(action==Action.INSPECT&&profile.wine()&&profile.style()!=DrinkProfile.Style.BRANDY
            &&ContainerAnimations.family(profile)!=ContainerAnimations.Family.BRANDY) {
            double centered=BottleMotion.smooth((t-6)/16)*(1-BottleMotion.smooth((t-86)/18));
            float scale=profile.renderScale()/16;
            var center=new org.joml.Vector3f(((profile.boundsMin()[0]+profile.boundsMax()[0])*.5f-8)*scale,
                ((profile.boundsMin()[1]+profile.boundsMax()[1])*.5f-4.8f)*scale,
                ((profile.boundsMin()[2]+profile.boundsMax()[2])*.5f-8)*scale);
            BottleRig.rotation(pose,1).transform(center);
            pose=pose.offset((-center.x/.75-pose.x())*centered,0,0,0,0);
        }
        return pose;
    }
    private static Pose sampleAuthored(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        if(BambooEggAnimations.owns(profile))return BambooEggAnimations.sample(action,t,duration,replacement);
        if(StickyRiceCakeAnimations.owns(profile))return StickyRiceCakeAnimations.sample(action,t,duration,replacement);
        if(PlateFoodAnimations.owns(profile))return PlateFoodAnimations.sample(profile,action,t,duration,replacement);
        if(ServingAnimations.owns(profile))return ServingAnimations.sample(profile,action,t,duration,replacement);
        if(profile.jam())return JamAnimations.sample(action,t,duration,replacement);
        if(LandNo1BottleAnimations.owns(profile)&&action==Action.INSPECT)return LandNo1BottleAnimations.inspect(t);
        if(profile.cup())return CupAnimations.sample(profile,action,t,duration,replacement);
        if(ContainerAnimations.owns(profile))return ContainerAnimations.sample(profile,action,t,duration,replacement);
        Pose pose=sampleMotion(profile,action,t,duration,replacement);
        if(profile.style()==DrinkProfile.Style.BRANDY) {
            double sipping=action==Action.DRINK?BottleMotion.smooth((t-35)/13)*(1-BottleMotion.smooth((t-48-BottleMotion.sipTicks(duration))/12)):0;
            pose=pose.offset(0,.12*(1-sipping),0,0,0);
        }
        return pose;
    }
    private static Pose sampleMotion(DrinkProfile profile,Action action,double t,int duration,boolean replacement) {
        if(profile.pullCap()||(profile.wine()&&action==Action.INSPECT)||action==Action.IDLE||action==Action.DRAW||action==Action.STOW)
            return BottleMotion.sample(action,t,duration,replacement);
        if(action==Action.INSPECT)return inspect(profile,t);
        double end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);
        Pose raised=raised(profile,false),empty=raised(profile,true);
        if(t<=48) {
            Pose grip=new Pose(.29,-.64,-1.06,-4,155,profile.can()?3:-3,.8);
            Pose opened=grip.offset(profile.can()?.008:.014,profile.can()?-.012:0,0,profile.can()?-2:0,0);
            Pose p=track(t,new double[]{0,6,12,18,26,30,35,48},
                new Pose[]{REST,REST.offset(-.015,.012,0,0,0),grip,grip,opened,opened,grip.offset(0,.03,0,6,0),raised});
            if(profile.can()) {
                double click=BottleMotion.gesturePulse(t,22,6);
                return p.offset(.002*click,-.010*click,0,-1.5*click,0);
            }
            double twist=twist(t),effort=BottleMotion.gesturePulse(t,16,8)+BottleMotion.gesturePulse(t,25,7);
            p=p.offset(.003*effort,.003*effort,0,0,-.35*effort);
            if(profile.style()==DrinkProfile.Style.JUICE) {
                double mix=BottleMotion.gesturePulse(t,3,9);
                p=p.offset(0,.018*Math.sin(t*1.25)*mix,0,2*Math.sin(t*1.25)*mix,0);
            }
            return new Pose(p.x(),p.y(),p.z(),p.pitch(),p.yaw()-twist*.018,p.roll(),p.fill());
        }
        if(t<end) {
            double fraction=BottleMotion.ease((t-48)/BottleMotion.sipTicks(duration));
            Pose p=raised.mix(empty,fraction);
            double breath=.8*Math.sin((t-48)*Math.PI/10)*Math.pow(Math.sin(Math.PI*fraction),2);
            return p.offset(0,.002*breath,0,.45*breath,0);
        }
        return tail(profile,t,end,empty,replacement);
    }
    private static Pose raised(DrinkProfile p,boolean empty) {
        double x=p.can()?.16:.12,y=p.can()?-.32:-.36,z=p.can()?-.98:-.97;
        double pitch=p.can()?67:p.style()==DrinkProfile.Style.WATER?73:p.style()==DrinkProfile.Style.JUICE?70:75;
        if(p.style()==DrinkProfile.Style.PINK_CAN){x+=.012;pitch-=3;}
        return new Pose(x-(empty?.015:0),y+(empty?.055:0),z+(empty?.04:0),pitch+(empty?21:0),161,p.can()?2:3,empty?.025:.8);
    }
    private static Pose inspect(DrinkProfile p,double t) {
        boolean can=p.can();
        Pose label=new Pose(.39,-.55,-1.04,can?-10:-5,155,can?-5:-8,.8);
        Pose side=new Pose(.40,-.555,-1.035,can?15:2,can?218:235,can?-7:-10,.8);
        Pose lid=new Pose(.405,-.56,-1.04,can?-20:-8,can?190:185,can?-3:-6,.8);
        if(p.style()==DrinkProfile.Style.SPORTS)side=side.offset(.015,-.015,0,4,2);
        if(p.style()==DrinkProfile.Style.PINK_CAN)lid=lid.offset(-.01,.005,0,3,1);
        Pose pose=track(t,new double[]{0,8,20,34,46,61,75,86,104},
            new Pose[]{REST,REST.offset(.008,-.006,0,0,1),label,label,side,side,lid,label,REST});
        if(p.style()==DrinkProfile.Style.JUICE) {
            double swirl=BottleMotion.gesturePulse(t,36,30);
            pose=pose.offset(.006*Math.sin(t*.25)*swirl,.004*Math.cos(t*.25)*swirl,0,1.4*Math.sin(t*.25)*swirl,0);
        }
        return pose;
    }
    private static Pose tail(DrinkProfile profile,double t,double end,Pose empty,boolean replacement) {
        double age=t-end;
        Pose lowered=new Pose(.30,-.66,-1.02,-5,155,12,.025);
        Pose hidden=new Pose(.58,-1.76,-.88,10,145,14,.025);
        if(age<=12)return empty.mix(lowered,BottleMotion.ease(age/12));
        if(age<=BottleMotion.LOWER_TICKS)return lowered.mix(hidden,BottleMotion.ease((age-12)/(BottleMotion.LOWER_TICKS-12)));
        if(age<=BottleMotion.LOWER_TICKS+7+6)return hidden;
        Pose hiddenFull=hidden.wine(replacement?.8:.025);
        if(age<=BottleMotion.LOWER_TICKS+7+6+4)return hiddenFull;
        if(!replacement)return hiddenFull;
        double draw=age-(BottleMotion.LOWER_TICKS+7+6+4);
        Pose settle=REST.offset(.018,.035,-.01,1.2,-.7);
        if(draw<=11)return hiddenFull.mix(settle,BottleMotion.ease(draw/11));
        return settle.mix(REST,BottleMotion.ease((draw-11)/(BottleMotion.REPLACEMENT_DRAW_TICKS-11)));
    }
    public static double assistance(DrinkProfile p,Action action,double t) {
        return assistance(p,action,t,16);
    }
    public static double assistance(DrinkProfile p,Action action,double t,int duration) {
        p=motionProfile(p,action);
        if(BambooEggAnimations.owns(p))return BambooEggAnimations.assistance(action,t,duration);
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.assistance(action,t,duration);
        if(PlateFoodAnimations.owns(p))return PlateFoodAnimations.assistance(action,t,duration);
        if(CoconutAnimations.owns(p)&&action==Action.INSPECT)
            return CoconutAnimations.support(t);
        if(action==Action.INSPECT&&CupAnimations.coffeePreview(p))return CupAnimations.coffeeSupport(t);
        if(ServingAnimations.owns(p))return ServingAnimations.assistance(p,action,t,duration);
        if(p.jam())return JamAnimations.assistance(action,t,duration);
        if(LandNo1BottleAnimations.owns(p))return LandNo1BottleAnimations.assistance(action,t,duration);
        if(action==Action.PLACE)return 0;
        if(ContainerAnimations.owns(p))return ContainerAnimations.assistance(p,action,t,duration);
        if(p.pullCap()||(p.wine()&&action==Action.INSPECT))return BottleMotion.assistance(action,t);
        if(action==Action.INSPECT)return BottleMotion.ease((t-10)/10)*(1-BottleMotion.ease((t-85)/13));
        if(action==Action.DRINK)return BottleMotion.ease((t-6)/8)*(1-BottleMotion.ease((t-(p.can()?35:32))/(p.can()?9:10)));
        return 0;
    }
    public static ArmMotion.Pose arm(DrinkProfile p,Action action,double t,int duration,boolean helper) {
        p=motionProfile(p,action);
        if(action==Action.PLACE)return helper?ArmMotion.REST:PlacementMotion.arm(p,t,true);
        if(BambooEggAnimations.owns(p))return BambooEggAnimations.arm(action,t,duration,helper);
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.arm(action,t,duration,helper);
        if(PlateFoodAnimations.owns(p))return PlateFoodAnimations.arm(action,t,duration,helper);
        if(LandNo1BottleAnimations.owns(p)&&(action==Action.INSPECT||action==Action.DRINK))
            return LandNo1BottleAnimations.arm(action,t,duration,helper);
        if(ContainerAnimations.owns(p))return ContainerAnimations.arm(p,action,t,duration,helper);
        if(p.pullCap()||(p.wine()&&action==Action.INSPECT)||action==Action.IDLE||action==Action.DRAW||action==Action.STOW)return ArmMotion.sample(action,t,duration,helper);
        if(action==Action.INSPECT) {
            double a=assistance(p,action,t);
            return helper?new ArmMotion.Pose(10*a,-4*a,0):new ArmMotion.Pose(20*a,2*a,0);
        }
        if(!helper)return ArmMotion.sample(action,t,duration,false);
        double a=assistance(p,action,t);
        double turn=p.can()?0:18*(BottleMotion.gesturePulse(t,15,9)+BottleMotion.gesturePulse(t,24,8));
        return new ArmMotion.Pose((p.can()?12:16)*a,-5*a,turn);
    }
    public static double twist(double t) {return 85*BottleMotion.smooth((t-15)/8)+90*BottleMotion.smooth((t-24)/8);}
    public static double capLift(DrinkProfile p,double t) {
        if(BambooEggAnimations.owns(p)||StickyRiceCakeAnimations.owns(p)||PlateFoodAnimations.owns(p)||p.cup())return 0;
        if(p.style()==DrinkProfile.Style.BEER)return 0;
        if(p.pullCap())return BottleMotion.corkLift(t);
        if(p.can())return 0;
        return .018*BottleMotion.smooth((t-15)/17)+.17*BottleMotion.smooth((t-30)/7);
    }
    public static boolean capDetached(DrinkProfile p,Action action,double t) {
        if(BambooEggAnimations.owns(p))return true;
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.detached(action,t);
        if(PlateFoodAnimations.owns(p))return PlateFoodAnimations.detached(action,t);
        if(p.jam())return JamAnimations.detached(action,t);
        if(p.cup())return false;
        if(p.style()==DrinkProfile.Style.BEER)return action==Action.DRINK&&t>=27&&t<47;
        if(p.can())return action==Action.DRINK&&t>28&&t<47;
        return action==Action.DRINK&&t>(p.pullCap()?16:15)&&t<(p.pullCap()?41:42);
    }
    public static double tabAngle(Action action,double t,int duration) {
        if(action!=Action.DRINK||!BottleMotion.mouthOpen(action,t,duration,true))return 0;
        return 58*BottleMotion.smooth((t-22)/6);
    }
    public static double canOpening(Action action,double t,int duration,boolean replacement) {
        if(action!=Action.DRINK||!BottleMotion.mouthOpen(action,t,duration,replacement))return 0;
        return BottleMotion.smooth((t-23)/5);
    }
    public static boolean capVisible(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(BambooEggAnimations.owns(p))return BambooEggAnimations.spoonVisible(action,t,duration,replacement);
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.propVisible(action,t,duration,replacement);
        if(PlateFoodAnimations.owns(p))return PlateFoodAnimations.propVisible(action,t,duration,replacement);
        if(p.jam())return JamAnimations.visible(action,t,duration,replacement);
        if(p.cup())return false;
        if(p.can()||p.style()==DrinkProfile.Style.BEER)return action!=Action.DRINK||t<47||!BottleMotion.mouthOpen(action,t,duration,replacement);
        return BottleMotion.corkVisible(action,t,duration,replacement)||(action==Action.DRINK&&!p.pullCap()&&t<42);
    }
    public static double neckReveal(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        if(BambooEggAnimations.owns(p))return 0;
        if(StickyRiceCakeAnimations.owns(p))return 0;
        if(PlateFoodAnimations.owns(p))return 0;
        // As with screw-cap drinks, the neck is fixed geometry beneath the closure.
        // An inset prevents coplanar flicker; lifting the opaque cap naturally uncovers it.
        return BottleClosures.waterCap(p)||p.pullCap()&&!BottleClosures.fixedBottle(p)?1:0;
    }
    public static Camera camera(DrinkProfile p,Action action,double t,int duration) {
        return camera(p,action,t,duration,0);
    }
    public static Camera camera(DrinkProfile p,Action action,double t,int duration,int variant) {
        p=motionProfile(p,action);
        Camera base=baseCamera(p,action,t,duration),detail=EatingMotion.camera(p,action,t,duration,variant);
        return new Camera(base.pitch()+detail.pitch(),base.yaw()+detail.yaw(),base.roll()+detail.roll());
    }
    private static Camera baseCamera(DrinkProfile p,Action action,double t,int duration) {
        if(action==Action.PLACE)return PlacementMotion.camera(t);
        if(p.jam())return JamAnimations.camera(action,t,duration);
        if(action==Action.INSPECT&&ContainerAnimations.family(p)==ContainerAnimations.Family.WATER&&t>=72)return new Camera(0,0,0);
        Camera base=authoredCamera(p,action,t,duration);
        if(action==Action.IDLE)return base;
        double end=action==Action.DRAW?19:action==Action.STOW?20:action==Action.INSPECT?104:BottleMotion.totalDrinkTicks(duration);
        double envelope=BottleMotion.smooth(t/2)*(1-BottleMotion.smooth((t-end+3)/3));
        if(action==Action.INSPECT&&ContainerAnimations.family(p)==ContainerAnimations.Family.WATER)
            envelope*=1-BottleMotion.smooth((t-65)/7);
        // The eyes follow after the hands start; a short causal filter rounds direction changes.
        Camera behind=authoredCamera(p,action,Math.max(0,t-.9),duration);
        Camera trailing=authoredCamera(p,action,Math.max(0,t-2.1),duration);
        base=new Camera(.45*base.pitch()+.35*behind.pitch()+.20*trailing.pitch(),
            .45*base.yaw()+.35*behind.yaw()+.20*trailing.yaw(),
            .45*base.roll()+.35*behind.roll()+.20*trailing.roll());
        Pose before=sample(p,action,Math.max(0,t-2),duration,true),after=sample(p,action,Math.max(0,t-1),duration,true);
        double pitch=.28*Math.tanh((after.y()-before.y())*18);
        double yaw=.14*Math.tanh((after.x()-before.x())*16);
        double roll=.10*Math.tanh((after.roll()-before.roll())*.16);
        double impact=0;
        if(action==Action.INSPECT) {
            var family=ContainerAnimations.family(p);
            if(family==ContainerAnimations.Family.WATER)impact=.30*MotionDetail.settle(t,48.8,10);
            else if(family==ContainerAnimations.Family.RUM||family==ContainerAnimations.Family.BEER||!ContainerAnimations.owns(p))
                for(int i=0;i<4;i++)impact+=.20*MotionDetail.settle(t,(family==ContainerAnimations.Family.RUM?30:29)+16*i+.8,8);
        }
        // The final envelope ends in the identical idle camera, even when the filter still has history.
        double contact=MotionDetail.gripCamera(p,action,t);
        double calm=action==Action.INSPECT?.10:1,level=action==Action.INSPECT?.04:1;
        return new Camera((base.pitch()+pitch+impact+contact)*envelope*calm,(base.yaw()*.72+yaw)*envelope*calm,(base.roll()*.72+roll+.12*contact)*envelope*level);
    }
    public static double movementCameraStrength(Action action) {
        return action==Action.INSPECT?.04:action==Action.IDLE?1:.4;
    }
    private static Camera authoredCamera(DrinkProfile p,Action action,double t,int duration) {
        if(BambooEggAnimations.owns(p))return BambooEggAnimations.camera(action,t,duration);
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.camera(action,t,duration);
        if(PlateFoodAnimations.owns(p))return PlateFoodAnimations.camera(p,action,t,duration);
        if(CoconutAnimations.owns(p)&&action==Action.INSPECT)return CoconutAnimations.camera(action,t,duration);
        if(LandNo1BottleAnimations.owns(p)&&action==Action.INSPECT)return LandNo1BottleAnimations.camera(t);
        if(ServingAnimations.owns(p)&&(action==Action.DRINK||action==Action.INSPECT))return ServingAnimations.camera(p,action,t,duration);
        if(p.jam())return JamAnimations.camera(action,t,duration);
        if(p.cup()&&action==Action.DRINK)return CupAnimations.camera(t,duration);
        if(action==Action.INSPECT&&CupAnimations.coffeePreview(p))
            return CupAnimations.coffeeInspectCamera(t);
        if(ContainerAnimations.owns(p))return ContainerAnimations.camera(p,action,t,duration);
        if(p.pullCap()||(p.wine()&&action==Action.INSPECT)||action==Action.DRAW||action==Action.STOW)return BottleMotion.camera(action,t,duration);
        double a=action==Action.INSPECT?BottleMotion.ease(t/18)*(1-BottleMotion.ease((t-83)/21)):0;
        if(action==Action.INSPECT)return new Camera(.65*a,.18*Math.sin(t*.065)*a,.12*Math.sin(t*.09)*a);
        if(action!=Action.DRINK)return new Camera(0,0,0);
        double end=48+BottleMotion.sipTicks(duration);
        double raise=BottleMotion.ease((t-34)/14)*(1-BottleMotion.ease((t-end)/14));
        double release=p.can()?BottleMotion.gesturePulse(t,22,7):BottleMotion.gesturePulse(t,30,8);
        return new Camera(.75*raise+.16*release,-.10*raise,.04*release);
    }
    private static Pose track(double t,double[] times,Pose[] poses) {
        if(t<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(t<times[i])return poses[i-1].mix(poses[i],BottleMotion.smooth((t-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
    private DrinkMotion() {}
}

