package net.farmingtales.ftdungeonmobs;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class DungeonGateTravel {
    public static final ResourceKey<Level> DUNGEON = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("ftdungeonmobs", "dungeon"));
    public static final int BRAZIER_RADIUS = 6;
    private static final String PLAYER_DATA = "FTDungeonGate";
    private static final int COOLDOWN_TICKS = 40;

    private DungeonGateTravel() {}

    public static void use(ServerPlayer player, BlockPos anchor, DungeonGateBlock gate, boolean returning) {
        ServerLevel source = player.serverLevel();
        BlockState state = source.getBlockState(anchor);
        if (!state.is(gate) || !DungeonGateBlock.complete(source, anchor, gate, state.getValue(DungeonGateBlock.FACING))) {
            message(player, "incomplete");
            return;
        }
        if (gate.boss()) {
            player.displayClientMessage(Component.translatable("message.ftdungeonmobs.boss_gate.hint"), true);
            return;
        }
        DungeonGateData round = DungeonGateData.get(source.getServer());
        if (!returning && round.roundActive()) {
            if (!round.matches(source.dimension(), anchor)) {
                message(player, "occupied");
                return;
            }
            if (!round.admissionOpen(source.getServer().overworld().getGameTime())) {
                message(player, "admission_closed");
                return;
            }
        }
        if (!state.getValue(DungeonGateBlock.OPEN) || state.getValue(DungeonGateBlock.MOVING)) {
            message(player, "closed");
            return;
        }
        if (returning) {
            returnToOrigin(player);
            return;
        }
        if (!canTravel(player)) {
            return;
        }
        if (source.dimension().equals(DUNGEON)) {
            message(player, "entry_outside");
            return;
        }
        MinecraftServer server = source.getServer();
        ServerLevel destination = server.getLevel(DUNGEON);
        if (destination == null) {
            message(player, "missing_dimension");
            return;
        }
        Optional<ArenaDifficultyApi.ArenaSource> brazier = round.roundActive()
                ? Optional.of(new ArenaDifficultyApi.ArenaSource(anchor, round.tier()))
                : DungeonGateInteraction.controllerSource(source, anchor).filter(controller -> controller.tier() > 0);
        if (brazier.isEmpty()) {
            message(player, "missing_brazier");
            return;
        }
        DungeonGateData data = DungeonGateData.get(server);
        if (!data.matches(source.dimension(), anchor) && (data.roundActive() || !destination.players().isEmpty())) {
            message(player, "occupied");
            return;
        }
        if (!DungeonArrival.prepare(destination, data)) {
            message(player, "arrival_blocked");
            return;
        }
        Optional<Vec3> landing = findSafeSpot(destination, player, Vec3.atBottomCenterOf(DungeonArrival.ARRIVAL));
        if (landing.isEmpty()) {
            message(player, "unsafe");
            return;
        }
        CompoundTag previous = playerState(player);
        CompoundTag travelling = previous.copy();
        travelling.putString("Dimension", source.dimension().location().toString());
        travelling.putDouble("X", player.getX());
        travelling.putDouble("Y", player.getY());
        travelling.putDouble("Z", player.getZ());
        travelling.putFloat("Yaw", player.getYRot());
        travelling.putFloat("Pitch", player.getXRot());
        writePlayerState(player, travelling);
        if (!teleport(player, destination, landing.get(), 0, 0)) {
            writePlayerState(player, previous);
            message(player, "cancelled");
            return;
        }
        data.bind(source.dimension(), anchor);
        if (!data.roundActive()) {
            applyTier(server, data, brazier.get().tier());
            data.startRound(player.getUUID(), server.overworld().getGameTime());
        } else {
            data.addParticipant(player.getUUID());
        }
        refreshBoundGates(server, data);
        player.sendSystemMessage(Component.translatable("message.ftdungeonmobs.gate.admission_remaining",
                data.admissionSeconds(server.overworld().getGameTime())));
        saveCooldown(player);
        player.displayClientMessage(Component.translatable("message.ftdungeonmobs.gate.entered", data.tier(),
                Component.translatable("tier.ftdungeonmobs." + data.tier())).withStyle(ChatFormatting.DARK_RED), true);
        DungeonRooms.Result rooms = DungeonRooms.prepare(destination);
        if (!rooms.code().equals("ready")) {
            player.sendSystemMessage(rooms.message());
        }
        for (DungeonRoomGallery.Report report : DungeonRoomGallery.prepareAll(destination, false)) {
            if (!report.code().equals("ready")) {
                player.sendSystemMessage(report.message());
            }
        }
    }

    public static boolean returnToOrigin(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(DUNGEON)) {
            message(player, "return_inside");
            return false;
        }
        if (!canTravel(player)) {
            return false;
        }
        CompoundTag saved = playerState(player);
        MinecraftServer server = player.serverLevel().getServer();
        ResourceLocation dimension = ResourceLocation.tryParse(saved.getString("Dimension"));
        ServerLevel destination = dimension == null ? null
                : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        boolean fallback = destination == null || destination.dimension().equals(DUNGEON) || !validReturnPosition(saved);
        Vec3 origin;
        if (fallback) {
            destination = server.overworld();
            origin = Vec3.atBottomCenterOf(destination.getSharedSpawnPos());
        } else {
            origin = new Vec3(saved.getDouble("X"), saved.getDouble("Y"), saved.getDouble("Z"));
        }
        Optional<Vec3> landing = findSafeSpot(destination, player, origin);
        if (landing.isEmpty() && !fallback) {
            fallback = true;
            destination = server.overworld();
            landing = findSafeSpot(destination, player, Vec3.atBottomCenterOf(destination.getSharedSpawnPos()));
        }
        if (landing.isEmpty()) {
            message(player, "unsafe");
            return false;
        }
        float yaw = Float.isFinite(saved.getFloat("Yaw")) ? saved.getFloat("Yaw") : 0;
        float pitch = Float.isFinite(saved.getFloat("Pitch")) ? saved.getFloat("Pitch") : 0;
        if (!teleport(player, destination, landing.get(), yaw, pitch)) {
            message(player, "cancelled");
            return false;
        }
        CompoundTag cleared = new CompoundTag();
        cleared.putLong("LastTravel", server.overworld().getGameTime());
        writePlayerState(player, cleared);
        message(player, fallback ? "returned_fallback" : "returned");
        return true;
    }

    public static OptionalInt tierForLevel(ServerLevel level) {
        return level.dimension().equals(DUNGEON) ? OptionalInt.of(DungeonGateData.get(level.getServer()).tier())
                : OptionalInt.empty();
    }

    public static int onArenaChanged(ServerLevel level, BlockPos position) {
        DungeonGateInteraction.refreshNearby(level, position);
        DungeonGateData data = DungeonGateData.get(level.getServer());
        if (level.dimension().equals(data.sourceDimension()) && data.sourceGate() != null
                && position.distSqr(data.sourceGate()) <= BRAZIER_RADIUS * BRAZIER_RADIUS) {
            return synchronize(level.getServer(), data);
        }
        return 0;
    }

    static int onArenaTierSelected(ServerLevel level, BlockPos position, int tier) {
        DungeonGateData data = DungeonGateData.get(level.getServer());
        if (level.dimension().equals(DUNGEON) && tier > 0) {
            int changed = applyTier(level.getServer(), data, tier, true);
            DungeonGateInteraction.refreshNearby(level, position);
            return changed;
        }
        if (tier > 0 && level.dimension().equals(data.sourceDimension()) && data.sourceGate() != null
                && position.distSqr(data.sourceGate()) <= BRAZIER_RADIUS * BRAZIER_RADIUS) {
            data.selectDimensionTier(false);
        }
        return onArenaChanged(level, position);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (event.phase == TickEvent.Phase.END && server != null && server.getTickCount() % 20 == 0) {
            DungeonGateData data = DungeonGateData.get(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.serverLevel().dimension().equals(DUNGEON)) {
                    data.addParticipant(player.getUUID());
                } else {
                    data.removeParticipant(player.getUUID());
                }
            }
            data.finishRoundIfEmpty(server.overworld().getGameTime());
            synchronize(server, data);
            refreshBoundGates(server, data);
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag previous = playerState(event.getOriginal());
        if (!previous.isEmpty()) {
            writePlayerState(event.getEntity(), previous.copy());
        }
    }

    public static boolean validReturnPosition(CompoundTag saved) {
        for (String key : new String[]{"X", "Y", "Z"}) {
            if (!saved.contains(key, Tag.TAG_ANY_NUMERIC) || !Double.isFinite(saved.getDouble(key))
                    || Math.abs(saved.getDouble(key)) > 30000000) {
                return false;
            }
        }
        return true;
    }

    private static int synchronize(MinecraftServer server, DungeonGateData data) {
        if (data.roundActive() || data.dimensionTierSelected()) {
            return 0;
        }
        if (data.sourceDimension() == null || data.sourceGate() == null) {
            return 0;
        }
        ServerLevel source = server.getLevel(data.sourceDimension());
        if (source == null || !source.hasChunkAt(data.sourceGate())) {
            return 0;
        }
        BlockState state = source.getBlockState(data.sourceGate());
        if (!state.is(FTDungeonMobs.DUNGEON_ENTRY_GATE.get())
                || state.getValue(DungeonGateBlock.PART) != DungeonGateBlock.ANCHOR_PART
                || !DungeonGateBlock.complete(source, data.sourceGate(), FTDungeonMobs.DUNGEON_ENTRY_GATE.get(),
                        state.getValue(DungeonGateBlock.FACING))) {
            return 0;
        }
        return DungeonGateInteraction.controllerSource(source, data.sourceGate()).filter(controller -> controller.tier() > 0)
                .map(brazier -> applyTier(server, data, brazier.tier())).orElse(0);
    }

    public static boolean brazierLocked(ServerLevel level, BlockPos position) {
        DungeonGateData data = DungeonGateData.get(level.getServer());
        return level.dimension().equals(DUNGEON) && data.roundActive()
                || data.locksBrazier(level.dimension(), position);
    }

    private static void refreshBoundGates(MinecraftServer server, DungeonGateData data) {
        ServerLevel source = data.sourceDimension() == null ? null : server.getLevel(data.sourceDimension());
        if (source != null && data.sourceGate() != null && source.hasChunkAt(data.sourceGate())) {
            DungeonGateInteraction.refreshControl(source, data.sourceGate());
        }
        ServerLevel dungeon = server.getLevel(DUNGEON);
        if (dungeon != null && dungeon.hasChunkAt(DungeonArrival.RETURN_GATE)) {
            DungeonGateInteraction.refreshControl(dungeon, DungeonArrival.RETURN_GATE);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        observePlayer(event.getEntity(), true);
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        observePlayer(event.getEntity(), true);
    }

    public static void onPlayerDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        observePlayer(event.getEntity(), false);
    }

    private static void observePlayer(Player player, boolean adoptLegacy) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        MinecraftServer server = serverPlayer.serverLevel().getServer();
        DungeonGateData data = DungeonGateData.get(server);
        if (serverPlayer.serverLevel().dimension().equals(DUNGEON)) {
            if (!data.roundActive() && adoptLegacy && data.sourceGate() != null && data.sourceDimension() != null) {
                data.startRound(serverPlayer.getUUID(), server.overworld().getGameTime());
            } else {
                data.addParticipant(serverPlayer.getUUID());
            }
        } else {
            data.removeParticipant(serverPlayer.getUUID());
        }
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protectedBrazier(level, event.getPos())) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof ServerPlayer player) {
                DungeonGateInteraction.showLocked(player);
            }
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(position -> protectedBrazier(level, position));
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && brazierLocked(level, event.getPos())
                && (event.getPlacedBlock().is(FTDungeonMobs.BLOODTHORN_BRAZIER.get())
                    || event.getPlacedBlock().is(FTDungeonMobs.ARENA_TORCH.get()))) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                DungeonGateInteraction.showLocked(player);
            }
        }
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level && DungeonGateData.get(level.getServer()).roundActive()) {
            net.minecraft.world.level.block.piston.PistonStructureResolver structure = event.getStructureHelper();
            if (structure != null && structure.resolve()
                    && (structure.getToPush().stream().anyMatch(position -> protectedBrazier(level, position))
                        || structure.getToDestroy().stream().anyMatch(position -> protectedBrazier(level, position)))) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean protectedBrazier(ServerLevel level, BlockPos position) {
        for (BlockPos candidate : new BlockPos[]{position, position.above()}) {
            BlockState state = level.getBlockState(candidate);
            BlockPos base = state.is(FTDungeonMobs.ARENA_TORCH.get())
                    && state.getValue(ArenaTorchBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER
                            ? candidate.below() : candidate;
            if ((state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get()) || state.is(FTDungeonMobs.ARENA_TORCH.get()))
                    && brazierLocked(level, base)) {
                return true;
            }
        }
        return false;
    }

    private static int applyTier(MinecraftServer server, DungeonGateData data, int tier) {
        return applyTier(server, data, tier, false);
    }

    private static int applyTier(MinecraftServer server, DungeonGateData data, int tier, boolean dimensionSelection) {
        data.selectDimensionTier(dimensionSelection);
        if (!data.setTier(tier)) {
            return 0;
        }
        ServerLevel dungeon = server.getLevel(DUNGEON);
        int changed = 0;
        if (dungeon != null) {
            for (Entity entity : dungeon.getAllEntities()) {
                if (entity instanceof DungeonSkeletonEntity skeleton && skeleton.automaticDifficulty()) {
                    DungeonDifficultyApi.refresh(skeleton);
                    changed++;
                }
            }
        }
        return changed;
    }

    private static boolean canTravel(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isVehicle() || player.isSleeping()) {
            message(player, "cannot_travel");
            return false;
        }
        CompoundTag state = playerState(player);
        long now = player.serverLevel().getServer().overworld().getGameTime();
        if (state.contains("LastTravel", Tag.TAG_LONG) && now >= state.getLong("LastTravel")
                && now - state.getLong("LastTravel") < COOLDOWN_TICKS) {
            message(player, "cooldown");
            return false;
        }
        return true;
    }

    private static Optional<Vec3> findSafeSpot(ServerLevel level, ServerPlayer player, Vec3 origin) {
        BlockPos center = BlockPos.containing(origin);
        if (!level.getWorldBorder().isWithinBounds(center) || !level.isInWorldBounds(center)) {
            return Optional.empty();
        }
        for (int radius = 0; radius <= 4; radius++) {
            for (int blockX = -radius; blockX <= radius; blockX++) {
                for (int blockZ = -radius; blockZ <= radius; blockZ++) {
                    if (Math.max(Math.abs(blockX), Math.abs(blockZ)) != radius) {
                        continue;
                    }
                    for (int offset = 0; offset <= 8; offset++) {
                        int blockY = offset % 2 == 0 ? offset / 2 : -(offset + 1) / 2;
                        BlockPos candidate = center.offset(blockX, blockY, blockZ);
                        if (!level.isInWorldBounds(candidate.below()) || !level.isInWorldBounds(candidate.above())
                                || !level.getWorldBorder().isWithinBounds(candidate)) {
                            continue;
                        }
                        level.getChunk(candidate);
                        BlockState floor = level.getBlockState(candidate.below());
                        if (!floor.isFaceSturdy(level, candidate.below(), net.minecraft.core.Direction.UP)
                                || hazardous(floor) || !level.getFluidState(candidate.below()).isEmpty()) {
                            continue;
                        }
                        Vec3 position = Vec3.atBottomCenterOf(candidate);
                        AABB box = new AABB(position.x - 0.3, position.y, position.z - 0.3,
                                position.x + 0.3, position.y + 1.8, position.z + 0.3);
                        if (level.noCollision(player, box) && level.getBlockStates(box)
                                .noneMatch(state -> hazardous(state) || !state.getFluidState().isEmpty())) {
                            return Optional.of(position);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean hazardous(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS) || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.WITHER_ROSE) || state.is(Blocks.SWEET_BERRY_BUSH);
    }

    private static boolean teleport(ServerPlayer player, ServerLevel destination, Vec3 position, float yaw, float pitch) {
        player.teleportTo(destination, position.x, position.y, position.z, yaw, pitch);
        if (player.serverLevel() != destination) {
            return false;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        destination.playSound(null, BlockPos.containing(position), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5F, 0.8F);
        return true;
    }

    private static CompoundTag playerState(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_DATA).copy();
    }

    private static void writePlayerState(Player player, CompoundTag state) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persistent.put(PLAYER_DATA, state);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
    }

    private static void saveCooldown(ServerPlayer player) {
        CompoundTag state = playerState(player);
        state.putLong("LastTravel", player.serverLevel().getServer().overworld().getGameTime());
        writePlayerState(player, state);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.ftdungeonmobs.gate." + key), true);
    }
}
