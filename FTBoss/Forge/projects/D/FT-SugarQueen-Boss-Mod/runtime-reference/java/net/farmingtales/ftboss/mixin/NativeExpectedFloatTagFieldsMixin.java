/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.FloatTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.FloatTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={FloatTag.class}, remap=false)
public interface NativeExpectedFloatTagFieldsMixin
extends NativeExpectedFieldViews.FloatValue {
    @Override
    @Accessor(value="f_128561_", remap=false)
    public float ftboss$rawFloat();
}

