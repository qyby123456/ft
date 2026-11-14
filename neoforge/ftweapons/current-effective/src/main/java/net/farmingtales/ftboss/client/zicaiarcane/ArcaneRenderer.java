package net.farmingtales.ftboss.client.zicaiarcane;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

public final class ArcaneRenderer {
    private static final RenderType ENERGY = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/zicai_arcane/energy.png"));
    private static final List<ArcaneRenderer.Batch> BATCHES = new ArrayList<>();
    private static final List<ArcaneRenderer.Visible> CACHE = new ArrayList<>();

    public static List<?> consume(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (!var0.isEmpty() && var1.getStage() == Stage.AFTER_PARTICLES) {
            ArrayList var3 = new ArrayList();
            ArrayList<OptimizedSpellVisual> var4 = new ArrayList<>();

            for (Object var6 : var0) {
                if (var6 instanceof OptimizedSpellVisual var7 && ArcaneField.IDS.contains(var7.packet().spell().id)) {
                    var4.add(var7);
                    continue;
                }

                var3.add(var6);
            }

            if (var4.isEmpty()) {
                return var0;
            } else {
                Minecraft var71 = Minecraft.getInstance();
                if (var2 != null && var71.level == var2) {
                    Vec3 var72 = var1.getCamera().getPosition();
                    float var73 = var71.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
                    Entity var8 = var1.getCamera().getEntity();
                    Vec3 var9 = var8 == null ? new Vec3(0.0, 0.0, -1.0) : var8.getViewVector(var73);
                    double[] var10 = new double[]{var72.x, var72.y, var72.z};
                    double[] var11 = new double[]{var9.x, var9.y, var9.z};
                    boolean var12 = !var1.getCamera().isDetached();
                    ParticleStatus var13 = var71.options.particles().get();
                    int var14 = var13 == ParticleStatus.MINIMAL ? 0 : (var13 == ParticleStatus.DECREASED ? 1 : 2);
                    int var15 = ArcaneField.visibleFields(var14);
                    int var16 = ArcaneField.triangleBudget(var14);
                    var4.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var72)));
                    ArrayList var17 = new ArrayList();

                    for (AbstractClientPlayer var19 : var2.players()) {
                        if (var19.getPosition(var73).distanceToSqr(var72) < 1024.0) {
                            var17.add(body(var19.getBoundingBox().inflate(0.08)));
                        }
                    }

                    ArrayList<ArcaneRenderer.Visible> var74 = new ArrayList<>();
                    int var75 = 0;
                    int var20 = 0;

                    for (OptimizedSpellVisual var22 : var4) {
                        if (var75 >= var15) {
                            break;
                        }

                        SpellVisualPacket var23 = var22.packet();
                        if (var23.valid() && var23.dimension().equals(var2.dimension().location())) {
                            ArcaneField.Model var24 = ArcaneField.MODELS.get(var23.spell().id);
                            Entity var25 = var2.getEntity(var23.ownerId());
                            if (var25 != null && !var25.getUUID().equals(var23.owner())) {
                                var25 = null;
                            }

                            Vec3 var26 = var25 == null ? null : var25.getPosition(var73);
                            double[] var27 = ArcaneRig.floorAnchor(
                                var24.anchor(),
                                var23.phase().name(),
                                coordinates(var23.start()),
                                coordinates(var23.end()),
                                var26 == null ? null : coordinates(var26)
                            );
                            Vec3 var28 = new Vec3(var27[0], var27[1], var27[2]);
                            double var29 = SpellVisuals.radiusScale(var25, var24.id());
                            double var31 = ArcaneRig.spatialScale(var29);
                            if (!(var28.distanceToSqr(var72) > 4096.0) && var1.getFrustum().isVisible(new AABB(var28, var28).inflate(7.0 * var31 + 1.0))) {
                                boolean var33 = var13 == ParticleStatus.ALL && var28.distanceToSqr(var72) <= 484.0;
                                int var34 = 0;

                                for (ArcaneField.Triangle var36 : var24.triangles()) {
                                    if (var33 || !var36.detail()) {
                                        var34++;
                                    }
                                }

                                if (var20 + var34 <= var16) {
                                    boolean var83 = var23.phase().name().equals("CHARGE");
                                    int var84 = var23.spell().lifetime(var23.phase());
                                    double var37 = ArcaneRig.animationAge(
                                        var24.id(), var83, (double)((float)(var2.getGameTime() - var22.born()) + var73), var84
                                    );
                                    double var39 = (double)Math.max(0.0F, (float)(var2.getGameTime() - var22.refreshed()) + var73);
                                    double var41 = ArcaneRig.persistence(var39, var84);
                                    if (!var83 && !var24.id().equals("halo_sanctuary")) {
                                        var41 *= 1.0 - 0.9 * ArcaneRig.ease((var37 - 47.0) / 33.0);
                                    }

                                    if (!(var41 <= 0.005)) {
                                        Vec3 var43 = var23.end().subtract(var23.start());
                                        if (var43.x * var43.x + var43.z * var43.z < 1.0E-5 && var25 != null) {
                                            var43 = var25.getViewVector(var73);
                                        }

                                        double var44 = Math.atan2(-var43.x, -var43.z);
                                        double var46 = Math.cos(var44);
                                        double var48 = Math.sin(var44);
                                        if (var75 == BATCHES.size()) {
                                            BATCHES.add(new ArcaneRenderer.Batch());
                                        }

                                        ArcaneRenderer.Batch var50 = BATCHES.get(var75++);
                                        var50.fit(var24);
                                        var20 += var34;

                                        for (int var51 = 0; var51 < var24.bones().size(); var51++) {
                                            var50.frames[var51] = ArcaneRig.frame(var24.bones().get(var51).behavior(), var37, var83);
                                        }

                                        for (int var85 = 0; var85 < var24.vertices().length; var85++) {
                                            int var52 = var24.vertexBones()[var85];
                                            double[] var53 = var50.positions[var85];
                                            ArcaneRig.position(var24.vertices()[var85], var24.bones().get(var52), var50.frames[var52], var53);
                                            double var54 = var53[0];
                                            double var56 = var53[2];
                                            var53[0] = var28.x + var31 * (var54 * var46 + var56 * var48);
                                            var53[1] = var28.y + var31 * var53[1];
                                            var53[2] = var28.z + var31 * (-var54 * var48 + var56 * var46);
                                        }

                                        int var86 = var17.size();
                                        if (var25 != null) {
                                            var17.add(body(var25.getBoundingBox().inflate(0.08)));
                                        }

                                        if (var24.softLight()) {
                                            for (int var87 = 0; var87 < var24.vertices().length; var87++) {
                                                var50.visibility[var87] = ArcaneRig.vertexVisibility(var50.positions[var87], var10, var11, var17, var12);
                                            }
                                        }

                                        for (ArcaneField.Triangle var89 : var24.triangles()) {
                                            if (!var89.detail() || var33) {
                                                double var90 = var89.alpha() * var41 * var50.frames[var24.vertexBones()[var89.indices()[0]]].opacity();
                                                double var91 = 0.0;
                                                if (var24.softLight()) {
                                                    for (int var61 : var89.indices()) {
                                                        var91 = Math.max(var91, var50.visibility[var61]);
                                                    }
                                                } else {
                                                    var90 *= ArcaneRig.visibility(var50.positions, var89.indices(), var10, var11, var17, var12);
                                                    var91 = 1.0;
                                                }

                                                if (!(var90 * var91 < 0.008)) {
                                                    if (var74.size() == CACHE.size()) {
                                                        CACHE.add(new ArcaneRenderer.Visible());
                                                    }

                                                    ArcaneRenderer.Visible var92 = CACHE.get(var74.size());
                                                    var92.batch = var50;
                                                    var92.triangle = var89;
                                                    var92.opacity = (float)var90;
                                                    double var93 = 0.0;
                                                    double var94 = 0.0;
                                                    double var63 = 0.0;

                                                    for (int var68 : var89.indices()) {
                                                        var93 += var50.positions[var68][0] / 3.0;
                                                        var94 += var50.positions[var68][1] / 3.0;
                                                        var63 += var50.positions[var68][2] / 3.0;
                                                    }

                                                    double var95 = var93 - var10[0];
                                                    double var96 = var94 - var10[1];
                                                    double var69 = var63 - var10[2];
                                                    var92.distance = var95 * var95 + var96 * var96 + var69 * var69;
                                                    var74.add(var92);
                                                }
                                            }
                                        }

                                        if (var17.size() > var86) {
                                            var17.remove(var17.size() - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!var74.isEmpty()) {
                        var74.sort(Comparator.<ArcaneRenderer.Visible>comparingDouble(var0x -> var0x.distance).reversed());
                        BufferSource var76 = var71.renderBuffers().bufferSource();
                        Matrix4f var77 = var1.getPoseStack().last().pose();
                        boolean var78 = false;
                        boolean var79 = false;

                        for (ArcaneRenderer.Visible var81 : var74) {
                            RenderType var82 = var81.batch.softLight ? ArcaneLightRenderType.LIGHT : ENERGY;
                            drawTriangle(var76.getBuffer(var82), var77, var10, var81);
                            if (var81.batch.softLight) {
                                var78 = true;
                            } else {
                                var79 = true;
                            }
                        }

                        if (var78) {
                            var76.endBatch(ArcaneLightRenderType.LIGHT);
                        }

                        if (var79) {
                            var76.endBatch(ENERGY);
                        }
                    }

                    return var3;
                } else {
                    return var3;
                }
            }
        } else {
            return var0;
        }
    }

    private static ArcaneRig.Box body(AABB var0) {
        return new ArcaneRig.Box(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ);
    }

    private static double[] coordinates(Vec3 var0) {
        return new double[]{var0.x, var0.y, var0.z};
    }

    private static void drawTriangle(VertexConsumer var0, Matrix4f var1, double[] var2, ArcaneRenderer.Visible var3) {
        int[] var4 = var3.triangle.indices();
        double[][] var5 = var3.batch.positions;
        double[] var6 = var5[var4[0]];
        double[] var7 = var5[var4[1]];
        double[] var8 = var5[var4[2]];
        double var9 = var7[0] - var6[0];
        double var11 = var7[1] - var6[1];
        double var13 = var7[2] - var6[2];
        double var15 = var8[0] - var6[0];
        double var17 = var8[1] - var6[1];
        double var19 = var8[2] - var6[2];
        double var21 = var11 * var19 - var13 * var17;
        double var23 = var13 * var15 - var9 * var19;
        double var25 = var9 * var17 - var11 * var15;
        double var27 = Math.sqrt(var21 * var21 + var23 * var23 + var25 * var25);
        if (!(var27 < 1.0E-10)) {
            for (int var29 = 0; var29 < 4; var29++) {
                int var30 = Math.min(2, var29);
                double[] var31 = var5[var4[var30]];
                double[] var32 = var3.triangle.uv()[var30];
                float var33 = var3.batch.softLight ? (float)((double)var3.opacity * var3.batch.visibility[var4[var30]]) : var3.opacity;
                var0.addVertex(var1, (float)(var31[0] - var2[0]), (float)(var31[1] - var2[1]), (float)(var31[2] - var2[2]))
                    .setColor(1.0F, 1.0F, 1.0F, var33)
                    .setUv((float)var32[0], (float)var32[1])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    .setNormal((float)(var21 / var27), (float)(var23 / var27), (float)(var25 / var27));
            }
        }
    }

    private ArcaneRenderer() {
    }

    private static final class Batch {
        double[][] positions = new double[0][3];
        double[] visibility = new double[0];
        boolean softLight;
        ArcaneRig.Frame[] frames = new ArcaneRig.Frame[0];

        void fit(ArcaneField.Model var1) {
            if (this.positions.length < var1.vertices().length) {
                this.positions = new double[var1.vertices().length][3];
            }

            if (this.visibility.length < var1.vertices().length) {
                this.visibility = new double[var1.vertices().length];
            }

            this.softLight = var1.softLight();
            if (this.frames.length < var1.bones().size()) {
                this.frames = new ArcaneRig.Frame[var1.bones().size()];
            }
        }
    }

    private static final class Visible {
        ArcaneRenderer.Batch batch;
        ArcaneField.Triangle triangle;
        double distance;
        float opacity;
    }
}
