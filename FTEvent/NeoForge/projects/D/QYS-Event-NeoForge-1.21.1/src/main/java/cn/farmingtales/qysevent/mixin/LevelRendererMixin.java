package cn.farmingtales.qysevent.mixin;

import cn.farmingtales.qysevent.client.ClientWeatherState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies one shader multiplier around vanilla precipitation rendering. This is
 * constant work per frame and does not allocate or spawn weather particles.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void qys_event$beginPrecipitationTint(LightTexture lightTexture, float partialTick,
                                                     double cameraX, double cameraY, double cameraZ,
                                                     CallbackInfo callback) {
        int color = ClientWeatherState.precipitationColor();
        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                1.0F
        );
    }

    @Inject(method = "renderSnowAndRain", at = @At("RETURN"))
    private void qys_event$endPrecipitationTint(LightTexture lightTexture, float partialTick,
                                                   double cameraX, double cameraY, double cameraZ,
                                                   CallbackInfo callback) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}


