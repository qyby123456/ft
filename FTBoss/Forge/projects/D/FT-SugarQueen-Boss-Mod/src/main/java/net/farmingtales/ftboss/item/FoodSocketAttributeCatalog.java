package net.farmingtales.ftboss.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Color;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Ingredient;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Processing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 食材属性只读目录：基础价快照分片及人工颜色/加工标签；2026-10-12，不依赖或修改价格模组。 */
public final class FoodSocketAttributeCatalog {
    private static final String PATH = "/assets/ftboss/food_socket_attributes/";
    private static final int PRICE_PARTS = 8;
    private static final Map<String, Double> PRICES = loadPrices();
    private static final Map<String, Trait> TRAITS = loadTraits();

    private record Trait(Color color, Processing processing) {}

    private FoodSocketAttributeCatalog() {}

    private static JsonObject read(String file) {
        try (InputStream stream = FoodSocketAttributeCatalog.class.getResourceAsStream(PATH + file)) {
            if (stream == null) throw new IllegalStateException("Missing attribute resource " + file);
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception failure) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodSocketAttributes] Ignoring invalid catalog {}", file, failure);
            return new JsonObject();
        }
    }

    private static Map<String, Double> loadPrices() {
        Map<String, Double> prices = new HashMap<>();
        for (int index = 0; index < PRICE_PARTS; index++) {
            for (Map.Entry<String, JsonElement> entry : read("prices_" + index + ".json").entrySet()) {
                try {
                    double price = entry.getValue().getAsDouble();
                    if (ResourceLocation.tryParse(entry.getKey()) != null && Double.isFinite(price) && price > 0.0D) {
                        prices.put(entry.getKey(), price);
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        return Map.copyOf(prices);
    }

    private static Map<String, Trait> loadTraits() {
        Map<String, Trait> traits = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : read("traits.json").entrySet()) {
            try {
                JsonObject data = entry.getValue().getAsJsonObject();
                if (ResourceLocation.tryParse(entry.getKey()) == null) continue;
                traits.put(entry.getKey(), new Trait(Color.valueOf(data.get("color").getAsString()),
                        Processing.valueOf(data.get("processing").getAsString())));
            } catch (RuntimeException ignored) {
            }
        }
        return Map.copyOf(traits);
    }

    public static Ingredient ingredient(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return null;
        Trait trait = TRAITS.get(id.toString());
        if (!stack.isEdible() && trait == null) return null;
        return descriptor(id.toString(), id.getNamespace());
    }

    public static Ingredient descriptor(String id, String namespace) {
        Trait trait = TRAITS.get(id);
        return new Ingredient(id, namespace, PRICES.getOrDefault(id, Double.NaN),
                trait == null ? Color.UNKNOWN : trait.color, trait == null ? Processing.UNKNOWN : trait.processing);
    }
}
