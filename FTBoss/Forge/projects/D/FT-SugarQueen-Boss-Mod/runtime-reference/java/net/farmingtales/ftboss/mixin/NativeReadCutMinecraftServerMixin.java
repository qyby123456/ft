/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeObservationCarrier;
import net.farmingtales.ftboss.item.NativeObservationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets={"net.minecraft.server.MinecraftServer"}, priority=1, remap=false)
public abstract class NativeReadCutMinecraftServerMixin
implements NativeObservationCarrier {
    @Unique
    private NativeObservationState ftboss$nativeObservations;

    @Override
    public final synchronized NativeObservationState ftboss$nativeObservationState() {
        if (this.ftboss$nativeObservations == null) {
            this.ftboss$nativeObservations = new NativeObservationState();
        }
        return this.ftboss$nativeObservations;
    }
}

