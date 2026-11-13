package net.farmingtales.ftdungeonloot;

import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

public record LootProfile(ResourceLocation id, ResourceLocation dimension, ResourceLocation currency,
                          List<ResourceLocation> tables, Map<ResourceLocation, Integer> values,
                          int pricePercent, int enchantmentValue, int minimumPrice, int maximumPrice,
                          int coinMin, int coinMax, String roundSource) {
    public LootProfile {
        tables = List.copyOf(tables);
        values = Map.copyOf(values);
    }

    public Item currencyItem() {
        Item item = ForgeRegistries.ITEMS.containsKey(currency) ? ForgeRegistries.ITEMS.getValue(currency) : null;
        return item == net.minecraft.world.item.Items.AIR ? null : item;
    }

    public int price(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() > stack.getMaxStackSize() || stack.is(currencyItem())) return 0;
        Integer unitValue = values.get(ForgeRegistries.ITEMS.getKey(stack.getItem()));
        if (unitValue == null) return 0;
        long enchantments = EnchantmentHelper.getEnchantments(stack).values().stream().mapToLong(value -> Math.max(0, value)).sum();
        BigInteger value = BigInteger.valueOf(unitValue).add(BigInteger.valueOf(enchantments).multiply(BigInteger.valueOf(enchantmentValue)))
                .multiply(BigInteger.valueOf(stack.getCount()));
        if (stack.isDamageableItem()) {
            long maximum = Math.max(1, stack.getMaxDamage());
            long durability = Math.max(0L, Math.min(maximum, maximum - stack.getDamageValue()));
            value = value.multiply(BigInteger.valueOf(durability)).add(BigInteger.valueOf(maximum - 1)).divide(BigInteger.valueOf(maximum));
        }
        if (value.signum() == 0) return 0;
        BigInteger calculated = value.multiply(BigInteger.valueOf(pricePercent)).add(BigInteger.valueOf(99))
                .divide(BigInteger.valueOf(100)).max(BigInteger.valueOf(minimumPrice));
        return calculated.compareTo(BigInteger.valueOf(maximumPrice)) > 0 ? 0 : calculated.intValueExact();
    }

    public static LootProfile parse(ResourceLocation id, JsonObject json) {
        if (!GsonHelper.getAsString(json, "category").equals("normal_chest")) {
            throw new IllegalArgumentException("Only category normal_chest is permitted");
        }
        List<ResourceLocation> tables = new ArrayList<>();
        for (var element : GsonHelper.getAsJsonArray(json, "loot_tables")) {
            ResourceLocation table = new ResourceLocation(element.getAsString());
            if (table.getPath().contains("boss") || !table.getPath().startsWith("chests/")) {
                throw new IllegalArgumentException("Not an ordinary chest table: " + table);
            }
            if (tables.contains(table)) throw new IllegalArgumentException("Duplicate loot table " + table);
            tables.add(table);
        }
        if (tables.isEmpty() || tables.size() > 32) throw new IllegalArgumentException("Expected 1..32 explicit ordinary chest tables");
        Map<ResourceLocation, Integer> values = new LinkedHashMap<>();
        GsonHelper.getAsJsonObject(json, "item_values").entrySet().forEach(entry -> {
            int value = entry.getValue().getAsBigDecimal().intValueExact();
            if (value < 1 || value > 1000000) throw new IllegalArgumentException("Item value out of range");
            values.put(new ResourceLocation(entry.getKey()), value);
        });
        if (values.isEmpty() || values.size() > 4096) throw new IllegalArgumentException("Expected 1..4096 item values");
        int minimum = integer(json, "minimum_price", 1, 1000000);
        int maximum = integer(json, "maximum_price", minimum, 1000000);
        int coinMin = integer(json, "coin_min", 0, 4096);
        int coinMax = integer(json, "coin_max", coinMin, 4096);
        String roundSource = GsonHelper.getAsString(json, "round_source", "manual");
        if (!roundSource.equals("manual") && !roundSource.equals("ftdungeon")) throw new IllegalArgumentException("Unknown round source");
        return new LootProfile(id, new ResourceLocation(GsonHelper.getAsString(json, "dimension")),
                new ResourceLocation(GsonHelper.getAsString(json, "currency")), tables, values,
                integer(json, "price_percent", 1, 10000), integer(json, "enchantment_level_value", 0, 1000000),
                minimum, maximum, coinMin, coinMax, roundSource);
    }

    private static int integer(JsonObject json, String key, int minimum, int maximum) {
        int value = json.get(key).getAsBigDecimal().intValueExact();
        if (value < minimum || value > maximum) throw new IllegalArgumentException(key + " out of range");
        return value;
    }
}
