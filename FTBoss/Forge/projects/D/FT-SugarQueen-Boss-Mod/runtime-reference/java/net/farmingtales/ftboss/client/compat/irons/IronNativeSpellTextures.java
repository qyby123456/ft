/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package net.farmingtales.ftboss.client.compat.irons;

import net.farmingtales.ftboss.client.compat.irons.IronNativeSpellModels;
import net.farmingtales.ftboss.util.IronNativeSpellRules;
import net.minecraft.resources.ResourceLocation;

public final class IronNativeSpellTextures {
    private IronNativeSpellTextures() {
    }

    public static ResourceLocation texture(IronNativeSpellRules.Style style) {
        return IronNativeSpellModels.texture(style);
    }

    public static void clear() {
    }
}

