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
package net.farmingtales.ftboss.dandaniron;

import java.util.Set;
import java.util.function.Supplier;
import net.farmingtales.ftboss.dandaniron.client.IronRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public record IronPacket(SpellVisualPacket visual, long cast, long start, long event, int ticks, float scale, boolean cancelled) {
    public static boolean matches(String string) {
        return Set.of("candle_reap", "candle_prison", "candle_eclipse").contains(string);
    }

    public boolean valid() {
        return this.visual != null && this.visual.valid() && IronPacket.matches(this.visual.spell().id) && this.cast >= 0L && this.start >= 0L && this.event >= this.start && this.event - this.start <= 1200L && this.ticks > 0 && this.ticks <= 1200 && Float.isFinite(this.scale) && this.scale >= 0.25f && this.scale <= 8.0f;
    }

    public static void encode(IronPacket ironPacket, FriendlyByteBuf friendlyByteBuf) {
        SpellVisualPacket.encode(ironPacket.visual(), friendlyByteBuf);
        friendlyByteBuf.writeLong(ironPacket.cast()).writeLong(ironPacket.start()).writeLong(ironPacket.event()).writeInt(ironPacket.ticks()).writeFloat(ironPacket.scale()).writeBoolean(ironPacket.cancelled());
    }

    public static IronPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new IronPacket(SpellVisualPacket.decode(friendlyByteBuf), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), friendlyByteBuf.readLong(), friendlyByteBuf.readInt(), friendlyByteBuf.readFloat(), friendlyByteBuf.readBoolean());
    }

    public static void handle(IronPacket ironPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && ironPacket.valid()) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> IronRenderer.receive(ironPacket)));
        }
        context.setPacketHandled(true);
    }
}

