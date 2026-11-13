package net.farmingtales.p01qa;

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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftp01qa")
@PrefixGameTestTemplate(false)
public final class ParkourTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int WIDTH = 19;
    private static final int HEIGHT = 14;
    private static final int DEPTH = 29;
    private static final int COUNT = WIDTH * HEIGHT * DEPTH;
    private static final int FLAGS = Block.UPDATE_CLIENTS;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw", "structure_void", "spawner", "tnt", "respawn_anchor");
    private static final Set<String> HAZARDS = Set.of("lava", "water", "fire", "soul_fire", "magma_block", "cactus", "powder_snow", "wither_rose", "sweet_berry_bush", "cobweb", "campfire", "soul_campfire");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        BlockPos minimum() {
            BlockPos minimum = world(BlockPos.ZERO);
            for (BlockPos corner : List.of(new BlockPos(WIDTH - 1, 0, 0), new BlockPos(0, 0, DEPTH - 1), new BlockPos(WIDTH - 1, 0, DEPTH - 1))) {
                BlockPos point = world(corner);
                minimum = new BlockPos(Math.min(minimum.getX(), point.getX()), origin.getY(), Math.min(minimum.getZ(), point.getZ()));
            }
            return minimum;
        }
        Vec3i size() {
            return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? new Vec3i(DEPTH, HEIGHT, WIDTH) : new Vec3i(WIDTH, HEIGHT, DEPTH);
        }
        Vec3 centre(BlockPos feet) { return Vec3.atBottomCenterOf(world(feet)); }
        Vec3 forward() { return centre(new BlockPos(9, 5, 7)).subtract(centre(new BlockPos(9, 5, 6))); }
    }

    private static final class ProbePlayer extends FakePlayer {
        ProbePlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.fromString("af6317c1-47f3-4000-bb11-000000000001"), "P01RouteProbe"));
            setGameMode(GameType.SURVIVAL);
            getAbilities().flying = false;
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1D);
            setHealth(20.0F);
        }
        void launch() { jumpFromGround(); }
        int predictedFallDamage(float distance) { return calculateFallDamage(distance, 1.0F); }
    }

    private static Path root() { return Path.of(System.getProperty("ftp01qa.root")).toAbsolutePath().normalize(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static String hash(Path path) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static void report(Map<String, Object> result) throws Exception { Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + "\n", StandardCharsets.UTF_8); }
    private static BlockPos coordinate(ListTag list) { require(list.size() == 3, "Invalid coordinate"); return new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2)); }
    private static boolean inside(BlockPos position) { return position.getX() >= 0 && position.getX() < WIDTH && position.getY() >= 0 && position.getY() < HEIGHT && position.getZ() >= 0 && position.getZ() < DEPTH; }
    private static BlockState decode(CompoundTag encoded) {
        ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
        require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity), "Unknown vanilla ID " + identity);
        require(!FORBIDDEN.contains(identity.getPath()), "Forbidden ID " + identity);
        Block block = BuiltInRegistries.BLOCK.get(identity);
        CompoundTag properties = encoded.getCompound("Properties");
        for (String key : properties.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + identity + " " + key);
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465, "Wrong DataVersion");
        require(coordinate(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(WIDTH, HEIGHT, DEPTH)), "Wrong dimensions");
        require(!data.contains("palettes") && data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Alternate palettes/entities forbidden");
        ListTag palette = data.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> decoded = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) decoded.add(decode(palette.getCompound(index)));
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            BlockPos position = coordinate(record.getList("pos", Tag.TAG_INT));
            int state = record.getInt("state");
            require(inside(position) && state >= 0 && state < decoded.size(), "Invalid voxel");
            require(!record.contains("nbt") && !decoded.get(state).hasBlockEntity(), "Block entity or loot forbidden");
            require(result.put(position, decoded.get(state)) == null, "Duplicate voxel");
        }
        require(result.size() == COUNT, "Missing explicit volume cells");
        return result;
    }
    private static boolean safe(BlockState state) { return state.getFluidState().isEmpty() && !HAZARDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()); }
    private static boolean clear(ServerLevel level, BlockPos point) { BlockState state = level.getBlockState(point); return safe(state) && state.getCollisionShape(level, point).isEmpty(); }
    private static boolean floor(ServerLevel level, BlockPos point) { BlockState state = level.getBlockState(point); return safe(state) && state.isFaceSturdy(level, point, Direction.UP); }
    private static boolean walkable(ServerLevel level, BlockPos feet) { return clear(level, feet) && clear(level, feet.above()) && floor(level, feet.below()); }
    private static boolean empty(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (BlockPos relative : expected.keySet()) if (!level.getBlockState(fixture.world(relative)).isAir()) return false;
        return true;
    }
    private static Set<BlockPos> reachable(ServerLevel level, Fixture fixture) {
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = new BlockPos(9, 5, 0);
        reached.add(start); queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int rise = -1; rise <= 1; rise++) {
                    BlockPos next = current.relative(direction).above(rise);
                    if (!inside(next) || reached.contains(next) || !walkable(level, fixture.world(next))) continue;
                    if (rise > 0 && !clear(level, fixture.world(current.above(2)))) continue;
                    if (rise < 0 && !clear(level, fixture.world(next.above(2)))) continue;
                    reached.add(next); queue.add(next);
                }
            }
        }
        return reached;
    }
    private static Map<String, Object> jump(ServerLevel level, Fixture fixture, int takeoff, int landingStart, boolean reverse) {
        ProbePlayer player = new ProbePlayer(level);
        Vec3 start = fixture.centre(new BlockPos(9, 5, takeoff));
        Vec3 forward = fixture.forward().scale(reverse ? -1 : 1);
        player.setPos(start.x, start.y, start.z);
        player.setYRot((float) Math.toDegrees(Math.atan2(-forward.x, forward.z)));
        player.setOnGround(true);
        player.setSprinting(true);
        player.setSpeed(0.13F);
        player.setDeltaMovement(forward.scale(0.12));
        player.launch();
        double apex = start.y;
        int flightTicks = 0;
        List<List<Double>> trace = new ArrayList<>();
        for (int tick = 1; tick <= 32; tick++) {
            player.travel(new Vec3(0, 0, 1));
            apex = Math.max(apex, player.getY());
            trace.add(List.of(player.getX(), player.getY(), player.getZ()));
            require(level.noCollision(player, player.getBoundingBox()), "Jump body intersects solid blocks");
            if (player.onGround()) { flightTicks = tick; break; }
        }
        require(flightTicks > 0, "Jump did not land");
        Vec3 actual = player.position();
        Vec3 targetFirst = fixture.centre(new BlockPos(9, 5, landingStart));
        double along = actual.subtract(targetFirst).dot(forward);
        double lateral = Math.abs(actual.subtract(start).dot(new Vec3(forward.z, 0, -forward.x)));
        require(Math.abs(actual.y - start.y) < 0.001 && along > -0.20 && along < 2.20 && lateral < 0.05, "Jump missed landing pad: " + fixture.rotation() + " " + takeoff + " -> " + actual + " along=" + along);
        require(player.getHealth() == 20.0F, "Jump incurred health damage");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("takeoff_local_feet", List.of(9, 5, takeoff));
        result.put("landing_first_local_feet", List.of(9, 5, landingStart));
        result.put("reverse", reverse);
        result.put("initial_horizontal_speed", 0.12);
        result.put("flight_ticks", flightTicks);
        result.put("apex_above_deck", apex - start.y);
        result.put("health_after", player.getHealth());
        result.put("actual_landing_world", List.of(actual.x, actual.y, actual.z));
        result.put("trace_world", trace);
        return result;
    }
    private static Map<String, Object> fall(ServerLevel level, Fixture fixture, int gapSouth, boolean afterJump) {
        ProbePlayer player = new ProbePlayer(level);
        Vec3 start = fixture.centre(new BlockPos(9, 5, gapSouth));
        player.setPos(start.x, start.y, start.z);
        player.setOnGround(false);
        player.setDeltaMovement(Vec3.ZERO);
        if (afterJump) { player.setOnGround(true); player.launch(); }
        double apex = start.y;
        int ticks = 0;
        for (int tick = 1; tick <= 40; tick++) {
            player.travel(Vec3.ZERO);
            apex = Math.max(apex,player.getY());
            if (player.onGround()) { ticks = tick; break; }
        }
        require(ticks > 0 && Math.abs(player.getY() - (fixture.origin().getY() + 2)) < 0.001, "Pit landing missed");
        double maximumFall = apex - player.getY();
        int predictedDamage = player.predictedFallDamage((float) maximumFall);
        require(predictedDamage <= (afterJump ? 2 : 0), "Pit fall exceeds safe damage bound");
        return Map.of("gap_south", gapSouth, "ticks_to_land", ticks, "deck_drop_blocks", start.y - player.getY(), "after_jump", afterJump, "maximum_fall_blocks", maximumFall, "vanilla_predicted_damage_hp", predictedDamage, "damage_scope", "Vanilla calculateFallDamage; FakePlayer health is not authoritative");
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected, boolean capture) throws Exception {
        int lights = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos position = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(position);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " actual=" + actual);
            require(actual.canSurvive(level, position), "Unsupported block " + entry.getKey());
            require(safe(actual), "Fluid/hazard present");
            require(level.getBlockEntity(position) == null, "Unexpected block entity");
            if (actual.is(Blocks.LANTERN) || actual.is(Blocks.WALL_TORCH)) lights++;
        }
        for (int south : new int[]{0, 28}) {
            for (int east = 8; east <= 10; east++) {
                require(walkable(level, fixture.world(new BlockPos(east, 5, south))), "Unsafe port");
                for (int up = 5; up <= 8; up++) require(clear(level, fixture.world(new BlockPos(east, up, south))), "Port obstructed");
            }
        }
        for (int south = 0; south < DEPTH; south++) {
            for (int east = 0; east < WIDTH; east++) {
                require(floor(level, fixture.world(new BlockPos(east, 13, south))), "Roof open");
                require(floor(level, fixture.world(new BlockPos(east, 0, south))), "Foundation open");
            }
        }
        Set<BlockPos> reached = reachable(level, fixture);
        require(reached.contains(new BlockPos(9,5,3)), "Interior not reachable");
        require(!reached.contains(new BlockPos(9,5,28)), "Ground route bypasses parkour");
        int recoveryCells = 0;
        for (int east = 6; east <= 12; east++) {
            for (int south = 7; south <= 23; south++) {
                BlockPos feet = new BlockPos(east, 2, south);
                if (!walkable(level, fixture.world(feet))) continue;
                require(reached.contains(feet), "Pit foot cell cannot return " + feet);
                recoveryCells++;
            }
        }
        List<Map<String, Object>> jumps = new ArrayList<>();
        int[] takeoffs = {6, 11, 16, 21};
        for (int takeoff : takeoffs) jumps.add(jump(level, fixture, takeoff, takeoff + 3, false));
        for (int takeoff : takeoffs) jumps.add(jump(level, fixture, takeoff + 3, takeoff, true));
        List<Map<String, Object>> falls = new ArrayList<>();
        for (int south : new int[]{7,8,12,13,17,18,22,23}) {
            falls.add(fall(level, fixture, south, false));
            falls.add(fall(level, fixture, south, true));
        }
        List<Map<String,Object>> walkingSegments = new ArrayList<>();
        for (int[] segment : new int[][]{{0,6},{9,11},{14,16},{19,21},{24,28}}) {
            for (int south = segment[0]; south <= segment[1]; south++) require(walkable(level,fixture.world(new BlockPos(9,5,south))), "Walking segment obstructed");
            walkingSegments.add(Map.of("from_local_feet",List.of(9,5,segment[0]),"to_local_feet",List.of(9,5,segment[1])));
        }
        BlockPos minimum = fixture.minimum();
        Vec3i size = fixture.size();
        AABB bounds = new AABB(minimum.getX()-1, minimum.getY()-1, minimum.getZ()-1, minimum.getX()+size.getX()+1, minimum.getY()+size.getY()+1, minimum.getZ()+size.getZ()+1);
        require(level.getEntities(null, bounds).isEmpty(), "Unexpected spawned/dropped entities");
        for (BlockPos halo : BlockPos.betweenClosed(minimum.offset(-1,-1,-1), minimum.offset(size.getX(),size.getY(),size.getZ()))) require(level.getFluidState(halo).isEmpty(), "Halo fluid");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rotation", fixture.rotation().name());
        result.put("voxel_records_checked", COUNT);
        result.put("lights_supported", lights);
        result.put("recovery_floor_cells", recoveryCells);
        result.put("reachable_feet_cells", reached.size());
        result.put("ground_bypass", false);
        result.put("native_player_jumps", jumps);
        result.put("native_player_falls", falls);
        result.put("walking_segments", walkingSegments);
        result.put("composed_entrance_exit_route", "Five verified walk segments joined by four verified native jumps; reverse jumps checked too");
        result.put("entities_and_fluids", 0);
        if (capture) {
            StructureTemplate placed = new StructureTemplate();
            placed.fillFromWorld(level, minimum, size, false, Blocks.STRUCTURE_VOID);
            CompoundTag saved = placed.save(new CompoundTag());
            saved.putInt("DataVersion", 3465);
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            ListTag records = saved.getList("blocks", Tag.TAG_COMPOUND);
            require(records.size() == COUNT, "Native capture not complete");
            Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
            for (int index = 0; index < records.size(); index++) {
                CompoundTag record = records.getCompound(index);
                captured.put(coordinate(record.getList("pos", Tag.TAG_INT)), decode(palette.getCompound(record.getInt("state"))));
            }
            for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) require(entry.getValue().rotate(fixture.rotation()).equals(captured.get(fixture.world(entry.getKey()).subtract(minimum))), "World capture differs");
            Path output = root().resolve("qa/native-placed-" + fixture.rotation().name().toLowerCase() + ".nbt");
            NbtIo.writeCompressed(saved, output.toFile());
            StructureTemplate roundtrip = new StructureTemplate();
            roundtrip.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(output.toFile()));
            require(roundtrip.getSize().equals(size), "Capture reload failed");
            result.put("world_capture", root().relativize(output).toString());
            result.put("world_capture_sha256", hash(output));
            result.put("capture_matches_source", true);
        }
        return result;
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void fourRotationsStabilityAndRoute(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "P01"); result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1"); result.put("forge", "47.4.10");
        result.put("scope", "Isolated Forge GameTestServer; native fake-player travel calls, not a human client playthrough");
        report(result);
        try {
            ServerLevel level = helper.getLevel();
            require(level.getServer() instanceof GameTestServer, "Refusing player server");
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            level.setDayTime(6000);
            Path source = root().resolve("room.nbt");
            String fingerprint = hash(source);
            JsonObject metadata = JSON.fromJson(Files.readString(root().resolve("room.json")), JsonObject.class);
            require(fingerprint.equals(metadata.get("sha256").getAsString()), "Source hash differs");
            result.put("structure_sha256", fingerprint);
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            StructureTemplate nativeRoundtrip = new StructureTemplate();
            nativeRoundtrip.load(BuiltInRegistries.BLOCK.asLookup(), template.save(new CompoundTag()));
            require(nativeRoundtrip.getSize().equals(template.getSize()), "Native roundtrip size differs");
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> before = new ArrayList<>();
            int index = 0;
            for (Rotation rotation : Rotation.values()) {
                BlockPos horizontal = helper.absolutePos(new BlockPos(80 + index * 96, 0, 80));
                Fixture fixture = new Fixture(new BlockPos(horizontal.getX(), 192, horizontal.getZ()), rotation);
                BlockPos minimum = fixture.minimum(); Vec3i size = fixture.size();
                for (int chunkX = (minimum.getX()-2)>>4; chunkX <= (minimum.getX()+size.getX()+2)>>4; chunkX++) {
                    for (int chunkZ = (minimum.getZ()-2)>>4; chunkZ <= (minimum.getZ()+size.getZ()+2)>>4; chunkZ++) {
                        level.getChunk(chunkX, chunkZ); level.setChunkForced(chunkX, chunkZ, true);
                    }
                }
                require(empty(level, fixture, expected), "Refusing occupied fixture volume");
                BlockPos airProbe = fixture.world(new BlockPos(9, 6, 7));
                level.setBlock(airProbe, Blocks.STONE.defaultBlockState(), FLAGS);
                require(!empty(level, fixture, expected), "Occupied explicit air negative probe missed");
                level.setBlock(airProbe, Blocks.AIR.defaultBlockState(), FLAGS);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), settings, level.random, FLAGS), "Native placement failed");
                before.add(verify(level, fixture, expected, false));
                BlockPos support = fixture.world(new BlockPos(4,10,5));
                BlockPos lamp = fixture.world(new BlockPos(4,9,5));
                BlockState supportState = level.getBlockState(support);
                level.setBlock(support, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!level.getBlockState(lamp).canSurvive(level,lamp), "Missing lamp chain negative probe missed");
                level.setBlock(support, supportState, FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos recoveryProbe = fixture.world(new BlockPos(6,1,23));
                BlockState recoveryState = level.getBlockState(recoveryProbe);
                level.setBlock(recoveryProbe, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_KNOWN_SHAPE);
                require(!walkable(level,fixture.world(new BlockPos(6,2,23))), "Missing recovery floor negative probe missed");
                level.setBlock(recoveryProbe,recoveryState,FLAGS | Block.UPDATE_KNOWN_SHAPE);
                BlockPos jumpProbe = fixture.world(new BlockPos(9,6,8));
                level.setBlock(jumpProbe,Blocks.STONE.defaultBlockState(),FLAGS | Block.UPDATE_KNOWN_SHAPE);
                boolean obstructionDetected = false;
                try { jump(level,fixture,6,9,false); } catch (IllegalStateException expectedFailure) { obstructionDetected = true; }
                require(obstructionDetected,"Native jump obstruction negative probe missed");
                level.setBlock(jumpProbe,Blocks.AIR.defaultBlockState(),FLAGS | Block.UPDATE_KNOWN_SHAPE);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos world = fixture.world(relative);
                    level.getBlockState(world).updateNeighbourShapes(level,world,Block.UPDATE_ALL);
                }
                fixtures.add(fixture); index++;
            }
            result.put("initial_rotations", before);
            result.put("negative_probes", Map.of("occupied_air_detected",4,"missing_lamp_support_detected",4,"missing_recovery_floor_detected",4,"blocked_jump_detected",4));
            result.put("stabilization_ticks",200);
            result.put("forced_chunk_ticking",true);
            report(result);
            helper.runAfterDelay(200, () -> {
                try {
                    List<Map<String,Object>> after = new ArrayList<>();
                    for (Fixture fixture : fixtures) after.add(verify(level,fixture,expected,true));
                    require(hash(source).equals(fingerprint),"Source changed during test");
                    result.put("rotations",after);
                    result.put("records_checked_after_ticks",COUNT * after.size());
                    result.put("native_jump_passes_after_ticks",32);
                    result.put("native_fall_passes_after_ticks",64);
                    result.put("source_unchanged",true);
                    result.put("status","PASS"); report(result);
                    System.out.println("P01_NATIVE_PASS rotations=4/4 voxels=30856 jumps=32 falls=64 ticks=200 sha256="+fingerprint);
                    helper.succeed();
                } catch (Exception error) {
                    result.put("status","FAIL"); result.put("error",error.toString());
                    try { report(result); } catch (Exception secondary) { error.addSuppressed(secondary); }
                    throw new IllegalStateException(error);
                }
            });
        } catch (Exception error) {
            result.put("status","FAIL"); result.put("error",error.toString()); report(result); throw error;
        }
    }
}

