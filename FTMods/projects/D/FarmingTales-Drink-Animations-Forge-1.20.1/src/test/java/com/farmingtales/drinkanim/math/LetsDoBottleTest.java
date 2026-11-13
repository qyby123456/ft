package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import static org.junit.jupiter.api.Assertions.*;

class LetsDoBottleTest {
    private static final String[] STOPPERS=("vinery:lilitu_wine vinery:stal_wine vinery:strad_wine vinery:mellohi_wine "
        +"vinery:magnetic_wine vinery:apple_wine vinery:creepers_crush vinery:mead brewery:whiskey_maggoallan "
        +"brewery:whiskey_highland_hearth vinery:chenet_wine vinery:aegis_wine vinery:cristel_wine "
        +"vinery:chorus_wine vinery:solaris_wine vinery:kelp_cider vinery:apple_cider vinery:eiswein").split(" ");
    @Test void screwCapsRotateWhileOriginalWoodenStoppersOnlyLift() throws Exception {
        for(String id:new String[]{"vinery:jellie_wine","vinery:jo_special_mixture"}) {
            var p=DrinkCatalog.find(id);assertFalse(p.cork());
            var pose=DrinkMotion.sample(p,BottleMotion.Action.DRINK,24,32,false);
            var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,24,32,1,false,p);
            var relative=new Quaternionf(rig.bottle().rotation()).conjugate().mul(rig.cork().rotation());
            assertTrue(Math.abs(relative.angle())>Math.toRadians(60),id+" twists before lifting off");
            assertTrue(DrinkMotion.capDetached(p,BottleMotion.Action.DRINK,24));
        }
        for(String id:STOPPERS) {
            var p=DrinkCatalog.find(id);assertTrue(p.cork());
            var pose=DrinkMotion.sample(p,BottleMotion.Action.DRINK,24,32,false);
            var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,24,32,1,false,p);
            assertEquals(1,Math.abs(rig.bottle().rotation().dot(rig.cork().rotation())),1e-5,id);
            assertTrue(DrinkMotion.capLift(p,30)>DrinkMotion.capLift(p,20));
            assertEquals(0,DrinkMotion.neckReveal(p,BottleMotion.Action.DRINK,35,32,false));
            assertEquals(0,elements(p.neckModel()).size(),id+" has no replacement neck layer");
            assertEquals(3,elements(p.capModel()).size(),id+" removes the head and two source-sized shank bands");
        }
    }
    @Test void orangeStyleBottlesKeepTheOrangeOpeningMotion() {
        String[] names=("vinery:bolvar_wine vinery:glowing_wine vinery:cherry_wine vinery:red_wine vinery:villagers_fright "
            +"brewery:whiskey_smokey_reverie brewery:whiskey_jojannik vinery:noir_wine vinery:bottle_mojang_noir "
            +"brewery:whiskey_ak brewery:whiskey_jamesons_malt brewery:whiskey_carrasconlabel vinery:clark_wine "
            +"brewery:whiskey_cristelwalker brewery:whiskey_lilitusinglemalt").split(" ");
        for(String id:names)for(double tick:new double[]{0,14,24,28,35,48}) {
            var p=DrinkCatalog.find(id);assertTrue(p.cork());
            assertEquals(DrinkMotion.sample(DrinkCatalog.ORANGE,BottleMotion.Action.DRINK,tick,32,false),
                DrinkMotion.sample(p,BottleMotion.Action.DRINK,tick,32,false),id);
            assertEquals(0,DrinkMotion.neckReveal(p,BottleMotion.Action.DRINK,tick,32,false));
        }
    }
    @Test void lilituKeepsItsOriginalSlopingContentsWithoutADynamicTShape() throws Exception {
        var p=DrinkCatalog.find("vinery:lilitu_wine");assertTrue(p.liquid().isEmpty());
        var elements=elements(p.bodyModel());int contents=0,slopes=0;
        for(var value:elements) {
            var e=value.getAsJsonObject();String name=e.get("name").getAsString();
            if(java.util.Set.of("source_9","source_10","source_11","source_12").contains(name)) {
                contents++;if(e.has("rotation")&&e.getAsJsonObject("rotation").get("angle").getAsDouble()!=0)slopes++;
            }
        }
        assertEquals(4,contents);assertEquals(2,slopes);
    }
    @Test void steadyWalkingMovesTheLiquidAndStoppingLetsItSettle() {
        var p=DrinkCatalog.ORANGE;var spring=new SloshSpring();
        double min=1,max=-1;
        for(int tick=0;tick<120;tick++) {
            double impulse=LiquidSurface.walkingImpulse(tick*.13,.18,true);
            spring.tick(impulse,-impulse*.7);
            var n=LiquidSurface.normal(p,new Quaternionf(),0,spring.x(1),spring.z(1),0);
            if(tick>20){min=Math.min(min,n.x());max=Math.max(max,n.x());}
        }
        assertTrue(max-min>.06,"Walking continues to move the surface at constant speed");
        for(int tick=0;tick<100;tick++)spring.tick(LiquidSurface.walkingImpulse(16,0,true),0);
        assertTrue(spring.energy()<.0001,"Liquid settles after walking stops");
        assertEquals(0,LiquidSurface.walkingImpulse(4,.2,false));
        for(var profile:DrinkCatalog.all())if(profile.id().startsWith("vinery:"))assertEquals(242,profile.liquidAlpha());
    }
    @Test void vineryAndWhiskeyUseFixedContentsAndThreeLengthCorks() throws Exception {
        int fixed=0,longCorks=0;
        for(var p:DrinkCatalog.all())if(p.id().startsWith("vinery:")||p.id().startsWith("brewery:whiskey_")) {
            fixed++;
            if(p.id().equals("brewery:whiskey_highland_hearth"))assertFalse(p.liquid().isEmpty());
            else assertTrue(p.liquid().isEmpty(),p.id()+" must not render dynamic liquid");
            assertTrue(elements(p.neckModel()).isEmpty(),p.id()+" must not add a replacement rim");
            if(BottleClosures.screw(p))continue;
            longCorks++;
            var cap=elements(p.capModel());double bottom=Double.POSITIVE_INFINITY,top=-Double.MAX_VALUE;
            for(var value:cap) {
                var e=value.getAsJsonObject();
                bottom=Math.min(bottom,e.getAsJsonArray("from").get(1).getAsDouble());
                top=Math.max(top,e.getAsJsonArray("to").get(1).getAsDouble());
            }
            double originalLength=p.capPivot()[1]-p.mouthY();
            assertEquals(3*originalLength,top-bottom,1e-5,p.id());
            assertEquals(originalLength,p.capPivot()[1]-16*BottleClosures.corkClipBottom(p,0),1e-5,
                p.id()+" only the source head is visible when seated");
            assertEquals(top-bottom,p.capPivot()[1]-16*BottleClosures.corkClipBottom(p,DrinkMotion.capLift(p,34)),1e-5,
                p.id()+" the full three-length cork is visible after extraction");
        }
        assertEquals(35,fixed);assertEquals(33,longCorks);
    }
    private com.google.gson.JsonArray elements(String name) throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+name+".json"))) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements");
        }
    }
}
