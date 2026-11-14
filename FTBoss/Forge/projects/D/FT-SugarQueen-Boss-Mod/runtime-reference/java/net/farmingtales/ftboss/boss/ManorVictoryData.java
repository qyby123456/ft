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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class ManorVictoryData
extends SavedData {
    final Map<UUID, CompoundTag> pending = new LinkedHashMap<UUID, CompoundTag>();
    final Set<UUID> delivered = new HashSet<UUID>();
    private final Set<UUID> conquerors = new HashSet<UUID>();

    public static ManorVictoryData get(MinecraftServer server) {
        return (ManorVictoryData)server.m_129783_().m_8895_().m_164861_(ManorVictoryData::load, ManorVictoryData::new, "ftboss_manor_victories");
    }

    public static ManorVictoryData load(CompoundTag tag) {
        CompoundTag record;
        ManorVictoryData result = new ManorVictoryData();
        for (Tag value : tag.m_128437_("Pending", 10)) {
            record = (CompoundTag)value;
            if (!record.m_128403_("Id")) continue;
            result.pending.put(record.m_128342_("Id"), record.m_6426_());
        }
        for (Tag value : tag.m_128437_("Delivered", 10)) {
            record = (CompoundTag)value;
            if (!record.m_128403_("Id")) continue;
            result.delivered.add(record.m_128342_("Id"));
        }
        for (Tag value : tag.m_128437_("Conquerors", 10)) {
            record = (CompoundTag)value;
            if (!record.m_128403_("Id")) continue;
            result.conquerors.add(record.m_128342_("Id"));
        }
        result.delivered.forEach(result.pending::remove);
        return result;
    }

    public boolean known(UUID identifier) {
        return this.pending.containsKey(identifier) || this.delivered.contains(identifier);
    }

    public boolean hasConquered(UUID player) {
        return this.conquerors.contains(player);
    }

    public void recordConqueror(UUID player) {
        if (player != null && this.conquerors.add(player)) {
            this.m_77762_();
        }
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        ListTag waiting = new ListTag();
        this.pending.values().forEach(record -> waiting.add((Object)record.m_6426_()));
        tag.m_128365_("Pending", (Tag)waiting);
        ListTag finished = new ListTag();
        for (UUID identifier : this.delivered) {
            CompoundTag record2 = new CompoundTag();
            record2.m_128362_("Id", identifier);
            finished.add((Object)record2);
        }
        tag.m_128365_("Delivered", (Tag)finished);
        ListTag winners = new ListTag();
        for (UUID player : this.conquerors) {
            CompoundTag record3 = new CompoundTag();
            record3.m_128362_("Id", player);
            winners.add((Object)record3);
        }
        tag.m_128365_("Conquerors", (Tag)winners);
        return tag;
    }
}

