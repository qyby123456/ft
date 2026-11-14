package net.farmingtales.ftboss.util.foundations;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Sequence;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;

public final class FoundationCanonicalDigest {
    public static final int VERSION = 3;
    public static final int MAX_DEPTH = 64;
    public static final int MAX_NODES = 65536;
    public static final int MAX_BYTES = 1048576;
    private static final Set<Class<?>> TYPES = Set.of(
        CompoundTag.class,
        ListTag.class,
        StringTag.class,
        ByteTag.class,
        ShortTag.class,
        IntTag.class,
        LongTag.class,
        FloatTag.class,
        DoubleTag.class,
        ByteArrayTag.class,
        IntArrayTag.class,
        LongArrayTag.class,
        EndTag.class
    );

    private FoundationCanonicalDigest() {
    }

    public static String fingerprint(String var0, Tag var1) {
        PrivateFingerprint416 var2 = new PrivateFingerprint416();
        var2.putString("Domain", var0);
        var2.putInt("DigestVersion", 3);
        if (var1 == null) {
            throw new IllegalArgumentException("null-digest-input");
        } else {
            var2.put("Value", var1);

            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical(var2.finish())));
            } catch (NoSuchAlgorithmException var4) {
                throw new IllegalStateException(var4);
            }
        }
    }

    public static byte[] canonical(Tag var0) {
        return OwnerFieldExpectedAuthority.foundationCanonicalRead(() -> canonicalTrusted(var0));
    }

    private static byte[] canonicalTrusted(Tag var0) {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();

        try {
            byte[] var3;
            try (DataOutputStream var2 = new DataOutputStream(var1)) {
                var2.writeInt(1180124740);
                var2.writeInt(3);
                append(var0, var2, 0, new int[]{0});
                var3 = var1.toByteArray();
            }

            return var3;
        } catch (IOException var7) {
            throw new IllegalStateException(var7);
        }
    }

    private static void string(String var0, DataOutputStream var1) throws IOException {
        FoundationStringEncoding.write(var0, var1, 1048576);
    }

    private static void append(Tag var0, DataOutputStream var1, int var2, int[] var3) throws IOException {
        if (var0 == null || var2 > 64 || ++var3[0] > 65536) {
            throw new IllegalArgumentException("foundation-NBT-complexity");
        } else if (!TYPES.contains(var0.getClass())) {
            throw new IllegalArgumentException("foundation-unknown-tag-type");
        } else {
            var1.writeByte(var0.getId());
            if (var0 instanceof CompoundTag var4) {
                if (!(var4 instanceof Compound var11)) {
                    throw new IllegalArgumentException("foundation-raw-compound");
                }

                Map<String, Tag> var12 = var11.ftboss$rawCompoundFields();
                if (var12.size() > 65536 - var3[0]) {
                    throw new IllegalArgumentException("foundation-compound-budget");
                }

                var1.writeInt(var12.size());

                for (String var14 : new TreeSet<>(var12.keySet())) {
                    string(var14, var1);
                    append((Tag)var12.get(var14), var1, var2 + 1, var3);
                }
            } else if (var0 instanceof ListTag var5) {
                if (!(var5 instanceof Sequence var17)) {
                    throw new IllegalArgumentException("foundation-raw-list");
                }

                List<Tag> var21 = var17.ftboss$rawListFields();
                byte var24 = var17.ftboss$rawListKind();
                if (var21.size() > 65536 - var3[0]) {
                    throw new IllegalArgumentException("foundation-list-budget");
                }

                var1.writeByte(var24);
                var1.writeInt(var21.size());

                for (Tag var15 : var21) {
                    if (var15.getId() != var24) {
                        throw new IllegalArgumentException("foundation-list-type");
                    }

                    append(var15, var1, var2 + 1, var3);
                }
            } else if (var0 instanceof StringTag var6) {
                string(var6.getAsString(), var1);
            } else if (var0 instanceof NumericTag var7) {
                switch (var0.getId()) {
                    case 1:
                        var1.writeByte(var7.getAsByte());
                        break;
                    case 2:
                        var1.writeShort(var7.getAsShort());
                        break;
                    case 3:
                        var1.writeInt(var7.getAsInt());
                        break;
                    case 4:
                        var1.writeLong(var7.getAsLong());
                        break;
                    case 5:
                        var1.writeInt(Float.floatToRawIntBits(var7.getAsFloat()));
                        break;
                    case 6:
                        var1.writeLong(Double.doubleToRawLongBits(var7.getAsDouble()));
                        break;
                    default:
                        throw new IllegalArgumentException("foundation-unknown-numeric-tag");
                }
            } else if (var0 instanceof ByteArrayTag var8) {
                byte[] var18 = var8.getAsByteArray();
                if (var18.length > 1048576 - var1.size() - 4) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }

                var1.writeInt(var18.length);
                var1.write(var18);
            } else if (var0 instanceof IntArrayTag var9) {
                int[] var19 = var9.getAsIntArray();
                if (var19.length > (1048576 - var1.size() - 4) / 4) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }

                var1.writeInt(var19.length);

                for (int var30 : var19) {
                    var1.writeInt(var30);
                }
            } else if (var0 instanceof LongArrayTag var10) {
                long[] var20 = var10.getAsLongArray();
                if (var20.length > (1048576 - var1.size() - 4) / 8) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }

                var1.writeInt(var20.length);

                for (long var31 : var20) {
                    var1.writeLong(var31);
                }
            } else if (!(var0 instanceof EndTag)) {
                throw new IllegalArgumentException("foundation-unknown-tag-type");
            }

            if (var1.size() > 1048576) {
                throw new IllegalArgumentException("foundation-byte-budget");
            }
        }
    }

    public static CompoundTag object(Object var0) {
        CompoundTag var1 = object(var0, 0, new int[]{0});
        canonical(var1);
        return var1;
    }

    private static CompoundTag object(Object var0, int var1, int[] var2) {
        if (var1 <= 64 && ++var2[0] <= 65536) {
            PrivateCompound var3 = new PrivateCompound();
            String var4 = var0 == null ? "null" : var0.getClass().getName();
            var3.putString("Type", var4);
            if (var0 == null) {
                return var3.finish();
            } else {
                if (var0 instanceof Tag var5) {
                    var3.put("Value", var5);
                } else if (var0 instanceof String var6) {
                    var3.putString("Value", var6);
                } else if (var0 instanceof ResourceLocation var7) {
                    var3.putString("Value", var7.toString());
                } else if (var0 instanceof Enum var8) {
                    var3.putString("Value", var8.name());
                } else if (var0 instanceof Boolean var9) {
                    var3.putBoolean("Value", var9);
                } else if (var0 instanceof Byte var10) {
                    var3.putByte("Value", var10);
                } else if (var0 instanceof Short var11) {
                    var3.putShort("Value", var11);
                } else if (var0 instanceof Integer var12) {
                    var3.putInt("Value", var12);
                } else if (var0 instanceof Long var13) {
                    var3.putLong("Value", var13);
                } else if (var0 instanceof Float var14) {
                    var3.putFloat("Value", var14);
                } else if (var0 instanceof Double var15) {
                    var3.putDouble("Value", var15);
                } else if (var0 instanceof Optional var16) {
                    var3.put("Value", object(var16.orElse(null), var1 + 1, var2));
                } else if (var0 instanceof OptionalInt var17) {
                    var3.put("Value", object(var17.isPresent() ? var17.getAsInt() : null, var1 + 1, var2));
                } else if (var0 instanceof OptionalLong var18) {
                    var3.put("Value", object(var18.isPresent() ? var18.getAsLong() : null, var1 + 1, var2));
                } else if (var0 instanceof OptionalDouble var19) {
                    var3.put("Value", object(var19.isPresent() ? var19.getAsDouble() : null, var1 + 1, var2));
                } else if (var0 instanceof Component var20) {
                    var3.put("Value", object(JsonParser.parseString(net.farmingtales.ftboss.item.NativeFoundationComponentText.serialize(var20)), var1 + 1, var2));
                } else if (var0 instanceof JsonElement var21) {
                    var3.putString("Type", "json");
                    if (var21.isJsonNull()) {
                        var3.put("Value", object(null, var1 + 1, var2));
                    } else if (var21.isJsonObject()) {
                        TreeMap var23 = new TreeMap();
                        var21.getAsJsonObject().entrySet().forEach(var1x -> var23.put((String)var1x.getKey(), var1x.getValue()));
                        var3.put("Value", object(var23, var1 + 1, var2));
                    } else if (var21.isJsonArray()) {
                        ArrayList var30 = new ArrayList();
                        var21.getAsJsonArray().forEach(var30::add);
                        var3.put("Value", object(var30, var1 + 1, var2));
                    } else {
                        JsonPrimitive var31 = var21.getAsJsonPrimitive();
                        var3.putString("Kind", var31.isString() ? "string" : (var31.isBoolean() ? "boolean" : "number"));
                        var3.putString("Value", var31.getAsString());
                    }
                } else if (var0 instanceof Map var22) {
                    if (var22.size() > 65536 - var2[0]) {
                        throw new IllegalArgumentException("foundation-map-budget");
                    }

                    var3.putString("Type", "map");
                    ArrayList<CompoundTag> var32 = new ArrayList<>();
                    var22.forEach((var3x, var4x) -> {
                        PrivateCompound var5x = new PrivateCompound();
                        var5x.put("Key", object(var3x, var1 + 1, var2));
                        var5x.put("Value", object(var4x, var1 + 1, var2));
                        var32.add(var5x.finish());
                    });
                    var32.sort(Comparator.comparing(var0x -> HexFormat.of().formatHex(canonical(var0x.get("Key")))));
                    ListTag var24 = new ListTag();
                    var32.forEach(var24::add);
                    var3.put("Value", var24);
                } else if (!(var0 instanceof List) && !(var0 instanceof Set) && !var0.getClass().isArray()) {
                    if (!var0.getClass().isRecord()) {
                        throw new IllegalArgumentException("foundation-unsupported-rule-type: " + var4);
                    }

                    PrivateCompound var34 = new PrivateCompound();

                    for (RecordComponent var27 : var0.getClass().getRecordComponents()) {
                        try {
                            var34.put(var27.getName(), object(var27.getAccessor().invoke(var0), var1 + 1, var2));
                        } catch (ReflectiveOperationException var29) {
                            throw new IllegalArgumentException("foundation-record-field", var29);
                        }
                    }

                    var3.put("Value", var34.finish());
                } else {
                    ArrayList<Tag> var33 = new ArrayList<>();
                    if (var0.getClass().isArray()) {
                        int var36 = Array.getLength(var0);
                        if (var36 > 65536 - var2[0]) {
                            throw new IllegalArgumentException("foundation-array-budget");
                        }

                        for (int var39 = 0; var39 < var36; var39++) {
                            var33.add(object(Array.get(var0, var39), var1 + 1, var2));
                        }
                    } else {
                        Collection var35 = (Collection)var0;
                        if (var35.size() > 65536 - var2[0]) {
                            throw new IllegalArgumentException("foundation-collection-budget");
                        }

                        for (Object var26 : var35) {
                            var33.add(object(var26, var1 + 1, var2));
                        }

                        var3.putString("Type", var0 instanceof Set ? "set" : "list");
                        if (var0 instanceof Set) {
                            var33.sort(Comparator.comparing(var0x -> HexFormat.of().formatHex(canonical(var0x))));
                        }
                    }

                    ListTag var37 = new ListTag();
                    var33.forEach(var37::add);
                    var3.put("Value", var37);
                }

                return var3.finish();
            }
        } else {
            throw new IllegalArgumentException("foundation-object-complexity");
        }
    }

    private static final class PrivateCompound {
        private final java.util.HashMap<String, Tag> fields = new java.util.HashMap<>();

        private Tag put(String key, Tag value) {
            return fields.put(key, value);
        }

        private void putString(String key, String value) { put(key, StringTag.valueOf(value)); }
        private void putBoolean(String key, boolean value) { put(key, ByteTag.valueOf(value)); }
        private void putByte(String key, byte value) { put(key, ByteTag.valueOf(value)); }
        private void putShort(String key, short value) { put(key, ShortTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, IntTag.valueOf(value)); }
        private void putLong(String key, long value) { put(key, LongTag.valueOf(value)); }
        private void putFloat(String key, float value) { put(key, FloatTag.valueOf(value)); }
        private void putDouble(String key, double value) { put(key, DoubleTag.valueOf(value)); }

        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.foundationCompoundForRead404(fields);
        }
    }

    private static final class PrivateFingerprint416 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.fingerprintCompoundForRead416(null, fields);
        }
    }
}
