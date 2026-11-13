package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A cohesive surface with delayed gravity response; tilt stays inside the short jar. */
public final class JamLiquid {
    public static final double MAX_TILT_DEGREES=35;
    public static LiquidMesh.V target(Quaternionf jar,float pitch,double sx,double sz) {
        double view=Math.toRadians(Math.max(-28,Math.min(28,pitch)));
        var up=new Quaternionf(jar).conjugate().transform(new Vector3f(0,(float)Math.cos(view),(float)Math.sin(view)));
        double x=.24*Math.tanh(sx*3),z=.24*Math.tanh(sz*3),length=Math.hypot(x,z);
        if(length>.24) {x*=.24/length;z*=.24/length;}
        up.add((float)x,0,(float)z).normalize();
        double horizontal=Math.hypot(up.x,up.z),slope=Math.tan(Math.toRadians(MAX_TILT_DEGREES));
        if(horizontal>1e-8&&horizontal>Math.max(0,up.y)*slope)
            return new LiquidMesh.V(up.x/horizontal*slope,1,up.z/horizontal*slope).unit();
        return new LiquidMesh.V(up.x,up.y,up.z);
    }
    public static final class State {
        private double last=Double.NaN;
        private final Quaternionf drag=new Quaternionf(),surface=new Quaternionf();
        public void reset() {last=Double.NaN;drag.identity();surface.identity();}
        public LiquidMesh.V sample(double time,Quaternionf jar,float pitch,double sx,double sz) {
            LiquidMesh.V desired=target(jar,pitch,sx,sz);
            Quaternionf goal=new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f((float)desired.x(),(float)desired.y(),(float)desired.z()));
            double dt=time-last;last=time;
            if(!Double.isFinite(dt)||dt<0||dt>.25) {drag.set(goal);surface.set(goal);dt=0;}
            int steps=(int)Math.ceil(dt*120);
            for(int i=0;i<steps;i++) {
                double h=dt/steps;
                drag.slerp(goal,(float)(1-Math.exp(-h/.18)));
                surface.slerp(drag,(float)(1-Math.exp(-h/.24)));
            }
            Vector3f up=surface.transform(new Vector3f(0,1,0));
            return new LiquidMesh.V(up.x,up.y,up.z).unit();
        }
    }
    private JamLiquid() {}
}
