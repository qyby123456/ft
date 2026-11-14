/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerRevocationNodeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets={"net.minecraft.nbt.CompoundTag", "net.minecraft.nbt.ListTag", "net.minecraft.nbt.ByteArrayTag", "net.minecraft.nbt.IntArrayTag", "net.minecraft.nbt.LongArrayTag", "net.minecraftforge.common.capabilities.CapabilityProvider", "net.minecraftforge.common.capabilities.CapabilityDispatcher"}, remap=false)
public abstract class OwnerRevocationNodeMixin
implements OwnerRevocationNodeAccess {
    @Unique
    private OwnerFullRepairGuard.Node ftboss$ownerRevocationNode;

    @Override
    @Unique
    public final OwnerFullRepairGuard.Node ftboss$ownerRevocationNode() {
        return this.ftboss$ownerRevocationNode;
    }

    @Override
    @Unique
    public final void ftboss$ownerInstallRevocationNode(OwnerFullRepairGuard.Node node) {
        boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == OwnerFullRepairGuard.class).orElse(false));
        if (bl && this.ftboss$ownerRevocationNode == null) {
            this.ftboss$ownerRevocationNode = node;
        }
    }
}

