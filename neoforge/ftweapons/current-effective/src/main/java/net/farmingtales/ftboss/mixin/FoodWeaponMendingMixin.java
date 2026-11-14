package net.farmingtales.ftboss.mixin;

import java.util.function.Predicate;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(
    value = {ExperienceOrb.class},
    remap = false
)
public abstract class FoodWeaponMendingMixin {
    @ModifyArg(
        method = {"repairPlayerItems(Lnet/minecraft/server/level/ServerPlayer;I)I"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRandomItemWith(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Optional;"
        ),
        index = 2,
        require = 0,
        remap = false
    )
    @Group(
        name = "ftbossMendingFilter",
        min = 1,
        max = 1
    )
    private Predicate<ItemStack> ftboss$srgMending(Predicate<ItemStack> var1) {
        return var1.and(var0 -> !NativeFoodWeaponWear.eligible(var0));
    }
}
