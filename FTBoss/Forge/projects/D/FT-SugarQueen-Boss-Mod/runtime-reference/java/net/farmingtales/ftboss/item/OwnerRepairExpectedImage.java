/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;

final class OwnerRepairExpectedImage {
    private OwnerRepairExpectedImage() {
    }

    private static Map<String, Value> map(Value value) {
        if (value == null || value.kind != 10) {
            throw new IllegalArgumentException("Independent compound required");
        }
        return (Map)value.data;
    }

    private static int integer(Value value) {
        if (value == null || value.kind != 3) {
            throw new IllegalArgumentException("Independent int required");
        }
        return ((Number)value.data).intValue();
    }

    private static String string(Value value) {
        if (value == null || value.kind != 8) {
            throw new IllegalArgumentException("Independent string required");
        }
        return (String)value.data;
    }

    static Plan reduction(byte[] byArray, int n) {
        if (byArray == null || byArray.length > 0x100000) {
            return null;
        }
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray));
            if (dataInputStream.readInt() != 1179932499 || dataInputStream.readInt() != 3) {
                return null;
            }
            Value value = OwnerRepairExpectedImage.read(dataInputStream, 0, new int[1]);
            if (dataInputStream.available() != 0 || OwnerRepairExpectedImage.map(value).containsKey("ForgeCaps")) {
                return null;
            }
            Map<String, Value> map = OwnerRepairExpectedImage.map(OwnerRepairExpectedImage.map(value).get("tag"));
            Value value2 = OwnerRepairExpectedImage.map(map.get("FTBossWeaponLifecycle")).get("Weapon");
            if (value2 == null || value2.kind != 11 || ((long[])value2.data).length != 4) {
                return null;
            }
            long[] lArray = (long[])value2.data;
            UUID uUID = new UUID((lArray[0] & 0xFFFFFFFFL) << 32 | lArray[1] & 0xFFFFFFFFL, (lArray[2] & 0xFFFFFFFFL) << 32 | lArray[3] & 0xFFFFFFFFL);
            int n2 = map.containsKey("Damage") ? OwnerRepairExpectedImage.integer(map.get("Damage")) : 0;
            byte[][] byArrayArray = new byte[2][];
            byArrayArray[0] = OwnerRepairExpectedImage.encode(value);
            map.put("Damage", new Value(3, Math.max(0, n2 - n)));
            byArrayArray[1] = OwnerRepairExpectedImage.encode(value);
            return new Plan(uUID, byArrayArray);
        }
        catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }

    static Plan statue(byte[] byArray, int n) {
        if (byArray == null || byArray.length > 0x100000) {
            return null;
        }
        try {
            Value value;
            int n2;
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray));
            if (dataInputStream.readInt() != 1179932499 || dataInputStream.readInt() != 3) {
                return null;
            }
            Value value2 = OwnerRepairExpectedImage.read(dataInputStream, 0, new int[1]);
            if (dataInputStream.available() != 0 || OwnerRepairExpectedImage.map(value2).containsKey("ForgeCaps")) {
                return null;
            }
            Map<String, Value> map = OwnerRepairExpectedImage.map(OwnerRepairExpectedImage.map(value2).get("tag"));
            Map<String, Value> map2 = OwnerRepairExpectedImage.map(map.get("FTBossWeaponLifecycle"));
            Map<String, Value> map3 = OwnerRepairExpectedImage.map(map2.get("RepairQuest"));
            Value value3 = map2.get("Weapon");
            if (value3 == null || value3.kind != 11 || ((long[])value3.data).length != 4) {
                return null;
            }
            long[] lArray = (long[])value3.data;
            UUID uUID = new UUID((lArray[0] & 0xFFFFFFFFL) << 32 | lArray[1] & 0xFFFFFFFFL, (lArray[2] & 0xFFFFFFFFL) << 32 | lArray[3] & 0xFFFFFFFFL);
            int n3 = OwnerRepairExpectedImage.integer(map3.get("Level"));
            if (n3 < 0 || n3 > 8 || OwnerRepairExpectedImage.integer(map3.get("Version")) != 1 || OwnerRepairExpectedImage.integer(map.get("Damage")) <= 0) {
                return null;
            }
            String string = OwnerRepairExpectedImage.string(map2.get("RepairProfile"));
            if (!string.equals(OwnerRepairExpectedImage.string(map3.get("Profile"))) || !string.equals(FoodWeaponEvolution.profileId(n))) {
                return null;
            }
            Value value4 = map3.get("Progress");
            if (value4 == null || value4.kind != 11 || ((long[])value4.data).length != 3) {
                return null;
            }
            long[] lArray2 = (long[])value4.data;
            for (int i = 0; i < 3; ++i) {
                if (lArray2[i] == (long)FoodWeaponLifecycleRules.repairRequired(n3, i)) continue;
                return null;
            }
            Map<String, Value> map4 = OwnerRepairExpectedImage.map(map.get("FTBossFoodEvolution"));
            int n4 = n2 = map4.containsKey("Level") ? OwnerRepairExpectedImage.integer(map4.get("Level")) : 0;
            if (n2 != n3 || !map2.containsKey("WearStep")) {
                return null;
            }
            byte[][] byArrayArray = new byte[4][];
            byArrayArray[0] = OwnerRepairExpectedImage.encode(value2);
            map2.remove("RepairQuest");
            byArrayArray[1] = OwnerRepairExpectedImage.encode(value2);
            map.put("Damage", new Value(3, 0));
            map2.remove("Broken");
            map2.remove("PendingMaterials");
            map2.remove("PendingSockets");
            map2.put("WearStep", new Value(3, 0));
            if (n3 > 0) {
                map2.put("WasEvolved", new Value(1, (byte)1));
            }
            if ((value = map.get("FTBossWeaponMaterials")) != null) {
                OwnerRepairExpectedImage.map(value).put("WearCarry", new Value(6, Double.doubleToRawLongBits(0.0)));
            }
            byArrayArray[2] = OwnerRepairExpectedImage.encode(value2);
            int n5 = map2.containsKey("RepairRevision") ? Math.max(0, OwnerRepairExpectedImage.integer(map2.get("RepairRevision"))) : 0;
            map2.put("RepairRevision", new Value(3, n5 == Integer.MAX_VALUE ? 0 : n5 + 1));
            byArrayArray[3] = OwnerRepairExpectedImage.encode(value2);
            return new Plan(uUID, byArrayArray);
        }
        catch (IOException | ClassCastException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static String text(DataInputStream dataInputStream) throws IOException {
        int n = dataInputStream.readInt();
        if (n < 0 || n > dataInputStream.available()) {
            throw new IOException("Independent text budget");
        }
        byte[] byArray = dataInputStream.readNBytes(n);
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(byArray)).toString();
    }

    private static Value read(DataInputStream dataInputStream, int n, int[] nArray) throws IOException {
        if (n > 64 || (nArray[0] = nArray[0] + 1) > 65536) {
            throw new IOException("Independent field budget");
        }
        int n2 = dataInputStream.readUnsignedByte();
        return switch (n2) {
            case 0 -> new Value(n2, null);
            case 1 -> new Value(n2, dataInputStream.readByte());
            case 2 -> new Value(n2, dataInputStream.readShort());
            case 3, 5 -> new Value(n2, dataInputStream.readInt());
            case 4, 6 -> new Value(n2, dataInputStream.readLong());
            case 8 -> new Value(n2, OwnerRepairExpectedImage.text(dataInputStream));
            case 10 -> {
                int var4_4 = dataInputStream.readInt();
                if (var4_4 < 0 || var4_4 > 65536 - nArray[0]) {
                    throw new IOException("Independent map budget");
                }
                TreeMap<String, Value> var5_7 = new TreeMap<String, Value>();
                String var6_10 = null;
                for (int var7_13 = 0; var7_13 < var4_4; ++var7_13) {
                    String var8_16 = OwnerRepairExpectedImage.text(dataInputStream);
                    if (var6_10 != null && var6_10.compareTo(var8_16) >= 0) {
                        throw new IOException("Independent sorted keys");
                    }
                    var5_7.put(var8_16, OwnerRepairExpectedImage.read(dataInputStream, n + 1, nArray));
                    var6_10 = var8_16;
                }
                yield new Value(n2, var5_7);
            }
            case 9 -> {
                int var4_5 = dataInputStream.readUnsignedByte();
                int var5_8 = dataInputStream.readInt();
                if (var5_8 < 0 || var5_8 > 65536 - nArray[0]) {
                    throw new IOException("Independent list budget");
                }
                Value[] var6_11 = new Value[var5_8];
                for (int var7_14 = 0; var7_14 < var5_8; ++var7_14) {
                    var6_11[var7_14] = OwnerRepairExpectedImage.read(dataInputStream, n + 1, nArray);
                    if (var6_11[var7_14].kind == var4_5) continue;
                    throw new IOException("Independent list type");
                }
                yield new Value(n2, new Object[]{var4_5, var6_11});
            }
            case 7, 11, 12 -> {
                int var5_9;
                int var4_6 = dataInputStream.readInt();
                int v1 = n2 == 7 ? 1 : (var5_9 = n2 == 11 ? 4 : 8);
                if (var4_6 < 0 || var4_6 > dataInputStream.available() / var5_9) {
                    throw new IOException("Independent array budget");
                }
                long[] var6_12 = new long[var4_6];
                for (int var7_15 = 0; var7_15 < var4_6; ++var7_15) {
                    var6_12[var7_15] = var5_9 == 1 ? (long)dataInputStream.readByte() : (var5_9 == 4 ? (long)dataInputStream.readInt() : dataInputStream.readLong());
                }
                yield new Value(n2, var6_12);
            }
            default -> throw new IOException("Independent tag kind");
        };
    }

    private static byte[] encode(Value value) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(1179932499);
        dataOutputStream.writeInt(3);
        OwnerRepairExpectedImage.write(value, dataOutputStream);
        if (byteArrayOutputStream.size() > 0x100000) {
            throw new IOException("Independent output budget");
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void write(Value value, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(value.kind);
        switch (value.kind) {
            case 0: {
                break;
            }
            case 1: {
                dataOutputStream.writeByte(((Number)value.data).byteValue());
                break;
            }
            case 2: {
                dataOutputStream.writeShort(((Number)value.data).shortValue());
                break;
            }
            case 3: 
            case 5: {
                dataOutputStream.writeInt(((Number)value.data).intValue());
                break;
            }
            case 4: 
            case 6: {
                dataOutputStream.writeLong(((Number)value.data).longValue());
                break;
            }
            case 8: {
                OwnerRepairExpectedImage.writeText((String)value.data, dataOutputStream);
                break;
            }
            case 10: {
                Map<String, Value> map = OwnerRepairExpectedImage.map(value);
                dataOutputStream.writeInt(map.size());
                for (Map.Entry<String, Value> entry : map.entrySet()) {
                    OwnerRepairExpectedImage.writeText(entry.getKey(), dataOutputStream);
                    OwnerRepairExpectedImage.write(entry.getValue(), dataOutputStream);
                }
                break;
            }
            case 9: {
                Object[] objectArray = (Object[])value.data;
                Value[] valueArray = (Value[])objectArray[1];
                dataOutputStream.writeByte((Integer)objectArray[0]);
                dataOutputStream.writeInt(valueArray.length);
                for (Value value2 : valueArray) {
                    OwnerRepairExpectedImage.write(value2, dataOutputStream);
                }
                break;
            }
            case 7: 
            case 11: 
            case 12: {
                long[] lArray = (long[])value.data;
                dataOutputStream.writeInt(lArray.length);
                for (long l : lArray) {
                    if (value.kind == 7) {
                        dataOutputStream.writeByte((int)l);
                        continue;
                    }
                    if (value.kind == 11) {
                        dataOutputStream.writeInt((int)l);
                        continue;
                    }
                    dataOutputStream.writeLong(l);
                }
                break;
            }
            default: {
                throw new IOException("Independent output kind");
            }
        }
    }

    private static void writeText(String string, DataOutputStream dataOutputStream) throws IOException {
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(byArray.length);
        dataOutputStream.write(byArray);
    }

    private static final class Value {
        final int kind;
        Object data;

        Value(int n, Object object) {
            this.kind = n;
            this.data = object;
        }
    }

    static final class Plan {
        final UUID weapon;
        final byte[][] prefixes;

        Plan(UUID uUID, byte[][] byArray) {
            this.weapon = uUID;
            this.prefixes = byArray;
        }
    }
}

