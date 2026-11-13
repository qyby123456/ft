package net.farmingtales.corridorm20qa;

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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftcorridorm20qa")
@GameTestHolder("ftcorridorm20qa")
@PrefixGameTestTemplate(false)
public final class CorridorM20GameTests {
    private static final BlockPos SIZE = new BlockPos(13, 11, 40);
    private static final int VOLUME = 5720;
    private static final int SETTLE_TICKS = 320;
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static void save(Path path, Object report) throws Exception {
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    @GameTest(template = "empty", batch = "corridor_m20", timeoutTicks = 1200)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is permitted");
        Path root = Path.of(System.getProperty("ftcorridorm20qa.root")).toRealPath();
        require(root.getFileName().toString().equals("corridor-M20"), "Refusing a different project root");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("id").getAsString().equals("corridor-M20") && metadata.get("sha256").getAsString().equals(hash), "Identity/hash mismatch");
        CompoundTag document = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        require(document.getInt("DataVersion") == 3465 && !document.contains("palettes"), "Wrong Java single-palette format");
        require(document.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag stateTags = document.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < stateTags.size(); index++) {
            CompoundTag entry = stateTags.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Invalid vanilla registry entry: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block state: " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()) && !loaded.hasBlockEntity() && loaded.getFluidState().isEmpty(), "Forbidden block, inventory or fluid");
            palette.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), document);
        require(template.getSize().equals(SIZE), "Incorrect native structure size");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "Native NBT roundtrip loses records");
        ListTag records = document.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == VOLUME, "Incomplete explicit volume");
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 96, 300, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 13 && local.getY() >= 0 && local.getY() < 11 && local.getZ() >= 0 && local.getZ() < 40, "Out-of-bounds record");
                require(record.getInt("state") >= 0 && record.getInt("state") < palette.size() && !record.contains("nbt"), "Invalid state or block entity");
                require(expected.put(transform(local, origin, rotation), palette.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 300, Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 310, Math.max(origin.getZ(), corner.getZ()));
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied isolated fixture volume: " + position + " " + level.getBlockState(position));
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Native placeInWorld failed");
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            initial.add(inspect(level, fixture));
        }
        long start = level.getGameTime();
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            try {
                require(level.getGameTime() - start >= SETTLE_TICKS, "Insufficient stability ticks");
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture));
                    StructureTemplate capture = new StructureTemplate();
                    capture.fillFromWorld(level, fixture.minimum, fixture.maximum.subtract(fixture.minimum).offset(1, 1, 1), false, null);
                    NbtIo.writeCompressed(capture.save(new CompoundTag()), root.resolve("qa/native-placed-" + fixture.rotation.name().toLowerCase() + ".nbt").toFile());
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "corridor-M20");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("palette_states_validated", palette.size());
                result.put("rotations", 4);
                result.put("ticks", level.getGameTime() - start);
                result.put("record_comparisons_per_snapshot", VOLUME * 4);
                result.put("initial", initial);
                result.put("after_stability", settled);
                result.put("neighbor_updates", "UPDATE_ALL placement and explicit neighbor notifications");
                result.put("random_tick_speed", 3);
                result.put("fixture_chunks_forced", true);
                result.put("fixture_origin_y", 300);
                result.put("final_template_sha256_rechecked", hash.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))))));
                require(result.get("final_template_sha256_rechecked").equals(true), "Source template changed during validation");
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                save(root.resolve("qa/native.json"), result);
                System.out.println("CORRIDOR_M20_NATIVE_PASS rotations=4 records=" + VOLUME * 4 + " ticks=" + SETTLE_TICKS + " sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                try {
                    save(root.resolve("qa/native.json"), Map.of("status", "FAIL", "sha256", hash, "error", exception.toString()));
                } catch (Exception writeFailure) {
                    exception.addSuppressed(writeFailure);
                }
                throw new IllegalStateException("Corridor M20 native stability failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int lanterns = 0;
        int chains = 0;
        int buttons = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State drift at " + position + ": " + entry.getValue() + " -> " + actual);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(position) == null, "Unexpected fluid/block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native survival check failed: " + position);
                supported++;
            }
            if (actual.is(Blocks.LANTERN)) {
                require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.IRON_BLOCK), "Unanchored cage lantern");
                lanterns++;
            }
            if (actual.is(Blocks.CHAIN)) {
                require(level.getBlockState(position.above()).isFaceSturdy(level, position.above(), Direction.DOWN)
                        && level.getBlockState(position.below()).is(Blocks.IRON_BLOCK), "Unanchored cage chain");
                chains++;
            }
            if (actual.is(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                Direction direction = actual.getValue(BlockStateProperties.HORIZONTAL_FACING);
                require(level.getBlockState(position.relative(direction.getOpposite())).is(Blocks.SMOOTH_QUARTZ), "Unsupported gauge button");
                buttons++;
            }
        }
        require(lanterns == 4 && chains == 4 && buttons == 12, "Theme feature count mismatch");
        for (int[] cage : new int[][]{{6, 7}, {5, 15}, {7, 23}, {6, 31}}) {
            for (int horizontal = cage[0] - 1; horizontal <= cage[0] + 1; horizontal++) {
                for (int depth = cage[1] - 1; depth <= cage[1] + 1; depth++) {
                    BlockPos base = transform(new BlockPos(horizontal, 5, depth), fixture.origin, fixture.rotation);
                    require(level.getBlockState(base).getCollisionShape(level, base).max(Direction.Axis.Y) == 1, "Cage base misses bar feet");
                    if (horizontal != cage[0] || depth != cage[1]) {
                        BlockPos lid = transform(new BlockPos(horizontal, 7, depth), fixture.origin, fixture.rotation);
                        require(level.getBlockState(lid).getCollisionShape(level, lid).min(Direction.Axis.Y) == 0, "Cage lid floats above bars");
                        BlockPos wall = transform(new BlockPos(horizontal, 6, depth), fixture.origin, fixture.rotation);
                        require(level.getBlockState(wall).is(Blocks.IRON_BARS), "Missing cage walls");
                    }
                }
            }
        }
        for (int horizontal = 0; horizontal < 13; horizontal++) {
            for (int depth = 0; depth < 40; depth++) {
                for (int vertical : new int[]{0, 10}) {
                    BlockPos position = transform(new BlockPos(horizontal, vertical, depth), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Incomplete floor foundation or roof");
                }
            }
        }
        Set<BlockPos> route = new HashSet<>();
        for (int horizontal = 4; horizontal <= 8; horizontal++) {
            for (int depth = 0; depth < 40; depth++) {
                BlockPos feet = transform(new BlockPos(horizontal, 2, depth), fixture.origin, fixture.rotation);
                for (int height = 0; height < 3; height++) {
                    require(clear(level, feet.above(height)), "Obstructed three-high straight route");
                }
                BlockPos floor = feet.below();
                VoxelShape shape = level.getBlockState(floor).getCollisionShape(level, floor);
                require(!shape.isEmpty() && shape.max(Direction.Axis.Y) == 1.0
                        && shape.min(Direction.Axis.X) == 0 && shape.max(Direction.Axis.X) == 1
                        && shape.min(Direction.Axis.Z) == 0 && shape.max(Direction.Axis.Z) == 1, "Unsafe level floor/grate");
                if (depth == 0 || depth == 39) {
                    require(Block.isShapeFullBlock(shape), "Endpoint threshold must be a full block");
                    require(clear(level, feet.above(3)) && clear(level, feet.above(4)), "Endpoint five-high aperture obstructed");
                }
                route.add(feet);
            }
        }
        BlockPos entrance = transform(new BlockPos(6, 2, 0), fixture.origin, fixture.rotation);
        BlockPos exit = transform(new BlockPos(6, 2, 39), fixture.origin, fixture.rotation);
        require(flood(route, entrance).contains(exit) && flood(route, exit).contains(entrance), "Bidirectional route failure");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary spread/falling block");
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1)).inflate(1)).isEmpty(), "Unexpected entities/drops");
        return Map.of("rotation", fixture.rotation.name(), "records", fixture.expected.size(), "surviving_blocks", supported,
                "route_feet_cells", route.size(), "lanterns", lanterns, "chains", chains, "gauge_buttons", buttons,
                "boundary_air_cells", boundary, "both_ports_clear", true, "roof_cells", 520);
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return block.getCollisionShape(level, position).isEmpty() && block.getFluidState().isEmpty() && !FORBIDDEN.contains(block.getBlock());
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
                if (route.contains(candidate) && visited.add(candidate)) {
                    pending.add(candidate);
                }
            }
        }
        require(visited.size() == 200, "Partially unreachable straight-route floor");
        return visited;
    }
}
