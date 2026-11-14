package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.item.RangedHybridCombat;
import net.farmingtales.ftboss.util.MaterialCrossbowRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/** 六种材料弩；2026-10-24 为基础箭伤、装填时间及面板提供同一战斗档案。 */
public final class MaterialCrossbowItem extends CrossbowItem {
    private final Tier tier;

    public MaterialCrossbowItem(Tier tier, Properties properties) {
        super(properties.durability(tier.getUses()));
        this.tier = tier;
    }

    public MaterialCrossbowRules.Profile profile() {
        return MaterialCrossbowRules.profile(ForgeRegistries.ITEMS.getKey(this).getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var profile = profile();
        tooltip.add(Component.translatable("tooltip.ftboss.material_crossbow.damage", profile.damage()).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.material_crossbow.reload",
                String.format(Locale.ROOT, "%.2f", profile.reloadTicks() / 20.0D), profile.reloadTicks()).withStyle(ChatFormatting.GREEN));
        int reload = RangedHybridCombat.crossbowChargeTicks(25, stack);
        tooltip.add(Component.translatable("tooltip.ftboss.material_crossbow.current_reload",
                String.format(Locale.ROOT, "%.2f", reload / 20.0D), reload).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack weapon, ItemStack ingredient) {
        return this.tier.getRepairIngredient().test(ingredient) || super.isValidRepairItem(weapon, ingredient);
    }
}
