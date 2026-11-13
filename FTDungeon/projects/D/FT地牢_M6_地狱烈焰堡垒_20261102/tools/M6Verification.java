package net.farmingtales.m6verification;

import com.google.gson.GsonBuilder;
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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftm6verification")
@GameTestHolder("ftm6verification")
@PrefixGameTestTemplate(false)
public final class M6Verification {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(21, 12, 21);
    private static final BlockPos ENTRANCE = new BlockPos(10, 2, 0);
    private static final BlockPos INTERIOR = new BlockPos(10, 2, 4);
    private static final Set<Block> HAZARDS = Set.of(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           Map<BlockPos, CompoundTag> entities, BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    @GameTest(template = "empty", batch = "m6_native_placement", timeoutTicks = 800)
    public static void nativePlacementFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftm6.project"));
        Path source = root.resolve("delivery/M6/room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = com.google.gson.JsonParser.parseString(Files.readString(root.resolve("delivery/M6/room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "No template entities allowed");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag entry = palette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(entry.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block: " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = entry.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property: " + entry);
            }
            BlockState loaded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry);
            require(!FORBIDDEN.contains(loaded.getBlock()), "Forbidden block: " + loaded);
            states.add(loaded);
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        require(template.getSize().equals(SIZE), "Template size mismatch");
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 5292, "Roundtrip record loss");
        List<Fixture> fixtures = new ArrayList<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(128 + rotation.ordinal() * 64, 100, 128);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> entities = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getY() >= 0 && local.getZ() >= 0
                        && local.getX() < 21 && local.getY() < 12 && local.getZ() < 21, "Record outside bounds");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = record.getCompound("nbt");
                    require(!entity.contains("Items") && !entity.contains("LootTable") && !entity.contains("Book"), "Unexpected inventory or loot");
                    entities.put(world, entity);
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 11, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Fixture area occupied at " + position);
            }
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            level.getChunkAt(origin);
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native template placement failed: " + rotation);
            fixtures.add(new Fixture(rotation, origin, expected, entities, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        List<Map<String, Object>> reports = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            reports.add(inspect(level, fixture, true));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                if (entry.getValue().is(Blocks.LAVA)) {
                    level.scheduleTick(entry.getKey(), Fluids.LAVA, 1);
                    level.scheduleTick(entry.getKey(), Fluids.FLOWING_LAVA, 1);
                }
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
            }
            System.out.println("M6_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + reports.get(reports.size() - 1));
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture, false));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("test", "GameTestServer real ServerLevel StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("initial", reports);
                result.put("after_400_ticks", settled);
                result.put("fire_tick_enabled", true);
                result.put("lava_ticks_explicitly_scheduled", true);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("M6_NATIVE_GAME_TEST_PASS: rotations=4/4; records=21168; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("M6 post-tick native verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture, boolean initial) {
        int supports = 0;
        int lava = 0;
        int fire = 0;
        int chains = 0;
        int lamps = 0;
        int bells = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState expected = entry.getValue();
            BlockState actual = level.getBlockState(position);
            if (expected.isAir()) {
                require(actual.isAir(), "Unexpected block in clear space: " + position + " / " + actual);
            } else if (!initial && expected.is(Blocks.LAVA)) {
                require(actual.is(Blocks.LAVA), "Lava disappeared: " + position);
            } else {
                require(actual.equals(expected), "State mismatch " + position + ": " + expected + " -> " + actual);
            }
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native support failure: " + position + " " + actual);
                supports++;
            }
            if (actual.is(Blocks.LAVA)) {
                lava++;
                for (Direction direction : Direction.values()) {
                    if (direction == Direction.UP) {
                        continue;
                    }
                    BlockPos adjacent = position.relative(direction);
                    BlockState neighbor = level.getBlockState(adjacent);
                    require(neighbor.is(Blocks.LAVA) || Block.isShapeFullBlock(neighbor.getCollisionShape(level, adjacent)),
                            "Unsealed lava face: " + position + " " + direction + " " + neighbor);
                }
            }
            if (actual.is(Blocks.SOUL_FIRE)) {
                require(level.getBlockState(position.below()).is(Blocks.SOUL_SAND), "Soul fire lacks soul sand");
                fire++;
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN) && anchor.getY() <= fixture.maximum.getY()) {
                    anchor = anchor.above();
                }
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Floating chain: " + position);
            }
            if (actual.is(Blocks.LANTERN) || actual.is(Blocks.SOUL_LANTERN)) {
                lamps++;
            }
            if (actual.is(Blocks.BELL)) {
                bells++;
            }
            BlockEntity entity = level.getBlockEntity(position);
            if (entity != null) {
                require(fixture.entities.containsKey(position), "Unexpected generated block entity");
                require(entity.getType().isValid(actual), "Invalid native block entity");
                require(!(entity instanceof Container), "No inventory-bearing block entities allowed");
                CompoundTag saved = entity.saveWithFullMetadata();
                require(!saved.contains("Items") && !saved.contains("LootTable") && !saved.contains("Book"), "Native entity has reward contents");
                require(saved.getString("id").equals(fixture.entities.get(position).getString("id")), "Entity identity changed");
            } else {
                require(!fixture.entities.containsKey(position), "Expected block entity missing");
            }
        }
        require(lava == 38 && fire == 1 && lamps == 6 && bells == 2, "Missing landmark or fluid cells");
        int perimeter = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (fixture.expected.containsKey(position)) {
                continue;
            }
            require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary leak or unwanted growth: " + position);
            perimeter++;
        }
        AABB bounds = new AABB(fixture.minimum, fixture.maximum.offset(1, 1, 1));
        require(level.getEntities(null, bounds).isEmpty(), "Unexpected spawned entity or item");
        BlockPos entrance = transform(ENTRANCE, fixture.origin, fixture.rotation);
        BlockPos interior = transform(INTERIOR, fixture.origin, fixture.rotation);
        for (int localX = 9; localX <= 11; localX++) {
            for (int localZ = 0; localZ < 6; localZ++) {
                BlockPos feet = transform(new BlockPos(localX, 2, localZ), fixture.origin, fixture.rotation);
                require(walkable(level, feet), "Unsafe three-wide north entrance");
                require(clear(level, feet.above(2)), "Three-high north clearance blocked");
            }
        }
        Set<BlockPos> reachable = flood(level, fixture, entrance);
        require(reachable.contains(interior), "Interior is unreachable");
        for (BlockPos target : List.of(new BlockPos(10, 2, 7), new BlockPos(7, 2, 11), new BlockPos(13, 2, 11),
                new BlockPos(10, 2, 16), new BlockPos(3, 2, 13), new BlockPos(17, 2, 13),
                new BlockPos(6, 2, 5), new BlockPos(16, 2, 5), new BlockPos(10, 2, 18))) {
            require(reachable.contains(transform(target, fixture.origin, fixture.rotation)), "Dry landmark route is disconnected: " + target);
        }
        require(flood(level, fixture, interior).contains(entrance), "Return route blocked");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supports);
        report.put("valid_block_entities", fixture.entities.size());
        report.put("lava_cells", lava);
        report.put("soul_fire", fire);
        report.put("anchored_chain_cells", chains);
        report.put("supported_lamps", lamps);
        report.put("supported_bells", bells);
        report.put("reachable_dry_feet_positions", reachable.size());
        report.put("dry_landmark_routes", 9);
        report.put("boundary_air_cells_checked", perimeter);
        report.put("reward_inventory", 0);
        return report;
    }

    private static boolean clear(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return !HAZARDS.contains(state.getBlock()) && state.getFluidState().isEmpty()
                && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floorPosition = feet.below();
        BlockState floor = level.getBlockState(floorPosition);
        return clear(level, feet) && clear(level, feet.above()) && !HAZARDS.contains(floor.getBlock())
                && floor.getFluidState().isEmpty() && (floor.isFaceSturdy(level, floorPosition, Direction.UP)
                || floor.getBlock() instanceof StairBlock);
    }

    private static Set<BlockPos> flood(ServerLevel level, Fixture fixture, BlockPos start) {
        require(walkable(level, start), "Unsafe start position: " + start);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int height : new int[]{0, 1, -1}) {
                    BlockPos candidate = current.relative(direction).offset(0, height, 0);
                    if (candidate.getX() < fixture.minimum.getX() || candidate.getX() > fixture.maximum.getX()
                            || candidate.getY() < fixture.minimum.getY() || candidate.getY() > fixture.maximum.getY()
                            || candidate.getZ() < fixture.minimum.getZ() || candidate.getZ() > fixture.maximum.getZ()) {
                        continue;
                    }
                    if (height > 0 && !clear(level, current.above(2)) || height < 0 && !clear(level, candidate.above(2))) {
                        continue;
                    }
                    if (walkable(level, candidate) && visited.add(candidate)) {
                        queue.add(candidate);
                    }
                }
            }
        }
        return visited;
    }
}
