import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.util.JsonUtil;

public final class VerifyGeckoAssets {
    public static void main(String[] arguments) throws Exception {
        Path assets = Path.of(arguments[0], "assets", "ftdungeonmobs");
        Map<String, List<String>> expected = Map.of(
                "skeleton_warrior", List.of("idle", "walk", "attack", "animation2"),
                "skeleton_archer", List.of("idle", "walk", "attack", "animation2"),
                "skeleton_mage", List.of("idle", "walk", "attack", "animation2"),
                "skeleton_king", List.of("idle", "walk", "attack", "dash", "summon"));
        int animationCount = 0;
        int boneCount = 0;
        for (Map.Entry<String, List<String>> entry : expected.entrySet()) {
            String modelId = entry.getKey();
            Path geometryPath = assets.resolve("geo/" + modelId + ".geo.json");
            Model model;
            try (Reader reader = Files.newBufferedReader(geometryPath, StandardCharsets.UTF_8)) {
                model = JsonUtil.GEO_GSON.fromJson(reader, Model.class);
            }
            BakedGeoModel baked = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
            JsonObject geometry = JsonParser.parseString(Files.readString(geometryPath)).getAsJsonObject();
            for (var bone : geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")) {
                String name = bone.getAsJsonObject().get("name").getAsString();
                if (baked.getBone(name).isEmpty()) {
                    throw new IllegalStateException("GeckoLib did not bake bone " + modelId + ":" + name);
                }
                boneCount++;
            }
            BakedAnimations animations;
            try (Reader reader = Files.newBufferedReader(assets.resolve("animations/" + modelId + ".animation.json"), StandardCharsets.UTF_8)) {
                JsonObject animationFile = JsonParser.parseReader(reader).getAsJsonObject();
                animations = JsonUtil.GEO_GSON.fromJson(animationFile.getAsJsonObject("animations"), BakedAnimations.class);
            }
            if (animations.animations().size() != entry.getValue().size()) {
                throw new IllegalStateException("Animation count mismatch for " + modelId);
            }
            for (String animation : entry.getValue()) {
                String animationId = "animation." + modelId + "." + animation;
                if (animations.getAnimation(animationId) == null) {
                    throw new IllegalStateException("GeckoLib could not load " + animationId);
                }
                animationCount++;
            }
            System.out.println("GeckoLib 4.8.4 baked geometry and loaded all animations: " + modelId);
        }
        if (animationCount != 17 || boneCount != 218) {
            throw new IllegalStateException("Incomplete imported asset set");
        }
        System.out.println("VERIFIED: 4 models, 218 bones, 17 animations through the actual GeckoLib parser and model baker.");
    }
}
