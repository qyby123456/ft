package net.farmingtales.ftboss.item;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.util.FoodSocketFoodRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/** 食物羁绊只读分类目录；2026-10-21 补齐拼音菜名及品牌酒，旧通用插槽只在读取时重新分类，不改 NBT。 */
public final class FoodSocketFoodCatalog {
    private static final Map<String, FoodType> TYPES = loadTypes();

    private FoodSocketFoodCatalog() {}

    private static Map<String, FoodType> loadTypes() {
        Map<String, FoodType> types = new HashMap<>();
        String path = "/assets/ftboss/food_socket_attributes/types.json";
        try (InputStream stream = FoodSocketFoodCatalog.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing food type catalog");
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                for (var entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                    if (ResourceLocation.tryParse(entry.getKey()) != null) {
                        types.put(entry.getKey(), FoodType.valueOf(entry.getValue().getAsString()));
                    }
                }
            }
        } catch (Exception failure) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodSocketFoods] Ignoring invalid food type catalog", failure);
        }
        return Map.copyOf(types);
    }

    public static FoodType type(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return null;
        String id = key.toString();
        FoodType explicit = TYPES.get(id);
        if (explicit != null && (stack.isEdible() || milkContainer(id))) return explicit;
        String type = FoodSocketFoodRules.type(id, stack.isEdible(), stack.getTags().map(tag -> tag.location().toString()).toList());
        return type == null ? null : FoodType.valueOf(type);
    }

    private static boolean milkContainer(String id) {
        return id.equals("minecraft:milk_bucket") || id.matches("meadow:wooden_(amethyst_|buffalo_|goat_|grain_|sheep_|warped_)?milk_bucket");
    }

    public static FoodType savedType(FoodType stored, String identifier) {
        if (stored != FoodType.FOOD) return stored;
        ResourceLocation id = ResourceLocation.tryParse(identifier);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) return stored;
        FoodType resolved = type(new ItemStack(item));
        return resolved == null ? stored : resolved;
    }
}
