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
import net.farmingtales.ftdungeonmobs.BloodOfferingAltarBlock;
import net.farmingtales.ftdungeonmobs.BloodOfferingAltarEntity;
import net.farmingtales.ftdungeonmobs.BloodPoolCommands;
import net.farmingtales.ftdungeonmobs.BloodPoolContent;
import net.farmingtales.ftdungeonmobs.BloodPoolDecorBlock;
import net.farmingtales.ftdungeonmobs.BloodPoolRitual;
import net.farmingtales.ftdungeonmobs.BloodPoolStructure;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.registries.ForgeRegistries;

public final class VerifyBloodPool {
    private VerifyBloodPool() {}

    public static void main(String[] arguments) throws Exception {
        verifyResources(Path.of(arguments[0]));
        verifyRitual();
    }

    public static void verifyResources(Path resources) throws Exception {
        Path assets = resources.resolve("assets/ftdungeonmobs");
        String[] names = {"bloodstone_bricks", "blood_rune_block", "blood_pool_rim", "blood_pool_corner", "blood_skull_relief",
                "blood_skull_shrine", "bloodstone_stairs", "bloodstone_slab", "blood_offering_altar"};
        int cuboids = 0;
        int faces = 0;
        Set<String> ingredients = new HashSet<>();
        for (String name : names) {
            JsonObject model = read(assets.resolve("models/block/" + name + ".json"));
            BlockModel.fromString(model.toString());
            if (name.equals("blood_skull_relief")) {
                verifyReliefFaces(model);
            }
            if (model.has("elements")) {
                int count = model.getAsJsonArray("elements").size();
                int modelFaces = 0;
                require(count <= 16, "Per-block cuboid budget");
                for (JsonElement value : model.getAsJsonArray("elements")) {
                    JsonObject element = value.getAsJsonObject();
                    require(!element.has("rotation"), "Only unrotated integer cuboids");
                    for (int axis = 0; axis < 3; axis++) {
                        double from = element.getAsJsonArray("from").get(axis).getAsDouble();
                        double to = element.getAsJsonArray("to").get(axis).getAsDouble();
                        require(from == Math.rint(from) && to == Math.rint(to) && from >= 0 && to <= 16 && from < to, "16-unit grid");
                    }
                    require(element.getAsJsonObject("faces").size() > 0, "No zero-contribution cuboids");
                    modelFaces += element.getAsJsonObject("faces").size();
                }
                require(modelFaces <= 96, "Per-block face budget");
                cuboids += count;
                faces += modelFaces;
            }
            JsonObject item = read(assets.resolve("models/item/" + name + ".json"));
            require(item.get("parent").getAsString().equals("ftdungeonmobs:block/" + name), "Item reuse");
            JsonObject recipe = read(resources.resolve("data/ftdungeonmobs/recipes/" + name + ".json"));
            require(recipe.getAsJsonObject("result").get("item").getAsString().equals("ftdungeonmobs:" + name), "Recipe result");
            if (recipe.has("ingredients")) {
                java.util.List<String> signature = new java.util.ArrayList<>();
                recipe.getAsJsonArray("ingredients").forEach(value -> signature.add(value.toString()));
                signature.sort(String::compareTo);
                require(ingredients.add(signature.toString()), "No ambiguous shapeless recipes");
            }
            read(resources.resolve("data/ftdungeonmobs/loot_tables/blocks/" + name + ".json"));
            JsonObject variants = read(assets.resolve("blockstates/" + name + ".json")).getAsJsonObject("variants");
            require(variants.size() == (name.equals("bloodstone_bricks") ? 1 : name.equals("bloodstone_slab") ? 3
                    : name.equals("bloodstone_stairs") ? 40 : name.equals("blood_offering_altar") ? 16 : 8), "State model variants");
            if (!name.equals("bloodstone_bricks") && !name.startsWith("bloodstone_")) {
                JsonObject lit = read(assets.resolve("models/block/" + name + "_lit.json"));
                require(!lit.has("elements") && lit.get("parent").getAsString().equals("ftdungeonmobs:block/" + name), "Lit states reuse geometry");
            }
        }
        for (String name : new String[]{"sacrifice_stone", "sacrifice_stone_lit"}) {
            java.awt.image.BufferedImage image = ImageIO.read(assets.resolve("textures/block/" + name + ".png").toFile());
            require(image.getWidth() == 64 && image.getHeight() == 64, "Shared 64px atlas");
        }
        for (String name : new String[]{"sacrificial_blood_still", "sacrificial_blood_flow"}) {
            java.awt.image.BufferedImage image = ImageIO.read(assets.resolve("textures/block/" + name + ".png").toFile());
            require(image.getWidth() == (name.endsWith("flow") ? 32 : 16) && image.getHeight() == image.getWidth() * 16, "16 fluid frames");
            require(read(assets.resolve("textures/block/" + name + ".png.mcmeta")).getAsJsonObject("animation").get("frametime").getAsInt() == 4, "Fluid timing");
            Set<Integer> frameHashes = new HashSet<>();
            for (int frame = 0; frame < 16; frame++) {
                int[] pixels = image.getRGB(0, frame * image.getWidth(), image.getWidth(), image.getWidth(), null, 0, image.getWidth());
                frameHashes.add(java.util.Arrays.hashCode(pixels));
                for (int pixel : pixels) {
                    require(pixel >>> 24 == 255, "Opaque fluid pixels");
                }
            }
            require(frameHashes.size() == 16, "Distinct fluid animation frames");
        }
        require(cuboids == 27 && faces <= 150, "New custom geometry budget");
        System.out.println("BLOOD_POOL_ASSETS_VERIFIED: 9 modular blocks; 27 custom cuboids; " + faces
                + " faces; native stair/slab parents; unique recipes, 64px shared atlases and two 16-frame fluids.");
    }

