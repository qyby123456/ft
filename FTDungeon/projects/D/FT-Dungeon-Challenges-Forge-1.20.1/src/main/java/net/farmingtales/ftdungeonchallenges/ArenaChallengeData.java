package net.farmingtales.ftdungeonchallenges;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class ArenaChallengeData extends SavedData {
    private final Map<UUID, Long> players = new HashMap<>();
    private final Map<String, Long> locations = new HashMap<>();

    static ArenaChallengeData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(ArenaChallengeData::load,
                ArenaChallengeData::new, FTDungeonChallenges.ID + "_cooldowns");
    }

    static ArenaChallengeData load(CompoundTag tag) {
        ArenaChallengeData data = new ArenaChallengeData();
        ListTag torchEntries = tag.getList("Torches", Tag.TAG_COMPOUND);
        for (int index = 0; index < torchEntries.size(); index++) {
            CompoundTag entry = torchEntries.getCompound(index);
            data.locations.put(entry.getString("Location"), entry.getLong("Until"));
        }
        ListTag playerEntries = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int index = 0; index < playerEntries.size(); index++) {
            CompoundTag entry = playerEntries.getCompound(index);
            if (entry.hasUUID("Player")) {
                data.players.put(entry.getUUID("Player"), entry.getLong("Until"));
            }
        }
        return data;
    }

    long remaining(ServerLevel level, BlockPos position, UUID player) {
        return Math.max(0, Math.max(locations.getOrDefault(location(level, position), 0L),
                players.getOrDefault(player, 0L)) - level.getServer().overworld().getGameTime());
    }

    long remainingForPlayer(ServerLevel level, UUID player) {
        return Math.max(0, players.getOrDefault(player, 0L) - level.getServer().overworld().getGameTime());
    }

    void reserve(ServerLevel level, BlockPos position, Iterable<UUID> participants, int seconds) {
        long now = level.getServer().overworld().getGameTime();
        locations.values().removeIf(until -> until <= now);
        players.values().removeIf(until -> until <= now);
        long until = now + seconds * 20L;
        locations.merge(location(level, position), until, Math::max);
        for (UUID participant : participants) {
            players.merge(participant, until, Math::max);
        }
        setDirty();
    }

    private static String location(ServerLevel level, BlockPos position) {
        return level.dimension().location() + "/" + position.asLong();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag torchEntries = new ListTag();
        locations.forEach((location, until) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Location", location);
            entry.putLong("Until", until);
            torchEntries.add(entry);
        });
        tag.put("Torches", torchEntries);
        ListTag playerEntries = new ListTag();
        players.forEach((player, until) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.putLong("Until", until);
            playerEntries.add(entry);
        });
        tag.put("Players", playerEntries);
        return tag;
    }
}
