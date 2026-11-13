package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.*;
import com.farmingtales.drinkanim.math.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.*;
import org.joml.Vector3f;

/** Observe the real render clock, decoration spring and world camera during sipping. */
@Mod.EventBusSubscriber(value=Dist.CLIENT)
public final class CoconutFrameSmoke {
    private static final List<String> rows=new ArrayList<>();
    private static double nextCapture;
    private static int number;
    private static boolean recorded;
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e)throws Exception {
        if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("farmingtales.captureSmooth"))return;
        var mc=Minecraft.getInstance();
        if(mc.player==null||!CoconutAnimations.owns(DrinkController.profile()))return;
        var action=DrinkController.action();
        double t=DrinkController.age(e.renderTickTime);
        int duration=DrinkController.duration();double end=48+BottleMotion.sipTicks(duration);
        if(action!=BottleMotion.Action.DRINK||t>end) {
            if(!rows.isEmpty()&&!recorded) {
                Files.write(Path.of("coconut-frames-"+mc.getUser().getName()+".csv"),rows);recorded=true;
            }
            return;
        }
        if(recorded)return;
        if(rows.isEmpty())rows.add("tick,handX,handY,handZ,strawX,strawY,strawZ,strawW,cameraPitch,cameraYaw,screenshot,tilt,fill,fruitVisible,fruitStage");
        var p=DrinkController.profile();int side=mc.player.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT?1:-1;
        var pose=DrinkMotion.sample(p,action,t,duration,true);
        var rig=BottleRig.solve(pose,action,t,duration,side,mc.player.getModelName().equals("slim"),p);
        rig=EatingMotion.apply(rig,p,action,t,duration,DrinkController.eatingVariant(),side);
        rig=BreathingMotion.apply(rig,mc.player.tickCount+e.renderTickTime,side);
        var field=BottleRenderer.class.getDeclaredField("DECORATIONS");field.setAccessible(true);
        var state=(DecorationMotion.State)field.get(null);
        var stick=DecorationMotion.parts(p).stream().filter(part->part.kind()==DecorationMotion.Kind.STICK).findFirst().orElseThrow();
        var up=new org.joml.Quaternionf(rig.bottle().rotation()).conjugate().transform(new Vector3f(0,1,0));
        var straw=state.transform(stick,p,new LiquidMesh.V(up.x,up.y,up.z),pose.fill(),action,t,duration).rotation();
        var hand=rig.hand().position();var camera=mc.gameRenderer.getMainCamera();
        int shot=-1;
        if(t>=nextCapture) {
            shot=number++;
            Screenshot.grab(mc.gameDirectory,"coconut-v1874-"+mc.getUser().getName()+"-"+String.format(Locale.ROOT,"%04d",shot)+".png",mc.getMainRenderTarget(),c->{});
            nextCapture=t+1;
        }
        rows.add(t+","+hand.x+","+hand.y+","+hand.z+","+straw.x+","+straw.y+","+straw.z+","+straw.w+","
            +(camera.getXRot()-mc.player.getXRot())+","+net.minecraft.util.Mth.wrapDegrees(camera.getYRot()-mc.player.getYRot())+","+shot+","+pose.pitch()+","+pose.fill()+","+ServingAnimations.propVisible(p,action,t,duration,true)+","+ServingAnimations.propStage(p,action,t));
    }
}
