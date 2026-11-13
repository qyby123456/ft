package net.farmingtales.ftdungeonmobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class DungeonRoomsData extends SavedData {
    private static final int LAYOUT_VERSION = 1;
    private int layoutVersion;
    private final Map<String, String> rooms = new LinkedHashMap<>();

    public static DungeonRoomsData get(ServerLevel level) {
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("Dungeon rooms require the server thread.");
        }
        return level.getDataStorage().computeIfAbsent(DungeonRoomsData::load, DungeonRoomsData::new,
                "ftdungeonmobs_rooms");
    }

    public static DungeonRoomsData load(CompoundTag tag) {
        DungeonRoomsData data = new DungeonRoomsData();
        data.layoutVersion = Math.max(0, tag.getInt("LayoutVersion"));
        for (Tag value : tag.getList("Rooms", Tag.TAG_COMPOUND)) {
            CompoundTag room = (CompoundTag) value;
            String identity = room.getString("Id");
            String fingerprint = room.getString("Fingerprint");
            if (identity.matches("[A-Z][A-Z0-9_]{0,31}") && fingerprint.matches("[0-9a-f]{64}")) {
                data.rooms.putIfAbsent(identity, fingerprint);
            }
        }
        return data;
    }

    public boolean ready() {
        return layoutVersion >= LAYOUT_VERSION;
    }

    public void markBuilt() {
        layoutVersion = LAYOUT_VERSION;
        setDirty();
    }

    public String roomFingerprint(String identity) {
        return rooms.get(identity);
    }

    public void markRoomBuilt(String identity, String fingerprint) {
        if (!identity.matches("[A-Z][A-Z0-9_]{0,31}") || !fingerprint.matches("[0-9a-f]{64}")
                || rooms.containsKey(identity)) {
            throw new IllegalArgumentException("Invalid or already built room: " + identity);
        }
        rooms.put(identity, fingerprint);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("LayoutVersion", layoutVersion);
        ListTag entries = new ListTag();
        rooms.forEach((identity, fingerprint) -> {
            CompoundTag room = new CompoundTag();
            room.putString("Id", identity);
            room.putString("Fingerprint", fingerprint);
            entries.add(room);
        });
        tag.put("Rooms", entries);
        return tag;
    }
}
