package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponEvolution"},
    remap = false
)
public abstract class NativeExplicitTimerPutMixin {
    @WrapOperation(
        method = {"writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
        )},
        require = 0,
        allow = 1,
        remap = false
    )
    @Group(
        name = "ftboss$r2$timerPut",
        min = 1,
        max = 1
    )
    private static Tag ftboss$r2$timerPutSrg(
        CompoundTag var0,
        String var1,
        Tag var2,
        Operation<Tag> var3,
        ItemStack var4,
        Profile var5,
        String var6,
        Tag var7,
        FoodWeaponSourceLedger.OwnerWriterStep var8,
        @Coerce Object var9,
        int var10,
        @Coerce Object var11,
        Object var12
    ) {
        return NativeExpectedFieldObservation.timerPut(var8, var4, var0, var1, var2, var3);
    }
}
