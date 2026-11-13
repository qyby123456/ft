package com.farmingtales.drinkanim.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.farmingtales.drinkanim.math.ConvexClip;
import com.farmingtales.drinkanim.math.DrinkProfile;
import com.farmingtales.drinkanim.math.HandVolumes;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

/** Clips only this animation's hand vertices before forwarding to vanilla/shader buffers. */
public final class HandClipper implements VertexConsumer {
    public record Mask(Matrix4f toBottle,List<List<ConvexClip.Plane>> volumes) {
        public static Mask create(Matrix4f bottleToView,DrinkProfile profile) {
            Matrix4f inverse=new Matrix4f(bottleToView).invert();
            Vector3f eye=inverse.transformPosition(new Vector3f());
            return new Mask(inverse,HandVolumes.occlusion(profile,new Vector3d(eye)));
        }
    }
    private record Vertex(double x,double y,double z,double vx,double vy,double vz,float u,float v,
        float r,float g,float b,float a,float nx,float ny,float nz,int overlayU,int overlayV,int lightU,int lightV)
        implements ConvexClip.Point<Vertex> {
        public Vertex mix(Vertex q,double t) {
            float f=(float)t;
            return new Vertex(x+(q.x-x)*t,y+(q.y-y)*t,z+(q.z-z)*t,vx+(q.vx-vx)*t,vy+(q.vy-vy)*t,vz+(q.vz-vz)*t,
                u+(q.u-u)*f,v+(q.v-v)*f,r+(q.r-r)*f,g+(q.g-g)*f,b+(q.b-b)*f,a+(q.a-a)*f,
                nx+(q.nx-nx)*f,ny+(q.ny-ny)*f,nz+(q.nz-nz)*f,overlayU,overlayV,lightU,lightV);
        }
    }
    private final VertexConsumer delegate;
    private final Mask mask;
    private final Vertex[] quad=new Vertex[4];
    private int count;
    private double x,y,z;
    private float u,v,r=255,g=255,b=255,a=255,nx,ny,nz;
    private int overlayU,overlayV,lightU,lightV;
    public HandClipper(VertexConsumer delegate,Mask mask) {this.delegate=delegate;this.mask=mask;}
    @Override public VertexConsumer vertex(double x,double y,double z) {this.x=x;this.y=y;this.z=z;return this;}
    @Override public VertexConsumer color(int r,int g,int b,int a) {this.r=r;this.g=g;this.b=b;this.a=a;return this;}
    @Override public VertexConsumer uv(float u,float v) {this.u=u;this.v=v;return this;}
    @Override public VertexConsumer overlayCoords(int u,int v) {overlayU=u;overlayV=v;return this;}
    @Override public VertexConsumer uv2(int u,int v) {lightU=u;lightV=v;return this;}
    @Override public VertexConsumer normal(float x,float y,float z) {nx=x;ny=y;nz=z;return this;}
    @Override public void defaultColor(int r,int g,int b,int a) {color(r,g,b,a);}
    @Override public void unsetDefaultColor() {}
    @Override public void endVertex() {
        Vector3f local=mask.toBottle.transformPosition(new Vector3f((float)x,(float)y,(float)z));
        quad[count++]=new Vertex(local.x,local.y,local.z,x,y,z,u,v,r,g,b,a,nx,ny,nz,overlayU,overlayV,lightU,lightV);
        if(count<4)return;
        List<Vertex> original=List.of(quad[0],quad[1],quad[2],quad[3]);
        for(var polygon:ConvexClip.subtract(original,mask.volumes)) {
            if(polygon.size()==4) {for(var point:polygon)emit(point);}
            else for(int i=1;i<polygon.size()-1;i++) {
                emit(polygon.get(0));emit(polygon.get(i));emit(polygon.get(i+1));emit(polygon.get(i+1));
            }
        }
        count=0;
    }
    private void emit(Vertex p) {
        delegate.vertex(p.vx,p.vy,p.vz).color(Math.round(p.r),Math.round(p.g),Math.round(p.b),Math.round(p.a))
            .uv(p.u,p.v).overlayCoords(p.overlayU,p.overlayV).uv2(p.lightU,p.lightV).normal(p.nx,p.ny,p.nz).endVertex();
    }
}
