/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.integ.NativeIronTokenCarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.api.magic.MagicData"}, remap=false)
public abstract class NativeIronTokenFieldsMixin
implements NativeIronTokenCarrier {
    @Unique
    private NativeIronExecution.CastFrame ftboss$v9$castFrame;

    @Override
    public NativeIronExecution.CastFrame ftboss$nativeIronFrame() {
        return this.ftboss$v9$castFrame;
    }

    @Override
    public void ftboss$bindNativeIronFrame(NativeIronExecution.CastFrame castFrame) {
        if (this.ftboss$v9$castFrame != castFrame) {
            NativeIronExecution.cancel(this.ftboss$v9$castFrame);
        }
        this.ftboss$v9$castFrame = castFrame;
    }

    @Override
    public void ftboss$clearNativeIronFrame() {
        this.ftboss$v9$castFrame = null;
    }
}

