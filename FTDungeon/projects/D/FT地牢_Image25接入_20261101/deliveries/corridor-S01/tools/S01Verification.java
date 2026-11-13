package net.farmingtales.corridors01verification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridors01verification")
@GameTestHolder("ftcorridors01verification")
@PrefixGameTestTemplate(false)
public final class S01Verification {
    private static final BlockPos SIZE = new BlockPos(13, 11, 16);
    private static final BlockPos ENTRANCE = new BlockPos(6, 2, 0);
    private static final BlockPos INTERIOR = new BlockPos(6, 2, 2);
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(6, 2, 15), new BlockPos(4, 2, 3), new BlockPos(8, 2, 3), new BlockPos(4, 2, 12), new BlockPos(8, 2, 12));
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER,
            Blocks.DISPENSER, Blocks.DROPPER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           Map<BlockPos, CompoundTag> entities, BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    @GameTest(template = "empty", batch = "corridor_s01_native_placement", timeoutTicks = 1200)
    public static void nativePlacementFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftcorridors01.project"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No template entities allowed");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()), "Forbidden block: " + loaded);
            states.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Template size mismatch");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 2288, "Roundtrip record loss");
        List<Fixture> fixtures = new ArrayList<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 80, 300, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> entities = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0
                        && local.getX() < 13 && local.getY() < 11 && local.getZ() < 16, "Record outside bounds");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = record.getCompound("nbt");
                    require(!entity.contains("Items") && !entity.contains("LootTable") && !entity.contains("Book"), "Unexpected inventory or loot");
                    entities.put(world, entity);
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Fixture area occupied at " + position);
            }
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            require(template.placeInWorld(level, origin, origin, settings, level.random, flags), "Native placement failed: " + rotation);
            fixtures.add(new Fixture(rotation, origin, expected, entities, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            System.out.println("S01_NATIVE_INITIAL_PASS " + fixture.rotation);
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "isolated GameTestServer / actual ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("initial", initial);
                result.put("after_400_ticks", settled);
                result.put("neighbor_updates_explicit", true);
                result.put("fire_tick_enabled", true);
                result.put("random_tick_speed", 3);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S01_NATIVE_GAME_TEST_PASS rotations=4/4 records=9152 ticks=400 sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("S01 post-tick native verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int skulls = 0;
        int candles = 0;
        int rods = 0;
        int chains = 0;
        int foliage = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State mismatch " + position + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native survival failure: " + position + " " + actual);
                supported++;
            }
            if (actual.is(Blocks.SKELETON_SKULL) || actual.is(Blocks.CANDLE) || actual.is(Blocks.MOSS_CARPET)) {
                BlockPos support = position.below();
                require(Block.isShapeFullBlock(level.getBlockState(support).getCollisionShape(level, support)), "Unsupported skull, candle or carpet: " + position);
                if (actual.is(Blocks.SKELETON_SKULL)) skulls++;
                if (actual.is(Blocks.CANDLE)) {
                    require(actual.getValue(BlockStateProperties.LIT), "Unlit candle");
                    candles++;
                }
            }
            if (actual.is(Blocks.END_ROD)) {
                BlockPos anchor = position.below();
                while (level.getBlockState(anchor).is(Blocks.END_ROD)) anchor = anchor.below();
                require(Block.isShapeFullBlock(level.getBlockState(anchor).getCollisionShape(level, anchor)), "Unsupported end rod: " + position);
                rods++;
            }
            if (actual.is(Blocks.CHAIN)) {
                BlockPos anchor = position.above();
                require(Block.isShapeFullBlock(level.getBlockState(anchor).getCollisionShape(level, anchor)), "Floating chain: " + position);
                require(level.getBlockState(position.below()).is(Blocks.IRON_BLOCK), "Chain not attached to cage roof");
                chains++;
            }
            if (actual.is(Blocks.AZALEA_LEAVES)) {
                require(actual.getValue(BlockStateProperties.PERSISTENT), "Leaves may decay");
                foliage++;
            }
            BlockEntity blockEntity = level.getBlockEntity(position);
            require(!(blockEntity instanceof Container), "Inventory container not allowed");
            if (fixture.entities.containsKey(position)) {
                require(blockEntity != null, "Missing block entity: " + position);
                CompoundTag saved = blockEntity.saveWithFullMetadata();
                CompoundTag expected = fixture.entities.get(position);
                require(saved.getString("id").equals(expected.getString("id")), "Block entity type mismatch");
                require(!saved.contains("Items") && !saved.contains("LootTable") && !saved.contains("Book"), "Unexpected reward payload");
            } else {
                require(blockEntity == null, "Unexpected block entity: " + position);
            }
        }
        require(skulls == 10 && candles == 8 && rods == 8 && chains == 4 && foliage == 12, "Missing corridor landmark");
        for (int posX = 0; posX < 13; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                BlockPos roof = transform(new BlockPos(posX, 10, posZ), fixture.origin, fixture.rotation);
                require(Block.isShapeFullBlock(level.getBlockState(roof).getCollisionShape(level, roof)), "Incomplete roof");
            }
        }
        for (int posX = 5; posX <= 7; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                BlockPos feet = transform(new BlockPos(posX, 2, posZ), fixture.origin, fixture.rotation);
                require(walkable(level, feet) && clear(level, feet.above(2)), "Unsafe three-wide straight-through route");
                require(Block.isShapeFullBlock(level.getBlockState(feet.below()).getCollisionShape(level, feet.below())), "Incomplete route floor");
            }
        }
        for (int sideX : new int[]{0, 12}) {
            for (int posY = 0; posY < 11; posY++) {
                for (int posZ = 0; posZ < 16; posZ++) {
                    BlockPos side = transform(new BlockPos(sideX, posY, posZ), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(side).getCollisionShape(level, side)), "Unwanted side opening");
                }
            }
        }
        BlockPos entrance = transform(ENTRANCE, fixture.origin, fixture.rotation);
        BlockPos interior = transform(INTERIOR, fixture.origin, fixture.rotation);
        BlockPos exit = transform(new BlockPos(6, 2, 15), fixture.origin, fixture.rotation);
        Set<BlockPos> reachable = flood(level, fixture, entrance);
        require(reachable.contains(interior) && reachable.contains(exit), "Straight-through route blocked");
        require(flood(level, fixture, exit).contains(entrance), "Return route blocked");
        for (BlockPos target : TARGETS) require(reachable.contains(transform(target, fixture.origin, fixture.rotation)), "Landmark inaccessible: " + target);
        int perimeter = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak: " + position);
                perimeter++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity or dropped item");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("valid_block_entities", fixture.entities.size());
        report.put("supported_skulls", skulls);
        report.put("lit_candle_clusters", candles);
        report.put("supported_end_rods", rods);
        report.put("anchored_hanging_cages", chains);
        report.put("persistent_leaf_cells", foliage);
        report.put("roof_full_blocks", 208);
        report.put("straight_route_width", 3);
        report.put("straight_route_length", 16);
        report.put("reachable_feet_positions", reachable.size());
        report.put("landmark_routes", TARGETS.size());
        report.put("boundary_air_cells_checked", perimeter);
        report.put("reward_inventory", 0);
        return report;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return !HAZARDS.contains(block.getBlock()) && block.getFluidState().isEmpty() && block.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && !HAZARDS.contains(floor.getBlock())
                && floor.getFluidState().isEmpty() && (floor.isFaceSturdy(level, below, Direction.UP) || floor.getBlock() instanceof StairBlock);
    }

    private static Set<BlockPos> flood(ServerLevel level, Fixture fixture, BlockPos start) {
        require(walkable(level, start), "Unsafe route start");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, 1, -1}) {
                    BlockPos candidate = current.relative(direction).offset(0, height, 0);
                    if (!fixture.expected.containsKey(candidate)) {
                        continue;
                    }
                    if (height > 0 && !clear(level, current.above(2)) || height < 0 && !clear(level, candidate.above(2))) {
                        continue;
                    }
                    if (walkable(level, candidate) && visited.add(candidate)) {
                        queue.add(candidate);
                    }
                }
            }
        }
        return visited;
    }
}
