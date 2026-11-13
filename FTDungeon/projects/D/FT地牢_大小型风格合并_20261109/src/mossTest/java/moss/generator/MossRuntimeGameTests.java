package moss.generator;

import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftmossgen")
@PrefixGameTestTemplate(false)
public final class MossRuntimeGameTests {
    private static final Set<String> REPLAYED = new HashSet<>();
    private static void build(GameTestHelper helper, ServerLevel level, Engine.Job job, Runnable complete) {
        helper.assertTrue(!"FAILED".equals(job.stage), "Real moss construction failed: " + job.error);
        if ("COMPLETE".equals(job.stage)) {
            helper.assertTrue(Engine.temporaryTicketCount(level) == 0, "COMPLETE releases all temporary Engine chunk tickets");
            complete.run();
            return;
        }
        Engine.tick(level, 32768);
        if ("BUILDING".equals(job.stage) && job.pendingStart >= 0
                && job.batchCursor > job.pendingStart && job.batchCursor < job.pendingEnd
                && REPLAYED.add(job.plan.id)) {
            Engine.Job recovered = Store.load(Store.get(level).save(new net.minecraft.nbt.CompoundTag())).jobs.get(job.plan.id);
            helper.assertTrue(recovered.pendingStart == job.pendingStart && recovered.pendingEnd == job.pendingEnd
                    && recovered.writeCursor == job.writeCursor && recovered.batchCursor == -1,
                    "Partial build persists ownership before writes and reload resets transient replay progress");
            job.batchCursor = recovered.batchCursor;
            System.out.println("MOSS_PARTIAL_REPLAY_PASS:" + job.plan.id + ":" + job.pendingStart + ".." + job.pendingEnd);
        }
        helper.runAfterDelay(1, () -> build(helper, level, job, complete));
    }

    private static void force(ServerLevel level, Planner.Plan plan, boolean value) {
        Set<Long> visited = new HashSet<>();
        for (var piece : plan.pieces) {
            var bounds = Planner.bounds(piece);
            for (int east = (int) bounds.minX >> 4; east <= ((int) bounds.maxX - 1) >> 4; east++) {
                for (int south = (int) bounds.minZ >> 4; south <= ((int) bounds.maxZ - 1) >> 4; south++) {
                    if (visited.add(net.minecraft.world.level.ChunkPos.asLong(east, south))) level.setChunkForced(east, south, value);
                }
            }
        }
    }

    private static ServerPlayer player(ServerLevel level, BlockPos feet) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "MossNativeQA"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setPos(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        level.addNewPlayer(player);
        return player;
    }

    static void geometry(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        MossPlannerGameTests.portalChoice(helper, job.plan);
        BlockPos anchor = Catalog.pos(job.plan.bossPortal);
        var root = level.getBlockState(anchor);
        helper.assertTrue(root.getBlock() instanceof DungeonGateBlock, "Built moss boss portal is an actual FT gate");
        helper.assertTrue(root.getValue(DungeonGateBlock.PART) == DungeonGateBlock.ANCHOR_PART
            && DungeonGateBlock.ANCHOR_PART == 1, "Frozen portal position is the native bottom-center anchor PART=1, never the bottom-left part");
        DungeonGateBlock block = (DungeonGateBlock) root.getBlock();
        Direction facing = root.getValue(DungeonGateBlock.FACING);
        helper.assertTrue(DungeonGateBlock.complete(level, anchor, block, facing), "All twelve real FT parts match facing and anchor");
        helper.assertTrue(level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity, "Real gate block entity is present");
        var anchorEntity = (DungeonGateBlockEntity) level.getBlockEntity(anchor);
        helper.assertTrue(anchorEntity.getBlockPos().equals(anchor) && DungeonGateBlock.anchor(anchor, root).equals(anchor),
            "Actual block entity, frozen portal and native center anchor resolve to the same world position");
        Set<BlockPos> door = new HashSet<>();
        for (int part = 0; part < 12; part++) door.add(DungeonGateBlock.partPosition(anchor, facing, part));
        for (int column = 0; column < 3; column++) {
            BlockPos floor = DungeonGateBlock.partPosition(anchor, facing, column).below();
            helper.assertTrue(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Every gate column has full original support");
        }
        for (BlockPos position : door) {
            for (Direction side : new Direction[] {facing, facing.getOpposite()}) {
                helper.assertTrue(level.getBlockState(position.relative(side)).isAir(), "Both sides of all gate parts retain AIR clearance: " + position);
            }
        }
        var room = job.plan.pieces.stream().filter(piece -> piece.index == job.plan.bossPortalPiece).findFirst().orElseThrow();
        int strict = 0;
        for (var cell : Catalog.blocks(room.key)) {
            BlockPos world = Planner.transform(Catalog.vec(cell.position()), room.size, room.rotation, room.origin);
            if (door.contains(world)) {
                helper.assertTrue(cell.state().isAir(), "All FT door cells originally were AIR; no room blocks replaced: " + world);
                continue;
            }
            if (cell.state().isAir()) continue;
            var expected = cell.state().rotate(Rotation.values()[room.rotation]);
            helper.assertTrue(level.getBlockState(world).equals(expected), "Boss door cannot overwrite any other original non-AIR room cell: " + world + ";expected=" + expected + ";actual=" + level.getBlockState(world));
            strict++;
        }
        helper.assertTrue(strict > 100, "Substantial original farthest-room material remains strictly unchanged");
        Map<BlockPos, Integer> ports = new HashMap<>();
        for (var piece : job.plan.pieces) {
            for (var port : piece.ports) for (int[] cell : port.cells) ports.put(Catalog.pos(cell), piece.index);
        }
        int crossings = 0;
        for (var entry : ports.entrySet()) {
            for (BlockPos neighbor : new BlockPos[] {entry.getKey().north(), entry.getKey().south(), entry.getKey().east(), entry.getKey().west()}) {
                Integer owner = ports.get(neighbor);
                if (owner == null || owner.equals(entry.getValue())) continue;
                BlockPos feet = entry.getKey();
                helper.assertTrue(level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                    && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP),
                    "Connected graph endpoint has actual walkable feet/head clearance and support: " + feet);
                crossings++;
            }
        }
        helper.assertTrue(crossings >= job.plan.edges * 2, "Every connected room boundary has actual traversable endpoints");
    }

