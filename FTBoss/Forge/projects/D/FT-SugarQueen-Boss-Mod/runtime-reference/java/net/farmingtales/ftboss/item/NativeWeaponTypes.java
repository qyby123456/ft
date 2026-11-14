/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class NativeWeaponTypes {
    private static final Map<String, String> STAFF = Map.ofEntries(Map.entry("armamentarium:mariana_scepter", "net.gqstavo.armamentarium.item.MarianaScepterItem"), Map.entry("legendary_monsters:wand_of_clouds", "net.miauczel.legendary_monsters.item.custom.WandOfCloudsItem"), Map.entry("irons_spellbooks:staff_of_the_nines", "io.redspace.ironsspellbooks.item.weapons.StaffOfTheNines"), Map.entry("ftboss:frost_queen_staff", "net.farmingtales.ftboss.item.FrostQueenStaffItem"), Map.entry("ftboss:qizai_prayer_staff", "net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem"));
    private static final Map<String, String> BOW = Map.of("armamentarium:ender_maiden_shortbow", "net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "cataclysm:cursed_bow", "com.github.L_Ender.cataclysm.items.Cursed_bow", "cataclysm:wrath_of_the_desert", "com.github.L_Ender.cataclysm.items.Wrath_of_the_desert");

    private NativeWeaponTypes() {
    }

    public static String id(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_()) {
            return "";
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    public static Set<String> types(Class<?> clazz) {
        HashSet<String> hashSet = new HashSet<String>();
        NativeWeaponTypes.collect(clazz, hashSet);
        return Set.copyOf(hashSet);
    }

    private static void collect(Class<?> clazz, Set<String> set) {
        if (clazz == null || !set.add(clazz.getName())) {
            return;
        }
        for (Class<?> clazz2 : clazz.getInterfaces()) {
            NativeWeaponTypes.collect(clazz2, set);
        }
        NativeWeaponTypes.collect(clazz.getSuperclass(), set);
    }

    public static FoodWeaponFamilyRules.Kind kind(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41614_()) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        String string = NativeWeaponTypes.id(itemStack);
        String string2 = string.substring(0, Math.max(0, string.indexOf(58)));
        if (List.of("constructionwand", "videomod", "create", "createaddition").contains(string2) || string.equals("irons_spellbooks:hither_thither_wand") || string.equals("irons_spellbooks:dev_staff")) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        Set<String> set = NativeWeaponTypes.types(itemStack.m_41720_().getClass());
        if (set.contains("io.redspace.ironsspellbooks.api.item.ISpellbook") || set.contains("io.redspace.ironsspellbooks.api.item.IScroll")) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        if (STAFF.containsKey(string)) {
            return set.contains(STAFF.get(string)) ? FoodWeaponFamilyRules.Kind.STAFF : FoodWeaponFamilyRules.Kind.MELEE;
        }
        if (BOW.containsKey(string)) {
            return set.contains(BOW.get(string)) ? FoodWeaponFamilyRules.Kind.BOW : FoodWeaponFamilyRules.Kind.MELEE;
        }
        if (itemStack.m_41720_() instanceof BowItem || itemStack.m_41720_() instanceof CrossbowItem) {
            return FoodWeaponFamilyRules.Kind.BOW;
        }
        if (set.contains("io.redspace.ironsspellbooks.item.weapons.StaffItem")) {
            return FoodWeaponFamilyRules.Kind.STAFF;
        }
        return FoodWeaponFamilyRules.Kind.MELEE;
    }

    public static boolean staff(ItemStack itemStack) {
        return NativeWeaponTypes.kind(itemStack) == FoodWeaponFamilyRules.Kind.STAFF;
    }
}

