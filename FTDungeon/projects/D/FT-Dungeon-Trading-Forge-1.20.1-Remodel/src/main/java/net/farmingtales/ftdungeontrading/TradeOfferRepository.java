package net.farmingtales.ftdungeontrading;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class TradeOfferRepository extends SimpleJsonResourceReloadListener {
    public static final TradeOfferRepository INSTANCE = new TradeOfferRepository();
    private static final Map<ResourceLocation, TradeOffer> CODE_OFFERS = new ConcurrentHashMap<>();
    private static final AtomicLong REVISION = new AtomicLong();
    private static volatile Map<ResourceLocation, TradeOffer> dataOffers = Map.of();

    private TradeOfferRepository() { super(new Gson(), "ft_trades"); }

    public static Optional<TradeOffer> find(ResourceLocation id) {
        TradeOffer offer = dataOffers.get(id);
        return Optional.ofNullable(offer == null ? CODE_OFFERS.get(id) : offer);
    }

    public static List<ResourceLocation> ids() {
        var ids = new java.util.TreeSet<ResourceLocation>(CODE_OFFERS.keySet());
        ids.addAll(dataOffers.keySet());
        return List.copyOf(ids);
    }

    public static long revision() { return REVISION.get(); }

    public static void register(TradeOffer offer) {
        CODE_OFFERS.put(offer.id(), offer);
        REVISION.incrementAndGet();
    }

    public static boolean removeCodeOffer(ResourceLocation id) {
        boolean removed = CODE_OFFERS.remove(id) != null;
        if (removed) REVISION.incrementAndGet();
        return removed;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, TradeOffer> loaded = new HashMap<>();
        for (var entry : resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (loaded.size() >= 1024) {
                FTDungeonTrading.LOGGER.error("Trade catalog exceeds 1024 valid entries; remaining entries are ignored");
                break;
            }
            try { loaded.put(entry.getKey(), TradeOfferCodec.parse(entry.getKey(), entry.getValue().getAsJsonObject())); }
            catch (RuntimeException exception) {
                FTDungeonTrading.LOGGER.error("Rejected trade {}: {}", entry.getKey(), exception.getMessage());
            }
        }
        dataOffers = Map.copyOf(loaded);
        REVISION.incrementAndGet();
        FTDungeonTrading.LOGGER.info("Loaded {} data-pack trades; revision {}", loaded.size(), revision());
    }
}
