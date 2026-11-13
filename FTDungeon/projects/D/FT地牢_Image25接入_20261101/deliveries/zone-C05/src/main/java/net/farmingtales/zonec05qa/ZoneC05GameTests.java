package net.farmingtales.zonec05qa;

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

@Mod("ftzonec05qa")
@GameTestHolder("ftzonec05qa")
@PrefixGameTestTemplate(false)
public final class ZoneC05GameTests {
    private static final BlockPos SIZE = new BlockPos(25, 14, 25);
    private static final int VOLUME = 8750;
    private static final int SETTLE_TICKS = 320;
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.TNT, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE,
            Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE, Blocks.BUDDING_AMETHYST);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    @GameTest(template = "empty", batch = "zone_c05", timeoutTicks = 1200)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is permitted");
        Path root = Path.of(System.getProperty("ftzonec05qa.root")).toRealPath();
        require(root.getFileName().toString().equals("zone-C05"), "Unexpected project root");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("id").getAsString().equals("zone-C05") && metadata.get("sha256").getAsString().equals(hash), "Identity/hash mismatch");
        CompoundTag document = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        require(document.getInt("DataVersion") == 3465 && !document.contains("palettes"), "Wrong single-palette format");
        require(document.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag stateTags = document.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < stateTags.size(); index++) {
            CompoundTag entry = stateTags.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Invalid vanilla block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block property: " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()) && !loaded.hasBlockEntity(), "Forbidden block or inventory");
            require(loaded.getFluidState().isEmpty() || loaded.is(Blocks.WATER), "Unexpected fluid");
            palette.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), document);
        require(template.getSize().equals(SIZE), "Incorrect native structure dimensions");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == VOLUME, "NBT roundtrip loses records");
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
                require(local.getX() >= 0 && local.getX() < 25 && local.getY() >= 0 && local.getY() < 14 && local.getZ() >= 0 && local.getZ() < 25, "Out-of-bounds record");
                require(record.getInt("state") >= 0 && record.getInt("state") < palette.size() && !record.contains("nbt"), "Invalid palette state or block entity");
                require(expected.put(transform(local, origin, rotation), palette.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), 300, Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), 313, Math.max(origin.getZ(), corner.getZ()));
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied isolated fixture: " + position);
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Native placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
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
                    capture.fillFromWorld(level, fixture.minimum, SIZE, false, null);
                    NbtIo.writeCompressed(capture.save(new CompoundTag()), root.resolve("qa/native-placed-" + fixture.rotation.name().toLowerCase() + ".nbt").toFile());
                }
                String rechecked = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
                require(hash.equals(rechecked), "Template changed during native QA");
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "zone-C05");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("rotations", 4);
                result.put("ticks", level.getGameTime() - start);
                result.put("random_tick_speed", 3);
                result.put("initial", initial);
                result.put("after_stability", settled);
                result.put("native_registry_and_property_validation", true);
                result.put("final_template_sha256_rechecked", true);
                result.put("isolated_world_only", true);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("ZONE_C05_NATIVE_PASS sha256=" + hash + " rotations=4 records=35000 ticks=" + (level.getGameTime() - start));
                helper.succeed();
            } catch (Throwable failure) {
                failure.printStackTrace();
                helper.fail(failure.toString());
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int lanterns = 0;
        int chains = 0;
        int crystals = 0;
        int water = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State drift at " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native survival failed at " + position);
                supported++;
            }
            if (actual.is(Blocks.WATER)) {
                require(actual.getFluidState().isSource(), "Flowing pool water");
                for (Direction direction : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
                    BlockPos neighbor = position.relative(direction);
                    BlockState boundary = level.getBlockState(neighbor);
                    require(boundary.is(Blocks.WATER) || Block.isShapeFullBlock(boundary.getCollisionShape(level, neighbor)), "Uncontained basin");
                }
                water++;
            } else require(actual.getFluidState().isEmpty(), "Unexpected waterlogging or fluid");
            if (actual.is(Blocks.LANTERN)) {
                if (actual.getValue(BlockStateProperties.HANGING)) {
                    BlockPos anchor = position.above();
                    require(level.getBlockState(anchor).is(Blocks.CHAIN), "Lantern misses chain");
                    while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                    require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain misses ceiling anchor");
                } else require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Standing lantern lacks support");
                lanterns++;
            }
            if (actual.is(Blocks.CHAIN)) chains++;
            if (actual.is(Blocks.AMETHYST_CLUSTER) || actual.is(Blocks.LARGE_AMETHYST_BUD)) {
                Direction facing = actual.getValue(BlockStateProperties.FACING);
                BlockPos attachment = position.relative(facing.getOpposite());
                require(level.getBlockState(attachment).isFaceSturdy(level, attachment, facing), "Crystal attachment gap");
                crystals++;
            }
        }
        require(lanterns == 6 && chains == 8 && crystals == 29 && water == 33, "Theme feature count changed");
        for (int horizontal = 0; horizontal < 25; horizontal++) {
            for (int depth = 0; depth < 25; depth++) {
                for (int vertical : new int[]{0, 13}) {
                    BlockPos position = transform(new BlockPos(horizontal, vertical, depth), fixture.origin, fixture.rotation);
                    require(Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position)), "Incomplete foundation or roof");
                }
            }
        }
        Set<BlockPos> route = new HashSet<>();
        for (int horizontal = 0; horizontal < 25; horizontal++) {
            for (int depth = 0; depth < 25; depth++) {
                if (!(horizontal >= 10 && horizontal <= 14 && depth <= 14 || horizontal >= 10 && depth >= 10 && depth <= 14)) continue;
                BlockPos feet = transform(new BlockPos(horizontal, 2, depth), fixture.origin, fixture.rotation);
                for (int height = 0; height < 3; height++) require(clear(level, feet.above(height)), "Obstructed three-high route");
                BlockPos floor = feet.below();
                require(Block.isShapeFullBlock(level.getBlockState(floor).getCollisionShape(level, floor)), "Unsafe route floor");
                require(level.noCollision(null, new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8)), "Player box collides with route");
                if (depth == 0 || horizontal == 24) {
                    require(clear(level, feet.above(3)) && clear(level, feet.above(4)), "Five-high portal obstruction");
                }
                route.add(feet);
            }
        }
        BlockPos entrance = transform(new BlockPos(12, 2, 0), fixture.origin, fixture.rotation);
        BlockPos exit = transform(new BlockPos(24, 2, 12), fixture.origin, fixture.rotation);
        BlockPos interior = transform(new BlockPos(12, 2, 4), fixture.origin, fixture.rotation);
        require(flood(route, entrance).contains(exit) && flood(route, exit).contains(interior), "Bidirectional corner route failed");
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary spread or falling block");
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1)).inflate(1)).isEmpty(), "Unexpected entities or drops");
        return Map.of("rotation", fixture.rotation.name(), "records", fixture.expected.size(), "surviving_blocks", supported,
                "route_feet_cells", route.size(), "crystals", crystals, "water_sources", water, "lanterns", lanterns,
                "chains", chains, "boundary_air_cells", boundary, "both_ports_clear", true);
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
                if (route.contains(candidate) && visited.add(candidate)) pending.add(candidate);
            }
        }
        require(visited.size() == 125, "Partially disconnected corner route");
        return visited;
    }
}
