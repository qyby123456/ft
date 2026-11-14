/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.qingyilight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.farmingtales.ftboss.qingyilight.LightGeometry;
import net.farmingtales.ftboss.qingyilight.LightRules;
import net.farmingtales.ftboss.qingyilight.client.LightClient;
import net.farmingtales.ftboss.qingyilight.client.LightRenderPlan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class LightRender {
    private static final RenderType TYPE = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "textures/qingyilight/pixel.png"));

    private LightRender() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase == TickEvent.Phase.END && !Minecraft.m_91087_().m_91104_()) {
            LightClient.tick();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.f_91073_ == null) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        List<LightRenderPlan.Prepared> list = LightRenderPlan.prepare(LightClient.scenes(renderLevelStageEvent.getPartialTick()), vec3, renderLevelStageEvent.getFrustum() == null ? null : arg_0 -> ((Frustum)renderLevelStageEvent.getFrustum()).m_113029_(arg_0));
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(TYPE);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = new Matrix3f((Matrix4fc)matrix4f);
        int n = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.MINIMAL ? 2 : (minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.DECREASED ? 1 : 0);
        int n2 = LightRules.budget(n);
        int n3 = 0;
        Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        for (int i = 0; i < 2; ++i) {
            for (LightRenderPlan.Prepared prepared : list) {
                LightClient.Draw draw = prepared.scene();
                Matrix4f matrix4f2 = prepared.placement();
                for (LightGeometry.Layer layer : draw.geometry().layers()) {
                    if (layer.essential() != (i == 0) || n == 2 && !layer.essential() || n > 0 && !layer.essential() && prepared.distanceSquared() > 256.0 || n3 + layer.faces().size() > n2) continue;
                    for (double[] dArray : layer.faces()) {
                        double d;
                        double d2;
                        double d3 = d2 = Double.POSITIVE_INFINITY;
                        double d4 = d2;
                        double d5 = d = Double.NEGATIVE_INFINITY;
                        double d6 = d;
                        for (int j = 0; j < 4; ++j) {
                            matrix4f2.transformPosition(vector3fArray[j].set((float)dArray[j * 3], (float)dArray[j * 3 + 1], (float)dArray[j * 3 + 2]));
                            d2 = Math.min(d2, (double)vector3fArray[j].x);
                            d3 = Math.min(d3, (double)vector3fArray[j].y);
                            d4 = Math.min(d4, (double)vector3fArray[j].z);
                            d = Math.max(d, (double)vector3fArray[j].x);
                            d5 = Math.max(d5, (double)vector3fArray[j].y);
                            d6 = Math.max(d6, (double)vector3fArray[j].z);
                        }
                        double d7 = layer.alpha() * LightRules.cameraOpacity(d2, d3, d4, d, d5, d6);
                        if (d7 < 0.012) continue;
                        vector3f.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f2.set((Vector3fc)vector3fArray[2]).sub((Vector3fc)vector3fArray[0]));
                        if ((double)vector3f.lengthSquared() < 1.0E-12) continue;
                        vector3f.normalize();
                        for (int j = 0; j < 4; ++j) {
                            vertexConsumer.m_252986_(matrix4f, vector3fArray[j].x, vector3fArray[j].y, vector3fArray[j].z).m_6122_(layer.color() >> 16 & 0xFF, layer.color() >> 8 & 0xFF, layer.color() & 0xFF, (int)Math.round(d7 * 255.0)).m_7421_(j == 1 || j == 2 ? 0.75f : 0.25f, j >= 2 ? 0.75f : 0.25f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, vector3f.x, vector3f.y, vector3f.z).m_5752_();
                        }
                        ++n3;
                    }
                }
            }
        }
        if (n3 > 0) {
            bufferSource.m_109912_(TYPE);
        }
    }
}

