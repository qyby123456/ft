/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.GongdiSpellRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class GongdiSpellCues {
    private static final Map<UUID, Cue> ACTIVE = new LinkedHashMap<UUID, Cue>();
    private static ClientLevel world;

    private GongdiSpellCues() {
    }

    public static Cue update(Cue cue, String string, String string2, long l, int n) {
        boolean bl;
        if (n <= 0 || !GongdiSpellRenderer.owns(string) || !Set.of("CHARGE", "RELEASE", "PULSE", "BEAM").contains(string2)) {
            return cue;
        }
        if (string2.equals("BEAM")) {
            return cue;
        }
        boolean bl2 = bl = cue != null && cue.skill().equals(string) && cue.phase().equals(string2) && !GongdiSpellCues.expired(cue, l);
        if (bl && !string2.equals("CHARGE")) {
            return cue;
        }
        return new Cue(string, string2, bl ? cue.born() : l, l, n);
    }

    public static boolean expired(Cue cue, long l) {
        return l < cue.born() || l < cue.refreshed() || l - (cue.phase().equals("CHARGE") ? cue.refreshed() : cue.born()) >= (long)cue.lifetime();
    }

    public static Pose pose(Cue cue, double d) {
        if (cue == null || GongdiSpellCues.expired(cue, (long)Math.floor(d))) {
            return null;
        }
        double d2 = Math.max(0.0, Math.min(1.0, (d - (double)cue.born()) / (double)Math.max(1, cue.lifetime())));
        if (cue.skill().equals("blood_verdict") && cue.phase().equals("RELEASE")) {
            return new Pose(cue.skill(), false, 1.0);
        }
        return new Pose(cue.skill(), !cue.phase().equals("CHARGE"), d2);
    }

    public static void accept(SpellVisualPacket spellVisualPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (!(clientLevel != null && spellVisualPacket.valid() && GongdiSpellRenderer.owns(spellVisualPacket.spell().id) && clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension()))) {
            return;
        }
        if (world != clientLevel) {
            ACTIVE.clear();
            world = clientLevel;
        }
        long l = clientLevel.m_46467_();
        Cue cue = GongdiSpellCues.update(ACTIVE.get(spellVisualPacket.owner()), spellVisualPacket.spell().id, spellVisualPacket.phase().name(), l, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
        if (cue == null) {
            return;
        }
        if (ACTIVE.size() >= 64 && !ACTIVE.containsKey(spellVisualPacket.owner())) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(spellVisualPacket.owner(), cue);
    }

    public static Pose current(EasterBossEntity easterBossEntity, float f) {
        if (world == null || easterBossEntity.m_9236_() != world || easterBossEntity.getVariant() != 3 || !easterBossEntity.m_6084_()) {
            return null;
        }
        Cue cue = ACTIVE.get(easterBossEntity.m_20148_());
        if (cue != null && cue.phase().equals("CHARGE") && !easterBossEntity.isCastingLike() && world.m_46467_() - cue.born() > 2L) {
            ACTIVE.remove(easterBossEntity.m_20148_());
            return null;
        }
        return GongdiSpellCues.pose(cue, (float)world.m_46467_() + Math.max(0.0f, Math.min(1.0f, f)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
            return;
        }
        ACTIVE.values().removeIf(cue -> GongdiSpellCues.expired(cue, clientLevel.m_46467_()));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        world = null;
    }

    public record Cue(String skill, String phase, long born, long refreshed, int lifetime) {
    }

    public record Pose(String skill, boolean released, double progress) {
    }
}

