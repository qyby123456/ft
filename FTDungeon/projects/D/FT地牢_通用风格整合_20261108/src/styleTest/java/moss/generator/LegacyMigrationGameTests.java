package moss.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstylesqa")
@PrefixGameTestTemplate(false)
public final class LegacyMigrationGameTests {
    private static final String SWITCH = "ftmossgen.allowLegacy025";
    private static final String FIXTURE = "/data/ftstylesqa/fixtures/legacy025-ftmossgen_instances.dat";
    private static final String FIXTURE_SHA = "ff5924bdba723960bd481eb9b0e20c3539f8e1b5f89e2468421437aa1d7fcb6f";
    private static final String OLD_FINGERPRINT = "370f21ea734326e54f14688e7530ac9c86db2be045883615354c34d7c491be66";
    private static final String FOREST = "ftmossgen:forest";

    private static String payload(CompoundTag tag) {
        if (!tag.contains("payloadParts", Tag.TAG_LIST)) return tag.getString("payload");
        StringBuilder joined = new StringBuilder();
        var parts = tag.getList("payloadParts", Tag.TAG_STRING);
        for (int index = 0; index < parts.size(); index++) joined.append(parts.getString(index));
        return joined.toString();
    }

    private static JsonObject snapshot(CompoundTag tag) {
        return JsonParser.parseString(payload(tag)).getAsJsonObject();
    }

    private static CompoundTag encode(CompoundTag original, JsonObject snapshot) {
        CompoundTag changed = original.copy();
        String json = snapshot.toString();
        changed.remove("payloadParts");
        changed.putString("payload", json);
        changed.putString("payloadHash", Catalog.hash(json.getBytes(StandardCharsets.UTF_8)));
        return changed;
    }

    private static JsonObject firstJob(JsonObject snapshot) {
        return snapshot.getAsJsonObject("jobs").entrySet().iterator().next().getValue().getAsJsonObject();
    }

    private static void rejected(GameTestHelper helper, CompoundTag tag, String reason) {
        CompoundTag before = tag.copy();
        boolean failed = false;
        try {
            Store.load(tag);
        } catch (IllegalStateException | IllegalArgumentException expected) {
            failed = true;
        }
        helper.assertTrue(failed, "Legacy migration must fail closed: " + reason);
        helper.assertTrue(tag.equals(before), "Rejected migration leaves original NBT unchanged: " + reason);
    }

    private static void rejectChanged(GameTestHelper helper, CompoundTag original, String reason, Consumer<JsonObject> change) {
        JsonObject changed = snapshot(original);
        change.accept(changed);
        rejected(helper, encode(original, changed), reason);
    }

    private static Map<String, String> diskState(Path folder) throws Exception {
        Map<String, String> state = new LinkedHashMap<>();
        if (!Files.isDirectory(folder)) return state;
        try (var paths = Files.list(folder)) {
            for (Path path : paths.filter(path -> path.getFileName().toString().startsWith("ftmossgen_instances.dat")).sorted().toList()) {
                state.put(path.getFileName().toString(), Catalog.hash(Files.readAllBytes(path)));
            }
        }
        return state;
    }

    private static JsonObject preservedJobs(JsonObject snapshot, boolean normalizeLegacyTypes) {
        JsonObject jobs = snapshot.getAsJsonObject("jobs").deepCopy();
        for (var entry : jobs.entrySet()) {
            var job = entry.getValue().getAsJsonObject();
            job.remove("planHash");
            job.remove("legacy025");
            var plan = job.getAsJsonObject("plan");
            plan.remove("style");
            plan.remove("fingerprint");
            for (JsonElement piece : plan.getAsJsonArray("pieces")) piece.getAsJsonObject().remove("style");
            if (job.has("gameplay") && !job.get("gameplay").isJsonNull()) {
                var gameplay = job.getAsJsonObject("gameplay");
                gameplay.remove("style");
                if (normalizeLegacyTypes) {
                    Map<String, String> types = Map.of("zombie", "minecraft:zombie", "skeleton", "minecraft:skeleton",
                        "wither_skeleton", "minecraft:wither_skeleton", "villager", "minecraft:villager");
                    for (var intent : gameplay.getAsJsonObject("intents").entrySet()) {
                        var data = intent.getValue().getAsJsonObject();
                        String type = data.get("type").getAsString();
                        if (types.containsKey(type)) data.addProperty("type", types.get(type));
                    }
                }
            }
        }
        return jobs;
    }

