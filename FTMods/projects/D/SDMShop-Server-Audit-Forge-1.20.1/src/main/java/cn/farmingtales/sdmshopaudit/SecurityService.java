package cn.farmingtales.sdmshopaudit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;

public final class SecurityService {
    private static final String ROOT = "net.sixik.sdmshoprework.";
    private static final ThreadLocal<Deque<Trade>> TRADES = ThreadLocal.withInitial(ArrayDeque::new);
    private static MinecraftServer server;
    private static TrustedCommands commands;
    private static volatile boolean ready;

    private SecurityService() {
    }

    public static void start(MinecraftServer minecraftServer) {
        ready = false;
        server = minecraftServer;
        try {
            commands = new TrustedCommands(FMLPaths.CONFIGDIR.get().resolve("sdmshop-audit-trusted-commands.json"),
                    OfficialCatalog.load().trustedCommands());
            ready = true;
            AuditMod.LOGGER.info("SDMShop security patch active: server OP authentication, trusted commands and trade guards");
        } catch (Exception | LinkageError exception) {
            AuditMod.LOGGER.error("SDMShop security could not initialize; edits and trades remain BLOCKED (fail closed)", exception);
        }
    }

    public static void stop() {
        tick();
        ready = false;
        commands = null;
        server = null;
    }

    private static void player(ServerPlayer player) {
        if (!ready || server == null) {
            throw new IllegalArgumentException("SECURITY_NOT_READY");
        }
        if (player == null || player.getServer() != server || !server.isSameThread()) {
            throw new IllegalArgumentException("INVALID_SERVER_PLAYER_OR_THREAD");
        }
    }

    public static boolean edit(Object packet, Object context) {
        ServerPlayer actor = null;
        try {
            actor = Bridge.player(context);
            player(actor);
            if (!actor.hasPermissions(2)) {
                throw new IllegalArgumentException("EDIT_REQUIRES_SERVER_OP_LEVEL_2");
            }
            EditValidation.check(packet);
            return true;
        } catch (Exception | LinkageError exception) {
            deny(actor, packet, reason(exception));
            return false;
        }
    }

    public static boolean purchase(Object packet, Object context) {
        ServerPlayer actor = null;
        try {
            actor = Bridge.player(context);
            player(actor);
            if (!TRADES.get().isEmpty()) {
                throw new IllegalArgumentException("NESTED_PACKET_TRADE_NOT_SUPPORTED");
            }
            Object entry = Bridge.entry(packet);
            if (entry == null) {
                throw new IllegalArgumentException("UNKNOWN_SHOP_ENTRY");
            }
            Object tab = Bridge.field(entry, "shopTab");
            int count = ((Number) Bridge.field(packet, "count")).intValue();
            validate(actor, entry, count);
            List<Limit> limits = new ArrayList<>();
            limits.add(limit(actor, tab, false, count));
            limits.add(limit(actor, entry, true, count));
            TRADES.get().push(new Trade(packet, actor, entry, count, limits));
            return true;
        } catch (Exception | LinkageError exception) {
            deny(actor, packet, reason(exception));
            return false;
        }
    }

    public static boolean beforeDispatch(Object packet) {
        Trade trade = TRADES.get().peek();
        if (trade == null || trade.packet != packet) {
            deny(null, packet, "TRADE_CONTEXT_UNAVAILABLE");
            return false;
        }
        try {
            validate(trade.player, trade.entry, trade.count);
            return true;
        } catch (Exception | LinkageError exception) {
            trade.failed = true;
            deny(trade.player, packet, reason(exception));
            return false;
        }
    }

    public static boolean entry(Player playerObject, Object type, int count, Object entry, boolean sell) {
        ServerPlayer actor = playerObject instanceof ServerPlayer value ? value : null;
        try {
            validate(actor, entry, count);
            if ((boolean) Bridge.field(entry, "isSell") != sell) {
                throw new IllegalArgumentException("ENTRY_DIRECTION_MISMATCH");
            }
            if (Bridge.entryType(entry) != type) {
                throw new IllegalArgumentException("ENTRY_TYPE_MISMATCH");
            }
            return true;
        } catch (Exception | LinkageError exception) {
            failure();
            deny(actor, entry, reason(exception));
            return false;
        }
    }

