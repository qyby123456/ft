/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  org.joml.Matrix4f
 *  org.slf4j.Logger
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SwordAxisDebugLayer<T extends GeoAnimatable>
extends GeoRenderLayer<T> {
    public static final String PROPERTY = "ftboss.debugSwordAxes";
    private static final float UNIT = 0.0625f;
    private static final float AXIS_LENGTH = 1.0f;
    private static final float TICK_STEP = 8.0f;
    private static final int TICK_COUNT = 4;
    private static final float TICK_ARM = 12.0f;
    private static final Logger LOGGER = LogUtils.getLogger();
    private Object reported;
    private final GeoRenderer<? extends GeoAnimatable> debugRenderer;

    public SwordAxisDebugLayer(GeoRenderer<? extends GeoAnimatable> renderer) {
        super(renderer);
        this.debugRenderer = renderer;
    }

    public static <R extends GeoAnimatable> SwordAxisDebugLayer<R> of(GeoRenderer<R> renderer) {
        return new SwordAxisDebugLayer(renderer);
    }

    public GeoRenderer<? extends GeoAnimatable> getDebugRenderer() {
        return this.debugRenderer;
    }

    public static boolean enabled() {
        return Boolean.parseBoolean(System.getProperty(PROPERTY, "false"));
    }

    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        int i;
        if (!SwordAxisDebugLayer.enabled() || bone.getParent() != null) {
            return;
        }
        this.reportOnce(poseStack, animatable, bone);
        VertexConsumer lines = bufferSource.m_6299_(RenderType.m_110504_());
        SwordAxisDebugLayer.cross(poseStack, lines);
        SwordAxisDebugLayer.axis(poseStack, lines, 1.0f, 0.0f, 0.0f, 1.0f, 0.2f, 0.2f);
        SwordAxisDebugLayer.axis(poseStack, lines, 0.0f, 1.0f, 0.0f, 0.2f, 1.0f, 0.3f);
        SwordAxisDebugLayer.axis(poseStack, lines, 0.0f, 0.0f, 1.0f, 0.3f, 0.5f, 1.0f);
        for (i = 1; i <= 4; ++i) {
            SwordAxisDebugLayer.tick(poseStack, lines, (float)i * 8.0f, 0.95f, 0.95f, 0.95f);
        }
        for (i = 1; i <= 4; ++i) {
            SwordAxisDebugLayer.tick(poseStack, lines, (float)(-i) * 8.0f, 1.0f, 0.85f, 0.2f);
        }
    }

    private void reportOnce(PoseStack poseStack, T animatable, GeoBone bone) {
        if (this.reported == animatable) {
            return;
        }
        this.reported = animatable;
        Matrix4f m = poseStack.m_85850_().m_252922_();
        LOGGER.info("[FTBoss][\u5251\u5750\u6807\u8f74] {} \u6839\u9aa8\u9abc={} \u5f53\u5e27\u539f\u70b9=[{}. {}. {}] \u8bfb\u6cd5\uff1a\u767d\u5341\u5b57=display\u539f\u70b9\uff0c\u7eff\u8f74=\u5251\u5c16\u65b9\u5411\uff0c\u523b\u5ea6\u95f4\u8ddd 8 \u6a21\u578b\u5355\u4f4d\uff08\u534a\u683c\uff09\uff0c\u767d\u523b\u5ea6\u5728\u5203\u4fa7\u3001\u9ec4\u523b\u5ea6\u5728\u67c4\u4fa7", new Object[]{animatable.getClass().getSimpleName(), bone.getName(), String.format("%.5f", Float.valueOf(m.m30())), String.format("%.5f", Float.valueOf(m.m31())), String.format("%.5f", Float.valueOf(m.m32()))});
    }

    private static void cross(PoseStack poseStack, VertexConsumer vc) {
        float r = 12.0f;
        SwordAxisDebugLayer.seg(poseStack, vc, -r, 0.0f, 0.0f, r, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        SwordAxisDebugLayer.seg(poseStack, vc, 0.0f, -r, 0.0f, 0.0f, r, 0.0f, 1.0f, 1.0f, 1.0f);
        SwordAxisDebugLayer.seg(poseStack, vc, 0.0f, 0.0f, -r, 0.0f, 0.0f, r, 1.0f, 1.0f, 1.0f);
    }

    private static void axis(PoseStack poseStack, VertexConsumer vc, float dx, float dy, float dz, float r, float g, float b) {
        float len = 16.0f;
        SwordAxisDebugLayer.seg(poseStack, vc, 0.0f, 0.0f, 0.0f, dx * len, dy * len, dz * len, r, g, b);
    }

    private static void tick(PoseStack poseStack, VertexConsumer vc, float y, float r, float g, float b) {
        float s = 12.0f;
        SwordAxisDebugLayer.seg(poseStack, vc, -s, y, 0.0f, s, y, 0.0f, r, g, b);
        SwordAxisDebugLayer.seg(poseStack, vc, 0.0f, y, -s, 0.0f, y, s, r, g, b);
    }

    private static void seg(PoseStack poseStack, VertexConsumer vc, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        PoseStack.Pose pose = poseStack.m_85850_();
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-5f) {
            return;
        }
        vc.m_252986_(pose.m_252922_(), x1, y1, z1).m_85950_(r, g, b, 1.0f).m_252939_(pose.m_252943_(), nx /= len, ny /= len, nz /= len).m_5752_();
        vc.m_252986_(pose.m_252922_(), x2, y2, z2).m_85950_(r, g, b, 1.0f).m_252939_(pose.m_252943_(), nx, ny, nz).m_5752_();
    }
}

