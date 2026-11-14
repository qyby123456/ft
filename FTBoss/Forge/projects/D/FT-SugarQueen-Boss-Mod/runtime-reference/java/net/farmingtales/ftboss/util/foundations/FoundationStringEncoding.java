/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FoundationStringEncoding {
    private static final int CACHE_ENTRIES = 1024;
    private static final int CACHE_STRING_LENGTH = 128;
    private static final Map<String, byte[]> ENCODINGS = new ConcurrentHashMap<String, byte[]>();

    private FoundationStringEncoding() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(String string, DataOutputStream dataOutputStream, int n) throws IOException {
        if (string.length() > n) {
            throw new IllegalArgumentException("foundation-string-budget");
        }
        byte[] byArray = ENCODINGS.get(string);
        if (byArray == null) {
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (!(Character.isHighSurrogate(c) ? ++i >= string.length() || !Character.isLowSurrogate(string.charAt(i)) : Character.isLowSurrogate(c))) continue;
                throw new IllegalArgumentException("foundation-malformed-UTF-16");
            }
            byArray = string.getBytes(StandardCharsets.UTF_8);
            if (string.length() <= 128) {
                Map<String, byte[]> map = ENCODINGS;
                synchronized (map) {
                    if (ENCODINGS.size() < 1024) {
                        ENCODINGS.putIfAbsent(string, byArray);
                    }
                }
            }
        }
        if (byArray.length > n - dataOutputStream.size() - 4) {
            throw new IllegalArgumentException("foundation-byte-budget");
        }
        dataOutputStream.writeInt(byArray.length);
        dataOutputStream.write(byArray);
    }
}

