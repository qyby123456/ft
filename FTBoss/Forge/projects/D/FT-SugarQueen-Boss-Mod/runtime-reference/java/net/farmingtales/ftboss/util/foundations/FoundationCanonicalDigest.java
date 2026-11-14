/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.EndTag
 *  net.minecraft.nbt.FloatTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.NumericTag
 *  net.minecraft.nbt.ShortTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.resources.ResourceLocation
 */
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
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.util.foundations.FoundationStringEncoding;
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
import net.minecraft.resources.ResourceLocation;

public final class FoundationCanonicalDigest {
    public static final int VERSION = 3;
    public static final int MAX_DEPTH = 64;
    public static final int MAX_NODES = 65536;
    public static final int MAX_BYTES = 0x100000;
    private static final Set<Class<?>> TYPES = Set.of(CompoundTag.class, ListTag.class, StringTag.class, ByteTag.class, ShortTag.class, IntTag.class, LongTag.class, FloatTag.class, DoubleTag.class, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class, EndTag.class);

    private FoundationCanonicalDigest() {
    }

    public static String fingerprint(String string, Tag tag) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("Domain", string);
        compoundTag.m_128405_("DigestVersion", 3);
        if (tag == null) {
            throw new IllegalArgumentException("null-digest-input");
        }
        compoundTag.m_128365_("Value", tag);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(FoundationCanonicalDigest.canonical((Tag)compoundTag)));
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    public static byte[] canonical(Tag tag) {
        return OwnerFieldExpectedAuthority.foundationCanonicalRead(() -> FoundationCanonicalDigest.canonicalTrusted(tag));
    }

    private static byte[] canonicalTrusted(Tag tag) {
        byte[] byArray;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(1180124740);
            dataOutputStream.writeInt(3);
            FoundationCanonicalDigest.append(tag, dataOutputStream, 0, new int[]{0});
            byArray = byteArrayOutputStream.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    dataOutputStream.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                throw new IllegalStateException(iOException);
            }
        }
        dataOutputStream.close();
        return byArray;
    }

    private static void string(String string, DataOutputStream dataOutputStream) throws IOException {
        FoundationStringEncoding.write(string, dataOutputStream, 0x100000);
    }

    private static void append(Tag tag, DataOutputStream dataOutputStream, int n, int[] nArray) throws IOException {
        block31: {
            block34: {
                block33: {
                    block32: {
                        block30: {
                            if (tag == null || n > 64 || (nArray[0] = nArray[0] + 1) > 65536) {
                                throw new IllegalArgumentException("foundation-NBT-complexity");
                            }
                            if (!TYPES.contains(tag.getClass())) {
                                throw new IllegalArgumentException("foundation-unknown-tag-type");
                            }
                            dataOutputStream.writeByte(tag.m_7060_());
                            if (!(tag instanceof CompoundTag)) break block30;
                            CompoundTag compoundTag = (CompoundTag)tag;
                            if (!(compoundTag instanceof NativeExpectedFieldViews.Compound)) {
                                throw new IllegalArgumentException("foundation-raw-compound");
                            }
                            NativeExpectedFieldViews.Compound compound = (NativeExpectedFieldViews.Compound)compoundTag;
                            Map<String, Tag> map = compound.ftboss$rawCompoundFields();
                            if (map.size() > 65536 - nArray[0]) {
                                throw new IllegalArgumentException("foundation-compound-budget");
                            }
                            dataOutputStream.writeInt(map.size());
                            for (String string : new TreeSet<String>(map.keySet())) {
                                FoundationCanonicalDigest.string(string, dataOutputStream);
                                FoundationCanonicalDigest.append(map.get(string), dataOutputStream, n + 1, nArray);
                            }
                            break block31;
                        }
                        if (!(tag instanceof ListTag)) break block32;
                        ListTag listTag = (ListTag)tag;
                        if (!(listTag instanceof NativeExpectedFieldViews.Sequence)) {
                            throw new IllegalArgumentException("foundation-raw-list");
                        }
                        NativeExpectedFieldViews.Sequence sequence = (NativeExpectedFieldViews.Sequence)listTag;
                        List<Tag> list = sequence.ftboss$rawListFields();
                        byte by = sequence.ftboss$rawListKind();
                        if (list.size() > 65536 - nArray[0]) {
                            throw new IllegalArgumentException("foundation-list-budget");
                        }
                        dataOutputStream.writeByte(by);
                        dataOutputStream.writeInt(list.size());
                        for (Tag tag2 : list) {
                            if (tag2.m_7060_() != by) {
                                throw new IllegalArgumentException("foundation-list-type");
                            }
                            FoundationCanonicalDigest.append(tag2, dataOutputStream, n + 1, nArray);
                        }
                        break block31;
                    }
                    if (!(tag instanceof StringTag)) break block33;
                    StringTag stringTag = (StringTag)tag;
                    FoundationCanonicalDigest.string(stringTag.m_7916_(), dataOutputStream);
                    break block31;
                }
                if (!(tag instanceof NumericTag)) break block34;
                NumericTag numericTag = (NumericTag)tag;
                switch (tag.m_7060_()) {
                    case 1: {
                        dataOutputStream.writeByte(numericTag.m_7063_());
                        break block31;
                    }
                    case 2: {
                        dataOutputStream.writeShort(numericTag.m_7053_());
                        break block31;
                    }
                    case 3: {
                        dataOutputStream.writeInt(numericTag.m_7047_());
                        break block31;
                    }
                    case 4: {
                        dataOutputStream.writeLong(numericTag.m_7046_());
                        break block31;
                    }
                    case 5: {
                        dataOutputStream.writeInt(Float.floatToRawIntBits(numericTag.m_7057_()));
                        break block31;
                    }
                    case 6: {
                        dataOutputStream.writeLong(Double.doubleToRawLongBits(numericTag.m_7061_()));
                        break block31;
                    }
                    default: {
                        throw new IllegalArgumentException("foundation-unknown-numeric-tag");
                    }
                }
            }
            if (tag instanceof ByteArrayTag) {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
                byte[] byArray = byteArrayTag.m_128227_();
                if (byArray.length > 0x100000 - dataOutputStream.size() - 4) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }
                dataOutputStream.writeInt(byArray.length);
                dataOutputStream.write(byArray);
            } else if (tag instanceof IntArrayTag) {
                IntArrayTag intArrayTag = (IntArrayTag)tag;
                int[] nArray2 = intArrayTag.m_128648_();
                if (nArray2.length > (0x100000 - dataOutputStream.size() - 4) / 4) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }
                dataOutputStream.writeInt(nArray2.length);
                for (int n2 : nArray2) {
                    dataOutputStream.writeInt(n2);
                }
            } else if (tag instanceof LongArrayTag) {
                LongArrayTag longArrayTag = (LongArrayTag)tag;
                long[] lArray = longArrayTag.m_128851_();
                if (lArray.length > (0x100000 - dataOutputStream.size() - 4) / 8) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }
                dataOutputStream.writeInt(lArray.length);
                for (long l : lArray) {
                    dataOutputStream.writeLong(l);
                }
            } else if (!(tag instanceof EndTag)) {
                throw new IllegalArgumentException("foundation-unknown-tag-type");
            }
        }
        if (dataOutputStream.size() > 0x100000) {
            throw new IllegalArgumentException("foundation-byte-budget");
        }
    }

    public static CompoundTag object(Object object) {
        CompoundTag compoundTag = FoundationCanonicalDigest.object(object, 0, new int[]{0});
        FoundationCanonicalDigest.canonical((Tag)compoundTag);
        return compoundTag;
    }

    private static CompoundTag object(Object object3, int n, int[] nArray) {
        if (n > 64 || (nArray[0] = nArray[0] + 1) > 65536) {
            throw new IllegalArgumentException("foundation-object-complexity");
        }
        CompoundTag compoundTag2 = new CompoundTag();
        String string = object3 == null ? "null" : object3.getClass().getName();
        compoundTag2.m_128359_("Type", string);
        if (object3 == null) {
            return compoundTag2;
        }
        if (object3 instanceof Tag) {
            Tag tag = (Tag)object3;
            compoundTag2.m_128365_("Value", tag);
        } else if (object3 instanceof String) {
            String string2 = (String)object3;
            compoundTag2.m_128359_("Value", string2);
        } else if (object3 instanceof ResourceLocation) {
            ResourceLocation resourceLocation = (ResourceLocation)object3;
            compoundTag2.m_128359_("Value", resourceLocation.toString());
        } else if (object3 instanceof Enum) {
            Enum enum_ = (Enum)object3;
            compoundTag2.m_128359_("Value", enum_.name());
        } else if (object3 instanceof Boolean) {
            Boolean bl = (Boolean)object3;
            compoundTag2.m_128379_("Value", bl.booleanValue());
        } else if (object3 instanceof Byte) {
            Byte by = (Byte)object3;
            compoundTag2.m_128344_("Value", by.byteValue());
        } else if (object3 instanceof Short) {
            Short s = (Short)object3;
            compoundTag2.m_128376_("Value", s.shortValue());
        } else if (object3 instanceof Integer) {
            Integer n2 = (Integer)object3;
            compoundTag2.m_128405_("Value", n2.intValue());
        } else if (object3 instanceof Long) {
            Long l = (Long)object3;
            compoundTag2.m_128356_("Value", l.longValue());
        } else if (object3 instanceof Float) {
            Float f = (Float)object3;
            compoundTag2.m_128350_("Value", f.floatValue());
        } else if (object3 instanceof Double) {
            Double d = (Double)object3;
            compoundTag2.m_128347_("Value", d.doubleValue());
        } else if (object3 instanceof Optional) {
            Optional optional = (Optional)object3;
            compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(optional.orElse(null), n + 1, nArray));
        } else if (object3 instanceof OptionalInt) {
            OptionalInt optionalInt = (OptionalInt)object3;
            compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(optionalInt.isPresent() ? Integer.valueOf(optionalInt.getAsInt()) : null, n + 1, nArray));
        } else if (object3 instanceof OptionalLong) {
            OptionalLong optionalLong = (OptionalLong)object3;
            compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(optionalLong.isPresent() ? Long.valueOf(optionalLong.getAsLong()) : null, n + 1, nArray));
        } else if (object3 instanceof OptionalDouble) {
            OptionalDouble optionalDouble = (OptionalDouble)object3;
            compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(optionalDouble.isPresent() ? Double.valueOf(optionalDouble.getAsDouble()) : null, n + 1, nArray));
        } else if (object3 instanceof Component) {
            Component component = (Component)object3;
            compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(JsonParser.parseString((String)Component.Serializer.m_130703_((Component)component)), n + 1, nArray));
        } else if (object3 instanceof JsonElement) {
            JsonElement jsonElement = (JsonElement)object3;
            compoundTag2.m_128359_("Type", "json");
            if (jsonElement.isJsonNull()) {
                compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(null, n + 1, nArray));
            } else if (jsonElement.isJsonObject()) {
                TreeMap treeMap = new TreeMap();
                jsonElement.getAsJsonObject().entrySet().forEach(entry -> treeMap.put((String)entry.getKey(), entry.getValue()));
                compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(treeMap, n + 1, nArray));
            } else if (jsonElement.isJsonArray()) {
                ArrayList arrayList = new ArrayList();
                jsonElement.getAsJsonArray().forEach(arrayList::add);
                compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(arrayList, n + 1, nArray));
            } else {
                JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                compoundTag2.m_128359_("Kind", jsonPrimitive.isString() ? "string" : (jsonPrimitive.isBoolean() ? "boolean" : "number"));
                compoundTag2.m_128359_("Value", jsonPrimitive.getAsString());
            }
        } else if (object3 instanceof Map) {
            Map map = (Map)object3;
            if (map.size() > 65536 - nArray[0]) {
                throw new IllegalArgumentException("foundation-map-budget");
            }
            compoundTag2.m_128359_("Type", "map");
            ArrayList<CompoundTag> arrayList = new ArrayList<CompoundTag>();
            map.forEach((object, object2) -> {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.m_128365_("Key", (Tag)FoundationCanonicalDigest.object(object, n + 1, nArray));
                compoundTag.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(object2, n + 1, nArray));
                arrayList.add(compoundTag);
            });
            arrayList.sort(Comparator.comparing(compoundTag -> HexFormat.of().formatHex(FoundationCanonicalDigest.canonical(compoundTag.m_128423_("Key")))));
            ListTag listTag = new ListTag();
            arrayList.forEach(arg_0 -> listTag.add(arg_0));
            compoundTag2.m_128365_("Value", (Tag)listTag);
        } else if (object3 instanceof List || object3 instanceof Set || object3.getClass().isArray()) {
            ArrayList<CompoundTag> arrayList = new ArrayList<CompoundTag>();
            if (object3.getClass().isArray()) {
                int n3 = Array.getLength(object3);
                if (n3 > 65536 - nArray[0]) {
                    throw new IllegalArgumentException("foundation-array-budget");
                }
                for (int i = 0; i < n3; ++i) {
                    arrayList.add(FoundationCanonicalDigest.object(Array.get(object3, i), n + 1, nArray));
                }
            } else {
                Collection collection = (Collection)object3;
                if (collection.size() > 65536 - nArray[0]) {
                    throw new IllegalArgumentException("foundation-collection-budget");
                }
                for (Object e : collection) {
                    arrayList.add(FoundationCanonicalDigest.object(e, n + 1, nArray));
                }
                compoundTag2.m_128359_("Type", object3 instanceof Set ? "set" : "list");
                if (object3 instanceof Set) {
                    arrayList.sort(Comparator.comparing(compoundTag -> HexFormat.of().formatHex(FoundationCanonicalDigest.canonical((Tag)compoundTag))));
                }
            }
            ListTag listTag = new ListTag();
            arrayList.forEach(arg_0 -> listTag.add(arg_0));
            compoundTag2.m_128365_("Value", (Tag)listTag);
        } else if (object3.getClass().isRecord()) {
            CompoundTag compoundTag3 = new CompoundTag();
            for (RecordComponent recordComponent : object3.getClass().getRecordComponents()) {
                try {
                    compoundTag3.m_128365_(recordComponent.getName(), (Tag)FoundationCanonicalDigest.object(recordComponent.getAccessor().invoke(object3, new Object[0]), n + 1, nArray));
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    throw new IllegalArgumentException("foundation-record-field", reflectiveOperationException);
                }
            }
            compoundTag2.m_128365_("Value", (Tag)compoundTag3);
        } else {
            throw new IllegalArgumentException("foundation-unsupported-rule-type: " + string);
        }
        return compoundTag2;
    }
}

