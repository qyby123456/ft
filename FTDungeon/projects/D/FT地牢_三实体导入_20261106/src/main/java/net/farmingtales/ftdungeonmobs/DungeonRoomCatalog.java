package net.farmingtales.ftdungeonmobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class DungeonRoomCatalog {
    private DungeonRoomCatalog() {}

    public record Entry(String id, int slot) {}

    public record Definition(String id, String name, int slot, Vec3i size, BlockPos entrance, BlockPos interior, String sha256) {
        public BlockPos origin() {
            return new BlockPos(96 + slot % 3 * 112, 65, 64 + slot / 3 * 160);
        }

        public ResourceLocation template() {
            return new ResourceLocation("ftdungeonmobs", "image25/" + id.toLowerCase(Locale.ROOT));
        }

        public int rowZ() {
            return 23 + slot / 3 * 160;
        }

        public String fingerprint() {
            return digest((id + "|" + slot + "|" + size + "|" + entrance + "|" + interior + "|" + sha256 + "|north|layout1")
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    public static List<Entry> entries(ResourceManager resources) throws Exception {
        JsonObject catalog = read(resources, new ResourceLocation("ftdungeonmobs", "dungeon_rooms/catalog.json"));
        List<Entry> result = new ArrayList<>();
        Set<String> identities = new HashSet<>();
        Set<Integer> slots = new HashSet<>();
        for (JsonElement value : catalog.getAsJsonArray("rooms")) {
            JsonObject item = value.getAsJsonObject();
            String identity = item.get("id").getAsString();
            int slot = item.get("slot").getAsBigDecimal().intValueExact();
            if (!identity.matches("[A-Z][A-Z0-9_]{0,31}") || slot < 0 || slot >= 64
                    || !identities.add(identity) || !slots.add(slot)) {
                throw new IllegalArgumentException("Invalid/duplicate room or slot: " + identity);
            }
            result.add(new Entry(identity, slot));
        }
        return List.copyOf(result);
    }

    public static Definition definition(ResourceManager resources, Entry entry) throws Exception {
        JsonObject item = read(resources, new ResourceLocation("ftdungeonmobs", "dungeon_rooms/image25/" + entry.id().toLowerCase(Locale.ROOT) + ".json"));
        BlockPos size = vector(item, "size");
        BlockPos entrance = vector(item, "entrance");
        BlockPos interior = vector(item, "interior");
        String name = item.get("name").getAsString();
        String hash = item.get("sha256").getAsString().toLowerCase(Locale.ROOT);
        if (!item.get("id").getAsString().equals(entry.id()) || !item.get("facing").getAsString().equals("north")
                || name.isBlank() || name.length() > 128 || !hash.matches("[0-9a-f]{64}")
                || size.getX() < 5 || size.getX() > 96 || size.getZ() < 5 || size.getZ() > 96 || size.getY() < 5 || size.getY() > 64
                || entrance.getZ() != 0 || entrance.getY() < 1 || entrance.getY() > 30 || entrance.getY() + 2 >= size.getY()
                || Math.abs(entrance.getX() - (size.getX() - 1) / 2.0) > 0.5
                || interior.getX() < 0 || interior.getX() >= size.getX() || interior.getZ() < 0 || interior.getZ() >= size.getZ()
                || interior.getY() < 1 || interior.getY() + 1 >= size.getY()) {
            throw new IllegalArgumentException("Invalid north-entry metadata: " + entry.id());
        }
        return new Definition(entry.id(), name, entry.slot(), size, entrance, interior, hash);
    }

    public static String digest(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static JsonObject read(ResourceManager resources, ResourceLocation identity) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(resources.open(identity), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static BlockPos vector(JsonObject item, String name) {
        JsonArray values = item.getAsJsonArray(name);
        if (values.size() != 3) {
            throw new IllegalArgumentException("Expected integer XYZ: " + name);
        }
        return new BlockPos(values.get(0).getAsBigDecimal().intValueExact(), values.get(1).getAsBigDecimal().intValueExact(),
                values.get(2).getAsBigDecimal().intValueExact());
    }
}
