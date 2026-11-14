package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public final class FoodRewardClaims extends SavedData {
    private final Set<UUID> claimed = new HashSet<>();

    public static FoodRewardClaims load(CompoundTag tag) {
        FoodRewardClaims data = new FoodRewardClaims();
        ListTag entries = tag.getList("Claims", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (entry.hasUUID("Id")) data.claimed.add(entry.getUUID("Id"));
        }
        return data;
    }

    public boolean claim(UUID identifier) {
        if (!claimed.add(identifier)) return false;
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (UUID identifier : claimed) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", identifier);
            entries.add(entry);
        }
        tag.put("Claims", entries);
        return tag;
    }
}
