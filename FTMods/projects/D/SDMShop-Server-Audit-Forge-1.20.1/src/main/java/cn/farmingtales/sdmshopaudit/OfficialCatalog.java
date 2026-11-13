package cn.farmingtales.sdmshopaudit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class OfficialCatalog {
    private final Set<String> products = new HashSet<>();
    private final Set<String> itemIds = new HashSet<>();
    private final Set<String> commands = new HashSet<>();
    private final List<String> tabs = new ArrayList<>();
    private final String sourceHash;
    private int buyEntries;

    public OfficialCatalog(CompoundTag shop, String sourceHash) {
        this.sourceHash = sourceHash;
        for (Tag element : shop.getList("shopTabs", Tag.TAG_COMPOUND)) {
            CompoundTag tab = (CompoundTag) element;
            tabs.add(tab.getString("title"));
            for (Tag entryTag : tab.getList("tabEntry", Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) entryTag;
                if (entry.getBoolean("isSell")) {
                    continue;
                }
                CompoundTag type = entry.getCompound("entryType");
                if (!type.contains("shopEntryTypeID")) {
                    throw new IllegalArgumentException("Official catalog contains an untyped product");
                }
                buyEntries++;
                products.add(identity(type));
                if (type.getString("shopEntryTypeID").equals("commandType")) {
                    commands.add(SecurityRules.commandFingerprint(type.getString("command"),
                            type.getBoolean("elevatePerms"), type.getBoolean("silent")));
                }
                if (isItem(type)) {
                    itemIds.add(type.getCompound("itemStack").getString("id"));
                }
            }
        }
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Official catalog has no purchasable products");
        }
    }

    public static OfficialCatalog load() throws Exception {
        try (InputStream stream = OfficialCatalog.class.getResourceAsStream("/sdmshop-audit-original.snbt")) {
            if (stream == null) {
                throw new IllegalStateException("Embedded fix2 official catalog is missing");
            }
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Object parsed = Class.forName("dev.ftb.mods.ftblibrary.snbt.SNBT")
                    .getMethod("readLines", List.class).invoke(null, text.lines().toList());
            if (!(parsed instanceof CompoundTag tag)) {
                throw new IllegalStateException("Embedded fix2 official catalog could not be parsed");
            }
            return new OfficialCatalog(tag, ShopSnapshot.digest(text));
        }
    }

    public JsonObject check(CompoundTag entry) {
        JsonObject result = new JsonObject();
        result.addProperty("baselineHash", sourceHash);
        if (entry == null) {
            result.addProperty("allowed", false);
            result.addProperty("reason", "PRODUCT_UNAVAILABLE");
            return result;
        }
        CompoundTag type = entry.getCompound("entryType");
        boolean allowed = products.contains(identity(type));
        String id = type.getCompound("itemStack").getString("id");
        result.addProperty("allowed", allowed);
        result.addProperty("itemId", id);
        result.addProperty("productType", type.getString("shopEntryTypeID"));
        result.addProperty("productFingerprint", ShopSnapshot.digest(identity(type)));
        result.addProperty("reason", allowed ? "OFFICIAL_PRODUCT" : isItem(type)
                ? itemIds.contains(id) ? "UNAPPROVED_ITEM_NBT" : "ITEM_NOT_IN_OFFICIAL_SHOP"
                : "PRODUCT_TYPE_OR_PAYLOAD_NOT_IN_OFFICIAL_SHOP");
        return result;
    }

    public JsonObject summary() {
        JsonObject result = new JsonObject();
        result.addProperty("source", "FarmingTales_ForgeⅡ v1.5.2fix2/config/SDMShop/sdmshop.snbt");
        result.addProperty("sourceSha256", sourceHash);
        result.addProperty("tabs", tabs.size());
        result.addProperty("buyEntries", buyEntries);
        result.addProperty("distinctProducts", products.size());
        result.addProperty("distinctItemIds", itemIds.size());
        result.addProperty("matching", "ITEM_ID_AND_NBT_IGNORE_STACK_COUNT_PRICE_TAB_AND_ENTRY_UUID");
        return result;
    }

    public List<String> tabNames() {
        return List.copyOf(tabs);
    }

    public List<String> allowedItemIds() {
        return itemIds.stream().sorted().toList();
    }

    public Set<String> trustedCommands() {
        return Set.copyOf(commands);
    }

    public static String identity(CompoundTag type) {
        CompoundTag product = new CompoundTag();
        product.putString("shopEntryTypeID", type.getString("shopEntryTypeID"));
        if (isItem(type)) {
            CompoundTag stack = type.getCompound("itemStack").copy();
            stack.remove("Count");
            if (stack.getCompound("tag").isEmpty()) {
                stack.remove("tag");
            }
            if (stack.getCompound("ForgeCaps").isEmpty()) {
                stack.remove("ForgeCaps");
            }
            product.put("itemStack", stack);
        } else {
            product = type.copy();
        }
        return typedIdentity(product).toString();
    }

    private static JsonElement typedIdentity(Tag tag) {
        JsonArray result = new JsonArray();
        result.add(tag.getId());
        if (tag instanceof CompoundTag compound) {
            JsonObject values = new JsonObject();
            for (String key : new TreeSet<>(compound.getAllKeys())) {
                values.add(key, typedIdentity(compound.get(key)));
            }
            result.add(values);
        } else if (tag instanceof ListTag list) {
            JsonArray values = new JsonArray();
            for (Tag element : list) {
                values.add(typedIdentity(element));
            }
            result.add(values);
        } else {
            result.add(tag.toString());
        }
        return result;
    }

    private static boolean isItem(CompoundTag type) {
        return type.getString("shopEntryTypeID").equals("shopItemEntryType");
    }
}
