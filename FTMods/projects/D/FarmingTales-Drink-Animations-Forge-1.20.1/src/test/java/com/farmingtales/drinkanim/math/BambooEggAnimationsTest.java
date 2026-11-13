package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class BambooEggAnimationsTest {
    private static final DrinkProfile FOOD=DrinkCatalog.find(BambooEggAnimations.ID);

    @Test void replacesTheFlatItemWithDetailedCupCustardAndSeparateSpoon() throws Exception {
        assertNotNull(FOOD);assertTrue(FOOD.food());
        assertEquals(DrinkProfile.Style.FOOD,FOOD.style());
        assertTrue(elements(FOOD.bodyModel())>=17);
        assertEquals(3,elements(FOOD.capModel()));
        assertNotNull(getClass().getResource("/assets/kaleidoscope_chinesefood/models/item/bamboo_steamed_egg.json"));
        assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/textures/bamboo_steamed_egg_liquid.png"));
    }

    @Test void inspectionTouchesTheSpoonBeforeTurningAndReturnsToTheSameHold() {
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(FOOD,BottleMotion.Action.INSPECT,0,32,true));
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(FOOD,BottleMotion.Action.INSPECT,104,32,true));
        assertEquals(0,DrinkMotion.assistance(FOOD,BottleMotion.Action.INSPECT,8,32));
        assertTrue(DrinkMotion.assistance(FOOD,BottleMotion.Action.INSPECT,20,32)>.9);
        assertTrue(DrinkMotion.capDetached(FOOD,BottleMotion.Action.INSPECT,20));
        var lifted=BottleRig.solve(DrinkMotion.sample(FOOD,BottleMotion.Action.INSPECT,32,32,true),
            BottleMotion.Action.INSPECT,32,32,1,true,FOOD);
        assertTrue(lifted.cork().position().y-lifted.bottle().position().y>.25,"spoon is visibly lifted only after contact");
        assertEquals(7,BambooEggAnimations.inspectionSounds().size());
    }

    @Test void eatingUsesThreeSeparatedSpoonfulsAndLowersTheCustard() {
        assertEquals(3,BambooEggAnimations.eatingSounds().stream().filter(c->c.event().equals("custard_eat")).count());
        assertEquals(.8,DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,58,32,true).fill(),1e-6);
        assertEquals(.55,DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,65,32,true).fill(),1e-6);
        assertEquals(.29,DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,76,32,true).fill(),1e-6);
        assertEquals(.025,DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,87,32,true).fill(),1e-6);
        for(int tick:new int[]{61,72,83}) {
            var rig=BottleRig.solve(DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,tick,32,true),
                BottleMotion.Action.DRINK,tick,32,1,true,FOOD);
            assertTrue(rig.bottle().position().x-rig.cork().position().x>.14,"spoon clears the cup silhouette at bite "+tick);
            assertTrue(rig.assistance()>.99);
        }
    }

    @Test void spoonReachDoesNotPullTheCupOutOfFrame() {
        float min=Float.POSITIVE_INFINITY,max=Float.NEGATIVE_INFINITY;
        for(double t=56;t<=88;t+=.5) {
            var motion=DrinkMotion.sample(FOOD,BottleMotion.Action.DRINK,t,32,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.DRINK,t,32,1,true,FOOD);
            rig=EatingMotion.apply(rig,FOOD,BottleMotion.Action.DRINK,t,32,0,1);
            var camera=DrinkMotion.camera(FOOD,BottleMotion.Action.DRINK,t,32,0).rotation();
            var framing=BottleFraming.offset(rig,BottleMotion.Action.DRINK,t,32,true,FOOD,camera);
            float y=rig.bottle().position().y+framing.y;
            min=Math.min(min,y);max=Math.max(max,y);
        }
        assertTrue(max-min<.09,"helper spoon reframes the supported cup by "+(max-min));
    }
    @Test void carryingPalmNeverSlidesOnTheBambooWall() {
        Vector3f contact=null;
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK})for(int t=10;t<=88;t+=2) {
            var rig=BottleRig.solve(DrinkMotion.sample(FOOD,action,t,32,true),action,t,32,1,false,FOOD);
            var local=new Quaternionf(rig.bottle().rotation()).conjugate()
                .transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
            if(contact==null)contact=local;
            assertEquals(0,contact.distance(local),1e-6);
        }
    }

    private static int elements(String model)throws Exception {
        try(var r=new java.io.InputStreamReader(BambooEggAnimationsTest.class.getResourceAsStream(
                "/assets/farmingtales_drink_anim/models/"+model+".json"))) {
            return com.google.gson.JsonParser.parseReader(r).getAsJsonObject().getAsJsonArray("elements").size();
        }
    }
}


