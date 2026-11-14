package net.farmingtales.ftboss.client.render.reapflat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class FlatReapRenderer {
    private static final List<FlatReapRenderer.Panel> PANELS = new ArrayList<>();
    private static final RenderType PIPELINE = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("ftboss", "reap_flat/materials.png"));
    private static double nativeSpan = 1.0;
    private static double placementHeight;
    private static double animationRate;
    private static final Map<OptimizedSpellVisual, double[]> RELEASE_ANCHORS = new IdentityHashMap<>();
    private static ClientLevel anchorWorld;

    private FlatReapRenderer() {
    }

    public static List<?> intercept(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (anchorWorld != var2) {
            RELEASE_ANCHORS.clear();
            anchorWorld = var2;
        }

        ArrayList var3 = new ArrayList();
        ArrayList<OptimizedSpellVisual> var4 = new ArrayList<>();
        Vec3 var5 = var1.getCamera().getPosition();

        for (Object var7 : var0) {
            if (var7 instanceof OptimizedSpellVisual var8 && var8.packet() != null && var8.packet().spell() == Spell.CANDLE_REAP) {
                SpellVisualPacket var9 = var8.packet();
                if (var9.valid() && var9.dimension().equals(var2.dimension().location()) && var9.start().distanceToSqr(var5) < 6400.0) {
                    var4.add(var8);
                }
                continue;
            }

            var3.add(var7);
        }

        RELEASE_ANCHORS.keySet().retainAll(var4);
        if (!var4.isEmpty() && !PANELS.isEmpty()) {
            var4.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var5)));
            Minecraft var55 = Minecraft.getInstance();
            ParticleStatus var56 = var55.options.particles().get();
            int var57 = var56 == ParticleStatus.MINIMAL ? 2 : (var56 == ParticleStatus.DECREASED ? 4 : 6);
            boolean var58 = var56 != ParticleStatus.MINIMAL;
            float var10 = var55.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
            double[] var11 = new double[]{var5.x, var5.y, var5.z};
            Entity var12 = var1.getCamera().getEntity();
            Vec3 var13 = var12 == null ? new Vec3(0.0, 0.0, 1.0) : var12.getLookAngle();
            double[] var14 = new double[]{var13.x, var13.y, var13.z};
            boolean var15 = !var1.getCamera().isDetached();
            BufferSource var16 = var55.renderBuffers().bufferSource();
            VertexConsumer var17 = var16.getBuffer(PIPELINE);
            Matrix4f var18 = var1.getPoseStack().last().pose();
            Matrix3f var19 = var1.getPoseStack().last().normal();
            ArrayList<AABB> var20 = new ArrayList<>();

            for (AbstractClientPlayer var22 : var2.players()) {
                if (var22.getPosition(var10).distanceToSqr(var5) < 6400.0) {
                    var20.add(var22.getBoundingBox().inflate(0.06));
                }
            }

            for (int var59 = Math.min(var57, var4.size()) - 1; var59 >= 0; var59--) {
                OptimizedSpellVisual var60 = (OptimizedSpellVisual)var4.get(var59);
                SpellVisualPacket var23 = var60.packet();
                Entity var24 = var2.getEntity(var23.ownerId());
                if (var24 != null && !var24.getUUID().equals(var23.owner())) {
                    var24 = null;
                }

                Vec3 var25 = var24 == null ? var23.start() : var24.getPosition(var10);
                if (var1.getFrustum().isVisible(new AABB(var25.x - 6.0, var25.y - 1.0, var25.z - 6.0, var25.x + 6.0, var25.y + 5.0, var25.z + 8.0))) {
                    int var26 = var23.spell().lifetime(var23.phase());
                    boolean var27 = var23.phase() == Phase.CHARGE;
                    double var28 = Math.max(1.0, var27 ? (double)var26 : (double)var26 / animationRate);
                    double var30 = (double)Math.max(0.0F, (float)(var2.getGameTime() - var60.born()) + var10);
                    double var32 = (double)Math.max(0.0F, (float)(var2.getGameTime() - var60.refreshed()) + var10);
                    float var34 = (float)(1.0 - FlatReapMotion.smooth((var32 - var28 * 0.72) / Math.max(1.0, var28 * 0.28)));
                    if (!((double)var34 < 0.012)) {
                        double var35 = FlatReapMotion.phaseClock(var30, (double)var26, var27, animationRate);
                        double var37 = 4.6 / nativeSpan * (var27 ? 0.72 + 0.28 * FlatReapMotion.smooth(var30 * animationRate / 12.0) : 1.0);
                        Vec3 var39 = var24 == null ? var23.end().subtract(var23.start()) : var24.getLookAngle();
                        double var40 = Math.hypot(var39.x, var39.z);
                        double var42 = var40 < 1.0E-8 ? 0.0 : var39.x / var40;
                        double var44 = var40 < 1.0E-8 ? 1.0 : var39.z / var40;
                        double var46 = var24 == null ? 0.3 : (double)(var24.getBbWidth() / 2.0F);
                        if (!var27 && !RELEASE_ANCHORS.containsKey(var60)) {
                            RELEASE_ANCHORS.put(var60, new double[]{var25.x, var25.y, var25.z, var42, var44, var46});
                        }

                        List<FlatReapRenderer.Positioned> var48 = place(var35, var37, var25, var42, var44, var46, var5, RELEASE_ANCHORS.get(var60));
                        var48.sort(Comparator.comparingDouble(FlatReapRenderer.Positioned::distance).reversed());

                        for (FlatReapRenderer.Positioned var50 : var48) {
                            if (!var50.panel().energy() || var58) {
                                float var51 = var34 * var50.panel().alpha();
                                if (var50.panel().energy()) {
                                    var51 = (float)((double)var51 * (var27 ? 0.0 : FlatReapMotion.trailAlpha(var35, var50.panel().emitted())));
                                }

                                var51 = (float)((double)var51 * FlatReapMotion.visibleAlpha(var50.limits(), var11, var14, var15));
                                AABB var52 = new AABB(
                                    var50.limits()[0], var50.limits()[1], var50.limits()[2], var50.limits()[3], var50.limits()[4], var50.limits()[5]
                                );

                                for (AABB var54 : var20) {
                                    if (var52.intersects(var54)) {
                                        var51 *= 0.12F;
                                        break;
                                    }
                                }

                                if ((double)var51 >= 0.012) {
                                    writePanel(var17, var50, var18, var19, var5, var51);
                                }
                            }
                        }
                    }
                }
            }

            var16.endBatch(PIPELINE);
            return var3;
        } else {
            return var3;
        }
    }

    private static List<FlatReapRenderer.Positioned> place(
        double var0, double var2, Vec3 var4, double var5, double var7, double var9, Vec3 var11, double[] var12
    ) {
        double[] var13 = FlatReapMotion.rotation(var0, 30);
        ArrayList var14 = new ArrayList(PANELS.size());
        double var15 = Double.POSITIVE_INFINITY;
        double var17 = Double.NEGATIVE_INFINITY;
        double var19 = Double.POSITIVE_INFINITY;

        for (FlatReapRenderer.Panel var22 : PANELS) {
            double[][] var23 = new double[4][];

            for (int var24 = 0; var24 < 4; var24++) {
                var23[var24] = var22.energy()
                    ? (double[])var22.vertices()[var24].clone()
                    : FlatReapMotion.transform(var22.vertices()[var24], var22.pivot(), var13);

                for (int var25 = 0; var25 < 3; var25++) {
                    var23[var24][var25] = var23[var24][var25] * var2;
                }

                if (!var22.energy()) {
                    var15 = Math.min(var15, var23[var24][1]);
                    var17 = Math.max(var17, var23[var24][1]);
                    var19 = Math.min(var19, var23[var24][2]);
                }
            }

            var14.add(var23);
        }

        double var39 = FlatReapMotion.placementLift(var15, var17, placementHeight);
        double var40 = 0.55 + var9 - var19;
        ArrayList var41 = new ArrayList(PANELS.size());

        for (int var26 = 0; var26 < PANELS.size(); var26++) {
            double[] var27 = new double[]{
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY
            };
            double var28 = 0.0;
            boolean var30 = PANELS.get(var26).energy();

            for (double[] var34 : (double[][])var14.get(var26)) {
                if (var30 && var12 != null) {
                    double[] var42 = FlatReapMotion.anchoredTrailPoint(var34, var12);
                    System.arraycopy(var42, 0, var34, 0, 3);
                } else {
                    double var35 = var34[0];
                    double var37 = var34[2] + (var30 ? var9 - 0.3 : var40);
                    var34[0] = var4.x + var35 * var7 + var37 * var5;
                    var34[1] += var4.y + (var30 ? 0.0 : var39);
                    var34[2] = var4.z - var35 * var5 + var37 * var7;
                }

                for (int var43 = 0; var43 < 3; var43++) {
                    var27[var43] = Math.min(var27[var43], var34[var43]);
                    var27[var43 + 3] = Math.max(var27[var43 + 3], var34[var43]);
                }

                var28 += Math.pow(var34[0] - var11.x, 2.0) + Math.pow(var34[1] - var11.y, 2.0) + Math.pow(var34[2] - var11.z, 2.0);
            }

            var41.add(new FlatReapRenderer.Positioned(PANELS.get(var26), (double[][])var14.get(var26), var27, var28 / 4.0));
        }

        return var41;
    }

    private static void writePanel(VertexConsumer var0, FlatReapRenderer.Positioned var1, Matrix4f var2, Matrix3f var3, Vec3 var4, float var5) {
        double[][] var6 = var1.corners();
        Vec3 var7 = new Vec3(var6[1][0] - var6[0][0], var6[1][1] - var6[0][1], var6[1][2] - var6[0][2]);
        Vec3 var8 = new Vec3(var6[2][0] - var6[0][0], var6[2][1] - var6[0][1], var6[2][2] - var6[0][2]);
        Vec3 var9 = var7.cross(var8).normalize();
        int var10 = var1.panel().slot();
        int var11 = (var10 < 9 || var10 > 11) && !var1.panel().energy()
            ? (int)(255.0 * (0.72 + 0.28 * Math.abs(var9.x * -0.45 + var9.y * 0.8 - var9.z * 0.4)))
            : 255;

        for (int var12 = 0; var12 < 4; var12++) {
            float var13 = (float)(((double)(var10 % 8 * 16 + 2) + var1.panel().uv()[var12][0] * 11.0) / 128.0);
            float var14 = (float)(((double)(var10 / 8 * 16 + 2) + var1.panel().uv()[var12][1] * 11.0) / 32.0);
            net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var0.addVertex(var2, (float)(var6[var12][0] - var4.x), (float)(var6[var12][1] - var4.y), (float)(var6[var12][2] - var4.z))
                .setColor(var11, var11, var11, Math.round(var5 * 255.0F))
                .setUv(var13, var14)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                , var3, (float)var9.x, (float)var9.y, (float)var9.z);
        }
    }

    static {
        try (InputStream var0 = FlatReapRenderer.class.getResourceAsStream("/assets/ftboss/reap_flat/model.json")) {
            if (var0 == null) {
                throw new IllegalStateException("New horizontal scythe asset is missing");
            }

            JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
            placementHeight = var1.getAsJsonObject("scene").get("height").getAsDouble();
            if (!Double.isFinite(placementHeight) || placementHeight <= 0.0) {
                throw new IllegalStateException("Invalid scythe placement height");
            }

            animationRate = var1.get("animationRate").getAsDouble();
            if (!Double.isFinite(animationRate) || animationRate <= 0.0) {
                throw new IllegalStateException("Invalid scythe animation rate");
            }

            double[] var2 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] var3 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

            for (JsonElement var5 : var1.getAsJsonArray("faces")) {
                JsonObject var6 = var5.getAsJsonObject();
                double[][] var7 = new double[4][3];
                double[][] var8 = new double[4][2];
                double[] var9 = new double[3];

                for (int var10 = 0; var10 < 3; var10++) {
                    var9[var10] = var6.getAsJsonArray("pivot").get(var10).getAsDouble();
                }

                for (int var16 = 0; var16 < 4; var16++) {
                    for (int var11 = 0; var11 < 3; var11++) {
                        var7[var16][var11] = var6.getAsJsonArray("points").get(var16).getAsJsonArray().get(var11).getAsDouble();
                        var2[var11] = Math.min(var2[var11], var7[var16][var11]);
                        var3[var11] = Math.max(var3[var11], var7[var16][var11]);
                    }

                    for (int var17 = 0; var17 < 2; var17++) {
                        var8[var16][var17] = var6.getAsJsonArray("uv").get(var16).getAsJsonArray().get(var17).getAsDouble();
                    }
                }

                PANELS.add(
                    new FlatReapRenderer.Panel(
                        var7,
                        var8,
                        var9,
                        var6.get("bone").getAsInt(),
                        var6.get("material").getAsInt(),
                        var6.get("opacity").getAsFloat(),
                        var6.get("group").getAsString().equals("detail"),
                        var6.has("emitted") ? var6.get("emitted").getAsDouble() : -1.0
                    )
                );
            }

            for (int var15 = 0; var15 < 3; var15++) {
                nativeSpan = Math.max(nativeSpan, var3[var15] - var2[var15]);
            }

            nativeSpan = var1.getAsJsonObject("scene").get("nativeSpan").getAsDouble();
            if (!Double.isFinite(nativeSpan) || nativeSpan <= 0.0) {
                throw new IllegalStateException("Invalid independent weapon scale");
            }
        } catch (Exception var14) {
            PANELS.clear();
            LogUtils.getLogger().error("Cannot load from-zero horizontal candle scythe", (Throwable)var14);
        }
    }

    private static record Panel(double[][] vertices, double[][] uv, double[] pivot, int joint, int slot, float alpha, boolean energy, double emitted) {
    }

    private static record Positioned(FlatReapRenderer.Panel panel, double[][] corners, double[] limits, double distance) {
    }
}
