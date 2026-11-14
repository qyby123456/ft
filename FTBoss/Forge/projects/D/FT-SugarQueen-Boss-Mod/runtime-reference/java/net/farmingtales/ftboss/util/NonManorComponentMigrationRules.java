/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class NonManorComponentMigrationRules {
    private NonManorComponentMigrationRules() {
    }

    public static CompoundTag reconcile(CompoundTag compoundTag, FourWeaponFoundationRules.Profile profile) {
        FourWeaponFoundationRules.Cell cell;
        int n;
        int n2;
        if (compoundTag == null || profile == null) {
            return null;
        }
        if (compoundTag.m_128461_("Rules").equals(FoundationProfileFingerprints.fingerprint(profile))) {
            return null;
        }
        FourWeaponFoundationRules.Profile profile2 = FourWeaponFoundationProfiles.previousBossProfile(profile.id());
        if (profile2 == null || !compoundTag.m_128425_("Rules", 8) || !FoundationProfileFingerprints.matches(compoundTag.m_128461_("Rules"), profile2)) {
            return null;
        }
        if (!(compoundTag.m_128451_("Schema") == 2 && compoundTag.m_128451_("DigestVersion") == 3 && compoundTag.m_128451_("ProfileRevision") == 1 && compoundTag.m_128461_("Profile").equals(profile.id()) && compoundTag.m_128471_("Initialized") && compoundTag.m_128425_("Revision", 3) && compoundTag.m_128451_("Revision") >= 0 && compoundTag.m_128451_("Revision") < 0x7FFFFFFE && compoundTag.m_128425_("Cells", 9) && compoundTag.m_128425_("RetiredNative", 9) && compoundTag.m_128425_("LegacyBackup", 10))) {
            return null;
        }
        ListTag listTag = compoundTag.m_128437_("Cells", 10);
        ListTag listTag2 = compoundTag.m_128437_("RetiredNative", 3);
        if (listTag.size() > 27 || listTag2.size() > 27 || ((ListTag)compoundTag.m_128423_("Cells")).size() != listTag.size() || ((ListTag)compoundTag.m_128423_("RetiredNative")).size() != listTag2.size()) {
            return null;
        }
        HashSet<Integer> hashSet = new HashSet<Integer>();
        HashSet<Integer> hashSet2 = new HashSet<Integer>();
        HashSet<Integer> hashSet3 = new HashSet<Integer>();
        ListTag listTag3 = new ListTag();
        for (n2 = 0; n2 < listTag.size(); ++n2) {
            CompoundTag compoundTag2 = listTag.m_128728_(n2);
            n = compoundTag2.m_128451_("Slot");
            if (!compoundTag2.m_128425_("Slot", 3) || n < 0 || n >= 27 || !hashSet.add(n)) {
                return null;
            }
            if (compoundTag2.m_128461_("Source").equals("native-reference")) {
                cell = profile2.nativeCells().get(n);
                if (cell != null && !compoundTag2.m_128441_("Item") && compoundTag2.m_128461_("Id").equals(cell.itemId()) && compoundTag2.m_128461_("Material").equals(cell.material().id())) continue;
                return null;
            }
            if (compoundTag2.m_128461_("Source").equals("player-paid") && compoundTag2.m_128425_("Item", 10)) {
                cell = compoundTag2.m_128469_("Item");
                if (cell.m_128451_("Count") != 1 || !cell.m_128461_("id").equals(compoundTag2.m_128461_("Id"))) {
                    return null;
                }
                hashSet2.add(n);
                listTag3.add((Object)compoundTag2.m_6426_());
                continue;
            }
            return null;
        }
        for (n2 = 0; n2 < listTag2.size(); ++n2) {
            int n3 = listTag2.m_128763_(n2);
            if (n3 >= 0 && n3 < 27 && hashSet3.add(n3) && (!hashSet.contains(n3) || hashSet2.contains(n3))) continue;
            return null;
        }
        Iterator<Object> iterator = profile2.nativeCells().keySet().iterator();
        while (iterator.hasNext()) {
            int n4 = iterator.next();
            if (hashSet.contains(n4) || hashSet3.contains(n4)) continue;
            return null;
        }
        for (Map.Entry entry : profile.nativeCells().entrySet()) {
            n = (Integer)entry.getKey();
            if (hashSet2.contains(n) || hashSet3.contains(n)) continue;
            cell = new CompoundTag();
            cell.m_128405_("Slot", n);
            cell.m_128359_("Source", "native-reference");
            cell.m_128359_("Id", ((FourWeaponFoundationRules.Cell)entry.getValue()).itemId());
            cell.m_128359_("Material", ((FourWeaponFoundationRules.Cell)entry.getValue()).material().id());
            listTag3.add((Object)cell);
        }
        iterator = compoundTag.m_6426_();
        iterator.m_128365_("Cells", (Tag)listTag3);
        iterator.m_128359_("Rules", FoundationProfileFingerprints.fingerprint(profile));
        iterator.m_128359_("PreviousBossComponentRules", compoundTag.m_128461_("Rules"));
        iterator.m_128405_("Revision", compoundTag.m_128451_("Revision") + 1);
        return iterator;
    }
}

