package net.farmingtales.ftboss.skill;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LemonCatFxPacket(ResourceLocation dimension, Vec3 position, float yaw, int kind) {
    public static void encode(LemonCatFxPacket message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.dimension);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.yaw);
        buffer.writeByte(message.kind);
    }

    public static LemonCatFxPacket decode(FriendlyByteBuf buffer) {
        return new LemonCatFxPacket(buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readUnsignedByte());
    }

    public static void handle(LemonCatFxPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            net.farmingtales.ftboss.client.render.LemonCatSkillVisual.receive(message);
        }
    }
}
