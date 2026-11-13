package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Area;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Point;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Room;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DungeonSpawnPointsGameTests {
    private DungeonSpawnPointsGameTests() {}

    @GameTest(template = "empty", batch = "zz_spawn_points", timeoutTicks = 400)
    public static void fixedRoomEncounters(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
        helper.assertTrue(level != null && level.getServer() instanceof GameTestServer, "Only modify an isolated GameTest world");
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        BlockPos origin = new BlockPos(1024, 64, 1024);
        Area area = new Area("SPAWN_TEST", origin, new Vec3i(32, 6, 32), origin.offset(16, 1, 0), "test");
        for (BlockPos position : BlockPos.betweenClosed(origin, origin.offset(31, 6, 31))) {
            level.setBlock(position, position.getY() == origin.getY() ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos position : BlockPos.betweenClosed(origin.offset(24, 1, 0), origin.offset(24, 5, 31))) {
            level.setBlock(position, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(origin.offset(10, 0, 10), Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(origin.offset(11, 1, 10), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
        DungeonSpawnData data = new DungeonSpawnData();
        level.getDataStorage().set(DungeonSpawnData.FILE_NAME, data);
        for (int tier = 1; tier <= 5; tier++) {
            DungeonSpawnPoints.beginRound(level, data, UUID.randomUUID(), tier);
            Room room = DungeonSpawnPoints.generate(level, data, area);
            helper.assertTrue(room.points().size() == tier * 2, "Default tier " + tier + " must produce " + tier * 2 + " points");
            for (Point point : room.points()) {
                helper.assertTrue(point.position.getY() == 65 && point.position.getX() < origin.getX() + 24,
                        "Points stay on reachable interior ground, never across a sealed wall or on a roof");
                helper.assertTrue(point.position.distSqr(area.entrance()) >= 16, "Keep the entrance clear");
                helper.assertTrue(point.group.size() >= 1 && point.group.size() <= 3 && !point.group.contains(SkeletonVariant.KING),
                        "Persist a bounded default group without an accidental boss");
                helper.assertTrue(!level.getBlockState(point.position.below()).is(Blocks.MAGMA_BLOCK)
                        && level.getFluidState(point.position).isEmpty(), "No hazardous or liquid floor positions");
                helper.assertTrue(level.getBlockState(point.position).getCollisionShape(level, point.position).isEmpty(),
                        "Floor sigils never obstruct movement or spawning");
                for (Point other : room.points()) {
                    helper.assertTrue(point == other || point.position.distSqr(other.position) >= 9, "Points are spaced apart");
                }
            }
        }
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "FTSpawnTest"));
        player.setGameMode(GameType.SURVIVAL);
        DungeonGateData gate = new DungeonGateData();
        level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", gate);
        gate.bind(level.getServer().overworld().dimension(), new BlockPos(100, 64, 100));
        gate.setTier(3);
        gate.startRound(player.getUUID(), level.getServer().overworld().getGameTime());
        UUID firstRound = gate.roundId();
        helper.assertTrue(firstRound.equals(DungeonGateData.load(gate.save(new CompoundTag())).roundId()), "Round identity survives restart");
        CompoundTag legacy = gate.save(new CompoundTag());
        legacy.remove("RoundId");
        helper.assertTrue(DungeonGateData.load(legacy).roundId() != null, "Active pre-0.19 saves migrate safely");
        DungeonSpawnPoints.beginRound(level, data, firstRound, gate.tier());
        Point point = new Point(origin.offset(12, 1, 14), List.of(SkeletonVariant.WARRIOR, SkeletonVariant.ARCHER, SkeletonVariant.MAGE));
        data.rooms.put(area.id(), new Room(area, List.of(point)));
        level.setBlock(point.position, FTDungeonMobs.DUNGEON_SPAWN_POINT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        player.moveTo(point.position.getX() + 0.5, 65, point.position.getZ() + 4.5, 0, 0);
        List<net.minecraft.server.level.ServerPlayer> players = List.of(player);
        player.setGameMode(GameType.CREATIVE);
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(point.spawned == 0 && !point.activated, "Creative players do not activate encounters");
        player.setGameMode(GameType.SPECTATOR);
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(point.spawned == 0 && !point.activated, "Spectators do not activate encounters");
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(point.position.getX() + 0.5, 65, point.position.getZ() + 8.5, 0, 0);
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(!point.activated, "Distant players do not activate encounters");
        player.moveTo(point.position.getX() + 0.5, 65, point.position.getZ() + 4.5, 0, 0);
        for (BlockPos position : BlockPos.betweenClosed(point.position.offset(-3, 0, 2), point.position.offset(3, 3, 2))) {
            level.setBlock(position, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(!point.activated, "A nearby player cannot trigger through a wall");
        for (BlockPos position : BlockPos.betweenClosed(point.position.offset(-3, 0, 2), point.position.offset(3, 3, 2))) {
            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        Consumer<EntityJoinLevelEvent> cancel = event -> {
            if (event.getEntity().getPersistentData().hasUUID(DungeonSpawnPoints.ROUND_TAG)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            DungeonSpawnPoints.tick(level, players);
            helper.assertTrue(point.activated && point.spawned == 0 && enemies(level, firstRound).isEmpty(),
                    "Canceled entity insertion consumes no monsters and preserves the fixed group for retry");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        for (BlockPos position : BlockPos.betweenClosed(point.position.offset(-2, 1, -2), point.position.offset(2, 1, 2))) {
            level.setBlock(position, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(point.spawned == 0, "Obstructed spawn space is never forced");
        for (BlockPos position : BlockPos.betweenClosed(point.position.offset(-2, 1, -2), point.position.offset(2, 1, 2))) {
            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.getServer().setDifficulty(Difficulty.PEACEFUL, true);
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(point.spawned == 0, "Peaceful pauses spawning without consuming the fixed group");
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(point.spawned == 3 && point.spent() && enemies(level, firstRound).size() == 3, "Spawn the exact fixed group once");
        helper.assertTrue(level.getBlockState(point.position).getValue(DungeonSpawnPointBlock.SPENT), "A consumed sigil visibly goes dark");
        for (DungeonSkeletonEntity entity : enemies(level, firstRound)) {
            helper.assertTrue(DungeonDifficultyApi.getTier(entity) == 3 && !entity.automaticDifficulty(), "Monsters inherit the locked round tier");
            helper.assertTrue(entity.isPersistenceRequired() && level.noCollision(entity)
                    && !level.containsAnyLiquid(entity.getBoundingBox()), "Monsters persist and occupy valid dry space");
        }
        List<SkeletonVariant> actual = enemies(level, firstRound).stream().map(DungeonSkeletonEntity::variant).toList();
        helper.assertTrue(actual.containsAll(point.group), "Spawn the saved warrior/archer/mage identities");
        for (int repeat = 0; repeat < 3; repeat++) {
            DungeonSpawnPoints.tick(level, players);
        }
        helper.assertTrue(enemies(level, firstRound).size() == 3, "Repeated scans and multiple proximity checks never duplicate a group");
        DungeonSpawnData restored = DungeonSpawnData.load(data.save(new CompoundTag()));
        level.getDataStorage().set(DungeonSpawnData.FILE_NAME, restored);
        DungeonSpawnPoints.tick(level, players);
        Point restoredPoint = restored.rooms.get(area.id()).points().get(0);
        helper.assertTrue(restored.roundId.equals(firstRound) && restoredPoint.spawned == 3
                && restoredPoint.group.equals(point.group) && enemies(level, firstRound).size() == 3,
                "Restart preserves positions, fixed groups, round identity and consumed state");
        enemies(level, firstRound).get(0).discard();
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(enemies(level, firstRound).size() == 2, "Removal or death does not re-arm a point in the same round");
        gate.removeParticipant(player.getUUID());
        helper.assertTrue(gate.finishRoundIfEmpty(level.getServer().overworld().getGameTime() + DungeonGateData.ADMISSION_TICKS), "End the first round normally");
        gate.startRound(player.getUUID(), level.getServer().overworld().getGameTime());
        DungeonSpawnPoints.tick(level, players);
        helper.assertTrue(!gate.roundId().equals(firstRound) && restored.rooms.isEmpty() && enemies(level, firstRound).isEmpty()
                && level.getBlockState(point.position).isAir(), "A new round removes only owned old markers and encounter monsters");
        DungeonSkeletonEntity stale = FTDungeonMobs.WARRIOR.get().create(level);
        stale.getPersistentData().putUUID(DungeonSpawnPoints.ROUND_TAG, firstRound);
        stale.moveTo(point.position.getX() + 0.5, 65, point.position.getZ() + 0.5);
        helper.assertTrue(!level.addFreshEntity(stale), "Old-round monsters cannot return when an unloaded chunk reloads");
        stale.discard();
        Room renewed = DungeonSpawnPoints.generate(level, restored, area);
        helper.assertTrue(renewed.points().size() == 6 && renewed.points().stream().noneMatch(Point::spent), "A fresh round receives new armed points at its locked tier");
        System.out.println("DUNGEON_SPAWN_POINTS_GAME_TEST_PASS: tiers 2/4/6/8/10; reachable dry floors; spacing; creative/spectator/distance/wall filters; canceled and blocked retries; peaceful pause; exact fixed group; locked attributes; one-shot persistence; new-round cleanup; stale-chunk rejection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zz_spawn_points", timeoutTicks = 400)
    public static void realRoomFloorCoverage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
        helper.assertTrue(level != null && level.getServer() instanceof GameTestServer, "Only inspect the isolated GameTest dungeon");
        List<Area> areas = DungeonSpawnPoints.catalog(level);
        for (Area area : areas) {
            BlockPos maximum = area.origin().offset(area.size()).offset(-1, -1, -1);
            for (int chunkX = area.origin().getX() >> 4; chunkX <= maximum.getX() >> 4; chunkX++) {
                for (int chunkZ = area.origin().getZ() >> 4; chunkZ <= maximum.getZ() >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, true);
                    level.getChunk(chunkX, chunkZ);
                }
            }
        }
        helper.runAfterDelay(60, () -> {
            DungeonSpawnData data = new DungeonSpawnData();
            data.roundId = UUID.randomUUID();
            data.tier = 5;
            helper.assertTrue(areas.size() == 11, "Cover the two original scenes and all nine installed theme rooms");
            for (Area area : areas) {
                Room room = DungeonSpawnPoints.generate(level, data, area);
                helper.assertTrue(!room.points().isEmpty(), "Real room must expose reachable safe ground: " + area.id()
                        + ", entrance=" + area.entrance() + ", state=" + level.getBlockState(area.entrance())
                        + ", floor=" + level.getBlockState(area.entrance().below()) + ", reachable=" + DungeonSpawnPoints.reachable(level, area).size());
                System.out.println("REAL_ROOM_SPAWN_POINTS_PASS: " + area.id() + "=" + room.points().size() + "/10");
            }
            for (Area area : areas) {
                BlockPos maximum = area.origin().offset(area.size()).offset(-1, -1, -1);
                for (int chunkX = area.origin().getX() >> 4; chunkX <= maximum.getX() >> 4; chunkX++) {
                    for (int chunkZ = area.origin().getZ() >> 4; chunkZ <= maximum.getZ() >> 4; chunkZ++) {
                        level.setChunkForced(chunkX, chunkZ, false);
                    }
                }
            }
            helper.succeed();
        });
    }

    private static List<DungeonSkeletonEntity> enemies(ServerLevel level, UUID round) {
        List<DungeonSkeletonEntity> result = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof DungeonSkeletonEntity skeleton && !entity.isRemoved()
                    && entity.getPersistentData().hasUUID(DungeonSpawnPoints.ROUND_TAG)
                    && entity.getPersistentData().getUUID(DungeonSpawnPoints.ROUND_TAG).equals(round)) {
                result.add(skeleton);
            }
        }
        return result;
    }
}
