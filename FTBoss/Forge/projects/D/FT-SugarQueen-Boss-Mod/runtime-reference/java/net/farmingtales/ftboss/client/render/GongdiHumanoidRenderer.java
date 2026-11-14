/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  org.joml.Quaterniond
 *  org.joml.Vector3d
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.manorrun.GongdiRunFrame;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import net.farmingtales.ftboss.client.render.GongdiHumanoidTimeline;
import net.farmingtales.ftboss.client.render.GongdiSpellCues;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiHumanoidRenderer {
    private static final Map<EasterBossModel, Actor> ACTORS = new WeakHashMap<EasterBossModel, Actor>();
    private static final Map<EasterBossEntity, Actor> ENTITY_ACTORS = new WeakHashMap<EasterBossEntity, Actor>();
    private static final Map<EasterBossEntity, GongdiHumanoidTimeline.State> STATES = new WeakHashMap<EasterBossEntity, GongdiHumanoidTimeline.State>();

    private GongdiHumanoidRenderer() {
    }

    public static boolean eligible(EasterBossEntity easterBossEntity) {
        if (easterBossEntity.getVariant() != 3 || !easterBossEntity.m_6084_() || easterBossEntity.m_20159_() || !easterBossEntity.m_21206_().m_41619_()) {
            return false;
        }
        ResourceLocation resourceLocation = BuiltInRegistries.f_257033_.m_7981_((Object)easterBossEntity.m_21205_().m_41720_());
        return resourceLocation != null && resourceLocation.toString().equals("ftboss:black_cleaver");
    }

    public static void setup(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity2, float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        ACTORS.remove((Object)easterBossModel);
        ENTITY_ACTORS.remove(easterBossEntity2);
        if (!GongdiHumanoidRenderer.eligible(easterBossEntity2)) {
            return;
        }
        float f6 = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity2.f_19797_));
        CompoundTag compoundTag = easterBossEntity2.getSkillCue();
        double d = (float)(easterBossEntity2.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + f6;
        boolean bl2 = compoundTag.m_128441_("gongdiSkill") && d >= 0.0 && d < (double)Math.max(1, compoundTag.m_128451_("duration")) && (easterBossEntity2.isCastingLike() || compoundTag.m_128471_("released"));
        GongdiSpellCues.Pose pose = GongdiSpellCues.current(easterBossEntity2, f6);
        if (pose != null && pose.released() && !easterBossEntity2.isCastingLike() && easterBossEntity2.getCombatAnimationWeight(f6) > 0.001f) {
            pose = null;
        }
        boolean bl3 = bl = bl2 || pose != null;
        if (easterBossEntity2.isCastingLike() && !bl) {
            return;
        }
        for (boolean bl4 : easterBossEntity2.getExtraBones()) {
            if (!bl4) continue;
            return;
        }
        double d2 = bl2 ? d / (double)Math.max(1, compoundTag.m_128451_("duration")) : (pose != null ? pose.progress() : (double)easterBossEntity2.getActionProgress(f6));
        long l = easterBossEntity2.m_9236_().m_46467_() - (long)Math.round(easterBossEntity2.getCombatAnimationAge(0.0f));
        GongdiHumanoidTimeline.Cue cue = new GongdiHumanoidTimeline.Cue(bl, bl2 ? compoundTag.m_128471_("released") : pose != null && pose.released(), bl2 ? compoundTag.m_128461_("gongdiSkill") : (pose != null ? pose.skill() : ""), d2, easterBossEntity2.getCombatAnimationAge(f6), easterBossEntity2.getCombatAnimationWeight(f6), easterBossEntity2.f_20916_ > 0, f2, f, f4, f5, l);
        Actor actor = new Actor(GongdiRunFrame.apply(GongdiHumanoidTimeline.select(STATES.computeIfAbsent(easterBossEntity2, easterBossEntity -> new GongdiHumanoidTimeline.State()), cue, f3), easterBossEntity2, f3));
        ACTORS.put(easterBossModel, actor);
        ENTITY_ACTORS.put(easterBossEntity2, actor);
    }

    public static boolean renderBody(EasterBossModel easterBossModel, PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        Actor actor = ACTORS.get((Object)easterBossModel);
        if (actor == null) {
            return false;
        }
        GongdiHumanoidRenderer.draw(actor.frame(), "skin", poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
        return true;
    }

    public static boolean renderWeapon(EasterBossEntity easterBossEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        Actor actor = ENTITY_ACTORS.get(easterBossEntity);
        if (actor == null || !GongdiHumanoidRenderer.eligible(easterBossEntity)) {
            return false;
        }
        ResourceLocation resourceLocation = new ResourceLocation("ftboss", "textures/item/black_cleaver_geo.png");
        GongdiHumanoidRenderer.draw(actor.frame(), "metal", poseStack, multiBufferSource.m_6299_(RenderType.m_110458_((ResourceLocation)resourceLocation)), n, OverlayTexture.f_118083_, 1.0f, 1.0f, 1.0f, 1.0f);
        return true;
    }

    private static List<Face> load() {
        List list;
        block14: {
            InputStream inputStream = GongdiHumanoidRenderer.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing humanoid geometry");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                ArrayList<Face> arrayList = new ArrayList<Face>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("faces")) {
                    int n;
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    double[][] dArray = new double[4][3];
                    double[][] dArray2 = new double[4][2];
                    for (int i = 0; i < 4; ++i) {
                        for (n = 0; n < 3; ++n) {
                            dArray[i][n] = jsonObject2.getAsJsonArray("points").get(i).getAsJsonArray().get(n).getAsDouble();
                        }
                        for (n = 0; n < 2; ++n) {
                            dArray2[i][n] = jsonObject2.getAsJsonArray("uv").get(i).getAsJsonArray().get(n).getAsDouble();
                        }
                    }
                    String[] stringArray = new String[4];
                    for (n = 0; n < 4; ++n) {
                        stringArray[n] = jsonObject2.has("bindings") ? jsonObject2.getAsJsonArray("bindings").get(n).getAsString() : jsonObject2.get("bone").getAsString();
                    }
                    arrayList.add(new Face(jsonObject2.get("bone").getAsString(), jsonObject2.get("material").getAsString(), stringArray, dArray, dArray2));
                }
                list = List.copyOf(arrayList);
                if (inputStream == null) break block14;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    throw new IllegalStateException("Invalid humanoid geometry", exception);
                }
            }
            inputStream.close();
        }
        return list;
    }

    public static double[] modelPoint(double[] dArray) {
        return new double[]{dArray[0], 1.5 - dArray[1], -dArray[2]};
    }

    public static int draw(GongdiBasicAttackSampler.Frame frame, String string, PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        Object object;
        int n3 = 0;
        PoseStack.Pose pose = poseStack.m_85850_();
        LinkedHashMap<String, Quaterniond> linkedHashMap = new LinkedHashMap<String, Quaterniond>();
        for (Map.Entry<String, GongdiBasicAttackSampler.Bone> object22 : frame.bones().entrySet()) {
            object = object22.getValue().quaternion();
            linkedHashMap.put(object22.getKey(), new Quaterniond(object[0], object[1], object[2], object[3]));
        }
        Vector3d vector3d = new Vector3d();
        Iterator iterator = Library.BY_MATERIAL.getOrDefault(string, List.of()).iterator();
        while (iterator.hasNext()) {
            double[] dArray;
            Object object2;
            object = (Face)iterator.next();
            double[][] dArrayArray = new double[4][];
            for (int i = 0; i < 4; ++i) {
                object2 = ((Face)object).bindings()[i];
                vector3d.set(((Face)object).points()[i]);
                ((Quaterniond)linkedHashMap.get(object2)).transform(vector3d);
                dArray = frame.bones().get(object2).position();
                dArrayArray[i] = new double[]{vector3d.x + dArray[0], 1.5 - vector3d.y - dArray[1], -vector3d.z - dArray[2]};
            }
            double[] i = new double[]{dArrayArray[1][0] - dArrayArray[0][0], dArrayArray[1][1] - dArrayArray[0][1], dArrayArray[1][2] - dArrayArray[0][2]};
            object2 = new double[]{dArrayArray[2][0] - dArrayArray[0][0], dArrayArray[2][1] - dArrayArray[0][1], dArrayArray[2][2] - dArrayArray[0][2]};
            dArray = new double[]{i[1] * object2[2] - i[2] * object2[1], i[2] * object2[0] - i[0] * object2[2], i[0] * object2[1] - i[1] * object2[0]};
            double d = Math.sqrt(dArray[0] * dArray[0] + dArray[1] * dArray[1] + dArray[2] * dArray[2]);
            if (d < 1.0E-10) continue;
            for (int j = 0; j < 4; ++j) {
                vertexConsumer.m_252986_(pose.m_252922_(), (float)dArrayArray[j][0], (float)dArrayArray[j][1], (float)dArrayArray[j][2]).m_85950_(f, f2, f3, f4).m_7421_((float)((Face)object).uv()[j][0], (float)((Face)object).uv()[j][1]).m_86008_(n2).m_85969_(n).m_252939_(pose.m_252943_(), (float)(dArray[0] / d), (float)(dArray[1] / d), (float)(dArray[2] / d)).m_5752_();
            }
            ++n3;
        }
        return n3;
    }

    private record Actor(GongdiBasicAttackSampler.Frame frame) {
    }

    private record Face(String bone, String material, String[] bindings, double[][] points, double[][] uv) {
    }

    private static final class Library {
        private static final List<Face> FACES = GongdiHumanoidRenderer.load();
        private static final Map<String, List<Face>> BY_MATERIAL = Map.of("skin", FACES.stream().filter(face -> face.material().equals("skin")).toList(), "metal", FACES.stream().filter(face -> face.material().equals("metal")).toList());

        private Library() {
        }
    }
}

