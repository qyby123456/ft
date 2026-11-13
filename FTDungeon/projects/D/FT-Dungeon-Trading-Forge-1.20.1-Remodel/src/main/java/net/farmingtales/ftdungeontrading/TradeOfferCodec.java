package net.farmingtales.ftdungeontrading;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Set;
import net.farmingtales.ftdungeontrading.api.TradeCost;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public final class TradeOfferCodec {
    private TradeOfferCodec() {}

    public static TradeOffer parse(ResourceLocation id, JsonObject json) {
        fields(json, Set.of("title", "costs", "rewards"));
        Component title = json.has("title") ? Component.Serializer.fromJson(json.get("title")) : Component.literal(id.toString());
        if (title == null) throw new JsonParseException("title is not a text component");
        var costs = new ArrayList<TradeCost>();
        var rewards = new ArrayList<ItemStack>();
        for (JsonElement entry : GsonHelper.getAsJsonArray(json, "costs")) {
            JsonObject cost = entry.getAsJsonObject();
            fields(cost, Set.of("item", "tag", "count", "nbt", "allow_any_nbt"));
            if (cost.has("item") == cost.has("tag")) throw new JsonParseException("Cost needs exactly one item or tag");
            JsonObject ingredient = new JsonObject();
            String key = cost.has("item") ? "item" : "tag";
            ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(cost, key));
            if (key.equals("item")) requireItem(location);
            ingredient.addProperty(key, location.toString());
            costs.add(new TradeCost(Ingredient.fromJson(ingredient), count(cost), nbt(cost), GsonHelper.getAsBoolean(cost, "allow_any_nbt", false)));
        }
        for (JsonElement entry : GsonHelper.getAsJsonArray(json, "rewards")) {
            JsonObject reward = entry.getAsJsonObject();
            fields(reward, Set.of("item", "count", "nbt"));
            ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(reward, "item"));
            requireItem(location);
            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(location), count(reward));
            stack.setTag(nbt(reward));
            rewards.add(stack);
        }
        return new TradeOffer(id, title, costs, rewards);
    }

    private static int count(JsonObject json) {
        JsonElement value = json.get("count");
        if (value == null) return 1;
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("[0-9]{1,4}")) {
            throw new JsonParseException("count must be an integer 1..4096");
        }
        int count = value.getAsInt();
        if (count < 1 || count > 4096) throw new JsonParseException("count must be 1..4096");
        return count;
    }

    private static CompoundTag nbt(JsonObject json) {
        if (!json.has("nbt")) return null;
        String source = GsonHelper.getAsString(json, "nbt");
        if (source.length() > 16384) throw new JsonParseException("NBT exceeds 16384 characters");
        try { return TagParser.parseTag(source); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            throw new JsonParseException("Invalid SNBT", exception);
        }
    }

    private static void requireItem(ResourceLocation id) {
        if (!ForgeRegistries.ITEMS.containsKey(id) || ForgeRegistries.ITEMS.getValue(id) == Items.AIR) {
            throw new JsonParseException("Unknown or air item: " + id);
        }
    }

    private static void fields(JsonObject json, Set<String> allowed) {
        for (String key : json.keySet()) if (!allowed.contains(key)) throw new JsonParseException("Unknown field: " + key);
    }
}
