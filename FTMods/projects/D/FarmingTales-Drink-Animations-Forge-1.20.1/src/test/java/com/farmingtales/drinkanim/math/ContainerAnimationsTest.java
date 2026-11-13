package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class ContainerAnimationsTest {
    private static final List<DrinkProfile> ITEMS=DrinkCatalog.all().stream().filter(p->p.id().startsWith("farmingtales:")&&ContainerAnimations.owns(p)&&!p.cup()).toList();
    @Test void nineItemsUseSevenIndependentlyAuthoredPerformances() {
        assertEquals(9,ITEMS.size());
        assertEquals(7,ITEMS.stream().map(ContainerAnimations::family).distinct().count());
        for(var action:List.of(Action.DRAW,Action.STOW,Action.INSPECT,Action.DRINK)) {
            var curves=new HashSet<String>();
            for(var family:ContainerAnimations.Family.values()) {
                var p=ITEMS.stream().filter(i->ContainerAnimations.family(i)==family).findFirst().orElseThrow();
                StringBuilder signature=new StringBuilder();
                int end=action==Action.DRAW?19:action==Action.STOW?20:action==Action.INSPECT?104:48;
                for(int t=0;t<=end;t++)signature.append(DrinkMotion.sample(p,action,t,16,true));
                assertTrue(curves.add(signature.toString()),"A family accidentally shares the whole "+action+" performance: "+family);
            }
        }
        for(String[] pair:new String[][]{{"mozhao","mozhao_p"},{"light_beer","wheatbeer"}}) {
            var a=DrinkCatalog.find("farmingtales:"+pair[0]);var b=DrinkCatalog.find("farmingtales:"+pair[1]);
            for(var action:Action.values())for(double t=0;t<150;t+=.5) {
                assertEquals(DrinkMotion.sample(a,action,t,16,true),DrinkMotion.sample(b,action,t,16,true));
                assertEquals(DrinkMotion.camera(a,action,t,16),DrinkMotion.camera(b,action,t,16));
                assertEquals(DrinkMotion.assistance(a,action,t),DrinkMotion.assistance(b,action,t));
                assertEquals(DrinkMotion.arm(a,action,t,16,true),DrinkMotion.arm(b,action,t,16,true));
            }
        }
    }
    @Test void waterActuallyLeavesThePalmAndIsCaughtWithoutSpinningTheForearm() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            BottleRig.Frame release=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,30,16,true),Action.INSPECT,30,16,side,slim,p);
            BottleRig.Frame apex=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,39,16,true),Action.INSPECT,39,16,side,slim,p);
            BottleRig.Frame caught=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,48,16,true),Action.INSPECT,48,16,side,slim,p);
            assertTrue(apex.bottle().position().y-apex.hand().position().y>.30,"Bottle must be visibly above the waiting palm");
            assertTrue(Math.abs(release.hand().rotation().dot(apex.hand().rotation()))>.95,"Palm must not follow the airborne spin");
            assertTrue(Math.abs(release.bottle().rotation().dot(apex.bottle().rotation()))<.01,"Halfway through the complete spin");
            assertTrue(Math.abs(release.bottle().rotation().dot(caught.bottle().rotation()))>1-1e-6,"Catch upright, never unwind the spin");
            assertEquals(release.hand().position().distance(release.bottle().position()),caught.hand().position().distance(caught.bottle().position()),1e-5);
            for(double t:List.of(30.0,48.0)) {
                var before=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,t-.01,16,true),Action.INSPECT,t-.01,16,side,slim,p);
                var after=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,t+.01,16,true),Action.INSPECT,t+.01,16,side,slim,p);
                assertTrue(before.hand().position().distance(after.hand().position())<.004,"No palm teleport on release/catch");
            }
        }
    }
    @Test void brandyUsesBothHandsThroughoutTheRaiseAndEverySip() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        assertTrue(DrinkMotion.sample(p,Action.INSPECT,25,16,true).y()-ContainerAnimations.rest(p).y()>.12,"Small centered lift");
        for(int duration:new int[]{16,40,100})for(double t=48;t<48+BottleMotion.sipTicks(duration);t+=.5) {
            assertEquals(1,DrinkMotion.assistance(p,Action.DRINK,t,duration),1e-8,"Two hands during every sip");
            assertEquals(1,ContainerAnimations.support(p,Action.DRINK,t,duration),1e-8,"Left hand cradles base after cap removal");
        }
    }
    @Test void brandyPalmsStayOnTheBroadSidesThroughoutLateralRocking() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        double minRoll=0,maxRoll=0;
        for(double t=25;t<=76;t+=.25) {
            var pose=DrinkMotion.sample(p,Action.INSPECT,t,16,true);
            assertTrue(Math.abs(pose.x())<.05,"Shake stays centered");
            assertTrue(pose.y()<-.39&&pose.y()>-.46,"Keep the bottle below the previous skyward height");
            assertEquals(180,pose.yaw(),1e-7);
            minRoll=Math.min(minRoll,pose.roll());maxRoll=Math.max(maxRoll,pose.roll());
            for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
                var rig=BottleRig.solve(pose,Action.INSPECT,t,16,side,slim,p);
                var inverse=new Quaternionf(rig.bottle().rotation()).rotateY((float)Math.toRadians(-side*180)).conjugate();
                var right=inverse.transform(new org.joml.Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                var left=inverse.transform(new org.joml.Vector3f(rig.helper().position()).sub(rig.bottle().position()));
                assertEquals(-right.x,left.x,1e-6);assertEquals(right.y,left.y,1e-6);assertEquals(right.z,left.z,1e-6);
                assertEquals((1.20f-4.8f)*p.renderScale()/16+.08f/16,right.y,1e-6,"Palms support the two lower corners");
                float innerEdge=Math.abs(right.x)-(slim?1.5f:2f)*BottleRig.ARM_SCALE/16;
                assertTrue(Math.abs(innerEdge-(4*p.renderScale()/16-.80f/16))<.03,"Palm matches the closer reference grip");
                var rightAxis=inverse.transform(rig.hand().rotation().transform(new org.joml.Vector3f(0,1,0)));
                var leftAxis=inverse.transform(rig.helper().rotation().transform(new org.joml.Vector3f(0,1,0)));
                assertEquals(-rightAxis.x,leftAxis.x,1e-6);assertEquals(rightAxis.y,leftAxis.y,1e-6);assertEquals(rightAxis.z,leftAxis.z,1e-6);
            }
        }
        assertTrue(minRoll< -5&&maxRoll>5,"Must rock in both directions");
    }
    @Test void waterFlightAcceleratesDownwardAndCatchesBeforeTheOldApex() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        double acceleration=Double.NaN;
        for(double t=31;t<47;t++) {
            double a=waterCentreY(p,t-1);
            double b=waterCentreY(p,t);
            double c=waterCentreY(p,t+1);
            double current=c-2*b+a;
            assertTrue(current<-.007,"Gravity must not ease out during descent");
            if(Double.isFinite(acceleration))assertEquals(acceleration,current,3e-7);
            acceleration=current;
        }
        assertTrue(AnimationPlayback.ticks(p,Action.INSPECT,16)<80);
    }
    @Test void brandyReturnsWithItsCarryGripAsOneAssembly() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1}) {
            var idle=BottleRig.solve(DrinkMotion.sample(p,Action.IDLE,0,16,true),Action.IDLE,0,16,side,slim,p);
            var end=BottleRig.solve(DrinkMotion.sample(p,Action.INSPECT,104,16,true),Action.INSPECT,104,16,side,slim,p);
            assertEquals(0,idle.hand().position().distance(end.hand().position()),1e-6);
            assertTrue(Math.abs(idle.hand().rotation().dot(end.hand().rotation()))>1-1e-6);
            for(var action:List.of(Action.INSPECT,Action.DRINK))for(double t=0;t<=104;t+=.25) {
                var frame=BottleRig.solve(DrinkMotion.sample(p,action,t,16,true),action,t,16,side,slim,p);
                if(action==Action.INSPECT&&t>=85) {
                    var next=BottleRig.solve(DrinkMotion.sample(p,action,t+.01,16,true),action,t+.01,16,side,slim,p);
                    assertTrue(frame.hand().position().distance(next.hand().position())<.001,"No return grip teleport");
                    var localHand=new Quaternionf(frame.bottle().rotation()).conjugate().transform(new org.joml.Vector3f(frame.hand().position()).sub(frame.bottle().position()));
                    var idleHand=new Quaternionf(idle.bottle().rotation()).conjugate().transform(new org.joml.Vector3f(idle.hand().position()).sub(idle.bottle().position()));
                    assertEquals(0,localHand.distance(idleHand),1e-6,"Hand and bottle return as one rigid assembly");
                    var localRotation=new Quaternionf(frame.bottle().rotation()).conjugate().mul(frame.hand().rotation());
                    var idleRotation=new Quaternionf(idle.bottle().rotation()).conjugate().mul(idle.hand().rotation());
                    assertTrue(Math.abs(localRotation.dot(idleRotation))>1-1e-6,"No independent wrist return");
                }
            }
        }
    }
    @Test void brandyCarriesAtTheNormalSideGripAndDrinksCentrallyWithMatchedPalms() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1}) {
            var carry=BottleRig.solve(DrinkMotion.sample(p,Action.IDLE,0,16,true),Action.IDLE,0,16,side,slim,p);
            var local=new Quaternionf(carry.bottle().rotation()).conjugate()
                .transform(new org.joml.Vector3f(carry.hand().position()).sub(carry.bottle().position()));
            assertTrue(local.y>-.02,"Carry with the palm on the body, not beneath the bottle");
            assertTrue(Math.abs(carry.hand().rotation().dot(BottleRig.armRotation(ArmMotion.REST,side,false)))>1-1e-6);
            for(double t=48;t<=88;t+=.25) {
                var pose=DrinkMotion.sample(p,Action.DRINK,t,16,true);
                assertEquals(0,pose.x(),1e-9);assertEquals(180,pose.yaw(),1e-9);assertEquals(0,pose.roll(),1e-9);
                var rig=BottleRig.solve(pose,Action.DRINK,t,16,side,slim,p);
                assertEquals(-rig.hand().position().x,rig.helper().position().x,1e-6);
                assertEquals(rig.hand().position().y,rig.helper().position().y,1e-6);
                assertEquals(rig.hand().position().z,rig.helper().position().z,1e-6);
                var right=rig.hand().rotation().transform(new org.joml.Vector3f(0,1,0));
                var left=rig.helper().rotation().transform(new org.joml.Vector3f(0,1,0));
                assertEquals(-right.x,left.x,1e-6);assertEquals(right.y,left.y,1e-6);assertEquals(right.z,left.z,1e-6);
            }
        }
    }
    @Test void brandyHasDelayedLeanDampedSettlingAndVisibleCameraFollow() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        double low=0,high=0;
        for(double t=30;t<=90;t+=.1) {
            var camera=DrinkMotion.camera(p,Action.INSPECT,t,16);
            low=Math.min(low,camera.yaw());high=Math.max(high,camera.yaw());
        }
        assertTrue(high-low>.10&&high-low<.35,"World camera follows only very slightly while the hands keep rocking");
        double peak=DrinkMotion.sample(p,Action.INSPECT,39,16,true).roll();
        double delayed=DrinkMotion.sample(p,Action.INSPECT,41,16,true).roll();
        assertTrue(delayed<peak,"Lean continues after lateral hand travel reverses");
        assertTrue(Math.abs(DrinkMotion.sample(p,Action.INSPECT,90,16,true).roll())<.35,"Rocking settles before release");
    }
    private static double waterCentreY(DrinkProfile p,double time) {
        var pose=DrinkMotion.sample(p,Action.INSPECT,time,16,true);
        var rig=BottleRig.solve(pose,Action.INSPECT,time,16,1,true,p);
        float centerY=((p.boundsMin()[1]+p.boundsMax()[1])*.5f-4.8f)*p.renderScale()/16;
        return rig.bottle().rotation().transform(new org.joml.Vector3f(0,centerY,0)).add(rig.bottle().position()).y;
    }
    @Test void beerCapIsBittenHeldAtMouthAndSpatAwayBeforeSipping() {
        for(String item:List.of("light_beer","wheatbeer"))for(int side:new int[]{-1,1}) {
            var p=DrinkCatalog.find("farmingtales:"+item);
            assertFalse(DrinkMotion.capDetached(p,Action.DRINK,26.9));
            BottleRig.Frame previous=null;
            for(double t=27;t<=45;t+=.25) {
                var rig=BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,16,true),Action.DRINK,t,16,side,true,p);
                assertEquals(0,rig.assistance(),"No hand removes the beer cap");
                assertTrue(DrinkMotion.capDetached(p,Action.DRINK,t));
                if(previous!=null&&t<=32)assertEquals(0,rig.cork().position().distance(previous.cork().position()),1e-6,"Cap remains at mouth while bottle lowers");
                if(previous!=null&&t>32)assertTrue(side*(rig.cork().position().x-previous.cork().position().x)>0,"Spit cap out to the side");
                previous=rig;
            }
            assertFalse(DrinkMotion.capVisible(p,Action.DRINK,48,16,true));
            assertEquals(1,DrinkMotion.neckReveal(p,Action.DRINK,30,16,true));
        }
    }
    @Test void shakingTheCanIsSealedAndCreatesLiquidInertiaWithoutChangingTheItem() {
        var p=DrinkCatalog.find("farmingtales:mozhao");
        for(int t=0;t<=104;t++) {
            assertEquals(.8,DrinkMotion.sample(p,Action.INSPECT,t,16,true).fill(),1e-8);
            assertEquals(0,DrinkMotion.canOpening(Action.INSPECT,t,16,true));
            assertTrue(DrinkMotion.capVisible(p,Action.INSPECT,t,16,true));
        }
        var spring=new SloshSpring();double peak=0;
        for(int t=0;t<90;t++) {
            spring.tick(InertialGestures.liquid(ContainerAnimations.Family.CAN,t,0),InertialGestures.liquid(ContainerAnimations.Family.CAN,t,2));
            peak=Math.max(peak,spring.energy());
        }
        assertTrue(peak>.12,"Wrist acceleration must excite the liquid, even after a reversal");
        assertTrue(DrinkMotion.sample(p,Action.INSPECT,40,16,true).y()-DrinkMotion.sample(p,Action.INSPECT,44,16,true).y()>.29);
        for(double t=32;t<=58;t+=.25) {
            var pose=DrinkMotion.sample(p,Action.INSPECT,t,16,true);
            assertEquals(.34,pose.x(),1e-7,"Shake vertically, not from left to right");
            assertEquals(-8,pose.roll(),1e-7,"Do not swing the wrist sideways");
        }
        assertTrue(DrinkMotion.tabAngle(Action.DRINK,28,16)>50);
        assertTrue(DrinkMotion.capDetached(p,Action.DRINK,36));
        assertFalse(DrinkMotion.capVisible(p,Action.DRINK,48,16,true));
    }
    @Test void newPerformancesKeepEndpointsAndHideTheWholeBottleBeforeReplacement() {
        for(var p:ITEMS) {
            var rest=DrinkMotion.sample(p,Action.IDLE,0,16,true);
            for(var action:List.of(Action.DRAW,Action.INSPECT,Action.DRINK)) {
                double end=action==Action.DRAW?19:action==Action.INSPECT?104:BottleMotion.totalDrinkTicks(16);
                var last=DrinkMotion.sample(p,action,end,16,true);
                assertEquals(rest.x(),last.x(),1e-6);assertEquals(rest.y(),last.y(),1e-6);assertEquals(rest.z(),last.z(),1e-6);
                assertTrue(Math.abs(BottleRig.rotation(last,1).dot(BottleRig.rotation(rest,1)))>1-1e-6);
                assertEquals(0,DrinkMotion.camera(p,action,end,16).magnitude(),1e-6);
            }
            int end=48+BottleMotion.sipTicks(16);
            for(int t=end+24;t<=end+41;t++) {
                var hidden=DrinkMotion.sample(p,Action.DRINK,t,16,true);
                assertTrue(hidden.y()<-2.3,p.id()+" replacement changes out of sight");
                assertEquals(DrinkMotion.sample(p,Action.DRAW,0,16,true).y(),hidden.y(),1e-7);
            }
            assertEquals(.025,DrinkMotion.sample(p,Action.DRINK,150,16,false).fill(),1e-7);
        }
    }
    @Test void allNewTracksRemainContinuousAndFiniteForBothArmsAndMainHands() {
        for(var p:ITEMS)for(var action:Action.values())for(int side:new int[]{1,-1})for(boolean slim:new boolean[]{false,true}) {
            int end=action==Action.DRAW?19:action==Action.STOW?20:action==Action.INSPECT?104:147;
            BottleRig.Frame previous=null;
            for(double t=0;t<=end;t+=.1) {
                var pose=DrinkMotion.sample(p,action,t,16,true);
                var rig=BottleRig.solve(pose,action,t,16,side,slim,p);
                var camera=DrinkMotion.camera(p,action,t,16);
                var offset=BottleFraming.offset(rig,action,t,16,true,p);
                assertTrue(Float.isFinite(rig.hand().position().length())&&Float.isFinite(offset.length()));
                assertTrue(camera.magnitude()<2.0,p.id()+" camera "+action+" t="+t);
                if(previous!=null)assertTrue(previous.bottle().position().distance(rig.bottle().position())<.06,p.id()+" bottle jump "+action+" t="+t);
                assertEquals(1,rig.hand().rotation().lengthSquared(),1e-5);
                previous=rig;
            }
        }
    }
    @Test void beerNeckStaysUnderTheThreePixelCapAndIsFullyOpaque() throws Exception {
        for(String name:List.of("light_beer","wheatbeer")) {
            var p=DrinkCatalog.find("farmingtales:"+name);
            assertEquals(1,DrinkMotion.neckReveal(p,Action.INSPECT,35,16,true));
            assertEquals(1,DrinkMotion.neckReveal(p,Action.DRINK,30,16,true));
            assertEquals(1,DrinkMotion.neckReveal(p,Action.DRINK,140,16,true));
            String root="/assets/farmingtales_drink_anim/";
            try(var reader=new java.io.InputStreamReader(getClass().getResourceAsStream(root+"models/"+p.neckModel()+".json"))) {
                var model=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("minecraft:cutout",model.get("render_type").getAsString());
                var cube=model.getAsJsonArray("elements").get(0).getAsJsonObject();
                assertEquals(2.98,Math.abs(cube.getAsJsonArray("to").get(1).getAsDouble()-cube.getAsJsonArray("from").get(1).getAsDouble()),1e-7);
                var face=cube.getAsJsonObject("faces").getAsJsonObject("north");
                String key=face.get("texture").getAsString().substring(1);
                String texture=model.getAsJsonObject("textures").get(key).getAsString();
                var im=javax.imageio.ImageIO.read(getClass().getResource("/assets/"+texture.replace(":","/textures/")+".png"));
                var uv=face.getAsJsonArray("uv");
                int x=(int)((uv.get(0).getAsDouble()+uv.get(2).getAsDouble())/32*im.getWidth());
                int y=(int)((uv.get(1).getAsDouble()+uv.get(3).getAsDouble())/32*im.getHeight());
                assertEquals(255,im.getRGB(x,y)>>>24);
            }
        }
    }
}
