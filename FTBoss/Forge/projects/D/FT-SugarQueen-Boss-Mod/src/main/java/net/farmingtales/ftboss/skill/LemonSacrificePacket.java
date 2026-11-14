package net.farmingtales.ftboss.skill;

import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry.Point;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record LemonSacrificePacket(UUID owner, ResourceLocation dimension, Vec3 center, long started, List<Point> souls) {
    public LemonSacrificePacket { souls = List.copyOf(souls); }

    public boolean valid() {
        return owner != null && dimension != null && center != null && started >= 0
                && Double.isFinite(center.x) && Double.isFinite(center.y) && Double.isFinite(center.z)
                && Math.abs(center.x) <= 30_000_000 && Math.abs(center.y) <= 30_000_000 && Math.abs(center.z) <= 30_000_000
                && souls.size() <= LemonSacrificeGeometry.MAX_SOULS && souls.stream().allMatch(Point::valid);
    }

    public static void encode(LemonSacrificePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.owner);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeDouble(packet.center.x);
        buffer.writeDouble(packet.center.y);
        buffer.writeDouble(packet.center.z);
        buffer.writeLong(packet.started);
        buffer.writeVarInt(packet.souls.size());
        for (Point point : packet.souls) {
            buffer.writeDouble(point.horizontal());
            buffer.writeDouble(point.height());
            buffer.writeDouble(point.depth());
        }
    }

    public static LemonSacrificePacket decode(FriendlyByteBuf buffer) {
        UUID owner = buffer.readUUID();
        ResourceLocation dimension = buffer.readResourceLocation();
        Vec3 center = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        long started = buffer.readLong();
        int count = buffer.readVarInt();
        if (count < 0 || count > LemonSacrificeGeometry.MAX_SOULS) throw new DecoderException("Invalid sacrifice source count");
        List<Point> souls = new ArrayList<>(count);
        for (int index = 0; index < count; index++) souls.add(new Point(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        return new LemonSacrificePacket(owner, dimension, center, started, souls);
    }

    public static void handle(LemonSacrificePacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT)
            net.farmingtales.ftboss.client.render.LemonSacrificeVisual.receive(packet);
    }
}
