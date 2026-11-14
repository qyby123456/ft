/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.skill.SpellVisualPacket;

public interface OptimizedSpellVisual {
    public SpellVisualPacket packet();

    public long born();

    public long refreshed();

    public boolean nativeLight();
}

