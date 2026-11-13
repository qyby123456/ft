package net.farmingtales.ftdungeonmobs;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class DungeonGateData extends SavedData {
    public static final int ADMISSION_TICKS = 5 * 60 * 20;
    private int tier = 1;
    private ResourceKey<Level> sourceDimension;
    private BlockPos sourceGate;
    private boolean arrivalBuilt;
    private boolean roundActive;
    private UUID roundId;
    private long admissionEndsAt;
    private final Set<UUID> participants = new LinkedHashSet<>();

    public static DungeonGateData get(MinecraftServer server) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Dungeon gate data requires the server thread.");
        }
        return server.overworld().getDataStorage().computeIfAbsent(DungeonGateData::load, DungeonGateData::new,
                "ftdungeonmobs_dungeon_gate");
    }

    public static DungeonGateData load(CompoundTag tag) {
        DungeonGateData data = new DungeonGateData();
        int storedTier = tag.getInt("Tier");
        data.tier = storedTier >= 1 && storedTier <= 5 ? storedTier : 1;
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("SourceDimension"));
        if (dimension != null && tag.contains("SourceGate", Tag.TAG_LONG)) {
            data.sourceDimension = ResourceKey.create(Registries.DIMENSION, dimension);
            data.sourceGate = BlockPos.of(tag.getLong("SourceGate"));
        }
        data.arrivalBuilt = tag.getBoolean("ArrivalBuilt");
        if (tag.getBoolean("RoundActive") && data.sourceDimension != null && data.sourceGate != null
                && tag.contains("AdmissionEndsAt", Tag.TAG_LONG) && tag.getLong("AdmissionEndsAt") >= 0) {
            data.roundActive = true;
            data.roundId = tag.hasUUID("RoundId") ? tag.getUUID("RoundId") : UUID.randomUUID();
            if (!tag.hasUUID("RoundId")) {
                data.setDirty();
            }
            data.admissionEndsAt = tag.getLong("AdmissionEndsAt");
            for (net.minecraft.nbt.Tag value : tag.getList("Participants", Tag.TAG_COMPOUND)) {
                CompoundTag participant = (CompoundTag) value;
                if (participant.hasUUID("Player")) {
                    data.participants.add(participant.getUUID("Player"));
                }
            }
        }
        return data;
    }

    public int tier() {
        return tier;
    }

    public ResourceKey<Level> sourceDimension() {
        return sourceDimension;
    }

    public BlockPos sourceGate() {
        return sourceGate;
    }

    public boolean matches(ResourceKey<Level> dimension, BlockPos position) {
        return dimension.equals(sourceDimension) && position.equals(sourceGate);
    }

    public void bind(ResourceKey<Level> dimension, BlockPos position) {
        if (roundActive && !matches(dimension, position)) {
            throw new IllegalStateException("An active dungeon round cannot change its entry gate.");
        }
        if (!matches(dimension, position)) {
            sourceDimension = dimension;
            sourceGate = position.immutable();
            setDirty();
        }
    }

    public boolean setTier(int value) {
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("Dungeon tier must be 1..5.");
        }
        if (tier == value) {
            return false;
        }
        if (roundActive) {
            throw new IllegalStateException("Dungeon difficulty is locked for the active round.");
        }
        tier = value;
        setDirty();
        return true;
    }

    public boolean arrivalBuilt() {
        return arrivalBuilt;
    }

    public boolean roundActive() {
        return roundActive;
    }

    public UUID roundId() {
        return roundId;
    }

    public boolean locksBrazier(ResourceKey<Level> dimension, BlockPos position) {
        return roundActive && sourceGate != null && dimension.equals(sourceDimension)
                && position.distSqr(sourceGate) <= DungeonGateTravel.BRAZIER_RADIUS * DungeonGateTravel.BRAZIER_RADIUS;
    }

    public boolean admissionOpen(long time) {
        return !roundActive || time >= 0 && time < admissionEndsAt;
    }

    public int admissionSeconds(long time) {
        if (!roundActive || time >= admissionEndsAt) {
            return 0;
        }
        return (int) ((Math.min(ADMISSION_TICKS, admissionEndsAt - Math.max(0, time)) + 19) / 20);
    }

    public void startRound(UUID player, long time) {
        if (roundActive || sourceDimension == null || sourceGate == null || time < 0
                || time > Long.MAX_VALUE - ADMISSION_TICKS) {
            throw new IllegalStateException("A dungeon round requires a bound gate and a valid start time.");
        }
        roundActive = true;
        roundId = UUID.randomUUID();
        admissionEndsAt = time + ADMISSION_TICKS;
        participants.clear();
        participants.add(player);
        setDirty();
    }

    public void addParticipant(UUID player) {
        if (roundActive && participants.add(player)) {
            setDirty();
        }
    }

    public void removeParticipant(UUID player) {
        if (participants.remove(player)) {
            setDirty();
        }
    }

    public boolean hasParticipant(UUID player) {
        return participants.contains(player);
    }

    public int participantCount() {
        return participants.size();
    }

    public boolean finishRoundIfEmpty(long time) {
        if (!roundActive || admissionOpen(time) || !participants.isEmpty()) {
            return false;
        }
        roundActive = false;
        admissionEndsAt = 0;
        setDirty();
        return true;
    }

    public void markArrivalBuilt() {
        arrivalBuilt = true;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Tier", tier);
        tag.putBoolean("ArrivalBuilt", arrivalBuilt);
        tag.putBoolean("RoundActive", roundActive);
        if (roundId != null) {
            tag.putUUID("RoundId", roundId);
        }
        tag.putLong("AdmissionEndsAt", admissionEndsAt);
        ListTag players = new ListTag();
        for (UUID player : participants) {
            CompoundTag participant = new CompoundTag();
            participant.putUUID("Player", player);
            players.add(participant);
        }
        tag.put("Participants", players);
        if (sourceDimension != null && sourceGate != null) {
            tag.putString("SourceDimension", sourceDimension.location().toString());
            tag.putLong("SourceGate", sourceGate.asLong());
        }
        return tag;
    }
}
