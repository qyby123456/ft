package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

/** C2S request carried by the eagerly registered {@link FTNetwork} channel. */
public record NonManorWeaponPacket(ResourceLocation item) {
    public static void encode(NonManorWeaponPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.item);
    }

    public static NonManorWeaponPacket decode(FriendlyByteBuf buffer) {
        return new NonManorWeaponPacket(buffer.readResourceLocation());
    }

    public static void handle(NonManorWeaponPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getSender() != null) net.farmingtales.ftboss.boss.ai.SparrowDisruption.castWeaponSkill(context.getSender(), packet.item);
        context.setPacketHandled(true);
    }
}
