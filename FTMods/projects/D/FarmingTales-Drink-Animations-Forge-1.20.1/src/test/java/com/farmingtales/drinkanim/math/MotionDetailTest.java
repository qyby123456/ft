package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class MotionDetailTest {
    private BottleMotion.Pose sample(DrinkProfile p,double t) {return DrinkMotion.sample(p,Action.INSPECT,t,16,true);}
    @Test void shakingReversesUnderForceWithoutFlatAccelerationAtEveryExtreme() {
        var p=DrinkCatalog.find("farmingtales:mozhao");double h=.02;
        for(double t:new double[]{36,40,44,48}) {
            double a=(sample(p,t+h).y()-2*sample(p,t).y()+sample(p,t-h).y())/(h*h);
            assertTrue(Math.abs(a)>.004,"Each shake must have restoring acceleration at its extreme: "+t);
            double left=(sample(p,t).y()-2*sample(p,t-h).y()+sample(p,t-2*h).y())/(h*h);
            double right=(sample(p,t+2*h).y()-2*sample(p,t+h).y()+sample(p,t).y())/(h*h);
            assertEquals(left,right,.001,"Continuous force through the reversal");
        }
    }
    @Test void waterLaunchAndCatchMatchFlightVelocityIncludingTheSpinningCentreOfMass() {
        var p=DrinkCatalog.find("farmingtales:purified_water");double h=.001;
        for(double t:new double[]{25,30,48,53}) {
            var a=sample(p,t-h);var b=sample(p,t);var c=sample(p,t+h);
            assertEquals((b.x()-a.x())/h,(c.x()-b.x())/h,.002,"Horizontal contact velocity at "+t);
            assertEquals((b.y()-a.y())/h,(c.y()-b.y())/h,.002,"Vertical contact velocity at "+t);
            assertEquals((b.roll()-a.roll())/h,(c.roll()-b.roll())/h,.03,"Angular contact velocity at "+t);
        }
        for(double t=48;t<54;t+=.1) {
            var pose=sample(p,t);var anchor=ContainerAnimations.handAnchor(p,Action.INSPECT,t,16,pose);
            assertTrue(Math.abs(anchor.roll())<20,"The catching forearm must not unwind a full turn");
        }
    }
    @Test void facetAccelerationDoesNotShortenTheFourPointFourFiveSecondStops() {
        for(String item:List.of("orange_wine","rum","light_beer")) {
            var p=DrinkCatalog.find("farmingtales:"+item);int start=item.equals("rum")?23:22;
            assertEquals(104,AnimationPlayback.ticks(p,Action.INSPECT,16));
            for(int turn=0;turn<4;turn++) {
                double begin=start+turn*16;
                assertTrue(sample(p,begin+3.5).yaw()>155+turn*90+45,"Torque precedes the longer braking phase");
                for(double t=begin+7;t<=begin+16;t+=.125)
                    assertEquals(155+(turn+1)*90,sample(p,t).yaw(),1e-7,"Each face holds for all nine ticks");
            }
        }
    }
    @Test void fourSwallowsFollowExistingAudioAndFinishWithoutAResidualCameraOffset() {
        for(int duration:new int[]{16,40,100}) {
            var cues=AnimationSoundTimeline.drinking(duration);assertEquals(4,cues.size());
            for(var cue:cues) {
                assertEquals(0,MotionDetail.swallow(cue.tick(),duration),1e-9);
                assertTrue(MotionDetail.swallow(cue.tick()+3,duration)>.3);
            }
            assertEquals(0,MotionDetail.swallow(48+BottleMotion.sipTicks(duration),duration),1e-9);
        }
        for(var p:DrinkCatalog.all())for(var action:List.of(Action.DRAW,Action.STOW,Action.INSPECT,Action.DRINK)) {
            double end=AnimationPlayback.authored(p,action,AnimationPlayback.ticks(p,action,16),16);
            assertEquals(0,DrinkMotion.camera(p,action,end,16).magnitude(),1e-7);
        }
    }
}
