package net.farmingtales.ftboss.util.foundations;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 规范指纹的有界 UTF-8 编码复用；2026-10-20 保留严格代理项和输出预算校验，减少重复编码与逐字节写入。 */
public final class FoundationStringEncoding {
    private static final int CACHE_ENTRIES = 1024;
    private static final int CACHE_STRING_LENGTH = 128;
    private static final Map<String, byte[]> ENCODINGS = new ConcurrentHashMap<>();

    private FoundationStringEncoding() {}

    public static void write(String value, DataOutputStream output, int maximumBytes) throws IOException {
        if (value.length() > maximumBytes) throw new IllegalArgumentException("foundation-string-budget");
        byte[] encoded = ENCODINGS.get(value);
        if (encoded == null) {
            for (int index = 0; index < value.length(); index++) {
                char character = value.charAt(index);
                if (Character.isHighSurrogate(character)) {
                    if (++index >= value.length() || !Character.isLowSurrogate(value.charAt(index))) {
                        throw new IllegalArgumentException("foundation-malformed-UTF-16");
                    }
                } else if (Character.isLowSurrogate(character)) {
                    throw new IllegalArgumentException("foundation-malformed-UTF-16");
                }
            }
            encoded = value.getBytes(StandardCharsets.UTF_8);
            if (value.length() <= CACHE_STRING_LENGTH) {
                synchronized (ENCODINGS) {
                    if (ENCODINGS.size() < CACHE_ENTRIES) ENCODINGS.putIfAbsent(value, encoded);
                }
            }
        }
        if (encoded.length > maximumBytes - output.size() - Integer.BYTES) throw new IllegalArgumentException("foundation-byte-budget");
        output.writeInt(encoded.length);
        output.write(encoded);
    }
}
