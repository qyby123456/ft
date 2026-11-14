package net.farmingtales.ftboss.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCompatibilityRules.Definition;
import net.farmingtales.ftboss.util.SwordCompatibilityRules.Form;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponCavityMenuRules {
    private WeaponCavityMenuRules() {
    }

    public static String shapeFamily(ItemStack var0) {
        if (FourWeaponFoundationAdapter.applies(var0)) {
            Profile var3 = FourWeaponFoundationAdapter.profile(var0);
            return var3 == null ? "sword" : var3.family();
        } else {
            String var1 = BuiltInRegistries.ITEM.getKey(var0.getItem()).toString();
            Definition var2 = SwordCompatibilityRules.definitions().get(var1);
            return var1.equals("minecraft:stone_sword") ? "stone_sword" : (var2 != null && var2.form() == Form.AXE ? "axe" : "sword");
        }
    }

    public static int[] layout(int[] var0, ItemStack var1) {
        int[] var2 = SwordCavityExtensions.validateLayout(var0);
        int[] var3 = new int[27];
        Arrays.setAll(var3, var0x -> SwordNativeMaterialRules.visualRow(var0x) * 9 + SwordNativeMaterialRules.visualColumn(var0x));
        if (!Arrays.equals(var2, var3)) {
            return var2;
        } else {
            ArrayList var4 = new ArrayList<>(Collections.nCopies(27, ""));
            Object var5 = new ArrayList<>(Collections.nCopies(27, 0));
            String var6;
            if (FourWeaponFoundationAdapter.applies(var1)) {
                Profile var7 = FourWeaponFoundationAdapter.profile(var1);
                if (var7 == null) {
                    return var2;
                }

                var6 = var7.family();
                var7.nativeCells().forEach((var1x, var2x) -> var4.set(var1x, var2x.itemId()));
                var5 = var7.unlocks();
            } else {
                if (!SwordCavityBuild.applies(var1)) {
                    return var2;
                }

                var6 = shapeFamily(var1);
                List var9 = SwordNativeMaterials.parts(var1);
                if (var9.size() != 27) {
                    return var2;
                }

                for (int var8 = 0; var8 < var9.size(); var8++) {
                    var4.set(var8, ((Piece)var9.get(var8)).itemId());
                }
            }

            return SwordCavityExtensions.validateLayout(WeaponCavityShapeRules.layout(var6, var4, (List<Integer>)var5));
        }
    }

    public static boolean applies(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) || SwordCavityBuild.applies(var0);
    }

    public static int revision(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.revision(var0) : SwordCavityBuild.revision(var0);
    }

    public static List<ItemStack> containerItems(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.slots(var0) : SwordCavityBuild.containerItems(var0);
    }

    public static boolean containerEditable(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.containerEditable(var0) : SwordCavityBuild.containerEditable(var0);
    }

    public static boolean canSetContainerItems(ItemStack var0, List<ItemStack> var1) {
        return FourWeaponFoundationAdapter.applies(var0)
            ? FourWeaponFoundationAdapter.canSetContainerItems(var0, var1)
            : SwordCavityBuild.canSetContainerItems(var0, var1);
    }

    public static boolean setContainerItems(ServerPlayer var0, ItemStack var1, List<ItemStack> var2, int var3) {
        return FourWeaponFoundationAdapter.applies(var1)
            ? FourWeaponFoundationAdapter.setContainerItems(var0, var1, var2, var3)
            : SwordCavityBuild.setContainerItems(var0, var1, var2, var3);
    }

    public static boolean reference(ItemStack var0, int var1) {
        if (var1 >= 0 && var1 < 27) {
            return FourWeaponFoundationAdapter.applies(var0)
                ? FourWeaponFoundationAdapter.reference(var0, var1)
                : SwordCavityBuild.applies(var0) && SwordCavityBuild.cells(var0).get(var1).present();
        } else {
            return false;
        }
    }

    public static boolean clearReference(ServerPlayer var0, ItemStack var1, Slot var2, int var3) {
        return FourWeaponFoundationAdapter.applies(var1)
            ? FourWeaponFoundationAdapter.replace(var0, var1, var2.ordinal(), -1, var3)
            : SwordCavityBuild.clearReference(var0, var1, var2, var3);
    }

    public static boolean cellUnlocked(ItemStack var0, int var1) {
        return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.cellUnlocked(var0, var1) : SwordCavityBuild.cellUnlocked(var0, var1);
    }
}
