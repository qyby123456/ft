package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleRig.Frame;
import com.farmingtales.drinkanim.math.BottleRig.Transform;

/** Present the broad chocolate face after lifting it above the rim; return by the same clear route. */
final class ChocolateInspection {
    static Pose cup(double t) {return PopsicleMotion.inspect(t);}
    static double assistance(double t) {return PopsicleMotion.inspectAssistance(t);}
    static double lock(double t) {return PopsicleMotion.inspectLock(t);}
    private static double smooth(double t) {return BottleMotion.smooth(t);}
    private static float rad(double x) {return (float)Math.toRadians(x);}
    private static double lift(double t) {return smooth((t-18)/8)*(1-smooth((t-62)/8));}
    private static double show(double t) {return smooth((t-26)/12)*(1-smooth((t-50)/12));}
    private static Transform prop(DrinkProfile p,double t,int side,Frame rig) {
        Transform rest=ServingAnimations.seated(p,rig);
        if(side<0) {
            // The opposite edge has little overhang. Slide the slab along the
            // rim toward the free hand before pinching that corner.
            float slide=(float)(smooth((t-8)/10)*(1-smooth((t-72)/12)));
            rest=new Transform(new Vector3f(rest.position()).add(.12f*slide,.006f*slide,0),rest.rotation());
        }
        float lifted=(float)lift(t),shown=(float)show(t);
        double look=smooth((t-38)/7)*(1-smooth((t-45)/7));
        Quaternionf facing=new Quaternionf().rotationZ(rad(side*48)).rotateY(rad(side*(155+8*look))).rotateX(rad(65));
        Quaternionf rotation=new Quaternionf(rest.rotation()).slerp(facing,shown);
        // Move the gripping corner rather than the slab centre, preserving the pinch.
        Vector3f grip=gripPoint(p,side);
        Vector3f at=rest.rotation().transform(new Vector3f(grip)).add(rest.position()).add(0,.13f*lifted,.06f*lifted);
        Vector3f target=new Vector3f(-side*.30f,-.18f,-.64f);
        at.lerp(target,shown).add(-side*.12f*4*shown*(1-shown),.09f*4*shown*(1-shown),0);
        return new Transform(at.sub(rotation.transform(grip)),rotation);
    }
    static Vector3f gripPoint(DrinkProfile p,int side) {
        if(side>0)return ServingAnimations.gripPoint(p);
        float margin=.25f*BottleRig.ARM_SCALE/p.renderScale()+.02f;
        return new Vector3f(-4.543277f-.55f*margin,-1-.83516465f*margin,.2961f).mul(p.renderScale()/16);
    }
    private static Transform helper(DrinkProfile p,Transform prop,int side,double show) {
        Quaternionf base=BottleRig.armRotation(new ArmMotion.Pose(24+11*show,-5+4*show,0),-side,false);
        Vector3f normal=prop.rotation().transform(new Vector3f(-side*.45f,.89302856f,0));
        Quaternionf face=new Quaternionf().rotationTo(base.transform(new Vector3f(0,1,0)),normal).mul(base);
        Vector3f corner=side>0?new Vector3f(6.067739f,-1,2.395227f):new Vector3f(-4.543277f,-1,.296101f);
        Vector3f grip=prop.rotation().transform(corner.mul(p.renderScale()/16)).add(prop.position());
        // The old anchor cleared the inflated sleeve, leaving a visible gap to
        // bare skin. Seat the retained corner 0.65 skin pixels inside the palm.
        float inset=.65f*BottleRig.ARM_SCALE/16;
        grip.fma(inset,normal);
        // Keep the pinched corner toward the inner palm edge, leaving the
        // outside of the sleeve clear of the glass during pickup and return.
        grip.add(face.transform(new Vector3f(-side*.025f,0,0)));
        return new Transform(grip,face);
    }
    static Frame rig(DrinkProfile p,double t,int side,boolean slim,Frame rig) {
        Transform prop=prop(p,t,side,rig);
        Transform contact=helper(p,prop,side,show(t));
        Vector3f hidden=new Vector3f(-side*.72f,-.95f,-.90f);
        double reach=BottleMotion.ease((t-8)/10),retreat=BottleMotion.ease((t-72)/12);
        Vector3f palm=reach(hidden,contact,side,reach);
        if(retreat>0)palm=reach(hidden,helper(p,prop(p,72,side,rig),side,0),side,1-retreat);
        Quaternionf rotation=BottleRig.armRotation(new ArmMotion.Pose(25,0,0),-side,false)
            .slerp(contact.rotation(),(float)(reach*(1-retreat)));
        return new Frame(rig.bottle(),rig.hand(),new Transform(palm,rotation),prop,assistance(t));
    }
    private static Vector3f reach(Vector3f hidden,Transform contact,int side,double u) {
        float v=(float)u,w=1-v;
        Vector3f target=contact.position();
        Vector3f approach=contact.rotation().transform(new Vector3f(0,-.24f,0)).add(target);
        return new Vector3f(hidden).mul(w*w*w)
            .fma(3*w*w*v,new Vector3f(hidden).add(-side*.08f,.22f,.15f))
            .fma(3*w*v*v,approach).fma(v*v*v,target);
    }
    private ChocolateInspection() {}
}