    private static void migrated(GameTestHelper helper, JsonObject legacy, Store migrated) {
        JsonObject saved = snapshot(migrated.save(new CompoundTag()));
        helper.assertTrue(legacy.getAsJsonObject("jobs").keySet().equals(saved.getAsJsonObject("jobs").keySet()), "Legacy instance IDs stay exact; migration does not allocate replacement plans");
        helper.assertTrue(preservedJobs(legacy, true).equals(preservedJobs(saved, false)), "All legacy IDs, UUIDs, cursors, stages, portal geometry and combat fields survive except explicit legacy025/style/fingerprint/planHash and entity type alias conversion");
        helper.assertTrue(!Catalog.get(FOREST).fingerprint.equals(OLD_FINGERPRINT), "Generic style fingerprint differs from actual legacy catalog fingerprint");
        for (var entry : migrated.jobs.entrySet()) {
            var job = entry.getValue();
            helper.assertTrue(job.legacy025, "Every real 0.25 migrated job retains explicit legacy origin for guarded portal compatibility");
            String oldId = Catalog.hash(("forest-v1:" + job.plan.seed + ":" + Catalog.pos(job.plan.anchor).asLong() + ":" + OLD_FINGERPRINT)
                .getBytes(StandardCharsets.UTF_8)).substring(0, 16);
            helper.assertTrue(entry.getKey().equals(oldId) && job.plan.id.equals(oldId), "Migrated legacy instance ID matches frozen original 0.25 algorithm");
            helper.assertTrue(FOREST.equals(job.plan.style) && Catalog.get(FOREST).fingerprint.equals(job.plan.fingerprint), "Migrated plan freezes current forest style and fingerprint");
            for (var piece : job.plan.pieces) helper.assertTrue(FOREST.equals(piece.style), "Every migrated piece explicitly owns forest style");
            if (job.gameplay != null) helper.assertTrue(FOREST.equals(job.gameplay.style), "Migrated combat owns forest style");
            helper.assertTrue(job.planHash.equals(Catalog.hash(Catalog.GSON.toJson(job.plan).getBytes(StandardCharsets.UTF_8))), "Migrated frozen plan hash matches exact new style-bearing plan");
            Engine.validatePlan(job.plan);
            Engine.validateFrozen(job);
            Engine.validateCursors(job);
        }
    }

