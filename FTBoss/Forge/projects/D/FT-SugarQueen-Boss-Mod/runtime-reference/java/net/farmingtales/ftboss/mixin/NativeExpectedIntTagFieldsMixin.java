/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.IntTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.IntTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={IntTag.class}, remap=false)
public interface NativeExpectedIntTagFieldsMixin
extends NativeExpectedFieldViews.IntValue {
    @Override
    @Accessor(value="f_128671_", remap=false)
    public int ftboss$rawInt();
}

