package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodBlessingCombat {
    private FoodBlessingCombat() {}

    public static boolean eligible(ItemStack weapon) {
        return !weapon.isEmpty() && !FoodWeaponEvolution.repairProfileId(weapon).isEmpty()
                && !FoodWeaponLifecycle.isBroken(weapon);
    }

    public static int level(ItemStack weapon, String id) {
        CompoundTag data = weapon.getTagElement("FTBossFoodEvolution");
        if (data == null) return 0;
        ListTag blessings = data.getList("FoodBlessings", Tag.TAG_COMPOUND);
        int result = 0;
        for (int index = 0; index < blessings.size(); index++) {
            CompoundTag entry = blessings.getCompound(index);
            if (id.equals(entry.getString("Blessing"))) result = Math.max(result, Math.max(1, Math.min(3, entry.getInt("Level"))));
        }
        return result;
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        EquipmentAttributeSlots.attributes(event);
    }

    public static void blessingAttributes(ItemAttributeModifierEvent event, ItemStack weapon) {
        if (!EquipmentAttributeSlots.blessingSlot(event, weapon)) return;
        StatueBlessingCombat.attributes(event, weapon);
        if (!eligible(weapon)) return;
        boolean main = EquipmentAttributeSlots.growthSlot(event.getSlotType(), weapon);
        if (!main && event.getSlotType() != EquipmentSlot.OFFHAND) return;
        boolean staff = FoodWeaponFamilyBuild.kind(weapon) == Kind.STAFF;
        boolean crossbow = weapon.getItem() instanceof CrossbowItem;
        if (main && !crossbow && !staff) {
            add(event, "cleaver_might", Attributes.ATTACK_DAMAGE, 0.75D * level(weapon, "cleaver_might"), false);
            add(event, "syrup_step", Attributes.ATTACK_SPEED, 0.04D * level(weapon, "syrup_step"), false);
        }
        if (staff) {
            Attribute speed = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cast_time_reduction"));
            add(event, "syrup_step", speed, 0.04D * level(weapon, "syrup_step"), false);
        }
        add(event, "recycle_heart", Attributes.MAX_HEALTH, 1.5D * level(weapon, "recycle_heart"), false);
        add(event, "volt_step", Attributes.MOVEMENT_SPEED, 0.012D * level(weapon, "volt_step"), true);
        add(event, "magnetic_plate", Attributes.KNOCKBACK_RESISTANCE, 0.08D * level(weapon, "magnetic_plate"), false);
    }

    public static int crossbowChargeTicks(int original, ItemStack weapon) {
        return RangedHybridCombat.crossbowChargeTicks(original, weapon);
    }

    public static void quoteDamage(FoodWeaponHitPlan.Builder builder, LivingHurtEvent event, ItemStack weapon) {
        if (eligible(weapon)) RangedHybridCombat.quoteDamage(builder, event, weapon);
    }

    private static int heldLevel(ServerPlayer player, String id) {
        int main = eligible(player.getMainHandItem()) ? level(player.getMainHandItem(), id) : 0;
        int off = eligible(player.getOffhandItem()) ? level(player.getOffhandItem(), id) : 0;
        return Math.max(main, off);
    }

    public static void defense(LivingHurtEvent event) {
        StatueBlessingCombat.defense(event);
        if (event.isCanceled() || event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)) return;
        float multiplier = 1.0F;
        int low = heldLevel(player, "butcher_hide");
        int projectile = heldLevel(player, "barrel_plate");
        int wet = heldLevel(player, "tidal_bulwark");
        if (low > 0 && player.getHealth() <= player.getMaxHealth() * 0.5F) multiplier *= Math.max(0.55F, 1.0F - low * 0.09F);
        if (projectile > 0 && event.getSource().getDirectEntity() instanceof Projectile) multiplier *= Math.max(0.55F, 1.0F - projectile * 0.10F);
        if (wet > 0 && player.isInWaterRainOrBubble()) multiplier *= Math.max(0.55F, 1.0F - wet * 0.08F);
        if (multiplier < 1.0F) event.setAmount(event.getAmount() * multiplier);
    }

    public static void killFeast(LivingDeathEvent event) {
        StatueBlessingCombat.kill(event);
        if (event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || LemonSummonRewards.suppressed(event.getEntity())) return;
        FoodWeaponBondHits.Hit hit = FoodWeaponBondHits.resolve(event.getSource(), player);
        ItemStack weapon = FoodWeaponBondHits.equipped(player, hit);
        if (!eligible(weapon)) return;
        int feast = level(weapon, "pack_feast");
        if (feast <= 0) return;
        player.heal(0.75F + feast * 0.5F);
        player.getFoodData().eat(1 + feast, 0.2F + feast * 0.1F);
    }

    private static void add(ItemAttributeModifierEvent event, String id, Attribute attribute, double amount, boolean multiply) {
        if (attribute == null || amount <= 0.0D) return;
        String suffix = event.getSlotType() == EquipmentSlot.OFFHAND ? ".offhand" : "";
        UUID modifier = EquipmentAttributeSlots.modifierId(
                UUID.nameUUIDFromBytes(("ftboss.food_blessing." + id + suffix).getBytes(StandardCharsets.UTF_8)), event.getItemStack());
        event.addModifier(attribute, new AttributeModifier(modifier, "ftboss.food_blessing." + id, amount,
                multiply ? AttributeModifier.Operation.MULTIPLY_BASE : AttributeModifier.Operation.ADDITION));
    }
}
