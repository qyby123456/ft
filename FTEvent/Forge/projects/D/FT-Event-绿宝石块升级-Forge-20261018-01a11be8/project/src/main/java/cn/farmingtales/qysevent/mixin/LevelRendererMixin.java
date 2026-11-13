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
    // Forge's 1.20.1 runtime keeps the SRG name even when the mod is reobfuscated.
    private static final String PRECIPITATION_METHOD =
            "m_109703_(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";

    @Inject(method = PRECIPITATION_METHOD, at = @At("HEAD"), remap = false)
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

    @Inject(method = PRECIPITATION_METHOD, at = @At("RETURN"), remap = false)
    private void qys_event$endPrecipitationTint(LightTexture lightTexture, float partialTick,
                                                   double cameraX, double cameraY, double cameraZ,
                                                   CallbackInfo callback) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
