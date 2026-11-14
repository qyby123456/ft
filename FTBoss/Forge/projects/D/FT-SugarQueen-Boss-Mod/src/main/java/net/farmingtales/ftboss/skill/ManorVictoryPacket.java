package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorVictoryVisual;
import net.farmingtales.ftboss.util.ManorVictoryGeometry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record ManorVictoryPacket(UUID id, ResourceLocation dimension, Vec3 center, int age) {
    public boolean valid() {
        return age >= 0 && age < ManorVictoryGeometry.DURATION && Double.isFinite(center.x) && Double.isFinite(center.y)
                && Double.isFinite(center.z) && Math.abs(center.x) <= 30000000 && Math.abs(center.z) <= 30000000 && Math.abs(center.y) <= 30000000;
    }

    public static void encode(ManorVictoryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.id);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeDouble(packet.center.x);
        buffer.writeDouble(packet.center.y);
        buffer.writeDouble(packet.center.z);
        buffer.writeVarInt(packet.age);
    }

    public static ManorVictoryPacket decode(FriendlyByteBuf buffer) {
        return new ManorVictoryPacket(buffer.readUUID(), buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readVarInt());
    }

    public static void handle(ManorVictoryPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) ManorVictoryVisual.receive(packet);
    }
}
