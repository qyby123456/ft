/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.LongTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.LongTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LongTag.class}, remap=false)
public interface NativeExpectedLongTagFieldsMixin
extends NativeExpectedFieldViews.LongValue {
    @Override
    @Accessor(value="f_128874_", remap=false)
    public long ftboss$rawLong();
}

