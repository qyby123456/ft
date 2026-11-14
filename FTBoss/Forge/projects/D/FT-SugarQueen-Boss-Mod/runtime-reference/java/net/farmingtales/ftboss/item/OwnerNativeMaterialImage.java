/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.DoubleTag
 *  net.minecraft.nbt.FloatTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.ShortTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.TreeSet;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

final class OwnerNativeMaterialImage {
    private static final int MAX_BYTES = 0x100000;
    private static final int MAX_NODES = 65536;
    private static final int MAX_DEPTH = 64;
    private final byte[] frozen;

    private OwnerNativeMaterialImage(byte[] byArray) {
        this.frozen = byArray;
    }

    static OwnerNativeMaterialImage freeze(CompoundTag compoundTag) {
        OwnerNativeMaterialImage ownerNativeMaterialImage;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            OwnerNativeMaterialImage.write((Tag)compoundTag, dataOutputStream, 0, new int[]{0}, new IdentityHashMap<Tag, Boolean>());
            ownerNativeMaterialImage = new OwnerNativeMaterialImage(byteArrayOutputStream.toByteArray());
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
        return ownerNativeMaterialImage;
    }

    boolean same(OwnerNativeMaterialImage ownerNativeMaterialImage) {
        return ownerNativeMaterialImage != null && MessageDigest.isEqual(this.frozen, ownerNativeMaterialImage.frozen);
    }

