package net.farmingtales.corridors09qa;

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
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftcorridors09qa")
@PrefixGameTestTemplate(false)
public final class CorridorS09Tests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(11, 9, 16);
    private static final Set<Block> HAZARDS = Set.of(Blocks.COBWEB, Blocks.FIRE, Blocks.SOUL_FIRE,
            Blocks.LAVA, Blocks.WATER, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE,
            Blocks.POWDER_SNOW, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE);
    private static final Set<Block> FORBIDDEN = Set.of(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.SPAWNER, Blocks.TNT, Blocks.CHEST, Blocks.TRAPPED_CHEST,
            Blocks.BARREL, Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER);

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
        BlockPos start = new BlockPos(5, 1, 0);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(start);
        pending.add(start);
        require(standable(level, transform(start, placement)), "North entrance not standable");
        while (!pending.isEmpty()) {
            BlockPos current = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (next.getX() < 0 || next.getX() >= 11 || next.getZ() < 0 || next.getZ() >= 16
                        || visited.contains(next)) continue;
                if (standable(level, transform(next, placement))) {
                    visited.add(next);
                    pending.add(next);
                }
            }
        }
        for (int blockX = 4; blockX <= 6; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                BlockPos local = new BlockPos(blockX, 1, blockZ);
                BlockPos feet = transform(local, placement);
                require(visited.contains(local) && standable(level, feet), "Main path disconnected: " + local);
                require(level.getBlockState(feet.above(2)).isAir(), "Main path lacks 3-block clearance: " + local);
            }
        }
        require(visited.contains(new BlockPos(5, 1, 2)) && visited.contains(new BlockPos(5, 1, 15)), "Safety point or south exit unreachable");
        return visited.size();
    }

    private static Map<String, Object> inspect(ServerLevel level, Placement placement) {
        int lanterns = 0;
        int chains = 0;
        int bars = 0;
        int trapdoors = 0;
        int skulls = 0;
        int webs = 0;
        for (Map.Entry<BlockPos, BlockState> entry : placement.expected().entrySet()) {
            BlockPos position = entry.getKey();
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State changed at " + position + ": " + entry.getValue() + " -> " + actual);
            require(actual.getFluidState().isEmpty(), "Unexpected liquid or waterlogging");
            require(actual.canSurvive(level, position), "Unsupported block: " + actual + " at " + position);
            require(!FORBIDDEN.contains(actual.getBlock()), "Forbidden block placed");
            if (actual.is(Blocks.CHAIN)) {
                chains++;
                require(actual.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y, "Nonvertical suspension");
                BlockPos above = position.above();
                require(level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN), "Chain lacks anchored crossbeam");
            }
            if (actual.is(Blocks.SOUL_LANTERN)) {
                lanterns++;
                require(actual.getValue(BlockStateProperties.HANGING) && level.getBlockState(position.above()).is(Blocks.CHAIN), "Lantern is not suspended");
            }
            if (actual.is(Blocks.IRON_BARS)) bars++;
            if (actual.is(Blocks.IRON_TRAPDOOR)) {
                trapdoors++;
                require(!actual.getValue(BlockStateProperties.OPEN) && !actual.getValue(BlockStateProperties.POWERED), "Cage grate opened or powered");
            }
            if (actual.is(Blocks.COBWEB)) webs++;
            if (actual.is(Blocks.SKELETON_SKULL)) {
                skulls++;
                require(level.getBlockEntity(position) instanceof SkullBlockEntity, "Skull block entity missing");
                require(level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP), "Bone pile lacks floor");
            } else {
                require(level.getBlockEntity(position) == null, "Unexpected block entity");
            }
        }
        require(lanterns == 3 && chains == 7 && bars == 36 && trapdoors == 16 && skulls == 6 && webs > 40,
                "Theme feature count mismatch");
        for (int blockX = 0; blockX < 11; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                BlockPos roof = transform(new BlockPos(blockX, 8, blockZ), placement);
                BlockPos floor = transform(new BlockPos(blockX, 0, blockZ), placement);
                require(level.getBlockState(roof).isFaceSturdy(level, roof, Direction.DOWN), "Roof gap");
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Floor gap");
            }
        }
        for (int sideX : new int[] {0, 10}) {
            for (int blockY = 1; blockY < 8; blockY++) {
                for (int blockZ = 0; blockZ < 16; blockZ++) {
                    BlockPos wall = transform(new BlockPos(sideX, blockY, blockZ), placement);
                    require(level.getBlockState(wall).isCollisionShapeFullBlock(level, wall), "Side opening or missing shell");
                }
            }
        }
        for (int startX : new int[] {2, 7}) {
            for (int startZ : new int[] {3, 11}) {
                int anchorX = startX == 2 ? 3 : 7;
                int anchorZ = startZ == 3 ? 3 : 12;
                require(level.getBlockState(transform(new BlockPos(anchorX, 6, anchorZ), placement)).is(Blocks.CHAIN), "Cage missing suspension");
                for (int blockX = startX; blockX <= startX + 1; blockX++) {
                    for (int blockZ = startZ; blockZ <= startZ + 1; blockZ++) {
                        BlockState base = level.getBlockState(transform(new BlockPos(blockX, 2, blockZ), placement));
                        require(base.is(Blocks.IRON_TRAPDOOR) && base.getValue(BlockStateProperties.HALF) == net.minecraft.world.level.block.state.properties.Half.TOP, "Cage floor does not meet bars");
                        require(level.getBlockState(transform(new BlockPos(blockX, 5, blockZ), placement)).is(Blocks.IRON_BLOCK), "Cage lid does not meet bars and chain");
                        for (int blockY = 3; blockY <= 4; blockY++) {
                            require(level.getBlockState(transform(new BlockPos(blockX, blockY, blockZ), placement)).is(Blocks.IRON_BARS), "Cage missing wall");
                        }
                    }
                }
            }
        }
        BlockPos first = transform(BlockPos.ZERO, placement);
        BlockPos last = transform(SIZE.offset(-1, -1, -1), placement);
        AABB bounds = new AABB(Math.min(first.getX(), last.getX()), first.getY(), Math.min(first.getZ(), last.getZ()),
                Math.max(first.getX(), last.getX()) + 1, last.getY() + 1, Math.max(first.getZ(), last.getZ()) + 1);
        require(level.getEntitiesOfClass(ItemEntity.class, bounds.inflate(2)).isEmpty(), "Placement produced item drops");
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("rotation", placement.rotation().name());
        evidence.put("states_preserved", placement.expected().size());
        evidence.put("collision_reachable_positions", walk(level, placement));
        evidence.put("protected_route", "3 wide x 3 high x 16 long, north to south, dry and hazard-free");
        evidence.put("anchored_chains", chains);
        evidence.put("supported_soul_lanterns", lanterns);
        evidence.put("cages", 4);
        evidence.put("stable_iron_bars", bars);
        evidence.put("closed_trapdoors", trapdoors);
        evidence.put("skull_block_entities", skulls);
        evidence.put("off_route_cobwebs", webs);
        evidence.put("item_drops", 0);
        evidence.put("floor_roof_side_shell", "PASS");
        return evidence;
    }

    @GameTest(template = "empty", timeoutTicks = 420)
    public static void fourRotationsStable(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        require(level.getServer() instanceof GameTestServer, "Only an isolated GameTest server is allowed");
        Path root = Path.of(System.getProperty("ftcorridors09qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("corridor-S09") && Files.exists(root.resolve("forge/build.gradle")), "Wrong isolated workspace");
        Path source = root.resolve("room.nbt");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata = JSON.fromJson(Files.readString(root.resolve("room.json")), JsonObject.class);
        require(metadata.get("id").getAsString().equals("corridor-S09") && metadata.get("sha256").getAsString().equals(digest), "Metadata/hash mismatch");
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
            require(!FORBIDDEN.contains(block), "Forbidden palette entry");
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
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == 1584, "Native roundtrip lost records");
        NbtIo.writeCompressed(roundtrip, root.resolve("qa/native-roundtrip.nbt").toFile());
        ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockPos, BlockState> local = new LinkedHashMap<>();
        for (int recordIndex = 0; recordIndex < records.size(); recordIndex++) {
            CompoundTag record = records.getCompound(recordIndex);
            ListTag coordinates = record.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            require(position.getX() >= 0 && position.getX() < 11 && position.getY() >= 0 && position.getY() < 9
                    && position.getZ() >= 0 && position.getZ() < 16, "Out-of-bounds record");
            require(local.put(position, states.get(record.getInt("state"))) == null, "Duplicate record");
            if (record.contains("nbt")) {
                CompoundTag blockEntity = record.getCompound("nbt");
                require(blockEntity.size() == 1 && blockEntity.getString("id").equals("minecraft:skull"), "Unexpected block entity payload");
            }
        }
        require(local.size() == 1584, "Incomplete volume");
        List<Placement> placements = new ArrayList<>();
        Rotation[] rotations = Rotation.values();
        BlockPos[] origins = {new BlockPos(8, 2, 8), new BlockPos(48, 2, 8), new BlockPos(48, 2, 48), new BlockPos(8, 2, 48)};
        Set<BlockPos> occupied = new HashSet<>();
        for (int placementIndex = 0; placementIndex < rotations.length; placementIndex++) {
            Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
            Placement placement = new Placement(helper.absolutePos(origins[placementIndex]), rotations[placementIndex], expected);
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
        System.out.println("S09_NATIVE_INITIAL_PASS: four rotations, 6336 records; forced neighbour updates");
        helper.runAfterDelay(240, () -> {
            try {
                List<Map<String, Object>> rotationsEvidence = new ArrayList<>();
                for (Placement placement : placements) rotationsEvidence.add(inspect(level, placement));
                require(digest.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)))), "Source changed during QA");
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("id", "corridor-S09");
                report.put("status", "PASS");
                report.put("sha256", digest);
                report.put("minecraft", "1.20.1");
                report.put("forge", "47.4.10");
                report.put("ticks_observed", 240);
                report.put("registry_properties_and_native_roundtrip", "PASS");
                report.put("forced_neighbor_updates", true);
                report.put("rotations", rotationsEvidence);
                report.put("preplaced_entities", 0);
                report.put("containers_items_and_loot_tables", 0);
                report.put("client_visual_tested", false);
                report.put("isolated_world_only", true);
                report.put("limits", List.of("Not a full modpack client, FPS or multiplayer play test", "No production JAR, core integration or player save modified"));
                Files.writeString(root.resolve("qa/forge-native.json"), JSON.toJson(report) + "\n", StandardCharsets.UTF_8);
                System.out.println("S09_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256=" + digest);
                helper.succeed();
            } catch (Exception exception) {
                helper.fail(exception.toString());
            }
        });
    }
}
