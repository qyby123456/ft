package com.farmingtales.drinkanim.math;

/** Distance-driven movement, with independent fading weights and damped jump/turn inertia. */
public final class Locomotion {
    private double weight,oldWeight,run,oldRun,air,oldAir,distance,oldDistance;
    private final SloshSpring inertia=new SloshSpring();
    public void tick(double walkDistance,double speed,boolean ground,boolean sprint,double verticalAcceleration,double turn) {
        oldWeight=weight;oldRun=run;oldDistance=distance;distance=walkDistance;
        oldAir=air;
        weight+=(ground?Math.min(1,speed/.12)-weight:-weight)*.3;
        run+=((sprint&&ground?1:0)-run)*.22;
        air+=((ground?0:1)-air)*.35;
        inertia.tick(clamp(-turn*.002,-.025,.025),clamp(-verticalAcceleration*.22,-.15,.15));
    }
    public double run(double partial) {return oldRun+(run-oldRun)*partial;}
    public BottleMotion.Pose apply(BottleMotion.Pose pose,double partial,double strength) {
        double amount=(oldWeight+(weight-oldWeight)*partial)*strength;
        double phase=(oldDistance+(distance-oldDistance)*partial)*Math.PI;
        double running=run(partial),airborne=oldAir+(air-oldAir)*partial,s=Math.sin(phase),c=Math.cos(phase*2);
        double footfall=Math.pow(Math.max(0,-c),3)-.2122;
        return pose.offset(s*(.011+.015*running)*amount+inertia.x(partial)*.14,
            (c*(.008+.015*running)-.012*running*footfall)*amount+inertia.z(partial)*.78-airborne*.018,
            (s*(.003+.006*running)+.010*running*footfall)*amount,
            (c*(.55+1.7*running)+.9*running*footfall)*amount+inertia.z(partial)*9,
            s*(1.35+2.75*running)*amount+inertia.x(partial)*9);
    }
    public BottleMotion.Camera camera(double partial,BottleMotion.Camera base,double strength) {
        double amount=(oldWeight+(weight-oldWeight)*partial)*strength,phase=(oldDistance+(distance-oldDistance)*partial)*Math.PI;
        double running=run(partial),pitch=Math.cos(phase*2)*(.08+.22*running)*amount+inertia.z(partial)*2.8*strength;
        double roll=Math.sin(phase)*(.05+.15*running)*amount;
        return new BottleMotion.Camera(base.pitch()+pitch,base.yaw(),base.roll()+roll);
    }
    public void reset(double walkDistance) {weight=oldWeight=run=oldRun=air=oldAir=0;distance=oldDistance=walkDistance;inertia.reset();}
    private static double clamp(double x,double lo,double hi) {return Math.max(lo,Math.min(hi,x));}
}
