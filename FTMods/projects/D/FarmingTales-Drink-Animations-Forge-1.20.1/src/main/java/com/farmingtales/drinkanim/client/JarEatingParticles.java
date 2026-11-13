package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.math.BottleMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.ItemStack;

public final class JarEatingParticles {
    public static boolean hide(ParticleOptions options,double x,double y,double z) {
        var mc=Minecraft.getInstance();
        if(!(options instanceof ItemParticleOption item)||mc.player==null
            ||!mc.options.getCameraType().isFirstPerson()||DrinkController.action()!=BottleMotion.Action.DRINK
            ||(!DrinkController.profile().jam()&&!DrinkController.profile().food()))return false;
        double age=DrinkController.age(1),end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(DrinkController.duration());
        return age>=end-DrinkController.duration()-1&&age<=end+8
            &&ItemStack.isSameItemSameTags(item.getItem(),DrinkController.renderStack(mc.player.getMainHandItem()))
            &&mc.player.getEyePosition().distanceToSqr(x,y,z)<1.44;
    }
    private JarEatingParticles() {}
}
