package net.farmingtales.ftdungeonloot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class LootTrades {
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Set<UUID> PROCESSING = new HashSet<>();
    public enum Status { PREVIEW, TOO_SOON, PURCHASED, SOLD_OUT, QUOTE_CHANGED, UNAVAILABLE, INSUFFICIENT_PAYMENT, INVENTORY_FULL }
    public record Result(Status status, Component message) {}
    private record Pending(ResourceLocation dimension, BlockPos position, UUID instance, long revision, long catalog, long created,
                           net.minecraft.nbt.CompoundTag signature) {}

    public static Result interact(ServerPlayer player, BlockPos position) {
        if (!player.server.isSameThread()) throw new IllegalStateException("Trades require server thread");
        if (!PROCESSING.add(player.getUUID())) return result(Status.UNAVAILABLE);
        try { return execute(player, position); }
        finally { PROCESSING.remove(player.getUUID()); }
    }

    private static Result execute(ServerPlayer player, BlockPos position) {
        var level = player.serverLevel();
        if (!player.isAlive() || player.isSpectator() || player.distanceToSqr(position.getX() + .5, position.getY() + .5, position.getZ() + .5) > 64
                || !level.hasChunkAt(position) || !level.mayInteract(player, position)
                || !(level.getBlockEntity(position) instanceof LootStandEntity stand) || !stand.ensureOffer()) {
            PENDING.remove(player.getUUID());
            return result(Status.UNAVAILABLE);
        }
        if (stand.sold()) { PENDING.remove(player.getUUID()); return result(Status.SOLD_OUT); }
        long now = player.server.overworld().getGameTime();
        Pending quote = PENDING.get(player.getUUID());
        if (quote == null || now - quote.created() > 100 || !quote.dimension().equals(level.dimension().location())
                || !quote.position().equals(position) || !quote.instance().equals(stand.instance())) {
            PENDING.put(player.getUUID(), new Pending(level.dimension().location(), position.immutable(), stand.instance(),
                    stand.revision(), LootProfiles.revision(), now, stand.saveWithoutMetadata()));
            return new Result(Status.PREVIEW, Component.translatable("message.ftdungeonloot.preview", stand.reward().getDisplayName(),
                    stand.reward().getCount(), stand.price(), ForgeRegistries.ITEMS.getValue(stand.currencyId()).getDescription()));
        }
        if (quote.revision() != stand.revision() || quote.catalog() != LootProfiles.revision() || !quote.signature().equals(stand.saveWithoutMetadata())) {
            PENDING.remove(player.getUUID());
            return result(Status.QUOTE_CHANGED);
        }
        if (now - quote.created() < 6) return result(Status.TOO_SOON);
        PENDING.remove(player.getUUID());
        ItemStack currency = ForgeRegistries.ITEMS.getValue(stand.currencyId()).getDefaultInstance();
        LootTransaction transaction = LootTransaction.plan(player.getInventory(), currency, stand.price(), stand.reward());
        if (transaction.failure() != null) return result(transaction.failure());
        if (!transaction.commit(player.getInventory())) return result(Status.QUOTE_CHANGED);
        stand.purchase();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        return result(Status.PURCHASED);
    }

    private static Result result(Status status) { return new Result(status, Component.translatable("message.ftdungeonloot." + status.name().toLowerCase(java.util.Locale.ROOT))); }
    public static void cancel(UUID player) { PENDING.remove(player); }
    public static void expire(long now) { PENDING.values().removeIf(quote -> now - quote.created() > 100); }
    public static void clear() { PENDING.clear(); PROCESSING.clear(); }
}
