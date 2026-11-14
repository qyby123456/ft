package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;

public final class FourWeaponFoundationAdapter {
    public static final int SCHEMA = 2;
    public static native boolean applies(net.minecraft.world.item.ItemStack stack);
    public static native boolean blocksUse(net.minecraft.world.item.ItemStack stack);
    public static native Profile profile(net.minecraft.world.item.ItemStack stack);
    private static String profileFingerprint(Profile profile) {
        return FoundationProfileFingerprints.fingerprint(profile);
    }
}
