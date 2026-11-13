package com.farmingtales.drinkanim.math;

import java.util.List;
import java.util.ArrayList;

/** All sections of a shaped bottle share one level and conserve their combined volume. */
public final class ContainerLiquid {
    public static double level(List<DrinkProfile.Region> regions,LiquidMesh.V normal,double fill) {
        LiquidMesh.V n=normal.unit();double low=Double.POSITIVE_INFINITY,high=Double.NEGATIVE_INFINITY,total=0;
        for(var r:regions) {
            var a=r.minimum();var b=r.maximum();total+=volume(r);
            for(double x:new double[]{a.x(),b.x()})for(double y:new double[]{a.y(),b.y()})for(double z:new double[]{a.z(),b.z()}) {
                double d=n.dot(new LiquidMesh.V(x,y,z));low=Math.min(low,d);high=Math.max(high,d);
            }
        }
        if(regions.isEmpty())return 0;
        double target=total*Math.max(0,Math.min(.999999,fill));
        for(int i=0;i<36;i++) {
            double mid=(low+high)/2,amount=0;
            for(var r:regions)amount+=volume(r)*LiquidMesh.fractionBelow(n,mid,r.minimum(),r.maximum());
            if(amount<target)low=mid;else high=mid;
        }
        return (low+high)/2;
    }
    public static double volume(DrinkProfile.Region r) {
        return (r.max()[0]-r.min()[0])*(r.max()[1]-r.min()[1])*(r.max()[2]-r.min()[2]);
    }
    public static List<LiquidMesh.Face> exposedFaces(List<DrinkProfile.Region> regions,DrinkProfile.Region region,LiquidMesh.V normal,double level) {
        var result=new ArrayList<LiquidMesh.Face>();
        for(var face:LiquidMesh.clip(normal,level,region.minimum(),region.maximum())) {
            if(face.surface()||Math.abs(face.normal().y())<.99) {result.add(face);continue;}
            double y=face.points().get(0).y(),probe=y+face.normal().y()*1e-7;
            List<List<LiquidMesh.V>> pieces=new ArrayList<>();pieces.add(face.points());
            for(var adjacent:regions) {
                if(adjacent==region||probe<=adjacent.min()[1]||probe>=adjacent.max()[1])continue;
                var remainder=new ArrayList<List<LiquidMesh.V>>();
                for(var polygon:pieces)remainder.addAll(outsideRectangle(polygon,adjacent.min()[0],adjacent.min()[2],adjacent.max()[0],adjacent.max()[2]));
                pieces=remainder;
            }
            for(var polygon:pieces)if(hasArea(polygon))result.add(new LiquidMesh.Face(polygon,face.normal(),false));
        }
        return result;
    }
    private static boolean hasArea(List<LiquidMesh.V> points) {
        if(points.size()<3)return false;
        var area=new LiquidMesh.V(0,0,0);var origin=points.get(0);
        for(int i=1;i<points.size()-1;i++)area=area.add(points.get(i).sub(origin).cross(points.get(i+1).sub(origin)));
        return area.dot(area)>1e-18;
    }
    private static List<List<LiquidMesh.V>> outsideRectangle(List<LiquidMesh.V> polygon,double x0,double z0,double x1,double z1) {
        var output=new ArrayList<List<LiquidMesh.V>>();var remaining=polygon;
        for(double[] plane:new double[][]{{1,0,x0},{-1,0,-x1},{0,1,z0},{0,-1,-z1}}) {
            var outside=half(remaining,plane[0],plane[1],plane[2]);
            if(outside.size()>=3)output.add(outside);
            remaining=half(remaining,-plane[0],-plane[1],-plane[2]);
            if(remaining.isEmpty())break;
        }
        return output;
    }
    private static List<LiquidMesh.V> half(List<LiquidMesh.V> polygon,double nx,double nz,double d) {
        var output=new ArrayList<LiquidMesh.V>();if(polygon.isEmpty())return output;
        var a=polygon.get(polygon.size()-1);double da=nx*a.x()+nz*a.z()-d;
        for(var b:polygon) {
            double db=nx*b.x()+nz*b.z()-d;
            if((da<0)!=(db<0))output.add(a.add(b.sub(a).mul(da/(da-db))));
            if(db<=0)output.add(b);
            a=b;da=db;
        }
        return output;
    }
    private ContainerLiquid() {}
}
