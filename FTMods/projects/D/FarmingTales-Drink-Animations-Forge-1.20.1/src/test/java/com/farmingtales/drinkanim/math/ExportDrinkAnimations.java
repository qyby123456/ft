package com.farmingtales.drinkanim.math;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public final class ExportDrinkAnimations {
    public static void main(String[] args)throws Exception {
        Path out=Path.of("build/blockbench/drinks");Files.createDirectories(out);
        for(var profile:DrinkCatalog.all()) {
            if(args.length>0&&!List.of(args).contains(profile.id())
                &&!(List.of(args).contains("--letsdo")&&(profile.id().startsWith("brewery:")||profile.id().startsWith("vinery:")))
                &&!(List.of(args).contains("--papercraft")&&profile.id().startsWith("papercraft_magic_decoration:"))
                &&!(List.of(args).contains("--cups")&&profile.cup())
                &&!(List.of(args).contains("--cocktails")&&!profile.id().startsWith("farmingtales:"))
                &&!(List.of(args).contains("--prototypes")&&(!profile.jam()||profile.id().equals("farmingtales:ancient_fruit_jam_1"))))continue;
            Map<String,Object> clips=new LinkedHashMap<>();
            for(boolean slim:new boolean[]{false,true})for(String name:List.of("idle","draw","put_away","inspect","drink","drink_variant_2","drink_variant_3","drink_variant_4","walk","sprint","jump","place","preview_complete")) {
                int variant=name.startsWith("drink_variant_")?Integer.parseInt(name.substring(name.length()-1))-1:0;
                var segments=segments(profile,name);
                int ticks=segments.stream().mapToInt(Segment::ticks).sum();
                var frames=new ArrayList<Object>();var movement=new Locomotion();movement.reset(0);
                var decorations=new DecorationMotion.State();var jamLiquid=new JamLiquid.State();
                for(int f=0;f<=ticks*3;f++) {
                    double local=f/3.0;var segment=segments.get(segments.size()-1);
                    for(var candidate:segments) {
                        segment=candidate;
                        if(local<=candidate.ticks())break;
                        local-=candidate.ticks();
                    }
                    var action=segment.action();
                    double age=AnimationPlayback.authored(profile,action,local,16);
                    if(f%3==0) {
                        double t=f/3;boolean moving=(name.equals("walk")||name.equals("sprint"))&&t>=10&&t<60;
                        boolean jump=name.equals("jump")&&t>=5&&t<17;
                        movement.tick(Math.max(0,Math.min(50,t-10))*(name.equals("sprint")?.18:.12),moving?.16:0,!jump,name.equals("sprint")&&moving,
                            name.equals("jump")?(t==5?.42:t==17?.65:jump?-.08:0):0,0);
                    }
                    var motion=DrinkMotion.sample(profile,action,age,16,true);
                    motion=DrinkMotion.blendRunning(profile,action,motion,movement.run((f%3)/3.0)*DrinkMotion.idleBlend(profile,action,age,16,true));
                    motion=movement.apply(motion,(f%3)/3.0,action==BottleMotion.Action.IDLE?1:.25);
                    var rig=BottleRig.solve(motion,action,age,16,1,slim,profile);
                    rig=EatingMotion.apply(rig,profile,action,age,16,variant,1);
                    rig=BreathingMotion.apply(rig,f/3.0,1);
                    decorations.advance(f/60.0,rig.bottle().position(),rig.bottle().rotation(),0,0);
                    rig=ServingAnimations.sway(profile,action,age,rig,decorations.x(),decorations.z());
                    var frame=new LinkedHashMap<String,Object>();frame.put("time",f/60.0);
                    frame.put("bottle",transform(rig.bottle()));frame.put("hand",transform(rig.hand()));
                    frame.put("helper",transform(rig.helper()));frame.put("cap",transform(rig.cork()));
                    frame.put("helperVisible",rig.assistance()>0);frame.put("capVisible",ServingAnimations.owns(profile)?ServingAnimations.propVisible(profile,action,age,16,true):DrinkMotion.capVisible(profile,action,age,16,true));
                    frame.put("servingStage",ServingAnimations.propStage(profile,action,age));
                    frame.put("bottleVisible",action!=BottleMotion.Action.PLACE||PlacementMotion.bottleVisible(age,true));
                    frame.put("neckReveal",DrinkMotion.neckReveal(profile,action,age,16,true));frame.put("fill",motion.fill());
                    frame.put("jamOpen",profile.jam()&&JamAnimations.open(action,age,16,true));
                    frame.put("canOpening",DrinkMotion.canOpening(action,age,16,true));
                    var camera=movement.camera((f%3)/3.0,DrinkMotion.camera(profile,action,age,16,variant),DrinkMotion.movementCameraStrength(action)).rotation();frame.put("camera",new float[]{camera.x,camera.y,camera.z,camera.w});
                    var offset=BottleFraming.offset(rig,action,age,16,true,profile,camera);frame.put("offset",new float[]{offset.x,offset.y,offset.z});
                    var surface=LiquidSurface.contained(profile,profile.jam()?jamLiquid.sample(f/60.0,rig.bottle().rotation(),0,0,0):LiquidSurface.normal(profile,rig.bottle().rotation(),0,0,0,0),motion.fill());
                    var up=new Vector3f((float)surface.x(),(float)surface.y(),(float)surface.z());
                    var ornamentFrames=new LinkedHashMap<String,Object>();
                    for(var part:DecorationMotion.parts(profile)) {
                        var tr=decorations.transform(part,profile,new LiquidMesh.V(up.x,up.y,up.z),motion.fill(),action,age,16);
                        var q=tr.rotation();var pos=tr.offset();
                        ornamentFrames.put(part.name(),Map.of("offset",new float[]{pos.x,pos.y,pos.z},"quaternion",new float[]{q.x,q.y,q.z,q.w}));
                    }
                    frame.put("decorations",ornamentFrames);
                    if(profile.can())new Quaternionf().rotateY((float)Math.toRadians(-profile.canLid().yaw())).transform(up);
                    if(profile.cup()||profile.jam())new Quaternionf().rotateY((float)Math.toRadians(-profile.cupShape().liquidYaw())).transform(up);
                    var n=new LiquidMesh.V(up.x,up.y,up.z).unit();double level=ContainerLiquid.level(profile.liquid(),n,motion.fill());
                    frame.put("liquidLevel",level);
                    frame.put("liquidNormal",new double[]{n.x(),n.y(),n.z()});
                    if(BeerFoam.supports(profile)) {
                        var foam=BeerFoam.surface(profile,n,motion.fill(),f/3.0);
                        frame.put("foamNormal",new double[]{foam.normal().x(),foam.normal().y(),foam.normal().z()});
                        frame.put("foamLevel",foam.level());
                    }
                    frame.put("liquidFractions",profile.liquid().stream().map(r->LiquidMesh.fractionBelow(n,level,r.minimum(),r.maximum())).toList());
                    frames.add(frame);
                }
                clips.put((slim?"slim/":"wide/")+name,Map.of("length",ticks/20.0,"frames",frames));
            }
            Files.writeString(out.resolve((profile.id().startsWith("papercraft_magic_decoration:")?"papercraft_":"")+profile.id().split(":")[1]+".json"),new GsonBuilder().create().toJson(Map.of("profile",profile,"clips",clips,"decorations",DecorationMotion.parts(profile),"servingModels",ServingAnimations.models(profile))));
        }
        System.out.println("Exported selected profiles, both arm shapes and thirteen tracks, including four eating variants and continuous breathing.");
    }
    private record Segment(BottleMotion.Action action,int ticks) {}
    private static List<Segment> segments(DrinkProfile profile,String name) {
        if(name.startsWith("drink_variant_"))return List.of(timed(profile,BottleMotion.Action.DRINK));
        if(name.equals("preview_complete"))return List.of(
            timed(profile,BottleMotion.Action.DRAW),new Segment(BottleMotion.Action.IDLE,10),
            timed(profile,BottleMotion.Action.INSPECT),new Segment(BottleMotion.Action.IDLE,10),
            timed(profile,BottleMotion.Action.STOW));
        var action=switch(name){case "inspect"->BottleMotion.Action.INSPECT;case "drink"->BottleMotion.Action.DRINK;case "draw"->BottleMotion.Action.DRAW;case "put_away"->BottleMotion.Action.STOW;case "place"->BottleMotion.Action.PLACE;default->BottleMotion.Action.IDLE;};
        return List.of(action==BottleMotion.Action.IDLE?new Segment(action,name.equals("jump")?40:BreathingMotion.PERIOD_TICKS):timed(profile,action));
    }
    private static Segment timed(DrinkProfile p,BottleMotion.Action a) {return new Segment(a,AnimationPlayback.ticks(p,a,16));}
    private static Object transform(BottleRig.Transform t) {
        var p=t.position();var q=t.rotation();
        return Map.of("position",new float[]{p.x*16,p.y*16,p.z*16},"quaternion",new float[]{q.x,q.y,q.z,q.w});
    }
}
