package net.farmingtales.ftboss.zheergenfx.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.farmingtales.ftboss.zheergenfx.LightQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class LightClient {
    private static final List<LightPacket> EFFECTS = new ArrayList<>();
    private static ClientLevel previousWorld;

    private LightClient() {
    }

    public static void world(ClientLevel var0) {
        if (var0 != previousWorld) {
            EFFECTS.clear();
            previousWorld = var0;
        }
    }

    public static void accept(LightPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        world(var1);
        if (var1 != null && var0.valid() && var0.dimension().equals(var1.dimension().location())) {
            LightQueue.offer(EFFECTS, var0, var1.getGameTime());
        }
    }

    public static List<LightPacket> snapshot(ClientLevel var0) {
        world(var0);
        if (var0 == null) {
            return List.of();
        } else {
            LightQueue.retain(EFFECTS, var0.getGameTime());
            return new ArrayList<>(EFFECTS);
        }
    }
}
