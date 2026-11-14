/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 */
package net.farmingtales.ftboss.zheergenfx.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.farmingtales.ftboss.zheergenfx.LightQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class LightClient {
    private static final List<LightPacket> EFFECTS = new ArrayList<LightPacket>();
    private static ClientLevel previousWorld;

    private LightClient() {
    }

    public static void world(ClientLevel clientLevel) {
        if (clientLevel != previousWorld) {
            EFFECTS.clear();
            previousWorld = clientLevel;
        }
    }

    public static void accept(LightPacket lightPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        LightClient.world(clientLevel);
        if (clientLevel == null || !lightPacket.valid() || !lightPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return;
        }
        LightQueue.offer(EFFECTS, lightPacket, clientLevel.m_46467_());
    }

    public static List<LightPacket> snapshot(ClientLevel clientLevel) {
        LightClient.world(clientLevel);
        if (clientLevel == null) {
            return List.of();
        }
        LightQueue.retain(EFFECTS, clientLevel.m_46467_());
        return new ArrayList<LightPacket>(EFFECTS);
    }
}

