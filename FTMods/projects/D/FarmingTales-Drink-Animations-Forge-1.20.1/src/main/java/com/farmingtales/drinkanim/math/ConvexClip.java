package com.farmingtales.drinkanim.math;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;

/** Subtract convex volumes from polygons, preserving each retained fragment's winding. */
public final class ConvexClip {
    public interface Point<T> {
        double x(); double y(); double z();
        T mix(T other,double t);
    }
    public record Plane(double x,double y,double z,double d) {
        public double distance(Point<?> p) {return x*p.x()+y*p.y()+z*p.z()-d;}
        public double distance(Vector3d p) {return x*p.x+y*p.y+z*p.z-d;}
    }
    public static <T extends Point<T>> List<List<T>> subtract(List<T> polygon,List<List<Plane>> volumes) {
        List<List<T>> parts=List.of(polygon);
        for(var volume:volumes) {
            List<List<T>> next=new ArrayList<>(parts.size()+2);
            for(var part:parts)subtractOne(part,volume,next);
            parts=next;
            if(parts.isEmpty())break;
        }
        return parts;
    }
    private static <T extends Point<T>> void subtractOne(List<T> polygon,List<Plane> planes,List<List<T>> out) {
        // A separating plane skips the volume without splitting an unaffected arm face.
        for(var plane:planes) {
            boolean outside=true;
            for(var p:polygon)if(plane.distance(p)<0) {outside=false;break;}
            if(outside) {out.add(polygon);return;}
        }
        List<T> inside=polygon;
        for(var plane:planes) {
            List<T> keep=new ArrayList<>(inside.size()+1),remainder=new ArrayList<>(inside.size()+1);
            T previous=inside.get(inside.size()-1);double a=plane.distance(previous);
            for(T current:inside) {
                double b=plane.distance(current);
                if((a>0)!=(b>0)) {
                    T crossing=previous.mix(current,a/(a-b));
                    keep.add(crossing);remainder.add(crossing);
                }
                if(b>0)keep.add(current);else remainder.add(current);
                previous=current;a=b;
            }
            if(keep.size()>=3)out.add(keep);
            if(remainder.size()<3)return;
            inside=remainder;
        }
        // What remains is inside every plane and is intentionally discarded.
    }
    private ConvexClip() {}
}
