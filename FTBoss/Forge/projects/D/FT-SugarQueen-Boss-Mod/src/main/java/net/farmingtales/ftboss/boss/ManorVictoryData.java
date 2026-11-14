package net.farmingtales.ftboss.boss;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class ManorVictoryData extends SavedData {
    final Map<UUID, CompoundTag> pending = new LinkedHashMap<>();
    final Set<UUID> delivered = new HashSet<>();
    private final Set<UUID> conquerors = new HashSet<>();

    public static ManorVictoryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ManorVictoryData::load, ManorVictoryData::new, "ftboss_manor_victories");
    }

    public static ManorVictoryData load(CompoundTag tag) {
        ManorVictoryData result = new ManorVictoryData();
        for (Tag value : tag.getList("Pending", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            if (record.hasUUID("Id")) result.pending.put(record.getUUID("Id"), record.copy());
        }
        for (Tag value : tag.getList("Delivered", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            if (record.hasUUID("Id")) result.delivered.add(record.getUUID("Id"));
        }
        for (Tag value : tag.getList("Conquerors", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            if (record.hasUUID("Id")) result.conquerors.add(record.getUUID("Id"));
        }
        result.delivered.forEach(result.pending::remove);
        return result;
    }

    public boolean known(UUID identifier) { return pending.containsKey(identifier) || delivered.contains(identifier); }

    public boolean hasConquered(UUID player) { return conquerors.contains(player); }

    public void recordConqueror(UUID player) {
        if (player != null && conquerors.add(player)) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag waiting = new ListTag();
        pending.values().forEach(record -> waiting.add(record.copy()));
        tag.put("Pending", waiting);
        ListTag finished = new ListTag();
        for (UUID identifier : delivered) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", identifier);
            finished.add(record);
        }
        tag.put("Delivered", finished);
        ListTag winners = new ListTag();
        for (UUID player : conquerors) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", player);
            winners.add(record);
        }
        tag.put("Conquerors", winners);
        return tag;
    }
}
