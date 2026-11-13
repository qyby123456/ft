package cn.farmingtales.sdmshopaudit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public final class ShopDiff {
    private final JsonArray changes = new JsonArray();
    private final int limit;
    private int total;

    private ShopDiff(int limit) {
        this.limit = limit;
    }

    public static JsonObject compare(JsonObject before, JsonObject after, int limit) {
        ShopDiff diff = new ShopDiff(limit);
        diff.fields("SHOP_CHANGED", "", "", without(before, "shopTabs"), without(after, "shopTabs"));
        Map<String, JsonObject> oldTabs = index(array(before, "shopTabs"), "shopTabUUID");
        Map<String, JsonObject> newTabs = index(array(after, "shopTabs"), "shopTabUUID");
        for (String tabId : union(oldTabs, newTabs)) {
            JsonObject oldTab = oldTabs.get(tabId);
            JsonObject newTab = newTabs.get(tabId);
            if (oldTab == null || newTab == null) {
                diff.add(oldTab == null ? "TAB_ADDED" : "TAB_REMOVED", tabId, "", oldTab, newTab);
                continue;
            }
            diff.fields("TAB_CHANGED", tabId, "", without(oldTab, "tabEntry"), without(newTab, "tabEntry"));
            Map<String, JsonObject> oldEntries = index(array(oldTab, "tabEntry"), "entryUUID");
            Map<String, JsonObject> newEntries = index(array(newTab, "tabEntry"), "entryUUID");
            for (String entryId : union(oldEntries, newEntries)) {
                JsonObject oldEntry = oldEntries.get(entryId);
                JsonObject newEntry = newEntries.get(entryId);
                if (oldEntry == null || newEntry == null) {
                    diff.add(oldEntry == null ? "ENTRY_ADDED" : "ENTRY_REMOVED", tabId, entryId, oldEntry, newEntry);
                } else {
                    diff.fields("ENTRY_CHANGED", tabId, entryId, oldEntry, newEntry);
                }
            }
            diff.order("ENTRY_ORDER_CHANGED", tabId, oldEntries, newEntries);
        }
        diff.order("TAB_ORDER_CHANGED", "", oldTabs, newTabs);
        JsonObject result = new JsonObject();
        result.addProperty("totalChanges", diff.total);
        result.addProperty("truncated", diff.total > diff.changes.size());
        result.add("changes", diff.changes);
        return result;
    }

    private void fields(String kind, String tabId, String entryId, JsonObject before, JsonObject after) {
        JsonObject fields = new JsonObject();
        TreeSet<String> keys = new TreeSet<>(before.keySet());
        keys.addAll(after.keySet());
        for (String key : keys) {
            JsonElement oldValue = before.get(key);
            JsonElement newValue = after.get(key);
            if (java.util.Objects.equals(oldValue, newValue)) {
                continue;
            }
            JsonObject values = new JsonObject();
            values.add("before", limited(oldValue));
            values.add("after", limited(newValue));
            fields.add(key, values);
        }
        if (fields.size() != 0) {
            JsonObject change = description(kind, tabId, entryId);
            change.add("fields", fields);
            append(change);
        }
    }

    private void add(String kind, String tabId, String entryId, JsonObject before, JsonObject after) {
        JsonObject change = description(kind, tabId, entryId);
        change.add("before", limited(before));
        change.add("after", limited(after));
        append(change);
    }

    private void order(String kind, String tabId, Map<String, JsonObject> before, Map<String, JsonObject> after) {
        List<String> oldOrder = before.keySet().stream().filter(after::containsKey).toList();
        List<String> newOrder = after.keySet().stream().filter(before::containsKey).toList();
        if (!oldOrder.equals(newOrder)) {
            JsonObject change = description(kind, tabId, "");
            JsonArray oldIds = new JsonArray();
            JsonArray newIds = new JsonArray();
            oldOrder.forEach(oldIds::add);
            newOrder.forEach(newIds::add);
            change.add("before", limited(oldIds));
            change.add("after", limited(newIds));
            append(change);
        }
    }

    private void append(JsonObject change) {
        total++;
        if (changes.size() < limit) {
            changes.add(change);
        }
    }

    private static JsonObject description(String kind, String tabId, String entryId) {
        JsonObject result = new JsonObject();
        result.addProperty("kind", kind);
        result.addProperty("tabUuid", tabId);
        result.addProperty("entryUuid", entryId);
        return result;
    }

    private static JsonObject without(JsonObject source, String excluded) {
        JsonObject copy = source.deepCopy();
        copy.remove(excluded);
        return copy;
    }

    private static JsonArray array(JsonObject owner, String field) {
        return owner.has(field) && owner.get(field).isJsonArray() ? owner.getAsJsonArray(field) : new JsonArray();
    }

    private static Map<String, JsonObject> index(JsonArray values, String idKey) {
        Map<String, JsonObject> indexed = new LinkedHashMap<>();
        int position = 0;
        for (JsonElement element : values) {
            if (element.isJsonObject()) {
                JsonObject value = element.getAsJsonObject();
                String id = identifier(value.get(idKey), position);
                String uniqueId = id;
                int duplicate = 1;
                while (indexed.containsKey(uniqueId)) {
                    uniqueId = id + "#duplicate" + duplicate++;
                }
                indexed.put(uniqueId, value);
            }
            position++;
        }
        return indexed;
    }

    public static String identifier(JsonElement value, int position) {
        if (value != null && value.isJsonArray() && value.getAsJsonArray().size() == 4) {
            JsonArray parts = value.getAsJsonArray();
            long most = ((long) parts.get(0).getAsInt() << 32) | (parts.get(1).getAsInt() & 0xffffffffL);
            long least = ((long) parts.get(2).getAsInt() << 32) | (parts.get(3).getAsInt() & 0xffffffffL);
            return new UUID(most, least).toString();
        }
        return value == null ? "@position" + position : value.toString();
    }

    private static List<String> union(Map<String, ?> before, Map<String, ?> after) {
        List<String> result = new ArrayList<>(before.keySet());
        after.keySet().stream().filter(key -> !before.containsKey(key)).forEach(result::add);
        return result;
    }

    public static JsonElement limited(JsonElement value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        String text = value.toString();
        if (text.length() <= 4096) {
            return value.deepCopy();
        }
        int end = Character.isHighSurrogate(text.charAt(4095)) ? 4095 : 4096;
        JsonObject truncated = new JsonObject();
        truncated.addProperty("truncated", true);
        truncated.addProperty("sha256", ShopSnapshot.digest(text));
        truncated.addProperty("preview", text.substring(0, end));
        return truncated;
    }
}
