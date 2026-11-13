package net.farmingtales.ftdungeonmobs;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class DungeonBossGateData extends SavedData {
    public static final String FILE_NAME = "ftdungeonmobs_boss_gates";
    private final Map<BlockPos, DungeonBossGateApi.Binding> bindings = new LinkedHashMap<>();
    private final Set<UUID> arrived = new LinkedHashSet<>();
    private UUID roundId;
    private BlockPos activeGate;

    public static DungeonBossGateData get(ServerLevel level) {
        if (!level.getServer().isSameThread() || !level.dimension().equals(DungeonGateTravel.DUNGEON)) {
            throw new IllegalStateException("Boss gates require the dungeon level on the server thread.");
        }
        return level.getDataStorage().computeIfAbsent(DungeonBossGateData::load, DungeonBossGateData::new, FILE_NAME);
    }

    public static DungeonBossGateData load(CompoundTag tag) {
        DungeonBossGateData data = new DungeonBossGateData();
        for (Tag value : tag.getList("Bindings", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            try {
                if (!entry.contains("Gate", Tag.TAG_LONG) || !entry.contains("Min", Tag.TAG_LONG)
                        || !entry.contains("Max", Tag.TAG_LONG) || !entry.contains("Arrival", Tag.TAG_LONG)) {
                    continue;
                }
                Direction facing = Direction.byName(entry.getString("Facing"));
                DungeonBossGateApi.Binding binding = new DungeonBossGateApi.Binding(BlockPos.of(entry.getLong("Gate")),
                        facing, BlockPos.of(entry.getLong("Min")), BlockPos.of(entry.getLong("Max")),
                        BlockPos.of(entry.getLong("Arrival")));
                if (data.bindings.size() < 64) {
                    data.bindings.putIfAbsent(binding.gate(), binding);
                }
            } catch (IllegalArgumentException ignored) {
                data.setDirty();
            }
        }
        if (tag.hasUUID("Round") && tag.contains("ActiveGate", Tag.TAG_LONG)
                && data.bindings.containsKey(BlockPos.of(tag.getLong("ActiveGate")))) {
            data.roundId = tag.getUUID("Round");
            data.activeGate = BlockPos.of(tag.getLong("ActiveGate"));
            for (Tag value : tag.getList("Arrived", Tag.TAG_COMPOUND)) {
                CompoundTag player = (CompoundTag) value;
                if (player.hasUUID("Player")) {
                    data.arrived.add(player.getUUID("Player"));
                }
            }
        }
        return data;
    }

    public Collection<DungeonBossGateApi.Binding> bindings() {
        return List.copyOf(bindings.values());
    }

    public DungeonBossGateApi.Binding binding(BlockPos gate) {
        return bindings.get(gate);
    }

    public DungeonBossGateApi.Binding activeBinding() {
        return activeGate == null ? null : binding(activeGate);
    }

    public boolean engaged() {
        return activeGate != null;
    }

    public boolean arrived(UUID player) {
        return arrived.contains(player);
    }

    void synchronizeRound(UUID current) {
        if (!java.util.Objects.equals(roundId, current)) {
            roundId = current;
            activeGate = null;
            arrived.clear();
            setDirty();
        }
    }

    void put(DungeonBossGateApi.Binding binding) {
        if (engaged() || bindings.size() >= 64 && !bindings.containsKey(binding.gate())) {
            throw new IllegalStateException("Boss gates are locked during a Boss encounter, or the 64-gate limit was reached.");
        }
        bindings.put(binding.gate(), binding);
        setDirty();
    }

    boolean remove(BlockPos gate) {
        if (engaged()) {
            throw new IllegalStateException("Boss gate bindings cannot change during an encounter.");
        }
        boolean removed = bindings.remove(gate) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    void engage(BlockPos gate) {
        if (roundId == null || !bindings.containsKey(gate)) {
            throw new IllegalStateException("A Boss encounter requires a configured gate and an active dungeon round.");
        }
        activeGate = gate.immutable();
        setDirty();
    }

    void markArrived(UUID player) {
        if (arrived.add(player)) {
            setDirty();
        }
    }

    void forgetArrival(UUID player) {
        if (arrived.remove(player)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (DungeonBossGateApi.Binding binding : bindings.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Gate", binding.gate().asLong());
            entry.putString("Facing", binding.facing().getName());
            entry.putLong("Min", binding.minimum().asLong());
            entry.putLong("Max", binding.maximum().asLong());
            entry.putLong("Arrival", binding.arrival().asLong());
            entries.add(entry);
        }
        tag.put("Bindings", entries);
        if (roundId != null && activeGate != null) {
            tag.putUUID("Round", roundId);
            tag.putLong("ActiveGate", activeGate.asLong());
            ListTag players = new ListTag();
            for (UUID identity : arrived) {
                CompoundTag player = new CompoundTag();
                player.putUUID("Player", identity);
                players.add(player);
            }
            tag.put("Arrived", players);
        }
        return tag;
    }
}
