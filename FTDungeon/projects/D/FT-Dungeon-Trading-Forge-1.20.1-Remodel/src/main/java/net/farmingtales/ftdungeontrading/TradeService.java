package net.farmingtales.ftdungeontrading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftdungeontrading.api.TradeContext;
import net.farmingtales.ftdungeontrading.api.TradeEvent;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.farmingtales.ftdungeontrading.api.TradeResult;
import net.farmingtales.ftdungeontrading.api.TradeResult.Status;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

public final class TradeService {
    public static final int CONFIRMATION_TICKS = 100;
    public static final int MINIMUM_CONFIRMATION_TICKS = 6;
    private static final Map<UUID, PendingQuote> PENDING = new HashMap<>();
    private static final Set<UUID> PROCESSING = new HashSet<>();

    private TradeService() {}

    public static TradeResult interact(TradeContext context) {
        requireServerThread(context);
        UUID playerId = context.player().getUUID();
        if (!PROCESSING.add(playerId)) return result(Status.UNAVAILABLE, "unavailable");
        try { return interactLocked(context); }
        finally { PROCESSING.remove(playerId); }
    }

    private static TradeResult interactLocked(TradeContext context) {
        var level = context.level();
        var player = context.player();
        if (player.level() != level || !player.isAlive() || player.isSpectator()
                || player.distanceToSqr(context.position().getCenter()) > 36
                || !level.hasChunkAt(context.position()) || !level.mayInteract(player, context.position())) {
            PENDING.remove(player.getUUID());
            return result(Status.DENIED, "denied");
        }
        var clickedState = level.getBlockState(context.position());
        if (!TradeTableBlock.hasPartner(level, context.position(), clickedState)) {
            PENDING.remove(player.getUUID());
            return result(Status.UNAVAILABLE, "unavailable");
        }
        BlockPos primary = TradeTableBlock.primaryPosition(clickedState, context.position());
        if (!TradeTableBlock.mayTrade(level, primary, player)) {
            PENDING.remove(player.getUUID());
            return result(Status.DENIED, "denied");
        }
        context = new TradeContext(level, primary, player);
        if (!(level.getBlockEntity(primary) instanceof TradeTableBlockEntity table)) {
            PENDING.remove(player.getUUID());
            return result(Status.UNAVAILABLE, "unavailable");
        }
        table.refreshDisplay();
        TradeOffer offer = TradeOfferRepository.find(table.offerId()).orElse(null);
        if (offer == null || table.remainingStock() == 0) {
            PENDING.remove(player.getUUID());
            return offer == null ? result(Status.MISSING_OFFER, "missing_offer") : result(Status.SOLD_OUT, "sold_out");
        }
        long now = level.getServer().overworld().getGameTime();
        PendingQuote pending = PENDING.get(player.getUUID());
        if (pending == null || pending.createdAt() + CONFIRMATION_TICKS < now || !pending.sameTable(context, table)) {
            PENDING.put(player.getUUID(), new PendingQuote(level.dimension(), context.position(), table.instanceId(),
                    table.changeVersion(), TradeOfferRepository.revision(), offer.signature(), now));
            return new TradeResult(Status.PREVIEW, Component.translatable("message.ftdungeontrading.preview", offer.title(),
                    offer.costDescription(), offer.rewardDescription(), stockDescription(table.remainingStock())));
        }
        if (!pending.matches(table, offer)) {
            PENDING.remove(player.getUUID());
            return result(Status.QUOTE_CHANGED, "quote_changed");
        }
        if (now - pending.createdAt() < MINIMUM_CONFIRMATION_TICKS) return result(Status.TOO_SOON, "too_soon");
        PENDING.remove(player.getUUID());
        boolean canceled;
        try { canceled = MinecraftForge.EVENT_BUS.post(new TradeEvent.BeforePurchase(context, offer)); }
        catch (RuntimeException exception) {
            FTDungeonTrading.LOGGER.error("Trade veto listener failed for {}", offer.id(), exception);
            return result(Status.CANCELED, "canceled");
        }
        if (canceled) return result(Status.CANCELED, "canceled");
        if (level.getBlockEntity(context.position()) != table || !pending.matches(table, offer)
                || TradeOfferRepository.find(table.offerId()).orElse(null) != offer || !player.isAlive()
                || player.level() != level || player.isSpectator() || !TradeTableBlock.mayTrade(level, context.position(), player)) {
            return result(Status.QUOTE_CHANGED, "quote_changed");
        }
        InventoryTransaction transaction = InventoryTransaction.plan(player.getInventory(), offer);
        if (transaction.failure() != null) {
            return result(transaction.failure(), transaction.failure() == Status.INVENTORY_FULL ? "inventory_full" : "insufficient_payment");
        }
        if (!transaction.commit(player.getInventory())) return result(Status.QUOTE_CHANGED, "quote_changed");
        table.consumeStock();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        try { MinecraftForge.EVENT_BUS.post(new TradeEvent.Purchased(context, offer, table.remainingStock())); }
        catch (RuntimeException exception) {
            FTDungeonTrading.LOGGER.error("Post-purchase listener failed after committed trade {} for {}", offer.id(), player.getUUID(), exception);
        }
        return new TradeResult(Status.PURCHASED, Component.translatable("message.ftdungeontrading.purchased", offer.rewardDescription()));
    }

    public static void cancel(UUID playerId) { PENDING.remove(playerId); }

    public static void expire(long serverTime) {
        PENDING.values().removeIf(quote -> quote.createdAt() + CONFIRMATION_TICKS < serverTime);
    }

    public static void clear() {
        PENDING.clear();
        PROCESSING.clear();
    }

    private static Component stockDescription(int stock) {
        return stock < 0 ? Component.translatable("message.ftdungeontrading.unlimited") : Component.literal(Integer.toString(stock));
    }

    private static TradeResult result(Status status, String key) {
        return new TradeResult(status, Component.translatable("message.ftdungeontrading." + key));
    }

    static void requireServerThread(TradeContext context) {
        if (!context.level().getServer().isSameThread()) throw new IllegalStateException("Trade API requires the server thread");
    }

    private record PendingQuote(ResourceKey<Level> dimension, BlockPos position, UUID instanceId,
                                long tableVersion, long catalogVersion, CompoundTag signature, long createdAt) {
        boolean sameTable(TradeContext context, TradeTableBlockEntity table) {
            return dimension.equals(context.level().dimension()) && position.equals(context.position()) && instanceId.equals(table.instanceId());
        }

        boolean matches(TradeTableBlockEntity table, TradeOffer offer) {
            return tableVersion == table.changeVersion() && catalogVersion == TradeOfferRepository.revision()
                    && signature.equals(offer.signature()) && table.remainingStock() != 0;
        }
    }
}
