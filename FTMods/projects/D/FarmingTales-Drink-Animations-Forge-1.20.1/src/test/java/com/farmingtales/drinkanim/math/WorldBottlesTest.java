package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class WorldBottlesTest {
    private static final List<String> IDS=List.of(
        "kaleidoscope_tavern:luminous_bride", "kaleidoscope_world_liquor:smirnoff_red_vodka",
        "kaleidoscope_world_liquor:johnnie_walker", "kaleidoscope_world_liquor:cool_tea",
        "kaleidoscope_world_liquor:skyy_vodka", "kaleidoscope_world_liquor:sour_plum", "smc:ice_tea",
        "kaleidoscope_world_liquor:absolut_vodka", "kaleidoscope_world_liquor:jack_daniel",
        "kaleidoscope_world_liquor:bacardi_carta_blanca", "kaleidoscope_world_liquor:kwas_chlebowy",
        "kaleidoscope_world_liquor:bombay_sapphire_gin", "kaleidoscope_world_liquor:strongbow",
        "kaleidoscope_world_liquor:spiryt_vodka", "kaleidoscope_tavern:glowflower_brew",
        "kaleidoscope_world_liquor:dassai");

    @Test void allRequestedIdsHaveOrangeWineGesturesAndTheirOwnBottleParts() {
        for(String id:IDS) {
            var p=DrinkCatalog.find(id);assertNotNull(p,id);
            assertEquals(DrinkProfile.Style.WINE,p.style());
            assertEquals(!BottleClosures.waterCap(p),p.pullCap());assertFalse(BottleClosures.fixedBottle(p));
            assertFalse(ContainerAnimations.owns(p));assertFalse(p.liquid().isEmpty());
            assertTrue(p.bodyModel().endsWith("/"+id.split(":")[1]+"_body"));
            assertFalse(HandVolumes.boxes(p).isEmpty(),id);
            for(int duration:new int[]{16,32})for(var action:List.of(Action.DRAW,Action.INSPECT,Action.DRINK,Action.STOW))
            for(double t=0;t<=140;t+=.5) {
                var reference=action==Action.DRINK&&BottleClosures.waterCap(p)?DrinkCatalog.find("farmingtales:purified_water"):DrinkCatalog.ORANGE;
                assertEquals(DrinkMotion.arm(reference,action,t,duration,false),DrinkMotion.arm(p,action,t,duration,false),id);
                assertEquals(DrinkMotion.arm(reference,action,t,duration,true),DrinkMotion.arm(p,action,t,duration,true),id);
                assertEquals(DrinkMotion.assistance(reference,action,t,duration),DrinkMotion.assistance(p,action,t,duration),id);
                assertEquals(DrinkMotion.capDetached(reference,action,t),DrinkMotion.capDetached(p,action,t),id);
                assertEquals(DrinkMotion.capVisible(reference,action,t,duration,true),DrinkMotion.capVisible(p,action,t,duration,true),id);
                if(action!=Action.INSPECT)assertEquals(DrinkMotion.sample(reference,action,t,duration,true),DrinkMotion.sample(p,action,t,duration,true),id);
            }
        }
        assertNull(DrinkCatalog.find("farmingtales:smc_ice_tea"));
    }

    @Test void threeTeaLidsRotateLikeWaterAndLeaveAVisibleNeck() {
        var water=DrinkCatalog.find("farmingtales:purified_water");
        for(String id:List.of("smc:ice_tea","kaleidoscope_world_liquor:cool_tea","kaleidoscope_world_liquor:sour_plum")) {
            var p=DrinkCatalog.find(id);assertTrue(BottleClosures.screw(p));assertFalse(p.cork());
            assertEquals(AnimationPlayback.raiseTicks(water),AnimationPlayback.raiseTicks(p));
            for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1})for(double t=0;t<42;t+=.25) {
                assertEquals(DrinkMotion.capLift(water,t),DrinkMotion.capLift(p,t));
                assertEquals(1,DrinkMotion.neckReveal(p,Action.DRINK,t,32,true));
                assertEquals(DrinkMotion.camera(water,Action.DRINK,t,32),DrinkMotion.camera(p,Action.DRINK,t,32));
                var tea=BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,p);
                var ref=BottleRig.solve(DrinkMotion.sample(water,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,water);
                assertTrue(tea.cork().rotation().equals(ref.cork().rotation(),1e-6f),"Water lid rotation at "+t);
            }
        }
    }

    @Test void rotatedTavernLiquidsFitTheirGlassAndTeaUsesTheWideGrip() {
        for(String id:List.of("kaleidoscope_tavern:luminous_bride","kaleidoscope_tavern:glowflower_brew")) {
            var p=DrinkCatalog.find(id);assertEquals(45,p.cupShape().liquidYaw());
            assertEquals(1,p.liquid().size());
            var r=p.liquid().get(0);
            assertTrue(r.min()[0]>-2/16.&&r.max()[0]<2/16.);
        }
        for(String id:List.of("smc:ice_tea","kaleidoscope_world_liquor:cool_tea","kaleidoscope_world_liquor:sour_plum")) {
            var p=DrinkCatalog.find(id);assertEquals(8,p.width());assertEquals(6,p.depth());
            assertEquals(2,p.liquid().size());assertTrue(p.scale()<1);
            assertEquals(p.liquid().get(0).max()[1],p.liquid().get(1).min()[1],1e-8);
        }
    }
}
