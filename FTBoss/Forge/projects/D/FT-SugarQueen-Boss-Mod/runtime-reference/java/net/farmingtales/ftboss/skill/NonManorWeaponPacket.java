/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.boss.ai.SparrowDisruption;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public record NonManorWeaponPacket(ResourceLocation item) {
    public static void encode(NonManorWeaponPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.item);
    }

    public static NonManorWeaponPacket decode(FriendlyByteBuf buffer) {
        return new NonManorWeaponPacket(buffer.m_130281_());
    }

    public static void handle(NonManorWeaponPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getSender() != null) {
            SparrowDisruption.castWeaponSkill(context.getSender(), packet.item);
        }
        context.setPacketHandled(true);
    }
}

