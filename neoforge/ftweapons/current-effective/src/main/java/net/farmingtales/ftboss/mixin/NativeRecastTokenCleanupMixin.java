package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.integ.NativeIronExecution.CastFrame;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts"},
    remap = false
)
public abstract class NativeRecastTokenCleanupMixin {
    @Shadow(
        remap = false
    )
    @Final
    private ServerPlayer serverPlayer;

    @Inject(
        method = {"removeRecast(Lio/redspace/ironsspellbooks/capabilities/magic/RecastInstance;Lio/redspace/ironsspellbooks/capabilities/magic/RecastResult;Z)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$recastRemoving(RecastInstance var1, RecastResult var2, boolean var3, CallbackInfo var4) {
        if (var1 != null && this.serverPlayer != null) {
            CastFrame var5 = NativeIronExecution.stored(MagicData.getPlayerMagicData(this.serverPlayer));
            if (var5 != null && var5.matchesRecast(var1.getSpellId(), var1.getCastSource(), var1.getSpellLevel())) {
                NativeIronExecution.cancel(var5);
            }
        }
    }

    @Inject(
        method = {"removeRecast(Ljava/lang/String;)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$recastRemovingId(String var1, CallbackInfo var2) {
        if (this.serverPlayer != null) {
            CastFrame var3 = NativeIronExecution.stored(MagicData.getPlayerMagicData(this.serverPlayer));
            if (var3 != null && var3.matchesSpell(var1)) {
                NativeIronExecution.cancel(var3);
            }
        }
    }

    @Unique
    private void ftboss$v9$recastUnwind() {
        if (this.serverPlayer != null) {
            NativeIronExecution.cancelStored(MagicData.getPlayerMagicData(this.serverPlayer));
        }
    }
}
