package moss.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LegacyForest {
    public static final String PROPERTY = "ftmossgen.allowLegacy025";
    public static final String RAW_CATALOG_SHA256 = "370f21ea734326e54f14688e7530ac9c86db2be045883615354c34d7c491be66";
    private static final String FOREST = "ftmossgen:forest";
    private static final String CURRENT = "per-instance-style-fingerprints";
    private static final Map<String, String> TYPES = Map.of("zombie", "minecraft:zombie", "skeleton", "minecraft:skeleton",
        "wither_skeleton", "minecraft:wither_skeleton", "villager", "minecraft:villager");

    private LegacyForest() {}

    public static boolean enabled() { return "true".equals(System.getProperty(PROPERTY)); }

    public static boolean prepare(JsonObject snapshot) {
        require(snapshot != null && snapshot.has("schema") && snapshot.get("schema").getAsInt() == 1, "Invalid snapshot schema");
        String fingerprint = string(snapshot, "fingerprint");
        JsonObject jobs = object(snapshot, "jobs");
        if (CURRENT.equals(fingerprint)) {
            validateCurrent(jobs);
            return false;
        }
        require(RAW_CATALOG_SHA256.equals(fingerprint), "Unknown legacy catalog; refusing migration");
        noStyleFields(snapshot);
        require(enabled(), "Legacy 0.25 forest data requires explicit -D" + PROPERTY + "=true; original data retained");
        require(snapshot.keySet().equals(Set.of("schema", "fingerprint", "jobs")), "Unknown legacy snapshot fields");
        for (Map.Entry<String, JsonElement> entry : jobs.entrySet()) verifyLegacyJob(entry.getKey(), entry.getValue().getAsJsonObject());
        validateForestContract();
        String fingerprintNow = Catalog.get(FOREST).fingerprint;
        for (Map.Entry<String, JsonElement> entry : jobs.entrySet()) {
            JsonObject job = entry.getValue().getAsJsonObject();
            JsonObject plan = object(job, "plan");
            job.addProperty("legacy025", true);
            plan.addProperty("style", FOREST);
            plan.addProperty("fingerprint", fingerprintNow);
            for (JsonElement element : array(plan, "pieces")) element.getAsJsonObject().addProperty("style", FOREST);
            if (job.has("gameplay") && !job.get("gameplay").isJsonNull()) {
                JsonObject gameplay = object(job, "gameplay");
                gameplay.addProperty("style", FOREST);
                for (Map.Entry<String, JsonElement> intentEntry : object(gameplay, "intents").entrySet()) {
                    JsonObject intent = intentEntry.getValue().getAsJsonObject();
                    intent.addProperty("type", TYPES.get(string(intent, "type")));
                }
            }
            Planner.Plan typed = Catalog.GSON.fromJson(plan, Planner.Plan.class);
            job.addProperty("planHash", hash(Catalog.GSON.toJson(typed)));
        }
        snapshot.addProperty("fingerprint", CURRENT);
        validateCurrent(jobs);
        return true;
    }

    private static void validateForestContract() {
        Profiles.Style style = Profiles.get(FOREST);
        require(FOREST.equals(style.id) && "ftmossgen:catalog".equals(style.catalog)
            && style.palette != null && style.palette.isEmpty() && style.decorations != null && style.decorations.isEmpty()
            && style.pools != null && style.pools.isEmpty(), "Legacy forest requires unchanged catalog and empty palette/decorations/pools");
        require("minecraft:mossy_stone_bricks".equals(style.capBlock)
            && "minecraft:green_stained_glass".equals(style.barrierBlock), "Legacy forest cap/barrier contract changed");
        require(RAW_CATALOG_SHA256.equals(Catalog.hash(Profiles.bytes(style.catalog, ".json"))), "Frozen legacy raw catalog mismatch");
        for (String key : Catalog.get(FOREST).templates.keySet()) Catalog.blocks(FOREST, key);
    }

    private static void verifyLegacyJob(String identifier, JsonObject job) {
        require(!job.has("legacy025"), "Original 0.25 payload must not contain migration markers");
        known(job, Engine.Job.class);
        for (String field : Set.of("stage", "pieceCursor", "cellCursor", "writeCursor", "checkPiece", "checkCell",
            "pendingStart", "pendingEnd", "engineVersion", "planHash")) require(job.has(field) && !job.get(field).isJsonNull(), "Missing legacy job field " + field);
        JsonObject plan = object(job, "plan");
        known(plan, Planner.Plan.class);
        require(identifier.equals(string(plan, "id")), "Legacy instance ownership mismatch");
        require(RAW_CATALOG_SHA256.equals(string(plan, "fingerprint")), "Mixed legacy/current plan fingerprints");
        require(string(job, "planHash").equals(hash(Catalog.GSON.toJson(plan))), "Original legacy planHash mismatch");
        for (JsonElement element : array(plan, "pieces")) known(element.getAsJsonObject(), Planner.Piece.class);
        if (job.has("gameplay") && !job.get("gameplay").isJsonNull()) {
            JsonObject gameplay = object(job, "gameplay");
            known(gameplay, Gameplay.State.class);
            require(identifier.equals(string(gameplay, "instanceId")) && gameplay.get("version").getAsInt() == 1,
                "Invalid legacy gameplay ownership/version");
            require(Set.of("READY", "FIGHTING", "LOCKED", "VICTORY").contains(string(gameplay, "bossState")), "Unknown legacy Boss state");
            for (Map.Entry<String, JsonElement> entry : object(gameplay, "intents").entrySet()) {
                JsonObject intent = entry.getValue().getAsJsonObject();
                known(intent, Gameplay.Intent.class);
                require(entry.getKey().equals(string(intent, "uuid")), "Legacy intent UUID ownership mismatch");
                java.util.UUID.fromString(string(intent, "uuid"));
                require(TYPES.containsKey(string(intent, "type")), "Unknown or already namespaced legacy intent type");
            }
        }
    }

    private static void validateCurrent(JsonObject jobs) {
        for (Map.Entry<String, JsonElement> entry : jobs.entrySet()) {
            JsonObject job = entry.getValue().getAsJsonObject();
            JsonObject plan = object(job, "plan");
            String style = string(plan, "style");
            require(Profiles.get(style).id.equals(style), "Unknown current style");
            if (job.has("legacy025")) {
                JsonElement marker = job.get("legacy025");
                require(marker.isJsonPrimitive() && marker.getAsJsonPrimitive().isBoolean(), "Invalid legacy migration marker");
                if (marker.getAsBoolean()) {
                    require(FOREST.equals(style), "Legacy marker only permitted for forest");
                    int[] anchor = Catalog.GSON.fromJson(array(plan, "anchor"), int[].class);
                    require(anchor.length == 3 && plan.has("seed"), "Invalid legacy instance identity inputs");
                    String expected = hash("forest-v1:" + plan.get("seed").getAsLong() + ":"
                        + Catalog.pos(anchor).asLong() + ":" + RAW_CATALOG_SHA256).substring(0, 16);
                    require(expected.equals(string(plan, "id")) && expected.equals(entry.getKey()), "Legacy marked instance ID does not match frozen 0.25 algorithm");
                }
            }
            for (JsonElement element : array(plan, "pieces")) require(style.equals(string(element.getAsJsonObject(), "style")), "Partial/mixed piece styles");
            if (job.has("gameplay") && !job.get("gameplay").isJsonNull()) {
                require(style.equals(string(object(job, "gameplay"), "style")), "Partial/mixed gameplay style");
            }
        }
    }

    static void validateRoundTrip(JsonElement raw, JsonElement typed) {
        if (raw.isJsonObject()) {
            require(typed != null && typed.isJsonObject(), "Legacy object lost during typed conversion");
            for (Map.Entry<String, JsonElement> field : raw.getAsJsonObject().entrySet()) {
                require(typed.getAsJsonObject().has(field.getKey()), "Legacy field lost during conversion: " + field.getKey());
                validateRoundTrip(field.getValue(), typed.getAsJsonObject().get(field.getKey()));
            }
        } else if (raw.isJsonArray()) {
            require(typed != null && typed.isJsonArray() && raw.getAsJsonArray().size() == typed.getAsJsonArray().size(), "Legacy array changed during conversion");
            for (int index = 0; index < raw.getAsJsonArray().size(); index++) validateRoundTrip(raw.getAsJsonArray().get(index), typed.getAsJsonArray().get(index));
        } else {
            require(raw.equals(typed), "Legacy value changed during typed conversion");
        }
    }

    private static void noStyleFields(JsonElement element) {
        if (element.isJsonObject()) for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            require(!key.contains("style") && !key.contains("profile"), "Legacy payload contains partial/unknown style fields");
            noStyleFields(entry.getValue());
        } else if (element.isJsonArray()) for (JsonElement child : element.getAsJsonArray()) noStyleFields(child);
    }

    private static void known(JsonObject value, Class<?> type) {
        Set<String> fields = new HashSet<>();
        for (Field field : type.getFields()) if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())
            && !"style".equals(field.getName())) fields.add(field.getName());
        require(fields.containsAll(value.keySet()), "Unknown legacy fields in " + type.getSimpleName());
    }

    private static String string(JsonObject object, String key) {
        require(object.has(key) && object.get(key).isJsonPrimitive() && object.getAsJsonPrimitive(key).isString()
            && !object.get(key).getAsString().isBlank(), "Missing string field " + key);
        return object.get(key).getAsString();
    }

    private static JsonObject object(JsonObject parent, String key) {
        require(parent.has(key) && parent.get(key).isJsonObject(), "Missing object " + key);
        return parent.getAsJsonObject(key);
    }

    private static JsonArray array(JsonObject parent, String key) {
        require(parent.has(key) && parent.get(key).isJsonArray(), "Missing array " + key);
        return parent.getAsJsonArray(key);
    }

    private static String hash(String value) { return Catalog.hash(value.getBytes(StandardCharsets.UTF_8)); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
