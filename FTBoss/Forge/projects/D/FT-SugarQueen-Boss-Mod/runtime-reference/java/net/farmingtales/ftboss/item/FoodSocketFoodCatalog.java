/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.util.FoodSocketFoodRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodSocketFoodCatalog {
    private static final Map<String, FoodWeaponEvolution.FoodType> TYPES = FoodSocketFoodCatalog.loadTypes();

    private FoodSocketFoodCatalog() {
    }

    private static Map<String, FoodWeaponEvolution.FoodType> loadTypes() {
        HashMap<String, FoodWeaponEvolution.FoodType> hashMap = new HashMap<String, FoodWeaponEvolution.FoodType>();
        String string = "/assets/ftboss/food_socket_attributes/types.json";
        try (InputStream inputStream = FoodSocketFoodCatalog.class.getResourceAsStream(string);){
            if (inputStream == null) {
                throw new IllegalStateException("Missing food type catalog");
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                for (Map.Entry entry : JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject().entrySet()) {
                    if (ResourceLocation.m_135820_((String)((String)entry.getKey())) == null) continue;
                    hashMap.put((String)entry.getKey(), FoodWeaponEvolution.FoodType.valueOf(((JsonElement)entry.getValue()).getAsString()));
                }
            }
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodSocketFoods] Ignoring invalid food type catalog", (Throwable)exception);
        }
        return Map.copyOf(hashMap);
    }

    public static FoodWeaponEvolution.FoodType type(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_()) {
            return null;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return null;
        }
        String string = resourceLocation.toString();
        FoodWeaponEvolution.FoodType foodType = TYPES.get(string);
        if (foodType != null && (itemStack.m_41614_() || FoodSocketFoodCatalog.milkContainer(string))) {
            return foodType;
        }
        String string2 = FoodSocketFoodRules.type(string, itemStack.m_41614_(), itemStack.m_204131_().map(tagKey -> tagKey.f_203868_().toString()).toList());
        return string2 == null ? null : FoodWeaponEvolution.FoodType.valueOf(string2);
    }

    private static boolean milkContainer(String string) {
        return string.equals("minecraft:milk_bucket") || string.matches("meadow:wooden_(amethyst_|buffalo_|goat_|grain_|sheep_|warped_)?milk_bucket");
    }

    public static FoodWeaponEvolution.FoodType savedType(FoodWeaponEvolution.FoodType foodType, String string) {
        Item item;
        if (foodType != FoodWeaponEvolution.FoodType.FOOD) {
            return foodType;
        }
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
        Item item2 = item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item == null || item == Items.f_41852_) {
            return foodType;
        }
        FoodWeaponEvolution.FoodType foodType2 = FoodSocketFoodCatalog.type(new ItemStack((ItemLike)item));
        return foodType2 == null ? foodType : foodType2;
    }
}

