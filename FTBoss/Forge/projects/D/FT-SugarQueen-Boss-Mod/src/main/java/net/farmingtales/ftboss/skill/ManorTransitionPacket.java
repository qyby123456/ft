package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorTransitionVisual;
import net.farmingtales.ftboss.util.ManorCeremonyGeometry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record ManorTransitionPacket(UUID id, ResourceLocation dimension, Vec3 center, int variant, int age, boolean active) {
    public boolean valid() {
        return id != null && dimension != null && center != null && Double.isFinite(center.x + center.y + center.z)
                && Math.abs(center.x) <= 30000000 && Math.abs(center.y) <= 30000000 && Math.abs(center.z) <= 30000000
                && variant >= -1 && variant < 12 && variant != 9 && age >= 0
                && age < ManorCeremonyGeometry.TRANSITION_DURATION;
    }

    public static void encode(ManorTransitionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.id);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeDouble(packet.center.x);
        buffer.writeDouble(packet.center.y);
        buffer.writeDouble(packet.center.z);
        buffer.writeInt(packet.variant);
        buffer.writeVarInt(packet.age);
        buffer.writeBoolean(packet.active);
    }

    public static ManorTransitionPacket decode(FriendlyByteBuf buffer) {
        return new ManorTransitionPacket(buffer.readUUID(), buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(ManorTransitionPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) ManorTransitionVisual.receive(packet);
    }
}
