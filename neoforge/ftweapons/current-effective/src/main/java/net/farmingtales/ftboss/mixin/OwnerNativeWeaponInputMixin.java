package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.StackWalker.Option;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemStack.class, remap = false)
public abstract class OwnerNativeWeaponInputMixin implements OwnerNativeWeaponInputFields {
    @Unique
    private OwnerNativeSemanticAuthority.WeaponInput ftboss$ownerNativeWeaponInput;

    @Unique
    @Override
    public final OwnerNativeSemanticAuthority.WeaponInput ftboss$ownerNativeWeaponInput() {
        return this.ftboss$ownerNativeWeaponInput;
    }

    @Unique
    @Override
    public final void ftboss$ownerInstallNativeWeaponInput(OwnerNativeSemanticAuthority.WeaponInput var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.skip(1L).findFirst().map(var0x -> var0x.getDeclaringClass() == OwnerNativeSemanticAuthority.class).orElse(false));
        if (var2 && this.ftboss$ownerNativeWeaponInput == null) {
            this.ftboss$ownerNativeWeaponInput = var1;
        }
    }

    @WrapOperation(
        method = "parse(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", remap = false),
        require = 1,
        remap = false
    )
    private static DataResult<ItemStack> ftboss$ownerNativeLoadedInput(Codec<ItemStack> codec, DynamicOps<?> operations, Object value, Operation<DataResult<ItemStack>> original, HolderLookup.Provider registries, Tag input) {
        OwnerNativeSemanticAuthority.WeaponInput quoted = null;
        try {
            if (input instanceof CompoundTag compound) quoted = OwnerNativeSemanticAuthority.weaponInputQuoted(registries, compound);
        } catch (Throwable failure) {
        }
        DataResult<ItemStack> result = original.call(codec, operations, value);
        Optional<ItemStack> parsed = result.result();
        try {
            if (parsed.isPresent()) OwnerNativeSemanticAuthority.weaponInputLoaded(quoted, parsed.orElseThrow());
        } catch (Throwable failure) {
        }
        return result;
    }
    @Unique
    @Override
    public final boolean ftboss$ownerAdvanceNativeWeaponInput(OwnerNativeSemanticAuthority.WeaponInput before, OwnerNativeSemanticAuthority.WeaponInput after) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeTransitionReturned")
            || before == null || after == null || before == after || this.ftboss$ownerNativeWeaponInput != before) return false;
        this.ftboss$ownerNativeWeaponInput = after;
        return true;
    }
}
