package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTDungeonMobs.ID)
public final class DungeonBossGateApi {
    private static final Map<ServerPlayer, Sample> SAMPLES = new WeakHashMap<>();
    private static final Set<ServerLevel> GATHERING = java.util.Collections.newSetFromMap(new WeakHashMap<>());

    private DungeonBossGateApi() {}

    public record Binding(BlockPos gate, Direction facing, BlockPos minimum, BlockPos maximum, BlockPos arrival) {
        public Binding {
            if (gate == null || facing == null || !facing.getAxis().isHorizontal() || minimum == null
                    || maximum == null || arrival == null) {
                throw new IllegalArgumentException("Expected a horizontal gate and integer room coordinates.");
            }
            gate = gate.immutable();
            minimum = minimum.immutable();
            maximum = maximum.immutable();
            arrival = arrival.immutable();
            if (maximum.getX() - minimum.getX() < 2 || maximum.getX() - minimum.getX() >= 128
                    || maximum.getY() - minimum.getY() < 2 || maximum.getY() - minimum.getY() >= 64
                    || maximum.getZ() - minimum.getZ() < 2 || maximum.getZ() - minimum.getZ() >= 128
                    || !bounds(minimum, maximum).contains(Vec3.atBottomCenterOf(arrival))
                    || arrival.getY() + 1 > maximum.getY()) {
                throw new IllegalArgumentException("Room bounds must be 3..128 x 3..64 x 3..128, and contain the arrival.");
            }
            for (BlockPos corner : List.of(minimum, maximum, new BlockPos(minimum.getX(), minimum.getY(), maximum.getZ()),
                    new BlockPos(maximum.getX(), maximum.getY(), minimum.getZ()))) {
                if (DungeonGateBlock.canonical(gate, facing, Vec3.atBottomCenterOf(corner)).z < 24) {
                    throw new IllegalArgumentException("The entire Boss room must be behind the gate, at least one block from its plane.");
                }
            }
        }

        public AABB bounds() {
            return bounds(minimum, maximum);
        }

        private static AABB bounds(BlockPos minimum, BlockPos maximum) {
            return new AABB(minimum, maximum.offset(1, 1, 1));
        }
    }

    private record Sample(ServerLevel level, long time, Vec3 position) {}
    private record Departure(ServerPlayer player, Vec3 position, float yaw, float pitch) {}

    public static DungeonBossGateData state(ServerLevel level) {
        DungeonBossGateData data = DungeonBossGateData.get(level);
        DungeonGateData round = DungeonGateData.get(level.getServer());
        data.synchronizeRound(round.roundActive() ? round.roundId() : null);
        return data;
    }

    public static void bind(ServerLevel level, Binding binding) {
        DungeonBossGateData data = state(level);
        if (!validGate(level, binding) || !level.isInWorldBounds(binding.minimum())
                || !level.isInWorldBounds(binding.maximum()) || !level.getWorldBorder().isWithinBounds(binding.bounds())) {
            throw new IllegalArgumentException("Expected a complete Boss gate and room within this dungeon's world border.");
        }
        data.put(binding);
        DungeonGateInteraction.refreshControl(level, binding.gate());
    }

    public static boolean unbind(ServerLevel level, BlockPos gate) {
        boolean removed = state(level).remove(gate);
        DungeonGateInteraction.refreshControl(level, gate);
        return removed;
    }

    public static void create(ServerLevel level, BlockPos anchor, Direction facing) {
        state(level);
        if (facing == null || !facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Gate facing must be north, south, east or west; it faces away from the Boss room.");
        }
        DungeonGateBlock gate = FTDungeonMobs.DUNGEON_BOSS_GATE.get();
        BlockState state = gate.defaultBlockState().setValue(DungeonGateBlock.FACING, facing);
        for (int part = 0; part < 12; part++) {
            BlockPos position = DungeonGateBlock.partPosition(anchor, facing, part);
            if (!level.isInWorldBounds(position) || !level.getWorldBorder().isWithinBounds(position)
                    || !level.getBlockState(position).isAir() || !level.getFluidState(position).isEmpty()
                    || !level.getEntitiesOfClass(LivingEntity.class, new AABB(position)).isEmpty()) {
                throw new IllegalArgumentException("Boss gate requires 3 x 4 empty, dry, unoccupied blocks; no existing blocks will be replaced.");
            }
        }
        if (!state.canSurvive(level, anchor)) {
            throw new IllegalArgumentException("All three gate columns require a solid supporting floor.");
        }
        for (int part = 0; part < 12; part++) {
            level.setBlock(DungeonGateBlock.partPosition(anchor, facing, part), state.setValue(DungeonGateBlock.PART, part),
                    Block.UPDATE_ALL);
        }
    }

