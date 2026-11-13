package net.farmingtales.ftdungeontrading.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class TradeOffer {
    private final ResourceLocation id;
    private final Component title;
    private final List<TradeCost> costs;
    private final List<ItemStack> rewards;

    public TradeOffer(ResourceLocation id, Component title, List<TradeCost> costs, List<ItemStack> rewards) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title).copy();
        if (costs.isEmpty() || costs.size() > 8 || rewards.isEmpty() || rewards.size() > 8) {
            throw new IllegalArgumentException("Each trade side must contain 1..8 entries");
        }
        this.costs = List.copyOf(costs);
        this.rewards = rewards.stream().map(stack -> {
            if (stack.isEmpty() || stack.getCount() < 1 || stack.getCount() > 4096) {
                throw new IllegalArgumentException("A reward requires a valid item and count 1..4096");
            }
            return stack.copy();
        }).toList();
    }

    public ResourceLocation id() { return id; }
    public Component title() { return title.copy(); }
    public List<TradeCost> costs() { return costs; }
    public List<ItemStack> rewards() { return rewards.stream().map(ItemStack::copy).toList(); }

    public Component costDescription() {
        return join(costs.stream().map(TradeCost::description).toList());
    }

    public Component rewardDescription() {
        return join(rewards.stream().map(stack -> (Component) Component.translatable(
                "message.ftdungeontrading.entry", stack.getCount(), stack.getDisplayName())).toList());
    }

    public CompoundTag signature() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("title", Component.Serializer.toJson(title));
        ListTag payments = new ListTag();
        costs.forEach(cost -> payments.add(cost.signature()));
        ListTag products = new ListTag();
        rewards.forEach(stack -> {
            CompoundTag product = new CompoundTag();
            product.putString("item", net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            product.putInt("count", stack.getCount());
            if (stack.hasTag()) product.put("nbt", stack.getTag().copy());
            products.add(product);
        });
        tag.put("costs", payments);
        tag.put("rewards", products);
        return tag;
    }

    private static Component join(List<Component> entries) {
        MutableComponent result = Component.empty();
        for (int index = 0; index < entries.size(); index++) {
            if (index > 0) result.append(Component.literal(" + "));
            result.append(entries.get(index));
        }
        return result;
    }
}
