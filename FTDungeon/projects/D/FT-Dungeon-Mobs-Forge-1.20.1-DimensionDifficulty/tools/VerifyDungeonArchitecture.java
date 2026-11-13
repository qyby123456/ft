package net.farmingtales.ftdungeonmobs.verification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import net.farmingtales.ftdungeonmobs.DungeonArchitectureBlock;
import net.farmingtales.ftdungeonmobs.DungeonArchitectureCatalog;
import net.farmingtales.ftdungeonmobs.DungeonArchitectureContent;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

public final class VerifyDungeonArchitecture {
    private VerifyDungeonArchitecture() {}

    public static void main(String[] arguments) throws Exception {
        verifyResources(Path.of(arguments[0]));
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("DUNGEON_ARCHITECTURE_FAILURE: " + message);
        }
    }

    public static void verifyResources(Path resources) throws Exception {
        Path assets = resources.resolve("assets/ftdungeonmobs");
        Set<String> identifiers = new HashSet<>();
        Set<String> recipes = new HashSet<>();
        int cuboids = 0;
        int faces = 0;
        for (DungeonArchitectureCatalog.Model model : DungeonArchitectureCatalog.MODELS) {
            String name = model.id();
            require(identifiers.add(name), "Unique model IDs");
            Path path = assets.resolve("models/block/" + name + ".json");
            try (java.io.Reader reader = Files.newBufferedReader(path)) {
                BlockModel parsed = BlockModel.fromStream(reader);
                require(parsed != null, "Native block model parser: " + name);
            }
            JsonObject data = read(path);
            JsonObject variants = read(assets.resolve("blockstates/" + name + ".json")).getAsJsonObject("variants");
            require(variants.size() == (model.nativeParent().equals("stairs") ? 40 : model.nativeParent().equals("slab") ? 3 : 4), "Expected blockstate variants: " + name);
            if (model.nativeParent().isEmpty()) {
                int modelFaces = 0;
                require(data.getAsJsonArray("elements").size() <= 16, "Per-block cuboid budget");
                for (JsonElement value : data.getAsJsonArray("elements")) {
                    JsonObject element = value.getAsJsonObject();
                    require(!element.has("rotation"), "Unrotated integer cuboids only");
                    for (int axis = 0; axis < 3; axis++) {
                        double start = element.getAsJsonArray("from").get(axis).getAsDouble();
                        double end = element.getAsJsonArray("to").get(axis).getAsDouble();
                        require(start == Math.rint(start) && end == Math.rint(end) && start >= 0 && start < end && end <= 16,
                                "Positive integer geometry inside one block");
                    }
                    JsonObject directions = element.getAsJsonObject("faces");
                    require(directions.size() > 0, "No face-less exported cuboid");
                    modelFaces += directions.size();
                    for (var direction : directions.entrySet()) {
                        JsonObject face = direction.getValue().getAsJsonObject();
                        require(face.get("texture").getAsString().equals("#atlas"), "Shared material only");
                        for (int coordinate = 0; coordinate < 4; coordinate++) {
                            double pixel = face.getAsJsonArray("uv").get(coordinate).getAsDouble() * (coordinate % 2 == 0 ? 8 : 4);
                            require(pixel == Math.rint(pixel) && pixel >= 0 && pixel <= (coordinate % 2 == 0 ? 128 : 64), "Pixel-aligned UV");
                        }
                    }
                }
                require(modelFaces <= 96, "Per-block face budget");
                cuboids += data.getAsJsonArray("elements").size();
                faces += modelFaces;
            } else {
                require(!data.has("elements") && data.get("parent").getAsString().startsWith("minecraft:block/"), "Native slab/stair geometry reuse");
            }
            require(read(assets.resolve("models/item/" + name + ".json")).get("parent").getAsString().equals("ftdungeonmobs:block/" + name), "Item model reuses block");
            JsonObject recipe = read(resources.resolve("data/ftdungeonmobs/recipes/" + name + ".json"));
            require(recipe.getAsJsonObject("result").get("item").getAsString().equals("ftdungeonmobs:" + name), "Recipe result");
            if (recipe.has("ingredients")) {
                java.util.List<String> ingredients = new java.util.ArrayList<>();
                recipe.getAsJsonArray("ingredients").forEach(ingredient -> ingredients.add(ingredient.toString()));
                ingredients.sort(String::compareTo);
                require(recipes.add(ingredients.toString()), "No ambiguous shapeless recipes");
            }
            require(Files.isRegularFile(resources.resolve("data/ftdungeonmobs/loot_tables/blocks/" + name + ".json")), "Loot table exists");
            for (String language : new String[]{"zh_cn", "en_us"}) {
                require(read(assets.resolve("lang/" + language + ".json")).has("block.ftdungeonmobs." + name), "Both language bindings");
            }
        }
        require(identifiers.size() == 24, "Twenty-four architecture modules");
        java.awt.image.BufferedImage texture = ImageIO.read(assets.resolve("textures/block/dungeon_architecture.png").toFile());
        require(texture.getWidth() == 128 && texture.getHeight() == 64, "Shared 128x64 atlas");
        for (int across = 0; across < texture.getWidth(); across++) {
            for (int depth = 0; depth < texture.getHeight(); depth++) {
                require((texture.getRGB(across, depth) >>> 24) == 255, "Opaque pixels; no transparent cards");
            }
        }
        require(read(resources.resolve("data/ftdungeonmobs/tags/blocks/dungeon_architecture.json")).getAsJsonArray("values").size() == 24, "Architecture tag covers every block");
        System.out.println("DUNGEON_ARCHITECTURE_ASSETS_VERIFIED: 24 modules; " + cuboids + " custom integer cuboids / " + faces
                + " faces; 16-cuboid/96-face per-block budgets; 128x64 shared atlas, native stair/slab parents, recipes/items/loot/languages.");
    }

    public static void verifyRuntime(Path resources) throws Exception {
        verifyResources(resources);
        int states = 0;
        for (DungeonArchitectureContent.Entry entry : DungeonArchitectureContent.ENTRIES) {
            DungeonArchitectureCatalog.Model model = entry.model();
            Block block = entry.block().get();
            require(ForgeRegistries.BLOCKS.getKey(block).equals(FTDungeonMobs.resource(model.id())), "Real block registry binding");
            require(entry.item().get() instanceof BlockItem item && item.getBlock() == block, "Real BlockItem binding");
            require(RecipeManager.fromJson(FTDungeonMobs.resource(model.id()), read(resources.resolve("data/ftdungeonmobs/recipes/" + model.id() + ".json"))) != null, "Native recipe parsing");
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                VoxelShape shape = block.getShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                require(!shape.isEmpty() && !state.hasBlockEntity() && state.getLightEmission() == model.light(), "Nonempty shape, fixed light and no BE");
                for (AABB box : shape.toAabbs()) {
                    for (double coordinate : new double[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ}) {
                        require(coordinate >= 0 && coordinate <= 1 && coordinate * 16 == Math.rint(coordinate * 16), "Integer collision within unit block");
                    }
                }
                states++;
            }
            if (block instanceof DungeonArchitectureBlock) {
                JsonObject data = read(resources.resolve("assets/ftdungeonmobs/models/block/" + model.id() + ".json"));
                VoxelShape expected = Shapes.empty();
                for (JsonElement value : data.getAsJsonArray("elements")) {
                    JsonObject element = value.getAsJsonObject();
                    int[] coordinates = new int[6];
                    for (int axis = 0; axis < 3; axis++) {
                        coordinates[axis] = element.getAsJsonArray("from").get(axis).getAsInt();
                        coordinates[axis + 3] = element.getAsJsonArray("to").get(axis).getAsInt();
                    }
                    expected = Shapes.or(expected, Block.box(coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5]));
                }
                BlockState state = block.defaultBlockState();
                for (int turns = 0; turns < 4; turns++) {
                    VoxelShape actual = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                    require(!Shapes.joinIsNotEmpty(expected, actual, BooleanOp.NOT_SAME), "Visual/collision equality in four directions: " + model.id());
                    state = state.rotate(Rotation.CLOCKWISE_90);
                    VoxelShape next = Shapes.empty();
                    for (AABB box : expected.toAabbs()) {
                        next = Shapes.or(next, Shapes.box(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX));
                    }
                    expected = next;
                }
                require(state.equals(block.defaultBlockState()) && state.getValue(DungeonArchitectureBlock.FACING) == Direction.NORTH, "Four-turn rotation round trip");
            }
        }
        require(states == 174, "22x4 custom + 80 stair + 6 slab states");
        String message = "DUNGEON_ARCHITECTURE_RUNTIME_VERIFIED: 24 registered blocks/items, 174 native states; "
                + "22 custom models have exact four-direction visual/collision equality; 24 native recipes; integer bounded shapes, fixed light, no new block entities. "
                + "No player world, live placement, client lighting or FPS test executed.";
        Path report = resources.getParent().getParent().getParent().resolve("build/reports/dungeon-architecture-runtime.txt");
        Files.createDirectories(report.getParent());
        Files.writeString(report, message + System.lineSeparator(), StandardCharsets.UTF_8);
        System.out.println(message);
    }
}
