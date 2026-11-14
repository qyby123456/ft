package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.farmingtales.ftboss.client.manorrun.GongdiRunFrame;
import net.farmingtales.ftboss.client.model.EasterBossModel;
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
    private static final Map<EasterBossModel, GongdiHumanoidRenderer.Actor> ACTORS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, GongdiHumanoidRenderer.Actor> ENTITY_ACTORS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, GongdiHumanoidTimeline.State> STATES = new WeakHashMap<>();

    private GongdiHumanoidRenderer() {
    }

    public static boolean eligible(EasterBossEntity var0) {
        if (var0.getVariant() == 3 && var0.isAlive() && !var0.isPassenger() && var0.getOffhandItem().isEmpty()) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getMainHandItem().getItem());
            return var1 != null && var1.toString().equals("ftboss:black_cleaver");
        } else {
            return false;
        }
    }

    public static void setup(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        ACTORS.remove(var0);
        ENTITY_ACTORS.remove(var1);
        if (eligible(var1)) {
            float var7 = Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
            CompoundTag var8 = var1.getSkillCue();
            double var9 = (double)((float)(var1.level().getGameTime() - var8.getLong("start")) + var7);
            boolean var11 = var8.contains("gongdiSkill")
                && var9 >= 0.0
                && var9 < (double)Math.max(1, var8.getInt("duration"))
                && (var1.isCastingLike() || var8.getBoolean("released"));
            GongdiSpellCues.Pose var12 = GongdiSpellCues.current(var1, var7);
            if (var12 != null && var12.released() && !var1.isCastingLike() && var1.getCombatAnimationWeight(var7) > 0.001F) {
                var12 = null;
            }

            boolean var13 = var11 || var12 != null;
            if (!var1.isCastingLike() || var13) {
                for (boolean var17 : var1.getExtraBones()) {
                    if (var17) {
                        return;
                    }
                }

                double var20 = var11
                    ? var9 / (double)Math.max(1, var8.getInt("duration"))
                    : (var12 != null ? var12.progress() : (double)var1.getActionProgress(var7));
                long var21 = var1.level().getGameTime() - (long)Math.round(var1.getCombatAnimationAge(0.0F));
                GongdiHumanoidTimeline.Cue var18 = new GongdiHumanoidTimeline.Cue(
                    var13,
                    var11 ? var8.getBoolean("released") : var12 != null && var12.released(),
                    var11 ? var8.getString("gongdiSkill") : (var12 != null ? var12.skill() : ""),
                    var20,
                    var1.getCombatAnimationAge(var7),
                    var1.getCombatAnimationWeight(var7),
                    var1.hurtTime > 0,
                    var3,
                    var2,
                    var5,
                    var6,
                    var21
                );
                GongdiHumanoidRenderer.Actor var19 = new GongdiHumanoidRenderer.Actor(
                    GongdiRunFrame.apply(
                        GongdiHumanoidTimeline.select(STATES.computeIfAbsent(var1, var0x -> new GongdiHumanoidTimeline.State()), var18, var4), var1, var4
                    )
                );
                ACTORS.put(var0, var19);
                ENTITY_ACTORS.put(var1, var19);
            }
        }
    }

    public static boolean renderBody(
        EasterBossModel var0, PoseStack var1, VertexConsumer var2, int var3, int var4, float var5, float var6, float var7, float var8
    ) {
        GongdiHumanoidRenderer.Actor var9 = ACTORS.get(var0);
        if (var9 == null) {
            return false;
        } else {
            draw(var9.frame(), "skin", var1, var2, var3, var4, var5, var6, var7, var8);
            return true;
        }
    }

    public static boolean renderWeapon(EasterBossEntity var0, PoseStack var1, MultiBufferSource var2, int var3) {
        GongdiHumanoidRenderer.Actor var4 = ENTITY_ACTORS.get(var0);
        if (var4 != null && eligible(var0)) {
            ResourceLocation var5 = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/item/black_cleaver_geo.png");
            draw(var4.frame(), "metal", var1, var2.getBuffer(RenderType.entityCutoutNoCull(var5)), var3, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    private static List<GongdiHumanoidRenderer.Face> load() {
        try {
            List var13;
            try (InputStream var0 = GongdiHumanoidRenderer.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing humanoid geometry");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                ArrayList var2 = new ArrayList();

                for (JsonElement var4 : var1.getAsJsonArray("faces")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    double[][] var6 = new double[4][3];
                    double[][] var7 = new double[4][2];

                    for (int var8 = 0; var8 < 4; var8++) {
                        for (int var9 = 0; var9 < 3; var9++) {
                            var6[var8][var9] = var5.getAsJsonArray("points").get(var8).getAsJsonArray().get(var9).getAsDouble();
                        }

                        for (int var15 = 0; var15 < 2; var15++) {
                            var7[var8][var15] = var5.getAsJsonArray("uv").get(var8).getAsJsonArray().get(var15).getAsDouble();
                        }
                    }

                    String[] var14 = new String[4];

                    for (int var16 = 0; var16 < 4; var16++) {
                        var14[var16] = var5.has("bindings") ? var5.getAsJsonArray("bindings").get(var16).getAsString() : var5.get("bone").getAsString();
                    }

                    var2.add(new GongdiHumanoidRenderer.Face(var5.get("bone").getAsString(), var5.get("material").getAsString(), var14, var6, var7));
                }

                var13 = List.copyOf(var2);
            }

            return var13;
        } catch (Exception var12) {
            throw new IllegalStateException("Invalid humanoid geometry", var12);
        }
    }

    public static double[] modelPoint(double[] var0) {
        return new double[]{var0[0], 1.5 - var0[1], -var0[2]};
    }

    public static int draw(
        GongdiBasicAttackSampler.Frame var0,
        String var1,
        PoseStack var2,
        VertexConsumer var3,
        int var4,
        int var5,
        float var6,
        float var7,
        float var8,
        float var9
    ) {
        int var10 = 0;
        Pose var11 = var2.last();
        LinkedHashMap var12 = new LinkedHashMap();

        for (Entry var14 : var0.bones().entrySet()) {
            double[] var15 = ((GongdiBasicAttackSampler.Bone)var14.getValue()).quaternion();
            var12.put((String)var14.getKey(), new Quaterniond(var15[0], var15[1], var15[2], var15[3]));
        }

        Vector3d var23 = new Vector3d();

        for (GongdiHumanoidRenderer.Face var25 : GongdiHumanoidRenderer.Library.BY_MATERIAL.getOrDefault(var1, List.of())) {
            double[][] var16 = new double[4][];

            for (int var17 = 0; var17 < 4; var17++) {
                String var18 = var25.bindings()[var17];
                var23.set(var25.points()[var17]);
                ((Quaterniond)var12.get(var18)).transform(var23);
                double[] var19 = var0.bones().get(var18).position();
                var16[var17] = new double[]{var23.x + var19[0], 1.5 - var23.y - var19[1], -var23.z - var19[2]};
            }

            double[] var26 = new double[]{var16[1][0] - var16[0][0], var16[1][1] - var16[0][1], var16[1][2] - var16[0][2]};
            double[] var27 = new double[]{var16[2][0] - var16[0][0], var16[2][1] - var16[0][1], var16[2][2] - var16[0][2]};
            double[] var28 = new double[]{
                var26[1] * var27[2] - var26[2] * var27[1], var26[2] * var27[0] - var26[0] * var27[2], var26[0] * var27[1] - var26[1] * var27[0]
            };
            double var20 = Math.sqrt(var28[0] * var28[0] + var28[1] * var28[1] + var28[2] * var28[2]);
            if (!(var20 < 1.0E-10)) {
                for (int var22 = 0; var22 < 4; var22++) {
                    net.farmingtales.ftboss.client.NativeLegacyVertex.normal(var3.addVertex(var11.pose(), (float)var16[var22][0], (float)var16[var22][1], (float)var16[var22][2])
                        .setColor(var6, var7, var8, var9)
                        .setUv((float)var25.uv()[var22][0], (float)var25.uv()[var22][1])
                        .setOverlay(var5)
                        .setLight(var4)
                        , var11, (float)(var28[0] / var20), (float)(var28[1] / var20), (float)(var28[2] / var20));
                }

                var10++;
            }
        }

        return var10;
    }

    private static record Actor(GongdiBasicAttackSampler.Frame frame) {
    }

    private static record Face(String bone, String material, String[] bindings, double[][] points, double[][] uv) {
    }

    private static final class Library {
        private static final List<GongdiHumanoidRenderer.Face> FACES = GongdiHumanoidRenderer.load();
        private static final Map<String, List<GongdiHumanoidRenderer.Face>> BY_MATERIAL = Map.of(
            "skin",
            FACES.stream().filter(var0 -> var0.material().equals("skin")).toList(),
            "metal",
            FACES.stream().filter(var0 -> var0.material().equals("metal")).toList()
        );
    }
}
