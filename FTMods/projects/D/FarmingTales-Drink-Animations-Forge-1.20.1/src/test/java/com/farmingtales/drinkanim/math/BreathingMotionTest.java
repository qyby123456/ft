package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BreathingMotionTest {
    private BottleRig.Frame rig(DrinkProfile p,BottleMotion.Action a,double age,boolean slim,int side) {
        return BottleRig.solve(DrinkMotion.sample(p,a,age,16,true),a,age,16,side,slim,p);
    }
    private Vector3f relative(BottleRig.Transform root,BottleRig.Transform child) {
        return new Quaternionf(root.rotation()).conjugate().transform(new Vector3f(child.position()).sub(root.position()));
    }
    @Test void allFamiliesRetainGripLidsAndIndependentFlightPaths() {
        for(var p:DrinkCatalog.all())for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1})
            for(var a:new BottleMotion.Action[]{BottleMotion.Action.IDLE,BottleMotion.Action.DRAW,BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK})
                for(double age:new double[]{0,8,24,35,56,75}) {
                    var base=rig(p,a,age,slim,side);var result=BreathingMotion.apply(base,143.5,side);
                    var before=new BottleRig.Transform[]{base.hand(),base.helper(),base.cork()};
                    var after=new BottleRig.Transform[]{result.hand(),result.helper(),result.cork()};
                    for(int i=0;i<3;i++) {
                        assertTrue(relative(base.bottle(),before[i]).distance(relative(result.bottle(),after[i]))<.00002,p.id());
                        var q=new Quaternionf(base.bottle().rotation()).conjugate().mul(before[i].rotation());
                        var r=new Quaternionf(result.bottle().rotation()).conjugate().mul(after[i].rotation());
                        assertTrue(Math.abs(q.dot(r))>.99999,p.id());
                    }
                    assertEquals(base.assistance(),result.assistance());
                }
    }
    @Test void breathingRemainsVisibleDuringEatingAndInspection() {
        for(var p:DrinkCatalog.all())for(var a:new BottleMotion.Action[]{BottleMotion.Action.IDLE,BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT}) {
            var base=rig(p,a,a==BottleMotion.Action.IDLE?0:57,true,1);
            var low=BreathingMotion.apply(base,60,1);var high=BreathingMotion.apply(base,20,1);
            assertTrue(low.hand().position().distance(high.hand().position())>.01,p.id());
            assertTrue(low.bottle().position().distance(high.bottle().position())>.01,p.id());
        }
    }
    @Test void phaseIsContinuousBoundedAndPauseStable() {
        var base=rig(DrinkCatalog.ORANGE,BottleMotion.Action.IDLE,0,true,1);
        for(double t=0;t<80;t+=.25) {
            var now=BreathingMotion.apply(base,t,1);var next=BreathingMotion.apply(base,t+.25,1);
            assertTrue(now.hand().position().distance(next.hand().position())<.0004);
            assertTrue(now.hand().position().distance(base.hand().position())<.013);
            assertEquals(now,BreathingMotion.apply(base,t,1));
            assertTrue(now.hand().position().distance(BreathingMotion.apply(base,t+80,1).hand().position())<1e-6);
        }
        var original=new Vector3f(base.hand().position());
        BreathingMotion.apply(base,24,1);
        assertEquals(original,base.hand().position());
    }
    @Test void actionChangesDoNotResetTheCommonBreath() {
        var base=rig(DrinkCatalog.ORANGE,BottleMotion.Action.IDLE,0,true,1);
        var finish=BreathingMotion.apply(base,79.999,1);
        var start=BreathingMotion.apply(base,80.001,1);
        assertTrue(finish.hand().position().distance(start.hand().position())<.00001);
        // The additive layer takes player time only: action age and stack identity cannot restart it.
        assertEquals(BreathingMotion.apply(base,124.5,1),BreathingMotion.apply(base,44.5,1));
    }
}
