package com.farmingtales.drinkanim.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.mojang.blaze3d.vertex.PoseStack;

/** Private vanilla parts cannot inherit a third-person renderer's animated transforms. */
final class VanillaHands {
    private static ModelPart wide,slim;
    static void toPalmSpace(PoseStack pose,boolean narrow,int side) {
        // Skin-model aiming uses the opposite X/Z basis. Correct the mesh, not the forearm path.
        pose.scale(-1,1,-1);
        pose.translate(side*(narrow?.5:1)/16,-10.0/16,0);
    }
    static ModelPart part(boolean narrow,int side,boolean sleeve) {
        if(narrow&&slim==null)slim=make(true);
        if(!narrow&&wide==null)wide=make(false);
        return (narrow?slim:wide).getChild((side>0?"right":"left")+(sleeve?"_sleeve":"_arm"));
    }
    private static ModelPart make(boolean narrow) {
        var root=LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,narrow),64,64).bakeRoot();
        for(String name:new String[]{"right_arm","left_arm","right_sleeve","left_sleeve"})root.getChild(name).loadPose(PartPose.ZERO);
        return root;
    }
}
