package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.util.GourmetAccessoryRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GourmetAccessoryEffects {
    private GourmetAccessoryEffects() {
    }

    public static void scalePowers(double[] var0, ItemStack var1) {
        GourmetAccessoryRules.scalePowers(var0, var1 != null && GourmetBladeCavity.identityOnly(var1));
    }

    public static Build scale(Build var0, ItemStack var1) {
        return var1 != null && GourmetBladeCavity.identityOnly(var1) ? GourmetAccessoryRules.scale(var0) : var0;
    }

    public static Bonus scale(Bonus var0, ItemStack var1) {
        return var1 != null && GourmetBladeCavity.identityOnly(var1) ? GourmetAccessoryRules.scale(var0) : var0;
    }

    public static void tooltip(List<Component> var0) {
        var0.add(Component.translatable("tooltip.ftboss.gourmet.accessory_strength").withStyle(ChatFormatting.GOLD));
    }
}
