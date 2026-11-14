package net.farmingtales.ftboss.item;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
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
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class BowGrowthCombat {
    private BowGrowthCombat() {
    }

    public static boolean applies(ItemStack var0) {
        return var0.getItem() instanceof BowItem;
    }

    private static ItemAttributeModifierEvent growth(ItemStack var0, EquipmentSlot var1) {
        ItemAttributeModifierEvent var2 = new ItemAttributeModifierEvent(var0, ItemAttributeModifiers.EMPTY);
        if (EquipmentAttributeSlots.growthSlot(var1, var0)) {
            FoodWeaponEvolution.growthAttributes(var2);
        }
        return var2;
    }

    public static void attributes(ItemAttributeModifierEvent var0) {
        if (RangedHybridCombat.redirects(var0.getItemStack())) {
            RangedHybridCombat.attributes(var0);
        } else if (!applies(var0.getItemStack())) {
            FoodWeaponEvolution.growthAttributes(var0);
        } else {
            NativeEquipmentAttributes.copyUtilityModifiers(growth(var0.getItemStack(), EquipmentSlot.MAINHAND), var0);
        }
    }

    private static double sum(ItemAttributeModifierEvent var0, net.minecraft.core.Holder<Attribute> var1) {
        return NativeEquipmentAttributes.sum(var0, var1, EquipmentSlot.MAINHAND);
    }

    public static BowGrowthCombat.Bonus bonuses(ItemStack var0) {
        if (!applies(var0)) {
            return new BowGrowthCombat.Bonus(0.0, 0.0);
        } else {
            ItemAttributeModifierEvent var1 = growth(var0, EquipmentSlot.MAINHAND);
            return new BowGrowthCombat.Bonus(sum(var1, Attributes.ATTACK_DAMAGE), sum(var1, Attributes.ATTACK_SPEED));
        }
    }

    public static double chargeMultiplier(ItemStack var0) {
        double var1 = FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(var0));
        return Math.min(1.5, var1 * (1.0 + bonuses(var0).speed()));
    }

    public static int drawTicks(ItemStack var0, int var1) {
        return FoodWeaponFamilyRules.chargeTicks(var1, chargeMultiplier(var0));
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (applies(var1)) {
            BowGrowthCombat.Bonus var2 = bonuses(var1);
            double var3 = chargeMultiplier(var1);
            List var5 = List.of(
                Component.translatable("tooltip.ftboss.bow_growth.damage", new Object[]{String.format(Locale.ROOT, "%.2f", var2.damage())})
                    .withStyle(ChatFormatting.AQUA),
                Component.translatable(
                        "tooltip.ftboss.bow_growth.charge",
                        new Object[]{String.format(Locale.ROOT, "%.2f", (var3 - 1.0) * 100.0), String.format(Locale.ROOT, "%.2f", var3)}
                    )
                    .withStyle(ChatFormatting.AQUA)
            );
            List var6 = var0.getToolTip();
            int var7 = Math.min(1, var6.size());

            for (int var8 = 0; var8 < var6.size(); var8++) {
                if (((Component)var6.get(var8)).getContents() instanceof TranslatableContents var9
                    && var9.getKey().equals("tooltip.ftboss.material_bow.current_draw")) {
                    var7 = var8 + 1;
                    break;
                }
            }

            var6.addAll(var7, var5);
        }
    }

    public static record Bonus(double damage, double speed) {
    }
}
