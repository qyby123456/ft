package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class CorkAttachmentTest {
    @Test void allWineCorksStayAlignedWithTheirMouthWhileBeingExtracted() {
        for(var profile:DrinkCatalog.all()) {
            if(!profile.cork())continue;
            for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
                double lastY=-100;
                for(double tick=0;tick<=33;tick+=.125) {
                    if(DrinkMotion.capLift(profile,tick)>profile.neckMax()-profile.neckMin()+.01)break;
                    var motion=DrinkMotion.sample(profile,BottleMotion.Action.DRINK,tick,16,true);
                    var frame=BottleRig.solve(motion,BottleMotion.Action.DRINK,tick,16,side,slim,profile);
                    var inverse=new Quaternionf(frame.bottle().rotation()).conjugate();
                    var local=inverse.transform(new Vector3f(frame.cork().position()).sub(frame.bottle().position()));
                    float scale=profile.renderScale();
                    assertEquals((profile.capPivot()[0]-8)/16*scale,local.x,1e-6,profile.id());
                    assertEquals((profile.capPivot()[2]-8)/16*scale,local.z,1e-6,profile.id());
                    assertTrue(local.y>=lastY-1e-6,"Pull only upward: "+profile.id());lastY=local.y;
                    var relative=new Quaternionf(frame.bottle().rotation()).conjugate().mul(frame.cork().rotation());
                    assertEquals(1,Math.abs(relative.w),1e-6,"No rocking or twisting inside mouth: "+profile.id());
                }
            }
        }
    }
}
