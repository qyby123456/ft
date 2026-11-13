package net.farmingtales.m09qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftm09qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("lava", "water", "fire", "soul_fire", "magma_block", "campfire", "soul_campfire", "cactus", "cobweb", "powder_snow", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftm09qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/forge.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Single-palette Java 1.20.1 required");
        ListTag dimensions = data.getList("size", Tag.TAG_INT);
        require(dimensions.getInt(0) == 15 && dimensions.getInt(1) == 12 && dimensions.getInt(2) == 41, "Wrong dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) entry;
            ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/non-vanilla block " + identifier);
            require(!FORBIDDEN.contains(identifier.getPath()), "Forbidden block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String name : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(name);
                require(property != null && property.getValue(properties.getString(name)).isPresent(), "Invalid block-state property " + identifier + " " + name);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getBlock() == block && !state.hasBlockEntity(), "Unexpected fallback or block entity");
            require(state.getFluidState().isEmpty(), "Unexpected fluid");
            palette.add(state);
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag position = record.getList("pos", Tag.TAG_INT);
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 15 && relative.getY() >= 0 && relative.getY() < 12 && relative.getZ() >= 0 && relative.getZ() < 41, "Out of bounds");
            require(!expected.containsKey(relative), "Duplicate coordinate");
            require(!record.contains("nbt"), "Unexpected payload");
            expected.put(relative, palette.get(record.getInt("state")));
        }
        require(expected.size() == 7380, "Missing explicit air or solids");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return block.isAir() && block.getFluidState().isEmpty() && block.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2)) && floor.getFluidState().isEmpty() && floor.isFaceSturdy(level, below, Direction.UP);
    }

    private static AABB bounds(Fixture fixture) {
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(14, 11, 40));
        return new AABB(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
    }

    private static CompoundTag capture(ServerLevel level, Fixture fixture, CompoundTag source, Map<BlockPos, BlockState> expected) {
        Rotation inverse = switch (fixture.rotation()) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> fixture.rotation();
        };
        CompoundTag result = source.copy();
        ListTag palette = new ListTag();
        ListTag records = new ListTag();
        Map<BlockState, Integer> indices = new LinkedHashMap<>();
        for (BlockPos relative : expected.keySet()) {
            BlockState state = level.getBlockState(fixture.world(relative)).rotate(inverse);
            if (!indices.containsKey(state)) {
                indices.put(state, palette.size());
                palette.add(NbtUtils.writeBlockState(state));
            }
            CompoundTag record = new CompoundTag();
            ListTag position = new ListTag();
            position.add(IntTag.valueOf(relative.getX()));
            position.add(IntTag.valueOf(relative.getY()));
            position.add(IntTag.valueOf(relative.getZ()));
            record.put("pos", position);
            record.putInt("state", indices.get(state));
            records.add(record);
        }
        result.put("palette", palette);
        result.put("blocks", records);
        return result;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject blueprint) {
        int candles = 0;
        int buttons = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            BlockState wanted = entry.getValue().rotate(fixture.rotation());
            require(actual.equals(wanted), "State drift " + fixture.rotation() + " " + entry.getKey() + " actual=" + actual + " expected=" + wanted);
            require(actual.canSurvive(level, position), "Unsupported block " + position + " " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected placed fluid");
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.CANDLE)) candles++;
            if (actual.is(Blocks.POLISHED_BLACKSTONE_BUTTON)) buttons++;
        }
        for (JsonElement entry : blueprint.getAsJsonArray("required_feet")) {
            require(walkable(level, fixture.world(coordinate(entry.getAsJsonArray()))), "Unsafe path " + entry);
        }
        for (int posX = 0; posX < 15; posX++) {
            for (int posZ = 0; posZ < 41; posZ++) {
                for (int posY : List.of(0, 11)) {
                    BlockPos position = fixture.world(new BlockPos(posX, posY, posZ));
                    require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Incomplete roof/foundation");
                }
            }
        }
        for (int sideX : List.of(0, 14)) {
            for (int posY = 2; posY <= 10; posY++) {
                for (int posZ = 0; posZ < 41; posZ++) {
                    BlockPos position = fixture.world(new BlockPos(sideX, posY, posZ));
                    require(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Side branch/leak");
                }
            }
        }
        require(candles == 12 && buttons == 24, "Decorations lost");
        require(level.getEntities(null, bounds(fixture).inflate(1)).isEmpty(), "Spawned entity or dropped item");
        return Map.of("rotation", fixture.rotation().name(), "status", "PASS", "exact_block_records", expected.size(), "safe_feet", 123, "lit_candle_groups", candles, "supported_buttons", buttons, "entities", 0, "fluid_blocks", 0);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void corridorFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        try {
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
            Path sourcePath = root().resolve("room.nbt");
            String fingerprint = hash(sourcePath);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("qa/blueprint.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash differs");
            CompoundTag source = NbtIo.readCompressed(sourcePath.toFile());
            Map<BlockPos, BlockState> expected = audit(source);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), source);
            require(template.getSize().equals(new Vec3i(15, 12, 41)), "Native template size mismatch");
            require(audit(template.save(new CompoundTag())).equals(expected), "Native template roundtrip mismatch");
            result.put("source_nbt_sha256", fingerprint);
            result.put("minecraft", "1.20.1");
            result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
            result.put("java", System.getProperty("java.version"));
            result.put("isolated_run_directory", System.getProperty("user.dir"));
            result.put("fire_ticks_enabled", true);
            result.put("random_tick_speed", 0);
            List<Fixture> fixtures = new ArrayList<>();
            int rotationIndex = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(64 + rotationIndex * 128, 220, 64), rotation);
                for (int chunkX = (fixture.origin().getX() - 48) >> 4; chunkX <= (fixture.origin().getX() + 48) >> 4; chunkX++) {
                    for (int chunkZ = (fixture.origin().getZ() - 48) >> 4; chunkZ <= (fixture.origin().getZ() + 48) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                require(level.getEntities(null, bounds(fixture).inflate(2)).isEmpty(), "Fixture occupied before placement");
                for (int posX = -1; posX <= 15; posX++) {
                    for (int posY = -1; posY <= 12; posY++) {
                        for (int posZ = -1; posZ <= 41; posZ++) {
                            level.setBlock(fixture.world(new BlockPos(posX, posY, posZ)), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                        }
                    }
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placeInWorld failed");
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos aisle = fixture.world(new BlockPos(7, 2, 3));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level, aisle), "Blocked route negative probe missed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos candle = fixture.world(new BlockPos(3, 3, 4));
                BlockPos support = candle.below();
                BlockState supportState = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(candle).canSurvive(level, candle), "Unsupported candle negative probe missed");
                level.setBlock(support, supportState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                fixtures.add(fixture);
                rotationIndex++;
            }
            result.put("negative_blocked_path_probes", 4);
            result.put("negative_unsupported_candle_probes", 4);
            result.put("stable_snapshot_ticks", List.of(200, 240));
            Map<Rotation, CompoundTag> earlier = new LinkedHashMap<>();
            helper.runAfterDelay(200, () -> {
                for (Fixture fixture : fixtures) earlier.put(fixture.rotation(), capture(level, fixture, source, expected));
            });
            report(result);
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        rotations.add(verify(level, fixture, expected, blueprint));
                        CompoundTag captured = capture(level, fixture, source, expected);
                        require(captured.equals(earlier.get(fixture.rotation())), "Changing between ticks 200 and 240");
                        NbtIo.writeCompressed(captured, root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt").toFile());
                    }
                    require(hash(sourcePath).equals(fingerprint), "Source modified during QA");
                    result.put("status", "PASS");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", 4 * expected.size());
                    result.put("stabilization_ticks", 240);
                    result.put("limitations", List.of("Headless isolated Forge server evidence; no modpack client screenshot, multiplayer or FPS test.", "No FT core/catalog integration, whole-volume occupancy refusal, idempotence or deployment tested.", "New throwaway world only; no player save opened or modified."));
                    report(result);
                    System.out.println("M09_FORGE_QA_PASS rotations=4/4 records=" + 4 * expected.size() + " ticks=240 sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    result.put("status", "FAIL");
                    result.put("error", exception.toString());
                    try { report(result); } catch (Exception secondary) { exception.addSuppressed(secondary); }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            result.put("status", "FAIL");
            result.put("error", exception.toString());
            report(result);
            throw exception;
        }
    }
}
