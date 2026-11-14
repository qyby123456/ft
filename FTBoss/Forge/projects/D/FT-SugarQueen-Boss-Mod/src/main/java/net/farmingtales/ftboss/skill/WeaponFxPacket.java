package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.DoraGunFxVisual;
import net.farmingtales.ftboss.client.render.PufferBatFxVisual;
import net.farmingtales.ftboss.client.render.WeaponFxVisual;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record WeaponFxPacket(ResourceLocation dimension, Kind kind, Theme theme, Vec3 start, Vec3 end, float radius, float yaw) {
    public boolean valid() {
        return dimension != null && kind != null && theme != null && start != null && end != null
                && WeaponFxRules.validPosition(start.x) && WeaponFxRules.validPosition(start.y) && WeaponFxRules.validPosition(start.z)
                && WeaponFxRules.validPosition(end.x) && WeaponFxRules.validPosition(end.y) && WeaponFxRules.validPosition(end.z)
                && WeaponFxRules.validSize(radius, yaw, start.distanceToSqr(end));
    }

    public static void encode(WeaponFxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeEnum(packet.kind);
        buffer.writeEnum(packet.theme);
        buffer.writeDouble(packet.start.x).writeDouble(packet.start.y).writeDouble(packet.start.z);
        buffer.writeDouble(packet.end.x).writeDouble(packet.end.y).writeDouble(packet.end.z);
        buffer.writeFloat(packet.radius).writeFloat(packet.yaw);
    }

    public static WeaponFxPacket decode(FriendlyByteBuf buffer) {
        return new WeaponFxPacket(buffer.readResourceLocation(), buffer.readEnum(Kind.class), buffer.readEnum(Theme.class),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(WeaponFxPacket packet, Supplier<NetworkEvent.Context> supplier) {
        if (FMLEnvironment.dist == Dist.CLIENT && packet.valid()) {
            if (packet.kind().doraGun()) DoraGunFxVisual.receive(packet);
            else if (packet.kind().pufferBat()) PufferBatFxVisual.receive(packet);
            else WeaponFxVisual.receive(packet);
        }
        supplier.get().setPacketHandled(true);
    }
}
