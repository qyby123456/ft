/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render.dandan;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.dandan.DandanMotion;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class DandanSpellRenderer {
    private static final Map<String, Model> MODELS = DandanSpellRenderer.load();
    private static final RenderType LAYER = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/dandan/materials.png"));

    private DandanSpellRenderer() {
    }

    private static double[] vector(JsonArray jsonArray) {
        if (jsonArray.size() != 3) {
            throw new IllegalArgumentException("Invalid Dandan vector");
        }
        double[] dArray = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 32.0)) continue;
            throw new IllegalArgumentException("Invalid Dandan coordinate");
        }
        return dArray;
    }

    private static Map<String, Model> load() {
        LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
        for (String string : List.of("candle_reap", "candle_prison", "candle_eclipse")) {
            try {
                InputStream inputStream = DandanSpellRenderer.class.getResourceAsStream("/assets/ftboss/dandan/" + string + ".json");
                try {
                    if (inputStream == null) {
                        throw new IllegalStateException("Missing independent Dandan model " + string);
                    }
                    JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonObject jsonObject2 = jsonObject.getAsJsonObject("scene");
                    double[] dArray = new double[]{32.0, 32.0, 32.0};
                    double[] dArray2 = new double[]{-32.0, -32.0, -32.0};
                    ArrayList<Face> arrayList = new ArrayList<Face>();
                    for (JsonElement jsonElement : jsonObject.getAsJsonArray("faces")) {
                        int n;
                        JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                        if (jsonObject3.getAsJsonArray("points").size() != 4 || jsonObject3.getAsJsonArray("uv").size() != 4) {
                            throw new IllegalArgumentException("Invalid Dandan face");
                        }
                        double[][] dArray3 = new double[4][3];
                        double[][] dArray4 = new double[4][2];
                        for (n = 0; n < 4; ++n) {
                            int n2;
                            dArray3[n] = DandanSpellRenderer.vector(jsonObject3.getAsJsonArray("points").get(n).getAsJsonArray());
                            JsonArray jsonArray = jsonObject3.getAsJsonArray("uv").get(n).getAsJsonArray();
                            if (jsonArray.size() != 2) {
                                throw new IllegalArgumentException("Invalid Dandan UV");
                            }
                            for (n2 = 0; n2 < 2; ++n2) {
                                dArray4[n][n2] = jsonArray.get(n2).getAsDouble();
                                if (Double.isFinite(dArray4[n][n2]) && !(dArray4[n][n2] < 0.0) && !(dArray4[n][n2] > 1.0)) continue;
                                throw new IllegalArgumentException("Dandan UV outside material");
                            }
                            for (n2 = 0; n2 < 3; ++n2) {
                                dArray[n2] = Math.min(dArray[n2], dArray3[n][n2]);
                                dArray2[n2] = Math.max(dArray2[n2], dArray3[n][n2]);
                            }
                        }
                        n = jsonObject3.get("material").getAsInt();
                        int n3 = jsonObject3.get("bone").getAsInt();
                        float f = jsonObject3.get("opacity").getAsFloat();
                        if (n < 0 || n >= 16 || !Float.isFinite(f) || f <= 0.0f || f > 1.0f) {
                            throw new IllegalArgumentException("Invalid Dandan material");
                        }
                        if (n3 != 0 && n3 != 1 && n3 != 5 && n3 != 6 && n3 != 7 && n3 != 9 && (n3 < 10 || n3 > 14)) {
                            throw new IllegalArgumentException("Invalid Dandan joint");
                        }
                        arrayList.add(new Face(dArray3, dArray4, DandanSpellRenderer.vector(jsonObject3.getAsJsonArray("pivot")), n, n3, f, jsonObject3.get("group").getAsString().equals("detail")));
                    }
                    double d = Math.max(dArray2[0] - dArray[0], Math.max(dArray2[1] - dArray[1], dArray2[2] - dArray[2]));
                    if (d <= 0.01 || arrayList.size() > 6000) {
                        throw new IllegalArgumentException("Invalid Dandan geometry budget");
                    }
                    linkedHashMap.put(string, new Model(string, jsonObject2.get("anchor").getAsString(), jsonObject2.get("span").getAsDouble(), jsonObject2.get("height").getAsDouble(), d, List.copyOf(arrayList)));
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot load independently authored Dandan model " + string, exception);
            }
        }
        return Map.copyOf(linkedHashMap);
    }

    public static List<?> renderAndExclude(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        int n;
        SpellVisualPacket spellVisualPacket;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || list.isEmpty()) {
            return list;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel2 = minecraft.f_91073_;
        if (clientLevel2 == null || clientLevel2 != clientLevel) {
            return list;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_() && entity != null;
        Vec3 vec32 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20154_();
        double[] dArray2 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        ArrayList arrayList = new ArrayList();
        ArrayList<Draw> arrayList2 = new ArrayList<Draw>();
        for (Object obj : list) {
            double d;
            OptimizedSpellVisual optimizedSpellVisual;
            if (!(obj instanceof OptimizedSpellVisual) || (optimizedSpellVisual = (OptimizedSpellVisual)obj).packet() == null || optimizedSpellVisual.packet().spell() == null || !MODELS.containsKey(optimizedSpellVisual.packet().spell().id)) {
                arrayList.add(obj);
                continue;
            }
            spellVisualPacket = optimizedSpellVisual.packet();
            if (!spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel2.m_46472_().m_135782_()) || !((d = spellVisualPacket.start().m_82557_(vec3)) < 6400.0)) continue;
            arrayList2.add(new Draw(optimizedSpellVisual, d));
        }
        if (arrayList2.isEmpty()) {
            return arrayList;
        }
        arrayList2.sort(Comparator.comparingDouble(Draw::distance));
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 2 : (n = particleStatus == ParticleStatus.DECREASED ? 4 : 6);
        int n3 = particleStatus == ParticleStatus.MINIMAL ? 6000 : (particleStatus == ParticleStatus.DECREASED ? 12000 : 18000);
        spellVisualPacket = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = spellVisualPacket.m_6299_(LAYER);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        ArrayList<double[]> arrayList3 = new ArrayList<double[]>();
        for (AbstractClientPlayer abstractClientPlayer : clientLevel2.m_6907_()) {
            if (!(abstractClientPlayer.m_20318_(f).m_82557_(vec3) < 4096.0)) continue;
            arrayList3.add(DandanSpellRenderer.actorBox(abstractClientPlayer.m_20191_().m_82400_(0.06)));
        }
        int n4 = 0;
        for (int i = Math.min(n, arrayList2.size()) - 1; i >= 0; --i) {
            double d;
            OptimizedSpellVisual optimizedSpellVisual = ((Draw)arrayList2.get(i)).visual();
            SpellVisualPacket spellVisualPacket2 = optimizedSpellVisual.packet();
            Model model = MODELS.get(spellVisualPacket2.spell().id);
            Entity entity2 = clientLevel2.m_6815_(spellVisualPacket2.ownerId());
            if (entity2 != null && !entity2.m_20148_().equals(spellVisualPacket2.owner())) {
                entity2 = null;
            }
            boolean bl2 = spellVisualPacket2.phase().name().equals("CHARGE");
            double d2 = Math.max(0.0f, (float)(clientLevel2.m_46467_() - optimizedSpellVisual.born()) + f);
            double d3 = Math.max(0.0f, (float)(clientLevel2.m_46467_() - optimizedSpellVisual.refreshed()) + f);
            float f2 = (float)DandanMotion.fade(d3, spellVisualPacket2.spell().lifetime(spellVisualPacket2.phase()));
            if ((double)f2 < 0.01) continue;
            Vec3 vec33 = model.anchor().equals("FRONT") && entity2 != null ? entity2.m_20318_(f) : spellVisualPacket2.start();
            double d4 = model.span() + (entity2 == null ? 0.6 : (double)entity2.m_20205_()) + 0.8;
            double d5 = d4 + (entity2 == null ? 1.8 : (double)entity2.m_20206_()) + model.height();
            AABB aABB = new AABB(vec33.f_82479_ - d4, vec33.f_82480_ - d4, vec33.f_82481_ - d4, vec33.f_82479_ + d4, vec33.f_82480_ + d5, vec33.f_82481_ + d4);
            if (!renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            double d6 = spellVisualPacket2.end().f_82479_ - spellVisualPacket2.start().f_82479_;
            double d7 = spellVisualPacket2.end().f_82481_ - spellVisualPacket2.start().f_82481_;
            if (d6 * d6 + d7 * d7 < 1.0E-8 && entity2 != null) {
                d6 = entity2.m_20154_().f_82479_;
                d7 = entity2.m_20154_().f_82481_;
            }
            if (!Double.isFinite(d = Math.hypot(d6, d7)) || d < 1.0E-8) {
                d6 = 0.0;
                d7 = 1.0;
                d = 1.0;
            }
            double d8 = model.span() / model.nativeSpan() * DandanMotion.emergence(d2, bl2);
            double d9 = bl2 ? 15.0 * Math.min(1.0, d2 / (double)Math.max(1, spellVisualPacket2.spell().lifetime(spellVisualPacket2.phase()))) : 15.0 + 24.166 * Math.min(1.0, d2 / (double)Math.max(1, spellVisualPacket2.spell().lifetime(spellVisualPacket2.phase())));
            List<Surface> list2 = DandanSpellRenderer.surfaces(model, d2, d9, vec33, d6 /= d, d7 /= d, d8, entity2, vec3);
            list2.sort(Comparator.comparingDouble(Surface::distance).reversed());
            double[] dArray3 = entity2 == null ? null : DandanSpellRenderer.actorBox(entity2.m_20191_().m_82400_(0.08));
            for (boolean bl3 : new boolean[]{false, true}) {
                for (Surface surface : list2) {
                    if (surface.face().detail() != bl3 || n4 >= n3) continue;
                    float f3 = f2 * surface.face().opacity();
                    if (!bl2 && model.id().equals("candle_prison") && surface.face().bone() >= 10) {
                        f3 *= 0.84f + 0.16f * (float)Math.cos(d2 * 0.7 - (double)(surface.face().bone() - 10) * 1.256);
                    }
                    if (bl2 && bl3) {
                        f3 *= 0.28f + 0.72f * (float)DandanMotion.smooth(d2 / 12.0);
                    }
                    f3 *= (float)DandanMotion.cameraFade(DandanMotion.cameraDistance(surface.bounds(), dArray));
                    if (bl && DandanMotion.blocksSight(surface.bounds(), dArray, dArray2)) {
                        f3 *= bl3 ? 0.45f : 0.16f;
                    }
                    for (double[] dArray4 : arrayList3) {
                        if (!DandanMotion.overlaps(surface.bounds(), dArray4)) continue;
                        f3 *= 0.12f;
                        break;
                    }
                    if (dArray3 != null && DandanMotion.overlaps(surface.bounds(), dArray3)) {
                        f3 *= 0.15f;
                    }
                    if (f3 < 0.012f) continue;
                    DandanSpellRenderer.emit(vertexConsumer, matrix4f, matrix3f, surface, vec3, f3);
                    ++n4;
                }
            }
        }
        spellVisualPacket.m_109912_(LAYER);
        return arrayList;
    }

    private static double[] actorBox(AABB aABB) {
        return new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
    }

    private static List<Surface> surfaces(Model model, double d, double d2, Vec3 vec3, double d3, double d4, double d5, Entity entity, Vec3 vec32) {
        ArrayList<double[][]> arrayList = new ArrayList<double[][]>();
        double[] dArray = new double[]{32.0, 32.0, 32.0};
        double[] dArray2 = new double[]{-32.0, -32.0, -32.0};
        DandanMotion.WeaponPose weaponPose = DandanMotion.weaponPose(d2);
        for (Face face : model.faces()) {
            double[][] dArray3 = new double[4][3];
            for (int i = 0; i < 4; ++i) {
                dArray3[i] = face.bone() == 1 ? DandanMotion.weaponPoint(face.points()[i], face.pivot(), weaponPose) : DandanMotion.point(face.points()[i], face.bone(), face.pivot(), d);
                for (int j = 0; j < 3; ++j) {
                    double[] dArray4 = dArray3[i];
                    int n = j;
                    dArray4[n] = dArray4[n] * d5;
                    dArray[j] = Math.min(dArray[j], dArray3[i][j]);
                    dArray2[j] = Math.max(dArray2[j], dArray3[i][j]);
                }
            }
            arrayList.add(dArray3);
        }
        double d6 = Math.max(0.18 - dArray[1], model.height() - (dArray[1] + dArray2[1]) / 2.0);
        double d7 = 0.0;
        if (model.anchor().equals("FRONT")) {
            d7 = 0.55 + (entity == null ? 0.3 : (double)(entity.m_20205_() / 2.0f)) - dArray[2];
        }
        if (model.anchor().equals("ABOVE")) {
            d6 = Math.max(d6, (entity == null ? 1.8 : (double)entity.m_20206_()) + 0.65 - dArray[1]);
        }
        ArrayList<Surface> arrayList2 = new ArrayList<Surface>();
        for (int i = 0; i < arrayList.size(); ++i) {
            double[][] dArray5 = (double[][])arrayList.get(i);
            double d8 = 0.0;
            double[] dArray6 = new double[3];
            for (double[] dArray7 : dArray5) {
                double d9 = dArray7[0];
                double d10 = dArray7[2] + d7;
                dArray7[0] = vec3.f_82479_ + d9 * d4 + d10 * d3;
                dArray7[1] = dArray7[1] + (vec3.f_82480_ + d6);
                dArray7[2] = vec3.f_82481_ - d9 * d3 + d10 * d4;
                for (int j = 0; j < 3; ++j) {
                    int n = j;
                    dArray6[n] = dArray6[n] + dArray7[j] / 4.0;
                }
            }
            d8 = Math.pow(dArray6[0] - vec32.f_82479_, 2.0) + Math.pow(dArray6[1] - vec32.f_82480_, 2.0) + Math.pow(dArray6[2] - vec32.f_82481_, 2.0);
            arrayList2.add(new Surface(model.faces().get(i), dArray5, DandanMotion.bounds(dArray5), d8));
        }
        return arrayList2;
    }

    private static void emit(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Surface surface, Vec3 vec3, float f) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        double d7;
        Face face = surface.face();
        double[][] dArray = surface.points();
        double d8 = dArray[1][1] - dArray[0][1];
        double d9 = d8 * (d7 = dArray[2][2] - dArray[0][2]) - (d6 = dArray[1][2] - dArray[0][2]) * (d5 = dArray[2][1] - dArray[0][1]);
        double d10 = Math.sqrt(d9 * d9 + (d4 = d6 * (d3 = dArray[2][0] - dArray[0][0]) - (d2 = dArray[1][0] - dArray[0][0]) * d7) * d4 + (d = d2 * d5 - d8 * d3) * d);
        if (d10 < 1.0E-8) {
            return;
        }
        int n = face.material() == 4 || face.material() >= 10 && face.material() != 12 && face.material() != 13 ? 250 : (int)(255.0 * (0.72 + 0.28 * Math.abs((d9 /= d10) * -0.45 + (d4 /= d10) * 0.8 + (d /= d10) * -0.4)));
        for (int i = 0; i < 4; ++i) {
            float f2 = (float)(((double)(face.material() % 8 * 16 + 2) + face.uv()[i][0] * 11.0) / 128.0);
            float f3 = (float)(((double)(face.material() / 8 * 16 + 2) + face.uv()[i][1] * 11.0) / 32.0);
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - vec3.f_82479_), (float)(dArray[i][1] - vec3.f_82480_), (float)(dArray[i][2] - vec3.f_82481_)).m_6122_(n, n, n, Math.round(f * 255.0f)).m_7421_(f2, f3).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)d9, (float)d4, (float)d).m_5752_();
        }
    }

    private record Face(double[][] points, double[][] uv, double[] pivot, int material, int bone, float opacity, boolean detail) {
    }

    private record Model(String id, String anchor, double span, double height, double nativeSpan, List<Face> faces) {
    }

    private record Draw(OptimizedSpellVisual visual, double distance) {
    }

    private record Surface(Face face, double[][] points, double[] bounds, double distance) {
    }
}

