package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.integ.NativeIronTokenCarrier;
import net.farmingtales.ftboss.integ.NativeIronExecution.CastFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(
    targets = {"io.redspace.ironsspellbooks.api.magic.MagicData"},
    remap = false
)
public abstract class NativeIronTokenFieldsMixin implements NativeIronTokenCarrier {
    @Unique
    private CastFrame ftboss$v9$castFrame;

    @Override
    public CastFrame ftboss$nativeIronFrame() {
        return this.ftboss$v9$castFrame;
    }

    @Override
    public void ftboss$bindNativeIronFrame(CastFrame var1) {
        if (this.ftboss$v9$castFrame != var1) {
            NativeIronExecution.cancel(this.ftboss$v9$castFrame);
        }

        this.ftboss$v9$castFrame = var1;
    }

    @Override
    public void ftboss$clearNativeIronFrame() {
        this.ftboss$v9$castFrame = null;
    }
}
