/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.IntArrayTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.IntArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={IntArrayTag.class}, remap=false)
public abstract class NativeExpectedIntArrayTagFieldsMixin
implements NativeExpectedFieldViews.Ints {
    @Shadow(remap=false, aliases={"data"})
    private int[] f_128600_;

    @Override
    public int[] ftboss$rawInts() {
        return this.f_128600_;
    }
}

