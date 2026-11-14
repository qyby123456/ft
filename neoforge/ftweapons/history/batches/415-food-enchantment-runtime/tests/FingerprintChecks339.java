package net.farmingtales.ftboss.item;

public final class FingerprintChecks339 {
    public static java.util.Map<String, Object> run() throws Exception {
        java.lang.reflect.Method before = FingerprintBefore339.class.getDeclaredMethod("append", java.security.MessageDigest.class, net.minecraft.nbt.Tag.class, int.class, int[].class);
        java.lang.reflect.Method after = FoodWeaponMaterialFingerprint.class.getDeclaredMethod("append", java.security.MessageDigest.class, net.minecraft.nbt.Tag.class, int.class, int[].class);
        before.setAccessible(true); after.setAccessible(true);
        int compared = 0;
        for (int variant = 0; variant < 6; variant++) {
            net.minecraft.nbt.CompoundTag original = fixture(variant);
            net.minecraft.nbt.CompoundTag current = fixture(variant);
            NativeNbtGraphObservation observation = NativeNbtGraphObservation.capture(current);
            java.security.MessageDigest digestBefore = java.security.MessageDigest.getInstance("SHA-256");
            java.security.MessageDigest digestAfter = java.security.MessageDigest.getInstance("SHA-256");
            before.invoke(null, digestBefore, original, 0, new int[]{0, 0});
            after.invoke(null, digestAfter, current, 0, new int[]{0, 0});
            if (!java.util.Arrays.equals(digestBefore.digest(), digestAfter.digest())) throw new AssertionError("Fingerprint mismatch " + variant);
            if (!observation.matches(current)) throw new AssertionError("Fingerprint exposed storage " + variant);
            compared++;
        }
        net.minecraft.nbt.CompoundTag current = fixture(0);
        java.util.Set<String> keys = NativeExpectedRawFields.detachedKeys(current);
        current.putInt("after_snapshot", 1);
        if (keys.contains("after_snapshot")) throw new AssertionError("Keys are live");
        boolean immutable = false;
        try { keys.clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
        if (!immutable) throw new AssertionError("Mutable snapshot");
        current.getAllKeys();
        if (!((NativeNbtMutationFields)current).ftboss$nativeNbtRawEscaped()) throw new AssertionError("Unknown alias not revoked");
        NativeExpectedRawFields.detachedKeys(current);
        if (!((NativeNbtMutationFields)current).ftboss$nativeNbtRawEscaped()) throw new AssertionError("Alias restored by helper");
        return java.util.Map.of("fingerprintVariantsEqual", compared, "readOnlyGraphUnchanged", true, "detachedImmutableKeys", true, "unknownAliasRemainsRevoked", true);
    }

    private static net.minecraft.nbt.CompoundTag fixture(int variant) {
        net.minecraft.nbt.CompoundTag result = new net.minecraft.nbt.CompoundTag();
        result.putString("unicode", "食武339");
        result.putByteArray("bytes", new byte[]{0, -1, (byte)variant});
        result.putIntArray("ints", new int[]{Integer.MIN_VALUE, variant, Integer.MAX_VALUE});
        result.putLongArray("longs", new long[]{Long.MIN_VALUE, variant, Long.MAX_VALUE});
        result.putDouble("number", variant == 0 ? -0.0 : variant + 0.125);
        net.minecraft.nbt.CompoundTag nested = new net.minecraft.nbt.CompoundTag(); nested.putInt("value", variant);
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag(); list.add(nested); result.put("list", list);
        return result;
    }
}
