package net.farmingtales.ftdungeontrading;

import java.util.Optional;
import net.farmingtales.ftdungeontrading.api.DungeonTradingApi.TableState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public final class TradeTableAccess {
    private TradeTableAccess() {}

    public static void configure(ServerLevel level, BlockPos position, ResourceLocation offer, int stock) {
        requireThread(level);
        if (TradeOfferRepository.find(offer).isEmpty()) throw new IllegalArgumentException("Unknown trade: " + offer);
        TradeTableBlockEntity table = entity(level, position).orElse(null);
        if (table == null) {
            throw new IllegalArgumentException("No loaded trading table at " + position.toShortString());
        }
        table.configure(offer, stock);
    }

    public static Optional<TableState> snapshot(ServerLevel level, BlockPos position) {
        requireThread(level);
        return entity(level, position).map(table -> new TableState(table.offerId(), table.remainingStock(), table.changeVersion()));
    }

    private static Optional<TradeTableBlockEntity> entity(ServerLevel level, BlockPos position) {
        if (!level.hasChunkAt(position)) return Optional.empty();
        var state = level.getBlockState(position);
        if (!TradeTableBlock.hasPartner(level, position, state)) return Optional.empty();
        BlockPos primary = TradeTableBlock.primaryPosition(state, position);
        return level.getBlockEntity(primary) instanceof TradeTableBlockEntity table ? Optional.of(table) : Optional.empty();
    }

    private static void requireThread(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Table API requires the server thread");
    }
}
