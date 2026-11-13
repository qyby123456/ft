package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class ServingAnimationsTest {
    private static final DrinkProfile ICE=DrinkCatalog.find("papercraft_magic_decoration:long_island_popsicle_tea");
    private static final DrinkProfile CHOC=DrinkCatalog.find("papercraft_magic_decoration:diplomatico_coffee");
    private static BottleRig.Frame rig(DrinkProfile p,double t,int side,boolean slim) {
        return BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,p);
    }
    @Test void coffeesHaveNoSpoonAndUseOrdinaryCupDrinking() {
        for(var p:DrinkCatalog.all())if(CupAnimations.coffeePreview(p)) {
            assertFalse(ServingAnimations.owns(p));assertEquals("animation/cups/empty",p.capModel());
            assertEquals(19,AnimationPlayback.raiseTicks(p));
            for(double t=0;t<140;t+=.2)assertEquals(CupAnimations.sample(p,Action.DRINK,t,32,true).fill(),DrinkMotion.sample(p,Action.DRINK,t,32,true).fill());
            assertEquals(4,AnimationSoundTimeline.drinking(p,32).size());
        }
    }
    @Test void bitesRemoveFoodBeforeLiquidAndMatchAudibleCues() {
        for(var p:new DrinkProfile[]{ICE,CHOC}) {
            var cues=AnimationSoundTimeline.drinking(p,32);
            var bites=cues.stream().filter(c->c.event().endsWith("_bite")).toList();
            assertEquals(p==ICE?3:2,bites.size());
            for(int i=0;i<bites.size();i++) {
                int tick=bites.get(i).tick();
                if(p==ICE)assertEquals(i+1,ServingAnimations.propStage(p,Action.DRINK,tick));
                assertTrue(bites.get(i).volume()>.5);
            }
            for(double t=0;t<=48;t+=.125)assertEquals(.8,DrinkMotion.sample(p,Action.DRINK,t,32,true).fill(),1e-7);
            assertTrue(cues.stream().filter(c->c.event().equals("gulp")).allMatch(c->c.tick()>bites.get(bites.size()-1).tick()));
            assertEquals(0,ServingAnimations.propStage(p,Action.DRINK,130),"replacement must have an intact garnish");
            assertFalse(ServingAnimations.propVisible(p,Action.DRINK,200,32,false));
        }
    }
    @Test void gripFollowsTheFoodForBothArmWidthsAndHands() {
        for(var p:new DrinkProfile[]{ICE,CHOC})for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            
            for(double t=ServingAnimations.GRASP_TICK;t<(p==ICE?ServingAnimations.RELEASE_TICK:25);t+=.1) {
                var r=rig(p,t,side,slim);
                if(p==ICE) {
                    PopsicleClearanceTest.assertWoodGripped(r,slim);
                } else {
                    var grip=r.cork().rotation().transform(ServingAnimations.gripPoint(p)).add(r.cork().position());
                    var palm=new Quaternionf(r.helper().rotation()).conjugate().transform(grip.sub(r.helper().position()));
                    assertTrue(palm.length()<1e-6,"chocolate retains its hand-end contact");
                    // The retained lower corner supports a diagonal plane through
                    // (6.069, -1). Check the entire sleeve against that food surface.
                    float nearest=Float.POSITIVE_INFINITY;
                    for(float x:new float[]{slim?-1.75f:-2.25f,slim?1.75f:2.25f})
                    for(float y:new float[]{-12.25f,.25f})for(float z:new float[]{-2.25f,2.25f}) {
                        var corner=r.helper().rotation().transform(new Vector3f(x,y,z).mul(BottleRig.ARM_SCALE/16)).add(r.helper().position());
                        new Quaternionf(r.cork().rotation()).conjugate().transform(corner.sub(r.cork().position())).mul(16/p.renderScale());
                        nearest=Math.min(nearest,.55f*corner.x-.83516465f*corner.y);
                    }
                    double edge=.55*6.069+.83516465;
                    assertTrue(nearest>edge&&nearest<edge+.09,"retained corner must touch without piercing the sleeve: "+nearest);
                }
            }
        }
    }
    @Test void trajectoriesHaveNoTeleportationOrVelocityBreaks() {
        for(var p:new DrinkProfile[]{ICE,CHOC})for(int side:new int[]{1,-1}) {
            for(double t:new double[]{0.5,4,5,8,9,14,15,22,25,30,34,37,39,41,47}) {
                var a=rig(p,t-.0001,side,true);var b=rig(p,t+.0001,side,true);
                assertTrue(a.hand().position().distance(b.hand().position())<.001,p.id()+" carrying hand at "+t);
                assertTrue(a.helper().position().distance(b.helper().position())<.001,p.id()+" helper at "+t);
                if(ServingAnimations.propVisible(p,Action.DRINK,t,32,true))
                    assertTrue(a.cork().position().distance(b.cork().position())<.001,p.id()+" prop at "+t);
            }
        }
    }
    @Test void takingFoodChangesFromLiftingToTurningContinuously() {
        for(var p:new DrinkProfile[]{ICE,CHOC})for(int side:new int[]{1,-1}) {
            float dt=.01f;
            int clear=ServingAnimations.CLEAR_TICK;
            var before=rig(p,clear-dt,side,true).cork().position();
            var at=rig(p,clear,side,true).cork().position();
            var after=rig(p,clear+dt,side,true).cork().position();
            var incoming=new Vector3f(at).sub(before).div(dt);
            var outgoing=new Vector3f(after).sub(at).div(dt);
            if(p==CHOC)assertTrue(incoming.length()>.025,"chocolate extraction retains its momentum");
            assertTrue(incoming.distance(outgoing)<.003,"no velocity jump at the clearance point");
        }
    }
    @Test void eachBiteWithdrawsFromTheMouthWithElbowFollowThrough() {
        for(var p:new DrinkProfile[]{ICE,CHOC})for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            int[] bites=ServingAnimations.bites(p);
            for(int i=0;i<bites.length-1;i++) {
                var mouth=rig(p,bites[i],side,slim);var away=rig(p,bites[i]+2.8,side,slim);
                double travel=away.cork().position().distance(mouth.cork().position());
                assertTrue(travel>(p==ICE?.18:.025)&&travel<(p==ICE?.55:.13),"bite recovery distance: "+travel);
                var cam=DrinkMotion.camera(p,Action.DRINK,bites[i],32).rotation();
                var q=cam.transform(new Vector3f(mouth.helper().position()).add(BottleFraming.offset(mouth,Action.DRINK,bites[i],32,true,p,cam)));
                cam=DrinkMotion.camera(p,Action.DRINK,bites[i]+2.8,32).rotation();
                var r=cam.transform(new Vector3f(away.helper().position()).add(BottleFraming.offset(away,Action.DRINK,bites[i]+2.8,32,true,p,cam)));
                double screenDistance=Math.hypot(q.x/-q.z-r.x/-r.z,q.y/-q.z-r.y/-r.z);
                assertTrue(screenDistance>.015&&screenDistance<(p==ICE?1.5:.15),p.id()+" restrained screen movement: "+screenDistance);
                double elbow=new Quaternionf(mouth.helper().rotation()).difference(away.helper().rotation()).angle();
                assertTrue(elbow>Math.toRadians(.5)&&elbow<Math.toRadians(p==ICE?55:6),
                    "the wrist follows the food as it withdraws from the mouth");
            }
        }
    }
    @Test void popsiclePointsTowardTheMouthWhileTheForearmEntersFromBelow() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            for(double t=14;t<=30;t+=.1) {
                var r=rig(ICE,t,side,slim);
                var tip=r.cork().rotation().transform(new Vector3f(0,-7.852976f,-3.252809f).normalize());
                assertTrue(tip.z>.60,"the frozen end points forward while withdrawal turns it up into view");
                var arm=r.helper().rotation().transform(new Vector3f(0,1,0));
                assertTrue(arm.y>.55&&arm.x*side>.15,"forearm must enter from below with no inward flip");
            }
        }
    }
    @Test void stickLeavesMouthWithForwardVelocityAndGravityAfterThirdBite() {
        for(int side:new int[]{1,-1}) {
            int spit=ServingAnimations.SPIT_TICK;
            assertEquals(3,ServingAnimations.propStage(ICE,Action.DRINK,spit));
            var a=rig(ICE,spit,side,true);var b=rig(ICE,spit+2,side,true);var c=rig(ICE,spit+4,side,true);
            assertTrue(b.cork().position().z<a.cork().position().z-.30);
            assertTrue(b.cork().position().y<a.cork().position().y,"spit must not launch the wood upward");
            assertTrue(Math.abs(b.cork().position().x-a.cork().position().x)<.06,"spit is forward, not sideways");
            assertTrue(c.cork().position().y-b.cork().position().y<b.cork().position().y-a.cork().position().y);
            assertTrue(c.cork().position().distance(c.helper().position())>.5);
            assertTrue(ServingAnimations.propVisible(ICE,Action.DRINK,47,32,true));
            assertFalse(ServingAnimations.propVisible(ICE,Action.DRINK,48,32,true));
        }
    }
    @Test void completeWoodSurvivesEveryBiteAndStaysStillAtLipsAfterHandRelease() throws Exception {
        com.google.gson.JsonObject wood=null;
        for(String model:ServingAnimations.models(ICE))try(var reader=new java.io.InputStreamReader(
                getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+model+".json"))) {
            var elements=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements");
            var current=elements.get(0).getAsJsonObject();
            assertEquals("original_0",current.get("name").getAsString());
            if(wood!=null)assertEquals(wood,current,"wood geometry must never shrink with edible stages");
            wood=current;
            assertEquals(9,current.getAsJsonArray("to").get(1).getAsDouble()-current.getAsJsonArray("from").get(1).getAsDouble(),1e-5);
        }
        for(int side:new int[]{-1,1}) {
            var last=rig(ICE,30,side,true);var release=rig(ICE,ServingAnimations.RELEASE_TICK,side,true);
            var held=rig(ICE,ServingAnimations.SPIT_TICK-.01,side,true);
            PopsicleClearanceTest.assertWoodGripped(release,true);
            assertTrue(new Quaternionf(last.cork().rotation()).difference(release.cork().rotation()).angle()<Math.toRadians(20),"no broadside stick presentation");
            assertEquals(0,release.cork().position().distance(held.cork().position()),1e-6);
            assertTrue(Math.abs(release.cork().rotation().dot(held.cork().rotation()))> .99999,"wood cannot rotate itself while held at the lips");
            assertTrue(held.cork().position().distance(held.helper().position())>.20,"hand clears the mouth before spitting");
        }
    }
    @Test void popsiclePickupIsDeliberateAndChocolateRetainsItsPace() {
        for(var p:new DrinkProfile[]{ICE,CHOC}) {
            double secondsPerTick=AnimationPlayback.raiseTicks(p)/960.0;
            assertTrue(ServingAnimations.GRASP_TICK*secondsPerTick<=(p==ICE?.5:.4));
            assertTrue(ServingAnimations.CLEAR_TICK*secondsPerTick<=(p==ICE?1:.8));
            int[] b=ServingAnimations.bites(p);
            if(p==ICE)assertEquals(1.75,b[0]*secondsPerTick,1e-6);
            else assertTrue(b[0]*secondsPerTick<1.35);
            for(int i=1;i<b.length;i++)assertEquals(p==ICE?1:.6458333,(b[i]-b[i-1])*secondsPerTick,1e-6);
        }
    }
    @Test void everyBiteHasADelayedSwallowBeforeTheNextMouthful() {
        for(var p:new DrinkProfile[]{ICE,CHOC}) {
            var swallows=ServingAnimations.sounds(p,32).stream().filter(c->c.event().equals("snack_swallow")).toList();
            int[] bites=ServingAnimations.bites(p);assertEquals(bites.length,swallows.size());
            for(int i=0;i<bites.length;i++) {
                assertTrue(swallows.get(i).tick()>bites[i]);
                assertTrue(swallows.get(i).tick()<(i+1<bites.length?bites[i+1]:48));
                var before=ServingAnimations.camera(p,Action.DRINK,bites[i]+1.5,32);
                var during=ServingAnimations.camera(p,Action.DRINK,bites[i]+3.35,32);
                assertTrue(Math.abs(during.pitch()-before.pitch())>.20,"swallow needs a small distinct head response");
            }
        }
    }
    @Test void iceSwaysWithMovementButLocksToTheGraspWhenExtracted() {
        var r=rig(ICE,0,1,true);
        var sway=ServingAnimations.sway(ICE,Action.IDLE,0,r,.28,-.26);
        assertTrue(new Quaternionf(r.cork().rotation()).difference(sway.cork().rotation()).angle()>.04);
        assertTrue(new Quaternionf(r.cork().rotation()).difference(sway.cork().rotation()).angle()<.20);
        var held=rig(ICE,22,1,true);
        assertEquals(held,ServingAnimations.sway(ICE,Action.DRINK,22,held,.28,-.26));
    }
    @Test void coffeeInspectionKeepsTheOrdinaryHandleGripAndShowsTheSurface() {
        for(var p:DrinkCatalog.all())if(CupAnimations.coffeePreview(p))for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            var idle=BottleRig.solve(DrinkMotion.sample(p,Action.IDLE,0,32,true),Action.IDLE,0,32,side,slim,p);
            var idleInverse=new Quaternionf(idle.bottle().rotation()).conjugate();
            Vector3f reference=idleInverse.transform(new Vector3f(idle.hand().position()).sub(idle.bottle().position()));
            Quaternionf carryWrist=new Quaternionf(idleInverse).mul(idle.hand().rotation());
            var modelContact=new Vector3f(reference).mul(16/p.renderScale()).add(8,4.8f,8);
            float handlePivot=p.id().endsWith(":white_rabbit_mocha")?4.5f:4.25f;
            var handleContact=new Quaternionf().rotationZ((float)Math.toRadians(-22.5))
                .transform(modelContact.sub(8+side*(handlePivot-8),5,8));
            if(side==1) {
                assertEquals(-2.5,handleContact.x,1e-5,"palm centre must meet the handle upright");
                assertEquals(-2,handleContact.y,1e-5);
                assertEquals(0,handleContact.z,1e-5);
            }
            double min=1000,max=-1000;
            for(double t=0;t<=(ServingAnimations.popsicle(p)?PopsicleMotion.INSPECT_TICKS:104);t+=.1) {
                var pose=DrinkMotion.sample(p,Action.INSPECT,t,32,true);
                var r=BottleRig.solve(pose,Action.INSPECT,t,32,side,slim,p);
                assertEquals(0,r.assistance(),1e-8);
                var local=new Quaternionf(r.bottle().rotation()).conjugate().transform(new Vector3f(r.hand().position()).sub(r.bottle().position()));
                assertTrue(reference.distance(local)<1e-6,"carrying palm must not slide");
                var wrist=new Quaternionf(r.bottle().rotation()).conjugate().mul(r.hand().rotation());
                assertEquals(1,Math.abs(carryWrist.dot(wrist)),1e-5,"inspection must keep the ordinary carrying grip");
                assertTrue(new Quaternionf(idle.hand().rotation()).difference(r.hand().rotation()).angle()<Math.toRadians(36),
                    "inspection must not flip the carrying forearm");
                min=Math.min(min,pose.pitch());max=Math.max(max,pose.pitch());
                var next=DrinkMotion.sample(p,Action.INSPECT,t+.1,32,true);
                assertTrue(Math.abs(next.yaw()-pose.yaw())<.8,"no sudden wrist turn");
            }
            assertTrue(max-min>18&&max<24,"present the surface without tipping a hot mug excessively");
        }
    }
    @Test void popsiclePalmTurnsBeforeContactWhileCupStaysNearlyUpright() {
        assertTrue(Math.abs(ServingAnimations.sample(ICE,Action.DRINK,4,32,true).roll())<6,"bring the glass within reach without a large tilt");
        assertTrue(Math.abs(ServingAnimations.sample(ICE,Action.DRINK,16,32,true).roll())<4,"return the cup upright after extraction");
        for(int side:new int[]{-1,1}) {
            var before=rig(ICE,0,side,true).helper().rotation();
            var grasp=rig(ICE,4,side,true).helper().rotation();
            var clear=rig(ICE,8,side,true).helper().rotation();
            var mouth=rig(ICE,14,side,true).helper().rotation();
            assertEquals(1,Math.abs(before.dot(grasp)),1e-6,"palm is oriented for contact before reaching the wood");
            assertTrue(new Quaternionf(grasp).difference(clear).angle()<Math.PI/2,"the grasp rotates within a quarter turn during extraction");
            assertTrue(new Quaternionf(clear).difference(mouth).angle()>.3);
            var r=rig(ICE,4,side,true);
            var forearm=r.helper().rotation().transform(new Vector3f(0,-8,0).mul(BottleRig.ARM_SCALE/16)).add(r.helper().position());
            assertTrue((r.helper().position().x-forearm.x)*side>.10,
                "the free forearm must approach from its own side, with the palm turned inward");
        }
    }
    @Test void chocolateHasRestrainedSeatedSwayButNoSlipWhileHeld() {
        var seated=rig(CHOC,0,1,true);
        var moving=ServingAnimations.sway(CHOC,Action.IDLE,0,seated,.3,-.3);
        double angle=new Quaternionf(seated.cork().rotation()).difference(moving.cork().rotation()).angle();
        assertTrue(angle>.01&&angle<.08);
        var held=rig(CHOC,20,1,true);
        assertEquals(held,ServingAnimations.sway(CHOC,Action.DRINK,20,held,.3,-.3));
    }
    @Test void eatingBreathingAndCameraRemainSubtleWithoutDetachingProps() {
        for(var p:new DrinkProfile[]{ICE,CHOC})for(int variant=0;variant<4;variant++) {
            double cameraPeak=0;
            for(double t=9;t<32;t+=.2) {
                var r=rig(p,t,1,true);float grip=r.helper().position().distance(r.cork().position());
                r=EatingMotion.apply(r,p,Action.DRINK,t,32,variant,1);
                r=BreathingMotion.apply(r,t*2,1);
                assertEquals(grip,r.helper().position().distance(r.cork().position()),1e-6);
                var cam=DrinkMotion.camera(p,Action.DRINK,t,32,variant);
                cameraPeak=Math.max(cameraPeak,Math.abs(cam.pitch()));
            }
            assertTrue(cameraPeak>.04&&cameraPeak<1,"subtle visible camera response");
        }
    }
}

