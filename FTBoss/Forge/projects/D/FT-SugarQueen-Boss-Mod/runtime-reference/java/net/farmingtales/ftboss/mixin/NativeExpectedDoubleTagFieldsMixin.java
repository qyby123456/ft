/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.DoubleTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.DoubleTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={DoubleTag.class}, remap=false)
public interface NativeExpectedDoubleTagFieldsMixin
extends NativeExpectedFieldViews.DoubleValue {
    @Override
    @Accessor(value="f_128495_", remap=false)
    public double ftboss$rawDouble();
}

