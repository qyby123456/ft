/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.lemonritual.client.RitualVisual;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record LemonSacrificePacket(UUID owner, ResourceLocation dimension, Vec3 center, long started, List<LemonSacrificeGeometry.Point> souls) {
    public LemonSacrificePacket {
        souls = List.copyOf(souls);
    }

    public boolean valid() {
        return this.owner != null && this.dimension != null && this.center != null && this.started >= 0L && Double.isFinite(this.center.f_82479_) && Double.isFinite(this.center.f_82480_) && Double.isFinite(this.center.f_82481_) && Math.abs(this.center.f_82479_) <= 3.0E7 && Math.abs(this.center.f_82480_) <= 3.0E7 && Math.abs(this.center.f_82481_) <= 3.0E7 && this.souls.size() <= 12 && this.souls.stream().allMatch(LemonSacrificeGeometry.Point::valid);
    }

    public static void encode(LemonSacrificePacket packet, FriendlyByteBuf buffer) {
        buffer.m_130077_(packet.owner);
        buffer.m_130085_(packet.dimension);
        buffer.writeDouble(packet.center.f_82479_);
        buffer.writeDouble(packet.center.f_82480_);
        buffer.writeDouble(packet.center.f_82481_);
        buffer.writeLong(packet.started);
        buffer.m_130130_(packet.souls.size());
        for (LemonSacrificeGeometry.Point point : packet.souls) {
            buffer.writeDouble(point.horizontal());
            buffer.writeDouble(point.height());
            buffer.writeDouble(point.depth());
        }
    }

    public static LemonSacrificePacket decode(FriendlyByteBuf buffer) {
        UUID owner = buffer.m_130259_();
        ResourceLocation dimension = buffer.m_130281_();
        Vec3 center = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        long started = buffer.readLong();
        int count = buffer.m_130242_();
        if (count < 0 || count > 12) {
            throw new DecoderException("Invalid sacrifice source count");
        }
        ArrayList<LemonSacrificeGeometry.Point> souls = new ArrayList<LemonSacrificeGeometry.Point>(count);
        for (int index = 0; index < count; ++index) {
            souls.add(new LemonSacrificeGeometry.Point(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new LemonSacrificePacket(owner, dimension, center, started, souls);
    }

    public static void handle(LemonSacrificePacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) {
            RitualVisual.receive(packet);
        }
    }
}

