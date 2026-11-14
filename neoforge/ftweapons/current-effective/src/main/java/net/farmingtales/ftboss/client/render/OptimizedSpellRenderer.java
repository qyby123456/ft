package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.client.render.dandan.DandanSpellRenderer;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapRenderer;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneRenderer;
import net.farmingtales.ftboss.dandaniron.client.IronRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class OptimizedSpellRenderer {
    private static final RenderType GLYPHS = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/spells/spell_atlas.png"));
    private static final RenderType MODELS = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "fx/spell_materials.png"));

    private OptimizedSpellRenderer() {
    }

    public static void render(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        var0 = QingyiSpellRenderer.renderAndFilter(var0, var1, var2);
        var0 = IronRenderer.exclude(var0, var1, var2);
        var0 = FlatReapRenderer.intercept(var0, var1, var2);
        var0 = ArcaneRenderer.consume(var0, var1, var2);
        var0 = ZheergenArcaneRenderer.dispatch(var0, var1, var2);
        var0 = DandanSpellRenderer.renderAndExclude(var0, var1, var2);
        if (var1.getStage() == Stage.AFTER_PARTICLES && !var0.isEmpty()) {
            Minecraft var3 = Minecraft.getInstance();
            ClientLevel var4 = var3.level;
            if (var4 != null && var4 == var2) {
                Vec3 var5 = var1.getCamera().getPosition();
                boolean var6 = !var1.getCamera().isDetached();
                Entity var7 = var1.getCamera().getEntity();
                ParticleStatus var8 = var3.options.particles().get();
                int var9 = var8 == ParticleStatus.MINIMAL ? 4 : (var8 == ParticleStatus.DECREASED ? 8 : 12);
                int var10 = var8 == ParticleStatus.MINIMAL ? 12 : (var8 == ParticleStatus.DECREASED ? 24 : 48);
                float var11 = var3.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
                List<OptimizedSpellVisual> var12 = var0.stream()
                    .map(var0x -> (OptimizedSpellVisual)var0x)
                    .filter(var1x -> var1x.packet().valid() && var4.dimension().location().equals(var1x.packet().dimension()))
                    .sorted(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var5)))
                    .limit((long)var10)
                    .toList();
                BufferSource var13 = var3.renderBuffers().bufferSource();
                Matrix4f var14 = var1.getPoseStack().last().pose();
                OptimizedSpellRenderer.Frame var15 = new OptimizedSpellRenderer.Frame(
                    var14, var5, var6, var8 == ParticleStatus.MINIMAL ? 10000 : (var8 == ParticleStatus.DECREASED ? 24000 : '뮀')
                );

                for (AbstractClientPlayer var17 : var4.players()) {
                    if (var17.getPosition(var11).distanceToSqr(var5) < 4096.0) {
                        var15.exclude(var17.getBoundingBox().inflate(0.12));
                    }
                }

                for (OptimizedSpellVisual var51 : var12) {
                    Entity var18 = owner(var4, var51.packet());
                    if (var18 != null) {
                        var15.exclude(var18.getBoundingBox().inflate(0.12));
                    }
                }

                HashSet var50 = new HashSet();
                ArrayList<Runnable> var52 = new ArrayList<>();
                VertexConsumer var53 = var13.getBuffer(MODELS);

                for (int var19 = Math.min(var9, var12.size()) - 1; var19 >= 0; var19--) {
                    OptimizedSpellVisual var20 = (OptimizedSpellVisual)var12.get(var19);
                    SpellVisualPacket var21 = var20.packet();
                    Entity var22 = owner(var4, var21);
                    double var23 = (double)((float)(var4.getGameTime() - var20.born()) + var11);
                    double var25 = (double)((float)(var4.getGameTime() - var20.refreshed()) + var11);
                    float var27 = SpellVisualRules.modelOpacity(var25, var21.spell().lifetime(var21.phase())) * 0.88F;
                    if (!(var27 <= 0.0F)) {
                        boolean var28 = var21.phase() == Phase.BEAM;
                        OptimizedSpellModelLibrary.Model var29 = OptimizedSpellModelLibrary.find(var21.spell().id);
                        boolean var30 = var21.spell().followsCaster(var21.phase()) || var29 != null && OptimizedSpellPlacement.casterSpace(var29.scene());
                        Vec3 var31 = !var28 && var30 && var22 != null ? var22.getPosition(var11) : var21.start();
                        Vec3 var32 = var28 ? clip(var4, var22, var21.start(), var21.end()) : var31;
                        float var33 = (float)SpellVisuals.radiusScale(var22, var21.spell().id);
                        if (!Float.isFinite(var33) || var33 <= 0.0F) {
                            var33 = 1.0F;
                        }

                        AABB var34 = new AABB(var31, var32).inflate(Math.max(16.0, var21.spell().radius * (double)var33));
                        if (!(var34.distanceToSqr(var5) > 4096.0) && var1.getFrustum().isVisible(var34)) {
                            Vec3 var35 = var32.subtract(var21.start());
                            if (var28) {
                                double var36 = var35.length();
                                if (!(var36 < 0.015) && !(var36 > 128.0)) {
                                    OptimizedSpellRenderer.Transform var38 = new OptimizedSpellRenderer.Transform(
                                        var21.start(), var35, true, (double)var33, 0.0
                                    );
                                    int var39 = var15.written;
                                    OptimizedSpellModelLibrary.Model var40 = OptimizedSpellModelLibrary.find(var21.spell().id);
                                    if (var40 != null && var40.beamBody().faces() > 0) {
                                        var15.beam(var53, var38, var40.beamBody(), var27, var36, var23);
                                        if (var8 == ParticleStatus.ALL && var19 < 4) {
                                            var52.add(() -> {
                                                if (var15.written + var40.beamDetail().faces() < var15.solidLimit) {
                                                    var15.beam(var53, var38, var40.beamDetail(), var27, var36, var23);
                                                }
                                            });
                                        }
                                    }

                                    if (var15.written > var39) {
                                        var50.add(var19);
                                    }
                                }
                            } else if (true || var7 == null || !var21.owner().equals(var7.getUUID()) || !var21.spell().attached) {
                                OptimizedSpellModelLibrary.Model var69 = OptimizedSpellModelLibrary.find(var21.spell().id);
                                if (var69 != null) {
                                    Vec3 var37 = var21.end().subtract(var21.start());
                                    if (var22 != null && (var21.spell().form.name().equals("WINGS") || var37.x * var37.x + var37.z * var37.z < 1.0E-5)) {
                                        var37 = var22.getViewVector(var11);
                                    }

                                    double var72 = var69.scene().effect().equals("VORTEX")
                                        ? var23 * 0.024
                                        : (var69.scene().effect().equals("ORBIT") ? var23 * 0.014 : 0.0);
                                    Vec3 var73 = ground(var4, var22, var31, false);
                                    OptimizedSpellRenderer.Transform var41 = new OptimizedSpellRenderer.Transform(
                                        var73, var37, false, OptimizedSpellPlacement.scale(var69, (double)var33, var23), var72
                                    );
                                    var41.animate(var21.spell(), var21.phase(), var23);
                                    var41.place(
                                        var69,
                                        var22 != null && var30 ? (double)var22.getBbWidth() : 0.6,
                                        var22 != null && var30 ? (double)var22.getBbHeight() : 1.8
                                    );
                                    float var42 = var27 * (var6 ? var41.hullOpacity(var69, var15.camera) : 1.0F);
                                    var15.group(var53, var41, var69.body(), var42);
                                    if (var8 == ParticleStatus.ALL && var19 < 4 && var31.distanceToSqr(var5) < 144.0) {
                                        var52.add(() -> {
                                            if (var15.written + var69.detail().faces() < var15.solidLimit) {
                                                var15.group(var53, var41, var69.detail(), var42);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }

                for (int var54 = var52.size() - 1; var54 >= 0; var54--) {
                    ((Runnable)var52.get(var54)).run();
                }

                var13.endBatch(MODELS);
                VertexConsumer var55 = var13.getBuffer(GLYPHS);

                for (int var56 = var12.size() - 1; var56 >= 0; var56--) {
                    OptimizedSpellVisual var57 = (OptimizedSpellVisual)var12.get(var56);
                    SpellVisualPacket var58 = var57.packet();
                    Entity var59 = owner(var4, var58);
                    double var24 = (double)((float)(var4.getGameTime() - var57.born()) + var11);
                    float var26 = SpellVisualRules.opacity(
                        (double)((float)(var4.getGameTime() - var57.refreshed()) + var11), var58.spell().lifetime(var58.phase())
                    );
                    if (!(var26 <= 0.0F)) {
                        boolean var60 = var58.phase() == Phase.BEAM;
                        OptimizedSpellModelLibrary.Model var61 = OptimizedSpellModelLibrary.find(var58.spell().id);
                        boolean var62 = var58.spell().followsCaster(var58.phase()) || var61 != null && OptimizedSpellPlacement.casterSpace(var61.scene());
                        Vec3 var63 = !var60 && var62 && var59 != null ? var59.getPosition(var11) : var58.start();
                        Vec3 var64 = var60 ? clip(var4, var59, var58.start(), var58.end()) : var58.end();
                        Vec3 var65 = var64.subtract(var58.start());
                        if (!var60 && var59 != null && var65.x * var65.x + var65.z * var65.z < 1.0E-5) {
                            var65 = var59.getViewVector(var11);
                        }

                        float var66 = (float)SpellVisuals.radiusScale(var59, var58.spell().id);
                        if (!Float.isFinite(var66) || var66 <= 0.0F) {
                            var66 = 1.0F;
                        }

                        AABB var67 = new AABB(var63, var60 ? var64 : var63).inflate(Math.max(16.0, var58.spell().radius * (double)var66));
                        if (!(var67.distanceToSqr(var5) > 4096.0) && var1.getFrustum().isVisible(var67)) {
                            Vec3 var68 = var60 ? var58.start() : ground(var4, var59, var63, true);
                            OptimizedSpellRenderer.Transform var70 = new OptimizedSpellRenderer.Transform(var68, var65, var60, (double)var66, 0.0);
                            boolean var71 = var50.contains(var56);
                            OptimizedSpellTelegraphs.draw(
                                var58.spell(),
                                var58.phase(),
                                var24,
                                var26,
                                var65.length(),
                                var71,
                                (var3x, var4x, var5x, var6x, var7x, var8x, var9x) -> var15.textured(
                                        var55,
                                        var70,
                                        var15.corners(
                                            var3x.horizontalX(),
                                            var3x.verticalY(),
                                            var3x.horizontalZ(),
                                            var4x.horizontalX(),
                                            var4x.verticalY(),
                                            var4x.horizontalZ(),
                                            var5x.horizontalX(),
                                            var5x.verticalY(),
                                            var5x.horizontalZ(),
                                            var6x.horizontalX(),
                                            var6x.verticalY(),
                                            var6x.horizontalZ()
                                        ),
                                        var7x,
                                        var8x,
                                        var9x
                                    )
                            );
                        }
                    }
                }

                var13.endBatch(GLYPHS);
            }
        }
    }

    private static Entity owner(ClientLevel var0, SpellVisualPacket var1) {
        Entity var2 = var0.getEntity(var1.ownerId());
        return var2 != null && var2.getUUID().equals(var1.owner()) ? var2 : null;
    }

    private static Vec3 clip(ClientLevel var0, Entity var1, Vec3 var2, Vec3 var3) {
        BlockHitResult var4 = var0.clip(new ClipContext(var2, var3, Block.COLLIDER, Fluid.NONE, var1));
        return var4.getType() == Type.MISS ? var3 : var4.getLocation();
    }

    private static Vec3 ground(ClientLevel var0, Entity var1, Vec3 var2, boolean var3) {
        BlockHitResult var4 = var0.clip(new ClipContext(var2.add(0.0, 0.3, 0.0), var2.add(0.0, -1.5, 0.0), Block.COLLIDER, Fluid.NONE, var1));
        return var4.getType() == Type.MISS ? var2 : new Vec3(var2.x, var3 ? var4.getLocation().y : Math.max(var2.y, var4.getLocation().y), var2.z);
    }

    private static final class Frame {
        private final Matrix4f matrix;
        private final Matrix3f normal = new Matrix3f();
        private final double[] camera;
        private final boolean firstPerson;
        private final int limit;
        private final int solidLimit;
        private int written;
        private final double[][] local = new double[4][3];
        private final double[][] transformed = new double[4][3];
        private final double[] authoredUv = new double[8];
        private final List<double[]> actors = new ArrayList<>();
        private boolean bodyPass;

        private void exclude(AABB var1) {
            double[] var2 = new double[]{var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ};

            for (double[] var4 : this.actors) {
                if (Arrays.equals(var4, var2)) {
                    return;
                }
            }

            this.actors.add(var2);
        }

        private Frame(Matrix4f var1, Vec3 var2, boolean var3, int var4) {
            this.matrix = var1;
            this.camera = new double[]{var2.x, var2.y, var2.z};
            this.firstPerson = var3;
            this.limit = var4;
            this.solidLimit = var4 - (var4 <= 3500 ? 700 : (var4 <= 6500 ? 1200 : 2200));
        }

        private void group(VertexConsumer var1, OptimizedSpellRenderer.Transform var2, OptimizedSpellModelLibrary.Group var3, float var4) {
            this.bodyPass = true;

            for (int var5 = 0; var5 < var3.faces() && this.written < this.solidLimit; var5++) {
                for (int var6 = 0; var6 < 4; var6++) {
                    System.arraycopy(var3.vertices(), var5 * 12 + var6 * 3, this.local[var6], 0, 3);
                }

                if (var3.bones()[var5] != 0) {
                    var2.animatePart(this.local, var3.bones()[var5], var3.pivots(), var5 * 3);
                }

                System.arraycopy(var3.uvs(), var5 * 8, this.authoredUv, 0, 8);
                this.emit(
                    var1,
                    var2.world(this.local, this.transformed),
                    var3.colors()[var5 * 4],
                    var4 * var3.alphas()[var5],
                    var3.materials()[var5] + 256,
                    this.authoredUv
                );
            }

            this.bodyPass = false;
        }

        private void beam(
            VertexConsumer var1, OptimizedSpellRenderer.Transform var2, OptimizedSpellModelLibrary.Group var3, float var4, double var5, double var7
        ) {
            double var9 = var7 * 0.07;
            double var11 = Math.cos(var9);
            double var13 = Math.sin(var9);

            for (int var15 = 0; var15 < var3.faces() && this.written < this.solidLimit; var15++) {
                for (int var16 = 0; var16 < 4; var16++) {
                    int var17 = var15 * 12 + var16 * 3;
                    double var18 = var3.vertices()[var17];
                    double var20 = var3.vertices()[var17 + 1];
                    this.local[var16][0] = var18 * var11 - var20 * var13;
                    this.local[var16][1] = var18 * var13 + var20 * var11;
                    this.local[var16][2] = Math.max(0.0, Math.min(var5, var3.vertices()[var17 + 2] * var5));
                }

                System.arraycopy(var3.uvs(), var15 * 8, this.authoredUv, 0, 8);
                this.emit(
                    var1,
                    var2.world(this.local, this.transformed),
                    var3.colors()[var15 * 4],
                    var4 * var3.alphas()[var15],
                    var3.materials()[var15] + 256,
                    this.authoredUv
                );
            }
        }

        private void solid(VertexConsumer var1, OptimizedSpellRenderer.Transform var2, double[][] var3, int var4, float var5, Set<Long> var6, double var7) {
            if (this.written < this.solidLimit && !(OptimizedSpellSafety.area(var3) < 1.0E-6) && var6.add(OptimizedSpellSafety.signature(var3))) {
                for (double[] var12 : var3) {
                    var12[2] = Math.max(0.0, Math.min(var7, var12[2]));
                }

                this.emit(var1, var2.world(var3, this.transformed), var4, var5, -1);
            }
        }

        private void textured(VertexConsumer var1, OptimizedSpellRenderer.Transform var2, double[][] var3, int var4, float var5, int var6) {
            if (this.written < this.limit) {
                this.emit(var1, var2.world(var3, this.transformed), var4, var5, var6);
            }
        }

        private double[][] corners(
            double var1,
            double var3,
            double var5,
            double var7,
            double var9,
            double var11,
            double var13,
            double var15,
            double var17,
            double var19,
            double var21,
            double var23
        ) {
            this.local[0][0] = var1;
            this.local[0][1] = var3;
            this.local[0][2] = var5;
            this.local[1][0] = var7;
            this.local[1][1] = var9;
            this.local[1][2] = var11;
            this.local[2][0] = var13;
            this.local[2][1] = var15;
            this.local[2][2] = var17;
            this.local[3][0] = var19;
            this.local[3][1] = var21;
            this.local[3][2] = var23;
            return this.local;
        }

        private void emit(VertexConsumer var1, double[][] var2, int var3, float var4, int var5) {
            this.emit(var1, var2, var3, var4, var5, null);
        }

        private void emit(VertexConsumer var1, double[][] var2, int var3, float var4, int var5, double[] var6) {
            double var7 = OptimizedSpellSafety.distanceSquared(var2, this.camera);
            if (!(var7 < 0.0) && this.written < this.limit) {
                if (var7 < 1.21) {
                    double var9 = edge(var2[0], var2[1]) + edge(var2[3], var2[2]);
                    double var11 = edge(var2[0], var2[3]) + edge(var2[1], var2[2]);
                    int var13 = Math.min(96, (int)Math.ceil(Math.max(var9, var11) / 1.3));
                    if (var13 > 1) {
                        for (int var14 = 0; var14 < var13 && this.written < this.limit; var14++) {
                            double var15 = (double)var14 / (double)var13;
                            double var17 = (double)(var14 + 1) / (double)var13;
                            double[][] var19 = var9 >= var11
                                ? new double[][]{
                                    lerp(var2[0], var2[1], var15), lerp(var2[0], var2[1], var17), lerp(var2[3], var2[2], var17), lerp(var2[3], var2[2], var15)
                                }
                                : new double[][]{
                                    lerp(var2[0], var2[3], var15), lerp(var2[1], var2[2], var15), lerp(var2[1], var2[2], var17), lerp(var2[0], var2[3], var17)
                                };
                            this.face(var1, var19, var3, var4, var5, subUv(var6, var15, var17, var9 >= var11));
                        }

                        return;
                    }
                }

                this.face(var1, var2, var3, var4, var5, var6);
            }
        }

        private void face(VertexConsumer var1, double[][] var2, int var3, float var4, int var5, double[] var6) {
            float var7 = var4 * OptimizedSpellSafety.cameraOpacity(var2, this.camera, this.firstPerson);
            if (this.bodyPass) {
                for (double[] var9 : this.actors) {
                    var7 *= OptimizedSpellPlacement.actorOpacity(var2, var9);
                    if ((double)var7 < 0.012) {
                        return;
                    }
                }
            }

            if (!((double)var7 < 0.012) && this.written < this.limit && !(OptimizedSpellSafety.area(var2) < 1.0E-6)) {
                float var16 = (float)(var5 % 16 * 128 + 1) / 2048.0F;
                float var17 = (float)(var5 / 16 * 128 + 1) / 2048.0F;
                float var10 = var16 + 0.061523438F;
                float var11 = var17 + 0.061523438F;
                if (var5 == 255) {
                    var11 = 0.96875F;
                    var17 = 0.96875F;
                    var10 = 0.96875F;
                    var16 = 0.96875F;
                }

                if (var5 >= 256) {
                    int var12 = var5 - 256;
                    var16 = (float)(var12 % 64 * 16 + 2) / 1024.0F;
                    var17 = (float)(var12 / 64 * 16 + 2) / 1024.0F;
                    var10 = var16 + 0.0107421875F;
                    var11 = var17 + 0.0107421875F;
                }

                for (int var18 = 0; var18 < 4; var18++) {
                    VertexConsumer var13 = var1.addVertex(
                            this.matrix,
                            (float)(var2[var18][0] - this.camera[0]),
                            (float)(var2[var18][1] - this.camera[1]),
                            (float)(var2[var18][2] - this.camera[2])
                        )
                        .setColor(var3 >> 16 & 0xFF, var3 >> 8 & 0xFF, var3 & 0xFF, Math.min(255, Math.round(var7 * 255.0F)));
                    float var14 = var6 == null ? (var18 < 2 ? var16 : var10) : (float)((double)var16 + (double)(var10 - var16) * var6[var18 * 2]);
                    float var15 = var6 == null
                        ? (var18 != 0 && var18 != 3 ? var17 : var11)
                        : (float)((double)var17 + (double)(var11 - var17) * var6[var18 * 2 + 1]);
                    if (var5 >= 0) {
                        net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var13.setUv(var14, var15).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880), this.normal, 0.0F, 1.0F, 0.0F);
                    }

                }

                this.written++;
            }
        }

        private static double edge(double[] var0, double[] var1) {
            return Math.sqrt(Math.pow(var0[0] - var1[0], 2.0) + Math.pow(var0[1] - var1[1], 2.0) + Math.pow(var0[2] - var1[2], 2.0));
        }

        private static double[] lerp(double[] var0, double[] var1, double var2) {
            return new double[]{var0[0] + (var1[0] - var0[0]) * var2, var0[1] + (var1[1] - var0[1]) * var2, var0[2] + (var1[2] - var0[2]) * var2};
        }

        private static double[] subUv(double[] var0, double var1, double var3, boolean var5) {
            if (var0 == null) {
                return null;
            } else {
                int[] var6 = var5 ? new int[]{0, 0, 3, 3} : new int[]{0, 1, 1, 0};
                int[] var7 = var5 ? new int[]{1, 1, 2, 2} : new int[]{3, 2, 2, 3};
                double[] var8 = var5 ? new double[]{var1, var3, var3, var1} : new double[]{var1, var1, var3, var3};
                double[] var9 = new double[8];

                for (int var10 = 0; var10 < 4; var10++) {
                    for (int var11 = 0; var11 < 2; var11++) {
                        int var12 = var6[var10] * 2 + var11;
                        var9[var10 * 2 + var11] = var0[var12] + (var0[var7[var10] * 2 + var11] - var0[var12]) * var8[var10];
                    }
                }

                return var9;
            }
        }
    }

    private static final class Transform {
        private static final Set<String> WEAPON_FORMS = Set.of("CLEAVE", "REAP", "OATH", "VERDICT", "SERAPH");
        private static final double[] WEAPON_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
        private static final double[] WEAPON_PITCH = new double[]{0.0, -115.9351, 134.06, 0.0};
        private static final double[] WEAPON_ROLL = new double[]{0.0, -65.81454, -65.81, 0.0};
        private Vec3 origin;
        private final double[] facing;
        private final Quaternionf rotation;
        private final double scale;
        private final double depthScale;
        private double wingAngle;
        private double motionAge;
        private final Vector3f scratch = new Vector3f();

        private void animate(Spell var1, Phase var2, double var3) {
            this.motionAge = Math.max(0.0, var3);
            double var5 = var2 == Phase.RELEASE ? -0.18 * Math.exp(-Math.max(0.0, var3) * 0.42) : 0.0;
            String var9 = var1.form.name();

            double var7 = switch (var9) {
                case "CLEAVE", "REAP", "OATH", "VERDICT", "SERAPH" -> 0.045 * Math.sin(var3 * 0.14) + var5;
                case "DOLL", "PROCESSION" -> 0.05 * Math.sin(var3 * 0.18 + (double)var1.id.hashCode() * 0.01);
                case "FEATHERS", "WINGS", "REFUGE" -> 0.03 * Math.sin(var3 * 0.2);
                default -> 0.0;
            };
            this.rotation.rotateZ((float)var7);
            if (WEAPON_FORMS.contains(var1.form.name()) && (var2 == Phase.CHARGE || var2 == Phase.RELEASE)) {
                double var17 = Math.min(1.0, Math.max(0.0, var3) / (double)var1.lifetime(var2));
                double var11 = var2 == Phase.CHARGE ? var17 * 0.75 : 0.75 + var17 * 1.2083;
                double var13 = keyframe(var11, WEAPON_TIMES, WEAPON_PITCH);
                double var15 = keyframe(var11, WEAPON_TIMES, WEAPON_ROLL);
                this.rotation.rotateX((float)Math.toRadians(var13 * 0.055)).rotateZ((float)Math.toRadians(var15 * 0.12));
            }

            double var18 = Math.max(0.0, var3) % 5.0 / 5.0;
            double var19 = var18 < 0.5 ? var18 * 2.0 : 2.0 - var18 * 2.0;
            this.wingAngle = Math.toRadians((var19 - 0.5) * 70.0 * 0.35);
        }

        private static double keyframe(double var0, double[] var2, double[] var3) {
            for (int var4 = 1; var4 < var2.length; var4++) {
                if (var0 <= var2[var4]) {
                    double var5 = Math.max(0.0, Math.min(1.0, (var0 - var2[var4 - 1]) / (var2[var4] - var2[var4 - 1])));
                    return var3[var4 - 1] + (var3[var4] - var3[var4 - 1]) * var5;
                }
            }

            return var3[var3.length - 1];
        }

        private void place(OptimizedSpellModelLibrary.Model var1, double var2, double var4) {
            double[] var6 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] var7 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

            for (int var8 = 0; var8 < 8; var8++) {
                Vector3f var9 = this.rotation
                    .transform(
                        this.scratch
                            .set(
                                (float)((var8 % 2 == 0 ? var1.minimum()[0] : var1.maximum()[0]) * this.scale),
                                (float)(((var8 & 2) == 0 ? var1.minimum()[1] : var1.maximum()[1]) * this.scale),
                                (float)(((var8 & 4) == 0 ? var1.minimum()[2] : var1.maximum()[2]) * this.scale)
                            )
                    );
                double[] var10 = new double[]{
                    (double)var9.x * this.facing[2] - (double)var9.z * this.facing[0],
                    (double)var9.y,
                    (double)var9.x * this.facing[0] + (double)var9.z * this.facing[2]
                };

                for (int var11 = 0; var11 < 3; var11++) {
                    var6[var11] = Math.min(var6[var11], var10[var11]);
                    var7[var11] = Math.max(var7[var11], var10[var11]);
                }
            }

            double[] var12 = OptimizedSpellPlacement.offset(var1.scene(), var6, var7, var2, var4);
            this.origin = this.origin
                .add(this.facing[2] * var12[0] + this.facing[0] * var12[2], var12[1], -this.facing[0] * var12[0] + this.facing[2] * var12[2]);
        }

        private void animatePart(double[][] var1, int var2, double[] var3, int var4) {
            if (var2 < 8) {
                boolean var25 = var2 <= 2;

                double var26 = switch (var2) {
                    case 1 -> -this.wingAngle;
                    case 2 -> this.wingAngle;
                    case 3 -> this.motionAge * 0.045;
                    case 4 -> this.motionAge * 0.065;
                    case 5 -> this.motionAge * 0.018;
                    default -> 0.0;
                    case 7 -> -this.motionAge * 0.038;
                };
                int var27 = var25 ? 2 : 1;
                double var28 = Math.cos(var26);
                double var29 = Math.sin(var26);
                double var13 = var3[var4];
                double var15 = var3[var4 + var27];

                for (double[] var20 : var1) {
                    double var21 = var20[0] - var13;
                    double var23 = var20[var27] - var15;
                    var20[0] = var13 + var21 * var28 + (double)(var25 ? 1 : -1) * var23 * var29;
                    var20[var27] = var15 + (double)(var25 ? -1 : 1) * var21 * var29 + var23 * var28;
                }
            } else {
                int var5 = var2 == 8 ? 2 : 1;
                double var6 = var2 == 8 ? 0.65 * Math.sin(this.motionAge * 0.16) : 0.45 * Math.cos(this.motionAge * 0.13);

                for (double[] var11 : var1) {
                    var11[var5] += var6;
                }
            }
        }

        private Transform(Vec3 var1, Vec3 var2, boolean var3, double var4, double var6) {
            this.origin = var1;
            this.scale = var4;
            this.depthScale = var3 ? 1.0 : var4;
            double[] var8 = OptimizedSpellSafety.direction(var2.x, var3 ? var2.y : 0.0, var2.z);
            this.facing = OptimizedSpellSafety.direction(var2.x, 0.0, var2.z);
            this.rotation = new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F, (float)var8[0], (float)var8[1], (float)var8[2]);
            if (!var3 && var6 != 0.0) {
                this.rotation.rotateY((float)var6);
            }
        }

        private double[][] world(double[][] var1, double[][] var2) {
            for (int var3 = 0; var3 < 4; var3++) {
                Vector3f var4 = this.rotation
                    .transform(
                        this.scratch.set((float)(var1[var3][0] * this.scale), (float)(var1[var3][1] * this.scale), (float)(var1[var3][2] * this.depthScale))
                    );
                var2[var3][0] = this.origin.x + (double)var4.x;
                var2[var3][1] = this.origin.y + (double)var4.y;
                var2[var3][2] = this.origin.z + (double)var4.z;
            }

            return var2;
        }

        private float hullOpacity(OptimizedSpellModelLibrary.Model var1, double[] var2) {
            if (FirstPersonSpellVisibility.firstPerson()) {
                return 1.0F;
            } else {
                Vector3f var3 = new Quaternionf(this.rotation)
                    .conjugate()
                    .transform(new Vector3f((float)(var2[0] - this.origin.x), (float)(var2[1] - this.origin.y), (float)(var2[2] - this.origin.z)));
                double[] var4 = new double[]{(double)var3.x / this.scale, (double)var3.y / this.scale, (double)var3.z / this.depthScale};
                double var5 = 0.0;

                for (int var7 = 0; var7 < 3; var7++) {
                    double var8 = Math.max(0.0, Math.max(var1.minimum()[var7] - var4[var7], var4[var7] - var1.maximum()[var7])) * this.scale;
                    var5 += var8 * var8;
                }

                double var10 = Math.min(1.0, Math.sqrt(var5) / 0.5);
                return (float)(0.16 + 0.84 * var10 * var10 * (3.0 - 2.0 * var10));
            }
        }
    }
}
