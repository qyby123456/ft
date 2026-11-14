package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class QingyiWaterRenderer {
    private static final Map<String, RenderType> TYPES = QingyiWaterTiming.IDS
        .stream()
        .collect(
            Collectors.toUnmodifiableMap(
                var0 -> (String)var0, var0 -> RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "qingyi/" + var0 + ".png"))
            )
        );
    private static final Map<String, int[]> COSTS = QingyiWaterTiming.IDS.stream().collect(Collectors.toUnmodifiableMap(var0 -> (String)var0, var0 -> {
        QingyiWaveModel.Face[] var1 = QingyiWaveModel.asset(var0).faces();
        return new int[]{(int)Arrays.stream(var1).filter(var0x -> !var0x.detail()).count(), var1.length};
    }));

    private QingyiWaterRenderer() {
    }

    public static void draw(List<OptimizedSpellVisual> var0, RenderLevelStageEvent var1, ClientLevel var2, int var3) {
        Minecraft var4 = Minecraft.getInstance();
        if (var3 > 0 && !var0.isEmpty() && var2 != null && var2 == var4.level) {
            Vec3 var5 = var1.getCamera().getPosition();
            float var6 = var4.isPaused() ? 0.0F : var1.getPartialTick().getGameTimeDeltaPartialTick(false);
            ParticleStatus var7 = var4.options.particles().get();
            boolean var8 = !var1.getCamera().isDetached();
            BufferSource var9 = var4.renderBuffers().bufferSource();
            Matrix4f var10 = var1.getPoseStack().last().pose();
            Matrix3f var11 = new Matrix3f(var10);
            Vector3f[] var12 = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
            Vector3f var13 = new Vector3f();
            Vector3f var14 = new Vector3f();
            HashSet<RenderType> var15 = new HashSet<>();
            var0.sort(Comparator.comparingDouble(var1x -> var1x.packet().start().distanceToSqr(var5)));
            int var16 = 0;

            for (OptimizedSpellVisual var18 : var0) {
                SpellVisualPacket var19 = var18.packet();
                String var20 = var19.spell().id;
                if (QingyiWaterTiming.IDS.contains(var20) && var19.valid() && var2.dimension().location().equals(var19.dimension())) {
                    double var21 = (double)((float)(var2.getGameTime() - var18.refreshed()) + var6);
                    double var23 = (double)((float)(var2.getGameTime() - var18.born()) + var6);
                    QingyiWaterTiming.State var25 = QingyiWaterTiming.state(var20, var19.phase().name(), var21, var23, var19.spell().lifetime(var19.phase()));
                    if (!((double)var25.opacity() < 0.012)) {
                        Entity var26 = var2.getEntity(var19.ownerId());
                        if (var26 != null && !var26.getUUID().equals(var19.owner())) {
                            var26 = null;
                        }

                        double var27 = QingyiWaveTiming.scale(SpellVisuals.radiusScale(var26, var20));
                        boolean var29 = "tidal_lotus".equals(var20) || "abyssal_carapace".equals(var20);
                        Vec3 var30 = var29 && var26 != null ? var26.getPosition(var6) : var19.start();
                        Vec3 var31 = var19.end().subtract(var19.start()).multiply(1.0, 0.0, 1.0).normalize();
                        if (var31.lengthSqr() < 0.001) {
                            var31 = new Vec3(0.0, 0.0, 1.0);
                        }

                        Vec3 var32 = var18 instanceof QingyiSpellRenderer.Visual var33 ? var33.castOrigin() : (var26 == null ? null : var26.position());
                        boolean var53 = var25.warning() && "riptide_corridor".equals(var20) && var32 != null;
                        int var34 = var53 ? 3 : 1;
                        QingyiWaveModel.Asset var35 = QingyiWaveModel.asset(var20);
                        Matrix4f[] var36 = QingyiWaveModel.pose(var35, var25.clip(), var25.time());

                        for (int var37 = 0; var37 < var34; var37++) {
                            Vec3 var38 = var53 ? var32.add(var31.scale((double)(5 + var37 * 5))) : var30;
                            AABB var39 = new AABB(var38, var38).inflate(Math.max(3.2, (double)var35.width() * 0.55) * var27);
                            if (!(var39.distanceToSqr(var5) > 4096.0) && var1.getFrustum().isVisible(var39)) {
                                if (!var29) {
                                    BlockHitResult var40 = var2.clip(
                                        new ClipContext(var38.add(0.0, 0.8, 0.0), var38.add(0.0, -1.5, 0.0), Block.COLLIDER, Fluid.NONE, var26)
                                    );
                                    if (var40.getType() != Type.MISS) {
                                        var38 = new Vec3(var38.x, Math.max(var38.y, var40.getLocation().y + 0.015), var38.z);
                                    }
                                }

                                boolean var54 = var7 == ParticleStatus.ALL && var38.distanceToSqr(var5) <= 576.0;
                                int var41 = COSTS.get(var20)[var54 ? 1 : 0];
                                if (var16 + var41 <= var3) {
                                    Matrix4f var42 = QingyiPlacement.frame(var38.x - var5.x, var38.y - var5.y, var38.z - var5.z, var31.x, var31.z, var27);
                                    RenderType var43 = TYPES.get(var20);
                                    VertexConsumer var44 = var9.getBuffer(var43);
                                    var15.add(var43);

                                    for (QingyiWaveModel.Face var48 : var35.faces()) {
                                        if (!var48.detail() || var54) {
                                            for (int var49 = 0; var49 < 4; var49++) {
                                                var42.transformPosition(QingyiWaveModel.vertex(var48, var49, var36, var12[var49]));
                                            }

                                            float var55 = var25.opacity()
                                                * var48.opacity()
                                                * QingyiWaterTiming.faceOpacity(var20, var48.part(), (double)var25.time(), var8 && var26 == var4.player)
                                                * nearCamera(var12, var8);
                                            if (!((double)var55 < 0.012)) {
                                                var13.set(var12[1]).sub(var12[0]).cross(var14.set(var12[2]).sub(var12[0]));
                                                if (var13.lengthSquared() < 1.0E-10F) {
                                                    var13.set(var12[1]).sub(var12[0]).cross(var14.set(var12[3]).sub(var12[0]));
                                                }

                                                if (!(var13.lengthSquared() < 1.0E-10F)) {
                                                    var13.normalize();

                                                    for (int var50 = 0; var50 < 4; var50++) {
                                                        net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var44.addVertex(var10, var12[var50].x, var12[var50].y, var12[var50].z)
                                                            .setColor(255, 255, 255, Math.round(var55 * 255.0F))
                                                            .setUv(var48.uv()[var50 * 2], var48.uv()[var50 * 2 + 1])
                                                            .setOverlay(OverlayTexture.NO_OVERLAY)
                                                            .setLight(15728880)
                                                            , var11, var13.x, var13.y, var13.z);
                                                    }

                                                    var16++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (RenderType var52 : var15) {
                var9.endBatch(var52);
            }
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
            var3 = Math.min(var3, var11.y);
            var4 = Math.min(var4, var11.z);
            var5 = Math.max(var5, var11.x);
            var6 = Math.max(var6, var11.y);
            var7 = Math.max(var7, var11.z);
        }

        return QingyiWaveTiming.cameraOpacity((double)var2, (double)var3, (double)var4, (double)var5, (double)var6, (double)var7, var1);
    }
}
