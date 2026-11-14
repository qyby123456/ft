/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRepairStackFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ItemStack.class})
public abstract class OwnerRepairStackFieldsMixin
implements OwnerRepairStackFields {
    @Shadow
    @Final
    private Item f_41589_;
    @Shadow
    private CompoundTag capNBT;
    @Unique
    private boolean ftboss$ownerRepairRetired;

    @Override
    @Unique
    public final Item ftboss$ownerRepairRawItem() {
        return this.f_41589_;
    }

    @Override
    @Unique
    public final boolean ftboss$ownerRepairRawCapNbtAbsent() {
        return this.capNBT == null;
    }

    @Override
    @Unique
    public final boolean ftboss$ownerRepairWasRetired() {
        return this.ftboss$ownerRepairRetired;
    }

    @Override
    @Unique
    public final void ftboss$ownerRepairRetire() {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFullRepairGuard.class).orElse(false));
        if (bl) {
            this.ftboss$ownerRepairRetired = true;
        }
    }
}

