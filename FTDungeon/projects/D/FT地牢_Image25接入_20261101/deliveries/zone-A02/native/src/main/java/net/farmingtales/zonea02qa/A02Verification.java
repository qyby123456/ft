package net.farmingtales.zonea02qa;

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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonea02qa")
@GameTestHolder("ftzonea02qa")
@PrefixGameTestTemplate(false)
public final class A02Verification {
    private static final BlockPos SIZE = new BlockPos(21, 12, 21);
    private static final BlockPos ENTRANCE = new BlockPos(10, 2, 0);
    private static final BlockPos EXIT = new BlockPos(10, 2, 20);
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(10, 2, 10), new BlockPos(8, 2, 8), new BlockPos(2, 2, 10), new BlockPos(15, 2, 13), new BlockPos(16, 2, 6), EXIT);
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW, Blocks.SPAWNER, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.SAND, Blocks.GRAVEL, Blocks.ANVIL);
    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected, Map<BlockPos, CompoundTag> entities, BlockPos minimum, BlockPos maximum) {}
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }
    private static boolean roofHole(int posX, int posZ) {
        if (posX == 17 && posZ == 3) return false;
        if (posX == 20 && posZ >= 1 && posZ <= 6) return true;
        if (posZ == 0 && posX >= 15 && posX <= 19 && posX != 17) return true;
        return posX >= 13 && posX <= 19 && posZ >= 1 && posZ <= 6 && !(posX == 13 && posZ >= 5) && !(posX == 19 && posZ == 6);
    }
    @GameTest(template = "empty", batch = "zone_a02_four_rotations", timeoutTicks = 1200)
    public static void nativePlacementFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftzonea02.project"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected Java 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown or non-vanilla block: " + identity);
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
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 5292 && template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Roundtrip loss or missing cells");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 80, 300, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> entities = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0 && local.getX() < 21 && local.getY() < 12 && local.getZ() < 21, "Record outside bounds");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = record.getCompound("nbt");
                    require(!entity.contains("Items") && !entity.contains("LootTable") && !entity.contains("Book"), "Unexpected inventory or loot");
                    require(Set.of("minecraft:bed", "minecraft:banner").contains(entity.getString("id")), "Unsupported block entity");
                    entities.put(world, entity);
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 300, Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 311, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Fixture area occupied: " + position);
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
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
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            System.out.println("A02_NATIVE_INITIAL_PASS " + fixture.rotation);
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) settled.add(inspect(level, fixture));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "isolated GameTestServer / actual ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("records_checked_each_phase", 21168);
                result.put("initial", initial);
                result.put("after_400_ticks", settled);
                result.put("neighbor_updates_explicit", true);
                result.put("fire_tick_enabled", true);
                result.put("random_tick_speed", 3);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("A02_NATIVE_GAME_TEST_PASS rotations=4/4 records=21168 ticks=400 sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) { throw new IllegalStateException("A02 post-tick verification failed", exception); }
        });
    }
    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int chains = 0;
        int lanterns = 0;
        int beds = 0;
        int banners = 0;
        int candles = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State mismatch " + position + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native survival failure: " + position + " " + actual);
                supported++;
            }
            String name = BuiltInRegistries.BLOCK.getKey(actual.getBlock()).getPath();
            if (name.endsWith("_carpet") || name.endsWith("_bed") || actual.is(Blocks.CANDLE)) {
                BlockPos below = position.below();
                require(Block.isShapeFullBlock(level.getBlockState(below).getCollisionShape(level, below)), "Unsupported furniture or textile: " + position);
                if (name.endsWith("_bed")) beds++;
                if (actual.is(Blocks.CANDLE)) { require(!actual.getValue(BlockStateProperties.LIT), "Nightstand candle should be extinguished"); candles++; }
            }
            if (actual.is(Blocks.CHAIN)) {
                BlockPos anchor = position.above();
                require(Block.isShapeFullBlock(level.getBlockState(anchor).getCollisionShape(level, anchor)) && level.getBlockState(position.below()).is(Blocks.LANTERN), "Chain anchor failure");
                chains++;
            }
            if (actual.is(Blocks.LANTERN)) { require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.CHAIN), "Floating lantern"); lanterns++; }
            BlockEntity blockEntity = level.getBlockEntity(position);
            require(!(blockEntity instanceof Container), "Inventory container forbidden");
            if (fixture.entities.containsKey(position)) {
                require(blockEntity != null, "Missing block entity");
                CompoundTag saved = blockEntity.saveWithFullMetadata();
                CompoundTag expected = fixture.entities.get(position);
                require(saved.getString("id").equals(expected.getString("id")), "Wrong block entity");
                require(!saved.contains("Items") && !saved.contains("LootTable") && !saved.contains("Book"), "Unexpected reward payload");
                if (saved.getString("id").equals("minecraft:banner")) { require(saved.getList("Patterns", Tag.TAG_COMPOUND).equals(expected.getList("Patterns", Tag.TAG_COMPOUND)), "Banner patterns changed"); banners++; }
            } else require(blockEntity == null, "Unexpected block entity");
        }
        require(chains == 4 && lanterns == 4 && beds == 4 && banners == 1 && candles == 1, "Missing bedroom landmark");
        int roofCells = 0;
        int holes = 0;
        for (int posX = 0; posX <= 20; posX++) {
            for (int posZ = 0; posZ <= 20; posZ++) {
                BlockPos roof = transform(new BlockPos(posX, 10, posZ), fixture.origin, fixture.rotation);
                require(level.getBlockState(roof).isAir() == roofHole(posX, posZ), "Roof damage mismatch");
                if (roofHole(posX, posZ)) holes++; else { require(Block.isShapeFullBlock(level.getBlockState(roof).getCollisionShape(level, roof)), "Incomplete retained roof"); roofCells++; }
                for (int posY = 0; posY <= 1; posY++) {
                    BlockPos floor = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(floor).getCollisionShape(level, floor)), "Floor gap");
                }
            }
        }
        for (int posX : new int[]{3, 17}) {
            for (int posZ : new int[]{3, 17}) {
                for (int posY = 2; posY <= 10; posY++) {
                    BlockPos pillar = transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(pillar).getCollisionShape(level, pillar)), "Pillar or beam broken");
                }
            }
        }
        for (int posX = 9; posX <= 11; posX++) {
            for (int posZ = 0; posZ <= 20; posZ++) {
                BlockPos feet = transform(new BlockPos(posX, 2, posZ), fixture.origin, fixture.rotation);
                require(walkable(level, feet) && clear(level, feet.above(2)) && clear(level, feet.above(3)), "Unsafe main route");
            }
        }
        Set<BlockPos> reachable = flood(level, fixture, transform(ENTRANCE, fixture.origin, fixture.rotation));
        require(flood(level, fixture, transform(EXIT, fixture.origin, fixture.rotation)).contains(transform(ENTRANCE, fixture.origin, fixture.rotation)), "Return route blocked");
        for (BlockPos target : TARGETS) require(reachable.contains(transform(target, fixture.origin, fixture.rotation)), "Landmark inaccessible: " + target);
        int perimeter = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) { require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak: " + position); perimeter++; }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity or dropped item");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("valid_block_entities", fixture.entities.size());
        report.put("supported_bed_halves", beds);
        report.put("anchored_lanterns", lanterns);
        report.put("wall_banners", banners);
        report.put("unlit_nightstand_candles", candles);
        report.put("retained_roof_cells", roofCells);
        report.put("designed_collapse_cells", holes);
        report.put("independent_floor_to_roof_pillars", 4);
        report.put("route_width", 3);
        report.put("route_clear_height", 4);
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
        AABB body = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return clear(level, feet) && clear(level, feet.above()) && !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, below, Direction.UP) && level.noCollision(null, body);
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
                BlockPos candidate = current.relative(direction);
                if (fixture.expected.containsKey(candidate) && walkable(level, candidate) && visited.add(candidate)) queue.add(candidate);
            }
        }
        return visited;
    }
}