    private static void validate(ServerPlayer actor, Object entry, int count) throws ReflectiveOperationException {
        player(actor);
        if (entry == null) {
            throw new IllegalArgumentException("UNKNOWN_SHOP_ENTRY");
        }
        CompoundTag data = Bridge.serialize(entry);
        boolean sell = data.getBoolean("isSell");
        long amount = SecurityRules.quote(data.getLong("entryPrice"), count, data.getInt("entryCount"));
        Long balance = Bridge.paymentBalance(entry, actor);
        if (balance == null) {
            throw new IllegalArgumentException("PAYMENT_BALANCE_UNAVAILABLE");
        }
        SecurityRules.balance(balance, amount, sell, data.getCompound("shopSeller").getString("shopSellerTypeID").equals("item"));
        Object type = Bridge.entryType(entry);
        Class<?> abstractType = Class.forName(ROOT + "api.shop.AbstractShopEntryType");
        Object sellType = Bridge.invoke(abstractType, "getSellType", Class.forName(ROOT + "api.shop.AbstractShopEntryType$SellType"), type, new Class<?>[0]);
        if (!(boolean) Bridge.invoke(sellType.getClass(), sell ? "isSell" : "isBuy", boolean.class, sellType, new Class<?>[0])) {
            throw new IllegalArgumentException("UNSUPPORTED_TRADE_DIRECTION");
        }
        if (!(boolean) Bridge.invoke(abstractType, "canExecute", boolean.class, type,
                new Class<?>[]{Player.class, boolean.class, int.class, Class.forName(ROOT + "api.shop.AbstractShopEntry")},
                actor, sell, count, entry)) {
            throw new IllegalArgumentException("PRODUCT_CAN_EXECUTE_REJECTED");
        }
        Object tab = Bridge.field(entry, "shopTab");
        ServerConditions.check(actor, entry, tab);
        if (data.getCompound("entryType").getString("shopEntryTypeID").equals("commandType")) {
            trusted(Bridge.entryType(entry));
        }
    }

    private static String trusted(Object type) throws ReflectiveOperationException {
        String command = (String) Bridge.field(type, "command");
        String fingerprint = SecurityRules.commandFingerprint(command, (boolean) Bridge.field(type, "elevatePerms"),
                (boolean) Bridge.field(type, "silent"));
        if (commands == null || !commands.contains(fingerprint)) {
            throw new IllegalArgumentException("UNTRUSTED_COMMAND_PRODUCT:" + fingerprint);
        }
        return command;
    }

    public static void executeCommand(Object type, Player playerObject, int count, Object entry) {
        if (!entry(playerObject, type, count, entry, false)) {
            return;
        }
        ServerPlayer actor = (ServerPlayer) playerObject;
        try {
            String template = trusted(type);
            String name = actor.getGameProfile().getName();
            if (template.contains("{player}") && !name.matches("[A-Za-z0-9_]{1,16}")) {
                throw new IllegalArgumentException("UNSAFE_PLAYER_NAME_IN_COMMAND_TEMPLATE");
            }
            String rendered = template.replace("{player}", name);
            CommandSourceStack source = actor.createCommandSourceStack();
            if ((boolean) Bridge.field(type, "elevatePerms")) {
                source = source.withPermission(2);
            }
            if ((boolean) Bridge.field(type, "silent")) {
                source = source.withSuppressedOutput();
            }
            long amount = SecurityRules.quote(((Number) Bridge.field(entry, "entryPrice")).longValue(), count,
                    ((Number) Bridge.field(entry, "entryCount")).intValue());
            if (server.getCommands().performPrefixedCommand(source, rendered) <= 0) {
                throw new IllegalStateException("COMMAND_RETURNED_NO_SUCCESS");
            }
            if (!Bridge.pay(entry, actor, amount)) {
                failure();
                AuditService.failure(new IllegalStateException("COMMAND_PAYMENT_REJECTED"));
            }
        } catch (Exception | LinkageError exception) {
            failure();
            AuditService.failure(exception);
            deny(actor, entry, "COMMAND_EXECUTION_FAILED:" + reason(exception));
        }
    }

