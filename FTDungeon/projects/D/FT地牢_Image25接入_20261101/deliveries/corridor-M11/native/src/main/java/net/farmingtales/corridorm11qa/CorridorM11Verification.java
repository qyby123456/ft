package net.farmingtales.corridorm11qa;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridorm11qa")
@GameTestHolder("ftcorridorm11qa")
@PrefixGameTestTemplate(false)
public final class CorridorM11Verification {
    private static final BlockPos SIZE = new BlockPos(15, 12, 40);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE);
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);

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

    private static void writeReport(Path root, Map<String, Object> report) throws Exception {
        Files.createDirectories(root.resolve("qa"));
        Files.writeString(root.resolve("qa/forge-placement.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "corridor_m11_placement", timeoutTicks = 1600)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer may run this fixture");
        Path root = Path.of(System.getProperty("corridorm11.delivery")).toAbsolutePath().normalize();
        Path world = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        require(world.startsWith(root.resolve("qa/forge-run")), "Test world outside this delivery: " + world);
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("sha256").getAsString().equals(hash), "room.json SHA-256 mismatch");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected Java 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
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
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state: " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()), "Forbidden block: " + loaded);
            states.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Native template size mismatch");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 7200, "Native roundtrip record loss");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 7200, "Explicit full-volume records required");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 128, 100, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> entities = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0
                        && local.getX() < 15 && local.getY() < 12 && local.getZ() < 40, "Record out of bounds: " + local);
                BlockPos position = transform(local, origin, rotation);
                require(expected.put(position, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate NBT record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = record.getCompound("nbt");
                    require(entity.getString("id").equals("minecraft:skull") && !entity.contains("Items") && !entity.contains("LootTable"), "Unexpected block entity payload");
                    entities.put(position, entity);
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 100, Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 111, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty() && level.getBlockEntity(position) == null,
                        "Refusing occupied fixture volume: " + position);
            }
            require(level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).isEmpty(), "Entity occupies fixture volume");
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed: " + rotation);
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
            System.out.println("CORRIDOR_M11_INITIAL_PASS " + fixture.rotation);
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
                result.put("evidence", "Actual isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("initial", initial);
                result.put("after_400_ticks", settled);
                result.put("explicit_neighbor_updates", true);
                result.put("random_tick_speed", 3);
                result.put("fire_tick_enabled", true);
                result.put("test_world", world.toString());
                result.put("production_save_touched", false);
                result.put("full_modpack_playtest", false);
                writeReport(root, result);
                System.out.println("CORRIDOR_M11_NATIVE_PASS rotations=4 records=28800 ticks=400 sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                try {
                    writeReport(root, Map.of("status", "FAIL", "sha256", hash, "error", exception.toString()));
                } catch (Exception reportFailure) {
                    exception.addSuppressed(reportFailure);
                }
                throw new IllegalStateException("corridor-M11 stability failed", exception);
            }
        });
    }

    private static boolean full(ServerLevel level, BlockPos position) {
        return Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position));
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return !HAZARDS.contains(block.getBlock()) && block.getFluidState().isEmpty() && block.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockState floor = level.getBlockState(feet.below());
        return clear(level, feet) && clear(level, feet.above()) && full(level, feet.below())
                && floor.getFluidState().isEmpty() && !HAZARDS.contains(floor.getBlock());
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int skulls = 0;
        int candles = 0;
        int lava = 0;
        int vines = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failed: " + position + " " + actual);
                supported++;
            }
            if (actual.is(Blocks.SKELETON_SKULL) || actual.is(Blocks.CANDLE)) {
                require(full(level, position.below()), "Floating skull/candle: " + position);
                if (actual.is(Blocks.SKELETON_SKULL)) {
                    skulls++;
                } else {
                    require(actual.getValue(BlockStateProperties.LIT), "Candle extinguished: " + position);
                    candles++;
                }
            }
            if (actual.is(Blocks.CAVE_VINES) || actual.is(Blocks.CAVE_VINES_PLANT)) {
                require(full(level, position.above()) || level.getBlockState(position.above()).is(Blocks.CAVE_VINES_PLANT), "Detached green vine: " + position);
                vines++;
            }
            if (actual.is(Blocks.LAVA)) {
                lava++;
                require(level.getBlockState(position.above()).is(Blocks.ORANGE_STAINED_GLASS), "Uncovered lava: " + position);
                for (Direction direction : Direction.values()) {
                    BlockPos adjacent = position.relative(direction);
                    require(level.getBlockState(adjacent).is(Blocks.LAVA) || full(level, adjacent), "Unsealed lava edge: " + adjacent);
                }
                for (int deltaX = -1; deltaX <= 1; deltaX++) {
                    for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
                        require(full(level, position.offset(deltaX, 1, deltaZ)), "Open fire-escape cap around lava: " + position);
                    }
                }
            }
            BlockEntity entity = level.getBlockEntity(position);
            require(!(entity instanceof Container), "Unexpected inventory container");
            if (fixture.entities.containsKey(position)) {
                require(entity != null && entity.saveWithFullMetadata().getString("id").equals("minecraft:skull"), "Missing skull block entity: " + position);
            } else {
                require(entity == null, "Unexpected block entity: " + position);
            }
        }
        require(skulls == 22 && candles == 24 && lava == 36 && vines == 40, "Theme landmark count mismatch");
        for (int posX = 0; posX < 15; posX++) {
            for (int posZ = 0; posZ < 40; posZ++) {
                require(full(level, transform(new BlockPos(posX, 11, posZ), fixture.origin, fixture.rotation)), "Roof gap");
            }
        }
        for (int posX = 5; posX <= 9; posX++) {
            for (int posZ = 0; posZ < 40; posZ++) {
                BlockPos feet = transform(new BlockPos(posX, 2, posZ), fixture.origin, fixture.rotation);
                require(walkable(level, feet) && clear(level, feet.above(2)), "Unsafe straight lane at " + feet);
            }
        }
        BlockPos start = transform(new BlockPos(7, 2, 0), fixture.origin, fixture.rotation);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = current.relative(direction);
                if (fixture.expected.containsKey(adjacent) && walkable(level, adjacent) && visited.add(adjacent)) {
                    pending.add(adjacent);
                }
            }
        }
        require(visited.contains(transform(new BlockPos(7, 2, 5), fixture.origin, fixture.rotation)), "Interior not reachable");
        require(visited.contains(transform(new BlockPos(7, 2, 39), fixture.origin, fixture.rotation)), "Exit not reachable");
        int border = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Block/fluid escaped fixture at " + position);
                border++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1))).isEmpty(), "Dropped item or entity appeared");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records_exact", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("valid_skull_block_entities", skulls);
        report.put("lit_candle_clusters", candles);
        report.put("sealed_lava_sources", lava);
        report.put("anchored_vine_cells", vines);
        report.put("roof_full_cells", 600);
        report.put("level_main_route_feet_cells", 200);
        report.put("reachable_feet_positions", visited.size());
        report.put("boundary_air_cells", border);
        report.put("unexpected_entities", 0);
        return report;
    }
}
