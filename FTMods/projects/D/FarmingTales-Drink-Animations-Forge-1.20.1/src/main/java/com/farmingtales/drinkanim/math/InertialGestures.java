package com.farmingtales.drinkanim.math;

/** Hand-driven inspection motion with a causal, damped wrist response. Authored time is in ticks. */
public final class InertialGestures {
    private static final double STEP=.025;
    private static final double[] CAN_PITCH=response(0,0),JUICE_PITCH=response(1,2),JUICE_ROLL=response(1,0),JAM_PITCH=response(2,0);

    private static double window(double t,double start,double in,double out,double end) {
        return BottleMotion.smooth((t-start)/(in-start))*(1-BottleMotion.smooth((t-out)/(end-out)));
    }
    static double canDrive(double t) {
        // Three fast elbow strokes; only the entrance and final stroke taper their amplitude.
        return .155*Math.sin((t-30)*Math.PI/4)*window(t,27,32,51,58);
    }
    private static double jamDrive(double t) {
        // Continuous elbow cycles, as in can inspection: do not stop at neutral between strokes.
        return -.09*Math.cos((t-19)*Math.PI/3.5)*window(t,15,18,20,24.5)
            -.105*Math.cos((t-28)*Math.PI/2.1)*window(t,25.5,28,36.5,40);
    }
    static double swirl(double t,int axis) {
        double phase=(t-27)*Math.PI/9,amount=window(t,23,31,68,81);
        return amount*(axis==0?.072*Math.cos(phase):axis==1?.007*Math.sin(2*phase):.048*Math.sin(phase));
    }
    private static double acceleration(int family,double t,int axis) {
        double h=.04;
        return family==2?(jamDrive(t+h)-2*jamDrive(t)+jamDrive(t-h))/(h*h)
            :family==0?(canDrive(t+h)-2*canDrive(t)+canDrive(t-h))/(h*h)
            :(swirl(t+h,axis)-2*swirl(t,axis)+swirl(t-h,axis))/(h*h);
    }
    private static double[] response(int family,int axis) {
        double[] angles=new double[4161];double angle=0,velocity=0;
        for(int i=1;i<angles.length;i++) {
            double target=(family==0||family==2?-180:axis==0?-1350:1350)*acceleration(family,i*STEP,axis);
            double stiffness=family==2?.95:family==0?.60:.30,damping=family==2?.95:family==0?.70:.55;
            velocity+=((target-angle)*stiffness-velocity*damping)*STEP;
            angle+=velocity*STEP;angles[i]=angle;
        }
        return angles;
    }
    private static double angle(double[] values,double t) {
        double index=Math.max(0,Math.min(104,t))/STEP;int a=(int)index,b=Math.min(a+1,values.length-1);
        return (values[a]+(values[b]-values[a])*(index-a))*(1-BottleMotion.smooth((t-88)/12));
    }
    public static BottleMotion.Pose apply(ContainerAnimations.Family family,double t,BottleMotion.Pose p) {
        if(family==ContainerAnimations.Family.CAN) {
            double pitch=angle(CAN_PITCH,t);
            return p.offset(0,canDrive(t),.0012*pitch,pitch,0);
        }
        if(family==ContainerAnimations.Family.JUICE)
            return p.offset(swirl(t,0),swirl(t,1),swirl(t,2),angle(JUICE_PITCH,t),angle(JUICE_ROLL,t));
        return p;
    }
    /** One firm stroke followed by three short terminal shakes, with damped wrist response. */
    public static BottleMotion.Pose jamShake(double t,BottleMotion.Pose p) {
        double finish=1-BottleMotion.smooth((t-38)/2),pitch=9*Math.tanh(angle(JAM_PITCH,t)/9)*finish;
        double drive=jamDrive(t);
        return p.offset(0,drive,-.26*drive+.0012*pitch,pitch*.8,0);
    }
    public static double jamCamera(double t) {
        return (-.45*jamDrive(t-1.2)+.010*angle(JAM_PITCH,t-1.2))*(1-BottleMotion.smooth((t-38)/2));
    }
    public static BottleMotion.Camera camera(ContainerAnimations.Family family,double t) {
        if(family==ContainerAnimations.Family.CAN)
            return new BottleMotion.Camera(-1.7*canDrive(t-1.3)+.032*angle(CAN_PITCH,t-1.2),0,0);
        if(family==ContainerAnimations.Family.JUICE)
            return new BottleMotion.Camera(-5*swirl(t-1.4,2),-6*swirl(t-1.4,0),.065*angle(JUICE_ROLL,t-1.6));
        return new BottleMotion.Camera(0,0,0);
    }
    public static double liquid(ContainerAnimations.Family family,double t,int axis) {
        if(family==ContainerAnimations.Family.JUICE)return -6*acceleration(1,t,axis);
        if(family==ContainerAnimations.Family.CAN) {
            // Vertical motion drives the wrist, whose angular motion excites the liquid plane.
            double angular=angle(CAN_PITCH,t)-2*angle(CAN_PITCH,t-1)+angle(CAN_PITCH,t-2);
            return axis==0?.0015*angular:.008*angular;
        }
        return 0;
    }
    private InertialGestures() {}
}
