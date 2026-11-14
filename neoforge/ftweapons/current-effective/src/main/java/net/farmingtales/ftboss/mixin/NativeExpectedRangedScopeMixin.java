package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf"},
    remap = false
)
public abstract class NativeExpectedRangedScopeMixin {
    @Shadow(
        remap = false
    )
    @Final
    private ItemStack weapon;

    @WrapOperation(
        method = {"apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponRanged;saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V",
            remap = false
        )},
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$originalRangedScope(
        ServerPlayer var1, DamageSource var2, CompoundTag var3, Operation<Void> var4, @Coerce Object var5, @Coerce Object var6
    ) {
        NativeExpectedFieldObservation.rangedSave(var1, var2, var3, this.weapon, var5, this, var4);
    }
}
