package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.world.item.ItemStack;

public final class FourWeaponFoundationAdapter {
    public static final int SCHEMA = 2;
    public static final int DIGEST_VERSION = 3;
    private static Profile nativeProfile(ItemStack weapon) { throw new UnsupportedOperationException(); }
    public static boolean applies(ItemStack weapon) { throw new UnsupportedOperationException(); }

    public static int durabilityAlloys(ItemStack weapon) {
        Profile profile = nativeProfile(weapon);
        return profile == null ? 0 : WeaponDurabilityBuild.foundationAlloys(weapon, profile);
    }
}
