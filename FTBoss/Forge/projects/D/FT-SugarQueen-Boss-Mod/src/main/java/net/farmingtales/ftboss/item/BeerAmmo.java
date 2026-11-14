package net.farmingtales.ftboss.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

/**
 * 【酒桶弩的弹药判定】(2026-09-14 v123 按用户要求「装填任意啤酒」)。
 *
 * <p>用户原话是"装填**任意**啤酒" —— 整合包里做酒的模组不少
 * (Let's Do Brewery / Vinery / Brewin' And Chewin' / 妖怪盛宴…), 物品 id 各式各样,
 * 硬编码一份清单肯定会漏。所以这里按**物品 id 的词元**判定:
 * 把 path 按 {@code _} 切开, 任一词元命中关键词就算酒
 * (例: {@code brewery:beer}、{@code ...:pale_ale}、{@code vinery:red_wine}、
 * {@code ...:rum}、{@code ...:whiskey} 全都算)。
 *
 * <p>短词(< 5 字母, 如 ale/wine/rum/sake)要求**词元完全相等**, 免得误伤
 * "sale"/"pale"/"rumble" 这类名字; 长词允许前缀匹配(whisky/whiskey、pilsner…)。
 *
 * <p>另外留了一个数据包标签 {@code ftboss:beer_ammo}: 往
 * {@code data/<你的命名空间>/tags/items/beer_ammo.json} 里塞 id 就能补上
 * 关键词没覆盖到的酒(标签里的**任何**物品都算)。
 */
public final class BeerAmmo {

    /** 数据包可扩展的酒类标签: {@code ftboss:beer_ammo} */
    public static final TagKey<Item> BEER_AMMO =
            TagKey.create(Registries.ITEM, new ResourceLocation("ftboss", "beer_ammo"));

    /** 词元关键词(命中即算酒) */
    private static final String[] KEYS = {
            "beer", "ale", "lager", "stout", "porter", "pilsner",
            "cider", "mead", "sake", "wine", "brandy", "rum",
            "whisky", "whiskey", "vodka", "cocktail", "moonshine",
    };

    private BeerAmmo() {}

    /** 这一叠物品能不能当"啤酒"塞进酒桶弩 */
    public static boolean isBeer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(BEER_AMMO)) return true;      // 数据包标签优先
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        for (String token : id.getPath().split("_")) {
            String t = token.toLowerCase(Locale.ROOT);
            if (t.isEmpty()) continue;
            for (String k : KEYS) {
                if (t.equals(k) || (k.length() >= 5 && t.startsWith(k))) return true;
            }
        }
        return false;
    }

    /** 找玩家身上第一瓶能用的酒: 副手 -> 背包(含主手/盔甲槽, 顺序遍历) */
    public static ItemStack findBeer(Player player) {
        ItemStack off = player.getOffhandItem();
        if (isBeer(off)) return off;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (isBeer(s)) return s;
        }
        return ItemStack.EMPTY;
    }
}
