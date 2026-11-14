package net.farmingtales.ftboss.dandannativelights.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.NativeLegacyVertexNormals;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.dandannativelights.LightMotion;
import net.farmingtales.ftboss.dandannativelights.LightPacket;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class LightRenderer {
    private static final List<LightRenderer.Effect> EFFECTS = new ArrayList<>();
    private static final List<LightRenderer.Active> ACTIVE = new ArrayList<>();
    private static final RenderType PIPELINE = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("ftboss", "dandan_native_lights/materials.png"));
    private static ClientLevel activeWorld;

    private LightRenderer() {
    }

    public static void receive(LightPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0.valid() && var1.dimension().location().equals(var0.dimension())) {
            if (activeWorld != var1) {
                ACTIVE.clear();
                activeWorld = var1;
            }

            if (var0.kind() == 12) {
                ACTIVE.removeIf(
                    var1x -> var1x.packet().owner().equals(var0.owner())
                            && (var1x.packet().kind() == 0 || var1x.packet().kind() == 2 || var0.index() == 3 && var1x.packet().kind() == 7)
                );
            } else {
                if (ACTIVE.size() >= 64) {
                    ACTIVE.remove(0);
                }

                ACTIVE.add(new LightRenderer.Active(var0, var1.getGameTime(), new HashMap<>()));
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES) {
            Minecraft var1 = Minecraft.getInstance();
            ClientLevel var2 = var1.level;
            if (var2 != null && var2 == activeWorld) {
                if (EFFECTS.size() == 12) {
                    ACTIVE.removeIf(var1x -> {
                        if (var2.getGameTime() - var1x.born() >= (long)var1x.packet().ticks()) {
                            return true;
                        } else if (var1x.packet().kind() != 0 && var1x.packet().kind() != 2 && var1x.packet().kind() != 7) {
                            return false;
                        } else {
                            Entity var2x = var2.getEntity(var1x.packet().ownerId());
                            return var2x == null || !var2x.getUUID().equals(var1x.packet().owner()) || !var2x.isAlive();
                        }
                    });
                    Vec3 var3 = var0.getCamera().getPosition();
                    Entity var4 = var0.getCamera().getEntity();
                    Vec3 var5 = var4 == null ? new Vec3(0.0, 0.0, 1.0) : var4.getLookAngle();
                    double[] var6 = new double[]{var3.x, var3.y, var3.z};
                    double[] var7 = new double[]{var5.x, var5.y, var5.z};
                    boolean var8 = !var0.getCamera().isDetached();
                    ParticleStatus var9 = var1.options.particles().get();
                    int var10 = var9 == ParticleStatus.MINIMAL ? 3 : (var9 == ParticleStatus.DECREASED ? 6 : 10);
                    ArrayList<LightRenderer.Active> var11 = new ArrayList<>(ACTIVE);
                    var11.sort(Comparator.comparingDouble(var1x -> var1x.packet().origin().distanceToSqr(var3)));
                    ArrayList<AABB> var12 = new ArrayList<>();

                    for (AbstractClientPlayer var14 : var2.players()) {
                        if (var14.position().distanceToSqr(var3) < 6400.0) {
                            var12.add(var14.getBoundingBox().inflate(0.04));
                        }
                    }

                    ArrayList<LightRenderer.Panel> var46 = new ArrayList<>();
                    float var47 = var1.isPaused() ? 0.0F : var0.getPartialTick().getGameTimeDeltaPartialTick(false);

                    for (int var15 = 0; var15 < Math.min(var10, var11.size()); var15++) {
                        LightRenderer.Active var16 = (LightRenderer.Active)var11.get(var15);
                        LightPacket var17 = var16.packet();
                        double var18 = var17.origin().distanceToSqr(var3);
                        if (!(var18 > 9216.0)) {
                            boolean var20 = var17.kind() == 4 || var17.kind() == 5;
                            Entity var21 = var20 ? var2.getEntity(var17.ownerId()) : null;
                            AABB var22 = (var17.kind() == 4 || var17.kind() == 5) && var21 != null && var21.getUUID().equals(var17.owner())
                                ? var21.getBoundingBox().inflate(0.04)
                                : null;
                            double var23 = (double)(var17.radius() + 1.0F);
                            if (var0.getFrustum()
                                .isVisible(
                                    new AABB(
                                        var17.origin().x - var23,
                                        var17.origin().y - 0.3,
                                        var17.origin().z - var23,
                                        var17.origin().x + var23,
                                        var17.origin().y + 9.0,
                                        var17.origin().z + var23
                                    )
                                )) {
                                double var25 = (double)(((float)(var2.getGameTime() - var16.born()) + var47) / (float)var17.ticks());
                                double[] var27 = new double[]{var17.origin().x, var17.origin().y, var17.origin().z};

                                for (LightRenderer.Part var29 : EFFECTS.get(var17.kind()).parts()) {
                                    if (!var29.detail() || var9 != ParticleStatus.MINIMAL && !(var18 > 2304.0)) {
                                        double[] var30 = LightMotion.sample(var29.keys(), var25);
                                        if (!(var30[7] < 0.012)) {
                                            double[] var31 = LightMotion.worldTransform(
                                                var30, LightMotion.partRadius(var29.fixedRadius(), (double)var17.radius()), (double)var17.yaw(), var27
                                            );

                                            for (double[][] var35 : var29.faces()) {
                                                double[][] var36 = new double[4][3];
                                                double[] var37 = new double[]{
                                                    Double.POSITIVE_INFINITY,
                                                    Double.POSITIVE_INFINITY,
                                                    Double.POSITIVE_INFINITY,
                                                    Double.NEGATIVE_INFINITY,
                                                    Double.NEGATIVE_INFINITY,
                                                    Double.NEGATIVE_INFINITY
                                                };

                                                for (int var38 = 0; var38 < 4; var38++) {
                                                    LightMotion.transformInto(var35[var38], var31, var36[var38]);
                                                }

                                                if (var29.ground()) {
                                                    Vec3 var58 = new Vec3(
                                                        (var36[0][0] + var36[2][0]) / 2.0, var17.origin().y, (var36[0][2] + var36[2][2]) / 2.0
                                                    );
                                                    long var39 = (long)Math.floor(var58.x * 4.0) << 32 ^ (long)Math.floor(var58.z * 4.0) & 4294967295L;
                                                    Double var41 = var16.ground().get(var39);
                                                    if (var41 == null) {
                                                        BlockHitResult var42 = var2.clip(
                                                            new ClipContext(
                                                                var58.add(0.0, 2.0, 0.0), var58.add(0.0, -3.0, 0.0), Block.COLLIDER, Fluid.NONE, var4
                                                            )
                                                        );
                                                        var41 = var42.getType() == Type.MISS ? Double.NaN : var42.getLocation().y - var17.origin().y;
                                                        if (var16.ground().size() < 4096) {
                                                            var16.ground().put(var39, var41);
                                                        }
                                                    }

                                                    if (!Double.isFinite(var41)) {
                                                        continue;
                                                    }

                                                    for (double[] var45 : var36) {
                                                        var45[1] += var41;
                                                    }
                                                }

                                                for (double[] var64 : var36) {
                                                    for (int var68 = 0; var68 < 3; var68++) {
                                                        var37[var68] = Math.min(var37[var68], var64[var68]);
                                                        var37[var68 + 3] = Math.max(var37[var68 + 3], var64[var68]);
                                                    }
                                                }

                                                float var60 = (float)var30[7];
                                                if (var8) {
                                                    var60 = (float)((double)var60 * LightMotion.sightFade(var37, var6, var7));
                                                }

                                                AABB var62 = new AABB(var37[0], var37[1], var37[2], var37[3], var37[4], var37[5]);
                                                boolean var63 = var22 != null && var62.intersects(var22);

                                                for (AABB var69 : var12) {
                                                    if (var62.intersects(var69)) {
                                                        var63 = true;
                                                        break;
                                                    }
                                                }

                                                if (var63) {
                                                    var60 *= 0.14F;
                                                }

                                                if (!((double)var60 < 0.012)) {
                                                    double var66 = var62.getCenter().distanceToSqr(var3);
                                                    var46.add(new LightRenderer.Panel(var36, var29.material(), var60, var66));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var46.sort(Comparator.comparingDouble(LightRenderer.Panel::distance).reversed());
                    BufferSource var48 = var1.renderBuffers().bufferSource();
                    VertexConsumer var49 = var48.getBuffer(PIPELINE);
                    Matrix4f var50 = var0.getPoseStack().last().pose();
                    Matrix3f var51 = var0.getPoseStack().last().normal();
                    float[][] var19 = new float[][]{{0.0F, 0.0F}, {0.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, 0.0F}};

                    for (LightRenderer.Panel var53 : var46) {
                        double[] var54 = var53.points()[0];
                        double[] var55 = var53.points()[1];
                        double[] var24 = var53.points()[2];
                        Vec3 var56 = new Vec3(var55[0] - var54[0], var55[1] - var54[1], var55[2] - var54[2])
                            .cross(new Vec3(var24[0] - var54[0], var24[1] - var54[1], var24[2] - var54[2]))
                            .normalize();

                        for (int var26 = 0; var26 < 4; var26++) {
                            double[] var57 = var53.points()[var26];
                            NativeLegacyVertexNormals.normal(var49.addVertex(var50, (float)(var57[0] - var3.x), (float)(var57[1] - var3.y), (float)(var57[2] - var3.z))
                                .setColor(1.0F, 1.0F, 1.0F, var53.alpha())
                                .setUv(((float)(var53.material() * 16 + 1) + var19[var26][0] * 14.0F) / 128.0F, (1.0F + var19[var26][1] * 14.0F) / 16.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(15728880)
                                , var51, (float)var56.x, (float)var56.y, (float)var56.z);
                        }
                    }

                    var48.endBatch(PIPELINE);
                }
            } else {
                ACTIVE.clear();
                activeWorld = var2;
            }
        }
    }

    static {
        try (InputStream var0 = LightRenderer.class.getResourceAsStream("/assets/ftboss/dandan_native_lights/effects.json")) {
            if (var0 == null) {
                throw new IllegalStateException("Dandan native light data is missing");
            }

            JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();

            for (JsonElement var3 : var1.getAsJsonArray("effects")) {
                ArrayList var4 = new ArrayList();

                for (JsonElement var6 : var3.getAsJsonObject().getAsJsonArray("parts")) {
                    JsonObject var7 = var6.getAsJsonObject();
                    JsonArray var8 = var7.getAsJsonArray("faces");
                    double[][][] var9 = new double[var8.size()][4][3];

                    for (int var10 = 0; var10 < var9.length; var10++) {
                        for (int var11 = 0; var11 < 4; var11++) {
                            for (int var12 = 0; var12 < 3; var12++) {
                                var9[var10][var11][var12] = var8.get(var10).getAsJsonArray().get(var11).getAsJsonArray().get(var12).getAsDouble();
                            }
                        }
                    }

                    JsonArray var17 = var7.getAsJsonArray("keys");
                    double[][] var18 = new double[var17.size()][9];

                    for (int var19 = 0; var19 < var18.length; var19++) {
                        for (int var13 = 0; var13 < 9; var13++) {
                            var18[var19][var13] = var17.get(var19).getAsJsonArray().get(var13).getAsDouble();
                        }
                    }

                    double var20 = var7.has("fixed_radius") ? var7.get("fixed_radius").getAsDouble() : 0.0;
                    if (!Double.isFinite(var20) || var20 < 0.0 || var20 > 4.0) {
                        throw new IllegalStateException("Invalid local Dandan light scale");
                    }

                    var4.add(
                        new LightRenderer.Part(
                            var9, var7.get("material").getAsInt(), var18, var7.get("detail").getAsBoolean(), var7.get("ground").getAsBoolean(), var20
                        )
                    );
                }

                EFFECTS.add(new LightRenderer.Effect(var4));
            }
        } catch (Exception var16) {
            EFFECTS.clear();
            LogUtils.getLogger().error("Cannot load independent Dandan native lights", (Throwable)var16);
        }
    }

    private static record Active(LightPacket packet, long born, Map<Long, Double> ground) {
    }

    private static record Effect(List<LightRenderer.Part> parts) {
    }

    private static record Panel(double[][] points, int material, float alpha, double distance) {
    }

    private static record Part(double[][][] faces, int material, double[][] keys, boolean detail, boolean ground, double fixedRadius) {
    }
}
