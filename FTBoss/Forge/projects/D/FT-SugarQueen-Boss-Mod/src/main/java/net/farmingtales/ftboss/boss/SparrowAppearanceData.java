package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.util.SparrowMultiplayerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class SparrowAppearanceData extends SavedData {
    public static final class Entry {
        public final UUID owner;
        public final int stage;
        public int players = 1;
        public int used;
        public long lastAppearance = Long.MIN_VALUE;
        public final Set<UUID> birds = new HashSet<>();

        public Entry(UUID owner, int stage, boolean visited) {
            this.owner = owner;
            this.stage = stage;
            this.used = visited ? 1 : 0;
        }

        public boolean observe(int count) {
            int maximum = Math.max(this.players, SparrowMultiplayerRules.quota(count));
            if (maximum == this.players) return false;
            this.players = maximum;
            return true;
        }

        public boolean record(UUID bird, boolean legacyBird) {
            if (bird == null || !this.birds.add(bird)) return false;
            if (!legacyBird && this.used < Integer.MAX_VALUE) this.used++;
            return true;
        }
    }

    private final Map<String, Entry> appearances = new HashMap<>();

    public static SparrowAppearanceData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(SparrowAppearanceData::load,
                SparrowAppearanceData::new, "ftboss_sparrow_multiplayer");
    }

    public Entry find(UUID owner, int stage) {
        return owner == null ? null : this.appearances.get(owner + ":" + stage);
    }

    public Entry entry(UUID owner, int stage, boolean visited) {
        Entry existing = this.find(owner, stage);
        if (existing != null || owner == null) return existing;
        Entry created = new Entry(owner, stage, visited);
        this.appearances.put(owner + ":" + stage, created);
        this.setDirty();
        return created;
    }

    public void prune(ManorFightData fights) {
        if (this.appearances.values().removeIf(entry -> entry.stage >= 0 && fights.retired(entry.owner))) this.setDirty();
    }

    public boolean onCooldown(Entry entry, long gameTime) {
        return entry != null && entry.lastAppearance != Long.MIN_VALUE && gameTime - entry.lastAppearance < 40;
    }

    public void markAppearance(Entry entry, long gameTime) {
        if (entry != null) {
            entry.lastAppearance = gameTime;
            this.setDirty();
        }
    }

    public static SparrowAppearanceData load(CompoundTag tag) {
        SparrowAppearanceData data = new SparrowAppearanceData();
        for (Tag element : tag.getList("Appearances", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) element;
            if (!record.hasUUID("Owner")) continue;
            int stage = record.getInt("Stage");
            if (stage != -1 && net.farmingtales.ftboss.util.SparrowAppearanceRules.bit(stage) == 0) continue;
            Entry entry = data.entry(record.getUUID("Owner"), stage, false);
            entry.players = Math.max(entry.players, SparrowMultiplayerRules.quota(record.getInt("Players")));
            entry.used = Math.max(entry.used, Math.max(0, record.getInt("Used")));
            entry.lastAppearance = record.contains("LastAppearance") ? record.getLong("LastAppearance") : Long.MIN_VALUE;
            for (Tag bird : record.getList("Birds", Tag.TAG_COMPOUND)) {
                CompoundTag identity = (CompoundTag) bird;
                if (identity.hasUUID("Id")) entry.birds.add(identity.getUUID("Id"));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag records = new ListTag();
        for (Entry entry : this.appearances.values()) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Owner", entry.owner);
            record.putInt("Stage", entry.stage);
            record.putInt("Players", entry.players);
            record.putInt("Used", entry.used);
            if (entry.lastAppearance != Long.MIN_VALUE) record.putLong("LastAppearance", entry.lastAppearance);
            ListTag birds = new ListTag();
            for (UUID bird : entry.birds) {
                CompoundTag identity = new CompoundTag();
                identity.putUUID("Id", bird);
                birds.add(identity);
            }
            record.put("Birds", birds);
            records.add(record);
        }
        tag.put("Appearances", records);
        return tag;
    }
}
