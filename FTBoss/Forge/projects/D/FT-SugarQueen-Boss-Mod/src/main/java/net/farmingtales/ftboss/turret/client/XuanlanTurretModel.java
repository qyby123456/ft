package net.farmingtales.ftboss.turret.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 玄蓝月轮炮台体素模型：最终 geo、像素贴图与部署、转轮、后坐关键帧同源。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XuanlanTurretModel {
    private static final Map<String, XuanlanTurretModel> CACHE = new HashMap<>();
    private static final java.util.Set<String> FAILED = new java.util.HashSet<>();
    public final ResourceLocation texture;
    public final ResourceLocation emission;
    private static final int[][] CORNERS = {{2, 3, 7, 6}, {0, 1, 5, 4}, {1, 5, 7, 3}, {4, 0, 2, 6}, {0, 1, 3, 2}, {5, 4, 6, 7}};
    private static final float[][] NORMALS = {{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, -1}, {0, 0, 1}};
    private final List<Bone> roots;
    private final float textureWidth;
    private final float textureHeight;
    public static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/xuanlan_moon_turret.png");
    public static final ResourceLocation GLOW = new ResourceLocation("ftboss", "textures/entity/xuanlan_moon_turret_glow.png");

    private record Cube(float[] origin, float[] size, float[] uv, boolean detail) {}
    private static final class Bone {
        final String name;
        final String parent;
        final float[] pivot;
        final float[] rotation;
        final List<Cube> cubes = new ArrayList<>();
        final List<Bone> children = new ArrayList<>();

        Bone(JsonObject json) {
            name = json.get("name").getAsString();
            parent = json.has("parent") ? json.get("parent").getAsString() : "";
            pivot = vector(json, "pivot", 3);
            rotation = vector(json, "rotation", 3);
            if (json.has("cubes")) for (var element : json.getAsJsonArray("cubes")) {
                JsonObject cube = element.getAsJsonObject();
                cubes.add(new Cube(vector(cube, "origin", 3), vector(cube, "size", 3), vector(cube, "uv", 2),
                        cube.has("ft_detail") && cube.get("ft_detail").getAsBoolean()));
            }
        }
    }

    private XuanlanTurretModel(String id) throws Exception {
        texture = new ResourceLocation("ftboss", "textures/entity/" + id + ".png");
        emission = new ResourceLocation("ftboss", "textures/entity/" + id + "_glow.png");
        try (var stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(
                new ResourceLocation("ftboss", "geo/" + id + ".geo.json")).open();
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject geometry = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            JsonObject description = geometry.getAsJsonObject("description");
            textureWidth = description.get("texture_width").getAsFloat();
            textureHeight = description.get("texture_height").getAsFloat();
            Map<String, Bone> bones = new LinkedHashMap<>();
            for (var element : geometry.getAsJsonArray("bones")) {
                Bone bone = new Bone(element.getAsJsonObject());
                if (bones.put(bone.name, bone) != null) throw new IllegalArgumentException("Duplicate bone");
            }
            roots = new ArrayList<>();
            for (Bone bone : bones.values()) {
                if (bone.parent.isEmpty()) roots.add(bone);
                else {
                    Bone parent = bones.get(bone.parent);
                    if (parent == null) throw new IllegalArgumentException("Missing parent");
                    parent.children.add(bone);
                }
            }
        }
    }

    public static XuanlanTurretModel get() {
        return get("xuanlan_moon_turret");
    }

    public static XuanlanTurretModel get(String id) {
        if (!CACHE.containsKey(id) && !FAILED.contains(id)) {
            try { CACHE.put(id, new XuanlanTurretModel(id)); }
            catch (Exception exception) {
                FAILED.add(id);
                FTBossMod.LOGGER.error("[FTBoss] 月轮炮台模型加载失败", exception);
            }
        }
        return CACHE.get(id);
    }

    @SubscribeEvent
    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> { CACHE.clear(); FAILED.clear(); });
    }

    private static float[] vector(JsonObject json, String name, int count) {
        float[] result = new float[count];
        if (json.has(name)) {
            JsonArray array = json.getAsJsonArray(name);
            for (int index = 0; index < count; index++) result[index] = array.get(index).getAsFloat();
        }
        return result;
    }

    public void render(PoseStack pose, VertexConsumer vertices, float age, float recoil, boolean luminous, int light) {
        pose.pushPose();
        for (Bone bone : roots) renderBone(bone, pose, vertices, age, recoil, luminous, light);
        pose.popPose();
    }

    private void renderBone(Bone bone, PoseStack pose, VertexConsumer vertices, float age, float recoil,
                            boolean luminous, int light) {
        pose.pushPose();
        if (bone.name.equals("barrel")) pose.translate(0, 0, recoil * 2.0F / 16.0F);
        float rotation = bone.name.equals("rotor") ? age * 9.0F : 0.0F;
        if (bone.name.equals("wing_left")) rotation = (float) Math.sin(age * 0.6F) * 25.0F;
        if (bone.name.equals("wing_right")) rotation = -(float) Math.sin(age * 0.6F) * 25.0F;
        pose.translate(bone.pivot[0] / 16.0F, bone.pivot[1] / 16.0F, bone.pivot[2] / 16.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(bone.rotation[2] + rotation));
        pose.mulPose(Axis.YP.rotationDegrees(bone.rotation[1]));
        pose.mulPose(Axis.XP.rotationDegrees(bone.rotation[0]));
        pose.translate(-bone.pivot[0] / 16.0F, -bone.pivot[1] / 16.0F, -bone.pivot[2] / 16.0F);
        for (Cube cube : bone.cubes) {
            renderCube(cube, pose, vertices, 1.0F, luminous ? LightTexture.FULL_BRIGHT : light, luminous ? 0.015F : 0.0F);
        }
        for (Bone child : bone.children) renderBone(child, pose, vertices, age, recoil, luminous, light);
        pose.popPose();
    }

    private void renderCube(Cube cube, PoseStack pose, VertexConsumer vertices, float alpha, int light, float inflate) {
        float width = cube.size[0], height = cube.size[1], depth = cube.size[2];
        float atlasU = cube.uv[0], atlasV = cube.uv[1];
        float[][] rectangles = {
                {atlasU + depth, atlasV, width, depth}, {atlasU + depth + width, atlasV, width, depth},
                {atlasU, atlasV + depth, depth, height}, {atlasU + depth + width, atlasV + depth, depth, height},
                {atlasU + depth, atlasV + depth, width, height}, {atlasU + depth + width + depth, atlasV + depth, width, height}
        };
        for (int face = 0; face < CORNERS.length; face++) {
            float[] rectangle = rectangles[face];
            for (int corner = 0; corner < 4; corner++) {
                int index = CORNERS[face][corner];
                float vertexU = rectangle[0] + (corner == 1 || corner == 2 ? rectangle[2] : 0);
                float vertexV = rectangle[1] + (corner >= 2 ? rectangle[3] : 0);
                vertices.vertex(pose.last().pose(), (cube.origin[0] - inflate + ((index & 1) != 0 ? width + 2 * inflate : 0)) / 16.0F,
                                (cube.origin[1] - inflate + ((index & 2) != 0 ? height + 2 * inflate : 0)) / 16.0F,
                                (cube.origin[2] - inflate + ((index & 4) != 0 ? depth + 2 * inflate : 0)) / 16.0F)
                        .color(1.0F, 1.0F, 1.0F, alpha).uv(vertexU / textureWidth, vertexV / textureHeight)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                        .normal(pose.last().normal(), NORMALS[face][0], NORMALS[face][1], NORMALS[face][2]).endVertex();
            }
        }
    }
}
