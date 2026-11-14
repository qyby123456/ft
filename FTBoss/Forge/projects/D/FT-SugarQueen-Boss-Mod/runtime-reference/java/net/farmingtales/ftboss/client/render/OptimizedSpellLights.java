/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.IronSpellLightParticles;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public final class OptimizedSpellLights {
    private OptimizedSpellLights() {
    }

    public static boolean emit(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            return false;
        }
        Camera camera = Minecraft.m_91087_().f_91063_.m_109153_();
        if (!camera.m_90594_() && camera.m_90583_().m_82557_(spellVisualPacket.start()) < 4.0) {
            return false;
        }
        return IronSpellLightParticles.emit(spellVisualPacket);
    }
}

