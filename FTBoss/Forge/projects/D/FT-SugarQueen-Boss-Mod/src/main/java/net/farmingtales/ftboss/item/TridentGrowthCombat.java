package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.integ.FoodWeaponTridentCarrierReader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 三叉戟投掷复用本体的近战附加伤害；2026-10-25 补齐成长、材料和祝福收益，保持原有基础伤害。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class TridentGrowthCombat {
    private TridentGrowthCombat() {}

    public static double damageBonus(ItemStack weapon) {
        if (!(weapon.getItem() instanceof TridentItem) || FoodWeaponLifecycle.isBroken(weapon)) return 0.0D;
        double bonus = 0.0D;
        for (AttributeModifier modifier : weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() != AttributeModifier.Operation.ADDITION) continue;
            switch (modifier.getName()) {
                case "ftboss.material.damage", "ftboss.food_evolution.damage",
                        "ftboss.material_rarity.attack_damage", "ftboss.food_blessing.cleaver_might" -> bonus += modifier.getAmount();
                default -> {}
            }
        }
        return Double.isFinite(bonus) ? bonus : 0.0D;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void damage(LivingHurtEvent event) {
        if (event.isCanceled() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0F
                || !(event.getSource().getDirectEntity() instanceof ThrownTrident trident)
                || !(trident.getOwner() instanceof ServerPlayer player) || event.getSource().getEntity() != player
                || !event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getEntity() == player || event.getEntity().isAlliedTo(player)) return;
        double damage = event.getAmount() + damageBonus(FoodWeaponTridentCarrierReader.readOriginalItem(trident));
        if (Double.isFinite(damage)) event.setAmount((float) Math.max(0.0D, damage));
    }
}
