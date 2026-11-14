package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemStack.class, remap = false)
public abstract class NativeProjectileAppliedWearMixin {
    @WrapOperation(
        method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"),
        require = 1
    )
    private void ftboss$v6$applied(ItemStack stack, int damage, Operation<Void> original, int requested, ServerLevel level, LivingEntity owner, Consumer<Item> broken) {
        FoodWeaponSourceLedger.nativeAppliedDamage(stack, damage, requested, owner, original);
    }
}
