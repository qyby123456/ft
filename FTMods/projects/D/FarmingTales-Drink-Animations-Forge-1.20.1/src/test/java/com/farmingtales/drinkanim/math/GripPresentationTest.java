package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class GripPresentationTest {
    @Test void wineInspectionCentersTheContainerWhileJuiceUsesOneHand() {
        for(var p:DrinkCatalog.all())for(int t=24;t<=84;t+=2) {
            if(p.wine()&&p.style()!=DrinkProfile.Style.BRANDY
                &&ContainerAnimations.family(p)!=ContainerAnimations.Family.BRANDY) {
                var pose=DrinkMotion.sample(p,Action.INSPECT,t,16,true);
                var rig=BottleRig.solve(pose,Action.INSPECT,t,16,1,true,p);
                float s=p.renderScale()/16;
                var center=new Vector3f(((p.boundsMin()[0]+p.boundsMax()[0])*.5f-8)*s,
                    ((p.boundsMin()[1]+p.boundsMax()[1])*.5f-4.8f)*s,
                    ((p.boundsMin()[2]+p.boundsMax()[2])*.5f-8)*s);
                rig.bottle().rotation().transform(center).add(rig.bottle().position());
                assertEquals(0,center.x,1e-6,p.id()+" centered in camera space");
                assertTrue(rig.assistance()>.8,p.id()+" supported during presentation");
            }
            if(p.style()==DrinkProfile.Style.JUICE)assertEquals(0,DrinkMotion.assistance(p,Action.INSPECT,t),1e-8);
        }
    }
    @Test void locomotionCameraRespondsToLandingAndComesFullyToRest() {
        var m=new Locomotion();m.reset(0);var zero=new BottleMotion.Camera(0,0,0);
        m.tick(0,0,false,false,.42,0);
        assertTrue(m.camera(1,zero,1).magnitude()>.15);
        for(int i=0;i<150;i++)m.tick(0,0,true,false,0,0);
        assertTrue(m.camera(1,zero,1).magnitude()<1e-6,"Residual angle below one millionth of a degree");
    }
}
