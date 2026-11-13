package com.farmingtales.drinkanim.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A volume-preserving, plane-clipped cuboid; handles sideways and inverted bottles. */
public final class LiquidMesh {
    public record V(double x, double y, double z) {
        public V add(V b) { return new V(x+b.x,y+b.y,z+b.z); }
        public V sub(V b) { return new V(x-b.x,y-b.y,z-b.z); }
        public V mul(double n) { return new V(x*n,y*n,z*n); }
        public double dot(V b) { return x*b.x+y*b.y+z*b.z; }
        public V cross(V b) { return new V(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x); }
        public V unit() { double l=Math.sqrt(dot(this));return l<1e-12?new V(0,1,0):mul(1/l); }
    }
    public record Face(List<V> points, V normal, boolean surface) {}
    public static final double WIDTH=3.2/16, HEIGHT=9.6/16, BOTTOM=1.1/16;
    private static final V MIN=new V(-WIDTH/2,BOTTOM,-WIDTH/2);
    private static final V MAX=new V(WIDTH/2,BOTTOM+HEIGHT,WIDTH/2);

    public static List<Face> build(V normal, double fraction) {
        if(fraction<=0) return List.of();
        V n=normal.unit();
        double d=planeOffset(n, Math.min(.99999,fraction));
        return clip(n,d,MIN,MAX);
    }
    public static List<Face> clip(V n,double d,V min,V max) {
        V a=new V(min.x,min.y,min.z), b=new V(max.x,min.y,min.z);
        V c=new V(max.x,min.y,max.z), e=new V(min.x,min.y,max.z);
        V A=new V(min.x,max.y,min.z), B=new V(max.x,max.y,min.z);
        V C=new V(max.x,max.y,max.z), E=new V(min.x,max.y,max.z);
        List<List<V>> box=List.of(List.of(a,b,c,e),List.of(A,E,C,B),List.of(a,A,B,b),List.of(e,c,C,E),List.of(a,e,E,A),List.of(b,B,C,c));
        List<Face> result=new ArrayList<>();
        List<V> rim=new ArrayList<>();
        for(List<V> face:box) {
            List<V> clipped=new ArrayList<>();
            for(int i=0;i<face.size();i++) {
                V from=face.get(i), to=face.get((i+1)%face.size());
                double df=n.dot(from)-d, dt=n.dot(to)-d;
                if(df<=0) clipped.add(from);
                if((df<=0)!=(dt<=0)) {
                    V crossing=from.add(to.sub(from).mul(df/(df-dt)));
                    clipped.add(crossing);
                    if(rim.stream().noneMatch(p->p.sub(crossing).dot(p.sub(crossing))<1e-14)) rim.add(crossing);
                }
            }
            if(clipped.size()>=3) result.add(new Face(clipped,face.get(1).sub(face.get(0)).cross(face.get(2).sub(face.get(0))).unit(),false));
        }
        if(rim.size()>=3) {
            V sum=new V(0,0,0);for(V p:rim) sum=sum.add(p);
            V center=sum.mul(1.0/rim.size());
            V u=n.cross(Math.abs(n.y)<.9?new V(0,1,0):new V(1,0,0)).unit(), v=n.cross(u);
            rim.sort(Comparator.comparingDouble(p->Math.atan2(p.sub(center).dot(v),p.sub(center).dot(u))));
            result.add(new Face(rim,n,true));
        }
        return result;
    }
    public static double fractionBelow(V n,double level,V min,V max) {
        double base=n.x*(n.x>=0?min.x:max.x)+n.y*(n.y>=0?min.y:max.y)+n.z*(n.z>=0?min.z:max.z);
        double[] raw={Math.abs(n.x)*(max.x-min.x),Math.abs(n.y)*(max.y-min.y),Math.abs(n.z)*(max.z-min.z)};
        double[] axes=new double[3];int count=0;double product=1,extent=0;
        for(double a:raw)if(a>1e-9){axes[count++]=a;product*=a;extent+=a;}
        double height=level-base;
        if(height<=0)return 0;if(height>=extent)return 1;
        double volume=0;
        for(int mask=0;mask<(1<<count);mask++) {
            double t=height;for(int j=0;j<count;j++)if((mask&(1<<j))!=0)t-=axes[j];
            if(t>0)volume+=((Integer.bitCount(mask)&1)==0?1:-1)*Math.pow(t,count);
        }
        return Math.max(0,Math.min(1,volume/(product*(count==3?6:count==2?2:1))));
    }
    private static double planeOffset(V n,double fill) {
        double base=n.x*(n.x>=0?MIN.x:MAX.x)+n.y*(n.y>=0?MIN.y:MAX.y)+n.z*(n.z>=0?MIN.z:MAX.z);
        double[] raw={Math.abs(n.x)*WIDTH,Math.abs(n.y)*HEIGHT,Math.abs(n.z)*WIDTH};
        double[] axes=new double[3];int count=0;double extent=0, product=1;
        for(double a:raw) if(a>1e-6) {axes[count++]=a;extent+=a;product*=a;}
        double divisor=product*(count==3?6:count==2?2:1);
        double low=0,high=extent;
        for(int k=0;k<32;k++) {
            double mid=(low+high)/2, volume=0;
            for(int mask=0;mask<(1<<count);mask++) {
                double t=mid;for(int j=0;j<count;j++) if((mask&(1<<j))!=0)t-=axes[j];
                if(t>0) volume+=((Integer.bitCount(mask)&1)==0?1:-1)*Math.pow(t,count);
            }
            if(volume/divisor<fill)low=mid;else high=mid;
        }
        return base+(low+high)/2;
    }
}
