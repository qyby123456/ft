package net.farmingtales.ftboss.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.NativeLegacyVertexNormals;
import com.mojang.math.Axis;
import java.io.InputStream;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD,
    value = {Dist.CLIENT}
)
public final class ManorDefenseModel {
    private static final Map<Integer, ManorDefenseModel> CACHE = new HashMap<>();
    private static final int[][] CORNERS = new int[][]{{2, 3, 7, 6}, {0, 1, 5, 4}, {1, 5, 7, 3}, {4, 0, 2, 6}, {0, 1, 3, 2}, {5, 4, 6, 7}};
    private static final float[][] NORMALS = new float[][]{
        {0.0F, 1.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -1.0F}, {0.0F, 0.0F, 1.0F}
    };
    private final List<ManorDefenseModel.Bone> roots;
    private final float textureWidth;
    private final float textureHeight;
    public final ResourceLocation texture;
    public final ResourceLocation glow;
    private final ResourceLocation physicalGlow;
    private final ResourceLocation magicGlow;

    private ManorDefenseModel(int variant) throws Exception {
        String id = ManorDefenseRules.profile(variant).model();
        this.texture = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/entity/manor_defense/" + id + ".png");
        this.glow = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/entity/manor_defense/" + id + "_glow.png");
        this.physicalGlow = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/entity/manor_defense/" + id + "_physical_glow.png");
        this.magicGlow = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/entity/manor_defense/" + id + "_magic_glow.png");

        try (
            InputStream stream = Minecraft.getInstance()
                .getResourceManager()
                .getResourceOrThrow(ResourceLocation.fromNamespaceAndPath("ftboss", "geo/manor_defense/" + id + ".geo.json"))
                .open();
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        ) {
            JsonObject geometry = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            JsonObject description = geometry.getAsJsonObject("description");
            this.textureWidth = description.get("texture_width").getAsFloat();
            this.textureHeight = description.get("texture_height").getAsFloat();
            Map<String, ManorDefenseModel.Bone> bones = new LinkedHashMap<>();

            for (JsonElement element : geometry.getAsJsonArray("bones")) {
                ManorDefenseModel.Bone bone = new ManorDefenseModel.Bone(element.getAsJsonObject());
                if (bones.put(bone.name, bone) != null) {
                    throw new IllegalArgumentException("Duplicate bone");
                }
            }

            this.roots = new ArrayList<>();

            for (ManorDefenseModel.Bone bone : bones.values()) {
                if (bone.parent.isEmpty()) {
                    this.roots.add(bone);
                } else {
                    ManorDefenseModel.Bone parent = bones.get(bone.parent);
                    if (parent == null) {
                        throw new IllegalArgumentException("Missing parent");
                    }

                    parent.children.add(bone);
                }
            }
        }
    }

    public ResourceLocation emission(int adaptiveKind) {
        return adaptiveKind == 1 ? this.physicalGlow : (adaptiveKind == 2 ? this.magicGlow : this.glow);
    }

    public static ManorDefenseModel get(int variant) {
        if (!CACHE.containsKey(variant)) {
            try {
                CACHE.put(variant, new ManorDefenseModel(variant));
            } catch (Exception var2) {
                FTBossMod.LOGGER.error("[FTBoss][防御模型] variant={} 无法加载", variant, var2);
                CACHE.put(variant, null);
            }
        }

        return CACHE.get(variant);
    }

