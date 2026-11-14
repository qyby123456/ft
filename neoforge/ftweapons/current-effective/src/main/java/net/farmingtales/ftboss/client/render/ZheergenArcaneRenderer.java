package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.zheergenrevision.VisualScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ZheergenArcaneRenderer {
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("ftboss", "fx/zheergen_arcane_light.png");
    private static final RenderType VOLUME = RenderType.entityTranslucent(ATLAS);
    private static final RenderType LIGHT = RenderType.entityTranslucentEmissive(ATLAS);
    private static final double[][] TRACE_UV = new double[][]{
        {0.5175781F, 0.017578125}, {0.7324219F, 0.017578125}, {0.7324219F, 0.23242188F}, {0.5175781F, 0.23242188F}
    };

    private ZheergenArcaneRenderer() {
    }

    public static List<?> dispatch(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (var1.getStage() == Stage.AFTER_PARTICLES && !var0.isEmpty()) {
            ArrayList var3 = new ArrayList();
            LinkedHashMap var4 = new LinkedHashMap();

            for (Object var6 : var0) {
                if (var6 instanceof OptimizedSpellVisual var7
                    && var7.packet() != null
                    && var7.packet().spell() != null
                    && ZheergenArcaneData.SPELLS.contains(var7.packet().spell().id)) {
                    SpellVisualPacket var8 = var7.packet();
                    if (var8.valid() && var2.dimension().location().equals(var8.dimension())) {
                        double var9 = (double)Math.max(0L, var2.getGameTime() - (var8.phase() == Phase.CHARGE ? var7.born() : var7.refreshed()));
                        if (ZheergenArcanePlayback.alive(var9, var8.spell().lifetime(var8.phase()))) {
                            String var11 = Math.round(var8.start().x * 32.0)
                                + "/"
                                + Math.round(var8.start().y * 32.0)
                                + "/"
                                + Math.round(var8.start().z * 32.0);
                            String var12 = var8.spell().id + "/" + var8.owner() + "/" + var11;
                            if (var8.phase() == Phase.BEAM) {
                                var12 = var12 + "/beam/" + var8.end();
                            }

                            OptimizedSpellVisual var13 = (OptimizedSpellVisual)var4.get(var12);
                            if (var13 == null
                                || ZheergenArcanePhases.prefer(var8.phase().name(), var7.refreshed(), var13.packet().phase().name(), var13.refreshed())) {
                                var4.put(var12, var7);
                            }
                        }
                    }
                    continue;
                }

                var3.add(var6);
            }

            if (!var4.isEmpty()) {
                draw(new ArrayList<>(var4.values()), var1, var2);
            }

            return var3;
        } else {
            return var0;
        }
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static void draw(List<OptimizedSpellVisual> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        Minecraft var3 = Minecraft.getInstance();
        if (var3.level == var2) {
            Vec3 var4 = var1.getCamera().getPosition();
            double[] var5 = new double[]{var4.x, var4.y, var4.z};
            float var6 = var3.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
            Vec3 var7 = var1.getCamera().getEntity() == null ? new Vec3(0.0, 0.0, 1.0) : var1.getCamera().getEntity().getViewVector(var6);
            double[] var8 = new double[]{var7.x, var7.y, var7.z};
            boolean var9 = !var1.getCamera().isDetached();
            ParticleStatus var10 = var3.options.particles().get();
            int var11 = var10 == ParticleStatus.MINIMAL ? 4000 : (var10 == ParticleStatus.DECREASED ? 8000 : 16000);
            ArrayList var12 = new ArrayList();

            for (AbstractClientPlayer var14 : var2.players()) {
                if (!(var14.position().distanceToSqr(var4) > 900.0)) {
                    AABB var15 = var14.getBoundingBox().inflate(0.08);
                    var12.add(new double[]{var15.minX, var15.minY, var15.minZ, var15.maxX, var15.maxY, var15.maxZ});
                }
            }

            var0.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var4)));
            ArrayList<OptimizedSpellVisual> var58 = new ArrayList<>();

            for (OptimizedSpellVisual var61 : var0) {
                SpellVisualPacket var16 = var61.packet();
                boolean var17 = var16.phase() == Phase.CHARGE;
                double var18 = (double)Math.max(0.0F, (float)(var2.getGameTime() - (var17 ? var61.born() : var61.refreshed())) + var6);
                if (!(var18 >= (double)var16.spell().lifetime(var16.phase())) && !(var16.start().distanceToSqr(var4) > 3600.0)) {
                    ZheergenArcaneData.Shape var20 = ZheergenArcaneData.shape(var16.spell().id);
                    boolean var21 = var10 == ParticleStatus.ALL && var16.start().distanceToSqr(var4) < 225.0;
                    int var22 = var16.phase() == Phase.BEAM ? 6 : (var21 ? var20.surfaces().size() : var20.essential()) + (var17 ? 12 : 0);
                    if (var22 <= var11) {
                        var58.add(var61);
                        var11 -= var22;
                    }
                }
            }

            Collections.reverse(var58);
            BufferSource var60 = var3.renderBuffers().bufferSource();
            VertexConsumer var62 = var60.getBuffer(VOLUME);
            VertexConsumer var63 = var60.getBuffer(LIGHT);
            Matrix4f var64 = var1.getPoseStack().last().pose();
            Matrix3f var65 = var1.getPoseStack().last().normal();

            for (OptimizedSpellVisual var66 : var58) {
                SpellVisualPacket var67 = var66.packet();
                boolean var68 = var67.phase() == Phase.CHARGE;
                double var23 = (double)Math.max(0.0F, (float)(var2.getGameTime() - (var68 ? var66.born() : var66.refreshed())) + var6);
                int var25 = var67.spell().lifetime(var67.phase());
                if (ZheergenArcanePlayback.alive(var23, var25)) {
                    float var26 = ZheergenArcanePlayback.envelope(var23, var25);
                    Entity var27 = var2.getEntity(var67.ownerId());
                    if (var27 != null && !var27.getUUID().equals(var67.owner())) {
                        var27 = null;
                    }

                    if (var67.phase() == Phase.BEAM) {
                        trace(var63, var64, var65, var67.start(), var67.end(), var2, var27, var26, var5, var8, var9, var12);
                    } else {
                        ZheergenArcaneData.Shape var28 = ZheergenArcaneData.shape(var67.spell().id);
                        Vec3 var29 = var67.start();
                        if (var28 != null && !(var29.distanceToSqr(var4) > 3600.0)) {
                            double var30 = SpellVisuals.radiusScale(var27, var67.spell().id);
                            if (!Double.isFinite(var30) || var30 <= 0.0) {
                                var30 = 1.0;
                            }

                            double var32 = var28.scale() * VisualScale.scale(var30);
                            double var34 = ZheergenArcanePlayback.elevation(var28.height(), var28.lower()[1], var28.upper()[1], var32);
                            double var36 = var67.end().x - var29.x;
                            double var38 = var67.end().z - var29.z;
                            double var40 = Math.hypot(var36, var38);
                            if (var40 > 1.0E-5) {
                                var36 /= var40;
                                var38 /= var40;
                            } else {
                                var36 = 0.0;
                                var38 = 1.0;
                            }

                            double var42 = Math.hypot(
                                    Math.max(Math.abs(var28.lower()[0]), Math.abs(var28.upper()[0])),
                                    Math.max(Math.abs(var28.lower()[2]), Math.abs(var28.upper()[2]))
                                )
                                * var32;
                            AABB var44 = new AABB(
                                var29.x - var42,
                                var29.y + var34 + var28.lower()[1] * var32,
                                var29.z - var42,
                                var29.x + var42,
                                var29.y + var34 + var28.upper()[1] * var32,
                                var29.z + var42
                            );
                            if (var1.getFrustum().isVisible(var44)) {
                                if (var68) {
                                    warning(var63, var64, var65, var29, var2, var27, var67.spell().radius * var30, var26, var5);
                                }

                                boolean var45 = var10 == ParticleStatus.ALL && var29.distanceToSqr(var4) < 225.0;
                                ZheergenArcaneMotion.Frame[] var46 = new ZheergenArcaneMotion.Frame[45];

                                for (int var47 = 20; var47 <= 44; var47++) {
                                    var46[var47] = ZheergenArcaneMotion.frame(var47, var23, var68);
                                }

                                double[] var71 = new double[45];

                                for (int var48 = 33; var48 <= 37; var48++) {
                                    var71[var48] = ZheergenArcaneMotion.frame(var48, Math.max(0.0, var23 - 1.8), var68).alpha();
                                }

                                double[][] var72 = new double[4][3];
                                double[] var49 = new double[3];
                                float[] var50 = new float[4];

                                for (ZheergenArcaneData.Surface var52 : var28.surfaces()) {
                                    if (!var52.detail() || var45) {
                                        ZheergenArcaneMotion.Frame var53 = var46[var52.channel()];

                                        for (int var54 = 0; var54 < 4; var54++) {
                                            ZheergenArcaneMotion.position(var52.positions()[var54], var52.pivot(), var53, var49);
                                            var72[var54][0] = var29.x + (var49[0] * var38 + var49[2] * var36) * var32;
                                            var72[var54][1] = var29.y + var34 + var49[1] * var32;
                                            var72[var54][2] = var29.z + (var49[2] * var38 - var49[0] * var36) * var32;
                                        }

                                        double var73 = var52.alphaLag() > 0.0 && !var68 ? var71[var52.channel()] : var53.alpha();
                                        float var56 = var26 * var52.opacity() * (float)var73 * protection(var72, var5, var8, false, var12);

                                        for (int var57 = 0; var57 < 4; var57++) {
                                            var50[var57] = var56 * (var9 ? ZheergenArcaneSafety.sightVertex(var72[var57], var5, var8) : 1.0F);
                                        }

                                        if (var56 > 0.012F) {
                                            quad(var52.opacity() >= 0.95F ? var62 : var63, var64, var65, var72, var52.uv(), var52.tint(), var50, var5);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var60.endBatch(VOLUME);
            var60.endBatch(LIGHT);
        }
    }

    private static float protection(double[][] var0, double[] var1, double[] var2, boolean var3, List<double[]> var4) {
        float var5 = 1.0F;

        for (double[] var7 : var4) {
            var5 = Math.min(var5, ZheergenArcaneSafety.actor(var0, var7));
        }

        return var5 * ZheergenArcaneSafety.camera(var0, var1) * (var3 ? ZheergenArcaneSafety.sight(var0, var1, var2) : 1.0F);
    }

    private static void quad(VertexConsumer var0, Matrix4f var1, Matrix3f var2, double[][] var3, double[][] var4, int var5, float var6, double[] var7) {
        quad(var0, var1, var2, var3, var4, var5, new float[]{var6, var6, var6, var6}, var7);
    }

    private static void quad(VertexConsumer var0, Matrix4f var1, Matrix3f var2, double[][] var3, double[][] var4, int var5, float[] var6, double[] var7) {
        double var8 = var3[1][0] - var3[0][0];
        double var10 = var3[1][1] - var3[0][1];
        double var12 = var3[1][2] - var3[0][2];
        double var14 = var3[2][0] - var3[0][0];
        double var16 = var3[2][1] - var3[0][1];
        double var18 = var3[2][2] - var3[0][2];
        double var20 = var10 * var18 - var12 * var16;
        double var22 = var12 * var14 - var8 * var18;
        double var24 = var8 * var16 - var10 * var14;
        double var26 = Math.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
        if (!(var26 < 1.0E-12)) {
            for (int var28 = 0; var28 < 4; var28++) {
                int var29 = Math.min(255, Math.max(0, Math.round(var6[var28] * 255.0F)));
                net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var0.addVertex(var1, (float)(var3[var28][0] - var7[0]), (float)(var3[var28][1] - var7[1]), (float)(var3[var28][2] - var7[2]))
                    .setColor(var5 >> 16 & 0xFF, var5 >> 8 & 0xFF, var5 & 0xFF, var29)
                    .setUv((float)var4[var28][0], (float)var4[var28][1])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    , var2, (float)(var20 / var26), (float)(var22 / var26), (float)(var24 / var26));
            }
        }
    }

    private static void trace(
        VertexConsumer var0,
        Matrix4f var1,
        Matrix3f var2,
        Vec3 var3,
        Vec3 var4,
        ClientLevel var5,
        Entity var6,
        float var7,
        double[] var8,
        double[] var9,
        boolean var10,
        List<double[]> var11
    ) {
        BlockHitResult var12 = var5.clip(new ClipContext(var3, var4, Block.COLLIDER, Fluid.NONE, var6));
        Vec3 var13 = var12.getType() == Type.MISS ? var4 : var12.getLocation();
        double[] var14 = new double[]{var13.x - var3.x, var13.y - var3.y, var13.z - var3.z};
        double var15 = Math.sqrt(var14[0] * var14[0] + var14[1] * var14[1] + var14[2] * var14[2]);
        if (!(var15 < 0.02) && !(var15 > 64.0)) {
            for (int var17 = 0; var17 < 3; var17++) {
                var14[var17] /= var15;
            }

            double var27 = Math.hypot(var14[0], var14[2]);
            double[] var19 = var27 > 1.0E-4 ? new double[]{-var14[2] / var27, 0.0, var14[0] / var27} : new double[]{1.0, 0.0, 0.0};
            double[] var20 = new double[]{
                var14[1] * var19[2] - var14[2] * var19[1], var14[2] * var19[0] - var14[0] * var19[2], var14[0] * var19[1] - var14[1] * var19[0]
            };
            double[][] var21 = new double[8][3];

            for (int var22 = 0; var22 < 8; var22++) {
                Vec3 var23 = var22 < 4 ? var3 : var13;
                double[] var24 = new double[]{var23.x, var23.y, var23.z};

                for (int var25 = 0; var25 < 3; var25++) {
                    var21[var22][var25] = var24[var25]
                        + var19[var25] * (var22 % 4 < 2 ? -0.018 : 0.018)
                        + var20[var25] * (var22 % 4 != 0 && var22 % 4 != 3 ? 0.014 : -0.014);
                }
            }

            for (int[] var31 : new int[][]{{0, 1, 2, 3}, {7, 6, 5, 4}, {0, 4, 5, 1}, {1, 5, 6, 2}, {2, 6, 7, 3}, {3, 7, 4, 0}}) {
                double[][] var26 = new double[][]{var21[var31[0]], var21[var31[1]], var21[var31[2]], var21[var31[3]]};
                quad(var0, var1, var2, var26, TRACE_UV, 14995967, var7 * 0.66F * protection(var26, var8, var9, var10, var11), var8);
            }
        }
    }

    private static void warning(
        VertexConsumer var0, Matrix4f var1, Matrix3f var2, Vec3 var3, ClientLevel var4, Entity var5, double var6, float var8, double[] var9
    ) {
        if (Double.isFinite(var6) && !(var6 <= 0.0)) {
            double var10 = Math.min(16.0, var6);
            Vec3 var12 = new Vec3(var3.x, var3.y + 0.25, var3.z);
            Vec3 var13 = new Vec3(var3.x, var3.y - 4.0, var3.z);
            BlockHitResult var14 = var4.clip(new ClipContext(var12, var13, Block.COLLIDER, Fluid.NONE, var5));
            if (var14.getType() != Type.MISS) {
                double var15 = var14.getLocation().y + 0.018;

                for (int var17 = 0; var17 < 3; var17++) {
                    for (int var18 = 0; var18 < 4; var18++) {
                        double var19 = (double)var17 * Math.PI * 2.0 / 3.0 + (double)var18 * 0.055;
                        double var21 = var19 + 0.055;
                        double[][] var23 = new double[][]{
                            {var3.x + Math.cos(var19) * var10, var15, var3.z + Math.sin(var19) * var10},
                            {var3.x + Math.cos(var19) * (var10 - 0.028), var15, var3.z + Math.sin(var19) * (var10 - 0.028)},
                            {var3.x + Math.cos(var21) * (var10 - 0.028), var15, var3.z + Math.sin(var21) * (var10 - 0.028)},
                            {var3.x + Math.cos(var21) * var10, var15, var3.z + Math.sin(var21) * var10}
                        };
                        quad(var0, var1, var2, var23, TRACE_UV, 11837903, var8 * 0.44F, var9);
                    }
                }
            }
        }
    }
}
