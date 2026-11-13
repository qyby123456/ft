package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleRig.Frame;
import com.farmingtales.drinkanim.math.BottleRig.Transform;

/** Long Island performance: lead, follow, then settle, with the existing contact/event clock.
 * Study notes: blockbench/03-参考素材/Minecraft-Swords-Pack/动作观察.md.
 * These are new cup-sized gestures; no sword/bow transforms or assets are shipped. */
final class PopsicleMotion {
    static final int INSPECT_TICKS=104;
    // Authored cup path: offer within reach, support while eating, then lift to sip.
    // Offer the upright glass within reach; the loose ice supplies its own left lean.
    private static final Track DRINK=new Track(new double[][]{
        {0,.48,-.69,-1.08,-3,155,0}, {1.5,.46,-.70,-1.06,-4,155,1},
        {4,.08,-.71,-.92,-4,155,4}, {8,.08,-.77,-.92,-3,155,4},
        {11,.40,-.76,-1.04,-2,156,3}, {14,.38,-.735,-1.055,-4,159,3},
        {17,.415,-.765,-1.065,-6,161,-3}, {21,.39,-.74,-1.045,-3,160,-1},
        {25,.415,-.77,-1.06,-5,156,2}, {30,.39,-.73,-1.04,-2,157,1},
        {34,.375,-.715,-1.025,-1,158,0}, {37,.36,-.72,-1.04,-4,157,-2},
        {40,.32,-.70,-1.07,1,158,-1}, {44,.18,-.57,-.995,29,162,1},
        {48,.08,-.48,-.89,48,163,2}
    });
    // Unequal withdrawal/return timing avoids repeating the same mechanical nod.
    private static final Track RECOVERY=new Track(new double[][]{
        {14,0}, {14.8,.18}, {16.5,1.18}, {19.1,.43}, {22,0},
        {23,.23}, {25,.84}, {27.7,.20}, {30,0}
    });
    private static final Track WRIST=new Track(new double[][]{
        {8,0,0,0}, {11.4,-1.6,-.7,.6}, {14,0,0,0},
        {16.3,1.2,.7,-.5}, {19,-.35,.15,.15}, {22,0,0,0},
        {24.8,-.9,-.6,.45}, {27.6,.3,.15,-.15}, {30,0,0,0}
    });
    // Follow the rim, turn to the ice, then return in a different arc. Holds keep
    // drifting slightly; the final overshoot is deliberately smaller than the lift.
    private static final Track INSPECT=new Track(new double[][]{
        {0,.48,-.69,-1.08,-3,155,0}, {6,.46,-.70,-1.06,-4,155,1},
        {14,.08,-.71,-.92,-4,155,4}, {20,.08,-.73,-.92,-3,155,4},
        {26,.08,-.77,-.92,-3,155,4}, {38,.39,-.72,-1.05,2,162,3},
        {50,.39,-.72,-1.05,1,160,3}, {62,.08,-.77,-.92,-3,155,4},
        {70,.08,-.71,-.92,-4,155,4}, {78,.08,-.71,-.92,-4,155,4},
        {84,.08,-.71,-.92,-4,155,4}, {92,.30,-.555,-1.115,12,170,3},
        {96,.42,-.645,-1.10,1,169,-2}, {99,.49,-.698,-1.073,-4,154,.6},
        {104,.48,-.69,-1.08,-3,155,0}
    });
    private static final Track PEEK_CLOCK=new Track(new double[][]{
        {18,4}, {26,8}, {38,14}, {50,14}, {62,8}, {70,4}
    });

