package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class MotionRefinementTest {
    @Test void cupLiftNeverStopsAtTheOldIntermediateKeys() {
        for(var p:DrinkCatalog.all())if(p.cup())for(double t=10;t<=38;t+=.1) {
            var a=CupAnimations.sample(p,Action.DRINK,t-.001,16,true);
            var b=CupAnimations.sample(p,Action.DRINK,t+.001,16,true);
            assertTrue((b.y()-a.y())/.002>.0005,p.id()+" interrupted lift at "+t);
        }
    }
    @Test void placementReachesItsEndpointsAndHasNoInternalStopsOnRecovery() {
        for(var p:DrinkCatalog.all()) {
            var rest=DrinkMotion.sample(p,Action.IDLE,0,16,true);
            assertEquals(rest,PlacementMotion.sample(p,0,true));
            assertEquals(rest,PlacementMotion.sample(p,PlacementMotion.TICKS,true));
            assertTrue(PlacementMotion.bottleVisible(6,true));
            assertFalse(PlacementMotion.bottleVisible(30,false));
            for(double t=0;t<=6;t+=.1) {
                var a=PlacementMotion.sample(p,t,true);
                assertTrue(PlacementMotion.bottleVisible(t,true));
                assertTrue(Math.abs(a.y()-rest.y())<=.061,"Short placement must not hide in the pocket");
            }
        }
    }
    @Test void waterReturnCompletesBeforeTick73AndTheActualPlaybackReachesRunningIdle() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        double end=AnimationPlayback.authored(p,Action.INSPECT,AnimationPlayback.ticks(p,Action.INSPECT,16),16);
        assertEquals(1,DrinkMotion.idleBlend(p,Action.INSPECT,end,16,true));
        assertEquals(0,DrinkMotion.camera(p,Action.INSPECT,end,16).magnitude());
        var rest=DrinkMotion.sample(p,Action.IDLE,0,16,true);
        var returned=DrinkMotion.sample(p,Action.INSPECT,73,16,true);
        assertEquals(rest.x(),returned.x(),1e-8);assertEquals(rest.y(),returned.y(),1e-8);assertEquals(rest.z(),returned.z(),1e-8);
    }
}