    private static void awaitBoss(GameTestHelper helper, ServerLevel level, Engine.Job job, int attempts, java.util.function.Consumer<LivingEntity> ready) {
        Entity entity = job.gameplay.bossUuid == null ? null : level.getEntity(UUID.fromString(job.gameplay.bossUuid));
        if (entity instanceof LivingEntity living && living.isAlive()) {
            for (var intent : job.gameplay.intents.values()) {
                Entity tracked = level.getEntity(UUID.fromString(intent.uuid));
                if (tracked instanceof Mob mob) mob.setNoAi(true);
            }
            ready.accept(living);
            return;
        }
        helper.assertTrue(attempts > 0, "Actual boss failed to materialize from persisted intent");
        Gameplay.tick(level);
        helper.runAfterDelay(1, () -> awaitBoss(helper, level, job, attempts - 1, ready));
    }

    private static void locked(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        helper.assertTrue(!"VICTORY".equals(job.gameplay.bossState) && !job.gameplay.rewardGranted, "Nonplayer/missing boss never gives victory or reward");
        for (BlockPos cell : Gameplay.gateCells(job.plan)) helper.assertTrue(!level.getBlockState(cell).isAir(), "Every return barrier remains locked");
    }

    private static void crossGate(GameTestHelper helper, ServerLevel level, ServerPlayer player, BlockPos anchor, int attempts, Runnable complete) {
        var state = level.getBlockState(anchor);
        helper.assertTrue(state.getBlock() instanceof DungeonGateBlock && state.getValue(DungeonGateBlock.PART) == DungeonGateBlock.ANCHOR_PART,
            "Travel targets an actual FT bottom-center gate anchor");
        if (!state.getValue(DungeonGateBlock.OPEN) || state.getValue(DungeonGateBlock.MOVING)) {
            helper.assertTrue(attempts > 0, "Actual FT gate never finished opening: " + anchor);
            helper.runAfterDelay(1, () -> crossGate(helper, level, player, anchor, attempts - 1, complete));
            return;
        }
        player.setPos(anchor.getX() + 0.5, anchor.getY() + 0.25, anchor.getZ() + 0.5);
        state.getBlock().entityInside(state, level, anchor, player);
        complete.run();
    }

