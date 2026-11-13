package net.farmingtales.m7verification;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("m7verification")
@PrefixGameTestTemplate(false)
public final class M7GameTests {
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.WATER,
            Blocks.POWDER_SNOW, Blocks.COBWEB, Blocks.SWEET_BERRY_BUSH);

    private record Placement(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> states,
            int blockEntities, int initialReachable) {}

    private M7GameTests() {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos vector(JsonObject object, String name) {
        var values = object.getAsJsonArray(name);
        return new BlockPos(values.get(0).getAsInt(), values.get(1).getAsInt(), values.get(2).getAsInt());
    }

    private static BlockPos transformed(BlockPos relative, Rotation rotation, BlockPos origin) {
        return StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !HAZARDS.contains(state.getBlock()) && state.getFluidState().isEmpty()
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean standable(ServerLevel level, BlockPos position) {
        BlockState floor = level.getBlockState(position.below());
        return clear(level, position) && clear(level, position.above())
                && !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && (floor.isFaceSturdy(level, position.below(), Direction.UP) || floor.getBlock() instanceof StairBlock);
    }

    private static Set<BlockPos> reachable(ServerLevel level, Placement placement, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, -1, 1}) {
                    BlockPos next = current.relative(direction).offset(0, height, 0);
                    if (!placement.states().containsKey(next) || visited.contains(next)) {
                        continue;
                    }
                    if (height != 0 && !(level.getBlockState(current.below()).getBlock() instanceof StairBlock)
                            && !(level.getBlockState(next.below()).getBlock() instanceof StairBlock)) {
                        continue;
                    }
                    if (standable(level, next)) {
                        visited.add(next);
                        pending.addLast(next);
                    }
                }
            }
        }
        return visited;
    }

    private static int verifyPlacement(ServerLevel level, Placement placement, JsonObject metadata) {
        BlockPos entry = vector(metadata, "entrance");
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos feet = transformed(entry.offset(offset, 0, 0), placement.rotation(), placement.origin());
            require(standable(level, feet) && clear(level, feet.above(2))
                    && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP), "Invalid 3x3 north interface " + feet);
        }
        int blockEntities = 0;
        for (Map.Entry<BlockPos, BlockState> record : placement.states().entrySet()) {
            BlockPos position = record.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual == record.getValue(), "Placed state mismatch " + position + ": expected=" + record.getValue() + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported native block " + actual + " at " + position);
            BlockEntity entity = level.getBlockEntity(position);
            if (actual.hasBlockEntity()) {
                require(entity != null && entity.getType().isValid(actual), "Native block entity missing or wrong type " + position);
                CompoundTag entityData = entity.saveWithFullMetadata();
                require(!entityData.contains("LootTable") && entityData.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Reward inventory found " + position);
                require(!(entity instanceof Container container) || container.isEmpty(), "Native container not empty " + position);
                blockEntities++;
            }
            if (actual.is(Blocks.LAVA)) {
                for (Direction direction : Direction.values()) {
                    BlockPos neighbor = position.relative(direction);
                    BlockState neighborState = level.getBlockState(neighbor);
                    require(neighborState.is(Blocks.LAVA) || neighborState.isFaceSturdy(level, neighbor, direction.getOpposite()), "Unsealed lava " + position);
                }
            }
        }
        require(blockEntities == placement.blockEntities(), "Block entity count changed");
        BlockPos entrance = transformed(entry, placement.rotation(), placement.origin());
        Set<BlockPos> path = reachable(level, placement, entrance);
        List<BlockPos> waypoints = List.of(vector(metadata, "interior"), new BlockPos(4, 3, 7), new BlockPos(14, 3, 7), new BlockPos(9, 3, 15), new BlockPos(9, 3, 16));
        for (BlockPos relative : waypoints) {
            require(path.contains(transformed(relative, placement.rotation(), placement.origin())), "Native path cannot reach " + relative);
        }
        int minimumX = placement.states().keySet().stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maximumX = placement.states().keySet().stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minimumZ = placement.states().keySet().stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maximumZ = placement.states().keySet().stream().mapToInt(BlockPos::getZ).max().orElseThrow();
        for (int blockX = minimumX - 1; blockX <= maximumX + 1; blockX++) {
            for (int blockZ = minimumZ - 1; blockZ <= maximumZ + 1; blockZ++) {
                for (int blockY = placement.origin().getY() - 1; blockY <= placement.origin().getY() + 14; blockY++) {
                    BlockPos position = new BlockPos(blockX, blockY, blockZ);
                    if (blockX < minimumX || blockX > maximumX || blockZ < minimumZ || blockZ > maximumZ || blockY < placement.origin().getY() || blockY > placement.origin().getY() + 13) {
                        require(level.getFluidState(position).isEmpty(), "Escaped boundary fluid " + position);
                    }
                }
            }
        }
        require(level.getEntities(null, new AABB(minimumX, placement.origin().getY(), minimumZ, maximumX + 1, placement.origin().getY() + 14, maximumZ + 1)).isEmpty(), "Unexpected room entities");
        return path.size();
    }

    @GameTest(templateNamespace = "m7verification", template = "empty", batch = "m7_crypt", timeoutTicks = 400)
    public static void fourRotationsPlacementAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server may place this room");
        Path root = Path.of(System.getProperty("ft.m7.project"));
        Path structure = root.resolve("delivery/M7/room.nbt");
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("delivery/M7/room.json"))).getAsJsonObject();
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(structure)));
        require(digest.equals(metadata.get("sha256").getAsString()), "Structure hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(structure.toFile());
        require(tag.getInt("DataVersion") == 3465, "Wrong DataVersion");
        require(tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected entity NBT");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> paletteStates = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Missing vanilla block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid block property " + identity + " " + key);
            }
            paletteStates.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(), "Native NBT roundtrip lost records");
        List<Placement> placements = new ArrayList<>();
        int rotationIndex = 0;
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(256 + rotationIndex * 64, 80, 256);
            rotationIndex++;
            Map<BlockPos, BlockState> states = new LinkedHashMap<>();
            int blockEntities = 0;
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos relative = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                BlockPos position = transformed(relative, rotation, origin);
                require(states.put(position, paletteStates.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate transformed record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    require(BlockEntity.loadStatic(position, states.get(position), record.getCompound("nbt")) != null, "Cannot decode block entity " + relative);
                    blockEntities++;
                }
            }
            for (int chunkX = (origin.getX() - 24) >> 4; chunkX <= (origin.getX() + 24) >> 4; chunkX++) {
                for (int chunkZ = (origin.getZ() - 24) >> 4; chunkZ <= (origin.getZ() + 24) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            for (int blockX = origin.getX() - 20; blockX <= origin.getX() + 20; blockX++) {
                for (int blockZ = origin.getZ() - 20; blockZ <= origin.getZ() + 20; blockZ++) {
                    for (int blockY = 79; blockY <= 94; blockY++) {
                        level.setBlock(new BlockPos(blockX, blockY, blockZ), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).setIgnoreEntities(true).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, RandomSource.create(702026L), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement rejected");
            Placement placement = new Placement(rotation, origin, states, blockEntities, 0);
            int reachable = verifyPlacement(level, placement, metadata);
            placements.add(new Placement(rotation, origin, states, blockEntities, reachable));
            for (Map.Entry<BlockPos, BlockState> record : states.entrySet()) {
                BlockPos position = record.getKey();
                if (!record.getValue().isAir()) {
                    BlockState updated = Block.updateFromNeighbourShapes(level.getBlockState(position), level, position);
                    require(updated == record.getValue(), "Unstable neighbor shape " + position + ": expected=" + record.getValue() + " updated=" + updated);
                    level.updateNeighborsAt(position, record.getValue().getBlock());
                    if (record.getValue().is(Blocks.LAVA)) {
                        level.scheduleTick(position, record.getValue().getFluidState().getType(), 1);
                    }
                }
            }
        }
        helper.runAfterDelay(120, () -> {
            try {
                List<Map<String, Object>> rotationReports = new ArrayList<>();
                for (Placement placement : placements) {
                    int reachable = verifyPlacement(level, placement, metadata);
                    require(reachable == placement.initialReachable(), "Native route changed after ticking");
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("rotation", placement.rotation().name());
                    report.put("status", "PASS");
                    report.put("origin", List.of(placement.origin().getX(), placement.origin().getY(), placement.origin().getZ()));
                    report.put("records_checked_before_and_after", placement.states().size());
                    report.put("native_block_entities", placement.blockEntities());
                    report.put("native_reachable_positions", reachable);
                    rotationReports.add(report);
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "M7");
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("structure_sha256", digest);
                result.put("scheduled_tick_delay", 120);
                result.put("rotations", rotationReports);
                result.put("checks", List.of("native registry and properties", "native StructureTemplate decode and roundtrip", "real ServerLevel placement at four rotations", "every block state before and after 120 ticks", "native canSurvive and neighbor-shape stability", "all block entities loaded and empty", "3x3 supported north interface", "native collision two-sided safe path and return", "all six lava faces and ticked fluid containment", "no spawned room entities"));
                result.put("scope", "Isolated Forge GameTest world only. No player saves, existing room catalog or installed JARs were modified. Not full modpack client or multiplayer acceptance.");
                Files.writeString(root.resolve("qa/native-placement.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("M7_NATIVE_PLACEMENT_PASS: rotations=4 records=" + records.size() + " ticks=120 sha256=" + digest);
                helper.succeed();
            } catch (Exception failure) {
                helper.fail("M7 delayed placement validation failed: " + failure);
            }
        });
    }
}
