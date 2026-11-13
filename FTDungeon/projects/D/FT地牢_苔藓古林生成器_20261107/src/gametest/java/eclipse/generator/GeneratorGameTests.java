package eclipse.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fteclipsegen")
@PrefixGameTestTemplate(false)
public final class GeneratorGameTests {
    private static final long SEED = 20261105L;
    private static final BlockPos ANCHOR = new BlockPos(0, 100, 0);

    private static int[] vector(JsonArray array) {
        return new int[] {array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt()};
    }

    private static JsonObject reference() throws Exception {
        var input = GeneratorGameTests.class.getResourceAsStream("/data/fteclipsegen/reference-test.json");
        if (input == null) {
            throw new IllegalStateException("Missing packaged reference-test.json");
        }
        try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void nativeReferenceTopology(GameTestHelper helper) throws Exception {
        JsonObject golden = reference();
        var generator = new PrisonGridGenerator(0, 0);
        generator.addRooms(RandomSource.create(golden.get("layout_seed").getAsLong()));
        JsonArray rooms = golden.getAsJsonArray("rooms");
        helper.assertTrue(generator.getVersion() == golden.get("generator_version").getAsInt(), "Native generator version matches golden");
        helper.assertTrue(generator.rooms.size() == golden.get("room_count").getAsInt(), "Exact golden room count");
        helper.assertTrue(generator.rooms.size() == rooms.size(), "Golden fixture count is internally consistent");
        Set<Integer> rotations = new HashSet<>();
        for (int index = 0; index < rooms.size(); index++) {
            JsonObject expected = rooms.get(index).getAsJsonObject();
            var actual = generator.rooms.get(index);
            int[] grid = vector(expected.getAsJsonArray("grid"));
            helper.assertTrue(actual.pos.equals(Catalog.pos(grid)), "Golden grid at room " + index);
            helper.assertTrue(actual.poolId.toString().equals(expected.get("pool").getAsString()), "Golden pool at room " + index);
            int rotation = actual.rotation.ordinal();
            helper.assertTrue(rotation == expected.get("rotation").getAsInt(), "Golden facing at room " + index);
            rotations.add(rotation);
            BlockPos original = new BlockPos((grid[0] - 32) * 16, grid[1] * 16, (grid[2] - 32) * 16).offset(actual.offset);
            helper.assertTrue(original.equals(Catalog.pos(vector(expected.getAsJsonArray("origin")))), "Golden unlifted world origin at room " + index);
        }
        helper.assertTrue(rotations.size() == 4, "Golden topology exercises all four directions");
        var plan = Planner.create(golden.get("world_seed").getAsLong(), new BlockPos(0, 238, 0));
        helper.assertTrue(plan.layoutSeed == golden.get("layout_seed").getAsLong(), "Planner uses original region layout seed");
        helper.assertTrue(plan.pieces.size() == rooms.size() + 1, "Only the independent boss extends the golden topology");
        for (int index = 0; index < rooms.size(); index++) {
            JsonObject expected = rooms.get(index).getAsJsonObject();
            var actual = plan.pieces.get(index);
            String template = expected.get("template").getAsString().replace("minecells:better_prison/", "");
            String child = expected.has("child_template") ? expected.get("child_template").getAsString().replace("minecells:better_prison/", "") : null;
            BlockPos origin = Catalog.pos(vector(expected.getAsJsonArray("origin")));
            if (template.startsWith("spawn/outside_")) {
                origin = origin.above(2);
            }
            helper.assertTrue(Arrays.equals(actual.grid, vector(expected.getAsJsonArray("grid"))) && actual.rotation == expected.get("rotation").getAsInt(), "Planner golden grid/facing at " + index);
            helper.assertTrue(template.equals(actual.template) && java.util.Objects.equals(child, actual.child), "Original weighted parent/child selection at " + index);
            helper.assertTrue(Catalog.pos(actual.origin).equals(origin), "Planner golden placement origin at " + index);
            helper.assertTrue(actual.pieceSeed == expected.get("piece_seed").getAsInt() && actual.forkSeed == expected.get("jigsaw_fork_seed").getAsLong(), "Original world-origin piece seed and fork consumption at " + index);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void parentChildRotationsAndPlanBounds(GameTestHelper helper) {
        var catalog = Catalog.get();
        helper.assertTrue(catalog.templates.size() == 27, "All 27 frozen variants loaded");
        Set<String> children = new HashSet<>();
        for (var template : catalog.templates.values()) {
            if (!"straight/room".equals(template.template)) {
                continue;
            }
            helper.assertTrue(template.child != null, "Composed parent has a child identity");
            children.add(template.child);
            var child = catalog.templates.get(template.child);
            helper.assertTrue(child != null, "Child source is packaged");
            var parentCells = Catalog.blocks(template.key).stream().collect(java.util.stream.Collectors.toMap(Catalog.Cell::position, Catalog.Cell::state));
            int compared = 0;
            for (var cell : Catalog.blocks(child.key)) {
                BlockPos local = cell.position().offset(3, 1, 2);
                if (local.getX() < 3 || local.getX() > 12 || local.getZ() < 3 || local.getZ() > 12) {
                    continue;
                }
                helper.assertTrue(cell.state().equals(parentCells.get(local)), "Frozen child interior retained in " + template.key + " at " + local);
                for (int rotation = 0; rotation < 4; rotation++) {
                    int[] origin = new int[] {101, 77, -203};
                    int expectedX = switch (rotation) {
                        case 0 -> local.getX();
                        case 1 -> template.size[2] - 1 - local.getZ();
                        case 2 -> template.size[0] - 1 - local.getX();
                        default -> local.getZ();
                    };
                    int expectedZ = switch (rotation) {
                        case 0 -> local.getZ();
                        case 1 -> local.getX();
                        case 2 -> template.size[2] - 1 - local.getZ();
                        default -> template.size[0] - 1 - local.getX();
                    };
                    BlockPos transformed = Planner.transform(Catalog.vec(local), template.size, rotation, origin);
                    helper.assertTrue(transformed.equals(new BlockPos(101 + expectedX, 77 + local.getY(), -203 + expectedZ)), "Bounding-correct child rotation " + rotation);
                    helper.assertTrue(cell.state().rotate(Rotation.values()[rotation]).rotate(Rotation.values()[(4 - rotation) % 4]).equals(cell.state()), "Native state rotation is reversible");
                }
                compared++;
            }
            helper.assertTrue(compared > 100, "Substantial child geometry compared, not just metadata");
        }
        helper.assertTrue(children.size() == 3, "All three parent/child alternatives verified");
        var plan = Planner.create(SEED, ANCHOR);
        var repeated = Planner.create(SEED, ANCHOR);
        helper.assertTrue(plan.id.equals(repeated.id), "Identical seed and anchor have stable instance identity");
        helper.assertTrue(new com.google.gson.Gson().toJson(plan).equals(new com.google.gson.Gson().toJson(repeated)), "Full plan is deterministic");
        helper.assertTrue(Arrays.equals(plan.entry, new int[] {7, 104, 8}), "Entry relative to requested anchor");
        long playable = plan.pieces.stream().filter(piece -> !piece.ports.isEmpty()).count();
        helper.assertTrue(plan.pieces.size() > 20 && plan.edges == playable - 1, "Main graph has tree edge count, excluding independent scenery and boss");
        for (int first = 0; first < plan.pieces.size(); first++) {
            for (int second = first + 1; second < plan.pieces.size(); second++) {
                helper.assertTrue(!Planner.bounds(plan.pieces.get(first)).intersects(Planner.bounds(plan.pieces.get(second))), "No piece-envelope overlap: " + first + "/" + second);
            }
        }
        helper.assertTrue("boss/eclipse_keeper".equals(plan.pieces.get(plan.pieces.size() - 1).template), "Independent boss is final piece");
        helper.succeed();
    }

    private static ServerLevel dimension(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(EclipseMod.DIMENSION);
        helper.assertTrue(level != null, "Dedicated dimension is registered and loaded");
        return level;
    }

    private static List<BlockPos> cells(Planner.Plan plan) {
        List<BlockPos> result = new ArrayList<>();
        for (var piece : plan.pieces) {
            for (var cell : Catalog.blocks(piece.key)) {
                result.add(Planner.transform(Catalog.vec(cell.position()), piece.size, piece.rotation, piece.origin));
            }
        }
        return result;
    }

    private static void rejected(GameTestHelper helper, Runnable operation, String message) {
        boolean rejected = false;
        try {
            operation.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, message);
    }

    private static void advance(GameTestHelper helper, ServerLevel level, Engine.Job job, Runnable complete) {
        helper.assertTrue(!"FAILED".equals(job.stage), "Construction failed: " + job.error);
        if ("COMPLETE".equals(job.stage)) {
            complete.run();
            return;
        }
        Engine.tick(level, 32768);
        helper.runAfterDelay(1, () -> advance(helper, level, job, complete));
    }

    private static void chunks(ServerLevel level, Planner.Plan plan, boolean forced) {
        Set<Long> changed = new HashSet<>();
        for (var piece : plan.pieces) {
            AABB bounds = Planner.bounds(piece);
            for (int chunkX = ((int) bounds.minX) >> 4; chunkX <= (((int) bounds.maxX) - 1) >> 4; chunkX++) {
                for (int chunkZ = ((int) bounds.minZ) >> 4; chunkZ <= (((int) bounds.maxZ) - 1) >> 4; chunkZ++) {
                    long key = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
                    if (changed.add(key)) {
                        level.setChunkForced(chunkX, chunkZ, forced);
                    }
                }
            }
        }
    }

    private static Store roundTrip(Store original) {
        CompoundTag saved = original.save(new CompoundTag());
        return Store.load(saved.copy());
    }

    @GameTest(template = "empty", timeoutTicks = 20000)
    public static void constructionPersistenceAndLifecycle(GameTestHelper helper) {
        ServerLevel level = dimension(helper);
        ServerLevel overworld = level.getServer().overworld();
        BlockPos spawnBefore = overworld.getSharedSpawnPos();
        String rulesBefore = overworld.getGameRules().createTag().toString();
        rejected(helper, () -> Engine.plan(overworld, SEED, ANCHOR), "Engine rejects planning in the overworld");
        Engine.Job obstructed = Engine.plan(level, SEED, ANCHOR.offset(8192, 0, 0));
        var last = obstructed.plan.pieces.get(obstructed.plan.pieces.size() - 1);
        var solid = Catalog.blocks(last.key).stream().filter(cell -> !cell.state().isAir()).reduce((previous, next) -> next).orElseThrow();
        BlockPos obstacle = Planner.transform(Catalog.vec(solid.position()), last.size, last.rotation, last.origin);
        List<BlockPos> reserved = cells(obstructed.plan);
        helper.assertTrue(reserved.stream().allMatch(position -> level.getBlockState(position).isAir()), "Fresh test reservation is genuinely empty");
        level.setBlockAndUpdate(obstacle, Blocks.DIAMOND_BLOCK.defaultBlockState());
        try {
            Engine.start(level, obstructed.plan.id);
        } catch (IllegalArgumentException | IllegalStateException expected) {
            helper.assertTrue("FAILED".equals(obstructed.stage), "Rejected preflight records FAILED state");
        }
        preflightFailure(helper, level, obstructed, reserved, obstacle, () -> {
            Engine.Job job = Engine.plan(level, SEED, ANCHOR);
            helper.assertTrue(Engine.plan(level, SEED, ANCHOR) == job, "Identical reservation reuses existing job");
            rejected(helper, () -> Engine.plan(level, SEED + 1, ANCHOR), "Different instance cannot reserve overlapping volumes");
            Engine.start(level, job.plan.id);
            pauseAfterFirstWrite(helper, level, job, () -> {
                Engine.Job resumed = Store.get(level).jobs.get(job.plan.id);
                advance(helper, level, resumed, () -> {
                    chunks(level, resumed.plan, true);
                    System.out.println("ECLIPSE_UNFORCED_BUILD_PASS: first instance reached COMPLETE without test force tickets");
                    helper.assertTrue(overworld.getSharedSpawnPos().equals(spawnBefore), "No global spawn mutation");
                    helper.assertTrue(overworld.getGameRules().createTag().toString().equals(rulesBefore), "No global gamerule mutation");
                    verifyComplete(helper, level, resumed);
                    verifyLifecycle(helper, level, resumed);
                });
            });
        });
    }

    private static void preflightFailure(GameTestHelper helper, ServerLevel level, Engine.Job job, List<BlockPos> reserved, BlockPos obstacle, Runnable complete) {
        if (!"FAILED".equals(job.stage)) {
            helper.assertTrue(!"BUILDING".equals(job.stage) && !"COMPLETE".equals(job.stage), "No build may start before full preflight rejects late obstacle");
            Engine.tick(level, 32768);
            helper.runAfterDelay(1, () -> preflightFailure(helper, level, job, reserved, obstacle, complete));
            return;
        }
        helper.assertTrue(job.error != null && !job.error.isBlank(), "Preflight records an actionable failure");
        for (BlockPos position : reserved) {
            helper.assertTrue(position.equals(obstacle) ? level.getBlockState(position).is(Blocks.DIAMOND_BLOCK) : level.getBlockState(position).isAir(), "Preflight failure wrote no geometry at " + position);
        }
        complete.run();
    }

    private static void pauseAfterFirstWrite(GameTestHelper helper, ServerLevel level, Engine.Job job, Runnable complete) {
        helper.assertTrue(!"FAILED".equals(job.stage), "Build failed before persistence checkpoint: " + job.error);
        if (!"BUILDING".equals(job.stage) || job.pieceCursor == 0 && job.cellCursor == 0) {
            helper.assertTrue(!"COMPLETE".equals(job.stage), "Bounded budget must leave a real partial-build checkpoint");
            Engine.tick(level, 257);
            helper.runAfterDelay(1, () -> pauseAfterFirstWrite(helper, level, job, complete));
            return;
        }
        Engine.pause(level, job.plan.id);
        int pieceCursor = job.pieceCursor;
        int cellCursor = job.cellCursor;
        helper.assertTrue("PAUSED".equals(job.stage), "Actual partial build can pause");
        Engine.tick(level, 32768);
        helper.assertTrue(job.pieceCursor == pieceCursor && job.cellCursor == cellCursor, "Paused job never advances");
        Store store = Store.get(level);
        CompoundTag wrongSchema = store.save(new CompoundTag());
        wrongSchema.putInt("schema", -1);
        rejected(helper, () -> Store.load(wrongSchema), "Unknown saved-data schema is refused, never silently cleared");
        CompoundTag wrongCatalog = store.save(new CompoundTag());
        JsonObject snapshot = JsonParser.parseString(wrongCatalog.getString("payload")).getAsJsonObject();
        snapshot.addProperty("fingerprint", "not-the-installed-catalog");
        wrongCatalog.putString("payload", snapshot.toString());
        wrongCatalog.putString("payloadHash", Catalog.hash(snapshot.toString().getBytes(StandardCharsets.UTF_8)));
        rejected(helper, () -> Store.load(wrongCatalog), "Mismatched catalog fingerprint fails closed");
        Store loaded = roundTrip(store);
        Engine.Job restored = loaded.jobs.get(job.plan.id);
        helper.assertTrue(restored != null && restored != job, "NBT load rebuilds an independent job object");
        helper.assertTrue("PAUSED".equals(restored.stage) && restored.pieceCursor == pieceCursor && restored.cellCursor == cellCursor, "NBT restart preserves exact stage and cursors");
        helper.assertTrue(new com.google.gson.Gson().toJson(restored.plan).equals(new com.google.gson.Gson().toJson(job.plan)), "Frozen plan and fingerprint survive restart");
        store.jobs.put(job.plan.id, restored);
        Engine.start(level, restored.plan.id);
        complete.run();
    }

    private static void verifyComplete(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        helper.assertTrue("COMPLETE".equals(job.stage) && job.gameplay != null, "Full geometry and gameplay initialization completed");
        Set<BlockPos> gatePositions = new HashSet<>();
        for (int east = 15; east <= 18; east++) {
            for (int height = 2; height <= 7; height++) {
                gatePositions.add(Catalog.pos(job.plan.bossOrigin).offset(east, height, 22));
            }
        }
        helper.assertTrue(gatePositions.size() == 24, "Exact runtime gate override volume");
        int runes = 0;
        int checked = 0;
        for (var piece : job.plan.pieces) {
            for (var socket : piece.sockets) {
                BlockPos rune = Catalog.pos(socket.position);
                helper.assertTrue(level.getBlockState(rune).is(EclipseMod.RUNE.get()), "Actual registered rune placed at " + rune);
                helper.assertTrue(level.getBlockState(rune.below()).isFaceSturdy(level, rune.below(), Direction.UP), "Rune retains full support at " + rune);
                helper.assertTrue(level.getBlockState(rune).canSurvive(level, rune), "Rune survives native block rules");
                runes++;
            }
            for (var cell : Catalog.blocks(piece.key)) {
                if (cell.state().isAir()) {
                    continue;
                }
                BlockPos position = Planner.transform(Catalog.vec(cell.position()), piece.size, piece.rotation, piece.origin);
                var templateExpected = cell.state().rotate(Rotation.values()[piece.rotation]);
                var expected = gatePositions.contains(position) ? Blocks.PURPLE_STAINED_GLASS.defaultBlockState() : templateExpected;
                var actual = level.getBlockState(position);
                String diagnostic = "piece=" + piece.index + " key=" + piece.key + " position=" + position
                    + " templateExpected=" + templateExpected + " runtimeExpected=" + expected + " actual=" + actual;
                if (gatePositions.contains(position) || !actual.equals(expected)) {
                    System.out.println("ECLIPSE_GEOMETRY_INFO: " + diagnostic);
                }
                helper.assertTrue(actual.equals(expected), "Actual template material and native rotation: " + diagnostic);
                if (cell.nbt() != null) {
                    var blockEntity = level.getBlockEntity(position);
                    helper.assertTrue(blockEntity != null && blockEntity.getBlockPos().equals(position), "Block entity loaded with world coordinates");
                }
                checked++;
            }
        }
        gate(helper, level, job, false);
        helper.assertTrue(runes > 0 && checked > 10000, "Substantial actual geometry and rune coverage");
        for (int[] cap : job.plan.caps) {
            BlockPos feet = Catalog.pos(cap);
            for (int rise = 0; rise < 5; rise++) {
                var expected = rise == 0 || rise == 4 ? Blocks.DEEPSLATE_BRICKS : Blocks.PURPLE_STAINED_GLASS;
                helper.assertTrue(level.getBlockState(feet.above(rise)).is(expected), "Every unmatched port is capped with the exact five-layer profile");
            }
        }
        helper.assertTrue(Engine.protectedAt(level, Catalog.pos(job.plan.entry)), "Complete instance entry belongs to protected volume");
        helper.assertTrue(!Engine.protectedAt(level, new BlockPos(-1000000, 100, -1000000)), "Protection does not extend globally");
        int cursor = job.pieceCursor;
        int cell = job.cellCursor;
        var first = job.plan.pieces.get(0);
        var source = Catalog.blocks(first.key).stream().filter(value -> !value.state().isAir() && value.nbt() == null).findFirst().orElseThrow();
        BlockPos marker = Planner.transform(Catalog.vec(source.position()), first.size, first.rotation, first.origin);
        var previous = level.getBlockState(marker);
        level.setBlockAndUpdate(marker, Blocks.GOLD_BLOCK.defaultBlockState());
        Engine.start(level, job.plan.id);
        Engine.tick(level, 32768);
        helper.assertTrue("COMPLETE".equals(job.stage) && job.pieceCursor == cursor && job.cellCursor == cell, "Completed job never advances or resets cursors");
        helper.assertTrue(level.getBlockState(marker).is(Blocks.GOLD_BLOCK), "Completed build cannot replay over the world");
        level.setBlockAndUpdate(marker, previous);
    }

    private static void awaitTracked(GameTestHelper helper, ServerLevel level, Engine.Job job, int attempts, Runnable complete) {
        boolean present = job.gameplay.intents.values().stream().filter(intent -> !"DEAD".equals(intent.status))
            .allMatch(intent -> level.getEntity(UUID.fromString(intent.uuid)) instanceof LivingEntity entity && entity.isAlive());
        if (present) {
            for (var intent : job.gameplay.intents.values()) {
                Entity entity = level.getEntity(UUID.fromString(intent.uuid));
                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                    if ("room".equals(intent.role)) {
                        mob.setInvulnerable(true);
                    }
                }
            }
            complete.run();
            return;
        }
        helper.assertTrue(attempts > 0, "Tracked entity intents did not materialize: " + new com.google.gson.Gson().toJson(job.gameplay.intents));
        Gameplay.tick(level);
        helper.runAfterDelay(1, () -> awaitTracked(helper, level, job, attempts - 1, complete));
    }

    private static void gate(GameTestHelper helper, ServerLevel level, Engine.Job job, boolean open) {
        int checked = 0;
        for (int east = 15; east <= 18; east++) {
            for (int height = 2; height <= 7; height++) {
                BlockPos position = Catalog.pos(job.plan.bossOrigin).offset(east, height, 22);
                helper.assertTrue(open ? level.getBlockState(position).isAir() : level.getBlockState(position).is(Blocks.PURPLE_STAINED_GLASS), "All 24 return-gate cells must be " + (open ? "open" : "locked") + ": " + position);
                checked++;
            }
        }
        helper.assertTrue(checked == 24, "Exact gate volume");
    }

    private static WitherSkeleton boss(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        Entity entity = level.getEntity(UUID.fromString(job.gameplay.bossUuid));
        helper.assertTrue(entity instanceof WitherSkeleton && entity.isAlive(), "Actual living wither-skeleton boss exists");
        return (WitherSkeleton) entity;
    }

    private static void verifySpawnPointsAndShops(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        Set<BlockPos> distinct = new HashSet<>();
        List<Gameplay.SpawnPoint> points = new ArrayList<>(job.gameplay.bossPoints);
        for (var room : job.gameplay.rooms) {
            points.addAll(room.points);
        }
        helper.assertTrue(!job.gameplay.rooms.isEmpty() && points.size() > 4, "Actual encounters and boss reinforcement sockets exist");
        for (var point : points) {
            BlockPos feet = Catalog.pos(point.feet);
            BlockPos rune = Catalog.pos(point.rune);
            helper.assertTrue(distinct.add(feet) && !feet.equals(rune), "Each safe entity foot position is separate from every other spawn and its rune");
            helper.assertTrue(level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir(), "Entity feet and head are AIR");
            helper.assertTrue(level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP), "Spawn point has full native support");
            helper.assertTrue(level.noCollision(new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2, feet.getX() + 0.8, feet.getY() + 1.95, feet.getZ() + 0.8)), "Spawn box does not collide with rune or geometry");
        }
        long expected = job.plan.pieces.stream().filter(piece -> "terminal/canteen".equals(piece.template)).count();
        long actual = job.gameplay.intents.values().stream().filter(intent -> "merchant".equals(intent.role)).count();
        helper.assertTrue(expected > 0 && actual == expected, "One merchant intent for every actual canteen");
        for (var intent : job.gameplay.intents.values()) {
            if (!"merchant".equals(intent.role)) {
                continue;
            }
            Entity entity = level.getEntity(UUID.fromString(intent.uuid));
            helper.assertTrue(entity instanceof Villager, "Actual merchant materialized");
            Villager merchant = (Villager) entity;
            helper.assertTrue(merchant.getOffers().size() == 3, "Actual merchant exposes three trades");
            merchant.getOffers().forEach(offer -> helper.assertTrue(!offer.getBaseCostA().isEmpty() && !offer.getResult().isEmpty(), "Trade has real input and output"));
            helper.assertTrue(level.noCollision(merchant, merchant.getBoundingBox()), "Merchant is collision-safe");
        }
    }

