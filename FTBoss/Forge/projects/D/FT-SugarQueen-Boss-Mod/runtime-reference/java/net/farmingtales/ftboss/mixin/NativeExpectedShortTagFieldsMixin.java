/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ShortTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ShortTag.class}, remap=false)
public interface NativeExpectedShortTagFieldsMixin
extends NativeExpectedFieldViews.ShortValue {
    @Override
    @Accessor(value="f_129245_", remap=false)
    public short ftboss$rawShort();
}

