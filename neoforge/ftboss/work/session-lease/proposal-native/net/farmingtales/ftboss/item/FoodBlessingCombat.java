package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponBondHits.Hit;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan.Builder;
import net.farmingtales.ftboss.util.MeleeEnhancementRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlotGroup;

public final class FoodBlessingCombat {
    private FoodBlessingCombat() {
    }

    public static boolean eligible(ItemStack stack) {
        return false;
    }

    public static int level(ItemStack stack, String identity) {
        return 0;
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        EquipmentAttributeSlots.attributes(event);
    }

    public static void blessingAttributes(ItemAttributeModifierEvent event, ItemStack stack) {
    }

    public static void blessingAttributes(ItemAttributeModifierEvent event, ItemStack stack, EquipmentSlot slot) {
    }

    public static int crossbowChargeTicks(int ticks, ItemStack stack) {
        return ticks;
    }

    public static void quoteDamage(Builder builder, BeforeMitigation event, ItemStack stack) {
    }

    public static void defense(BeforeMitigation event) {
    }

    public static void killFeast(LivingDeathEvent event) {
    }
}
