package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME,
    value = {Dist.CLIENT}
)
public final class GongdiSpellCues {
    private static final Map<UUID, GongdiSpellCues.Cue> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;

    private GongdiSpellCues() {
    }

    public static GongdiSpellCues.Cue update(GongdiSpellCues.Cue var0, String var1, String var2, long var3, int var5) {
        if (var5 <= 0 || !GongdiSpellRenderer.owns(var1) || !Set.of("CHARGE", "RELEASE", "PULSE", "BEAM").contains(var2)) {
            return var0;
        } else if (var2.equals("BEAM")) {
            return var0;
        } else {
            boolean var6 = var0 != null && var0.skill().equals(var1) && var0.phase().equals(var2) && !expired(var0, var3);
            return var6 && !var2.equals("CHARGE") ? var0 : new GongdiSpellCues.Cue(var1, var2, var6 ? var0.born() : var3, var3, var5);
        }
    }

    public static boolean expired(GongdiSpellCues.Cue var0, long var1) {
        return var1 < var0.born()
            || var1 < var0.refreshed()
            || var1 - (var0.phase().equals("CHARGE") ? var0.refreshed() : var0.born()) >= (long)var0.lifetime();
    }

    public static GongdiSpellCues.Pose pose(GongdiSpellCues.Cue var0, double var1) {
        if (var0 != null && !expired(var0, (long)Math.floor(var1))) {
            double var3 = Math.max(0.0, Math.min(1.0, (var1 - (double)var0.born()) / (double)Math.max(1, var0.lifetime())));
            return var0.skill().equals("blood_verdict") && var0.phase().equals("RELEASE")
                ? new GongdiSpellCues.Pose(var0.skill(), false, 1.0)
                : new GongdiSpellCues.Pose(var0.skill(), !var0.phase().equals("CHARGE"), var3);
        } else {
            return null;
        }
    }

    public static void accept(SpellVisualPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var0.valid() && GongdiSpellRenderer.owns(var0.spell().id) && var1.dimension().location().equals(var0.dimension())) {
            if (world != var1) {
                ACTIVE.clear();
                world = var1;
            }

            long var2 = var1.getGameTime();
            GongdiSpellCues.Cue var4 = update(ACTIVE.get(var0.owner()), var0.spell().id, var0.phase().name(), var2, var0.spell().lifetime(var0.phase()));
            if (var4 != null) {
                if (ACTIVE.size() >= 64 && !ACTIVE.containsKey(var0.owner())) {
                    ACTIVE.remove(ACTIVE.keySet().iterator().next());
                }

                ACTIVE.put(var0.owner(), var4);
            }
        }
    }

    public static GongdiSpellCues.Pose current(EasterBossEntity var0, float var1) {
        if (world != null && var0.level() == world && var0.getVariant() == 3 && var0.isAlive()) {
            GongdiSpellCues.Cue var2 = ACTIVE.get(var0.getUUID());
            if (var2 != null && var2.phase().equals("CHARGE") && !var0.isCastingLike() && world.getGameTime() - var2.born() > 2L) {
                ACTIVE.remove(var0.getUUID());
                return null;
            } else {
                return pose(var2, (double)((float)world.getGameTime() + Math.max(0.0F, Math.min(1.0F, var1))));
            }
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var1 == world) {
            ACTIVE.values().removeIf(var1x -> expired(var1x, var1.getGameTime()));
        } else {
            ACTIVE.clear();
            world = var1;
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        ACTIVE.clear();
        world = null;
    }

    public static record Cue(String skill, String phase, long born, long refreshed, int lifetime) {
    }

    public static record Pose(String skill, boolean released, double progress) {
    }
}
