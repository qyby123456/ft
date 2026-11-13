package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class PopsicleMotionTest {
    private static final DrinkProfile ICE=DrinkCatalog.find("papercraft_magic_decoration:long_island_popsicle_tea");
    private static BottleRig.Frame rig(Action action,double t,int side,boolean slim) {
        return BottleRig.solve(DrinkMotion.sample(ICE,action,t,32,true),action,t,32,side,slim,ICE);
    }
    @Test void carryingHandKeepsMovingBetweenMouthfuls() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            var start=rig(Action.DRINK,14,side,slim).hand().position();
            double excursion=0;
            for(double t=14;t<30;t+=.1) {
                var r=rig(Action.DRINK,t,side,slim);
                excursion=Math.max(excursion,r.hand().position().distance(start));
                PopsicleClearanceTest.assertWoodGripped(r,slim);
            }
            assertTrue(excursion>.02&&excursion<.085,"visible, restrained weight transfer: "+excursion);
        }
    }
    @Test void renderedTrajectoriesRetainVelocityAcrossGestureKeys() {
        for(int side:new int[]{-1,1})for(Action action:new Action[]{Action.DRINK,Action.INSPECT}) {
            double[] times=action==Action.DRINK?new double[]{8,11,14,16.5,17,19.1,21,22,25,25.5,27.7,30,34,40,44,48}
                :new double[]{6,19,28,39,51,62,74,84,96,104};
            for(double t:times) {
                double dt=.01;var before=rig(action,t-dt,side,true);var at=rig(action,t,side,true);var after=rig(action,t+dt,side,true);
                Vector3f incoming=new Vector3f(at.hand().position()).sub(before.hand().position()).div((float)dt);
                Vector3f outgoing=new Vector3f(after.hand().position()).sub(at.hand().position()).div((float)dt);
                assertTrue(incoming.distance(outgoing)<.003,"carrying hand velocity at "+action+" "+t+": "+incoming.distance(outgoing));
                if(action==Action.DRINK&&t<30) {
                    incoming.set(at.cork().position()).sub(before.cork().position()).div((float)dt);
                    outgoing.set(after.cork().position()).sub(at.cork().position()).div((float)dt);
                    assertTrue(incoming.distance(outgoing)<.005,"ice velocity at "+t+": "+incoming.distance(outgoing));
                }
            }
        }
    }
    @Test void inspectionShowsIceWithAContinuousCarryGripAndReturnsToIdle() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            var idle=rig(Action.IDLE,0,side,slim);double minYaw=1000,maxYaw=-1000;
            var contact=new org.joml.Quaternionf(idle.bottle().rotation()).conjugate()
                .transform(new Vector3f(idle.hand().position()).sub(idle.bottle().position()));
            for(double t=0;t<=PopsicleMotion.INSPECT_TICKS;t+=.125) {
                var pose=DrinkMotion.sample(ICE,Action.INSPECT,t,32,true);var r=rig(Action.INSPECT,t,side,slim);
                var local=new org.joml.Quaternionf(r.bottle().rotation()).conjugate()
                    .transform(new Vector3f(r.hand().position()).sub(r.bottle().position()));
                assertTrue(contact.distance(local)<1e-6,"palm must stay fixed to the glass");
                if(t>=18&&t<=72)assertEquals(1,r.assistance(),1e-6);
                if(t<=8||t>=84)assertEquals(0,r.assistance(),1e-6);
                assertTrue(Math.abs(pose.pitch())<15&&Math.abs(pose.roll())<8,"do not over-tilt the glass");
                minYaw=Math.min(minYaw,pose.yaw());maxYaw=Math.max(maxYaw,pose.yaw());
            }
            assertTrue(maxYaw-minYaw>10&&maxYaw-minYaw<20,"restrained cup presentation");
            assertEquals(0,idle.bottle().position().distance(rig(Action.INSPECT,104,side,slim).bottle().position()),1e-6);
        }
    }
    @Test void inspectingIceKeepsThePalmAttachedAndReseatsBeforeRelease() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            for(double t=18;t<=72;t+=.125) {
                var r=rig(Action.INSPECT,t,side,slim);
                PopsicleClearanceTest.assertWoodGripped(r,slim);
                assertEquals(r,ServingAnimations.sway(ICE,Action.INSPECT,t,r,.3,-.3));
                assertEquals(0,ServingAnimations.propStage(ICE,Action.INSPECT,t));
            }
            var r=rig(Action.INSPECT,84,side,slim);
            assertEquals(0,ServingAnimations.seated(ICE,r).position().distance(r.cork().position()),1e-6);
            assertEquals(1,Math.abs(ServingAnimations.seated(ICE,r).rotation().dot(r.cork().rotation())),1e-6);
        }
    }
    @Test void iceLeansIndependentlyBeforeTheHandExtractsIt() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            var r=rig(Action.INSPECT,18,side,slim);var seated=ServingAnimations.seated(ICE,r);
            var original=seated.rotation().transform(ServingAnimations.tip(ICE));
            var leaning=r.cork().rotation().transform(ServingAnimations.tip(ICE));
            assertTrue((leaning.x-original.x)*side>.06,"the wooden tail leans toward the free hand");
            assertEquals(14,Math.toDegrees(new org.joml.Quaternionf(seated.rotation()).difference(r.cork().rotation()).angle()),.001);
            assertTrue(Math.abs(PopsicleMotion.inspect(18).roll())<5,"glass remains upright while ice tilts");
            var pivot=ServingAnimations.tip(ICE).mul(.65f);
            assertTrue(seated.rotation().transform(new Vector3f(pivot)).add(seated.position())
                .distance(r.cork().rotation().transform(pivot).add(r.cork().position()))<.025,"small slide inside the glass keeps the rigid ice clear of the lemon");
        }
    }
    @Test void pickupKeepsGlassVisibleAndClearsRimBeforeTurningIce() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            for(var action:new Action[]{Action.DRINK,Action.INSPECT})for(double t=0;t<(action==Action.DRINK?14:84);t+=.125) {
                var r=rig(action,t,side,slim);var camera=DrinkMotion.camera(ICE,action,t,32).rotation();
                var offset=BottleFraming.offset(r,action,t,32,true,ICE,camera);
                var rim=r.bottle().rotation().transform(new Vector3f(0,8-4.8f,0).mul(ICE.renderScale()/16)).add(r.bottle().position()).add(offset);
                camera.transform(rim);
                assertTrue(rim.y/-rim.z>-.85,"pickup keeps its established lower framing limit: "+action+" "+t+" "+rim);
                if(action==Action.DRINK?t>=8:t>=26&&t<=62)
                    assertTrue(rim.y/-rim.z>-.65,"turning the ice must not pull the glass out of view: "+action+" "+t+" "+rim);
            }
            var clear=rig(Action.DRINK,8,side,slim);
            var tip=clear.cork().rotation().transform(ServingAnimations.tip(ICE)).add(clear.cork().position());
            new org.joml.Quaternionf(clear.bottle().rotation()).conjugate().transform(tip.sub(clear.bottle().position()));
            assertTrue(tip.y>(ICE.mouthY()-4.8)*ICE.renderScale()/16,"frozen end clears the rim before flipping");
        }
    }
    @Test void helperForearmStaysOnItsOwnSideThroughoutPickupAndReturn() {
        for(var action:new Action[]{Action.DRINK,Action.INSPECT})for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})
        for(double t=.125;t<=(action==Action.DRINK?41:84);t+=.125) {
            var r=rig(action,t,side,slim);if(r.assistance()<.25)continue;
            var cam=DrinkMotion.camera(ICE,action,t,32).rotation();
            var offset=BottleFraming.offset(r,action,t,32,true,ICE,cam);
            var elbow=cam.transform(r.helper().rotation().transform(new Vector3f(0,-12,0).mul(BottleRig.ARM_SCALE/16))
                .add(r.helper().position()).add(offset));
            assertTrue(side*elbow.x<-.06,"free forearm must enter from its own half of the screen at "+action+" "+t+": "+elbow);
        }
    }
    @Test void inspectionRetainsTheOriginalUprightIceAndPalmGrip() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(double t=38;t<=50;t+=.125) {
            var r=rig(Action.INSPECT,t,side,slim);
            var referenceIce=new Quaternionf().rotationY((float)Math.toRadians(side*12))
                .rotateZ((float)Math.toRadians(-side*6)).rotateX((float)Math.toRadians(-178));
            var referenceHand=BottleRig.armRotation(new ArmMotion.Pose(35,-1,0),-side,false);
            var relative=new Quaternionf(r.cork().rotation()).conjugate().mul(r.helper().rotation());
            assertEquals(1,Math.abs(relative.dot(referenceIce.conjugate().mul(referenceHand))),1e-6,
                "retain the old palm/wood orientation rather than pinching the side");
            assertTrue(r.cork().rotation().transform(ServingAnimations.tip(ICE).normalize()).y>.85);
            assertTrue(r.helper().position().distance(r.cork().position())<.041,"only a sub-pixel adjustment along the wood clears the sleeve");
            PopsicleClearanceTest.assertWoodGripped(r,slim);
        }
    }

    @Test void runningBlendWaitsForInspectionToFinish() {
        for(double t=6;t<=98;t+=.25)assertEquals(0,DrinkMotion.idleBlend(ICE,Action.INSPECT,t,32,true),1e-8);
        assertEquals(1,DrinkMotion.idleBlend(ICE,Action.INSPECT,104,32,true),1e-8);
    }
    @Test void flipFinishesEarlierWithoutShorteningTheLiftOrTranslation() {
        for(int side:new int[]{-1,1}) {
            var r=rig(Action.DRINK,8,side,false);
            var target=PopsicleMotion.upright(side);var grip=new Vector3f(-side*.19f,-.24f,-.72f);
            var turned=PopsicleMotion.extract(ICE,13,side,r.cork(),grip,target);
            assertEquals(1,Math.abs(target.dot(turned.rotation())),1e-6,"flip takes five authored ticks instead of six");
            assertTrue(turned.position().distance(grip)>.005,"position still eases toward its full fourteen-tick endpoint");
            assertEquals(0,PopsicleMotion.extract(ICE,14,side,r.cork(),grip,target).position().distance(grip),1e-6);
        }
    }

    private static Vector3f renderedPoint(Action action,double t,int side,boolean slim,int part) {
        var r=rig(action,t,side,slim);
        var camera=DrinkMotion.camera(ICE,action,t,32).rotation();
        var offset=BottleFraming.offset(r,action,t,32,true,ICE,camera);
        var point=part==0?r.bottle().rotation().transform(new Vector3f(0,.13f,0)).add(r.bottle().position())
            :part==1?new Vector3f(r.helper().position())
            :r.cork().rotation().transform(new Vector3f(0,-.15f,-.065f)).add(r.cork().position());
        return camera.transform(point.add(offset));
    }
    @Test void helperVisibilityChangesDoNotTeleportTheWholeAssembly() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})
        for(var action:new Action[]{Action.INSPECT,Action.DRINK})
        for(double t:action==Action.INSPECT?new double[]{0,8,18,26,38,50,62,72,84,104}:new double[]{0,4,8,11,14,34,41})
        for(int part=0;part<3;part++) {
            if(part==2&&action==Action.DRINK&&t>34)continue; // The released stick has ballistic speed.
            var before=renderedPoint(action,t-.0005,side,slim,part);
            var after=renderedPoint(action,t+.0005,side,slim,part);
            assertTrue(before.distance(after)<.0006,"rendered continuity including near-plane and shoulder framing: "+action+" "+t+" part="+part+" jump="+before.distance(after));
        }
    }
    @Test void inspectionMovesBetweenFramesWithoutAbruptVelocityChanges() {
        // 60 rendered frames per second, using the actual slower inspection clock.
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(int part=0;part<3;part++) {
            Vector3f previous=null,velocity=null;
            for(int frame=0;frame<=3*AnimationPlayback.ticks(ICE,Action.INSPECT,32);frame++) {
                double t=AnimationPlayback.authored(ICE,Action.INSPECT,frame/3.0,32);
                var point=renderedPoint(Action.INSPECT,t,side,slim,part);
                if(previous!=null) {
                    var step=new Vector3f(point).sub(previous);
                    assertTrue(step.length()<.095,"60fps step "+step.length()+" at "+t+" part "+part);
                    if(velocity!=null)assertTrue(step.distance(velocity)<.014,
                        "adjacent rendered steps must blend: "+step.distance(velocity)+" at "+t+" part "+part);
                    velocity=step;
                }
                previous=point;
            }
        }
    }

}
