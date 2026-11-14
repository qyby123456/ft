/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.LongArrayTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.LongArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={LongArrayTag.class}, remap=false)
public abstract class NativeExpectedLongArrayTagFieldsMixin
implements NativeExpectedFieldViews.Longs {
    @Shadow(remap=false, aliases={"data"})
    private long[] f_128801_;

    @Override
    public long[] ftboss$rawLongs() {
        return this.f_128801_;
    }
}

