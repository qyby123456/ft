import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.block.model.BlockModel;

public final class VerifyBloodthornAssets {
    private static final String ID = "ftdungeonmobs:bloodthorn_brazier";
    private static final String MODEL = "ftdungeonmobs:block/bloodthorn_brazier";

    private VerifyBloodthornAssets() {}

    public static void main(String[] arguments) throws Exception {
        Path resources = Path.of(arguments[0]);
        Path assets = resources.resolve("assets/ftdungeonmobs");
        Path modelPath = assets.resolve("models/block/bloodthorn_brazier.json");
        JsonObject model = read(modelPath);
        require(!model.has("texture_size") && !model.has("groups"), "Unexpected editor-only runtime fields");
        require(!model.get("ambientocclusion").getAsBoolean(), "Bloodflame must not use ambient occlusion");
        JsonObject textures = model.getAsJsonObject("textures");
        require(textures.get("0").getAsString().equals(MODEL), "Body texture binding");
        require(textures.get("1").getAsString().equals(MODEL + "_fire"), "Fire texture binding");
        require(textures.get("particle").getAsString().equals(MODEL), "Particle texture binding");
        JsonArray elements = model.getAsJsonArray("elements");
        require(elements.size() == 43, "Expected 43 cuboids");
        int faces = 0;
        int fireCuboids = 0;
        for (JsonElement elementValue : elements) {
            JsonObject element = elementValue.getAsJsonObject();
            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            verifyVector(from, 0, 16, "from");
            verifyVector(to, 0, 16, "to");
            for (int axis = 0; axis < 3; axis++) {
                require(to.get(axis).getAsInt() > from.get(axis).getAsInt(), "Flat or inverted cuboid");
            }
            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                require(rotation.get("angle").getAsDouble() == 0, "Geometry must remain unrotated");
                verifyVector(rotation.getAsJsonArray("origin"), 0, 16, "pivot");
            }
            boolean fire = false;
            for (Map.Entry<String, JsonElement> faceEntry : element.getAsJsonObject("faces").entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                String texture = face.get("texture").getAsString();
                require(texture.equals("#0") || texture.equals("#1"), "Unknown face material");
                JsonArray uv = face.getAsJsonArray("uv");
                require(uv.size() == 4, "Face UV needs four coordinates");
                double pixelsPerUnit = texture.equals("#0") ? 4 : 1;
                for (JsonElement coordinate : uv) {
                    double value = coordinate.getAsDouble();
                    require(value >= 0 && value <= 16, "UV exceeds texture");
                    require(value * pixelsPerUnit == Math.rint(value * pixelsPerUnit), "Subpixel UV");
                }
                fire |= texture.equals("#1");
                faces++;
            }
            if (fire) {
                require(element.has("shade") && !element.get("shade").getAsBoolean(), "Fire directional shade");
                fireCuboids++;
            }
        }
        require(faces == 217 && fireCuboids == 11, "Unexpected face or flame count");
        try (Reader reader = Files.newBufferedReader(modelPath, StandardCharsets.UTF_8)) {
            BlockModel parsed = BlockModel.fromStream(reader);
            require(parsed.getElements().size() == 43, "Minecraft BlockModel element count");
            require(parsed.getElements().stream().mapToInt(element -> element.faces.size()).sum() == 217,
                    "Minecraft BlockModel face count");
            require(parsed.getElements().stream().filter(element -> !element.shade).count() == 11,
                    "Minecraft BlockModel flame shading");
        }
        JsonObject variants = read(assets.resolve("blockstates/bloodthorn_brazier.json")).getAsJsonObject("variants");
        require(variants.size() == 6, "Unlit plus five one-block tier states required");
        require(variants.getAsJsonObject("tier=0").get("model").getAsString().equals(MODEL + "_unlit"),
                "Inactive state must select the unlit model");
        verifyUnlit(assets, model);
        for (int tier = 1; tier <= 5; tier++) {
            JsonObject variant = variants.getAsJsonObject("tier=" + tier);
            String expectedModel = tier == 5 ? MODEL : MODEL + "_t" + tier;
            require(variant != null && variant.get("model").getAsString().equals(expectedModel), "Tier model binding");
        }
        Path itemPath = assets.resolve("models/item/bloodthorn_brazier.json");
        require(read(itemPath).get("parent").getAsString().equals(MODEL + "_unlit"), "Unlit item parent binding");
        try (Reader reader = Files.newBufferedReader(itemPath, StandardCharsets.UTF_8)) {
            require(BlockModel.fromStream(reader) != null, "Minecraft item model parsing");
        }
        verifyImage(assets.resolve("textures/block/bloodthorn_brazier.png"), 64, 64);
        verifyImage(assets.resolve("textures/block/bloodthorn_brazier_fire.png"), 16, 256);
        JsonObject animation = read(assets.resolve("textures/block/bloodthorn_brazier_fire.png.mcmeta"))
                .getAsJsonObject("animation");
        require(animation.get("frametime").getAsInt() == 2, "Fire frametime");
        require(!animation.get("interpolate").getAsBoolean(), "Pixel animation must not interpolate");
        JsonArray frames = animation.getAsJsonArray("frames");
        require(frames.size() == 16, "Fire requires 16 frames");
        for (int frame = 0; frame < 16; frame++) {
            require(frames.get(frame).getAsInt() == frame, "Fire frame sequence");
        }
        verifyTierTextures(assets, animation);
        JsonObject recipe = read(resources.resolve("data/ftdungeonmobs/recipes/bloodthorn_brazier.json"));
        require(recipe.get("type").getAsString().equals("minecraft:crafting_shaped"), "Recipe type");
        require(recipe.getAsJsonObject("result").get("item").getAsString().equals(ID), "Recipe result");
        require(recipe.getAsJsonObject("result").get("count").getAsInt() == 1, "Recipe result count");
        JsonObject loot = read(resources.resolve("data/ftdungeonmobs/loot_tables/blocks/bloodthorn_brazier.json"));
        JsonObject pool = loot.getAsJsonArray("pools").get(0).getAsJsonObject();
        require(pool.get("rolls").getAsInt() == 1, "Single-block loot rolls");
        require(pool.getAsJsonArray("entries").size() == 1, "Single-block loot count");
        require(pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString().equals(ID),
                "Loot item binding");
        require(pool.getAsJsonArray("conditions").get(0).getAsJsonObject().get("condition").getAsString()
                .equals("minecraft:survives_explosion"), "Loot explosion condition");
        JsonArray mineable = read(resources.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"))
                .getAsJsonArray("values");
        require(contains(mineable, ID) && contains(mineable, "ftdungeonmobs:arena_torch"), "Both pickaxe tags retained");
        for (String language : new String[]{"zh_cn", "en_us"}) {
            JsonObject translations = read(assets.resolve("lang/" + language + ".json"));
            for (String key : new String[]{"block.ftdungeonmobs.bloodthorn_brazier",
                    "tooltip.ftdungeonmobs.bloodthorn_brazier.radius",
                    "tooltip.ftdungeonmobs.bloodthorn_brazier.flame", "message.ftdungeonmobs.bloodthorn_tier",
                    "message.ftdungeonmobs.bloodthorn_unlit"}) {
                require(translations.has(key) && !translations.get(key).getAsString().isBlank(), "Translation: " + key);
            }
        }
        System.out.println("BLOODTHORN_ASSETS_VERIFIED: Minecraft 1.20.1 BlockModel; 43 integer cuboids, "
                + faces + " faces, " + fireCuboids + " unshaded flame cuboids; five lit tiers + 32-cuboid unlit state; "
                + "64x64 shared body + five distinct 16x256 fire palettes, 16 frames / 32 ticks each; "
                + "shared geometry/UV, inherited tier materials, item, recipe, loot, tags and two languages.");
    }

