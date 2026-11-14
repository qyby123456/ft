/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class FoundationProfileFingerprints {
    private static final int CACHE_LIMIT = 128;
    private static final Map<FourWeaponFoundationRules.Profile, String> FINGERPRINTS = new ConcurrentHashMap<FourWeaponFoundationRules.Profile, String>();
    private static final Map<FourWeaponFoundationRules.Profile, String> LEGACY_FINGERPRINTS = new ConcurrentHashMap<FourWeaponFoundationRules.Profile, String>();

    private FoundationProfileFingerprints() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String fingerprint(FourWeaponFoundationRules.Profile profile) {
        String string = FINGERPRINTS.get(profile);
        if (string != null) {
            return string;
        }
        String string2 = FoundationProfileFingerprints.calculate(profile);
        Map<FourWeaponFoundationRules.Profile, String> map = FINGERPRINTS;
        synchronized (map) {
            if (FINGERPRINTS.size() < 128) {
                FINGERPRINTS.putIfAbsent(profile, string2);
            }
        }
        return string2;
    }

    public static boolean matches(String string, FourWeaponFoundationRules.Profile profile) {
        if (FoundationProfileFingerprints.fingerprint(profile).equals(string)) {
            return true;
        }
        return FoundationProfileFingerprints.legacyFingerprint(profile).equals(string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String legacyFingerprint(FourWeaponFoundationRules.Profile profile) {
        String string = LEGACY_FINGERPRINTS.get(profile);
        if (string != null) {
            return string;
        }
        EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values> enumMap = new EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values>(FoodWeaponBuildRules.Material.class);
        enumMap.putAll(profile.materials());
        enumMap.remove((Object)FoodWeaponBuildRules.Material.DIAMOND_ALLOY);
        FourWeaponFoundationRules.Profile profile2 = new FourWeaponFoundationRules.Profile(profile.id(), profile.family(), profile.axes(), profile.minimum(), profile.weights(), profile.bases(), profile.unlocks(), enumMap, profile.items(), profile.nativeCells(), profile.recipeEvidence(), profile.recipeBudget(), profile.environmentBudget());
        String string2 = FoundationProfileFingerprints.calculate(profile2, true);
        Map<FourWeaponFoundationRules.Profile, String> map = LEGACY_FINGERPRINTS;
        synchronized (map) {
            if (LEGACY_FINGERPRINTS.size() < 128) {
                LEGACY_FINGERPRINTS.putIfAbsent(profile, string2);
            }
        }
        return string2;
    }

    private static String calculate(FourWeaponFoundationRules.Profile profile) {
        return FoundationProfileFingerprints.calculate(profile, false);
    }

    private static String calculate(FourWeaponFoundationRules.Profile profile, boolean bl) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128365_("Profile", (Tag)FoundationCanonicalDigest.object(profile));
        ListTag listTag = new ListTag();
        for (FoodWeaponBuildRules.Material material : FoodWeaponBuildRules.Material.values()) {
            if (bl && material == FoodWeaponBuildRules.Material.DIAMOND_ALLOY) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128359_("Name", material.name());
            compoundTag2.m_128359_("Id", material.id());
            compoundTag2.m_128365_("Durability", (Tag)FoundationCanonicalDigest.object(material.durability));
            compoundTag2.m_128365_("Wear", (Tag)FoundationCanonicalDigest.object(material.wear));
            compoundTag2.m_128365_("Damage", (Tag)FoundationCanonicalDigest.object(material.damage));
            compoundTag2.m_128365_("Speed", (Tag)FoundationCanonicalDigest.object(material.speed));
            compoundTag2.m_128365_("Reach", (Tag)FoundationCanonicalDigest.object(material.reach));
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("Materials", (Tag)listTag);
        compoundTag.m_128405_("Schema", 2);
        return FoundationCanonicalDigest.fingerprint("profile-rules/foundation-build-v1", (Tag)compoundTag);
    }
}

