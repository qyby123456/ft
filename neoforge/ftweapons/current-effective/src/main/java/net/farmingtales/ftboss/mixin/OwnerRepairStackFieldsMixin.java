package net.farmingtales.ftboss.mixin;

import java.lang.StackWalker.Option;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRepairStackFields;
import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ItemStack.class})
public abstract class OwnerRepairStackFieldsMixin implements OwnerRepairStackFields {
    @Shadow
    @Final
    private Item item;
    @Unique
    private boolean ftboss$ownerRepairRetired;

    @Unique
    @Override
    public final Item ftboss$ownerRepairRawItem() {
        return this.item;
    }

    @Unique
    @Override
    public final boolean ftboss$ownerRepairRawCapNbtAbsent() {
        return ((OwnerRepairCapabilityFields)(Object)this).ftboss$ownerRepairInitializedNoProviders();
    }

    @Unique
    @Override
    public final boolean ftboss$ownerRepairWasRetired() {
        return this.ftboss$ownerRepairRetired;
    }

    @Unique
    @Override
    public final void ftboss$ownerRepairRetire() {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(var0 -> var0.skip(1L).findFirst().map(var0x -> var0x.getDeclaringClass() == OwnerFullRepairGuard.class).orElse(false));
        if (var1) {
            this.ftboss$ownerRepairRetired = true;
        }
    }
}

