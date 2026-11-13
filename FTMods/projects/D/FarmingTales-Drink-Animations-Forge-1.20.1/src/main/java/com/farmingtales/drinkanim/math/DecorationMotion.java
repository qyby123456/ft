package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3d;

/** Secondary motion driven by acceleration, with bounded, damped spring response. */
public final class DecorationMotion {
    public enum Kind { LEAF,RIBBON,ICE,STICK,FRUIT,FLOATING_FRUIT }
    public record Ribbon(float length,float gain,float[] outward,boolean rigid,float[] swingAxis,float maxAngle) {}
    public record Part(String name,String model,Kind kind,float[] pivot,float[] extent,boolean layered,boolean capAttached,Ribbon ribbon) {
        public Part(String name,String model,Kind kind,float[] pivot,float[] extent) {this(name,model,kind,pivot,extent,false,false,null);}
        public Part(String name,String model,Kind kind,float[] pivot,float[] extent,boolean layered) {this(name,model,kind,pivot,extent,layered,false,null);}
        public Part(String name,String model,Kind kind,float[] pivot,float[] extent,boolean layered,boolean capAttached) {this(name,model,kind,pivot,extent,layered,capAttached,null);}
    }
    public record Transform(Vector3f offset,Quaternionf rotation) {}
    private static final Map<String,List<Part>> PARTS=load();
    private static Map<String,List<Part>> load() {
        try(var in=DecorationMotion.class.getResourceAsStream("/assets/farmingtales_drink_anim/decorations.json")) {
            if(in==null)throw new IllegalStateException("Missing decoration definitions");
            return Collections.unmodifiableMap(new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),
                new TypeToken<Map<String,List<Part>>>(){}.getType()));
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static List<Part> parts(DrinkProfile profile) {return PARTS.getOrDefault(profile.id(),List.of());}
    public static final class State {
        private double lastTime=Double.NaN;
        private final Vector3f position=new Vector3f(),velocity=new Vector3f();
        private final Quaternionf orientation=new Quaternionf();
        private double x,z,vx,vz;
        private double yaw,yawSpeed,tailX,tailY,tailZ,tailVX,tailVY,tailVZ;
        private Vector3d previousEye;
        private Vector3f previousViewAt,viewPosition;
        public void reset() {lastTime=Double.NaN;x=z=vx=vz=0;yaw=yawSpeed=tailX=tailY=tailZ=tailVX=tailVY=tailVZ=0;velocity.zero();previousEye=null;previousViewAt=null;viewPosition=null;}
        public double x() {return x;}
        public double z() {return z;}
        /** Include movement of the eyes: a stationary camera-space bottle still moves when its owner turns or walks. */
        public void advanceWithView(double time,Vector3f at,Quaternionf rotation,Vector3d eye,Quaternionf view) {
            Vector3d lastEye=previousEye;
            Vector3f lastAt=previousViewAt,position=viewPosition;
            if(lastEye==null||!Double.isFinite(lastTime)||time<lastTime||time-lastTime>.25
                ||lastEye.distanceSquared(eye)>4096||position.lengthSquared()>4096) {
                reset();lastEye=new Vector3d(eye);lastAt=new Vector3f(at);position=new Vector3f();
            }
            if(Double.isFinite(lastTime)&&time-lastTime<1e-5)return;
            // Integrate only the player's translation and local hand travel. View turns already supply
            // angular drag; differentiating their orbital offset again cancels it at tick boundaries.
            position=new Vector3f(position).add(new Quaternionf(view).transform(new Vector3f(at).sub(lastAt)))
                .add((float)(eye.x-lastEye.x),(float)(eye.y-lastEye.y),(float)(eye.z-lastEye.z));
            advance(time,position,new Quaternionf(view).mul(rotation),0,0,.28);
            previousEye=new Vector3d(eye);previousViewAt=new Vector3f(at);viewPosition=position;
        }
        public void advance(double time,Vector3f at,Quaternionf rotation,double sloshX,double sloshZ) {
            advance(time,at,rotation,sloshX,sloshZ,.08);
        }
        private void advance(double time,Vector3f at,Quaternionf rotation,double sloshX,double sloshZ,double angularGain) {
            double dt=time-lastTime;
            if(!Double.isFinite(lastTime)||dt<0||dt>.25) {
                reset();lastTime=time;position.set(at);orientation.set(rotation);return;
            }
            if(dt<1e-5)return;
            Vector3f speed=new Vector3f(at).sub(position).div((float)dt);
            Vector3f acceleration=new Vector3f(speed).sub(velocity).div((float)dt);
            new Quaternionf(rotation).conjugate().transform(acceleration);
            Quaternionf delta=new Quaternionf(orientation).conjugate().mul(rotation);
            if(delta.w<0)delta.set(-delta.x,-delta.y,-delta.z,-delta.w);
            double targetX=limit(-acceleration.z*.026-delta.x/dt*angularGain+sloshZ*.65,.30);
            double targetZ=limit(acceleration.x*.026-delta.z/dt*angularGain-sloshX*.65,.30);
            // Ribbon ends also lag behind axial bottle turns, which do not excite the old X/Z spring.
            double targetYaw=limit(-delta.y/dt*.22,.45);
            int steps=Math.max(1,(int)Math.ceil(dt*120));double h=dt/steps;
            for(int i=0;i<steps;i++) {
                vx+=((targetX-x)*105-vx*11)*h;vz+=((targetZ-z)*105-vz*11)*h;
                x+=vx*h;z+=vz*h;
                if(Math.abs(x)>.38){x=limit(x,.38);vx*=.3;}
                if(Math.abs(z)>.38){z=limit(z,.38);vz*=.3;}
                yawSpeed+=((targetYaw-yaw)*75-yawSpeed*9)*h;yaw+=yawSpeed*h;
                tailVX+=((x-tailX)*48-tailVX*6)*h;tailVY+=((yaw-tailY)*48-tailVY*6)*h;
                tailVZ+=((z-tailZ)*48-tailVZ*6)*h;
                tailX+=tailVX*h;tailY+=tailVY*h;tailZ+=tailVZ*h;
            }
            velocity.set(speed);position.set(at);orientation.set(rotation);lastTime=time;
        }
        public Transform transform(Part part,DrinkProfile profile,LiquidMesh.V normal,double fill) {
            if(part.ribbon()!=null)return new Transform(new Vector3f(),part.ribbon().rigid()?rigidRibbonRotation(part.ribbon()):new Quaternionf());
            double strength=switch(part.kind()){case RIBBON->1;case LEAF->.72;case STICK->.28;case ICE->.16;case FRUIT->.32;case FLOATING_FRUIT->.23;};
            Vector3f offset=new Vector3f();
            double ax=x*strength,az=z*strength;
            if(part.kind()==Kind.STICK) {
                ax+=limit(normal.z(),.5)*.07;az-=limit(normal.x(),.5)*.07;
            }
            Quaternionf rotation=new Quaternionf().rotateX((float)ax).rotateZ((float)az);
            if((part.kind()==Kind.ICE||part.kind()==Kind.FLOATING_FRUIT)&&!profile.liquid().isEmpty()) {
                var region=profile.liquid().get(0);double yaw=Math.toRadians(profile.cupShape().liquidYaw());
                Quaternionf local=new Quaternionf().rotateY((float)-yaw);
                Vector3f n=local.transform(new Vector3f((float)normal.x(),(float)normal.y(),(float)normal.z()));
                double level=ContainerLiquid.level(profile.liquid(),new LiquidMesh.V(n.x,n.y,n.z),fill);
                float[] pivot=part.pivot(),extent=part.extent();
                Vector3f center=local.transform(new Vector3f(pivot[0]-8,pivot[1],pivot[2]-8));
                // Conservative rotated extent keeps all eight corners inside the cup.
                Vector3f radius=radius(local,rotation,extent);
                if(part.layered()) {
                    double roomX=(region.max()[0]-region.min()[0])*8,roomZ=(region.max()[2]-region.min()[2])*8;
                    double roomY=(profile.mouthY()-.08-region.min()[1]*16)*.5;
                    for(int i=0;i<40&&(radius.x>roomX||radius.z>roomZ||radius.y>roomY);i++) {
                        rotation.slerp(new Quaternionf(),.25f);radius=radius(local,rotation,extent);
                    }
                }
                double cx=center.x-z*.5,cz=center.z+x*.5;
                cx=bound(cx,region.min()[0]*16+radius.x,region.max()[0]*16-radius.x);
                cz=bound(cz,region.min()[2]*16+radius.z,region.max()[2]*16-radius.z);
                double cy=(16*level-n.x*cx-n.z*cz)/Math.max(.22,n.y)-radius.y*.45;
                if(part.layered()) {
                    // Keep the original submerged layers instead of collecting every cube on one plane.
                    double rest=ContainerLiquid.level(profile.liquid(),new LiquidMesh.V(0,1,0),.8)*16;
                    cy=center.y+(16*level-n.x*cx-n.z*cz)/Math.max(.22,n.y)-rest;
                }
                cy=bound(cy,region.min()[1]*16+radius.y,profile.mouthY()-radius.y-.08);
                offset.set((float)cx-center.x,(float)cy-center.y,(float)cz-center.z);
                new Quaternionf().rotateY((float)yaw).transform(offset);
            }
            return new Transform(offset,rotation);
        }
        public Transform transform(Part part,DrinkProfile profile,LiquidMesh.V normal,double fill,
                                   BottleMotion.Action action,double t,int duration) {
            Transform spring=transform(part,profile,normal,fill);
            return spring;
        }
        private Vector3f ribbonAngles(Ribbon settings) {
            var angles=new Vector3f((float)((.45*x+.95*tailX)*settings.gain()),
                (float)((.50*yaw+.80*tailY)*settings.gain()),(float)((.45*z+.95*tailZ)*settings.gain()));
            if(angles.length()>.85f)angles.normalize().mul(.85f);
            return angles;
        }
        private Quaternionf rigidRibbonRotation(Ribbon settings) {
            var angles=ribbonAngles(settings);
            float maxAngle=(float)Math.toRadians(settings.maxAngle());
            if(settings.swingAxis()!=null) {
                // Rotate along the model's safe plane (Dassai) or around the cap centre (leaves).
                // All motion inputs still excite the hinge; no part can tip through the cap.
                var axis=new Vector3f(settings.swingAxis()).normalize();
                float spin=angles.dot(axis)+.35f*(angles.x+angles.z)+.8f*angles.y;
                return new Quaternionf().rotationAxis((float)limit(spin,maxAngle),axis);
            }
            if(settings.outward()!=null) {
                // A wall-mounted ornament swings as one piece. Rotate in the wall's plane and
                // hinge outward from its top edge, instead of pushing individual vertices outward.
                var outward=new Vector3f(settings.outward()).normalize();
                var hinge=new Vector3f(0,-1,0).cross(outward).normalize();
                float spin=angles.dot(outward),flare=Math.abs(angles.dot(hinge))+.5f*Math.abs(angles.y);
                float magnitude=(float)Math.hypot(spin,flare);
                if(magnitude>maxAngle){spin*=maxAngle/magnitude;flare*=maxAngle/magnitude;}
                return new Quaternionf().rotationAxis(spin,outward).rotateAxis(flare,hinge);
            }
            if(angles.length()>maxAngle)angles.normalize().mul(maxAngle);
            return new Quaternionf().rotateXYZ(angles.x,angles.y,angles.z);
        }
        /** Model-pixel coordinates; rigid ornaments use exactly the whole-model render transform. */
        public Vector3f ribbonPoint(Part part,Vector3f point) {
            if(part.ribbon()==null)return new Vector3f(point);
            var settings=part.ribbon();var pivot=new Vector3f(part.pivot());
            var local=new Vector3f(point).sub(pivot);
            if(settings.rigid())return rigidRibbonRotation(settings).transform(local).add(pivot);
            double weight=Math.pow(Math.min(1,local.length()/Math.max(.1,settings.length())),1.2);
            var angles=ribbonAngles(settings);
            angles.mul((float)weight);
            var bent=new Quaternionf().rotateXYZ(angles.x,angles.y,angles.z).transform(new Vector3f(local));
            if(settings.outward()!=null) {
                // A ribbon lying against the bottle may flare outward, but cannot swing through its wall.
                var outward=new Vector3f(settings.outward()).normalize();
                float inward=new Vector3f(bent).sub(local).dot(outward);
                if(inward<0)bent.fma(-inward,outward);
            }
            return bent.add(pivot);
        }
        private static Vector3f radius(Quaternionf local,Quaternionf rotation,float[] extent) {
            Vector3f radius=new Vector3f();
            for(int i=-1;i<=1;i+=2)for(int j=-1;j<=1;j+=2)for(int k=-1;k<=1;k+=2) {
                var corner=local.transform(rotation.transform(new Vector3f(i*extent[0],j*extent[1],k*extent[2])));
                radius.max(new Vector3f(Math.abs(corner.x),Math.abs(corner.y),Math.abs(corner.z)));
            }
            return radius;
        }
    }
    private static double bound(double v,double lo,double hi) {return lo>hi?(lo+hi)*.5:Math.max(lo,Math.min(hi,v));}
    private static double limit(double value,double max) {return Math.max(-max,Math.min(max,value));}
    private DecorationMotion() {}
}
