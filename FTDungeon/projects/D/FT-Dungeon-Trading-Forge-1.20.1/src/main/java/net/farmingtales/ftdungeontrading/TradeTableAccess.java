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
        if (!level.hasChunkAt(position) || !(level.getBlockEntity(position) instanceof TradeTableBlockEntity table)) {
            throw new IllegalArgumentException("No loaded trading table at " + position.toShortString());
        }
        table.configure(offer, stock);
    }

    public static Optional<TableState> snapshot(ServerLevel level, BlockPos position) {
        requireThread(level);
        if (!level.hasChunkAt(position) || !(level.getBlockEntity(position) instanceof TradeTableBlockEntity table)) return Optional.empty();
        return Optional.of(new TableState(table.offerId(), table.remainingStock(), table.changeVersion()));
    }

    private static void requireThread(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Table API requires the server thread");
    }
}
