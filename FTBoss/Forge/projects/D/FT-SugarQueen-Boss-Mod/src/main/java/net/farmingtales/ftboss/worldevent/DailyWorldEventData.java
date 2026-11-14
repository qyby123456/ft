package net.farmingtales.ftboss.worldevent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/** Persistent day boundary, active event, one-per-player gifts, and Oracle follow-up state. */
public final class DailyWorldEventData extends SavedData {
    private static final String DATA_ID = "ftboss_daily_world_events";
    private long currentDay = Long.MIN_VALUE;
    private String activeId = "";
    private final Set<String> claimedGifts = new HashSet<>();
    private final Map<UUID, Long> oracleConsultedDays = new HashMap<>();
    private final Map<UUID, Long> oracleAlertedDays = new HashMap<>();

    public static DailyWorldEventData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                DailyWorldEventData::load, DailyWorldEventData::new, DATA_ID);
    }

    public static DailyWorldEventData load(CompoundTag tag) {
        DailyWorldEventData data = new DailyWorldEventData();
        data.currentDay = tag.contains("CurrentDay", Tag.TAG_LONG) ? tag.getLong("CurrentDay") : Long.MIN_VALUE;
        data.activeId = tag.getString("ActiveEvent");
        ListTag gifts = tag.getList("ClaimedGifts", Tag.TAG_STRING);
        for (int i = 0; i < gifts.size(); i++) data.claimedGifts.add(gifts.getString(i));
        readPlayerDays(tag, "OracleConsultedDays", data.oracleConsultedDays);
        readPlayerDays(tag, "OracleAlertedDays", data.oracleAlertedDays);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("CurrentDay", this.currentDay);
        tag.putString("ActiveEvent", this.activeId);
        ListTag gifts = new ListTag();
        this.claimedGifts.stream().sorted().forEach(key -> gifts.add(StringTag.valueOf(key)));
        tag.put("ClaimedGifts", gifts);
        tag.put("OracleConsultedDays", writePlayerDays(this.oracleConsultedDays));
        tag.put("OracleAlertedDays", writePlayerDays(this.oracleAlertedDays));
        return tag;
    }

    private static void readPlayerDays(CompoundTag root, String key, Map<UUID, Long> target) {
        if (!root.contains(key, Tag.TAG_COMPOUND)) return;
        CompoundTag values = root.getCompound(key);
        for (String playerId : values.getAllKeys()) {
            if (!values.contains(playerId, Tag.TAG_LONG)) continue;
            try {
                target.put(UUID.fromString(playerId), values.getLong(playerId));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed entries rather than making an old world unloadable.
            }
        }
    }

    private static CompoundTag writePlayerDays(Map<UUID, Long> source) {
        CompoundTag values = new CompoundTag();
        source.forEach((player, day) -> values.putLong(player.toString(), day));
        return values;
    }

    public long currentDay() {
        return this.currentDay;
    }

    @Nullable
    public DailyWorldEvent activeEvent() {
        return DailyWorldEvent.byId(this.activeId);
    }

    public void beginDay(long day, @Nullable DailyWorldEvent active) {
        this.currentDay = day;
        this.activeId = active == null ? "" : active.id();
        if (this.claimedGifts.size() > 4096) {
            this.claimedGifts.removeIf(key -> {
                int split = key.indexOf('|');
                if (split < 1) return true;
                try {
                    return Long.parseLong(key.substring(0, split)) < day - 370L;
                } catch (NumberFormatException ignored) {
                    return true;
                }
            });
        }
        this.setDirty();
    }

    public boolean claimGift(UUID player, long day) {
        boolean added = this.claimedGifts.add(day + "|" + player);
        if (added) this.setDirty();
        return added;
    }

    /** Remembers the most recent day on which this player paid for and received an Oracle forecast. */
    public void recordOracleConsultation(UUID player, long day) {
        Long previous = this.oracleConsultedDays.put(player, day);
        if (previous == null || previous != day) this.setDirty();
    }

    /** Claims the next-day realization notice exactly once, including after reconnecting or restarting. */
    public boolean claimOracleAlert(UUID player, long day, boolean eventTriggered) {
        Long consultedDay = this.oracleConsultedDays.get(player);
        if (consultedDay == null || !DailyWorldEventRules.oracleAlertDue(consultedDay, day, eventTriggered)) {
            return false;
        }
        Long alertedDay = this.oracleAlertedDays.get(player);
        if (alertedDay != null && alertedDay == day) return false;
        this.oracleAlertedDays.put(player, day);
        this.setDirty();
        return true;
    }
}