package net.farmingtales.ftdungeonloot;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class LootRunData extends SavedData {
    private UUID run = UUID.randomUUID();

    public static LootRunData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(LootRunData::load, () -> {
            LootRunData data = new LootRunData();
            data.setDirty();
            return data;
        }, "ftdungeonloot_run");
    }

    private static LootRunData load(CompoundTag tag) {
        LootRunData data = new LootRunData();
        if (tag.hasUUID("Run")) data.run = tag.getUUID("Run");
        return data;
    }

    public UUID run() { return run; }
    public void next() { run = UUID.randomUUID(); setDirty(); }
    @Override
    public CompoundTag save(CompoundTag tag) { tag.putUUID("Run", run); return tag; }
}
