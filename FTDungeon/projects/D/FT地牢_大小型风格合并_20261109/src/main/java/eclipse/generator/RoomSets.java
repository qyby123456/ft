package eclipse.generator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RoomSets {
    private static final Set<String> BUILT_INS = Set.of("clockwork", "forest", "desert", "frost", "swamp", "coral", "lava", "amethyst");
    private static final Map<String, Definition> LOADED = new HashMap<>();
    private static final Map<String, List<Catalog.Cell>> DECODED = new HashMap<>();

    private static final class Entry {
        String key, sha256;
    }

    private static final class Manifest {
        int schema;
        String style, selection;
        List<Entry> templates;
    }

    private record Definition(String fingerprint, String directory, Map<String, Entry> entries, boolean balancedLayouts) {}

    private RoomSets() {}

    public static String fingerprint(String style) {
        if (!style.startsWith("fteclipsegen:") || !BUILT_INS.contains(style.substring("fteclipsegen:".length()))) return null;
        return definition(style, null).fingerprint();
    }

    public static boolean balancedLayouts(String style) {
        return fingerprint(style) != null && definition(style, null).balancedLayouts();
    }

    private static synchronized Definition definition(String style, String expected) {
        Catalog.require(style != null && style.startsWith("fteclipsegen:"), "Unknown themed room style");
        String name = style.substring("fteclipsegen:".length());
        Catalog.require(BUILT_INS.contains(name), "Unknown themed room set");
        if (expected != null) {
            Catalog.require(expected.matches("[0-9a-f]{64}"), "Invalid themed room set fingerprint");
            Definition current = definition(style, null);
            if (expected.equals(current.fingerprint())) return current;
        }
        String cacheKey = style + ":" + (expected == null ? "current" : expected);
        return LOADED.computeIfAbsent(cacheKey, unused -> {
            String directory = expected == null ? "data/fteclipsegen/roomsets/" + name + "/"
                : "data/fteclipsegen/roomsets-history/" + name + "/" + expected + "/";
            byte[] raw = Catalog.resource(directory + "manifest.json");
            Catalog.require(expected == null || expected.equals(Catalog.hash(raw)), "Archived themed room manifest fingerprint mismatch");
            Manifest manifest = Catalog.GSON.fromJson(new String(raw, StandardCharsets.UTF_8), Manifest.class);
            Catalog.require(manifest != null && manifest.schema == 1 && style.equals(manifest.style)
                && manifest.templates != null, "Invalid themed room manifest");
            Catalog.require(manifest.selection == null || manifest.selection.equals("balanced-layout-v1"), "Unknown themed room selection policy");
            Map<String, Entry> entries = new LinkedHashMap<>();
            for (Entry entry : manifest.templates) {
                Catalog.require(entry != null && entry.key != null && Catalog.get().templates.containsKey(entry.key)
                    && entry.sha256 != null && entry.sha256.matches("[0-9a-f]{64}"), "Invalid themed room entry");
                Catalog.require(entries.putIfAbsent(entry.key, entry) == null, "Duplicate themed room entry");
            }
            Catalog.require(entries.keySet().equals(Catalog.get().templates.keySet()), "Incomplete themed room set");
            return new Definition(Catalog.hash(raw), directory, Map.copyOf(entries), "balanced-layout-v1".equals(manifest.selection));
        });
    }

    public static void validate(Planner.Plan plan) {
        if (plan.roomSetFingerprint == null) return;
        definition(plan.style, plan.roomSetFingerprint);
    }

    public static List<Catalog.Cell> blocks(Planner.Plan plan, String key) {
        validate(plan);
        Definition definition = definition(Profiles.style(plan), plan.roomSetFingerprint);
        Entry entry = definition.entries().get(key);
        Catalog.require(entry != null, "Missing themed room: " + key);
        return DECODED.computeIfAbsent(definition.directory() + definition.fingerprint() + ":" + key, unused ->
            Catalog.decode(Catalog.get().templates.get(key), definition.directory() + key + ".nbt", entry.sha256));
    }
}
