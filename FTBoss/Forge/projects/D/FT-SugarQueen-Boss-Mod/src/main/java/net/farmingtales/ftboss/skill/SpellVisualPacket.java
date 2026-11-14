package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record SpellVisualPacket(ResourceLocation dimension, int ownerId, UUID owner, Spell spell,
        Phase phase, Vec3 start, Vec3 end) {
    public boolean valid() {
        return dimension != null && ownerId >= 0 && owner != null && spell != null && phase != null
                && valid(start) && valid(end) && start.distanceToSqr(end) <= (spell == Spell.STAR_CROSS ? 128 * 128 : 64 * 64);
    }

    private static boolean valid(Vec3 point) {
        return point != null && SpellVisualRules.validPoint(point.x) && SpellVisualRules.validPoint(point.y)
                && SpellVisualRules.validPoint(point.z);
    }

    public static void encode(SpellVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.ownerId);
        buffer.writeUUID(packet.owner);
        buffer.writeEnum(packet.spell);
        buffer.writeEnum(packet.phase);
        buffer.writeDouble(packet.start.x).writeDouble(packet.start.y).writeDouble(packet.start.z);
        buffer.writeDouble(packet.end.x).writeDouble(packet.end.y).writeDouble(packet.end.z);
    }

    public static SpellVisualPacket decode(FriendlyByteBuf buffer) {
        return new SpellVisualPacket(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readUUID(),
                buffer.readEnum(Spell.class), buffer.readEnum(Phase.class),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(SpellVisualPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> net.farmingtales.ftboss.client.render.SpellVisualRenderer.accept(packet));
        context.get().setPacketHandled(true);
    }
}
