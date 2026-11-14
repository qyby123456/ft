package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.item.MaterialBowItems.Kind;
import net.farmingtales.ftboss.item.BowGrowthCombat;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBonuses;
import net.farmingtales.ftboss.util.MaterialBowRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MaterialBowItem extends BowItem {
    private final Kind kind;

    public MaterialBowItem(Kind kind) {
        super(properties(kind));
        this.kind = kind;
    }

    private static Properties properties(Kind kind) {
        Properties properties = new Properties().durability(kind.tier.getUses());
        return kind.tier == Tiers.NETHERITE ? properties.fireResistant() : properties;
    }

    public Kind kind() {
        return kind;
    }

    public MaterialBowRules.Profile profile() {
        return MaterialBowRules.profile(kind.itemId);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity caster, int remaining) {
        int elapsed = stack.getUseDuration() - remaining;
        super.releaseUsing(stack, level, caster, stack.getUseDuration() - profile().vanillaChargeTicks(elapsed));
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        AbstractArrow result = super.customArrow(arrow);
        result.setBaseDamage(result.getBaseDamage() + profile().arrowDamageAdjustment());
        return result;
    }

    @Override
    public int getEnchantmentValue() {
        return kind.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack weapon, ItemStack ingredient) {
        return kind.tier.getRepairIngredient().test(ingredient) || super.isValidRepairItem(weapon, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.description." + kind.itemId)
                .withStyle(ChatFormatting.GRAY));
        var profile = profile();
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.base_damage", profile.damage())
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.base_draw",
                String.format(Locale.ROOT, "%.2f", profile.drawTicks() / 20.0D), profile.drawTicks()).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.base_velocity",
                String.format(Locale.ROOT, "%.1f", MaterialBowRules.ARROW_SPEED)).withStyle(ChatFormatting.GREEN));
        double accessoryDamage = WeaponMaterialBonuses.bonuses(stack).attackDamage();
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.accessory_damage",
                String.format(Locale.ROOT, "%.1f", accessoryDamage)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.current_damage",
                String.format(Locale.ROOT, "%.2f", (profile.damage() + accessoryDamage + BowGrowthCombat.bonuses(stack).damage())
                        * FoodWeaponFamilyBuild.damageMultiplier(stack)))
                .withStyle(ChatFormatting.AQUA));
        int currentDraw = BowGrowthCombat.drawTicks(stack, profile.drawTicks());
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.current_draw",
                String.format(Locale.ROOT, "%.2f", currentDraw / 20.0D), currentDraw).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.durability",
                Math.max(0, stack.getMaxDamage() - stack.getDamageValue()), stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.enchantability", getEnchantmentValue())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.use").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.material_bow.components").withStyle(ChatFormatting.DARK_AQUA));
    }
}
