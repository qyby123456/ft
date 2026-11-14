/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraftforge.common.capabilities.CapabilityDispatcher
 *  net.minecraftforge.common.capabilities.CapabilityProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Supplier;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={CapabilityProvider.class}, remap=false)
public abstract class NativeExpectedProviderFieldsMixin
implements NativeExpectedFieldViews.Provider {
    @Shadow(remap=false)
    private CapabilityDispatcher capabilities;
    @Shadow(remap=false)
    private boolean valid;
    @Shadow(remap=false)
    private boolean isLazy;
    @Shadow(remap=false)
    private boolean initialized;
    @Shadow(remap=false)
    private Supplier<?> lazyParentSupplier;
    @Shadow(remap=false)
    private CompoundTag lazyData;

    @Override
    public Object ftboss$rawDispatcher() {
        return this.ftboss$rawActualDispatcher();
    }

    public CapabilityDispatcher ftboss$rawActualDispatcher() {
        return this.capabilities;
    }

    @Override
    public boolean ftboss$rawCapsValid() {
        return this.valid;
    }

    @Override
    public boolean ftboss$rawCapsLazy() {
        return this.isLazy;
    }

    @Override
    public boolean ftboss$rawCapsInitialized() {
        return this.initialized;
    }

    @Override
    public Object ftboss$rawCapsParentSupplier() {
        return this.ftboss$rawActualCapsParentSupplier();
    }

    public Supplier<?> ftboss$rawActualCapsParentSupplier() {
        return this.lazyParentSupplier;
    }

    @Override
    public CompoundTag ftboss$rawCapsLazyData() {
        return this.lazyData;
    }
}

