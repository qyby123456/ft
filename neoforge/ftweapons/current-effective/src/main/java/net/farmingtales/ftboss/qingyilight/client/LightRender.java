package net.farmingtales.ftboss.qingyilight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.NativeLegacyVertexNormals;
import java.util.List;
import net.farmingtales.ftboss.qingyilight.LightGeometry;
import net.farmingtales.ftboss.qingyilight.LightRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME,
    value = {Dist.CLIENT}
)
public final class LightRender {
    private static final RenderType TYPE = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/qingyilight/pixel.png"));

    private LightRender() {
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        if (!Minecraft.getInstance().isPaused()) {
            LightClient.tick();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var0.getStage() == Stage.AFTER_PARTICLES && var1.level != null) {
            Vec3 var2 = var0.getCamera().getPosition();
            List<LightRenderPlan.Prepared> var3 = LightRenderPlan.prepare(
                LightClient.scenes(var0.getPartialTick().getGameTimeDeltaPartialTick(false)), var2, var0.getFrustum() == null ? null : var0.getFrustum()::isVisible
            );
            BufferSource var4 = var1.renderBuffers().bufferSource();
            VertexConsumer var5 = var4.getBuffer(TYPE);
            Matrix4f var6 = var0.getPoseStack().last().pose();
            Matrix3f var7 = new Matrix3f(var6);
            int var8 = var1.options.particles().get() == ParticleStatus.MINIMAL ? 2 : (var1.options.particles().get() == ParticleStatus.DECREASED ? 1 : 0);
            int var9 = LightRules.budget(var8);
            int var10 = 0;
            Vector3f[] var11 = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
            Vector3f var12 = new Vector3f();
            Vector3f var13 = new Vector3f();

            for (int var14 = 0; var14 < 2; var14++) {
                for (LightRenderPlan.Prepared var16 : var3) {
                    LightClient.Draw var17 = var16.scene();
                    Matrix4f var18 = var16.placement();

                    for (LightGeometry.Layer var20 : var17.geometry().layers()) {
                        if (var20.essential() == (var14 == 0)
                            && (var8 != 2 || var20.essential())
                            && (var8 <= 0 || var20.essential() || !(var16.distanceSquared() > 256.0))
                            && var10 + var20.faces().size() <= var9) {
                            for (double[] var22 : var20.faces()) {
                                double var23 = Double.POSITIVE_INFINITY;
                                double var25 = var23;
                                double var27 = var23;
                                double var29 = Double.NEGATIVE_INFINITY;
                                double var31 = var29;
                                double var33 = var29;

                                for (int var35 = 0; var35 < 4; var35++) {
                                    var18.transformPosition(var11[var35].set((float)var22[var35 * 3], (float)var22[var35 * 3 + 1], (float)var22[var35 * 3 + 2]));
                                    var23 = Math.min(var23, (double)var11[var35].x);
                                    var25 = Math.min(var25, (double)var11[var35].y);
                                    var27 = Math.min(var27, (double)var11[var35].z);
                                    var29 = Math.max(var29, (double)var11[var35].x);
                                    var31 = Math.max(var31, (double)var11[var35].y);
                                    var33 = Math.max(var33, (double)var11[var35].z);
                                }

                                double var38 = var20.alpha() * LightRules.cameraOpacity(var23, var25, var27, var29, var31, var33);
                                if (!(var38 < 0.012)) {
                                    var12.set(var11[1]).sub(var11[0]).cross(var13.set(var11[2]).sub(var11[0]));
                                    if (!((double)var12.lengthSquared() < 1.0E-12)) {
                                        var12.normalize();

                                        for (int var37 = 0; var37 < 4; var37++) {
                                            NativeLegacyVertexNormals.normal(var5.addVertex(var6, var11[var37].x, var11[var37].y, var11[var37].z)
                                                .setColor(
                                                    var20.color() >> 16 & 0xFF, var20.color() >> 8 & 0xFF, var20.color() & 0xFF, (int)Math.round(var38 * 255.0)
                                                )
                                                .setUv(var37 != 1 && var37 != 2 ? 0.25F : 0.75F, var37 >= 2 ? 0.75F : 0.25F)
                                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                                .setLight(15728880)
                                                , var7, var12.x, var12.y, var12.z);
                                        }

                                        var10++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (var10 > 0) {
                var4.endBatch(TYPE);
            }
        }
    }
}
