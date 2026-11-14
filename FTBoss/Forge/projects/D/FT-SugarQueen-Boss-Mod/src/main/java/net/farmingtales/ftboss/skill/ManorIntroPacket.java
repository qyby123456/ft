package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.client.render.ManorIntroVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record ManorIntroPacket(UUID id, ResourceLocation dimension, Vec3 position, long elapsedMillis, boolean active) {
    public static void encode(ManorIntroPacket message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.id);
        buffer.writeResourceLocation(message.dimension);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeVarLong(message.elapsedMillis);
        buffer.writeBoolean(message.active);
    }

    public static ManorIntroPacket decode(FriendlyByteBuf buffer) {
        return new ManorIntroPacket(buffer.readUUID(), buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readVarLong(), buffer.readBoolean());
    }

    public static void handle(ManorIntroPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) ManorIntroVisual.receive(message);
    }
}
