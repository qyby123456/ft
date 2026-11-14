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

@Mixin(
    targets = {"net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf"},
    remap = false
)
public abstract class OwnerTimerQuoteMixin implements OwnerTimerQuoteFields {
    @Unique
    private OwnerFieldExpectedAuthority.TimerQuote ftboss$ownerTimerQuote;

    @Unique
    @Override
    public final OwnerFieldExpectedAuthority.TimerQuote ftboss$ownerTimerQuote() {
        return this.ftboss$ownerTimerQuote;
    }

    @WrapOperation(
        method = {"<init>"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;",
            remap = true
        )},
        require = 1
    )
    private CompoundTag ftboss$ownerTimerQuoted(
        CompoundTag var1,
        Operation<CompoundTag> var2,
        ItemStack var3,
        @Coerce Object var4,
        ServerPlayer var5,
        DamageSource var6,
        CompoundTag var7,
        CompoundTag var8
    ) {
        try {
            if (var1 == var7) {
                this.ftboss$ownerTimerQuote = OwnerFieldExpectedAuthority.timerQuoted(this, var3, var4, var5, var6, var7, var8);
            }
        } catch (Throwable var10) {
        }

        return (CompoundTag)var2.call(new Object[]{var1});
    }
}
