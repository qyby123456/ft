package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.skill.SpellVisualPacket;

public interface OptimizedSpellVisual {
    SpellVisualPacket packet();

    long born();

    long refreshed();

    boolean nativeLight();
}
