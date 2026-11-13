package cn.farmingtales.sdmshopaudit;

import cn.farmingtales.sdmshopaudit.mixin.ServerMixinPlugin;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

public final class AuditService {
    private static volatile AuditService instance;
    private final MinecraftServer server;
    private final AuditWriter writer;
    private final OfficialCatalog catalog;
    private final ZoneId zone;
    private final String session = UUID.randomUUID().toString();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong hookFailures = new AtomicLong();
    private final ThreadLocal<Deque<Request>> requests = ThreadLocal.withInitial(ArrayDeque::new);
    private final Map<String, ShopSnapshot> known = new HashMap<>();
    private long ticks;
    private long lastError;

    private AuditService(MinecraftServer server) throws Exception {
        this.server = server;
        zone = ZoneId.of(AuditConfig.TIME_ZONE.get());
        catalog = OfficialCatalog.load();
        Path directory = FMLPaths.GAMEDIR.get().resolve("logs/sdmshop-audit");
        writer = new AuditWriter(directory);
    }

    public static void start(MinecraftServer server) {
        try {
            instance = new AuditService(server);
            JsonObject record = instance.record("audit_started", unknownActor());
            JsonArray hooks = new JsonArray();
            ServerMixinPlugin.appliedTargets().forEach(hooks::add);
            record.add("appliedHooks", hooks);
            record.addProperty("mode", "SECURITY_PATCH_AND_AUDIT");
            record.addProperty("security", SecurityService.status());
            record.add("officialCatalog", instance.catalog.summary());
            instance.writer.emit("system", record, List.of(), "安全补丁与审计启动；目录 " + instance.writer.directory());
            instance.scan("startup", unknownActor(), true);
            AuditMod.LOGGER.info("SDMShop audit active: {}; security patch enabled; {} hooked classes",
                    instance.writer.directory(), hooks.size());
        } catch (Exception | LinkageError exception) {
            AuditMod.LOGGER.error("SDMShop audit could not start; auditing is unavailable", exception);
            if (instance != null) {
                instance.writer.close();
                instance = null;
            }
        }
    }

    public static void stop() {
        AuditService service = instance;
        if (service != null) {
            guard(() -> {
                service.finishPending();
                service.scan("shutdown", unknownActor(), false);
                JsonObject record = service.record("audit_stopped", unknownActor());
                record.addProperty("hookFailures", service.hookFailures.get());
                service.writer.emit("system", record, List.of(), "审计停止；写入错误=" + service.writer.failures());
            });
            instance = null;
            service.writer.close();
        }
    }

    public static void tick() {
        guard(() -> {
            instance.finishPending();
            if (++instance.ticks % AuditConfig.SCAN_INTERVAL.get() == 0) {
                instance.scan("periodic_scan", unknownActor(), false);
            }
        });
    }

    public static void manualSnapshot(ServerPlayer player) {
        guard(() -> instance.scan("manual_snapshot", actor(player), true));
    }

    public static void beginEdit(Object packet, Object context) {
        guard(() -> {
            AuditService service = instance;
            ServerPlayer player = Bridge.player(context);
            ShopSnapshot before = ShopSnapshot.capture(Bridge.mainShop());
            service.observe(before, "detected_before_edit", unknownActor(), null);
            Request request = new Request(packet, player, actor(player), true);
            request.before = before;
            service.requests.get().push(request);
            JsonObject record = service.requestRecord("edit_request", request);
            record.addProperty("beforeHash", before.hash());
            record.addProperty("nonOpEdit", player == null || !player.hasPermissions(2));
            JsonObject arguments = new JsonObject();
            for (String name : List.of("tabID", "tabId", "tab", "entryID", "entryId", "from", "to", "index")) {
                Object value = Bridge.optionalField(packet, name);
                if (value != null) {
                    arguments.addProperty(name, value.toString());
                }
            }
            record.add("arguments", arguments);
            Object payload = Bridge.optionalField(packet, "nbt");
            List<ShopSnapshot> snapshots = List.of(before);
            if (payload instanceof CompoundTag tag) {
                String snbt = tag.toString();
                ShopSnapshot proposed = new ShopSnapshot("request", ShopSnapshot.toJson(tag).getAsJsonObject(),
                        snbt, ShopSnapshot.digest(snbt));
                record.addProperty("requestPayloadHash", proposed.hash());
                record.add("requestPayload", ShopDiff.limited(proposed.data()));
                snapshots = List.of(before, proposed);
            }
            service.write("edits", record, snapshots, "编辑请求 玩家=" + name(request.actor) + " 操作=" + request.operation);
        });
    }

