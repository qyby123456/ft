package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** 庄园输出账本独立SavedData；2026-10-15：跨区块和服务器重载保存每轮输出及已领取仇恨，战斗结束清理。 */
public final class ManorContributionData extends SavedData {
    private final Map<UUID, ManorRaidLedger> fights = new HashMap<>();

    public static ManorContributionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                ManorContributionData::load, ManorContributionData::new, "ftboss_manor_contributions");
    }

    public ManorRaidLedger observe(UUID fight, UUID boss) {
        ManorRaidLedger ledger = this.fights.computeIfAbsent(fight, ignored -> new ManorRaidLedger());
        if (ledger.enter(boss)) this.setDirty();
        return ledger;
    }

    public void prune(Predicate<UUID> active) {
        if (this.fights.keySet().removeIf(fight -> !active.test(fight))) this.setDirty();
    }

    public static ManorContributionData load(CompoundTag tag) {
        ManorContributionData data = new ManorContributionData();
        for (Tag value : tag.getList("Fights", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) value;
            if (!record.hasUUID("Fight") || !record.hasUUID("Stage")) continue;
            ManorRaidLedger ledger = ManorRaidLedger.restore(record.getUUID("Stage"),
                    readScores(record.getList("Current", Tag.TAG_COMPOUND)),
                    readScores(record.getList("Previous", Tag.TAG_COMPOUND)), record.getBoolean("BonusConsumed"));
            ledger.restoreDamageMix(record.getDouble("CurrentPhysical"), record.getDouble("CurrentMagic"),
                    record.getDouble("PreviousPhysical"), record.getDouble("PreviousMagic"));
            data.fights.put(record.getUUID("Fight"), ledger);
        }
        return data;
    }

    private static Map<UUID, Double> readScores(ListTag list) {
        Map<UUID, Double> scores = new HashMap<>();
        for (Tag value : list) {
            CompoundTag record = (CompoundTag) value;
            double damage = record.getDouble("Damage");
            if (record.hasUUID("Player") && Double.isFinite(damage) && damage > 0.0D) {
                scores.put(record.getUUID("Player"), damage);
            }
        }
        return scores;
    }

    private static ListTag writeScores(Map<UUID, Double> scores) {
        ListTag list = new ListTag();
        scores.forEach((player, damage) -> {
            CompoundTag record = new CompoundTag();
            record.putUUID("Player", player);
            record.putDouble("Damage", damage);
            list.add(record);
        });
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        this.fights.forEach((fight, ledger) -> {
            if (ledger.stage() == null) return;
            CompoundTag record = new CompoundTag();
            record.putUUID("Fight", fight);
            record.putUUID("Stage", ledger.stage());
            record.putBoolean("BonusConsumed", ledger.bonusConsumed());
            record.putDouble("CurrentPhysical", ledger.currentPhysical());
            record.putDouble("CurrentMagic", ledger.currentMagic());
            record.putDouble("PreviousPhysical", ledger.previousPhysical());
            record.putDouble("PreviousMagic", ledger.previousMagic());
            record.put("Current", writeScores(ledger.current()));
            record.put("Previous", writeScores(ledger.previous()));
            list.add(record);
        });
        tag.put("Fights", list);
        return tag;
    }
}
