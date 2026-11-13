package com.farmingtales.drinkanim.client;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Native skin and sleeve vertices/UVs, collected through the same transform as the game. */
public final class ExportHandMeshes {
    public static void main(String[] args) throws Exception {
        Map<String,Object> meshes=new LinkedHashMap<>();
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{1,-1})for(boolean sleeve:new boolean[]{false,true}) {
            var pose=new PoseStack();VanillaHands.toPalmSpace(pose,slim,side);
            var collector=new Collector();
            VanillaHands.part(slim,side,sleeve).render(pose,collector,0,0);
            if(collector.vertices.size()!=24)throw new AssertionError("Expected six original skin faces");
            meshes.put((slim?"slim":"wide")+"/"+(side>0?"right":"left")+"/"+(sleeve?"sleeve":"skin"),collector.vertices);
        }
        Files.createDirectories(Path.of("build/blockbench"));
        Files.writeString(Path.of("build/blockbench/vanilla-arm-meshes.json"),new GsonBuilder().create().toJson(meshes));
        System.out.println("Exported native skin and sleeve geometry for both arm profiles and both hands.");
    }
    private static final class Collector implements VertexConsumer {
        final List<Map<String,Object>> vertices=new ArrayList<>();
        double[] position,uv,normal;
        public VertexConsumer vertex(double x,double y,double z){position=new double[]{x*16,y*16,z*16};return this;}
        public VertexConsumer uv(float u,float v){uv=new double[]{u*64,v*64};return this;}
        public VertexConsumer normal(float x,float y,float z){normal=new double[]{x,y,z};return this;}
        public VertexConsumer color(int r,int g,int b,int a){return this;}
        public VertexConsumer overlayCoords(int u,int v){return this;}
        public VertexConsumer uv2(int u,int v){return this;}
        public void endVertex(){vertices.add(Map.of("position",position,"uv",uv,"normal",normal));}
        public void defaultColor(int r,int g,int b,int a){}
        public void unsetDefaultColor(){}
    }
}
