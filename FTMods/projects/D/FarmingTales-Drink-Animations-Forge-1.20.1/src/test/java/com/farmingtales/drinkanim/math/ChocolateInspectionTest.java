package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class ChocolateInspectionTest {
    private static final DrinkProfile CHOC=DrinkCatalog.find("papercraft_magic_decoration:diplomatico_coffee");
    private static BottleRig.Frame rig(double t,int side,boolean slim) {
        return BottleRig.solve(DrinkMotion.sample(CHOC,Action.INSPECT,t,32,true),Action.INSPECT,t,32,side,slim,CHOC);
    }
    @Test void inspectionTakesIntactChocolateThenReseatsItWithoutConsumption() {
        assertEquals(156,AnimationPlayback.ticks(CHOC,Action.INSPECT,32));
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            for(double t=18;t<=72;t+=.125) {
                var r=rig(t,side,slim);assertEquals(1,r.assistance(),1e-8);
                assertActualCornerInsidePalm(r,side,slim);
                assertEquals(r,ServingAnimations.sway(CHOC,Action.INSPECT,t,r,.3,-.3));
                assertEquals(0,ServingAnimations.propStage(CHOC,Action.INSPECT,t));
                assertTrue(ServingAnimations.propVisible(CHOC,Action.INSPECT,t,32,false));
            }
            var shown=rig(43,side,slim);
            assertTrue(shown.cork().position().distance(ServingAnimations.seated(CHOC,shown).position())>.25);
            var reseated=rig(84,side,slim);
            assertEquals(0,ServingAnimations.seated(CHOC,reseated).position().distance(reseated.cork().position()),1e-6);
            assertEquals(0,reseated.assistance());
            assertEquals(DrinkMotion.sample(CHOC,Action.IDLE,0,32,true),DrinkMotion.sample(CHOC,Action.INSPECT,104,32,true));
        }
    }
    private static void assertActualCornerInsidePalm(BottleRig.Frame r,int side,boolean slim) {
        // A real bottom vertex of original_8, rotated around its exported pivot.
        Vector3f corner=new Vector3f(side>0?4:-5,-1,side>0?4:-2).sub(1,0,-2);
        new Quaternionf().rotationY((float)Math.toRadians(22.5)).transform(corner).add(1,0,-2).mul(CHOC.renderScale()/16);
        r.cork().rotation().transform(corner).add(r.cork().position());
        new Quaternionf(r.helper().rotation()).conjugate().transform(corner.sub(r.helper().position())).mul(16/BottleRig.ARM_SCALE);
        assertTrue(corner.y<-.60&&corner.y>-.70,"actual chocolate corner is visibly embedded 0.65 skin pixels: "+corner);
        assertTrue(Math.abs(corner.x)<(slim?1.5:2)&&Math.abs(corner.z)<2,"contact lies inside the bare skin palm: "+corner);
    }
    @Test void fullChocolateAndSleevesClearTheCupThroughoutInspection() throws Exception {
        var solid=PopsicleClearanceTest.model(CHOC.bodyModel(),false,CHOC);
        var food=PopsicleClearanceTest.model(ServingAnimations.models(CHOC)[0],true,CHOC);
        float worstFood=100,worstCup=100,worstSleeve=100;
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(double t=0;t<=104;t+=.125) {
            var r=rig(t,side,slim);var sleeve=PopsicleClearanceTest.sleeve(r.helper(),slim);
            for(var a:food) {
                for(var b:solid) {
                    float gap=PopsicleClearanceTest.gap(a.world(r.cork()),b.world(r.bottle()));
                    worstFood=Math.min(worstFood,gap);
                    assertTrue(gap>0,"chocolate/cup at "+t+" side "+side+" gap="+gap);
                }
                if(r.assistance()>0) {
                    float gap=PopsicleClearanceTest.gap(a.world(r.cork()),sleeve);worstSleeve=Math.min(worstSleeve,gap);
                    // Intentional palm contact includes the sleeve's 0.25px inflation.
                    // The slab must not sink farther than that plus the requested inset.
                    assertTrue(gap>-.043,"only the retained corner may enter the palm at "+t+" side "+side+" gap="+gap);
                }
            }
            if(r.assistance()>0)for(var b:solid) {
                float gap=PopsicleClearanceTest.gap(sleeve,b.world(r.bottle()));worstCup=Math.min(worstCup,gap);
                assertTrue(gap>.003,"sleeve/cup at "+t+" side "+side+" slim="+slim+" gap="+gap);
            }
        }
        System.out.println("Chocolate gaps food/cup="+worstFood+", sleeve/cup="+worstCup+", food/sleeve="+worstSleeve);
    }
    @Test void renderedChocolateAndBothHandsMoveContinuously() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(int part=0;part<4;part++) {
            Vector3f previous=null,velocity=null;
            for(int frame=0;frame<=468;frame++) {
                double t=AnimationPlayback.authored(CHOC,Action.INSPECT,frame/3.0,32);var r=rig(t,side,slim);
                var camera=DrinkMotion.camera(CHOC,Action.INSPECT,t,32).rotation();
                var shift=BottleFraming.offset(r,Action.INSPECT,t,32,true,CHOC,camera);
                var rim=camera.transform(r.bottle().rotation().transform(new Vector3f(0,8-4.8f,0).mul(CHOC.renderScale()/16)).add(r.bottle().position()).add(shift));
                assertTrue(rim.y/-rim.z>-.85,"cup remains visible t="+t+" side="+side+" rim="+rim);
                if(t>=26&&t<=62)assertTrue(rim.y/-rim.z>-.65,"cup remains in view during display "+t+" side="+side);
                if(t>=38&&t<=50) {
                    var foodCenter=camera.transform(new Vector3f(r.cork().position()).add(shift));
                    assertTrue(foodCenter.y/-foodCenter.z>-.55,"chocolate display stays in view t="+t+" side="+side+" point="+foodCenter);
                }
                var at=part==0?r.bottle():part==1?r.helper():part==2?r.cork():r.hand();
                var point=camera.transform(at.rotation().transform(new Vector3f(0,part==2?.1f:0,0)).add(at.position()).add(shift));
                if(previous!=null) {
                    var step=new Vector3f(point).sub(previous);assertTrue(step.length()<.095,"step at "+t);
                    if(velocity!=null)assertTrue(step.distance(velocity)<.014,"acceleration side="+side+" slim="+slim+" at "+t+" part "+part+" ="+step.distance(velocity));
                    velocity=step;
                }
                previous=point;
                if(r.assistance()>.25) {
                    var elbow=camera.transform(r.helper().rotation().transform(new Vector3f(0,-12,0).mul(BottleRig.ARM_SCALE/16)).add(r.helper().position()).add(shift));
                    assertTrue(side*elbow.x<-.06,"helper on own side at "+t+" elbow="+elbow);
                }
            }
        }
    }
    @Test void inspectionInertiaCannotDipChocolateThroughTheRim() throws Exception {
        var solid=PopsicleClearanceTest.model(CHOC.bodyModel(),false,CHOC);
        var food=PopsicleClearanceTest.model(ServingAnimations.models(CHOC)[0],true,CHOC);
        for(int side:new int[]{-1,1}) {
            var spring=new DecorationMotion.State();
            for(int frame=0;frame<=468;frame++) {
                double t=AnimationPlayback.authored(CHOC,Action.INSPECT,frame/3.0,32);var r=rig(t,side,false);
                spring.advance(frame/60.0,r.bottle().position(),r.bottle().rotation(),0,0);
                r=ServingAnimations.sway(CHOC,Action.INSPECT,t,r,spring.x(),spring.z());
                for(var a:food)for(var b:solid)assertTrue(PopsicleClearanceTest.gap(a.world(r.cork()),b.world(r.bottle()))>0,"dynamic chocolate/cup at "+t+" side="+side);
            }
        }
    }
    @Test void crumbsAppearAtEachBiteFallAwayAndNeverAppearInInspection() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(int variant=0;variant<4;variant++) {
            for(int bite:ServingAnimations.bites(CHOC)) {
                assertTrue(ChocolateCrumbs.sample(CHOC,Action.DRINK,bite-.01,32,side,slim,variant).isEmpty());
                var start=ChocolateCrumbs.sample(CHOC,Action.DRINK,bite,32,side,slim,variant);
                assertEquals(8,start.size());
                assertEquals(start,ChocolateCrumbs.sample(CHOC,Action.DRINK,bite,32,side,slim,variant),"multiple rendering passes must not duplicate particles");
                var falling=ChocolateCrumbs.sample(CHOC,Action.DRINK,bite+5,32,side,slim,variant);
                assertFalse(falling.isEmpty());
                for(var crumb:falling) {
                    assertTrue(crumb.position().y<start.get(crumb.index()).position().y);
                    assertTrue(crumb.position().z<-.28,"crumbs clear camera near plane");
                    assertTrue(Math.abs(crumb.position().x/-crumb.position().z)<.8&&crumb.position().y/-crumb.position().z>-.85,"visible bite particles");
                }
                assertTrue(ChocolateCrumbs.sample(CHOC,Action.DRINK,bite+9,32,side,slim,variant).isEmpty());
                assertTrue(ChocolateCrumbs.sample(CHOC,Action.INSPECT,bite,32,side,slim,variant).isEmpty());
            }
        }
        assertTrue(ChocolateCrumbs.sample(DrinkCatalog.find("papercraft_magic_decoration:long_island_popsicle_tea"),Action.DRINK,15,32,1,true,0).isEmpty());
    }
}
