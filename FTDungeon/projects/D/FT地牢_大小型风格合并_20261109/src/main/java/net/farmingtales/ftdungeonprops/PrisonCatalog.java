package net.farmingtales.ftdungeonprops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PrisonCatalog {
    public record Spec(String id, String kind, String base, String mount, int light, List<int[]> boxes) {}

    public static final List<Spec> ALL = load();

    private PrisonCatalog() {}

    private static List<Spec> load() {
        InputStream stream = PrisonCatalog.class.getResourceAsStream("/data/ftdungeonprops/kit_catalog.json");
        if (stream == null) {
            throw new IllegalStateException("Missing prison kit catalog");
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonArray entries = JsonParser.parseReader(reader).getAsJsonArray();
            List<Spec> result = new ArrayList<>();
            Set<String> identifiers = new HashSet<>();
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                String identifier = entry.get("id").getAsString();
                String kind = entry.get("kind").getAsString();
                String mount = entry.get("mount").getAsString();
                int light = entry.get("light").getAsInt();
                if (!identifier.matches("prison_[a-z0-9_]+") || !identifiers.add(identifier)) {
                    throw new IllegalStateException("Invalid/duplicate identifier: " + identifier);
                }
                if (!Set.of("solid", "decor", "stairs", "slab").contains(kind)
                        || !Set.of("any", "floor", "wall", "ceiling").contains(mount)
                        || light < 0 || light > 15 || (light > 0 && !kind.equals("decor"))) {
                    throw new IllegalStateException("Invalid block behavior: " + identifier);
                }
                List<int[]> boxes = new ArrayList<>();
                for (JsonElement shape : entry.getAsJsonArray("boxes")) {
                    JsonArray coordinates = shape.getAsJsonArray();
                    if (coordinates.size() != 6) {
                        throw new IllegalStateException("Invalid collision box: " + identifier);
                    }
                    int[] bounds = new int[6];
                    for (int coordinate = 0; coordinate < 6; coordinate++) {
                        double value = coordinates.get(coordinate).getAsDouble();
                        if (value != Math.rint(value) || value < 0 || value > 16) {
                            throw new IllegalStateException("Invalid coordinate: " + identifier);
                        }
                        bounds[coordinate] = (int) value;
                    }
                    for (int axis = 0; axis < 3; axis++) {
                        if (bounds[axis] >= bounds[axis + 3]) {
                            throw new IllegalStateException("Zero/inverted box: " + identifier);
                        }
                    }
                    boxes.add(bounds);
                }
                if (boxes.isEmpty() || boxes.size() > 28) {
                    throw new IllegalStateException("Geometry budget exceeded: " + identifier);
                }
                String base = entry.has("base") ? entry.get("base").getAsString() : "";
                result.add(new Spec(identifier, kind, base, mount, light, List.copyOf(boxes)));
            }
            if (result.size() != 24) {
                throw new IllegalStateException("Expected 24 prison kit blocks, got " + result.size());
            }
            return List.copyOf(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load prison kit catalog", exception);
        }
    }
}
