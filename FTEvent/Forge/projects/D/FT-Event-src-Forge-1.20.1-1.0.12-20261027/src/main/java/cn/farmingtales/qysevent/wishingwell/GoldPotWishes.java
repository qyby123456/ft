package cn.farmingtales.qysevent.wishingwell;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.loading.FMLPaths;

public final class GoldPotWishes {
    private static final String[] FOODS = {
            "farmersdelight:beef_stew", "farmersdelight:vegetable_soup",
            "farmersdelight:chicken_soup", "farmersdelight:cooked_rice",
            "farmersdelight:roasted_mutton_chops", "farmersdelight:pasta_with_meatballs",
            "farmersdelight:fish_stew", "minecraft:pumpkin_pie"
    };
    private static final String[] SKILL_BOOKS = {
            "farmingtales:farming_experience_book", "farmingtales:husbandry_experience_book",
            "farmingtales:mining_experience_book", "farmingtales:artisan_experience_book",
            "farmingtales:fishing_experience_book", "farmingtales:combat_experience_book"
    };
    public record Reward(String category, ItemStack stack) {}

    private GoldPotWishes() {}

    public static List<Reward> rollRewards(ServerLevel level, long previousPots, int donatedPots) {
        return generateRewards(level, GoldPotWishRules.eligiblePots(previousPots, donatedPots));
    }

    public static List<Reward> rollUnlockedRewards(ServerLevel level, int donatedPots) {
        return generateRewards(level, GoldPotWishRules.unlockedRolls(donatedPots));
    }

    private static List<Reward> generateRewards(ServerLevel level, int rolls) {
        GoldPotCoinRewards coins;
        try {
            coins = GoldPotCoinRewards.load(FMLPaths.CONFIGDIR.get()
                    .resolve("qys_event/wishing_well/gold_pot_coins.json"));
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot read config/qys_event/wishing_well/gold_pot_coins.json", error);
        }
        List<Reward> rewards = new ArrayList<>(rolls);
        for (int index = 0; index < rolls; index++) {
            GoldPotWishRules.Category category = GoldPotWishRules.categoryForRoll(
                    level.getRandom().nextInt(GoldPotWishRules.TOTAL_WEIGHT));
            rewards.add(new Reward(category.id(), createReward(level.getRandom(), category, coins)));
        }
        return rewards;
    }

    private static ItemStack createReward(RandomSource random, GoldPotWishRules.Category category, GoldPotCoinRewards coins) {
        return switch (category) {
            case SPELL -> randomScroll(random);
            case FOOD -> registeredStack(FOODS[random.nextInt(FOODS.length)], 1 + random.nextInt(3));
            case MONEY -> coinReward(coins.coinsForRoll(random.nextInt(coins.totalWeight())));
            case GEODE -> registeredStack("farmingtales:geode", 1);
            case MINERAL_BLOCK -> mineralBlock(random);
            case SKILL_EXPERIENCE -> registeredStack(SKILL_BOOKS[random.nextInt(SKILL_BOOKS.length)], 1);
            case MANOR_REWARD -> manorReward(random);
        };
    }

    private static ItemStack coinReward(int coins) {
        if (coins == 1) return registeredStack("kubejs:coin", 1);
        if (coins == 100) return registeredStack("farmingtales:gold_bundle", 1);
        if (coins == 1000) return registeredStack("farmingtales:gold_chest", 1);
        return registeredStack("farmingtales:gold_bucket", coins / 10000);
    }

    private static ItemStack registeredStack(String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(id));
        if (item == null || item == Items.AIR) throw new IllegalStateException("Wish reward unavailable: " + id);
        return new ItemStack(item, count);
    }

    private static ItemStack mineralBlock(RandomSource random) {
        GoldPotWishRules.Mineral mineral = GoldPotWishRules.mineralForRoll(
                random.nextInt(GoldPotWishRules.MINERAL_TOTAL_WEIGHT));
        return registeredStack(mineral.id(), 1 + random.nextInt(2));
    }

    private static ItemStack randomScroll(RandomSource random) {
        try {
            Class<?> registry = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
            Class<?> spellType = Class.forName("io.redspace.ironsspellbooks.api.spells.AbstractSpell");
            Method spellId = spellType.getMethod("getSpellId");
            Method allowLooting = spellType.getMethod("allowLooting");
            Method minLevel = spellType.getMethod("getMinLevel");
            Method maxLevel = spellType.getMethod("getMaxLevel");
            List<?> enabled = (List<?>) registry.getMethod("getEnabledSpells").invoke(null);
            List<Object> spells = new ArrayList<>();
            for (Object spell : enabled) {
                ResourceLocation id = ResourceLocation.parse(String.valueOf(spellId.invoke(spell)));
                int minimum = ((Number) minLevel.invoke(spell)).intValue();
                int maximum = ((Number) maxLevel.invoke(spell)).intValue();
                if (id.getNamespace().equals("irons_spellbooks") && Boolean.TRUE.equals(allowLooting.invoke(spell))
                        && minimum >= 1 && maximum >= minimum) spells.add(spell);
            }
            if (spells.isEmpty()) throw new IllegalStateException("No lootable Iron spells available");
            Object spell = spells.get(random.nextInt(spells.size()));
            int minimum = ((Number) minLevel.invoke(spell)).intValue();
            int maximum = Math.min(((Number) maxLevel.invoke(spell)).intValue(), Math.max(minimum, 3));
            int spellLevel = minimum + random.nextInt(maximum - minimum + 1);
            ItemStack scroll = registeredStack("irons_spellbooks:scroll", 1);
            Class<?> container = Class.forName("io.redspace.ironsspellbooks.api.spells.ISpellContainer");
            container.getMethod("createScrollContainer", spellType, int.class, ItemStack.class)
                    .invoke(null, spell, spellLevel, scroll);
            Object written = container.getMethod("get", ItemStack.class).invoke(null, scroll);
            Object[] slots = (Object[]) container.getMethod("getAllSpells").invoke(written);
            Class<?> slotType = Class.forName("io.redspace.ironsspellbooks.api.spells.SpellSlot");
            for (Object slot : slots) {
                if (slot != null && spell.equals(slotType.getMethod("getSpell").invoke(slot))
                        && ((Number) slotType.getMethod("getLevel").invoke(slot)).intValue() == spellLevel) {
                    return scroll;
                }
            }
            throw new IllegalStateException("Wish scroll contents were not written");
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Iron spell wish integration unavailable", error);
        }
    }

    private static ItemStack manorReward(RandomSource random) {
        try {
            Class<?> loot = Class.forName("net.farmingtales.ftboss.boss.ManorVictoryLoot");
            List<?> contents = (List<?>) loot.getMethod("contents").invoke(null);
            List<ItemStack> rewards = new ArrayList<>();
            for (Object entry : contents) {
                if (entry instanceof ItemStack stack && !stack.isEmpty()) rewards.add(stack);
            }
            if (rewards.isEmpty()) throw new IllegalStateException("No manor victory rewards available");
            ItemStack reward = rewards.get(random.nextInt(rewards.size())).copy();
            reward.setCount(1);
            return reward;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Manor victory wish integration unavailable", error);
        }
    }
}
