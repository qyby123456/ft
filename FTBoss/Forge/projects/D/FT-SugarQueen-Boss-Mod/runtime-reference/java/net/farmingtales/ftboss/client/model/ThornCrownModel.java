/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class ThornCrownModel
extends HumanoidModel<LivingEntity> {
    public ThornCrownModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.m_171576_();
        PartDefinition head = root.m_171599_("head", CubeListBuilder.m_171558_(), PartPose.f_171404_);
        for (String part : new String[]{"hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            root.m_171599_(part, CubeListBuilder.m_171558_(), PartPose.f_171404_);
        }
        head.m_171599_("band_z_-1", CubeListBuilder.m_171558_().m_171514_(1, 1).m_171481_(-5.0f, -0.5f, -0.5f, 10.0f, 1.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-7.3f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("band_x_-1", CubeListBuilder.m_171558_().m_171514_(25, 1).m_171481_(-0.5f, -0.5f, -5.0f, 1.0f, 1.0f, 10.0f), PartPose.m_171423_((float)-5.2f, (float)-7.55f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_-1_-2_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)-4.246448f, (float)-8.094978f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_-2_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)-4.632193f, (float)-9.026249f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_-2_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)-4.910787f, (float)-9.698833f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_-1_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)-2.246448f, (float)-8.094978f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_-1_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)-2.632193f, (float)-9.026249f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_-1_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)-2.910787f, (float)-9.698833f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_-1_0_0", CubeListBuilder.m_171558_().m_171514_(1, 14).m_171481_(-0.55f, -0.962f, -0.55f, 1.1f, 1.924f, 1.1f), PartPose.m_171423_((float)0.0f, (float)-8.351f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_-1_0_1", CubeListBuilder.m_171558_().m_171514_(8, 14).m_171481_(-0.35f, -0.592f, -0.35f, 0.7f, 1.184f, 0.7f), PartPose.m_171423_((float)0.0f, (float)-9.683f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_-1_0_2", CubeListBuilder.m_171558_().m_171514_(13, 14).m_171481_(-0.15f, -0.481f, -0.15f, 0.3f, 0.962f, 0.3f), PartPose.m_171423_((float)0.0f, (float)-10.645f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_-1_1_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)2.246448f, (float)-8.094978f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_-1_1_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)2.632193f, (float)-9.026249f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_-1_1_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)2.910787f, (float)-9.698833f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_-1_2_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)4.246448f, (float)-8.094978f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_-1_2_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)4.632193f, (float)-9.026249f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_-1_2_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)4.910787f, (float)-9.698833f, (float)-5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_x_-1_-1_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)-5.2f, (float)-8.181231f, (float)-3.020043f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_-1_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)-5.2f, (float)-9.012722f, (float)-3.364458f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_-1_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)-5.2f, (float)-9.613244f, (float)-3.613202f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_0_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)-5.2f, (float)-8.225f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_0_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)-5.2f, (float)-9.125f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_0_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)-5.2f, (float)-9.775f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_1_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)-5.2f, (float)-8.181231f, (float)3.020043f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_1_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)-5.2f, (float)-9.012722f, (float)3.364458f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_-1_1_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)-5.2f, (float)-9.613244f, (float)3.613202f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_-1_-3", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)-3.0f, (float)-7.45f, (float)-5.5f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("weave_x_-1_-3", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)-5.5f, (float)-7.8f, (float)-3.0f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_-1_0", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-7.45f, (float)-5.5f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("weave_x_-1_0", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)-5.5f, (float)-7.8f, (float)0.0f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_-1_3", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)3.0f, (float)-7.45f, (float)-5.5f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("weave_x_-1_3", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)-5.5f, (float)-7.8f, (float)3.0f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("band_z_1", CubeListBuilder.m_171558_().m_171514_(1, 1).m_171481_(-5.0f, -0.5f, -0.5f, 10.0f, 1.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-7.3f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("band_x_1", CubeListBuilder.m_171558_().m_171514_(25, 1).m_171481_(-0.5f, -0.5f, -5.0f, 1.0f, 1.0f, 10.0f), PartPose.m_171423_((float)5.2f, (float)-7.55f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_1_-2_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)-4.246448f, (float)-8.094978f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_-2_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)-4.632193f, (float)-9.026249f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_-2_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)-4.910787f, (float)-9.698833f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_-1_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)-2.246448f, (float)-8.094978f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_-1_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)-2.632193f, (float)-9.026249f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_-1_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)-2.910787f, (float)-9.698833f, (float)5.2f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("thorn_z_1_0_0", CubeListBuilder.m_171558_().m_171514_(1, 14).m_171481_(-0.55f, -0.962f, -0.55f, 1.1f, 1.924f, 1.1f), PartPose.m_171423_((float)0.0f, (float)-8.351f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_1_0_1", CubeListBuilder.m_171558_().m_171514_(8, 14).m_171481_(-0.35f, -0.592f, -0.35f, 0.7f, 1.184f, 0.7f), PartPose.m_171423_((float)0.0f, (float)-9.683f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_1_0_2", CubeListBuilder.m_171558_().m_171514_(13, 14).m_171481_(-0.15f, -0.481f, -0.15f, 0.3f, 0.962f, 0.3f), PartPose.m_171423_((float)0.0f, (float)-10.645f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_z_1_1_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)2.246448f, (float)-8.094978f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_1_1_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)2.632193f, (float)-9.026249f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_1_1_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)2.910787f, (float)-9.698833f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_1_2_0", CubeListBuilder.m_171558_().m_171514_(49, 1).m_171481_(-0.55f, -0.728f, -0.55f, 1.1f, 1.456f, 1.1f), PartPose.m_171423_((float)4.246448f, (float)-8.094978f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_1_2_1", CubeListBuilder.m_171558_().m_171514_(56, 1).m_171481_(-0.35f, -0.448f, -0.35f, 0.7f, 0.896f, 0.7f), PartPose.m_171423_((float)4.632193f, (float)-9.026249f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_z_1_2_2", CubeListBuilder.m_171558_().m_171514_(61, 1).m_171481_(-0.15f, -0.364f, -0.15f, 0.3f, 0.728f, 0.3f), PartPose.m_171423_((float)4.910787f, (float)-9.698833f, (float)5.2f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("thorn_x_1_-1_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)5.2f, (float)-8.181231f, (float)-3.020043f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_-1_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)5.2f, (float)-9.012722f, (float)-3.364458f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_-1_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)5.2f, (float)-9.613244f, (float)-3.613202f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_0_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)5.2f, (float)-8.225f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_0_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)5.2f, (float)-9.125f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_0_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)5.2f, (float)-9.775f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_1_0", CubeListBuilder.m_171558_().m_171514_(17, 14).m_171481_(-0.55f, -0.65f, -0.55f, 1.1f, 1.3f, 1.1f), PartPose.m_171423_((float)5.2f, (float)-8.181231f, (float)3.020043f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_1_1", CubeListBuilder.m_171558_().m_171514_(24, 14).m_171481_(-0.35f, -0.4f, -0.35f, 0.7f, 0.8f, 0.7f), PartPose.m_171423_((float)5.2f, (float)-9.012722f, (float)3.364458f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("thorn_x_1_1_2", CubeListBuilder.m_171558_().m_171514_(29, 14).m_171481_(-0.15f, -0.325f, -0.15f, 0.3f, 0.65f, 0.3f), PartPose.m_171423_((float)5.2f, (float)-9.613244f, (float)3.613202f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_1_-3", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)-3.0f, (float)-7.45f, (float)5.5f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("weave_x_1_-3", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)5.5f, (float)-7.8f, (float)-3.0f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_1_0", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-7.45f, (float)5.5f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("weave_x_1_0", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)5.5f, (float)-7.8f, (float)0.0f, (float)-0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("weave_z_1_3", CubeListBuilder.m_171558_().m_171514_(33, 14).m_171481_(-1.5f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.m_171423_((float)3.0f, (float)-7.45f, (float)5.5f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        head.m_171599_("weave_x_1_3", CubeListBuilder.m_171558_().m_171514_(43, 14).m_171481_(-0.5f, -0.5f, -1.5f, 1.0f, 1.0f, 3.0f), PartPose.m_171423_((float)5.5f, (float)-7.8f, (float)3.0f, (float)0.392699f, (float)0.0f, (float)0.0f));
        head.m_171599_("blood_heart", CubeListBuilder.m_171558_().m_171514_(53, 14).m_171481_(-1.0f, -1.0f, -0.5f, 2.0f, 2.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-8.0f, (float)-5.85f, (float)0.0f, (float)0.0f, (float)-0.785398f));
        head.m_171599_("blood_drop", CubeListBuilder.m_171558_().m_171514_(1, 20).m_171481_(-0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 1.0f), PartPose.m_171423_((float)0.0f, (float)-6.25f, (float)-5.6f, (float)0.0f, (float)0.0f, (float)-0.785398f));
        head.m_171599_("leaf_-1", CubeListBuilder.m_171558_().m_171514_(7, 20).m_171481_(-1.0f, -0.5f, -0.5f, 2.0f, 1.0f, 1.0f), PartPose.m_171423_((float)-2.6f, (float)-7.5f, (float)-5.85f, (float)0.0f, (float)0.0f, (float)0.392699f));
        head.m_171599_("leaf_1", CubeListBuilder.m_171558_().m_171514_(7, 20).m_171481_(-1.0f, -0.5f, -0.5f, 2.0f, 1.0f, 1.0f), PartPose.m_171423_((float)2.6f, (float)-7.5f, (float)-5.85f, (float)0.0f, (float)0.0f, (float)-0.392699f));
        return LayerDefinition.m_171565_((MeshDefinition)mesh, (int)64, (int)64);
    }
}

