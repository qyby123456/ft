package net.farmingtales.ftboss.item;

import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class MaterialHitPlanCompatibility {
    private MaterialHitPlanCompatibility() {}

    private static boolean synergy(ItemStack weapon, Material material) {
        String family = WeaponMaterialPowerRules.foodFamily(material);
        return !family.isEmpty() && WeaponMaterialPowerRules.synergy(FoodWeaponEvolution.socketTypeCount(weapon, FoodType.valueOf(family)));
    }

    private static List<TamableAnimal> pets(ServerPlayer player, boolean wounded) {
        double range = WeaponMaterialPowerRules.PET_RANGE;
        return player.serverLevel().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(range), animal ->
                animal.isAlive() && animal.isTame() && animal.isOwnedBy(player) && animal.distanceToSqr(player) <= range * range
                        && (!wounded || animal.getHealth() < animal.getMaxHealth()))
                .stream().sorted(Comparator.comparingDouble(animal -> animal.distanceToSqr(player))).limit(1).toList();
    }

    public static float quoteHit(LivingEntity target, ServerPlayer player, ItemStack weapon, List<Material> materials, float amount) {
        if (materials.contains(Material.COAL) && target.isOnFire()) amount *= synergy(weapon, Material.COAL) ? 1.06F : 1.04F;
        if (materials.contains(Material.QUARTZ) && target.hasEffect(MobEffects.GLOWING)) amount *= synergy(weapon, Material.QUARTZ) ? 1.10F : 1.06F;
        if (materials.contains(Material.BOSS_CHEESE) && !pets(player, false).isEmpty()) amount *= synergy(weapon, Material.BOSS_CHEESE) ? 1.12F : 1.08F;
        if (materials.contains(Material.BOSS_TIDE) && player.isInWaterOrRain()) amount *= 1.08F;
        return amount;
    }

    public static void quoteFinalHitEffects(FoodWeaponHitPlan.Builder builder, LivingHurtEvent event, ServerPlayer player, List<Material> materials) {
        builder.materialPowers(event, player, materials, materials.contains(Material.BOSS_SHEPHERD) ? pets(player, true) : List.of());
    }
}
