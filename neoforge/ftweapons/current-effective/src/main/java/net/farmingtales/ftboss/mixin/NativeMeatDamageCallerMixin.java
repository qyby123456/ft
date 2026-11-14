package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.items.Meat_Shredder"},
    remap = false
)
public abstract class NativeMeatDamageCallerMixin {
    @Redirect(
        method = {"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        require = 0,
        remap = false
    )
    private boolean nativeActualShredderHit(LivingEntity var1, DamageSource var2, float var3, Level var4, LivingEntity var5, ItemStack var6, int var7) {
        return FoodWeaponSourceLedger.nativeMeatActualHurt(var1, var2, var3);
    }
}
