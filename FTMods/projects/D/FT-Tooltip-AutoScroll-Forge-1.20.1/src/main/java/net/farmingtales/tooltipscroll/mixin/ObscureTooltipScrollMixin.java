package net.farmingtales.tooltipscroll.mixin;

import dev.obscuria.tooltips.client.TooltipState;
import dev.obscuria.tooltips.client.tooltip.TooltipScroll;
import net.farmingtales.tooltipscroll.AutoScrollController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TooltipScroll.class, remap = false)
public abstract class ObscureTooltipScrollMixin {
    @Shadow private static float scroll;
    @Shadow private static float startScroll;
    @Shadow private static float endScroll;

    @Unique
    private static final AutoScrollController ftTooltipScroll$controller = new AutoScrollController();

    @Shadow
    private static float minScroll() {
        throw new AssertionError();
    }

    @Shadow
    private static float maxScroll() {
        throw new AssertionError();
    }

    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private static void ftTooltipScroll$autoScroll(TooltipState state, int tooltipHeight, int screenHeight,
                                                 CallbackInfo callback) {
        boolean overflowing = tooltipHeight > screenHeight && TooltipScroll.shouldCaptureInput();
        float offset = ftTooltipScroll$controller.update(state, overflowing, minScroll(), maxScroll(),
                scroll, System.nanoTime() / 1_000_000L);
        if (!Float.isNaN(offset)) {
            scroll = offset;
            startScroll = offset;
            endScroll = offset;
        }
    }

    @Inject(method = "onInput", at = @At("HEAD"), remap = false)
    private static void ftTooltipScroll$manualScroll(float amount, CallbackInfo callback) {
        if (amount != 0.0f) {
            ftTooltipScroll$controller.onManualInput(System.nanoTime() / 1_000_000L);
        }
    }
}
