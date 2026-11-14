package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import org.joml.Quaterniond;
import org.joml.Vector3f;

public final class GongdiSpellRenderer {
    private GongdiSpellRenderer() {
    }

    private static Map<String, RenderType> surfaces() {
        LinkedHashMap var0 = new LinkedHashMap();

        for (String var2 : List.of("metal", "wave", "cut", "blood", "blood_fine", "trace", "glow")) {
            ResourceLocation var3 = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/gongdi/" + var2 + ".png");
            var0.put(var2, var2.equals("glow") ? GongdiSpellGlowType.layer(var3) : RenderType.entityTranslucentEmissive(var3));
        }

        return Map.copyOf(var0);
    }

    public static boolean owns(String var0) {
        return var0.equals("rending_cleave") || var0.equals("crimson_oath") || var0.equals("blood_verdict");
    }

    public static void render(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (var1.getStage() != Stage.AFTER_PARTICLES) {
            OptimizedSpellRenderer.render(var0, var1, var2);
        } else {
            ArrayList var3 = new ArrayList();
            ArrayList<OptimizedSpellVisual> var4 = new ArrayList<>();

            for (Object var6 : var0) {
                if (var6 instanceof OptimizedSpellVisual var7 && owns(var7.packet().spell().id)) {
                    var4.add(var7);
                    continue;
                }

                var3.add(var6);
            }

            OptimizedSpellRenderer.render(var3, var1, var2);
            if (!var4.isEmpty()) {
                Minecraft var70 = Minecraft.getInstance();
                ClientLevel var71 = var70.level;
                if (var71 != null && var71 == var2) {
                    Vec3 var72 = var1.getCamera().getPosition();
                    boolean var8 = !var1.getCamera().isDetached();
                    Entity var9 = var1.getCamera().getEntity();
                    float var10 = var70.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
                    ParticleStatus var11 = var70.options.particles().get();
                    int var12 = var11 == ParticleStatus.MINIMAL ? 1 : (var11 == ParticleStatus.DECREASED ? 2 : 4);
                    var4.removeIf(var1x -> !var1x.packet().valid() || !var71.dimension().location().equals(var1x.packet().dimension()));
                    var4.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var72)));
                    BufferSource var13 = var70.renderBuffers().bufferSource();
                    GongdiSpellRenderer.Painter var14 = new GongdiSpellRenderer.Painter(
                        var1.getPoseStack().last().pose(),
                        var72,
                        var8,
                        var11 == ParticleStatus.MINIMAL ? 14000 : (var11 == ParticleStatus.DECREASED ? 28000 : '\udac0')
                    );
                    Vector3f var15 = var1.getCamera().getLookVector();
                    var14.look = new double[]{(double)var15.x, (double)var15.y, (double)var15.z};
                    if (var9 != null && !var8) {
                        var14.actor(bounds(var9.getBoundingBox()));
                    }

                    int var16 = 0;

                    for (OptimizedSpellVisual var18 : var4) {
                        if (var16 >= var12 || var14.written >= var14.limit) {
                            break;
                        }

                        SpellVisualPacket var19 = var18.packet();
                        double var20 = (double)Math.max(0.0F, (float)(var71.getGameTime() - var18.born()) + var10);
                        double var22 = (double)Math.max(0.0F, (float)(var71.getGameTime() - var18.refreshed()) + var10);
                        double var24 = (double)var19.spell().lifetime(var19.phase());
                        boolean var26 = var19.phase() == Phase.CHARGE;
                        if ((var26 || !(var20 >= var24)) && !(var22 >= var24)) {
                            float var27 = (float)Math.min(1.0, (var24 - (var26 ? var22 : var20)) / Math.min(3.0, var24 * 0.25));
                            Entity var28 = var71.getEntity(var19.ownerId());
                            if (var28 != null && !var28.getUUID().equals(var19.owner())) {
                                var28 = null;
                            }

                            if (var28 != null) {
                                var14.actor(bounds(var28.getBoundingBox()));
                            }

                            if (var19.phase() == Phase.BEAM) {
                                if (var19.spell().id.equals("rending_cleave")) {
                                    BlockHitResult var78 = var71.clip(new ClipContext(var19.start(), var19.end(), Block.COLLIDER, Fluid.NONE, var28));
                                    Vec3 var79 = var78.getType() == Type.MISS ? var19.end() : var78.getLocation();
                                    if (!(var19.start().distanceToSqr(var79) > 16384.0)) {
                                        var14.beam("wave", var19.start(), var79, 0.15, var27 * 0.74F);
                                        var16++;
                                    }
                                }
                            } else {
                                GongdiSpellModel.Model var29 = GongdiSpellModel.find(var19.spell().id);
                                Vec3 var30 = var29.anchor().equals("CASTER") && var28 != null ? var28.getPosition(var10) : var19.start();
                                if (!(var30.distanceToSqr(var72) > 4096.0)
                                    && (var1.getFrustum() == null || var1.getFrustum().isVisible(new AABB(var30, var30).inflate(14.0)))) {
                                    for (Entity var32 : var71.getEntities(
                                        var28, new AABB(var30, var30).inflate(8.0), var2x -> var2x instanceof LivingEntity && (!var8 || var2x != var9)
                                    )) {
                                        var14.actor(bounds(var32.getBoundingBox()));
                                    }

                                    Vec3 var80 = var19.end().subtract(var19.start());
                                    if (var80.x * var80.x + var80.z * var80.z < 1.0E-5 && var28 != null) {
                                        var80 = var28.getViewVector(var10);
                                    }

                                    double var81 = Math.atan2(var80.x, var80.z);
                                    double var34 = var28 != null ? Math.max(0.6, (double)Math.min(12.0F, var28.getBbWidth())) : 0.6;
                                    double var36 = SpellVisuals.radiusScale(var28, var19.spell().id);
                                    double var38 = Double.isFinite(var36) && var36 > 0.0 ? Math.max(0.78, Math.min(1.65, var36)) : 1.0;

                                    for (GongdiSpellModel.Part var43 : var29.parts()) {
                                        GongdiSpellModel.State var44 = GongdiSpellModel.sample(var43, var19.phase().name(), Math.min(1.0, var20 / var24));
                                        var44 = GongdiVerdictPlacement.stateForCamera(var44, var29, var43, var30, var72, var81, var38, var8);
                                        if (!(var44.alpha() < 0.01)) {
                                            Quaterniond var45 = GongdiSpellModel.rotation(var44);
                                            double[][][] var46 = new double[var43.faces().length][4][];
                                            double var47 = Double.POSITIVE_INFINITY;

                                            for (int var49 = 0; var49 < var43.faces().length; var49++) {
                                                for (int var50 = 0; var50 < 4; var50++) {
                                                    double[] var51 = GongdiSpellModel.pose(var43.faces()[var49].points()[var50], var43, var44, var45);
                                                    var46[var49][var50] = var51;
                                                    var47 = Math.min(var47, var51[1]);
                                                }
                                            }

                                            double var83 = Math.max(0.0, (var43.telegraph() ? 0.04 : 0.12) - var47);
                                            double var84 = (double)var43.sideSign() * Math.max(0.0, var34 / 2.0 - 0.3);
                                            double var53 = var29.id().equals("rending_cleave") ? var34 / 2.0 + 0.48 : 0.0;
                                            double var55 = Math.sin(var81);
                                            double var57 = Math.cos(var81);

                                            for (int var59 = 0; var59 < var43.faces().length; var59++) {
                                                GongdiSpellModel.Face var60 = var43.faces()[var59];
                                                double[][] var61 = var46[var59];

                                                for (double[] var65 : var61) {
                                                    double var66 = (var65[0] + var84) * var38;
                                                    double var68 = (var65[2] + var53) * var38;
                                                    var65[0] = var30.x + var66 * var57 + var68 * var55;
                                                    var65[1] = var30.y + (var65[1] + var83) * var38;
                                                    var65[2] = var30.z - var66 * var55 + var68 * var57;
                                                }

                                                var14.queue(
                                                    var60.material(),
                                                    var61,
                                                    var60.uv(),
                                                    var60.tint(),
                                                    (float)((double)var27 * var44.alpha() * var60.alpha()),
                                                    GongdiVerdictPlacement.avoidActors(!var43.telegraph(), var29, var43)
                                                );
                                            }
                                        }
                                    }

                                    var16++;
                                }
                            }
                        }
                    }

                    for (Entry var74 : var14.batches.entrySet()) {
                        var14.solidWeapon = ((String)var74.getKey()).equals("metal");
                        RenderType var75 = GongdiSpellRenderer.Surfaces.ALL.get(var74.getKey());
                        VertexConsumer var76 = var13.getBuffer(var75);

                        for (GongdiSpellRenderer.Painter.Draw var77 : (List<GongdiSpellRenderer.Painter.Draw>)var74.getValue()) {
                            var14.draw(var76, var77.points(), var77.uv(), var77.tint(), var77.alpha(), var77.avoidActors());
                        }

                        var13.endBatch(var75);
                    }
                }
            }
        }
    }

    private static double[] bounds(AABB var0) {
        return new double[]{var0.minX - 0.035, var0.minY - 0.03, var0.minZ - 0.035, var0.maxX + 0.035, var0.maxY + 0.04, var0.maxZ + 0.035};
    }

    public static double separation(double[][] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Double.POSITIVE_INFINITY;
            double var7 = Double.NEGATIVE_INFINITY;

            for (double[] var12 : var0) {
                var5 = Math.min(var5, var12[var4]);
                var7 = Math.max(var7, var12[var4]);
            }

            double var13 = Math.max(0.0, Math.max(var1[var4] - var7, var5 - var1[var4 + 3]));
            var2 += var13 * var13;
        }

        return Math.sqrt(var2);
    }

    public static double[] blend(double[][] var0, double var1, double var3) {
        double[] var5 = new double[var0[0].length];

        for (int var6 = 0; var6 < var5.length; var6++) {
            var5[var6] = var0[0][var6] * (1.0 - var1) * (1.0 - var3)
                + var0[1][var6] * var1 * (1.0 - var3)
                + var0[2][var6] * var1 * var3
                + var0[3][var6] * (1.0 - var1) * var3;
        }

        return var5;
    }

    private static final class Painter {
        private final Matrix4f matrix;
        private final Matrix3f normal = new Matrix3f();
        private final double[] camera;
        private double[] look = new double[]{0.0, 0.0, 1.0};
        private boolean solidWeapon;
        private final double[] cameraBox;
        private final boolean firstPerson;
        private final int limit;
        private int written;
        private final List<double[]> actors = new ArrayList<>();
        private final Map<String, List<GongdiSpellRenderer.Painter.Draw>> batches = new LinkedHashMap<>();

        private void actor(double[] var1) {
            if (this.actors.size() < 96 && this.actors.stream().noneMatch(var1x -> Arrays.equals(var1x, var1))) {
                this.actors.add(var1);
            }
        }

        private Painter(Matrix4f var1, Vec3 var2, boolean var3, int var4) {
            this.matrix = var1;
            this.camera = new double[]{var2.x, var2.y, var2.z};
            this.cameraBox = new double[]{this.camera[0], this.camera[1], this.camera[2], this.camera[0], this.camera[1], this.camera[2]};
            this.firstPerson = var3;
            this.limit = var4;
        }

        private void queue(String var1, double[][] var2, double[][] var3, int var4, float var5, boolean var6) {
            if ((double)var5 >= 0.009) {
                this.batches.computeIfAbsent(var1, var0 -> new ArrayList<>()).add(new GongdiSpellRenderer.Painter.Draw(var2, var3, var4, var5, var6));
            }
        }

        private void draw(VertexConsumer var1, double[][] var2, double[][] var3, int var4, float var5, boolean var6) {
            if (!((double)var5 < 0.009) && this.written < this.limit) {
                double var7 = GongdiSpellRenderer.separation(var2, this.cameraBox);
                int var9 = 1;
                int var10 = 1;
                if (!this.firstPerson && var7 < 1.3) {
                    var9 = Math.min(24, Math.max(1, (int)Math.ceil(Math.max(edge(var2[0], var2[1]), edge(var2[3], var2[2])) / 0.42)));
                    var10 = Math.min(12, Math.max(1, (int)Math.ceil(Math.max(edge(var2[0], var2[3]), edge(var2[1], var2[2])) / 0.42)));
                }

                if (var9 == 1 && var10 == 1) {
                    this.face(var1, var2, var3, var4, var5, var6);
                } else {
                    for (int var11 = 0; var11 < var10 && this.written < this.limit; var11++) {
                        for (int var12 = 0; var12 < var9 && this.written < this.limit; var12++) {
                            double var13 = (double)var12 / (double)var9;
                            double var15 = (double)(var12 + 1) / (double)var9;
                            double var17 = (double)var11 / (double)var10;
                            double var19 = (double)(var11 + 1) / (double)var10;
                            double[][] var21 = new double[][]{
                                GongdiSpellRenderer.blend(var2, var13, var17),
                                GongdiSpellRenderer.blend(var2, var15, var17),
                                GongdiSpellRenderer.blend(var2, var15, var19),
                                GongdiSpellRenderer.blend(var2, var13, var19)
                            };
                            double[][] var22 = new double[][]{
                                GongdiSpellRenderer.blend(var3, var13, var17),
                                GongdiSpellRenderer.blend(var3, var15, var17),
                                GongdiSpellRenderer.blend(var3, var15, var19),
                                GongdiSpellRenderer.blend(var3, var13, var19)
                            };
                            this.face(var1, var21, var22, var4, var5, var6);
                        }
                    }
                }
            }
        }

        private void face(VertexConsumer var1, double[][] var2, double[][] var3, int var4, float var5, boolean var6) {
            double var7 = GongdiSpellRenderer.separation(var2, this.cameraBox);
            double var9 = this.firstPerson ? 0.62 : 0.12;
            double var11 = this.firstPerson ? 1.17 : 0.3;
            if (!this.firstPerson) {
                var5 = (float)((double)var5 * smooth(var9, var11, var7));
            }

            if (var6) {
                for (double[] var14 : this.actors) {
                    if (GongdiSpellRenderer.separation(var2, var14) < 0.07) {
                        return;
                    }
                }
            }

            if (!((double)var5 < 0.009) && this.written < this.limit) {
                double[] var19 = subtract(var2[1], var2[0]);
                double[] var20 = subtract(var2[3], var2[0]);
                double[] var15 = new double[]{
                    var19[1] * var20[2] - var19[2] * var20[1], var19[2] * var20[0] - var19[0] * var20[2], var19[0] * var20[1] - var19[1] * var20[0]
                };
                double var16 = Math.sqrt(var15[0] * var15[0] + var15[1] * var15[1] + var15[2] * var15[2]);
                if (!(var16 < 1.0E-7)) {
                    for (int var18 = 0; var18 < 4; var18++) {
                        net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var1.addVertex(
                                this.matrix,
                                (float)(var2[var18][0] - this.camera[0]),
                                (float)(var2[var18][1] - this.camera[1]),
                                (float)(var2[var18][2] - this.camera[2])
                            )
                            .setColor(var4 >> 16 & 0xFF, var4 >> 8 & 0xFF, var4 & 0xFF, Math.min(255, Math.round(var5 * 255.0F)))
                            .setUv((float)var3[var18][0], (float)var3[var18][1])
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(15728880)
                            , this.normal, (float)(var15[0] / var16), (float)(var15[1] / var16), (float)(var15[2] / var16));
                    }

                    this.written++;
                }
            }
        }

        private void beam(String var1, Vec3 var2, Vec3 var3, double var4, float var6) {
            double[] var7 = new double[]{var3.x - var2.x, var3.y - var2.y, var3.z - var2.z};
            double var8 = Math.sqrt(var7[0] * var7[0] + var7[1] * var7[1] + var7[2] * var7[2]);
            if (!(var8 < 0.02)) {
                double var10 = Math.hypot(var7[0], var7[2]);
                double[] var12 = var10 > 1.0E-4 ? new double[]{-var7[2] / var10 * var4, 0.0, var7[0] / var10 * var4} : new double[]{var4, 0.0, 0.0};
                double[][] var13 = new double[][]{
                    {var2.x - var12[0], var2.y, var2.z - var12[2]},
                    {var3.x - var12[0], var3.y, var3.z - var12[2]},
                    {var3.x + var12[0], var3.y, var3.z + var12[2]},
                    {var2.x + var12[0], var2.y, var2.z + var12[2]}
                };
                this.queue(var1, var13, new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, 16777215, var6, false);
            }
        }

        private static double edge(double[] var0, double[] var1) {
            return Math.sqrt(Math.pow(var0[0] - var1[0], 2.0) + Math.pow(var0[1] - var1[1], 2.0) + Math.pow(var0[2] - var1[2], 2.0));
        }

        private static double smooth(double var0, double var2, double var4) {
            double var6 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
            return var6 * var6 * (3.0 - 2.0 * var6);
        }

        private static double[] subtract(double[] var0, double[] var1) {
            return new double[]{var0[0] - var1[0], var0[1] - var1[1], var0[2] - var1[2]};
        }

        private static record Draw(double[][] points, double[][] uv, int tint, float alpha, boolean avoidActors) {
        }
    }

    private static final class Surfaces {
        private static final Map<String, RenderType> ALL = GongdiSpellRenderer.surfaces();
    }
}
