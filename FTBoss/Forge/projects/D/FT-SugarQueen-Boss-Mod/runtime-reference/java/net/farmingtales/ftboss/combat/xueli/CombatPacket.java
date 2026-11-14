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
package net.farmingtales.ftboss.combat.xueli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.xueli_combat.XueliCombatRenderer;
import net.farmingtales.ftboss.combat.xueli.CombatRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CombatPacket(ResourceLocation dimension, int ownerId, UUID owner, int kind, long stamp, Vec3 origin, double radius, int duration, double progress, float yaw, List<Anchor> guards) {
    private static boolean position(Vec3 vec3) {
        return vec3 != null && Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_) && Math.abs(vec3.f_82479_) <= 3.0E7 && Math.abs(vec3.f_82481_) <= 3.0E7 && Math.abs(vec3.f_82480_) < 4096.0;
    }

    public boolean valid() {
        return this.dimension != null && this.ownerId >= 0 && this.owner != null && this.stamp >= 0L && Float.isFinite(this.yaw) && CombatPacket.position(this.origin) && this.guards != null && CombatRules.valid(this.kind, this.duration, this.radius, this.progress, this.guards.size()) && this.guards.stream().allMatch(anchor -> anchor.entityId() >= 0 && anchor.uuid() != null && CombatPacket.position(anchor.position()));
    }

    private static void point(FriendlyByteBuf friendlyByteBuf, Vec3 vec3) {
        friendlyByteBuf.writeDouble(vec3.f_82479_);
        friendlyByteBuf.writeDouble(vec3.f_82480_);
        friendlyByteBuf.writeDouble(vec3.f_82481_);
    }

    private static Vec3 point(FriendlyByteBuf friendlyByteBuf) {
        return new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
    }

    public static void encode(CombatPacket combatPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130085_(combatPacket.dimension());
        friendlyByteBuf.m_130130_(combatPacket.ownerId());
        friendlyByteBuf.m_130077_(combatPacket.owner());
        friendlyByteBuf.writeByte(combatPacket.kind());
        friendlyByteBuf.writeLong(combatPacket.stamp());
        CombatPacket.point(friendlyByteBuf, combatPacket.origin());
        friendlyByteBuf.writeDouble(combatPacket.radius());
        friendlyByteBuf.m_130130_(combatPacket.duration());
        friendlyByteBuf.writeDouble(combatPacket.progress());
        friendlyByteBuf.writeFloat(combatPacket.yaw());
        friendlyByteBuf.writeByte(combatPacket.guards().size());
        for (Anchor anchor : combatPacket.guards()) {
            friendlyByteBuf.m_130130_(anchor.entityId());
            friendlyByteBuf.m_130077_(anchor.uuid());
            CombatPacket.point(friendlyByteBuf, anchor.position());
        }
    }

    public static CombatPacket decode(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.m_130281_();
        int n = friendlyByteBuf.m_130242_();
        UUID uUID = friendlyByteBuf.m_130259_();
        short s = friendlyByteBuf.readUnsignedByte();
        long l = friendlyByteBuf.readLong();
        Vec3 vec3 = CombatPacket.point(friendlyByteBuf);
        double d = friendlyByteBuf.readDouble();
        int n2 = friendlyByteBuf.m_130242_();
        double d2 = friendlyByteBuf.readDouble();
        float f = friendlyByteBuf.readFloat();
        int n3 = friendlyByteBuf.readUnsignedByte();
        if (n3 > 6) {
            throw new IllegalArgumentException("Unbounded maid snapshot");
        }
        ArrayList<Anchor> arrayList = new ArrayList<Anchor>();
        for (int i = 0; i < n3; ++i) {
            arrayList.add(new Anchor(friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), CombatPacket.point(friendlyByteBuf)));
        }
        CombatPacket combatPacket = new CombatPacket(resourceLocation, n, uUID, s, l, vec3, d, n2, d2, f, List.copyOf(arrayList));
        if (!combatPacket.valid()) {
            throw new IllegalArgumentException("Invalid Xueli combat snapshot");
        }
        return combatPacket;
    }

    public static void handle(CombatPacket combatPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> XueliCombatRenderer.accept(combatPacket)));
        context.setPacketHandled(true);
    }

    public record Anchor(int entityId, UUID uuid, Vec3 position) {
    }
}

