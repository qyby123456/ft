package net.farmingtales.ironshopqa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftironshopqa")
@PrefixGameTestTemplate(false)
public final class IronShopGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(17, 11, 17);
    private static final int RECORDS = 3179;
    private static final int FLAGS = Block.UPDATE_ALL;
    private static final Set<String> FORBIDDEN = Set.of("water", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light", "tnt", "respawn_anchor");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        BlockPos minimum() {
            BlockPos first = world(BlockPos.ZERO);
            BlockPos opposite = world(new BlockPos(16, 0, 16));
            return new BlockPos(Math.min(first.getX(), opposite.getX()), first.getY(), Math.min(first.getZ(), opposite.getZ()));
        }
    }

    private IronShopGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftironshopqa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static BlockPos coordinate(JsonArray values) {
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static boolean emptyPayload(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            Tag value = data.get(key);
            if (Set.of("LootTable", "LootTableSeed", "Book", "RecordItem", "Item", "Owner", "SkullOwner", "Command").contains(key)) {
                return false;
            }
            if (key.equals("Items") && (!(value instanceof ListTag list) || !list.isEmpty())) {
                return false;
            }
            if (value instanceof CompoundTag child && !emptyPayload(child)) {
                return false;
            }
        }
        return true;
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identity + " " + key);
        }
        BlockState decoded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(decoded.is(Blocks.LAVA) || decoded.getFluidState().isEmpty(), "Unexpected fluid or waterlogging");
        return decoded;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && data.contains("palette") && !data.contains("palettes"), "Invalid template version or palette");
        require(coordinate(metadata.getAsJsonArray("size")).equals(new BlockPos(SIZE)), "Wrong metadata dimensions");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && size.getInt(0) == 17 && size.getInt(1) == 11 && size.getInt(2) == 17, "Wrong template dimensions");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entities");
        ListTag paletteData = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < paletteData.size(); index++) {
            palette.add(decode(paletteData.getCompound(index)));
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag entries = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag position = entry.getList("pos", Tag.TAG_INT);
            require(position.size() == 3, "Malformed coordinate");
            BlockPos relative = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            require(relative.getX() >= 0 && relative.getX() < 17 && relative.getY() >= 0 && relative.getY() < 11 && relative.getZ() >= 0 && relative.getZ() < 17, "Out of bounds record");
            int paletteIndex = entry.getInt("state");
            require(paletteIndex >= 0 && paletteIndex < palette.size(), "Bad palette index");
            BlockState block = palette.get(paletteIndex);
            require(expected.put(relative, block) == null, "Duplicate record");
            require(block.hasBlockEntity() == entry.contains("nbt", Tag.TAG_COMPOUND), "Wrong block-entity presence at " + relative);
            if (block.hasBlockEntity()) {
                CompoundTag payload = entry.getCompound("nbt").copy();
                require(emptyPayload(payload), "Stock, loot or commands in payload");
                payload.putInt("x", relative.getX());
                payload.putInt("y", relative.getY());
                payload.putInt("z", relative.getZ());
                BlockEntity entity = BlockEntity.loadStatic(relative, block, payload);
                require(entity != null && entity.getType().isValid(block), "Invalid native block entity " + relative);
                require(!(entity instanceof Container container) || container.isEmpty(), "Stocked native inventory");
            }
        }
        require(expected.size() == RECORDS, "Full explicit volume required");
        return expected;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return block.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath()) && block.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(floor).getFluidState().isEmpty() && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP) && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(level.getBlockState(floor).getBlock()).getPath());
    }

    private static boolean sealedLava(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        for (JsonElement value : blueprint.getAsJsonArray("lava_cells")) {
            JsonObject cell = value.getAsJsonObject();
            BlockPos source = fixture.world(coordinate(cell.getAsJsonArray("source")));
            if (!level.getBlockState(source).is(Blocks.LAVA) || !level.getFluidState(source).isSource()) {
                return false;
            }
            for (JsonElement neighbor : cell.getAsJsonArray("enclosure")) {
                BlockPos position = fixture.world(coordinate(neighbor.getAsJsonArray()));
                BlockState guard = level.getBlockState(position);
                if (!(guard.is(Blocks.OBSIDIAN) || guard.is(Blocks.GLASS)) || !guard.getFluidState().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Native survival failed " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Missing/wrong block entity");
                require(emptyPayload(entity.saveWithFullMetadata()), "Unexpected inventory or loot");
                require(!(entity instanceof Container container) || container.isEmpty(), "Container stocked");
                blockEntities++;
            }
        }
        for (String category : List.of("entrance_to_counter_aisle", "counter_clearance")) {
            for (JsonElement value : blueprint.getAsJsonArray(category)) {
                require(clear(level, fixture.world(coordinate(value.getAsJsonArray()))), "Collision in " + category);
            }
        }
        for (JsonElement value : blueprint.getAsJsonArray("lamp_supports")) {
            JsonObject lamp = value.getAsJsonObject();
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("lantern")))).is(Blocks.LANTERN), "Lantern missing");
            for (JsonElement chain : lamp.getAsJsonArray("chains")) {
                require(level.getBlockState(fixture.world(coordinate(chain.getAsJsonArray()))).is(Blocks.CHAIN), "Chain support missing");
            }
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("beam")))).is(Blocks.POLISHED_DEEPSLATE), "Beam missing");
            require(level.getBlockState(fixture.world(coordinate(lamp.getAsJsonArray("roof")))).is(Blocks.DEEPSLATE_TILES), "Roof above beam missing");
        }
        require(sealedLava(level, fixture, blueprint), "Lava containment failed");
        int fireproofProbes = 0;
        for (JsonElement value : blueprint.getAsJsonArray("lava_cells")) {
            BlockPos source = fixture.world(coordinate(value.getAsJsonObject().getAsJsonArray("source")));
            for (BlockPos probe : BlockPos.betweenClosed(source.offset(-4, -4, -4), source.offset(4, 4, 4))) {
                BlockState block = level.getBlockState(probe);
                for (Direction face : Direction.values()) {
                    require(block.getFlammability(level, probe, face) == 0 && block.getFireSpreadSpeed(level, probe, face) == 0, "Flammable material near lava " + probe);
                }
                fireproofProbes++;
            }
        }
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entrance = coordinate(metadata.getAsJsonArray("entrance"));
        require(walkable(level, fixture.world(entrance)), "Entrance not walkable");
        reached.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos following = current.relative(direction);
                if (following.getX() >= 0 && following.getX() < 17 && following.getZ() >= 0 && following.getZ() < 17 && !reached.contains(following) && walkable(level, fixture.world(following))) {
                    reached.add(following);
                    pending.addLast(following);
                }
            }
        }
        require(reached.contains(coordinate(metadata.getAsJsonArray("interior"))), "Safe point unreachable");
        for (JsonElement value : blueprint.getAsJsonArray("required_walkable")) {
            require(reached.contains(coordinate(value.getAsJsonArray())), "Counter loop or storage unreachable");
        }
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = fixture.world(entrance.offset(offset, 0, 0));
            require(walkable(level, feet) && clear(level, feet.above(2)), "Entry not 3x3 clear");
        }
        BlockPos minimum = fixture.minimum();
        int outsideProbes = 0;
        for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), minimum.offset(17, 11, 17))) {
            boolean inside = position.getX() >= minimum.getX() && position.getX() < minimum.getX() + 17 && position.getY() >= minimum.getY() && position.getY() < minimum.getY() + 11 && position.getZ() >= minimum.getZ() && position.getZ() < minimum.getZ() + 17;
            if (!inside) {
                require(level.getFluidState(position).isEmpty() && !level.getBlockState(position).is(Blocks.FIRE), "Fluid/fire escaped template");
                outsideProbes++;
            }
        }
        int entities = level.getEntities(null, new AABB(minimum, minimum.offset(17, 11, 17))).size();
        require(entities == 0, "Unexpected world entities");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("reachable_foot_positions", reached.size());
        result.put("fireproof_radius4_positions", fireproofProbes);
        result.put("outside_fluid_fire_probes", outsideProbes);
        result.put("unexpected_entities", entities);
        result.put("state_survival_collision_inventory_lava", "PASS");
        if (capture) {
            BlockPos canary = minimum.offset(-8, 1, -8);
            require(!level.getFluidState(canary.east()).isEmpty() && level.getBlockState(canary.east()).is(Blocks.LAVA), "Fluid-ticking canary did not spread; stability test was not active");
            result.put("separate_bounded_fluid_ticking_canary", "PASS");
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, SIZE, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            require(saved.getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Capture lost records");
            Path destination = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(saved, destination.toFile());
            StructureTemplate reloaded = new StructureTemplate();
            reloaded.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(destination.toFile()));
            require(reloaded.getSize().equals(SIZE), "Native capture reload dimensions");
            result.put("world_capture", root().relativize(destination).toString());
            result.put("world_capture_sha256", hash(destination));
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void roomPlacement(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Standalone headless Forge GameTestServer, not FT core or a player/client world");
        writeReport(report);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Only isolated GameTestServer may write fixtures");
            level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
            report.put("do_fire_tick", level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK));
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("out/room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source hash mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(root().resolve("out/room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(data, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            require(template.getSize().equals(SIZE) && template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == RECORDS, "Native template load/roundtrip failed");
            report.put("registry_properties_block_entities_template_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(64 + index * 48, 32, 64)), rotation);
                BlockPos minimum = fixture.minimum();
                for (int chunkX = (minimum.getX() - 12) >> 4; chunkX <= (minimum.getX() + 20) >> 4; chunkX++) {
                    for (int chunkZ = (minimum.getZ() - 12) >> 4; chunkZ <= (minimum.getZ() + 20) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-4, -4, -4), minimum.offset(20, 14, 20))) {
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                verify(level, fixture, expected, metadata, blueprint, false);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos position = fixture.world(relative);
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                }
                BlockPos chain = fixture.world(new BlockPos(7, 6, 6));
                BlockState savedChain = level.getBlockState(chain);
                level.setBlock(chain, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(fixture.world(new BlockPos(7, 5, 6))).canSurvive(level, fixture.world(new BlockPos(7, 5, 6))), "Missing-chain control not detected");
                level.setBlock(chain, savedChain, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos glass = fixture.world(new BlockPos(3, 2, 14));
                level.setBlock(glass, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                require(!sealedLava(level, fixture, blueprint), "Open-lava-guard control not detected");
                level.setBlock(glass, Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos aisle = fixture.world(new BlockPos(8, 1, 2));
                level.setBlock(aisle, Blocks.OBSIDIAN.defaultBlockState(), FLAGS);
                require(!walkable(level, aisle), "Blocked-aisle control not detected");
                level.setBlock(aisle, Blocks.AIR.defaultBlockState(), FLAGS);
                for (JsonElement value : blueprint.getAsJsonArray("lava_cells")) {
                    BlockPos lava = fixture.world(coordinate(value.getAsJsonObject().getAsJsonArray("source")));
                    level.scheduleTick(lava, level.getFluidState(lava).getType(), 1);
                }
                BlockPos canary = minimum.offset(-8, 1, -8);
                for (int offsetX = -2; offsetX <= 2; offsetX++) {
                    for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                        level.setBlock(canary.offset(offsetX, -1, offsetZ), Blocks.OBSIDIAN.defaultBlockState(), FLAGS);
                        boolean wall = Math.abs(offsetX) == 2 || Math.abs(offsetZ) == 2;
                        level.setBlock(canary.offset(offsetX, 0, offsetZ), wall ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState(), FLAGS);
                        level.setBlock(canary.offset(offsetX, 1, offsetZ), wall ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState(), FLAGS);
                    }
                }
                level.setBlock(canary, Blocks.LAVA.defaultBlockState(), FLAGS);
                level.scheduleTick(canary, level.getFluidState(canary).getType(), 1);
                fixtures.add(fixture);
                index++;
            }
            report.put("negative_missing_chain_controls", 4);
            report.put("negative_open_lava_guard_controls", 4);
            report.put("negative_blocked_aisle_controls", 4);
            report.put("explicit_scheduled_lava_ticks", 8);
            report.put("force_loaded_fixture_chunks", true);
            report.put("separate_bounded_fluid_ticking_canaries", 4);
            report.put("stabilization_ticks", 200);
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        results.add(verify(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("out/room.nbt")).equals(fingerprint), "Source changed during test");
                    report.put("rotations", results);
                    report.put("records_checked_after_ticks", results.size() * expected.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("No FT catalog ID allocation, deployment, hallway, idempotence or occupied-space rejection tested.", "No full modpack client, player screenshot, multiplayer, FPS or functional trading test."));
                    writeReport(report);
                    System.out.println("IRON_SHOP_NATIVE_PASS rotations=4 records=12716 ticks=200 lava_sources=8 source=" + fingerprint);
                    helper.succeed();
                } catch (Exception exception) {
                    report.put("status", "FAIL");
                    report.put("error", exception.toString());
                    try {
                        writeReport(report);
                    } catch (Exception secondary) {
                        exception.addSuppressed(secondary);
                    }
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
