package net.farmingtales.s17qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

@GameTestHolder("fts17qa")
@PrefixGameTestTemplate(false)
public final class S17PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(15, 13, 16);
    private static final int RECORDS = 3120;
    private static final int FLAGS = Block.UPDATE_ALL;
    private static final Set<String> FORBIDDEN = Set.of("water", "lava", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "tnt");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        BlockPos minimum() {
            BlockPos first = world(BlockPos.ZERO);
            BlockPos last = world(new BlockPos(14, 12, 15));
            return new BlockPos(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()));
        }

        Vec3i dimensions() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(16, 13, 15) : SIZE;
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("s17qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path source) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
    }

    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/forge.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && data.contains("palette", Tag.TAG_LIST) && !data.contains("palettes"), "Not 1.20.1 single-palette NBT");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        ListTag paletteData = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < paletteData.size(); index++) {
            CompoundTag entry = paletteData.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
            require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid native property " + identity + " " + key);
            }
            BlockState decoded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(decoded.getBlock() == block && !decoded.hasBlockEntity() && decoded.getFluidState().isEmpty(), "Unsafe block entity or fluid");
            palette.add(decoded);
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        require(records.size() == RECORDS, "Missing explicit volume");
        for (int index = 0; index < records.size(); index++) {
            CompoundTag entry = records.getCompound(index);
            ListTag values = entry.getList("pos", Tag.TAG_INT);
            require(values.size() == 3 && !entry.contains("nbt"), "Malformed record or unexpected payload");
            BlockPos position = new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
            require(position.getX() >= 0 && position.getX() < 15 && position.getY() >= 0 && position.getY() < 13 && position.getZ() >= 0 && position.getZ() < 16, "Out-of-bounds position");
            int state = entry.getInt("state");
            require(state >= 0 && state < palette.size(), "Invalid palette index");
            require(expected.put(position, palette.get(state)) == null, "Duplicate coordinate");
        }
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()) && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return clear(level, feet) && clear(level, feet.above()) && clear(level, feet.above(2)) && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP) && level.getFluidState(floor).isEmpty();
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + ": " + actual + " expected " + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, position), "Unsupported block " + entry.getKey());
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
        }
        int pathProbes = 0;
        for (int positionZ = 0; positionZ < 16; positionZ++) {
            for (int positionX = 6; positionX <= 8; positionX++) {
                require(walkable(level, fixture.world(new BlockPos(positionX, 1, positionZ))), "Straight route blocked");
                pathProbes++;
            }
        }
        for (int portalZ : new int[]{0, 15}) {
            for (int positionX = 4; positionX <= 10; positionX++) {
                for (int positionY = 1; positionY <= 6; positionY++) require(clear(level, fixture.world(new BlockPos(positionX, positionY, portalZ))), "Portal narrowed");
            }
        }
        require(walkable(level, fixture.world(new BlockPos(7, 1, 3))), "Interior unsafe");
        BlockPos minimum = fixture.minimum();
        Vec3i dimensions = fixture.dimensions();
        AABB bounds = new AABB(minimum, minimum.offset(dimensions));
        require(level.getEntities(null, bounds).isEmpty(), "Unexpected entities");
        int outsideProbes = 0;
        for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), minimum.offset(dimensions).offset(1, 1, 1))) {
            if (!bounds.contains(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5)) {
                require(level.getFluidState(position).isEmpty() && !level.getBlockState(position).is(Blocks.FIRE), "Escaped fluid/fire");
                outsideProbes++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("walkway_foot_probes", pathProbes);
        result.put("outside_fluid_fire_probes", outsideProbes);
        result.put("state_survival_collision_entities", "PASS");
        if (capture) {
            StructureTemplate captured = new StructureTemplate();
            captured.fillFromWorld(level, minimum, dimensions, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = captured.save(new CompoundTag());
            require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Capture record loss");
            Path target = root().resolve("qa/placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, target.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(target.toFile()));
            require(reloaded.getSize().equals(dimensions), "Capture reload size mismatch");
            result.put("world_capture", root().relativize(target).toString());
            result.put("world_capture_sha256", hash(target));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 900)
    public static void placement(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        result.put("scope", "Standalone Forge 47.4.10 / Minecraft 1.20.1 GameTestServer only; no production save or FT core");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing a non-GameTest server");
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source fingerprint mismatch");
            CompoundTag encoded = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(encoded);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(template.getSize().equals(SIZE) && template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Native roundtrip mismatch");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(64 + index * 48, 32, 64)), rotation);
                BlockPos minimum = fixture.minimum();
                for (int chunkX = (minimum.getX() - 3) >> 4; chunkX <= (minimum.getX() + 19) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 3) >> 4; chunkZ <= (minimum.getZ() + 19) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-2, -2, -2), minimum.offset(fixture.dimensions()).offset(2, 2, 2))) level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, false);
                BlockPos probe = fixture.world(new BlockPos(7, 1, 2));
                level.setBlock(probe, Blocks.OBSIDIAN.defaultBlockState(), FLAGS);
                require(!walkable(level, probe), "Blocked-route negative control missed");
                level.setBlock(probe, Blocks.AIR.defaultBlockState(), FLAGS);
                BlockPos chain = fixture.world(new BlockPos(4, 8, 3));
                BlockState saved = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos lantern = fixture.world(new BlockPos(4, 7, 3));
                require(!level.getBlockState(lantern).canSurvive(level, lantern), "Missing-chain negative control missed");
                level.setBlock(chain, saved, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                fixtures.add(fixture);
                index++;
            }
            result.put("structure_sha256", fingerprint);
            result.put("registry_property_template_roundtrip", "PASS");
            result.put("negative_blocked_route_controls", 4);
            result.put("negative_missing_chain_controls", 4);
            result.put("force_loaded_fixture_chunks", true);
            result.put("stabilization_ticks", 200);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected, true));
                    require(hash(source).equals(fingerprint), "Source changed during test");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", RECORDS * 4);
                    result.put("status", "PASS");
                    result.put("limitations", List.of("Not a full modpack client, multiplayer, FPS or gameplay test.", "No FT catalog integration, occupied-space rejection or generation idempotence tested."));
                    report(result);
                    System.out.println("CORRIDOR_S17_FORGE_PASS rotations=4 records=12480 ticks=200 source=" + fingerprint);
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
