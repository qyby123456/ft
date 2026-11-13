package net.farmingtales.zoneh02qa;

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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzoneh02qa")
@GameTestHolder("ftzoneh02qa")
@PrefixGameTestTemplate(false)
public final class H02GameTests {
    private static final BlockPos SIZE = new BlockPos(29, 16, 33);
    private static final int VOLUME = 15312;
    private static final int SETTLE_TICKS = 320;
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE, Blocks.SAND, Blocks.GRAVEL);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos position(Fixture fixture, int horizontal, int vertical, int depth) {
        return transform(new BlockPos(horizontal, vertical, depth), fixture.origin, fixture.rotation);
    }

    private static void save(Path path, Object report) throws Exception {
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "zone_h02", timeoutTicks = 1500)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Isolated GameTestServer required");
        Path root = Path.of(System.getProperty("ftzoneh02qa.root")).toRealPath();
        require(root.getFileName().toString().equals("zone-H02"), "Incorrect project root");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("id").getAsString().equals("H02") && metadata.get("sha256").getAsString().equals(hash), "Identity/hash mismatch");
        CompoundTag document = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        require(document.getInt("DataVersion") == 3465 && !document.contains("palettes"), "Wrong Java single-palette format");
        require(document.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities present");
        List<BlockState> palette = new ArrayList<>();
        ListTag stateTags = document.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < stateTags.size(); index++) {
            CompoundTag entry = stateTags.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Invalid vanilla block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block state " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()) && !loaded.hasBlockEntity() && loaded.getFluidState().isEmpty(), "Forbidden block/fluid/inventory");
            palette.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), document);
        require(template.getSize().equals(SIZE), "Wrong native size");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "NBT roundtrip loses records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        ListTag records = document.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == VOLUME, "Incomplete explicit volume");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Fixture> fixtures = new ArrayList<>();
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 96, 200, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 29 && local.getY() >= 0 && local.getY() < 16 && local.getZ() >= 0 && local.getZ() < 33, "Out-of-bounds record");
                require(record.getInt("state") >= 0 && record.getInt("state") < palette.size() && !record.contains("nbt"), "Invalid state/entity");
                require(expected.put(transform(local, origin, rotation), palette.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 200, Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 215, Math.max(origin.getZ(), corner.getZ()));
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (BlockPos world : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(world) && level.getFluidState(world).isEmpty(), "Occupied fixture volume " + world);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Native placeInWorld failed");
            Fixture fixture = new Fixture(rotation, origin, expected, minimum, maximum);
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            fixtures.add(fixture);
            initial.add(inspect(level, fixture));
        }
        save(root.resolve("qa/native-initial.json"), Map.of("sha256", hash, "rotations", initial));
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture));
                    StructureTemplate capture = new StructureTemplate();
                    capture.fillFromWorld(level, fixture.minimum, fixture.maximum.subtract(fixture.minimum).offset(1, 1, 1), false, null);
                    NbtIo.writeCompressed(capture.save(new CompoundTag()), root.resolve("qa/capture-" + fixture.rotation.name().toLowerCase() + ".nbt").toFile());
                }
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("sha256", hash);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("settle_ticks", SETTLE_TICKS);
                report.put("records_per_rotation", VOLUME);
                report.put("records_checked_each_phase", VOLUME * 4);
                report.put("palette_states_validated", palette.size());
                report.put("initial", initial);
                report.put("settled", settled);
                report.put("state_drift", 0);
                report.put("entities_or_drops", 0);
                report.put("fire_tick", true);
                report.put("random_tick_speed", 3);
                report.put("deployment", "isolated disposable GameTest world only");
                save(root.resolve("qa/native.json"), report);
                System.out.println("H02_NATIVE_PASS rotations=4 records=" + VOLUME * 4 + " ticks=" + SETTLE_TICKS + " sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                try { save(root.resolve("qa/native.json"), Map.of("status", "FAIL", "sha256", hash, "error", exception.toString())); }
                catch (Exception writeFailure) { exception.addSuppressed(writeFailure); }
                throw new IllegalStateException("H02 native stability failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int surviving = 0;
        int lanterns = 0;
        int chains = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos world = entry.getKey();
            BlockState actual = level.getBlockState(world);
            require(actual.equals(entry.getValue()), "State drift " + fixture.rotation + " " + world + " expected=" + entry.getValue() + " actual=" + actual);
            require(level.getBlockEntity(world) == null && level.getFluidState(world).isEmpty(), "Unexpected block entity/fluid");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, world), "Unsupported block " + world + " " + actual);
                surviving++;
            }
            if (actual.is(Blocks.CHAIN)) {
                BlockPos anchor = world.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                require(Block.canSupportCenter(level, anchor, Direction.DOWN), "Chain has no ceiling anchor " + world);
                chains++;
            }
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (actual.is(Blocks.SPRUCE_TRAPDOOR) && actual.getValue(BlockStateProperties.OPEN)) {
                BlockPos below = world.below();
                BlockState seat = level.getBlockState(below);
                require(seat.is(Blocks.SPRUCE_STAIRS), "Chair back lacks seat support");
                for (AABB upper : actual.getCollisionShape(level, world).toAabbs()) {
                    require(upper.minY == 0 && seat.getCollisionShape(level, below).toAabbs().stream().anyMatch(lower ->
                            lower.maxY == 1 && lower.minX <= upper.minX && lower.maxX >= upper.maxX
                            && lower.minZ <= upper.minZ && lower.maxZ >= upper.maxZ), "Chair back floats above low half of seat " + world);
                }
            }
        }
        for (int horizontal = 0; horizontal < 29; horizontal++) {
            for (int depth = 0; depth < 33; depth++) {
                for (int vertical : new int[]{0, 14}) {
                    BlockPos world = position(fixture, horizontal, vertical, depth);
                    require(Block.isShapeFullBlock(level.getBlockState(world).getCollisionShape(level, world)), "Incomplete floor or roof");
                }
            }
        }
        for (int horizontal : new int[]{12, 16}) {
            for (int depth : new int[]{11, 17, 23}) {
                require(level.getBlockState(position(fixture, horizontal, 1, depth)).is(Blocks.STRIPPED_SPRUCE_LOG), "Missing table leg");
                require(level.getBlockState(position(fixture, horizontal, 2, depth)).is(Blocks.DARK_OAK_PLANKS), "Tabletop disconnected from leg");
            }
        }
        for (int horizontal : new int[]{4, 24}) {
            for (int depth : new int[]{8, 16, 24}) {
                for (int vertical = 0; vertical <= 14; vertical++) require(!level.isEmptyBlock(position(fixture, horizontal, vertical, depth)), "Column/beam vertical gap");
            }
        }
        Set<BlockPos> route = new HashSet<>();
        for (int horizontal = 0; horizontal < 29; horizontal++) {
            for (int depth = 0; depth < 33; depth++) {
                boolean entrance = horizontal >= 13 && horizontal <= 15 && depth <= 8;
                boolean sides = (horizontal >= 6 && horizontal <= 8 || horizontal >= 20 && horizontal <= 22) && depth >= 6 && depth <= 27;
                boolean ends = horizontal >= 6 && horizontal <= 22 && (depth >= 6 && depth <= 8 || depth >= 25 && depth <= 27);
                if (entrance || sides || ends) {
                    BlockPos feet = position(fixture, horizontal, 1, depth);
                    for (int height = 0; height < 3; height++) require(clear(level, feet.above(height)), "Blocked three-high main route");
                    require(Block.isShapeFullBlock(level.getBlockState(feet.below()).getCollisionShape(level, feet.below())), "Unsafe floor");
                    route.add(feet);
                }
            }
        }
        for (int horizontal = 13; horizontal <= 15; horizontal++) {
            for (int vertical = 1; vertical <= 4; vertical++) require(clear(level, position(fixture, horizontal, vertical, 0)), "Blocked north port");
        }
        require(route.size() == 216, "Incorrect route coverage");
        require(flood(route, position(fixture, 14, 1, 0)).equals(route), "Entrance route disconnected");
        require(flood(route, position(fixture, 14, 1, 5)).equals(route), "Safety point return route disconnected");
        int boundary = 0;
        for (BlockPos world : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(world)) {
                require(level.isEmptyBlock(world) && level.getFluidState(world).isEmpty(), "Boundary escape/falling blocks");
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1)).inflate(1)).isEmpty(), "Unexpected entity/drop");
        return Map.of("rotation", fixture.rotation.name(), "records", fixture.expected.size(), "surviving_blocks", surviving,
                "route_feet_cells", route.size(), "lanterns", lanterns, "chains", chains,
                "boundary_air_cells", boundary, "floor_cells", 957, "roof_cells", 957, "bidirectional", true);
    }

    private static boolean clear(ServerLevel level, BlockPos world) {
        BlockState state = level.getBlockState(world);
        return state.getCollisionShape(level, world).isEmpty() && state.getFluidState().isEmpty() && !FORBIDDEN.contains(state.getBlock());
    }

    private static Set<BlockPos> flood(Set<BlockPos> route, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = current.relative(direction);
                if (route.contains(candidate) && visited.add(candidate)) pending.add(candidate);
            }
        }
        return visited;
    }
}
