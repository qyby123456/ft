package net.farmingtales.ftboss.util.foundations;

import java.io.DataOutputStream;
import java.io.IOException;

public final class FoundationCanonicalDigest {
    public static native net.minecraft.nbt.CompoundTag object(Object value);
    public static native String fingerprint(String domain, net.minecraft.nbt.Tag value);
    private static void string(String value, DataOutputStream output) throws IOException {
        FoundationStringEncoding.write(value, output, 1048576);
    }
}
