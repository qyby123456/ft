/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerTimerQuoteFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf"}, remap=false)
public abstract class OwnerTimerQuoteMixin
implements OwnerTimerQuoteFields {
    @Unique
    private OwnerFieldExpectedAuthority.TimerQuote ftboss$ownerTimerQuote;

    @Override
    @Unique
    public final OwnerFieldExpectedAuthority.TimerQuote ftboss$ownerTimerQuote() {
        return this.ftboss$ownerTimerQuote;
    }

    @WrapOperation(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;", remap=true)}, require=1)
    private CompoundTag ftboss$ownerTimerQuoted(CompoundTag compoundTag, Operation<CompoundTag> operation, ItemStack itemStack, @Coerce Object object, ServerPlayer serverPlayer, DamageSource damageSource, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        try {
            if (compoundTag == compoundTag2) {
                this.ftboss$ownerTimerQuote = OwnerFieldExpectedAuthority.timerQuoted(this, itemStack, object, serverPlayer, damageSource, compoundTag2, compoundTag3);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return (CompoundTag)operation.call(new Object[]{compoundTag});
    }
}

