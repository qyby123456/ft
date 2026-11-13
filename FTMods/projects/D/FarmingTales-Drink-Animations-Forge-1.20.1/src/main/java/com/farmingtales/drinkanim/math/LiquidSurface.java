package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Limit head-driven motion without flattening the bottle's own drinking or toss rotation. */
public final class LiquidSurface {
    /** walkDist advances in first person even when the player model is not rendered. */
    public static double walkingImpulse(double walkDistance,double speed,boolean grounded) {
        return grounded?Math.sin(walkDistance*6)*Math.max(0,Math.min(.4,speed))*.30:0;
    }
    public static final float MAX_VIEW_PITCH=25;
    public static final double MAX_SLOSH=.18;
    private static final float BOTTLE_VIEW_PITCH=35;
    private static final double BOTTLE_SLOSH=.27, BOTTLE_GAIN=1.45;
    private static final float CUP_VIEW_PITCH=30;
    private static final double CUP_SLOSH=.22, CUP_GAIN=1.2;
    private static boolean bottle(DrinkProfile profile) {return !profile.cup()&&!profile.can()&&!profile.food();}
    public static float viewPitch(float pitch) {return Math.max(-MAX_VIEW_PITCH,Math.min(MAX_VIEW_PITCH,pitch));}
    public static float viewPitch(DrinkProfile profile,float pitch) {
        float limit=bottle(profile)?BOTTLE_VIEW_PITCH:profile.cup()?CUP_VIEW_PITCH:MAX_VIEW_PITCH;
        return Math.max(-limit,Math.min(limit,pitch));
    }
    public static LiquidMesh.V normal(Quaternionf bottle,float pitch,double sx,double sz,double ripple) {
        return normalWithLimits(bottle,viewPitch(pitch),sx,sz,ripple,1,MAX_SLOSH);
    }
    public static LiquidMesh.V normal(DrinkProfile profile,Quaternionf rotation,float pitch,double sx,double sz,double ripple) {
        if(profile.food())return new LiquidMesh.V(0,1,0);
        if(profile.jam())return JamLiquid.target(rotation,pitch,sx,sz);
        boolean larger=bottle(profile);
        return normalWithLimits(rotation,viewPitch(profile,pitch),sx,sz,ripple,
            larger?BOTTLE_GAIN:profile.cup()?CUP_GAIN:1,larger?BOTTLE_SLOSH:profile.cup()?CUP_SLOSH:MAX_SLOSH);
    }
    private static LiquidMesh.V normalWithLimits(Quaternionf bottle,float pitch,double sx,double sz,double ripple,double gain,double limit) {
        double angle=Math.toRadians(pitch);
        var up=new Vector3f(0,(float)Math.cos(angle),(float)Math.sin(angle));
        new Quaternionf(bottle).conjugate().transform(up);
        sx=(sx+ripple)*gain;sz=(sz+ripple*.7)*gain;
        double length=Math.hypot(sx,sz);
        if(length>limit) {sx*=limit/length;sz*=limit/length;}
        return new LiquidMesh.V(up.x+sx,up.y,up.z+sz).unit();
    }
    public static LiquidMesh.V contained(DrinkProfile profile,LiquidMesh.V normal,double fill) {
        if((!profile.cup()&&!profile.jam()&&!profile.food())||profile.liquid().isEmpty())return normal;
        double horizontal=Math.hypot(normal.x(),normal.z());
        if(horizontal<1e-8)return new LiquidMesh.V(0,1,0);
        // Test the actual volume below a plane touching the uphill rim. The old
        // twice-radius estimate reserved the same headroom twice and froze swirls.
        double yaw=Math.toRadians(-profile.cupShape().liquidYaw());
        double x=normal.x()/horizontal,z=normal.z()/horizontal;
        double localX=Math.cos(yaw)*x+Math.sin(yaw)*z;
        double localZ=-Math.sin(yaw)*x+Math.cos(yaw)*z;
        double wanted=Math.min(Math.tan(Math.toRadians(profile.jam()?JamLiquid.MAX_TILT_DEGREES:60)),horizontal/Math.max(.001,normal.y()));
        double low=0,high=wanted;
        if(capacityAtRim(profile,localX,localZ,high)>=fill)low=high;
        else for(int i=0;i<24;i++) {
            double mid=(low+high)*.5;
            if(capacityAtRim(profile,localX,localZ,mid)>=fill)low=mid;else high=mid;
        }
        double scale=low/horizontal;
        return new LiquidMesh.V(normal.x()*scale,1,normal.z()*scale).unit();
    }
    private static double capacityAtRim(DrinkProfile profile,double x,double z,double slope) {
        var n=new LiquidMesh.V(x*slope,1,z*slope);
        double ceiling=profile.liquid().stream().mapToDouble(r->r.max()[1]).max().orElse(0);
        double rim=Math.min(profile.mouthY()/16.0,ceiling)-.00001;
        double level=Double.POSITIVE_INFINITY;
        for(var r:profile.liquid())for(double px:new double[]{r.min()[0],r.max()[0]})
            for(double pz:new double[]{r.min()[2],r.max()[2]})level=Math.min(level,rim+n.x()*px+n.z()*pz);
        double amount=0,total=0;
        for(var r:profile.liquid()) {
            double v=ContainerLiquid.volume(r);total+=v;
            amount+=v*LiquidMesh.fractionBelow(n,level,r.minimum(),r.maximum());
        }
        return total>0?amount/total:0;
    }
    private LiquidSurface() {}
}
