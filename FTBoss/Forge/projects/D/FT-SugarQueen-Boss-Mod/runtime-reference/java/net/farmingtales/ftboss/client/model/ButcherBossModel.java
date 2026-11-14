/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.ArmedModel
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.butcherskills.ButcherSkillMotion;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ButcherBossModel<T extends MeatMountainEntity>
extends EntityModel<T>
implements ArmedModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation("ftboss", "butcher"), "main");
    public static final float HAND_OFFSET_Y = -0.375f;
    public static final float WEAPON_SCALE = 1.15f;
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart apron_l;
    private final ModelPart apron_r;
    private final ModelPart arm_l;
    private final ModelPart forearm_l;
    private final ModelPart hand_l;
    private final ModelPart arm_r;
    private final ModelPart forearm_r;
    private final ModelPart hand_r;
    private final ModelPart leg_l;
    private final ModelPart shin_l;
    private final ModelPart boot_l;
    private final ModelPart leg_r;
    private final ModelPart shin_r;
    private final ModelPart boot_r;
    private static final float[][] CHOP_BODY = new float[][]{{0.0f, -4.0f, -12.0f, 0.0f}, {1.0f, 16.0f, 10.0f, 0.0f}, {2.0f, 14.0f, 10.0f, 0.0f}, {4.0f, 4.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_HEAD = new float[][]{{0.0f, 4.0f, 10.0f, 0.0f}, {1.0f, -12.0f, 0.0f, 0.0f}, {2.0f, -10.0f, 0.0f, 0.0f}, {4.0f, -4.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_ARM_R = new float[][]{{0.0f, -145.0f, -15.0f, 15.0f}, {1.0f, -20.0f, -10.0f, 12.0f}, {2.0f, -20.0f, -10.0f, 12.0f}, {4.0f, -12.0f, -8.0f, 8.0f}};
    private static final float[][] CHOP_FOREARM_R = new float[][]{{0.0f, -30.0f, 0.0f, 0.0f}, {1.0f, -15.0f, 0.0f, 0.0f}, {2.0f, -15.0f, 0.0f, 0.0f}, {4.0f, -18.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_ARM_L = new float[][]{{0.0f, -35.0f, 0.0f, -22.0f}, {1.0f, -15.0f, 0.0f, -20.0f}, {2.0f, 4.0f, 0.0f, -8.0f}, {4.0f, 4.0f, 0.0f, -8.0f}};
    private static final float[][] CHOP_HAND_R = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f}, {1.0f, 70.0f, 0.0f, 0.0f}, {2.0f, 65.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_APRON_R = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f}, {1.0f, -20.0f, 0.0f, 0.0f}, {2.0f, 0.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_APRON_L = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f}, {1.0f, -20.0f, 0.0f, 0.0f}, {2.0f, 0.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] CHOP_FOREARM_L = new float[][]{{0.0f, -8.0f, 0.0f, 0.0f}, {1.0f, -8.0f, 0.0f, 0.0f}, {2.0f, -8.0f, 0.0f, 0.0f}, {4.0f, -8.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_BODY = new float[][]{{0.0f, 4.0f, -22.0f, 0.0f}, {1.0f, 8.0f, 25.0f, 0.0f}, {2.0f, 8.0f, 25.0f, 0.0f}, {4.0f, 4.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_ARM_R = new float[][]{{0.0f, -62.0f, -65.0f, 28.0f}, {1.0f, -65.0f, 30.0f, 8.0f}, {2.0f, -65.0f, 30.0f, 8.0f}, {4.0f, -12.0f, -8.0f, 8.0f}};
    private static final float[][] BACKHAND_FOREARM_R = new float[][]{{0.0f, -20.0f, 0.0f, 0.0f}, {1.0f, -15.0f, 0.0f, 0.0f}, {2.0f, -15.0f, 0.0f, 0.0f}, {4.0f, -18.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_HAND_R = new float[][]{{0.0f, 90.0f, 0.0f, 0.0f}, {1.0f, 90.0f, 0.0f, 0.0f}, {2.0f, 90.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_ARM_L = new float[][]{{0.0f, -18.0f, 0.0f, -25.0f}, {1.0f, -15.0f, 0.0f, -28.0f}, {2.0f, 4.0f, 0.0f, -8.0f}, {4.0f, 4.0f, 0.0f, -8.0f}};
    private static final float[][] BACKHAND_HEAD = new float[][]{{0.0f, -4.0f, 18.0f, 0.0f}, {1.0f, -6.0f, -12.0f, 0.0f}, {2.0f, -4.0f, 0.0f, 0.0f}, {4.0f, -4.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_APRON_L = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f}, {1.0f, -12.0f, 0.0f, 6.0f}, {2.0f, 0.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_APRON_R = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f}, {1.0f, -12.0f, 0.0f, 6.0f}, {2.0f, 0.0f, 0.0f, 0.0f}, {4.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] BACKHAND_FOREARM_L = new float[][]{{0.0f, -8.0f, 0.0f, 0.0f}, {1.0f, -8.0f, 0.0f, 0.0f}, {2.0f, -8.0f, 0.0f, 0.0f}, {4.0f, -8.0f, 0.0f, 0.0f}};

    public ButcherBossModel(ModelPart root) {
        this.root = root;
        this.body = this.root.m_171324_("body");
        this.head = this.body.m_171324_("head");
        this.apron_l = this.body.m_171324_("apron_l");
        this.apron_r = this.body.m_171324_("apron_r");
        this.arm_l = this.body.m_171324_("arm_l");
        this.forearm_l = this.arm_l.m_171324_("forearm_l");
        this.hand_l = this.forearm_l.m_171324_("hand_l");
        this.arm_r = this.body.m_171324_("arm_r");
        this.forearm_r = this.arm_r.m_171324_("forearm_r");
        this.hand_r = this.forearm_r.m_171324_("hand_r");
        this.leg_l = this.root.m_171324_("leg_l");
        this.shin_l = this.leg_l.m_171324_("shin_l");
        this.boot_l = this.shin_l.m_171324_("boot_l");
        this.leg_r = this.root.m_171324_("leg_r");
        this.shin_r = this.leg_r.m_171324_("shin_r");
        this.boot_r = this.shin_r.m_171324_("boot_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.m_171576_();
        PartDefinition body = root.m_171599_("body", CubeListBuilder.m_171558_().m_171514_(1, 1).m_171481_(-9.0f, -7.0f, -6.0f, 18.0f, 12.0f, 12.0f).m_171514_(63, 1).m_171481_(-8.5f, -1.0f, -7.0f, 17.0f, 8.0f, 14.0f).m_171514_(127, 1).m_171481_(-9.5f, 5.0f, -7.5f, 19.0f, 2.0f, 15.0f).m_171514_(197, 1).m_171481_(-2.0f, 5.3f, -8.2f, 4.0f, 2.0f, 1.0f).m_171514_(209, 1).m_171481_(-6.0f, -5.5f, -6.8f, 12.0f, 7.0f, 1.0f).m_171514_(1, 27).m_171481_(-7.0f, 0.0f, -7.9f, 14.0f, 6.0f, 1.0f).m_171514_(33, 27).m_171481_(-6.5f, -7.6f, -6.5f, 2.0f, 9.0f, 1.0f).m_171514_(41, 27).m_171481_(4.5f, -7.6f, -6.5f, 2.0f, 9.0f, 1.0f).m_171514_(49, 27).m_171481_(-5.7f, -6.7f, -7.0f, 1.0f, 1.0f, 1.0f).m_171514_(55, 27).m_171481_(4.7f, -6.7f, -7.0f, 1.0f, 1.0f, 1.0f).m_171514_(61, 27).m_171481_(-3.5f, -8.0f, -3.5f, 7.0f, 3.0f, 7.0f).m_171514_(91, 27).m_171481_(-6.0f, -6.0f, 6.4f, 2.0f, 12.0f, 1.0f).m_171514_(99, 27).m_171481_(4.0f, -6.0f, 6.4f, 2.0f, 12.0f, 1.0f), PartPose.m_171419_((float)0.0f, (float)3.0f, (float)0.0f));
        PartDefinition head = body.m_171599_("head", CubeListBuilder.m_171558_().m_171514_(107, 27).m_171481_(-5.0f, -9.0f, -5.0f, 10.0f, 10.0f, 10.0f).m_171514_(149, 27).m_171481_(-4.5f, -2.3f, -5.8f, 9.0f, 3.0f, 3.0f).m_171514_(175, 27).m_171481_(-1.5f, -4.8f, -6.3f, 3.0f, 3.0f, 2.0f).m_171514_(187, 27).m_171481_(-5.8f, -5.0f, -1.5f, 2.0f, 3.0f, 3.0f).m_171514_(199, 27).m_171481_(3.8f, -5.0f, -1.5f, 2.0f, 3.0f, 3.0f).m_171514_(211, 27).m_171481_(-5.5f, -9.8f, -5.5f, 11.0f, 2.0f, 11.0f).m_171514_(1, 49).m_171481_(-4.7f, -13.0f, -4.4f, 10.0f, 4.0f, 9.0f).m_171514_(41, 49).m_171481_(-3.7f, -14.0f, -3.9f, 8.0f, 2.0f, 8.0f).m_171514_(75, 49).m_171481_(-5.4f, -8.8f, -6.6f, 11.0f, 1.0f, 2.0f), PartPose.m_171419_((float)0.0f, (float)-8.0f, (float)-0.7f));
        PartDefinition apron_l = body.m_171599_("apron_l", CubeListBuilder.m_171558_().m_171514_(103, 49).m_171481_(-3.0f, 0.0f, -0.6f, 6.0f, 7.0f, 1.0f), PartPose.m_171419_((float)3.4f, (float)6.0f, (float)-8.1f));
        PartDefinition apron_r = body.m_171599_("apron_r", CubeListBuilder.m_171558_().m_171514_(119, 49).m_171481_(-3.0f, 0.0f, -0.6f, 6.0f, 7.0f, 1.0f), PartPose.m_171419_((float)-3.4f, (float)6.0f, (float)-8.1f));
        PartDefinition arm_l = body.m_171599_("arm_l", CubeListBuilder.m_171558_().m_171514_(135, 49).m_171481_(-2.6f, -1.5f, -3.5f, 6.0f, 8.0f, 7.0f).m_171514_(163, 49).m_171481_(-2.9f, 4.0f, -3.8f, 6.0f, 2.0f, 8.0f), PartPose.m_171419_((float)11.5f, (float)-6.0f, (float)0.0f));
        PartDefinition forearm_l = arm_l.m_171599_("forearm_l", CubeListBuilder.m_171558_().m_171514_(193, 49).m_171481_(-2.5f, -1.0f, -3.0f, 5.0f, 8.0f, 6.0f).m_171514_(217, 49).m_171481_(-2.7f, 4.7f, -3.2f, 5.0f, 2.0f, 7.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition hand_l = forearm_l.m_171599_("hand_l", CubeListBuilder.m_171558_().m_171514_(1, 66).m_171481_(-2.8f, -0.4f, -3.3f, 6.0f, 4.0f, 7.0f).m_171514_(29, 66).m_171481_(-3.5f, 0.3f, -2.4f, 2.0f, 3.0f, 3.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition arm_r = body.m_171599_("arm_r", CubeListBuilder.m_171558_().m_171514_(41, 66).m_171481_(-3.4f, -1.5f, -3.5f, 6.0f, 4.0f, 7.0f).m_171514_(69, 66).m_171481_(-3.0f, 1.0f, -3.2f, 6.0f, 6.0f, 6.0f), PartPose.m_171419_((float)-11.5f, (float)-6.0f, (float)0.0f));
        PartDefinition forearm_r = arm_r.m_171599_("forearm_r", CubeListBuilder.m_171558_().m_171514_(95, 66).m_171481_(-3.0f, -1.0f, -3.4f, 6.0f, 8.0f, 7.0f).m_171514_(123, 66).m_171481_(-3.3f, 3.0f, -3.7f, 7.0f, 2.0f, 8.0f).m_171514_(155, 66).m_171481_(-3.1f, 5.2f, -3.5f, 6.0f, 1.0f, 7.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition hand_r = forearm_r.m_171599_("hand_r", CubeListBuilder.m_171558_().m_171514_(183, 66).m_171481_(-3.2f, -0.4f, -3.3f, 6.0f, 4.0f, 7.0f).m_171514_(211, 66).m_171481_(1.5f, 0.3f, -2.4f, 2.0f, 3.0f, 3.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition leg_l = root.m_171599_("leg_l", CubeListBuilder.m_171558_().m_171514_(223, 66).m_171481_(-3.5f, -1.0f, -3.5f, 7.0f, 8.0f, 7.0f), PartPose.m_171419_((float)4.8f, (float)10.0f, (float)0.7f));
        PartDefinition shin_l = leg_l.m_171599_("shin_l", CubeListBuilder.m_171558_().m_171514_(1, 83).m_171481_(-3.2f, -1.0f, -3.2f, 6.0f, 6.0f, 6.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition boot_l = shin_l.m_171599_("boot_l", CubeListBuilder.m_171558_().m_171514_(27, 83).m_171481_(-3.8f, -0.6f, -4.8f, 8.0f, 3.0f, 9.0f).m_171514_(63, 83).m_171481_(-4.0f, 2.0f, -5.0f, 8.0f, 1.0f, 10.0f).m_171514_(101, 83).m_171481_(-3.6f, -0.4f, -5.0f, 7.0f, 1.0f, 1.0f), PartPose.m_171419_((float)0.0f, (float)5.0f, (float)0.0f));
        PartDefinition leg_r = root.m_171599_("leg_r", CubeListBuilder.m_171558_().m_171514_(119, 83).m_171481_(-3.5f, -1.0f, -3.5f, 7.0f, 8.0f, 7.0f), PartPose.m_171419_((float)-4.8f, (float)10.0f, (float)0.7f));
        PartDefinition shin_r = leg_r.m_171599_("shin_r", CubeListBuilder.m_171558_().m_171514_(149, 83).m_171481_(-2.8f, -1.0f, -3.2f, 6.0f, 6.0f, 6.0f), PartPose.m_171419_((float)0.0f, (float)6.0f, (float)0.0f));
        PartDefinition boot_r = shin_r.m_171599_("boot_r", CubeListBuilder.m_171558_().m_171514_(175, 83).m_171481_(-4.2f, -0.6f, -4.8f, 8.0f, 3.0f, 9.0f).m_171514_(211, 83).m_171481_(-4.0f, 2.0f, -5.0f, 8.0f, 1.0f, 10.0f).m_171514_(1, 100).m_171481_(-3.4f, -0.4f, -5.0f, 7.0f, 1.0f, 1.0f), PartPose.m_171419_((float)0.0f, (float)5.0f, (float)0.0f));
        return LayerDefinition.m_171565_((MeshDefinition)mesh, (int)256, (int)256);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float age;
        this.root.m_171331_().forEach(ModelPart::m_233569_);
        float stride = limbSwing * 0.62f;
        float amplitude = Math.min(limbSwingAmount, 0.6f);
        this.leg_l.f_104203_ = Mth.m_14089_((float)stride) * amplitude * 0.8f;
        this.leg_r.f_104203_ = -this.leg_l.f_104203_;
        this.shin_l.f_104203_ = -Math.max(0.0f, this.leg_l.f_104203_) * 0.6f;
        this.shin_r.f_104203_ = -Math.max(0.0f, this.leg_r.f_104203_) * 0.6f;
        this.apron_l.f_104203_ = -Math.max(0.0f, -this.leg_l.f_104203_) * 0.8f;
        this.apron_r.f_104203_ = -Math.max(0.0f, -this.leg_r.f_104203_) * 0.8f;
        this.body.f_104203_ = 0.0698f;
        this.body.f_104204_ = 0.0f;
        this.body.f_104205_ = 0.0f;
        this.head.f_104203_ = -0.0698f;
        this.head.f_104204_ = 0.0f;
        this.head.f_104205_ = 0.0f;
        this.arm_r.f_104203_ = -0.2094f;
        this.arm_r.f_104204_ = -0.1396f;
        this.arm_r.f_104205_ = 0.1396f;
        this.forearm_r.f_104203_ = -0.3142f;
        this.forearm_r.f_104204_ = 0.0f;
        this.forearm_r.f_104205_ = 0.0f;
        this.hand_r.f_104203_ = 0.0f;
        this.hand_r.f_104204_ = 0.0f;
        this.hand_r.f_104205_ = 0.0f;
        this.arm_l.f_104203_ = 0.0698f;
        this.arm_l.f_104204_ = 0.0f;
        this.arm_l.f_104205_ = -0.1396f;
        this.forearm_l.f_104203_ = -0.1396f;
        this.forearm_l.f_104204_ = 0.0f;
        this.forearm_l.f_104205_ = 0.0f;
        this.body.f_104203_ += Mth.m_14031_((float)(ageInTicks * 0.08f)) * 0.012f;
        this.arm_l.f_104203_ -= this.leg_l.f_104203_ * 0.6f;
        this.head.f_104204_ = Mth.m_14036_((float)netHeadYaw, (float)-40.0f, (float)40.0f) * ((float)Math.PI / 180);
        this.head.f_104203_ += Mth.m_14036_((float)headPitch, (float)-25.0f, (float)25.0f) * ((float)Math.PI / 180);
        String action = ((MeatMountainEntity)((Object)entity)).getButcherAction();
        float f = age = ((MeatMountainEntity)((Object)entity)).getButcherActionAge(ageInTicks - (float)((MeatMountainEntity)((Object)entity)).f_19797_);
        String string = action;
        this.applyAction(string, f);
        ButcherSkillMotion.finish(string, f, this.body);
    }

    /*
     * WARNING - void declaration
     */
    private void applyAction(String string, float f) {
        String action;
        f = ButcherSkillMotion.age(string, f);
        switch (action = ButcherSkillMotion.name(string)) {
            case "chop": {
                void age;
                ButcherBossModel.animate(this.body, (float)age, CHOP_BODY);
                ButcherBossModel.animate(this.head, (float)age, CHOP_HEAD);
                ButcherBossModel.animate(this.arm_r, (float)age, CHOP_ARM_R);
                ButcherBossModel.animate(this.forearm_r, (float)age, CHOP_FOREARM_R);
                ButcherBossModel.animate(this.arm_l, (float)age, CHOP_ARM_L);
                ButcherBossModel.animate(this.hand_r, (float)age, CHOP_HAND_R);
                ButcherBossModel.animate(this.apron_r, (float)age, CHOP_APRON_R);
                ButcherBossModel.animate(this.apron_l, (float)age, CHOP_APRON_L);
                ButcherBossModel.animate(this.forearm_l, (float)age, CHOP_FOREARM_L);
                break;
            }
            case "backhand": {
                void age;
                ButcherBossModel.animate(this.body, (float)age, BACKHAND_BODY);
                ButcherBossModel.animate(this.arm_r, (float)age, BACKHAND_ARM_R);
                ButcherBossModel.animate(this.forearm_r, (float)age, BACKHAND_FOREARM_R);
                ButcherBossModel.animate(this.hand_r, (float)age, BACKHAND_HAND_R);
                ButcherBossModel.animate(this.arm_l, (float)age, BACKHAND_ARM_L);
                ButcherBossModel.animate(this.head, (float)age, BACKHAND_HEAD);
                ButcherBossModel.animate(this.apron_l, (float)age, BACKHAND_APRON_L);
                ButcherBossModel.animate(this.apron_r, (float)age, BACKHAND_APRON_R);
                ButcherBossModel.animate(this.forearm_l, (float)age, BACKHAND_FOREARM_L);
                break;
            }
        }
    }

    private static void animate(ModelPart part, float age, float[][] frames) {
        int index;
        for (index = 0; index < frames.length - 2 && age > frames[index + 1][0]; ++index) {
        }
        float[] from = frames[index];
        float[] to = frames[index + 1];
        float progress = Mth.m_14036_((float)((age - from[0]) / (to[0] - from[0])), (float)0.0f, (float)1.0f);
        progress = progress * progress * (3.0f - 2.0f * progress);
        part.f_104203_ = Mth.m_14179_((float)progress, (float)from[1], (float)to[1]) * ((float)Math.PI / 180);
        part.f_104204_ = Mth.m_14179_((float)progress, (float)from[2], (float)to[2]) * ((float)Math.PI / 180);
        part.f_104205_ = Mth.m_14179_((float)progress, (float)from[3], (float)to[3]) * ((float)Math.PI / 180);
    }

    public void m_7695_(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.m_104306_(pose, buffer, light, overlay, red, green, blue, alpha);
    }

    public void m_6002_(HumanoidArm arm, PoseStack pose) {
        this.root.m_104299_(pose);
        this.body.m_104299_(pose);
        boolean right = arm == HumanoidArm.RIGHT;
        (right ? this.arm_r : this.arm_l).m_104299_(pose);
        (right ? this.forearm_r : this.forearm_l).m_104299_(pose);
        (right ? this.hand_r : this.hand_l).m_104299_(pose);
        pose.m_252880_(0.0f, -0.375f, -0.05f);
        pose.m_85841_(1.15f, 1.15f, 1.15f);
    }

    public ModelPart root() {
        return this.root;
    }
}

