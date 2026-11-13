package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.*;
import com.farmingtales.drinkanim.DrinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

/** Observe the real optional mods in isolated clients; never register fake handlers. */
@Mod.EventBusSubscriber(modid="drink_animation_smoke",value=Dist.CLIENT)
public final class ModelCompatSmoke {
    private static final String KIND=System.getProperty("farmingtales.modelCompatSmoke","");
    private static int left,right,unhandled;
    private static boolean setup;
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void arm(RenderArmEvent event) {
        if(!KIND.equals("ysm")||!ModelAnimationCompat.armPass())return;
        if(!event.isCanceled()){unhandled++;return;}
        if(event.getArm()==net.minecraft.world.entity.HumanoidArm.LEFT)left++;else right++;
    }
    static void prepare(Minecraft mc) {
        if(setup||KIND.isEmpty())return;setup=true;
        check(ModList.get().isLoaded(KIND.equals("ysm")?"yes_steve_model":"punchy"),"real optional mod loaded");
        check(!ModList.get().isLoaded(KIND.equals("ysm")?"punchy":"yes_steve_model"),"other optional mod absent");
        if(KIND.equals("ysm"))mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"ysm model set @a default -");
        });
    }
    static void verify(Minecraft mc,int tick) {
        if(KIND.isEmpty())return;
        try {
            if(tick==60||tick==61||tick==90||tick==168||tick==169||tick==190)
                com.mojang.logging.LogUtils.getLogger().info("MODEL_COMPAT_STATE tick={} action={} age={} screen={} using={} view={} duration={}",tick,DrinkController.action(),DrinkController.age(1),mc.screen,mc.player.isUsingItem(),mc.options.getCameraType(),mc.player.getMainHandItem().getUseDuration());
            if(tick==50) {
                check(ModelAnimationCompat.ownsFirstPerson(),"supported item belongs to Lively Bites");
                var enabled=KIND.equals("punchy")?Class.forName("punchy.config.PunchyConfig").getMethod("isModEnabled"):null;
                if(enabled!=null)check(!(boolean)enabled.invoke(null),"Punchy yields hand rendering");
                if(enabled!=null)check(!(boolean)Class.forName("punchy.client.render.CameraAnimationState").getMethod("isActive").invoke(null),"Punchy yields camera motion");
                var view=mc.options.getCameraType();
                try {
                    mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    check(!ModelAnimationCompat.ownsFirstPerson(),"third person remains untouched");
                    if(enabled!=null)check((boolean)enabled.invoke(null),"Punchy restored in third person");
                } finally {mc.options.setCameraType(view);}
                if(KIND.equals("punchy")) {
                boolean configured=DrinkConfig.ENABLED.get();
                try {
                    DrinkConfig.ENABLED.set(false);
                    check(!ModelAnimationCompat.ownsFirstPerson(),"disabled animation yields control");
                    if(enabled!=null)check((boolean)enabled.invoke(null),"Punchy restored when animation disabled");
                } finally {DrinkConfig.ENABLED.set(configured);}
                }
            }
            if(tick==334&&KIND.equals("ysm")) {
                check(left>5&&right>5,"YSM renders both animated arms: left="+left+" right="+right+" fallback="+unhandled);
                check(!ModelAnimationCompat.armPass(),"arm event scope restored");
            }
            if(tick==334&&KIND.equals("punchy")) {
                check(DrinkController.vanillaUseActive()&&!ModelAnimationCompat.ownsFirstPerson(),"ordinary use yields control");
                check((boolean)Class.forName("punchy.config.PunchyConfig").getMethod("isModEnabled").invoke(null),"Punchy restored for ordinary use");
            }
            if(tick==366) {
                check(!ModelAnimationCompat.ownsFirstPerson(),"empty hand returns control");
                if(KIND.equals("punchy"))check((boolean)Class.forName("punchy.config.PunchyConfig").getMethod("isModEnabled").invoke(null),"Punchy resumes after stowing");
                com.mojang.logging.LogUtils.getLogger().info("MODEL_COMPAT_COMPLETE {}",KIND);
            }
        } catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    private static void check(boolean value,String label) {
        if(!value)throw new IllegalStateException("MODEL_COMPAT_FAIL "+KIND+" "+label);
        com.mojang.logging.LogUtils.getLogger().info("MODEL_COMPAT_PASS {} {}",KIND,label);
    }
}
