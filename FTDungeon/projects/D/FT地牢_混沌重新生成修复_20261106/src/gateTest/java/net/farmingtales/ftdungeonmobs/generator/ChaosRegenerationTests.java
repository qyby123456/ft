package net.farmingtales.ftdungeonmobs.generator;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.farmingtales.ftdungeonmobs.DungeonGateGenerators;
import net.farmingtales.ftdungeonmobs.DungeonGeneratorMenu;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class ChaosRegenerationTests {
    private static final class TestPlayer extends ServerPlayer {
        TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "ChaosRegenQA"));
            connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), this) {
                @Override public void send(Packet<?> packet) {}
                @Override public void send(Packet<?> packet, PacketSendListener listener) {}
            };
            setGameMode(GameType.CREATIVE);
            setNoGravity(true);
        }
    }

    private static void reject(GameTestHelper helper, Runnable operation, String message) {
        boolean refused = false;
        try { operation.run(); } catch (IllegalStateException expected) { refused = true; }
        helper.assertTrue(refused, message);
    }

    @GameTest(template = "empty", timeoutTicks = 20000)
    public static void actualMenuCleanupAndFreshBuild(GameTestHelper helper) throws Exception {
        ServerLevel source = helper.getLevel();
        var server = source.getServer();
        ServerLevel destination = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ftmaze", "depths")));
        BlockPos anchor = new BlockPos(500000, 100, 500000);
        for (int offsetX = -3; offsetX <= 4; offsetX++) {
            for (int offsetZ = -3; offsetZ <= 4; offsetZ++) {
                source.setBlock(anchor.offset(offsetX, -1, offsetZ), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
        for (int part = 0; part < 12; part++) {
            source.setBlock(DungeonGateBlock.partPosition(anchor, Direction.NORTH, part), FTDungeonMobs.DUNGEON_ENTRY_GATE.get().defaultBlockState()
                    .setValue(DungeonGateBlock.FACING, Direction.NORTH).setValue(DungeonGateBlock.PART, part), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        source.setBlock(anchor.offset(3, 0, 0), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState()
                .setValue(net.farmingtales.ftdungeonmobs.BloodthornBrazierBlock.TIER, 1), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        DungeonGateBlockEntity gate = (DungeonGateBlockEntity) source.getBlockEntity(anchor);
        TestPlayer player = new TestPlayer(source);
        player.setPos(anchor.getX() + .5, anchor.getY(), anchor.getZ() + 2.5);
        source.addNewPlayer(player);
        helper.assertTrue(DungeonGateGenerators.valid(player, source, anchor), "Complete native gate is usable");
        ChaosDungeonGenerator provider = new ChaosDungeonGenerator();
        gate.configureGenerator(DungeonGenerators.CHAOS, 20261106L);
        String old = provider.reserve(new DungeonGenerator.Request(source, anchor, gate.generatorSeed(), 1));
        gate.bindGenerator(old, 1);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4), "scoreboard players set #run ftm_state 2");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4), "scoreboard players set #g0 ftm_state 2");
        provider.status(server, old);
        player.setGameMode(GameType.SURVIVAL);
        reject(helper, () -> DungeonGateGenerators.regenerate(player, gate), "Ordinary player cannot regenerate");
        player.setGameMode(GameType.CREATIVE);
        TestPlayer inside = new TestPlayer(destination);
        inside.setPos(-15.5, 211, 88.5);
        destination.addNewPlayer(inside);
        reject(helper, () -> DungeonGateGenerators.regenerate(player, gate), "Online player inside blocks regeneration");
        destination.removePlayerImmediately(inside, Entity.RemovalReason.DISCARDED);
        var offline = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(UUID.randomUUID() + ".dat");
        CompoundTag offlineData = new CompoundTag();
        offlineData.putString("Dimension", "ftmaze:depths");
        NbtIo.writeCompressed(offlineData, offline.toFile());
        try { reject(helper, () -> DungeonGateGenerators.regenerate(player, gate), "Offline player inside blocks regeneration"); }
        finally { Files.delete(offline); }
        helper.assertTrue(gate.generatorInstance().equals(old) && provider.status(server, old).ready(), "Rejected operations preserve old dungeon binding");
        source.setBlock(anchor.below(), Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        destination.setBlock(new BlockPos(0, 224, 0), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        long previousSeed = gate.generatorSeed();
        DungeonGeneratorMenu menu = new DungeonGeneratorMenu(44, player.getInventory(), source, anchor);
        helper.assertTrue(menu.stillValid(player), "Native menu is valid before confirmation");
        menu.clicked(20, 0, ClickType.PICKUP, player);
        helper.assertTrue(gate.generatorInstance().equals(old), "First menu click only asks confirmation");
        menu.clicked(20, 0, ClickType.PICKUP, player);
        String replacement = gate.generatorInstance();
        helper.assertTrue(!old.equals(replacement), "Confirmed native menu generates a fresh instance");
        helper.assertTrue(Math.floorMod(previousSeed, 4) != Math.floorMod(gate.generatorSeed(), 4), "Next layout differs");
        helper.assertTrue(provider.status(server, replacement).phase() == DungeonGenerator.Phase.BUILDING, "Cleanup closes entry");
        reject(helper, () -> DungeonGateGenerators.regenerate(player, gate), "Repeated regeneration during cleanup rejected");
        reject(helper, () -> provider.enter(player, replacement), "Cannot enter while old world clears");
        awaitReady(helper, player, source, destination, gate, provider, old, replacement, 0);
    }

    private static void awaitReady(GameTestHelper helper, TestPlayer player, ServerLevel source, ServerLevel destination,
            DungeonGateBlockEntity gate, ChaosDungeonGenerator provider, String old, String replacement, int checks) {
        helper.runAfterDelay(20, () -> {
            var status = provider.status(source.getServer(), replacement);
            helper.assertTrue(status.phase() != DungeonGenerator.Phase.FAILED, status.detail().getString());
            if (!status.ready()) {
                helper.assertTrue(checks < 900, "Regeneration completes in time");
                awaitReady(helper, player, source, destination, gate, provider, old, replacement, checks + 1);
                return;
            }
            try {
                helper.assertTrue(destination.getBlockState(new BlockPos(0, 224, 0)).isAir(), "Old-only block cleared");
                helper.assertTrue(source.getBlockState(gate.getBlockPos().below()).is(Blocks.DIAMOND_BLOCK), "Source world unchanged");
                helper.assertTrue(provider.status(source.getServer(), old).phase() == DungeonGenerator.Phase.UNAVAILABLE, "Old instance cannot enter new dungeon");
                var objective = source.getServer().getScoreboard().getObjective("ftm_state");
                helper.assertTrue(source.getServer().getScoreboard().getOrCreatePlayerScore("#g0", objective).getScore() == 0, "Guard progress reset");
                provider.enter(player, replacement);
                helper.assertTrue(player.serverLevel() == destination && player.position().distanceToSqr(ChaosSpawnRoom.TARGET) < .001, "Fresh dungeon enters rebuilt spawn room");
                source.getServer().getCommands().performPrefixedCommand(source.getServer().createCommandSourceStack().withPermission(4), "scoreboard players set #run ftm_state 4");
                provider.leave(player);
                helper.assertTrue(player.serverLevel() == source, "Fresh dungeon return works");
                System.out.println("CHAOS_REGENERATION_PASS menuConfirm=true onlineOfflineGuards=true changedLayout=true actualCleanup=true actualBuild=true spawnEntryReturn=true");
                helper.succeed();
            } finally { player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED); }
        });
    }
}
