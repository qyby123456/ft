package com.farmingtales.drinkanim.math;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RibbonMotionTest {
    private static final Set<String> IDS=Set.of("kaleidoscope_world_liquor:bamboo_leaf_green_liquor",
        "kaleidoscope_world_liquor:maotai","kaleidoscope_world_liquor:dassai","kaleidoscope_world_liquor:strongbow");

    @Test void dassaiFollowsViewAndWalkingInEveryDirectionWhileItsLocalHoldIsStationary() {
        var profile=DrinkCatalog.find("kaleidoscope_world_liquor:dassai");
        var part=DecorationMotion.parts(profile).get(0);
        var hold=BottleRig.solve(DrinkMotion.sample(profile,BottleMotion.Action.IDLE,0,32,true),BottleMotion.Action.IDLE,0,32,1,false,profile).cork();
        var root=new Vector3f(part.pivot());var tip=new Vector3f(root).add(-2.3097f,-3.5f,.9567f);
        for(int axis=0;axis<4;axis++)for(int sign:new int[]{-1,1})for(int fps:new int[]{30,60,120}) {
            var state=new DecorationMotion.State();var oldScreenOnly=new DecorationMotion.State();double peak=0;
            for(int f=0;f<=fps*7;f++) {
                double t=f/(double)fps,drive=t<1?sign*Math.pow(Math.sin(Math.PI*t),2):0;
                var eye=new org.joml.Vector3d(axis==2?.45*drive:0,0,axis==3?.45*drive:0);
                // LocalPlayer view changes can arrive at tick boundaries while the hand renders more often.
                double viewTime=Math.floor(t*20)/20,turn=viewTime<1?sign*Math.pow(Math.sin(Math.PI*viewTime),2):0;
                var view=new Quaternionf().rotateY(axis==0?(float)(.6*turn):0).rotateX(axis==1?(float)(.45*turn):0);
                var at=hold.position();var orientation=hold.rotation();
                state.advanceWithView(t,at,orientation,eye,view);
                oldScreenOnly.advance(t,at,orientation,0,0);
                peak=Math.max(peak,state.ribbonPoint(part,tip).distance(tip));
                assertEquals(0,oldScreenOnly.ribbonPoint(part,tip).distance(tip),1e-6,"Previous camera-space input missed this motion");
                assertEquals(0,state.ribbonPoint(part,root).distance(root),1e-6,"The attachment stays pinned");
            }
            assertTrue(peak>.05,"axis="+axis+" sign="+sign+" fps="+fps+" peak="+peak);
            assertTrue(state.ribbonPoint(part,tip).distance(tip)<.0001,"Settles after the player stops");
        }
    }

    @Test void viewDrivenRibbonKeepsPrecisionFarFromSpawnAndResetsOnTeleport() {
        var part=DecorationMotion.parts(DrinkCatalog.find("kaleidoscope_world_liquor:dassai")).get(0);
        var tip=new Vector3f(part.pivot()).add(-2.3097f,-3.5f,.9567f);
        var near=new DecorationMotion.State();var far=new DecorationMotion.State();
        for(int f=0;f<240;f++) {
            double time=f/120.;var eye=new org.joml.Vector3d(.2*Math.sin(time*5),.05*Math.cos(time*6),.3*Math.sin(time*3));
            var view=new Quaternionf().rotateY((float)(.3*Math.sin(time*5))).rotateX((float)(.2*Math.sin(time*4)));
            var at=new Vector3f(.3f,-.15f,-.85f);
            near.advanceWithView(time,at,new Quaternionf(),eye,view);
            far.advanceWithView(time,at,new Quaternionf(),new org.joml.Vector3d(eye).add(20000000,180,-20000000),view);
            assertEquals(0,near.ribbonPoint(part,tip).distance(far.ribbonPoint(part,tip)),.0001);
        }
        far.advanceWithView(2,new Vector3f(),new Quaternionf(),new org.joml.Vector3d(4000,100,8000),new Quaternionf());
        assertEquals(0,far.ribbonPoint(part,tip).distance(tip),1e-6);
        far.advanceWithView(1,new Vector3f(),new Quaternionf(),new org.joml.Vector3d(),new Quaternionf());
        assertEquals(0,far.ribbonPoint(part,tip).distance(tip),1e-6);
    }

    @Test void onlyRequestedOrnamentsUseRigidSwingAndMaotaiHasNoDuplicateStaticRibbon() throws Exception {
        var found=new java.util.HashSet<String>();
        for(var p:DrinkCatalog.all())for(var part:DecorationMotion.parts(p))if(part.ribbon()!=null) {
            found.add(p.id());assertTrue(part.ribbon().length()>2);assertTrue(part.ribbon().gain()>0&&part.ribbon().gain()<=.7);assertTrue(part.ribbon().rigid());
            assertEquals(8,part.ribbon().maxAngle());
            assertEquals(!p.id().endsWith(":maotai"),part.capAttached());
        }
        assertEquals(IDS,found);
        var p=DrinkCatalog.find("kaleidoscope_world_liquor:maotai");
        for(String model:new String[]{p.bodyModel(),p.capModel(),DecorationMotion.parts(p).get(0).model()}) {
            try(var in=new InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+model+".json"))) {
                var elements=JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("elements");
                long ribbons=java.util.stream.StreamSupport.stream(elements.spliterator(),false)
                    .filter(e->e.getAsJsonObject().has("name")&&e.getAsJsonObject().get("name").getAsString().equals("source_3")).count();
                assertEquals(model.endsWith("_ribbon")?1:0,ribbons);
            }
        }
    }

    @Test void axialTurnsSwingWholeOrnamentsAfterTheBottleStopsAndThenSettleAtEveryFrameRate() {
        for(String id:IDS)for(int fps:new int[]{30,60,120}) {
            var part=DecorationMotion.parts(DrinkCatalog.find(id)).get(0);
            var state=new DecorationMotion.State();var root=new Vector3f(part.pivot());
            var tip=new Vector3f(root).add(part.ribbon().length()*.8f,0,0);
            double peak=0,after=0;
            for(int f=0;f<=fps*7;f++) {
                double t=f/(double)fps;
                state.advance(t,new Vector3f(),new Quaternionf().rotateY((float)(.9*BottleMotion.smooth(t/.5))),0,0);
                var bent=state.ribbonPoint(part,tip);
                assertTrue(bent.isFinite());assertEquals(0,root.distance(state.ribbonPoint(part,root)),1e-7,id);
                peak=Math.max(peak,bent.distance(tip));
                if(f==fps)after=bent.distance(tip);
            }
            assertTrue(peak>.05,id+" gentle swing: "+peak);
            assertTrue(after>.001,id+" delayed free end: "+after);
            assertTrue(state.ribbonPoint(part,tip).distance(tip)<.0001,id+" settles");
            state.reset();assertEquals(0,state.ribbonPoint(part,tip).distance(tip),1e-7);
        }
    }

    @Test void wholeModelStaysRigidWithPinnedRootAndMaotaiNeverSwingsIntoItsBottleWall() throws Exception {
        for(String id:IDS) {
            var profile=DrinkCatalog.find(id);var part=DecorationMotion.parts(profile).get(0);
            var root=new Vector3f(part.pivot());var state=new DecorationMotion.State();
            var points=modelPoints(part);
            for(int f=0;f<240;f++) {
                float t=f/120f;
                state.advance(t,new Vector3f(.12f*(float)Math.sin(6*t),0,.08f*(float)Math.cos(5*t)),
                    new Quaternionf().rotateXYZ(.2f*(float)Math.sin(t*4),t*3,.3f*(float)Math.sin(t*5)),0,0);
                var transform=state.transform(part,profile,new LiquidMesh.V(0,1,0),1);
                assertEquals(0,transform.offset().length(),1e-7);
                assertEquals(0,root.distance(state.ribbonPoint(part,root)),1e-7);
                for(var point:points) {
                    var moved=state.ribbonPoint(part,point);
                    assertTrue(moved.isFinite());
                    var rendered=transform.rotation().transform(new Vector3f(point).sub(root)).add(root);
                    assertEquals(0,rendered.distance(moved),1e-5,"Whole-model rendering matches measured motion");
                    if(id.endsWith(":maotai"))assertTrue(moved.x<=point.x+1e-5,"Red ornament stays outside its original wall clearance");
                    for(var other:points) {
                        assertEquals(point.distance(other),moved.distance(state.ribbonPoint(part,other)),1e-5,"No stretching or bending: "+id);
                        var middle=new Vector3f(point).lerp(other,.5f);
                        var movedMiddle=new Vector3f(moved).lerp(state.ribbonPoint(part,other),.5f);
                        assertEquals(0,movedMiddle.distance(state.ribbonPoint(part,middle)),1e-5,"Edges stay straight: "+id);
                    }
                }
            }
        }
    }

    @Test void abruptMovementCannotExceedEightDegreesOrPushExposedSurfacesThroughBodyAndCap() throws Exception {
        for(String id:IDS) {
            var profile=DrinkCatalog.find(id);var part=DecorationMotion.parts(profile).get(0);
            var corners=modelPoints(part);var origin=corners.get(0);
            var spans=new java.util.ArrayList<Vector3f>();
            for(int index:new int[]{1,2,4}) {
                var span=new Vector3f(corners.get(index)).sub(origin);
                if(span.lengthSquared()>1e-6)spans.add(span);
            }
            assertEquals(2,spans.size());
            var samples=new java.util.ArrayList<Vector3f>();
            for(int u=0;u<=20;u++)for(int v=0;v<=20;v++)
                samples.add(new Vector3f(origin).fma(u/20f,spans.get(0)).fma(v/20f,spans.get(1)));
            var boxes=solidBounds(profile);var state=new DecorationMotion.State();double peakAngle=0;
            for(int f=0;f<360;f++) {
                float t=f/120f;
                state.advance(t,new Vector3f((float)Math.sin(t*31),(float)Math.cos(t*29),(float)Math.sin(t*23)),
                    new Quaternionf().rotateXYZ(t*11,(float)Math.sin(t*19)*3,t*17),Math.sin(t*13),Math.cos(t*17));
                var transform=state.transform(part,profile,new LiquidMesh.V(0,1,0),1);
                double angle=2*Math.acos(Math.min(1,Math.abs(transform.rotation().w)));
                assertTrue(angle<=Math.toRadians(8)+1e-5,"Hard swing limit: "+id+" "+angle);
                peakAngle=Math.max(peakAngle,angle);
                for(var point:samples) {
                    var moved=state.ribbonPoint(part,point);
                    for(var box:boxes)if(!inside(point,box,-.005f))
                        assertFalse(inside(moved,box,.005f),"Exposed surface must stay outside: "+id+" "+point+" -> "+moved);
                }
            }
            assertTrue(peakAngle>Math.toRadians(3),"Retain visible motion under strong input: "+id);
        }
    }

    private static boolean inside(Vector3f point,Vector3f[] box,float inset) {
        return point.x>box[0].x+inset&&point.x<box[1].x-inset&&point.y>box[0].y+inset&&point.y<box[1].y-inset
            &&point.z>box[0].z+inset&&point.z<box[1].z-inset;
    }
    private java.util.List<Vector3f[]> solidBounds(DrinkProfile profile) throws Exception {
        var boxes=new java.util.ArrayList<Vector3f[]>();
        for(String model:new String[]{profile.bodyModel(),profile.capModel()}) {
            try(var in=new InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+model+".json"))) {
                for(var entry:JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("elements")) {
                    var element=entry.getAsJsonObject();var from=element.getAsJsonArray("from");var to=element.getAsJsonArray("to");
                    if(element.has("rotation"))assertEquals(0,element.getAsJsonObject("rotation").get("angle").getAsFloat());
                    var a=new Vector3f(from.get(0).getAsFloat(),from.get(1).getAsFloat(),from.get(2).getAsFloat());
                    var b=new Vector3f(to.get(0).getAsFloat(),to.get(1).getAsFloat(),to.get(2).getAsFloat());
                    var lo=new Vector3f(a).min(b);var hi=new Vector3f(a).max(b);
                    if(hi.x>lo.x&&hi.y>lo.y&&hi.z>lo.z)boxes.add(new Vector3f[]{lo,hi});
                }
            }
        }
        return boxes;
    }

    private java.util.List<Vector3f> modelPoints(DecorationMotion.Part part) throws Exception {
        var points=new java.util.ArrayList<Vector3f>();
        try(var in=new InputStreamReader(getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/"+part.model()+".json"))) {
            for(var entry:JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("elements")) {
                var element=entry.getAsJsonObject();var from=element.getAsJsonArray("from");var to=element.getAsJsonArray("to");
                var rotation=new Quaternionf();var origin=new Vector3f();var scale=new Vector3f(1);
                if(element.has("rotation")) {
                    var r=element.getAsJsonObject("rotation");var o=r.getAsJsonArray("origin");
                    origin.set(o.get(0).getAsFloat(),o.get(1).getAsFloat(),o.get(2).getAsFloat());
                    float angle=(float)Math.toRadians(r.get("angle").getAsDouble());
                    var axis=switch(r.get("axis").getAsString()){case "x"->new Vector3f(1,0,0);case "y"->new Vector3f(0,1,0);default->new Vector3f(0,0,1);};
                    rotation.rotationAxis(angle,axis);
                    if(r.has("rescale")&&r.get("rescale").getAsBoolean()) {
                        float s=1/(float)Math.cos(angle);scale.set(axis.x==0?s:1,axis.y==0?s:1,axis.z==0?s:1);
                    }
                }
                for(int i=0;i<8;i++) {
                    var point=new Vector3f(((i&1)==0?from:to).get(0).getAsFloat(),((i&2)==0?from:to).get(1).getAsFloat(),((i&4)==0?from:to).get(2).getAsFloat());
                    points.add(rotation.transform(point.sub(origin).mul(scale)).add(origin));
                }
            }
        }
        return points;
    }
}
