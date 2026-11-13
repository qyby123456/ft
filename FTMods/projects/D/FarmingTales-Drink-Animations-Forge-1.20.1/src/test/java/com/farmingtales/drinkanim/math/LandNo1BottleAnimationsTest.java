package com.farmingtales.drinkanim.math;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LandNo1BottleAnimationsTest {
    private static final DrinkProfile BOTTLE=DrinkCatalog.find(LandNo1BottleAnimations.ID);

    @Test void exactNativeBottleGetsItsOwnDetailedThreePartModel() throws Exception {
        assertNotNull(BOTTLE);
        assertEquals(DrinkProfile.Style.WINE,BOTTLE.style());
        assertTrue(BottleClosures.screw(BOTTLE));
        assertFalse(BottleClosures.fixedBottle(BOTTLE),"Paper Kite closure is not a Let's Do fixed-content source model");
        assertFalse(BOTTLE.cork());
        assertEquals(.54f,BOTTLE.scale(),1e-6);
        assertEquals(3,BOTTLE.liquid().size());
        assertTrue(LandNo1BottleAnimations.presentationOnlyDrink(BOTTLE));
        assertFalse(LandNo1BottleAnimations.presentationOnlyDrink(DrinkCatalog.find("papercraft_magic_decoration:land_no1")));
        assertTrue(elements(BOTTLE.bodyModel())>=30,"body retains raised label, seams and stepped shoulder");
        assertTrue(elements(BOTTLE.neckModel())>=12,"open neck has walls and a hollow two-level lip");
        assertTrue(elements(BOTTLE.capModel())>=10,"screw sleeve has modeled grip ridges");
        assertTrue(elements("animation/papercraft/bottle_of_land_no1_closed")>=55,"closed inventory model combines every detailed part");
        assertNotNull(getClass().getResource("/assets/papercraft_magic_decoration/models/item/bottle_of_land_no1.json"));
    }

    @Test void inspectionUsesContactFirstAndShortSupportedTurnsInsteadOfAFreeSpin() {
        var rest=DrinkMotion.sample(BOTTLE,BottleMotion.Action.IDLE,0,32,true);
        var start=DrinkMotion.sample(BOTTLE,BottleMotion.Action.INSPECT,0,32,true);
        var finish=DrinkMotion.sample(BOTTLE,BottleMotion.Action.INSPECT,104,32,true);
        assertEquals(rest.x(),start.x(),1e-7);assertEquals(rest.y(),start.y(),1e-7);
        assertEquals(rest.x(),finish.x(),1e-7);assertEquals(rest.y(),finish.y(),1e-7);
        double minYaw=Double.POSITIVE_INFINITY,maxYaw=Double.NEGATIVE_INFINITY;
        for(int tick=0;tick<=104;tick++) {
            var pose=DrinkMotion.sample(BOTTLE,BottleMotion.Action.INSPECT,tick,32,true);
            minYaw=Math.min(minYaw,pose.yaw());maxYaw=Math.max(maxYaw,pose.yaw());
            if(tick>=20&&tick<=85) {
                assertTrue(DrinkMotion.assistance(BOTTLE,BottleMotion.Action.INSPECT,tick,32)>.95);
                var rig=BottleRig.solve(pose,BottleMotion.Action.INSPECT,tick,32,1,false,BOTTLE);
                assertTrue(rig.helper().position().distance(rig.bottle().position())<.7,"support palm stays with bottle at "+tick);
                assertTrue(rig.hand().position().distance(rig.bottle().position())<.7,"carry palm stays with bottle at "+tick);
            }
        }
        assertTrue(maxYaw-minYaw<55,"label turns stay under one sixth of a revolution");
        assertTrue(minYaw>=130&&maxYaw<=183,"no independent axial spin");
    }

    @Test void visualDrinkUnscrewsThenTransfersWeightToTheSecondPalm() {
        int duration=32,end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);
        assertTrue(DrinkMotion.capDetached(BOTTLE,BottleMotion.Action.DRINK,30));
        assertFalse(DrinkMotion.capVisible(BOTTLE,BottleMotion.Action.DRINK,50,duration,true));
        assertEquals(0,LandNo1BottleAnimations.bodySupport(41,duration),1e-7);
        assertTrue(LandNo1BottleAnimations.bodySupport(52,duration)>.99);
        assertTrue(LandNo1BottleAnimations.bodySupport(end,duration)>.99);
        var pose=DrinkMotion.sample(BOTTLE,BottleMotion.Action.DRINK,60,duration,true);
        var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,60,duration,1,false,BOTTLE);
        assertTrue(rig.assistance()>.99);
        assertTrue(rig.helper().position().distance(rig.bottle().position())<.55,"free palm bears the bottle during the sip");
        assertEquals(4,AnimationSoundTimeline.drinking(BOTTLE,duration).stream().filter(c->c.event().equals("gulp")).count());
    }

    private int elements(String model) throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream(
            "/assets/farmingtales_drink_anim/models/"+model+".json"))) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements").size();
        }
    }
}
