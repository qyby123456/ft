package net.farmingtales.zonec04qa;

import com.google.gson.Gson;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("zonec04qa")
@PrefixGameTestTemplate(false)
public final class ZoneC04Tests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(25, 13, 25);
    private static final Set<Block> HAZARDS = Set.of(Blocks.COBWEB, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.LAVA, Blocks.WATER, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE,
            Blocks.POWDER_SNOW, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT);

    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        for (int height = 0; height < 2; height++) {
            BlockPos position = feet.above(height);
            BlockState state = level.getBlockState(position);
            if (HAZARDS.contains(state.getBlock()) || !state.getFluidState().isEmpty()
                    || !state.getCollisionShape(level, position).isEmpty()) return false;
        }
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        return !HAZARDS.contains(floor.getBlock()) && floor.getFluidState().isEmpty()
                && floor.isFaceSturdy(level, below, Direction.UP)
                && level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                        feet.getX() + 0.8, feet.getY() + 1.8, feet.getZ() + 0.8));
    }

    private static int walk(ServerLevel level, Placement placement) {
        BlockPos start = new BlockPos(12, 1, 0);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        require(standable(level, transform(start, placement)), "North connector not standable");
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 25 || next.getZ() < 0 || next.getZ() >= 25
                        || visited.contains(next)) continue;
                if (standable(level, transform(next, placement))) {
                    visited.add(next);
                    pending.add(next);
                }
            }
        }
        int protectedCells = 0;
        for (int east = 0; east < 25; east++) {
            for (int south = 0; south < 25; south++) {
                boolean route = east >= 11 && east <= 13 && south <= 13
                        || east >= 11 && south >= 11 && south <= 13;
                if (!route) continue;
                BlockPos local = new BlockPos(east, 1, south);
                require(visited.contains(local) && standable(level, transform(local, placement)), "Protected route disconnected at " + local);
                for (int up = 1; up <= 3; up++) require(level.getBlockState(transform(new BlockPos(east, up, south), placement)).isAir(), "Route lacks clearance");
                protectedCells++;
            }
        }
        require(protectedCells == 75 && visited.contains(new BlockPos(24, 1, 12))
                && visited.contains(new BlockPos(12, 1, 4)), "Exit or safety point unreachable");
        for (int offset = -1; offset <= 1; offset++) {
            for (int up = 1; up <= 4; up++) {
                require(level.getBlockState(transform(new BlockPos(12 + offset, up, 0), placement)).isAir(), "North port obstructed");
                require(level.getBlockState(transform(new BlockPos(24, up, 12 + offset), placement)).isAir(), "East port obstructed");
            }
        }
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lanterns = 0;
        int chains = 0;
        int barrels = 0;
        int banners = 0;
        int candles = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": " + entry.getValue() + " -> " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected fluid");
            require(actual.canSurvive(level, position), "Unsupported block: " + actual + " at " + position);
            require(!FORBIDDEN.contains(actual.getBlock()) && !HAZARDS.contains(actual.getBlock()), "Forbidden or hazardous block");
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Chain axis invalid");
                BlockPos anchor = position.above();
                while (level.getBlockState(anchor).is(Blocks.CHAIN)) anchor = anchor.above();
                require(level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN), "Chain lacks load-bearing anchor");
            }
            if (actual.is(Blocks.LANTERN)) {
                lanterns++;
                require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern suspension invalid");
            }
            if (actual.is(Blocks.CANDLE)) {
                candles++;
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Candle lacks tabletop or stone support");
            }
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (actual.is(Blocks.BARREL)) {
                barrels++;
                require(blockEntity instanceof BarrelBlockEntity, "Barrel entity missing");
                require(((BarrelBlockEntity) blockEntity).isEmpty(), "Unexpected container items");
                require(!blockEntity.saveWithoutMetadata().contains("LootTable"), "Loot table present");
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Barrel lacks shelf support");
            } else if (actual.is(Blocks.RED_WALL_BANNER)) {
                banners++;
                require(blockEntity instanceof BannerBlockEntity, "Banner entity missing");
            } else {
                require(blockEntity == null, "Unexpected block entity");
            }
        }
        require(lanterns == 4 && chains == 12 && barrels == 8 && banners == 4 && candles == 3, "Theme counts changed");
        for (int east = 0; east < 25; east++) {
            for (int south = 0; south < 25; south++) {
                BlockPos floor = transform(new BlockPos(east, 0, south), placement);
                BlockPos roof = transform(new BlockPos(east, 12, south), placement);
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Floor gap");
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof gap");
            }
        }
        for (int east = 0; east < 25; east++) {
            for (int south = 0; south < 25; south++) {
                if (east != 0 && east != 24 && south != 0 && south != 24) continue;
                for (int up = 1; up < 12; up++) {
                    boolean port = up <= 4 && (south == 0 && east >= 11 && east <= 13
                            || east == 24 && south >= 11 && south <= 13);
                    if (port) continue;
                    BlockPos wall = transform(new BlockPos(east, up, south), placement);
                    require(level.getBlockState(wall).isCollisionShapeFullBlock(level, wall), "Unplanned external shell opening");
                }
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()),
                Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds.inflate(1)).isEmpty(), "Placement generated item drops");
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("rotation", placement.rotation().name());
        evidence.put("states_preserved", placement.expected().size());
        evidence.put("collision_reachable_positions", walk(level, placement));
        evidence.put("protected_route_positions", 75);
        evidence.put("connector_clearance", "north and east: 3 wide x 4 high");
        evidence.put("anchored_chains", chains);
        evidence.put("supported_lanterns", lanterns);
        evidence.put("empty_supported_barrels", barrels);
        evidence.put("supported_banners", banners);
        evidence.put("supported_candle_groups", candles);
        evidence.put("item_drops", 0);
        evidence.put("floor_roof_external_shell", "PASS");
        evidence.put("can_survive_all_records", "PASS");
        return evidence;
    }

    @GameTest(template = "empty", timeoutTicks = 520)
    public static void fourRotationsStable(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only isolated GameTest server allowed");
        Path root = Path.of(System.getProperty("zonec04qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("zone-C04") && Files.exists(root.resolve("forge/build.gradle")), "Wrong isolated workspace");
        Path source = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("zone-C04") && metadata.get("sha256").getAsString().equals(digest), "Metadata/hash mismatch");
        CompoundTag tag = NbtIo.readCompressed(source.toFile());
        require(tag.getInt("DataVersion") == 3465 && !tag.contains("palettes")
                && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Version, palette or entity error");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int paletteIndex = 0; paletteIndex < palette.size(); paletteIndex++) {
            CompoundTag entry = palette.getCompound(paletteIndex);
            ResourceLocation identifier = new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Unknown block: " + identifier);
            Block block = BuiltInRegistries.BLOCK.get(identifier);
            require(!FORBIDDEN.contains(block) && !HAZARDS.contains(block), "Forbidden palette entry");
            CompoundTag properties = entry.getCompound("Properties");
            require(properties.size() == block.getStateDefinition().getProperties().size(), "Incomplete state: " + identifier);
            for (String propertyName : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(propertyName);
                require(property != null && property.getValue(properties.getString(propertyName)).isPresent(), "Invalid property: " + identifier + " " + propertyName);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry));
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(SIZE), "Wrong template size");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 8125, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
            CompoundTag record = records.getCompound(recordIndex);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 25 && position.getY() >= 0 && position.getY() < 13
                    && position.getZ() >= 0 && position.getZ() < 25, "Out-of-bounds record");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
            if (record.contains("nbt")) {
                CompoundTag blockEntity = record.getCompound("nbt");
                require(Set.of("minecraft:barrel", "minecraft:banner").contains(blockEntity.getString("id"))
                        && !blockEntity.contains("LootTable") && !blockEntity.contains("Items"), "Unsafe block entity payload");
            }
        }
        require(local.size() == 8125, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(10, 2, 10), new BlockPos(75, 2, 10), new BlockPos(75, 2, 75), new BlockPos(10, 2, 75)};
        Set<BlockPos> occupied = new HashSet<>();
        for (int placementIndex = 0; placementIndex < rotations.length; placementIndex++) {
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            BlockPos horizontalOrigin = helper.absolutePos(origins[placementIndex]);
            Placement placement = new Placement(new BlockPos(horizontalOrigin.getX(), 200, horizontalOrigin.getZ()), rotations[placementIndex], expected);
            local.forEach((position, state) -> expected.put(transform(position, placement), state.rotate(placement.rotation())));
            require(expected.keySet().stream().allMatch(occupied::add), "Rotated test volumes overlap");
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(placement.rotation()).setKnownShape(true);
            require(template.placeInWorld(level, placement.origin(), placement.origin(), settings, level.random,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE), "Native template placement rejected");
            placements.add(placement);
            inspect(level, placement);
            expected.forEach((position, state) -> {
                state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
                level.updateNeighborsAt(position, state.getBlock());
            });
        }
        System.out.println("C04_NATIVE_INITIAL_PASS: four rotations; 32500 records; forced neighbor updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> rotationEvidence = new ArrayList<>();
                for (Placement placement : placements) rotationEvidence.add(inspect(level, placement));
                require(digest.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)))), "Source changed during QA");
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("id", "zone-C04");
                report.put("status", "PASS");
                report.put("sha256", digest);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("ticks_observed", 240);
                report.put("registry_properties_and_native_roundtrip", "PASS");
                report.put("forced_neighbor_updates", true);
                report.put("rotations", rotationEvidence);
                report.put("preplaced_entities", 0);
                report.put("isolated_world_only", true);
                report.put("fixture_floor_world_y", 200);
                report.put("fixture_reason", "Above terrain, isolated from natural fluids entering intentional open connectors");
                report.put("client_visual_tested", false);
                report.put("limits", List.of("Isolated Forge server verification; no full modpack client or multiplayer play test", "No production save, JAR, resource pack or configuration modified"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("C04_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