    public static boolean canOpen(ServerLevel level, BlockPos anchor) {
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON)) {
            return false;
        }
        DungeonBossGateData data = state(level);
        Binding binding = data.binding(anchor);
        return DungeonGateData.get(level.getServer()).roundActive() && !data.engaged() && binding != null
                && validGate(level, binding);
    }

    private static boolean validGate(ServerLevel level, Binding binding) {
        return level.hasChunkAt(binding.gate()) && level.getBlockState(binding.gate()).is(FTDungeonMobs.DUNGEON_BOSS_GATE.get())
                && level.getBlockState(binding.gate()).getValue(DungeonGateBlock.FACING) == binding.facing()
                && DungeonGateBlock.complete(level, binding.gate(), FTDungeonMobs.DUNGEON_BOSS_GATE.get(), binding.facing());
    }

    public static boolean crossesFront(BlockPos anchor, Direction facing, Vec3 previous, Vec3 current) {
        Vec3 before = DungeonGateBlock.canonical(anchor, facing, previous);
        Vec3 after = DungeonGateBlock.canonical(anchor, facing, current);
        if (before.z >= 8 || after.z < 8 || after.z <= before.z) {
            return false;
        }
        double fraction = (8 - before.z) / (after.z - before.z);
        double horizontal = before.x + (after.x - before.x) * fraction;
        double height = before.y + (after.y - before.y) * fraction;
        return horizontal > 10 && horizontal < 38 && height >= 3 && height < 54;
    }

    static boolean observeMovement(ServerPlayer player, Vec3 previous, Vec3 current) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON) || !player.isAlive()
                || previous.distanceToSqr(current) > 256 || GATHERING.contains(level)) {
            return false;
        }
        DungeonBossGateData data = state(level);
        for (Binding binding : data.bindings()) {
            if (!validGate(level, binding)) {
                continue;
            }
            if (crossesFront(binding.gate(), binding.facing(), current, previous)) {
                player.teleportTo(previous.x, previous.y, previous.z);
                player.setDeltaMovement(Vec3.ZERO);
                player.displayClientMessage(Component.translatable("message.ftdungeonmobs.boss_gate.one_way"), true);
                return false;
            }
            BlockState block = level.getBlockState(binding.gate());
            if (!data.engaged() && block.getValue(DungeonGateBlock.OPEN) && !block.getValue(DungeonGateBlock.MOVING)
                    && crossesFront(binding.gate(), binding.facing(), previous, current)) {
                boolean successful = gather(level, data, binding, List.copyOf(level.players()));
                if (!successful) {
                    player.teleportTo(previous.x, previous.y, previous.z);
                    player.setDeltaMovement(Vec3.ZERO);
                    player.displayClientMessage(Component.translatable("message.ftdungeonmobs.boss_gate.unsafe"), true);
                }
                return successful;
            }
        }
        return false;
    }

    private static boolean gather(ServerLevel level, DungeonBossGateData data, Binding binding, List<ServerPlayer> requested) {
        DungeonGateData round = DungeonGateData.get(level.getServer());
        if (!round.roundActive() || !GATHERING.add(level)) {
            return false;
        }
        try {
            UUID expectedRound = round.roundId();
            List<ServerPlayer> players = requested.stream().filter(player -> player.serverLevel() == level && player.isAlive()).toList();
            if (players.isEmpty()) {
                return false;
            }
            Map<UUID, Vec3> landings = planLandings(level, binding, players);
            if (landings.size() != players.size() || MinecraftForge.EVENT_BUS.post(new DungeonBossGatherEvent(level,
                    binding, players, landings, expectedRound, round.tier())) || !round.roundActive()
                    || !expectedRound.equals(round.roundId())
                    || !data.engaged() && !validGate(level, binding)
                    || !java.util.Objects.equals(data.binding(binding.gate()), binding)
                    || players.stream().anyMatch(player -> player.serverLevel() != level || !player.isAlive())
                    || !planLandings(level, binding, players).equals(landings)) {
                return false;
            }
            List<Departure> moved = new ArrayList<>();
            for (ServerPlayer player : players) {
                Vec3 landing = landings.get(player.getUUID());
                moved.add(new Departure(player, player.position(), player.getYRot(), player.getXRot()));
                if (player.isSleeping()) {
                    player.stopSleepInBed(true, true);
                }
                player.stopRiding();
                player.ejectPassengers();
                player.teleportTo(level, landing.x, landing.y, landing.z, binding.facing().getOpposite().toYRot(), 0);
                if (player.serverLevel() != level || player.position().distanceToSqr(landing) > 0.01) {
                    for (Departure departure : moved) {
                        Vec3 origin = departure.position();
                        departure.player().teleportTo(level, origin.x, origin.y, origin.z, departure.yaw(), departure.pitch());
                    }
                    return false;
                }
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0;
                SAMPLES.remove(player);
            }
            data.engage(binding.gate());
            for (ServerPlayer player : players) {
                data.markArrived(player.getUUID());
                player.sendSystemMessage(Component.translatable("message.ftdungeonmobs.boss_gate.gathered", players.size(), round.tier()));
            }
            for (Binding configured : data.bindings()) {
                if (level.hasChunkAt(configured.gate())) {
                    DungeonGateInteraction.refreshControl(level, configured.gate());
                }
            }
            level.playSound(null, binding.arrival(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 0.8F);
            return true;
        } finally {
            GATHERING.remove(level);
        }
    }

    static Map<UUID, Vec3> planLandings(ServerLevel level, Binding binding, List<ServerPlayer> players) {
        Map<UUID, Vec3> result = new LinkedHashMap<>();
        List<AABB> reserved = new ArrayList<>();
        Set<UUID> travelling = players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
        for (ServerPlayer player : players) {
            boolean found = false;
            for (int radius = 0; radius <= 16 && !found; radius++) {
                for (int offsetX = -radius; offsetX <= radius && !found; offsetX++) {
                    for (int offsetZ = -radius; offsetZ <= radius && !found; offsetZ++) {
                        if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) {
                            continue;
                        }
                        for (int height : new int[]{0, 1, -1, 2, -2}) {
                            BlockPos candidate = binding.arrival().offset(offsetX, height, offsetZ);
                            Vec3 position = Vec3.atBottomCenterOf(candidate);
                            AABB box = player.getDimensions(net.minecraft.world.entity.Pose.STANDING).makeBoundingBox(position);
                            if (!binding.bounds().contains(new Vec3(box.minX, box.minY, box.minZ))
                                    || !binding.bounds().contains(new Vec3(box.maxX, box.maxY, box.maxZ))
                                    || !level.getWorldBorder().isWithinBounds(box) || !level.isInWorldBounds(candidate.below())
                                    || reserved.stream().anyMatch(box::intersects)) {
                                continue;
                            }
                            level.getChunk(candidate);
                            BlockState floor = level.getBlockState(candidate.below());
                            if (!floor.isFaceSturdy(level, candidate.below(), Direction.UP) || hazardous(floor)
                                    || !floor.getFluidState().isEmpty() || !level.noCollision(player, box)
                                    || level.getBlockStates(box).anyMatch(state -> hazardous(state) || !state.getFluidState().isEmpty())
                                    || !level.getEntitiesOfClass(LivingEntity.class, box,
                                            entity -> entity.isAlive() && !travelling.contains(entity.getUUID())).isEmpty()) {
                                continue;
                            }
                            reserved.add(box);
                            result.put(player.getUUID(), position);
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                return Map.of();
            }
        }
        return Map.copyOf(result);
    }

    private static boolean hazardous(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CACTUS) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.WITHER_ROSE) || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(DungeonGateTravel.DUNGEON)) {
            SAMPLES.remove(player);
            return;
        }
        long now = level.getGameTime();
        Sample previous = SAMPLES.put(player, new Sample(level, now, player.position()));
        if (previous != null && previous.level() == level && now > previous.time() && now - previous.time() <= 2) {
            observeMovement(player, previous.position(), player.position());
            SAMPLES.put(player, new Sample(player.serverLevel(), now, player.position()));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || !level.dimension().equals(DungeonGateTravel.DUNGEON) || level.getGameTime() % 20 != 0) {
            return;
        }
        DungeonBossGateData data = state(level);
        if (data.activeBinding() != null) {
            List<ServerPlayer> pending = level.players().stream().filter(player -> player.isAlive() && !data.arrived(player.getUUID())).toList();
            if (!pending.isEmpty()) {
                gather(level, data, data.activeBinding(), pending);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { forgetArrival(event); }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) { forgetArrival(event); }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) { forgetArrival(event); }

    private static void forgetArrival(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SAMPLES.remove(player);
            if (player.serverLevel().dimension().equals(DungeonGateTravel.DUNGEON)) {
                state(player.serverLevel()).forgetArrival(player.getUUID());
            }
        }
    }
}