    @SubscribeEvent
    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener)manager -> CACHE.clear());
    }

    private static float[] vector(JsonObject json, String name, int count) {
        float[] result = new float[count];
        if (json.has(name)) {
            JsonArray array = json.getAsJsonArray(name);

            for (int index = 0; index < count; index++) {
                result[index] = array.get(index).getAsFloat();
            }
        }

        return result;
    }

    public void render(PoseStack pose, VertexConsumer vertices, float openness, float alpha, boolean reduced, boolean luminous, int light) {
        pose.pushPose();
        pose.translate(0.0F, 2.0F * (1.0F - openness) / 16.0F, 0.0F);

        for (ManorDefenseModel.Bone bone : this.roots) {
            this.renderBone(bone, pose, vertices, openness, alpha, reduced, luminous, light);
        }

        pose.popPose();
    }

    private void renderBone(
        ManorDefenseModel.Bone bone, PoseStack pose, VertexConsumer vertices, float openness, float alpha, boolean reduced, boolean luminous, int light
    ) {
        pose.pushPose();
        float hinge = bone.name.equals("panel_l") ? -50.0F * (1.0F - openness) : (bone.name.equals("panel_r") ? 50.0F * (1.0F - openness) : 0.0F);
        pose.translate(bone.pivot[0] / 16.0F, bone.pivot[1] / 16.0F, bone.pivot[2] / 16.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(bone.rotation[2]));
        pose.mulPose(Axis.YP.rotationDegrees(bone.rotation[1] + hinge));
        pose.mulPose(Axis.XP.rotationDegrees(bone.rotation[0]));
        pose.translate(-bone.pivot[0] / 16.0F, -bone.pivot[1] / 16.0F, -bone.pivot[2] / 16.0F);

        for (ManorDefenseModel.Cube cube : bone.cubes) {
            if (!reduced || !cube.detail) {
                this.renderCube(cube, pose, vertices, alpha, luminous ? 15728880 : light, luminous ? 0.22F : 0.0F);
            }
        }

        for (ManorDefenseModel.Bone child : bone.children) {
            this.renderBone(child, pose, vertices, openness, alpha, reduced, luminous, light);
        }

        pose.popPose();
    }

    private void renderCube(ManorDefenseModel.Cube cube, PoseStack pose, VertexConsumer vertices, float alpha, int light, float inflate) {
        float width = cube.size[0];
        float height = cube.size[1];
        float depth = cube.size[2];
        float atlasU = cube.uv[0];
        float atlasV = cube.uv[1];
        float[][] rectangles = new float[][]{
            {atlasU + depth, atlasV, width, depth},
            {atlasU + depth + width, atlasV, width, depth},
            {atlasU, atlasV + depth, depth, height},
            {atlasU + depth + width, atlasV + depth, depth, height},
            {atlasU + depth, atlasV + depth, width, height},
            {atlasU + depth + width + depth, atlasV + depth, width, height}
        };

        for (int face = 0; face < CORNERS.length; face++) {
            float[] rectangle = rectangles[face];

            for (int corner = 0; corner < 4; corner++) {
                int index = CORNERS[face][corner];
                float vertexU = rectangle[0] + (corner != 1 && corner != 2 ? 0.0F : rectangle[2]);
                float vertexV = rectangle[1] + (corner >= 2 ? rectangle[3] : 0.0F);
                NativeLegacyVertexNormals.normal(vertices.addVertex(
                        pose.last().pose(),
                        (cube.origin[0] - inflate + ((index & 1) != 0 ? width + 2.0F * inflate : 0.0F)) / 16.0F,
                        (cube.origin[1] - inflate + ((index & 2) != 0 ? height + 2.0F * inflate : 0.0F)) / 16.0F,
                        (cube.origin[2] - inflate + ((index & 4) != 0 ? depth + 2.0F * inflate : 0.0F)) / 16.0F
                    )
                    .setColor(1.0F, 1.0F, 1.0F, alpha)
                    .setUv(vertexU / this.textureWidth, vertexV / this.textureHeight)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light), pose.last().normal(), NORMALS[face][0], NORMALS[face][1], NORMALS[face][2]);
            }
        }
    }

    private static final class Bone {
        final String name;
        final String parent;
        final float[] pivot;
        final float[] rotation;
        final List<ManorDefenseModel.Cube> cubes = new ArrayList<>();
        final List<ManorDefenseModel.Bone> children = new ArrayList<>();

        Bone(JsonObject json) {
            this.name = json.get("name").getAsString();
            this.parent = json.has("parent") ? json.get("parent").getAsString() : "";
            this.pivot = ManorDefenseModel.vector(json, "pivot", 3);
            this.rotation = ManorDefenseModel.vector(json, "rotation", 3);
            if (json.has("cubes")) {
                for (JsonElement element : json.getAsJsonArray("cubes")) {
                    JsonObject cube = element.getAsJsonObject();
                    this.cubes
                        .add(
                            new ManorDefenseModel.Cube(
                                ManorDefenseModel.vector(cube, "origin", 3),
                                ManorDefenseModel.vector(cube, "size", 3),
                                ManorDefenseModel.vector(cube, "uv", 2),
                                cube.has("ft_detail") && cube.get("ft_detail").getAsBoolean()
                            )
                        );
                }
            }
        }
    }

    private static record Cube(float[] origin, float[] size, float[] uv, boolean detail) {
    }
}
