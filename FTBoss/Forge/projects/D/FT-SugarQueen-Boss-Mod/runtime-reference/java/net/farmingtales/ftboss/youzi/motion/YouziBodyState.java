/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.youzi.motion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.motion.YouziBodyClock;
import net.farmingtales.ftboss.youzi.motion.YouziBodyPacket;
import net.farmingtales.ftboss.youzi.motion.YouziCharacterPose;
import net.farmingtales.ftboss.youzi.spells.YouziSpellPacket;
import net.farmingtales.ftboss.youzi.spells.YouziSpellTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class YouziBodyState {
    private static final Map<UUID, Active> ACTIVE = new LinkedHashMap<UUID, Active>();
    private static ClientLevel world;

    private static void synchronize() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
        }
    }

    private static boolean owner(int n, UUID uUID) {
        EasterBossEntity easterBossEntity;
        Entity entity;
        return world != null && (entity = world.m_6815_(n)) instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 0 && easterBossEntity.m_6084_() && easterBossEntity.m_20148_().equals(uUID);
    }

    private static void put(Active active) {
        if (ACTIVE.size() >= 128 && !ACTIVE.containsKey(active.actor())) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(active.actor(), active);
    }

    public static void summon(YouziBodyPacket youziBodyPacket) {
        YouziBodyState.synchronize();
        if (world == null || !world.m_46472_().m_135782_().toString().equals(youziBodyPacket.dimension()) || !YouziBodyState.owner(youziBodyPacket.actorId(), youziBodyPacket.actor()) || world.m_46467_() - youziBodyPacket.tick() > 18L || world.m_46467_() < youziBodyPacket.tick() - 2L) {
            return;
        }
        Active active = ACTIVE.get(youziBodyPacket.actor());
        if (active != null && (active.refreshed() >= youziBodyPacket.tick() || active.kind().equals("CHARGE"))) {
            return;
        }
        YouziBodyState.put(new Active(youziBodyPacket.actorId(), youziBodyPacket.actor(), "clone_summon", "SUMMON", youziBodyPacket.tick(), youziBodyPacket.tick(), 18));
    }

    public static void spell(YouziSpellPacket youziSpellPacket) {
        String string;
        if (youziSpellPacket.visual().phase() == SpellVisualRules.Phase.CHARGE && youziSpellPacket.castDuration() <= 0) {
            return;
        }
        YouziBodyState.synchronize();
        SpellVisualPacket spellVisualPacket = youziSpellPacket.visual();
        if (world == null || !world.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension()) || !YouziBodyState.owner(spellVisualPacket.ownerId(), spellVisualPacket.owner()) || spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            return;
        }
        switch (spellVisualPacket.spell().id) {
            case "effigy_volley": {
                String string2 = "effigy_volley_cast";
                break;
            }
            case "effigy_ward": {
                String string2 = "effigy_ward_cast";
                break;
            }
            case "effigy_procession": {
                String string2 = "effigy_procession_cast";
                break;
            }
            default: {
                String string2 = string = null;
            }
        }
        if (string == null || !YouziSpellTiming.eventFresh(world.m_46467_(), youziSpellPacket.sentTick(), spellVisualPacket.phase().name())) {
            return;
        }
        Object object = ACTIVE.get(spellVisualPacket.owner());
        if (object != null && !YouziSpellTiming.ordered(youziSpellPacket.sentTick(), spellVisualPacket.phase().name(), ((Active)object).refreshed(), ((Active)object).kind())) {
            return;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE) {
            if (object != null && ((Active)object).clip().equals(string) && ((Active)object).kind().equals("RELEASE")) {
                YouziBodyState.put(new Active(((Active)object).actorId(), ((Active)object).actor(), ((Active)object).clip(), ((Active)object).kind(), ((Active)object).started(), youziSpellPacket.sentTick(), ((Active)object).castDuration()));
            }
            return;
        }
        int n = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE ? 1 : 0;
        YouziBodyState.put(new Active(spellVisualPacket.ownerId(), spellVisualPacket.owner(), string, n != 0 ? "CHARGE" : "RELEASE", n != 0 ? youziSpellPacket.castStarted() : youziSpellPacket.sentTick(), youziSpellPacket.sentTick(), youziSpellPacket.castDuration()));
    }

    public static Motion sample(EasterBossEntity easterBossEntity, float f, YouziCharacterPose youziCharacterPose) {
        YouziBodyState.synchronize();
        Active active = ACTIVE.get(easterBossEntity.m_20148_());
        if (active == null || !YouziBodyState.owner(active.actorId(), active.actor())) {
            return null;
        }
        double d = (float)world.m_46467_() + f;
        YouziBodyClock.Sample sample = YouziBodyClock.sample(active.kind(), d - (double)active.started(), active.castDuration(), youziCharacterPose.duration(active.clip()), youziCharacterPose.chargeEnd(active.clip()), d - (double)active.refreshed());
        if (sample.expired()) {
            ACTIVE.remove(active.actor());
            return null;
        }
        return new Motion(active.clip(), sample.phase(), sample.weight());
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        YouziBodyState.synchronize();
        if (world != null) {
            ACTIVE.values().removeIf(active -> !YouziBodyState.owner(active.actorId(), active.actor()) || (active.kind().equals("CHARGE") ? world.m_46467_() - active.refreshed() > 8L : world.m_46467_() - active.started() > 100L));
        }
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

    private record Active(int actorId, UUID actor, String clip, String kind, long started, long refreshed, int castDuration) {
    }

    public record Motion(String clip, double phase, double weight) {
    }
}