    private static void verifyLifecycle(GameTestHelper helper, ServerLevel level, Engine.Job original) {
        Gameplay.initialize(level, original);
        awaitTracked(helper, level, original, 100, () -> {
            verifySpawnPointsAndShops(helper, level, original);
            var room = original.gameplay.rooms.get(0);
            helper.assertTrue(Gameplay.startWave(level, original, room), "A real complete room starts its encounter");
            helper.assertTrue(room.nextWaveAt == level.getGameTime() + 3600 && room.wave == 1, "Room shares an exact 180-second cooldown");
            helper.assertTrue(!Gameplay.startWave(level, original, room), "Same room cannot spawn twice within its cooldown");
            awaitTracked(helper, level, original, 100, () -> {
                helper.assertTrue(room.active.size() == room.points.size(), "Every legal room socket produces a tracked living enemy");
                for (String uuid : room.active) {
                    Entity enemy = level.getEntity(UUID.fromString(uuid));
                    helper.assertTrue(enemy instanceof LivingEntity && enemy.isAlive(), "Real room enemy exists");
                    helper.assertTrue(level.noCollision(enemy, enemy.getBoundingBox()), "Real enemy collision box is safe");
                }
                String savedState = new com.google.gson.Gson().toJson(original.gameplay);
                Store reloaded = roundTrip(Store.get(level));
                Engine.Job restored = reloaded.jobs.get(original.plan.id);
                helper.assertTrue(savedState.equals(new com.google.gson.Gson().toJson(restored.gameplay)), "Restart preserves all UUID intents, cooldowns and lifecycle state");
                Store.get(level).jobs.put(restored.plan.id, restored);
                Gameplay.initialize(level, restored);
                Gameplay.tick(level);
                helper.assertTrue(savedState.equals(new com.google.gson.Gson().toJson(restored.gameplay)), "Runtime reinitialization reuses entities without rewriting intent identity");
                for (var intent : restored.gameplay.intents.values()) {
                    Entity entity = level.getEntity(UUID.fromString(intent.uuid));
                    helper.assertTrue(entity != null && entity.isAlive(), "Persisted UUID is reused after actual NBT reload");
                    helper.assertTrue(level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1), candidate -> candidate.getUUID().toString().equals(intent.uuid)).size() == 1, "No duplicate entity after reload");
                }
                helper.assertTrue(Gameplay.startBoss(level, restored), "First instance starts unique boss encounter");
                awaitTracked(helper, level, restored, 100, () -> {
                    WitherSkeleton missing = boss(helper, level, restored);
                    helper.assertTrue(missing.getMaxHealth() == 200.0F, "Boss has 200 maximum health");
                    helper.assertTrue(restored.gameplay.intents.values().stream().filter(intent -> "boss_add".equals(intent.role)).count() == 4, "Four real tracked boss reinforcements");
                    helper.assertTrue(!Gameplay.startBoss(level, restored), "Repeated boss start is rejected without duplicates");
                    missing.discard();
                    helper.runAfterDelay(2, () -> {
                        Gameplay.tick(level);
                        helper.assertTrue(!"VICTORY".equals(restored.gameplay.bossState) && !restored.gameplay.rewardGranted, "Disappearance is never victory");
                        gate(helper, level, restored, false);
                        Engine.Job second = Engine.plan(level, SEED, ANCHOR.offset(16384, 0, 0));
                        chunks(level, second.plan, true);
                        Engine.start(level, second.plan.id);
                        advance(helper, level, second, () -> nonplayerAndIsolation(helper, level, restored, second));
                    });
                });
            });
        });
    }

    private static void nonplayerAndIsolation(GameTestHelper helper, ServerLevel level, Engine.Job first, Engine.Job second) {
        helper.assertTrue(first.gameplay != second.gameplay && !first.gameplay.instanceId.equals(second.gameplay.instanceId), "Two complete instances own distinct runtime state");
        helper.assertTrue(Gameplay.startBoss(level, second), "Independent second instance starts its own boss");
        awaitTracked(helper, level, second, 100, () -> {
            helper.assertTrue(!first.gameplay.bossUuid.equals(second.gameplay.bossUuid), "Boss identities are per-instance");
            helper.assertTrue(boss(helper, level, second).hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "Actual nonplayer damage kills the second boss");
            helper.runAfterDelay(2, () -> {
                Gameplay.tick(level);
                helper.assertTrue(!"VICTORY".equals(second.gameplay.bossState) && !second.gameplay.rewardGranted, "Nonplayer death never grants victory or rewards");
                gate(helper, level, second, false);
                gate(helper, level, first, false);
                Engine.Job third = Engine.plan(level, SEED, ANCHOR.offset(32768, 0, 0));
                chunks(level, third.plan, true);
                Engine.start(level, third.plan.id);
                advance(helper, level, third, () -> playerVictory(helper, level, first, second, third));
            });
        });
    }

    private static ServerPlayer realPlayer(ServerLevel level, BlockPos position) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "EclipseGenQA"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        level.addNewPlayer(player);
        return player;
    }

    private static void playerVictory(GameTestHelper helper, ServerLevel level, Engine.Job first, Engine.Job second, Engine.Job winner) {
        ServerPlayer player = realPlayer(level, Catalog.pos(winner.plan.exit));
        Gameplay.tick(level);
        helper.assertTrue(player.blockPosition().distSqr(Catalog.pos(winner.gameplay.bossEntry)) < 4, "Actual promenade trigger teleports to this instance's boss entry");
        awaitTracked(helper, level, winner, 100, () -> {
            WitherSkeleton target = boss(helper, level, winner);
            gate(helper, level, winner, false);
            helper.assertTrue("FIGHTING".equals(winner.gameplay.bossState), "Victory requires fighting state");
            helper.assertTrue(target.hurt(level.damageSources().playerAttack(player), 10000.0F), "Real ServerPlayer inflicts actual lethal boss damage");
            helper.runAfterDelay(65, () -> {
                Gameplay.tick(level);
                helper.assertTrue("VICTORY".equals(winner.gameplay.bossState) && winner.gameplay.rewardGranted, "Registered real LivingDeathEvent grants victory exactly once");
                helper.assertTrue(player.getUUID().toString().equals(winner.gameplay.victorUuid), "Victor identity is persisted");
                gate(helper, level, winner, true);
                gate(helper, level, first, false);
                gate(helper, level, second, false);
                for (var intent : winner.gameplay.intents.values()) {
                    if ("boss_add".equals(intent.role)) {
                        Entity entity = level.getEntity(UUID.fromString(intent.uuid));
                        helper.assertTrue(entity == null || !entity.isAlive(), "Only winning instance boss adds are cleaned up");
                    }
                }
                for (String uuid : first.gameplay.rooms.get(0).active) {
                    Entity entity = level.getEntity(UUID.fromString(uuid));
                    helper.assertTrue(entity != null && entity.isAlive(), "Other instance's room mobs survive victory cleanup");
                }
                int reward = player.getInventory().countItem(Items.EMERALD);
                helper.assertTrue(reward == 24, "One actual 24-emerald reward");
                Gameplay.initialize(level, winner);
                Gameplay.tick(level);
                helper.assertTrue(player.getInventory().countItem(Items.EMERALD) == reward, "Repeated initialization and tick cannot duplicate reward");
                BlockPos back = Catalog.pos(winner.gameplay.bossReturn);
                player.setPos(back.getX() + 0.5, back.getY(), back.getZ() + 0.5);
                Gameplay.tick(level);
                helper.assertTrue(player.blockPosition().distSqr(Catalog.pos(winner.plan.entry)) < 4, "Victory return trigger targets this plan's entry");
                verifyEnterLeave(helper, level, player, winner);
                verifyDiskCheckpoint(helper, level, first, second, winner);
                player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                chunks(level, first.plan, false);
                chunks(level, second.plan, false);
                chunks(level, winner.plan, false);
                helper.succeed();
            });
        });
    }

    private static void verifyEnterLeave(GameTestHelper helper, ServerLevel dungeon, ServerPlayer player, Engine.Job winner) {
        ServerLevel overworld = dungeon.getServer().overworld();
        BlockPos safe = helper.absolutePos(new BlockPos(2, 24, 2));
        overworld.getChunk(safe);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                BlockPos feet = safe.offset(offsetX, 0, offsetZ);
                overworld.setBlockAndUpdate(feet.below(), Blocks.STONE.defaultBlockState());
                for (int height = 0; height < 3; height++) {
                    overworld.setBlockAndUpdate(feet.above(height), Blocks.AIR.defaultBlockState());
                }
            }
        }
        player.teleportTo(overworld, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, 42.0F, 7.0F);
        String diagnostic = "expectedDimension=" + overworld.dimension().location() + ";actualDimension=" + player.level().dimension().location()
            + ";expectedFeet=" + safe + ";actualPosition=" + player.position() + ";box=" + player.getBoundingBox()
            + ";support=" + overworld.getBlockState(safe.below()) + ";feet=" + overworld.getBlockState(safe)
            + ";head=" + overworld.getBlockState(safe.above()) + ";clearance=" + overworld.getBlockState(safe.above(2))
            + ";noCollision=" + overworld.noCollision(player, player.getBoundingBox());
        System.out.println("ECLIPSE_ENTRY_FIXTURE_INFO: " + diagnostic);
        helper.assertTrue(player.serverLevel() == overworld && player.blockPosition().equals(safe), "Actual player reaches overworld test platform: " + diagnostic);
        helper.assertTrue(overworld.getBlockState(safe.below()).isFaceSturdy(overworld, safe.below(), Direction.UP)
            && overworld.getBlockState(safe).isAir() && overworld.getBlockState(safe.above()).isAir()
            && overworld.getBlockState(safe.above(2)).isAir() && overworld.noCollision(player, player.getBoundingBox()),
            "Actual player starts entry test on full support with clear collision space: " + diagnostic);
        var origin = player.position();
        var dimension = player.level().dimension();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        Engine.enter(player, winner.plan.id);
        helper.assertTrue(player.serverLevel() == dungeon && player.level().dimension().equals(EclipseMod.DIMENSION), "Engine.enter moves actual player into dedicated dimension");
        helper.assertTrue(player.blockPosition().distSqr(Catalog.pos(winner.plan.entry)) < 4, "Engine.enter uses the requested COMPLETE instance entry");
        helper.assertTrue(player.getPersistentData().contains("fteclipsegen_return", 10), "Engine.enter persists the original return location");
        CompoundTag back = player.getPersistentData().getCompound("fteclipsegen_return");
        helper.assertTrue(back.getString("dimension").equals(dimension.location().toString())
            && back.getDouble("x") == origin.x && back.getDouble("y") == origin.y && back.getDouble("z") == origin.z,
            "Return NBT stores exact original dimension and coordinates");
        EclipseMod.leave(player);
        helper.assertTrue(player.serverLevel() == overworld && player.level().dimension().equals(dimension), "EclipseMod.leave restores original dimension");
        helper.assertTrue(player.position().distanceToSqr(origin) < 1.0E-10
            && Math.abs(player.getYRot() - yaw) < 0.001F && Math.abs(player.getXRot() - pitch) < 0.001F,
            "EclipseMod.leave restores original coordinates and orientation");
        helper.assertTrue(!player.getPersistentData().contains("fteclipsegen_return"), "Successful leave consumes the persisted return record");
        System.out.println("ECLIPSE_ENTER_LEAVE_PASS: originalDimension=" + dimension.location() + ";origin=" + origin + ";returnNBT=consumed");
        net.farmingtales.ftdungeonmobs.DungeonGateIntegrationTests.verifyCompletedEclipseGate(helper, player, winner);
    }

    private static void verifyDiskCheckpoint(GameTestHelper helper, ServerLevel level, Engine.Job first, Engine.Job second, Engine.Job winner) {
        Store current = Store.get(level);
        Store.flush(level);
        Path file = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
            .resolve("data").resolve("fteclipsegen_instances.dat");
        helper.assertTrue(Files.isRegularFile(file), "Actual dedicated-dimension checkpoint exists on disk: " + file);
        CompoundTag root;
        try {
            root = NbtIo.readCompressed(file.toFile());
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read actual Eclipse disk checkpoint: " + file, failure);
        }
        helper.assertTrue(root.contains("data", 10), "Disk checkpoint contains the SavedData compound");
        CompoundTag data = root.getCompound("data");
        helper.assertTrue(data.contains("payloadParts", 9) && data.getString("payload").isEmpty(), "Large actual disk payload uses only the multipart encoding");
        var parts = data.getList("payloadParts", 8);
        helper.assertTrue(parts.size() > 1, "Large frozen plans are stored as multiple NBT strings");
        StringBuilder joined = new StringBuilder();
        for (int index = 0; index < parts.size(); index++) {
            String part = parts.getString(index);
            helper.assertTrue(!part.isEmpty() && part.length() <= 16000, "Each disk payload part obeys the NBT string-safe size bound");
            joined.append(part);
        }
        helper.assertTrue(joined.length() > 16000 && data.getString("payloadHash").equals(Catalog.hash(joined.toString().getBytes(StandardCharsets.UTF_8))), "Joined large disk payload matches its persisted integrity hash");
        Store disk = Store.load(data);
        helper.assertTrue(disk != current && Store.get(level) == current, "Disk verification reads a detached Store without replacing current world state");
        helper.assertTrue(disk.jobs.values().stream().filter(job -> "COMPLETE".equals(job.stage)).count() == 3, "Actual disk checkpoint contains exactly three COMPLETE instances");
        var gson = new com.google.gson.Gson();
        for (Engine.Job expected : List.of(first, second, winner)) {
            Engine.Job restored = disk.jobs.get(expected.plan.id);
            helper.assertTrue(restored != null && "COMPLETE".equals(restored.stage), "Every built instance is COMPLETE on disk: " + expected.plan.id);
            helper.assertTrue(gson.toJson(restored).equals(gson.toJson(expected)), "Disk plan, cursors, entity intents and lifecycle match live instance: " + expected.plan.id);
        }
        Engine.Job savedWinner = disk.jobs.get(winner.plan.id);
        helper.assertTrue("VICTORY".equals(savedWinner.gameplay.bossState) && savedWinner.gameplay.rewardGranted
            && winner.gameplay.victorUuid.equals(savedWinner.gameplay.victorUuid), "Victory and exactly-once reward ownership survive actual disk read");
        CompoundTag tamperedHash = data.copy();
        tamperedHash.putString("payloadHash", "tampered-checkpoint-hash");
        rejected(helper, () -> Store.load(tamperedHash), "Tampered multipart disk payloadHash is refused rather than silently cleared");
        System.out.println("ECLIPSE_DISK_CHECKPOINT_PASS: file=" + file + ";parts=" + parts.size()
            + ";characters=" + joined.length() + ";complete=3;victory=preserved;tamperedHash=rejected");
    }
}
