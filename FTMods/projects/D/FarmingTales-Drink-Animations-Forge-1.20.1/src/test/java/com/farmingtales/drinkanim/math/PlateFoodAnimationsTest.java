package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class PlateFoodAnimationsTest {
    private static final DrinkProfile PLATE=DrinkCatalog.find("kaleidoscope_cookery:sticky_rice_cake_plate");
    private static final int USE_TICKS=32, SIP_END=88;
    private static final int PORTIONS=5;

    @Test void reusesTheSourcePerPortionModelsInsteadOfAuthoringBites() throws Exception {
        assertNotNull(PLATE);
        assertEquals(DrinkProfile.Style.FOOD,PLATE.style());
        assertTrue(PLATE.food()&&!PLATE.cup()&&!PLATE.can()&&!PLATE.cork());
        assertTrue(PLATE.liquid().isEmpty(),"the plate carries no liquid");
        assertEquals(PORTIONS,PlateFoodAnimations.portions(PLATE));
        assertEquals(PORTIONS+1,PlateFoodAnimations.models(PLATE).length,"one model per remaining portion plus empty");
        assertEquals("animation/plates/sticky_rice_cake_plate/level"+PORTIONS,PLATE.bodyModel());
        for(String stage:PlateFoodAnimations.models(PLATE))
            assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/models/"+stage+".json"),stage);
        // The upstream inventory sprite stays exactly as the source mod ships it.
        assertNull(getClass().getResource("/assets/kaleidoscope_cookery/models/item/sticky_rice_cake_plate.json"),
            "the original flat inventory icon must not be replaced");
        assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/textures/plates/plate.png"));
    }

    @Test void everyStageNamesAnAtlasSpriteThatExists() throws Exception {
        for(String stage:PlateFoodAnimations.models(PLATE))
            for(var entry:model(stage).getAsJsonObject("textures").entrySet()) {
                String reference=entry.getValue().getAsString();
                String path=reference.split(":")[1];
                assertTrue(path.equals("snacks/sticky_rice_cake")||path.equals("plates/plate"),reference);
                assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/textures/"+path+".png"),reference);
            }
    }

    /** The plate carries its food on the top face, so the underside must never face the player. */
    @Test void theFoodFacesThePlayerWhileItIsShownAndEaten() {
        // The window stops before the settle back into the shared idle carry pose.
        double worst=1;
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK})
            for(int t=20;t<=86;t++) {
                var motion=DrinkMotion.sample(PLATE,action,t,USE_TICKS,true);
                worst=Math.min(worst,BottleRig.rotation(motion,1).transform(new Vector3f(0,1,0)).z());
            }
        assertTrue(worst>0,"the plate turns its top face away from the player; worst up.z="+worst);
        assertTrue(DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,60,USE_TICKS,true).pitch()>20,
            "eating tips the food toward the mouth");
        assertTrue(DrinkMotion.sample(PLATE,BottleMotion.Action.INSPECT,48,USE_TICKS,true).pitch()>20,
            "inspection lifts the far rim so the dish is read from above");
    }

    @Test void portionsAreEatenOneAtATimeAndAFreshPlateComesBackWhole() {
        assertEquals(147,(int)AnimationPlayback.authored(PLATE,BottleMotion.Action.DRINK,
            AnimationPlayback.ticks(PLATE,BottleMotion.Action.DRINK,USE_TICKS),USE_TICKS),
            "the controller clock is remapped onto the authored 48 + 40 + 41 + 18 timeline");
        assertArrayEquals(new double[]{58,64,70,76,82},PlateFoodAnimations.bites(PLATE,USE_TICKS),1e-9);

        assertEquals(level(50),5);
        assertEquals(level(60),4);
        assertEquals(level(66),3);
        assertEquals(level(72),2);
        assertEquals(level(78),1);
        assertEquals(level(84),0,"the empty plate is the source's own empty model");
        assertEquals(level(120),0,"the empty hand keeps the empty plate");
        assertEquals(level(135),5,"the replacement is a fresh plate");

        assertEquals(.8,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,58,USE_TICKS,true).fill(),1e-6);
        assertEquals(.645,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,61,USE_TICKS,true).fill(),1e-6);
        assertEquals(.49,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,67,USE_TICKS,true).fill(),1e-6);
        assertEquals(.335,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,73,USE_TICKS,true).fill(),1e-6);
        assertEquals(.18,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,79,USE_TICKS,true).fill(),1e-6);
        assertEquals(.025,DrinkMotion.sample(PLATE,BottleMotion.Action.DRINK,85,USE_TICKS,true).fill(),1e-6);
    }

    @Test void oneBiteAndOneSwallowSoundPerPortion() {
        var cues=AnimationSoundTimeline.drinking(PLATE,USE_TICKS);
        assertEquals(cues,PlateFoodAnimations.eatingSounds(PLATE,USE_TICKS));
        assertEquals(PORTIONS,cues.stream().filter(c->c.event().equals("sticky_rice_cake_bite")).count());
        assertEquals(PORTIONS,cues.stream().filter(c->c.event().equals("snack_swallow")).count());
        assertTrue(cues.stream().noneMatch(c->c.event().equals("gulp")),"a plate never plays drink gulps");
        assertEquals(6,PlateFoodAnimations.inspectionSounds().size());
    }

    @Test void theHotkeyOnlyPerformsSoThePlaceableDishIsNeverUsedOrPlaced() {
        assertTrue(PresentationOnly.active(PLATE),"the world owns the portions");
        assertTrue(PlateFoodAnimations.owns(PLATE));
        // Its own item is a different, non-presentation object.
        assertFalse(PresentationOnly.active(DrinkCatalog.find("papercraft_magic_decoration:land_no1")));
    }

    @Test void inspectionReturnsToTheSameHoldAndLetGoOfThePlateSpins() {
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(PLATE,BottleMotion.Action.INSPECT,0,USE_TICKS,true));
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(PLATE,BottleMotion.Action.INSPECT,
            BottleMotion.INSPECT_TICKS,USE_TICKS,true));
        assertEquals(0,DrinkMotion.assistance(PLATE,BottleMotion.Action.INSPECT,8,USE_TICKS));
        assertTrue(DrinkMotion.assistance(PLATE,BottleMotion.Action.INSPECT,24,USE_TICKS)>.9,"the far hand takes the rim");
        assertTrue(DrinkMotion.sample(PLATE,BottleMotion.Action.INSPECT,62,USE_TICKS,true).yaw()>240,"turned past the rim");
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT})
            for(int t=0;t<=BottleMotion.INSPECT_TICKS;t+=4) {
                assertEquals(0,DrinkMotion.neckReveal(PLATE,action,t,USE_TICKS,true),"a plate has no neck");
                assertFalse(DrinkMotion.capDetached(PLATE,action,t),"nothing detaches");
                if(action==BottleMotion.Action.DRINK)
                    assertFalse(DrinkMotion.capVisible(PLATE,action,t,USE_TICKS,true));
            }
    }

    /** Same guard as the other handheld foods: a support hand must not reframe the plate. */
    @Test void supportHandDoesNotDragThePlateOutOfFrame() {
        double deepest=0;
        for(double t=20;t<=96;t+=.5) {
            var motion=DrinkMotion.sample(PLATE,BottleMotion.Action.INSPECT,t,USE_TICKS,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.INSPECT,t,USE_TICKS,1,true,PLATE);
            rig=EatingMotion.apply(rig,PLATE,BottleMotion.Action.INSPECT,t,USE_TICKS,0,1);
            var camera=DrinkMotion.camera(PLATE,BottleMotion.Action.INSPECT,t,USE_TICKS,0).rotation();
            deepest=Math.min(deepest,BottleFraming.offset(rig,BottleMotion.Action.INSPECT,t,USE_TICKS,true,PLATE,camera).y);
        }
        assertTrue(-deepest<.10,"support hand reframes the plate by "+(-deepest));
    }

    @Test void carryingPalmNeverSlidesOnThePlate() {
        Vector3f contact=null;
        float radius=-1;
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK})
            for(int t=10;t<=SIP_END;t+=2) {
                var rig=BottleRig.solve(DrinkMotion.sample(PLATE,action,t,USE_TICKS,true),action,t,USE_TICKS,1,false,PLATE);
                var local=new Quaternionf(rig.bottle().rotation()).conjugate()
                    .transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                if(action==BottleMotion.Action.DRINK) {
                    if(contact==null)contact=local;
                    assertEquals(0,contact.distance(local),1e-6,"contact moves at "+action+" "+t);
                } else {
                    if(radius<0)radius=local.length();
                    assertEquals(radius,local.length(),1e-6,"palm radius changes at "+action+" "+t);
                }
            }
    }

    private static int level(int t) {
        String path=PlateFoodAnimations.bodyModel(PLATE,BottleMotion.Action.DRINK,t,USE_TICKS,true);
        return Integer.parseInt(path.substring(path.lastIndexOf("level")+"level".length()));
    }
    private static com.google.gson.JsonObject model(String path)throws Exception {
        try(var r=new java.io.InputStreamReader(PlateFoodAnimationsTest.class.getResourceAsStream(
                "/assets/farmingtales_drink_anim/models/"+path+".json"))) {
            return com.google.gson.JsonParser.parseReader(r).getAsJsonObject();
        }
    }
}
