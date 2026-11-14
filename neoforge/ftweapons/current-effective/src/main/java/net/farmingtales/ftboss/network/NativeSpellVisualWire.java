package net.farmingtales.ftboss.network;

import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class NativeSpellVisualWire {
    private NativeSpellVisualWire() {
    }

    public static void encode(SpellVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeVarInt(packet.ownerId());
        buffer.writeUUID(packet.owner());
        buffer.writeEnum(packet.spell());
        buffer.writeEnum(packet.phase());
        buffer.writeDouble(packet.start().x).writeDouble(packet.start().y).writeDouble(packet.start().z);
        buffer.writeDouble(packet.end().x).writeDouble(packet.end().y).writeDouble(packet.end().z);
    }

    public static SpellVisualPacket decode(FriendlyByteBuf buffer) {
        return new SpellVisualPacket(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readUUID(), buffer.readEnum(SpellVisualRules.Spell.class), buffer.readEnum(SpellVisualRules.Phase.class), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }
}
