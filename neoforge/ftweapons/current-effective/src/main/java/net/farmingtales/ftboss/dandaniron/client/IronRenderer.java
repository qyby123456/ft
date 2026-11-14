package net.farmingtales.ftboss.dandaniron.client;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.client.render.dandan.DandanMotion;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapMotion;
import net.farmingtales.ftboss.dandaniron.IronClock;
import net.farmingtales.ftboss.dandaniron.IronGeometry;
import net.farmingtales.ftboss.dandaniron.IronLifecycle;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanironmotion.client.IronActor;
import net.farmingtales.ftboss.dandanrun.client.RunActor;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class IronRenderer {
    private static final Map<String, IronRenderer.Model> MODELS = new HashMap<>();
    private static final List<IronPacket> ACTIVE = new ArrayList<>();
    private static final RenderType AREA = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/dandan/materials.png"));
    private static final RenderType REAP = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("ftboss", "reap_flat/materials.png"));
    private static ClientLevel world;
    private static final double[][] UV = new double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}};

    private IronRenderer() {
    }

    public static void receive(IronPacket var0) {
        RunActor.notice(var0);
        IronActor.receive(var0);
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0.valid() && var0.visual().dimension().equals(var1.dimension().location())) {
            if (var1 != world) {
                ACTIVE.clear();
                world = var1;
            }

            if (var0.cancelled()) {
                ACTIVE.removeIf(var1x -> same(var1x, var0) && IronLifecycle.cancels(var1x.visual().phase().name(), var1x.cast(), var0.cast()));
            } else {
                for (IronPacket var3 : ACTIVE) {
                    if (same(var3, var0)
                        && IronLifecycle.stale(var3.visual().phase().name(), var3.cast(), var3.event(), var0.visual().phase().name(), var0.cast(), var0.event())
                        )
                     {
                        return;
                    }
                }

                if (var0.visual().phase() != Phase.BEAM) {
                    ACTIVE.removeIf(
                        var1x -> same(var1x, var0)
                                && IronLifecycle.replaces(var1x.visual().phase().name(), var1x.cast(), var0.visual().phase().name(), var0.cast())
                    );
                }

                if (ACTIVE.size() >= 64) {
                    ACTIVE.remove(0);
                }

                ACTIVE.add(var0);
            }
        }
    }

    private static boolean same(IronPacket var0, IronPacket var1) {
        return var0.visual().owner().equals(var1.visual().owner()) && var0.visual().spell() == var1.visual().spell();
    }

    public static List<?> exclude(List<?> var0, RenderLevelStageEvent var1, ClientLevel var2) {
        if (var1.getStage() == Stage.AFTER_PARTICLES && var2 != null) {
            ArrayList var3 = new ArrayList();

            for (Object var5 : var0) {
                if (var5 instanceof OptimizedSpellVisual var6 && IronPacket.matches(var6.packet().spell().id)) {
                    Entity var7 = var2.getEntity(var6.packet().ownerId());
                    if (var7 instanceof EasterBossEntity var8
                        && var7.getUUID().equals(var6.packet().owner())
                        && var8.getVariant() == 1
                        && !var8.isMini()
                        && !var8.isGiant()) {
                        continue;
                    }
                }

                var3.add(var5);
            }

            return var3;
        } else {
            return var0;
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var1 == world) {
            ACTIVE.removeIf(var1x -> expired(var1x, (double)var1.getGameTime()));
        } else {
            ACTIVE.clear();
            world = var1;
        }
    }

    private static boolean expired(IronPacket var0, double var1) {
        Entity var3 = world.getEntity(var0.visual().ownerId());
        return var3 != null && var3.getUUID().equals(var0.visual().owner()) && var3.isAlive()
            ? IronClock.opacity(var0.visual().phase() == Phase.CHARGE, var1, var0.start(), var0.ticks(), var0.event()) <= 0.0
            : true;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES && world != null && Minecraft.getInstance().level == world && !ACTIVE.isEmpty() && MODELS.size() == 3) {
            boolean var1 = SpellBloomRenderer.begin(var0, true);

            try {
                draw(var0);
            } finally {
                SpellBloomRenderer.end(var1);
            }
        }
    }

    private static void draw(RenderLevelStageEvent var0) {
        Minecraft var1 = Minecraft.getInstance();
        double var2 = (double)((float)world.getGameTime() + (var1.isPaused() ? 0.0F : var0.getPartialTick().getGameTimeDeltaPartialTick(false)));
        ACTIVE.removeIf(var2x -> expired(var2x, var2));
        Vec3 var4 = var0.getCamera().getPosition();
        boolean var5 = !var0.getCamera().isDetached();
        ParticleStatus var6 = var1.options.particles().get();
        int var7 = var6 == ParticleStatus.MINIMAL ? 4000 : (var6 == ParticleStatus.DECREASED ? 8000 : 12000);
        int var8 = var6 == ParticleStatus.MINIMAL ? 1 : (var6 == ParticleStatus.DECREASED ? 2 : 3);
        int var9 = 0;
        int var10 = 0;
        ArrayList<IronPacket> var11 = new ArrayList<>(ACTIVE);
        var11.sort(Comparator.comparingDouble(var1x -> var1x.visual().start().distanceToSqr(var4)));
        ArrayList<AABB> var12 = new ArrayList<>();

        for (AbstractClientPlayer var14 : world.players()) {
            if (var14.position().distanceToSqr(var4) < 9216.0) {
                var12.add(var14.getBoundingBox().inflate(0.04));
            }
        }

        ArrayList<IronRenderer.Panel> var33 = new ArrayList<>();
        Iterator var34 = var11.iterator();

        while (true) {
            Entity var17;
            List<IronRenderer.Panel> var23;
            while (true) {
                if (!var34.hasNext()) {
                    var33.sort(Comparator.comparingDouble(IronRenderer.Panel::distance).reversed());
                    BufferSource var35 = var1.renderBuffers().bufferSource();
                    Matrix4f var36 = var0.getPoseStack().last().pose();
                    Matrix3f var37 = var0.getPoseStack().last().normal();

                    for (IronRenderer.Panel var39 : var33) {
                        emit(var35.getBuffer(var39.pipeline()), var39, var36, var37, var4);
                    }

                    var35.endBatch(REAP);
                    var35.endBatch(AREA);
                    return;
                }

                IronPacket var15 = (IronPacket)var34.next();
                SpellVisualPacket var16 = var15.visual();
                if (!(var16.start().distanceToSqr(var4) > 9216.0) || !(var16.end().distanceToSqr(var4) > 9216.0)) {
                    var17 = world.getEntity(var16.ownerId());
                    IronRenderer.Model var18 = MODELS.get(var16.spell().id);
                    double var19 = IronClock.opacity(var16.phase() == Phase.CHARGE, var2, var15.start(), var15.ticks(), var15.event());
                    double var21 = Math.max(var18.span() * (double)var15.scale(), var16.start().distanceTo(var16.end())) + 2.0;
                    if (var0.getFrustum().isVisible(new AABB(var16.start(), var16.end()).inflate(var21))) {
                        if (var16.phase() == Phase.BEAM) {
                            if (var10++ < 16) {
                                var23 = beam(var15, var19, var4);
                                break;
                            }
                        } else if (var9 < var8 && var33.size() + var18.faces().size() <= var7) {
                            var23 = body(var15, var18, var17, var2, var19, var6 != ParticleStatus.MINIMAL, var4);
                            var9++;
                            break;
                        }
                    }
                }
            }

            AABB var24 = var17.getBoundingBox().inflate(0.04);

            for (IronRenderer.Panel var26 : var23) {
                if (var33.size() >= var7) {
                    break;
                }

                AABB var27 = bounds(var26.points());
                double var28 = (double)var26.alpha();
                boolean var30 = var27.intersects(var24);
                if (!var30) {
                    for (AABB var32 : var12) {
                        if (var27.intersects(var32)) {
                            var30 = true;
                            break;
                        }
                    }
                }

                var28 *= IronClock.personOpacity(var5, var30);
                if (var28 >= 0.012) {
                    var33.add(new IronRenderer.Panel(var26.points(), var26.uv(), var26.material(), (float)var28, var26.pipeline(), var26.distance()));
                }
            }
        }
    }

    private static List<IronRenderer.Panel> body(IronPacket var0, IronRenderer.Model var1, Entity var2, double var3, double var5, boolean var7, Vec3 var8) {
        boolean var9 = var0.visual().phase() == Phase.CHARGE;
        boolean var10 = var1.id().equals("candle_reap");
        double var11 = IronClock.clock(var9, var3, var0.start(), var0.ticks());
        double var13 = var9 ? 12.0 * IronClock.progress(var3, var0.start(), var0.ticks()) : var3 - (double)var0.start();
        double var15 = var9 ? 0.72 + 0.28 * IronClock.smooth(IronClock.progress(var3, var0.start(), var0.ticks())) : 1.0;
        double var17 = var1.span() / var1.nativeSpan() * var15;
        double[] var19 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] var20 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        ArrayList var21 = new ArrayList();
        double[] var22 = FlatReapMotion.rotation(var11, 30);
        DandanMotion.WeaponPose var23 = DandanMotion.weaponPose(var11);

        for (IronRenderer.Face var25 : var1.faces()) {
            double[][] var26 = new double[4][3];

            for (int var27 = 0; var27 < 4; var27++) {
                double[] var28 = IronGeometry.point(
                    var25.points()[var27], var25.pivot(), var25.bone(), var10, var25.emitted() >= 0.0, var22, var23, var13, var17, (double)var0.scale()
                );
                var26[var27] = var28;
                if (var25.emitted() < 0.0) {
                    for (int var29 = 0; var29 < 3; var29++) {
                        var19[var29] = Math.min(var19[var29], var28[var29]);
                        var20[var29] = Math.max(var20[var29], var28[var29]);
                    }
                }
            }

            var21.add(var26);
        }

        Vec3 var47 = var9 && var10 ? var2.getPosition(Minecraft.getInstance().isPaused() ? 0.0F : (float)(var3 % 1.0)) : var0.visual().start();
        Vec3 var48 = var0.visual().end().subtract(var0.visual().start());
        double var49 = Math.hypot(var48.x, var48.z);
        double var50 = var49 < 1.0E-8 ? 0.0 : var48.x / var49;
        double var30 = var49 < 1.0E-8 ? 1.0 : var48.z / var49;
        double var32 = FlatReapMotion.placementLift(var19[1], var20[1], var1.height());
        double var34 = var1.anchor().equals("FRONT") ? 0.55 + (double)(var2.getBbWidth() / 2.0F) - var19[2] : 0.0;
        if (var1.anchor().equals("ABOVE")) {
            var32 = Math.max(var32, (double)var2.getBbHeight() + 0.65 - var19[1]);
        }

        double[] var36 = new double[]{var47.x, var47.y, var47.z};
        ArrayList var37 = new ArrayList();

        for (int var38 = 0; var38 < var1.faces().size(); var38++) {
            IronRenderer.Face var39 = var1.faces().get(var38);
            if (!var39.detail() || var7) {
                double var40 = var5 * (double)var39.opacity();
                if (var39.emitted() >= 0.0) {
                    var40 *= var9 ? 0.0 : FlatReapMotion.trailAlpha(var11, var39.emitted());
                }

                if (var9 && var39.detail()) {
                    var40 *= 0.28 + 0.72 * IronClock.smooth(IronClock.progress(var3, var0.start(), var0.ticks()));
                }

                if (!var9 && var0.visual().phase() == Phase.PULSE && var39.bone() >= 10) {
                    var40 *= 0.84 + 0.16 * Math.cos(var13 * 0.7 - (double)(var39.bone() - 10) * 1.256);
                }

                if (!(var40 < 0.012)) {
                    double[][] var42 = (double[][])var21.get(var38);

                    for (double[] var46 : var42) {
                        IronGeometry.worldInto(
                            var46,
                            var36,
                            var39.emitted() >= 0.0 ? 0.0 : var32,
                            var39.emitted() >= 0.0 ? (double)(var2.getBbWidth() / 2.0F) - 0.3 : var34,
                            var50,
                            var30
                        );
                    }

                    var37.add(
                        new IronRenderer.Panel(
                            var42, var39.uv(), var39.material(), (float)var40, var1.pipeline(), bounds(var42).getCenter().distanceToSqr(var8)
                        )
                    );
                }
            }
        }

        return var37;
    }

    private static List<IronRenderer.Panel> beam(IronPacket var0, double var1, Vec3 var3) {
        Vec3 var4 = var0.visual().start();
        Vec3 var5 = var0.visual().end().subtract(var4);
        double var6 = var5.length();
        if (var6 < 0.01) {
            return List.of();
        } else {
            Vec3 var8 = var5.normalize();
            Vec3 var9 = var8.cross(Math.abs(var8.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize().scale(0.018);
            Vec3 var10 = var8.cross(var9).normalize().scale(0.008);
            ArrayList var11 = new ArrayList();
            boolean var12 = var0.visual().spell().id.equals("candle_reap");

            for (int var13 = 0; var13 < 8; var13++) {
                double[][][] var14 = new double[2][4][3];

                for (int var15 = 0; var15 < 2; var15++) {
                    Vec3 var16 = var4.add(var5.scale((double)(var13 + var15) / 8.0));

                    for (int var17 = 0; var17 < 4; var17++) {
                        Vec3 var18 = var16.add(var9.scale(var17 < 2 ? -1.0 : 1.0)).add(var10.scale(var17 != 0 && var17 != 3 ? 1.0 : -1.0));
                        var14[var15][var17] = new double[]{var18.x, var18.y, var18.z};
                    }
                }

                for (int var19 = 0; var19 < 4; var19++) {
                    double[][] var21 = new double[][]{var14[0][var19], var14[1][var19], var14[1][(var19 + 1) % 4], var14[0][(var19 + 1) % 4]};
                    var11.add(
                        new IronRenderer.Panel(
                            var21,
                            UV,
                            var12 ? 10 : (var13 % 2 == 0 ? 10 : 11),
                            (float)(var1 * 0.55),
                            var12 ? REAP : AREA,
                            bounds(var21).getCenter().distanceToSqr(var3)
                        )
                    );
                }

                if (var13 == 0 || var13 == 7) {
                    double[][] var20 = var13 == 0 ? var14[0] : new double[][]{var14[1][3], var14[1][2], var14[1][1], var14[1][0]};
                    var11.add(new IronRenderer.Panel(var20, UV, 10, (float)(var1 * 0.55), var12 ? REAP : AREA, bounds(var20).getCenter().distanceToSqr(var3)));
                }
            }

            return var11;
        }
    }

    private static AABB bounds(double[][] var0) {
        double[] var1 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] var2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

        for (double[] var6 : var0) {
            for (int var7 = 0; var7 < 3; var7++) {
                var1[var7] = Math.min(var1[var7], var6[var7]);
                var2[var7] = Math.max(var2[var7], var6[var7]);
            }
        }

        return new AABB(var1[0], var1[1], var1[2], var2[0], var2[1], var2[2]);
    }

    private static double[] vector(AABB var0) {
        return new double[]{var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ};
    }

    private static void emit(VertexConsumer var0, IronRenderer.Panel var1, Matrix4f var2, Matrix3f var3, Vec3 var4) {
        double[][] var5 = var1.points();
        Vec3 var6 = new Vec3(var5[1][0] - var5[0][0], var5[1][1] - var5[0][1], var5[1][2] - var5[0][2]);
        Vec3 var7 = new Vec3(var5[2][0] - var5[0][0], var5[2][1] - var5[0][1], var5[2][2] - var5[0][2]);
        Vec3 var8 = var6.cross(var7).normalize();

        for (int var9 = 0; var9 < 4; var9++) {
            net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var0.addVertex(var2, (float)(var5[var9][0] - var4.x), (float)(var5[var9][1] - var4.y), (float)(var5[var9][2] - var4.z))
                .setColor(1.0F, 1.0F, 1.0F, var1.alpha())
                .setUv(
                    (float)(((double)(var1.material() % 8 * 16 + 2) + var1.uv()[var9][0] * 11.0) / 128.0),
                    (float)(((double)(var1.material() / 8 * 16 + 2) + var1.uv()[var9][1] * 11.0) / 32.0)
                )
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                , var3, (float)var8.x, (float)var8.y, (float)var8.z);
        }
    }

    static {
        try {
            for (String var1 : List.of("candle_reap", "candle_prison", "candle_eclipse")) {
                boolean var2 = var1.equals("candle_reap");
                String var3 = var2 ? "/assets/ftboss/reap_flat/model.json" : "/assets/ftboss/dandan/" + var1 + ".json";

                try (InputStream var4 = IronRenderer.class.getResourceAsStream(var3)) {
                    if (var4 == null) {
                        throw new IllegalStateException("Missing final Dandan Iron model: " + var3);
                    }

                    JsonObject var5 = JsonParser.parseReader(new InputStreamReader(var4, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonObject var6 = var5.getAsJsonObject("scene");
                    ArrayList var7 = new ArrayList();
                    double[] var8 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                    double[] var9 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

                    for (JsonElement var11 : var5.getAsJsonArray("faces")) {
                        JsonObject var12 = var11.getAsJsonObject();
                        double[][] var13 = new double[4][3];
                        double[][] var14 = new double[4][2];
                        double[] var15 = new double[3];

                        for (int var16 = 0; var16 < 3; var16++) {
                            var15[var16] = var12.getAsJsonArray("pivot").get(var16).getAsDouble();
                        }

                        for (int var24 = 0; var24 < 4; var24++) {
                            for (int var17 = 0; var17 < 3; var17++) {
                                double var18 = var12.getAsJsonArray("points").get(var24).getAsJsonArray().get(var17).getAsDouble();
                                if (!Double.isFinite(var18)) {
                                    throw new IllegalStateException("Nonfinite final Dandan geometry");
                                }

                                var13[var24][var17] = var18;
                                var8[var17] = Math.min(var8[var17], var18);
                                var9[var17] = Math.max(var9[var17], var18);
                            }

                            for (int var25 = 0; var25 < 2; var25++) {
                                var14[var24][var25] = var12.getAsJsonArray("uv").get(var24).getAsJsonArray().get(var25).getAsDouble();
                            }
                        }

                        var7.add(
                            new IronRenderer.Face(
                                var13,
                                var14,
                                var15,
                                var12.get("bone").getAsInt(),
                                var12.get("material").getAsInt(),
                                var12.get("opacity").getAsFloat(),
                                var12.get("group").getAsString().equals("detail"),
                                var12.has("emitted") ? var12.get("emitted").getAsDouble() : -1.0
                            )
                        );
                    }

                    double var23 = var2 ? var6.get("nativeSpan").getAsDouble() : Math.max(var9[0] - var8[0], Math.max(var9[1] - var8[1], var9[2] - var8[2]));
                    MODELS.put(
                        var1,
                        new IronRenderer.Model(
                            var1,
                            var6.get("anchor").getAsString(),
                            var6.get("span").getAsDouble(),
                            var6.get("height").getAsDouble(),
                            var23,
                            var7,
                            var2 ? REAP : AREA
                        )
                    );
                }
            }
        } catch (Exception var22) {
            MODELS.clear();
            LogUtils.getLogger().error("Cannot load final Dandan Iron adaptation", (Throwable)var22);
        }
    }

    private static record Face(double[][] points, double[][] uv, double[] pivot, int bone, int material, float opacity, boolean detail, double emitted) {
    }

    private static record Model(String id, String anchor, double span, double height, double nativeSpan, List<IronRenderer.Face> faces, RenderType pipeline) {
    }

    private static record Panel(double[][] points, double[][] uv, int material, float alpha, RenderType pipeline, double distance) {
    }
}
