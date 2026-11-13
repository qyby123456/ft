package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DungeonBossGateGameTests {
    private static int assertions;

    private DungeonBossGateGameTests() {}

    @GameTest(template = "empty", batch = "zzzz_boss_gate", timeoutTicks = 400)
    public static void oneWayPartyRally(GameTestHelper helper) {
        assertions = 0;
        Fixture fixture = new Fixture(helper);
        try {
            fixture.prepare();
            geometry(helper, fixture.anchor);
            fixture.checkConfiguration();
            helper.runAfterDelay(45, () -> {
                try {
                    fixture.checkRally();
                    fixture.addLatePlayers();
                    helper.runAfterDelay(22, () -> {
                        try {
                            fixture.checkLatePlayersAndReset();
                            System.out.println("BOSS_GATE_VERIFIED assertions=" + assertions
                                    + " real_server_players=true scope=dungeon_only one_way=true safe_party=true persistence=true late_arrivals=true");
                            helper.succeed();
                        } finally {
                            fixture.cleanup();
                        }
                    });
                } catch (Throwable error) {
                    fixture.cleanup();
                    throw error;
                }
            });
        } catch (Throwable error) {
            fixture.cleanup();
            throw error;
        }
    }

    @GameTest(template = "empty", batch = "zzzzz_boss_tick", timeoutTicks = 400)
    public static void playerTickTriggersRally(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            fixture.prepare();
            fixture.checkConfiguration();
            helper.runAfterDelay(45, () -> {
                fixture.leader.moveTo(point(fixture.anchor, Direction.NORTH, 0, -0.8));
                MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, fixture.leader));
                helper.runAfterDelay(1, () -> {
                    try {
                        fixture.leader.move(MoverType.SELF, new Vec3(0, 0, 1.6));
                        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, fixture.leader));
                        check(helper, DungeonBossGateApi.state(fixture.level).engaged(), "Forge PlayerTick subscriber detects a real crossing");
                        check(helper, fixture.binding.bounds().contains(fixture.teammate.position()), "PlayerTick gathers a distant teammate");
                        System.out.println("BOSS_GATE_PLAYER_TICK_PASS: registered Forge event; real forward movement; distant teammate gathered");
                        helper.succeed();
                    } finally { fixture.cleanup(); }
                });
            });
        } catch (Throwable error) {
            fixture.cleanup();
            throw error;
        }
    }

    private static void geometry(GameTestHelper helper, BlockPos anchor) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Vec3 front = point(anchor, facing, 0, -0.8);
            Vec3 back = point(anchor, facing, 0, 0.8);
            check(helper, DungeonBossGateApi.crossesFront(anchor, facing, front, back), "Front crossing for " + facing);
            check(helper, !DungeonBossGateApi.crossesFront(anchor, facing, back, front), "Reverse cannot trigger for " + facing);
            check(helper, !DungeonBossGateApi.crossesFront(anchor, facing, front, point(anchor, facing, 0, -0.05)), "Approach is not a crossing");
            check(helper, !DungeonBossGateApi.crossesFront(anchor, facing, front, front), "Stationary does not trigger");
            check(helper, !DungeonBossGateApi.crossesFront(anchor, facing, point(anchor, facing, 1.3, -0.8),
                    point(anchor, facing, 1.3, 0.8)), "Outside the passage does not trigger");
            check(helper, !DungeonBossGateApi.crossesFront(anchor, facing, front.add(0, 4, 0), back.add(0, 4, 0)), "Above the gate does not trigger");
        }
    }

    private static Vec3 point(BlockPos anchor, Direction facing, double side, double back) {
        Direction right = facing.getClockWise();
        return Vec3.atBottomCenterOf(anchor).add(right.getStepX() * side - facing.getStepX() * back,
                3.0 / 16, right.getStepZ() * side - facing.getStepZ() * back);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        assertions++;
        helper.assertTrue(condition, message);
    }

    private static final class Fixture {
        private final GameTestHelper helper;
        private final ServerLevel level;
        private final BlockPos anchor = new BlockPos(4096, 64, 4096);
        private final List<TestPlayer> players = new ArrayList<>();
        private final List<Long> forced = new ArrayList<>();
        private final CompoundTag savedRound;
        private final CompoundTag savedBoss;
        private final DungeonBossGateApi.Binding binding;
        private DungeonGateData round;
        private TestPlayer leader;
        private TestPlayer teammate;
        private TestPlayer outsider;
        private TestPlayer netherPlayer;
        private TestPlayer late;
        private TestPlayer dead;
        private CompoundTag returnTag;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
            check(helper, helper.getLevel().getServer() instanceof GameTestServer && level != null,
                    "Only run against the isolated Forge GameTest server");
            savedRound = DungeonGateData.get(level.getServer()).save(new CompoundTag());
            DungeonBossGateData previous = level.getDataStorage().get(DungeonBossGateData::load, DungeonBossGateData.FILE_NAME);
            savedBoss = previous == null ? new CompoundTag() : previous.save(new CompoundTag());
            binding = new DungeonBossGateApi.Binding(anchor, Direction.NORTH, anchor.offset(-8, 0, 4),
                    anchor.offset(8, 5, 18), anchor.offset(0, 0, 8));
        }

        void prepare() {
            for (int chunkX = (anchor.getX() - 10) >> 4; chunkX <= (anchor.getX() + 10) >> 4; chunkX++) {
                for (int chunkZ = (anchor.getZ() - 4) >> 4; chunkZ <= (anchor.getZ() + 21) >> 4; chunkZ++) {
                    long identity = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
                    if (!level.getForcedChunks().contains(identity)) {
                        level.setChunkForced(chunkX, chunkZ, true);
                        forced.add(identity);
                    }
                }
            }
            for (BlockPos position : BlockPos.betweenClosed(anchor.offset(-10, -1, -4), anchor.offset(10, 6, 21))) {
                level.setBlock(position, position.getY() == 63 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
            round = new DungeonGateData();
            level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", round);
            level.getDataStorage().set(DungeonBossGateData.FILE_NAME, new DungeonBossGateData());
            leader = player(level, point(anchor, Direction.NORTH, 0, -1.2), "FTBossLeader");
            teammate = player(level, new Vec3(8192.5, 64, 8192.5), "FTBossFar");
            outsider = player(level.getServer().overworld(), new Vec3(9216.5, 64, 9216.5), "FTBossOutside");
            netherPlayer = player(level.getServer().getLevel(Level.NETHER), new Vec3(9216.5, 100, 9216.5), "FTBossNether");
            returnTag = new CompoundTag();
            returnTag.putString("Dimension", "minecraft:overworld");
            returnTag.putDouble("X", 19.5);
            returnTag.putDouble("Y", 75);
            returnTag.putDouble("Z", 31.5);
            returnTag.putFloat("Yaw", 72);
            returnTag.putLong("LastTravel", 123);
            for (TestPlayer player : List.of(leader, teammate)) {
                CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                persistent.put("FTDungeonGate", returnTag.copy());
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
            }
        }

        void checkConfiguration() {
            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            DungeonBossGateCommands.register(dispatcher);
            CommandSourceStack source = level.getServer().createCommandSourceStack().withLevel(level).withPermission(2);
            try {
                check(helper, dispatcher.execute("ftdungeon bossgate create 4096 64 4096 north", source) == 1, "OP command creates the gate");
                check(helper, !DungeonBossGateApi.canOpen(level, anchor), "Unbound gate remains closed");
                check(helper, dispatcher.execute("ftdungeon bossgate bind 4096 64 4096 4088 64 4100 4104 69 4114 4096 64 4104", source) == 1,
                        "OP command saves a Boss room");
                check(helper, dispatcher.execute("ftdungeon bossgate status", source) == 1, "Status command lists the binding");
                boolean denied = false;
                try { dispatcher.execute("ftdungeon bossgate status", source.withPermission(0)); }
                catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { denied = true; }
                check(helper, denied, "Players cannot configure Boss gates without OP permission");
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
                throw new IllegalStateException(exception);
            }
            int entities = 0;
            for (int part = 0; part < 12; part++) {
                BlockPos position = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
                check(helper, level.getBlockState(position).is(FTDungeonMobs.DUNGEON_BOSS_GATE.get()), "Created all twelve gate parts");
                if (level.getBlockEntity(position) != null) { entities++; }
            }
            check(helper, entities == 1, "Only the anchor has a block entity");
            check(helper, !DungeonBossGateApi.canOpen(level, anchor), "No round means no Boss entry");
            boolean invalid = false;
            try { new DungeonBossGateApi.Binding(anchor, Direction.NORTH, anchor.offset(-3, 0, -8), anchor.offset(3, 4, -3), anchor.north(4)); }
            catch (IllegalArgumentException expected) { invalid = true; }
            check(helper, invalid, "Cannot bind a room on the corridor side");
            invalid = false;
            try { new DungeonBossGateApi.Binding(anchor, Direction.UP, binding.minimum(), binding.maximum(), binding.arrival()); }
            catch (IllegalArgumentException expected) { invalid = true; }
            check(helper, invalid, "Cannot bind a vertical facing");
            invalid = false;
            try { DungeonBossGateApi.bind(outsider.serverLevel(), binding); }
            catch (IllegalStateException expected) { invalid = true; }
            check(helper, invalid, "Cannot bind Boss gates in another dimension");
            invalid = false;
            try { DungeonBossGateApi.create(level, anchor, Direction.NORTH); }
            catch (IllegalArgumentException expected) { invalid = true; }
            check(helper, invalid && level.getBlockState(anchor).is(FTDungeonMobs.DUNGEON_BOSS_GATE.get()), "Creation never overwrites a gate or building");
            round.bind(Level.OVERWORLD, new BlockPos(0, 64, 0));
            round.setTier(4);
            round.startRound(leader.getUUID(), level.getServer().overworld().getGameTime());
            round.addParticipant(teammate.getUUID());
            check(helper, DungeonBossGateApi.canOpen(level, anchor), "Configured gate opens in an active round");
            DungeonGateInteraction.refreshControl(level, anchor);
            check(helper, level.getBlockState(anchor).getValue(DungeonGateBlock.MOVING), "Reuse the real moving iron-gate animation");
            check(helper, DungeonBossGateData.load(DungeonBossGateApi.state(level).save(new CompoundTag())).binding(anchor).equals(binding),
                    "Room configuration survives serialization");
        }

        void checkRally() {
            BlockState state = level.getBlockState(anchor);
            check(helper, state.getValue(DungeonGateBlock.OPEN) && !state.getValue(DungeonGateBlock.MOVING), "Door finishes opening before passage");
            check(helper, state.getValue(DungeonGateBlock.PORTAL_TIER) == 4, "Boss gate uses this round's dimension difficulty");
            Vec3 front = point(anchor, Direction.NORTH, 0, -0.8);
            Vec3 back = point(anchor, Direction.NORTH, 0, 0.8);
            leader.moveTo(front);
            state.use(level, leader, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(anchor), Direction.NORTH, anchor, false));
            check(helper, !DungeonBossGateApi.state(level).engaged(), "Right-click does not gather the party");
            leader.moveTo(point(anchor, Direction.NORTH, 0, -0.05));
            check(helper, !DungeonBossGateApi.observeMovement(leader, front, leader.position()), "Approaching the plane does not gather");
            leader.moveTo(back);
            leader.move(MoverType.SELF, front.subtract(back));
            check(helper, leader.getZ() > Vec3.atBottomCenterOf(anchor).z, "Reverse movement is physically blocked");
            leader.moveTo(back);
            check(helper, !DungeonBossGateApi.observeMovement(leader, back, front) && leader.position().distanceToSqr(back) < 0.01,
                    "Reverse crossing correction does not begin a rally");
            Vec3 far = teammate.position();
            room(Blocks.STONE.defaultBlockState());
            level.setBlock(binding.arrival(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(binding.arrival().above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            check(helper, DungeonBossGateApi.planLandings(level, binding, List.of(leader, teammate)).isEmpty(), "One safe column cannot accept two players");
            leader.moveTo(back);
            check(helper, !DungeonBossGateApi.observeMovement(leader, front, back), "Insufficient room aborts the crossing");
            check(helper, leader.position().distanceToSqr(front) < 0.01 && teammate.position().distanceToSqr(far) < 0.01,
                    "Unsafe preflight keeps the entire party outside");
            room(Blocks.AIR.defaultBlockState());
            BlockPos arrival = binding.arrival();
            level.setBlock(arrival.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            Vec3 landing = DungeonBossGateApi.planLandings(level, binding, List.of(leader)).get(leader.getUUID());
            check(helper, landing != null && !BlockPos.containing(landing).equals(arrival), "Avoid hazardous support blocks");
            level.setBlock(arrival.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(arrival, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
            landing = DungeonBossGateApi.planLandings(level, binding, List.of(leader)).get(leader.getUUID());
            check(helper, landing != null && !BlockPos.containing(landing).equals(arrival), "Avoid liquids in the player's body");
            level.setBlock(arrival, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            Consumer<DungeonBossGatherEvent> cancelling = event -> {
                check(helper, event.level() == level && event.players().size() == 2 && event.landings().size() == 2,
                        "Integration event contains only this dungeon's online players");
                check(helper, event.tier() == 4 && event.roundId().equals(round.roundId()) && event.binding().equals(binding),
                        "Integration event exposes the frozen difficulty and room");
                event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(cancelling);
            try {
                leader.moveTo(back);
                check(helper, !DungeonBossGateApi.observeMovement(leader, front, back), "Integration cancellation aborts the rally");
                check(helper, teammate.position().distanceToSqr(far) < 0.01 && !DungeonBossGateApi.state(level).engaged(), "Cancellation never moves a teammate");
            } finally { MinecraftForge.EVENT_BUS.unregister(cancelling); }
            teammate.rejectNext = true;
            leader.moveTo(back);
            check(helper, !DungeonBossGateApi.observeMovement(leader, front, back), "Rejected teleport does not mark the round engaged");
            check(helper, teammate.position().distanceToSqr(far) < 0.01 && leader.position().distanceToSqr(front) < 0.01,
                    "A rejected transfer rolls back already-moved players");
            Vec3 outside = outsider.position();
            Vec3 nether = netherPlayer.position();
            leader.moveTo(front);
            leader.move(MoverType.SELF, back.subtract(front));
            check(helper, leader.getZ() > Vec3.atBottomCenterOf(anchor).z, "Open gate allows physical forward movement");
            check(helper, DungeonBossGateApi.observeMovement(leader, front, leader.position()), "Real forward crossing gathers the party");
            DungeonBossGateData data = DungeonBossGateApi.state(level);
            check(helper, data.engaged() && data.activeBinding().equals(binding), "Boss rally is now locked for this round");
            for (TestPlayer player : List.of(leader, teammate)) {
                check(helper, binding.bounds().contains(player.position()), "Every live player in the dungeon reached the room");
                check(helper, data.arrived(player.getUUID()), "Record each successful arrival");
                check(helper, player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("FTDungeonGate").equals(returnTag),
                        "Do not overwrite the original dungeon return record or cooldown");
                check(helper, player.getDeltaMovement().equals(Vec3.ZERO) && player.fallDistance == 0, "Clear velocity and fall damage");
            }
            check(helper, !leader.getBoundingBox().intersects(teammate.getBoundingBox()), "Use separate non-overlapping landings");
            check(helper, outsider.position().equals(outside) && netherPlayer.position().equals(nether), "Never move overworld or Nether players");
            check(helper, !DungeonBossGateApi.canOpen(level, anchor), "Boss gate no longer admits another encounter this round");
            CompoundTag saved = data.save(new CompoundTag());
            DungeonBossGateData restored = DungeonBossGateData.load(saved);
            level.getDataStorage().set(DungeonBossGateData.FILE_NAME, restored);
            check(helper, DungeonBossGateApi.state(level).engaged() && restored.arrived(teammate.getUUID()), "Reload preserves the active room and arrivals");
            boolean locked = false;
            try { DungeonBossGateApi.unbind(level, anchor); }
            catch (IllegalStateException expected) { locked = true; }
            check(helper, locked, "Cannot rebind a running encounter");
            Vec3 bossPosition = teammate.position();
            check(helper, !DungeonBossGateApi.observeMovement(leader, front, back) && teammate.position().equals(bossPosition),
                    "Repeated crossings cannot gather twice");
        }

        void addLatePlayers() {
            late = player(level, new Vec3(8195.5, 64, 8192.5), "FTBossLate");
            DungeonBossGateApi.onLogin(new PlayerEvent.PlayerLoggedInEvent(late));
            dead = player(level, new Vec3(8198.5, 64, 8192.5), "FTBossDead");
            dead.setHealth(0);
            round.addParticipant(late.getUUID());
            round.addParticipant(dead.getUUID());
        }

        void checkLatePlayersAndReset() {
            DungeonBossGateData data = DungeonBossGateApi.state(level);
            check(helper, binding.bounds().contains(late.position()) && data.arrived(late.getUUID()), "Late entry and reconnect join the active Boss room");
            check(helper, !data.arrived(dead.getUUID()), "A player on the death screen waits for respawn");
            check(helper, level.getBlockState(anchor).getValue(DungeonGateBlock.MOVING)
                    || !level.getBlockState(anchor).getValue(DungeonGateBlock.OPEN), "Door closes behind the gathered party");
            check(helper, round.tier() == 4 && DungeonGateTravel.tierForLevel(level).orElse(0) == 4, "Rally never changes dungeon difficulty");
            for (TestPlayer player : List.of(leader, teammate, late, dead)) { round.removeParticipant(player.getUUID()); }
            check(helper, round.finishRoundIfEmpty(level.getServer().overworld().getGameTime() + DungeonGateData.ADMISSION_TICKS + 1),
                    "Empty finished round can end normally");
            check(helper, !DungeonBossGateApi.state(level).engaged() && data.binding(anchor).equals(binding), "Round end clears encounter state but keeps builder configuration");
            round.startRound(leader.getUUID(), level.getServer().overworld().getGameTime());
            check(helper, DungeonBossGateApi.canOpen(level, anchor) && !data.arrived(leader.getUUID()), "A new round rearms the same Boss gate");
            check(helper, DungeonBossGateApi.unbind(level, anchor) && DungeonBossGateApi.state(level).binding(anchor) == null,
                    "Administrator can remove an idle binding without removing blocks");
        }

        private void room(BlockState block) {
            for (BlockPos position : BlockPos.betweenClosed(binding.minimum(), binding.maximum())) {
                level.setBlock(position, block, Block.UPDATE_CLIENTS);
            }
        }

        private TestPlayer player(ServerLevel world, Vec3 position, String name) {
            world.getChunk(BlockPos.containing(position));
            world.setBlock(BlockPos.containing(position).below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            world.setBlock(BlockPos.containing(position), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            world.setBlock(BlockPos.containing(position).above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            TestPlayer player = new TestPlayer(world, name);
            player.moveTo(position);
            world.addNewPlayer(player);
            players.add(player);
            return player;
        }

        void cleanup() {
            for (TestPlayer player : players) {
                if (!player.isRemoved()) { player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED); }
            }
            players.clear();
            for (long identity : forced) {
                level.setChunkForced(net.minecraft.world.level.ChunkPos.getX(identity), net.minecraft.world.level.ChunkPos.getZ(identity), false);
            }
            forced.clear();
            level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", DungeonGateData.load(savedRound));
            level.getDataStorage().set(DungeonBossGateData.FILE_NAME, DungeonBossGateData.load(savedBoss));
        }
    }

    private static final class TestPlayer extends ServerPlayer {
        private boolean rejectNext;

        TestPlayer(ServerLevel level, String name) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), name));
            connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), this) {
                @Override
                public void send(Packet<?> packet) {}
                @Override
                public void send(Packet<?> packet, PacketSendListener listener) {}
            };
        }

        @Override
        public void teleportTo(ServerLevel level, double positionX, double positionY, double positionZ, float yaw, float pitch) {
            if (rejectNext) { rejectNext = false; return; }
            super.teleportTo(level, positionX, positionY, positionZ, yaw, pitch);
        }
    }
}
