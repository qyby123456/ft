package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.message.MessageSwingArm$Handler"},
    remap = false
)
public abstract class NativeCatLeftClickMixin {
    @Redirect(
        method = {"lambda$handle$0"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/items/ILeftClick;onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"
        ),
        require = 0,
        remap = false
    )
    private static boolean ftboss$actualLeftClick(@Coerce Object var0, ItemStack var1, LivingEntity var2) {
        return NativeSpecialCaller.leftClick(var0, var1, var2);
    }
}
