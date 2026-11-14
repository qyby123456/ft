package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponUseBridge"},
    remap = false
)
public abstract class NativeExpectedCaptureBoundaryMixin {
    @WrapOperation(
        method = {"invoke(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponRanged;captureBound(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/nbt/CompoundTag;",
            remap = false
        )},
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private static CompoundTag ftboss$originalCapture(ServerPlayer var0, ItemStack var1, Kind var2, InteractionHand var3, Operation<CompoundTag> var4) {
        return NativeExpectedFieldObservation.capture(var0, var1, var2, var3, var4);
    }
}
