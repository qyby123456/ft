/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.dandannativelights.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.dandannativelights.LightMotion;
import net.farmingtales.ftboss.dandannativelights.LightPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class LightRenderer {
    private static final List<Effect> EFFECTS = new ArrayList<Effect>();
    private static final List<Active> ACTIVE = new ArrayList<Active>();
    private static final RenderType PIPELINE = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "dandan_native_lights/materials.png"));
    private static ClientLevel activeWorld;

    private LightRenderer() {
    }

    public static void receive(LightPacket lightPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !lightPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)lightPacket.dimension())) {
            return;
        }
        if (activeWorld != clientLevel) {
            ACTIVE.clear();
            activeWorld = clientLevel;
        }
        if (lightPacket.kind() == 12) {
            ACTIVE.removeIf(active -> active.packet().owner().equals(lightPacket.owner()) && (active.packet().kind() == 0 || active.packet().kind() == 2 || lightPacket.index() == 3 && active.packet().kind() == 7));
            return;
        }
        if (ACTIVE.size() >= 64) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Active(lightPacket, clientLevel.m_46467_(), new HashMap<Long, Double>()));
    }

    /*
     * Could not resolve type clashes
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        double[] dArray;
        Object object;
        Object object2;
        LightPacket lightPacket;
        Active active2;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != activeWorld) {
            ACTIVE.clear();
            activeWorld = clientLevel;
            return;
        }
        if (EFFECTS.size() != 12) {
            return;
        }
        ACTIVE.removeIf(active -> {
            if (clientLevel.m_46467_() - active.born() >= (long)active.packet().ticks()) {
                return true;
            }
            if (active.packet().kind() != 0 && active.packet().kind() != 2 && active.packet().kind() != 7) {
                return false;
            }
            Entity entity = clientLevel.m_6815_(active.packet().ownerId());
            return entity == null || !entity.m_20148_().equals(active.packet().owner()) || !entity.m_6084_();
        });
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        Vec3 vec32 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20154_();
        double[] dArray2 = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        double[] dArray3 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 3 : (particleStatus == ParticleStatus.DECREASED ? 6 : 10);
        ArrayList<Active> arrayList = new ArrayList<Active>(ACTIVE);
        arrayList.sort(Comparator.comparingDouble(active -> active.packet().origin().m_82557_(vec3)));
        ArrayList<AABB> arrayList2 = new ArrayList<AABB>();
        for (AbstractClientPlayer abstractClientPlayer : clientLevel.m_6907_()) {
            if (!(abstractClientPlayer.m_20182_().m_82557_(vec3) < 6400.0)) continue;
            arrayList2.add(abstractClientPlayer.m_20191_().m_82400_(0.04));
        }
        ArrayList arrayList3 = new ArrayList();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        for (int i = 0; i < Math.min(n, arrayList.size()); ++i) {
            active2 = (Active)arrayList.get(i);
            lightPacket = active2.packet();
            double d = lightPacket.origin().m_82557_(vec3);
            if (d > 9216.0) continue;
            boolean bl2 = lightPacket.kind() == 4 || lightPacket.kind() == 5;
            object2 = bl2 ? clientLevel.m_6815_(lightPacket.ownerId()) : null;
            object = (lightPacket.kind() == 4 || lightPacket.kind() == 5) && object2 != null && object2.m_20148_().equals(lightPacket.owner()) ? object2.m_20191_().m_82400_(0.04) : null;
            double d2 = lightPacket.radius() + 1.0f;
            if (!renderLevelStageEvent.getFrustum().m_113029_(new AABB(lightPacket.origin().f_82479_ - d2, lightPacket.origin().f_82480_ - 0.3, lightPacket.origin().f_82481_ - d2, lightPacket.origin().f_82479_ + d2, lightPacket.origin().f_82480_ + 9.0, lightPacket.origin().f_82481_ + d2))) continue;
            double d3 = ((float)(clientLevel.m_46467_() - active2.born()) + f) / (float)lightPacket.ticks();
            dArray = new double[]{lightPacket.origin().f_82479_, lightPacket.origin().f_82480_, lightPacket.origin().f_82481_};
            for (Part part : EFFECTS.get(lightPacket.kind()).parts()) {
                double[] dArray4;
                if (part.detail() && (particleStatus == ParticleStatus.MINIMAL || d > 2304.0) || (dArray4 = LightMotion.sample(part.keys(), d3))[7] < 0.012) continue;
                double[] dArray5 = LightMotion.worldTransform(dArray4, LightMotion.partRadius(part.fixedRadius(), lightPacket.radius()), lightPacket.yaw(), dArray);
                for (double[][] dArray6 : part.faces()) {
                    Object object3;
                    Object object42;
                    double[][] dArray7 = new double[4][3];
                    double[] dArray8 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
                    for (int j = 0; j < 4; ++j) {
                        LightMotion.transformInto(dArray6[j], dArray5, dArray7[j]);
                    }
                    if (part.ground()) {
                        Vec3 vec33 = new Vec3((dArray7[0][0] + dArray7[2][0]) / 2.0, lightPacket.origin().f_82480_, (dArray7[0][2] + dArray7[2][2]) / 2.0);
                        long l = (long)Math.floor(vec33.f_82479_ * 4.0) << 32 ^ (long)Math.floor(vec33.f_82481_ * 4.0) & 0xFFFFFFFFL;
                        object42 = active2.ground().get(l);
                        if (object42 == null) {
                            object3 = clientLevel.m_45547_(new ClipContext(vec33.m_82520_(0.0, 2.0, 0.0), vec33.m_82520_(0.0, -3.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
                            object42 = object3.m_6662_() == HitResult.Type.MISS ? Double.NaN : object3.m_82450_().f_82480_ - lightPacket.origin().f_82480_;
                            if (active2.ground().size() < 4096) {
                                active2.ground().put(l, (Double)object42);
                            }
                        }
                        if (!Double.isFinite((Double)object42)) continue;
                        object3 = dArray7;
                        int n2 = ((AABB)object3).length;
                        for (int j = 0; j < n2; ++j) {
                            AABB aABB;
                            AABB aABB2 = aABB = object3[j];
                            aABB2[1] = aABB2[1] + (Double)object42;
                        }
                    }
                    for (Object object42 : dArray7) {
                        for (int j = 0; j < 3; ++j) {
                            dArray8[j] = Math.min(dArray8[j], object42[j]);
                            dArray8[j + 3] = Math.max(dArray8[j + 3], object42[j]);
                        }
                    }
                    float f2 = (float)dArray4[7];
                    if (bl) {
                        f2 = (float)((double)f2 * LightMotion.sightFade(dArray8, dArray2, dArray3));
                    }
                    AABB aABB = new AABB(dArray8[0], dArray8[1], dArray8[2], dArray8[3], dArray8[4], dArray8[5]);
                    int n3 = object != null && aABB.m_82381_(object) ? 1 : 0;
                    object42 = arrayList2.iterator();
                    while (object42.hasNext()) {
                        object3 = (AABB)object42.next();
                        if (!aABB.m_82381_(object3)) continue;
                        n3 = 1;
                        break;
                    }
                    if (n3 != 0) {
                        f2 *= 0.14f;
                    }
                    if ((double)f2 < 0.012) continue;
                    double d4 = aABB.m_82399_().m_82557_(vec3);
                    arrayList3.add(new Panel(dArray7, part.material(), f2, d4));
                }
            }
        }
        arrayList3.sort(Comparator.comparingDouble(Panel::distance).reversed());
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        active2 = bufferSource.m_6299_(PIPELINE);
        lightPacket = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        float[][] fArrayArray = new float[][]{{0.0f, 0.0f}, {0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}};
        Iterator iterator = arrayList3.iterator();
        while (iterator.hasNext()) {
            object2 = (Panel)iterator.next();
            object = ((Panel)object2).points()[0];
            double[] dArray9 = ((Panel)object2).points()[1];
            double[] dArray10 = ((Panel)object2).points()[2];
            Vec3 vec34 = new Vec3(dArray9[0] - object[0], dArray9[1] - object[1], dArray9[2] - object[2]).m_82537_(new Vec3(dArray10[0] - object[0], dArray10[1] - object[1], dArray10[2] - object[2])).m_82541_();
            for (int i = 0; i < 4; ++i) {
                dArray = ((Panel)object2).points()[i];
                active2.m_252986_((Matrix4f)lightPacket, (float)(dArray[0] - vec3.f_82479_), (float)(dArray[1] - vec3.f_82480_), (float)(dArray[2] - vec3.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, ((Panel)object2).alpha()).m_7421_(((float)(((Panel)object2).material() * 16 + 1) + fArrayArray[i][0] * 14.0f) / 128.0f, (1.0f + fArrayArray[i][1] * 14.0f) / 16.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)vec34.f_82479_, (float)vec34.f_82480_, (float)vec34.f_82481_).m_5752_();
            }
        }
        bufferSource.m_109912_(PIPELINE);
    }

    static {
        try (InputStream inputStream = LightRenderer.class.getResourceAsStream("/assets/ftboss/dandan_native_lights/effects.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Dandan native light data is missing");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("effects")) {
                ArrayList<Part> arrayList = new ArrayList<Part>();
                for (JsonElement jsonElement2 : jsonElement.getAsJsonObject().getAsJsonArray("parts")) {
                    double d;
                    int n;
                    JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
                    JsonArray jsonArray = jsonObject2.getAsJsonArray("faces");
                    double[][][] dArray = new double[jsonArray.size()][4][3];
                    for (int i = 0; i < dArray.length; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            for (n = 0; n < 3; ++n) {
                                dArray[i][j][n] = jsonArray.get(i).getAsJsonArray().get(j).getAsJsonArray().get(n).getAsDouble();
                            }
                        }
                    }
                    JsonArray jsonArray2 = jsonObject2.getAsJsonArray("keys");
                    double[][] dArray2 = new double[jsonArray2.size()][9];
                    for (n = 0; n < dArray2.length; ++n) {
                        for (int i = 0; i < 9; ++i) {
                            dArray2[n][i] = jsonArray2.get(n).getAsJsonArray().get(i).getAsDouble();
                        }
                    }
                    double d2 = d = jsonObject2.has("fixed_radius") ? jsonObject2.get("fixed_radius").getAsDouble() : 0.0;
                    if (!Double.isFinite(d) || d < 0.0 || d > 4.0) {
                        throw new IllegalStateException("Invalid local Dandan light scale");
                    }
                    arrayList.add(new Part(dArray, jsonObject2.get("material").getAsInt(), dArray2, jsonObject2.get("detail").getAsBoolean(), jsonObject2.get("ground").getAsBoolean(), d));
                }
                EFFECTS.add(new Effect(arrayList));
            }
        }
        catch (Exception exception) {
            EFFECTS.clear();
            LogUtils.getLogger().error("Cannot load independent Dandan native lights", (Throwable)exception);
        }
    }

    private record Active(LightPacket packet, long born, Map<Long, Double> ground) {
    }

    private record Effect(List<Part> parts) {
    }

    private record Part(double[][][] faces, int material, double[][] keys, boolean detail, boolean ground, double fixedRadius) {
    }

    private record Panel(double[][] points, int material, float alpha, double distance) {
    }
}

