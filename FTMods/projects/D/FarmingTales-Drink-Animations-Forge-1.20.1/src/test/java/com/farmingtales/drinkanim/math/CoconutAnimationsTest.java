package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class CoconutAnimationsTest {
    private final DrinkProfile p=DrinkCatalog.find("papercraft_magic_decoration:ferry_whiskey");
    private BottleRig.Frame rig(double t,int side,boolean slim) {
        return BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,p);
    }
    @Test void pineappleIsEatenBeforeLiquidAndOnlyReplacementGetsFreshFruit() {
        assertTrue(ServingAnimations.pineapple(p));
        assertEquals(1,ServingAnimations.models(p).length);
        assertEquals(0,ServingAnimations.propStage(p,Action.DRINK,13.99));
        assertEquals(0,ServingAnimations.propStage(p,Action.DRINK,14));
        assertTrue(ServingAnimations.propVisible(p,Action.DRINK,13.99,32,true));
        for(double t=14;t<129;t+=.1)assertFalse(ServingAnimations.propVisible(p,Action.DRINK,t,32,true));
        assertTrue(ServingAnimations.propVisible(p,Action.DRINK,129,32,true));
        assertEquals(0,ServingAnimations.propStage(p,Action.DRINK,129));
        assertFalse(ServingAnimations.propVisible(p,Action.DRINK,200,32,false));
        for(double t=0;t<=48;t+=.1)assertEquals(.8,DrinkMotion.sample(p,Action.DRINK,t,32,true).fill(),1e-6);
        var sounds=AnimationSoundTimeline.drinking(p,32);
        var bites=sounds.stream().filter(c->c.event().equals("pineapple_bite")).toList();
        assertEquals(1,bites.size());
        assertTrue(sounds.stream().filter(c->c.event().equals("gulp")).allMatch(c->c.tick()>bites.get(0).tick()));
        assertTrue(DecorationMotion.parts(p).stream().noneMatch(d->d.kind()==DecorationMotion.Kind.FRUIT),"do not draw an uneaten duplicate");
    }
    @Test void freePalmOwnsFruitWhileItClearsTheRimAndApproachesMouth() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            var grasp=rig(4,side,slim);var clear=rig(8,side,slim);
            assertTrue(clear.cork().position().y>grasp.cork().position().y+.16,"lift out before approaching mouth");
            for(double t=4;t<14;t+=.125) {
                var r=rig(t,side,slim);
                assertEquals(0,r.helper().position().distance(r.cork().position()),1e-6,"fruit must stay on palm");
                assertEquals(1,r.assistance(),1e-6);
            }
            var mouth=rig(14,side,slim);
            var tip=mouth.cork().rotation().transform(ServingAnimations.tip(p)).add(mouth.cork().position());
            assertEquals(-side*.09,tip.x,1e-5);assertEquals(-.26,tip.z,1e-5);
            assertEquals(0,rig(22,side,slim).assistance(),1e-6,"empty helper retracts before drinking");
        }
    }
    @Test void leftFreeHandBringsFruitToMouthWithoutPushingCupOutOfView() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(int variant=0;variant<4;variant++) {
            for(double t=0;t<=32;t+=.25) {
                var r=EatingMotion.apply(rig(t,side,slim),p,Action.DRINK,t,32,variant,side);
                r=BreathingMotion.apply(r,t,side);
                var cam=DrinkMotion.camera(p,Action.DRINK,t,32,variant).rotation();
                var offset=BottleFraming.offset(r,Action.DRINK,t,32,true,p,cam);
                assertTrue(offset.y>-.13,"helper must not push the whole meal below the screen: "+t+" / "+offset);
                assertTrue(BottleFraming.bottleFront(r,offset,cam,Action.DRINK,t,32,true,p)<-.279);
                for(var arm:new BottleRig.Transform[]{r.hand(),r.helper()})for(float x:new float[]{-2.25f,2.25f})
                    for(float y:new float[]{-12.25f,.25f})for(float z:new float[]{-2.25f,2.25f}) {
                        var corner=arm.rotation().transform(new Vector3f(x,y,z).mul(BottleRig.ARM_SCALE/16)).add(arm.position()).add(offset);
                        assertTrue(cam.transform(corner).z<-.059,"sleeve near-plane clearance");
                    }
                if(t==14) {
                    var tip=r.cork().rotation().transform(ServingAnimations.tip(p)).add(r.cork().position()).add(offset);
                    cam.transform(tip);
                    assertTrue(tip.y/-tip.z<-.55,"edible end enters the lower mouth edge at the only bite");
                    assertTrue(r.helper().position().x*side<0,"free hand returns to its own side at the mouth");
                }
            }
        }
    }
    @Test void drinkingProgressivelyTiltsTheCoconutAndMovesTheHoldingWrist() {
        for(int duration:new int[]{16,32,64})for(int side:new int[]{-1,1}) {
            double end=48+BottleMotion.sipTicks(duration),previous=0;
            for(double t=48;t<=end;t+=.25) {
                var pose=DrinkMotion.sample(p,Action.DRINK,t,duration,true);
                assertTrue(pose.pitch()>=previous-.03,"no backwards drinking tilt");previous=pose.pitch();
                assertTrue(pose.pitch()>=47&&pose.pitch()<=70);
            }
            var start=DrinkMotion.sample(p,Action.DRINK,48,duration,true);
            var stop=DrinkMotion.sample(p,Action.DRINK,end,duration,true);
            assertTrue(stop.pitch()-start.pitch()>19);
            var a=BottleRig.solve(start,Action.DRINK,48,duration,side,true,p);
            var b=BottleRig.solve(stop,Action.DRINK,end,duration,side,true,p);
            assertTrue(new Quaternionf(a.hand().rotation()).difference(b.hand().rotation()).angle()>.30);
            assertTrue(DrinkMotion.camera(p,Action.DRINK,end-2,duration).pitch()>DrinkMotion.camera(p,Action.DRINK,48,duration).pitch()+.7);
        }
    }
    @Test void extractionBitesAndDrinkingHaveNoPositionOrRotationPops() {
        for(int side:new int[]{-1,1})for(double t:new double[]{4,8,14,21,25,48,88,100,112,125,129}) {
            var a=rig(t-.0001,side,true);var b=rig(t+.0001,side,true);
            assertTrue(a.bottle().position().distance(b.bottle().position())<.001,"cup jump at "+t);
            assertEquals(1,Math.abs(a.bottle().rotation().dot(b.bottle().rotation())),.00001);
            if(t<14) {
                assertTrue(a.cork().position().distance(b.cork().position())<.001,"fruit jump at "+t);
                assertEquals(1,Math.abs(a.cork().rotation().dot(b.cork().rotation())),.00001);
            }
            if(t<32)assertTrue(a.helper().position().distance(b.helper().position())<.001,"helper jump at "+t);
        }
    }
    @Test void inspectionSupportsCoconutAndReturnsWithoutSpillingOrConsumption() {
        assertEquals(104,AnimationPlayback.ticks(p,Action.INSPECT,32));
        assertTrue(DrinkMotion.assistance(p,Action.INSPECT,35)>.99);
        for(double t=0;t<=104;t+=.25) {
            var pose=DrinkMotion.sample(p,Action.INSPECT,t,32,true);
            assertEquals(.8,pose.fill());assertTrue(Math.abs(pose.pitch())<25);
            assertTrue(ServingAnimations.propVisible(p,Action.INSPECT,t,32,true));
        }
        assertEquals(DrinkMotion.sample(p,Action.IDLE,0,32,true),DrinkMotion.sample(p,Action.INSPECT,104,32,true));
    }
    @Test void strawRemainsAnInertialDecorationWithoutMouthLock() {
        var state=new DecorationMotion.State();
        var stick=DecorationMotion.parts(p).stream().filter(part->part.kind()==DecorationMotion.Kind.STICK).findFirst().orElseThrow();
        state.advance(0,new Vector3f(),new Quaternionf(),0,0);
        state.advance(.05,new Vector3f(.2f,.1f,0),new Quaternionf().rotateX(.2f),.3,.3);
        var normal=new LiquidMesh.V(.3,1,.3);
        var spring=state.transform(stick,p,normal,.4);
        assertTrue(spring.rotation().angle()>.01);
        for(double t:new double[]{0,15,48,60,88})assertEquals(spring,state.transform(stick,p,normal,.4,Action.DRINK,t,32));
    }
}
