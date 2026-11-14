package net.farmingtales.ftboss.mixin;

import java.util.function.Predicate;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** 修补候选过滤；进化武器不分走修补经验，2026-10-13 同时校验开发映射与正式SRG入口。 */
@Mixin(value = ExperienceOrb.class, remap = false)
public abstract class FoodWeaponMendingMixin {
    @Group(name = "ftbossMendingFilter", min = 1, max = 1)
    @ModifyArg(method = "repairPlayerItems(Lnet/minecraft/world/entity/player/Player;I)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRandomItemWith(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;"),
            index = 2, require = 0, remap = false)
    private Predicate<ItemStack> ftboss$mappedMending(Predicate<ItemStack> original) {
        return original.and(stack -> !FoodWeaponLifecycle.requiresStatueRepair(stack));
    }

    @Group(name = "ftbossMendingFilter", min = 1, max = 1)
    @ModifyArg(method = "m_147092_(Lnet/minecraft/world/entity/player/Player;I)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;m_44839_(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;"),
            index = 2, require = 0, remap = false)
    private Predicate<ItemStack> ftboss$srgMending(Predicate<ItemStack> original) {
        return original.and(stack -> !FoodWeaponLifecycle.requiresStatueRepair(stack));
    }
}
