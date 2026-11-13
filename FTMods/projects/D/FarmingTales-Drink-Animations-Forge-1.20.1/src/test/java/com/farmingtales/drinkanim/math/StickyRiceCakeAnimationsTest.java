package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class StickyRiceCakeAnimationsTest {
    private static final DrinkProfile CAKE=DrinkCatalog.find(StickyRiceCakeAnimations.ID);
    private static final String TEXTURE="farmingtales_drink_anim:textures/snacks/sticky_rice_cake.png";
    private static final int USE_TICKS=32, SIP_END=88;

    @Test void replacesTheFlatSpriteWithFourBiteStagesOfPaintedGeometry() throws Exception {
        assertNotNull(CAKE);
        assertEquals(DrinkProfile.Style.FOOD,CAKE.style());
        assertTrue(CAKE.food()&&!CAKE.cup()&&!CAKE.can()&&!CAKE.cork());
        assertTrue(CAKE.liquid().isEmpty(),"the cake carries no liquid");
        // The upstream inventory sprite stays exactly as the source mod ships it.
        assertNull(getClass().getResource("/assets/kaleidoscope_cookery/models/item/sticky_rice_cake.json"),
            "the original flat inventory icon must not be replaced");
        assertNotNull(getClass().getResource("/assets/"+TEXTURE.replace(':','/')));
        for(String stage:StickyRiceCakeAnimations.models()) {
            assertEquals(1,elements(stage),"one box per stage");
            assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/models/"+stage+".json"));
        }
        assertEquals("animation/snacks/sticky_rice_cake/whole",CAKE.bodyModel());
    }

    @Test void stageModelsNameThePackedAtlasSpriteNotAFilePath() throws Exception {
        for(String stage:StickyRiceCakeAnimations.models())
            for(var entry:model(stage).getAsJsonObject("textures").entrySet()) {
                String reference=entry.getValue().getAsString();
                assertEquals("farmingtales_drink_anim:snacks/sticky_rice_cake",reference,
                    "model textures name the atlas sprite, not a textures/….png file path");
                assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/textures/"
                    +reference.split(":")[1]+".png"),reference);
            }
    }

    @Test void everyAuthoredFaceSamplesPaintedTexels() throws Exception {
        var image=javax.imageio.ImageIO.read(getClass().getResource("/assets/"+TEXTURE.replace(':','/')));
        assertNotNull(image);
        for(String stage:StickyRiceCakeAnimations.models()) {
            var faces=element(stage).getAsJsonObject().getAsJsonObject("faces");
            for(var entry:faces.entrySet()) {
                var uv=entry.getValue().getAsJsonObject().getAsJsonArray("uv");
                int x0=(int)(uv.get(0).getAsFloat()/16f*image.getWidth());
                int y0=(int)(uv.get(1).getAsFloat()/16f*image.getHeight());
                int x1=Math.min(image.getWidth()-1,(int)(uv.get(2).getAsFloat()/16f*image.getWidth())-1);
                int y1=Math.min(image.getHeight()-1,(int)(uv.get(3).getAsFloat()/16f*image.getHeight())-1);
                assertTrue(x1>=x0&&y1>=y0,stage+" "+entry.getKey()+" has an empty face");
                for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)
                    assertEquals(255,(image.getRGB(x,y)>>>24)&255,stage+" "+entry.getKey()+" hole at "+x+","+y);
            }
        }
    }

    @Test void inspectionTurnsTheCakeTwiceAndReturnsToTheSameHold() {
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,0,USE_TICKS,true));
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,
            StickyRiceCakeAnimations.INSPECT_TICKS,USE_TICKS,true));
        assertEquals(0,DrinkMotion.assistance(CAKE,BottleMotion.Action.INSPECT,8,USE_TICKS));
        assertTrue(DrinkMotion.assistance(CAKE,BottleMotion.Action.INSPECT,24,USE_TICKS)>.9,"the far palm steadies it");
        assertTrue(DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,58,USE_TICKS,true).yaw()>230,"turned past the rim");
        var steady=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,44,USE_TICKS,true),
            BottleMotion.Action.INSPECT,44,USE_TICKS,1,true,CAKE);
        assertTrue(steady.helper().position().y<steady.bottle().position().y,"support comes from underneath");
        assertEquals(6,StickyRiceCakeAnimations.inspectionSounds().size());
    }

    @Test void eatingTakesThreeMouthfulsAndSwapsTheStageForEach() {
        assertEquals(147,(int)AnimationPlayback.authored(CAKE,BottleMotion.Action.DRINK,
            AnimationPlayback.ticks(CAKE,BottleMotion.Action.DRINK,USE_TICKS),USE_TICKS),
            "the controller clock is remapped onto the authored 48 + 40 + 41 + 18 timeline");
        assertArrayEquals(new int[]{57,69,81},StickyRiceCakeAnimations.bites(USE_TICKS));
        assertEquals(StickyRiceCakeAnimations.eatingSounds(USE_TICKS),
            AnimationSoundTimeline.drinking(CAKE,USE_TICKS));
        assertEquals(3,StickyRiceCakeAnimations.eatingSounds(USE_TICKS).stream()
            .filter(c->c.event().equals("sticky_rice_cake_bite")).count());

        assertEquals(.8,DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,57,USE_TICKS,true).fill(),1e-6);
        assertEquals(.55,DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,61,USE_TICKS,true).fill(),1e-6);
        assertEquals(.29,DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,73,USE_TICKS,true).fill(),1e-6);
        assertEquals(.025,DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,85,USE_TICKS,true).fill(),1e-6);

        assertEquals("animation/snacks/sticky_rice_cake/whole",stage(50));
        assertEquals("animation/snacks/sticky_rice_cake/bite1",stage(65));
        assertEquals("animation/snacks/sticky_rice_cake/bite2",stage(75));
        assertEquals("animation/snacks/sticky_rice_cake/bite3",stage(85));
        assertEquals("animation/snacks/sticky_rice_cake/bite3",stage(120),"the empty hand keeps the last crumb");
        assertEquals("animation/snacks/sticky_rice_cake/whole",stage(135),"the replacement is a fresh cake");
    }

    @Test void theCakeOwnsNoClosureAndBitesPushTowardTheLips() {
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT})
            for(int t=0;t<=StickyRiceCakeAnimations.INSPECT_TICKS;t+=4) {
                assertEquals(0,DrinkMotion.neckReveal(CAKE,action,t,USE_TICKS,true),"no neck is ever uncovered");
                assertFalse(DrinkMotion.capDetached(CAKE,action,t),"nothing detaches");
                if(action==BottleMotion.Action.DRINK)assertFalse(DrinkMotion.capVisible(CAKE,action,t,USE_TICKS,true));
            }
        // Every feed carries the cake towards the face; the waiting hold between feeds lets it go.
        for(int bite:StickyRiceCakeAnimations.bites(USE_TICKS))
            assertTrue(DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS,true).z()
                >DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite+5.5,USE_TICKS,true).z(),
                "each bite leans in at "+bite);
    }

    /** The hand feeds the cake: three separate lifts to the lips with a waiting hold between them. */
    @Test void everyMouthfulIsFedToTheLipsAndLoweredAgain() {
        int[] bites=StickyRiceCakeAnimations.bites(USE_TICKS);
        for(int bite:bites) {
            var fed=DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS,true);
            var waiting=DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite+5.5,USE_TICKS,true);
            assertEquals(1,StickyRiceCakeAnimations.reach(BottleMotion.Action.DRINK,bite,USE_TICKS),1e-6);
            assertEquals(0,StickyRiceCakeAnimations.reach(BottleMotion.Action.DRINK,bite+5.5,USE_TICKS),1e-6);
            assertTrue(fed.y()-waiting.y()>.12,"the hand lifts the cake at "+bite);
            assertTrue(fed.z()-waiting.z()>.30,"the mouthful is carried in to the face at "+bite);
            assertTrue(fed.pitch()-waiting.pitch()>22,"the cake tips into the mouth at "+bite);
        }
        // The pauses are shared by the same waiting hold, so the three feeds never merge.
        for(int i=0;i+1<bites.length;i++)
            assertEquals(0,StickyRiceCakeAnimations.reach(BottleMotion.Action.DRINK,
                (bites[i]+bites[i+1])/2.0,USE_TICKS),1e-6);
    }

    /** The far hand takes the food over for the meal and hands the empty cake back at the end. */
    @Test void theFarHandTakesTheCakeOverForTheMeal() {
        int[] bites=StickyRiceCakeAnimations.bites(USE_TICKS);
        assertEquals(0,DrinkMotion.assistance(CAKE,BottleMotion.Action.DRINK,20,USE_TICKS));
        assertEquals(0,StickyRiceCakeAnimations.handover(BottleMotion.Action.DRINK,40,USE_TICKS),1e-6);
        assertTrue(DrinkMotion.assistance(CAKE,BottleMotion.Action.DRINK,48,USE_TICKS)>0.99,
            "the far hand arrives with the waiting hold");
        for(int bite:bites) {
            assertEquals(1,StickyRiceCakeAnimations.handover(BottleMotion.Action.DRINK,bite,USE_TICKS),1e-6);
            assertEquals(1,DrinkMotion.assistance(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS),1e-6);
            var rig=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS,true),
                BottleMotion.Action.DRINK,bite,USE_TICKS,1,false,CAKE);
            var local=new org.joml.Quaternionf(rig.bottle().rotation()).conjugate()
                .transform(new org.joml.Vector3f(rig.helper().position()).sub(rig.bottle().position()));
            // The far palm holds the carrying grip mirrored across the cake's own centre line.
            var expected=BottleRig.idleGripOffset(1,false);
            BottleRig.rotation(BottleMotion.IDLE,1).conjugate().transform(expected);
            expected.set(-expected.x,expected.y,expected.z);
            assertEquals(0,new org.joml.Vector3f(local).distance(expected),1e-5,
                "the far palm mirrors the carrying grip at "+bite);
        }
        assertEquals(0,DrinkMotion.assistance(CAKE,BottleMotion.Action.DRINK,88,USE_TICKS),
            "the empty hand lets go for the stow");
        var stowed=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,20,USE_TICKS,true),
            BottleMotion.Action.DRINK,20,USE_TICKS,1,false,CAKE);
        assertTrue(stowed.helper().position().distance(stowed.bottle().position())>.8,
            "the far hand stays in the pocket until the cake reaches the waiting hold");
    }

    /** The fed cake really rises on screen, and the safety framing barely answers it. */
    @Test void theFeedIsWhatMovesTheCakeAndNotTheSafetyFraming() {
        double lowestBite=Double.MAX_VALUE,highestBite=-Double.MAX_VALUE;
        double lowestRise=Double.MAX_VALUE,highestRise=-Double.MAX_VALUE;
        double lowestFrame=Double.MAX_VALUE,highestFrame=-Double.MAX_VALUE;
        org.joml.Vector3f previous=null;
        for(double t=48;t<=88;t+=.5) {
            var motion=DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,t,USE_TICKS,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.DRINK,t,USE_TICKS,1,true,CAKE);
            rig=EatingMotion.apply(rig,CAKE,BottleMotion.Action.DRINK,t,USE_TICKS,0,1);
            var camera=DrinkMotion.camera(CAKE,BottleMotion.Action.DRINK,t,USE_TICKS,0).rotation();
            var framing=BottleFraming.offset(rig,BottleMotion.Action.DRINK,t,USE_TICKS,true,CAKE,camera);
            // The cake's own biting point: the top of its front face, in the model's local frame.
            float unit=CAKE.renderScale()/16;
            org.joml.Vector3f local=new org.joml.Vector3f(0,(6-4.8f)*unit,(4-8)*unit);
            var seen=camera.transform(rig.bottle().rotation().transform(local)
                .add(rig.bottle().position()).add(framing));
            lowestBite=Math.min(lowestBite,seen.z);highestBite=Math.max(highestBite,seen.z);
            lowestRise=Math.min(lowestRise,seen.y);highestRise=Math.max(highestRise,seen.y);
            lowestFrame=Math.min(lowestFrame,framing.y);highestFrame=Math.max(highestFrame,framing.y);
            if(previous!=null)assertTrue(framing.distance(previous)<.06,"framing jumps at "+t);
            previous=new org.joml.Vector3f(framing);
        }
        assertTrue(highestBite-lowestBite>.25,
            "the far hand carries the cake "+(highestBite-lowestBite)+" blocks towards the face");
        assertTrue(highestRise-lowestRise>.20,
            "the fed cake rises "+(highestRise-lowestRise)+" blocks on screen");
        assertTrue(highestFrame-lowestFrame<.20,
            "the safety framing only answers the lift by "+(highestFrame-lowestFrame));
    }

    /** Mirrors the bamboo spoon guard: a support hand must not push the held food around the frame. */
    @Test void supportPalmDoesNotDragTheCakeOutOfFrame() {
        double deepest=0;
        for(double t=20;t<=96;t+=.5) {
            var motion=DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,t,USE_TICKS,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.INSPECT,t,USE_TICKS,1,true,CAKE);
            rig=EatingMotion.apply(rig,CAKE,BottleMotion.Action.INSPECT,t,USE_TICKS,0,1);
            var camera=DrinkMotion.camera(CAKE,BottleMotion.Action.INSPECT,t,USE_TICKS,0).rotation();
            deepest=Math.min(deepest,BottleFraming.offset(rig,BottleMotion.Action.INSPECT,t,USE_TICKS,true,CAKE,camera).y);
        }
        assertTrue(-deepest<.10,"support palm reframes the cake by "+(-deepest));
    }

    @Test void carryingPalmNeverSlidesOnTheCake() {
        Vector3f contact=null;
        float radius=-1;
        for(int t=10;t<=SIP_END;t+=2) {
            // Inspection turns the cake between the fingers, so the palm keeps its distance from
            // the pivot while the cake spins underneath it.
            var rig=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.INSPECT,t,USE_TICKS,true),
                BottleMotion.Action.INSPECT,t,USE_TICKS,1,false,CAKE);
            var local=new Quaternionf(rig.bottle().rotation()).conjugate()
                .transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
            if(radius<0)radius=local.length();
            assertEquals(radius,local.length(),1e-6,"palm radius changes at INSPECT "+t);
        }
        // Until the far hand takes over, eating is a rigid grip: the palm may not move on the cake.
        for(int t=10;t<=StickyRiceCakeAnimations.bites(USE_TICKS)[0]-16;t+=2) {
            var rig=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,t,USE_TICKS,true),
                BottleMotion.Action.DRINK,t,USE_TICKS,1,false,CAKE);
            var local=new Quaternionf(rig.bottle().rotation()).conjugate()
                .transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
            if(contact==null)contact=local;
            assertEquals(0,contact.distance(local),1e-6,"carrying contact moves at DRINK "+t);
        }
    }

    /** From the hand-over on it is the far palm that is rigid on the cake, not the carrying one. */
    @Test void theFarHandFeedsTheCakeWhileTheCarryingHandWaits() {
        Vector3f farContact=null;
        Vector3f waiting=new Vector3f();
        for(double t=StickyRiceCakeAnimations.bites(USE_TICKS)[0];t<=StickyRiceCakeAnimations.bites(USE_TICKS)[2];t+=2) {
            var rig=BottleRig.solve(DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,t,USE_TICKS,true),
                BottleMotion.Action.DRINK,t,USE_TICKS,1,false,CAKE);
            var farLocal=new Quaternionf(rig.bottle().rotation()).conjugate()
                .transform(new Vector3f(rig.helper().position()).sub(rig.bottle().position()));
            if(farContact==null) {farContact=farLocal;waiting.set(rig.hand().position());}
            assertEquals(0,farContact.distance(farLocal),1e-6,"the far grip slides at "+t);
            assertTrue(new Vector3f(rig.hand().position()).distance(waiting)<.02,
                "the carrying palm follows the food instead of waiting behind at "+t);
        }
    }

    /** The far palm grips from inside the cake, so it is the far arm that has to come into view. */
    @Test void theFarHandIsVisibleWhileItFeedsTheCake() {
        for(int bite:StickyRiceCakeAnimations.bites(USE_TICKS)) {
            var motion=DrinkMotion.sample(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.DRINK,bite,USE_TICKS,1,false,CAKE);
            var camera=DrinkMotion.camera(CAKE,BottleMotion.Action.DRINK,bite,USE_TICKS,0).rotation();
            var framing=BottleFraming.offset(rig,BottleMotion.Action.DRINK,bite,USE_TICKS,true,CAKE,camera);
            float s=CAKE.renderScale()/16;float[] a=CAKE.boundsMin(),b=CAKE.boundsMax();
            float[] low={(a[0]-8)*s,(a[1]-4.8f)*s,(a[2]-8)*s},high={(b[0]-8)*s,(b[1]-4.8f)*s,(b[2]-8)*s};
            var inverse=new org.joml.Quaternionf(rig.bottle().rotation()).conjugate();
            var eye=inverse.transform(new org.joml.Vector3f(framing).negate().sub(rig.bottle().position()));
            // The outer half of the far forearm is what the player actually sees of that hand.
            for(float px:new float[]{-10f,-12.25f}) {
                var point=rig.helper().rotation()
                    .transform(new org.joml.Vector3f(0,px*BottleRig.ARM_SCALE/16,0)).add(rig.helper().position());
                assertFalse(segmentHitsBox(eye,inverse.transform(new org.joml.Vector3f(point)
                    .sub(rig.bottle().position())),low,high),
                    "the cake covers the feeding forearm at "+bite+" px="+px);
            }
        }
    }

    /** Slab test for the segment eye->palm against the cake's own box, in the cake's own frame. */
    private static boolean segmentHitsBox(org.joml.Vector3f eye,org.joml.Vector3f palm,float[] low,float[] high) {
        float[] origin={eye.x,eye.y,eye.z},delta={palm.x-eye.x,palm.y-eye.y,palm.z-eye.z};
        float enter=0,exit=1;
        for(int axis=0;axis<3;axis++) {
            if(Math.abs(delta[axis])<1e-7f) {
                if(origin[axis]<low[axis]||origin[axis]>high[axis])return false;
                continue;
            }
            float near=(low[axis]-origin[axis])/delta[axis],far=(high[axis]-origin[axis])/delta[axis];
            if(near>far) {float swap=near;near=far;far=swap;}
            enter=Math.max(enter,near);exit=Math.min(exit,far);
            if(enter>exit)return false;
        }
        return enter<=1&&exit>=0;
    }

    private static String stage(int t) {
        return StickyRiceCakeAnimations.bodyModel(BottleMotion.Action.DRINK,t,USE_TICKS,true);
    }
    private static com.google.gson.JsonObject model(String path)throws Exception {
        try(var r=new java.io.InputStreamReader(StickyRiceCakeAnimationsTest.class.getResourceAsStream(
                "/assets/farmingtales_drink_anim/models/"+path+".json"))) {
            return com.google.gson.JsonParser.parseReader(r).getAsJsonObject();
        }
    }
    private static com.google.gson.JsonElement element(String path)throws Exception {
        return model(path).getAsJsonArray("elements").get(0);
    }
    private static int elements(String path)throws Exception {
        return model(path).getAsJsonArray("elements").size();
    }
}
