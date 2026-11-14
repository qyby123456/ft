package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.BossImpactVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record BossImpactPacket(ResourceLocation dimension, Vec3 position, Vec3 normal, float size,
                               float strength, int color, boolean shield) {
    public boolean valid() {
        return dimension != null && position != null && normal != null
                && validCoordinate(position.x) && validCoordinate(position.y) && validCoordinate(position.z)
                && Double.isFinite(normal.lengthSqr()) && Math.abs(normal.lengthSqr() - 1) < 0.01
                && Float.isFinite(size) && size >= 0.35f && size <= 1.5f
                && Float.isFinite(strength) && strength > 0 && strength <= 1 && color >= 0 && color <= 0xffffff;
    }

    private static boolean validCoordinate(double value) {
        return Double.isFinite(value) && Math.abs(value) <= 30_000_064;
    }

    public static void encode(BossImpactPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeDouble(packet.position.x).writeDouble(packet.position.y).writeDouble(packet.position.z);
        buffer.writeFloat((float) packet.normal.x).writeFloat((float) packet.normal.y).writeFloat((float) packet.normal.z);
        buffer.writeFloat(packet.size).writeFloat(packet.strength).writeInt(packet.color).writeBoolean(packet.shield);
    }

    public static BossImpactPacket decode(FriendlyByteBuf buffer) {
        return new BossImpactPacket(buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()),
                buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(BossImpactPacket packet, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT && packet.valid()) BossImpactVisual.receive(packet);
        context.get().setPacketHandled(true);
    }
}
