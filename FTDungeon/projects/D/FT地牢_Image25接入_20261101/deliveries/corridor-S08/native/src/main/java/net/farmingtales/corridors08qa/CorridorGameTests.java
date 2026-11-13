package net.farmingtales.corridors08qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors08qa")
@PrefixGameTestTemplate(false)
public final class CorridorGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final int TICKS = 120;
    private static final Set<String> ALLOWED = Set.of("air", "stone_bricks", "chiseled_stone_bricks", "stone_brick_stairs", "packed_ice", "blue_ice", "snow_block", "smooth_quartz", "smooth_quartz_slab", "wall_torch", "light_blue_stained_glass_pane");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private CorridorGameTests() {}

    private static Path root() {
        return Path.of(System.getProperty("ftcorridors08qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.createDirectories(root().resolve("qa"));
        Files.writeString(root().resolve("qa/forge.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identifier = new ResourceLocation(encoded.getString("Name"));
        require(BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block " + identifier);
        require(identifier.getNamespace().equals("minecraft") && ALLOWED.contains(identifier.getPath()), "Disallowed block " + identifier);
        Block block = BuiltInRegistries.BLOCK.get(identifier);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + "/" + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag document) {
        require(document.getInt("DataVersion") == 3465, "DataVersion mismatch");
        require(!document.contains("palettes") && document.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Multiple palettes or entities");
        ListTag palette = document.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(decode(palette.getCompound(index)));
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = document.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            require(!entry.contains("nbt"), "Block entity NBT not permitted");
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 11 && position.getY() >= 0 && position.getY() < 10 && position.getZ() >= 0 && position.getZ() < 16, "Position outside volume");
            int stateIndex = entry.getInt("state");
            require(stateIndex >= 0 && stateIndex < states.size(), "Bad palette index");
            require(expected.put(position, states.get(stateIndex)) == null, "Duplicate record");
        }
        require(expected.size() == 1760, "Incomplete explicit volume");
        return expected;
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY() + 0.01, feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty();
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int supportChecks = 0;
        int torches = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState state = level.getBlockState(position);
            require(state.equals(entry.getValue().rotate(fixture.rotation())), "State changed " + fixture.rotation() + " " + entry.getKey() + ": " + state);
            require(state.canSurvive(level, position), "Unsupported block " + position);
            require(level.getFluidState(position).isEmpty(), "Unexpected fluid " + position);
            require(level.getBlockEntity(position) == null, "Unexpected block entity " + position);
            supportChecks++;
            if (state.is(Blocks.WALL_TORCH)) torches++;
        }
        int aisleChecks = 0;
        for (int across = 4; across <= 6; across++) {
            for (int depth = 0; depth < 16; depth++) {
                BlockPos feet = fixture.world(new BlockPos(across, 2, depth));
                require(walkable(level, feet), "Blocked straight lane " + feet);
                require(level.getBlockState(fixture.world(new BlockPos(across, 4, depth))).isAir(), "Missing three-high clearance");
                aisleChecks++;
            }
        }
        require(torches == 8, "Torch count changed");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_exact", expected.size());
        result.put("survival_checks", supportChecks);
        result.put("walkable_aisle_cells", aisleChecks);
        result.put("torches_supported", torches);
        result.put("fluids", 0);
        result.put("block_entities", 0);
        result.put("status", "PASS");
        if (capture) {
            BlockPos first = fixture.world(BlockPos.ZERO);
            BlockPos last = fixture.world(new BlockPos(10, 9, 15));
            BlockPos minimum = new BlockPos(Math.min(first.getX(), last.getX()), Math.min(first.getY(), last.getY()), Math.min(first.getZ(), last.getZ()));
            Vec3i size = new Vec3i(Math.abs(first.getX() - last.getX()) + 1, 10, Math.abs(first.getZ() - last.getZ()) + 1);
            StructureTemplate saved = new StructureTemplate();
            saved.fillFromWorld(level, minimum, size, false, null);
            CompoundTag encoded = saved.save(new CompoundTag());
            ListTag palette = encoded.getList("palette", Tag.TAG_COMPOUND);
            Map<BlockPos, BlockState> recovered = new LinkedHashMap<>();
            ListTag records = encoded.getList("blocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < records.size(); index++) {
                CompoundTag entry = records.getCompound(index);
                ListTag position = entry.getList("pos", Tag.TAG_INT);
                recovered.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), decode(palette.getCompound(entry.getInt("state"))));
            }
            require(recovered.size() == expected.size(), "Capture lost cells");
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(recovered.get(fixture.world(entry.getKey()).subtract(minimum))), "Capture mismatch");
            Path output = root().resolve("qa/captures/" + fixture.rotation().name().toLowerCase() + ".nbt");
            Files.createDirectories(output.getParent());
            NbtIo.writeCompressed(encoded, output.toFile());
            result.put("world_capture", root().relativize(output).toString());
            result.put("capture_sha256", hash(output));
            result.put("capture_matches_source", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 360)
    public static void placeFourRotationsAndStabilize(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("isolated_gametest_server", helper.getLevel().getServer() instanceof GameTestServer);
        try {
            require(helper.getLevel().getServer() instanceof GameTestServer, "Not an isolated GameTest server");
            String fingerprint = hash(root().resolve("room.nbt"));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Metadata hash mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag document = NbtIo.readCompressed(root().resolve("room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(document);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), document);
            require(template.getSize().equals(new Vec3i(11, 10, 16)), "Template size mismatch");
            require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == expected.size(), "Native roundtrip lost records");
            report.put("decode_registry_properties_roundtrip", "PASS");
            ServerLevel level = helper.getLevel();
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(32 + index * 48, 20, 32)), rotation);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos support = fixture.world(new BlockPos(2, 5, 5));
                BlockPos torch = fixture.world(new BlockPos(3, 5, 5));
                BlockState savedSupport = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(torch).canSurvive(level, torch), "Missing-support negative control failed");
                level.setBlock(support, savedSupport, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(5, 2, 7));
                level.setBlock(aisle, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Blocked-path negative control failed");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_missing_support_probes", 4);
            report.put("negative_blocked_path_probes", 4);
            report.put("stabilization_ticks", TICKS);
            writeReport(report);
            helper.runAfterDelay(TICKS, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, true));
                    require(hash(root().resolve("room.nbt")).equals(fingerprint), "Source NBT changed during test");
                    report.put("rotations", rotations);
                    report.put("records_checked_after_ticks", expected.size() * 4);
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Isolated Forge GameTest, not full modpack client/multiplayer/FPS verification.", "No FT catalog slot, production placement, deployment or player save modified."));
                    writeReport(report);
                    System.out.println("CORRIDOR_S08_FORGE_PASS rotations=4/4 records=7040 ticks=" + TICKS + " source=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try { writeReport(report); } catch (Exception secondary) { exception.addSuppressed(secondary); }
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            report.put("status", "FAIL");
            report.put("error", exception.toString());
            writeReport(report);
            throw exception;
        }
    }
}
