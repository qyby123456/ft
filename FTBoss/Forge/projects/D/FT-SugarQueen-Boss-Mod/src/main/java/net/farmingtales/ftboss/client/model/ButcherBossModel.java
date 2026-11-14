package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public class ButcherBossModel<T extends MeatMountainEntity> extends EntityModel<T> implements ArmedModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation("ftboss", "butcher"), "main");
    public static final float HAND_OFFSET_Y = -0.375F;
    public static final float WEAPON_SCALE = 1.15F;
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

    public ButcherBossModel(ModelPart root) {
        this.root = root;
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.apron_l = this.body.getChild("apron_l");
        this.apron_r = this.body.getChild("apron_r");
        this.arm_l = this.body.getChild("arm_l");
        this.forearm_l = this.arm_l.getChild("forearm_l");
        this.hand_l = this.forearm_l.getChild("hand_l");
        this.arm_r = this.body.getChild("arm_r");
        this.forearm_r = this.arm_r.getChild("forearm_r");
        this.hand_r = this.forearm_r.getChild("hand_r");
        this.leg_l = this.root.getChild("leg_l");
        this.shin_l = this.leg_l.getChild("shin_l");
        this.boot_l = this.shin_l.getChild("boot_l");
        this.leg_r = this.root.getChild("leg_r");
        this.shin_r = this.leg_r.getChild("shin_r");
        this.boot_r = this.shin_r.getChild("boot_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-9.0F, -7.0F, -6.0F, 18.0F, 12.0F, 12.0F)
                .texOffs(63, 1).addBox(-8.5F, -1.0F, -7.0F, 17.0F, 8.0F, 14.0F)
                .texOffs(127, 1).addBox(-9.5F, 5.0F, -7.5F, 19.0F, 2.0F, 15.0F)
                .texOffs(197, 1).addBox(-2.0F, 5.3F, -8.2F, 4.0F, 2.0F, 1.0F)
                .texOffs(209, 1).addBox(-6.0F, -5.5F, -6.8F, 12.0F, 7.0F, 1.0F)
                .texOffs(1, 27).addBox(-7.0F, 0.0F, -7.9F, 14.0F, 6.0F, 1.0F)
                .texOffs(33, 27).addBox(-6.5F, -7.6F, -6.5F, 2.0F, 9.0F, 1.0F)
                .texOffs(41, 27).addBox(4.5F, -7.6F, -6.5F, 2.0F, 9.0F, 1.0F)
                .texOffs(49, 27).addBox(-5.7F, -6.7F, -7.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(55, 27).addBox(4.7F, -6.7F, -7.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(61, 27).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 3.0F, 7.0F)
                .texOffs(91, 27).addBox(-6.0F, -6.0F, 6.4F, 2.0F, 12.0F, 1.0F)
                .texOffs(99, 27).addBox(4.0F, -6.0F, 6.4F, 2.0F, 12.0F, 1.0F)
                , PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(107, 27).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                .texOffs(149, 27).addBox(-4.5F, -2.3F, -5.8F, 9.0F, 3.0F, 3.0F)
                .texOffs(175, 27).addBox(-1.5F, -4.8F, -6.3F, 3.0F, 3.0F, 2.0F)
                .texOffs(187, 27).addBox(-5.8F, -5.0F, -1.5F, 2.0F, 3.0F, 3.0F)
                .texOffs(199, 27).addBox(3.8F, -5.0F, -1.5F, 2.0F, 3.0F, 3.0F)
                .texOffs(211, 27).addBox(-5.5F, -9.8F, -5.5F, 11.0F, 2.0F, 11.0F)
                .texOffs(1, 49).addBox(-4.7F, -13.0F, -4.4F, 10.0F, 4.0F, 9.0F)
                .texOffs(41, 49).addBox(-3.7F, -14.0F, -3.9F, 8.0F, 2.0F, 8.0F)
                .texOffs(75, 49).addBox(-5.4F, -8.8F, -6.6F, 11.0F, 1.0F, 2.0F)
                , PartPose.offset(0.0F, -8.0F, -0.7F));
        PartDefinition apron_l = body.addOrReplaceChild("apron_l", CubeListBuilder.create()
                .texOffs(103, 49).addBox(-3.0F, 0.0F, -0.6F, 6.0F, 7.0F, 1.0F)
                , PartPose.offset(3.4F, 6.0F, -8.1F));
        PartDefinition apron_r = body.addOrReplaceChild("apron_r", CubeListBuilder.create()
                .texOffs(119, 49).addBox(-3.0F, 0.0F, -0.6F, 6.0F, 7.0F, 1.0F)
                , PartPose.offset(-3.4F, 6.0F, -8.1F));
        PartDefinition arm_l = body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(135, 49).addBox(-2.6F, -1.5F, -3.5F, 6.0F, 8.0F, 7.0F)
                .texOffs(163, 49).addBox(-2.9F, 4.0F, -3.8F, 6.0F, 2.0F, 8.0F)
                , PartPose.offset(11.5F, -6.0F, 0.0F));
        PartDefinition forearm_l = arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(193, 49).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 8.0F, 6.0F)
                .texOffs(217, 49).addBox(-2.7F, 4.7F, -3.2F, 5.0F, 2.0F, 7.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition hand_l = forearm_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(1, 66).addBox(-2.8F, -0.4F, -3.3F, 6.0F, 4.0F, 7.0F)
                .texOffs(29, 66).addBox(-3.5F, 0.3F, -2.4F, 2.0F, 3.0F, 3.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition arm_r = body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(41, 66).addBox(-3.4F, -1.5F, -3.5F, 6.0F, 4.0F, 7.0F)
                .texOffs(69, 66).addBox(-3.0F, 1.0F, -3.2F, 6.0F, 6.0F, 6.0F)
                , PartPose.offset(-11.5F, -6.0F, 0.0F));
        PartDefinition forearm_r = arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(95, 66).addBox(-3.0F, -1.0F, -3.4F, 6.0F, 8.0F, 7.0F)
                .texOffs(123, 66).addBox(-3.3F, 3.0F, -3.7F, 7.0F, 2.0F, 8.0F)
                .texOffs(155, 66).addBox(-3.1F, 5.2F, -3.5F, 6.0F, 1.0F, 7.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition hand_r = forearm_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(183, 66).addBox(-3.2F, -0.4F, -3.3F, 6.0F, 4.0F, 7.0F)
                .texOffs(211, 66).addBox(1.5F, 0.3F, -2.4F, 2.0F, 3.0F, 3.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(223, 66).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 8.0F, 7.0F)
                , PartPose.offset(4.8F, 10.0F, 0.7F));
        PartDefinition shin_l = leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(1, 83).addBox(-3.2F, -1.0F, -3.2F, 6.0F, 6.0F, 6.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition boot_l = shin_l.addOrReplaceChild("boot_l", CubeListBuilder.create()
                .texOffs(27, 83).addBox(-3.8F, -0.6F, -4.8F, 8.0F, 3.0F, 9.0F)
                .texOffs(63, 83).addBox(-4.0F, 2.0F, -5.0F, 8.0F, 1.0F, 10.0F)
                .texOffs(101, 83).addBox(-3.6F, -0.4F, -5.0F, 7.0F, 1.0F, 1.0F)
                , PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(119, 83).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 8.0F, 7.0F)
                , PartPose.offset(-4.8F, 10.0F, 0.7F));
        PartDefinition shin_r = leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(149, 83).addBox(-2.8F, -1.0F, -3.2F, 6.0F, 6.0F, 6.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition boot_r = shin_r.addOrReplaceChild("boot_r", CubeListBuilder.create()
                .texOffs(175, 83).addBox(-4.2F, -0.6F, -4.8F, 8.0F, 3.0F, 9.0F)
                .texOffs(211, 83).addBox(-4.0F, 2.0F, -5.0F, 8.0F, 1.0F, 10.0F)
                .texOffs(1, 100).addBox(-3.4F, -0.4F, -5.0F, 7.0F, 1.0F, 1.0F)
                , PartPose.offset(0.0F, 5.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ImportedBossActions.butcher(this.root, entity, ageInTicks)) return;
        this.root.getAllParts().forEach(ModelPart::resetPose);
        float stride = limbSwing * 0.62F;
        float amplitude = Math.min(limbSwingAmount, 0.6F);
        this.leg_l.xRot = Mth.cos(stride) * amplitude * 0.8F;
        this.leg_r.xRot = -this.leg_l.xRot;
        this.shin_l.xRot = -Math.max(0, this.leg_l.xRot) * 0.6F;
        this.shin_r.xRot = -Math.max(0, this.leg_r.xRot) * 0.6F;
        this.apron_l.xRot = -Math.max(0, -this.leg_l.xRot) * 0.8F;
        this.apron_r.xRot = -Math.max(0, -this.leg_r.xRot) * 0.8F;
        this.body.xRot = 0.0698F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;
        this.head.xRot = -0.0698F;
        this.head.yRot = 0.0F;
        this.head.zRot = 0.0F;
        this.arm_r.xRot = -0.2094F;
        this.arm_r.yRot = -0.1396F;
        this.arm_r.zRot = 0.1396F;
        this.forearm_r.xRot = -0.3142F;
        this.forearm_r.yRot = 0.0F;
        this.forearm_r.zRot = 0.0F;
        this.hand_r.xRot = 0.0F;
        this.hand_r.yRot = 0.0F;
        this.hand_r.zRot = 0.0F;
        this.arm_l.xRot = 0.0698F;
        this.arm_l.yRot = 0.0F;
        this.arm_l.zRot = -0.1396F;
        this.forearm_l.xRot = -0.1396F;
        this.forearm_l.yRot = 0.0F;
        this.forearm_l.zRot = 0.0F;
        this.body.xRot += Mth.sin(ageInTicks * 0.08F) * 0.012F;
        this.arm_l.xRot -= this.leg_l.xRot * 0.6F;
        this.head.yRot = Mth.clamp(netHeadYaw, -40, 40) * Mth.DEG_TO_RAD;
        this.head.xRot += Mth.clamp(headPitch, -25, 25) * Mth.DEG_TO_RAD;
        String action = entity.getButcherAction();
        float age = entity.getButcherActionAge(ageInTicks - entity.tickCount);
        applyAction(action, age);
    }

    private void applyAction(String action, float age) {
        switch (action) {
            case "chop" -> {
                animate(this.body, age, CHOP_BODY);
                animate(this.head, age, CHOP_HEAD);
                animate(this.arm_r, age, CHOP_ARM_R);
                animate(this.forearm_r, age, CHOP_FOREARM_R);
                animate(this.arm_l, age, CHOP_ARM_L);
                animate(this.hand_r, age, CHOP_HAND_R);
                animate(this.apron_r, age, CHOP_APRON_R);
                animate(this.apron_l, age, CHOP_APRON_L);
                animate(this.forearm_l, age, CHOP_FOREARM_L);
            }
            case "backhand" -> {
                animate(this.body, age, BACKHAND_BODY);
                animate(this.arm_r, age, BACKHAND_ARM_R);
                animate(this.forearm_r, age, BACKHAND_FOREARM_R);
                animate(this.hand_r, age, BACKHAND_HAND_R);
                animate(this.arm_l, age, BACKHAND_ARM_L);
                animate(this.head, age, BACKHAND_HEAD);
                animate(this.apron_l, age, BACKHAND_APRON_L);
                animate(this.apron_r, age, BACKHAND_APRON_R);
                animate(this.forearm_l, age, BACKHAND_FOREARM_L);
            }
            default -> { }
        }
    }

    private static final float[][] CHOP_BODY = {
            {0.0F, -4.0F, -12.0F, 0.0F},
            {1.0F, 16.0F, 10.0F, 0.0F},
            {2.0F, 14.0F, 10.0F, 0.0F},
            {4.0F, 4.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_HEAD = {
            {0.0F, 4.0F, 10.0F, 0.0F},
            {1.0F, -12.0F, 0.0F, 0.0F},
            {2.0F, -10.0F, 0.0F, 0.0F},
            {4.0F, -4.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_ARM_R = {
            {0.0F, -145.0F, -15.0F, 15.0F},
            {1.0F, -20.0F, -10.0F, 12.0F},
            {2.0F, -20.0F, -10.0F, 12.0F},
            {4.0F, -12.0F, -8.0F, 8.0F},
    };
    private static final float[][] CHOP_FOREARM_R = {
            {0.0F, -30.0F, 0.0F, 0.0F},
            {1.0F, -15.0F, 0.0F, 0.0F},
            {2.0F, -15.0F, 0.0F, 0.0F},
            {4.0F, -18.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_ARM_L = {
            {0.0F, -35.0F, 0.0F, -22.0F},
            {1.0F, -15.0F, 0.0F, -20.0F},
            {2.0F, 4.0F, 0.0F, -8.0F},
            {4.0F, 4.0F, 0.0F, -8.0F},
    };
    private static final float[][] CHOP_HAND_R = {
            {0.0F, 0.0F, 0.0F, 0.0F},
            {1.0F, 70.0F, 0.0F, 0.0F},
            {2.0F, 65.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_APRON_R = {
            {0.0F, 0.0F, 0.0F, 0.0F},
            {1.0F, -20.0F, 0.0F, 0.0F},
            {2.0F, 0.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_APRON_L = {
            {0.0F, 0.0F, 0.0F, 0.0F},
            {1.0F, -20.0F, 0.0F, 0.0F},
            {2.0F, 0.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] CHOP_FOREARM_L = {
            {0.0F, -8.0F, 0.0F, 0.0F},
            {1.0F, -8.0F, 0.0F, 0.0F},
            {2.0F, -8.0F, 0.0F, 0.0F},
            {4.0F, -8.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_BODY = {
            {0.0F, 4.0F, -22.0F, 0.0F},
            {1.0F, 8.0F, 25.0F, 0.0F},
            {2.0F, 8.0F, 25.0F, 0.0F},
            {4.0F, 4.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_ARM_R = {
            {0.0F, -62.0F, -65.0F, 28.0F},
            {1.0F, -65.0F, 30.0F, 8.0F},
            {2.0F, -65.0F, 30.0F, 8.0F},
            {4.0F, -12.0F, -8.0F, 8.0F},
    };
    private static final float[][] BACKHAND_FOREARM_R = {
            {0.0F, -20.0F, 0.0F, 0.0F},
            {1.0F, -15.0F, 0.0F, 0.0F},
            {2.0F, -15.0F, 0.0F, 0.0F},
            {4.0F, -18.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_HAND_R = {
            {0.0F, 90.0F, 0.0F, 0.0F},
            {1.0F, 90.0F, 0.0F, 0.0F},
            {2.0F, 90.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_ARM_L = {
            {0.0F, -18.0F, 0.0F, -25.0F},
            {1.0F, -15.0F, 0.0F, -28.0F},
            {2.0F, 4.0F, 0.0F, -8.0F},
            {4.0F, 4.0F, 0.0F, -8.0F},
    };
    private static final float[][] BACKHAND_HEAD = {
            {0.0F, -4.0F, 18.0F, 0.0F},
            {1.0F, -6.0F, -12.0F, 0.0F},
            {2.0F, -4.0F, 0.0F, 0.0F},
            {4.0F, -4.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_APRON_L = {
            {0.0F, 0.0F, 0.0F, 0.0F},
            {1.0F, -12.0F, 0.0F, 6.0F},
            {2.0F, 0.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_APRON_R = {
            {0.0F, 0.0F, 0.0F, 0.0F},
            {1.0F, -12.0F, 0.0F, 6.0F},
            {2.0F, 0.0F, 0.0F, 0.0F},
            {4.0F, 0.0F, 0.0F, 0.0F},
    };
    private static final float[][] BACKHAND_FOREARM_L = {
            {0.0F, -8.0F, 0.0F, 0.0F},
            {1.0F, -8.0F, 0.0F, 0.0F},
            {2.0F, -8.0F, 0.0F, 0.0F},
            {4.0F, -8.0F, 0.0F, 0.0F},
    };

    private static void animate(ModelPart part, float age, float[][] frames) {
        int index = 0;
        while (index < frames.length - 2 && age > frames[index + 1][0]) index++;
        float[] from = frames[index];
        float[] to = frames[index + 1];
        float progress = Mth.clamp((age - from[0]) / (to[0] - from[0]), 0, 1);
        progress = progress * progress * (3 - 2 * progress);
        part.xRot = Mth.lerp(progress, from[1], to[1]) * Mth.DEG_TO_RAD;
        part.yRot = Mth.lerp(progress, from[2], to[2]) * Mth.DEG_TO_RAD;
        part.zRot = Mth.lerp(progress, from[3], to[3]) * Mth.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(pose, buffer, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        if (ImportedBossActions.hideWeapon(this.root, pose)) return;
        this.root.translateAndRotate(pose);
        this.body.translateAndRotate(pose);
        boolean right = arm == HumanoidArm.RIGHT;
        (right ? this.arm_r : this.arm_l).translateAndRotate(pose);
        (right ? this.forearm_r : this.forearm_l).translateAndRotate(pose);
        (right ? this.hand_r : this.hand_l).translateAndRotate(pose);
        pose.translate(0, HAND_OFFSET_Y, -0.05F);
        pose.scale(WEAPON_SCALE, WEAPON_SCALE, WEAPON_SCALE);
    }

    public ModelPart root() { return this.root; }
}
