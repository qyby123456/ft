package net.farmingtales.zoneh03qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
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
import java.util.UUID;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzoneh03qa")
@PrefixGameTestTemplate(false)
public final class ZoneH03PlacementTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(29, 16, 35);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.SPAWNER,
            Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.COBWEB, Blocks.POWDER_SNOW);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean walkable(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (FORBIDDEN.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) return false;
        }
        BlockPos floor = feet.below();
        return level.getFluidState(floor).isEmpty() && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walkGround(ServerLevel level, Placement placement) {
        for (int blockX = 12; blockX <= 16; blockX++) for (int blockZ = 0; blockZ <= 26; blockZ++) {
            BlockPos feet = transform(new BlockPos(blockX, 2, blockZ), placement);
            require(walkable(level, feet) && level.getBlockState(feet.above(2)).isAir(), "Main route obstructed " + feet);
        }
        BlockPos entrance = new BlockPos(14, 2, 0);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(entrance);
        pending.add(entrance);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 29 || next.getZ() < 0 || next.getZ() >= 35 || visited.contains(next)) continue;
                if (walkable(level, transform(next, placement))) { visited.add(next); pending.add(next); }
            }
        }
        for (BlockPos checkpoint : List.of(new BlockPos(14, 2, 4), new BlockPos(3, 2, 17), new BlockPos(25, 2, 17), new BlockPos(14, 2, 26))) require(visited.contains(checkpoint), "Ground checkpoint disconnected " + checkpoint);
        return visited.size();
    }

    private static void walkStairs(ServerLevel level, Placement placement) {
        BlockPos start = transform(new BlockPos(14, 2, 0), placement);
        BlockPos landing = transform(new BlockPos(14, 4, 29), placement);
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes(("H03-" + placement.rotation()).getBytes(StandardCharsets.UTF_8)), "H03QA"));
        player.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);
        player.setOnGround(true);
        Direction forward = placement.rotation().rotate(Direction.SOUTH);
        Vec3 movement = new Vec3(forward.getStepX() * 0.1, -0.08, forward.getStepZ() * 0.1);
        for (int step = 0; step < 290; step++) {
            player.move(MoverType.SELF, movement);
            require(level.noCollision(player.getBoundingBox()), "Native player collision on ascent");
        }
        require(Math.abs(player.getX() - landing.getX() - 0.5) < 0.02 && Math.abs(player.getZ() - landing.getZ() - 0.5) < 0.02
                && Math.abs(player.getY() - landing.getY()) < 0.02, "Native stair ascent failed " + player.position());
        for (int step = 0; step < 290; step++) {
            player.move(MoverType.SELF, new Vec3(-movement.x, -0.08, -movement.z));
            require(level.noCollision(player.getBoundingBox()), "Native player collision on descent");
        }
        require(Math.abs(player.getX() - start.getX() - 0.5) < 0.02 && Math.abs(player.getZ() - start.getZ() - 0.5) < 0.02
                && Math.abs(player.getY() - start.getY()) < 0.02, "Native stair return failed " + player.position());
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int candleGroups = 0;
        int individualCandles = 0;
        int chains = 0;
        int banners = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "Changed state at " + position + ": expected " + entry.getValue() + ", actual " + actual);
            require(actual.canSurvive(level, position) && actual.getFluidState().isEmpty(), "Survival/fluid check failed " + position);
            BlockEntity entity = level.getBlockEntity(position);
            if (actual.is(Blocks.CANDLE)) {
                candleGroups++;
                individualCandles += actual.getValue(BlockStateProperties.CANDLES);
                require(actual.getValue(BlockStateProperties.LIT), "Candle went out");
                BlockPos support = position.below();
                require(level.getBlockState(support).isFaceSturdy(level, support, Direction.UP), "Unsupported candle " + position);
            }
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Unexpected chain axis");
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Unanchored chain");
            }
            if (entity != null) {
                require(entity instanceof BannerBlockEntity && actual.is(Blocks.PURPLE_WALL_BANNER), "Unexpected block entity");
                CompoundTag saved = entity.saveWithoutMetadata();
                require(saved.getList("Patterns", Tag.TAG_COMPOUND).size() == 2 && !saved.contains("Items") && !saved.contains("LootTable"), "Invalid banner payload");
                Direction facing = actual.getValue(BlockStateProperties.HORIZONTAL_FACING);
                BlockPos support = position.relative(facing.getOpposite());
                require(level.getBlockState(support).isFaceSturdy(level, support, facing), "Unanchored banner");
                banners++;
            }
        }
        require(candleGroups == 36 && individualCandles == 140 && chains == 10 && banners == 11, "Theme count changed");
        for (int blockX = 0; blockX < 29; blockX++) for (int blockZ = 0; blockZ < 35; blockZ++) for (int blockY : new int[] {14, 15}) {
            BlockPos roof = transform(new BlockPos(blockX, blockY, blockZ), placement);
            require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.UP), "Roof gap");
        }
        for (int[] pillar : new int[][] {{7, 10}, {21, 10}, {7, 24}, {21, 24}}) for (int blockY = 0; blockY < 16; blockY++) {
            BlockPos position = transform(new BlockPos(pillar[0], blockY, pillar[1]), placement);
            require(level.getBlockState(position).isFaceSturdy(level, position, Direction.UP), "Pillar not continuous");
        }
        BlockPos cornerA = transform(BlockPos.ZERO, placement);
        BlockPos cornerB = transform(SIZE.offset(-1, -1, -1), placement);
        AABB bounds = new AABB(Math.min(cornerA.getX(), cornerB.getX()) - 1, cornerA.getY() - 1,
                Math.min(cornerA.getZ(), cornerB.getZ()) - 1, Math.max(cornerA.getX(), cornerB.getX()) + 2,
                cornerB.getY() + 2, Math.max(cornerA.getZ(), cornerB.getZ()) + 2);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(), "Dropped items after placement");
        walkStairs(level, placement);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", placement.rotation().name());
        result.put("states_preserved", placement.expected().size());
        result.put("main_route_positions", 135);
        result.put("reachable_ground_foot_positions", walkGround(level, placement));
        result.put("native_player_stair_ascent_and_return", "PASS: 580 Entity.move collision steps, 0.1 block per step, 0.6 step height");
        result.put("roof_blocks", 2030);
        result.put("continuous_pillars", 4);
        result.put("candle_groups", candleGroups);
        result.put("individual_candles", individualCandles);
        result.put("anchored_chain_blocks", chains);
        result.put("supported_patterned_banners", banners);
        result.put("dropped_items", 0);
        return result;
    }

    @GameTest(template = "empty", batch = "zone_h03_only", timeoutTicks = 2400)
    public static void ceremonialHallSurvivesFourRotations(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTest server permitted");
        Path root = Path.of(System.getProperty("ftzoneh03qa.root")).toAbsolutePath().normalize();
        require(root.toString().contains("routes") && Files.isRegularFile(root.resolve("forge/build.gradle")), "Wrong independent workspace");
        Path nbtPath = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(nbtPath)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("zone-H03") && metadata.get("sha256").getAsString().equals(digest), "Identity/hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(nbtPath.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes") && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Invalid format/entities");
        List<BlockState> palette = new ArrayList<>();
        ListTag paletteTags = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < paletteTags.size(); index++) {
            CompoundTag entry = paletteTags.getCompound(index);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown/nonvanilla block " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block), "Forbidden/hazardous block");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete properties " + identifier);
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong dimensions");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 16240, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getY() >= 0 && position.getZ() >= 0 && position.getX() < 29 && position.getY() < 16 && position.getZ() < 35, "Out of bounds");
            require(local.put(position, palette.get(record.getInt("state"))) == null, "Duplicate coordinate");
            if (record.contains("nbt")) require(palette.get(record.getInt("state")).is(Blocks.PURPLE_WALL_BANNER), "Unexpected custom payload");
        }
        require(local.size() == 16240, "Incomplete explicit volume");
        List<Placement> placements = new ArrayList<>();
        BlockPos[] origins = {new BlockPos(10, 3, 10), new BlockPos(100, 3, 10), new BlockPos(100, 3, 100), new BlockPos(10, 3, 100)};
        Set<BlockPos> allPositions = new HashSet<>();
        Rotation[] rotations = Rotation.values();
        for (int index = 0; index < rotations.length; index++) {
            BlockPos origin = helper.absolutePos(origins[index]);
            Rotation rotation = rotations[index];
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(origin, rotation, expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(rotation)));
            require(expected.keySet().stream().allMatch(allPositions::add), "Test placements overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level, origin, origin, settings, level.random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native placement failed");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, state) -> { state.updateNeighbourShapes(level, position, Block.UPDATE_ALL); level.updateNeighborsAt(position, state.getBlock()); });
        }
        System.out.println("ZONE_H03_INITIAL_PASS: four placements, native player stair return, forced neighbor shape/event updates");
        helper.runAfterDelay(400, () -> {
            try {
                List<Map<String, Object>> evidence = new ArrayList<>();
                for (Placement placement : placements) evidence.add(inspect(level, placement));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", "zone-H03"); result.put("status", "PASS"); result.put("sha256", digest);
                result.put("minecraft", "1.20.1"); result.put("forge", "47.4.10");
                result.put("test", "StructureTemplate.placeInWorld in isolated Forge GameTest server; Entity.move with FakePlayer for stair ascent/return");
                result.put("ticks_observed", 400); result.put("forced_neighbor_shape_and_event_updates", true);
                result.put("native_registry_complete_properties_and_roundtrip", "PASS"); result.put("rotations", evidence);
                result.put("deployed", false); result.put("player_saves_modified", false); result.put("client_visual_tested", false);
                result.put("limits", List.of("Independent vanilla/Forge placement and software rendering; no modpack-client, multiplayer, FPS or existing-hall integration claim", "No combat, rewards, entities or automatic loot are added"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8);
                System.out.println("ZONE_H03_FORGE_PLACEMENT_PASS: four rotations stable after 400 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) { helper.fail(exception.toString()); }
        });
    }
}