    public static boolean payment(Object seller, Player playerObject, Object entry, long amount) {
        ServerPlayer actor = playerObject instanceof ServerPlayer value ? value : null;
        try {
            player(actor);
            Long balance = Bridge.paymentBalance(entry, actor);
            if (balance == null) {
                throw new IllegalArgumentException("PAYMENT_BALANCE_UNAVAILABLE");
            }
            boolean itemCurrency = seller.getClass().getSimpleName().equals("ItemSellerType");
            SecurityRules.balance(balance, amount, (boolean) Bridge.field(entry, "isSell"), itemCurrency);
            return true;
        } catch (Exception | LinkageError exception) {
            failure();
            deny(actor, entry, reason(exception));
            return false;
        }
    }

    public static void paymentResult(Object playerObject, Object entry, boolean accepted) {
        Trade trade = TRADES.get().peek();
        if (trade != null && trade.player == playerObject && trade.entry == entry) {
            trade.payments++;
            trade.failed |= !accepted;
        }
    }

    public static void completed() {
        Trade trade = TRADES.get().peek();
        if (trade != null) {
            trade.completed = true;
        }
    }

    public static void failure() {
        Trade trade = TRADES.get().peek();
        if (trade != null) {
            trade.failed = true;
        }
    }

    public static void finish(Object packet) {
        Deque<Trade> trades = TRADES.get();
        if (!trades.isEmpty() && trades.peek().packet == packet) {
            restore(trades.pop(), false);
        }
        if (trades.isEmpty()) {
            TRADES.remove();
        }
    }

    public static void tick() {
        Deque<Trade> trades = TRADES.get();
        while (!trades.isEmpty()) {
            restore(trades.pop(), true);
        }
        TRADES.remove();
    }

    @SuppressWarnings("unchecked")
    private static Limit limit(ServerPlayer actor, Object owner, boolean entry, int batches) throws ReflectiveOperationException {
        int maximum = ((Number) Bridge.field(owner, "limit")).intValue();
        Object limiter = Class.forName(ROOT + "common.data.LimiterData").getField("SERVER").get(null);
        Map<UUID, Map<UUID, Integer>> map = (Map<UUID, Map<UUID, Integer>>) Bridge.field(limiter,
                entry ? "PLAYER_ENTRY_DATA" : "PLAYER_TAB_DATA");
        boolean global = (boolean) Bridge.field(owner, "globalLimit");
        UUID actorId = global ? (UUID) limiter.getClass().getField("defaul_").get(null) : actor.getUUID();
        UUID id = (UUID) Bridge.field(owner, entry ? "entryUUID" : "shopTabUUID");
        Map<UUID, Integer> values = map.get(actorId);
        Integer used = values == null ? null : values.get(id);
        SecurityRules.limit(used == null ? 0 : used, maximum, batches);
        return new Limit(map, actorId, id, used, maximum != 0, limiter);
    }

