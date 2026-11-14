/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.OwnerTridentCarrierAccess;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ThrownTrident.class})
public abstract class OwnerTridentOriginalCopyMixin
implements OwnerTridentCarrierAccess {
    @Unique
    private FoodWeaponSourceLedger.TridentCarrierLineage ftboss$ownerTridentLineage;

    @Override
    @Unique
    public final FoodWeaponSourceLedger.TridentCarrierLineage ftboss$ownerTridentLineage() {
        return this.ftboss$ownerTridentLineage;
    }

    @Override
    @Unique
    public final void ftboss$ownerInstallTridentLineage(FoodWeaponSourceLedger.TridentCarrierLineage tridentCarrierLineage) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponSourceLedger.class).orElse(false));
        if (bl && this.ftboss$ownerTridentLineage == null) {
            this.ftboss$ownerTridentLineage = tridentCarrierLineage;
        }
    }

    @WrapOperation(method={"<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;")}, require=1)
    private ItemStack ftboss$ownerTridentOriginalCopy(ItemStack itemStack, Operation<ItemStack> operation) {
        ItemStack itemStack2 = (ItemStack)operation.call(new Object[]{itemStack});
        try {
            FoodWeaponSourceLedger.ownerTridentOriginalCopy((ThrownTrident)this, itemStack, itemStack2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return itemStack2;
    }
}