    public static void beginPurchase(Object packet, Object context) {
        guard(() -> {
            AuditService service = instance;
            ServerPlayer player = Bridge.player(context);
            Request request = new Request(packet, player, actor(player), false);
            service.requests.get().push(request);
            request.count = ((Number) Bridge.field(packet, "count")).intValue();
            request.entry = Bridge.entry(packet);
            if (request.entry != null) {
                request.entryBefore = Bridge.serialize(request.entry);
                request.entrySnapshot = entrySnapshot(request.entryBefore);
                request.balanceBefore = player == null ? null : Bridge.paymentBalance(request.entry, player);
            }
            JsonObject record = service.requestRecord("trade_request", request);
            service.tradeDetails(record, request);
            service.write("transactions", record, request.entrySnapshot == null ? List.of() : List.of(request.entrySnapshot), "交易请求 玩家=" + name(request.actor)
                    + " 数量=" + request.count + " 商品=" + item(request.entryBefore));
        });
    }

    public static void dispatch(boolean completed) {
        guard(() -> {
            Request request = instance.requests.get().peek();
            if (request != null && !request.edit) {
                request.dispatched = true;
                request.completed |= completed;
            }
        });
    }

    public static void payment(Object seller, Object playerObject, Object entry, long amount, boolean accepted) {
        guard(() -> {
            if (!(playerObject instanceof ServerPlayer player)) {
                return;
            }
            JsonObject payment = new JsonObject();
            payment.addProperty("sellerClass", seller.getClass().getName());
            payment.addProperty("amount", amount);
            payment.addProperty("accepted", accepted);
            Request request = instance.requests.get().peek();
            if (request != null && !request.edit && request.player == player && request.entry == entry) {
                request.payments.add(payment);
            } else {
                JsonObject record = instance.record("direct_payment", actor(player));
                record.add("payment", payment);
                record.add("entry", ShopSnapshot.toJson(Bridge.serialize(entry)));
                record.addProperty("status", accepted ? "PAYMENT_ACCEPTED_DELIVERY_UNVERIFIED" : "PAYMENT_REJECTED");
                instance.write("transactions", record, List.of(), "直接支付 玩家=" + player.getGameProfile().getName()
                        + " 金额=" + amount + " 接受=" + accepted + "；未验证商品交付");
            }
        });
    }

    public static void failure(Throwable error) {
        guard(() -> {
            Request request = instance.requests.get().peek();
            if (request != null && error != null) {
                request.error = error.getClass().getName() + ": " + error.getMessage();
            }
        });
    }

    public static void securityBlocked(ServerPlayer player, Object operation, String reason) {
        guard(() -> {
            AuditService service = instance;
            Request request = service.requests.get().peek();
            boolean associated = request != null && (request.packet == operation || request.entry == operation);
            if (associated) {
                request.blockedReason = reason;
            }
            JsonObject record = service.record("security_block", actor(player));
            record.addProperty("severity", "WARN");
            record.addProperty("status", "BLOCKED_SECURITY");
            record.addProperty("operation", operation.getClass().getSimpleName());
            record.addProperty("reason", reason);
            if (associated) {
                record.addProperty("requestId", request.id);
            }
            for (String field : List.of("tabUUID", "entryUUID", "tabID", "entryID", "count")) {
                Object value = Bridge.optionalField(operation, field);
                if (value != null) {
                    record.addProperty(field, value.toString());
                }
            }
            service.write("warnings", record, List.of(), "安全拦截 玩家=" + name(record.getAsJsonObject("actor"))
                    + " 操作=" + operation.getClass().getSimpleName() + " 原因=" + reason);
        });
    }

    public static void commandTrust(ServerPlayer player, String action, String fingerprint, CompoundTag entry) {
        guard(() -> {
            JsonObject record = instance.record("command_trust_changed", actor(player));
            record.addProperty("action", action);
            record.addProperty("fingerprint", fingerprint);
            if (entry != null) {
                record.add("entry", ShopSnapshot.toJson(entry));
            }
            instance.write("system", record, List.of(), "可信指令调整 操作者=" + name(record.getAsJsonObject("actor"))
                    + " 操作=" + action + " 指纹=" + fingerprint);
        });
    }

