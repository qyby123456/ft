package net.farmingtales.s19qa;

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

@Mod("fts19qa")
@GameTestHolder("fts19qa")
@PrefixGameTestTemplate(false)
public final class S19Verification {
    private static final BlockPos SIZE = new BlockPos(15, 11, 16);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID, Blocks.JIGSAW,
            Blocks.SPAWNER, Blocks.CHEST, Blocks.BARREL, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK, Blocks.COBWEB,
            Blocks.POWDER_SNOW, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);

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

    private static BlockPos world(Fixture fixture, int posX, int posY, int posZ) {
        return transform(new BlockPos(posX, posY, posZ), fixture.origin, fixture.rotation);
    }

    @GameTest(template = "empty", batch = "s19_four_rotations", timeoutTicks = 1200)
    public static void nativePlacement(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer is permitted");
        Path root = Path.of(System.getProperty("fts19.project")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-S19"), "Wrong workspace");
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        require(metadata.get("sha256").getAsString().equals(hash), "Source fingerprint mismatch");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected single-palette Java 1.20.1");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + entry);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(state.getBlock()) && state.getFluidState().isEmpty(), "Forbidden block " + state);
            states.add(state);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Wrong template dimensions");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == 2640, "Missing explicit volume records");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 2640, "Native roundtrip lost records");
        NbtIo.writeCompressed(template.save(new CompoundTag()), root.resolve("qa/native-roundtrip.nbt").toFile());
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 80, 240, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag position = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
                require(local.getX() >= 0 && local.getX() < 15 && local.getY() >= 0 && local.getY() < 11
                        && local.getZ() >= 0 && local.getZ() < 16, "Out-of-bounds record");
                require(!record.contains("nbt"), "Unexpected inventory or block entity");
                require(expected.put(transform(local, origin, rotation), states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Occupied fixture; refuse placement " + position + " " + level.getBlockState(position));
            }
            require(level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).isEmpty(), "Occupied entity fixture");
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> initial = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            System.out.println("S19_NATIVE_INITIAL_PASS " + fixture.rotation);
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture));
                    StructureTemplate snapshot = new StructureTemplate();
                    snapshot.fillFromWorld(level, fixture.minimum, fixture.maximum.subtract(fixture.minimum).offset(1, 1, 1), false, null);
                    NbtIo.writeCompressed(snapshot.save(new CompoundTag()), root.resolve("qa/native-placed-" + fixture.rotation.toString().toLowerCase() + ".nbt").toFile());
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("nbt_sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "isolated GameTestServer / actual ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("ticks", 400);
                result.put("initial", initial);
                result.put("after_400_ticks", settled);
                result.put("explicit_neighbor_updates", true);
                result.put("fire_tick_enabled", true);
                result.put("random_tick_speed", 3);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("S19_NATIVE_GAME_TEST_PASS rotations=4/4 records=10560 ticks=400 sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("S19 post-tick verification failed", exception);
            }
        });
    }

    private static boolean full(ServerLevel level, BlockPos position) {
        return Block.isShapeFullBlock(level.getBlockState(position).getCollisionShape(level, position));
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !FORBIDDEN.contains(state.getBlock()) && state.getFluidState().isEmpty() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return clear(level, feet) && clear(level, feet.above()) && full(level, feet.below())
                && !FORBIDDEN.contains(level.getBlockState(feet.below()).getBlock());
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int rods = 0;
        int chains = 0;
        int bars = 0;
        int supported = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed " + position + ": " + entry.getValue() + " -> " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            require(level.getBlockEntity(position) == null, "Unexpected container or block entity");
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failure " + position);
                supported++;
            }
            if (actual.is(Blocks.END_ROD)) {
                Direction facing = actual.getValue(BlockStateProperties.FACING);
                BlockPos anchor = position.relative(facing.getOpposite());
                while (level.getBlockState(anchor).is(Blocks.END_ROD)) {
                    anchor = anchor.relative(facing.getOpposite());
                }
                require(full(level, anchor), "Unanchored end rod " + position);
                rods++;
            }
            if (actual.is(Blocks.CHAIN)) {
                require(full(level, position.above()), "Unanchored cage chain");
                require(level.getBlockState(position.below()).is(Blocks.IRON_BLOCK), "Cage cap disconnected");
                chains++;
            }
            if (actual.is(Blocks.IRON_BARS)) {
                bars++;
            }
        }
        require(rods == 20 && chains == 4 && bars == 32, "Missing design landmarks");
        for (int posX = 0; posX < 15; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                require(full(level, world(fixture, posX, 10, posZ)), "Incomplete roof");
                require(full(level, world(fixture, posX, 0, posZ)), "Incomplete base");
            }
        }
        for (int posZ = 0; posZ < 16; posZ++) {
            for (int posX = 6; posX <= 8; posX++) {
                BlockPos feet = world(fixture, posX, 2, posZ);
                require(walkable(level, feet) && clear(level, feet.above(2)), "Blocked 3x3 through-route");
            }
        }
        BlockPos start = world(fixture, 7, 2, 0);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = current.relative(direction);
                if (fixture.expected.containsKey(candidate) && walkable(level, candidate) && visited.add(candidate)) {
                    queue.add(candidate);
                }
            }
        }
        for (int targetZ : new int[]{3, 12, 15}) {
            require(visited.contains(world(fixture, 7, 2, targetZ)), "Disconnected safety point or exit");
        }
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak or falling block " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1))).isEmpty(), "Unexpected entity or dropped item");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("anchored_end_rods", rods);
        report.put("anchored_cages", chains);
        report.put("connected_iron_bars", bars);
        report.put("roof_full_cells", 240);
        report.put("reachable_feet", visited.size());
        report.put("through_route_3x3", true);
        report.put("boundary_air_cells", boundary);
        report.put("containers_and_entities", 0);
        return report;
    }
}
