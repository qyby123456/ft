package cn.farmingtales.sdmshopaudit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

public final class AuditWriter implements AutoCloseable {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    private final Path directory;
    private final ThreadPoolExecutor worker;
    private final Object fileLock = new Object();
    private final AtomicLong written = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private volatile boolean accepting = true;

    public AuditWriter(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(this.directory.resolve("snapshots"));
        worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024), task -> {
            Thread thread = new Thread(task, "SDMShop-Audit-Writer");
            thread.setDaemon(true);
            return thread;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void emit(String category, JsonObject record, List<ShopSnapshot> snapshots, String summary) {
        if (!accepting) {
            failures.incrementAndGet();
            AuditMod.LOGGER.error("SDM audit writer is closed; event {} was not written", record);
            return;
        }
        worker.execute(() -> {
            synchronized (fileLock) {
                JsonArray snapshotErrors = new JsonArray();
                for (ShopSnapshot snapshot : snapshots) {
                    try {
                        saveSnapshot(snapshot);
                    } catch (IOException | RuntimeException exception) {
                        failures.incrementAndGet();
                        JsonObject error = new JsonObject();
                        error.addProperty("hash", snapshot.hash());
                        error.addProperty("error", exception.toString());
                        snapshotErrors.add(error);
                        AuditMod.LOGGER.error("Cannot save SDM audit snapshot {}", snapshot.hash(), exception);
                    }
                }
                if (!snapshotErrors.isEmpty()) {
                    record.add("snapshotErrors", snapshotErrors);
                }
                try {
                    String time = record.get("time").getAsString();
                    String day = time.substring(0, 10);
                    Files.writeString(directory.resolve(day + "-" + category + ".jsonl"),
                            GSON.toJson(record) + "\n", StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    Files.writeString(directory.resolve(day + ".log"),
                            time + " [" + record.get("event").getAsString() + "] " + singleLine(summary) + "\n",
                            StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    written.incrementAndGet();
                } catch (IOException | RuntimeException exception) {
                    failures.incrementAndGet();
                    AuditMod.LOGGER.error("Cannot write SDM audit event {}", record, exception);
                }
            }
        });
    }

    private void saveSnapshot(ShopSnapshot snapshot) throws IOException {
        Path target = directory.resolve("snapshots").resolve(snapshot.hash() + ".snbt.gz");
        if (Files.exists(target)) {
            return;
        }
        Path temporary = Files.createTempFile(directory.resolve("snapshots"), "snapshot-", ".tmp");
        try {
            try (GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(temporary))) {
                gzip.write(snapshot.snbt().getBytes(StandardCharsets.UTF_8));
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void flush() throws Exception {
        worker.submit(() -> { }).get(30, TimeUnit.SECONDS);
    }

    public Path directory() {
        return directory;
    }

    public long written() {
        return written.get();
    }

    public long failures() {
        return failures.get();
    }

    public int queued() {
        return worker.getQueue().size();
    }

    public static String singleLine(String value) {
        return value.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t")
                .replace("\u001b", "\\u001b");
    }

    @Override
    public void close() {
        accepting = false;
        worker.shutdown();
        try {
            if (!worker.awaitTermination(30, TimeUnit.SECONDS)) {
                AuditMod.LOGGER.error("SDM audit shutdown timed out; {} records remain queued", queued());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            AuditMod.LOGGER.error("SDM audit shutdown interrupted", exception);
        }
    }
}
