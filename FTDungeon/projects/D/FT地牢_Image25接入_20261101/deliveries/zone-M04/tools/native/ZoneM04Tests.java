package net.farmingtales.zonem04qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
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
import net.minecraft.SharedConstants;
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
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftzonem04qa")
@GameTestHolder("ftzonem04qa")
@PrefixGameTestTemplate(false)
public final class ZoneM04Tests {
    private static final Path ROOT = Path.of(System.getProperty("ftzonem04qa.root"));
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "spawner", "end_portal", "nether_portal", "end_gateway", "fire", "soul_fire", "cobweb", "powder_snow", "tnt");

    public ZoneM04Tests() {}

    private static <Value extends Comparable<Value>> String propertyValue(BlockState state, Property<Value> property) {
        return property.getName(state.getValue(property));
    }

    private static BlockState strictState(GameTestHelper helper, CompoundTag palette) {
        ResourceLocation identity = ResourceLocation.tryParse(palette.getString("Name"));
        helper.assertTrue(identity != null && identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown block " + identity);
        helper.assertTrue(!FORBIDDEN.contains(identity.getPath()), "Forbidden block " + identity);
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette);
        CompoundTag properties = palette.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(key);
            helper.assertTrue(property != null && propertyValue(state, property).equals(properties.getString(key)), "Invalid property " + identity + " " + key);
        }
        return state;
    }

    private static BlockPos world(BlockPos local, BlockPos origin, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
    }

    private static boolean dryWalkable(ServerLevel level, BlockPos feet) {
        AABB body = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8);
        return level.noCollision(body) && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getFluidState(feet.below()).isEmpty() && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty();
    }

    private static Map<String, Object> verify(GameTestHelper helper, Map<BlockPos, BlockState> states, BlockPos origin, Rotation rotation, String phase) {
        ServerLevel level = helper.getLevel();
        int lava = 0;
        int lanterns = 0;
        int containers = 0;
        int rails = 0;
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            BlockPos position = world(entry.getKey(), origin, rotation);
            BlockState actual = level.getBlockState(position);
            BlockState expected = entry.getValue().rotate(rotation);
            helper.assertTrue(actual.equals(expected), phase + " state mismatch " + rotation + " " + entry.getKey() + " expected=" + expected + " actual=" + actual);
            helper.assertTrue(actual.canSurvive(level, position), "Unsupported block " + rotation + " " + entry.getKey() + " " + actual);
            BlockEntity entity = level.getBlockEntity(position);
            if (entity != null) {
                helper.assertTrue(entity instanceof Container, "Unexpected block entity " + entity.getType());
                helper.assertTrue(((Container) entity).isEmpty(), "Nonempty decorative container");
                helper.assertTrue(!entity.saveWithFullMetadata().contains("LootTable"), "Loot table injected");
                containers++;
            }
            if (actual.is(Blocks.LAVA)) {
                lava++;
                helper.assertTrue(actual.getFluidState().isSource(), "Flowing lava after placement");
                for (Direction face : Direction.values()) {
                    BlockPos adjacent = position.relative(face);
                    BlockState seal = level.getBlockState(adjacent);
                    helper.assertTrue(seal.is(Blocks.LAVA) || seal.isCollisionShapeFullBlock(level, adjacent), "Lava enclosure has an open face at " + entry.getKey() + " " + face);
                }
            }
            if (actual.is(Blocks.LANTERN)) {
                lanterns++;
                for (int height = entry.getKey().getY() + 1; height <= 11; height++) {
                    helper.assertTrue(level.getBlockState(world(new BlockPos(entry.getKey().getX(), height, entry.getKey().getZ()), origin, rotation)).is(Blocks.CHAIN), "Broken lamp chain");
                }
                BlockPos anchor = world(new BlockPos(entry.getKey().getX(), 12, entry.getKey().getZ()), origin, rotation);
                helper.assertTrue(level.getBlockState(anchor).isCollisionShapeFullBlock(level, anchor), "Lantern chain has no ceiling anchor");
            }
            if (actual.is(Blocks.RAIL)) rails++;
        }
        helper.assertTrue(lava == 57 && lanterns == 6 && containers == 9 && rails == 26, "Theme or lava count changed: " + lava + "/" + lanterns + "/" + containers + "/" + rails);
        int routeCells = 0;
        for (int width = 11; width <= 13; width++) {
            for (int depth = -3; depth <= 23; depth++) {
                helper.assertTrue(dryWalkable(level, world(new BlockPos(width, 3, depth), origin, rotation)), "Blocked or unsafe route " + rotation + " " + width + "," + depth);
                routeCells++;
            }
        }
        for (int width = 10; width <= 14; width++) {
            for (int depth = 2; depth <= 6; depth++) helper.assertTrue(dryWalkable(level, world(new BlockPos(width, 3, depth), origin, rotation)), "Unsafe five-by-five landing");
        }
        for (int width = 0; width <= 24; width++) {
            for (int depth = 0; depth <= 24; depth++) helper.assertTrue(level.getBlockState(world(new BlockPos(width, 13, depth), origin, rotation)).is(Blocks.DEEPSLATE_TILES), "Incomplete roof");
        }
        for (int width : List.of(4, 20)) {
            for (int depth : List.of(5, 20)) {
                for (int height = 2; height <= 13; height++) {
                    BlockPos position = world(new BlockPos(width, height, depth), origin, rotation);
                    helper.assertTrue(level.getBlockState(position).isCollisionShapeFullBlock(level, position), "Column not floor-to-roof");
                }
            }
        }
        int boundaryCells = 0;
        for (int width = -2; width <= 26; width++) {
            for (int depth = -4; depth <= 26; depth++) {
                for (int height = -2; height <= 15; height++) {
                    BlockPos position = world(new BlockPos(width, height, depth), origin, rotation);
                    BlockState actual = level.getBlockState(position);
                    helper.assertTrue(!actual.is(Blocks.FIRE) && !actual.is(Blocks.SOUL_FIRE), "Fire appeared near timber");
                    if (!level.getFluidState(position).isEmpty()) {
                        boolean permitted = height == 1 && width >= 3 && width <= 21 && depth >= 13 && depth <= 15;
                        helper.assertTrue(permitted && level.getFluidState(position).isSource() && actual.is(Blocks.LAVA), "Liquid leaked at local " + width + "," + height + "," + depth);
                    }
                    boundaryCells++;
                }
            }
        }
        BlockPos first = world(new BlockPos(-2, -2, -4), origin, rotation);
        BlockPos last = world(new BlockPos(26, 15, 26), origin, rotation);
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()), Math.min(first.getY(), last.getY()), Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, Math.max(first.getY(), last.getY()) + 1, Math.max(first.getZ(), last.getZ()) + 1);
        helper.assertTrue(level.getEntities((Entity) null, bounds).isEmpty(), "Entity or item drop appeared in isolated room");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "PASS");
        result.put("rotation", rotation.name());
        result.put("phase", phase);
        result.put("origin", List.of(origin.getX(), origin.getY(), origin.getZ()));
        result.put("compared_block_states", states.size());
        result.put("state_mismatches", 0);
        result.put("support_failures", 0);
        result.put("route_cells_including_approach", routeCells);
        result.put("safe_landing_cells", 25);
        result.put("lava_sources", lava);
        result.put("lava_open_faces", 0);
        result.put("fluid_and_fire_boundary_cells", boundaryCells);
        result.put("empty_containers", containers);
        result.put("rails", rails);
        result.put("entities_or_item_drops", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "zone_m04_native", timeoutTicks = 1000)
    public static void roomPlacement(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
        Path source = ROOT.resolve("room.nbt");
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        helper.assertTrue(hash.equals(JsonParser.parseString(Files.readString(ROOT.resolve("room.json"))).getAsJsonObject().get("sha256").getAsString()), "Structure hash mismatch");
        CompoundTag data = NbtIo.readCompressed(source.toFile());
        helper.assertTrue(data.getInt("DataVersion") == 3465 && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Wrong data version or entities");
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : data.getList("palette", Tag.TAG_COMPOUND)) palette.add(strictState(helper, (CompoundTag) entry));
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (Tag entry : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            helper.assertTrue(local.getX() >= 0 && local.getX() < 25 && local.getY() >= 0 && local.getY() < 14 && local.getZ() >= 0 && local.getZ() < 25, "Coordinate outside room");
            helper.assertTrue(!states.containsKey(local) && !record.contains("nbt"), "Duplicate coordinate or inventory payload");
            states.put(local, palette.get(record.getInt("state")));
        }
        helper.assertTrue(states.size() == 8750, "Incomplete template volume");
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), data);
        helper.assertTrue(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == 8750, "Native roundtrip lost records");
        List<Rotation> rotations = List.of(Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
        List<BlockPos> origins = new ArrayList<>();
        List<Map<String, Object>> immediate = new ArrayList<>();
        for (int index = 0; index < 4; index++) {
            Rotation rotation = rotations.get(index);
            BlockPos origin = helper.absolutePos(new BlockPos(96 + index * 96, 260, 96));
            origins.add(origin);
            BlockPos first = world(new BlockPos(-4, -3, -6), origin, rotation);
            BlockPos last = world(new BlockPos(28, 16, 28), origin, rotation);
            for (int chunkX = Math.min(first.getX(), last.getX()) >> 4; chunkX <= Math.max(first.getX(), last.getX()) >> 4; chunkX++) {
                for (int chunkZ = Math.min(first.getZ(), last.getZ()) >> 4; chunkZ <= Math.max(first.getZ(), last.getZ()) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
            }
            for (int width = -4; width <= 28; width++) {
                for (int depth = -6; depth <= 28; depth++) {
                    for (int height = -3; height <= 16; height++) {
                        BlockPos position = world(new BlockPos(width, height, depth), origin, rotation);
                        helper.assertTrue(level.getFluidState(position).isEmpty(), "Ambient fluid in sky fixture");
                        level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Placement failed " + rotation);
            helper.assertTrue(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_ALL), "Repeated placement failed " + rotation);
            for (int width = 11; width <= 13; width++) {
                for (int depth = -3; depth < 0; depth++) level.setBlock(world(new BlockPos(width, 2, depth), origin, rotation), Blocks.POLISHED_ANDESITE.defaultBlockState(), Block.UPDATE_ALL);
            }
            for (BlockPos local : states.keySet()) {
                BlockPos position = world(local, origin, rotation);
                level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                if (!level.getFluidState(position).isEmpty()) level.scheduleTick(position, level.getFluidState(position).getType(), 1);
            }
            immediate.add(verify(helper, states, origin, rotation, "repeated_placement_and_neighbor_updates"));
            System.out.println("ZONE_M04_IMMEDIATE_PASS rotation=" + rotation + " states=8750 lava=57");
        }
        int[] lavaRandomTicks = {0};
        for (int delay = 20; delay < 400; delay += 20) {
            helper.runAfterDelay(delay, () -> {
                for (int index = 0; index < 4; index++) {
                    for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
                        if (entry.getValue().is(Blocks.LAVA)) {
                            BlockPos position = world(entry.getKey(), origins.get(index), rotations.get(index));
                            level.getBlockState(position).randomTick(level, position, level.random);
                            lavaRandomTicks[0]++;
                        }
                    }
                }
            });
        }
        long placedAt = level.getGameTime();
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> delayed = new ArrayList<>();
                for (int index = 0; index < 4; index++) {
                    Rotation rotation = rotations.get(index);
                    BlockPos origin = origins.get(index);
                    delayed.add(verify(helper, states, origin, rotation, "after_400_server_ticks_and_lava_fire_stress"));
                    BlockPos first = world(BlockPos.ZERO, origin, rotation);
                    BlockPos last = world(new BlockPos(24, 13, 24), origin, rotation);
                    BlockPos minimum = new BlockPos(Math.min(first.getX(), last.getX()), Math.min(first.getY(), last.getY()), Math.min(first.getZ(), last.getZ()));
                    StructureTemplate capture = new StructureTemplate();
                    capture.fillFromWorld(level, minimum, new BlockPos(25, 14, 25), false, Blocks.STRUCTURE_VOID);
                    NbtIo.writeCompressed(capture.save(new CompoundTag()), ROOT.resolve("qa/native-placed-" + rotation.name() + ".nbt").toFile());
                }
                helper.assertTrue(level.getGameTime() - placedAt >= 400 && lavaRandomTicks[0] == 4332, "Incomplete stability interval");
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "PASS");
                result.put("id", "zone-M04");
                result.put("structure_sha256", hash);
                result.put("minecraft", SharedConstants.getCurrentVersion().getName());
                result.put("forge", net.minecraftforge.versions.forge.ForgeVersion.getVersion());
                result.put("strict_palette_states", palette.size());
                result.put("roundtrip_records", states.size());
                result.put("rotations", 4);
                result.put("actual_world_placements", 8);
                result.put("post_placement_server_ticks", level.getGameTime() - placedAt);
                result.put("lava_native_random_tick_stress_calls", lavaRandomTicks[0]);
                result.put("do_fire_tick", true);
                result.put("random_tick_speed", 3);
                result.put("fluid_ticks_explicitly_scheduled", true);
                result.put("placement_chunks_force_loaded", true);
                result.put("fixture", "Isolated sky fixture. No exterior basin or containment blocks; only nine dry entry approach floor blocks per rotation.");
                result.put("immediate", immediate);
                result.put("delayed", delayed);
                result.put("scope", "Actual Forge ServerLevel StructureTemplate placement, repeat placement, four rotations, collision, canSurvive, inventories, roof, columns, six-face lava seal, native lava/fire ticks, expanded fluid/fire boundary and no dropped entities. No formal integration or full-modpack client claim.");
                Files.writeString(ROOT.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("ZONE_M04_FORGE_PASS sha256=" + hash + " rotations=4 placements=8 ticks=400 lava_sources=57 leaks=0 fire=0 stress=" + lavaRandomTicks[0]);
                helper.succeed();
            } catch (Exception failure) {
                throw new IllegalStateException(failure);
            }
        });
    }
}
