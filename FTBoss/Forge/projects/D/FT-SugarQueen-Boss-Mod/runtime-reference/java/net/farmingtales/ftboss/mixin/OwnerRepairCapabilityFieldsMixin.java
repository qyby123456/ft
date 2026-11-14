/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraftforge.common.capabilities.CapabilityDispatcher
 *  net.minecraftforge.common.capabilities.CapabilityProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerRepairCapabilityFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={CapabilityProvider.class}, remap=false)
public abstract class OwnerRepairCapabilityFieldsMixin
implements OwnerRepairCapabilityFields {
    @Shadow(remap=false)
    private CapabilityDispatcher capabilities;
    @Shadow(remap=false)
    private boolean valid;
    @Shadow(remap=false)
    private boolean initialized;
    @Shadow(remap=false)
    private CompoundTag lazyData;

    @Override
    @Unique
    public final boolean ftboss$ownerRepairInitializedNoProviders() {
        return this.valid && this.initialized && this.capabilities == null && this.lazyData == null;
    }
}

