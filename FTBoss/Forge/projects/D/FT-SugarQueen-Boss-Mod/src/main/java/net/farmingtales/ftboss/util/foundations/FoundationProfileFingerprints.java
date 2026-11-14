package net.farmingtales.ftboss.util.foundations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** 不可变基础配置的完整值指纹；2026-10-20 有界复用纯规则计算，不保存物品状态、权限或可用性结论。 */
public final class FoundationProfileFingerprints {
    private static final int CACHE_LIMIT = 128;
    private static final Map<Profile, String> FINGERPRINTS = new ConcurrentHashMap<>();
    private static final Map<Profile, String> LEGACY_FINGERPRINTS = new ConcurrentHashMap<>();
    private FoundationProfileFingerprints() {}

    public static String fingerprint(Profile profile) {
        String cached = FINGERPRINTS.get(profile);
        if (cached != null) return cached;
        String calculated = calculate(profile);
        synchronized (FINGERPRINTS) {
            if (FINGERPRINTS.size() < CACHE_LIMIT) FINGERPRINTS.putIfAbsent(profile, calculated);
        }
        return calculated;
    }

    public static boolean matches(String stored, Profile profile) {
        if (fingerprint(profile).equals(stored)) return true;
        return legacyFingerprint(profile).equals(stored);
    }

    public static String legacyFingerprint(Profile profile) {
        String cached = LEGACY_FINGERPRINTS.get(profile);
        if (cached != null) return cached;
        Map<Material, FourWeaponFoundationRules.Values> materials = new java.util.EnumMap<>(Material.class);
        materials.putAll(profile.materials());
        materials.remove(Material.DIAMOND_ALLOY);
        Profile legacy = new Profile(profile.id(), profile.family(), profile.axes(), profile.minimum(), profile.weights(), profile.bases(),
                profile.unlocks(), materials, profile.items(), profile.nativeCells(), profile.recipeEvidence(), profile.recipeBudget(), profile.environmentBudget());
        String calculated = calculate(legacy, true);
        synchronized (LEGACY_FINGERPRINTS) {
            if (LEGACY_FINGERPRINTS.size() < CACHE_LIMIT) LEGACY_FINGERPRINTS.putIfAbsent(profile, calculated);
        }
        return calculated;
    }

    private static String calculate(Profile profile) { return calculate(profile, false); }

    private static String calculate(Profile profile, boolean legacy) {
        CompoundTag rules = new CompoundTag();
        rules.put("Profile", FoundationCanonicalDigest.object(profile));
        ListTag materials = new ListTag();
        for (Material material : Material.values()) {
            if (legacy && material == Material.DIAMOND_ALLOY) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", material.name());
            entry.putString("Id", material.id());
            entry.put("Durability", FoundationCanonicalDigest.object(material.durability));
            entry.put("Wear", FoundationCanonicalDigest.object(material.wear));
            entry.put("Damage", FoundationCanonicalDigest.object(material.damage));
            entry.put("Speed", FoundationCanonicalDigest.object(material.speed));
            entry.put("Reach", FoundationCanonicalDigest.object(material.reach));
            materials.add(entry);
        }
        rules.put("Materials", materials);
        rules.putInt("Schema", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.SCHEMA);
        return FoundationCanonicalDigest.fingerprint("profile-rules/foundation-build-v1", rules);
    }
}
