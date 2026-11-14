/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodSocketAttributeCatalog {
    private static final String PATH = "/assets/ftboss/food_socket_attributes/";
    private static final int PRICE_PARTS = 8;
    private static final Map<String, Double> PRICES = FoodSocketAttributeCatalog.loadPrices();
    private static final Map<String, Trait> TRAITS = FoodSocketAttributeCatalog.loadTraits();

    private FoodSocketAttributeCatalog() {
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static JsonObject read(String string) {
        try (InputStream inputStream = FoodSocketAttributeCatalog.class.getResourceAsStream(PATH + string);){
            JsonObject jsonObject;
            if (inputStream == null) {
                throw new IllegalStateException("Missing attribute resource " + string);
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                jsonObject = JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject();
            }
            return jsonObject;
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodSocketAttributes] Ignoring invalid catalog {}", (Object)string, (Object)exception);
            return new JsonObject();
        }
    }

    private static Map<String, Double> loadPrices() {
        HashMap<String, Double> hashMap = new HashMap<String, Double>();
        for (int i = 0; i < 8; ++i) {
            for (Map.Entry entry : FoodSocketAttributeCatalog.read("prices_" + i + ".json").entrySet()) {
                try {
                    double d = ((JsonElement)entry.getValue()).getAsDouble();
                    if (ResourceLocation.m_135820_((String)((String)entry.getKey())) == null || !Double.isFinite(d) || !(d > 0.0)) continue;
                    hashMap.put((String)entry.getKey(), d);
                }
                catch (RuntimeException runtimeException) {}
            }
        }
        return Map.copyOf(hashMap);
    }

    private static Map<String, Trait> loadTraits() {
        HashMap<String, Trait> hashMap = new HashMap<String, Trait>();
        for (Map.Entry entry : FoodSocketAttributeCatalog.read("traits.json").entrySet()) {
            try {
                JsonObject jsonObject = ((JsonElement)entry.getValue()).getAsJsonObject();
                if (ResourceLocation.m_135820_((String)((String)entry.getKey())) == null) continue;
                hashMap.put((String)entry.getKey(), new Trait(FoodSocketAttributeRules.Color.valueOf(jsonObject.get("color").getAsString()), FoodSocketAttributeRules.Processing.valueOf(jsonObject.get("processing").getAsString())));
            }
            catch (RuntimeException runtimeException) {}
        }
        return Map.copyOf(hashMap);
    }

    public static FoodSocketAttributeRules.Ingredient ingredient(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_()) {
            return null;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return null;
        }
        Trait trait = TRAITS.get(resourceLocation.toString());
        if (!itemStack.m_41614_() && trait == null) {
            return null;
        }
        return FoodSocketAttributeCatalog.descriptor(resourceLocation.toString(), resourceLocation.m_135827_());
    }

    public static FoodSocketAttributeRules.Ingredient descriptor(String string, String string2) {
        Trait trait = TRAITS.get(string);
        return new FoodSocketAttributeRules.Ingredient(string, string2, PRICES.getOrDefault(string, Double.NaN), trait == null ? FoodSocketAttributeRules.Color.UNKNOWN : trait.color, trait == null ? FoodSocketAttributeRules.Processing.UNKNOWN : trait.processing);
    }

    private record Trait(FoodSocketAttributeRules.Color color, FoodSocketAttributeRules.Processing processing) {
    }
}

