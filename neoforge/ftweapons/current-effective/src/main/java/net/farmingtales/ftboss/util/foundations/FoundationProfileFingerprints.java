package net.farmingtales.ftboss.util.foundations;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public final class FoundationProfileFingerprints {
    private static final int CACHE_LIMIT = 128;
    private static final Map<Profile, String> FINGERPRINTS = new ConcurrentHashMap<>();
    private static final Map<Profile, String> LEGACY_FINGERPRINTS = new ConcurrentHashMap<>();

    private FoundationProfileFingerprints() {
    }

    public static String fingerprint(Profile var0) {
        String var1 = FINGERPRINTS.get(var0);
        if (var1 != null) {
            return var1;
        } else {
            String var2 = calculate(var0);
            synchronized (FINGERPRINTS) {
                if (FINGERPRINTS.size() < 128) {
                    FINGERPRINTS.putIfAbsent(var0, var2);
                }

                return var2;
            }
        }
    }

    public static boolean matches(String var0, Profile var1) {
        return fingerprint(var1).equals(var0) ? true : legacyFingerprint(var1).equals(var0);
    }

    public static String legacyFingerprint(Profile var0) {
        String var1 = LEGACY_FINGERPRINTS.get(var0);
        if (var1 != null) {
            return var1;
        } else {
            EnumMap var2 = new EnumMap<>(Material.class);
            var2.putAll(var0.materials());
            var2.remove(Material.DIAMOND_ALLOY);
            Profile var3 = new Profile(
                var0.id(),
                var0.family(),
                var0.axes(),
                var0.minimum(),
                var0.weights(),
                var0.bases(),
                var0.unlocks(),
                var2,
                var0.items(),
                var0.nativeCells(),
                var0.recipeEvidence(),
                var0.recipeBudget(),
                var0.environmentBudget()
            );
            String var4 = calculate(var3, true);
            synchronized (LEGACY_FINGERPRINTS) {
                if (LEGACY_FINGERPRINTS.size() < 128) {
                    LEGACY_FINGERPRINTS.putIfAbsent(var0, var4);
                }

                return var4;
            }
        }
    }

    private static String calculate(Profile var0) {
        return calculate(var0, false);
    }

    private static String calculate(Profile var0, boolean var1) {
        CompoundTag var2 = new CompoundTag();
        var2.put("Profile", FoundationCanonicalDigest.object(var0));
        ListTag var3 = new ListTag();

        for (Material var7 : Material.values()) {
            if (!var1 || var7 != Material.DIAMOND_ALLOY) {
                CompoundTag var8 = new CompoundTag();
                var8.putString("Name", var7.name());
                var8.putString("Id", var7.id());
                var8.put("Durability", FoundationCanonicalDigest.object(var7.durability));
                var8.put("Wear", FoundationCanonicalDigest.object(var7.wear));
                var8.put("Damage", FoundationCanonicalDigest.object(var7.damage));
                var8.put("Speed", FoundationCanonicalDigest.object(var7.speed));
                var8.put("Reach", FoundationCanonicalDigest.object(var7.reach));
                var3.add(var8);
            }
        }

        var2.put("Materials", var3);
        var2.putInt("Schema", 2);
        return FoundationCanonicalDigest.fingerprint("profile-rules/foundation-build-v1", var2);
    }
}
