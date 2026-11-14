/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponCavityMenuRules {
    private WeaponCavityMenuRules() {
    }

    public static String shapeFamily(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.profile(itemStack);
            return profile == null ? "sword" : profile.family();
        }
        String string = BuiltInRegistries.f_257033_.m_7981_((Object)itemStack.m_41720_()).toString();
        SwordCompatibilityRules.Definition definition = SwordCompatibilityRules.definitions().get(string);
        return string.equals("minecraft:stone_sword") ? "stone_sword" : (definition != null && definition.form() == SwordCompatibilityRules.Form.AXE ? "axe" : "sword");
    }

    public static int[] layout(int[] nArray, ItemStack itemStack) {
        String string;
        int[] nArray2 = SwordCavityExtensions.validateLayout(nArray);
        int[] nArray3 = new int[27];
        Arrays.setAll(nArray3, n -> SwordNativeMaterialRules.visualRow(n) * 9 + SwordNativeMaterialRules.visualColumn(n));
        if (!Arrays.equals(nArray2, nArray3)) {
            return nArray2;
        }
        ArrayList<String> arrayList = new ArrayList<String>(Collections.nCopies(27, ""));
        List<Integer> list = new ArrayList<Integer>(Collections.nCopies(27, 0));
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.profile(itemStack);
            if (profile == null) {
                return nArray2;
            }
            string = profile.family();
            profile.nativeCells().forEach((n, cell) -> arrayList.set((int)n, cell.itemId()));
            list = profile.unlocks();
        } else {
            if (!SwordCavityBuild.applies(itemStack)) {
                return nArray2;
            }
            string = WeaponCavityMenuRules.shapeFamily(itemStack);
            List<SwordNativeMaterialRules.Piece> list2 = SwordNativeMaterials.parts(itemStack);
            if (list2.size() != 27) {
                return nArray2;
            }
            for (int i = 0; i < list2.size(); ++i) {
                arrayList.set(i, list2.get(i).itemId());
            }
        }
        return SwordCavityExtensions.validateLayout(WeaponCavityShapeRules.layout(string, arrayList, list));
    }

    public static boolean applies(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack);
    }

    public static int revision(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.revision(itemStack) : SwordCavityBuild.revision(itemStack);
    }

    public static List<ItemStack> containerItems(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.slots(itemStack) : SwordCavityBuild.containerItems(itemStack);
    }

    public static boolean containerEditable(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.containerEditable(itemStack) : SwordCavityBuild.containerEditable(itemStack);
    }

    public static boolean canSetContainerItems(ItemStack itemStack, List<ItemStack> list) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.canSetContainerItems(itemStack, list) : SwordCavityBuild.canSetContainerItems(itemStack, list);
    }

    public static boolean setContainerItems(ServerPlayer serverPlayer, ItemStack itemStack, List<ItemStack> list, int n) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.setContainerItems(serverPlayer, itemStack, list, n) : SwordCavityBuild.setContainerItems(serverPlayer, itemStack, list, n);
    }

    public static boolean reference(ItemStack itemStack, int n) {
        if (n < 0 || n >= 27) {
            return false;
        }
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.reference(itemStack, n) : SwordCavityBuild.applies(itemStack) && SwordCavityBuild.cells(itemStack).get(n).present();
    }

    public static boolean clearReference(ServerPlayer serverPlayer, ItemStack itemStack, SwordCavityRules.Slot slot, int n) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.replace(serverPlayer, itemStack, slot.ordinal(), -1, n) : SwordCavityBuild.clearReference(serverPlayer, itemStack, slot, n);
    }

    public static boolean cellUnlocked(ItemStack itemStack, int n) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.cellUnlocked(itemStack, n) : SwordCavityBuild.cellUnlocked(itemStack, n);
    }
}

