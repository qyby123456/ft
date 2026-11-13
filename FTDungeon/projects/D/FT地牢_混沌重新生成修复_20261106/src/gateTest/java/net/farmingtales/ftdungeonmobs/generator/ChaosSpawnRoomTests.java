package net.farmingtales.ftdungeonmobs.generator;

import com.mojang.authlib.GameProfile;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftgateintegration")
@PrefixGameTestTemplate(false)
public final class ChaosSpawnRoomTests {
    private static final class TestPlayer extends ServerPlayer {
        TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "ChaosSpawnQA"));
            connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), this) {
                @Override public void send(Packet<?> packet) {}
                @Override public void send(Packet<?> packet, PacketSendListener listener) {}
            };
            setGameMode(GameType.CREATIVE);
        }
    }

    private static boolean connected(ServerLevel level) {
        var visited = new HashSet<BlockPos>();
        var pending = new ArrayDeque<BlockPos>();
        pending.add(ChaosSpawnRoom.LANDING);
        while (!pending.isEmpty()) {
            BlockPos feet = pending.removeFirst();
            if (!visited.add(feet) || feet.getX() < -24 || feet.getX() > 80
                    || feet.getZ() < 80 || feet.getZ() > 108 || !ChaosSpawnRoom.safe(level, feet)) continue;
            if (feet.equals(new BlockPos(72, 211, 100))) return true;
            for (Direction direction : Direction.Plane.HORIZONTAL) pending.add(feet.relative(direction));
        }
        return false;
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void fourLayoutsAndRealEntry(GameTestHelper helper) {
        ServerLevel source = helper.getLevel();
        var server = source.getServer();
        ServerLevel destination = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ftmaze", "depths")));
        helper.assertTrue(destination != null, "Chaos dimension exists");
        for (int layout = 0; layout < 4; layout++) {
            var template = destination.getStructureManager().get(new ResourceLocation("ftmaze", "v" + layout + "/infra_0_0")).orElseThrow();
            for (int chunkX = 0; chunkX <= 9; chunkX++) {
                for (int chunkZ = 0; chunkZ <= 19; chunkZ++) destination.getChunk(chunkX, chunkZ);
            }
            template.placeInWorld(destination, new BlockPos(0, 207, 0), new BlockPos(0, 207, 0), new StructurePlaceSettings(), destination.random, Block.UPDATE_CLIENTS);
            CompoundTag record = new CompoundTag();
            ChaosSpawnRoom.prepare(destination, record);
            helper.assertTrue(record.getInt("SpawnRoomVersion") == 1, "Room completion persists");
            helper.assertTrue(ChaosSpawnRoom.safe(destination, ChaosSpawnRoom.LANDING), "Safe top floor landing for layout " + layout);
            helper.assertTrue(connected(destination), "Covered room connects to original starting route for layout " + layout);
        }
        TestPlayer player = new TestPlayer(source);
        player.setPos(10000.5, 100, 10000.5);
        source.addNewPlayer(player);
        try {
            ChaosDungeonGenerator provider = new ChaosDungeonGenerator();
            var instance = provider.reserve(new DungeonGenerator.Request(source, player.blockPosition(), 20261106L, 1));
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4), "scoreboard players set #run ftm_state 2");
            helper.assertTrue(provider.status(server, instance).ready(), "Existing completed dungeon gains spawn room before READY");
            Vec3 origin = player.position();
            player.setDeltaMovement(0, -3, 0);
            player.fallDistance = 80;
            provider.enter(player, instance);
            helper.assertTrue(player.serverLevel() == destination && player.position().distanceToSqr(ChaosSpawnRoom.TARGET) < 0.001, "Real entry function teleports to center of room");
            helper.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && player.fallDistance == 0 && player.getDeltaMovement().equals(Vec3.ZERO), "Entry resets fall and velocity");
            helper.assertTrue(player.getPersistentData().contains("FTChaosReturn"), "Return record preserved");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4), "scoreboard players set #run ftm_state 4");
            provider.leave(player);
            helper.assertTrue(player.serverLevel() == source && player.position().distanceToSqr(origin) < 0.001 && player.gameMode.getGameModeForPlayer() == GameType.CREATIVE, "Boss-complete return restores source and mode");
            destination.setBlock(ChaosSpawnRoom.LANDING.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            boolean refused = false;
            try { provider.enter(player, instance); } catch (IllegalStateException expected) { refused = true; }
            helper.assertTrue(refused && player.serverLevel() == source && !player.getPersistentData().contains("FTChaosReturn"), "Damaged floor refuses entry without changing return data");
            System.out.println("CHAOS_SPAWN_ROOM_PASS layouts=4 safePath=true actualEntry=true return=true damagedFloorRejected=true");
            helper.succeed();
        } finally {
            player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }
}
