package com.farmingtales.drinkanim.math;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleRig.Transform;
import com.farmingtales.drinkanim.math.BottleRig.Frame;

/** Keep the original pineapple slice in the free palm through a single pickup and bite. */
final class PineappleMotion {
    private static double smooth(double t) {return BottleMotion.smooth(t);}
    private static Vector3f curve(Vector3f a,Vector3f b,Vector3f c,Vector3f d,double t) {
        float u=(float)Math.max(0,Math.min(1,t)),v=1-u;
        return new Vector3f(a).mul(v*v*v).fma(3*v*v*u,b).fma(3*v*u*u,c).fma(u*u*u,d);
    }
    static Transform held(DrinkProfile p,double t,int side,Frame rig) {
        if(t<=ServingAnimations.GRASP_TICK)return ServingAnimations.seated(p,rig);
        // Freeze the seat at grasp; the food is now owned by the free hand.
        var pose=DrinkMotion.sample(p,Action.DRINK,4,32,true);
        var rotation=BottleRig.rotation(pose,side);
        var bottle=new Transform(new Vector3f((float)(side*pose.x()*.75),
            (float)(pose.y()*.75+.30*p.renderScale()),(float)(pose.z()*.75)),rotation);
        var rest=ServingAnimations.seated(p,new Frame(bottle,rig.hand(),rig.helper(),rig.cork(),0));
        double approach=smooth((t-10)/4);
        Vector3f direction=new Vector3f(side*.18f,.55f,.82f).normalize();
        Quaternionf mouth=new Quaternionf().rotationTo(ServingAnimations.tip(p).normalize(),direction);
        Vector3f lips=new Vector3f(-side*.09f,(float)(-.025-.060*approach),(float)(-.30+.04*approach));
        Vector3f target=new Vector3f(lips).sub(mouth.transform(ServingAnimations.tip(p)));
        Vector3f clear=new Vector3f(rest.position()).add(-side*.12f,.18f,.12f);
        Vector3f tangent=new Vector3f(-side*.014f,.026f,.022f);
        Vector3f position=t<=8?curve(rest.position(),new Vector3f(rest.position()),new Vector3f(clear).fma(-4f/3,tangent),clear,(t-4)/4)
            :curve(clear,new Vector3f(clear).fma(6f/3,tangent),target,target,(t-8)/6);
        return new Transform(position,new Quaternionf(rest.rotation()).slerp(mouth,(float)smooth((t-4)/10)));
    }
    static Transform helper(DrinkProfile p,Transform prop,int side,double t) {
        // The top of the palm touches the retained lower edge. Its forearm
        // extends down and away from the camera, keeping the slice unobscured.
        double lift=smooth((t-4)/10);
        Vector3f up=new Vector3f((float)(side*(.35-.17*lift)),(float)(.86+.10*lift),(float)(-.55+.67*lift)).normalize();
        Quaternionf arm=new Quaternionf().rotationTo(new Vector3f(0,1,0),up);
        return new Transform(new Vector3f(prop.position()),arm);
    }
    private PineappleMotion() {}
}
