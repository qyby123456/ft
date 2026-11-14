package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponRanged"},
    remap = false
)
public abstract class OwnerTimerRangedGuardMixin {
    @WrapOperation(
        method = {"saveHitTimers"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;isEmpty()Z",
            remap = true
        )},
        require = 1
    )
    private static boolean ftboss$ownerMaterialGuard(CompoundTag var0, Operation<Boolean> var1, ServerPlayer var2, DamageSource var3, CompoundTag var4) {
        boolean var5 = (Boolean)var1.call(new Object[]{var0});

        try {
            OwnerTimerSemanticAuthority.rangedGuard(var2, var3, var0, var5);
        } catch (Throwable var7) {
        }

        return var5;
    }
}