    private static void verifyUnlit(Path assets, JsonObject lit) throws Exception {
        Path path = assets.resolve("models/block/bloodthorn_brazier_unlit.json");
        JsonObject unlit = read(path);
        JsonArray body = new JsonArray();
        for (JsonElement element : lit.getAsJsonArray("elements")) {
            boolean flame = element.getAsJsonObject().getAsJsonObject("faces").entrySet().stream()
                    .anyMatch(face -> face.getValue().getAsJsonObject().get("texture").getAsString().equals("#1"));
            if (!flame) {
                body.add(element);
            }
        }
        require(body.size() == 32 && unlit.getAsJsonArray("elements").equals(body),
                "Unlit geometry/UV must be the exact existing body, without flame cuboids");
        JsonObject textures = lit.getAsJsonObject("textures").deepCopy();
        textures.remove("1");
        textures.addProperty("0", MODEL + "_unlit");
        textures.addProperty("particle", MODEL + "_unlit");
        require(unlit.getAsJsonObject("textures").equals(textures), "No animated fire material in unlit state");
        require(unlit.getAsJsonObject("display").equals(lit.getAsJsonObject("display")), "Unchanged display transforms");
        Path texturePath = assets.resolve("textures/block/bloodthorn_brazier_unlit.png");
        verifyImage(texturePath, 64, 64);
        BufferedImage original = ImageIO.read(assets.resolve("textures/block/bloodthorn_brazier.png").toFile());
        BufferedImage cooled = ImageIO.read(texturePath.toFile());
        int changed = 0;
        for (int pixelY = 0; pixelY < 64; pixelY++) {
            for (int pixelX = 0; pixelX < 64; pixelX++) {
                int before = original.getRGB(pixelX, pixelY);
                int after = cooled.getRGB(pixelX, pixelY);
                boolean coal = pixelX >= 16 && pixelX < 20 && pixelY >= 32 && pixelY < 35
                        && !(pixelX == 19 && pixelY == 34);
                require(coal || before == after, "Unlit texture must preserve metal, bones and blood pixels");
                require((before >>> 24) == (after >>> 24), "Unchanged texture alpha");
                if (coal) {
                    require((after >> 16 & 255) < 64 && (after >> 8 & 255) < 64 && (after & 255) < 64,
                            "Unlit charcoal must have no hot ember pixels");
                }
                changed += before == after ? 0 : 1;
            }
        }
        require(changed == 11, "Exactly eleven shared charcoal pixels are cooled");
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            BlockModel parsed = BlockModel.fromStream(reader);
            require(parsed.getElements().size() == 32, "Minecraft unlit cuboid count");
            require(parsed.getElements().stream().mapToInt(element -> element.faces.size()).sum() == 161,
                    "Minecraft unlit face count");
            require(parsed.getElements().stream().allMatch(element -> element.shade), "No emissive flame cubes retained");
        }
    }

    private static void verifyTierTextures(Path assets, JsonObject animation) throws Exception {
        BufferedImage crimson = ImageIO.read(assets.resolve("textures/block/bloodthorn_brazier_fire.png").toFile());
        Set<Integer> colors = new HashSet<>();
        for (int tier = 1; tier <= 5; tier++) {
            Path texture = assets.resolve("textures/block/bloodthorn_brazier_fire"
                    + (tier == 5 ? "" : "_t" + tier) + ".png");
            verifyImage(texture, 16, 256);
            BufferedImage image = ImageIO.read(texture.toFile());
            require(colors.add(image.getRGB(0, 0)), "Tier palettes must be distinct");
            require(read(Path.of(texture + ".mcmeta")).getAsJsonObject("animation").equals(animation),
                    "Tier frame timings must match");
            for (int pixelY = 0; pixelY < 256; pixelY++) {
                for (int pixelX = 0; pixelX < 16; pixelX++) {
                    require(brightness(image.getRGB(pixelX, pixelY)) == brightness(crimson.getRGB(pixelX, pixelY)),
                            "Only hue may change, not flame pixels or brightness");
                }
            }
            if (tier < 5) {
                Path modelPath = assets.resolve("models/block/bloodthorn_brazier_t" + tier + ".json");
                JsonObject model = read(modelPath);
                require(model.get("parent").getAsString().equals(MODEL), "Inherited tier geometry");
                require(!model.has("elements"), "Tier models must not duplicate cuboids");
                require(model.getAsJsonObject("textures").size() == 1, "Body texture must remain unchanged");
                require(model.getAsJsonObject("textures").get("1").getAsString()
                        .equals(MODEL + "_fire_t" + tier), "Tier fire binding");
                try (Reader reader = Files.newBufferedReader(modelPath, StandardCharsets.UTF_8)) {
                    BlockModel parsed = BlockModel.fromStream(reader);
                    parsed.resolveParents(parent -> {
                        require(parent.toString().equals(MODEL), "Only shared brazier parent is allowed");
                        try (Reader parentReader = Files.newBufferedReader(
                                assets.resolve("models/block/bloodthorn_brazier.json"), StandardCharsets.UTF_8)) {
                            return BlockModel.fromStream(parentReader);
                        } catch (Exception cause) {
                            throw new IllegalStateException(cause);
                        }
                    });
                    require(parsed.getElements().size() == 43, "Minecraft resolved tier geometry");
                    require(parsed.getMaterial("1").texture().toString().equals(MODEL + "_fire_t" + tier),
                            "Minecraft resolved tier fire material");
                    require(parsed.getMaterial("0").texture().toString().equals(MODEL),
                            "Minecraft resolved unchanged body material");
                }
            }
        }
    }

    private static int brightness(int color) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        return Math.max(red, Math.max(green, blue)) + Math.min(red, Math.min(green, blue));
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void verifyVector(JsonArray vector, int minimum, int maximum, String label) {
        require(vector != null && vector.size() == 3, "Three coordinates required: " + label);
        for (JsonElement coordinate : vector) {
            double value = coordinate.getAsDouble();
            require(value == Math.rint(value) && value >= minimum && value <= maximum,
                    "Integer 16-grid violation: " + label);
        }
    }

    private static void verifyImage(Path path, int width, int height) throws Exception {
        BufferedImage image = ImageIO.read(path.toFile());
        require(image != null && image.getWidth() == width && image.getHeight() == height, "Texture size: " + path);
        for (int pixelY = 0; pixelY < height; pixelY++) {
            for (int pixelX = 0; pixelX < width; pixelX++) {
                require((image.getRGB(pixelX, pixelY) >>> 24) == 255, "Opaque cuboid texture required");
            }
        }
    }

    private static boolean contains(JsonArray values, String expected) {
        for (JsonElement value : values) {
            if (value.getAsString().equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("BLOODTHORN_ASSET_FAILURE: " + message);
        }
    }
}
