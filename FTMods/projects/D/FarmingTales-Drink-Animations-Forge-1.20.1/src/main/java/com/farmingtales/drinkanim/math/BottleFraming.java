package com.farmingtales.drinkanim.math;

import org.joml.Vector3f;
import org.joml.Quaternionf;

/** Keep the complete moving assembly outside the near plane, including both sleeves. */
public final class BottleFraming {
    public static final float MIN_DEPTH=.28f;
    public static final float MIN_ARM_DEPTH=.06f;
    public static final float SHOULDER_FRUSTUM_TAN=.70f;
    public static Vector3f offset(BottleRig.Frame rig,BottleMotion.Action action,double age,int duration,boolean replacement) {
        return offset(rig,action,age,duration,replacement,DrinkCatalog.ORANGE);
    }
    public static Vector3f offset(BottleRig.Frame rig,BottleMotion.Action action,double age,int duration,boolean replacement,DrinkProfile profile) {
        return offset(rig,action,age,duration,replacement,profile,DrinkMotion.camera(profile,action,age,duration).rotation());
    }
    public static Vector3f offset(BottleRig.Frame rig,BottleMotion.Action action,double age,int duration,boolean replacement,DrinkProfile profile,Quaternionf camera) {
        Vector3f offset=new Vector3f(0,BottleRig.VIEW_Y,BottleRig.VIEW_Z);
        if(action==BottleMotion.Action.DRINK) {
            double sipEnd=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);
            float raise=(float)(BottleMotion.ease((age-32)/16)*(1-BottleMotion.ease((age-sipEnd)/12)));
            float opening=(float)(BottleMotion.ease(age/12)*(1-BottleMotion.ease((age-36)/12)));
            float counterpull=(float)(BottleMotion.ease((age-18)/9)*(1-BottleMotion.ease((age-32)/12)));
            float openingDrop=profile==DrinkCatalog.ORANGE?.20f:.035f;
            float pullDrop=profile==DrinkCatalog.ORANGE?.29f:profile.wine()?.035f:0;
            offset.add(.025f*raise,-.045f*raise-openingDrop*opening-pullDrop*counterpull,-.10f*raise);
        }
        if(action==BottleMotion.Action.INSPECT||action==BottleMotion.Action.DRINK) {
            // Helper hands that hold their own prop or only steady the food (the bamboo spoon, the
            // cake's support palm) must not reframe it; only the carrying arm owns retreat and drop.
            boolean independentSpoon=BambooEggAnimations.owns(profile)||StickyRiceCakeAnimations.owns(profile);
            boolean continuous=ServingAnimations.popsicle(profile)||ServingAnimations.chocolate(profile)&&action==BottleMotion.Action.INSPECT;
            float radius=ServingAnimations.chocolate(profile)&&action==BottleMotion.Action.INSPECT?.20f:continuous?.12f:0;
            float depth=MIN_DEPTH;
            float retreat=upper(bottleFront(rig,offset,camera,action,age,duration,replacement,profile)+depth,
                armFront(rig.hand(),offset,camera,BottleRig.ARM_SCALE)+MIN_ARM_DEPTH,radius);
            if(rig.assistance()>0&&!independentSpoon)retreat=upper(retreat,armFront(rig.helper(),offset,camera,BottleRig.ARM_SCALE)+MIN_ARM_DEPTH,radius);
            // Smooth the active clearance constraint without ever reducing its safety margin.
            retreat=upper(0,retreat,radius);
            if(retreat>0)offset.z-=retreat/camera.transform(new Vector3f(0,0,1)).z;
            float shoulder=shoulderExposure(rig.hand(),offset,camera);
            if(rig.assistance()>0&&!independentSpoon)shoulder=upper(shoulder,shoulderExposure(rig.helper(),offset,camera),radius);
            float drop=upper(0,shoulder+.025f,radius);
            if(drop>0) {
                Vector3f down=camera.transform(new Vector3f(0,1,0));
                offset.y-=drop/(down.y-SHOULDER_FRUSTUM_TAN*down.z);
            }
        }
        return offset;
    }
    private static float upper(float a,float b,float radius) {
        if(radius==0)return Math.max(a,b);
        float blend=Math.max(0,radius-Math.abs(a-b));
        return Math.max(a,b)+blend*blend/(4*radius);
    }
    public static float front(BottleRig.Frame rig,Vector3f offset,Quaternionf camera,BottleMotion.Action action,double age,int duration,boolean replacement) {
        float front=bottleFront(rig,offset,camera,action,age,duration,replacement);
        front=Math.max(front,armFront(rig.hand(),offset,camera,BottleRig.ARM_SCALE));
        if(rig.assistance()>0)front=Math.max(front,armFront(rig.helper(),offset,camera,BottleRig.ARM_SCALE));
        return front;
    }
    public static float shoulderExposure(BottleRig.Transform arm,Vector3f offset,Quaternionf camera) {
        float unit=BottleRig.ARM_SCALE/16,result=-Float.MAX_VALUE;
        for(float x:new float[]{-2.25f,2.25f})for(float z:new float[]{-2.25f,2.25f}) {
            Vector3f corner=arm.rotation().transform(new Vector3f(x*unit,-12.25f*unit,z*unit));
            camera.transform(corner.add(arm.position()).add(offset));
            result=Math.max(result,corner.y-SHOULDER_FRUSTUM_TAN*corner.z);
        }
        return result;
    }
    public static float bottleFront(BottleRig.Frame rig,Vector3f offset,Quaternionf camera,BottleMotion.Action action,double age,int duration,boolean replacement) {
        return bottleFront(rig,offset,camera,action,age,duration,replacement,DrinkCatalog.ORANGE);
    }
    public static float bottleFront(BottleRig.Frame rig,Vector3f offset,Quaternionf camera,BottleMotion.Action action,double age,int duration,boolean replacement,DrinkProfile profile) {
        if(profile!=DrinkCatalog.ORANGE) {
            float s=profile.renderScale()/16;float[] a=profile.boundsMin(),b=profile.boundsMax(),cap=profile.capPivot();
            float front=boxFront(rig.bottle(),offset,camera,(a[0]-8)*s,(a[1]-4.8f)*s,(a[2]-8)*s,(b[0]-8)*s,(b[1]-4.8f)*s,(b[2]-8)*s);
            float corkBottom=BottleClosures.longCork(profile)?-BottleClosures.corkLength(profile)*16:profile.neckMin()*16-cap[1];
            if(DrinkMotion.capDetached(profile,action,age))front=Math.max(front,boxFront(rig.cork(),offset,camera,-2*s,corkBottom*s,-2*s,2*s,.1f*s,2*s));
            return front;
        }
        float scale=BottleRig.SCALE;
        float front=boxFront(rig.bottle(),offset,camera,-.13f*scale,-.30f*scale,-.13f*scale,.13f*scale,.707f*scale,.13f*scale);
        if(BottleMotion.corkVisible(action,age,duration,replacement))
            front=Math.max(front,boxFront(rig.cork(),offset,camera,-.08f*scale,-.32f*scale,-.08f*scale,.08f*scale,.01f*scale,.08f*scale));
        return front;
    }
    private static float armFront(BottleRig.Transform arm,Vector3f offset,Quaternionf camera,float lengthScale) {
        float unit=BottleRig.ARM_SCALE/16;
        return boxFront(arm,offset,camera,-2.25f*unit,-12.25f*lengthScale/16,-2.25f*unit,2.25f*unit,.25f*lengthScale/16,2.25f*unit);
    }
    private static float boxFront(BottleRig.Transform transform,Vector3f offset,Quaternionf camera,float x0,float y0,float z0,float x1,float y1,float z1) {
        float front=-Float.MAX_VALUE;
        for(float x:new float[]{x0,x1})for(float y:new float[]{y0,y1})for(float z:new float[]{z0,z1}) {
            Vector3f p=transform.rotation().transform(new Vector3f(x,y,z)).add(transform.position()).add(offset);
            front=Math.max(front,camera.transform(p).z);
        }
        return front;
    }
}


