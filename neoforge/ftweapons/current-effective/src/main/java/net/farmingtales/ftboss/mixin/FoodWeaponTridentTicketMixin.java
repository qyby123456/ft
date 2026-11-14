package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {ThrownTrident.class},
    remap = false
)
public abstract class FoodWeaponTridentTicketMixin {
    @Inject(
        method = {"<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$bindTicket(Level var1, LivingEntity var2, ItemStack var3, CallbackInfo var4) {
        FoodWeaponSourceLedger.attachTrident((ThrownTrident)(Object)this, var2, var3);
    }
}
