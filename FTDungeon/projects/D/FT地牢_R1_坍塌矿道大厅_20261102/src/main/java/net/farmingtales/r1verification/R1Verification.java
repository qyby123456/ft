package net.farmingtales.r1verification;

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
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftr1verification")
@GameTestHolder("ftr1verification")
@PrefixGameTestTemplate(false)
public final class R1Verification {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos SIZE = new BlockPos(17, 11, 17);
    private static final BlockPos ENTRANCE = new BlockPos(8, 1, 0);
    private static final BlockPos INTERIOR = new BlockPos(8, 1, 4);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.MAGMA_BLOCK, Blocks.CACTUS);

    private record Fixture(Rotation rotation, BlockPos origin, Map<BlockPos, BlockState> expected,
                           Map<BlockPos, CompoundTag> entities, BlockPos minimum, BlockPos maximum) {}

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static BlockPos transform(BlockPos local, Fixture fixture) {
        return transform(local, fixture.origin, fixture.rotation);
    }

    private static BlockPos transform(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    @GameTest(template = "empty", batch = "r1_native_placement", timeoutTicks = 850)
    public static void fourRotationsAndStability(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer is allowed");
        Path root = Path.of(System.getProperty("ftr1.project"));
        Path source = root.resolve("room.nbt");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        JsonObject metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        require(metadata.get("sha256").getAsString().equals(hash), "Metadata fingerprint mismatch");
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Expected 1.20.1 single palette");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Template entities are forbidden");
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
        require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 3179, "Roundtrip record loss");
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        List<Fixture> fixtures = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = new BlockPos(1024 + rotation.ordinal() * 64, 100, 1024);
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Map<BlockPos, CompoundTag> entities = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(local.getX() >= 0 && local.getX() < 17 && local.getY() >= 0 && local.getY() < 11
                        && local.getZ() >= 0 && local.getZ() < 17, "Record outside bounds");
                BlockPos world = transform(local, origin, rotation);
                require(expected.put(world, states.get(record.getInt("state")).rotate(rotation)) == null, "Duplicate record");
                if (record.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = record.getCompound("nbt");
                    require(entity.getString("id").equals("minecraft:barrel")
                            && entity.getList("Items", Tag.TAG_COMPOUND).isEmpty()
                            && !entity.contains("LootTable") && !entity.contains("LootTableSeed"), "Unexpected inventory or loot");
                    entities.put(world, entity);
                }
            }
            BlockPos corner = transform(SIZE.offset(-1, -1, -1), origin, rotation);
            BlockPos minimum = new BlockPos(Math.min(origin.getX(), corner.getX()), origin.getY(), Math.min(origin.getZ(), corner.getZ()));
            BlockPos maximum = new BlockPos(Math.max(origin.getX(), corner.getX()), origin.getY() + 10, Math.max(origin.getZ(), corner.getZ()));
            for (BlockPos position : BlockPos.betweenClosed(minimum.offset(-1, -1, -1), maximum.offset(1, 1, 1))) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Fixture volume is occupied: " + position);
            }
            for (int chunkX = (minimum.getX() - 1) >> 4; chunkX <= (maximum.getX() + 1) >> 4; chunkX++) {
                for (int chunkZ = (minimum.getZ() - 1) >> 4; chunkZ <= (maximum.getZ() + 1) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true).setFinalizeEntities(false);
            require(template.placeInWorld(level, origin, origin, settings, level.random, FLAGS), "Native template placement failed");
            fixtures.add(new Fixture(rotation, origin, expected, entities, minimum, maximum));
        }
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.setWeatherParameters(12000, 0, false, false);
        List<Map<String, Object>> initial = new ArrayList<>();
        List<String> negatives = negativeProbes(level, fixtures.get(0));
        for (Fixture fixture : fixtures) {
            initial.add(inspect(level, fixture));
            for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
                level.updateNeighborsAt(entry.getKey(), entry.getValue().getBlock());
                if (entry.getValue().getBlock() instanceof FallingBlock) {
                    level.scheduleTick(entry.getKey(), entry.getValue().getBlock(), 1);
                }
            }
            System.out.println("R1_NATIVE_INITIAL_PASS: " + fixture.rotation + "; " + initial.get(initial.size() - 1));
        }
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> settled = new ArrayList<>();
                for (Fixture fixture : fixtures) {
                    settled.add(inspect(level, fixture));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("sha256", hash);
                result.put("minecraft", "1.20.1");
                result.put("forge", "47.4.10");
                result.put("evidence", "real isolated GameTestServer / ServerLevel / StructureTemplate.placeInWorld");
                result.put("rotations", 4);
                result.put("initial", initial);
                result.put("after_400_ticks", settled);
                result.put("negative_probes_rejected", negatives);
                result.put("gravity_ticks_explicitly_scheduled", true);
                result.put("neighbor_updates_dispatched", true);
                result.put("random_tick_speed", 3);
                result.put("fire_tick_enabled", true);
                result.put("production_save_used", false);
                result.put("full_modpack_playtest", false);
                Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("R1_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400; sha256=" + hash);
                helper.succeed();
            } catch (Exception exception) {
                throw new IllegalStateException("R1 post-tick native verification failed", exception);
            }
        });
    }

    private static Map<String, Object> inspect(ServerLevel level, Fixture fixture) {
        int supported = 0;
        int lamps = 0;
        int rails = 0;
        int gravity = 0;
        for (Map.Entry<BlockPos, BlockState> entry : fixture.expected.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed: " + position + ": " + entry.getValue() + " -> " + actual);
            require(level.getFluidState(position).isEmpty(), "Unexpected fluid: " + position);
            if (!actual.isAir()) {
                require(actual.canSurvive(level, position), "Native canSurvive failure: " + position + " " + actual);
                supported++;
            }
            if (actual.is(Blocks.LANTERN)) {
                lamps++;
                BlockPos anchor = actual.getValue(BlockStateProperties.HANGING) ? position.above() : position.below();
                require(!level.isEmptyBlock(anchor), "Floating lantern: " + position);
            }
            if (actual.is(Blocks.RAIL)) {
                rails++;
            }
            if (actual.getBlock() instanceof FallingBlock) {
                gravity++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Gravity block lacks sturdy support");
            }
            BlockEntity entity = level.getBlockEntity(position);
            if (entity != null) {
                require(fixture.entities.containsKey(position) && entity.getType().isValid(actual), "Unexpected/invalid block entity");
                require(entity instanceof Container && ((Container) entity).isEmpty(), "Nonempty native barrel inventory");
                CompoundTag saved = entity.saveWithFullMetadata();
                require(saved.getString("id").equals("minecraft:barrel") && !saved.contains("LootTable")
                        && saved.getList("Items", Tag.TAG_COMPOUND).isEmpty(), "Loot inventory is forbidden");
            } else {
                require(!fixture.entities.containsKey(position), "Expected barrel block entity is missing");
            }
        }
        require(supported == 1351 && lamps == 5 && rails == 19 && gravity == 6 && fixture.entities.size() == 6, "Missing architectural landmark");
        Map<String, Object> report = spatial(level, fixture);
        int boundary = 0;
        for (BlockPos position : BlockPos.betweenClosed(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(1, 1, 1))) {
            if (!fixture.expected.containsKey(position)) {
                require(level.isEmptyBlock(position) && level.getFluidState(position).isEmpty(), "Boundary changed: " + position);
                boundary++;
            }
        }
        require(level.getEntities(null, new AABB(fixture.minimum.offset(-1, -1, -1), fixture.maximum.offset(2, 2, 2))).isEmpty(), "Unexpected entity / falling block / item");
        report.put("rotation", fixture.rotation.toString());
        report.put("records", fixture.expected.size());
        report.put("native_supported_blocks", supported);
        report.put("empty_native_barrel_entities", fixture.entities.size());
        report.put("supported_lanterns", lamps);
        report.put("stable_rails", rails);
        report.put("supported_gravity_blocks", gravity);
        report.put("unchanged_boundary_air_cells", boundary);
        report.put("unwanted_entities", 0);
        return report;
    }

    private static Map<String, Object> spatial(ServerLevel level, Fixture fixture) {
        for (int posX = 0; posX < 17; posX++) {
            for (int posZ = 0; posZ < 17; posZ++) {
                for (int posY : List.of(0, 10)) {
                    BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture);
                    require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Floor/roof hole at " + position);
                }
                if (posX == 0 || posX == 16 || posZ == 0 || posZ == 16) {
                    for (int posY = 1; posY < 10; posY++) {
                        if (!(posZ == 0 && posX >= 7 && posX <= 9 && posY <= 3)) {
                            require(!level.isEmptyBlock(transform(new BlockPos(posX, posY, posZ), fixture)), "Perimeter hole");
                        }
                    }
                }
            }
        }
        for (int posX = 7; posX <= 9; posX++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                BlockPos feet = transform(new BlockPos(posX, 1, posZ), fixture);
                require(walkable(level, feet), "Three-wide route is blocked: " + feet);
                require(level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty(), "Three-high approach is blocked");
            }
        }
        int collapse = 0;
        for (int posX = 10; posX <= 15; posX++) {
            for (int posZ = 6; posZ <= 12; posZ++) {
                for (int posY = 1; posY <= 4; posY++) {
                    BlockPos position = transform(new BlockPos(posX, posY, posZ), fixture);
                    if (!level.isEmptyBlock(position)) {
                        require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Floating collapse/beam: " + position);
                        collapse++;
                    }
                }
            }
        }
        for (int posX : List.of(3, 13)) {
            for (int posZ : List.of(5, 13)) {
                for (int posY = 0; posY <= 10; posY++) {
                    require(!level.isEmptyBlock(transform(new BlockPos(posX, posY, posZ), fixture)), "Broken structural post");
                }
            }
        }
        Set<BlockPos> reachable = flood(level, fixture, transform(ENTRANCE, fixture));
        require(reachable.contains(transform(INTERIOR, fixture)), "Interior is unreachable");
        require(flood(level, fixture, transform(INTERIOR, fixture)).contains(transform(ENTRANCE, fixture)), "Return path is blocked");
        List<BlockPos> landmarks = List.of(new BlockPos(3, 1, 8), new BlockPos(6, 1, 10), new BlockPos(4, 1, 14),
                new BlockPos(9, 1, 9), new BlockPos(12, 1, 14), new BlockPos(8, 1, 14));
        for (BlockPos landmark : landmarks) {
            require(reachable.contains(transform(landmark, fixture)), "Landmark is unreachable: " + landmark);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reachable_dry_player_positions", reachable.size());
        report.put("player_collision_box", "0.6 x 1.8 blocks");
        report.put("reachable_landmarks", landmarks.size());
        report.put("grounded_collapse_blocks", collapse);
        report.put("complete_floor_and_roof_cells", 578);
        report.put("two_way_entrance_route", true);
        return report;
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static Set<BlockPos> flood(ServerLevel level, Fixture fixture, BlockPos start) {
        require(walkable(level, start), "Flood start is unsafe");
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = current.relative(direction);
                if (candidate.getX() < fixture.minimum.getX() || candidate.getX() > fixture.maximum.getX()
                        || candidate.getZ() < fixture.minimum.getZ() || candidate.getZ() > fixture.maximum.getZ()) {
                    continue;
                }
                if (!visited.contains(candidate) && walkable(level, candidate)) {
                    visited.add(candidate);
                    pending.addLast(candidate);
                }
            }
        }
        return visited;
    }

    private static List<String> negativeProbes(ServerLevel level, Fixture fixture) {
        List<String> rejected = new ArrayList<>();
        Map<String, BlockPos> mutations = new LinkedHashMap<>();
        mutations.put("floor_hole", new BlockPos(8, 0, 2));
        mutations.put("roof_hole", new BlockPos(8, 10, 8));
        mutations.put("blocked_entrance", new BlockPos(8, 1, 0));
        mutations.put("floating_collapse", new BlockPos(15, 2, 8));
        mutations.put("broken_support_post", new BlockPos(3, 3, 5));
        for (Map.Entry<String, BlockPos> mutation : mutations.entrySet()) {
            BlockPos world = transform(mutation.getValue(), fixture);
            BlockState saved = level.getBlockState(world);
            level.setBlock(world, mutation.getKey().equals("blocked_entrance") ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), FLAGS);
            boolean failed = false;
            try {
                spatial(level, fixture);
            } catch (IllegalStateException expected) {
                failed = true;
            } finally {
                level.setBlock(world, saved, FLAGS);
            }
            require(failed, "Negative probe escaped detection: " + mutation.getKey());
            rejected.add(mutation.getKey());
        }
        BlockPos lantern = transform(new BlockPos(6, 4, 5), fixture);
        BlockPos anchor = lantern.above();
        BlockState saved = level.getBlockState(anchor);
        level.setBlock(anchor, Blocks.AIR.defaultBlockState(), FLAGS);
        boolean unsupported = !level.getBlockState(lantern).canSurvive(level, lantern);
        level.setBlock(anchor, saved, FLAGS);
        require(unsupported, "Unsupported lantern escaped native canSurvive");
        rejected.add("unanchored_lantern");
        return rejected;
    }
}
