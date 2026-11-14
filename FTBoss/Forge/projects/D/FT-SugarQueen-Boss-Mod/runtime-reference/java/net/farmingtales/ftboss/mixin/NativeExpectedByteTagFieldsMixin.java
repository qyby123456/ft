/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ByteTag.class}, remap=false)
public interface NativeExpectedByteTagFieldsMixin
extends NativeExpectedFieldViews.ByteValue {
    @Override
    @Accessor(value="f_128258_", remap=false)
    public byte ftboss$rawByte();
}