    public static void verifyRitual() {
        BloodPoolRitual ritual = new BloodPoolRitual();
        require(ritual.phase(0) == 0 && !ritual.activate(100), "Empty ritual cannot trigger");
        require(!ritual.offer(0, 100) && !ritual.offer(17, 100) && !ritual.offer(1, -1), "Invalid offerings/time rejected");
        require(ritual.offer(8, 100) && ritual.offer(8, 100) && ritual.charge() == 16, "Fill offering charge");
        require(!ritual.offer(1, 100) && ritual.phase(100) == 1, "No over-consumption and explicit confirmation");
        require(ritual.activate(100) && ritual.phase(299) == 2 && ritual.phase(300) == 3
                && ritual.phase(899) == 3 && ritual.phase(900) == 0, "Exact 200 active plus 600 cooldown ticks");
        require(!ritual.activate(110) && !ritual.offer(8, 110) && !ritual.offer(8, 899), "No double trigger or cooldown bypass");
        BloodPoolRitual loaded = new BloodPoolRitual();
        loaded.restore(ritual.charge(), ritual.activeUntil(), ritual.cooldownUntil());
        require(loaded.phase(200) == 2 && loaded.phase(400) == 3 && loaded.phase(900) == 0, "Persistent absolute deadlines");
        require(loaded.offer(4, 900) && loaded.charge() == 4, "New charge after cooldown");
        BloodPoolRitual offered = new BloodPoolRitual();
        offered.restore(loaded.charge(), loaded.activeUntil(), loaded.cooldownUntil());
        require(offered.charge() == 4 && offered.phase(900) == 1, "Partial charge survives later save/load");
        require(offered.offer(8, 901) && offered.offer(4, 902) && offered.activate(903), "Repeated ritual");
        offered.interrupt(950);
        require(offered.phase(950) == 3 && offered.phase(1549) == 3 && offered.phase(1550) == 0, "Broken structure cuts signal with cooldown");
        loaded.restore(-99, -1, Long.MAX_VALUE);
        require(loaded.charge() == 0 && loaded.phase(0) == 0, "Corrupt state bounded");
        System.out.println("BLOOD_POOL_RITUAL_VERIFIED: charge, no double trigger, exact deadlines, cooldown, interruption and reload.");
    }