    private static void restore(Trade trade, boolean interrupted) {
        if (!interrupted && trade.completed && trade.payments > 0 && !trade.failed) {
            return;
        }
        try {
            boolean changed = false;
            Object limiter = null;
            for (Limit limit : trade.limits) {
                if (!limit.enabled) {
                    continue;
                }
                Map<UUID, Integer> values = limit.map.get(limit.actor);
                if (values != null) {
                    Integer current = values.get(limit.id);
                    changed |= !java.util.Objects.equals(current, limit.used);
                    if (limit.used == null) {
                        values.remove(limit.id);
                        if (values.isEmpty()) {
                            limit.map.remove(limit.actor);
                        }
                    } else {
                        values.put(limit.id, limit.used);
                    }
                }
                limiter = limit.limiter;
            }
            if (changed && limiter != null) {
                Bridge.invoke(limiter.getClass(), "save", void.class, limiter, new Class<?>[]{MinecraftServer.class}, server);
                Class<?> packetType = Class.forName(ROOT + "network.client.SendEntryLimitS2C");
                List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
                if (!players.contains(trade.player)) {
                    players.add(trade.player);
                }
                for (ServerPlayer actor : players) {
                    CompoundTag data = (CompoundTag) Bridge.invoke(limiter.getClass(), "serializeClient", CompoundTag.class,
                            limiter, new Class<?>[]{UUID.class}, actor.getUUID());
                    Object packet = packetType.getConstructor(CompoundTag.class).newInstance(data);
                    Bridge.invoke(packetType, "sendTo", void.class, packet, new Class<?>[]{ServerPlayer.class}, actor);
                }
            }
        } catch (Exception | LinkageError exception) {
            ready = false;
            AuditMod.LOGGER.error("SDMShop limit rollback failed; further SDM edits/trades BLOCKED until server restart", exception);
        }
    }

    public static String approve(ServerPlayer actor, UUID tab, UUID entry) throws Exception {
        Object shop = Bridge.mainShop();
        Object page = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, tab);
        Object product = page == null ? null : page.getClass().getMethod("getShopEntry", UUID.class).invoke(page, entry);
        if (!ready || !server.isSameThread() || actor != null && !actor.hasPermissions(4) || product == null
                || !Bridge.serialize(product).getCompound("entryType").getString("shopEntryTypeID").equals("commandType")) {
            throw new IllegalArgumentException("找不到指令商品，或安全服务尚未就绪。");
        }
        Object type = Bridge.entryType(product);
        String fingerprint = commands.approve((String) Bridge.field(type, "command"),
                (boolean) Bridge.field(type, "elevatePerms"), (boolean) Bridge.field(type, "silent"));
        AuditService.commandTrust(actor, "COMMAND_APPROVED", fingerprint, Bridge.serialize(product));
        return fingerprint;
    }

    public static boolean revoke(ServerPlayer actor, String fingerprint) throws Exception {
        if (!ready || !server.isSameThread() || actor != null && !actor.hasPermissions(4)) {
            throw new IllegalArgumentException("安全服务尚未就绪。");
        }
        boolean removed = commands.revoke(fingerprint);
        AuditService.commandTrust(actor, "COMMAND_REVOKED", fingerprint, null);
        return removed;
    }

    private static void deny(ServerPlayer actor, Object operation, String reason) {
        AuditService.securityBlocked(actor, operation, reason);
        AuditMod.LOGGER.warn("[SDM安全拦截] 玩家={} UUID={} 操作={} 原因={}", actor == null ? "UNKNOWN" : actor.getGameProfile().getName(),
                actor == null ? "UNKNOWN" : actor.getUUID(), operation.getClass().getSimpleName(), AuditWriter.singleLine(reason));
    }

    private static String reason(Throwable exception) {
        return exception instanceof IllegalArgumentException ? String.valueOf(exception.getMessage())
                : "SECURITY_CHECK_FAILED:" + exception.getClass().getSimpleName();
    }

    public static String status() {
        return ready ? "安全补丁已启用（编辑需 OP≥2，指令需可信白名单）" : "安全服务异常，编辑和交易已拒绝（fail closed）";
    }

    private record Limit(Map<UUID, Map<UUID, Integer>> map, UUID actor, UUID id, Integer used, boolean enabled, Object limiter) {
    }

    private static final class Trade {
        private final Object packet;
        private final ServerPlayer player;
        private final Object entry;
        private final int count;
        private final List<Limit> limits;
        private int payments;
        private boolean completed;
        private boolean failed;

        private Trade(Object packet, ServerPlayer player, Object entry, int count, List<Limit> limits) {
            this.packet = packet;
            this.player = player;
            this.entry = entry;
            this.count = count;
            this.limits = limits;
        }
    }
}
