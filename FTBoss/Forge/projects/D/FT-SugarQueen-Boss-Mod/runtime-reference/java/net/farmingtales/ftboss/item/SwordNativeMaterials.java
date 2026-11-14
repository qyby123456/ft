/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TieredItem
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class SwordNativeMaterials {
    public static final String ROOT = "FTBossSwordNativeMaterials";
    private static final int VERSION = 1;

    private SwordNativeMaterials() {
    }

    private static String itemId(ItemStack itemStack) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    private static boolean netherTheme(String string) {
        return !string.startsWith("minecraft:") && string.matches(".*(?:nether|infernal|incinerator|ignit|blaze|flame|firebrand|demon).*");
    }

    public static List<SwordNativeMaterialRules.Piece> budget(ItemStack itemStack) {
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.saved(itemStack);
        return list.isEmpty() ? SwordNativeMaterials.defaults(itemStack) : list;
    }

    private static Map<String, Integer> budget(List<SwordNativeMaterialRules.Piece> list) {
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (SwordNativeMaterialRules.Piece piece : list) {
            if (piece.origin() != SwordNativeMaterialRules.Origin.RECIPE || piece.count() <= 0) continue;
            linkedHashMap.merge(piece.itemId(), piece.count(), Integer::sum);
        }
        return linkedHashMap;
    }

    private static List<SwordNativeMaterialRules.Piece> ingredients(Recipe<?> recipe) {
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
        for (Ingredient ingredient : recipe.m_7527_()) {
            String string;
            ArrayList<ItemStack> arrayList2 = new ArrayList<ItemStack>(List.of(ingredient.m_43908_()));
            arrayList2.removeIf(ItemStack::m_41619_);
            arrayList2.sort(Comparator.comparing(SwordNativeMaterials::itemId));
            if (arrayList2.isEmpty() || (string = SwordNativeMaterials.itemId((ItemStack)arrayList2.get(0))).endsWith("_smithing_template")) continue;
            if (string.equals("minecraft:netherite_ingot")) {
                arrayList.add(new SwordNativeMaterialRules.Piece("minecraft:netherite_scrap", 4, SwordNativeMaterialRules.Origin.RECIPE));
                arrayList.add(new SwordNativeMaterialRules.Piece("minecraft:gold_nugget", 36, SwordNativeMaterialRules.Origin.RECIPE));
                continue;
            }
            List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterialRules.vanilla(string);
            if (list.isEmpty()) {
                arrayList.add(new SwordNativeMaterialRules.Piece(string, 1, SwordNativeMaterialRules.Origin.RECIPE));
                continue;
            }
            for (SwordNativeMaterialRules.Piece piece : list) {
                if (piece.count() <= 0) continue;
                arrayList.add(new SwordNativeMaterialRules.Piece(piece.itemId(), piece.count(), SwordNativeMaterialRules.Origin.RECIPE));
            }
        }
        return arrayList;
    }

    public static void capture(ItemStack itemStack, Level level) {
        if (!SwordCavityBuild.applies(itemStack) || itemStack.m_41737_(ROOT) != null) {
            return;
        }
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.budget(itemStack, level);
        if (list.size() < 4 || list.size() > 27) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("Version", 1);
        compoundTag.m_128405_("Size", list.size());
        compoundTag.m_128359_("Weapon", SwordNativeMaterials.itemId(itemStack));
        CompoundTag compoundTag2 = new CompoundTag();
        for (int i = 0; i < list.size(); ++i) {
            SwordNativeMaterialRules.Piece piece = list.get(i);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128359_("Id", piece.itemId());
            compoundTag3.m_128405_("Count", piece.count());
            compoundTag3.m_128359_("Origin", piece.origin().name());
            compoundTag2.m_128365_(Integer.toString(i), (Tag)compoundTag3);
        }
        compoundTag.m_128365_("Parts", (Tag)compoundTag2);
        itemStack.m_41784_().m_128365_(ROOT, (Tag)compoundTag);
    }

    public static ItemStack display(ItemStack itemStack, int n, List<SwordNativeMaterialRules.Piece> list) {
        if (!SwordNativeMaterialRules.hasPiece(list, n)) {
            return ItemStack.f_41583_;
        }
        SwordNativeMaterialRules.Piece piece = list.get(n);
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)piece.itemId());
        if (resourceLocation == null || !ForgeRegistries.ITEMS.containsKey(resourceLocation)) {
            return ItemStack.f_41583_;
        }
        return new ItemStack((ItemLike)ForgeRegistries.ITEMS.getValue(resourceLocation), 1);
    }

    public static List<SwordNativeMaterialRules.Piece> defaults(ItemStack itemStack) {
        List<SwordNativeMaterialRules.Piece> list = SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), List.of());
        if (!list.isEmpty()) {
            return list;
        }
        if (!SwordCavityBuild.applies(itemStack)) {
            return List.of();
        }
        String string = SwordNativeMaterials.itemId(itemStack);
        if (string.equals("minecraft:iron_sword")) {
            return SwordNativeMaterialRules.ironPrototype();
        }
        List<SwordNativeMaterialRules.Piece> list2 = SwordNativeMaterialRules.vanilla(string);
        if (!list2.isEmpty()) {
            return list2;
        }
        Item item = itemStack.m_41720_();
        if (!(item instanceof TieredItem)) {
            return List.of();
        }
        Tier tier = ((TieredItem)item).m_43314_();
        String string2 = tier == Tiers.WOOD ? "wooden" : (tier == Tiers.STONE ? "stone" : (tier == Tiers.IRON ? "iron" : (tier == Tiers.GOLD ? "golden" : (tier == Tiers.DIAMOND ? "diamond" : (tier == Tiers.NETHERITE ? "netherite" : "")))));
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
        if (!string2.isEmpty()) {
            arrayList.addAll(SwordNativeMaterialRules.vanilla("minecraft:" + string2 + "_sword"));
        } else {
            for (ItemStack itemStack2 : tier.m_6282_().m_43908_()) {
                if (itemStack2.m_41619_()) continue;
                arrayList.add(new SwordNativeMaterialRules.Piece(SwordNativeMaterials.itemId(itemStack2), 2, SwordNativeMaterialRules.Origin.RECIPE));
                break;
            }
            if (arrayList.isEmpty()) {
                arrayList.add(new SwordNativeMaterialRules.Piece(string, 0, SwordNativeMaterialRules.Origin.SHARED));
            }
            arrayList.add(new SwordNativeMaterialRules.Piece("minecraft:stick", 1, SwordNativeMaterialRules.Origin.RECIPE));
        }
        List<SwordNativeMaterialRules.Piece> list3 = SwordNativeMaterialRules.recipe(arrayList.stream().filter(piece -> piece.count() > 0).toList(), SwordNativeMaterials.netherTheme(string));
        return list3.stream().map(piece -> piece.origin() == SwordNativeMaterialRules.Origin.SHARED ? piece : new SwordNativeMaterialRules.Piece(piece.itemId(), piece.count(), SwordNativeMaterialRules.Origin.THEME)).toList();
    }

    public static List<SwordNativeMaterialRules.Piece> parts(ItemStack itemStack) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), List.of()).isEmpty()) {
            return SwordCompatibilityRules.nativeLayout(SwordNativeMaterials.itemId(itemStack), SwordNativeMaterials.budget(itemStack));
        }
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.budget(itemStack);
        return SwordNativeMaterials.itemId(itemStack).equals("minecraft:iron_sword") ? SwordNativeMaterialRules.ironPrototypeLayout(list) : SwordNativeMaterialRules.layout(list);
    }

    public static List<SwordNativeMaterialRules.Piece> parts(ItemStack itemStack, Level level) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), List.of()).isEmpty()) {
            return SwordCompatibilityRules.nativeLayout(SwordNativeMaterials.itemId(itemStack), SwordNativeMaterials.budget(itemStack, level));
        }
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.budget(itemStack, level);
        return SwordNativeMaterials.itemId(itemStack).equals("minecraft:iron_sword") ? SwordNativeMaterialRules.ironPrototypeLayout(list) : SwordNativeMaterialRules.layout(list);
    }

    public static List<SwordNativeMaterialRules.Piece> budget(ItemStack itemStack, Level level) {
        if (!SwordCavityBuild.applies(itemStack)) {
            return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), List.of());
        }
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.saved(itemStack);
        if (!list.isEmpty() || itemStack.m_41737_(ROOT) != null) {
            return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), list.isEmpty() ? SwordNativeMaterials.defaults(itemStack) : list);
        }
        if (level == null) {
            return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), SwordNativeMaterials.defaults(itemStack));
        }
        String string = SwordNativeMaterials.itemId(itemStack);
        ResourceLocation resourceLocation = SwordCavityExtensions.nativeRecipe(itemStack);
        ArrayList<Recipe> arrayList = new ArrayList<Recipe>(level.m_7465_().m_44051_());
        arrayList.sort(Comparator.comparingInt(recipe -> recipe.m_6423_().equals((Object)resourceLocation) ? 0 : 1).thenComparing(recipe -> recipe.m_6423_().toString()));
        for (Recipe recipe2 : arrayList) {
            List<SwordNativeMaterialRules.Piece> list2;
            List<SwordNativeMaterialRules.Piece> list3;
            block7: {
                if (!(recipe2 instanceof CraftingRecipe) && !(recipe2 instanceof SmithingRecipe)) continue;
                try {
                    List<SwordNativeMaterialRules.Piece> list4;
                    ItemStack itemStack2 = recipe2.m_8043_(level.m_9598_());
                    if (itemStack2.m_41619_() || itemStack2.m_41720_() != itemStack.m_41720_() || itemStack2.m_41613_() != 1 || (list4 = SwordNativeMaterials.ingredients(recipe2)).isEmpty() || (list3 = SwordNativeMaterialRules.recipe(list4, SwordNativeMaterials.netherTheme(string))).isEmpty()) continue;
                    list2 = SwordNativeMaterialRules.vanilla(string);
                    if (!string.equals("minecraft:iron_sword") || !SwordNativeMaterialRules.ironRecipe(list3)) break block7;
                    return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), SwordNativeMaterialRules.ironPrototype());
                }
                catch (RuntimeException runtimeException) {
                    continue;
                }
            }
            return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), !list2.isEmpty() && SwordNativeMaterials.budget(list2).equals(SwordNativeMaterials.budget(list3)) ? list2 : list3);
        }
        return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), SwordNativeMaterials.defaults(itemStack));
    }

    private static List<SwordNativeMaterialRules.Piece> saved(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag == null || compoundTag.m_128451_("Version") != 1) {
            return List.of();
        }
        if (!compoundTag.m_128461_("Weapon").equals(SwordNativeMaterials.itemId(itemStack))) {
            return List.of();
        }
        int n = compoundTag.m_128451_("Size");
        if (n < 4 || n > 27) {
            return List.of();
        }
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
        try {
            for (int i = 0; i < n; ++i) {
                CompoundTag compoundTag2 = compoundTag.m_128469_("Parts").m_128469_(Integer.toString(i));
                SwordNativeMaterialRules.Piece piece = new SwordNativeMaterialRules.Piece(compoundTag2.m_128461_("Id"), compoundTag2.m_128451_("Count"), SwordNativeMaterialRules.Origin.valueOf(compoundTag2.m_128461_("Origin")));
                ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)piece.itemId());
                if (resourceLocation == null || !ForgeRegistries.ITEMS.containsKey(resourceLocation) || (piece.origin() == SwordNativeMaterialRules.Origin.SHARED ? piece.count() != 0 : piece.count() <= 0)) {
                    return List.of();
                }
                arrayList.add(piece);
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return List.of();
        }
        return SwordCompatibilityRules.upgradeReferenceBudget(SwordNativeMaterials.itemId(itemStack), List.copyOf(arrayList));
    }
}