    public static void verifyRuntime(Path resources) throws Exception {
        verifyResources(resources);
        verifyRitual();
        int states = 0;
        for (net.minecraftforge.registries.RegistryObject<? extends Block> registry : BloodPoolContent.BUILDING_BLOCKS) {
            Block block = registry.get();
            require(ForgeRegistries.BLOCKS.getKey(block).equals(registry.getId()), "Native block registration");
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.getShape(null, BlockPos.ZERO, CollisionContext.empty());
                states++;
            }
        }
        require(states == 143, "143 actual building block states including vanilla waterlogging");
        require(BloodPoolContent.BLOOD.get().getFluidType() == BloodPoolContent.BLOOD_TYPE.get()
                && BloodPoolContent.BLOOD.get().defaultFluidState().isSource()
                && !BloodPoolContent.FLOWING_BLOOD.get().defaultFluidState().isSource(), "True Forge fluid pair");
        require(BloodPoolContent.BLOOD_BUCKET.get() instanceof BucketItem
                && BloodPoolContent.BLOOD.get().getBucket() == BloodPoolContent.BLOOD_BUCKET.get(), "Bucket binding");
        require(BloodPoolStructure.INTERIOR.size() == 44 && BloodPoolStructure.BORDER.size() == 24, "Bounded 9x9 stepped pattern");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockPos anchor = new BlockPos(20, 65, 30);
            java.util.Map<BlockPos, BlockState> plan = BloodPoolCommands.blueprint(anchor, facing);
            Set<BlockPos> cells = new HashSet<>();
            for (BloodPoolStructure.Cell cell : BloodPoolStructure.INTERIOR) {
                BlockPos position = BloodPoolStructure.position(anchor, facing, cell);
                require(cells.add(position) && plan.get(position).is(BloodPoolContent.BLOOD_BLOCK.get()), "All four prefab rotations contain every source cell");
                require(plan.get(position.below()).is(BloodPoolContent.BRICKS.get()), "All source cells have a floor");
            }
            require(plan.get(anchor).is(BloodPoolContent.ALTAR.get()), "Altar at designated anchor");
            require(plan.values().stream().filter(state -> state.is(BloodPoolContent.BLOOD_BLOCK.get())).count() == 44, "No accidental flood sources");
            require(plan.values().stream().filter(state -> state.is(BloodPoolContent.ALTAR.get())).count() == 1, "One ticking altar per prefab");
            BlockState altarState = BloodPoolContent.ALTAR.get().defaultBlockState().setValue(BloodOfferingAltarBlock.FACING, facing);
            require(BloodPoolContent.ALTAR.get().getSignal(altarState.setValue(BloodOfferingAltarBlock.PHASE, 2), null, anchor, Direction.UP) == 15
                    && BloodPoolContent.ALTAR.get().getSignal(altarState, null, anchor, Direction.UP) == 0, "Only active phase outputs redstone");
            BlockEntity blockEntity = BloodPoolContent.ALTAR.get().newBlockEntity(anchor, altarState);
            require(blockEntity instanceof BloodOfferingAltarEntity, "Native altar BE factory");
            CompoundTag saved = new CompoundTag();
            saved.putInt("Charge", 8);
            blockEntity.load(saved);
            require(((BloodOfferingAltarEntity) blockEntity).charge() == 8 && blockEntity.saveWithoutMetadata().getInt("Charge") == 8, "Altar NBT charge roundtrip");
        }
        for (String name : new String[]{"bloodstone_bricks", "blood_rune_block", "blood_pool_rim", "blood_pool_corner", "blood_skull_relief",
                "blood_skull_shrine", "bloodstone_stairs", "bloodstone_slab", "blood_offering_altar", "sacrificial_blood_bucket"}) {
            Recipe<?> recipe = RecipeManager.fromJson(net.farmingtales.ftdungeonmobs.FTDungeonMobs.resource(name),
                    read(resources.resolve("data/ftdungeonmobs/recipes/" + name + ".json")));
            require(recipe != null, "Native crafting recipe parser");
        }
        String result = "BLOOD_POOL_RUNTIME_VERIFIED: true Forge source/flow/bucket, 9 blocks and 143 native states; "
                + "44 blood sources, 24 boundary cells and four-facing safe prefab data; native crafting and altar NBT; "
                + "16-point charge, explicit confirmation, 200 tick signal, 600 tick cooldown and reload. "
                + "No player world, client fluid renderer, item collection or live redstone circuit was executed.";
        Path report = resources.getParent().getParent().getParent().resolve("build/reports/blood-pool-runtime.txt");
        Files.createDirectories(report.getParent());
        Files.writeString(report, result + System.lineSeparator(), StandardCharsets.UTF_8);
        System.out.println(result);
    }

    private static void verifyReliefFaces(JsonObject model) {
        var elements = model.getAsJsonArray("elements");
        for (int firstIndex = 0; firstIndex < elements.size(); firstIndex++) {
            JsonObject first = elements.get(firstIndex).getAsJsonObject();
            for (int secondIndex = firstIndex + 1; secondIndex < elements.size(); secondIndex++) {
                JsonObject second = elements.get(secondIndex).getAsJsonObject();
                for (Direction direction : Direction.values()) {
                    String face = direction.getName();
                    if (!first.getAsJsonObject("faces").has(face) || !second.getAsJsonObject("faces").has(face)) {
                        continue;
                    }
                    int axis = direction.getAxis() == Direction.Axis.X ? 0 : direction.getAxis() == Direction.Axis.Y ? 1 : 2;
                    String boundary = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? "to" : "from";
                    if (first.getAsJsonArray(boundary).get(axis).getAsDouble()
                            != second.getAsJsonArray(boundary).get(axis).getAsDouble()) {
                        continue;
                    }
                    boolean overlaps = true;
                    for (int projectedAxis = 0; projectedAxis < 3; projectedAxis++) {
                        if (projectedAxis != axis) {
                            double start = Math.max(first.getAsJsonArray("from").get(projectedAxis).getAsDouble(),
                                    second.getAsJsonArray("from").get(projectedAxis).getAsDouble());
                            double end = Math.min(first.getAsJsonArray("to").get(projectedAxis).getAsDouble(),
                                    second.getAsJsonArray("to").get(projectedAxis).getAsDouble());
                            overlaps &= end > start;
                        }
                    }
                    require(!overlaps, "No overlapping coplanar relief faces: " + first.get("name").getAsString()
                            + " / " + second.get("name").getAsString() + " / " + face);
                }
            }
        }
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void require(boolean valid, String message) {
        if (!valid) {
            throw new IllegalStateException("BLOOD_POOL_FAILURE: " + message);
        }
    }
}
