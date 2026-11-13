package net.farmingtales.zonep04qa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
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
import java.util.UUID;
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
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzonep04qa")
@PrefixGameTestTemplate(false)
public final class LedgeTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int RECORDS = 23 * 16 * 27;
    private static final Set<String> FORBIDDEN = Set.of("command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "water", "lava", "fire", "soul_fire", "magma_block", "campfire", "soul_campfire", "cobweb", "powder_snow", "sand", "gravel", "chest", "barrel");

    private record Fixture(BlockPos origin, Rotation rotation) {
        BlockPos world(BlockPos relative) {
            return origin.offset(StructureTemplate.transform(relative, Mirror.NONE, rotation, BlockPos.ZERO));
        }
        Vec3 vector(Vec3 input) {
            return switch (rotation) {
                case NONE -> input;
                case CLOCKWISE_90 -> new Vec3(-input.z, input.y, input.x);
                case CLOCKWISE_180 -> new Vec3(-input.x, input.y, -input.z);
                case COUNTERCLOCKWISE_90 -> new Vec3(input.z, input.y, -input.x);
            };
        }
        Vec3 point(double posX, double posY, double posZ) {
            return vector(new Vec3(posX - .5, posY, posZ - .5)).add(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
        }
        Vec3 local(Vec3 input) {
            Vec3 relative = input.subtract(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
            Vec3 rotated = switch (rotation) {
                case NONE -> relative;
                case CLOCKWISE_90 -> new Vec3(relative.z, relative.y, -relative.x);
                case CLOCKWISE_180 -> new Vec3(-relative.x, relative.y, -relative.z);
                case COUNTERCLOCKWISE_90 -> new Vec3(-relative.z, relative.y, relative.x);
            };
            return rotated.add(.5, 0, .5);
        }
    }

    private LedgeTests() {}

    private static Path root() {
        return Path.of(System.getProperty("ftzonep04qa.root")).toAbsolutePath().normalize();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static void report(Map<String, Object> result) throws Exception {
        Files.writeString(root().resolve("qa/native.json"), JSON.toJson(result) + System.lineSeparator(), StandardCharsets.UTF_8);
    }
    private static BlockPos position(ListTag values) {
        return new BlockPos(values.getInt(0), values.getInt(1), values.getInt(2));
    }
    private static Map<BlockPos, BlockState> audit(CompoundTag data) {
        require(data.getInt("DataVersion") == 3465 && !data.contains("palettes"), "Not 1.20.1 single palette");
        require(position(data.getList("size", Tag.TAG_INT)).equals(new BlockPos(23, 16, 27)), "Wrong size");
        require(data.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Entities in template");
        List<BlockState> palette = new ArrayList<>();
        ListTag encodedPalette = data.getList("palette", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedPalette.size(); index++) {
            CompoundTag encoded = encodedPalette.getCompound(index);
            ResourceLocation identity = new ResourceLocation(encoded.getString("Name"));
            require(identity.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identity) && !FORBIDDEN.contains(identity.getPath()), "Unknown or forbidden block " + identity);
            Block block = BuiltInRegistries.BLOCK.get(identity);
            CompoundTag properties = encoded.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid state " + identity + ":" + key);
            }
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encoded);
            require(state.getFluidState().isEmpty() && !state.hasBlockEntity(), "Fluid or block entity state");
            palette.add(state);
        }
        Map<BlockPos, BlockState> expected = new LinkedHashMap<>();
        ListTag records = data.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.getCompound(index);
            BlockPos relative = position(record.getList("pos", Tag.TAG_INT));
            require(relative.getX() >= 0 && relative.getX() < 23 && relative.getY() >= 0 && relative.getY() < 16 && relative.getZ() >= 0 && relative.getZ() < 27, "Out of bounds");
            require(!record.contains("nbt"), "Unexpected block entity payload");
            require(record.getInt("state") >= 0 && record.getInt("state") < palette.size(), "Palette index invalid");
            require(expected.put(relative, palette.get(record.getInt("state"))) == null, "Duplicate coordinate");
        }
        require(expected.size() == RECORDS, "Incomplete explicit volume");
        return expected;
    }
    private static AABB bounds(Fixture fixture) {
        BlockPos first = fixture.world(BlockPos.ZERO);
        BlockPos last = fixture.world(new BlockPos(22, 15, 26));
        return new AABB(Math.min(first.getX(), last.getX()), 160, Math.min(first.getZ(), last.getZ()), Math.max(first.getX(), last.getX()) + 1, 176, Math.max(first.getZ(), last.getZ()) + 1);
    }
    private static AABB playerBox(Vec3 feet) {
        return new AABB(feet.x - .3, feet.y + .0001, feet.z - .3, feet.x + .3, feet.y + 1.8, feet.z + .3);
    }
    private static void walk(ServerLevel level, Fixture fixture, Vec3 from, Vec3 to) {
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) / .1));
        for (int step = 0; step <= steps; step++) {
            Vec3 local = from.lerp(to, (double) step / steps);
            Vec3 feet = fixture.point(local.x, local.y, local.z);
            AABB body = playerBox(feet);
            require(level.noCollision(body), "Walking collision " + fixture.rotation() + " " + local);
            require(!level.noCollision(body.move(0, -.05, 0)), "Walking floor missing " + local);
        }
    }
    private static FakePlayer player(ServerLevel level, Fixture fixture, Vec3 feet) {
        String identity = "P04-" + fixture.rotation();
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)), "P04Probe"));
        Vec3 world = fixture.point(feet.x, feet.y, feet.z);
        player.setPos(world.x, world.y, world.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.setOnGround(true);
        player.fallDistance = 0;
        require(Math.abs(player.getBbWidth() - .6) < .001 && Math.abs(player.getBbHeight() - 1.8) < .001, "Wrong player body");
        return player;
    }
    private static Vec3 jump(ServerLevel level, Fixture fixture, double takeoffZ, double feetY, double landingMin, double landingMax, double landingY) {
        FakePlayer player = player(level, fixture, new Vec3(3.5, feetY, takeoffZ));
        player.setOnGround(false);
        double velocityY = .42;
        for (int tick = 1; tick <= 30; tick++) {
            Vec3 movement = fixture.vector(new Vec3(0, velocityY, .21));
            player.setDeltaMovement(movement);
            player.move(MoverType.SELF, movement);
            Vec3 local = fixture.local(player.position());
            require(!player.horizontalCollision, "Jump blocked " + local);
            if (player.onGround() && velocityY < 0) {
                require(Math.abs(local.y - landingY) < .001 && local.z >= landingMin && local.z <= landingMax, "Jump landed off target " + local);
                require(level.noCollision(player.getBoundingBox().deflate(.0001)), "Landing body collision");
                return local;
            }
            velocityY = (velocityY - .08) * .98;
        }
        throw new IllegalStateException("Jump never landed");
    }
    private static Vec3 stairWalk(ServerLevel level, Fixture fixture, Vec3 from, Vec3 to) {
        FakePlayer player = player(level, fixture, from);
        Vec3 offset = to.subtract(from);
        Vec3 direction = new Vec3(offset.x, 0, offset.z).normalize();
        double velocityY = 0;
        int steps = (int) Math.ceil(Math.sqrt(offset.x * offset.x + offset.z * offset.z) / .1);
        for (int step = 0; step < steps; step++) {
            double remaining = Math.sqrt(Math.pow(to.x - fixture.local(player.position()).x, 2) + Math.pow(to.z - fixture.local(player.position()).z, 2));
            double speed = Math.min(.1, remaining);
            velocityY = player.onGround() ? -.08 : (velocityY - .08) * .98;
            Vec3 movement = fixture.vector(new Vec3(direction.x * speed, velocityY, direction.z * speed));
            player.setDeltaMovement(movement);
            player.move(MoverType.SELF, movement);
        }
        for (int tick = 0; tick < 20 && !player.onGround(); tick++) {
            velocityY = (velocityY - .08) * .98;
            player.move(MoverType.SELF, new Vec3(0, velocityY, 0));
        }
        Vec3 result = fixture.local(player.position());
        require(Math.abs(result.x - to.x) < .02 && Math.abs(result.z - to.z) < .02 && Math.abs(result.y - to.y) < .02, "Stairs failed " + result + " expected " + to);
        return result;
    }
    private static Map<String, Object> route(ServerLevel level, Fixture fixture) {
        for (int posZ : new int[] {0, 1, 26}) {
            for (int posX = 10; posX <= 12; posX++) {
                BlockPos floor = fixture.world(new BlockPos(posX, 4, posZ));
                require(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Connector floor missing");
                for (int posY = 5; posY <= 7; posY++) require(level.getBlockState(fixture.world(new BlockPos(posX, posY, posZ))).isAir(), "Connector blocked");
            }
        }
        walk(level, fixture, new Vec3(11.5, 5, .5), new Vec3(11.5, 5, 3.5));
        walk(level, fixture, new Vec3(11.5, 5, 3.5), new Vec3(3.5, 5, 3.5));
        walk(level, fixture, new Vec3(3.5, 5, 3.5), new Vec3(3.5, 5, 7.65));
        double[][] jumps = {{7.65, 5, 9.3, 11.7, 5}, {11.65, 5, 13.3, 15.7, 6}, {15.65, 6, 17.3, 19.7, 6}, {19.65, 6, 21.3, 23.7, 6}};
        List<Map<String, Object>> landings = new ArrayList<>();
        Vec3 last = null;
        for (int index = 0; index < jumps.length; index++) {
            double[] parameters = jumps[index];
            if (last != null) walk(level, fixture, last, new Vec3(3.5, parameters[1], parameters[0]));
            last = jump(level, fixture, parameters[0], parameters[1], parameters[2], parameters[3], parameters[4]);
            landings.add(Map.of("jump", index + 1, "landing_local", List.of(last.x, last.y, last.z), "horizontal_velocity", .21, "initial_jump_velocity", .42, "sprint", false));
        }
        walk(level, fixture, last, new Vec3(3.5, 6, 23.5));
        walk(level, fixture, new Vec3(3.5, 6, 23.5), new Vec3(11.5, 6, 23.5));
        walk(level, fixture, new Vec3(11.5, 6, 23.5), new Vec3(11.5, 6, 24.5));
        stairWalk(level, fixture, new Vec3(11.5, 6, 24.5), new Vec3(11.5, 5, 26.5));
        List<Map<String, Object>> falls = new ArrayList<>();
        for (int gapZ : new int[] {8, 12, 16, 20}) {
            double height = gapZ >= 16 ? 6 : 5;
            FakePlayer falling = player(level, fixture, new Vec3(3.5, height, gapZ + .5));
            falling.setOnGround(false);
            double velocityY = 0;
            for (int tick = 0; tick < 40 && !falling.onGround(); tick++) {
                velocityY = (velocityY - .08) * .98;
                falling.move(MoverType.SELF, new Vec3(0, velocityY, 0));
            }
            Vec3 landed = fixture.local(falling.position());
            require(falling.onGround() && Math.abs(landed.y - 3) < .001 && height - landed.y <= 3.001, "Unsafe failure landing " + landed);
            walk(level, fixture, landed, new Vec3(7.5, 3, gapZ + .5));
            walk(level, fixture, new Vec3(7.5, 3, gapZ + .5), new Vec3(7.5, 3, 12.5));
            walk(level, fixture, new Vec3(7.5, 3, 12.5), new Vec3(18.5, 3, 12.5));
            walk(level, fixture, new Vec3(18.5, 3, 12.5), new Vec3(18.5, 3, 8.5));
            falls.add(Map.of("gap_z", gapZ, "landing_feet_y", landed.y, "drop", height - landed.y, "recovery_walk", "PASS"));
        }
        stairWalk(level, fixture, new Vec3(18.5, 3, 8.5), new Vec3(18.5, 5, 4.5));
        stairWalk(level, fixture, new Vec3(18.5, 5, 4.5), new Vec3(18.5, 3, 8.5));
        walk(level, fixture, new Vec3(18.5, 5, 4.5), new Vec3(11.5, 5, 4.5));
        walk(level, fixture, new Vec3(11.5, 5, 4.5), new Vec3(11.5, 5, 3.5));
        for (Vec3 safe : List.of(new Vec3(11.5, 5, 3.5), new Vec3(4.5, 6, 14.5), new Vec3(4.5, 6, 22.5))) walk(level, fixture, safe, safe);
        return Map.of("status", "PASS", "jumps", landings, "failures_and_recovery", falls, "stairs_both_directions", true, "entry_safe_return", true, "exit_reached", true);
    }
    private static void mounts(ServerLevel level, Fixture fixture) {
        for (int[] position : new int[][] {{3,3},{3,14},{3,23},{19,3},{19,14},{19,23}}) {
            require(level.getBlockState(fixture.world(new BlockPos(position[0], 10, position[1]))).is(Blocks.LANTERN), "Lantern missing");
            for (int posY = 11; posY <= 13; posY++) require(level.getBlockState(fixture.world(new BlockPos(position[0], posY, position[1]))).is(Blocks.CHAIN), "Chain missing");
            for (int posY = 14; posY <= 15; posY++) require(level.getBlockState(fixture.world(new BlockPos(position[0], posY, position[1]))).is(Blocks.STONE_BRICKS) || level.getBlockState(fixture.world(new BlockPos(position[0], posY, position[1]))).is(Blocks.MOSSY_STONE_BRICKS) || level.getBlockState(fixture.world(new BlockPos(position[0], posY, position[1]))).is(Blocks.CRACKED_STONE_BRICKS), "Ceiling mount missing");
        }
    }
    private static Map<String, Object> verify(ServerLevel level, Fixture fixture, Map<BlockPos, BlockState> expected) {
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            BlockPos world = fixture.world(entry.getKey());
            BlockState actual = level.getBlockState(world);
            require(actual.equals(entry.getValue().rotate(fixture.rotation())), "State drift " + fixture.rotation() + " " + entry.getKey() + " actual=" + actual + " expected=" + entry.getValue().rotate(fixture.rotation()));
            require(actual.canSurvive(level, world), "Unsupported block " + world);
            require(actual.getFluidState().isEmpty() && level.getBlockEntity(world) == null, "Fluid or block entity");
        }
        mounts(level, fixture);
        Map<String, Object> route = route(level, fixture);
        require(level.getEntities(null, bounds(fixture)).isEmpty(), "Dropped attachments or entities appeared");
        return Map.of("rotation", fixture.rotation().name(), "status", "PASS", "records_checked", expected.size(), "state_drift", 0, "route", route);
    }
    private static int probes(ServerLevel level, Fixture fixture) {
        int count = 0;
        for (BlockPos relative : List.of(new BlockPos(11, 4, 0), new BlockPos(3, 6, 8), new BlockPos(18, 4, 7), new BlockPos(3, 12, 14))) {
            BlockPos world = fixture.world(relative);
            BlockState saved = level.getBlockState(world);
            boolean chain = relative.getY() == 12;
            level.setBlock(world, relative.getY() == 4 && relative.getZ() == 0 || chain ? Blocks.AIR.defaultBlockState() : Blocks.STONE.defaultBlockState(), FLAGS);
            boolean rejected = false;
            try {
                if (chain) mounts(level, fixture);
                else route(level, fixture);
            } catch (IllegalStateException expected) {
                rejected = true;
            } finally {
                level.setBlock(world, saved, FLAGS);
            }
            require(rejected, "Negative probe undetected " + relative);
            count++;
        }
        return count;
    }
    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void crumblingLedgeRoom(GameTestHelper helper) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "RUNNING");
        result.put("minecraft", "1.20.1");
        result.put("forge", "47.4.10");
        result.put("test_world_only", true);
        result.put("stabilization_ticks", 400);
        result.put("route_method", "Forge FakePlayer 0.6x1.8 entity, vanilla Entity.move collision, walk-jump velocity .21/.42 with .08 gravity and .98 vertical drag; scripted probes, not client keyboard playthrough.");
        try {
            Path source = root().resolve("room.nbt");
            String sourceHash = hash(source);
            CompoundTag data = NbtIo.readCompressed(source.toFile());
            Map<BlockPos, BlockState> expected = audit(data);
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK.asLookup(), data);
            ServerLevel level = helper.getLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(true, level.getServer());
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, level.getServer());
            List<Fixture> fixtures = new ArrayList<>();
            List<Map<String, Object>> initial = new ArrayList<>();
            int negativeProbes = 0;
            for (Rotation rotation : Rotation.values()) {
                Fixture fixture = new Fixture(new BlockPos(2048 + fixtures.size() * 80, 160, 2048), rotation);
                AABB box = bounds(fixture);
                for (int chunkX = ((int) box.minX) >> 4; chunkX <= (((int) box.maxX) - 1) >> 4; chunkX++) {
                    for (int chunkZ = ((int) box.minZ) >> 4; chunkZ <= (((int) box.maxZ) - 1) >> 4; chunkZ++) level.setChunkForced(chunkX, chunkZ, true);
                }
                for (BlockPos relative : expected.keySet()) level.setBlock(fixture.world(relative), Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_SUPPRESS_DROPS);
                level.getEntities(null, box).forEach(entity -> entity.discard());
                require(template.placeInWorld(level, fixture.origin(), fixture.origin(), new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKeepLiquids(false), level.random, FLAGS), "Placement failed");
                initial.add(verify(level, fixture, expected));
                negativeProbes += probes(level, fixture);
                for (BlockPos relative : expected.keySet()) {
                    BlockPos world = fixture.world(relative);
                    level.updateNeighborsAt(world, level.getBlockState(world).getBlock());
                }
                fixtures.add(fixture);
                System.out.println("ZONE_P04_PLACED " + rotation);
            }
            result.put("room_sha256", sourceHash);
            result.put("initial", initial);
            result.put("negative_probes", negativeProbes);
            result.put("negative_probe_types", List.of("missing entry floor", "blocked jump headroom", "blocked recovery stairs", "missing light chain"));
            result.put("placement_policy", "Fresh isolated fixtures with forced ticking chunks; native placeInWorld known-shape initial placement followed by normal neighbor updates for every record; random ticks=3 and block drops enabled.");
            report(result);
            helper.runAfterDelay(400, () -> {
                try {
                    List<Map<String, Object>> rotations = new ArrayList<>();
                    for (Fixture fixture : fixtures) rotations.add(verify(level, fixture, expected));
                    require(hash(source).equals(sourceHash), "Source changed during test");
                    result.put("status", "PASS");
                    result.put("rotations", rotations);
                    result.put("records_checked_after_ticks", expected.size() * rotations.size());
                    result.put("state_drift", 0);
                    result.put("limitations", List.of("Scripted native collision probes are not full-modpack client or multiplayer playtests.", "No FT hall links, occupied-volume rejection, idempotence, automatic checkpoints or deployment are tested."));
                    report(result);
                    System.out.println("ZONE_P04_NATIVE_PASS rotations=4 ticks=400 records=" + RECORDS * 4 + " negative_probes=16 sha256=" + sourceHash);
                    helper.succeed();
                } catch (Exception exception) {
                    result.put("status", "FAIL");
                    result.put("error", exception.toString());
                    try {report(result);} catch (Exception secondary) {exception.addSuppressed(secondary);}
                    throw new IllegalStateException(exception);
                }
            });
        } catch (Exception exception) {
            result.put("status", "FAIL");
            result.put("error", exception.toString());
            report(result);
            throw exception;
        }
    }
}
