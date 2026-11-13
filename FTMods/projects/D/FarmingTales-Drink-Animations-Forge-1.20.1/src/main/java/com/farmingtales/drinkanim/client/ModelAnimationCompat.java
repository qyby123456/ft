package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/** Optional model renderers participate without importing or bundling their classes. */
public final class ModelAnimationCompat {
    private static int armPassDepth;
    public static boolean ownsFirstPerson() {
        var mc=Minecraft.getInstance();
        return mc.player!=null&&mc.options!=null&&mc.options.getCameraType().isFirstPerson()
            &&DrinkConfig.ENABLED.get()&&!DrinkController.vanillaUseActive()
            &&DrinkController.supports(DrinkController.renderStack(mc.player.getMainHandItem()));
    }
    public static boolean armPass() {return armPassDepth>0;}
    static boolean renderModelArm(PoseStack pose,MultiBufferSource buffers,int light,int side,boolean slim,HandClipper.Mask mask) {
        if(!ModList.get().isLoaded("yes_steve_model"))return false;
        var player=Minecraft.getInstance().player;
        pose.pushPose();armPassDepth++;
        try {
            // RenderArmEvent normally runs with PlayerModel's shoulder pivot.
            // Remove that pivot, retaining our animated palm-space transform.
            VanillaHands.toPalmSpace(pose,slim,side);
            pose.translate(side*5.0/16,-(slim?2.5:2.0)/16,0);
            MultiBufferSource clipped=type->new HandClipper(buffers.getBuffer(type),mask);
            return MinecraftForge.EVENT_BUS.post(new RenderArmEvent(pose,clipped,light,player,
                side>0?HumanoidArm.RIGHT:HumanoidArm.LEFT));
        } finally {armPassDepth--;pose.popPose();}
    }
    private ModelAnimationCompat() {}
}
