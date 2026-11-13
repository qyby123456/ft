package com.farmingtales.drinkanim.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TwelveTavernBottlesTest {
    private static final List<String> IDS=List.of(
        "kaleidoscope_tavern:carignan","kaleidoscope_tavern:mother_snow",
        "kaleidoscope_tavern:miners_star","kaleidoscope_tavern:rum",
        "kaleidoscope_tavern:riesling_dry_white","kaleidoscope_tavern:madame_shexiang",
        "kaleidoscope_world_liquor:bamboo_leaf_green_liquor","kaleidoscope_tavern:sunset_glow",
        "kaleidoscope_tavern:sweet_berry_wine","kaleidoscope_tavern:vodka",
        "kaleidoscope_world_liquor:maotai","kaleidoscope_tavern:brandy");
    private static final Set<String> SCREW=Set.of("kaleidoscope_tavern:carignan","kaleidoscope_tavern:mother_snow");
    private static final Set<String> XO=Set.of("kaleidoscope_tavern:brandy","kaleidoscope_tavern:carignan");

    @Test void requestedBottlesShareXoInspectionAndSippingWithoutChangingClosures() {
        var reference=DrinkCatalog.find("farmingtales:brandy_xo");
        for(String id:IDS)assertEquals(XO.contains(id),ContainerAnimations.family(DrinkCatalog.find(id))==ContainerAnimations.Family.BRANDY,id);
        for(String id:XO) {
            var p=DrinkCatalog.find(id);
            assertEquals(AnimationSoundTimeline.inspection(reference),AnimationSoundTimeline.inspection(p),id);
            for(var action:List.of(BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK))for(int duration:new int[]{16,32,100}) {
                double start=action==BottleMotion.Action.DRINK?48:0;
                double end=action==BottleMotion.Action.DRINK?48+BottleMotion.sipTicks(duration):104;
                for(double t=start;t<=end;t+=.5) {
                    assertEquals(DrinkMotion.sample(reference,action,t,duration,true),DrinkMotion.sample(p,action,t,duration,true),id+" "+action+" "+t);
                    assertEquals(DrinkMotion.assistance(reference,action,t,duration),DrinkMotion.assistance(p,action,t,duration),1e-8,id);
                    for(int variant=0;variant<EatingMotion.VARIANTS;variant++)
                        assertEquals(DrinkMotion.camera(reference,action,t,duration,variant),DrinkMotion.camera(p,action,t,duration,variant),id);
                }
            }
        }
        assertTrue(DrinkCatalog.find("kaleidoscope_tavern:brandy").pullCap());
        assertTrue(BottleClosures.screw(DrinkCatalog.find("kaleidoscope_tavern:carignan")));
    }

    @Test void xoPalmsSupportBothTavernBottlesAndReturnContinuouslyForEitherHand() {
        for(String id:XO)for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            var p=DrinkCatalog.find(id);
            var idle=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.IDLE,0,32,true),BottleMotion.Action.IDLE,0,32,side,slim,p);
            var returned=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.INSPECT,104,32,true),BottleMotion.Action.INSPECT,104,32,side,slim,p);
            assertEquals(0,idle.hand().position().distance(returned.hand().position()),1e-6,id);
            assertEquals(1,Math.abs(idle.hand().rotation().dot(returned.hand().rotation())),1e-6,id);
            for(var action:List.of(BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK)) {
                double start=action==BottleMotion.Action.INSPECT?25:48,end=action==BottleMotion.Action.INSPECT?76:79;
                for(double t=start;t<=end;t+=.5) {
                    var frame=BottleRig.solve(DrinkMotion.sample(p,action,t,32,true),action,t,32,side,slim,p);
                    var inverse=new Quaternionf(frame.bottle().rotation()).rotateY((float)Math.toRadians(-side*180)).conjugate();
                    var right=inverse.transform(new org.joml.Vector3f(frame.hand().position()).sub(frame.bottle().position()));
                    var left=inverse.transform(new org.joml.Vector3f(frame.helper().position()).sub(frame.bottle().position()));
                    assertEquals(-right.x,left.x,1e-6,id);assertEquals(right.y,left.y,1e-6,id);assertEquals(right.z,left.z,1e-6,id);
                    assertEquals(1,frame.assistance(),1e-8,id);
                }
            }
        }
    }

    @Test void allTwelveOwnTheirModelsAndUseOnlyTheRequestedClosureType() throws Exception {
        for(String id:IDS) {
            var p=DrinkCatalog.find(id);assertNotNull(p,id);
            assertEquals(DrinkProfile.Style.WINE,p.style());assertTrue(BottleClosures.fixedBottle(p));
            assertEquals(SCREW.contains(id),BottleClosures.screw(p),id);
            assertEquals(!SCREW.contains(id),p.pullCap(),id);
            assertTrue(p.liquid().isEmpty(),id);assertFalse(HandVolumes.boxes(p).isEmpty(),id);
            assertTrue(p.bodyModel().endsWith("/"+id.split(":")[1]+"_body"),id);
            assertEquals(0,elements(p.neckModel()).size(),id);
            assertEquals(SCREW.contains(id)?1:3,elements(p.capModel()).size(),id);
            assertNotSame(p,DrinkCatalog.find("farmingtales:"+id.split(":")[1]),id);
        }
    }

    @Test void specifiedLidsTurnAroundTheirOwnVerticalAxisForBothHandsAndArmWidths() {
        var action=BottleMotion.Action.DRINK;
        for(String id:SCREW)for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            var p=DrinkCatalog.find(id);
            for(double t:new double[]{14,19,23,27,31}) {
                var frame=BottleRig.solve(DrinkMotion.sample(p,action,t,32,false),action,t,32,side,slim,p);
                var relative=new Quaternionf(frame.bottle().rotation()).conjugate().mul(frame.cork().rotation());
                var expected=new Quaternionf().rotateY((float)Math.toRadians(-side*DrinkMotion.twist(t)));
                assertEquals(1,Math.abs(relative.dot(expected)),1e-5,id+" at "+t);
            }
            assertTrue(DrinkMotion.twist(31)>150);
            assertTrue(DrinkMotion.capLift(p,36)>DrinkMotion.capLift(p,23));
            assertFalse(DrinkMotion.capVisible(p,action,52,32,false));
        }
    }

    @Test void extractedStoppersFitTheSeatedCavityAndLeafFollowsItsOwnClosure() throws Exception {
        for(String id:IDS) {
            var p=DrinkCatalog.find(id);var cap=elements(p.capModel());
            double bottom=Double.POSITIVE_INFINITY,top=Double.NEGATIVE_INFINITY;
            for(var value:cap) {
                var e=value.getAsJsonObject();bottom=Math.min(bottom,e.getAsJsonArray("from").get(1).getAsDouble());
                top=Math.max(top,e.getAsJsonArray("to").get(1).getAsDouble());
            }
            if(SCREW.contains(id))assertEquals(p.mouthY(),bottom,1e-5,id);
            else assertEquals(BottleClosures.corkLength(p)*16,top-bottom,1e-5,id);
            if(id.endsWith("bamboo_leaf_green_liquor")) {
                var parts=DecorationMotion.parts(p);assertEquals(1,parts.size());
                assertTrue(parts.get(0).capAttached());assertEquals(DecorationMotion.Kind.LEAF,parts.get(0).kind());
            }
        }
    }

    private JsonArray elements(String path) throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+path+".json"))) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements");
        }
    }
}
