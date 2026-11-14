/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.turret.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XuanlanTurretModel {
    private static final Map<String, XuanlanTurretModel> CACHE = new HashMap<String, XuanlanTurretModel>();
    private static final Set<String> FAILED = new HashSet<String>();
    public final ResourceLocation texture;
    public final ResourceLocation emission;
    private static final int[][] CORNERS = new int[][]{{2, 3, 7, 6}, {0, 1, 5, 4}, {1, 5, 7, 3}, {4, 0, 2, 6}, {0, 1, 3, 2}, {5, 4, 6, 7}};
    private static final float[][] NORMALS = new float[][]{{0.0f, 1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, 1.0f}};
    private final List<Bone> roots;
    private final float textureWidth;
    private final float textureHeight;
    public static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/xuanlan_moon_turret.png");
    public static final ResourceLocation GLOW = new ResourceLocation("ftboss", "textures/entity/xuanlan_moon_turret_glow.png");

    private XuanlanTurretModel(String id) throws Exception {
        this.texture = new ResourceLocation("ftboss", "textures/entity/" + id + ".png");
        this.emission = new ResourceLocation("ftboss", "textures/entity/" + id + "_glow.png");
        try (InputStream stream = Minecraft.m_91087_().m_91098_().m_215593_(new ResourceLocation("ftboss", "geo/" + id + ".geo.json")).m_215507_();
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);){
            JsonObject geometry = JsonParser.parseReader((Reader)reader).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            JsonObject description = geometry.getAsJsonObject("description");
            this.textureWidth = description.get("texture_width").getAsFloat();
            this.textureHeight = description.get("texture_height").getAsFloat();
            LinkedHashMap<String, Bone> bones = new LinkedHashMap<String, Bone>();
            for (JsonElement element : geometry.getAsJsonArray("bones")) {
                Bone bone = new Bone(element.getAsJsonObject());
                if (bones.put(bone.name, bone) == null) continue;
                throw new IllegalArgumentException("Duplicate bone");
            }
            this.roots = new ArrayList<Bone>();
            for (Bone bone : bones.values()) {
                if (bone.parent.isEmpty()) {
                    this.roots.add(bone);
                    continue;
                }
                Bone parent = (Bone)bones.get(bone.parent);
                if (parent == null) {
                    throw new IllegalArgumentException("Missing parent");
                }
                parent.children.add(bone);
            }
        }
    }

    public static XuanlanTurretModel get() {
        return XuanlanTurretModel.get("xuanlan_moon_turret");
    }

    public static XuanlanTurretModel get(String id) {
        if (!CACHE.containsKey(id) && !FAILED.contains(id)) {
            try {
                CACHE.put(id, new XuanlanTurretModel(id));
            }
            catch (Exception exception) {
                FAILED.add(id);
                FTBossMod.LOGGER.error("[FTBoss] \u6708\u8f6e\u70ae\u53f0\u6a21\u578b\u52a0\u8f7d\u5931\u8d25", (Throwable)exception);
            }
        }
        return CACHE.get(id);
    }

    @SubscribeEvent
    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            CACHE.clear();
            FAILED.clear();
        }));
    }

    private static float[] vector(JsonObject json, String name, int count) {
        float[] result = new float[count];
        if (json.has(name)) {
            JsonArray array = json.getAsJsonArray(name);
            for (int index = 0; index < count; ++index) {
                result[index] = array.get(index).getAsFloat();
            }
        }
        return result;
    }

    public void render(PoseStack pose, VertexConsumer vertices, float age, float recoil, boolean luminous, int light) {
        pose.m_85836_();
        for (Bone bone : this.roots) {
            this.renderBone(bone, pose, vertices, age, recoil, luminous, light);
        }
        pose.m_85849_();
    }

    private void renderBone(Bone bone, PoseStack pose, VertexConsumer vertices, float age, float recoil, boolean luminous, int light) {
        float rotation;
        pose.m_85836_();
        if (bone.name.equals("barrel")) {
            pose.m_252880_(0.0f, 0.0f, recoil * 2.0f / 16.0f);
        }
        float f = rotation = bone.name.equals("rotor") ? age * 9.0f : 0.0f;
        if (bone.name.equals("wing_left")) {
            rotation = (float)Math.sin(age * 0.6f) * 25.0f;
        }
        if (bone.name.equals("wing_right")) {
            rotation = -((float)Math.sin(age * 0.6f)) * 25.0f;
        }
        pose.m_252880_(bone.pivot[0] / 16.0f, bone.pivot[1] / 16.0f, bone.pivot[2] / 16.0f);
        pose.m_252781_(Axis.f_252403_.m_252977_(bone.rotation[2] + rotation));
        pose.m_252781_(Axis.f_252436_.m_252977_(bone.rotation[1]));
        pose.m_252781_(Axis.f_252529_.m_252977_(bone.rotation[0]));
        pose.m_252880_(-bone.pivot[0] / 16.0f, -bone.pivot[1] / 16.0f, -bone.pivot[2] / 16.0f);
        for (Cube cube : bone.cubes) {
            this.renderCube(cube, pose, vertices, 1.0f, luminous ? 0xF000F0 : light, luminous ? 0.015f : 0.0f);
        }
        for (Bone child : bone.children) {
            this.renderBone(child, pose, vertices, age, recoil, luminous, light);
        }
        pose.m_85849_();
    }

    private void renderCube(Cube cube, PoseStack pose, VertexConsumer vertices, float alpha, int light, float inflate) {
        float width = cube.size[0];
        float height = cube.size[1];
        float depth = cube.size[2];
        float atlasU = cube.uv[0];
        float atlasV = cube.uv[1];
        float[][] rectangles = new float[][]{{atlasU + depth, atlasV, width, depth}, {atlasU + depth + width, atlasV, width, depth}, {atlasU, atlasV + depth, depth, height}, {atlasU + depth + width, atlasV + depth, depth, height}, {atlasU + depth, atlasV + depth, width, height}, {atlasU + depth + width + depth, atlasV + depth, width, height}};
        for (int face = 0; face < CORNERS.length; ++face) {
            float[] rectangle = rectangles[face];
            for (int corner = 0; corner < 4; ++corner) {
                int index = CORNERS[face][corner];
                float vertexU = rectangle[0] + (corner == 1 || corner == 2 ? rectangle[2] : 0.0f);
                float vertexV = rectangle[1] + (corner >= 2 ? rectangle[3] : 0.0f);
                vertices.m_252986_(pose.m_85850_().m_252922_(), (cube.origin[0] - inflate + ((index & 1) != 0 ? width + 2.0f * inflate : 0.0f)) / 16.0f, (cube.origin[1] - inflate + ((index & 2) != 0 ? height + 2.0f * inflate : 0.0f)) / 16.0f, (cube.origin[2] - inflate + ((index & 4) != 0 ? depth + 2.0f * inflate : 0.0f)) / 16.0f).m_85950_(1.0f, 1.0f, 1.0f, alpha).m_7421_(vertexU / this.textureWidth, vertexV / this.textureHeight).m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(pose.m_85850_().m_252943_(), NORMALS[face][0], NORMALS[face][1], NORMALS[face][2]).m_5752_();
            }
        }
    }

    private static final class Bone {
        final String name;
        final String parent;
        final float[] pivot;
        final float[] rotation;
        final List<Cube> cubes = new ArrayList<Cube>();
        final List<Bone> children = new ArrayList<Bone>();

        Bone(JsonObject json) {
            this.name = json.get("name").getAsString();
            this.parent = json.has("parent") ? json.get("parent").getAsString() : "";
            this.pivot = XuanlanTurretModel.vector(json, "pivot", 3);
            this.rotation = XuanlanTurretModel.vector(json, "rotation", 3);
            if (json.has("cubes")) {
                for (JsonElement element : json.getAsJsonArray("cubes")) {
                    JsonObject cube = element.getAsJsonObject();
                    this.cubes.add(new Cube(XuanlanTurretModel.vector(cube, "origin", 3), XuanlanTurretModel.vector(cube, "size", 3), XuanlanTurretModel.vector(cube, "uv", 2), cube.has("ft_detail") && cube.get("ft_detail").getAsBoolean()));
                }
            }
        }
    }

    private record Cube(float[] origin, float[] size, float[] uv, boolean detail) {
    }
}

