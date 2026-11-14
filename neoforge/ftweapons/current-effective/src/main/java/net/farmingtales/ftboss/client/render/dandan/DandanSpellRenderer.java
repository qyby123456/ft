package net.farmingtales.ftboss.client.render.dandan;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class DandanSpellRenderer {
    private static final Map<String, DandanSpellRenderer.Model> MODELS = load();
    private static final RenderType LAYER = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/dandan/materials.png"));

    private DandanSpellRenderer() {
    }

    private static double[] vector(JsonArray var0) {
        if (var0.size() != 3) {
            throw new IllegalArgumentException("Invalid Dandan vector");
        } else {
            double[] var1 = new double[3];

            for (int var2 = 0; var2 < 3; var2++) {
                var1[var2] = var0.get(var2).getAsDouble();
                if (!Double.isFinite(var1[var2]) || Math.abs(var1[var2]) > 32.0) {
                    throw new IllegalArgumentException("Invalid Dandan coordinate");
                }
            }

            return var1;
        }
    }

    private static Map<String, DandanSpellRenderer.Model> load() {
        LinkedHashMap var0 = new LinkedHashMap();

        for (String var2 : List.of("candle_reap", "candle_prison", "candle_eclipse")) {
            try (InputStream var3 = DandanSpellRenderer.class.getResourceAsStream("/assets/ftboss/dandan/" + var2 + ".json")) {
                if (var3 == null) {
                    throw new IllegalStateException("Missing independent Dandan model " + var2);
                }

                JsonObject var4 = JsonParser.parseReader(new InputStreamReader(var3, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject var5 = var4.getAsJsonObject("scene");
                double[] var6 = new double[]{32.0, 32.0, 32.0};
                double[] var7 = new double[]{-32.0, -32.0, -32.0};
                ArrayList var8 = new ArrayList();

                for (JsonElement var10 : var4.getAsJsonArray("faces")) {
                    JsonObject var11 = var10.getAsJsonObject();
                    if (var11.getAsJsonArray("points").size() == 4 && var11.getAsJsonArray("uv").size() == 4) {
                        double[][] var12 = new double[4][3];
                        double[][] var13 = new double[4][2];

                        for (int var14 = 0; var14 < 4; var14++) {
                            var12[var14] = vector(var11.getAsJsonArray("points").get(var14).getAsJsonArray());
                            JsonArray var15 = var11.getAsJsonArray("uv").get(var14).getAsJsonArray();
                            if (var15.size() != 2) {
                                throw new IllegalArgumentException("Invalid Dandan UV");
                            }

                            for (int var16 = 0; var16 < 2; var16++) {
                                var13[var14][var16] = var15.get(var16).getAsDouble();
                                if (!Double.isFinite(var13[var14][var16]) || var13[var14][var16] < 0.0 || var13[var14][var16] > 1.0) {
                                    throw new IllegalArgumentException("Dandan UV outside material");
                                }
                            }

                            for (int var23 = 0; var23 < 3; var23++) {
                                var6[var23] = Math.min(var6[var23], var12[var14][var23]);
                                var7[var23] = Math.max(var7[var23], var12[var14][var23]);
                            }
                        }

                        int var21 = var11.get("material").getAsInt();
                        int var22 = var11.get("bone").getAsInt();
                        float var24 = var11.get("opacity").getAsFloat();
                        if (var21 >= 0 && var21 < 16 && Float.isFinite(var24) && !(var24 <= 0.0F) && !(var24 > 1.0F)) {
                            if (var22 == 0 || var22 == 1 || var22 == 5 || var22 == 6 || var22 == 7 || var22 == 9 || var22 >= 10 && var22 <= 14) {
                                var8.add(
                                    new DandanSpellRenderer.Face(
                                        var12,
                                        var13,
                                        vector(var11.getAsJsonArray("pivot")),
                                        var21,
                                        var22,
                                        var24,
                                        var11.get("group").getAsString().equals("detail")
                                    )
                                );
                                continue;
                            }

                            throw new IllegalArgumentException("Invalid Dandan joint");
                        }

                        throw new IllegalArgumentException("Invalid Dandan material");
                    }

                    throw new IllegalArgumentException("Invalid Dandan face");
                }

                double var20 = Math.max(var7[0] - var6[0], Math.max(var7[1] - var6[1], var7[2] - var6[2]));
                if (var20 <= 0.01 || var8.size() > 6000) {
                    throw new IllegalArgumentException("Invalid Dandan geometry budget");
                }

                var0.put(
                    var2,
                    new DandanSpellRenderer.Model(
                        var2, var5.get("anchor").getAsString(), var5.get("span").getAsDouble(), var5.get("height").getAsDouble(), var20, List.copyOf(var8)
                    )
                );
            } catch (Exception var19) {
                throw new IllegalStateException("Cannot load independently authored Dandan model " + var2, var19);
            }
        }

        return Map.copyOf(var0);
    }

    public static List<?> renderAndExclude(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (var1.getStage() == Stage.AFTER_PARTICLES && !var0.isEmpty()) {
            Minecraft var3 = Minecraft.getInstance();
            ClientLevel var4 = var3.level;
            if (var4 != null && var4 == var2) {
                Vec3 var5 = var1.getCamera().getPosition();
                double[] var6 = new double[]{var5.x, var5.y, var5.z};
                Entity var7 = var1.getCamera().getEntity();
                boolean var8 = !var1.getCamera().isDetached() && var7 != null;
                Vec3 var9 = var7 == null ? new Vec3(0.0, 0.0, 1.0) : var7.getLookAngle();
                double[] var10 = new double[]{var9.x, var9.y, var9.z};
                ArrayList var11 = new ArrayList();
                ArrayList var12 = new ArrayList();

                for (Object var14 : var0) {
                    if (var14 instanceof OptimizedSpellVisual var15
                        && var15.packet() != null
                        && var15.packet().spell() != null
                        && MODELS.containsKey(var15.packet().spell().id)) {
                        SpellVisualPacket var16 = var15.packet();
                        if (var16.valid() && var16.dimension().equals(var4.dimension().location())) {
                            double var17 = var16.start().distanceToSqr(var5);
                            if (var17 < 6400.0) {
                                var12.add(new DandanSpellRenderer.Draw(var15, var17));
                            }
                        }
                        continue;
                    }

                    var11.add(var14);
                }

                if (var12.isEmpty()) {
                    return var11;
                } else {
                    var12.sort(Comparator.comparingDouble(DandanSpellRenderer.Draw::distance));
                    ParticleStatus var61 = var3.options.particles().get();
                    int var62 = var61 == ParticleStatus.MINIMAL ? 2 : (var61 == ParticleStatus.DECREASED ? 4 : 6);
                    int var63 = var61 == ParticleStatus.MINIMAL ? 6000 : (var61 == ParticleStatus.DECREASED ? 12000 : 18000);
                    BufferSource var64 = var3.renderBuffers().bufferSource();
                    VertexConsumer var65 = var64.getBuffer(LAYER);
                    Matrix4f var18 = var1.getPoseStack().last().pose();
                    Matrix3f var19 = var1.getPoseStack().last().normal();
                    float var20 = var3.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
                    ArrayList<double[]> var21 = new ArrayList<>();

                    for (AbstractClientPlayer var23 : var4.players()) {
                        if (var23.getPosition(var20).distanceToSqr(var5) < 4096.0) {
                            var21.add(actorBox(var23.getBoundingBox().inflate(0.06)));
                        }
                    }

                    int var66 = 0;

                    for (int var67 = Math.min(var62, var12.size()) - 1; var67 >= 0; var67--) {
                        OptimizedSpellVisual var24 = ((DandanSpellRenderer.Draw)var12.get(var67)).visual();
                        SpellVisualPacket var25 = var24.packet();
                        DandanSpellRenderer.Model var26 = MODELS.get(var25.spell().id);
                        Entity var27 = var4.getEntity(var25.ownerId());
                        if (var27 != null && !var27.getUUID().equals(var25.owner())) {
                            var27 = null;
                        }

                        boolean var28 = var25.phase().name().equals("CHARGE");
                        double var29 = (double)Math.max(0.0F, (float)(var4.getGameTime() - var24.born()) + var20);
                        double var31 = (double)Math.max(0.0F, (float)(var4.getGameTime() - var24.refreshed()) + var20);
                        float var33 = (float)DandanMotion.fade(var31, var25.spell().lifetime(var25.phase()));
                        if (!((double)var33 < 0.01)) {
                            Vec3 var34 = var26.anchor().equals("FRONT") && var27 != null ? var27.getPosition(var20) : var25.start();
                            double var35 = var26.span() + (var27 == null ? 0.6 : (double)var27.getBbWidth()) + 0.8;
                            double var37 = var35 + (var27 == null ? 1.8 : (double)var27.getBbHeight()) + var26.height();
                            AABB var39 = new AABB(var34.x - var35, var34.y - var35, var34.z - var35, var34.x + var35, var34.y + var37, var34.z + var35);
                            if (var1.getFrustum().isVisible(var39)) {
                                double var40 = var25.end().x - var25.start().x;
                                double var42 = var25.end().z - var25.start().z;
                                if (var40 * var40 + var42 * var42 < 1.0E-8 && var27 != null) {
                                    var40 = var27.getLookAngle().x;
                                    var42 = var27.getLookAngle().z;
                                }

                                double var44 = Math.hypot(var40, var42);
                                if (!Double.isFinite(var44) || var44 < 1.0E-8) {
                                    var40 = 0.0;
                                    var42 = 1.0;
                                    var44 = 1.0;
                                }

                                var40 /= var44;
                                var42 /= var44;
                                double var46 = var26.span() / var26.nativeSpan() * DandanMotion.emergence(var29, var28);
                                double var48 = var28
                                    ? 15.0 * Math.min(1.0, var29 / (double)Math.max(1, var25.spell().lifetime(var25.phase())))
                                    : 15.0 + 24.166 * Math.min(1.0, var29 / (double)Math.max(1, var25.spell().lifetime(var25.phase())));
                                List<DandanSpellRenderer.Surface> var50 = surfaces(var26, var29, var48, var34, var40, var42, var46, var27, var5);
                                var50.sort(Comparator.comparingDouble(DandanSpellRenderer.Surface::distance).reversed());
                                double[] var51 = var27 == null ? null : actorBox(var27.getBoundingBox().inflate(0.08));

                                for (boolean var55 : new boolean[]{false, true}) {
                                    for (DandanSpellRenderer.Surface var57 : var50) {
                                        if (var57.face().detail() == var55 && var66 < var63) {
                                            float var58 = var33 * var57.face().opacity();
                                            if (!var28 && var26.id().equals("candle_prison") && var57.face().bone() >= 10) {
                                                var58 *= 0.84F + 0.16F * (float)Math.cos(var29 * 0.7 - (double)(var57.face().bone() - 10) * 1.256);
                                            }

                                            if (var28 && var55) {
                                                var58 *= 0.28F + 0.72F * (float)DandanMotion.smooth(var29 / 12.0);
                                            }

                                            var58 *= (float)DandanMotion.cameraFade(DandanMotion.cameraDistance(var57.bounds(), var6));
                                            if (var8 && DandanMotion.blocksSight(var57.bounds(), var6, var10)) {
                                                var58 *= var55 ? 0.45F : 0.16F;
                                            }

                                            for (double[] var60 : var21) {
                                                if (DandanMotion.overlaps(var57.bounds(), var60)) {
                                                    var58 *= 0.12F;
                                                    break;
                                                }
                                            }

                                            if (var51 != null && DandanMotion.overlaps(var57.bounds(), var51)) {
                                                var58 *= 0.15F;
                                            }

                                            if (!(var58 < 0.012F)) {
                                                emit(var65, var18, var19, var57, var5, var58);
                                                var66++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var64.endBatch(LAYER);
                    return var11;
                }
            } else {
                return var0;
            }
        } else {
            return var0;
        }
    }

    private static double[] actorBox(AABB var0) {
        return new double[]{var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ};
    }

    private static List<DandanSpellRenderer.Surface> surfaces(
        DandanSpellRenderer.Model var0, double var1, double var3, Vec3 var5, double var6, double var8, double var10, Entity var12, Vec3 var13
    ) {
        ArrayList var14 = new ArrayList();
        double[] var15 = new double[]{32.0, 32.0, 32.0};
        double[] var16 = new double[]{-32.0, -32.0, -32.0};
        DandanMotion.WeaponPose var17 = DandanMotion.weaponPose(var3);

        for (DandanSpellRenderer.Face var19 : var0.faces()) {
            double[][] var20 = new double[4][3];

            for (int var21 = 0; var21 < 4; var21++) {
                var20[var21] = var19.bone() == 1
                    ? DandanMotion.weaponPoint(var19.points()[var21], var19.pivot(), var17)
                    : DandanMotion.point(var19.points()[var21], var19.bone(), var19.pivot(), var1);

                for (int var22 = 0; var22 < 3; var22++) {
                    var20[var21][var22] = var20[var21][var22] * var10;
                    var15[var22] = Math.min(var15[var22], var20[var21][var22]);
                    var16[var22] = Math.max(var16[var22], var20[var21][var22]);
                }
            }

            var14.add(var20);
        }

        double var37 = Math.max(0.18 - var15[1], var0.height() - (var15[1] + var16[1]) / 2.0);
        double var38 = 0.0;
        if (var0.anchor().equals("FRONT")) {
            var38 = 0.55 + (var12 == null ? 0.3 : (double)(var12.getBbWidth() / 2.0F)) - var15[2];
        }

        if (var0.anchor().equals("ABOVE")) {
            var37 = Math.max(var37, (var12 == null ? 1.8 : (double)var12.getBbHeight()) + 0.65 - var15[1]);
        }

        ArrayList var39 = new ArrayList();

        for (int var23 = 0; var23 < var14.size(); var23++) {
            double[][] var24 = (double[][])var14.get(var23);
            double var25 = 0.0;
            double[] var27 = new double[3];

            for (double[] var31 : var24) {
                double var32 = var31[0];
                double var34 = var31[2] + var38;
                var31[0] = var5.x + var32 * var8 + var34 * var6;
                var31[1] += var5.y + var37;
                var31[2] = var5.z - var32 * var6 + var34 * var8;

                for (int var36 = 0; var36 < 3; var36++) {
                    var27[var36] += var31[var36] / 4.0;
                }
            }

            var25 = Math.pow(var27[0] - var13.x, 2.0) + Math.pow(var27[1] - var13.y, 2.0) + Math.pow(var27[2] - var13.z, 2.0);
            var39.add(new DandanSpellRenderer.Surface(var0.faces().get(var23), var24, DandanMotion.bounds(var24), var25));
        }

        return var39;
    }

    private static void emit(VertexConsumer var0, Matrix4f var1, Matrix3f var2, DandanSpellRenderer.Surface var3, Vec3 var4, float var5) {
        DandanSpellRenderer.Face var6 = var3.face();
        double[][] var7 = var3.points();
        double var8 = var7[1][0] - var7[0][0];
        double var10 = var7[1][1] - var7[0][1];
        double var12 = var7[1][2] - var7[0][2];
        double var14 = var7[2][0] - var7[0][0];
        double var16 = var7[2][1] - var7[0][1];
        double var18 = var7[2][2] - var7[0][2];
        double var20 = var10 * var18 - var12 * var16;
        double var22 = var12 * var14 - var8 * var18;
        double var24 = var8 * var16 - var10 * var14;
        double var26 = Math.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
        if (!(var26 < 1.0E-8)) {
            var20 /= var26;
            var22 /= var26;
            var24 /= var26;
            int var28 = var6.material() != 4 && (var6.material() < 10 || var6.material() == 12 || var6.material() == 13)
                ? (int)(255.0 * (0.72 + 0.28 * Math.abs(var20 * -0.45 + var22 * 0.8 + var24 * -0.4)))
                : 250;

            for (int var29 = 0; var29 < 4; var29++) {
                float var30 = (float)(((double)(var6.material() % 8 * 16 + 2) + var6.uv()[var29][0] * 11.0) / 128.0);
                float var31 = (float)(((double)(var6.material() / 8 * 16 + 2) + var6.uv()[var29][1] * 11.0) / 32.0);
                net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var0.addVertex(var1, (float)(var7[var29][0] - var4.x), (float)(var7[var29][1] - var4.y), (float)(var7[var29][2] - var4.z))
                    .setColor(var28, var28, var28, Math.round(var5 * 255.0F))
                    .setUv(var30, var31)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    , var2, (float)var20, (float)var22, (float)var24);
            }
        }
    }

    private static record Draw(OptimizedSpellVisual visual, double distance) {
    }

    private static record Face(double[][] points, double[][] uv, double[] pivot, int material, int bone, float opacity, boolean detail) {
    }

    private static record Model(String id, String anchor, double span, double height, double nativeSpan, List<DandanSpellRenderer.Face> faces) {
    }

    private static record Surface(DandanSpellRenderer.Face face, double[][] points, double[] bounds, double distance) {
    }
}
