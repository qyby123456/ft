package com.farmingtales.drinkanim.client;

import com.mojang.blaze3d.vertex.*;
import com.farmingtales.drinkanim.math.*;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandClipperTest {
    private record Vertex(Vector3f position,float u,float v,int color,int overlay,int light,Vector3f normal) {}
    private static final class Capture implements VertexConsumer {
        final List<Vertex> vertices=new ArrayList<>();
        Vector3f p,n;float u,v;int color,overlay,light;
        public VertexConsumer vertex(double x,double y,double z) {p=new Vector3f((float)x,(float)y,(float)z);return this;}
        public VertexConsumer uv(float u,float v) {this.u=u;this.v=v;return this;}
        public VertexConsumer normal(float x,float y,float z) {n=new Vector3f(x,y,z);return this;}
        public VertexConsumer color(int r,int g,int b,int a) {color=(a<<24)|(r<<16)|(g<<8)|b;return this;}
        public VertexConsumer overlayCoords(int u,int v) {overlay=u|(v<<16);return this;}
        public VertexConsumer uv2(int u,int v) {light=u|(v<<16);return this;}
        public void defaultColor(int r,int g,int b,int a) {}
        public void unsetDefaultColor() {}
        public void endVertex() {vertices.add(new Vertex(p,u,v,color,overlay,light,n));}
    }
    @Test void clipsRealSkinAndSleeveMeshesForBothHandSizesAndDominantHands() {
        int removed=0,retained=0;
        for(var profile:DrinkCatalog.all())for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1})
            for(var action:List.of(BottleMotion.Action.IDLE,BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK))
                for(int age:action==BottleMotion.Action.IDLE?new int[]{0}:action==BottleMotion.Action.INSPECT?new int[]{32,61}:new int[]{23,38,60,80,136}) {
                    var motion=DrinkMotion.sample(profile,action,age,16,true);
                    var rig=BottleRig.solve(motion,action,age,16,side,slim,profile);
                    var offset=BottleFraming.offset(rig,action,age,16,true,profile);
                    PoseStack pose=new PoseStack();pose.mulPose(DrinkMotion.camera(profile,action,age,16).rotation());pose.translate(offset.x,offset.y,offset.z);
                    var matrix=new Matrix4f(pose.last().pose()).translate(rig.bottle().position()).scale(profile.renderScale())
                        .rotate(rig.bottle().rotation()).translate(-.5f,-.3f,-.5f).scale(1f/16);
                    var mask=HandClipper.Mask.create(matrix,profile);
                    for(boolean helper:new boolean[]{false,true})for(boolean sleeve:new boolean[]{false,true}) {
                        if(helper&&rig.assistance()==0)continue;
                        var hand=helper?rig.helper():rig.hand();int handSide=helper?-side:side;
                        pose.pushPose();pose.translate(hand.position().x,hand.position().y,hand.position().z);pose.mulPose(hand.rotation());
                        pose.scale(BottleRig.ARM_SCALE,BottleRig.ARM_SCALE,BottleRig.ARM_SCALE);VanillaHands.toPalmSpace(pose,slim,handSide);
                        var original=new Capture();var clipped=new Capture();var part=VanillaHands.part(slim,handSide,sleeve);
                        part.render(pose,original,15728880,655360);
                        part.render(pose,new HandClipper(clipped,mask),15728880,655360);
                        pose.popPose();
                        assertEquals(0,clipped.vertices.size()%4);
                        if(!clipped.vertices.equals(original.vertices))removed++;
                        retained+=clipped.vertices.size();
                        for(var vertex:clipped.vertices) {
                            assertEquals(-1,vertex.color);assertEquals(655360,vertex.overlay);assertEquals(15728880,vertex.light);
                            assertTrue(vertex.u>=0&&vertex.u<=1&&vertex.v>=0&&vertex.v<=1);
                            assertTrue(Float.isFinite(vertex.position.length()));
                            assertEquals(1,vertex.normal.length(),1e-5);
                        }
                        for(int q=0;q<clipped.vertices.size();q+=4)for(int j=1;j<=2;j++) {
                            var center=new Vector3f(clipped.vertices.get(q).position).add(clipped.vertices.get(q+j).position).add(clipped.vertices.get(q+j+1).position).div(3);
                            Vector3d local=new Vector3d(mask.toBottle().transformPosition(center));
                            assertFalse(mask.volumes().stream().anyMatch(v->v.stream().allMatch(p->p.distance(local)<-1e-4)),profile.id()+" retained triangle inside bottle");
                        }
                    }
                }
        assertTrue(removed>100);assertTrue(retained>1000,"Keep the visible outside forearms");
    }
}