    String digest() {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(this.frozen));
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    CompoundTag image() {
        CompoundTag compoundTag;
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(this.frozen));
        try {
            CompoundTag compoundTag2;
            block10: {
                block9: {
                    Tag tag = OwnerNativeMaterialImage.read(dataInputStream);
                    if (!(tag instanceof CompoundTag)) break block9;
                    compoundTag2 = (CompoundTag)tag;
                    if (dataInputStream.available() == 0) break block10;
                }
                throw new IllegalStateException("Owner frozen image");
            }
            compoundTag = compoundTag2;
        }
        catch (Throwable throwable) {
            try {
                try {
                    dataInputStream.close();
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
        dataInputStream.close();
        return compoundTag;
    }

    private static void string(String string, DataOutputStream dataOutputStream) throws IOException {
        if (string.length() > (0x100000 - dataOutputStream.size() - 4) / 2) {
            throw new IllegalArgumentException("Owner string budget");
        }
        dataOutputStream.writeInt(string.length());
        for (int i = 0; i < string.length(); ++i) {
            dataOutputStream.writeChar(string.charAt(i));
        }
    }

    private static String string(DataInputStream dataInputStream) throws IOException {
        int n = dataInputStream.readInt();
        StringBuilder stringBuilder = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            stringBuilder.append(dataInputStream.readChar());
        }
        return stringBuilder.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void write(Tag tag, DataOutputStream dataOutputStream, int n, int[] nArray, IdentityHashMap<Tag, Boolean> identityHashMap) throws IOException {
        if (tag == null || n > 64 || (nArray[0] = nArray[0] + 1) > 65536 || identityHashMap.put(tag, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Owner material image complexity");
        }
        try {
            Class<?> clazz = tag.getClass();
            if (clazz == CompoundTag.class) {
                CompoundTag compoundTag = (CompoundTag)tag;
                if (compoundTag.m_128440_() > 65536 - nArray[0]) {
                    throw new IllegalArgumentException("Owner compound budget");
                }
                dataOutputStream.writeByte(10);
                dataOutputStream.writeInt(compoundTag.m_128440_());
                for (String string : new TreeSet(compoundTag.m_128431_())) {
                    OwnerNativeMaterialImage.string(string, dataOutputStream);
                    OwnerNativeMaterialImage.write(compoundTag.m_128423_(string), dataOutputStream, n + 1, nArray, identityHashMap);
                }
            } else if (clazz == ListTag.class) {
                ListTag listTag = (ListTag)tag;
                if (listTag.isEmpty() && listTag.m_7264_() != 0) {
                    throw new IllegalArgumentException("Owner typed empty list");
                }
                if (listTag.size() > 65536 - nArray[0]) {
                    throw new IllegalArgumentException("Owner list budget");
                }
                dataOutputStream.writeByte(9);
                dataOutputStream.writeInt(listTag.size());
                for (Tag tag2 : listTag) {
                    OwnerNativeMaterialImage.write(tag2, dataOutputStream, n + 1, nArray, identityHashMap);
                }
            } else if (clazz == StringTag.class) {
                dataOutputStream.writeByte(8);
                OwnerNativeMaterialImage.string(((StringTag)tag).m_7916_(), dataOutputStream);
            } else if (clazz == ByteTag.class) {
                dataOutputStream.writeByte(1);
                dataOutputStream.writeByte(((ByteTag)tag).m_7063_());
            } else if (clazz == ShortTag.class) {
                dataOutputStream.writeByte(2);
                dataOutputStream.writeShort(((ShortTag)tag).m_7053_());
            } else if (clazz == IntTag.class) {
                dataOutputStream.writeByte(3);
                dataOutputStream.writeInt(((IntTag)tag).m_7047_());
            } else if (clazz == LongTag.class) {
                dataOutputStream.writeByte(4);
                dataOutputStream.writeLong(((LongTag)tag).m_7046_());
            } else if (clazz == FloatTag.class) {
                dataOutputStream.writeByte(5);
                dataOutputStream.writeInt(Float.floatToRawIntBits(((FloatTag)tag).m_7057_()));
            } else if (clazz == DoubleTag.class) {
                dataOutputStream.writeByte(6);
                dataOutputStream.writeLong(Double.doubleToRawLongBits(((DoubleTag)tag).m_7061_()));
            } else if (clazz == ByteArrayTag.class) {
                byte[] byArray = ((ByteArrayTag)tag).m_128227_();
                if (byArray.length > 0x100000 - dataOutputStream.size() - 5) {
                    throw new IllegalArgumentException("Owner byte array budget");
                }
                dataOutputStream.writeByte(7);
                dataOutputStream.writeInt(byArray.length);
                dataOutputStream.write(byArray);
            } else if (clazz == IntArrayTag.class) {
                int[] nArray2 = ((IntArrayTag)tag).m_128648_();
                if (nArray2.length > (0x100000 - dataOutputStream.size() - 5) / 4) {
                    throw new IllegalArgumentException("Owner int array budget");
                }
                dataOutputStream.writeByte(11);
                dataOutputStream.writeInt(nArray2.length);
                for (int n2 : nArray2) {
                    dataOutputStream.writeInt(n2);
                }
            } else if (clazz == LongArrayTag.class) {
                long[] lArray = ((LongArrayTag)tag).m_128851_();
                if (lArray.length > (0x100000 - dataOutputStream.size() - 5) / 8) {
                    throw new IllegalArgumentException("Owner long array budget");
                }
                dataOutputStream.writeByte(12);
                dataOutputStream.writeInt(lArray.length);
                for (long l : lArray) {
                    dataOutputStream.writeLong(l);
                }
            } else {
                throw new IllegalArgumentException("Owner non-vanilla material tag");
            }
            if (dataOutputStream.size() > 0x100000) {
                throw new IllegalArgumentException("Owner image byte budget");
            }
        }
        finally {
            identityHashMap.remove(tag);
        }
    }

    private static Tag read(DataInputStream dataInputStream) throws IOException {
        return switch (dataInputStream.readUnsignedByte()) {
            case 1 -> ByteTag.m_128266_((byte)dataInputStream.readByte());
            case 2 -> ShortTag.m_129258_((short)dataInputStream.readShort());
            case 3 -> IntTag.m_128679_((int)dataInputStream.readInt());
            case 4 -> LongTag.m_128882_((long)dataInputStream.readLong());
            case 5 -> FloatTag.m_128566_((float)Float.intBitsToFloat(dataInputStream.readInt()));
            case 6 -> DoubleTag.m_128500_((double)Double.longBitsToDouble(dataInputStream.readLong()));
            case 8 -> StringTag.m_129297_((String)OwnerNativeMaterialImage.string(dataInputStream));
            case 7 -> {
                byte[] var1_1 = new byte[dataInputStream.readInt()];
                dataInputStream.readFully(var1_1);
                yield new ByteArrayTag(var1_1);
            }
            case 11 -> {
                int[] var1_2 = new int[dataInputStream.readInt()];
                for (int var2_6 = 0; var2_6 < var1_2.length; ++var2_6) {
                    var1_2[var2_6] = dataInputStream.readInt();
                }
                yield new IntArrayTag(var1_2);
            }
            case 12 -> {
                long[] var1_3 = new long[dataInputStream.readInt()];
                for (int var2_7 = 0; var2_7 < var1_3.length; ++var2_7) {
                    var1_3[var2_7] = dataInputStream.readLong();
                }
                yield new LongArrayTag(var1_3);
            }
            case 9 -> {
                int var1_4 = dataInputStream.readInt();
                ListTag var2_8 = new ListTag();
                for (int var3_10 = 0; var3_10 < var1_4; ++var3_10) {
                    var2_8.add((Object)OwnerNativeMaterialImage.read(dataInputStream));
                }
                yield var2_8;
            }
            case 10 -> {
                int var1_5 = dataInputStream.readInt();
                CompoundTag var2_9 = new CompoundTag();
                for (int var3_11 = 0; var3_11 < var1_5; ++var3_11) {
                    var2_9.m_128365_(OwnerNativeMaterialImage.string(dataInputStream), OwnerNativeMaterialImage.read(dataInputStream));
                }
                yield var2_9;
            }
            default -> throw new IllegalStateException("Owner frozen tag type");
        };
    }
}

