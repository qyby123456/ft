package net.farmingtales.stairdqa;

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
import java.util.TreeSet;
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
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstairdqa")
@PrefixGameTestTemplate(false)
public final class StairGameTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Vec3i SIZE = new Vec3i(29, 23, 33);
    private static final int VOLUME = 22011;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("water", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "campfire", "soul_campfire", "wither_rose", "sweet_berry_bush", "cobweb", "spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "light");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }

        double[] worldPoint(double relativeX, double relativeZ) {
            return switch (rotation) {
                case NONE -> new double[] {origin.getX() + relativeX, origin.getZ() + relativeZ};
                case CLOCKWISE_90 -> new double[] {origin.getX() + 1 - relativeZ, origin.getZ() + relativeX};
                case CLOCKWISE_180 -> new double[] {origin.getX() + 1 - relativeX, origin.getZ() + 1 - relativeZ};
                case COUNTERCLOCKWISE_90 -> new double[] {origin.getX() + relativeZ, origin.getZ() + 1 - relativeX};
            };
        }

        BlockPos minimum() {
            int minimumX = Integer.MAX_VALUE;
            int minimumZ = Integer.MAX_VALUE;
            for (BlockPos corner : List.of(BlockPos.ZERO, new BlockPos(28, 0, 0), new BlockPos(0, 0, 32), new BlockPos(28, 0, 32))) {
                BlockPos position = world(corner);
                minimumX = Math.min(minimumX, position.getX());
                minimumZ = Math.min(minimumZ, position.getZ());
            }
            return new BlockPos(minimumX, origin.getY(), minimumZ);
        }

        Vec3i rotatedSize() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(33, 23, 29) : SIZE;
        }

        AABB bounds() {
            return new AABB(minimum().offset(-1, -1, -1), minimum().offset(rotatedSize()).offset(1, 1, 1));
        }
    }

    private StairGameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static Path root() {
        return Path.of(System.getProperty("ftstairdqa.root")).toAbsolutePath().normalize();
    }

    private static String hash(Path file) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static BlockPos coordinate(JsonArray values) {
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static void writeReport(Map<String, Object> report) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla block " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String name : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(name);
            require(property != null && property.getValue(properties.getString(name)).isPresent(), "Invalid property " + identity + " " + name);
        }
        BlockState result = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
        require(NbtUtils.writeBlockState(result).equals(encoded), "Properties not explicitly complete " + identity);
        return result;
    }

    private static Map<BlockPos, BlockState> audit(CompoundTag data, JsonObject metadata) {
        require(data.getInt("DataVersion") == 3465 && data.contains("palette") && !data.contains("palettes"), "Require Java 1.20.1 single palette");
        ListTag size = data.getList("size", Tag.TAG_INT);
        require(size.size() == 3 && new Vec3i(size.getInt(0), size.getInt(1), size.getInt(2)).equals(SIZE), "Wrong template dimensions");
        require(coordinate(metadata.getAsJsonArray("size")).equals(SIZE), "Metadata dimensions differ");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected template entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            palette.add(decode(encodedPalette.getCompound(index)));
        }
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag values = record.getList("pos", Tag.TAG_INT);
            require(values.size() == 3, "Invalid coordinate record");
            BlockPos position = new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
            require(position.getX() >= 0 && position.getX() < 29 && position.getY() >= 0 && position.getY() < 23 && position.getZ() >= 0 && position.getZ() < 33, "Record out of bounds");
            int paletteIndex = record.getInt("state");
            require(paletteIndex >= 0 && paletteIndex < palette.size(), "Invalid palette index");
            BlockState block = palette.get(paletteIndex);
            require(result.put(position, block) == null, "Duplicate coordinate");
            require(block.hasBlockEntity() == record.contains("nbt", Tag.TAG_COMPOUND), "Block entity payload mismatch " + position);
            if (block.hasBlockEntity()) {
                CompoundTag payload = record.getCompound("nbt");
                require(!payload.contains("LootTable") && !payload.contains("Items") && !payload.contains("Item"), "Loot or inventory present");
                BlockEntity entity = BlockEntity.loadStatic(position, block, payload.copy());
                require(entity != null && entity.getType().isValid(block), "Invalid native block entity");
            }
        }
        require(result.size() == VOLUME, "Require full explicit volume");
        return result;
    }

    private static boolean safeSolid(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return block.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath()) && block.isCollisionShapeFullBlock(level, position);
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState block = level.getBlockState(position);
        return block.getFluidState().isEmpty() && !FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath()) && block.getCollisionShape(level, position).isEmpty();
    }

    private static boolean enclosed(ServerLevel level, BlockPos start) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(start);
        visited.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (level.getFluidState(neighbor).is(FluidTags.LAVA)) {
                    if (visited.add(neighbor)) {
                        pending.addLast(neighbor);
                    }
                } else if (!safeSolid(level, neighbor)) {
                    return false;
                }
            }
            require(visited.size() <= 16, "Unexpected connected lava outside sealed channels");
        }
        return true;
    }

    private static double footHeight(ServerLevel level, double centerX, double centerZ, double previous) {
        TreeSet<Double> candidates = new TreeSet<>();
        for (int posX = (int) Math.floor(centerX - 0.3); posX <= (int) Math.floor(centerX + 0.3); posX++) {
            for (int posZ = (int) Math.floor(centerZ - 0.3); posZ <= (int) Math.floor(centerZ + 0.3); posZ++) {
                for (int posY = (int) Math.floor(previous - 1.6); posY <= (int) Math.ceil(previous + 0.6); posY++) {
                    BlockPos position = new BlockPos(posX, posY, posZ);
                    BlockState block = level.getBlockState(position);
                    if (!block.getFluidState().isEmpty() || FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath())) {
                        continue;
                    }
                    for (AABB shape : block.getCollisionShape(level, position).toAabbs()) {
                        AABB bounds = shape.move(posX, posY, posZ);
                        if (bounds.maxX > centerX - 0.3 + 0.00001 && bounds.minX < centerX + 0.3 - 0.00001 && bounds.maxZ > centerZ - 0.3 + 0.00001 && bounds.minZ < centerZ + 0.3 - 0.00001 && Math.abs(bounds.maxY - previous) <= 0.6001) {
                            candidates.add(bounds.maxY);
                        }
                    }
                }
            }
        }
        for (double height : candidates) {
            AABB body = new AABB(centerX - 0.299, height + 0.001, centerZ - 0.299, centerX + 0.299, height + 1.799, centerZ + 0.299);
            if (level.noCollision(body)) {
                for (BlockPos position : BlockPos.betweenClosed((int) Math.floor(body.minX), (int) Math.floor(body.minY), (int) Math.floor(body.minZ), (int) Math.floor(body.maxX), (int) Math.floor(body.maxY), (int) Math.floor(body.maxZ))) {
                    require(level.getFluidState(position).isEmpty(), "Route body touches liquid");
                    require(!FORBIDDEN.contains(BuiltInRegistries.BLOCK.getKey(level.getBlockState(position).getBlock()).getPath()), "Route body touches hazard");
                }
                return height;
            }
        }
        throw new IllegalStateException("No supported native 0.6x1.8 body at " + centerX + "," + previous + "," + centerZ);
    }

    private static Map<String, Object> walkRoute(ServerLevel level, Fixture fixture, List<double[]> waypoints, double startY, double endY) {
        double previous = fixture.origin().getY() + startY;
        double maximumStep = 0;
        int samples = 0;
        for (int segment = 0; segment < waypoints.size() - 1; segment++) {
            double[] start = waypoints.get(segment);
            double[] end = waypoints.get(segment + 1);
            int divisions = Math.max(1, (int) Math.ceil(Math.hypot(end[0] - start[0], end[1] - start[1]) * 4));
            for (int index = 0; index <= divisions; index++) {
                double fraction = (double) index / divisions;
                double[] point = fixture.worldPoint(start[0] + (end[0] - start[0]) * fraction, start[1] + (end[1] - start[1]) * fraction);
                double height = footHeight(level, point[0], point[1], previous);
                maximumStep = Math.max(maximumStep, Math.abs(height - previous));
                previous = height;
                samples++;
            }
        }
        require(Math.abs(previous - fixture.origin().getY() - endY) < 0.01, "Route did not reach declared floor elevation");
        return Map.of("samples", samples, "maximum_step", maximumStep, "end_y", previous - fixture.origin().getY());
    }

    private static Map<String, Object> walkBoth(ServerLevel level, Fixture fixture, List<double[]> waypoints, double startY, double endY) {
        Map<String, Object> forward = walkRoute(level, fixture, waypoints, startY, endY);
        List<double[]> reversed = new ArrayList<>(waypoints);
        java.util.Collections.reverse(reversed);
        return Map.of("forward", forward, "reverse", walkRoute(level, fixture, reversed, endY, startY));
    }

    private static Map<String, Object> routes(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (JsonElement entry : blueprint.getAsJsonArray("routes")) {
            JsonObject specification = entry.getAsJsonObject();
            List<double[]> waypoints = new ArrayList<>();
            for (JsonElement value : specification.getAsJsonArray("waypoints")) {
                JsonArray point = value.getAsJsonArray();
                waypoints.add(new double[] {point.get(0).getAsDouble(), point.get(1).getAsDouble()});
            }
            result.put(specification.get("id").getAsString(), walkBoth(level, fixture, waypoints, specification.get("start_y").getAsDouble(), specification.get("end_y").getAsDouble()));
        }
        for (int lane = 8; lane <= 16; lane++) {
            walkBoth(level, fixture, List.of(new double[] {lane + 0.5, 7.5}, new double[] {lane + 0.5, 15.5}), 7, 13);
        }
        for (int lane = 10; lane <= 16; lane++) {
            walkBoth(level, fixture, List.of(new double[] {lane + 0.5, 22.5}, new double[] {lane + 0.5, 28.5}), 13, 17);
        }
        for (int lane = 20; lane <= 24; lane++) {
            walkBoth(level, fixture, List.of(new double[] {lane + 0.5, 7.5}, new double[] {lane + 0.5, 15.5}), 7, 1);
        }
        return result;
    }

    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, JsonObject metadata, JsonObject blueprint, boolean capture) throws Exception {
        int blockEntities = 0;
        int lava = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " " + actual);
            require(actual.canSurvive(level, position), "Native support invalid " + entry.getKey());
            if (actual.hasBlockEntity()) {
                BlockEntity entity = level.getBlockEntity(position);
                require(entity != null && entity.getType().isValid(actual), "Native block entity invalid");
                CompoundTag payload = entity.saveWithFullMetadata();
                require(!payload.contains("Items") && !payload.contains("LootTable"), "Inventory or loot appeared");
                blockEntities++;
            }
            if (actual.is(Blocks.LAVA)) {
                require(actual.getFluidState().isSource(), "Lava source changed");
                lava++;
            }
        }
        Set<BlockPos> apertures = new HashSet<>();
        for (JsonElement value : metadata.getAsJsonArray("interfaces")) {
            JsonObject specification = value.getAsJsonObject();
            BlockPos feet = coordinate(specification.getAsJsonArray("feet"));
            for (int horizontal = -1; horizontal <= 1; horizontal++) {
                require(safeSolid(level, fixture.world(feet.offset(horizontal, -1, 0))), "Interface footing missing");
                for (int vertical = 0; vertical < 3; vertical++) {
                    BlockPos relative = feet.offset(horizontal, vertical, 0);
                    apertures.add(relative);
                    require(clear(level, fixture.world(relative)), "3x3 interface obstructed " + specification.get("id"));
                }
            }
        }
        for (int posX = 0; posX < 29; posX++) {
            for (int posZ = 0; posZ < 33; posZ++) {
                require(safeSolid(level, fixture.world(new BlockPos(posX, 22, posZ))), "Roof missing");
                require(safeSolid(level, fixture.world(new BlockPos(posX, 0, posZ))), "Foundation missing");
                if (posX == 0 || posX == 28 || posZ == 0 || posZ == 32) {
                    for (int posY = 1; posY < 22; posY++) {
                        BlockPos relative = new BlockPos(posX, posY, posZ);
                        require(apertures.contains(relative) || safeSolid(level, fixture.world(relative)), "Undeclared opening in outer shell " + relative);
                    }
                }
            }
        }
        for (String category : List.of("supports", "stair_supports")) {
            for (JsonElement value : blueprint.getAsJsonArray(category)) {
                JsonObject support = value.getAsJsonObject();
                BlockPos position = fixture.world(coordinate(support.getAsJsonArray("position")));
                BlockPos anchor = fixture.world(coordinate(support.getAsJsonArray("support")));
                require(!level.getBlockState(position).isAir() && !level.getBlockState(anchor).isAir(), "Declared support missing");
                if (category.equals("stair_supports")) {
                    require(safeSolid(level, anchor), "Stair tread has no full masonry support");
                }
            }
        }
        for (JsonElement value : blueprint.getAsJsonObject("features").getAsJsonArray("railings")) {
            require(safeSolid(level, fixture.world(coordinate(value.getAsJsonArray()))), "Guardrail missing");
        }
        for (JsonElement value : blueprint.getAsJsonArray("sealed_lava")) {
            require(enclosed(level, fixture.world(coordinate(value.getAsJsonArray()))), "Lava envelope breached");
        }
        require(lava == 16, "Lava count changed");
        int surroundingFluids = 0;
        for (int posX = -1; posX <= 29; posX++) {
            for (int posY = -1; posY <= 23; posY++) {
                for (int posZ = -1; posZ <= 33; posZ++) {
                    BlockPos relative = new BlockPos(posX, posY, posZ);
                    if (!level.getFluidState(fixture.world(relative)).isEmpty()) {
                        require(expected.containsKey(relative) && expected.get(relative).is(Blocks.LAVA), "Fluid leaked into undeclared coordinate " + relative);
                        surroundingFluids++;
                    }
                }
            }
        }
        require(surroundingFluids == 16, "Unexpected surrounding fluid");
        Map<String, Object> routeResults = routes(level, fixture, blueprint);
        for (JsonElement value : blueprint.getAsJsonArray("safe_points")) {
            BlockPos relative = coordinate(value.getAsJsonArray());
            double[] point = fixture.worldPoint(relative.getX() + 0.5, relative.getZ() + 0.5);
            require(Math.abs(footHeight(level, point[0], point[1], fixture.origin().getY() + relative.getY()) - fixture.origin().getY() - relative.getY()) < 0.01, "Safe point not supported and clear");
        }
        int entities = level.getEntities(null, fixture.bounds()).size();
        require(entities == 0, "Unexpected world entity or dropped block");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("records_checked", expected.size());
        result.put("block_entities_checked", blockEntities);
        result.put("state_drift", 0);
        result.put("survival_failures", 0);
        result.put("sealed_lava_sources", lava);
        result.put("undeclared_fluid_blocks", surroundingFluids - lava);
        result.put("unexpected_world_entities", entities);
        result.put("stair_lanes_bidirectionally_checked", 21);
        result.put("body_width_height", List.of(0.6, 1.8));
        result.put("sample_spacing_blocks", 0.25);
        result.put("routes", routeResults);
        result.put("interfaces_roof_shell_supports_rails_safe_points", "PASS");
        if (capture) {
            StructureTemplate snapshot = new StructureTemplate();
            snapshot.fillFromWorld(level, fixture.minimum(), fixture.rotatedSize(), false, Blocks.STRUCTURE_VOID);
            CompoundTag serialized = snapshot.save(new CompoundTag());
            Path path = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase(java.util.Locale.ROOT) + ".nbt");
            NbtIo.writeCompressed(serialized, path.toFile());
            result.put("capture", root().relativize(path).toString());
            result.put("capture_sha256", hash(path));
        }
        return result;
    }

    private static void negativeProbes(ServerLevel level, Fixture fixture, JsonObject blueprint) {
        JsonObject probes = blueprint.getAsJsonObject("negative_probes");
        BlockPos chain = fixture.world(coordinate(probes.getAsJsonArray("chain")));
        BlockState savedChain = level.getBlockState(chain);
        level.setBlock(chain, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos lamp = fixture.world(coordinate(probes.getAsJsonArray("lamp")));
        require(!level.getBlockState(lamp).canSurvive(level, lamp), "Missing-chain negative probe missed");
        level.setBlock(chain, savedChain, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos cover = fixture.world(coordinate(probes.getAsJsonArray("lava_cover")));
        BlockState savedCover = level.getBlockState(cover);
        level.setBlock(cover, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        require(!enclosed(level, fixture.world(coordinate(probes.getAsJsonArray("lava")))), "Open-lava negative probe missed");
        level.setBlock(cover, savedCover, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos entry = fixture.world(coordinate(probes.getAsJsonArray("blocked_entry")));
        level.setBlock(entry, Blocks.STONE.defaultBlockState(), FLAGS);
        require(!clear(level, entry), "Blocked-entry negative probe missed");
        level.setBlock(entry, Blocks.AIR.defaultBlockState(), FLAGS);
        BlockPos stair = fixture.world(coordinate(probes.getAsJsonArray("missing_stair")));
        BlockState savedStair = level.getBlockState(stair);
        level.setBlock(stair, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
        boolean rejected = false;
        try {
            walkRoute(level, fixture, List.of(new double[] {12.5, 7.5}, new double[] {12.5, 15.5}), 7, 13);
        } catch (IllegalStateException exception) {
            rejected = true;
        }
        require(rejected, "Missing-stair native collision negative probe missed");
        level.setBlock(stair, savedStair, FLAGS | Block.UPDATE_KNOWN_SHAPE);
        BlockPos roof = fixture.world(coordinate(probes.getAsJsonArray("roof")));
        BlockState savedRoof = level.getBlockState(roof);
        level.setBlock(roof, Blocks.AIR.defaultBlockState(), FLAGS);
        require(!safeSolid(level, roof), "Missing-roof negative probe missed");
        level.setBlock(roof, savedRoof, FLAGS);
    }

    @GameTest(template = "empty", timeoutTicks = 1600)
    public static void isolatedFourRotations(GameTestHelper helper) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "RUNNING");
        report.put("minecraft", "1.20.1");
        report.put("forge", "47.4.10");
        report.put("java", System.getProperty("java.version"));
        report.put("scope", "Isolated headless Forge GameTest. No player save, production mod, FT core, registry slot, trade or reward logic touched.");
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Fixture may run only under GameTestServer");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("out/room.json")), JsonObject.class);
            JsonObject blueprint = JSON.fromJson(Files.readString(root().resolve("out/blueprint.json")), JsonObject.class);
            String fingerprint = hash(root().resolve("out/room.nbt"));
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source fingerprint mismatch");
            report.put("structure_sha256", fingerprint);
            CompoundTag encoded = NbtIo.readCompressed(root().resolve("out/room.nbt").toFile());
            Map<BlockPos, BlockState> expected = audit(encoded, metadata);
            StructureTemplate template = new StructureTemplate();
            template.load(level.holderLookup(BuiltInRegistries.BLOCK.key()), encoded);
            CompoundTag roundtrip = template.save(new CompoundTag());
            require(audit(roundtrip, metadata).equals(expected), "Native load/save roundtrip differs");
            report.put("native_registry_properties_entities_and_roundtrip", "PASS");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> immediate = new ArrayList<>();
            int fixtureIndex = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(helper.absolutePos(new BlockPos(64 + fixtureIndex * 100, 20, 64)), rotation);
                BlockPos minimum = fixture.minimum();
                Vec3i extent = fixture.rotatedSize();
                for (int chunkX = minimum.getX() >> 4; chunkX <= (minimum.getX() + extent.getX()) >> 4; chunkX++) {
                    for (int chunkZ = minimum.getZ() >> 4; chunkZ <= (minimum.getZ() + extent.getZ()) >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                level.getEntities(null, fixture.bounds()).forEach(entity -> entity.discard());
                for (BlockPos relative : BlockPos.betweenClosed(-1, -1, -1, 29, 23, 33)) {
                    BlockPos position = fixture.world(relative);
                    level.getChunk(position);
                    level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                }
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement rejected");
                immediate.add(verify(level, fixture, expected, metadata, blueprint, false));
                negativeProbes(level, fixture, blueprint);
                for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
                    BlockPos position = fixture.world(entry.getKey());
                    level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                    if (entry.getValue().is(Blocks.LAVA)) {
                        level.scheduleTick(position, Fluids.LAVA, 1);
                    }
                }
                fixtures.add(fixture);
                fixtureIndex++;
            }
            report.put("immediate_rotations", immediate);
            report.put("negative_probes", Map.of("missing_chain", 4, "exposed_real_lava", 4, "blocked_entry", 4, "missing_stair", 4, "missing_roof", 4));
            report.put("stabilization_ticks", 200);
            report.put("neighbor_updates_and_forced_lava_ticks", true);
            writeReport(report);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String, Object>> stable = new ArrayList<>();
                    for (Fixture fixture : fixtures) {
                        stable.add(verify(level, fixture, expected, metadata, blueprint, true));
                    }
                    require(hash(root().resolve("out/room.nbt")).equals(fingerprint), "Source changed during QA");
                    report.put("rotations", stable);
                    report.put("records_checked_after_ticks", expected.size() * fixtures.size());
                    report.put("status", "PASS");
                    report.put("limitations", List.of("Not a full modpack client screenshot or multiplayer/FPS run", "No FT-core hallway/occupancy/idempotence/integration/deployment test", "Transit interfaces are declared relative coordinates only; coordinator must connect actual floors"));
                    writeReport(report);
                    System.out.println("STAIR_D_NATIVE_PASS rotations=4 records=" + expected.size() * fixtures.size() + " ticks=200 lava=64 source=" + fingerprint);
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
