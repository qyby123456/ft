package cn.farmingtales.sdmshopaudit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public final class AuditVerifier {
    private static int assertions;

    public static void main(String[] arguments) throws Exception {
        security();
        JsonObject before = fixture();
        JsonObject after = before.deepCopy();
        JsonObject entry = after.getAsJsonArray("shopTabs").get(0).getAsJsonObject()
                .getAsJsonArray("tabEntry").get(0).getAsJsonObject();
        entry.addProperty("entryPrice", 999999999999L);
        JsonObject difference = ShopDiff.compare(before, after, 200);
        require(difference.get("totalChanges").getAsInt() == 1, "price change count");
        JsonObject priceChange = difference.getAsJsonArray("changes").get(0).getAsJsonObject();
        require(priceChange.get("kind").getAsString().equals("ENTRY_CHANGED"), "price change kind");
        require(priceChange.getAsJsonObject("fields").getAsJsonObject("entryPrice")
                .get("before").getAsLong() == 10, "old price");
        require(priceChange.getAsJsonObject("fields").getAsJsonObject("entryPrice")
                .get("after").getAsLong() == 999999999999L, "64-bit new price");
        require(ShopDiff.compare(before, before.deepCopy(), 200).get("totalChanges").getAsInt() == 0, "unchanged snapshot");

        JsonObject added = before.deepCopy();
        added.getAsJsonArray("shopTabs").add(JsonParser.parseString("{\"shopTabUUID\":[0,0,0,3],\"title\":\"异常页面\",\"tabEntry\":[]}"));
        require(kind(ShopDiff.compare(before, added, 200), "TAB_ADDED"), "added tab");
        require(kind(ShopDiff.compare(added, before, 200), "TAB_REMOVED"), "removed tab");
        JsonArray tabs = added.getAsJsonArray("shopTabs");
        JsonArray reversed = new JsonArray();
        reversed.add(tabs.get(1));
        reversed.add(tabs.get(0));
        JsonObject moved = added.deepCopy();
        moved.add("shopTabs", reversed);
        require(kind(ShopDiff.compare(added, moved, 200), "TAB_ORDER_CHANGED"), "moved tab");
        JsonObject tooMany = moved.deepCopy();
        tooMany.getAsJsonArray("shopTabs").get(1).getAsJsonObject().getAsJsonArray("tabEntry")
                .get(0).getAsJsonObject().addProperty("entryPrice", 20);
        require(ShopDiff.compare(before, tooMany, 1).get("truncated").getAsBoolean(), "inline limit");
        require(ShopDiff.identifier(JsonParser.parseString("[0,0,0,1]"), 0)
                .equals("00000000-0000-0000-0000-000000000001"), "NBT UUID decoding");

        JsonArray accepted = new JsonArray();
        JsonObject payment = new JsonObject();
        payment.addProperty("accepted", true);
        accepted.add(payment);
        require(AuditService.tradeStatus(true, true, accepted, false, false).equals("SUCCESS"), "completed payment");
        require(!AuditService.tradeStatus(false, false, accepted, false, false).equals("SUCCESS"), "request is not success");
        require(!AuditService.tradeStatus(true, true, new JsonArray(), false, false).equals("SUCCESS"), "unverified payment");
        payment.addProperty("accepted", false);
        require(AuditService.tradeStatus(true, true, accepted, false, false).equals("EXECUTED_PAYMENT_REJECTED"), "rejected payment");
        require(AuditService.tradeStatus(true, true, accepted, true, false).equals("ERROR"), "caught original error");
        require(AuditService.tradeStatus(true, false, accepted, false, true).equals("INTERRUPTED"), "uncaught interruption");

        Files.createDirectories(Path.of("build/verification"));
        Path directory = Files.createTempDirectory(Path.of("build/verification"), "audit-");
        String snbt = "{shopName:\"取证快照\",shopTabs:[]}";
        ShopSnapshot snapshot = new ShopSnapshot("test", before, snbt, ShopSnapshot.digest(snbt));
        try (AuditWriter writer = new AuditWriter(directory)) {
            JsonObject record = record(1);
            record.addProperty("actor", "玩家\n伪造记录");
            writer.emit("edits", record, List.of(snapshot), "玩家\n伪造记录");
            CountDownLatch threads = new CountDownLatch(4);
            for (int workerIndex = 0; workerIndex < 4; workerIndex++) {
                int workerId = workerIndex;
                new Thread(() -> {
                    try {
                        for (int recordIndex = 0; recordIndex < 25; recordIndex++) {
                            writer.emit("transactions", record(workerId * 25 + recordIndex + 2), List.of(), "购买记录");
                        }
                    } finally {
                        threads.countDown();
                    }
                }).start();
            }
            require(threads.await(10, TimeUnit.SECONDS), "producer completion");
            writer.flush();
            require(writer.written() == 101 && writer.failures() == 0, "concurrent writes without loss");
        }
        List<String> lines = Files.readAllLines(directory.resolve("2026-10-12.log"), StandardCharsets.UTF_8);
        require(lines.size() == 101, "human log injection resistance");
        require(Files.readAllLines(directory.resolve("2026-10-12-transactions.jsonl")).size() == 100, "transaction JSONL lines");
        String jsonLine = Files.readString(directory.resolve("2026-10-12-edits.jsonl"));
        require(JsonParser.parseString(jsonLine).getAsJsonObject().get("actor").getAsString()
                .equals("玩家\n伪造记录"), "lossless UTF-8 and JSON escaping");
        try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(directory.resolve("snapshots/" + snapshot.hash() + ".snbt.gz")))) {
            require(new String(gzip.readAllBytes(), StandardCharsets.UTF_8).equals(snbt), "snapshot round trip");
        }
        Path failedDirectory = Files.createTempDirectory(Path.of("build/verification"), "snapshot-failure-");
        try (AuditWriter writer = new AuditWriter(failedDirectory)) {
            Files.delete(failedDirectory.resolve("snapshots"));
            Files.writeString(failedDirectory.resolve("snapshots"), "blocked snapshot directory");
            writer.emit("edits", record(1), List.of(snapshot), "快照失败仍保留操作记录");
            writer.flush();
            require(writer.written() == 1 && writer.failures() == 1, "snapshot failure does not discard audit event");
        }
        JsonObject recovered = JsonParser.parseString(Files.readString(
                failedDirectory.resolve("2026-10-12-edits.jsonl"))).getAsJsonObject();
        require(recovered.getAsJsonArray("snapshotErrors").size() == 1, "missing snapshot is explicitly reported");
        System.out.println("AUDIT_VERIFIER_PASS assertions=" + assertions + " evidence=" + directory.toAbsolutePath());
    }

    private static void security() throws Exception {
        require(SecurityRules.quote(10, 1024, 64) == 10240, "positive bounded quote");
        require(SecurityRules.quote(0, 1, 1) == 0, "free products remain supported");
        rejects(() -> SecurityRules.quote(10, 0, 1), "zero batches");
        rejects(() -> SecurityRules.quote(10, -1, 1), "negative batches");
        rejects(() -> SecurityRules.quote(10, 1025, 1), "excessive batches");
        rejects(() -> SecurityRules.quote(-1, 1, 1), "negative prices");
        rejects(() -> SecurityRules.quote(Long.MAX_VALUE, 2, 1), "price overflow");
        rejects(() -> SecurityRules.quote(1, 2, Integer.MAX_VALUE), "item count overflow");
        rejects(() -> SecurityRules.quote(1, 1, 0), "zero item count");
        rejects(() -> SecurityRules.balance(0, 1, false, false), "insufficient funds");
        rejects(() -> SecurityRules.balance(-1, 0, false, false), "negative balance");
        rejects(() -> SecurityRules.balance(1, -1, false, false), "negative direct payment");
        rejects(() -> SecurityRules.balance(Long.MAX_VALUE, 1, true, false), "sell balance overflow");
        rejects(() -> SecurityRules.balance(0, (long) Integer.MAX_VALUE + 1, true, true), "item currency truncation");
        SecurityRules.balance(0, 10, true, false);
        SecurityRules.balance(10, 10, false, false);
        SecurityRules.limit(1, 2, 1);
        rejects(() -> SecurityRules.limit(2, 2, 1), "exact limit exhaustion");
        rejects(() -> SecurityRules.limit(1, 2, 2), "batch overshoot");
        rejects(() -> SecurityRules.limit(Integer.MAX_VALUE, 0, 1), "limit counter overflow");
        rejects(() -> SecurityRules.limit(0, -1, 1), "negative limit");
        String trusted = SecurityRules.commandFingerprint("/test {player}", true, false);
        require(!trusted.equals(SecurityRules.commandFingerprint("/test Other", true, false)), "exact command payload matching");
        require(!trusted.equals(SecurityRules.commandFingerprint("/test {player}", false, false)), "permission flag bound to approval");
        require(!trusted.equals(SecurityRules.commandFingerprint("/test {player}", true, true)), "silent flag bound to approval");
        Path directory = Files.createTempDirectory("sdm-security-verifier-");
        Path file = directory.resolve("trusted.json");
        TrustedCommands commands = new TrustedCommands(file, java.util.Set.of(trusted));
        require(commands.contains(trusted), "immutable official approvals");
        String extra = commands.approve("/another {player}", true, true);
        require(commands.contains(extra), "explicit custom approval");
        commands = new TrustedCommands(file, java.util.Set.of(trusted));
        require(commands.contains(extra), "approval survives restart");
        require(commands.revoke(extra), "custom approval revocation");
        require(!new TrustedCommands(file, java.util.Set.of(trusted)).contains(extra), "revocation survives restart");
        require(!commands.revoke(trusted) && commands.contains(trusted), "custom store does not overwrite official catalog");
        Files.writeString(file, "{\"schemaVersion\":99,\"commands\":[]}");
        boolean failed = false;
        try {
            new TrustedCommands(file, java.util.Set.of(trusted));
        } catch (IllegalArgumentException exception) {
            failed = true;
        }
        require(failed, "corrupted approval file fails closed");
    }

    private static void rejects(Runnable action, String label) {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        require(rejected, label);
    }

    private static JsonObject fixture() {
        return JsonParser.parseString("{\"shopName\":\"审计测试\",\"shopTabs\":[{\"shopTabUUID\":[0,0,0,1],\"title\":\"商品\",\"tabEntry\":[{\"entryUUID\":[0,0,0,2],\"entryPrice\":10,\"entryCount\":1}]}]}").getAsJsonObject();
    }

    private static boolean kind(JsonObject diff, String expected) {
        for (var change : diff.getAsJsonArray("changes")) {
            if (change.getAsJsonObject().get("kind").getAsString().equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject record(int sequence) {
        JsonObject record = new JsonObject();
        record.addProperty("time", "2026-10-12T22:00:00+08:00[Asia/Shanghai]");
        record.addProperty("event", "verification");
        record.addProperty("sequence", sequence);
        return record;
    }

    private static void require(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
