package cn.farmingtales.sdmshopaudit.smoke;

import cn.farmingtales.sdmshopaudit.AuditMod;
import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.Bridge;
import cn.farmingtales.sdmshopaudit.SecurityRules;
import cn.farmingtales.sdmshopaudit.SecurityService;
import cn.farmingtales.sdmshopaudit.ServerConditions;
import cn.farmingtales.sdmshopaudit.ShopSnapshot;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;

final class SecurityGameTestChecks {
    private static final String ROOT = "net.sixik.sdmshoprework.";
    private static final UUID TAB = new UUID(0, 101);
    private static final UUID BUY = new UUID(0, 201);
    private static final UUID SELL = new UUID(0, 202);
    private static final UUID COMMAND = new UUID(0, 301);
    private static final UUID LIMITED = UUID.randomUUID();
    private static final UUID GLOBAL = UUID.randomUUID();

    private SecurityGameTestChecks() {
    }

    static void verify(GameTestHelper helper) throws Exception {
        ServerPlayer actor = player(helper, "SDMSecuritySmoke", 0);
        ServerPlayer admin = player(helper, "SDMSecurityAdmin", 4);
        Object context = context(actor);
        Object adminContext = context(admin);
        verifyEditValidation(helper, adminContext);
        Object shop = Bridge.mainShop();
        String before = ShopSnapshot.capture(shop).hash();
        CompoundTag forgedMode = (CompoundTag) Class.forName("net.sixik.sdm_economy.api.ICustomData")
                .getMethod("sdm$getCustomData").invoke(actor);
        forgedMode.putBoolean("edit_mode", true);
        helper.assertTrue(!actor.hasPermissions(2) && Bridge.editMode(actor), "forged edit flag is not server OP");
        CompoundTag payload = Bridge.serialize(shop);
        Object page = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, TAB);
        CompoundTag pageData = Bridge.serialize(page);
        CompoundTag entry = product(BUY, false);
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{payload}, context);
        send("server.create.SendCreateShopTabC2S", new Class<?>[]{CompoundTag.class}, new Object[]{pageData}, context);
        send("server.create.SendCreateShopEntryC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, entry}, context);
        send("server.SendChangeShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, pageData}, context);
        send("server.SendChangesShopEntriesC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, pageData}, context);
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, pageData}, context);
        send("server.edit.SendEditShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, CompoundTag.class}, new Object[]{TAB, BUY, entry}, context);
        send("server.move.SendMoveShopTabC2S", new Class<?>[]{int.class, int.class}, new Object[]{0, 1}, context);
        send("server.move.SendMoveShopEntryC2S", new Class<?>[]{UUID.class, int.class, int.class}, new Object[]{TAB, 0, 1}, context);
        helper.assertTrue(before.equals(ShopSnapshot.capture(shop).hash()), "all nine forged edits leave store unchanged");
        AuditService.flush();
        String date = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        long denied = Files.readAllLines(AuditService.directory().resolve(date + "-warnings.jsonl"), StandardCharsets.UTF_8).stream()
                .map(line -> com.google.gson.JsonParser.parseString(line).getAsJsonObject())
                .filter(record -> record.get("event").getAsString().equals("security_block")
                        && record.getAsJsonObject("actor").get("uuid").getAsString().equals(actor.getUUID().toString())
                        && record.get("reason").getAsString().equals("EDIT_REQUIRES_SERVER_OP_LEVEL_2")).count();
        helper.assertTrue(denied == 9, "each blocked edit is attributed and logged once");

        List<String> names = new ArrayList<>();
        helper.getLevel().getServer().getCommands().getDispatcher().register(Commands.literal("audit-security-probe")
                .requires(source -> source.hasPermission(2)).then(Commands.argument("name", StringArgumentType.word()).executes(command -> {
                    if (command.getSource().hasPermission(3)) {
                        throw new IllegalStateException("Elevated source must be capped at level 2");
                    }
                    names.add(StringArgumentType.getString(command, "name"));
                    return 1;
                })));
        CompoundTag command = product(COMMAND, false);
        CompoundTag type = new CompoundTag();
        type.putString("shopEntryTypeID", "commandType");
        type.putString("command", "/audit-security-probe {player}");
        type.putBoolean("elevatePerms", true);
        type.putBoolean("silent", true);
        type.put("iconPathNew", new ItemStack(Items.STONE).save(new CompoundTag()));
        command.put("entryType", type);
        create(adminContext, command);
        money(actor, 1000);
        buy(context, COMMAND, 1);
        helper.assertTrue(names.isEmpty() && balance(actor) == 1000, "stored elevated command is blocked before approval");
        Object liveCommand = lookup(COMMAND);
        Bridge.invoke(Class.forName(ROOT + "api.shop.AbstractShopEntryType"), "buy", void.class, Bridge.entryType(liveCommand),
                new Class<?>[]{Player.class, int.class, Class.forName(ROOT + "api.shop.AbstractShopEntry")}, actor, 1, liveCommand);
        helper.assertTrue(names.isEmpty() && balance(actor) == 1000, "direct type.buy cannot bypass command whitelist");
        ServerPlayer operator2 = player(helper, "SDMLevelTwo", 2);
        String approval = "sdmaudit trust-command " + TAB + " " + COMMAND;
        helper.assertTrue(helper.getLevel().getServer().getCommands().performPrefixedCommand(operator2.createCommandSourceStack(), approval) == 0,
                "level-2 operator cannot authorize elevated custom commands");
        helper.assertTrue(helper.getLevel().getServer().getCommands().performPrefixedCommand(admin.createCommandSourceStack(), approval) == 1,
                "level-4 operator can explicitly approve exact command payload");
        String fingerprint = SecurityRules.commandFingerprint(type.getString("command"), true, true);
        buy(context, COMMAND, 1);
        ServerPlayer second = player(helper, "SDMSecondSmoke", 0);
        money(second, 1000);
        buy(context(second), COMMAND, 1);
        helper.assertTrue(names.equals(List.of(actor.getGameProfile().getName(), second.getGameProfile().getName())),
                "trusted command uses current buyer, not the first buyer");
        helper.assertTrue(Bridge.field(Bridge.entryType(lookup(COMMAND)), "command").equals("/audit-security-probe {player}"),
                "placeholder substitution never mutates persistent template");
        helper.assertTrue(balance(actor) == 990 && balance(second) == 990, "approved commands are charged normally");
        ServerPlayer unsafeName = player(helper, "@a", 0);
        money(unsafeName, 1000);
        buy(context(unsafeName), COMMAND, 1);
        helper.assertTrue(names.size() == 2 && balance(unsafeName) == 1000,
                "player-name substitution cannot inject command selectors");
        CompoundTag altered = command.copy();
        altered.getCompound("entryType").putBoolean("silent", false);
        edit(adminContext, COMMAND, altered);
        buy(context, COMMAND, 1);
        helper.assertTrue(names.size() == 2 && balance(actor) == 990, "changed approval-bound flags are rejected");
        edit(adminContext, COMMAND, command);
        helper.assertTrue(helper.getLevel().getServer().getCommands().performPrefixedCommand(admin.createCommandSourceStack(),
                "sdmaudit revoke-command " + fingerprint) == 1, "approval revocation command succeeds");
        buy(context, COMMAND, 1);
        helper.assertTrue(names.size() == 2 && balance(actor) == 990, "revoked command cannot run");

        money(actor, 1000);
        actor.getInventory().add(new ItemStack(Items.STONE, 4));
        int stones = actor.getInventory().countItem(Items.STONE);
        for (int count : new int[]{-1, 0, 1025, Integer.MIN_VALUE}) {
            buy(context, SELL, count);
        }
        helper.assertTrue(stones == actor.getInventory().countItem(Items.STONE) && balance(actor) == 1000,
                "negative/zero/excessive SELL quantities cannot change items or money");
        CompoundTag overflow = product(BUY, false);
        overflow.putLong("entryPrice", Long.MAX_VALUE);
        edit(adminContext, BUY, overflow);
        buy(context, BUY, 2);
        helper.assertTrue(stones == actor.getInventory().countItem(Items.STONE) && balance(actor) == 1000, "overflowing price is blocked before delivery");
        helper.assertTrue(!Bridge.pay(lookup(BUY), actor, -10) && balance(actor) == 1000, "negative direct payment cannot create funds");
        edit(adminContext, BUY, product(BUY, false));

        CompoundTag limited = product(LIMITED, false);
        limited.putInt("limit", 2);
        create(adminContext, limited);
        buy(context, LIMITED, 3);
        helper.assertTrue(used(actor, LIMITED, false) == 0, "batch larger than remaining limit is rejected without accounting");
        buy(context, LIMITED, 2);
        buy(context, LIMITED, 1);
        helper.assertTrue(used(actor, LIMITED, false) == 2 && balance(actor) == 980, "exact-limit purchase succeeds and next purchase is rejected");
        CompoundTag global = product(GLOBAL, false);
        global.putInt("limit", 2);
        global.putBoolean("globalLimit", true);
        create(adminContext, global);
        buy(context, GLOBAL, 1);
        buy(context(second), GLOBAL, 2);
        helper.assertTrue(used(actor, GLOBAL, true) == 1 && balance(second) == 990, "global limits are shared across players and reject overshoot");
        buy(context(second), GLOBAL, 1);
        helper.assertTrue(used(actor, GLOBAL, true) == 2 && balance(second) == 980, "global remaining one batch succeeds");
        verifyLimitStorage(helper, actor);

        CompoundTag failedCommand = command.copy();
        failedCommand.getCompound("entryType").putString("command", "/audit-security-missing-command {player}");
        failedCommand.putInt("limit", 1);
        edit(adminContext, COMMAND, failedCommand);
        String failureApproval = SecurityService.approve(admin, TAB, COMMAND);
        CompoundTag currentPage = Bridge.serialize(page);
        currentPage.putInt("limit", 5);
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, currentPage}, adminContext);
        long prior = balance(actor);
        buy(context, COMMAND, 1);
        helper.assertTrue(balance(actor) == prior && used(actor, COMMAND, false) == 0 && tabUsed(actor) == 0,
                "failed command is not charged and both tab/entry counters roll back");
        helper.assertTrue(SecurityService.status().contains("已启用"), "rollback and limit synchronization do not disable security");
        SecurityService.revoke(admin, failureApproval);

        CompoundTag unknown = new CompoundTag();
        unknown.putString("entryConditionID", "unrecognized-test-condition");
        boolean conditionBlocked = false;
        try {
            ServerConditions.checkCondition(actor, unknown);
        } catch (IllegalArgumentException exception) {
            conditionBlocked = true;
        }
        helper.assertTrue(conditionBlocked, "unknown server conditions fail closed");
        SmokeQuestCondition questCondition = new SmokeQuestCondition();
        CompoundTag questData = new CompoundTag();
        questData.putString("entryConditionID", "ftbquestConditions");
        ListTag questIds = new ListTag();
        questIds.add(net.minecraft.nbt.StringTag.valueOf("000000000000abcd"));
        questData.put("questID", questIds);
        questCondition.deserializeNBT(questData);
        @SuppressWarnings("unchecked")
        List<Object> conditions = (List<Object>) Bridge.field(lookup(BUY), "entryConditions");
        conditions.add(questCondition);
        int itemsBeforeCondition = actor.getInventory().countItem(Items.STONE);
        buy(context, BUY, 1);
        helper.assertTrue(itemsBeforeCondition == actor.getInventory().countItem(Items.STONE) && balance(actor) == prior,
                "actual purchase cannot bypass unavailable/nonexistent quest condition");
        conditions.clear();
        @SuppressWarnings("unchecked")
        List<Object> tabConditions = (List<Object>) Bridge.field(page, "tabConditions");
        tabConditions.add(questCondition);
        buy(context, BUY, 1);
        helper.assertTrue(itemsBeforeCondition == actor.getInventory().countItem(Items.STONE) && balance(actor) == prior,
                "actual purchase cannot bypass locked tab condition");
        tabConditions.clear();
        var trustedFile = FMLPaths.CONFIGDIR.get().resolve("sdmshop-audit-trusted-commands.json");
        byte[] trustedBefore = Files.readAllBytes(trustedFile);
        try {
            Files.writeString(trustedFile, "{\"schemaVersion\":99,\"commands\":[]}", StandardCharsets.UTF_8);
            SecurityService.start(helper.getLevel().getServer());
            String storeBefore = ShopSnapshot.capture(shop).hash();
            send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{payload}, adminContext);
            buy(context, BUY, 1);
            helper.assertTrue(storeBefore.equals(ShopSnapshot.capture(shop).hash()) && balance(actor) == prior,
                    "security initialization failure blocks both edits and trades, even for OP");
        } finally {
            Files.write(trustedFile, trustedBefore);
            SecurityService.start(helper.getLevel().getServer());
        }
        AuditService.flush();
        helper.assertTrue(AuditService.status().contains("钩子错误=0"), "all security operations finish without audit hook errors");
        String storeBeforeNoAudit = ShopSnapshot.capture(shop).hash();
        AuditService.stop();
        try {
            send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{payload}, context);
            buy(context, COMMAND, 1);
            helper.assertTrue(storeBeforeNoAudit.equals(ShopSnapshot.capture(shop).hash()) && balance(actor) == prior,
                    "security remains enforced while the independent audit service is unavailable");
        } finally {
            AuditService.start(helper.getLevel().getServer());
        }
        AuditMod.LOGGER.info("SECURITY_GAMETEST_PASS forged_edits=9 edit_validation=PASS command_whitelist=PASS direct_bypass=BLOCKED quantity_overflow=BLOCKED limits=PASS rollback=PASS conditions=BLOCKED fail_closed=PASS audit_independence=PASS actor={}", actor.getUUID());
    }

    private static void verifyEditValidation(GameTestHelper helper, Object context) throws Exception {
        Object shop = Bridge.mainShop();
        Object page = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, TAB);
        CompoundTag originalPage = Bridge.serialize(page);
        java.nio.file.Path shopFile = (java.nio.file.Path) Class.forName(ROOT + "SDMShopPaths").getMethod("getFile").invoke(null);
        byte[] diskBefore = Files.readAllBytes(shopFile);
        String before = ShopSnapshot.capture(shop).hash();
        for (String field : List.of("entryPrice", "entryCount", "limit")) {
            CompoundTag invalid = product(BUY, false);
            if (field.equals("entryPrice")) {
                invalid.putLong(field, -1);
            } else {
                invalid.putInt(field, field.equals("entryCount") ? 0 : -1);
            }
            edit(context, BUY, invalid);
        }
        CompoundTag wrongType = product(BUY, false);
        wrongType.putString("entryPrice", "free");
        edit(context, BUY, wrongType);
        edit(context, BUY, product(UUID.randomUUID(), false));
        CompoundTag missingId = product(BUY, false);
        missingId.remove("entryUUID");
        edit(context, BUY, missingId);
        create(context, product(BUY, false));
        send("server.create.SendCreateShopEntryC2S", new Class<?>[]{UUID.class, CompoundTag.class},
                new Object[]{UUID.randomUUID(), product(UUID.randomUUID(), false)}, context);
        CompoundTag invalidPage = Bridge.serialize(page);
        invalidPage.getList("tabEntry", 10).getCompound(0).putLong("entryPrice", -1);
        send("server.SendChangeShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, invalidPage}, context);
        send("server.SendChangesShopEntriesC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, invalidPage}, context);
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, invalidPage}, context);
        CompoundTag newPage = invalidPage.copy();
        newPage.putUUID("shopTabUUID", UUID.randomUUID());
        send("server.create.SendCreateShopTabC2S", new Class<?>[]{CompoundTag.class}, new Object[]{newPage}, context);
        CompoundTag wholeShop = Bridge.serialize(shop);
        wholeShop.getList("shopTabs", 10).getCompound(0).putInt("limit", -1);
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{wholeShop}, context);
        CompoundTag duplicate = Bridge.serialize(page);
        ListTag products = duplicate.getList("tabEntry", 10);
        products.add(products.getCompound(0).copy());
        send("server.SendChangesShopEntriesC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, duplicate}, context);
        CompoundTag duplicateTabs = Bridge.serialize(shop);
        ListTag pages = duplicateTabs.getList("shopTabs", 10);
        pages.add(pages.getCompound(0).copy());
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{duplicateTabs}, context);
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{new CompoundTag()}, context);
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{null}, context);
        CompoundTag legacy = Bridge.serialize(shop);
        legacy.putString("sdmversion", "0.0.1");
        send("server.SendChangesShopC2S", new Class<?>[]{CompoundTag.class}, new Object[]{legacy}, context);
        CompoundTag wrongList = Bridge.serialize(page);
        ListTag strings = new ListTag();
        strings.add(net.minecraft.nbt.StringTag.valueOf("invalid"));
        wrongList.put("tabEntry", strings);
        send("server.SendChangesShopEntriesC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, wrongList}, context);
        send("server.move.SendMoveShopTabC2S", new Class<?>[]{int.class, int.class}, new Object[]{-1, 0}, context);
        send("server.move.SendMoveShopEntryC2S", new Class<?>[]{UUID.class, int.class, int.class}, new Object[]{TAB, 0, Integer.MAX_VALUE}, context);
        helper.assertTrue(before.equals(ShopSnapshot.capture(shop).hash()), "invalid OP edits cannot partially mutate the shop");
        helper.assertTrue(java.util.Arrays.equals(diskBefore, Files.readAllBytes(shopFile)), "invalid OP edits leave persisted shop bytes unchanged");
        AuditService.flush();
        String date = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        UUID actorId = Bridge.player(context).getUUID();
        long denied = Files.readAllLines(AuditService.directory().resolve(date + "-warnings.jsonl"), StandardCharsets.UTF_8).stream()
                .map(line -> com.google.gson.JsonParser.parseString(line).getAsJsonObject())
                .filter(record -> record.get("event").getAsString().equals("security_block")
                        && record.getAsJsonObject("actor").get("uuid").getAsString().equals(actorId.toString()))
                .count();
        helper.assertTrue(denied == 21, "all 21 malformed OP edits are explicitly rejected and audited");
        CompoundTag free = Bridge.serialize(page);
        free.getList("tabEntry", 10).getCompound(0).putLong("entryPrice", 0);
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, free}, context);
        helper.assertTrue(!before.equals(ShopSnapshot.capture(shop).hash()), "zero-price products remain supported");
        send("server.edit.SendEditShopTabC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, originalPage}, context);
        helper.assertTrue(before.equals(ShopSnapshot.capture(shop).hash()), "valid OP edit restores the complete original page");
    }

    private static ServerPlayer player(GameTestHelper helper, String name, int permission) {
        ServerPlayer actor = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        actor.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), actor);
        if (permission > 0) {
            helper.getLevel().getServer().getPlayerList().getOps().add(new ServerOpListEntry(actor.getGameProfile(), permission, false));
        }
        return actor;
    }

    @SuppressWarnings("unchecked")
    private static void verifyLimitStorage(GameTestHelper helper, ServerPlayer actor) throws Exception {
        Object limiter = Class.forName(ROOT + "common.data.LimiterData").getField("SERVER").get(null);
        Map<UUID, Map<UUID, Integer>> tabs = (Map<UUID, Map<UUID, Integer>>) Bridge.field(limiter, "PLAYER_TAB_DATA");
        Map<UUID, Map<UUID, Integer>> entries = (Map<UUID, Map<UUID, Integer>>) Bridge.field(limiter, "PLAYER_ENTRY_DATA");
        UUID tabOnly = UUID.randomUUID();
        tabs.put(tabOnly, new java.util.HashMap<>(Map.of(TAB, 3)));
        helper.assertTrue(!entries.containsKey(tabOnly), "fixture has only tab-limit data");
        CompoundTag saved = Bridge.serialize(limiter);
        Bridge.invoke(limiter.getClass(), "deserializeNBT", void.class, limiter, new Class<?>[]{CompoundTag.class}, saved);
        helper.assertTrue(tabs.getOrDefault(tabOnly, Map.of()).getOrDefault(TAB, 0) == 3,
                "tab-only limit data survives server save/load roundtrip");
        helper.assertTrue(used(actor, GLOBAL, true) == 2 && used(actor, LIMITED, false) == 2,
                "global and personal limits survive save/load without global data copied to personal rows");
        tabs.remove(tabOnly);
        entries.remove(tabOnly);
    }

    private static Object context(ServerPlayer player) throws Exception {
        Class<?> type = Class.forName("dev.architectury.networking.NetworkManager$PacketContext");
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, arguments) -> switch (method.getName()) {
            case "getPlayer" -> player;
            case "queue" -> { ((Runnable) arguments[0]).run(); yield null; }
            case "toString" -> "SecuritySmokeContext";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0];
            default -> null;
        });
    }

    private static CompoundTag product(UUID id, boolean sell) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID("entryUUID", id);
        entry.putLong("entryPrice", 10);
        entry.putInt("entryCount", 1);
        entry.putBoolean("isSell", sell);
        entry.put("icon", new ItemStack(Items.STONE).save(new CompoundTag()));
        CompoundTag type = new CompoundTag();
        type.putString("shopEntryTypeID", "shopItemEntryType");
        type.put("itemStack", new ItemStack(Items.STONE).save(new CompoundTag()));
        entry.put("entryType", type);
        CompoundTag seller = new CompoundTag();
        seller.putString("shopSellerTypeID", "money");
        seller.putString("moneyID", "base_money");
        entry.put("shopSeller", seller);
        return entry;
    }

    private static void send(String name, Class<?>[] parameters, Object[] arguments, Object context) throws Exception {
        Class<?> packetType = Class.forName(ROOT + "network." + name);
        Object packet = packetType.getConstructor(parameters).newInstance(arguments);
        packetType.getMethod("handle", Class.forName("dev.architectury.networking.NetworkManager$PacketContext")).invoke(packet, context);
    }

    private static void create(Object context, CompoundTag entry) throws Exception {
        send("server.create.SendCreateShopEntryC2S", new Class<?>[]{UUID.class, CompoundTag.class}, new Object[]{TAB, entry}, context);
    }

    private static void edit(Object context, UUID id, CompoundTag entry) throws Exception {
        send("server.edit.SendEditShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, CompoundTag.class}, new Object[]{TAB, id, entry}, context);
    }

    private static void buy(Object context, UUID entry, int count) throws Exception {
        send("client.SendBuyShopEntryC2S", new Class<?>[]{UUID.class, UUID.class, int.class}, new Object[]{TAB, entry, count}, context);
    }

    private static Object lookup(UUID id) throws Exception {
        Object shop = Bridge.mainShop();
        Object page = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, TAB);
        return page.getClass().getMethod("getShopEntry", UUID.class).invoke(page, id);
    }

    private static void money(Player actor, long balance) throws Exception {
        Class.forName(ROOT + "SDMShopR").getMethod("setMoney", Player.class, long.class).invoke(null, actor, balance);
    }

    private static long balance(Player actor) throws Exception {
        return ((Number) Class.forName(ROOT + "SDMShopR").getMethod("getMoney", Player.class).invoke(null, actor)).longValue();
    }

    @SuppressWarnings("unchecked")
    private static int used(ServerPlayer actor, UUID id, boolean global) throws Exception {
        Class<?> type = Class.forName(ROOT + "common.data.LimiterData");
        Object limiter = type.getField("SERVER").get(null);
        Map<UUID, Map<UUID, Integer>> values = (Map<UUID, Map<UUID, Integer>>) Bridge.field(limiter, "PLAYER_ENTRY_DATA");
        UUID owner = global ? (UUID) type.getField("defaul_").get(null) : actor.getUUID();
        return values.getOrDefault(owner, Map.of()).getOrDefault(id, 0);
    }

    @SuppressWarnings("unchecked")
    private static int tabUsed(ServerPlayer actor) throws Exception {
        Object limiter = Class.forName(ROOT + "common.data.LimiterData").getField("SERVER").get(null);
        Map<UUID, Map<UUID, Integer>> values = (Map<UUID, Map<UUID, Integer>>) Bridge.field(limiter, "PLAYER_TAB_DATA");
        return values.getOrDefault(actor.getUUID(), Map.of()).getOrDefault(TAB, 0);
    }
}
