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
import net.farmingtales.ftboss.util.SparrowAppearanceRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class ManorFightData
extends SavedData {
    private ListTag active = new ListTag();
    private ListTag deaths = new ListTag();
    private final Set<UUID> retired = new HashSet<UUID>();
    private final Set<UUID> sparrowVisits = new HashSet<UUID>();
    private final Map<UUID, Integer> sparrowPlans = new HashMap<UUID, Integer>();
    private final Map<UUID, Integer> sparrowStages = new HashMap<UUID, Integer>();

    public static ManorFightData get(MinecraftServer server) {
        return (ManorFightData)server.m_129783_().m_8895_().m_164861_(ManorFightData::load, ManorFightData::new, "ftboss_manor_fights");
    }

    private static ManorFightData load(CompoundTag tag) {
        CompoundTag record;
        ManorFightData data = new ManorFightData();
        data.active = tag.m_128437_("Active", 10).m_6426_();
        data.deaths = tag.m_128437_("Deaths", 10).m_6426_();
        for (Tag entry : tag.m_128437_("Retired", 10)) {
            record = (CompoundTag)entry;
            if (!record.m_128403_("Id")) continue;
            data.retired.add(record.m_128342_("Id"));
        }
        for (Tag entry : tag.m_128437_("SparrowVisits", 10)) {
            record = (CompoundTag)entry;
            if (!record.m_128403_("Id") || data.retired(record.m_128342_("Id"))) continue;
            data.sparrowVisits.add(record.m_128342_("Id"));
        }
        for (Tag entry : tag.m_128437_("SparrowRounds", 10)) {
            record = (CompoundTag)entry;
            if (!record.m_128403_("Id") || data.retired(record.m_128342_("Id"))) continue;
            UUID fight = record.m_128342_("Id");
            int visited = record.m_128451_("Visited") & 0xDFF;
            int planned = record.m_128451_("Planned") & 0xDFF;
            if (Integer.bitCount(planned) < 4) {
                planned = SparrowAppearanceRules.plan(fight);
            }
            data.sparrowPlans.put(fight, planned | visited);
            data.sparrowStages.put(fight, visited);
        }
        return data;
    }

    public ListTag active() {
        return this.active.m_6426_();
    }

    public ListTag deaths() {
        return this.deaths.m_6426_();
    }

    public void storeDeaths(ListTag records) {
        this.deaths = records.m_6426_();
        this.m_77762_();
    }

    public void store(ListTag fights) {
        this.active = fights.m_6426_();
        this.m_77762_();
    }

    public boolean retired(UUID fight) {
        return fight != null && this.retired.contains(fight);
    }

    public boolean sparrowVisited(UUID fight) {
        return fight != null && this.sparrowVisits.contains(fight);
    }

    public void visitSparrow(UUID fight) {
        if (fight != null && !this.retired(fight) && this.sparrowVisits.add(fight)) {
            this.m_77762_();
        }
    }

    public int sparrowPlan(UUID fight) {
        if (fight == null || this.retired(fight)) {
            return 0;
        }
        if (!this.sparrowPlans.containsKey(fight)) {
            this.sparrowPlans.put(fight, SparrowAppearanceRules.plan(fight));
            this.m_77762_();
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
        if (fight == null || this.retired(fight) || stage == 0 || this.sparrowVisited(fight, variant)) {
            return;
        }
        this.sparrowPlans.put(fight, this.sparrowPlan(fight) | stage);
        this.sparrowStages.merge(fight, stage, (previous, added) -> previous | added);
        this.visitSparrow(fight);
        this.m_77762_();
    }

    public void retire(UUID fight) {
        this.retired.add(fight);
        this.sparrowVisits.remove(fight);
        this.sparrowPlans.remove(fight);
        this.sparrowStages.remove(fight);
        this.m_77762_();
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        tag.m_128365_("Active", (Tag)this.active.m_6426_());
        tag.m_128365_("Deaths", (Tag)this.deaths.m_6426_());
        ListTag ended = new ListTag();
        for (UUID uUID : this.retired) {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Id", uUID);
            ended.add((Object)record);
        }
        tag.m_128365_("Retired", (Tag)ended);
        ListTag visits = new ListTag();
        for (UUID fight : this.sparrowVisits) {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Id", fight);
            visits.add((Object)record);
        }
        tag.m_128365_("SparrowVisits", (Tag)visits);
        ListTag listTag = new ListTag();
        for (Map.Entry<UUID, Integer> plan : this.sparrowPlans.entrySet()) {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Id", plan.getKey());
            record.m_128405_("Planned", plan.getValue().intValue());
            record.m_128405_("Visited", this.sparrowStages.getOrDefault(plan.getKey(), 0).intValue());
            listTag.add((Object)record);
        }
        tag.m_128365_("SparrowRounds", (Tag)listTag);
        return tag;
    }
}

