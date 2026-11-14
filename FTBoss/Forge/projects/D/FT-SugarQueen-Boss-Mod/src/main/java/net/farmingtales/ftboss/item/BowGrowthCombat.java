package net.farmingtales.ftboss.item;

import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 弓的食材、祝福与进化收益；2026-10-23 将攻击与攻速用于箭伤和蓄力，保留其他持握属性。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BowGrowthCombat {
    public record Bonus(double damage, double speed) {}

    private BowGrowthCombat() {}

    public static boolean applies(ItemStack weapon) {
        return weapon.getItem() instanceof BowItem;
    }

    private static ItemAttributeModifierEvent growth(ItemStack weapon, EquipmentSlot slot) {
        var modifiers = new ItemAttributeModifierEvent(weapon, slot, ImmutableMultimap.of());
        FoodWeaponEvolution.growthAttributes(modifiers);
        return modifiers;
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        if (RangedHybridCombat.redirects(event.getItemStack())) {
            RangedHybridCombat.attributes(event);
            return;
        }
        if (!applies(event.getItemStack())) {
            FoodWeaponEvolution.growthAttributes(event);
            return;
        }
        for (var entry : growth(event.getItemStack(), event.getSlotType()).getModifiers().entries()) {
            if (entry.getKey() != Attributes.ATTACK_DAMAGE && entry.getKey() != Attributes.ATTACK_SPEED) {
                event.addModifier(entry.getKey(), entry.getValue());
            }
        }
    }

    private static double sum(ItemAttributeModifierEvent event, Attribute attribute) {
        double amount = event.getModifiers().get(attribute).stream().mapToDouble(AttributeModifier::getAmount).sum();
        return Double.isFinite(amount) ? Math.max(0.0D, amount) : 0.0D;
    }

    public static Bonus bonuses(ItemStack weapon) {
        if (!applies(weapon)) return new Bonus(0.0D, 0.0D);
        var modifiers = growth(weapon, EquipmentSlot.MAINHAND);
        return new Bonus(sum(modifiers, Attributes.ATTACK_DAMAGE), sum(modifiers, Attributes.ATTACK_SPEED));
    }

    public static double chargeMultiplier(ItemStack weapon) {
        double material = FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(weapon));
        return Math.min(1.5D, material * (1.0D + bonuses(weapon).speed()));
    }

    public static int drawTicks(ItemStack weapon, int baseTicks) {
        return FoodWeaponFamilyRules.chargeTicks(baseTicks, chargeMultiplier(weapon));
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack weapon = event.getItemStack();
        if (!applies(weapon)) return;
        Bonus bonus = bonuses(weapon);
        double charge = chargeMultiplier(weapon);
        List<Component> details = List.of(
                Component.translatable("tooltip.ftboss.bow_growth.damage", String.format(Locale.ROOT, "%.2f", bonus.damage()))
                        .withStyle(ChatFormatting.AQUA),
                Component.translatable("tooltip.ftboss.bow_growth.charge",
                        String.format(Locale.ROOT, "%.2f", (charge - 1.0D) * 100.0D), String.format(Locale.ROOT, "%.2f", charge))
                        .withStyle(ChatFormatting.AQUA));
        List<Component> tooltip = event.getToolTip();
        int position = Math.min(1, tooltip.size());
        for (int index = 0; index < tooltip.size(); index++) {
            if (tooltip.get(index).getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals("tooltip.ftboss.material_bow.current_draw")) {
                position = index + 1;
                break;
            }
        }
        tooltip.addAll(position, details);
    }
}
