package com.farmingtales.drinkanim.math;

/** The whole forearm lifts and settles; there is no authored wrist twisting. */
public final class ArmMotion {
    public record Pose(double lift,double sweep,double pronation) {
        Pose mix(Pose b,double t) {return new Pose(lift+(b.lift-lift)*t,sweep+(b.sweep-sweep)*t,pronation+(b.pronation-pronation)*t);}
    }
    public static final Pose REST=new Pose(0,0,0);
    private static final double[] INSPECT_TIMES={0,6,16,22,34,50,56,66,74,80,84};
    private static final Pose[] INSPECT={REST,new Pose(-2,-1,0),new Pose(22,1,0),new Pose(24,2,0),
        new Pose(25,3,0),new Pose(26,2,0),new Pose(25,2,0),new Pose(22,1,0),new Pose(10,0,0),new Pose(-.5,0,0),REST};
    private static final Pose[] INSPECT_HELPER={REST,new Pose(0,-1,0),new Pose(9,-4,0),new Pose(12,-5,0),
        new Pose(13,-6,0),new Pose(15,-4,0),new Pose(14,-4,0),new Pose(8,-2,0),REST,REST,REST};
    private static final double[] OPEN_TIMES={0,7,14,22,26,28,31,36,43,48};
    private static final Pose[] OPEN={REST,new Pose(2,6,0),new Pose(6,10,0),new Pose(8,10,0),
        new Pose(10,11,0),new Pose(-3,6,0),new Pose(2,8,0),new Pose(2,8,0),new Pose(16,1,0),new Pose(24,-3,0)};
    private static final double[] DRAW_TIMES={0,2.4,4.8,8.4,11.6,14.4,18.4,19};
    private static final Pose[] DRAW={new Pose(-12,-18,0),new Pose(-16,-20,0),new Pose(12,3,0),
        new Pose(-5,-2,0),new Pose(2,.5,0),new Pose(-.5,-.2,0),REST,REST};
    private static final double[] STOW_TIMES={0,2,4.4,7.2,10.4,13.2,16.8,20};
    private static final Pose[] STOW={REST,new Pose(-1,0,0),new Pose(7,1,0),new Pose(18,3,0),
        new Pose(12,3,0),new Pose(-16,-20,0),new Pose(-12,-18,0),new Pose(-12,-18,0)};
    public static Pose sample(BottleMotion.Action action,double tick,int useTicks,boolean helper) {
        if(action==BottleMotion.Action.INSPECT) {
            Pose p=track(BottleMotion.inspectionMotionTime(tick),INSPECT_TIMES,helper?INSPECT_HELPER:INSPECT);
            return helper?p:new Pose(p.lift+2.2*BottleMotion.inspectionPush(tick),p.sweep-2.5*BottleMotion.inspectionRegrip(tick),0);
        }
        if(action==BottleMotion.Action.DRAW)return helper?REST:track(tick,DRAW_TIMES,DRAW);
        if(action==BottleMotion.Action.STOW)return helper?REST:track(tick,STOW_TIMES,STOW);
        if(helper) {
            double grip=BottleMotion.ease((tick-7)/7),pull=BottleMotion.ease((tick-26)/3),exit=BottleMotion.ease((tick-33)/8);
            return new Pose(-5*grip+13*pull-20*exit+30*BottleMotion.openingHand(tick),-3*grip-12*pull-6*exit,0);
        }
        if(action==BottleMotion.Action.IDLE)return REST;
        if(tick<=BottleMotion.RAISE_TICKS)return track(tick,OPEN_TIMES,OPEN);
        double end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(useTicks);
        if(tick<=end)return OPEN[OPEN.length-1].mix(new Pose(31,-5,0),BottleMotion.ease((tick-BottleMotion.RAISE_TICKS)/BottleMotion.sipTicks(useTicks)));
        return track(tick-end,new double[]{0,8,15,24},new Pose[]{new Pose(31,-5,0),new Pose(7,5,0),new Pose(-4,2,0),REST});
    }
    private static Pose track(double tick,double[] times,Pose[] poses) {
        if(tick<=times[0])return poses[0];
        for(int i=1;i<times.length;i++)if(tick<times[i])return poses[i-1].mix(poses[i],BottleMotion.ease((tick-times[i-1])/(times[i]-times[i-1])));
        return poses[poses.length-1];
    }
}
