package net.farmingtales.zonereceiver;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.ClipContext;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonereceiver")
@GameTestHolder("ftzonereceiver")
@PrefixGameTestTemplate(false)
public final class ReceiverTests {
    private static final Rotation[] ROTATIONS = Rotation.values();
    private static final String[] IDS = {"zone-A05", "zone-A01", "zone-C02", "zone-C05", "zone-M01", "zone-H03", "zone-H02", "zone-R04", "zone-M03", "zone-A04", "zone-N02", "zone-P03", "zone-N01", "zone-N04", "zone-A02", "zone-R03", "zone-C03", "zone-R02", "zone-N03", "zone-P04"};
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT);
    private record Fixture(JsonObject spec, Path root, String hash, ServerLevel level, Rotation rotation,
                           BlockPos origin, BlockPos size, BlockPos minimum, BlockPos maximum,
                           Map<BlockPos, BlockState> states, Map<BlockPos, CompoundTag> entities) {}

    public ReceiverTests() {
        try {
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(Path.of(System.getProperty("ftreceiver.root")).resolve("native-tests.xml").toFile()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos position(JsonArray coordinates) {
        return new BlockPos(coordinates.get(0).getAsInt(), coordinates.get(1).getAsInt(), coordinates.get(2).getAsInt());
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static BlockPos world(BlockPos local, Fixture fixture) {
        return transform(local, fixture.origin, fixture.rotation);
    }

    private static void run(GameTestHelper helper, int roomIndex, int rotationIndex) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Fresh isolated GameTestServer required");
        Path batch = Path.of(System.getProperty("ftreceiver.root"));
        Path root = batch.resolve(IDS[roomIndex]);
        JsonObject spec = JsonParser.parseString(Files.readString(root.resolve("spec.json"))).getAsJsonObject();
        Path source = root.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(hash.equals(spec.get("sha256").getAsString()), "Frozen NBT changed");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected single-palette Minecraft 1.20.1 structure");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Preplaced entities forbidden");
        List<BlockState> palette = new ArrayList<>();
        ListTag rawPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int paletteIndex = 0; paletteIndex < rawPalette.size(); paletteIndex++) {
            CompoundTag entry = rawPalette.getCompound(paletteIndex);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unregistered palette entry");
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String name : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(name);
                require(property != null && property.getValue(properties.getString(name)).isPresent(), "Invalid explicit block property");
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(state.getBlock()), "Forbidden source block");
            palette.add(state);
        }
        BlockPos size = position(spec.getAsJsonArray("size"));
        Rotation rotation = ROTATIONS[rotationIndex];
        BlockPos origin = new BlockPos(1024 + roomIndex * 1024 + rotationIndex * 160, 240, 1024);
        BlockPos corner = transform(size.offset(-1, -1, -1), origin, rotation);
        BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
        BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + size.getY() - 1, Math.max(origin.getZ(), corner.getZ()));
        for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
            for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
        }
        for (BlockPos point : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
            require(level.isEmptyBlock(point) && level.getFluidState(point).isEmpty(), "Occupied test volume");
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new LinkedHashMap<>();
        ListTag blocks = data.getList("blocks", Tag.TAG_COMPOUND);
        require(blocks.size() == spec.get("records").getAsInt(), "Record count changed");
        for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
            CompoundTag entry = blocks.getCompound(blockIndex);
            ListTag coordinates = entry.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            BlockPos target = transform(local, origin, rotation);
            require(expected.put(target, palette.get(entry.getInt("state")).rotate(rotation)) == null, "Duplicate position");
            if (entry.contains("nbt")) blockEntities.put(target, entry.getCompound("nbt"));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(size), "Template size changed");
        NbtIo.writeCompressed(template.save(new CompoundTag()), root.resolve("native-roundtrip-" + rotation.name().toLowerCase() + ".nbt").toFile());
        require(template.placeInWorld(level, origin, origin,
                new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false),
                level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        Fixture fixture = new Fixture(spec, root, hash, level, rotation, origin, size, minimum, maximum, expected, blockEntities);
        Map<String, Object> initial = inspect(fixture);
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos point = entry.getKey();
            BlockState state = level.getBlockState(point);
            BlockState updated = Block.updateFromNeighbourShapes(state, level, point);
            if (!updated.equals(state)) level.setBlock(point, updated, Block.UPDATE_ALL);
            level.updateNeighborsAt(point, state.getBlock());
            if (!state.getFluidState().isEmpty()) level.scheduleTick(point, state.getFluidState().getType(), 1);
        }
        System.out.println("RECEIVER_INITIAL_PASS " + IDS[roomIndex] + " " + rotation);
        helper.runAfterDelay(400, () -> {
            try {
                Map<String, Object> settled = inspect(fixture);
                StructureTemplate capture = new StructureTemplate();
                capture.fillFromWorld(level, minimum, maximum.subtract(minimum).offset(1, 1, 1), false, null);
                NbtIo.writeCompressed(capture.save(new CompoundTag()), root.resolve("native-placed-" + rotation.name().toLowerCase() + ".nbt").toFile());
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("status", "PASS");
                report.put("source_nbt_sha256", hash);
                report.put("requested_rotation", rotation.name());
                report.put("stabilization_ticks", 400);
                report.put("rotations", List.of(Map.of("rotation", rotation.name(), "status", "PASS")));
                report.put("initial", initial);
                report.put("settled", settled);
                report.put("random_tick_speed", 0);
                report.put("fluid_ticks_forced", true);
                report.put("player_saves_accessed", false);
                Files.writeString(root.resolve("forge-" + rotation.name().toLowerCase() + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
                System.out.println("RECEIVER_STABLE_PASS " + IDS[roomIndex] + " " + rotation + " ticks=400");
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("Receiver stability failed: " + IDS[roomIndex] + " " + rotation, exception);
            }
        });
    }

    private static Map<String, Object> inspect(Fixture fixture) {
        ServerLevel level = fixture.level;
        int supported = 0;
        int water = 0;
        int rails = 0;
        int emptySlots = 0;
        List<BlockPos> containers = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : fixture.states.entrySet()) {
            BlockPos point = entry.getKey();
            BlockState actual = level.getBlockState(point);
            require(actual.equals(entry.getValue()), "State drift at " + point + ": " + entry.getValue() + " -> " + actual);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, point), "Unsupported native block " + point + " " + actual);
                supported++;
            }
            if (!actual.getFluidState().isEmpty()) {
                require(actual.getFluidState().isSource(), "Unexpected flowing fluid");
                water++;
            }
            if (actual.is(Blocks.RAIL)) rails++;
            CompoundTag sourceEntity = fixture.entities.get(point);
            if (sourceEntity == null) require(level.getBlockEntity(point) == null, "Unexpected block entity");
            else {
                require(level.getBlockEntity(point) != null, "Missing block entity");
                CompoundTag saved = level.getBlockEntity(point).saveWithFullMetadata();
                require(saved.getString("id").equals(sourceEntity.getString("id")), "Block entity identity changed");
                require(!saved.contains("LootTable") && !saved.contains("LootTableSeed") && saved.getString("Lock").isEmpty(), "Loot or lock found");
                if (!(level.getBlockEntity(point) instanceof Container)) continue;
                Container inventory = (Container) level.getBlockEntity(point);
                require(inventory.isEmpty(), "Nonempty container");
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) require(inventory.getItem(slot).isEmpty(), "Unexpected item slot");
                require(level.getBlockState(point.below()).isFaceSturdy(level, point.below(), Direction.UP), "Floating container");
                if (actual.is(Blocks.CHEST)) require(clear(level, point.above()) && actual.getMenuProvider(level, point) != null, "Obstructed chest lid");
                emptySlots += inventory.getContainerSize();
                containers.add(point);
            }
        }
        require(water == fixture.spec.get("water_cells").getAsInt(), "Water count mismatch");
        require(rails == fixture.spec.get("rails").getAsInt(), "Rail count mismatch");
        require(containers.size() == fixture.spec.get("containers").getAsInt(), "Container count mismatch");
        int foot = fixture.spec.get("feet_y").getAsInt();
        Set<BlockPos> reachable = flood(fixture, world(position(fixture.spec.getAsJsonArray("entrance")), fixture));
        if (!fixture.spec.get("specialized_route").getAsBoolean()) {
            require(reachable.contains(world(position(fixture.spec.getAsJsonArray("interior")), fixture)), "Safe point unreachable");
            for (var item : fixture.spec.getAsJsonArray("ports")) require(reachable.contains(world(position(item.getAsJsonArray()), fixture)), "Port unreachable");
        }
        for (var item : fixture.spec.getAsJsonArray("lane")) {
            BlockPos feet = world(position(item.getAsJsonArray()), fixture);
            require(reachable.contains(feet) && clear(level, feet.above(2)), "Required three-high path obstructed");
        }
        for (BlockPos container : containers) {
            boolean accessible = false;
            for (BlockPos feet : reachable) {
                Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + 1.62, feet.getZ() + 0.5);
                Vec3 target = Vec3.atCenterOf(container);
                if (eye.distanceToSqr(target) > 4.5 * 4.5) continue;
                var hit = level.clip(new ClipContext(eye, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
                if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(container)) { accessible = true; break; }
            }
            require(accessible, "Container is beyond unobstructed player reach: " + container);
        }
        for (int across = 0; across < fixture.size.getX(); across++) {
            for (int depth = 0; depth < fixture.size.getZ(); depth++) {
                boolean covered = false;
                for (var exception : fixture.spec.getAsJsonArray("roof_exceptions")) {
                    JsonArray coordinates = exception.getAsJsonArray();
                    if (coordinates.get(0).getAsInt() == across && coordinates.get(1).getAsInt() == depth) covered = true;
                }
                for (int height = foot + 3; height < fixture.size.getY(); height++) {
                    if (!level.getBlockState(world(new BlockPos(across, height, depth), fixture)).isAir()) { covered = true; break; }
                }
                require(covered, "Missing roof column " + across + "," + depth);
            }
        }
        for (BlockPos point : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (fixture.states.containsKey(point)) continue;
            require(level.isEmptyBlock(point) && level.getFluidState(point).isEmpty(), "Boundary leak or spilled state " + point);
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected dropped item/entity");
        return Map.of("exact_records", fixture.states.size(), "native_support_checks", supported, "water_cells", water,
                "rails", rails, "empty_containers", containers.size(), "empty_slots", emptySlots,
                "reachable_feet", reachable.size(), "required_lane_feet", fixture.spec.getAsJsonArray("lane").size(),
                "roof_columns", fixture.size.getX() * fixture.size.getZ(), "entities_and_drops", 0);
    }

    private static boolean clear(ServerLevel level, BlockPos point) {
        BlockState state = level.getBlockState(point);
        return !state.is(Blocks.COBWEB) && state.getFluidState().isEmpty() && state.getCollisionShape(level, point).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP);
    }

    private static Set<BlockPos> flood(Fixture fixture, BlockPos start) {
        require(walkable(fixture.level, start), "Unsafe entrance");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < fixture.minimum.getX() || next.getX() > fixture.maximum.getX() || next.getZ() < fixture.minimum.getZ() || next.getZ() > fixture.maximum.getZ()) continue;
                if (!visited.contains(next) && walkable(fixture.level, next)) { visited.add(next); queue.add(next); }
            }
        }
        return visited;
    }

    @GameTest(template = "empty", batch = "receiver", timeoutTicks = 1200)
    public static void receiverNONE (GameTestHelper helper) throws Exception { run(helper, 16, 0); }

    @GameTest(template = "empty", batch = "receiver", timeoutTicks = 1200)
    public static void receiverCLOCKWISE90 (GameTestHelper helper) throws Exception { run(helper, 16, 1); }

    @GameTest(template = "empty", batch = "receiver", timeoutTicks = 1200)
    public static void receiverCLOCKWISE180 (GameTestHelper helper) throws Exception { run(helper, 16, 2); }

    @GameTest(template = "empty", batch = "receiver", timeoutTicks = 1200)
    public static void receiverCOUNTERCLOCKWISE90 (GameTestHelper helper) throws Exception { run(helper, 16, 3); }
}
