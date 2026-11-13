package cn.farmingtales.qysevent.worldevent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/** Saved daily-event state owned exclusively by qys-event. */
public final class DailyWorldEventData extends SavedData {
    private static final String DATA_ID = "qys_event_daily_world_events";
    private long currentDay = Long.MIN_VALUE;
    private String activeId = "";
    private final Set<String> claimedGifts = new HashSet<>();
    private final Map<UUID, Long> oracleConsultedDays = new HashMap<>();
    private final Map<UUID, Long> oracleAlertedDays = new HashMap<>();

    private static final SavedData.Factory<DailyWorldEventData> FACTORY =
            new SavedData.Factory<>(DailyWorldEventData::new, (tag, registries) -> load(tag));

    public static DailyWorldEventData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public static DailyWorldEventData load(CompoundTag tag) {
        DailyWorldEventData data = new DailyWorldEventData();
        data.currentDay = tag.contains("CurrentDay") ? tag.getLong("CurrentDay") : Long.MIN_VALUE;
        data.activeId = tag.getString("ActiveEvent");
        ListTag gifts = tag.getList("ClaimedGifts", Tag.TAG_STRING);
        for (int i = 0; i < gifts.size(); i++) data.claimedGifts.add(gifts.getString(i));
        readPlayerDays(tag, "OracleConsultedDays", data.oracleConsultedDays);
        readPlayerDays(tag, "OracleAlertedDays", data.oracleAlertedDays);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("CurrentDay", currentDay);
        tag.putString("ActiveEvent", activeId);
        ListTag gifts = new ListTag();
        claimedGifts.stream().sorted().forEach(key -> gifts.add(StringTag.valueOf(key)));
        tag.put("ClaimedGifts", gifts);
        tag.put("OracleConsultedDays", writePlayerDays(oracleConsultedDays));
        tag.put("OracleAlertedDays", writePlayerDays(oracleAlertedDays));
        return tag;
    }

    private static void readPlayerDays(CompoundTag parent, String key, Map<UUID, Long> target) {
        CompoundTag values = parent.getCompound(key);
        for (String raw : values.getAllKeys()) {
            try { target.put(UUID.fromString(raw), values.getLong(raw)); }
            catch (IllegalArgumentException ignored) { }
        }
    }

    private static CompoundTag writePlayerDays(Map<UUID, Long> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((uuid, day) -> tag.putLong(uuid.toString(), day));
        return tag;
    }

    public long currentDay() { return currentDay; }
    @Nullable public DailyWorldEvent activeEvent() { return DailyWorldEvent.byId(activeId); }

    public void beginDay(long day, @Nullable DailyWorldEvent active) {
        currentDay = day;
        activeId = active == null ? "" : active.id();
        if (claimedGifts.size() > 4096) {
            claimedGifts.removeIf(key -> {
                int split = key.indexOf('|');
                if (split < 1) return true;
                try { return Long.parseLong(key.substring(0, split)) < day - 370L; }
                catch (NumberFormatException ignored) { return true; }
            });
        }
        setDirty();
    }

    public boolean claimGift(UUID player, long day) {
        boolean added = claimedGifts.add(day + "|" + player);
        if (added) setDirty();
        return added;
    }

    public void recordOracleConsultation(UUID player, long day) {
        oracleConsultedDays.put(player, day);
        setDirty();
    }

    public boolean claimOracleAlert(UUID player, long day, boolean eventTriggered) {
        Long consulted = oracleConsultedDays.get(player);
        if (!eventTriggered || consulted == null || consulted != day - 1L) return false;
        if (oracleAlertedDays.getOrDefault(player, Long.MIN_VALUE) == day) return false;
        oracleAlertedDays.put(player, day);
        setDirty();
        return true;
    }
}




