/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.lemoncasting;

import io.netty.handler.codec.DecoderException;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingActor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record CastingPacket(UUID owner, ResourceLocation dimension, CastingClock.Cue cue) {
    public boolean valid() {
        return this.owner != null && this.dimension != null && CastingClock.valid(this.cue);
    }

    public static void encode(CastingPacket castingPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130077_(castingPacket.owner());
        friendlyByteBuf.m_130085_(castingPacket.dimension());
        CastingClock.Cue cue = castingPacket.cue();
        friendlyByteBuf.m_130070_(cue.spell());
        friendlyByteBuf.writeLong(cue.token());
        friendlyByteBuf.writeLong(cue.started());
        friendlyByteBuf.writeLong(cue.changed());
        friendlyByteBuf.writeInt(cue.duration());
        friendlyByteBuf.writeByte(cue.phase());
        friendlyByteBuf.writeBoolean(cue.primary());
    }

    public static CastingPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CastingPacket castingPacket = new CastingPacket(friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130281_(), new CastingClock.Cue(friendlyByteBuf.m_130136_(32), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), friendlyByteBuf.readInt(), friendlyByteBuf.readUnsignedByte(), friendlyByteBuf.readBoolean()));
        if (!castingPacket.valid()) {
            throw new DecoderException("Invalid Lemoncat casting cue");
        }
        return castingPacket;
    }

    public static void handle(CastingPacket castingPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && castingPacket.valid()) {
            DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> LemonCastingActor.receive(castingPacket));
        }
        context.setPacketHandled(true);
    }
}

