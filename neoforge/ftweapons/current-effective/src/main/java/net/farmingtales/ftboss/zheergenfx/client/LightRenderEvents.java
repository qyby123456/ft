package net.farmingtales.ftboss.zheergenfx.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.NativeLegacyVertexNormals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.farmingtales.ftboss.zheergenfx.LightClock;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME,
    value = {Dist.CLIENT}
)
public final class LightRenderEvents {
    private static final RenderType LIGHT = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath("ftboss", "fx/zheergen_remaining_lights.png"));
    private static final Map<String, Double> TERRAIN = new HashMap<>();
    private static final Map<String, Boolean> OCCLUDED = new HashMap<>();
    private static ClientLevel terrainWorld;
    private static long terrainEpoch = -1L;
    private static ClientLevel actorWorld;
    private static long actorTick = Long.MIN_VALUE;
    private static List<LivingEntity> nearbyActors = List.of();

    private LightRenderEvents() {
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (true) {
            ClientLevel var1 = Minecraft.getInstance().level;
            LightClient.world(var1);
            if (actorWorld != var1) {
                actorWorld = var1;
                actorTick = Long.MIN_VALUE;
                nearbyActors = List.of();
            }

            if (terrainWorld != var1) {
                terrainWorld = var1;
                terrainEpoch = -1L;
                TERRAIN.clear();
                OCCLUDED.clear();
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES) {
            Minecraft var1 = Minecraft.getInstance();
            ClientLevel var2 = var1.level;
            if (var2 == null) {
                actorWorld = null;
                nearbyActors = List.of();
                actorTick = Long.MIN_VALUE;
            } else {
                if (var2 != terrainWorld || var2.getGameTime() / 4L != terrainEpoch) {
                    TERRAIN.clear();
                    OCCLUDED.clear();
                    terrainWorld = var2;
                    terrainEpoch = var2.getGameTime() / 4L;
                }

                Vec3 var3 = var0.getCamera().getPosition();
                double[] var4 = new double[]{var3.x, var3.y, var3.z};
                float var5 = var1.isPaused() ? 0.0F : var0.getPartialTick().getGameTimeDeltaPartialTick(false);
                Vec3 var6 = var0.getCamera().getEntity() == null ? new Vec3(0.0, 0.0, 1.0) : var0.getCamera().getEntity().getViewVector(var5);
                double[] var7 = new double[]{var6.x, var6.y, var6.z};
                boolean var8 = !var0.getCamera().isDetached();
                ParticleStatus var9 = var1.options.particles().get();
                int var10 = var9 == ParticleStatus.MINIMAL ? 1800 : (var9 == ParticleStatus.DECREASED ? 3600 : 7200);
                List<LightPacket> var11 = LightClient.snapshot(var2);
                ArrayList<double[]> var12 = new ArrayList<>();
                if (var1.player != null) {
                    AABB var13 = var1.player.getBoundingBox().inflate(0.08);
                    var12.add(new double[]{var13.minX, var13.minY, var13.minZ, var13.maxX, var13.maxY, var13.maxZ});
                    if (actorWorld != var2 || actorTick != var2.getGameTime()) {
                        actorWorld = var2;
                        actorTick = var2.getGameTime();
                        nearbyActors = var2.getEntitiesOfClass(LivingEntity.class, var1.player.getBoundingBox().inflate(24.0)).stream().limit(256L).toList();
                    }

                    for (LivingEntity var15 : nearbyActors) {
                        if (var15 != var1.player && var12.size() < 16 && var15.isAlive() && var15.position().distanceToSqr(var3) < 144.0) {
                            AABB var16 = var15.getBoundingBox().inflate(0.06);
                            var12.add(new double[]{var16.minX, var16.minY, var16.minZ, var16.maxX, var16.maxY, var16.maxZ});
                        }

                        MobEffectInstance var54 = var15.getEffect(FTEffects.SHADOW_EROSION);
                        if (var54 != null && var15.isAlive()) {
                            var11.add(
                                new LightPacket(
                                    10,
                                    var15.getId(),
                                    var15.getUUID(),
                                    var2.dimension().location(),
                                    var2.getGameTime() - (long)Math.floorMod(var2.getGameTime(), 20),
                                    var15.position(),
                                    new Vec3(0.0, 0.0, 1.0),
                                    1.0,
                                    20,
                                    Math.min(5, var54.getAmplifier() + 1)
                                )
                            );
                        }

                        if (var11.size() >= 96) {
                            break;
                        }
                    }
                }

                var11.sort(Comparator.comparingDouble(var1x -> var1x.origin().distanceToSqr(var3)));
                ArrayList<LightPacket> var49 = new ArrayList<>();

                for (LightPacket var52 : var11) {
                    LightBank.Shape var55 = LightBank.shape(var52.kind(), var52.index());
                    if (var55 != null && !(var52.origin().distanceToSqr(var3) > 2304.0)) {
                        Entity var17 = var2.getEntity(var52.ownerId());
                        if ((var17 == null || var17.getUUID().equals(var52.owner()))
                            && (!LightClock.charge(var52.kind()) || var17 != null && var17.isAlive())
                            && (
                                var52.kind() != 8
                                    || !var2.getEntitiesOfClass(
                                            ShadowPhantomEntity.class,
                                            new AABB(
                                                var52.origin().x - 0.4,
                                                var52.origin().y - 0.2,
                                                var52.origin().z - 0.4,
                                                var52.origin().x + 0.4,
                                                var52.origin().y + 2.2,
                                                var52.origin().z + 0.4
                                            )
                                        )
                                        .isEmpty()
                            )) {
                            int var18 = var55.parts()
                                .stream()
                                .filter(var1x -> !var1x.detail() || var9 == ParticleStatus.ALL)
                                .mapToInt(var0x -> var0x.points()[0].length)
                                .sum();
                            if (var18 <= var10) {
                                var49.add(var52);
                                var10 -= var18;
                            }
                        }
                    }
                }

                Collections.reverse(var49);
                BufferSource var51 = var1.renderBuffers().bufferSource();
                VertexConsumer var53 = var51.getBuffer(LIGHT);
                Matrix4f var56 = var0.getPoseStack().last().pose();
                Matrix3f var57 = var0.getPoseStack().last().normal();
                double[][] var58 = new double[4][3];
                double[] var19 = new double[3];

                for (LightPacket var21 : var49) {
                    double var22 = (double)((float)var2.getGameTime() + var5 - (float)var21.born());
                    if (LightClock.alive(var22, var21.duration())) {
                        double var24 = LightClock.progress(var22, var21.duration());
                        LightBank.Shape var26 = LightBank.shape(var21.kind(), var21.index());
                        double var27 = var21.direction().x;
                        double var29 = var21.direction().z;
                        double var31 = Math.hypot(var27, var29);
                        if (var31 > 1.0E-6) {
                            var27 /= var31;
                            var29 /= var31;
                        } else {
                            var27 = 0.0;
                            var29 = 1.0;
                        }

                        double var33 = var26.rangeAnchored() ? var21.radius() : 1.0;

                        for (LightBank.Part var36 : var26.parts()) {
                            if (!var36.detail() || var9 == ParticleStatus.ALL && !(var21.origin().distanceToSqr(var3) > 225.0)) {
                                float var37 = LightBank.alpha(var36, var24);
                                if (!(var37 <= 0.008F)) {
                                    for (int var38 = 0; var38 < var36.points()[0].length; var38++) {
                                        for (int var39 = 0; var39 < 4; var39++) {
                                            LightBank.point(var36, var24, var38, var39, var19);
                                            var58[var39][0] = var21.origin().x + (var19[0] * var29 + var19[2] * var27) * var33;
                                            var58[var39][1] = var21.origin().y + var19[1];
                                            var58[var39][2] = var21.origin().z + (var19[2] * var29 - var19[0] * var27) * var33;
                                        }

                                        if (var36.ground()) {
                                            Vec3 var61 = new Vec3((var58[0][0] + var58[2][0]) * 0.5, var21.origin().y, (var58[0][2] + var58[2][2]) * 0.5);
                                            String var40 = Math.round(var61.x * 32.0) + "/" + Math.round(var61.y * 32.0) + "/" + Math.round(var61.z * 32.0);
                                            if (TERRAIN.size() > 4096) {
                                                TERRAIN.clear();
                                            }

                                            double var41 = TERRAIN.computeIfAbsent(
                                                var40,
                                                var3x -> {
                                                    BlockHitResult var4x = var2.clip(
                                                        new ClipContext(
                                                            var61.add(0.0, 1.5, 0.0), var61.add(0.0, -4.0, 0.0), Block.COLLIDER, Fluid.NONE, var1.player
                                                        )
                                                    );
                                                    return var4x.getType() == Type.MISS ? Double.NaN : var4x.getLocation().y;
                                                }
                                            );
                                            if (!Double.isFinite(var41)) {
                                                continue;
                                            }

                                            double var43 = var41 - var21.origin().y;

                                            for (double[] var48 : var58) {
                                                var48[1] += var43;
                                            }
                                        } else {
                                            Vec3 var62 = new Vec3(
                                                (var58[0][0] + var58[2][0]) * 0.5, (var58[0][1] + var58[2][1]) * 0.5, (var58[0][2] + var58[2][2]) * 0.5
                                            );
                                            String var64 = var21.owner()
                                                + "/"
                                                + var21.born()
                                                + "/"
                                                + Math.round(var62.x * 16.0)
                                                + "/"
                                                + Math.round(var62.y * 16.0)
                                                + "/"
                                                + Math.round(var62.z * 16.0);
                                            if (OCCLUDED.size() > 4096) {
                                                OCCLUDED.clear();
                                            }

                                            boolean var66 = OCCLUDED.computeIfAbsent(
                                                var64,
                                                var4x -> var2.clip(
                                                                new ClipContext(
                                                                    var21.origin().add(0.0, 1.0, 0.0), var62, Block.COLLIDER, Fluid.NONE, var1.player
                                                                )
                                                            )
                                                            .getType()
                                                        != Type.MISS
                                            );
                                            if (var66) {
                                                continue;
                                            }
                                        }

                                        float var63 = var37 * LightSafety.camera(var58, var4);

                                        for (double[] var67 : var12) {
                                            var63 *= LightSafety.actor(var58, var67);
                                        }

                                        if (!(var63 <= 0.008F)) {
                                            quad(var53, var56, var57, var58, var36.tint(), var63, var4, var7, var8);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                var51.endBatch(LIGHT);
            }
        }
    }

    private static void quad(
        VertexConsumer var0, Matrix4f var1, Matrix3f var2, double[][] var3, int var4, float var5, double[] var6, double[] var7, boolean var8
    ) {
        double var9 = (var3[1][1] - var3[0][1]) * (var3[2][2] - var3[0][2]) - (var3[1][2] - var3[0][2]) * (var3[2][1] - var3[0][1]);
        double var11 = (var3[1][2] - var3[0][2]) * (var3[2][0] - var3[0][0]) - (var3[1][0] - var3[0][0]) * (var3[2][2] - var3[0][2]);
        double var13 = (var3[1][0] - var3[0][0]) * (var3[2][1] - var3[0][1]) - (var3[1][1] - var3[0][1]) * (var3[2][0] - var3[0][0]);
        double var15 = Math.max(1.0E-10, Math.sqrt(var9 * var9 + var11 * var11 + var13 * var13));

        for (int var17 = 0; var17 < 4; var17++) {
            float var18 = var5 * (var8 ? LightSafety.sightVertex(var3[var17], var6, var7) : 1.0F);
            NativeLegacyVertexNormals.normal(var0.addVertex(var1, (float)(var3[var17][0] - var6[0]), (float)(var3[var17][1] - var6[1]), (float)(var3[var17][2] - var6[2]))
                .setColor(var4 >> 16 & 0xFF, var4 >> 8 & 0xFF, var4 & 0xFF, Math.round(var18 * 255.0F))
                .setUv(var17 != 0 && var17 != 3 ? 0.95F : 0.05F, var17 < 2 ? 0.95F : 0.05F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880), var2, (float)(var9 / var15), (float)(var11 / var15), (float)(var13 / var15));
        }
    }
}
