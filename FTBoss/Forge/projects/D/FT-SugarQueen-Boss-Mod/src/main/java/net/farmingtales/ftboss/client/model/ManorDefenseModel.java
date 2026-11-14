package net.farmingtales.ftboss.client.model;

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
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 防御独立体素模型：2026-10-13，从可编辑最终 geo/PNG 读取，折翼动画与离线预览同源。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ManorDefenseModel {
    private static final Map<Integer, ManorDefenseModel> CACHE = new HashMap<>();
    private static final int[][] CORNERS = {{2, 3, 7, 6}, {0, 1, 5, 4}, {1, 5, 7, 3}, {4, 0, 2, 6}, {0, 1, 3, 2}, {5, 4, 6, 7}};
    private static final float[][] NORMALS = {{0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, -1}, {0, 0, 1}};
    private final List<Bone> roots;
    private final float textureWidth;
    private final float textureHeight;
    public final ResourceLocation texture;
    public final ResourceLocation glow;
    private final ResourceLocation physicalGlow;
    private final ResourceLocation magicGlow;

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

    private ManorDefenseModel(int variant) throws Exception {
        String id = ManorDefenseRules.profile(variant).model();
        texture = new ResourceLocation("ftboss", "textures/entity/manor_defense/" + id + ".png");
        glow = new ResourceLocation("ftboss", "textures/entity/manor_defense/" + id + "_glow.png");
        physicalGlow = new ResourceLocation("ftboss", "textures/entity/manor_defense/" + id + "_physical_glow.png");
        magicGlow = new ResourceLocation("ftboss", "textures/entity/manor_defense/" + id + "_magic_glow.png");
        try (var stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(
                new ResourceLocation("ftboss", "geo/manor_defense/" + id + ".geo.json")).open();
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

    public ResourceLocation emission(int adaptiveKind) {
        return adaptiveKind == 1 ? physicalGlow : adaptiveKind == 2 ? magicGlow : glow;
    }

    public static ManorDefenseModel get(int variant) {
        if (!CACHE.containsKey(variant)) {
            try { CACHE.put(variant, new ManorDefenseModel(variant)); }
            catch (Exception exception) {
                FTBossMod.LOGGER.error("[FTBoss][防御模型] variant={} 无法加载", variant, exception);
                CACHE.put(variant, null);
            }
        }
        return CACHE.get(variant);
    }

    @SubscribeEvent
    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> CACHE.clear());
    }

    private static float[] vector(JsonObject json, String name, int count) {
        float[] result = new float[count];
        if (json.has(name)) {
            JsonArray array = json.getAsJsonArray(name);
            for (int index = 0; index < count; index++) result[index] = array.get(index).getAsFloat();
        }
        return result;
    }

    public void render(PoseStack pose, VertexConsumer vertices, float openness, float alpha, boolean reduced, boolean luminous, int light) {
        pose.pushPose();
        pose.translate(0, 2.0F * (1.0F - openness) / 16.0F, 0);
        for (Bone bone : roots) renderBone(bone, pose, vertices, openness, alpha, reduced, luminous, light);
        pose.popPose();
    }

    private void renderBone(Bone bone, PoseStack pose, VertexConsumer vertices, float openness, float alpha,
                            boolean reduced, boolean luminous, int light) {
        pose.pushPose();
        float hinge = bone.name.equals("panel_l") ? -50.0F * (1.0F - openness)
                : bone.name.equals("panel_r") ? 50.0F * (1.0F - openness) : 0.0F;
        pose.translate(bone.pivot[0] / 16.0F, bone.pivot[1] / 16.0F, bone.pivot[2] / 16.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(bone.rotation[2]));
        pose.mulPose(Axis.YP.rotationDegrees(bone.rotation[1] + hinge));
        pose.mulPose(Axis.XP.rotationDegrees(bone.rotation[0]));
        pose.translate(-bone.pivot[0] / 16.0F, -bone.pivot[1] / 16.0F, -bone.pivot[2] / 16.0F);
        for (Cube cube : bone.cubes) {
            if (reduced && cube.detail) continue;
            renderCube(cube, pose, vertices, alpha, luminous ? LightTexture.FULL_BRIGHT : light, luminous ? 0.22F : 0.0F);
        }
        for (Bone child : bone.children) renderBone(child, pose, vertices, openness, alpha, reduced, luminous, light);
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
