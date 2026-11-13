package com.farmingtales.drinkanim.math;

import com.google.gson.GsonBuilder;
import org.joml.Vector3f;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Export directly from the runtime rig; no duplicated animation curves. Not shipped in the mod. */
public final class ExportAnimations {
    public static void main(String[] args) throws Exception {
        Map<String,Object> clips=new LinkedHashMap<>();
        for(boolean slim:new boolean[]{false,true})for(String name:List.of("idle","draw","put_away","inspect","drink","walk","sprint","jump")) {
            var action=switch(name){case "inspect"->BottleMotion.Action.INSPECT;case "drink"->BottleMotion.Action.DRINK;case "draw"->BottleMotion.Action.DRAW;case "put_away"->BottleMotion.Action.STOW;default->BottleMotion.Action.IDLE;};
            int ticks=switch(name){case "inspect"->BottleMotion.INSPECT_TICKS;case "drink"->BottleMotion.totalDrinkTicks(16);case "draw"->BottleMotion.DRAW_TICKS;case "put_away"->BottleMotion.STOW_TICKS;case "idle"->20;case "jump"->40;default->80;};
            var frames=new ArrayList<Object>();
            var movement=new Locomotion();movement.reset(0);
            for(int frame=0;frame<=ticks*3;frame++) {
                double age=frame/3.0;
                if(frame%3==0) {
                    double t=frame/3;
                    boolean moving=(name.equals("walk")||name.equals("sprint"))&&t>=10&&t<60;
                    boolean jumping=name.equals("jump")&&t>=5&&t<17;
                    movement.tick(Math.max(0,Math.min(50,t-10))*(name.equals("sprint")?.18:.12),moving?.16:0,!jumping,name.equals("sprint")&&moving,
                        name.equals("jump")?(t==5?.42:t==17?.65:jumping?-.08:0):0,0);
                }
                var motion=BottleMotion.sample(action,age,16,true);
                if(action==BottleMotion.Action.IDLE)motion=motion.mix(BottleMotion.RUN,movement.run((frame%3)/3.0));
                motion=movement.apply(motion,(frame%3)/3.0,action==BottleMotion.Action.IDLE?1:.25);
                var rig=BottleRig.solve(motion,action,age,16,1,slim);
                Map<String,Object> values=new LinkedHashMap<>();
                values.put("time",age/20);values.put("bottle",transform(rig.bottle()));values.put("hand",transform(rig.hand()));
                values.put("helper",transform(rig.helper()));values.put("cork",transform(rig.cork()));
                values.put("elbow",transform(new BottleRig.Transform(ArmKinematics.elbow(rig.hand().position(),rig.hand().rotation()),rig.hand().rotation())));
                values.put("helperElbow",transform(new BottleRig.Transform(ArmKinematics.elbow(rig.helper().position(),rig.helper().rotation()),rig.helper().rotation())));
                values.put("helperVisible",rig.assistance()>0);values.put("corkVisible",BottleMotion.corkVisible(action,age,16,true));
                values.put("corkAttached",!BottleRig.corkDetached(action,age));
                values.put("corkSeatOffset",BottleRig.CORK_SEATED_HEIGHT*BottleRig.SCALE*16);
                values.put("neckJoinOffset",BottleRig.NECK_JOIN_Y*BottleRig.SCALE*16);
                values.put("open",BottleMotion.mouthOpen(action,age,16,true));values.put("fill",motion.fill());
                values.put("mouthReveal",BottleMotion.mouthReveal(action,age,16,true));
                values.put("helperScale",new float[]{BottleRig.ARM_SCALE,BottleRig.ARM_SCALE,BottleRig.ARM_SCALE});
                var offset=BottleFraming.offset(rig,action,age,16,true);
                values.put("offset",new float[]{offset.x,offset.y,offset.z});
                var camera=BottleMotion.camera(action,age,16).rotation();
                values.put("camera",new float[]{camera.x,camera.y,camera.z,camera.w});
                var up=new Vector3f(0,1,0);new org.joml.Quaternionf(rig.bottle().rotation()).conjugate().transform(up);
                values.put("liquidNormal",new double[]{up.x,up.y,up.z});
                frames.add(values);
            }
            clips.put((slim?"slim/":"wide/")+name,Map.of("length",ticks/20.0,"frames",frames));
        }
        Files.createDirectories(Path.of("build/blockbench"));
        Files.writeString(Path.of("build/blockbench/runtime-motion.json"),new GsonBuilder().create().toJson(clips));
        System.out.println("Exported 8 animation clips for both profiles directly from BottleRig and BottleFraming at 60 fps.");
    }
    private static Object transform(BottleRig.Transform t) {
        var p=t.position();var q=t.rotation();
        return Map.of("position",new double[]{p.x*16,p.y*16,p.z*16},"quaternion",new double[]{q.x,q.y,q.z,q.w});
    }
}
