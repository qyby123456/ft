package com.farmingtales.drinkanim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Reveal only the glass vacated by the cork; preserve the baked geometry and UVs. */
final class NeckRenderer {
    private record Point(float x,float y,float z,float u,float v) {
        Point mix(Point b,float t) {return new Point(x+(b.x-x)*t,y+(b.y-y)*t,z+(b.z-z)*t,u+(b.u-u)*t,v+(b.v-v)*t);}
    }
    static void render(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,double reveal) {
        render(pose,buffers,light,stack,reveal,com.farmingtales.drinkanim.math.DrinkCatalog.ORANGE);
    }
    static void render(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,double reveal,com.farmingtales.drinkanim.math.DrinkProfile profile) {
        if(reveal<=0)return;
        var mc=Minecraft.getInstance();var model=mc.getModelManager().getModel(BottleRenderer.model(profile.neckModel()));
        var layer=RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        var buffer=buffers.getBuffer(layer);
        if(reveal>=1) {
            mc.getItemRenderer().renderModelLists(model,stack,light,OverlayTexture.NO_OVERLAY,pose,buffer);return;
        }
        float top=(float)(profile.neckMin()+reveal*(profile.neckMax()-profile.neckMin()));
        var random=RandomSource.create(42);
        for(Direction direction:Direction.values()) {
            random.setSeed(42);for(var quad:model.getQuads(null,direction,random))draw(quad,top,true,pose,buffer,light);
        }
        random.setSeed(42);for(var quad:model.getQuads(null,null,random))draw(quad,top,true,pose,buffer,light);
    }
    static void cork(PoseStack pose,MultiBufferSource buffers,int light,com.farmingtales.drinkanim.math.DrinkProfile profile,double lift) {
        var model=Minecraft.getInstance().getModelManager().getModel(BottleRenderer.model(profile.capModel()));
        var buffer=buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        float bottom=com.farmingtales.drinkanim.math.BottleClosures.corkClipBottom(profile,lift);
        var random=RandomSource.create(42);
        for(Direction direction:Direction.values()) {
            random.setSeed(42);for(var quad:model.getQuads(null,direction,random))draw(quad,bottom,false,pose,buffer,light);
        }
        random.setSeed(42);for(var quad:model.getQuads(null,null,random))draw(quad,bottom,false,pose,buffer,light);
    }
    private static void draw(BakedQuad quad,float top,boolean below,PoseStack pose,VertexConsumer buffer,int light) {
        int[] data=quad.getVertices();int stride=data.length/4;
        List<Point> points=new ArrayList<>(4);
        for(int i=0;i<4;i++) {
            int p=i*stride;
            points.add(new Point(Float.intBitsToFloat(data[p]),Float.intBitsToFloat(data[p+1]),Float.intBitsToFloat(data[p+2]),Float.intBitsToFloat(data[p+4]),Float.intBitsToFloat(data[p+5])));
        }
        List<Point> clipped=new ArrayList<>(5);
        Point a=points.get(3);
        for(Point b:points) {
            boolean insideA=below?a.y<=top:a.y>=top,insideB=below?b.y<=top:b.y>=top;
            if(insideA!=insideB)clipped.add(a.mix(b,(top-a.y)/(b.y-a.y)));
            if(insideB)clipped.add(b);
            a=b;
        }
        if(clipped.size()<3)return;
        var normal=quad.getDirection();
        for(int i=1;i<clipped.size()-1;i++) {
            vertex(clipped.get(0),normal,pose,buffer,light);
            vertex(clipped.get(i),normal,pose,buffer,light);
            vertex(clipped.get(i+1),normal,pose,buffer,light);
            vertex(clipped.get(i+1),normal,pose,buffer,light);
        }
    }
    private static void vertex(Point p,Direction normal,PoseStack pose,VertexConsumer buffer,int light) {
        buffer.vertex(pose.last().pose(),p.x,p.y,p.z).color(255,255,255,255).uv(p.u,p.v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(pose.last().normal(),normal.getStepX(),normal.getStepY(),normal.getStepZ()).endVertex();
    }
}