    @GameTest(template = "empty", timeoutTicks = 30000)
    public static void realBuildBossGateAndPlayerOnlyVictory(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(MossMod.DIMENSION);
        helper.assertTrue(level != null, "Dedicated moss dimension is registered");
        Engine.Job obstructed = Engine.plan(level, 20261107L, new BlockPos(32768, 100, 0));
        var first = obstructed.plan.pieces.get(0);
        BlockPos obstacle = Planner.transform(new int[] {0, 4, 0}, first.size, first.rotation, first.origin);
        level.setBlockAndUpdate(obstacle, net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.defaultBlockState());
        Engine.start(level, obstructed.plan.id);
        Engine.tick(level, 1);
        helper.assertTrue(Engine.temporaryTicketCount(level) > 0, "Active preflight acquires real temporary Engine chunk tickets");
        Engine.pause(level, obstructed.plan.id);
        helper.assertTrue("PAUSED".equals(obstructed.stage) && Engine.temporaryTicketCount(level) == 0, "Pause immediately releases temporary Engine chunk tickets");
        Engine.start(level, obstructed.plan.id);
        awaitFailure(helper, level, obstructed, obstacle, () -> completeLifecycle(helper, level));
    }

    private static void awaitFailure(GameTestHelper helper, ServerLevel level, Engine.Job job, BlockPos obstacle, Runnable complete) {
        if ("FAILED".equals(job.stage)) {
            helper.assertTrue(Engine.temporaryTicketCount(level) == 0 && job.error != null && !job.error.isBlank(), "Real obstacle failure records error and releases every temporary ticket");
            helper.assertTrue(level.getBlockState(obstacle).is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK), "Failed preflight preserves actual obstacle");
            System.out.println("MOSS_TICKETS_PAUSE_FAILURE_PASS: acquired;pause=0;obstacleFailure=0");
            complete.run();
            return;
        }
        helper.assertTrue(!"BUILDING".equals(job.stage) && !"COMPLETE".equals(job.stage), "Occupied preflight cannot begin writes");
        Engine.tick(level, 32768);
        helper.runAfterDelay(1, () -> awaitFailure(helper, level, job, obstacle, complete));
    }

    private static void completeLifecycle(GameTestHelper helper, ServerLevel level) {
        Engine.Job job = Engine.plan(level, 20261107L, new BlockPos(0, 100, 0));
        Engine.start(level, job.plan.id);
        build(helper, level, job, () -> {
            geometry(helper, level, job);
            force(level, job.plan, true);
            menuIntegration(helper, job);
            ServerPlayer player = player(level, Catalog.pos(job.plan.bossPortal).relative(Direction.NORTH, 2));
            var menu = player.containerMenu;
            BlockPos portal = Catalog.pos(job.plan.bossPortal);
            var state = level.getBlockState(portal);
            state.use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(portal), state.getValue(DungeonGateBlock.FACING), portal, false));
            helper.assertTrue(player.containerMenu == menu, "Boss FT gate interaction must never open the entrance generator selection menu");
            crossGate(helper, level, player, portal, 160, () -> awaitBoss(helper, level, job, 120, boss -> {
                helper.assertTrue("FIGHTING".equals(job.gameplay.bossState), "Boss gate starts its own instance fight");
                helper.assertTrue(player.blockPosition().distSqr(Catalog.pos(job.gameplay.bossEntry)) < 9, "Boss gate routes to this instance's arena");
                String uuid = job.gameplay.bossUuid;
                player.setPos(portal.getX() + 0.5, portal.getY() + 0.25, portal.getZ() + 0.5);
                var afterFirst = player.position();
                var opened = level.getBlockState(portal);
                opened.getBlock().entityInside(opened, level, portal, player);
                helper.assertTrue(uuid.equals(job.gameplay.bossUuid) && player.position().equals(afterFirst), "Immediate valid nearby reuse cannot bypass Boss gate cooldown");
                BlockPos arena = Catalog.pos(job.gameplay.bossEntry);
                player.setPos(arena.getX() + 0.5, arena.getY(), arena.getZ() + 0.5);
                boss.discard();
                helper.runAfterDelay(2, () -> {
                    Gameplay.tick(level);
                    locked(helper, level, job);
                    Engine.Job other = Engine.plan(level, 20261107L, new BlockPos(8192, 100, 0));
                    Engine.start(level, other.plan.id);
                    build(helper, level, other, () -> nonplayer(helper, level, job, other, player));
                });
            }));
        });
    }

    private static void menuIntegration(GameTestHelper helper, Engine.Job completed) {
        ServerLevel source = helper.getLevel().getServer().overworld();
        BlockPos anchor = new BlockPos(500000, 150, 500000);
        var block = net.farmingtales.ftdungeonmobs.FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        for (int part = 0; part < 12; part++) {
            BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            if (part < 3) source.setBlockAndUpdate(point.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            source.setBlock(point, block.defaultBlockState().setValue(DungeonGateBlock.PART, part).setValue(DungeonGateBlock.FACING, Direction.NORTH), 18);
        }
        BlockPos brazier = anchor.east(3);
        source.setBlockAndUpdate(brazier.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        source.setBlockAndUpdate(brazier, net.farmingtales.ftdungeonmobs.FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState()
            .setValue(net.farmingtales.ftdungeonmobs.BloodthornBrazierBlock.TIER, 2));
        helper.assertTrue(DungeonGateBlock.complete(source, anchor, block, Direction.NORTH), "Menu fixture has complete native supported FT gate");
        ServerPlayer administrator = player(source, anchor.south(2));
        administrator.setGameMode(GameType.CREATIVE);
        try {
            net.farmingtales.ftdungeonmobs.DungeonGateGenerators.open(administrator, anchor);
            helper.assertTrue(administrator.containerMenu instanceof net.farmingtales.ftdungeonmobs.DungeonGeneratorMenu, "Actual FT entrance opens production generator menu");
            var menu = administrator.containerMenu;
            var providers = net.farmingtales.ftdungeonmobs.generator.DungeonGenerators.all();
            int selected = -1;
            for (int index = 0; index < providers.size(); index++) if (providers.get(index).id().equals(MossBossPortals.ID)) selected = index;
            helper.assertTrue(selected >= 0 && selected < 21 && !menu.getSlot(selected).getItem().isEmpty(), "Moss provider has an actual visible production menu icon");
            var visible = menu.getSlot(selected).getItem();
            helper.assertTrue(visible.is(providers.get(selected).icon().getItem())
                && visible.getHoverName().getString().contains(providers.get(selected).title().getString()),
                "Actual displayed slot icon and title identify the registered ftmossgen:forest provider");
            menu.clicked(selected, 0, net.minecraft.world.inventory.ClickType.PICKUP, administrator);
            var entity = (DungeonGateBlockEntity) source.getBlockEntity(anchor);
            helper.assertTrue(entity.generatorId().equals(MossBossPortals.ID) && entity.hasGeneratorSeed()
                && entity.generatorInstance().isEmpty() && menu.getCarried().isEmpty(), "Production menu selects moss and freezes seed without stealing icon or starting unsolicited build");
            net.farmingtales.ftdungeonmobs.DungeonGateGenerators.choose(administrator, entity, MossBossPortals.ID);
            entity.configureGenerator(MossBossPortals.ID, completed.plan.seed);
            ServerLevel forest = source.getServer().getLevel(MossMod.DIMENSION);
            int jobs = Store.get(forest).jobs.size();
            net.farmingtales.ftdungeonmobs.DungeonGateGenerators.prepare(administrator, entity);
            net.farmingtales.ftdungeonmobs.DungeonGateGenerators.prepare(administrator, entity);
            helper.assertTrue(entity.generatorInstance().equals(completed.plan.id) && entity.generatorTier() == 2
                && Store.get(forest).jobs.size() == jobs && "COMPLETE".equals(completed.stage),
                "Actual Moss provider prepare reuses exactly matching COMPLETE instance without allocating or rebuilding");
            helper.assertTrue(Engine.temporaryTicketCount(forest) == 0, "Preparing an already COMPLETE instance does not acquire build tickets");
            for (int part = 0; part < 12; part++) {
                BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
                source.setBlock(point, source.getBlockState(point).setValue(DungeonGateBlock.OPEN, true)
                    .setValue(DungeonGateBlock.MOVING, false), 18);
            }
            administrator.closeContainer();
            administrator.setGameMode(GameType.SURVIVAL);
            administrator.setPos(anchor.getX() + 0.5, anchor.getY() + 0.25, anchor.getZ() + 0.5);
            Vec3 original = administrator.position();
            helper.assertTrue(net.farmingtales.ftdungeonmobs.DungeonGateGenerators.travel(administrator, anchor, block, false), "Actual FT gate handles selected Moss provider travel");
            helper.assertTrue(administrator.serverLevel() == forest && administrator.blockPosition().distSqr(Catalog.pos(completed.plan.entry)) < 4,
                "External production gate/provider chain admits normal player to its exact COMPLETE moss instance");
            helper.assertTrue(administrator.getPersistentData().contains("ftmossgen_return", 10), "Provider entry persists original external gate return point");
            var provider = net.farmingtales.ftdungeonmobs.generator.DungeonGenerators.find(MossBossPortals.ID).orElseThrow();
            helper.assertTrue(provider instanceof net.farmingtales.ftdungeonmobs.generator.MossDungeonGenerator, "Registered entry provider is actual production MossDungeonGenerator");
            provider.leave(administrator);
            helper.assertTrue(administrator.serverLevel() == source && administrator.position().distanceToSqr(original) < 0.001
                && !administrator.getPersistentData().contains("ftmossgen_return"), "MossDungeonGenerator.leave restores original overworld gate point and consumes return NBT");
            helper.assertTrue(!net.farmingtales.ftdungeonmobs.DungeonGateGenerators.automaticAllowed(administrator, anchor), "External source gate cannot immediately auto-reenter after return");
            block.entityInside(source.getBlockState(anchor), source, anchor, administrator);
            helper.assertTrue(administrator.serverLevel() == source, "Actual source gate entityInside respects automatic reentry guard");
            administrator.setPos(anchor.getX() + 6, anchor.getY(), anchor.getZ());
            net.farmingtales.ftdungeonmobs.DungeonGateGenerators.playerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(
                net.minecraftforge.event.TickEvent.Phase.END, administrator));
            helper.assertTrue(net.farmingtales.ftdungeonmobs.DungeonGateGenerators.automaticAllowed(administrator, anchor), "Walking clear of actual external gate rearms entry");
            System.out.println("MOSS_MENU_PROVIDER_PASS: actual menu selection;native gate+brazier;COMPLETE seed reuse;prepare idempotent;provider travel;provider leave;automatic reentry guard");
        } finally {
            administrator.closeContainer();
            administrator.serverLevel().removePlayerImmediately(administrator, Entity.RemovalReason.DISCARDED);
        }
    }

    private static void nonplayer(GameTestHelper helper, ServerLevel level, Engine.Job first, Engine.Job second, ServerPlayer player) {
        force(level, second.plan, true);
        helper.assertTrue(Gameplay.startBoss(level, second), "Independent instance can start its own boss");
        awaitBoss(helper, level, second, 120, boss -> {
            helper.assertTrue(boss.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "Actual nonplayer lethal damage is applied");
            helper.runAfterDelay(2, () -> {
                Gameplay.tick(level);
                locked(helper, level, first);
                locked(helper, level, second);
                Engine.Job winner = Engine.plan(level, 20261107L, new BlockPos(16384, 100, 0));
                Engine.start(level, winner.plan.id);
                build(helper, level, winner, () -> victory(helper, level, first, second, winner, player));
            });
        });
    }

    private static void victory(GameTestHelper helper, ServerLevel level, Engine.Job first, Engine.Job second, Engine.Job winner, ServerPlayer player) {
        force(level, winner.plan, true);
        helper.assertTrue("READY".equals(winner.gameplay.bossState), "Earlier gate did not start another instance boss");
        helper.assertTrue(Gameplay.startBoss(level, winner), "Third independent boss fight starts");
        awaitBoss(helper, level, winner, 120, boss -> {
            helper.assertTrue(boss.hurt(level.damageSources().playerAttack(player), 10000), "Actual ServerPlayer deals lethal damage through Forge death event");
            helper.runAfterDelay(70, () -> {
                Gameplay.tick(level);
                helper.assertTrue("VICTORY".equals(winner.gameplay.bossState) && winner.gameplay.rewardGranted, "Only real player kill grants victory and guarded reward");
                for (BlockPos cell : Gameplay.gateCells(winner.plan)) helper.assertTrue(level.getBlockState(cell).isAir(), "All winning-instance return barriers open");
                locked(helper, level, first);
                locked(helper, level, second);
                BlockPos back = MossBossPortals.gates(winner.plan).stream().filter(MossBossPortals.Gate::returning).findFirst().orElseThrow().anchor();
                crossGate(helper, level, player, back, 160, () -> {
                helper.assertTrue(player.blockPosition().distSqr(Catalog.pos(winner.plan.entry)) < 4, "Victory return routes to correct instance entrance");
                Store.flush(level);
                Store detached = Store.load(Store.get(level).save(new net.minecraft.nbt.CompoundTag()));
                helper.assertTrue("VICTORY".equals(detached.jobs.get(winner.plan.id).gameplay.bossState), "Persisted victory survives NBT reload");
                level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                force(level, first.plan, false);
                force(level, second.plan, false);
                force(level, winner.plan, false);
                System.out.println("MOSS_RUNTIME_PASS:real build;12-part farthest door;original room preserved;no entry menu;instance routing;cooldown;nonplayer locked;player victory;correct return");
                helper.succeed();
                });
            });
        });
    }
}
