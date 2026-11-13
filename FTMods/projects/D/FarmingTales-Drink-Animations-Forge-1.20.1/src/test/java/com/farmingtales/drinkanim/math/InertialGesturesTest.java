package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.ContainerAnimations.Family;

class InertialGesturesTest {
    private static final BottleMotion.Pose ZERO=new BottleMotion.Pose(0,0,0,0,0,0,.8);
    @Test void canMakesFastVerticalStrokesWithTrailingAngularMomentumAndSettles() {
        double maxSpeed=0,lastPitch=0;boolean lag=false;
        for(double t=32;t<51;t+=.125) {
            var a=InertialGestures.apply(Family.CAN,t,ZERO);
            var b=InertialGestures.apply(Family.CAN,t+.125,ZERO);
            maxSpeed=Math.max(maxSpeed,Math.abs(b.y()-a.y())/.125);
            assertEquals(0,a.x(),1e-10);assertTrue(Math.abs(a.pitch())<25);
            if(Math.abs(a.y())>.145&&Math.abs(b.pitch()-a.pitch())>.3)lag=true;
            lastPitch=b.pitch();
        }
        assertTrue(maxSpeed>.11,"Travel is a fast stroke, not the old slow bob");
        assertTrue(lag,"Angular response continues at the vertical reversal");
        assertTrue(Math.abs(InertialGestures.apply(Family.CAN,59,ZERO).pitch())>.15,"Damped wrist motion survives the last drive");
        var settled=InertialGestures.apply(Family.CAN,100,ZERO);
        assertEquals(0,settled.pitch(),1e-9);assertEquals(0,settled.y(),1e-9);
        var plain=DrinkCatalog.find("farmingtales:mozhao");var pink=DrinkCatalog.find("farmingtales:mozhao_p");
        for(double t=0;t<104;t+=.5)assertEquals(DrinkMotion.sample(plain,Action.INSPECT,t,16,true),DrinkMotion.sample(pink,Action.INSPECT,t,16,true));
    }
    @Test void juiceSwirlsInOneDirectionWithTwoAxisLiquidForcingAndOneHand() {
        double area=0,yRange=0,axisDifference=0;
        var p=DrinkCatalog.find("farmingtales:sweet_orange_juice");
        for(double t=32;t<67;t+=.125) {
            var a=InertialGestures.apply(Family.JUICE,t,ZERO);var b=InertialGestures.apply(Family.JUICE,t+.125,ZERO);
            double swept=a.x()*b.z()-a.z()*b.x();assertTrue(swept>0,"Keep circling in one direction");area+=swept;
            yRange=Math.max(yRange,Math.abs(a.y()));
            axisDifference=Math.max(axisDifference,Math.abs(InertialGestures.liquid(Family.JUICE,t,0)-InertialGestures.liquid(Family.JUICE,t,2)));
            assertEquals(0,DrinkMotion.assistance(p,Action.INSPECT,t),1e-9);
        }
        assertTrue(area>.01);assertTrue(yRange<.01,"Swirl horizontally rather than shake vertically");assertTrue(axisDifference>.02);
        for(var p2:DrinkCatalog.all())if(p2.pullCap())for(var action:Action.values())for(double t=0;t<140;t+=.5)
            assertEquals(BottleClosures.fixedBottle(p2)?0:1,DrinkMotion.neckReveal(p2,action,t,16,true),"Only bottles with a replacement neck render that layer");
    }
}
