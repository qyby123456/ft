package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.Set;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** 已知旧版霜杖/酒桶弩的原生参考升级；只返回副本，保留实装物、已拆格、等级、磨损及旧备份。 */
public final class NonManorComponentMigrationRules {
    private NonManorComponentMigrationRules() {}

    public static CompoundTag reconcile(CompoundTag source, Profile current) {
        if (source == null || current == null) return null;
        if (source.getString("Rules").equals(FoundationProfileFingerprints.fingerprint(current))) return null;
        Profile previous = FourWeaponFoundationProfiles.previousBossProfile(current.id());
        if (previous == null || !source.contains("Rules", Tag.TAG_STRING)
                || !FoundationProfileFingerprints.matches(source.getString("Rules"), previous)) return null;
        if (source.getInt("Schema") != 2 || source.getInt("DigestVersion") != 3 || source.getInt("ProfileRevision") != 1
                || !source.getString("Profile").equals(current.id()) || !source.getBoolean("Initialized")
                || !source.contains("Revision", Tag.TAG_INT) || source.getInt("Revision") < 0
                || source.getInt("Revision") >= Integer.MAX_VALUE - 1 || !source.contains("Cells", Tag.TAG_LIST)
                || !source.contains("RetiredNative", Tag.TAG_LIST) || !source.contains("LegacyBackup", Tag.TAG_COMPOUND)) return null;
        ListTag oldCells = source.getList("Cells", Tag.TAG_COMPOUND);
        ListTag retired = source.getList("RetiredNative", Tag.TAG_INT);
        if (oldCells.size() > 27 || retired.size() > 27
                || ((ListTag) source.get("Cells")).size() != oldCells.size()
                || ((ListTag) source.get("RetiredNative")).size() != retired.size()) return null;
        Set<Integer> occupied = new HashSet<>();
        Set<Integer> paid = new HashSet<>();
        Set<Integer> removed = new HashSet<>();
        ListTag cells = new ListTag();
        for (int index = 0; index < oldCells.size(); index++) {
            CompoundTag entry = oldCells.getCompound(index);
            int slot = entry.getInt("Slot");
            if (!entry.contains("Slot", Tag.TAG_INT) || slot < 0 || slot >= 27 || !occupied.add(slot)) return null;
            if (entry.getString("Source").equals("native-reference")) {
                var old = previous.nativeCells().get(slot);
                if (old == null || entry.contains("Item") || !entry.getString("Id").equals(old.itemId())
                        || !entry.getString("Material").equals(old.material().id())) return null;
            } else if (entry.getString("Source").equals("player-paid") && entry.contains("Item", Tag.TAG_COMPOUND)) {
                CompoundTag item = entry.getCompound("Item");
                if (item.getInt("Count") != 1 || !item.getString("id").equals(entry.getString("Id"))) return null;
                paid.add(slot);
                cells.add(entry.copy());
            } else return null;
        }
        for (int index = 0; index < retired.size(); index++) {
            int slot = retired.getInt(index);
            if (slot < 0 || slot >= 27 || !removed.add(slot) || occupied.contains(slot) && !paid.contains(slot)) return null;
        }
        for (int slot : previous.nativeCells().keySet()) if (!occupied.contains(slot) && !removed.contains(slot)) return null;
        for (var component : current.nativeCells().entrySet()) {
            int slot = component.getKey();
            if (paid.contains(slot) || removed.contains(slot)) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.putString("Source", "native-reference");
            entry.putString("Id", component.getValue().itemId());
            entry.putString("Material", component.getValue().material().id());
            cells.add(entry);
        }
        CompoundTag result = source.copy();
        result.put("Cells", cells);
        result.putString("Rules", FoundationProfileFingerprints.fingerprint(current));
        result.putString("PreviousBossComponentRules", source.getString("Rules"));
        result.putInt("Revision", source.getInt("Revision") + 1);
        return result;
    }
}
