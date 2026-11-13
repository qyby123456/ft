package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkAnimations;
import com.farmingtales.drinkanim.DrinkConfig;
import com.farmingtales.drinkanim.math.BottleMotion;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=DrinkAnimations.ID,value=Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void key(InputEvent.Key e) {
        if(e.getAction()!=GLFW.GLFW_PRESS)return;
        var mc=Minecraft.getInstance();
        if(mc.screen!=null||mc.player==null||!DrinkController.supports(mc.player.getMainHandItem()))return;
        var key=InputConstants.getKey(e.getKey(),e.getScanCode());
        KeyMapping drink=DrinkKeys.drinkKey();
        if(drink.isActiveAndMatches(key)) {while(drink.consumeClick()){} DrinkController.start(Action.DRINK);}
        else if(DrinkKeys.INSPECT.isActiveAndMatches(key)) {while(DrinkKeys.INSPECT.consumeClick()){} DrinkController.start(Action.INSPECT);}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mouse(InputEvent.MouseButton.Pre e) {
        var mc=Minecraft.getInstance();
        if(e.getAction()!=GLFW.GLFW_PRESS||mc.screen!=null||mc.player==null)return;
        if(DrinkController.active()&&DrinkController.action()!=Action.PLACE) {DrinkController.cancel();return;}
        if(!DrinkController.supports(mc.player.getMainHandItem()))return;
        var key=InputConstants.Type.MOUSE.getOrCreate(e.getButton());
        if(DrinkKeys.drinkKey().isActiveAndMatches(key)) {DrinkController.start(Action.DRINK);e.setCanceled(true);}
        else if(DrinkKeys.INSPECT.isActiveAndMatches(key)) {DrinkController.start(Action.INSPECT);e.setCanceled(true);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void tick(TickEvent.ClientTickEvent e) {
        if(e.phase==TickEvent.Phase.END) DrinkController.tick();
    }
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e) {
        var mc=Minecraft.getInstance();
        if(!DrinkController.renderable()||DrinkController.vanillaUseActive()||mc.player==null||!mc.options.getCameraType().isFirstPerson()
            ||!com.farmingtales.drinkanim.DrinkConfig.ENABLED.get())return;
        var motion=DrinkController.renderCamera((float)e.getPartialTick());
        e.setPitch(e.getPitch()+(float)motion.pitch());
        e.setYaw(e.getYaw()+(float)motion.yaw());
        e.setRoll(e.getRoll()+(float)motion.roll());
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {DrinkController.cancel();FirstPersonCompat.update(false);}
    @SubscribeEvent public static void sound(PlaySoundEvent e) {
        var sound=e.getSound();var mc=Minecraft.getInstance();
        if(sound==null||mc.player==null||DrinkController.action()!=Action.DRINK)return;
        String name=sound.getLocation().toString();
        if(!name.equals("minecraft:entity.generic.eat")&&!name.equals("minecraft:entity.generic.drink")&&!name.equals("minecraft:entity.player.burp"))return;
        double age=DrinkController.age(1),end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(DrinkController.duration());
        // Filter both local use sounds and the server's completion sound through Forge.
        // Leave LivingEntity's call sites available for other mods such as Grilling.
        if(age>=end-DrinkController.duration()-1&&age<=end+8
            &&mc.player.distanceToSqr(sound.getX(),sound.getY(),sound.getZ())<.64)e.setSound(null);
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e) {
        if(!DrinkConfig.ENABLED.get()||!DrinkController.supports(e.getItemStack()))return;
        e.getToolTip().add(Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(DrinkKeys.drinkKey().getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY))
            .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable("hint.farmingtales_drink_anim.drink").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("  "))
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(DrinkKeys.INSPECT.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY))
            .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable("hint.farmingtales_drink_anim.inspect").withStyle(ChatFormatting.GOLD)));
    }
}
