package net.farmingtales.ftdungeonloot;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(FTDungeonLoot.ID)
@PrefixGameTestTemplate(false)
public final class LootRuntimeTest {
    private static final ResourceLocation TEST = new ResourceLocation(FTDungeonLoot.ID, "test");
    private static int assertions;

    @GameTest(template = "empty", batch = "pricing", timeoutTicks = 400)
    public static void pricingAndPool(GameTestHelper helper) {
        assertions = 0;
        LootProfile profile = LootProfiles.find(TEST).orElseThrow();
        check(helper, profile.price(new ItemStack(Items.BREAD, 3)) == 6, "Whole-stack value determines price");
        check(helper, profile.price(new ItemStack(Items.DIAMOND)) == 13, "Fractional coin prices round upward");
        check(helper, profile.price(new ItemStack(Items.COAL)) == 1, "Minimum prevents free offers");
        check(helper, profile.price(new ItemStack(Items.BONE)) == 0, "Unpriced items excluded");
        check(helper, profile.price(new ItemStack(Items.GOLD_NUGGET)) == 0, "Currency cannot be sold back");
        ItemStack enchanted = new ItemStack(Items.IRON_SWORD);
        enchanted.enchant(Enchantments.UNBREAKING, 2);
        check(helper, profile.price(enchanted) == 29, "Enchantment levels contribute value");
        enchanted.setDamageValue(enchanted.getMaxDamage());
        check(helper, profile.price(enchanted) == 0, "Fully broken item not sold");
        LootProfile ordinary = LootProfiles.find(new ResourceLocation(FTDungeonLoot.ID, "ordinary")).orElseThrow();
        check(helper, ordinary.currency().toString().equals("kubejs:coin"), "Production currency is not silently replaced");
        check(helper, ordinary.currencyItem() == null, "Missing KubeJS currency fails closed in isolated test");
        check(helper, DungeonRounds.current(helper.getLevel(), ordinary) == null, "Missing FT round API does not fall back to a fresh manual run");
        var source = JsonParser.parseString("{\"category\":\"normal_chest\",\"dimension\":\"minecraft:overworld\",\"currency\":\"minecraft:gold_nugget\",\"loot_tables\":[\"ftdungeonloot:chests/test_fixed\"],\"price_percent\":50,\"enchantment_level_value\":15,\"minimum_price\":1,\"maximum_price\":1000000,\"coin_min\":8,\"coin_max\":18,\"item_values\":{\"minecraft:iron_sword\":28}}").getAsJsonObject();
        for (String bad : List.of("category", "boss_table", "entity_table", "empty_tables", "price_percent", "coin_max", "fractional_value")) {
            var candidate = source.deepCopy();
            switch (bad) {
                case "category" -> candidate.addProperty("category", "boss");
                case "boss_table" -> candidate.add("loot_tables", JsonParser.parseString("[\"ftdungeonloot:chests/boss\"]"));
                case "entity_table" -> candidate.add("loot_tables", JsonParser.parseString("[\"minecraft:entities/zombie\"]"));
                case "empty_tables" -> candidate.add("loot_tables", JsonParser.parseString("[]"));
                case "price_percent" -> candidate.addProperty("price_percent", 0);
                case "coin_max" -> candidate.addProperty("coin_max", 2);
                default -> candidate.getAsJsonObject("item_values").addProperty("minecraft:iron_sword", .5);
            }
            boolean rejected = false;
            try { LootProfile.parse(TEST, candidate); } catch (RuntimeException expected) { rejected = true; }
            check(helper, rejected, "Invalid profile rejected: " + bad);
        }
        var first = LootRoller.roll(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), profile, 1314);
        var second = LootRoller.roll(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), profile, 1314);
        check(helper, first.size() == 1 && ItemStack.matches(first.get(0), second.get(0)), "Deterministic roll retains complete stack");
        check(helper, first.get(0).getTag().getString("ft_probe").equals("kept"), "Loot NBT retained");
        var empty = LootProfiles.find(new ResourceLocation(FTDungeonLoot.ID, "test_empty")).orElseThrow();
        check(helper, LootRoller.roll(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), empty, 1).isEmpty(), "Empty pool stays empty without fallback");
        var limitedJson = source.deepCopy();
        limitedJson.addProperty("maximum_price", 5);
        check(helper, LootProfile.parse(TEST, limitedJson).price(new ItemStack(Items.IRON_SWORD)) == 0, "Price cap rejects costly goods rather than making them cheap");
        var productionRoll = LootRoller.roll(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), ordinary, 47);
        check(helper, !productionRoll.isEmpty(), "Production ordinary chest table resolves in Minecraft");
        FTDungeonLoot.LOGGER.info("LOOT_PRICING_PASS assertions={}", assertions);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purchase", timeoutTicks = 400)
    public static void purchaseSafety(GameTestHelper helper) {
        assertions = 0;
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(position, FTDungeonLoot.STAND.get().defaultBlockState(), 3);
        LootStandEntity stand = (LootStandEntity) level.getBlockEntity(position);
        ServerPlayer player = player(level, position);
        ServerPlayer competitor = player(level, position);
        stand.configure(TEST);
        int price = stand.price();
        check(helper, price == 14 && stand.reward().is(Items.IRON_SWORD), "Stand uses configured normal chest table");
        player.getInventory().setItem(0, new ItemStack(Items.GOLD_NUGGET, 64));
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        check(helper, count(player, Items.GOLD_NUGGET) == 64, "Preview never debits");
        expect(helper, player, position, LootTrades.Status.TOO_SOON);
        advance(level, 6);
        expect(helper, player, position, LootTrades.Status.PURCHASED);
        check(helper, count(player, Items.GOLD_NUGGET) == 64 - price && count(player, Items.IRON_SWORD) == 1, "Exact payment and one reward");
        check(helper, player.getInventory().items.stream().filter(stack -> stack.is(Items.IRON_SWORD)).findFirst().orElseThrow()
                .getTag().getString("ft_probe").equals("kept"), "Purchased loot NBT unchanged");
        expect(helper, player, position, LootTrades.Status.SOLD_OUT);
        check(helper, count(player, Items.GOLD_NUGGET) == 64 - price, "Repeated purchase never debits");
        CompoundTag saved = stand.saveWithoutMetadata();
        LootStandEntity restored = new LootStandEntity(position, stand.getBlockState());
        restored.load(saved);
        check(helper, restored.sold() && restored.price() == price && ItemStack.matches(restored.reward(), stand.reward()), "Sold stock and exact offer persist");
        stand.configure(TEST);
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.GOLD_NUGGET, 64));
        competitor.getInventory().setItem(0, new ItemStack(Items.GOLD_NUGGET, 64));
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        expect(helper, competitor, position, LootTrades.Status.PREVIEW);
        advance(level, 6);
        expect(helper, player, position, LootTrades.Status.PURCHASED);
        expect(helper, competitor, position, LootTrades.Status.SOLD_OUT);
        check(helper, count(competitor, Items.GOLD_NUGGET) == 64, "Competing player not charged after global stock spent");
        stand.configure(TEST);
        clear(player);
        attempt(helper, player, position, LootTrades.Status.INSUFFICIENT_PAYMENT);
        check(helper, !stand.sold(), "Insufficient payment preserves stock");
        for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        player.getInventory().setItem(40, new ItemStack(Items.GOLD_NUGGET, 64));
        attempt(helper, player, position, LootTrades.Status.INVENTORY_FULL);
        check(helper, count(player, Items.GOLD_NUGGET) == 64 && !stand.sold(), "Full backpack rolls back offhand payment and stock");
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.GOLD_NUGGET, price));
        for (int slot = 1; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        attempt(helper, player, position, LootTrades.Status.PURCHASED);
        check(helper, count(player, Items.IRON_SWORD) == 1 && count(player, Items.GOLD_NUGGET) == 0, "Payment-freed slot receives reward");
        stand.configure(TEST);
        clear(player);
        ItemStack named = new ItemStack(Items.GOLD_NUGGET, 64);
        named.setHoverName(net.minecraft.network.chat.Component.literal("Do not consume"));
        player.getInventory().setItem(0, named);
        attempt(helper, player, position, LootTrades.Status.INSUFFICIENT_PAYMENT);
        check(helper, named.getCount() == 64, "NBT-bearing currency not accidentally consumed");
        clear(player);
        player.getInventory().setItem(0, new ItemStack(Items.GOLD_NUGGET, 64));
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        LootRunData.get(level).next();
        advance(level, 6);
        expect(helper, player, position, LootTrades.Status.QUOTE_CHANGED);
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        saved = stand.saveWithoutMetadata();
        saved.putInt("Price", 2);
        stand.load(saved);
        advance(level, 6);
        expect(helper, player, position, LootTrades.Status.QUOTE_CHANGED);
        check(helper, count(player, Items.GOLD_NUGGET) == 64, "NBT quote tampering invalidates rather than charging");
        stand.configure(TEST);
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        advance(level, 101);
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        check(helper, count(player, Items.GOLD_NUGGET) == 64, "Expired quote requires a new preview");
        clear(player);
        stand.getBlockState().use(level, player, InteractionHand.OFF_HAND, new BlockHitResult(position.getCenter(), Direction.NORTH, position, false));
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        clear(player);
        ItemStack payment = new ItemStack(Items.GOLD_NUGGET, 64);
        player.getInventory().setItem(0, payment);
        LootTransaction pending = LootTransaction.plan(player.getInventory(), new ItemStack(Items.GOLD_NUGGET), 14, new ItemStack(Items.BREAD));
        payment.shrink(1);
        check(helper, !pending.commit(player.getInventory()) && count(player, Items.GOLD_NUGGET) == 63, "Inventory changes invalidate an uncommitted transaction");
        clear(player);
        player.moveTo(position.getX() + 50, position.getY(), position.getZ());
        expect(helper, player, position, LootTrades.Status.UNAVAILABLE);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var state = stand.getBlockState().setValue(LootStandBlock.FACING, facing);
            check(helper, state.getShape(level, position).bounds().getXsize() == 1 && state.getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.BLOCK,
                    "Solid one-cell collision and piston lock: " + facing);
        }
        LootTrades.clear();
        FTDungeonLoot.LOGGER.info("LOOT_PURCHASE_PASS assertions={}", assertions);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "chest", timeoutTicks = 400)
    public static void chestLifecycle(GameTestHelper helper) {
        assertions = 0;
        var level = helper.getLevel();
        var position = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(position, Blocks.CHEST.defaultBlockState(), 3);
        LootProfile profile = LootProfiles.find(TEST).orElseThrow();
        var chest = (ChestBlockEntity) level.getBlockEntity(position);
        check(helper, DungeonChests.fill(level, position).equals("unbound"), "Unbound and boss chests untouched");
        DungeonChests.bind(level, position, profile);
        var player = player(level, position);
        var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, position,
                new BlockHitResult(position.getCenter(), Direction.NORTH, position, false));
        MinecraftForge.EVENT_BUS.post(event);
        check(helper, chestCount(chest, Items.GOLD_NUGGET) == 12 && chestCount(chest, Items.IRON_SWORD) == 1, "Real interaction listener adds shared loot and configured coins");
        check(helper, DungeonChests.fill(level, position).equals("already"), "Reopen cannot duplicate loot");
        CompoundTag saved = chest.saveWithoutMetadata();
        chest.load(saved);
        chest.clearContent();
        check(helper, DungeonChests.fill(level, position).equals("already") && chest.isEmpty(), "Emptying and loading in same run do not regenerate");
        LootRunData.get(level).next();
        check(helper, DungeonChests.fill(level, position).equals("filled") && chestCount(chest, Items.GOLD_NUGGET) == 12, "New run replenishes once");
        LootRunData.get(level).next();
        check(helper, DungeonChests.fill(level, position).equals("occupied") && chestCount(chest, Items.GOLD_NUGGET) == 12, "New run never destroys leftover contents");
        chest.clearContent();
        var denied = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, position,
                new BlockHitResult(position.getCenter(), Direction.NORTH, position, false));
        denied.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
        DungeonChests.interact(denied);
        check(helper, chest.isEmpty(), "Denied interactions do not generate loot");
        denied.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DEFAULT);
        denied.setCanceled(true);
        DungeonChests.interact(denied);
        check(helper, chest.isEmpty(), "Canceled interactions do not generate loot");
        chest.setLootTable(new ResourceLocation("minecraft:chests/simple_dungeon"), 11);
        check(helper, DungeonChests.fill(level, position).equals("occupied"), "Externally assigned vanilla loot is never overwritten");
        chest.setLootTable(null, 0);
        level.setBlock(position, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT), 18);
        check(helper, DungeonChests.fill(level, position).equals("double_chest"), "Merged chests fail closed");
        var emptyPosition = position.offset(3, 0, 0);
        level.setBlock(emptyPosition, Blocks.CHEST.defaultBlockState(), 3);
        var emptyProfile = LootProfiles.find(new ResourceLocation(FTDungeonLoot.ID, "test_empty")).orElseThrow();
        DungeonChests.bind(level, emptyPosition, emptyProfile);
        check(helper, DungeonChests.fill(level, emptyPosition).equals("empty_pool"), "Empty tables cannot farm coins");
        var overflowPosition = position.offset(6, 0, 0);
        level.setBlock(overflowPosition, Blocks.CHEST.defaultBlockState(), 3);
        DungeonChests.bind(level, overflowPosition, LootProfiles.find(new ResourceLocation(FTDungeonLoot.ID, "test_overflow")).orElseThrow());
        check(helper, DungeonChests.fill(level, overflowPosition).equals("overflow"), "Oversized loot bundle rejected");
        check(helper, ((ChestBlockEntity) level.getBlockEntity(overflowPosition)).isEmpty(), "Overflow leaves chest wholly empty, including coins");
        LootRunData data = LootRunData.get(level);
        check(helper, data.save(new CompoundTag()).getUUID("Run").equals(data.run()), "Run UUID written to saved data");
        FTDungeonLoot.LOGGER.info("LOOT_CHEST_PASS assertions={}", assertions);
        helper.succeed();
    }

    private static ServerPlayer player(ServerLevel level, BlockPos position) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "FT-loot-probe"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(position.getX() + 1, position.getY(), position.getZ() - 1);
        return player;
    }
    private static void advance(ServerLevel level, int ticks) { level.getServer().getWorldData().overworldData().setGameTime(level.getServer().overworld().getGameTime() + ticks); }
    private static void clear(ServerPlayer player) { player.getInventory().clearContent(); LootTrades.cancel(player.getUUID()); }
    private static void expect(GameTestHelper helper, ServerPlayer player, BlockPos position, LootTrades.Status expected) {
        var actual = LootTrades.interact(player, position).status();
        check(helper, actual == expected, "Expected " + expected + ", got " + actual);
    }
    private static void attempt(GameTestHelper helper, ServerPlayer player, BlockPos position, LootTrades.Status expected) {
        expect(helper, player, position, LootTrades.Status.PREVIEW);
        advance(player.serverLevel(), 6);
        expect(helper, player, position, expected);
    }
    private static int count(ServerPlayer player, Item item) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum()
                + player.getInventory().offhand.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
    private static int chestCount(ChestBlockEntity chest, Item item) {
        int result = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) if (chest.getItem(slot).is(item)) result += chest.getItem(slot).getCount();
        return result;
    }
    private static void check(GameTestHelper helper, boolean condition, String message) { assertions++; helper.assertTrue(condition, message); }
}