    public static void finish(Object packet) {
        guard(() -> {
            Deque<Request> stack = instance.requests.get();
            if (!stack.isEmpty() && stack.peek().packet == packet) {
                instance.finishRequest(stack.pop(), false);
            }
            if (stack.isEmpty()) {
                instance.requests.remove();
            }
        });
    }

    public static void saved(Object shop) {
        guard(() -> {
            if (instance.requests.get().isEmpty()) {
                instance.observe(ShopSnapshot.capture(shop), "save_method_returned", unknownActor(), null);
            }
        });
    }

    private void finishPending() throws Exception {
        Deque<Request> stack = requests.get();
        while (!stack.isEmpty()) {
            finishRequest(stack.pop(), true);
        }
        requests.remove();
    }

    private void finishRequest(Request request, boolean interrupted) throws Exception {
        if (request.edit) {
            ShopSnapshot after = ShopSnapshot.capture(Bridge.mainShop());
            boolean changed = !request.before.hash().equals(after.hash());
            JsonObject record = requestRecord("edit_result", request);
            record.addProperty("status", interrupted ? "INTERRUPTED" : request.error != null ? "ERROR" : changed ? "CHANGED" : "NO_CHANGE");
            record.addProperty("changed", changed);
            addDifference(record, request.before, after);
            known.put(after.shopId(), after);
            write("edits", record, List.of(request.before, after), "编辑结果 玩家=" + name(request.actor)
                    + " 操作=" + request.operation + " 状态=" + record.get("status").getAsString()
                    + " 页数=" + request.before.tabs() + "->" + after.tabs() + " 差异=" + record.get("diff"));
            if (changed && (request.player == null || !request.player.hasPermissions(2))) {
                AuditMod.LOGGER.warn("SDMShop was changed by non-OP {} UUID={} request={}; see audit files",
                        name(request.actor), request.actor.get("uuid"), request.id);
            }
            return;
        }
        JsonObject record = requestRecord("trade_result", request);
        String status = request.blockedReason != null ? "BLOCKED_SECURITY"
                : tradeStatus(request.dispatched, request.completed, request.payments, request.error != null, interrupted);
        record.addProperty("status", status);
        if (request.blockedReason != null) {
            record.addProperty("blockedReason", request.blockedReason);
        }
        tradeDetails(record, request);
        JsonObject officialProduct = catalog.check(request.entryBefore);
        record.add("officialProduct", officialProduct);
        record.add("payments", request.payments);
        List<ShopSnapshot> snapshots = List.of();
        if (request.entry != null) {
            CompoundTag after = Bridge.serialize(request.entry);
            ShopSnapshot afterSnapshot = entrySnapshot(after);
            snapshots = request.entrySnapshot == null ? List.of(afterSnapshot) : List.of(request.entrySnapshot, afterSnapshot);
            Long balanceAfter = request.player == null ? null : Bridge.paymentBalance(request.entry, request.player);
            record.add("entryAfter", ShopDiff.limited(afterSnapshot.data()));
            record.addProperty("entryAfterHash", afterSnapshot.hash());
            record.add("paymentBalanceAfter", balanceAfter == null ? JsonNull.INSTANCE : new com.google.gson.JsonPrimitive(balanceAfter));
            if (request.balanceBefore != null && balanceAfter != null) {
                record.addProperty("paymentBalanceDelta", BigInteger.valueOf(balanceAfter)
                        .subtract(BigInteger.valueOf(request.balanceBefore)).toString());
            }
            if (request.entryBefore != null && !request.entryBefore.equals(after)) {
                observe(ShopSnapshot.capture(Bridge.mainShop()), "trade_side_effect", request.actor, request.id);
            }
        }
        write("transactions", record, snapshots, "交易结果 玩家=" + name(request.actor) + " 类型="
                + record.get("direction") + " 商品=" + item(request.entryBefore) + " 批数=" + request.count
                + " 报价=" + record.get("quotedTotal") + " 状态=" + status
                + " 支付余额=" + record.get("paymentBalanceBefore") + "->" + record.get("paymentBalanceAfter"));
        boolean purchase = request.entryBefore != null && !request.entryBefore.getBoolean("isSell");
        if (purchase && status.equals("SUCCESS") && !officialProduct.get("allowed").getAsBoolean()) {
            JsonObject warning = record.deepCopy();
            warning.addProperty("sequence", sequence.incrementAndGet());
            warning.addProperty("event", "non_original_purchase_warning");
            warning.addProperty("severity", "WARN");
            String message = "[非原商店物品购入警告] 玩家=" + name(request.actor) + " UUID="
                    + request.actor.get("uuid") + " 商品=" + item(request.entryBefore)
                    + " 数量=" + record.get("requestedUnits") + " 原因=" + officialProduct.get("reason")
                    + " request=" + request.id;
            write("warnings", warning, List.of(), message);
            AuditMod.LOGGER.warn("{}", AuditWriter.singleLine(message));
        }
    }

