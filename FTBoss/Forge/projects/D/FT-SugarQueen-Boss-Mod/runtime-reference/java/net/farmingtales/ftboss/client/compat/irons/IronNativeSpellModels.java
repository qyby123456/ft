/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.compat.irons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public final class IronNativeSpellModels {
    private static final Map<IronNativeSpellRules.Style, Baked> MODELS = new EnumMap<IronNativeSpellRules.Style, Baked>(IronNativeSpellRules.Style.class);

    private IronNativeSpellModels() {
    }

    public static ResourceLocation texture(IronNativeSpellRules.Style style) {
        if (style == IronNativeSpellRules.Style.ICE_CROWN) {
            return new ResourceLocation("ftboss", "textures/fx/irons/manor_crystal_coronation.png");
        }
        if (style == IronNativeSpellRules.Style.GOLD_SHIELD) {
            return new ResourceLocation("ftboss", "textures/fx/irons/manor_orchard_aegis.png");
        }
        if (style == IronNativeSpellRules.Style.IVORY_SHIELD) {
            return new ResourceLocation("ftboss", "textures/fx/irons/manor_effigy_ward.png");
        }
        if (style == IronNativeSpellRules.Style.WINGS) {
            return new ResourceLocation("ftboss", "textures/fx/irons/manor_wingstep_wings.png");
        }
        throw new IllegalArgumentException("No custom Manor spell model: " + String.valueOf((Object)style));
    }

    public static int instances(IronNativeSpellRules.Style style) {
        if (style == IronNativeSpellRules.Style.ICE_CROWN) {
            return 8;
        }
        if (style == IronNativeSpellRules.Style.GOLD_SHIELD) {
            return 6;
        }
        if (style == IronNativeSpellRules.Style.IVORY_SHIELD) {
            return 4;
        }
        if (style == IronNativeSpellRules.Style.WINGS) {
            return 1;
        }
        return 0;
    }

    public static void render(IronNativeSpellRules.Style style, double d, int n, float f, PoseStack poseStack, VertexConsumer vertexConsumer) {
        if (!IronNativeSpellRules.model(style) || !Double.isFinite(d) || d < 0.0 || !Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        Baked baked = MODELS.computeIfAbsent(style, IronNativeSpellModels::bake);
        baked.root().m_171331_().forEach(ModelPart::m_233569_);
        if (style == IronNativeSpellRules.Style.WINGS) {
            float f2 = 0.12f + IronNativeSpellRules.growth(d, 0.0) * 0.52f;
            float f3 = (float)Math.sin(d * 0.22) * 0.13f;
            ModelPart modelPart = baked.root().m_171324_("left_wing");
            ModelPart modelPart2 = baked.root().m_171324_("right_wing");
            modelPart.f_104205_ = -f2;
            modelPart2.f_104205_ = f2;
            modelPart.f_104204_ = f3;
            modelPart2.f_104204_ = -f3;
            IronNativeSpellModels.draw(baked, poseStack, vertexConsumer, 0.0, 0.3, -0.34, 0.0, 1.55, 0xFFFFFF, f);
            return;
        }
        int n2 = IronNativeSpellModels.instances(style);
        for (int i = 0; i < n2; ++i) {
            double d2;
            double d3 = (double)i * Math.PI * 2.0 / (double)n2 + (style == IronNativeSpellRules.Style.ICE_CROWN ? 0.0 : d * 0.025);
            double d4 = IronNativeSpellRules.growth(d + 1.0, style == IronNativeSpellRules.Style.ICE_CROWN ? (double)(i % 2) * 2.0 : 0.0);
            double d5 = style == IronNativeSpellRules.Style.ICE_CROWN ? 2.75 : (d2 = style == IronNativeSpellRules.Style.GOLD_SHIELD ? 1.28 : 1.48);
            double d6 = style == IronNativeSpellRules.Style.ICE_CROWN ? (i % 2 == 0 ? 2.45 : 1.75) * d4 : 1.02 * d4;
            double d7 = style == IronNativeSpellRules.Style.ICE_CROWN ? 0.04 : 0.73 + Math.sin(d * 0.09 + (double)i) * 0.09;
            IronNativeSpellModels.draw(baked, poseStack, vertexConsumer, Math.sin(d3) * d2, d7, Math.cos(d3) * d2, d3, Math.max(0.005, d6), 0xFFFFFF, f);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void draw(Baked baked, PoseStack poseStack, VertexConsumer vertexConsumer, double d, double d2, double d3, double d4, double d5, int n, float f) {
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(d, d2, d3);
            poseStack.m_252781_(Axis.f_252436_.m_252961_((float)d4));
            float f2 = (float)(d5 / baked.height());
            poseStack.m_85841_(f2, -f2, f2);
            poseStack.m_85837_(-baked.centerX(), -baked.bottom(), -baked.centerZ());
            baked.root().m_104306_(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.f_118083_, (float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, Math.min(1.0f, f));
        }
        finally {
            poseStack.m_85849_();
        }
    }

    private static Baked bake(IronNativeSpellRules.Style style) {
        ModelPart modelPart;
        if (style == IronNativeSpellRules.Style.ICE_CROWN) {
            modelPart = IronNativeSpellModels.createCrystalCoronationLayer().m_171564_();
        } else if (style == IronNativeSpellRules.Style.GOLD_SHIELD) {
            modelPart = IronNativeSpellModels.createOrchardAegisLayer().m_171564_();
        } else if (style == IronNativeSpellRules.Style.IVORY_SHIELD) {
            modelPart = IronNativeSpellModels.createEffigyWardLayer().m_171564_();
        } else if (style == IronNativeSpellRules.Style.WINGS) {
            modelPart = IronNativeSpellModels.createWingstepLayer().m_171564_();
        } else {
            throw new IllegalArgumentException("No custom Manor spell model: " + String.valueOf((Object)style));
        }
        double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        modelPart.m_171309_(new PoseStack(), (pose, string, n, cube) -> {
            for (float f : new float[]{cube.f_104335_, cube.f_104338_}) {
                for (float f2 : new float[]{cube.f_104336_, cube.f_104339_}) {
                    for (float f3 : new float[]{cube.f_104337_, cube.f_104340_}) {
                        Vector3f vector3f = new Vector3f(f / 16.0f, f2 / 16.0f, f3 / 16.0f);
                        pose.m_252922_().transformPosition(vector3f);
                        dArray[0] = Math.min(dArray[0], (double)vector3f.x);
                        dArray[1] = Math.min(dArray[1], (double)vector3f.y);
                        dArray[2] = Math.min(dArray[2], (double)vector3f.z);
                        dArray[3] = Math.max(dArray[3], (double)vector3f.x);
                        dArray[4] = Math.max(dArray[4], (double)vector3f.y);
                        dArray[5] = Math.max(dArray[5], (double)vector3f.z);
                    }
                }
            }
        });
        if (!Double.isFinite(dArray[4]) || dArray[4] - dArray[1] < 0.001) {
            throw new IllegalStateException("Empty custom Manor model: " + String.valueOf((Object)style));
        }
        return new Baked(modelPart, (dArray[0] + dArray[3]) / 2.0, dArray[4], (dArray[2] + dArray[5]) / 2.0, dArray[4] - dArray[1]);
    }

    private static LayerDefinition createCrystalCoronationLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("crystal", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-3.8f, -1.5f, -2.8f, 7.6f, 1.5f, 5.6f).m_171514_(96, 0).m_171481_(-3.15f, -3.0f, -2.35f, 6.3f, 1.8f, 4.7f).m_171514_(0, 0).m_171481_(-2.45f, -7.4f, -1.8f, 4.9f, 5.0f, 3.6f).m_171514_(32, 0).m_171481_(-1.85f, -12.3f, -1.45f, 3.7f, 5.5f, 2.9f).m_171514_(64, 0).m_171481_(-1.25f, -15.6f, -1.05f, 2.5f, 4.1f, 2.1f).m_171514_(96, 0).m_171481_(-0.62f, -18.2f, -0.62f, 1.24f, 3.4f, 1.24f).m_171514_(32, 0).m_171481_(-1.35f, -12.7f, -2.12f, 2.7f, 8.7f, 0.4f).m_171514_(0, 0).m_171481_(-1.1f, -12.1f, 1.72f, 2.2f, 7.1f, 0.33f).m_171514_(32, 32).m_171481_(-2.7f, -4.0f, -2.05f, 5.4f, 1.0f, 4.1f).m_171514_(32, 32).m_171481_(-2.05f, -8.9f, -1.7f, 4.1f, 0.8f, 3.4f), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        partDefinition2.m_171599_("heart_gem", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.72f, -0.8f, -0.35f, 1.44f, 1.6f, 0.6f), PartPose.m_171423_((float)0.0f, (float)-9.6f, (float)-2.2f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("l_lower_shard", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-2.4f, -7.6f, -1.0f, 2.6f, 8.6f, 2.0f), PartPose.m_171423_((float)-3.2f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.38397f));
        partDefinition2.m_171599_("l_mid_shard", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.85f, -7.4f, -0.85f, 1.95f, 8.3f, 1.7f), PartPose.m_171423_((float)-2.7f, (float)-6.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.2618f));
        partDefinition2.m_171599_("l_wing_facet", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-1.55f, -4.3f, -0.3f, 1.65f, 5.0f, 0.5f), PartPose.m_171423_((float)-2.6f, (float)-4.5f, (float)-1.9f, (float)0.0f, (float)0.0f, (float)-0.41888f));
        partDefinition2.m_171599_("l_crown_tine", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-1.15f, -5.2f, -0.65f, 1.25f, 5.8f, 1.3f), PartPose.m_171423_((float)-2.35f, (float)-11.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.19199f));
        partDefinition2.m_171599_("l_gold_brace", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.78f, -6.0f, -0.25f, 0.85f, 6.7f, 0.45f), PartPose.m_171423_((float)-2.62f, (float)-5.0f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("r_lower_shard", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(0.0f, -7.6f, -1.0f, 2.4f, 8.6f, 2.0f), PartPose.m_171423_((float)3.2f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.38397f));
        partDefinition2.m_171599_("r_mid_shard", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-0.15f, -7.4f, -0.85f, 2.0f, 8.3f, 1.7f), PartPose.m_171423_((float)2.7f, (float)-6.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.2618f));
        partDefinition2.m_171599_("r_wing_facet", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.05f, -4.3f, -0.3f, 1.6f, 5.0f, 0.5f), PartPose.m_171423_((float)2.6f, (float)-4.5f, (float)-1.9f, (float)0.0f, (float)0.0f, (float)0.41888f));
        partDefinition2.m_171599_("r_crown_tine", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-0.05f, -5.2f, -0.65f, 1.2f, 5.8f, 1.3f), PartPose.m_171423_((float)2.35f, (float)-11.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.19199f));
        partDefinition2.m_171599_("r_gold_brace", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.07f, -6.0f, -0.25f, 0.85f, 6.7f, 0.45f), PartPose.m_171423_((float)2.62f, (float)-5.0f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("front_rune_0", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-0.35f, -0.35f, -0.22f, 0.7f, 0.7f, 0.42f), PartPose.m_171423_((float)0.0f, (float)-5.55f, (float)-2.28f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("front_rune_1", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-0.35f, -0.35f, -0.22f, 0.7f, 0.7f, 0.42f), PartPose.m_171423_((float)0.0f, (float)-7.45f, (float)-2.28f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("front_rune_2", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-0.35f, -0.35f, -0.22f, 0.7f, 0.7f, 0.42f), PartPose.m_171423_((float)0.0f, (float)-11.45f, (float)-2.28f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("satellite_shard_0", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.45f, -3.9f, -0.45f, 0.9f, 4.3f, 0.9f), PartPose.m_171423_((float)-1.7f, (float)-1.4f, (float)1.7f, (float)0.27925f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("satellite_shard_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.45f, -3.9f, -0.45f, 0.9f, 4.3f, 0.9f), PartPose.m_171423_((float)1.7f, (float)-1.4f, (float)1.7f, (float)0.27925f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("satellite_shard_2", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.45f, -3.4f, -0.45f, 0.9f, 3.8f, 0.9f), PartPose.m_171423_((float)-1.8f, (float)-1.4f, (float)-1.6f, (float)-0.24435f, (float)0.0f, (float)0.2618f));
        partDefinition2.m_171599_("satellite_shard_3", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.45f, -3.4f, -0.45f, 0.9f, 3.8f, 0.9f), PartPose.m_171423_((float)1.8f, (float)-1.4f, (float)-1.6f, (float)-0.24435f, (float)0.0f, (float)-0.2618f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private static LayerDefinition createOrchardAegisLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("aegis", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-4.8f, -15.2f, -1.5f, 9.6f, 11.2f, 3.0f).m_171514_(32, 0).m_171481_(-6.3f, -16.8f, -1.35f, 12.6f, 4.2f, 2.7f).m_171514_(0, 0).m_171481_(-5.7f, -7.0f, -1.3f, 11.4f, 4.6f, 2.6f).m_171514_(0, 32).m_171481_(-2.0f, -12.7f, 1.3f, 4.0f, 6.7f, 1.1f).m_171514_(32, 32).m_171481_(-3.1f, -18.5f, -1.55f, 6.2f, 2.7f, 3.1f).m_171514_(64, 0).m_171481_(-2.45f, -12.0f, -2.55f, 4.9f, 5.0f, 0.95f).m_171514_(64, 32).m_171481_(-1.4f, -11.1f, -2.75f, 0.95f, 1.3f, 0.33f).m_171514_(32, 32).m_171481_(-4.22f, -15.12f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(32, 32).m_171481_(-0.42f, -17.22f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(32, 32).m_171481_(3.38f, -15.12f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(32, 32).m_171481_(-5.02f, -5.62f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(32, 32).m_171481_(4.18f, -5.62f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(32, 32).m_171481_(-0.42f, -2.82f, -2.05f, 0.84f, 0.84f, 0.55f).m_171514_(0, 32).m_171481_(-3.12f, -15.4f, 1.45f, 0.64f, 12.4f, 0.55f).m_171514_(0, 32).m_171481_(-0.32f, -15.4f, 1.45f, 0.64f, 12.4f, 0.55f).m_171514_(0, 32).m_171481_(2.48f, -15.4f, 1.45f, 0.64f, 12.4f, 0.55f), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        partDefinition2.m_171599_("shield_tip", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-3.4f, -1.5f, -1.15f, 6.8f, 4.0f, 2.3f), PartPose.m_171423_((float)0.0f, (float)-2.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("l_upper_rim", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.1f, -3.8f, -1.85f, 1.25f, 7.1f, 3.7f), PartPose.m_171423_((float)-5.9f, (float)-13.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("l_lower_rim", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.1f, -4.7f, -1.7f, 1.2f, 8.9f, 3.4f), PartPose.m_171423_((float)-5.0f, (float)-6.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.17453f));
        partDefinition2.m_171599_("l_brow_scroll", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-2.4f, -1.6f, -1.8f, 2.6f, 2.5f, 3.6f), PartPose.m_171423_((float)-5.0f, (float)-15.8f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.34907f));
        partDefinition2.m_171599_("l_branch", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.85f, -7.7f, -0.25f, 1.0f, 9.7f, 0.45f), PartPose.m_171423_((float)-5.35f, (float)-7.0f, (float)-1.75f, (float)0.0f, (float)0.0f, (float)0.24435f));
        partDefinition2.m_171599_("l_leaf_0", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-2.8f, -0.9f, -0.35f, 3.9f, 1.7f, 0.7f), PartPose.m_171423_((float)-5.0f, (float)-7.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)0.48869f));
        partDefinition2.m_171599_("l_leaf_1", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-2.98f, -0.9f, -0.35f, 3.88f, 1.7f, 0.7f), PartPose.m_171423_((float)-5.0f, (float)-10.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)0.36652f));
        partDefinition2.m_171599_("l_leaf_2", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-3.16f, -0.9f, -0.35f, 3.86f, 1.7f, 0.7f), PartPose.m_171423_((float)-5.0f, (float)-13.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)0.24435f));
        partDefinition2.m_171599_("r_upper_rim", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-0.1f, -3.8f, -1.85f, 1.2f, 7.1f, 3.7f), PartPose.m_171423_((float)5.9f, (float)-13.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("r_lower_rim", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-0.1f, -4.7f, -1.7f, 1.2f, 8.9f, 3.4f), PartPose.m_171423_((float)5.0f, (float)-6.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition2.m_171599_("r_brow_scroll", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.2f, -1.6f, -1.8f, 2.6f, 2.5f, 3.6f), PartPose.m_171423_((float)5.0f, (float)-15.8f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.34907f));
        partDefinition2.m_171599_("r_branch", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.15f, -7.7f, -0.25f, 1.0f, 9.7f, 0.45f), PartPose.m_171423_((float)5.35f, (float)-7.0f, (float)-1.75f, (float)0.0f, (float)0.0f, (float)-0.24435f));
        partDefinition2.m_171599_("r_leaf_0", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-1.1f, -0.9f, -0.35f, 3.9f, 1.7f, 0.7f), PartPose.m_171423_((float)5.0f, (float)-7.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)-0.48869f));
        partDefinition2.m_171599_("r_leaf_1", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.9f, -0.9f, -0.35f, 3.88f, 1.7f, 0.7f), PartPose.m_171423_((float)5.0f, (float)-10.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)-0.36652f));
        partDefinition2.m_171599_("r_leaf_2", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.7f, -0.9f, -0.35f, 3.86f, 1.7f, 0.7f), PartPose.m_171423_((float)5.0f, (float)-13.8f, (float)-2.1f, (float)0.0f, (float)0.0f, (float)-0.24435f));
        partDefinition2.m_171599_("apple_lobe_l", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-3.2f, -1.7f, -0.45f, 3.0f, 3.4f, 0.8f), PartPose.m_171423_((float)0.0f, (float)-9.7f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.31416f));
        partDefinition2.m_171599_("apple_lobe_r", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(0.2f, -1.7f, -0.45f, 3.0f, 3.4f, 0.8f), PartPose.m_171423_((float)0.0f, (float)-9.7f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)-0.31416f));
        partDefinition2.m_171599_("apple_stem", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-0.35f, -1.9f, -0.4f, 0.7f, 2.2f, 0.62f), PartPose.m_171423_((float)0.0f, (float)-11.7f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition2.m_171599_("apple_leaf", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.35f, -0.9f, -0.55f, 3.1f, 1.3f, 0.8f), PartPose.m_171423_((float)0.35f, (float)-12.5f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)-0.40143f));
        partDefinition2.m_171599_("crown_leaf_l", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-3.7f, -1.2f, -0.3f, 3.9f, 1.6f, 0.55f), PartPose.m_171423_((float)-1.0f, (float)-17.0f, (float)-1.6f, (float)0.0f, (float)0.0f, (float)0.31416f));
        partDefinition2.m_171599_("crown_leaf_r", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.2f, -1.2f, -0.3f, 3.9f, 1.6f, 0.55f), PartPose.m_171423_((float)1.0f, (float)-17.0f, (float)-1.6f, (float)0.0f, (float)0.0f, (float)-0.31416f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private static LayerDefinition createEffigyWardLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("ward", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-4.7f, -15.0f, -1.45f, 9.4f, 11.0f, 2.9f).m_171514_(0, 32).m_171481_(-6.4f, -15.8f, -1.3f, 12.8f, 4.5f, 2.6f).m_171514_(0, 0).m_171481_(-3.7f, -19.3f, -1.55f, 7.4f, 5.0f, 3.1f).m_171514_(32, 0).m_171481_(-2.6f, -15.0f, -1.72f, 5.2f, 2.2f, 3.44f).m_171514_(32, 0).m_171481_(-5.2f, -6.0f, -1.25f, 10.4f, 4.0f, 2.5f).m_171514_(96, 32).m_171481_(-0.65f, -19.0f, 1.35f, 1.3f, 17.0f, 0.9f).m_171514_(32, 0).m_171481_(-8.9f, -9.2f, -1.05f, 1.7f, 2.3f, 2.1f).m_171514_(64, 0).m_171481_(-2.65f, -2.2f, -2.35f, 1.3f, 2.0f, 0.6f).m_171514_(64, 32).m_171481_(-4.95f, -2.2f, -2.35f, 1.3f, 2.0f, 0.6f).m_171514_(32, 0).m_171481_(7.2f, -9.2f, -1.05f, 1.7f, 2.3f, 2.1f).m_171514_(64, 0).m_171481_(1.35f, -2.2f, -2.35f, 1.3f, 2.0f, 0.6f).m_171514_(64, 32).m_171481_(3.65f, -2.2f, -2.35f, 1.3f, 2.0f, 0.6f).m_171514_(64, 0).m_171481_(-2.1f, -15.7f, -2.0f, 4.2f, 0.5f, 0.5f).m_171514_(96, 0).m_171481_(-3.65f, -22.1f, -0.9f, 1.3f, 3.5f, 0.8f).m_171514_(96, 32).m_171481_(-3.16f, -22.7f, -0.6f, 0.32f, 0.6f, 0.25f).m_171514_(96, 0).m_171481_(-0.65f, -23.4f, -0.9f, 1.3f, 4.8f, 0.8f).m_171514_(96, 32).m_171481_(-0.16f, -24.0f, -0.6f, 0.32f, 0.6f, 0.25f).m_171514_(96, 0).m_171481_(2.35f, -22.1f, -0.9f, 1.3f, 3.5f, 0.8f).m_171514_(96, 32).m_171481_(2.84f, -22.7f, -0.6f, 0.32f, 0.6f, 0.25f), PartPose.m_171419_((float)0.0f, (float)0.0f, (float)0.0f));
        partDefinition2.m_171599_("bottom_point", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-2.4f, -1.4f, -1.05f, 4.8f, 3.0f, 2.1f), PartPose.m_171423_((float)0.0f, (float)-1.6f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("l_frame", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-1.25f, -7.4f, -1.75f, 1.3f, 14.3f, 3.5f), PartPose.m_171423_((float)-5.55f, (float)-10.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.10472f));
        partDefinition2.m_171599_("l_arm", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-3.0f, -1.3f, -1.25f, 3.1f, 2.8f, 2.5f), PartPose.m_171423_((float)-5.3f, (float)-9.7f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.24435f));
        partDefinition2.m_171599_("l_temple_plate", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-1.3f, -2.8f, -0.35f, 1.5f, 3.8f, 0.6f), PartPose.m_171423_((float)-3.8f, (float)-16.0f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("l_prayer_strip", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.8f, -1.0f, -0.3f, 0.95f, 8.5f, 0.45f), PartPose.m_171423_((float)-4.7f, (float)-9.5f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)0.12217f));
        partDefinition2.m_171599_("l_cord_0", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.2f, -3.0f, -0.25f, 0.4f, 10.7f, 0.4f), PartPose.m_171423_((float)-2.0f, (float)-9.0f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.08727f));
        partDefinition2.m_171599_("l_cord_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.2f, -1.9f, -0.25f, 0.4f, 9.6f, 0.4f), PartPose.m_171423_((float)-4.3f, (float)-9.0f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition2.m_171599_("r_frame", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.05f, -7.4f, -1.75f, 1.3f, 14.3f, 3.5f), PartPose.m_171423_((float)5.55f, (float)-10.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.10472f));
        partDefinition2.m_171599_("r_arm", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-0.3f, -1.3f, -1.25f, 3.3f, 2.8f, 2.5f), PartPose.m_171423_((float)5.3f, (float)-9.7f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.24435f));
        partDefinition2.m_171599_("r_temple_plate", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-0.2f, -2.8f, -0.35f, 1.5f, 3.8f, 0.6f), PartPose.m_171423_((float)3.8f, (float)-16.0f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("r_prayer_strip", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.15f, -1.0f, -0.3f, 0.95f, 8.5f, 0.45f), PartPose.m_171423_((float)4.7f, (float)-9.5f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)-0.12217f));
        partDefinition2.m_171599_("r_cord_0", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.2f, -3.0f, -0.25f, 0.4f, 10.7f, 0.4f), PartPose.m_171423_((float)2.0f, (float)-9.0f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)-0.08727f));
        partDefinition2.m_171599_("r_cord_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.2f, -1.9f, -0.25f, 0.4f, 9.6f, 0.4f), PartPose.m_171423_((float)4.3f, (float)-9.0f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition2.m_171599_("eye_l", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.9f, -0.4f, -0.35f, 1.8f, 0.75f, 0.57f), PartPose.m_171423_((float)-1.55f, (float)-17.05f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)0.13963f));
        partDefinition2.m_171599_("eye_r", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.9f, -0.4f, -0.35f, 1.8f, 0.75f, 0.57f), PartPose.m_171423_((float)1.55f, (float)-17.05f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)-0.13963f));
        partDefinition2.m_171599_("brow_l", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-1.2f, -0.25f, -0.3f, 2.4f, 0.45f, 0.5f), PartPose.m_171423_((float)-1.6f, (float)-18.2f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("brow_r", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-1.2f, -0.25f, -0.3f, 2.4f, 0.45f, 0.5f), PartPose.m_171423_((float)1.6f, (float)-18.2f, (float)-1.7f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("mouth_stitch_0", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.13f, -0.55f, -0.35f, 0.27f, 1.1f, 0.6f), PartPose.m_171423_((float)-1.62f, (float)-15.45f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition2.m_171599_("mouth_stitch_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.13f, -0.55f, -0.35f, 0.27f, 1.1f, 0.6f), PartPose.m_171423_((float)-0.77f, (float)-15.45f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)-0.17453f));
        partDefinition2.m_171599_("mouth_stitch_2", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.13f, -0.55f, -0.35f, 0.27f, 1.1f, 0.6f), PartPose.m_171423_((float)0.08f, (float)-15.45f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition2.m_171599_("mouth_stitch_3", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.13f, -0.55f, -0.35f, 0.27f, 1.1f, 0.6f), PartPose.m_171423_((float)0.93f, (float)-15.45f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)-0.17453f));
        partDefinition2.m_171599_("mouth_stitch_4", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-0.13f, -0.55f, -0.35f, 0.27f, 1.1f, 0.6f), PartPose.m_171423_((float)1.78f, (float)-15.45f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)0.17453f));
        partDefinition2.m_171599_("chest_seal", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-1.75f, -1.75f, -0.3f, 3.5f, 3.5f, 0.6f), PartPose.m_171423_((float)0.0f, (float)-9.85f, (float)-1.8f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("seal_core", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-0.75f, -0.75f, -0.35f, 1.5f, 1.5f, 0.4f), PartPose.m_171423_((float)0.0f, (float)-9.85f, (float)-2.0f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("flame_0", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-0.48f, -1.4f, -0.33f, 0.96f, 1.7f, 0.6f), PartPose.m_171423_((float)-3.0f, (float)-22.8f, (float)-0.45f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("flame_1", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-0.48f, -1.4f, -0.33f, 0.96f, 1.7f, 0.6f), PartPose.m_171423_((float)0.0f, (float)-24.1f, (float)-0.45f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("flame_2", CubeListBuilder.m_171558_().m_171514_(64, 32).m_171481_(-0.48f, -1.4f, -0.33f, 0.96f, 1.7f, 0.6f), PartPose.m_171423_((float)3.0f, (float)-22.8f, (float)-0.45f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private static LayerDefinition createWingstepLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.m_171576_();
        PartDefinition partDefinition2 = partDefinition.m_171599_("left_wing", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-1.55f, -2.1f, -1.2f, 2.0f, 3.8f, 2.4f), PartPose.m_171419_((float)-1.25f, (float)-9.5f, (float)1.4f));
        partDefinition2.m_171599_("left_shoulder_fan", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-3.8f, -3.5f, -0.95f, 4.0f, 5.0f, 1.9f), PartPose.m_171423_((float)-0.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.31416f));
        partDefinition2.m_171599_("left_gold_spar", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-12.0f, -0.65f, -0.65f, 12.0f, 1.3f, 1.3f), PartPose.m_171423_((float)-0.25f, (float)-0.15f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.41888f));
        partDefinition2.m_171599_("left_upper_spar", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-10.0f, -0.6f, -0.5f, 10.0f, 1.2f, 1.05f), PartPose.m_171423_((float)-0.25f, (float)-1.9f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.66323f));
        partDefinition2.m_171599_("left_primary_0", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-18.5f, -1.15f, -0.75f, 18.5f, 2.3f, 1.5f), PartPose.m_171423_((float)-2.15f, (float)3.35f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.8203f));
        partDefinition2.m_171599_("left_primary_vein_0", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-15.54f, -0.345f, -0.17f, 15.54f, 0.529f, 0.32f), PartPose.m_171423_((float)-2.15f, (float)3.35f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.8203f));
        partDefinition2.m_171599_("left_primary_1", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-18.0f, -0.95f, -0.75f, 18.0f, 1.9f, 1.5f), PartPose.m_171423_((float)-2.9f, (float)4.55f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.59341f));
        partDefinition2.m_171599_("left_primary_vein_1", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-15.12f, -0.285f, -0.17f, 15.12f, 0.437f, 0.32f), PartPose.m_171423_((float)-2.9f, (float)4.55f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.59341f));
        partDefinition2.m_171599_("left_primary_2", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-17.0f, -0.85f, -0.75f, 17.0f, 1.7f, 1.5f), PartPose.m_171423_((float)-3.65f, (float)5.65f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.38397f));
        partDefinition2.m_171599_("left_primary_vein_2", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-14.28f, -0.255f, -0.17f, 14.28f, 0.391f, 0.32f), PartPose.m_171423_((float)-3.65f, (float)5.65f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.38397f));
        partDefinition2.m_171599_("left_primary_3", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-15.5f, -0.775f, -0.75f, 15.5f, 1.55f, 1.5f), PartPose.m_171423_((float)-4.4f, (float)6.725f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.19199f));
        partDefinition2.m_171599_("left_primary_vein_3", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-13.02f, -0.2325f, -0.17f, 13.02f, 0.3565f, 0.32f), PartPose.m_171423_((float)-4.4f, (float)6.725f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.19199f));
        partDefinition2.m_171599_("left_primary_4", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-14.0f, -0.725f, -0.75f, 14.0f, 1.45f, 1.5f), PartPose.m_171423_((float)-5.15f, (float)7.775f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.01745f));
        partDefinition2.m_171599_("left_primary_vein_4", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-11.76f, -0.2175f, -0.17f, 11.76f, 0.3335f, 0.32f), PartPose.m_171423_((float)-5.15f, (float)7.775f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.01745f));
        partDefinition2.m_171599_("left_primary_5", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-12.5f, -0.675f, -0.75f, 12.5f, 1.35f, 1.5f), PartPose.m_171423_((float)-5.9f, (float)8.825f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("left_primary_vein_5", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-10.5f, -0.2025f, -0.17f, 10.5f, 0.3105f, 0.32f), PartPose.m_171423_((float)-5.9f, (float)8.825f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("left_primary_6", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-11.0f, -0.6f, -0.75f, 11.0f, 1.2f, 1.5f), PartPose.m_171423_((float)-6.65f, (float)9.7f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.40143f));
        partDefinition2.m_171599_("left_primary_vein_6", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-9.24f, -0.18f, -0.17f, 9.24f, 0.276f, 0.32f), PartPose.m_171423_((float)-6.65f, (float)9.7f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.40143f));
        partDefinition2.m_171599_("left_secondary_0", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-10.5f, -1.5f, -0.85f, 10.5f, 3.0f, 1.65f), PartPose.m_171423_((float)-1.25f, (float)-3.0f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.73304f));
        partDefinition2.m_171599_("left_secondary_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-11.3f, -1.4f, -0.85f, 11.3f, 2.8f, 1.65f), PartPose.m_171423_((float)-1.8f, (float)-1.3f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.47124f));
        partDefinition2.m_171599_("left_secondary_2", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-11.5f, -1.3f, -0.85f, 11.5f, 2.6f, 1.65f), PartPose.m_171423_((float)-2.35f, (float)0.4f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition2.m_171599_("left_secondary_3", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-10.7f, -1.25f, -0.85f, 10.7f, 2.5f, 1.65f), PartPose.m_171423_((float)-2.9f, (float)1.85f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.08727f));
        partDefinition2.m_171599_("left_secondary_4", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-9.5f, -1.15f, -0.85f, 9.5f, 2.3f, 1.65f), PartPose.m_171423_((float)-3.45f, (float)3.15f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.34907f));
        partDefinition2.m_171599_("left_covert_0", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-5.6f, -0.8f, -0.65f, 5.6f, 1.55f, 1.3f), PartPose.m_171423_((float)-0.55f, (float)0.55f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.41888f));
        partDefinition2.m_171599_("left_covert_1", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-6.05f, -0.8f, -0.65f, 6.05f, 1.55f, 1.3f), PartPose.m_171423_((float)-1.0f, (float)-0.5f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.2618f));
        partDefinition2.m_171599_("left_covert_2", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-6.5f, -0.8f, -0.65f, 6.5f, 1.55f, 1.3f), PartPose.m_171423_((float)-1.45f, (float)-1.55f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.10472f));
        partDefinition2.m_171599_("left_covert_3", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-6.95f, -0.8f, -0.65f, 6.95f, 1.55f, 1.3f), PartPose.m_171423_((float)-1.9f, (float)-2.6f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.05236f));
        partDefinition2.m_171599_("left_covert_4", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-7.4f, -0.8f, -0.65f, 7.4f, 1.55f, 1.3f), PartPose.m_171423_((float)-2.35f, (float)-3.65f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition2.m_171599_("left_covert_5", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-7.85f, -0.8f, -0.65f, 7.85f, 1.55f, 1.3f), PartPose.m_171423_((float)-2.8f, (float)-4.7f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.36652f));
        partDefinition2.m_171599_("left_star_0", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)-1.85f, (float)-0.7f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("left_star_1", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)-3.85f, (float)-1.8f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("left_star_2", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)-5.85f, (float)-2.9f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition2.m_171599_("left_tip_guard", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-2.4f, -3.0f, -0.6f, 2.8f, 3.6f, 1.2f), PartPose.m_171423_((float)-12.15f, (float)-2.1f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.61087f));
        PartDefinition partDefinition3 = partDefinition.m_171599_("right_wing", CubeListBuilder.m_171558_().m_171514_(32, 32).m_171481_(-0.45f, -2.1f, -1.2f, 2.0f, 3.8f, 2.4f), PartPose.m_171419_((float)1.25f, (float)-9.5f, (float)1.4f));
        partDefinition3.m_171599_("right_shoulder_fan", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-0.4f, -3.5f, -0.95f, 4.2f, 5.0f, 1.9f), PartPose.m_171423_((float)0.35f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.31416f));
        partDefinition3.m_171599_("right_gold_spar", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(0.0f, -0.65f, -0.65f, 12.0f, 1.3f, 1.3f), PartPose.m_171423_((float)0.25f, (float)-0.15f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.41888f));
        partDefinition3.m_171599_("right_upper_spar", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(0.0f, -0.6f, -0.5f, 10.0f, 1.2f, 1.05f), PartPose.m_171423_((float)0.25f, (float)-1.9f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.66323f));
        partDefinition3.m_171599_("right_primary_0", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-6.8f, -1.15f, -0.75f, 25.3f, 2.3f, 1.5f), PartPose.m_171423_((float)2.15f, (float)3.35f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.8203f));
        partDefinition3.m_171599_("right_primary_vein_0", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-6.8f, -0.345f, -0.17f, 22.34f, 0.529f, 0.32f), PartPose.m_171423_((float)2.15f, (float)3.35f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.8203f));
        partDefinition3.m_171599_("right_primary_1", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-8.3f, -0.95f, -0.75f, 26.3f, 1.9f, 1.5f), PartPose.m_171423_((float)2.9f, (float)4.55f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.59341f));
        partDefinition3.m_171599_("right_primary_vein_1", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-8.3f, -0.285f, -0.17f, 23.42f, 0.437f, 0.32f), PartPose.m_171423_((float)2.9f, (float)4.55f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.59341f));
        partDefinition3.m_171599_("right_primary_2", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-9.8f, -0.85f, -0.75f, 26.8f, 1.7f, 1.5f), PartPose.m_171423_((float)3.65f, (float)5.65f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.38397f));
        partDefinition3.m_171599_("right_primary_vein_2", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-9.8f, -0.255f, -0.17f, 24.08f, 0.391f, 0.32f), PartPose.m_171423_((float)3.65f, (float)5.65f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.38397f));
        partDefinition3.m_171599_("right_primary_3", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-11.3f, -0.775f, -0.75f, 26.8f, 1.55f, 1.5f), PartPose.m_171423_((float)4.4f, (float)6.725f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)-0.19199f));
        partDefinition3.m_171599_("right_primary_vein_3", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-11.3f, -0.2325f, -0.17f, 24.32f, 0.3565f, 0.32f), PartPose.m_171423_((float)4.4f, (float)6.725f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)-0.19199f));
        partDefinition3.m_171599_("right_primary_4", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-12.8f, -0.725f, -0.75f, 26.8f, 1.45f, 1.5f), PartPose.m_171423_((float)5.15f, (float)7.775f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.01745f));
        partDefinition3.m_171599_("right_primary_vein_4", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-12.8f, -0.2175f, -0.17f, 24.56f, 0.3335f, 0.32f), PartPose.m_171423_((float)5.15f, (float)7.775f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.01745f));
        partDefinition3.m_171599_("right_primary_5", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-14.3f, -0.675f, -0.75f, 26.8f, 1.35f, 1.5f), PartPose.m_171423_((float)5.9f, (float)8.825f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition3.m_171599_("right_primary_vein_5", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-14.3f, -0.2025f, -0.17f, 24.8f, 0.3105f, 0.32f), PartPose.m_171423_((float)5.9f, (float)8.825f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition3.m_171599_("right_primary_6", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-15.8f, -0.6f, -0.75f, 26.8f, 1.2f, 1.5f), PartPose.m_171423_((float)6.65f, (float)9.7f, (float)-0.8f, (float)0.0f, (float)0.0f, (float)0.40143f));
        partDefinition3.m_171599_("right_primary_vein_6", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(-15.8f, -0.18f, -0.17f, 25.04f, 0.276f, 0.32f), PartPose.m_171423_((float)6.65f, (float)9.7f, (float)-1.55f, (float)0.0f, (float)0.0f, (float)0.40143f));
        partDefinition3.m_171599_("right_secondary_0", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-5.0f, -1.5f, -0.85f, 15.5f, 3.0f, 1.65f), PartPose.m_171423_((float)1.25f, (float)-3.0f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.73304f));
        partDefinition3.m_171599_("right_secondary_1", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-6.1f, -1.4f, -0.85f, 17.4f, 2.8f, 1.65f), PartPose.m_171423_((float)1.8f, (float)-1.3f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.47124f));
        partDefinition3.m_171599_("right_secondary_2", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-7.2f, -1.3f, -0.85f, 18.7f, 2.6f, 1.65f), PartPose.m_171423_((float)2.35f, (float)0.4f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.20944f));
        partDefinition3.m_171599_("right_secondary_3", CubeListBuilder.m_171558_().m_171514_(64, 0).m_171481_(-8.3f, -1.25f, -0.85f, 19.0f, 2.5f, 1.65f), PartPose.m_171423_((float)2.9f, (float)1.85f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.08727f));
        partDefinition3.m_171599_("right_secondary_4", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-9.4f, -1.15f, -0.85f, 18.9f, 2.3f, 1.65f), PartPose.m_171423_((float)3.45f, (float)3.15f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)0.34907f));
        partDefinition3.m_171599_("right_covert_0", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-3.6f, -0.8f, -0.65f, 9.2f, 1.55f, 1.3f), PartPose.m_171423_((float)0.55f, (float)0.55f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.41888f));
        partDefinition3.m_171599_("right_covert_1", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-4.5f, -0.8f, -0.65f, 10.55f, 1.55f, 1.3f), PartPose.m_171423_((float)1.0f, (float)-0.5f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.2618f));
        partDefinition3.m_171599_("right_covert_2", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-5.4f, -0.8f, -0.65f, 11.9f, 1.55f, 1.3f), PartPose.m_171423_((float)1.45f, (float)-1.55f, (float)0.3f, (float)0.0f, (float)0.0f, (float)-0.10472f));
        partDefinition3.m_171599_("right_covert_3", CubeListBuilder.m_171558_().m_171514_(96, 0).m_171481_(-6.3f, -0.8f, -0.65f, 13.25f, 1.55f, 1.3f), PartPose.m_171423_((float)1.9f, (float)-2.6f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.05236f));
        partDefinition3.m_171599_("right_covert_4", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-7.2f, -0.8f, -0.65f, 14.6f, 1.55f, 1.3f), PartPose.m_171423_((float)2.35f, (float)-3.65f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.20944f));
        partDefinition3.m_171599_("right_covert_5", CubeListBuilder.m_171558_().m_171514_(0, 32).m_171481_(-8.1f, -0.8f, -0.65f, 15.95f, 1.55f, 1.3f), PartPose.m_171423_((float)2.8f, (float)-4.7f, (float)0.3f, (float)0.0f, (float)0.0f, (float)0.36652f));
        partDefinition3.m_171599_("right_star_0", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)1.85f, (float)-0.7f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition3.m_171599_("right_star_1", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)3.85f, (float)-1.8f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition3.m_171599_("right_star_2", CubeListBuilder.m_171558_().m_171514_(96, 32).m_171481_(-0.55f, -0.55f, -0.35f, 1.1f, 1.1f, 0.6f), PartPose.m_171423_((float)5.85f, (float)-2.9f, (float)-1.5f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        partDefinition3.m_171599_("right_tip_guard", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171481_(0.2f, -3.0f, -0.6f, 2.2f, 3.6f, 1.2f), PartPose.m_171423_((float)12.15f, (float)-2.1f, (float)-0.4f, (float)0.0f, (float)0.0f, (float)-0.61087f));
        return LayerDefinition.m_171565_((MeshDefinition)meshDefinition, (int)128, (int)128);
    }

    private record Baked(ModelPart root, double centerX, double bottom, double centerZ, double height) {
    }
}

