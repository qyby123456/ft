package net.farmingtales.ftdungeontrading;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.farmingtales.ftdungeontrading.api.DungeonTradingApi;
import net.farmingtales.ftdungeontrading.api.TradeContext;
import net.farmingtales.ftdungeontrading.api.TradeCost;
import net.farmingtales.ftdungeontrading.api.TradeEvent;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.farmingtales.ftdungeontrading.api.TradeResult.Status;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(FTDungeonTrading.ID)
@PrefixGameTestTemplate(false)
public final class TradingRuntimeTest {
    private static int assertions;

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void tradingSafety(GameTestHelper helper) {
        assertions = 0;
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(2, 1, 2));
        ServerPlayer player = player(helper, position);
        ServerPlayer competitor = player(helper, position);
        var block = FTDungeonTrading.TRADING_TABLE.get();
        level.setBlock(position, block.defaultBlockState(), 3);
        TradeTableBlockEntity table = (TradeTableBlockEntity) level.getBlockEntity(position);
        check(helper, table.remainingStock() == 0, "New or replaced tables cannot replenish configured stock");
        expect(helper, player, position, Status.MISSING_OFFER);
        ResourceLocation id = new ResourceLocation(FTDungeonTrading.ID, "runtime_test");
        TradeOffer basic = offer(id, 3, new ItemStack(Items.BREAD, 6));
        DungeonTradingApi.registerOffer(basic);
        DungeonTradingApi.bindTable(level, position, id, 2);
        check(helper, DungeonTradingApi.offerIds().contains(id), "Public catalog API");
        check(helper, DungeonTradingApi.offer(id).orElseThrow() == basic, "Immutable offer registry");
        check(helper, DungeonTradingApi.table(level, position).orElseThrow().remainingStock() == 2, "Public table snapshot");
        check(helper, TradeOfferRepository.find(new ResourceLocation(FTDungeonTrading.ID, "rations")).isPresent(), "Real data-pack listener loaded bundled offer");
        basic.rewards().get(0).setCount(999);
        check(helper, basic.rewards().get(0).getCount() == 6, "Reward accessor makes defensive copies");
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 10));
        expect(helper, player, position, Status.PREVIEW);
        check(helper, count(player, Items.EMERALD) == 10 && count(player, Items.BREAD) == 0, "Preview never settles");
        expect(helper, player, position, Status.TOO_SOON);
        check(helper, table.remainingStock() == 2, "Same-tick duplicate click does not settle");
        advance(level, 6);
        expect(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.EMERALD) == 7 && count(player, Items.BREAD) == 6, "Atomic payment and reward");
        check(helper, table.remainingStock() == 1, "Stock decremented once");
        expect(helper, player, position, Status.PREVIEW);
        check(helper, count(player, Items.BREAD) == 6, "Replay requires new preview");
        var hit = new BlockHitResult(position.getCenter(), Direction.UP, position, false);
        InteractionResult offhand = block.defaultBlockState().use(level, player, InteractionHand.OFF_HAND, hit);
        check(helper, offhand == InteractionResult.PASS && count(player, Items.BREAD) == 6, "Offhand cannot double settle");
        TradeService.cancel(player.getUUID());

        DungeonTradingApi.bindTable(level, position, id, 1);
        competitor.getInventory().setItem(0, new ItemStack(Items.EMERALD, 10));
        expect(helper, player, position, Status.PREVIEW);
        expect(helper, competitor, position, Status.PREVIEW);
        advance(level, 6);
        expect(helper, player, position, Status.PURCHASED);
        expect(helper, competitor, position, Status.SOLD_OUT);
        check(helper, count(competitor, Items.EMERALD) == 10 && count(competitor, Items.BREAD) == 0, "Last-stock competitor not charged");

        DungeonTradingApi.bindTable(level, position, id, -1);
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
        purchase(helper, player, position, Status.INSUFFICIENT_PAYMENT);
        check(helper, count(player, Items.EMERALD) == 2, "Insufficient payment is unchanged");
        for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 64));
        purchase(helper, player, position, Status.INVENTORY_FULL);
        check(helper, count(player, Items.EMERALD) == 64, "Full inventory does not charge");
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 3));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 6 && count(player, Items.EMERALD) == 0, "Freed payment slot can hold reward");

        clear(player);
        player.getInventory().setItem(40, new ItemStack(Items.EMERALD, 3));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 6 && player.getOffhandItem().isEmpty(), "Offhand payment allowed, reward goes to main inventory");
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 3));
        player.getInventory().getItem(0).setHoverName(Component.literal("Protected named currency"));
        purchase(helper, player, position, Status.INSUFFICIENT_PAYMENT);
        check(helper, player.getInventory().getItem(0).hasCustomHoverName(), "Default cost cannot silently consume special NBT");

        String exactJson = "{\"costs\":[{\"item\":\"minecraft:emerald\",\"count\":2,\"nbt\":\"{Token:7}\"}],\"rewards\":[{\"item\":\"minecraft:diamond\",\"nbt\":\"{Reward:1}\"}]}";
        DungeonTradingApi.registerOffer(TradeOfferCodec.parse(id, JsonParser.parseString(exactJson).getAsJsonObject()));
        clear(player);
        ItemStack currency = new ItemStack(Items.EMERALD, 2);
        currency.getOrCreateTag().putInt("Token", 7);
        player.getInventory().setItem(0, currency);
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, player.getInventory().getItem(0).is(Items.DIAMOND) && player.getInventory().getItem(0).getTag().getInt("Reward") == 1, "Exact NBT input and configured NBT output");
        rejectJson(helper, id, "{\"costs\":[],\"rewards\":[]}");
        rejectJson(helper, id, "{\"costs\":[{\"item\":\"minecraft:emerald\",\"count\":1.2}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}");
        rejectJson(helper, id, "{\"costs\":[{\"item\":\"missing:coin\"}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}");
        rejectJson(helper, id, "{\"costs\":[{\"item\":\"minecraft:emerald\",\"nbt\":\"{Bad\"}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}");
        rejectJson(helper, id, "{\"costs\":[{\"item\":\"minecraft:emerald\",\"surprise\":true}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}");

        String tagJson = "{\"costs\":[{\"tag\":\"ftdungeontrading:test_currency\",\"count\":2}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}";
        DungeonTradingApi.registerOffer(TradeOfferCodec.parse(id, JsonParser.parseString(tagJson).getAsJsonObject()));
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 1 && count(player, Items.EMERALD) == 0 && count(player, Items.DIAMOND) == 0, "Real data-pack item tag accepts mixed payment");
        String anyNbtJson = "{\"costs\":[{\"item\":\"minecraft:emerald\",\"allow_any_nbt\":true}],\"rewards\":[{\"item\":\"minecraft:bread\"}]}";
        DungeonTradingApi.registerOffer(TradeOfferCodec.parse(id, JsonParser.parseString(anyNbtJson).getAsJsonObject()));
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        player.getInventory().getItem(0).setHoverName(Component.literal("Explicitly accepted"));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 1, "Explicit any-NBT payment policy");

        DungeonTradingApi.registerOffer(offer(id, 1, new ItemStack(Items.BREAD)));
        clear(player);
        player.getInventory().setItem(36, new ItemStack(Items.EMERALD));
        purchase(helper, player, position, Status.INSUFFICIENT_PAYMENT);
        check(helper, player.getInventory().getItem(36).is(Items.EMERALD), "Armor slots are never payment slots");
        clear(player);
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.EMERALD) == 0 && count(player, Items.BREAD) == 1, "Creative purchases are not free");
        player.setGameMode(GameType.SURVIVAL);
        DungeonTradingApi.registerOffer(new TradeOffer(id, Component.literal("Bundle"), List.of(TradeCost.item(new ItemStack(Items.EMERALD), 1)), List.of(new ItemStack(Items.BREAD), new ItemStack(Items.DIAMOND))));
        clear(player);
        for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        purchase(helper, player, position, Status.INVENTORY_FULL);
        check(helper, count(player, Items.EMERALD) == 1 && count(player, Items.BREAD) == 0 && count(player, Items.DIAMOND) == 0, "A partly fitting reward bundle never partly settles");

        TradeCost flexible = new TradeCost(Ingredient.of(Items.EMERALD, Items.DIAMOND), 1, null, false);
        TradeCost exact = TradeCost.item(new ItemStack(Items.EMERALD), 1);
        DungeonTradingApi.registerOffer(new TradeOffer(id, Component.literal("Overlap"), List.of(flexible, exact), List.of(new ItemStack(Items.BREAD))));
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 1 && count(player, Items.EMERALD) == 0 && count(player, Items.DIAMOND) == 0, "Maximum flow resolves overlapping ingredients instead of greedy failure");
        DungeonTradingApi.registerOffer(offer(id, 1, new ItemStack(Items.BREAD, 130)));
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD));
        purchase(helper, player, position, Status.PURCHASED);
        check(helper, count(player, Items.BREAD) == 130, "Oversized rewards split into legal stacks");
        for (int slot = 0; slot < 36; slot++) check(helper, player.getInventory().getItem(slot).getCount() <= player.getInventory().getItem(slot).getMaxStackSize(), "Stack size limit slot " + slot);

        DungeonTradingApi.registerOffer(basic);
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 32));
        expect(helper, player, position, Status.PREVIEW);
        DungeonTradingApi.registerOffer(offer(id, 4, new ItemStack(Items.BREAD, 7)));
        advance(level, 6);
        expect(helper, player, position, Status.QUOTE_CHANGED);
        check(helper, count(player, Items.EMERALD) == 32, "Catalog edit invalidates quoted price");
        expect(helper, player, position, Status.PREVIEW);
        DungeonTradingApi.bindTable(level, position, id, 5);
        advance(level, 6);
        expect(helper, player, position, Status.QUOTE_CHANGED);
        expect(helper, player, position, Status.PREVIEW);
        advance(level, 101);
        expect(helper, player, position, Status.PREVIEW);
        check(helper, count(player, Items.EMERALD) == 32, "Expired quote becomes preview without purchase");
        TradeService.cancel(player.getUUID());
        expect(helper, player, position, Status.PREVIEW);
        CompoundTag saved = table.saveWithoutMetadata();
        var restored = new TradeTableBlockEntity(position, block.defaultBlockState());
        restored.load(saved);
        check(helper, restored.remainingStock() == 5 && restored.offerId().equals(id), "Offer and stock persist through NBT");
        check(helper, !restored.instanceId().equals(table.instanceId()), "Reloaded table invalidates pending instance");
        var clientCopy = new TradeTableBlockEntity(position, block.defaultBlockState());
        clientCopy.load(table.getUpdateTag());
        check(helper, !clientCopy.displayCosts().isEmpty() && !clientCopy.displayRewards().isEmpty(), "Client display synchronized without client catalog");
        clientCopy.displayRewards().get(0).setCount(99);
        check(helper, clientCopy.displayRewards().get(0).getCount() != 99, "Display stack accessor makes defensive copy");

        AtomicBoolean veto = new AtomicBoolean(true);
        AtomicBoolean reenter = new AtomicBoolean(false);
        AtomicInteger afterCount = new AtomicInteger();
        Consumer<TradeEvent.BeforePurchase> beforeListener = event -> {
            if (!event.offer().id().equals(id)) return;
            if (veto.get()) event.setCanceled(true);
            if (reenter.get()) check(helper, DungeonTradingApi.interact(event.context()).status() == Status.UNAVAILABLE, "Reentrant settlement blocked");
        };
        Consumer<TradeEvent.Purchased> afterListener = event -> { if (event.offer().id().equals(id)) afterCount.incrementAndGet(); };
        MinecraftForge.EVENT_BUS.addListener(beforeListener);
        MinecraftForge.EVENT_BUS.addListener(afterListener);
        try {
            TradeService.cancel(player.getUUID());
            purchase(helper, player, position, Status.CANCELED);
            check(helper, count(player, Items.EMERALD) == 32 && afterCount.get() == 0, "Veto is pre-settlement");
            veto.set(false);
            reenter.set(true);
            purchase(helper, player, position, Status.PURCHASED);
            check(helper, afterCount.get() == 1 && count(player, Items.EMERALD) == 28, "Exactly one committed notification");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(beforeListener);
            MinecraftForge.EVENT_BUS.unregister(afterListener);
        }

        player.moveTo(position.getX() + 30, position.getY(), position.getZ());
        expect(helper, player, position, Status.DENIED);
        player.moveTo(position.getX() + 1, position.getY(), position.getZ() - 1);
        player.setGameMode(GameType.SPECTATOR);
        expect(helper, player, position, Status.DENIED);
        player.setGameMode(GameType.SURVIVAL);
        AtomicBoolean wrongThreadRejected = new AtomicBoolean();
        Thread probe = new Thread(() -> {
            try { DungeonTradingApi.interact(new TradeContext(level, position, player)); }
            catch (IllegalStateException expected) { wrongThreadRejected.set(true); }
        });
        probe.start();
        try { probe.join(); } catch (InterruptedException exception) { throw new IllegalStateException(exception); }
        check(helper, wrongThreadRejected.get(), "Server thread required");
        try {
            level.getServer().getCommands().getDispatcher().parse("fttrade offers", player.createCommandSourceStack().withPermission(0)).getReader();
            int ordinary = level.getServer().getCommands().getDispatcher().execute("fttrade offers", player.createCommandSourceStack().withPermission(0));
            check(helper, ordinary == 0, "Ordinary players cannot use admin commands");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
            check(helper, true, "Ordinary player command rejected");
        }
        try {
            check(helper, level.getServer().getCommands().getDispatcher().execute("fttrade inspect " + position.toShortString().replace(",", ""), player.createCommandSourceStack().withPermission(2)) == 1, "Admin inspect command");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { throw new IllegalStateException(exception); }
        check(helper, DungeonTradingApi.removeCodeOffer(id), "Remove code offer API");
        TradeService.cancel(player.getUUID());
        expect(helper, player, position, Status.MISSING_OFFER);
        check(helper, table.onlyOpCanSetNbt(), "Client BlockEntityTag requires operator permission");
        TradeService.clear();
        removePlayer(player);
        removePlayer(competitor);
        FTDungeonTrading.LOGGER.info("TRADING_RUNTIME_TEST passed with {} assertions", assertions);
        helper.succeed();
    }

    private static TradeOffer offer(ResourceLocation id, int price, ItemStack reward) {
        return new TradeOffer(id, Component.literal("Runtime"), List.of(TradeCost.item(new ItemStack(Items.EMERALD), price)), List.of(reward));
    }

    private static void purchase(GameTestHelper helper, ServerPlayer player, BlockPos position, Status expected) {
        expect(helper, player, position, Status.PREVIEW);
        advance(player.serverLevel(), 6);
        expect(helper, player, position, expected);
    }

    private static void expect(GameTestHelper helper, ServerPlayer player, BlockPos position, Status expected) {
        var result = DungeonTradingApi.interact(new TradeContext(player.serverLevel(), position, player));
        check(helper, result.status() == expected, "Expected " + expected + ", got " + result.status());
    }

    private static void advance(ServerLevel level, int ticks) {
        var server = level.getServer();
        server.getWorldData().overworldData().setGameTime(server.overworld().getGameTime() + ticks);
    }
    private static void clear(ServerPlayer player) { player.getInventory().clearContent(); TradeService.cancel(player.getUUID()); }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum()
                + player.getInventory().offhand.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static void rejectJson(GameTestHelper helper, ResourceLocation id, String source) {
        try { TradeOfferCodec.parse(id, JsonParser.parseString(source).getAsJsonObject()); throw new IllegalStateException("Invalid trade accepted"); }
        catch (IllegalArgumentException | com.google.gson.JsonParseException expected) { check(helper, true, "Invalid configuration rejected"); }
    }

    @SuppressWarnings("unchecked")
    private static ServerPlayer player(GameTestHelper helper, BlockPos position) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "FT-trade-probe"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        try {
            var field = PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            ((Map<UUID, ServerPlayer>) field.get(level.getServer().getPlayerList())).put(player.getUUID(), player);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
        player.moveTo(position.getX() + 1, position.getY(), position.getZ() - 1);
        return player;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        assertions++;
        helper.assertTrue(condition, message);
    }

    private static void removePlayer(ServerPlayer player) {
        try {
            var field = PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            ((Map<?, ?>) field.get(player.serverLevel().getServer().getPlayerList())).remove(player.getUUID());
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }
}
