package net.farmingtales.ftdungeonmobs;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class DungeonGateInteraction {
    private DungeonGateInteraction() {}

    public static void refreshNearby(ServerLevel level, BlockPos brazier) {
        int radius = DungeonGateTravel.BRAZIER_RADIUS;
        for (BlockPos position : BlockPos.betweenClosed(brazier.offset(-radius, -radius, -radius),
                brazier.offset(radius, radius, radius))) {
            if (position.distSqr(brazier) > radius * radius || !level.hasChunkAt(position)
                    || !level.isInWorldBounds(position)) {
                continue;
            }
            BlockState state = level.getBlockState(position);
            if (state.getBlock() instanceof DungeonGateBlock && state.getValue(DungeonGateBlock.PART)
                    == DungeonGateBlock.ANCHOR_PART) {
                refreshControl(level, position);
            }
        }
    }

    public static boolean entranceOpen(int tier, boolean roundActive, boolean boundEntry, boolean admissionOpen) {
        return tier > 0 && (!roundActive || boundEntry && admissionOpen);
    }

    public static void refreshControl(ServerLevel level, BlockPos anchor) {
        if (moss.generator.MossBossPortals.refreshControl(level, anchor)) return;
        BlockState state = level.getBlockState(anchor);
        if (!(state.getBlock() instanceof DungeonGateBlock gate)
                || state.getValue(DungeonGateBlock.PART) != DungeonGateBlock.ANCHOR_PART
                || !DungeonGateBlock.complete(level, anchor, gate, state.getValue(DungeonGateBlock.FACING))
                || !(level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity)) {
            return;
        }
        DungeonGateData data = DungeonGateData.get(level.getServer());
        if (gate.returning()) {
            entity.applyControl(cn.ft.biomedungeon.runtime.DungeonExitEncounter.unlocked(level, anchor), Math.max(1, data.tier()));
            return;
        }
        boolean inside = level.dimension().equals(DungeonGateTravel.DUNGEON)
                || net.farmingtales.ftdungeonmobs.generator.DungeonGenerators.all().stream()
                        .anyMatch(provider -> provider.ownsDimension(level));
        int controllerTier = controllerSource(level, anchor).map(ArenaDifficultyApi.ArenaSource::tier).orElse(0);
        boolean ready = !inside && controllerTier > 0 && DungeonGateGenerators.status(level, entity).ready();
        if (entity.generatorId().equals(DungeonGenerators.AMETHYST_BIOME)) {
            ready = ready && entranceOpen(controllerTier, data.roundActive(), data.matches(level.dimension(), anchor),
                    data.admissionOpen(level.getServer().overworld().getGameTime()));
        }
        entity.applyControl(ready, controllerTier);
    }

    public static void useWallTorch(ServerPlayer player, BlockPos anchor, DungeonGateBlock gate) {
        ServerLevel level = player.serverLevel();
        if (!DungeonGateBlock.complete(level, anchor, gate,
                level.getBlockState(anchor).getValue(DungeonGateBlock.FACING))) {
            player.displayClientMessage(Component.translatable("message.ftdungeonmobs.gate.incomplete"), true);
            return;
        }
        if (gate.returning()) {
            refreshControl(level, anchor);
            boolean unlocked = level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity && entity.targetOpen();
            player.displayClientMessage(unlocked ? Component.translatable("message.ftdungeonmobs.gate.exit_open")
                    : Component.literal("请先击败本实例的地牢首领，返程门仍被封印"), true);
            return;
        }
        if (DungeonGateTravel.brazierLocked(level, anchor)) {
            showLocked(player);
            return;
        }
        Optional<BlockPos> source = nearestBrazier(level, anchor);
        if (source.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ftdungeonmobs.gate.missing_brazier"), true);
            return;
        }
        BlockPos position = source.get();
        level.getBlockState(position).use(level, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false));
    }

    public static void showLocked(ServerPlayer player) {
        DungeonGateData data = DungeonGateData.get(player.serverLevel().getServer());
        int seconds = data.admissionSeconds(player.serverLevel().getServer().overworld().getGameTime());
        player.displayClientMessage(Component.translatable(seconds > 0 ? "message.ftdungeonmobs.gate.locked_window"
                : "message.ftdungeonmobs.gate.locked", seconds), true);
    }

    private static Optional<BlockPos> nearestBrazier(ServerLevel level, BlockPos anchor) {
        return controllerSource(level, anchor).map(ArenaDifficultyApi.ArenaSource::position);
    }

    public static Optional<ArenaDifficultyApi.ArenaSource> controllerSource(ServerLevel level, BlockPos anchor) {
        BlockPos nearest = null;
        double closest = Double.POSITIVE_INFINITY;
        int radius = DungeonGateTravel.BRAZIER_RADIUS;
        for (BlockPos position : BlockPos.betweenClosed(anchor.offset(-radius, -radius, -radius),
                anchor.offset(radius, radius, radius))) {
            double distance = position.distSqr(anchor);
            if (distance > radius * radius || !level.hasChunkAt(position) || !level.isInWorldBounds(position)) {
                continue;
            }
            BlockState state = level.getBlockState(position);
            boolean brazier = state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get())
                    || state.is(FTDungeonMobs.ARENA_TORCH.get()) && state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.LOWER;
            if (brazier && (nearest == null || distance < closest
                    || distance == closest && position.asLong() < nearest.asLong())) {
                nearest = position.immutable();
                closest = distance;
            }
        }
        if (nearest == null) {
            return Optional.empty();
        }
        BlockState state = level.getBlockState(nearest);
        int tier = state.is(FTDungeonMobs.BLOODTHORN_BRAZIER.get()) ? state.getValue(BloodthornBrazierBlock.TIER)
                : state.getValue(ArenaTorchBlock.TIER);
        return Optional.of(new ArenaDifficultyApi.ArenaSource(nearest, tier));
    }
}
