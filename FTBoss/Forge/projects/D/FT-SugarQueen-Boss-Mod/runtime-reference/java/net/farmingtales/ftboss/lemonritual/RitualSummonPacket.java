/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.lemonritual;

import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.lemonritual.RitualTimeline;
import net.farmingtales.ftboss.lemonritual.client.RitualVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record RitualSummonPacket(UUID owner, UUID subject, ResourceLocation dimension, Vec3 position, long started, float width, float height) {
    public RitualTimeline.Cue cue() {
        return new RitualTimeline.Cue(false, this.owner, this.subject, this.dimension.toString(), new double[]{this.position.f_82479_, this.position.f_82480_, this.position.f_82481_}, List.of(), this.started, this.width, this.height);
    }

    public boolean valid() {
        return this.dimension != null && this.position != null && RitualTimeline.valid(this.cue());
    }

    public static void encode(RitualSummonPacket ritualSummonPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130077_(ritualSummonPacket.owner());
        friendlyByteBuf.m_130077_(ritualSummonPacket.subject());
        friendlyByteBuf.m_130085_(ritualSummonPacket.dimension());
        friendlyByteBuf.writeDouble(ritualSummonPacket.position().f_82479_);
        friendlyByteBuf.writeDouble(ritualSummonPacket.position().f_82480_);
        friendlyByteBuf.writeDouble(ritualSummonPacket.position().f_82481_);
        friendlyByteBuf.writeLong(ritualSummonPacket.started());
        friendlyByteBuf.writeFloat(ritualSummonPacket.width());
        friendlyByteBuf.writeFloat(ritualSummonPacket.height());
    }

    public static RitualSummonPacket decode(FriendlyByteBuf friendlyByteBuf) {
        RitualSummonPacket ritualSummonPacket = new RitualSummonPacket(friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130281_(), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.readLong(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
        if (!ritualSummonPacket.valid()) {
            throw new DecoderException("Invalid Lemoncat summon visual");
        }
        return ritualSummonPacket;
    }

    public static void handle(RitualSummonPacket ritualSummonPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && ritualSummonPacket.valid()) {
            DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> RitualVisual.receiveSummon(ritualSummonPacket));
        }
        context.setPacketHandled(true);
    }
}

