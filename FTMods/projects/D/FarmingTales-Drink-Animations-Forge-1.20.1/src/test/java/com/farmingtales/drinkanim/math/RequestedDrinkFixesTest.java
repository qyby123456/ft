package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3d;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class RequestedDrinkFixesTest {
    @Test void joRestoresTheOriginalCarryHandAndKeepsTheCapMotion() {
        var p=DrinkCatalog.find("vinery:jo_special_mixture");
        // A second screw-cap ID with Jo's dimensions exercises the unchanged rig.
        var original=new DrinkProfile("vinery:jellie_wine",p.style(),p.scale(),p.width(),p.depth(),
            p.capPivot(),p.neckJoin(),p.neckMin(),p.neckMax(),p.mouthY(),p.bodyModel(),p.capModel(),p.neckModel(),
            p.boundsMin(),p.boundsMax(),p.liquid(),p.canLid(),p.cupShape());
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1})
        for(var action:Action.values())for(double t=0;t<=120;t+=.5) {
            var pose=DrinkMotion.sample(p,action,t,32,true);
            var actual=BottleRig.solve(pose,action,t,32,side,slim,p);
            var expected=BottleRig.solve(pose,action,t,32,side,slim,original);
            assertEquals(expected.hand(),actual.hand(),"Restore the carry hand for "+action+" "+t);
            assertEquals(expected.bottle(),actual.bottle());
            assertEquals(expected.cork(),actual.cork());
            if(action!=Action.DRINK||t>=42)assertEquals(expected.helper(),actual.helper());
        }
    }
    @Test void joOpeningHandClearsTheBottleShoulder() {
        var collisions=new java.util.TreeMap<Double, String>();
        double cameraClearance=0;
        var p=DrinkCatalog.find("vinery:jo_special_mixture");
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1})
        for(double t=6;t<42;t+=.25) {
            var rig=BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,32,true),Action.DRINK,t,32,side,slim,p);
            var inverse=new Quaternionf(rig.bottle().rotation()).conjugate();
            for(float x:new float[]{slim?-1.75f:-2.25f,0,slim?1.75f:2.25f})
            for(float y=-12.25f;y<=.25f;y+=.5f)for(float z:new float[]{-2.25f,0,2.25f}) {
                var v=rig.helper().rotation().transform(new Vector3f(x,y,z).mul(BottleRig.ARM_SCALE/16)).add(rig.helper().position());
                inverse.transform(v.sub(rig.bottle().position())).mul(16/p.renderScale()).add(8,4.8f,8);
                boolean body=v.x>4&&v.x<12&&v.y>1&&v.y<7&&v.z>4&&v.z<12;
                boolean shoulder=v.x>5&&v.x<11&&v.y>7&&v.y<8&&v.z>5&&v.z<11;
                if(body||shoulder) {
                    collisions.putIfAbsent(t,v+" slim="+slim+" side="+side);
                    var shift=inverse.transform(new Vector3f(0,0,1)).mul((float)rig.assistance()*16/p.renderScale());
                    cameraClearance=Math.max(cameraClearance,((body?4:5)-v.z)/shift.z);
                }
            }
        }
        assertTrue(collisions.isEmpty(),"Camera clearance="+cameraClearance+" intersections="+collisions);
    }

    @Test void opaqueBeerMasksDoNotExtendAboveOrBehindTheMug() {
        for(var p:DrinkCatalog.all())if(p.breweryMug()) {
            var mask=HandVolumes.occlusion(p,new Vector3d(8,14,35));
            for(var point:new Vector3d[]{new Vector3d(8,8.1,8),new Vector3d(8,4,-8),new Vector3d(12,4,8)})
                assertFalse(mask.stream().anyMatch(box->box.stream().allMatch(plane->plane.distance(point)<=0)),p.id()+" wrongly clips "+point);
            assertTrue(mask.stream().anyMatch(box->box.stream().allMatch(plane->plane.distance(new Vector3d(8,4,8))<=0)),p.id()+" must hide cavity intrusion");
        }
    }

    @Test void allFiveSlicesUseTheCocktailSpringAndReturnToTheirSeats() {
        for(String fruit:new String[]{"orange","lemon","strawberry","apple","watermelon"}) {
            var p=DrinkCatalog.find("farmingtales:"+fruit+"_juice_1");
            var parts=DecorationMotion.parts(p);assertEquals(1,parts.size());
            var part=parts.get(0);assertEquals(DecorationMotion.Kind.FRUIT,part.kind());
            var state=new DecorationMotion.State();
            state.advance(0,new Vector3f(),new Quaternionf(),0,0);
            for(int i=1;i<=24;i++)state.advance(i/120.,new Vector3f(i*.002f,0,0),new Quaternionf(),.2,.15);
            var moved=state.transform(part,p,new LiquidMesh.V(0,1,0),.8);
            assertTrue(moved.rotation().angle()>.005,p.id());assertEquals(new Vector3f(),moved.offset());
            for(int i=25;i<=600;i++)state.advance(i/120.,new Vector3f(.048f,0,0),new Quaternionf(),0,0);
            assertEquals(0,state.transform(part,p,new LiquidMesh.V(0,1,0),.8).rotation().angle(),.001,p.id());
        }
    }

    @Test void highlandWhiskyConservesVolumeAndFallsAsItIsDrunk() {
        var p=DrinkCatalog.find("brewery:whiskey_highland_hearth");
        assertFalse(p.liquid().isEmpty());
        for(var normal:new LiquidMesh.V[]{new LiquidMesh.V(0,1,0),new LiquidMesh.V(.4,.8,.3).unit()}) {
            double previous=Double.POSITIVE_INFINITY;
            for(double fill:new double[]{.8,.5,.15}) {
                double level=ContainerLiquid.level(p.liquid(),normal,fill),volume=0,total=0;
                assertTrue(level<previous);previous=level;
                for(var r:p.liquid()) {total+=ContainerLiquid.volume(r);volume+=ContainerLiquid.volume(r)*LiquidMesh.fractionBelow(normal,level,r.minimum(),r.maximum());}
                assertEquals(fill,volume/total,1e-7);
            }
        }
        assertTrue(BottleClosures.longCork(p));
    }
}
