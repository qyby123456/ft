package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class AnimationPlaybackTest {
    @Test void everyJuiceCupReachesDrinkingPoseInUnderOneSecond() {
        for(var p:DrinkCatalog.all())if(p.cup()&&!ServingAnimations.owns(p)) {
            double authored=AnimationPlayback.authored(p,Action.DRINK,19,16);
            assertEquals(BottleMotion.RAISE_TICKS,authored,1e-8,p.id());
            assertEquals(DrinkMotion.sample(p,Action.DRINK,BottleMotion.RAISE_TICKS,16,true),
                DrinkMotion.sample(p,Action.DRINK,authored,16,true),p.id());
        }
    }
    @Test void bottledJuiceRetainsItsOpeningAndThenRaisesTwiceAsFast() {
        var p=DrinkCatalog.find("farmingtales:sweet_orange_juice");
        for(double t=0;t<=28;t+=.25)
            assertEquals(t*BottleMotion.RAISE_TICKS/38,AnimationPlayback.authored(p,Action.DRINK,t,16),1e-8);
        assertEquals(BottleMotion.RAISE_TICKS,AnimationPlayback.authored(p,Action.DRINK,33,16),1e-8);
        assertEquals(104,AnimationPlayback.ticks(p,Action.INSPECT,16));
    }
    @Test void everyProfileReachesTheFullDrawStowAndReplacementEndpoints() {
        for(var p:DrinkCatalog.all())for(int use:new int[]{16,40,100}) {
            int drawTicks=10;
            assertEquals(drawTicks,AnimationPlayback.ticks(p,Action.DRAW,use));
            assertEquals(19,AnimationPlayback.authored(p,Action.DRAW,drawTicks,use),1e-8);
            assertEquals(9,AnimationPlayback.ticks(p,Action.STOW,use));
            assertEquals(20,AnimationPlayback.authored(p,Action.STOW,9,use),1e-8);
            assertEquals(BottleMotion.totalDrinkTicks(use)-(p.jam()?19:0),AnimationPlayback.authored(p,Action.DRINK,AnimationPlayback.ticks(p,Action.DRINK,use),use),1e-8);
            for(var action:new Action[]{Action.DRAW,Action.STOW,Action.DRINK,Action.INSPECT}) {
                double before=0;
                for(double t=0;t<=AnimationPlayback.ticks(p,action,use);t+=.05) {
                    double now=AnimationPlayback.authored(p,action,t,use);
                    assertTrue(now>=before&&now-before<.13,"Continuous monotonic clock for "+p.id()+" "+action);
                    before=now;
                }
            }
        }
    }
    @Test void fourSipCuesAndVanillaUseDurationArePreservedDespiteFasterOpening() {
        for(var p:DrinkCatalog.all())for(int use:new int[]{16,40,100}) {
            int count=0;
            for(int tick=1;tick<=AnimationPlayback.ticks(p,Action.DRINK,use);tick++) {
                double before=AnimationPlayback.authored(p,Action.DRINK,tick-1,use),now=AnimationPlayback.authored(p,Action.DRINK,tick,use);
                for(var cue:AnimationSoundTimeline.drinking(p,use))if(AnimationPlayback.crossed(before,now,cue.tick()))count++;
                if(tick>AnimationPlayback.raiseTicks(p)&&tick<=AnimationPlayback.sipEnd(p,use))
                    assertEquals(1,now-before,1e-8,"Shared sip clock");
            }
            assertEquals(ServingAnimations.owns(p)?ServingAnimations.popsicle(p)?11:ServingAnimations.pineapple(p)?6:8
                :p.jam()?6:PlateFoodAnimations.owns(p)?2*PlateFoodAnimations.portions(p):p.food()?6:4,count);
            assertEquals(BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(use),AnimationPlayback.authored(p,Action.DRINK,AnimationPlayback.sipEnd(p,use),use),1e-8);
        }
    }
    @Test void otherInspectionClocksStayUnchanged() {
        for(var p:DrinkCatalog.all()) {
            if(p.id().equals("farmingtales:purified_water")||p.jam()||p.food()||ServingAnimations.owns(p))continue;
            assertEquals(104,AnimationPlayback.ticks(p,Action.INSPECT,16));
            for(double time=0;time<=104;time+=.25)assertEquals(time,AnimationPlayback.authored(p,Action.INSPECT,time,16),1e-8);
        }
    }
    @Test void popsiclePickupAndReturnPlaySlowerWithSharedEventTiming() {
        var p=DrinkCatalog.find("papercraft_magic_decoration:long_island_popsicle_tea");
        assertEquals(156,AnimationPlayback.ticks(p,Action.INSPECT,32));
        // The 20 authored ticks from grasp to display, and back, now take 30 real ticks.
        assertEquals(18,AnimationPlayback.authored(p,Action.INSPECT,27,32),1e-8);
        assertEquals(38,AnimationPlayback.authored(p,Action.INSPECT,57,32),1e-8);
        assertEquals(50,AnimationPlayback.authored(p,Action.INSPECT,75,32),1e-8);
        assertEquals(70,AnimationPlayback.authored(p,Action.INSPECT,105,32),1e-8);
        assertEquals(104,AnimationPlayback.authored(p,Action.INSPECT,156,32),1e-8);
        // Eating also allows 1.75 s to take and turn the ice before the first bite.
        assertEquals(14,AnimationPlayback.authored(p,Action.DRINK,35,32),1e-8);
        for(int bite:new int[]{14,22,30}) {
            double elapsed=bite*2.5;
            assertTrue(AnimationPlayback.crossed(AnimationPlayback.authored(p,Action.DRINK,elapsed-.1,32),
                AnimationPlayback.authored(p,Action.DRINK,elapsed,32),bite));
        }
    }
    @Test void quarterTurnInspectionHoldsStillLastNineRealTicks() {
        for(String id:new String[]{"orange_wine","light_beer","rum"}) {
            var p=DrinkCatalog.find("farmingtales:"+id);
            double start=id.equals("rum")?30:29;
            double realStart=start;
            for(int face=0;face<4;face++) {
                double begin=realStart+face*16;
                double a=AnimationPlayback.authored(p,Action.INSPECT,begin,16);
                double b=AnimationPlayback.authored(p,Action.INSPECT,begin+9,16);
                assertEquals(start+16*face,a,1e-8);assertEquals(a+9,b,1e-8);
                assertEquals(DrinkMotion.sample(p,Action.INSPECT,a,16,true).yaw(),DrinkMotion.sample(p,Action.INSPECT,b,16,true).yaw(),1e-7);
            }
        }
    }
}
