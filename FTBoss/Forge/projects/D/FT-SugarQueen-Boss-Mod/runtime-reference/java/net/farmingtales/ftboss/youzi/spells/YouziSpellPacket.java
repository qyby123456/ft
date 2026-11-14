/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.youzi.spells;

import java.util.function.Supplier;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.youzi.motion.YouziBodyState;
import net.farmingtales.ftboss.youzi.spells.client.YouziSpellVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record YouziSpellPacket(SpellVisualPacket visual, long sentTick, long castStarted, int castDuration, double scale) {
    public YouziSpellPacket {
        if (spellVisualPacket == null || !spellVisualPacket.valid() || l < 0L || l2 < 0L || n < 0 || n > 12000 || !Double.isFinite(d) || d <= 0.0 || d > 4.0) {
            throw new IllegalArgumentException("Invalid authoritative Youzi visual event");
        }
    }

    public static void encode(YouziSpellPacket youziSpellPacket, FriendlyByteBuf friendlyByteBuf) {
        SpellVisualPacket.encode(youziSpellPacket.visual(), friendlyByteBuf);
        friendlyByteBuf.writeLong(youziSpellPacket.sentTick());
        friendlyByteBuf.writeLong(youziSpellPacket.castStarted());
        friendlyByteBuf.m_130130_(youziSpellPacket.castDuration());
        friendlyByteBuf.writeDouble(youziSpellPacket.scale());
    }

    public static YouziSpellPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new YouziSpellPacket(SpellVisualPacket.decode(friendlyByteBuf), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), friendlyByteBuf.m_130242_(), friendlyByteBuf.readDouble());
    }

    public static void handle(YouziSpellPacket youziSpellPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> {
                YouziSpellVisual.acceptAuthoritative(youziSpellPacket);
                YouziBodyState.spell(youziSpellPacket);
            }));
        }
        context.setPacketHandled(true);
    }
}

