package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class SpellModelFacing {
    private SpellModelFacing() {}

    public static Vec3 direction(SpellVisualPacket packet, Entity owner, float partial) {
        if (owner != null && packet.phase() != Phase.BEAM && packet.spell().followsCaster(packet.phase())) {
            return owner.getViewVector(partial);
        }
        return packet.end().subtract(packet.start());
    }
}
