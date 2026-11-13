package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BeerFoamTest {
    @Test void beerUsesCanStyleBodyContactInsteadOfItsHandle() {
        int mugs=0;
        for(var p:DrinkCatalog.all())if(p.breweryMug()) {
         mugs++;
         for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1}) {
            var pose=DrinkMotion.sample(p,BottleMotion.Action.IDLE,0,32,true);
            var rig=BottleRig.solve(pose,BottleMotion.Action.IDLE,0,32,side,slim,p);
            var expected=BottleRig.idleGripOffset(side,slim);
            BottleRig.rotation(BottleMotion.IDLE,side).conjugate().transform(expected);
            expected.mul(p.width()/4*p.scale(),1,p.depth()/4*p.scale());
            var actual=new org.joml.Vector3f(rig.hand().position()).sub(rig.bottle().position());
            new org.joml.Quaternionf(rig.bottle().rotation()).conjugate().transform(actual);
            assertTrue(expected.distance(actual)<1e-6,p.id());
         }
        }
        assertEquals(6,mugs);
    }
    @Test void foamStaysInsideMugAndJoinedToLiquidAtEveryFillAndTilt() {
        int count=0;
        for(var p:DrinkCatalog.all())if(BeerFoam.supports(p)) {
            count++;assertTrue(p.cup());assertEquals(0,BeerFoam.mesh(p,new LiquidMesh.V(0,1,0),0,0).size());
            for(double fill:new double[]{.02,.2,.5,.8})for(double x:new double[]{-.9,0,.9})for(double z:new double[]{-.7,0,.7}) {
                var n=LiquidSurface.contained(p,new LiquidMesh.V(x,1,z).unit(),fill);
                double level=ContainerLiquid.level(p.liquid(),n,fill);
                var region=BeerFoam.region(p);var faces=BeerFoam.mesh(p,n,fill,15);
                assertFalse(faces.isEmpty(),p.id()+" fill="+fill+" tilt="+x+","+z);
                for(var f:faces)for(var v:f.points()) {
                    assertTrue(n.dot(v)>=level-1e-8,"Foam does not sink below liquid");
                    var top=BeerFoam.surface(p,n,fill,15);
                    assertTrue(top.normal().dot(v)<=top.level()+1e-8,"Foam stays below its damped surface");
                    assertTrue(v.x()>=region.min()[0]-1e-8&&v.x()<=region.max()[0]+1e-8);
                    assertTrue(v.z()>=region.min()[2]-1e-8&&v.z()<=region.max()[2]+1e-8);
                    assertTrue(v.y()<=region.max()[1]+1e-8);
                }
            }
        }
        assertEquals(6,count);
    }
    @Test void foamRespondsToTimeAndDropsAsBeerIsDrunk() {
        var p=DrinkCatalog.find("brewery:beer_hops");var n=new LiquidMesh.V(0,1,0);
        assertNotEquals(BeerFoam.mesh(p,n,.8,0),BeerFoam.mesh(p,n,.8,12));
        double full=BeerFoam.mesh(p,n,.8,0).stream().flatMap(f->f.points().stream()).mapToDouble(LiquidMesh.V::y).max().orElseThrow();
        double low=BeerFoam.mesh(p,n,.2,0).stream().flatMap(f->f.points().stream()).mapToDouble(LiquidMesh.V::y).max().orElseThrow();
        assertTrue(full-low>.25);
        assertFalse(BeerFoam.supports(DrinkCatalog.ORANGE));
    }
    @Test void allSixMugsHaveTheirOwnFoamTexture() {
        var textures=new java.util.HashSet<String>();
        for(String name:new String[]{"wheat","barley","hops","nettle","haley","oat"}) {
            var p=DrinkCatalog.find("brewery:beer_"+name);
            assertTrue(BeerFoam.supports(p));
            assertEquals(255,p.liquidAlpha(),p.id()+" must retain opaque source colors");
            assertFalse(BeerFoam.mesh(p,new LiquidMesh.V(0,1,0),.8,0).isEmpty());
            textures.add(BeerFoam.region(p).texture());
        }
        assertEquals(6,textures.size());
    }
    @Test void foamTiltIsSmallEvenDuringLargeLiquidSwings() {
        var p=DrinkCatalog.find("brewery:beer_hops");
        for(double pitch:new double[]{-80,-30,-15,0,15,30,80}) {
            double angle=Math.toRadians(pitch);var n=new LiquidMesh.V(Math.sin(angle),Math.cos(angle),0);
            var top=BeerFoam.surface(p,n,.8,40);double tilt=Math.acos(top.normal().y());
            assertTrue(tilt<=Math.toRadians(4)+1e-9);
            assertTrue(tilt<=Math.abs(angle)*.22+1e-9);
        }
    }
}
