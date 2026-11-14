package net.farmingtales.ftboss.mixin;

import java.util.function.Consumer;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "com.github.L_Ender.cataclysm.items.Ancient_Spear", "com.github.L_Ender.cataclysm.items.Coral_Spear", "com.github.L_Ender.cataclysm.items.Coral_Bardiche"},
    remap = false
)
public abstract class NativeSpecialWearMixin {
    @Redirect(
        method = {"onUseTick", "launchTornado", "releaseUsing", "hurtEnemy"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"
        ),
        require = 0,
        remap = false
    )
    private void ftboss$observeActualWear(ItemStack var1, int var2, LivingEntity var3, Consumer<LivingEntity> var4) {
        NativeActionObservation.nativeWear(var1, var2, var3, var4);
    }
}
