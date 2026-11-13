package eclipse.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
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
    private static final String NAME = "fteclipsegen_instances";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Set<String> STAGES = Set.of("PLANNED", "CHECKING", "BUILDING", "INITIALIZING", "COMPLETE", "PAUSED", "FAILED");
    public final LinkedHashMap<String, Engine.Job> jobs = new LinkedHashMap<>();
    private static final Map<DimensionDataStorage, Store> LOADED = new WeakHashMap<>();

    public static Store get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        Store existing = LOADED.get(storage);
        if (existing != null) return existing;
        Path file = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT))
            .resolve("data").resolve(NAME + ".dat");
        Store store;
        try {
            if (Files.exists(file)) {
                CompoundTag root = NbtIo.readCompressed(file.toFile());
                if (!root.contains("data", 10)) throw new IllegalStateException("Missing Eclipse saved-data payload");
                store = load(root.getCompound("data"));
            } else {
                if (Files.exists(file.resolveSibling(file.getFileName() + ".pending"))) {
                    throw new IllegalStateException("Interrupted Eclipse checkpoint exists; refusing empty replacement");
                }
                store = new Store();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unreadable Eclipse data; refusing empty replacement", exception);
        }
        storage.set(NAME, store);
        LOADED.put(storage, store);
        return store;
    }

    public static Store load(CompoundTag tag) {
        if (tag.getInt("schema") != SCHEMA || !tag.contains("payload", 8)) {
            throw new IllegalStateException("Unsupported or corrupt Eclipse saved-data schema");
        }
        String payload = tag.getString("payload");
        if (tag.contains("payloadParts", 9)) {
            if (!payload.isEmpty()) throw new IllegalStateException("Ambiguous Eclipse payload encoding");
            ListTag parts = tag.getList("payloadParts", 8);
            if (parts.isEmpty()) throw new IllegalStateException("Missing Eclipse payload parts");
            StringBuilder joined = new StringBuilder();
            for (int index = 0; index < parts.size(); index++) joined.append(parts.getString(index));
            payload = joined.toString();
        }
        if (!tag.getString("payloadHash").equals(Catalog.hash(payload.getBytes(StandardCharsets.UTF_8)))) {
            throw new IllegalStateException("Corrupt Eclipse saved-data payload hash");
        }
        try {
            var raw = JsonParser.parseString(payload).getAsJsonObject();
            for (var entry : raw.getAsJsonObject("jobs").entrySet()) {
                var plan = entry.getValue().getAsJsonObject().getAsJsonObject("plan");
                boolean styled = plan.has("style");
                if (styled != plan.has("styleFingerprint") || styled && (!plan.get("style").isJsonPrimitive()
                    || !plan.getAsJsonPrimitive("style").isString() || !plan.get("styleFingerprint").isJsonPrimitive()
                    || !plan.getAsJsonPrimitive("styleFingerprint").isString())) throw new IllegalStateException("Partial Eclipse style fields");
            }
            Snapshot snapshot = GSON.fromJson(payload, Snapshot.class);
            if (snapshot == null || snapshot.schema != SCHEMA || snapshot.jobs == null
                || !Catalog.get().fingerprint.equals(snapshot.fingerprint)) {
                throw new IllegalStateException("Eclipse saved-data catalog/schema mismatch");
            }
            Store store = new Store();
            int active = 0;
            for (Map.Entry<String, Engine.Job> entry : snapshot.jobs.entrySet()) {
                Engine.Job job = entry.getValue();
                if (job == null || job.plan == null || !entry.getKey().equals(job.plan.id)
                    || !snapshot.fingerprint.equals(job.plan.fingerprint) || job.plan.version != 1
                    || !STAGES.contains(job.stage) || job.pieceCursor < 0 || job.cellCursor < 0
                    || job.writeCursor < 0 || job.checkPiece < 0 || job.checkCell < 0 || job.engineVersion != 1
                    || job.pendingStart < -1 || job.pendingEnd < 0
                    || (job.pendingStart >= 0 && (job.pendingStart != job.writeCursor || job.pendingEnd <= job.pendingStart))) {
                    throw new IllegalStateException("Corrupt Eclipse job " + entry.getKey());
                }
                Engine.validatePlan(job.plan);
                Engine.validateFrozen(job);
                Engine.validateCursors(job);
                if ("CHECKING".equals(job.stage)) { job.checkPiece = 0; job.checkCell = 0; }
                if (Engine.active(job)) active++;
                store.jobs.put(entry.getKey(), job);
            }
            if (active > 1) throw new IllegalStateException("Multiple active Eclipse jobs in saved data");
            Engine.validateReservations(store);
            return store;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Refusing corrupt Eclipse saved data; original data was not reset", exception);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        Snapshot snapshot = new Snapshot();
        snapshot.schema = SCHEMA;
        snapshot.fingerprint = Catalog.get().fingerprint;
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
            NbtIo.writeCompressed(root, temporary.toFile());
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) { channel.force(true); }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            setDirty(false);
        } catch (IOException exception) {
            throw new IllegalStateException("Eclipse checkpoint failed; refusing further writes", exception);
        }
    }

    private static final class Snapshot {
        int schema;
        String fingerprint;
        LinkedHashMap<String, Engine.Job> jobs;
    }
}
