package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import org.joml.Vector3f;

public final class QingyiSpellRenderer {
    private static final RenderType WATER = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "qingyi/four_tides.png"));
    private static final int[] COSTS = costs();
    private static final List<QingyiSpellRenderer.Visual> ACTIVE = new ArrayList<>();
    private static ClientLevel activeWorld;

    private QingyiSpellRenderer() {
    }

    public static boolean handles(String var0) {
        return QingyiWaterTiming.handles(var0);
    }

    public static boolean accept(SpellVisualPacket var0) {
        if (var0 != null && handles(var0.spell().id)) {
            ClientLevel var1 = Minecraft.getInstance().level;
            synchronize(var1);
            if (var1 != null && var0.valid() && var1.dimension().location().equals(var0.dimension())) {
                long var2 = var1.getGameTime();
                Entity var4 = var1.getEntity(var0.ownerId());
                if (var4 != null && !var4.getUUID().equals(var0.owner())) {
                    var4 = null;
                }

                if (var0.phase() == Phase.CHARGE) {
                    for (int var5 = 0; var5 < ACTIVE.size(); var5++) {
                        QingyiSpellRenderer.Visual var6 = ACTIVE.get(var5);
                        if (var6.same(var0) && var6.packet().phase() == var0.phase()) {
                            ACTIVE.set(var5, new QingyiSpellRenderer.Visual(var0, var6.born(), var2, var4 == null ? null : var4.position()));
                            return true;
                        }
                    }
                } else {
                    ACTIVE.removeIf(
                        var1x -> var1x.same(var0)
                                && (
                                    var1x.packet().phase() == Phase.CHARGE
                                        || var0.phase() != Phase.BEAM
                                            && var1x.packet().phase() != Phase.BEAM
                                            && (
                                                !"riptide_corridor".equals(var0.spell().id)
                                                    || var1x.packet().phase() != Phase.PULSE
                                                    || var1x.packet().start().distanceToSqr(var0.start()) < 0.01
                                            )
                                )
                    );
                }

                if (ACTIVE.size() >= 96) {
                    ACTIVE.remove(0);
                }

                ACTIVE.add(new QingyiSpellRenderer.Visual(var0, var2, var2, var4 == null ? null : var4.position()));
                return true;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static void synchronize(ClientLevel var0) {
        if (activeWorld != var0) {
            ACTIVE.clear();
            activeWorld = var0;
        }

        if (var0 != null) {
            long var1 = var0.getGameTime();
            ACTIVE.removeIf(
                var3 -> {
                    long var4 = var1 - var3.refreshed();
                    Entity var6 = var0.getEntity(var3.packet().ownerId());
                    return var4 < 0L
                        || var4
                            >= (long)QingyiWaterTiming.lifetime(
                                var3.packet().spell().id, var3.packet().phase().name(), var3.packet().spell().lifetime(var3.packet().phase())
                            )
                        || var6 != null && var6.getUUID().equals(var3.packet().owner()) && !var6.isAlive();
                }
            );
        }
    }

    public static void render(RenderLevelStageEvent var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        synchronize(var1);
        if (var1 != null && !ACTIVE.isEmpty() && var0.getStage() == Stage.AFTER_PARTICLES) {
            drawAll(new ArrayList<>(ACTIVE), var0, var1);
        }
    }

    private static int[] costs() {
        int[] var0 = new int[4];

        for (QingyiWaveModel.Face var4 : QingyiWaveModel.asset().faces()) {
            for (int var5 = 0; var5 < 4; var5++) {
                if ((!var4.interior() || (var5 & 1) == 0) && (!var4.detail() || (var5 & 2) != 0)) {
                    var0[var5]++;
                }
            }
        }

        return var0;
    }

    public static List<?> renderAndFilter(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (var0.isEmpty()) {
            return var0;
        } else {
            ArrayList var3 = new ArrayList();
            ArrayList var4 = new ArrayList();

            for (Object var6 : var0) {
                if (var6 instanceof OptimizedSpellVisual var7 && handles(var7.packet().spell().id)) {
                    var4.add(var7);
                    continue;
                }

                var3.add(var6);
            }

            if (var4.isEmpty()) {
                return var0;
            } else {
                if (var1.getStage() == Stage.AFTER_PARTICLES) {
                    drawAll(var4, var1, var2);
                }

                return var3;
            }
        }
    }

    private static void drawAll(List<OptimizedSpellVisual> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        ParticleStatus var3 = Minecraft.getInstance().options.particles().get();
        int var4 = var3 == ParticleStatus.MINIMAL ? 4500 : (var3 == ParticleStatus.DECREASED ? 9000 : 18000);
        ArrayList var5 = new ArrayList();
        ArrayList var6 = new ArrayList();

        for (OptimizedSpellVisual var8 : var0) {
            ("four_tides".equals(var8.packet().spell().id) ? var5 : var6).add(var8);
        }

        int var9 = draw(var5, var1, var2, var4);
        QingyiWaterRenderer.draw(var6, var1, var2, var4 - var9);
    }

    private static int draw(List<OptimizedSpellVisual> var0, RenderLevelStageEvent var1, ClientLevel var2, int var3) {
        Minecraft var4 = Minecraft.getInstance();
        if (var2 != null && var2 == var4.level && !var0.isEmpty() && var3 > 0) {
            Vec3 var5 = var1.getCamera().getPosition();
            ParticleStatus var6 = var4.options.particles().get();
            float var7 = var4.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
            var0.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var5)));
            BufferSource var8 = var4.renderBuffers().bufferSource();
            VertexConsumer var9 = var8.getBuffer(WATER);
            Matrix4f var10 = var1.getPoseStack().last().pose();
            Matrix3f var11 = new Matrix3f(var10);
            Vector3f[] var12 = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
            Vector3f var13 = new Vector3f();
            Vector3f var14 = new Vector3f();
            Vector3f var15 = new Vector3f();
            int var16 = 0;

            for (OptimizedSpellVisual var18 : var0) {
                SpellVisualPacket var19 = var18.packet();
                if (var19.valid() && var2.dimension().location().equals(var19.dimension())) {
                    double var20 = (double)((float)(var2.getGameTime() - var18.refreshed()) + var7);
                    Entity var22 = var2.getEntity(var19.ownerId());
                    if (var22 != null && !var22.getUUID().equals(var19.owner())) {
                        var22 = null;
                    }

                    double var23 = QingyiWaveTiming.scale(SpellVisuals.radiusScale(var22, var19.spell().id));
                    Vec3 var26;
                    double var27;
                    String var29;
                    float var30;
                    float var31;
                    Vec3 var52;
                    if (var19.phase() == Phase.BEAM) {
                        Vec3 var32 = var19.end().subtract(var19.start());
                        double var33 = Math.sqrt(var32.x * var32.x + var32.z * var32.z);
                        if (var33 < 0.1 || var33 > 64.0) {
                            continue;
                        }

                        int var35 = QingyiWaveTiming.slice(var33, var23);
                        var31 = QingyiWaveTiming.opacity(var20, var35);
                        if ((double)var31 < 0.01) {
                            continue;
                        }

                        var52 = var19.start().add(var32.scale(0.5)).add(0.0, -0.8, 0.0);
                        var26 = new Vec3(var32.z, 0.0, -var32.x).normalize();
                        Vec3 var36 = var22 == null ? null : var52.subtract(var22.position());
                        if (var36 != null && var26.dot(var36) < 0.0) {
                            var26 = var26.scale(-1.0);
                        }

                        var52 = var52.add(var26.scale(QingyiWaveTiming.travel(var20, var35) * var23));
                        var27 = var33 / (double)QingyiWaveModel.asset().width();
                        var30 = QingyiWaveTiming.time(var20, var35);
                        var29 = "surge";
                    } else {
                        boolean var54 = var19.phase() == Phase.CHARGE;
                        if (!var54 && var19.phase() != Phase.RELEASE
                            || var0.stream().anyMatch(var1x -> var1x.packet().phase() == Phase.BEAM && var1x.packet().owner().equals(var19.owner()))) {
                            continue;
                        }

                        var52 = var54 && var22 != null ? var22.getPosition(var7) : var19.start();
                        var26 = var19.end().subtract(var19.start()).multiply(1.0, 0.0, 1.0).normalize();
                        if (var26.lengthSqr() < 0.001) {
                            var26 = var22 == null ? new Vec3(0.0, 0.0, 1.0) : var22.getViewVector(var7).multiply(1.0, 0.0, 1.0).normalize();
                        }

                        var52 = var52.add(var26.scale(0.85 * var23));
                        var27 = 0.55 * var23;
                        var29 = "gather";
                        var30 = var54 ? (float)Math.min(0.5, (double)((float)(var2.getGameTime() - var18.born()) + var7) * 0.05) : 0.5F;
                        var31 = var54 ? (float)(0.6 * QingyiWaveTiming.smooth(var20 / 2.0)) : (float)(0.45 * (1.0 - QingyiWaveTiming.smooth(var20 / 8.0)));
                    }

                    if (!((double)var31 < 0.01)) {
                        AABB var55 = new AABB(var52, var52).inflate(Math.max(var27 * 2.5, var23 * 3.0));
                        if (!(var55.distanceToSqr(var5) > 4096.0) && var1.getFrustum().isVisible(var55)) {
                            BlockHitResult var56 = var2.clip(
                                new ClipContext(var52.add(0.0, 0.8, 0.0), var52.add(0.0, -1.5, 0.0), Block.COLLIDER, Fluid.NONE, var22)
                            );
                            if (var56.getType() != Type.MISS) {
                                var52 = new Vec3(var52.x, Math.max(var52.y, var56.getLocation().y + 0.015), var52.z);
                            }

                            Matrix4f[] var34 = QingyiWaveModel.pose(var29, var30);
                            boolean var57 = var6 != ParticleStatus.ALL || var52.distanceToSqr(var5) > 576.0;
                            boolean var58 = var6 != ParticleStatus.MINIMAL && var52.distanceToSqr(var5) <= 256.0;
                            int var37 = COSTS[(var57 ? 1 : 0) | (var58 ? 2 : 0)];
                            if (var16 + var37 <= var3) {
                                double var38 = var23 * Math.sqrt(var27 / var23);

                                for (QingyiWaveModel.Face var43 : QingyiWaveModel.asset().faces()) {
                                    if ((!var43.interior() || !var57) && (!var43.detail() || var58)) {
                                        if (var43.featurePivot() != null) {
                                            var34[var43.bone()].transformPosition(var15.set(var43.featurePivot()));
                                        }

                                        for (int var44 = 0; var44 < 4; var44++) {
                                            Vector3f var45 = QingyiWaveModel.vertex(var43, var44, var34, var12[var44]);
                                            double var46 = var43.featurePivot() == null
                                                ? (double)var45.x * var27
                                                : (double)var15.x * var27 + (double)(var45.x - var15.x) * var38;
                                            double var48 = (double)var45.y * var23;
                                            double var50 = (double)var45.z * var38;
                                            var45.set(
                                                (float)(var52.x + var26.z * var46 + var26.x * var50 - var5.x),
                                                (float)(var52.y + var48 - var5.y),
                                                (float)(var52.z - var26.x * var46 + var26.z * var50 - var5.z)
                                            );
                                        }

                                        float var59 = var31 * var43.opacity() * nearCamera(var12, !var1.getCamera().isDetached());
                                        if (!((double)var59 < 0.012)) {
                                            var13.set(var12[1]).sub(var12[0]).cross(var14.set(var12[2]).sub(var12[0]));
                                            if (var13.lengthSquared() < 1.0E-10F) {
                                                var13.set(var12[1]).sub(var12[0]).cross(var14.set(var12[3]).sub(var12[0]));
                                            }

                                            if (!(var13.lengthSquared() < 1.0E-10F)) {
                                                var13.normalize();
                                                float var60 = 1.0F;
                                                float var61 = 0.0F;

                                                for (int var47 = 0; var47 < 4; var47++) {
                                                    var60 = Math.min(var60, var43.uv()[var47 * 2]);
                                                    var61 = Math.max(var61, var43.uv()[var47 * 2]);
                                                }

                                                float var62 = (float)Math.min(31.0, (double)((var61 - var60) * 128.0F) * var27 / var23);
                                                float var63 = var43.tile() < 0 ? 0.0F : (var60 * 128.0F - (float)var43.tile()) % (32.0F - var62);

                                                for (int var49 = 0; var49 < 4; var49++) {
                                                    float var64 = var43.uv()[var49 * 2];
                                                    if (var43.tile() >= 0 && var61 > var60) {
                                                        var64 = ((float)var43.tile() + var63 + (var64 - var60) / (var61 - var60) * var62) / 128.0F;
                                                    }

                                                    net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var9.addVertex(var10, var12[var49].x, var12[var49].y, var12[var49].z)
                                                        .setColor(255, 255, 255, Math.round(var59 * 255.0F))
                                                        .setUv(var64, var43.uv()[var49 * 2 + 1])
                                                        .setOverlay(OverlayTexture.NO_OVERLAY)
                                                        .setLight(15728880)
                                                        , var11, var13.x, var13.y, var13.z);
                                                }

                                                var16++;
                                            }
                                        }
                                    }
                                }

                                if (var16 >= var3) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            var8.endBatch(WATER);
            return var16;
        } else {
            return 0;
        }
    }

    private static float nearCamera(Vector3f[] var0, boolean var1) {
        float var2 = Float.POSITIVE_INFINITY;
        float var3 = var2;
        float var4 = var2;
        float var5 = Float.NEGATIVE_INFINITY;
        float var6 = var5;
        float var7 = var5;

        for (Vector3f var11 : var0) {
            var2 = Math.min(var2, var11.x);
            var5 = Math.max(var5, var11.x);
            var3 = Math.min(var3, var11.y);
            var6 = Math.max(var6, var11.y);
            var4 = Math.min(var4, var11.z);
            var7 = Math.max(var7, var11.z);
        }

        return QingyiWaveTiming.cameraOpacity((double)var2, (double)var3, (double)var4, (double)var5, (double)var6, (double)var7, var1);
    }

    static record Visual(SpellVisualPacket packet, long born, long refreshed, Vec3 castOrigin) implements OptimizedSpellVisual {
        @Override
        public boolean nativeLight() {
            return false;
        }

        boolean same(SpellVisualPacket var1) {
            return this.packet.owner().equals(var1.owner()) && this.packet.spell() == var1.spell();
        }
    }
}