    private static void ordinary026(GameTestHelper helper, Store migrated) {
        Engine.Job ordinary = new Engine.Job();
        helper.assertTrue(!ordinary.legacy025, "Ordinary 0.26 job defaults to nonlegacy");
        ordinary.plan = Catalog.GSON.fromJson(Catalog.GSON.toJson(migrated.jobs.values().iterator().next().plan), Planner.Plan.class);
        ordinary.plan.id = Catalog.hash(("forest-v1:" + ordinary.plan.seed + ":" + Catalog.pos(ordinary.plan.anchor).asLong()
            + ":" + ordinary.plan.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
        ordinary.planHash = Catalog.hash(Catalog.GSON.toJson(ordinary.plan).getBytes(StandardCharsets.UTF_8));
        Store current = new Store();
        current.jobs.put(ordinary.plan.id, ordinary);
        CompoundTag tag = current.save(new CompoundTag());
        for (String enabled : List.of("true", "false")) {
            System.setProperty(SWITCH, enabled);
            Store reloaded = Store.load(tag);
            helper.assertTrue(!reloaded.migratedLegacy025() && !reloaded.jobs.get(ordinary.plan.id).legacy025
                && snapshot(tag).equals(snapshot(reloaded.save(new CompoundTag()))), "Ordinary 0.26 job stays nonlegacy and lossless for migration switch=" + enabled);
        }
        System.clearProperty(SWITCH);
        helper.assertTrue(!Store.load(tag).jobs.get(ordinary.plan.id).legacy025, "Ordinary 0.26 job remains nonlegacy with migration switch absent");
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void realLegacy025MigrationIsOptInLosslessIdempotentAndFailClosed(GameTestHelper helper) throws Exception {
        String previous = System.getProperty(SWITCH);
        Path liveData = DimensionType.getStorageFolder(MossMod.DIMENSION, helper.getLevel().getServer().getWorldPath(LevelResource.ROOT)).resolve("data");
        Map<String, String> diskBefore = diskState(liveData);
        try (InputStream stream = LegacyMigrationGameTests.class.getResourceAsStream(FIXTURE)) {
            helper.assertTrue(stream != null, "Actual installed 0.25 .dat fixture must exist on test classpath");
            byte[] bytes = stream.readAllBytes();
            helper.assertTrue(FIXTURE_SHA.equals(Catalog.hash(bytes)), "Fixture is byte-identical to actual installed legacy .dat, never synthesized");
            CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            CompoundTag untouchedRoot = root.copy();
            CompoundTag legacy = root.getCompound("data");
            JsonObject old = snapshot(legacy);
            helper.assertTrue(OLD_FINGERPRINT.equals(old.get("fingerprint").getAsString())
                && old.getAsJsonObject("jobs").has("fdccfb1b7f58340e")
                && !firstJob(old).getAsJsonObject("plan").has("style"), "Fixture contains actual old catalog, installed instance ID and style-less plan");
            System.clearProperty(SWITCH);
            rejected(helper, legacy, "property absent by default");
            System.setProperty(SWITCH, "false");
            rejected(helper, legacy, "explicit false");
            System.setProperty(SWITCH, "true");
            Store converted = Store.load(legacy);
            helper.assertTrue(converted.migratedLegacy025(), "Store flags pure-memory conversion from real legacy 0.25");
            migrated(helper, old, converted);
            helper.assertTrue(root.equals(untouchedRoot), "Successful pure-memory migration never rewrites input NBT");
            CompoundTag generic = converted.save(new CompoundTag());
            CompoundTag genericBefore = generic.copy();
            helper.assertTrue(snapshot(generic).equals(snapshot(Store.load(legacy).save(new CompoundTag()))), "Repeated legacy conversion is deterministic");
            for (String enabled : List.of("true", "false")) {
                System.setProperty(SWITCH, enabled);
                Store roundtrip = Store.load(generic);
                helper.assertTrue(!roundtrip.migratedLegacy025(), "Generic Store reload is never marked as another legacy migration");
                for (var job : roundtrip.jobs.values()) helper.assertTrue(job.legacy025, "Persisted migrated job keeps legacy origin without redoing migration");
                helper.assertTrue(snapshot(generic).equals(snapshot(roundtrip.save(new CompoundTag()))), "Entire generic 0.26 Store roundtrip is lossless and switch-independent: " + enabled);
            }
            System.clearProperty(SWITCH);
            helper.assertTrue(snapshot(generic).equals(snapshot(Store.load(generic).save(new CompoundTag()))), "Generic 0.26 data remains loadable with property absent");
            helper.assertTrue(generic.equals(genericBefore), "Generic reload does not mutate source NBT");
            ordinary026(helper, converted);
            System.setProperty(SWITCH, "true");
            CompoundTag badHash = legacy.copy();
            badHash.putString("payloadHash", "corrupt");
            rejected(helper, badHash, "raw payload hash mismatch");
            CompoundTag badJson = legacy.copy();
            badJson.remove("payloadParts");
            badJson.putString("payload", "{broken");
            badJson.putString("payloadHash", Catalog.hash("{broken".getBytes(StandardCharsets.UTF_8)));
            rejected(helper, badJson, "malformed payload with valid outer hash");
            rejectChanged(helper, legacy, "wrong frozen legacy planHash", changed -> firstJob(changed).addProperty("planHash", "corrupt"));
            rejectChanged(helper, legacy, "unknown legacy snapshot catalog", changed -> changed.addProperty("fingerprint", "unknown-catalog"));
            rejectChanged(helper, legacy, "legacy plan catalog mismatch", changed -> firstJob(changed).getAsJsonObject("plan").addProperty("fingerprint", "unknown-catalog"));
            rejectChanged(helper, legacy, "partial plan style", changed -> firstJob(changed).getAsJsonObject("plan").addProperty("style", FOREST));
            rejectChanged(helper, legacy, "partial piece style", changed -> firstJob(changed).getAsJsonObject("plan").getAsJsonArray("pieces").get(0).getAsJsonObject().addProperty("style", FOREST));
            rejectChanged(helper, legacy, "partial combat style", changed -> firstJob(changed).getAsJsonObject("gameplay").addProperty("style", FOREST));
            rejectChanged(helper, legacy, "null style is not an absent legacy field", changed -> firstJob(changed).getAsJsonObject("plan").add("style", com.google.gson.JsonNull.INSTANCE));
            rejectChanged(helper, legacy, "corrupt job map", changed -> changed.add("jobs", com.google.gson.JsonNull.INSTANCE));
            helper.assertTrue(root.equals(untouchedRoot), "All detached migration and rejection cases leave real source root unchanged");
            helper.assertTrue(diskBefore.equals(diskState(liveData)), "Pure-memory success/failure paths never create or alter actual server Store .dat, pending or backup files");
            System.out.println("STYLE_LEGACY_MIGRATION_PASS:real025dat;default opt-out;lossless IDs/UUIDs/cursors/portal/combat;new style fingerprint/hash;legacy025 origin+old ID;ordinary026 nonlegacy;generic roundtrip;idempotent;corrupt/mixed-style failclosed;no live Store access");
            helper.succeed();
        } finally {
            if (previous == null) System.clearProperty(SWITCH); else System.setProperty(SWITCH, previous);
        }
    }
}
