/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.StringTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={StringTag.class}, remap=false)
public interface NativeExpectedStringTagFieldsMixin
extends NativeExpectedFieldViews.TextValue {
    @Override
    @Accessor(value="f_129290_", remap=false)
    public String ftboss$rawText();
}

