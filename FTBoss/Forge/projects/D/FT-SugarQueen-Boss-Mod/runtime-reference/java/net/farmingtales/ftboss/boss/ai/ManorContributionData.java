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
package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.ManorRaidLedger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class ManorContributionData
extends SavedData {
    private final Map<UUID, ManorRaidLedger> fights = new HashMap<UUID, ManorRaidLedger>();

    public static ManorContributionData get(MinecraftServer server) {
        return (ManorContributionData)server.m_129783_().m_8895_().m_164861_(ManorContributionData::load, ManorContributionData::new, "ftboss_manor_contributions");
    }

    public ManorRaidLedger observe(UUID fight, UUID boss) {
        ManorRaidLedger ledger = this.fights.computeIfAbsent(fight, ignored -> new ManorRaidLedger());
        if (ledger.enter(boss)) {
            this.m_77762_();
        }
        return ledger;
    }

    public void prune(Predicate<UUID> active) {
        if (this.fights.keySet().removeIf(fight -> !active.test((UUID)fight))) {
            this.m_77762_();
        }
    }

    public static ManorContributionData load(CompoundTag tag) {
        ManorContributionData data = new ManorContributionData();
        for (Tag value : tag.m_128437_("Fights", 10)) {
            CompoundTag record = (CompoundTag)value;
            if (!record.m_128403_("Fight") || !record.m_128403_("Stage")) continue;
            ManorRaidLedger ledger = ManorRaidLedger.restore(record.m_128342_("Stage"), ManorContributionData.readScores(record.m_128437_("Current", 10)), ManorContributionData.readScores(record.m_128437_("Previous", 10)), record.m_128471_("BonusConsumed"));
            ledger.restoreDamageMix(record.m_128459_("CurrentPhysical"), record.m_128459_("CurrentMagic"), record.m_128459_("PreviousPhysical"), record.m_128459_("PreviousMagic"));
            data.fights.put(record.m_128342_("Fight"), ledger);
        }
        return data;
    }

    private static Map<UUID, Double> readScores(ListTag list) {
        HashMap<UUID, Double> scores = new HashMap<UUID, Double>();
        for (Tag value : list) {
            CompoundTag record = (CompoundTag)value;
            double damage = record.m_128459_("Damage");
            if (!record.m_128403_("Player") || !Double.isFinite(damage) || !(damage > 0.0)) continue;
            scores.put(record.m_128342_("Player"), damage);
        }
        return scores;
    }

    private static ListTag writeScores(Map<UUID, Double> scores) {
        ListTag list = new ListTag();
        scores.forEach((player, damage) -> {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Player", player);
            record.m_128347_("Damage", damage.doubleValue());
            list.add((Object)record);
        });
        return list;
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        ListTag list = new ListTag();
        this.fights.forEach((fight, ledger) -> {
            if (ledger.stage() == null) {
                return;
            }
            CompoundTag record = new CompoundTag();
            record.m_128362_("Fight", fight);
            record.m_128362_("Stage", ledger.stage());
            record.m_128379_("BonusConsumed", ledger.bonusConsumed());
            record.m_128347_("CurrentPhysical", ledger.currentPhysical());
            record.m_128347_("CurrentMagic", ledger.currentMagic());
            record.m_128347_("PreviousPhysical", ledger.previousPhysical());
            record.m_128347_("PreviousMagic", ledger.previousMagic());
            record.m_128365_("Current", (Tag)ManorContributionData.writeScores(ledger.current()));
            record.m_128365_("Previous", (Tag)ManorContributionData.writeScores(ledger.previous()));
            list.add((Object)record);
        });
        tag.m_128365_("Fights", (Tag)list);
        return tag;
    }
}

