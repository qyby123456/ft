/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemStack.class})
public abstract class OwnerNativeWeaponInputMixin
implements OwnerNativeWeaponInputFields {
    @Unique
    private OwnerNativeSemanticAuthority.WeaponInput ftboss$ownerNativeWeaponInput;

    @Override
    @Unique
    public final OwnerNativeSemanticAuthority.WeaponInput ftboss$ownerNativeWeaponInput() {
        return this.ftboss$ownerNativeWeaponInput;
    }

    @Override
    @Unique
    public final void ftboss$ownerInstallNativeWeaponInput(OwnerNativeSemanticAuthority.WeaponInput weaponInput) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerNativeSemanticAuthority.class).orElse(false));
        if (bl && this.ftboss$ownerNativeWeaponInput == null) {
            this.ftboss$ownerNativeWeaponInput = weaponInput;
        }
    }

    @WrapOperation(method={"of"}, at={@At(value="NEW", target="(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;")}, require=1)
    private static ItemStack ftboss$ownerNativeLoadedInput(CompoundTag compoundTag, Operation<ItemStack> operation) {
        OwnerNativeSemanticAuthority.WeaponInput weaponInput = null;
        try {
            weaponInput = OwnerNativeSemanticAuthority.weaponInputQuoted(compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        ItemStack itemStack = (ItemStack)operation.call(new Object[]{compoundTag});
        try {
            OwnerNativeSemanticAuthority.weaponInputLoaded(weaponInput, itemStack);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return itemStack;
    }
}

