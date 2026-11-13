package moss.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class Store extends SavedData {
    private static final int SCHEMA = 1;
    private static final String NAME = "ftmossgen_instances";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Set<String> STAGES = Set.of("PLANNED", "CHECKING", "BUILDING", "INITIALIZING", "COMPLETE", "PAUSED", "FAILED");
    public final LinkedHashMap<String, Engine.Job> jobs = new LinkedHashMap<>();
    private static final Map<DimensionDataStorage, Store> LOADED = new WeakHashMap<>();
    private boolean legacy025;
    private boolean requireAtomic;

    public boolean migratedLegacy025() { return legacy025; }

    public static Store get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        Store existing = LOADED.get(storage);
        if (existing != null) return existing;
        Path file = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
            .resolve("data").resolve(NAME + ".dat");
        Store store;
        try {
            rejectSymbolicPath(file);
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                requireRegularSource(file);
                byte[] original = Files.readAllBytes(file);
                CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(original));
                if (!root.contains("data", 10)) throw new IllegalStateException("Missing Moss saved-data payload");
                store = load(root.getCompound("data"));
                if (store.legacy025) {
                    if (Files.exists(file.resolveSibling(file.getFileName() + ".pending"), LinkOption.NOFOLLOW_LINKS)) {
                        throw new IllegalStateException("Interrupted checkpoint exists; refusing legacy migration without touching source or pending");
                    }
                    requireRegularSource(file);
                    backupLegacy025(file, original);
                    if (!Arrays.equals(original, Files.readAllBytes(file))) throw new IllegalStateException("Legacy source changed during validation");
                    store.requireAtomic = true;
                    store.setDirty();
                    store.save(file.toFile());
                    store.requireAtomic = false;
                }
            } else {
                if (Files.exists(file.resolveSibling(file.getFileName() + ".pending"), LinkOption.NOFOLLOW_LINKS)) {
                    throw new IllegalStateException("Interrupted Moss checkpoint exists; refusing empty replacement");
                }
                store = new Store();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unreadable Moss data; refusing empty replacement", exception);
        }
        storage.set(NAME, store);
        LOADED.put(storage, store);
        return store;
    }

    public static Store load(CompoundTag tag) {
        if (tag.getInt("schema") != SCHEMA || !tag.contains("payload", 8)) {
            throw new IllegalStateException("Unsupported or corrupt Moss saved-data schema");
        }
        String payload = tag.getString("payload");
        if (tag.contains("payloadParts", 9)) {
            if (!payload.isEmpty()) throw new IllegalStateException("Ambiguous Moss payload encoding");
            ListTag parts = tag.getList("payloadParts", 8);
            if (parts.isEmpty()) throw new IllegalStateException("Missing Moss payload parts");
            StringBuilder joined = new StringBuilder();
            for (int index = 0; index < parts.size(); index++) joined.append(parts.getString(index));
            payload = joined.toString();
        }
        if (!tag.getString("payloadHash").equals(Catalog.hash(payload.getBytes(StandardCharsets.UTF_8)))) {
            throw new IllegalStateException("Corrupt Moss saved-data payload hash");
        }
        try {
            JsonObject raw = JsonParser.parseString(payload).getAsJsonObject();
            boolean migrated = LegacyForest.prepare(raw);
            Snapshot snapshot = GSON.fromJson(raw, Snapshot.class);
            if (migrated) LegacyForest.validateRoundTrip(raw, GSON.toJsonTree(snapshot));
            if (snapshot == null || snapshot.schema != SCHEMA || snapshot.jobs == null) {
                throw new IllegalStateException("Moss saved-data catalog/schema mismatch");
            }
            Store store = new Store();
            store.legacy025 = migrated;
            int active = 0;
            for (Map.Entry<String, Engine.Job> entry : snapshot.jobs.entrySet()) {
                Engine.Job job = entry.getValue();
                if (job == null || job.plan == null || !entry.getKey().equals(job.plan.id)
                    || !Catalog.get(job.plan.style).fingerprint.equals(job.plan.fingerprint) || job.plan.version != 1
                    || !STAGES.contains(job.stage) || job.pieceCursor < 0 || job.cellCursor < 0
                    || job.writeCursor < 0 || job.checkPiece < 0 || job.checkCell < 0 || job.engineVersion != 1
                    || job.pendingStart < -1 || job.pendingEnd < 0
                    || (job.pendingStart >= 0 && (job.pendingStart != job.writeCursor || job.pendingEnd <= job.pendingStart))) {
                    throw new IllegalStateException("Corrupt Moss job " + entry.getKey());
                }
                Engine.validatePlan(job.plan);
                Engine.validateFrozen(job);
                Engine.validateCursors(job);
                if (!migrated && "CHECKING".equals(job.stage)) { job.checkPiece = 0; job.checkCell = 0; }
                if (Engine.active(job)) active++;
                store.jobs.put(entry.getKey(), job);
            }
            if (active > 1) throw new IllegalStateException("Multiple active Moss jobs in saved data");
            Engine.validateReservations(store);
            return store;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Refusing corrupt Moss saved data; original data was not reset", exception);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        Snapshot snapshot = new Snapshot();
        snapshot.schema = SCHEMA;
        snapshot.fingerprint = "per-instance-style-fingerprints";
        snapshot.jobs = jobs;
        tag.putInt("schema", SCHEMA);
        String payload = GSON.toJson(snapshot);
        tag.putString("payload", payload);
        tag.putString("payloadHash", Catalog.hash(payload.getBytes(StandardCharsets.UTF_8)));
        return tag;
    }

    public static void flush(ServerLevel level) {
        Store store = get(level);
        store.setDirty();
        Path file = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
                .resolve("data").resolve(NAME + ".dat");
        store.save(file.toFile());
    }

    @Override
    public void save(File file) {
        if (!isDirty()) return;
        Path target = file.toPath();
        Path temporary = target.resolveSibling(target.getFileName() + ".pending");
        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            CompoundTag root = new CompoundTag();
            CompoundTag data = save(new CompoundTag());
            String payload = data.getString("payload");
            if (payload.length() > 16000) {
                ListTag parts = new ListTag();
                for (int offset = 0; offset < payload.length(); offset += 16000) {
                    parts.add(StringTag.valueOf(payload.substring(offset, Math.min(offset + 16000, payload.length()))));
                }
                data.putString("payload", "");
                data.put("payloadParts", parts);
            }
            root.put("data", data);
            root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            if (requireAtomic) {
                requireRegularSource(target);
                try (var output = Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
                    NbtIo.writeCompressed(root, output);
                }
            } else {
                NbtIo.writeCompressed(root, temporary.toFile());
            }
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) { channel.force(true); }
            try {
                if (requireAtomic) requireRegularSource(target);
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                if (requireAtomic) throw new IOException("Legacy migration requires atomic replacement", exception);
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            setDirty(false);
        } catch (IOException exception) {
            throw new IllegalStateException("Moss checkpoint failed; refusing further writes", exception);
        }
    }

    public static Path legacyBackupPath(Path source, String sha256) {
        if (sha256 == null || !sha256.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid original file SHA256");
        return source.resolveSibling(source.getFileName() + ".legacy025." + sha256 + ".bak");
    }

    private static void rejectSymbolicPath(Path source) {
        for (Path current = source.toAbsolutePath().normalize(); current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IllegalStateException("Symbolic source/parent path refused: " + current);
        }
    }

    private static void requireRegularSource(Path source) {
        rejectSymbolicPath(source);
        if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("Saved-data source must be a regular non-symbolic file: " + source);
        }
    }

    private static void backupLegacy025(Path source, byte[] original) throws IOException {
        String hash = Catalog.hash(original);
        Path backup = legacyBackupPath(source, hash);
        try (FileChannel channel = FileChannel.open(backup, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(original);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        } catch (java.nio.file.FileAlreadyExistsException exception) {
            if (!Files.isRegularFile(backup, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Legacy backup is not a regular immutable backup", exception);
            }
        }
        byte[] saved = Files.readAllBytes(backup);
        if (!Arrays.equals(original, saved) || !hash.equals(Catalog.hash(saved))) {
            throw new IOException("Legacy backup contents do not match original; refusing replacement");
        }
    }

    private static final class Snapshot {
        int schema;
        String fingerprint;
        LinkedHashMap<String, Engine.Job> jobs;
    }
}
