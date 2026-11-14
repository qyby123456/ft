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
 *  net.minecraft.world.entity.EquipmentSlot
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class ThornArmorModel
extends HumanoidModel<LivingEntity> {
    public ThornArmorModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createLayer(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.CHEST) {
            return ThornArmorModel.createChestLayer();
        }
        if (equipmentSlot == EquipmentSlot.LEGS) {
            return ThornArmorModel.createLegsLayer();
        }
        if (equipmentSlot == EquipmentSlot.FEET) {
            return ThornArmorModel.createFeetLayer();
        }
        throw new IllegalArgumentException("Unsupported thorn armor slot: " + String.valueOf(equipmentSlot));
    }

    private static LayerDefinition createChestLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("head", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition3 = partDefinition.m_171599_("hat", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition4 = partDefinition.m_171599_("body", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-4.45f, -0.1f, -2.55f, 8.9f, 12.5f, 5.1f).m_171514_(32, 0).m_171481_(-4.7f, 0.6f, -3.0f, 9.4f, 6.4f, 6.0f).m_171514_(0, 20).m_171481_(-4.4f, 6.75f, -3.08f, 8.8f, 5.25f, 6.16f).m_171514_(32, 20).m_171481_(-4.8f, 10.9f, -3.18f, 9.6f, 2.35f, 6.36f).m_171514_(0, 32).m_171481_(-0.55f, 0.8f, -3.58f, 1.1f, 10.2f, 0.53f).m_171514_(80, 0).m_171481_(-0.62f, 0.8f, 3.02f, 1.24f, 11.2f, 0.6f).m_171514_(64, 32).m_171481_(-0.62f, 3.4f, -4.0f, 1.24f, 1.25f, 0.5f).m_171514_(32, 32).m_171481_(-0.72f, 1.65f, -3.98f, 1.44f, 1.9f, 0.46f).m_171514_(32, 32).m_171481_(-0.72f, 4.45f, -3.98f, 1.44f, 1.9f, 0.46f), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        partDefinition4.m_171599_("rib_l_0", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.7f, -0.5f, -0.23f, 3.35f, 1.0f, 0.46f), PartPose.m_171423_((float)-2.1f, (float)3.4f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)0.15708f));
        partDefinition4.m_171599_("rib_r_0", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.65f, -0.5f, -0.23f, 3.35f, 1.0f, 0.46f), PartPose.m_171423_((float)2.1f, (float)3.4f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)-0.15708f));
        partDefinition4.m_171599_("rib_l_1", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.9f, -0.5f, -0.23f, 3.55f, 1.0f, 0.46f), PartPose.m_171423_((float)-2.1f, (float)5.45f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)0.10472f));
        partDefinition4.m_171599_("rib_r_1", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.65f, -0.5f, -0.23f, 3.55f, 1.0f, 0.46f), PartPose.m_171423_((float)2.1f, (float)5.45f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)-0.10472f));
        partDefinition4.m_171599_("rib_l_2", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.65f, -0.5f, -0.23f, 3.3f, 1.0f, 0.46f), PartPose.m_171423_((float)-2.1f, (float)7.55f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)0.06981f));
        partDefinition4.m_171599_("rib_r_2", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.65f, -0.5f, -0.23f, 3.3f, 1.0f, 0.46f), PartPose.m_171423_((float)2.1f, (float)7.55f, (float)-3.25f, (float)0.0f, (float)0.0f, (float)-0.06981f));
        partDefinition4.m_171599_("collar_l", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-2.0f, -1.2f, -0.35f, 3.85f, 2.15f, 0.63f), PartPose.m_171423_((float)-2.2f, (float)1.2f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.22689f));
        partDefinition4.m_171599_("collar_r", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.85f, -1.2f, -0.35f, 3.85f, 2.15f, 0.63f), PartPose.m_171423_((float)2.2f, (float)1.2f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.22689f));
        partDefinition4.m_171599_("back_leaf_l_0", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.65f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)-2.0f, (float)10.0f, (float)3.24f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition4.m_171599_("back_leaf_r_0", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.55f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)2.0f, (float)10.0f, (float)3.24f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition4.m_171599_("back_leaf_l_1", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.65f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)-2.0f, (float)7.3f, (float)3.24f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition4.m_171599_("back_leaf_r_1", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.55f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)2.0f, (float)7.3f, (float)3.24f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition4.m_171599_("back_leaf_l_2", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.65f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)-2.0f, (float)4.6f, (float)3.24f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition4.m_171599_("back_leaf_r_2", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.55f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)2.0f, (float)4.6f, (float)3.24f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition4.m_171599_("back_leaf_l_3", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.65f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)-2.0f, (float)1.9f, (float)3.24f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition4.m_171599_("back_leaf_r_3", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.55f, -0.48f, -0.24f, 3.2f, 0.96f, 0.48f), PartPose.m_171423_((float)2.0f, (float)1.9f, (float)3.24f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition4.m_171599_("vine_l", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.35f, -4.0f, -0.22f, 0.67f, 8.0f, 0.42f), PartPose.m_171423_((float)-3.5f, (float)7.2f, (float)-3.4f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition4.m_171599_("vine_r", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.32f, -4.0f, -0.22f, 0.67f, 8.0f, 0.42f), PartPose.m_171423_((float)3.5f, (float)7.2f, (float)-3.4f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition4.m_171599_("camellia_left", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.8f, -0.65f, -0.23f, 1.58f, 1.33f, 0.46f), PartPose.m_171423_((float)-1.2f, (float)4.0f, (float)-3.75f, (float)0.0f, (float)0.0f, (float)0.31416f));
        partDefinition4.m_171599_("camellia_right", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.78f, -0.65f, -0.23f, 1.58f, 1.33f, 0.46f), PartPose.m_171423_((float)1.2f, (float)4.0f, (float)-3.75f, (float)0.0f, (float)0.0f, (float)-0.31416f));
        PartDefinition partDefinition5 = partDefinition.m_171599_("right_arm", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-4.0f, -1.35f, -3.25f, 5.35f, 6.35f, 6.5f).m_171514_(64, 0).m_171481_(-3.55f, 4.7f, -2.72f, 5.07f, 6.0f, 5.44f).m_171514_(64, 18).m_171481_(-3.8f, 9.25f, -3.0f, 5.5f, 2.1f, 6.0f).m_171514_(0, 32).m_171481_(-2.2f, 5.0f, -3.2f, 2.2f, 5.0f, 0.48f), PartPose.m_171419_((float)-5.0f, (float)2.0f, (float)0.0f));
        partDefinition5.m_171599_("right_pauldron_crown", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-3.25f, -1.15f, -3.65f, 6.3f, 2.1f, 7.3f), PartPose.m_171423_((float)-1.4f, (float)-1.2f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.08727f));
        partDefinition5.m_171599_("right_pauldron_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.95f, -2.65f, -2.8f, 1.8f, 5.25f, 5.6f), PartPose.m_171423_((float)-4.2f, (float)1.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition5.m_171599_("right_pauldron_front_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-2.2f, -1.6f, -0.32f, 4.4f, 3.2f, 0.64f), PartPose.m_171423_((float)-1.4f, (float)1.4f, (float)-3.4f, (float)0.0f, (float)0.0f, (float)0.15708f));
        partDefinition5.m_171599_("right_pauldron_back_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-2.2f, -1.6f, -0.32f, 4.4f, 3.2f, 0.64f), PartPose.m_171423_((float)-1.4f, (float)1.4f, (float)3.4f, (float)0.0f, (float)0.0f, (float)-0.15708f));
        partDefinition5.m_171599_("right_arm_vine", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.32f, -3.5f, -0.22f, 0.57f, 6.8f, 0.37f), PartPose.m_171423_((float)-3.4f, (float)6.7f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition5.m_171599_("right_shoulder_thorn_up", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.4f, -3.4f, -0.4f, 0.75f, 3.8f, 0.8f), PartPose.m_171423_((float)-4.7f, (float)-1.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.41888f));
        partDefinition5.m_171599_("right_shoulder_thorn_front", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.4f, -0.4f, -2.8f, 0.75f, 0.8f, 3.0f), PartPose.m_171423_((float)-3.9f, (float)1.5f, (float)-2.8f, (float)0.48869f, (float)0.0f, (float)0.0f));
        partDefinition5.m_171599_("right_shoulder_thorn_back", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.4f, -0.4f, -0.2f, 0.75f, 0.8f, 3.0f), PartPose.m_171423_((float)-3.9f, (float)1.5f, (float)2.8f, (float)-0.48869f, (float)0.0f, (float)0.0f));
        partDefinition5.m_171599_("right_cuff_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.45f, -2.4f, -0.35f, 0.65f, 2.9f, 0.7f), PartPose.m_171423_((float)-3.9f, (float)10.4f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.55851f));
        PartDefinition partDefinition6 = partDefinition.m_171599_("left_arm", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.35f, -1.35f, -3.25f, 5.35f, 6.35f, 6.5f).m_171514_(64, 0).m_171481_(-1.52f, 4.7f, -2.72f, 5.07f, 6.0f, 5.44f).m_171514_(64, 18).m_171481_(-1.7f, 9.25f, -3.0f, 5.5f, 2.1f, 6.0f).m_171514_(0, 32).m_171481_(0.0f, 5.0f, -3.2f, 2.2f, 5.0f, 0.48f), PartPose.m_171419_((float)5.0f, (float)2.0f, (float)0.0f));
        partDefinition6.m_171599_("left_pauldron_crown", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-3.05f, -1.15f, -3.65f, 6.3f, 2.1f, 7.3f), PartPose.m_171423_((float)1.4f, (float)-1.2f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.08727f));
        partDefinition6.m_171599_("left_pauldron_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.85f, -2.65f, -2.8f, 1.8f, 5.25f, 5.6f), PartPose.m_171423_((float)4.2f, (float)1.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.17453f));
        partDefinition6.m_171599_("left_pauldron_front_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-2.2f, -1.6f, -0.32f, 4.4f, 3.2f, 0.64f), PartPose.m_171423_((float)1.4f, (float)1.4f, (float)-3.4f, (float)0.0f, (float)0.0f, (float)-0.15708f));
        partDefinition6.m_171599_("left_pauldron_back_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-2.2f, -1.6f, -0.32f, 4.4f, 3.2f, 0.64f), PartPose.m_171423_((float)1.4f, (float)1.4f, (float)3.4f, (float)0.0f, (float)0.0f, (float)0.15708f));
        partDefinition6.m_171599_("left_arm_vine", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.25f, -3.5f, -0.22f, 0.57f, 6.8f, 0.37f), PartPose.m_171423_((float)3.4f, (float)6.7f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition6.m_171599_("left_shoulder_thorn_up", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.35f, -3.4f, -0.4f, 0.75f, 3.8f, 0.8f), PartPose.m_171423_((float)4.7f, (float)-1.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.41888f));
        partDefinition6.m_171599_("left_shoulder_thorn_front", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.35f, -0.4f, -2.8f, 0.75f, 0.8f, 3.0f), PartPose.m_171423_((float)3.9f, (float)1.5f, (float)-2.8f, (float)0.48869f, (float)0.0f, (float)0.0f));
        partDefinition6.m_171599_("left_shoulder_thorn_back", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.35f, -0.4f, -0.2f, 0.75f, 0.8f, 3.0f), PartPose.m_171423_((float)3.9f, (float)1.5f, (float)2.8f, (float)-0.48869f, (float)0.0f, (float)0.0f));
        partDefinition6.m_171599_("left_cuff_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.2f, -2.4f, -0.35f, 0.65f, 2.9f, 0.7f), PartPose.m_171423_((float)3.9f, (float)10.4f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.55851f));
        PartDefinition partDefinition7 = partDefinition.m_171599_("right_leg", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)-1.9f, (float)12.0f, (float)0.0f));
        PartDefinition partDefinition8 = partDefinition.m_171599_("left_leg", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)1.9f, (float)12.0f, (float)0.0f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private static LayerDefinition createLegsLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("head", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition3 = partDefinition.m_171599_("hat", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition4 = partDefinition.m_171599_("body", CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(-4.85f, 10.9f, -2.85f, 9.7f, 2.75f, 5.7f).m_171514_(0, 32).m_171481_(-4.45f, 11.85f, -3.28f, 8.9f, 0.8f, 0.43f).m_171514_(16, 48).m_171481_(-3.25f, 12.8f, -3.15f, 6.5f, 3.5f, 0.53f).m_171514_(16, 60).m_171481_(-2.65f, 15.7f, -3.05f, 5.3f, 2.65f, 0.51f).m_171514_(16, 70).m_171481_(-1.85f, 17.75f, -2.96f, 3.7f, 2.15f, 0.48f).m_171514_(64, 32).m_171481_(-0.5f, 12.3f, -3.75f, 1.0f, 1.0f, 0.45f).m_171514_(32, 32).m_171481_(-0.55f, 11.15f, -3.72f, 1.1f, 1.3f, 0.4f).m_171514_(32, 32).m_171481_(-0.55f, 13.15f, -3.72f, 1.1f, 1.3f, 0.4f), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        partDefinition4.m_171599_("hip_fauld_r", CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(-1.25f, -1.0f, -2.72f, 2.0f, 5.5f, 5.44f), PartPose.m_171423_((float)-4.0f, (float)13.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition4.m_171599_("hip_fauld_l", CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(-0.75f, -1.0f, -2.72f, 2.0f, 5.5f, 5.44f), PartPose.m_171423_((float)4.0f, (float)13.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.17453f));
        partDefinition4.m_171599_("girdle_flower_left", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.6f, -0.55f, -0.22f, 1.2f, 1.1f, 0.4f), PartPose.m_171423_((float)-0.95f, (float)12.8f, (float)-3.5f, (float)0.0f, (float)0.0f, (float)0.31416f));
        partDefinition4.m_171599_("girdle_flower_right", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.6f, -0.55f, -0.22f, 1.2f, 1.1f, 0.4f), PartPose.m_171423_((float)0.95f, (float)12.8f, (float)-3.5f, (float)0.0f, (float)0.0f, (float)-0.31416f));
        PartDefinition partDefinition5 = partDefinition.m_171599_("right_arm", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)-5.0f, (float)2.0f, (float)0.0f));
        PartDefinition partDefinition6 = partDefinition.m_171599_("left_arm", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)5.0f, (float)2.0f, (float)0.0f));
        PartDefinition partDefinition7 = partDefinition.m_171599_("right_leg", CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(-2.35f, -0.35f, -2.48f, 4.6f, 8.6f, 4.96f).m_171514_(16, 48).m_171481_(-2.15f, 0.25f, -2.95f, 4.2f, 4.75f, 0.53f).m_171514_(24, 48).m_171481_(-2.55f, 5.35f, -3.15f, 5.0f, 3.35f, 5.95f), PartPose.m_171419_((float)-1.9f, (float)12.0f, (float)0.0f));
        partDefinition7.m_171599_("right_legging_thigh_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.6f, -2.5f, -2.35f, 1.05f, 5.2f, 4.7f), PartPose.m_171423_((float)-2.55f, (float)3.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition7.m_171599_("right_legging_knee_beak", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.55f, -1.0f, -1.0f, 3.05f, 1.9f, 1.1f), PartPose.m_171423_((float)0.0f, (float)7.5f, (float)-3.1f, (float)0.20944f, (float)0.0f, (float)0.0f));
        partDefinition7.m_171599_("right_legging_leg_vine_a", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.28f, -2.7f, -0.25f, 0.53f, 5.2f, 0.37f), PartPose.m_171423_((float)-2.3f, (float)3.8f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.24435f));
        partDefinition7.m_171599_("right_legging_leg_vine_b", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.2f, -2.5f, -0.2f, 0.52f, 4.7f, 0.35f), PartPose.m_171423_((float)2.0f, (float)3.3f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition7.m_171599_("right_legging_knee_thorn_outer", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.45f, -0.35f, -0.35f, 1.6f, 0.7f, 0.7f), PartPose.m_171423_((float)-2.5f, (float)7.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.43633f));
        PartDefinition partDefinition8 = partDefinition.m_171599_("left_leg", CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(-2.25f, -0.35f, -2.48f, 4.6f, 8.6f, 4.96f).m_171514_(16, 48).m_171481_(-2.05f, 0.25f, -2.95f, 4.2f, 4.75f, 0.53f).m_171514_(24, 48).m_171481_(-2.45f, 5.35f, -3.15f, 5.0f, 3.35f, 5.95f), PartPose.m_171419_((float)1.9f, (float)12.0f, (float)0.0f));
        partDefinition8.m_171599_("left_legging_thigh_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.45f, -2.5f, -2.35f, 1.05f, 5.2f, 4.7f), PartPose.m_171423_((float)2.55f, (float)3.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition8.m_171599_("left_legging_knee_beak", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.5f, -1.0f, -1.0f, 3.05f, 1.9f, 1.1f), PartPose.m_171423_((float)0.0f, (float)7.5f, (float)-3.1f, (float)0.20944f, (float)0.0f, (float)0.0f));
        partDefinition8.m_171599_("left_legging_leg_vine_a", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.25f, -2.7f, -0.25f, 0.53f, 5.2f, 0.37f), PartPose.m_171423_((float)2.3f, (float)3.8f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.24435f));
        partDefinition8.m_171599_("left_legging_leg_vine_b", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.32f, -2.5f, -0.2f, 0.52f, 4.7f, 0.35f), PartPose.m_171423_((float)-2.0f, (float)3.3f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition8.m_171599_("left_legging_knee_thorn_outer", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.15f, -0.35f, -0.35f, 1.6f, 0.7f, 0.7f), PartPose.m_171423_((float)2.5f, (float)7.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.43633f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private static LayerDefinition createFeetLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("head", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition3 = partDefinition.m_171599_("hat", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition4 = partDefinition.m_171599_("body", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition partDefinition5 = partDefinition.m_171599_("right_arm", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)-5.0f, (float)2.0f, (float)0.0f));
        PartDefinition partDefinition6 = partDefinition.m_171599_("left_arm", CubeListBuilder.m_171558_(), PartPose.m_171419_((float)5.0f, (float)2.0f, (float)0.0f));
        PartDefinition partDefinition7 = partDefinition.m_171599_("right_leg", CubeListBuilder.m_171558_().m_171514_(48, 48).m_171481_(-2.35f, 6.3f, -2.62f, 4.6f, 5.8f, 5.24f).m_171514_(72, 48).m_171481_(-2.72f, 5.65f, -3.02f, 5.34f, 2.1f, 6.04f).m_171514_(48, 64).m_171481_(-2.45f, 9.3f, -4.05f, 4.8f, 2.9f, 6.8f).m_171514_(72, 64).m_171481_(-2.25f, 8.85f, 2.45f, 4.4f, 3.3f, 1.1f).m_171514_(48, 48).m_171481_(-2.12f, 4.55f, -3.12f, 4.14f, 4.95f, 0.57f), PartPose.m_171419_((float)-1.9f, (float)12.0f, (float)0.0f));
        partDefinition7.m_171599_("right_boot_toe_ridge", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.55f, -0.65f, -0.58f, 3.1f, 1.25f, 0.76f), PartPose.m_171423_((float)0.0f, (float)10.4f, (float)-4.0f, (float)0.17453f, (float)0.0f, (float)0.0f));
        partDefinition7.m_171599_("right_boot_cuff_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.8f, -1.95f, -2.4f, 1.05f, 3.0f, 4.8f), PartPose.m_171423_((float)-2.5f, (float)6.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.2618f));
        partDefinition7.m_171599_("right_boot_cuff_inner_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.15f, -1.55f, -2.4f, 1.0f, 2.5f, 4.8f), PartPose.m_171423_((float)2.0f, (float)6.4f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition7.m_171599_("right_boot_boot_vine_outer", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.28f, -2.25f, -0.18f, 0.53f, 4.35f, 0.41f), PartPose.m_171423_((float)-2.3f, (float)9.1f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.2618f));
        partDefinition7.m_171599_("right_boot_boot_vine_inner", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.2f, -2.2f, -0.18f, 0.51f, 4.3f, 0.41f), PartPose.m_171423_((float)2.02f, (float)9.4f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition7.m_171599_("right_boot_heel_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.4f, -0.35f, -0.15f, 0.75f, 0.75f, 2.8f), PartPose.m_171423_((float)-0.7f, (float)10.3f, (float)3.35f, (float)-0.48869f, (float)0.0f, (float)0.0f));
        partDefinition7.m_171599_("right_boot_outer_spur", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.85f, -0.35f, -0.35f, 2.05f, 0.7f, 0.7f), PartPose.m_171423_((float)-2.45f, (float)10.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.48869f));
        partDefinition7.m_171599_("right_boot_cuff_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.55f, -2.75f, -0.35f, 0.75f, 3.0f, 0.7f), PartPose.m_171423_((float)-2.75f, (float)5.65f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.43633f));
        PartDefinition partDefinition8 = partDefinition.m_171599_("left_leg", CubeListBuilder.m_171558_().m_171514_(48, 48).m_171481_(-2.25f, 6.3f, -2.62f, 4.6f, 5.8f, 5.24f).m_171514_(72, 48).m_171481_(-2.62f, 5.65f, -3.02f, 5.34f, 2.1f, 6.04f).m_171514_(48, 64).m_171481_(-2.35f, 9.3f, -4.05f, 4.8f, 2.9f, 6.8f).m_171514_(72, 64).m_171481_(-2.15f, 8.85f, 2.45f, 4.4f, 3.3f, 1.1f).m_171514_(48, 48).m_171481_(-2.02f, 4.55f, -3.12f, 4.14f, 4.95f, 0.57f), PartPose.m_171419_((float)1.9f, (float)12.0f, (float)0.0f));
        partDefinition8.m_171599_("left_boot_toe_ridge", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-1.55f, -0.65f, -0.58f, 3.1f, 1.25f, 0.76f), PartPose.m_171423_((float)0.0f, (float)10.4f, (float)-4.0f, (float)0.17453f, (float)0.0f, (float)0.0f));
        partDefinition8.m_171599_("left_boot_cuff_outer_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.25f, -1.95f, -2.4f, 1.05f, 3.0f, 4.8f), PartPose.m_171423_((float)2.5f, (float)6.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.2618f));
        partDefinition8.m_171599_("left_boot_cuff_inner_leaf", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.85f, -1.55f, -2.4f, 1.0f, 2.5f, 4.8f), PartPose.m_171423_((float)-2.0f, (float)6.4f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition8.m_171599_("left_boot_boot_vine_outer", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.25f, -2.25f, -0.18f, 0.53f, 4.35f, 0.41f), PartPose.m_171423_((float)2.3f, (float)9.1f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)0.2618f));
        partDefinition8.m_171599_("left_boot_boot_vine_inner", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.31f, -2.2f, -0.18f, 0.51f, 4.3f, 0.41f), PartPose.m_171423_((float)-2.02f, (float)9.4f, (float)-3.0f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition8.m_171599_("left_boot_heel_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.35f, -0.35f, -0.15f, 0.75f, 0.75f, 2.8f), PartPose.m_171423_((float)0.7f, (float)10.3f, (float)3.35f, (float)-0.48869f, (float)0.0f, (float)0.0f));
        partDefinition8.m_171599_("left_boot_outer_spur", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.2f, -0.35f, -0.35f, 2.05f, 0.7f, 0.7f), PartPose.m_171423_((float)2.45f, (float)10.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.48869f));
        partDefinition8.m_171599_("left_boot_cuff_thorn", CubeListBuilder.m_171558_().m_171514_(96, 48).m_171481_(-0.2f, -2.75f, -0.35f, 0.75f, 3.0f, 0.7f), PartPose.m_171423_((float)2.75f, (float)5.65f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.43633f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }
}

