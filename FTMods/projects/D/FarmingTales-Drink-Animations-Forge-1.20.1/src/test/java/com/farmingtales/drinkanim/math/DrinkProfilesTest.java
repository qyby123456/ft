package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DrinkProfilesTest {
    @Test void allThreeAgingTiersUseTheirExactSourceNamespaceAndRejectPlacedStacks() {
        assertEquals(303,DrinkCatalog.aged().size());
        assertEquals(101,DrinkCatalog.aged().values().stream().distinct().count());
        var examples=java.util.Map.of(
            "orange_wine","farmingtales:orange_wine",
            "tavern_sweet_berry_wine","kaleidoscope_tavern:sweet_berry_wine",
            "tavern_rum","kaleidoscope_tavern:rum",
            "tavern_carignan","kaleidoscope_tavern:carignan",
            "world_liquor_dassai","kaleidoscope_world_liquor:dassai",
            "world_liquor_maotai","kaleidoscope_world_liquor:maotai",
            "smc_ice_tea","smc:ice_tea",
            "brewery_whiskey_highland_hearth","brewery:whiskey_highland_hearth",
            "vinery_chenet_wine","vinery:chenet_wine");
        for(var entry:examples.entrySet())for(String suffix:List.of("_i","_g","_d"))
            assertSame(DrinkCatalog.find(entry.getValue()),DrinkCatalog.find("farmingtales:"+entry.getKey()+suffix));
        assertNotSame(DrinkCatalog.find("farmingtales:rum_g"),DrinkCatalog.find("farmingtales:tavern_rum_g"));
        for(var entry:DrinkCatalog.aged().entrySet()) {
            assertSame(DrinkCatalog.find(entry.getValue().id()),DrinkCatalog.find(entry.getKey()));
            for(String invalid:List.of(entry.getKey()+"_2",entry.getKey()+"_3",entry.getKey()+"_4",entry.getKey()+"_g",
                entry.getKey().replace("farmingtales:","minecraft:")))assertNull(DrinkCatalog.find(invalid),invalid);
        }
    }
    @Test void agedBottlesReuseCanonicalAnimationsWithoutMatchingOtherItems() {
        for(var base:DrinkCatalog.all())for(String suffix:List.of("_i","_g","_d")) {
            var aged=DrinkCatalog.find(base.id()+suffix);
            if(!base.agedBottle())assertNull(aged,base.id()+suffix);
            else assertSame(base,aged,base.id()+suffix);
        }
        for(String id:List.of("farmingtales:orange_wine_i_2","farmingtales:orange_wine_i_g",
            "farmingtales:orange_wine_p","farmingtales:missing_wine_d","minecraft:orange_wine_i"))
            assertNull(DrinkCatalog.find(id),id);
        assertNull(DrinkCatalog.find(null));
    }
    @Test void everyRequestedContainerHasItsOwnResourcesAndGeometry() {
        assertEquals(311,DrinkCatalog.all().size());
        for(String name:List.of("white_rabbit_mocha","gold_medal_coffee","nocturnal_cat_coffee"))
            assertNull(DrinkCatalog.find("papercraft_magic_decoration:"+name));
        assertNotNull(DrinkCatalog.find("farmingtales:sakura_wine"));
        assertEquals(140,DrinkCatalog.all().stream().filter(p->!p.wine()&&!p.cup()).count());
        assertEquals(130,DrinkCatalog.all().stream().filter(DrinkProfile::jam).count());
        assertEquals(74,DrinkCatalog.all().stream().filter(DrinkProfile::cup).count());
        var bamboo=DrinkCatalog.find("kaleidoscope_chinesefood:bamboo_steamed_egg");
        assertNotNull(bamboo);assertEquals(DrinkProfile.Style.FOOD,bamboo.style());
        assertTrue(bamboo.food()&&!bamboo.wine()&&!bamboo.cup());
        assertEquals("animation/bamboo_steamed_egg/body",bamboo.bodyModel());
        assertEquals("animation/bamboo_steamed_egg/spoon",bamboo.capModel());
        for(var p:DrinkCatalog.all()) {
            assertTrue(p.scale()>0&&p.width()>0&&p.depth()>0);
            for(String name:List.of(p.bodyModel(),p.capModel(),p.neckModel()))
                assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/models/"+name+".json"),name);
            for(var r:p.liquid()) {
                assertTrue(ContainerLiquid.volume(r)>0,p.id());
                assertTrue(r.texture().startsWith("farmingtales_drink_anim:textures/")&&r.texture().endsWith(".png"),r.texture());
                assertNotNull(getClass().getResource("/assets/"+r.texture().replace(':','/')),r.texture());
                for(int axis=0;axis<3;axis++)assertTrue(r.max()[axis]>r.min()[axis]);
            }
        }
        var chocolate=DrinkCatalog.find("farmingtales:chocolate_sauce");
        assertNotNull(chocolate);
        assertTrue(chocolate.sweetJam());
        assertEquals(AnimationSoundTimeline.drinking(DrinkCatalog.find("farmingtales:ancient_fruit_jam_1"),16),
            AnimationSoundTimeline.drinking(chocolate,16));
        assertNull(DrinkCatalog.find("farmingtales:chocolate_sauce_2"));
        assertNull(DrinkCatalog.find("minecraft:potion"));
    }
    @Test void canTabLiftsDetachesAndLeavesBeforeDrinking() {
        assertEquals(0,DrinkMotion.tabAngle(BottleMotion.Action.INSPECT,30,16));
        assertEquals(0,DrinkMotion.tabAngle(BottleMotion.Action.DRINK,22,16));
        assertEquals(58,DrinkMotion.tabAngle(BottleMotion.Action.DRINK,28,16),1e-6);
        assertEquals(58,DrinkMotion.tabAngle(BottleMotion.Action.DRINK,40,16),1e-6);
        assertEquals(0,DrinkMotion.tabAngle(BottleMotion.Action.DRINK,140,16));
        for(String id:List.of("mozhao","mozhao_p"))for(int side:new int[]{1,-1}) {
            var p=DrinkCatalog.find("farmingtales:"+id);
            assertFalse(DrinkMotion.capDetached(p,BottleMotion.Action.DRINK,28));
            assertTrue(DrinkMotion.capDetached(p,BottleMotion.Action.DRINK,33));
            for(double boundary:new double[]{28,35}) {
                var a=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.DRINK,boundary,16,true),BottleMotion.Action.DRINK,boundary,16,side,true,p);
                var b=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.DRINK,boundary+.001,16,true),BottleMotion.Action.DRINK,boundary+.001,16,side,true,p);
                assertTrue(a.cork().position().distance(b.cork().position())<.001,"No jump on pull or throw");
            }
            var flight=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.DRINK,46,16,true),BottleMotion.Action.DRINK,46,16,side,true,p);
            assertTrue(side*(flight.cork().position().x-flight.bottle().position().x)<-.8,"Throw away from the bottle");
            assertFalse(DrinkMotion.capVisible(p,BottleMotion.Action.DRINK,48,16,true));
            assertFalse(DrinkMotion.capVisible(p,BottleMotion.Action.DRINK,140,16,false));
            assertTrue(DrinkMotion.capVisible(p,BottleMotion.Action.DRINK,140,16,true));
        }
    }
    @Test void canOnlyOpensAfterSealBreakAndNewCanIsSealed() {
        assertEquals(0,DrinkMotion.canOpening(BottleMotion.Action.INSPECT,50,16,true));
        assertEquals(0,DrinkMotion.canOpening(BottleMotion.Action.DRINK,23,16,true));
        assertEquals(1,DrinkMotion.canOpening(BottleMotion.Action.DRINK,30,16,true));
        assertEquals(1,DrinkMotion.canOpening(BottleMotion.Action.DRINK,110,16,true));
        assertEquals(0,DrinkMotion.canOpening(BottleMotion.Action.DRINK,140,16,true));
        assertEquals(1,DrinkMotion.canOpening(BottleMotion.Action.DRINK,140,16,false));
    }
    @Test void brandyKeepsWineConsumptionButUsesAScrewCapAndRealNeck() {
        var brandy=DrinkCatalog.find("farmingtales:brandy_xo");
        assertTrue(brandy.wine());assertFalse(brandy.cork());
        assertEquals(0,DrinkMotion.neckReveal(brandy,BottleMotion.Action.DRINK,30,16,true));
        assertTrue(DrinkMotion.capDetached(brandy,BottleMotion.Action.DRINK,32));
        var pose=DrinkMotion.sample(brandy,BottleMotion.Action.DRINK,0,16,true);
        assertEquals(DrinkMotion.sample(brandy,BottleMotion.Action.IDLE,0,16,true).y(),pose.y(),1e-7);
    }
    @Test void joinedLiquidSectionsHaveNoInternalPartitionAndKeepTheirVolume() {
        var a=new DrinkProfile.Region(new double[]{0,0,0},new double[]{1,1,1},new float[4],new float[4],"unused");
        var b=new DrinkProfile.Region(new double[]{0,1,0},new double[]{1,2,1},new float[4],new float[4],"unused");
        var regions=List.of(a,b);var normal=new LiquidMesh.V(0,1,0);double volume=0;
        for(var region:regions)for(var face:ContainerLiquid.exposedFaces(regions,region,normal,1.6)) {
            assertFalse(face.points().stream().allMatch(p->Math.abs(p.y()-1)<1e-8),"No sheet at the shared section height");
            var pts=face.points();
            for(int i=1;i<pts.size()-1;i++)volume+=pts.get(0).dot(pts.get(i).cross(pts.get(i+1)))/6;
        }
        assertEquals(1.6,volume,1e-7);
    }
    @Test void allShapedLiquidsConserveVolumeAtAnyBottleAngle() {
        for(var p:DrinkCatalog.all())for(var n:List.of(new LiquidMesh.V(0,1,0),new LiquidMesh.V(0,-1,0),new LiquidMesh.V(1,0,0),new LiquidMesh.V(.3,-.7,.5).unit()))
            for(double fill:new double[]{.025,.2,.5,.8}) {
                if(p.liquid().isEmpty())continue;
                double d=ContainerLiquid.level(p.liquid(),n,fill),volume=0,capacity=0;
                for(var region:p.liquid()) {
                    capacity+=ContainerLiquid.volume(region);
                    for(var face:LiquidMesh.clip(n,d,region.minimum(),region.maximum())) {
                        var pts=face.points();
                        for(int i=1;i<pts.size()-1;i++)volume+=pts.get(0).dot(pts.get(i).cross(pts.get(i+1)))/6;
                    }
                }
                assertEquals(capacity*fill,Math.abs(volume),capacity*1e-5,p.id());
            }
    }
    @Test void beverageGesturesUseTheirClosuresAndFinishWithoutAPoseJump() {
        for(var p:DrinkCatalog.all())for(boolean slim:new boolean[]{false,true}) {
            int end=BottleMotion.totalDrinkTicks(16);
            for(var action:List.of(BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT)) {
                int last=action==BottleMotion.Action.DRINK?end:(int)AnimationPlayback.authored(p,action,AnimationPlayback.ticks(p,action,16),16);
                var rest=DrinkMotion.sample(p,BottleMotion.Action.IDLE,0,16,true);
                var finish=DrinkMotion.sample(p,action,last,16,true);
                assertEquals(rest.x(),finish.x(),1e-7);assertEquals(rest.y(),finish.y(),1e-7);
                for(int tick=0;tick<=last;tick++) {
                    var motion=DrinkMotion.sample(p,action,tick,16,true);
                    var rig=BottleRig.solve(motion,action,tick,16,1,slim,p);
                    var offset=BottleFraming.offset(rig,action,tick,16,true,p);
                    assertTrue(Float.isFinite(rig.hand().position().length())&&Float.isFinite(offset.length()),p.id());
                    assertTrue(DrinkMotion.camera(p,action,tick,16).magnitude()<3,p.id());
                    double clearance=-.27;
                    assertTrue(BottleFraming.bottleFront(rig,offset,DrinkMotion.camera(p,action,tick,16).rotation(),action,tick,16,true,p)<clearance,p.id());
                    if(!p.pullCap())assertEquals(BottleClosures.waterCap(p)?1:0,DrinkMotion.neckReveal(p,action,tick,16,true),"Tea lids keep their separate fixed neck; other screw caps expose the body neck");
                }
            }
        }
    }
    @Test void liquidFaceSamplingNeverTouchesTransparentAtlasHoles() throws Exception {
        for(var p:DrinkCatalog.all())for(var region:p.liquid()) {
            var image=javax.imageio.ImageIO.read(getClass().getResource("/assets/"+region.texture().replace(':','/')));
            for(var n:List.of(new LiquidMesh.V(0,1,0),new LiquidMesh.V(0,-1,0),new LiquidMesh.V(1,0,0),new LiquidMesh.V(.3,.7,-.5).unit())) {
                double level=ContainerLiquid.level(p.liquid(),n,.45);
                for(var face:ContainerLiquid.exposedFaces(p.liquid(),region,n,level)) {
                    var center=new LiquidMesh.V(0,0,0);
                    for(var v:face.points())center=center.add(v.mul(1.0/face.points().size()));
                    var points=new java.util.ArrayList<>(face.points());points.add(center);
                    for(var point:points) {
                        var uv=LiquidUV.sample(region,face,point);
                        int x=Math.min(image.getWidth()-1,(int)(uv.u()*image.getWidth()));
                        int y=Math.min(image.getHeight()-1,(int)(uv.v()*image.getHeight()));
                        assertEquals(255,(image.getRGB(x,y)>>>24)&255,p.id()+" liquid texture hole "+uv);
                    }
                }
            }
        }
    }
    @Test void beerRemovesOnlyThreePixelsAndKeepsItsOriginalNeck() throws Exception {
        for(String id:List.of("light_beer","wheatbeer")) {
            var p=DrinkCatalog.find("farmingtales:"+id);
            assertTrue(p.wine()&&p.pullCap()&&!p.cork());
            try(var reader=new java.io.InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+p.capModel()+".json"))) {
                var cap=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements").get(0).getAsJsonObject();
                assertEquals(3,cap.getAsJsonArray("to").get(1).getAsDouble()-cap.getAsJsonArray("from").get(1).getAsDouble(),1e-6);
                assertEquals(12.1,cap.getAsJsonArray("from").get(1).getAsDouble(),1e-6);
            }
            assertEquals(1,DrinkMotion.neckReveal(p,BottleMotion.Action.DRINK,35,16,true));
        }
    }
}

