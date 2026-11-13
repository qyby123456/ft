package net.farmingtales.corridorm03qa;

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
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridorm03qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Expected(BlockState state, CompoundTag nbt) {}

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        BlockPos minimum() {
            BlockPos corner = world(new BlockPos(12, 11, 40));
            return new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
        }

        Vec3i size() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(41, 12, 13) : new Vec3i(13, 12, 41);
        }
    }

    private CorridorGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Path root() {
        return Path.of(System.getProperty("ftcorridorm03qa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static BlockPos coordinate(JsonArray value) {
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }

    private static void report(Map<String, Object> value) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(value), StandardCharsets.UTF_8);
    }

    private static Map<BlockPos, Expected> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Not a 1.20.1 single-palette template");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 13 && size.getInt(1) == 12 && size.getInt(2) == 41, "Wrong dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            CompoundTag encoded = encodedPalette.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown vanilla block: " + identifier);
            require(!FORBIDDEN.contains(identifier.getPath()), "Forbidden block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getFluidState().isEmpty(), "Fluid or waterlogging found");
            palette.add(state);
        }
        Map<BlockPos, Expected> result = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        int banners = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag entry = blocks.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Invalid coordinate");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 13 && relative.getY() >= 0 && relative.getY() < 12 && relative.getZ() >= 0 && relative.getZ() < 41, "Out-of-bounds record");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            BlockState state = palette.get(stateIndex);
            require(state.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch at " + relative);
            CompoundTag nbt = entry.contains("nbt") ? entry.getCompound("nbt").copy() : null;
            if (nbt != null) {
                require(state.is(Blocks.RED_WALL_BANNER) && nbt.getString("id").equals("minecraft:banner"), "Only patterned banners are allowed block entities");
                require(!nbt.contains("Items") && !nbt.contains("LootTable") && !nbt.contains("Command"), "Reward or executable payload");
                require(nbt.getList("Patterns", Tag.TAG_COMPOUND).size() == 3, "Banner sigil missing");
                BlockEntity entity = BlockEntity.loadStatic(relative, state, nbt.copy());
                require(entity != null && entity.getType().isValid(state), "Invalid banner entity");
                banners++;
            }
            require(result.put(relative, new Expected(state, nbt)) == null, "Duplicate coordinate");
        }
        require(result.size() == 6396 && banners == 10, "Incomplete explicit volume or banners");
        return result;
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(feet).getFluidState().isEmpty()
                && level.getBlockState(feet.above()).getFluidState().isEmpty();
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, Expected> expected, JsonObject metadata, boolean capture) throws Exception {
        int banners = 0;
        int candles = 0;
        for (Map.Entry<BlockPos, Expected> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().state().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " actual=" + actual);
            require(actual.getFluidState().isEmpty() && actual.canSurvive(level, position), "Unsupported or fluid block " + position);
            if (entry.getValue().nbt() != null) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Lost banner entity");
                require(entity.saveWithoutMetadata().getList("Patterns", Tag.TAG_COMPOUND).equals(entry.getValue().nbt().getList("Patterns", Tag.TAG_COMPOUND)), "Banner patterns changed");
                banners++;
            }
            if (actual.is(Blocks.CANDLE)) candles++;
        }
        for (JsonElement value : metadata.getAsJsonArray("supported_decor")) {
            JsonObject support = value.getAsJsonObject();
            BlockPos decoration = fixture.world(coordinate(support.getAsJsonArray("position")));
            BlockPos anchor = fixture.world(coordinate(support.getAsJsonArray("anchor")));
            Direction face = Direction.fromDelta(decoration.getX() - anchor.getX(), decoration.getY() - anchor.getY(), decoration.getZ() - anchor.getZ());
            require(face != null && level.getBlockState(anchor).isFaceSturdy(level, anchor, face), "Declared support face absent");
        }
        int lanes = 0;
        for (int depth = 0; depth < 41; depth++) {
            for (int across = 4; across <= 8; across++) {
                require(walkable(level, fixture.world(new BlockPos(across, 1, depth))), "Blocked straight aisle");
                for (int elevation = 1; elevation <= 3; elevation++) require(level.getBlockState(fixture.world(new BlockPos(across, elevation, depth))).isAir(), "Headroom less than three");
                lanes++;
            }
            for (int across = 0; across < 13; across++) require(!level.getBlockState(fixture.world(new BlockPos(across, 11, depth))).isAir(), "Roof breach");
        }
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        AABB volume = new AABB(minimum, minimum.offset(size));
        require(level.getEntities(null, volume).isEmpty(), "Unexpected drops or entities after placement");
        require(banners == 10 && candles == 20, "Lost themed decoration");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("north_entrance_facing", fixture.rotation().rotate(Direction.NORTH).getName());
        result.put("records_checked", expected.size());
        result.put("clear_lane_positions", lanes);
        result.put("banners", banners);
        result.put("lit_candle_clusters", candles);
        result.put("survival_support_state_collision_roof", "PASS");
        if (capture) {
            StructureTemplate snapshot = new StructureTemplate();
            snapshot.fillFromWorld(level, minimum, size, false, null);
            CompoundTag encoded = snapshot.save(new CompoundTag());
            encoded.putInt("DataVersion", 3465);
            require(encoded.getList("blocks", Tag.TAG_COMPOUND).size() == 6396, "Capture dropped records");
            Path output = root().resolve("qa/world-" + fixture.rotation().name().toLowerCase() + ".nbt");
            NbtIo.writeCompressed(encoded, output.toFile());
            result.put("capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_size", List.of(size.getX(), size.getY(), size.getZ()));
        }
        return result;
    }

    private static void negativeProbes(ServerLevel level, Fixture fixture) {
        for (BlockPos[] probe : List.of(new BlockPos[] {new BlockPos(2, 3, 3), new BlockPos(2, 2, 3)}, new BlockPos[] {new BlockPos(2, 6, 4), new BlockPos(1, 6, 4)})) {
            BlockPos decoration = fixture.world(probe[0]);
            BlockPos anchor = fixture.world(probe[1]);
            BlockState saved = level.getBlockState(anchor);
            level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
            require(!level.getBlockState(decoration).canSurvive(level, decoration), "Missing-support negative control was not detected");
            level.setBlock(anchor, saved, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        }
        BlockPos aisle = fixture.world(new BlockPos(6, 1, 4));
        level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        require(!walkable(level, aisle), "Blocked-aisle negative control was not detected");
        level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
    }

    @GameTest(template = "empty", timeoutTicks = 500)
    public static void fourRotationsAndTwoHundredTicks(GameTestHelper helper) throws Exception {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("status", "RUNNING");
        output.put("minecraft", "1.20.1");
        output.put("forge", "47.4.10");
        output.put("java", System.getProperty("java.version"));
        output.put("scope", "Independent headless Forge GameTestServer; no player save or FT core integration");
        report(output);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing to write a non-GameTest server");
            require(level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().startsWith(root().resolve("forge/run")), "Test save escaped isolated root");
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Structure hash mismatch");
            output.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, Expected> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(new Vec3i(13, 12, 41)), "Native template size mismatch");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 6396, "Native roundtrip lost records");
            output.put("decode_registry_properties_entities_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(80 + index * 128, 25, 80)), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i size = fixture.size();
                for (int chunkX = (minimum.getX() - 2) >> 4; chunkX <= (minimum.getX() + size.getX() + 2) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 2) >> 4; chunkZ <= (minimum.getZ() + size.getZ() + 2) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                level.getEntities(null, new AABB(minimum, minimum.offset(size))).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                immediate.add(verify(level, fixture, expected, metadata, false));
                negativeProbes(level, fixture);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            output.put("immediate_rotations", immediate);
            output.put("negative_missing_support_probes", 8);
            output.put("negative_blocked_aisle_probes", 4);
            output.put("stabilization_ticks", 200);
            output.put("chunks_forced_to_tick", true);
            report(output);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> stabilized = new ArrayList<>();
                    for (Fixture fixture : fixtures) stabilized.add(verify(level, fixture, expected, metadata, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source changed during verification");
                    output.put("rotations", stabilized);
                    output.put("records_checked_after_ticks", 4 * expected.size());
                    output.put("status", "PASS");
                    output.put("limitations", List.of("Not a full modpack/client screenshot or multiplayer test.", "No production slot, roadway integration, saved-data idempotence, loot or combat tested."));
                    report(output);
                    System.out.println("CORRIDOR_M03_FORGE_PASS: rotations=4/4, records=25584, ticks=200, sha256=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    output.put("status", "FAIL");
                    output.put("error", exception.toString());
                    try { report(output); } catch (Exception secondary) { exception.addSuppressed(secondary); }
                    helper.fail(exception.toString());
                }
            });
        } catch (Exception exception) {
            output.put("status", "FAIL");
            output.put("error", exception.toString());
            report(output);
            throw exception;
        }
    }
}