    static Pose drink(double t) {
        double[] d=DRINK.sample(t);
        return new Pose(d[0],d[1],d[2],d[3],d[4],d[5],.8);
    }
    static Quaternionf upright(int side) {
        return new Quaternionf().rotationY(rad(side*12)).rotateZ(rad(-side*6)).rotateX(rad(-178));
    }
    static Transform looseSeat(DrinkProfile p,Frame rig,int side,double amount) {
        Transform rest=ServingAnimations.seated(p,rig);
        Vector3f pivot=ServingAnimations.tip(p).mul(.65f);
        Quaternionf lean=new Quaternionf().rotationZ(rad(side*14*amount));
        Quaternionf rotation=new Quaternionf(lean).mul(rest.rotation());
        Vector3f fixed=rest.rotation().transform(new Vector3f(pivot)).add(rest.position());
        fixed.sub(rotation.transform(pivot));
        fixed.add(rig.bottle().rotation().transform((side>0?new Vector3f(0,0,.02f):new Vector3f(.02f,0,-.008f)).mul((float)amount)));
        fixed.add(rig.bottle().rotation().transform(new Vector3f(.035f,.02f,.047f).mul((float)(amount*(1-amount)))));
        return new Transform(fixed,rotation);
    }
    static Transform extract(DrinkProfile p,double t,int side,Transform rest,Vector3f mouthGrip,Quaternionf mouthRotation) {
        Vector3f lift=new Quaternionf().rotationZ(rad(-side*14)).mul(rest.rotation()).transform(side>0?new Vector3f(0,.18f,.04f):new Vector3f(-.045f,.18f,.005f));
        Vector3f lateral=new Vector3f(-side*.18f,0,.28f);
        
        if(t<=8) {
            // Rise through the opening with a small correction away from the lemon.
            // The entire frozen block clears the rim before the lateral withdrawal.
            Vector3f pos=new Vector3f(rest.position()).fma((float)BottleMotion.smooth((t-4)/2.8),lift)
                .fma((float)exitCurve((t-6.4)/1.6),lateral);
            return new Transform(pos,new Quaternionf(rest.rotation()));
        }
        // Turn around the frozen portion's centre, so the hand follows an arc
        // instead of dragging the complete ice through a straight-line flip.
        float blend=(float)BottleMotion.smooth((t-8)/5);
        Quaternionf start=new Quaternionf().rotationZ(rad(side*14)).mul(BottleRig.rotation(drink(8),side));
        // Delay the last part of the flip until the ice is in front of the lemon.
        Quaternionf turn=new Quaternionf().rotationZ(rad(side*140*blend)).mul(start);
        turn.slerp(mouthRotation,(float)BottleMotion.smooth((t-11.3333333333)/1.6666666667));
        Vector3f center=ServingAnimations.tip(p).mul(.5f);
        // Once clear, the ice belongs to the free hand; returning the cup to
        // the right must not drag the extraction pivot with it.
        Transform anchor=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.DRINK,8,32,true),BottleMotion.Action.DRINK,8,32,side,false,p).cork();
        Transform preceding=BottleRig.solve(DrinkMotion.sample(p,BottleMotion.Action.DRINK,7.99,32,true),BottleMotion.Action.DRINK,7.99,32,side,false,p).cork();
        Vector3f direction=new Vector3f(anchor.position()).sub(preceding.position()).div(.01f);
        Vector3f a=anchor.rotation().transform(new Vector3f(center)).add(anchor.position());
        Vector3f b=mouthRotation.transform(new Vector3f(center)).add(mouthGrip);
        float travel=(float)BottleMotion.smooth((t-8)/6);
        Vector3f moving=curve(a,b,direction,new Vector3f(),8,14,t).add(-side*.12f*4*travel*(1-travel),0,0);
        return new Transform(moving.sub(turn.transform(center)),turn);
    }
    private static double exitCurve(double u) {
        u=Math.max(0,Math.min(1,u));
        return u*u*(2-u);
    }
    static double inspectAssistance(double t) {
        return BottleMotion.smooth((t-8)/10)*(1-BottleMotion.smooth((t-72)/12));
    }
    static double inspectLock(double t) {
        return BottleMotion.smooth((t-12)/6)*(1-BottleMotion.smooth((t-72)/6));
    }
    private static Transform inspectProp(DrinkProfile p,double t,int side,Frame rig) {
        double look=pulse(t,34,43,56),phase=PEEK_CLOCK.sample(t)[0];
        Quaternionf rotation=upright(side).rotateY(rad(side*8*look));
        Vector3f grip=new Vector3f((float)(-side*(.19+.012*look)),(float)(-.24+.008*look),-.72f);
        return extract(p,phase,side,looseSeat(p,rig,side,BottleMotion.smooth((t-8)/10)*(1-BottleMotion.smooth((t-72)/12))),grip,rotation);
    }
    static Frame inspectRig(DrinkProfile p,double t,int side,boolean slim,Frame rig) {
        Transform prop=inspectProp(p,t,side,rig);
        Transform contact=helper(p,prop,side,slim,PEEK_CLOCK.sample(t)[0]);
        Vector3f hidden=new Vector3f(-side*.72f,-.95f,-.90f);
        double reach=BottleMotion.ease((t-8)/10),retreat=BottleMotion.ease((t-72)/12);
        Vector3f palm=reach(hidden,contact.position(),side,reach);
        Quaternionf rotation=new Quaternionf(contact.rotation());
        if(retreat>0) {
            palm=reach(hidden,helper(p,inspectProp(p,72,side,rig),side,slim,4).position(),side,1-retreat);
        }
        return new Frame(rig.bottle(),rig.hand(),new Transform(palm,rotation),prop,inspectAssistance(t));
    }
    private static Vector3f reach(Vector3f hidden,Vector3f target,int side,double u) {
        float v=(float)u,w=1-v;
        return new Vector3f(hidden).mul(w*w*w)
            .fma(3*w*w*v,new Vector3f(hidden).add(-side*.08f,.22f,.15f))
            .fma(3*w*v*v,new Vector3f(target).add(-side*.28f,.22f,.65f)).fma(v*v*v,target);
    }
    // Restore the original upright palm grip after extraction. During the flip,
    // the forearm stays on its own side and passes around the wooden endpoint.
    static Transform helper(DrinkProfile p,Transform prop,int side,boolean slim,double t) {
        Quaternionf original=BottleRig.armRotation(new ArmMotion.Pose(35-3.5*recovery(t),-1-1.5*recovery(t),0),-side,false);
        Quaternionf ready=BottleRig.armRotation(new ArmMotion.Pose(25,0,-45),-side,false)
            .slerp(BottleRig.armRotation(new ArmMotion.Pose(35,-20,30),-side,false),(float)BottleMotion.smooth((t-4)/4));
        Quaternionf held=new Quaternionf(prop.rotation()).mul(upright(side).conjugate()).mul(original);
        Quaternionf middle=BottleRig.armRotation(new ArmMotion.Pose(30,-15,15),-side,false);
        float blend=(float)BottleMotion.smooth((t-8)/5);
        Quaternionf arm=new Quaternionf(ready).slerp(middle,blend)
            .slerp(new Quaternionf(middle).slerp(held,blend),blend);
        Vector3f wood=new Vector3f(0,1.367f,.5664f).mul(p.renderScale()/16);
        Vector3f grip=prop.rotation().transform(new Vector3f(wood)).add(prop.position());
        Vector3f finalContact=new Quaternionf(original).conjugate().mul(upright(side)).transform(new Vector3f(wood).sub(0,.03695518f,.01530734f));
        Vector3f initial=new Vector3f(side*(slim?1.45f:1.95f),-.4f,-1.95f).mul(BottleRig.ARM_SCALE/16);
        Vector3f towardIce=new Quaternionf(arm).conjugate().mul(prop.rotation()).transform(new Vector3f(0,-.9238795f,-.3826834f));
        Vector3f middleContact=new Vector3f((float)Math.tanh(towardIce.x*4)*(slim?1.25f:1.75f),-.1f,(float)Math.tanh(towardIce.z*4)*1.75f).mul(BottleRig.ARM_SCALE/16);
        Vector3f palm=initial.lerp(middleContact,blend).lerp(new Vector3f(middleContact).lerp(finalContact,blend),blend);
        return new Transform(grip.sub(arm.transform(palm)),arm);
    }
    private static Vector3f curve(Vector3f a,Vector3f b,Vector3f va,Vector3f vb,double start,double stop,double t) {
        float u=(float)Math.max(0,Math.min(1,(t-start)/(stop-start))),u2=u*u,u3=u2*u,h=(float)(stop-start);
        return new Vector3f(a).mul(2*u3-3*u2+1).fma((u3-2*u2+u)*h,va)
            .fma(-2*u3+3*u2,b).fma((u3-u2)*h,vb);
    }

    static double recovery(double t) {return RECOVERY.sample(t)[0];}
    static Quaternionf wrist(Quaternionf rotation,double t,int side) {
        double[] d=WRIST.sample(t);
        return rotation.rotateXYZ(rad(d[0]),rad(side*d[1]),rad(side*d[2]));
    }
    static Vector3f mouthOffset(double t,int side) {
        double a=pulse(t,14,16.8,22),b=pulse(t,22,25.2,30);
        // Small curved accents on top of the visible mouth-to-view recovery.
        return new Vector3f((float)(side*(-.005*a-.004*b)),(float)(.003*a-.002*b),
            (float)(-.006*a+.003*b));
    }
    static Pose inspect(double t) {
        double[] d=INSPECT.sample(t);
        return new Pose(d[0],d[1],d[2],d[3],d[4],d[5],.8);
    }
    static Camera inspectCamera(double t) {
        Pose lead=inspect(Math.max(0,t-1.5));
        double gate=BottleMotion.smooth(t/6)*(1-BottleMotion.smooth((t-(INSPECT_TICKS-8))/8));
        return new Camera((lead.pitch()+3)*.035*gate,(lead.yaw()-155)*.012*gate,lead.roll()*.018*gate);
    }
    static Camera carryCamera(double t) {
        double[] d=DRINK.sample(Math.max(0,t-.9));
        double gate=1-BottleMotion.smooth((t-44)/4);
        return new Camera(((d[1]+.69)*.8+(d[3]+3)*.006)*gate,(d[0]-.48)*.15*gate,d[5]*.008*gate);
    }
    private static double pulse(double t,double start,double crest,double end) {
        return BottleMotion.smooth((t-start)/(crest-start))*(1-BottleMotion.smooth((t-crest)/(end-crest)));
    }
    private static float rad(double degrees) {return (float)Math.toRadians(degrees);}

    /** Neighboring keys share velocity, as in the reference Catmull-Rom tracks.
     * Monotone tangents bound each channel to its keys so a cup cannot overshoot into a hand. */
    private static final class Track {
        private final double[][] keys,velocity;
        Track(double[][] keys) {
            this.keys=keys;velocity=new double[keys.length][keys[0].length];
            for(int i=1;i<keys.length-1;i++)for(int c=1;c<keys[0].length;c++) {
                double h0=keys[i][0]-keys[i-1][0],h1=keys[i+1][0]-keys[i][0];
                double a=(keys[i][c]-keys[i-1][c])/h0,b=(keys[i+1][c]-keys[i][c])/h1;
                if(a*b>0)velocity[i][c]=3*(h0+h1)/((2*h1+h0)/a+(h1+2*h0)/b);
            }
        }
        double[] sample(double t) {
            int b=1;while(b<keys.length&&t>=keys[b][0])b++;
            int a=Math.max(0,b-1);b=Math.min(b,keys.length-1);
            double h=keys[b][0]-keys[a][0],u=h==0?0:Math.max(0,Math.min(1,(t-keys[a][0])/h));
            double u2=u*u,u3=u2*u;double[] result=new double[keys[0].length-1];
            for(int c=1;c<keys[0].length;c++)result[c-1]=(2*u3-3*u2+1)*keys[a][c]
                +(u3-2*u2+u)*h*velocity[a][c]+(-2*u3+3*u2)*keys[b][c]
                +(u3-u2)*h*velocity[b][c];
            return result;
        }
    }
    private PopsicleMotion() {}
}
