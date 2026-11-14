package net.farmingtales.ftboss.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class ThornCrownModel extends HumanoidModel<LivingEntity> {
    public ThornCrownModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        for (String part : new String[]{"hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            root.addOrReplaceChild(part, CubeListBuilder.create(), PartPose.ZERO);
        }
        head.addOrReplaceChild("band_z_-1", CubeListBuilder.create().texOffs(1, 1)
                .addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.3F, -5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("band_x_-1", CubeListBuilder.create().texOffs(25, 1)
                .addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 10.0F),
                PartPose.offsetAndRotation(-5.2F, -7.55F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_-1_-2_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(-4.246448F, -8.094978F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_-2_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(-4.632193F, -9.026249F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_-2_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(-4.910787F, -9.698833F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_-1_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(-2.246448F, -8.094978F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_-1_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(-2.632193F, -9.026249F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_-1_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(-2.910787F, -9.698833F, -5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_-1_0_0", CubeListBuilder.create().texOffs(1, 14)
                .addBox(-0.55F, -0.962F, -0.55F, 1.1F, 1.924F, 1.1F),
                PartPose.offsetAndRotation(0.0F, -8.351F, -5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_-1_0_1", CubeListBuilder.create().texOffs(8, 14)
                .addBox(-0.35F, -0.592F, -0.35F, 0.7F, 1.184F, 0.7F),
                PartPose.offsetAndRotation(0.0F, -9.683F, -5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_-1_0_2", CubeListBuilder.create().texOffs(13, 14)
                .addBox(-0.15F, -0.481F, -0.15F, 0.3F, 0.962F, 0.3F),
                PartPose.offsetAndRotation(0.0F, -10.645F, -5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_-1_1_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(2.246448F, -8.094978F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_-1_1_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(2.632193F, -9.026249F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_-1_1_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(2.910787F, -9.698833F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_-1_2_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(4.246448F, -8.094978F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_-1_2_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(4.632193F, -9.026249F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_-1_2_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(4.910787F, -9.698833F, -5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_x_-1_-1_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(-5.2F, -8.181231F, -3.020043F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_-1_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(-5.2F, -9.012722F, -3.364458F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_-1_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(-5.2F, -9.613244F, -3.613202F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_0_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(-5.2F, -8.225F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_0_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(-5.2F, -9.125F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_0_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(-5.2F, -9.775F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_1_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(-5.2F, -8.181231F, 3.020043F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_1_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(-5.2F, -9.012722F, 3.364458F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_-1_1_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(-5.2F, -9.613244F, 3.613202F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_-1_-3", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -7.45F, -5.5F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("weave_x_-1_-3", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-5.5F, -7.8F, -3.0F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_-1_0", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.45F, -5.5F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("weave_x_-1_0", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-5.5F, -7.8F, 0.0F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_-1_3", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -7.45F, -5.5F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("weave_x_-1_3", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-5.5F, -7.8F, 3.0F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("band_z_1", CubeListBuilder.create().texOffs(1, 1)
                .addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.3F, 5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("band_x_1", CubeListBuilder.create().texOffs(25, 1)
                .addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 10.0F),
                PartPose.offsetAndRotation(5.2F, -7.55F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_1_-2_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(-4.246448F, -8.094978F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_-2_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(-4.632193F, -9.026249F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_-2_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(-4.910787F, -9.698833F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_-1_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(-2.246448F, -8.094978F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_-1_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(-2.632193F, -9.026249F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_-1_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(-2.910787F, -9.698833F, 5.2F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("thorn_z_1_0_0", CubeListBuilder.create().texOffs(1, 14)
                .addBox(-0.55F, -0.962F, -0.55F, 1.1F, 1.924F, 1.1F),
                PartPose.offsetAndRotation(0.0F, -8.351F, 5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_1_0_1", CubeListBuilder.create().texOffs(8, 14)
                .addBox(-0.35F, -0.592F, -0.35F, 0.7F, 1.184F, 0.7F),
                PartPose.offsetAndRotation(0.0F, -9.683F, 5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_1_0_2", CubeListBuilder.create().texOffs(13, 14)
                .addBox(-0.15F, -0.481F, -0.15F, 0.3F, 0.962F, 0.3F),
                PartPose.offsetAndRotation(0.0F, -10.645F, 5.2F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_z_1_1_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(2.246448F, -8.094978F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_1_1_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(2.632193F, -9.026249F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_1_1_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(2.910787F, -9.698833F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_1_2_0", CubeListBuilder.create().texOffs(49, 1)
                .addBox(-0.55F, -0.728F, -0.55F, 1.1F, 1.456F, 1.1F),
                PartPose.offsetAndRotation(4.246448F, -8.094978F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_1_2_1", CubeListBuilder.create().texOffs(56, 1)
                .addBox(-0.35F, -0.448F, -0.35F, 0.7F, 0.896F, 0.7F),
                PartPose.offsetAndRotation(4.632193F, -9.026249F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_z_1_2_2", CubeListBuilder.create().texOffs(61, 1)
                .addBox(-0.15F, -0.364F, -0.15F, 0.3F, 0.728F, 0.3F),
                PartPose.offsetAndRotation(4.910787F, -9.698833F, 5.2F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("thorn_x_1_-1_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(5.2F, -8.181231F, -3.020043F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_-1_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(5.2F, -9.012722F, -3.364458F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_-1_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(5.2F, -9.613244F, -3.613202F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_0_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(5.2F, -8.225F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_0_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(5.2F, -9.125F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_0_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(5.2F, -9.775F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_1_0", CubeListBuilder.create().texOffs(17, 14)
                .addBox(-0.55F, -0.65F, -0.55F, 1.1F, 1.3F, 1.1F),
                PartPose.offsetAndRotation(5.2F, -8.181231F, 3.020043F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_1_1", CubeListBuilder.create().texOffs(24, 14)
                .addBox(-0.35F, -0.4F, -0.35F, 0.7F, 0.8F, 0.7F),
                PartPose.offsetAndRotation(5.2F, -9.012722F, 3.364458F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("thorn_x_1_1_2", CubeListBuilder.create().texOffs(29, 14)
                .addBox(-0.15F, -0.325F, -0.15F, 0.3F, 0.65F, 0.3F),
                PartPose.offsetAndRotation(5.2F, -9.613244F, 3.613202F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_1_-3", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -7.45F, 5.5F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("weave_x_1_-3", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(5.5F, -7.8F, -3.0F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_1_0", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.45F, 5.5F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("weave_x_1_0", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(5.5F, -7.8F, 0.0F, -0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("weave_z_1_3", CubeListBuilder.create().texOffs(33, 14)
                .addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -7.45F, 5.5F, 0.0F, 0.0F, -0.392699F));
        head.addOrReplaceChild("weave_x_1_3", CubeListBuilder.create().texOffs(43, 14)
                .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(5.5F, -7.8F, 3.0F, 0.392699F, 0.0F, 0.0F));
        head.addOrReplaceChild("blood_heart", CubeListBuilder.create().texOffs(53, 14)
                .addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, -5.85F, 0.0F, 0.0F, -0.785398F));
        head.addOrReplaceChild("blood_drop", CubeListBuilder.create().texOffs(1, 20)
                .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.25F, -5.6F, 0.0F, 0.0F, -0.785398F));
        head.addOrReplaceChild("leaf_-1", CubeListBuilder.create().texOffs(7, 20)
                .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-2.6F, -7.5F, -5.85F, 0.0F, 0.0F, 0.392699F));
        head.addOrReplaceChild("leaf_1", CubeListBuilder.create().texOffs(7, 20)
                .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(2.6F, -7.5F, -5.85F, 0.0F, 0.0F, -0.392699F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