    public static String tradeStatus(boolean dispatched, boolean completed, JsonArray payments, boolean error, boolean interrupted) {
        if (interrupted) {
            return "INTERRUPTED";
        }
        if (error) {
            return "ERROR";
        }
        if (!dispatched) {
            return "REJECTED_BEFORE_EXECUTION";
        }
        if (!completed) {
            return "EXECUTION_NOT_COMPLETED";
        }
        if (payments.isEmpty()) {
            return "EXECUTED_PAYMENT_UNVERIFIED";
        }
        for (var payment : payments) {
            if (!payment.getAsJsonObject().get("accepted").getAsBoolean()) {
                return "EXECUTED_PAYMENT_REJECTED";
            }
        }
        return "SUCCESS";
    }

    private void tradeDetails(JsonObject record, Request request) {
        record.addProperty("tabUuid", String.valueOf(Bridge.optionalField(request.packet, "tabUUID")));
        record.addProperty("entryUuid", String.valueOf(Bridge.optionalField(request.packet, "entryUUID")));
        record.addProperty("requestedBatches", request.count);
        record.addProperty("direction", request.entryBefore != null && request.entryBefore.getBoolean("isSell") ? "SELL" : "BUY");
        record.add("paymentBalanceBefore", request.balanceBefore == null ? JsonNull.INSTANCE : new com.google.gson.JsonPrimitive(request.balanceBefore));
        if (request.entryBefore != null) {
            if (request.entrySnapshot != null) {
                record.addProperty("entryBeforeHash", request.entrySnapshot.hash());
            }
            record.add("entryBefore", ShopDiff.limited(ShopSnapshot.toJson(request.entryBefore)));
            record.addProperty("unitPrice", request.entryBefore.getLong("entryPrice"));
            record.addProperty("unitsPerBatch", request.entryBefore.getInt("entryCount"));
            record.addProperty("requestedUnits", (long) request.count * request.entryBefore.getInt("entryCount"));
            record.addProperty("quotedTotal", BigInteger.valueOf(request.entryBefore.getLong("entryPrice"))
                    .multiply(BigInteger.valueOf(request.count)).toString());
        } else {
            record.add("quotedTotal", JsonNull.INSTANCE);
        }
    }

    private static ShopSnapshot entrySnapshot(CompoundTag tag) {
        String snbt = tag.toString();
        return new ShopSnapshot("trade_entry", ShopSnapshot.toJson(tag).getAsJsonObject(), snbt, ShopSnapshot.digest(snbt));
    }

    private void scan(String source, JsonObject actor, boolean force) throws Exception {
        for (Object shop : Bridge.shops()) {
            ShopSnapshot snapshot = ShopSnapshot.capture(shop);
            if (force) {
                observe(snapshot, source, actor, null);
                JsonObject record = record("shop_snapshot", actor);
                record.addProperty("source", source);
                record.addProperty("shopUuid", snapshot.shopId());
                record.addProperty("hash", snapshot.hash());
                record.addProperty("tabs", snapshot.tabs());
                write("edits", record, List.of(snapshot), "商店快照 来源=" + source + " 页数=" + snapshot.tabs() + " hash=" + snapshot.hash());
            } else {
                observe(snapshot, source, actor, null);
            }
        }
    }

    private void observe(ShopSnapshot after, String source, JsonObject actor, String requestId) {
        ShopSnapshot before = known.put(after.shopId(), after);
        if (before == null || before.hash().equals(after.hash())) {
            return;
        }
        JsonObject record = record("shop_change", actor);
        record.addProperty("source", source);
        record.addProperty("requestId", requestId);
        addDifference(record, before, after);
        write("edits", record, List.of(before, after), "商店变化 来源=" + source + " 玩家=" + name(actor)
                + " 页数=" + before.tabs() + "->" + after.tabs() + " 差异=" + record.get("diff"));
    }

