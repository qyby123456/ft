package net.farmingtales.ftboss.item;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan.Builder;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlotGroup;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class RangedHybridCombat {
    private static final UUID CAST_GROWTH = UUID.nameUUIDFromBytes("ftboss.growth.cast_speed".getBytes(StandardCharsets.UTF_8));
    private static final int VANILLA_RELOAD_TICKS = 25;

    private RangedHybridCombat() {
    }

    public static boolean redirects(ItemStack var0) {
        return !EquipmentAttributeSlots.heldItem(var0) ? false : var0.getItem() instanceof CrossbowItem || FoodWeaponFamilyBuild.kind(var0) == Kind.STAFF;
    }

    private static ItemAttributeModifierEvent growth(ItemStack var0, EquipmentSlot var1) {
        ItemAttributeModifierEvent var2 = new ItemAttributeModifierEvent(var0, ItemAttributeModifiers.EMPTY);
        if (EquipmentAttributeSlots.growthSlot(var1, var0)) {
            FoodWeaponEvolution.growthAttributes(var2);
        }
        return var2;
    }

    private static double sum(ItemAttributeModifierEvent var0, net.minecraft.core.Holder<Attribute> var1) {
        return NativeEquipmentAttributes.sum(var0, var1, EquipmentSlot.MAINHAND);
    }

    public static BowGrowthCombat.Bonus bonuses(ItemStack var0) {
        ItemAttributeModifierEvent var1 = growth(var0, EquipmentSlot.MAINHAND);
        return new BowGrowthCombat.Bonus(sum(var1, Attributes.ATTACK_DAMAGE), sum(var1, Attributes.ATTACK_SPEED));
    }

    public static void attributes(ItemAttributeModifierEvent var0) {
        ItemAttributeModifierEvent var1 = growth(var0.getItemStack(), EquipmentSlot.MAINHAND);

        NativeEquipmentAttributes.copyUtilityModifiers(var1, var0);

        if (FoodWeaponFamilyBuild.kind(var0.getItemStack()) == Kind.STAFF) {
            Holder<Attribute> var5 = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cast_time_reduction")).orElse(null);
            double var6 = sum(var1, Attributes.ATTACK_SPEED);
            if (var5 != null && var6 > 0.0) {
                var0.addModifier(var5, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "growth/" + CAST_GROWTH), var6, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }
        }
    }

    public static double crossbowSpeed(ItemStack var0) {
        double var1 = FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(var0));
        double var3 = FoodBlessingCombat.eligible(var0) ? 0.04 * (double)FoodBlessingCombat.level(var0, "syrup_step") : 0.0;
        return Math.min(1.5, var1 * (1.0 + bonuses(var0).speed() + var3));
    }

    public static int crossbowChargeTicks(int var0, ItemStack var1) {
        if (var1.getItem() instanceof CrossbowItem && !FoodWeaponLifecycle.isBroken(var1)) {
            int var2 = var0;
            if (var1.getItem() instanceof MaterialCrossbowItem var3) {
                var2 = Math.max(1, NativeEquipmentAttributes.crossbowProfile(var3).reloadTicks() - (25 - var0));
            }

            return FoodWeaponFamilyRules.chargeTicks(var2, crossbowSpeed(var1));
        } else {
            return var0;
        }
    }

    public static void quoteDamage(Builder var0, BeforeMitigation var1, ItemStack var2) {
        if (!var1.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            if (FoodWeaponFamilyBuild.kind(var2) == Kind.STAFF) {
                double var3 = bonuses(var2).damage()
                    + WeaponMaterialBonuses.bonuses(var2).attackDamage()
                    + 0.75 * (double)FoodBlessingCombat.level(var2, "cleaver_might");
                if (var3 > 0.0) {
                    var0.add((float)var3);
                }
            }
        }
    }

    public static AbstractArrow crossbowArrow(AbstractArrow var0, ItemStack var1) {
        return ArrowPassiveDamage.apply(var0, var1);
    }

    public static void launch(Entity var0, CompoundTag var1) {
        if (var0 instanceof AbstractArrow var2 && var1.getString("Family").equals("BOW")) {
            double var3 = var1.getDouble("LaunchSpeed");
            double var5 = Double.isFinite(var3) ? Math.max(1.0, Math.min(1.45, var3)) : 1.0;
            var0.setDeltaMovement(var0.getDeltaMovement().scale(var5));
            var2.setBaseDamage(var2.getBaseDamage() / var5);
            return;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if ((redirects(var1) || var1.getItem() instanceof TridentItem) && FoodBlessingCombat.eligible(var1)) {
            boolean var2 = var1.getItem() instanceof TridentItem;
            boolean var3 = FoodWeaponFamilyBuild.kind(var1) == Kind.STAFF;
            double var4 = var2 ? TridentGrowthCombat.damageBonus(var1) : bonuses(var1).damage() + WeaponMaterialBonuses.bonuses(var1).attackDamage();
            if (!var2) {
                var4 += 0.75 * (double)FoodBlessingCombat.level(var1, "cleaver_might");
            }

            String var6 = var3 ? "spell" : (var2 ? "hybrid" : "ranged");
            var0.getToolTip().add(Component.translatable("tooltip.ftboss.hybrid." + var6, new Object[]{format(var4)}).withStyle(ChatFormatting.AQUA));
            if (var1.getItem() instanceof CrossbowItem) {
                var0.getToolTip()
                    .add(
                        Component.translatable("tooltip.ftboss.hybrid.reload", new Object[]{format((crossbowSpeed(var1) - 1.0) * 100.0)})
                            .withStyle(ChatFormatting.AQUA)
                    );
            }
        }
    }

    private static String format(double var0) {
        return String.format(Locale.ROOT, "%.2f", var0);
    }
}
