package com.farmingtales.drinkanim.math;

import java.util.*;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandVolumesTest {
    private record V(double x,double y,double z,double u) implements ConvexClip.Point<V> {
        public V mix(V q,double t) {return new V(x+(q.x-x)*t,y+(q.y-y)*t,z+(q.z-z)*t,u+(q.u-u)*t);}
    }
    private static List<V> square(double z) {return List.of(new V(-2,-2,z,0),new V(2,-2,z,1),new V(2,2,z,1),new V(-2,2,z,0));}
    private static HandVolumes.Box box() {return new HandVolumes.Box(new Vector3d(-1),new Vector3d(1),new Matrix4d());}
    private static double area(List<List<V>> parts) {
        double sum=0;
        for(var p:parts)for(int i=0;i<p.size();i++) {var a=p.get(i);var b=p.get((i+1)%p.size());sum+=(a.x*b.y-b.x*a.y)/2;}
        return sum;
    }
    @Test void cutsAcrossAnArmFaceEvenWhenEveryOriginalCornerIsOutside() {
        var result=ConvexClip.subtract(square(0),List.of(box().interior()));
        assertEquals(12,area(result),1e-9);
        for(var polygon:result)for(var vertex:polygon)assertEquals((vertex.x+2)/4,vertex.u,1e-9,"Preserve skin UVs at cut edges");
    }
    @Test void retainsTheHandInFrontAndHidesTheFarHandThroughTheBottle() {
        var mask=List.of(box().occlusion(new Vector3d(0,0,5)));
        var front=square(2);
        assertEquals(List.of(front),ConvexClip.subtract(front,mask));
        assertEquals(9.75,area(ConvexClip.subtract(square(0),mask)),1e-8);
        assertEquals(0,area(ConvexClip.subtract(square(-3),mask)),1e-8);
    }
    @Test void overlappingVolumesDoNotDiscardOrDuplicateTheOutsideArea() {
        var a=new HandVolumes.Box(new Vector3d(-1,-1,-1),new Vector3d(.5,1,1),new Matrix4d());
        var b=new HandVolumes.Box(new Vector3d(-.5,-1,-1),new Vector3d(1,1,1),new Matrix4d());
        assertEquals(12,area(ConvexClip.subtract(square(0),List.of(a.interior(),b.interior()))),1e-9);
        assertEquals(List.of(square(4)),ConvexClip.subtract(square(4),List.of(a.interior(),b.interior())));
    }
    @Test void silhouettePlanesAgreeWithRayIntersectionsForAllRotatedContainerParts() {
        var random=new Random(52912);
        for(var profile:DrinkCatalog.all()) {
            assertFalse(HandVolumes.boxes(profile).isEmpty());
            for(Vector3d eye:List.of(new Vector3d(8,10,40),new Vector3d(34,2,-20),new Vector3d(-12,35,12)))
                for(var b:HandVolumes.boxes(profile)) {
                    var silhouette=b.occlusion(eye);
                    for(int i=0;i<150;i++) {
                        Vector3d point=new Vector3d(random.nextDouble()*40-12,random.nextDouble()*40-10,random.nextDouble()*40-12);
                        boolean hidden=silhouette.stream().allMatch(p->p.distance(point)<0);
                        double first=0,last=1;
                        for(var plane:b.interior()) {
                            double start=plane.distance(eye),delta=plane.distance(point)-start;
                            if(Math.abs(delta)<1e-12) {if(start>0)last=-1;}
                            else if(delta<0)first=Math.max(first,-start/delta);
                            else last=Math.min(last,-start/delta);
                        }
                        assertEquals(first<last&&last>0&&first<1,hidden,profile.id()+" eye="+eye+" point="+point);
                    }
                }
        }
    }
}
