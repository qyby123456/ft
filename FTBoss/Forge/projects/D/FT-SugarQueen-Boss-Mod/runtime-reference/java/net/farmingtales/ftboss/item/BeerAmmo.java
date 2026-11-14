/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class BeerAmmo {
    public static final TagKey<Item> BEER_AMMO = TagKey.m_203882_((ResourceKey)Registries.f_256913_, (ResourceLocation)new ResourceLocation("ftboss", "beer_ammo"));
    private static final String[] KEYS = new String[]{"beer", "ale", "lager", "stout", "porter", "pilsner", "cider", "mead", "sake", "wine", "brandy", "rum", "whisky", "whiskey", "vodka", "cocktail", "moonshine"};

    private BeerAmmo() {
    }

    public static boolean isBeer(ItemStack stack) {
        if (stack == null || stack.m_41619_()) {
            return false;
        }
        if (stack.m_204117_(BEER_AMMO)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        if (id == null) {
            return false;
        }
        for (String token : id.m_135815_().split("_")) {
            String t = token.toLowerCase(Locale.ROOT);
            if (t.isEmpty()) continue;
            for (String k : KEYS) {
                if (!t.equals(k) && (k.length() < 5 || !t.startsWith(k))) continue;
                return true;
            }
        }
        return false;
    }

    public static ItemStack findBeer(Player player) {
        ItemStack off = player.m_21206_();
        if (BeerAmmo.isBeer(off)) {
            return off;
        }
        Inventory inv = player.m_150109_();
        for (int i = 0; i < inv.m_6643_(); ++i) {
            ItemStack s = inv.m_8020_(i);
            if (!BeerAmmo.isBeer(s)) continue;
            return s;
        }
        return ItemStack.f_41583_;
    }
}

