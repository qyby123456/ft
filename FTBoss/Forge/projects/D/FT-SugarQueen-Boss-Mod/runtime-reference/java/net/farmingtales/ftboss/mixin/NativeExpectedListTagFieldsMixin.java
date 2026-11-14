/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ListTag.class}, remap=false)
public abstract class NativeExpectedListTagFieldsMixin
implements NativeExpectedFieldViews.Sequence {
    @Shadow(remap=false, aliases={"list"})
    @Final
    private List<Tag> f_128716_;
    @Shadow(remap=false, aliases={"type"})
    private byte f_128717_;

    @Override
    public List<Tag> ftboss$rawListFields() {
        return this.f_128716_;
    }

    @Override
    public byte ftboss$rawListKind() {
        return this.f_128717_;
    }
}

