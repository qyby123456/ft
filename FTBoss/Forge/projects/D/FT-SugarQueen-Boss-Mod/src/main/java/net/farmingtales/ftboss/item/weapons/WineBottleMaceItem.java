package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class WineBottleMaceItem extends SwordItem {
    public WineBottleMaceItem() {
        super(FTWeaponTier.INSTANCE, 5, -3.2F, new Properties().rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.wine_mace.pressure").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.wine_mace.damage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.wine_mace.impact").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.wine_mace.rules").withStyle(ChatFormatting.GRAY));
    }
}
