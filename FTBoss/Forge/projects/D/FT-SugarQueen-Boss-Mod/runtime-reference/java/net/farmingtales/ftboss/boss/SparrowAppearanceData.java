/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.level.saveddata.SavedData
 */
package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.util.SparrowAppearanceRules;
import net.farmingtales.ftboss.util.SparrowMultiplayerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class SparrowAppearanceData
extends SavedData {
    private final Map<String, Entry> appearances = new HashMap<String, Entry>();

    public static SparrowAppearanceData get(MinecraftServer minecraftServer) {
        return (SparrowAppearanceData)minecraftServer.m_129783_().m_8895_().m_164861_(SparrowAppearanceData::load, SparrowAppearanceData::new, "ftboss_sparrow_multiplayer");
    }

    public Entry find(UUID uUID, int n) {
        return uUID == null ? null : this.appearances.get(String.valueOf(uUID) + ":" + n);
    }

    public Entry entry(UUID uUID, int n, boolean bl) {
        Entry entry = this.find(uUID, n);
        if (entry != null || uUID == null) {
            return entry;
        }
        Entry entry2 = new Entry(uUID, n, bl);
        this.appearances.put(String.valueOf(uUID) + ":" + n, entry2);
        this.m_77762_();
        return entry2;
    }

    public void prune(ManorFightData manorFightData) {
        if (this.appearances.values().removeIf(entry -> entry.stage >= 0 && manorFightData.retired(entry.owner))) {
            this.m_77762_();
        }
    }

    public boolean onCooldown(Entry entry, long l) {
        return entry != null && entry.lastAppearance != Long.MIN_VALUE && l - entry.lastAppearance < 40L;
    }

    public void markAppearance(Entry entry, long l) {
        if (entry != null) {
            entry.lastAppearance = l;
            this.m_77762_();
        }
    }

    public static SparrowAppearanceData load(CompoundTag compoundTag) {
        SparrowAppearanceData sparrowAppearanceData = new SparrowAppearanceData();
        for (Tag tag : compoundTag.m_128437_("Appearances", 10)) {
            int n;
            CompoundTag compoundTag2 = (CompoundTag)tag;
            if (!compoundTag2.m_128403_("Owner") || (n = compoundTag2.m_128451_("Stage")) != -1 && SparrowAppearanceRules.bit(n) == 0) continue;
            Entry entry = sparrowAppearanceData.entry(compoundTag2.m_128342_("Owner"), n, false);
            entry.players = Math.max(entry.players, SparrowMultiplayerRules.quota(compoundTag2.m_128451_("Players")));
            entry.used = Math.max(entry.used, Math.max(0, compoundTag2.m_128451_("Used")));
            entry.lastAppearance = compoundTag2.m_128441_("LastAppearance") ? compoundTag2.m_128454_("LastAppearance") : Long.MIN_VALUE;
            for (Tag tag2 : compoundTag2.m_128437_("Birds", 10)) {
                CompoundTag compoundTag3 = (CompoundTag)tag2;
                if (!compoundTag3.m_128403_("Id")) continue;
                entry.birds.add(compoundTag3.m_128342_("Id"));
            }
        }
        return sparrowAppearanceData;
    }

    public CompoundTag m_7176_(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (Entry entry : this.appearances.values()) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128362_("Owner", entry.owner);
            compoundTag2.m_128405_("Stage", entry.stage);
            compoundTag2.m_128405_("Players", entry.players);
            compoundTag2.m_128405_("Used", entry.used);
            if (entry.lastAppearance != Long.MIN_VALUE) {
                compoundTag2.m_128356_("LastAppearance", entry.lastAppearance);
            }
            ListTag listTag2 = new ListTag();
            for (UUID uUID : entry.birds) {
                CompoundTag compoundTag3 = new CompoundTag();
                compoundTag3.m_128362_("Id", uUID);
                listTag2.add((Object)compoundTag3);
            }
            compoundTag2.m_128365_("Birds", (Tag)listTag2);
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("Appearances", (Tag)listTag);
        return compoundTag;
    }

    public static final class Entry {
        public final UUID owner;
        public final int stage;
        public int players = 1;
        public int used;
        public long lastAppearance = Long.MIN_VALUE;
        public final Set<UUID> birds = new HashSet<UUID>();

        public Entry(UUID uUID, int n, boolean bl) {
            this.owner = uUID;
            this.stage = n;
            this.used = bl ? 1 : 0;
        }

        public boolean observe(int n) {
            int n2 = Math.max(this.players, SparrowMultiplayerRules.quota(n));
            if (n2 == this.players) {
                return false;
            }
            this.players = n2;
            return true;
        }

        public boolean record(UUID uUID, boolean bl) {
            if (uUID == null || !this.birds.add(uUID)) {
                return false;
            }
            if (!bl && this.used < Integer.MAX_VALUE) {
                ++this.used;
            }
            return true;
        }
    }
}

