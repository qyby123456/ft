package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.math.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

/** Small textured fragments from the chocolate interior; rendered in the same first-person pass as the food. */
final class ChocolateCrumbRenderer {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("farmingtales_drink_anim:papercraft/diplomatico_coffee_0");
    private static final int[][] FACES={{0,0,1,0,1,2},{0,0,-1,0,1,2},{1,0,0,2,1,0},{-1,0,0,2,1,0},{0,1,0,0,2,1},{0,-1,0,0,2,1}};
    static void draw(PoseStack pose,MultiBufferSource buffers,int light,DrinkProfile p,BottleMotion.Action action,
                     double t,int duration,int side,boolean slim,int variant) {
        var crumbs=ChocolateCrumbs.sample(p,action,t,duration,side,slim,variant);if(crumbs.isEmpty())return;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TEXTURE);
        var buffer=buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        for(var crumb:crumbs) {
            pose.pushPose();pose.translate(crumb.position().x,crumb.position().y,crumb.position().z);
            pose.mulPose(new Quaternionf().rotateXYZ(crumb.spin(),crumb.spin()*.7f,crumb.spin()*.3f));
            float half=crumb.size()/2,u0=4.6f+(crumb.index()%4)*.6f,v0=6.55f;
            for(var face:FACES)for(int vertex=0;vertex<4;vertex++) {
                int a=vertex==0||vertex==3?-1:1,b=vertex<2?-1:1;
                float[] xyz=new float[3];xyz[face[3]]=a*half;xyz[face[4]]=b*half;
                xyz[face[5]]=half*(face[0]+face[1]+face[2]);
                buffer.vertex(pose.last().pose(),xyz[0],xyz[1],xyz[2]).color(255,255,255,255)
                    .uv(sprite.getU(u0+(a+1)*.22f),sprite.getV(v0+(b+1)*.20f))
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                    .normal(pose.last().normal(),face[0],face[1],face[2]).endVertex();
            }
            pose.popPose();
        }
    }
    private ChocolateCrumbRenderer() {}
}
