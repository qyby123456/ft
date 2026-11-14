package net.farmingtales.ftboss.boss;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.util.SparrowAppearanceRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class ManorFightData extends SavedData {
    private ListTag active = new ListTag();
    private ListTag deaths = new ListTag();
    private final Set<UUID> retired = new HashSet<>();
    private final Set<UUID> sparrowVisits = new HashSet<>();
    private final Map<UUID, Integer> sparrowPlans = new HashMap<>();
    private final Map<UUID, Integer> sparrowStages = new HashMap<>();

    public static ManorFightData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ManorFightData::load, ManorFightData::new, "ftboss_manor_fights");
    }

    private static ManorFightData load(CompoundTag tag) {
        ManorFightData data = new ManorFightData();
        data.active = tag.getList("Active", Tag.TAG_COMPOUND).copy();
        data.deaths = tag.getList("Deaths", Tag.TAG_COMPOUND).copy();
        for (Tag entry : tag.getList("Retired", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Id")) data.retired.add(record.getUUID("Id"));
        }
        for (Tag entry : tag.getList("SparrowVisits", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Id") && !data.retired(record.getUUID("Id"))) {
                data.sparrowVisits.add(record.getUUID("Id"));
            }
        }
        for (Tag entry : tag.getList("SparrowRounds", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            if (!record.hasUUID("Id") || data.retired(record.getUUID("Id"))) continue;
            UUID fight = record.getUUID("Id");
            int visited = record.getInt("Visited") & SparrowAppearanceRules.VALID_MASK;
            int planned = record.getInt("Planned") & SparrowAppearanceRules.VALID_MASK;
            if (Integer.bitCount(planned) < SparrowAppearanceRules.MIN_APPEARANCES) planned = SparrowAppearanceRules.plan(fight);
            data.sparrowPlans.put(fight, planned | visited);
            data.sparrowStages.put(fight, visited);
        }
        return data;
    }

    public ListTag active() { return this.active.copy(); }

    public ListTag deaths() { return this.deaths.copy(); }

    public void storeDeaths(ListTag records) { this.deaths = records.copy(); this.setDirty(); }

    public void store(ListTag fights) {
        this.active = fights.copy();
        this.setDirty();
    }

    public boolean retired(UUID fight) { return fight != null && this.retired.contains(fight); }

    public boolean sparrowVisited(UUID fight) { return fight != null && this.sparrowVisits.contains(fight); }

    public void visitSparrow(UUID fight) {
        if (fight != null && !this.retired(fight) && this.sparrowVisits.add(fight)) this.setDirty();
    }

    public int sparrowPlan(UUID fight) {
        if (fight == null || this.retired(fight)) return 0;
        if (!this.sparrowPlans.containsKey(fight)) {
            this.sparrowPlans.put(fight, SparrowAppearanceRules.plan(fight));
            this.setDirty();
        }
        return this.sparrowPlans.get(fight);
    }

    public boolean sparrowPlanned(UUID fight, int variant) {
        return (this.sparrowPlan(fight) & SparrowAppearanceRules.bit(variant)) != 0;
    }

    public boolean sparrowVisited(UUID fight, int variant) {
        return (this.sparrowStages.getOrDefault(fight, 0) & SparrowAppearanceRules.bit(variant)) != 0;
    }

    public void visitSparrow(UUID fight, int variant) {
        int stage = SparrowAppearanceRules.bit(variant);
        if (fight == null || this.retired(fight) || stage == 0 || this.sparrowVisited(fight, variant)) return;
        this.sparrowPlans.put(fight, this.sparrowPlan(fight) | stage);
        this.sparrowStages.merge(fight, stage, (previous, added) -> previous | added);
        this.visitSparrow(fight);
        this.setDirty();
    }

    public void retire(UUID fight) {
        this.retired.add(fight);
        this.sparrowVisits.remove(fight);
        this.sparrowPlans.remove(fight);
        this.sparrowStages.remove(fight);
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("Active", this.active.copy());
        tag.put("Deaths", this.deaths.copy());
        ListTag ended = new ListTag();
        for (UUID fight : this.retired) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", fight);
            ended.add(record);
        }
        tag.put("Retired", ended);
        ListTag visits = new ListTag();
        for (UUID fight : this.sparrowVisits) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", fight);
            visits.add(record);
        }
        tag.put("SparrowVisits", visits);
        ListTag rounds = new ListTag();
        for (Map.Entry<UUID, Integer> plan : this.sparrowPlans.entrySet()) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", plan.getKey());
            record.putInt("Planned", plan.getValue());
            record.putInt("Visited", this.sparrowStages.getOrDefault(plan.getKey(), 0));
            rounds.add(record);
        }
        tag.put("SparrowRounds", rounds);
        return tag;
    }
}
