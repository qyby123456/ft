package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class WeaponMaterialDurabilityMixin {
    @ModifyVariable(
        method = {"hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"},
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        remap = false
    )
    private int ftboss$materialWear(int var1) {
        return WeaponMaterialBuild.durabilityCost((ItemStack)(Object)this, var1);
    }

    @ModifyVariable(
        method = {"setDamageValue(I)V", "setDamageValue(I)V"},
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        remap = false
    )
    private int ftboss$protectedDamage(int var1) {
        return FoodWeaponLifecycle.beforeDamageChange((ItemStack)(Object)this, var1);
    }

    @Inject(
        method = {"setDamageValue(I)V", "setDamageValue(I)V"},
        at = {@At("RETURN")},
        remap = false
    )
    private void ftboss$wearLoss(int var1, CallbackInfo var2) {
        FoodWeaponLifecycle.afterDamageChange((ItemStack)(Object)this);
    }

    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxDamage()I"),
        cancellable = true,
        require = 1,
        remap = false
    )
    private void ftboss$keepExhausted(int amount, ServerLevel level, LivingEntity actor, Consumer<Item> onBreak, CallbackInfo callback) {
        ItemStack stack = (ItemStack)(Object)this;
        boolean exhausted = stack.getDamageValue() >= stack.getMaxDamage();
        if (!FoodWeaponLifecycle.finishHurt(stack, exhausted, actor instanceof ServerPlayer player ? player : null)) {
            callback.cancel();
        }
    }

    @Inject(
        method = {"getMaxDamage()I", "getMaxDamage()I"},
        at = {@At("RETURN")},
        cancellable = true,
        remap = false
    )
    private void ftboss$materialMaxDamage(CallbackInfoReturnable<Integer> var1) {
        ItemStack var2 = (ItemStack)(Object)this;
        int var3 = (Integer)var1.getReturnValue();
        int var4 = WeaponMaterialBuild.maxDurability(var2, var3);
        if (var4 != var3) {
            var1.setReturnValue(var4);
        }
    }
}
