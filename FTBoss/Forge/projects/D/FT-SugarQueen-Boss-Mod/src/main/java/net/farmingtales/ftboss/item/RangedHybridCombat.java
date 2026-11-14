package net.farmingtales.ftboss.item;

import com.google.common.collect.ImmutableMultimap;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.MaterialCrossbowRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 弩、法杖和三叉戟的收益路由；2026-10-24 保留三叉戟近战属性并单独结算可信投掷命中。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class RangedHybridCombat {
    private static final UUID CAST_GROWTH = UUID.nameUUIDFromBytes("ftboss.growth.cast_speed".getBytes(StandardCharsets.UTF_8));
    private static final int VANILLA_RELOAD_TICKS = 25;

    private RangedHybridCombat() {}

    public static boolean redirects(ItemStack weapon) {
        return EquipmentAttributeSlots.heldItem(weapon)
                && (weapon.getItem() instanceof CrossbowItem || FoodWeaponFamilyBuild.kind(weapon) == Kind.STAFF);
    }

    private static ItemAttributeModifierEvent growth(ItemStack weapon, EquipmentSlot slot) {
        var event = new ItemAttributeModifierEvent(weapon, slot, ImmutableMultimap.of());
        FoodWeaponEvolution.growthAttributes(event);
        return event;
    }

    private static double sum(ItemAttributeModifierEvent event, Attribute attribute) {
        double result = event.getModifiers().get(attribute).stream().mapToDouble(AttributeModifier::getAmount).sum();
        return Double.isFinite(result) ? Math.max(0.0D, result) : 0.0D;
    }

    public static BowGrowthCombat.Bonus bonuses(ItemStack weapon) {
        var event = growth(weapon, EquipmentSlot.MAINHAND);
        return new BowGrowthCombat.Bonus(sum(event, Attributes.ATTACK_DAMAGE), sum(event, Attributes.ATTACK_SPEED));
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        var raw = growth(event.getItemStack(), event.getSlotType());
        for (var entry : raw.getModifiers().entries()) {
            if (entry.getKey() != Attributes.ATTACK_DAMAGE && entry.getKey() != Attributes.ATTACK_SPEED) {
                event.addModifier(entry.getKey(), entry.getValue());
            }
        }
        if (FoodWeaponFamilyBuild.kind(event.getItemStack()) == Kind.STAFF) {
            Attribute cast = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cast_time_reduction"));
            double speed = sum(raw, Attributes.ATTACK_SPEED);
            if (cast != null && speed > 0.0D) event.addModifier(cast,
                    new AttributeModifier(CAST_GROWTH, "ftboss.growth.cast_speed", speed, AttributeModifier.Operation.ADDITION));
        }
    }

    public static double crossbowSpeed(ItemStack weapon) {
        double material = FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(weapon));
        double blessing = FoodBlessingCombat.eligible(weapon) ? 0.04D * FoodBlessingCombat.level(weapon, "syrup_step") : 0.0D;
        return Math.min(1.5D, material * (1.0D + bonuses(weapon).speed() + blessing));
    }

    public static int crossbowChargeTicks(int original, ItemStack weapon) {
        if (!(weapon.getItem() instanceof CrossbowItem) || FoodWeaponLifecycle.isBroken(weapon)) return original;
        int base = original;
        if (weapon.getItem() instanceof MaterialCrossbowItem material) {
            base = Math.max(1, material.profile().reloadTicks() - (VANILLA_RELOAD_TICKS - original));
        }
        return FoodWeaponFamilyRules.chargeTicks(base, crossbowSpeed(weapon));
    }

    public static void quoteDamage(FoodWeaponHitPlan.Builder builder, LivingHurtEvent event, ItemStack weapon) {
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;
        if (FoodWeaponFamilyBuild.kind(weapon) != Kind.STAFF) return;
        double damage = bonuses(weapon).damage() + WeaponMaterialBonuses.bonuses(weapon).attackDamage()
                + 0.75D * FoodBlessingCombat.level(weapon, "cleaver_might");
        if (damage > 0.0D) builder.add((float) damage);
    }

    public static AbstractArrow crossbowArrow(AbstractArrow arrow, ItemStack weapon) {
        return ArrowPassiveDamage.apply(arrow, weapon);
    }

    public static void launch(Entity entity, CompoundTag snapshot) {
        if (!(entity instanceof AbstractArrow arrow) || !snapshot.getString("Family").equals("BOW")) return;
        double stored = snapshot.getDouble("LaunchSpeed");
        double speed = Double.isFinite(stored) ? Math.max(1.0D, Math.min(1.45D, stored)) : 1.0D;
        entity.setDeltaMovement(entity.getDeltaMovement().scale(speed));
        arrow.setBaseDamage(arrow.getBaseDamage() / speed);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack weapon = event.getItemStack();
        if ((!redirects(weapon) && !(weapon.getItem() instanceof TridentItem)) || !FoodBlessingCombat.eligible(weapon)) return;
        boolean trident = weapon.getItem() instanceof TridentItem;
        boolean staff = FoodWeaponFamilyBuild.kind(weapon) == Kind.STAFF;
        double damage = trident ? TridentGrowthCombat.damageBonus(weapon)
                : bonuses(weapon).damage() + WeaponMaterialBonuses.bonuses(weapon).attackDamage();
        if (!trident) damage += 0.75D * FoodBlessingCombat.level(weapon, "cleaver_might");
        String key = staff ? "spell" : trident ? "hybrid" : "ranged";
        event.getToolTip().add(Component.translatable("tooltip.ftboss.hybrid." + key, format(damage)).withStyle(ChatFormatting.AQUA));
        if (weapon.getItem() instanceof CrossbowItem) {
            event.getToolTip().add(Component.translatable("tooltip.ftboss.hybrid.reload", format((crossbowSpeed(weapon) - 1.0D) * 100.0D))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static String format(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }
}
