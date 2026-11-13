package cn.farmingtales.sdmshopaudit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.TreeSet;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public record ShopSnapshot(String shopId, JsonObject data, String snbt, String hash) {
    public static ShopSnapshot capture(Object shop) throws ReflectiveOperationException {
        CompoundTag tag = Bridge.serialize(shop);
        JsonObject data = toJson(tag).getAsJsonObject();
        String shopId = tag.hasUUID("shopUUID") ? tag.getUUID("shopUUID").toString() : "unknown";
        String snbt = tag.toString();
        return new ShopSnapshot(shopId, data, snbt, digest(snbt));
    }

    public int tabs() {
        return data.has("shopTabs") ? data.getAsJsonArray("shopTabs").size() : 0;
    }

    public static String digest(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static JsonElement toJson(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            JsonObject result = new JsonObject();
            for (String key : new TreeSet<>(compound.getAllKeys())) {
                result.add(key, toJson(compound.get(key)));
            }
            return result;
        }
        JsonArray array = new JsonArray();
        if (tag instanceof ListTag list) {
            for (Tag element : list) {
                array.add(toJson(element));
            }
            return array;
        }
        if (tag instanceof IntArrayTag integers) {
            for (int value : integers.getAsIntArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof LongArrayTag longs) {
            for (long value : longs.getAsLongArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof ByteArrayTag bytes) {
            for (byte value : bytes.getAsByteArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof NumericTag number) {
            return new JsonPrimitive(number.getAsNumber());
        }
        return new JsonPrimitive(tag.getAsString());
    }
}
