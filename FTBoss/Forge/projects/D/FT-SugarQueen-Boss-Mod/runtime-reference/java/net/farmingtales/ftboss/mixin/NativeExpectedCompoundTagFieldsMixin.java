/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import java.util.Map;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={CompoundTag.class}, remap=false)
public abstract class NativeExpectedCompoundTagFieldsMixin
implements NativeExpectedFieldViews.Compound {
    @Shadow(remap=false, aliases={"tags"})
    @Final
    private Map<String, Tag> f_128329_;

    @Override
    public Map<String, Tag> ftboss$rawCompoundFields() {
        return this.f_128329_;
    }
}

