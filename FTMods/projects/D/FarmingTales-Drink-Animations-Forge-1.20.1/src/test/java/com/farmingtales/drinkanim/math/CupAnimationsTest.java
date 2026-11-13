package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class CupAnimationsTest {
    @Test void elbowAndShoulderFramingDoNotPushRaisedCupsBelowTheirIdleHold() {
        for(var p:DrinkCatalog.all())if(p.cup())for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            var idle=BottleRig.solve(DrinkMotion.sample(p,Action.IDLE,0,16,true),Action.IDLE,0,16,side,slim,p);
            float idleY=idle.bottle().position().y+BottleFraming.offset(idle,Action.IDLE,0,16,true,p).y;
            for(double t:new double[]{48,54,62}) {
                var raised=BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,16,true),Action.DRINK,t,16,side,slim,p);
                var framing=BottleFraming.offset(raised,Action.DRINK,t,16,true,p);
                assertTrue(raised.bottle().position().y+framing.y>idleY+.05,p.id()+" shoulder correction hides the raised cup");
            }
        }
    }
    @Test void openCupsShareTheRequestedSwirlAndNeverAcquireAClosure() {
        var juice=DrinkCatalog.find("farmingtales:sweet_orange_juice");
        for(var p:DrinkCatalog.all())if(p.cup()) {
            assertNotNull(p.cupShape());assertFalse(p.liquid().isEmpty());
            for(int t=0;t<=104;t++) {
                if(!CupAnimations.coffeePreview(p)&&!ServingAnimations.owns(p)&&!CoconutAnimations.owns(p))assertEquals(DrinkMotion.sample(juice,Action.INSPECT,t,16,true).offset(0,.12,0,0,0),DrinkMotion.sample(p,Action.INSPECT,t,16,true));
                if(!CupAnimations.coffeePreview(p)&&!ServingAnimations.owns(p)&&!CoconutAnimations.owns(p))assertEquals(0,DrinkMotion.assistance(p,Action.INSPECT,t));
            }
            for(var action:Action.values())for(int t=0;t<160;t++) {
                assertFalse(DrinkMotion.capVisible(p,action,t,16,true));
                assertFalse(DrinkMotion.capDetached(p,action,t));
                assertEquals(0,DrinkMotion.neckReveal(p,action,t,16,true));
            }
        }
    }
    @Test void bothArmWidthsAndHandsKeepTheirGripFixedToTheCup() {
        for(var p:DrinkCatalog.all())if(p.cup())for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            Vector3f reference=null;Quaternionf direction=null;
            for(var action:new Action[]{Action.INSPECT,Action.DRINK})for(int t=0;t<=100;t+=2) {
                var rig=BottleRig.solve(DrinkMotion.sample(p,action,t,16,true),action,t,16,side,slim,p);
                Quaternionf inverse=new Quaternionf(rig.bottle().rotation()).conjugate();
                var contact=inverse.transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                var relative=new Quaternionf(inverse).mul(rig.hand().rotation());
                if(reference==null){reference=contact;direction=relative;}
                assertTrue(reference.distance(contact)<1e-6,p.id()+" drifting palm");
                if(action==Action.INSPECT)assertEquals(1,Math.abs(direction.dot(relative)),1e-5,p.id()+" slipping wrist");
            }
        }
    }
    @Test void cupReplacementAndTailHaveNoPositionOrRotationJumps() {
        for(var p:DrinkCatalog.all())if(p.cup())for(int use:new int[]{16,40}) {
            double end=48+BottleMotion.sipTicks(use);
            for(double t:new double[]{14,32,48,end,end+12,end+24,end+37,end+41}) {
                var a=DrinkMotion.sample(p,Action.DRINK,t-.0001,use,true);
                var b=DrinkMotion.sample(p,Action.DRINK,t+.0001,use,true);
                assertTrue(Math.abs(a.x()-b.x())+Math.abs(a.y()-b.y())+Math.abs(a.z()-b.z())<.001,p.id()+" jump at "+t);
                assertEquals(1,Math.abs(BottleRig.rotation(a,1).dot(BottleRig.rotation(b,1))),1e-5);
            }
            var empty=DrinkMotion.sample(p,Action.DRINK,end+40,use,false);
            assertTrue(empty.y()<-2);assertEquals(.025,empty.fill());
        }
    }
}
