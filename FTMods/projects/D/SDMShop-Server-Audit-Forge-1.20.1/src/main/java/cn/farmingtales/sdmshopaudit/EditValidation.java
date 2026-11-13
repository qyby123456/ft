package cn.farmingtales.sdmshopaudit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class EditValidation {
    private EditValidation() {
    }

    public static void check(Object packet) throws ReflectiveOperationException {
        Object shop = Bridge.mainShop();
        String operation = packet.getClass().getSimpleName();
        if (operation.equals("SendChangesShopC2S")) {
            CompoundTag data = payload(packet);
            if (data.contains("sdmversion") && !data.getString("sdmversion").equals("1.0.0")) {
                throw new IllegalArgumentException("UNSUPPORTED_NETWORK_SHOP_SCHEMA");
            }
            identity(data, "shopUUID", (UUID) Bridge.field(shop, "shopUUID"));
            Set<UUID> identities = new HashSet<>();
            for (Tag value : list(data, "shopTabs", true)) {
                CompoundTag tab = (CompoundTag) value;
                unique(identities, identity(tab, "shopTabUUID", null));
                tab(tab);
            }
            return;
        }
        if (operation.equals("SendCreateShopTabC2S")) {
            CompoundTag data = payload(packet);
            UUID id = identity(data, "shopTabUUID", null);
            if (shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, id) != null) {
                throw new IllegalArgumentException("DUPLICATE_TAB_UUID");
            }
            tab(data);
            return;
        }
        if (operation.equals("SendMoveShopTabC2S")) {
            indexes(packet, (List<?>) shop.getClass().getMethod("getShopTabs").invoke(shop));
            return;
        }
        String field = operation.equals("SendChangeShopTabC2S") ? "tab" : "tabID";
        UUID tabId = (UUID) Bridge.field(packet, field);
        Object target = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, tabId);
        if (target == null) {
            throw new IllegalArgumentException("UNKNOWN_SHOP_TAB");
        }
        if (operation.equals("SendMoveShopEntryC2S")) {
            indexes(packet, (List<?>) target.getClass().getMethod("getTabEntry").invoke(target));
            return;
        }
        CompoundTag data = payload(packet);
        switch (operation) {
            case "SendCreateShopEntryC2S" -> {
                entry(data);
                if (target.getClass().getMethod("getShopEntry", UUID.class).invoke(target, data.getUUID("entryUUID")) != null) {
                    throw new IllegalArgumentException("DUPLICATE_ENTRY_UUID");
                }
            }
            case "SendEditShopEntryC2S" -> {
                UUID entryId = (UUID) Bridge.field(packet, "entryID");
                if (target.getClass().getMethod("getShopEntry", UUID.class).invoke(target, entryId) == null) {
                    throw new IllegalArgumentException("UNKNOWN_SHOP_ENTRY");
                }
                identity(data, "entryUUID", entryId);
                entry(data);
            }
            case "SendChangeShopTabC2S", "SendChangesShopEntriesC2S", "SendEditShopTabC2S" -> {
                identity(data, "shopTabUUID", tabId);
                tab(data);
            }
            default -> throw new IllegalArgumentException("UNKNOWN_EDIT_OPERATION");
        }
    }

    private static CompoundTag payload(Object packet) throws ReflectiveOperationException {
        if (Bridge.field(packet, "nbt") instanceof CompoundTag data) {
            return data;
        }
        throw new IllegalArgumentException("MISSING_EDIT_PAYLOAD");
    }

    private static void indexes(Object packet, List<?> values) throws ReflectiveOperationException {
        int from = ((Number) Bridge.field(packet, "from")).intValue();
        int to = ((Number) Bridge.field(packet, "to")).intValue();
        if (from < 0 || to < 0 || from >= values.size() || to >= values.size()) {
            throw new IllegalArgumentException("INVALID_SORT_INDEX");
        }
    }

    private static void tab(CompoundTag data) {
        identity(data, "shopTabUUID", null);
        limit(data);
        Set<UUID> identities = new HashSet<>();
        for (Tag value : list(data, "tabEntry", false)) {
            CompoundTag product = (CompoundTag) value;
            unique(identities, identity(product, "entryUUID", null));
            entry(product);
        }
    }

    private static void entry(CompoundTag data) {
        identity(data, "entryUUID", null);
        if (!(data.contains("entryPrice", Tag.TAG_LONG) || data.contains("entryPrice", Tag.TAG_INT))
                || !data.contains("entryCount", Tag.TAG_INT)) {
            throw new IllegalArgumentException("INVALID_PRICE_OR_COUNT_TYPE");
        }
        SecurityRules.quote(data.getLong("entryPrice"), 1, data.getInt("entryCount"));
        limit(data);
    }

    private static void limit(CompoundTag data) {
        if (data.contains("limit") && (!data.contains("limit", Tag.TAG_INT) || data.getInt("limit") < 0)) {
            throw new IllegalArgumentException("INVALID_EDIT_LIMIT");
        }
    }

    private static UUID identity(CompoundTag data, String key, UUID expected) {
        if (!data.hasUUID(key)) {
            throw new IllegalArgumentException("INVALID_UUID:" + key);
        }
        UUID id = data.getUUID(key);
        if (expected != null && !expected.equals(id)) {
            throw new IllegalArgumentException("EDIT_UUID_MISMATCH:" + key);
        }
        return id;
    }

    private static ListTag list(CompoundTag data, String key, boolean required) {
        if (!data.contains(key) && !required) {
            return new ListTag();
        }
        if (!(data.get(key) instanceof ListTag values)
                || !values.isEmpty() && values.getElementType() != Tag.TAG_COMPOUND) {
            throw new IllegalArgumentException("INVALID_EDIT_LIST:" + key);
        }
        return values;
    }

    private static void unique(Set<UUID> identities, UUID id) {
        if (!identities.add(id)) {
            throw new IllegalArgumentException("DUPLICATE_EDIT_UUID");
        }
    }
}
