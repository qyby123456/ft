package cn.farmingtales.sdmshopaudit.smoke;

import cn.farmingtales.sdmshopaudit.AuditMod;
import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.Bridge;
import cn.farmingtales.sdmshopaudit.OfficialCatalog;
import cn.farmingtales.sdmshopaudit.ShopSnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.fml.common.Mod;

@Mod("sdmshop_audit_smoke")
@GameTestHolder("forge")
@PrefixGameTestTemplate(false)
public final class AuditGameTests {
    private static final UUID TAB_A = new UUID(0, 101);
    private static final UUID TAB_B = new UUID(0, 102);
    private static final UUID BUY = new UUID(0, 201);
    private static final UUID SELL = new UUID(0, 202);
    private static final UUID EXCHANGE = new UUID(0, 203);

    @GameTest(template = "empty3x3", timeoutTicks = 400)
    public static void auditRequests(GameTestHelper helper) throws Exception {
        helper.assertTrue(AuditService.directory() != null, "audit service must start on physical server");
        if (Boolean.getBoolean("sdmaudit.testReleaseJar")) {
            String location = AuditMod.class.getProtectionDomain().getCodeSource().getLocation().toString();
            helper.assertTrue(location.contains("sdmshop-server-audit-forge-1.20.1") && location.contains(".jar"),
                    "release-JAR smoke test must load audit classes from the packaged JAR");
            AuditMod.LOGGER.info("AUDIT_RELEASE_JAR_LOADED {}", location);
        }
        OfficialCatalog catalog = OfficialCatalog.load();
        verifyOfficialProducts(helper, catalog);
        JsonObject report = catalog.summary();
        com.google.gson.JsonArray tabNames = new com.google.gson.JsonArray();
        catalog.tabNames().forEach(tabNames::add);
        report.add("tabNames", tabNames);
        com.google.gson.JsonArray itemIds = new com.google.gson.JsonArray();
        catalog.allowedItemIds().forEach(itemIds::add);
        report.add("allowedItemIds", itemIds);
        Files.createDirectories(Path.of("../build/verification"));
        Files.writeString(Path.of("../build/verification/official-catalog-report.json"),
                new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report), StandardCharsets.UTF_8);
        Files.write(Path.of("../build/verification/official-item-ids.txt"), catalog.allowedItemIds(), StandardCharsets.UTF_8);
        UUID actorId = UUID.randomUUID();
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(actorId, "SDMAuditSmoke"));
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(),
                new Connection(PacketFlow.SERVERBOUND), player);
        Class<?> contextType = Class.forName("dev.architectury.networking.NetworkManager$PacketContext");
        Object context = Proxy.newProxyInstance(contextType.getClassLoader(), new Class<?>[]{contextType},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getPlayer" -> player;
                    case "queue" -> { ((Runnable) arguments[0]).run(); yield null; }
                    case "toString" -> "AuditSmokeContext";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> null;
                });
        Object shop = Bridge.mainShop();
        helper.getLevel().getServer().getPlayerList().getOps().add(new ServerOpListEntry(player.getGameProfile(), 4, false));
        CompoundTag base = new CompoundTag();
        base.putUUID("shopUUID", (UUID) Bridge.field(shop, "shopUUID"));
        base.putString("shopName", "Audit Smoke Shop");
        ListTag tabs = new ListTag();
        tabs.add(tab(TAB_A, "商品", true));
        tabs.add(tab(TAB_B, "备用", false));
        base.put("shopTabs", tabs);
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{base}, context);
        send("server.create.SendCreateShopTabC2S", new Class<?>[]{CompoundTag.class},
                new Object[]{tab(new UUID(0, 103), "新增页面", false)}, context);
        send("server.create.SendCreateShopEntryC2S", new Class<?>[]{UUID.class, CompoundTag.class},
                new Object[]{TAB_A, entry(new UUID(0, 204), false, false)}, context);
        send("server.SendChangeShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class},
                new Object[]{TAB_B, tab(TAB_B, "改名一", false)}, context);
        CompoundTag changedTab = tab(TAB_A, "商品", true);
        changedTab.getList("tabEntry", 10).getCompound(0).putLong("entryPrice", 12);
        send("server.SendChangesShopEntriesC2S", new Class<?>[]{UUID.class, CompoundTag.class},
                new Object[]{TAB_A, changedTab}, context);
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class},
                new Object[]{TAB_B, tab(TAB_B, "改名二", false)}, context);
        send("server.edit.SendEditShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, CompoundTag.class},
                new Object[]{TAB_A, BUY, entry(BUY, false, false)}, context);
        send("server.move.SendMoveShopTabC2S", new Class<?>[]{int.class, int.class},
                new Object[]{0, 1}, context);
        send("server.move.SendMoveShopEntryC2S", new Class<?>[]{UUID.class, int.class, int.class},
                new Object[]{TAB_A, 0, 1}, context);

        money(player, 1000);
        helper.getLevel().getServer().getPlayerList().getOps().remove(player.getGameProfile());
        purchase(context, BUY, 3);
        helper.assertTrue(player.getInventory().countItem(Items.STONE) == 3, "successful buy delivers three items");
        helper.assertTrue(balance(player) == 970, "successful buy debits 30");
        purchase(context, SELL, 2);
        helper.assertTrue(player.getInventory().countItem(Items.STONE) == 1, "successful sell removes two items");
        helper.assertTrue(balance(player) == 990, "successful sell credits 20");
        purchase(context, BUY, 1025);
        helper.assertTrue(balance(player) == 990, "invalid quantity is not charged");
        money(player, 0);
        purchase(context, BUY, 1);
        helper.assertTrue(player.getInventory().countItem(Items.STONE) == 1, "insufficient funds do not give items");
        player.getInventory().add(new ItemStack(Items.EMERALD, 2));
        purchase(context, EXCHANGE, 1);
        helper.assertTrue(player.getInventory().countItem(Items.EMERALD) == 0, "item currency consumed");
        helper.assertTrue(player.getInventory().countItem(Items.DIRT) == 1, "item currency purchase delivered");
        purchase(context, EXCHANGE, 1);
        CompoundTag taggedStone = entry(BUY, false, false);
        CompoundTag extraTag = new CompoundTag();
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"text\":\"玩家追加的变种\"}");
        extraTag.put("display", display);
        taggedStone.getCompound("entryType").getCompound("itemStack").put("tag", extraTag);
        helper.getLevel().getServer().getPlayerList().getOps().add(new ServerOpListEntry(player.getGameProfile(), 4, false));
        send("server.edit.SendEditShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, CompoundTag.class},
                new Object[]{TAB_A, BUY, taggedStone}, context);
        money(player, 1000);
        helper.getLevel().getServer().getPlayerList().getOps().remove(player.getGameProfile());
        purchase(context, BUY, 1);

        helper.getLevel().getServer().getPlayerList().getOps().add(new ServerOpListEntry(player.getGameProfile(), 4, false));
        send("server.edit.SendEditShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, CompoundTag.class},
                new Object[]{TAB_A, new UUID(0, 999), entry(BUY, false, false)}, context);
        AuditService.flush();
        String day = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        List<JsonObject> edits = records(day + "-edits.jsonl", actorId);
        List<JsonObject> trades = records(day + "-transactions.jsonl", actorId);
        List<JsonObject> warnings = records(day + "-warnings.jsonl", actorId);
        helper.assertTrue(edits.stream().filter(record -> event(record, "edit_request")).count() == 10, "nine valid edit endpoints plus NBT edit logged");
        helper.assertTrue(edits.stream().filter(record -> event(record, "edit_result")).count() == 10, "each accepted edit has a result");
        helper.assertTrue(warnings.stream().anyMatch(record -> record.toString().contains("UNKNOWN_SHOP_ENTRY")),
                "unknown edit target is rejected before original handler runs");
        helper.assertTrue(trades.stream().filter(record -> event(record, "trade_result")
                && record.get("status").getAsString().equals("SUCCESS")).count() == 4, "money buy/sell, item currency and modified NBT trade are successful");
        helper.assertTrue(trades.stream().filter(record -> event(record, "trade_result")
                && record.get("status").getAsString().equals("BLOCKED_SECURITY")).count() == 3, "all failed attempts logged separately");
        helper.assertTrue(warnings.stream().filter(record -> event(record, "non_original_purchase_warning")).count() == 2,
                "only successful unapproved purchases emit product warnings");
        helper.assertTrue(warnings.stream().filter(record -> event(record, "non_original_purchase_warning")).anyMatch(record -> record.getAsJsonObject("officialProduct")
                .get("reason").getAsString().equals("ITEM_NOT_IN_OFFICIAL_SHOP")), "non-original item ID warning");
        helper.assertTrue(warnings.stream().filter(record -> event(record, "non_original_purchase_warning")).anyMatch(record -> record.getAsJsonObject("officialProduct")
                .get("reason").getAsString().equals("UNAPPROVED_ITEM_NBT")), "modified legal-item NBT warning");
        for (JsonObject trade : trades) {
            if (event(trade, "trade_result")) {
                helper.assertTrue(Files.exists(AuditService.directory().resolve("snapshots")
                        .resolve(trade.get("entryBeforeHash").getAsString() + ".snbt.gz")), "full pre-trade product evidence exists");
                helper.assertTrue(Files.exists(AuditService.directory().resolve("snapshots")
                        .resolve(trade.get("entryAfterHash").getAsString() + ".snbt.gz")), "full post-trade product evidence exists");
            }
        }
        helper.assertTrue(AuditService.status().contains("钩子错误=0"), "no audit integration errors");
        SecurityGameTestChecks.verify(helper);
        helper.assertTrue(AuditService.status().contains("钩子错误=0"), "no security-test audit integration errors");
        AuditMod.LOGGER.info("AUDIT_GAMETEST_PASS edits={} trades={} warnings={} actor={}", edits.size(), trades.size(), warnings.size(), actorId);
        helper.succeed();
    }

    private static void verifyOfficialProducts(GameTestHelper helper, OfficialCatalog catalog) throws Exception {
        String original;
        try (var stream = OfficialCatalog.class.getResourceAsStream("/sdmshop-audit-original.snbt")) {
            original = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        CompoundTag shop = (CompoundTag) Class.forName("dev.ftb.mods.ftblibrary.snbt.SNBT")
                .getMethod("readLines", List.class).invoke(null, original.lines().toList());
        CompoundTag validationData = shop.copy();
        validationData.putUUID("shopUUID", (UUID) Bridge.field(Bridge.mainShop(), "shopUUID"));
        Object validationPacket = Class.forName("net.sixik.sdmshoprework.network.server.SendChangesShopC2S")
                .getConstructor(CompoundTag.class).newInstance(validationData);
        cn.farmingtales.sdmshopaudit.EditValidation.check(validationPacket);
        AuditMod.LOGGER.info("OFFICIAL_CATALOG_EDIT_VALIDATION_PASS");
        JsonArray products = new JsonArray();
        for (Tag tabTag : shop.getList("shopTabs", Tag.TAG_COMPOUND)) {
            CompoundTag tab = (CompoundTag) tabTag;
            for (Tag entryTag : tab.getList("tabEntry", Tag.TAG_COMPOUND)) {
                CompoundTag product = (CompoundTag) entryTag;
                if (product.getBoolean("isSell")) {
                    continue;
                }
                helper.assertTrue(catalog.check(product).get("allowed").getAsBoolean(), "every official purchase product is allowed");
                JsonObject listing = ShopSnapshot.toJson(product).getAsJsonObject();
                listing.addProperty("tabName", tab.getString("title"));
                products.add(listing);
            }
        }
        helper.assertTrue(products.size() == 1050, "fix2 baseline has 1050 purchase entries");
        helper.assertTrue(!catalog.allowedItemIds().contains("minecraft:barrier"), "icons are not approved purchase products");
        CompoundTag normalStone = entry(BUY, false, false);
        normalStone.putLong("entryPrice", 1);
        normalStone.putInt("entryCount", 64);
        normalStone.getCompound("entryType").getCompound("itemStack").putByte("Count", (byte) 64);
        helper.assertTrue(catalog.check(normalStone).get("allowed").getAsBoolean(), "quantity and price do not redefine a product");
        helper.assertTrue(!catalog.check(entry(EXCHANGE, false, true)).get("allowed").getAsBoolean(), "non-original dirt is denied by the catalog");
        CompoundTag byteVariant = normalStone.getCompound("entryType").copy();
        CompoundTag extra = new CompoundTag();
        extra.putByte("sampleValue", (byte) 1);
        byteVariant.getCompound("itemStack").put("tag", extra);
        CompoundTag integerVariant = byteVariant.copy();
        integerVariant.getCompound("itemStack").getCompound("tag").putInt("sampleValue", 1);
        helper.assertTrue(!OfficialCatalog.identity(byteVariant).equals(OfficialCatalog.identity(integerVariant)), "NBT numeric types are preserved");
        Files.createDirectories(Path.of("../build/verification"));
        Files.writeString(Path.of("../build/verification/official-products.json"),
                new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(products), StandardCharsets.UTF_8);
    }

    private static void send(String suffix, Class<?>[] types, Object[] arguments, Object context) throws Exception {
        Class<?> packetType = Class.forName("net.sixik.sdmshoprework.network." + suffix);
        Object packet = packetType.getConstructor(types).newInstance(arguments);
        packetType.getMethod("handle", Class.forName("dev.architectury.networking.NetworkManager$PacketContext"))
                .invoke(packet, context);
    }

    private static void purchase(Object context, UUID entryId, int count) throws Exception {
        send("client.SendBuyShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, int.class},
                new Object[]{TAB_A, entryId, count}, context);
    }

    private static void money(Player player, long amount) throws Exception {
        Class.forName("net.sixik.sdmshoprework.SDMShopR").getMethod("setMoney", Player.class, long.class)
                .invoke(null, player, amount);
    }

    private static long balance(Player player) throws Exception {
        return ((Number) Class.forName("net.sixik.sdmshoprework.SDMShopR").getMethod("getMoney", Player.class)
                .invoke(null, player)).longValue();
    }

    private static CompoundTag tab(UUID id, String title, boolean populated) {
        CompoundTag tab = new CompoundTag();
        tab.putUUID("shopTabUUID", id);
        tab.putString("title", title);
        tab.put("icon", new ItemStack(Items.STONE).save(new CompoundTag()));
        ListTag entries = new ListTag();
        if (populated) {
            entries.add(entry(BUY, false, false));
            entries.add(entry(SELL, true, false));
            entries.add(entry(EXCHANGE, false, true));
        }
        tab.put("tabEntry", entries);
        return tab;
    }

    private static CompoundTag entry(UUID id, boolean sell, boolean itemCurrency) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID("entryUUID", id);
        entry.putLong("entryPrice", itemCurrency ? 2 : 10);
        entry.putInt("entryCount", 1);
        entry.putBoolean("isSell", sell);
        entry.put("icon", new ItemStack(Items.STONE).save(new CompoundTag()));
        CompoundTag type = new CompoundTag();
        type.putString("shopEntryTypeID", "shopItemEntryType");
        type.put("itemStack", new ItemStack(itemCurrency ? Items.DIRT : Items.STONE).save(new CompoundTag()));
        entry.put("entryType", type);
        CompoundTag seller = new CompoundTag();
        seller.putString("shopSellerTypeID", itemCurrency ? "item" : "money");
        seller.putString("moneyID", "base_money");
        if (itemCurrency) {
            seller.put("item", new ItemStack(Items.EMERALD).save(new CompoundTag()));
        }
        entry.put("shopSeller", seller);
        return entry;
    }

    private static List<JsonObject> records(String file, UUID actor) throws Exception {
        return Files.readAllLines(AuditService.directory().resolve(file), StandardCharsets.UTF_8).stream()
                .map(line -> JsonParser.parseString(line).getAsJsonObject())
                .filter(record -> record.has("actor") && record.getAsJsonObject("actor").has("uuid")
                        && !record.getAsJsonObject("actor").get("uuid").isJsonNull()
                        && record.getAsJsonObject("actor").get("uuid").getAsString().equals(actor.toString()))
                .toList();
    }

    private static boolean event(JsonObject record, String event) {
        return record.get("event").getAsString().equals(event);
    }
}
