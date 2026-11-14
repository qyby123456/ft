/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.ByteArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ByteArrayTag.class}, remap=false)
public abstract class NativeExpectedByteArrayTagFieldsMixin
implements NativeExpectedFieldViews.Bytes {
    @Shadow(remap=false, aliases={"data"})
    private byte[] f_128186_;

    @Override
    public byte[] ftboss$rawBytes() {
        return this.f_128186_;
    }
}

