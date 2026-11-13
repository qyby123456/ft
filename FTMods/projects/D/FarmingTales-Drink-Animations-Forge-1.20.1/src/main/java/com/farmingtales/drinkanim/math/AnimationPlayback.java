package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Action;

/** One playback clock for the renderer, events and editable export. Authored poses stay in ticks. */
public final class AnimationPlayback {
    public static final int DRAW_TICKS=10, STOW_TICKS=9, RAISE_TICKS=38;
    private static final double WATER_RATE=104.0/76, TOSS_SPEED=1.4;
    private static final double WATER_CRUISE_END=24+(65-25.2)/TOSS_SPEED;
    public static int ticks(DrinkProfile profile,Action action,int useTicks) {
        return switch(action) {
            case DRAW -> DRAW_TICKS;
            case STOW -> STOW_TICKS;
            case PLACE -> PlacementMotion.TICKS;
            case INSPECT -> (int)Math.ceil(inspectionDuration(profile));
            case DRINK -> sipEnd(profile,useTicks)+(profile.jam()?22+JamAnimations.DRAW_TICKS:44);
            default -> 20;
        };
    }
    public static double authored(DrinkProfile profile,Action action,double elapsed,int useTicks) {
        elapsed=Math.max(0,elapsed);
        if(action==Action.DRAW)return elapsed*BottleMotion.DRAW_TICKS/DRAW_TICKS;
        if(action==Action.STOW)return elapsed*BottleMotion.STOW_TICKS/STOW_TICKS;
        if(action==Action.INSPECT)return inspectionTime(profile,elapsed);
        if(action==Action.DRINK) {
            int raise=raiseTicks(profile);
            if(elapsed<=raise) {
                if(ServingAnimations.owns(profile))return elapsed*BottleMotion.RAISE_TICKS/raise;
                // Cups have no opening. Bottles keep the opening clock, then halve
                // the final ten-tick lift, sharing this clock with hands and audio.
                double clock=raise==33&&elapsed>28?28+(elapsed-28)*2:raise==19?elapsed*2:elapsed;
                return clock*BottleMotion.RAISE_TICKS/RAISE_TICKS;
            }
            double end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(useTicks),tail=elapsed-sipEnd(profile,useTicks);
            if(profile.jam())return end+(tail<=22?tail:22+(tail-22)*BottleMotion.REPLACEMENT_DRAW_TICKS/JamAnimations.DRAW_TICKS);
            if(tail<=12)return end+tail;
            // Preserve every use tick and gulp. Speed up only empty-bottle stow and replacement.
            if(tail>32)return end+41+(tail-32)*BottleMotion.REPLACEMENT_DRAW_TICKS/(profile.jam()?22:12);
            if(tail>12)return end+12+(tail-12)*29/20;
        }
        return elapsed;
    }
    public static int raiseTicks(DrinkProfile profile) {
        if(ServingAnimations.owns(profile))return ServingAnimations.popsicle(profile)?120:ServingAnimations.pineapple(profile)?56:62;
        return profile.cup()?19:profile.cork()||profile.style()==DrinkProfile.Style.JUICE?33:RAISE_TICKS;
    }
    public static int sipEnd(DrinkProfile profile,int useTicks) {
        return raiseTicks(profile)+BottleMotion.sipTicks(useTicks);
    }
    private static double inspectionDuration(DrinkProfile p) {
        if(BambooEggAnimations.owns(p))return BambooEggAnimations.INSPECT_TICKS;
        if(StickyRiceCakeAnimations.owns(p))return StickyRiceCakeAnimations.INSPECT_TICKS;
        if(PlateFoodAnimations.owns(p))return BottleMotion.INSPECT_TICKS;
        if(p.jam())return JamAnimations.INSPECT_TICKS;
        if(ServingAnimations.owns(p)&&!ServingAnimations.pineapple(p))return PopsicleMotion.INSPECT_TICKS*1.5;
        return p.id().equals("farmingtales:purified_water")?(82-72.2+WATER_CRUISE_END+6)/WATER_RATE:BottleMotion.INSPECT_TICKS;
    }
    private static double inspectionTime(DrinkProfile p,double elapsed) {
        if(BambooEggAnimations.owns(p))return Math.min(BambooEggAnimations.INSPECT_TICKS,elapsed);
        if(StickyRiceCakeAnimations.owns(p))return Math.min(StickyRiceCakeAnimations.INSPECT_TICKS,elapsed);
        if(PlateFoodAnimations.owns(p))return Math.min(BottleMotion.INSPECT_TICKS,elapsed);
        if(p.jam())return Math.min(JamAnimations.INSPECT_TICKS,elapsed);
        // The cup, palm, ice and sound cues share the same slower presentation clock.
        if(ServingAnimations.owns(p)&&!ServingAnimations.pineapple(p))return Math.min(PopsicleMotion.INSPECT_TICKS,elapsed/1.5);
        if(!p.id().equals("farmingtales:purified_water"))return Math.min(BottleMotion.INSPECT_TICKS,elapsed);
        double clock=elapsed*WATER_RATE;
        // Accelerate only the load / flight / catch. Shared time keeps the hand, bottle,
        // sound and liquid response together. Constant flight speed preserves gravity.
        if(clock<=18)return clock;
        if(clock<24)return clock+.4*6*smoothIntegral((clock-18)/6);
        if(clock<WATER_CRUISE_END)return 25.2+TOSS_SPEED*(clock-24);
        if(clock<WATER_CRUISE_END+6) {
            double age=clock-WATER_CRUISE_END;
            return 65+TOSS_SPEED*age-.4*6*smoothIntegral(age/6);
        }
        return Math.min(82,72.2+clock-WATER_CRUISE_END-6);
    }
    private static double smoothIntegral(double u) {return u*u*u*u*(u*u-3*u+2.5);}
    public static boolean crossed(double previous,double current,double cue) {return previous<cue&&current>=cue;}
    private AnimationPlayback() {}
}
