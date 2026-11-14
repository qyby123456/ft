package net.farmingtales.ftboss.client.compat.irons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.ice_spike.IceSpikeRenderer.IceSpikeModel;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldModel;
import io.redspace.ironsspellbooks.render.AngelWingsModel;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.farmingtales.ftboss.util.IronNativeSpellRules.Style;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public final class IronNativeSpellModels {
    private static final Map<Style, Baked> MODELS = new EnumMap<>(Style.class);

    private IronNativeSpellModels() {}

    public static ResourceLocation texture(Style style) {
        String path = switch (style) {
            case ICE_CROWN -> "textures/entity/ice_spike.png";
            case GOLD_SHIELD, IVORY_SHIELD -> "textures/entity/shield/shield_overlay.png";
            case WINGS -> "textures/entity/angel_wings.png";
            default -> throw new IllegalArgumentException("No native model: " + style);
        };
        return new ResourceLocation("irons_spellbooks", path);
    }

    public static int instances(Style style) {
        return switch (style) {
            case ICE_CROWN -> 8;
            case GOLD_SHIELD -> 6;
            case IVORY_SHIELD -> 4;
            case WINGS -> 1;
            default -> 0;
        };
    }

    public static void render(Style style, double age, int color, float alpha, PoseStack poses, VertexConsumer vertices) {
        if (!IronNativeSpellRules.model(style) || !Double.isFinite(age) || age < 0 || !Float.isFinite(alpha) || alpha <= 0) return;
        Baked baked = MODELS.computeIfAbsent(style, IronNativeSpellModels::bake);
        baked.root().getAllParts().forEach(ModelPart::resetPose);
        if (style == Style.WINGS) {
            float spread = 0.3F + IronNativeSpellRules.growth(age, 0) * 0.8F;
            float flap = (float) Math.sin(age * 0.22) * 0.16F;
            ModelPart left = baked.root().getChild("left_wing");
            ModelPart right = baked.root().getChild("right_wing");
            left.zRot = -spread;
            right.zRot = spread;
            left.yRot = flap;
            right.yRot = -flap;
            draw(baked, poses, vertices, 0, 0.35, -0.28, 0, 1.5, color, alpha);
            return;
        }
        int count = instances(style);
        for (int index = 0; index < count; index++) {
            double angle = index * Math.PI * 2 / count + (style == Style.ICE_CROWN ? 0 : age * 0.025);
            double growth = IronNativeSpellRules.growth(age + 1, style == Style.ICE_CROWN ? index % 2 * 2 : 0);
            double radius = style == Style.ICE_CROWN ? 2.75 : style == Style.GOLD_SHIELD ? 1.2 : 1.45;
            double height = style == Style.ICE_CROWN ? (index % 2 == 0 ? 2.4 : 1.7) * growth : 0.95 * growth;
            double vertical = style == Style.ICE_CROWN ? 0.04 : 0.75 + Math.sin(age * 0.09 + index) * 0.09;
            draw(baked, poses, vertices, Math.sin(angle) * radius, vertical, Math.cos(angle) * radius,
                    angle, Math.max(0.005, height), color, alpha);
        }
    }

    private static void draw(Baked baked, PoseStack poses, VertexConsumer vertices, double across, double vertical,
                             double forward, double angle, double height, int color, float alpha) {
        poses.pushPose();
        try {
            poses.translate(across, vertical, forward);
            poses.mulPose(Axis.YP.rotation((float) angle));
            float scale = (float) (height / baked.height());
            poses.scale(scale, -scale, scale);
            poses.translate(-baked.centerX(), -baked.bottom(), -baked.centerZ());
            baked.root().render(poses, vertices, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F,
                    Math.min(1, alpha));
        } finally {
            poses.popPose();
        }
    }

    private static Baked bake(Style style) {
        ModelPart root = switch (style) {
            case ICE_CROWN -> IceSpikeModel.createBodyLayer().bakeRoot();
            case GOLD_SHIELD, IVORY_SHIELD -> ShieldModel.createBodyLayer().bakeRoot();
            case WINGS -> AngelWingsModel.createLayer().bakeRoot();
            default -> throw new IllegalArgumentException("No native model: " + style);
        };
        double[] bounds = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            for (float horizontal : new float[] {cube.minX, cube.maxX}) {
                for (float vertical : new float[] {cube.minY, cube.maxY}) {
                    for (float depth : new float[] {cube.minZ, cube.maxZ}) {
                        Vector3f point = new Vector3f(horizontal / 16, vertical / 16, depth / 16);
                        pose.pose().transformPosition(point);
                        bounds[0] = Math.min(bounds[0], point.x);
                        bounds[1] = Math.min(bounds[1], point.y);
                        bounds[2] = Math.min(bounds[2], point.z);
                        bounds[3] = Math.max(bounds[3], point.x);
                        bounds[4] = Math.max(bounds[4], point.y);
                        bounds[5] = Math.max(bounds[5], point.z);
                    }
                }
            }
        });
        if (!Double.isFinite(bounds[4]) || bounds[4] - bounds[1] < 0.001) throw new IllegalStateException("Empty native model: " + style);
        return new Baked(root, (bounds[0] + bounds[3]) / 2, bounds[4], (bounds[2] + bounds[5]) / 2, bounds[4] - bounds[1]);
    }

    private record Baked(ModelPart root, double centerX, double bottom, double centerZ, double height) {}
}
