package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.math.DecorationMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

/** Subdivide the original textured sheet so its free end can bend without opening seams. */
final class RibbonRenderer {
    private static final int DIVISIONS=8;
    private record Point(Vector3f position,float u,float v) {
        Point mix(Point b,float t) {return new Point(new Vector3f(position).lerp(b.position,t),u+(b.u-u)*t,v+(b.v-v)*t);}
    }
    static void render(BakedModel model,DecorationMotion.Part part,DecorationMotion.State state,
                       PoseStack pose,VertexConsumer buffer,int light) {
        var random=RandomSource.create(42);
        for(Direction face:Direction.values()) {
            random.setSeed(42);for(var quad:model.getQuads(null,face,random))draw(quad,part,state,pose,buffer,light);
        }
        random.setSeed(42);for(var quad:model.getQuads(null,null,random))draw(quad,part,state,pose,buffer,light);
    }
    private static void draw(BakedQuad quad,DecorationMotion.Part part,DecorationMotion.State state,
                             PoseStack pose,VertexConsumer buffer,int light) {
        int[] data=quad.getVertices();int stride=data.length/4;Point[] corners=new Point[4];
        for(int i=0;i<4;i++) {
            int p=i*stride;
            corners[i]=new Point(new Vector3f(Float.intBitsToFloat(data[p]),Float.intBitsToFloat(data[p+1]),Float.intBitsToFloat(data[p+2])).mul(16),
                Float.intBitsToFloat(data[p+4]),Float.intBitsToFloat(data[p+5]));
        }
        Point[][] grid=new Point[DIVISIONS+1][DIVISIONS+1];
        for(int row=0;row<=DIVISIONS;row++) {
            float v=row/(float)DIVISIONS;
            Point left=corners[0].mix(corners[3],v),right=corners[1].mix(corners[2],v);
            for(int col=0;col<=DIVISIONS;col++) {
                Point p=left.mix(right,col/(float)DIVISIONS);
                grid[row][col]=new Point(state.ribbonPoint(part,p.position).div(16),p.u,p.v);
            }
        }
        for(int row=0;row<DIVISIONS;row++)for(int col=0;col<DIVISIONS;col++) {
            Point a=grid[row][col],b=grid[row][col+1],c=grid[row+1][col+1],d=grid[row+1][col];
            var normal=new Vector3f(b.position).sub(a.position).cross(new Vector3f(d.position).sub(a.position));
            if(normal.lengthSquared()<1e-12f)continue;
            normal.normalize();
            vertex(a,normal,pose,buffer,light);vertex(b,normal,pose,buffer,light);
            vertex(c,normal,pose,buffer,light);vertex(d,normal,pose,buffer,light);
        }
    }
    private static void vertex(Point p,Vector3f normal,PoseStack pose,VertexConsumer buffer,int light) {
        buffer.vertex(pose.last().pose(),p.position.x,p.position.y,p.position.z).color(255,255,255,255).uv(p.u,p.v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(pose.last().normal(),normal.x,normal.y,normal.z).endVertex();
    }
    private RibbonRenderer() {}
}
