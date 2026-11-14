/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.sound.PlaySoundEvent
 *  net.minecraftforge.client.event.sound.SoundEngineLoadEvent
 *  net.minecraftforge.client.event.sound.SoundEvent$SoundSourceEvent
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.sound.BossMusicPacket;
import net.farmingtales.ftboss.util.BossMusicPlayback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class BossMusicClient {
    private static final BossMusicPlayback<SoundInstance> PLAYER = new BossMusicPlayback<SoundInstance>(new BossMusicPlayback.Backend<SoundInstance>(){

        @Override
        public SoundInstance create(String track) {
            SoundEvent event = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(track));
            return event == null ? null : SimpleSoundInstance.m_119745_((SoundEvent)event);
        }

        @Override
        public void play(SoundInstance sound) {
            Minecraft.m_91087_().m_91397_().m_120186_();
            Minecraft.m_91087_().m_91106_().m_120367_(sound);
            FTBossMod.LOGGER.info("[FTBoss][Music] single-channel start {}", (Object)sound.m_7904_());
        }

        @Override
        public void stop(SoundInstance sound) {
            Minecraft.m_91087_().m_91106_().m_120399_(sound);
            FTBossMod.LOGGER.info("[FTBoss][Music] single-channel stop {}", (Object)sound.m_7904_());
        }

        @Override
        public boolean active(SoundInstance sound) {
            return Minecraft.m_91087_().m_91106_().m_120403_(sound);
        }
    });
    private static BossMusicPacket pending;
    private static long receivedRevision;

    private BossMusicClient() {
    }

    public static void receive(BossMusicPacket packet) {
        if (packet.revision() <= receivedRevision) {
            return;
        }
        receivedRevision = packet.revision();
        pending = packet;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || PLAYER.owns(sound)) {
            return;
        }
        ResourceLocation location = sound.m_7904_();
        if (location != null && location.m_135827_().equals("ftboss") && location.m_135815_().startsWith("boss_music_") || PLAYER.busy() && sound.m_8070_() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onSourceReady(SoundEvent.SoundSourceEvent event) {
        ResourceLocation location = event.getSound().m_7904_();
        if (location.m_135827_().equals("ftboss") && location.m_135815_().startsWith("boss_music_") && !PLAYER.owns(event.getSound())) {
            event.getChannel().m_83679_();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91074_ == null) {
            PLAYER.suspend();
            return;
        }
        if (pending != null && minecraft.f_91073_.m_46472_().m_135782_().equals((Object)pending.dimension())) {
            PLAYER.accept(pending.revision(), pending.encounter().toString(), pending.tracks().stream().map(Object::toString).toList());
            pending = null;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        if (PLAYER.busy()) {
            minecraft.m_91397_().m_120186_();
        }
        PLAYER.tick();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            PLAYER.suspend();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        PLAYER.clear();
        pending = null;
        receivedRevision = -1L;
    }

    static {
        receivedRevision = -1L;
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static final class ReloadEvents {
        private ReloadEvents() {
        }

        @SubscribeEvent
        public static void soundReload(SoundEngineLoadEvent event) {
            Minecraft minecraft = Minecraft.m_91087_();
            if (minecraft != null) {
                minecraft.execute(PLAYER::suspend);
            }
        }
    }
}

