package net.farmingtales.corridors10qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors10qa")
@PrefixGameTestTemplate(false)
public final class CorridorTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "spawner", "structure_void", "water", "lava", "fire", "soul_fire", "magma_block", "campfire", "soul_campfire", "cobweb", "powder_snow", "gravel", "sand", "chest", "barrel");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
    }

    private CorridorTests() {}

    private static Path root() {
        return Path.of(System.getProperty("ftcorridors10qa.root")).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockPos position(ListTag values) {
        return new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Not Java 1.20.1 single palette");
        require(position(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(13, 11, 16)), "Wrong structure size");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template contains entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            CompoundTag encoded = encodedPalette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && !FORBIDDEN.contains(identity.getPath()), "Unknown or forbidden block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state property " + identity + ":" + key);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getFluidState().isEmpty(), "Unexpected fluid " + state);
            palette.add(state);
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            BlockPos relative = position(record.getList("pos", Tag.TAG_INT));
            require(relative.getX() >= 0 && relative.getX() < 13 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 16, "Out of bounds " + relative);
            require(!record.contains("nbt"), "Unexpected block entity payload");
            int stateIndex = record.getInt("state");
            require(stateIndex >= 0 && stateIndex < palette.size(), "Invalid palette index");
            require(expected.put(relative, palette.get(stateIndex)) == null, "Duplicate block coordinate");
        }
        require(expected.size() == 2288, "Explicit volume incomplete");
        return expected;
    }

    private static AABB bounds(Fixture fixture) {
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(12, 10, 15));
        return new AABB(Math.min(first.getX(), last.getX()), 160, Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, 171, Math.max(first.getZ(), last.getZ()) + 1);
    }

    private static void checkPath(ServerLevel level, Fixture fixture) {
        for (int posZ = 0; posZ < 16; posZ++) {
            for (int posX = 5; posX <= 7; posX++) {
                BlockPos floor = fixture.world(new BlockPos(posX, 1, posZ));
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Missing full aisle floor " + floor);
                BlockPos foot = fixture.world(new BlockPos(posX, 2, posZ));
                require(level.noCollision(new AABB(foot.getX() + .2, foot.getY() + .001, foot.getZ() + .2, foot.getX() + .8, foot.getY() + 1.8, foot.getZ() + .8)), "Player collision " + foot);
                for (int posY = 2; posY <= 4; posY++) {
                    BlockPos clear = fixture.world(new BlockPos(posX, posY, posZ));
                    require(level.getBlockState(clear).isAir(), "Three-high lane obstructed " + clear);
                }
            }
        }
    }

    private static void checkMounts(ServerLevel level, Fixture fixture) {
        for (int posZ : new int[] {2, 7, 12}) {
            BlockPos lamp = fixture.world(new BlockPos(6, 8, posZ));
            BlockPos mount = fixture.world(new BlockPos(6, 9, posZ));
            require(level.getBlockState(lamp).is(Blocks.END_ROD) && level.getBlockState(mount).is(Blocks.SEA_LANTERN), "Hanging light missing its ceiling mount");
            require(level.getBlockState(lamp).getLightEmission() > 0 && level.getBlockState(mount).getLightEmission() > 0, "Light emitter not active");
            for (int posX : new int[] {2, 10}) {
                BlockPos gauge = fixture.world(new BlockPos(posX, 5, posZ));
                BlockPos button = fixture.world(new BlockPos(posX == 2 ? 3 : 9, 5, posZ));
                require(level.getBlockState(gauge).is(Blocks.TARGET) && level.getBlockState(button).canSurvive(level, button), "Gauge mount invalid");
            }
        }
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos world = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(world);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " actual=" + actual + " expected=" + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, world), "Unsupported block " + world);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(world) == null, "Unexpected fluid or block entity " + world);
        }
        checkPath(level, fixture);
        checkMounts(level, fixture);
        require(level.getEntities(null, bounds(fixture)).isEmpty(), "Entities or dropped attachments appeared");
        return Map.of("rotation", fixture.rotation().name(), "status", "PASS", "records_checked", expected.size(), "floor_cells", 48, "three_high_air_cells", 144, "mounted_lights", 3, "source_entities", 0, "state_drift", 0);
    }

    private static int probes(ServerLevel level, Fixture fixture) {
        int count = 0;
        for (BlockPos relative : List.of(new BlockPos(6, 2, 8), new BlockPos(6, 1, 8), new BlockPos(6, 9, 7))) {
            BlockPos world = fixture.world(relative);
            BlockState saved = level.getBlockState(world);
            level.setBlock(world, relative.getY() == 2 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), FLAGS);
            boolean rejected = false;
            try {
                if (relative.getY() == 9) checkMounts(level, fixture);
                else checkPath(level, fixture);
            } catch (IllegalStateException expected) {
                rejected = true;
            } finally {
                level.setBlock(world, saved, FLAGS);
            }
            require(rejected, "Negative probe was not detected " + relative);
            count++;
        }
        return count;
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void straightCopperCorridor(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("test_world_only", true);
        result.put("stabilization_ticks", 240);
        try {
            Path source = root().resolve("room.nbt");
            String sourceHash = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(metadata.get("sha256").getAsString().equals(sourceHash), "Metadata fingerprint differs");
            require(hash(Path.of(metadata.get("reference").getAsString())).equals(metadata.get("reference_sha256").getAsString()), "Reference fingerprint differs");
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(true, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            int negativeProbes = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(2048 + fixtures.size() * 64, 160, 2048), rotation);
                AABB box = bounds(fixture);
                for (int chunkX = ((int) box.minX) >> 4; chunkX <= (((int) box.maxX) - 1) >> 4; chunkX++) {
                    for (int chunkZ = ((int) box.minZ) >> 4; chunkZ <= (((int) box.maxZ) - 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_SUPPRESS_DROPS);
                level.getEntities(null, box).forEach(entity -> entity.discard());
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected);
                negativeProbes += probes(level, fixture);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos world = fixture.world(relative);
                    level.updateNeighborsAt(world, level.getBlockState(world).getBlock());
                }
                fixtures.add(fixture);
                System.out.println("CORRIDOR_S10_PLACED " + rotation);
            }
            result.put("room_sha256", sourceHash);
            result.put("negative_probes", negativeProbes);
            result.put("negative_probe_types", List.of("blocked lane", "missing floor", "missing light mount"));
            result.put("placement_policy", "Fresh isolated sky fixtures; native placeInWorld with known-shape initial placement, then normal neighbor updates on every record; forced ticking chunks, random ticks=3, block drops enabled.");
            report(result);
            helper.runAfterDelay(240, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected));
                    require(hash(source).equals(sourceHash), "Source changed during test");
                    result.put("status", "PASS");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", expected.size() * rotations.size());
                    result.put("limitations", List.of("Not a full-modpack client or multiplayer playtest.", "Does not test FT hall links, occupied-volume rejection, idempotence or deployment."));
                    report(result);
                    System.out.println("CORRIDOR_S10_NATIVE_PASS rotations=4 ticks=240 records=9152 negative_probes=12 sha256=" + sourceHash);
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
