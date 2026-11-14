package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;

/** 运行时方法移植模板；2026-10-20 只替换作者规则快照，不改武器解析与交易校验。 */
public final class FourWeaponFoundationAdapter {
    public static final class View {
        public native boolean valid();
        public native Profile profile();
        public native Map<Integer, Entry> entries();
    }

    public static final class Entry {
        public native boolean paid();
        public native ItemStack storedItem();
    }

    public static native View read(ItemStack weapon);
    private static native String profileFingerprint(Profile profile);

    public static String rulesFingerprint(ItemStack weapon) {
        View view = read(weapon);
        if (!view.valid()) return null;
        try {
            java.util.List<Object> definitions = new ArrayList<>();
            for (int index : new TreeSet<>(view.entries().keySet())) {
                Entry entry = view.entries().get(index);
                if (entry.paid()) definitions.add(Map.of("Slot", index, "Definition", net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(entry.storedItem())));
            }
            return FoundationAuthorRuleSnapshots.fingerprint(weapon, profileFingerprint(view.profile()), FoundationCanonicalDigest.object(definitions));
        } catch (IllegalArgumentException rejected) {
            return null;
        }
    }

    private static CompoundTag authorRulesSnapshot(ItemStack weapon) {
        return FoundationAuthorRuleSnapshots.snapshot(weapon);
    }
}
