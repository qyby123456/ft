package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import com.farmingtales.drinkanim.math.ConvexClip.Plane;

/** Bottle-local cuboids and their perspective occlusion volumes. No render-state overrides. */
public final class HandVolumes {
    private record Source(double[] min,double[] max,double[] origin,String axis,double angle,boolean rescale,double shiftY) {}
    private static final Map<String,List<Box>> BOXES=load();
    public static List<Box> boxes(DrinkProfile profile) {return BOXES.getOrDefault(profile.id(),List.of());}
    public static List<List<Plane>> occlusion(DrinkProfile profile,Vector3d eye) {
        var result=new ArrayList<List<Plane>>();
        // Opaque mugs already hide the far hand in the depth buffer. Extending
        // their cavity behind the rim cuts a visible hole in the raised wrist.
        for(var box:boxes(profile))result.add(profile.breweryMug()?box.interior():box.occlusion(eye));
        return result;
    }
    public static final class Box {
        private final Vector3d[] corners=new Vector3d[8];
        private final Plane[] faces=new Plane[6];
        private final Vector3d center=new Vector3d();
        public Box(Vector3d min,Vector3d max,Matrix4d transform) {
            for(int i=0;i<8;i++) {
                corners[i]=transform.transformPosition(new Vector3d((i&1)==0?min.x:max.x,(i&2)==0?min.y:max.y,(i&4)==0?min.z:max.z));
                center.add(corners[i]);
            }
            center.div(8);
            for(int axis=0;axis<3;axis++)for(int side=0;side<2;side++) {
                Vector3d normal=transform.transformDirection(new Vector3d().setComponent(axis,side==0?-1:1)).normalize();
                Vector3d point=corners[side<<axis];
                faces[axis*2+side]=new Plane(normal.x,normal.y,normal.z,normal.dot(point));
            }
        }
        public List<Plane> interior() {return List.of(faces);}
        public List<Plane> occlusion(Vector3d eye) {
            boolean[] front=new boolean[6];boolean any=false;
            List<Plane> planes=new ArrayList<>(9);
            for(int i=0;i<6;i++) {
                front[i]=faces[i].distance(eye)>1e-8;
                if(front[i]) {planes.add(faces[i]);any=true;}
            }
            if(!any)return interior();
            // Each silhouette edge contributes a plane through the eye. Together with
            // the near faces these hide intrusions and the far hand seen through glass.
            for(int axis=0;axis<3;axis++)for(int i=0;i<8;i++) {
                if((i&(1<<axis))!=0)continue;
                int a=(axis+1)%3,b=(axis+2)%3;
                if(front[a*2+((i>>a)&1)]==front[b*2+((i>>b)&1)])continue;
                Vector3d p=corners[i],q=corners[i|(1<<axis)];
                Vector3d n=new Vector3d(q).sub(p).cross(new Vector3d(eye).sub(p));
                if(n.lengthSquared()<1e-18)continue;
                n.normalize();double d=n.dot(p);
                if(n.dot(center)>d) {n.negate();d=-d;}
                planes.add(new Plane(n.x,n.y,n.z,d));
            }
            return planes;
        }
    }
    private static Map<String,List<Box>> load() {
        try(var stream=HandVolumes.class.getResourceAsStream("/assets/farmingtales_drink_anim/hand_volumes.json")) {
            if(stream==null)throw new IllegalStateException("Missing container hand masks");
            Map<String,List<Source>> data=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),new TypeToken<Map<String,List<Source>>>(){}.getType());
            var result=new LinkedHashMap<String,List<Box>>();
            for(var entry:data.entrySet()) {
                var boxes=new ArrayList<Box>();
                for(var s:entry.getValue()) {
                    Vector3d axis=switch(s.axis) {case "x"->new Vector3d(1,0,0);case "z"->new Vector3d(0,0,1);default->new Vector3d(0,1,0);};
                    Matrix4d transform=new Matrix4d().translate(0,s.shiftY,0).translate(s.origin[0],s.origin[1],s.origin[2]).rotate(Math.toRadians(s.angle),axis);
                    if(s.rescale) {
                        double factor=1/Math.cos(Math.toRadians(s.angle));
                        transform.scale(axis.x==0?factor:1,axis.y==0?factor:1,axis.z==0?factor:1);
                    }
                    transform.translate(-s.origin[0],-s.origin[1],-s.origin[2]);
                    boxes.add(new Box(new Vector3d(s.min),new Vector3d(s.max),transform));
                }
                result.put(entry.getKey(),List.copyOf(boxes));
            }
            return Collections.unmodifiableMap(result);
        } catch(java.io.IOException e) {throw new IllegalStateException("Cannot load hand masks",e);}
    }
    private HandVolumes() {}
}
