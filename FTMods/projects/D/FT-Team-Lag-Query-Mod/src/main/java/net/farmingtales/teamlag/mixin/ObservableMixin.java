package net.farmingtales.teamlag.mixin;

import net.farmingtales.teamlag.ObservableReports;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "observable.server.ProfilingData$Companion", remap = false)
public abstract class ObservableMixin {
    @Inject(method = "create(Ljava/util/Map;Ljava/util/Map;ILobservable/server/TraceMap;)Lobservable/server/ProfilingData;", at = @At("RETURN"), remap = false)
    private void ftteamlagReport(CallbackInfoReturnable<Object> callback) {
        try { ObservableReports.accept(callback.getReturnValue()); }
        catch (Throwable error) { net.farmingtales.teamlag.LagQuery.warnOnce(ObservableMixin.class, "OBS 报告接入失败，不影响原报告", error); }
    }
}
