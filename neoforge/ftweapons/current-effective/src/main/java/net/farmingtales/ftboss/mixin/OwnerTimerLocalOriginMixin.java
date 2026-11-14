package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponEvolution"},
    remap = false
)
public abstract class OwnerTimerLocalOriginMixin {
    @WrapOperation(
        method = {"quoteFoodBlessingHits"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getPersistentData()Lnet/minecraft/nbt/CompoundTag;",
            remap = false
        )},
        require = 8,
        expect = 8,
        allow = 8
    )
    private static CompoundTag ftboss$ownerTargetReceiver(
        LivingEntity var0,
        Operation<CompoundTag> var1,
        FoodWeaponHitPlan.Builder var2,
        ServerPlayer var3,
        LivingEntity var4,
        CompoundTag var5,
        long var6,
        ItemStack var8,
        FoodWeaponHitPlan.RandomQuotes var9
    ) {
        CompoundTag var10 = (CompoundTag)var1.call(new Object[]{var0});

        try {
            OwnerNativeSemanticAuthority.targetReceiverReturned(var2, var4, var0, var10);
        } catch (Throwable var12) {
        }

        return var10;
    }

    @WrapOperation(
        method = {"quoteFoodBlessingHits"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;write(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V"
        )},
        require = 7,
        expect = 7,
        allow = 7
    )
    private static void ftboss$ownerTargetWrite(
        FoodWeaponHitPlan.Builder var0,
        FoodWeaponSourceLedger.AttemptDomain var1,
        CompoundTag var2,
        String var3,
        Tag var4,
        Operation<Void> var5,
        FoodWeaponHitPlan.Builder var6,
        ServerPlayer var7,
        LivingEntity var8,
        CompoundTag var9,
        long var10,
        ItemStack var12,
        FoodWeaponHitPlan.RandomQuotes var13
    ) throws Throwable {
        OwnerNativeSemanticAuthority.TargetWriteCall var14 = null;

        try {
            var14 = OwnerNativeSemanticAuthority.targetWriteBegins(var0, var6, var7, var8, var12, var9, var2, var3, var1, var4);
        } catch (Throwable var24) {
        }

        try {
            var5.call(new Object[]{var0, var1, var2, var3, var4});
        } finally {
            try {
                OwnerNativeSemanticAuthority.targetWriteEnds(var14);
            } catch (Throwable var22) {
            }
        }
    }

    @WrapOperation(
        method = {"quoteFinalWeaponHit"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;",
            ordinal = 1,
            remap = true
        )},
        require = 1
    )
    private static CompoundTag ftboss$ownerLocalDataCopy(CompoundTag var0, Operation<CompoundTag> var1) {
        CompoundTag var2 = (CompoundTag)var1.call(new Object[]{var0});

        try {
            OwnerTimerSemanticAuthority.localDataCopied(var0, var2);
        } catch (Throwable var4) {
        }

        return var2;
    }

    @WrapOperation(
        method = {"quoteFinalWeaponHit"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;",
            ordinal = 2,
            remap = true
        )},
        require = 1
    )
    private static CompoundTag ftboss$ownerLocalBaselineCopy(CompoundTag var0, Operation<CompoundTag> var1) {
        CompoundTag var2 = (CompoundTag)var1.call(new Object[]{var0});

        try {
            OwnerTimerSemanticAuthority.localBaselineCopied(var0, var2);
        } catch (Throwable var4) {
        }

        return var2;
    }
}