    private void addDifference(JsonObject record, ShopSnapshot before, ShopSnapshot after) {
        record.addProperty("shopUuid", after.shopId());
        record.addProperty("beforeHash", before.hash());
        record.addProperty("afterHash", after.hash());
        record.addProperty("tabsBefore", before.tabs());
        record.addProperty("tabsAfter", after.tabs());
        record.add("diff", ShopDiff.compare(before.data(), after.data(), AuditConfig.INLINE_CHANGES.get()));
    }

    private JsonObject requestRecord(String event, Request request) {
        JsonObject record = record(event, request.actor);
        record.addProperty("requestId", request.id);
        record.addProperty("operation", request.operation);
        record.addProperty("serverThread", server.isSameThread());
        record.addProperty("error", request.error);
        return record;
    }

    private JsonObject record(String event, JsonObject actor) {
        JsonObject record = new JsonObject();
        record.addProperty("schemaVersion", 1);
        record.addProperty("time", ZonedDateTime.now(zone).toString());
        record.addProperty("session", session);
        record.addProperty("sequence", sequence.incrementAndGet());
        record.addProperty("event", event);
        record.add("actor", actor);
        return record;
    }

    private void write(String category, JsonObject record, List<ShopSnapshot> snapshots, String summary) {
        record.addProperty("snapshotsEnabled", AuditConfig.SNAPSHOTS.get());
        writer.emit(category, record, AuditConfig.SNAPSHOTS.get() ? snapshots : List.of(), summary);
    }

    private static JsonObject actor(ServerPlayer player) throws Exception {
        if (player == null) {
            return unknownActor();
        }
        JsonObject actor = new JsonObject();
        actor.addProperty("name", player.getGameProfile().getName());
        actor.addProperty("uuid", player.getUUID().toString());
        actor.addProperty("opLevel2", player.hasPermissions(2));
        actor.addProperty("editMode", Bridge.editMode(player));
        actor.addProperty("dimension", player.level().dimension().location().toString());
        actor.addProperty("x", player.getX());
        actor.addProperty("y", player.getY());
        actor.addProperty("z", player.getZ());
        return actor;
    }

    private static JsonObject unknownActor() {
        JsonObject actor = new JsonObject();
        actor.addProperty("name", "UNKNOWN");
        actor.add("uuid", JsonNull.INSTANCE);
        actor.addProperty("attribution", "unavailable");
        return actor;
    }

    private static String name(JsonObject actor) {
        return actor.get("name").getAsString();
    }

    private static String item(CompoundTag entry) {
        return entry == null ? "UNKNOWN" : entry.getCompound("entryType").getCompound("itemStack").getString("id");
    }

    public static String status() {
        AuditService service = instance;
        return service == null ? SecurityService.status() + "；审计未启动，请检查服务器启动错误。" : "SDMShop " + SecurityService.status() + "；目录="
                + service.writer.directory() + "；已写=" + service.writer.written() + "；排队=" + service.writer.queued()
                + "；文件错误=" + service.writer.failures() + "；钩子错误=" + service.hookFailures.get();
    }

    public static Path directory() {
        return instance == null ? null : instance.writer.directory();
    }

    public static void flush() throws Exception {
        if (instance != null) {
            instance.writer.flush();
        }
    }

    private static void guard(CheckedAction action) {
        AuditService service = instance;
        if (service == null) {
            return;
        }
        try {
            action.run();
        } catch (Exception | LinkageError exception) {
            service.hookFailures.incrementAndGet();
            if (System.currentTimeMillis() - service.lastError > 30000) {
                service.lastError = System.currentTimeMillis();
                AuditMod.LOGGER.error("SDMShop auditing failed; independent security guards remain active", exception);
            }
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private static final class Request {
        private final Object packet;
        private final ServerPlayer player;
        private final JsonObject actor;
        private final boolean edit;
        private final String id = UUID.randomUUID().toString();
        private final String operation;
        private final JsonArray payments = new JsonArray();
        private ShopSnapshot before;
        private Object entry;
        private CompoundTag entryBefore;
        private ShopSnapshot entrySnapshot;
        private Long balanceBefore;
        private int count;
        private boolean dispatched;
        private boolean completed;
        private String error;
        private String blockedReason;

        private Request(Object packet, ServerPlayer player, JsonObject actor, boolean edit) {
            this.packet = packet;
            this.player = player;
            this.actor = actor;
            this.edit = edit;
            operation = packet.getClass().getSimpleName();
        }
    }
}
