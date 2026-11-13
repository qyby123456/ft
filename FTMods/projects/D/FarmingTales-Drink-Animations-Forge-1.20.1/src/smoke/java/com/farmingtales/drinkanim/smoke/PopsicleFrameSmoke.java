package com.farmingtales.drinkanim.smoke;
import com.farmingtales.drinkanim.client.DrinkController;
import com.farmingtales.drinkanim.math.BottleMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.*;
@Mod.EventBusSubscriber(value=Dist.CLIENT)
public final class PopsicleFrameSmoke {
 private static final List<String> rows=new ArrayList<>();
 private static long previous;
 private static double nextCapture;
 private static BottleMotion.Action last=BottleMotion.Action.IDLE;
 private static int number;
 @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e)throws Exception {
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("farmingtales.captureSmooth"))return;
  var mc=Minecraft.getInstance();if(mc.player==null||!DrinkController.profile().id().endsWith(":long_island_popsicle_tea"))return;
  var action=DrinkController.action();
  if(action!=last){
   if(!rows.isEmpty())Files.write(Path.of("popsicle-frames-"+mc.getUser().getName()+"-"+last+".csv"),rows);
   rows.clear();rows.add("nanoTime,authoredTick,partialTick,frameMillis,screenshot");number=0;nextCapture=0;previous=0;last=action;
  }
  if(action!=BottleMotion.Action.INSPECT&&action!=BottleMotion.Action.DRINK)return;
  double age=DrinkController.age(e.renderTickTime);long now=System.nanoTime();
  boolean shot=age>=nextCapture&&(action==BottleMotion.Action.INSPECT||age<=48);
  if(shot){
   String file="smooth-v1867-"+mc.getUser().getName()+"-"+action+"-"+String.format(java.util.Locale.ROOT,"%04d",number++)+".png";
   Screenshot.grab(mc.gameDirectory,file,mc.getMainRenderTarget(),c->{});
   nextCapture=age+(action==BottleMotion.Action.INSPECT?20.0/45:20.0/75);
  }
  rows.add(now+","+age+","+e.renderTickTime+","+(previous==0?0:(now-previous)/1e6)+","+(shot?number-1:-1));previous=now;
 }
}