/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtBridge;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record SpellVisualPacket(ResourceLocation dimension, int ownerId, UUID owner, SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, Vec3 start, Vec3 end) {
    public boolean valid() {
        return this.dimension != null && this.ownerId >= 0 && this.owner != null && this.spell != null && this.phase != null && SpellVisualPacket.valid(this.start) && SpellVisualPacket.valid(this.end) && this.start.m_82557_(this.end) <= 4096.0;
    }

    private static boolean valid(Vec3 point) {
        return point != null && SpellVisualRules.validPoint(point.f_82479_) && SpellVisualRules.validPoint(point.f_82480_) && SpellVisualRules.validPoint(point.f_82481_);
    }

    public static void encode(SpellVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.dimension);
        buffer.m_130130_(packet.ownerId);
        buffer.m_130077_(packet.owner);
        buffer.m_130068_((Enum)packet.spell);
        buffer.m_130068_((Enum)packet.phase);
        buffer.writeDouble(packet.start.f_82479_).writeDouble(packet.start.f_82480_).writeDouble(packet.start.f_82481_);
        buffer.writeDouble(packet.end.f_82479_).writeDouble(packet.end.f_82480_).writeDouble(packet.end.f_82481_);
    }

    public static SpellVisualPacket decode(FriendlyByteBuf buffer) {
        return new SpellVisualPacket(buffer.m_130281_(), buffer.m_130242_(), buffer.m_130259_(), (SpellVisualRules.Spell)buffer.m_130066_(SpellVisualRules.Spell.class), (SpellVisualRules.Phase)buffer.m_130066_(SpellVisualRules.Phase.class), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(SpellVisualPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid()) {
            DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> LemoncatArtBridge.accept(packet));
        }
        context.get().setPacketHandled(true);
    }
}

