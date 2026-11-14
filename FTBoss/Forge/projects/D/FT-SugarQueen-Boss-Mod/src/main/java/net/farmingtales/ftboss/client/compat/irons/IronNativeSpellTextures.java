package net.farmingtales.ftboss.client.compat.irons;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.farmingtales.ftboss.util.IronNativeSpellRules.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public final class IronNativeSpellTextures {
    private static final Map<ResourceLocation, ResourceLocation> TINTED = new HashMap<>();

    private IronNativeSpellTextures() {}

    public static ResourceLocation texture(Style style) {
        ResourceLocation original = IronNativeSpellModels.texture(style);
        if (style == Style.ICE_CROWN) return original;
        return TINTED.computeIfAbsent(original, source -> {
            Minecraft minecraft = Minecraft.getInstance();
            try (var stream = minecraft.getResourceManager().getResourceOrThrow(source).open()) {
                NativeImage image = NativeImage.read(stream);
                for (int vertical = 0; vertical < image.getHeight(); vertical++) {
                    for (int horizontal = 0; horizontal < image.getWidth(); horizontal++) {
                        image.setPixelRGBA(horizontal, vertical, IronNativeSpellRules.monochrome(image.getPixelRGBA(horizontal, vertical)));
                    }
                }
                DynamicTexture texture = new DynamicTexture(image);
                return minecraft.getTextureManager().register("ftboss_iron_native_" + (style == Style.WINGS ? "wings" : "shield"), texture);
            } catch (IOException exception) {
                LogUtils.getLogger().warn("Could not tint native spell texture {}; using original", source, exception);
                return source;
            }
        });
    }

    public static void clear() {
        TINTED.forEach((original, tinted) -> {
            if (!original.equals(tinted)) Minecraft.getInstance().getTextureManager().release(tinted);
        });
        TINTED.clear();
    }
}
