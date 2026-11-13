package com.farmingtales.drinkanim.math;

import com.google.gson.*;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

/** Separating-axis checks use the exported rotated cubes, including the full sleeve. */
class PopsicleClearanceTest {
    private static final DrinkProfile ICE=DrinkCatalog.find("papercraft_magic_decoration:long_island_popsicle_tea");
    record Box(Vector3f center,Vector3f half,Quaternionf rotation) {
        Box world(BottleRig.Transform transform) {
            return new Box(transform.rotation().transform(new Vector3f(center)).add(transform.position()),half,
                new Quaternionf(transform.rotation()).mul(rotation));
        }
        Vector3f axis(int i) {return rotation.transform(new Vector3f(i==0?1:0,i==1?1:0,i==2?1:0));}
        float radius(Vector3f n) {return Math.abs(n.dot(axis(0)))*half.x+Math.abs(n.dot(axis(1)))*half.y+Math.abs(n.dot(axis(2)))*half.z;}
    }
    static List<Box> model(String name,boolean serving) throws Exception {return model(name,serving,ICE);}
    static List<Box> model(String name,boolean serving,DrinkProfile profile) throws Exception {
        try(var in=new InputStreamReader(PopsicleClearanceTest.class.getResourceAsStream(
            "/assets/farmingtales_drink_anim/models/"+name+".json"))) {
            var result=new ArrayList<Box>();
            for(var element:JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("elements")) {
                var e=element.getAsJsonObject();var a=vector(e.getAsJsonArray("from"));var b=vector(e.getAsJsonArray("to"));
                Vector3f center=new Vector3f(a).add(b).mul(.5f),half=new Vector3f(b).sub(a).absolute().mul(.5f);
                Quaternionf q=new Quaternionf();
                if(e.has("rotation")) {
                    var r=e.getAsJsonObject("rotation");var o=vector(r.getAsJsonArray("origin"));
                    float angle=(float)Math.toRadians(r.get("angle").getAsDouble());
                    q.rotationAxis(angle,switch(r.get("axis").getAsString()){case "x"->new Vector3f(1,0,0);case "y"->new Vector3f(0,1,0);default->new Vector3f(0,0,1);});
                    q.transform(center.sub(o)).add(o);
                }
                if(!serving)center.sub(8,4.8f,8);
                result.add(new Box(center.mul(profile.renderScale()/16),half.mul(profile.renderScale()/16),q));
            }
            return result;
        }
    }
    private static Vector3f vector(JsonArray a) {return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());}
    static Box sleeve(BottleRig.Transform hand,boolean slim) {
        float s=BottleRig.ARM_SCALE/16;
        return new Box(new Vector3f(0,-6*s,0),new Vector3f((slim?1.75f:2.25f)*s,6.25f*s,2.25f*s),new Quaternionf()).world(hand);
    }
    static float gap(Box a,Box b) {
        Vector3f distance=new Vector3f(b.center).sub(a.center);float best=-Float.MAX_VALUE;
        var axes=new ArrayList<Vector3f>();
        for(int i=0;i<3;i++){axes.add(a.axis(i));axes.add(b.axis(i));}
        for(int i=0;i<3;i++)for(int j=0;j<3;j++)axes.add(a.axis(i).cross(b.axis(j)));
        for(var n:axes)if(n.lengthSquared()>1e-8) {
            n.normalize();best=Math.max(best,Math.abs(distance.dot(n))-a.radius(n)-b.radius(n));
        }
        return best;
    }
    static void assertWoodGripped(BottleRig.Frame rig,boolean slim) {
        try {
            Box wood=model(ServingAnimations.models(ICE)[0],true).get(0);
            Vector3f end=new Vector3f(wood.center).fma(wood.half.y-.02f*ICE.renderScale()/16,wood.axis(1));
            rig.cork().rotation().transform(end).add(rig.cork().position());
            new Quaternionf(rig.helper().rotation()).conjugate().transform(end.sub(rig.helper().position())).mul(16/BottleRig.ARM_SCALE);
            assertTrue(Math.abs(end.x)<(slim?1.5:2)&&end.y<0&&end.y>-2&&Math.abs(end.z)<2,
                "the actual wooden endpoint must remain inside the skin palm, including when sleeves are disabled: "+end);
        } catch(Exception e) {throw new AssertionError(e);}
    }
    static List<Box> walls(Box body) {
        var walls=new ArrayList<Box>();
        for(int i:new int[]{0,2})for(int sign:new int[]{-1,1}) {
            var half=new Vector3f(body.half);float radius=half.get(i);half.setComponent(i,.0015f);
            walls.add(new Box(new Vector3f(body.center).fma(sign*(radius-.0015f),body.axis(i)),half,body.rotation));
        }
        return walls;
    }
    static List<Box> movingFruit(List<Box> fruit) {
        var result=new ArrayList<Box>();
        Vector3f pivot=new Vector3f(12.582392f-8,7.63f-4.8f,0).mul(ICE.renderScale()/16);
        for(float x:new float[]{-.1216f,0,.1216f})for(float z:new float[]{-.1216f,0,.1216f})for(var box:fruit) {
            var q=new Quaternionf().rotateX(x).rotateZ(z);
            result.add(new Box(q.transform(new Vector3f(box.center).sub(pivot)).add(pivot),box.half,new Quaternionf(q).mul(box.rotation)));
        }
        return result;
    }
    static void audit(boolean enforce,String[] args) throws Exception {
        var solid=model(ICE.bodyModel(),false);var fruit=movingFruit(model("animation/papercraft/long_island_popsicle_tea_fruit_0",false));
        var glass=walls(solid.get(0));
        float[] worst={100,100,100,100,100};String[] where={"","","","",""};
        for(var action:(args.length>0?new Action[]{Action.valueOf(args[0])}:new Action[]{Action.DRINK,Action.INSPECT}))for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})
        for(double t=.125;t<=(action==Action.DRINK?41:PopsicleMotion.INSPECT_TICKS);t+=.125) {
            var r=BottleRig.solve(DrinkMotion.sample(ICE,action,t,32,true),action,t,32,side,slim,ICE);
            var hand=sleeve(r.helper(),slim);
            var ice=model(ServingAnimations.propModel(ICE,action,t),true);
            var groups=List.of(ice.size()>1?ice.subList(1,ice.size()):List.<Box>of(),solid,fruit);
            for(int k=0;k<3;k++)if(r.assistance()>0)for(var box:groups.get(k)) {
                if(box.half.y==0)continue;
                float gap=gap(hand,box.world(k==0?r.cork():r.bottle()));
                if(gap<worst[k]){worst[k]=gap;where[k]=action+" t="+t+" side="+side+" slim="+slim;}
            }
            if((action==Action.DRINK&&t<=34)||action==Action.INSPECT)for(var food:ice)for(var wall:glass) {
                float g=gap(food.world(r.cork()),wall.world(r.bottle()));
                if(g<worst[3]){worst[3]=g;where[3]=action+" t="+t+" side="+side;}
            }
            for(var food:ice)for(var lemon:fruit) {
                float g=gap(food.world(r.cork()),lemon.world(r.bottle()));
                if(g<worst[4]){worst[4]=g;where[4]=action+" t="+t+" side="+side;}
            }
        }
        for(int k=0;k<5;k++) {
            String label=new String[]{"food","cup","lemon","glass","ice-lemon"}[k]+" gap="+worst[k]+" "+where[k];
            System.out.println(label);if(enforce)assertTrue(worst[k]>(k<3?.003f:.001f),label);
        }
    }
    @Test void sleevesClearTheActualFoodGlassAndMovingLemonThroughoutBothActions() throws Exception {audit(true,new String[0]);}
    @Test void inspectionSpringKeepsIceClearOfGlassAndMovingFruit() throws Exception {
        var glass=walls(model(ICE.bodyModel(),false).get(0));
        var fruit=movingFruit(model("animation/papercraft/long_island_popsicle_tea_fruit_0",false));
        var ice=model(ServingAnimations.models(ICE)[0],true);
        for(int side:new int[]{-1,1}) {
            var spring=new DecorationMotion.State();
            for(int frame=0;frame<=3*AnimationPlayback.ticks(ICE,Action.INSPECT,32);frame++) {
                double t=AnimationPlayback.authored(ICE,Action.INSPECT,frame/3.0,32);
                var r=BottleRig.solve(DrinkMotion.sample(ICE,Action.INSPECT,t,32,true),Action.INSPECT,t,32,side,false,ICE);
                spring.advance(frame/60.0,r.bottle().position(),r.bottle().rotation(),0,0);
                r=ServingAnimations.sway(ICE,Action.INSPECT,t,r,spring.x(),spring.z());
                for(var food:ice) {
                    for(var wall:glass)assertTrue(gap(food.world(r.cork()),wall.world(r.bottle()))>.001,"dynamic ice/glass at "+t+" side "+side);
                    for(var lemon:fruit)assertTrue(gap(food.world(r.cork()),lemon.world(r.bottle()))>.001,"dynamic ice/lemon at "+t+" side "+side);
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {audit(false,args);mouthReport(false);}
    @Test void bitesEnterTheLowerCameraEdgeAndWithdrawBeforeTheNextBite() throws Exception {mouthReport(true);}
    static void mouthReport(boolean enforce) throws Exception {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{true,false})for(int bite:new int[]{14,22}) {
            float[] front=new float[2],bottom=new float[2],top=new float[2];
            for(int phase=0;phase<2;phase++) {
                double t=phase==0?bite-.01:bite+2.8;
                var r=BottleRig.solve(DrinkMotion.sample(ICE,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,ICE);
                var cam=DrinkMotion.camera(ICE,Action.DRINK,t,32).rotation();var offset=BottleFraming.offset(r,Action.DRINK,t,32,true,ICE,cam);
                front[phase]=-100;bottom[phase]=100;top[phase]=-100;
                var parts=model(ServingAnimations.propModel(ICE,Action.DRINK,t),true);
                for(var box:parts.subList(1,parts.size()))for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})for(int z:new int[]{-1,1}) {
                    var point=box.rotation.transform(new Vector3f(x*box.half.x,y*box.half.y,z*box.half.z)).add(box.center);
                    r.cork().rotation().transform(point).add(r.cork().position()).add(offset);cam.transform(point);
                    front[phase]=Math.max(front[phase],point.z);bottom[phase]=Math.min(bottom[phase],point.y/-point.z);top[phase]=Math.max(top[phase],point.y/-point.z);
                }
            }
            String label="bite="+bite+" side="+side+" slim="+slim+" front="+Arrays.toString(front)+" bottom="+Arrays.toString(bottom)+" top="+Arrays.toString(top);
            if(enforce){assertTrue(bottom[0]<-.8,label);assertTrue(top[0]>-.46,label);assertTrue(top[1]>-.30&&bottom[1]>-.60,label);assertTrue(front[0]-front[1]>.10,label);}
            else System.out.println(label);
        }
    }
}
