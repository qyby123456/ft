/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render.reapflat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapMotion;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class FlatReapRenderer {
    private static final List<Panel> PANELS = new ArrayList<Panel>();
    private static final RenderType PIPELINE = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "reap_flat/materials.png"));
    private static double nativeSpan = 1.0;
    private static double placementHeight;
    private static double animationRate;
    private static final Map<OptimizedSpellVisual, double[]> RELEASE_ANCHORS;
    private static ClientLevel anchorWorld;

    private FlatReapRenderer() {
    }

    public static List<?> intercept(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        Object object2;
        if (anchorWorld != clientLevel) {
            RELEASE_ANCHORS.clear();
            anchorWorld = clientLevel;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        for (Object object2 : list) {
            OptimizedSpellVisual optimizedSpellVisual2;
            if (object2 instanceof OptimizedSpellVisual && (optimizedSpellVisual2 = (OptimizedSpellVisual)object2).packet() != null && optimizedSpellVisual2.packet().spell() == SpellVisualRules.Spell.CANDLE_REAP) {
                SpellVisualPacket spellVisualPacket = optimizedSpellVisual2.packet();
                if (!spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_()) || !(spellVisualPacket.start().m_82557_(vec3) < 6400.0)) continue;
                arrayList2.add(optimizedSpellVisual2);
                continue;
            }
            arrayList.add(object2);
        }
        RELEASE_ANCHORS.keySet().retainAll(arrayList2);
        if (arrayList2.isEmpty() || PANELS.isEmpty()) {
            return arrayList;
        }
        arrayList2.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3)));
        Minecraft minecraft = Minecraft.m_91087_();
        object2 = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = object2 == ParticleStatus.MINIMAL ? 2 : (object2 == ParticleStatus.DECREASED ? 4 : 6);
        boolean bl = object2 != ParticleStatus.MINIMAL;
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        Vec3 vec32 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20154_();
        double[] dArray2 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        boolean bl2 = !renderLevelStageEvent.getCamera().m_90594_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(PIPELINE);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        ArrayList<AABB> arrayList3 = new ArrayList<AABB>();
        for (Object object3 : clientLevel.m_6907_()) {
            if (!(object3.m_20318_(f).m_82557_(vec3) < 6400.0)) continue;
            arrayList3.add(object3.m_20191_().m_82400_(0.06));
        }
        for (int i = Math.min(n, arrayList2.size()) - 1; i >= 0; --i) {
            double d;
            Vec3 vec33;
            Object object3;
            object3 = (OptimizedSpellVisual)arrayList2.get(i);
            SpellVisualPacket spellVisualPacket = object3.packet();
            Entity entity2 = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity2 != null && !entity2.m_20148_().equals(spellVisualPacket.owner())) {
                entity2 = null;
            }
            Vec3 vec34 = vec33 = entity2 == null ? spellVisualPacket.start() : entity2.m_20318_(f);
            if (!renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec33.f_82479_ - 6.0, vec33.f_82480_ - 1.0, vec33.f_82481_ - 6.0, vec33.f_82479_ + 6.0, vec33.f_82480_ + 5.0, vec33.f_82481_ + 8.0))) continue;
            int n2 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
            boolean bl3 = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
            double d2 = Math.max(1.0, bl3 ? (double)n2 : (double)n2 / animationRate);
            double d3 = Math.max(0.0f, (float)(clientLevel.m_46467_() - object3.born()) + f);
            double d4 = Math.max(0.0f, (float)(clientLevel.m_46467_() - object3.refreshed()) + f);
            float f2 = (float)(1.0 - FlatReapMotion.smooth((d4 - d2 * 0.72) / Math.max(1.0, d2 * 0.28)));
            if ((double)f2 < 0.012) continue;
            double d5 = FlatReapMotion.phaseClock(d3, n2, bl3, animationRate);
            double d6 = 4.6 / nativeSpan * (bl3 ? 0.72 + 0.28 * FlatReapMotion.smooth(d3 * animationRate / 12.0) : 1.0);
            Vec3 vec35 = entity2 == null ? spellVisualPacket.end().m_82546_(spellVisualPacket.start()) : entity2.m_20154_();
            double d7 = Math.hypot(vec35.f_82479_, vec35.f_82481_);
            double d8 = d7 < 1.0E-8 ? 0.0 : vec35.f_82479_ / d7;
            double d9 = d7 < 1.0E-8 ? 1.0 : vec35.f_82481_ / d7;
            double d10 = d = entity2 == null ? 0.3 : (double)(entity2.m_20205_() / 2.0f);
            if (!bl3 && !RELEASE_ANCHORS.containsKey(object3)) {
                RELEASE_ANCHORS.put((OptimizedSpellVisual)object3, new double[]{vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, d8, d9, d});
            }
            List<Positioned> list2 = FlatReapRenderer.place(d5, d6, vec33, d8, d9, d, vec3, RELEASE_ANCHORS.get(object3));
            list2.sort(Comparator.comparingDouble(Positioned::distance).reversed());
            for (Positioned positioned : list2) {
                if (positioned.panel().energy() && !bl) continue;
                float f3 = f2 * positioned.panel().alpha();
                if (positioned.panel().energy()) {
                    f3 = (float)((double)f3 * (bl3 ? 0.0 : FlatReapMotion.trailAlpha(d5, positioned.panel().emitted())));
                }
                f3 = (float)((double)f3 * FlatReapMotion.visibleAlpha(positioned.limits(), dArray, dArray2, bl2));
                AABB aABB = new AABB(positioned.limits()[0], positioned.limits()[1], positioned.limits()[2], positioned.limits()[3], positioned.limits()[4], positioned.limits()[5]);
                for (AABB aABB2 : arrayList3) {
                    if (!aABB.m_82381_(aABB2)) continue;
                    f3 *= 0.12f;
                    break;
                }
                if (!((double)f3 >= 0.012)) continue;
                FlatReapRenderer.writePanel(vertexConsumer, positioned, matrix4f, matrix3f, vec3, f3);
            }
        }
        bufferSource.m_109912_(PIPELINE);
        return arrayList;
    }

    private static List<Positioned> place(double d, double d2, Vec3 vec3, double d3, double d4, double d5, Vec3 vec32, double[] dArray) {
        double[] dArray2 = FlatReapMotion.rotation(d, 30);
        ArrayList<double[][]> arrayList = new ArrayList<double[][]>(PANELS.size());
        double d6 = Double.POSITIVE_INFINITY;
        double d7 = Double.NEGATIVE_INFINITY;
        double d8 = Double.POSITIVE_INFINITY;
        for (Panel panel : PANELS) {
            double[][] dArrayArray = new double[4][];
            for (int i = 0; i < 4; ++i) {
                dArrayArray[i] = panel.energy() ? (double[])panel.vertices()[i].clone() : FlatReapMotion.transform(panel.vertices()[i], panel.pivot(), dArray2);
                int n = 0;
                while (n < 3) {
                    double[] dArray3 = dArrayArray[i];
                    int n2 = n++;
                    dArray3[n2] = dArray3[n2] * d2;
                }
                if (panel.energy()) continue;
                d6 = Math.min(d6, dArrayArray[i][1]);
                d7 = Math.max(d7, dArrayArray[i][1]);
                d8 = Math.min(d8, dArrayArray[i][2]);
            }
            arrayList.add(dArrayArray);
        }
        double d9 = FlatReapMotion.placementLift(d6, d7, placementHeight);
        double d10 = 0.55 + d5 - d8;
        ArrayList<Positioned> arrayList2 = new ArrayList<Positioned>(PANELS.size());
        for (int i = 0; i < PANELS.size(); ++i) {
            double[] dArray4 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            double d11 = 0.0;
            boolean bl = PANELS.get(i).energy();
            for (double[] dArray5 : (double[][])arrayList.get(i)) {
                if (bl && dArray != null) {
                    double[] dArray6 = FlatReapMotion.anchoredTrailPoint(dArray5, dArray);
                    System.arraycopy(dArray6, 0, dArray5, 0, 3);
                } else {
                    double d12 = dArray5[0];
                    double d13 = dArray5[2] + (bl ? d5 - 0.3 : d10);
                    dArray5[0] = vec3.f_82479_ + d12 * d4 + d13 * d3;
                    dArray5[1] = dArray5[1] + (vec3.f_82480_ + (bl ? 0.0 : d9));
                    dArray5[2] = vec3.f_82481_ - d12 * d3 + d13 * d4;
                }
                for (int j = 0; j < 3; ++j) {
                    dArray4[j] = Math.min(dArray4[j], dArray5[j]);
                    dArray4[j + 3] = Math.max(dArray4[j + 3], dArray5[j]);
                }
                d11 += Math.pow(dArray5[0] - vec32.f_82479_, 2.0) + Math.pow(dArray5[1] - vec32.f_82480_, 2.0) + Math.pow(dArray5[2] - vec32.f_82481_, 2.0);
            }
            arrayList2.add(new Positioned(PANELS.get(i), (double[][])arrayList.get(i), dArray4, d11 / 4.0));
        }
        return arrayList2;
    }

    private static void writePanel(VertexConsumer vertexConsumer, Positioned positioned, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, float f) {
        double[][] dArray = positioned.corners();
        Vec3 vec32 = new Vec3(dArray[1][0] - dArray[0][0], dArray[1][1] - dArray[0][1], dArray[1][2] - dArray[0][2]);
        Vec3 vec33 = new Vec3(dArray[2][0] - dArray[0][0], dArray[2][1] - dArray[0][1], dArray[2][2] - dArray[0][2]);
        Vec3 vec34 = vec32.m_82537_(vec33).m_82541_();
        int n = positioned.panel().slot();
        int n2 = n >= 9 && n <= 11 || positioned.panel().energy() ? 255 : (int)(255.0 * (0.72 + 0.28 * Math.abs(vec34.f_82479_ * -0.45 + vec34.f_82480_ * 0.8 - vec34.f_82481_ * 0.4)));
        for (int i = 0; i < 4; ++i) {
            float f2 = (float)(((double)(n % 8 * 16 + 2) + positioned.panel().uv()[i][0] * 11.0) / 128.0);
            float f3 = (float)(((double)(n / 8 * 16 + 2) + positioned.panel().uv()[i][1] * 11.0) / 32.0);
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - vec3.f_82479_), (float)(dArray[i][1] - vec3.f_82480_), (float)(dArray[i][2] - vec3.f_82481_)).m_6122_(n2, n2, n2, Math.round(f * 255.0f)).m_7421_(f2, f3).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)vec34.f_82479_, (float)vec34.f_82480_, (float)vec34.f_82481_).m_5752_();
        }
    }

    static {
        RELEASE_ANCHORS = new IdentityHashMap<OptimizedSpellVisual, double[]>();
        try (InputStream inputStream = FlatReapRenderer.class.getResourceAsStream("/assets/ftboss/reap_flat/model.json");){
            if (inputStream == null) {
                throw new IllegalStateException("New horizontal scythe asset is missing");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            placementHeight = jsonObject.getAsJsonObject("scene").get("height").getAsDouble();
            if (!Double.isFinite(placementHeight) || placementHeight <= 0.0) {
                throw new IllegalStateException("Invalid scythe placement height");
            }
            animationRate = jsonObject.get("animationRate").getAsDouble();
            if (!Double.isFinite(animationRate) || animationRate <= 0.0) {
                throw new IllegalStateException("Invalid scythe animation rate");
            }
            double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("faces")) {
                int n;
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                double[][] dArray3 = new double[4][3];
                double[][] dArray4 = new double[4][2];
                double[] dArray5 = new double[3];
                for (n = 0; n < 3; ++n) {
                    dArray5[n] = jsonObject2.getAsJsonArray("pivot").get(n).getAsDouble();
                }
                for (n = 0; n < 4; ++n) {
                    int n2;
                    for (n2 = 0; n2 < 3; ++n2) {
                        dArray3[n][n2] = jsonObject2.getAsJsonArray("points").get(n).getAsJsonArray().get(n2).getAsDouble();
                        dArray[n2] = Math.min(dArray[n2], dArray3[n][n2]);
                        dArray2[n2] = Math.max(dArray2[n2], dArray3[n][n2]);
                    }
                    for (n2 = 0; n2 < 2; ++n2) {
                        dArray4[n][n2] = jsonObject2.getAsJsonArray("uv").get(n).getAsJsonArray().get(n2).getAsDouble();
                    }
                }
                PANELS.add(new Panel(dArray3, dArray4, dArray5, jsonObject2.get("bone").getAsInt(), jsonObject2.get("material").getAsInt(), jsonObject2.get("opacity").getAsFloat(), jsonObject2.get("group").getAsString().equals("detail"), jsonObject2.has("emitted") ? jsonObject2.get("emitted").getAsDouble() : -1.0));
            }
            for (int i = 0; i < 3; ++i) {
                nativeSpan = Math.max(nativeSpan, dArray2[i] - dArray[i]);
            }
            nativeSpan = jsonObject.getAsJsonObject("scene").get("nativeSpan").getAsDouble();
            if (!Double.isFinite(nativeSpan) || nativeSpan <= 0.0) {
                throw new IllegalStateException("Invalid independent weapon scale");
            }
        }
        catch (Exception exception) {
            PANELS.clear();
            LogUtils.getLogger().error("Cannot load from-zero horizontal candle scythe", (Throwable)exception);
        }
    }

    private record Positioned(Panel panel, double[][] corners, double[] limits, double distance) {
    }

    private record Panel(double[][] vertices, double[][] uv, double[] pivot, int joint, int slot, float alpha, boolean energy, double emitted) {
    }
}

