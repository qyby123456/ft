package net.farmingtales.ftdungeontrading.api;

import java.util.List;
import java.util.Optional;
import net.farmingtales.ftdungeontrading.TradeOfferRepository;
import net.farmingtales.ftdungeontrading.TradeService;
import net.farmingtales.ftdungeontrading.TradeTableAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public final class DungeonTradingApi {
    private DungeonTradingApi() {}

    public static void registerOffer(TradeOffer offer) { TradeOfferRepository.register(offer); }
    public static boolean removeCodeOffer(ResourceLocation id) { return TradeOfferRepository.removeCodeOffer(id); }
    public static Optional<TradeOffer> offer(ResourceLocation id) { return TradeOfferRepository.find(id); }
    public static List<ResourceLocation> offerIds() { return TradeOfferRepository.ids(); }
    public static TradeResult interact(TradeContext context) { return TradeService.interact(context); }

    public static void bindTable(ServerLevel level, BlockPos position, ResourceLocation offer, int stock) {
        TradeTableAccess.configure(level, position, offer, stock);
    }

    public static Optional<TableState> table(ServerLevel level, BlockPos position) {
        return TradeTableAccess.snapshot(level, position);
    }

    public record TableState(ResourceLocation offerId, int remainingStock, long changeVersion) {}
}
