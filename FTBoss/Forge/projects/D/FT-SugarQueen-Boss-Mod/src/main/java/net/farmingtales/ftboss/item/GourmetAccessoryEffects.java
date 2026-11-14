package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.GourmetAccessoryRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GourmetAccessoryEffects {
    private GourmetAccessoryEffects() {}

    public static void scalePowers(double[] powers, ItemStack weapon) {
        GourmetAccessoryRules.scalePowers(powers, weapon != null && GourmetBladeCavity.identityOnly(weapon));
    }

    public static Build scale(Build build, ItemStack weapon) {
        return weapon != null && GourmetBladeCavity.identityOnly(weapon) ? GourmetAccessoryRules.scale(build) : build;
    }

    public static Bonus scale(Bonus bonus, ItemStack weapon) {
        return weapon != null && GourmetBladeCavity.identityOnly(weapon) ? GourmetAccessoryRules.scale(bonus) : bonus;
    }

    public static void tooltip(List<Component> lines) {
        lines.add(Component.translatable("tooltip.ftboss.gourmet.accessory_strength").withStyle(ChatFormatting.GOLD));
    }
}
